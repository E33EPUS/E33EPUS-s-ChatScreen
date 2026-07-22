package com.niuqu.chatbubble;

import com.niuqu.chatbubble.chat.ChatMessage;
import net.minecraft.text.Text;

import java.util.*;

public class ChatRuntimeState {
    static final int MAX_MESSAGES = 10000;
    static final int PREVIEW_TICKS = 100;

    final List<ChatMessage> messages = new ArrayList<>();
    int unreadCount;
    boolean screenOpen;
    String pendingReplyContent;
    String pendingReplySender;
    final List<PreviewEntry> previews = new ArrayList<>();
    final Deque<HintEntry> strongHintQueue = new ArrayDeque<>();
    int strongHintTicks;
    final Set<String> unreadWhisperPartners = new HashSet<>();

    final List<EchoEntry> pendingEchoes = new ArrayList<>();
    long pendingWhisperEchoTime;
    boolean suppressNextCapture;

    final Map<String, PendingMeta> pendingMetas = new HashMap<>();

    public static class PreviewEntry {
        public final Text text;
        public int ticks;
        public PreviewEntry(Text text, int ticks) { this.text = text; this.ticks = ticks; }
    }

    record HintEntry(Text text, boolean isMention) {}
    record EchoEntry(String text, long time) {}
    record PendingMeta(UUID senderUUID, String quoteSender, String quoteContent,
                       List<String> mentionTargets, java.time.LocalTime createdAt) {}

    public void clear() {
        messages.clear(); unreadCount = 0; screenOpen = false;
        pendingReplyContent = null; pendingReplySender = null;
        previews.clear(); strongHintQueue.clear(); strongHintTicks = 0;
        unreadWhisperPartners.clear(); pendingEchoes.clear();
        pendingWhisperEchoTime = 0; suppressNextCapture = false;
        pendingMetas.clear();
    }

    public boolean isRecentDuplicate(String content) {
        int size = messages.size();
        for (int i = size - 1; i >= 0 && i >= size - 2; i--)
            if (messages.get(i).content().getString().equals(content)) return true;
        return false;
    }

    public void markWhisperUnread(String partner) { if (partner != null) unreadWhisperPartners.add(partner); }
    public void clearWhisperUnread(String partner) { unreadWhisperPartners.remove(partner); }
    public boolean hasWhisperUnread(String partner) { return unreadWhisperPartners.contains(partner); }

    public void purgeStaleEchoes() {
        long cutoff = System.currentTimeMillis() - 10000;
        pendingEchoes.removeIf(e -> e.time() < cutoff);
    }
    public void incrementPendingEcho(String sentText) {
        purgeStaleEchoes();
        pendingEchoes.add(new EchoEntry(sentText, System.currentTimeMillis()));
    }
    public boolean consumeEchoBySystemChat(String incomingText) {
        purgeStaleEchoes();
        for (int i = 0; i < pendingEchoes.size(); i++) {
            if (incomingText.equals(pendingEchoes.get(i).text())) {
                pendingEchoes.remove(i); return true;
            }
        }
        return false;
    }
}
