package com.niuqu.chatbubble;

import net.minecraftforge.common.ForgeConfigSpec;

public class ChatServerConfig {
    public static final ForgeConfigSpec SERVER_CONFIG;
    public static final ForgeConfigSpec.BooleanValue HISTORY_ENABLED;

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
        builder.comment("E33Chat server settings");
        HISTORY_ENABLED = builder
            .comment("Send recent chat history to players when they join")
            .define("history_enabled", true);
        SERVER_CONFIG = builder.build();
    }
}
