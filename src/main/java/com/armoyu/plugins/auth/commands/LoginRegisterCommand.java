package com.armoyu.plugins.auth.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.armoyu.plugins.actionmanager.ActionManager;
import com.armoyu.utils.PlayerRole;

import com.armoyu.plugins.authspawn.SpawnManager;
import org.bukkit.Location;

public class LoginRegisterCommand implements CommandExecutor {

        private final ActionManager actionManager;
        private final SpawnManager spawnManager;

        public LoginRegisterCommand(ActionManager actionManager, SpawnManager spawnManager) {
                this.actionManager = actionManager;
                this.spawnManager = spawnManager;
        }

        @Override
        public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
                if (!(sender instanceof Player)) {
                        sender.sendMessage("Bu komutu yalnızca bir oyuncu çalıştırabilir!");
                        return true;
                }

                Player player = (Player) sender;

                switch (command.getName().toLowerCase()) {
                        case "login":
                                if (args.length < 1) {
                                        player.sendMessage("/login <parola>");
                                        return true;
                                }
                                completeLogin(player);
                                break;

                        case "register":
                                if (args.length < 1) {
                                        player.sendMessage("/register <parola>");
                                        return true;
                                }
                                completeLogin(player);
                                break;

                        default:
                                return false;
                }
                return true;
        }

        private void completeLogin(Player player) {
                actionManager.setRole(player, PlayerRole.REGISTERED);
                player.setGameMode(org.bukkit.GameMode.SURVIVAL);
                player.sendMessage("Giriş başarılı! Rolün registered olarak atandı ve Survival moda geçtiniz.");

                // Giriş başarılı olduktan sonra son konumuna geri gönder
                if (spawnManager.hasLastLocation(player)) {
                        Location lastLoc = spawnManager.getLastLocation(player);
                        player.teleport(lastLoc);
                        player.sendMessage("En son kaldığınız yere ışınlandınız.");
                }
        }
}
