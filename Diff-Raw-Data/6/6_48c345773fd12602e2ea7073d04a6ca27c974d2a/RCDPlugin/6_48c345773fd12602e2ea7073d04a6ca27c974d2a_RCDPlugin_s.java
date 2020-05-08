 package me.hwei.bukkit.redstoneClockDetector;
 
 import java.util.Comparator;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.TreeMap;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.event.Event;
 import org.bukkit.event.Listener;
 import org.bukkit.event.Event.Priority;
 import org.bukkit.event.block.BlockRedstoneEvent;
 import org.bukkit.permissions.Permission;
 import org.bukkit.plugin.EventExecutor;
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class RCDPlugin extends JavaPlugin implements CommandExecutor, Listener, EventExecutor {
 
 	
 	
 	@Override
 	public void onDisable() {
 		this.getServer().getLogger().info(this.prefex_normal + "Disabled.");
 		
 	}
 
 	@Override
 	public void onEnable() {
 		String pluginName = this.getDescription().getName();
 		this.prefex_normal = "[" + pluginName + "] ";
 		this.prefex_color = "[" + ChatColor.YELLOW + pluginName + ChatColor.WHITE + "] ";
 		this.getServer().getLogger().info(this.prefex_normal + "Enabled.");
 		
 		this.getCommand("rcd").setExecutor(this);
 		this.permission = this.getDescription().getPermissions().get(0);
 		
 		this.getServer().getPluginManager().registerEvent(Event.Type.REDSTONE_CHANGE, this, this, Priority.Normal, this);
 	}
 	
 	@Override
 	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
 		if(sender instanceof Player) {
 			this.player = (Player)sender;
 			this.sender = null;
 		} else {
 			this.player = null;
 			this.sender = sender;
 		}
 		if(!sender.hasPermission(this.permission)) {
 			this.sendMessage("You don't have permissions.");
 			return true;
 		}
 		if(this.taskId != -1) {
 			this.sendMessage("Someone is using this. Try after " + this.waitingSeconds + " seconds.");
 			return true;
 		}
 		
 		this.waitingSeconds = 5;
 		if(args.length >= 1) {
 			try {
 				int input = Integer.parseInt(args[0]);
 				if(input > 0) {
 					this.waitingSeconds = input;
 				}
 			} catch (NumberFormatException e) {
 			}
 			if(args.length == 2) {
 				Player player = this.getServer().getPlayer(args[1]);
 				if(player == null) {
 					this.sendMessage("Can not find player: " + args[1] + " .");
 					return true;
 				}
 				if(this.sender == null) {
 					this.sender = this.player;
 				}
 				this.player = player;
 			} else if(args.length > 2) {
 				this.sendMessage("Too many arguments.");
 				return true;
 			}
 		}
 		if(this.player == null) {
 			this.sendMessage("Need to specify a player to teleport.");
 			return true;
 		}
 		this.playerName = this.player.getName();
 		this.redstoneActivityCount = 0;
 		this.redstoneActivityTable.clear();
 		
 		this.taskId = this.getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
 			class ValueComparator implements Comparator {
 				Map base;
 				public ValueComparator(Map base) {
 					this.base = base;
 				}
 				public int compare(Object a, Object b) {
 					if((Integer)base.get(a) < (Integer)base.get(b)) {
 						return 1;
 					} else if((Integer)base.get(a) == (Integer)base.get(b)) {
 						return 0;
 					} else {
 						return -1;
 					}
 				}
 			}	
 			public void run() {
 				player = getServer().getPlayer(playerName);
 				if(player == null) {
 					sendMessage("Player " + playerName + " dose not exists any more.");
 					getServer().getScheduler().cancelTask(taskId);
 					redstoneActivityTable.clear();
 					taskId = -1;
 					return;
 				}
 				
 				if(waitingSeconds == 0) {
 					getServer().getScheduler().cancelTask(taskId);
					redstoneActivityTable.clear();
					taskId = -1;
 					
 					if(redstoneActivityTable.size() == 0) {
 						sendMessage("No redstone activities found.");
 						return;
 					}
 					ValueComparator bvc = new ValueComparator(redstoneActivityTable);
 					TreeMap<Location, Integer> sortedMap = new TreeMap(bvc);
 					sortedMap.putAll(redstoneActivityTable);
 					Location l = sortedMap.keySet().iterator().next();
 					int acitvityCount = redstoneActivityTable.get(l);
 					
 					sendMessage("Teleporting " + playerName + " to x: " + l.getBlockX()
 							+ ", y: " + l.getBlockY()
 							+ ", z: " + l.getBlockZ() + " (Redstone activity count: " + acitvityCount + ")...");
 					player.teleport(l);
 					
 					return;
 				}
 				
 				sendMessage("Teleport " + playerName + " after " + waitingSeconds + " seconds. "
 						+ "(Overall redstone activity count: " + redstoneActivityCount + " )");
 		        --waitingSeconds;
 		    }
 		}, 0L, 20L);
 		
 		return false;
 	}
 	
 	@Override
 	public void execute(Listener listener, Event event) {
 		if(this.taskId == -1) {
 			return;
 		}
 		BlockRedstoneEvent e = null;
 		if(event instanceof BlockRedstoneEvent) {
 			e = (BlockRedstoneEvent)event;
 		} else {
 			return;
 		}
 		Location loc = e.getBlock().getLocation();
 		++this.redstoneActivityCount;
 		int count = 1;
 		if(this.redstoneActivityTable.containsKey(loc)) {
 			count += this.redstoneActivityTable.get(loc);
 		}
 		this.redstoneActivityTable.put(loc, count);
 	}
 	
 	
 	protected void sendMessage(String msg) {
 		if(this.player != null) {
 			this.player.sendMessage(this.prefex_color + msg);
 		}
 		if(this.sender != null) {
 			if(this.sender instanceof Player) {
 				this.sender.sendMessage(this.prefex_color + msg);
 			} else {
 				this.sender.sendMessage(this.prefex_normal + msg);
 			}
 		}
 	}
 	
 	protected HashMap<Location, Integer> redstoneActivityTable = new HashMap<Location, Integer>();
 	protected int redstoneActivityCount = 0;
 	protected int waitingSeconds = -1;
 	protected CommandSender sender = null;
 	protected Player player = null;
 	protected String playerName = "";
 	protected int taskId = -1;
 	protected String prefex_normal = "";
 	protected String prefex_color = "";
 	protected Permission permission = null;
 	
 	
 }
