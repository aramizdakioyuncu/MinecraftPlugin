package com.armoyu.plugins.auth.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.armoyu.plugins.actionmanager.ActionManager;
import com.armoyu.utils.PlayerRole;

public class LoginRegisterCommand implements CommandExecutor {

        private final ActionManager actionManager;

        public LoginRegisterCommand(ActionManager actionManager) {
                this.actionManager = actionManager;
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
                                actionManager.setRole(player, PlayerRole.REGISTERED);
                                player.sendMessage("Giriş başarılı! Rolün registered olarak atandı.");
                                break;

                        case "register":
                                if (args.length < 1) {
                                        player.sendMessage("/register <parola>");
                                        return true;
                                }
                                actionManager.setRole(player, PlayerRole.REGISTERED);
                                player.sendMessage("Kayıt başarılı! Rolün registered olarak atandı.");
                                break;

                        default:
                                return false;
                }
                return true;
        }
}
