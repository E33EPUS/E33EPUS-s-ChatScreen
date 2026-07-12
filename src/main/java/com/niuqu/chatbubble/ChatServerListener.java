package com.niuqu.chatbubble;

import com.niuqu.chatbubble.packets.ChatMetaPayload;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.ServerChatEvent;
import net.neoforged.neoforge.network.PacketDistributor;

public class ChatServerListener {
    private static final Pattern MENTION_PATTERN = Pattern.compile("@(\\w+)");

    private static final Map<UUID, QuotePending> pendingQuotes = new HashMap<>();

    private record QuotePending(String quotedSenderName, String quotedContent, String messageHash) {}

    @SubscribeEvent
    public void onServerChat(ServerChatEvent event) {
        ServerPlayer player = event.getPlayer();
        String rawText = event.getRawText();
        String messageHash = String.valueOf(rawText.hashCode());

        List<String> mentions = extractMentions(rawText, player.getServer().getPlayerList().getPlayerCount());

        QuotePending quote = pendingQuotes.remove(player.getUUID());
        String quoteSender = quote != null ? quote.quotedSenderName() : "";
        String quoteContent = quote != null ? quote.quotedContent() : "";

        if (quote != null || !mentions.isEmpty()) {
            ChatMetaPayload meta = new ChatMetaPayload(
                player.getUUID(), messageHash, quoteSender, quoteContent, mentions);
            PacketDistributor.sendToAllPlayers(meta);
        }
    }

    public static void onQuoteReceived(UUID senderUUID, String quotedSenderName,
                                        String quotedContent, String messageHash) {
        pendingQuotes.put(senderUUID, new QuotePending(quotedSenderName, quotedContent, messageHash));
    }

    private static List<String> extractMentions(String text, int playerCount) {
        if (playerCount <= 1) return Collections.emptyList();
        List<String> mentions = new ArrayList<>();
        Matcher m = MENTION_PATTERN.matcher(text);
        while (m.find()) {
            mentions.add(m.group(1));
        }
        return mentions;
    }
}
