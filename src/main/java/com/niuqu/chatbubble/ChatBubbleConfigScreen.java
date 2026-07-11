package com.niuqu.chatbubble;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

public class ChatBubbleConfigScreen extends Screen {
    private final Screen lastScreen;
    private static final int LABEL_X = 40;
    private static final int INPUT_X = 165;
    private static final int INPUT_W = 135;
    private static final int PREVIEW_X = 308;
    private static final int ROW_H = 28;
    private static final int START_Y = 38;

    private int scrollOffset;
    private final List<AbstractWidget> scrollWidgets = new ArrayList<>();

    public ChatBubbleConfigScreen(Screen lastScreen) {
        super(Component.translatable("e33chat.config.title"));
        this.lastScreen = lastScreen;
    }

    @Override
    protected void init() {
        scrollWidgets.clear();
        scrollOffset = Mth.clamp(scrollOffset, 0, calcMaxScroll());
        int y = START_Y - scrollOffset;

        scrollWidgets.add(addRenderableWidget(CycleButton.onOffBuilder(E33ChatConfig.enabled)
            .create(INPUT_X, y, INPUT_W, 20, Component.translatable("e33chat.config.enabled"),
                (btn, val) -> E33ChatConfig.enabled = val)));
        y += ROW_H;

        scrollWidgets.add(addRenderableWidget(CycleButton.onOffBuilder(E33ChatConfig.redDot)
            .create(INPUT_X, y, INPUT_W, 20, Component.translatable("e33chat.config.red_dot"),
                (btn, val) -> E33ChatConfig.redDot = val)));
        y += ROW_H;

        scrollWidgets.add(addRenderableWidget(CycleButton.onOffBuilder(E33ChatConfig.animation)
            .create(INPUT_X, y, INPUT_W, 20, Component.translatable("e33chat.config.animation"),
                (btn, val) -> E33ChatConfig.animation = val)));
        y += ROW_H;

        scrollWidgets.add(addRenderableWidget(CycleButton.onOffBuilder(E33ChatConfig.strongHint)
            .create(INPUT_X, y, INPUT_W, 20, Component.translatable("e33chat.config.strong_hint"),
                (btn, val) -> E33ChatConfig.strongHint = val)));
        y += ROW_H;

        scrollWidgets.add(addRenderableWidget(CycleButton.onOffBuilder(E33ChatConfig.mentionStrongHint)
            .create(INPUT_X, y, INPUT_W, 20, Component.translatable("e33chat.config.mention_strong_hint"),
                (btn, val) -> E33ChatConfig.mentionStrongHint = val)));
        y += ROW_H;

        scrollWidgets.add(addRenderableWidget(CycleButton.onOffBuilder(E33ChatConfig.previewEnabled)
            .create(INPUT_X, y, INPUT_W, 20, Component.translatable("e33chat.config.preview_enabled"),
                (btn, val) -> E33ChatConfig.previewEnabled = val)));
        y += ROW_H;

        scrollWidgets.add(addRenderableWidget(CycleButton.<Integer>builder(v -> Component.literal(String.valueOf(v)))
            .withValues(1, 2, 3)
            .withInitialValue(E33ChatConfig.previewLines)
            .create(INPUT_X, y, INPUT_W, 20, Component.translatable("e33chat.config.preview_lines"),
                (btn, val) -> E33ChatConfig.previewLines = val)));
        y += ROW_H;

        EditBox widthBox = mkIntBox(y, String.valueOf(E33ChatConfig.previewWidth), 50, 400,
            v -> E33ChatConfig.previewWidth = v);
        scrollWidgets.add(addRenderableWidget(widthBox)); y += ROW_H;

        EditBox ownBubbleBox = mkHexBox(y, E33ChatConfig.ownBubbleColor,
            s -> { E33ChatConfig.ownBubbleColor = s; });
        scrollWidgets.add(addRenderableWidget(ownBubbleBox)); y += ROW_H;

        EditBox otherBubbleBox = mkHexBox(y, E33ChatConfig.otherBubbleColor,
            s -> { E33ChatConfig.otherBubbleColor = s; });
        scrollWidgets.add(addRenderableWidget(otherBubbleBox)); y += ROW_H;

        EditBox ownTextBox = mkHexBox(y, E33ChatConfig.ownTextColor,
            s -> { E33ChatConfig.ownTextColor = s; });
        scrollWidgets.add(addRenderableWidget(ownTextBox)); y += ROW_H;

        EditBox otherTextBox = mkHexBox(y, E33ChatConfig.otherTextColor,
            s -> { E33ChatConfig.otherTextColor = s; });
        scrollWidgets.add(addRenderableWidget(otherTextBox)); y += ROW_H;

        addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, btn -> onClose())
            .bounds(width / 2 - 100, height - 32, 200, 20).build());
    }

    private EditBox mkHexBox(int y, String initial, java.util.function.Consumer<String> onChange) {
        EditBox box = new EditBox(font, INPUT_X, y, INPUT_W, 20, Component.literal(""));
        box.setValue(initial);
        box.setMaxLength(7);
        box.setResponder(s -> {
            if (!s.matches("#?[0-9a-fA-F]{0,6}")) return;
            if (s.length() == 6 && !s.startsWith("#")) {
                box.setValue("#" + s);
                onChange.accept("#" + s);
            } else if (s.length() == 7) {
                onChange.accept(s);
            }
        });
        return box;
    }

    private EditBox mkIntBox(int y, String initial, int min, int max, java.util.function.Consumer<Integer> onChange) {
        EditBox box = new EditBox(font, INPUT_X, y, INPUT_W, 20, Component.literal(""));
        box.setValue(initial);
        box.setMaxLength(3);
        box.setResponder(s -> {
            if (!s.matches("\\d*")) return;
            try {
                int v = Integer.parseInt(s);
                if (v >= min && v <= max) onChange.accept(v);
            } catch (NumberFormatException ignored) {}
        });
        return box;
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        renderBackground(g);
        g.drawCenteredString(font, title, width / 2, 14, 0xFFFFFFFF);

        int y = START_Y + 6 - scrollOffset;
        String[] labels = {"e33chat.config.enabled", "e33chat.config.red_dot", "e33chat.config.animation",
            "e33chat.config.strong_hint", "e33chat.config.mention_strong_hint",
            "e33chat.config.preview_enabled", "e33chat.config.preview_lines", "e33chat.config.preview_width",
            "e33chat.config.own_bubble_color", "e33chat.config.other_bubble_color", "e33chat.config.own_text_color", "e33chat.config.other_text_color",
        };
        for (String label : labels) {
            if (y > -ROW_H && y < height)
                g.drawString(font, Component.translatable(label), LABEL_X, y, 0xFFAAAAAA, false);
            y += ROW_H;
        }

        int py = START_Y + ROW_H * 8 + 4 - scrollOffset;
        drawPreview(g, py, E33ChatConfig.ownBubbleColor); py += ROW_H;
        drawPreview(g, py, E33ChatConfig.otherBubbleColor); py += ROW_H;
        drawPreview(g, py, E33ChatConfig.ownTextColor); py += ROW_H;
        drawPreview(g, py, E33ChatConfig.otherTextColor);

        super.render(g, mouseX, mouseY, partialTick);
    }

    private void drawPreview(GuiGraphics g, int y, String hex) {
        int color = E33ChatConfig.parseHexColor(hex, 0xFF000000);
        g.fill(PREVIEW_X, y, PREVIEW_X + 14, y + 14, 0xFF444444);
        g.fill(PREVIEW_X + 1, y + 1, PREVIEW_X + 13, y + 13, color);
    }

    @Override
    public void onClose() {
        E33ChatConfig.save();
        minecraft.setScreen(lastScreen);
    }

    private int calcMaxScroll() {
        int contentBottom = START_Y + 12 * ROW_H + 10;
        return Math.max(0, contentBottom - (height - 42));
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        int maxScroll = calcMaxScroll();
        if (maxScroll <= 0) return false;
        scrollOffset -= (int) (delta * 20);
        scrollOffset = Mth.clamp(scrollOffset, 0, maxScroll);
        for (int i = 0; i < scrollWidgets.size(); i++)
            scrollWidgets.get(i).setY(START_Y + i * ROW_H - scrollOffset);
        return true;
    }
}
