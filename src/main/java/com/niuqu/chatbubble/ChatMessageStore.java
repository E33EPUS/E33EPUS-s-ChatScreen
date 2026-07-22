package com.niuqu.chatbubble;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.niuqu.chatbubble.chat.ChatMessage;
import com.niuqu.chatbubble.config.ChatBubbleConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class ChatMessageStore {
    public static final int STRONG_HINT_DURATION = 60;

    private static volatile ChatMessageStore INSTANCE;

    String currentWorldKey;
    String historyKey;
    int sessionToken = new Random().nextInt();
    static final Gson GSON = new Gson();

    final ChatRuntimeState rt;

    private static final ThreadLocal<SenderMeta> PENDING_META = new ThreadLocal<>();

    public record SenderMeta(UUID senderUUID, Text senderName,
                             Text rawContent, boolean isSystem,
                             String rawPlayerName,
                             boolean whisper, String whisperPartner) {}

    ChatMessageStore(ChatRuntimeState rt) { this.rt = rt; }

    // --- singleton (delegates through lifecycle when available) ---
    public static ChatMessageStore getInstance() {
        var lifecycle = ChatBubbleClientSetup.lifecycle();
        if (lifecycle != null) return lifecycle.messageStore();
        if (INSTANCE == null) INSTANCE = new ChatMessageStore(new ChatRuntimeState());
        return INSTANCE;
    }
    public static void resetInstance() { INSTANCE = null; }

    void clearMessages() { rt.messages.clear(); }
    void tryLoad(String key) {
        if (key == null) return;
        historyKey = key;
        tryLoadMessages();
    }

    // ===== public API (static bridges) =====

    public static void debugLog(String msg) {
        var cfg = ChatBubbleClientSetup.config();
        if (cfg != null && cfg.debugLog()) com.mojang.logging.LogUtils.getLogger().info(msg);
    }

    public static void setPendingMeta(SenderMeta meta) { PENDING_META.set(meta); }
    public static SenderMeta consumePendingMeta() { SenderMeta m = PENDING_META.get(); PENDING_META.remove(); return m; }

    public static void addMessage(Text content, UUID senderUUID, Text senderName, boolean isSystem, String rawPlayerName, boolean whisper, String whisperPartner) {
        getInstance().addMessageImpl(content, senderUUID, senderName, isSystem, rawPlayerName, whisper, whisperPartner);
    }
    public static List<ChatMessage> getMessages() { return getInstance().rt.messages; }
    public static List<ChatMessage> getWhisperMessages(String partner) {
        ChatRuntimeState r = getInstance().rt;
        List<ChatMessage> out = new ArrayList<>();
        for (ChatMessage m : r.messages) if (m.whisper() && partner.equals(m.whisperPartner())) out.add(m);
        return out;
    }
    public static List<ChatMessage> getPublicMessages() {
        ChatRuntimeState r = getInstance().rt;
        List<ChatMessage> out = new ArrayList<>();
        for (ChatMessage m : r.messages) if (!m.whisper()) out.add(m);
        return out;
    }
    public static ChatMessage getLatestWhisperWith(String partner) {
        List<ChatMessage> msgs = getInstance().rt.messages;
        for (int i = msgs.size() - 1; i >= 0; i--) { ChatMessage m = msgs.get(i); if (m.whisper() && partner.equals(m.whisperPartner())) return m; }
        return null;
    }
    public static ChatMessage getLatestPublicMessage() {
        List<ChatMessage> msgs = getInstance().rt.messages;
        for (int i = msgs.size() - 1; i >= 0; i--) { ChatMessage m = msgs.get(i); if (!m.whisper()) return m; }
        return null;
    }

    public static int getUnreadCount() { return getInstance().rt.unreadCount; }
    public static void setScreenOpen(boolean open) {
        ChatRuntimeState r = getInstance().rt;
        r.screenOpen = open;
        if (open) r.unreadCount = 0;
    }
    public static boolean hasUnreadMention(String playerName) {
        if (playerName == null || playerName.isEmpty()) return false;
        ChatRuntimeState r = getInstance().rt;
        for (int i = r.messages.size() - r.unreadCount; i < r.messages.size(); i++) {
            if (i < 0) continue;
            if (r.messages.get(i).content().getString().contains("@" + playerName)) return true;
        }
        return false;
    }

    public static void markWhisperUnread(String partner) { if (partner != null) getInstance().rt.markWhisperUnread(partner); }
    public static void clearUnreadWhisper(String partner) { getInstance().rt.clearWhisperUnread(partner); }
    public static boolean hasUnreadWhisper(String partner) { return getInstance().rt.hasWhisperUnread(partner); }

    public static void markPendingWhisperEcho() { getInstance().rt.pendingWhisperEchoTime = System.currentTimeMillis(); }
    public static void markSuppressCapture() { getInstance().rt.suppressNextCapture = true; }
    public static boolean hasPendingWhisperEcho() {
        ChatRuntimeState r = getInstance().rt;
        return r.pendingWhisperEchoTime != 0 && System.currentTimeMillis() - r.pendingWhisperEchoTime < 10_000;
    }
    public static void consumeWhisperEcho() { getInstance().rt.pendingWhisperEchoTime = 0; }
    public static boolean consumeSuppressCapture() {
        ChatRuntimeState r = getInstance().rt;
        if (r.suppressNextCapture) { r.suppressNextCapture = false; return true; }
        return false;
    }
    public static void incrementPendingEcho(String sentText) { getInstance().rt.incrementPendingEcho(sentText); }
    public static boolean consumeEchoBySystemChat(String incomingText) { return getInstance().rt.consumeEchoBySystemChat(incomingText); }
    public static boolean isRecentDuplicate(String content) { return getInstance().rt.isRecentDuplicate(content); }
    public static boolean consumeEchoIfSenderMatches(Text senderName) { return getInstance().consumeEchoIfSenderMatchesImpl(senderName); }

    public static void setPendingReply(String content, String sender) {
        ChatRuntimeState r = getInstance().rt;
        r.pendingReplyContent = content;
        r.pendingReplySender = sender;
    }

    public static List<ChatRuntimeState.PreviewEntry> getPreviews() {
        List<ChatRuntimeState.PreviewEntry> p = getInstance().rt.previews;
        return p.isEmpty() ? null : p;
    }
    public static void tickPreview() {
        ChatRuntimeState r = getInstance().rt;
        var it = r.previews.iterator();
        while (it.hasNext()) { ChatRuntimeState.PreviewEntry e = it.next(); if (--e.ticks <= 0) it.remove(); }
    }
    public static Text getStrongHintText() {
        ChatRuntimeState r = getInstance().rt;
        if (r.strongHintQueue.isEmpty()) return null;
        return r.strongHintTicks > 0 ? r.strongHintQueue.peek().text() : null;
    }
    public static boolean isStrongHintMention() {
        ChatRuntimeState r = getInstance().rt;
        if (r.strongHintQueue.isEmpty()) return false;
        return r.strongHintQueue.peek().isMention();
    }
    public static int getStrongHintTicks() { return getInstance().rt.strongHintTicks; }
    public static void tickStrongHint() {
        ChatRuntimeState r = getInstance().rt;
        if (r.strongHintTicks > 0) {
            r.strongHintTicks--;
            if (r.strongHintTicks <= 0) {
                r.strongHintQueue.poll();
                if (!r.strongHintQueue.isEmpty()) r.strongHintTicks = STRONG_HINT_DURATION;
            }
        }
    }

    public static void setCurrentWorld(String conn, String dim) { getInstance().setCurrentWorldImpl(conn, dim); }

    public static void addHistoryMessages(List<HistoryEntry> entries) { getInstance().addHistoryMessagesImpl(entries); }
    public record HistoryEntry(UUID senderUUID, String senderName, String content,
                               LocalTime time, boolean isSystem, String replyContent, String replySender) {}

    public static void applyChatMeta(UUID senderUUID, String messageHash, String quoteSender,
                                      String quoteContent, List<String> mentionTargets) {
        getInstance().applyChatMetaImpl(senderUUID, messageHash, quoteSender, quoteContent, mentionTargets);
    }

    public static ChatMessage getMessageAt(int index) {
        List<ChatMessage> msgs = getInstance().rt.messages;
        return (index < 0 || index >= msgs.size()) ? null : msgs.get(index);
    }
    public static Text sliceStyled(Text src, int start, int end) {
        MutableText out = Text.empty();
        int[] pos = {0};
        src.visit((style, text) -> {
            int s = pos[0], e = s + text.length();
            pos[0] = e;
            int from = Math.max(start, s), to = Math.min(end, e);
            if (from < to) out.append(Text.literal(text.substring(from - s, to - s)).fillStyle(style));
            return Optional.empty();
        }, Style.EMPTY);
        return out;
    }

    // ===== instance methods =====

    private boolean consumeEchoIfSenderMatchesImpl(Text senderName) {
        rt.purgeStaleEchoes();
        if (rt.pendingEchoes.isEmpty()) return false;
        var player = MinecraftClient.getInstance().player;
        if (player == null) return false;
        String s = senderName.getString();
        String name = player.getName().getString();
        boolean match = wordContains(s, name);
        if (!match && player.networkHandler != null) {
            var info = player.networkHandler.getPlayerListEntry(player.getUuid());
            if (info != null && info.getDisplayName() != null) {
                String tab = info.getDisplayName().getString().trim();
                match = !tab.isEmpty() && wordContains(s, tab);
            }
        }
        if (match) {
            int best = -1;
            for (int i = 0; i < rt.pendingEchoes.size(); i++) {
                String echoText = rt.pendingEchoes.get(i).text();
                if (s.contains(echoText) || echoText.contains(s)) { best = i; break; }
            }
            if (best >= 0) rt.pendingEchoes.remove(best);
            else rt.pendingEchoes.remove(0);
            updateLatestOwnSenderName(senderName);
            return true;
        }
        return false;
    }

    private static boolean wordContains(String text, String word) {
        int idx = text.indexOf(word);
        if (idx < 0) return false;
        boolean beforeOk = idx == 0 || !Character.isLetterOrDigit(text.charAt(idx - 1));
        int end = idx + word.length();
        boolean afterOk = end >= text.length() || !Character.isLetterOrDigit(text.charAt(end));
        return beforeOk && afterOk;
    }

    private void updateLatestOwnSenderName(Text senderName) {
        for (int i = rt.messages.size() - 1; i >= 0 && i >= rt.messages.size() - 5; i--) {
            ChatMessage m = rt.messages.get(i);
            if (!m.isOwn()) continue;
            if (!m.senderName().getString().equals(senderName.getString())) {
                rt.messages.set(i, new ChatMessage(m.senderUUID(), senderName, m.content(), m.time(),
                    m.isOwn(), m.isSystem(), m.replyContent(), m.replySender(),
                    m.messageHash(), m.duplicateCount(), m.rawPlayerName(), m.whisper(), m.whisperPartner()));
            }
            return;
        }
    }

    private static boolean isSameSender(ChatMessage last, Text senderName, String rawPlayerName) {
        if (rawPlayerName != null && !rawPlayerName.isEmpty()
            && last.rawPlayerName() != null && !last.rawPlayerName().isEmpty())
            return rawPlayerName.equals(last.rawPlayerName());
        if (rawPlayerName != null && !rawPlayerName.isEmpty()
            && (last.rawPlayerName() == null || last.rawPlayerName().isEmpty()))
            return false;
        return last.senderName().getString().equals(senderName.getString());
    }

    void addMessageImpl(Text content, UUID senderUUID, Text senderName, boolean isSystem, String rawPlayerName, boolean whisper, String whisperPartner) {
        String messageHash = String.valueOf(content.getString().hashCode());
        var client = MinecraftClient.getInstance();
        String playerName = client.player != null ? client.player.getName().getString() : "";
        boolean own = (rawPlayerName != null && !rawPlayerName.isEmpty())
            ? rawPlayerName.equals(playerName)
            : senderName != null && senderName.getString().equals(playerName);

        var cfg = ChatBubbleClientSetup.config();
        boolean antiSpam = cfg != null && cfg.antiSpam();

        if (antiSpam && !rt.messages.isEmpty()) {
            ChatMessage last = rt.messages.get(rt.messages.size() - 1);
            if (!last.isSystem() && isSameSender(last, senderName, rawPlayerName)
                && last.content().getString().equals(content.getString())) {
                if (own && rt.pendingReplyContent != null) { rt.pendingReplyContent = null; rt.pendingReplySender = null; }
                rt.messages.set(rt.messages.size() - 1, new ChatMessage(last.senderUUID(), last.senderName(), last.content(),
                    LocalTime.now(), last.isOwn(), last.isSystem(), last.replyContent(), last.replySender(),
                    last.messageHash(), last.duplicateCount() + 1, last.rawPlayerName(), last.whisper(), last.whisperPartner()));
                return;
            }
        }

        ChatRuntimeState.PendingMeta pending = rt.pendingMetas.remove(messageHash);
        if (pending != null && pending.createdAt().isBefore(LocalTime.now().minusSeconds(10))) pending = null;

        String replyContent = null, replySender = null;
        if (own && rt.pendingReplyContent != null) {
            replyContent = rt.pendingReplyContent; replySender = rt.pendingReplySender;
            rt.pendingReplyContent = null; rt.pendingReplySender = null;
        } else if (pending != null && !pending.quoteContent().isEmpty()) {
            replyContent = pending.quoteContent(); replySender = pending.quoteSender();
        }

        rt.messages.add(new ChatMessage(senderUUID, senderName != null ? senderName : Text.literal(""), content,
            LocalTime.now(), own, isSystem, replyContent, replySender, messageHash, 1, rawPlayerName, whisper, whisperPartner));
        while (rt.messages.size() > ChatRuntimeState.MAX_MESSAGES) rt.messages.remove(0);

        boolean isMentionOrQuote = !own && !isSystem
            && (content.getString().contains("@" + playerName) || (replySender != null && replySender.equals(playerName)));

        boolean playSound = false;
        if (!own && client.player != null) {
            if (isMentionOrQuote && cfg != null && cfg.soundMention()) playSound = true;
            else if (whisper && cfg != null && cfg.soundWhisper()) playSound = true;
            else if (isSystem && cfg != null && cfg.soundSystem()) playSound = true;
            else if (!isSystem && !whisper && cfg != null && cfg.soundPublic()) playSound = true;
        }
        if (playSound) client.player.playSound(net.minecraft.sound.SoundEvents.BLOCK_NOTE_BLOCK_CHIME.value(), 0.6F, 1.0F);

        if (!rt.screenOpen) {
            rt.unreadCount++;
            boolean mentionHint = cfg != null && HintPolicy.shouldShow(HintPolicy.Kind.MENTION_OR_QUOTE, cfg.strongHintEnabled(), cfg.mentionStrongHintEnabled());
            boolean sysHint = cfg != null && HintPolicy.shouldShow(HintPolicy.Kind.SYSTEM, cfg.strongHintEnabled(), cfg.mentionStrongHintEnabled());

            if (mentionHint && isMentionOrQuote) {
                rt.strongHintQueue.add(new ChatRuntimeState.HintEntry(Text.translatable("e33chat.notif.mention"), true));
                if (rt.strongHintTicks <= 0) rt.strongHintTicks = STRONG_HINT_DURATION;
            }
            if (cfg != null && cfg.previewEnabled() && !sysHint && !(mentionHint && isMentionOrQuote)) {
                Text pc;
                if (senderName == null || senderName.getString().isEmpty()) {
                    pc = isSystem ? Text.translatable("e33chat.sender.system").copy().append(Text.literal(": ")).append(content) : content;
                } else {
                    pc = Text.empty().append(senderName).append(Text.literal(": ")).append(content);
                }
                rt.previews.add(new ChatRuntimeState.PreviewEntry(pc, ChatRuntimeState.PREVIEW_TICKS));
                while (rt.previews.size() > (cfg != null ? cfg.previewLines() : 3)) rt.previews.remove(0);
            }
            if (sysHint && isSystem && !(mentionHint && isMentionOrQuote)) {
                rt.strongHintQueue.removeIf(e -> !e.isMention());
                rt.strongHintQueue.add(new ChatRuntimeState.HintEntry(content, false));
                if (rt.strongHintTicks <= 0) rt.strongHintTicks = STRONG_HINT_DURATION;
            }
        }
        if (whisper && whisperPartner != null && !own) rt.markWhisperUnread(whisperPartner);
    }

    // ===== world management =====

    void setCurrentWorldImpl(String conn, String dim) {
        String newKey = conn == null ? null : WorldIdentity.key(conn, dim, sessionToken);
        if (Objects.equals(newKey, currentWorldKey)) return;
        String oldConn = historyKey != null && historyKey.contains("|DIM:") ? historyKey.substring(0, historyKey.indexOf("|DIM:")) : null;
        boolean sameConnection = Objects.equals(oldConn, conn);
        boolean wasFallback = currentWorldKey == null || currentWorldKey.equals("world");
        boolean isSpecific = conn != null && (conn.startsWith("SP:") || conn.startsWith("MP:"));
        boolean isRefinement = wasFallback && isSpecific;
        boolean hasPending = currentWorldKey == null && isSpecific && !rt.messages.isEmpty();
        var cfg = ChatBubbleClientSetup.config();
        if (cfg != null && cfg.chatHistoryEnabled() && historyKey != null) saveMessages();
        currentWorldKey = newKey;
        historyKey = conn == null ? null : WorldIdentity.historyKey(conn, dim);
        if (conn != null && !sameConnection) sessionToken = new Random().nextInt();
        if (isRefinement || hasPending || sameConnection) { tryLoadMessages(); return; }
        rt.clear();
        tryLoadMessages();
    }

    // ===== persistence =====

    private void saveMessages() {
        if (historyKey == null || rt.messages.isEmpty()) return;
        File f = historyFile(historyKey);
        f.getParentFile().mkdirs();
        List<Object> list = new ArrayList<>();
        for (ChatMessage msg : rt.messages) {
            try {
                var obj = new HashMap<String, Object>();
                obj.put("senderUUID", msg.senderUUID().toString());
                obj.put("senderName", msg.senderName().getString());
                obj.put("content", msg.content().getString());
                var registries = MinecraftClient.getInstance().getNetworkHandler() != null
                    ? MinecraftClient.getInstance().getNetworkHandler().getRegistryManager() : null;
                if (registries != null) {
                    try { obj.put("senderNameJson", Text.Serialization.toJsonString(msg.senderName(), registries)); } catch (Exception ignored) {}
                    try { obj.put("contentJson", Text.Serialization.toJsonString(msg.content(), registries)); } catch (Exception ignored) {}
                }
                obj.put("time", msg.time().format(DateTimeFormatter.ISO_LOCAL_TIME));
                obj.put("isOwn", msg.isOwn()); obj.put("isSystem", msg.isSystem());
                if (msg.replyContent() != null) { obj.put("replyContent", msg.replyContent()); obj.put("replySender", msg.replySender()); }
                if (msg.rawPlayerName() != null && !msg.rawPlayerName().isEmpty()) obj.put("rawPlayerName", msg.rawPlayerName());
                if (msg.whisper()) { obj.put("whisper", true); if (msg.whisperPartner() != null) obj.put("whisperPartner", msg.whisperPartner()); }
                list.add(obj);
            } catch (Exception e) { com.mojang.logging.LogUtils.getLogger().warn("[e33chat] Failed to save chat history", e); }
        }
        try (Writer w = new OutputStreamWriter(new FileOutputStream(f), StandardCharsets.UTF_8)) { GSON.toJson(list, w); }
        catch (Exception e) { com.mojang.logging.LogUtils.getLogger().warn("[e33chat] Failed to save chat history", e); }
    }

    private void tryLoadMessages() {
        var cfg = ChatBubbleClientSetup.config();
        if (cfg == null || !cfg.chatHistoryEnabled() || historyKey == null) return;
        if (loadMessagesFile(historyKey)) return;
        String conn = historyKey.substring(0, historyKey.indexOf("|DIM:"));
        if (!conn.isEmpty()) loadMessagesFile(conn);
    }

    private boolean loadMessagesFile(String key) {
        File f = historyFile(key);
        if (!f.exists()) return false;
        try (Reader r = new InputStreamReader(new FileInputStream(f), StandardCharsets.UTF_8)) {
            List<Map<String, Object>> list = GSON.fromJson(r, new TypeToken<List<Map<String, Object>>>(){}.getType());
            if (list == null) return false;
            for (Map<String, Object> obj : list) {
                try {
                    UUID uuid = UUID.fromString((String) obj.get("senderUUID"));
                    Text senderName = null;
                    Text content = null;
                    var registries = MinecraftClient.getInstance().getNetworkHandler() != null
                        ? MinecraftClient.getInstance().getNetworkHandler().getRegistryManager() : null;
                    if (registries != null) {
                        String nameJson = (String) obj.get("senderNameJson");
                        String contentJson = (String) obj.get("contentJson");
                        if (nameJson != null) try { senderName = Text.Serialization.fromJson(nameJson, registries); } catch (Exception ignored) {}
                        if (contentJson != null) try { content = Text.Serialization.fromJson(contentJson, registries); } catch (Exception ignored) {}
                    }
                    if (senderName == null) senderName = Text.literal((String) obj.getOrDefault("senderName", ""));
                    if (content == null) content = Text.literal((String) obj.getOrDefault("content", ""));
                    LocalTime time = LocalTime.parse((String) obj.get("time"), DateTimeFormatter.ISO_LOCAL_TIME);
                    boolean isOwn = (Boolean) obj.getOrDefault("isOwn", false);
                    boolean isSystem = (Boolean) obj.getOrDefault("isSystem", false);
                    String replyContent = (String) obj.get("replyContent");
                    String replySender = (String) obj.get("replySender");
                    String rawPlayerName = (String) obj.get("rawPlayerName");
                    boolean whisper = Boolean.TRUE.equals(obj.get("whisper"));
                    String whisperPartner = (String) obj.get("whisperPartner");
                    rt.messages.add(new ChatMessage(uuid, senderName, content, time, isOwn, isSystem,
                        replyContent, replySender, "", 1, rawPlayerName, whisper, whisperPartner));
                } catch (Exception e) { com.mojang.logging.LogUtils.getLogger().warn("[e33chat] Failed to load chat history", e); }
            }
            while (rt.messages.size() > ChatRuntimeState.MAX_MESSAGES) rt.messages.remove(0);
        } catch (Exception e) { com.mojang.logging.LogUtils.getLogger().warn("[e33chat] Failed to load chat history", e); }
        return true;
    }

    void addHistoryMessagesImpl(List<HistoryEntry> entries) {
        if (!rt.messages.isEmpty() || entries.isEmpty()) return;
        for (var e : entries) rt.messages.add(new ChatMessage(e.senderUUID(), Text.literal(e.senderName()),
            Text.literal(e.content()), e.time(), false, e.isSystem(), e.replyContent(), e.replySender(),
            String.valueOf(e.content().hashCode()), 1, e.senderName(), false, null));
    }

    void applyChatMetaImpl(UUID senderUUID, String messageHash, String quoteSender, String quoteContent, List<String> mentionTargets) {
        LocalTime cutoff = LocalTime.now().minusSeconds(5);
        for (int i = rt.messages.size() - 1; i >= 0; i--) {
            ChatMessage msg = rt.messages.get(i);
            if (!msg.messageHash().equals(messageHash) || !msg.senderUUID().equals(senderUUID)) continue;
            if (msg.replyContent() != null || msg.time().isBefore(cutoff)) continue;
            if (!quoteContent.isEmpty()) {
                rt.messages.set(i, new ChatMessage(msg.senderUUID(), msg.senderName(), msg.content(), msg.time(),
                    msg.isOwn(), msg.isSystem(), quoteContent, quoteSender, msg.messageHash(),
                    msg.duplicateCount(), msg.rawPlayerName(), msg.whisper(), msg.whisperPartner()));
                var client = MinecraftClient.getInstance();
                String playerName = client.player != null ? client.player.getName().getString() : "";
                var cfg = ChatBubbleClientSetup.config();
                if (!msg.isOwn() && !playerName.isEmpty() && playerName.equals(quoteSender)
                    && !msg.content().getString().contains("@" + playerName)
                    && cfg != null && cfg.soundMention()) {
                    client.player.playSound(net.minecraft.sound.SoundEvents.BLOCK_NOTE_BLOCK_CHIME.value(), 0.6F, 1.0F);
                    if (!rt.screenOpen && HintPolicy.shouldShow(HintPolicy.Kind.MENTION_OR_QUOTE,
                        cfg.strongHintEnabled(), cfg.mentionStrongHintEnabled())) {
                        rt.strongHintQueue.add(new ChatRuntimeState.HintEntry(Text.translatable("e33chat.notif.mention"), true));
                        if (rt.strongHintTicks <= 0) rt.strongHintTicks = STRONG_HINT_DURATION;
                    }
                }
            }
            return;
        }
        if (!quoteContent.isEmpty()) rt.pendingMetas.put(messageHash, new ChatRuntimeState.PendingMeta(senderUUID, quoteSender, quoteContent, mentionTargets, LocalTime.now()));
    }

    // ===== helpers =====

    private static File historyFile(String key) {
        String safe = key.replaceAll("[^a-zA-Z0-9_.\\-]", "_");
        String hash = Integer.toHexString(key.hashCode());
        return new File(MinecraftClient.getInstance().runDirectory, "e33chat/history/" + safe + "_" + hash + ".json");
    }
}
