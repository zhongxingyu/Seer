 package gt.plugin.helloworld;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Material;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.BlockBreakEvent;
 import org.bukkit.event.block.BlockDamageEvent;
 
 
 
 public class BlockListener implements Listener {
 	
 	/**
 	 * FOR TESTING ONLY
 	 * Break Pumpkins immediately on damage
 	 * @param event Damaging a block
 	 */
 	@EventHandler
	public void breakSocketOnDamage(final BlockDamageEvent event) {
		Material mat = event.getBlock().getType();
		// these are the underlying blocks
		if (event.getPlayer().isOp() && 
				(mat==Material.JACK_O_LANTERN || mat==Material.CAKE_BLOCK ) ) {
 			event.getBlock().setType(Material.AIR);
 		}
 	}
 	
 	/**
 	 * FOR TESTING ONLY
 	 * @param event Damaging a block
 	 */
 	@EventHandler
 	public void clearInventoryOnGoldBlockDamage(final BlockDamageEvent event) {
 		if (event.getBlock().getType() == Material.GOLD_BLOCK) {
 			event.setCancelled(true);
 			event.getPlayer().sendMessage(ChatColor.GREEN + "Cleaned up your inventory!");
 			event.getPlayer().getInventory().clear();
 		}
 	}
 	
 	/**
 	 * FOR TESTING ONLY
 	 * @param event Breaking a block
 	 */
 	@EventHandler
 	public void clearInventoryOnGoldBlockBreak(final BlockBreakEvent event) {
 		if (event.getBlock().getType() == Material.GOLD_BLOCK) {
 			event.setCancelled(true);
 			event.getPlayer().sendMessage(ChatColor.GREEN + "Cleaned up your inventory!");
 			event.getPlayer().getInventory().clear();
 		}
 	}
 	
 
 }
