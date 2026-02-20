package com.armoyu.plugins.ui;

import com.armoyu.plugins.economy.MoneyManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;

public class ScoreManager {

    private final MoneyManager moneyManager;
    private final com.armoyu.plugins.clans.ClanManager clanManager;

    public ScoreManager(MoneyManager moneyManager, com.armoyu.plugins.clans.ClanManager clanManager) {
        this.moneyManager = moneyManager;
        this.clanManager = clanManager;
    }

    public void setScoreboard(Player player) {
        ScoreboardManager manager = Bukkit.getScoreboardManager();
        if (manager == null)
            return;

        Scoreboard board = manager.getNewScoreboard();
        Objective objective = board.registerNewObjective("armosu", "dummy",
                ChatColor.GOLD + "" + ChatColor.BOLD + " ARMOYU ");
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);

        updateScores(player, objective);
        player.setScoreboard(board);
    }

    public void updateScoreboard(Player player) {
        Scoreboard board = player.getScoreboard();
        Objective objective = board.getObjective("armosu");

        if (objective == null) {
            setScoreboard(player);
            return;
        }

        // Skorları temizleyip yeniden ekleyelim (Basit yöntem)
        for (String entry : board.getEntries()) {
            board.resetScores(entry);
        }

        updateScores(player, objective);
    }

    private void updateScores(Player player, Objective objective) {
        objective.getScore(ChatColor.DARK_GRAY + "----------------").setScore(8);

        com.armoyu.plugins.clans.Clan clan = clanManager.getClanByPlayer(player.getUniqueId());
        String nameLine = ChatColor.YELLOW + "Oyuncu: " + ChatColor.WHITE + player.getName();
        if (clan != null) {
            nameLine += ChatColor.GRAY + " [" + ChatColor.GOLD + clan.getTag() + ChatColor.GRAY + "]";
        }
        objective.getScore(nameLine).setScore(7);

        objective.getScore(" ").setScore(6);
        objective.getScore(ChatColor.YELLOW + "Bakiye:").setScore(5);
        objective.getScore(ChatColor.GREEN + "" + moneyManager.getBalance(player.getUniqueId()) + " ARMO").setScore(4);
        objective.getScore("  ").setScore(3);
        objective.getScore(ChatColor.AQUA + "armoyu.com").setScore(2);
        objective.getScore(ChatColor.DARK_GRAY + "-----------------").setScore(1);
    }
}
