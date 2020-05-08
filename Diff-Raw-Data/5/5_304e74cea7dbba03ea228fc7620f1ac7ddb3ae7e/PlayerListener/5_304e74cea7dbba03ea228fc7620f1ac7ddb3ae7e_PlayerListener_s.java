 package com.craftrealms.playerlog;
 
 import java.sql.SQLException;
 
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.Action;
 import org.bukkit.event.entity.PlayerDeathEvent;
 import org.bukkit.event.player.AsyncPlayerChatEvent;
 import org.bukkit.event.player.PlayerCommandPreprocessEvent;
 import org.bukkit.event.player.PlayerInteractEvent;
 import org.bukkit.event.player.PlayerLoginEvent;
 import org.bukkit.event.player.PlayerTeleportEvent;
 
 public class PlayerListener implements Listener {
 	private PlayerLog plugin;
 	
 	public PlayerListener(PlayerLog plugin) {
 		plugin.getServer().getPluginManager().registerEvents(this, plugin);
 		this.plugin = plugin;
 	}
 	
 	@EventHandler
 	public void PlayerChat(AsyncPlayerChatEvent e) {
 		try {
			this.plugin.sqlinsert("INSERT INTO  `playerlog`.`chat` (`id` ,`player` ,`date` ,`message` ,`server`) VALUES (NULL ,  '" + e.getPlayer().getName().toLowerCase() + "', CURRENT_TIMESTAMP ,  '" + e.getMessage() + "', '" + this.plugin.server + "')");
 		} catch (SQLException e1) {
 			e1.printStackTrace();
 		}
 	}
 	
 	@EventHandler
 	public void PlayerCommand(PlayerCommandPreprocessEvent c) {
 		if("/login" != c.getMessage().substring(0, Math.min(c.getMessage().length(), 5))) {
 			try {
				this.plugin.sqlinsert("INSERT INTO  `playerlog`.`command` (`id` ,`player` ,`date` ,`command` ,`server`) VALUES (NULL ,  '" + c.getPlayer().getName().toLowerCase() + "', CURRENT_TIMESTAMP ,  '" + c.getMessage() + "', '" + this.plugin.server + "')");
 			} catch (SQLException e1) {
 				e1.printStackTrace();
 			}
 		}
 	}
 	
 	@EventHandler
 	public void PlayerChestOpen(PlayerInteractEvent c) {
 		if(c.getAction() == Action.RIGHT_CLICK_BLOCK) {
 			if(c.getClickedBlock().getType().getId() == 54) {
 				try {
 					this.plugin.sqlinsert("INSERT INTO  `playerlog`.`chest` (`id` ,`player` ,`date` ,`x` ,`y` ,`z` ,`server`) VALUES (NULL ,  '" + c.getPlayer().getName().toLowerCase() + "', CURRENT_TIMESTAMP ,  '" + Integer.toString(c.getClickedBlock().getX()) + "', '" + Integer.toString(c.getClickedBlock().getY()) + "', '" + Integer.toString(c.getClickedBlock().getZ()) + "', '" + this.plugin.server + "')");
 				} catch (SQLException e) {
 					e.printStackTrace();
 				}
 			}
 		}
 	}
 	
 	@EventHandler
 	public void PlayerTeleport(PlayerTeleportEvent t) {
 		Integer fromx = t.getFrom().getBlockX();
 		Integer fromy = t.getFrom().getBlockY();
 		Integer fromz = t.getFrom().getBlockZ();
 		Integer tox = t.getTo().getBlockX();
 		Integer toy = t.getTo().getBlockY();
 		Integer toz = t.getTo().getBlockZ();
 		if(tox != fromx && tox != fromx + 5 && tox != fromx - 5) {
 			try {
 				this.plugin.sqlinsert("INSERT INTO  `playerlog`.`teleport` (`id` ,`player` ,`date` ,`fromx` ,`fromy` ,`fromz` ,`tox` ,`toy` ,`toz` ,`server`) VALUES (NULL ,  '" + t.getPlayer().getName().toLowerCase() + "', CURRENT_TIMESTAMP ,  '" + Integer.toString(fromx) + "', '" + Integer.toString(fromy) + "', '" + Integer.toString(fromz) + "', '" + Integer.toString(tox) + "', '" + Integer.toString(toy) + "', '" + Integer.toString(toz) + "', '" + this.plugin.server + "')");
 			} catch (SQLException e) {
 				e.printStackTrace();
 			}
 		}
 	}
 	
 	@EventHandler
 	public void PlayerLogin(PlayerLoginEvent l) {
 		try {
 			this.plugin.sqlinsert("INSERT INTO  `playerlog`.`loginlogout` (`id` ,`player` ,`date` ,`log` ,`server`) VALUES (NULL ,  '" + l.getPlayer().getName().toLowerCase() + "', CURRENT_TIMESTAMP ,  'login', '" + this.plugin.server + "')");
 		} catch (SQLException e1) {
 			e1.printStackTrace();
 		}
 	}
 	
 	@EventHandler
 	public void PlayerDeath(PlayerDeathEvent d) {
 		if(d.getEntity().getKiller() != null) {
 			try {
 				this.plugin.sqlinsert("INSERT INTO  `playerlog`.`killdeath` (`id` ,`date` ,`victim` ,`killer` ,`server`) VALUES (NULL ,CURRENT_TIMESTAMP , '" + d.getEntity().getName().toLowerCase() + "' ,'" + d.getEntity().getKiller().getName().toLowerCase() + "', '" + this.plugin.server + "')");
 			} catch (SQLException e) {
 				e.printStackTrace();
 			}
 		}
 	}
 }
