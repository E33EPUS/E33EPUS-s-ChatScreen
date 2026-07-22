package com.niuqu.chatbubble;

import com.niuqu.chatbubble.config.ChatBubbleConfig;
import com.niuqu.chatbubble.config.ConfigManager;
import com.niuqu.chatbubble.network.ChatMetaPayload;
import com.niuqu.chatbubble.network.HistoryPayload;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.client.MinecraftClient;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;

import java.nio.file.Path;

public class ChatBubbleClientSetup implements ClientModInitializer {
    private static ChatBubbleConfig config;
    private static Path configPath;
    private static boolean leftWasDown;
    private static ClientLifecycleState lifecycle;

    public static ChatBubbleConfig config() { return config; }
    public static ClientLifecycleState lifecycle() { return lifecycle; }

    public static void saveConfig(ChatBubbleConfig newConfig) {
        config = newConfig;
        ConfigManager.save(configPath, config);
    }

    @Override
    public void onInitializeClient() {
        configPath = MinecraftClient.getInstance().runDirectory.toPath().resolve("config/e33chat.json");
        config = ConfigManager.load(configPath);
        lifecycle = new ClientLifecycleState();

        ClientPlayNetworking.registerGlobalReceiver(ChatMetaPayload.ID, (payload, context) -> {
            context.client().execute(() -> lifecycle.receiveMetadata(
                payload.senderUUID(), payload.messageHash(),
                payload.quoteSender(), payload.quoteContent(), payload.mentionTargets()));
        });
        ClientPlayNetworking.registerGlobalReceiver(HistoryPayload.ID, (payload, context) -> {
            context.client().execute(() -> ChatMessageStore.addHistoryMessages(payload.entries()));
        });

        HudRenderCallback.EVENT.register((drawContext, tickDelta) -> {
            if (!config.enabled()) return;
            ChatBubbleHudOverlay.render(drawContext);
        });

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (!config.enabled()) return;

            String conn;
            if (client.world == null || client.player == null) {
                conn = null;
            } else if (client.getServer() != null) {
                conn = "SP:" + client.getServer().getSaveProperties().getLevelName();
            } else if (client.getCurrentServerEntry() != null) {
                conn = "MP:" + client.getCurrentServerEntry().name;
            } else {
                conn = "world";
            }
            String dim = client.world != null ? client.world.getRegistryKey().getValue().toString() : "unknown";
            lifecycle.setCurrentWorld(conn, dim);
            ChatMessageStore.tickPreview();
            ChatMessageStore.tickStrongHint();

            if (client.currentScreen == null) {
                boolean leftDown = org.lwjgl.glfw.GLFW.glfwGetMouseButton(
                    client.getWindow().getHandle(), org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_1) == org.lwjgl.glfw.GLFW.GLFW_PRESS;
                if (leftDown && !leftWasDown) {
                    double mx = client.mouse.getX() * (double)client.getWindow().getScaledWidth() / (double)client.getWindow().getWidth();
                    double my = client.mouse.getY() * (double)client.getWindow().getScaledHeight() / (double)client.getWindow().getHeight();
                    if (ChatBubbleHudOverlay.isMouseOverIcon(mx, my)) {
                        client.setScreen(new ChatBubbleScreen(""));
                    }
                }
                leftWasDown = leftDown;
            } else {
                leftWasDown = false;
            }
        });

        ScreenEvents.BEFORE_INIT.register((client, screen, width, height) ->
            ScreenEvents.afterRender(screen).register((scr, g, mouseX, mouseY, delta) -> {
                if (config.enabled()) ChatBubbleHudOverlay.renderStrongHint(g);
            })
        );

        ResourceManagerHelper.get(ResourceType.CLIENT_RESOURCES).registerReloadListener(
            new SimpleSynchronousResourceReloadListener() {
                @Override
                public Identifier getFabricId() {
                    return Identifier.of(ChatBubbleMod.MOD_ID, "shader_reload");
                }
                @Override
                public void reload(ResourceManager manager) {
                    RoundRectRenderer.resetShader();
                }
            }
        );
    }
}
