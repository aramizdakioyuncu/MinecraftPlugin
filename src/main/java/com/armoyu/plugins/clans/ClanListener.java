package com.armoyu.plugins.clans;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.scoreboard.Team;

public class ClanListener implements Listener {

    private final ClanManager clanManager;

    public ClanListener(ClanManager clanManager) {
        this.clanManager = clanManager;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        Clan clan = clanManager.getClanByPlayer(player.getUniqueId());

        if (clan != null) {
            String tag = ChatColor.GRAY + "[" + ChatColor.GOLD + clan.getTag() + ChatColor.GRAY + "] ";
            event.setFormat(tag + ChatColor.WHITE + "%1$s: %2$s");
        } else {
            event.setFormat(ChatColor.WHITE + "%1$s: %2$s");
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        // Kendi tag'ini güncelle
        updateTabName(event.getPlayer());
        // Diğer oyuncuların tag'lerini de bu oyuncuya göster
        for (Player online : Bukkit.getOnlinePlayers()) {
            updateNameTag(online);
        }
    }

    @EventHandler
    public void onDamage(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player) || !(event.getEntity() instanceof Player)) {
            return;
        }

        Player damager = (Player) event.getDamager();
        Player victim = (Player) event.getEntity();

        Clan damagerClan = clanManager.getClanByPlayer(damager.getUniqueId());
        Clan victimClan = clanManager.getClanByPlayer(victim.getUniqueId());

        if (damagerClan != null && victimClan != null && damagerClan.getId().equals(victimClan.getId())) {
            event.setCancelled(true);
            damager.sendMessage(ChatColor.RED + "Klan arkadaşlarınıza hasar veremezsiniz!");
        }
    }

    public void updateTabName(Player player) {
        Clan clan = clanManager.getClanByPlayer(player.getUniqueId());
        if (clan != null) {
            String tabName = ChatColor.GRAY + "[" + ChatColor.GOLD + clan.getTag() + ChatColor.GRAY + "] "
                    + ChatColor.WHITE + player.getName();
            player.setPlayerListName(tabName);
            player.setDisplayName(player.getName());
        } else {
            player.setPlayerListName(player.getName());
            player.setDisplayName(player.getName());
        }
        updateNameTag(player);
    }

    public void updateNameTag(Player player) {
        Clan clan = clanManager.getClanByPlayer(player.getUniqueId());

        // Her online oyuncunun scoreboard'una bu oyuncu için team ekle
        for (Player observer : Bukkit.getOnlinePlayers()) {
            org.bukkit.scoreboard.Scoreboard board = observer.getScoreboard();
            if (board == null)
                continue;

            // Önceki takımdan çıkar
            for (Team team : board.getTeams()) {
                if (team.getName().startsWith("clan_") && team.hasEntry(player.getName())) {
                    team.removeEntry(player.getName());
                }
            }

            if (clan != null) {
                String teamName = "clan_" + clan.getId().toString().substring(0, 8);
                Team team = board.getTeam(teamName);
                if (team == null) {
                    team = board.registerNewTeam(teamName);
                }

                String prefix = ChatColor.GRAY + "[" + ChatColor.GOLD + clan.getTag() + ChatColor.GRAY + "] "
                        + ChatColor.WHITE;
                team.setPrefix(prefix);
                team.setColor(ChatColor.WHITE);

                if (!team.hasEntry(player.getName())) {
                    team.addEntry(player.getName());
                }
            }
        }
    }

    // Tüm oyuncuların tag'lerini güncelle (klan oluşturma/silme/katılma sonrası
    // çağrılır)
    public void refreshAllTags() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            updateTabName(player);
        }
    }
}
