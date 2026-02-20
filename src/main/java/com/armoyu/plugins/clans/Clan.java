package com.armoyu.plugins.clans;

import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;
import java.util.*;

public class Clan {
    private UUID id;
    private String name;
    private String tag;
    private String description;
    private UUID leader;
    private final Set<UUID> members = new HashSet<>();
    private final Set<UUID> officers = new HashSet<>();
    private final Set<UUID> pendingInvites = new HashSet<>();
    private int level;
    private double balance;
    private Location spawn;
    private List<ItemStack> vault = new ArrayList<>();
    private String castleName;
    private String castleType;

    public void setId(UUID id) {
        this.id = id;
    }

    public void setVault(List<ItemStack> vault) {
        this.vault = vault;
    }

    public Clan(UUID leader, String name, String tag) {
        this.id = UUID.randomUUID();
        this.leader = leader;
        this.name = name;
        this.tag = tag;
        this.description = "Yeni bir klan!";
        this.level = 1;
        this.balance = 0;
        this.members.add(leader);
    }

    // Getters and Setters
    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public UUID getLeader() {
        return leader;
    }

    public void setLeader(UUID leader) {
        this.leader = leader;
    }

    public Set<UUID> getMembers() {
        return members;
    }

    public Set<UUID> getOfficers() {
        return officers;
    }

    public void addOfficer(UUID uuid) {
        officers.add(uuid);
    }

    public void removeOfficer(UUID uuid) {
        officers.remove(uuid);
    }

    public boolean isOfficer(UUID uuid) {
        return officers.contains(uuid) || leader.equals(uuid);
    }

    public Set<UUID> getPendingInvites() {
        return pendingInvites;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public double getBalance() {
        return balance;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }

    public Location getSpawn() {
        return spawn;
    }

    public void setSpawn(Location spawn) {
        this.spawn = spawn;
    }

    public List<ItemStack> getVault() {
        return vault;
    }

    public void addMember(UUID uuid) {
        members.add(uuid);
    }

    public void removeMember(UUID uuid) {
        members.remove(uuid);
    }

    public boolean isMember(UUID uuid) {
        return members.contains(uuid);
    }

    public void addBalance(double amount) {
        this.balance += amount;
    }

    public boolean removeBalance(double amount) {
        if (this.balance >= amount) {
            this.balance -= amount;
            return true;
        }
        return false;
    }

    public String getCastleName() {
        return castleName;
    }

    public void setCastleName(String castleName) {
        this.castleName = castleName;
    }

    public String getCastleType() {
        return castleType;
    }

    public void setCastleType(String castleType) {
        this.castleType = castleType;
    }
}
