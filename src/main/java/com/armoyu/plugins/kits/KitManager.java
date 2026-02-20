package com.armoyu.plugins.kits;

import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class KitManager {
    private final JavaPlugin plugin;
    private final Map<String, Kit> kits = new HashMap<>();
    private final Map<UUID, Map<String, Long>> cooldowns = new HashMap<>();
    private final File dataFile;
    private FileConfiguration dataConfig;

    public KitManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.dataFile = new File(plugin.getDataFolder(), "kits_data.yml");
        loadKits();
        loadData();
    }

    private void loadKits() {
        // Savaş Kiti
        List<ItemStack> pvpItems = new ArrayList<>();
        pvpItems.add(new ItemStack(Material.IRON_SWORD));
        pvpItems.add(new ItemStack(Material.IRON_CHESTPLATE));
        pvpItems.add(new ItemStack(Material.IRON_LEGGINGS));
        pvpItems.add(new ItemStack(Material.IRON_BOOTS));
        pvpItems.add(new ItemStack(Material.IRON_HELMET));
        pvpItems.add(new ItemStack(Material.COOKED_BEEF, 16));
        kits.put("savas", new Kit("Savaş", pvpItems, 500.0, 3600)); // 1 saat cooldown, 500 para

        // Maden Kiti
        List<ItemStack> mineItems = new ArrayList<>();
        mineItems.add(new ItemStack(Material.IRON_PICKAXE));
        mineItems.add(new ItemStack(Material.IRON_SHOVEL));
        mineItems.add(new ItemStack(Material.TORCH, 32));
        mineItems.add(new ItemStack(Material.BREAD, 8));
        kits.put("maden", new Kit("Maden", mineItems, 250.0, 1800)); // 30 dk cooldown, 250 para
    }

    public Kit getKit(String name) {
        return kits.get(name.toLowerCase());
    }

    public Collection<Kit> getAllKits() {
        return kits.values();
    }

    public long getRemainingCooldown(UUID playerUUID, String kitName) {
        Map<String, Long> playerCooldowns = cooldowns.get(playerUUID);
        if (playerCooldowns == null || !playerCooldowns.containsKey(kitName)) {
            return 0;
        }

        long lastUse = playerCooldowns.get(kitName);
        Kit kit = getKit(kitName);
        if (kit == null)
            return 0;

        long secondsPassed = (System.currentTimeMillis() - lastUse) / 1000;
        long remaining = kit.getCooldownSeconds() - secondsPassed;

        return Math.max(0, remaining);
    }

    public void setCooldown(UUID playerUUID, String kitName) {
        cooldowns.computeIfAbsent(playerUUID, k -> new HashMap<>()).put(kitName.toLowerCase(),
                System.currentTimeMillis());
        saveData();
    }

    private void loadData() {
        if (!dataFile.exists())
            return;
        dataConfig = YamlConfiguration.loadConfiguration(dataFile);

        if (dataConfig.getConfigurationSection("cooldowns") == null)
            return;

        for (String uuidStr : dataConfig.getConfigurationSection("cooldowns").getKeys(false)) {
            UUID uuid = UUID.fromString(uuidStr);
            Map<String, Long> playerKits = new HashMap<>();
            for (String kitName : dataConfig.getConfigurationSection("cooldowns." + uuidStr).getKeys(false)) {
                playerKits.put(kitName, dataConfig.getLong("cooldowns." + uuidStr + "." + kitName));
            }
            cooldowns.put(uuid, playerKits);
        }
    }

    private void saveData() {
        dataConfig = new YamlConfiguration();
        for (Map.Entry<UUID, Map<String, Long>> entry : cooldowns.entrySet()) {
            for (Map.Entry<String, Long> kitEntry : entry.getValue().entrySet()) {
                dataConfig.set("cooldowns." + entry.getKey().toString() + "." + kitEntry.getKey(), kitEntry.getValue());
            }
        }
        try {
            dataConfig.save(dataFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Kit verileri kaydedilemedi!");
        }
    }
}
