package com.niuqu.chatbubble;

import org.junit.jupiter.api.Test;
import java.time.LocalTime;
import static org.junit.jupiter.api.Assertions.*;

class ChatUiBehaviorTest {

    @Test
    void disabledReturnsEmpty() {
        assertEquals("", ChatUiBehavior.timeKey(LocalTime.of(10, 30), 0));
    }

    @Test
    void oneMinuteReturnsHHMM() {
        assertEquals("10:30", ChatUiBehavior.timeKey(LocalTime.of(10, 30), 1));
    }

    @Test
    void fiveMinuteBuckets() {
        assertEquals("10:30", ChatUiBehavior.timeKey(LocalTime.of(10, 32), 5));
        assertEquals("10:30", ChatUiBehavior.timeKey(LocalTime.of(10, 30), 5));
        assertEquals("10:35", ChatUiBehavior.timeKey(LocalTime.of(10, 37), 5));
    }

    @Test
    void fifteenMinuteBuckets() {
        assertEquals("10:00", ChatUiBehavior.timeKey(LocalTime.of(10, 14), 15));
        assertEquals("10:15", ChatUiBehavior.timeKey(LocalTime.of(10, 15), 15));
        assertEquals("10:45", ChatUiBehavior.timeKey(LocalTime.of(10, 59), 15));
    }

    @Test
    void midnightBoundary() {
        assertEquals("00:00", ChatUiBehavior.timeKey(LocalTime.of(0, 0), 5));
        assertEquals("23:55", ChatUiBehavior.timeKey(LocalTime.of(23, 59), 5));
    }
}
