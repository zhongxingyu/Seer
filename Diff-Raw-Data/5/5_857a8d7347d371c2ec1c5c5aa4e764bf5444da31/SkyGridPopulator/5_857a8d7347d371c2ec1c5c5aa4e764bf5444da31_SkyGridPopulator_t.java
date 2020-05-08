 package com.LRFLEW.bukkit.skygrid;
 
 import java.util.HashSet;
 import java.util.List;
 import java.util.Random;
 
 import org.bukkit.Chunk;
 import org.bukkit.Material;
 import org.bukkit.World;
 import org.bukkit.block.Block;
 import org.bukkit.block.BlockFace;
 import org.bukkit.block.Chest;
 import org.bukkit.block.CreatureSpawner;
 import org.bukkit.entity.EntityType;
 import org.bukkit.generator.BlockPopulator;
 import org.bukkit.inventory.Inventory;
 import org.bukkit.inventory.ItemStack;
 
 import com.LRFLEW.bukkit.skygrid.rseries.RandomBlockSeries;
 import com.LRFLEW.bukkit.skygrid.rseries.RandomSeries;
 
 public class SkyGridPopulator extends BlockPopulator {
 	private RandomBlockSeries rnd = null;
 	private static RandomSeries slt = new RandomSeries(27);
 
 	@Override
 	public void populate(World world, Random random, Chunk chunk) {
 		if (rnd == null || rnd.hight != world.getMaxHeight()) {
 			rnd = new RandomBlockSeries(world);
 		}
 		for(int i = 0; random.nextDouble() < WorldStyles.getSProb(world, i); i++) {
 			if (WorldStyles.isChest(world, random)) {
 				newChest(chunk, random);
 			} else {
 				newSpawner(chunk, random);
 			}
 		}
 		rnd.reset();
 		if (chunk.getX() == 0 && chunk.getZ() == 0) {
 			chunk.getBlock(0, 200, 0).setTypeId(1); //Set Spawn to Stone
 			chunk.getBlock(0, 201, 0).setTypeId(0);
 			chunk.getBlock(1, 200, 0).setTypeId(0);
 			chunk.getBlock(0, 199, 0).setTypeId(0);
 			
 			setEndPortal(chunk);
 		}
 	}
 	
 	private void newSpawner(Chunk chunk, Random random) {
 		Block b = rnd.nextBlock(chunk, random);
 		
 		b.getRelative(BlockFace.UP).setTypeId(0);
 		b.getRelative(BlockFace.DOWN).setTypeId(0);
 		b.getRelative(BlockFace.SOUTH).setTypeId(0);
 		
 		b.setType(Material.MOB_SPAWNER);
 		CreatureSpawner spawner = (CreatureSpawner) b.getState();
 		
 		List<EntityType> spawns = WorldStyles.get(chunk.getWorld().getEnvironment()).spawns;
 		EntityType type = spawns.get(random.nextInt(spawns.size()));
 		spawner.setDelay(120);
 		spawner.setSpawnedType(type);
 	}
 	
 	private void newChest(Chunk chunk, Random random) {
 		Block b = rnd.nextBlock(chunk, random);
 		
 		b.getRelative(BlockFace.UP).setTypeId(0);
 		b.getRelative(BlockFace.DOWN).setTypeId(0);
 		b.getRelative(BlockFace.SOUTH).setTypeId(0);
 		
 		b.setType(Material.CHEST);
 		Chest chest = (Chest) b.getState();
 		Inventory inv = chest.getBlockInventory();
 		HashSet<ItemStack> set = new HashSet<ItemStack>();
 		
 		if (random.nextDouble() < 0.7)
 			set.add(itemInRange(256, 294, random)); //weapon/random
 		
 		if (random.nextDouble() < 0.7)
 			set.add(itemInRange(298, 317, random)); //armor
 		
 		if (random.nextDouble() < 0.7)
 			set.add(itemInRange(318, 350, random)); //food/tools
 		
 		if (random.nextDouble() < 0.3)
 			set.add(damageInRange(383, 50, 52, random)); //spawn eggs
 		
 		if (random.nextDouble() < 0.9)
			set.add(damageInRange(383, 54, 62, random)); //spawn eggs
 		
 		if (random.nextDouble() < 0.4)
			set.add(damageInRange(383, 92, 96, random)); //spawn eggs
 		
 		if (random.nextDouble() < 0.1)
 			set.add(new ItemStack(383, 1, (short) 98)); //ocelot spawn egg
 		
 		if (random.nextDouble() < 0.1)
 			set.add(new ItemStack(383, 1, (short) 120)); //villager spawn egg
 		
 		if (random.nextDouble() < 0.7)
 			set.add(itemMas(1, 5, 10, 64, random)); //materials
 		
 		set.add(damageInRange(6, 0, 3, random)); //sapling
 		
 		for (ItemStack i : set) {
 			inv.setItem(slt.next(random), i);
 		}
 		slt.reset();
 	}
 	
 	private ItemStack itemInRange(int min, int max, Random random) {
 		return new ItemStack(random.nextInt(max - min + 1) + min, 1);
 	}
 	
 	private ItemStack damageInRange(int type, int min, int max, Random random) {
 		return new ItemStack(type, 1, (short) (random.nextInt(max - min + 1) + min));
 	}
 	
 	private ItemStack itemMas(int min, int max, int sm, int lg, Random random) {
 		return new ItemStack(random.nextInt(max - min + 1) + min, 
 				random.nextInt(lg - sm + 1) + sm);
 	}
 	
 	private void setEndPortal(Chunk chunk) {
 		
 		chunk.getBlock(1, 4, 0).setTypeId(120);
 		chunk.getBlock(2, 4, 0).setTypeId(120);
 		chunk.getBlock(3, 4, 0).setTypeId(120);
 		
 		chunk.getBlock(4, 4, 1).setTypeId(120);
 		chunk.getBlock(4, 4, 2).setTypeId(120);
 		chunk.getBlock(4, 4, 3).setTypeId(120);
 		
 		chunk.getBlock(3, 4, 4).setTypeId(120);
 		chunk.getBlock(2, 4, 4).setTypeId(120);
 		chunk.getBlock(1, 4, 4).setTypeId(120);
 		
 		chunk.getBlock(0, 4, 3).setTypeId(120);
 		chunk.getBlock(0, 4, 2).setTypeId(120);
 		chunk.getBlock(0, 4, 1).setTypeId(120);
 		
 		//orient
 		chunk.getBlock(1, 4, 0).setData((byte) 0);
 		chunk.getBlock(2, 4, 0).setData((byte) 0);
 		chunk.getBlock(3, 4, 0).setData((byte) 0);
 		
 		chunk.getBlock(4, 4, 1).setData((byte) 1);
 		chunk.getBlock(4, 4, 2).setData((byte) 1);
 		chunk.getBlock(4, 4, 3).setData((byte) 1);
 		
 		chunk.getBlock(3, 4, 4).setData((byte) 2);
 		chunk.getBlock(2, 4, 4).setData((byte) 2);
 		chunk.getBlock(1, 4, 4).setData((byte) 2);
 		
 		chunk.getBlock(0, 4, 3).setData((byte) 3);
 		chunk.getBlock(0, 4, 2).setData((byte) 3);
 		chunk.getBlock(0, 4, 1).setData((byte) 3);
 	}
 	
 }
