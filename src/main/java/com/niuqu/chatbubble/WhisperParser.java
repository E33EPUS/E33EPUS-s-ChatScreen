package com.niuqu.chatbubble;

import java.util.List;
import java.util.Optional;

public final class WhisperParser {

    public record Result(String partner, String content, boolean outgoing) {}

    private static final String[] KEYWORDS = {
        "悄悄", "whisper", "whispers", "对你说", "to you",
        "私聊", "密语", "密聊"
    };

    private WhisperParser() {}

    public static Optional<Result> parse(String text, List<String> playerNames, String selfName) {
        if (text == null || text.isEmpty()) return Optional.empty();
        if (!containsKeyword(text)) return Optional.empty();

        boolean outgoing = false;
        String lower = text.toLowerCase();
        if (lower.startsWith("you ") || lower.startsWith("to ") || text.startsWith("你"))
            outgoing = true;

        for (String name : playerNames) {
            if (name.isEmpty()) continue;
            int idx = text.indexOf(name);
            if (idx < 0 || idx > 30) continue;
            String content = extractContent(text, name, idx);
            if (content.isEmpty()) continue;
            return Optional.of(new Result(name, content, outgoing));
        }
        return Optional.empty();
    }

    private static boolean containsKeyword(String text) {
        String lower = text.toLowerCase();
        for (String kw : KEYWORDS)
            if (lower.contains(kw.toLowerCase())) return true;
        return false;
    }

    static String extractContent(String text, String name, int nameIdx) {
        String after = text.substring(nameIdx + name.length());
        for (String sep : new String[]{": ", "：", " :", " ："}) {
            int i = after.lastIndexOf(sep);
            if (i >= 0) return after.substring(i + sep.length()).trim();
        }
        return after.trim();
    }
}
