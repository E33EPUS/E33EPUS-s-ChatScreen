package com.niuqu.chatbubble;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(ChatBubbleMod.MODID)
public class ChatBubbleMod {
    public static final String MODID = "e33chat";

    public ChatBubbleMod() {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(
            (FMLCommonSetupEvent event) -> event.enqueueWork(NetworkHandler::register));

        DistExecutor.safeRunWhenOn(Dist.CLIENT, () -> ChatBubbleClientSetup::init);

        MinecraftForge.EVENT_BUS.register(new ChatServerListener());
    }
}
