 package com.ftwinston.KillerMinecraft;
 
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.OfflinePlayer;
 import org.bukkit.World;
 import org.bukkit.block.Block;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.Player;
 import org.bukkit.event.Event;
 import org.bukkit.event.EventException;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.Action;
 import org.bukkit.event.block.BlockBreakEvent;
 import org.bukkit.event.block.BlockFromToEvent;
 import org.bukkit.event.block.BlockPlaceEvent;
 import org.bukkit.event.entity.EntityDamageByEntityEvent;
 import org.bukkit.event.entity.EntityDamageEvent;
 import org.bukkit.event.entity.EntityDeathEvent;
 import org.bukkit.event.entity.EntityExplodeEvent;
 import org.bukkit.event.entity.EntityPortalEvent;
 import org.bukkit.event.entity.EntityTargetEvent;
 import org.bukkit.event.entity.ItemSpawnEvent;
 import org.bukkit.event.entity.PlayerDeathEvent;
 import org.bukkit.event.hanging.HangingBreakByEntityEvent;
 import org.bukkit.event.hanging.HangingBreakEvent;
 import org.bukkit.event.hanging.HangingPlaceEvent;
 import org.bukkit.event.inventory.InventoryClickEvent;
 import org.bukkit.event.inventory.PrepareItemCraftEvent;
 import org.bukkit.event.player.AsyncPlayerChatEvent;
 import org.bukkit.event.player.PlayerBucketEmptyEvent;
 import org.bukkit.event.player.PlayerChangedWorldEvent;
 import org.bukkit.event.player.PlayerDropItemEvent;
 import org.bukkit.event.player.PlayerInteractEntityEvent;
 import org.bukkit.event.player.PlayerInteractEvent;
 import org.bukkit.event.player.PlayerItemHeldEvent;
 import org.bukkit.event.player.PlayerJoinEvent;
 import org.bukkit.event.player.PlayerPickupItemEvent;
 import org.bukkit.event.player.PlayerPortalEvent;
 import org.bukkit.event.player.PlayerQuitEvent;
 import org.bukkit.event.player.PlayerRespawnEvent;
 import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.plugin.RegisteredListener;
 
 import com.ftwinston.KillerMinecraft.Game.GameState;
 import com.ftwinston.KillerMinecraft.PlayerManager.Info;
 
 class EventManager implements Listener
 {
 	KillerMinecraft plugin;
 	
 	public EventManager(KillerMinecraft instance)
 	{
 		plugin = instance;
 	}
 
 	public void registerEvents(KillerModule module)
 	{
 		module.eventHandlers = plugin.getPluginLoader().createRegisteredListeners(module, plugin);
 	}
 
 	public void unregisterEvents(KillerModule module)
 	{
 		module.eventHandlers.clear();
 	}
 
 	private void fireGameEvents(Event event, Game game) throws EventException
 	{
 		fireModuleEvents(event, game.getGameMode());
 		//fireModuleEvents(event, game.getWorldOption());
 	}
 	
 	private void fireModuleEvents(Event event, KillerModule module) throws EventException
 	{	
 		Set<RegisteredListener> listeners = module.eventHandlers.get(event.getClass());
 		if ( listeners != null )
 			for (RegisteredListener listener : listeners)
 				listener.callEvent(event);
 	}
 
 	// when you die a spectator, be made able to fly again when you respawn
 	@EventHandler(priority = EventPriority.HIGHEST) // run last, so we can absolutely say where you should respawn, in a Killer game
 	public void onEvent(PlayerRespawnEvent event) throws EventException
 	{
 		World world = event.getPlayer().getWorld();
 		if ( world == plugin.stagingWorld )
 		{
 			event.setRespawnLocation(plugin.worldManager.getStagingAreaSpawnPoint());
 			return;
 		}
 		
 		final Game game = plugin.getGameForWorld(world);
 		if ( game == null )
 			return;
 		
 		final String playerName = event.getPlayer().getName();
 		
 		if ( game.getGameState().usesGameWorlds && game.getWorlds().size() > 0 )
 			event.setRespawnLocation(game.getGameMode().getSpawnLocation(event.getPlayer()));
 		else
 			event.setRespawnLocation(plugin.worldManager.getStagingAreaSpawnPoint());
 	
 		if( !Helper.isAlive(game, event.getPlayer()) )
 		{
 			plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
 				public void run()
 				{
 					Player player = plugin.getServer().getPlayerExact(playerName);
 					if ( player != null )
 					{
 						boolean alive = game.getGameMode().isAllowedToRespawn(player);
 						PlayerManager.instance.setAlive(game, player, alive);
 						if ( alive )
 							player.setCompassTarget(PlayerManager.instance.getCompassTarget(game, player));
 					}
 				}
 			});
 		}
 		
 		fireGameEvents(event, game);
 	}
 	
 	@EventHandler(priority = EventPriority.HIGHEST)
 	public void onEvent(PlayerChangedWorldEvent event) throws EventException
 	{
 		World toWorld = event.getPlayer().getWorld();
 		Game fromGame = plugin.getGameForWorld(event.getFrom());
 		Game toGame = plugin.getGameForWorld(toWorld);
 		
 		boolean wasInGame = fromGame != null;
 		boolean nowInGame = toGame != null;
 		Player player = event.getPlayer();
 		
 		if ( wasInGame )
 		{
 			if ( nowInGame )
 			{
 				
 				if ( fromGame != toGame )
 				{
 					PlayerManager.instance.playerKilled(fromGame, player);
 					PlayerManager.instance.clearInventory(player);
 					toGame.addPlayerToGame(player);
 					
 					if ( Settings.filterScoreboard )
 					{// hide from old game, show for new
 						String name = fromGame.calculateColoredName(player);
 						for ( Player other : fromGame.getOnlinePlayers(new PlayerFilter().exclude(player)) )
 							plugin.craftBukkit.sendForScoreboard(other, name, false);
 						
 						name = toGame.calculateColoredName(player);
 						for ( Player other : toGame.getOnlinePlayers(new PlayerFilter().exclude(player)) )
 							plugin.craftBukkit.sendForScoreboard(other, name, true);
 					}
 				}
 				else
 				{
 					Info info = toGame.getPlayerInfo().get(player.getName());
 					if( info != null && info.isAlive() )
 						player.setCompassTarget(PlayerManager.instance.getCompassTarget(fromGame, player));
 					else
 						PlayerManager.instance.setAlive(toGame, player, false);
 				}
 			}
 			else
 			{
 				PlayerManager.instance.restoreInventory(event.getPlayer());
 				playerQuit(fromGame, event.getPlayer(), false);
 				
 				if ( toWorld != plugin.stagingWorld )
 					PlayerManager.instance.previousLocations.remove(event.getPlayer().getName()); // they left Killer, so forget where they should be put on leaving
 
 				if ( Settings.filterScoreboard )
 				{
 					String name = fromGame.calculateColoredName(player);
 					for ( Player other : fromGame.getOnlinePlayers(new PlayerFilter().exclude(player)) )
 						plugin.craftBukkit.sendForScoreboard(other, name, false);
 					
 					// if game uses colored names, remove "colored" names for players in this game
 					if ( !fromGame.getGameMode().teamAllocationIsSecret() )
 						for ( Player other : fromGame.getOnlinePlayers() )
 							plugin.craftBukkit.sendForScoreboard(player, fromGame.calculateColoredName(other), false);
 					
 					// now add everyone that wasn't in this game to this player's scoreboard...
 					for ( Player other : plugin.getServer().getOnlinePlayers() )
 						plugin.craftBukkit.sendForScoreboard(player, other, true);
 				}
 			}
 		}
 		else if ( nowInGame )
 		{
 			toGame.addPlayerToGame(event.getPlayer());
 			
 			if ( Settings.filterScoreboard )
 			{
 				// if game uses colored names, hide everyone then send the colored names for players in this game
 				if ( !toGame.getGameMode().teamAllocationIsSecret() )
 				{
 					for ( Player other : plugin.getServer().getOnlinePlayers() )
 						plugin.craftBukkit.sendForScoreboard(player, other, false);
 					
 					for ( Player other : toGame.getOnlinePlayers() )
 						plugin.craftBukkit.sendForScoreboard(player, toGame.calculateColoredName(other), true);
 				}
 				else // otherwise, just hide everyone that isn't in this game
 					for ( Player other : plugin.getServer().getOnlinePlayers() )
 						if ( other != player && plugin.getGameForWorld(other.getWorld()) != toGame )
 							plugin.craftBukkit.sendForScoreboard(player, other, false);
 				
 				// then send me to everyone in this game
 				String name = toGame.calculateColoredName(player);
 				for ( Player other : toGame.getOnlinePlayers() )
 					plugin.craftBukkit.sendForScoreboard(other, name, true);
 			}
 		}
 		
 		if ( toWorld == plugin.stagingWorld )
 			PlayerManager.instance.putPlayerInStagingWorld(event.getPlayer());
 		
 		if ( event.getFrom() == plugin.stagingWorld )
 		{
 			GameConfiguration.checkEvent(event);
 			
 			if ( !nowInGame )
 			{
 			// if you leave the staging world, you leave any game you were in ... unless you are entering a game world
 			Game game = plugin.getGameForPlayer(event.getPlayer());
 			if ( game != null )
 				game.removePlayerFromGame(event.getPlayer());
 			}
 		}
 		
 		if ( wasInGame && toGame == fromGame )
 			fireGameEvents(event, toGame);
 	}
 	
 	@EventHandler(priority = EventPriority.HIGHEST)
 	public void onEvent(PlayerPortalEvent event) throws EventException
 	{
 		Game game = plugin.getGameForWorld(event.getFrom().getWorld());
 		if ( game == null )
 			return;
 		
 		PortalHelper helper = new PortalHelper(event.getPortalTravelAgent());
 		event.setCancelled(true); // we're going to handle implementing the portalling ourselves
 		
 		game.getGameMode().handlePortal(event.getCause(), event.getPortalTravelAgent().findPortal(event.getFrom()), helper); // see? I told you
 		helper.performTeleport(event.getCause(), event.getPlayer());
 		fireGameEvents(event, game);
 	}
 	
 	@EventHandler(priority = EventPriority.HIGHEST)
 	public void onEvent(EntityPortalEvent event) throws EventException
 	{
 		Game game = plugin.getGameForWorld(event.getFrom().getWorld());
 		if ( game == null )
 			return;
 		
 		PortalHelper helper = new PortalHelper(event.getPortalTravelAgent());
 		event.setCancelled(true); // we're going to handle implementing the portalling ourselves
 		
 		game.getGameMode().handlePortal(TeleportCause.NETHER_PORTAL, event.getPortalTravelAgent().findPortal(event.getFrom()), helper); // see? I told you
 		helper.performTeleport(TeleportCause.NETHER_PORTAL, event.getEntity());
 		fireGameEvents(event, game);
 	}
 		
 	// prevent spectators picking up anything
 	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
 	public void onEvent(PlayerPickupItemEvent event) throws EventException
 	{
 		Game game = plugin.getGameForWorld(event.getPlayer().getWorld());
 		if ( game == null )
 			return;
 		
 		if( !Helper.isAlive(game, event.getPlayer()) )
 			event.setCancelled(true);
 		
 		fireGameEvents(event, game);
 	}
 	
 	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
 	public void onEvent(ItemSpawnEvent event) throws EventException
 	{
 		Game game = plugin.getGameForWorld(event.getLocation().getWorld());
 		
 		if ( game == null )
 			return;
 
 		event.setCancelled(plugin.worldManager.isProtectedLocation(game, event.getLocation(), null));
 		fireGameEvents(event, game);
 	}
 	
 	// prevent spectators breaking anything, prevent anyone breaking protected locations
 	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
 	public void onEvent(BlockBreakEvent event) throws EventException
 	{
 		World world = event.getBlock().getWorld();
 		Game game = plugin.getGameForWorld(world);
 		
 		if ( game != null || world == plugin.stagingWorld ) 
 			event.setCancelled(
 				!Helper.isAlive(game, event.getPlayer()) ||
 				plugin.worldManager.isProtectedLocation(game, event.getBlock().getLocation(), event.getPlayer())
 			);
 		
 		if ( game != null )
 			fireGameEvents(event, game);
 	}
 	
 	// prevent spectators breaking frames, prevent anyone breaking protected frames
 	// more complicated than above, because we want to protect against breakings that aren't by entities (I guess)
 	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
 	public void onEvent(HangingBreakEvent event) throws EventException
 	{
 		Location loc = event.getEntity().getLocation();
 		Game game = plugin.getGameForWorld(loc.getWorld());
 		
 		if ( game == null && loc.getWorld() != plugin.stagingWorld )
 			return;
 		
 		Player player = null;
 		if ( event instanceof HangingBreakByEntityEvent)
 		{
 			Entity entity = ((HangingBreakByEntityEvent)event).getRemover();
 			if ( entity instanceof Player )
 			{
 				player = (Player)entity;
 				if ( !Helper.isAlive(game, player) )
 				{
 					event.setCancelled(true);
 					return;
 				}
 			}
 		}
 		
 		event.setCancelled(plugin.worldManager.isProtectedLocation(game, loc, player));
 		if ( game != null )
 			fireGameEvents(event, game);
 	}
 	
 	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
 	public void onEvent(HangingPlaceEvent event) throws EventException
 	{
 		Location loc = event.getEntity().getLocation();
 		Game game = plugin.getGameForWorld(loc.getWorld());
 		
 		if ( game != null || loc.getWorld() == plugin.stagingWorld ) 
 			event.setCancelled(
 				!Helper.isAlive(game, event.getPlayer()) ||
 				plugin.worldManager.isProtectedLocation(game, loc, event.getPlayer())
 			);
 		
 		if ( game != null )
 			fireGameEvents(event, game);
 	}
 	
 	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
 	public void onEvent(PlayerInteractEntityEvent event) throws EventException
 	{	
 		Location loc = event.getRightClicked().getLocation();
 		Game game = plugin.getGameForWorld(loc.getWorld());
 		
 		if ( game != null || loc.getWorld() == plugin.stagingWorld ) 
 			event.setCancelled(
 				!Helper.isAlive(game, event.getPlayer()) ||
 				plugin.worldManager.isProtectedLocation(game, loc, event.getPlayer())
 			);
 		
 		if ( game != null )
 			fireGameEvents(event, game);
 	}
 	
 	// prevent anyone placing blocks on protected locations
 	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
 	public void onEvent(BlockPlaceEvent event) throws EventException
 	{
 		World world = event.getBlock().getWorld();
 		Game game = plugin.getGameForWorld(world);
 		
 		if ( game != null || world == plugin.stagingWorld ) 
 			event.setCancelled(
 				!Helper.isAlive(game, event.getPlayer()) ||
 				plugin.worldManager.isProtectedLocation(game, event.getBlock().getLocation(), event.getPlayer())
 			);
 		
 		if ( game != null )
 			fireGameEvents(event, game);
 	}
 	
 	// prevent lava/water from flowing onto protected locations
 	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
 	public void onEvent(BlockFromToEvent event) throws EventException
 	{
 		event.setCancelled(plugin.worldManager.isProtectedLocation(event.getBlock().getLocation(), null));
 		
 		Game game = plugin.getGameForWorld(event.getBlock().getWorld());		
 		if ( game != null )
 			fireGameEvents(event, game);
 	}
 	
 	// prevent explosions from damaging protected locations
 	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
 	public void onEvent(EntityExplodeEvent event) throws EventException
 	{
 		World world = event.getEntity().getWorld();
 		Game game = plugin.getGameForWorld(world);
 		if ( game == null && world != plugin.stagingWorld ) 
 			return;
 		
 		List<Block> blocks = event.blockList();
 		for ( int i=0; i<blocks.size(); i++ )
 		if ( plugin.worldManager.isProtectedLocation(game, blocks.get(i).getLocation(), null) )
 			{
 				blocks.remove(i);
 				i--;
 			}
 		
 		if ( game != null )
 			fireGameEvents(event, game);
 	}
 	
 	// switching between spectator items
 	@EventHandler(priority = EventPriority.NORMAL)
 	public void onEvent(PlayerItemHeldEvent event) throws EventException
 	{
 		World world = event.getPlayer().getWorld();
 		Game game = plugin.getGameForWorld(world);
 		if ( game == null ) 
 			return;
 		
 		if ( !Helper.isAlive(game, event.getPlayer()) )
 		{
 			ItemStack item = event.getPlayer().getInventory().getItem(event.getNewSlot());
 			
 			if ( item == null )
 				Helper.setTargetOf(game, event.getPlayer(), (String)null);
 			else if ( item.getType() == Settings.teleportModeItem )
 			{
 				event.getPlayer().sendMessage("Free look mode: left click to teleport " + ChatColor.YELLOW + "to" + ChatColor.RESET + " where you're looking, right click to teleport " + ChatColor.YELLOW + "through" + ChatColor.RESET + " through what you're looking");
 				Helper.setTargetOf(game, event.getPlayer(), (String)null);
 			}
 			else if ( item.getType() == Settings.followModeItem )
 			{
 				event.getPlayer().sendMessage("Follow mode: click to cycle target");
 				Player target = PlayerManager.instance.getNearestFollowTarget(game, event.getPlayer());
 				Helper.setTargetOf(game, event.getPlayer(), target);
 				if ( target != null )
 					PlayerManager.instance.checkFollowTarget(game, event.getPlayer(), target.getName());
 			}
 			else
 				Helper.setTargetOf(game, event.getPlayer(), (String)null);
 		}
 		
 		fireGameEvents(event, game);
 	}
 	
 	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
 	public void onEvent(PlayerInteractEvent event) throws EventException
 	{
 		Block b = event.getClickedBlock();
 		
 		if ( b.getWorld() == plugin.stagingWorld )
 		{
 			if ( event.getAction() == Action.RIGHT_CLICK_BLOCK && (b.getType() == Material.STONE_BUTTON || b.getType() == Material.WOOD_BUTTON) )
 				for ( Game game : plugin.games )
 					if ( game.checkButtonPressed(b.getLocation(), event.getPlayer()) )
 						break;
 			return;
 		}
 
 		Game game = plugin.getGameForPlayer(event.getPlayer());		
 		if ( game == null ) 
 			return;
 
 		// spectators can't interact with anything, but they do use clicking to handle their spectator stuff
 		if ( !Helper.isAlive(game, event.getPlayer()) )
 		{
 			event.setCancelled(true);
 			Material held = event.getPlayer().getItemInHand().getType();
 			
 			if ( held == Settings.teleportModeItem )
 			{
 				if ( event.getAction() == Action.LEFT_CLICK_AIR || event.getAction() == Action.LEFT_CLICK_BLOCK )
 					PlayerManager.instance.doSpectatorTeleport(event.getPlayer(), false);
 				else if ( event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK )
 					PlayerManager.instance.doSpectatorTeleport(event.getPlayer(), true);
 			}
 			else if ( held == Settings.followModeItem )
 			{
 				String targetName = Helper.getTargetName(game, event.getPlayer());
 				
 				if ( event.getAction() == Action.LEFT_CLICK_AIR || event.getAction() == Action.LEFT_CLICK_BLOCK )
 				{
 					String target = PlayerManager.instance.getNextFollowTarget(game, event.getPlayer(), targetName, true);
 					Helper.setTargetOf(game, event.getPlayer(), target);
 					PlayerManager.instance.checkFollowTarget(game, event.getPlayer(), target);
 					event.getPlayer().sendMessage("Following " + target);
 				}
 				else if ( event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK )
 				{
 					String target = PlayerManager.instance.getNextFollowTarget(game, event.getPlayer(), targetName, false);
 					Helper.setTargetOf(game, event.getPlayer(), target);
 					PlayerManager.instance.checkFollowTarget(game, event.getPlayer(), target);
 					event.getPlayer().sendMessage("Following " + target);
 				}
 			}
 		}
 		// prevent spectators from interfering with other players' block placement
 		else if ( !event.isCancelled() && event.getAction() == Action.RIGHT_CLICK_BLOCK && event.getClickedBlock() != null )
 		{
 			b = b.getRelative(event.getBlockFace());
 			double minX = b.getX() - 1, maxX = b.getX() + 2,
 				   minY = b.getY() - 2, maxY = b.getY() + 1,
 				   minZ = b.getZ() - 1, maxZ = b.getZ() + 2;
 			
 			List<Player> spectators = game.getGameMode().getOnlinePlayers(new PlayerFilter().notAlive().world(b.getWorld()).exclude(event.getPlayer()));
 			for ( Player spectator : spectators )
 			{
 				Location loc = spectator.getLocation();
 				if ( loc.getX() >= minX && loc.getX() <= maxX
 						&& loc.getY() >= minY && loc.getY() <= maxY
 						&& loc.getZ() >= minZ && loc.getZ() <= maxZ )
 					spectator.teleport(spectator.getLocation().add(0, 3, 0)); // just teleport them upwards, out of the way of this block place
 			}
 		}
 		
 		fireGameEvents(event, game);
 	}
 	
 	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
 	public void onEvent(PlayerDropItemEvent event) throws EventException
 	{
 		Game game = plugin.getGameForWorld(event.getPlayer().getWorld());
 		if ( game == null ) 
 			return;
 		
 		// spectators can't drop items
 		if ( !Helper.isAlive(game, event.getPlayer()) )
 			event.setCancelled(true);
 		
 		fireGameEvents(event, game);
 	}
 	
 	@EventHandler(priority = EventPriority.NORMAL)
 	public void onEvent(InventoryClickEvent event) throws EventException
 	{
 		Player player = (Player)event.getWhoClicked();
 		if ( player == null )
 			return;
 
 		World world = player.getWorld();
 		if ( world == plugin.stagingWorld )
 		{
 			// if the inventory is a game configuration inventory menu, tell the relevant game's game configuration
 			GameConfiguration.checkEvent(event);
 			return;
 		}
 		
 		Game game = plugin.getGameForWorld(world);
 		if ( game == null )
 			return;
 		
 		// spectators can't rearrange their inventory ... is that a bit mean?
 		if ( !Helper.isAlive(game, player) )
 			event.setCancelled(true);
 		
 		fireGameEvents(event, game);
 	}
 	
 	// spectators can't deal or receive damage
 	@EventHandler(priority = EventPriority.HIGH)
 	public void onEvent(EntityDamageEvent event) throws EventException
 	{
 		Game game = plugin.getGameForWorld(event.getEntity().getWorld());
 		if ( game == null ) 
 			return;
 		
 		event.setCancelled(false);
 		if ( event instanceof EntityDamageByEntityEvent )
 		{
 			Player attacker = Helper.getAttacker(event);
 			if ( attacker != null )
 			{
 				if ( !Helper.isAlive(game, attacker) )
 					event.setCancelled(true);
 			}
 		}
 		if ( event.isCancelled() || event.getEntity() == null || !(event.getEntity() instanceof Player))
 			return;
 		
 		Player victim = (Player)event.getEntity();
 		
 		if( !Helper.isAlive(game, victim) )
 			event.setCancelled(true);
 		
 		fireGameEvents(event, game);
 	}
 	
 	// can't empty buckets onto protected locations
 	@EventHandler(priority = EventPriority.HIGH)
 	public void onEvent(PlayerBucketEmptyEvent event) throws EventException
 	{
 		event.setCancelled(plugin.worldManager.isProtectedLocation(event.getBlockClicked().getRelative(event.getBlockFace()).getLocation(), null));
 		
 		Game game = plugin.getGameForWorld(event.getBlockClicked().getWorld());
 		if ( game != null )
 			fireGameEvents(event, game);
 	}
 	
 	@EventHandler(priority = EventPriority.HIGH)
 	public void onEvent(PrepareItemCraftEvent event) throws EventException
 	{
 		plugin.recipeManager.handleCraftEvent(event);
		
 		Game game = plugin.getGameForWorld(event.getViewers().get(0).getWorld());
 		if ( game != null )
 			fireGameEvents(event, game);
 	}
 	
 	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
 	public void onEvent(EntityTargetEvent event) throws EventException
 	{
 		World world = event.getEntity().getWorld();
 		Game game = plugin.getGameForWorld(world);
 		if ( game == null ) 
 			return;
 		
 		// monsters shouldn't target spectators
 		if( event.getTarget() != null && event.getTarget() instanceof Player && !Helper.isAlive(game, (Player)event.getTarget()) )
 			event.setCancelled(true);
 		
 		fireGameEvents(event, game);
 	}
 	
 	@EventHandler(priority = EventPriority.NORMAL)
 	public void onEvent(AsyncPlayerChatEvent event) throws EventException
 	{
 		// don't mess with chat if they're in a conversation
 		if ( event.getPlayer().isConversing() )
 			return;
 			
 		World world = event.getPlayer().getWorld();
 		Game game = plugin.getGameForWorld(world);
 		if ( game == null )
 		{
 			if ( Settings.filterChat )
 			{// this player isn't in a game world, so players that are in game worlds should not see this message
 				for (Player recipient : new HashSet<Player>(event.getRecipients()))
 					if ( recipient != null && recipient.isOnline() && plugin.getGameForWorld(recipient.getWorld()) != null )
 						event.getRecipients().remove(recipient);
 			}
 			
 			return;
 		}
 		
 		boolean isVote = false;
 		if ( plugin.voteManager.isInVote() )
 		{
 			if ( event.getMessage().equalsIgnoreCase("Y") && plugin.voteManager.doVote(event.getPlayer(), true) )
 			{
 				event.setMessage(ChatColor.GREEN + "Y");
 				isVote = true;
 			}
 			else if ( event.getMessage().equalsIgnoreCase("N") && plugin.voteManager.doVote(event.getPlayer(), false) )
 			{
 				event.setMessage(ChatColor.RED + "N");
 				isVote = true;
 			}
 		}
 		
 		if ( Settings.filterChat )
 		{// players that are not in this game's worlds should not see this message
 				for (Player recipient : new HashSet<Player>(event.getRecipients()))
 					if ( recipient != null && recipient.isOnline()
 						&& recipient.getWorld() != world
 						&& plugin.getGameForWorld(recipient.getWorld()) != game )
 						event.getRecipients().remove(recipient);
 		}
 		
 		if ( isVote )
 			return;
 		
 		if ( game.getGameState() == GameState.finished || Helper.isAlive(game, event.getPlayer()) )
 		{// colored player names shouldn't produce colored messages ... spectator chat isn't special when the game is in the "finished" state.
 			event.setMessage(ChatColor.RESET + event.getMessage());
 		}
 		else
 		{// mark spectator chat, and hide it from non-spectators
 			event.setMessage(ChatColor.YELLOW + "[Spec] " + ChatColor.RESET + event.getMessage());
 			
 			for (Player recipient : new HashSet<Player>(event.getRecipients()))
 				if ( recipient != null && recipient.isOnline() && !Helper.isAlive(game, recipient.getPlayer()))
 					event.getRecipients().remove(recipient);
 		}
 		
 		if ( game != null )
 			fireGameEvents(event, game);
 	}
 	
 	@EventHandler(priority = EventPriority.HIGHEST)
 	public void onEvent(PlayerJoinEvent event)
 	{
 		final World world = event.getPlayer().getWorld();
 		final Game game = plugin.getGameForWorld(world);
 		final String playerName = event.getPlayer().getName();
 		
 		plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
 			public void run()
 			{
 				Player player = plugin.getServer().getPlayerExact(playerName);
 				if ( player == null )
 					return;
 
 				if ( plugin.getGameForWorld(world) == null )
 					PlayerManager.instance.restoreInventory(player);
 				
 				if ( world == plugin.stagingWorld )
 					PlayerManager.instance.putPlayerInStagingWorld(player);
 				
 				if ( Settings.filterScoreboard )
 				{// hide this person from the scoreboard of any games that they aren't in
 					for ( Game otherGame : plugin.games )
 						if ( otherGame != game )
 							for ( Player other : otherGame.getOnlinePlayers(new PlayerFilter().exclude(player)) )
 								plugin.craftBukkit.sendForScoreboard(other, player, false);
 				}
 			}
 		});
 		
 		if ( game != null )
 			game.addPlayerToGame(event.getPlayer());
 	}
 	
 	@EventHandler(priority = EventPriority.LOWEST)
 	public void onEvent(PlayerQuitEvent event)
 	{
 		World world = event.getPlayer().getWorld();
 		if ( world == plugin.stagingWorld )
 		{
 			GameConfiguration.checkEvent(event);
 			
 			Game game = plugin.getGameForPlayer(event.getPlayer());
 			if ( game != null )
 				game.removePlayerFromGame(event.getPlayer());
 		}
 		else
 		{
 			Game game = plugin.getGameForWorld(world);
    		 	if ( game != null )
 				playerQuit(game, event.getPlayer(), true);
 		}
 	}
 	
 	private void playerQuit(Game game, Player player, boolean actuallyLeftServer)
 	{
 		game.removePlayerFromGame(player);
 		if ( actuallyLeftServer ) // the quit message should be sent to the scoreboard of anyone who this player was invisible to
 			for ( Player online : game.getOnlinePlayers() )
 				if ( !online.canSee(player) )
 					plugin.craftBukkit.sendForScoreboard(online, player, false);
 		
 		plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new DelayedDeathEffect(game, player.getName(), true), 600);
 	}
 	
 	@EventHandler(priority = EventPriority.HIGH)
 	public void onEvent(EntityDeathEvent event)
 	{
 		if ( !(event instanceof PlayerDeathEvent) )
 			return;
 	
 		Game game = plugin.getGameForWorld(event.getEntity().getWorld());
 	 	if ( game == null )
 	 	{
 	 		GameConfiguration.checkEvent((PlayerDeathEvent)event);
 	 		return;
 	 	}
 		
 		PlayerDeathEvent pEvent = (PlayerDeathEvent)event;
 		
 		Player player = pEvent.getEntity();
 		if ( player == null )
 			return;
 		
 		game.broadcastMessage(game.getGameMode().useDiscreetDeathMessages() ? ChatColor.RED + player.getName() + " died" : pEvent.getDeathMessage());
 		pEvent.setDeathMessage(""); // we only want the message to go to people in the game	
 		
 		// the only reason this is delayed is to avoid banning the player before they properly die, if we're banning players on death
 		plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new DelayedDeathEffect(game, player.getName(), false), 10);
 	}
 	
 	class DelayedDeathEffect implements Runnable
 	{
 		Game game;
 		String name;
 		boolean checkDisconnected;
 		public DelayedDeathEffect(Game game, String playerName, boolean disconnect)
 		{
 			this.game = game;
 			name = playerName;
 			checkDisconnected = disconnect;
 		}
 		
 		public void run()
 		{
 			OfflinePlayer player = Bukkit.getServer().getPlayerExact(name);
 			if ( player == null )
 				player = Bukkit.getServer().getOfflinePlayer(name);
 			
 			if ( checkDisconnected )
 			{
 				if ( player != null && player.isOnline() )
 					return; // player has reconnected, so don't do anything
 				
 				if ( Helper.isAlive(game, player) )
 					plugin.statsManager.playerQuit(game.getNumber());
 			}
 			PlayerManager.instance.playerKilled(game, player);
 		}
 	}
 	
 	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
 	public void onEvent(org.bukkit.event.block.BlockBurnEvent event) throws EventException
 	{
 		Game game = plugin.getGameForWorld(event.getBlock().getWorld());
 		if ( game != null )
 			fireGameEvents(event, game);
 	}
 
 	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
 	public void onEvent(org.bukkit.event.block.BlockCanBuildEvent event) throws EventException
 	{
 		Game game = plugin.getGameForWorld(event.getBlock().getWorld());
 		if ( game != null )
 			fireGameEvents(event, game);
 	}
 
 	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
 	public void onEvent(org.bukkit.event.block.BlockDamageEvent event) throws EventException
 	{
 		Game game = plugin.getGameForWorld(event.getBlock().getWorld());
 		if ( game != null )
 			fireGameEvents(event, game);
 	}
 
 	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
 	public void onEvent(org.bukkit.event.block.BlockDispenseEvent event) throws EventException
 	{
 		Game game = plugin.getGameForWorld(event.getBlock().getWorld());
 		if ( game != null )
 			fireGameEvents(event, game);
 	}
 
 	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
 	public void onEvent(org.bukkit.event.block.BlockExpEvent event) throws EventException
 	{
 		Game game = plugin.getGameForWorld(event.getBlock().getWorld());
 		if ( game != null )
 			fireGameEvents(event, game);
 	}
 
 	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
 	public void onEvent(org.bukkit.event.block.BlockFadeEvent event) throws EventException
 	{
 		Game game = plugin.getGameForWorld(event.getBlock().getWorld());
 		if ( game != null )
 			fireGameEvents(event, game);
 	}
 
 	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
 	public void onEvent(org.bukkit.event.block.BlockFormEvent event) throws EventException
 	{
 		Game game = plugin.getGameForWorld(event.getBlock().getWorld());
 		if ( game != null )
 			fireGameEvents(event, game);
 	}
 
 	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
 	public void onEvent(org.bukkit.event.block.BlockGrowEvent event) throws EventException
 	{
 		Game game = plugin.getGameForWorld(event.getBlock().getWorld());
 		if ( game != null )
 			fireGameEvents(event, game);
 	}
 
 	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
 	public void onEvent(org.bukkit.event.block.BlockIgniteEvent event) throws EventException
 	{
 		Game game = plugin.getGameForWorld(event.getBlock().getWorld());
 		if ( game != null )
 			fireGameEvents(event, game);
 	}
 
 	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
 	public void onEvent(org.bukkit.event.block.BlockPhysicsEvent event) throws EventException
 	{
 		Game game = plugin.getGameForWorld(event.getBlock().getWorld());
 		if ( game != null )
 			fireGameEvents(event, game);
 	}
 
 	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
 	public void onEvent(org.bukkit.event.block.BlockPistonExtendEvent event) throws EventException
 	{
 		// prevent pistons pushing things into/out of protected locations
 		event.setCancelled(plugin.worldManager.isProtectedLocation(event.getBlock().getLocation(), null));
 		
 		Game game = plugin.getGameForWorld(event.getBlock().getWorld());
 		if ( game != null )
 			fireGameEvents(event, game);
 	}
 
 	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
 	public void onEvent(org.bukkit.event.block.BlockPistonRetractEvent event) throws EventException
 	{
 		Game game = plugin.getGameForWorld(event.getBlock().getWorld());
 		if ( game != null )
 			fireGameEvents(event, game);
 	}
 
 	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
 	public void onEvent(org.bukkit.event.block.BlockRedstoneEvent event) throws EventException
 	{
 		Game game = plugin.getGameForWorld(event.getBlock().getWorld());
 		if ( game != null )
 			fireGameEvents(event, game);
 	}
 
 	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
 	public void onEvent(org.bukkit.event.block.BlockSpreadEvent event) throws EventException
 	{
 		Game game = plugin.getGameForWorld(event.getBlock().getWorld());
 		if ( game != null )
 			fireGameEvents(event, game);
 	}
 
 	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
 	public void onEvent(org.bukkit.event.block.LeavesDecayEvent event) throws EventException
 	{
 		Game game = plugin.getGameForWorld(event.getBlock().getWorld());
 		if ( game != null )
 			fireGameEvents(event, game);
 	}
 
 	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
 	public void onEvent(org.bukkit.event.block.NotePlayEvent event) throws EventException
 	{
 		Game game = plugin.getGameForWorld(event.getBlock().getWorld());
 		if ( game != null )
 			fireGameEvents(event, game);
 	}
 
 	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
 	public void onEvent(org.bukkit.event.block.SignChangeEvent event) throws EventException
 	{
 		Game game = plugin.getGameForWorld(event.getBlock().getWorld());
 		if ( game != null )
 			fireGameEvents(event, game);
 	}
 
 	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
 	public void onEvent(org.bukkit.event.enchantment.EnchantItemEvent event) throws EventException
 	{
 		Game game = plugin.getGameForWorld(event.getEnchanter().getWorld());
 		if ( game != null )
 			fireGameEvents(event, game);
 	}
 
 	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
 	public void onEvent(org.bukkit.event.enchantment.PrepareItemEnchantEvent event) throws EventException
 	{
 		Game game = plugin.getGameForWorld(event.getEnchanter().getWorld());
 		if ( game != null )
 			fireGameEvents(event, game);
 	}
 
 	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
 	public void onEvent(org.bukkit.event.entity.CreatureSpawnEvent event) throws EventException
 	{
 		Game game = plugin.getGameForWorld(event.getLocation().getWorld());
 		if ( game != null )
 			fireGameEvents(event, game);
 	}
 
 	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
 	public void onEvent(org.bukkit.event.entity.CreeperPowerEvent event) throws EventException
 	{
 		Game game = plugin.getGameForWorld(event.getEntity().getWorld());
 		if ( game != null )
 			fireGameEvents(event, game);
 	}
 
 	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
 	public void onEvent(org.bukkit.event.entity.EntityChangeBlockEvent event) throws EventException
 	{
 		Game game = plugin.getGameForWorld(event.getBlock().getWorld());
 		if ( game != null )
 			fireGameEvents(event, game);
 	}
 
 	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
 	public void onEvent(org.bukkit.event.entity.EntityCombustEvent event) throws EventException
 	{
 		Game game = plugin.getGameForWorld(event.getEntity().getWorld());
 		if ( game != null )
 			fireGameEvents(event, game);
 	}
 
 	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
 	public void onEvent(org.bukkit.event.entity.EntityCreatePortalEvent event) throws EventException
 	{
 		Game game = plugin.getGameForWorld(event.getEntity().getWorld());
 		if ( game != null )
 			fireGameEvents(event, game);
 	}
 
 	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
 	public void onEvent(org.bukkit.event.entity.EntityInteractEvent event) throws EventException
 	{
 		Game game = plugin.getGameForWorld(event.getEntity().getWorld());
 		if ( game != null )
 			fireGameEvents(event, game);
 	}
 
 	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
 	public void onEvent(org.bukkit.event.entity.EntityPortalEnterEvent event) throws EventException
 	{
 		Game game = plugin.getGameForWorld(event.getEntity().getWorld());
 		if ( game != null )
 			fireGameEvents(event, game);
 	}
 
 	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
 	public void onEvent(org.bukkit.event.entity.EntityPortalExitEvent event) throws EventException
 	{
 		Game game = plugin.getGameForWorld(event.getEntity().getWorld());
 		if ( game != null )
 			fireGameEvents(event, game);
 	}
 
 	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
 	public void onEvent(org.bukkit.event.entity.EntityRegainHealthEvent event) throws EventException
 	{
 		Game game = plugin.getGameForWorld(event.getEntity().getWorld());
 		if ( game != null )
 			fireGameEvents(event, game);
 	}
 
 	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
 	public void onEvent(org.bukkit.event.entity.EntityShootBowEvent event) throws EventException
 	{
 		Game game = plugin.getGameForWorld(event.getEntity().getWorld());
 		if ( game != null )
 			fireGameEvents(event, game);
 	}
 
 	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
 	public void onEvent(org.bukkit.event.entity.EntityTameEvent event) throws EventException
 	{
 		Game game = plugin.getGameForWorld(event.getEntity().getWorld());
 		if ( game != null )
 			fireGameEvents(event, game);
 	}
 
 	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
 	public void onEvent(org.bukkit.event.entity.EntityTeleportEvent event) throws EventException
 	{
 		Game game = plugin.getGameForWorld(event.getEntity().getWorld());
 		if ( game != null )
 			fireGameEvents(event, game);
 	}
 
 	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
 	public void onEvent(org.bukkit.event.entity.ExpBottleEvent event) throws EventException
 	{
 		Game game = plugin.getGameForWorld(event.getEntity().getWorld());
 		if ( game != null )
 			fireGameEvents(event, game);
 	}
 
 	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
 	public void onEvent(org.bukkit.event.entity.ExplosionPrimeEvent event) throws EventException
 	{
 		Game game = plugin.getGameForWorld(event.getEntity().getWorld());
 		if ( game != null )
 			fireGameEvents(event, game);
 	}
 
 	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
 	public void onEvent(org.bukkit.event.entity.FoodLevelChangeEvent event) throws EventException
 	{
 		Game game = plugin.getGameForWorld(event.getEntity().getWorld());
 		if ( game != null )
 			fireGameEvents(event, game);
 	}
 
 	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
 	public void onEvent(org.bukkit.event.entity.ItemDespawnEvent event) throws EventException
 	{
 		Game game = plugin.getGameForWorld(event.getEntity().getWorld());
 		if ( game != null )
 			fireGameEvents(event, game);
 	}
 
 	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
 	public void onEvent(org.bukkit.event.entity.PigZapEvent event) throws EventException
 	{
 		Game game = plugin.getGameForWorld(event.getEntity().getWorld());
 		if ( game != null )
 			fireGameEvents(event, game);
 	}
 
 	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
 	public void onEvent(org.bukkit.event.entity.PotionSplashEvent event) throws EventException
 	{
 		Game game = plugin.getGameForWorld(event.getEntity().getWorld());
 		if ( game != null )
 			fireGameEvents(event, game);
 	}
 
 	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
 	public void onEvent(org.bukkit.event.entity.ProjectileHitEvent event) throws EventException
 	{
 		Game game = plugin.getGameForWorld(event.getEntity().getWorld());
 		if ( game != null )
 			fireGameEvents(event, game);
 	}
 
 	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
 	public void onEvent(org.bukkit.event.entity.ProjectileLaunchEvent event) throws EventException
 	{
 		Game game = plugin.getGameForWorld(event.getEntity().getWorld());
 		if ( game != null )
 			fireGameEvents(event, game);
 	}
 
 	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
 	public void onEvent(org.bukkit.event.entity.SheepDyeWoolEvent event) throws EventException
 	{
 		Game game = plugin.getGameForWorld(event.getEntity().getWorld());
 		if ( game != null )
 			fireGameEvents(event, game);
 	}
 
 	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
 	public void onEvent(org.bukkit.event.entity.SheepRegrowWoolEvent event) throws EventException
 	{
 		Game game = plugin.getGameForWorld(event.getEntity().getWorld());
 		if ( game != null )
 			fireGameEvents(event, game);
 	}
 
 	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
 	public void onEvent(org.bukkit.event.entity.SlimeSplitEvent event) throws EventException
 	{
 		Game game = plugin.getGameForWorld(event.getEntity().getWorld());
 		if ( game != null )
 			fireGameEvents(event, game);
 	}
 
 	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
 	public void onEvent(org.bukkit.event.inventory.BrewEvent event) throws EventException
 	{
 		Game game = plugin.getGameForWorld(event.getBlock().getWorld());
 		if ( game != null )
 			fireGameEvents(event, game);
 	}
 
 	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
 	public void onEvent(org.bukkit.event.inventory.FurnaceBurnEvent event) throws EventException
 	{
 		Game game = plugin.getGameForWorld(event.getBlock().getWorld());
 		if ( game != null )
 			fireGameEvents(event, game);
 	}
 
 	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
 	public void onEvent(org.bukkit.event.inventory.FurnaceSmeltEvent event) throws EventException
 	{
 		Game game = plugin.getGameForWorld(event.getBlock().getWorld());
 		if ( game != null )
 			fireGameEvents(event, game);
 	}
 
 	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
 	public void onEvent(org.bukkit.event.inventory.InventoryCloseEvent event) throws EventException
 	{
 		World world = event.getPlayer().getWorld();
 		
 		if ( world == plugin.stagingWorld )
 		{
 			// if the inventory is a game configuration inventory menu, tell the relevant game's game configuration
 			GameConfiguration.checkEvent(event);
 			return;
 		}
 		
 		Game game = plugin.getGameForWorld(world);
 		if ( game != null )
 			fireGameEvents(event, game);
 	}
 
 	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
 	public void onEvent(org.bukkit.event.inventory.InventoryDragEvent event) throws EventException
 	{
 		Game game = plugin.getGameForWorld(event.getWhoClicked().getWorld());
 		if ( game != null )
 			fireGameEvents(event, game);
 	}
 
 	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
 	public void onEvent(org.bukkit.event.inventory.InventoryEvent event) throws EventException
 	{
 		Game game = plugin.getGameForWorld(event.getViewers().get(0).getWorld());
 		if ( game != null )
 			fireGameEvents(event, game);
 	}
 
 	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
 	public void onEvent(org.bukkit.event.inventory.InventoryMoveItemEvent event) throws EventException
 	{
 		Game game = plugin.getGameForWorld(event.getInitiator().getViewers().get(0).getWorld());
 		if ( game != null )
 			fireGameEvents(event, game);
 	}
 
 	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
 	public void onEvent(org.bukkit.event.inventory.InventoryOpenEvent event) throws EventException
 	{
 		Game game = plugin.getGameForWorld(event.getPlayer().getWorld());
 		if ( game != null )
 			fireGameEvents(event, game);
 	}
 
 	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
 	public void onEvent(org.bukkit.event.inventory.InventoryPickupItemEvent event) throws EventException
 	{
 		Game game = plugin.getGameForWorld(event.getItem().getWorld());
 		if ( game != null )
 			fireGameEvents(event, game);
 	}
 
 	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
 	public void onEvent(org.bukkit.event.player.PlayerAnimationEvent event) throws EventException
 	{
 		Game game = plugin.getGameForWorld(event.getPlayer().getWorld());
 		if ( game != null )
 			fireGameEvents(event, game);
 	}
 
 	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
 	public void onEvent(org.bukkit.event.player.PlayerBedEnterEvent event) throws EventException
 	{
 		Game game = plugin.getGameForWorld(event.getPlayer().getWorld());
 		if ( game != null )
 			fireGameEvents(event, game);
 	}
 
 	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
 	public void onEvent(org.bukkit.event.player.PlayerBedLeaveEvent event) throws EventException
 	{
 		Game game = plugin.getGameForWorld(event.getPlayer().getWorld());
 		if ( game != null )
 			fireGameEvents(event, game);
 	}
 
 	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
 	public void onEvent(org.bukkit.event.player.PlayerBucketFillEvent event) throws EventException
 	{
 		Game game = plugin.getGameForWorld(event.getPlayer().getWorld());
 		if ( game != null )
 			fireGameEvents(event, game);
 	}
 
 	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
 	public void onEvent(org.bukkit.event.player.PlayerChannelEvent event) throws EventException
 	{
 		Game game = plugin.getGameForWorld(event.getPlayer().getWorld());
 		if ( game != null )
 			fireGameEvents(event, game);
 	}
 
 	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
 	public void onEvent(org.bukkit.event.player.PlayerChatTabCompleteEvent event) throws EventException
 	{
 		Game game = plugin.getGameForWorld(event.getPlayer().getWorld());
 		if ( game != null )
 			fireGameEvents(event, game);
 	}
 
 	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
 	public void onEvent(org.bukkit.event.player.PlayerCommandPreprocessEvent event) throws EventException
 	{
 		Game game = plugin.getGameForWorld(event.getPlayer().getWorld());
 		if ( game != null )
 			fireGameEvents(event, game);
 	}
 	
 	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
 	public void onEvent(org.bukkit.event.player.PlayerEditBookEvent event) throws EventException
 	{
 		Game game = plugin.getGameForWorld(event.getPlayer().getWorld());
 		if ( game != null )
 			fireGameEvents(event, game);
 	}
 
 	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
 	public void onEvent(org.bukkit.event.player.PlayerEggThrowEvent event) throws EventException
 	{
 		Game game = plugin.getGameForWorld(event.getPlayer().getWorld());
 		if ( game != null )
 			fireGameEvents(event, game);
 	}
 
 	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
 	public void onEvent(org.bukkit.event.player.PlayerExpChangeEvent event) throws EventException
 	{
 		Game game = plugin.getGameForWorld(event.getPlayer().getWorld());
 		if ( game != null )
 			fireGameEvents(event, game);
 	}
 
 	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
 	public void onEvent(org.bukkit.event.player.PlayerFishEvent event) throws EventException
 	{
 		Game game = plugin.getGameForWorld(event.getPlayer().getWorld());
 		if ( game != null )
 			fireGameEvents(event, game);
 	}
 
 	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
 	public void onEvent(org.bukkit.event.player.PlayerGameModeChangeEvent event) throws EventException
 	{
 		Game game = plugin.getGameForWorld(event.getPlayer().getWorld());
 		if ( game != null )
 			fireGameEvents(event, game);
 	}
 
 	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
 	public void onEvent(org.bukkit.event.player.PlayerItemBreakEvent event) throws EventException
 	{
 		Game game = plugin.getGameForWorld(event.getPlayer().getWorld());
 		if ( game != null )
 			fireGameEvents(event, game);
 	}
 
 	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
 	public void onEvent(org.bukkit.event.player.PlayerItemConsumeEvent event) throws EventException
 	{
 		Game game = plugin.getGameForWorld(event.getPlayer().getWorld());
 		if ( game != null )
 			fireGameEvents(event, game);
 	}
 
 	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
 	public void onEvent(org.bukkit.event.player.PlayerKickEvent event) throws EventException
 	{
 		Game game = plugin.getGameForWorld(event.getPlayer().getWorld());
 		if ( game != null )
 			fireGameEvents(event, game);
 	}
 
 	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
 	public void onEvent(org.bukkit.event.player.PlayerLevelChangeEvent event) throws EventException
 	{
 		Game game = plugin.getGameForWorld(event.getPlayer().getWorld());
 		if ( game != null )
 			fireGameEvents(event, game);
 	}
 
 	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
 	public void onEvent(org.bukkit.event.player.PlayerMoveEvent event) throws EventException
 	{
 		Game game = plugin.getGameForWorld(event.getPlayer().getWorld());
 		if ( game != null )
 			fireGameEvents(event, game);
 	}
 
 	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
 	public void onEvent(org.bukkit.event.player.PlayerShearEntityEvent event) throws EventException
 	{
 		Game game = plugin.getGameForWorld(event.getPlayer().getWorld());
 		if ( game != null )
 			fireGameEvents(event, game);
 	}
 
 	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
 	public void onEvent(org.bukkit.event.player.PlayerTeleportEvent event) throws EventException
 	{
 		Game game = plugin.getGameForWorld(event.getPlayer().getWorld());
 		if ( game != null )
 			fireGameEvents(event, game);
 	}
 
 	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
 	public void onEvent(org.bukkit.event.player.PlayerToggleFlightEvent event) throws EventException
 	{
 		Game game = plugin.getGameForWorld(event.getPlayer().getWorld());
 		if ( game != null )
 			fireGameEvents(event, game);
 	}
 
 	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
 	public void onEvent(org.bukkit.event.player.PlayerToggleSneakEvent event) throws EventException
 	{
 		Game game = plugin.getGameForWorld(event.getPlayer().getWorld());
 		if ( game != null )
 			fireGameEvents(event, game);
 	}
 
 	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
 	public void onEvent(org.bukkit.event.player.PlayerToggleSprintEvent event) throws EventException
 	{
 		Game game = plugin.getGameForWorld(event.getPlayer().getWorld());
 		if ( game != null )
 			fireGameEvents(event, game);
 	}
 
 	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
 	public void onEvent(org.bukkit.event.player.PlayerVelocityEvent event) throws EventException
 	{
 		Game game = plugin.getGameForWorld(event.getPlayer().getWorld());
 		if ( game != null )
 			fireGameEvents(event, game);
 	}
 
 	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
 	public void onEvent(org.bukkit.event.server.MapInitializeEvent event) throws EventException
 	{
 		Game game = plugin.getGameForWorld(event.getMap().getWorld());
 		if ( game != null )
 			fireGameEvents(event, game);
 	}
 
 	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
 	public void onEvent(org.bukkit.event.vehicle.VehicleBlockCollisionEvent event) throws EventException
 	{
 		Game game = plugin.getGameForWorld(event.getVehicle().getWorld());
 		if ( game != null )
 			fireGameEvents(event, game);
 	}
 
 	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
 	public void onEvent(org.bukkit.event.vehicle.VehicleCreateEvent event) throws EventException
 	{
 		Game game = plugin.getGameForWorld(event.getVehicle().getWorld());
 		if ( game != null )
 			fireGameEvents(event, game);
 	}
 
 	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
 	public void onEvent(org.bukkit.event.vehicle.VehicleDamageEvent event) throws EventException
 	{
 		Game game = plugin.getGameForWorld(event.getVehicle().getWorld());
 		if ( game != null )
 			fireGameEvents(event, game);
 	}
 
 	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
 	public void onEvent(org.bukkit.event.vehicle.VehicleDestroyEvent event) throws EventException
 	{
 		Game game = plugin.getGameForWorld(event.getVehicle().getWorld());
 		if ( game != null )
 			fireGameEvents(event, game);
 	}
 
 	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
 	public void onEvent(org.bukkit.event.vehicle.VehicleEnterEvent event) throws EventException
 	{
 		Game game = plugin.getGameForWorld(event.getVehicle().getWorld());
 		if ( game != null )
 			fireGameEvents(event, game);
 	}
 
 	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
 	public void onEvent(org.bukkit.event.vehicle.VehicleEntityCollisionEvent event) throws EventException
 	{
 		Game game = plugin.getGameForWorld(event.getVehicle().getWorld());
 		if ( game != null )
 			fireGameEvents(event, game);
 	}
 
 	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
 	public void onEvent(org.bukkit.event.vehicle.VehicleExitEvent event) throws EventException
 	{
 		Game game = plugin.getGameForWorld(event.getVehicle().getWorld());
 		if ( game != null )
 			fireGameEvents(event, game);
 	}
 
 	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
 	public void onEvent(org.bukkit.event.vehicle.VehicleMoveEvent event) throws EventException
 	{
 		Game game = plugin.getGameForWorld(event.getVehicle().getWorld());
 		if ( game != null )
 			fireGameEvents(event, game);
 	}
 
 	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
 	public void onEvent(org.bukkit.event.vehicle.VehicleUpdateEvent event) throws EventException
 	{
 		Game game = plugin.getGameForWorld(event.getVehicle().getWorld());
 		if ( game != null )
 			fireGameEvents(event, game);
 	}
 
 	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
 	public void onEvent(org.bukkit.event.weather.LightningStrikeEvent event) throws EventException
 	{
 		Game game = plugin.getGameForWorld(event.getWorld());
 		if ( game != null )
 			fireGameEvents(event, game);
 	}
 
 	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
 	public void onEvent(org.bukkit.event.weather.ThunderChangeEvent event) throws EventException
 	{
 		Game game = plugin.getGameForWorld(event.getWorld());
 		if ( game != null )
 			fireGameEvents(event, game);
 	}
 
 	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
 	public void onEvent(org.bukkit.event.weather.WeatherChangeEvent event) throws EventException
 	{
 		Game game = plugin.getGameForWorld(event.getWorld());
 		if ( game != null )
 			fireGameEvents(event, game);
 	}
 
 	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
 	public void onEvent(org.bukkit.event.world.ChunkLoadEvent event) throws EventException
 	{
 		Game game = plugin.getGameForWorld(event.getWorld());
 		if ( game != null )
 			fireGameEvents(event, game);
 	}
 
 	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
 	public void onEvent(org.bukkit.event.world.ChunkPopulateEvent event) throws EventException
 	{
 		Game game = plugin.getGameForWorld(event.getWorld());
 		if ( game != null )
 			fireGameEvents(event, game);
 	}
 
 	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
 	public void onEvent(org.bukkit.event.world.ChunkUnloadEvent event) throws EventException
 	{
 		Game game = plugin.getGameForWorld(event.getWorld());
 		if ( game != null )
 			fireGameEvents(event, game);
 	}
 
 	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
 	public void onEvent(org.bukkit.event.world.PortalCreateEvent event) throws EventException
 	{
 		Game game = plugin.getGameForWorld(event.getWorld());
 		if ( game != null )
 			fireGameEvents(event, game);
 	}
 
 	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
 	public void onEvent(org.bukkit.event.world.SpawnChangeEvent event) throws EventException
 	{
 		Game game = plugin.getGameForWorld(event.getWorld());
 		if ( game != null )
 			fireGameEvents(event, game);
 	}
 
 	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
 	public void onEvent(org.bukkit.event.world.StructureGrowEvent event) throws EventException
 	{
 		Game game = plugin.getGameForWorld(event.getWorld());
 		if ( game != null )
 			fireGameEvents(event, game);
 	}
 
 	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
 	public void onEvent(org.bukkit.event.world.WorldInitEvent event) throws EventException
 	{
 		Game game = plugin.getGameForWorld(event.getWorld());
 		if ( game != null )
 			fireGameEvents(event, game);
 	}
 
 	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
 	public void onEvent(org.bukkit.event.world.WorldLoadEvent event) throws EventException
 	{
 		Game game = plugin.getGameForWorld(event.getWorld());
 		if ( game != null )
 			fireGameEvents(event, game);
 	}
 
 	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
 	public void onEvent(org.bukkit.event.world.WorldSaveEvent event) throws EventException
 	{
 		Game game = plugin.getGameForWorld(event.getWorld());
 		if ( game != null )
 			fireGameEvents(event, game);
 	}
 
 	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
 	public void onEvent(org.bukkit.event.world.WorldUnloadEvent event) throws EventException
 	{
 		Game game = plugin.getGameForWorld(event.getWorld());
 		if ( game != null )
 			fireGameEvents(event, game);
 	}
 }
