package com.niuqu.chatbubble;

import net.minecraftforge.common.ForgeConfigSpec;

public class ChatBubbleConfig {
    public static final ForgeConfigSpec CLIENT_CONFIG;

    public static final ForgeConfigSpec.BooleanValue ENABLED;
    public static final ForgeConfigSpec.BooleanValue RED_DOT_ENABLED;
    public static final ForgeConfigSpec.BooleanValue ANIMATION_ENABLED;
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

        ANIMATION_ENABLED = builder
            .comment("聊天框打开/关闭动画")
            .define("animation", true);

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
