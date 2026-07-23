package com.niuqu.chatbubble;

import java.util.ArrayList;
import java.util.List;
import net.minecraftforge.common.ForgeConfigSpec;

public class ChatBubbleConfig {
    public static final ForgeConfigSpec CLIENT_CONFIG;

    public static final ForgeConfigSpec.EnumValue<ChatBubbleTheme> THEME;
    public static final ForgeConfigSpec.BooleanValue ENABLED;
    public static final ForgeConfigSpec.BooleanValue RED_DOT_ENABLED;
    public static final ForgeConfigSpec.BooleanValue HIDE_CHAT_ICON;
    public static final ForgeConfigSpec.BooleanValue ANIMATION_ENABLED;
    public static final ForgeConfigSpec.BooleanValue STRONG_HINT_ENABLED;
    public static final ForgeConfigSpec.BooleanValue MENTION_STRONG_HINT_ENABLED;
    public static final ForgeConfigSpec.BooleanValue SYSTEM_CHAT_AS_BUBBLE;
    public static final ForgeConfigSpec.BooleanValue ANTI_SPAM;
    public static final ForgeConfigSpec.BooleanValue CHAT_HISTORY_ENABLED;
    public static final ForgeConfigSpec.BooleanValue PREVIEW_ENABLED;
    public static final ForgeConfigSpec.IntValue PREVIEW_LINES;
    public static final ForgeConfigSpec.IntValue PREVIEW_WIDTH;
    public static final ForgeConfigSpec.IntValue TIME_SEPARATOR_MINUTES;
    public static final ForgeConfigSpec.ConfigValue<String> OWN_BUBBLE_COLOR;
    public static final ForgeConfigSpec.ConfigValue<String> OTHER_BUBBLE_COLOR;
    public static final ForgeConfigSpec.IntValue BUBBLE_CORNER_RADIUS;
    public static final ForgeConfigSpec.ConfigValue<String> OWN_TEXT_COLOR;
    public static final ForgeConfigSpec.ConfigValue<String> OTHER_TEXT_COLOR;
    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> QUICK_CHAT_PHRASES;
    public static final ForgeConfigSpec.IntValue PANEL_WIDTH;
    public static final ForgeConfigSpec.BooleanValue DEBUG_LOG;
    public static final ForgeConfigSpec.BooleanValue SOUND_SYSTEM;
    public static final ForgeConfigSpec.BooleanValue SOUND_MENTION;
    public static final ForgeConfigSpec.BooleanValue SOUND_WHISPER;
    public static final ForgeConfigSpec.BooleanValue SOUND_PUBLIC;
    public static final ForgeConfigSpec.BooleanValue PRESERVE_INPUT;
    public static final ForgeConfigSpec.BooleanValue COLOR_CODES;
    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();

        builder.comment("E33Chat client settings");
        builder.push("general");

        THEME = builder
            .comment("Color theme: DARK (default) or LIGHT")
            .translation("e33chat.config.theme")
            .defineEnum("theme", ChatBubbleTheme.DARK);

        ENABLED = builder
            .comment("Enable the custom chat overlay (disable to restore vanilla chat)")
            .translation("e33chat.config.enabled")
            .define("enabled", true);

        RED_DOT_ENABLED = builder
            .comment("Show an unread red dot on the HUD chat icon")
            .translation("e33chat.config.red_dot")
            .define("red_dot", true);

        HIDE_CHAT_ICON = builder
            .comment("Hide the HUD chat icon (including the red dot)")
            .translation("e33chat.config.hide_chat_icon")
            .define("hide_chat_icon", false);

        ANIMATION_ENABLED = builder
            .comment("Chat screen open/close animation")
            .translation("e33chat.config.animation")
            .define("animation", true);

        DEBUG_LOG = builder
            .comment("Verbose message-pipeline logging for troubleshooting (writes chat text to latest.log)")
            .translation("e33chat.config.debug_log")
            .define("debug_log", false);

        PANEL_WIDTH = builder
            .comment("Chat panel width in physical screen pixels (800-1600, independent of GUI scale and aspect ratio)")
            .translation("e33chat.config.panel_width")
            .defineInRange("panel_width", 1000, 800, 1600);

        STRONG_HINT_ENABLED = builder
            .comment("Show system messages as a strong hint above the hotbar (otherwise they go to the message preview)")
            .translation("e33chat.config.strong_hint")
            .define("strong_hint", true);

        MENTION_STRONG_HINT_ENABLED = builder
            .comment("Show a strong hint above the hotbar when you are @mentioned or quoted")
            .translation("e33chat.config.mention_strong_hint")
            .define("mention_strong_hint", true);

        SYSTEM_CHAT_AS_BUBBLE = builder
            .comment("Render system messages as chat bubbles")
            .translation("e33chat.config.system_chat_as_bubble")
            .define("system_chat_as_bubble", false);

        ANTI_SPAM = builder
            .comment("Collapse consecutive identical messages into one bubble with a counter")
            .translation("e33chat.config.anti_spam")
            .define("anti_spam", false);

        CHAT_HISTORY_ENABLED = builder
            .comment("Keep per-world chat history (restored when you rejoin)")
            .translation("e33chat.config.chat_history")
            .define("chat_history", false);

        PREVIEW_ENABLED = builder
            .comment("Show a recent-message preview above the HUD icon")
            .translation("e33chat.config.preview_enabled")
            .define("preview_enabled", true);

        PREVIEW_LINES = builder
            .comment("Preview line count (1-8)")
            .translation("e33chat.config.preview_lines")
            .defineInRange("preview_lines", 3, 1, 8);

        PREVIEW_WIDTH = builder
            .comment("Preview width in pixels (50-400)")
            .translation("e33chat.config.preview_width")
            .defineInRange("preview_width", 200, 50, 400);

        TIME_SEPARATOR_MINUTES = builder
            .comment("Minutes between time separators in the chat list (0 = off, 1-60)")
            .translation("e33chat.config.time_separator")
            .defineInRange("time_separator_minutes", 5, 0, 60);

        PRESERVE_INPUT = builder
            .comment("Keep typed text in the input box when the chat closes, restoring it on reopen")
            .translation("e33chat.config.preserve_input")
            .define("preserve_input", true);

        COLOR_CODES = builder
            .comment("Interpret & color/format codes as color in YOUR OWN outgoing bubble (local only). The raw & is sent unchanged (never §), so it never kicks; color plugins color it for everyone, plain servers show literal & to others. Off by default so normal text like 'B&B' isn't colored locally")
            .translation("e33chat.config.color_codes")
            .define("color_codes", false);

        builder.pop();
        builder.push("bubble");

        OWN_BUBBLE_COLOR = builder
            .comment("Your bubble color (hex RRGGBB)")
            .translation("e33chat.config.own_bubble_color")
            .define("own_color", "#1E90FF");

        OTHER_BUBBLE_COLOR = builder
            .comment("Other players' bubble color (hex RRGGBB)")
            .translation("e33chat.config.other_bubble_color")
            .define("other_color", "#4A4A4A");

        BUBBLE_CORNER_RADIUS = builder
            .comment("Bubble corner radius (0 = square, max 10)")
            .translation("e33chat.config.bubble_corner_radius")
            .defineInRange("corner_radius", 4, 0, 10);

        builder.pop();
        builder.push("text");

        OWN_TEXT_COLOR = builder
            .comment("Your text color (hex RRGGBB)")
            .translation("e33chat.config.own_text_color")
            .define("own_color", "#FFFFFF");

        OTHER_TEXT_COLOR = builder
            .comment("Other players' text color (hex RRGGBB)")
            .translation("e33chat.config.other_text_color")
            .define("other_color", "#FFFFFF");

        builder.pop();
        builder.push("quick_chat");

        QUICK_CHAT_PHRASES = builder
            .comment("Quick chat phrase list")
            .translation("e33chat.config.quick_chat_phrases")
            .defineListAllowEmpty("phrases", ArrayList::new, o -> o instanceof String);

        builder.pop();
        builder.push("sound");

        SOUND_SYSTEM = builder
            .comment("Play a notification sound for system messages")
            .translation("e33chat.config.sound_system")
            .define("sound_system", false);

        SOUND_MENTION = builder
            .comment("Play a notification sound when you are @mentioned or quoted")
            .translation("e33chat.config.sound_mention")
            .define("sound_mention", true);

        SOUND_WHISPER = builder
            .comment("Play a notification sound for private / whisper messages")
            .translation("e33chat.config.sound_whisper")
            .define("sound_whisper", true);

        SOUND_PUBLIC = builder
            .comment("Play a notification sound for public chat messages")
            .translation("e33chat.config.sound_public")
            .define("sound_public", false);

        builder.pop();

        CLIENT_CONFIG = builder.build();
    }

    public static int parseHexColor(String hex, int defaultColor) {
        try {
            String h = hex.replace("#", "").trim();
            if (h.length() != 6) return defaultColor;
            return 0xFF000000 | Integer.parseInt(h, 16);
        } catch (NumberFormatException e) {
            return defaultColor;
        }
    }
}
