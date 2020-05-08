 package muCkk.DeathAndRebirth.listener;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.ArrayList;
 
 import muCkk.DeathAndRebirth.DAR;
 import muCkk.DeathAndRebirth.ghost.Ghosts;
 import muCkk.DeathAndRebirth.ghost.Graves;
 import muCkk.DeathAndRebirth.ghost.Shrines;
 import muCkk.DeathAndRebirth.messages.Errors;
 import muCkk.DeathAndRebirth.messages.Messages;
 import net.minecraft.server.Packet29DestroyEntity;
 
 import org.bukkit.Bukkit;
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.block.Block;
 import org.bukkit.craftbukkit.entity.CraftPlayer;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.Action;
 import org.bukkit.event.player.AsyncPlayerChatEvent;
 import org.bukkit.event.player.PlayerCommandPreprocessEvent;
 import org.bukkit.event.player.PlayerDropItemEvent;
 import org.bukkit.event.player.PlayerInteractEvent;
 import org.bukkit.event.player.PlayerJoinEvent;
 import org.bukkit.event.player.PlayerMoveEvent;
 import org.bukkit.event.player.PlayerPickupItemEvent;
 import org.bukkit.event.player.PlayerPortalEvent;
 import org.bukkit.event.player.PlayerQuitEvent;
 import org.bukkit.event.player.PlayerRespawnEvent;
 import org.bukkit.event.player.PlayerTeleportEvent;
 import org.bukkit.inventory.ItemStack;
 
 public class PListener implements Listener {
 
 	private Ghosts ghosts;
 	private Graves graves;
 	private Shrines shrines;
 	private DAR plugin;
 	private double flySpeed;
 	
 	private ArrayList<String> checkList;
 	
 	public PListener(DAR plugin, Ghosts ghosts, Shrines shrines, Graves graves) {
 		this.plugin = plugin;
 		this.flySpeed = plugin.getConfig().getDouble("FLY_SPEED");
 		this.ghosts = ghosts;
 		this.shrines = shrines;
 		this.graves = graves;
 		checkList = new ArrayList<String>();
 	}
 	
 	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
 	public void onPlayerDropItem(PlayerDropItemEvent event) {
 		if(ghosts.isGhost(event.getPlayer())) event.setCancelled(true);
 	}
 	
 	/**
 	 * Looks for new Players and adds them to the list
 	 */
 	@EventHandler(priority = EventPriority.LOWEST)
 	public void onPlayerJoin(final PlayerJoinEvent event) {
 		final Player player = event.getPlayer();
 		if(!ghosts.existsPlayer(player)) {
 			ghosts.newPlayer(player);
 		}
 	// if dead players join
 		if(ghosts.isGhost(player)) {
 			ghosts.setDisplayName(player, true);
 			
 			if(plugin.getConfig().getBoolean("GRAVE_SIGNS"))
 			{
 				String playerName = player.getName();
 				String worldName = player.getWorld().getName();
 				String l1 = plugin.getConfig().getString("GRAVE_TEXT");
 				Block block = ghosts.getLocation(player, worldName).getBlock();
 				
 				ghosts.getCustomConfig().set("players."+ playerName +"."+ worldName +".offline", false);
 				graves.removeSign(block, playerName, worldName);
 				graves.placeSign(block, l1, playerName);
 			}
 			
 			// compass
 			// reverse spawning
 			if (!plugin.getConfig().getBoolean("CORPSE_SPAWNING")) {
 				Location corpse = ghosts.getLocation(player, player.getWorld().getName());
 				player.setCompassTarget(corpse);
 			}
 			// corpse spawning
 			else {
 				Location nearestShrine = shrines.getNearestShrineSpawn(player.getLocation());
 				if (nearestShrine != null) player.setCompassTarget(nearestShrine);
 			}
 			// end compass
 			if(plugin.getConfig().getBoolean("INVISIBILITY")) ghosts.vanish(player);
 			if (plugin.getConfig().getBoolean("SPOUT_ENABLED")) {
 				plugin.darSpout.setDeathOptions(player, plugin.getConfig().getString("GHOST_SKIN"));
 			}
 			plugin.message.send(player, Messages.playerDied);
 		}
 		else if(plugin.getConfig().getBoolean("INVISIBILITY")) hideGhosts(player); 
 		
 	// version checking
 	// in it's own thread because it takes some time and would stop the rest of the world to load
 		if(player.isOp() || plugin.hasPermAdminNoMsg(player)) {
 			new Thread() {
 				public void run() {
 					try {
 						URL versionURL = new URL("http://dl.dropbox.com/u/96045686/DeathAndRebirth/Version.txt");
 						BufferedReader reader = new BufferedReader(new InputStreamReader(versionURL.openStream()));
 						
 						String line = reader.readLine();
 						if (!plugin.getDescription().getVersion().equalsIgnoreCase(line)) {
 							plugin.message.sendChat(player, Messages.newVersion, ": " + line);
 						}
 						reader.close();
 					} catch (MalformedURLException e) {
 						// versionURL
 						Errors.readingURL();
 						e.printStackTrace();
 					} catch (IOException e) {
 						// versionURL.openstream()
 						Errors.openingURL();
 						e.printStackTrace();
 					}
 				}
 			}.start();
 		}
 	}
 	
 	// if otherplayer is a ghost make him invisible
 	private void hideGhosts(Player player) {
 		Player[] onlinePlayers = player.getServer().getOnlinePlayers();
 		for (Player otherPlayer : onlinePlayers) {
 			if (otherPlayer == player || !ghosts.isGhost(otherPlayer)) continue;
 			((CraftPlayer) player).getHandle().netServerHandler.sendPacket(new Packet29DestroyEntity(((CraftPlayer) otherPlayer).getEntityId()));
 		}
 	}
 	
 	/**
 	 * Checks if ghosts are allowed to chat
 	 */
 
 	
 	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
 	public void onAsyncPlayerChat(AsyncPlayerChatEvent event) {
 		Player player = event.getPlayer();
 		if(ghosts.isGhost(player) && !plugin.getConfig().getBoolean("GHOST_CHAT")) {
 			plugin.message.send(player, Messages.ghostNoChat);
 			event.setCancelled(true);
 		}
 	}
 	
 	/**
 	 * Sets the players respawn location to their location of death
 	 */
 	@EventHandler(priority = EventPriority.HIGHEST)
 	public void onPlayerRespawn(PlayerRespawnEvent event) {
 		Player player = event.getPlayer();
 	// check if the world is enabled
 		if(!plugin.getConfig().getBoolean(player.getWorld().getName())) return;
 		
 		if(ghosts.isGhost(player)) {
 			// reverse spawning		
 			Location nearestShrine = shrines.getNearestShrineSpawn(player.getLocation());
 			Location corpse = ghosts.getLocation(player, player.getWorld().getName());
 			if (!plugin.getConfig().getBoolean("CORPSE_SPAWNING")) {
 				Location loc = ghosts.getBoundShrine(player);
 				
 				if (loc != null) event.setRespawnLocation(loc);
 				else if(nearestShrine != null) event.setRespawnLocation(nearestShrine);
 				giveGhostCompass(player, corpse);
 			}
 			// corpse spawning
 			else {
 				event.setRespawnLocation(corpse);
 				if (nearestShrine != null) giveGhostCompass(player, nearestShrine);
 			}
 			plugin.message.send(player, Messages.playerDied);
 	//  spout related
 			if (plugin.getConfig().getBoolean("SPOUT_ENABLED")) {
 				plugin.darSpout.setDeathOptions(player, plugin.getConfig().getString("GHOST_SKIN"));
 			}
 			ghosts.setDisplayName(player, true);
 		// invisibility
 			if (plugin.getConfig().getBoolean("INVISIBILITY")) ghosts.vanish(player);
 		}
 	}
 	
 	public void giveGhostCompass(final Player player, final Location loc) {
 	    if(plugin.getConfig().getBoolean("COMPASS") == true)
 	    {
 	        new Thread() {
 	        	@Override
 	        	public void run() {
 	        		try {
 	        			sleep(1000);
 	        		} catch (InterruptedException e) {
 	        			e.printStackTrace();
 	        		}
 				
 	        		ItemStack compass = new ItemStack(345);
 	        		compass.setAmount(1);
 	        		player.getInventory().addItem(compass);
 	        		player.setCompassTarget(loc);
 	        	}
 	        }.start();
 	    }
 	}
 
 	
 	/**
 	 * Prevents dead players from picking up items
 	 */
 	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
 	public void onPlayerPickupItem(PlayerPickupItemEvent event) {
 		// check if the world is enabled
 		if(!plugin.getConfig().getBoolean(event.getPlayer().getWorld().getName())) {
 			return;
 		}
 		if(ghosts.isGhost(event.getPlayer())) {
 			event.setCancelled(true);
 		}
 	}
 	
 	/**
 	 * Flying for ghosts
 	 */
 	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
 	public void onPlayerMove(PlayerMoveEvent event) {		
 		final Player player = event.getPlayer();			
 		
 		if(ghosts.isGhost(player)) {
 		// inform ghost if he is on a shrine
 			if(!checkList.contains(player.getName())) {
 				if ((event.getFrom().getBlockX() != event.getTo().getBlockX()) || (event.getFrom().getBlockY() != event.getTo().getBlockY()) || (event.getFrom().getBlockZ() != event.getTo().getBlockZ())) {
 					if(!checkList.contains(player.getName())) {
 						if(shrines.isOnShrine(player)) { 
 							plugin.message.send(player, Messages.shrineArea);
 							checkList.add(player.getName());
 							// 5 second delay before that player gets checked again
 							plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
 								@Override
 								public void run() {
 									checkList.remove(player.getName());
 								}
 							}, 100L);
 						}
 						
 					}
 				}
 			}
 		// flying for ghosts
 			if(player.isSneaking() &&  plugin.getConfig().getBoolean("FLY")) {		
 				player.setVelocity(player.getLocation().getDirection().multiply(flySpeed));
 				return;
 			}
 		}
 	}
 	
 	/**
 	 * Players try to bind their soul to a shrine
 	 */
 	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
 	public void onPlayerInteract(PlayerInteractEvent event) {
 		Player player = event.getPlayer();
		boolean canRes = ghosts.getCustomConfig().getBoolean("players."+player.getName() +"."+player.getWorld().getName() +".canres");
 		
 		// check if the world is enabled
 		if(!plugin.getConfig().getBoolean(player.getWorld().getName()))
 			return;
 		
 	// *** hardcore mode ***
 		if(plugin.getConfig().getBoolean("HARDCORE") && canRes)
 		{
 			Player[] all = Bukkit.getServer().getOnlinePlayers();
 			for(Player hPlayer:all)
 			{
 				String hPlayerName = hPlayer.getName();
 				Location hGrave = ghosts.getLocation(hPlayer, hPlayer.getWorld().getName());
 				String worldName = hGrave.getWorld().getName();
 				String shrine = shrines.getClose(hPlayer.getLocation());
 				int timer = plugin.getConfig().getInt("TIMER")*60;
 				
 				//if he's a ghost it's checked if his grave is right clicked				
 				if(hPlayer.getWorld() == player.getWorld() && hPlayer != player && plugin.hasPermRebOthers(player) && !ghosts.isGhost(player) && ghosts.isGhost(hPlayer) && plugin.getConfig().getBoolean("GRAVE_SIGNS") && event.getClickedBlock().getLocation().distance(hGrave) < 3 && plugin.getConfig().getBoolean("OTHERS_RESURRECT"))
 				{
 					if(plugin.getConfig().getBoolean("ONLY_DAY") && player.getWorld().getTime()  >= 12000 && player.getWorld().getTime() <= 24000)
 						plugin.message.send(player, Messages.mustBeDay);
 					
 					if(plugin.getConfig().getBoolean("ONLY_DAY") && player.getWorld().getTime()  >= 0 && player.getWorld().getTime() <= 12000)
 					{
 						ghosts.punishResurrecter(player);
 						ghosts.resurrect(player, hPlayer);
 						ghosts.selfResPunish(hPlayer);
 					}
 					else if(hPlayer != player)
 					{
 						ghosts.punishResurrecter(player);
 						ghosts.resurrect(player, hPlayer);
 						ghosts.selfResPunish(hPlayer);
 
 					}
 					else
 					{
 						plugin.message.send(player, Messages.cantResurrect);						
 					}
 				}
 				else if(ghosts.isGhost(hPlayer))
 				{
 					long currentTime = System.currentTimeMillis();
 					long startTime = ghosts.getCustomConfig().getLong("players."+hPlayerName +"."+worldName +".starttime");
 					long diff = (currentTime - startTime)/1000;
 					
 					//normal spawning
 					if(!plugin.getConfig().getBoolean("CORPSE_SPAWNING"))
 					{
 						//checks if players grave is clicked and timer is expired
 						if (diff > timer && event.getClickedBlock().getLocation().distance(hGrave) < 3 && !plugin.getConfig().getBoolean("SHRINE_ONLY"))
 						{
 							if(plugin.getConfig().getBoolean("ONLY_DAY") && player.getWorld().getTime()  >= 12000 && player.getWorld().getTime() <= 24000)
 								plugin.message.sendChat(hPlayer, Messages.mustBeDay);
 							
 							if(plugin.getConfig().getBoolean("ONLY_DAY") && player.getWorld().getTime()  >= 0 && player.getWorld().getTime() <= 12000)
 							{
 								ghosts.resurrect(hPlayer);
 								ghosts.selfResPunish(hPlayer);
 							}
 							else if(diff > timer)
 							{
 								ghosts.resurrect(hPlayer);
 								ghosts.selfResPunish(hPlayer);
 							}
 							else
 							{
 								plugin.message.sendTime(hPlayer, Messages.timerNotExpired, checkTime(startTime));
 							}
 						}
 						else if (shrine != null && diff > timer && plugin.hasPermShrine(hPlayer, shrine)){
 							if(plugin.getConfig().getBoolean("ONLY_DAY") && player.getWorld().getTime()  >= 12000 && player.getWorld().getTime() <= 24000)
 								plugin.message.sendChat(hPlayer, Messages.mustBeDay);
 							
 							if(plugin.getConfig().getBoolean("ONLY_DAY") && player.getWorld().getTime()  >= 0 && player.getWorld().getTime() <= 12000)
 							{
 								ghosts.resurrect(player);
 								ghosts.selfResPunish(hPlayer);
 							}
 							else if(diff > timer)
 							{
 								ghosts.resurrect(player);
 								ghosts.selfResPunish(hPlayer);
 							}
 							else
 							{
 								plugin.message.sendTime(hPlayer, Messages.timerNotExpired, checkTime(startTime));
 							}	
 						}
 					}
 				// corpse spawning
 					else {
 						if (shrine != null && diff > timer && plugin.hasPermShrine(hPlayer, shrine))	
 						{							
 							if(plugin.getConfig().getBoolean("ONLY_DAY") && player.getWorld().getTime()  >= 12000 && player.getWorld().getTime() <= 24000)
 								plugin.message.sendChat(hPlayer, Messages.mustBeDay);
 							
 							if(plugin.getConfig().getBoolean("ONLY_DAY") && player.getWorld().getTime()  >= 0 && player.getWorld().getTime() <= 12000)
 							{
 								ghosts.resurrect(hPlayer);
 								ghosts.selfResPunish(hPlayer);
 							}
 							else if(diff > timer)
 							{
 								ghosts.resurrect(hPlayer);
 								ghosts.selfResPunish(hPlayer);
 							}
 							else
 							{
 								plugin.message.sendTime(hPlayer, Messages.timerNotExpired, checkTime(startTime));
 							}	
 						}
 						else if(event.getClickedBlock().getLocation().distance(hGrave) < 3 && !plugin.getConfig().getBoolean("SHRINE_ONLY") && plugin.getConfig().getBoolean("GRAVE_SIGNS")){
 							
 							if(diff > timer && plugin.getConfig().getBoolean("ONLY_DAY") && player.getWorld().getTime()  >= 12000 && player.getWorld().getTime() <= 24000)
 								plugin.message.sendChat(hPlayer, Messages.mustBeDay);
 							
 							if(diff > timer && plugin.getConfig().getBoolean("ONLY_DAY") && player.getWorld().getTime()  >= 0 && player.getWorld().getTime() <= 12000)
 							{
 								ghosts.resurrect(player);
 								ghosts.selfResPunish(player);
 							}
 							else if(diff > timer)
 							{
 								ghosts.resurrect(player);
 								ghosts.selfResPunish(player);
 							}
 							else
 							{	
 								plugin.message.sendTime(hPlayer, Messages.timerNotExpired, checkTime(startTime));
 							}
 						}
 					}
 				}
 			}
 		}
 		
 		if(plugin.getConfig().getBoolean("OTHERS_RESURRECT") && !plugin.getConfig().getBoolean("HARDCORE") && canRes)
 		{
 			Player [] all = Bukkit.getServer().getOnlinePlayers();
 			for(Player dPlayer:all)
 			{	
 				Location hGrave = ghosts.getLocation(dPlayer, dPlayer.getWorld().getName());
 				if(player.getWorld() == dPlayer.getWorld() && !ghosts.isGhost(player) && event.getClickedBlock().getLocation().distance(hGrave) < 3 && ghosts.isGhost(dPlayer) && plugin.getConfig().getBoolean("GRAVE_SIGNS") && plugin.hasPermRebOthers(player))
 				{
 					if(plugin.getConfig().getBoolean("ONLY_DAY") && player.getWorld().getTime() >= 12000 && player.getWorld().getTime() <= 24000)
 						plugin.message.sendChat(player, Messages.mustBeDay);
 				
 					if(plugin.getConfig().getBoolean("ONLY_DAY") && player.getWorld().getTime() >= 0 && player.getWorld().getTime() <= 12000)
 					{
 						ghosts.punishResurrecter(player);
 						ghosts.resurrect(player, dPlayer);
 					}
 					else if(!ghosts.isGhost(player))
 					{
 						ghosts.punishResurrecter(player);
 						ghosts.resurrect(player, dPlayer);
 					}
 					else
 						plugin.message.sendChat(player, Messages.cantResurrect);
 				}
 			}
 		}
 
 	// *** ghost interactions ***
 		if (ghosts.isGhost(player)) {
 			Block block = event.getClickedBlock();
 		// chest, furnace
 			try {
 				Material type = block.getType(); 			
 				if(			type.equals(Material.FURNACE)
 						||	type.equals(Material.CHEST)
 						|| (plugin.getConfig().getBoolean("BLOCK_GHOST_INTERACTION")
 						&& (type.equals(Material.BED)
 						|| type.equals(Material.LEVER)
 						|| type.equals(Material.STONE_BUTTON)
 						|| type.equals(Material.WOODEN_DOOR) || type.equals(Material.IRON_DOOR)))) {
 					plugin.message.send(player, Messages.cantDoThat);
 					event.setCancelled(true);
 					return;
 				}
 			}catch (NullPointerException e) {
 				// Material = null
 			}
 		// resurrection
 			if(event.getAction() == Action.RIGHT_CLICK_BLOCK && canRes) {				
 				Location locDeath = ghosts.getLocation(player, player.getWorld().getName());
 				// reverse spawning
 				if(!plugin.getConfig().getBoolean("CORPSE_SPAWNING") && !plugin.getConfig().getBoolean("HARDCORE"))
 				{
 					if(event.getClickedBlock().getLocation().distance(locDeath) < 3 && !plugin.getConfig().getBoolean("SHRINE_ONLY") && plugin.getConfig().getBoolean("ONLY_DAY") && player.getWorld().getTime() >= 12000 && player.getWorld().getTime() <= 24000)
 						plugin.message.sendChat(player, Messages.mustBeDay);
 				
 					if(event.getClickedBlock().getLocation().distance(locDeath) < 3 && !plugin.getConfig().getBoolean("SHRINE_ONLY") && plugin.getConfig().getBoolean("ONLY_DAY") && player.getWorld().getTime() >= 0 && player.getWorld().getTime() <= 12000) 
 						ghosts.resurrect(player);
 					
 					else if(event.getClickedBlock().getLocation().distance(locDeath) < 3 && !plugin.getConfig().getBoolean("SHRINE_ONLY")) 
 						ghosts.resurrect(player);
 					
 					else {
 						String shrine = shrines.getClose(player.getLocation());
 						
 						if(shrine != null && plugin.hasPermShrine(player, shrine) && plugin.getConfig().getBoolean("ONLY_DAY") && player.getWorld().getTime() >= 12000 && player.getWorld().getTime() <= 24000)
 							plugin.message.sendChat(player, Messages.mustBeDay);
 						
 						if(shrine != null && plugin.hasPermShrine(player, shrine) && plugin.getConfig().getBoolean("ONLY_DAY") && player.getWorld().getTime() >= 0 && player.getWorld().getTime() <= 12000) {
 							ghosts.resurrect(player);
 						    ghosts.selfResPunish(player);
 						}
 						
 						else if(shrine != null && plugin.hasPermShrine(player, shrine)) {
 							ghosts.resurrect(player);
 						    ghosts.selfResPunish(player);
 						}
 					}
 				}
 			// corpse spawning
 				else {
 					String shrine = shrines.getClose(player.getLocation());
 					
 					if (shrine != null && plugin.hasPermShrine(player, shrine) && !plugin.getConfig().getBoolean("HARDCORE") && plugin.getConfig().getBoolean("ONLY_DAY") && player.getWorld().getTime() >= 12000 && player.getWorld().getTime() <= 24000)
 						plugin.message.sendChat(player, Messages.mustBeDay);
 
 					if (shrine != null && !plugin.getConfig().getBoolean("HARDCORE") && plugin.hasPermShrine(player, shrine) && plugin.getConfig().getBoolean("ONLY_DAY") && player.getWorld().getTime() >= 0 && player.getWorld().getTime() <= 12000)
 						ghosts.resurrect(player);
 					
 					else if (shrine != null && !plugin.getConfig().getBoolean("HARDCORE") && plugin.hasPermShrine(player, shrine))
 						ghosts.resurrect(player);
 					
 					else {
 						if (event.getClickedBlock().getLocation().distance(locDeath) < 3 && !plugin.getConfig().getBoolean("SHRINE_ONLY") && plugin.getConfig().getBoolean("GRAVE_SIGNS") && !plugin.getConfig().getBoolean("HARDCORE") && plugin.getConfig().getBoolean("ONLY_DAY") && player.getWorld().getTime() >= 12000 && player.getWorld().getTime() <= 24000)
 							plugin.message.sendChat(player, Messages.mustBeDay);
 						
 						if (event.getClickedBlock().getLocation().distance(locDeath) < 3 && !plugin.getConfig().getBoolean("SHRINE_ONLY") && plugin.getConfig().getBoolean("GRAVE_SIGNS") && !plugin.getConfig().getBoolean("HARDCORE") && plugin.getConfig().getBoolean("ONLY_DAY") && player.getWorld().getTime() >= 0 && player.getWorld().getTime() <= 12000) {							
 							ghosts.resurrect(player);
 					        ghosts.selfResPunish(player);
 						}
 						else if (event.getClickedBlock().getLocation().distance(locDeath) < 3 && !plugin.getConfig().getBoolean("SHRINE_ONLY") && plugin.getConfig().getBoolean("GRAVE_SIGNS") && !plugin.getConfig().getBoolean("HARDCORE")) {							
 							ghosts.resurrect(player);
 					        ghosts.selfResPunish(player);
 						}
 					}
 				}
 			return;
 			}
 			
 			// normal interactions		
 			if (plugin.getConfig().getBoolean("BLOCK_GHOST_INTERACTION")) {
 				event.setCancelled(true);
 				return;
 			}
 		}
 	// *** selection mode for creating shrines ****************
 		if(player.getName().equalsIgnoreCase(shrines.getSelPlayer()) && shrines.isSelModeEnable() && player.getItemInHand().getTypeId() == 280) {
 			if(event.getAction() == Action.LEFT_CLICK_BLOCK) {
 				shrines.setSel1(event.getClickedBlock().getLocation());
 				player.sendMessage("Position 1 set");
 			}
 			if(event.getAction() == Action.RIGHT_CLICK_BLOCK) {
 				shrines.setSel2(event.getClickedBlock().getLocation());
 				player.sendMessage("Position 2 set");
 			}
 		}
 		
 	// *** shrine is clicked ***
 		if(event.getAction() == Action.RIGHT_CLICK_BLOCK) {
 			String shrine = shrines.getClose(player.getLocation());
 			if (shrine != null && plugin.hasPermShrine(player, shrine)) {
 			// check if soul can be bound at this shrine
 				if (!shrines.checkBinding(shrine, player.getWorld().getName())) {
 					plugin.message.send(player, Messages.cantBindSoul);
 					return;
 				}
 				Block clickedBlock = event.getClickedBlock();
 				Location boundShrine = ghosts.getBoundShrine(player);
 			// soul is bound
 				if (boundShrine != null) {
 					Location bshrine = shrines.getNearestShrine(ghosts.getBoundShrine(player));
 			// unbinding
 					if((shrines.getNearestShrine(player.getLocation() ) ).distance(bshrine) < 2) {
 						ghosts.unbind(player);
 						plugin.message.send(player, Messages.unbindSoul);
 						return;
 					}
 				}
 			// binding
 				if(shrines.isShrineArea(shrine, clickedBlock)) {
 					ghosts.bindSoul(player);
 					plugin.message.send(player, Messages.soulNowBound);
 				}
 			}
 			return;
 		}
 		
 	// *** getting the name of a shrine ******************************
 		if (player.isOp() && event.getAction() == Action.LEFT_CLICK_BLOCK && player.getItemInHand().getTypeId() == 280) {
 			String shrine = shrines.getClose(player.getLocation());
 			if (shrine != null) {
 				player.sendMessage("Shrine: "+shrine);
 			}
 			return;
 		}
 		
 		
 		//grave robbery
 		//Checks if grave robbery is enabled
 		if (plugin.getConfig().getDouble("GRAVEROBBERY") > 0.0 && plugin.getConfig().getBoolean("GRAVE_SIGNS") && plugin.hasPermRobb(player))
 		{			
 			Player[] all = Bukkit.getServer().getOnlinePlayers();
 			//for each player which is online is checked if he is a ghost
 			for(Player robbed:all)
 			{
 				Player robber = event.getPlayer();
 				String robberName = robber.getName();
 				String robbedName = robbed.getName();
 				Location robbedGrave = ghosts.getLocation(robbed, robbed.getWorld().getName());
 				Location ownGrave = ghosts.getLocation(robber, robber.getWorld().getName());
 				String worldName = robbedGrave.getWorld().getName();
 				double percent = plugin.getConfig().getDouble("GRAVEROBBERY");
 								
 			for (String listedItem:plugin.getConfig().getStringList("ROBBERY_ITEMS"))
 			{				
 				//if he's a ghost it's checked if his grave is right clicked
 				if((!ghosts.isGhost(robber)) && ghosts.isGhost(robbed) && event.getClickedBlock().getLocation().distance(robbedGrave) == 0 && !(event.getClickedBlock().getLocation().distance(ownGrave) == 0) && robber.getItemInHand().getType() == getMaterial(listedItem.toUpperCase()))
 				{
 					if(!ghosts.getCustomConfig().getBoolean("players."+robbedName +"."+ worldName +".graveRobbed"))
 					{
 						double robbedBalance = DAR.econ.getBalance(robbedName);
 						double amount = (robbedBalance/100)*percent;
 					
 						amount = amount*100;
 						amount = Math.round(amount);
 						amount = amount/100;
 					
 						DAR.econ.withdrawPlayer(robbedName, amount);
 						DAR.econ.depositPlayer(robberName, amount);
 					
 						plugin.message.sendRobber(robbed, robber, Messages.youRobbed, amount);
 						plugin.message.sendRobbed(robbed, robber, Messages.robbedYou, amount);
 						
 						ghosts.getCustomConfig().set("players."+robbedName +"."+ worldName +".graveRobbed", true);
 						ghosts.saveCustomConfig();
 					}
 					else
 						plugin.message.send(robber, Messages.alreadyRobbed);
 				}
 			}
 			}			
 		}
 	}
 	
 	/**
 	 * checks if the player enters a new world
 	 */
 	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
 	public void onPlayerTeleport(PlayerTeleportEvent event) {
 		Player player = event.getPlayer();	
 		try {
 			String world = event.getFrom().getWorld().getName();
 			String newworld = event.getTo().getWorld().getName();
 			
 			if(ghosts.isGhostInWorld(player, world) && !plugin.getConfig().getBoolean("GHOST_WORLD_CHANGE"))
 				event.setCancelled(true);
 			else
 				ghosts.worldChange(player, world, newworld);
 			
 		} catch(NullPointerException e) {
 			System.out.println("[Death and Rebirth] error: Could not find the entered world");
 		}
 	}
 	
 	/**
 	 * checks if the player enters a new world
 	 */
 	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
 	public void onPlayerPortal(PlayerPortalEvent event) {
 		Player player = event.getPlayer();	
 		try {
 			String world = event.getFrom().getWorld().getName();
 			String newworld = event.getTo().getWorld().getName();
 			
 			if(ghosts.isGhostInWorld(player, world) && !plugin.getConfig().getBoolean("GHOST_WORLD_CHANGE"))
 				event.setCancelled(true);
 			else
 				ghosts.worldChange(player, world, newworld);
 			
 		} catch(NullPointerException e) {
 			System.out.println("[Death and Rebirth] error: Could not find the entered world");
 		}
 	}
 	
 	@EventHandler(ignoreCancelled = true)
 	public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
 		Player player = event.getPlayer();
 		if(!ghosts.isGhost(player)) return;
 		//for every command which is in the disabledCommands list it creates an string command and if the used command starts with that string it will be cancelled
 		for(String command : plugin.getConfig().getStringList("DISABLED_COMMANDS"))
 		{
 			if(event.getMessage().startsWith(command))
 			{
 				event.setCancelled(true);
 				plugin.message.send(player, Messages.disabledCommand);
 			}
 
 		}
 	}
 	
 	@EventHandler(ignoreCancelled = true)
 	public void onPlayerQuit(PlayerQuitEvent event)
 	{
 		Player player = event.getPlayer();
 		String playerName = player.getName();
 		String worldName = player.getWorld().getName();
 		String l1 = plugin.getConfig().getString("GRAVE_TEXT");
 		if(ghosts.isGhost(player) && plugin.getConfig().getBoolean("GRAVE_SIGNS"))
 		{
 			Block block = ghosts.getLocation(player, worldName).getBlock();
 			ghosts.getCustomConfig().set("players."+ playerName +"."+ worldName +".offline", true);
 			graves.removeSign(block, playerName, worldName);
 			graves.placeSign(block, l1, playerName);
 		}
 		
 	}
 	
 	//gets the id or the name of the item from config and returns it as Material
 	public Material getMaterial(String id)
 	{
 		Material get = Material.getMaterial(id);
 		if(get != null) return get;
 	try
 	{
 		get = Material.getMaterial(Integer.valueOf(id));
 	}
 	catch(NumberFormatException e)
 	{
 	}
 	   return get;
 	}
 	
 	public int checkTime(long startTime)
 	{
 		int timer = plugin.getConfig().getInt("TIMER");	
 		long currentTime = System.currentTimeMillis();
 		long diff = (currentTime - startTime)/60000;
 		int time = (int) (timer-diff);
 		return time;
 	}
 }
