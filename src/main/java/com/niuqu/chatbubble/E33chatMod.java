package com.niuqu.chatbubble;

import com.niuqu.chatbubble.packets.E33chatNetworking;
import io.netty.buffer.Unpooled;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.message.v1.ServerMessageEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;

import java.util.*;

public class E33chatMod implements ModInitializer {

    private static final Map<UUID, QuotePending> pendingQuotes = new HashMap<>();

    private record QuotePending(String quoteSender, String quoteContent, String messageHash) {}

    @Override
    public void onInitialize() {
        ServerPlayNetworking.registerGlobalReceiver(E33chatNetworking.QUOTE_SYNC,
            (server, player, handler, buf, responseSender) -> {
                String quoteSender = buf.readUtf();
                String quoteContent = buf.readUtf();
                String messageHash = buf.readUtf();
                pendingQuotes.put(player.getUUID(),
                    new QuotePending(quoteSender, quoteContent, messageHash));
            });

        ServerMessageEvents.CHAT_MESSAGE.register((message, sender, params) -> {
            UUID uuid = sender.getUUID();
            QuotePending pending = pendingQuotes.remove(uuid);
            List<String> mentions = new ArrayList<>();
            String rawText = message.decoratedContent().getString();
            if (sender.getServer() != null && sender.getServer().getPlayerList().getPlayerCount() > 1) {
                java.util.regex.Matcher m = java.util.regex.Pattern.compile("@(\\w+)").matcher(rawText);
                while (m.find()) mentions.add(m.group(1));
            }
            if (pending != null || !mentions.isEmpty()) {
                String msgHash = String.valueOf(rawText.hashCode());
                broadcastChatMeta(sender, uuid, msgHash,
                    pending != null ? pending.quoteSender() : "",
                    pending != null ? pending.quoteContent() : "",
                    mentions);
            }
        });
    }

    private static void broadcastChatMeta(ServerPlayer sender, UUID senderUUID, String messageHash,
                                           String quoteSender, String quoteContent,
                                           List<String> mentions) {
        for (ServerPlayer p : sender.getServer().getPlayerList().getPlayers()) {
            FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
            buf.writeUUID(senderUUID);
            buf.writeUtf(messageHash);
            buf.writeUtf(quoteSender);
            buf.writeUtf(quoteContent);
            buf.writeInt(mentions.size());
            for (String m : mentions) buf.writeUtf(m);
            ServerPlayNetworking.send(p, E33chatNetworking.CHAT_META, buf);
        }
    }
}
