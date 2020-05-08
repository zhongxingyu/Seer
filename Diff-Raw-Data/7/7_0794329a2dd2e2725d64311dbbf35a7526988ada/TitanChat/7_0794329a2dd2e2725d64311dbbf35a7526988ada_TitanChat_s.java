 package com.titankingdoms.nodinchan.titanchat;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.InputStreamReader;
 import java.lang.reflect.Method;
 import java.net.HttpURLConnection;
 import java.net.URL;
 import java.net.URLClassLoader;
 import java.nio.channels.Channels;
 import java.nio.channels.ReadableByteChannel;
 import java.util.Enumeration;
 import java.util.List;
 import java.util.jar.JarEntry;
 import java.util.jar.JarFile;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import org.bukkit.ChatColor;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.java.JavaPlugin;
 
 import com.nodinchan.ncloader.metrics.Metrics;
import com.nodinchan.ncloader.metrics.Metrics.Graph;
import com.nodinchan.ncloader.metrics.Metrics.Plotter;
 import com.titankingdoms.nodinchan.titanchat.addon.AddonManager;
 import com.titankingdoms.nodinchan.titanchat.channel.ChannelManager;
 import com.titankingdoms.nodinchan.titanchat.command.CommandManager;
 import com.titankingdoms.nodinchan.titanchat.util.Debugger;
 import com.titankingdoms.nodinchan.titanchat.util.FormatHandler;
 import com.titankingdoms.nodinchan.titanchat.util.PermsBridge;
 
 /*     Copyright (C) 2012  Nodin Chan <nodinchan@live.com>
  * 
  *     This program is free software: you can redistribute it and/or modify
  *     it under the terms of the GNU General Public License as published by
  *     the Free Software Foundation, either version 3 of the License, or
  *     (at your option) any later version.
  * 
  *     This program is distributed in the hope that it will be useful,
  *     but WITHOUT ANY WARRANTY; without even the implied warranty of
  *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *     GNU General Public License for more details.
  * 
  *     You should have received a copy of the GNU General Public License
  *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 /**
  * TitanChat - Main Class
  * 
  * @author NodinChan
  *
  */
 public final class TitanChat extends JavaPlugin {
 	
 	private static TitanChat instance;
 	
 	private String NAME;
 	
 	private static final Logger log = Logger.getLogger("TitanLog");
 	private static final Debugger db = new Debugger(1);
 	
 	private AddonManager addonManager;
 	private ChannelManager chManager;
 	private CommandManager cmdManager;
 	private FormatHandler format;
 	private PermsBridge permBridge;
 	
 	private boolean silenced = false;
 	
 	/**
 	 * Creates a new list with items seperated with commas
 	 * 
 	 * @param list The string list to create a list from
 	 * 
 	 * @return The created list of items
 	 */
 	public String createList(List<String> list) {
 		db.i("Creating string out of stringlist: " + list.toString());
 		
 		StringBuilder str = new StringBuilder();
 		
 		for (String item : list) {
 			if (str.length() > 0)
 				str.append(", ");
 			
 			str.append(item);
 		}
 		
 		return str.toString();
 	}
 	
 	/**
 	 * Check if Channels are enabled
 	 * 
 	 * @return True if Channels are enabled
 	 */
 	public boolean enableChannels() {
 		return getConfig().getBoolean("channels.enable-channels");
 	}
 	
 	/**
 	 * Check if join messages are enabled
 	 * 
 	 * @return True if join messages are enabled
 	 */
 	public boolean enableJoinMessage() {
 		return getConfig().getBoolean("channels.messages.join");
 	}
 	
 	/**
 	 * Check if leave messages are enabled
 	 * 
 	 * @return True if leave messages are enabled
 	 */
 	public boolean enableLeaveMessage() {
 		return getConfig().getBoolean("channels.messages.leave");
 	}
 	
 	/**
 	 * Gets the AddonManager
 	 * 
 	 * @return The AddonManager
 	 */
 	public AddonManager getAddonManager() {
 		return addonManager;
 	}
 	
 	/**
 	 * Gets the Channel directory
 	 * 
 	 * @return The Channel directory
 	 */
 	public File getChannelDir() {
 		return new File(getDataFolder(), "channels");
 	}
 	
 	/**
 	 * Gets the ChannelManager
 	 * 
 	 * @return The ChannelManager
 	 */
 	public ChannelManager getChannelManager() {
 		return chManager;
 	}
 	
 	/**
 	 * Gets the CommandManager
 	 * 
 	 * @return The CommandManager
 	 */
 	public CommandManager getCommandManager() {
 		return cmdManager;
 	}
 	
 	/**
 	 * Gets the FormatHandler
 	 * 
 	 * @return The FormatHandler
 	 */
 	public FormatHandler getFormatHandler() {
 		return format;
 	}
 	
 	/**
 	 * Gets an instance of this
 	 * 
 	 * @return TitanChat instance
 	 */
 	public static TitanChat getInstance() {
 		return instance;
 	}
 	
 	@Override
 	public Logger getLogger() {
 		return log;
 	}
 	
 	/**
 	 * Gets the PermsBridge
 	 * 
 	 * @return The built-in PermsBridge
 	 */
 	public PermsBridge getPermsBridge() {
 		return permBridge;
 	}
 	
 	/**
 	 * Gets Player by name
 	 * 
 	 * @param name The name of the Player
 	 * 
 	 * @return The Player with the name
 	 */
 	public Player getPlayer(String name) {
 		return getServer().getPlayer(name);
 	}
 	
 	/**
 	 * Initialises the NC-LoaderLib
 	 * 
 	 * @return True if the Lib is initialised
 	 */
 	public boolean initLoaderLib() {
 		try {
 			File destination = new File(getDataFolder().getParentFile().getParentFile(), "lib");
 			destination.mkdirs();
 			
 			File lib = new File(destination, "NC-LoaderLib.jar");
 			
 			boolean download = false;
 			
 			if (!lib.exists()) {
 				System.out.println("Missing NC-Loader lib");
 				download = true;
 				
 			} else {
 				JarFile jarFile = new JarFile(lib);
 				Enumeration<JarEntry> entries = jarFile.entries();
 				
 				double version = 0;
 				
 				while (entries.hasMoreElements()) {
 					JarEntry element = entries.nextElement();
 					
 					if (element.getName().equals("version.yml")) {
 						BufferedReader reader = new BufferedReader(new InputStreamReader(jarFile.getInputStream(element)));
 						version = Double.parseDouble(reader.readLine().substring(9).trim());
 					}
 				}
 				
 				if (version == 0) {
 					System.out.println("NC-Loader lib outdated");
 					download = true;
 					
 				} else {
 					HttpURLConnection urlConnection = (HttpURLConnection) new URL("http://www.nodinchan.com/NC-LoaderLib/version.yml").openConnection();
 					BufferedReader reader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
 					
 					if (Double.parseDouble(reader.readLine().replace("NC-LoaderLib Version ", "").trim()) > version)
 						download = true;
 				}
 			}
 			
 			if (download) {
 				System.out.println("Downloading NC-Loader lib...");
 				URL url = new URL("http://www.nodinchan.com/NC-LoaderLib/NC-LoaderLib.jar");
 				ReadableByteChannel rbc = Channels.newChannel(url.openStream());
 				FileOutputStream output = new FileOutputStream(lib);
 				output.getChannel().transferFrom(rbc, 0, 1 << 24);
 				System.out.println("Downloaded NC-Loader lib");
 			}
 			
 			URLClassLoader sysLoader = (URLClassLoader) ClassLoader.getSystemClassLoader();
 			
 			for (URL url : sysLoader.getURLs()) {
 				if (url.sameFile(lib.toURI().toURL()))
 					return true;
 			}
 			
 			try {
 				Method method = URLClassLoader.class.getDeclaredMethod("addURL", new Class[] { URL.class });
 				method.setAccessible(true);
 				method.invoke(sysLoader, new Object[] { lib.toURI().toURL() });
 				
 			} catch (Exception e) { return false; }
 			
 			return true;
 			
 		} catch (Exception e) { e.printStackTrace(); }
 		
 		return false;
 	}
 	
 	/**
 	 * Initialises Metrics
 	 * 
 	 * @return True is Metrics is initialised
 	 */
 	public boolean initMetrics() {
 		try {
 			Metrics metrics = new Metrics(this);
 			
 			Graph graph = metrics.createGraph("Permissions");
 			graph.addPlotter(new Plotter(permBridge.using().getName()) {
 				
 				@Override
 				public int getValue() {
 					return 1;
 				}
 				
 			});
 			
 			if (!metrics.start())
 				throw new Exception();
 			
 			return true;
 			
 		} catch (Exception e) { return false; }
 	}
 
 	/**
 	 * Check if the Server is silenced
 	 * 
 	 * @return True if the Server is silenced
 	 */
 	public boolean isSilenced() {
 		return silenced;
 	}
 	
 	/**
 	 * Check if the Player is staff
 	 * 
 	 * @param player The Player to check
 	 * 
 	 * @return True if the Player has TitanChat.admin
 	 */
 	public boolean isStaff(Player player) {
 		return permBridge.has(player, "TitanChat.admin");
 	}
 	
 	/**
 	 * Sends the message to the log
 	 * 
 	 * @param level Level of the announcement
 	 * 
 	 * @param msg The message to send
 	 */
 	public void log(Level level, String msg) {
 		log.log(level, "[" + NAME + "] " + msg);
 	}
 	
 	/**
 	 * Called when a Player uses a command
 	 * 
 	 * @param sender The sender who sent the command
 	 * 
 	 * @param cmd The Command used
 	 * 
 	 * @param label The exact word the Player used
 	 * 
 	 * @param args The list of words that follows
 	 * 
 	 * @return True if the Command is executed
 	 */
 	@Override
 	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
 		db.i("onCommand: " + cmd.getName());
 		
 		if (cmd.getName().equals("titanchat")) {
 			if (args.length < 1) {
 				db.i("onCommand: No arguments!");
 				
 				sender.sendMessage(ChatColor.AQUA + "You are running " + this);
 				
 				if (sender instanceof Player)
 					sendInfo((Player) sender, "\"/titanchat commands [page]\" for command list");
 				else
 					log(Level.INFO, "\"/titanchat commands [page]\" for command list");
 				
 				return true;
 			}
 			
 			if (!(sender instanceof Player)) {
 				if (args[0].equalsIgnoreCase("reload")) {
 					try { cmdManager.getCommandExecutor("Reload").execute(null, new String[0]);
 					} catch (Exception e) { e.printStackTrace(); }
 					return true;
 				}
 				
 				log(Level.INFO, "Please use commands in-game"); return true;
 			}
 
 			db.i("CommandManager executing command:");
 			cmdManager.execute((Player) sender, args[0], parseCommand(args));
 			return true;
 		}
 		
 		if (cmd.getName().equalsIgnoreCase("broadcast")) {
 			if (!(sender instanceof Player)) {
 				if (!getConfig().getBoolean("broadcast.server.enable")) {
 					log(Level.WARNING, "Command disabled");
 					return true;
 				}
 				
 				String message = getConfig().getString("broadcast.server.format");
 				
 				StringBuilder str = new StringBuilder();
 				
 				for (String word : args) {
 					if (str.length() > 0)
 						str.append(" ");
 					
 					str.append(word);
 				}
 				
 				message = message.replace("%message", str.toString());
 				
 				getServer().broadcastMessage(getFormatHandler().colourise(message));
 				getLogger().info("<Server> " + getFormatHandler().decolourise(str.toString()));
 				return true;
 			}
 			
 			if (!getConfig().getBoolean("broadcast.player.enable")) {
 				sendWarning((Player) sender, "Command disabled");
 				return true;
 			}
 			
 			if (permBridge.has((Player) sender, "TitanChat.broadcast"))
 				try { cmdManager.execute((Player) sender, "broadcast", args); } catch (Exception e) {}
 			else
 				sendWarning((Player) sender, "You do not have permission");
 			
 			return true;
 		}
 		
 		if (cmd.getName().equalsIgnoreCase("me")) {
 			if (!(sender instanceof Player)) {
 				if (!getConfig().getBoolean("emote.server.enable")) {
 					log(Level.WARNING, "Command disabled");
 					return true;
 				}
 				
 				String message = getConfig().getString("emote.server.format");
 				
 				StringBuilder str = new StringBuilder();
 				
 				for (String word : args) {
 					if (str.length() > 0)
 						str.append(" ");
 					
 					str.append(word);
 				}
 				
 				message = message.replace("%action", str.toString());
 				getServer().broadcastMessage(getFormatHandler().colourise(message));
 				getLogger().info("* Server " + getFormatHandler().decolourise(str.toString()));
 				return true;
 			}
 			
 			if (!getConfig().getBoolean("emote.player.enable")) {
 				sendWarning((Player) sender, "Command disabled");
 				return true;
 			}
 			
 			if (permBridge.has((Player) sender, "TitanChat.emote.server"))
 				try { cmdManager.execute((Player) sender, "me", args); } catch (Exception e) {}
 			else
 				sendWarning((Player) sender, "You do not have permission");
 			
 			return true;
 		}
 		
 		if (cmd.getName().equalsIgnoreCase("whisper")) {
 			if (!(sender instanceof Player)) {
 				if (!getConfig().getBoolean("whisper.server.enable")) {
 					log(Level.WARNING, "Command disabled");
 					return true;
 				}
 				
 				if (getPlayer(args[0]) == null) {
 					log(Level.WARNING, "Player not online");
 					return true;
 				}
 				
 				String message = getConfig().getString("whisper.server.format");
 				
 				StringBuilder str = new StringBuilder();
 				
 				for (String word : args) {
 					if (str.length() > 0)
 						str.append(" ");
 					
 					str.append(word);
 				}
 				
 				if (args[0].equalsIgnoreCase("console")) {
 					log(Level.INFO, "You whispered to yourself: " + str.toString());
 					log(Level.INFO, message.replace("%message", str.toString()));
 					return true;
 				}
 				
 				if (getPlayer(args[0]) != null) {
 					log(Level.INFO, "You whispered to " + getPlayer(args[0]).getName() + ": " + str.toString());
 					getPlayer(args[0]).sendMessage(message.replace("%message", getFormatHandler().colourise(str.toString())));
 					getLogger().info("[Server -> " + getPlayer(args[0]).getName() + "] " + str.toString());
 					
 				} else { log(Level.WARNING, "Player not online"); }
 			}
 			
 			if (!getConfig().getBoolean("whisper.player.enable")) {
 				sendWarning((Player) sender, "Command disabled");
 				return true;
 			}
 			
 			if (permBridge.has((Player) sender, "TitanChat.whisper"))
 				try { cmdManager.execute((Player) sender, "whisper", args); } catch (Exception e) {}
 			else
 				sendWarning((Player) sender, "You do not have permission");
 			
 			return true;
 		}
 		
 		return false;
 	}
 	
 	/**
 	 * Called when the Plugin disables
 	 */
 	@Override
 	public void onDisable() {
 		log(Level.INFO, "is now disabling...");
 		
 		getServer().getScheduler().cancelTasks(this);
 		
 		log(Level.INFO, "Unloading managers...");
 
 		addonManager.unload();
 		chManager.unload();
 		cmdManager.unload();
 		
 		log(Level.INFO, "is now disabled");
 	}
 	
 	/**
 	 * Called when the Plugin enables
 	 */
 	@Override
 	public void onEnable() {
 		instance = this;
 		NAME = "TitanChat " + instance.toString().split(" ")[1];
 		
 		log(Level.INFO, "is now enabling...");
 		
 		if (!initLoaderLib())
 			log(Level.WARNING, "Failed to initialise Loader lib");
 		
 		if (!initMetrics())
 			log(Level.WARNING, "Failed to hook into Metrics");
 		
 		File config = new File(getDataFolder(), "config.yml");
 		
 		if (!config.exists()) {
 			log(Level.INFO, "Loading default config");
 			saveResource("config.yml", false);
 		}
 		
 		if (getChannelDir().mkdir()) {
 			log(Level.INFO, "Creating channel directory...");
 			saveResource("channels/Default.yml", false);
 			saveResource("channels/Password.yml", false);
 			saveResource("channels/Private.yml", false);
 			saveResource("channels/Public.yml", false);
 			saveResource("channels/README.yml", false);
 			saveResource("channels/Staff.yml", false);
 		}
 		
 		addonManager = new AddonManager(this);
 		chManager = new ChannelManager(this);
 		cmdManager = new CommandManager(this);
 		format = new FormatHandler(this);
 		permBridge = new PermsBridge(this);
 		
 		PluginManager pm = getServer().getPluginManager();
 		
 		Debugger.load(this);
 		
 		pm.registerEvents(permBridge, this);
 		pm.registerEvents(new TitanChatListener(this), this);
 		
 		try { chManager.load(); } catch (Exception e) {}
 		
 		addonManager.load();
 		cmdManager.load();
 		
 		if (chManager.getDefaultChannel() == null && enableChannels()) {
 			log(Level.SEVERE, "A default channel is not defined");
 			pm.disablePlugin(this);
 			return;
 		}
 		
 		log(Level.INFO, "is now enabled");
 	}
 	
 	/**
 	 * Parses the command arguments
 	 * 
 	 * @param args Arguments to parse
 	 * 
 	 * @return The parsed arguments
 	 */
 	public String[] parseCommand(String[] args) {
 		db.i("Parsing command");
 		
 		StringBuilder str = new StringBuilder();
 		
 		for (String arg : args) {
 			if (str.length() > 0)
 				str.append(" ");
 			
 			if (arg.equals(args[0]))
 				continue;
 			
 			str.append(arg);
 		}
 		
 		db.i("Command arguments: " + str.toString());
 		
 		return (str.toString().equals("")) ? new String[] {} : str.toString().split(" ");
 	}
 	
 	/**
 	 * Sends an info to the Player
 	 * 
 	 * @param player The Player to send to
 	 * 
 	 * @param info The message
 	 */
 	public void sendInfo(Player player, String info) {
 		db.i("@" + player.getName() + ": " + info);
 		
 		player.sendMessage("[TitanChat] " + ChatColor.GOLD + info);
 	}
 	
 	/**
 	 * Sends an info to all the Players within the list
 	 * 
 	 * @param players String list of Players to send to
 	 * 
 	 * @param info The message
 	 */
 	public void sendInfo(List<String> players, String info) {
 		for (String player : players) {
 			if (getPlayer(player) != null)
 				sendInfo(getPlayer(player), info);
 		}
 	}
 	
 	/**
 	 * Sends a warning to the Player
 	 * 
 	 * @param player The Player to send to
 	 * 
 	 * @param warning The message
 	 */
 	public void sendWarning(Player player, String warning) {
 		db.i("Warning @" + player.getName() + ": " + warning);
 		
 		player.sendMessage("[TitanChat] " + ChatColor.RED + warning);
 	}
 	
 	/**
 	 * Sends a warning to all the Players within the list
 	 * 
 	 * @param players String list of Players to send to
 	 * 
 	 * @param warning The message
 	 */
 	public void sendWarning(List<String> players, String warning) {
 		for (String player : players) {
 			if (getPlayer(player) != null)
 				sendWarning(getPlayer(player), warning);
 		}
 	}
 	
 	/**
 	 * Sets whether the Server is silenced
 	 * 
 	 * @param silenced True if setting the Server to silenced
 	 */
 	public void setSilenced(boolean silenced) {
 		db.i("Setting silenced to " + silenced);
 		this.silenced = silenced;
 	}
 	
 	/**
 	 * Check if default formatting should be used
 	 * 
 	 * @return True if default formatting should be used
 	 */
 	public boolean useDefaultFormat() {
 		return getConfig().getBoolean("formatting.use-built-in");
 	}
 }
