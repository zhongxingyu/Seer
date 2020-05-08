 package com.nekocraft.skin;
 
 import com.nekocraft.skin.listener.NekoSkinPlayerListener;
 import org.bukkit.configuration.Configuration;
 import org.bukkit.configuration.InvalidConfigurationException;
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.java.JavaPlugin;
 
 import java.io.File;
 import java.io.IOException;
 
 public class NekoSkin extends JavaPlugin {
 	public static NekoSkin plugin;
 	private String skinurl;
 	private String cloakurl;
 	protected FileConfiguration config;
 
 
 	@Override
 	public void onEnable() {
 		plugin = this;
 		initConfig();
 		PluginManager pm = getServer().getPluginManager();
 		pm.registerEvents(new NekoSkinPlayerListener(), this);
 	}
 
 	private void initConfig() {
 		config = getConfig();
 		try {
 			config.load(getDataFolder() + File.separator + "config.yml");
 		} catch (IOException e) {
 			e.printStackTrace();
 		} catch (InvalidConfigurationException e) {
 			e.printStackTrace();
 		}
 		skinurl = config.getString("setting.skinurl");
 		cloakurl = config.getString("setting.cloakurl");
 		try {
 			config.save(getDataFolder() + File.separator + "config.yml");
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 
 	@Override
 	public void onDisable() {
 		plugin = null;
 	}
 
 	public String getSkinurl(String player) {
		return String.format(skinurl, player.toLowerCase());
 	}
 
 	public String getCloakurl(String player) {
		return String.format(cloakurl, player.toLowerCase());
 	}
 }
