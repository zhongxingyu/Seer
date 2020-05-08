 package com.nuclearw.onlinesigns;
 
 import java.io.BufferedInputStream;
 import java.io.BufferedWriter;
 import java.io.EOFException;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Properties;
 import java.util.logging.Handler;
 import java.util.logging.LogRecord;
 import java.util.logging.Logger;
 
 import org.bukkit.block.Block;
 import org.bukkit.block.BlockState;
 import org.bukkit.block.Sign;
 import org.bukkit.entity.Player;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.java.JavaPlugin;
 
 import ru.tehkode.permissions.bukkit.PermissionsEx;
 
 public class OnlineSigns extends JavaPlugin {
 	static String mainDirectory = "plugins" + File.separator + "OnlineSigns";
 	static String boardFile = mainDirectory + File.separator + "boards";
 	static File versionFile = new File(mainDirectory + File.separator + "VERSION");
 	static File languageFile = new File(mainDirectory + File.separator + "lang");
 	static File configFile = new File(mainDirectory + File.separator + "config.yml");
 
 	public int maxplayers;
 
	Logger log = this.getLogger();
 
 	public HashMap<String, String[]> board = new HashMap<String, String[]>();
 	public HashMap<Player, Boolean> oneFish = new HashMap<Player, Boolean>(maxplayers);
 	public HashMap<Player, Block> twoFish = new HashMap<Player, Block>(maxplayers);
 
 	public static String[] language = new String[8];
 
 	Properties prop = new Properties();
 
 	private final OnlineSignsPlayerListener playerListener = new OnlineSignsPlayerListener(this);
 	private final OnlineSignsPluginListener pluginListener = new OnlineSignsPluginListener(this);
 	private final OnlineSignsPermissionsHandler permissionsHandler = new OnlineSignsPermissionsHandler(this);
 	private final OnlineSignsBlockListener blockListener = new OnlineSignsBlockListener(this);
 
 	private static boolean pexPrefix = false;
 
 	public void onEnable() {
 		new File(mainDirectory).mkdir();
 
 		if(!versionFile.exists()) {
 			updateVersion();
 		} else {
 			String vnum = readVersion();
 			if(vnum.equals("0.1")) updateVersion();
 		}
 
 		if(!languageFile.exists()) tryMakeLangFile();
 
 		tryLoadLangFile();
 
 		if(!prop.containsKey("Users-Online") || !prop.containsKey("begin-1") || !prop.containsKey("begin-2")
 				 || !prop.containsKey("slapped-enough") || !prop.containsKey("x-left") || !prop.containsKey("board-created")
 				 || !prop.containsKey("slap-more") || !prop.containsKey("no-permission")) {
 			this.log.severe("[OnlineSigns] Lang file not complete! Restoring to default!");
 			tryMakeLangFile();
 			tryLoadLangFile();
 		}
 
 		OnlineSigns.language[0] = prop.getProperty("Users-Online");
 		OnlineSigns.language[1] = prop.getProperty("begin-1");
 		OnlineSigns.language[2] = prop.getProperty("begin-2");
 		OnlineSigns.language[3] = prop.getProperty("slapped-enough");
 		OnlineSigns.language[4] = prop.getProperty("x-left");
 		OnlineSigns.language[5] = prop.getProperty("board-created");
 		OnlineSigns.language[6] = prop.getProperty("slap-more");
 		OnlineSigns.language[7] = prop.getProperty("no-permission");
 
 		loadConfig();
 
 		loadBoards();
 
 		OnlineSignsPermissionsHandler.initialize(this.getServer());
 
 		// Pretty sure this is an old workaround for an old issue in CB...
 		log.addHandler(new Handler() {
         	public void publish(LogRecord logRecord) {
         		String mystring = logRecord.getMessage();
         		if(mystring.contains(" lost connection: ")) {
         			String myarray[] = mystring.split(" ");
         			String DisconnectMessage = myarray[3];
         			if(DisconnectMessage.equals("disconnect.quitting")) return;
         			updateSigns();
         		}
         	}
         	public void flush() {}
         	public void close() {
         	}
         });
 
 		this.maxplayers = getServer().getMaxPlayers();
 		PluginManager pluginManager = getServer().getPluginManager();
 		pluginManager.registerEvents(playerListener, this);
 		pluginManager.registerEvents(pluginListener, this);
         pluginManager.registerEvents(blockListener, this);
 
 		log.info("[OnlineSigns] version " + this.getDescription().getVersion() + " loaded.");
 	}
 
 	public void onDisable() {
 		saveBoards();
 		log.info("[OnlineSigns] version " + this.getDescription().getVersion() + " loaded.");
 	}
 	
 	public boolean hasPermission(Player player, String permission) {
 		return permissionsHandler.hasPermission(player, permission);
 	}
 
 	public String blockToString(Block block) {
 		String string = block.getWorld().getName();
 		string += ":" + Integer.toString(block.getX());
 		string += ":" + Integer.toString(block.getY());
 		string += ":" + Integer.toString(block.getZ());
 		return string;
 	}
 
 	public Block stringToBlock(String string) {
 		// World:X:Y:Z
 		if(!string.contains(":")) return null;
 		String[] split = string.split(":");
 		if(split.length != 4) return null;
 		Block block = getServer().getWorld(split[0]).getBlockAt(Integer.parseInt(split[1]), Integer.parseInt(split[2]), Integer.parseInt(split[3]));
 		return block;
 	}
 
 	public void addToBoard(String blueFish, String blockToString) {
 		if(!board.containsKey(blueFish)) board.put(blueFish, null);
 		String[] strings = board.get(blueFish);
 		if(strings == null) {
 			ArrayList<String> newBlocksList = new ArrayList<String>();
 			newBlocksList.add(blockToString);
 			String[] newStrings = new String[newBlocksList.size()];
 			newStrings = newBlocksList.toArray(newStrings);
 			board.put(blueFish, newStrings);
 		} else {
 			ArrayList<String> newStringsList = new ArrayList<String>(Arrays.asList(strings));
 			newStringsList.add(blockToString);
 			String[] newStrings = new String[newStringsList.size()];
 			newStrings = newStringsList.toArray(newStrings);
 			board.put(blueFish, newStrings);
 		}
 		saveBoards();
 	}
 
 	public void writeOnSign(Block block, String line0, String line1, String line2, String line3) {
 		if(block.getTypeId() != 63 && block.getTypeId() != 68) return;
 		final BlockState bState = block.getState();
 		final String fline0 = (line0 == null) ? "" : line0;
 		final String fline1 = (line1 == null) ? "" : line1;
 		final String fline2 = (line2 == null) ? "" : line2;
 		final String fline3 = (line3 == null) ? "" : line3;
 		getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
 			public void run() {
                 Sign sign = (Sign) bState;
                 sign.setLine(0, fline0);
                 sign.setLine(1, fline1);
                 sign.setLine(2, fline2);
                 sign.setLine(3, fline3);
                 sign.update();
 			}
 		}, 1L);
 	}
 
 	public void updateSigns() {
 		getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
 			public void run() {
 				Iterator<String> i = board.keySet().iterator();
 				while(i.hasNext()) {
 					String mainKey = i.next();
 					Block mainBlock = stringToBlock(mainKey);
 					writeOnSign(mainBlock, OnlineSigns.language[0], Integer.toString(getServer().getOnlinePlayers().length)+"/"+Integer.toString(maxplayers), null, null);
 					int j = 0;
 					Player[] onlinePlayers = getServer().getOnlinePlayers();
 					for(String s : board.get(mainKey)) {
 						Block b = stringToBlock(s);
 						if(!b.getWorld().isChunkLoaded(b.getChunk())) b.getWorld().loadChunk(b.getChunk());
 						String[] lines = new String[4];
 						int k = 0;
 						while(k < 4 && j < onlinePlayers.length) {
 							if(pexPrefix) {
 								String playerName = onlinePlayers[j].getName();
 								String colorizedName = playerName;
 								if(playerName.length()<=14) {
 									String pexPrefix = PermissionsEx.getPermissionManager().getUser(playerName).getPrefix();
 									if(pexPrefix.startsWith("&") && pexPrefix.length() >= 2) {
 										String pexPrefixColor = pexPrefix.substring(0, 2);
 										colorizedName = colorize(pexPrefixColor+playerName);
 									}
 								}
 
 								lines[k] = colorizedName;
 							} else {
 								lines[k] = onlinePlayers[j].getName();
 							}
 
 							j++;
 							k++;
 						}
 						writeOnSign(b, lines[0], lines[1], lines[2], lines[3]);
 					}
 				}
 			}
 		}, 1L);
 	}
 
 	@SuppressWarnings("unchecked")
 	public void loadBoards() {
 		if(new File(boardFile).exists()) {
 			try {
 				ObjectInputStream obj = new ObjectInputStream(new FileInputStream(boardFile));
 				this.board = (HashMap<String, String[]>)obj.readObject();
 			} catch (FileNotFoundException e) { e.printStackTrace();
 			} catch (EOFException e) { log.info("[OnlineSigns] boards file empty.");
 			} catch (IOException e) { e.printStackTrace();
 			} catch (ClassNotFoundException e) { e.printStackTrace(); }
     	}
 	}
 
 	public void saveBoards() {
 		try {
 			new File(boardFile).createNewFile();
 			ObjectOutputStream obj = new ObjectOutputStream(new FileOutputStream(boardFile));
 			obj.writeObject(this.board);
 			obj.close();
 		} catch (FileNotFoundException e) { e.printStackTrace();
 		} catch (IOException e) { e.printStackTrace(); }
 	}
 
 	private void loadConfig() {
 		if(!configFile.exists()) {
 			this.saveDefaultConfig();
 		}
 
 		pexPrefix = getConfig().getBoolean("PEX_Prefix", false);
 	}
 
 	public void updateVersion() {
 		try {
 			versionFile.createNewFile();
 			BufferedWriter vout = new BufferedWriter(new FileWriter(versionFile));
 			vout.write(this.getDescription().getVersion());
 			vout.close();
 		} catch (IOException ex) {
 			ex.printStackTrace();
 		} catch (SecurityException ex) {
 			ex.printStackTrace();
 		}
 	}
 
 	public String readVersion() {
 		byte[] buffer = new byte[(int) versionFile.length()];
 		BufferedInputStream f = null;
 		try {
 			f = new BufferedInputStream(new FileInputStream(versionFile));
 			f.read(buffer);
 		} catch (FileNotFoundException ex) {
 			ex.printStackTrace();
 		} catch (IOException ex) {
 			ex.printStackTrace();
 		} finally {
 			if (f != null) try { f.close(); } catch (IOException ignored) { }
 		}
 
 		return new String(buffer);
 	}
 
 	public void tryLoadLangFile() {
 		FileInputStream langin;
 		try {
 			langin = new FileInputStream(languageFile);
 			this.prop.load(langin);
 			langin.close();
 		} catch (FileNotFoundException ex) {
 			ex.printStackTrace();
 		} catch (IOException ex) {
 			ex.printStackTrace();
 		}
 	}
 
 	public void tryMakeLangFile() {
 		try {
 			languageFile.createNewFile();
 			FileOutputStream out = new FileOutputStream(languageFile);
 			this.prop.put("Users-Online", "Users Online");
 			this.prop.put("begin-1", "You have begun construction of an OnlineSigns board.");
 			this.prop.put("begin-2", "Slap more signs to add them to this board.");
 			this.prop.put("slapped-enough", "You have already slapped enough signs.  Slap the first sign again to finish.");
 			this.prop.put("x-left", "<NUMBER> signs left to slap.");
 			this.prop.put("board-created", "OnlineSigns board created");
 			this.prop.put("slap-more", "You haven't slapped enough signs");
 			this.prop.put("no-permission", "You do not have permission to make an OnlineSign.");
 			this.prop.store(out, "Loaclization. Users-Online must be shorter than 16 characters to fit on the sign.");
 			out.flush();
 			out.close();
 			this.prop.clear();
 		} catch (IOException ex) {
 			ex.printStackTrace();
 		}
 	}
 
 	public String colorize(String string) {
 		if(string == null){
 			return "";
 		}
 
 		return string.replaceAll("&([a-z0-9])", "\u00A7$1");
 	}
 }
