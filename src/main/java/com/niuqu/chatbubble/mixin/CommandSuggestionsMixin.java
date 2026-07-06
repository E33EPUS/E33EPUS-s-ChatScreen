package com.niuqu.chatbubble.mixin;

import com.niuqu.chatbubble.ChatBubbleScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.CommandSuggestions;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CommandSuggestions.class)
public class CommandSuggestionsMixin {

    @Inject(method = "renderUsage", at = @At("HEAD"), cancellable = true)
    private void onRenderUsage(GuiGraphics g, CallbackInfo ci) {
        if (Minecraft.getInstance().screen instanceof ChatBubbleScreen) {
            ci.cancel();
        }
    }

    @ModifyArg(method = "showSuggestions",
        at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/gui/components/CommandSuggestions$SuggestionsList;<init>(Lnet/minecraft/client/gui/components/CommandSuggestions;IIILjava/util/List;Z)V"),
        index = 2)
    private int fixSuggestionsY(int y) {
        return ChatBubbleScreen.getInputY() - 3;
    }
}
