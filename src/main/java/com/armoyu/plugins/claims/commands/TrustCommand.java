package com.armoyu.plugins.claims.commands;

import com.armoyu.plugins.claims.Claim;
import com.armoyu.plugins.claims.ClaimManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

public class TrustCommand implements CommandExecutor {
    private final ClaimManager claimManager;
    private final boolean untrust;

    public TrustCommand(ClaimManager claimManager, boolean untrust) {
        this.claimManager = claimManager;
        this.untrust = untrust;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player))
            return true;
        Player player = (Player) sender;

        if (args.length == 0) {
            player.sendMessage(ChatColor.RED + "Kullanım: /" + label + " <oyuncu>");
            return true;
        }

        Claim claim = claimManager.getClaimAt(player.getLocation());
        if (claim == null || claim.getOwnerPlayer() == null || !claim.getOwnerPlayer().equals(player.getUniqueId())) {
            player.sendMessage(ChatColor.RED + "Bu komutu sadece kendi claiminizde kullanabilirsiniz!");
            return true;
        }

        OfflinePlayer target = Bukkit.getOfflinePlayer(args[0]);
        if (target == null || target.getUniqueId() == null) {
            player.sendMessage(ChatColor.RED + "Oyuncu bulunamadı.");
            return true;
        }

        if (untrust) {
            if (claim.getTrustedPlayers().remove(target.getUniqueId())) {
                player.sendMessage(ChatColor.GREEN + target.getName() + " artık bu claimde yetkili değil.");
                claimManager.saveClaims();
            } else {
                player.sendMessage(ChatColor.RED + "Bu oyuncu zaten yetkili değildi.");
            }
        } else {
            if (claim.getTrustedPlayers().add(target.getUniqueId())) {
                player.sendMessage(ChatColor.GREEN + target.getName() + " bu claimde yetkilendirildi.");
                claimManager.saveClaims();
            } else {
                player.sendMessage(ChatColor.RED + "Bu oyuncu zaten yetkili.");
            }
        }

        return true;
    }
}
