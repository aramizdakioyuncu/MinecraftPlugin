package com.armoyu.plugins.structures;

import com.armoyu.plugins.structures.impl.CastleClassic;
import com.armoyu.plugins.structures.impl.CastleWood;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.*;

public class StructureManager {
    private final Map<String, IStructure> structures = new HashMap<>();
    private static JavaPlugin plugin;

    public StructureManager() {
        IStructure classic = new CastleClassic();
        IStructure wood = new CastleWood();

        registerStructure("klasik", classic);
        registerStructure("1", classic);

        registerStructure("odun", wood);
        registerStructure("2", wood);
    }

    public static void init(JavaPlugin p) {
        plugin = p;
        // structures klasörü yoksa oluştur
        File dir = new File(p.getDataFolder(), "structures");
        if (!dir.exists())
            dir.mkdirs();
    }

    public void registerStructure(String key, IStructure structure) {
        structures.put(key.toLowerCase(), structure);
    }

    public void placeCastle(Location center, String type, String clanName) {
        IStructure structure = structures.get(type.toLowerCase());
        if (structure != null) {
            structure.place(center, clanName);
        } else if (plugin != null) {
            // WorldEdit ile kaydedilmiş yapıyı dene
            boolean loaded = loadAndPlace(type.toLowerCase(), center);
            if (!loaded) {
                // Bulamazsa klasik kullan
                structures.get("klasik").place(center, clanName);
            }
        } else {
            structures.get("klasik").place(center, clanName);
        }
    }

    private boolean loadAndPlace(String name, Location center) {
        if (plugin == null)
            return false;
        File file = new File(plugin.getDataFolder(), "structures/" + name + ".yml");
        if (!file.exists())
            return false;

        FileConfiguration config = YamlConfiguration.loadConfiguration(file);
        int count = config.getInt("blockCount", 0);
        int sizeX = config.getInt("size.x", 0);
        int sizeZ = config.getInt("size.z", 0);

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

    public Set<String> getAvailableTypes() {
        Set<String> types = new LinkedHashSet<>(structures.keySet());
        // Kaydedilmiş yapıları da ekle
        if (plugin != null) {
            File dir = new File(plugin.getDataFolder(), "structures");
            if (dir.exists()) {
                File[] files = dir.listFiles((d, n) -> n.endsWith(".yml"));
                if (files != null) {
                    for (File f : files) {
                        types.add(f.getName().replace(".yml", ""));
                    }
                }
            }
        }
        return types;
    }
}
