 package me.furt.CraftEssence;
 
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import me.furt.CraftEssence.commands.*;
 import me.furt.CraftEssence.listener.ceBlockListener;
 import me.furt.CraftEssence.listener.ceEntityListener;
 import me.furt.CraftEssence.listener.cePlayerListener;
 import me.furt.CraftEssence.sql.ceConnector;
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 import org.bukkit.World;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.event.Event;
 import org.bukkit.plugin.Plugin;
 import org.bukkit.plugin.PluginDescriptionFile;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.java.JavaPlugin;
 import org.bukkit.util.config.Configuration;
 
 import com.nijiko.permissions.PermissionHandler;
 import com.nijikokun.bukkit.Permissions.Permissions;
 
 public class CraftEssence extends JavaPlugin {
 	public static ArrayList<String> muteList = new ArrayList<String>();
 	public static ArrayList<String> godmode = new ArrayList<String>();
 	public static ArrayList<String> prayList = new ArrayList<String>();
 	public final static String premessage = ChatColor.RED + "[CraftEssence] "
 			+ ChatColor.YELLOW;
 	public static final Logger log = Logger.getLogger("Minecraft");
 	public static PermissionHandler Permissions;
 	public cePlayerListener cepl = new cePlayerListener(this);
 	public ceBlockListener cebl = new ceBlockListener(this);
 	public ceEntityListener ceel = new ceEntityListener(this);
 
 	public void onEnable() {
 		registerEvents();
 		setupPermissions();
 		checkFiles();
 		sqlConnection();
 		addCommands();
 		PluginDescriptionFile pdfFile = this.getDescription();
 		log.info(pdfFile.getName() + " v" + pdfFile.getVersion()
 				+ " is enabled!");
 	}
 
 	public void onDisable() {
 		PluginDescriptionFile pdfFile = this.getDescription();
 		log.info(pdfFile.getName() + " Disabled");
 
 	}
 
 	private void addCommands() {
 		getCommand("clearinventory").setExecutor(
 				new ClearInventoryCommand(this));
 		getCommand("broadcast").setExecutor(new BroadcastCommand(this));
 		getCommand("ban").setExecutor(new BanCommand(this));
 		getCommand("compass").setExecutor(new CompassCommand(this));
 		getCommand("give").setExecutor(new GiveCommand(this));
 		getCommand("god").setExecutor(new GodCommand(this));
 		getCommand("heal").setExecutor(new HealCommand(this));
 		// getCommand("help").setExecutor(new HelpCommand(this));
 		getCommand("home").setExecutor(new HomeCommand(this));
 		getCommand("item").setExecutor(new ItemCommand(this));
 		getCommand("jump").setExecutor(new JumpCommand(this));
 		getCommand("kick").setExecutor(new KickCommand(this));
 		getCommand("kill").setExecutor(new KillCommand(this));
 		getCommand("kit").setExecutor(new KitCommand(this));
 		getCommand("mail").setExecutor(new MailCommand(this));
 		getCommand("me").setExecutor(new MeCommand(this));
 		getCommand("motd").setExecutor(new MotdCommand(this));
 		getCommand("msg").setExecutor(new MsgCommand(this));
 		getCommand("mute").setExecutor(new MuteCommand(this));
 		getCommand("pardon").setExecutor(new PardonCommand(this));
 		getCommand("playerlist").setExecutor(new PlayerlistCommand(this));
 		getCommand("sethome").setExecutor(new SetHomeCommand(this));
 		getCommand("setspawn").setExecutor(new SetSpawnCommand(this));
 		getCommand("setwarp").setExecutor(new SetWarpCommand(this));
 		getCommand("spawn").setExecutor(new SpawnCommand(this));
 		getCommand("spawnmob").setExecutor(new SpawnMobCommand(this));
 		getCommand("support").setExecutor(new SupportCommand(this));
 		getCommand("time").setExecutor(new TimeCommand(this));
 		getCommand("top").setExecutor(new TopCommand(this));
 		getCommand("tp").setExecutor(new TpComand(this));
 		getCommand("tphere").setExecutor(new TpHereCommand(this));
 		getCommand("warp").setExecutor(new WarpCommand(this));
 		getCommand("worldlist").setExecutor(new WorldListCommand(this));
 	}
 
 	public boolean isPlayer(CommandSender sender) {
 		if (sender instanceof Player)
 			return true;
 
 		return false;
 	}
 
 	public String message(String[] args) {
 		StringBuilder msg = new StringBuilder();
 		for (String loop : args) {
 			msg.append(loop + " ");
 		}
 		return msg.toString();
 	}
 
 	public Player playerMatch(String name) {
 		if (this.getServer().getOnlinePlayers().length < 1) {
 			return null;
 		}
 
 		Player[] online = this.getServer().getOnlinePlayers();
 		Player lastPlayer = null;
 
 		for (Player player : online) {
 			String playerName = player.getName();
 			String playerDisplayName = player.getDisplayName();
 
 			if (playerName.equalsIgnoreCase(name)) {
 				lastPlayer = player;
 				break;
 			} else if (playerDisplayName.equalsIgnoreCase(name)) {
 				lastPlayer = player;
 				break;
 			}
 
 			if (playerName.toLowerCase().indexOf(name.toLowerCase()) != -1) {
 				if (lastPlayer != null) {
 					return null;
 				}
 
 				lastPlayer = player;
 			} else if (playerDisplayName.toLowerCase().indexOf(
 					name.toLowerCase()) != -1) {
 				if (lastPlayer != null) {
 					return null;
 				}
 
 				lastPlayer = player;
 			}
 		}
 
 		return lastPlayer;
 	}
 
 	private void setupPermissions() {
 		Plugin test = this.getServer().getPluginManager()
 				.getPlugin("Permissions");
 
 		if (Permissions == null) {
 			if (test != null) {
 				Permissions = ((Permissions) test).getHandler();
 
 			} else {
 				log.info("Permission system not detected, disabling CraftEssence");
 				this.getServer().getPluginManager().disablePlugin(this);
 			}
 
 		}
 	}
 
 	private void checkFiles() {
 		if (!this.getDataFolder().exists())
 			this.getDataFolder().mkdirs();
 
 		ceConfig.Load(getConfiguration());
 
 		if (!new File(getDataFolder(), "motd.txt").exists()) {
 			this.createMotdConfig();
 			log.info("motd.properties not found, creating.");
 		}
 		if (!new File(getDataFolder(), "bans.txt").exists()) {
 			this.createBansConfig();
 			log.info("bans.txt not found, creating.");
 		}
 	}
 
 	public void createMobBlacklist(String world) {
 		try {
 			new File("plugins" + File.separator + "CraftEssence"
 					+ File.separator + "MobBlackList", world + ".txt")
 					.createNewFile();
 			FileWriter fstream = new FileWriter(new File("plugins"
 					+ File.separator + "CraftEssence" + File.separator
 					+ "MobBlackList", world + ".txt"));
 			BufferedWriter out = new BufferedWriter(fstream);
 			out.close();
 			fstream.close();
 		} catch (IOException ex) {
 			setEnabled(false);
 		}
 
 	}
 
 	public void registerEvents() {
 		PluginManager pm = getServer().getPluginManager();
 		pm.registerEvent(Event.Type.PLAYER_JOIN, this.cepl,
 				Event.Priority.High, this);
 		pm.registerEvent(Event.Type.PLAYER_LOGIN, this.cepl,
 				Event.Priority.Normal, this);
 		pm.registerEvent(Event.Type.PLAYER_QUIT, this.cepl,
 				Event.Priority.Normal, this);
 		pm.registerEvent(Event.Type.PLAYER_MOVE, this.cepl,
 				Event.Priority.Normal, this);
 		pm.registerEvent(Event.Type.PLAYER_CHAT, this.cepl,
 				Event.Priority.Normal, this);
 		pm.registerEvent(Event.Type.PLAYER_ITEM_HELD, this.cepl,
 				Event.Priority.Normal, this);
 		pm.registerEvent(Event.Type.PLAYER_RESPAWN, this.cepl,
 				Event.Priority.Normal, this);
 		pm.registerEvent(Event.Type.PLAYER_KICK, this.cepl,
 				Event.Priority.Normal, this);
 		pm.registerEvent(Event.Type.BLOCK_DAMAGE, this.cebl,
 				Event.Priority.Normal, this);
 		pm.registerEvent(Event.Type.BLOCK_PLACE, this.cebl,
 				Event.Priority.Normal, this);
 		pm.registerEvent(Event.Type.ENTITY_DAMAGE, this.ceel,
 				Event.Priority.Highest, this);
 		pm.registerEvent(Event.Type.CREATURE_SPAWN, this.ceel,
 				Event.Priority.Highest, this);
 	}
 
 	public void sqlConnection() {
 		Connection conn = ceConnector.createConnection();
 
 		if (conn == null) {
 			log.log(Level.SEVERE,
 					"[CraftEssence] Could not establish SQL connection. Disabling CraftEssence");
 			getServer().getPluginManager().disablePlugin(this);
 			return;
 		} else {
 			try {
 				conn.close();
 			} catch (SQLException e) {
 				e.printStackTrace();
 			}
 		}
 	}
 
 	public void createMotdConfig() {
 		try {
 			new File(this.getDataFolder(), "motd.txt").createNewFile();
 			FileWriter fstream = new FileWriter(new File(getDataFolder(),
 					"motd.txt"));
 			BufferedWriter out = new BufferedWriter(fstream);
 			out.write("4Welcome to our 9Minecraft Server4,f +d4!\n");
 			out.write("4There are +online players online!\n");
 			out.close();
 			fstream.close();
 		} catch (IOException ex) {
 			setEnabled(false);
 		}
 	}
 
 	public void createBansConfig() {
 		try {
 			new File(this.getDataFolder(), "bans.txt").createNewFile();
 			FileWriter fstream = new FileWriter(new File(getDataFolder(),
 					"bans.txt"));
 			BufferedWriter out = new BufferedWriter(fstream);
 			out.close();
 			fstream.close();
 		} catch (IOException ex) {
 			setEnabled(false);
 		}
 
 	}
 
 	public List<String> spawnList(Configuration config) {
 		config.load();
 		ArrayList<String> spawns = new ArrayList<String>();
 		List<?> worldSpawn = config.getList("worldSpawn");
 		String spawnString = null;
 		Iterator<?> i$;
 		if ((worldSpawn != null) && (worldSpawn.size() > 0))
 			for (i$ = worldSpawn.iterator(); i$.hasNext();) {
 				Object spawn = i$.next();
 				spawnString = (String) spawn;
 				spawns.add("  " + spawnString);
 			}
 		return spawns;
 	}
 
 	public String[] getBans() {
 		ArrayList<String> banlist = new ArrayList<String>();
 		try {
 			BufferedReader in = new BufferedReader(new FileReader(
 					getDataFolder() + File.separator + "bans.txt"));
 			String str;
 			while ((str = in.readLine()) != null) {
 				banlist.add(str);
 			}
 			in.close();
 		} catch (IOException e) {
 			log.info("[CraftEssence] Could not get ban list");
 		}
 
 		return banlist.toArray(new String[] {});
 	}
 
 	public String locationToString(Location location) {
 		StringBuilder test = new StringBuilder();
 		test.append(location.getBlockX() + ":");
 		test.append(location.getBlockY() + ":");
 		test.append(location.getBlockZ() + ":");
 		test.append(location.getYaw() + ":");
 		test.append(location.getPitch());
 		return test.toString();
 	}
 
 	public static String string(int i) {
 		return String.valueOf(i);
 	}
 
 	public String argument(String original, String[] arguments, String[] points) {
 		for (int i = 0; i < arguments.length; i++) {
 			if (arguments[i].contains(",")) {
 				for (String arg : arguments[i].split(",")) {
 					original = original.replace(arg, points[i]);
 				}
 			} else {
 				original = original.replace(arguments[i], points[i]);
 			}
 		}
 
 		return original;
 	}
 
 	public String[] getMotd() {
 		ArrayList<String> motd = new ArrayList<String>();
 
 		try {
 			BufferedReader in = new BufferedReader(new FileReader(
 					getDataFolder() + File.separator + "motd.txt"));
 			String str;
 			while ((str = in.readLine()) != null) {
 				motd.add(str);
 			}
 			in.close();
 		} catch (IOException e) {
 		}
 
 		return motd.toArray(new String[] {});
 	}
 
 	public List<String> readMail(Player player) {
 		return readMail(player.getName());
 	}
 
 	public List<String> readMail(String player) {
 		Connection conn = null;
 		PreparedStatement ps = null;
 		ResultSet rs = null;
 		String reciever = player;
 		ArrayList<String> mailarray = new ArrayList<String>();
 
 		try {
 			conn = ceConnector.getConnection();
 			ps = conn
 					.prepareStatement("Select * FROM mail WHERE `reciever` = '"
 							+ reciever + "'");
 			rs = ps.executeQuery();
 			conn.commit();
 			while (rs.next()) {
 				mailarray.add(rs.getString("sender") + ": "
 						+ rs.getString("text"));
 			}
 		} catch (SQLException ex) {
 			CraftEssence.log.log(Level.SEVERE,
 					"[CraftEssence]: Find SQL Exception", ex);
 		} finally {
 			try {
 				if (ps != null) {
 					ps.close();
 				}
 				if (rs != null) {
 					rs.close();
 				}
 				if (conn != null)
 					conn.close();
 			} catch (SQLException ex) {
 				CraftEssence.log.log(Level.SEVERE,
 						"[CraftEssence]: Find SQL Exception (on close)");
 			}
 		}
 		return mailarray;
 	}
 
 	public void sendMail(Player player, String string, String string2) {
 		Connection conn = null;
 		Statement stmt = null;
 		int count = 0;
 		try {
 			conn = ceConnector.getConnection();
 			stmt = conn.createStatement();
 			count += stmt.executeUpdate("INSERT INTO `mail`"
 					+ " (`sender`, `reciever`, `text`)" + " VALUES ('"
 					+ player.getName() + "', '" + string + "', '" + string2
 					+ "')");
 			stmt.close();
 			player.sendMessage(CraftEssence.premessage + "Mail sent");
 		} catch (SQLException ex) {
 			CraftEssence.log.log(Level.SEVERE,
 					"[CraftEssence]: Find SQL Exception", ex);
 			player.sendMessage(CraftEssence.premessage + "Mail error");
 		}
 	}
 
 	public void clearMail(Player player) {
 		Connection conn = null;
 		PreparedStatement ps = null;
 		String query = "DELETE FROM `mail` WHERE `reciever` = '"
 				+ player.getName() + "'";
 		try {
 			conn = ceConnector.getConnection();
 			ps = conn.prepareStatement(query);
 			ps.execute();
 			ps.close();
 			player.sendMessage(CraftEssence.premessage + "Mail deleted");
 		} catch (SQLException ex) {
 			CraftEssence.log.log(Level.SEVERE,
 					"[CraftEssence]: Find SQL Exception", ex);
 			player.sendMessage(CraftEssence.premessage + "Mail error");
 		}
 
 	}
 
 	public boolean kitRank(Player player, String[] args) {
 		// String world = player.getWorld().getName();
 		// String rank = "";
 		Connection conn = null;
 		PreparedStatement ps = null;
 		ResultSet rs = null;
 		try {
 			conn = ceConnector.getConnection();
 			ps = conn.prepareStatement("Select * FROM kit WHERE `name` = '"
 					+ args[0] + "'");
 			rs = ps.executeQuery();
 			conn.commit();
 			while (rs.next()) {
 				// rank = rs.getString("rank");
 				return true;
 			}
 
 		} catch (SQLException ex) {
 			CraftEssence.log.log(Level.SEVERE,
 					"[CraftEssence]: Find SQL Exception", ex);
 			return false;
 		}
 		return false;
 	}
 
 	public int kitID(Player player, String[] args) {
 		Connection conn = null;
 		PreparedStatement ps = null;
 		ResultSet rs = null;
 		int id = 0;
 		try {
 			conn = ceConnector.getConnection();
 			ps = conn.prepareStatement("Select * FROM kit WHERE `name` = '"
 					+ args[0] + "'");
 			rs = ps.executeQuery();
 			conn.commit();
 			while (rs.next()) {
 				id = rs.getInt("id");
 			}
 		} catch (SQLException ex) {
 			CraftEssence.log.log(Level.SEVERE,
 					"[CraftEssence]: Find SQL Exception", ex);
 		}
 		return id;
 	}
 
 	public ArrayList<String> getKit(Player player, Object kitID) {
 		Connection conn = null;
 		PreparedStatement ps = null;
 		ResultSet rs = null;
 		ArrayList<String> itemarray = new ArrayList<String>();
 		try {
 			conn = ceConnector.getConnection();
 			ps = conn
 					.prepareStatement("Select * FROM `kit_items` WHERE `id` = '"
 							+ kitID + "'");
 			rs = ps.executeQuery();
 			conn.commit();
 			while (rs.next()) {
 				itemarray.add(rs.getString("item") + " "
 						+ rs.getString("quanity"));
 			}
 		} catch (SQLException ex) {
 			CraftEssence.log.log(Level.SEVERE,
 					"[CraftEssence]: Find SQL Exception", ex);
 		} finally {
 			try {
 				if (ps != null) {
 					ps.close();
 				}
 				if (rs != null) {
 					rs.close();
 				}
 				if (conn != null)
 					conn.close();
 			} catch (SQLException ex) {
 				CraftEssence.log.log(Level.SEVERE,
 						"[CraftEssence]: Find SQL Exception (on close)");
 			}
 		}
 		return itemarray;
 	}
 
 	public List<String> kitList(Player player) {
 		return kitList(player.getName());
 	}
 
 	public List<String> kitList(String player) {
 		Connection conn = null;
 		PreparedStatement ps = null;
 		ResultSet rs = null;
 		ArrayList<String> namearray = new ArrayList<String>();
 		try {
 			conn = ceConnector.getConnection();
 			ps = conn.prepareStatement("Select * FROM `kit`");
 			rs = ps.executeQuery();
 			conn.commit();
 			while (rs.next()) {
 				namearray.add(rs.getString("name"));
 			}
 		} catch (SQLException ex) {
 			CraftEssence.log.log(Level.SEVERE,
 					"[CraftEssence]: Find SQL Exception", ex);
 		} finally {
 			try {
 				if (ps != null) {
 					ps.close();
 				}
 				if (rs != null) {
 					rs.close();
 				}
 				if (conn != null)
 					conn.close();
 			} catch (SQLException ex) {
 				CraftEssence.log.log(Level.SEVERE,
 						"[CraftEssence]: Find SQL Exception (on close)");
 			}
 		}
 		return namearray;
 	}
 
 	public String getPrefix(Player player) {
 		World world = player.getWorld();
 		if (Permissions != null) {
 			String userPrefix = Permissions.getUserPermissionString(
 					world.getName(), player.getName(), "prefix");
 			if ((userPrefix != null) && (!userPrefix.isEmpty())) {
 				return userPrefix;
 			}
 
 			String group = Permissions.getGroup(world.getName(),
 					player.getName());
 			if (group == null) {
 				CraftEssence.log.log(Level.SEVERE,
 						"[CraftEssence] Group cannot be found for player: "
 								+ player.getName());
 				return null;
 			}
 			String groupPrefix = Permissions.getGroupPrefix(world.getName(),
 					group);
 			return groupPrefix;
 		}
 		CraftEssence.log
 				.log(Level.SEVERE,
 						"[CraftEssence] Permissions resulted in null for prefix function");
 		return null;
 	}
 
 	public String[] getMobs(String world) {
 		ArrayList<String> moblist = new ArrayList<String>();
 		try {
 			BufferedReader in = new BufferedReader(new FileReader(
 					getDataFolder() + File.separator + "MobBlacklist"
 							+ File.separator + world + ".txt"));
 			String str;
 			while ((str = in.readLine()) != null) {
 				moblist.add(str);
 			}
 			in.close();
 		} catch (IOException e) {
			log.info("[CraftEssence] Could not get mob blacklist.");
 		}
 
 		return moblist.toArray(new String[] {});
 	}
 
 	public String[] itemList() {
 		ArrayList<String> itemlist = new ArrayList<String>();
 		try {
 			BufferedReader in = new BufferedReader(new FileReader(
 					getDataFolder() + File.separator + "item.txt"));
 			String str;
 			while ((str = in.readLine()) != null) {
 				itemlist.add(str);
 			}
 			in.close();
 		} catch (IOException e) {
			log.info("[CraftEssence] Could not get item list.");
 		}
 
 		return itemlist.toArray(new String[] {});
 	}
 	
 	
 }
