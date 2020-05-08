 
 package net.loadingchunks.plugins.GuardWolf;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 import org.bukkit.entity.Player;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.Calendar;
 
 /**
  * Handler for the /gw sample command.
  * @author Cue
  */
 public class GWBan implements CommandExecutor {
     private final GuardWolf plugin;
     private final GWSQL sql;
 
     public GWBan (GuardWolf plugin) {
         this.plugin = plugin;
         this.sql = this.plugin.sql;
     }
 
     @Override
     public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
         if (!(sender instanceof Player)) {
             return false;
         }
         Player player = (Player) sender;
 
         if (args.length == 0 && !command.getName().equalsIgnoreCase("banlist")) {
         	player.sendMessage("GuardWolf detected no arguments, type '/gw help' for a list of commands.");
         	return true;
         } else if(command.getName().equalsIgnoreCase("ban"))
         {
         	if(this.plugin.gm.getWorldsHolder().getWorldPermissions((Player) sender).has((Player) sender, "guardwolf.ban.ban"))
         	{
         		funcBan(player, args);
         		return true;
         	} else return false;
         } else if(command.getName().equalsIgnoreCase("unban"))
         {
         	if(this.plugin.gm.getWorldsHolder().getWorldPermissions((Player) sender).has((Player) sender, "guardwolf.ban.unban"))
         	{
         		funcUnBan(player, args);
         		return true;
         	} else return false;
         } else if(command.getName().equalsIgnoreCase("banlist"))
         {
         	if(this.plugin.gm.getWorldsHolder().getWorldPermissions((Player) sender).has((Player) sender, "guardwolf.ban.banlist"))
         	{
         		funcBanList(player, args);
         		return true;
         	} else return false;
     	}
         else
         	player.sendMessage("That command is unknown/not implemented yet!");
         
         return false;
     }
     
     private void funcUnBan(Player sender, String[] args) {
     	if(args.length == 0)
     	{
     		sender.sendMessage(ChatColor.DARK_AQUA + "[GUARDWOLF] " + ChatColor.RED + "No arguments given!");
     		return;
     	} else {
     		plugin.sql.UnBan(args[0], sender.getName());
     		sender.sendMessage(ChatColor.DARK_AQUA + "[GUARDWOLF] " + ChatColor.RED + "Unbanned user " + ChatColor.YELLOW + args[0]);
     	}
 	}
 
 	public void funcBanList(Player sender, String[] args)
     {
     	if(args.length == 0)
     	{
     		sender.sendMessage(ChatColor.DARK_AQUA + "[GUARDWOLF] " + ChatColor.RED + "Looking up bans...");
     		plugin.sql.ListBan(1, "", sender);
     	} else if (args.length == 1)
     	{
     		sender.sendMessage(ChatColor.DARK_AQUA + "[GUARDWOLF] " + ChatColor.RED + "Looking up bans...");
     		plugin.sql.ListBan(Integer.parseInt(args[0]), "", sender);
     	} else if (args.length == 2)
     	{
     		sender.sendMessage(ChatColor.DARK_AQUA + "[GUARDWOLF] " + ChatColor.RED + "Looking up bans for " + args[1] + "...");
     		plugin.sql.ListBan(Integer.parseInt(args[0]), args[1], sender);
     	} else sender.sendMessage(ChatColor.DARK_AQUA + "[GUARDWOLF] " + ChatColor.WHITE + "Invalid Arguments!");
     }
     
     public void funcBan(Player sender, String[] args)
     {
         Calendar time = null;
         String reason = "";
         String target = "";
 
         
     	if(args.length == 0)
     	{
     		sender.sendMessage(ChatColor.DARK_AQUA + "[GUARDWOLF] " + ChatColor.RED + "No arguments given!");
     		return;
     	} else {
             if(this.plugin.getServer().getPlayer(args[0]) != null)
             	target = this.plugin.getServer().getPlayer(args[0]).getName();
             else
             	target = args[0];
 
     		if(args.length >= 2)
     		{
     			if(!args[1].equalsIgnoreCase("permanent"))
     				time = TimeParser.parseTime(args[1], sender);
     		}
     		else
     			time = TimeParser.parseTime(this.plugin.gwConfig.get("default_time"), sender);
    		
    		sender.sendMessage(ChatColor.DARK_AQUA + "[GUARDWOLF] " + ChatColor.RED + "Banning " + ChatColor.YELLOW + target + ChatColor.RED + " for so long, the default would be: " + this.plugin.gwConfig.get("default_time"));
    		sender.sendMessage(time.toString());
 
     		if(args.length >= 3)
     		{
     			for (int i = 2; i < args.length; i++) {
     				reason = reason + " " + args[i];
     			}
     		} else {
     			this.plugin.gwConfig.get("default_reason");
     		}
     		
     		if(args.length >= 2)
     		{
     			if(args[1].equalsIgnoreCase("permanent"))
     				plugin.sql.Ban(target, sender.getName(), 0, reason, 1);
     			else
     				plugin.sql.Ban(target, sender.getName(), (((System.currentTimeMillis() - time.getTimeInMillis()) + System.currentTimeMillis()) / 1000), reason, 0);
     		} else
    			plugin.sql.Ban(target, sender.getName(), (((System.currentTimeMillis() + time.getTimeInMillis()) + System.currentTimeMillis()) / 1000), reason, 0);
     		
     		if(this.plugin.getServer().getPlayer(target) != null)
     			this.plugin.getServer().getPlayer(target).kickPlayer(reason);
     		
     	}
     }
 }
