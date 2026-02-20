package com.armoyu.plugins.clans;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;

public class ClanUtils {

    public static void updateLocationVisual(Clan clan, Location loc, String suffix) {
        if (loc == null)
            return;

        Location targetLoc = loc.clone().add(0, 2.0, 0);

        // Remove existing armor stands nearby
        targetLoc.getWorld().getNearbyEntities(targetLoc, 2, 3, 2).stream()
                .filter(e -> e instanceof ArmorStand)
                .filter(e -> e.getCustomName() != null && e.getCustomName().contains(suffix))
                .forEach(org.bukkit.entity.Entity::remove);

        ArmorStand as = targetLoc.getWorld().spawn(targetLoc, ArmorStand.class);
        as.setVisible(false);
        as.setGravity(false);
        as.setBasePlate(false);
        as.setArms(false);
        as.setSmall(true);
        as.setCustomName(
                ChatColor.GOLD + "[" + ChatColor.YELLOW + clan.getName() + " " + suffix + ChatColor.GOLD + "]");
        as.setCustomNameVisible(true);
        as.setMarker(true);
    }
}
