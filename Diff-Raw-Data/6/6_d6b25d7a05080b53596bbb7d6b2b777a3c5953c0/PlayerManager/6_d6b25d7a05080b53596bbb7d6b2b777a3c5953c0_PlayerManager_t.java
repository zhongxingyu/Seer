 package com.ftwinston.Killer;
 
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.Random;
 import java.util.Set;
 import java.util.TreeMap;
 
 import net.minecraft.server.Packet201PlayerInfo;
 import net.minecraft.server.Packet205ClientCommand;
 
 import org.bukkit.ChatColor;
 import org.bukkit.GameMode;
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.OfflinePlayer;
 import org.bukkit.block.Block;
 import org.bukkit.craftbukkit.entity.CraftPlayer;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.Inventory;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.inventory.PlayerInventory;
 import org.bukkit.potion.PotionEffectType;
 import org.bukkit.util.BlockIterator;
 import org.bukkit.util.Vector;
 
 class PlayerManager
 {
 	public static PlayerManager instance;
 	private Killer plugin;
 	private Random random;
 	private int killerAssignProcess, helpMessageProcess, compassProcess, spectatorFollowProcess;
 	
 	public PlayerManager(Killer _plugin)
 	{
 		this.plugin = _plugin;
 		instance = this;
 		random = new Random();
 		
 		transparentBlocks.clear();
 		transparentBlocks.add(new Byte((byte)Material.AIR.getId()));
 		transparentBlocks.add(new Byte((byte)Material.WATER.getId()));
 		transparentBlocks.add(new Byte((byte)Material.STATIONARY_WATER.getId()));
 	}
 	
 	public Map<String, Location> previousLocations = new HashMap<String, Location>();
 	public void movePlayerOutOfKillerGame(Player player)
 	{
 		Location exitPoint = previousLocations.get(player.getName());
 		if ( exitPoint != null )
 		{
 			plugin.getGameMode().broadcastMessage(player.getName() + " quit the game");
 			teleport(player, exitPoint);
 		}
 		else
 			player.sendMessage("Cannot quit killer, because there is no exit point stored for you! Leave this world via other commands, if the server has them.");
 	}
 	
 	public void movePlayerIntoKillerGame(Player player)
 	{
 		previousLocations.put(player.getName(), player.getLocation());
 		if ( plugin.getGameState().usesGameWorlds )
 			teleport(player, plugin.getGameMode().getSpawnLocation(player));
 		else
 			teleport(player, plugin.stagingWorldManager.getStagingWorldSpawnPoint());
 	}
 	
 	public class Info
 	{
 		public Info(boolean alive) { a = alive; t = -1; target = null; }
 		
 		private boolean a;
 		private int t;
 		public int getTeam() { return t; }
 		
 		public void setTeam(int i)
 		{
 			t = i;
 		}
 		
 		// am I a survivor or a spectator?
 		public boolean isAlive() { return a; }
 		
 		public void setAlive(boolean b)
 		{
 			a = b;
 		}
 		
 		// spectator target, and also kill target in Contract Killer mode
 		public String target;
 		
 		public int nextHelpMessage = 0;
 	}
 	
 	private TreeMap<String, Info> playerInfo = new TreeMap<String, Info>();
 	public Set<Map.Entry<String, Info>> getPlayerInfo() { return playerInfo.entrySet(); }
 	
 	public void setTeam(OfflinePlayer player, int teamNum)
 	{
 		Info info = playerInfo.get(player.getName());
 		if ( info != null )
 			info.setTeam(teamNum);
 	}
 
 	public void reset()
 	{
 		playerInfo.clear();
 		
 		if ( helpMessageProcess != -1 )
 		{
 			plugin.getServer().getScheduler().cancelTask(helpMessageProcess);
 			helpMessageProcess = -1;
 		}
 		
 		for ( Player player : plugin.getOnlinePlayers() )
 		{
 			resetPlayer(player);
 			setAlive(player, true);
 		}
 		
 		if ( Settings.banOnDeath )
 			for ( OfflinePlayer player : plugin.getServer().getBannedPlayers() )
 				player.setBanned(false);
 		
 		if ( killerAssignProcess != -1 )
 		{
 			plugin.getServer().getScheduler().cancelTask(killerAssignProcess);
 			killerAssignProcess = -1;
 		}
 
 		plugin.getServer().getScheduler().cancelTask(compassProcess);
 		plugin.getServer().getScheduler().cancelTask(spectatorFollowProcess);
 	}
 	
 	public void startGame()
 	{
 		reset();  
 		
 		// start sending out help messages explaining the game rules
 		helpMessageProcess = plugin.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable() {
 			public void run()
 			{
 				plugin.getGameMode().sendGameModeHelpMessage();
 			}
 		}, 0, 550L); // send every 25 seconds
 		
 		compassProcess = plugin.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable() {
         	public void run()
         	{
 	        	for ( Player player : plugin.getGameMode().getOnlinePlayers(true) )
 	        		if ( player.getInventory().contains(Material.COMPASS) )
 	        		{// does this need a null check on the target?
 	        			player.setCompassTarget(getCompassTarget(player));
 	        		}
         	}
         }, 20, 10);
 	        			
 		spectatorFollowProcess = plugin.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable() {
         	public void run()
         	{
 	        	for ( Player player : plugin.getGameMode().getOnlinePlayers(false) )
 	        	{
 	        		PlayerManager.Info info = getInfo(player.getName());
 	        		if (info.target != null )
 	        			checkFollowTarget(player, info.target);
 	        	}
         	}
         }, 40, 40);
 	}
 	
 	public void colorPlayerName(Player player, ChatColor color)
 	{
 		String oldListName = player.getPlayerListName();
 	
 		player.setDisplayName(color + ChatColor.stripColor(player.getDisplayName()));
 		
 		// mustn't be > 16 chars, or it throws an exception
 		String name = ChatColor.stripColor(player.getPlayerListName());
 		if ( name.length() > 15 )
 			name = name.substring(0, 15);
 		player.setPlayerListName(color + name);
 		
 		// ensure this change occurs on the scoreboard of anyone I'm currently invisible to
 		for ( Player online : plugin.getOnlinePlayers() )
 			if ( !online.canSee(player) )
 			{
 				((CraftPlayer)online).getHandle().netServerHandler.sendPacket(new Packet201PlayerInfo(oldListName, false, 9999));
 				sendForScoreboard(online, player, true);
 			}
 	}
 	
 	public void clearPlayerNameColor(Player player)
 	{
 		String oldListName = player.getPlayerListName();
 		
 		player.setDisplayName(ChatColor.stripColor(player.getDisplayName()));
 		player.setPlayerListName(ChatColor.stripColor(player.getPlayerListName()));
 		
 		// ensure this change occurs on the scoreboard of anyone I'm currently invisible to
 		for ( Player online : plugin.getOnlinePlayers() )
 			if ( online != player && !online.canSee(player) )
 			{
 				((CraftPlayer)online).getHandle().netServerHandler.sendPacket(new Packet201PlayerInfo(oldListName, false, 9999));
 				sendForScoreboard(online, player, true);
 			}
 	}
 
 	public void playerJoined(Player player)
 	{
 		Info info = playerInfo.get(player.getName());
 		boolean isNewPlayer;
 		if ( info == null )
 		{
 			isNewPlayer = true;
 			
 			if ( !plugin.getGameState().usesGameWorlds )
 				info = new Info(true);
 			else if ( Settings.lateJoinersStartAsSpectator )
 				info = new Info(false);
 			else
 			{
 				info = new Info(true);
 				plugin.statsManager.playerJoinedLate();
 			}
 			playerInfo.put(player.getName(), info);
 			
 			// this player is new for this game, but they might still have stuff from previous game on same world. clear them down.
 			resetPlayer(player);
 		}
 		else
 			isNewPlayer = false;
 		
 		if ( !plugin.getGameState().usesGameWorlds )
 			return;
 
 		// hide all spectators from this player
 		for ( Map.Entry<String, Info> entry : getPlayerInfo() )
 			if ( !entry.getValue().isAlive() && !entry.getKey().equals(player.getName()) )
 			{
 				Player other = plugin.getServer().getPlayerExact(entry.getKey());
 				if ( other != null && other.isOnline() && plugin.isGameWorld(other.getWorld()) )
 					hidePlayer(player, other);
 			}
 
 		plugin.getGameMode().playerJoinedLate(player, isNewPlayer);
 		
 		if ( !info.isAlive() )
 		{
 			String message = isNewPlayer ? "" : "Welcome Back. ";
 			message += "You are now a spectator. You can fly, but can't be seen or interact. Type " + ChatColor.YELLOW + "/spec" + ChatColor.RESET + " to list available commands.";
 			
 			player.sendMessage(message);
 			setAlive(player, false);
 			
 			// send this player to everyone else's scoreboards, because they're now invisible, and won't show otherwise
 			for ( Player online : plugin.getOnlinePlayers() )
 				if ( online != player && !online.canSee(player) )
 					sendForScoreboard(online, player, true);
 		}
 		else
 		{
 			if ( info.isAlive() )
 			{
 				setAlive(player, true);
 				
 				if ( !plugin.getGameMode().teamAllocationIsSecret() )
 					colorPlayerName(player, info.getTeam() == 1 ? ChatColor.RED : ChatColor.BLUE);
 			}
 			else
 			{
 				setAlive(player, false); // game mode made them a spectator for some reason
 				player.sendMessage("You are now a spectator. You can fly, but can't be seen or interact. Type " + ChatColor.YELLOW + "/spec" + ChatColor.RESET + " to list available commands.");
 			}
 		}
 		if ( isNewPlayer );
 			plugin.getGameMode().sendGameModeHelpMessage(player);
 			
 		if ( player.getInventory().contains(Material.COMPASS) )
 		{// does this need a null check on the target?
 			player.setCompassTarget(getCompassTarget(player));
 		}
 	}
 	
 	// player either died, or disconnected and didn't rejoin in the required time
 	public void playerKilled(final OfflinePlayer player)
 	{
 		if ( !plugin.getGameState().usesGameWorlds )
 			return;
 		
 		Info info = playerInfo.get(player.getName());
 		info.setAlive(false);
 		
 		if ( plugin.getOnlinePlayers().size() == 0 )
 		{// no one still playing, so end the game
 			plugin.forcedGameEnd = true;
 			plugin.getGameMode().gameFinished();
 			plugin.endGame(null);
 			return;
 		}
 		
 		plugin.getServer().getScheduler().scheduleAsyncDelayedTask(plugin, new Runnable() {
 			@Override
 			public void run() {
 				plugin.getGameMode().playerKilledOrQuit(player);				
 			}
 		}, 15); // game mode doesn't respond for short period, so as to be able to account for other deaths happening simultaneously (e.g. caused by the same explosion)
 		
 		Player online = player.isOnline() ? (Player)player : null;
 		if ( online != null )
 		{
 			if ( plugin.getGameMode().isAllowedToRespawn(online) )
 			{
 				setAlive(online, true);
 				return;
 			}
 			
 			if ( !plugin.getGameMode().teamAllocationIsSecret() )
 				plugin.playerManager.clearPlayerNameColor(online);
 		}
 		
 		if ( Settings.banOnDeath )
 		{
 			player.setBanned(true);
 			if ( online != null )
 				online.kickPlayer("You died, and are now banned until the end of the game");
 		}
 	}
 	
 	public Location getCompassTarget(Player player)
 	{
 		Location target = plugin.getGameMode().getCompassTarget(player);
 		if ( target == null )
 			return player.getWorld().getSpawnLocation();
 		else
 			return target;
 	}
 	
 	public Info getInfo(String player)
 	{
 		return playerInfo.get(player);
 	}
 	
 	public boolean isSpectator(String player)
 	{
 		Info info = playerInfo.get(player);
 		return info == null || !info.isAlive();
 	}
 
 	public boolean isAlive(String player)
 	{
 		Info info = playerInfo.get(player);
 		return info != null && info.isAlive();
 	}
 
 	public int getTeam(String player)
 	{
 		Info info = playerInfo.get(player);
 		return info == null ? 0 : info.getTeam();
 	}
 
 	public void setAlive(Player player, boolean bAlive)
 	{
 		boolean wasAlive;
 		Info info = playerInfo.get(player.getName());
 		if ( info == null )
 		{
 			info = new Info(bAlive);
 			playerInfo.put(player.getName(), info);
 			wasAlive = true;
 		}
 		else
 			wasAlive = info.isAlive();
 
 		Inventory inv = player.getInventory(); 
 		inv.clear();
 		
 		info.setAlive(bAlive);
 		if ( bAlive )
 		{
 			// you shouldn't stop being able to fly in creative mode, cos you're (hopefully) only there for testing
 			if ( player.getGameMode() != GameMode.CREATIVE )
 			{
 				player.setFlying(false);
 				player.setAllowFlight(false);
 			}
 			makePlayerVisibleToAll(player);
 			
 			if ( !wasAlive )
 				player.sendMessage("You are no longer a spectator.");
 		}
 		else if ( !player.isDead() )
 		{
 			player.setAllowFlight(true);
 			player.setFlying(true);
 			inv.addItem(new ItemStack(Settings.teleportModeItem, 1));
 			inv.addItem(new ItemStack(Settings.followModeItem, 1));
 			makePlayerInvisibleToAll(player);
 			
 			player.sendMessage("You are now a spectator. You can fly, but can't be seen or interact. Clicking has different effects depending on the selected item. Type " + ChatColor.YELLOW + "/spec" + ChatColor.RESET + " to list available commands.");
 		}
 	}
 	
 	public void hidePlayer(Player fromMe, Player hideMe)
 	{
 		fromMe.hidePlayer(hideMe);
 		sendForScoreboard(fromMe, hideMe, true); // hiding will take them out of the scoreboard, so put them back in again
 	}
 	
 	public void sendForScoreboard(Player viewer, Player other, boolean show)
 	{
 		((CraftPlayer)viewer).getHandle().netServerHandler.sendPacket(new Packet201PlayerInfo(other.getPlayerListName(), show, show ? ((CraftPlayer)other).getHandle().ping : 9999));
 	}
 	
 	public void makePlayerInvisibleToAll(Player player)
 	{
 		for(Player p : plugin.getOnlinePlayers())
 			if (p != player && p.canSee(player))
 				hidePlayer(p, player);
 	}
 	
 	public void makePlayerVisibleToAll(Player player)
 	{
 		for(Player p : plugin.getOnlinePlayers())
 			if (p != player && !p.canSee(player))
 				p.showPlayer(player);
 	}
 
 	public void resetPlayer(Player player)
 	{
 		if ( !player.isDead() )
 		{
 			player.setTotalExperience(0);
 			player.setHealth(player.getMaxHealth());
 			player.setFoodLevel(20);
 			player.setSaturation(20);
 			player.setExhaustion(0);
 			player.setFireTicks(0);
 			
 			PlayerInventory inv = player.getInventory();
 			inv.clear();
 			inv.setHelmet(null);
 			inv.setChestplate(null);
 			inv.setLeggings(null);
 			inv.setBoots(null);
 			
 			for (PotionEffectType p : PotionEffectType.values())
 			     if (p != null && player.hasPotionEffect(p))
 			          player.removePotionEffect(p);
 			
 			player.closeInventory(); // this stops them from keeping items they had in (e.g.) a crafting table
 			
 			if ( isAlive(player.getName()) ) // if any starting items are configured, give them if the player is alive
 				for ( Material material : Settings.startingItems )
 					inv.addItem(new ItemStack(material));
 		}
 		
 		clearPlayerNameColor(player);
 	}
 	
     public void forceRespawn(final Player player)
     {
     	plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
             @Override
             public void run() {
                 Packet205ClientCommand packet = new Packet205ClientCommand();
                 packet.a = 1;
                 ((CraftPlayer) player).getHandle().netServerHandler.a(packet);
             }
         }, 1);
 	}
 	
 	public String getFollowTarget(Player player)
 	{
 		Info info = playerInfo.get(player.getName());
 		return info == null ? null : info.target;
 	}
 	
 	public void setFollowTarget(Player player, String target)
 	{
 		Info info = playerInfo.get(player.getName());
 		
 		if ( info != null )
 			info.target = target;
 	}
 	
 	private final double maxFollowSpectateRangeSq = 40 * 40, maxAcceptableOffsetDot = 0.65, farEnoughSpectateRangeSq = 35 * 35;
 	private final int maxSpectatePositionAttempts = 5, idealFollowSpectateRange = 20;
 	
 	public void checkFollowTarget(Player player, String targetName)
	{
		Player target = targetName == null ? null : plugin.getServer().getPlayerExact(targetName);
		if ( targetName == null || target == null || !isAlive(targetName) || !target.isOnline() || !plugin.isGameWorld(target.getWorld()) )
 		{
 			targetName = getNearestFollowTarget(player);
 			setFollowTarget(player, targetName);
 			if ( targetName == null )
 				return; // if there isn't a valid follow target, don't let it try to move them to it
 
 			target = plugin.getServer().getPlayerExact(targetName);
 			if ( !isAlive(targetName) || target == null || !target.isOnline() )
 			{// something went wrong with the default follow target, so just clear it
 				setFollowTarget(player, null);
 				return;
 			}
 		}
 		
 		if ( !canSee(player,  target, maxFollowSpectateRangeSq) )
 			moveToSee(player, target);
 	}
 	
 	public boolean canSee(Player looker, Player target, double maxDistanceSq)
 	{
 		Location specLoc = looker.getEyeLocation();
 		Location targetLoc = target.getEyeLocation();
 		
 		// check they're in the same world
 		if ( specLoc.getWorld() != targetLoc.getWorld() )
 			return false;
 		
 		// then check the distance is appropriate
 		double targetDistSqr = specLoc.distanceSquared(targetLoc); 
 		if ( targetDistSqr > maxDistanceSq )
 			return false;
 		
 		// check if they're facing the right way
 		Vector specDir = specLoc.getDirection().normalize();
 		Vector dirToTarget = targetLoc.subtract(specLoc).toVector().normalize();
 		if ( specDir.dot(dirToTarget) < maxAcceptableOffsetDot )
 			return false;
 		
 		// then do a ray trace to see if there's anything in the way
         Iterator<Block> itr = new BlockIterator(specLoc.getWorld(), specLoc.toVector(), dirToTarget, 0, (int)Math.sqrt(targetDistSqr));
         while (itr.hasNext())
         {
             Block block = itr.next();
             if ( block != null && !block.isEmpty() )
             	return false;
         }
         
         return true;
 	}
 	
 	public void moveToSee(Player player, Player target)
 	{
 		if ( target == null || !target.isOnline() )
 			return;
 
 		Location targetLoc = target.getEyeLocation();
 		
 		Location bestLoc = targetLoc;
 		double bestDistSq = 0;
 		
 		// try a few times to move away in a random direction, see if we can make it up to idealFollowSpectateRange
 		for ( int i=0; i<maxSpectatePositionAttempts; i++ )
 		{
 			// get a mostly-horizontal direction
 			Vector dir = new Vector(random.nextDouble()-0.5, random.nextDouble() * 0.35 - 0.1, random.nextDouble()-0.5).normalize();
 			if ( dir.getY() > 0.25 )
 			{
 				dir.setY(0.25);
 				dir = dir.normalize();
 			}
 			else if ( dir.getY() < -0.1 )
 			{
 				dir.setY(-0.1);
 				dir = dir.normalize();
 			}
 			
 			Location pos = findSpaceForPlayer(player, targetLoc, dir, idealFollowSpectateRange, false, true);
 			if ( pos == null )
 				pos = targetLoc;
 			
 			double distSq = pos.distanceSquared(targetLoc); 
 			if ( distSq > bestDistSq )
 			{
 				bestLoc = pos;
 				bestDistSq = distSq; 
 				
 				if ( distSq > farEnoughSpectateRangeSq )
 					break; // close enough to the max distance, just use this
 			}
 		}
 		
 		// work out the yaw
 		double xDif = targetLoc.getX() - bestLoc.getX();
 		double zDif = targetLoc.getZ() - bestLoc.getZ();
 		
 		if ( xDif == 0 )
 		{
 			if ( zDif >= 0 )
 				bestLoc.setYaw(270);
 			else
 				bestLoc.setYaw(90);
 		}
 		else if ( xDif > 0 )
 		{
 			if ( zDif >= 0)
 				bestLoc.setYaw(270f + (float)Math.toDegrees(Math.atan(zDif / xDif)));
 			else
 				bestLoc.setYaw(180f + (float)Math.toDegrees(Math.atan(xDif / -zDif)));
 		}
 		else
 		{
 			if ( zDif >= 0)
 				bestLoc.setYaw((float)(Math.toDegrees(Math.atan(-xDif / zDif))));
 			else
 				bestLoc.setYaw(90f + (float)Math.toDegrees(Math.atan(zDif / xDif)));
 		}
 		
 		// work out the pitch
 		double horizDist = Math.sqrt(xDif * xDif + zDif * zDif);
 		double yDif = targetLoc.getY() - bestLoc.getY();
 		if ( horizDist == 0 )
 			bestLoc.setPitch(0);
 		else if ( yDif >= 0 )
 			bestLoc.setPitch(-(float)Math.toDegrees(Math.atan(yDif / horizDist)));
 		else
 			bestLoc.setPitch((float)Math.toDegrees(Math.atan(-yDif / horizDist)));
 		
 		// as we're dealing in eye position thus far, reduce the Y to get the "feet position"
 		bestLoc.setY(bestLoc.getY() - player.getEyeHeight());
 		
 		// set them as flying so they don't fall from this position, then do the teleport
 		player.setFlying(true);
 		player.teleport(bestLoc);
 	}
 	
 	public String getNearestFollowTarget(Player lookFor)
 	{
 		double nearestDistSq = Double.MAX_VALUE;
 		String nearestName = null, firstName = null;
 		
 		for ( Map.Entry<String, Info> entry : getPlayerInfo() )
 		{
 			if ( !entry.getValue().isAlive() )
 				continue;
 			
 			Player player = plugin.getServer().getPlayerExact(entry.getKey());
 			if ( player != null && player.isOnline() )
 			{
 				if ( firstName == null && plugin.isGameWorld(player.getWorld()) )
 					firstName = player.getName();
 				
 				if ( lookFor.getWorld() == player.getWorld() )
 				{
 					double testDistSq = player.getLocation().distanceSquared(lookFor.getLocation());
 					if ( testDistSq < nearestDistSq )
 					{
 						nearestName = player.getName();
 						nearestDistSq = testDistSq;
 					}
 				}
 			}
 		}
 		
 		if ( nearestName != null )
 			return nearestName;
 		
 		return firstName;
 	}
 	
 	public String getNextFollowTarget(Player lookFor, String currentTargetName, boolean forwards)
 	{
 		Set<Map.Entry<String, Info>> players = forwards ? playerInfo.entrySet() : playerInfo.descendingMap().entrySet();
 		boolean useNextTarget = false;
 		
 		for ( Map.Entry<String, Info> entry : players )
 		{
 			if ( !useNextTarget && entry.getKey().equals(currentTargetName) )
 			{
 				useNextTarget = true;
 				continue;
 			}
 			if ( !entry.getValue().isAlive() || !useNextTarget )
 				continue;
 			
 			Player player = plugin.getServer().getPlayerExact(entry.getKey());
 			if ( player != null && player.isOnline() && plugin.isGameWorld(player.getWorld()) )
 				return entry.getKey();
 		}
 				
 		// couldn't find one, or ran off the end of the list, so start again at the beginning, and use the first valid one
 		for ( Map.Entry<String, Info> entry : players )
 		{
 			if ( !entry.getValue().isAlive() )
 				continue;
 			
 			Player player = plugin.getServer().getPlayerExact(entry.getKey());
 			if ( player != null && player.isOnline() && plugin.isGameWorld(player.getWorld()) )
 				return entry.getKey();
 		}
 		
 		// there really just wasn't one
 		return null;
 	}
 
 	private Location findSpaceForPlayer(Player player, Location targetLoc, Vector dir, int maxDist, boolean seekClosest, boolean abortOnAnySolid)
 	{
 		Location bestPos = null;
 
 		Iterator<Block> itr = new BlockIterator(targetLoc.getWorld(), targetLoc.toVector(), dir, 0, maxDist);
 		while (itr.hasNext())
 		{
 			Block block = itr.next();
 			if ( block == null || block.getLocation().getBlockY() <= 0 || block.getLocation().getBlockY() >= block.getWorld().getMaxHeight() )
 				break; // don't go out the world
 			
 			if ( !block.isEmpty() && !block.isLiquid() )
 				if ( abortOnAnySolid )
 					break;
 				else
 					continue;
 			
 			Block blockBelow = targetLoc.getWorld().getBlockAt(block.getLocation().getBlockX(), block.getLocation().getBlockY()-1, block.getLocation().getBlockZ());
 			if ( blockBelow.isEmpty() || blockBelow.isLiquid() )
 			{
 				bestPos = blockBelow.getLocation().add(new Vector(0.5, player.getEyeHeight()-1, 0.5));
 				if ( seekClosest )
 					return bestPos;
 			}
 		}
 		
 		return bestPos;
 	}
 	
 	private final int maxSpecTeleportDist = 64, maxSpecTeleportPenetrationDist = 32;
 	private final HashSet<Byte> transparentBlocks = new HashSet<Byte>();
 	
 	// teleport forward, to get around doors, walls, etc. that spectators can't dig through
 	public void doSpectatorTeleport(Player player, boolean goThroughTarget)
 	{
 		Location lookAtPos = player.getTargetBlock(transparentBlocks, maxSpecTeleportDist).getLocation();
 		
 		Vector facingDir = player.getLocation().getDirection().normalize();
 		Location traceStartPos = goThroughTarget ? lookAtPos.add(facingDir) : lookAtPos;
 		Vector traceDir = goThroughTarget ? facingDir : facingDir.multiply(-1.0);
 	
 		Location targetPos = findSpaceForPlayer(player, traceStartPos, traceDir, goThroughTarget ? maxSpecTeleportPenetrationDist : maxSpecTeleportDist, true, false);
 
 		player.setFlying(true);
 		if ( targetPos != null )
 		{
 			targetPos.setPitch(player.getLocation().getPitch());
 			targetPos.setYaw(player.getLocation().getYaw());
 			player.teleport(targetPos);
 		}
 		else
 			player.sendMessage("No space to teleport into!");
 	}
 
 	public void teleport(Player player, Location loc)
 	{
 		if ( player.isDead() )
 			forceRespawn(player); // stop players getting stuck at the "you are dead" screen, unable to do anything except disconnect
 		player.setVelocity(new Vector(0,0,0));
 		player.teleport(loc);
 	}
 
 	public boolean isInventoryEmpty(PlayerInventory inv)
 	{
 		for (ItemStack is : inv.getArmorContents())
             if (is != null && is.getAmount() > 0)
                 return false;
         for (ItemStack is : inv.getContents())
             if (is != null && is.getAmount() > 0)
                 return false;
         return true;
 	}
 }
