 package com.desiringsolid.info;
 
 import org.bukkit.ChatColor;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.BlockBreakEvent;
 
 public class MyPlayerListener implements Listener {
 	@EventHandler
 	public void onBlockBreak(BlockBreakEvent event){
 		Player player = event.getPlayer();
     	if(event.getBlock().getType().getId() == 56 ){
    	player.sendMessage(ChatColor.AQUA + "You have found a Diamond ore!");
     	}
 	}
 }
