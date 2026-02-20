package com.armoyu.plugins.trader;

import com.armoyu.plugins.economy.MoneyManager;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class TraderManager {

    private final MoneyManager moneyManager;
    private final List<MarketItem> marketItems = new ArrayList<>();
    private static final int MAX_ITEMS_PER_PLAYER = 5;

    public TraderManager(MoneyManager moneyManager) {
        this.moneyManager = moneyManager;
    }

    public List<MarketItem> getAllItems() {
        return new ArrayList<>(marketItems);
    }

    public List<MarketItem> getPlayerItems(UUID sellerId) {
        return marketItems.stream()
                .filter(item -> item.getSellerId().equals(sellerId))
                .collect(Collectors.toList());
    }

    public boolean listItem(Player seller, ItemStack item, double price) {
        if (getPlayerItems(seller.getUniqueId()).size() >= MAX_ITEMS_PER_PLAYER) {
            return false;
        }

        MarketItem marketItem = new MarketItem(seller.getUniqueId(), seller.getName(), item.clone(), price);
        marketItems.add(marketItem);
        return true;
    }

    public boolean buyItem(Player buyer, UUID marketItemId) {
        MarketItem marketItem = marketItems.stream()
                .filter(item -> item.getId().equals(marketItemId))
                .findFirst()
                .orElse(null);

        if (marketItem == null)
            return false;
        if (marketItem.getSellerId().equals(buyer.getUniqueId()))
            return false; // Kendi malını alamaz

        if (!moneyManager.hasEnough(buyer.getUniqueId(), marketItem.getPrice())) {
            return false;
        }

        // Para transferi
        moneyManager.removeMoney(buyer.getUniqueId(), marketItem.getPrice());
        moneyManager.addMoney(marketItem.getSellerId(), marketItem.getPrice());

        // Eşya teslimi
        buyer.getInventory().addItem(marketItem.getItem());
        marketItems.remove(marketItem);

        return true;
    }

    public void removeItem(UUID marketItemId) {
        marketItems.removeIf(item -> item.getId().equals(marketItemId));
    }
}
