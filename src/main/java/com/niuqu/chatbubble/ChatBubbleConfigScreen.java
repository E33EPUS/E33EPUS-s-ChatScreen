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

        addRenderableWidget(CycleButton.onOffBuilder(E33ChatConfig.enabled)
            .create(INPUT_X, y, INPUT_W, 20, Component.translatable("e33chat.config.enabled"),
                (btn, val) -> E33ChatConfig.enabled = val));
        y += ROW_H;

        addRenderableWidget(CycleButton.onOffBuilder(E33ChatConfig.redDot)
            .create(INPUT_X, y, INPUT_W, 20, Component.translatable("e33chat.config.red_dot"),
                (btn, val) -> E33ChatConfig.redDot = val));
        y += ROW_H;

        addRenderableWidget(CycleButton.onOffBuilder(E33ChatConfig.animation)
            .create(INPUT_X, y, INPUT_W, 20, Component.translatable("e33chat.config.animation"),
                (btn, val) -> E33ChatConfig.animation = val));
        y += ROW_H;

        EditBox ownBubbleBox = mkHexBox(y, E33ChatConfig.ownBubbleColor,
            s -> { E33ChatConfig.ownBubbleColor = s; });
        addRenderableWidget(ownBubbleBox); y += ROW_H;

        EditBox otherBubbleBox = mkHexBox(y, E33ChatConfig.otherBubbleColor,
            s -> { E33ChatConfig.otherBubbleColor = s; });
        addRenderableWidget(otherBubbleBox); y += ROW_H;

        EditBox ownTextBox = mkHexBox(y, E33ChatConfig.ownTextColor,
            s -> { E33ChatConfig.ownTextColor = s; });
        addRenderableWidget(ownTextBox); y += ROW_H;

        EditBox otherTextBox = mkHexBox(y, E33ChatConfig.otherTextColor,
            s -> { E33ChatConfig.otherTextColor = s; });
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

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        renderBackground(g);
        g.drawCenteredString(font, title, width / 2, 14, 0xFFFFFFFF);

        int y = START_Y + 6;
        String[] labels = {"e33chat.config.enabled", "e33chat.config.red_dot", "e33chat.config.animation",
            "e33chat.config.own_bubble_color", "e33chat.config.other_bubble_color", "e33chat.config.own_text_color", "e33chat.config.other_text_color"};
        for (String label : labels) {
            g.drawString(font, Component.translatable(label), LABEL_X, y, 0xFFAAAAAA, false);
            y += ROW_H;
        }

        int py = START_Y + ROW_H * 3 + 4;
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
}
