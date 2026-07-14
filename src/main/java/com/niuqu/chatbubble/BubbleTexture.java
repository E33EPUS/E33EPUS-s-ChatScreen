package com.niuqu.chatbubble;

import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.LinkedHashMap;
import java.util.Map;

public class BubbleTexture {

    private static final int SS = 3;
    private static final float CORNER_RADIUS = 16f;
    private static final float SOFTNESS = 2.0f;
    private static final int MAX_CACHE = 128;

    private static final Map<String, ResourceLocation> cache = new LinkedHashMap<>() {
        @Override
        protected boolean removeEldestEntry(Map.Entry<String, ResourceLocation> eldest) {
            return size() > MAX_CACHE;
        }
    };

    public static ResourceLocation get(int bgColor, int w, int h) {
        if (w <= 0 || h <= 0) return null;
        String key = Integer.toHexString(bgColor) + ":" + w + ":" + h;
        return cache.computeIfAbsent(key, k -> generate(bgColor, w, h));
    }

    private static ResourceLocation generate(int bgColor, int w, int h) {
        int a = (bgColor >> 24) & 0xFF;
        int r = (bgColor >> 16) & 0xFF;
        int g = (bgColor >> 8) & 0xFF;
        int b = bgColor & 0xFF;

        int tw = w * SS;
        int th = h * SS;
        float r2 = Math.min(CORNER_RADIUS * SS, Math.min(tw, th) / 2f);
        float soft = SOFTNESS * SS;
        float hw = tw / 2f;
        float hh = th / 2f;
        float innerW = hw - r2;
        float innerH = hh - r2;

        // SDF at supersampled resolution
        BufferedImage hi = new BufferedImage(tw, th, BufferedImage.TYPE_INT_ARGB);
        for (int y = 0; y < th; y++) {
            for (int x = 0; x < tw; x++) {
                float dx = Math.abs(x + 0.5f - hw) - innerW;
                float dy = Math.abs(y + 0.5f - hh) - innerH;
                float dist = (float)(len(Math.max(dx, 0), Math.max(dy, 0))
                    + Math.min(Math.max(dx, dy), 0) - r2);
                int pa = clamp((int)(a * smoothstep(-soft, soft, -dist) + 0.5f));
                hi.setRGB(x, y, (pa << 24) | (r << 16) | (g << 8) | b);
            }
        }

        // Java2D bicubic downscale
        BufferedImage lo = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D gs = lo.createGraphics();
        gs.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        gs.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        gs.drawImage(hi, 0, 0, w, h, null);
        gs.dispose();

        NativeImage ni = new NativeImage(NativeImage.Format.RGBA, w, h, false);
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int argb = lo.getRGB(x, y);
                int pa = (argb >> 24) & 0xFF;
                int pr = (argb >> 16) & 0xFF;
                int pg = (argb >> 8) & 0xFF;
                int pb = argb & 0xFF;
                ni.setPixelRGBA(x, y, (pa << 24) | (pb << 16) | (pg << 8) | pr);
            }
        }

        DynamicTexture dt = new DynamicTexture(ni);
        ResourceLocation loc = ResourceLocation.fromNamespaceAndPath("e33chat",
            "bubble_" + Integer.toHexString((bgColor * 31 + w * 17 + h) & 0x7FFFFFFF));
        Minecraft.getInstance().getTextureManager().register(loc, dt);
        return loc;
    }

    private static float len(float x, float y) {
        return (float)Math.sqrt(x * x + y * y);
    }

    private static float smoothstep(float edge0, float edge1, float x) {
        float t = clamp((x - edge0) / (edge1 - edge0));
        return t * t * (3f - 2f * t);
    }

    private static float clamp(float v) {
        return v < 0f ? 0f : (v > 1f ? 1f : v);
    }

    private static int clamp(int v) {
        return v < 0 ? 0 : (v > 255 ? 255 : v);
    }
}
