package com.niuqu.chatbubble;

import net.neoforged.neoforge.common.ModConfigSpec;

public class ChatServerConfig {
    public static final ModConfigSpec SERVER_CONFIG;
    public static final ModConfigSpec.BooleanValue HISTORY_ENABLED;

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();
        builder.comment("E33Chat server settings");
        HISTORY_ENABLED = builder
            .comment("Send recent chat history to players when they join")
            .define("history_enabled", true);
        SERVER_CONFIG = builder.build();
    }
}
