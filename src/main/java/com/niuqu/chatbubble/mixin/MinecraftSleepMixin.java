package com.niuqu.chatbubble.mixin;

import com.niuqu.chatbubble.ChatBubbleClientListener;
import com.niuqu.chatbubble.ChatBubbleConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = Minecraft.class, priority = 500)
public class MinecraftSleepMixin {

    @Inject(method = "setScreen", at = @At("HEAD"), cancellable = true)
    private void onSetScreen(Screen screen, CallbackInfo ci) {
        if (!ChatBubbleConfig.ENABLED.get()) return;
        Minecraft self = (Minecraft) (Object) this;
        if (screen instanceof ChatScreen
            && self.player != null
            && self.player.isSleeping()
            && self.screen == null
            && !ChatBubbleClientListener.isManualChatOpen()) {
            ci.cancel();
        }
    }
}
