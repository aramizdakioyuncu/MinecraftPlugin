package com.armoyu.plugins.clans;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

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
        updateTabName(event.getPlayer());
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
    }
}
