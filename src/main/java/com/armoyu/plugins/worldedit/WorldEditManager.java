package com.armoyu.plugins.worldedit;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class WorldEditManager {

    private final JavaPlugin plugin;
    private final Map<UUID, Location> pos1Map = new HashMap<>();
    private final Map<UUID, Location> pos2Map = new HashMap<>();
    // clipboard: blocks relative to pos1
    private final Map<UUID, List<int[]>> clipboardPositions = new HashMap<>();
    private final Map<UUID, List<Material>> clipboardMaterials = new HashMap<>();
    private final Map<UUID, List<Byte>> clipboardData = new HashMap<>();

    public WorldEditManager(JavaPlugin plugin) {
        this.plugin = plugin;
        File dir = new File(plugin.getDataFolder(), "structures");
        if (!dir.exists())
            dir.mkdirs();
    }

    public void setPos1(Player player, Location loc) {
        pos1Map.put(player.getUniqueId(), loc.clone());
    }

    public void setPos2(Player player, Location loc) {
        pos2Map.put(player.getUniqueId(), loc.clone());
    }

    public Location getPos1(Player player) {
        return pos1Map.get(player.getUniqueId());
    }

    public Location getPos2(Player player) {
        return pos2Map.get(player.getUniqueId());
    }

    public boolean hasSelection(Player player) {
        return pos1Map.containsKey(player.getUniqueId()) && pos2Map.containsKey(player.getUniqueId());
    }

    public int[] getSelectionSize(Player player) {
        Location p1 = pos1Map.get(player.getUniqueId());
        Location p2 = pos2Map.get(player.getUniqueId());
        int dx = Math.abs(p2.getBlockX() - p1.getBlockX()) + 1;
        int dy = Math.abs(p2.getBlockY() - p1.getBlockY()) + 1;
        int dz = Math.abs(p2.getBlockZ() - p1.getBlockZ()) + 1;
        return new int[] { dx, dy, dz };
    }

    public void copy(Player player) {
        if (!hasSelection(player))
            return;

        Location p1 = pos1Map.get(player.getUniqueId());
        Location p2 = pos2Map.get(player.getUniqueId());
        World world = p1.getWorld();

        int minX = Math.min(p1.getBlockX(), p2.getBlockX());
        int minY = Math.min(p1.getBlockY(), p2.getBlockY());
        int minZ = Math.min(p1.getBlockZ(), p2.getBlockZ());
        int maxX = Math.max(p1.getBlockX(), p2.getBlockX());
        int maxY = Math.max(p1.getBlockY(), p2.getBlockY());
        int maxZ = Math.max(p1.getBlockZ(), p2.getBlockZ());

        List<int[]> positions = new ArrayList<>();
        List<Material> materials = new ArrayList<>();
        List<Byte> data = new ArrayList<>();

        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    Block block = world.getBlockAt(x, y, z);
                    if (block.getType() != Material.AIR) {
                        positions.add(new int[] { x - minX, y - minY, z - minZ });
                        materials.add(block.getType());
                        data.add((byte) 0);
                    }
                }
            }
        }

        clipboardPositions.put(player.getUniqueId(), positions);
        clipboardMaterials.put(player.getUniqueId(), materials);
        clipboardData.put(player.getUniqueId(), data);
    }

    public boolean hasClipboard(Player player) {
        return clipboardPositions.containsKey(player.getUniqueId());
    }

    public int getClipboardSize(Player player) {
        List<int[]> positions = clipboardPositions.get(player.getUniqueId());
        return positions != null ? positions.size() : 0;
    }

    public void paste(Player player) {
        if (!hasClipboard(player))
            return;

        Location base = player.getLocation();
        World world = base.getWorld();
        List<int[]> positions = clipboardPositions.get(player.getUniqueId());
        List<Material> materials = clipboardMaterials.get(player.getUniqueId());

        for (int i = 0; i < positions.size(); i++) {
            int[] pos = positions.get(i);
            Block block = world.getBlockAt(
                    base.getBlockX() + pos[0],
                    base.getBlockY() + pos[1],
                    base.getBlockZ() + pos[2]);
            block.setType(materials.get(i));
        }
    }

    public boolean saveStructure(String name, Player player) {
        if (!hasSelection(player))
            return false;

        Location p1 = pos1Map.get(player.getUniqueId());
        Location p2 = pos2Map.get(player.getUniqueId());
        World world = p1.getWorld();

        int minX = Math.min(p1.getBlockX(), p2.getBlockX());
        int minY = Math.min(p1.getBlockY(), p2.getBlockY());
        int minZ = Math.min(p1.getBlockZ(), p2.getBlockZ());
        int maxX = Math.max(p1.getBlockX(), p2.getBlockX());
        int maxY = Math.max(p1.getBlockY(), p2.getBlockY());
        int maxZ = Math.max(p1.getBlockZ(), p2.getBlockZ());

        File file = new File(plugin.getDataFolder(), "structures/" + name.toLowerCase() + ".yml");
        FileConfiguration config = new YamlConfiguration();

        config.set("size.x", maxX - minX + 1);
        config.set("size.y", maxY - minY + 1);
        config.set("size.z", maxZ - minZ + 1);

        int index = 0;
        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    Block block = world.getBlockAt(x, y, z);
                    if (block.getType() != Material.AIR) {
                        String path = "blocks." + index;
                        config.set(path + ".x", x - minX);
                        config.set(path + ".y", y - minY);
                        config.set(path + ".z", z - minZ);
                        config.set(path + ".type", block.getType().name());
                        index++;
                    }
                }
            }
        }
        config.set("blockCount", index);

        try {
            config.save(file);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean loadStructure(String name, Location center) {
        File file = new File(plugin.getDataFolder(), "structures/" + name.toLowerCase() + ".yml");
        if (!file.exists())
            return false;

        FileConfiguration config = YamlConfiguration.loadConfiguration(file);
        int count = config.getInt("blockCount", 0);
        int sizeX = config.getInt("size.x", 0);
        int sizeZ = config.getInt("size.z", 0);

        // Merkeze göre offset
        int offsetX = sizeX / 2;
        int offsetZ = sizeZ / 2;

        World world = center.getWorld();

        for (int i = 0; i < count; i++) {
            String path = "blocks." + i;
            int x = config.getInt(path + ".x");
            int y = config.getInt(path + ".y");
            int z = config.getInt(path + ".z");
            String typeName = config.getString(path + ".type");
            Material type = Material.getMaterial(typeName);

            if (type != null) {
                Block block = world.getBlockAt(
                        center.getBlockX() + x - offsetX,
                        center.getBlockY() + y,
                        center.getBlockZ() + z - offsetZ);
                block.setType(type);
            }
        }
        return true;
    }

    public List<String> getSavedStructures() {
        File dir = new File(plugin.getDataFolder(), "structures");
        List<String> names = new ArrayList<>();
        if (dir.exists()) {
            File[] files = dir.listFiles((d, n) -> n.endsWith(".yml"));
            if (files != null) {
                for (File f : files) {
                    names.add(f.getName().replace(".yml", ""));
                }
            }
        }
        return names;
    }
}
