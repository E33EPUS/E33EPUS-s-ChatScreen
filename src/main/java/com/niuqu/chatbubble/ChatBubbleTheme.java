package com.niuqu.chatbubble;

public enum ChatBubbleTheme {
    DARK,
    LIGHT;

    public record Colors(
        int panelBg, int titleBg, int barBg,
        int sidebarBg, int sidebarItemHover, int sidebarItemSelected, int sidebarDivider,
        int divider, int inputBg,
        int textPrimary, int textSecondary, int textMuted,
        int nameColor, int timeColor,
        int popupBg, int popupHover, int popupText,
        int contextBg, int contextHover, int contextText,
        int iconHover,
        int notificationText, int whisperBar,
        int toastBg, int toastText,
        int scrollbar, int scrollbarHover,
        int closeBg, int closeHoverBg, int closeText,
        int systemText, int quoteBar, int duplicateLabel,
        int redDot, int redDotMention,
        int strongHintNormal, int strongHintMention,
        int previewText,
        int configTitle, int configSection, int configLabel, int configBg
    ) {}

    public Colors colors() {
        return switch (this) {
            case DARK -> new Colors(
                0xEE1E1E1E, 0xFF242424, 0xFF242424,
                0xFF1A1A1A, 0xFF2A2A2A, 0xFF3A3A3A, 0xFF333333,
                0xFF333333, 0xFF2A2A2A,
                0xFFFFFFFF, 0xFFAAAAAA, 0xFF888888,
                0xFFCCCCCC, 0xFF999999,
                0xB31E1E1E, 0xB3444444, 0xFFFFFFFF,
                0xEE2A2A2A, 0xFF4A4A4A, 0xFFFFFFFF,
                0xFF444444,
                0xFFFFFF55, 0xAA7B2D8B,
                0xCC000000, 0xFFFFFFFF,
                0x00FFFFFF, 0x00FFFFFF,
                0xFF333333, 0xFF555555, 0xFFCCCCCC,
                0xFF888888, 0xFFFFFFFF, 0xFFFFAA00,
                0xFFFF0000, 0xFFFF4444,
                0xFFFFFFFF, 0xFFFFFF55,
                0xFFFFFFFF,
                0xFFFFFFFF, 0xFFFFAA00, 0xFFFFFFFF, 0xC0101010
            );
            case LIGHT -> new Colors(
                0xEEF0F0F0, 0xFFE0E0E0, 0xFFE8E8E8,
                0xFFE8E8E8, 0xFFD8D8D8, 0xFFCCDDFF, 0xFFCCCCCC,
                0xFFCCCCCC, 0xFFFFFFFF,
                0xFF1A1A1A, 0xFF666666, 0xFF999999,
                0xFF0066CC, 0xFF888888,
                0xDDEEEEEE, 0xDDCCCCCC, 0xFF1A1A1A,
                0xFFF0F0F0, 0xFFBBDDFF, 0xFF1A1A1A,
                0xFFCCCCCC,
                0xFF2266CC, 0xAA9966CC,
                0xCCFFFFFF, 0xFF1A1A1A,
                0x00333333, 0x00666666,
                0xFFCCCCCC, 0xFFAAAAAA, 0xFF666666,
                0xFF888888, 0xFF3366CC, 0xFFCC6600,
                0xFFCC0000, 0xFFFF2222,
                0xFF1A1A1A, 0xFFCC8800,
                0xFF1A1A1A,
                0xFF0066CC, 0xFF0066CC, 0xFF333333, 0xC0F0F0F0
            );
        };
    }

    public static int argb(int a, int r, int g, int b) {
        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    public static int alphaBlend(int color, int alpha) {
        return (alpha << 24) | (color & 0x00FFFFFF);
    }
}
