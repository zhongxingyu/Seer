 package com.cole2sworld.dragonlist;
 
 import java.io.IOException;
 
 import org.bukkit.ChatColor;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.BlockBreakEvent;
 import org.bukkit.event.block.BlockPlaceEvent;
 import org.bukkit.event.player.PlayerChatEvent;
 import org.bukkit.event.player.PlayerCommandPreprocessEvent;
 import org.bukkit.event.player.PlayerInteractEvent;
 import org.bukkit.event.player.PlayerKickEvent;
 import org.bukkit.event.player.PlayerLoginEvent;
 import org.bukkit.event.player.PlayerMoveEvent;
 import org.bukkit.event.player.PlayerPreLoginEvent;
 import org.bukkit.event.player.PlayerLoginEvent.Result;
 import org.bukkit.event.player.PlayerQuitEvent;
 @SuppressWarnings("static-method")
 /**
  * Listens for events we want
  *
  */
 public final class DragonListener implements Listener {
 	@EventHandler (priority=EventPriority.HIGHEST)
 	public void onPreLogin(PlayerPreLoginEvent event) {
 		IPLogManager.initalize();
 		IPLogManager.ipLog.set(event.getName(), event.getAddress().getHostAddress());
 		try {
 			IPLogManager.ipLog.save(IPLogManager.logFile);
 		} catch (IOException e) {
 			Main.instance.logger.severe("[DragonList] Error saving IP log file! ("+e.getMessage() == null ? "" : e.getMessage()+")");
 		}
 	}
 	@EventHandler
 	public void onLogin(PlayerLoginEvent event) {
 		if (!GlobalConf.enabled) return;
 		Player player = event.getPlayer();
 		AuthManager.deauth(player);
 		if (!WhitelistManager.isWhitelisted(player.getName())) {
 			event.disallow(Result.KICK_WHITELIST, GlobalConf.kickMessage);
 		}
 		if (WhitelistManager.pass.getString(player.getName()).equals(WhitelistManager.UNSET_MESSAGE)) {
 			RestrictionManager.freeze(player);
 			player.sendMessage(ChatColor.BLUE+"Please enter a password - this can be changed later through /whitelist setpass <password>");
 		}
 	}
 	@EventHandler
 	public void onMove(PlayerMoveEvent event) {
 		if (RestrictionManager.isFrozen(event.getPlayer())) {
 			event.getPlayer().teleport(RestrictionManager.FREEZE_LOC);
 			event.getPlayer().setFallDistance(0);
 			event.getPlayer().setFireTicks(0);
 		}
 	}
 	@EventHandler
 	public void onCommand(PlayerCommandPreprocessEvent event) {
 		if (RestrictionManager.isFrozen(event.getPlayer())) {
 			event.getPlayer().sendMessage(ChatColor.RED+"No, bad "+event.getPlayer().getName()+"! You can't use that while frozen!");
 			event.setCancelled(true);
 		}
 	}
 	@EventHandler
 	public void onPlace(BlockPlaceEvent event) {
 		if (RestrictionManager.isFrozen(event.getPlayer())) {
 			event.getPlayer().sendMessage(ChatColor.RED+"No, bad "+event.getPlayer().getName()+"! You can't do that while frozen!");
 			event.setCancelled(true);
 		}
 	}
 	@EventHandler
 	public void onBreak(BlockBreakEvent event) {
 		if (RestrictionManager.isFrozen(event.getPlayer())) {
 			event.getPlayer().sendMessage(ChatColor.RED+"No, bad "+event.getPlayer().getName()+"! You can't do that while frozen!");
 			event.setCancelled(true);
 		}
 	}
 	@EventHandler
 	public void onInteract(PlayerInteractEvent event) {
 		if (RestrictionManager.isFrozen(event.getPlayer())) {
 			event.getPlayer().sendMessage(ChatColor.RED+"No, bad "+event.getPlayer().getName()+"! You can't do that while frozen!");
 			event.setCancelled(true);
 		}
 	}
 	@EventHandler
 	public void onChat(PlayerChatEvent event) {
 		if (RestrictionManager.isFrozen(event.getPlayer())) {
 			event.setCancelled(true);
 			try {
 				AuthManager.auth(event.getPlayer(), event.getMessage());
 			} catch (IncorrectPasswordException e) {
 				RestrictionManager.thaw(event.getPlayer());
 				event.getPlayer().kickPlayer("Incorrect password! Make sure any chat prefixes are turned off!");
 			} catch (PasswordNotSetException e) {
 				if (AuthManager.badPasswords.contains(event.getMessage())) {
 					RestrictionManager.thaw(event.getPlayer());
 					event.getPlayer().kickPlayer("Invalid password!");
 					return;
 				}
 				event.getPlayer().sendMessage(ChatColor.GREEN+"Password set to "+event.getMessage());
 				AuthManager.changePassword(event.getPlayer().getName(), event.getMessage());
 				try {
 					AuthManager.auth(event.getPlayer(), event.getMessage());
 				} catch (IncorrectPasswordException e1) {
 					Main.instance.logger.warning("[DragonList] Something wierd happened, and the password we JUST set is incorrect.");
 				} catch (PasswordNotSetException e1) {
 					Main.instance.logger.warning("[DragonList] Something wierd happened, and the password we JUST set is incorrect.");
 				}
 				RestrictionManager.thaw(event.getPlayer());
 			}
 		}
 	}
 	@EventHandler
 	public void onQuit(PlayerQuitEvent event) {
 		RestrictionManager.thaw(event.getPlayer());
 	}
 	@EventHandler
 	public void onKick(PlayerKickEvent event) {
 		RestrictionManager.thaw(event.getPlayer());
 	}
 }
