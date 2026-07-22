package com.niuqu.chatbubble;

import com.niuqu.chatbubble.chat.ChatMessage;

import java.util.ArrayList;
import java.util.List;
import java.util.function.ToIntFunction;

public final class ChatListLayout {

    public record Entry(ChatMessage message, int sourceIndex, int y, int messageHeight, String separator) {}

    public record Result(List<Entry> entries, int totalHeight) {
        public int scrollForIndex(int sourceIndex) {
            for (Entry e : entries)
                if (e.sourceIndex == sourceIndex) return e.y;
            return totalHeight;
        }
    }

    private ChatListLayout() {}

    public static Result layout(List<ChatMessage> messages, int separatorMinutes,
                                 int separatorHeight, int gap,
                                 ToIntFunction<ChatMessage> heights) {
        List<Entry> entries = new ArrayList<>();
        int y = 0;
        String prevKey = null;
        for (int i = 0; i < messages.size(); i++) {
            ChatMessage m = messages.get(i);
            String sep = "";
            if (!m.isSystem()) {
                String key = ChatUiBehavior.timeKey(m.time(), separatorMinutes);
                if (prevKey == null || !key.equals(prevKey)) {
                    sep = key;
                    prevKey = key;
                }
            }
            int start = y;
            if (!sep.isEmpty()) y += separatorHeight + gap;
            int h = heights.applyAsInt(m);
            entries.add(new Entry(m, i, start, h, sep));
            y += h + gap;
        }
        return new Result(List.copyOf(entries), y);
    }
}
