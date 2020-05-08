 package com.wolvencraft.prison.mines.mine;
 
 import com.wolvencraft.prison.region.PrisonRegion;
 import com.wolvencraft.prison.region.PrisonSelection;
 import com.wolvencraft.prison.mines.CommandManager;
 import com.wolvencraft.prison.mines.PrisonMine;
 import com.wolvencraft.prison.mines.generation.BaseGenerator;
 import com.wolvencraft.prison.mines.triggers.BaseTrigger;
 import com.wolvencraft.prison.mines.triggers.CompositionTrigger;
 import com.wolvencraft.prison.mines.triggers.TimeTrigger;
 import com.wolvencraft.prison.mines.util.GeneratorUtil;
 import com.wolvencraft.prison.mines.util.Message;
 import com.wolvencraft.prison.mines.util.Util;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.World;
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.configuration.file.YamlConfiguration;
 import org.bukkit.configuration.serialization.ConfigurationSerializable;
 import org.bukkit.configuration.serialization.SerializableAs;
 import org.bukkit.entity.Player;
 import org.bukkit.event.Listener;
 import org.bukkit.event.player.PlayerTeleportEvent;
 import org.bukkit.material.MaterialData;
 
 import java.io.File;
 import java.io.FileFilter;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.logging.Level;
 
 /**
  * A virtual representation of a cuboid region that is being reset during the runtime
  * @author bitWolfy
  *
  */
 @SerializableAs("Mine")
 public class Mine implements ConfigurationSerializable, Listener {
 
     private String id;
     private String name;
 
     private String parent;
     
     private PrisonRegion region;
     private World world;
     private Location tpPoint;
     
     private List<MineBlock> blocks;
     private Blacklist blockReplaceBlacklist;
     
     private List<BaseTrigger> resetTriggers;
     
     private boolean cooldownEnabled;
     private int cooldownPeriod;
     private long cooldownEndsIn;
     
     private String generator;
     
     private boolean silent;
     
     private boolean warned;
     private List<Integer> warningTimes;
     
     private List<Protection> enabledProtection;
     private PrisonRegion protectionRegion; 
     private Blacklist breakBlacklist;
     private Blacklist placeBlacklist;
     
     private int totalBlocks;
     private int blocksLeft;
 
     /**
      * Standard constructor for new mines
      * @param id Mine ID, specified by player. This will be the name of the mine configuration file.
      * @param region Mine region
      * @param world World where the mine is located
      * @param tpPoint Mine warp location
      * @param generator Generator for the mine
      */
     public Mine(String id, PrisonRegion region, World world, Location tpPoint, String generator) {
     	this.id = id;
     	name = "";
     	
     	parent = null;
     	
     	this.region = region;
     	this.world = world;
     	this.tpPoint = tpPoint;
     	
     	blocks = new ArrayList<MineBlock>();
     	blocks.add(new MineBlock(new MaterialData(Material.AIR), 1.0));
     	blockReplaceBlacklist = new Blacklist();
     	
     	resetTriggers = new ArrayList<BaseTrigger>();
     	
     	cooldownEnabled = false;
     	cooldownPeriod = 0;
     	cooldownEndsIn = 0;
     	
     	this.generator = generator;
     	
     	silent = false;
     	
     	warned = true;
     	warningTimes = new ArrayList<Integer>();
     	
     	enabledProtection = new ArrayList<Protection>();
     	protectionRegion = region.clone();
     	breakBlacklist = new Blacklist();
     	placeBlacklist = new Blacklist();
     	
     	totalBlocks = blocksLeft = region.getBlockCount();
     }
 
     /**
      * Constructor for deserialization from a map
      * @param map Map to deserialize from
      */
 	@SuppressWarnings("unchecked")
 	public Mine(Map<String, Object> map) {
     	id = (String) map.get("id");
     	name = (String) map.get("name");
     	
     	parent = (String) map.get("parent");
     	
     	region = (PrisonRegion) map.get("region");
     	world = Bukkit.getWorld((String) map.get("world"));
     	tpPoint = ((SimpleLoc) map.get("tpPoint")).toLocation();
     	
     	blocks = (List<MineBlock>) map.get("blocks");
     	blockReplaceBlacklist = (Blacklist) map.get("blockReplaceBlacklist");
     	
     	resetTriggers = (List<BaseTrigger>) map.get("resetTriggers");
 
     	cooldownEnabled = ((Boolean) map.get("cooldownEnabled")).booleanValue();
     	cooldownPeriod = ((Integer) map.get("cooldownPeriod")).intValue();
     	cooldownEndsIn = 0;
     	
     	generator = (String) map.get("generator");
     	
     	silent = ((Boolean) map.get("silent")).booleanValue();
     	
     	warned = ((Boolean) map.get("warned")).booleanValue();
     	warningTimes = (List<Integer>) map.get("warningTimes");
     	
     	enabledProtection = (List<Protection>) map.get("enabledProtection");
     	protectionRegion = (PrisonRegion) map.get("protectionRegion");
     	breakBlacklist = (Blacklist) map.get("breakBlacklist");
     	placeBlacklist = (Blacklist) map.get("placeBlacklist");
     	
     	totalBlocks = region.getBlockCount();
     	blocksLeft = ((Integer) map.get("blocksLeft")).intValue();
     }
 	
 	/**
 	 * Serialization method for mine data storage
 	 * @return Serialization map
 	 */
     public Map<String, Object> serialize() {
         Map<String, Object> map = new HashMap<String, Object>();
         map.put("id", id);
         map.put("name", name);
         
         map.put("parent", parent);
         
         map.put("region", region);
         map.put("world", world.getName());
         map.put("tpPoint", new SimpleLoc(tpPoint));
         
         map.put("blocks", blocks);
         map.put("blockReplaceBlacklist", blockReplaceBlacklist);
         
         map.put("resetTriggers", resetTriggers);
         
         map.put("cooldownEnabled", cooldownEnabled);
         map.put("cooldownPeriod", cooldownPeriod);
         
         map.put("generator", generator);
         
         map.put("silent", silent);
         
         map.put("warned", warned);
         map.put("warningTimes", warningTimes);
         
         map.put("enabledProtection", enabledProtection);
         map.put("protectionRegion", protectionRegion);
         map.put("breakBlacklist", breakBlacklist);
         map.put("placeBlacklist", placeBlacklist);
         
         map.put("blocksLeft", blocksLeft);
         return map;
     }
     
     /**
      * Reset the mine according to the rules specified by the generator
      * @param generator Terrain generation rules
      * @return true if successful, false if not
      */
     public boolean reset(String generator) {
         removePlayers();
         BaseGenerator gen = GeneratorUtil.get(generator);
         if(gen == null) {
         	if(CommandManager.getSender() != null) Message.send((Player) CommandManager.getSender(), "Invalid generator selected!");
             else Message.log("Invalid generator selected!");
             return false;
         }
         else return gen.run(this);
     }
     
     public boolean reset() { return reset(generator); }
     
     /**
      * Teleport all the players from the region that is being reset
      * @return true if successful, false if not
      */
     private boolean removePlayers() {
     	if(!PrisonMine.getSettings().TPONRESET) return true;
         for (Player p : world.getPlayers()) {
             if (region.isLocationInRegion(p.getLocation())) {
                 p.teleport(tpPoint, PlayerTeleportEvent.TeleportCause.PLUGIN);
                 Message.sendSuccess(p, Util.parseVars(PrisonMine.getLanguage().MISC_TELEPORT, this));
             }
         }
         return true;
     }
     
     public String getId()							{ return id; }
     public String getName() 						{ if(name.equalsIgnoreCase("")) return id; else return name; }
     
     public boolean hasParent()						{ return (parent != null); }
     public String getParent() 						{ return parent; }
     public Mine getSuperParent()					{ return getSuperParent(this); }
     
     public PrisonRegion getRegion() 				{ return region;}
     public World getWorld() 						{ return world; }
     public Location getTpPoint() 					{ return tpPoint; }
 
     public List<MineBlock> getBlocks()				{ return blocks; }
     public Blacklist getBlacklist() 				{ return blockReplaceBlacklist; }
     
     public boolean getCooldown() 					{ return cooldownEnabled; }
     public int getCooldownPeriod() 					{ return cooldownPeriod; }
     public int getCooldownEndsIn() 					{ return (int)(cooldownEndsIn / 20); }
     
     public boolean getSilent() 						{ return silent; }
     
     public String getGenerator() 					{ return generator.toUpperCase(); }
     
     public boolean getWarned()						{ return warned; }
     public List<Integer> getWarningTimes()			{ return warningTimes; }
     
     public List<Protection> getProtection() 		{ return enabledProtection; }
     public PrisonRegion getProtectionRegion() 		{ return protectionRegion; }
     public Blacklist getBreakBlacklist() 			{ return breakBlacklist; }
     public Blacklist getPlaceBlacklist() 			{ return placeBlacklist; }
     
     
     public void setName(String name) 								{ this.name = name; }
     public void setParent(String parent) 							{ this.parent = parent; }
     public void setRegion(PrisonSelection sel)						{ this.region = null; this.region = new PrisonRegion(sel); }
     public void setTpPoint(Location tpPoint) 						{ this.tpPoint = tpPoint; }
     public void setSilent(boolean silent) 							{ this.silent = silent; }
     
     public void setCooldownEnabled(boolean cooldownEnabled) 		{ this.cooldownEnabled = cooldownEnabled; }
     public void setCooldownPeriod (int cooldownPeriod) 				{ this.cooldownPeriod = cooldownPeriod; }
     public void updateCooldown(long ticks) 							{ cooldownEndsIn -= ticks; }
     public void resetCooldown() 									{ cooldownEndsIn = cooldownPeriod * 20; }
 
     public void setGenerator(String generator) 						{ this.generator = generator; }
     
     public void setWarned(boolean warned) 							{ this.warned = warned; }
     
 	
     // Triggers
     public List<BaseTrigger> getTriggers()			{ return resetTriggers; }
     
     public BaseTrigger getTrigger(String triggerId)		{
     	for(BaseTrigger trigger : resetTriggers) {
     		if(trigger.getId().equalsIgnoreCase(triggerId)) return trigger;
     	}
     	return null;
     }
     
     public boolean removeTrigger(String triggerId) {
     	for(BaseTrigger trigger : resetTriggers) {
     		if(trigger.getId().equalsIgnoreCase(triggerId)) return resetTriggers.remove(trigger);
     	}
     	return false;
     }
     
     public boolean getAutomaticReset() { return (getTrigger("time") != null); }
     
     public int getResetPeriod() 	{ return ((TimeTrigger)(getTrigger("time"))).getPeriod(); }
     public int getResetPeriodSafe() { return getResetPeriodSafe(this); }
     public int getResetsIn() 		{ return ((TimeTrigger)(getTrigger("time"))).getNext(); }
     public int getResetsInSafe() 	{ return getResetsInSafe(this); }
     
     public void setResetPeriod(int period)	{ ((TimeTrigger)(getTrigger("time"))).setPeriod(period); }
     public boolean setAutomaticReset(boolean state) {
     	if(state) {
     		if(getAutomaticReset()) return false;
     		resetTriggers.add(new TimeTrigger(this, 900));
     	}
     	else {
     		if(!getAutomaticReset()) return false;
     		getTrigger("time").cancel();
     	}
     	return true;
     }
     
     public boolean getCompositionReset() { return (getTrigger("composition") != null); }
     
     public int getBlocksLeft() 		{ return blocksLeft; }
     public int getTotalBlocks() 	{ return totalBlocks; }
     
     public void updateBlocksLeft()	{ blocksLeft--; }
     public void resetBlocksLeft() 	{ blocksLeft = totalBlocks; }
     
     public double getCompositionPercent() { return ((CompositionTrigger)(getTrigger("composition"))).getPercent(); }
     
     public void setCompositionPercent(double percent) { ((CompositionTrigger)(getTrigger("composition"))).setPercent(percent); }
     
     public boolean setCompositionReset(boolean state) {
     	if(state) {
     		if(getCompositionReset()) return false;
    		resetTriggers.add(new CompositionTrigger(this, 0));
     	}
     	else {
     		if(!getCompositionReset()) return false;
     		getTrigger("composition").cancel();
     		resetTriggers.remove(getTrigger("composition"));
     	}
     	return true;
     }
     
     // Everything else
     
 	public List<Mine> getChildren() {
 		List<Mine> children = new ArrayList<Mine>();
 		for(Mine mine : PrisonMine.getMines()) {
 			if(mine.hasParent() && mine.getParent().equalsIgnoreCase(getId())) { children.add(mine); }
 		}
 		return children;
 	}
 	
 	public MineBlock getMostCommonBlock() {
 		MineBlock mostCommon = blocks.get(0);
 		for(MineBlock curBlock : blocks) {
 			if(curBlock.getChance() > mostCommon.getChance()) mostCommon = curBlock;
 		}
 		return mostCommon;
 	}
 	
 	public MineBlock getBlock(MaterialData block) {
 		if(block == null) return null;
 		for(MineBlock thisBlock : blocks) { if(thisBlock.getBlock().equals(block)) return thisBlock; }
 		return null;
 	}
 	
 	public List<String> getBlocksSorted() {
 		List<String> finalList = new ArrayList<String>(blocks.size());
 		
 		MineBlock tempBlock;
 		for(int j = blocks.size(); j > 0; j--) {
 			for(int i = 0; i < (j - 1); i++) {
 				if(blocks.get(i + 1).getChance() > blocks.get(i).getChance()) {
 					tempBlock = blocks.get(i).clone();
 					blocks.set(i, blocks.get(i + 1).clone());
 					blocks.set(i + 1, tempBlock.clone());
 				}
 				
 			}
 		}
 		
 		for(MineBlock block : blocks) {
 			String blockName = block.getBlock().getItemType().toString().toLowerCase().replace("_", " ");
 			if(block.getBlock().getData() != 0) {
 				String[] tempBlockName = {block.getBlock().getItemTypeId() + "", block.getBlock().getData() + ""};
 				blockName = Util.parseMetadata(tempBlockName, true) + " " + blockName;
 			}
 			String blockWeight = Util.round(block.getChance());
 			
 			if(!blockWeight.equalsIgnoreCase("0.0%"))
 				finalList.add(ChatColor.WHITE + blockWeight + " " + ChatColor.GREEN + blockName + ChatColor.WHITE);
 		}
 		
 		return finalList;
 	}
 	
 	private static int getResetsInSafe(Mine curMine) {
 		if(!curMine.hasParent()) return curMine.getResetsIn();
 		else return getResetsInSafe(get(curMine.getParent()));
 	}
 
 	private static int getResetPeriodSafe(Mine curMine) {
 		if(curMine.getParent() == null) return curMine.getResetPeriod();
 		else return getResetPeriodSafe(get(curMine.getParent()));
 	}
 	
 	private static Mine getSuperParent(Mine curMine) {
 		if(!curMine.hasParent()) return curMine;
 		return getSuperParent(get(curMine.getParent()));
 	}
 	
 	public static Mine get(String id) {
 		for(Mine curMine : PrisonMine.getMines()) {
 			if(curMine.getName().equalsIgnoreCase(id)) return curMine;
 		}
 		return null;
 	}
 	
 	public boolean save() {
 		File mineFile = new File(new File(CommandManager.getPlugin().getDataFolder(), "mines"), id + ".yml");
         FileConfiguration mineConf =  YamlConfiguration.loadConfiguration(mineFile);
         mineConf.set("mine", this);
         try {
             mineConf.save(mineFile);
         } catch (IOException e) {
         	Message.log(Level.SEVERE, "Unable to serialize mine '" + id + "'!");
             e.printStackTrace();
             return false;
         }
         return true;
 	}
 	
 	public boolean delete() {
 		File mineFolder = new File(CommandManager.getPlugin().getDataFolder(), "mines");
 		if(!mineFolder.exists() || !mineFolder.isDirectory()) return false;
 		
 		File[] mineFiles = mineFolder.listFiles(new FileFilter() {
             public boolean accept(File file) { return file.getName().contains(".yml"); }
         });
 		
 		for(File mineFile : mineFiles) {
 			if(mineFile.getName().equals(id + ".yml")) {
 				return mineFile.delete();
 			}
 		}
 		
 		return false;
 	}
 }
