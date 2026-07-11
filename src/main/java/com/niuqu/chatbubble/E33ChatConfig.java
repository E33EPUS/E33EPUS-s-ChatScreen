package com.niuqu.chatbubble;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraft.client.Minecraft;

import java.io.*;
import java.nio.charset.StandardCharsets;

public class E33ChatConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public static boolean enabled = true;
    public static boolean redDot = true;
    public static boolean animation = true;
    public static boolean strongHint = true;
    public static boolean mentionStrongHint = true;
    public static boolean previewEnabled = true;
    public static int previewLines = 2;
    public static int previewWidth = 150;
    public static String ownBubbleColor = "#95EC69";
    public static String otherBubbleColor = "#4A4A4A";
    public static String ownTextColor = "#0A0A0A";
    public static String otherTextColor = "#FFFFFF";

    private static boolean loaded;

    public static void load() {
        if (loaded) return;
        loaded = true;
        File f = getConfigFile();
        if (!f.exists()) return;
        try (Reader r = new InputStreamReader(new FileInputStream(f), StandardCharsets.UTF_8)) {
            ConfigData data = GSON.fromJson(r, ConfigData.class);
            if (data != null) {
                enabled = data.enabled;
                redDot = data.redDot;
                animation = data.animation;
                if (data.strongHint != null) strongHint = data.strongHint;
                if (data.mentionStrongHint != null) mentionStrongHint = data.mentionStrongHint;
                if (data.previewEnabled != null) previewEnabled = data.previewEnabled;
                if (data.previewLines > 0) previewLines = data.previewLines;
                if (data.previewWidth > 0) previewWidth = data.previewWidth;
                if (data.ownBubbleColor != null) ownBubbleColor = data.ownBubbleColor;
                if (data.otherBubbleColor != null) otherBubbleColor = data.otherBubbleColor;
                if (data.ownTextColor != null) ownTextColor = data.ownTextColor;
                if (data.otherTextColor != null) otherTextColor = data.otherTextColor;
            }
        } catch (Exception ignored) {}
    }

    public static void save() {
        File f = getConfigFile();
        f.getParentFile().mkdirs();
        ConfigData data = new ConfigData();
        data.enabled = enabled;
        data.redDot = redDot;
        data.animation = animation;
        data.strongHint = strongHint;
        data.mentionStrongHint = mentionStrongHint;
        data.previewEnabled = previewEnabled;
        data.previewLines = previewLines;
        data.previewWidth = previewWidth;
        data.ownBubbleColor = ownBubbleColor;
        data.otherBubbleColor = otherBubbleColor;
        data.ownTextColor = ownTextColor;
        data.otherTextColor = otherTextColor;
        try (Writer w = new OutputStreamWriter(new FileOutputStream(f), StandardCharsets.UTF_8)) {
            GSON.toJson(data, w);
        } catch (Exception ignored) {}
    }

    private static File getConfigFile() {
        return new File(Minecraft.getInstance().gameDirectory, "config/e33chat.json");
    }

    public static int parseHexColor(String hex, int defaultColor) {
        try {
            String h = hex.replace("#", "").trim();
            if (h.length() != 6) return defaultColor;
            return 0xFF000000 | Integer.parseInt(h, 16);
        } catch (NumberFormatException e) {
            return defaultColor;
        }
    }

    private static class ConfigData {
        boolean enabled = true;
        boolean redDot = true;
        boolean animation = true;
        Boolean strongHint;
        Boolean mentionStrongHint;
        Boolean previewEnabled;
        int previewLines;
        int previewWidth;
        String ownBubbleColor = "#95EC69";
        String otherBubbleColor = "#4A4A4A";
        String ownTextColor = "#0A0A0A";
        String otherTextColor = "#FFFFFF";
    }
}
