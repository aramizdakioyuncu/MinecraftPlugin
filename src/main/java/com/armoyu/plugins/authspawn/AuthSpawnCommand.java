package com.armoyu.plugins.authspawn;

import com.armoyu.plugins.actionmanager.ActionManager;
import com.armoyu.utils.ChatUtils;
import com.armoyu.utils.PlayerPermission;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class AuthSpawnCommand implements CommandExecutor {

    private final SpawnManager spawnManager;
    private final ActionManager actionManager;

    public AuthSpawnCommand(SpawnManager spawnManager, ActionManager actionManager) {
        this.spawnManager = spawnManager;
        this.actionManager = actionManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Bu komutu sadece oyuncular kullanabilir.");
            return true;
        }

        Player player = (Player) sender;

        // Yetki kontrolü (Kendi sistemimiz)
        if (!actionManager.hasPermission(player, PlayerPermission.AUTHSPAWN)) {
            player.sendMessage(ChatColor.RED + "Bu komutu kullanmak için yetkiniz yok.");
            return true;
        }

        if (args.length == 0) {
            player.sendMessage(ChatColor.GOLD + "AuthSpawn Kullanımı:");
            player.sendMessage(ChatColor.YELLOW + "/authspawn add" + ChatColor.WHITE
                    + " - Mevcut konumunuzu spawn noktası olarak ekler.");
            player.sendMessage(
                    ChatColor.YELLOW + "/authspawn clear" + ChatColor.WHITE + " - Tüm spawn noktalarını temizler.");
            return true;
        }

        if (args[0].equalsIgnoreCase("add")) {
            spawnManager.addSpawnPoint(player.getLocation());
            ChatUtils.sendMessagePlayer(player, ChatColor.GREEN + "Spawn noktası eklendi!");
            return true;
        }

        if (args[0].equalsIgnoreCase("clear")) {
            spawnManager.clearSpawnPoints();
            ChatUtils.sendMessagePlayer(player, ChatColor.RED + "Tüm spawn noktaları temizlendi!");
            return true;
        }

        return false;
    }
}
