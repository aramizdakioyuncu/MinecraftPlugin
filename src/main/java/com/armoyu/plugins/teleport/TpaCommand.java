package com.armoyu.plugins.teleport;

import com.armoyu.plugins.actionmanager.ActionManager;
import com.armoyu.plugins.economy.MoneyManager;
import com.armoyu.utils.ChatUtils;
import com.armoyu.utils.PlayerPermission;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class TpaCommand implements CommandExecutor {

    private final TeleportManager teleportManager;
    private final ActionManager actionManager;
    private final MoneyManager moneyManager;
    private final JavaPlugin plugin;

    public TpaCommand(TeleportManager teleportManager, ActionManager actionManager, MoneyManager moneyManager,
            JavaPlugin plugin) {
        this.teleportManager = teleportManager;
        this.actionManager = actionManager;
        this.moneyManager = moneyManager;
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Bu komutu sadece oyuncular kullanabilir.");
            return true;
        }

        Player player = (Player) sender;

        if (!actionManager.hasPermission(player, PlayerPermission.TELEPORT)) {
            player.sendMessage(ChatColor.RED + "Bu komutu kullanmak için yetkiniz yok.");
            return true;
        }

        if (args.length < 1) {
            player.sendMessage(ChatColor.RED + "Kullanım: /tpa <oyuncu>");
            return true;
        }

        Player target = player.getServer().getPlayer(args[0]);
        if (target == null || !target.isOnline()) {
            player.sendMessage(ChatColor.RED + "Oyuncu bulunamadı veya çevrimdışı.");
            return true;
        }

        if (target.equals(player)) {
            player.sendMessage(ChatColor.RED + "Kendinize istek atamazsınız.");
            return true;
        }

        double cost = plugin.getConfig().getDouble("economy.teleport_costs.tpa", 25.0);
        if (!moneyManager.hasEnough(player.getUniqueId(), cost)) {
            player.sendMessage(ChatColor.RED + "TPA isteği göndermek için yeterli paranız yok! Gerekli: " + cost);
            return true;
        }

        moneyManager.removeMoney(player.getUniqueId(), cost);
        teleportManager.addRequest(player, target);

        ChatUtils.sendMessagePlayer(player,
                ChatColor.GREEN + target.getName() + " oyuncusuna ışınlanma isteği gönderildi. Ücret: " + cost
                        + " ARMO");

        // Alıcıya havalı tıklanabilir mesaj gönder
        target.sendMessage(ChatUtils.ARMOYUTag + ChatColor.YELLOW + player.getName() + ChatColor.GOLD
                + " size ışınlanmak istiyor.");

        TextComponent acceptBtn = new TextComponent("  [ KABUL ET ]  ");
        acceptBtn.setColor(net.md_5.bungee.api.ChatColor.GREEN);
        acceptBtn.setBold(true);
        acceptBtn.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/tpaccept"));
        acceptBtn.setHoverEvent(
                new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("§aTıklayarak kabul edebilirsin!")));

        TextComponent denyBtn = new TextComponent("  [ REDDET ]  ");
        denyBtn.setColor(net.md_5.bungee.api.ChatColor.RED);
        denyBtn.setBold(true);
        denyBtn.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/tpdeny"));
        denyBtn.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("§cTıklayarak reddedebilirsin!")));

        target.spigot().sendMessage(acceptBtn, new TextComponent(" "), denyBtn);

        return true;
    }
}
