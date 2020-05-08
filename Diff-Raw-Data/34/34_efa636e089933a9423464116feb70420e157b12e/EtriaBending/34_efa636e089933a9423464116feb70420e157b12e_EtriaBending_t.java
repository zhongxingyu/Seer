 package com.etriacraft.etriabending;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.util.logging.Logger;
 
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.configuration.file.YamlConfiguration;
 import org.bukkit.plugin.java.JavaPlugin;
 
 import com.etriacraft.etriabending.listeners.*;
 import com.etriacraft.etriabending.suites.*;
 
 public class EtriaBending extends JavaPlugin {
 	
 	// Variables
 	public static Logger log;
 	public static EtriaBending instance;
 	DBConnection dbc;
 	
 	File configFile;
 	FileConfiguration config;
 	
 	// Commands
 	BackpackSuite bs;
 	InventorySuite is;
 	WorldSuite ws;
 	PlayerSuite ps;
 	TeleportSuite ts;
 	HomeSuite hs;
 	MessagingSuite ms;
 	
 	// Events
 	
 	public void onEnable() {
 		instance = this;
 		this.log = this.getLogger();
 		
 		configFile = new File(getDataFolder(), "config.yml");
 		
 		PlayerSuite.Helps();
 		try {
 			firstRun();
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		config = new YamlConfiguration();
 		loadYamls();
 		
 		DBConnection.sqlite_db = config.getString("sqlitedb", "etriabending.db");
 		// Events
 		this.getServer().getPluginManager().registerEvents(new BackpackListeners(), this);
 		this.getServer().getPluginManager().registerEvents(new ElevatorListener(), this);
 		this.getServer().getPluginManager().registerEvents(new EntityListener(), this);
 		this.getServer().getPluginManager().registerEvents(new InventoryListener(), this);
 		this.getServer().getPluginManager().registerEvents(new PlayerListener(), this);
 		
 		// Register Commands
 		bs = new BackpackSuite(this);
 		ws = new WorldSuite(this);
 		is = new InventorySuite(this);
 		ps = new PlayerSuite(this);
 		ts = new TeleportSuite(this);
 		hs = new HomeSuite(this);
 		ms = new MessagingSuite(this);
 		//
 		PlayerListener.joinmessage = getConfig().getString("messaging.joinmessage"); 
 		PlayerListener.quitmessage = getConfig().getString("messaging.leavemessage");
 		PlayerListener.welcomemessage = getConfig().getString("messaging.firstjoinmessage");
		HomeSuite.homescap = getConfig().getInt("PlayerHomeLimit");
 		//
 		DBConnection.initialize();
 		HomeSuite.Homes();
 		BackpackSuite.BackpackInitialize();
 		BackpackSuite.loadPackDb();
 	}
 	
 	public void onDisable() {
 		DBConnection.sql.close();
 		BackpackSuite.shutdown();
 	}
 	
 	// Misc. Methods
 	public void firstRun() throws Exception {
 		if (!configFile.exists()) {
 			configFile.getParentFile().mkdirs();
 			copy(getResource("config.yml"), configFile);
 			log.info("Config not found, Generating.");
 		}
 	}
 	
 	private void loadYamls() {
 		try {
 			config.load(configFile);
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 	
 	private void copy(InputStream in, File file) {
 		try {
 			OutputStream out = new FileOutputStream(file);
 			byte[] buf = new byte[1024];
 			int len;
 			while ((len = in.read(buf))>0) {
 				out.write(buf,0,len);
 			}
 			out.close();
 			in.close();
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 	
 	public void saveYamls() {
 		try {
 			config.save(configFile);
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	public static EtriaBending getInstance() {
 		return instance;
 	}
 	
 
 }
