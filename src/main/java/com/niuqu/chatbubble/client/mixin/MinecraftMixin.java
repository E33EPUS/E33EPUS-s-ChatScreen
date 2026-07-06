package com.niuqu.chatbubble.client.mixin;

import com.niuqu.chatbubble.ChatBubbleScreen;
import com.niuqu.chatbubble.E33ChatConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public class MinecraftMixin {

    @Inject(method = "setScreen", at = @At("HEAD"), cancellable = true)
    private void onSetScreen(Screen screen, CallbackInfo ci) {
        if (!E33ChatConfig.enabled) return;
        if (screen instanceof ChatScreen chatScreen) {
            ci.cancel();
            String initial = "";
            try {
                var f = ChatScreen.class.getDeclaredField("initial");
                f.setAccessible(true);
                Object val = f.get(chatScreen);
                if (val != null) initial = (String) val;
            } catch (Exception ignored) {}
            Minecraft.getInstance().setScreen(new ChatBubbleScreen(initial));
        }
    }
}
