package com.armoyu.plugins.teleport;

import com.armoyu.plugins.actionmanager.ActionManager;
import com.armoyu.utils.ChatUtils;
import com.armoyu.utils.PlayerPermission;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class TpDenyCommand implements CommandExecutor {

    private final TeleportManager teleportManager;
    private final ActionManager actionManager;

    public TpDenyCommand(TeleportManager teleportManager, ActionManager actionManager) {
        this.teleportManager = teleportManager;
        this.actionManager = actionManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Bu komutu sadece oyuncular kullanabilir.");
            return true;
        }

        Player target = (Player) sender;

        if (!actionManager.hasPermission(target, PlayerPermission.TELEPORT)) {
            target.sendMessage(ChatColor.RED + "Bu komutu kullanmak için yetkiniz yok.");
            return true;
        }

        if (!teleportManager.hasRequest(target)) {
            target.sendMessage(ChatColor.RED + "Size gelen bir ışınlanma isteği yok.");
            return true;
        }

        Player requester = teleportManager.getRequestSender(target);
        if (requester != null && requester.isOnline()) {
            // Eğer ışınlanma zaten başlamışsa (sayaç varsa) iptal et
            teleportManager.cancelTask(requester);

            ChatUtils.sendMessagePlayer(requester,
                    ChatColor.RED + target.getName() + " ışınlanma isteğinizi reddetti.");
        }

        teleportManager.removeRequest(target);
        ChatUtils.sendMessagePlayer(target, ChatColor.GREEN + "İstek reddedildi.");

        return true;
    }
}
