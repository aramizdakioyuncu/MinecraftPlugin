package com.armoyu.plugins.sleeppingbed;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerBedEnterEvent;
import org.bukkit.event.player.PlayerBedLeaveEvent;

public class SleepingBedListener implements Listener {

    private final SleepingBedManager sleepingBedManager;

    public SleepingBedListener(SleepingBedManager sleepingBedManager) {
        this.sleepingBedManager = sleepingBedManager;
    }

    @EventHandler
    public void onPlayerEnterBed(PlayerBedEnterEvent event) {
        if (event.getBedEnterResult() == PlayerBedEnterEvent.BedEnterResult.OK) {
            sleepingBedManager.addPlayer(event.getPlayer());
        }
    }

    @EventHandler
    public void onPlayerLeaveBed(PlayerBedLeaveEvent event) {
        sleepingBedManager.removePlayer(event.getPlayer());
    }
}
