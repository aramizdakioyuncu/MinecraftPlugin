package com.armoyu.plugins.teleport;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerMoveEvent;

public class TeleportListener implements Listener {

    private final TeleportManager teleportManager;

    public TeleportListener(TeleportManager teleportManager) {
        this.teleportManager = teleportManager;
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        if (!teleportManager.hasActiveTask(player)) {
            return;
        }

        Location from = event.getFrom();
        Location to = event.getTo();

        // Sadece blok değişikliğinde iptal et (kafa çevirme hariç)
        if (to != null && (from.getBlockX() != to.getBlockX() || from.getBlockY() != to.getBlockY()
                || from.getBlockZ() != to.getBlockZ())) {
            teleportManager.cancelTask(player);
            player.sendMessage(ChatColor.RED + "Hareket ettiğiniz için ışınlanma iptal edildi!");
            player.sendTitle("", "", 0, 0, 0); // Ekrana gelen yazıyı temizle
        }
    }

    @EventHandler
    public void onPlayerDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getEntity();
        if (teleportManager.hasActiveTask(player)) {
            teleportManager.cancelTask(player);
            player.sendMessage(ChatColor.RED + "Hasar aldığınız için ışınlanma iptal edildi!");
            player.sendTitle("", "", 0, 0, 0);
        }
    }
}
