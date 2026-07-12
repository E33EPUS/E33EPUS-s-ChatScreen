package com.niuqu.chatbubble;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.common.NeoForge;

@Mod(value = ChatBubbleMod.MODID, dist = Dist.CLIENT)
public class ChatBubbleClientSetup {
    public ChatBubbleClientSetup(ModContainer container, IEventBus modEventBus) {
        container.registerExtensionPoint(IConfigScreenFactory.class,
            (modContainer, screen) -> new ChatBubbleConfigScreen(screen));
        NeoForge.EVENT_BUS.register(new ChatBubbleClientListener());

        modEventBus.addListener(this::onClientSetup);
    }

    private void onClientSetup(FMLClientSetupEvent event) {
        ChatBubbleMod.LOGGER.info("E33Chat client setup done");
    }
}
