 package me.furt.CraftEssence;
 
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Timer;
 import java.util.logging.Level;
 import javax.persistence.PersistenceException;
 
 import me.furt.CraftEssence.commands.*;
 import me.furt.CraftEssence.listener.ceEntityListener;
 import me.furt.CraftEssence.listener.cePlayerListener;
 import me.furt.CraftEssence.sql.HomeTable;
 import me.furt.CraftEssence.sql.KitItemsTable;
 import me.furt.CraftEssence.sql.KitTable;
 import me.furt.CraftEssence.sql.MailTable;
 import me.furt.CraftEssence.sql.UserTable;
 import me.furt.CraftEssence.sql.WarpTable;
 import me.furt.CraftEssence.timers.AFKKickTask;
 import me.furt.CraftEssence.timers.AFKMarkerTask;
 import me.furt.CraftEssence.timers.VoteTask;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.plugin.PluginDescriptionFile;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class CraftEssence extends JavaPlugin {
 	public static ArrayList<String> prayList = new ArrayList<String>();
 	public static ArrayList<String> reply = new ArrayList<String>();
 	public static ArrayList<String> homeInvite = new ArrayList<String>();
 	public HashMap<String, Long> users = new HashMap<String, Long>();
 	public String vote = null;
 	public HashMap<String, String> vuser = new HashMap<String, String>();
 	private Timer etimer = new Timer();
 	private AFKKickTask afkKick;
 	private AFKMarkerTask afkMarker;
 	private VoteTask voteTask;
 	public final static String premessage = ChatColor.RED + "[CraftEssence] "
 			+ ChatColor.YELLOW;
 	public cePlayerListener cepl = new cePlayerListener(this);
 	public ceEntityListener ceel = new ceEntityListener(this);
 	public boolean permEnabled;
 
 	public void onEnable() {
 		registerEvents();
 		checkFiles();
 		setupDatabase();
 		addCommands();
 		checkPlayers();
 		PluginDescriptionFile pdfFile = this.getDescription();
		this.getLogger().log(Level.INFO, "v" + pdfFile.getVersion() + " Enabled");
 	}
 
 	public void onDisable() {
 		etimer.cancel();
 		etimer = null;
 		afkMarker = null;
 		afkKick = null;
 		voteTask = null;
 		PluginDescriptionFile pdfFile = this.getDescription();
		this.getLogger().log(Level.INFO, "v" + pdfFile.getVersion() + " Disabled");
 
 	}
 
 	public void startVoteTimer() {
 		voteTask = new VoteTask(this);
 		etimer.schedule(voteTask, getConfig().getInt("VOTE_TIMER") * 1000);
 	}
 
 	private void checkPlayers() {
 		try {
 			afkMarker = new AFKMarkerTask(this);
 			etimer.schedule(afkMarker, 1000,
 					getConfig().getInt("AFK_TIMER") * 1000);
 			if (getConfig().getBoolean("AUTO_KICK")) {
 				afkKick = new AFKKickTask(this);
 				etimer.schedule(afkKick, 2000,
 						getConfig().getInt("KICK_TIMER") * 1000);
 			}
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 
 	private void addCommands() {
 		getCommand("afk").setExecutor(new AFKCommand(this));
 		getCommand("clearinventory").setExecutor(
 				new ClearInventoryCommand(this));
 		getCommand("broadcast").setExecutor(new BroadcastCommand(this));
 		getCommand("ban").setExecutor(new BanCommand(this));
 		getCommand("ceuser").setExecutor(new UserCommand(this));
 		getCommand("compass").setExecutor(new CompassCommand(this));
 		getCommand("gamemode").setExecutor(new GameModeCommand(this));
 		getCommand("give").setExecutor(new GiveCommand(this));
 		getCommand("heal").setExecutor(new HealCommand(this));
 		getCommand("home").setExecutor(new HomeCommand(this));
 		getCommand("hunger").setExecutor(new HungerCommand(this));
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
 		getCommand("reply").setExecutor(new ReplyCommand(this));
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
 		getCommand("vote").setExecutor(new VoteCommand(this));
 		getCommand("warp").setExecutor(new WarpCommand(this));
 		getCommand("weather").setExecutor(new WeatherCommand(this));
 		getCommand("who").setExecutor(new WhoCommand(this));
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
 
 	public boolean hasPerm(CommandSender sender, String label,
 			boolean consoleUse) {
 		boolean perm = sender.hasPermission("craftessence." + label);
 
 		if (this.console(sender)) {
 			if (consoleUse)
 				return true;
 
 			this.logger(Level.INFO, "This command cannot be used in console.");
 			return false;
 		} else {
 			if (sender.isOp())
 				return true;
 
 			return perm;
 		}
 	}
 
 	public boolean console(CommandSender sender) {
 		if (sender instanceof Player) {
 			return false;
 		}
 		// Needs more checks
 		return true;
 	}
 
 	private void checkFiles() {
 		if (!this.getDataFolder().exists())
 			this.getDataFolder().mkdirs();
 
 		this.getConfig().addDefault("DEATH_MSG", true);
 		this.getConfig().addDefault("KICK_OP", false);
 		this.getConfig().addDefault("KICK_TIMER", 300);
 		this.getConfig().addDefault("AFK_TIMER", 300);
 		this.getConfig().addDefault("UNIQUE_MSG",
 				"A new player has joined the server!");
 		this.getConfig().addDefault("AUTO_KICK", true);
 		this.getConfig().addDefault("VOTE_TIMER", 30);
 		this.getConfig().addDefault("ENABLE_VOTE", true);
 		this.getConfig().options().copyDefaults(true);
 		this.saveConfig();
 
 		if (!new File(getDataFolder() + File.separator + "MobBlacklist")
 				.isDirectory())
 			new File(getDataFolder() + File.separator + "MobBlacklist").mkdir();
 
 		if (!new File(getDataFolder(), "motd.txt").exists()) {
 			this.createMotdConfig();
 			this.logger(Level.INFO, "motd.txt not found, creating.");
 		}
 		if (!new File(getDataFolder(), "bans.txt").exists()) {
 			this.createBansConfig();
 			this.logger(Level.INFO, "bans.txt not found, creating.");
 		}
 	}
 
 	public void createMobBlacklist(String world) {
 		try {
 			new File(getDataFolder() + File.separator + "MobBlacklist", world
 					+ ".txt").createNewFile();
 			FileWriter fstream = new FileWriter(new File(getDataFolder()
 					+ File.separator + "MobBlacklist", world + ".txt"));
 			BufferedWriter out = new BufferedWriter(fstream);
 			out.close();
 			fstream.close();
 		} catch (IOException ex) {
 			setEnabled(false);
 		}
 
 	}
 
 	public void registerEvents() {
 		PluginManager pm = getServer().getPluginManager();
 		pm.registerEvents(cepl, this);
 		pm.registerEvents(ceel, this);
 	}
 
 	private void setupDatabase() {
 		try {
 			File ebeans = new File("ebean.properties");
 			if (!ebeans.exists()) {
 				try {
 					ebeans.createNewFile();
 				} catch (IOException e) {
 					e.printStackTrace();
 				}
 			}
 
 			getDatabase().find(HomeTable.class).findRowCount();
 			getDatabase().find(WarpTable.class).findRowCount();
 			getDatabase().find(MailTable.class).findRowCount();
 			getDatabase().find(KitTable.class).findRowCount();
 			getDatabase().find(KitItemsTable.class).findRowCount();
 			getDatabase().find(UserTable.class).findRowCount();
 			// getDatabase().find(JailTable.class).findRowCount();
 		} catch (PersistenceException ex) {
 			this.logger(Level.INFO, "Installing database.");
 			installDDL();
 		}
 	}
 
 	@Override
 	public List<Class<?>> getDatabaseClasses() {
 		List<Class<?>> list = new ArrayList<Class<?>>();
 		list.add(HomeTable.class);
 		list.add(WarpTable.class);
 		list.add(MailTable.class);
 		list.add(KitTable.class);
 		list.add(KitItemsTable.class);
 		list.add(UserTable.class);
 		// list.add(JailTable.class);
 		return list;
 	}
 
 	public void createMotdConfig() {
 		try {
 			new File(this.getDataFolder(), "motd.txt").createNewFile();
 			FileWriter fstream = new FileWriter(new File(getDataFolder(),
 					"motd.txt"));
 			BufferedWriter out = new BufferedWriter(fstream);
 			out.write("&4Welcome to our 9Minecraft Server&4,&f +d&4!\n");
 			out.write("&4There are +online players online!\n");
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
 			this.logger(Level.WARNING, "Could not get ban list");
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
 
 	public List<String> readMail(String reciever) {
 		ArrayList<String> mailarray = new ArrayList<String>();
 		List<MailTable> mt = this.getDatabase().find(MailTable.class).where()
 				.ieq("reciever", reciever).findList();
 		for (MailTable m : mt) {
 			mailarray.add(m.getSender() + ": " + m.getMessage());
 		}
 		return mailarray;
 	}
 
 	public void sendMail(Player player, String targetPlayer, String message) {
 		MailTable mt = new MailTable();
 		mt.setSender(player.getName());
 		mt.setReciever(targetPlayer);
 		mt.setMessage(message);
 		this.getDatabase().save(mt);
 		Player tplayer = this.playerMatch(targetPlayer);
 		if (tplayer != null) {
 			tplayer.sendMessage("You have a message!");
 		}
 		player.sendMessage(premessage + "Mail sent");
 	}
 
 	public void clearMail(Player player) {
 		List<MailTable> mt = this.getDatabase().find(MailTable.class).where()
 				.ieq("reciever", player.getName()).findList();
 		for (MailTable m : mt) {
 			if (m == null)
 				continue;
 
 			this.getDatabase().delete(m);
 		}
 		player.sendMessage(premessage + "Mail deleted");
 	}
 
 	public boolean hasKitRank(Player player, String kitname) {
 		if (!this.permEnabled) {
 			KitTable kit = this.getDatabase().find(KitTable.class).where()
 					.ieq("name", kitname).findUnique();
 			String userGroup = "";
 			if (userGroup.equalsIgnoreCase(kit.getRank()))
 				return true;
 
 			return false;
 		}
 
 		return true;
 	}
 
 	public void addKit(Player p, String name, int id, short durability,
 			int quanity) {
 		KitTable k = getDatabase().find(KitTable.class).where()
 				.ieq("name", name).findUnique();
 		if (k != null) {
 			p.sendMessage(premessage + "Kit name is already in use.");
 		} else {
 			KitTable k1 = new KitTable();
 			k1.setName(name);
 			getDatabase().save(k1);
 			KitItemsTable kit = new KitItemsTable();
 			kit.setItemid(k1.getId());
 			kit.setQuanity(quanity);
 			kit.setDurability(durability);
 			getDatabase().save(kit);
 			p.sendMessage(premessage
 					+ "The new kit has been saved, to add more items use the /kit additem command.");
 		}
 	}
 
 	public void removeKit(Player p, String name) {
 		KitTable k = getDatabase().find(KitTable.class).where()
 				.ieq("name", name).findUnique();
 		if (k == null) {
 			p.sendMessage(premessage + "Kit not found.");
 		} else {
 			List<KitItemsTable> k1 = getDatabase().find(KitItemsTable.class)
 					.where().eq("itemid", k.getId()).findList();
 			if (k1 != null) {
 				for (KitItemsTable k2 : k1)
 					getDatabase().delete(k2);
 			}
 			getDatabase().delete(k);
 			p.sendMessage(premessage + name + " kit has been deleted");
 		}
 	}
 
 	public void addItem(Player p, String name, int id, short durability,
 			int quanity) {
 		KitTable k = getDatabase().find(KitTable.class).where()
 				.ieq("name", name).findUnique();
 		if (k != null) {
 			KitItemsTable k1 = new KitItemsTable();
 			k1.setItemid(k.getId());
 			k1.setItem(id);
 			k1.setDurability(durability);
 			k1.setQuanity(quanity);
 			getDatabase().save(k1);
 			p.sendMessage(premessage + "New item added to " + name);
 		}
 	}
 
 	public void removeItem(Player p, String name, int id, short durability) {
 		KitTable k = getDatabase().find(KitTable.class).where()
 				.ieq("name", name).findUnique();
 		if (k != null) {
 			KitItemsTable k1 = getDatabase().find(KitItemsTable.class).where()
 					.eq("itemid", k.getId()).eq("item", id)
 					.eq("durability", durability).findUnique();
 			if (k1 != null) {
 				getDatabase().delete(k1);
 				p.sendMessage(premessage + "Item removed from " + name);
 			}
 		}
 	}
 
 	public ArrayList<String> getKit(Player player, String[] args) {
 		int id = 0;
 		KitTable kit = this.getDatabase().find(KitTable.class).where()
 				.ieq("name", args[0]).findUnique();
 		if (kit != null) {
 			id = kit.getId();
 		} else {
 			player.sendMessage("Kit not found.");
 			return null;
 		}
 		ArrayList<String> itemarray = new ArrayList<String>();
 
 		List<KitItemsTable> kt = this.getDatabase().find(KitItemsTable.class)
 				.where().eq("id", id).findList();
 		for (KitItemsTable k : kt) {
 			itemarray.add(k.getItem() + " " + k.getQuanity());
 		}
 
 		return itemarray;
 	}
 
 	public List<String> kitList(String player) {
 		ArrayList<String> namearray = new ArrayList<String>();
 
 		List<KitTable> kt = this.getDatabase().find(KitTable.class)
 				.select("name").findList();
 		for (KitTable k : kt) {
 			namearray.add(k.getName());
 		}
 
 		return namearray;
 	}
 
 	public String[] getMobs(String world) {
 		ArrayList<String> moblist = new ArrayList<String>();
 		try {
 			BufferedReader in = new BufferedReader(new FileReader(
 					getDataFolder() + File.separator + "MobBlackList"
 							+ File.separator + world + ".txt"));
 			String str;
 			while ((str = in.readLine()) != null) {
 				moblist.add(str);
 			}
 			in.close();
 		} catch (IOException e) {
 			this.logger(Level.WARNING, "Could not get mob blacklist.");
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
 			this.logger(Level.WARNING, "Could not find item.txt");
 		}
 
 		return itemlist.toArray(new String[] {});
 	}
 
 	public String colorizeText(String string) {
 		string = string.replaceAll("&0", "0");
 		string = string.replaceAll("&1", "1");
 		string = string.replaceAll("&2", "2");
 		string = string.replaceAll("&3", "3");
 		string = string.replaceAll("&4", "4");
 		string = string.replaceAll("&5", "5");
 		string = string.replaceAll("&6", "6");
 		string = string.replaceAll("&7", "7");
 		string = string.replaceAll("&8", "8");
 		string = string.replaceAll("&9", "9");
 		string = string.replaceAll("&a", "a");
 		string = string.replaceAll("&b", "b");
 		string = string.replaceAll("&c", "c");
 		string = string.replaceAll("&d", "d");
 		string = string.replaceAll("&e", "e");
 		string = string.replaceAll("&f", "f");
 		return string;
 	}
 
 	public void logger(Level l, String s) {
 		this.getLogger().log(l, "[CraftEssence] " + s);
 	}
 
 }
