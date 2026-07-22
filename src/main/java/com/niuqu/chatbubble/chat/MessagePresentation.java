package com.niuqu.chatbubble.chat;

import java.util.Collection;
import java.util.Comparator;
import java.util.Optional;

public final class MessagePresentation {
    private MessagePresentation() {}

    public record PlayerLine(String playerName, String displayLabel, String content) {}

    public static Optional<PlayerLine> parseDecoratedPlayerLine(
        String text, Collection<String> onlineNames
    ) {
        if (text == null || onlineNames == null) return Optional.empty();
        return onlineNames.stream()
            .filter(n -> n != null && !n.isBlank())
            .sorted(Comparator.comparingInt(String::length).reversed())
            .flatMap(name -> parseForName(text, name).stream())
            .findFirst();
    }

    private static Optional<PlayerLine> parseForName(String text, String name) {
        return parseGeneric(text, name);
    }

    static Optional<PlayerLine> parseGeneric(String text, String name) {
        if (text == null || name == null || name.length() < 3) return Optional.empty();
        int idx = text.indexOf(name);
        if (idx < 0 || idx >= 30) return Optional.empty();

        if (idx > 0) {
            char prev = text.charAt(idx - 1);
            if (Character.isLetterOrDigit(prev) || prev == '_') {
                int openAngle = text.lastIndexOf('<', idx);
                int closeAngle = text.indexOf('>', idx + name.length());
                if (openAngle >= 0 && closeAngle >= 0 && closeAngle - openAngle <= 64) {
                    // inside angle brackets like <[VIP]Steve>
                } else {
                    int bracketClose = text.lastIndexOf(']', idx);
                    if (bracketClose >= 0) {
                        int bracketOpen = text.lastIndexOf('[', bracketClose);
                        if (bracketOpen < 0 || idx - bracketClose > 2) return Optional.empty();
                    } else {
                        return Optional.empty();
                    }
                }
            }
        }

        int after = idx + name.length();
        if (after < text.length()) {
            char next = text.charAt(after);
            if (Character.isLetterOrDigit(next) || next == '_') return Optional.empty();
        }

        int sep = after;
        while (sep < text.length()) {
            char ch = text.charAt(sep);
            if (Character.isWhitespace(ch) || ch == '>' || ch == ':'
                || ch == '：' || ch == '»' || ch == '-' || ch == '|') sep++;
            else break;
        }
        if (sep <= after || sep >= text.length()) return Optional.empty();

        String displayLabel = text.substring(0, idx + name.length());
        return Optional.of(new PlayerLine(name, displayLabel, text.substring(sep).strip()));
    }
}
