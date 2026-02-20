package com.armoyu.plugins.sethome;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

public class HomeListener implements Listener {

    private final HomeManager homeManager;

    public HomeListener(HomeManager homeManager) {
        this.homeManager = homeManager;
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        if (!homeManager.hasActiveTask(player)) {
            // System.out.println("No active task for " + player.getName());
            return;
        }
        // System.out.println("Active task found for " + player.getName());

        Location from = event.getFrom();
        Location to = event.getTo();

        // Sadece blok değişikliğinde iptal et (kafa çevirme hariç)
        if (to != null && (from.getBlockX() != to.getBlockX() || from.getBlockY() != to.getBlockY()
                || from.getBlockZ() != to.getBlockZ())) {
            homeManager.cancelTask(player);
            player.sendMessage(ChatColor.RED + "Hareket ettiğiniz için ışınlanma iptal edildi!");
            player.sendTitle("", "", 0, 0, 0); // Ekrana gelen yazıyı temizle
        }
    }
}
