package com.niuqu.chatbubble.client.mixin;

import com.niuqu.chatbubble.ChatBubbleHudOverlay;
import com.niuqu.chatbubble.ChatBubbleScreen;
import com.niuqu.chatbubble.E33ChatConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MouseHandler.class)
public class MouseHandlerMixin {

    @Inject(method = "onPress", at = @At("HEAD"), cancellable = true)
    private void onMousePress(long window, int button, int action, int mods, CallbackInfo ci) {
        if (!E33ChatConfig.enabled || button != 0 || action != 1) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.screen != null) return;
        double mx = mc.mouseHandler.xpos() * (double) mc.getWindow().getGuiScaledWidth()
            / (double) mc.getWindow().getScreenWidth();
        double my = mc.mouseHandler.ypos() * (double) mc.getWindow().getGuiScaledHeight()
            / (double) mc.getWindow().getScreenHeight();
        if (ChatBubbleHudOverlay.isMouseOverIcon(mx, my)) {
            ci.cancel();
            mc.setScreen(new ChatBubbleScreen(""));
        }
    }
}
