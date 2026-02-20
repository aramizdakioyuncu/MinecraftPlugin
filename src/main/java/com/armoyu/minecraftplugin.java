package com.armoyu;

import com.armoyu.plugins.actionmanager.ActionManager;
import com.armoyu.plugins.auth.commands.LoginRegisterCommand;
import com.armoyu.plugins.sleeppingbed.listener.sleepingbed;
import com.armoyu.plugins.teleport.listener.teleport;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public final class minecraftplugin extends JavaPlugin {

    public static String ARMOYUTag = "§b[ARMOYU] ";
    public static String ARMOYUTagColor = "§e";

    // LİSTELER
    public static final Set<Player> playersInBed = new HashSet<>();
    public static HashMap<Player, Player> teleportRequests = new HashMap<>();

    // LİSTELER

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

    public static void sendActionBarMessage(Player player, String message) {
        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacy(message));
    }

    public static void sendActionBarToAllPlayers(String message) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            sendActionBarMessage(player, message);
        }
    }

    public static void sendMessagePlayer(String message, Player p) {
        p.sendMessage(ARMOYUTag + ARMOYUTagColor + message);
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
                        world.setTime(1000); // Gündüz yap
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

        // SAYAÇ FONKSİYONU BAŞLAT
        startActionBarTask();
        ActionManager actionManager = new ActionManager(this);
        getServer().getPluginManager().registerEvents(actionManager, this);
        getServer().getPluginManager().registerEvents(new listener(), this);
        getServer().getPluginManager().registerEvents(new sleepingbed(), this);

        // Komutlar

        // login ve register komutlarını tek executor ile bağla
        LoginRegisterCommand loginCommand = new LoginRegisterCommand(actionManager);
        getCommand("login").setExecutor(loginCommand);
        getCommand("register").setExecutor(loginCommand);

        getCommand("tpa").setExecutor(new teleport.MyCommandExecutor());
        getCommand("tpaccept").setExecutor(new teleport.MyCommandExecutor());
        getCommand("tpdeny").setExecutor(new teleport.MyCommandExecutor());
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic

        consoleSendMessage("Plugin Kapatıldı!");

    }
}
