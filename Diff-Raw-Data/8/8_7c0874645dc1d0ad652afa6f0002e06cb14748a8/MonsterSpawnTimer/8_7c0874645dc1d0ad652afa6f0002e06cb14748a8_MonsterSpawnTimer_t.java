 package com.runetooncraft.plugins.EasyMobArmory.SpawnerHandler;
 
 import java.util.HashMap;
 import java.util.Random;
 
 import org.bukkit.Bukkit;
 import org.bukkit.Material;
 import org.bukkit.craftbukkit.v1_6_R2.inventory.CraftInventory;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.Inventory;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.scheduler.BukkitRunnable;
 
 import com.runetooncraft.plugins.EasyMobArmory.core.Messenger;
 import com.runetooncraft.plugins.EasyMobArmory.egghandler.EggHandler;
 
 public class MonsterSpawnTimer extends BukkitRunnable {
 	SpawnerCache sc = null;
 	public MonsterSpawnTimer(SpawnerCache sc) {
 		this.sc = sc;
 	}
 	@Override
 	public void run() {
 		Inventory RunningInv = Bukkit.createInventory(sc.getInventory().getHolder(), 54, "Spawnerrunninginv");
 		RunningInv.clear();
 		RunningInv.setContents(sc.getEggs());
 		HashMap<Integer, ? extends ItemStack> m = RunningInv.all(Material.MONSTER_EGG);
 		int EggInt = m.size();
 		if(RunningInv.contains(Material.MONSTER_EGG)) {
 			Random generator = new Random();
 			int RandomInt = generator.nextInt(EggInt);
 			ItemStack is = RunningInv.getItem(RandomInt);
 			EggHandler.Loadentity(EggHandler.getEggID(is), sc.RandomSpawnLocation());
 		}
 	}
 
 }
