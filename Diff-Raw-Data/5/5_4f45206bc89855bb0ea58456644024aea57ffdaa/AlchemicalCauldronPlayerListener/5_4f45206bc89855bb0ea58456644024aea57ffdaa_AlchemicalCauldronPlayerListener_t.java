 package com.circuitlocution.alchemicalcauldron;
 
 import java.util.logging.Logger;
 
 import org.bukkit.DyeColor;
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.World;
 import org.bukkit.block.Block;
 import org.bukkit.entity.Player;
 import org.bukkit.event.player.PlayerItemEvent;
 import org.bukkit.event.player.PlayerListener;
 import org.bukkit.inventory.ItemStack;
 
 
 public class AlchemicalCauldronPlayerListener extends PlayerListener
 {
 	private AlchemicalCauldron plugin;
 	protected static final Logger log = Logger.getLogger("Minecraft");
 
 	public AlchemicalCauldronPlayerListener(AlchemicalCauldron instance)
 	{
 		plugin = instance;
 	}
 
 	@Override
 	public void onPlayerItem(PlayerItemEvent event){
 
 //		log.info("AlchemicalCauldronPlayerListener.onPlayerItem() called with event: " + event.toString());
 		if (event.isCancelled()){
 			return;
 		}
 
 		Block block = event.getBlockClicked();
 		if (block == null){
 			return;
 		}
 		World world = block.getWorld();
 		Location loc = new Location(world, block.getX(), block.getY(), block.getZ());
 		ItemStack item = event.getItem();
 		
 		Player p = event.getPlayer();
//		log.info("Click! " + p.getDisplayName() + " clicked on a " + block.getType().name() + " with a " + event.getItem().toString() + " which has material: " + item.getType().name());
 
 
 		if (item.getType() != Material.INK_SACK || item.getData().getData() != DyeColor.GREEN.getData()){
//			log.info("Clicked with either the wrong material ("+item.getType().name()+") or wrong data ("+item.getData().getData()+ " instead of " + DyeColor.GREEN.getData() + ")");
 			return;
 		}
 
 
 		Block base_block = world.getBlockAt(block.getX(), block.getY()-1, block.getZ());
 		log.info("Block under the block clicked is a " + base_block.getType().name());
 		
 		//figure out if this matches a recipe, and if so, what should we do with it?
 		if (block.getType() != Material.COBBLESTONE || base_block.getType() != Material.SNOW_BLOCK){
 			log.info("Types of blocks in cauldron are wrong, should be cobblestone on snow, is " + block.getType().name() + " on " + base_block.getType().name());
 			return;
 		}
 		//check to see if the block was just placed in a cauldron
 		if (!is_on_cauldron(loc)){
 			log.info("Doesn't look like a cauldron to me.");
 			return;
 		}
 		log.info("Hey, it's a cauldron and everything checks out!  Spawning lapis.");		
 
 		//looks good, spawn a lapis and delete those blocks
 		block.setType(Material.AIR);
 		base_block.setType(Material.AIR);
 		world.dropItemNaturally(loc, new ItemStack(Material.INK_SACK, 1, (byte) 4));
 		
 	}
 	
 	
 	private boolean is_on_cauldron(Location loc){
 		/**
 		 * Checks to see if the location is one block above an obsidian cauldron.
 		 * Cauldrons look like this:
 		 * Top:  Mid:  Bottom:
 		 * ***   ooo   ooo
 		 * *2*   o1o   ooo
 		 * ***   ooo   ooo
 		 * 
 		 * Key:	* = anything, probably air
 		 * 		2 = reagent 2, a placed block
 		 * 		1 = reagent 1, a placed block
 		 * 		o = obsidian
 		 */
 		int y = loc.getBlockY(); // y is used for height in minecraft
 		if (y <2 ){
 			return false;  //can't place a cauldron inside bedrock; also crashing is bad
 		}
 		int x = loc.getBlockX();
 		int z = loc.getBlockZ();
 
 		World world = loc.getWorld();
 		
 		//begin ugly hard-coded checks.
 		//maybe there's a more graceful way to do this, but this should be fast
 		
 		//two blocks under; this should be the fastest disqualifier in most cases
 		if (world.getBlockAt(x, y-2, z).getType() != Material.OBSIDIAN){
 			return false;
 		}
 		
 		//around the ring for tier2
 		if (world.getBlockAt(x-1, y-1, z-1).getType() != Material.OBSIDIAN){
 			return false;
 		}
 		if (world.getBlockAt(x, y-1, z-1).getType() != Material.OBSIDIAN){
 			return false;
 		}
 		if (world.getBlockAt(x+1, y-1, z-1).getType() != Material.OBSIDIAN){
 			return false;
 		}
 		if (world.getBlockAt(x-1, y-1, z+1).getType() != Material.OBSIDIAN){
 			return false;
 		}
 		if (world.getBlockAt(x, y-1, z+1).getType() != Material.OBSIDIAN){
 			return false;
 		}
 		if (world.getBlockAt(x+1, y-1, z+1).getType() != Material.OBSIDIAN){
 			return false;
 		}
 
 		//We don't check x,y-1,z, because that should be the location of reagent 1
 		if (world.getBlockAt(x-1, y-1, z).getType() != Material.OBSIDIAN){
 			return false;
 		}
 		if (world.getBlockAt(x+1, y-1, z).getType() != Material.OBSIDIAN){
 			return false;
 		}
 		
 		//Bottom tier
 		if (world.getBlockAt(x-1, y-2, z-1).getType() != Material.OBSIDIAN){
 			return false;
 		}
 		if (world.getBlockAt(x, y-2, z-1).getType() != Material.OBSIDIAN){
 			return false;
 		}
 		if (world.getBlockAt(x+1, y-2, z-1).getType() != Material.OBSIDIAN){
 			return false;
 		}
 		if (world.getBlockAt(x-1, y-2, z+1).getType() != Material.OBSIDIAN){
 			return false;
 		}
 		if (world.getBlockAt(x, y-2, z+1).getType() != Material.OBSIDIAN){
 			return false;
 		}
 		if (world.getBlockAt(x+1, y-2, z+1).getType() != Material.OBSIDIAN){
 			return false;
 		}
 
 		//we don't check x,y-2,z because we already did that
 		
 		if (world.getBlockAt(x-1, y-2, z).getType() != Material.OBSIDIAN){
 			return false;
 		}
 		if (world.getBlockAt(x+1, y-2, z).getType() != Material.OBSIDIAN){
 			return false;
 		}
 		
 		//if we got here, it's a cauldron
 		return true;
 	}
 	
 }
