package com.niuqu.chatbubble;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.ConfigScreenHandler;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RenderGuiEvent;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.loading.FMLEnvironment;

@Mod(ChatBubbleMod.MODID)
public class ChatBubbleMod {
    public static final String MODID = "e33chat";

    public ChatBubbleMod() {
        if (FMLEnvironment.dist != Dist.CLIENT) return;

        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT,
            ChatBubbleConfig.CLIENT_CONFIG, "chatbubble-client.toml");
        ModLoadingContext.get().registerExtensionPoint(
            ConfigScreenHandler.ConfigScreenFactory.class,
            () -> new ConfigScreenHandler.ConfigScreenFactory(
                (mc, screen) -> new ChatBubbleConfigScreen(screen)));

        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onScreenOpen(ScreenEvent.Opening event) {
        if (!ChatBubbleConfig.ENABLED.get()) return;
        if (event.getScreen() instanceof ChatScreen chatScreen) {
            event.setCanceled(true);
            String initial = getChatInitialText(chatScreen);
            Minecraft.getInstance().setScreen(new ChatBubbleScreen(initial));
        }
    }

    private static String getChatInitialText(ChatScreen chatScreen) {
        try {
            var f = ChatScreen.class.getDeclaredField("initial");
            f.setAccessible(true);
            String val = (String) f.get(chatScreen);
            System.out.println("[e33chat] initial='" + val + "'");
            return val != null ? val : "";
        } catch (Exception e) {
            System.out.println("[e33chat] 'initial' field not found, scanning: " + e);
        }
        for (var f : ChatScreen.class.getDeclaredFields()) {
            if (f.getType() == String.class) {
                f.setAccessible(true);
                try {
                    String val = (String) f.get(chatScreen);
                    System.out.println("[e33chat] String field '" + f.getName() + "' = '" + val + "'");
                    if (val != null && !val.isEmpty()) return val;
                } catch (Exception ignored) {}
            }
        }
        return "";
    }

    @SubscribeEvent
    public void onRenderGui(RenderGuiEvent.Post event) {
        if (!ChatBubbleConfig.ENABLED.get()) return;
        ChatBubbleHudOverlay.render(event.getGuiGraphics());
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        Minecraft mc = Minecraft.getInstance();
        String key;
        if (mc.level == null || mc.player == null) {
            key = null;
        } else if (mc.getSingleplayerServer() != null) {
            key = "SP:" + mc.getSingleplayerServer().getWorldData().getLevelName();
        } else if (mc.getCurrentServer() != null) {
            key = "MP:" + mc.getCurrentServer().name;
        } else {
            key = "world";
        }
        ChatMessageStore.setCurrentWorld(key);
        ChatMessageStore.tickPreview();
        ChatMessageStore.tickStrongHint();
    }

    @SubscribeEvent
    public void onMouseClick(InputEvent.MouseButton.Pre event) {
        if (!ChatBubbleConfig.ENABLED.get()) return;
        if (event.getButton() != 0) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.screen != null) return;
        double mx = mc.mouseHandler.xpos() * (double) mc.getWindow().getGuiScaledWidth() / (double) mc.getWindow().getScreenWidth();
        double my = mc.mouseHandler.ypos() * (double) mc.getWindow().getGuiScaledHeight() / (double) mc.getWindow().getScreenHeight();
        if (ChatBubbleHudOverlay.isMouseOverIcon(mx, my)) {
            event.setCanceled(true);
            mc.setScreen(new ChatBubbleScreen(""));
        }
    }
}
