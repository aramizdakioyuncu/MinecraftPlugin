package com.armoyu;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerBedEnterEvent;
import org.bukkit.event.player.PlayerBedLeaveEvent;
import org.bukkit.event.player.PlayerMoveEvent;

import java.util.HashSet;
import java.util.Set;

public class listener implements Listener {


    // Yatağa yatan oyuncuları tutmak için bir set

    @EventHandler
    public void onMove(PlayerMoveEvent Event) {
        Player p = Event.getPlayer();

        if (p.getName().equals("pandora")) {
            minecraftplugin.consoleSendMessage(p.getDisplayName() + " kullanıcı kısıtlandı");
            Event.setCancelled(true);
            return;
        }
        Event.setCancelled(false);
    }

    @EventHandler
    public void enterBed(PlayerBedEnterEvent Event) {
        Player p = Event.getPlayer();

        long time = p.getWorld().getTime(); // Dünyanın zamanını al (0-23999)
        // Eğer gece vaktiyse (12000-23999 arası gece sayılır)
        if (!(time >= 12000 && time <= 23999)) {
            return;
        }

        minecraftplugin.playersInBed.add(p);


        int currentplayermembercount = Bukkit.getOnlinePlayers().size();
        double oran = ((double) minecraftplugin.playersInBed.size() * 100 / (double) currentplayermembercount);
        for (Player player : minecraftplugin.playersInBed) {
            minecraftplugin.sendActionBarMessage(player, "(%" + oran + ")");
        }
    }

    @EventHandler
    public void BedLeave(PlayerBedLeaveEvent Event) {
        Player p = Event.getPlayer();

        minecraftplugin.playersInBed.remove(p);


    }

}
