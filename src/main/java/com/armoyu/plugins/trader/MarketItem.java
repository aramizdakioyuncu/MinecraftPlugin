package com.armoyu.plugins.trader;

import org.bukkit.inventory.ItemStack;
import java.util.UUID;

public class MarketItem {
    private final UUID id;
    private final UUID sellerId;
    private final String sellerName;
    private final ItemStack item;
    private final double price;
    private final long listedAt;

    public MarketItem(UUID sellerId, String sellerName, ItemStack item, double price) {
        this.id = UUID.randomUUID();
        this.sellerId = sellerId;
        this.sellerName = sellerName;
        this.item = item;
        this.price = price;
        this.listedAt = System.currentTimeMillis();
    }

    public UUID getId() {
        return id;
    }

    public UUID getSellerId() {
        return sellerId;
    }

    public String getSellerName() {
        return sellerName;
    }

    public ItemStack getItem() {
        return item;
    }

    public double getPrice() {
        return price;
    }

    public long getListedAt() {
        return listedAt;
    }
}
