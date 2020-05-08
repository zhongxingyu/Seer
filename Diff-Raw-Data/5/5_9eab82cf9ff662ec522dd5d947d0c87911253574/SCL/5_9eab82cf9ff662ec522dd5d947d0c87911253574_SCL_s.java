 package com.webkonsept.bukkit.simplechestlock;
 
 import com.webkonsept.bukkit.simplechestlock.listener.SCLBlockListener;
 import com.webkonsept.bukkit.simplechestlock.listener.SCLEntityListener;
 import com.webkonsept.bukkit.simplechestlock.listener.SCLPlayerListener;
 import com.webkonsept.bukkit.simplechestlock.listener.SCLWorldListener;
 import com.webkonsept.bukkit.simplechestlock.locks.LimitHandler;
 import com.webkonsept.bukkit.simplechestlock.locks.SCLItem;
 import com.webkonsept.bukkit.simplechestlock.locks.SCLList;
 import com.webkonsept.bukkit.simplechestlock.locks.TrustHandler;
 import org.bukkit.ChatColor;
 import org.bukkit.Material;
 import org.bukkit.Server;
 import org.bukkit.block.Block;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.permissions.Permission;
 import org.bukkit.permissions.PermissionDefault;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.java.JavaPlugin;
 
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.logging.Logger;
 
 public class SCL extends JavaPlugin {
 	private static final Logger log = Logger.getLogger("Minecraft");
 
     protected static final String pluginName = "SimpleChestLock";
     protected static String pluginVersion = "???";
 
     protected static boolean verbose = false;
 
     public Settings cfg = null;
 
 	public final Messaging messaging = new Messaging(3000);
     public TrustHandler trustHandler;
     public LimitHandler limitHandler;
 
 	protected Server server = null;
 	
 	// Lockable blocks
 	public final HashSet<Material> lockable = new HashSet<Material>();
     // Locks that automagically find "the other half", like chests and doors
     public final HashSet<Material> doubleLock = new HashSet<Material>();
     // Blocks that can't be built next to if protective auras are active
     public final HashSet<Material> hasAura = new HashSet<Material>();
 	// Holding the valid locations for a multi-lockable block
 	public final HashSet<Material> lockIncludeVertical = new HashSet<Material>();
 	// Okay for the "sucks items" feature (Item containers only plx!)
 	public final HashSet<Material> canSuck = new HashSet<Material>();
 	
 	// The "Lock as" feature!
 	public final HashMap<String,String> locksAs = new HashMap<String,String>();
 	
 	private final SCLPlayerListener playerListener 	= new SCLPlayerListener(this);
 	private final SCLBlockListener 	blockListener 	= new SCLBlockListener(this);
 	private final SCLEntityListener entityListener 	= new SCLEntityListener(this);
 	private final SCLWorldListener	worldListener	= new SCLWorldListener(this);
 	public  final SCLList			chests			= new SCLList(this);
 	
 	@Override
 	public void onDisable() {
 		chests.save("Chests.txt");
 		// out("Disabled!"); // Bukkit already does this
 		getServer().getScheduler().cancelTasks(this);
 	}
 
 	@Override
 	public void onEnable() {
         pluginVersion = getDescription().getVersion();
         cfg = new Settings(this);
 		setupLockables();
 		
 		trustHandler = new TrustHandler(this);
 		limitHandler = new LimitHandler(this);
 
 		server = getServer();
 		chests.load("Chests.txt");
 		PluginManager pm = getServer().getPluginManager();
 		
 		pm.registerEvents(playerListener,this);
 		pm.registerEvents(blockListener,this);
 		pm.registerEvents(entityListener,this);
 		pm.registerEvents(worldListener,this);
 		
 		if (cfg.lockedChestsSuck()){
 			server.getScheduler().scheduleSyncRepeatingTask(this,chests, cfg.suckInterval(), cfg.suckInterval());
 		}
 	}
 	
 	public boolean onCommand(CommandSender sender, Command command, String commandLabel, String[] args) {
 		
 		if ( ! this.isEnabled() ) return false;
 		
 		boolean success = false;
 		boolean isPlayer = false;
 		Player player = null;
 		
 		if (sender instanceof Player){
 			isPlayer = true;
 			player = (Player)sender;
 		}
 
 		if (command.getName().equalsIgnoreCase("scl")){
 			if (args.length == 0){
 				success = false;  // Automagically prints the usage.
 			}
 			else if (args.length >= 1){
 				if (args[0].equalsIgnoreCase("reload")){
 					success = true;  // This is a valid command.
 					if ( !isPlayer  || permit(player, "simplechestlock.command.reload")){
                         cfg.load();
                         cfg.report(sender);
                         setupLockables();
 						String saveFile = "Chests.txt";
 						if (args.length == 2){
 							saveFile = args[1];
 						}
 						chests.load(saveFile);
 						server.getScheduler().cancelTasks(this);
 						if (cfg.lockedChestsSuck()){
 							server.getScheduler().scheduleSyncRepeatingTask(this,chests, 100, 100);
 						}
 						sender.sendMessage(ChatColor.GREEN+"Successfully reloaded configuration and locks from "+saveFile);
 						
 					}
 					else {
 						sender.sendMessage(ChatColor.RED+"[SimpleChestLock] Sorry, permission denied!");
 					}
 				}
 				else if (args[0].equalsIgnoreCase("as")){
 					success = true;
 					if (args.length == 2){
 						if (!isPlayer){
 							sender.sendMessage("Sorry mr. Console, you can't lock as anyone.  How will you swing the stick?");
 						}
 						else if (permit(player, "simplechestlock.command.as")){
 							locksAs.put(player.getName(),args[1]);
 							sender.sendMessage(ChatColor.RED+"[SimpleChestLock] Locking chests for "+args[1]);
 						}
 						else {
 							sender.sendMessage(ChatColor.RED+"[SimpleChestLock] Sorry, permission denied!");
 						}
 					}
 					else if (args.length == 1){
 						if (locksAs.containsKey(player.getName())){
 							locksAs.remove(player.getName());
 						}
 						sender.sendMessage(ChatColor.GREEN+"[SimpleChestLock] Locking chests for yourself");
 					}
 					else if (args.length > 2){
 						sender.sendMessage(ChatColor.YELLOW+"[SimpleChestLock] Argument amount mismatch.  /scl as <name>");
 					}
 				}
 				else if (args[0].equalsIgnoreCase("save")){
 					success = true;
 					if (!isPlayer || permit(player,"simplechestlock.command.save")){
 						String saveFile = "Chests.txt";
 						if (args.length == 2){
 							saveFile = args[1];
 						}
 						chests.save(saveFile);
 						sender.sendMessage(ChatColor.GREEN+"Saved locks to "+saveFile);
 					}
 					else {
 						sender.sendMessage(ChatColor.RED+"[SimpleChestLock] Sorry, permission denied!");
 					}
 				}
 				else if (args[0].equalsIgnoreCase("limit")){
 				    success = true;
 				    if (!isPlayer){
 				        sender.sendMessage("Mr. Console, you can't lock anything at all, so your limit is -1!");
 				    }
 				    else if (!cfg.useLimits()){
 				        sender.sendMessage(ChatColor.GOLD+"This server has no lock limits");
 				    }
 				    else if (permit(player,"simplechestlock.nolimit")){
 				        sender.sendMessage(ChatColor.GOLD+"You are excempt from lock limits");
 				    }
 				    else {
 				        sender.sendMessage(ChatColor.GREEN+limitHandler.usedString(player));
 				    }
 				}
 				else if (args[0].equalsIgnoreCase("status")){
 					success = true;
 					if (!isPlayer || permit(player,"simplechestlock.command.status")){
 						HashMap<String,Integer> ownership = new HashMap<String,Integer>();
 						int total = 0;
 						for (SCLItem item : chests.list.values()){
 							Integer owned = ownership.get(item.getOwner());
 							total++;
 							if (owned == null){
 								ownership.put(item.getOwner(),1);
 							}
 							else {
 								ownership.put(item.getOwner(),++owned);
 							}
 						}
 						
 						for (String playerName : ownership.keySet()){
 							Integer owned = ownership.get(playerName);
 							if (owned == null){
 								owned = 0;
 							}
 							sender.sendMessage(playerName+": "+owned);
 						}
 						sender.sendMessage("Total: "+total);
 					}
 				}
 				else if (args[0].equalsIgnoreCase("trust")){
 				    if (isPlayer){
 				        if (permit(player,"simplechestlock.command.trust")){
 				            trustHandler.parseCommand(player,args);
 				        }
 				    }
 				    success = true;
 				}
 				else if (args[0].equalsIgnoreCase("list")){
 					success = true;
 					if (!isPlayer || permit(player, "simplechestlock.command.list")){
 						for (SCLItem item : chests.list.values()){
 							sender.sendMessage(item.getLocation().toString());
 						}
 					}
 				}
 				else if (args[0].equalsIgnoreCase("getkey")){
 				    success = true;
 				    if (isPlayer){
 				        if (permit(player, "simplechestlock.command.getkey")){
 				            player.getInventory().addItem(cfg.key().clone());
 				            player.sendMessage(ChatColor.GREEN+"One key coming right up!");
 				        }
 				        else {
 				            player.sendMessage(ChatColor.RED+"Access denied");
 				        }
 				    }
 				    else {
 				        sender.sendMessage("Sorry, Mr. Console, you can't carry keys.");
 				    }
 				}
 				else if (args[0].equalsIgnoreCase("getcombokey")){
 				    success = true;
                     if (isPlayer){
                         if (permit(player, "simplechestlock.command.getcombokey")){
                             player.getInventory().addItem(cfg.comboKey().clone());
                             player.sendMessage(ChatColor.GREEN+"One combokey coming right up!");
                         }
                         else {
                             player.sendMessage(ChatColor.RED+"Access denied");
                         }
                     }
                     else {
                         sender.sendMessage("Sorry, Mr. Console, you can't carry keys.");
                     }				    
 				}
 			}
 		}
 		else {
 			sender.sendMessage(ChatColor.RED+"Command is deprecated, try /scl");
 		}
 		return success;
 	}
 	public static boolean permit(Player player,String[] permissions){
 		
 		if (player == null) return false;
 		if (permissions == null) return false;
 		String playerName = player.getName();
 		boolean permit = false;
 		for (String permission : permissions){
 			permit = player.hasPermission(permission);
 			if (permit){
 				verbose("Permission granted: " + playerName + "->(" + permission + ")");
 				break;
 			}
 			else {
 				verbose("Permission denied: " + playerName + "->(" + permission + ")");
 			}
 		}
 		return permit;
 		
 	}
 	public static boolean permit(Player player,String permission){
 		return permit(player,new String[]{permission});
 	}
 	public static void out(String message) {
         log.info("[" + pluginName + " v" + pluginVersion + "] " + message);
 	}
 	public static void crap(String message){
         log.severe("[" + pluginName + " v" + pluginVersion + "] " + message);
 	}
 	public static void verbose(String message){
 		if (!verbose){ return; }
 		log.info("[" + pluginName + " v" + pluginVersion + " VERBOSE] " + message);
 	}
 	public static String plural(int number) {
 		if (number == 1){
 			return "";
 		}
 		else {
 			return "s";
 		}
 	}
 	private void setupLockables() {
         // TODO: Move this to Settings.java where it belongs!
		lockable.clear();
         canSuck.clear();
 
         reloadConfig();
 
         List<String> lockables = getConfig().getStringList("lockables.lockable");
         verbose("lockable:");
         for (String lockableBlockName : lockables){
             Material mat = null;
             try {
                 mat = Material.valueOf(lockableBlockName);
             }
             catch (IllegalArgumentException e) {
                 try {
                     int i = Integer.parseInt(lockableBlockName.trim());
                     mat = Material.getMaterial(i);
                 }
                 catch (NumberFormatException nfe) {
                     // Empty catch block, will null-check later
                 }
             }
 
             if (mat != null){
                 lockable.add(mat);
                 verbose("    "+lockableBlockName);
             }
             else {
                 SCL.crap("Sorry, material named '"+lockableBlockName+"' does not exist, and can't be locked!");
             }
         }
 
         List<String> doubles = getConfig().getStringList("lockables.lockPair");
         verbose("lockPair:");
         for (String lockableBlockName : doubles){
             Material mat = Material.valueOf(lockableBlockName);
             if (mat != null && lockable.contains(mat)){
                 doubleLock.add(mat);
                 verbose("    "+lockableBlockName);
             }
             else {
                 SCL.crap("Sorry, material named '"+lockableBlockName+"' can't be pair locked.  Is it lockable at all?");
             }
         }
 
         List<String> canHaveAura = getConfig().getStringList("lockables.protectiveAura");
         verbose("protectiveAura:");
         for (String blockName : canHaveAura){
             Material mat = Material.valueOf(blockName);
             if (mat != null && lockable.contains(mat)){
                 hasAura.add(mat);
                 verbose("    "+blockName);
             }
             else {
                 crap("Sorry, '"+blockName+"' can't have a protective aura. Is it lockable at all?");
             }
         }
 
         List<String> verticalLockable = getConfig().getStringList("lockables.lockVertical");
         verbose("lockVertical:");
         for (String blockName : verticalLockable){
             Material mat = Material.valueOf(blockName);
             if (mat != null && lockable.contains(mat)){
                 lockIncludeVertical.add(mat);
                 verbose("    "+blockName);
             }
             else {
                 crap("Sorry, '"+blockName+"' can't be vertically lockable.  Is it lockable at all?");
             }
         }
 
         List<String> omgThisSucks = getConfig().getStringList("lockables.canSuck");
         verbose("canSuck");
         for (String sucks : omgThisSucks){
             Material mat = Material.valueOf(sucks);
             if (mat != null && lockable.contains(mat)){
                 canSuck.add(mat);
                 verbose("    "+sucks);
             }
             else {
                 crap("Sorry, '"+sucks+"' can't be vertically lockable.  Is it lockable at all?");
             }
         }
 		
 		// The associated permissions
 		verbose("Preparing permissions:");
 
 	    Permission allBlocksPermission = new Permission("simplechestlock.locktype.*");
         Permission oldAllBlocksPermission = getServer().getPluginManager().getPermission("simplechestlock.locktype.*");
         if (oldAllBlocksPermission != null){
             verbose("Old all blocks permission removed.");
             getServer().getPluginManager().removePermission(oldAllBlocksPermission);
         }
         for (Material mat : lockable){
             if (mat.isBlock()){
                 String permissionName = "simplechestlock.locktype."+mat.toString().toLowerCase();
                 verbose("   -> Preparing permission " + permissionName);
                 Permission thisBlockPermission = new Permission(permissionName,PermissionDefault.OP);
                 //getServer().getPluginManager().addPermission(allBlocksPermission);
                 thisBlockPermission.addParent(allBlocksPermission, true);
             }
         }
         getServer().getPluginManager().addPermission(allBlocksPermission);
 	}
 	public boolean canLock (Block block){
 		if (block == null) return false;
 		Material material = block.getType();
 		return lockable.contains(material);
 	}
 	public boolean canDoubleLock (Block block){
 		if (block == null) return false;
 		Material material = block.getType();
         return lockable.contains(material) && doubleLock.contains(material);
 	}
     public boolean hasAura (Block block){
         if (block == null) return false;
         Material material = block.getType();
         return lockable.contains(material) && hasAura.contains(material);
     }
 
 	public boolean toolMatch (ItemStack candidate1,ItemStack candidate2){
 	    if (candidate1 == null || candidate2 == null){
 	        return false;
 	    }
 	    else return candidate1.getType().equals(candidate2.getType())
                 && candidate1.getData().equals(candidate2.getData());
 	}
 }
