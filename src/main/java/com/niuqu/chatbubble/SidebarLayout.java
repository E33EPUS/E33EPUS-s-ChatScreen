package com.niuqu.chatbubble;

public record SidebarLayout(int scroll, int publicTop, int publicHeight,
                             int playersTop, int rowHeight, int playerCount) {
    public static final int NONE = -2, PUBLIC = -1;

    public static SidebarLayout create(int viewHeight, int requestedScroll,
                                        int publicTop, int playersTop, int playerCount) {
        int row = 22;
        int viewport = Math.max(0, viewHeight - playersTop);
        int max = Math.max(0, playerCount * row - viewport);
        return new SidebarLayout(
            Math.max(0, Math.min(requestedScroll, max)),
            publicTop, 20, playersTop, row, playerCount);
    }

    public int hit(double y) {
        if (y >= publicTop && y < publicTop + publicHeight) return PUBLIC;
        if (y < playersTop) return NONE;
        int index = (int) ((y - (playersTop - scroll)) / rowHeight);
        return index >= 0 && index < playerCount ? index : NONE;
    }

    public int maxScroll() {
        int viewport = Math.max(0, playersTop + playerCount * rowHeight - playersTop);
        return Math.max(0, playerCount * rowHeight - viewport);
    }
}
