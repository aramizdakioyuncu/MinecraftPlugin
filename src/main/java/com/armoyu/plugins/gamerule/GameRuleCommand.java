package com.armoyu.plugins.gamerule;

import com.armoyu.plugins.actionmanager.ActionManager;
import com.armoyu.utils.PlayerRole;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Map;

public class GameRuleCommand implements CommandExecutor {

    private final GameRuleManager ruleManager;
    private final ActionManager actionManager;

    public GameRuleCommand(GameRuleManager ruleManager, ActionManager actionManager) {
        this.ruleManager = ruleManager;
        this.actionManager = actionManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Bu komutu sadece oyuncular kullanabilir.");
            return true;
        }

        Player player = (Player) sender;

        if (actionManager.getRole(player) != PlayerRole.ADMIN) {
            player.sendMessage(ChatColor.RED + "Bu komutu sadece adminler kullanabilir!");
            return true;
        }

        if (args.length == 0) {
            showAllRules(player);
            return true;
        }

        if (args.length == 1) {
            // Bir kuralın değerini göster
            String rule = args[0].toLowerCase();
            Map<String, Boolean> rules = ruleManager.getAllRules();
            if (rules.containsKey(rule)) {
                boolean value = rules.get(rule);
                player.sendMessage(ChatColor.GOLD + "Kural: " + ChatColor.YELLOW + rule
                        + ChatColor.GRAY + " = " + (value ? ChatColor.GREEN + "AÇIK" : ChatColor.RED + "KAPALI"));
            } else {
                player.sendMessage(ChatColor.RED + "Bilinmeyen kural: " + rule);
                player.sendMessage(ChatColor.GRAY + "Tüm kurallar için: /gr");
            }
            return true;
        }

        if (args.length == 2) {
            String rule = args[0].toLowerCase();
            String valueStr = args[1].toLowerCase();

            Map<String, Boolean> rules = ruleManager.getAllRules();
            if (!rules.containsKey(rule)) {
                player.sendMessage(ChatColor.RED + "Bilinmeyen kural: " + rule);
                return true;
            }

            boolean value;
            if (valueStr.equals("true") || valueStr.equals("açık") || valueStr.equals("on") || valueStr.equals("1")) {
                value = true;
            } else if (valueStr.equals("false") || valueStr.equals("kapalı") || valueStr.equals("off")
                    || valueStr.equals("0")) {
                value = false;
            } else {
                player.sendMessage(ChatColor.RED + "Değer true/false olmalı!");
                return true;
            }

            ruleManager.setRule(rule, value);
            player.sendMessage(ChatColor.GREEN + "✓ " + ChatColor.YELLOW + rule
                    + ChatColor.GREEN + " → " + (value ? ChatColor.GREEN + "AÇIK" : ChatColor.RED + "KAPALI"));
            return true;
        }

        player.sendMessage(ChatColor.RED + "Kullanım: /gr [kural] [true/false]");
        return true;
    }

    private void showAllRules(Player player) {
        Map<String, Boolean> rules = ruleManager.getAllRules();

        player.sendMessage(ChatColor.GOLD + "========= [ OYUN KURALLARI ] =========");

        player.sendMessage(ChatColor.RED + "» Patlamalar:");
        showRule(player, rules, "tnt_explosion", "TNT Patlaması");
        showRule(player, rules, "creeper_explosion", "Creeper Patlaması");
        showRule(player, rules, "wither_explosion", "Wither Patlaması");
        showRule(player, rules, "fireball_explosion", "Fireball Patlaması");
        showRule(player, rules, "bed_explosion", "Yatak Patlaması");

        player.sendMessage(ChatColor.DARK_GREEN + "» Moblar:");
        showRule(player, rules, "mob_item_drop", "Mob Eşya Düşürme");
        showRule(player, rules, "mob_grief", "Mob Tahribatı");
        showRule(player, rules, "enderman_grief", "Enderman Blok Alma");
        showRule(player, rules, "phantom_spawn", "Phantom Doğması");
        showRule(player, rules, "zombie_break_door", "Zombi Kapı Kırma");

        player.sendMessage(ChatColor.DARK_PURPLE + "» Mekanik:");
        showRule(player, rules, "piston", "Piston");
        showRule(player, rules, "fire_spread", "Ateş Yayılması");
        showRule(player, rules, "leaf_decay", "Yaprak Dökülmesi");
        showRule(player, rules, "ice_form", "Buz Oluşumu");
        showRule(player, rules, "snow_form", "Kar Oluşumu");
        showRule(player, rules, "coral_dry", "Mercan Kuruması");
        showRule(player, rules, "crop_trample", "Ekin Ezme");

        player.sendMessage(ChatColor.AQUA + "» Oyuncu:");
        showRule(player, rules, "pvp", "PvP");
        showRule(player, rules, "hunger", "Açlık");
        showRule(player, rules, "fall_damage", "Düşme Hasarı");
        showRule(player, rules, "drowning_damage", "Boğulma Hasarı");
        showRule(player, rules, "fire_damage", "Ateş Hasarı");
        showRule(player, rules, "keep_inventory", "Envanter Koruma");

        player.sendMessage(ChatColor.GOLD + "======================================");
        player.sendMessage(ChatColor.GRAY + "Değiştirmek için: /gr <kural> <true/false>");
    }

    private void showRule(Player player, Map<String, Boolean> rules, String key, String displayName) {
        boolean value = rules.getOrDefault(key, false);
        String status = value ? ChatColor.GREEN + "AÇIK" : ChatColor.RED + "KAPALI";

        net.md_5.bungee.api.chat.TextComponent message = new net.md_5.bungee.api.chat.TextComponent(
                "  " + ChatColor.YELLOW + displayName + ChatColor.GRAY + " (" + key + "): " + status + " ");

        // Tıklanabilir toggle butonu
        String newValue = value ? "false" : "true";
        net.md_5.bungee.api.chat.TextComponent toggle = new net.md_5.bungee.api.chat.TextComponent(
                value ? " [KAPAT] " : " [AÇ] ");
        toggle.setColor(value ? net.md_5.bungee.api.ChatColor.RED : net.md_5.bungee.api.ChatColor.GREEN);
        toggle.setBold(true);
        toggle.setClickEvent(new net.md_5.bungee.api.chat.ClickEvent(
                net.md_5.bungee.api.chat.ClickEvent.Action.RUN_COMMAND, "/gr " + key + " " + newValue));
        toggle.setHoverEvent(new net.md_5.bungee.api.chat.HoverEvent(
                net.md_5.bungee.api.chat.HoverEvent.Action.SHOW_TEXT,
                new net.md_5.bungee.api.chat.hover.content.Text("§eTıkla: " + key + " → " + newValue)));

        player.spigot().sendMessage(message, toggle);
    }
}
