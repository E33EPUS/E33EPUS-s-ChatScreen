package com.niuqu.chatbubble;

import com.niuqu.chatbubble.packets.ChatMetaPacket;
import com.niuqu.chatbubble.packets.HistoryPacket;
import com.niuqu.chatbubble.packets.QuoteSyncPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

public class NetworkHandler {
    private static final String PROTOCOL = "1";
    public static SimpleChannel CHANNEL;

    public static void register() {
        CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(ChatBubbleMod.MODID, "main"),
            () -> PROTOCOL,
            NetworkRegistry.acceptMissingOr(PROTOCOL),
            NetworkRegistry.acceptMissingOr(PROTOCOL)
        );

        CHANNEL.messageBuilder(QuoteSyncPacket.class, 0)
            .encoder(QuoteSyncPacket::encode)
            .decoder(QuoteSyncPacket::decode)
            .consumerMainThread(QuoteSyncPacket::handle)
            .add();

        CHANNEL.messageBuilder(ChatMetaPacket.class, 1)
            .encoder(ChatMetaPacket::encode)
            .decoder(ChatMetaPacket::decode)
            .consumerMainThread(ChatMetaPacket::handle)
            .add();

        CHANNEL.messageBuilder(HistoryPacket.class, 2)
            .encoder(HistoryPacket::encode)
            .decoder(HistoryPacket::decode)
            .consumerMainThread(HistoryPacket::handle)
            .add();
    }
}
