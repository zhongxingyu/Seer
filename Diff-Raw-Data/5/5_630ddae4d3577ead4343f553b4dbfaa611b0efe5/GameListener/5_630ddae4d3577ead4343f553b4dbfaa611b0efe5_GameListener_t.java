 package com.homie.endersgame.listeners;
 
 import java.sql.SQLException;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Random;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.block.Block;
 import org.bukkit.block.Sign;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.EntityType;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.Action;
 import org.bukkit.event.block.BlockBreakEvent;
 import org.bukkit.event.block.BlockPlaceEvent;
 import org.bukkit.event.block.SignChangeEvent;
 import org.bukkit.event.entity.ProjectileHitEvent;
 import org.bukkit.event.player.AsyncPlayerChatEvent;
 import org.bukkit.event.player.PlayerBucketEmptyEvent;
 import org.bukkit.event.player.PlayerCommandPreprocessEvent;
 import org.bukkit.event.player.PlayerDropItemEvent;
 import org.bukkit.event.player.PlayerInteractEvent;
 import org.bukkit.event.player.PlayerMoveEvent;
 import org.bukkit.event.player.PlayerQuitEvent;
 
 import com.homie.endersgame.EndersGame;
 import com.homie.endersgame.api.Game;
 import com.homie.endersgame.api.Game.GameTeam;
 import com.homie.endersgame.api.GameManager;
 import com.homie.endersgame.api.Lobby;
 import com.homie.endersgame.api.events.EventHandle;
 import com.homie.endersgame.runnable.RemovePlayer;
 
 public class GameListener implements Listener {
 
 	private EndersGame plugin;
 	private GameManager gm;
 	
 	public static HashMap<String, Location> creating_game_locations = new HashMap<String, Location>();
 	public static HashMap<String, Location> creating_lobby_locations = new HashMap<String, Location>();
 	public static HashMap<String, Integer> players_hit = new HashMap<String, Integer>();
 	
 	public GameListener(EndersGame plugin) {
 		this.plugin = plugin;
 		this.gm = plugin.getGameManager();
 	}
 	
 	@EventHandler(priority = EventPriority.NORMAL)
 	public void onSignChange(SignChangeEvent event) {
 		Player player = event.getPlayer();
 		String[] line = event.getLines();
 		int i;
 		if (line[0].toLowerCase().contains("[endersgame]")) {
 			try {
 				i = Integer.parseInt(line[1]);
 			} catch (NumberFormatException e) {
 				return;
 			}
 			if (player.hasPermission("EndersGame.createsign")) {
 				try {
 					if (gm.getGame(i) != null && !gm.getAllSignsFromDatabase().contains(i)) {
 						event.setLine(0, ChatColor.DARK_RED + "Ender's Game");
 						event.setLine(1, "Arena " + i);
 						event.setLine(2, "0/" + plugin.getEnderConfig().getMaxPlayers());
 						event.setLine(3, "Lobby");
 						try {
 							gm.registerSign(event.getBlock(), i);
 						} catch (SQLException e) {
 							e.printStackTrace();
 						}
 					} else {
 						player.sendMessage(ChatColor.RED + "That arena doesn't exist or a sign to that arena already exists");
 						event.getBlock().breakNaturally();
 						return;
 					}
 				} catch (SQLException e) {
 					if (e.getMessage() != null && e.getMessage().equalsIgnoreCase("ResultSet closed")) {
 						player.sendMessage(ChatColor.RED + "That arena doesn't exist or a sign to that arena already exists");
 						event.getBlock().breakNaturally();
 						return;
 					}
 					e.printStackTrace();
 				}
 			}
 		}
 	}
 	
 	@EventHandler(priority = EventPriority.HIGHEST)
 	public void onBlockPlace(BlockPlaceEvent event) {
 		Player player = event.getPlayer();
 		try {
 			for (Integer i : gm.getAllGamesFromDatabase()) {
 				Game game = gm.getGame(i);
 				if (gm.isInsideCuboid(player.getLocation(), game.getLocationOne(), game.getLocationTwo()) && !player.hasPermission("EndersGame.override")) {
 					event.setCancelled(true);
 					return;
 				}
 			}
 			for (Integer i : gm.getAllLobbiesFromDatabase()) {
 				Lobby lobby = gm.getLobby(i);
 				if (gm.isInsideCuboid(player.getLocation(), lobby.getLocationOne(), lobby.getLocationTwo()) && !player.hasPermission("EndersGame.override")) {
 					event.setCancelled(true);
 					return;
 				}
 			}
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	@EventHandler(priority = EventPriority.HIGHEST)
 	public void onBlockBreak(BlockBreakEvent event) {
 		Block b = event.getBlock();
 		if (b.getState() instanceof Sign) {
 			Sign s = (Sign) b.getState();
 			if (s.getLine(0).equalsIgnoreCase(ChatColor.DARK_RED + "Ender's Game")) {
 				try {
 					if (gm.getSign(Integer.parseInt(s.getLine(1).split("Arena ")[1])) != null && !event.isCancelled()) {
 						gm.unregisterSign(Integer.parseInt(s.getLine(1).split("Arena ")[1]));
 						return;
 					}
 				} catch (NumberFormatException | IndexOutOfBoundsException | SQLException e) {
 					if (e.getMessage() != null && e.getMessage().equalsIgnoreCase("ResultSet closed")) return;
 					e.printStackTrace();
 				}
 			}
 		}
 		Player player = event.getPlayer();
 		try {
 			for (Integer i : gm.getAllGamesFromDatabase()) {
 				Game game = gm.getGame(i);
 				if (gm.isInsideCuboid(player.getLocation(), game.getLocationOne(), game.getLocationTwo()) && !player.hasPermission("EndersGame.override")) {
 					event.setCancelled(true);
 					return;
 				}
 			}
 			for (Integer i : gm.getAllLobbiesFromDatabase()) {
 				Lobby lobby = gm.getLobby(i);
 				if (gm.isInsideCuboid(player.getLocation(), lobby.getLocationOne(), lobby.getLocationTwo()) && !player.hasPermission("EndersGame.override")) {
 					event.setCancelled(true);
 					return;
 				}
 			}
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	@EventHandler(priority = EventPriority.HIGHEST)
 	public void onPlayerDrop(PlayerDropItemEvent event) {
 		Player player = event.getPlayer();
 		try {
 			for (Integer i : gm.getAllGamesFromDatabase()) {
 				Game game = gm.getGame(i);
 				if (gm.isInsideCuboid(player.getLocation(), game.getLocationOne(), game.getLocationTwo()) && !player.hasPermission("EndersGame.override")) {
 					event.setCancelled(true);
 					return;
 				}
 			}
 			for (Integer i : gm.getAllLobbiesFromDatabase()) {
 				Lobby lobby = gm.getLobby(i);
 				if (gm.isInsideCuboid(player.getLocation(), lobby.getLocationOne(), lobby.getLocationTwo()) && !player.hasPermission("EndersGame.override")) {
 					event.setCancelled(true);
 					return;
 				}
 			}
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	@EventHandler(priority = EventPriority.HIGHEST)
 	public void onPlayerMove(PlayerMoveEvent event) {
		if (players_hit.containsKey(event.getPlayer().getName())) {
 			event.setCancelled(true);
 		}
 	}
 	
 	@EventHandler(priority = EventPriority.NORMAL)
 	public void onSnowallHit(ProjectileHitEvent event) {
 		if (event.getEntity().getType() == EntityType.SNOWBALL) {
 			List<Entity> e = event.getEntity().getNearbyEntities(1, 1, 1);
 			if (!e.isEmpty()) {
 				if (e.get(0) instanceof Player) {
 					Player player = (Player) e.get(0);
 					if (!players_hit.containsKey(player.getName())) {
 						try {
 							for (Integer i : gm.getAllGamesFromDatabase()) {
 								if (gm.getGamePlayerList(i).contains(player.getName())) {
 									players_hit.put(player.getName(), 0);
 									player.sendMessage(ChatColor.GOLD + "[EndersGame] " + ChatColor.RED + "You've been hit, you cannot move or shoot for 3 seconds");
 									return;
 								}
 							}
 						} catch (SQLException g) {
 							g.printStackTrace();
 						}
 					}
 				}
 			}
 		}
 	}
 	
 	@EventHandler(priority = EventPriority.HIGHEST)
 	public void onPlayerChat(AsyncPlayerChatEvent event) {
 		Player player = event.getPlayer();
 		try {
 			for (Integer i : gm.getAllGamesFromDatabase()) {
 				if (gm.getGamePlayers(i).containsKey(player.getName())) {
 					HashMap<String, GameTeam> pl = gm.getGamePlayers(i);
 					GameTeam pteam = pl.get(player.getName());
 					if (pteam == GameTeam.Team1) {
 						event.setMessage(ChatColor.AQUA + "[Team1] " + ChatColor.RESET + event.getMessage());
 					}
 					else if (pteam == GameTeam.Team1Leader) {
 						event.setMessage(ChatColor.BLUE + "[Leader] " + ChatColor.RESET + event.getMessage());
 					}
 					else if (pteam == GameTeam.Team2) {
 						event.setMessage(ChatColor.RED + "[Team2] " + ChatColor.RESET + event.getMessage());
 					}
 					else if (pteam == GameTeam.Team2Leader) {
 						event.setMessage(ChatColor.DARK_RED + "[Leader] " + ChatColor.RESET + event.getMessage());
 					}
 					for (Map.Entry<String, GameTeam> en : pl.entrySet()) {
 						if (en.getValue() != pteam) {
 							if (pteam == GameTeam.Team1 && en.getValue() == GameTeam.Team1Leader) continue;
 							if (pteam == GameTeam.Team1Leader && en.getValue() == GameTeam.Team1) continue;
 							if (pteam == GameTeam.Team2 && en.getValue() == GameTeam.Team2Leader) continue;
 							if (pteam == GameTeam.Team2Leader && en.getValue() == GameTeam.Team2) continue;
 							event.getRecipients().remove(plugin.getServer().getPlayer(en.getKey()));
 						}
 					}
 				}
 			}
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	@EventHandler(priority = EventPriority.HIGHEST)
 	public void onBucketEmpty(PlayerBucketEmptyEvent event) {
 		Player player = event.getPlayer();
 		try {
 			for (Integer i : gm.getAllGamesFromDatabase()) {
 				Game game = gm.getGame(i);
 				if (gm.isInsideCuboid(player.getLocation(), game.getLocationOne(), game.getLocationTwo()) && !player.hasPermission("EndersGame.override")) {
 					event.setCancelled(true);
 					return;
 				}
 			}
 			for (Integer i : gm.getAllLobbiesFromDatabase()) {
 				Lobby lobby = gm.getLobby(i);
 				if (gm.isInsideCuboid(player.getLocation(), lobby.getLocationOne(), lobby.getLocationTwo()) && !player.hasPermission("EndersGame.override")) {
 					event.setCancelled(true);
 					return;
 				}
 			}
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	@EventHandler(priority = EventPriority.HIGHEST)
 	public void onCommandPreprocess(PlayerCommandPreprocessEvent event) {
 		if (event.getMessage().toLowerCase().startsWith("/eg leave")) return;
 		Player player = event.getPlayer();
 		try {
 			for (Integer i : gm.getAllGamesFromDatabase()) {
 				Game game = gm.getGame(i);
 				if (gm.isInsideCuboid(player.getLocation(), game.getLocationOne(), game.getLocationTwo()) && !player.hasPermission("EndersGame.override")) {
 					event.setCancelled(true);
 					player.sendMessage(ChatColor.RED + "You can't use commands while in-game. To leave, use /eg leave");
 					return;
 				}
 			}
 			for (Integer i : gm.getAllLobbiesFromDatabase()) {
 				Lobby lobby = gm.getLobby(i);
 				if (gm.isInsideCuboid(player.getLocation(), lobby.getLocationOne(), lobby.getLocationTwo()) && !player.hasPermission("EndersGame.override")) {
 					event.setCancelled(true);
 					player.sendMessage(ChatColor.RED + "You can't use commands while in-game. To leave, use /eg leave");
 					return;
 				}
 			}
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	@EventHandler(priority = EventPriority.NORMAL)
 	public void onPlayerLeave(PlayerQuitEvent event) {
 		plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new RemovePlayer(plugin, event.getPlayer().getName()), 4L);
 		if (event.getPlayer().hasPermission("EndersGame.create")) {
 			EventHandle.callCancelCreatingCommandEvent(event.getPlayer());
 		}
 	}
 	
 	@EventHandler(priority = EventPriority.NORMAL)
 	public void onPlayerInteract(PlayerInteractEvent event) {
 		Player player = event.getPlayer();
 		String name = player.getName();
 		if (players_hit.containsKey(name)) {
 			if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
				if (player.getItemInHand().getType() == Material.SNOW_BALL && new Random().nextInt(2) == 0) {
 					event.setCancelled(true);
 					return;
 				}
 			}
 		}
 		if (EndersGame.creating_game_players.contains(name) || EndersGame.creating_lobby_players.contains(name)) {
 			if (event.getItem() != null && event.getItem().getType() == Material.WOOD_SPADE) {
 				if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
 					Block b = event.getClickedBlock();
 					event.setCancelled(true);
 					if (creating_game_locations.containsKey(name)) {
 						Location l1 = creating_game_locations.get(name);
 						Location l2 = b.getLocation();
 						creating_game_locations.remove(name);
 						try {
 							gm.registerGame(EndersGame.creating_game_ids.get(name).get(0), EndersGame.creating_game_ids.get(name).get(1), 
 									(int)l1.getX(), (int)l1.getY(), (int)l1.getZ(), (int)l2.getX(), (int)l2.getY(), (int)l2.getZ(), l1.getWorld());
 						} catch (SQLException e) {
 							player.sendMessage(ChatColor.RED + "There has been an error with the database: " + e.getMessage());
 							EndersGame.sendErr("SQLException while trying to register a new game, error: " + e.getErrorCode() + ", message: " + e.getMessage());
 							e.printStackTrace();
 							return;
 						}
 						EndersGame.creating_game_players.remove(name);
 						EndersGame.creating_game_ids.remove(name);
 						player.getInventory().remove(Material.WOOD_SPADE);
 						player.sendMessage(ChatColor.GREEN + "Point 2 specified, arena created. You should set the arena's spawn locations with /eg setspawn");
 						return;
 					}
 					else if (!creating_game_locations.containsKey(name) && !creating_lobby_locations.containsKey(name) && EndersGame.creating_game_players.contains(name)) {
 						creating_game_locations.put(name, b.getLocation());
 						player.sendMessage(ChatColor.GREEN + "Position 1 specified, select position two (opposite corner)");
 						return;
 					}
 					else if (creating_lobby_locations.containsKey(name)) {
 						Location l1 = creating_lobby_locations.get(name);
 						Location l2 = b.getLocation();
 						creating_lobby_locations.remove(name);
 						try {
 							gm.registerLobby(EndersGame.creating_lobby_ids.get(name), 
 									(int)l1.getX(), (int)l1.getY(), (int)l1.getZ(), (int)l2.getX(), (int)l2.getY(), (int)l2.getZ(), l1.getWorld());
 						} catch (SQLException e) {
 							player.sendMessage(ChatColor.RED + "There has been an error with the database: " + e.getMessage());
 							EndersGame.sendErr("SQLException while trying to register a new lobby, error: " + e.getErrorCode() + ", message: " + e.getMessage());
 							e.printStackTrace();
 							return;
 						}
 						EndersGame.creating_lobby_players.remove(name);
 						EndersGame.creating_lobby_ids.remove(name);
 						player.getInventory().remove(Material.WOOD_SPADE);
 						player.sendMessage(ChatColor.GREEN + "Point 2 specified, lobby created. You should set the lobby's spawn location with /eg setspawn");
 						return;
 					}
 					else if (!creating_game_locations.containsKey(name) && !creating_lobby_locations.containsKey(name) && EndersGame.creating_lobby_players.contains(name)) {
 						creating_lobby_locations.put(name, b.getLocation());
 						player.sendMessage(ChatColor.GREEN + "Position 1 specified, select position two (opposite corner)");
 						return;
 					}
 				}
 			}
 		}
 		
 		if (!event.hasBlock()) return;
 		Block b = event.getClickedBlock();
 		if (b.getState() instanceof Sign) {
 			Sign sign = (Sign) b.getState();
 			if (gm.isRegisteredSign(sign) && event.getAction() == Action.RIGHT_CLICK_BLOCK) {
 				if (player.hasPermission("EndersGame.join")) {
 					try {
 						EventHandle.callPlayerJoinEndersGameEvent(gm.getGame(Integer.parseInt(sign.getLine(1).split("Arena ")[1])), player);
 						event.setCancelled(true);
 						return;
 					} catch (IndexOutOfBoundsException | NumberFormatException e) {
 						player.sendMessage(ChatColor.RED + "The sign isn't formatted properly!");
 						return;
 					} catch (SQLException e) {
 						player.sendMessage(ChatColor.RED + "There has been an error with the database: " + e.getMessage());
 						EndersGame.sendErr("SQLException while trying to get a lobby and game from the database, error: " + e.getErrorCode() + ", message: " + e.getMessage());
 						e.printStackTrace();
 						return;
 					}
 				} else {
 					player.sendMessage(ChatColor.RED + "You do not have permission (EndersGame.join)");
 					return;
 				}
 			}
 		}
 	}
 }
