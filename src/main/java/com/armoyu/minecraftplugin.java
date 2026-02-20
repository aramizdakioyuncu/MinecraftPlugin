package com.armoyu;

import com.armoyu.plugins.actionmanager.ActionManager;
import com.armoyu.plugins.auth.commands.LoginRegisterCommand;
import com.armoyu.plugins.authspawn.AuthSpawnListener;
import com.armoyu.plugins.authspawn.SpawnManager;
import com.armoyu.plugins.authspawn.AuthSpawnCommand;
import com.armoyu.plugins.claims.ClaimManager;
import com.armoyu.plugins.claims.ClaimListener;
import com.armoyu.plugins.claims.commands.ClaimCommand;
import com.armoyu.plugins.clans.ClanManager;
import com.armoyu.plugins.clans.ClanListener;
import com.armoyu.plugins.clans.ClanVaultGUI;
import com.armoyu.plugins.clans.commands.ClanCommand;
import com.armoyu.plugins.economy.MoneyManager;
import com.armoyu.plugins.economy.commands.MoneyCommand;
import com.armoyu.plugins.kits.KitManager;
import com.armoyu.plugins.kits.KitCommand;
import com.armoyu.plugins.rcon.RconCommand;
import com.armoyu.plugins.sethome.HomeCommand;
import com.armoyu.plugins.sethome.HomeManager;
import com.armoyu.plugins.sethome.HomeListener;
import com.armoyu.plugins.sleeppingbed.SleepingBedManager;
import com.armoyu.plugins.sleeppingbed.SleepingBedListener;
import com.armoyu.plugins.teleport.TeleportManager;
import com.armoyu.plugins.teleport.TeleportListener;
import com.armoyu.plugins.teleport.TpaCommand;
import com.armoyu.plugins.teleport.TpAcceptCommand;
import com.armoyu.plugins.teleport.TpDenyCommand;
import com.armoyu.plugins.trader.TraderManager;
import com.armoyu.plugins.trader.TraderGUI;
import com.armoyu.plugins.trader.TraderCommand;
import com.armoyu.plugins.ui.ScoreManager;
import com.armoyu.utils.ChatUtils;
import org.bukkit.Color;
import org.bukkit.plugin.java.JavaPlugin;

public final class minecraftplugin extends JavaPlugin {

        private static ScoreManager scoreManager;
        private static ClanManager clanManager;

        public static ScoreManager getScoreManager() {
                return scoreManager;
        }

        @Override
        public void onEnable() {
                // Plugin startup logic

                ChatUtils.consoleSendMessage("PLugin etkinlestirildi!!");
                ChatUtils.consoleSendMessage("Yesil Renk", Color.GREEN);
                ChatUtils.consoleSendMessage("Kirmizi Renk", Color.RED);

                // SleepingBed
                SleepingBedManager sleepingBedManager = new SleepingBedManager(this);
                getServer().getPluginManager().registerEvents(new SleepingBedListener(sleepingBedManager), this);

                saveDefaultConfig();

                ActionManager actionManager = new ActionManager();
                getServer().getPluginManager().registerEvents(actionManager, this);

                // AuthSpawn
                SpawnManager spawnManager = new SpawnManager(this);
                getServer().getPluginManager().registerEvents(new AuthSpawnListener(spawnManager, actionManager), this);
                getCommand("authspawn").setExecutor(new AuthSpawnCommand(spawnManager, actionManager));

                // RCON Admin
                getCommand("rcon").setExecutor(new RconCommand(this, actionManager));

                // Economy
                MoneyManager moneyManager = new MoneyManager(this);
                getCommand("para").setExecutor(new MoneyCommand(moneyManager, actionManager));

                // login ve register komutlarını tek executor ile bağla
                LoginRegisterCommand loginCommand = new LoginRegisterCommand(actionManager, spawnManager);
                getCommand("login").setExecutor(loginCommand);
                getCommand("register").setExecutor(loginCommand);

                // Teleport
                TeleportManager teleportManager = new TeleportManager();
                getServer().getPluginManager().registerEvents(new TeleportListener(teleportManager), this);

                getCommand("tpa").setExecutor(new TpaCommand(teleportManager, actionManager));
                getCommand("tpaccept").setExecutor(new TpAcceptCommand(teleportManager, actionManager, this));
                getCommand("tpdeny").setExecutor(new TpDenyCommand(teleportManager, actionManager));

                // SetHome
                HomeManager homeManager = new HomeManager();
                getCommand("home").setExecutor(new HomeCommand(actionManager, homeManager, this));
                getServer().getPluginManager().registerEvents(new HomeListener(homeManager), this);

                // Trader System
                TraderManager traderManager = new TraderManager(moneyManager);
                TraderGUI traderGUI = new TraderGUI(traderManager);
                getCommand("tuccar").setExecutor(new TraderCommand(traderManager, traderGUI));
                getServer().getPluginManager().registerEvents(traderGUI, this);

                // Clan System
                clanManager = new ClanManager(this);
                ClanListener clanListener = new ClanListener(clanManager);
                ClanVaultGUI vaultGUI = new ClanVaultGUI(clanManager);

                // Claim System
                ClaimManager claimManager = new ClaimManager(this);
                ClaimListener claimListener = new ClaimListener(claimManager, clanManager);

                getCommand("klan").setExecutor(
                                new ClanCommand(clanManager, moneyManager, clanListener, vaultGUI, teleportManager,
                                                claimManager));
                getCommand("claim").setExecutor(new ClaimCommand(claimManager, moneyManager, clanManager));

                // Kit System
                KitManager kitManager = new KitManager(this);
                getCommand("kit").setExecutor(new KitCommand(kitManager, moneyManager, actionManager));

                getServer().getPluginManager().registerEvents(clanListener, this);
                getServer().getPluginManager().registerEvents(vaultGUI, this);
                getServer().getPluginManager().registerEvents(claimListener, this);

                // UI (Moved down to include clanManager)
                scoreManager = new ScoreManager(moneyManager, clanManager);
                moneyManager.setScoreManager(scoreManager);
        }

        @Override
        public void onDisable() {
                // Plugin shutdown logic

                if (clanManager != null) {
                        clanManager.saveClans();
                }
                ChatUtils.consoleSendMessage("Plugin Kapatıldı!");

        }
}
