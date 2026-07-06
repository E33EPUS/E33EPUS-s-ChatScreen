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

@Mixin(value = ChatComponent.class, priority = 500)
public class ChatComponentMixin {

    private static boolean loggedRender, loggedAddMsg, loggedAddMsgFull;

    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    private void onRender(GuiGraphics guiGraphics, int tickCount, int mouseX,
                          int mouseY, CallbackInfo ci) {
        if (!loggedRender) {
            System.out.println("[e33chat] ChatComponent.render 拦截生效");
            loggedRender = true;
        }
        if (ChatBubbleConfig.ENABLED.get()) {
            ci.cancel();
        }
    }

    @Inject(method = "addMessage(Lnet/minecraft/network/chat/Component;)V",
            at = @At("HEAD"), cancellable = true)
    private void onAddMessage(Component message, CallbackInfo ci) {
        if (!loggedAddMsg) {
            System.out.println("[e33chat] ChatComponent.addMessage 拦截生效");
            loggedAddMsg = true;
        }
        if (ChatBubbleConfig.ENABLED.get()) {
            ci.cancel();
        }
    }

    @Inject(method = "addMessage(Lnet/minecraft/network/chat/Component;Lnet/minecraft/network/chat/MessageSignature;Lnet/minecraft/client/GuiMessageTag;)V",
            at = @At("HEAD"), cancellable = true)
    private void onAddMessageFull(Component message, MessageSignature signature,
                                   GuiMessageTag tag, CallbackInfo ci) {
        if (!loggedAddMsgFull) {
            System.out.println("[e33chat] ChatComponent.addMessage(3参) 拦截生效");
            loggedAddMsgFull = true;
        }
        if (ChatBubbleConfig.ENABLED.get()) {
            ci.cancel();
        }
    }
}
