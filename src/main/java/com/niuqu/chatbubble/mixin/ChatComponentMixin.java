package com.niuqu.chatbubble.mixin;

import com.niuqu.chatbubble.ChatBubbleConfig;
import net.minecraft.client.GuiMessageTag;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MessageSignature;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ChatComponent.class)
public class ChatComponentMixin {

    @Inject(method = "render", at = @At("HEAD"), cancellable = true, require = 0)
    private void onRender(GuiGraphics guiGraphics, int tickCount, int mouseX,
                          int mouseY, CallbackInfo ci) {
        if (ChatBubbleConfig.ENABLED.get()) {
            ci.cancel();
        }
    }

    @Inject(method = "addMessage(Lnet/minecraft/network/chat/Component;Lnet/minecraft/network/chat/MessageSignature;ILnet/minecraft/client/GuiMessageTag;Z)V",
            at = @At("HEAD"), cancellable = true, require = 0)
    private void onAddMessage(Component message, MessageSignature signature, int ticks,
                              GuiMessageTag tag, boolean refresh, CallbackInfo ci) {
        if (ChatBubbleConfig.ENABLED.get()) {
            ci.cancel();
        }
    }
}
