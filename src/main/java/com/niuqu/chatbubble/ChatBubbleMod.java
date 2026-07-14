package com.niuqu.chatbubble;

import com.mojang.logging.LogUtils;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import org.slf4j.Logger;

@Mod(ChatBubbleMod.MODID)
public class ChatBubbleMod {
    public static final String MODID = "e33chat";
    public static final Logger LOGGER = LogUtils.getLogger();

    public ChatBubbleMod(IEventBus modEventBus, ModContainer modContainer, Dist dist) {
        modEventBus.register(NetworkHandler.class);

        if (dist == Dist.CLIENT) {
            modContainer.registerConfig(net.neoforged.fml.config.ModConfig.Type.CLIENT,
                ChatBubbleConfig.CLIENT_CONFIG, "chatbubble-client.toml");
        }

        NeoForge.EVENT_BUS.register(new ChatServerListener());
    }
}
