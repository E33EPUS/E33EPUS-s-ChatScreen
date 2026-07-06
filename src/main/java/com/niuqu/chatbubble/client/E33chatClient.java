package com.niuqu.chatbubble.client;

import com.niuqu.chatbubble.*;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;

public class E33chatClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        E33ChatConfig.load();

        HudRenderCallback.EVENT.register((drawContext, tickDelta) -> {
            if (!E33ChatConfig.enabled) return;
            ChatBubbleHudOverlay.render(drawContext);
        });

        ClientTickEvents.END_CLIENT_TICK.register(client -> ChatMessageStore.tickPreview());

        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
            System.out.println("[e33chat] JOIN fired! world="
                + (client.getCurrentServer() != null ? client.getCurrentServer().name : "singleplayer"));
            ChatMessageStore.resetForNewWorld();
        });

        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
            System.out.println("[e33chat] DISCONNECT fired, clearing messages");
            ChatMessageStore.resetForNewWorld();
        });
    }
}
