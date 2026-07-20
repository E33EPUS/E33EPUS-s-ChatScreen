package com.niuqu.chatbubble;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.ConfigScreenHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLPaths;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@OnlyIn(Dist.CLIENT)
public class ChatBubbleClientSetup {
    public static void init() {
        migrateConfig();
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT,
            ChatBubbleConfig.CLIENT_CONFIG, "e33chat-client.toml");
        ModLoadingContext.get().registerExtensionPoint(
            ConfigScreenHandler.ConfigScreenFactory.class,
            () -> new ConfigScreenHandler.ConfigScreenFactory(
                (mc, screen) -> new ChatBubbleConfigScreen(screen)));
        FMLJavaModLoadingContext.get().getModEventBus().addListener(RoundRectRenderer::registerShaders);
        MinecraftForge.EVENT_BUS.register(new ChatBubbleClientListener());
    }

    private static void migrateConfig() {
        Path configDir = FMLPaths.CONFIGDIR.get();
        Path oldFile = configDir.resolve("chatbubble-client.toml");
        Path newFile = configDir.resolve("e33chat-client.toml");
        if (Files.exists(oldFile) && !Files.exists(newFile)) {
            try {
                Files.copy(oldFile, newFile);
                Files.delete(oldFile);
            } catch (IOException ignored) {}
        }
    }
}
