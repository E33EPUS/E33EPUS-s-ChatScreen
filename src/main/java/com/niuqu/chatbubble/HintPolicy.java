package com.niuqu.chatbubble;

public enum HintPolicy {
    ;

    public enum Kind { SYSTEM, MENTION_OR_QUOTE, PLAYER }

    public static boolean shouldShow(Kind kind, boolean strongHintEnabled, boolean mentionStrongHintEnabled) {
        return switch (kind) {
            case SYSTEM -> strongHintEnabled;
            case MENTION_OR_QUOTE -> mentionStrongHintEnabled;
            case PLAYER -> false;
        };
    }
}
