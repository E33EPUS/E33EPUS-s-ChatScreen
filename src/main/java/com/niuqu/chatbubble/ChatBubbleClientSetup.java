package com.niuqu.chatbubble;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.ConfigScreenHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@OnlyIn(Dist.CLIENT)
public class ChatBubbleClientSetup {
    public static void init() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT,
            ChatBubbleConfig.CLIENT_CONFIG, "e33chat-client.toml");
        ModLoadingContext.get().registerExtensionPoint(
            ConfigScreenHandler.ConfigScreenFactory.class,
            () -> new ConfigScreenHandler.ConfigScreenFactory(
                (mc, screen) -> new ChatBubbleConfigScreen(screen)));
        FMLJavaModLoadingContext.get().getModEventBus().addListener(RoundRectRenderer::registerShaders);
        MinecraftForge.EVENT_BUS.register(new ChatBubbleClientListener());
    }
}
