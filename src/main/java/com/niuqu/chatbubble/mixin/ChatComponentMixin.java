package com.niuqu.chatbubble.mixin;

import com.niuqu.chatbubble.ChatMessageStore;
import com.niuqu.chatbubble.ChatMessageStore.SenderMeta;
import com.niuqu.chatbubble.E33ChatConfig;
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
        if (E33ChatConfig.enabled) {
            ci.cancel();
        }
    }

    @Inject(method = "addMessage(Lnet/minecraft/network/chat/Component;)V",
            at = @At("HEAD"))
    private void onAddMessage(Component message, CallbackInfo ci) {
        captureMessage(message);
    }

    @Inject(method = "addMessage(Lnet/minecraft/network/chat/Component;Lnet/minecraft/network/chat/MessageSignature;Lnet/minecraft/client/GuiMessageTag;)V",
            at = @At("HEAD"))
    private void onAddMessageFull(Component message, MessageSignature signature,
                                   GuiMessageTag tag, CallbackInfo ci) {
        captureMessage(message);
    }

    private void captureMessage(Component finalComponent) {
        if (!E33ChatConfig.enabled) return;

        SenderMeta meta = ChatMessageStore.consumePendingMeta();
        if (meta == null) {
            if (ChatMessageStore.isRecentDuplicate(finalComponent.getString())) return;
            meta = new SenderMeta(
                new UUID(0, 0),
                Component.translatable("e33chat.sender.system"),
                finalComponent,
                true
            );
        }

        String coreHash = String.valueOf(meta.rawContent().getString().hashCode());
        if (ChatMessageStore.consumeEcho(coreHash)) return;

        String rawStr = meta.rawContent().getString();
        String finalStr = finalComponent.getString();
        Component content;
        if (finalStr.contains(rawStr)) {
            content = meta.rawContent();
        } else {
            content = finalComponent;
        }

        ChatMessageStore.addMessage(content, meta.senderUUID(), meta.senderName(), meta.isSystem());
    }
}
