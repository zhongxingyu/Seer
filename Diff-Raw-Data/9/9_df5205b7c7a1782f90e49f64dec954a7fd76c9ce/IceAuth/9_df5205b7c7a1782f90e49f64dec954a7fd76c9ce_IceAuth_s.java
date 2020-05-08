 package eu.icecraft.iceauth;
 
 import java.io.File;
 import java.math.BigInteger;
 import java.security.MessageDigest;
 import java.security.NoSuchAlgorithmException;
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.logging.Logger;
 
 import org.bukkit.ChatColor;
 import org.bukkit.GameMode;
 import org.bukkit.Location;
 import org.bukkit.World;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.inventory.PlayerInventory;
 import org.bukkit.metadata.FixedMetadataValue;
 import org.bukkit.plugin.java.JavaPlugin;
 
 import com.alta189.sqlLibrary.MySQL.mysqlCore;
 import com.alta189.sqlLibrary.SQLite.sqlCore;
 
 import eu.icecraft.iceauth.configCompat.Configuration;
 
 public class IceAuth extends JavaPlugin {
 	public String logPrefix = "[IceAuth] ";
 	public Logger nativeLogger = Logger.getLogger("Minecraft");
 	private Configuration conf;
 
 	public mysqlCore manageMySQL;
 	public sqlCore manageSQLite;
 
 	public boolean giveKits;
 	public List<String> kit;
 	public List<String> referralKit;
 
 	public Location firstSpawn;
 
 	public boolean hideChatNonLogged = true;
 	public boolean useReferrals = false;
 	public boolean noCreativeWorld = true;
 
 	public boolean MySQL = false;
 	public String dbHost;
 	public String dbUser;
 	public String dbPass;
 	public String dbDatabase;
 	public String tableName;
 	public String userField;
 	public String passField;
 
 	public Map<String, LoggedInPlayer> playersLoggedIn = new HashMap<String, LoggedInPlayer>();
 	public ArrayList<String> notRegistered = new ArrayList<String>();
 	public Map<String, NLIData> notLoggedIn = new HashMap<String, NLIData>();
 
 	public static int threadRuns;
 	public static int sqlQueries = 0;
 	public static long timingMicros;
 	public static long sqlQueryTime;
 	public static long syncTaskTime;
 
 	public static BufferedLogger bufferedLogger;
 	public MessageDigest md5;
 	public NLICacheHandler nch;
 	public Referrals ref;
 
 	@Override
 	public void onDisable() {
 		this.getServer().getScheduler().cancelTasks(this);
 		bufferedLogger.disable();
 		System.out.println(this.getDescription().getName() + " " + this.getDescription().getVersion() + " was disabled!");
 	}
 
 	@Override
 	public void onEnable() {
 		startTiming();
 
 		if(!this.getDataFolder().exists()) this.getDataFolder().mkdir();
 
		bufferedLogger = new BufferedLogger(new File(this.getDataFolder(), "log.txt"), 5);
 
 		File confFile = new File(this.getDataFolder(), "config.yml");
 		conf = new Configuration(confFile);
 		if(!confFile.exists()) {
 			conf.setProperty("mysql.use", false);
 			conf.setProperty("mysql.dbHost", "localhost");
 			conf.setProperty("mysql.dbUser", "root");
 			conf.setProperty("mysql.dbPass", "");
 			conf.setProperty("mysql.database", "minecraft");
 			conf.setProperty("mysql.tableName", "auth");
 			conf.setProperty("mysql.userField", "username");
 			conf.setProperty("mysql.passField", "password");
 
 			List<String> defaultKit = new ArrayList<String>();
 			defaultKit.add("358:1:1"); // 1 welcome map
 			defaultKit.add("358:2:1"); // 1 rules map
 			defaultKit.add("270:0:1"); // 1 wooden pickaxe
 			defaultKit.add("17:0:4"); // 4 logs
 
 			conf.setProperty("kits.enable", true);
 			conf.setProperty("kits.items", defaultKit);
 
 			conf.setProperty("hideChatNonLogged", true);
 			conf.setProperty("referrals.enable", false);
 			conf.setProperty("referrals.items", defaultKit);
 
 			conf.setProperty("no-creative-world", true);
 
 			conf.save();
 		}
 
 		conf.load();
 
 		this.giveKits = conf.getBoolean("kits.enable", true);
 		this.kit = conf.getStringList("kits.items", null);
 
 		this.MySQL = conf.getBoolean("mysql.use", false);
 		this.dbHost = conf.getString("mysql.dbHost");
 		this.dbUser = conf.getString("mysql.dbUser");
 		this.dbPass = conf.getString("mysql.dbPass");
 		this.dbDatabase = conf.getString("mysql.database");
 		this.tableName = conf.getString("mysql.tableName");
 		this.userField = conf.getString("mysql.userField");
 		this.passField = conf.getString("mysql.passField");
 
 		this.hideChatNonLogged = conf.getBoolean("hideChatNonLogged", true);
 		this.useReferrals = conf.getBoolean("referrals.enable", false);
 		if(useReferrals) this.referralKit = conf.getStringList("referrals.items", null);
 		this.noCreativeWorld = conf.getBoolean("no-creative-world", true);
 
 		World world = this.getServer().getWorlds().get(0);
 		try {
 			double x = Double.parseDouble(conf.getString("firstspawn.x"));
 			double y = Double.parseDouble(conf.getString("firstspawn.y"));
 			double z = Double.parseDouble(conf.getString("firstspawn.z"));
 			float yaw = Float.parseFloat(conf.getString("firstspawn.yaw"));
 			float pitch = Float.parseFloat(conf.getString("firstspawn.pitch"));
 
 			firstSpawn = new Location(world, x, y, z, yaw, pitch);
 		} catch(Exception firstExc) {
 			firstSpawn = this.getServer().getWorlds().get(0).getSpawnLocation();
 		}
 
 		if (this.MySQL) {
 			if (this.dbHost.equals(null)) { this.MySQL = false; this.nativeLogger.severe(this.logPrefix + "MySQL is on, but host is not defined, defaulting to SQLite"); }
 			if (this.dbUser.equals(null)) { this.MySQL = false; this.nativeLogger.severe(this.logPrefix + "MySQL is on, but username is not defined, defaulting to SQLite"); }
 			if (this.dbPass.equals(null)) { this.MySQL = false; this.nativeLogger.severe(this.logPrefix + "MySQL is on, but password is not defined, defaulting to SQLite"); }
 			if (this.dbDatabase.equals(null)) { this.MySQL = false; this.nativeLogger.severe(this.logPrefix + "MySQL is on, but database is not defined, defaulting to SQLite"); }
 		}
 
 		if (this.MySQL) {
 
 			this.manageMySQL = new mysqlCore(this.nativeLogger, this.logPrefix, this.dbHost, this.dbDatabase, this.dbUser, this.dbPass);
 
 			this.nativeLogger.info(this.logPrefix + "MySQL Initializing");
 
 			this.manageMySQL.initialize();
 
 			try {
 				if (this.manageMySQL.checkConnection()) {
 					this.nativeLogger.info(this.logPrefix + "MySQL connection successful");
 					if (!this.manageMySQL.checkTable(tableName)) {
 						this.MySQL = false;
 					}
 				} else {
 					this.nativeLogger.severe(this.logPrefix + "MySQL connection failed. Defaulting to SQLite");
 					this.MySQL = false;
 					this.tableName = "auth";
 					this.userField = "username";
 					this.passField = "password";
 				}
 			} catch (Exception e) {
 				e.printStackTrace();
 			}
 		} else {
 			this.nativeLogger.info(this.logPrefix + "SQLite Initializing");
 
 			this.manageSQLite = new sqlCore(this.nativeLogger, this.logPrefix, "IceAuth", this.getDataFolder().getPath());
 
 			this.tableName = "auth";
 			this.userField = "username";
 			this.passField = "password";
 
 			this.manageSQLite.initialize();
 
 			if (!this.manageSQLite.checkTable(tableName)) {
 				this.manageSQLite.createTable("CREATE TABLE auth (id INT AUTO_INCREMENT PRIMARY_KEY, username VARCHAR(30), password VARCHAR(50));");
 			}
 		}
 
 		if(this.manageMySQL == null) useReferrals = false;
 		if(useReferrals) ref = new Referrals(this);
 
 		try {
 			this.md5 = MessageDigest.getInstance("MD5");
 		} catch(NoSuchAlgorithmException ex) {
 			ex.printStackTrace();
 		}
 
 		nch = new NLICacheHandler(this);
 
 		IceAuthPlayerListener playerListener = new IceAuthPlayerListener(this);
 		IceAuthBlockListener blockListener = new IceAuthBlockListener(this);
 		IceAuthEntityListener entityListener = new IceAuthEntityListener(this);
 
 		this.getServer().getPluginManager().registerEvents(playerListener, this);
 		this.getServer().getPluginManager().registerEvents(blockListener, this);
 		this.getServer().getPluginManager().registerEvents(entityListener, this);
 
 		this.getServer().getScheduler().scheduleSyncRepeatingTask(this, new PlayerSyncThread(), 25, 25);
 
 		int registeredUsers = registeredUsers();
 
 		int timeToStart = Math.round(stopTiming() / 1000);
 		System.out.println(this.getDescription().getName() + " " + this.getDescription().getVersion() + " has been enabled in " + timeToStart + "ms. Loaded " + registeredUsers + " users.");
 	}
 
 	@Override
 	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
 		if(commandLabel.equalsIgnoreCase("register")) {
 			if(!(sender instanceof Player)) {
 				return false;
 			}
 
 			Player player = (Player) sender;
 			if(!checkUnReg(player)) {
 				player.sendMessage(ChatColor.RED + "Already registered.");
 				return false;
 			}
 			if(args.length != 1) {
 				player.sendMessage("Usage: /register <password>");
 				return false;
 			}
 
 			String password = args[0];
 
 			if(!register(player.getName(), password)) {
 				player.sendMessage(ChatColor.RED + "Something failed?");
 				return false;
 			}
 
 			updateIP(player.getName(), player.getAddress().getAddress().getHostAddress(), true);
 
 			player.sendMessage(ChatColor.GREEN + "Registered successfully! You have been logged in.");
 
 			restoreInv(player);
 
 			// removed due to exploitation issues
 			//NLIData nli = notLoggedIn.get(player.getName());
 			//player.setGameMode(nli.getGameMode());
 
 			delPlayerNotLoggedIn(player);
 			addAuthPlayer(player);
 
 			ref.onRegister(player);
 
 			this.getServer().getPluginManager().callEvent(new AuthPlayerLoginEvent(player, true));
 
 			return true;
 		}
 
 		if(commandLabel.equalsIgnoreCase("login") || commandLabel.equalsIgnoreCase("l")) {
 			if(!(sender instanceof Player)) {
 				return false;
 			}
 			Player player = (Player) sender;
 
 			if(args.length != 1) {
 				player.sendMessage("Usage: /login <password>");
 				return false;
 			}
 
 			String playername = player.getName();
 			String password = args[0];
 
 			if(checkUnReg(player)) {
 				player.sendMessage(ChatColor.RED + "You need to register first!");
 				return false;
 			}
 
 			if(checkAuth(player)) {
 				player.sendMessage(ChatColor.RED + "Already logged in!");
 				return false;
 			}
 
 			if(checkLogin(playername, password)) {
 				player.sendMessage(ChatColor.GREEN + "Logged in successfully.");
 
 				restoreInv(player);
 
 				// removed due to exploitation issues
 				NLIData nli = notLoggedIn.get(playername);
 				//player.setGameMode(nli.getGameMode());
 
 				player.teleport(nli.getLoc());
 
 				delPlayerNotLoggedIn(player);
 				addAuthPlayer(player);
 
 				if(getIP(playername, true) == null) updateIP(playername, player.getAddress().getAddress().getHostAddress(), true); // Fill in missing register IPs
 				else updateIP(playername, player.getAddress().getAddress().getHostAddress());
 
 				this.getServer().getPluginManager().callEvent(new AuthPlayerLoginEvent(player, false));
 
 				return true;
 			} else {
 				player.sendMessage(ChatColor.RED + "Wrong password!");
 				log("Player "+player.getName()+" tried logging in with a wrong password!");
 				return false;
 			}
 		}
 
 		if(commandLabel.equalsIgnoreCase("changepassword")) {
 			if(!(sender instanceof Player)) {
 				return false;
 			}
 
 			Player player = (Player) sender;
 
 			if(checkUnReg(player)) {
 				player.sendMessage(ChatColor.RED + "You aren't registered!");
 				return false;
 			}
 			if(!checkAuth(player)) {
 				player.sendMessage(ChatColor.RED + "You aren't logged in!");
 				return false;
 			}
 			if(args.length != 2) {
 				player.sendMessage("Usage: /changepassword <oldpassword> <newpassword>");
 				return false;
 			}
 
 			changePassword(args[0], args[1], player);
 			return true;
 		}
 
 		if(commandLabel.equalsIgnoreCase("iceauth") && sender.isOp()) {
 			int sqlMillis = Math.round(IceAuth.sqlQueryTime / 1000);
 			String sqlMsg = null;
 			if(sqlMillis == 0) {
 				sqlMsg = IceAuth.sqlQueryTime + "us.";
 			} else {
 				sqlMsg = sqlMillis + "ms.";
 			}
 
 			int taskMillis = Math.round(IceAuth.syncTaskTime / 1000);
 			String taskMsg = null;
 			if(taskMillis == 0) {
 				taskMsg =  IceAuth.syncTaskTime + "us.";
 			} else {
 				taskMsg = taskMillis + "ms.";
 			}
 
 			sender.sendMessage(ChatColor.YELLOW + "=== IceAuth performance stats ===");
 			sender.sendMessage("Time taken for SQL queries: " + sqlMsg + " over " + IceAuth.sqlQueries + " queries.");
 			sender.sendMessage("Time taken for sync task: " + taskMsg + " over " + IceAuth.threadRuns + " runs.");
 			sender.sendMessage("playersLoggedIn size: " + this.playersLoggedIn.size());
 			sender.sendMessage("notRegistered size: " + this.notRegistered.size());
 			sender.sendMessage("notLoggedIn size: " + this.notLoggedIn.size());
 			return true;
 		}
 
 		if(commandLabel.equalsIgnoreCase("setfirstspawn") && sender.isOp()) {
 			if(!(sender instanceof Player)) {
 				return false;
 			}
 			Player player = (Player) sender;
 
 			Location loc = player.getLocation();
 
 			conf.setProperty("firstspawn.x", loc.getX());
 			conf.setProperty("firstspawn.y", loc.getY());
 			conf.setProperty("firstspawn.z", loc.getZ());
 			conf.setProperty("firstspawn.yaw", loc.getYaw());
 			conf.setProperty("firstspawn.pitch", loc.getPitch());
 			conf.save();
 
 			this.firstSpawn = loc;
 			player.sendMessage(ChatColor.GREEN + "First spawn point set sucessfully.");
 			return true;
 		}
 
 		if(commandLabel.equalsIgnoreCase("firstspawn")) {
 			if(!(sender instanceof Player)) {
 				return false;
 			}
 			Player player = (Player) sender;
 			player.teleport(firstSpawn);
 			return true;
 		}
 
 		if(commandLabel.equalsIgnoreCase("ref")) {
 			if(!(sender instanceof Player)) {
 				return false;
 			}
 			Player player = (Player) sender;
 			if(useReferrals) ref.refLinkCmd(player);
 			return true;
 		}
 
 		if(commandLabel.equalsIgnoreCase("onlinetime")) {
 			if(args.length != 1) return false;
 			LoggedInPlayer lp = null;
 			if(playersLoggedIn.containsKey(args[0])) {
 				lp = playersLoggedIn.get(args[0]);
 			} else {
 				lp = getLoginData(args[0]);
 			}
 			int playTime = Math.round(lp.getOnlineTime() + (((int)(System.currentTimeMillis()/1000L) - lp.getLoggedInAt())));
 			if(playTime <= 1) {
 				sender.sendMessage(ChatColor.RED + "Not enough data collected for that player.");
 			} else {
 				sender.sendMessage(ChatColor.DARK_AQUA + args[0] + ChatColor.YELLOW + " has played for:");
 				sender.sendMessage(ChatColor.GRAY + getDetailedTimeString(playTime));
 			}
 			return true;
 		}
 
 		if(commandLabel.equalsIgnoreCase("ipseen")) {
 			if(args.length != 1 || !sender.hasPermission("iceauth.ipseen")) return false;
 			ResultSet result = null;
 			try {
 				Connection connection = null;
 
 				if (this.MySQL) {
 					try {
 						connection = this.manageMySQL.getConnection();
 					} catch (Exception e) {
 						e.printStackTrace();
 					}
 				} else {
 					connection = this.manageSQLite.getConnection();
 				}
 
 				String ip = getIP(args[0]);
 				if(ip == null) {
 					sender.sendMessage(ChatColor.RED + "Player not found in history!");
 					return true;
 				}
 
 				PreparedStatement regQ = connection.prepareStatement("SELECT "+userField+" FROM "+tableName+" WHERE "+tableName+".lastIP = ?");
 				regQ.setString(1, ip);
 				result = regQ.executeQuery();
 
 				sender.sendMessage(ChatColor.GOLD + "All accounts on IP " + ChatColor.DARK_AQUA + ip + ChatColor.GOLD + ", player " + ChatColor.DARK_AQUA + args[0] + ChatColor.GOLD + ":");
 				StringBuilder names = new StringBuilder();
 				while(result.next()) {
 					names.append(result.getString(userField)).append(", ");
 				}
 				if(names.length() > 2) names.deleteCharAt(names.length() - 2);
 				sender.sendMessage(ChatColor.YELLOW + names.toString());
 
 			} catch (Exception e) {
 				e.printStackTrace();
 			} finally {
 				try {
 					result.close();
 				} catch (Exception e) {
 				}
 			}
 
 			return true;
 		}
 
 		if(commandLabel.equalsIgnoreCase("ipinfo")) {
 			if(args.length != 1 || !sender.hasPermission("iceauth.ipinfo")) return false;
 			ResultSet result = null;
 			try {
 				Connection connection = null;
 
 				if (this.MySQL) {
 					try {
 						connection = this.manageMySQL.getConnection();
 					} catch (Exception e) {
 						e.printStackTrace();
 					}
 				} else {
 					connection = this.manageSQLite.getConnection();
 				}
 
 				PreparedStatement regQ = connection.prepareStatement("SELECT "+userField+" FROM "+tableName+" WHERE lastIP = ?");
 				regQ.setString(1, args[0].toLowerCase());
 				result = regQ.executeQuery();
 
 				sender.sendMessage(ChatColor.GOLD + "All accounts on IP " + ChatColor.DARK_AQUA + args[0] + ChatColor.GOLD + ":");
 				StringBuilder names = new StringBuilder();
 				while(result.next()) {
 					names.append(result.getString(userField)).append(", ");
 				}
 				if(names.length() > 2) names.deleteCharAt(names.length() - 2);
 				sender.sendMessage(ChatColor.YELLOW + names.toString());
 
 			} catch (Exception e) {
 				e.printStackTrace();
 			} finally {
 				try {
 					result.close();
 				} catch (Exception e) {
 				}
 			}
 
 			return true;
 		}
 
 		return false;
 	}
 
 	public void addAuthPlayer(Player player) {
 		playersLoggedIn.put(player.getName(), getLoginData(player.getName()));
 	}
 
 	public boolean checkAuth(Player player) {
 		return playersLoggedIn.containsKey(player.getName());
 	}
 
 	public boolean checkUnReg(Player player) {
 		return notRegistered.contains(player.getName());
 	}
 
 	public void removePlayerCache(Player player) {
 		String pName = player.getName();
 
 		if(!checkAuth(player)) restoreInv(player); else saveInventory(player);
 
 		playersLoggedIn.remove(pName);
 		notLoggedIn.remove(pName);	
 		notRegistered.remove(pName);
 		player.removeMetadata("notLoggedIn", this);
 	}
 
 	public void addPlayerNotLoggedIn(Player player, Location loc, boolean registered) {
 		NLIData nli = new NLIData(loc, (int) (System.currentTimeMillis() / 1000L), player.getInventory().getContents(), player.getInventory().getArmorContents(), player.getGameMode());
 		notLoggedIn.put(player.getName(), nli);
 		if(!registered) notRegistered.add(player.getName());
 		player.setMetadata("notLoggedIn", new FixedMetadataValue(this, true));
 	}
 
 	public void saveInventory(Player player) {
 		NLIData nli = new NLIData(player.getInventory().getContents(), player.getInventory().getArmorContents());
 		nch.createCache(player.getName(), nli);
 	}
 
 	public void delPlayerNotLoggedIn(Player player) {
 		notLoggedIn.remove(player.getName());	
 		notRegistered.remove(player.getName());
 		player.removeMetadata("notLoggedIn", this);
 	}
 
 	public void msgPlayerLogin(Player player) {
 		msgPlayerLogin(player, false);
 	}
 
 	public void msgPlayerLogin(Player player, boolean first) {
 		if(checkUnReg(player)) {
 			if(first) player.sendMessage(ChatColor.DARK_AQUA + "Welcome! You need to register to be able to move and build.");
 			player.sendMessage(ChatColor.GREEN + "Use /register <password> to register!");
 		} else {
 			if(first) player.sendMessage(ChatColor.DARK_AQUA + "Welcome! You need to log in to be able to move and build.");
 			player.sendMessage(ChatColor.GREEN + "Use /login <password> to log in!");
 		}
 	}
 
 	public boolean checkInvEmpty(ItemStack[] invstack) {
 		for (int i = 0; i < invstack.length; i++) {
 			if (invstack[i] != null) {
 				if(invstack[i].getTypeId() > 0) return false;
 			}
 		}
 		return true;
 	}
 
 	public boolean isInvCacheEmpty(String pName) {
 		NLIData nli = nch.readCache(pName);
 		ItemStack[] inv = nli.getInventory();
 		if(checkInvEmpty(inv)) return true; 
 		return false;
 	}
 
 	public void restoreInv(Player player) {
 		restoreInv(player, false);
 	}
 
 	public void restoreInv(Player player, boolean useCache) {
 		NLIData nli = null;
 
 		if(useCache) {
 			nli = nch.readCache(player.getName());
 		} else {
 			nli = notLoggedIn.get(player.getName());
 		}
 
 		ItemStack[] invstackbackup = null;
 		ItemStack[] armStackBackup = null;
 
 		try {
 			invstackbackup = nli.getInventory();
 			armStackBackup = nli.getArmour();
 		} catch(Exception e) {
 			log("Restoring inventory failed for player " + player.getName());
 			e.printStackTrace();
 			return;
 		}
 
 		if(invstackbackup != null) {
 			player.getInventory().setContents(invstackbackup);
 		}
 
 		if(armStackBackup[3] != null) {
 			if(armStackBackup[3].getAmount() != 0) {
 				player.getInventory().setHelmet(armStackBackup[3]);
 			}
 		}
 		if(armStackBackup[2] != null) {
 			if(armStackBackup[2].getAmount() != 0) {
 				player.getInventory().setChestplate(armStackBackup[2]);
 			}
 		}
 		if(armStackBackup[1] != null) {
 			if(armStackBackup[1].getAmount() != 0) {
 				player.getInventory().setLeggings(armStackBackup[1]);
 			}
 		}
 		if(armStackBackup[0] != null) {
 			if(armStackBackup[0].getAmount() != 0) {
 				player.getInventory().setBoots(armStackBackup[0]);
 			}
 		}
 	}
 
 	public String getMD5(String message) {
 		byte[] digest;
 		md5.reset();
 		md5.update(message.getBytes());
 		digest = md5.digest();
 
 		return String.format("%0" + (digest.length << 1) + "x", new BigInteger(1, digest));
 	}
 
 	public static void log(String what) {
 		log(what, "INFO");
 	}
 
 	public static void log(String what, String prefix) {
 		bufferedLogger.log("["+prefix+"] " + what);
 	}
 
 	public boolean isRegistered(String name) {
 
 		ResultSet result = null;
 
 		Connection connection = null;
 
 		if (this.MySQL) {
 			try {
 				connection = this.manageMySQL.getConnection();
 			} catch (Exception e) {
 				e.printStackTrace();
 			}
 		} else {
 			connection = this.manageSQLite.getConnection();
 		}
 
 		try {
 			startTiming();
 			PreparedStatement regQ = connection.prepareStatement("SELECT COUNT(*) AS c FROM "+tableName+" WHERE " + userField + " = ?");
 			regQ.setString(1, name);
 			result = regQ.executeQuery();
 			IceAuth.sqlQueryTime += stopTiming();
 			IceAuth.sqlQueries++;
 
 			while(result.next()) {
 				if(result.getInt("c") > 0) {
 					return true;
 				} else {
 					return false;
 				}
 			}
 
 		} catch (SQLException e) {
 			e.printStackTrace();
 		} finally {
 			try {
 				result.close();
 			} catch (SQLException e) {
 				e.printStackTrace();
 			}
 		}
 
 		return false;
 	}
 
 	public boolean checkLogin(String name, String password) {
 
 		ResultSet result = null;
 		Connection connection = null;
 
 		if (this.MySQL) {
 			try {
 				connection = this.manageMySQL.getConnection();
 			} catch (Exception e) {
 				e.printStackTrace();
 			}
 		} else {
 			connection = this.manageSQLite.getConnection();
 		}
 		try {
 			startTiming();
 			PreparedStatement regQ = connection.prepareStatement("SELECT COUNT(*) AS c FROM "+tableName+" WHERE " + userField + " = ? && "+passField+" = ?");
 			regQ.setString(1, name);
 			regQ.setString(2, getMD5(password));
 			result = regQ.executeQuery();
 			IceAuth.sqlQueryTime += stopTiming();
 			IceAuth.sqlQueries++;
 
 			while(result.next()) {
 				if(result.getInt("c") > 0) {
 					return true;
 				} else {
 					return false;
 				}
 			}
 
 		} catch (SQLException e) {
 			e.printStackTrace();
 		} finally {
 			try {
 				result.close();
 			} catch (SQLException e) {
 				e.printStackTrace();
 			}
 		}
 
 
 		return false;
 	}
 
 	public boolean register(String name, String password) {
 		Connection connection = null;
 
 		if (this.MySQL) {
 			try {
 				connection = this.manageMySQL.getConnection();
 			} catch (Exception e) {
 				e.printStackTrace();
 			}
 		} else {
 			connection = this.manageSQLite.getConnection();
 		}
 		try {
 			startTiming();
 			PreparedStatement regQ = connection.prepareStatement("INSERT INTO "+tableName+" ("+userField+", "+passField+", registeredOn) VALUES(?,?,?)");
 			regQ.setString(1, name);
 			regQ.setString(2, getMD5(password));
 			regQ.setInt(3, (int)(System.currentTimeMillis() / 1000L));
 			regQ.executeUpdate();
 			IceAuth.sqlQueryTime += stopTiming();
 			IceAuth.sqlQueries++;
 
 			log("Player "+name+" registered sucessfully.");
 
 			notRegistered.remove(name);
 
 			return true;
 
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 
 		return false;
 	}
 
 	public boolean changePassword(String oldpass, String password, Player player) {
 
 		if(checkLogin(player.getName(), oldpass)) {
 
 			Connection connection = null;
 
 			if (this.MySQL) {
 				try {
 					connection = this.manageMySQL.getConnection();
 				} catch (Exception e) {
 					e.printStackTrace();
 				}
 			} else {
 				connection = this.manageSQLite.getConnection();
 			}
 			try {
 				startTiming();
 				PreparedStatement regQ = connection.prepareStatement("UPDATE "+tableName+" SET " + passField + " = ? WHERE " + userField + " = ?");
 				regQ.setString(1, getMD5(password));
 				regQ.setString(2, player.getName());
 				regQ.executeUpdate();
 				IceAuth.sqlQueryTime += stopTiming();
 				IceAuth.sqlQueries++;
 
 				player.sendMessage(ChatColor.GREEN + "Password updated sucessfully!");
 				log("Player "+player.getName()+" changed his password!");
 				return true;
 
 			} catch (SQLException e) {
 				e.printStackTrace();
 			}
 
 		} else {
 			player.sendMessage(ChatColor.RED + "Wrong password!");
 			log("Player "+player.getName()+" tried changepassword with a wrong password!");
 			return true;
 		}
 
 		return false;
 	}
 
 	public int registeredUsers() {
 
 		ResultSet result = null;
 
 		Connection connection = null;
 
 		if (this.MySQL) {
 			try {
 				connection = this.manageMySQL.getConnection();
 			} catch (Exception e) {
 				e.printStackTrace();
 			}
 		} else {
 			connection = this.manageSQLite.getConnection();
 		}
 
 		try {
 			startTiming();
 			PreparedStatement regQ = connection.prepareStatement("SELECT COUNT(*) AS c FROM "+tableName);
 			result = regQ.executeQuery();
 			IceAuth.sqlQueryTime += stopTiming();
 			IceAuth.sqlQueries++;
 
 			while(result.next()) {
 				return result.getInt("c");
 			}
 
 		} catch (SQLException e) {
 			e.printStackTrace();
 		} finally {
 			try {
 				result.close();
 			} catch (SQLException e) {
 				e.printStackTrace();
 			}
 		}
 
 		return 0;
 	}
 
 	public String getIP(String nick) {
 		return getIP(nick, false);
 	}
 
 	public String getIP(String nick, boolean registerIP) {
 		ResultSet result = null;
 		try {
 			Connection connection = null;
 
 			if (this.MySQL) {
 				try {
 					connection = this.manageMySQL.getConnection();
 				} catch (Exception e) {
 					e.printStackTrace();
 				}
 			} else {
 				connection = this.manageSQLite.getConnection();
 			}
 
 			PreparedStatement regQ = connection.prepareStatement("SELECT registerIP, lastIP FROM "+tableName+" WHERE "+tableName+"."+userField+" = ?");
 			regQ.setString(1, nick.toLowerCase());
 			result = regQ.executeQuery();
 
 			while(result.next()) {
 				return registerIP ? result.getString("registerIP") : result.getString("lastIP");
 			}
 		} catch (Exception e) {
 			e.printStackTrace();
 		} finally {
 			try {
 				result.close();
 			} catch (SQLException e) {
 				e.printStackTrace();
 			}
 		}
 		return null;
 	}
 
 	public void updateIP(String nick, String ip) {
 		updateIP(nick, ip, false);
 	}
 
 	public void updateIP(String nick, String ip, boolean registerIP) {
 		Connection connection = null;
 		try {
 			if (this.MySQL) {
 				connection = this.manageMySQL.getConnection();
 			} else {
 				connection = this.manageSQLite.getConnection();
 			}
 			PreparedStatement regQupd;
 			if(registerIP) {
 				regQupd = connection.prepareStatement("UPDATE "+tableName+" SET registerIP = ?, lastIP = ? WHERE "+userField+" = ?");
 				regQupd.setString(1, ip);
 				regQupd.setString(2, ip);
 				regQupd.setString(3, nick);
 			} else {
 				regQupd = connection.prepareStatement("UPDATE "+tableName+" SET lastIP = ? WHERE "+userField+" = ?");
 				regQupd.setString(1, ip);
 				regQupd.setString(2, nick);
 			}
 
 			regQupd.executeUpdate();
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 
 	public LoggedInPlayer getLoginData(String player) {
 
 		ResultSet result = null;
 		Connection connection = null;
 		int onlineMins = 0;
 		boolean wasReferred = false;
 		String referredBy = null;
 
 		if (this.MySQL) {
 			try {
 				connection = this.manageMySQL.getConnection();
 			} catch (Exception e) {
 				e.printStackTrace();
 			}
 		} else {
 			connection = this.manageSQLite.getConnection();
 		}
 
 		try {
 			startTiming();
 			PreparedStatement regQ = connection.prepareStatement("SELECT onlineMins FROM "+tableName+" WHERE "+tableName+"."+userField+" = ?");
 			regQ.setString(1, player);
 			result = regQ.executeQuery();
 			IceAuth.sqlQueryTime += stopTiming();
 			IceAuth.sqlQueries++;
 
 			while(result.next()) {
 				onlineMins = result.getInt("onlineMins");
 			}
 
 			if(useReferrals) {
 				startTiming();
 				PreparedStatement regQ1 = connection.prepareStatement("SELECT referred_by FROM referrals WHERE ign = ? AND hasplayed = 0");
 				regQ1.setString(1, player);
 				result = regQ1.executeQuery();
 				IceAuth.sqlQueryTime += stopTiming();
 				IceAuth.sqlQueries++;
 
 				while(result.next()) {
 					wasReferred = true;
 					referredBy = result.getString("referred_by");
 				}
 			}
 
 		} catch (SQLException e) {
 			e.printStackTrace();
 		} finally {
 			try {
 				result.close();
 			} catch (SQLException e) {
 				e.printStackTrace();
 			}
 		}
 
 		return new LoggedInPlayer((int) (System.currentTimeMillis()/1000L), onlineMins, wasReferred, referredBy);
 	}
 
 	public void recalculateOnlineTime(Player player) {
 		if(!checkAuth(player)) return;
 		LoggedInPlayer lp = playersLoggedIn.get(player.getName());
 
 		int onlineTimeMins = Math.round(lp.getOnlineTime() + (((int)(System.currentTimeMillis()/1000L) - lp.getLoggedInAt())));
 		Connection connection = null;
 		if (this.MySQL) {
 			try {
 				connection = this.manageMySQL.getConnection();
 			} catch (Exception e) {
 				e.printStackTrace();
 			}
 		} else {
 			connection = this.manageSQLite.getConnection();
 		}
 
 		try {
 			startTiming();
 			PreparedStatement regQ = connection.prepareStatement("UPDATE "+tableName+" SET onlineMins = ? WHERE name = ?");
 			regQ.setInt(1, onlineTimeMins);
 			regQ.setString(2, player.getName());
 			regQ.executeUpdate();
 			IceAuth.sqlQueryTime += stopTiming();
 			IceAuth.sqlQueries++;
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 	}
 
 	public void markPlayerPaid(Player player) {
 
 		Connection connection = null;
 		if (this.MySQL) {
 			try {
 				connection = this.manageMySQL.getConnection();
 			} catch (Exception e) {
 				e.printStackTrace();
 			}
 		} else {
 			connection = this.manageSQLite.getConnection();
 		}
 
 		try {
 			IceAuth.startTiming();
 			PreparedStatement regQupd = connection.prepareStatement("UPDATE reflinks SET referred = referred + 1 WHERE ign = ?");
 			regQupd.setString(1, playersLoggedIn.get(player.getName()).getReferredBy());
 			regQupd.executeUpdate();
 			IceAuth.sqlQueryTime += IceAuth.stopTiming();
 			IceAuth.sqlQueries++;
 
 			startTiming();
 			PreparedStatement regQ = connection.prepareStatement("UPDATE referrals SET hasplayed = 1 WHERE ign = ?");
 			regQ.setString(1, player.getName());
 			regQ.executeUpdate();
 			IceAuth.sqlQueryTime += stopTiming();
 			IceAuth.sqlQueries++;
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 	}
 
 	public static String getDetailedTimeString(long unixTime) {
 		return getDetailedTimeString(unixTime, true);
 	}
 
 	public static String getDetailedTimeString(long unixTime, boolean showSeconds) {
 		int n = (int)(unixTime / 86400L);
 		unixTime -= n * 86400;
 		int i = (int)(unixTime / 3600L);
 		unixTime -= i * 3600;
 		int j = (int)(unixTime / 60L);
 		unixTime -= j * 60;
 		int k = (int)unixTime;
 
 		String str = "";
 
 		if (n > 0) {
 			str = str + n;
 			if (n == 1) str = str + " day";
 			else str = str + " days"; 
 		}
 
 		if (i > 0) {
 			if ((n > 0) && (j > 0)) str = str + ", ";
 			else if ((n > 0) && (j == 0)) str = str + " and "; str = str + i;
 			if (i == 1) str = str + " hour"; 
 			else str = str + " hours"; 
 		}
 
 		if (j > 0) {
 			if ((i > 0) && (k > 0)) str = str + ", ";
 			else if ((i > 0) && (k == 0)) str = str + " and "; str = str + j;
 			if (j == 1) str = str + " minute"; 
 			else str = str + " minutes"; 
 		}
 
 		if (k > 0 && showSeconds) {
 			if ((i > 0) || (j > 0)) str = str + " and "; str = str + k;
 			if (k == 1) str = str + " second";
 			else str = str + " seconds";
 		}
 
 		return str;
 	}
 
 	public void giveKits(Player player, List<String> items, boolean randomTwo) {
 		if(items != null) {
 			PlayerInventory inv = player.getInventory();
 			boolean wasFull = false;
 
 			if(randomTwo) Collections.shuffle(items);
 			int i = 0;
 			for(String item : items) {
 				String[] parts = item.split(":");
 				if(parts.length != 3) continue;
 
 				if(randomTwo) {
 					i++;
 					if(i > 2) break;
 				}
 
 				int type = Integer.parseInt(parts[0]);
 				short damage = Short.parseShort(parts[1]);
 				int amount = Integer.parseInt(parts[2]);
 
 				ItemStack is = new ItemStack(type, amount);
 				if(damage != 0) is.setDurability(damage);
 				if(inv.firstEmpty() == -1) { // Handle full inventories
 					player.getWorld().dropItemNaturally(player.getLocation(), is);
 					if(!wasFull) {
 						player.sendMessage(ChatColor.RED + "Your inventory is full, the remaining items will be dropped.");
 						wasFull = true;
 					}
 				} else {
 					inv.addItem(is);
 				}
 			}
 		}
 	}
 
 	public static void startTiming() {
 		IceAuth.timingMicros = System.nanoTime()/1000;
 	}
 
 	public static long stopTiming() {
 		return (System.nanoTime()/1000) - IceAuth.timingMicros;
 	}
 
 	public void tpPlayers(boolean msgLogin) {
 		startTiming();
 		try {
 			for (Player player : this.getServer().getOnlinePlayers()) {
 				if(player == null) continue;
 				if(!checkAuth(player)) {
 					String playerName = player.getName();
 					NLIData nli = notLoggedIn.get(playerName);
 					Location pos = nli.getLoc();
 					int secondsSinceLogin = (int) (System.currentTimeMillis() / 1000L) - nli.getLoggedSecs();
 
 					if(secondsSinceLogin > 60) {
 						player.kickPlayer("You took too long to log in!");
 						log("Player "+playerName+" took too long to log in");
 						continue;
 					}
 
 					if(secondsSinceLogin > 2) player.teleport(pos); // ignore teleports for 2 seconds to try and work around the Hacking? kick
 
 					if(msgLogin) msgPlayerLogin(player);
 				} else {
 					if(msgLogin && useReferrals) {
 						// There has to be a better place for this
 						LoggedInPlayer lp = playersLoggedIn.get(player.getName());
 						if(lp.isReferred()) {
 							int playTime = Math.round(lp.getOnlineTime() + (((int)(System.currentTimeMillis()/1000L) - lp.getLoggedInAt())));
 							if(playTime >= 4*60*60) {
 								ref.rewardPlayer(player, lp.getReferredBy());
 								markPlayerPaid(player);
 								lp.setReferred(false);
 								playersLoggedIn.put(player.getName(), lp);
 								log("[IceAuth Referrals] Rewarded player: "+player.getName() + ", referred by: " + lp.getReferredBy());
 							}
 						}
 					}
 				}
 			}
 		} catch(Exception ex) {
 			// we don't want the task to die
 			ex.printStackTrace();
 			log(ex.getMessage(), "EXCEPTION");
 		}
 		IceAuth.syncTaskTime += stopTiming();
 	}
 
 	// Data structures
 
 	public class NLIData {
 		private int loggedSecs;
 		private Location loc;
 		private ItemStack[] inventory;
 		private ItemStack[] armour;
 		private GameMode gameMode;	
 
 		public NLIData(ItemStack[] inventory, ItemStack[] armour) {
 			this.inventory = inventory;
 			this.armour = armour;
 		}
 
 		public NLIData(Location loc, int loggedSecs, ItemStack[] inventory, ItemStack[] armour, GameMode gameMode) {
 			this.inventory = inventory;
 			this.armour = armour;
 			this.loc = loc;
 			this.loggedSecs = loggedSecs;
 			this.gameMode = gameMode;
 		}
 
 		public Location getLoc() {
 			return this.loc;
 		}
 
 		public int getLoggedSecs() {
 			return this.loggedSecs;
 		}
 
 		public ItemStack[] getInventory() {
 			return inventory;
 		}
 
 		public ItemStack[] getArmour() {
 			return armour;
 		}
 
 		public GameMode getGameMode() {
 			return gameMode;
 		}
 	}
 
 	public class LoggedInPlayer {
 		private int loggedInAt;
 		private int onlineTime;
 		private boolean isReferred;
 		private String referredBy;
 
 		public LoggedInPlayer(int loggedInAt, int onlineTime, boolean isReferred, String referredBy) {
 			this.loggedInAt = loggedInAt;
 			this.onlineTime = onlineTime;
 			this.isReferred = isReferred;
 			this.referredBy = referredBy;
 		}
 
 		public int getLoggedInAt() {
 			return loggedInAt;
 		}
 
 		public int getOnlineTime() {
 			return onlineTime;
 		}
 
 		public boolean isReferred() {
 			return isReferred;
 		}
 
 		public String getReferredBy() {
 			return referredBy;
 		}
 
 		public void setReferred(boolean flag) {
 			isReferred = flag;
 		}
 	}
 
 	public class PlayerSyncThread implements Runnable {
 		@Override
 		public void run() {
 			threadRuns++;
 			if(threadRuns % 15 == 0) {
 				tpPlayers(true);
 			} else {
 				tpPlayers(false);
 			}
 		}
 	}
 }
