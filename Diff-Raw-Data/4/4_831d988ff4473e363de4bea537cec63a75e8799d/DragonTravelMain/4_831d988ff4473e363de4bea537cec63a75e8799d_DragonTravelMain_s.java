 package com.xemsdoom.dt;
 
 import com.xemsdoom.dt.anticheat.DragonTravelAntiCheat;
 import com.xemsdoom.dt.commands.Commands;
 import com.xemsdoom.dt.economy.EconomyHandler;
 import com.xemsdoom.dt.listeners.*;
 import com.xemsdoom.dt.modules.ConfigurationLoader;
 import com.xemsdoom.dt.modules.DatabaseLoader;
 import com.xemsdoom.dt.modules.FAQLoader;
 import com.xemsdoom.dt.modules.MessagesLoader;
 import com.xemsdoom.dt.movement.FlightEditor;
 import static com.xemsdoom.dt.movement.Waypoint.markers;
 import com.xemsdoom.dt.spout.DragonTravelSpout;
 import com.xemsdoom.metrics.Metrics;
 import com.xemsdoom.mexdb.MexDB;
 import java.io.*;
 import java.lang.reflect.Method;
 import java.net.URL;
 import java.util.HashMap;
 import java.util.logging.Logger;
 import net.milkbowl.vault.Vault;
 import net.milkbowl.vault.economy.Economy;
 import net.minecraft.server.v1_4_5.EntityTypes;
 import org.bukkit.Bukkit;
 import org.bukkit.Material;
 import org.bukkit.block.Block;
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.entity.LivingEntity;
 import org.bukkit.entity.Player;
 import org.bukkit.event.Listener;
 import org.bukkit.plugin.Plugin;
 import org.bukkit.plugin.PluginDescriptionFile;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.java.JavaPlugin;
 import org.getspout.spoutapi.sound.SoundManager;
 
 /**
  * Copyright (C) 2011-2012 Moser Luca/Philipp Wagner
  * moser.luca@gmail.com/mail@phiwa.eu
  * 
  * This file is part of DragonTravel.
  * 
  * DragonTravel is free software: you can redistribute it and/or modify it under
  * the terms of the GNU General Public License as published by the Free Software
  * Foundation, either version 3 of the License, or (at your option) any later
  * version.
  * 
  * DragonTravel is distributed in the hope that it will be useful, but WITHOUT
  * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
  * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
  * details.
  * 
  * You should have received a copy of the GNU General Public License along with
  * Foobar. If not, see <http://www.gnu.org/licenses/>.
  * 
  * Lovingly adopted and re-animated by ellbristow <admin@jigsawspain.com>
  * 
  */
 public class DragonTravelMain extends JavaPlugin {
 
 	/*
 	 * Bukkit Package Versioning Bypass START
 	 * 
 	 * This static block of code allows us to bypass the Bukkit
 	 * package-versioning, this means that DragonTravel will not break on
 	 * minor/major updates, when there isn't a significant change.
 	 */
 
 	private static boolean shutdown = false;
 
 	static {

 		// Check if Nover is already available
 		File nover = new File("plugins/", "NoverPackage.jar");
 		if (!nover.exists()) {
 
 			System.out.println("[DragonTravel] Downloading NoverPackage: Please wait!");
 			InputStream fileIn = null;
 			OutputStream fileOut = null;
 
 			try {
 
 				URL noverUrl = new URL("http://dl.xemsdoom.com/NoverPackage.jar");
 				fileIn = noverUrl.openStream();
 				fileOut = new FileOutputStream(nover);
 
 				byte[] buffer = new byte[2048];
 				int bytes;
 
 				while ((bytes = fileIn.read(buffer)) != -1) {
 					fileOut.write(buffer, 0, bytes);
 				}
 
 				System.out.println("[DragonTravel] Downloaded NoverPackage. Please restart the server!");
 				System.out.println("[DragonTravel] Make sure to enable 'enabledInAllPlugins' in NoverPackage!");
 
 			} catch (IOException ex) {
 				ex.printStackTrace();
 			} finally {
 				if (fileIn != null) {
 					try {
 						fileIn.close();
 					} catch (IOException e) {
 						e.printStackTrace();
 					}
 				}
 
 				if (fileOut != null) {
 					try {
 						fileOut.close();
 					} catch (IOException e) {
 						e.printStackTrace();
 					}
 				}
 
 				// Shutdown the server and prevent DragonTravel initialisation.
 				System.out.println("[DragonTravel] Shutting down server:");
 				try {
 					Thread.sleep(6000);
 				} catch (InterruptedException e) {
 					e.printStackTrace();
 				} finally {
 					Bukkit.shutdown();
 					shutdown = true;
 				}
 			}
 		}
 	}
 
 	/*
 	 * Bukkit Package Versioning Bypass End
 	 */
 
 	// Server
 	public static PluginManager pm;
 
 	public static DragonTravelMain instance;
 
 	// Booleans for EntityListener
 	public static boolean onlydragontraveldragons;
 	public static boolean alldragons;
 
 	// Config
 	public static FileConfiguration config;
 	public static Double ver = 1.5; // IMPORTANT
 
 	// Messages
 	public static File messagesFile; // Config erstellen
 	public static FileConfiguration messages;
 	public static Double messagesVer = 0.6; // IMPORTANT
 	MessagesLoader DTmessages;
 
 	// Commands
 	public static boolean onlysigns;
 
 	// Spout
 	public static SoundManager sound;
 	public static boolean sendplaymessage = false;
 	public static boolean spout = false;
 
 	public static boolean ignoreAntiMobspawnAreas = true;
 
 	// AntiCheat
 	public static boolean anticheat = false;
 
 	// DT Dragon
 	public static Double speed;
 
 	// Database
 	public static MexDB dbd; // DatabaseDestinations
 	public static MexDB dbs; // DatabaseStations
 	public static MexDB wps;
 	public static MexDB signs; // DatabaseSigns
 	public static MexDB players;// Player homes
 
 	// HashMaps for needed information on DragonTravelFunctions
 	public static HashMap<Player, XemDragon> TravelInformation = new HashMap<Player, XemDragon>();
 	public static HashMap<XemDragon, XemDragon> XemDragonRemoval = new HashMap<XemDragon, XemDragon>();
 
 	// Economy
 	public static boolean EconomyEnabled = false;
 	public static Economy Economy = null;
 
 	// Console Output
 	public static final Logger log = Logger.getLogger("Minecraft");
 
 	// Events
 	private static Listener entitiesListener;
 	private static Listener playersListener;
 	private static Listener blocksListener;
 	private static Listener inputListener;
 	private static Listener buttonListener;
 	private static Listener editorListener;
 	private static Listener flightsignListener;
 
 	@Override
 	public void onDisable() {
 
 		// Removes all XemDragons which still exist(not stationary dragons)
 		for (XemDragon dragon : XemDragonRemoval.keySet()) {
 			LivingEntity a = (LivingEntity) dragon.getBukkitEntity();
 			a.remove();
 		}
 
 		try {
 			if (markers != null) {
 				for (Block block : markers.values()) {
 					if (block != null)
 						block.getChunk().load();
 					block.setType(Material.AIR);
 				}
 			}
 		} catch (Exception ex) {
 		}
 
 		setStuffToNull();
 
 		log.info(String.format("[%s] Disabled v%s", getDescription().getName(), getDescription().getVersion()));
 	}
 
 	@Override
 	public void onEnable() {
 
 		if (shutdown) {
 			return;
 		}
 
 		PluginDescriptionFile description = getDescription();
 
 		// Add our new entity to minecrafts entities
 		try {
 			Method method = EntityTypes.class.getDeclaredMethod("a", new Class[] { Class.class, String.class, int.class });
 			method.setAccessible(true);
 			method.invoke(EntityTypes.class, XemDragon.class, "XemDragon", 63);
 		} catch (Exception e) {
 			log.info("[DragonTravel] Error registering Entity!");
 			e.printStackTrace();
 			pm.disablePlugin(this);
 			return;
 		}
 
 		// Configuration file
 		ConfigurationLoader dtc = new ConfigurationLoader(this);
 		dtc.loadConfig();
 		if (!dtc.checkConfig()) {
 			getPluginLoader().disablePlugin(this);
 			return;
 		}
 
 		onlydragontraveldragons = config.getBoolean("AntiGriefDragonTravelDragons");
 		alldragons = config.getBoolean("AntiGriefallDragons");
 		EconomyEnabled = config.getBoolean("Economy");
 		speed = config.getDouble("DragonSpeed");
 		sendplaymessage = config.getBoolean("MessageOnPlay");
 		onlysigns = config.getBoolean("UseOnlySigns");
 		ignoreAntiMobspawnAreas = config.getBoolean("IgnoreAntiMobspawnAreas");
 
 		// Messages file
 		DTmessages = new MessagesLoader();
 		DTmessages.initMessages();
 
 		// FAQ file
 		FAQLoader faq = new FAQLoader();
 		faq.copy();
 
 		// Database
 		DatabaseLoader.loadDatabase();
 
 		// Commands
 		getCommand("dt").setExecutor(new Commands(this));
 
 		// Registering Events
 		pm = getServer().getPluginManager();
 		entitiesListener = new EntityListener(this);
 		playersListener = new PlayerListener(this);
 		blocksListener = new BlockListener(this);
 		inputListener = new InputListener();
 		buttonListener = new ScreenListener();
 		editorListener = new FlightEditor();
 		flightsignListener = new FlightSignsInteract();
 
 		pm.registerEvents(playersListener, this);
 		pm.registerEvents(blocksListener, this);
 		pm.registerEvents(entitiesListener, this);
 		pm.registerEvents(editorListener, this);
 		pm.registerEvents(flightsignListener, this);
 
 		// Spout
 		if (DragonTravelSpout.getSpout()) {
 			System.out.println("[DragonTravel] Hooked into Spout");
 
 			// Registring
 			pm.registerEvents(inputListener, this);
 			pm.registerEvents(buttonListener, this);
 		}
 
 		// AntiCheat
 		if (DragonTravelAntiCheat.getAntiCheat()) {
 			System.out.println("[DragonTravel] AntiCheat support enabled");
 		}
 
 		// Economy
 		if (!EconomyEnabled) {
 			log.info(String.format("[%s] Enabled v%s ", description.getName(), description.getVersion()));
 			return;
 		}
 
 		// Metrics
 		try {
 			Metrics metrics = new Metrics(this);
 			metrics.start();
 		} catch (IOException e) {
 		}
 
 		instance = this;
 
 		Plugin x = pm.getPlugin("Vault");
 
 		if (x != null & x instanceof Vault) {
 			log.info(String.format("[DragonTravel] Hooked into Vault, using it for economy support"));
 			log.info(String.format("[DragonTravel] Enabled v%s", description.getVersion()));
 			EconomyHandler dte = new EconomyHandler(this.getServer());
 			dte.setupEconomy();
 		} else {
 			log.warning(String.format("[DragonTravel] Vault was not found, disabling plugin!"));
 			log.warning(String.format("[DragonTravel] Turn off \"Economy\" in the config-file or install Vault!"));
 			getPluginLoader().disablePlugin(this);
 		}
 	}
 
 	private void setStuffToNull() {
 		pm = null;
 		instance = null;
 		config = null;
 		sound = null;
 		dbd = null;
 		dbs = null;
 		signs = null;
 		players = null;
 		TravelInformation.clear();
 		XemDragonRemoval.clear();
 		Economy = null;
 		entitiesListener = null;
 		playersListener = null;
 		blocksListener = null;
 		inputListener = null;
 		buttonListener = null;
 	}
 }
