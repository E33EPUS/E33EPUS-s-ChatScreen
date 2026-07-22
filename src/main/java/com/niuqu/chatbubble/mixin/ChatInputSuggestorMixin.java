package com.niuqu.chatbubble.mixin;

import com.niuqu.chatbubble.ChatBubbleScreen;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ChatInputSuggestor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = ChatInputSuggestor.class, priority = 500)
public class ChatInputSuggestorMixin {

    @Shadow private int x;

    @Inject(method = "renderMessages", at = @At("HEAD"), cancellable = true, require = 0)
    private void onRenderMessages(DrawContext context, CallbackInfo ci) {
        if (MinecraftClient.getInstance().currentScreen instanceof ChatBubbleScreen) {
            ci.cancel();
        }
    }

    @Inject(method = "showCommandSuggestions", at = @At("HEAD"), require = 0)
    private void onShowSuggestions(CallbackInfo ci) {
        if (MinecraftClient.getInstance().currentScreen instanceof ChatBubbleScreen) {
            this.x = ChatBubbleScreen.getInputX();
        }
    }
}
