package aor.AnotherPlugin;

 import org.bukkit.inventory.*;
 import org.bukkit.Location;
 import org.bukkit.entity.Player;
 import org.bukkit.event.block.Action;
 import org.bukkit.event.player.PlayerListener;
 import org.bukkit.event.player.PlayerMoveEvent;
 import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.Material;
 /* Example Template
  * By Adamki11s
  * HUGE Plugin Tutorial
  */
 
 public class SPPlayerListener extends PlayerListener {
     public static SimplePlugin plugin;
     
     public SPPlayerListener(SimplePlugin instance) {
         plugin = instance;
     }
 
     public void onPlayerInteract(PlayerInteractEvent event) {
         //check if the player right clicks a block
         if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
             //get the player
             Player player = event.getPlayer();
             //check if the player is holding a gold hoe
            if((player.getItemInHand()).getType()== Material.GOLD_HOE){
                 //give the player a message saying Hello World!
                 player.sendMessage("Hello World!");
             }
         }
     }
 }
