package com.armoyu.plugins.sleeppingbed.listener;

import com.armoyu.minecraftplugin;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerBedEnterEvent;
import org.bukkit.event.player.PlayerBedLeaveEvent;

public class sleepingbed implements Listener {


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
