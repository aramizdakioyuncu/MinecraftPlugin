package com.armoyu.plugins.authspawn;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public class SpawnManager {

    private final JavaPlugin plugin;
    private final List<Location> spawnPoints = new ArrayList<>();
    private final Map<UUID, Location> lastLocations = new HashMap<>();
    private final Random random = new Random();

    public SpawnManager(JavaPlugin plugin) {
        this.plugin = plugin;
        loadSpawnPoints();
    }

    private void loadSpawnPoints() {
        FileConfiguration config = plugin.getConfig();
        List<?> list = config.getList("spawnpoints");
        if (list != null) {
            for (Object obj : list) {
                if (obj instanceof Location) {
                    spawnPoints.add((Location) obj);
                }
            }
        }

        // Eğer liste boşsa ve dünya spawnı varsa ekle (opsiyonel, ama en az 1 nokta
        // olsun)
        if (spawnPoints.isEmpty() && Bukkit.getWorld("world") != null) {
            spawnPoints.add(Bukkit.getWorld("world").getSpawnLocation());
        }
    }

    private void saveSpawnPoints() {
        plugin.getConfig().set("spawnpoints", spawnPoints);
        plugin.saveConfig();
    }

    public void addSpawnPoint(Location location) {
        spawnPoints.add(location);
        saveSpawnPoints();
    }

    public void clearSpawnPoints() {
        spawnPoints.clear();
        saveSpawnPoints();
    }

    public Location getRandomSpawn() {
        if (spawnPoints.isEmpty()) {
            return Bukkit.getWorlds().get(0).getSpawnLocation();
        }
        return spawnPoints.get(random.nextInt(spawnPoints.size()));
    }

    public void saveLastLocation(Player player) {
        lastLocations.put(player.getUniqueId(), player.getLocation());
    }

    public Location getLastLocation(Player player) {
        return lastLocations.get(player.getUniqueId());
    }

    public boolean hasLastLocation(Player player) {
        return lastLocations.containsKey(player.getUniqueId());
    }
}
