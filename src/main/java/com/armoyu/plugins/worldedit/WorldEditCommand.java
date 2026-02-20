package com.armoyu.plugins.worldedit;

import com.armoyu.plugins.actionmanager.ActionManager;
import com.armoyu.utils.PlayerRole;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.List;

public class WorldEditCommand implements CommandExecutor {

    private final WorldEditManager weManager;
    private final ActionManager actionManager;

    public WorldEditCommand(WorldEditManager weManager, ActionManager actionManager) {
        this.weManager = weManager;
        this.actionManager = actionManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Bu komutu sadece oyuncular kullanabilir.");
            return true;
        }

        Player player = (Player) sender;

        // Sadece ADMIN kullanabilir
        if (actionManager.getRole(player) != PlayerRole.ADMIN) {
            player.sendMessage(ChatColor.RED + "Bu komutu sadece adminler kullanabilir!");
            return true;
        }

        if (args.length == 0) {
            sendHelp(player);
            return true;
        }

        String sub = args[0].toLowerCase();

        switch (sub) {
            case "balta":
                giveWand(player);
                break;
            case "kopyala":
            case "copy":
                handleCopy(player);
                break;
            case "yapistir":
            case "paste":
                handlePaste(player);
                break;
            case "kaydet":
            case "save":
                handleSave(player, args);
                break;
            case "yukle":
            case "load":
                handleLoad(player, args);
                break;
            case "liste":
            case "list":
                handleList(player);
                break;
            default:
                sendHelp(player);
                break;
        }

        return true;
    }

    private void giveWand(Player player) {
        ItemStack wand = new ItemStack(Material.WOODEN_AXE);
        ItemMeta meta = wand.getItemMeta();
        meta.setDisplayName(ChatColor.LIGHT_PURPLE + "✦ WE Balta ✦");
        meta.setLore(Arrays.asList(
                ChatColor.GRAY + "Sol tık: Pos1 belirle",
                ChatColor.GRAY + "Sağ tık: Pos2 belirle"));
        wand.setItemMeta(meta);
        player.getInventory().addItem(wand);
        player.sendMessage(ChatColor.LIGHT_PURPLE + "✦ WE Balta verildi! Sol tık = Pos1, Sağ tık = Pos2");
    }

    private void handleCopy(Player player) {
        if (!weManager.hasSelection(player)) {
            player.sendMessage(ChatColor.RED + "Önce pos1 ve pos2 belirleyin! (/we balta)");
            return;
        }

        weManager.copy(player);
        int count = weManager.getClipboardSize(player);
        int[] size = weManager.getSelectionSize(player);
        player.sendMessage(
                ChatColor.GREEN + "✓ " + count + " blok kopyalandı! (" + size[0] + "x" + size[1] + "x" + size[2] + ")");
    }

    private void handlePaste(Player player) {
        if (!weManager.hasClipboard(player)) {
            player.sendMessage(ChatColor.RED + "Kopyalanmış bir yapı yok! Önce /we kopyala kullanın.");
            return;
        }

        player.sendMessage(ChatColor.YELLOW + "Yapıştırılıyor...");
        weManager.paste(player);
        player.sendMessage(ChatColor.GREEN + "✓ " + weManager.getClipboardSize(player) + " blok yapıştırıldı!");
    }

    private void handleSave(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(ChatColor.RED + "Kullanım: /we kaydet <isim>");
            return;
        }

        if (!weManager.hasSelection(player)) {
            player.sendMessage(ChatColor.RED + "Önce pos1 ve pos2 belirleyin!");
            return;
        }

        String name = args[1];
        boolean ok = weManager.saveStructure(name, player);
        if (ok) {
            int[] size = weManager.getSelectionSize(player);
            player.sendMessage(ChatColor.GREEN + "✓ '" + name + "' yapısı kaydedildi! (" + size[0] + "x" + size[1] + "x"
                    + size[2] + ")");
        } else {
            player.sendMessage(ChatColor.RED + "Yapı kaydedilemedi!");
        }
    }

    private void handleLoad(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(ChatColor.RED + "Kullanım: /we yukle <isim>");
            return;
        }

        String name = args[1];
        player.sendMessage(ChatColor.YELLOW + "'" + name + "' yükleniyor...");
        boolean ok = weManager.loadStructure(name, player.getLocation());
        if (ok) {
            player.sendMessage(ChatColor.GREEN + "✓ '" + name + "' yapısı konumunuza yüklendi!");
        } else {
            player.sendMessage(ChatColor.RED + "Yapı bulunamadı: " + name);
        }
    }

    private void handleList(Player player) {
        List<String> structures = weManager.getSavedStructures();
        player.sendMessage(ChatColor.GOLD + "=== Kayıtlı Yapılar ===");
        if (structures.isEmpty()) {
            player.sendMessage(ChatColor.GRAY + "Henüz kaydedilmiş yapı yok.");
        } else {
            for (String s : structures) {
                player.sendMessage(ChatColor.YELLOW + "- " + ChatColor.WHITE + s);
            }
        }
        player.sendMessage(ChatColor.GOLD + "======================");
    }

    private void sendHelp(Player player) {
        player.sendMessage(ChatColor.GOLD + "=== [ WORLD EDIT ] ===");
        player.sendMessage(ChatColor.YELLOW + "/we balta " + ChatColor.GRAY + "- Seçim baltası verir");
        player.sendMessage(ChatColor.YELLOW + "/we kopyala " + ChatColor.GRAY + "- Seçimi kopyalar");
        player.sendMessage(ChatColor.YELLOW + "/we yapistir " + ChatColor.GRAY + "- Kopyayı yapıştırır");
        player.sendMessage(ChatColor.YELLOW + "/we kaydet <isim> " + ChatColor.GRAY + "- Seçimi yapı olarak kaydeder");
        player.sendMessage(ChatColor.YELLOW + "/we yukle <isim> " + ChatColor.GRAY + "- Yapıyı konumunuza yükler");
        player.sendMessage(ChatColor.YELLOW + "/we liste " + ChatColor.GRAY + "- Kayıtlı yapıları listeler");
        player.sendMessage(ChatColor.GOLD + "====================");
    }
}
