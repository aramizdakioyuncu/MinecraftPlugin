package com.armoyu.plugins.claims;

import com.armoyu.plugins.clans.Clan;
import com.armoyu.plugins.clans.ClanManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.ArrayList;
import java.util.List;

public class ClaimListener implements Listener {
    private final ClaimManager claimManager;
    private final ClanManager clanManager;
    private final java.util.Map<java.util.UUID, Long> clickCooldown = new java.util.HashMap<>();

    public ClaimListener(ClaimManager claimManager, ClanManager clanManager) {
        this.claimManager = claimManager;
        this.clanManager = clanManager;
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        if (!canModify(event.getPlayer(), event.getBlock().getLocation())) {
            event.setCancelled(true);
            event.getPlayer().sendMessage(ChatColor.RED + "Burası korumalı bir alan!");
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        if (!canModify(event.getPlayer(), event.getBlock().getLocation())) {
            event.setCancelled(true);
            event.getPlayer().sendMessage(ChatColor.RED + "Burası korumalı bir alan!");
        }
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (event.getClickedBlock() == null)
            return;

        // Selection Logic (Gold Shovel)
        if (event.getPlayer().getInventory().getItemInMainHand().getType() == Material.GOLDEN_SHOVEL) {
            event.setCancelled(true);
            int point = (event.getAction() == Action.LEFT_CLICK_BLOCK) ? 0 : 1;
            claimManager.setSelection(event.getPlayer().getUniqueId(), point, event.getClickedBlock().getLocation());
            event.getPlayer().sendMessage(ChatColor.YELLOW + (point == 0 ? "1." : "2.") + " nokta seçildi: " +
                    event.getClickedBlock().getX() + ", " + event.getClickedBlock().getZ());
            return;
        }

        // Info and Visualization Tool (Stick)
        if (event.getPlayer().getInventory().getItemInMainHand().getType() == Material.STICK) {
            if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                event.setCancelled(true);

                long now = System.currentTimeMillis();
                long last = clickCooldown.getOrDefault(event.getPlayer().getUniqueId(), 0L);
                if (now - last < 3000) { // 3 saniye cooldown
                    return;
                }
                clickCooldown.put(event.getPlayer().getUniqueId(), now);

                handleClaimInfo(event.getPlayer(), event.getClickedBlock().getLocation());
            }
            return;
        }

        if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            if (!canModify(event.getPlayer(), event.getClickedBlock().getLocation())) {
                event.setCancelled(true);
                event.getPlayer().sendMessage(ChatColor.RED + "Burası korumalı bir alan!");
            }
        }
    }

    private void handleClaimInfo(Player player, Location loc) {
        Claim claim = claimManager.getClaimAt(loc);
        if (claim == null) {
            player.sendMessage(ChatColor.YELLOW + "Burası sahipsiz topraklar.");
            return;
        }

        if (claim.getOwnerPlayer() != null) {
            String ownerName = Bukkit.getOfflinePlayer(claim.getOwnerPlayer()).getName();
            player.sendMessage(ChatColor.GOLD + "=== Claim Bilgisi ===");
            player.sendMessage(
                    ChatColor.YELLOW + "Sahibi: " + ChatColor.WHITE + (ownerName != null ? ownerName : "Bilinmiyor"));
        } else if (claim.getOwnerClan() != null) {
            Clan clan = clanManager.getClanById(claim.getOwnerClan());
            player.sendMessage(ChatColor.GOLD + "=== Klan Claim Bilgisi ===");
            player.sendMessage(
                    ChatColor.YELLOW + "Klan: " + ChatColor.WHITE + (clan != null ? clan.getName() : "Bilinmiyor"));
        }

        visualizeClaim(player, claim);
    }

    private void visualizeClaim(Player player, Claim claim) {
        List<Location> edgeBlocks = new ArrayList<>();

        int xLen = claim.getMaxX() - claim.getMinX() + 1;
        int zLen = claim.getMaxZ() - claim.getMinZ() + 1;

        // Büyük claimler için her bloğu gösterme, aralıklarla örnekle
        int xStep = Math.max(1, xLen / 50); // X kenarında maks ~50 blok
        int zStep = Math.max(1, zLen / 50); // Z kenarında maks ~50 blok

        for (int x = claim.getMinX(); x <= claim.getMaxX(); x += xStep) {
            edgeBlocks.add(getGroundLocation(player.getWorld(), x, claim.getMinZ()));
            edgeBlocks.add(getGroundLocation(player.getWorld(), x, claim.getMaxZ()));
        }
        for (int z = claim.getMinZ(); z <= claim.getMaxZ(); z += zStep) {
            edgeBlocks.add(getGroundLocation(player.getWorld(), claim.getMinX(), z));
            edgeBlocks.add(getGroundLocation(player.getWorld(), claim.getMaxX(), z));
        }

        // Köşeleri mutlaka ekle
        edgeBlocks.add(getGroundLocation(player.getWorld(), claim.getMinX(), claim.getMinZ()));
        edgeBlocks.add(getGroundLocation(player.getWorld(), claim.getMaxX(), claim.getMinZ()));
        edgeBlocks.add(getGroundLocation(player.getWorld(), claim.getMinX(), claim.getMaxZ()));
        edgeBlocks.add(getGroundLocation(player.getWorld(), claim.getMaxX(), claim.getMaxZ()));

        // Blokları altın bloğu olarak göster
        for (Location l : edgeBlocks) {
            player.sendBlockChange(l, Material.GOLD_BLOCK, (byte) 0);
        }

        // 5 saniye sonra eski haline döndür
        Bukkit.getScheduler().runTaskLater(com.armoyu.minecraftplugin.getPlugin(com.armoyu.minecraftplugin.class),
                () -> {
                    for (Location l : edgeBlocks) {
                        Block b = l.getBlock();
                        player.sendBlockChange(l, b.getType(), b.getData());
                    }
                }, 100L);
    }

    private boolean canModify(Player player, org.bukkit.Location loc) {
        Claim claim = claimManager.getClaimAt(loc);
        if (claim == null)
            return true; // Wild land

        if (claim.getOwnerPlayer() != null) {
            return claim.isTrusted(player.getUniqueId());
        }

        if (claim.getOwnerClan() != null) {
            Clan clan = clanManager.getClanById(claim.getOwnerClan());
            if (clan != null) {
                return clan.isOfficer(player.getUniqueId());
            }
        }

        return false;
    }

    private Location getGroundLocation(org.bukkit.World world, int x, int z) {
        Block top = world.getHighestBlockAt(x, z);
        while (top.getType().name().contains("AIR") || top.getType().name().contains("LEAVES")) {
            if (top.getY() <= 0)
                break;
            top = top.getRelative(0, -1, 0);
        }
        return top.getLocation();
    }
}
