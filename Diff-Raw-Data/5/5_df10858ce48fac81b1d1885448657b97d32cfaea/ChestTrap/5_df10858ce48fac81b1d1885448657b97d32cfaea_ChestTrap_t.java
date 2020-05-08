 package com.em.chesttrap;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.block.Block;
 import org.bukkit.block.BlockState;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.InventoryHolder;
 import org.bukkit.plugin.java.JavaPlugin;
 import org.yaml.snakeyaml.Yaml;
 
 public class ChestTrap extends JavaPlugin {
 
 	ChestTrapListener thelistener = new ChestTrapListener(this);
 
 	// Properties
 	Map<Location, ChestTrapContent> chestMap = new HashMap<Location, ChestTrapContent>();
 
 	// Constant
 	private static final List<Material> BLOCK_TYPES = new ArrayList<Material>();
 	static {
 		BLOCK_TYPES.add(Material.CHEST);
 		BLOCK_TYPES.add(Material.FURNACE);
 		BLOCK_TYPES.add(Material.DISPENSER);
 	}
 
 	public void onDisable() {
 		saveDatas();
 	}
 
 	/**
 	 * save Datas to configuration file
 	 */
 	private void saveDatas() {
 		// reload the configuration
 		reloadConfig();
 
 		// Construct the data
 		Map<String, String> chestMapS = new HashMap<String, String>();
 		for (Location l : this.chestMap.keySet()) {
 			chestMapS.put(convertLocation(l), this.chestMap.get(l).getSort().toString());
 		}
 		String chests = new Yaml().dump(chestMapS);
 
 		// if data change, update and save
 		if (!chests.equals(getConfig().getString("data", "{}"))) {
 			getConfig().set("data", chests);
 
 			saveConfig();
 		}
 	}
 
 	public void onEnable() {
 
 		getServer().getPluginManager().registerEvents(this.thelistener, this);
 
 		@SuppressWarnings("unchecked")
 		HashMap<String, String> chestMapS = (HashMap<String, String>) new Yaml().loadAs(getConfig().getString("data", "{}"), HashMap.class);
 		for (String s : chestMapS.keySet()) {
 			Location l = convertString(s);
 			String sortS = chestMapS.get(s);
 
 			BlockState blockState = l.getBlock().getState();
 			if (blockState instanceof InventoryHolder) {
 				InventoryHolder ih = (InventoryHolder) blockState;
 
 				this.chestMap.put(l, new ChestTrapContent(ih.getInventory(), sortS));
 			}
 		}
 
 		getLogger().info(chestMap.size() + " chests");
 	}
 
 	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
 		if (!sender.hasPermission("chesttrap.create")) {
 			return false;
 		}
 		try {
 			Player player = (Player) sender;
 			Block block = getPlayerTargetBlock(player);
 
 			// Wrong pointed block
 			if (!BLOCK_TYPES.contains(block.getType())) {
 				sender.sendMessage(ChatColor.RED + "This " + block.getType().toString().toLowerCase() + "is not in the list : " + getBlockTypesAsString());
 				return true;
 			}

 			// Already a chestTrap
 			if (this.chestMap.containsKey(block.getLocation())) {
 				ChestTrapContent content = this.chestMap.get(block.getLocation());
 
 				// Change "sort" if needed
 				String sortS = "";
 				if (args.length != 0) {
 					sortS = args[0].toUpperCase();
 				}
 				content.setSort(sortS);
 
 				sender.sendMessage(ChatColor.RED + "That " + block.getType().toString().toLowerCase() + " is already a chesttrap " + ChatColor.GREEN + "(" + content.getSortLabel() + ", "
 						+ content.contentToText() + ")");
 				return true;
 			}
 
 			// Create a chestTrap
 			BlockState blockState = block.getState();
 			if (blockState instanceof InventoryHolder) {
 				InventoryHolder ih = (InventoryHolder) blockState;
 
 				String sortS = "";
 				if (args.length != 0) {
 					sortS = args[0].toUpperCase();
 				}
 				this.chestMap.put(block.getLocation(), new ChestTrapContent(ih.getInventory(), sortS));
 				saveDatas();
 
				sender.sendMessage(ChatColor.GREEN + "Chesttrap created (" + this.chestMap.get(block.getLocation()).getSortLabel() + ")");
 			}
 
 		} catch (ClassCastException e) {
 			sender.sendMessage("You can only use this command as a player!");
 		}
 		return true;
 	}
 
 	String convertLocation(Location l) {
 		String out = "";
 		out = out + l.getWorld().getName() + ",";
 		out = out + l.getBlockX() + ",";
 		out = out + l.getBlockY() + ",";
 		out = out + l.getBlockZ();
 		return out;
 	}
 
 	Location convertString(String s) {
 		String[] parts = s.split(",");
 		return new Location(getServer().getWorld(parts[0]), Integer.parseInt(parts[1]), Integer.parseInt(parts[2]), Integer.parseInt(parts[3]));
 	}
 
 	/**
 	 * Get list of allowed block as String (to be trace of put in messaged)
 	 * 
 	 * @return
 	 */
 	String getBlockTypesAsString() {
 		String ret = "";
 
 		for (Material m : BLOCK_TYPES) {
 			ret += ", " + m.toString().toLowerCase();
 		}
 
 		ret.replaceFirst(", ", "");
 
 		return ret;
 	}
 
 	/**
 	 * Get the targeted Block
 	 * 
 	 * @param player
 	 * @return
 	 */
 	private Block getPlayerTargetBlock(Player player) {
 		Block block = player.getTargetBlock(TRANSPARENT, 5);
 		return block;
 	}
 
 	// define transparent blocks id
 	private static final HashSet<Byte> TRANSPARENT = new HashSet<Byte>();
 	static {
 		TRANSPARENT.add((byte) Material.AIR.getId());
 		TRANSPARENT.add((byte) Material.FENCE.getId());
 		TRANSPARENT.add((byte) Material.FENCE_GATE.getId());
 		TRANSPARENT.add((byte) Material.DETECTOR_RAIL.getId());
 		TRANSPARENT.add((byte) Material.POWERED_RAIL.getId());
 		TRANSPARENT.add((byte) Material.RAILS.getId());
 		TRANSPARENT.add((byte) Material.REDSTONE_WIRE.getId());
 		TRANSPARENT.add((byte) Material.TORCH.getId());
 	}
 
 }
