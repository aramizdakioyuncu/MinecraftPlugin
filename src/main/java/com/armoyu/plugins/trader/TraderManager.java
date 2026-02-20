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
        synchronized (marketItems) {
            return new ArrayList<>(marketItems);
        }
    }

    public List<MarketItem> getPlayerItems(UUID sellerId) {
        synchronized (marketItems) {
            return marketItems.stream()
                    .filter(item -> item.getSellerId().equals(sellerId))
                    .collect(Collectors.toList());
        }
    }

    public boolean listItem(Player seller, ItemStack item, double price) {
        synchronized (marketItems) {
            if (getPlayerItems(seller.getUniqueId()).size() >= MAX_ITEMS_PER_PLAYER) {
                return false;
            }

            MarketItem marketItem = new MarketItem(seller.getUniqueId(), seller.getName(), item.clone(), price);
            marketItems.add(marketItem);
            return true;
        }
    }

    public boolean buyItem(Player buyer, UUID marketItemId) {
        MarketItem marketItem;

        // Atomik bul-ve-sil: aynı anda iki kişi aynı itemi alamaz
        synchronized (marketItems) {
            marketItem = marketItems.stream()
                    .filter(item -> item.getId().equals(marketItemId))
                    .findFirst()
                    .orElse(null);

            if (marketItem == null)
                return false; // Zaten satılmış veya silinmiş
            if (marketItem.getSellerId().equals(buyer.getUniqueId()))
                return false; // Kendi malını alamaz

            if (!moneyManager.hasEnough(buyer.getUniqueId(), marketItem.getPrice())) {
                return false;
            }

            // Ürünü listeden HEMEN kaldır — ikinci alıcı burada null alacak
            marketItems.remove(marketItem);
        }

        // Para transferi (artık ürün listeden kaldırılmış durumda)
        moneyManager.removeMoney(buyer.getUniqueId(), marketItem.getPrice());
        moneyManager.addMoney(marketItem.getSellerId(), marketItem.getPrice());

        // Eşya teslimi
        buyer.getInventory().addItem(marketItem.getItem());

        return true;
    }

    public void removeItem(UUID marketItemId) {
        synchronized (marketItems) {
            marketItems.removeIf(item -> item.getId().equals(marketItemId));
        }
    }
}
