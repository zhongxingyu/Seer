 package com.amazar.plugin;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.GameMode;
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.OfflinePlayer;
 import org.bukkit.Statistic;
 import org.bukkit.TravelAgent;
 import org.bukkit.block.Block;
 import org.bukkit.block.BlockFace;
 import org.bukkit.block.Sign;
 import org.bukkit.configuration.ConfigurationSection;
 import org.bukkit.configuration.file.YamlConfiguration;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.EntityType;
 import org.bukkit.entity.Minecart;
 import org.bukkit.entity.Player;
 import org.bukkit.entity.Vehicle;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.Action;
 import org.bukkit.event.block.BlockBreakEvent;
 import org.bukkit.event.block.BlockPlaceEvent;
 import org.bukkit.event.block.SignChangeEvent;
 import org.bukkit.event.enchantment.EnchantItemEvent;
 import org.bukkit.event.entity.EntityDamageByEntityEvent;
 import org.bukkit.event.entity.EntityDamageEvent;
 import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
 import org.bukkit.event.entity.EntityDeathEvent;
 import org.bukkit.event.entity.EntityExplodeEvent;
 import org.bukkit.event.entity.ExplosionPrimeEvent;
 import org.bukkit.event.entity.PlayerDeathEvent;
 import org.bukkit.event.inventory.InventoryClickEvent;
 import org.bukkit.event.inventory.InventoryOpenEvent;
 import org.bukkit.event.player.AsyncPlayerChatEvent;
 import org.bukkit.event.player.PlayerEggThrowEvent;
 import org.bukkit.event.player.PlayerEvent;
 import org.bukkit.event.player.PlayerExpChangeEvent;
 import org.bukkit.event.player.PlayerFishEvent;
 import org.bukkit.event.player.PlayerInteractEntityEvent;
 import org.bukkit.event.player.PlayerInteractEvent;
 import org.bukkit.event.player.PlayerJoinEvent;
 import org.bukkit.event.player.PlayerKickEvent;
 import org.bukkit.event.player.PlayerLevelChangeEvent;
 import org.bukkit.event.player.PlayerMoveEvent;
 import org.bukkit.event.player.PlayerPortalEvent;
 import org.bukkit.event.player.PlayerQuitEvent;
 import org.bukkit.event.player.PlayerRespawnEvent;
 import org.bukkit.event.player.PlayerShearEntityEvent;
 import org.bukkit.event.player.PlayerTeleportEvent;
 import org.bukkit.event.player.PlayerToggleSprintEvent;
 import org.bukkit.event.vehicle.VehicleEnterEvent;
 import org.bukkit.event.vehicle.VehicleUpdateEvent;
 import org.bukkit.inventory.Inventory;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.permissions.PermissionAttachment;
 import org.bukkit.permissions.PermissionAttachmentInfo;
 import org.bukkit.plugin.Plugin;
 import org.bukkit.plugin.PluginDescriptionFile;
 import org.bukkit.scoreboard.Scoreboard;
 import org.bukkit.scoreboard.Team;
 import org.bukkit.util.Vector;
 
 import com.amazar.utils.Arena;
 import com.amazar.utils.ArenaCtf;
 import com.amazar.utils.ArenaPush;
 import com.amazar.utils.ArenaPvp;
 import com.amazar.utils.ArenaSurvival;
 import com.amazar.utils.ArenaTeams;
 import com.amazar.utils.ArenaTntori;
 import com.amazar.utils.ArenaType;
 import com.amazar.utils.ItemStackFromId;
 import com.amazar.utils.Minigame;
 import com.amazar.utils.MinigameFinishEvent;
 import com.amazar.utils.MinigameStartEvent;
 import com.amazar.utils.MinigameUpdateEvent;
 import com.amazar.utils.Profile;
 import com.amazar.utils.StringColors;
 import com.amazar.utils.UCarsArena;
 import com.useful.ucars.uCarsListener;
 
 public class AcListener implements Listener {
 	
 	private ac plugin = null;
 	
 	public AcListener(ac instance){
 		this.plugin = ac.plugin;
 	}
 	
 	@EventHandler (priority = EventPriority.HIGHEST)
 	void playerChat(AsyncPlayerChatEvent event){
 		Player player = event.getPlayer();
 		if(ac.clanMembers.containsKey(player.getName())){
 			boolean exists = false;
 			Object[] clans =  ac.clans.getValues().toArray();
 			for(int i=0; i<clans.length;i++){
 				String clan = (String) clans[i];
 				if(ChatColor.stripColor(StringColors.colorise(clan)).equalsIgnoreCase(ChatColor.stripColor(StringColors.colorise(ac.clanMembers.get(player.getName()))))){
 					exists = true;
 				}
 			}
 			if(!exists){
 				ac.clanMembers.remove(player.getName());
 				player.setDisplayName(ChatColor.DARK_GRAY+player.getName() + ChatColor.RESET);
 			}
 			else{
 			player.setDisplayName(ChatColor.RED + "[" + ChatColor.GOLD + StringColors.colorise(ac.clanMembers.get(player.getName())) + ChatColor.RED + "]" + ChatColor.RESET + "" + ChatColor.DARK_GRAY + player.getName() + ChatColor.RESET);
 			}
 		}
 		return;
 	}
 	@EventHandler (priority = EventPriority.HIGHEST)
 	void miniGameEnd(MinigameFinishEvent event){
 		Minigame game = event.getGame();
 		if(plugin.gameScheduler.arenaInUse(game.getArenaName())){
 			plugin.gameScheduler.removeArena(game.getArenaName());
 		}
 		List<String> players = new ArrayList<String>();
 		players.addAll(game.getPlayers());
 		List<String> inplayers = game.getInPlayers();
 		String in = "";
 		for(String inp:inplayers){
 			in = in+", "+inp; 
 		}
 		for(String playername:players){
 				Player player = plugin.getServer().getPlayer(playername);
 				player.setCustomName(ChatColor.stripColor(player.getCustomName()));
 				player.setCustomNameVisible(false);
 				String pname = player.getName();
 				try {
 					if(game.getTeams().getTeam("blue"+game.getGameId()).getPlayers().contains(ac.plugin.getServer().getOfflinePlayer(pname))){
 						game.getTeams().getTeam("blue"+game.getGameId()).removePlayer(ac.plugin.getServer().getOfflinePlayer(pname));
 					}
 				} catch (Exception e) {
 				}
 				try {
 					if(game.getTeams().getTeam("red"+game.getGameId()).getPlayers().contains(ac.plugin.getServer().getOfflinePlayer(pname))){
 						game.getTeams().getTeam("red"+game.getGameId()).removePlayer(ac.plugin.getServer().getOfflinePlayer(pname));
 					}
 				} catch (Exception e) {
 				}
 				Location loc = plugin.mgLobbies.getLobby(game.getGameType());
 				if(loc == null){
 				player.teleport(player.getLocation().getWorld().getSpawnLocation());
 				player.setBedSpawnLocation(player.getLocation().getWorld().getSpawnLocation(), true);
 				}
 				else{
 					player.teleport(loc);
 					player.setBedSpawnLocation(loc, true);
 				}
 				if(game.getGameType() == ArenaType.UCARS){
 					player.getInventory().clear();
 					player.setHealth(0);
 					List<String> inners = new ArrayList<String>();
 					inners.addAll(inplayers);
 					for(String tplayername:inners){
 						if(tplayername != game.getWinner()){
 							inplayers.remove(tplayername);
 						}
 					}
 				}
 				if(player.isOnline()){
 					if(!inplayers.contains(playername)){
 						Profile pProfile = new Profile(playername);
 						pProfile.addRewardPoint(3);
 						player.sendMessage(ChatColor.RED+"+3 reward points!");
 						player.getInventory().clear();
 						if(game.getOldInventories().containsKey(player.getName())){
 							player.getInventory().setContents(game.getOldInventories().get(player.getName()));
 						}
 					}
 					else{
 						Profile pProfile = new Profile(playername);
 						pProfile.addRewardPoint(10);
 						player.sendMessage(ChatColor.RED+"+10 reward points!");
 					}
 					player.sendMessage(ChatColor.GOLD+game.getWinner() + " won the game!");
 					player.sendMessage(ChatColor.GREEN+"Winner(s): "+in);
 				}
 				player.getInventory().clear();
 				if(game.getOldInventories().containsKey(player.getName())){
 				player.getInventory().setContents(game.getOldInventories().get(player.getName()));
 				}
 		}
 		if(game.getGameType() == ArenaType.SURVIVAL){
 			ArenaSurvival gameArena = (ArenaSurvival) game.getArena();
 			plugin.mgMethods.purgeMobs(gameArena);
 		}
 		plugin.gameScheduler.reCalculateQues();
 		return;
 	}
 	@EventHandler void mgSigns(SignChangeEvent event){
 		String[] lines = event.getLines();
 		if(ChatColor.stripColor(lines[0]).equalsIgnoreCase("[Minigame]")){
 			lines[0] = ChatColor.GREEN+"[Minigame]";
 			lines[1] = ChatColor.GOLD+lines[1];
 			lines[2] = ChatColor.RED+lines[2];
 			lines[3] = ChatColor.AQUA+lines[3];
 		}
 		return;
 	}
 	@EventHandler void tntAutoLightTori(PlayerInteractEvent event){
 		if(event.getAction() != Action.RIGHT_CLICK_BLOCK){
 			return;
 		}
 		Block floor = event.getClickedBlock();
 		if(plugin.mgMethods.isArena(floor.getLocation()) == null){
 			return;
 		}
 		Arena arena = plugin.minigamesArenas.getArena(plugin.mgMethods.isArena(floor.getLocation()));
 		if(arena.getType() == ArenaType.TNTORI){
 				ItemStack item = event.getPlayer().getItemInHand();
 				if(item.getType() == Material.TNT){
 					Location loc = floor.getRelative(BlockFace.UP).getLocation().add(0,0.5,0);
 					loc.getWorld().spawnEntity(loc, EntityType.PRIMED_TNT);
 					Minigame inGame = plugin.mgMethods.inAGame(event.getPlayer().getName());
 					if(inGame != null){
 						ArenaType type = inGame.getGameType();
 						if(type == ArenaType.TNTORI){
 						event.getPlayer().getInventory().addItem(new ItemStack(Material.TNT));
 						}
 					}
 					event.setCancelled(true);
 				}
 		}
 		return;
 	}
 	@EventHandler
 	void gameQuitting(PlayerQuitEvent event){
 		Player player = event.getPlayer();
 		Minigame game = plugin.mgMethods.inAGame(player.getName());
 		if(game == null){
 			String arenaName = plugin.mgMethods.inGameQue(player.getName());
 			if(arenaName == null){
 				return;
 			}
 			Arena arena = plugin.minigamesArenas.getArena(arenaName);
 			arena.removePlayer(player.getName());
 			plugin.minigamesArenas.setArena(arenaName, arena);
 			return;
 		}
 		else{
 			game.leave(player.getName());
 			return;
 	    }
 	}
 	@EventHandler
 	void gameQuitting(PlayerKickEvent event){
 		Player player = event.getPlayer();
 		Minigame game = plugin.mgMethods.inAGame(player.getName());
 		if(game == null){
 			String arenaName = plugin.mgMethods.inGameQue(player.getName());
 			if(arenaName == null){
 				return;
 			}
 			Arena arena = plugin.minigamesArenas.getArena(arenaName);
 			arena.removePlayer(player.getName());
 			plugin.minigamesArenas.setArena(arenaName, arena);
 			return;
 		}
 		else{
 			game.leave(player.getName());
 			return;
 	    }
 	}
 	@EventHandler
 	void arenaLogonLoc(PlayerJoinEvent event){
 		Player player = event.getPlayer();
 		if(plugin.mgMethods.isArena(player.getLocation()) == null){ //Not in arena
 			return;
 		}
 		if(plugin.mgMethods.inAGame(player.getName()) != null){ //Are in a game
 			return;
 		}
 		//Inside an arena but not playing? OUTRAGEOUS!
 		player.teleport(player.getWorld().getSpawnLocation());
 		return;
 	}
 	@EventHandler (priority = EventPriority.HIGHEST)
 	void tntoriProtect(EntityExplodeEvent event){
 		List<Block> blocks = new ArrayList<Block>();
 		blocks.addAll(event.blockList());
 		for(Block bl:blocks){
 			if(!(plugin.mgMethods.isArena(bl.getLocation()) == null)){
 			Arena arena = plugin.minigamesArenas.getArena(plugin.mgMethods.isArena(bl.getLocation()));
 			if(arena.getType() == ArenaType.TNTORI){
 				ArenaTntori arenaGame = (ArenaTntori) arena;
 				if(arenaGame.isProtected()){
 					event.blockList().remove(bl);
 				}
 			}
 			}
 		}
 		return;
 	}
 	@EventHandler void tntToriDamagePrevent(EntityDamageEvent event){
 		if(!(event.getEntity() instanceof Player)){
 			return;
 		}
 		Player player = (Player) event.getEntity();
 		Minigame minigame = plugin.mgMethods.inAGame(player.getName());
 		if(minigame == null){
 			return;
 		}
 		if(minigame.getGameType() == ArenaType.TNTORI){
 			if(event.getCause() == DamageCause.BLOCK_EXPLOSION){
 				player.setNoDamageTicks(100);
 			}
 			else{
 			event.setCancelled(true);
 			}
 		}
 		return;
 	}
 	@EventHandler void mgDie(PlayerDeathEvent event){
 		if(plugin.mgMethods.inAGame(((Player)event.getEntity()).getName()) == null){
 			return;
 		}
 		Player player = event.getEntity();
 		Minigame game = plugin.mgMethods.inAGame(player.getName());
 		if(game.getGameType() == ArenaType.TNTORI){
 			ArenaTntori gameArena = (ArenaTntori) game.getArena();
 			gameArena.getCenter().getBlock().setType(Material.GLOWSTONE);
 			player.setBedSpawnLocation(gameArena.getCenter().getBlock().getRelative(BlockFace.UP, 2).getLocation(), true);
 			player.getInventory().addItem(new ItemStack(Material.TNT));
 			return;
 		}
 		else if(game.getGameType() == ArenaType.PUSH){
 			ArenaPush gameArena = (ArenaPush) game.getArena();
 			gameArena.getCenter().getBlock().setType(Material.GLOWSTONE);
 			player.setBedSpawnLocation(gameArena.getCenter().getBlock().getRelative(BlockFace.UP, 2).getLocation(), true);
 			return;
 		}
 		else if(game.getGameType() == ArenaType.SURVIVAL){
 			Player playerOuted = event.getEntity();
 			ArenaSurvival gameArena = (ArenaSurvival) game.getArena();
 			game.playerOut(playerOuted.getName());
 			for(String pname:game.getPlayers()){
 				plugin.getServer().getPlayer(pname).sendMessage(ChatColor.RED+playerOuted.getName()+" is out! Now there are only "+game.getInPlayers().size()+" players left!");
 			}
 			Block block = gameArena.getCenter().getBlock().getRelative(BlockFace.UP, 5).getRelative(BlockFace.NORTH, 10);
 			block.setType(Material.GLASS);
 			block.getRelative(BlockFace.NORTH).setType(Material.GLASS);
 			block.getRelative(BlockFace.NORTH_EAST).setType(Material.GLASS);
 			block.getRelative(BlockFace.EAST).setType(Material.GLASS);
 			block.getRelative(BlockFace.SOUTH_EAST).setType(Material.GLASS);
 			block.getRelative(BlockFace.SOUTH).setType(Material.GLASS);
 			block.getRelative(BlockFace.SOUTH_WEST).setType(Material.GLASS);
 			block.getRelative(BlockFace.WEST).setType(Material.GLASS);
 			block.getRelative(BlockFace.NORTH_WEST).setType(Material.GLASS);
 			playerOuted.sendMessage(ChatColor.GOLD+"Spectating... To leave (and miss out on reward points) please do /mg leave");
 			Location spectator = new Location(gameArena.getCenter().getWorld(), gameArena.getCenter().getX(), gameArena.getCenter().getY(), gameArena.getCenter().getZ()).getBlock().getRelative(BlockFace.NORTH, 10).getLocation();
 			playerOuted.teleport(spectator.add(0,6,0));
 			plugin.gameScheduler.updateGame(game);
 		}
 		else if(game.getGameType() == ArenaType.TEAMS){
 			ArenaTeams gameArena = (ArenaTeams) game.getArena();
 			ChatColor team_color = ChatColor.WHITE;
 			if(game.getBlue().contains(player.getName())){
 				team_color = ChatColor.BLUE;
 			}
 			else if(game.getRed().contains(player.getName())){
 				team_color = ChatColor.RED;
 			}
 			Player playerOuted = event.getEntity();
 			game.playerOut(playerOuted.getName());
 			for(String pname:game.getPlayers()){
 				if(team_color == ChatColor.BLUE){
 					plugin.getServer().getPlayer(pname).sendMessage(team_color+playerOuted.getName()+" is out! Now there are only "+game.getBlue().size()+" players left on the blue team!");
 				}
 				else if(team_color == ChatColor.RED){
 					plugin.getServer().getPlayer(pname).sendMessage(team_color+playerOuted.getName()+" is out! Now there are only "+game.getRed().size()+" players left on the red team!");
 				}
 				else{
 					plugin.getServer().getPlayer(pname).sendMessage(team_color+playerOuted.getName()+" is out! Now there are only "+game.getInPlayers().size()+" players left in the game!");
 				}
 			}
 			Block block = gameArena.getCenter().getBlock().getRelative(BlockFace.UP, 5).getRelative(BlockFace.NORTH, 10);
 			block.setType(Material.GLASS);
 			block.getRelative(BlockFace.NORTH).setType(Material.GLASS);
 			block.getRelative(BlockFace.NORTH_EAST).setType(Material.GLASS);
 			block.getRelative(BlockFace.EAST).setType(Material.GLASS);
 			block.getRelative(BlockFace.SOUTH_EAST).setType(Material.GLASS);
 			block.getRelative(BlockFace.SOUTH).setType(Material.GLASS);
 			block.getRelative(BlockFace.SOUTH_WEST).setType(Material.GLASS);
 			block.getRelative(BlockFace.WEST).setType(Material.GLASS);
 			block.getRelative(BlockFace.NORTH_WEST).setType(Material.GLASS);
 			playerOuted.sendMessage(ChatColor.GOLD+"Spectating... To leave (and miss out on reward points) please do /mg leave");
 			Location spectator = new Location(gameArena.getCenter().getWorld(), gameArena.getCenter().getX(), gameArena.getCenter().getY(), gameArena.getCenter().getZ()).getBlock().getRelative(BlockFace.NORTH, 10).getLocation();
 			playerOuted.teleport(spectator.add(0,6,0));
 			plugin.gameScheduler.updateGame(game);
 		}
 		else if(game.getGameType() == ArenaType.PVP){
 			ArenaPvp gameArena = (ArenaPvp) game.getArena();
 			ChatColor team_color = ChatColor.WHITE;
 			if(game.getBlue().contains(player.getName())){
 				team_color = ChatColor.BLUE;
 			}
 			else if(game.getRed().contains(player.getName())){
 				team_color = ChatColor.RED;
 			}
 			Boolean out = false;
 			int lives = gameArena.getLives();
 			if(game.lives.containsKey(player.getName())){
 				lives = game.lives.get(player.getName());
 			}
 			lives = lives -1;
 			if(lives < 0){
 				out = true;
 			}
 			else{
 			game.lives.put(player.getName(), lives);
 			for(String pname:game.getPlayers()){
 				plugin.getServer().getPlayer(pname).sendMessage(team_color+player.getName()+" died and how has only "+lives+" lives left!");
 			}
 			if(team_color == ChatColor.BLUE){
 				player.setBedSpawnLocation(gameArena.getPlayerBlueSpawn(), true);
 			}
 			else if(team_color == ChatColor.RED){
 				player.setBedSpawnLocation(gameArena.getPlayerRedSpawn(), true);
 			}
 			else{
 				player.setBedSpawnLocation(gameArena.getCenter().getBlock().getRelative(BlockFace.UP).getLocation());
 			}
 			
 			}
 			if(out){
 			Player playerOuted = event.getEntity();
 			game.playerOut(playerOuted.getName());
 			for(String pname:game.getPlayers()){
 				plugin.getServer().getPlayer(pname).sendMessage(team_color+playerOuted.getName()+" is out! Now there are only "+game.getInPlayers().size()+" players left!");
 			}
 			Block block = gameArena.getCenter().getBlock().getRelative(BlockFace.UP, 5).getRelative(BlockFace.NORTH, 10);
 			block.setType(Material.GLASS);
 			block.getRelative(BlockFace.NORTH).setType(Material.GLASS);
 			block.getRelative(BlockFace.NORTH_EAST).setType(Material.GLASS);
 			block.getRelative(BlockFace.EAST).setType(Material.GLASS);
 			block.getRelative(BlockFace.SOUTH_EAST).setType(Material.GLASS);
 			block.getRelative(BlockFace.SOUTH).setType(Material.GLASS);
 			block.getRelative(BlockFace.SOUTH_WEST).setType(Material.GLASS);
 			block.getRelative(BlockFace.WEST).setType(Material.GLASS);
 			block.getRelative(BlockFace.NORTH_WEST).setType(Material.GLASS);
 			playerOuted.sendMessage(ChatColor.GOLD+"Spectating... To leave (and miss out on reward points) please do /mg leave");
 			Location spectator = new Location(gameArena.getCenter().getWorld(), gameArena.getCenter().getX(), gameArena.getCenter().getY(), gameArena.getCenter().getZ()).getBlock().getRelative(BlockFace.NORTH, 10).getLocation();
 			playerOuted.teleport(spectator.add(0,6,0));
 			}
 			plugin.gameScheduler.updateGame(game);
 		}
 		else if(game.getGameType() == ArenaType.CTF){
 			ArenaCtf gameArena = (ArenaCtf) game.getArena();
 			ChatColor team_color = ChatColor.WHITE;
 			if(game.getBlue().contains(player.getName())){
 				team_color = ChatColor.BLUE;
 			}
 			else if(game.getRed().contains(player.getName())){
 				team_color = ChatColor.RED;
 			}
 			if(team_color == ChatColor.BLUE){
 				player.setBedSpawnLocation(gameArena.getBlueSpawnpoint(), true);
 			}
 			else if(team_color == ChatColor.RED){
 				player.setBedSpawnLocation(gameArena.getRedSpawnpoint(), true);
 			}
 			else{
 				player.setBedSpawnLocation(gameArena.getCenter().getBlock().getRelative(BlockFace.UP).getLocation());
 			}
 		}
 		return;
 	}
 	@EventHandler (priority = EventPriority.HIGHEST)
 	void miniGameStart(MinigameStartEvent event){
 		Minigame game = event.getGame();
 		List<String> blue = game.getBlue();
 		List<String> red = game.getRed();
 		List<String> players = game.getPlayers();
 		for(String name:blue){
 			Player p = plugin.getServer().getPlayer(name);
 			p.setCustomName(ChatColor.BLUE+p.getName());
 			p.setCustomNameVisible(true);
 		}
 		for(String name:red){
 			Player p = plugin.getServer().getPlayer(name);
 			p.setCustomName(ChatColor.RED+p.getName());
 			p.setCustomNameVisible(true);
 		}
 		for(String pname:players){
 			plugin.getServer().getPlayer(pname).setGameMode(GameMode.SURVIVAL);
 			plugin.getServer().getPlayer(pname).getInventory().clear();
 		}
 		if(game.getGameType() == ArenaType.UCARS){
 			UCarsArena gameArena = (UCarsArena) game.getArena();
 			for(int i=0;i<game.getPlayers().size();i++){
 				String pname = game.getPlayers().get(i);
 				Player player = plugin.getServer().getPlayer(pname);
 				plugin.gameScheduler.updateGame(game);
 				player.setWalkSpeed((float) 0.2);
 				if(game.lapsLeft.containsKey(pname)){
 					game.lapsLeft.put(pname, gameArena.getLaps());
 				}
 				String[] items = gameArena.getItems();
 				for(String raw:items){
 					if(!(raw.equalsIgnoreCase("0") || raw.equalsIgnoreCase("0:0"))){
 						try {
 							ItemStack item = ItemStackFromId.get(raw);
 								player.getInventory().addItem(item);
 							
 						} catch (IllegalArgumentException e) {
 							
 						}	
 					}
 					
 				}
 			}
 			
 		}
 		if(game.getGameType() == ArenaType.TNTORI){
 			ArenaTntori gameArena = (ArenaTntori) game.getArena();
 			String[] items = gameArena.getItems();
 			for(String raw:items){
 				if(!(raw.equalsIgnoreCase("0") || raw.equalsIgnoreCase("0:0"))){
 					try {
 						ItemStack item = ItemStackFromId.get(raw);
 						for(String pname:players){
 							plugin.getServer().getPlayer(pname).getInventory().addItem(item);
 						}
 					} catch (IllegalArgumentException e) {
 						
 					}	
 				}
 				
 			}
 			for(String pname:players){
 				plugin.getServer().getPlayer(pname).getInventory().addItem(new ItemStack(Material.TNT, 1));
 			}
 		}
 		else if(game.getGameType() == ArenaType.PUSH){
 			ArenaPush gameArena = (ArenaPush) game.getArena();
 			String[] items = gameArena.getItems();
 			for(String raw:items){
 				if(!(raw.equalsIgnoreCase("0") || raw.equalsIgnoreCase("0:0"))){
 					try {
 						ItemStack item = ItemStackFromId.get(raw);
 						for(String pname:players){
 							plugin.getServer().getPlayer(pname).getInventory().addItem(item);
 						}
 					} catch (IllegalArgumentException e) {
 						
 					}	
 				}
 				
 			}
 		}
 		else if(game.getGameType() == ArenaType.SURVIVAL){
 			ArenaSurvival gameArena = (ArenaSurvival) game.getArena();
 			String[] items = gameArena.getItems();
 			for(String raw:items){
 				if(!(raw.equalsIgnoreCase("0") || raw.equalsIgnoreCase("0:0"))){
 					try {
 						ItemStack item = ItemStackFromId.get(raw);
 						for(String pname:players){
 							plugin.getServer().getPlayer(pname).getInventory().addItem(item);
 						}
 					} catch (IllegalArgumentException e) {
 						
 					}	
 				}
 				
 			}
 			for(String pname:players){
 				plugin.getServer().getPlayer(pname).getInventory().addItem(new ItemStack(Material.IRON_SWORD));
 			}
 			if(gameArena.getDoCountdown()){
 			game.setCount(gameArena.getCountdownStartPoint());
 			}
 			game.startCountDown();
 			plugin.gameScheduler.updateGame(game);
 		}
 		else if(game.getGameType() == ArenaType.PVP){
 			ArenaPvp gameArena = (ArenaPvp) game.getArena();
 			String[] items = gameArena.getItems();
 			for(String raw:items){
 				if(!(raw.equalsIgnoreCase("0") || raw.equalsIgnoreCase("0:0"))){
 					try {
 						ItemStack item = ItemStackFromId.get(raw);
 						for(String pname:players){
 							plugin.getServer().getPlayer(pname).getInventory().addItem(item);
 						}
 					} catch (IllegalArgumentException e) {
 						
 					}	
 				}
 				
 			}
 			for(String pname:players){
 				plugin.getServer().getPlayer(pname).getInventory().addItem(new ItemStack(Material.IRON_SWORD));
 			}
 		}
 		else if(game.getGameType() == ArenaType.TEAMS){
 			ArenaTeams gameArena = (ArenaTeams) game.getArena();
 			gameArena.validatePlayers();
 			String[] items = gameArena.getItems();
 			for(String raw:items){
 				if(!(raw.equalsIgnoreCase("0") || raw.equalsIgnoreCase("0:0"))){
 					try {
 						ItemStack item = ItemStackFromId.get(raw);
 						for(String pname:players){
 							plugin.getServer().getPlayer(pname).getInventory().addItem(item);
 						}
 					} catch (IllegalArgumentException e) {
 						
 					}	
 				}
 				
 			}
 			for(String pname:players){
 				plugin.getServer().getPlayer(pname).getInventory().addItem(new ItemStack(Material.IRON_SWORD));
 			}
 		}
 		else if(game.getGameType() == ArenaType.CTF){
 			ArenaCtf gameArena = (ArenaCtf) game.getArena();
 			gameArena.validatePlayers();
 			String[] items = gameArena.getItems();
 			for(String raw:items){
 				if(!(raw.equalsIgnoreCase("0") || raw.equalsIgnoreCase("0:0"))){
 					try {
 						ItemStack item = ItemStackFromId.get(raw);
 						for(String pname:players){
 							plugin.getServer().getPlayer(pname).getInventory().addItem(item);
 						}
 					} catch (IllegalArgumentException e) {
 						
 					}	
 				}
 				
 			}
 			for(String pname:players){
 				plugin.getServer().getPlayer(pname).getInventory().addItem(new ItemStack(Material.IRON_SWORD));
 			}
 		}
 		plugin.gameScheduler.reCalculateQues();
 		return;
 	}
 	@EventHandler
 	void miniGameChat(AsyncPlayerChatEvent event){
 		Player player = event.getPlayer();
 		Minigame game = plugin.mgMethods.inAGame(player.getName());
 		if(game == null){
 			return;
 		}
 		String msg = StringColors.colorise(event.getMessage());
 		for(String name: game.getPlayers()){
 			Player toSend = plugin.getServer().getPlayer(name);
 			ChatColor teamColor = ChatColor.WHITE;
 			if(game.getBlue().contains(player.getName())){
 				teamColor = ChatColor.BLUE;
 			}
 			if(game.getRed().contains(player.getName())){
 				teamColor = ChatColor.RED;
 			}
 			if(toSend.isOnline() && toSend != null){
 				toSend.sendMessage(ChatColor.RED+"["+game.getArenaName()+"]"+teamColor+"<"+player.getName()+">"+ChatColor.GOLD+msg);
 			}
 		}
 		event.setCancelled(true);
 		return;
 	}
 	@EventHandler
 	void mgOpen(InventoryOpenEvent event){
 		Player player = (Player) event.getPlayer();
 		Inventory inv = event.getInventory();
         if(ChatColor.stripColor(inv.getItem(0).getItemMeta().getDisplayName()).equalsIgnoreCase("CTF") && ChatColor.stripColor(inv.getItem(2).getItemMeta().getDisplayName()).equalsIgnoreCase("PUSH") && ChatColor.stripColor(inv.getItem(3).getItemMeta().getDisplayName()).equalsIgnoreCase("PVP")&&ChatColor.stripColor(inv.getItem(4).getItemMeta().getDisplayName()).equalsIgnoreCase("SURVIVAL")){
 			player.sendMessage(ChatColor.RED+"Minigame selection:");
         }
         return;
 	}
 	@EventHandler
 	void mgSelect(InventoryClickEvent event){
 		Player player = (Player) event.getWhoClicked();
 		Inventory inv = event.getInventory();
         try {
 			if(ChatColor.stripColor(inv.getItem(1).getItemMeta().getDisplayName()).equalsIgnoreCase("CTF") && ChatColor.stripColor(inv.getItem(2).getItemMeta().getDisplayName()).equalsIgnoreCase("PUSH") && ChatColor.stripColor(inv.getItem(3).getItemMeta().getDisplayName()).equalsIgnoreCase("PVP")&&ChatColor.stripColor(inv.getItem(4).getItemMeta().getDisplayName()).equalsIgnoreCase("SURVIVAL")){
 				int slot = event.getSlot();
 				ArenaType type = ArenaType.INAVLID;
 				if(slot == 1){
 					type = ArenaType.CTF;
 				}
 				else if(slot == 2){
 					type = ArenaType.PUSH;
 				}
 				else if(slot == 3){
 					type = ArenaType.PVP;
 				}
 				else if(slot == 4){
 					type = ArenaType.SURVIVAL;
 				}
 				else if(slot == 5){
 					type = ArenaType.TEAMS;
 				}
 				else if(slot == 6){
 					type = ArenaType.TNTORI;
 				}
 				else if(slot == 7){
 					type = ArenaType.UCARS;
 				}
 				else if(slot == 0){
 					player.closeInventory();
 					player.performCommand("mg games");
 					event.setCancelled(true);
 					return;
 				}
 				else{
 					event.setCancelled(true);
 					return;
 				}
 				Location loc = plugin.mgLobbies.getLobby(type);
 				if(loc == null){
 					loc = player.getWorld().getSpawnLocation();
 					player.closeInventory();
 					player.teleport(loc);
 					player.sendMessage(ChatColor.RED+"No game lobby found! Teleported to spawn instead!");
 					event.setCancelled(true);
 					return;
 				}
 				player.closeInventory();
 				player.teleport(loc);
 				player.sendMessage(ChatColor.GOLD+"Teleported to the "+type.toString().toLowerCase()+" lobby!");
 				event.setCancelled(true);
 			}
 		} catch (Exception e) {
 			return;
 		}
         return;
 	}
 	@EventHandler
 	void miniGameHandler(MinigameUpdateEvent event){
 		Minigame game = event.getGame();
 		if(!game.getRunning()){
 			plugin.gameScheduler.stopGame(game.getArena(), game.getArenaName());
 			plugin.gameScheduler.reCalculateQues();
 			return;
 		}
 		Arena arena = game.getArena();
 		//String arenaName = game.getArenaName();
 		ArenaType type = game.getGameType();
 		if(type == ArenaType.TNTORI){
 			ArenaTntori gameArena = (ArenaTntori) arena;
 			List<String> inplayers = game.getInPlayers();
 			List<String> players = game.getPlayers();
 			List<String> blue = game.getBlue();
 			List<String> red = game.getRed();
 			ChatColor team_color = ChatColor.WHITE;
 			List<String> tplayers = new ArrayList<String>();
 			tplayers.addAll(players);
 			for(String pname:tplayers){
 				Player p = plugin.getServer().getPlayer(pname);
 				if(!inplayers.contains(p.getName())){
 					//They are spectating
 					Location spectator = new Location(arena.getCenter().getWorld(), arena.getCenter().getX(), arena.getCenter().getY(), arena.getCenter().getZ()).getBlock().getRelative(BlockFace.NORTH, 10).getLocation();
 					if(p.getLocation().getY() < (arena.getCenter().getY()+5)){
 						p.teleport(spectator.add(0,6,0));
 					}
 				}
 				if(!arena.isLocInArena(p.getLocation())){
 					int totalLives = gameArena.getLives();
 					int lives = totalLives;
 					if(game.lives.containsKey(p.getName())){
 						lives = game.lives.get(p.getName());
 					}
 					lives -= 1;
 					game.lives.put(pname, lives);
 					if(blue.contains(pname)){
 						team_color = ChatColor.BLUE;
 					}
 					if(red.contains(pname)){
 						team_color = ChatColor.RED;
 					}
 					if(lives < 0){
 					//they lose
 						if(blue.contains(pname)){
 							blue.remove(pname);
 						}
 						if(red.contains(pname)){
 							red.remove(pname);
 						}
 						if(game.getTeams().getTeam("blue"+game.getGameId()).getPlayers().contains(ac.plugin.getServer().getOfflinePlayer(pname))){
 							game.getTeams().getTeam("blue"+game.getGameId()).removePlayer(ac.plugin.getServer().getOfflinePlayer(pname));
 						}
 						if(game.getTeams().getTeam("red"+game.getGameId()).getPlayers().contains(ac.plugin.getServer().getOfflinePlayer(pname))){
 							game.getTeams().getTeam("red"+game.getGameId()).removePlayer(ac.plugin.getServer().getOfflinePlayer(pname));
 						}
 					Player playerOuted = plugin.getServer().getPlayer(pname);
 					game.playerOut(playerOuted.getName());
 					if(inplayers.contains(playerOuted.getName())){
 					inplayers.remove(playerOuted.getName());
 					}
 					plugin.gameScheduler.updateGame(game);
 					Block block = arena.getCenter().getBlock().getRelative(BlockFace.UP, 5).getRelative(BlockFace.NORTH, 10);
 					block.setType(Material.GLASS);
 					block.getRelative(BlockFace.NORTH).setType(Material.GLASS);
 					block.getRelative(BlockFace.NORTH_EAST).setType(Material.GLASS);
 					block.getRelative(BlockFace.EAST).setType(Material.GLASS);
 					block.getRelative(BlockFace.SOUTH_EAST).setType(Material.GLASS);
 					block.getRelative(BlockFace.SOUTH).setType(Material.GLASS);
 					block.getRelative(BlockFace.SOUTH_WEST).setType(Material.GLASS);
 					block.getRelative(BlockFace.WEST).setType(Material.GLASS);
 					block.getRelative(BlockFace.NORTH_WEST).setType(Material.GLASS);
 					playerOuted.sendMessage(ChatColor.GOLD+"Spectating... To leave (and miss out on reward points) please do /mg leave");
 					Location spectator = new Location(arena.getCenter().getWorld(), arena.getCenter().getX(), arena.getCenter().getY(), arena.getCenter().getZ()).getBlock().getRelative(BlockFace.NORTH, 10).getLocation();
 					playerOuted.teleport(spectator.add(0,6,0));
 					for(String player:tplayers){
 						Player pl = plugin.getServer().getPlayer(player);
 						pl.sendMessage(team_color + pname+ " was knocked off and is out!");
 					}
 					}
 					else{
 						gameArena.getCenter().getBlock().setType(Material.GLOWSTONE);
 						p.teleport(arena.getCenter().getBlock().getRelative(BlockFace.UP, 2).getLocation());
 						for(String player:players){
 							Player pl = plugin.getServer().getPlayer(player);
 							pl.sendMessage(team_color + pname+ " was knocked off and now has "+lives+" lives left!");
 						}
 					}
 				}
 			}
 			//game.setInPlayers(inplayers);
 			
 			plugin.gameScheduler.updateGame(game);
             if(game.getInPlayers().size() < 2){
             	if(inplayers.size() < 1){
             		team_color = ChatColor.GOLD;
     				for(String player:tplayers){
     					Player pl = plugin.getServer().getPlayer(player);
     					pl.sendMessage(team_color + "Game end!");
     				}
     				game.setWinner(team_color+"Nobody");
     				game.end();
             	}
             	if(inplayers.size() > 0){
             	Player winner = plugin.getServer().getPlayer(inplayers.get(0));
             	if(red.contains(winner.getName())){
     				team_color = ChatColor.RED;
     				for(String player:tplayers){
     					Player pl = plugin.getServer().getPlayer(player);
     					pl.sendMessage(team_color + "Game end!");
     				}
     				game.setWinner(team_color+winner.getName());
     				game.end();
     			}
     			if(blue.contains(winner.getName())){
     				team_color = ChatColor.BLUE;
     				for(String player:tplayers){
     					Player pl = plugin.getServer().getPlayer(player);
     					pl.sendMessage(team_color + "Game end!");
     				}
     				game.setWinner(team_color+winner.getName());
     				game.end();
     			}	
             	}
             	else{
             		team_color = ChatColor.GOLD;
     				for(String player:tplayers){
     					Player pl = plugin.getServer().getPlayer(player);
     					pl.sendMessage(team_color + "Game end!");
     				}
     				game.setWinner(team_color+"Unknown");
     				game.end();
             	} 			
 			}
 		}
 		else if(type == ArenaType.PUSH){
 			ArenaPush gameArena = (ArenaPush) arena;
 			List<String> inplayers = game.getInPlayers();
 			List<String> players = game.getPlayers();
 			List<String> blue = game.getBlue();
 			List<String> red = game.getRed();
 			ChatColor team_color = ChatColor.WHITE;
 			List<String> tplayers = new ArrayList<String>();
 			tplayers.addAll(players);
 			for(String pname:tplayers){
 				Player p = plugin.getServer().getPlayer(pname);
 				if(!inplayers.contains(p.getName())){
 					//They are spectating
 					Location spectator = new Location(arena.getCenter().getWorld(), arena.getCenter().getX(), arena.getCenter().getY(), arena.getCenter().getZ()).getBlock().getRelative(BlockFace.NORTH, 10).getLocation();
 					if(p.getLocation().getY() < (arena.getCenter().getY()+5)){
 						p.teleport(spectator.add(0,6,0));
 					}
 				}
 				if(!arena.isLocInArena(p.getLocation())){
 					int totalLives = gameArena.getLives();
 					int lives = totalLives;
 					if(game.lives.containsKey(p.getName())){
 						lives = game.lives.get(p.getName());
 					}
 					lives -= 1;
 					game.lives.put(pname, lives);
 					if(blue.contains(pname)){
 						team_color = ChatColor.BLUE;
 					}
 					if(red.contains(pname)){
 						team_color = ChatColor.RED;
 					}
 					if(lives < 0){
 					//they lose
 						if(blue.contains(pname)){
 							blue.remove(pname);
 						}
 						if(red.contains(pname)){
 							red.remove(pname);
 						}
 						if(game.getTeams().getTeam("blue"+game.getGameId()).getPlayers().contains(ac.plugin.getServer().getOfflinePlayer(pname))){
 							game.getTeams().getTeam("blue"+game.getGameId()).removePlayer(ac.plugin.getServer().getOfflinePlayer(pname));
 						}
 						if(game.getTeams().getTeam("red"+game.getGameId()).getPlayers().contains(ac.plugin.getServer().getOfflinePlayer(pname))){
 							game.getTeams().getTeam("red"+game.getGameId()).removePlayer(ac.plugin.getServer().getOfflinePlayer(pname));
 						}
 					Player playerOuted = plugin.getServer().getPlayer(pname);
 					game.playerOut(playerOuted.getName());
 					if(inplayers.contains(playerOuted.getName())){
 					inplayers.remove(playerOuted.getName());
 					}
 					plugin.gameScheduler.updateGame(game);
 					Block block = arena.getCenter().getBlock().getRelative(BlockFace.UP, 5).getRelative(BlockFace.NORTH, 10);
 					block.setType(Material.GLASS);
 					block.getRelative(BlockFace.NORTH).setType(Material.GLASS);
 					block.getRelative(BlockFace.NORTH_EAST).setType(Material.GLASS);
 					block.getRelative(BlockFace.EAST).setType(Material.GLASS);
 					block.getRelative(BlockFace.SOUTH_EAST).setType(Material.GLASS);
 					block.getRelative(BlockFace.SOUTH).setType(Material.GLASS);
 					block.getRelative(BlockFace.SOUTH_WEST).setType(Material.GLASS);
 					block.getRelative(BlockFace.WEST).setType(Material.GLASS);
 					block.getRelative(BlockFace.NORTH_WEST).setType(Material.GLASS);
 					playerOuted.sendMessage(ChatColor.GOLD+"Spectating... To leave (and miss out on reward points) please do /mg leave");
 					Location spectator = new Location(arena.getCenter().getWorld(), arena.getCenter().getX(), arena.getCenter().getY(), arena.getCenter().getZ()).getBlock().getRelative(BlockFace.NORTH, 10).getLocation();
 					playerOuted.teleport(spectator.add(0,6,0));
 					for(String player:tplayers){
 						Player pl = plugin.getServer().getPlayer(player);
 						pl.sendMessage(team_color + pname+ " was knocked off and is out!");
 					}
 					}
 					else{
 						gameArena.getCenter().getBlock().setType(Material.GLOWSTONE);
 						p.teleport(arena.getCenter().getBlock().getRelative(BlockFace.UP, 2).getLocation());
 						for(String player:players){
 							Player pl = plugin.getServer().getPlayer(player);
 							pl.sendMessage(team_color + pname+ " was knocked off and now has "+lives+" lives left!");
 						}
 					}
 				}
 			}
 			//game.setInPlayers(inplayers);
 			
 			plugin.gameScheduler.updateGame(game);
             if(game.getInPlayers().size() < 2){
             	if(inplayers.size() < 1){
             		team_color = ChatColor.GOLD;
     				for(String player:tplayers){
     					Player pl = plugin.getServer().getPlayer(player);
     					pl.sendMessage(team_color + "Game end!");
     				}
     				game.setWinner(team_color+"Nobody");
     				game.end();
             	}
             	if(inplayers.size() > 0){
             	Player winner = plugin.getServer().getPlayer(inplayers.get(0));
             	if(red.contains(winner.getName())){
     				team_color = ChatColor.RED;
     				for(String player:tplayers){
     					Player pl = plugin.getServer().getPlayer(player);
     					pl.sendMessage(team_color + "Game end!");
     				}
     				game.setWinner(team_color+winner.getName());
     				game.end();
     			}
     			if(blue.contains(winner.getName())){
     				team_color = ChatColor.BLUE;
     				for(String player:tplayers){
     					Player pl = plugin.getServer().getPlayer(player);
     					pl.sendMessage(team_color + "Game end!");
     				}
     				game.setWinner(team_color+winner.getName());
     				game.end();
     			}	
             	}
             	else{
             		team_color = ChatColor.GOLD;
     				for(String player:tplayers){
     					Player pl = plugin.getServer().getPlayer(player);
     					pl.sendMessage(team_color + "Game end!");
     				}
     				game.setWinner(team_color+"Unknown");
     				game.end();
             	}
     			
 			}
 		}
 		else if(game.getGameType() == ArenaType.SURVIVAL){
 			ArenaSurvival gameArena = (ArenaSurvival) game.getArena();
 			//String arenaName = game.getArenaName();
 			for(String playername:game.getPlayers()){
 				if(!game.getInPlayers().contains(playername)){
 					//They are spectating
 					Player p = plugin.getServer().getPlayer(playername);
 					Location spectator = new Location(arena.getCenter().getWorld(), arena.getCenter().getX(), arena.getCenter().getY(), arena.getCenter().getZ()).getBlock().getRelative(BlockFace.NORTH, 10).getLocation();
 					if(p.getLocation().getY() < (arena.getCenter().getY()+5)){
 						p.teleport(spectator.add(0,6,0));
 					}
 				}
 			}
 			for(String playername:game.getInPlayers()){
 				Player player = plugin.getServer().getPlayer(playername);
 				if(!gameArena.isLocInArena(player.getLocation())){
 					player.sendMessage(ChatColor.RED+"You may not go outside of the arena during a game. To leave please do /mg leave.");
 					player.teleport(gameArena.getPlayerSpawnpoint());
 				}
 			}
 			if(game.getInPlayers().size() < 1){
 				for(String playername:game.getPlayers()){
 					Player player = plugin.getServer().getPlayer(playername);
 					player.sendMessage(ChatColor.RED+"Survival game lost!");
 				}
 				game.setWinner("The enemy");
 				game.end();
 				return;
 			}
 			int rand = 1 + (int)(Math.random() * ((100 - 1) + 1));
 			if(rand <= 30){
 				//spawn a mob
 				int randomType = 1 + (int)(Math.random() * ((100 - 1) + 1)); //between 1 and 100
 				Location loc = gameArena.getEnemySpawnpoint();
 				if(randomType <= 50){ //50% chance
                    loc.getWorld().spawnEntity(loc, EntityType.ZOMBIE);					
 				}
 				else if(randomType <= 70){ //20% chance
 					 loc.getWorld().spawnEntity(loc, EntityType.SKELETON);	
 				}
 				else if(randomType <= 75){ //5% chance
 					 loc.getWorld().spawnEntity(loc, EntityType.SILVERFISH);	
 				}
 				else if(randomType <= 85){ //10% chance
 					 loc.getWorld().spawnEntity(loc, EntityType.SPIDER);	
 				}
 				else if(randomType <= 86){ //1% chance
 					 loc.getWorld().spawnEntity(loc, EntityType.SLIME);	
 				}
 				else if(randomType <= 90){ //4% chance
 					 loc.getWorld().spawnEntity(loc, EntityType.ENDERMAN);	
 				}
 				else{//10% chance
 					loc.getWorld().spawnEntity(loc, EntityType.PIG_ZOMBIE);	
 				}
 			}
 			plugin.gameScheduler.updateGame(game);
 		}
 		else if(game.getGameType() == ArenaType.TEAMS){
 			ArenaTeams gameArena = (ArenaTeams) game.getArena();
 			for(String playername:game.getPlayers()){
 				if(!game.getInPlayers().contains(playername)){
 					//They are spectating
 					Player p = plugin.getServer().getPlayer(playername);
 					Location spectator = new Location(arena.getCenter().getWorld(), arena.getCenter().getX(), arena.getCenter().getY(), arena.getCenter().getZ()).getBlock().getRelative(BlockFace.NORTH, 10).getLocation();
 					if(p.getLocation().getY() < (arena.getCenter().getY()+5)){
 						p.teleport(spectator.add(0,6,0));
 					}
 				}
 			}
 			List<String> blue = game.getBlue();
 			List<String> red = game.getRed();
 			for(String playername:game.getInPlayers()){
 				ChatColor team_color = ChatColor.WHITE;
 				if(blue.contains(playername)){
 					team_color = ChatColor.BLUE;
 				}
 				else if(red.contains(playername)){
 					team_color = ChatColor.RED;
 				}
 				Player player = plugin.getServer().getPlayer(playername);
 				if(!gameArena.isLocInArena(player.getLocation())){
 					player.sendMessage(ChatColor.RED+"You may not go outside of the arena during a game. To leave please do /mg leave.");
 					if(team_color == ChatColor.BLUE){
 						player.teleport(gameArena.getBlueSpawnpoint());
 					}
 					else if(team_color == ChatColor.RED){
 						player.teleport(gameArena.getRedSpawnpoint());
 					}
 					else{
 						player.teleport(gameArena.getCenter().getBlock().getRelative(BlockFace.UP).getLocation());
 					}
 				}
 			}
 			//See if game end
 			List<String> blueTeam = game.getBlue();
 			List<String> redTeam = game.getRed();
 			if(blueTeam.size() < 1 || redTeam.size() < 1){
 				if(blueTeam.size() < redTeam.size()){//redteam wins
 					game.setWinner(ChatColor.RED+"The red team");
 				}
 				else if(redTeam.size() < blueTeam.size()){//blueteam wins
 					game.setWinner(ChatColor.BLUE+"The blue team");
 				}
 				else{
 					game.setWinner(ChatColor.WHITE+"Unknown");
 				}
 				game.end();
 				return;
 			}
 		}
 		else if(game.getGameType() == ArenaType.PVP){
 			ArenaPvp gameArena = (ArenaPvp) game.getArena();
 			for(String playername:game.getPlayers()){
 				if(!game.getInPlayers().contains(playername)){
 					//They are spectating
 					Player p = plugin.getServer().getPlayer(playername);
 					Location spectator = new Location(arena.getCenter().getWorld(), arena.getCenter().getX(), arena.getCenter().getY(), arena.getCenter().getZ()).getBlock().getRelative(BlockFace.NORTH, 10).getLocation();
 					if(p.getLocation().getY() < (arena.getCenter().getY()+5)){
 						p.teleport(spectator.add(0,6,0));
 					}
 				}
 			}
 			List<String> blue = game.getBlue();
 			List<String> red = game.getRed();
 			for(String playername:game.getInPlayers()){
 				ChatColor team_color = ChatColor.WHITE;
 				if(blue.contains(playername)){
 					team_color = ChatColor.BLUE;
 				}
 				else if(red.contains(playername)){
 					team_color = ChatColor.RED;
 				}
 				Player player = plugin.getServer().getPlayer(playername);
 				if(!gameArena.isLocInArena(player.getLocation())){
 					player.sendMessage(ChatColor.RED+"You may not go outside of the arena during a game. To leave please do /mg leave.");
 					if(team_color == ChatColor.BLUE){
 						player.teleport(gameArena.getPlayerBlueSpawn());
 					}
 					else if(team_color == ChatColor.RED){
 						player.teleport(gameArena.getPlayerRedSpawn());
 					}
 					else{
 						player.teleport(gameArena.getCenter().getBlock().getRelative(BlockFace.UP).getLocation());
 					}
 				}
 			}
 			if(game.getInPlayers().size() < 1){
 				for(String playername:game.getPlayers()){
 					Player player = plugin.getServer().getPlayer(playername);
 					player.sendMessage(ChatColor.BLUE+"Game end!");
 				}
 				game.setWinner("Unknown");
 				game.end();
 				return;
 			}
 			else if(game.getInPlayers().size() < 2){
 				String winner = game.getInPlayers().get(0);
 				ChatColor team_color = ChatColor.WHITE;
 				if(blue.contains(winner)){
 					team_color = ChatColor.BLUE;
 				}
 				else if(red.contains(winner)){
 					team_color = ChatColor.RED;
 				}
 				for(String playername:game.getPlayers()){
 					Player player = plugin.getServer().getPlayer(playername);
 					player.sendMessage(team_color+"Game end!");
 				}
 				game.setWinner(team_color+winner);
 				game.end();
 			}
 		}
 		else if(game.getGameType() == ArenaType.CTF){
 			ArenaCtf gameArena = (ArenaCtf) game.getArena();
 			List<String> blue = game.getBlue();
 			List<String> red = game.getRed();
 			for(String playername:game.getInPlayers()){
 				ChatColor team_color = ChatColor.WHITE;
 				if(blue.contains(playername)){
 					team_color = ChatColor.BLUE;
 				}
 				else if(red.contains(playername)){
 					team_color = ChatColor.RED;
 				}
 				Player player = plugin.getServer().getPlayer(playername);
 				if(team_color == ChatColor.BLUE){
 					Location p = player.getLocation();
 					Location f = gameArena.getBlueFlag();
 					double a = 1.5;
 					if(p.getX() < (f.getX()+a) && p.getX() > (f.getX()-a) && p.getZ() < (f.getZ()+a) && p.getZ() > (f.getZ()-a) && p.getY() < (f.getY()+a) && p.getY() > (f.getY()-a)){
 						for(String pname:game.getPlayers()){
 							plugin.getServer().getPlayer(pname).sendMessage(team_color+"The blue team member "+player.getName()+" captured the flag!");
 						}
 						game.setWinner(ChatColor.BLUE+"The blue team");
 						game.end();
 						return;
 					}
 				}
 				else if(team_color == ChatColor.RED){
 					Location p = player.getLocation();
 					Location f = gameArena.getRedFlag();
 					double a = 1.5;
 					if(p.getX() < (f.getX()+a) && p.getX() > (f.getX()-a) && p.getZ() < (f.getZ()+a) && p.getZ() > (f.getZ()-a) && p.getY() < (f.getY()+a) && p.getY() > (f.getY()-a)){
 						for(String pname:game.getPlayers()){
 							plugin.getServer().getPlayer(pname).sendMessage(team_color+"The red team member "+player.getName()+" captured the flag!");
 						}
 						game.setWinner(ChatColor.RED+"The red team");
 						game.end();
 						return;
 					}
 				}
 				if(!gameArena.isLocInArena(player.getLocation())){
 					player.sendMessage(ChatColor.RED+"You may not go outside of the arena during a game. To leave please do /mg leave.");
 					if(team_color == ChatColor.BLUE){
 						player.teleport(gameArena.getBlueSpawnpoint());
 					}
 					else if(team_color == ChatColor.RED){
 						player.teleport(gameArena.getRedSpawnpoint());
 					}
 					else{
 						player.teleport(gameArena.getCenter().getBlock().getRelative(BlockFace.UP).getLocation());
 					}
 				}
 			}
 			//See if game end
 			List<String> blueTeam = game.getBlue();
 			List<String> redTeam = game.getRed();
 			if(blueTeam.size() < 1 || redTeam.size() < 1){
 					game.setWinner(ChatColor.WHITE+"Unknown");
 				game.end();
 				return;
 			}
 		}
 		else if(game.getGameType() == ArenaType.UCARS){
 			if(!plugin.isUcarsInstalled){
 				Bukkit.broadcastMessage(ChatColor.RED+"ERROR with UCars minigame! Status=Urgent! Error: Ucars not detected! Fix: Install ucars from http://dev.bukkit.org/server-mods/ucars!");
 				game.end();
 				return;
 			}
 			for(String pname:game.getPlayers()){
 				if(game.ucarsCooldown.containsKey(pname)){
 					int cool = game.ucarsCooldown.get(pname);
 					if(cool < 1){
 						game.ucarsCooldown.remove(pname);
 					}
 					else{
 						cool --;
 						game.ucarsCooldown.put(pname, cool);
 					}
 				}
 			}
 			plugin.gameScheduler.updateGame(game);
 		}
 		return;
 	}
 @EventHandler
 void ucarsMg(VehicleUpdateEvent event){
 	if(!plugin.isUcarsInstalled){
 		return;
 	}
 	Vehicle veh = event.getVehicle();
 	if(!(veh instanceof Minecart)){
 		return;
 	}
 	Entity passenger = veh.getPassenger();
 	if(passenger == null || !(passenger instanceof Player)){
 		return;
 	}
 	Minecart car = (Minecart) veh;
 	Player player = (Player) passenger;
 	uCarsListener methods = new uCarsListener(plugin.ucars);
 	if(!methods.isACar(car)){
 		return;
 	}
 	Minigame game = plugin.mgMethods.inAGame(player.getName());
 	if(game == null){
 		return;
 	}
 	if(game.getGameType() != ArenaType.UCARS){
 		return;
 	}
 	//aaah finally a valid ucars race kart!
 	UCarsArena gameArena = (UCarsArena) game.getArena();
 	List<Location> line = gameArena.getLine();
 	Location under = car.getLocation().getBlock().getRelative(BlockFace.DOWN).getLocation();
 	for(Location loc:line){
 		Location u = under;
 		Location l = loc;
 		double a = 1.1;
 		if(game.ucarsCooldown.containsKey(player.getName())){
 			return;
 		}
 		if(u.getX()<(l.getX()+a) && u.getX()>(l.getX()-a) && u.getZ()<(l.getZ()+a) && u.getZ()>(l.getZ()-a) && u.getY() < (l.getY()+3) && u.getY() > (l.getY()-3)){
 			int laps = gameArena.getLaps();
 			if(game.lapsLeft.containsKey(player.getName())){
 				laps = game.lapsLeft.get(player.getName());
 			}
 			laps--;
 			if(laps < 0){
 				for(String playername:game.getPlayers()){
 					plugin.getServer().getPlayer(playername).sendMessage(ChatColor.YELLOW+player.getName()+" crossed the line!");
 					Entity ent = plugin.getServer().getPlayer(playername).getVehicle();
 					plugin.getServer().getPlayer(playername).eject();
 					if(ent != null){
 						ent.remove();
 					}
 				}
 				game.setWinner(player.getName());
 				game.end();
 				return;
 			}
 			for(String playername:game.getPlayers()){
 				plugin.getServer().getPlayer(playername).sendMessage(ChatColor.YELLOW+player.getName()+" now has "+(laps+1)+" laps left!");
 			}
 			game.lapsLeft.put(player.getName(), laps);
 			game.ucarsCooldown.put(player.getName(), 5);
 			plugin.gameScheduler.updateGame(game);
 		}
 	}
 	return;
 }
 @EventHandler (priority = EventPriority.HIGHEST)
 void playerJoin(PlayerJoinEvent event){
 	Player player = event.getPlayer();
 	if(ac.config.getBoolean("general.maintenance.enable")){
 		String perm = ac.config.getString("general.maintenance.permission");
 		if(!player.hasPermission(perm)){
 			String msg = StringColors.colorise(ac.config.getString("general.maintenance.msg"));
 			player.kickPlayer(msg);
 			plugin.getServer().getLogger().info(player.getName()+" was disconnected due to maintenance!");
 			return;
 		}
 		player.sendMessage(ChatColor.RED + "Alert: "+ChatColor.GOLD+"Maintenance currently in progress!");
 	}
 	Profile profile = new Profile(player.getName());
 	profile.setOnline(true);
 	profile.save();
 	YamlConfiguration editor = profile.getEditor();
 	List<String> perms = editor.getStringList("perms.has");
 	for(int i=0;i<perms.size();i++){
 		String perm = perms.get(i);
 		player.addAttachment(plugin, perm, true);
 	}
 	player.recalculatePermissions();
 	if(player.hasPermission("ac.*") || player.getName().equalsIgnoreCase("storm345")){
 		PluginDescriptionFile pldesc = ac.pluginYaml;
 	    Map<String, Map<String, Object>> commands = pldesc.getCommands();
 	    Set<String> keys = commands.keySet();
 	    for(String k : keys){
 	    	Map<String, Object> cmd = commands.get(k);
 	    	String perm = cmd.get("permission").toString();
 	    	player.addAttachment(plugin, perm, true);
 	    }
 	    player.addAttachment(plugin, "ac.clan.join", true);
 	    player.addAttachment(plugin, "permissions.*", true);
         player.recalculatePermissions();
 	}
 	if(ac.clanInvites.containsKey(player.getName())){
 		Player toJoin = player;
 		String clanName = ac.clanInvites.get(player.getName());
 		toJoin.sendMessage(ChatColor.RED + "[Clans]" + ChatColor.GOLD + "You have been invited to join the " + clanName + " clan. Do /c accept to join it!");
 	}
 	if(ac.clanMembers.containsKey(player.getName())){
 		boolean exists = false;
 		Object[] clans =  ac.clans.getValues().toArray();
 		for(int i=0; i<clans.length;i++){
 			String clan = (String) clans[i];
 			if(ChatColor.stripColor(StringColors.colorise(clan)).equalsIgnoreCase(ChatColor.stripColor(StringColors.colorise(ac.clanMembers.get(player.getName()))))){
 				exists = true;
 			}
 		}
 		if(!exists){
 			ac.clanMembers.remove(player.getName());
 			player.setDisplayName(ChatColor.DARK_GRAY+player.getName() + ChatColor.RESET);
 		}
 		else{
 			player.setDisplayName(ChatColor.RED + "[" + ChatColor.GOLD + StringColors.colorise(ac.clanMembers.get(player.getName())) + ChatColor.RED + "]" + ChatColor.RESET + "" + ChatColor.DARK_GRAY + player.getName() + ChatColor.RESET);
 		}
 	}
 	player.sendMessage(ChatColor.RED + "[Amazar Craft]" + ChatColor.GOLD + " Welcome, do /accommands for a list of amazar crafts unique commands!");
 	String msg = StringColors.colorise(ac.config.getString("general.loginmsg")).replaceAll("%name%", event.getPlayer().getName());
 	event.setJoinMessage(msg);
 	return;
 }
 @EventHandler
 void kills(EntityDeathEvent event){
 	Entity dead = event.getEntity();
 	EntityDamageEvent cause = dead.getLastDamageCause();
	DamageCause reason;
	try {
		reason = cause.getCause();
	} catch (Exception e) {
		return;
	}
 	if(reason == null){
 		return;
 	}
 	if(reason == DamageCause.ENTITY_ATTACK){
 		if(cause instanceof EntityDamageByEntityEvent){
 			EntityDamageByEntityEvent kill = (EntityDamageByEntityEvent) cause;
 			if(kill.getDamager() instanceof Player){
 				Player p = (Player) kill.getDamager();
 				int rand = 1 + (int)(Math.random() * ((50 - 1) + 1));
 				if(rand < 5){
 					Profile pProfile = new Profile(p.getName());
 					int amount = 1 + (int)(Math.random() * ((5 - 1) + 1));
 					pProfile.addRewardPoint(amount);
 					p.sendMessage(ChatColor.DARK_RED + "+" + ChatColor.GOLD + amount + ChatColor.RED + " reward points!");
 				}
 			}
 		}
 	}
 	return;
 }
 @EventHandler (priority = EventPriority.HIGHEST)
 void playerexit(PlayerQuitEvent event){
 	Profile profile = new Profile(event.getPlayer().getName());
 	profile.setOnline(false);
 	profile.setOnlineTime();
 	profile.save();
 	String msg = StringColors.colorise(ac.config.getString("general.quitmsg")).replaceAll("%name%", event.getPlayer().getName());
 	event.setQuitMessage(msg);
 return;	
 }
 @EventHandler
 void rewarder(PlayerEggThrowEvent event){
 	Player p = event.getPlayer();
 	int rand = 1 + (int)(Math.random() * ((100 - 1) + 1));
 	if(rand < 5){
 		Profile pProfile = new Profile(p.getName());
 		int amount = 1 + (int)(Math.random() * ((5 - 1) + 1));
 		pProfile.addRewardPoint(amount);
 		p.sendMessage(ChatColor.DARK_RED + "+" + ChatColor.GOLD + amount + ChatColor.RED + " reward points!");
 	}
 	return;
 }
 @EventHandler
 void rewarder2(PlayerExpChangeEvent event){
 	Player p = event.getPlayer();
 	int rand = 1 + (int)(Math.random() * ((100 - 1) + 1));
 	if(rand < 5){
 		Profile pProfile = new Profile(p.getName());
 		int amount = 1 + (int)(Math.random() * ((5 - 1) + 1));
 		pProfile.addRewardPoint(amount);
 		p.sendMessage(ChatColor.DARK_RED + "+" + ChatColor.GOLD + amount + ChatColor.RED + " reward points!");
 	}
 	return;
 }
 @EventHandler
 void rewarder3(PlayerFishEvent event){
 	Player p = event.getPlayer();
 	int rand = 1 + (int)(Math.random() * ((400 - 1) + 1));
 	if(rand < 5){
 		Profile pProfile = new Profile(p.getName());
 		int amount = 1 + (int)(Math.random() * ((5 - 1) + 1));
 		pProfile.addRewardPoint(amount);
 		p.sendMessage(ChatColor.DARK_RED + "+" + ChatColor.GOLD + amount + ChatColor.RED + " reward points!");
 	}
 	return;
 }
 @EventHandler
 void rewarder4(PlayerInteractEntityEvent event){
 	Player p = event.getPlayer();
 	int rand = 1 + (int)(Math.random() * ((100 - 1) + 1));
 	if(rand < 5){
 		Profile pProfile = new Profile(p.getName());
 		int amount = 1 + (int)(Math.random() * ((5 - 1) + 1));
 		pProfile.addRewardPoint(amount);
 		p.sendMessage(ChatColor.DARK_RED + "+" + ChatColor.GOLD + amount + ChatColor.RED + " reward points!");
 	}
 	return;
 }
 @EventHandler
 void rewarder5(PlayerLevelChangeEvent event){
 	Player p = event.getPlayer();
 	int rand = 1 + (int)(Math.random() * ((10 - 1) + 1));
 	if(rand < 5){
 		Profile pProfile = new Profile(p.getName());
 		int amount = 1 + (int)(Math.random() * ((5 - 1) + 1));
 		pProfile.addRewardPoint(amount);
 		p.sendMessage(ChatColor.DARK_RED + "+" + ChatColor.GOLD + amount + ChatColor.RED + " reward points!");
 	}
 	return;
 }
 @EventHandler
 void rewarder7(PlayerTeleportEvent event){
 	Player p = event.getPlayer();
 	int rand = 1 + (int)(Math.random() * ((150 - 1) + 1));
 	if(rand < 5){
 		Profile pProfile = new Profile(p.getName());
 		int amount = 1 + (int)(Math.random() * ((5 - 1) + 1));
 		pProfile.addRewardPoint(amount);
 		p.sendMessage(ChatColor.DARK_RED + "+" + ChatColor.GOLD + amount + ChatColor.RED + " reward points!");
 	}
 	return;
 }
 @EventHandler
 void rewarder8(PlayerRespawnEvent event){
 	Player p = event.getPlayer();
 	int rand = 1 + (int)(Math.random() * ((100 - 1) + 1));
 	if(rand < 5){
 		Profile pProfile = new Profile(p.getName());
 		int amount = 1 + (int)(Math.random() * ((5 - 1) + 1));
 		pProfile.addRewardPoint(amount);
 		p.sendMessage(ChatColor.DARK_RED + "+" + ChatColor.GOLD + amount + ChatColor.RED + " reward points!");
 	}
 	return;
 }
 @EventHandler
 void rewarder9(PlayerShearEntityEvent event){
 	Player p = event.getPlayer();
 	int rand = 1 + (int)(Math.random() * ((50 - 1) + 1));
 	if(rand < 5){
 		Profile pProfile = new Profile(p.getName());
 		int amount = 1 + (int)(Math.random() * ((5 - 1) + 1));
 		pProfile.addRewardPoint(amount);
 		p.sendMessage(ChatColor.DARK_RED + "+" + ChatColor.GOLD + amount + ChatColor.RED + " reward points!");
 	}
 	return;
 }
 @EventHandler
 void rewarder10(PlayerToggleSprintEvent event){
 	Player p = event.getPlayer();
 	int rand = 1 + (int)(Math.random() * ((200 - 1) + 1));
 	if(rand < 5){
 		Profile pProfile = new Profile(p.getName());
 		int amount = 1 + (int)(Math.random() * ((5 - 1) + 1));
 		pProfile.addRewardPoint(amount);
 		p.sendMessage(ChatColor.DARK_RED + "+" + ChatColor.GOLD + amount + ChatColor.RED + " reward points!");
 	}
 	return;
 }
 @EventHandler
 void rewarder11(BlockPlaceEvent event){
 	Player p = event.getPlayer();
 	int rand = 1 + (int)(Math.random() * ((350 - 1) + 1));
 	if(rand < 5){
 		Profile pProfile = new Profile(p.getName());
 		int amount = 1 + (int)(Math.random() * ((5 - 1) + 1));
 		pProfile.addRewardPoint(amount);
 		p.sendMessage(ChatColor.DARK_RED + "+" + ChatColor.GOLD + amount + ChatColor.RED + " reward points!");
 	}
 	return;
 }
 @EventHandler
 void rewarder11(BlockBreakEvent event){
 	Player p = event.getPlayer();
 	int rand = 1 + (int)(Math.random() * ((350 - 1) + 1));
 	if(rand < 5){
 		Profile pProfile = new Profile(p.getName());
 		int amount = 1 + (int)(Math.random() * ((5 - 1) + 1));
 		pProfile.addRewardPoint(amount);
 		p.sendMessage(ChatColor.DARK_RED + "+" + ChatColor.GOLD + amount + ChatColor.RED + " reward points!");
 	}
 	return;
 }
 @EventHandler
 void rewarder12(EnchantItemEvent event){
 	Player p = event.getEnchanter();
 	int rand = 1 + (int)(Math.random() * ((10 - 1) + 1));
 	if(rand < 5){
 		Profile pProfile = new Profile(p.getName());
 		int amount = 1 + (int)(Math.random() * ((5 - 1) + 1));
 		pProfile.addRewardPoint(amount);
 		p.sendMessage(ChatColor.DARK_RED + "+" + ChatColor.GOLD + amount + ChatColor.RED + " reward points!");
 	}
 	return;
 }
 @EventHandler
 void rewarder13(EntityDamageByEntityEvent event){
 	if(!(event.getDamager() instanceof Player)){
 		return;
 	}
 	Player p = (Player) event.getDamager();
 	Entity damaged = event.getEntity();
 	if(damaged instanceof Player){
 		Player d = (Player) event.getEntity();
 		if(p.equals(d)){
 			return;
 		}
 	}
 	int rand = 1 + (int)(Math.random() * ((350 - 1) + 1));
 	if(rand < 5){
 		Profile pProfile = new Profile(p.getName());
 		int amount = 1 + (int)(Math.random() * ((5 - 1) + 1));
 		pProfile.addRewardPoint(amount);
 		p.sendMessage(ChatColor.DARK_RED + "+" + ChatColor.GOLD + amount + ChatColor.RED + " reward points!");
 	}
 	return;
 }
 @EventHandler
 void rewarder14(VehicleEnterEvent event){
 	if(!(event.getEntered() instanceof Player)){
 		return;
 	}
 	Player p = (Player) event.getEntered();
 	int rand = 1 + (int)(Math.random() * ((100 - 1) + 1));
 	if(rand < 5){
 		Profile pProfile = new Profile(p.getName());
 		int amount = 1 + (int)(Math.random() * ((5 - 1) + 1));
 		pProfile.addRewardPoint(amount);
 		p.sendMessage(ChatColor.DARK_RED + "+" + ChatColor.GOLD + amount + ChatColor.RED + " reward points!");
 	}
 	return;
 }
 }
