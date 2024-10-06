package com.armoyu;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

public class listener implements Listener {


    @EventHandler
    public void onMove(PlayerMoveEvent Event) {
        Player p = Event.getPlayer();

        minecraftplugin.consoleSendMessage(p.getDisplayName() + " hareket etme eventini tetikledi");
        if(p.getName().equals("pandora")){
            Event.setCancelled(false);
            return;
        }
        Event.setCancelled(true);

    }

}
