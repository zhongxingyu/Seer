 package com.vxnick.abilitytrader;
 
 import java.text.MessageFormat;
 import java.util.Arrays;
 import java.util.List;
 import java.util.Set;
 import java.util.logging.Level;
 
 import net.milkbowl.vault.economy.Economy;
 import net.milkbowl.vault.economy.EconomyResponse;
 import net.milkbowl.vault.permission.Permission;
 
 import org.bukkit.plugin.RegisteredServiceProvider;
 import org.bukkit.plugin.java.JavaPlugin;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.configuration.ConfigurationSection;
 import org.bukkit.entity.Player;
 import org.bukkit.ChatColor;
 
 public class AbilityTrader extends JavaPlugin {
     public static Economy econ = null;
     public static Permission perms = null;
 	
 	@Override
 	public void onEnable() {
 		if (!setupEconomy() ) {
 			getLogger().log(Level.SEVERE, "Vault not found. Disabling AbilityTrader");
 			return;
 		}
 		
 		setupPermissions();
 		saveDefaultConfig();
 		
 		// Schedule removal of expired player abilities every minute
 		getServer().getScheduler().runTaskTimerAsynchronously(this, new Runnable() {
 			public void run() {
 				removeExpiredPlayerAbilities();
 			}
 		}, 40L, 1200L);
 	}
 	
 	@Override
 	public void onDisable() {
 
 	}
 	
 	/**
 	 * Initialise Vault Economy
 	 */	
 	private boolean setupEconomy() {
 		if (getServer().getPluginManager().getPlugin("Vault") == null) {
             return false;
         }
 		
         RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
         
         if (rsp == null) {
             return false;
         }
         
         econ = rsp.getProvider();
         
         return econ != null;
 	}
 	
 	/**
 	 * Initialise Vault Permissions
 	 */
 	private boolean setupPermissions() {
         RegisteredServiceProvider<Permission> rsp = getServer().getServicesManager().getRegistration(Permission.class);
         
         perms = rsp.getProvider();
         
         return perms != null;
     }
 	
 	private Set<String> getPlayers() {
 		ConfigurationSection playerSection = getConfig().getConfigurationSection("players");
 		
 		if (playerSection != null) {
 			return playerSection.getKeys(false);
 		}
 		
 		return null;
 	}
 	
 	private boolean playerHasAbility(String player, String ability) {
 		// Check the players configuration section exists
 		if (getPlayers() == null) {
 			return false;
 		}
 		
 		if (getConfig().getString(String.format("players.%s.%s", player, ability)) == null) {
 			return false;
 		}
 		
 		return true;
 	}
 
 	
 	private Set<String> getPlayerAbilities(String player) {
 		// Check the players configuration section exists
 		if (getPlayers() == null) {
 			return null;
 		}
 		
 		ConfigurationSection playerSection = getConfig().getConfigurationSection(String.format("players.%s", player));
 		
 		if (playerSection != null) {
 			return playerSection.getKeys(false);
 		}
 		
 		return null;
 	}
 	
 	private Set<String> getAvailableAbilities() {
 		ConfigurationSection abilitiesSection = getConfig().getConfigurationSection("abilities");
 		
 		if (abilitiesSection != null) {
 			return abilitiesSection.getKeys(false);
 		}
 		
 		return null;
 	}
 	
 	/**
 	 * Remove expired player abilities.
 	 * 
 	 * This is called by the async task every minute by default.
 	 */
 	private void removeExpiredPlayerAbilities() {
 		Set<String> players = getPlayers();
 		
 		if (players == null) {
 			return;
 		}
 		
 		for (String player : players) {
 			Set<String> playerAbilities = getPlayerAbilities(player);
 			
 			if (playerAbilities != null) {
 				for (String ability : playerAbilities) {
 					long expiresAt = getConfig().getLong(String.format("players.%s.%s.expires_at", player, ability), 0);
 
 					if (expiresAt > 0 && expiresAt < getUnixTime()) {
 						// Get a list of permissions for the current player ability
 						List<String> permissions = getConfig().getStringList(String.format("abilities.%s.permissions", ability));
 						
 						for (String permission : permissions) {
 							// Remove the permission if the player has it
 							if (perms.has((String) null, player, permission)) {
 								// Apply this to all worlds
 								perms.playerRemove((String) null, player, permission);
 							}
 						}
 						
 						// Move the player to a given group
 						String currentGroup = perms.getPrimaryGroup((String) null, player);
 						String newGroup = getConfig().getString(String.format("abilities.%s.groups.remove", ability));
 						
 						if (newGroup != null) {
 							// Check if this is their previous group
 							String finalNewGroup = null;
 							if (newGroup.equals("PREVIOUS_GROUP")) {
 								String previousGroup = getConfig().getString(String.format("players.%s.%s.previous_group", player, ability));
 								
 								if (previousGroup == null) {
 									finalNewGroup = newGroup;
 								} else {
 									finalNewGroup = previousGroup;
 								}
 							} else {
 								finalNewGroup = newGroup;
 							}
 							
 							perms.playerRemoveGroup((String) null, player, currentGroup);
 							perms.playerAddGroup((String) null, player, finalNewGroup);
 						}
 						
 						String purchaseType = getConfig().getString(String.format("players.%s.%s.purchase_type", player, ability), null);
 						runCommands("remove", player, ability, purchaseType.equals("rent"));
 						
 						// Remove the player ability
 						getConfig().set(String.format("players.%s.%s", player, ability), null);
 						
 						// Remove this player's section if they have no other abilities
 						Set<String> removeFromAbilities = getPlayerAbilities(player);
 						
 						if (removeFromAbilities == null || removeFromAbilities.isEmpty()) {
 							getConfig().set(String.format("players.%s", player), null);
 						}
 						
 						getLogger().log(Level.INFO, String.format("Removed the expired '%s' ability from %s", ability, player));
 					}
 				}
 			}
 		}
 		
 		saveConfig();
 	}
 	
 	private void runCommands(String type, String player, String ability, boolean rented) {
 		List<String> commandSections = Arrays.asList("global", String.format("abilities.%s", ability));
 		
 		for (String commandSection : commandSections) {
 			List<String> commands = getConfig().getStringList(String.format("%s.commands.%s", commandSection, type));
 			
 			if (commands != null) {
 				// Get some ability attributes that commands can use
 				Integer rentCost = getConfig().getInt(String.format("abilities.%s.rent_cost", ability), 0);
 				Integer buyCost = getConfig().getInt(String.format("abilities.%s.buy_cost", ability), 0);
 				Integer cost = (rented) ? rentCost : buyCost;
 				
 				for (String command : commands) {
 					// Run the command
 					getServer().dispatchCommand(getServer().getConsoleSender(), MessageFormat.format(command, player, 
 							ability, cost, econ.format(cost)));
 				}
 			}
 		}
 	}
 	
 	private boolean givePlayerAbility(CommandSender player, String ability, boolean rented) {
 		// Get a list of permissions for the given ability
 		List<String> permissions = getConfig().getStringList(String.format("abilities.%s.permissions", ability));
 		
 		if (permissions != null) {
 			for (String permission : permissions) {
 				// Add the permission if the player doesn't already have it
 				if (!perms.has(player, permission)) {
 					// Apply this to all worlds
 					perms.playerAdd((String) null, player.getName(), permission);
 				}
 			}
 		}
 		
 		// Move the player to a given group
 		String newGroup = getConfig().getString(String.format("abilities.%s.groups.add", ability));
 		
 		if (newGroup != null) {
 			// Set current (old) group
 			getConfig().set(String.format("players.%s.%s.previous_group", player.getName(), ability), 
 					perms.getPrimaryGroup((String) null, player.getName()));
 			perms.playerAddGroup((String) null, player.getName(), newGroup);
 		}
 		
 		runCommands("add", player.getName(), ability, rented);
 		
 		// Set whether it was bought or rented
 		getConfig().set(String.format("players.%s.%s.purchase_type", player.getName(), ability), (rented ? "rent" : "buy"));
 		
 		// Record this player's abilities in the configuration
 		getConfig().set(String.format("players.%s.%s.given_at", player.getName(), ability), getUnixTime());
 		
 		// Set duration (expiry) for this ability if rented
 		if (rented) {
 			Integer duration = getConfig().getInt(String.format("abilities.%s.duration", ability), 0);
 			getConfig().set(String.format("players.%s.%s.expires_at", player.getName(), ability), (duration > 0 ? getUnixTime() + duration : 0));
 		}
 		
 		String word = (rented) ? "rented" : "bought";
 		getLogger().log(Level.INFO, String.format("%s has %s the '%s' ability", player.getName(), word, ability));
 		
 		saveConfig();
 		return true;
 	}
 	
 	/**
 	 * A simple wrapper to return the Unix timestamp.
 	 */
 	private long getUnixTime() {
 		return (System.currentTimeMillis() / 1000L);
 	}
 	
 	/**
 	 * Format a value in seconds into minutes/hours/days.
 	 */
 	private String formatDuration(long seconds) {
 		if (seconds < 60) {
 			return String.format("%d second%s", seconds, (seconds == 1 ? "" : "s"));
 		} else if (seconds < 3600) {
 			return String.format("%d minute%s", (seconds / 60), ((seconds / 60) == 1 ? "" : "s"));
 		} else if (seconds < 86400) {
 			return String.format("%d hour%s", (seconds / 3600), ((seconds / 3600) == 1 ? "" : "s"));
 		} else {
 			return String.format("%d day%s", (seconds / 86400), ((seconds / 86400) == 1 ? "" : "s"));
 		}
 	}
 	
 	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
 		if (cmd.getName().equalsIgnoreCase("ability")) {
 			if (!perms.has(sender, "abilitytrader.use")) {
 				sender.sendMessage(ChatColor.RED + "Sorry, you don't have access to Ability Trader");
 				return true;
 			}
 			
 			if (args.length == 0) {
 				return false;
 			}
 			
 			String command = args[0].toLowerCase();
 			
 			if (perms.has(sender, "abilitytrader.admin.reload") && command.equals("reload")) {
 				sender.sendMessage(ChatColor.YELLOW + "Reloading configuration");
 				reloadConfig();
 				return true;
 			// List players with abilities
 			} else if (perms.has(sender, "abilitytrader.admin.players") && command.equals("players")) {
 				Set<String> listedPlayers = getPlayers();
 				
 				if (listedPlayers == null || listedPlayers.isEmpty()) {
 					sender.sendMessage(ChatColor.YELLOW + "There are currently no players with abilities");
 					return true;
 				}
 				
 				sender.sendMessage(ChatColor.GOLD + "Players with Abilities");
 				
 				for (String listedPlayer : listedPlayers) {
 					Set<String> playerAbilities = getPlayerAbilities(listedPlayer);
 					
 					sender.sendMessage(ChatColor.BLUE + listedPlayer);
 					
 					if (playerAbilities != null) {
 						for (String listedAbility : playerAbilities) {
 							String description = getConfig().getString(String.format("abilities.%s.description", listedAbility), "No description");
 							long expiresAt = getConfig().getLong(String.format("players.%s.%s.expires_at", listedPlayer, listedAbility), 0);
 							String timeRemaining = expiresAt == 0 ? "" : String.format("%s remaining", formatDuration(expiresAt - getUnixTime()));
 							
 							sender.sendMessage(String.format(ChatColor.YELLOW + "%s -- %s", listedAbility, description));
 
 							// Only show when remaining time is a positive number - the task doesn't remove abilities at the exact moment of expiry
 							if ((expiresAt - getUnixTime()) > 0) {					
 								
 								if (timeRemaining != "") {
 									sender.sendMessage(timeRemaining);
 								}
 							} else {
 								if (timeRemaining != "") {
 									sender.sendMessage("expired - pending removal");
 								}
 							}
 						}
 					}
 				}
 				
 				return true;
 			// Remove an ability from another player
 			} else if (perms.has(sender, "abilitytrader.admin.remove") && command.equals("remove")) {
 				if (args.length < 3) {
 					sender.sendMessage(ChatColor.RED + "Please specify a player and ability to remove");
 					return true;
 				}
 				
 				String removeFrom = args[1].toLowerCase();
 				String ability = args[2].toLowerCase();
 				
 				if (playerHasAbility(removeFrom, ability)) {
 					// Just remove it - don't run any post-removal commands
 					getConfig().set(String.format("players.%s.%s", removeFrom, ability), null);
 					
 					// Get a list of permissions for the current player ability
 					List<String> permissions = getConfig().getStringList(String.format("abilities.%s.permissions", ability));
 					
 					for (String permission : permissions) {
 						// Remove the permission if the player has it
 						if (perms.has((String) null, removeFrom, permission)) {
 							// Apply this to all worlds
 							perms.playerRemove((String) null, removeFrom, permission);
 						}
 					}
 					
 					// Move the player to a given group
 					String currentGroup = perms.getPrimaryGroup((String) null, removeFrom);
 					String newGroup = getConfig().getString(String.format("abilities.%s.groups.remove", ability));
 					
 					if (newGroup != null) {
 						// Check if this is their previous group
 						String finalNewGroup = null;
 						if (newGroup.equals("PREVIOUS_GROUP")) {
 							String previousGroup = getConfig().getString(String.format("players.%s.%s.previous_group", removeFrom, ability));
 							
 							if (previousGroup == null) {
 								finalNewGroup = newGroup;
 							} else {
 								finalNewGroup = previousGroup;
 							}
 						} else {
 							finalNewGroup = newGroup;
 						}
 						
 						perms.playerRemoveGroup((String) null, removeFrom, currentGroup);
 						perms.playerAddGroup((String) null, removeFrom, finalNewGroup);
 					}
 					
 					// Remove this player's section if they have no other abilities
 					Set<String> removeFromAbilities = getPlayerAbilities(removeFrom);
 					
 					if (removeFromAbilities == null || removeFromAbilities.isEmpty()) {
 						getConfig().set(String.format("players.%s", removeFrom), null);
 					}
 					
 					sender.sendMessage(ChatColor.GREEN + String.format("The '%s' ability has been removed from %s!", ability, removeFrom));
 					getLogger().log(Level.INFO, String.format("%s has removed the '%s' ability from %s", sender.getName(), ability, removeFrom));
 					
 					saveConfig();
 					return true;
 				}
 				
 				sender.sendMessage(ChatColor.YELLOW + String.format("%s doesn't have the '%s' ability", removeFrom, ability));
 				return true;
 			}
 			
 			if (command.equals("list")) {
 				Set<String> availableAbilities = getAvailableAbilities();
 				
 				if (availableAbilities == null) {
 					sender.sendMessage(ChatColor.YELLOW + "There are no abilities available!");
 					return true;
 				}
 				
 				sender.sendMessage(ChatColor.GOLD + "Available Abilities");
 				
 				for (String ability : availableAbilities) {
 					String description = getConfig().getString(String.format("abilities.%s.description", ability), "No description");
 					Integer duration = getConfig().getInt(String.format("abilities.%s.duration", ability), 0);
 					Integer rentCost = getConfig().getInt(String.format("abilities.%s.rent_cost", ability), 0);
 					Integer buyCost = getConfig().getInt(String.format("abilities.%s.buy_cost", ability), 0);
 					
 					sender.sendMessage(ChatColor.YELLOW + String.format("%s -- %s", ability, description));
 
 					// Figure out what type of cost this is
 					if (rentCost == 0 && buyCost != 0) {
 						sender.sendMessage(String.format("%s to buy", econ.format(buyCost)));
 					} else if (rentCost != 0 && buyCost == 0) {
 						sender.sendMessage(String.format("%s to rent for %s", econ.format(rentCost), formatDuration(duration)));
 					} else {
 						sender.sendMessage(String.format("%s to buy or %s to rent for %s", econ.format(buyCost), econ.format(rentCost), formatDuration(duration)));
 					}										
 				}
 			} else if (command.equals("info")) {
 				if (!(sender instanceof Player)) {
 					sender.sendMessage(ChatColor.RED + "The console does not have any abilities");
 					return true;
 				}
 				
 				Set<String> playerAbilities = getPlayerAbilities(sender.getName());
 				
 				if (playerAbilities == null || playerAbilities.isEmpty()) {
 					sender.sendMessage(ChatColor.YELLOW + "You currently have no abilities");
 					return true;
 				}
 				
 				sender.sendMessage(ChatColor.GOLD + "My Abilities");
 				
 				for (String ability : playerAbilities) {
 					String description = getConfig().getString(String.format("abilities.%s.description", ability), "No description");
 					long expiresAt = getConfig().getLong(String.format("players.%s.%s.expires_at", sender.getName(), ability), 0);
 					String timeRemaining = expiresAt == 0 ? "" : String.format("%s remaining", formatDuration(expiresAt - getUnixTime()));
 					
 					sender.sendMessage(String.format(ChatColor.YELLOW + "%s -- %s", ability, description));
 
 					// Only show when remaining time is a positive number - the task doesn't remove abilities at the exact moment of expiry
 					if ((expiresAt - getUnixTime()) > 0) {					
 						
 						if (timeRemaining != "") {
 							sender.sendMessage(timeRemaining);
 						}
 					} else {
 						if (timeRemaining != "") {
 							sender.sendMessage("expired - pending removal");
 						}
 					}
 				}
 			} else if (command.equals("buy") || command.equals("rent")) {
 				if (!(sender instanceof Player)) {
 					sender.sendMessage(ChatColor.RED + "You can not buy or rent abilities via the console");
 					return true;
 				}
 				
 				if (args.length < 2) {
 					sender.sendMessage(ChatColor.YELLOW + "Please specify an ability");
 					return true;
 				}
 				
 				String requestedAbility = args[1];
 				
 				// Check this ability exists
 				if (getConfig().getString(String.format("abilities.%s", requestedAbility)) == null) {
 					sender.sendMessage(ChatColor.RED + "Ability not found - please check /ability list for available abilities");
 					return true;
 				}
 				
 				// Get type of purchase (rent or buy) and its price
 				String purchaseType = command.equals("rent") ? "rent" : "buy";
				Integer cost = getConfig().getInt(String.format("abilities.%s.%s_cost", requestedAbility, purchaseType), -1);
 				
 				// Check that the player has specified the correct purchase type
				if (cost == -1) {
 					sender.sendMessage(ChatColor.YELLOW + String.format("You can not %s this ability", purchaseType));
 					return true;
 				}
 				
 				// Check the player doesn't already have this ability
 				if (getConfig().getString(String.format("players.%s.%s", sender.getName(), requestedAbility)) != null) {
 					sender.sendMessage(ChatColor.YELLOW + "You already have this ability!");
 					return true;
 				}
 				
 				// Attempt to purchase this ability
 				if (econ.getBalance(sender.getName()) >= cost) {
 					EconomyResponse r = econ.withdrawPlayer(sender.getName(), cost);
 					
 					if (r.transactionSuccess()) {
 						if (givePlayerAbility(sender, requestedAbility, purchaseType.equals("rent"))) {
 							sender.sendMessage(ChatColor.GOLD + String.format("You have been given the '%s' ability!", requestedAbility));
 						} else {
 							econ.depositPlayer(sender.getName(), cost);
 							sender.sendMessage(ChatColor.RED + "Sorry, something went wrong. You have been refunded");
 						}
 					} else {
 						sender.sendMessage(ChatColor.RED + "Sorry, something went wrong. No money has been taken");
 					}
 				} else {
 					sender.sendMessage(ChatColor.YELLOW + String.format("You do not have enough money to %s this ability!", purchaseType));
 				}
 			} else {
 				sender.sendMessage(ChatColor.GOLD + "Ability Trader Commands");
 				sender.sendMessage("/ability info -- Show which abilities you have");
 				sender.sendMessage("/ability list -- List available abilities");
 				sender.sendMessage("/ability buy <ability> -- Buy <ability>");
 				sender.sendMessage("/ability rent <ability> -- Rent <ability>");
 			}
 		}
 		
 		return true;
 	}
 }
