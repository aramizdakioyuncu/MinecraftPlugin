package com.armoyu.plugins.teleport.listener;

import com.armoyu.minecraftplugin;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Map;

public class teleport {


    // Komut işleyici sınıfı
    public static class MyCommandExecutor implements CommandExecutor {


        @Override
        public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {


            if (command.getName().equalsIgnoreCase("tpa")) {

                if (!(sender instanceof Player)) {
                    sender.sendMessage("Bu komutu yalnızca bir oyuncu çalıştırabilir!");
                    return true;
                }

                Player senderPlayer = (Player) sender;

                // Komut argümanlarını kontrol et
                if (args.length < 1) {
                    senderPlayer.sendMessage("/tpa <oyuncu adı>");
                    return true;
                }

                // Hedef oyuncuyu bul
                Player target = senderPlayer.getServer().getPlayer(args[0]);
                if (target == null || !target.isOnline()) {
                    senderPlayer.sendMessage("Belirtilen oyuncu bulunamadı veya çevrimdışı.");
                    return true;
                }

                //Kritik//
                minecraftplugin.teleportRequests.put(target, senderPlayer);
                //Kritik//

                minecraftplugin.sendMessagePlayer(senderPlayer.getName() + " yanına ışınlanmak istiyor. Kabul etmek için /tpaccept yaz.", target);
                minecraftplugin.sendMessagePlayer(target.getName() + " oyuncusuna ışınlanma isteği gönderildi.", senderPlayer);

                return true;
            }


            if (command.getName().equalsIgnoreCase("tpaccept")) {

                if (!(sender instanceof Player)) {
                    sender.sendMessage("Bu komutu yalnızca bir oyuncu çalıştırabilir!");
                    return true;
                }

                Player senderPlayer = (Player) sender;

                // Komut argümanlarını kontrol et
                if (args.length < 1) {
                    senderPlayer.sendMessage("/tpaccept <oyuncu adı>");
                    return true;
                }

                // Hedef oyuncuyu bul
                Player target = senderPlayer.getServer().getPlayer(args[0]);
                if (target == null || !target.isOnline()) {
                    senderPlayer.sendMessage("Belirtilen oyuncu bulunamadı veya çevrimdışı.");
                    return true;
                }


                boolean isRequesthave = false;
                Player player = null;

                for (Map.Entry<Player, Player> entry : minecraftplugin.teleportRequests.entrySet()) {
                    if (entry.getKey().equals(senderPlayer)) {
                        isRequesthave = true;
                        player = entry.getValue();
                        break;
                    }
                }
                if (!isRequesthave) {
                    senderPlayer.sendMessage("İstek gelmemiş");
                    return true;
                }

                try {
                    player.teleport(senderPlayer.getLocation());
                    minecraftplugin.teleportRequests.entrySet().removeIf(entry ->
                            entry.getKey().equals(senderPlayer) && entry.getValue().equals(target)
                    );

                    return true;

                } catch (Exception e) {
                    minecraftplugin.consoleSendMessage(e.toString());
                    return true;
                }


            }

            return true;

        }
    }

}
