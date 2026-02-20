package com.armoyu.plugins.ui;

import com.armoyu.plugins.economy.MoneyManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;
import org.bukkit.scoreboard.Team;

public class ScoreManager {

    private final MoneyManager moneyManager;
    private final com.armoyu.plugins.clans.ClanManager clanManager;

    // Her satır için benzersiz görünmez entry'ler (ChatColor kodları)
    private static final String[] LINE_ENTRIES = {
            ChatColor.BLACK + "" + ChatColor.RESET,
            ChatColor.DARK_BLUE + "" + ChatColor.RESET,
            ChatColor.DARK_GREEN + "" + ChatColor.RESET,
            ChatColor.DARK_AQUA + "" + ChatColor.RESET,
            ChatColor.DARK_RED + "" + ChatColor.RESET,
            ChatColor.DARK_PURPLE + "" + ChatColor.RESET,
            ChatColor.GOLD + "" + ChatColor.RESET,
            ChatColor.GRAY + "" + ChatColor.RESET,
            ChatColor.DARK_GRAY + "" + ChatColor.RESET
    };

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

        // Satır team'lerini oluştur
        for (int i = 0; i < LINE_ENTRIES.length; i++) {
            Team team = board.registerNewTeam("line_" + i);
            team.addEntry(LINE_ENTRIES[i]);
        }

        updateScores(player, board, objective);
        player.setScoreboard(board);

        // Klan tag'lerini bu yeni scoreboard'a sync et
        syncClanTags(player, board);
    }

    public void updateScoreboard(Player player) {
        Scoreboard board = player.getScoreboard();
        Objective objective = board.getObjective("armosu");

        if (objective == null) {
            setScoreboard(player);
            return;
        }

        // Skorları temizleyip yeniden ekleyelim
        for (String entry : board.getEntries()) {
            board.resetScores(entry);
        }

        updateScores(player, board, objective);
    }

    private void updateScores(Player player, Scoreboard board, Objective objective) {
        com.armoyu.plugins.clans.Clan clan = clanManager.getClanByPlayer(player.getUniqueId());

        // Satır 8: Üst çizgi
        setLine(board, objective, 0, 8, ChatColor.DARK_GRAY + "▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬");

        // Satır 7: Oyuncu adı
        String nameLine = ChatColor.YELLOW + "Oyuncu: " + ChatColor.WHITE + player.getName();
        if (clan != null) {
            nameLine += ChatColor.GRAY + " [" + ChatColor.GOLD + clan.getTag() + ChatColor.GRAY + "]";
        }
        setLine(board, objective, 1, 7, nameLine);

        // Satır 6: Boşluk
        setLine(board, objective, 2, 6, "");

        // Satır 5: Bakiye başlık
        setLine(board, objective, 3, 5, ChatColor.YELLOW + "Bakiye:");

        // Satır 4: Bakiye değer
        setLine(board, objective, 4, 4,
                ChatColor.GREEN + "" + moneyManager.getBalance(player.getUniqueId()) + " ARMO");

        // Satır 3: Boşluk
        setLine(board, objective, 5, 3, " ");

        // Satır 2: Web
        setLine(board, objective, 6, 2, ChatColor.AQUA + "armoyu.com");

        // Satır 1: Alt çizgi
        setLine(board, objective, 7, 1, ChatColor.DARK_GRAY + "▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬");
    }

    private void setLine(Scoreboard board, Objective objective, int lineIndex, int score, String text) {
        if (lineIndex >= LINE_ENTRIES.length)
            return;

        Team team = board.getTeam("line_" + lineIndex);
        if (team != null) {
            // Prefix'e max 64 karakter (1.13+), eski versiyonlarda 16
            team.setPrefix(text);
        }

        objective.getScore(LINE_ENTRIES[lineIndex]).setScore(score);
    }

    private void syncClanTags(Player owner, Scoreboard board) {
        // Tüm online oyuncuların klan tag'lerini bu scoreboard'a ekle
        for (Player online : Bukkit.getOnlinePlayers()) {
            com.armoyu.plugins.clans.Clan clan = clanManager.getClanByPlayer(online.getUniqueId());
            if (clan != null) {
                String teamName = "clan_" + clan.getId().toString().substring(0, 8);
                Team team = board.getTeam(teamName);
                if (team == null) {
                    team = board.registerNewTeam(teamName);
                }
                String prefix = ChatColor.GRAY + "[" + ChatColor.GOLD + clan.getTag() + ChatColor.GRAY + "] "
                        + ChatColor.WHITE;
                team.setPrefix(prefix);
                team.setColor(ChatColor.WHITE);
                if (!team.hasEntry(online.getName())) {
                    team.addEntry(online.getName());
                }
            }
        }
    }
}
