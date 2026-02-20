package com.armoyu.plugins.teleport;

import com.armoyu.plugins.actionmanager.ActionManager;
import com.armoyu.utils.ChatUtils;
import com.armoyu.utils.PlayerPermission;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class TpaCommand implements CommandExecutor {

    private final TeleportManager teleportManager;
    private final ActionManager actionManager;

    public TpaCommand(TeleportManager teleportManager, ActionManager actionManager) {
        this.teleportManager = teleportManager;
        this.actionManager = actionManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Bu komutu sadece oyuncular kullanabilir.");
            return true;
        }

        Player player = (Player) sender;

        if (!actionManager.hasPermission(player, PlayerPermission.TELEPORT)) {
            player.sendMessage(ChatColor.RED + "Bu komutu kullanmak için yetkiniz yok.");
            return true;
        }

        if (args.length < 1) {
            player.sendMessage(ChatColor.RED + "Kullanım: /tpa <oyuncu>");
            return true;
        }

        Player target = player.getServer().getPlayer(args[0]);
        if (target == null || !target.isOnline()) {
            player.sendMessage(ChatColor.RED + "Oyuncu bulunamadı veya çevrimdışı.");
            return true;
        }

        if (target.equals(player)) {
            player.sendMessage(ChatColor.RED + "Kendinize istek atamazsınız.");
            return true;
        }

        teleportManager.addRequest(player, target);

        ChatUtils.sendMessagePlayer(player,
                ChatColor.GREEN + target.getName() + " oyuncusuna ışınlanma isteği gönderildi.");
        ChatUtils.sendMessagePlayer(target, ChatColor.GOLD + player.getName() + " size ışınlanmak istiyor.");
        ChatUtils.sendMessagePlayer(target, ChatColor.GOLD + "Kabul etmek için: " + ChatColor.GREEN + "/tpaccept");
        ChatUtils.sendMessagePlayer(target, ChatColor.GOLD + "Reddetmek için: " + ChatColor.RED + "/tpdeny");

        return true;
    }
}
