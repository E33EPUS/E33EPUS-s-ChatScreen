package com.niuqu.chatbubble;

import com.niuqu.chatbubble.packets.ChatMetaPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.ServerChatEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.network.PacketDistributor;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ChatServerListener {
    private static final Pattern MENTION_PATTERN = Pattern.compile("@(\\w+)");

    // player UUID → pending quote data (QuoteSyncPacket arrived before ServerChatEvent)
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
            ChatMetaPacket meta = new ChatMetaPacket(
                player.getUUID(), messageHash, quoteSender, quoteContent, mentions);
            NetworkHandler.CHANNEL.send(PacketDistributor.ALL.noArg(), meta);
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
