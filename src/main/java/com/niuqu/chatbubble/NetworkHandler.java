package com.niuqu.chatbubble;

import com.niuqu.chatbubble.packets.ChatMetaPayload;
import com.niuqu.chatbubble.packets.QuoteSyncPayload;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public class NetworkHandler {

    @SubscribeEvent
    public static void registerPayloads(RegisterPayloadHandlersEvent event) {
        // optional: joining servers without this mod must not be rejected
        PayloadRegistrar registrar = event.registrar("1").optional();
        registrar.playToServer(QuoteSyncPayload.TYPE, QuoteSyncPayload.STREAM_CODEC, QuoteSyncPayload::handleServer);
        registrar.playToClient(ChatMetaPayload.TYPE, ChatMetaPayload.STREAM_CODEC, ChatMetaPayload::handleClient);
    }
}
