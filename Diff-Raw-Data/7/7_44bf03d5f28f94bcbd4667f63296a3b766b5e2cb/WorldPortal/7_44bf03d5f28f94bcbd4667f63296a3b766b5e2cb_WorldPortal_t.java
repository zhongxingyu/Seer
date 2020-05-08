 package me.timvisee.WorldPortal;
 
 import java.io.BufferedInputStream;
 import java.io.BufferedWriter;
 import java.io.DataInputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.List;
 import java.util.logging.Logger;
 
 import me.timvisee.WorldPortal.WorldPortalPlayerListener;
 
 import com.nijiko.permissions.PermissionHandler;
 import com.nijikokun.bukkit.Permissions.Permissions;
 
 import org.anjocaido.groupmanager.GroupManager;
 import org.anjocaido.groupmanager.permissions.AnjoPermissionsHandler;
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 import org.bukkit.World;
 import org.bukkit.World.Environment;
 import org.bukkit.WorldCreator;
 import org.bukkit.block.Block;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.configuration.file.YamlConfiguration;
 import org.bukkit.plugin.Plugin;
 import org.bukkit.plugin.PluginDescriptionFile;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.java.JavaPlugin;
 import org.bukkit.util.Vector;
 import org.bukkit.entity.Player;
 import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
 
 import ru.tehkode.permissions.PermissionManager;
 import ru.tehkode.permissions.PermissionUser;
 import ru.tehkode.permissions.bukkit.PermissionsEx;
 
 public class WorldPortal extends JavaPlugin implements CommandExecutor {
 	public static Logger log = Logger.getLogger("Minecraft");
 	
 	private final WorldPortalBlockListener blockListener = new WorldPortalBlockListener(this);
 	private final WorldPortalEntityListener entityListener = new WorldPortalEntityListener(this);
 	private final WorldPortalPlayerListener playerListener = new WorldPortalPlayerListener(this);
 	public final WorldPortalCreatePortal createPortal = new WorldPortalCreatePortal(this);
 
 	private File worldPortalsFile = new File("plugins/World Portal/World Portals.list");
 	private File worldPortalConfigFile = new File("plugins/World Portal/config.yml");
 	private File worldPortalLangFile = new File("plugins/World Portal/messages.yml");
 	
 	public final List<Player> wpRemoveUsers = new ArrayList<Player>();
 	
 	/*
 	 * 0 = none
 	 * 1 = PermissionsEx
 	 * 2 = PermissionsBukkit
 	 * 3 = bPermissions
 	 * 4 = Essentials Group Manager
 	 * 5 = Permissions
 	 */
 	private int permissionsSystem = 0;
 	private PermissionManager pexPermissions;
 	private PermissionHandler defaultPermsissions;
 	private GroupManager groupManagerPermissions;
 	
 	List<String> worldPortals = new ArrayList<String>();
 	List<String> movedTooQuicklyIgnoreList = new ArrayList<String>();
 	
 	public final HashMap<String, Location> lastPlayerTeleportLocation = new HashMap<String, Location>();
 
 	public void onEnable() {
 		// Setup costum files and folders
 		worldPortalsFile = new File(getDataFolder() + "/" + getConfig().getString("WorldPortalsListFile", "World Portals.list"));
 		worldPortalConfigFile = new File(getDataFolder() + "/" + "config.yml");
 		worldPortalLangFile = new File(getDataFolder() + "/" + "messages.yml");
 		
 		// Check if all the config file exists
 		try {
 			checkConigFilesExist();
 	    } catch (Exception e) {
 	        e.printStackTrace();
 	    }
 		
 		PluginManager pm = getServer().getPluginManager();
 		pm.registerEvents(this.blockListener, this);
 		pm.registerEvents(this.entityListener, this);
 		pm.registerEvents(this.playerListener, this);
 		
 		// Setup the permission system
 		setupPermissions();
 		
 		// Load all the world portals in the local list
 		loadWorldPortals();
 		
 		PluginDescriptionFile pdfFile = getDescription();
 		log.info("[World Portal] World Portal v" + pdfFile.getVersion() + " Started");
 	}
 
 	public void onDisable() {
 		// Save all the world portals to a external file (don't save it anymore this could couse an empty file bug)
 		//saveWorldPortals();
 		
 		PluginDescriptionFile pdfFile = getDescription();
 		log.info("[World Portal] World Portal v" + pdfFile.getVersion() + " Disabled");
 	}
 	
 	private void setupPermissions() {
 		// Reset permissions
 		permissionsSystem = 0;
 		
 		// Check PermissionsEx system
 		Plugin testPex = this.getServer().getPluginManager().getPlugin("PermissionsEx");
 		if(testPex != null) {
 			pexPermissions = PermissionsEx.getPermissionManager();
 			if(pexPermissions != null) {
 				permissionsSystem = 1;
 				
 				System.out.println("[World Portal] Hooked into PermissionsEx!");
 				return;
 			}
 		}
 		
 		// Check PermissionsBukkit system
 		Plugin testBukkitPerms = this.getServer().getPluginManager().getPlugin("PermissionsBukkit");
 		if(testBukkitPerms != null) {
 			permissionsSystem = 2;
 			System.out.println("[World Portal] Hooked into PermissionsBukkit!");
 			return;
 		}
 		
 		// Check bPermissions system
 		/*
 		 * Not available yet!
 		 */
 		
 		// Check Essentials Group Manager system
 		final PluginManager pluginManager = getServer().getPluginManager();
 		final Plugin GMplugin = pluginManager.getPlugin("GroupManager");
 		if (GMplugin != null && GMplugin.isEnabled())
 		{
 			permissionsSystem = 4;
 			groupManagerPermissions = (GroupManager)GMplugin;
             System.out.println("[World Portal] Hooked into Essentials Group Manager!");
             return;
 		}
 		
 		// Check Permissions system
 	    Plugin testPerms = this.getServer().getPluginManager().getPlugin("Permissions");
 	    if (this.defaultPermsissions == null) {
 	        if (testPerms != null) {
 	        	permissionsSystem = 5;
 	            this.defaultPermsissions = ((Permissions) testPerms).getHandler();
 	            System.out.println("[World Portal] Hooked into Permissions!");
 	            return;
 	        }
 	    }
 	    
 	    // None of the permissions systems worked >:c.
 	    permissionsSystem = 0;
 	    System.out.println("[World Portal] No Permissions system found! Permissions disabled!");
 	}
 	
 	public boolean usePermissions() {
 		if(permissionsSystem == 0) {
 			return false;
 		}
 		return true;
 	}
 	
 	public int getPermissionsSystem() {
 		return permissionsSystem;
 	}
 	
 	public boolean hasPermission(Player player, String permissionNode) {
 		if(usePermissions() == false) {
 			return false;
 		}
 		
 		// Using PermissionsEx
 		if(getPermissionsSystem() == 1) {
 			PermissionUser user  = PermissionsEx.getUser(player);
 			return user.has(permissionNode);
 		}
 		
 		// Using PermissionsBukkit
 		if(getPermissionsSystem() == 2) {
 			return player.hasPermission(permissionNode);
 		}
 		
 		// Using bPemissions
 		// Available soon!
 		
 		// Using Essentials Group Manager
 		if(getPermissionsSystem() == 4) {
 			final AnjoPermissionsHandler handler = groupManagerPermissions.getWorldsHolder().getWorldPermissions(player);
 			if (handler == null)
 			{
 				return false;
 			}
 			return handler.has(player, permissionNode);
 		}
 		
 		// Using Permissions
 		if(getPermissionsSystem() == 5) {
 			return this.defaultPermsissions.has(player, permissionNode);
 		}
 
 		return false;
 	}
 
 	public boolean canUsePortal(Player player) {
 	    if (usePermissions()) {
 	        return hasPermission(player, "worldportal.use");
 	    }
 	    return true;
 	}
 	public boolean canUseWPCreate(Player player) {
 	    if (usePermissions()) {
 	        return hasPermission(player, "worldportal.create");
 	    }
 	    return player.isOp();
 	}
 	public boolean canUseWPRemove(Player player) {
 	    if (usePermissions()) {
 	        return hasPermission(player, "worldportal.remove");
 	    }
 	    return player.isOp();
 	}
 	public boolean canUseWPTeleport(Player player) {
 	    if (usePermissions()) {
 	        return hasPermission(player, "worldportal.teleport");
 	    }
 	    return player.isOp();
 	}
 	public boolean canUseWPSave(Player player) {
 	    if (usePermissions()) {
 	        return hasPermission(player, "worldportal.save");
 	    }
 	    return player.isOp();
 	}
 	public boolean canUseWPReload(Player player) {
 	    if (usePermissions()) {
 	        return hasPermission(player, "worldportal.reload");
 	    }
 	    return player.isOp();
 	}
 	
 	public void saveWorldPortals() {
 		// Save the Array(List) worldPortals in a String, line by line to a file
 		log.info("[World Portal] Saving World Portals...");
 		try {
 			BufferedWriter out = new BufferedWriter(new FileWriter(worldPortalsFile));
 			//ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(worldPortalsFile));
 			
 			for(int i = 0; i < worldPortals.size(); i++) {
 				if(i != 0) { out.newLine(); }
 				out.write(worldPortals.get(i));
 			}
 			out.close();
 
 			log.info("[World Portal] World Portals saved");
 		} catch(IOException e) {
 	      System.out.println(e);
 			log.info("[World Portal] Error by saving World Portals");
 		}
 	}
 	
 	@SuppressWarnings("deprecation")
 	public void loadWorldPortals() {
 		// Load the Array(List) worldPortals in a
 	    if(worldPortalsFile.exists()) {
 			log.info("[World Portal] Loading World Portals...");
 	    	File file = worldPortalsFile;
 	        FileInputStream fis = null;
 	        BufferedInputStream bis = null;
 	        DataInputStream dis = null;
 
 	        try {
 				fis = new FileInputStream(file);
 				
 				// Here BufferedInputStream is added for fast reading.
 				bis = new BufferedInputStream(fis);
 				dis = new DataInputStream(bis);
 				
 				// Make the Array(List) WorldPortals empty
 				worldPortals.clear();
 				
 				// dis.available() returns 0 if the file does not have more lines.
 				while (dis.available() != 0) {
 					worldPortals.add(dis.readLine());
 				}
 				
 				fis.close();
 				bis.close();
 				dis.close();
 				
 				log.info("[World Portal] World Portals loaded");
 	        } catch (FileNotFoundException e) {
 	        	e.printStackTrace();
 	    		log.info("[World Portal] Error by loading World Portals");
 	        } catch (IOException e) {
 	        	e.printStackTrace();
 	    		log.info("[World Portal] Error by loading World Portals");
 	        }
 	    } else {
     		log.info("[World Portal] File '" + worldPortalsFile.getPath() + "' not found");
 	    }
 	}
 	
 	public void checkConigFilesExist() throws Exception {
 		if(!getDataFolder().exists()) {
 			log.info("[WorldPortal] Creating new World Portal folder");
 			getDataFolder().mkdirs();
 		}
 		if(!worldPortalConfigFile.exists()) {
 			log.info("[WorldPortal] Generating new config file");
 			copy(getResource("res/World Portal/config.yml"), worldPortalConfigFile);
 		}
 		if(!worldPortalsFile.exists()) {
 			log.info("[WorldPortal] Generating new portals file");
 			copy(getResource("res/World Portal/World Portals.list"), worldPortalsFile);
 		}
 		if(!worldPortalLangFile.exists()) {
 			log.info("[WorldPortal] Generating new language file");
 			copy(getResource("res/World Portal/messages.yml"), worldPortalLangFile);
 		}
 	}
 	
 	private void copy(InputStream in, File file) {
 	    try {
 	        OutputStream out = new FileOutputStream(file);
 	        byte[] buf = new byte[1024];
 	        int len;
 	        while((len=in.read(buf))>0){
 	            out.write(buf,0,len);
 	        }
 	        out.close();
 	        in.close();
 	    } catch (Exception e) {
 	        e.printStackTrace();
 	    }
 	}
 	
 	
 	
 	
 	
 	
 	
 	// Function to get a configuration file (.yml) from a file path
 	public FileConfiguration getConfigurationFromPath(String filePath, boolean insideDataFolder) {
 		if(insideDataFolder) {
 			File file = new File(getDataFolder(), filePath);
 			return getConfigFromPath(file);
 		} else {
 			File file = new File(filePath);
 			return getConfigFromPath(file);
 		}
 	}
 	
 	// Fuctnion to get a constum configuration file
 	public FileConfiguration getConfigFromPath(String filePath, boolean insideDataFolder) {
 		if(insideDataFolder) {
 			File file = new File(getDataFolder(), filePath);
 			return getConfigFromPath(file);
 		} else {
 			File file = new File(filePath);
 			return getConfigFromPath(file);
 		}
 	}
 	
 	// Function to get a costum configuration file
 	public FileConfiguration getConfigFromPath(File file) {
 		FileConfiguration c;
 		
 		if (file == null) {
 		    return null;
 		}
 
 	    c = YamlConfiguration.loadConfiguration(file);
 	    
 	    return c;
 	}
 	
 	public String convertChatColors(String input)
     {        
 		// Convert the color strings in a string to colors
         return input
                 .replace("&0", ChatColor.BLACK.toString())
                 .replace("&1", ChatColor.DARK_BLUE.toString())
                 .replace("&2", ChatColor.DARK_GREEN.toString())
                 .replace("&3", ChatColor.DARK_AQUA.toString())
                 .replace("&4", ChatColor.DARK_RED.toString())
                 .replace("&5", ChatColor.DARK_PURPLE.toString())
                 .replace("&6", ChatColor.GOLD.toString())
                 .replace("&7", ChatColor.GRAY.toString())
                 .replace("&8", ChatColor.DARK_GRAY.toString())
                 .replace("&9", ChatColor.BLUE.toString())
                 .replace("&a", ChatColor.GREEN.toString()).replace("&A", ChatColor.GREEN.toString())
                 .replace("&b", ChatColor.AQUA.toString()).replace("&B", ChatColor.AQUA.toString())
                 .replace("&c", ChatColor.RED.toString()).replace("&C", ChatColor.RED.toString())
                 .replace("&d", ChatColor.LIGHT_PURPLE.toString()).replace("&D", ChatColor.LIGHT_PURPLE.toString())
                 .replace("&e", ChatColor.YELLOW.toString()).replace("&E", ChatColor.YELLOW.toString())
                 .replace("&f", ChatColor.WHITE.toString()).replace("&F", ChatColor.WHITE.toString());        
     }
 	
 	public String getMessage(String messageId, String defaultMessage) {
 		return convertChatColors(getConfigurationFromPath("messages.yml", true).getString(messageId, defaultMessage));
 	}
 	
 	public List<String> getMessageList(String messageId, List<String> defaultMessages) {
 		List<String> messages = getConfigurationFromPath("messages.yml", true).getStringList(messageId);
 		List<String> messagesOutput = new ArrayList<String>();
 		
 		for(int i = 0; i < messages.size(); i++) {
 			messagesOutput.add(convertChatColors(messages.get(i).toString()));
 		}
 		
 		return messagesOutput;
 	}
 	
 	public void sendMessageList(Player player, String messageId, List<String> defaultMessages) {
 		List<String> messages = getMessageList(messageId, defaultMessages);
 		for(int i = 0; i < messages.size(); i++) {
 			player.sendMessage(convertChatColors(messages.get(i).toString()));
 		}
 	}
 	
 	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
 		if (commandLabel.equalsIgnoreCase("worldportal") || commandLabel.equalsIgnoreCase("wp")) {
 			if(args.length >= 1) {
 				if(args[0].equalsIgnoreCase("reload")) {
 					if(sender instanceof Player) {
 						if(canUseWPReload((Player) sender) ) {
 							saveWorldPortals();
 							loadWorldPortals();
 							sender.sendMessage(getMessage("worldPortalListReloaded", "&e[World Portal] &aSuccesfully reloaded!"));
 							return true;
 						} else {
 							sender.sendMessage(getMessage("noReloadPermission", "&e[World Portal] &4You don''t have permisson"));
 							return true;
 						}
 					} else {
 						saveWorldPortals();
 						loadWorldPortals();
 						sender.sendMessage(ChatColor.YELLOW + "[World Portal] Succesfully reloaded!");
 						return true;
 					}
 					
 				} else if(args[0].equalsIgnoreCase("save")) {
 					if(sender instanceof Player) {
 						if(canUseWPSave((Player) sender) ) {
 							saveWorldPortals();
 							sender.sendMessage(getMessage("worldPortalListSaved", "&e[World Portal] &aSuccesfully saved!"));
 							return true;
 						} else {
 							sender.sendMessage(getMessage("noSavePermission", "&e[World Portal] &4You don''t have permisson"));
 							return true;
 						}
 					} else {
 						saveWorldPortals();
 						sender.sendMessage(ChatColor.YELLOW + "[World Portal] Succesfully saved!");
 						return true;
 					}
 					
 				} else if(args[0].equalsIgnoreCase("create")) {
 					if(sender instanceof Player) {
 						// Enable creation mode
 						if (canUseWPCreate((Player) sender)) {
 							createPortal.toggleWPUsers((Player) sender);
 						} else {
 							sender.sendMessage(getMessage("noCreatePermission", "&e[World Portal] &4You don''t have permisson"));
 						}
 					} else {
 						sender.sendMessage(ChatColor.YELLOW + "[World Portal] You can only make World Portals in-game");
 					}
 					return true;
 				} else if(args[0].equalsIgnoreCase("createstop")) {
 					// Disable the creation mode
 					if(createPortal.isPlayerInCreationMode((Player) sender)) {
 						createPortal.toggleWPUsers((Player) sender);
 					}
 					return true;
 				} else if(args[0].equalsIgnoreCase("remove")) {
 					if(sender instanceof Player) {
 						// Enable creation mode
 						if (canUseWPRemove((Player) sender)) {
 							toggleWPRemoveUsers((Player) sender);
 						} else {
 							sender.sendMessage(getMessage("noRemovePermission", "&e[World Portal] &4You don''t have permisson"));
 						}
 					} else {
 						sender.sendMessage(ChatColor.YELLOW + "[World Portal] You can only remove World Portals in-game");
 					}
 					return true;
 				} else if(args[0].equalsIgnoreCase("removestop")) {
 					// Disable the creation mode
 					if(WPRemoveUsersEnabled((Player) sender)) {
 						toggleWPRemoveUsers((Player) sender);
 					}
 					return true;
 				} else if(args[0].equalsIgnoreCase("tp") || args[0].equalsIgnoreCase("teleport")) {
					
					// Make sure the command has at least 2 or more arguments
					if(args.length <= 1) {
						sender.sendMessage(ChatColor.YELLOW + "[World Portal] Invalid command arguments!");
						return true;
					}
					
 					if(sender instanceof Player) {
 						// Check if the player can use this command
 						if(canUseWPTeleport((Player) sender)) {
 							// Check if the world where the player want to teleport to exsists
 							if(worldExists(args[1].toString())) {
 								// Load the world if it isn't loaded
 								if(!isWorldLoaded(args[1].toString())) {
 									loadWorld(args[1].toString());
 								}
 								
 								if(args.length == 2) {
 									// Teleport to the other world
 									Location spawnLocation = getServer().getWorld(args[1].toString()).getSpawnLocation();
 									teleportPlayer((Player) sender, getFixedSpawnLocation(spawnLocation));
 								} else if(args.length == 3) {
 									if(args[2].equalsIgnoreCase("spawn")) {
 										Location spawnLocation = getServer().getWorld(args[1].toString()).getSpawnLocation();
 										teleportPlayer((Player) sender, getFixedSpawnLocation(spawnLocation));
 										
 									} else {
 										String[] defaultMessages = {"&e[World Portal] &4Unknown command values! &eUse:", "&e[World Portal]   &f/wp tp <world>",
 												"&e[World Portal]   &f/wp tp <world> <x> <z>", "&f/wp tp <world> <x> <y> <z>"};
 										sendMessageList((Player) sender, "unknownTeleportCommand", Arrays.asList(defaultMessages));
 									}
 								} else if(args.length == 4) {
 									Location location = getServer().getWorld(args[1].toString()).getSpawnLocation();
 									if(stringIsInt(args[2].toString()) || stringIsInt(args[3].toString())) {
 										location.setX(Integer.parseInt(args[2].toString()));
 										location.setY(0);
 										location.setZ(Integer.parseInt(args[3].toString()));
 										location.setY(getServer().getWorld(args[1].toString()).getHighestBlockYAt(location.getBlockX(), location.getBlockZ()));
 										teleportPlayer((Player) sender, getFixedSpawnLocation(location));
 										
 									} else {
 										String[] defaultMessages = {"&e[World Portal] &4Unknown command values! &eUse:", "&e[World Portal]   &f/wp tp <world>",
 												"&e[World Portal]   &f/wp tp <world> <x> <z>", "&f/wp tp <world> <x> <y> <z>"};
 										sendMessageList((Player) sender, "unknownTeleportCommand", Arrays.asList(defaultMessages));
 									}
 								} else if(args.length == 5) {
 									Location location = getServer().getWorld(args[1].toString()).getSpawnLocation();
 									if(stringIsInt(args[2].toString()) || stringIsInt(args[3].toString()) || stringIsInt(args[4].toString())) {
 										location.setX(Integer.parseInt(args[2].toString()));
 										location.setY(Integer.parseInt(args[3].toString()));
 										location.setZ(Integer.parseInt(args[4].toString()));
 										teleportPlayer((Player) sender, location);
 										
 									} else {
 										String[] defaultMessages = {"&e[World Portal] &4Unknown command values! &eUse:", "&e[World Portal]   &f/wp tp <world>",
 												"&e[World Portal]   &f/wp tp <world> <x> <z>", "&f/wp tp <world> <x> <y> <z>"};
 										sendMessageList((Player) sender, "unknownTeleportCommand", Arrays.asList(defaultMessages));
 									}
 								}
 							} else {
 								String message = getMessage("tpToWorldMessage", "&e[World Portal] &4The world ''&f%worldname&4'' doesn't exists!");
 								sender.sendMessage(message.replaceAll("%worldname%", args[1].toString()));
 								return true;
 							}
 						} else {
 							sender.sendMessage(getMessage("noTeleportCommandPermission", "&e[World Portal] &4You don''t have permisson"));
 						}						
 					} else {
 						sender.sendMessage(ChatColor.YELLOW + "[World Portal] You can only use this in-game");
 					}
 					return true;
 				} else if(args[0].equalsIgnoreCase("info")) {
 					if(sender instanceof Player) {
 						if(createPortal.isPlayerInCreationMode((Player) sender)) {
 							getMessage("infoCreationModeEnabled", "&e[World Portal] Creation-mode &aenabled");
 						} else {
 							getMessage("infoCreationModeDisabled", "&e[World Portal] Creation-mode &4disabled");
 						}
 						if(WPRemoveUsersEnabled((Player) sender)) {
 							getMessage("infoRemoveModeEnabled", "&e[World Portal] Remove-mode &aenabled");
 						} else {
 							sender.sendMessage(getMessage("infoRemoveModeDisabled", "&e[World Portal] Remove-mode &4disabled"));
 						}
 					} else {
 						sender.sendMessage(ChatColor.YELLOW + "[World Portal] You can only view your info in-game");
 					}
 					return true;
 				} else if(args[0].equalsIgnoreCase("version") || args[0].equalsIgnoreCase("ver")) {
 					PluginDescriptionFile pdfFile = getDescription();
 					sender.sendMessage(ChatColor.YELLOW + "This server is running World Portal v" + pdfFile.getVersion());
 					sender.sendMessage(ChatColor.YELLOW + "World Portal is made my Tim Visee - timvisee.com");
 					return true;
 				}
 			}
 			
 			sender.sendMessage(getMessage("unknownCommandMessage", "&e[World Portal] &4Unknown command! &eTry &f/wp create"));
 			return true;
 		}
 		return false;
 	}
 	
 	public void addMovedTooQuicklyIgnoreListPlayer(Player player) {
 		addMovedTooQuicklyIgnoreListPlayer(player.getName());
 	}
 	
 	public void addMovedTooQuicklyIgnoreListPlayer(String player) {
 		if(movedTooQuicklyIgnoreList.contains(player)) {
 			movedTooQuicklyIgnoreList.remove(player);
 		}
 		
 		movedTooQuicklyIgnoreList.add(player);
 		
 		getServer().getScheduler().scheduleAsyncDelayedTask(this, new Runnable() { public void run() {
 			if(movedTooQuicklyIgnoreList.size() >= 1) {
 				movedTooQuicklyIgnoreList.remove(0);
 			}
 		} }, 20*10);
 	}
 	
 	public void addLastTeleportPlayerLocation(Player player, Location location) {
 		addLastTeleportPlayerLocation(player.getName(), location);
 	}
 	
 	public void addLastTeleportPlayerLocation(String player, Location location) {
 		if(lastPlayerTeleportLocation.containsKey(player)) {
 			lastPlayerTeleportLocation.remove(player);
 		}
 		
 		lastPlayerTeleportLocation.put(player, location);
 	}
 	
 	public Location getLastTeleportPlayerLocation(String player) {
 		if(lastPlayerTeleportLocation.containsKey(player)) {
 			return lastPlayerTeleportLocation.get(player);
 		}
 		return getServer().getPlayer(player).getLocation();
 	}
 	
 	
 	
 	public void toggleWPRemoveUsers(Player player) {
 		if (wpRemoveUsers.contains(player)) {
 			// Disable creation mode
 			wpRemoveUsers.remove(player);
 			player.sendMessage(ChatColor.YELLOW + "[World Portal] Remove-mode " + ChatColor.DARK_RED + "disabled");
 		} else {
 			// Enable creation mode
 			wpRemoveUsers.add(player);
 			String[] defaultMessages = {"&e[World Portal] Remove-mode &aenabled", "&e[World Portal] Right-click on a World Portal to remove it",
 					"&e[World Portal] Remove a &fSign&e, &fLever&e, &fPressureplate&e or a &fbutton&e", "&e[World Portal] Use &f/wp removestop &eto disable the remove-mode"};
 			sendMessageList(player, "removeModeEnabled", Arrays.asList(defaultMessages));
 		}
 	}
 	
 	public boolean WPRemoveUsersEnabled(Player player) {
 		return wpRemoveUsers.contains(player);
 	}
 	
 	public boolean isInt(String string) {
 		return stringIsInt(string);
 	}
 	
 	public boolean stringIsInt(String string) {
         try {
             @SuppressWarnings("unused")
 			int i = Integer.parseInt(string);
         } catch (NumberFormatException ex) {
             return false;
         }
         return true;
     }
 	
 	public void createWorld(String worldName, Environment environment) {
 		createWorld(worldName, environment, true);
 	}
 	public void createWorld(String worldName, Environment environment, boolean broadcastMessage) {
 		if(!worldExists(worldName)) {
 			if(getConfig().getBoolean("broadcastMessageOnWorldGeneration", true)) {
 				//getServer().broadcastMessage(getMessage("worldGenerationBroadcastMessage", "&d[World Portal] Generating a new world, there's probably some lag for a little while"));
 				//getServer().broadcastMessage(ChatColor.LIGHT_PURPLE + "[World Portal] Generating a new world, there's probably some lag for a little while");
 			}
 			
 			/*//getServer().createWorld(worldName, environment);
 			
 			WorldCreator newWorld = new WorldCreator(worldName);
 			newWorld.environment(environment);
 			newWorld.generateStructures(true);
 			getServer().createWorld(newWorld);*/
 			(new WorldCreator(worldName)).environment(environment).createWorld();
 			
 			if(getConfig().getBoolean("broadcastMessageOnWorldGeneration", true)) {
 				//getServer().broadcastMessage(getMessage("worldGenerationBroadcastMessageDone", "&d[World Portal] World generation complete!"));
 				//getServer().broadcastMessage(ChatColor.LIGHT_PURPLE + "[World Portal] World generation complete!");
 			}
 		}
 	}
 	
 	public boolean worldExists(World world) {
 		return worldExists(world.getName());
 	}
 	
 	public boolean worldExists(String worldName) {
 		File worldLevelFile = new File(worldName + "/level.dat");
 		return worldLevelFile.exists();
 	}
 	
 	public boolean isWorldLoaded(World world) {
 		return isWorldLoaded(world.getName());
 	}
 	public boolean isWorldLoaded(String worldName) {
 		@SuppressWarnings({ "unchecked", "rawtypes" })
 		List<World> worlds = new ArrayList();
 		@SuppressWarnings({ "unchecked", "rawtypes" })
 		List<String> worldNames = new ArrayList();
 		
 		worlds.addAll(getServer().getWorlds());
 		for(int i=0; i < worlds.size(); i++) {
 			worldNames.add(worlds.get(i).getName());
 		}
 		if(worldNames.contains(worldName)) {
 			return true;
 		}
 		// No world with this name, return false
 		return false;
 	}
 	
 	public void loadWorld(World world) {
 		loadWorld(world.getName());
 	}
 	public void loadWorld(String worldName) {
 		if(worldExists(worldName)) {
 			if(getConfig().getBoolean("broadcastMessageOnWorldLoad", true)) {
 				getServer().broadcastMessage(getMessage("worldLoadBroadcastMessage", "&d[World Portal] Loading world, there's probably some lag for a little while"));
 			}
 			
 			WorldCreator newWorld = new WorldCreator(worldName);
 			newWorld.environment(Environment.NORMAL);
 			newWorld.generateStructures(true);
 			getServer().createWorld(newWorld);
 			
 			if(getConfig().getBoolean("broadcastMessageOnWorldLoad", true)) {
 				getServer().broadcastMessage(getMessage("worldLoadBroadcastMessageDone", "&d[World Portal] World succesfully loaded!"));
 			}
 		}
 	}
 	
 	public boolean checkIfNumber(String string) {
         try {
             Integer.parseInt(string);
         } catch (NumberFormatException ex) {
             return false;
         }
         return true;
     }
 	
 	public void removeWorldPortal(Block block, boolean removeBlock) {
 		removeWorldPortal(block.getWorld(), block, removeBlock);
 	}
 	public void removeWorldPortal(World world, Block block, boolean removeBlock) {
 		for(int i = 0; i < worldPortals.size(); i++) {
 			String[] worldPortalValues = worldPortals.get(i).split("\\|");
 			if(worldPortalValues[0].equals(world.getName())) {
 				String worldPortalLocationString = Integer.toString(block.getX())+" "+Integer.toString(block.getY())+" "+Integer.toString(block.getZ());
 				if(worldPortalValues[1].equals(worldPortalLocationString)) {
 					worldPortals.remove(i);
 				}
 			}
 		}
 		if(removeBlock) {
 			block.setTypeId(0);
 		}
 		
 		// Save the portals because one was removed
 		saveWorldPortals();
 	}
 	
 	public boolean isWorldPortal(Block block) {
 		return isWorldPortal(block.getWorld(), block);
 	}
 	public boolean isWorldPortal(World world, Block block) {
 		for(int i = 0; i < worldPortals.size(); i++) {
 			String[] worldPortalValues = worldPortals.get(i).split("\\|");
 			if(worldPortalValues[0].equals(world.getName())) {
 				String worldPortalLocationString = Integer.toString(block.getX())+" "+Integer.toString(block.getY())+" "+Integer.toString(block.getZ());
 				if(worldPortalValues[1].equals(worldPortalLocationString)) {
 					return true;
 				}
 			}
 		}
 		return false;
 	}
 	
 	public World getWorldPortalLinkedWorld(Block block) {
 		return getWorldPortalLinkedWorld(block.getWorld(), block);
 	}
 	public World getWorldPortalLinkedWorld(World world, Block block) {
 		for(int i = 0; i < worldPortals.size(); i++) {  
 			String[] worldPortalValues = worldPortals.get(i).split("\\|");
 			if(worldPortalValues[0].equals(world.getName())) {
 				String worldPortalLocationString = Integer.toString(block.getX())+" "+Integer.toString(block.getY())+" "+Integer.toString(block.getZ());
 				if(worldPortalValues[1].equals(worldPortalLocationString)) {
 					return getServer().getWorld(worldPortalValues[2].toString());
 				}
 			}
 		}
 		return world;
 	}
 	public String getWorldPortalLinkedWorldName(String world, Block block) {
 		for(int i = 0; i < worldPortals.size(); i++) {  
 			String[] worldPortalValues = worldPortals.get(i).split("\\|");
 			if(worldPortalValues[0].equals(world)) {
 				String worldPortalLocationString = Integer.toString(block.getX())+" "+Integer.toString(block.getY())+" "+Integer.toString(block.getZ());
 				if(worldPortalValues[1].equals(worldPortalLocationString)) {
 					return worldPortalValues[2].toString();
 				}
 			}
 		}
 		return world;
 	}
 	
 	public Location getWorldPortalLinkedWorldSpawnLocation(World world, Block block) {
 		for(int i = 0; i < worldPortals.size(); i++) {  
 			String[] worldPortalValues = worldPortals.get(i).split("\\|");
 			if(worldPortalValues[0].equals(world.getName())) {
 				String worldPortalLocationString = Integer.toString(block.getX())+" "+Integer.toString(block.getY())+" "+Integer.toString(block.getZ());
 				if(worldPortalValues[1].equals(worldPortalLocationString)) {
 					
 					if(worldPortalValues[3].equalsIgnoreCase("spawn")) {
 						return getFixedSpawnLocation(getWorldPortalLinkedWorld(world, block).getSpawnLocation());
 						
 					} else if(worldPortalValues[3].split(" ").length == 2){
 						String[] splittedLocation = worldPortalValues[3].split(" ");
 						Location linkedWorldSpawnLocation = block.getLocation();;
 						linkedWorldSpawnLocation.setX(Integer.parseInt(splittedLocation[0].toString()));
 						linkedWorldSpawnLocation.setY(0);
 						linkedWorldSpawnLocation.setZ(Integer.parseInt(splittedLocation[1].toString()));
 						linkedWorldSpawnLocation.setY(getWorldPortalLinkedWorld(world, block).getHighestBlockYAt(linkedWorldSpawnLocation.getBlockX(), linkedWorldSpawnLocation.getBlockZ()));
 						return linkedWorldSpawnLocation;
 						
 					} else {
 						String[] splittedLocation = worldPortalValues[3].split(" ");
 						Location linkedWorldSpawnLocation = block.getLocation();;
 						linkedWorldSpawnLocation.setX(Integer.parseInt(splittedLocation[0].toString()));
 						linkedWorldSpawnLocation.setY(Integer.parseInt(splittedLocation[1].toString()));
 						linkedWorldSpawnLocation.setZ(Integer.parseInt(splittedLocation[2].toString()));
 						return linkedWorldSpawnLocation;
 					}
 						
 				}
 			}
 		}
 		return block.getLocation();
 	}
 	
 	public float getWorldPortalLinkedWorldLookingDirection(World world, Block block) {
 		for(int i = 0; i < worldPortals.size(); i++) {  
 			String[] worldPortalValues = worldPortals.get(i).split("\\|");
 			if(worldPortalValues[0].equals(world.getName())) {
 				String worldPortalLocationString = Integer.toString(block.getX())+" "+Integer.toString(block.getY())+" "+Integer.toString(block.getZ());
 				if(worldPortalValues[1].equals(worldPortalLocationString)) {
 					
 					if(worldPortalValues.length >= 5) {
 						return Float.parseFloat(worldPortalValues[4].toString());
 					}
 						
 				}
 			}
 		}
 		return 0;
 	}
 	
 	public void doWorldPortal(Player player, Block block) {
 		doWorldPortal(player, block.getWorld(), block);
 	}
 	public void doWorldPortal(Player player, World world, Block block) {
 		if(!canUsePortal(player)) {
 			player.sendMessage(ChatColor.YELLOW + "[World Portal] " + ChatColor.DARK_RED + "You don't have permission to use this portal!");
 			return;
 		}
 		
 		String tpToWorldName = getWorldPortalLinkedWorldName(block.getWorld().getName(), block);
 		if(!isWorldLoaded(tpToWorldName)) {
 			if(worldExists(tpToWorldName)) {
 				loadWorld(tpToWorldName);
 			} else {
 				createWorld(tpToWorldName, Environment.NORMAL, true);
 			}
 		}
 		
 		// Set some variables to teleport to
 		World tpToWorld = getWorldPortalLinkedWorld(block.getWorld(), block);
 		Location WorldPortalLinkedWorldSpawnLocation = getWorldPortalLinkedWorldSpawnLocation(player.getWorld(), block);
 		Location tpToWorldLocation = new Location(tpToWorld, WorldPortalLinkedWorldSpawnLocation.getX(), WorldPortalLinkedWorldSpawnLocation.getY(), WorldPortalLinkedWorldSpawnLocation.getZ());
 		
 		// Set the looking direction of the teleportation
 		tpToWorldLocation.setYaw(getWorldPortalLinkedWorldLookingDirection(player.getWorld(), block));
 		
 		// Moved to quickly fix!
 		addMovedTooQuicklyIgnoreListPlayer(player);
 		
 		// Teleport the player using the following function
 		teleportPlayer(player, tpToWorldLocation, true);
 		
 		// Force the chunk where the player was teleported to, to load.
 		// Otherwise its possible that the player is floating in a black hole in the world.
 		forceChunkToLoad(tpToWorldLocation);
 	}
 	
 	public void teleportPlayer(Player player, String worldName) {
 		teleportPlayer(player, worldName, true);
 	}
 	public void teleportPlayer(Player player, String worldName, boolean showMessage) {
 		teleportPlayer(player, getServer().getWorld(worldName).getSpawnLocation(), showMessage);
 	}
 	public void teleportPlayer(Player player, String worldName, int x, int z) {
 		teleportPlayer(player, worldName, x, z, true);
 	}
 	public void teleportPlayer(Player player, String worldName, int x, int z, boolean showMessage) {
 		int y = getServer().getWorld(worldName).getHighestBlockYAt(x, z);
 		teleportPlayer(player, worldName, x, y, z, showMessage);
 	}
 	public void teleportPlayer(Player player, String worldName, int x, int y, int z) {
 		teleportPlayer(player, worldName, x, y, z, true);
 	}
 	public void teleportPlayer(Player player, String worldName, int x, int y, int z, boolean showMessage) {
 		teleportPlayer(player, new Location(getServer().getWorld(worldName), x, y, z), showMessage);
 	}
 	public void teleportPlayer(Player player, Location location) {
 		teleportPlayer(player, location, true);
 	}
 	public void teleportPlayer(Player player, Location location, boolean showMessage) {
 		// Teleport the player
 		
 		// Fix the location (to the middle of the block!)
 		if(((double)location.getBlockX()) >= 0) {
 			location.setX(((double)location.getBlockX())+0.5);
 		} else {
 			location.setX(((double)location.getBlockX())+0.5);
 		}
 		if(((double)location.getBlockZ()) >= 0) {
 			location.setZ(((double)location.getBlockZ())+0.5);
 		} else {
 			location.setZ(((double)location.getBlockZ())+0.5);
 		}
 		
 		addLastTeleportPlayerLocation(player, location);
 		player.setVelocity(new Vector(0, 0, 0));
 		player.teleport(location);
 
 		/*getServer().broadcastMessage("0:" + player.getLocation().toString());
 		Location l = new Location(location.getWorld(), player.getLocation().getX(), player.getLocation().getY(), player.getLocation().getZ());
 		addMovedTooQuicklyIgnoreListPlayer(player);
 		player.teleport(l);
 		getServer().broadcastMessage("1:" + player.getLocation().toString());
 		addLastTeleportPlayerLocation(player, location);
 		addMovedTooQuicklyIgnoreListPlayer(player);
 		player.teleport(location);
 		getServer().broadcastMessage("2:" + player.getLocation().toString());
 		
 		getServer().broadcastMessage("TELEPORTHAT");
 		
 		player.teleport(location, TeleportCause.COMMAND);
 		getServer().broadcastMessage("3:" + player.getLocation().toString());*/
 		
 		if(showMessage) {
 			if(getConfig().getBoolean("showMessageOnTeleportation", true)) {
 				String message = getMessage("tpToWorldMessage", "&e[World Portal] Teleported to the world '&f%worldname%&e'");
 				player.sendMessage(message.replace("%worldname%", location.getWorld().getName().toString()));
 			}
 		}
 	}
 	
 	public Location getFixedSpawnLocation(Location spawnLocation) {
 		Location currentLocation = spawnLocation;
 		Location aboveCurrentLocation = currentLocation; aboveCurrentLocation.setY(aboveCurrentLocation.getY() + 1);
 		
 		for(int y = currentLocation.getBlockY(); y < 128; y++) {
 			if(currentLocation.getBlock().getTypeId() == 0) {
 				if(aboveCurrentLocation.getBlock().getTypeId() == 0) {
 					return currentLocation.getBlock().getLocation();
 				}
 			}
 			
 			currentLocation.setY(currentLocation.getY() + 1);
 			aboveCurrentLocation.setY(currentLocation.getY() + 1);
 		}
 		return spawnLocation;
 	}
 	
 	/**
 	 * This will force a chunk to reload
 	 * @param location The location of a block inside the chunk
 	 * @return Returns true when the chunk was already loaded. But it is force reloaded again
 	 */
 	public boolean forceChunkToLoad(Location location) {
 		boolean b = location.getChunk().isLoaded();
 		location.getChunk().load();
 		return b;
 	}
 }
