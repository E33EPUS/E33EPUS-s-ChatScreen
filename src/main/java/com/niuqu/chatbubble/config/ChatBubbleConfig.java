package com.niuqu.chatbubble.config;

import java.util.List;

public record ChatBubbleConfig(
    boolean enabled,
    String theme,
    boolean redDotEnabled,
    boolean hideChatIcon,
    boolean animationEnabled,
    boolean strongHintEnabled,
    boolean mentionStrongHintEnabled,
    boolean systemChatAsBubble,
    boolean antiSpam,
    boolean chatReportCompat,
    boolean chatHistoryEnabled,
    boolean previewEnabled,
    int previewLines,
    int previewWidth,
    int timeSeparatorMinutes,
    int panelWidth,
    int bubbleCornerRadius,
    String ownBubbleColor,
    String otherBubbleColor,
    String ownTextColor,
    String otherTextColor,
    boolean soundPublic,
    boolean soundSystem,
    boolean soundMention,
    boolean soundWhisper,
    boolean debugLog,
    List<String> quickChatPhrases
) {
    public static ChatBubbleConfig defaults() {
        return new ChatBubbleConfig(
            true, "dark", true, false, true,
            true, true, false, false, false,
            false, true, 3, 200, 5, 1000, 4,
            "#1E90FF", "#4A4A4A", "#FFFFFF", "#FFFFFF",
            false, false, true, true, false,
            List.of()
        );
    }

    public static int parseHexColor(String hex, int defaultColor) {
        if (hex == null) return defaultColor;
        try {
            String h = hex.replace("#", "").trim();
            if (h.length() != 6) return defaultColor;
            return 0xFF000000 | Integer.parseInt(h, 16);
        } catch (NumberFormatException e) {
            return defaultColor;
        }
    }

    public ChatBubbleConfig withTheme(String theme) {
        return new ChatBubbleConfig(enabled, theme, redDotEnabled, hideChatIcon, animationEnabled,
            strongHintEnabled, mentionStrongHintEnabled, systemChatAsBubble, antiSpam, chatReportCompat,
            chatHistoryEnabled, previewEnabled, previewLines, previewWidth, timeSeparatorMinutes,
            panelWidth, bubbleCornerRadius, ownBubbleColor, otherBubbleColor, ownTextColor, otherTextColor,
            soundPublic, soundSystem, soundMention, soundWhisper, debugLog, quickChatPhrases);
    }

    public ChatBubbleConfig withQuickChatPhrases(List<String> phrases) {
        return new ChatBubbleConfig(enabled, theme, redDotEnabled, hideChatIcon, animationEnabled,
            strongHintEnabled, mentionStrongHintEnabled, systemChatAsBubble, antiSpam, chatReportCompat,
            chatHistoryEnabled, previewEnabled, previewLines, previewWidth, timeSeparatorMinutes,
            panelWidth, bubbleCornerRadius, ownBubbleColor, otherBubbleColor, ownTextColor, otherTextColor,
            soundPublic, soundSystem, soundMention, soundWhisper, debugLog, phrases);
    }
}
