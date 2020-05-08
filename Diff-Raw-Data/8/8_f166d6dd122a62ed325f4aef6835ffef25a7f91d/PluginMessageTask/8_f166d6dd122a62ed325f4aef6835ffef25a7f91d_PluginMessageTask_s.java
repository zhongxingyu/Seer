 package com.minecraftdimensions.bungeesuiteteleports.tasks;
 
 import java.io.ByteArrayOutputStream;
 import java.io.File;
 import java.io.InputStream;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.bukkit.Bukkit;
 import org.bukkit.configuration.file.YamlConfiguration;
 import org.bukkit.scheduler.BukkitRunnable;
 
 import com.minecraftdimensions.bungeesuiteteleports.BungeeSuiteTeleports;
 import com.minecraftdimensions.bungeesuiteteleports.socket.Client;
 
 public class PluginMessageTask extends BukkitRunnable {
 
 	private final ByteArrayOutputStream bytes;
 	private final boolean empty;
 
 	public PluginMessageTask(ByteArrayOutputStream bytes) {
 		this.bytes = bytes;
 		empty = false;
 	}
 
 	public PluginMessageTask(ByteArrayOutputStream b, boolean empty) {
 		this.bytes = b;
 		this.empty = empty;
 	}
 
 	@SuppressWarnings("unchecked")
 	public void run() {
 		if (!empty) {
			Bukkit.getOnlinePlayers()[0].sendPluginMessage(
 					BungeeSuiteTeleports.instance,
 					BungeeSuiteTeleports.OUTGOING_PLUGIN_CHANNEL,
 					bytes.toByteArray());
 		} else {
 			File customConfigFile = new File("spigot.yml");
 			YamlConfiguration customConfig = YamlConfiguration
 					.loadConfiguration(customConfigFile);
 			InputStream defConfigStream = BungeeSuiteTeleports.instance
 					.getResource("spigot.yml");
 			if (defConfigStream != null) {
 				YamlConfiguration defConfig = YamlConfiguration
 						.loadConfiguration(defConfigStream);
 				customConfig.setDefaults(defConfig);
 			}
 			List<String> bungee;
 			if (customConfig != null) {
 				bungee = (List<String>) customConfig
 						.getList("settings.bungeecord-addresses");
 			} else {
 				bungee = new ArrayList<String>();
 				bungee.add("127.0.0.1");
 			}
 			for (String l : bungee) {
 				Client.send(l, 14455, bytes);
 			}
 		}
 	}
 
 }
