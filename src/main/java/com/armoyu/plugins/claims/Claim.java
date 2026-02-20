package com.armoyu.plugins.claims;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class Claim {
    private final UUID id;
    private final String world;
    private final int minX, maxX, minZ, maxZ;
    private final UUID ownerPlayer; // Null if clan claim
    private final UUID ownerClan; // Null if player claim
    private final Set<UUID> trustedPlayers = new HashSet<>();

    public Claim(UUID ownerPlayer, UUID ownerClan, String world, int x1, int x2, int z1, int z2) {
        this.id = UUID.randomUUID();
        this.ownerPlayer = ownerPlayer;
        this.ownerClan = ownerClan;
        this.world = world;
        this.minX = Math.min(x1, x2);
        this.maxX = Math.max(x1, x2);
        this.minZ = Math.min(z1, z2);
        this.maxZ = Math.max(z1, z2);
    }

    public UUID getId() {
        return id;
    }

    public String getWorld() {
        return world;
    }

    public int getMinX() {
        return minX;
    }

    public int getMaxX() {
        return maxX;
    }

    public int getMinZ() {
        return minZ;
    }

    public int getMaxZ() {
        return maxZ;
    }

    public UUID getOwnerPlayer() {
        return ownerPlayer;
    }

    public UUID getOwnerClan() {
        return ownerClan;
    }

    public Set<UUID> getTrustedPlayers() {
        return trustedPlayers;
    }

    public boolean isInside(String world, int x, int z) {
        return this.world.equals(world) && x >= minX && x <= maxX && z >= minZ && z <= maxZ;
    }

    public boolean isOwner(UUID uuid) {
        return (ownerPlayer != null && ownerPlayer.equals(uuid));
    }

    public boolean isTrusted(UUID uuid) {
        return isOwner(uuid) || trustedPlayers.contains(uuid);
    }

    public void addTrust(UUID uuid) {
        trustedPlayers.add(uuid);
    }

    public void removeTrust(UUID uuid) {
        trustedPlayers.remove(uuid);
    }
}
