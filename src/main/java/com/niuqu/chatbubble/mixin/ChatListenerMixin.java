package com.niuqu.chatbubble.mixin;

import com.mojang.authlib.GameProfile;
import com.niuqu.chatbubble.ChatMessageStore;
import net.minecraft.client.multiplayer.chat.ChatListener;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.PlayerChatMessage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.UUID;

@Mixin(value = ChatListener.class, priority = 500)
public class ChatListenerMixin {

    @Inject(method = "handlePlayerChatMessage", at = @At("HEAD"), cancellable = true)
    private void onPlayerChat(PlayerChatMessage message, GameProfile gameProfile,
                              ChatType.Bound bound, CallbackInfo ci) {
        var player = net.minecraft.client.Minecraft.getInstance().player;
        var senderId = gameProfile.getId();
        if (player == null || senderId == null || !senderId.equals(player.getUUID())) {
            ChatMessageStore.addMessage(message.decoratedContent(),
                senderId != null ? senderId : new UUID(0, 0),
                Component.literal(gameProfile.getName()), false);
        }
        ci.cancel();
    }

    @Inject(method = "handleDisguisedChatMessage", at = @At("HEAD"), cancellable = true)
    private void onDisguisedChat(Component message, ChatType.Bound bound, CallbackInfo ci) {
        ChatMessageStore.addMessage(message, new UUID(0, 0),
            Component.translatable("e33chat.sender.system"), true);
        ci.cancel();
    }

    @Inject(method = "handleSystemMessage", at = @At("HEAD"), cancellable = true)
    private void onSystemChat(Component message, boolean overlay, CallbackInfo ci) {
        if (overlay) return;
        ChatMessageStore.addMessage(message, new UUID(0, 0),
            Component.translatable("e33chat.sender.system"), true);
        ci.cancel();
    }
}
