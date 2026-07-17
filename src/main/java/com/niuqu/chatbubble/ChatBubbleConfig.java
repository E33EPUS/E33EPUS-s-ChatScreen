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
    public static final ForgeConfigSpec.BooleanValue CHAT_REPORT_COMPAT;
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
    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();

        builder.comment("ChatBubble 聊天界面设置");
        builder.push("general");

        THEME = builder
            .comment("颜色主题：DARK = 深色（默认），LIGHT = 浅色")
            .defineEnum("theme", ChatBubbleTheme.DARK);

        ENABLED = builder
            .comment("启用自定义聊天浮层（关闭后恢复原版聊天）")
            .define("enabled", true);

        RED_DOT_ENABLED = builder
            .comment("HUD 图标上显示未读红点")
            .define("red_dot", true);

        HIDE_CHAT_ICON = builder
            .comment("隐藏聊天HUD图标（红点等一并隐藏）")
            .define("hide_chat_icon", false);

        ANIMATION_ENABLED = builder
            .comment("聊天框打开/关闭动画")
            .define("animation", true);

        STRONG_HINT_ENABLED = builder
            .comment("系统消息在物品栏上方显示强提示（不启用则系统消息进入消息预览）")
            .define("strong_hint", true);

        MENTION_STRONG_HINT_ENABLED = builder
            .comment("被别人 @ 或引用时在物品栏上方显示强提示")
            .define("mention_strong_hint", true);

        SYSTEM_CHAT_AS_BUBBLE = builder
            .comment("系统消息以气泡形式显示在聊天框中")
            .define("system_chat_as_bubble", false);

        ANTI_SPAM = builder
            .comment("防刷屏")
            .define("anti_spam", true);

        CHAT_REPORT_COMPAT = builder
            .comment("禁用聊天举报后兼容 <玩家名> 消息格式")
            .define("chat_report_compat", false);

        CHAT_HISTORY_ENABLED = builder
            .comment("保留每个存档的聊天记录（退出后恢复）")
            .define("chat_history", false);

        PREVIEW_ENABLED = builder
            .comment("在 HUD 图标上方显示最近消息预览")
            .define("preview_enabled", true);

        PREVIEW_LINES = builder
            .comment("消息预览行数（1-8）")
            .defineInRange("preview_lines", 3, 1, 8);

        PREVIEW_WIDTH = builder
            .comment("消息预览宽度（像素，50-400）")
            .defineInRange("preview_width", 200, 50, 400);

        TIME_SEPARATOR_MINUTES = builder
            .comment("时间分隔符间隔（分钟，0=关闭，1-60）。默认5分钟")
            .defineInRange("time_separator_minutes", 5, 0, 60);

        builder.pop();
        builder.push("bubble");

        OWN_BUBBLE_COLOR = builder
            .comment("自己的气泡颜色 (十六进制 RRGGBB)")
            .define("own_color", "#1E90FF");

        OTHER_BUBBLE_COLOR = builder
            .comment("别人的气泡颜色 (十六进制 RRGGBB)")
            .define("other_color", "#4A4A4A");

        BUBBLE_CORNER_RADIUS = builder
            .comment("气泡圆角半径（0=直角，0-10）")
            .defineInRange("corner_radius", 4, 0, 10);

        builder.pop();
        builder.push("text");

        OWN_TEXT_COLOR = builder
            .comment("自己的文字颜色 (十六进制 RRGGBB)")
            .define("own_color", "#FFFFFF");

        OTHER_TEXT_COLOR = builder
            .comment("别人的文字颜色 (十六进制 RRGGBB)")
            .define("other_color", "#FFFFFF");

        builder.pop();
        builder.push("quick_chat");

        QUICK_CHAT_PHRASES = builder
            .comment("常用语列表")
            .defineListAllowEmpty("phrases", ArrayList::new, o -> o instanceof String);

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
