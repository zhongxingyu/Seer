 package com.spikemeister.prank;
 
 import java.util.Random;
 
 import org.bukkit.World;
 import org.bukkit.block.Block;
import org.bukkit.entity.EntityType;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.player.PlayerMoveEvent;
 
 /***
  * Implements the "creeper" prank. Whenever a user moves, they have a .5% chance
  * of spawning a creeper at the location they are looking at.
  * 
  * @author Andrew
  * 
  */
 public class CreeperPrank implements Listener {
 	private final ConfigurationManager config = ConfigurationManager
 			.getConfigurationManager();
 	private final double DEFAULT_PROBABILITY = 0.005;
 	private final Random rand = new Random(System.currentTimeMillis());
 
 	/**
 	 * Whenever the player moves, if the random number is less than 0.005, spawn
 	 * a creeper at the target block of the player.
 	 */
 	@EventHandler
 	public void onPlayerMove(PlayerMoveEvent event) {
 		Player p = event.getPlayer();
 		boolean beingPranked = config.checkPrank(p.getName(), "creeper");
 
 		if (beingPranked) {
 			if (rand.nextDouble() < DEFAULT_PROBABILITY) {
 				World w = p.getWorld();
 				Block b = p.getTargetBlock(null, 256);
				w.spawnCreature(b.getLocation(), EntityType.CREEPER);

 			}
 		}
 	}
 }
