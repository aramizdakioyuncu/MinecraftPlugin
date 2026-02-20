package com.armoyu.plugins.clans;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ClanVaultGUI implements Listener {

    private final ClanManager clanManager;

    public ClanVaultGUI(ClanManager clanManager) {
        this.clanManager = clanManager;
    }

    public void openVault(Player player) {
        Clan clan = clanManager.getClanByPlayer(player.getUniqueId());
        if (clan == null)
            return;

        int slots = getVaultSize(clan.getLevel());
        Inventory inv = Bukkit.createInventory(player, slots,
                ChatColor.DARK_GREEN + "Klan Kasası (Lv." + clan.getLevel() + ")");

        // Eşyaları yükle
        List<ItemStack> items = clan.getVault();
        for (int i = 0; i < Math.min(items.size(), slots); i++) {
            if (items.get(i) != null) {
                inv.setItem(i, items.get(i));
            }
        }

        player.openInventory(inv);
    }

    @EventHandler
    public void onVaultClose(InventoryCloseEvent event) {
        if (!event.getView().getTitle().startsWith(ChatColor.DARK_GREEN + "Klan Kasası"))
            return;

        Player player = (Player) event.getPlayer();
        Clan clan = clanManager.getClanByPlayer(player.getUniqueId());
        if (clan == null)
            return;

        // Eşyaları kaydet
        Inventory inv = event.getInventory();
        List<ItemStack> items = new ArrayList<>(Arrays.asList(inv.getContents()));
        clan.getVault().clear();
        clan.getVault().addAll(items);
    }

    private int getVaultSize(int level) {
        switch (level) {
            case 1:
                return 9;
            case 2:
                return 18;
            case 3:
                return 27;
            case 4:
                return 36;
            case 5:
                return 54;
            default:
                return 9;
        }
    }
}
