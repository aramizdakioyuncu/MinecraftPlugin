package com.armoyu.plugins.teleport;

import com.armoyu.plugins.actionmanager.ActionManager;
import com.armoyu.utils.ChatUtils;
import com.armoyu.utils.PlayerPermission;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

public class TpAcceptCommand implements CommandExecutor {

    private final TeleportManager teleportManager;
    private final ActionManager actionManager;
    private final JavaPlugin plugin;

    public TpAcceptCommand(TeleportManager teleportManager, ActionManager actionManager, JavaPlugin plugin) {
        this.teleportManager = teleportManager;
        this.actionManager = actionManager;
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Bu komutu sadece oyuncular kullanabilir.");
            return true;
        }

        Player target = (Player) sender; // İsteği kabul eden kişi (hedef)

        if (!actionManager.hasPermission(target, PlayerPermission.TELEPORT)) {
            target.sendMessage(ChatColor.RED + "Bu komutu kullanmak için yetkiniz yok.");
            return true;
        }

        if (!teleportManager.hasRequest(target)) {
            target.sendMessage(ChatColor.RED + "Size gelen bir ışınlanma isteği yok.");
            return true;
        }

        Player requester = teleportManager.getRequestSender(target);
        if (requester == null || !requester.isOnline()) {
            target.sendMessage(ChatColor.RED + "İstek gönderen oyuncu oyundan çıkmış.");
            teleportManager.removeRequest(target);
            return true;
        }

        if (teleportManager.hasActiveTask(requester)) {
            target.sendMessage(ChatColor.YELLOW + "Oyuncunun zaten aktif bir ışınlanma işlemi var.");
            return true;
        }

        ChatUtils.sendMessagePlayer(target,
                ChatColor.GREEN + "İsteği kabul ettiniz. Oyuncu 5 saniye içinde ışınlanacak.");
        ChatUtils.sendMessagePlayer(requester, ChatColor.GREEN + "İsteğiniz kabul edildi. 5 saniye hareket etmeyin.");

        // BukkitRunnable ile sayaç (Requester için)
        BukkitRunnable runnable = new BukkitRunnable() {
            int seconds = 5;

            @Override
            public void run() {
                // Görev iptal edildiyse dur (Listener tarafından)
                if (!teleportManager.hasActiveTask(requester)) {
                    this.cancel();
                    return;
                }

                if (seconds > 0) {
                    requester.sendTitle(ChatColor.BLUE + String.valueOf(seconds), "", 0, 20, 0);
                    seconds--;
                } else {
                    requester.teleport(target.getLocation());
                    ChatUtils.sendMessagePlayer(requester, ChatColor.GREEN + "Işınlandınız!");
                    teleportManager.removeTask(requester);
                    teleportManager.removeRequest(target); // İsteği temizle
                    this.cancel();
                }
            }
        };

        int taskId = runnable.runTaskTimer(plugin, 0L, 20L).getTaskId();
        teleportManager.addTask(requester, taskId);

        return true;
    }
}
