package com.armoyu.plugins.claims;

import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class ClaimManager {
    private final List<Claim> claims = new ArrayList<>();
    private final Map<UUID, Location[]> selections = new HashMap<>(); // UUID -> [Point A, Point B]
    private final File file;
    private final FileConfiguration config;

    public ClaimManager(JavaPlugin plugin) {
        this.file = new File(plugin.getDataFolder(), "claims.yml");
        this.config = YamlConfiguration.loadConfiguration(file);
        loadClaims();
    }

    public void setSelection(UUID uuid, int pointIndex, Location loc) {
        Location[] points = selections.getOrDefault(uuid, new Location[2]);
        points[pointIndex] = loc;
        selections.put(uuid, points);
    }

    public Location[] getSelection(UUID uuid) {
        return selections.get(uuid);
    }

    public Claim createClaim(UUID ownerPlayer, UUID ownerClan, Location loc1, Location loc2) {
        if (!loc1.getWorld().getName().equals(loc2.getWorld().getName()))
            return null;

        Claim newClaim = new Claim(ownerPlayer, ownerClan, loc1.getWorld().getName(),
                loc1.getBlockX(), loc2.getBlockX(),
                loc1.getBlockZ(), loc2.getBlockZ());

        // Overlap check
        for (Claim c : claims) {
            if (isOverlapping(newClaim, c))
                return null;
        }

        claims.add(newClaim);
        saveClaims();
        return newClaim;
    }

    private boolean isOverlapping(Claim c1, Claim c2) {
        if (!c1.getWorld().equals(c2.getWorld()))
            return false;
        return c1.getMinX() <= c2.getMaxX() && c1.getMaxX() >= c2.getMinX() &&
                c1.getMinZ() <= c2.getMaxZ() && c1.getMaxZ() >= c2.getMinZ();
    }

    public Claim getClaimAt(Location loc) {
        for (Claim c : claims) {
            if (c.isInside(loc.getWorld().getName(), loc.getBlockX(), loc.getBlockZ())) {
                return c;
            }
        }
        return null;
    }

    public List<Claim> getClaimsByPlayer(UUID playerUUID) {
        List<Claim> result = new ArrayList<>();
        for (Claim c : claims) {
            if (c.getOwnerPlayer() != null && c.getOwnerPlayer().equals(playerUUID)) {
                result.add(c);
            }
        }
        return result;
    }

    public List<Claim> getClaimsByClan(UUID clanUUID) {
        List<Claim> result = new ArrayList<>();
        for (Claim c : claims) {
            if (c.getOwnerClan() != null && c.getOwnerClan().equals(clanUUID)) {
                result.add(c);
            }
        }
        return result;
    }

    public void deleteClaim(Claim claim) {
        claims.remove(claim);
        saveClaims();
    }

    public void saveClaims() {
        config.set("claims", null);
        for (int i = 0; i < claims.size(); i++) {
            Claim c = claims.get(i);
            String path = "claims." + i;
            config.set(path + ".world", c.getWorld());
            config.set(path + ".minX", c.getMinX());
            config.set(path + ".maxX", c.getMaxX());
            config.set(path + ".minZ", c.getMinZ());
            config.set(path + ".maxZ", c.getMaxZ());
            config.set(path + ".ownerPlayer", c.getOwnerPlayer() != null ? c.getOwnerPlayer().toString() : null);
            config.set(path + ".ownerClan", c.getOwnerClan() != null ? c.getOwnerClan().toString() : null);
            config.set(path + ".trusted", c.getTrustedPlayers().stream().map(UUID::toString).toList());
        }
        try {
            config.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadClaims() {
        if (!config.contains("claims"))
            return;
        for (String key : config.getConfigurationSection("claims").getKeys(false)) {
            String path = "claims." + key;
            String world = config.getString(path + ".world");
            int minX = config.getInt(path + ".minX");
            int maxX = config.getInt(path + ".maxX");
            int minZ = config.getInt(path + ".minZ");
            int maxZ = config.getInt(path + ".maxZ");
            String ownerP = config.getString(path + ".ownerPlayer");
            String ownerC = config.getString(path + ".ownerClan");
            List<String> trusted = config.getStringList(path + ".trusted");

            UUID pId = (ownerP != null) ? UUID.fromString(ownerP) : null;
            UUID cId = (ownerC != null) ? UUID.fromString(ownerC) : null;

            Claim c = new Claim(pId, cId, world, minX, maxX, minZ, maxZ);
            for (String t : trusted) {
                c.getTrustedPlayers().add(UUID.fromString(t));
            }
            claims.add(c);
        }
    }
}
