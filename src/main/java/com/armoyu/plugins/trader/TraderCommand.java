package com.armoyu.plugins.trader;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class TraderCommand implements CommandExecutor {

    private final TraderManager traderManager;
    private final TraderGUI traderGUI;

    public TraderCommand(TraderManager traderManager, TraderGUI traderGUI) {
        this.traderManager = traderManager;
        this.traderGUI = traderGUI;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Bu bir oyuncu komutudur.");
            return true;
        }

        Player player = (Player) sender;

        if (args.length == 0) {
            traderGUI.openGUI(player);
            return true;
        }

        if (args[0].equalsIgnoreCase("sat")) {
            if (args.length < 2) {
                player.sendMessage(ChatColor.RED + "Kullanım: /tuccar sat <fiyat>");
                return true;
            }

            double price;
            try {
                price = Double.parseDouble(args[1]);
            } catch (NumberFormatException e) {
                player.sendMessage(ChatColor.RED + "Geçersiz fiyat.");
                return true;
            }

            if (price <= 0) {
                player.sendMessage(ChatColor.RED + "Fiyat 0'dan büyük olmalıdır.");
                return true;
            }

            ItemStack itemInHand = player.getInventory().getItemInMainHand();
            if (itemInHand.getType() == Material.AIR) {
                player.sendMessage(ChatColor.RED + "Elinizde satılacak bir eşya yok!");
                return true;
            }

            if (traderManager.listItem(player, itemInHand, price)) {
                player.getInventory().setItemInMainHand(null);
                player.sendMessage(ChatColor.GREEN + "Eşyanız " + ChatColor.GOLD + price + " ARMO" + ChatColor.GREEN
                        + " karşılığında pazara konuldu.");
            } else {
                player.sendMessage(ChatColor.RED + "Pazara daha fazla eşya koyamazsınız! (Sınır: 5)");
            }
            return true;
        }

        return false;
    }
}
