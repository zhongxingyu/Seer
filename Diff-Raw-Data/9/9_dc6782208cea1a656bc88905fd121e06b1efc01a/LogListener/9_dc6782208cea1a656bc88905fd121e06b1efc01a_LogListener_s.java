 package me.arno.blocklog.listeners;
 
 import java.util.logging.Logger;
 
 import me.arno.blocklog.BlockLog;
 import me.arno.blocklog.LoggedBlock;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.BlockPlaceEvent;
 import org.bukkit.event.block.BlockBreakEvent;
 
 public class LogListener implements Listener {
 	BlockLog plugin;
 	
 	Logger log;
 	
 	public LogListener(BlockLog plugin) {
 		this.plugin = plugin;
 		
 		log = plugin.log;
 	}
 	
 	public void sendAdminMessage(String msg) {
 		for (Player player : Bukkit.getServer().getOnlinePlayers()) {
 	    	if (player.isOp() || player.hasPermission("blocklog.notices")) {
 	    		player.sendMessage(msg);
 	        }
 	    }
 	}
 	
 	public void BlocksLimitReached() {
 		if(plugin.blocks.size() == plugin.getConfig().getInt("database.warning")) {
 			sendAdminMessage(ChatColor.DARK_RED +"[BlockLog] " + ChatColor.GOLD + "BlockLog reached an internal storage of " + plugin.getConfig().getInt("database.warning") + "!");
 			sendAdminMessage(ChatColor.DARK_RED +"[BlockLog] " + ChatColor.GOLD + "You can fix this by using the command " + ChatColor.DARK_BLUE + "/blocklog fullsave" + ChatColor.GOLD + " or " + ChatColor.DARK_BLUE + "/blocklog save <blocks>");
 		} else if(plugin.blocks.size() > plugin.getConfig().getInt("database.warning")) {
 			int Size = plugin.blocks.size()/100;
 			if(Size == Math.round(Size)) {
				sendAdminMessage(ChatColor.DARK_RED +"[BlockLog] " + ChatColor.GOLD + "BlockLog reached an internal storage of " + plugin.blocks.size() + "!");
				sendAdminMessage(ChatColor.DARK_RED +"[BlockLog] " + ChatColor.GOLD + "You can fix this by using the command " + ChatColor.DARK_BLUE + "/blocklog fullsave" + ChatColor.GOLD + " or " + ChatColor.DARK_BLUE + "/blocklog save <blocks>");
 			}
 		}
 	}
 	
 	@EventHandler
 	public void onBlockPlace(BlockPlaceEvent event) {
 		if(!event.isCancelled()) {
 			LoggedBlock lb = new LoggedBlock(event.getPlayer(), event.getBlockPlaced(), 1);
 			plugin.blocks.add(lb);
 			BlocksLimitReached();
 		}
 	}
 
 	@EventHandler
 	public void onBlockBreak(BlockBreakEvent event) {
 		if(!event.isCancelled()) {
 			LoggedBlock lb = new LoggedBlock(event.getPlayer(), event.getBlock(), 0);
 			plugin.blocks.add(lb);
 			BlocksLimitReached();
 		}
 	}
 }
