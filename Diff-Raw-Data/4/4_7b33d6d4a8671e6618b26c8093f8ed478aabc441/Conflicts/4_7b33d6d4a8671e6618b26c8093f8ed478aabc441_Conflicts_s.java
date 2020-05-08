 package com.turt2live.antishare;
 
 import org.bukkit.plugin.Plugin;
 
 public class Conflicts {
 
 	public boolean INVENTORY_CONFLICT_PRESENT = false;
 	public boolean CREATIVE_MANAGER_CONFLICT_PRESENT = false;
 	public boolean WORLD_MANAGER_CONFLICT_PRESENT = false;
 	public boolean OTHER_CONFLICT_PRESENT = false;
 
 	public String INVENTORY_CONFLICT = "None";
 	public String CREATIVE_MANAGER_CONFLICT = "None";
 	public String WORLD_MANAGER_CONFLICT = "None";
 	public String OTHER_CONFLICT = "None";
 
 	public Conflicts(AntiShare plugin){
 		findInventoryManagerConflicts(plugin.getServer().getPluginManager().getPlugins());
 		findCreativeModeManagerConflicts(plugin.getServer().getPluginManager().getPlugins());
 		findWorldManagerConflicts(plugin.getServer().getPluginManager().getPlugins());
 		findOtherConflicts(plugin.getServer().getPluginManager().getPlugins());
 		if(INVENTORY_CONFLICT_PRESENT){
 			AntiShare.log.severe("[AntiShare-Conflicts] Inventory Manager Conflict: " + INVENTORY_CONFLICT);
 			AntiShare.log.severe("[AntiShare-Conflicts] AntiShare will not deal with inventories.");
 		}
 		if(CREATIVE_MANAGER_CONFLICT_PRESENT){
 			AntiShare.log.severe("[AntiShare-Conflicts] Creative Mode Manager Conflict: " + CREATIVE_MANAGER_CONFLICT);
 			AntiShare.log.severe("[AntiShare-Conflicts] AntiShare will disable itself.");
 			plugin.getServer().getPluginManager().disablePlugin(plugin);
 		}
 		if(WORLD_MANAGER_CONFLICT_PRESENT){
 			AntiShare.log.severe("[AntiShare-Conflicts] World Manager Conflict: " + WORLD_MANAGER_CONFLICT);
 			AntiShare.log.severe("[AntiShare-Conflicts] AntiShare will not deal with allowance of world transfers.");
 		}
 		if(OTHER_CONFLICT_PRESENT){
 			AntiShare.log.severe("[AntiShare-Conflicts] Other Conflict: " + OTHER_CONFLICT);
 			AntiShare.log.severe("[AntiShare-Conflicts] AntiShare won't do anything, but there may be problems.");
 		}
 	}
 
 	private void findInventoryManagerConflicts(Plugin[] plugins){
 		for(Plugin plugin : plugins){
 			String name = plugin.getName();
 			if(name.equalsIgnoreCase("ClearInv")){
 				INVENTORY_CONFLICT_PRESENT = true;
 				INVENTORY_CONFLICT = name;
 				break;
 			}else if(name.equalsIgnoreCase("ChangeDat")){
 				INVENTORY_CONFLICT_PRESENT = true;
 				INVENTORY_CONFLICT = name;
 				break;
 			}else if(name.equalsIgnoreCase("MultiInv") || name.equalsIgnoreCase("Multiverse-Inventories")){
 				INVENTORY_CONFLICT_PRESENT = true;
 				INVENTORY_CONFLICT = name;
 				break;
 			}else if(name.equalsIgnoreCase("Multiworld")){
 				INVENTORY_CONFLICT_PRESENT = true;
 				INVENTORY_CONFLICT = name;
 				break;
 			}else if(name.equalsIgnoreCase("AutoGamemode")){
 				INVENTORY_CONFLICT_PRESENT = true;
 				INVENTORY_CONFLICT = name;
 				break;
 			}
 		}
 	}
 
 	private void findCreativeModeManagerConflicts(Plugin[] plugins){
 		for(Plugin plugin : plugins){
 			String name = plugin.getName();
 			if(name.equalsIgnoreCase("CreativeControl")){
 				CREATIVE_MANAGER_CONFLICT_PRESENT = true;
 				CREATIVE_MANAGER_CONFLICT = name;
 				break;
 			}else if(name.equalsIgnoreCase("BurningCreativeSuite") || name.equalsIgnoreCase("BurningCreative") || name.equalsIgnoreCase("BCS")){
 				CREATIVE_MANAGER_CONFLICT_PRESENT = true;
 				CREATIVE_MANAGER_CONFLICT = name;
 				break;
 			}else if(name.equalsIgnoreCase("LimitedCreative")){
 				CREATIVE_MANAGER_CONFLICT_PRESENT = true;
 				CREATIVE_MANAGER_CONFLICT = name;
 				break;
 			}else if(name.equalsIgnoreCase("Anti-Place-Destroy-Ignite") || name.equalsIgnoreCase("APDI")){
 				CREATIVE_MANAGER_CONFLICT_PRESENT = true;
 				CREATIVE_MANAGER_CONFLICT = name;
 				break;
 			}else if(name.equalsIgnoreCase("iCreative")){
 				CREATIVE_MANAGER_CONFLICT_PRESENT = true;
 				CREATIVE_MANAGER_CONFLICT = name;
 				break;
 			}else if(name.equalsIgnoreCase("GameModeNoPlace")){
 				CREATIVE_MANAGER_CONFLICT_PRESENT = true;
 				CREATIVE_MANAGER_CONFLICT = name;
 				break;
 			}else if(name.equalsIgnoreCase("NoDrop")){
 				CREATIVE_MANAGER_CONFLICT_PRESENT = true;
 				CREATIVE_MANAGER_CONFLICT = name;
 				break;
 			}
 		}
 	}
 
 	private void findWorldManagerConflicts(Plugin[] plugins){
 		for(Plugin plugin : plugins){
 			String name = plugin.getName();
 			if(name.toLowerCase().contains("multiverse")){ // Will likely have MV-Core
 				WORLD_MANAGER_CONFLICT_PRESENT = true;
 				WORLD_MANAGER_CONFLICT = "Multiverse";
 				break;
 			}else if(name.equalsIgnoreCase("Multiworld")){
 				OTHER_CONFLICT_PRESENT = true;
 				OTHER_CONFLICT = name;
 				break;
 			}
 		}
 	}
 
 	private void findOtherConflicts(Plugin[] plugins){
 		for(Plugin plugin : plugins){
 			String name = plugin.getName();
 			if(name.equalsIgnoreCase("AntiGrief")){
 				OTHER_CONFLICT_PRESENT = true;
 				OTHER_CONFLICT = name;
 				break;
 			}else if(name.equalsIgnoreCase("MobEggs")){
 				OTHER_CONFLICT_PRESENT = true;
 				OTHER_CONFLICT = name;
 				break;
 			}else if(name.equalsIgnoreCase("WorldGuard")){
 				OTHER_CONFLICT_PRESENT = true;
 				OTHER_CONFLICT = name;
 				break;
 			}else if(name.equalsIgnoreCase("CreativeStick")){
 				OTHER_CONFLICT_PRESENT = true;
 				OTHER_CONFLICT = name;
 				break;
 			}else if(name.equalsIgnoreCase("Regios")){
 				OTHER_CONFLICT_PRESENT = true;
 				OTHER_CONFLICT = name;
 				break;
 			}else if(name.toLowerCase().startsWith("voxel")){
 				OTHER_CONFLICT_PRESENT = true;
 				OTHER_CONFLICT = name;
 				break;
 			}
 		}
 	}
 
 }
