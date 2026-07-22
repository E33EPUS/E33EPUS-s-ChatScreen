package com.niuqu.chatbubble;

import com.niuqu.chatbubble.chat.ChatMessage;
import org.junit.jupiter.api.Test;

import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class ChatListLayoutTest {

    private static ChatMessage msg(String content, LocalTime time, boolean system) {
        return new ChatMessage(UUID.randomUUID(),
            net.minecraft.text.Text.literal("Test"), net.minecraft.text.Text.literal(content),
            time, false, system, null, null, String.valueOf(content.hashCode()), 1,
            "Test", false, null);
    }

    @Test
    void emptyListReturnsZeroHeight() {
        var result = ChatListLayout.layout(List.of(), 5, 14, 2, m -> 20);
        assertEquals(0, result.totalHeight());
        assertTrue(result.entries().isEmpty());
    }

    @Test
    void singleMessageNoSeparator() {
        var messages = List.of(msg("hello", LocalTime.of(10, 30), false));
        var result = ChatListLayout.layout(messages, 5, 14, 2, m -> 20);
        assertEquals(1, result.entries().size());
        assertEquals(22, result.totalHeight()); // 20 + 2 gap
        assertEquals(0, result.entries().get(0).y());
        assertFalse(result.entries().get(0).separator().isEmpty()); // first message always gets separator
    }

    @Test
    void separatorInsertedOnTimeChange() {
        var messages = List.of(
            msg("a", LocalTime.of(10, 0), false),
            msg("b", LocalTime.of(10, 1), false),  // same 5-min bucket
            msg("c", LocalTime.of(10, 5), false)   // new bucket
        );
        var result = ChatListLayout.layout(messages, 5, 14, 2, m -> 20);
        assertEquals(3, result.entries().size());
        assertFalse(result.entries().get(0).separator().isEmpty()); // first
        assertTrue(result.entries().get(1).separator().isEmpty());  // same bucket
        assertFalse(result.entries().get(2).separator().isEmpty()); // new bucket
        // total: 3*(20+2) + 2*(14+2) = 66 + 32 = 98
        assertEquals(98, result.totalHeight());
    }

    @Test
    void systemMessagesSkipSeparator() {
        var messages = List.of(
            msg("join", LocalTime.of(10, 0), true),
            msg("hi", LocalTime.of(10, 1), false)
        );
        var result = ChatListLayout.layout(messages, 5, 14, 2, m -> 20);
        assertTrue(result.entries().get(0).separator().isEmpty()); // system: no sep
        assertFalse(result.entries().get(1).separator().isEmpty()); // first non-system
    }

    @Test
    void scrollForIndexFindsCorrectY() {
        var messages = List.of(
            msg("a", LocalTime.of(10, 0), false),
            msg("b", LocalTime.of(10, 0), false),
            msg("c", LocalTime.of(10, 0), false)
        );
        var result = ChatListLayout.layout(messages, 5, 14, 2, m -> 20);
        assertEquals(result.entries().get(1).y(), result.scrollForIndex(1));
        assertEquals(result.totalHeight(), result.scrollForIndex(99)); // not found
    }

    @Test
    void disabledSeparatorProducesNoSeparators() {
        var messages = List.of(
            msg("a", LocalTime.of(10, 0), false),
            msg("b", LocalTime.of(11, 0), false)
        );
        var result = ChatListLayout.layout(messages, 0, 14, 2, m -> 20);
        assertTrue(result.entries().get(0).separator().isEmpty());
        assertTrue(result.entries().get(1).separator().isEmpty());
        assertEquals(44, result.totalHeight()); // 2*(20+2), no separators
    }
}
