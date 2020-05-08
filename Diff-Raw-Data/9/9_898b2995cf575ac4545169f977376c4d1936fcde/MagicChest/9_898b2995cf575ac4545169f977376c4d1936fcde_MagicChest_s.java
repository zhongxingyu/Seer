 package org.zonedabone.magicchest;
 
 import java.io.IOException;
 
 import org.bukkit.ChatColor;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class MagicChest extends JavaPlugin {
 
 	public MagicChestListener mcl;
 
 	@Override
 	public void onEnable() {
 		mcl = new MagicChestListener(this);
 		getServer().getPluginManager().registerEvents(this.mcl, this);
 		saveDefaultConfig();
		getLogger().info(PluginCompatibility.printPluginCompatibilty());
 		getConfig().options().copyDefaults(true);
 		try {
 			MetricsLite metrics = new MetricsLite(this);
 			metrics.start();
 		} catch (IOException e) {
 			// Failed to submit the stats :-(
 		}
 	}
 
 	public void onDisable() {
 		//save all config
 		if(getConfig().getConfigurationSection("players") != null)
 		{
 			saveConfig();
 		}
 	}
 
 	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args)
 	{
 		if(sender instanceof Player)
 		{
 			Player plcmd = (Player)sender;
 			if((!getConfig().getBoolean("override")) || ((getConfig().getBoolean("override") && (plcmd.isOp() || plcmd.hasPermission("magicchest.reload")))))
 			{
 				if(args.length > 0)
 				{
 					//show help
 					if(args[0].equalsIgnoreCase("help"))
 					{
 						if(plcmd.isOp() || plcmd.hasPermission("magicchest.help"))
 						{
 							Chat.sendPM(plcmd, "Usage:");
 							//*facepalm*.... U NOOB.
 							return false;
 						}
 					}
 					//turn individual player sort on
 					if(args[0].equalsIgnoreCase("on"))
 					{
 						//check for perms
 						if(plcmd.isOp() || plcmd.hasPermission("magicchest.sort"))
 						{
 							if(getConfig().getConfigurationSection("players") != null)
 							{
 								if(getConfig().getConfigurationSection("players").contains(plcmd.getName()))
 								{
 									getConfig().set("players." + plcmd.getName(), true);
 									Chat.sendPM(plcmd, "Auto sorting switched on!");
 									saveConfig();
 									return true;
 								}
 							}
 							else
 							{
 								getConfig().set("players." + plcmd.getName(), true);
 								Chat.sendPM(plcmd, "Auto sorting switched on!");
 								saveConfig();
 								return true;
 							}
 						}
 						//nope!
 						Chat.sendPM(plcmd, "Your permissions do not allow you to sort chests!");
 						return true;
 					}
 					//turn individual player sort off
 					if(args[0].equalsIgnoreCase("off"))
 					{
 						//check for perms
 						if(plcmd.isOp() || plcmd.hasPermission("magicchest.sort"))
 						{
 							if(getConfig().getConfigurationSection("players") != null)
 							{
 								if(getConfig().getConfigurationSection("players").contains(plcmd.getName()))
 								{
 									getConfig().set("players." + plcmd.getName(), false);
 									Chat.sendPM(plcmd, "Auto sorting switched off!");
 									saveConfig();
 									return true;
 								}
 							}
 							else
 							{
 								getConfig().set("players." + plcmd.getName(), false);
 								Chat.sendPM(plcmd, "Auto sorting switched off!");
 								saveConfig();
 								return true;
 							}
 						}
 						//nope!
 						Chat.sendPM(plcmd, "Your permissions do not allow you to sort chests!");
 						return true;
 					}
 					//reload all config
 					if(args[0].equalsIgnoreCase("reload"))
 					{
 						if(plcmd.isOp() || plcmd.hasPermission("magicchest.reload"))
 						{
 							reloadConfig();
 							Chat.sendPM(plcmd, "Reloaded configuration!");
 							return true;
 						}
 						//y u no op?!
 						Chat.sendPM(plcmd, "Your permissions do not allow you to reload the config!");
 						return false;
 					}
 					//theres no command with that name!
 					Chat.sendPM(plcmd, "Unrecognized command: " + cmd.getName());
 					return false;
 				}
 				//version # and stuff like that
 				Chat.sendPM(plcmd, "MagicChest v" + getDescription().getVersion().toString());
 				Chat.sendPM(plcmd, "Authors: " + getDescription().getAuthors().toArray()[0] + " - founder and lead dev, " + getDescription().getAuthors().toArray()[1] + " - coauthor and developer");
 				Chat.sendPM(plcmd, "Type /mgcs help to see all commands.");
 				return true;
 			}
 			//you must be a robot!
 			sender.sendMessage(ChatColor.GREEN + "[MagicChest] " + ChatColor.RESET + "You have to be a player to run this command!");
 			return false;
 		}
 		return true;
 	}
 }
