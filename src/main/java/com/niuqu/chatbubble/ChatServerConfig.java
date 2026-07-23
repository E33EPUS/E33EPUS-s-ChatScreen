package com.niuqu.chatbubble;

import net.neoforged.neoforge.common.ModConfigSpec;

public class ChatServerConfig {
    public static final ModConfigSpec SERVER_CONFIG;
    public static final ModConfigSpec.BooleanValue HISTORY_ENABLED;
    public static final ModConfigSpec.BooleanValue USE_TPA;

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();
        builder.comment("E33Chat server settings");
        HISTORY_ENABLED = builder
            .comment("Send recent chat history to players when they join")
            .define("history_enabled", false);
        USE_TPA = builder
            .comment("Make the player-head menu teleport with /tpa (request) instead of /tp")
            .define("use_tpa", false);
        SERVER_CONFIG = builder.build();
    }
}
