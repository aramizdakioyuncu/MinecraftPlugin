package com.armoyu.plugins.clans.commands;

import com.armoyu.plugins.clans.Clan;
import com.armoyu.plugins.clans.ClanManager;
import com.armoyu.plugins.clans.ClanListener;
import com.armoyu.plugins.clans.ClanVaultGUI;
import com.armoyu.plugins.economy.MoneyManager;
import com.armoyu.plugins.claims.ClaimManager;
import com.armoyu.plugins.claims.commands.ClaimCommand;
import com.armoyu.plugins.teleport.TeleportManager;
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

    public ClanCommand(ClanManager clanManager, MoneyManager moneyManager, ClanListener clanListener,
            ClanVaultGUI vaultGUI, TeleportManager teleportManager, ClaimManager claimManager) {
        this.clanManager = clanManager;
        this.moneyManager = moneyManager;
        this.clanListener = clanListener;
        this.vaultGUI = vaultGUI;
        this.teleportManager = teleportManager;
        this.claimManager = claimManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Bu bir oyuncu komutudur.");
            return true;
        }

        Player player = (Player) sender;

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
                handleSetSpawn(player);
                break;
            case "spawn":
                handleSpawn(player);
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
            case "kale":
                handleCastleList(player);
                break;
            case "kaleler":
                handleClanCastlesList(player);
                break;
            case "kaleal":
                handleBuyCastle(player, args);
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

        clanManager.createClan(player, name, tag);
        clanListener.updateTabName(player);
        player.sendMessage(ChatColor.GREEN + name + " klanı başarıyla kuruldu! Tag: [" + tag + "]");
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
        target.sendMessage(ChatColor.YELLOW + clan.getName() + " klanından davet aldınız! Katılmak için: /klan katil "
                + clan.getName());
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

    private void handleSetSpawn(Player player) {
        com.armoyu.plugins.clans.Clan clan = clanManager.getClanByPlayer(player.getUniqueId());
        if (clan == null || !clan.getLeader().equals(player.getUniqueId())) {
            player.sendMessage(ChatColor.RED + "Sadece klan lideri spawn noktası belirleyebilir.");
            return;
        }

        clan.setSpawn(player.getLocation());
        clanManager.saveClans();
        updateSpawnVisual(clan);
        player.sendMessage(ChatColor.GREEN + "Klan spawn noktası başarıyla ayarlandı!");
    }

    private void updateSpawnVisual(com.armoyu.plugins.clans.Clan clan) {
        if (clan.getSpawn() == null)
            return;

        org.bukkit.Location loc = clan.getSpawn().clone().add(0, 2.0, 0); // Biraz daha yüksek yapalım

        // Remove existing armor stands nearby
        loc.getWorld().getNearbyEntities(loc, 2, 3, 2).stream()
                .filter(e -> e instanceof org.bukkit.entity.ArmorStand)
                .filter(e -> e.getCustomName() != null && e.getCustomName().contains("Merkezi"))
                .forEach(org.bukkit.entity.Entity::remove);

        org.bukkit.entity.ArmorStand as = loc.getWorld().spawn(loc, org.bukkit.entity.ArmorStand.class);
        as.setVisible(false);
        as.setGravity(false);
        as.setBasePlate(false);
        as.setArms(false);
        as.setSmall(true);
        as.setCustomName(ChatColor.GOLD + "[" + ChatColor.YELLOW + clan.getName() + " Merkezi" + ChatColor.GOLD + "]");
        as.setCustomNameVisible(true);
        as.setMarker(true);
    }

    private void handleSpawn(Player player) {
        Clan clan = clanManager.getClanByPlayer(player.getUniqueId());
        if (clan == null) {
            player.sendMessage(ChatColor.RED + "Bir klanda değilsiniz.");
            return;
        }

        if (clan.getSpawn() == null) {
            player.sendMessage(ChatColor.RED + "Klan spawn noktası henüz ayarlanmamış.");
            return;
        }

        if (teleportManager.hasActiveTask(player)) {
            player.sendMessage(ChatColor.YELLOW + "Zaten bir ışınlanma işleminiz var.");
            return;
        }

        int cooldown = getSpawnCooldown(clan.getLevel());
        if (cooldown > 0) {
            player.sendMessage(
                    ChatColor.YELLOW + "Klan merkezine gidiliyor... " + cooldown + " saniye hareket etmeyin.");

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
                        player.teleport(clan.getSpawn());
                        player.sendMessage(ChatColor.GREEN + "Klan merkezine ışınlandınız.");
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
            player.teleport(clan.getSpawn());
            player.sendMessage(ChatColor.GREEN + "Klan merkezine ışınlandınız.");
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

        double cost = 500; // Örnek maliyet
        if (!clan.removeBalance(cost)) {
            player.sendMessage(ChatColor.RED + "Klan kasasında yeterli para yok! (Gereken: " + cost + " ARMO)");
            return;
        }
        clanManager.saveClans();

        player.sendMessage(ChatColor.GOLD + "Ekibi toplama başlatıldı! 5 saniye içinde herkes yanınıza ışınlanacak.");

        for (UUID memberId : clan.getMembers()) {
            Player member = Bukkit.getPlayer(memberId);
            if (member == null || member.equals(player))
                continue;

            member.sendMessage(
                    ChatColor.YELLOW + "Klan lideri ekibi topluyor! 5 saniye içinde yanına ışınlanıyorsunuz...");

            Bukkit.getScheduler().runTaskLater(com.armoyu.minecraftplugin.getPlugin(com.armoyu.minecraftplugin.class),
                    () -> {
                        member.teleport(player.getLocation());
                        equipArmor(member, clan.getLevel());
                        member.sendMessage(ChatColor.GREEN + "Liderin yanına ışınlandınız!");
                    }, 100L); // 5 saniye
        }
    }

    private void equipArmor(Player player, int level) {
        if (level < 3)
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

        double cost = clan.getLevel() * 5000;
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
        new ClaimCommand(claimManager, moneyManager, clanManager)
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
        player.sendMessage(ChatColor.GOLD + "=== Mevcut Kale Tipleri ===");
        player.sendMessage(
                ChatColor.YELLOW + "- " + ChatColor.WHITE + "Klasik" + ChatColor.GRAY + " (Taş Sur + Ahşap Kulübe)");
        player.sendMessage(
                ChatColor.YELLOW + "- " + ChatColor.WHITE + "Odun" + ChatColor.GRAY + " (Kütük Sur + Taş Kulübe)");
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
        if (clan == null || !clan.getLeader().equals(player.getUniqueId())) {
            player.sendMessage(ChatColor.RED + "Bunu sadece klan lideri yapabilir.");
            return;
        }

        if (clan.getSpawn() == null) {
            player.sendMessage(ChatColor.RED + "Önce klan spawn noktası belirlemelisiniz! (/klan setspawn)");
            return;
        }

        org.bukkit.Location spawn = clan.getSpawn();
        com.armoyu.plugins.claims.Claim claim = claimManager.getClaimAt(spawn);

        if (claim == null || claim.getOwnerClan() == null || !claim.getOwnerClan().equals(clan.getId())) {
            player.sendMessage(ChatColor.RED + "Klan merkeziniz size ait bir klan bölgesinde (claim) olmalıdır!");
            return;
        }

        // Check if claim is big enough (21x21 minimum centered at spawn)
        int minX = spawn.getBlockX() - 10;
        int maxX = spawn.getBlockX() + 10;
        int minZ = spawn.getBlockZ() - 10;
        int maxZ = spawn.getBlockZ() + 10;

        if (claim.getMinX() > minX || claim.getMaxX() < maxX || claim.getMinZ() > minZ || claim.getMaxZ() < maxZ) {
            player.sendMessage(ChatColor.RED
                    + "Klan bölgeniz (claim) kale için yeterli büyüklükte değil! (Minimum 21x21 gerekli)");
            return;
        }

        double cost = 10000;
        if (!clan.removeBalance(cost)) {
            player.sendMessage(ChatColor.RED + "Klan kasasında yeterli para yok! (Gereken: " + cost + " ARMO)");
            return;
        }

        clan.setCastleName(castleName);
        clan.setCastleType(castleType);
        clanManager.saveClans();
        new com.armoyu.plugins.structures.StructureManager().placeCastle(spawn, castleType, clan.getName());
        Bukkit.broadcastMessage(ChatColor.GOLD + "[🏰] " + ChatColor.GREEN + clan.getName() + " klanı, klan merkezine '"
                + ChatColor.YELLOW + castleName + ChatColor.GREEN + "' kalesini inşa etti!");
    }

    private void handleClanCastlesList(Player player) {
        java.util.Collection<com.armoyu.plugins.clans.Clan> allClans = clanManager.getAllClans();
        boolean found = false;

        player.sendMessage(ChatColor.GOLD + "=== İnşa Edilen Klan Kaleleri ===");
        for (com.armoyu.plugins.clans.Clan clan : allClans) {
            if (clan.getCastleName() != null) {
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

    private void sendHelp(Player player) {
        player.sendMessage(ChatColor.GOLD + "=== Klan Komutları ===");
        player.sendMessage(ChatColor.YELLOW + "/klan kur <isim> <tag>");
        player.sendMessage(ChatColor.YELLOW + "/klan davet <oyuncu>");
        player.sendMessage(ChatColor.YELLOW + "/klan katil <isim>");
        player.sendMessage(ChatColor.YELLOW + "/klan para <yatir|cek>");
        player.sendMessage(ChatColor.YELLOW + "/klan profil [isim]");
        player.sendMessage(ChatColor.YELLOW + "/klan spawn");
        player.sendMessage(ChatColor.YELLOW + "/klan setspawn");
        player.sendMessage(ChatColor.YELLOW + "/klan banka" + ChatColor.GRAY + " - Ortak kasayı açar.");
        player.sendMessage(ChatColor.YELLOW + "/klan topla" + ChatColor.GRAY + " - Tüm üyeleri yanınıza çağırır.");
        player.sendMessage(ChatColor.YELLOW + "/klan sevyeatla" + ChatColor.GRAY + " - Klan seviyesini artırır.");
        player.sendMessage(ChatColor.YELLOW + "/klan liste" + ChatColor.GRAY + " - Tüm klanları listeler.");
        player.sendMessage(ChatColor.YELLOW + "/klan claim" + ChatColor.GRAY + " - Seçili alanı klan bölgesi yapar.");
        player.sendMessage(ChatColor.YELLOW + "/klan kale" + ChatColor.GRAY + " - Kale tiplerini listeler.");
        player.sendMessage(
                ChatColor.YELLOW + "/klan kaleler" + ChatColor.GRAY + " - İnşa edilen tüm kaleleri listeler.");
        player.sendMessage(
                ChatColor.YELLOW + "/klan kaleal <isim> <tip>" + ChatColor.GRAY + " - İsimli kale inşa eder ($10k)..");
        player.sendMessage(ChatColor.YELLOW + "/klan yetki <ver|al> <isim>" + ChatColor.GRAY + " - Yetkili yönetimi.");
    }
}
