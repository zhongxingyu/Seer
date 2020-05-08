 /* This program is free software. It comes without any warranty, to
  * the extent permitted by applicable law. You can redistribute it
  * and/or modify it under the terms of the Do What The Fuck You Want
  * To Public License, Version 2, as published by Sam Hocevar. See
  * http://sam.zoy.org/wtfpl/COPYING for more details. */
 
 package net.openprog.gambler;
 
 import java.util.logging.Logger;
 
 import net.milkbowl.vault.economy.Economy;
 import net.openprog.gambler.command.Commandgamble;
 import net.openprog.gambler.command.Commandgambler;
 
 import org.bukkit.ChatColor;
 import org.bukkit.plugin.RegisteredServiceProvider;
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class Gambler extends JavaPlugin {
 	private static final Logger log = Logger.getLogger("Minecraft");
 	public static Economy econ = null;
 	public static String chatPrefix = ChatColor.RESET + "[" + ChatColor.DARK_RED + "Gambler" + ChatColor.RESET + "] ";
 	public static String errorPrefix = ChatColor.RESET + "[" + ChatColor.DARK_RED + "Gambler" + ChatColor.RESET + "] " + ChatColor.RED + "Error: " + ChatColor.RESET;
 	private Commandgambler gamblerExecutor;
 	private Commandgamble gambleExecutor;
 	
 	@Override
 	public void onEnable() {
 		if (!setupEconomy()) {
 			log.info(String.format("[%s] Disabling due to no Vault or Economy Plugin dependency found!", getDescription().getName()));
 			getServer().getPluginManager().disablePlugin(this);
 			return;
 		}
 		
 		loadConfig();
 		gamblerExecutor = new Commandgambler(this);
 		gambleExecutor = new Commandgamble(this);
 		getCommand("gambler").setExecutor(gamblerExecutor);
 		getCommand("gamble").setExecutor(gambleExecutor);
 		log.info(String.format("[%s] Successfully enabled version %s", getDescription().getName(), getDescription().getVersion()));
 	}
 	
 	@Override
 	public void onDisable() {
 		log.info(String.format("[%s] Disabled Version %s", getDescription().getName(), getDescription().getVersion()));
 	}
 	
 	private boolean setupEconomy() {
 		if (getServer().getPluginManager().getPlugin("Vault") == null) {
 			return false;
 		}
 		
 		RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
 		
 		if (rsp == null) {
 			return false;
 		}
 		
 		econ = rsp.getProvider();
 		return econ != null;
 	}
 	
 	public void loadConfig() {
 		getConfig().options().copyDefaults(true);
 		
 		getConfig().addDefault("max-bet", 1000);
 		getConfig().addDefault("winning-diceroll-threshold", 65);
 		getConfig().addDefault("dicerollmax", 100);
 		getConfig().options().copyDefaults(true);
 		saveConfig();
 	}
 }
