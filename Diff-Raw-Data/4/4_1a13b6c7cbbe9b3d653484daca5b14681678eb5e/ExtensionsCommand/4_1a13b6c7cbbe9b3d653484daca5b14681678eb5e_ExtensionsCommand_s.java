 package com.barroncraft.sce;
 
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import net.sacredlabyrinth.phaed.simpleclans.Clan;
 import net.sacredlabyrinth.phaed.simpleclans.ClanPlayer;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.ItemStack;
 
 public class ExtensionsCommand 
 {
 	SimpleClansExtensions plugin;
 	Set<String> surrenderPlayers;
 	
 	public ExtensionsCommand(SimpleClansExtensions plugin)
 	{
 		this.plugin = plugin;
 		this.surrenderPlayers = new HashSet<String>();
 	}
 	
 	public void CommandJoin(Player player, String newClanName)
 	{
 		ClanPlayer clanPlayer = plugin.clanManager.getCreateClanPlayer(player.getName());
 		Clan newClan = plugin.clanManager.getClan(newClanName);
 		Clan oldClan = clanPlayer.getClan();
 		int transferDifference = plugin.maxDifference + 1;
 		
 		if (oldClan != null) // Changing clans
 		{
 			if (newClan != null && oldClan.getName().equalsIgnoreCase(newClan.getName()))
 				player.sendMessage(ChatColor.RED + "You can't transfer to the same team");
 			else if (PlayerDifference(oldClan, newClan) < transferDifference)
 				player.sendMessage(ChatColor.RED + "You can't transfer teams unless there is a difference of " + transferDifference + " between them");
 			else
 			{
 				oldClan.removeMember(player.getName());
 				AddPlayerToClan(clanPlayer, newClan, newClanName);
 				player.sendMessage(ChatColor.BLUE + "You have been transfered to team " + newClanName);
 			}
 		} 
 		else // Joining a clan for the first time
 		{
 			if (PlayerDifference(newClan, plugin.maxDifference) >= plugin.maxDifference)
 				player.sendMessage(ChatColor.RED + "That team already has too many players.  Try a different one.");
 			else
 			{
 				AddPlayerToClan(clanPlayer, newClan, newClanName);
 				player.getInventory().addItem(new ItemStack(Material.STONE_SWORD, 1));
 				player.sendMessage(ChatColor.BLUE + "You have joined team " + newClanName);
 			}
 		}
 	}
 	
 	public void CommandSurrender(Player player) 
 	{
 		String playerName = player.getName();
 		Clan currentClan = plugin.clanManager.getCreateClanPlayer(playerName).getClan();
 		
 		if (currentClan == null)
 		{
 			player.sendMessage(ChatColor.RED + "You can't surrender until you join a team");
 			return;
 		}
 		
 		if (!surrenderPlayers.contains(playerName))
 		{
 			surrenderPlayers.add(playerName);
 			
 			List<ClanPlayer> currentClanPlayers = currentClan.getOnlineMembers();
 			List<ClanPlayer> surrenderingPlayers = new ArrayList<ClanPlayer>();
 			
 			for (ClanPlayer clanPlayer : currentClan.getOnlineMembers())
 			{
 				if (surrenderPlayers.contains(clanPlayer.getName()))
 					surrenderingPlayers.add(clanPlayer);
 			}
 			
 			double surrenderRatio = ((double)surrenderingPlayers.size()) / ((double)currentClanPlayers.size());
 			
 			if (surrenderRatio > 0.66d)
 			{
 				if (!ServerResetter.getResetFlag())
 				{
                     ServerResetter.enableResetFlag();
 					ClanTeam team = plugin.clanTeams.get(currentClan.getName());
 					String teamName = team.getColor() + team.getName().toUpperCase() + ChatColor.YELLOW;
 					plugin.getServer().broadcastMessage(ChatColor.YELLOW + "The " + teamName + " team has agreed to surrender.  Game over.");
 					plugin.getServer().broadcastMessage(ChatColor.YELLOW + "The map should auto reset within a few minutes.");
 				}
 			}
 			else
 			{
 				player.sendMessage(ChatColor.YELLOW + "You have been added to the list of people wishing to surrender. (" + Math.floor(surrenderRatio * 100) + "% so far)");
 				player.sendMessage(ChatColor.YELLOW + "To remove your surrender vote type /surrender again.");
 			}
 		}
 		else
 		{
 			surrenderPlayers.remove(playerName);
 			player.sendMessage(ChatColor.YELLOW + "You have been removed from the list of people wanting to surrender.");
 		}
 	}
 	
 	private int PlayerDifference(Clan clan, int max)
 	{
 		int returnValue = 0;
 		int count = GetOnlinePlayerCount(clan);
 		for (String name : plugin.clanTeams.keySet())
 		{
 			int clanCount = GetOnlinePlayerCount(plugin.clanManager.getClan(name));
 			int difference = count - clanCount;
 			if (difference >= max)
 				return difference;
 			else if (difference > returnValue)
 				returnValue = difference;
 		}
 		
 		return returnValue;
 	}
 	
 	private int PlayerDifference(Clan firstClan, Clan secondClan)
 	{
 		return GetOnlinePlayerCount(firstClan) - GetOnlinePlayerCount(secondClan);
 	}
 	
 	private void AddPlayerToClan(ClanPlayer player, Clan clan, String clanName)
 	{
 		if (clan == null)
 		{
 			plugin.clanManager.createClan(player.toPlayer(), clanName, clanName);
 			clan = plugin.clanManager.getClan(clanName);
 			Location location = plugin.clanTeams.get(clanName).getSpawn();
 			if (location != null)
 				clan.setHomeLocation(location);
 		}
 		clan.addPlayerToClan(player);
		player.toPlayer().teleport(clan.getHomeLocation());
 	}
 	
 	private int GetOnlinePlayerCount(Clan clan)
 	{
 		if (clan == null)
 			return 0;
 		
 		int count = 0;
 		for (ClanPlayer player : clan.getMembers())
 		{
 			Player onlinePlayer = player.toPlayer();
 			if (onlinePlayer != null && onlinePlayer.isOnline())
 				count++;
 		}
 		return count;
 	}
 
 	
 }
