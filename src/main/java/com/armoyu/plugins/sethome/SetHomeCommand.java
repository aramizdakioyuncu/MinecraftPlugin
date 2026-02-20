package com.armoyu.plugins.sethome;

import com.armoyu.plugins.actionmanager.ActionManager;
import com.armoyu.utils.ChatUtils;
import org.bukkit.ChatColor;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SetHomeCommand implements CommandExecutor {

    private final ActionManager actionManager;
    private final HomeManager homeManager;

    public SetHomeCommand(com.armoyu.plugins.actionmanager.ActionManager actionManager, HomeManager homeManager) {
        this.actionManager = actionManager;
        this.homeManager = homeManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Bu komutu sadece oyuncular kullanabilir.");
            return true;
        }

        Player player = (Player) sender;

        if (!actionManager.hasPermission(player, com.armoyu.utils.PlayerPermission.HOME)) {
            player.sendMessage(ChatColor.RED + "Bu komutu kullanmak için yetkiniz yok.");
            return true;
        }

        homeManager.setHome(player);
        ChatUtils.sendMessagePlayer(player, ChatColor.GREEN + "Eviniz başarıyla kaydedildi!");

        return true;
    }
}
