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
    private static final int MAX = 10000;
    private static final List<ChatMessage> messages = new ArrayList<>();
    private static int unreadCount = 0;
    private static boolean screenOpen = false;
    private static String pendingReplyContent;
    private static String pendingReplySender;
    private static final List<PreviewEntry> previews = new ArrayList<>();
    private static final int PREVIEW_TICKS = 100;
    private static String strongHintText;
    private static int strongHintTicks;
    private static boolean strongHintIsMention;
    public static final int STRONG_HINT_DURATION = 60;

    private static String currentWorldKey;
    private static final Map<String, String> worldTitles = new HashMap<>();
    private static final Gson GSON = new Gson();
    private static boolean titlesLoaded;
    private static final Map<String, PendingMeta> pendingMetas = new HashMap<>();

    public record SenderMeta(UUID senderUUID, Component senderName,
                             Component rawContent, boolean isSystem,
                             String rawPlayerName,
                             boolean whisper, String whisperPartner) {}

    private static final ThreadLocal<SenderMeta> PENDING_META = new ThreadLocal<>();

    public static void setPendingMeta(SenderMeta meta) { PENDING_META.set(meta); }

    public static SenderMeta consumePendingMeta() {
        SenderMeta m = PENDING_META.get();
        PENDING_META.remove();
        return m;
    }

    private record PendingEcho(String text, long time) {}
    private static final List<PendingEcho> pendingEchoes = new ArrayList<>();

    private static long pendingWhisperEchoTime;
    private static boolean suppressNextCapture;

    public static void markPendingWhisperEcho() { pendingWhisperEchoTime = System.currentTimeMillis(); }
    public static void markSuppressCapture() { suppressNextCapture = true; }

    public static boolean hasPendingWhisperEcho() {
        return pendingWhisperEchoTime != 0 && System.currentTimeMillis() - pendingWhisperEchoTime < 10_000;
    }
    public static void consumeWhisperEcho() { pendingWhisperEchoTime = 0; }

    public static boolean consumeSuppressCapture() {
        if (suppressNextCapture) { suppressNextCapture = false; return true; }
        return false;
    }

    // Echoes not consumed within 10s (e.g. commands with no chat feedback) would
    // otherwise poison the counter and swallow later self-attributed messages
    private static void purgeStaleEchoes() {
        long cutoff = System.currentTimeMillis() - 10_000;
        pendingEchoes.removeIf(e -> e.time() < cutoff);
    }

    public static void incrementPendingEcho(String sentText) {
        purgeStaleEchoes();
        pendingEchoes.add(new PendingEcho(sentText, System.currentTimeMillis()));
    }

    public static boolean consumeEchoBySystemChat(String incomingText) {
        purgeStaleEchoes();
        for (int i = 0; i < pendingEchoes.size(); i++) {
            if (incomingText.equals(pendingEchoes.get(i).text())) {
                pendingEchoes.remove(i);
                return true;
            }
        }
        return false;
    }

    public static void debugLog(String msg) {
        if (ChatBubbleConfig.DEBUG_LOG.get())
            com.mojang.logging.LogUtils.getLogger().info(msg);
    }

    public static boolean consumeEchoIfSenderMatches(Component senderName) {
        purgeStaleEchoes();
        if (pendingEchoes.isEmpty()) return false;
        var player = net.minecraft.client.Minecraft.getInstance().player;
        if (player == null) return false;
        String s = senderName.getString();
        boolean match = s.contains(player.getName().getString());
        if (!match && player.connection != null) {
            var info = player.connection.getPlayerInfo(player.getUUID());
            if (info != null && info.getTabListDisplayName() != null) {
                String tab = info.getTabListDisplayName().getString().trim();
                match = !tab.isEmpty() && s.contains(tab);
            }
        }
        if (match) {
            pendingEchoes.remove(0);
            updateLatestOwnSenderName(senderName);
            return true;
        }
        return false;
    }

    // The local echo bubble is created with the bare name before the server's
    // decorated version (titles/prefixes) is known — patch it when the echo arrives
    private static void updateLatestOwnSenderName(Component senderName) {
        for (int i = messages.size() - 1; i >= 0 && i >= messages.size() - 5; i--) {
            ChatMessage m = messages.get(i);
            if (!m.isOwn()) continue;
            if (!m.senderName().getString().equals(senderName.getString())) {
                messages.set(i, new ChatMessage(
                    m.senderUUID(), senderName, m.content(), m.time(),
                    m.isOwn(), m.isSystem(), m.replyContent(), m.replySender(),
                    m.messageHash(), m.duplicateCount(), m.rawPlayerName(),
                    m.whisper(), m.whisperPartner()));
            }
            return;
        }
    }

    public static boolean isRecentDuplicate(String content) {
        int size = messages.size();
        for (int i = size - 1; i >= 0 && i >= size - 2; i--) {
            if (messages.get(i).content().getString().equals(content)) return true;
        }
        return false;
    }

    private record PendingMeta(UUID senderUUID, String quoteSender, String quoteContent,
                               List<String> mentionTargets, LocalTime createdAt) {}

    public record ChatMessage(
        UUID senderUUID,
        Component senderName,
        Component content,
        LocalTime time,
        boolean isOwn,
        boolean isSystem,
        String replyContent,
        String replySender,
        String messageHash,
        int duplicateCount,
        String rawPlayerName,
        boolean whisper,
        String whisperPartner
    ) {}

    public static class PreviewEntry {
        public final Component text;
        public int ticks;
        public PreviewEntry(Component text, int ticks) {
            this.text = text;
            this.ticks = ticks;
        }
    }

    public static void addMessage(Component content, UUID senderUUID, Component senderName, boolean isSystem, String rawPlayerName, boolean whisper, String whisperPartner) {
        content = addUnderlineToClicks(content);
        String messageHash = String.valueOf(content.getString().hashCode());

        String playerName = net.minecraft.client.Minecraft.getInstance().player != null
            ? net.minecraft.client.Minecraft.getInstance().player.getName().getString() : "";
        boolean own = (rawPlayerName != null && !rawPlayerName.isEmpty())
            ? rawPlayerName.equals(playerName)
            : senderName != null && senderName.getString().equals(playerName);

        if (ChatBubbleConfig.ANTI_SPAM.get() && !messages.isEmpty()) {
            ChatMessage last = messages.get(messages.size() - 1);
            if (!last.isSystem() && last.senderName().getString().equals(senderName.getString())
                && last.content().getString().equals(content.getString())) {
                if (own && pendingReplyContent != null) {
                    pendingReplyContent = null;
                    pendingReplySender = null;
                }
                messages.set(messages.size() - 1, new ChatMessage(
                    last.senderUUID(), last.senderName(), last.content(),
                    LocalTime.now(),
                    last.isOwn(), last.isSystem(),
                    last.replyContent(), last.replySender(), last.messageHash(),
                    last.duplicateCount() + 1,
                    last.rawPlayerName(),
                    last.whisper(), last.whisperPartner()
                ));
                return;
            }
        }

        PendingMeta pending = pendingMetas.remove(messageHash);
        if (pending != null && pending.createdAt().isBefore(LocalTime.now().minusSeconds(10))) {
            pending = null;
        }

        String replyContent = null;
        String replySender = null;
        if (own && pendingReplyContent != null) {
            replyContent = pendingReplyContent;
            replySender = pendingReplySender;
            pendingReplyContent = null;
            pendingReplySender = null;
        } else if (pending != null && !pending.quoteContent().isEmpty()) {
            replyContent = pending.quoteContent();
            replySender = pending.quoteSender();
        }

        messages.add(new ChatMessage(
            senderUUID,
            senderName != null ? senderName : Component.literal(""),
            content,
            LocalTime.now(),
            own,
            isSystem,
            replyContent,
            replySender,
            messageHash,
            1,
            rawPlayerName,
            whisper,
            whisperPartner
        ));

        while (messages.size() > MAX)
            messages.remove(0);

        boolean isMentionOrQuote = !own && !isSystem
            && (content.getString().contains("@" + playerName)
                || (replySender != null && replySender.equals(playerName)));

        if (isMentionOrQuote && Minecraft.getInstance().player != null) {
            Minecraft.getInstance().player.playSound(
                net.minecraft.sounds.SoundEvents.NOTE_BLOCK_CHIME.get(), 0.6F, 1.0F);
        }

        if (!screenOpen) {
            unreadCount++;
            boolean systemToHint = isSystem && ChatBubbleConfig.STRONG_HINT_ENABLED.get();
            boolean mentionToHint = isMentionOrQuote && ChatBubbleConfig.MENTION_STRONG_HINT_ENABLED.get();

            if (mentionToHint) {
                strongHintText = Component.translatable("e33chat.notif.mention").getString();
                strongHintTicks = STRONG_HINT_DURATION;
                strongHintIsMention = true;
            }

            if (ChatBubbleConfig.PREVIEW_ENABLED.get() && !systemToHint && !mentionToHint) {
                Component previewComp;
                if (senderName == null || senderName.getString().isEmpty()) {
                    if (isSystem) {
                        previewComp = Component.translatable("e33chat.sender.system").copy().append(Component.literal(": ")).append(content);
                    } else {
                        previewComp = content;
                    }
                } else {
                    previewComp = Component.empty().append(senderName).append(Component.literal(": ")).append(content);
                }
                previews.add(new PreviewEntry(previewComp, PREVIEW_TICKS));
                while (previews.size() > ChatBubbleConfig.PREVIEW_LINES.get())
                    previews.remove(0);
            }
            if (systemToHint && !mentionToHint) {
                strongHintText = content.getString();
                strongHintTicks = STRONG_HINT_DURATION;
                strongHintIsMention = false;
            }
        }

        if (whisper && whisperPartner != null && !own) {
            markWhisperUnread(whisperPartner);
        }
    }

    public static Component sliceStyled(Component src, int start, int end) {
        MutableComponent out = Component.empty();
        int[] pos = {0};
        src.visit((style, text) -> {
            int s = pos[0], e = s + text.length();
            pos[0] = e;
            int from = Math.max(start, s), to = Math.min(end, e);
            if (from < to)
                out.append(Component.literal(text.substring(from - s, to - s)).withStyle(style));
            return Optional.<Object>empty();
        }, Style.EMPTY);
        return out;
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

    public static List<ChatMessage> getWhisperMessages(String partnerName) {
        List<ChatMessage> result = new ArrayList<>();
        for (ChatMessage msg : messages) {
            if (msg.whisper() && partnerName.equals(msg.whisperPartner())) {
                result.add(msg);
            }
        }
        return result;
    }

    public static List<ChatMessage> getPublicMessages() {
        List<ChatMessage> result = new ArrayList<>();
        for (ChatMessage msg : messages) {
            if (!msg.whisper()) {
                result.add(msg);
            }
        }
        return result;
    }

    public static ChatMessage getLatestWhisperWith(String partnerName) {
        for (int i = messages.size() - 1; i >= 0; i--) {
            ChatMessage msg = messages.get(i);
            if (msg.whisper() && partnerName.equals(msg.whisperPartner())) {
                return msg;
            }
        }
        return null;
    }

    public static ChatMessage getLatestPublicMessage() {
        for (int i = messages.size() - 1; i >= 0; i--) {
            ChatMessage msg = messages.get(i);
            if (!msg.whisper()) {
                return msg;
            }
        }
        return null;
    }

    private static final Set<String> unreadWhisperPartners = new java.util.HashSet<>();

    public static void markWhisperUnread(String partner) {
        if (partner != null) unreadWhisperPartners.add(partner);
    }

    public static void clearUnreadWhisper(String partner) {
        unreadWhisperPartners.remove(partner);
    }

    public static boolean hasUnreadWhisper(String partner) {
        return unreadWhisperPartners.contains(partner);
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
        String qName = (msg.rawPlayerName() != null && !msg.rawPlayerName().isEmpty())
            ? msg.rawPlayerName() : msg.senderName().getString();
        MutableComponent quote = Component.literal("> " + qName + ": ");
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

    public static boolean isStrongHintMention() { return strongHintIsMention; }

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
        boolean wasFallback = "world".equals(currentWorldKey);
        boolean isSpecific = name != null && (name.startsWith("SP:") || name.startsWith("MP:"));
        boolean isRefinement = wasFallback && isSpecific;
        boolean hasPendingMessages = currentWorldKey == null && isSpecific && !messages.isEmpty();
        if (ChatBubbleConfig.CHAT_HISTORY_ENABLED.get() && isWorldSpecific(currentWorldKey))
            saveMessages(currentWorldKey);
        currentWorldKey = name;
        if (isRefinement || hasPendingMessages) {
            if (ChatBubbleConfig.CHAT_HISTORY_ENABLED.get() && isWorldSpecific(currentWorldKey))
                loadMessages(currentWorldKey);
            return;
        }
        messages.clear();
        unreadCount = 0;
        previews.clear();
        if (ChatBubbleConfig.CHAT_HISTORY_ENABLED.get() && isWorldSpecific(currentWorldKey))
            loadMessages(currentWorldKey);
    }

    private static boolean isWorldSpecific(String key) {
        return key != null && (key.startsWith("SP:") || key.startsWith("MP:"));
    }

    private static File getHistoryFile(String worldKey) {
        String safe = worldKey.replaceAll("[^a-zA-Z0-9_.\\-]", "_");
        String hash = Integer.toHexString(worldKey.hashCode());
        return new File(Minecraft.getInstance().gameDirectory, "e33chat/history/" + safe + "_" + hash + ".json");
    }

    private static String toJsonSafe(Component c) {
        try {
            return Component.Serializer.toJson(c);
        } catch (Exception e) {
            return Component.Serializer.toJson(Component.literal(c.getString()));
        }
    }

    private static void saveMessages(String worldKey) {
        if (messages.isEmpty()) return;
        File f = getHistoryFile(worldKey);
        f.getParentFile().mkdirs();
        List<Object> list = new ArrayList<>();
        for (ChatMessage msg : messages) {
            try {
                var obj = new HashMap<String, Object>();
                obj.put("senderUUID", msg.senderUUID().toString());
                obj.put("senderName", msg.senderName().getString());
                obj.put("senderNameJson", toJsonSafe(msg.senderName()));
                obj.put("content", toJsonSafe(msg.content()));
                obj.put("time", msg.time().format(DateTimeFormatter.ISO_LOCAL_TIME));
                obj.put("isOwn", msg.isOwn());
                obj.put("isSystem", msg.isSystem());
                if (msg.replyContent() != null) {
                    obj.put("replyContent", msg.replyContent());
                    obj.put("replySender", msg.replySender());
                }
                if (msg.rawPlayerName() != null && !msg.rawPlayerName().isEmpty()) {
                    obj.put("rawPlayerName", msg.rawPlayerName());
                }
                if (msg.whisper()) {
                    obj.put("whisper", true);
                    if (msg.whisperPartner() != null) obj.put("whisperPartner", msg.whisperPartner());
                }
                list.add(obj);
            } catch (Exception ignored) {}
        }
        try (Writer w = new OutputStreamWriter(new FileOutputStream(f), StandardCharsets.UTF_8)) {
            GSON.toJson(list, w);
        } catch (Exception ignored) {}
    }

    private static void loadMessages(String worldKey) {
        File f = getHistoryFile(worldKey);
        if (!f.exists()) return;
        try (Reader r = new InputStreamReader(new FileInputStream(f), StandardCharsets.UTF_8)) {
            List<Map<String, Object>> list = GSON.fromJson(r, new TypeToken<List<Map<String, Object>>>(){}.getType());
            if (list == null) return;
            for (Map<String, Object> obj : list) {
                try {
                    UUID uuid = UUID.fromString((String) obj.get("senderUUID"));
                    Component senderName = null;
                    String snJson = (String) obj.get("senderNameJson");
                    if (snJson != null) {
                        try { senderName = Component.Serializer.fromJson(snJson); } catch (Exception ignored2) {}
                    }
                    if (senderName == null) senderName = Component.literal((String) obj.get("senderName"));
                    Component content = Component.Serializer.fromJson((String) obj.get("content"));
                    if (content == null) content = Component.literal("");
                    LocalTime time = LocalTime.parse((String) obj.get("time"), DateTimeFormatter.ISO_LOCAL_TIME);
                    boolean isOwn = (Boolean) obj.getOrDefault("isOwn", false);
                    boolean isSystem = (Boolean) obj.getOrDefault("isSystem", false);
                    String replyContent = (String) obj.get("replyContent");
                    String replySender = (String) obj.get("replySender");
                    String rawPlayerName = (String) obj.get("rawPlayerName");
                    boolean whisper = Boolean.TRUE.equals(obj.get("whisper"));
                    String whisperPartner = (String) obj.get("whisperPartner");
                    messages.add(new ChatMessage(uuid, senderName, content, time,
                        isOwn, isSystem, replyContent, replySender, "", 1, rawPlayerName,
                        whisper, whisperPartner));
                } catch (Exception ignored) {}
            }
            while (messages.size() > MAX) messages.remove(0);
        } catch (Exception ignored) {}
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

    public static void applyChatMeta(UUID senderUUID, String messageHash, String quoteSender,
                                      String quoteContent, List<String> mentionTargets) {
        LocalTime cutoff = LocalTime.now().minusSeconds(5);
        for (int i = messages.size() - 1; i >= 0; i--) {
            ChatMessage msg = messages.get(i);
            if (msg.messageHash().equals(messageHash) && msg.senderUUID().equals(senderUUID)) {
                if (msg.replyContent() != null) continue;
                if (msg.time().isBefore(cutoff)) continue;
                if (!quoteContent.isEmpty()) {
                    messages.set(i, new ChatMessage(
                        msg.senderUUID(), msg.senderName(), msg.content(), msg.time(),
                        msg.isOwn(), msg.isSystem(), quoteContent, quoteSender, msg.messageHash(),
                        msg.duplicateCount(), msg.rawPlayerName(),
                        msg.whisper(), msg.whisperPartner()));
                    String playerName = Minecraft.getInstance().player != null
                        ? Minecraft.getInstance().player.getName().getString() : "";
                    if (!msg.isOwn() && !playerName.isEmpty()
                        && playerName.equals(quoteSender)
                        && !msg.content().getString().contains("@" + playerName)) {
                        Minecraft.getInstance().player.playSound(
                            net.minecraft.sounds.SoundEvents.NOTE_BLOCK_CHIME.get(), 0.6F, 1.0F);
                        if (!screenOpen && ChatBubbleConfig.MENTION_STRONG_HINT_ENABLED.get()) {
                            strongHintText = Component.translatable("e33chat.notif.mention").getString();
                            strongHintTicks = STRONG_HINT_DURATION;
                            strongHintIsMention = true;
                        }
                    }
                }
                return;
            }
        }
        if (!quoteContent.isEmpty()) {
            pendingMetas.put(messageHash, new PendingMeta(senderUUID, quoteSender, quoteContent,
                mentionTargets, LocalTime.now()));
        }
    }
}
