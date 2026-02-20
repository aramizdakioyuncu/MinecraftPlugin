package com.armoyu.plugins.actionmanager;

import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerToggleSprintEvent;
import org.bukkit.event.player.PlayerToggleFlightEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.plugin.java.JavaPlugin;

import com.armoyu.utils.PlayerPermission;
import com.armoyu.utils.PlayerRole;

import java.util.HashMap;
import java.util.Map;

public class ActionManager implements Listener {

    private final JavaPlugin plugin;
    private final Map<Player, PlayerRole> playerRoles = new HashMap<>();

    public ActionManager(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    // Oyuncu girişinde guest rolü atanır
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        setRole(player, PlayerRole.GUEST); // varsayılan rol guest
    }

    // Hareket
    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        if (!hasPermission(event.getPlayer(), PlayerPermission.MOVE))
            event.setCancelled(true);
    }

    // Can kaybını engelle
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntityDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();

            // MOVE izni olmayanlar veya sadece guest için örnek
            if (!hasPermission(player, PlayerPermission.ATTACK)) {
                event.setCancelled(true);
            }
        }
    }

    // Bir oyuncu diğerini vurursa da iptal et
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {

        if (event.getEntity() instanceof Player) {
            Player target = (Player) event.getEntity();

            if (event.getDamager() instanceof Player) {
                Player damager = (Player) event.getDamager();
                if (!hasPermission(damager, PlayerPermission.ATTACK)) {
                    event.setCancelled(true);
                }
            } else {
                // Eğer hedef guest ise, diğer tüm hasarlara kapalı
                if (!hasPermission(target, PlayerPermission.ATTACK)) {
                    event.setCancelled(true);
                }
            }
        }
    }

    // Sprint
    @EventHandler
    public void onPlayerSprint(PlayerToggleSprintEvent event) {
        if (!hasPermission(event.getPlayer(), PlayerPermission.SPRINT))
            event.setCancelled(true);
    }

    // Fly
    @EventHandler
    public void onPlayerToggleFlight(PlayerToggleFlightEvent event) {
        if (!hasPermission(event.getPlayer(), PlayerPermission.FLY))
            event.setCancelled(true);
    }

    // Envanter açma
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInventoryOpen(InventoryOpenEvent event) {
        Player player = (Player) event.getPlayer();
        if (!hasPermission(player, PlayerPermission.INTERACT)) {
            event.setCancelled(true);
            player.sendMessage("Inventory açamazsın!");
        }
    }

    // Yere item atmayı engelle
    @EventHandler
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        Player player = event.getPlayer();
        if (!hasPermission(player, PlayerPermission.INTERACT)) {
            event.setCancelled(true);
            player.sendMessage("Şu an item atamazsın!");
        }
    }

    // Blok kırma
    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        if (!hasPermission(event.getPlayer(), PlayerPermission.BREAK))
            event.setCancelled(true);
    }

    // Blok yerleştirme
    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        if (!hasPermission(event.getPlayer(), PlayerPermission.PLACE))
            event.setCancelled(true);
    }

    // Saldırı
    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player) {
            Player player = (Player) event.getDamager();
            if (!hasPermission(player, PlayerPermission.ATTACK))
                event.setCancelled(true);
        }
    }

    // Rol atama
    public void setRole(Player player, PlayerRole role) {
        playerRoles.put(player, role);
    }

    // İzin kontrolü
    private boolean hasPermission(Player player, PlayerPermission permission) {
        PlayerRole role = playerRoles.get(player);
        if (role == null)
            return false; // default guest gibi davran
        return role.hasPermission(permission);
    }

}
