package com.niuqu.chatbubble;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.ConfigScreenHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(ChatBubbleMod.MODID)
public class ChatBubbleMod {
    public static final String MODID = "e33chat";

    public ChatBubbleMod() {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(
            (FMLCommonSetupEvent event) -> event.enqueueWork(NetworkHandler::register));

        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
            ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT,
                ChatBubbleConfig.CLIENT_CONFIG, "chatbubble-client.toml");
            ModLoadingContext.get().registerExtensionPoint(
                ConfigScreenHandler.ConfigScreenFactory.class,
                () -> new ConfigScreenHandler.ConfigScreenFactory(
                    (mc, screen) -> new ChatBubbleConfigScreen(screen)));
            MinecraftForge.EVENT_BUS.register(new ChatBubbleClientListener());
        });

        MinecraftForge.EVENT_BUS.register(new ChatServerListener());
    }
}
