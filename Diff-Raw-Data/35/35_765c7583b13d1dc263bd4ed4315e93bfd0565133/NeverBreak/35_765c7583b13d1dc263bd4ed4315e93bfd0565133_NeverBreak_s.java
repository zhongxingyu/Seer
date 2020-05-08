 package com.github.thebiologist13;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.util.HashMap;
 import java.util.List;
 import java.util.logging.Logger;
 
 import net.minecraft.server.NBTTagCompound;
 
 import org.bukkit.ChatColor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.configuration.file.YamlConfiguration;
 import org.bukkit.craftbukkit.inventory.CraftItemStack;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.plugin.java.JavaPlugin;
 
 import com.github.thebiologist13.listeners.BlockDamageListener;
 import com.github.thebiologist13.listeners.BowFireListener;
 import com.github.thebiologist13.listeners.BreakListener;
 import com.github.thebiologist13.listeners.EntityDamageListener;
 import com.github.thebiologist13.listeners.FarmListener;
 import com.github.thebiologist13.listeners.FireStartListener;
 import com.github.thebiologist13.listeners.FishListener;
 import com.github.thebiologist13.listeners.LoginListener;
 import com.github.thebiologist13.listeners.PlayerDamageListener;
 import com.github.thebiologist13.listeners.ShearListener;
 import com.github.thebiologist13.listeners.WorldChangeListener;
 
 public class NeverBreak extends JavaPlugin {
 	
 	//YAML variable
 	private FileConfiguration config;
 	
 	//YAML file variable
 	private File configFile;
 	
 	//Toggling command executor variable
 	private ToggleCommand tc;
 	
 	//Set durability command executor variable
 	private DurabilityCommand dc;
 	
 	//Map of durability
 	private HashMap<Integer, Integer> configuredDurabilities = new HashMap<Integer, Integer>();
 	
 	//Logger
 	private Logger log = Logger.getLogger("Minecraft");
 	
 	//Map of modes
 	private HashMap<Player, Boolean> mode = new HashMap<Player, Boolean>();
 	
 	public void onEnable() {
 		
 		//TODO Maybe add higher priority in events.
 		
 		//Config
 		config = this.getCustomConfig();
 		
 		//Listeners
 		getServer().getPluginManager().registerEvents(new BreakListener(this), this);
 		getServer().getPluginManager().registerEvents(new LoginListener(this), this);
 		getServer().getPluginManager().registerEvents(new FishListener(this), this);
 		getServer().getPluginManager().registerEvents(new EntityDamageListener(this), this);
 		getServer().getPluginManager().registerEvents(new PlayerDamageListener(this), this);
 		getServer().getPluginManager().registerEvents(new WorldChangeListener(this), this);
 		getServer().getPluginManager().registerEvents(new BowFireListener(this), this);
 		getServer().getPluginManager().registerEvents(new FireStartListener(this), this);
 		getServer().getPluginManager().registerEvents(new BlockDamageListener(this), this);
 		getServer().getPluginManager().registerEvents(new ShearListener(this), this);
 		getServer().getPluginManager().registerEvents(new FarmListener(this), this);
 		
 		//Toggle command setup
 		tc = new ToggleCommand(this);
 		getCommand("neverbreak").setExecutor(tc);
 		
 		//Durability command setup
 		dc = new DurabilityCommand(this);
 		getCommand("setdurability").setExecutor(dc);
 		
 		//Durability map setting
 		putDamages(config);
 		
 		//Just removes players from the map that are not logged in anymore
 		getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
 
 			@Override
 			public void run() {
 				
 				for(Player p : mode.keySet()) {
 					
 					if(!p.isOnline()) {
 						mode.remove(p);
 					}
 					
 				}
 				
 			}
 			
 		}, 20, 1200);
 		
 		//Enable message
 		log.info("NeverBreak v" + this.getDescription().getVersion() + " by thebiologist13 has been enabled!");
 	}
 	
 	public void onDisable() {
 		//Disable message
 		log.info("NeverBreak v" + this.getDescription().getVersion() + " by thebiologist13 has been disabled!");
 	}
 	
 	//Config stuff
 	//Credit goes to respective owners.
 	public void reloadCustomConfig() {
 		if (configFile == null) {
 		    configFile = new File(getDataFolder(), "config.yml");
 		    
 		    if(!configFile.exists()){
 		        configFile.getParentFile().mkdirs();
 		        copy(getResource("config.yml"), configFile);
 		    }
 		}
 		
 		config = YamlConfiguration.loadConfiguration(configFile);
 		 
 		// Look for defaults in the jar
 		InputStream defConfigStream = this.getResource("config.yml");
 		if (defConfigStream != null) {
 		    YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(defConfigStream);
 		    config.setDefaults(defConfig);
 		}
 	}
 	
 	public FileConfiguration getCustomConfig() {
 		if (config == null) {
 	        reloadCustomConfig();
 	    }
 	    return config;
 	}
 	
 	public void saveCustomConfig() {
 		if (config == null || configFile == null) {
 		    return;
 		}
 		try {
 			config.save(configFile);
 		} catch (IOException ex) {
 			log.severe("Could not save config to " + configFile.getPath());
 		}
 	}
 	
 	public void copy(InputStream in, File file) {
 		try {
 			OutputStream out = new FileOutputStream(file);
 			byte[] buf = new byte[1024];
 			int len;
 			while ((len = in.read(buf)) > 0) {
 				out.write(buf, 0, len);
 			}
 			out.close();
 			in.close();
 		} catch (Exception e) {
 			log.severe("Could not copy config from jar!");
 			e.printStackTrace();
 		}
 	}
 	
 	public void resetDurability(ItemStack stack, Player p, boolean fullDecrement) {
 		
 		if(stack == null) 
 			return;
 		
 		if(configuredDurabilities.containsKey(stack.getTypeId())) {
 			
 			makeTag(stack);
 
 			//If that mode is true
 			if(getMode(p)) {
 
 				//Set the item to -128 durability
 				stack.setDurability((short) -128);
 
 				//If that mode is false, proceed as normal 
 			} else {
 
 				//Unless it was set to REALLY unused, then make the durability 0 again
 				if(stack.getDurability() < 0 ) {
 					stack.setDurability((short) 0);
 				}
 
 				/*
 				 * Math
 				 * 
 				 * c = configured max durability 
 				 * a = tagged damage 
 				 * x = bar durability
 				 * m = real max durability 
 				 * 
 				 * (a/c) + (x/m) = 1
 				 * x/m = 1 - (a/c)
 				 * x = [1 - (a/c)]*m
 				 */
 
 				if(fullDecrement) {
 					int cd = getDurability(stack);
 					setTag(stack, cd + 1);
 				}
 
 				stack.setDurability(getRelativeDurability(stack));
 
 			}
 
 		} 
 		
 	}
 	
 	public short getRelativeDurability(ItemStack stack) {
 		float a = getDurability(stack);
 		float c = configuredDurabilities.get(stack.getTypeId());
 		float m = stack.getType().getMaxDurability();
 		short x = (short) Math.round((a / c) * m);
 		
 		if(c <= 0) {
 			return (short) m;
 		}
 		
 		return x;
 		
 	}
 	
 	public net.minecraft.server.ItemStack makeTag(ItemStack stack) {
 		net.minecraft.server.ItemStack nmStack = ((CraftItemStack) stack).getHandle();
 		
 		if(!nmStack.hasTag())
 			nmStack.tag = new NBTTagCompound();
 		
 		return nmStack;
 	}
 	
 	public int getDurability(ItemStack stack) {
 		
 		NBTTagCompound tag = ((CraftItemStack) stack).getHandle().getTag();
 		
 		String key = "NeverBreak";
 		
 		return (tag.hasKey(key)) ? tag.getInt(key) : 0;
 		
 	}
 	
 	public void setTag(ItemStack stack, int value) {
 		
 		NBTTagCompound tag = new NBTTagCompound();
 		net.minecraft.server.ItemStack nmStack = makeTag(stack);
 		
 		tag = nmStack.getTag();
 		tag.setInt("NeverBreak", value);
 		nmStack.setTag(tag);
 		
 	}
 	
 	public HashMap<Integer, Integer> getItems() {
 		List<?> items = config.getList("items");
 		HashMap<Integer, Integer> map = null;
 
 		if(items != null) {
 			
 			map = new HashMap<Integer, Integer>();
 			
 			//Loop for all items from config
 			for(Object o : items) {
 
 				int itemId = -1;
 				int damageInt = -1;
 
 				String input = String.valueOf(o);
 				
 				int dashIndex = input.indexOf("-");
 				
 				try {
 					if(dashIndex == -1) {
						damageInt = 0;
						
 						itemId = Integer.parseInt(input);
 					} else {
 						String itemStr = input.substring(0, dashIndex);
 						String damageStr = input.substring(dashIndex + 1, input.length());
 						
 						itemId = Integer.parseInt(itemStr);
 						damageInt = Integer.parseInt(damageStr);
 					}
 				} catch(NumberFormatException e) {
 					log.info("Invalid config value " + input + ". Make sure it is in the format <item id>-<item damage>");
 				}
 				
 				if(damageInt <= -1 || itemId <= 0) {
 					log.info("Invalid config value " + input + ". Make sure it is in the format <item id>-<item damage>");
 					continue;
 				} else {
 					map.put(itemId, damageInt);
 				}
 
 			}
 			
 		}
 		
 		return map;
 		
 	}
 	
 	private void putDamages(FileConfiguration config) {
 		
 		HashMap<Integer, Integer> mappedValues = getItems();
 		
 		if(mappedValues != null) {
 			configuredDurabilities = mappedValues;
 		}
 		
 	}
 
 	public boolean getMode(Player p) {
 		return mode.get(p);
 	}
 
 	public void setMode(Player p, boolean mode) {
 		this.mode.put(p, mode);
 	}
 	
 	public void toggleMode(Player p) {
		boolean cur = this.mode.get(p);
 		this.mode.put(p, !cur);
 	}
 	
 	public void sendMessage(CommandSender sender, String message) {
 		
 		if(sender == null) 
 			return;
 		
 		Player p = null;
 		
 		if(sender instanceof Player)
 			p = (Player) sender;
 		
 		if(p == null) {
 			message = ChatColor.stripColor(message);
 			log.info(message);
 		} else {
 			p.sendMessage(message);
 		}
 		
 	}
 	
 	public void sendMessage(CommandSender sender, String[] message) {
 		
 		if(sender == null) 
 			return;
 		
 		Player p = null;
 		
 		if(sender instanceof Player)
 			p = (Player) sender;
 		
 		if(p == null) {
 			
 			for(String s : message) {
 				s = ChatColor.stripColor(s);
 				log.info(s);
 			}
 			
 		} else {
 			p.sendMessage(message);
 		}
 		
 	}
 	
 }
