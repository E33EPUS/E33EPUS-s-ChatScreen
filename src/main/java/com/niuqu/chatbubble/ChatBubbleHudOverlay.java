package com.niuqu.chatbubble;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;

public class ChatBubbleHudOverlay {

    private static final int ICON_S = 16;
    private static final int RED_DOT_R = 4;
    public static final ResourceLocation TEX_CHAT_ICON =
        ResourceLocation.fromNamespaceAndPath("e33chat", "textures/gui/chat_icon");
    public static boolean iconLoaded;

    public static void render(GuiGraphics g) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.options == null) return;
        if (mc.screen != null) return;

        String keyName = mc.options.keyChat.getTranslatedKeyMessage().getString();
        int screenH = mc.getWindow().getGuiScaledHeight();
        int x = 3;
        int iconY = screenH - ICON_S - 20;
        int textY = iconY + ICON_S + 1;

        g.pose().pushPose();
        g.pose().translate(0, 0, 300);

        // Strong hint above hotbar
        if (ChatBubbleConfig.STRONG_HINT_ENABLED.get()) {
            String hint = ChatMessageStore.getStrongHintText();
            if (hint != null) {
                int ticks = ChatMessageStore.getStrongHintTicks();
                int screenW = mc.getWindow().getGuiScaledWidth();
                int hintW = mc.font.width(hint);
                int hintX = (screenW - hintW) / 2;
                int hintY = screenH - 22 - 30 - mc.font.lineHeight;
                int alpha;
                if (ticks > 50)
                    alpha = (ChatMessageStore.STRONG_HINT_DURATION - ticks) * 0xFF / 10;
                else if (ticks > 10)
                    alpha = 0xFF;
                else
                    alpha = ticks * 0xFF / 10;
                alpha = Math.min(alpha, 0xFF);
                int bgAlpha = alpha / 2;
                int bgColor = (bgAlpha << 24) | 0x000000;
                int baseColor = ChatMessageStore.isStrongHintMention() ? 0xFFFF55 : 0xFFFFFF;
                int textColor = (alpha << 24) | baseColor;
                g.fill(hintX - 6, hintY - 3, hintX + hintW + 6, hintY + mc.font.lineHeight + 3, bgColor);
                g.drawString(mc.font, hint, hintX, hintY, textColor, false);
            }
        }

        // Message preview above icon (multi-line)
        if (ChatBubbleConfig.PREVIEW_ENABLED.get()) {
            List<ChatMessageStore.PreviewEntry> previews = ChatMessageStore.getPreviews();
            if (previews != null && !previews.isEmpty()) {
                int maxW = ChatBubbleConfig.PREVIEW_WIDTH.get();
                int lineH = mc.font.lineHeight;
                int gap = 2;

                List<String> displays = new ArrayList<>();
                int maxTextW = 0;
                for (var e : previews) {
                    String d = mc.font.plainSubstrByWidth(e.text, maxW - 4);
                    if (!d.equals(e.text)) d += "...";
                    displays.add(d);
                    maxTextW = Math.max(maxTextW, mc.font.width(d));
                }

                int px = x + ICON_S / 2 - maxTextW / 2;
                if (px < 2) px = 2;
                int bgX1 = px - 3;
                if (bgX1 < 0) bgX1 = 0;

                int bottomLineY = iconY - 5 - lineH;
                int topLineY = bottomLineY - (displays.size() - 1) * (lineH + gap);
                int newestTicks = previews.get(previews.size() - 1).ticks;
                int newestAlpha = newestTicks > 10 ? 0xDD : (newestTicks * 0xDD / 10);
                int bgAlpha = newestAlpha / 2;
                int bgColor = (bgAlpha << 24) | 0x000000;
                g.fill(bgX1, topLineY - 2, px + maxTextW + 3, bottomLineY + lineH + 2, bgColor);
                for (int i = displays.size() - 1; i >= 0; i--) {
                    int lineY = bottomLineY - (displays.size() - 1 - i) * (lineH + gap);
                    g.drawString(mc.font, displays.get(i), px, lineY, 0xFFFFFFFF, false);
                }
            }
        }

        // Chat bubble icon (hidden if hide_chat_icon enabled)
        if (!ChatBubbleConfig.HIDE_CHAT_ICON.get()) {
            if (!iconLoaded) {
                loadIconTexture();
                iconLoaded = true;
            }
            drawIcon(g, x, iconY);

            // Red dot
            if (ChatBubbleConfig.RED_DOT_ENABLED.get() && ChatMessageStore.getUnreadCount() > 0) {
                int dotX = x + ICON_S - RED_DOT_R;
                int dotY = iconY + RED_DOT_R;
                int dotColor = ChatMessageStore.hasUnreadMention(mc.player.getName().getString())
                    ? 0xFFFF4444 : 0xFFFF0000;
                g.fill(dotX - RED_DOT_R, dotY - RED_DOT_R, dotX + RED_DOT_R, dotY + RED_DOT_R, dotColor);
            }

            // Keybind text below icon
            String keyDisplay = "[" + keyName + "]";
            int keyW = mc.font.width(keyDisplay);
            int keyX = keyW > ICON_S ? x : x + (ICON_S - keyW) / 2;
            g.drawString(mc.font, keyDisplay, keyX, textY, 0xFFFFFFFF, false);
        }

        g.pose().popPose();
    }

    public static boolean isMouseOverIcon(double mx, double my) {
        if (ChatBubbleConfig.HIDE_CHAT_ICON.get()) return false;
        Minecraft mc = Minecraft.getInstance();
        if (mc.screen != null) return false;
        int screenH = mc.getWindow().getGuiScaledHeight();
        int iconY = screenH - ICON_S - 20;
        return mx >= 3 && mx <= 3 + ICON_S && my >= iconY && my <= iconY + ICON_S + mc.font.lineHeight + 2;
    }

    private static void loadIconTexture() {
        try (java.io.InputStream in = ChatBubbleHudOverlay.class.getClassLoader()
                .getResourceAsStream("assets/e33chat/textures/gui/chat_icon.png")) {
            if (in != null) {
                com.mojang.blaze3d.platform.NativeImage img = com.mojang.blaze3d.platform.NativeImage.read(in);
                net.minecraft.client.renderer.texture.DynamicTexture tex =
                    new net.minecraft.client.renderer.texture.DynamicTexture(img);
                Minecraft.getInstance().getTextureManager().register(TEX_CHAT_ICON, tex);
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    private static void drawIcon(GuiGraphics g, int x, int y) {
        var mc = Minecraft.getInstance();
        AbstractTexture abstractTex;
        try {
            abstractTex = mc.getTextureManager().getTexture(TEX_CHAT_ICON);
        } catch (Exception e) {
            // Texture lost (F3+T resource reload), reload it
            loadIconTexture();
            abstractTex = mc.getTextureManager().getTexture(TEX_CHAT_ICON);
        }
        RenderSystem.setShaderTexture(0, abstractTex.getId());
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.enableBlend();
        g.blit(TEX_CHAT_ICON, x, y, 0, 0, ICON_S, ICON_S, ICON_S, ICON_S);
    }
}
