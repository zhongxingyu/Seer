 package com.ftwinston.Killer;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Random;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.GameMode;
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.OfflinePlayer;
 import org.bukkit.World;
 import org.bukkit.block.Block;
 import org.bukkit.enchantments.Enchantment;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.inventory.PlayerInventory;
 import org.bukkit.util.BlockIterator;
 import org.bukkit.util.Vector;
 
 public class PlayerManager
 {
 	public static PlayerManager instance;
 	private Killer plugin;
 	private Random random;
 	public PlayerManager(Killer _plugin)
 	{
 		this.plugin = _plugin;
 		instance = this;
 		random = new Random();
 		
     	if ( plugin.autoAssignKiller )
     	{
 			plugin.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable() {
     			long lastRun = 0;
     			public void run()
     			{
     				long time = plugin.getServer().getWorlds().get(0).getTime();
     				
     				if ( time < lastRun && !hasEnoughKillers() ) // time of day has gone backwards! Must be a new day! See if we need to add a killer
 						assignKiller(plugin.informEveryoneOfReassignedKillers || killers.size() == 0); // don't inform people of any killer being added apart from the first one, unless the config is set
 
 					lastRun = time;
     			}
     		}, 600L, 100L); // initial wait: 30s, then check every 5s (still won't try to assign unless it detects a new day starting)
     	}
 	}
 	
 	private List<String> alive = new ArrayList<String>();
 	private List<String> killers = new ArrayList<String>();
 	private Map<String, String> spectators = new LinkedHashMap<String, String>();
 	
 	public boolean hasKillerAssigned() { return killers.size() > 0; }
 	public boolean hasEnoughKillers()
 	{
 		// if we don't have enough players for a game, we don't want to assign a killer
 		if ( alive.size() < plugin.absMinPlayers )
 			return true;
 		
 		// if we're not set to auto-reassign the killer once one has been assigned at all, even if they're no longer alive / connected, don't do so
 		if ( !plugin.autoReassignKiller && killers.size() > 0 )
 			return true;
 		
 		int numAliveKillers = 0;
 		for ( String name : alive )
 			if ( isKiller(name) )
 				numAliveKillers ++;
 		
 		// for now, one living killer at a time is plenty. But this should be easy to extend later.
 		return numAliveKillers > 0;
 	}
 	
 	public void reset(boolean resetInventories)
 	{
 		alive.clear();
 		for ( Player player : plugin.getServer().getOnlinePlayers() )
 		{
 			resetPlayer(player, resetInventories);
 			setAlive(player,true);
 		}
 		
 		// don't do this til after, so addAlive can remove spectator effects if needed
 		spectators.clear();
 		
 		// inform all killers that they're not any more, just to be clear
 		for ( String killerName : killers )
 		{
 			Player killerPlayer = Bukkit.getServer().getPlayerExact(killerName);
 			if ( killerPlayer != null && killerPlayer.isOnline() )
 				killerPlayer.sendMessage(ChatColor.YELLOW + "You are no longer " + (killers.size() == 1 ? "the" : "a") + " killer.");
 		}
 		killers.clear();
 		
 		if ( plugin.banOnDeath )
 			for ( OfflinePlayer player : plugin.getServer().getBannedPlayers() )
 				player.setBanned(false);
 	}
 	
 	public boolean assignKiller(boolean informNonKillers)
 	{
 		Player[] players = plugin.getServer().getOnlinePlayers();
 		if ( players.length < plugin.absMinPlayers )
 		{
 			plugin.getServer().broadcastMessage("Insufficient players to assign a killer. A minimum of 3 players are required.");
 			return false;
 		}
 		
 		if ( informNonKillers )
 			plugin.getServer().broadcastMessage("A killer has been randomly assigned - nobody but the killer knows who it is.");
 		
 		int availablePlayers = 0;
 		for ( String name : alive )
 		{
 			if ( isKiller(name) )
 				continue;
 			
 			Player player = plugin.getServer().getPlayerExact(name);
 			if ( player != null && player.isOnline() )
 				availablePlayers ++;
 		}
 		
 		if ( availablePlayers == 0 )
 			return false;
 		
 		int randomIndex = random.nextInt(availablePlayers);
 		Player killer = null;
 		
 		int num = 0;
 		for ( Player player : players )
 		{
 			if ( isKiller(player.getName()) || isSpectator(player.getName()) )
 				continue;
 		
 			if ( num == randomIndex )
 			{
 				addKiller(player);
 				killer = player;
 				String message = ChatColor.RED + "You are ";
 				message += killers.size() > 1 ? "now a" : "the";
 				message += " killer!";
 				
 				if ( !informNonKillers )
 					message += ChatColor.WHITE + " No one else has been told a new killer was assigned.";
 					
 				player.sendMessage(message);
 			}
 			else if ( informNonKillers )
 				player.sendMessage(ChatColor.YELLOW + "You are not the killer.");
 				
 			num++;
 		}
 		
 		giveKillerItems(killer, availablePlayers - 1);
 		return true;
 	}
 
 	private void giveKillerItems(Player player, int numFriendlies)
 	{
 		PlayerInventory inv = player.getInventory();
 		
 		if ( numFriendlies >= 2 )
 			inv.addItem(new ItemStack(Material.STONE, 6));
 		else
 			return;
 		
 		if ( numFriendlies >= 3 )
 			inv.addItem(new ItemStack(Material.COOKED_BEEF, 1), new ItemStack(Material.IRON_INGOT, 1), new ItemStack(Material.REDSTONE, 2));
 		else
 			return;
 		
 		if ( numFriendlies >= 4 )
 			inv.addItem(new ItemStack(Material.COOKED_BEEF, 1), new ItemStack(Material.IRON_INGOT, 2), new ItemStack(Material.SULPHUR, 1));
 		else
 			return;
 		
 		if ( numFriendlies >= 5 )
 			inv.addItem(new ItemStack(Material.COOKED_BEEF, 1), new ItemStack(Material.IRON_INGOT, 1), new ItemStack(Material.REDSTONE, 2), new ItemStack(Material.ARROW, 3));
 		else
 			return;
 		
 		if ( numFriendlies >= 6 )
 			inv.addItem(new ItemStack(Material.MONSTER_EGG, 1, (short)50), new ItemStack(Material.REDSTONE, 2), new ItemStack(Material.SULPHUR, 1), new ItemStack(Material.ARROW, 2));
 		else
 			return;
 		
 		if ( numFriendlies >= 7 )
 		{
 			inv.addItem(new ItemStack(Material.COOKED_BEEF, 1), new ItemStack(Material.REDSTONE, 2), new ItemStack(Material.SULPHUR, 1), new ItemStack(Material.ARROW, 2));
 			
 			if ( numFriendlies < 11 )
 				inv.addItem(new ItemStack(Material.IRON_PICKAXE, 1)); // at 11 friendlies, they'll get a diamond pick instead
 		}
 		else
 			return;
 		
 		if ( numFriendlies >= 8 )
 			inv.addItem(new ItemStack(Material.COOKED_BEEF, 1), new ItemStack(Material.IRON_INGOT, 2), new ItemStack(Material.BOW, 1), new ItemStack(Material.ARROW, 3));
 		else
 			return;
 		
 		if ( numFriendlies >= 9 )
 			inv.addItem(new ItemStack(Material.COOKED_BEEF, 1), new ItemStack(Material.MONSTER_EGGS, 4, (short)0), new ItemStack(Material.STONE, 2));
 		else
 			return;
 		
 		if ( numFriendlies >= 10 )
 			inv.addItem(new ItemStack(Material.IRON_INGOT, 2), new ItemStack(Material.MONSTER_EGG, 1, (short)50), new ItemStack(Material.ARROW, 2));
 		else
 			return;
 		
 		if ( numFriendlies >= 11 )
 		{
 			inv.addItem(new ItemStack(Material.COOKED_BEEF, 1), new ItemStack(Material.SULPHUR, 1));
 			
 			if ( numFriendlies < 18 )
 				inv.addItem(new ItemStack(Material.DIAMOND_PICKAXE, 1)); // at 18 friendlies, they get an enchanted version
 		}
 		else
 			return;
 		
 		if ( numFriendlies >= 12 )
 			inv.addItem(new ItemStack(Material.COOKED_BEEF, 1), new ItemStack(Material.DIAMOND, 2), new ItemStack(Material.REDSTONE, 2), new ItemStack(Material.STONE, 2), new ItemStack(Material.SULPHUR, 1));
 		else
 			return;
 		
 		if ( numFriendlies >= 13 )
 			inv.addItem(new ItemStack(Material.IRON_INGOT, 2), new ItemStack(Material.MONSTER_EGGS, 2, (short)0), new ItemStack(Material.ARROW, 2));
 		else
 			return;
 		
 		if ( numFriendlies >= 14 )
 			inv.addItem(new ItemStack(Material.COOKED_BEEF, 1), new ItemStack(Material.DIAMOND, 2), new ItemStack(Material.MONSTER_EGGS, 1, (short)0), new ItemStack(Material.REDSTONE, 2), new ItemStack(Material.STONE, 2));
 		else
 			return;
 		
 		if ( numFriendlies >= 15 )
 			inv.addItem(new ItemStack(Material.COOKED_BEEF, 1), new ItemStack(Material.DIAMOND, 2), new ItemStack(Material.MONSTER_EGGS, 1, (short)0), new ItemStack(Material.PISTON_STICKY_BASE, 3));
 		else
 			return;
 		
 		if ( numFriendlies >= 16 )
 			inv.addItem(new ItemStack(Material.COOKED_BEEF, 1), new ItemStack(Material.DIAMOND, 1), new ItemStack(Material.SULPHUR, 5));
 		else
 			return;
 		
 		if ( numFriendlies >= 17 )
 			inv.addItem(new ItemStack(Material.COOKED_BEEF, 1), new ItemStack(Material.DIAMOND, 1), new ItemStack(Material.MONSTER_EGG, 1, (short)50), new ItemStack(Material.ARROW, 2));
 		else
 			return;
 		
 		if ( numFriendlies >= 18 )
 		{
 			inv.addItem(new ItemStack(Material.COOKED_BEEF, 1), new ItemStack(Material.DIAMOND, 2));
 			if ( numFriendlies == 18 )
 			{
 				ItemStack stack = new ItemStack(Material.DIAMOND_PICKAXE, 1);
 				stack.addEnchantment(Enchantment.DIG_SPEED, 1);
 				inv.addItem(stack);
 			}
 		}
 		else
 			return;
 		
 		if ( numFriendlies >= 19 )
 		{
 			inv.addItem(new ItemStack(Material.COOKED_BEEF, 1), new ItemStack(Material.DIAMOND, 2));
 			if ( numFriendlies == 19 )
 			{
 				ItemStack stack = new ItemStack(Material.DIAMOND_PICKAXE, 1);
 				stack.addEnchantment(Enchantment.DIG_SPEED, 2);
 				inv.addItem(stack);
 			}
 		}
 		else
 			return;
 		
 		if ( numFriendlies >= 20 )
 		{
 			inv.addItem(new ItemStack(Material.COOKED_BEEF, 1), new ItemStack(Material.DIAMOND, 2));
 		
 			ItemStack stack = new ItemStack(Material.DIAMOND_PICKAXE, 1);
 			stack.addEnchantment(Enchantment.DIG_SPEED, 3);
 			inv.addItem(stack);
 		}
 		else
 			return;	
 	}
 	
 	public void playerJoined(Player player)
 	{		
 		for(String spec:spectators.keySet())
 			if(spec != player.getName())
 			{
 				Player other = plugin.getServer().getPlayerExact(spec);
 				if ( other != null && other.isOnline() )
 					player.hidePlayer(other);
 			}
 		
 		if ( isSpectator(player.getName()) )
 		{
 			player.sendMessage("Welcome back. You are now a spectator. You can fly, but can't be seen or interact. Type " + ChatColor.YELLOW + "/spec" + ChatColor.RESET + " to list available commands.");
 			setAlive(player,false);
 		}
 		
 		else if ( isKiller(player.getName()) ) // inform them that they're still a killer
 			player.sendMessage("Welcome back. " + ChatColor.RED + "You are still " + (killers.size() > 1 ? "a" : "the" ) + " killer!"); 
 		
 		else if ( !isAlive(player.getName())) // this is a new player, tell them the rules & state of the game
 		{
 			String message = "Welcome to Killer Minecraft! One player ";
 			message += hasKillerAssigned() ? "has been" : "will soon be";
 			message += " assigned as the killer, and must kill the rest. To win, the other players must bring a ";
 			
 			message += plugin.tidyItemName(plugin.winningItems[0]);
 			
 			if ( plugin.winningItems.length > 1 )
 			{
 				for ( int i=1; i<plugin.winningItems.length-1; i++)
 					message += ", a " + plugin.tidyItemName(plugin.winningItems[i]);
 				
 				message += " or a " + plugin.tidyItemName(plugin.winningItems[plugin.winningItems.length-1]);
 			}
 			
 			message += " to the plinth near the spawn.";
 			player.sendMessage(message);
 			
 			if ( hasKillerAssigned() && plugin.lateJoinersStartAsSpectator )
 				setAlive(player,false);
 			else
 				setAlive(player,true);
 		}
 		
 		else
 			player.sendMessage("Welcome back. You are not the killer, and you're still alive.");
 		
     	if ( plugin.restartDayWhenFirstPlayerJoins && plugin.getServer().getOnlinePlayers().length == 1 )
 			plugin.getServer().getWorlds().get(0).setTime(0);
 	}
 	
 	// player either died, or disconnected and didn't rejoin in the required time
 	public void playerKilled(String playerName)
 	{
 		Player player = plugin.getServer().getPlayerExact(playerName);
 		if ( player != null && player.isOnline() )
 		{
 			setAlive(player, false);
 		}
 		else // player disconnected ... move them to spectator in our records, in case they reconnect
 		{
 			if(alive.contains(playerName) )
 				alive.remove(playerName);
 			if(!spectators.containsKey(playerName))
 				spectators.put(playerName, null);
 		}
 		
 		if ( plugin.banOnDeath )
 		{
 			player.setBanned(true);
 			player.kickPlayer("You died, and are now banned until the end of the game");
 		}
 		
 		// if there's no one alive at all, game was drawn
 		if (alive.size() == 0 )
 			gameFinished(false, false, null, null);
 		else
 		{// check for victory ... if there's no one alive that isn't a killer, the killer(s) won
 			for ( String survivor : alive )
 				if ( !isKiller(survivor) )
 					return;
 			
 			gameFinished(true, false, null, null);
 		}
 	}
 	
 	public void gameFinished(boolean killerWon, boolean friendliesWon, String winningPlayerName, String winningItem)
 	{
 		String message;
 		if ( killerWon )
 		{
 			message = "All friendly players have been killed, the killer";
 			
 			if ( killers.size() > 1 )
 			{
 				message += "s win!";
 
 				if ( winningPlayerName != null )
 					message += "\nWinning kill by " + winningPlayerName + ".";
 			}
 			else
 				message += " wins!";
 		}
 		else if ( friendliesWon )
 			message = (winningPlayerName == null ? "The players" : winningPlayerName) + " brought " + (winningItem == null ? "an item" : "a " + winningItem) + " to the plinth - the friendlies win!";
 		else
 			message = "No players survived, game drawn!";
 		
 		plugin.getServer().broadcastMessage(ChatColor.YELLOW + message);
 		if ( plugin.autoReveal )
 			clearKillers();
 
 		if ( friendliesWon || plugin.autoRecreateWorld || plugin.voteManager.isInVote() || plugin.getServer().getOnlinePlayers().length == 0 )
 		{	// schedule a game restart in 10 secs, with a new world
 			plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
 	
 				@Override
 				public void run()
 				{
 	    			plugin.restartGame(false, true);					
 				}
 	    	}, 200L);
 		}
 		else
 		{// start a vote that's been set up to call restartGame with true / false parameter 
 			Runnable yesResult = new Runnable() {
 				public void run()
 				{
 					plugin.restartGame(true, false);
 				}
 			};
 			
 			Runnable noResult = new Runnable() {
 				public void run()
 				{
 					plugin.restartGame(false, true);
 				}
 			};
 			
 			plugin.voteManager.startVote("Start next round with the same world & items?", null, yesResult, noResult, noResult);
 		}
 	}
 	
 	public void clearKillers()
 	{
 		String message;
 		
 		if ( hasKillerAssigned() )
 		{
 			message = ChatColor.RED + "Revealed: ";
 			if ( killers.size() == 1 )
 				message += killers.get(0) + " was the killer!";
 			else
 			{
 				message += "The killers were ";
 				message += killers.get(0);
 				
 				for ( int i=1; i<killers.size(); i++ )
 				{
 					message += i == killers.size()-1 ? " and " : ", ";
 					message += killers.get(i);
 				}
 				
 				message += "!";
 			}
 			
 			killers.clear();
 		}
 		else
 			message = "No killers are currently assigned!";
 		
 		plugin.getServer().broadcastMessage(message);
 	}
 	
 	public boolean isSpectator(String player)
 	{
 		return spectators.containsKey(player);
 	}
 
 	public boolean isAlive(String player)
 	{
 		return alive.contains(player);
 	}
 
 	public boolean isKiller(String player)
 	{
 		return killers.contains(player);
 	}
 
 	public void setAlive(Player player, boolean bAlive)
 	{
 		if ( bAlive )
 		{
 			// you shouldn't stop being able to fly in creative mode, cos you're (hopefully) only there for testing
 			if ( player.getGameMode() != GameMode.CREATIVE )
 			{
 				player.setFlying(false);
 				player.setAllowFlight(false);
 			}
 			
 			makePlayerVisibleToAll(player);
 			
 			if(!alive.contains(player.getName()))
 				alive.add(player.getName());
 			if(spectators.containsKey(player.getName()))
 				spectators.remove(player.getName());
 		}
 		else
 		{
 			player.setAllowFlight(true);
 			player.getInventory().clear();
 			makePlayerInvisibleToAll(player);
 			
 			if(alive.contains(player.getName()))
 				alive.remove(player.getName());
 			if(!spectators.containsKey(player.getName()))
 			{
 				spectators.put(player.getName(), null);
 				player.sendMessage("You are now a spectator. You can fly, but can't be seen or interact. Type " + ChatColor.YELLOW + "/spec" + ChatColor.RESET + " to list available commands.");
 			}
 		}
 	}
 	
 	private void addKiller(Player player)
 	{
 		if(!killers.contains(player.getName()))
 			killers.add(player.getName());
 	}
 	
 	private void makePlayerInvisibleToAll(Player player)
 	{
 		for(Player p : plugin.getServer().getOnlinePlayers())
 			p.hidePlayer(player);
 	}
 	
 	private void makePlayerVisibleToAll(Player player)
 	{
 		for(Player p :  plugin.getServer().getOnlinePlayers())
 			p.showPlayer(player);
 	}
 
 	public void resetPlayer(Player player, boolean resetInventory)
 	{
 		player.setTotalExperience(0);
 		player.setHealth(player.getMaxHealth());
 		player.setFoodLevel(20);
 		player.setSaturation(20);
 		player.setExhaustion(0);
 		player.setFireTicks(0);
 		
 		if ( resetInventory )
 		{
 			PlayerInventory inv = player.getInventory();
 			inv.clear();
 			inv.setHelmet(null);
 			inv.setChestplate(null);
 			inv.setLeggings(null);
 			inv.setBoots(null);		
 		}
 	}
 	
 	public void putPlayerInWorld(Player player, World world, boolean checkSpawn)
 	{		
 		// check spawn location is clear and is on the ground!
 		// update it if its not!
 		Location spawn = world.getSpawnLocation();
 		
 		if ( checkSpawn )
 		{
 			if ( spawn.getBlock().isEmpty() )
 			{	// while the block below spawn is empty, move down one
 				Block b = world.getBlockAt(spawn.getBlockX(), spawn.getBlockY() - 1, spawn.getBlockZ()); 
 				while ( b.isEmpty() && !b.isLiquid() )
 				{
 					spawn.setY(spawn.getY()-1);
 					b = world.getBlockAt(spawn.getBlockX(), spawn.getBlockY() - 1, spawn.getBlockZ());
 				}
 			}
 			else
 			{	// keep moving up til we have two empty blocks
 				do
 				{
 					do
 					{
 						spawn.setY(spawn.getY()+1);
 					}
 					while ( !spawn.getBlock().isEmpty() );
 				
 					// that's one empty, see if there's another (or if we're at the tom of the world)
 					if ( spawn.getBlockY() >= world.getMaxHeight() || world.getBlockAt(spawn.getBlockX(), spawn.getBlockY() + 1, spawn.getBlockZ()).isEmpty() )
 						break;
 				} while (true);
 				
 				world.setSpawnLocation(spawn.getBlockX(), spawn.getBlockY(), spawn.getBlockZ());
 			}
 		}
 		
 		player.teleport(spawn);
 	}
 
 	public Location getNearestPlayerTo(Player player)
 	{
 		Location nearest = null;
 		double nearestDistSq = Double.MAX_VALUE;
 		World playerWorld = player.getWorld();
 		for ( Player other : plugin.getServer().getOnlinePlayers() )
 		{
 			if ( other == player || other.getWorld() != playerWorld || !isAlive(other.getName()) )
 				continue;
 			
 			double distSq = other.getLocation().distanceSquared(nearest);
 			if ( nearest == null || distSq < nearestDistSq )
 			{
 				nearestDistSq = distSq;
 				nearest = other.getLocation();
 			}
 		}
 		
 		// if there's no player to point at, point in a random direction
 		if ( nearest == null )
 		{
 			nearest = player.getLocation();
 			nearest.setX(nearest.getX() + random.nextDouble() * 32 - 16);
 			nearest.setZ(nearest.getZ() + random.nextDouble() * 32 - 16);
 		}
 		return nearest;
 	}
 	
 	public String getFollowTarget(Player player)
 	{
 		if ( spectators.containsKey(player.getName()) )
 			return spectators.get(player.getName());
 		return null;
 	}
 	
 	public void setFollowTarget(Player player, String target)
 	{
 		spectators.put(player.getName(), target);
 	}
 	
 	private final double maxFollowSpectateRangeSq = 40 * 40, maxAcceptableOffsetDot = 0.65;
 	private final int maxSpectatePositionAttempts = 5, idealFollowSpectateRange = 20;
 	
 	public void checkFollowTarget(Player player)
 	{
 		String targetName = spectators.get(player.getName()); 
 		if ( targetName == null )
 			return; // don't have a target, don't want to get moved to it
 		
 		Player target = plugin.getServer().getPlayerExact(targetName);
 		if ( !isAlive(targetName) || target == null || !target.isOnline() )
 		{
 			targetName = getDefaultFollowTarget();
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
 		
 		Location specLoc = player.getEyeLocation();
 		Location targetLoc = target.getEyeLocation();
 		
 		// check they're in the same world
 		if ( specLoc.getWorld() != targetLoc.getWorld() )
 		{
 			moveToSee(player, target);
 			return;
 		}
 		
 		// then check the distance is appropriate
 		double targetDistSqr = specLoc.distanceSquared(targetLoc); 
 		if ( targetDistSqr > maxFollowSpectateRangeSq )
 		{
 			moveToSee(player, target);
 			return;
 		}
 		
 		// check if they're facing the right way
 		Vector specDir = specLoc.getDirection().normalize();
 		Vector dirToTarget = targetLoc.subtract(specLoc).toVector().normalize();
 		if ( specDir.dot(dirToTarget) < maxAcceptableOffsetDot )
 		{
 			moveToSee(player, target);
 			return;
 		}
 		
 		// then do a ray trace to see if there's anything in the way
         Iterator<Block> itr = new BlockIterator(specLoc.getWorld(), specLoc.toVector(), dirToTarget, 0, (int)Math.sqrt(targetDistSqr));
         while (itr.hasNext())
         {
             Block block = itr.next();
             if ( !block.isEmpty() )
 			{
 				moveToSee(player, target);
 				return;
 			}
         }
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
 			
 			Location pos = targetLoc;
 			// keep going until we reach the ideal distance or hit a non-empty block
 			Iterator<Block> itr = new BlockIterator(targetLoc.getWorld(), targetLoc.toVector(), dir, 0, idealFollowSpectateRange);
 	        while (itr.hasNext())
 	        {
 	            Block block = itr.next();
 	            if ( !block.isEmpty() )
 	            	break;
 	            
 	            if ( targetLoc.getWorld().getBlockAt(block.getLocation().getBlockX(), block.getLocation().getBlockY()-1, block.getLocation().getBlockZ()).isEmpty() )
 	            	pos = block.getLocation().add(0.5, player.getEyeHeight()-1, 0.5);
 	        }
 	        
 	        if ( !itr.hasNext() ) // we made it the max distance! use this!
 	        {
 	        	bestLoc = pos;
 	        	break;
 	        }
 	        else
 	        {
 	        	double distSq = pos.distanceSquared(targetLoc); 
 	        	if ( distSq > bestDistSq )
 		        {
 		        	bestLoc = pos;
 		        	bestDistSq = distSq; 
 		        }
 	        }
 		}
 
 		// as we're dealing in eye position thus far, reduce the Y to get the "feet position"
 		bestLoc.setY(bestLoc.getY() - player.getEyeHeight());
 		
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
			bestLoc.setPitch((float)Math.toDegrees(Math.atan(yDif / horizDist)));
 		else
			bestLoc.setPitch(-(float)Math.toDegrees(Math.atan(-yDif / horizDist)));
 		
 		// set them as flying so they don't fall from this position, then do the teleport
 		player.setFlying(true);
 		player.teleport(bestLoc);
 	}
 	
 	public String getDefaultFollowTarget()
 	{
 		for ( String name : alive )
 		{
 			Player player = plugin.getServer().getPlayerExact(name);
 			if ( player != null && player.isOnline() )
 				return name;
 		}
 		return null;
 	}
 }
