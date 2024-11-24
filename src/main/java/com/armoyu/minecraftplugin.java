package com.armoyu;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.plugin.java.JavaPlugin;

public final class minecraftplugin extends JavaPlugin {


    static String ARMOYUTag = "§b[ARMOYU] ";
    static String ARMOYUTagColor = "§e";


    static void consoleSendMessage(String message, Color colorType) {
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

    static void consoleSendMessage(String message) {
        Bukkit.getConsoleSender().sendMessage(ARMOYUTag + ARMOYUTagColor + message);
    }

    @Override
    public void onEnable() {
        // Plugin startup logic

        consoleSendMessage("PLugin etkinlestirildi!!");
        consoleSendMessage("Yesil Renk", Color.GREEN);
        consoleSendMessage("Kirmizi Renk", Color.RED);


        getServer().getPluginManager().registerEvents(new listener(), this);

    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic

        consoleSendMessage("Plugin Kapatıldı!");

    }
}
