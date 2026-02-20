package com.armoyu.plugins.kits;

import com.armoyu.plugins.actionmanager.ActionManager;
import com.armoyu.plugins.economy.MoneyManager;
import com.armoyu.utils.PlayerRole;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class KitCommand implements CommandExecutor {

    private final KitManager kitManager;
    private final MoneyManager moneyManager;
    private final ActionManager actionManager;

    public KitCommand(KitManager kitManager, MoneyManager moneyManager, ActionManager actionManager) {
        this.kitManager = kitManager;
        this.moneyManager = moneyManager;
        this.actionManager = actionManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Bu komutu sadece oyuncular kullanabilir.");
            return true;
        }

        Player player = (Player) sender;

        if (actionManager.getRole(player) == PlayerRole.GUEST) {
            player.sendMessage(ChatColor.RED + "Kit komutlarını kullanabilmek için giriş yapmalısınız!");
            return true;
        }

        if (args.length == 0) {
            sendKitList(player);
            return true;
        }

        String kitName = args[0].toLowerCase();
        Kit kit = kitManager.getKit(kitName);

        if (kit == null) {
            player.sendMessage(ChatColor.RED + "Böyle bir kit bulunamadı.");
            sendKitList(player);
            return true;
        }

        long remaining = kitManager.getRemainingCooldown(player.getUniqueId(), kitName);
        if (remaining > 0) {
            player.sendMessage(
                    ChatColor.RED + "Bu kiti tekrar almak için " + formatTime(remaining) + " beklemelisiniz.");
            return true;
        }

        if (moneyManager.getBalance(player.getUniqueId()) < kit.getCost()) {
            player.sendMessage(ChatColor.RED + "Yetersiz bakiye! Gerekli para: " + kit.getCost());
            return true;
        }

        // Para düş ve kiti ver
        moneyManager.removeMoney(player.getUniqueId(), kit.getCost());
        for (ItemStack item : kit.getItems()) {
            player.getInventory().addItem(item.clone());
        }

        kitManager.setCooldown(player.getUniqueId(), kitName);
        player.sendMessage(ChatColor.GREEN + kit.getName() + " kiti başarıyla alındı! Bedel: " + kit.getCost());

        return true;
    }

    private void sendKitList(Player player) {
        player.sendMessage(ChatColor.GOLD + "=== Mevcut Kitler ===");
        for (Kit kit : kitManager.getAllKits()) {
            String status = kitManager.getRemainingCooldown(player.getUniqueId(), kit.getName()) > 0
                    ? ChatColor.RED + "(Beklemede)"
                    : ChatColor.GREEN + "(Hazır)";

            player.sendMessage(ChatColor.YELLOW + "- " + kit.getName() +
                    ChatColor.WHITE + " | Bedel: " + kit.getCost() + " | " + status);
        }
        player.sendMessage(ChatColor.GRAY + "Almak için: /kit <isim>");
    }

    private String formatTime(long seconds) {
        if (seconds < 60)
            return seconds + " saniye";
        if (seconds < 3600)
            return (seconds / 60) + " dakika";
        return (seconds / 3600) + " saat";
    }
}
