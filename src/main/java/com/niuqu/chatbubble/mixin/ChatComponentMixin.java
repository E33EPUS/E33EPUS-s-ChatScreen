package com.niuqu.chatbubble.mixin;

import com.niuqu.chatbubble.ChatBubbleConfig;
import com.niuqu.chatbubble.ChatMessageStore;
import net.minecraft.client.GuiMessageTag;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MessageSignature;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.UUID;

@Mixin(value = ChatComponent.class, priority = 500)
public class ChatComponentMixin {

    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    private void onRender(GuiGraphics guiGraphics, int tickCount, int mouseX,
                          int mouseY, CallbackInfo ci) {
        if (ChatBubbleConfig.ENABLED.get()) {
            ci.cancel();
        }
    }

    @Inject(method = "addMessage(Lnet/minecraft/network/chat/Component;)V",
            at = @At("HEAD"), cancellable = true)
    private void onAddMessage(Component message, CallbackInfo ci) {
        if (ChatBubbleConfig.ENABLED.get()) {
            ChatMessageStore.addMessage(message, new UUID(0, 0),
                Component.translatable("e33chat.sender.system"), true);
            ci.cancel();
        }
    }

    @Inject(method = "addMessage(Lnet/minecraft/network/chat/Component;Lnet/minecraft/network/chat/MessageSignature;Lnet/minecraft/client/GuiMessageTag;)V",
            at = @At("HEAD"), cancellable = true)
    private void onAddMessageFull(Component message, MessageSignature signature,
                                   GuiMessageTag tag, CallbackInfo ci) {
        if (ChatBubbleConfig.ENABLED.get()) {
            ChatMessageStore.addMessage(message, new UUID(0, 0),
                Component.translatable("e33chat.sender.system"), true);
            ci.cancel();
        }
    }
}
