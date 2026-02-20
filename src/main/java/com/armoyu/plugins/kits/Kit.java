package com.armoyu.plugins.kits;

import org.bukkit.inventory.ItemStack;
import java.util.List;

public class Kit {
    private final String name;
    private final List<ItemStack> items;
    private final double cost;
    private final int cooldownSeconds;

    public Kit(String name, List<ItemStack> items, double cost, int cooldownSeconds) {
        this.name = name;
        this.items = items;
        this.cost = cost;
        this.cooldownSeconds = cooldownSeconds;
    }

    public String getName() {
        return name;
    }

    public List<ItemStack> getItems() {
        return items;
    }

    public double getCost() {
        return cost;
    }

    public int getCooldownSeconds() {
        return cooldownSeconds;
    }
}
