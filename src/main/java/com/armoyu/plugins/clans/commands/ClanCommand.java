package com.armoyu.plugins.clans.commands;

import com.armoyu.plugins.clans.Clan;
import com.armoyu.plugins.clans.ClanManager;
import com.armoyu.plugins.clans.ClanUtils;
import com.armoyu.plugins.clans.ClanListener;
import com.armoyu.plugins.clans.ClanVaultGUI;
import com.armoyu.plugins.economy.MoneyManager;
import com.armoyu.plugins.claims.ClaimManager;
import com.armoyu.plugins.claims.commands.ClaimCommand;
import com.armoyu.plugins.teleport.TeleportManager;
import com.armoyu.plugins.actionmanager.ActionManager;
import com.armoyu.utils.ChatUtils;
import com.armoyu.utils.PlayerRole;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.UUID;

public class ClanCommand implements CommandExecutor {

    private final ClanManager clanManager;
    private final MoneyManager moneyManager;
    private final ClanListener clanListener;
    private final ClanVaultGUI vaultGUI;
    private final TeleportManager teleportManager;
    private final ClaimManager claimManager;
    private final ActionManager actionManager;

    public ClanCommand(ClanManager clanManager, MoneyManager moneyManager, ClanListener clanListener,
            ClanVaultGUI vaultGUI, TeleportManager teleportManager, ClaimManager claimManager,
            ActionManager actionManager) {
        this.clanManager = clanManager;
        this.moneyManager = moneyManager;
        this.clanListener = clanListener;
        this.vaultGUI = vaultGUI;
        this.teleportManager = teleportManager;
        this.claimManager = claimManager;
        this.actionManager = actionManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Bu bir oyuncu komutudur.");
            return true;
        }

        Player player = (Player) sender;

        if (actionManager.getRole(player) == PlayerRole.GUEST) {
            player.sendMessage(ChatColor.RED + "Klan komutlarını kullanabilmek için giriş yapmalısınız!");
            return true;
        }

        if (args.length == 0) {
            sendHelp(player);
            return true;
        }

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "kur":
                handleCreate(player, args);
                break;
            case "davet":
                handleInvite(player, args);
                break;
            case "katil":
                handleJoin(player, args);
                break;
            case "ayril":
                handleLeave(player);
                break;
            case "para":
                handleMoney(player, args);
                break;
            case "profil":
                handleProfile(player, args);
                break;
            case "setspawn":
                handleSetSpawn(player, args);
                break;
            case "spawn":
                handleSpawn(player, args);
                break;
            case "banka":
            case "kasa":
                handleVault(player);
                break;
            case "topla":
                handleAssemble(player);
                break;
            case "sevyeatla":
                handleLevelUp(player);
                break;
            case "claim":
                handleClanClaim(player);
                break;
            case "yetki":
                handleOfficer(player, args);
                break;
            case "liste":
                handleClanList(player);
                break;
            case "yetkililer":
                handleOfficerList(player);
                break;
            case "kale":
                if (args.length > 1) {
                    handleCastleTeleport(player, args);
                } else {
                    handleCastleList(player);
                }
                break;
            case "kaleler":
                handleClanCastlesList(player);
                break;
            case "kaleal":
                handleBuyCastle(player, args);
                break;
            case "kalesil":
                handleCastleDelete(player);
                break;
            case "kaletamir":
                handleCastleRepair(player);
                break;
            case "arsalar":
                handleClanLands(player);
                break;
            default:
                sendHelp(player);
                break;
        }

        return true;
    }

    private void handleCreate(Player player, String[] args) {
        if (args.length < 3) {
            player.sendMessage(ChatColor.RED + "Kullanım: /klan kur <isim> <etiket>");
            return;
        }

        if (clanManager.getClanByPlayer(player.getUniqueId()) != null) {
            player.sendMessage(ChatColor.RED + "Zaten bir klandasınız.");
            return;
        }

        String name = args[1];
        String tag = args[2];

        if (clanManager.getClanByName(name) != null) {
            player.sendMessage(ChatColor.RED + "Bu isimde bir klan zaten var.");
            return;
        }

        double cost = com.armoyu.minecraftplugin.getPlugin(com.armoyu.minecraftplugin.class).getConfig()
                .getDouble("economy.clan_creation_cost", 1000.0);

        if (!moneyManager.hasEnough(player.getUniqueId(), cost)) {
            player.sendMessage(ChatColor.RED + "Klan kurmak için yeterli paranız yok! Gerekli: " + cost + " ARMO");
            return;
        }

        moneyManager.removeMoney(player.getUniqueId(), cost);
        clanManager.createClan(player, name, tag);
        clanListener.updateTabName(player);
        com.armoyu.minecraftplugin.getScoreManager().updateScoreboard(player);
        player.sendMessage(
                ChatColor.GREEN + name + " klanı başarıyla kuruldu! Tag: [" + tag + "] Ücret: " + cost + " ARMO");
    }

    private void handleInvite(Player player, String[] args) {
        Clan clan = clanManager.getClanByPlayer(player.getUniqueId());
        if (clan == null || !clan.getLeader().equals(player.getUniqueId())) {
            player.sendMessage(ChatColor.RED + "Bunun için klan lideri olmalısınız.");
            return;
        }

        if (args.length < 2) {
            player.sendMessage(ChatColor.RED + "Kullanım: /klan davet <oyuncu>");
            return;
        }

        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            player.sendMessage(ChatColor.RED + "Oyuncu bulunamadı.");
            return;
        }

        if (target.getUniqueId().equals(player.getUniqueId())) {
            player.sendMessage(ChatColor.RED + "Kendinizi davet edemezsiniz!");
            return;
        }

        clan.getPendingInvites().add(target.getUniqueId());
        player.sendMessage(ChatColor.GREEN + target.getName() + " klana davet edildi.");

        target.sendMessage(
                ChatUtils.ARMOYUTag + ChatColor.YELLOW + clan.getName() + ChatColor.GOLD + " klanından davet aldınız!");

        net.md_5.bungee.api.chat.TextComponent acceptBtn = new net.md_5.bungee.api.chat.TextComponent("  [ KATIL ]  ");
        acceptBtn.setColor(net.md_5.bungee.api.ChatColor.GREEN);
        acceptBtn.setBold(true);
        acceptBtn.setClickEvent(new net.md_5.bungee.api.chat.ClickEvent(
                net.md_5.bungee.api.chat.ClickEvent.Action.RUN_COMMAND, "/klan katil " + clan.getName()));
        acceptBtn.setHoverEvent(
                new net.md_5.bungee.api.chat.HoverEvent(net.md_5.bungee.api.chat.HoverEvent.Action.SHOW_TEXT,
                        new net.md_5.bungee.api.chat.hover.content.Text("§aTıklayarak klan katilabilirsin!")));

        net.md_5.bungee.api.chat.TextComponent denyBtn = new net.md_5.bungee.api.chat.TextComponent("  [ REDDET ]  ");
        denyBtn.setColor(net.md_5.bungee.api.ChatColor.RED);
        denyBtn.setBold(true);
        denyBtn.setClickEvent(new net.md_5.bungee.api.chat.ClickEvent(
                net.md_5.bungee.api.chat.ClickEvent.Action.RUN_COMMAND, "/klan ayril")); // Ayrıl/Reddet mantığı için
                                                                                         // uygun
        denyBtn.setHoverEvent(
                new net.md_5.bungee.api.chat.HoverEvent(net.md_5.bungee.api.chat.HoverEvent.Action.SHOW_TEXT,
                        new net.md_5.bungee.api.chat.hover.content.Text("§cTıklayarak reddedebilirsin!")));

        target.spigot().sendMessage(acceptBtn, new net.md_5.bungee.api.chat.TextComponent(" "), denyBtn);
    }

    private void handleJoin(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(ChatColor.RED + "Kullanım: /klan katil <klan_ismi>");
            return;
        }

        Clan clan = clanManager.getClanByName(args[1]);
        if (clan == null) {
            player.sendMessage(ChatColor.RED + "Klan bulunamadı.");
            return;
        }

        if (!clan.getPendingInvites().contains(player.getUniqueId())) {
            player.sendMessage(ChatColor.RED + "Bu klandan davet almadınız.");
            return;
        }

        clanManager.addMember(clan, player);
        clan.getPendingInvites().remove(player.getUniqueId());
        clanListener.updateTabName(player);
        com.armoyu.minecraftplugin.getScoreManager().updateScoreboard(player);
        player.sendMessage(ChatColor.GREEN + clan.getName() + " klanına katıldınız!");
    }

    private void handleLeave(Player player) {
        Clan clan = clanManager.getClanByPlayer(player.getUniqueId());
        if (clan == null) {
            player.sendMessage(ChatColor.RED + "Bir klanda değilsiniz.");
            return;
        }

        clanManager.removeMember(player);
        clanListener.updateTabName(player);
        com.armoyu.minecraftplugin.getScoreManager().updateScoreboard(player);
        player.sendMessage(ChatColor.YELLOW + "Klandan ayrıldınız.");
    }

    private void handleMoney(Player player, String[] args) {
        Clan clan = clanManager.getClanByPlayer(player.getUniqueId());
        if (clan == null) {
            player.sendMessage(ChatColor.RED + "Bir klanda değilsiniz.");
            return;
        }

        if (args.length < 3) {
            player.sendMessage(ChatColor.RED + "Kullanım: /klan para <yatir|cek> <miktar>");
            player.sendMessage(ChatColor.GOLD + "Klan Bakiyesi: " + ChatColor.GREEN + clan.getBalance() + " ARMO");
            return;
        }

        double amount;
        try {
            amount = Double.parseDouble(args[2]);
        } catch (NumberFormatException e) {
            player.sendMessage(ChatColor.RED + "Geçersiz miktar.");
            return;
        }

        if (args[1].equalsIgnoreCase("yatir")) {
            if (moneyManager.hasEnough(player.getUniqueId(), amount)) {
                moneyManager.removeMoney(player.getUniqueId(), amount);
                clan.addBalance(amount);
                clanManager.saveClans();
                player.sendMessage(ChatColor.GREEN + "Klana " + amount + " ARMO yatırıldı.");
            } else {
                player.sendMessage(ChatColor.RED + "Yetersiz bakiye.");
            }
        } else if (args[1].equalsIgnoreCase("cek")) {
            if (!clan.getLeader().equals(player.getUniqueId())) {
                player.sendMessage(ChatColor.RED + "Sadece klan lideri para çekebilir.");
                return;
            }
            if (clan.removeBalance(amount)) {
                moneyManager.addMoney(player.getUniqueId(), amount);
                clanManager.saveClans();
                player.sendMessage(ChatColor.GREEN + "Klandan " + amount + " ARMO çekildi.");
            } else {
                player.sendMessage(ChatColor.RED + "Klan kasasında yeterli para yok.");
            }
        }
    }

    private void handleProfile(Player player, String[] args) {
        Clan clanProfile;
        if (args.length < 2) {
            clanProfile = clanManager.getClanByPlayer(player.getUniqueId());
        } else {
            clanProfile = clanManager.getClanByName(args[1]);
        }

        if (clanProfile == null) {
            player.sendMessage(ChatColor.RED + "Klan bulunamadı.");
            return;
        }

        player.sendMessage(ChatColor.GOLD + "=== " + clanProfile.getName() + " Profil ===");
        player.sendMessage(ChatColor.YELLOW + "Tag: " + ChatColor.WHITE + "[" + clanProfile.getTag() + "]");
        player.sendMessage(ChatColor.YELLOW + "Seviye: " + ChatColor.GREEN + clanProfile.getLevel());
        player.sendMessage(ChatColor.YELLOW + "Bakiye: " + ChatColor.GREEN + clanProfile.getBalance() + " ARMO");
        player.sendMessage(ChatColor.YELLOW + "Üye Sayısı: " + ChatColor.WHITE + clanProfile.getMembers().size());
        player.sendMessage(ChatColor.GOLD + "======================");
    }

    private void handleSetSpawn(Player player, String[] args) {
        com.armoyu.plugins.clans.Clan clan = clanManager.getClanByPlayer(player.getUniqueId());
        if (clan == null || !clan.getLeader().equals(player.getUniqueId())) {
            player.sendMessage(ChatColor.RED + "Sadece klan lideri spawn noktası belirleyebilir.");
            return;
        }

        com.armoyu.plugins.claims.Claim claim = claimManager.getClaimAt(player.getLocation());
        if (claim == null || claim.getOwnerClan() == null || !claim.getOwnerClan().equals(clan.getId())) {
            player.sendMessage(
                    ChatColor.RED + "Burada size ait bir klan claimi yok! Spawn sadece klan arsasında ayarlanabilir.");
            return;
        }

        if (args.length > 1 && args[1].equalsIgnoreCase("arsa")) {
            clan.setLandSpawn(claim.getId(), player.getLocation());
            clanManager.saveClans();
            ClanUtils.updateLocationVisual(clan, player.getLocation(), "Arsa Spawn");
            player.sendMessage(ChatColor.GREEN + "Bu arsa için klan ışınlanma noktası başarıyla ayarlandı!");
        } else {
            clan.setSpawn(player.getLocation());
            clanManager.saveClans();
            ClanUtils.updateLocationVisual(clan, clan.getSpawn(), "Merkezi");
            player.sendMessage(ChatColor.GREEN + "Ana klan spawn noktası başarıyla ayarlandı!");
        }
    }

    // Exposing this for other classes if needed, though ClanUtils is preferred
    public void updateSpawnVisual(com.armoyu.plugins.clans.Clan clan) {
        ClanUtils.updateLocationVisual(clan, clan.getSpawn(), "Merkezi");
    }

    private void handleSpawn(Player player, String[] args) {
        com.armoyu.plugins.clans.Clan clan = clanManager.getClanByPlayer(player.getUniqueId());
        if (clan == null) {
            player.sendMessage(ChatColor.RED + "Bir klanda değilsiniz.");
            return;
        }

        org.bukkit.Location targetLoc = clan.getSpawn();
        String targetName = "Merkezi";

        if (args.length > 1) {
            try {
                int landIdx = Integer.parseInt(args[1]) - 1;
                java.util.List<com.armoyu.plugins.claims.Claim> claims = claimManager.getClaimsByClan(clan.getId());
                if (landIdx >= 0 && landIdx < claims.size()) {
                    com.armoyu.plugins.claims.Claim claim = claims.get(landIdx);
                    targetLoc = clan.getLandSpawn(claim.getId());
                    if (targetLoc == null) {
                        targetLoc = new org.bukkit.Location(player.getWorld(),
                                (claim.getMinX() + claim.getMaxX()) / 2.0, player.getLocation().getY(),
                                (claim.getMinZ() + claim.getMaxZ()) / 2.0);
                    }
                    targetName = "Arsası (" + (landIdx + 1) + ")";
                }
            } catch (NumberFormatException ignored) {
            }
        }

        if (targetLoc == null) {
            player.sendMessage(ChatColor.RED + "Klan spawn noktası belirlenmemiş!");
            return;
        }

        if (teleportManager.hasActiveTask(player)) {
            player.sendMessage(ChatColor.YELLOW + "Zaten bir ışınlanma işleminiz var.");
            return;
        }

        double cost = com.armoyu.minecraftplugin.getPlugin(com.armoyu.minecraftplugin.class).getConfig()
                .getDouble("economy.teleport_costs." + (targetName.equals("Merkezi") ? "spawn" : "land_spawn"),
                        targetName.equals("Merkezi") ? 100.0 : 150.0);

        if (!moneyManager.hasEnough(player.getUniqueId(), cost)) {
            player.sendMessage(
                    ChatColor.RED + "Bu klan noktasına ışınlanmak için yeterli paranız yok! Gerekli: " + cost);
            return;
        }

        player.sendMessage(ChatColor.YELLOW + "Klan " + targetName + " gidiliyor... Ücret: " + cost + ". "
                + getSpawnCooldown(clan.getLevel()) + " saniye hareket etmeyin.");
        moneyManager.removeMoney(player.getUniqueId(), cost);

        int cooldown = getSpawnCooldown(clan.getLevel());
        final org.bukkit.Location finalLoc = targetLoc;
        final String finalName = targetName;

        if (cooldown > 0) {
            player.sendMessage(
                    ChatColor.YELLOW + "Klan " + finalName + " gidiliyor... " + cooldown + " saniye hareket etmeyin.");

            BukkitRunnable runnable = new BukkitRunnable() {
                int seconds = cooldown;

                @Override
                public void run() {
                    if (!teleportManager.hasActiveTask(player)) {
                        this.cancel();
                        return;
                    }

                    if (seconds > 0) {
                        player.sendTitle(ChatColor.BLUE + String.valueOf(seconds), "", 0, 20, 0);
                        seconds--;
                    } else {
                        player.teleport(finalLoc);
                        player.sendMessage(ChatColor.GREEN + "Klan " + finalName + " ışınlandınız.");
                        teleportManager.removeTask(player);
                        this.cancel();
                    }
                }
            };

            int taskId = runnable
                    .runTaskTimer(com.armoyu.minecraftplugin.getPlugin(com.armoyu.minecraftplugin.class), 0, 20)
                    .getTaskId();
            teleportManager.addTask(player, taskId);
        } else {
            player.teleport(finalLoc);
            player.sendMessage(ChatColor.GREEN + "Klan " + finalName + " ışınlandınız.");
        }
    }

    private int getSpawnCooldown(int level) {
        switch (level) {
            case 1:
                return 10;
            case 2:
                return 7;
            case 3:
                return 5;
            case 4:
                return 3;
            case 5:
                return 0;
            default:
                return 10;
        }
    }

    private void handleVault(Player player) {
        vaultGUI.openVault(player);
    }

    private void handleAssemble(Player player) {
        Clan clan = clanManager.getClanByPlayer(player.getUniqueId());
        if (clan == null || !clan.getLeader().equals(player.getUniqueId())) {
            player.sendMessage(ChatColor.RED + "Bunu sadece klan lideri yapabilir.");
            return;
        }

        // Önce online üyeleri say
        int memberCount = 0;
        for (UUID memberId : clan.getMembers()) {
            Player member = Bukkit.getPlayer(memberId);
            if (member != null && !member.equals(player))
                memberCount++;
        }

        if (memberCount == 0) {
            player.sendMessage(ChatColor.RED + "Toplanacak online üye yok!");
            return;
        }

        // Kişi başı maliyet (× klan seviyesi)
        double baseCost = com.armoyu.minecraftplugin.getPlugin(com.armoyu.minecraftplugin.class).getConfig()
                .getDouble("economy.gather_cost", 100.0);
        double perMemberCost = baseCost * clan.getLevel();
        double totalCost = perMemberCost * memberCount;

        if (!clan.removeBalance(totalCost)) {
            player.sendMessage(ChatColor.RED + "Klan kasasında yeterli para yok! (Gereken: " + totalCost + " ARMO, "
                    + memberCount + " üye × " + perMemberCost + " ARMO)");
            return;
        }
        clanManager.saveClans();

        player.sendMessage(ChatColor.GOLD + "Ekibi toplama başlatıldı! 5 saniye içinde herkes yanınıza ışınlanacak.");
        player.sendMessage(ChatColor.YELLOW + "Klan kasasından " + ChatColor.RED + totalCost + " ARMO"
                + ChatColor.YELLOW + " düşüldü (" + memberCount + " üye × " + perMemberCost + " ARMO, Seviye: "
                + clan.getLevel() + ").");
        ;

        org.bukkit.plugin.java.JavaPlugin plugin = com.armoyu.minecraftplugin
                .getPlugin(com.armoyu.minecraftplugin.class);

        for (UUID memberId : clan.getMembers()) {
            Player member = Bukkit.getPlayer(memberId);
            if (member == null || member.equals(player))
                continue;

            member.sendMessage(
                    ChatColor.YELLOW + "Klan lideri ekibi topluyor! 5 saniye içinde yanına ışınlanıyorsunuz...");

            org.bukkit.scheduler.BukkitRunnable runnable = new org.bukkit.scheduler.BukkitRunnable() {
                int seconds = 5;

                @Override
                public void run() {
                    if (!teleportManager.hasActiveTask(member)) {
                        this.cancel();
                        return;
                    }

                    if (seconds > 0) {
                        member.sendTitle(ChatColor.GOLD + "Ekip Toplanıyor!",
                                ChatColor.YELLOW + String.valueOf(seconds) + " saniye", 0, 25, 0);
                        seconds--;
                    } else {
                        member.teleport(player.getLocation());
                        equipArmor(member, clan.getLevel());
                        member.sendTitle(ChatColor.GREEN + "✓ Işınlandın!",
                                ChatColor.GRAY + "Liderin yanındasın", 0, 40, 20);
                        member.sendMessage(ChatColor.GREEN + "Liderin yanına ışınlandınız!");
                        teleportManager.removeTask(member);
                        this.cancel();
                    }
                }
            };

            int taskId = runnable.runTaskTimer(plugin, 0L, 20L).getTaskId();
            teleportManager.addTask(member, taskId);
        }

        player.sendMessage(ChatColor.GREEN + "Toplam " + memberCount + " üye çağrıldı.");
    }

    private void equipArmor(Player player, int level) {
        if (level < 2)
            return;

        org.bukkit.inventory.PlayerInventory inv = player.getInventory();
        if (level >= 5) {
            if (inv.getHelmet() == null)
                inv.setHelmet(new org.bukkit.inventory.ItemStack(org.bukkit.Material.DIAMOND_HELMET));
            if (inv.getChestplate() == null)
                inv.setChestplate(new org.bukkit.inventory.ItemStack(org.bukkit.Material.DIAMOND_CHESTPLATE));
            if (inv.getLeggings() == null)
                inv.setLeggings(new org.bukkit.inventory.ItemStack(org.bukkit.Material.DIAMOND_LEGGINGS));
            if (inv.getBoots() == null)
                inv.setBoots(new org.bukkit.inventory.ItemStack(org.bukkit.Material.DIAMOND_BOOTS));
        } else if (level >= 4) {
            if (inv.getHelmet() == null)
                inv.setHelmet(new org.bukkit.inventory.ItemStack(org.bukkit.Material.IRON_HELMET));
            if (inv.getChestplate() == null)
                inv.setChestplate(new org.bukkit.inventory.ItemStack(org.bukkit.Material.IRON_CHESTPLATE));
            if (inv.getLeggings() == null)
                inv.setLeggings(new org.bukkit.inventory.ItemStack(org.bukkit.Material.IRON_LEGGINGS));
            if (inv.getBoots() == null)
                inv.setBoots(new org.bukkit.inventory.ItemStack(org.bukkit.Material.IRON_BOOTS));
        } else if (level >= 3) {
            if (inv.getHelmet() == null)
                inv.setHelmet(new org.bukkit.inventory.ItemStack(org.bukkit.Material.CHAINMAIL_HELMET));
            if (inv.getChestplate() == null)
                inv.setChestplate(new org.bukkit.inventory.ItemStack(org.bukkit.Material.CHAINMAIL_CHESTPLATE));
            if (inv.getLeggings() == null)
                inv.setLeggings(new org.bukkit.inventory.ItemStack(org.bukkit.Material.CHAINMAIL_LEGGINGS));
            if (inv.getBoots() == null)
                inv.setBoots(new org.bukkit.inventory.ItemStack(org.bukkit.Material.CHAINMAIL_BOOTS));
        } else if (level >= 2) {
            if (inv.getHelmet() == null)
                inv.setHelmet(new org.bukkit.inventory.ItemStack(org.bukkit.Material.LEATHER_HELMET));
            if (inv.getChestplate() == null)
                inv.setChestplate(new org.bukkit.inventory.ItemStack(org.bukkit.Material.LEATHER_CHESTPLATE));
            if (inv.getLeggings() == null)
                inv.setLeggings(new org.bukkit.inventory.ItemStack(org.bukkit.Material.LEATHER_LEGGINGS));
            if (inv.getBoots() == null)
                inv.setBoots(new org.bukkit.inventory.ItemStack(org.bukkit.Material.LEATHER_BOOTS));
        }
    }

    private void handleLevelUp(Player player) {
        Clan clan = clanManager.getClanByPlayer(player.getUniqueId());
        if (clan == null || !clan.getLeader().equals(player.getUniqueId())) {
            player.sendMessage(ChatColor.RED + "Bunu sadece klan lideri yapabilir.");
            return;
        }

        if (clan.getLevel() >= 5) {
            player.sendMessage(ChatColor.RED + "Klan zaten maksimum seviyede!");
            return;
        }

        double cost = com.armoyu.minecraftplugin.getPlugin(com.armoyu.minecraftplugin.class).getConfig()
                .getDouble("economy.clan_levelup_cost", 5000.0) * clan.getLevel();

        if (clan.removeBalance(cost)) {
            clan.setLevel(clan.getLevel() + 1);
            clanManager.saveClans();
            player.sendMessage(ChatColor.GREEN + "Klan seviye atladı! Yeni Seviye: " + clan.getLevel());
        } else {
            player.sendMessage(ChatColor.RED + "Klan kasasında yeterli para yok! (Gereken: " + cost + " ARMO)");
        }
    }

    private void handleClanClaim(Player player) {
        // Redirection to reuse logic
        new ClaimCommand(claimManager, moneyManager, clanManager, actionManager)
                .handleClanClaimRedirect(player);
    }

    private void handleOfficer(Player player, String[] args) {
        if (args.length < 3) {
            player.sendMessage(ChatColor.RED + "Kullanım: /klan yetki <ver|al> <isim>");
            return;
        }

        Clan clan = clanManager.getClanByPlayer(player.getUniqueId());
        if (clan == null || !clan.getLeader().equals(player.getUniqueId())) {
            player.sendMessage(ChatColor.RED + "Bunu sadece klan lideri yapabilir!");
            return;
        }

        org.bukkit.OfflinePlayer target = Bukkit.getOfflinePlayer(args[2]);
        if (!clan.isMember(target.getUniqueId())) {
            player.sendMessage(ChatColor.RED + "Bu oyuncu klanınızda değil!");
            return;
        }

        if (args[1].equalsIgnoreCase("ver")) {
            clan.addOfficer(target.getUniqueId());
            player.sendMessage(ChatColor.GREEN + target.getName() + " klan yetkilisi yapıldı.");
        } else {
            clan.removeOfficer(target.getUniqueId());
            player.sendMessage(ChatColor.GREEN + target.getName() + " yetkisi alındı.");
        }
        clanManager.saveClans();
    }

    private void handleCastleList(Player player) {
        double cost = com.armoyu.minecraftplugin.getPlugin(com.armoyu.minecraftplugin.class).getConfig()
                .getDouble("economy.castle_cost", 10000.0);

        com.armoyu.plugins.structures.StructureManager sm = new com.armoyu.plugins.structures.StructureManager();
        java.util.Set<String> types = sm.getAvailableTypes();

        player.sendMessage(ChatColor.GOLD + "=== Mevcut Kale Tipleri (Ücret: " + cost + " ARMO) ===");
        player.sendMessage(
                ChatColor.YELLOW + "- " + ChatColor.WHITE + "Klasik" + ChatColor.GRAY + " (Taş Sur + Ahşap Kulübe)");
        player.sendMessage(
                ChatColor.YELLOW + "- " + ChatColor.WHITE + "Odun" + ChatColor.GRAY + " (Kütük Sur + Taş Kulübe)");

        // WorldEdit ile eklenen özel yapılar
        for (String type : types) {
            if (!type.equals("klasik") && !type.equals("1") && !type.equals("odun") && !type.equals("2")) {
                player.sendMessage(ChatColor.YELLOW + "- " + ChatColor.AQUA + type
                        + ChatColor.GRAY + " (Özel yapı - WorldEdit ile oluşturuldu)");
            }
        }
        player.sendMessage(ChatColor.GOLD + "===========================");
    }

    private void handleBuyCastle(Player player, String[] args) {
        if (args.length < 3) {
            player.sendMessage(ChatColor.RED + "Kullanım: /klan kaleal <isim> <tip>");
            player.sendMessage(ChatColor.YELLOW + "Tipler için: /klan kale");
            return;
        }

        String castleName = args[1];
        String castleType = args[2];

        Clan clan = clanManager.getClanByPlayer(player.getUniqueId());
        if (clan == null || !clan.isOfficer(player.getUniqueId())) {
            player.sendMessage(ChatColor.RED + "Bunu sadece klan lideri veya yetkilileri yapabilir.");
            return;
        }

        // Oyuncunun üzerinde durduğu claim'i bul
        com.armoyu.plugins.claims.Claim claim = claimManager.getClaimAt(player.getLocation());
        if (claim == null || claim.getOwnerClan() == null || !claim.getOwnerClan().equals(clan.getId())) {
            player.sendMessage(ChatColor.RED + "Klanınıza ait bir arsa (claim) üzerinde durmalısınız!");
            return;
        }

        // Bu arsada zaten kale var mı kontrol et (claim UUID ile doğrudan bakıyoruz)
        String claimKey = claim.getId().toString();
        String[] existingCastle = clan.getCastles().get(claimKey);

        if (existingCastle != null) {
            player.sendMessage(ChatColor.GOLD + "Bu arsada zaten bir kale var: " + ChatColor.YELLOW + existingCastle[0]
                    + ChatColor.GRAY + " (" + existingCastle[1] + ")");
            player.sendMessage(ChatColor.YELLOW + "Seçenekler:");

            net.md_5.bungee.api.chat.TextComponent deleteBtn = new net.md_5.bungee.api.chat.TextComponent(
                    "  [ KALE SİL ]  ");
            deleteBtn.setColor(net.md_5.bungee.api.ChatColor.RED);
            deleteBtn.setBold(true);
            deleteBtn.setClickEvent(new net.md_5.bungee.api.chat.ClickEvent(
                    net.md_5.bungee.api.chat.ClickEvent.Action.RUN_COMMAND, "/klan kalesil"));
            deleteBtn.setHoverEvent(
                    new net.md_5.bungee.api.chat.HoverEvent(net.md_5.bungee.api.chat.HoverEvent.Action.SHOW_TEXT,
                            new net.md_5.bungee.api.chat.hover.content.Text("§cKaleyi tamamen siler")));

            net.md_5.bungee.api.chat.TextComponent repairBtn = new net.md_5.bungee.api.chat.TextComponent(
                    "  [ TAMİR ET ]  ");
            repairBtn.setColor(net.md_5.bungee.api.ChatColor.GREEN);
            repairBtn.setBold(true);
            repairBtn.setClickEvent(new net.md_5.bungee.api.chat.ClickEvent(
                    net.md_5.bungee.api.chat.ClickEvent.Action.RUN_COMMAND, "/klan kaletamir"));
            repairBtn.setHoverEvent(
                    new net.md_5.bungee.api.chat.HoverEvent(net.md_5.bungee.api.chat.HoverEvent.Action.SHOW_TEXT,
                            new net.md_5.bungee.api.chat.hover.content.Text("§aKaleyi tamir eder (%50 maliyet)")));

            player.spigot().sendMessage(deleteBtn, new net.md_5.bungee.api.chat.TextComponent(" "), repairBtn);
            return;
        }

        // Arsanın merkezini hesapla
        int centerX = (claim.getMinX() + claim.getMaxX()) / 2;
        int centerZ = (claim.getMinZ() + claim.getMaxZ()) / 2;
        org.bukkit.Location claimCenter = new org.bukkit.Location(
                player.getWorld(), centerX, player.getWorld().getHighestBlockYAt(centerX, centerZ), centerZ);

        // Claim büyüklük kontrolü (21x21 minimum)
        int claimWidth = claim.getMaxX() - claim.getMinX() + 1;
        int claimLength = claim.getMaxZ() - claim.getMinZ() + 1;
        if (claimWidth < 21 || claimLength < 21) {
            player.sendMessage(ChatColor.RED
                    + "Bu arsa kale için yeterli büyüklükte değil! (Minimum 21x21 gerekli, mevcut: "
                    + claimWidth + "x" + claimLength + ")");
            return;
        }

        double cost = com.armoyu.minecraftplugin.getPlugin(com.armoyu.minecraftplugin.class).getConfig()
                .getDouble("economy.castle_cost", 10000.0);

        if (!clan.removeBalance(cost)) {
            player.sendMessage(ChatColor.RED + "Klan kasasında yeterli para yok! (Gereken: " + cost + " ARMO)");
            return;
        }

        // Claim UUID'sini castle key olarak kullan
        clan.addCastle(claimKey, castleName, castleType);

        // Kale konumunu landSpawns'a kaydet (ışınlanma için)
        clan.setLandSpawn(claim.getId(), claimCenter);

        clanManager.saveClans();
        new com.armoyu.plugins.structures.StructureManager().placeCastle(claimCenter, castleType, clan.getName());
        Bukkit.broadcastMessage(ChatColor.GOLD + "[🏰] " + ChatColor.GREEN + clan.getName() + " klanı, '"
                + ChatColor.YELLOW + castleName + ChatColor.GREEN + "' kalesini inşa etti!");
    }

    private void handleCastleDelete(Player player) {
        Clan clan = clanManager.getClanByPlayer(player.getUniqueId());
        if (clan == null || !clan.isOfficer(player.getUniqueId())) {
            player.sendMessage(ChatColor.RED + "Bunu sadece klan lideri veya yetkilileri yapabilir.");
            return;
        }

        com.armoyu.plugins.claims.Claim claim = claimManager.getClaimAt(player.getLocation());
        if (claim == null || claim.getOwnerClan() == null || !claim.getOwnerClan().equals(clan.getId())) {
            player.sendMessage(ChatColor.RED + "Klanınıza ait bir arsa üzerinde durmalısınız!");
            return;
        }

        // Bu arsadaki kaleyi doğrudan claim UUID ile bul ve sil
        String claimKey = claim.getId().toString();
        String[] info = clan.getCastles().get(claimKey);

        if (info != null) {
            clan.getCastles().remove(claimKey);
            clan.getLandSpawns().remove(claim.getId());
            clanManager.saveClans();
            player.sendMessage(ChatColor.GREEN + "'" + info[0] + "' kalesi başarıyla silindi.");
        } else {
            player.sendMessage(ChatColor.RED + "Bu arsada bir kale bulunamadı.");
        }
    }

    private void handleCastleRepair(Player player) {
        Clan clan = clanManager.getClanByPlayer(player.getUniqueId());
        if (clan == null || !clan.isOfficer(player.getUniqueId())) {
            player.sendMessage(ChatColor.RED + "Bunu sadece klan lideri veya yetkilileri yapabilir.");
            return;
        }

        com.armoyu.plugins.claims.Claim claim = claimManager.getClaimAt(player.getLocation());
        if (claim == null || claim.getOwnerClan() == null || !claim.getOwnerClan().equals(clan.getId())) {
            player.sendMessage(ChatColor.RED + "Klanınıza ait bir arsa üzerinde durmalısınız!");
            return;
        }

        // Bu arsadaki kaleyi doğrudan claim UUID ile bul
        String claimKey = claim.getId().toString();
        String[] castleInfo = clan.getCastles().get(claimKey);

        if (castleInfo == null) {
            player.sendMessage(ChatColor.RED + "Bu arsada tamir edilecek bir kale bulunamadı.");
            return;
        }

        double fullCost = com.armoyu.minecraftplugin.getPlugin(com.armoyu.minecraftplugin.class).getConfig()
                .getDouble("economy.castle_cost", 10000.0);
        double repairCost = fullCost * 0.5; // %50 tamir masrafı

        if (!clan.removeBalance(repairCost)) {
            player.sendMessage(
                    ChatColor.RED + "Klan kasasında yeterli para yok! (Tamir ücreti: " + repairCost + " ARMO)");
            return;
        }

        // Kaleyi tekrar inşa et (tamir)
        int centerX = (claim.getMinX() + claim.getMaxX()) / 2;
        int centerZ = (claim.getMinZ() + claim.getMaxZ()) / 2;
        org.bukkit.Location claimCenter = new org.bukkit.Location(
                player.getWorld(), centerX, player.getWorld().getHighestBlockYAt(centerX, centerZ), centerZ);

        new com.armoyu.plugins.structures.StructureManager().placeCastle(claimCenter, castleInfo[1], clan.getName());
        clanManager.saveClans();
        player.sendMessage(ChatColor.GREEN + "'" + castleInfo[0] + "' kalesi başarıyla tamir edildi! (Ücret: "
                + repairCost + " ARMO)");
    }

    private void handleClanLands(Player player) {
        Clan clan = clanManager.getClanByPlayer(player.getUniqueId());
        if (clan == null) {
            player.sendMessage(ChatColor.RED + "Bir klana üye değilsiniz!");
            return;
        }

        java.util.List<com.armoyu.plugins.claims.Claim> claims = claimManager.getClaimsByClan(clan.getId());
        player.sendMessage(ChatColor.GOLD + "=== " + clan.getName() + " Arsaları ===");

        if (claims.isEmpty()) {
            player.sendMessage(ChatColor.RED + "Klanınızın hiç arsası yok.");
        } else {
            int index = 1;
            for (com.armoyu.plugins.claims.Claim claim : claims) {
                int width = claim.getMaxX() - claim.getMinX() + 1;
                int length = claim.getMaxZ() - claim.getMinZ() + 1;
                int area = width * length;
                String claimKey = claim.getId().toString();
                String[] castle = clan.getCastles().get(claimKey);

                String castleStr = (castle != null)
                        ? ChatColor.GREEN + " 🏰 " + castle[0] + " (" + castle[1] + ")"
                        : ChatColor.GRAY + " (Kale yok)";

                player.sendMessage(ChatColor.YELLOW + "#" + index + " " + ChatColor.WHITE
                        + width + "x" + length + ChatColor.GRAY + " (" + area + " blok) "
                        + ChatColor.DARK_GRAY + "[" + claim.getMinX() + "," + claim.getMinZ()
                        + " → " + claim.getMaxX() + "," + claim.getMaxZ() + "]"
                        + castleStr);
                index++;
            }
        }

        player.sendMessage(ChatColor.GRAY + "Toplam: " + claims.size() + " arsa");
        player.sendMessage(ChatColor.GOLD + "================================");
    }

    private void handleClanCastlesList(Player player) {
        java.util.Collection<com.armoyu.plugins.clans.Clan> allClans = clanManager.getAllClans();
        boolean found = false;

        player.sendMessage(ChatColor.GOLD + "=== İnşa Edilen Klan Kaleleri ===");
        for (com.armoyu.plugins.clans.Clan clan : allClans) {
            for (java.util.Map.Entry<String, String[]> entry : clan.getCastles().entrySet()) {
                player.sendMessage(ChatColor.YELLOW + "- " + ChatColor.GREEN + clan.getName() +
                        ChatColor.WHITE + ": " + ChatColor.YELLOW + entry.getValue()[0] +
                        ChatColor.GRAY + " (" + entry.getValue()[1] + ") ID: " + entry.getKey());
                found = true;
            }
            // Eski tek kale sistemi uyumu
            if (!found && clan.getCastleName() != null) {
                player.sendMessage(ChatColor.YELLOW + "- " + ChatColor.GREEN + clan.getName() +
                        ChatColor.WHITE + ": " + ChatColor.YELLOW + clan.getCastleName() +
                        ChatColor.GRAY + " (" + clan.getCastleType() + ")");
                found = true;
            }
        }

        if (!found) {
            player.sendMessage(ChatColor.RED + "Henüz kimsede kale yok.");
        }
        player.sendMessage(ChatColor.GOLD + "================================");
    }

    private void handleCastleTeleport(Player player, String[] args) {
        String clanName = args[1];
        Clan targetClan = clanManager.getClanByName(clanName);

        if (targetClan == null) {
            player.sendMessage(ChatColor.RED + "Klan bulunamadı.");
            return;
        }

        if (targetClan.getSpawn() == null) {
            player.sendMessage(ChatColor.RED + "Bu klanın bir merkezi (kalesi) bulunmuyor.");
            return;
        }

        if (teleportManager.hasActiveTask(player)) {
            player.sendMessage(ChatColor.YELLOW + "Zaten bir ışınlanma işleminiz var.");
            return;
        }

        double cost = com.armoyu.minecraftplugin.getPlugin(com.armoyu.minecraftplugin.class).getConfig()
                .getDouble("economy.teleport_costs.spawn", 100.0);

        if (!moneyManager.hasEnough(player.getUniqueId(), cost)) {
            player.sendMessage(ChatColor.RED + "Kaleye ışınlanmak için yeterli paranız yok! Gerekli: " + cost);
            return;
        }

        int cooldown = 10; // Sabit 10 saniye bekleme
        player.sendMessage(ChatColor.YELLOW + targetClan.getName() + " kalesine gidiliyor... Ücret: " + cost + ". "
                + cooldown + " saniye hareket etmeyin.");

        moneyManager.removeMoney(player.getUniqueId(), cost);

        final org.bukkit.Location finalLoc = targetClan.getSpawn();
        final String finalClanName = targetClan.getName();

        BukkitRunnable runnable = new BukkitRunnable() {
            int seconds = cooldown;

            @Override
            public void run() {
                if (!teleportManager.hasActiveTask(player)) {
                    this.cancel();
                    return;
                }

                if (seconds > 0) {
                    player.sendTitle(ChatColor.BLUE + String.valueOf(seconds), "", 0, 20, 0);
                    seconds--;
                } else {
                    player.teleport(finalLoc);
                    player.sendMessage(ChatColor.GREEN + finalClanName + " kalesine ışınlandınız.");
                    teleportManager.removeTask(player);
                    this.cancel();
                }
            }
        };

        int taskId = runnable
                .runTaskTimer(com.armoyu.minecraftplugin.getPlugin(com.armoyu.minecraftplugin.class), 0, 20)
                .getTaskId();
        teleportManager.addTask(player, taskId);
    }

    private void handleClanList(Player player) {
        java.util.Collection<Clan> allClans = clanManager.getAllClans();
        if (allClans.isEmpty()) {
            player.sendMessage(ChatColor.RED + "Henüz hiç klan kurulmamış.");
            return;
        }

        player.sendMessage(ChatColor.GOLD + "=== Klan Listesi ===");
        for (Clan clan : allClans) {
            player.sendMessage(ChatColor.YELLOW + "- " + ChatColor.WHITE +
                    clan.getName() + " [" + clan.getTag() + "] - Seviye: " + clan.getLevel());
        }
    }

    private void handleOfficerList(Player player) {
        Clan clan = clanManager.getClanByPlayer(player.getUniqueId());
        if (clan == null) {
            player.sendMessage(ChatColor.RED + "Bir klana üye değilsiniz!");
            return;
        }

        player.sendMessage(ChatColor.GOLD + "=== " + clan.getName() + " Yetkilileri ===");
        String leaderName = Bukkit.getOfflinePlayer(clan.getLeader()).getName();
        player.sendMessage(
                ChatColor.YELLOW + "♛ Lider: " + ChatColor.WHITE + (leaderName != null ? leaderName : "Bilinmiyor"));

        if (clan.getOfficers().isEmpty()) {
            player.sendMessage(ChatColor.GRAY + "Henüz yetkili yok.");
        } else {
            for (UUID officerId : clan.getOfficers()) {
                if (officerId.equals(clan.getLeader()))
                    continue;
                String name = Bukkit.getOfflinePlayer(officerId).getName();
                player.sendMessage(ChatColor.YELLOW + "★ " + ChatColor.WHITE + (name != null ? name : "Bilinmiyor"));
            }
        }
        player.sendMessage(ChatColor.GOLD + "================================");
    }

    private void sendHelp(Player player) {
        player.sendMessage(ChatColor.GOLD + "=== [ ARMOYU KLAN SİSTEMİ ] ===");
        player.sendMessage(ChatColor.YELLOW + "/klan kur <isim> <etiket> " + ChatColor.GRAY + "- Yeni bir klan kurar.");
        player.sendMessage(ChatColor.YELLOW + "/klan ayril " + ChatColor.GRAY + "- Mevcut klanınızdan ayrılırsınız.");
        player.sendMessage(
                ChatColor.YELLOW + "/klan davet <oyuncu> " + ChatColor.GRAY + "- Oyuncuyu klanınıza davet eder.");
        player.sendMessage(
                ChatColor.YELLOW + "/klan katil <klan_ismi> " + ChatColor.GRAY + "- Daveti kabul edip klana girer.");
        player.sendMessage(
                ChatColor.YELLOW + "/klan para <yatir|cek> " + ChatColor.GRAY + "- Klan kasasıyla etkileşim.");
        player.sendMessage(ChatColor.YELLOW + "/klan profil [isim] " + ChatColor.GRAY + "- Klan detaylarını gösterir.");
        player.sendMessage(
                ChatColor.YELLOW + "/klan spawn [no] " + ChatColor.GRAY + "- Klan merkeze veya arsalara ışınlar.");
        player.sendMessage(
                ChatColor.YELLOW + "/klan setspawn [arsa] " + ChatColor.GRAY + "- Işınlanma noktasını ayarlar.");
        player.sendMessage(ChatColor.YELLOW + "/klan banka " + ChatColor.GRAY + "- Ortak klan kasasını açar.");
        player.sendMessage(ChatColor.YELLOW + "/klan topla " + ChatColor.GRAY + "- Tüm üyeleri yanınıza çağırır.");
        player.sendMessage(ChatColor.YELLOW + "/klan sevyeatla " + ChatColor.GRAY + "- Klan seviyesini yükseltir.");
        player.sendMessage(ChatColor.YELLOW + "/klan liste " + ChatColor.GRAY + "- Kurulmuş tüm klanları listeler.");
        player.sendMessage(
                ChatColor.YELLOW + "/klan claim " + ChatColor.GRAY + "- Durduğunuz yeri klan arazisi yapar.");
        player.sendMessage(
                ChatColor.YELLOW + "/klan yetki <ver|al> <isim> " + ChatColor.GRAY + "- Yetkili yönetimi sağlar.");
        player.sendMessage(
                ChatColor.YELLOW + "/klan yetkililer " + ChatColor.GRAY + "- Klan yetkililerini listeler.");
        player.sendMessage(ChatColor.YELLOW + "/klan kale <isim> " + ChatColor.GRAY + "- Bir klanın kalesine ışınlar.");
        player.sendMessage(
                ChatColor.YELLOW + "/klan kaleal <isim> <tip> " + ChatColor.GRAY + "- Klan kalesi inşa eder.");
        player.sendMessage(
                ChatColor.YELLOW + "/klan kaleler " + ChatColor.GRAY + "- İnşa edilen tüm klan kalelerini listeler.");
        player.sendMessage(
                ChatColor.YELLOW + "/klan arsalar " + ChatColor.GRAY + "- Klan arsalarını listeler.");
        player.sendMessage(ChatColor.GOLD + "================================");
    }
}
