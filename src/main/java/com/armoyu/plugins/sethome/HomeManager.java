package com.armoyu.plugins.sethome;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class HomeManager {

    private final Map<UUID, Location> playerHomes = new HashMap<>();
    private final Map<UUID, Integer> activeTasks = new HashMap<>();

    public void setHome(Player player) {
        playerHomes.put(player.getUniqueId(), player.getLocation());
    }

    public Location getHome(Player player) {
        return playerHomes.get(player.getUniqueId());
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
            org.bukkit.Bukkit.getScheduler().cancelTask(activeTasks.get(player.getUniqueId()));
            removeTask(player);
        }
    }
}
