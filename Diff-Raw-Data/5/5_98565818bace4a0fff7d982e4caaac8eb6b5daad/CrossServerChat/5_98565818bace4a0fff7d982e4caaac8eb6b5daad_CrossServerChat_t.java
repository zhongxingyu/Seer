 package me.blha303;
 
 import java.io.File;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.Map;
 import java.util.concurrent.ConcurrentHashMap;
 import java.util.logging.Level;
 
 import org.yaml.snakeyaml.Yaml;
 
 import com.google.common.eventbus.Subscribe;
 
 import net.md_5.bungee.api.ChatColor;
 import net.md_5.bungee.api.ProxyServer;
 import net.md_5.bungee.api.connection.ProxiedPlayer;
 import net.md_5.bungee.api.event.ChatEvent;
 import net.md_5.bungee.api.plugin.Listener;
 import net.md_5.bungee.api.plugin.Plugin;
 
 public class CrossServerChat extends Plugin implements Listener {
 
 	private Yaml yaml;
 	private File file;
 	private Map<String, String> data;
 
 	@Override
 	public void onEnable() {
 		String defaultmsg = "&7<&2%s&8-&2%s&7> &f%s";
 		file = new File("plugins" + File.pathSeparator + this.getDescription().getName() + File.pathSeparator + "config.yml");
 		yaml = new Yaml();
 		loadYAML();
 		if (Integer.parseInt(data.get("version")) < Integer.parseInt(this.getDescription().getVersion())) {
 			String a = get("string");
 			if (get("string") == defaultmsg) data.clear();
 			put("version", this.getDescription().getVersion());
 			put("help", "%s is replaced with (in order) server name, player display name, message. Make sure you have all three.");
 			if (a != defaultmsg) put("string", a);
 			put("server.lobby", "Lobby");
 		}
 		ProxyServer.getInstance().getPluginManager().registerListener(this);
 	}
 
 	@Subscribe
 	public void onChat(ChatEvent e) {
 		String m = e.getMessage();
 		String servername = "";
 		String msg = ChatColor.translateAlternateColorCodes('&', data.get("string"));
 		if (e.getSender() instanceof ProxiedPlayer) {
 			ProxiedPlayer pl = (ProxiedPlayer) e.getSender();
 			if (containsKey("server." + pl.getServer().getInfo().getName().toLowerCase())) {
 				servername = ChatColor.translateAlternateColorCodes('&', 
 					get("server." + pl.getServer().getInfo().getName().toLowerCase()));
 			} else {
 				servername = pl.getServer().getInfo().getName().toLowerCase();
 			}
 			for (ProxiedPlayer p : ProxyServer.getInstance().getPlayers()) {
 				if (p.getServer().getInfo().getName() != pl.getServer().getInfo().getName()) {
 					p.sendMessage(String.format(msg, servername, pl.getDisplayName(), m));
 				}
 			}
 		}
 	}
 
 	/**
 	 * Initialize YAML
 	 */
 	@SuppressWarnings("unchecked")
 	public void loadYAML() {
 		
 		try {
 			file.createNewFile();
 		} catch (IOException e) {
 			ProxyServer.getInstance().getLogger().log(Level.WARNING, "Could not create config file", e);
 		}
 		try {
 			FileReader rd = new FileReader(file);
 			data = yaml.loadAs(rd, Map.class);
 		} catch (IOException ex) {
 			ProxyServer.getInstance().getLogger().log(Level.WARNING, "Could not load CrossServerChat config", ex);
 		}
 
 		if (data == null) {
			data = new ConcurrentHashMap<String, String>();
 		} else {
			data = new ConcurrentHashMap<String, String>(data);
 		}
 	}
 
 	/**
 	 * Save YAML to file
 	 */
 	public void saveYAML() {
 		try {
 			FileWriter wr = new FileWriter(file);
 			yaml.dump(data, wr);
 		} catch (IOException ex) {
 			ProxyServer.getInstance().getLogger().log(Level.WARNING, "Could not load CrossServerChat config", ex);
 		}
 	}
 
 	/**
 	 * Get a key value from YAML
 	 * 
 	 * @param key Key name
 	 * @return Value of key
 	 */
 	public String get(String key) {
 		return data.get(key);
 	}
 
 	/**
 	 * Put a key value into YAML
 	 * 
 	 * @param key Key name
 	 * @param value Key value
 	 */
 	public void put(String key, String value) {
 		data.put(key, value);
 	}
 
 	public boolean containsKey(String key) {
 		return data.containsKey(key);
 	}
 
 	public boolean containsValue(String value) {
 		return data.containsValue(value);
 	}
 }
