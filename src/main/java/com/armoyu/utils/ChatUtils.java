package com.armoyu.utils;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.entity.Player;

public class ChatUtils {

    public static String ARMOYUTag = "§b[ARMOYU] ";
    public static String ARMOYUTagColor = "§e";

    public static void consoleSendMessage(String message, Color colorType) {
        String selectedColor = "§2";
        if (colorType.equals(Color.GREEN)) {
            selectedColor = "§a";
        } else if (colorType.equals(Color.RED)) {
            selectedColor = "§c";
        } else if (colorType.equals(Color.YELLOW)) {
            selectedColor = "§e";
        } else if (colorType.equals(Color.PURPLE)) {
            selectedColor = "§d";
        }

        Bukkit.getConsoleSender().sendMessage(ARMOYUTag + selectedColor + message);
    }

    public static void consoleSendMessage(String message) {
        Bukkit.getConsoleSender().sendMessage(ARMOYUTag + ARMOYUTagColor + message);
    }

    public static void sendMessage(String message) {
        Bukkit.getServer().broadcastMessage(ARMOYUTag + ARMOYUTagColor + message);
    }

    public static void sendActionBar(Player player, String message) {
        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacy(message));
    }

    public static void sendActionBarToAll(String message) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            sendActionBar(player, message);
        }
    }

    public static void sendMessagePlayer(Player p, String message) {
        p.sendMessage(ARMOYUTag + ARMOYUTagColor + message);
    }
}
