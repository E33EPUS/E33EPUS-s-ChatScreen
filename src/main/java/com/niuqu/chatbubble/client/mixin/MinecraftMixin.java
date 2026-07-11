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
            Minecraft.getInstance().setScreen(new ChatBubbleScreen(getChatInitialText(chatScreen)));
        }
    }

    private static String getChatInitialText(ChatScreen chatScreen) {
        try {
            var f = ChatScreen.class.getDeclaredField("initial");
            f.setAccessible(true);
            String val = (String) f.get(chatScreen);
            return val != null ? val : "";
        } catch (Exception ignored) {}
        for (var f : ChatScreen.class.getDeclaredFields()) {
            if (f.getType() == String.class) {
                f.setAccessible(true);
                try {
                    String val = (String) f.get(chatScreen);
                    if (val != null && !val.isEmpty()) return val;
                } catch (Exception ignored) {}
            }
        }
        return "";
    }
}
