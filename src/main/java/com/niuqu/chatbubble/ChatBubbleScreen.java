package com.niuqu.chatbubble;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.CommandSuggestions;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.PlayerFaceRenderer;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import com.mojang.blaze3d.platform.NativeImage;
import java.io.InputStream;

import com.niuqu.chatbubble.mixin.MinecraftServerAccessor;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ChatBubbleScreen extends Screen {

    // Layout
    private int panelX, panelW;
    private static final int TITLE_H = 24;
    private int titleY, msgTop, msgBottom, barTop;
    private static final int PAD = 10;
    private static final int AVATAR = 20;
    private static final int BUBBLE_PAD_X = 8;
    private static final int BUBBLE_PAD_Y = 5;
    private static final int GAP = 6;
    private static final int NAME_H = 10;
    private static final int TIME_SEP_H = 14;
    private static final int BAR_H = 38;

    private static final int ICON_S = 16;
    private static final ResourceLocation TEX_GEAR = new ResourceLocation("e33chat","textures/gui/settings");
    private static final ResourceLocation TEX_SEND = new ResourceLocation("e33chat","textures/gui/send");
    private static boolean iconsLoaded;

    private static final int COLOR_NAME = 0xFFCCCCCC;
    private static final int COLOR_TIME = 0xFF999999;
    private static final int COLOR_PANEL_BG = 0xEE1E1E1E;
    private static final int COLOR_TITLE_BG = 0xFF242424;
    private static final int COLOR_BAR_BG = 0xFF242424;
    private static final int COLOR_DIVIDER = 0xFF333333;
    private static final int COLOR_INPUT_BG = 0xFF2A2A2A;
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm");

    private EditBox input;
    private CommandSuggestions commandSuggestions;
    private static int inputY;
    private final String initialText;
    private int scrollOffset;
    private int maxScroll;
    private boolean scrollToBottom = true;
    private String historyBuffer = "";
    private int historyPos = -1;
    private String worldName;
    private boolean editingTitle;
    private EditBox titleEditor;

    // Right-click menu
    private int contextMsgIndex = -1;
    private int contextX, contextY;
    private static final int CTX_W = 80;
    private static final int CTX_ITEM_H = 18;

    // Bubble hit tracking
    private final List<int[]> bubbleRects = new ArrayList<>();

    // Reply / quote
    private int replyTargetIndex = -1;

    // Copy toast
    private int copyToastTicks;

    // Animations
    private long animStart;
    private boolean closing;
    private static final int ANIM_MS = 150;

    public ChatBubbleScreen(String initialText) {
        super(Component.translatable("e33chat.screen.title"));
        this.initialText = initialText;
    }

    @Override
    protected void init() {
        historyPos = minecraft.gui.getChat().getRecentChat().size();
        ChatMessageStore.setScreenOpen(true);
        animStart = net.minecraft.Util.getMillis();
        closing = false;

        panelW = Math.max(200, (int) (width * 0.4));
        panelX = 0;
        titleY = 4;
        msgTop = titleY + TITLE_H + 1;
        barTop = height - BAR_H;
        msgBottom = barTop - 1;

        // Input box between gear (left) and send (right) icons
        int ibY = barTop + (BAR_H - 20) / 2;
        inputY = ibY;
        int inputX = panelX + PAD + ICON_S + 8;
        int inputW = panelX + panelW - PAD - ICON_S - 8 - inputX;

        input = new EditBox(font, inputX, ibY, inputW, 20, Component.literal(""));
        input.setMaxLength(256);
        input.setBordered(false);
        input.setValue(initialText);
        input.setCanLoseFocus(false);
        input.setResponder(this::onEdited);
        addRenderableWidget(input);

        commandSuggestions = new CommandSuggestions(minecraft, this, input, font,
            false, false, 0, 8, true, 0xDD1E1E1E);
        commandSuggestions.updateCommandInfo();

        if (!iconsLoaded) {
            loadIconTextures();
            iconsLoaded = true;
        }

        worldName = getWorldDisplayName();
        ChatMessageStore.setCurrentWorld(getWorldKey());

        int editW = Math.min(180, panelW - 80);
        int editX = panelX + (panelW - editW) / 2;
        int editY = titleY + (TITLE_H - 20) / 2;
        titleEditor = new EditBox(font, editX, editY, editW, 20, Component.literal(""));
        titleEditor.setMaxLength(32);
        titleEditor.setBordered(false);
        titleEditor.setVisible(false);
        addRenderableWidget(titleEditor);

        setInitialFocus(input);
    }

    private String getWorldKey() {
        if (minecraft.getSingleplayerServer() != null)
            return "sp:" + ((MinecraftServerAccessor) minecraft.getSingleplayerServer()).getStorageSource().getLevelId();
        if (minecraft.getCurrentServer() != null)
            return "mp:" + minecraft.getCurrentServer().ip;
        return "unknown";
    }

    private String getWorldDisplayName() {
        if (minecraft.getSingleplayerServer() != null)
            return minecraft.getSingleplayerServer().getWorldData().getLevelName();
        if (minecraft.getCurrentServer() != null)
            return minecraft.getCurrentServer().name;
        return Component.translatable("e33chat.title.fallback").getString();
    }

    private void onEdited(String text) {
        if (commandSuggestions != null) {
            commandSuggestions.setAllowSuggestions(!text.equals(initialText));
            commandSuggestions.updateCommandInfo();
        }
    }

    @Override
    public void tick() {
        if (editingTitle) titleEditor.tick();
        if (copyToastTicks > 0) copyToastTicks--;
        input.tick();
        if (closing && net.minecraft.Util.getMillis() - animStart >= ANIM_MS)
            minecraft.setScreen(null);
    }

    private float getAnimProgress() {
        if (!E33ChatConfig.animation) return 1.0f;
        long elapsed = net.minecraft.Util.getMillis() - animStart;
        float p = (float) elapsed / ANIM_MS;
        if (closing) p = 1.0f - p;
        return Mth.clamp(p, 0f, 1f);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (editingTitle) {
            if (keyCode == 256) { exitTitleEdit(false); return true; }
            if (keyCode == 257 || keyCode == 335) { exitTitleEdit(true); return true; }
            return titleEditor.keyPressed(keyCode, scanCode, modifiers);
        }
        if (commandSuggestions != null && commandSuggestions.keyPressed(keyCode, scanCode, modifiers))
            return true;
        if (keyCode == 256) { onClose(); return true; }
        if (keyCode == 257 || keyCode == 335) {
            if (commandSuggestions != null) commandSuggestions.hide();
            sendMessage();
            return true;
        }
        if (keyCode == 265) { moveInHistory(-1); return true; }
        if (keyCode == 264) { moveInHistory(1); return true; }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        if (commandSuggestions != null && commandSuggestions.mouseScrolled(delta))
            return true;
        scrollToBottom = false;
        scrollOffset -= (int) (delta * 20);
        scrollOffset = Mth.clamp(scrollOffset, 0, maxScroll);
        return true;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        // Context menu clicks must be handled before dismiss
        if (button == 0 && contextMsgIndex >= 0) {
            handleContextClick((int) mouseX, (int) mouseY);
            return true;
        }
        if (contextMsgIndex >= 0) { contextMsgIndex = -1; return true; }

        // Reply bar cancel button
        if (button == 0 && replyTargetIndex >= 0 && isMouseOverReplyCancel(mouseX, mouseY)) {
            replyTargetIndex = -1;
            return true;
        }

        if (commandSuggestions != null && commandSuggestions.mouseClicked((int) mouseX, (int) mouseY, button))
            return true;

        if (button == 0) {
            if (editingTitle) {
                if (!isMouseOverTitleEditor(mouseX, mouseY)) {
                    exitTitleEdit(true);
                    return true;
                }
                return super.mouseClicked(mouseX, mouseY, button);
            }
            if (isMouseOverPen(mouseX, mouseY)) {
                enterTitleEdit();
                return true;
            }
            if (mouseX >= panelX + panelW - 18 && mouseX <= panelX + panelW - 6
                && mouseY >= titleY + 6 && mouseY <= titleY + 18) {
                onClose();
                return true;
            }
            if (mouseY >= barTop) {
                if (handleIconClick((int) mouseX, (int) mouseY))
                    return true;
            }
        }

        if (button == 1) {
            for (int[] r : bubbleRects) {
                if (mouseX >= r[0] && mouseX <= r[0] + r[2]
                    && mouseY >= r[1] && mouseY <= r[1] + r[3]) {
                    contextMsgIndex = r[4];
                    contextX = (int) mouseX;
                    contextY = (int) mouseY;
                    return true;
                }
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    private boolean handleIconClick(int mx, int my) {
        int iconY = barTop + (BAR_H - ICON_S) / 2;
        // Gear icon (left)
        int gearX = panelX + PAD;
        if (mx >= gearX && mx <= gearX + ICON_S && my >= iconY && my <= iconY + ICON_S) {
            minecraft.setScreen(new ChatBubbleConfigScreen(this));
            return true;
        }
        // Send icon (right)
        int sendX = panelX + panelW - PAD - ICON_S;
        if (mx >= sendX && mx <= sendX + ICON_S && my >= iconY && my <= iconY + ICON_S) {
            sendMessage();
            return true;
        }
        return false;
    }

    private void handleContextClick(int mx, int my) {
        int menuH = CTX_ITEM_H * 2 + 2;
        int menuX = Math.min(contextX, panelX + panelW - CTX_W - 2);
        int menuY = contextY - menuH;
        if (menuY < msgTop) menuY = contextY + 4;

        if (mx >= menuX && mx <= menuX + CTX_W) {
            if (my >= menuY && my <= menuY + CTX_ITEM_H) {
                ChatMessageStore.ChatMessage msg = ChatMessageStore.getMessageAt(contextMsgIndex);
                if (msg != null) {
                    minecraft.keyboardHandler.setClipboard(msg.content().getString());
                    copyToastTicks = 20;
                }
            } else if (my >= menuY + CTX_ITEM_H + 1 && my <= menuY + CTX_ITEM_H * 2 + 1) {
                replyTargetIndex = contextMsgIndex;
            }
        }
        contextMsgIndex = -1;
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        float anim = getAnimProgress();
        int panelOffset = (int) ((anim - 1.0f) * panelW);

        // Dark overlay to the right of panel
        int overlayAlpha = (int) (0.94f * anim * 160) << 24;
        if (overlayAlpha != 0)
            g.fill(panelX + panelW + panelOffset, 0, width, height, overlayAlpha | 0x000000);

        // Panel contents slide in from left
        g.pose().pushPose();
        g.pose().translate(panelOffset, 0, 0);

        g.fill(panelX, 0, panelX + panelW, height, COLOR_PANEL_BG);

        renderTitleBar(g, mouseX, mouseY);
        renderMessages(g, mouseX, mouseY);
        renderReplyBar(g, mouseX, mouseY);
        renderContextMenu(g, mouseX, mouseY);
        renderToast(g);
        renderBottomBar(g, mouseX, mouseY);

        g.enableScissor(panelX, 0, panelX + panelW, height);
        if (commandSuggestions != null) commandSuggestions.render(g, mouseX, mouseY);
        g.disableScissor();

        g.pose().popPose();

        super.render(g, mouseX, mouseY, partialTick);
    }

    private void renderTitleBar(GuiGraphics g, int mouseX, int mouseY) {
        int ty = titleY;
        g.fill(panelX, ty, panelX + panelW, ty + TITLE_H, COLOR_TITLE_BG);
        g.fill(panelX, ty + TITLE_H, panelX + panelW, ty + TITLE_H + 1, COLOR_DIVIDER);

        if (editingTitle) {
            // titleEditor rendered via super.render() — just skip title text
        } else {
            String title = getDisplayTitle();
            int titleW = font.width(title);
            int titleX = panelX + (panelW - titleW) / 2;
            int titleTextY = ty + (TITLE_H - font.lineHeight) / 2;
            g.drawString(font, Component.literal(title), titleX, titleTextY, 0xFFFFFFFF, false);

            int penX = titleX + titleW + 3;
            int penY = ty + (TITLE_H - 9) / 2;
            boolean hoverPen = mouseX >= penX && mouseX <= penX + 9 && mouseY >= penY && mouseY <= penY + 9;
            int penColor = hoverPen ? 0xFFFFFF88 : 0xFF888888;
            g.drawString(font, Component.literal("✎"), penX, penY, penColor, false);
        }

        // Time
        String time = LocalTime.now().format(TIME_FMT);
        int timeW = font.width(time);
        g.drawString(font, Component.literal(time),
            panelX + panelW - PAD - 20 - timeW, ty + (TITLE_H - font.lineHeight) / 2, COLOR_TIME, false);

        // Close button
        int closeX = panelX + panelW - 18;
        int closeY = ty + 6;
        boolean hoverClose = mouseX >= closeX && mouseX <= closeX + 12 && mouseY >= closeY && mouseY <= closeY + 12;
        int closeBg = hoverClose ? 0xFF555555 : 0xFF333333;
        g.fill(closeX, closeY, closeX + 12, closeY + 12, closeBg);
        g.drawCenteredString(font, Component.literal("✕"), closeX + 6, closeY + 2, 0xFFCCCCCC);
    }

    private String getDisplayTitle() {
        String ct = ChatMessageStore.getCustomTitle();
        return ct != null ? ct : worldName;
    }

    private boolean isMouseOverPen(double mx, double my) {
        String title = getDisplayTitle();
        int titleW = font.width(title);
        int titleX = panelX + (panelW - titleW) / 2;
        int penX = titleX + titleW + 3;
        int penY = titleY + (TITLE_H - 9) / 2;
        return mx >= penX && mx <= penX + 9 && my >= penY && my <= penY + 9;
    }

    private boolean isMouseOverTitleEditor(double mx, double my) {
        return mx >= titleEditor.getX() && mx <= titleEditor.getX() + titleEditor.getWidth()
            && my >= titleEditor.getY() && my <= titleEditor.getY() + titleEditor.getHeight();
    }

    private void enterTitleEdit() {
        editingTitle = true;
        titleEditor.setVisible(true);
        titleEditor.setValue(getDisplayTitle());
        titleEditor.setCursorPosition(titleEditor.getValue().length());
        titleEditor.setFocused(true);
        input.setCanLoseFocus(true);
        setFocused(titleEditor);
    }

    private void exitTitleEdit(boolean save) {
        if (save) {
            ChatMessageStore.setCustomTitle(titleEditor.getValue().trim());
        }
        editingTitle = false;
        titleEditor.setVisible(false);
        input.setCanLoseFocus(false);
        setFocused(input);
    }

    private void renderMessages(GuiGraphics g, int mouseX, int mouseY) {
        bubbleRects.clear();
        List<ChatMessageStore.ChatMessage> messages = ChatMessageStore.getMessages();
        if (messages.isEmpty()) return;

        // Count time separators for total height
        int timeSeps = 0;
        String lastKey = null;
        for (var msg : messages) {
            if (!msg.isSystem()) {
                String key = msg.time().format(TIME_FMT);
                if (lastKey == null || !key.equals(lastKey)) { timeSeps++; lastKey = key; }
            }
        }

        int areaH = msgBottom - msgTop;
        int totalH = 0;
        for (var msg : messages) totalH += getMsgHeight(msg) + GAP;
        totalH += timeSeps * (TIME_SEP_H + GAP);
        maxScroll = Math.max(0, totalH - areaH);

        boolean wasAtBottom = scrollOffset >= maxScroll - 2;
        if (scrollToBottom || wasAtBottom) {
            scrollOffset = maxScroll;
            scrollToBottom = false;
        }
        scrollOffset = Mth.clamp(scrollOffset, 0, maxScroll);

        g.enableScissor(panelX, msgTop, panelX + panelW, msgBottom);

        int contentY = 0;
        lastKey = null;
        for (int i = 0; i < messages.size(); i++) {
            var msg = messages.get(i);

            // Time separator
            if (!msg.isSystem()) {
                String key = msg.time().format(TIME_FMT);
                if (lastKey == null || !key.equals(lastKey)) {
                    lastKey = key;
                    int ssy = msgTop + contentY - scrollOffset;
                    if (ssy + TIME_SEP_H > msgTop && ssy < msgBottom)
                        renderTimeSeparator(g, msg.time(), ssy);
                    contentY += TIME_SEP_H + GAP;
                }
            }

            int h = getMsgHeight(msg);
            int screenY = msgTop + contentY - scrollOffset;
            contentY += h + GAP;

            if (screenY + h <= msgTop || screenY >= msgBottom) continue;
            renderBubble(g, msg, i, screenY, mouseX, mouseY);
        }
        g.disableScissor();
    }

    private void renderTimeSeparator(GuiGraphics g, LocalTime time, int y) {
        String text = time.format(TIME_FMT);
        int tw = font.width(text);
        int tx = panelX + (panelW - tw) / 2;
        g.fill(tx - 6, y + 2, tx + tw + 6, y + TIME_SEP_H - 2, 0x44000000);
        g.drawString(font, Component.literal(text), tx, y + 3, 0xFF999999, false);
    }

    private int getMsgHeight(ChatMessageStore.ChatMessage msg) {
        if (msg.isSystem()) {
            List<FormattedCharSequence> lines = font.split(msg.content(), panelW - PAD * 2 - 20);
            return lines.size() * font.lineHeight + 4;
        }
        int bubbleMaxW = panelW - AVATAR - PAD * 2 - BUBBLE_PAD_X * 2 - 16;
        List<FormattedCharSequence> lines = font.split(msg.content(), bubbleMaxW);
        int h = lines.size() * font.lineHeight + BUBBLE_PAD_Y * 2 + NAME_H;
        if (msg.replyContent() != null) h += font.lineHeight + 2;
        return h;
    }

    private void renderBubble(GuiGraphics g, ChatMessageStore.ChatMessage msg,
                               int index, int baseY, int mouseX, int mouseY) {
        if (msg.isSystem()) {
            List<FormattedCharSequence> lines = font.split(msg.content(), panelW - PAD * 2 - 20);
            int yy = baseY + 2;
            for (var line : lines) {
                int lw = font.width(line);
                g.drawString(font, line, panelX + (panelW - lw) / 2, yy, 0xFF888888, false);
                yy += font.lineHeight;
            }
            return;
        }

        boolean own = msg.isOwn();
        int bubbleMaxW = panelW - AVATAR - PAD * 2 - BUBBLE_PAD_X * 2 - 16;
        List<FormattedCharSequence> lines = font.split(msg.content(), bubbleMaxW);

        int textW = 0;
        for (var line : lines) textW = Math.max(textW, font.width(line));
        int bubbleW = Math.max(textW + BUBBLE_PAD_X * 2, 36);
        int bubbleH = lines.size() * font.lineHeight + BUBBLE_PAD_Y * 2;

        int avatarX, bubbleX;
        if (own) {
            avatarX = panelX + panelW - PAD - AVATAR;
            bubbleX = avatarX - 4 - bubbleW;
        } else {
            avatarX = panelX + PAD;
            bubbleX = avatarX + AVATAR + 4;
        }

        int nameY = baseY;

        // Reply preview
        if (msg.replyContent() != null) {
            int replyH = font.lineHeight;
            int replyBarX = own ? bubbleX : bubbleX;
            int replyMaxW = bubbleW - 10;
            String replyText = msg.replySender() + ": " + msg.replyContent();
            String replyDisplay = font.plainSubstrByWidth(replyText, replyMaxW - font.width("..."));
            if (!replyDisplay.equals(replyText)) replyDisplay += "...";
            int accentColor = own
                ? E33ChatConfig.parseHexColor(E33ChatConfig.ownTextColor, 0xFF0A0A0A)
                : E33ChatConfig.parseHexColor(E33ChatConfig.otherTextColor, 0xFFFFFFFF);
            g.fill(replyBarX, nameY, replyBarX + 2, nameY + replyH, accentColor);
            g.drawString(font, Component.literal(replyDisplay), replyBarX + 6, nameY + 1, 0xFF999999, false);
            nameY += replyH + 2;
        }

        if (!msg.senderName().getString().isEmpty()) {
            int maxNameW = panelW - AVATAR - PAD * 2 - 20;
            Component displayName = msg.senderName();
            if (font.width(displayName) > maxNameW)
                displayName = Component.literal(font.plainSubstrByWidth(displayName.getString(), maxNameW - font.width("...")) + "...");
            int nameW = font.width(displayName);
            int startX = own ? (bubbleX + bubbleW - nameW) : bubbleX;
            g.drawString(font, displayName, startX, nameY, COLOR_NAME, false);
        }

        int bubbleY = baseY + NAME_H;
        int avatarY = bubbleY - 6;

        int bg = own
            ? E33ChatConfig.parseHexColor(E33ChatConfig.ownBubbleColor, 0xFF95EC69)
            : E33ChatConfig.parseHexColor(E33ChatConfig.otherBubbleColor, 0xFF4A4A4A);
        int fg = own
            ? E33ChatConfig.parseHexColor(E33ChatConfig.ownTextColor, 0xFF0A0A0A)
            : E33ChatConfig.parseHexColor(E33ChatConfig.otherTextColor, 0xFFFFFFFF);

        g.fill(bubbleX, bubbleY, bubbleX + bubbleW, bubbleY + bubbleH, bg);

        for (int li = 0; li < lines.size(); li++)
            g.drawString(font, lines.get(li), bubbleX + BUBBLE_PAD_X,
                bubbleY + BUBBLE_PAD_Y + li * font.lineHeight, fg, false);

        ResourceLocation skin = getSkin(msg.senderUUID());
        PlayerFaceRenderer.draw(g, skin, avatarX, avatarY, AVATAR);

        bubbleRects.add(new int[]{bubbleX, bubbleY, bubbleW, bubbleH, index});
    }

    private void renderContextMenu(GuiGraphics g, int mouseX, int mouseY) {
        if (contextMsgIndex < 0) return;
        int menuH = CTX_ITEM_H * 2 + 2;
        int menuX = Math.min(contextX, panelX + panelW - CTX_W - 2);
        int menuY = contextY - menuH;
        if (menuY < msgTop) menuY = contextY + 4;

        g.fill(menuX, menuY, menuX + CTX_W, menuY + menuH, 0xEE2A2A2A);
        g.fill(menuX, menuY, menuX + CTX_W, menuY + 1, COLOR_DIVIDER);
        g.fill(menuX, menuY + menuH - 1, menuX + CTX_W, menuY + menuH, COLOR_DIVIDER);
        g.fill(menuX, menuY, menuX + 1, menuY + menuH, COLOR_DIVIDER);
        g.fill(menuX + CTX_W - 1, menuY, menuX + CTX_W, menuY + menuH, COLOR_DIVIDER);

        boolean hoverCopy = mouseX >= menuX && mouseX <= menuX + CTX_W
            && mouseY >= menuY && mouseY <= menuY + CTX_ITEM_H;
        int copyBg = hoverCopy ? 0xFF4A4A4A : 0xFF3A3A3A;
        g.fill(menuX + 1, menuY + 1, menuX + CTX_W - 1, menuY + CTX_ITEM_H, copyBg);
        g.drawString(font, Component.translatable("e33chat.context.copy"), menuX + 8, menuY + 4, 0xFFFFFFFF, false);

        g.fill(menuX + 4, menuY + CTX_ITEM_H, menuX + CTX_W - 4, menuY + CTX_ITEM_H + 1, 0xFF555555);

        boolean hoverQuote = mouseX >= menuX && mouseX <= menuX + CTX_W
            && mouseY >= menuY + CTX_ITEM_H + 1 && mouseY <= menuY + menuH;
        int quoteBg = hoverQuote ? 0xFF4A4A4A : 0xFF3A3A3A;
        g.fill(menuX + 1, menuY + CTX_ITEM_H + 1, menuX + CTX_W - 1, menuY + menuH - 1, quoteBg);
        g.drawString(font, Component.translatable("e33chat.context.quote"), menuX + 8, menuY + CTX_ITEM_H + 5, 0xFFFFFFFF, false);
    }

    private static final int REPLY_BAR_H = 18;

    private void renderReplyBar(GuiGraphics g, int mouseX, int mouseY) {
        if (replyTargetIndex < 0) return;
        ChatMessageStore.ChatMessage target = ChatMessageStore.getMessageAt(replyTargetIndex);
        if (target == null) { replyTargetIndex = -1; return; }

        int gearX = panelX + PAD;
        int sendX = panelX + panelW - PAD - ICON_S;
        int barX = gearX + ICON_S + 8;
        int barW = sendX - 6 - barX;
        int barY = barTop - REPLY_BAR_H;

        g.fill(barX, barY, barX + barW, barTop, 0xEE1E1E1E);
        g.fill(barX, barTop - 1, barX + barW, barTop, COLOR_DIVIDER);

        String sender = target.senderName().getString();
        if (sender.isEmpty()) sender = Component.translatable("e33chat.sender.system").getString();
        String preview = sender + ": " + target.content().getString();
        int maxW = barW - 24;
        String display = font.plainSubstrByWidth(preview, maxW - font.width("..."));
        if (!display.equals(preview)) display += "...";
        g.drawString(font, Component.literal(display), barX + 6, barY + 4, 0xFFAAAAAA, false);

        int cx = barX + barW - 16;
        int cy = barY + 3;
        boolean hoverX = mouseX >= cx && mouseX <= cx + 12 && mouseY >= cy && mouseY <= cy + 12;
        int xBg = hoverX ? 0xFF555555 : 0xFF3A3A3A;
        g.fill(cx, cy, cx + 12, cy + 12, xBg);
        g.drawCenteredString(font, Component.literal("✕"), cx + 6, cy + 2, 0xFFCCCCCC);
    }

    private boolean isMouseOverReplyCancel(double mx, double my) {
        if (replyTargetIndex < 0) return false;
        int gearX = panelX + PAD;
        int sendX = panelX + panelW - PAD - ICON_S;
        int barX = gearX + ICON_S + 8;
        int barW = sendX - 6 - barX;
        int barY = barTop - REPLY_BAR_H;
        int cx = barX + barW - 16;
        int cy = barY + 3;
        return mx >= cx && mx <= cx + 12 && my >= cy && my <= cy + 12;
    }

    private void renderToast(GuiGraphics g) {
        if (copyToastTicks <= 0) return;
        int alpha = copyToastTicks > 5 ? 0xFF : (copyToastTicks * 255 / 5) << 24;
        int color = alpha | 0xFFFFFF;
        String text = Component.translatable("e33chat.toast.copied").getString();
        int tw = font.width(text);
        int tx = panelX + (panelW - tw) / 2;
        int ty = msgBottom - 24;
        g.fill(tx - 6, ty - 2, tx + tw + 6, ty + font.lineHeight + 2, 0xCC000000);
        g.drawString(font, Component.literal(text), tx, ty, color, false);
    }

    private void renderBottomBar(GuiGraphics g, int mouseX, int mouseY) {
        g.fill(panelX, barTop, panelX + panelW, height, COLOR_BAR_BG);
        g.fill(panelX, barTop, panelX + panelW, barTop + 1, COLOR_DIVIDER);

        int iconY = barTop + (BAR_H - ICON_S) / 2;

        // Input background + divider above it (between suggestions and input)
        int gearX = panelX + PAD;
        int sendX = panelX + panelW - PAD - ICON_S;
        int ibX = gearX + ICON_S + 6;
        int ibY = barTop + (BAR_H - 20) / 2;
        int ibW = sendX - 6 - ibX;
        int ibH = 20;
        g.fill(ibX, ibY - 1, ibX + ibW, ibY, COLOR_DIVIDER);
        g.fill(ibX, ibY, ibX + ibW, ibY + ibH, COLOR_INPUT_BG);

        // Gear icon (left)
        boolean hoverGear = mouseX >= gearX && mouseX <= gearX + ICON_S
            && mouseY >= iconY && mouseY <= iconY + ICON_S;
        if (hoverGear) g.fill(gearX - 1, iconY - 1, gearX + ICON_S + 1, iconY + ICON_S + 1, 0xFF444444);
        drawTextureIcon(g, TEX_GEAR, gearX, iconY, ICON_S);

        // Send icon (right)
        boolean hoverSend = mouseX >= sendX && mouseX <= sendX + ICON_S
            && mouseY >= iconY && mouseY <= iconY + ICON_S;
        if (hoverSend) g.fill(sendX - 1, iconY - 1, sendX + ICON_S + 1, iconY + ICON_S + 1, 0xFF444444);
        drawTextureIcon(g, TEX_SEND, sendX, iconY, ICON_S);
    }

    private void loadIconTextures() {
        loadIconTexture(TEX_GEAR, "assets/e33chat/textures/gui/settings.png");
        loadIconTexture(TEX_SEND, "assets/e33chat/textures/gui/send.png");
    }

    private void loadIconTexture(ResourceLocation loc, String classpath) {
        try (InputStream in = getClass().getClassLoader().getResourceAsStream(classpath)) {
            if (in != null) {
                NativeImage img = NativeImage.read(in);
                DynamicTexture tex = new DynamicTexture(img);
                minecraft.getTextureManager().register(loc, tex);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void drawTextureIcon(GuiGraphics g, ResourceLocation tex, int x, int y, int size) {
        var abstractTex = minecraft.getTextureManager().getTexture(tex);
        RenderSystem.setShaderTexture(0, abstractTex.getId());
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.enableBlend();
        g.blit(tex, x, y, 0, 0, size, size, size, size);
    }

    private static final UUID NIL_UUID = new UUID(0, 0);

    private ResourceLocation getSkin(UUID uuid) {
        if (uuid == null || uuid.equals(NIL_UUID))
            return DefaultPlayerSkin.getDefaultSkin(NIL_UUID);
        if (minecraft.getConnection() == null)
            return DefaultPlayerSkin.getDefaultSkin(NIL_UUID);
        PlayerInfo info = minecraft.getConnection().getPlayerInfo(uuid);
        return info != null ? info.getSkinLocation() : DefaultPlayerSkin.getDefaultSkin(uuid);
    }

    private void sendMessage() {
        String text = input.getValue().trim();
        if (text.isEmpty()) return;

        if (replyTargetIndex >= 0) {
            ChatMessageStore.ChatMessage target = ChatMessageStore.getMessageAt(replyTargetIndex);
            if (target != null)
                ChatMessageStore.setPendingReply(target.content().getString(), target.senderName().getString());
            replyTargetIndex = -1;
        }

        if (text.startsWith("/"))
            minecraft.player.connection.sendCommand(text.substring(1));
        else
            minecraft.player.connection.sendChat(text);
        minecraft.gui.getChat().addRecentChat(text);

        // 直接添加到 store，不依赖服务器回显（多模组环境回显可能被拦截）
        ChatMessageStore.addMessage(Component.literal(text),
            minecraft.player.getUUID(),
            Component.literal(minecraft.player.getName().getString()),
            false);

        input.setValue("");
        scrollToBottom = true;
    }

    private void moveInHistory(int delta) {
        int size = minecraft.gui.getChat().getRecentChat().size();
        int newPos = Mth.clamp(historyPos + delta, 0, size);
        if (newPos != historyPos) {
            if (newPos == size) {
                historyPos = size;
                input.setValue(historyBuffer);
            } else {
                if (historyPos == size) historyBuffer = input.getValue();
                input.setValue(minecraft.gui.getChat().getRecentChat().get(newPos));
                historyPos = newPos;
            }
        }
    }

    @Override
    public void removed() {
        ChatMessageStore.setScreenOpen(false);
        minecraft.gui.getChat().resetChatScroll();
    }

    @Override
    public void onClose() {
        if (!E33ChatConfig.animation) {
            minecraft.setScreen(null);
            return;
        }
        if (closing) return;
        closing = true;
        animStart = net.minecraft.Util.getMillis();
    }

    public static int getInputY() { return inputY; }

    @Override
    public boolean isPauseScreen() { return false; }
}
