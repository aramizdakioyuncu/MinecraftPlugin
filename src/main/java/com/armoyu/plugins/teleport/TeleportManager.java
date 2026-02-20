package com.armoyu.plugins.teleport;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class TeleportManager {

    // Target -> Sender (Kim kime istek attı: Hedef oyuncu -> İstek atan oyuncu)
    private final Map<Player, Player> teleportRequests = new HashMap<>();

    // Işınlanma sayacı için aktif görevler (Oyuncu -> TaskID)
    private final Map<UUID, Integer> activeTasks = new HashMap<>();

    public void addRequest(Player sender, Player target) {
        teleportRequests.put(target, sender);
    }

    public Player getRequestSender(Player target) {
        return teleportRequests.get(target);
    }

    public void removeRequest(Player target) {
        teleportRequests.remove(target);
    }

    public boolean hasRequest(Player target) {
        return teleportRequests.containsKey(target);
    }

    public void addTask(Player player, int taskId) {
        activeTasks.put(player.getUniqueId(), taskId);
    }

    public void removeTask(Player player) {
        activeTasks.remove(player.getUniqueId());
    }

    public boolean hasActiveTask(Player player) {
        return activeTasks.containsKey(player.getUniqueId());
    }

    public void cancelTask(Player player) {
        if (hasActiveTask(player)) {
            Bukkit.getScheduler().cancelTask(activeTasks.get(player.getUniqueId()));
            removeTask(player);
        }
    }
}
