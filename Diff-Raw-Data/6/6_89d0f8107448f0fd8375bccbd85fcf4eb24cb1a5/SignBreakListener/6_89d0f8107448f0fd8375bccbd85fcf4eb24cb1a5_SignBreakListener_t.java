 package com.wolvencraft.prison.mines.events;
 
 import org.bukkit.block.BlockState;
 import org.bukkit.block.Sign;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.BlockBreakEvent;
 
 import com.wolvencraft.prison.mines.PrisonMine;
 import com.wolvencraft.prison.mines.mine.DisplaySign;
 import com.wolvencraft.prison.mines.util.Message;
 
 public class SignBreakListener implements Listener {
 	public SignBreakListener(PrisonMine plugin) {
         plugin.getServer().getPluginManager().registerEvents(this, plugin);
     }
 	
 	@EventHandler
 	public void onSignBreak(BlockBreakEvent event) {
 		if(event.isCancelled()) return;
         BlockState b = event.getBlock().getState();
         if(b instanceof Sign) {
         	Message.debug("Checking for defined signs...");
         	DisplaySign sign = DisplaySign.get((Sign) b);
         	if(sign == null) return;

    		if(!event.getPlayer().hasPermission("prison.mine.edit")) {
    			event.setCancelled(true);
        		Message.sendFormattedError(event.getPlayer(), PrisonMine.getLanguage().ERROR_ACCESS, false);
    			return;
    		}
         	
         	if(sign.deleteFile()) {
         		PrisonMine.removeSign(sign);
         		Message.sendFormattedSuccess(event.getPlayer(), "Sign successfully removed", false);
         	}
         	else {
         		Message.sendFormattedError(event.getPlayer(), "Error removing sign!", false);
         		event.setCancelled(true);
         	}
         	return;
         }
         else return;
 	}
 }
