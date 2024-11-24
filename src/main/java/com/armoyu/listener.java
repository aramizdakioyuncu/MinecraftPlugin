package com.armoyu;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerBedEnterEvent;
import org.bukkit.event.player.PlayerMoveEvent;

public class listener implements Listener {
    // Yatağa yatan oyuncuları tutmak için bir set
    //private final Set<Player> playersInBed = new HashSet<>();

    @EventHandler
    public void onMove(PlayerMoveEvent Event) {
        Player p = Event.getPlayer();

        if(p.getName().equals("pandora")){
            minecraftplugin.consoleSendMessage(p.getDisplayName() + " kullanıcı kısıtlandı");
            Event.setCancelled(true);
            return;
        }
        Event.setCancelled(false);
    }

    @EventHandler
    public void enterBed(PlayerBedEnterEvent Event){
        Player p = Event.getPlayer();

        p.setFoodLevel(1);
        p.setHealth(1);
    }

}
