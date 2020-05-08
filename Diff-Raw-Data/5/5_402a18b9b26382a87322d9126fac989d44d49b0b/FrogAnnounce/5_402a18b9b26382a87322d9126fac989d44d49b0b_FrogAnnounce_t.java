 package main.java.me.thelunarfrog.FrogAnnounce;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Random;
 
 import net.milkbowl.vault.permission.Permission;
 
 import org.bukkit.ChatColor;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.configuration.InvalidConfigurationException;
 import org.bukkit.entity.Player;
 import org.bukkit.plugin.Plugin;
 import org.bukkit.plugin.PluginDescriptionFile;
 import org.bukkit.plugin.RegisteredServiceProvider;
 import org.bukkit.plugin.java.JavaPlugin;
 
 /**
  * The FrogAnnounce core. Handles loops, grabbing configuration values from ConfigurationManager, commands, and all announcements. API will be found here, too.
  * 
  * @author Dan | TheLunarFrog
  * @version 2.0.10.13
  * @category main
  * 
  */
 public class FrogAnnounce extends JavaPlugin{
 	private PluginDescriptionFile pdfFile;
 	protected FrogLog logger = new FrogLog();
 	public static Permission permission = null;
 	protected static String tag;
 	protected static int interval, taskId = -1, counter = 0;
 	protected static boolean running = false, random, permissionsEnabled = false, toGroups, usingPerms;
 	protected static List<String> strings, Groups;
 	protected static ArrayList<String> ignoredPlayers = null;
 	public static FrogAnnounce plugin;
 
 	@Override
 	public void onEnable(){
 		plugin = this;
 		pdfFile = this.getDescription();
 		try{
 			ConfigurationHandler.loadConfig();
 		}catch(Exception e){
 			e.printStackTrace();
 		}
 		if(usingPerms)
 			checkPermissionsVaultPlugins();
 		logger.info("Settings loaded "+strings.size()+" announcements!");
 		running = turnOn(null);
 		logger.info("Version "+pdfFile.getVersion()+" by TheLunarFrog has been enabled!");
 	}
 
 	@Override
 	public void onDisable(){
 		turnOff(true, null);
 		logger.info("Version "+pdfFile.getVersion()+" by TheLunarFrog has been disabled!");
 	}
 
 	private boolean permit(CommandSender player, String perm){
 		if(usingPerms){
 			return permission.has(player, perm);
 		}else{
 			return player.isOp();
 		}
 	}
 
 	private void turnOff(boolean disabled, CommandSender player){
 		if(running){
 			getServer().getScheduler().cancelTask(taskId);
 			sendMessage(player, 0, "Announcer disabled!");
 			running = false;
 		}else{
 			if(!disabled)
 				sendMessage(player, 2, "The announcer is not running!");
 		}
 	}
 
 	private boolean turnOn(CommandSender player){
 		if(!running){
 			if(strings.size()> 0){
 				taskId = getServer().getScheduler().scheduleAsyncRepeatingTask(this, new Announcer(), interval * 1200, interval * 1200);
 				if(taskId == -1){
 					sendMessage(player, 2, "The announcer module has failed to start! Please check your configuration. If this does not fix it, then submit a support ticket on the BukkitDev page for FrogAnnounce.");
 					return false;
 				}else{
 					counter = 0;
 					sendMessage(player, 0, "Success! Now announcing every "+interval+" minute(s)!");
 					return true;
 				}
 			}else{
 				sendMessage(player, 2, "The announcer failed to start! There are no announcements!");
 				return false;
 			}
 		}else{
 			sendMessage(player, 2, ChatColor.DARK_RED+"Announcer is already running.");
 			return true;
 		}
 	}
 
 	private void reloadPlugin(CommandSender player){
 		if(running){
 			turnOff(false, null);
 			try{
 				ConfigurationHandler.loadConfig();
 			}catch(InvalidConfigurationException e){
 				e.printStackTrace();
 			}
 			running = turnOn(player);
 			sendMessage(player, 0, "FrogAnnounce has been successfully reloaded!");
 			sendMessage(player, 0, "Settings loaded "+strings.size()+" announcements!");
 		}else
 			sendMessage(player, 2, "No announcements running!");
 	}
 
 	@Override
 	public boolean onCommand(CommandSender sender, Command cmd,
 			String commandLabel, String[] args){
 		if(commandLabel.equalsIgnoreCase("fa") || commandLabel.equalsIgnoreCase("frogannounce")){
 			if(permit(sender, "frogannounce.admin") || permit(sender, "frogannounce.*")){
 				try{
 					if(args.length == 0){
 						sendMessage(sender, 0, "FrogAnnounce version: "+pdfFile.getVersion());
 						sendMessage(sender, 0, "For help, use /fa help.");
 					}else if(args[0].equalsIgnoreCase("help") || args[0].equalsIgnoreCase("?")){
 						if(args.length == 2)
 							returnHelp(sender, args[1]);
 						else
 							returnHelp(sender, "0");
 					}else if(args[0].equalsIgnoreCase("on"))
 						running = turnOn(sender);
 					else if(args[0].equalsIgnoreCase("off"))
 						turnOff(false, sender);
 					else if(args[0].equalsIgnoreCase("version") || args[0].equalsIgnoreCase("v"))
 						sendMessage(sender, 0, "Current version: "+pdfFile.getVersion());
 					else if(args[0].equalsIgnoreCase("ignore") || args[0].equalsIgnoreCase("optout") || args[0].equalsIgnoreCase("opt-out"))
 						ignorePlayer(sender, args[1]);
 					else if(args[0].equalsIgnoreCase("unignore") || args[0].equalsIgnoreCase("optin") || args[0].equalsIgnoreCase("opt-in"))
 						unignorePlayer(sender, args[1]);
 					else if(args[0].equalsIgnoreCase("interval") || args[0].equalsIgnoreCase("int"))
 						setInterval(args, sender);
 					else if(args[0].equalsIgnoreCase("random") || args[0].equalsIgnoreCase("rand"))
 						setRandom(args, sender);
 					else if(args[0].equalsIgnoreCase("broadcast") || args[0].equalsIgnoreCase("bc")){
 						broadcastMessage(args[1], sender);
 					}else if(args[0].equalsIgnoreCase("restart") || args[0].equalsIgnoreCase("reload")){
 						reloadPlugin(sender);
 						reloadConfig();
 					}else if(args[0].equalsIgnoreCase("list")){
 						sendMessage(sender, 0, "Loaded announcements:");
 						for (String s : strings){
 							sendMessage(sender, 0, strings.indexOf(s)+". "+colourizeText(s));
 						}
 					}else if(args[0].equalsIgnoreCase("add")){
 						StringBuilder sb = new StringBuilder();
 						for (int i = 1; i < args.length; i++){
 							sb.append(args[i]+" ");
 						}
 						strings.add(sb.toString().trim());
 						ConfigurationHandler.Settings.set("Announcer.Strings", strings);
 						ConfigurationHandler.save();
 						sendMessage(sender, 0, "Successfully added the announcement \""+sb.toString().trim()+"\" to the configuration. Reloading config...");
 						reloadPlugin(sender);
 					}else if(args[0].equalsIgnoreCase("manualbroadcast") || args[0].equalsIgnoreCase("mbc")){
 						StringBuilder sb = new StringBuilder();
 						for (int i = 1; i < args.length; i++){
 							sb.append(args[i]+" ");
 						}
 						if(tag.isEmpty())
 							getServer().broadcastMessage(colourizeText(sb.toString().trim()));
 						else
 							getServer().broadcastMessage(tag+" "+colourizeText(sb.toString().trim()));
 					}else if(args[0].equalsIgnoreCase("remove") || args[0].equalsIgnoreCase("delete") || args[0].equalsIgnoreCase("rem") || args[0].equalsIgnoreCase("del")){
 						int i = 0;
 						if(args.length == 2){
 							try{
 								i = Integer.parseInt(args[1]);
 								try{
 									sendMessage(sender, 0, "Removing announcement "+i+" ("+strings.get(i)+")...");
 									strings.remove(i);
 									ConfigurationHandler.Settings.set("Announcer.Strings", strings);
 									ConfigurationHandler.save();
 									sendMessage(sender, 0, "Announcement "+i+" successfully removed. Reloading configuration...");
 									reloadPlugin(sender);
 								}catch(IndexOutOfBoundsException e){
 									sendMessage(sender, 1, "Error: There are only "+strings.size()+" announcements. You must count from 0!");
 								}
 							}catch(NumberFormatException e){
 								sendMessage(sender, 1, "Please enter an announcement index.");
 							}
 						}else{
 							sendMessage(sender, 1, "You must specify an index to remove.");
 						}
 					}else{
 						sendMessage(sender, 1, "That didn't seem like a valid command. Here's some help...");
 						if(args.length == 2)
 							returnHelp(sender, args[1]);
 						else
 							returnHelp(sender, "0");
 					}
 					return true;
 				}catch(ArrayIndexOutOfBoundsException e){
 					return false;
 				}
			}else if(args.length > 1){
				if(args[0].equalsIgnoreCase("ignore") || args[0].equalsIgnoreCase("optout") || args[0].equalsIgnoreCase("opt-out")){
 					if(permit(sender, "frogannounce.optout")){
 						if(args.length == 2)
 							ignorePlayer(sender, args[1]);
 						else
 							ignorePlayer(sender, sender.getName());
 					}else
 						sendMessage(sender, 1, "You don't have permission to access that command.");
 				}else if(args[0].equalsIgnoreCase("unignore") || args[0].equalsIgnoreCase("optin") || args[0].equalsIgnoreCase("opt-in")){
 					if(permit(sender, "frogannounce.optin")){
 						if(args.length == 2)
 							ignorePlayer(sender, args[1]);
 						else
 							ignorePlayer(sender, sender.getName());
 					}else
 						sendMessage(sender, 1, "You don't have permission to access that command.");
 				}
 			}
 		}
 		return false;
 	}
 
 	public void returnHelp(CommandSender sender, String pageString){
 		String or = ChatColor.WHITE.toString()+"|";
 		String auctionStatusColor = ChatColor.DARK_GREEN.toString();
 		String helpMainColor = ChatColor.GOLD.toString();
 		String helpCommandColor = ChatColor.AQUA.toString();
 		String helpObligatoryColor = ChatColor.DARK_RED.toString();
 		try{
 			int page;
 			page = Integer.parseInt(pageString);
 			if(page == 1 || page == 0){
 				sendMessage(sender, 0, helpMainColor+"*"+auctionStatusColor+"Help for FrogAnnounce "+pdfFile.getVersion()+" (1/2)"+helpMainColor+"*");
 				sendMessage(sender, 0, helpCommandColor+"/fa <help"+or+helpCommandColor+"?>"+helpMainColor+" - Show this message.");
 				sendMessage(sender, 0, helpCommandColor+"/fa <on"+or+helpCommandColor+"off>"+helpMainColor+" - Start or stop FrogAnnounce.");
 				sendMessage(sender, 0, helpCommandColor+"/fa <restart"+or+helpCommandColor+"reload>"+helpMainColor+" - Restart FrogAnnounce.");
 				sendMessage(sender, 0, helpCommandColor+"/fa <interval"+or+helpCommandColor+"int>"+helpObligatoryColor+" <minutes>"+helpMainColor+" - Set the time between each announcement.");
 				sendMessage(sender, 0, helpCommandColor+"/fa <random"+or+helpCommandColor+"rand>"+helpObligatoryColor+" <on"+or+helpObligatoryColor+"off>"+helpMainColor+" - Set random or consecutive.");
 				sendMessage(sender, 0, helpCommandColor+"/fa <broadcast"+or+helpCommandColor+"bc>"+helpObligatoryColor+"<AnnouncementIndex>"+helpMainColor+" - Announces the announcement specified by the index immediately. Will not interrupt the normal order/time. Please note that this starts at 0.");
 				sendMessage(sender, 0, ChatColor.GOLD+"Use /fa help 2 to see the next page.");
 			}else if(page == 2){
 				sendMessage(sender, 0, helpMainColor+"*"+auctionStatusColor+"Help for FrogAnnounce "+pdfFile.getVersion()+" (2/2)"+helpMainColor+"*");
 				sendMessage(sender, 0, helpCommandColor+"/fa <add "+or+helpCommandColor+"add> "+helpObligatoryColor+"<announcement message>"+helpMainColor+" - Adds an announcement to the list. (Command /faadd or /fa-add is not a typo; technical restrictions forced this.)");
 				sendMessage(sender, 0, helpCommandColor+"/fa <remove "+or+helpCommandColor+"delete"+or+helpCommandColor+"rem"+or+helpCommandColor+"del> "+helpObligatoryColor+"<announcementIndex>"+helpMainColor+" - Removes the specified announcement (announcementIndex = announcement number from top to bottom in the file; starts at 0).");
 				sendMessage(sender, 0, helpCommandColor+"/fa <manualbroadcast"+or+helpCommandColor+"mbc"+helpObligatoryColor+"<Message>"+helpMainColor+" - Announces a message to the entire server. Ignores groups in the config.");
 			}else
 				sendMessage(sender, 0, "There's no page "+page+".");
 		}catch(NumberFormatException e){
 			sendMessage(sender, 0, "You must specify a page - positive integers only.");
 		}
 	}
 
 	protected static String colourizeText(String announce){
 		announce = announce.replaceAll("&AQUA;",		ChatColor.AQUA.toString());
 		announce = announce.replaceAll("&BLACK;",		ChatColor.BLACK.toString());
 		announce = announce.replaceAll("&BLUE;",		ChatColor.BLUE.toString());
 		announce = announce.replaceAll("&DARK_AQUA;",	ChatColor.DARK_AQUA.toString());
 		announce = announce.replaceAll("&DARK_BLUE;",	ChatColor.DARK_BLUE.toString());
 		announce = announce.replaceAll("&DARK_GRAY;",	ChatColor.DARK_GRAY.toString());
 		announce = announce.replaceAll("&DARK_GREEN;",	ChatColor.DARK_GREEN.toString());
 		announce = announce.replaceAll("&DARK_PURPLE;",	ChatColor.DARK_PURPLE.toString());
 		announce = announce.replaceAll("&RED;",			ChatColor.RED.toString());
 		announce = announce.replaceAll("&DARK_RED;",	ChatColor.DARK_RED.toString());
 		announce = announce.replaceAll("&GOLD;",		ChatColor.GOLD.toString());
 		announce = announce.replaceAll("&GRAY;",		ChatColor.GRAY.toString());
 		announce = announce.replaceAll("&GREEN;",		ChatColor.GREEN.toString());
 		announce = announce.replaceAll("&LIGHT_PURPLE;",ChatColor.LIGHT_PURPLE.toString());
 		announce = announce.replaceAll("&PURPLE;",		ChatColor.LIGHT_PURPLE.toString());
 		announce = announce.replaceAll("&PINK;",		ChatColor.LIGHT_PURPLE.toString());
 		announce = announce.replaceAll("&WHITE;",		ChatColor.WHITE.toString());
 		announce = announce.replaceAll("&b;",			ChatColor.AQUA.toString());
 		announce = announce.replaceAll("&0;",			ChatColor.BLACK.toString());
 		announce = announce.replaceAll("&9;",			ChatColor.BLUE.toString());
 		announce = announce.replaceAll("&3;",			ChatColor.DARK_AQUA.toString());
 		announce = announce.replaceAll("&1;",			ChatColor.DARK_BLUE.toString());
 		announce = announce.replaceAll("&8;",			ChatColor.DARK_GRAY.toString());
 		announce = announce.replaceAll("&2;",			ChatColor.DARK_GREEN.toString());
 		announce = announce.replaceAll("&5;",			ChatColor.DARK_PURPLE.toString());
 		announce = announce.replaceAll("&4;",			ChatColor.DARK_RED.toString());
 		announce = announce.replaceAll("&6;",			ChatColor.GOLD.toString());
 		announce = announce.replaceAll("&7;",			ChatColor.GRAY.toString());
 		announce = announce.replaceAll("&a;",			ChatColor.GREEN.toString());
 		announce = announce.replaceAll("&d;",			ChatColor.LIGHT_PURPLE.toString());
 		announce = announce.replaceAll("&c;",			ChatColor.RED.toString());
 		announce = announce.replaceAll("&f;",			ChatColor.WHITE.toString());
 		announce = announce.replaceAll("&e;",			ChatColor.YELLOW.toString());
 		announce = announce.replaceAll("&k;",			ChatColor.MAGIC.toString());
 		announce = announce.replaceAll("&MAGIC;",		ChatColor.MAGIC.toString());
 		announce = announce.replaceAll("&BOLD;",		ChatColor.BOLD.toString());
 		announce = announce.replaceAll("&ITALIC;",		ChatColor.ITALIC.toString());
 		announce = announce.replaceAll("&STRIKE;",		ChatColor.STRIKETHROUGH.toString());
 		announce = announce.replaceAll("&UNDERLINE;",	ChatColor.UNDERLINE.toString());
 		announce = announce.replaceAll("&RESET;",		ChatColor.RESET.toString());
 		return announce;
 	}
 
 	protected void broadcastMessage(String s, CommandSender player){
 		int _int = 0;
 		try{
 			_int = Integer.parseInt(s);
 			if(_int > strings.size()- 1){
 				sendMessage(player, 1, "You specified a number that does not correspond to any of the announcements in the file. Remember: it starts at 0! Operation aborted.");
 			}else{
 				try{
 					for (String line : strings.get(_int).split("&NEW_LINE;")){
 						if(tag.equals("")|| tag.isEmpty())
 							getServer().broadcastMessage(colourizeText(line));
 						else
 							getServer().broadcastMessage(tag+" "+colourizeText(line));
 					}
 					sendMessage(player, 0, "Successfully forced the announcement.");
 				}catch(NumberFormatException e){
 					sendMessage(player, 1, "Error. No letters or symtbols; only numbers. Try this format: "+ChatColor.DARK_RED+"/fa bc 5 (for more help, consult /fa help).");
 				}
 			}
 		}catch(NumberFormatException e){
 			sendMessage(player, 1, "Only numbers can be entered as an index. Remember to start counting at 0.");
 		}
 	}
 
 	protected Boolean setupPermissions(){
 		RegisteredServiceProvider<Permission> permissionProvider = super.getServer().getServicesManager().getRegistration(net.milkbowl.vault.permission.Permission.class);
 		if(permissionProvider != null){
 			permission = permissionProvider.getProvider();
 		}
 		return (permission != null);
 	}
 
 	private void setRandom(String[] args, CommandSender player){
 		boolean s = (boolean)Boolean.parseBoolean(args[1]);
 		if(s != random){
 			random = s;
 			ConfigurationHandler.Settings.set("Settings.Random", s);
 			if(s == true)
 				sendMessage(player, 0, "Announcer has been successfully changed to announce randomly. Reloading configuration...");
 			else
 				sendMessage(player, 0, "Announcer has been successfully changed to announce in sequence. Reloading configuration...");
 			ConfigurationHandler.save();
 			reloadPlugin(player);
 		}else{
 			if(random == true)
 				sendMessage(player, 1, "The announcer is already set to announce randomly! There's no need to change it!");
 			else
 				sendMessage(player, 1, "The announcer is already set to not announce randomly! There's no need to change it!");
 		}
 	}
 
 	private void setInterval(String[] cmdArgs, CommandSender player){
 		int newInterval = (int)Integer.parseInt(cmdArgs[1]);
 		if(newInterval != interval){
 			interval = newInterval;
 			ConfigurationHandler.Settings.set("Settings.Interval", interval);
 			ConfigurationHandler.save();
 			sendMessage(player, 0, "Announcement interval has successfully been changed to "+interval+". Reloading configuration...");
 			reloadPlugin(player);
 		}else
 			sendMessage(player, 1, "The announcement interval is already set to "+interval+"! There's no need to change it!");
 	}
 
 	public void checkPermissionsVaultPlugins(){
 		Plugin vault = this.getServer().getPluginManager().getPlugin("Vault");
 		if(vault != null){
 			if(setupPermissions()!= null){
 				logger.info("Vault hooked successfully.");
 				usingPerms = true;
 			}else if(setupPermissions()== null){
 				logger.info("Vault wasn't found. Defaulting to OP/Non-OP system.");
 				usingPerms = false;
 			}
 		}else
 			logger.warning("Vault is not in your plugins directory! This plugin has a soft dependency of Vault, but if you don't have it, this will still work (you just can't use permission-based stuff).");
 	}
 
 	private void ignorePlayer(CommandSender player, String other){
 		Player otherPlayer = getServer().getPlayer(other);
 		if(other.equals(player.getName()))
 			otherPlayer = (Player)player;
 		else
 			otherPlayer = getServer().getPlayer(other);
 		if(otherPlayer != null && otherPlayer == player){
 			if(permit(player, "frogannounce.ignore")){
 				if(!ignoredPlayers.contains(player.getName())){
 					ignoredPlayers.add(otherPlayer.getName());
 					ConfigurationHandler.Settings.set("ignoredPlayers", ignoredPlayers);
 					try{
 						ConfigurationHandler.Settings.save(ConfigurationHandler.configFile);
 						sendMessage(otherPlayer, 0, ChatColor.GRAY+"You are now being ignored by FrogAnnounce. You will no longer receive announcements from it until you opt back in.");
 					}catch(IOException e){
 						e.printStackTrace();
 					}
 				}else{
 					sendMessage(player, 1, "That player is already being ignored.");
 				}
 			}else{
 				sendMessage(player, 1, "You don't have sufficient permission to opt another player out of FrogAnnounce's announcements. Sorry!");
 			}
 		}else if(otherPlayer != null && otherPlayer != player){
 			if(permit(player, "frogannounce.ignore.other")){
 				if(!ignoredPlayers.contains(otherPlayer.getName())){
 					ignoredPlayers.add(otherPlayer.getName());
 					ConfigurationHandler.Settings.set("ignoredPlayers", ignoredPlayers);
 					try{
 						ConfigurationHandler.Settings.save(ConfigurationHandler.configFile);
 						sendMessage(player, 0, "Success! The player has been added to FrogAnnounce's ignore list and will no longer see its announcements until he/she opts back in.");
 						sendMessage(otherPlayer, 0, ChatColor.GRAY+"You are now being ignored by FrogAnnounce. You will no longer receive announcements from it until you opt back in.");
 					}catch(IOException e){
 						e.printStackTrace();
 					}
 				}else{
 					sendMessage(player, 1, "You're already being ignored by FrogAnnounce.");
 				}
 			}else{
 				sendMessage(player, 1, "You don't have sufficient permission to opt another player out of FrogAnnounce's announcements. Sorry!");
 			}
 		}else{
 			sendMessage(player, 1, "That player isn't online right now.");
 		}
 	}
 
 	private void unignorePlayer(CommandSender player, String other){
 		Player otherPlayer;
 		if(other.isEmpty())
 			otherPlayer = (Player)player;
 		else
 			otherPlayer = getServer().getPlayer(other);
 		if(otherPlayer != null && otherPlayer == player){
 			if(permit(player, "frogannounce.unignore")){
 				if(ignoredPlayers.contains(player.getName())){
 					ignoredPlayers.remove(otherPlayer.getName());
 					ConfigurationHandler.Settings.set("ignoredPlayers", ignoredPlayers);
 					try{
 						ConfigurationHandler.Settings
 						.save(ConfigurationHandler.configFile);
 						sendMessage(otherPlayer, 0, ChatColor.GRAY+"You are no longer being ignored by FrogAnnounce. You will receive announcements until you opt out of them again.");
 					}catch(IOException e){
 						e.printStackTrace();
 					}
 				}else
 					sendMessage(player, 1, "You're already not being ignored.");
 			}else
 				sendMessage(player, 1, "You don't have sufficient permission to opt another player back into FrogAnnounce's announcements. Sorry!");
 		}else if(otherPlayer != null && otherPlayer != player){
 			if(permit(player, "frogannounce.unignore.other")){
 				if(ignoredPlayers.contains(otherPlayer.getName())){
 					ignoredPlayers.remove(otherPlayer.getName());
 					ConfigurationHandler.Settings.set("ignoredPlayers", ignoredPlayers);
 					try{
 						ConfigurationHandler.Settings.save(ConfigurationHandler.configFile);
 						sendMessage(player, 0, "Success! The player has been removed from FrogAnnounce's ignore list and will see its announcements again until he/she opts out again.");
 						sendMessage(otherPlayer, 0, ChatColor.GRAY+"You are no longer being ignored by FrogAnnounce. You will receive announcements until you opt out of them again.");
 					}catch(IOException e){
 						e.printStackTrace();
 					}
 				}else
 					sendMessage(player, 1, "That player is already not being ignored.");
 			}
 		}else
 			sendMessage(player, 1, "That player isn't online right now!");
 	}
 
 	protected void sendMessage(CommandSender sender, int severity, String message){
 		if(sender instanceof Player){
 			if(severity == 0)
 				sender.sendMessage(ChatColor.DARK_GREEN+"[FrogAnnounce] "+ChatColor.GREEN+message);
 			else if(severity == 1)
 				sender.sendMessage(ChatColor.DARK_GREEN+"[FrogAnnounce] "+ChatColor.RED+message);
 			else if(severity == 2)
 				sender.sendMessage(ChatColor.DARK_GREEN+"[FrogAnnounce] "+ChatColor.DARK_RED+message);
 		}else{
 			if(severity == 0)
 				logger.info(message);
 			else if(severity == 1)
 				logger.warning(message);
 			else if(severity == 2)
 				logger.severe(message);
 		}
 	}
 
 	protected void sendMessage(Player player, int severity, String message){
 		if(player != null){
 			if(severity == 0)
 				player.sendMessage(ChatColor.DARK_GREEN+"[FrogAnnounce] "+ChatColor.GREEN+message);
 			else if(severity == 1)
 				player.sendMessage(ChatColor.DARK_GREEN+"[FrogAnnounce] "+ChatColor.RED+message);
 			else if(severity == 2)
 				player.sendMessage(ChatColor.DARK_GREEN+"[FrogAnnounce] "+ChatColor.DARK_RED+message);
 		}else{
 			if(severity == 0)
 				logger.info(message);
 			else if(severity == 1)
 				logger.warning(message);
 			else if(severity == 2)
 				logger.severe(message);
 		}
 	}
 
 	class Announcer implements Runnable {
 		@Override
 		public void run(){
 			String announce = "";
 			if(random){
 				Random randomise = new Random();
 				int selection = randomise.nextInt(strings.size());
 				announce = strings.get(selection);
 			}else{
 				announce = strings.get(counter);
 				counter++;
 				if(counter >= strings.size())
 					counter = 0;
 			}
 			if(!announce.startsWith("&USE-CMD;")){
 				if(usingPerms && toGroups){
 					Player[] players = getServer().getOnlinePlayers();
 					for (Player p: players){
 						for (String group: Groups){
 							if(permission.playerInGroup(p.getWorld().getName(), p.getName(), group) && !ignoredPlayers.contains(p.getName())){
 								for (String line : announce.split("&NEW_LINE;")){
 									if(ignoredPlayers.contains(p.getName())){
 										if(tag.equals("")|| tag.equals(" ") || tag.isEmpty())
 											p.sendMessage(colourizeText(line));
 										else
 											p.sendMessage(tag+" "+colourizeText(line));
 									}
 								}
 							}
 						}
 					}
 				}else{
 					Player[] onlinePlayers = getServer().getOnlinePlayers();
 					for (Player p : onlinePlayers){
 						for (String line : announce.split("&NEW_LINE;")){
 							if(!ignoredPlayers.contains(p.getName())){
 								if(tag.equals("") || tag.equals(" ") || tag.isEmpty())
 									p.sendMessage(colourizeText(line));
 								else
 									p.sendMessage(tag+" "+colourizeText(line));
 							}
 						}
 					}
 				}
 			}else{
 				announce = announce.replace("&USE-CMD;", "/");
 				getServer().dispatchCommand(getServer().getConsoleSender(), announce);
 			}
 		}
 	}
 }
