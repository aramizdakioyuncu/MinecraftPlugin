package com.armoyu.plugins.structures;

import org.bukkit.Location;

public interface IStructure {
    void place(org.bukkit.Location center, String clanName);

    String getName();
}
