package com.armoyu.plugins.structures;

import com.armoyu.plugins.structures.impl.CastleClassic;
import com.armoyu.plugins.structures.impl.CastleWood;
import org.bukkit.Location;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class StructureManager {
    private final Map<String, IStructure> structures = new HashMap<>();

    public StructureManager() {
        IStructure classic = new CastleClassic();
        IStructure wood = new CastleWood();

        registerStructure("klasik", classic);
        registerStructure("1", classic);

        registerStructure("odun", wood);
        registerStructure("2", wood);
    }

    public void registerStructure(String key, IStructure structure) {
        structures.put(key.toLowerCase(), structure);
    }

    public void placeCastle(Location center, String type, String clanName) {
        IStructure structure = structures.get(type.toLowerCase());
        if (structure != null) {
            structure.place(center, clanName);
        } else {
            // Default to classic if not found
            structures.get("klasik").place(center, clanName);
        }
    }

    public Set<String> getAvailableTypes() {
        return structures.keySet();
    }
}
