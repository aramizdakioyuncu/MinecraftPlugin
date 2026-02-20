package com.armoyu.plugins.rcon;

import com.armoyu.plugins.actionmanager.ActionManager;
import com.armoyu.utils.ChatUtils;
import com.armoyu.utils.PlayerRole;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class RconCommand implements CommandExecutor {

    private final JavaPlugin plugin;
    private final ActionManager actionManager;

    public RconCommand(JavaPlugin plugin, ActionManager actionManager) {
        this.plugin = plugin;
        this.actionManager = actionManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Bu komutu sadece oyuncular kullanabilir.");
            return true;
        }

        Player player = (Player) sender;

        boolean rconEnabled = plugin.getConfig().getBoolean("rcon", false);
        String rconPassword = plugin.getConfig().getString("rconpassword", "");

        if (!rconEnabled) {
            player.sendMessage(ChatColor.RED + "RCON özelliği şu an devre dışı.");
            return true;
        }

        if (args.length == 0) {
            player.sendMessage(ChatColor.RED + "Kullanım: /rcon <şifre>");
            return true;
        }

        if (args[0].equals(rconPassword)) {
            actionManager.setRole(player, PlayerRole.ADMIN);
            ChatUtils.sendMessagePlayer(player, ChatColor.GREEN + "Başarıyla ADMIN rolüne yükseltildiniz!");
            ChatUtils.consoleSendMessage(player.getName() + " rcon kullanarak ADMIN oldu.");
        } else {
            player.sendMessage(ChatColor.RED + "Hatalı şifre!");
        }

        return true;
    }
}
