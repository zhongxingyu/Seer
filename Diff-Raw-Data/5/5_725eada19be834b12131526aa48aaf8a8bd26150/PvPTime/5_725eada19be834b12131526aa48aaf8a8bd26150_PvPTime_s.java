 package net.erbros.PvPTime;
 
 import java.util.HashMap;
 import java.util.List;
 import java.util.logging.Logger;
 
 import org.bukkit.configuration.Configuration;
 import org.bukkit.entity.Player;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.java.JavaPlugin;
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.World;
 
 
 public class PvPTime extends JavaPlugin {
 
 	public boolean pvpPluginDisable;
 	
 	private DamageListener dL = new DamageListener(this);
 	private PlayerEventListener pEL = new PlayerEventListener(this);
 	private WorldLoadListener wLL = new WorldLoadListener(this);
 	public HashMap<String,HashMap<String,Object>> pvpWorlds = new HashMap<String,HashMap<String,Object>>();
 	public HashMap<String,Boolean> pvpAnnouncedWorlds = new HashMap<String, Boolean>();
 	// Getting some logging done.
 	protected final Logger log = Logger.getLogger("Minecraft");
 	
 	
 	@Override
 	public void onDisable() {
 		// Disable all running timers.
 		Bukkit.getServer().getScheduler().cancelTasks(this);
 	}
 
 	@Override
 	public void onEnable() {
         getDataFolder().mkdirs();
 	    // Any enabled worlds already?
         for(World w : Bukkit.getServer().getWorlds()) {
             loadWorldConfig(w);
         }
 
         // Any broadcast? Then we need a timer ;)
         
 		
 		PluginManager pm = this.getServer().getPluginManager();
 		pm.registerEvents(dL, this);
 		pm.registerEvents(pEL, this);
 		pm.registerEvents(wLL, this);
 		
 	}
 	
 	public void reloadPvP() {
         boolean anyPvPBroadcast = false;
 	    for(World w : Bukkit.getServer().getWorlds()){
             if((Boolean) getValue(pvpWorlds,w.getName(),"enabled") == true) {
                 
                 // Checking if we start with pvpTime
                 if(dL.isItPvPTime(w.getName())) {
                     pvpAnnouncedWorlds.put(w.getName(), true);
                 } else {
                     pvpAnnouncedWorlds.put(w.getName(), false);
                 }
                 
 
                 // Are we forcing the pvp setting on?
                 if((Boolean) getValue(pvpWorlds, w.getName() ,"forcePvP")) {
                     getServer().getWorld(w.getName()).setPVP(true);
                 }
                 // Any broadcast? Do we need a timer?
                 if((Boolean) getValue(pvpWorlds, w.getName() ,"startMsgBroadcast") || (Boolean) getValue(pvpWorlds, w.getName() ,"endMsgBroadcast")) {
                     anyPvPBroadcast = true;
                 }
             }
         }
 	    
 	    if(anyPvPBroadcast) {
             checkTime();
         }
 	}
 	
 	public void checkTime() {
 		// is it pvp on now?
         long lowestTimeLeft = 0;
 	    
 	    for(World w : Bukkit.getServer().getWorlds()){
     	    if((Boolean) getValue(pvpWorlds,w.getName(),"enabled")) {
     	        if((Boolean) getValue(pvpWorlds,w.getName(),"startMsgBroadcast") || (Boolean) getValue(pvpWorlds,w.getName(),"endMsgBroadcast")) {
             		if(dL.isItPvPTime(w.getName())) {
             			// it's pvp time, but have we announced it?
             			if(pvpAnnouncedWorlds.get(w.getName()) == false) {
             				announceNow(true,w.getName());
             			}
             			
             		} else {
             			// it's not pvp time, but have we announced it?
             			if(pvpAnnouncedWorlds.get(w.getName()) == true) {
             				announceNow(false,w.getName());
             			}
             			
             		}
             		// Is this the lowest time left?
             		if(nextBroadcast(w.getName()) < lowestTimeLeft || lowestTimeLeft == 0) {
             		    lowestTimeLeft = nextBroadcast(w.getName());
             		}
     	        }
     	    }
 	    }
 		checkTimeClock(Math.round(lowestTimeLeft/3+1));
 		
 	}
 	
 	// Check time for next broadcast in world
 	public Long nextBroadcast(String world) {
 	    long nextBroadcast = 0;
 	    
 	    if(dL.isItPvPTime(world)) {
             // Check how long until pvp is over.
             if(Bukkit.getWorld(world).getTime() > Long.parseLong(getValue(pvpWorlds,world,"endTime").toString())) {
                 nextBroadcast = 24000 - Bukkit.getWorld(world).getTime() + Long.parseLong(getValue(pvpWorlds,world,"endTime").toString());
             } else {
                 nextBroadcast = Long.parseLong(getValue(pvpWorlds,world,"endTime").toString()) - Bukkit.getWorld(world).getTime();
             }
         } else {
             if(Bukkit.getWorld(world).getTime() > Long.parseLong(getValue(pvpWorlds,world,"startTime").toString())) {
                 nextBroadcast = 24000 - Bukkit.getWorld(world).getTime() + Long.parseLong(getValue(pvpWorlds,world,"startTime").toString());
             } else {
                 nextBroadcast = Long.parseLong(getValue(pvpWorlds,world,"startTime").toString()) - Bukkit.getWorld(world).getTime();
             }
         }
 	    
 	    return nextBroadcast;
 	}
 	
 	public void checkTimeClock(long countdown) {
 		getServer().getScheduler().cancelTasks(this);
 		getServer().getScheduler().scheduleAsyncDelayedTask(this, new Runnable() {
 				@Override
                 public void run() {
 					checkTime();
 				}
 			}, countdown);
 	}
 	
 	public void announceNow (boolean pvpOn, String world) {
 		if(pvpOn) {
 			if((Boolean) getValue(pvpWorlds,world,"startMsgBroadcast")) {
 			    broadcastAllPlayersWorld(Bukkit.getWorld(world),getColorFromString((String) getValue(pvpWorlds,world,"startMsgColor")) + (String) getValue(pvpWorlds,world,"startMsg"));
 			}
 			pvpAnnouncedWorlds.put(world, true);			
 		} else {
 			if((Boolean) getValue(pvpWorlds,world,"endMsgBroadcast")) {
 			    broadcastAllPlayersWorld(Bukkit.getWorld(world),getColorFromString((String) getValue(pvpWorlds,world,"endMsgColor")) + (String) getValue(pvpWorlds,world,"endMsg"));
 			}
 			pvpAnnouncedWorlds.put(world, false); 
 		}
 	}
 	
 	public ChatColor getColorFromString(String color) {
 		if(color.equalsIgnoreCase("BLACK")) {
 			return ChatColor.BLACK;
 		}else if(color.equalsIgnoreCase("DARK_BLUE")) {
 			return ChatColor.DARK_BLUE;
 		}else if(color.equalsIgnoreCase("DARK_AQUA")) {
 			return ChatColor.DARK_AQUA;
 		}else if(color.equalsIgnoreCase("DARK_RED")) {
 			return ChatColor.DARK_RED;
 		}else if(color.equalsIgnoreCase("DARK_PURPLE")) {
 			return ChatColor.DARK_PURPLE;
 		}else if(color.equalsIgnoreCase("GOLD")) {
 			return ChatColor.GOLD;
 		}else if(color.equalsIgnoreCase("GRAY")) {
 			return ChatColor.GRAY;
 		}else if(color.equalsIgnoreCase("DARK_GRAY")) {
 			return ChatColor.DARK_GRAY;
 		}else if(color.equalsIgnoreCase("BLUE")) {
 			return ChatColor.BLUE;
 		}else if(color.equalsIgnoreCase("GREEN")) {
 			return ChatColor.GREEN;
 		}else if(color.equalsIgnoreCase("AQUA")) {
 			return ChatColor.AQUA;
 		}else if(color.equalsIgnoreCase("RED")) {
 			return ChatColor.RED;
 		}else if(color.equalsIgnoreCase("LIGHT_PURPLE")) {
 			return ChatColor.LIGHT_PURPLE;
 		}else if(color.equalsIgnoreCase("YELLOW")) {
 			return ChatColor.YELLOW;
 		}else if(color.equalsIgnoreCase("WHITE")) {
 			return ChatColor.WHITE;
 		}		
 		return ChatColor.WHITE;
 	}
 	
 	// Get value from hashmap inside hashmap
 	public Object getValue (HashMap<String,HashMap<String,Object>> map, String mainKey, String nodeKey) {
 	    // do mainKey exist?
 	    if(!map.containsKey(mainKey)) {
 	        return false;
 	    }
         HashMap<String,Object> obj = map.get(mainKey);
         if(!obj.containsKey(nodeKey)) {
             return false;
         }
         Object value = obj.get(nodeKey);
         
         // Null check
         if(value == null) {
             value = false;
         }
         
 	    return value;
 	}
 	
 	public void broadcastAllPlayersWorld(World world, String message) {
 	    List<Player> players = world.getPlayers();
 	    for(Player p : players) {
 	        p.sendMessage(message);
 	    }
 	}
 	
 	public void loadWorldConfig(World w) {
         Configuration config = getConfig();
         
         if(!config.contains("world." + w.getName() + ".enabled")) {
             setWorldConfig(w);
         }
         
         // First, making a HashMap to go inside the global HashMap of the worlds.
         HashMap<String,Object> currentWorld = new HashMap<String, Object>();
         // before we put this HashMap inside the global hashmap, let's fill it up.
         currentWorld.put("enabled", config.getBoolean("world." + w.getName() + ".enabled", false));
         currentWorld.put("startTime", config.getInt("world." + w.getName() + ".start.time", 13000));
         currentWorld.put("startMsg", config.getString("world." + w.getName() + ".start.msg.text", "It's night and PvP is turned on"));
         currentWorld.put("startMsgColor", config.getString("world." + w.getName() + ".start.msg.color", "DARK_RED"));
         currentWorld.put("startMsgBroadcast", config.getBoolean("world." + w.getName() + ".start.msg.broadcast", true));
         currentWorld.put("endTime", config.getInt("world." + w.getName() + ".end.time", 1000));
         currentWorld.put("endMsg", config.getString("world." + w.getName() + ".end.msg.text", "It's daytime and PvP is turned off"));
         currentWorld.put("endMsgColor", config.getString("world." + w.getName() + ".end.msg.color", "GREEN"));
         currentWorld.put("endMsgBroadcast", config.getBoolean("world." + w.getName() + ".end.msg.broadcast", true));
         currentWorld.put("forcePvP", config.getBoolean("world." + w.getName() + ".forcePvP", false));
         currentWorld.put("overrideEnabled", config.getBoolean("world." + w.getName() + ".override", false));
         
         
         // Let's put the currentWorld in pvpWorlds hashMap
         pvpWorlds.put(w.getName(), currentWorld);
         // Let's run the plugin refresh so it knows we have some worlds for it ;)
         reloadPvP();
     }
 	
 	public void setWorldConfig(World w) {
         Configuration config = getConfig();
 
         config.set("world." + w.getName() + ".enabled", false);
         config.set("world." + w.getName() + ".start.time", 13000);
         config.set("world." + w.getName() + ".start.msg.text", "It's night and PvP is turned on");
         config.set("world." + w.getName() + ".start.msg.color", "DARK_RED");
         config.set("world." + w.getName() + ".start.msg.broadcast", true);
         config.set("world." + w.getName() + ".end.time", 1000);
         config.set("world." + w.getName() + ".end.msg.text", "It's daytime and PvP is turned off");
         config.set("world." + w.getName() + ".end.msg.color", "GREEN");
         config.set("world." + w.getName() + ".end.msg.broadcast", true);
         config.set("world." + w.getName() + ".forcePvP", false);
         config.set("world." + w.getName() + ".override", false);
 	}
 	  
 
 }
