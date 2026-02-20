package com.armoyu.plugins.gamerule;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.*;

public class GameRuleListener implements Listener {

    private final GameRuleManager ruleManager;

    public GameRuleListener(GameRuleManager ruleManager) {
        this.ruleManager = ruleManager;
    }

    // ═══════════════ PATLAMALAR ═══════════════

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onExplode(EntityExplodeEvent event) {
        Entity entity = event.getEntity();

        if (entity instanceof TNTPrimed && !ruleManager.isTntExplosion()) {
            event.blockList().clear();
            event.setCancelled(true);
            return;
        }
        if (entity instanceof Creeper && !ruleManager.isCreeperExplosion()) {
            event.blockList().clear();
            event.setCancelled(true);
            return;
        }
        if (entity instanceof Wither && !ruleManager.isWitherExplosion()) {
            event.blockList().clear();
            event.setCancelled(true);
            return;
        }
        if (entity instanceof WitherSkull && !ruleManager.isWitherExplosion()) {
            event.blockList().clear();
            event.setCancelled(true);
            return;
        }
        if (entity instanceof Fireball && !ruleManager.isFireballExplosion()) {
            event.blockList().clear();
            event.setCancelled(true);
            return;
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockExplode(BlockExplodeEvent event) {
        // Yatak / Respawn Anchor patlaması
        if (!ruleManager.isBedExplosion()) {
            event.blockList().clear();
            event.setCancelled(true);
        }
    }

    // ═══════════════ MOB ═══════════════

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntityDeath(EntityDeathEvent event) {
        if (!(event.getEntity() instanceof Player) && !ruleManager.isMobItemDrop()) {
            event.getDrops().clear();
            event.setDroppedExp(0);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntityChangeBlock(EntityChangeBlockEvent event) {
        Entity entity = event.getEntity();

        // Enderman blok kaldırma
        if (entity instanceof Enderman && !ruleManager.isEndermanGrief()) {
            event.setCancelled(true);
            return;
        }

        // Genel mob grief (Wither, Ravager, vs.)
        if (!(entity instanceof Player) && !ruleManager.isMobGrief()) {
            // Ekin ezme hariç (ayrı kontrol ediliyor)
            if (event.getBlock().getType() != Material.FARMLAND) {
                event.setCancelled(true);
            }
        }

        // Ekin ezme (crop trample)
        if (event.getBlock().getType() == Material.FARMLAND && !ruleManager.isCropTrample()) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onCreatureSpawn(CreatureSpawnEvent event) {
        if (event.getEntityType() == EntityType.PHANTOM && !ruleManager.isPhantomSpawn()) {
            event.setCancelled(true);
        }
    }

    // Zombi kapı kırma
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntityBreakDoor(EntityBreakDoorEvent event) {
        if (!ruleManager.isZombieBreakDoor()) {
            event.setCancelled(true);
        }
    }

    // ═══════════════ REDSTONE / MEKANİK ═══════════════

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPistonExtend(BlockPistonExtendEvent event) {
        if (!ruleManager.isPistonEnabled()) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPistonRetract(BlockPistonRetractEvent event) {
        if (!ruleManager.isPistonEnabled()) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onFireSpread(BlockSpreadEvent event) {
        if (event.getSource().getType() == Material.FIRE && !ruleManager.isFireSpread()) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockBurn(BlockBurnEvent event) {
        if (!ruleManager.isFireSpread()) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockFade(BlockFadeEvent event) {
        Material type = event.getBlock().getType();

        // Buz erime
        if ((type == Material.ICE || type == Material.FROSTED_ICE) && ruleManager.isIceForm()) {
            // Ice form true ise erimeyi engelle (buz korunur)
        }

        // Mercan kuruması
        if (type.name().contains("CORAL") && !ruleManager.isCoralDry()) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onLeafDecay(LeavesDecayEvent event) {
        if (!ruleManager.isLeafDecay()) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockForm(BlockFormEvent event) {
        Material type = event.getNewState().getType();

        if ((type == Material.ICE || type == Material.FROSTED_ICE) && !ruleManager.isIceForm()) {
            event.setCancelled(true);
        }

        if ((type == Material.SNOW || type == Material.SNOW_BLOCK) && !ruleManager.isSnowForm()) {
            event.setCancelled(true);
        }
    }

    // ═══════════════ OYUNCU ═══════════════

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        // PvP kontrolü
        if (event.getEntity() instanceof Player && event.getDamager() instanceof Player) {
            if (!ruleManager.isPvpEnabled()) {
                event.setCancelled(true);
                event.getDamager().sendMessage(ChatColor.RED + "PvP kapalı!");
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player))
            return;

        EntityDamageEvent.DamageCause cause = event.getCause();

        if (cause == EntityDamageEvent.DamageCause.FALL && !ruleManager.isFallDamage()) {
            event.setCancelled(true);
        }

        if (cause == EntityDamageEvent.DamageCause.DROWNING && !ruleManager.isDrowningDamage()) {
            event.setCancelled(true);
        }

        if ((cause == EntityDamageEvent.DamageCause.FIRE || cause == EntityDamageEvent.DamageCause.FIRE_TICK
                || cause == EntityDamageEvent.DamageCause.LAVA) && !ruleManager.isFireDamage()) {
            event.setCancelled(true);
        }

        if (cause == EntityDamageEvent.DamageCause.STARVATION && !ruleManager.isHungerEnabled()) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onFoodLevelChange(FoodLevelChangeEvent event) {
        if (!ruleManager.isHungerEnabled()) {
            event.setCancelled(true);
            if (event.getEntity() instanceof Player) {
                ((Player) event.getEntity()).setFoodLevel(20);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerDeath(PlayerDeathEvent event) {
        if (ruleManager.isKeepInventory()) {
            event.setKeepInventory(true);
            event.getDrops().clear();
            event.setKeepLevel(true);
            event.setDroppedExp(0);
        }
    }
}
