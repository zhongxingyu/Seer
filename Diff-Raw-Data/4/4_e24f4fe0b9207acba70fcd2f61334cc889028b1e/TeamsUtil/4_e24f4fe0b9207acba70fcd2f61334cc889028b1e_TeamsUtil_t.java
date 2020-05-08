 package com.bendude56.hunted.teams;
 
 import org.bukkit.ChatColor;
 import org.bukkit.entity.Player;
 
 
 import com.bendude56.hunted.HuntedPlugin;
import com.bendude56.hunted.teams.TeamManager.Team;
 
 public class TeamsUtil {
 
 	public static void sendMessageJoinTeam(Player p)
 	{
		TeamManager teams = HuntedPlugin.getInstance().getTeams();
 		
 		p.sendMessage(ChatColor.YELLOW + "You have joined the " + getTeamColor(teams.getTeamOf(p) + getTeamName(teams.getTeamOf(p), true) + ChatColor.YELLOW + "."));
 	}
 
 	public static ChatColor getTeamColor(Player p)
 	{
 		return getTeamColor(HuntedPlugin.getInstance().getTeams().getTeamOf(p));
 	}
 
 	public static ChatColor getTeamColor(String s)
 	{
 		return getTeamColor(HuntedPlugin.getInstance().getTeams().getTeamOf(s));
 	}
 
 	public static ChatColor getTeamColor(Team t)
 	{
 		switch (t) {
 			case HUNTERS:	return ChatColor.DARK_RED;
 			case PREY:		return ChatColor.BLUE;
 			case SPECTATORS:return ChatColor.YELLOW;
 			case NONE:		return ChatColor.WHITE;
 		}
 		return ChatColor.WHITE;
 	}
 
 	public static String getTeamName(Team team, boolean plural)
 	{
 		switch (team) {
 			case HUNTERS:	return (plural ? "Hunters" : "Hunter");
 			case PREY:		return (plural ? "Prey" : "Prey");
 			case SPECTATORS:return (plural ? "Spectators" : "Spectator");
 			case NONE:		return "";
 		}
 		return "";
 	}
 
 }
