package com.armoyu.plugins.clans;

import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class ClanManager {

    private final Map<UUID, Clan> clans = new HashMap<>();
    private final Map<UUID, UUID> playerClanMap = new HashMap<>(); // Player UUID -> Clan UUID
    private final File file;
    private final FileConfiguration config;

    public ClanManager(JavaPlugin plugin) {
        this.file = new File(plugin.getDataFolder(), "clans.yml");
        this.config = YamlConfiguration.loadConfiguration(file);
        loadClans();
    }

    public Clan createClan(Player leader, String name, String tag) {
        if (getClanByPlayer(leader.getUniqueId()) != null)
            return null;

        Clan clan = new Clan(leader.getUniqueId(), name, tag);
        clans.put(clan.getId(), clan);
        playerClanMap.put(leader.getUniqueId(), clan.getId());
        saveClans();
        return clan;
    }

    public Clan getClan(UUID clanId) {
        return clans.get(clanId);
    }

    public Clan getClanByPlayer(UUID playerUuid) {
        UUID clanId = playerClanMap.get(playerUuid);
        return clanId != null ? clans.get(clanId) : null;
    }

    public Clan getClanByName(String name) {
        return clans.values().stream()
                .filter(c -> c.getName().equalsIgnoreCase(name))
                .findFirst()
                .orElse(null);
    }

    public void addMember(Clan clan, Player player) {
        clan.addMember(player.getUniqueId());
        playerClanMap.put(player.getUniqueId(), clan.getId());
        saveClans();
    }

    public void removeMember(Player player) {
        Clan clan = getClanByPlayer(player.getUniqueId());
        if (clan != null) {
            clan.removeMember(player.getUniqueId());
            playerClanMap.remove(player.getUniqueId());
            if (clan.getMembers().isEmpty()) {
                clans.remove(clan.getId());
            } else if (clan.getLeader().equals(player.getUniqueId())) {
                // Lider ayrılırsa rastgele birine devret (veya klanı sil)
                UUID nextLeader = clan.getMembers().iterator().next();
                clan.setLeader(nextLeader);
            }
            saveClans();
        }
    }

    public void deleteClan(Clan clan) {
        for (UUID member : clan.getMembers()) {
            playerClanMap.remove(member);
        }
        clans.remove(clan.getId());
        saveClans();
    }

    public Collection<Clan> getAllClans() {
        return clans.values();
    }

    public Clan getClanById(UUID id) {
        return clans.get(id);
    }

    public void saveClans() {
        config.set("clans", null); // Clear existing
        for (Clan clan : clans.values()) {
            String path = "clans." + clan.getId().toString();
            config.set(path + ".name", clan.getName());
            config.set(path + ".tag", clan.getTag());
            config.set(path + ".leader", clan.getLeader().toString());
            config.set(path + ".level", clan.getLevel());
            config.set(path + ".balance", clan.getBalance());
            config.set(path + ".members", clan.getMembers().stream().map(UUID::toString).toList());
            config.set(path + ".officers", clan.getOfficers().stream().map(UUID::toString).toList());
            if (clan.getSpawn() != null) {
                config.set(path + ".spawn", clan.getSpawn());
            }
            config.set(path + ".vault", clan.getVault());
            config.set(path + ".castleName", clan.getCastleName());
            config.set(path + ".castleType", clan.getCastleType());
        }
        try {
            config.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadClans() {
        if (!config.contains("clans"))
            return;
        for (String key : config.getConfigurationSection("clans").getKeys(false)) {
            UUID id = UUID.fromString(key);
            String path = "clans." + key;
            String name = config.getString(path + ".name");
            String tag = config.getString(path + ".tag");
            UUID leader = UUID.fromString(config.getString(path + ".leader"));
            int level = config.getInt(path + ".level");
            double balance = config.getDouble(path + ".balance");
            List<String> memberStrings = config.getStringList(path + ".members");
            List<String> officerStrings = config.getStringList(path + ".officers");
            Location spawn = config.getLocation(path + ".spawn");
            List<ItemStack> vault = (List<ItemStack>) config.get(path + ".vault");

            Clan clan = new Clan(leader, name, tag);
            clan.setId(id);
            clan.setLevel(level);
            clan.setBalance(balance);
            clan.setSpawn(spawn);
            if (vault != null) {
                clan.setVault(new ArrayList<>(vault));
            }
            clan.setCastleName(config.getString(path + ".castleName"));
            clan.setCastleType(config.getString(path + ".castleType"));

            for (String m : memberStrings) {
                UUID memberId = UUID.fromString(m);
                clan.addMember(memberId);
                playerClanMap.put(memberId, id);
            }
            for (String o : officerStrings) {
                clan.addOfficer(UUID.fromString(o));
            }
            clans.put(id, clan);
        }
    }
}
