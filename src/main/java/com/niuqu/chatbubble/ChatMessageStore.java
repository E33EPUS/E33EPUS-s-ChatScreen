package com.niuqu.chatbubble;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;


public class ChatMessageStore {
    private static final int MAX = 100;
    private static final List<ChatMessage> messages = new ArrayList<>();
    private static int unreadCount = 0;
    private static boolean screenOpen = false;
    private static String pendingReplyContent;
    private static String pendingReplySender;
    private static final List<PreviewEntry> previews = new ArrayList<>();
    private static final int PREVIEW_TICKS = 100;
    private static String strongHintText;
    private static int strongHintTicks;
    public static final int STRONG_HINT_DURATION = 60;

    private static String currentWorldKey;
    private static final Map<String, String> worldTitles = new HashMap<>();
    private static final Gson GSON = new Gson();
    private static boolean titlesLoaded;
    public record ChatMessage(
        UUID senderUUID,
        Component senderName,
        Component content,
        LocalTime time,
        boolean isOwn,
        boolean isSystem,
        String replyContent,
        String replySender
    ) {}

    public static class PreviewEntry {
        public final String text;
        public int ticks;
        public PreviewEntry(String text, int ticks) {
            this.text = text;
            this.ticks = ticks;
        }
    }

    public static void addMessage(Component content, UUID senderUUID, Component senderName, boolean isSystem) {
        content = addUnderlineToClicks(content);
        String playerName = net.minecraft.client.Minecraft.getInstance().player != null
            ? net.minecraft.client.Minecraft.getInstance().player.getName().getString() : "";
        boolean own = senderName != null && senderName.getString().equals(playerName);

        String replyContent = null;
        String replySender = null;
        if (own && pendingReplyContent != null) {
            replyContent = pendingReplyContent;
            replySender = pendingReplySender;
            pendingReplyContent = null;
            pendingReplySender = null;
        }

        messages.add(new ChatMessage(
            senderUUID,
            senderName != null ? senderName : Component.literal(""),
            content,
            LocalTime.now(),
            own,
            isSystem,
            replyContent,
            replySender
        ));

        while (messages.size() > MAX)
            messages.remove(0);

        if (!screenOpen) {
            unreadCount++;
            boolean systemToHint = isSystem && ChatBubbleConfig.STRONG_HINT_ENABLED.get();
            if (ChatBubbleConfig.PREVIEW_ENABLED.get() && !systemToHint) {
                String sender = senderName != null ? senderName.getString() : "";
                if (sender.isEmpty() && isSystem) sender = Component.translatable("e33chat.sender.system").getString();
                String preview = sender.isEmpty() ? content.getString() : sender + ": " + content.getString();
                previews.add(new PreviewEntry(preview, PREVIEW_TICKS));
                while (previews.size() > ChatBubbleConfig.PREVIEW_LINES.get())
                    previews.remove(0);
            }
            if (systemToHint) {
                strongHintText = content.getString();
                strongHintTicks = STRONG_HINT_DURATION;
            }
        }
    }

    private static Component addUnderlineToClicks(Component original) {
        MutableComponent result = Component.empty();
        original.visit((style, text) -> {
            Style newStyle = style.getClickEvent() != null
                ? style.withUnderlined(true)
                : style;
            result.append(Component.literal(text).withStyle(newStyle));
            return Optional.<Object>empty();
        }, Style.EMPTY);
        return result;
    }

    public static List<ChatMessage> getMessages() {
        return messages;
    }

    public static int getUnreadCount() {
        return unreadCount;
    }

    public static void markAllRead() {
        unreadCount = 0;
    }

    public static void setScreenOpen(boolean open) {
        screenOpen = open;
        if (open) {
            unreadCount = 0;
        }
    }

    public static boolean hasUnreadMention(String playerName) {
        if (playerName == null || playerName.isEmpty()) return false;
        for (int i = messages.size() - unreadCount; i < messages.size(); i++) {
            if (i < 0) continue;
            String text = messages.get(i).content().getString();
            if (text.contains("@" + playerName)) return true;
        }
        return false;
    }

    public static Component quoteMessage(int index) {
        if (index < 0 || index >= messages.size()) return Component.literal("");
        ChatMessage msg = messages.get(index);
        MutableComponent quote = Component.literal("> " + msg.senderName().getString() + ": ");
        quote.append(msg.content());
        return quote;
    }

    public static ChatMessage getMessageAt(int index) {
        if (index < 0 || index >= messages.size()) return null;
        return messages.get(index);
    }

    public static void setPendingReply(String content, String sender) {
        pendingReplyContent = content;
        pendingReplySender = sender;
    }

    public static List<PreviewEntry> getPreviews() {
        return previews.isEmpty() ? null : previews;
    }

    public static void tickPreview() {
        var it = previews.iterator();
        while (it.hasNext()) {
            PreviewEntry e = it.next();
            if (--e.ticks <= 0) it.remove();
        }
    }

    public static String getStrongHintText() { return strongHintTicks > 0 ? strongHintText : null; }

    public static int getStrongHintTicks() { return strongHintTicks; }

    public static void tickStrongHint() { if (strongHintTicks > 0) strongHintTicks--; }

    public static int size() {
        return messages.size();
    }

    public static String getCustomTitle() {
        if (currentWorldKey == null) return null;
        loadWorldTitles();
        String v = worldTitles.get(currentWorldKey);
        return (v != null && !v.isEmpty()) ? v : null;
    }

    public static void setCustomTitle(String title) {
        if (currentWorldKey == null) return;
        loadWorldTitles();
        String v = (title != null && !title.isEmpty()) ? title : "";
        if (v.isEmpty())
            worldTitles.remove(currentWorldKey);
        else
            worldTitles.put(currentWorldKey, v);
        saveWorldTitles();
    }

    public static void setCurrentWorld(String name) {
        if (java.util.Objects.equals(name, currentWorldKey)) return;
        currentWorldKey = name;
        messages.clear();
        unreadCount = 0;
        previews.clear();
    }

    private static File getTitlesFile() {
        return new File(Minecraft.getInstance().gameDirectory, "e33chat/titles.json");
    }

    private static void loadWorldTitles() {
        if (titlesLoaded) return;
        titlesLoaded = true;
        File f = getTitlesFile();
        if (!f.exists()) return;
        try (Reader r = new InputStreamReader(new FileInputStream(f), StandardCharsets.UTF_8)) {
            Map<String, String> data = GSON.fromJson(r, new TypeToken<Map<String, String>>(){}.getType());
            if (data != null) worldTitles.putAll(data);
        } catch (Exception ignored) {}
    }

    private static void saveWorldTitles() {
        File f = getTitlesFile();
        f.getParentFile().mkdirs();
        try (Writer w = new OutputStreamWriter(new FileOutputStream(f), StandardCharsets.UTF_8)) {
            GSON.toJson(worldTitles, w);
        } catch (Exception ignored) {}
    }
}
