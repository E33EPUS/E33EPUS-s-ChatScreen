package com.niuqu.chatbubble;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

public class ChatBubbleConfigScreen extends Screen {
    private final Screen lastScreen;
    private static final int LABEL_X = 40;
    private static final int INPUT_X = 165;
    private static final int INPUT_W = 135;
    private static final int PREVIEW_X = 308;
    private static final int ROW_H = 28;
    private static final int START_Y = 38;

    public ChatBubbleConfigScreen(Screen lastScreen) {
        super(Component.translatable("e33chat.config.title"));
        this.lastScreen = lastScreen;
    }

    @Override
    protected void init() {
        int y = START_Y;

        addRenderableWidget(CycleButton.onOffBuilder(ChatBubbleConfig.ENABLED.get())
            .create(INPUT_X, y, INPUT_W, 20, Component.translatable("e33chat.config.enabled"),
                (btn, val) -> ChatBubbleConfig.ENABLED.set(val)));
        y += ROW_H;

        addRenderableWidget(CycleButton.onOffBuilder(ChatBubbleConfig.RED_DOT_ENABLED.get())
            .create(INPUT_X, y, INPUT_W, 20, Component.translatable("e33chat.config.red_dot"),
                (btn, val) -> ChatBubbleConfig.RED_DOT_ENABLED.set(val)));
        y += ROW_H;

        addRenderableWidget(CycleButton.onOffBuilder(ChatBubbleConfig.ANIMATION_ENABLED.get())
            .create(INPUT_X, y, INPUT_W, 20, Component.translatable("e33chat.config.animation"),
                (btn, val) -> ChatBubbleConfig.ANIMATION_ENABLED.set(val)));
        y += ROW_H;

        addRenderableWidget(CycleButton.onOffBuilder(ChatBubbleConfig.STRONG_HINT_ENABLED.get())
            .create(INPUT_X, y, INPUT_W, 20, Component.translatable("e33chat.config.strong_hint"),
                (btn, val) -> ChatBubbleConfig.STRONG_HINT_ENABLED.set(val)));
        y += ROW_H;

        addRenderableWidget(CycleButton.onOffBuilder(ChatBubbleConfig.PREVIEW_ENABLED.get())
            .create(INPUT_X, y, INPUT_W, 20, Component.translatable("e33chat.config.preview_enabled"),
                (btn, val) -> ChatBubbleConfig.PREVIEW_ENABLED.set(val)));
        y += ROW_H;

        addRenderableWidget(CycleButton.<Integer>builder(v -> Component.literal(String.valueOf(v)))
            .withValues(1, 2, 3)
            .withInitialValue(ChatBubbleConfig.PREVIEW_LINES.get())
            .create(INPUT_X, y, INPUT_W, 20, Component.translatable("e33chat.config.preview_lines"),
                (btn, val) -> ChatBubbleConfig.PREVIEW_LINES.set(val)));
        y += ROW_H;

        EditBox widthBox = mkIntBox(y, String.valueOf(ChatBubbleConfig.PREVIEW_WIDTH.get()), 50, 400, ChatBubbleConfig.PREVIEW_WIDTH::set);
        addRenderableWidget(widthBox); y += ROW_H;

        EditBox ownBubbleBox = mkHexBox(y, ChatBubbleConfig.OWN_BUBBLE_COLOR.get(), ChatBubbleConfig.OWN_BUBBLE_COLOR::set);
        addRenderableWidget(ownBubbleBox); y += ROW_H;

        EditBox otherBubbleBox = mkHexBox(y, ChatBubbleConfig.OTHER_BUBBLE_COLOR.get(), ChatBubbleConfig.OTHER_BUBBLE_COLOR::set);
        addRenderableWidget(otherBubbleBox); y += ROW_H;

        EditBox ownTextBox = mkHexBox(y, ChatBubbleConfig.OWN_TEXT_COLOR.get(), ChatBubbleConfig.OWN_TEXT_COLOR::set);
        addRenderableWidget(ownTextBox); y += ROW_H;

        EditBox otherTextBox = mkHexBox(y, ChatBubbleConfig.OTHER_TEXT_COLOR.get(), ChatBubbleConfig.OTHER_TEXT_COLOR::set);
        addRenderableWidget(otherTextBox); y += ROW_H;

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

        int y = START_Y + 6;
        String[] labels = {"e33chat.config.enabled", "e33chat.config.red_dot", "e33chat.config.animation",
            "e33chat.config.strong_hint",
            "e33chat.config.preview_enabled", "e33chat.config.preview_lines", "e33chat.config.preview_width",
            "e33chat.config.own_bubble_color", "e33chat.config.other_bubble_color", "e33chat.config.own_text_color", "e33chat.config.other_text_color"};
        for (String label : labels) {
            g.drawString(font, Component.translatable(label), LABEL_X, y, 0xFFAAAAAA, false);
            y += ROW_H;
        }

        int py = START_Y + ROW_H * 7 + 4;
        drawPreview(g, py, ChatBubbleConfig.OWN_BUBBLE_COLOR.get()); py += ROW_H;
        drawPreview(g, py, ChatBubbleConfig.OTHER_BUBBLE_COLOR.get()); py += ROW_H;
        drawPreview(g, py, ChatBubbleConfig.OWN_TEXT_COLOR.get()); py += ROW_H;
        drawPreview(g, py, ChatBubbleConfig.OTHER_TEXT_COLOR.get());

        super.render(g, mouseX, mouseY, partialTick);
    }

    private void drawPreview(GuiGraphics g, int y, String hex) {
        int color = ChatBubbleConfig.parseHexColor(hex, 0xFF000000);
        g.fill(PREVIEW_X, y, PREVIEW_X + 14, y + 14, 0xFF444444);
        g.fill(PREVIEW_X + 1, y + 1, PREVIEW_X + 13, y + 13, color);
    }

    @Override
    public void onClose() {
        minecraft.setScreen(lastScreen);
    }
}
