package com.armoyu.plugins.clans;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.ArmorStand;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;

public class ClanUtils {

    public static void updateLocationVisual(Clan clan, Location loc, String suffix) {
        if (loc == null)
            return;

        JavaPlugin plugin = com.armoyu.minecraftplugin.getPlugin(com.armoyu.minecraftplugin.class);
        File file = new File(plugin.getDataFolder(), "labels.yml");
        FileConfiguration config = YamlConfiguration.loadConfiguration(file);

        String key = clan.getId().toString() + "." + suffix;

        // Eski etiketi dosyadan bul ve sil
        if (config.contains(key)) {
            String worldName = config.getString(key + ".world");
            double oldX = config.getDouble(key + ".x");
            double oldY = config.getDouble(key + ".y");
            double oldZ = config.getDouble(key + ".z");

            org.bukkit.World world = Bukkit.getWorld(worldName);
            if (world != null) {
                Location oldLoc = new Location(world, oldX, oldY, oldZ);
                // Sadece o noktadaki zırh askılarını temizle
                world.getNearbyEntities(oldLoc, 0.5, 0.5, 0.5).stream()
                        .filter(e -> e instanceof ArmorStand)
                        .forEach(org.bukkit.entity.Entity::remove);
            }
        }

        Location targetLoc = loc.clone().add(0, 2.0, 0);

        ArmorStand as = targetLoc.getWorld().spawn(targetLoc, ArmorStand.class);
        as.setVisible(false);
        as.setGravity(false);
        as.setBasePlate(false);
        as.setArms(false);
        as.setSmall(true);
        as.setCustomName(
                ChatColor.GOLD + "[" + ChatColor.YELLOW + clan.getName() + " " + suffix + ChatColor.GOLD + "]");
        as.setCustomNameVisible(true);
        as.setMarker(true);

        // Yeni konumu dosyaya kaydet
        config.set(key + ".world", targetLoc.getWorld().getName());
        config.set(key + ".x", targetLoc.getX());
        config.set(key + ".y", targetLoc.getY());
        config.set(key + ".z", targetLoc.getZ());
        try {
            config.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
