package com.armoyu.plugins.structures.impl;

import com.armoyu.plugins.structures.IStructure;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.ArmorStand;
import org.bukkit.ChatColor;

public class CastleWood implements IStructure {

    @Override
    public void place(Location center, String clanName) {
        int startX = center.getBlockX();
        int startY = center.getBlockY();
        int startZ = center.getBlockZ();

        // 1. Kütük Çitler ve Gözcü Kuleleri (Radius 10 -> 21x21)
        for (int x = -10; x <= 10; x++) {
            for (int z = -10; z <= 10; z++) {
                if (Math.abs(x) == 10 || Math.abs(z) == 10) {
                    boolean isCorner = Math.abs(x) == 10 && Math.abs(z) == 10;
                    int height = isCorner ? 5 : 2; // Köşeler daha yüksek kazıklar

                    for (int y = 0; y <= height; y++) {
                        Location loc = new Location(center.getWorld(), startX + x, startY + y, startZ + z);
                        if (z == -10 && Math.abs(x) <= 2 && y < 2)
                            continue; // Geniş giriş

                        loc.getBlock().setType(Material.STRIPPED_OAK_LOG);
                    }
                }
                // Zemin
                new Location(center.getWorld(), startX + x, startY - 1, startZ + z).getBlock()
                        .setType(Material.COARSE_DIRT);
            }
        }

        // 2. Kapı Etiketi
        org.bukkit.Location gateLoc = new org.bukkit.Location(center.getWorld(), startX, startY + 3, startZ - 10.5);
        createLabel(gateLoc, ChatColor.GOLD + "🛖 " + ChatColor.YELLOW + clanName + " Karakolu");

        // 3. Merkez Bina (Büyük Taş Kule/Ev)
        for (int x = -4; x <= 4; x++) {
            for (int z = -4; z <= 4; z++) {
                for (int y = 0; y <= 6; y++) {
                    Location loc = new Location(center.getWorld(), startX + x, startY + y, startZ + z);
                    Block block = loc.getBlock();

                    if (y == 6) {
                        block.setType(Material.OAK_FENCE);
                    } else if (Math.abs(x) == 4 || Math.abs(z) == 4) {
                        if (z == -4 && Math.abs(x) <= 1 && y < 3) {
                            block.setType(Material.AIR);
                        } else {
                            block.setType(Material.COBBLESTONE);
                        }
                    } else if (y == 0) {
                        block.setType(Material.COBBLESTONE);
                    } else {
                        block.setType(Material.AIR);
                    }
                }
            }
        }

        // 4. Flama ve Etiket (Spawn Noktası)
        center.getBlock().setType(Material.WHITE_BANNER);
        createLabel(center.clone().add(0, 2, 0), ChatColor.WHITE + "🏳️ " + clanName + " Merkezi");
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
        return "Odun";
    }
}
