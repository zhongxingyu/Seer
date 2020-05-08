 package me.smith_61.adventure.bukkit.commands;
 
 import me.smith_61.adventure.bukkit.BukkitPlugin;
 import me.smith_61.adventure.common.Adventure;
 import me.smith_61.adventure.common.AdventureManager;
 import me.smith_61.adventure.common.AdventurePlayer;
 import me.smith_61.adventure.common.AdventureTeam;
 
 import org.bukkit.ChatColor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 import se.ranzdo.bukkit.methodcommand.Arg;
 import se.ranzdo.bukkit.methodcommand.Command;
 
 public class CommandAdventure {
 
 	@Command(
 			identifier = "adventure team create",
 			description = "Creates a new group with the given name",
 			onlyPlayers = true
 			)
 	public void createGroup(Player sender, @Arg(name = "team_name") String teamName) {
 		teamName = teamName.trim();
 		if(teamName.isEmpty()) {
 			return;
 		}
 		
 		AdventureManager manager = BukkitPlugin.getInstance().getAdventureManager();
 		
 		AdventurePlayer player = manager.getAdventurePlayer(sender.getName());
 		
 		if(manager.getAdventureTeam(teamName) != null) {
 			player.sendMessage("Adventure team already exists with name: " + teamName);
 			return;
 		}
 		manager.createTeam(teamName, player);
 		
 		player.sendMessage("Created adventure team with name: " + teamName);
 	}
 	
 	@Command(
 			identifier = "adventure team leave",
 			description = "Leaves the current team",
 			onlyPlayers = true
 			)
 	public void leaveTeam(Player sender) {
 		AdventureManager manager = BukkitPlugin.getInstance().getAdventureManager();
 		
 		AdventurePlayer player = manager.getAdventurePlayer(sender.getName());
 		player.joinTeam(null);
 	}
 	
 	@Command(
 			identifier = "adventure team join",
 			description = "Joins the given team",
 			onlyPlayers = true
 			)
 	public void joinTeam(Player sender, @Arg(name = "team_name") String teamName) {
 		AdventureManager manager = BukkitPlugin.getInstance().getAdventureManager();
 		AdventurePlayer player = manager.getAdventurePlayer(sender.getName());
 		
 		AdventureTeam team = manager.getAdventureTeam(teamName);
 		if(team == null) {
 			player.sendMessage("No team found for name: " + teamName);
 			return;
 		}
 		
 		player.joinTeam(team);
 	}
 	
 	@Command(
 			identifier = "adventure team list",
 			description = "Lists all current teams",
 			onlyPlayers = false
 			)
 	public void listTeams(CommandSender sender) {
 		AdventureManager manager = BukkitPlugin.getInstance().getAdventureManager();
 		
 		AdventureTeam[] teams = manager.getAdventureTeams();
 		sender.sendMessage(ChatColor.RED + "Adventure Teams ( " + teams.length + " ):");
 		
 		for(AdventureTeam team : teams) {
 			sender.sendMessage(ChatColor.GRAY + "    - " + team.getName() + " ( " + team.getTeammates().length + " )");
 		}
 	}
 	
 	@Command(
 			identifier = "adventure team info",
 			description = "Gets info about a specific team",
 			onlyPlayers = false
 			)
 	public void teamInfo(CommandSender sender, @Arg(name = "team_name", def = "") String teamName) {
 		AdventureManager manager = BukkitPlugin.getInstance().getAdventureManager();
 		
 		
 		AdventureTeam team = null;
 		if(teamName.isEmpty()) {
 			if(sender instanceof Player) {
 				AdventurePlayer player = manager.getAdventurePlayer(sender.getName());
 				team = player.getCurrentTeam();
 				
 				if(team == null) {
 					sender.sendMessage(ChatColor.RED + "Not in a team.");
 					return;
 				}
 			}
 			else {
 				sender.sendMessage(ChatColor.RED + "Must be a player to issue command without team name.");
 				return;
 			}
 		}
 		else {
 			team = manager.getAdventureTeam(teamName);
 		}
 		
 		if(team == null) {
 			sender.sendMessage(ChatColor.RED + "No team found for name: " + teamName);
 			return;
 		}
 		
 		sender.sendMessage(ChatColor.RED + "Info for team " + team.getName() + ":");
 		String adventureName = "None";
 		if(team.getCurrentAdventure() != null) {
 			adventureName = team.getCurrentAdventure().getName();
 		}
 		sender.sendMessage(ChatColor.GRAY + "    - Adventure: " + adventureName);
 		
 		AdventurePlayer[] players = team.getTeammates();
 		sender.sendMessage(ChatColor.GRAY + "    - Players( " + players.length + " ):");
 		for(AdventurePlayer player : players) {
 			if(player == team.getLeader()) {
 				sender.sendMessage(ChatColor.GRAY + "        - " + player.getName() + " (Leader)");
 			}
 			else {
 				sender.sendMessage(ChatColor.GRAY + "        - " + player.getName());
 			}
 		}
 	}
 	
 	@Command(
 			identifier = "adventure start",
 			description = "Starts an adventure with the given name",
 			onlyPlayers = true
 			)
 	public void startAdventure(Player sender, @Arg(name = "adventure_name") String adventureName) {
 		AdventureManager manager = BukkitPlugin.getInstance().getAdventureManager();
 		AdventurePlayer player = manager.getAdventurePlayer(sender.getName());
 		AdventureTeam team = player.getCurrentTeam();
 		
 		if(team == null) {
 			player.sendMessage("You are not a part of any adventure team.");
 			return;
 		}
 		
 		if(team.getLeader() != player) {
 			player.sendMessage("You are not the leader of your team.");
 			return;
 		}
 		
 		if(team.getCurrentAdventure() != null) {
 			player.sendMessage("You are currently in an adventure. You must leave the current adventure before joining a new adventure.");
 			return;
 		}
 		
 		Adventure adventure = manager.getAdventure(adventureName);
 		if(adventure == null) {
 			player.sendMessage("No adventure found for name: " + adventureName);
 			return;
 		}
 		
 		team.joinAdventure(adventure);
 	}
 	
 	@Command(
 			identifier = "adventure stop",
 			description = "Stops the current adventure",
 			onlyPlayers = true
 			)
 	public void stopAdventure(Player sender) {
 		AdventureManager manager = BukkitPlugin.getInstance().getAdventureManager();
 		AdventurePlayer player = manager.getAdventurePlayer(sender.getName());
 		AdventureTeam team = player.getCurrentTeam();
 		
 		if(team == null || team.getCurrentAdventure() == null) {
 			player.sendMessage("You are not currently in an adventure.");
 			return;
 		}
 		
 		team.joinAdventure(null);
 	}
 	
 	@Command(
 			identifier = "adventure list",
 			description = "Lists all current adventures",
 			onlyPlayers = false
 			)
 	public void listAdventures(CommandSender sender) {
 		AdventureManager manager = BukkitPlugin.getInstance().getAdventureManager();
 		
 		Adventure[] adventures = manager.getAdventures();
 		sender.sendMessage(ChatColor.RED + "Adventures ( " + adventures.length + " ):");
 		
 		for(Adventure adventure : adventures) {
 			sender.sendMessage(ChatColor.GRAY + "    - " + adventure.getName() + " ( " + adventure.getAdventureTeams().length + " )");
 		}
 		
 	}
 	
 	@Command(
 			identifier = "adventure info",
 			description = "Gets info about an adventure",
 			onlyPlayers = false
 			)
 	public void adventureInfo(CommandSender sender, @Arg(name = "adventure_name", def = "") String adventureName) {
 		AdventureManager manager = BukkitPlugin.getInstance().getAdventureManager();
 		
 		Adventure adventure = null;
 		if(adventureName.isEmpty()) {
 			if(sender instanceof Player) {
 				AdventurePlayer player = manager.getAdventurePlayer(sender.getName());
 				AdventureTeam team = player.getCurrentTeam();
 				if(team == null) {
 					sender.sendMessage(ChatColor.RED + "Not in a team.");
 					return;
 				}
 				
 				adventure = team.getCurrentAdventure();
 				if(adventure == null) {
 					sender.sendMessage(ChatColor.RED + "Not currently in an adventure.");
 					return;
 				}
 			}
 			else {
 				sender.sendMessage(ChatColor.RED + "Must be a player to execute this command without an adventure name.");
 				return;
 			}
 		}
 		else {
 			adventure = manager.getAdventure(adventureName);
 		}
 		
 		if(adventure == null) {
 			sender.sendMessage(ChatColor.RED + "No adventure found for name: " + adventureName);
 			return;
 		}
 		
 		sender.sendMessage(ChatColor.RED + "Info for " + adventure.getName() + ":");
 		
 		AdventureTeam[] teams = adventure.getAdventureTeams();
 		sender.sendMessage(ChatColor.GRAY + "    - Teams ( " + teams.length + " ):");
 		for(AdventureTeam team : teams) {
 			sender.sendMessage(ChatColor.GRAY + "        - " + team.getName());
 		}
 	}
 }
