package com.armoyu;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashSet;
import java.util.Set;

public final class minecraftplugin extends JavaPlugin {


    static String ARMOYUTag = "§b[ARMOYU] ";
    static String ARMOYUTagColor = "§e";


    //LİSTELER
    static final Set<Player> playersInBed = new HashSet<>();
    //LİSTELER


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


    static void sendMessage(String message) {
        Bukkit.getServer().broadcastMessage(ARMOYUTag + ARMOYUTagColor + message);
    }

    static void sendActionBarMessage(Player player, String message) {
        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacy(message));
    }

    static void sendActionBarToAllPlayers(String message) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            sendActionBarMessage(player, message);
        }
    }

    public void startActionBarTask() {
        // Her saniyede bir çalışan bir görev oluştur


        new BukkitRunnable() {

            int sayac = 5;

            @Override
            public void run() {
                int currentplayermembercount = Bukkit.getOnlinePlayers().size();

                double oran = ((double) currentplayermembercount / 2);


                if (minecraftplugin.playersInBed.size() >= oran) {
                    sayac--;

                    sendActionBarToAllPlayers(String.valueOf(sayac));

                    if (sayac <= 0) {
                        World world = Bukkit.getWorld("world");
                        world.setTime(1000); //Gündüz yap
                        world.setStorm(false); // Yağmuru kapat
                        world.setThundering(false); // Yıldırımı kapat
                        world.setWeatherDuration(6000); // Temiz hava süresini ayarla (6000 tick = 5 dakika)

                        playersInBed.clear();
                        sayac = 5;
                        return;
                    }
                    return;
                }
                sayac = 5;

            }
        }.runTaskTimer(this, 0, 20); // 20 tick = 1 saniye
    }

    @Override
    public void onEnable() {
        // Plugin startup logic

        consoleSendMessage("PLugin etkinlestirildi!!");
        consoleSendMessage("Yesil Renk", Color.GREEN);
        consoleSendMessage("Kirmizi Renk", Color.RED);

        //SAYAÇ FINKSİYONU BAŞLAT
        startActionBarTask();

        getServer().getPluginManager().registerEvents(new listener(), this);

    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic

        consoleSendMessage("Plugin Kapatıldı!");

    }
}
