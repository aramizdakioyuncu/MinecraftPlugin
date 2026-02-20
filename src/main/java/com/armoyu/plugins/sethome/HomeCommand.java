package com.armoyu.plugins.sethome;

import com.armoyu.utils.ChatUtils;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

public class HomeCommand implements CommandExecutor {

    private final com.armoyu.plugins.actionmanager.ActionManager actionManager;
    private final HomeManager homeManager;
    private final JavaPlugin plugin;

    public HomeCommand(com.armoyu.plugins.actionmanager.ActionManager actionManager, HomeManager homeManager,
            JavaPlugin plugin) {
        this.actionManager = actionManager;
        this.homeManager = homeManager;
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Bu komutu sadece oyuncular kullanabilir.");
            return true;
        }

        Player player = (Player) sender;

        if (args.length > 0 && args[0].equalsIgnoreCase("set")) {
            handleSetHome(player);
            return true;
        }

        if (!actionManager.hasPermission(player, com.armoyu.utils.PlayerPermission.HOME)) {
            player.sendMessage(ChatColor.RED + "Bu komutu kullanmak için yetkiniz yok.");
            return true;
        }

        Location homeLocation = homeManager.getHome(player);

        if (homeLocation == null) {
            player.sendMessage(ChatColor.RED + "Henüz bir ev belirlemediniz. /sethome komutunu kullanın.");
            return true;
        }

        if (homeManager.hasActiveTask(player)) {
            player.sendMessage(ChatColor.YELLOW + "Zaten aktif bir ışınlanma işleminiz var.");
            return true;
        }

        player.sendMessage(ChatColor.GREEN + "5 saniye içinde ışınlanacaksınız. Lütfen hareket etmeyin.");

        // DÜZELTME: BukkitRunnable'ı değişkene atayıp task ID'sini alacağız.
        BukkitRunnable runnable = new BukkitRunnable() {
            int seconds = 5;

            @Override
            public void run() {
                if (!homeManager.hasActiveTask(player)) {
                    this.cancel();
                    return;
                }
                if (seconds > 0) {
                    player.sendTitle(ChatColor.RED + String.valueOf(seconds), "", 0, 20, 0);
                    seconds--;
                } else {
                    player.teleport(homeLocation);
                    ChatUtils.sendMessagePlayer(player, ChatColor.GREEN + "Evinize ışınlandınız!");
                    homeManager.removeTask(player); // Görevi listeden sil
                    this.cancel();
                }
            }
        };

        int taskId = runnable.runTaskTimer(plugin, 0L, 20L).getTaskId();
        homeManager.addTask(player, taskId);

        return true;
    }

    private void handleSetHome(Player player) {
        if (!actionManager.hasPermission(player, com.armoyu.utils.PlayerPermission.HOME)) {
            player.sendMessage(ChatColor.RED + "Bu komutu kullanmak için yetkiniz yok.");
            return;
        }

        homeManager.setHome(player);
        ChatUtils.sendMessagePlayer(player, ChatColor.GREEN + "Eviniz başarıyla kaydedildi!");
    }
}
