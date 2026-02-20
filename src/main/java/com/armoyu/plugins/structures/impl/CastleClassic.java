package com.armoyu.plugins.structures.impl;

import com.armoyu.plugins.structures.IStructure;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.ChatColor;
import org.bukkit.entity.ArmorStand;

public class CastleClassic implements IStructure {

    @Override
    public void place(Location center, String clanName) {
        int startX = center.getBlockX();
        int startY = center.getBlockY();
        int startZ = center.getBlockZ();

        // 1. Surlar ve Köşe Kuleleri (Radius 10 -> 21x21)
        for (int x = -10; x <= 10; x++) {
            for (int z = -10; z <= 10; z++) {
                if (Math.abs(x) == 10 || Math.abs(z) == 10) {
                    // Köşe Kuleleri
                    boolean isCorner = Math.abs(x) == 10 && Math.abs(z) == 10;
                    int height = isCorner ? 7 : 4;

                    for (int y = 0; y <= height; y++) {
                        Location loc = new Location(center.getWorld(), startX + x, startY + y, startZ + z);
                        // Giriş (South side)
                        if (z == -10 && Math.abs(x) <= 2 && y < 4)
                            continue;

                        loc.getBlock().setType(Material.STONE_BRICKS);

                        // Surların üstü
                        if (y == height && (x + z) % 2 == 0) {
                            loc.getBlock().setType(Material.STONE_BRICK_SLAB);
                        }
                    }
                }
                // Zemin
                new Location(center.getWorld(), startX + x, startY - 1, startZ + z).getBlock()
                        .setType(Material.GRASS_BLOCK);
            }
        }

        // 2. Kapı Etiketi (South Gate)
        Location gateLoc = new Location(center.getWorld(), startX, startY + 4, startZ - 10.5);
        createLabel(gateLoc, ChatColor.GOLD + "🏰 " + ChatColor.YELLOW + clanName + " Kalesi");

        // 3. Merkez Bina (Daha Büyük)
        for (int x = -4; x <= 4; x++) {
            for (int z = -4; z <= 4; z++) {
                for (int y = 0; y <= 5; y++) {
                    Location loc = new Location(center.getWorld(), startX + x, startY + y, startZ + z);
                    Block block = loc.getBlock();

                    if (y == 5) {
                        block.setType(Material.DARK_OAK_SLAB);
                    } else if (Math.abs(x) == 4 || Math.abs(z) == 4) {
                        if (z == -4 && Math.abs(x) <= 1 && y < 3) {
                            block.setType(Material.AIR);
                        } else {
                            block.setType(Material.OAK_PLANKS);
                        }
                    } else {
                        block.setType(Material.AIR);
                    }
                }
            }
        }

        // 4. Flama ve Etiket (Spawn Noktası)
        center.getBlock().setType(Material.RED_BANNER);
        createLabel(center.clone().add(0, 2, 0), ChatColor.RED + "🚩 " + ChatColor.WHITE + clanName + " Merkezi");
    }

    private void createLabel(Location loc, String text) {
        ArmorStand as = loc.getWorld().spawn(loc, ArmorStand.class);
        as.setVisible(false);
        as.setGravity(false);
        as.setBasePlate(false);
        as.setMarker(true);
        as.setCustomName(text);
        as.setCustomNameVisible(true);
    }

    @Override
    public String getName() {
        return "Klasik";
    }
}
