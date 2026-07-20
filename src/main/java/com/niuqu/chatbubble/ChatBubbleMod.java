package com.niuqu.chatbubble;

import com.mojang.logging.LogUtils;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.common.NeoForge;
import org.slf4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Mod(ChatBubbleMod.MODID)
public class ChatBubbleMod {
    public static final String MODID = "e33chat";
    public static final Logger LOGGER = LogUtils.getLogger();

    public ChatBubbleMod(IEventBus modEventBus, ModContainer modContainer, Dist dist) {
        modEventBus.register(NetworkHandler.class);

        if (dist == Dist.CLIENT) {
            migrateClientConfig();
            modContainer.registerConfig(net.neoforged.fml.config.ModConfig.Type.CLIENT,
                ChatBubbleConfig.CLIENT_CONFIG, "e33chat-client.toml");
        }

        modContainer.registerConfig(net.neoforged.fml.config.ModConfig.Type.SERVER,
            ChatServerConfig.SERVER_CONFIG, "e33chat-server.toml");

        NeoForge.EVENT_BUS.register(new ChatServerListener());
    }

    private static void migrateClientConfig() {
        Path configDir = FMLPaths.CONFIGDIR.get();
        Path oldFile = configDir.resolve("chatbubble-client.toml");
        Path newFile = configDir.resolve("e33chat-client.toml");
        if (Files.exists(oldFile) && !Files.exists(newFile)) {
            try {
                Files.copy(oldFile, newFile);
                Files.delete(oldFile);
                LOGGER.info("[e33chat] Migrated config to e33chat-client.toml");
            } catch (IOException ignored) {}
        }
    }
}
