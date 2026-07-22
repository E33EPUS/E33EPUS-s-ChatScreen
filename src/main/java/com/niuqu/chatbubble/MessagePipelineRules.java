package com.niuqu.chatbubble;

import java.util.Collection;
import java.util.Comparator;

public final class MessagePipelineRules {
    public enum Route { UNKNOWN, SYSTEM, WHISPER_INCOMING, WHISPER_OUTGOING, PLAYER }

    private MessagePipelineRules() {}

    public static Route classify(String key) {
        if ("commands.message.display.incoming".equals(key)) return Route.WHISPER_INCOMING;
        if ("commands.message.display.outgoing".equals(key)) return Route.WHISPER_OUTGOING;
        if ("chat.type.text".equals(key)) return Route.PLAYER;
        return isVanillaBroadcast(key) ? Route.SYSTEM : Route.UNKNOWN;
    }

    public static boolean isVanillaBroadcast(String key) {
        return key.startsWith("chat.type.advancement.") || key.startsWith("death.")
            || key.startsWith("multiplayer.player.")
            || (key.startsWith("commands.") && !key.startsWith("commands.message.display."))
            || key.equals("chat.type.admin") || key.equals("chat.type.announcement")
            || key.equals("chat.type.emote") || key.startsWith("chat.type.team.");
    }

    public static boolean isXaeroWaypoint(String content) {
        return content.startsWith("xaero-waypoint:")
            || content.startsWith("xaero_waypoint:")
            || content.startsWith("xaero_waypoint_add:");
    }

    public static String resolveOnlineName(String displayName, Collection<String> onlineNames) {
        if (displayName == null || onlineNames == null) return "";
        return onlineNames.stream().filter(n -> n != null && !n.isBlank())
            .filter(displayName::equals).findFirst()
            .orElseGet(() -> onlineNames.stream().filter(n -> n != null && n.length() >= 3)
                .filter(displayName::contains).max(Comparator.comparingInt(String::length)).orElse(""));
    }
}
