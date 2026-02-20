package com.armoyu.plugins.economy;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class MoneyManager {

    private final JavaPlugin plugin;
    private final Map<UUID, Double> balances = new HashMap<>();
    private com.armoyu.plugins.ui.ScoreManager scoreManager;

    public MoneyManager(JavaPlugin plugin) {
        this.plugin = plugin;
        loadBalances();
    }

    public void setScoreManager(com.armoyu.plugins.ui.ScoreManager scoreManager) {
        this.scoreManager = scoreManager;
    }

    private void loadBalances() {
        FileConfiguration config = plugin.getConfig();
        if (config.isConfigurationSection("economy.balances")) {
            for (String key : config.getConfigurationSection("economy.balances").getKeys(false)) {
                try {
                    UUID uuid = UUID.fromString(key);
                    double balance = config.getDouble("economy.balances." + key);
                    balances.put(uuid, balance);
                } catch (IllegalArgumentException ignored) {
                }
            }
        }
    }

    public void saveBalances() {
        for (Map.Entry<UUID, Double> entry : balances.entrySet()) {
            plugin.getConfig().set("economy.balances." + entry.getKey().toString(), entry.getValue());
        }
        plugin.saveConfig();
    }

    public double getBalance(UUID uuid) {
        return balances.getOrDefault(uuid, plugin.getConfig().getDouble("economy.starting_balance", 100.0));
    }

    public void setBalance(UUID uuid, double amount) {
        balances.put(uuid, amount);
        saveBalances();

        // UI Güncelle
        if (scoreManager != null) {
            org.bukkit.entity.Player p = org.bukkit.Bukkit.getPlayer(uuid);
            if (p != null && p.isOnline()) {
                scoreManager.updateScoreboard(p);
            }
        }
    }

    public void addMoney(UUID uuid, double amount) {
        setBalance(uuid, getBalance(uuid) + amount);
    }

    public void removeMoney(UUID uuid, double amount) {
        setBalance(uuid, getBalance(uuid) - amount);
    }

    public boolean hasEnough(UUID uuid, double amount) {
        return getBalance(uuid) >= amount;
    }
}
