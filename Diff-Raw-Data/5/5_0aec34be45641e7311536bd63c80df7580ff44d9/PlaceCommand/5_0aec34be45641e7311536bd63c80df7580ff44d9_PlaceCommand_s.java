 package net.kiwz.ThePlugin.commands;
 
 import net.kiwz.ThePlugin.ThePlugin;
 import net.kiwz.ThePlugin.utils.HandlePlaces;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 public class PlaceCommand {
 	
 	/**
 	 * 
 	 * @param sender player who issued the command
 	 * @param cmd command that was issued
 	 * @param args as String[] describing what we want to do with given place
 	 * @return true no matter what
 	 */
 	public boolean place(CommandSender sender, Command cmd, String[] args) {
 		HandlePlaces hPlaces = new HandlePlaces();
 		HelpCommand help = new HelpCommand();
 		Player player = Bukkit.getPlayer(sender.getName());
 		int id;
 		
 		if (!(sender instanceof Player)) {
 			int check = 0;
 			if (args.length == 0) {
 				check = 1;
 			}
 			if((args.length == 1 || args.length == 2) && args[0].equalsIgnoreCase("spiller")) {
 				check = 1;
 			}
 			if (args.length == 1 && !args[0].equalsIgnoreCase("her")) {
 				check = 1;
 			}
 			if (check == 0) {
 				sender.sendMessage(ThePlugin.c2 + "Consolen kan bare bruke /plass, /plass liste, /plass spiller <navn> og /plass <navn>");
 				return true;
 			}
 		}
 		
 		for (String arg : args) {
 			if (!arg.matches("[a-zA-Z-_0-9]+")) {
 				sender.sendMessage(ThePlugin.c2 + "Tillatte tegn er a- A- 0-9 - _");
 				return true;
 			}
 		}
 		
 		if (args.length == 0) {
 			help.customHelp(sender, cmd.getName(), "1", help());
 			return true;
 		}
 		
 		else if (args.length == 1) {
 			if (args[0].equalsIgnoreCase("liste")) {
 				hPlaces.sendPlaceList(sender);
 				return true;
 			}
 			else if (args[0].equalsIgnoreCase("spiller")) {
 				hPlaces.sendPlayersPlaceList(sender);
 				return true;
 			}
 			else if (args[0].equalsIgnoreCase("her")) {
 				hPlaces.sendPlaceHere(sender);
 				return true;
 			}
 			else if (args[0].length() == 1) {
 				help.customHelp(sender, cmd.getName(), args[0], help());
 				return true;
 			}
 			else {
 				id = hPlaces.getID(args[0]);
 				if (id == 0) {
 					sender.sendMessage(ThePlugin.c2 + args[0] + " finnes ikke");
 					return true;
 				}
 				hPlaces.sendPlace(sender, id);
 				return true;
 			}
 		}
 		
 		if (args.length == 2) {
 			if (args[0].equalsIgnoreCase("spiller")) {
 				sender.sendMessage(hPlaces.getOwned(args[1]));
 				sender.sendMessage(hPlaces.getMembered(args[1]));
 				return true;
 			}
 			if (args[0].equalsIgnoreCase("ny")) {
 				sender.sendMessage(hPlaces.addPlace(player, args[1], "40"));
 				return true;
 			}
 			id = hPlaces.getID(args[1]);
 			if (id == 0) {
 				sender.sendMessage(ThePlugin.c2 + args[1] + " finnes ikke");
 				return true;
 			}
 			if (args[0].equalsIgnoreCase("flytt")) {
 				sender.sendMessage(hPlaces.setPlace(player, id, "40"));
 				return true;
 			}
 			else if (args[0].equalsIgnoreCase("spawn")) {
 				player.teleport(hPlaces.getSpawn(id));
 				return true;
 			}
 			else if (args[0].equalsIgnoreCase("setspawn")) {
 				sender.sendMessage(hPlaces.setSpawn(player, id));
 				return true;
 			}
 			else if (args[0].equalsIgnoreCase("slett")) {
 				sender.sendMessage(hPlaces.remPlace(player, id));
 				return true;
 			}
 			else if (args[0].equalsIgnoreCase("entre")) {
 				sender.sendMessage(hPlaces.setEnter(player, id, ""));
 				return true;
 			}
 			else if (args[0].equalsIgnoreCase("forlate")) {
 				sender.sendMessage(hPlaces.setLeave(player, id, ""));
 				return true;
 			}
 			else if (args[0].equalsIgnoreCase("navn")) {
 				sender.sendMessage(ThePlugin.c2 + "/plass navn <plass-navn> <nytt-plass-navn>");
 				return true;
 			}
 			else if (args[0].equalsIgnoreCase("toggle")) {
 				sender.sendMessage(ThePlugin.c2 + "/plass toggle <plass-navn> <[pvp] [monstre] [dyr]>");
 				return true;
 			}
 			else if (args[0].equalsIgnoreCase("medlem")) {
 				sender.sendMessage(ThePlugin.c2 + "/plass medlem <plass-navn> <spiller-navn>");
 				return true;
 			}
 			else if (args[0].equalsIgnoreCase("spark")) {
 				sender.sendMessage(ThePlugin.c2 + "/plass spark <plass-navn> <spiller-navn>");
 				return true;
 			}
 			else if (args[0].equalsIgnoreCase("eier")) {
 				sender.sendMessage(ThePlugin.c2 + "/plass eier <plass-navn> <spiller-navn>");
 				return true;
 			}
 			else {
 				help.customHelp(sender, cmd.getName(), args[0], help());
 				return true;
 			}
 		}
 		
 		else if (args.length == 3) {
 			if (args[0].equalsIgnoreCase("ny")) {
 				if (!args[2].matches("[0-9]+")) {
 					sender.sendMessage(ThePlugin.c2 + "Strrelsen m defineres med tall");
 					return true;
 				}
 				sender.sendMessage(hPlaces.addPlace(player, args[1], args[2]));
 				return true;
 			}
 			id = hPlaces.getID(args[1]);
 			if (id == 0) {
 				sender.sendMessage(ThePlugin.c2 + args[1] + " finnes ikke");
 				return true;
 			}
 			if (args[0].equalsIgnoreCase("flytt")) {
 				if (!args[2].matches("[0-9]+")) {
 					sender.sendMessage(ThePlugin.c2 + "Strrelsen m defineres med tall");
 					return true;
 				}
 				sender.sendMessage(hPlaces.setPlace(player, id, args[2]));
 				return true;
 			}
 			else if (args[0].equalsIgnoreCase("entre")) {
 				sender.sendMessage(hPlaces.setEnter(player, id, args[2]));
 				return true;
 			}
 			else if (args[0].equalsIgnoreCase("forlate")) {
 				sender.sendMessage(hPlaces.setLeave(player, id, args[2]));
 				return true;
 			}
 			else if (args[0].equalsIgnoreCase("navn")) {
 				sender.sendMessage(hPlaces.setName(player, id, args[2]));
 				return true;
 			}
 			else if (args[0].equalsIgnoreCase("toggle")) {
 				if (args[2].equalsIgnoreCase("pvp")) {
 					sender.sendMessage(hPlaces.setPvP(player, id));
 					return true;
 				}
 				else if (args[2].equalsIgnoreCase("monstre")) {
 					sender.sendMessage(hPlaces.setMonsters(player, id));
 					return true;
 				}
 				else if (args[2].equalsIgnoreCase("dyr")) {
 					sender.sendMessage(hPlaces.setAnimals(player, id));
 					return true;
 				}
 				else {
 					sender.sendMessage(ThePlugin.c2 + "Du m skrive pvp/monstre/dyr");
 					return true;
 				}
 			}
 			else if (args[0].equalsIgnoreCase("medlem")) {
 				sender.sendMessage(hPlaces.addMember(player, id, args[2]));
 				return true;
 			}
 			else if (args[0].equalsIgnoreCase("spark")) {
 				sender.sendMessage(hPlaces.remMember(player, id, args[2]));
 				return true;
 			}
 			else if (args[0].equalsIgnoreCase("eier")) {
 				sender.sendMessage(hPlaces.setOwner(player, id, args[2]));
 				return true;
 			}
 			else {
 				help.customHelp(sender, cmd.getName(), args[0], help());
 				return true;
 			}
 		}
 		
 		else if (args.length > 3 && (args[0].equalsIgnoreCase("entre") || args[0].equalsIgnoreCase("forlate"))) {
 			id = hPlaces.getID(args[1]);
 			if (id == 0) {
 				sender.sendMessage(ThePlugin.c2 + args[1] + " finnes ikke");
 				return true;
 			}
 			String arg = "";
 			for (int i = 2; i < args.length; i++) {
 				arg = arg + args[i] + " ";
 			}
 			arg = arg.trim();
			if (args[1].equalsIgnoreCase("entre")) {
 				sender.sendMessage(hPlaces.setEnter(player, id, arg));
 				return true;
 			}
			else if (args[1].equalsIgnoreCase("forlate")) {
 				sender.sendMessage(hPlaces.setLeave(player, id, arg));
 				return true;
 			}
 			else {
 				help.customHelp(sender, cmd.getName(), args[0], help());
 				return true;
 			}
 		}
 		else {
 			help.customHelp(sender, cmd.getName(), args[0], help());
 			return true;
 		}
 	}
 	
 	/**
 	 * 
 	 * @return Help entryes for /plass
 	 */
 	private String help() {
 		ChatColor white = ChatColor.WHITE;
 		ChatColor gold = ChatColor.GOLD;
 		StringBuilder help = new StringBuilder();
 		help.append(gold + "/plass\n");
 		help.append(white + "Viser denne hjelpe menyen\n");
 		help.append(gold + "/plass liste\n");
 		help.append(white + "Viser en liste over alle plasser\n");
 		help.append(gold + "/plass spiller\n");
 		help.append(white + "Viser en liste over spillere som har plasser\n");
 		help.append(gold + "/plass spiller <spiller-navn>\n");
 		help.append(white + "Viser hvilke plasser spilleren er eier og medlem av\n");
 		help.append(gold + "/plass her\n");
 		help.append(white + "Viser info om plassen du str p\n");
 		help.append(gold + "/plass <plass-navn>\n");
 		help.append(white + "Viser info om angitte plass\n");
 		help.append(gold + "/plass ny <plass-navn>\n");
 		help.append(white + "Lager ny plass der du str (strrelse blir 81x81)\n");
 		help.append(gold + "/plass ny <plass-navn> <strrelse>\n");
 		help.append(white + "Lager ny plass der du str med nsket strrelse\n");
 		help.append(gold + "/plass flytt <plass-navn>\n");
 		help.append(white + "Flytter plassen din til der du str (strrelse blir 81x81)\n");
 		help.append(gold + "/plass flytt <plass-navn> <strrelse>\n");
 		help.append(white + "Flytter plassen din til der du str med nsket strrelse\n");
 		help.append(gold + "/plass spawn <plass-navn>\n");
 		help.append(white + "Teleporterer deg til spawn i angitte plass\n");
 		help.append(gold + "/plass setspawn <plass-navn>\n");
 		help.append(white + "Setter ny spawn til der du str\n");
 		help.append(gold + "/plass medlem <plass-navn> <spiller-navn>\n");
 		help.append(white + "Inviterer spiller til  bli medlem av din plass\n");
 		help.append(gold + "/plass spark <plass-navn> <spiller-navn>\n");
 		help.append(white + "Fjerner spiller som medlem av din plass\n");
 		help.append(gold + "/plass eier <plass-navn> <spiller-navn>\n");
 		help.append(white + "Setter ny eier av din plass, DU kan IKKE gjre om dette\n");
 		help.append(gold + "/plass navn <plass-navn> <nytt-plass-navn>\n");
 		help.append(white + "Bytter navn p angitte plass\n");
 		help.append(gold + "/plass toggle <plass-navn> [pvp, monstre, dyr]\n");
 		help.append(white + "Skrur p/av pvp, monstre eller dyr\n");
 		help.append(gold + "/plass entre <plass-navn> <ny velkomst-melding>\n");
 		help.append(white + "Setter ny velkomst-melding for angitte plass\n");
 		help.append(gold + "/plass entre <plass-navn>\n");
 		help.append(white + "Fjerner velkomst-melding for angitte plass\n");
 		help.append(gold + "/plass forlate <plass-navn> <ny forlat-melding>\n");
 		help.append(white + "Setter ny forlat-melding for angitte plass\n");
 		help.append(gold + "/plass forlate <plass-navn>\n");
 		help.append(white + "Fjerner forlat-melding for angitte plass\n");
 		help.append(gold + "/plass slett <plass-navn>\n");
 		help.append(white + "Sletter angitte plass\n");
 		return help.toString();
 	}
 }
