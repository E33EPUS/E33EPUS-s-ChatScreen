package com.niuqu.chatbubble.mixin;

import com.mojang.authlib.GameProfile;
import com.niuqu.chatbubble.*;
import com.niuqu.chatbubble.chat.MessagePresentation;
import com.niuqu.chatbubble.config.ChatBubbleConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.network.message.MessageType;
import net.minecraft.network.message.SignedMessage;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableTextContent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.*;

@Mixin(value = net.minecraft.client.network.message.MessageHandler.class, priority = 500)
public class ChatListenerMixin {

    private static Text extractDecoratedName(Text fullLine, String contentStr,
                                              String rawName, Text fallback) {
        if (contentStr == null || contentStr.isEmpty()) return fallback;
        String fullStr = fullLine.getString();
        int idx = fullStr.lastIndexOf(contentStr);
        if (idx <= 0) return fallback;
        return cleanNameArea(fullLine, 0, idx, rawName, fallback);
    }

    private static Text cleanNameArea(Text fullLine, int a, int b,
                                       String rawName, Text fallback) {
        String fullStr = fullLine.getString();
        while (a < b && Character.isWhitespace(fullStr.charAt(a))) a++;
        while (b > a) {
            char ch = fullStr.charAt(b - 1);
            if (Character.isWhitespace(ch) || ch == ':' || ch == '：' || ch == '»') b--;
            else if (ch == '>' && b >= a + 2 && fullStr.charAt(b - 2) == '>') b -= 2;
            else break;
        }
        if (a >= b) return fallback;
        Text nameArea = ChatMessageStore.sliceStyled(fullLine, a, b);
        String ns = nameArea.getString();
        if (rawName != null && !rawName.isEmpty()) {
            String bracketed = "<" + rawName + ">";
            int p = ns.indexOf(bracketed);
            if (p >= 0) {
                var out = Text.empty();
                if (p > 0) out.append(ChatMessageStore.sliceStyled(nameArea, 0, p));
                out.append(ChatMessageStore.sliceStyled(nameArea, p + 1, p + 1 + rawName.length()));
                int tail = p + bracketed.length();
                if (tail < ns.length()) out.append(ChatMessageStore.sliceStyled(nameArea, tail, ns.length()));
                return out;
            }
            if (ns.length() > 2 && ns.charAt(0) == '<' && ns.charAt(ns.length() - 1) == '>')
                return ChatMessageStore.sliceStyled(nameArea, 1, ns.length() - 1);
        }
        return nameArea;
    }

    private static String[] nameCandidates(PlayerListEntry info) {
        var out = new LinkedHashSet<String>();
        String profile = info.getProfile().getName();
        addNameVariants(out, profile);
        var tab = info.getDisplayName();
        if (tab != null) addNameVariants(out, tab.getString().trim());
        return out.toArray(new String[0]);
    }

    private static void addNameVariants(Set<String> out, String name) {
        if (name == null || name.isEmpty()) return;
        out.add(name);
        String stripped = name.replaceAll("§.", "");
        if (!stripped.isEmpty()) out.add(stripped);
    }

    private static boolean isVanillaBroadcast(Text message) {
        if (message.getContent() instanceof TranslatableTextContent tc)
            return MessagePipelineRules.isVanillaBroadcast(tc.getKey());
        return false;
    }

    private static Text argAsComponent(Object arg) {
        return arg instanceof Text c ? c : Text.literal(String.valueOf(arg));
    }

    private static PlayerListEntry findOnlinePlayer(String displayName) {
        var player = MinecraftClient.getInstance().player;
        if (player == null || player.networkHandler == null || displayName.isEmpty()) return null;
        var online = player.networkHandler.getPlayerList();
        for (var info : online) {
            for (String cand : nameCandidates(info))
                if (cand.equals(displayName)) return info;
        }
        PlayerListEntry best = null;
        int bestLen = 0;
        for (var info : online) {
            for (String cand : nameCandidates(info)) {
                if (cand.length() >= 3 && cand.length() > bestLen && displayName.contains(cand)) {
                    best = info;
                    bestLen = cand.length();
                }
            }
        }
        return best;
    }

    private static boolean classifyByKey(Text message) {
        if (!(message.getContent() instanceof TranslatableTextContent tc)) return false;
        String key = tc.getKey();
        Object[] args = tc.getArgs();
        var cfg = ChatBubbleClientSetup.config();
        boolean ncr = cfg != null && cfg.chatReportCompat();

        if (key.equals("commands.message.display.incoming") && args.length >= 2) {
            Text name = argAsComponent(args[0]);
            Text content = argAsComponent(args[1]);
            String displayName = name.getString().replaceAll("§.", "").trim();
            var info = findOnlinePlayer(displayName);
            String profile = info != null ? info.getProfile().getName() : displayName;
            UUID uuid = info != null ? info.getProfile().getId() : new UUID(0, 0);
            ChatMessageStore.debugLog("[e33chat] Key(whisper in) | name=" + profile + " | content='" + content.getString() + "'");
            ChatMessageStore.setPendingMeta(new ChatMessageStore.SenderMeta(uuid, name, content, false, profile, true, profile));
            return true;
        }

        if (key.equals("commands.message.display.outgoing")) {
            if (ChatMessageStore.hasPendingWhisperEcho()) {
                ChatMessageStore.consumeWhisperEcho();
                ChatMessageStore.markSuppressCapture();
                ChatMessageStore.debugLog("[e33chat] Key(whisper echo suppressed)");
                return true;
            }
            var player = MinecraftClient.getInstance().player;
            if (player != null && args.length >= 2) {
                String partner = argAsComponent(args[0]).getString().replaceAll("§.", "").trim();
                Text content = argAsComponent(args[1]);
                String own = player.getName().getString();
                ChatMessageStore.debugLog("[e33chat] Key(whisper out) | partner=" + partner + " | content='" + content.getString() + "'");
                ChatMessageStore.setPendingMeta(new ChatMessageStore.SenderMeta(player.getUuid(),
                    Text.literal(own), content, false, own, true, partner));
                return true;
            }
            return false;
        }

        if (ncr && key.equals("chat.type.text") && args.length >= 2) {
            Text name = argAsComponent(args[0]);
            Text content = argAsComponent(args[1]);
            String contentStr = content.getString();
            if (MessagePipelineRules.isXaeroWaypoint(contentStr)) {
                ChatMessageStore.debugLog("[e33chat] Key(waypoint data) -> system");
                ChatMessageStore.setPendingMeta(new ChatMessageStore.SenderMeta(new UUID(0, 0),
                    Text.translatable("e33chat.sender.system"), message, true, null, false, null));
                return true;
            }
            String displayName = name.getString().replaceAll("§.", "").trim();
            var info = findOnlinePlayer(displayName);
            String profile = info != null ? info.getProfile().getName() : displayName;
            UUID uuid = info != null ? info.getProfile().getId() : new UUID(0, 0);
            ChatMessageStore.debugLog("[e33chat] Key(chat) | name=" + profile + " | display='" + name.getString() + "' | content='" + contentStr + "'");
            ChatMessageStore.setPendingMeta(new ChatMessageStore.SenderMeta(uuid, name, content, false, profile, false, null));
            return true;
        }

        if (isVanillaBroadcast(message)) {
            boolean isSystem = cfg == null || !cfg.systemChatAsBubble();
            ChatMessageStore.debugLog("[e33chat] Key(broadcast) | key=" + key);
            ChatMessageStore.setPendingMeta(new ChatMessageStore.SenderMeta(new UUID(0, 0),
                Text.translatable("e33chat.sender.system"), message, isSystem, null, false, null));
            return true;
        }

        return false;
    }

    private static ChatMessageStore.SenderMeta detectByTellClick(Text message, String text) {
        if (isVanillaBroadcast(message)) return null;
        var player = MinecraftClient.getInstance().player;
        if (player == null || player.networkHandler == null) return null;
        final int[] pos = {0};
        final int[] range = {-1, -1};
        final String[] tellName = {null};
        final String[] clickedText = {null};
        message.visit((style, str) -> {
            int s = pos[0], e = s + str.length();
            pos[0] = e;
            var click = style.getClickEvent();
            if (tellName[0] == null && click != null
                && click.getAction() == ClickEvent.Action.SUGGEST_COMMAND
                && click.getValue() != null) {
                String cmd = click.getValue();
                for (String p : new String[]{"/tell ", "/msg ", "/w ", "/whisper "}) {
                    if (cmd.startsWith(p)) {
                        String n = cmd.substring(p.length()).trim();
                        int sp = n.indexOf(' ');
                        if (sp > 0) n = n.substring(0, sp);
                        if (!n.isEmpty()) {
                            tellName[0] = n;
                            range[0] = s;
                            range[1] = e;
                            clickedText[0] = str;
                        }
                        break;
                    }
                }
            }
            return Optional.empty();
        }, Style.EMPTY);
        if (tellName[0] == null || range[0] > 32) return null;

        PlayerListEntry sender = null;
        for (var info : player.networkHandler.getPlayerList()) {
            String profile = info.getProfile().getName();
            if (profile.equals(tellName[0]) || profile.replaceAll("§.", "").equals(tellName[0])) {
                sender = info;
                break;
            }
        }
        if (sender == null) return null;

        String clicked = clickedText[0].replaceAll("§.", "").trim();
        boolean clickedIsName = false;
        for (String cand : nameCandidates(sender)) {
            if (!cand.isEmpty() && clicked.contains(cand)) { clickedIsName = true; break; }
        }
        if (!clickedIsName) return null;

        int b = range[1];
        if (b < text.length() && text.charAt(b) == '>') b++;
        int contentStart = b;
        while (contentStart < text.length()) {
            char ch = text.charAt(contentStart);
            if (Character.isWhitespace(ch) || ch == ':' || ch == '：' || ch == '»' || ch == '-') contentStart++;
            else break;
        }
        if (contentStart >= text.length()) return null;

        String profile = sender.getProfile().getName();
        Text displayName = cleanNameArea(message, 0, b, tellName[0], Text.literal(profile));
        Text content = ChatMessageStore.sliceStyled(message, contentStart, text.length());
        ChatMessageStore.debugLog("[e33chat] System(tell click) | text='" + text + "' | name=" + profile + " | display='" + displayName.getString() + "' | content='" + content.getString() + "'");
        return new ChatMessageStore.SenderMeta(sender.getProfile().getId(), displayName, content, false, profile, false, null);
    }

    private static String extractWhisperContent(String fullText, String senderName) {
        if (senderName == null || senderName.isEmpty()) return fullText;
        int idx = fullText.indexOf(senderName);
        if (idx < 0) return fullText;
        String after = fullText.substring(idx + senderName.length());
        for (String sep : new String[]{": ", "：", " :", " ："}) {
            int i = after.lastIndexOf(sep);
            if (i >= 0) return after.substring(i + sep.length());
        }
        return after.trim();
    }

    private static ChatMessageStore.SenderMeta detectWhisperInSystemMessage(String text, String logTag) {
        var player = MinecraftClient.getInstance().player;
        if (player == null || player.networkHandler == null) return null;
        for (var info : player.networkHandler.getPlayerList()) {
            String profile = info.getProfile().getName();
            for (String cand : nameCandidates(info)) {
                int idx = text.indexOf(cand);
                if (idx >= 0 && idx < 30) {
                    if (text.contains("悄悄") || text.contains("whisper") || text.contains("对你说") || text.contains("to you")
                        || text.contains("私聊") || text.contains("密语") || text.contains("密聊")) {
                        String content = extractWhisperContent(text, cand);
                        UUID senderId = info.getProfile().getId();
                        ChatMessageStore.debugLog("[e33chat] System(" + logTag + ") | text='" + text + "' | name=" + cand + " | content='" + content + "'");
                        return new ChatMessageStore.SenderMeta(senderId, Text.literal(cand),
                            Text.literal(content), false, profile, true, profile);
                    }
                }
            }
        }
        return null;
    }

    @Inject(method = "onChatMessage", at = @At("HEAD"))
    private void onPlayerChat(SignedMessage message, GameProfile gameProfile,
                               MessageType.Parameters params, CallbackInfo ci) {
        UUID senderId = gameProfile.getId();
        Text raw = message.getContent();
        String rawStr = raw.getString();
        if (MessagePipelineRules.isXaeroWaypoint(rawStr)) return;

        var cfg = ChatBubbleClientSetup.config();
        boolean ncr = cfg != null && cfg.chatReportCompat();
        boolean isWhisper = params.targetName().isPresent();
        String whisperPartner = null;
        if (isWhisper) {
            String localName = MinecraftClient.getInstance().player != null
                ? MinecraftClient.getInstance().player.getName().getString() : "";
            if (gameProfile.getName().equals(localName)) {
                whisperPartner = params.targetName().map(Text::getString).orElse(null);
            } else {
                whisperPartner = gameProfile.getName();
            }
        }

        if (ncr) {
            String name = gameProfile.getName();
            String pattern = "<" + name + "> ";
            int idx = rawStr.indexOf(pattern);
            int contentStart = idx >= 0 ? idx + pattern.length() : -1;
            int prefixEnd = idx;
            if (contentStart < 0) {
                int i2 = rawStr.indexOf(name + "> ");
                if (i2 > 0) {
                    int open = rawStr.lastIndexOf('<', i2);
                    if (open >= 0 && rawStr.indexOf('>', open) == i2 + name.length()) {
                        contentStart = i2 + name.length() + 2;
                        prefixEnd = open;
                    }
                }
            }
            if (contentStart >= 0) {
                String cleanContent = rawStr.substring(contentStart);
                Text displayName = extractDecoratedName(raw, cleanContent, name,
                    Text.literal((rawStr.substring(0, prefixEnd) + name).trim()));
                Text contentComp = ChatMessageStore.sliceStyled(raw, contentStart, rawStr.length());
                ChatMessageStore.setPendingMeta(new ChatMessageStore.SenderMeta(
                    senderId != null ? senderId : new UUID(0, 0), displayName, contentComp,
                    false, name, isWhisper, whisperPartner));
                return;
            }
        }

        Text playerContent = raw;
        Text senderName = Text.literal(gameProfile.getName());
        if (!isWhisper) {
            Text fullLine = params.applyChatDecoration(raw);
            senderName = extractDecoratedName(fullLine, rawStr, gameProfile.getName(), senderName);
        } else {
            playerContent = Text.literal(extractWhisperContent(rawStr, gameProfile.getName()));
        }
        ChatMessageStore.debugLog("[e33chat] PlayerChat | raw='" + rawStr + "' | whisper=" + isWhisper + " | partner=" + whisperPartner);
        ChatMessageStore.setPendingMeta(new ChatMessageStore.SenderMeta(
            senderId != null ? senderId : new UUID(0, 0), senderName, playerContent,
            false, gameProfile.getName(), isWhisper, whisperPartner));
    }

    @Inject(method = "onProfilelessMessage", at = @At("HEAD"))
    private void onDisguisedChat(Text content, MessageType.Parameters params, CallbackInfo ci) {
        String msgStr = content.getString();
        if (MessagePipelineRules.isXaeroWaypoint(msgStr)) return;
        boolean hasSender = params.name() != null;

        boolean isWhisper = false;
        String whisperPartner = null;
        String playerName = MinecraftClient.getInstance().player != null
            ? MinecraftClient.getInstance().player.getName().getString() : "";
        if (params.targetName().isPresent()) {
            isWhisper = true;
            if (hasSender && params.name().getString().equals(playerName)) {
                whisperPartner = params.targetName().map(Text::getString).orElse(null);
            } else {
                whisperPartner = hasSender ? params.name().getString() : null;
            }
        }

        Text disContent = content;
        Text disSender = hasSender ? params.name() : Text.translatable("e33chat.sender.system");
        if (isWhisper && hasSender) {
            disContent = Text.literal(extractWhisperContent(msgStr, params.name().getString()));
        } else if (hasSender) {
            Text fullLine = params.applyChatDecoration(content);
            disSender = extractDecoratedName(fullLine, msgStr, params.name().getString(), disSender);
        }
        ChatMessageStore.debugLog("[e33chat] Disguised | raw='" + msgStr + "' | whisper=" + isWhisper
            + " | partner=" + whisperPartner + " | sender='" + disSender.getString()
            + "' | content='" + disContent.getString() + "'");
        ChatMessageStore.setPendingMeta(new ChatMessageStore.SenderMeta(
            new UUID(0, 0), disSender, disContent, !hasSender,
            hasSender ? params.name().getString() : null,
            isWhisper, whisperPartner));
    }

    @Inject(method = "onGameMessage", at = @At("HEAD"))
    private void onSystemChat(Text message, boolean overlay, CallbackInfo ci) {
        if (overlay) return;

        if (classifyByKey(message)) return;

        String sysText = message.getString();
        boolean hasEchoFlag = ChatMessageStore.hasPendingWhisperEcho();
        boolean hasKw = sysText.contains("悄悄") || sysText.contains("whispers") || sysText.contains("whisper")
            || sysText.contains("私聊") || sysText.contains("密语") || sysText.contains("密聊");
        if (hasEchoFlag && hasKw) {
            ChatMessageStore.consumeWhisperEcho();
            ChatMessageStore.markSuppressCapture();
            ChatMessageStore.debugLog("[e33chat] System(echo suppressed) | text='" + sysText + "'");
            return;
        }

        var cfg = ChatBubbleClientSetup.config();
        ChatMessageStore.debugLog("[e33chat] System | text='" + sysText + "' | overlay=" + overlay);

        if (cfg != null && cfg.chatReportCompat()) {
            String text = message.getString();
            var player = MinecraftClient.getInstance().player;
            if (player != null && player.networkHandler != null) {
                var online = player.networkHandler.getPlayerList();
                var onlineNames = new ArrayList<String>();
                for (var info : online)
                    for (String cand : nameCandidates(info)) onlineNames.add(cand);
                onlineNames = new ArrayList<>(new LinkedHashSet<>(onlineNames));

                var parsed = MessagePresentation.parseDecoratedPlayerLine(text, onlineNames);
                if (parsed.isPresent()) {
                    var pl = parsed.orElseThrow();
                    var info = online.stream()
                        .filter(i -> {
                            for (String cand : nameCandidates(i))
                                if (cand.equals(pl.playerName())) return true;
                            return false;
                        }).findFirst().orElse(null);
                    UUID uid = info != null ? info.getProfile().getId() : new UUID(0, 0);
                    int nameIdx = text.indexOf(pl.playerName());
                    int contentStart = nameIdx + pl.playerName().length();
                    while (contentStart < text.length()) {
                        char ch = text.charAt(contentStart);
                        if (Character.isWhitespace(ch) || ch == '>' || ch == ':'
                            || ch == '：' || ch == '»' || ch == '-' || ch == '|') contentStart++;
                        else break;
                    }
                    Text displayName = extractDecoratedName(message, pl.content(), pl.playerName(),
                        Text.literal((text.substring(0, nameIdx) + pl.playerName()).trim()));
                    Text contentComp = ChatMessageStore.sliceStyled(message, contentStart, text.length());
                    ChatMessageStore.setPendingMeta(new ChatMessageStore.SenderMeta(
                        uid, displayName, contentComp, false,
                        info != null ? info.getProfile().getName() : pl.playerName(),
                        false, null));
                    return;
                }
            }

            var wm = detectWhisperInSystemMessage(sysText, "whisper compat");
            if (wm != null) { ChatMessageStore.setPendingMeta(wm); return; }

            var tc = detectByTellClick(message, sysText);
            if (tc != null) { ChatMessageStore.setPendingMeta(tc); return; }

            boolean isSystem = !cfg.systemChatAsBubble();
            ChatMessageStore.setPendingMeta(new ChatMessageStore.SenderMeta(
                new UUID(0, 0), Text.translatable("e33chat.sender.system"),
                message, isSystem, null, false, null));
            return;
        }

        var wm = detectWhisperInSystemMessage(sysText, "whisper");
        if (wm != null) { ChatMessageStore.setPendingMeta(wm); return; }

        boolean isSystem = cfg == null || !cfg.systemChatAsBubble();
        ChatMessageStore.setPendingMeta(new ChatMessageStore.SenderMeta(
            new UUID(0, 0), Text.translatable("e33chat.sender.system"),
            message, isSystem, null, false, null));
    }
}
