 package com.runetooncraft.plugins.EasyMobArmory.SpawnerHandler;
 
 import java.util.Random;
 
 import org.bukkit.Location;
 import org.bukkit.block.Block;
 import org.bukkit.inventory.Inventory;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.scheduler.BukkitTask;
 
 import com.runetooncraft.plugins.EasyMobArmory.core.CoreMethods;
 
 public class SpawnerCache {
 	Block SpawnerBlock = null;
 	Location SpawnerLocation = null;
 	Inventory SpawnerInventory = null;
 	ItemStack[] Eggs = null;
 	Boolean TimerEnabled = false;
 	int TimerTick = 120;
 	public SpawnerCache(Block SpawnerBlock, Location SpawnerLocation, Inventory SpawnerInventory) {
 		this.SpawnerBlock = SpawnerBlock;
 		this.SpawnerLocation = SpawnerLocation;
 		this.SpawnerInventory = SpawnerInventory;
 		this.Eggs = SpawnerInventory.getContents();
 	}
 	public Block getBlock() {
 		return SpawnerBlock;
 	}
 	public Location getLocation() {
 		return SpawnerLocation;
 	}
 	public Inventory getInventory() {
 		return SpawnerInventory;
 	}
 	public ItemStack[] getEggs() {
 		Eggs = SpawnerInventory.getContents();
 		return Eggs;
 	}
 	public int getTimerTick() {
 		return TimerTick;
 	}
 	public Boolean GetTimerEnabled() {
 		return TimerEnabled;
 	}
 	public Location RandomSpawnLocation() {
 		Location center = this.SpawnerLocation;
 			Random rand = new Random();
 			double angle = rand.nextDouble()*360; //Generate a random angle
 			double x = center.getX() + (rand.nextDouble()*5*Math.cos(Math.toRadians(angle))); // x
 			double z = center.getZ() + (rand.nextDouble()*5*Math.sin(Math.toRadians(angle))); // z
 			Location newloc = new Location(this.SpawnerLocation.getWorld(), x, this.SpawnerLocation.getY(), z);
 			return CoreMethods.CheckIfAirBlock(newloc);
 	}
 }
