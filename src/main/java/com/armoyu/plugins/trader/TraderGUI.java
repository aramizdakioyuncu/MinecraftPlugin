package com.armoyu.plugins.trader;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class TraderGUI implements Listener {

    private final TraderManager traderManager;
    private final String title = ChatColor.DARK_BLUE + "Tüccar - Global Pazar";

    public TraderGUI(TraderManager traderManager) {
        this.traderManager = traderManager;
    }

    public void openGUI(Player player) {
        Inventory inv = Bukkit.createInventory(null, 54, title);

        // Alt bar sekmeleri (Örnek bloklar)
        inv.setItem(45, createGuiItem(Material.GRASS_BLOCK, ChatColor.GREEN + "Bloklar",
                ChatColor.GRAY + "Tüm blok ilanlarını gör"));
        inv.setItem(46, createGuiItem(Material.DIAMOND_SWORD, ChatColor.RED + "Silahlar",
                ChatColor.GRAY + "Tüm silah ilanlarını gör"));
        inv.setItem(47, createGuiItem(Material.IRON_PICKAXE, ChatColor.YELLOW + "Aletler",
                ChatColor.GRAY + "Tüm alet ilanlarını gör"));
        inv.setItem(48, createGuiItem(Material.APPLE, ChatColor.GOLD + "Yiyecekler",
                ChatColor.GRAY + "Tüm yiyecek ilanlarını gör"));
        inv.setItem(49, createGuiItem(Material.CHEST, ChatColor.AQUA + "Tüm İlanlar",
                ChatColor.GRAY + "Pazardaki her şeyi gör"));

        // Cam paneller (Süs)
        ItemStack filler = createGuiItem(Material.BLACK_STAINED_GLASS_PANE, " ", "");
        for (int i = 36; i < 45; i++)
            inv.setItem(i, filler);

        // İlanları listele (Şu an kategori ayrımı olmadan hepsini basıyoruz)
        List<MarketItem> items = traderManager.getAllItems();
        int slot = 0;
        for (MarketItem marketItem : items) {
            if (slot >= 36)
                break;

            ItemStack displayItem = marketItem.getItem().clone();
            ItemMeta meta = displayItem.getItemMeta();
            if (meta != null) {
                List<String> lore = meta.getLore();
                if (lore == null)
                    lore = new ArrayList<>();
                lore.add("");
                lore.add(ChatColor.YELLOW + "Fiyat: " + ChatColor.GOLD + marketItem.getPrice() + " ARMO");
                lore.add(ChatColor.YELLOW + "Satıcı: " + ChatColor.WHITE + marketItem.getSellerName());
                lore.add("");
                lore.add(ChatColor.GREEN + "Satın almak için TIKLA");

                // Gizli UUID saklama (Hack: Ismin sonuna ekleyelim veya NBT kullanalım,
                // şimdilik basitleştiriyoruz)
                meta.setLore(lore);
                displayItem.setItemMeta(meta);
            }
            inv.setItem(slot++, displayItem);
        }

        player.openInventory(inv);
    }

    private ItemStack createGuiItem(Material material, String name, String... lores) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            List<String> loreList = new ArrayList<>();
            for (String lore : lores)
                loreList.add(lore);
            meta.setLore(loreList);
            item.setItemMeta(meta);
        }
        return item;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!event.getView().getTitle().equals(title))
            return;
        event.setCancelled(true);

        Player player = (Player) event.getWhoClicked();
        ItemStack clicked = event.getCurrentItem();

        if (clicked == null || clicked.getType() == Material.AIR)
            return;

        // Sekme geçişleri (Şimdilik sadece mesaj verir, ileride filtreleme eklenir)
        if (event.getRawSlot() >= 45 && event.getRawSlot() <= 49) {
            player.sendMessage(ChatColor.YELLOW + "Kategori filtreleme çok yakında!");
            return;
        }

        // Satın alma veya İptal işlemi
        if (event.getRawSlot() < 36) {
            List<MarketItem> items = traderManager.getAllItems();
            if (event.getRawSlot() < items.size()) {
                MarketItem target = items.get(event.getRawSlot());

                // Kendi eşyası ise iptal et
                if (target.getSellerId().equals(player.getUniqueId())) {
                    player.getInventory().addItem(target.getItem());
                    traderManager.removeItem(target.getId());
                    player.sendMessage(ChatColor.YELLOW + "İlanınız iptal edildi ve eşya geri verildi.");
                    openGUI(player);
                    return;
                }

                // Başkasının eşyası ise satın al
                if (traderManager.buyItem(player, target.getId())) {
                    player.sendMessage(ChatColor.GREEN + "Başarıyla satın aldınız!");
                    openGUI(player); // Yenile
                } else {
                    player.sendMessage(ChatColor.RED + "Satın alma başarısız! (Bakiye yetersiz)");
                }
            }
        }
    }
}
