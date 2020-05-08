 package com.untoldadventures.arena;
 
 import java.util.List;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.GameMode;
 import org.bukkit.Location;
 import org.bukkit.World;
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.Inventory;
 import org.bukkit.plugin.Plugin;
 
 import com.untoldadventures.arena.events.ArenaAddQueueEvent;
 import com.untoldadventures.arena.events.ArenaCreateEvent;
 import com.untoldadventures.arena.events.ArenaDeleteEvent;
 import com.untoldadventures.arena.events.ArenaJoinEvent;
 import com.untoldadventures.arena.events.ArenaPlayerWinEvent;
 import com.untoldadventures.arena.events.ArenaQuitEvent;
 import com.untoldadventures.arena.events.ArenaStartEvent;
 import com.untoldadventures.arena.events.ArenaStopEvent;
 import com.untoldadventures.arena.events.Countdown;
 
 public class Methods
 {
 	public static void createArena(Plugin plugin, String name, Player player, FileConfiguration arenaConfig)
 	{
 		Arena arena = new Arena(name.toLowerCase(), plugin);
 		if (player.hasPermission(plugin.getName().toLowerCase() + ".admin.create"))
 		{
 			if (!arenaConfig.contains("arenas." + arena.getName().toLowerCase()))
 			{
 				// Create the Arena
 				arenaConfig.createSection("arenas." + arena.getName().toLowerCase());
 				List<String> players = null;
 				arenaConfig.set("arenas." + arena.getName().toLowerCase() + ".players", players);
 				arenaConfig.set("arenas." + arena.getName().toLowerCase() + ".enabled", false);
 				arenaConfig.set("arenas." + arena.getName().toLowerCase() + ".inGame", false);
 				arenaConfig.set("arenas." + arena.getName().toLowerCase() + ".min-players", 2);
 				arenaConfig.set("arenas." + arena.getName().toLowerCase() + ".max-players", 10);
 				List<String> arenas = arenaConfig.getStringList("list.arenas");
 				arenas.add(arena.getName().toLowerCase());
 				arenaConfig.set("list.arenas", arenas);
 				pm(player, "Successfully created " + arena.getName().toLowerCase() + "!", plugin);
 				ArenaCreateEvent event = new ArenaCreateEvent(player, arena, plugin, arenaConfig);
 				Bukkit.getServer().getPluginManager().callEvent(event);
 				return;
 			}
 
 			pm(player, "An arena with the name: " + arena.getName() + " already exists!", plugin);
 			return;
 		}
 		pm(player, "You can't do that!", plugin);
 		return;
 	}
 
 	public static void deleteArena(Plugin plugin, String name, Player player, FileConfiguration arenaConfig, FileConfiguration playerConfig) throws CustomException
 	{
 		Arena arena = new Arena(name, plugin);
 		if (player.hasPermission(plugin.getName().toLowerCase() + ".admin.delete"))
 		{
 			if (arenaConfig.contains("arenas." + arena.getName().toLowerCase()))
 			{
 				stopArena(plugin, arena.getName(), arenaConfig, playerConfig);
 				arenaConfig.set("arenas." + arena.getName().toLowerCase(), null);
 				List<String> arenas = arenaConfig.getStringList("list.arenas");
 				arenas.remove(arena.getName().toLowerCase());
 				arenaConfig.set("list.arenas", arenas);
 				pm(player, "The arena, " + arena.getName().toLowerCase() + ", has been deleted!", plugin);
 				ArenaDeleteEvent event = new ArenaDeleteEvent(player, arena, plugin, arenaConfig);
 				Bukkit.getServer().getPluginManager().callEvent(event);
 				return;
 			}
 			pm(player, "The arena, " + arena.getName().toLowerCase() + ", doesn't exist!", plugin);
 			return;
 		}
 		pm(player, "You can't do that!", plugin);
 		return;
 	}
 
 	public static void playerWin(Plugin plugin, Player player, String name, FileConfiguration arenaConfig, FileConfiguration playerConfig) throws CustomException
 	{
 		Arena arena = new Arena(name, plugin);
 
 		if (!arenaConfig.equals(playerConfig))
 		{
 			Bukkit.getServer().broadcastMessage(ChatColor.AQUA + "[" + plugin.getName() + "] " + ChatColor.GRAY + player.getName() + " has won " + arena.getName().toLowerCase() + "!");
 			stopArena(plugin, arena.getName(), arenaConfig, playerConfig);
 			ArenaPlayerWinEvent event = new ArenaPlayerWinEvent(player, arena, plugin, arenaConfig);
 			Bukkit.getServer().getPluginManager().callEvent(event);
 			return;
 		}
 
 	}
 
 	public static void stopArena(Plugin plugin, String name, FileConfiguration arenaConfig, FileConfiguration playerConfig) throws CustomException
 	{
 		Arena arena = new Arena(name, plugin);
 
 		if (!arenaConfig.equals(playerConfig))
 		{
 			if (arenaConfig.contains("arenas." + arena.getName().toLowerCase()))
 			{
 				if (arenaConfig.getBoolean("arenas." + arena.getName().toLowerCase() + ".inGame"))
 				{
 					List<String> players = arenaConfig.getStringList("arenas." + arena.getName().toLowerCase() + ".players");
 					arenaConfig.set("arenas." + arena.getName().toLowerCase() + ".inGame", false);
 					for (String pname : players)
 					{
 						Player player = Bukkit.getPlayer(pname);
 						quitArena(plugin, player, arenaConfig, playerConfig);
 					}
 					List<String> queue = arenaConfig.getStringList("arenas." + arena.getName().toLowerCase() + ".queue");
 					for (String p : queue)
 					{
 						joinArena(plugin, arena.getName(), Bukkit.getPlayer(p), arenaConfig, playerConfig);
 					}
 					ArenaStopEvent event = new ArenaStopEvent(arena, plugin, arenaConfig);
 					Bukkit.getServer().getPluginManager().callEvent(event);
 					return;
 				}
 			}
 		}
 	}
 
 	public static void stopNoWinArena(Plugin plugin, String name, FileConfiguration arenaConfig, FileConfiguration playerConfig) throws CustomException
 	{
 		Arena arena = new Arena(name, plugin);
 
 		if (!arenaConfig.equals(playerConfig))
 		{
 			if (arenaConfig.contains("arenas." + arena.getName().toLowerCase()))
 			{
 				if (arenaConfig.getBoolean("arenas." + arena.getName().toLowerCase() + ".inGame"))
 				{
 					List<String> players = arenaConfig.getStringList("arenas." + arena.getName().toLowerCase() + ".players");
 					arenaConfig.set("arenas." + arena.getName().toLowerCase() + ".inGame", false);
 					for (String pname : players)
 					{
 						Player player = Bukkit.getPlayer(pname);
 						quitNoWinArena(plugin, player, arenaConfig, playerConfig);
 					}
 					List<String> queue = arenaConfig.getStringList("arenas." + arena.getName().toLowerCase() + ".queue");
 					queue.clear();
 					arenaConfig.set("arenas." + arena.getName().toLowerCase() + ".queue", queue);
 					ArenaStopEvent event = new ArenaStopEvent(arena, plugin, arenaConfig);
 					Bukkit.getServer().getPluginManager().callEvent(event);
 					return;
 				}
 			}
 		}
 	}
 
 	public static void joinArena(Plugin plugin, String name, Player player, FileConfiguration arenaConfig, FileConfiguration playerConfig) throws CustomException
 	{
 		Arena arena = new Arena(name, plugin);
 
 		if (!arenaConfig.equals(playerConfig))
 		{
 			if (player.hasPermission(plugin.getName().toLowerCase() + ".player.join"))
 			{
 				if (arenaConfig.contains("arenas." + arena.getName().toLowerCase()))
 				{
 					if (!playerConfig.contains("players." + player.getName()))
 					{
 						List<String> players = arenaConfig.getStringList("arenas." + arena.getName().toLowerCase() + ".players");
 						int max = arenaConfig.getInt("arenas." + arena.getName().toLowerCase() + ".max-players");
 						if (arenaConfig.getBoolean("arenas." + arena.getName().toLowerCase() + ".inGame"))
 						{
 							if (player.hasPermission(plugin.getName().toLowerCase() + ".player.queue"))
 							{
 								addToQueue(plugin, player, arena.getName().toLowerCase(), arenaConfig, playerConfig);
 								return;
 							} else
 							{
 								pm(player, "That arena is in game!", plugin);
 							}
 							return;
 						}
 						if (players.size() == max)
 						{
 
 							if (player.hasPermission(plugin.getName().toLowerCase() + ".player.queue"))
 							{
 								addToQueue(plugin, player, arena.getName(), arenaConfig, playerConfig);
 							} else
 							{
 								pm(player, "That arena is full!", plugin);
 							}
 							return;
 						}
 						playerConfig.createSection("players." + player.getName());
 						// Return position
 						Location returnLoc = player.getLocation();
 						int X = returnLoc.getBlockX(), Y = returnLoc.getBlockY(), Z = returnLoc.getBlockZ();
 						String world = returnLoc.getWorld().getName();
 						playerConfig.set("players." + player.getName() + ".return.x", X);
 						playerConfig.set("players." + player.getName() + ".return.y", Y);
 						playerConfig.set("players." + player.getName() + ".return.z", Z);
 						playerConfig.set("players." + player.getName() + ".return.world", world);
 						// Return GameMode
 						int gameMode = player.getGameMode().getValue();
 						playerConfig.set("players." + player.getName() + ".return.gamemode", gameMode);
 						// Return Inventory
 						String inventory = InventoryUtil.toBase64(player.getInventory());
 						Inventory armorInv = InventoryUtil.getArmorInventory(player.getInventory());
 						String armor = InventoryUtil.toBase64(armorInv);
 						playerConfig.set("players." + player.getName() + ".return.inventory", inventory);
 						playerConfig.set("players." + player.getName() + ".return.armor", armor);
 						// Inventory Management
 						player.getInventory().clear();
 						player.getInventory().setHelmet(null);
 						player.getInventory().setChestplate(null);
 						player.getInventory().setLeggings(null);
 						player.getInventory().setBoots(null);
 						// Teleportation
 						int lobbyX = arenaConfig.getInt("arenas." + arena.getName().toLowerCase() + ".positions.lobby.x"), lobbyZ = arenaConfig.getInt("arenas." + arena.getName().toLowerCase() + ".positions.lobby.z"), lobbyY = arenaConfig.getInt("arenas." + arena.getName().toLowerCase() + ".positions.lobby.y");
 						World worldName = Bukkit.getWorld(arenaConfig.getString("arenas." + arena.getName().toLowerCase() + ".positions.lobby.world"));
 						Location lobby = new Location(worldName, lobbyX, lobbyY, lobbyZ);
 						player.teleport(lobby);
 						// Setting them to Adventure Time
 						player.setGameMode(GameMode.ADVENTURE);
 						// Setting them to Player List
 						players.add(player.getName());
 						arenaConfig.set("arenas." + arena.getName().toLowerCase() + ".players", players);
 						// Storing the Arena they are in
 						playerConfig.set("players." + player.getName() + ".arena", arena.getName().toLowerCase());
 
 						ArenaJoinEvent event = new ArenaJoinEvent(player, arena, plugin, arenaConfig);
 						Bukkit.getServer().getPluginManager().callEvent(event);
 
 						for (String player1 : players)
 						{
							pm(Bukkit.getPlayer(player1), player.getName() + " joined the arena!", plugin);
 						}
 
 						if (players.size() == Math.ceil(max * .66))
 						{
 							new Countdown(name.toLowerCase(), plugin, arenaConfig).runTaskTimer(plugin, 0L, 20L);
 						}
 
 						if (!arenaConfig.getBoolean("arenas." + arena.getName().toLowerCase() + ".inGame"))
 						{
 							List<String> Vplayers = arenaConfig.getStringList("arenas." + arena.getName().toLowerCase() + ".voted");
 							int min = arenaConfig.getInt("arenas." + arena.getName().toLowerCase() + ".min-players");
 							long needed = (long) Math.ceil(players.size() / 2);
 							int current = Vplayers.size();
 
 							if (current == needed)
 							{
 								if (players.size() >= min)
 								{
 									new Countdown(arena.getName().toLowerCase(), plugin, arenaConfig).runTaskTimer(plugin, 0L, 20L);
 								}
 							}
 						}
						
 
 						return;
 					}
 					pm(player, "You are already in an arena!", plugin);
 					return;
 				}
 				pm(player, "That arena doesn't exist!", plugin);
 				return;
 			}
 			noPerm(player, plugin);
 			return;
 		}
 		CustomException configCantMatch = new CustomException(arenaConfig.getName() + " cannot equal " + playerConfig.getName());
 		throw (configCantMatch);
 	}
 
 	public static void quitArena(Plugin plugin, Player player, FileConfiguration arenaConfig, FileConfiguration playerConfig) throws CustomException
 	{
 		if (!arenaConfig.equals(playerConfig))
 		{
 			if (player.hasPermission(plugin.getName().toLowerCase() + ".player.quit"))
 			{
 				String arena = playerConfig.getString("players." + player.getName() + ".arena");
 
 				List<String> players = arenaConfig.getStringList("arenas." + arena + ".players");
 
 				if (players.contains(player.getName()))
 				{
 					// Inventory Management
 					player.getInventory().clear();
 					Inventory temp = InventoryUtil.fromBase64(playerConfig.getString("players." + player.getName() + ".return.inventory"));
 					if (temp != null)
 					{
 						player.getInventory().setContents(temp.getContents());
 					}
 					temp = InventoryUtil.fromBase64(playerConfig.getString("players." + player.getName() + ".return.armor"));
 					if (temp != null)
 					{
 						player.getInventory().setArmorContents(temp.getContents());
 					}
 					// Clearing Configs
 					if (playerConfig.getBoolean("players." + player.getName() + ".voted"))
 					{
 						List<String> voted = arenaConfig.getStringList("arenas." + arena + ".voted");
 						voted.remove(player.getName());
 						arenaConfig.set("arenas." + arena + ".voted", voted);
 					}
 
 					players.remove(player.getName());
 					arenaConfig.set("arenas." + arena + ".players", players);
 
 					List<String> Vplayers = arenaConfig.getStringList("arenas." + arena.toLowerCase() + ".voted");
 					if (Vplayers.contains(player.getName()))
 					{
 						Vplayers.remove(player.getName());
 						arenaConfig.set("arenas." + arena.toLowerCase() + ".voted", Vplayers);
 					}
 
 					// Gamemode
 					GameMode returnGm = GameMode.getByValue(playerConfig.getInt("players." + player.getName() + ".return.gamemode"));
 					player.setGameMode(returnGm);
 					// Return position
 					int x = playerConfig.getInt("players." + player.getName() + ".return.x"), y = playerConfig.getInt("players." + player.getName() + ".return.y"), z = playerConfig.getInt("players." + player.getName() + ".return.z");
 					World returnWorld = Bukkit.getWorld(playerConfig.getString("players." + player.getName() + ".return.world"));
 					Location returnPos = new Location(returnWorld, x, y, z);
 					player.teleport(returnPos);
 
					playerConfig.set("players." + player.getName().toLowerCase(), null);
 
 					Arena arena1 = new Arena(arena, plugin);
 					ArenaQuitEvent event = new ArenaQuitEvent(player, arena1, plugin, arenaConfig, playerConfig);
 					Bukkit.getServer().getPluginManager().callEvent(event);
 
 					if (!arenaConfig.getBoolean("arenas." + arena.toLowerCase() + ".inGame"))
 					{
 						int min = arenaConfig.getInt("arenas." + arena.toLowerCase() + ".min-players");
 						long needed = (long) Math.ceil(players.size() / 2);
 						int current = Vplayers.size();
 
 						if (current == needed)
 						{
 							if (players.size() >= min)
 							{
 								new Countdown(arena.toLowerCase(), plugin, arenaConfig).runTaskTimer(plugin, 0L, 20L);
 							}
 						}
 					}
 					if (players.size() == 1)
 					{
 						if (arenaConfig.getBoolean("arenas." + arena + ".inGame"))
 						{
 							Player winner = Bukkit.getPlayer(players.get(0));
 							playerWin(plugin, winner, arena, arenaConfig, playerConfig);
 						} else
 						{
 							pm(player, "You have quit the arena!", plugin);
 						}
 					} else
 					{
 						pm(player, "You have quit the arena!", plugin);
 					}
 					return;
 				}
 
 				if (playerConfig.contains("queue." + player.getName()))
 				{
 					String name = playerConfig.getString("queue." + player.getName());
 					deleteFromQueue(plugin, player, name, arenaConfig, playerConfig);
 				}
 				return;
 			}
 			noPerm(player, plugin);
 			return;
 		}
 		CustomException configCantMatch = new CustomException(arenaConfig.getName() + " cannot equal " + playerConfig.getName());
 		throw (configCantMatch);
 	}
 
 	public static void quitNoWinArena(Plugin plugin, Player player, FileConfiguration arenaConfig, FileConfiguration playerConfig) throws CustomException
 	{
 		if (!arenaConfig.equals(playerConfig))
 		{
 			if (player.hasPermission(plugin.getName().toLowerCase() + ".player.quit"))
 			{
 				String arena = playerConfig.getString("players." + player.getName() + ".arena");
 
 				List<String> players = arenaConfig.getStringList("arenas." + arena + ".players");
 
 				if (players.contains(player.getName()))
 				{
 					// Inventory Management
 					player.getInventory().clear();
 					Inventory temp = InventoryUtil.fromBase64(playerConfig.getString("players." + player.getName() + ".return.inventory"));
 					if (temp != null)
 					{
 						player.getInventory().setContents(temp.getContents());
 					}
 					temp = InventoryUtil.fromBase64(playerConfig.getString("players." + player.getName() + ".return.armor"));
 					if (temp != null)
 					{
 						player.getInventory().setArmorContents(temp.getContents());
 					}
 					// Clearing Configs
 					if (playerConfig.getBoolean("players." + player.getName() + ".voted"))
 					{
 						List<String> voted = arenaConfig.getStringList("arenas." + arena + ".voted");
 						voted.remove(player.getName());
 						arenaConfig.set("arenas." + arena + ".voted", voted);
 					}
 
 					players.remove(player.getName());
 					arenaConfig.set("arenas." + arena + ".players", players);
 
 					List<String> Vplayers = arenaConfig.getStringList("arenas." + arena.toLowerCase() + ".voted");
 					if (Vplayers.contains(player.getName()))
 					{
 						Vplayers.remove(player.getName());
 					}
 
 					// Gamemode
 					GameMode returnGm = GameMode.getByValue(playerConfig.getInt("players." + player.getName() + ".return.gamemode"));
 					player.setGameMode(returnGm);
 					// Return position
 					int x = playerConfig.getInt("players." + player.getName() + ".return.x"), y = playerConfig.getInt("players." + player.getName() + ".return.y"), z = playerConfig.getInt("players." + player.getName() + ".return.z");
 					World returnWorld = Bukkit.getWorld(playerConfig.getString("players." + player.getName() + ".return.world"));
 					Location returnPos = new Location(returnWorld, x, y, z);
 					player.teleport(returnPos);
 
 					playerConfig.set("players." + player.getName(), null);
 
 					Arena arena1 = new Arena(arena, plugin);
 					ArenaQuitEvent event = new ArenaQuitEvent(player, arena1, plugin, arenaConfig, playerConfig);
 					Bukkit.getServer().getPluginManager().callEvent(event);
 
 					pm(player, "You have quit the arena!", plugin);
 
 					return;
 				}
 
 				if (playerConfig.contains("queue." + player.getName()))
 				{
 					String name = playerConfig.getString("queue." + player.getName());
 					deleteFromQueue(plugin, player, name, arenaConfig, playerConfig);
 				}
 				return;
 			}
 			noPerm(player, plugin);
 			return;
 		}
 		CustomException configCantMatch = new CustomException(arenaConfig.getName() + " cannot equal " + playerConfig.getName());
 		throw (configCantMatch);
 	}
 
 	public static void addToQueue(Plugin plugin, Player player, String name, FileConfiguration arenaConfig, FileConfiguration playerConfig)
 	{
 		Arena arena = new Arena(name, plugin);
 
 		if (playerConfig.contains("queue." + player.getName()))
 		{
 			deleteFromQueue(plugin, player, name, arenaConfig, playerConfig);
 		}
 		if (!arenaConfig.contains("arenas." + arena.getName().toLowerCase() + ".queue"))
 		{
 			List<String> queue = null;
 			arenaConfig.set("arenas." + arena.getName().toLowerCase() + ".queue", queue);
 		}
 		List<String> queue = arenaConfig.getStringList("arenas." + arena.getName().toLowerCase() + ".queue");
 		queue.add(player.getName());
 		arenaConfig.set("arenas." + arena.getName().toLowerCase() + ".queue", queue);
 		int position = queue.size();
 		pm(player, "You are now in queue for the arena, " + arena.getName().toLowerCase() + ". Your position is " + position, plugin);
 		playerConfig.set("queue." + player.getName(), name);
 
 		ArenaAddQueueEvent event = new ArenaAddQueueEvent(player, plugin, arenaConfig);
 		Bukkit.getServer().getPluginManager().callEvent(event);
 		return;
 	}
 
 	public static void deleteFromQueue(Plugin plugin, Player player, String name, FileConfiguration arenaConfig, FileConfiguration playerConfig)
 	{
 		List<String> queue = arenaConfig.getStringList("arenas." + name + ".queue");
 		queue.remove(player.getName());
 		arenaConfig.set("arenas." + name + ".queue", queue);
 		playerConfig.set("queue." + player.getName(), null);
 		pm(player, "You left the queue for the arena, " + name + "!", plugin);
 
 		ArenaAddQueueEvent event = new ArenaAddQueueEvent(player, plugin, arenaConfig);
 		Bukkit.getServer().getPluginManager().callEvent(event);
 		return;
 	}
 
 	public static void setLobby(Plugin plugin, Player player, String name, FileConfiguration arenaConfig)
 	{
 		Location lobby = player.getLocation();
 		Arena arena = new Arena(name, plugin);
 
 		if (player.hasPermission(plugin.getName().toLowerCase() + ".admin.set.lobby"))
 		{
 			if (arenaConfig.contains("arenas." + arena.getName().toLowerCase().toLowerCase()))
 			{
 				int X = lobby.getBlockX(), Y = lobby.getBlockY(), Z = lobby.getBlockZ();
 				String world = lobby.getWorld().getName();
 				arenaConfig.set("arenas." + arena.getName().toLowerCase() + ".positions.lobby.x", X);
 				arenaConfig.set("arenas." + arena.getName().toLowerCase() + ".positions.lobby.y", Y);
 				arenaConfig.set("arenas." + arena.getName().toLowerCase() + ".positions.lobby.z", Z);
 				arenaConfig.set("arenas." + arena.getName().toLowerCase() + ".positions.lobby.world", world);
 				pm(player, "Lobby position Set!", plugin);
 				return;
 			}
 			noPerm(player, plugin);
 			return;
 		}
 		noPerm(player, plugin);
 		return;
 	}
 
 	public static void setSpawn(Plugin plugin, Player player, String name, FileConfiguration arenaConfig)
 	{
 		Location spawn = player.getLocation();
 		Arena arena = new Arena(name, plugin);
 
 		if (player.hasPermission(plugin.getName().toLowerCase() + ".admin.set.spawn"))
 		{
 			if (arenaConfig.contains("arenas." + arena.getName().toLowerCase()))
 			{
 				int X = spawn.getBlockX(), Y = spawn.getBlockY(), Z = spawn.getBlockZ();
 				String world = spawn.getWorld().getName();
 				arenaConfig.set("arenas." + arena.getName().toLowerCase() + ".positions.spawn.x", X);
 				arenaConfig.set("arenas." + arena.getName().toLowerCase() + ".positions.spawn.y", Y);
 				arenaConfig.set("arenas." + arena.getName().toLowerCase() + ".positions.spawn.z", Z);
 				arenaConfig.set("arenas." + arena.getName().toLowerCase() + ".positions.spawn.world", world);
 				pm(player, "Spawn position Set!", plugin);
 				return;
 			}
 			pm(player, "That arena doesn't exist!", plugin);
 			return;
 		}
 		noPerm(player, plugin);
 		return;
 	}
 
 	public static void setType(Plugin plugin, Player player, String name, FileConfiguration arenaConfig, String type)
 	{
 		Arena arena = new Arena(name, plugin);
 
 		if (player.hasPermission(plugin.getName().toLowerCase() + ".admin.set.type"))
 		{
 
 			arenaConfig.set("arenas." + arena.getName().toLowerCase() + ".type", type);
 			pm(player, "Type Set!", plugin);
 		}
 
 		noPerm(player, plugin);
 		return;
 	}
 
 	public static void listArenas(Plugin plugin, Player player, FileConfiguration arenaConfig)
 	{
 		if (player.hasPermission(plugin.getName().toLowerCase() + ".player.list"))
 		{
 			List<String> arenas = arenaConfig.getStringList("list.arenas");
 			if (!arenas.isEmpty())
 			{
 				String arenasS = arenas.toString().replace("[", "").replace("]", "");
 				pm(player, "Arenas: " + arenasS, plugin);
 				return;
 			}
 			pm(player, "There are no arenas!", plugin);
 			return;
 		}
 		noPerm(player, plugin);
 		return;
 	}
 
 	public static void addVote(Plugin plugin, String arena, Player player, FileConfiguration arenaConfig, FileConfiguration playerConfig)
 	{
 		if (player.hasPermission(plugin.getName().toLowerCase() + ".player.vote"))
 		{
 			if (playerConfig.contains("players." + player.getName()))
 			{
 
 				List<String> Vplayers = arenaConfig.getStringList("arenas." + arena.toLowerCase() + ".voted");
 				if (!Vplayers.contains(player.getName()))
 				{
 					Vplayers.add(player.getName());
 					List<String> players = arenaConfig.getStringList("arenas." + arena.toLowerCase() + ".players");
 					arenaConfig.set("arenas." + arena.toLowerCase() + ".voted", Vplayers);
 					long needed = (long) Math.ceil(players.size() / 2);
 					int current = Vplayers.size();
 					for (String p : players)
 					{
 						Player playerZ = Bukkit.getPlayer(p);
 						if (needed != 0)
 						{
 							pm(playerZ, player.getName() + " voted to start the game! " + current + "/" + needed, plugin);
 						}
 						if (needed == 0)
 						{
 							pm(playerZ, player.getName() + " voted to start the game! " + current + "/1", plugin);
 						}
 					}
 					int min = arenaConfig.getInt("arenas." + arena.toLowerCase() + ".min-players");
 
 					if (current == needed)
 					{
 						if (players.size() >= min)
 						{
 							new Countdown(arena.toLowerCase(), plugin, arenaConfig).runTaskTimer(plugin, 0L, 20L);
 						}
 					}
 				}
 			}
 		}
 	}
 
 	public static void startArena(Plugin plugin, String name, FileConfiguration arenaConfig)
 	{
 		Arena arena = new Arena(name, plugin);
 
 		ArenaStartEvent event = new ArenaStartEvent(arena, plugin);
 		Bukkit.getServer().getPluginManager().callEvent(event);
 	}
 
 	public static void pm(Player player, String message, Plugin plugin)
 	{
 		player.sendMessage(ChatColor.AQUA + "[" + plugin.getName() + "] " + ChatColor.GRAY + message);
 	}
 
 	public static void noPerm(Player player, Plugin plugin)
 	{
 		player.sendMessage(ChatColor.AQUA + "[" + plugin.getName() + "] " + ChatColor.GRAY + "You can't do that!");
 	}
 }
