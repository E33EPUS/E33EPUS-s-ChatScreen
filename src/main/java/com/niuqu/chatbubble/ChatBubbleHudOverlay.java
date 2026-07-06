package com.niuqu.chatbubble;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;

public class ChatBubbleHudOverlay {

    private static final int ICON_S = 16;
    private static final int RED_DOT_R = 4;
    public static final ResourceLocation TEX_CHAT_ICON =
        ResourceLocation.fromNamespaceAndPath("e33chat", "textures/gui/chat_icon");
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

        // Message preview above icon
        String preview = ChatMessageStore.getLatestPreview();
        if (preview != null) {
            int maxPreviewW = mc.font.width("玩家名: 消息内容...") + 10;
            String display = mc.font.plainSubstrByWidth(preview, maxPreviewW - 4);
            if (!display.equals(preview)) display += "...";
            int pw = mc.font.width(display);
            int px = x + ICON_S / 2 - pw / 2;
            int py = iconY - mc.font.lineHeight - 5;
            int alpha = ChatMessageStore.getPreviewTicks() > 10 ? 0xDD : (ChatMessageStore.getPreviewTicks() * 0xDD / 10);
            int bgColor = (alpha << 24) | 0x000000;
            g.fill(px - 3, py - 2, px + pw + 3, py + mc.font.lineHeight + 2, bgColor);
            g.drawString(mc.font, display, px, py, 0xFFFFFFFF, false);
        }

        // Chat bubble icon (reloads on F3+T etc.)
        try {
            mc.getTextureManager().getTexture(TEX_CHAT_ICON);
        } catch (Exception e) {
            loadIconTexture();
        }
        drawIcon(g, x, iconY);

        // Red dot
        if (ChatBubbleConfig.RED_DOT_ENABLED.get() && ChatMessageStore.getUnreadCount() > 0) {
            int dotX = x + ICON_S - RED_DOT_R;
            int dotY = iconY - RED_DOT_R / 2;
            int dotColor = ChatMessageStore.hasUnreadMention(mc.player.getName().getString())
                ? 0xFFFF4444 : 0xFFFF0000;
            g.fill(dotX - RED_DOT_R, dotY - RED_DOT_R, dotX + RED_DOT_R, dotY + RED_DOT_R, dotColor);
        }

        // Keybind text below icon
        String keyDisplay = "[" + keyName + "]";
        int keyW = mc.font.width(keyDisplay);
        int keyX = keyW > ICON_S ? x : x + (ICON_S - keyW) / 2;
        g.drawString(mc.font, keyDisplay, keyX, textY, 0xFFFFFFFF, false);

        g.pose().popPose();
    }

    public static boolean isMouseOverIcon(double mx, double my) {
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
        var abstractTex = mc.getTextureManager().getTexture(TEX_CHAT_ICON);
        RenderSystem.setShaderTexture(0, abstractTex.getId());
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.enableBlend();
        g.blit(TEX_CHAT_ICON, x, y, 0, 0, ICON_S, ICON_S, ICON_S, ICON_S);
    }
}
