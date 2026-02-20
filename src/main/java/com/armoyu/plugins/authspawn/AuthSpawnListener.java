package com.armoyu.plugins.authspawn;

import com.armoyu.utils.ChatUtils;
import com.armoyu.plugins.actionmanager.ActionManager;
import com.armoyu.utils.PlayerRole;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class AuthSpawnListener implements Listener {

    private final SpawnManager spawnManager;
    private final ActionManager actionManager;

    public AuthSpawnListener(SpawnManager spawnManager, ActionManager actionManager) {
        this.spawnManager = spawnManager;
        this.actionManager = actionManager;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        // Bukkit scheduler ile 1 tick gecikmeli ışınla (Spigot bazen join anında
        // teleportu görmezden gelebilir)
        org.bukkit.Bukkit.getScheduler()
                .runTaskLater(com.armoyu.minecraftplugin.getPlugin(com.armoyu.minecraftplugin.class), () -> {
                    Location spawnLocation = spawnManager.getRandomSpawn();
                    player.teleport(spawnLocation);
                    player.setGameMode(org.bukkit.GameMode.ADVENTURE);

                    // Scoreboard'u ayarla
                    com.armoyu.minecraftplugin.getScoreManager().setScoreboard(player);

                    ChatUtils.consoleSendMessage(player.getName() + " lobi spawn noktasına gönderildi.");
                }, 1L);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        PlayerRole role = actionManager.getRole(player);

        // Sadece giriş yapmış oyuncuların konumunu kaydet
        if (role != null && role != PlayerRole.GUEST) {
            spawnManager.saveLastLocation(player);
            ChatUtils.consoleSendMessage(player.getName() + " konumu kaydedildi.");
        } else {
            ChatUtils.consoleSendMessage(player.getName() + " guest olduğu için konumu kaydedilmedi.");
        }
    }
}
