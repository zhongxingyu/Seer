 package net.roguedraco.infobutton;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.Set;
 import java.util.logging.Level;
 
 import org.bukkit.Bukkit;
 import org.bukkit.Location;
 import org.bukkit.block.Block;
 import org.bukkit.configuration.ConfigurationSection;
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.configuration.file.YamlConfiguration;
 
 
 public class InfoButtons {
 
 	public static Set<InfoButton> infoButtons = new HashSet<InfoButton>();
 	
 	private static File confFile = null;
 	private static FileConfiguration conf = null;
 	
 	public static boolean isButton(Block block) {
 		
 		if(InfoButtonPlugin.getPlugin().getConfig().getIntegerList("buttonBlocks").contains(block.getTypeId())) {
 			for(InfoButton button : infoButtons) {
 				Location loc = button.getLocation();
 				Location bloc = block.getLocation();
 				if(loc.getX() == bloc.getX() && loc.getY() == bloc.getY() && loc.getZ() == bloc.getZ() && loc.getWorld() == bloc.getWorld()) {
 					return true;
 				}
 			}
 		}
 		return false;
 	}
 	
 	public static InfoButton getButton(Block block) {
 		if(InfoButtonPlugin.getPlugin().getConfig().getIntegerList("buttonBlocks").contains(block.getTypeId())) {
 			for(InfoButton button : infoButtons) {
 				Location loc = button.getLocation();
 				Location bloc = block.getLocation();
 				if(loc.getX() == bloc.getX() && loc.getY() == bloc.getY() && loc.getZ() == bloc.getZ()) {
 					return button;
 				}
 			}
 		}
 		return null;
 	}
 	
 	public static void deleteButton(Block block) {
 		if(InfoButtonPlugin.getPlugin().getConfig().getIntegerList("buttonBlocks").contains(block.getTypeId())) {
 			for(InfoButton button : infoButtons) {
 				Location loc = button.getLocation();
 				Location bloc = block.getLocation();
 				if(loc.getX() == bloc.getX() && loc.getY() == bloc.getY() && loc.getZ() == bloc.getZ()) {
 					infoButtons.remove(button);
 				}
 			}
 		}
 	}
 	
 	public static void addButton(InfoButton button) {
 		infoButtons.add(button);
 	}
 	
 	public static void saveButtons() {
 		confFile = new File("plugins/InfoButton/", "buttons.yml");
 		conf = YamlConfiguration.loadConfiguration(confFile);
 		
 		Map<String, Integer> worldKeys = new HashMap<String, Integer>();
 		
 		for(InfoButton button : infoButtons) {
 			Location bloc = button.getLocation();
 			String world = bloc.getWorld().getName();
 			Integer worldKey = 0;
 			if(worldKeys.containsKey(world)) {
 				worldKey = worldKeys.get(world);
 			}
 			
 			conf.set(world+"."+worldKey+".location.x", bloc.getBlockX());
 			conf.set(world+"."+worldKey+".location.y", bloc.getBlockY());
 			conf.set(world+"."+worldKey+".location.z", bloc.getBlockZ());
 			conf.set(world+"."+worldKey+".enabled", button.isEnabled());
 			conf.set(world+"."+worldKey+".permission", button.getPermission());
 			conf.set(world+"."+worldKey+".price", button.getPrice());
 			
 			Integer x = 0;
 			for(ButtonAction action : button.getActions()) {
				conf.set(world+"."+worldKey+".actions."+x+".type", action.getType());
 				conf.set(world+"."+worldKey+".actions."+x+".val", action.getValue());				
 				x++;
 			}
 			
 			worldKeys.put(world, (worldKey+1));
 		}
 		try {
 			conf.save(confFile); // Save the file
 		} catch (IOException ex) {
 			InfoButtonPlugin.log(Level.SEVERE, "Could not save config to "
 					+ confFile);
 		}
 	}
 	
 	public static void loadButtons() {
 		confFile = new File("plugins/InfoButton/", "buttons.yml");
 		conf = YamlConfiguration.loadConfiguration(confFile);
 		
 		Iterator<String> worlds = conf.getKeys(false).iterator();
 		while(worlds.hasNext()) {
 			String worldName = worlds.next();
 			ConfigurationSection world = conf.getConfigurationSection(worldName);
 			Iterator<String> buttons = world.getKeys(false).iterator();
 			while(buttons.hasNext()) {
 				String id = buttons.next();
 				Block block = Bukkit.getServer().getWorld(worldName).getBlockAt(world.getInt(id+".location.x"), world.getInt(id+".location.y"), world.getInt(id+".location.z"));
 				InfoButton ib = new InfoButton(block);
 				ib.setEnabled(world.getBoolean(id+".enabled",true));
 				ib.setPermission(world.getString(id+".permission",""));
 				ib.setPrice(world.getDouble(id+".price",0.00));
 				
 				ConfigurationSection actionsSection = world.getConfigurationSection(".actions");
 				Iterator<String> actions = actionsSection.getKeys(false).iterator();
 				while(actions.hasNext()) {
 					String actionId = actions.next();
 					ActionType actionType = ActionType.getType(actionsSection.getString(actionId+".type","PLAYER_COMMAND"));
 					String actionValue = actionsSection.getString(actionId+".val");
 					ButtonAction action = new ButtonAction(actionType,actionValue);
 					ib.addAction(action);
 				}
 			}
 		}
 		
 	}
 	
 }
