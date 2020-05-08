 package com.mrz.dyndns.server.warpsuite.util;
 
 import static com.mrz.dyndns.server.warpsuite.util.Coloring.POSITIVE_PRIMARY;
 import static com.mrz.dyndns.server.warpsuite.util.Coloring.POSITIVE_SECONDARY;
 
 import java.util.logging.Level;
 
 import org.bukkit.ChatColor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 import com.mrz.dyndns.server.warpsuite.WarpSuite;
 import com.mrz.dyndns.server.warpsuite.WarpSuitePlayer;
 
 public final class Util
 {
 	private Util()
 	{	
 	}
 	
 	private static boolean isDebugging = false;
 	private static WarpSuite plugin;
 	
 	public static void initialize(WarpSuite ws)
 	{
 		plugin = ws;
 	}
 	
 	public static void setDebugging(boolean debugging)
 	{
 		isDebugging = debugging;
 	}
 	
 	public static void Debug(Object message)
 	{
 		if(isDebugging == true)
 		{
 			if(plugin != null)
 			{
 				plugin.getLogger().log(Level.INFO, "[DEBUG] " + message.toString());
 			}
 			else
 			{
 				System.out.println(plugin.getName() + "[INFO] [DEBUG] " + message.toString());
 			}
 		}
 	}
 	
 	public static long getUnixTime()
 	{
 		return System.currentTimeMillis() / 1000L;
 	}
 	
 	public static boolean mustBePlayer(CommandSender sender)
 	{
 		sender.sendMessage(ChatColor.RED + "You must be a player to use this command!");
 		return true;
 	}
 	
 	public static boolean isValidWarpName(String warpName)
 	{
 		return (warpName.contains("\'")
 				|| warpName.contains("\'")
 				|| warpName.contains(" ") == false);
 	}
 	
 	public static boolean areTherePlayersInRadius(WarpSuitePlayer player)
 	{
 		Player p = player.getPlayer();
 		double radius = Config.radius;
 		
 		return (p.getNearbyEntities(radius, radius, radius).size() != 0);
 	}
 	
 	public static void sendYouWillBeWarpedMessage(WarpSuitePlayer player)
 	{
 		//TODO: my gosh this needs testing!
 		StringBuilder sb = new StringBuilder();
		sb.append(POSITIVE_PRIMARY + "You will be warped in " + POSITIVE_SECONDARY + Config.timer + POSITIVE_PRIMARY + "seconds.");
 		boolean thingsToSay = (Config.cancelOnMobDamage || Config.cancelOnMove || Config.cancelOnPvp);
 		if(thingsToSay)
 		{
 			sb.append(" Don\'t ");
 			
 			if(Config.cancelOnPvp)
 			{
 				sb.append("engage in pvp");
 				if(Config.cancelOnMobDamage && Config.cancelOnMove)
 				{
 					sb.append(", ");
 				}
 				else if(!Config.cancelOnMobDamage || !Config.cancelOnMobDamage)
 				{
					sb.append("or ");
 				}
 				else
 				{
 					sb.append(".");
 				}
 			}
 			if(Config.cancelOnMobDamage)
 			{
 				sb.append("get hurt by mobs");
 				if(Config.cancelOnMove)
 				{
					sb.append(" or");
 				}
 				else
 				{
 					sb.append(".");
 				}
 			}
 			if(Config.cancelOnMove)
 			{
 				sb.append("move around.");
 			}
 			
 			player.sendMessage(sb.toString());
 		}
 		else
 		{
 			player.sendMessage(sb.toString());
 		}
 	}
 }
