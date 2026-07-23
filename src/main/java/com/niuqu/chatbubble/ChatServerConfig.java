package com.niuqu.chatbubble;

import net.minecraftforge.common.ForgeConfigSpec;

public class ChatServerConfig {
    public static final ForgeConfigSpec SERVER_CONFIG;
    public static final ForgeConfigSpec.BooleanValue HISTORY_ENABLED;
    public static final ForgeConfigSpec.BooleanValue USE_TPA;

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
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
