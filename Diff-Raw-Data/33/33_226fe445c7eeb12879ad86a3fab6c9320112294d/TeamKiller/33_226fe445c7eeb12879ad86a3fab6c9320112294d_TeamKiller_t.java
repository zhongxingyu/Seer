 package com.ftwinston.Killer.GameModes;
 
 import java.util.Map;
 
 import com.ftwinston.Killer.GameMode;
 import com.ftwinston.Killer.PlayerManager;
 import com.ftwinston.Killer.PlayerManager.Info;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 import org.bukkit.entity.Player;
 import org.bukkit.Material;
 
 public class TeamKiller extends GameMode
 {	
 	@Override
 	public String getName() { return "Team Killer"; }
 	
 	@Override
 	public int getModeNumber() { return 4; }
 
 	@Override
 	public int absMinPlayers() { return 2; }
 
 	@Override
 	public boolean killersCompassPointsAtFriendlies() { return false; }
 
 	@Override
 	public boolean friendliesCompassPointsAtKiller() { return false; }
 
 	@Override
 	public boolean discreteDeathMessages() { return false; }
 
 	@Override
 	public boolean usesPlinth() { return true; }
 
 	@Override
 	public int determineNumberOfKillersToAdd(int numAlive, int numKillers, int numAliveKillers)
 	{
 		// if we're not set to auto-reassign the killer once one has been assigned at all, even if they're no longer alive / connected, don't do so
 		if ( numKillers > 0 )
 			return 0;
 		
 		// half the players should be killers. don't care if its the "bigger" half or not
 		if ( numAlive % 2 == 1 )
 			return numAlive / 2 + r.nextInt(1); // it's random which team should have the extra player
 		else
 			return numAlive / 2;
 	}
 	
 	@Override
 	public String describePlayer(boolean killer, boolean plural)
 	{
 		if ( killer )
 			return "red team";
 		else
 			return "blue team";
 	}
 	
 	@Override
 	public boolean informOfKillerAssignment(PlayerManager pm) { return false; }
 	
 	@Override
 	public boolean informOfKillerIdentity() { return true; }
 	
 	@Override
 	public boolean revealKillersIdentityAtEnd() { return false; }
 	
 	@Override
 	public boolean immediateKillerAssignment() { return true; }
 	
 	@Override
 	public void explainGameMode(Player player, PlayerManager pm)
 	{
 		String message = getName() + "\n";
 		message += "Players " + (pm.numKillersAssigned() > 0 ? "have been" : "will soon be") + " split into two teams, and each team must race to bring a ";
 			
 		message += plugin.tidyItemName(plugin.winningItems[0]);
 		
 		if ( plugin.winningItems.length > 1 )
 		{
 			for ( int i=1; i<plugin.winningItems.length-1; i++)
 				message += ", a " + plugin.tidyItemName(plugin.winningItems[i]);
 			
 			message += " or a " + plugin.tidyItemName(plugin.winningItems[plugin.winningItems.length-1]);
 		}
 		
 		message += " to the plinth near the spawn, or to eliminate the other.\nUse the scoreboard to check what team each player is on.";
 		player.sendMessage(message);
 	}
 	
 	@Override
 	public void playerJoined(Player player, PlayerManager pm, boolean isNewPlayer, PlayerManager.Info info)
 	{
 		if ( isNewPlayer ) // this is a new player, tell them the rules & state of the game
 		{
 			player.sendMessage("Welcome to Killer Minecraft!");
 			
 			if ( pm.numKillersAssigned() > 0 && info.isAlive() )
 			{
				// add them to whichever team has fewest players
				int numFriendlies = 0, numKillers = 0;
 				for ( Map.Entry<String, Info> entry : pm.getPlayerInfo() )
 					if ( entry.getValue().isKiller() )
						numKillers++;
 					else
						numFriendlies++;
 				
				boolean killer;
				
				if ( numKillers < numFriendlies )
					killer = true;
				else if ( numKillers == numFriendlies )
					killer = r.nextInt() == 1;
				else
					killer = false;
				
				if ( killer )
 				{
 					info.setKiller(true);
 					plugin.getServer().broadcastMessage(player.getName() + " is on the " + ChatColor.RED + "red" + ChatColor.RESET + " team.");
 				}
 				else
 					plugin.getServer().broadcastMessage(player.getName() + " is on the " + ChatColor.BLUE + "blue" + ChatColor.RESET + " team.");
 			}
 		}
 		else
 			player.sendMessage("Welcome back.");
 	}
 	
 	private final int teamSeparationOffset = 25;
 	
 	@Override
 	public void prepareKiller(Player player, PlayerManager pm, boolean isNewPlayer)
 	{
 		player.sendMessage("You are on the " + ChatColor.RED + "red" + ChatColor.RESET + " team. Use the /team command to chat without the other team seeing your messages.");
 		
 		if ( !isNewPlayer )
 			return; // don't do the teleport thing when reconnecting
 			
 		Location moveTo = player.getWorld().getSpawnLocation();
 		moveTo.setZ(moveTo.getZ() + teamSeparationOffset + r.nextDouble() * 6);
 		moveTo.setX(moveTo.getX() + r.nextDouble() * 6);
 		moveTo.setY(moveTo.getWorld().getHighestBlockYAt(moveTo) + 1);
 		
 		player.teleport(moveTo);
 	}
 	
 	@Override
 	public void prepareFriendly(Player player, PlayerManager pm, boolean isNewPlayer)
 	{
 		player.sendMessage("You are on the " + ChatColor.BLUE + "blue" + ChatColor.RESET + " team. Use the /team command to chat without the other team seeing your messages.");
 		
 		if ( !isNewPlayer )
 			return; // don't do the teleport thing when reconnecting
 			
 		Location moveTo = player.getWorld().getSpawnLocation();
 		moveTo.setZ(moveTo.getZ() - teamSeparationOffset - r.nextDouble() * 6);
 		moveTo.setX(moveTo.getX() + r.nextDouble() * 6);
 		moveTo.setY(moveTo.getWorld().getHighestBlockYAt(moveTo) + 1);
 		
 		player.teleport(moveTo);
 	}
 	
 	@Override
 	public void checkForEndOfGame(PlayerManager pm, Player playerOnPlinth, Material itemOnPlinth)
 	{
 		// if there's no one alive at all, game was drawn
 		if (pm.numSurvivors() == 0 )
 		{
 			pm.gameFinished(false, false, null, null);
 			return;
 		}
 		
 		// if someone stands on the plinth with a winning item, their team wins
 		if ( playerOnPlinth != null && itemOnPlinth != null )
 		{
 			boolean killersWin = pm.isKiller(playerOnPlinth.getName());
 			pm.gameFinished(killersWin, !killersWin, playerOnPlinth.getName(), itemOnPlinth);
 			return;
 		}
 		
 		boolean killersAlive = false, friendliesAlive = false;
 		for ( Map.Entry<String, Info> entry : pm.getPlayerInfo() )
 			if ( entry.getValue().isAlive() )
 				if ( entry.getValue().isKiller() )
 					killersAlive = true;
 				else
 					friendliesAlive = true;
 		
 		// if only killers are left alive, the killers won
 		if ( killersAlive && !friendliesAlive )
 			pm.gameFinished(true, false, null, null);
 		// if only friendlies are left alive, the friendlies won
 		else if ( !killersAlive && friendliesAlive )
 			pm.gameFinished(false, true, null, null);
 	}
 }
