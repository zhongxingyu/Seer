 package com.github.winneonsword.JM;
 
 import java.util.List;
 
 //import net.milkbowl.vault.permission.Permission;




 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 //import org.bukkit.plugin.Plugin;
 
 import org.xeustechnologies.googleapi.spelling.SpellChecker;
 import org.xeustechnologies.googleapi.spelling.SpellCorrection;
 import org.xeustechnologies.googleapi.spelling.SpellResponse;
 
 import static com.github.winneonsword.CMAPI.API.ChatAPI.*;
 
 public class CommandJM implements CommandExecutor{
 	
 	private MainJM plugin;
 	//private static Permission perm;
 	
 	public CommandJM(MainJM plugin){
 		this.plugin = plugin;
 	}
 	
 	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args){
 		if(cmd.getName().equalsIgnoreCase("jm")){
 			String introMessage = plugin.ConfigJM.getString("introMessage");
 			String messageColour = plugin.ConfigJM.getString("messageColour");
 			String nameColour = plugin.ConfigJM.getString("nameColour");
 			Player player = (Player) sender;
 			//Plugin Vault = plugin.getServer().getPluginManager().getPlugin("Vault");
 			
 			if (args.length == 0){
 				// Name & version number.
 				sender.sendMessage(rA(introMessage + " &7Join Messages, v" + plugin.PluginJM.getString("version")));
 				return true;
 			}
 			if (args[0].equalsIgnoreCase("?") || args[0].equalsIgnoreCase("help")){
 				// The main help menu.
 				sender.sendMessage(rA(introMessage + " &7Join Messages Help Menu"));
 				sender.sendMessage(rA("  &c/jm ? &7- Help Menu."));
 				sender.sendMessage(rA("  &c/jm list &7- List Message Help Menu."));
 				sender.sendMessage(rA("  &c/jm add &7- Add Message Help Menu. &8// &cStaff command only!"));
 				sender.sendMessage(rA("  &c/jm remove &7- Remove Message Help Menu. &8// &cStaff command"));
 				sender.sendMessage(rA("      &conly!"));
 				sender.sendMessage(rA("  &c/jm reload &7- Reloads the config file. &8// &cStaff command only!"));
 				sender.sendMessage(rA("&7Created by WS, v" + plugin.PluginJM.getString("version")));
 				return true;
 			}
 			if (args[0].equalsIgnoreCase("list") && args.length == 1){
 				// The List Message Help Menu.
 				sender.sendMessage(rA(introMessage + " &7List Message Help Menu"));
 				sender.sendMessage(rA("  &c/jm list join &7- List the join messages and their"));
 				sender.sendMessage(rA("      &7corresponding ID."));
 				sender.sendMessage(rA("  &c/jm list leave &7- List the leave messages and their"));
 				sender.sendMessage(rA("      &7corresponding ID."));
 				return true;
 			}
 			if (args[0].equalsIgnoreCase("list") && args[1].equalsIgnoreCase("join")){
 				// The /jm list join command.
 				List<String> joinMessageList = plugin.ConfigJM.getStringList("joinMessages");
 				
 				sender.sendMessage(rA(introMessage + " &7Join Messages:"));
 				for (int i = 1; i < joinMessageList.size() + 1; i++){
 					String message = joinMessageList.get(i - 1);
 					//String group = "";
 					//if (Vault != null){
 					//	group = perm.getPrimaryGroup(player);
 					//}
 					sender.sendMessage(rA("  &7[&c" + i + "&7] &" + messageColour + message.replace("%p", "&" + nameColour + player.getDisplayName() + "&" + messageColour)));
 				}
 				return true;
 			}
 			if (args[0].equalsIgnoreCase("list") && args[1].equalsIgnoreCase("leave")){
 				// The /jm list leave command.
 				List<String> leaveMessageList = plugin.ConfigJM.getStringList("leaveMessages");
 				
 				sender.sendMessage(rA(introMessage + " &7Leave Messages:"));
 				for (int i = 1; i < leaveMessageList.size() + 1; i++){
 					String message = leaveMessageList.get(i - 1);
 					//String group = "";
 					//if (Vault != null){
 					//	group = perm.getPrimaryGroup(player);
 					//}
 					sender.sendMessage(rA("  &7[&c" + i + "&7] &" + messageColour + message.replace("%p", "&" + nameColour + player.getDisplayName() + "&" + messageColour)));
 				}
 				return true;
 			}
 			if (args[0].equalsIgnoreCase("add") && args.length == 1){
 				// The Add Message Help Menu.
 				sender.sendMessage(rA(introMessage + " &7Add Message Help Menu &8// &cStaff commands only!"));
 				sender.sendMessage(rA("  &c/jm add join <message> &7- Add a join message to the config."));
 				sender.sendMessage(rA("  &c/jm add leave <message> &7- Add a leave message to the config."));
 				return true;
 			}
 			if (args[0].equalsIgnoreCase("add") && args[1].equalsIgnoreCase("join")){
 				// The /jm add join <message> command.
 				if (player.hasPermission("joinmessages.add") || player.hasPermission("joinmessages.add.join")){
 					if (args.length == 2){
 						sender.sendMessage(rA(introMessage + " &7Usage: &c/jm add join <message>"));
 						return true;
 					}
 					StringBuilder initial = new StringBuilder();
 					
 					for (int i = 2; i < args.length; i++){
 						initial.append(' ').append(args[i]);
 					}
 					String message = initial.toString().replaceFirst(" ", "");
 					
 					if (!(message.contains("%p"))){
 						sender.sendMessage(rA(introMessage + " &cYou have not defined a playername! &7Be sure to include &c%p &7in your message to define the playername."));
 						return true;
 					}
 					List<String> joinMessageList = plugin.ConfigJM.getStringList("joinMessages");
 					int joinMessageID = joinMessageList.size() + 1;
 					
 					joinMessageList.add(message);
 					plugin.ConfigJM.set("joinMessages", joinMessageList);
 					plugin.saveYMLs();
 					sender.sendMessage(rA(introMessage + " &7Successfully added a join message. The message ID is &c" + joinMessageID + "&7."));
 					sender.sendMessage(rA(introMessage + " &7Here is what it will look like:"));
 					//String group = "";
 					//if (Vault != null){
 					//	group = perm.getPrimaryGroup(player);
 					//}
 					sender.sendMessage(rA("&" + messageColour + message.replace("%p", "&" + nameColour + player.getDisplayName() + "&" + messageColour)));
 					return true;
 				}
 				return true;
 			}
 			if (args[0].equalsIgnoreCase("add") && args[1].equalsIgnoreCase("leave")){
 				// The /jm add leave <message> command.
 				if (player.hasPermission("joinmessages.add") || player.hasPermission("joinmessages.add.leave")){
 					if (args.length == 2){
 						sender.sendMessage(rA(introMessage + " &7Usage: &c/jm add leave <message>"));
 						return true;
 					}
 					StringBuilder initial = new StringBuilder();
 					
 					for (int i = 2; i < args.length; i++){
 						initial.append(' ').append(args[i]);
 					}
 					String message = initial.toString().replaceFirst(" ", "");
 					
 					if (!(message.contains("%p"))){
 						sender.sendMessage(rA(introMessage + " &cYou have not defined a playername! &7Be sure to include &c%p &7in your message to define the playername."));
 						return true;
 					}
 					List<String> leaveMessageList = plugin.ConfigJM.getStringList("leaveMessages");
 					int leaveMessageID = leaveMessageList.size() + 1;
 					
 					leaveMessageList.add(message);
 					plugin.ConfigJM.set("leaveMessages", leaveMessageList);
 					plugin.saveYMLs();
 					sender.sendMessage(rA(introMessage + " &7Successfully added a leave message. The message ID is &c" + leaveMessageID + "&7."));
 					sender.sendMessage(rA(introMessage + " &7Here is what it will look like:"));
 					//String group = "";
 					//if (Vault != null){
 					//	group = perm.getPrimaryGroup(player);
 					//}
 					sender.sendMessage(rA("&" + messageColour + message.replace("%p", "&" + nameColour + player.getDisplayName() + "&" + messageColour)));
 					return true;
 				}
 				return true;
 			}
 			if (args[0].equalsIgnoreCase("remove") && args.length == 1){
 				// The Remove Message Help Menu.
 				sender.sendMessage(rA(introMessage + " &7Remove Message Help Menu &8// &cStaff commands only!"));
 				sender.sendMessage(rA("  &c/jm remove join <ID> &7- Remove a join message from the config."));
 				sender.sendMessage(rA("  &c/jm remove leave <ID> &7- Remove a leave message from the"));
 				sender.sendMessage(rA("      &7config."));
 				sender.sendMessage(rA("&7To view message IDs, type &c/jm list&7."));
 				return true;
 			}
 			if (args[0].equalsIgnoreCase("remove") && args[1].equalsIgnoreCase("join")){
 				// The /jm remove join <message> command.
 				if (player.hasPermission("joinmessages.remove") || player.hasPermission("joinmessages.remove.join")){
 					if (args.length == 2){
 						sender.sendMessage(rA(introMessage + " &7Usage: &c/jm remove join <ID>&7. To view message IDs, type &c/jm list&7."));
 						return true;
 					}
 					List<String> joinMessageList = plugin.ConfigJM.getStringList("joinMessages");
 					try {
 						Integer.parseInt(args[2]);
 					} catch (NumberFormatException e){
 						sender.sendMessage(rA(introMessage + " &cThe ID you entered is not a number!"));
 						return true;
 					}
 					if (joinMessageList.size() < Integer.parseInt(args[2])){
 						sender.sendMessage(rA(introMessage + " &cThere is no join message with the ID specified!"));
 						return true;
 					}
 					if (joinMessageList.size() == 1){
 						sender.sendMessage(rA(introMessage + " &cThere is only 1 message left! Add another before you remove this message."));
 						return true;
 					}
 					int joinMessageID = Integer.parseInt(args[2]);
 					String message = joinMessageList.get(joinMessageID - 1);
 					
 					joinMessageList.remove(message);
 					plugin.ConfigJM.set("joinMessages", joinMessageList);
 					plugin.saveYMLs();
 					sender.sendMessage(rA(introMessage + " &7Successfully removed the join message which goes by the ID of &c" + joinMessageID + "&7."));
 					sender.sendMessage(rA(introMessage + " &7Here is the message you removed:"));
 					//String group = "";
 					///if (Vault != null){
 					//	group = perm.getPrimaryGroup(player);
 					//}
 					sender.sendMessage(rA("&" + messageColour + message.replace("%p", "&" + nameColour + player.getDisplayName() + "&" + messageColour)));
 					return true;
 				}
 				return true;
 			}
 			if (args[0].equalsIgnoreCase("remove") && args[1].equalsIgnoreCase("leave")){
 				// The /jm remove join <message> command.
 				if (player.hasPermission("joinmessages.remove") || player.hasPermission("joinmessages.remove.leave")){
 					if (args.length == 2){
 						sender.sendMessage(rA(introMessage + " &7Usage: &c/jm remove leave <ID>&7. To view message IDs, type &c/jm list&7."));
 						return true;
 					}
 					List<String> leaveMessageList = plugin.ConfigJM.getStringList("leaveMessages");
 					try {
 						Integer.parseInt(args[2]);
 					} catch (NumberFormatException e){
 						sender.sendMessage(rA(introMessage + " &cThe ID you entered is not a number!"));
 						return true;
 					}
 					if (leaveMessageList.size() < Integer.parseInt(args[2])){
 						sender.sendMessage(rA(introMessage + " &cThere is no leave message with the ID specified!"));
 						return true;
 					}
 					if (leaveMessageList.size() == 1){
 						sender.sendMessage(rA(introMessage + " &cThere is only 1 message left! Add another before you remove this message."));
 						return true;
 					}
 					int leaveMessageID = Integer.parseInt(args[2]);
 					String message = leaveMessageList.get(leaveMessageID - 1);
 					
 					leaveMessageList.remove(message);
 					plugin.ConfigJM.set("leaveMessages", leaveMessageList);
 					plugin.saveYMLs();
 					sender.sendMessage(rA(introMessage + " &7Successfully removed the leave message which goes by the ID of &c" + leaveMessageID + "&7."));
 					sender.sendMessage(rA(introMessage + " &7Here is the message you removed:"));
 					//String group = "";
 					//if (Vault != null){
 					//	group = perm.getPrimaryGroup(player);
 					//}
 					sender.sendMessage(rA("&" + messageColour + message.replace("%p", "&" + nameColour + player.getDisplayName() + "&" + messageColour)));
 					return true;
 				}
 				return true;
 			}
 			if (args[0].equalsIgnoreCase("spell")){
 				SpellChecker check = new SpellChecker();
 				StringBuilder initial = new StringBuilder();
 				
 				for (int i = 1; i < args.length; i++){
 					initial.append(' ').append(args[i]);
 				}
 				SpellResponse message = check.check(initial.toString().replaceFirst(" ", ""));
 				SpellCorrection[] sc = message.getCorrections();
 				sender.sendMessage(sc[0].toString());
 			}
 			if (args[0].equalsIgnoreCase("reload")){
 				// The reload command.
 				if (player.hasPermission("joinmessages.reload")){
 					plugin.loadYMLs();
 					sender.sendMessage(rA(introMessage + " &7Successfully reloaded the config file."));
 					return true;
 				}
 			}
 			sender.sendMessage(rA(introMessage + " &cUnknown argument. &7Type &c/jm ? &7for command usage."));
 			return true;
 		}
 		return false;
 	}
 	
 }
