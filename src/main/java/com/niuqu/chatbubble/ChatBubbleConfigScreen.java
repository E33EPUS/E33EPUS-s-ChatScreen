package com.niuqu.chatbubble;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.neoforged.neoforge.common.ModConfigSpec;

public class ChatBubbleConfigScreen extends Screen {
    private final Screen lastScreen;
    private static final int LABEL_X = 40;
    private static final int INPUT_X = 165;
    private static final int INPUT_W = 80;
    private static final int PREVIEW_X = 255;
    private static final int ROW_H = 28;
    private static final int START_Y = 38;

    private int scrollOffset;
    private final List<AbstractWidget> scrollWidgets = new ArrayList<>();

    public ChatBubbleConfigScreen(Screen lastScreen) {
        super(Component.translatable("e33chat.config.title"));
        this.lastScreen = lastScreen;
    }

    @Override
    protected void init() {
        scrollWidgets.clear();
        scrollOffset = Mth.clamp(scrollOffset, 0, calcMaxScroll());
        int y = START_Y - scrollOffset;

        scrollWidgets.add(addRenderableWidget(
            mkBoolButton(y, ChatBubbleConfig.ENABLED))); y += ROW_H;
        scrollWidgets.add(addRenderableWidget(
            mkBoolButton(y, ChatBubbleConfig.RED_DOT_ENABLED))); y += ROW_H;
        scrollWidgets.add(addRenderableWidget(
            mkBoolButton(y, ChatBubbleConfig.HIDE_CHAT_ICON))); y += ROW_H;
        scrollWidgets.add(addRenderableWidget(
            mkBoolButton(y, ChatBubbleConfig.ANIMATION_ENABLED))); y += ROW_H;
        scrollWidgets.add(addRenderableWidget(
            mkBoolButton(y, ChatBubbleConfig.STRONG_HINT_ENABLED))); y += ROW_H;
        scrollWidgets.add(addRenderableWidget(
            mkBoolButton(y, ChatBubbleConfig.MENTION_STRONG_HINT_ENABLED))); y += ROW_H;
        scrollWidgets.add(addRenderableWidget(
            mkBoolButton(y, ChatBubbleConfig.ANTI_SPAM))); y += ROW_H;
        scrollWidgets.add(addRenderableWidget(
            mkBoolButton(y, ChatBubbleConfig.PREVIEW_ENABLED))); y += ROW_H;

        scrollWidgets.add(addRenderableWidget(
            mkCycleButton(y))); y += ROW_H;

        EditBox widthBox = mkIntBox(y, String.valueOf(ChatBubbleConfig.PREVIEW_WIDTH.get()), 50, 400, ChatBubbleConfig.PREVIEW_WIDTH::set);
        scrollWidgets.add(addRenderableWidget(widthBox)); y += ROW_H;

        EditBox ownBubbleBox = mkHexBox(y, ChatBubbleConfig.OWN_BUBBLE_COLOR.get(), ChatBubbleConfig.OWN_BUBBLE_COLOR::set);
        scrollWidgets.add(addRenderableWidget(ownBubbleBox)); y += ROW_H;

        EditBox otherBubbleBox = mkHexBox(y, ChatBubbleConfig.OTHER_BUBBLE_COLOR.get(), ChatBubbleConfig.OTHER_BUBBLE_COLOR::set);
        scrollWidgets.add(addRenderableWidget(otherBubbleBox)); y += ROW_H;

        EditBox ownTextBox = mkHexBox(y, ChatBubbleConfig.OWN_TEXT_COLOR.get(), ChatBubbleConfig.OWN_TEXT_COLOR::set);
        scrollWidgets.add(addRenderableWidget(ownTextBox)); y += ROW_H;

        EditBox otherTextBox = mkHexBox(y, ChatBubbleConfig.OTHER_TEXT_COLOR.get(), ChatBubbleConfig.OTHER_TEXT_COLOR::set);
        scrollWidgets.add(addRenderableWidget(otherTextBox)); y += ROW_H;

        y += 12;

        scrollWidgets.add(addRenderableWidget(
            mkBoolButton(y, ChatBubbleConfig.SYSTEM_CHAT_AS_BUBBLE))); y += ROW_H;
        scrollWidgets.add(addRenderableWidget(
            mkBoolButton(y, ChatBubbleConfig.CHAT_REPORT_COMPAT))); y += ROW_H;

        addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, btn -> onClose())
            .bounds(width / 2 - 100, height - 32, 200, 20).build());
    }

    private Button mkBoolButton(int y, ModConfigSpec.BooleanValue cfg) {
        boolean v = cfg.get();
        return Button.builder(
            v ? CommonComponents.OPTION_ON : CommonComponents.OPTION_OFF,
            btn -> {
                boolean nv = !cfg.get();
                cfg.set(nv);
                btn.setMessage(nv ? CommonComponents.OPTION_ON : CommonComponents.OPTION_OFF);
            }
        ).bounds(INPUT_X, y, INPUT_W, 20).build();
    }

    private Button mkCycleButton(int y) {
        return Button.builder(
            Component.literal(String.valueOf(ChatBubbleConfig.PREVIEW_LINES.get())),
            btn -> {
                int v = ChatBubbleConfig.PREVIEW_LINES.get() + 1;
                if (v > 3) v = 1;
                ChatBubbleConfig.PREVIEW_LINES.set(v);
                btn.setMessage(Component.literal(String.valueOf(v)));
            }
        ).bounds(INPUT_X, y, INPUT_W, 20).build();
    }

    private EditBox mkHexBox(int y, String initial, java.util.function.Consumer<String> onChange) {
        EditBox box = new EditBox(font, INPUT_X, y, INPUT_W, 20, Component.literal(""));
        box.setValue(initial);
        box.setMaxLength(7);
        box.setResponder(s -> {
            if (!s.matches("#?[0-9a-fA-F]{0,6}")) return;
            if (s.length() == 6 && !s.startsWith("#")) {
                box.setValue("#" + s);
                onChange.accept("#" + s);
            } else if (s.length() == 7) {
                onChange.accept(s);
            }
        });
        return box;
    }

    private EditBox mkIntBox(int y, String initial, int min, int max, java.util.function.Consumer<Integer> onChange) {
        EditBox box = new EditBox(font, INPUT_X, y, INPUT_W, 20, Component.literal(""));
        box.setValue(initial);
        box.setMaxLength(3);
        box.setResponder(s -> {
            if (!s.matches("\\d*")) return;
            try {
                int v = Integer.parseInt(s);
                if (v >= min && v <= max) onChange.accept(v);
            } catch (NumberFormatException ignored) {}
        });
        return box;
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        renderBackground(g, mouseX, mouseY, partialTick);
        super.render(g, mouseX, mouseY, partialTick);

        g.drawCenteredString(font, title, width / 2, 14, 0xFFFFFFFF);

        int y = START_Y + 6 - scrollOffset;
        int generalHeaderY = START_Y - scrollOffset - 20;
        Component generalHeader = Component.translatable("e33chat.config.section.general");
        if (generalHeaderY > -ROW_H && generalHeaderY < height)
            g.drawString(font, generalHeader, LABEL_X, generalHeaderY, 0xFFFFAA00, false);

        String[] labels = {"e33chat.config.enabled", "e33chat.config.red_dot", "e33chat.config.hide_chat_icon", "e33chat.config.animation",
            "e33chat.config.strong_hint", "e33chat.config.mention_strong_hint",
            "e33chat.config.anti_spam",
            "e33chat.config.preview_enabled", "e33chat.config.preview_lines", "e33chat.config.preview_width",
            "e33chat.config.own_bubble_color", "e33chat.config.other_bubble_color", "e33chat.config.own_text_color", "e33chat.config.other_text_color",
            "e33chat.config.system_chat_as_bubble",
            "e33chat.config.chat_report_compat",
        };
        for (String label : labels) {
            if (y > -ROW_H && y < height)
                g.drawString(font, Component.translatable(label), LABEL_X, y, 0xFFFFFFFF, false);
            y += ROW_H;
            if (label.equals("e33chat.config.other_text_color")) y += 12;
        }

        int compatHeaderY = START_Y + 14 * ROW_H + 12 - scrollOffset - 20;
        Component compatHeader = Component.translatable("e33chat.config.section.compatibility");
        if (compatHeaderY > -ROW_H && compatHeaderY < height)
            g.drawString(font, compatHeader, LABEL_X, compatHeaderY, 0xFFFFAA00, false);

        int py = START_Y + ROW_H * 10 + 4 - scrollOffset;
        drawPreview(g, py, ChatBubbleConfig.OWN_BUBBLE_COLOR.get()); py += ROW_H;
        drawPreview(g, py, ChatBubbleConfig.OTHER_BUBBLE_COLOR.get()); py += ROW_H;
        drawPreview(g, py, ChatBubbleConfig.OWN_TEXT_COLOR.get()); py += ROW_H;
        drawPreview(g, py, ChatBubbleConfig.OTHER_TEXT_COLOR.get());
    }

    private void drawPreview(GuiGraphics g, int y, String hex) {
        int color = ChatBubbleConfig.parseHexColor(hex, 0xFF000000);
        g.fill(PREVIEW_X, y, PREVIEW_X + 14, y + 14, 0xFF444444);
        g.fill(PREVIEW_X + 1, y + 1, PREVIEW_X + 13, y + 13, color);
    }

    @Override
    public void onClose() {
        if (minecraft != null) minecraft.setScreen(lastScreen);
    }

    @Override
    public void renderBackground(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        g.fill(0, 0, width, height, 0xC0101010);
    }

    private int calcMaxScroll() {
        int contentBottom = START_Y + 16 * ROW_H + 12 + 10;
        return Math.max(0, contentBottom - (height - 42));
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        int maxScroll = calcMaxScroll();
        if (maxScroll <= 0) return false;
        scrollOffset -= (int) (scrollY * 20);
        scrollOffset = Mth.clamp(scrollOffset, 0, maxScroll);
        for (int i = 0; i < scrollWidgets.size(); i++)
            scrollWidgets.get(i).setY(START_Y + i * ROW_H - scrollOffset + (i >= 14 ? 12 : 0));
        return true;
    }
}
