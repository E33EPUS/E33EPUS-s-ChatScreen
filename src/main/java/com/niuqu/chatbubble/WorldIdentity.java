package com.niuqu.chatbubble;

public final class WorldIdentity {
    private WorldIdentity() {}

    public static String key(String conn, String dim, int sessionToken) {
        return conn + "|DIM:" + dim + "|TOK:" + sessionToken;
    }

    public static String historyKey(String conn, String dim) {
        return conn + "|DIM:" + dim;
    }
}
