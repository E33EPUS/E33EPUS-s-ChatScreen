package com.niuqu.chatbubble;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.neoforged.neoforge.common.ModConfigSpec;

public class ChatBubbleConfigScreen extends Screen {
    private final Screen lastScreen;
    private static final int LABEL_X = 40;
    private static final int INPUT_X = 165;
    private static final int INPUT_W = 80;
    private static final int PREVIEW_X = 255;
    private static final int ROW_H = 28;
    private static final int START_Y = 38;

    private int scrollOffset;
    private final List<AbstractWidget> scrollWidgets = new ArrayList<>();
    private int previewStartIdx = -1;

    private interface WidgetFactory {
        AbstractWidget create(int y);
    }

    private record Entry(String labelKey, WidgetFactory factory, boolean isGap) {}

    private List<Entry> entries;

    private void buildEntries() {
        if (entries != null) return;
        entries = new ArrayList<>();
        entries.add(new Entry("e33chat.config.enabled", y -> mkBoolButton(y, ChatBubbleConfig.ENABLED), false));
        entries.add(new Entry("e33chat.config.red_dot", y -> mkBoolButton(y, ChatBubbleConfig.RED_DOT_ENABLED), false));
        entries.add(new Entry("e33chat.config.hide_chat_icon", y -> mkBoolButton(y, ChatBubbleConfig.HIDE_CHAT_ICON), false));
        entries.add(new Entry("e33chat.config.animation", y -> mkBoolButton(y, ChatBubbleConfig.ANIMATION_ENABLED), false));
        entries.add(new Entry("e33chat.config.strong_hint", y -> mkBoolButton(y, ChatBubbleConfig.STRONG_HINT_ENABLED), false));
        entries.add(new Entry("e33chat.config.mention_strong_hint", y -> mkBoolButton(y, ChatBubbleConfig.MENTION_STRONG_HINT_ENABLED), false));
        entries.add(new Entry("e33chat.config.anti_spam", y -> mkBoolButton(y, ChatBubbleConfig.ANTI_SPAM), false));
        entries.add(new Entry("e33chat.config.chat_history", y -> mkBoolButton(y, ChatBubbleConfig.CHAT_HISTORY_ENABLED), false));
        entries.add(new Entry("e33chat.config.preview_enabled", y -> mkBoolButton(y, ChatBubbleConfig.PREVIEW_ENABLED), false));
        entries.add(new Entry("e33chat.config.preview_lines", this::mkCycleButton, false));
        entries.add(new Entry("e33chat.config.preview_width", y -> mkIntBox(y, String.valueOf(ChatBubbleConfig.PREVIEW_WIDTH.get()), 50, 400, ChatBubbleConfig.PREVIEW_WIDTH::set), false));
        entries.add(new Entry("e33chat.config.time_separator", this::mkTimeSepButton, false));
        previewStartIdx = entries.size();
        entries.add(new Entry("e33chat.config.own_bubble_color", y -> mkHexBox(y, ChatBubbleConfig.OWN_BUBBLE_COLOR.get(), ChatBubbleConfig.OWN_BUBBLE_COLOR::set), false));
        entries.add(new Entry("e33chat.config.other_bubble_color", y -> mkHexBox(y, ChatBubbleConfig.OTHER_BUBBLE_COLOR.get(), ChatBubbleConfig.OTHER_BUBBLE_COLOR::set), false));
        entries.add(new Entry("e33chat.config.own_text_color", y -> mkHexBox(y, ChatBubbleConfig.OWN_TEXT_COLOR.get(), ChatBubbleConfig.OWN_TEXT_COLOR::set), false));
        entries.add(new Entry("e33chat.config.other_text_color", y -> mkHexBox(y, ChatBubbleConfig.OTHER_TEXT_COLOR.get(), ChatBubbleConfig.OTHER_TEXT_COLOR::set), false));
        entries.add(new Entry(null, null, true)); // section gap
        entries.add(new Entry("e33chat.config.system_chat_as_bubble", y -> mkBoolButton(y, ChatBubbleConfig.SYSTEM_CHAT_AS_BUBBLE), false));
        entries.add(new Entry("e33chat.config.chat_report_compat", y -> mkBoolButton(y, ChatBubbleConfig.CHAT_REPORT_COMPAT), false));
    }

    public ChatBubbleConfigScreen(Screen lastScreen) {
        super(Component.translatable("e33chat.config.title"));
        this.lastScreen = lastScreen;
    }

    @Override
    protected void init() {
        super.init();
        buildEntries();
        scrollWidgets.clear();
        scrollOffset = Mth.clamp(scrollOffset, 0, calcMaxScroll());

        int y = START_Y - scrollOffset;
        for (Entry e : entries) {
            if (e.isGap) { y += 12; continue; }
            scrollWidgets.add(addRenderableWidget(e.factory.create(y)));
            y += ROW_H;
        }

        addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, btn -> onClose())
            .bounds(width / 2 - 100, height - 32, 200, 20).build());
    }

    private Button mkBoolButton(int y, ModConfigSpec.BooleanValue cfg) {
        boolean v = cfg.get();
        return Button.builder(
            v ? CommonComponents.OPTION_ON : CommonComponents.OPTION_OFF,
            btn -> {
                boolean nv = !cfg.get();
                cfg.set(nv);
                btn.setMessage(nv ? CommonComponents.OPTION_ON : CommonComponents.OPTION_OFF);
            }
        ).bounds(INPUT_X, y, INPUT_W, 20).build();
    }

    private Button mkCycleButton(int y) {
        return Button.builder(
            Component.literal(String.valueOf(ChatBubbleConfig.PREVIEW_LINES.get())),
            btn -> {
                int v = ChatBubbleConfig.PREVIEW_LINES.get() + 1;
                if (v > 8) v = 1;
                ChatBubbleConfig.PREVIEW_LINES.set(v);
                btn.setMessage(Component.literal(String.valueOf(v)));
            }
        ).bounds(INPUT_X, y, INPUT_W, 20).build();
    }

    private static final int[] TIME_SEP_VALUES = {1, 5, 10, 15, 30, 0};

    private Button mkTimeSepButton(int y) {
        int v = ChatBubbleConfig.TIME_SEPARATOR_MINUTES.get();
        String label = v == 0
            ? Component.translatable("e33chat.config.time_separator.disable").getString()
            : v + Component.translatable("e33chat.config.time_separator.minute").getString();
        return Button.builder(
            Component.literal(label),
            btn -> {
                int cur = ChatBubbleConfig.TIME_SEPARATOR_MINUTES.get();
                int idx = 0;
                for (int i = 0; i < TIME_SEP_VALUES.length; i++) {
                    if (TIME_SEP_VALUES[i] == cur) { idx = (i + 1) % TIME_SEP_VALUES.length; break; }
                }
                int nv = TIME_SEP_VALUES[idx];
                ChatBubbleConfig.TIME_SEPARATOR_MINUTES.set(nv);
                String nl = nv == 0
                    ? Component.translatable("e33chat.config.time_separator.disable").getString()
                    : nv + Component.translatable("e33chat.config.time_separator.minute").getString();
                btn.setMessage(Component.literal(nl));
            }
        ).bounds(INPUT_X, y, INPUT_W, 20).build();
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
        super.render(g, mouseX, mouseY, partialTick);
        g.drawCenteredString(font, title, width / 2, 14, 0xFFFFFFFF);

        int y = START_Y + 6 - scrollOffset;

        // Section header: general
        int generalHeaderY = START_Y - scrollOffset - 20;
        Component generalHeader = Component.translatable("e33chat.config.section.general");
        if (generalHeaderY > -ROW_H && generalHeaderY < height)
            g.drawString(font, generalHeader, LABEL_X, generalHeaderY, 0xFFFFAA00, false);

        String[] previewColors = {
            ChatBubbleConfig.OWN_BUBBLE_COLOR.get(),
            ChatBubbleConfig.OTHER_BUBBLE_COLOR.get(),
            ChatBubbleConfig.OWN_TEXT_COLOR.get(),
            ChatBubbleConfig.OTHER_TEXT_COLOR.get()
        };
        int previewIdx = 0;

        boolean compatHeaderDrawn = false;
        for (Entry e : entries) {
            if (e.isGap) {
                y += 12;
                if (!compatHeaderDrawn) {
                    int compatHeaderY = y - 20;
                    Component compatHeader = Component.translatable("e33chat.config.section.compatibility");
                    if (compatHeaderY > -ROW_H && compatHeaderY < height)
                        g.drawString(font, compatHeader, LABEL_X, compatHeaderY, 0xFFFFAA00, false);
                    compatHeaderDrawn = true;
                }
                continue;
            }
            if (y > -ROW_H && y < height)
                g.drawString(font, Component.translatable(e.labelKey), LABEL_X, y, 0xFFFFFFFF, false);
            y += ROW_H;
        }

        // Color previews
        int py = START_Y + previewStartIdx * ROW_H + 4 - scrollOffset;
        for (String hex : previewColors) {
            drawPreview(g, py, hex);
            py += ROW_H;
        }
    }

    private void drawPreview(GuiGraphics g, int y, String hex) {
        int color = ChatBubbleConfig.parseHexColor(hex, 0xFF000000);
        g.fill(PREVIEW_X, y, PREVIEW_X + 14, y + 14, 0xFF444444);
        g.fill(PREVIEW_X + 1, y + 1, PREVIEW_X + 13, y + 13, color);
    }

    @Override
    public void onClose() {
        if (minecraft != null) minecraft.setScreen(lastScreen);
    }

    @Override
    public void renderBackground(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        g.fill(0, 0, width, height, 0xC0101010);
    }

    private int calcMaxScroll() {
        int total = 0;
        for (Entry e : entries) {
            if (e.isGap) total += 12;
            else total += ROW_H;
        }
        return Math.max(0, START_Y + total - (height - 42));
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        int maxScroll = calcMaxScroll();
        if (maxScroll <= 0) return false;
        scrollOffset -= (int) (scrollY * 20);
        scrollOffset = Mth.clamp(scrollOffset, 0, maxScroll);

        int y = START_Y - scrollOffset;
        int wi = 0;
        for (Entry e : entries) {
            if (e.isGap) { y += 12; continue; }
            if (wi < scrollWidgets.size())
                scrollWidgets.get(wi).setY(y);
            wi++;
            y += ROW_H;
        }
        return true;
    }
}
