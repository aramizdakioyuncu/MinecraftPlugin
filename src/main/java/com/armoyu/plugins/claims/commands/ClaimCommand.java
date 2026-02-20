package com.armoyu.plugins.claims.commands;

import com.armoyu.plugins.claims.Claim;
import com.armoyu.plugins.claims.ClaimManager;
import com.armoyu.plugins.clans.Clan;
import com.armoyu.plugins.clans.ClanManager;
import com.armoyu.plugins.clans.ClanUtils;
import com.armoyu.plugins.economy.MoneyManager;
import com.armoyu.plugins.actionmanager.ActionManager;
import com.armoyu.utils.PlayerRole;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

public class ClaimCommand implements CommandExecutor {
    private final ClaimManager claimManager;
    private final MoneyManager moneyManager;
    private final ClanManager clanManager;
    private final ActionManager actionManager;

    public ClaimCommand(ClaimManager claimManager, MoneyManager moneyManager, ClanManager clanManager,
            ActionManager actionManager) {
        this.claimManager = claimManager;
        this.moneyManager = moneyManager;
        this.clanManager = clanManager;
        this.actionManager = actionManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player))
            return true;
        Player player = (Player) sender;

        if (actionManager.getRole(player) == PlayerRole.GUEST) {
            player.sendMessage(ChatColor.RED + "Arsa komutlarını kullanabilmek için giriş yapmalısınız!");
            return true;
        }

        if (args.length == 0) {
            player.sendMessage(ChatColor.YELLOW + "/claim al - Seçili alanı size zimmetler (veya genişletir).");
            player.sendMessage(ChatColor.YELLOW + "/claim sil - Durduğunuz yerdeki claimi siler.");
            player.sendMessage(ChatColor.YELLOW + "/claim liste - Sahip olduğunuz claimleri listeler.");
            player.sendMessage(ChatColor.YELLOW + "/claim trust <oyuncu> - Claiminize arkadaş ekler.");
            player.sendMessage(ChatColor.YELLOW + "/claim untrust <oyuncu> - Arkadaşı çıkarır.");
            player.sendMessage(ChatColor.YELLOW + "/claim bilgi - Durduğunuz yerdeki claim bilgisini gösterir.");
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "al":
                handleClaimCreate(player, false);
                break;
            case "sil":
                handleClaimDelete(player);
                break;
            case "bilgi":
                handleInfo(player);
                break;
            case "liste":
                handleClaimList(player);
                break;
            case "trust":
                handleTrust(player, args);
                break;
            case "untrust":
                handleUntrust(player, args);
                break;
        }

        return true;
    }

    private void handleClaimCreate(Player player, boolean isClan) {
        Location[] points = claimManager.getSelection(player.getUniqueId());
        if (points == null || points[0] == null || points[1] == null) {
            player.sendMessage(ChatColor.RED + "Lütfen önce altın kürek ile iki köşe seçin!");
            return;
        }

        // Genişletme kontrolü: Seçilen noktalardan biri mevcut bir claim'in içindeyse
        // genişletme yap
        Claim existingClaim = claimManager.getClaimAt(points[0]);
        if (existingClaim == null)
            existingClaim = claimManager.getClaimAt(points[1]);

        if (existingClaim != null) {
            handleClaimExpand(player, existingClaim, points, isClan);
            return;
        }

        int width = Math.abs(points[0].getBlockX() - points[1].getBlockX()) + 1;
        int length = Math.abs(points[0].getBlockZ() - points[1].getBlockZ()) + 1;
        int area = width * length;
        double cost = area * 10.0;

        if (isClan) {
            Clan clan = clanManager.getClanByPlayer(player.getUniqueId());
            if (clan == null || !clan.isOfficer(player.getUniqueId())) {
                player.sendMessage(ChatColor.RED + "Bunu yapmak için klan yetkilisi olmalısınız!");
                return;
            }
            if (!clan.removeBalance(cost)) {
                player.sendMessage(ChatColor.RED + "Klan kasasında yeterli para yok! (Gereken: " + cost + " ARMO)");
                return;
            }
            Claim newClaim = claimManager.createClaim(null, clan.getId(), points[0], points[1]);
            if (newClaim != null) {
                player.sendMessage(ChatColor.GREEN + "Klan claimi başarıyla oluşturuldu! (" + area + " blok)");

                Location center = points[0].clone().add(points[1]).multiply(0.5);
                center.setY(points[0].getWorld().getHighestBlockYAt(center) + 1.0);

                clan.setLandSpawn(newClaim.getId(), center);
                clanManager.saveClans();

                ClanUtils.updateLocationVisual(clan, center, "Bölgesi");
            } else {
                player.sendMessage(ChatColor.RED + "Bu alan başka bir claim ile çakışıyor!");
                clan.addBalance(cost);
            }
        } else {
            if (!moneyManager.hasEnough(player.getUniqueId(), cost)) {
                player.sendMessage(ChatColor.RED + "Yetersiz bakiye! (Gereken: " + cost + " ARMO)");
                return;
            }
            if (claimManager.createClaim(player.getUniqueId(), null, points[0], points[1]) != null) {
                moneyManager.removeMoney(player.getUniqueId(), cost);
                player.sendMessage(ChatColor.GREEN + "Bölge başarıyla claimlendi! (" + area + " blok)");
            } else {
                player.sendMessage(ChatColor.RED + "Bu alan başka bir claim ile çakışıyor!");
            }
        }
    }

    private void handleClaimExpand(Player player, Claim existing, Location[] points, boolean isClan) {
        // Sahiplik kontrolü
        boolean canExpand = false;
        Clan clan = null;
        if (isClan) {
            clan = clanManager.getClanByPlayer(player.getUniqueId());
            if (clan != null && existing.getOwnerClan() != null && existing.getOwnerClan().equals(clan.getId())
                    && clan.isOfficer(player.getUniqueId())) {
                canExpand = true;
            }
        } else {
            if (existing.getOwnerPlayer() != null && existing.getOwnerPlayer().equals(player.getUniqueId())) {
                canExpand = true;
            }
        }

        if (!canExpand) {
            player.sendMessage(ChatColor.RED + "Bu claim size ait değil, genişletemezsiniz!");
            return;
        }

        // Yeni sınırları hesapla (mevcut claim + yeni noktaların tümünü kapsa)
        int newMinX = Math.min(existing.getMinX(), Math.min(points[0].getBlockX(), points[1].getBlockX()));
        int newMaxX = Math.max(existing.getMaxX(), Math.max(points[0].getBlockX(), points[1].getBlockX()));
        int newMinZ = Math.min(existing.getMinZ(), Math.min(points[0].getBlockZ(), points[1].getBlockZ()));
        int newMaxZ = Math.max(existing.getMaxZ(), Math.max(points[0].getBlockZ(), points[1].getBlockZ()));

        int oldArea = existing.getArea();
        int newArea = (newMaxX - newMinX + 1) * (newMaxZ - newMinZ + 1);
        int addedArea = newArea - oldArea;

        if (addedArea <= 0) {
            player.sendMessage(ChatColor.YELLOW + "Seçim mevcut arsanın dışına çıkmıyor, genişletmeye gerek yok.");
            return;
        }

        double cost = addedArea * 10.0;

        if (isClan && clan != null) {
            if (!clan.removeBalance(cost)) {
                player.sendMessage(ChatColor.RED + "Klan kasasında yeterli para yok! (Gereken: " + cost + " ARMO)");
                return;
            }
            if (claimManager.expandClaim(existing, newMinX, newMaxX, newMinZ, newMaxZ)) {
                player.sendMessage(ChatColor.GREEN + "Klan arsası genişletildi! (+" + addedArea + " blok, ücret: "
                        + cost + " ARMO)");
            } else {
                player.sendMessage(ChatColor.RED + "Genişletme başka bir claim ile çakışıyor!");
                clan.addBalance(cost);
            }
        } else {
            if (!moneyManager.hasEnough(player.getUniqueId(), cost)) {
                player.sendMessage(ChatColor.RED + "Yetersiz bakiye! (Gereken: " + cost + " ARMO)");
                return;
            }
            if (claimManager.expandClaim(existing, newMinX, newMaxX, newMinZ, newMaxZ)) {
                moneyManager.removeMoney(player.getUniqueId(), cost);
                player.sendMessage(
                        ChatColor.GREEN + "Arsa genişletildi! (+" + addedArea + " blok, ücret: " + cost + " ARMO)");
            } else {
                player.sendMessage(ChatColor.RED + "Genişletme başka bir claim ile çakışıyor!");
            }
        }
    }

    private void handleClaimDelete(Player player) {
        Claim claim = claimManager.getClaimAt(player.getLocation());
        if (claim == null) {
            player.sendMessage(ChatColor.RED + "Burada bir claim yok!");
            return;
        }

        boolean canDelete = false;
        if (claim.getOwnerPlayer() != null && claim.getOwnerPlayer().equals(player.getUniqueId())) {
            canDelete = true;
        } else if (claim.getOwnerClan() != null) {
            Clan clan = clanManager.getClanById(claim.getOwnerClan());
            if (clan != null && clan.getLeader().equals(player.getUniqueId())) {
                canDelete = true;
            }
        }

        if (canDelete) {
            claimManager.deleteClaim(claim);
            player.sendMessage(ChatColor.GREEN + "Claim başarıyla silindi.");
        } else {
            player.sendMessage(ChatColor.RED + "Bu claimi silme yetkiniz yok!");
        }
    }

    private void handleInfo(Player player) {
        Claim claim = claimManager.getClaimAt(player.getLocation());
        if (claim == null) {
            player.sendMessage(ChatColor.YELLOW + "Burası sahipsiz topraklar.");
            return;
        }

        if (claim.getOwnerPlayer() != null) {
            String ownerName = Bukkit.getOfflinePlayer(claim.getOwnerPlayer()).getName();
            player.sendMessage(ChatColor.GOLD + "=== Claim Bilgisi ===");
            player.sendMessage(ChatColor.YELLOW + "Sahibi: " + ChatColor.WHITE + ownerName);
        } else if (claim.getOwnerClan() != null) {
            Clan clan = clanManager.getClanById(claim.getOwnerClan());
            player.sendMessage(ChatColor.GOLD + "=== Klan Claim Bilgisi ===");
            player.sendMessage(
                    ChatColor.YELLOW + "Klan: " + ChatColor.WHITE + (clan != null ? clan.getName() : "Bilinmiyor"));
        }
    }

    private void handleClaimList(Player player) {
        java.util.List<com.armoyu.plugins.claims.Claim> playerClaims = claimManager
                .getClaimsByPlayer(player.getUniqueId());
        if (playerClaims.isEmpty()) {
            player.sendMessage(ChatColor.RED + "Henüz bir claiminiz yok.");
            return;
        }

        player.sendMessage(ChatColor.GOLD + "=== Claim Listesi ===");
        for (int i = 0; i < playerClaims.size(); i++) {
            com.armoyu.plugins.claims.Claim c = playerClaims.get(i);
            player.sendMessage(ChatColor.YELLOW.toString() + (i + 1) + ". " + ChatColor.WHITE +
                    c.getWorld() + " (" + c.getMinX() + "," + c.getMinZ() + " -> " + c.getMaxX() + "," + c.getMaxZ()
                    + ")");
        }
    }

    private void handleTrust(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(ChatColor.RED + "Kullanım: /claim trust <oyuncu>");
            return;
        }

        Claim claim = claimManager.getClaimAt(player.getLocation());
        if (claim == null || !player.getUniqueId().equals(claim.getOwnerPlayer())) {
            player.sendMessage(ChatColor.RED + "Burası size ait bir claim değil!");
            return;
        }

        org.bukkit.OfflinePlayer target = org.bukkit.Bukkit.getOfflinePlayer(args[1]);
        if (target == null || target.getUniqueId() == null) {
            player.sendMessage(ChatColor.RED + "Oyuncu bulunamadı.");
            return;
        }

        claim.addTrust(target.getUniqueId());
        claimManager.saveClaims();
        player.sendMessage(ChatColor.GREEN + target.getName() + " başarıyla claim'e eklendi.");
    }

    private void handleUntrust(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(ChatColor.RED + "Kullanım: /claim untrust <oyuncu>");
            return;
        }

        Claim claim = claimManager.getClaimAt(player.getLocation());
        if (claim == null || !player.getUniqueId().equals(claim.getOwnerPlayer())) {
            player.sendMessage(ChatColor.RED + "Burası size ait bir claim değil!");
            return;
        }

        org.bukkit.OfflinePlayer target = org.bukkit.Bukkit.getOfflinePlayer(args[1]);
        claim.removeTrust(target.getUniqueId());
        claimManager.saveClaims();
        player.sendMessage(ChatColor.GREEN + target.getName() + " claim'den çıkarıldı.");
    }

    public void handleClanClaimRedirect(Player player) {
        handleClaimCreate(player, true);
    }
}
