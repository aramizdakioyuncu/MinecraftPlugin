package com.armoyu.plugins.sleeppingbed;

import com.armoyu.utils.ChatUtils;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashSet;
import java.util.Set;

public class SleepingBedManager {

    private final JavaPlugin plugin;
    private final Set<Player> playersInBed = new HashSet<>();
    private BukkitRunnable task;
    private int countdown = 5;

    public SleepingBedManager(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void addPlayer(Player player) {
        playersInBed.add(player);
        checkNightSkip();
    }

    public void removePlayer(Player player) {
        playersInBed.remove(player);
    }

    public boolean isPlayerInBed(Player player) {
        return playersInBed.contains(player);
    }

    private void checkNightSkip() {
        if (task != null && !task.isCancelled()) {
            return; // Zaten çalışıyorsa tekrar başlatma
        }

        task = new BukkitRunnable() {
            @Override
            public void run() {
                if (playersInBed.isEmpty()) {
                    this.cancel();
                    task = null;
                    countdown = 5;
                    return;
                }

                int totalPlayers = Bukkit.getOnlinePlayers().size();
                if (totalPlayers == 0)
                    return;

                // %50 oran
                double required = totalPlayers / 2.0;

                if (playersInBed.size() >= required) {
                    countdown--;
                    String message = "§eUyuyanlar: §a" + playersInBed.size() + "/" + totalPlayers
                            + " §eGece atlanıyor: §c" + countdown;
                    ChatUtils.sendActionBarToAll(message);

                    if (countdown <= 0) {
                        skipNight();
                        this.cancel();
                        task = null;
                        countdown = 5;
                    }
                } else {
                    countdown = 5; // Oran düştüyse sayacı sıfırla
                    String message = "§eUyuyanlar: §a" + playersInBed.size() + "/" + totalPlayers + " §e(Gereken: "
                            + (int) Math.ceil(required) + ")";
                    ChatUtils.sendActionBarToAll(message);
                }
            }
        };
        task.runTaskTimer(plugin, 0L, 20L);
    }

    private void skipNight() {
        World world = Bukkit.getWorld("world");
        if (world != null) {
            world.setTime(1000);
            world.setStorm(false);
            world.setThundering(false);
            world.setWeatherDuration(6000); // 5 dakika temiz hava
            ChatUtils.sendMessage("§aHerkes uyudu, günaydın!");
        }
        playersInBed.clear();
    }
}
