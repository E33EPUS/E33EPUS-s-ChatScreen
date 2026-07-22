package com.niuqu.chatbubble;

import java.util.ArrayList;
import java.util.List;

public final class RoundedRectangleGeometry {

    public static final int MAX_CONFIG_RADIUS = 10;

    public record Span(int x1, int y1, int x2, int y2) {}

    private RoundedRectangleGeometry() {}

    public static int clampRadius(int width, int height, int requestedRadius) {
        if (width <= 0 || height <= 0 || requestedRadius <= 0) return 0;
        return Math.min(Math.min(requestedRadius, MAX_CONFIG_RADIUS), Math.min(width, height) / 2);
    }

    public static List<Span> spans(int x1, int y1, int x2, int y2, int requestedRadius) {
        int width = x2 - x1;
        int height = y2 - y1;
        if (width <= 0 || height <= 0) return List.of();

        int radius = clampRadius(width, height, requestedRadius);
        if (radius == 0) return List.of(new Span(x1, y1, x2, y2));

        List<Span> spans = new ArrayList<>(radius * 2 + 1);
        for (int row = 0; row < radius; row++) {
            int inset = cornerInset(radius, row);
            spans.add(new Span(x1 + inset, y1 + row, x2 - inset, y1 + row + 1));
        }
        if (y1 + radius < y2 - radius) {
            spans.add(new Span(x1, y1 + radius, x2, y2 - radius));
        }
        for (int row = radius - 1; row >= 0; row--) {
            int inset = cornerInset(radius, row);
            int y = y2 - row - 1;
            spans.add(new Span(x1 + inset, y, x2 - inset, y + 1));
        }
        return List.copyOf(spans);
    }

    static int cornerInset(int radius, int row) {
        double fromCenter = radius - row - 0.5;
        double circleWidth = Math.sqrt(radius * radius - fromCenter * fromCenter);
        return Math.min(radius - 1, Math.max(0, (int) Math.ceil(radius - circleWidth)));
    }
}
