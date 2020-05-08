 /*
  * This file is part of MineQuest, The ultimate MMORPG plugin!.
  * MineQuest is licensed under GNU General Public License v3.
  * Copyright (C) 2012 The MineQuest Team
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package com.theminequest.MineQuest;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.logging.Level;
 
 import net.milkbowl.vault.economy.Economy;
 import net.milkbowl.vault.permission.Permission;
 
 import org.bukkit.Bukkit;
 import org.bukkit.plugin.PluginDescriptionFile;
 import org.bukkit.plugin.RegisteredServiceProvider;
 import org.bukkit.plugin.java.JavaPlugin;
 
 import com.alta189.simplesave.exceptions.ConnectionException;
 import com.theminequest.MQCoreEvents.RegisterEvents;
 import com.theminequest.MQCoreRequirements.RegisterRequirements;
 import com.theminequest.MineQuest.API.Managers;
 import com.theminequest.MineQuest.API.Edit.EditManager;
 import com.theminequest.MineQuest.API.Events.EventManager;
 import com.theminequest.MineQuest.API.Tracker.LogStatistic;
 import com.theminequest.MineQuest.API.Tracker.SnapshotStatistic;
 import com.theminequest.MineQuest.API.Tracker.StatisticManager;
 import com.theminequest.MineQuest.API.Utils.GriefcraftMetrics;
 import com.theminequest.MineQuest.API.Utils.UtilManager;
 import com.theminequest.MineQuest.Events.MQEventManager;
 import com.theminequest.MineQuest.Frontend.Command.CommandListener;
 import com.theminequest.MineQuest.Frontend.Command.PartyCommandFrontend;
 import com.theminequest.MineQuest.Frontend.Command.QuestCommandFrontend;
 import com.theminequest.MineQuest.Frontend.Sign.QuestSign;
 import com.theminequest.MineQuest.Group.MQQuestGroupManager;
 import com.theminequest.MineQuest.Quest.QuestManager;
 import com.theminequest.MineQuest.Quest.Requirements.MQRequirementManager;
 import com.theminequest.MineQuest.Target.TargetManager;
 
 /**
  * MineQuest Plugin Class for Bukkit
  * @author The MineQuest Party
  *
  */
 public class MineQuest extends JavaPlugin {
 
 	/**
 	 * Access Permissions via Vault
 	 */
 	public static Permission permission = null;
 	/**
 	 * Access Economy via Vault
 	 */
 	public static Economy economy = null;
 	/**
 	 * Access MineQuest configuration
 	 */
 	public static QuestConfig configuration = null;
 	/**
 	 * Access Hidendra's Metrics
 	 */
 	public static GriefcraftMetrics gcMetrics = null;
 	
 	/**
 	 * Access private instance of this plugin.
 	 */
 	private static MineQuest active = null;
 	
 	/**
 	 * Access main /minequest help menu modifier
 	 */
 	public static CommandListener commandListener = null;
 
 	/**
 	 * Get this build version of MineQuest.
 	 * @return Build version.
 	 */
 	public static String getVersion() {
		return active.getDescription().getVersion();
 	}
 
 	/**
 	 * Get this central plugin name.
 	 * @return Name.
 	 */
 	public static String getPluginName() {
		return active.getDescription().getName();
 	}
 	
 	/**
 	 * Get the underlying jarfile
 	 * @return Underlying Jarfile
 	 */
 	public static File getFileReference() {
 		return active.getFile();
 	}
 
 	private boolean setupPermissions() {
 		RegisteredServiceProvider<Permission> permissionProvider = getServer()
 				.getServicesManager().getRegistration(
 						net.milkbowl.vault.permission.Permission.class);
 		if (permissionProvider != null) {
 			permission = permissionProvider.getProvider();
 		}
 		return (permission != null);
 	}
 
 	private boolean setupEconomy() {
 		RegisteredServiceProvider<Economy> economyProvider = getServer()
 				.getServicesManager().getRegistration(
 						net.milkbowl.vault.economy.Economy.class);
 		if (economyProvider != null) {
 			economy = economyProvider.getProvider();
 		}
 		return (economy != null);
 	}
 
 	@Override
 	public void onEnable() {
 		if (getServer().getPluginManager().getPlugin("Vault") == null) {
 			getServer().getLogger().severe("================= MineQuest ==================");
 			getServer().getLogger().severe("Vault is required for MineQuest to operate!");
 			getServer().getLogger().severe("Please install Vault first!");
 			getServer().getLogger().severe("You can find the latest version here:");
 			getServer().getLogger().severe("http://dev.bukkit.org/server-mods/vault/");
 			getServer().getLogger().severe("==============================================");
 			setEnabled(false);
 			return;
 		}
 		if (!getDataFolder().exists())
 			getDataFolder().mkdirs();
 		active = this;
 		Managers.setActivePlugin(this);
		if (getVersion().equals("unofficialDev")){
 			Managers.log(Level.SEVERE,"[Core] You're using an unofficial dev build!");
 			Managers.log(Level.SEVERE,"[Core] We cannot provide support for this unless you know the GIT hash.");
 		}
 		configuration = new QuestConfig();
 		try {
 			Statistics s = new Statistics();
 			Managers.setStatisticManager(s);
 			Managers.setQuestStatisticManager(s);
 			Managers.getStatisticManager().registerStatistic(LogStatistic.class);
 			Managers.getStatisticManager().registerStatistic(SnapshotStatistic.class);
 			getServer().getPluginManager().registerEvents(s, this);
 		} catch (ConnectionException e1) {
 			Managers.log(Level.SEVERE,"[Core] Can't start Statistic Manager!");
 			e1.fillInStackTrace();
 			e1.printStackTrace();
 			Bukkit.getPluginManager().disablePlugin(this);
 		}
 		Managers.setEditManager(new EditManager());
 		getServer().getPluginManager().registerEvents(Managers.getEditManager(), this);
 		Managers.setEventManager(new MQEventManager());
 		getServer().getPluginManager().registerEvents(Managers.getEventManager(), this);
 		Managers.setQuestGroupManager(new MQQuestGroupManager());
 		Managers.setGroupManager(Managers.getQuestGroupManager());
 		getServer().getPluginManager().registerEvents(Managers.getGroupManager(), this);
 		Managers.setQuestManager(new QuestManager());
 		getServer().getPluginManager().registerEvents(Managers.getQuestManager(), this);
 		Managers.setUtilManager(new UtilManager());
 		getServer().getPluginManager().registerEvents(Managers.getUtilManager(), this);
 		Managers.setRequirementManager(new MQRequirementManager());
 		Managers.setTargetManager(new TargetManager());
 		try {
 			gcMetrics = new GriefcraftMetrics(this);
 			gcMetrics.start();
 		} catch (IOException e) {
 			Managers.log(Level.WARNING, "[Metrics] Could not attach to Hidendra Metrics.");
 		}
 		
 		if (!setupPermissions())
 			Managers.log(Level.SEVERE,"[Vault] You don't seem to have any permissions plugin...");
 		if (!setupEconomy())
 			Managers.log(Level.SEVERE,"[Vault] You don't seem to have any economy plugin...");
 		RegisterEvents.registerEvents();
 		RegisterRequirements.registerRequirements();
 
 		// sign frontend
 		getServer().getPluginManager().registerEvents(new QuestSign(), this);
 		// command frontend
 		commandListener = new CommandListener();
 		getCommand("minequest").setExecutor(commandListener);
 		getCommand("quest").setExecutor(new QuestCommandFrontend());
 		getCommand("party").setExecutor(new PartyCommandFrontend());
 		
 		Bukkit.getScheduler().scheduleSyncDelayedTask(this, new Runnable(){
 			@Override
 			public void run() {
 				Managers.getQuestManager().reloadQuests();
 				try {
 					Managers.getStatisticManager().connect(true);
 				} catch (ConnectionException e) {
 					Managers.log(Level.SEVERE,"[Core] Can't start Statistic Manager!");
 					e.fillInStackTrace();
 					e.printStackTrace();
 					Bukkit.getPluginManager().disablePlugin(Managers.getActivePlugin());
 				}
 			}
 		});
 
 		Bukkit.getScheduler().scheduleSyncDelayedTask(this, new Runnable(){
 
 			@Override
 			public void run() {
 				Managers.log("Refreshing i18n messages...");
 				I18NMessage.getLocale();
 				Managers.log("Refreshed i18n messages!");
 			}
 			
 		});
 	}
 
 	@Override
 	public void onDisable() {
 		try {
 			StatisticManager statisticManager = Managers.getStatisticManager();
 			if (statisticManager != null)
 				statisticManager.connect(false);
 		} catch (ConnectionException e) {
 			e.printStackTrace();
 		}
 		active = null;
 		Managers.setActivePlugin(null);
 		EventManager eventManager = Managers.getEventManager();
 		if (eventManager != null) {
 			Managers.getEventManager().dismantleRunnable();
 			Managers.setEventManager(null);
 		}
 		commandListener = null;
 		Managers.setEditManager(null);
 		Managers.setQuestManager(null);
 		Managers.setGroupManager(null);
 		Managers.setUtilManager(null);
 		Managers.setRequirementManager(null);
 		Managers.setTargetManager(null);
 		configuration = null;
 		permission = null;
 		economy = null;
 	}
 }
