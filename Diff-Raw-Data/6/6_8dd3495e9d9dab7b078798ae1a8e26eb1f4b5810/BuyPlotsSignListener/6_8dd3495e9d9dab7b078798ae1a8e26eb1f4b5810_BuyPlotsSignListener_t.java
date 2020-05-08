 package net.daboross.bukkitdev.wildwest;
 
import org.bukkit.Material;
import org.bukkit.block.Sign;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
 import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
 
 /**
  *
  */
 public class BuyPlotsSignListener implements Listener {
 
     private final WildWestBukkit wildWestBukkit;
 
     public BuyPlotsSignListener(WildWestBukkit wildWestBukkit) {
         this.wildWestBukkit = wildWestBukkit;
     }
 
     @EventHandler
     public void onSignCreate(SignChangeEvent sign) {
         Player player = sign.getPlayer();
         if (sign.getLine(0).equalsIgnoreCase("[BuyPlots]")) {
             sign.setLine(0, "[BuyPlots]");
             String line2 = sign.getLine(2);
             int line2num;
             boolean parsed = true;
             try {
                 line2num = Integer.parseInt(line2);
             } catch (NumberFormatException nfe) {
                 parsed = false;
             }
             if (parsed) {
                 player.sendMessage(MessageStatic.BUYPLOTS_CONFIRMATION);
             } else {
                 sign.setLine(1, "INVALID #");
                 player.sendMessage(MessageStatic.BUYPLOTS_NUMBER_FORMAT_ERROR);
             }
         }
     }
      @EventHandler
      public void RightClickSign(PlayerInteractEvent p) {
         if(p.getClickedBlock().getType() == Material.WALL_SIGN || 
            p.getClickedBlock().getType() == Material.SIGN ||
            p.getClickedBlock().getType() == Material.SIGN_POST) {
            Sign sign = (Sign) p.getClickedBlock().getState();
            if(p.getAction() == Action.RIGHT_CLICK_BLOCK) {
               boolean msg = false;
            if(sign.getLine(0).contains("[Freedom]")) {
         	
         	    	/*More to be added done by nerd
         	    	 * 
         	    	 */       
             }  
         }      
      }  
   }   
 }
