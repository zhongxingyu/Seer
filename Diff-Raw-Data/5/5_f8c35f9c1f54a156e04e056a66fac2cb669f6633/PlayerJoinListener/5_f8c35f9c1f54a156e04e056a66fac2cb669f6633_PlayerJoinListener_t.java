 package me.tobi.FloatingIslands.Listeners;
 
 import me.tobi.FloatingIslands.Util;
 
 import org.bukkit.Chunk;
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.World;
 import org.bukkit.block.Block;
 import org.bukkit.block.BlockFace;
 import org.bukkit.entity.Player;
 import org.bukkit.event.Listener;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.player.PlayerJoinEvent;
 import org.bukkit.inventory.ItemStack;
 
 
 public class PlayerJoinListener implements Listener {
 	
 	private int maxGenHeight=127;
 	private int minGenHeight=0;
 	
 	public PlayerJoinListener(int maxGenHeight, int minGenHeight){
 		this.maxGenHeight=maxGenHeight;
 		this.minGenHeight=minGenHeight;
 	}
 	
 	/**
 	 * Teleports Player to a valid spwan location; when logging in
 	 * for the first time, fill inventory with lava bucket, ice and melon seeds.
 	 * @param pjevt
 	 */
 	@EventHandler
 	public void onPlayerJoin(PlayerJoinEvent pjevt){
 		Player player=pjevt.getPlayer();
 		if(!player.hasPlayedBefore()){
 			player.getInventory().addItem(new ItemStack(Material.ICE, 1));
 			player.getInventory().addItem(new ItemStack(Material.LAVA_BUCKET, 1));
 			player.getInventory().addItem(new ItemStack(Material.MELON_SEEDS, 1));
 		}
 		/*ensure that the player spawns at a valid island*/
 		Location spawn=player.getWorld().getSpawnLocation();
 		if(!Util.isValidSpawn(spawn.getBlock().getRelative(BlockFace.DOWN))){
 			System.out.println("invalid spawn at x="+spawn.getBlockX()+" y="+
 					spawn.getY()+" z="+spawn.getZ());
 			Block newSpawnBlock=getNearestSpawnBlock(player.getWorld(), spawn);
 			System.out.println("new spawn is at x="+newSpawnBlock.getX()+" y="+
 					newSpawnBlock.getY()+" z="+newSpawnBlock.getZ());
 			player.getWorld().setSpawnLocation(
 					newSpawnBlock.getX(),
 					newSpawnBlock.getY(),
 					newSpawnBlock.getZ()
 			);
 			//newSpawnBlock is the block the player spawns inside!
 			Util.ensureTreeAtIsland(newSpawnBlock.getRelative(BlockFace.DOWN));
			if(!Util.isValidSpawn(newSpawnBlock)){ //if tree generated is poor
				newSpawnBlock.getRelative(0, 0, 0).setType(Material.AIR);
				newSpawnBlock.getRelative(0, 1, 0).setType(Material.AIR);
				newSpawnBlock.getRelative(0, 2, 0).setType(Material.AIR);
			}
 			player.teleport(newSpawnBlock.getLocation());
 		}
 	}
 	
 	/**
 	 * Tries to find a near valid spawn location
 	 * @param world The world the spawn is in
 	 * @param oldSpawn The old and invalid spawn
 	 * @return The block the player spawns inside
 	 */
 	private Block getNearestSpawnBlock(World world, Location oldSpawn){
 		Chunk chunk=oldSpawn.getChunk();
 		do{
 			Block block=
 					Util.getFirstSolidBlockInChunk(chunk, maxGenHeight, minGenHeight);
 			/*if a grass block was found*/
 			if(block.getType()==Material.GRASS){
 				if(Util.isValidSpawn(block)){
 					return block.getRelative(BlockFace.UP);
 				}
 				else{
 					chunk=world.getChunkAt(chunk.getX(), chunk.getZ()-1);
 					continue;
 				}
 			}
 			/*if no grass block was found*/
 			else{
 				chunk=world.getChunkAt(chunk.getX()+1, chunk.getZ());
 				continue;
 			}
 		}while(true);
 	}
 }
