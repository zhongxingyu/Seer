 package com.norcode.bukkit.jukeloop;
 
 import java.util.HashMap;
 import java.util.LinkedHashMap;
 
 import org.bukkit.Effect;
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.block.Block;
 import org.bukkit.block.BlockFace;
 import org.bukkit.block.BlockState;
 import org.bukkit.block.Chest;
 import org.bukkit.block.Jukebox;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.ItemStack;
 
 public class LoopingJukebox {
 	private Location location;
 	private Jukebox jukebox;
 	private Chest chest;
 	private JukeLoopPlugin plugin;
 	private int startedAt = -1;
 	public boolean isDead = false;
 	
 	public static LinkedHashMap<Location, LoopingJukebox> jukeboxMap = new LinkedHashMap<Location, LoopingJukebox>();
 	public static LoopingJukebox getAt(JukeLoopPlugin plugin, Location loc) {
 		LoopingJukebox box = null;
 		if (jukeboxMap.containsKey(loc)) {
 			box = jukeboxMap.get(loc);
 		} else {
 			box = new LoopingJukebox(plugin, loc);
 		}
 		if (box.validate()) {
 			jukeboxMap.put(loc, box);
 			return box;
 		}
 		return null;
 	}
 	
 	public Location getLocation() {
 		return location;
 	}
 	public LoopingJukebox(JukeLoopPlugin plugin, Location location) {
 		this.location = location;
 		this.plugin = plugin;
 	}
 	
 	public void log(String msg) {
 		plugin.getLogger().info("[Jukebox@" + location.getWorld().getName() + " " + location.getBlockX() + " " + location.getBlockY() + " " + location.getBlockZ() + "] " + msg);
 	}
 	public Jukebox getJukebox() {
 		try {
 			return (Jukebox)this.location.getBlock().getState();
 		} catch (ClassCastException ex) {
 			return null;
 		}
 	}
 	
 	public Chest getChest() {
 		if (this.chest == null) {
 			return null;
 		} else {
 			try {
 				return (Chest)this.chest.getBlock().getLocation().getBlock().getState();
 			} catch (ClassCastException ex) {}
 		}
 		return null;
 	}
 	
 	public boolean validate() {
 		try {
 			this.jukebox = (Jukebox)this.location.getBlock().getState();
 		} catch (ClassCastException ex) {
 			return false;
 		}
 		this.chest = null;
 		BlockState rel = null;
 		for (BlockFace f: plugin.directions) {
 			try {
 				rel = (BlockState)this.jukebox.getBlock().getRelative(f).getState();
 				this.chest = (Chest)rel;
 			} catch (ClassCastException ex) {
 				continue;
 			}
 		}
 		return true;
 	}
 	
 	public boolean playersNearby() {
 		double dist;
 		for (Player p: plugin.getServer().getOnlinePlayers()) {
 			try {
 				dist = getJukebox().getLocation().distance(p.getLocation());
 				if (dist <= 64) {
 					return true; 
 				}
 			} catch (IllegalArgumentException ex) {
 				// Cannot measure distance between 2 different worlds.
 				//log(ex.getMessage()); 
 			}
 		} 
 		
 		return false;
 	}
 	
 	public void doLoop() {
 		Jukebox jukebox = getJukebox();
 		if (jukebox == null) {
 			log("Destroyed, removing from cache");
 			this.isDead = true;
 			return;
 		}
 		if (!getJukebox().isPlaying()) return;
 		
 		
 		
 		int now = (int)(System.currentTimeMillis()/1000);
 		Material record = this.jukebox.getPlaying();
 		if (now - startedAt > plugin.recordDurations.get(record)) {
 			if (!playersNearby()) return;
 			if (this.getChest() == null) {
 				loopOneDisc(now);
 			} else {
 				loopFromChest(now);
 			}
 		}
 
 	}
 	
 	
 	
 	private void loopOneDisc(int now) {
 			Material record = this.getJukebox().getPlaying();
 			log("Looping single disc: " + plugin.recordNames.get(record));
 			this.getJukebox().setPlaying(record);
 			onInsert(record);
 					
 	}
 	
 	public void onInsert(Material record) {
 		startedAt = (int)(System.currentTimeMillis()/1000);
 	}
 	
 	public void onEject() {
 		this.startedAt = -1;
 	}
 	
 	private void loopFromChest(int now) {
 		
 		Jukebox jukebox = getJukebox();
 		Material record = jukebox.getPlaying();
 		
 		Chest chest = getChest();
 		int idx = chest.getBlockInventory().firstEmpty();
 		if (idx == -1) { 
 			loopOneDisc(now); 
 			return; 
 		}
 		chest.getBlockInventory().setItem(idx, new ItemStack(record));
 		Material newRecord = null;
 		ItemStack[] contents = chest.getBlockInventory().getContents();
 		int i=idx +1;
 		if (i >= chest.getBlockInventory().getSize()) i = 0;
 		while (newRecord == null) {
 			if (contents[i] != null) {
 				if (plugin.recordDurations.containsKey(contents[i].getType())) {
 					newRecord = contents[i].getType();
 					chest.getBlockInventory().setItem(i, new ItemStack(Material.AIR));
 					break;
 				}
 			}
 			i++;
 			if (i == contents.length) i = 0;
 			if (i == idx) break;
 		}
 		log("Looping from chest: " + plugin.recordNames.get(record) + " -> " + plugin.recordNames.get(newRecord));
 		this.startedAt = now;
		if (newRecord != null) {
			jukebox.setPlaying(newRecord);
		}
 		
 	}
 	
 }
