 package se.jkrau.bukkit.secretword.events;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.GameMode;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.player.AsyncPlayerChatEvent;
 import org.bukkit.event.player.PlayerJoinEvent;
 import org.bukkit.event.player.PlayerKickEvent;
 import org.bukkit.event.player.PlayerLoginEvent;
 import org.bukkit.event.player.PlayerLoginEvent.Result;
 import org.bukkit.event.player.PlayerQuitEvent;
 import se.jkrau.bukkit.secretword.SecretWord;
 import se.jkrau.bukkit.secretword.database.SecretPlayer;
 import se.jkrau.bukkit.secretword.settings.Configuration;
 import se.jkrau.bukkit.secretword.settings.Permissions;
 import se.jkrau.bukkit.secretword.utils.StringUtils;
 
 public class CoreEvents implements Listener {
 	
 	public SecretWord core = null;
 	
 	public CoreEvents(SecretWord instance) {
 		core = instance;
 	}
 	
 	@EventHandler(priority = EventPriority.LOWEST)
 	public void reallyLogin(PlayerLoginEvent e) {
 		if (!StringUtils.checkUsername(e.getPlayer().getName())) e.disallow(Result.KICK_OTHER, "Bad username!");
 		
 		Configuration.log("Player logging in: " + e.getPlayer().getName());
 		// logged in from another location check.
 		if (Bukkit.getPlayerExact(e.getPlayer().getName()) != null && Configuration.blockLoginFromOtherLocation) {
 			Player pl = Bukkit.getPlayerExact(e.getPlayer().getName());
 			pl.sendMessage(Configuration.prefix + ChatColor.DARK_RED + "Somebody tried to login to this server on your account!" + (Bukkit.getOnlineMode() ? " Your password is either compromised or a new server exploit has been discovered." : ""));
 			e.disallow(Result.KICK_OTHER, "You are already logged ingame (maybe wait a minute?)");
 			Configuration.log("Player kicked because he's already logged in.");
 			core.getDB().failedlogins++;
 			return;
 		}
 		
 		// now register the player instance.
 		SecretPlayer player = new SecretPlayer(core, e.getPlayer().getName(), e.getAddress().toString().split(":")[0].replace("/", ""));
 		core.getDB().secplayers.put(player.getName(), player);
 		player.setBukkitPlayer(e.getPlayer());
 		
 		// check registered
 		player.setRegistered(core.getDB().userExists(player.getName()));
 		
 		Configuration.log("User " + player.getName() + " is " + (player.isRegistered() ? "" : "not ") + "registered.");
 		
 		// crap, this got removed
		if (player.isRegistered() && core.getServer().getOnlineMode() && Configuration.onlineModeBehavior) {
 			Configuration.log("Checking IP match..");
 			player.setLoggedIn(core.getDB().ipMatches(player.getIP(), player.getName()));
 			Configuration.log("IP match? " + Boolean.toString(player.isLoggedIn()));
 		}
 		
 		// then check if they have to relogin due to half hour stuff.
 		if (player.isRegistered() && Permissions.HALFHOUR.check(e.getPlayer())) {
 			Configuration.log("Checking Halfhour..");
 			player.setLoggedIn(core.getDB().halfHourCheck(player.getName()));
 			Configuration.log("Been less than half hour? " + Boolean.toString(player.isLoggedIn()));
 		}
 	}
 	
 	@EventHandler(priority = EventPriority.LOWEST)
 	public void onchat(AsyncPlayerChatEvent event) {
 		if (core.getDB().secplayers.containsKey(event.getPlayer().getName())) {
 			SecretPlayer player = core.getDB().secplayers.get(event.getPlayer().getName());
 			final String word = event.getMessage();
 			if (player.isLoggedIn()) { return; }
 			
 			// Security fix: Worse case-scenario the message does get broadcasted.
 			event.setMessage(Configuration.prefix + " " + ChatColor.RED + "Blocked");
 			
 			// this event is going to be cancelled. Just set it early.
 			event.setCancelled(true);
 			
 			if (player.threeSecondRule()) { return; }
 			
 			if (!player.isLoggedIn() && !player.isRegistered()) {
 				// register the player.
 				// but first, get the word in our string.
 				if (word.length() < Configuration.minwordlength) {
 					player.getPlayer().sendMessage(Configuration.prefix + ChatColor.RED + "Your secret word must be longer than " + Integer.toString(Configuration.minwordlength) + " characters.");
 					return;
 				}
 				
 				// good, register it and login.
 				core.getDB().addLogin(player.getName(), word);
 				player.setRegistered(true);
 				player.setLoggedIn(true);
 			} else if (!player.isLoggedIn()) {
 				// process login
 				
 				if (!core.getDB().checkLogin(player.getName(), word)) {
 					player.getPlayer().sendMessage(Configuration.prefix + ChatColor.RED + "Your secret word is incorrect!");
 					if (player.triggerAttempt()) {
 						core.getDB().secplayers.remove(player.getName());
 						core.getDB().failedlogins++;
 					}
 					
 					return;
 				}
 				
 				player.setLoggedIn(true);
 			}
 		}
 	}
 	
 	@EventHandler(priority = EventPriority.HIGHEST)
 	public void onjoin(PlayerJoinEvent e) {
 		if (core.getDB().secplayers.containsKey(e.getPlayer().getName())) {
 			SecretPlayer player = core.getDB().secplayers.get(e.getPlayer().getName());
 			player.setInitialLocation(e.getPlayer().getLocation());
 			if (!player.isLoggedIn()) {
 				
 				Configuration.log("Checking perms! " + Boolean.toString(Configuration.enableByPermission) + " - " + Boolean.toString(Permissions.LOGIN.check(e.getPlayer())));
 				
 				// check permissions.
 				if (Configuration.enableByPermission && !Permissions.LOGIN.check(e.getPlayer())) {
 					player.setLoggedIn(true);
 					Configuration.log("Player doesn't need to use SecretWord!");
 					return;
 				}
 				
 				// nullify the join message if necessary
 				if (Configuration.hideJoinNotifications && !Permissions.SHOWJOIN.check(e.getPlayer())) {
 					player.setJoinMessage(e.getJoinMessage());
 					e.setJoinMessage(null);
 				}
 				
 				// check creative mode/strip it if necessary.
 				player.setHasCreative(e.getPlayer().getGameMode() == GameMode.CREATIVE);
 				
 				// clear inventory
 				// player.setInventory(e.getPlayer().getInventory().getContents());
 				// e.getPlayer().getInventory().clear();
 				
 				// now lets check if registered or just stupid.
 				if (!player.isRegistered()) {
 					player.loadData();
 					Configuration.log("User " + player.getName() + " will need to register.");
 					player.getPlayer().sendMessage(Configuration.prefix + ChatColor.YELLOW + "Please enter a sentence, or a " + Configuration.minwordlength + "-letter word that you can remember.");
 				} else {
 					Configuration.log("User " + player.getName() + " will need to login.");
 					player.getPlayer().sendMessage(Configuration.prefix + ChatColor.RED + "Please enter your secret word.");
 				}
 			}
 		}
 	}
 	
 	@EventHandler(priority = EventPriority.MONITOR)
 	public void unregister(PlayerQuitEvent e) {
 		//SecretPlayer player = core.getDB().secplayers.get(e.getPlayer().getName());
 		/*
 		if (player != null) {
 			if (!player.isLoggedIn()) {
 				try {
 					// e.getPlayer().getInventory().addItem(player.getInventory());
 				} catch (Exception bukkitderpsalot) {}
 			}
 		}*/
 		
 		core.getDB().secplayers.remove(e.getPlayer().getName());
 	}
 	
 	@EventHandler(priority = EventPriority.MONITOR)
 	public void unregister(PlayerKickEvent e) {
 		if (e.isCancelled()) return;
 		
 		//SecretPlayer player = core.getDB().secplayers.get(e.getPlayer().getName());
 		
 		/*
 		if (player != null) {
 			if (!player.isLoggedIn()) {
 				try {
 					// e.getPlayer().getInventory().addItem(player.getInventory());
 				} catch (Exception bukkitderpsalot) {}
 			}
 		}*/
 		
 		core.getDB().secplayers.remove(e.getPlayer().getName());
 	}
 	
 }
