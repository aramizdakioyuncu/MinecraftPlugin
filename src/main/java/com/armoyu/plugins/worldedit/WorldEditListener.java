package com.armoyu.plugins.worldedit;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

public class WorldEditListener implements Listener {

    private final WorldEditManager weManager;

    public WorldEditListener(WorldEditManager weManager) {
        this.weManager = weManager;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();

        // Tahta balta kontrolü
        if (player.getInventory().getItemInMainHand().getType() != Material.WOODEN_AXE)
            return;
        if (!player.getInventory().getItemInMainHand().hasItemMeta())
            return;
        if (player.getInventory().getItemInMainHand().getItemMeta() == null)
            return;
        String displayName = player.getInventory().getItemInMainHand().getItemMeta().getDisplayName();
        if (displayName == null || !displayName.contains("WE Balta"))
            return;

        Block block = event.getClickedBlock();
        if (block == null)
            return;

        event.setCancelled(true);

        if (event.getAction() == Action.LEFT_CLICK_BLOCK) {
            weManager.setPos1(player, block.getLocation());
            player.sendMessage(ChatColor.LIGHT_PURPLE + "◆ Pos1 ayarlandı: " + ChatColor.WHITE
                    + block.getX() + ", " + block.getY() + ", " + block.getZ());
        } else if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            weManager.setPos2(player, block.getLocation());
            player.sendMessage(ChatColor.LIGHT_PURPLE + "◆ Pos2 ayarlandı: " + ChatColor.WHITE
                    + block.getX() + ", " + block.getY() + ", " + block.getZ());

            if (weManager.hasSelection(player)) {
                int[] size = weManager.getSelectionSize(player);
                player.sendMessage(ChatColor.GRAY + "Seçim boyutu: " + size[0] + "x" + size[1] + "x" + size[2]
                        + " (" + (size[0] * size[1] * size[2]) + " blok)");
            }
        }
    }
}
