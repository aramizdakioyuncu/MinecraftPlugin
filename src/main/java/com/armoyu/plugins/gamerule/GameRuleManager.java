package com.armoyu.plugins.gamerule;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public class GameRuleManager {

    private final JavaPlugin plugin;

    // Patlama / Hasar
    private boolean tntExplosion;
    private boolean creeperExplosion;
    private boolean witherExplosion;
    private boolean fireballExplosion;
    private boolean bedExplosion;

    // Mob
    private boolean mobItemDrop;
    private boolean mobGrief;
    private boolean endermanGrief;
    private boolean phantomSpawn;
    private boolean zombieBreakDoor;

    // Redstone / Mekanik
    private boolean pistonEnabled;
    private boolean fireSpread;
    private boolean leafDecay;
    private boolean iceForm;
    private boolean snowForm;
    private boolean coralDry;
    private boolean cropTrample;

    // Oyuncu
    private boolean pvpEnabled;
    private boolean hungerEnabled;
    private boolean fallDamage;
    private boolean drowningDamage;
    private boolean fireDamage;
    private boolean keepInventory;

    public GameRuleManager(JavaPlugin plugin) {
        this.plugin = plugin;
        loadRules();
    }

    public void loadRules() {
        FileConfiguration config = plugin.getConfig();

        // Varsayılan değerleri ayarla (yoksa)
        setDefault(config, "gamerules.tnt_explosion", false);
        setDefault(config, "gamerules.creeper_explosion", false);
        setDefault(config, "gamerules.wither_explosion", false);
        setDefault(config, "gamerules.fireball_explosion", false);
        setDefault(config, "gamerules.bed_explosion", false);

        setDefault(config, "gamerules.mob_item_drop", false);
        setDefault(config, "gamerules.mob_grief", false);
        setDefault(config, "gamerules.enderman_grief", false);
        setDefault(config, "gamerules.phantom_spawn", false);
        setDefault(config, "gamerules.zombie_break_door", false);

        setDefault(config, "gamerules.piston", false);
        setDefault(config, "gamerules.fire_spread", false);
        setDefault(config, "gamerules.leaf_decay", true);
        setDefault(config, "gamerules.ice_form", true);
        setDefault(config, "gamerules.snow_form", true);
        setDefault(config, "gamerules.coral_dry", false);
        setDefault(config, "gamerules.crop_trample", false);

        setDefault(config, "gamerules.pvp", true);
        setDefault(config, "gamerules.hunger", true);
        setDefault(config, "gamerules.fall_damage", true);
        setDefault(config, "gamerules.drowning_damage", true);
        setDefault(config, "gamerules.fire_damage", true);
        setDefault(config, "gamerules.keep_inventory", false);

        plugin.saveConfig();

        // Değerleri oku
        tntExplosion = config.getBoolean("gamerules.tnt_explosion");
        creeperExplosion = config.getBoolean("gamerules.creeper_explosion");
        witherExplosion = config.getBoolean("gamerules.wither_explosion");
        fireballExplosion = config.getBoolean("gamerules.fireball_explosion");
        bedExplosion = config.getBoolean("gamerules.bed_explosion");

        mobItemDrop = config.getBoolean("gamerules.mob_item_drop");
        mobGrief = config.getBoolean("gamerules.mob_grief");
        endermanGrief = config.getBoolean("gamerules.enderman_grief");
        phantomSpawn = config.getBoolean("gamerules.phantom_spawn");
        zombieBreakDoor = config.getBoolean("gamerules.zombie_break_door");

        pistonEnabled = config.getBoolean("gamerules.piston");
        fireSpread = config.getBoolean("gamerules.fire_spread");
        leafDecay = config.getBoolean("gamerules.leaf_decay");
        iceForm = config.getBoolean("gamerules.ice_form");
        snowForm = config.getBoolean("gamerules.snow_form");
        coralDry = config.getBoolean("gamerules.coral_dry");
        cropTrample = config.getBoolean("gamerules.crop_trample");

        pvpEnabled = config.getBoolean("gamerules.pvp");
        hungerEnabled = config.getBoolean("gamerules.hunger");
        fallDamage = config.getBoolean("gamerules.fall_damage");
        drowningDamage = config.getBoolean("gamerules.drowning_damage");
        fireDamage = config.getBoolean("gamerules.fire_damage");
        keepInventory = config.getBoolean("gamerules.keep_inventory");
    }

    public void setRule(String rule, boolean value) {
        plugin.getConfig().set("gamerules." + rule, value);
        plugin.saveConfig();
        loadRules();
    }

    public boolean getRule(String rule) {
        return plugin.getConfig().getBoolean("gamerules." + rule, false);
    }

    private void setDefault(FileConfiguration config, String path, boolean value) {
        if (!config.contains(path)) {
            config.set(path, value);
        }
    }

    // Getters
    public boolean isTntExplosion() {
        return tntExplosion;
    }

    public boolean isCreeperExplosion() {
        return creeperExplosion;
    }

    public boolean isWitherExplosion() {
        return witherExplosion;
    }

    public boolean isFireballExplosion() {
        return fireballExplosion;
    }

    public boolean isBedExplosion() {
        return bedExplosion;
    }

    public boolean isMobItemDrop() {
        return mobItemDrop;
    }

    public boolean isMobGrief() {
        return mobGrief;
    }

    public boolean isEndermanGrief() {
        return endermanGrief;
    }

    public boolean isPhantomSpawn() {
        return phantomSpawn;
    }

    public boolean isZombieBreakDoor() {
        return zombieBreakDoor;
    }

    public boolean isPistonEnabled() {
        return pistonEnabled;
    }

    public boolean isFireSpread() {
        return fireSpread;
    }

    public boolean isLeafDecay() {
        return leafDecay;
    }

    public boolean isIceForm() {
        return iceForm;
    }

    public boolean isSnowForm() {
        return snowForm;
    }

    public boolean isCoralDry() {
        return coralDry;
    }

    public boolean isCropTrample() {
        return cropTrample;
    }

    public boolean isPvpEnabled() {
        return pvpEnabled;
    }

    public boolean isHungerEnabled() {
        return hungerEnabled;
    }

    public boolean isFallDamage() {
        return fallDamage;
    }

    public boolean isDrowningDamage() {
        return drowningDamage;
    }

    public boolean isFireDamage() {
        return fireDamage;
    }

    public boolean isKeepInventory() {
        return keepInventory;
    }

    // Tüm kuralları isim-değer olarak döndür
    public java.util.Map<String, Boolean> getAllRules() {
        java.util.LinkedHashMap<String, Boolean> map = new java.util.LinkedHashMap<>();
        map.put("tnt_explosion", tntExplosion);
        map.put("creeper_explosion", creeperExplosion);
        map.put("wither_explosion", witherExplosion);
        map.put("fireball_explosion", fireballExplosion);
        map.put("bed_explosion", bedExplosion);
        map.put("mob_item_drop", mobItemDrop);
        map.put("mob_grief", mobGrief);
        map.put("enderman_grief", endermanGrief);
        map.put("phantom_spawn", phantomSpawn);
        map.put("zombie_break_door", zombieBreakDoor);
        map.put("piston", pistonEnabled);
        map.put("fire_spread", fireSpread);
        map.put("leaf_decay", leafDecay);
        map.put("ice_form", iceForm);
        map.put("snow_form", snowForm);
        map.put("coral_dry", coralDry);
        map.put("crop_trample", cropTrample);
        map.put("pvp", pvpEnabled);
        map.put("hunger", hungerEnabled);
        map.put("fall_damage", fallDamage);
        map.put("drowning_damage", drowningDamage);
        map.put("fire_damage", fireDamage);
        map.put("keep_inventory", keepInventory);
        return map;
    }
}
