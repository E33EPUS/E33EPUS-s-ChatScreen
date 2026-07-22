package com.niuqu.chatbubble;

import com.niuqu.chatbubble.chat.ChatMessage;
import net.minecraft.text.Text;

import java.util.*;
import java.util.function.LongSupplier;

public final class ClientLifecycleState {

    private static final int APPLIED_METADATA_LIMIT = 256;
    private static final long APPLIED_METADATA_TTL_MILLIS = 30_000L;

    private final ChatMessageStore messageStore;
    private final ChatRuntimeState runtimeState;
    private final LongSupplier clock;

    private final List<PendingMeta> pendingMetadata = new ArrayList<>();
    private final LinkedHashMap<AppliedKey, Long> appliedMetadata = new LinkedHashMap<>();

    private String currentWorld;
    private String currentHistoryKey;
    private boolean screenOpen;

    public record PendingMeta(UUID senderUUID, String messageHash,
                               String quoteSender, String quoteContent,
                               List<String> mentionTargets) {}

    private record AppliedKey(UUID senderUUID, String messageHash, String quoteContent) {}

    public ClientLifecycleState() {
        this(System::currentTimeMillis);
    }

    ClientLifecycleState(LongSupplier clock) {
        this.clock = Objects.requireNonNull(clock);
        this.runtimeState = new ChatRuntimeState();
        this.messageStore = new ChatMessageStore(runtimeState);
    }

    public ChatMessageStore messageStore() { return messageStore; }
    public ChatRuntimeState runtimeState() { return runtimeState; }

    // ===== world lifecycle =====

    public synchronized void setCurrentWorld(String conn, String dim) {
        pendingMetadata.clear();
        appliedMetadata.clear();
        messageStore.setCurrentWorldImpl(conn, dim);
    }

    public synchronized void disconnect() {
        screenOpen = false;
        pendingMetadata.clear();
        appliedMetadata.clear();
        messageStore.clearMessages();
        runtimeState.clear();
    }

    // ===== screen state =====

    public synchronized void setScreenOpen(boolean open) {
        screenOpen = open;
        if (open) runtimeState.unreadCount = 0;
    }

    public synchronized boolean isScreenOpen() { return screenOpen; }

    // ===== metadata (Codex store-and-replay) =====

    public synchronized void receiveMetadata(UUID senderUUID, String messageHash,
                                              String quoteSender, String quoteContent,
                                              List<String> mentionTargets) {
        boolean applied = tryApplyMetadata(senderUUID, messageHash, quoteSender, quoteContent, mentionTargets);
        if (!applied) {
            pendingMetadata.add(new PendingMeta(senderUUID, messageHash, quoteSender, quoteContent, mentionTargets));
        }
    }

    public synchronized void processMessageArrival(ChatMessage msg, int index, String localPlayerName) {
        var it = pendingMetadata.iterator();
        while (it.hasNext()) {
            PendingMeta meta = it.next();
            if (!meta.senderUUID().equals(msg.senderUUID()) || !meta.messageHash().equals(msg.messageHash()))
                continue;
            if (wasApplied(meta)) continue;
            applyQuote(msg, index, meta);
            markApplied(meta);
            checkMention(meta, msg, index, localPlayerName);
            it.remove();
        }
    }

    private boolean tryApplyMetadata(UUID senderUUID, String messageHash,
                                      String quoteSender, String quoteContent,
                                      List<String> mentionTargets) {
        var msgs = runtimeState.messages;
        for (int i = msgs.size() - 1; i >= 0; i--) {
            ChatMessage msg = msgs.get(i);
            if (!msg.senderUUID().equals(senderUUID) || !msg.messageHash().equals(messageHash)) continue;
            PendingMeta meta = new PendingMeta(senderUUID, messageHash, quoteSender, quoteContent, mentionTargets);
            if (wasApplied(meta)) return true;
            applyQuote(msg, i, meta);
            markApplied(meta);
            String localName = net.minecraft.client.MinecraftClient.getInstance().player != null
                ? net.minecraft.client.MinecraftClient.getInstance().player.getName().getString() : "";
            checkMention(meta, msg, i, localName);
            return true;
        }
        return false;
    }

    private void applyQuote(ChatMessage msg, int index, PendingMeta meta) {
        if (meta.quoteContent().isEmpty()) return;
        runtimeState.messages.set(index, new ChatMessage(
            msg.senderUUID(), msg.senderName(), msg.content(), msg.time(),
            msg.isOwn(), msg.isSystem(), meta.quoteContent(), meta.quoteSender(),
            msg.messageHash(), msg.duplicateCount(), msg.rawPlayerName(),
            msg.whisper(), msg.whisperPartner()));
    }

    private void checkMention(PendingMeta meta, ChatMessage msg, int index, String localPlayerName) {
        boolean mentioned = !localPlayerName.isEmpty() && meta.mentionTargets().contains(localPlayerName);
        boolean quoted = !meta.quoteContent().isEmpty()
            && msg.content().getString().contains(localPlayerName);
        if (mentioned || quoted) {
            runtimeState.strongHintQueue.add(
                new ChatRuntimeState.HintEntry(Text.translatable("e33chat.notif.mention"), true));
            if (runtimeState.strongHintTicks <= 0)
                runtimeState.strongHintTicks = ChatMessageStore.STRONG_HINT_DURATION;
        }
    }

    private boolean wasApplied(PendingMeta meta) {
        long now = clock.getAsLong();
        pruneApplied(now);
        return appliedMetadata.containsKey(
            new AppliedKey(meta.senderUUID(), meta.messageHash(), meta.quoteContent()));
    }

    private void markApplied(PendingMeta meta) {
        long now = clock.getAsLong();
        pruneApplied(now);
        appliedMetadata.put(
            new AppliedKey(meta.senderUUID(), meta.messageHash(), meta.quoteContent()), now);
        while (appliedMetadata.size() > APPLIED_METADATA_LIMIT) {
            var it = appliedMetadata.keySet().iterator();
            it.next();
            it.remove();
        }
    }

    private void pruneApplied(long now) {
        appliedMetadata.entrySet().removeIf(e -> now - e.getValue() >= APPLIED_METADATA_TTL_MILLIS);
    }
}
