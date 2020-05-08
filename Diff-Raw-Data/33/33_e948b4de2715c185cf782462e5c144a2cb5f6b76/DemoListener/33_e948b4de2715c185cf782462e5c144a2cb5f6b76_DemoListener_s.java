 package edu.unca.rbruce.Demo;
 
 import java.text.MessageFormat;
 
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.block.Block;
 import org.bukkit.entity.EntityType;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.player.PlayerInteractEntityEvent;
 import org.bukkit.event.player.PlayerJoinEvent;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.block.Action;
 import org.bukkit.event.player.PlayerInteractEvent;
 
 /*
  * This is a sample event listener
  */
 public class DemoListener implements Listener {
     private final Demo plugin;
 
     /*
      * This listener needs to know about the plugin which it came from
      */
     public DemoListener(Demo plugin) {
         // Register the listener
         plugin.getServer().getPluginManager().registerEvents(this, plugin);
         
         this.plugin = plugin;
     }
 
     /*
      * Send the sample message to all players that join
      */
     @EventHandler
     public void onPlayerJoin(PlayerJoinEvent event) {
         event.getPlayer().sendMessage(this.plugin.getConfig().getString("sample.message"));
     }
     
     /*
      * Another example of a event handler. This one will give you the name of
      * the entity you interact with, if it is a Creature it will give you the
      * creature Id.
      */
     @EventHandler
     public void onPlayerInteract(PlayerInteractEntityEvent event) {
         final EntityType entityType = event.getRightClicked().getType();
 
         event.getPlayer().sendMessage(MessageFormat.format(
                 "You interacted with a {0} it has an id of {1}",
                 entityType.getName(),
                 entityType.getTypeId()));
     }
 	@EventHandler(priority = EventPriority.LOW)
	public void tntevent(PlayerInteractEvent tnt) {
 		Player jim = tnt.getPlayer();
 		if (tnt.getAction() == Action.LEFT_CLICK_BLOCK && jim.getItemInHand().getType() == Material.DIAMOND_SWORD) {
 			Block b = tnt.getClickedBlock();
 			if (b != null) {
 				Location loc = b.getLocation();
 				loc.getWorld().createExplosion(loc, 1, true) ;
 				tnt.getPlayer().sendMessage("You have exploded the ground in front of you!");
 				plugin.logger.info(tnt.getPlayer() + " created an explosion at  " + loc);
 			}
 		}
 	}
 }
