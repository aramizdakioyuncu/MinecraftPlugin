package com.armoyu.plugins.economy.commands;

import com.armoyu.plugins.actionmanager.ActionManager;
import com.armoyu.plugins.economy.MoneyManager;
import com.armoyu.utils.ChatUtils;
import com.armoyu.utils.PlayerPermission;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class MoneyCommand implements CommandExecutor {

    private final MoneyManager moneyManager;
    private final ActionManager actionManager;

    public MoneyCommand(MoneyManager moneyManager, ActionManager actionManager) {
        this.moneyManager = moneyManager;
        this.actionManager = actionManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Bu komut sadece oyuncular içindir.");
            return true;
        }

        Player player = (Player) sender;

        if (args.length == 0) {
            double balance = moneyManager.getBalance(player.getUniqueId());
            ChatUtils.sendMessagePlayer(player, ChatColor.GOLD + "Bakiyeniz: " + ChatColor.GREEN + balance + " ARMO");
            return true;
        }

        if (args[0].equalsIgnoreCase("gonder")) {
            if (args.length < 3) {
                player.sendMessage(ChatColor.RED + "Kullanım: /para gonder <oyuncu> <miktar>");
                return true;
            }

            Player target = Bukkit.getPlayer(args[1]);
            if (target == null) {
                player.sendMessage(ChatColor.RED + "Oyuncu bulunamadı.");
                return true;
            }

            if (target.equals(player)) {
                player.sendMessage(ChatColor.RED + "Kendinize para gönderemezsiniz.");
                return true;
            }

            double amount;
            try {
                amount = Double.parseDouble(args[2]);
            } catch (NumberFormatException e) {
                player.sendMessage(ChatColor.RED + "Geçersiz miktar.");
                return true;
            }

            if (amount <= 0) {
                player.sendMessage(ChatColor.RED + "Miktar 0'dan büyük olmalıdır.");
                return true;
            }

            if (!moneyManager.hasEnough(player.getUniqueId(), amount)) {
                player.sendMessage(ChatColor.RED + "Yetersiz bakiye!");
                return true;
            }

            moneyManager.removeMoney(player.getUniqueId(), amount);
            moneyManager.addMoney(target.getUniqueId(), amount);

            ChatUtils.sendMessagePlayer(player, ChatColor.GREEN + target.getName() + " adlı oyuncuya " + ChatColor.GOLD
                    + amount + " ARMO" + ChatColor.GREEN + " gönderildi.");
            ChatUtils.sendMessagePlayer(target, ChatColor.GREEN + player.getName() + " size " + ChatColor.GOLD + amount
                    + " ARMO" + ChatColor.GREEN + " gönderdi.");
            return true;
        }

        if (args[0].equalsIgnoreCase("admin")) {
            if (!actionManager.hasPermission(player, PlayerPermission.AUTHSPAWN)) { // Admin yetkisi kontrolü
                player.sendMessage(ChatColor.RED + "Bu komut için yetkiniz yok.");
                return true;
            }

            if (args.length < 4) {
                player.sendMessage(ChatColor.RED + "Kullanım: /para admin <set|add|remove> <oyuncu> <miktar>");
                return true;
            }

            Player target = Bukkit.getPlayer(args[2]);
            if (target == null) {
                player.sendMessage(ChatColor.RED + "Oyuncu bulunamadı.");
                return true;
            }

            double amount;
            try {
                amount = Double.parseDouble(args[3]);
            } catch (NumberFormatException e) {
                player.sendMessage(ChatColor.RED + "Geçersiz miktar.");
                return true;
            }

            String action = args[1].toLowerCase();
            switch (action) {
                case "set":
                    moneyManager.setBalance(target.getUniqueId(), amount);
                    player.sendMessage(
                            ChatColor.GREEN + target.getName() + " bakiyesi " + amount + " olarak ayarlandı.");
                    break;
                case "add":
                    moneyManager.addMoney(target.getUniqueId(), amount);
                    player.sendMessage(ChatColor.GREEN + target.getName() + " hesabına " + amount + " eklendi.");
                    break;
                case "remove":
                    moneyManager.removeMoney(target.getUniqueId(), amount);
                    player.sendMessage(ChatColor.GREEN + target.getName() + " hesabından " + amount + " eksiltildi.");
                    break;
                default:
                    player.sendMessage(ChatColor.RED + "Bilinmeyen işlem.");
                    break;
            }
            return true;
        }

        return false;
    }
}
