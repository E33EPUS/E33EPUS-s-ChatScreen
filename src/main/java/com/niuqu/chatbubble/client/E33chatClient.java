package com.niuqu.chatbubble.client;

import com.niuqu.chatbubble.*;
import com.niuqu.chatbubble.mixin.MinecraftServerAccessor;
import com.niuqu.chatbubble.packets.E33chatNetworking;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class E33chatClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        E33ChatConfig.load();

        HudRenderCallback.EVENT.register((drawContext, tickDelta) -> {
            if (!E33ChatConfig.enabled) return;
            ChatBubbleHudOverlay.render(drawContext);
        });

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            String key;
            if (client.level == null || client.player == null) {
                key = null;
            } else if (client.getSingleplayerServer() != null) {
                key = "SP:" + ((MinecraftServerAccessor) client.getSingleplayerServer()).getStorageSource().getLevelId();
            } else if (client.getCurrentServer() != null) {
                key = "MP:" + client.getCurrentServer().ip;
            } else {
                key = "world";
            }
            ChatMessageStore.setCurrentWorld(key);
            ChatMessageStore.tickPreview();
            ChatMessageStore.tickStrongHint();
        });

        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
            System.out.println("[e33chat] JOIN fired! world="
                + (client.getCurrentServer() != null ? client.getCurrentServer().name : "singleplayer"));
            ChatMessageStore.resetForNewWorld();
        });

        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
            System.out.println("[e33chat] DISCONNECT fired, clearing messages");
            ChatMessageStore.resetForNewWorld();
        });

        ClientPlayNetworking.registerGlobalReceiver(E33chatNetworking.CHAT_META,
            (client, handler, buf, responseSender) -> {
                UUID senderUUID = buf.readUUID();
                String messageHash = buf.readUtf();
                String quoteSender = buf.readUtf();
                String quoteContent = buf.readUtf();
                int mentionCount = buf.readInt();
                List<String> mentions = new ArrayList<>();
                for (int i = 0; i < mentionCount; i++) mentions.add(buf.readUtf());
                client.execute(() ->
                    ChatMessageStore.applyChatMeta(senderUUID, messageHash, quoteSender, quoteContent, mentions));
            });
    }
}
