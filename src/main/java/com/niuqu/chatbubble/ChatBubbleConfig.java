package com.niuqu.chatbubble;

import net.minecraftforge.common.ForgeConfigSpec;

public class ChatBubbleConfig {
    public static final ForgeConfigSpec CLIENT_CONFIG;

    public static final ForgeConfigSpec.BooleanValue ENABLED;
    public static final ForgeConfigSpec.BooleanValue RED_DOT_ENABLED;
    public static final ForgeConfigSpec.BooleanValue HIDE_CHAT_ICON;
    public static final ForgeConfigSpec.BooleanValue ANIMATION_ENABLED;
    public static final ForgeConfigSpec.BooleanValue STRONG_HINT_ENABLED;
    public static final ForgeConfigSpec.BooleanValue MENTION_STRONG_HINT_ENABLED;
    public static final ForgeConfigSpec.BooleanValue SYSTEM_CHAT_AS_BUBBLE;
    public static final ForgeConfigSpec.BooleanValue ANTI_SPAM;
    public static final ForgeConfigSpec.BooleanValue CHAT_REPORT_COMPAT;
    public static final ForgeConfigSpec.BooleanValue PREVIEW_ENABLED;
    public static final ForgeConfigSpec.IntValue PREVIEW_LINES;
    public static final ForgeConfigSpec.IntValue PREVIEW_WIDTH;
    public static final ForgeConfigSpec.ConfigValue<String> OWN_BUBBLE_COLOR;
    public static final ForgeConfigSpec.ConfigValue<String> OTHER_BUBBLE_COLOR;
    public static final ForgeConfigSpec.ConfigValue<String> OWN_TEXT_COLOR;
    public static final ForgeConfigSpec.ConfigValue<String> OTHER_TEXT_COLOR;
    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();

        builder.comment("ChatBubble 聊天界面设置");
        builder.push("general");

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
            .comment("系统消息也渲染为聊天气泡（兼容禁用聊天举报的插件服）")
            .define("system_chat_as_bubble", false);

        ANTI_SPAM = builder
            .comment("防刷屏——连续相同消息合并为一条，旁白显示重复次数")
            .define("anti_spam", false);

        CHAT_REPORT_COMPAT = builder
            .comment("禁用聊天举报兼容模式——从系统消息中扫描在线玩家名并还原身份（支持服务器前缀/称号）")
            .define("chat_report_compat", false);

        PREVIEW_ENABLED = builder
            .comment("在 HUD 图标上方显示最近消息预览")
            .define("preview_enabled", true);

        PREVIEW_LINES = builder
            .comment("消息预览行数（1-3）")
            .defineInRange("preview_lines", 2, 1, 3);

        PREVIEW_WIDTH = builder
            .comment("消息预览宽度（像素，50-400）")
            .defineInRange("preview_width", 150, 50, 400);

        builder.pop();
        builder.push("bubble");

        OWN_BUBBLE_COLOR = builder
            .comment("自己的气泡颜色 (十六进制 RRGGBB)")
            .define("own_color", "#95EC69");

        OTHER_BUBBLE_COLOR = builder
            .comment("别人的气泡颜色 (十六进制 RRGGBB)")
            .define("other_color", "#4A4A4A");

        builder.pop();
        builder.push("text");

        OWN_TEXT_COLOR = builder
            .comment("自己的文字颜色 (十六进制 RRGGBB)")
            .define("own_color", "#0A0A0A");

        OTHER_TEXT_COLOR = builder
            .comment("别人的文字颜色 (十六进制 RRGGBB)")
            .define("other_color", "#FFFFFF");

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
