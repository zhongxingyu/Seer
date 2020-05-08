 package com.adamki11s.npcs.io;
 
 import java.io.File;
 
 import org.bukkit.ChatColor;
 import org.bukkit.inventory.ItemStack;
 
 import com.adamki11s.data.ItemStackDrop;
 import com.adamki11s.data.ItemStackProbability;
 import com.adamki11s.exceptions.MissingPropertyException;
 import com.adamki11s.io.FileLocator;
 import com.adamki11s.io.NPCTag;
 import com.adamki11s.questx.QuestX;
 import com.adamki11s.sync.io.configuration.SyncConfiguration;
 
 public class LoadNPCTemplate {
 
 	final String npc_root = FileLocator.npc_data_root;
 	final File folder;
 
 	final String name;
 
 	public LoadNPCTemplate(String name) {
 		this.name = name;
 		this.folder = new File(npc_root + File.separator + name);
 	}
 
 	boolean unload = false;
 	String inventDrops, gear;
 
 	ItemStackDrop itemStackDrop;
 	ItemStack[] npcGear;
 
 	NPCTemplate npcTemplate;
 
 	boolean moveable, attackable, load;
 	int minPauseTicks, maxPauseTicks, maxVariation, respawnTicks, maxHealth, damageMod;
 	double retalliationMultiplier;
 
 	public boolean wantsToLoad() {
 		File prop = new File(folder + File.separator + FileLocator.propertyFile);
 		if (prop.exists()) {
 			SyncConfiguration conf = new SyncConfiguration(prop);
 			conf.read();
 			return conf.getBoolean(NPCTag.LOAD.toString());
 		}
 		return false;
 	}
 
 	public NPCTemplate getLoadedNPCTemplate() {
 		return this.npcTemplate;
 	}
 
 	public void loadProperties() throws MissingPropertyException{
 		File prop = FileLocator.getNPCPropertiesFile(name);
 		// File prop = new File(folder + File.separator +
 		// FileLocator.propertyFile);
 		if (prop.exists()) {
 			SyncConfiguration conf = new SyncConfiguration(prop);
 			conf.read();
 
 			if (conf.doesKeyExist(NPCTag.MOVEABLE.toString())) {
 				this.moveable = conf.getBoolean(NPCTag.MOVEABLE.toString());
 			} else {
 				throw new MissingPropertyException(name, NPCTag.MOVEABLE.toString());
 			}
 
 			if (conf.doesKeyExist(NPCTag.ATTACKABLE.toString())) {
 				this.attackable = conf.getBoolean(NPCTag.ATTACKABLE.toString());
 			} else {
 				throw new MissingPropertyException(name, NPCTag.ATTACKABLE.toString());
 			}
 			
 			if (conf.doesKeyExist(NPCTag.MIN_PAUSE_TICKS.toString())) {
 				this.minPauseTicks = conf.getInt(NPCTag.MIN_PAUSE_TICKS.toString());
 			} else {
 				throw new MissingPropertyException(name, NPCTag.MIN_PAUSE_TICKS.toString());
 			}
 
 			if (conf.doesKeyExist(NPCTag.MAX_PAUSE_TICKS.toString())) {
 				this.maxPauseTicks = conf.getInt(NPCTag.MAX_PAUSE_TICKS.toString());
 			} else {
 				throw new MissingPropertyException(name, NPCTag.MAX_PAUSE_TICKS.toString());
 			}
 
 			if (conf.doesKeyExist(NPCTag.MAX_VARIATION.toString())) {
 				this.maxVariation = conf.getInt(NPCTag.MAX_VARIATION.toString());
 			} else {
 				throw new MissingPropertyException(name, NPCTag.MAX_VARIATION.toString());
 			}
 
 			if (conf.doesKeyExist(NPCTag.RESPAWN_TICKS.toString())) {
 				this.respawnTicks = conf.getInt(NPCTag.RESPAWN_TICKS.toString());
 			} else {
 				throw new MissingPropertyException(name, NPCTag.RESPAWN_TICKS.toString());
 			}
 
 			if (conf.doesKeyExist(NPCTag.MAX_HEALTH.toString())) {
 				this.maxHealth = conf.getInt(NPCTag.MAX_HEALTH.toString());
 			} else {
 				throw new MissingPropertyException(name, NPCTag.MAX_HEALTH.toString());
 			}
 
 			if (conf.doesKeyExist(NPCTag.DAMAGE_MODIFIER.toString())) {
 				this.damageMod = conf.getInt(NPCTag.DAMAGE_MODIFIER.toString());
 			} else {
 				throw new MissingPropertyException(name, NPCTag.DAMAGE_MODIFIER.toString());
 			}
 
 			if (conf.doesKeyExist(NPCTag.RETALLIATION_MULTIPLIER.toString())) {
 				this.retalliationMultiplier = conf.getDouble(NPCTag.RETALLIATION_MULTIPLIER.toString());
 			} else {
 				throw new MissingPropertyException(name, NPCTag.RETALLIATION_MULTIPLIER.toString());
 			}
 			
 			if (conf.doesKeyExist(NPCTag.INVENTORY_DROPS.toString())) {
 				this.inventDrops = conf.getString(NPCTag.INVENTORY_DROPS.toString());// id,data,quantity,chance#
 			} else {
 				throw new MissingPropertyException(name, NPCTag.INVENTORY_DROPS.toString());
 			}
 			
 			if (conf.doesKeyExist(NPCTag.GEAR.toString())) {
 				this.gear = conf.getString(NPCTag.GEAR.toString());// boots,legs,chest,helm,arm
 			} else {
 				throw new MissingPropertyException(name, NPCTag.GEAR.toString());
 			}
 
 			String[] inventDropsToParse = this.inventDrops.split("#");
 
 			ItemStackProbability[] ispDrops = new ItemStackProbability[inventDropsToParse.length];
 
 			for (int i = 0; i < inventDropsToParse.length; i++) {
 				ispDrops[i] = this.parseISP(inventDropsToParse[i]);
 			}
 
 			this.itemStackDrop = new ItemStackDrop(ispDrops);
 			this.npcGear = this.parseGear(gear);
 
 			if (!unload) {
 				// create template npc
 				this.npcTemplate = new NPCTemplate(this.name, moveable, attackable, minPauseTicks, maxPauseTicks, maxVariation, maxHealth, respawnTicks, itemStackDrop, npcGear, damageMod,
 						retalliationMultiplier);
 			}
 
 		} else {
 			QuestX.logMSG("Could not locate properties file for NPC '" + name + "'.");
 		}
 	}
 
 	// public ItemStackProbability(ItemStack is, int probability){
 
 	ItemStack[] parseGear(String gear) {
 		String[] parts = gear.split(",");
 		ItemStack[] g = new ItemStack[5];
 		try {
 			for (int i = 0; i < 5; i++) {
 				g[i] = new ItemStack(Integer.parseInt(parts[i]));
 			}
 		} catch (NumberFormatException nfe) {
 			QuestX.logMSG("NPC " + this.name + " has invalid data in field 'GEAR'.");
 			QuestX.logMSG("NPC " + this.name + " unloaded.");
 			unload = true;
 		}
 		return g;
 	}
 
 	ItemStackProbability parseISP(String isp) {
 		String[] parts = isp.split(",");
 		int id = 0, data = 0, quantity = 0, probability = 0;
 		try {
 			id = Integer.parseInt(parts[0]);
 			data = Integer.parseInt(parts[1]);
 			quantity = Integer.parseInt(parts[2]);
 			probability = Integer.parseInt(parts[3]);
 		} catch (NumberFormatException nfe) {
 			QuestX.logMSG("NPC " + this.name + " has invalid data in field 'INVENTORY_DROPS'.");
 			QuestX.logMSG("NPC " + this.name + " unloaded.");
 			unload = true;
 		}
 		if (probability < 0 || probability > 10000) {
 			QuestX.logMSG("NPC " + this.name + " has invalid data in field 'INVENTORY_DROPS'. Probability must be between 0-10000");
 			QuestX.logMSG("NPC " + this.name + " unloaded.");
 			unload = true;
 		}
 
 		ItemStack drop = new ItemStack(id, quantity);
 		drop.getData().setData((byte) data);
 		return new ItemStackProbability(drop, probability);
 	}
 
 }
