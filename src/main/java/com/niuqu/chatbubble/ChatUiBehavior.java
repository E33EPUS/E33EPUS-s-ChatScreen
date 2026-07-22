package com.niuqu.chatbubble;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public final class ChatUiBehavior {
    private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("HH:mm");
    private ChatUiBehavior() {}

    public static String timeKey(LocalTime time, int interval) {
        if (interval <= 0) return "";
        if (interval == 1) return time.format(TIME);
        return String.format("%02d:%02d", time.getHour(), time.getMinute() / interval * interval);
    }
}
