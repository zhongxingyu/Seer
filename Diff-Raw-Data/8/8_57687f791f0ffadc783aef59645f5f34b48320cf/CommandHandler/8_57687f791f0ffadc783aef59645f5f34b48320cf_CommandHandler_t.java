 package com.araeosia.ArcherGames;
 
 import com.araeosia.ArcherGames.utils.Archer;
 import org.bukkit.ChatColor;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.player.PlayerCommandPreprocessEvent;
 
 public class CommandHandler implements CommandExecutor, Listener {
 
 	public ArcherGames plugin;
 
 	public CommandHandler(ArcherGames plugin) {
 		this.plugin = plugin;
 	}
 
 	/**
 	 *
 	 * @param sender
 	 * @param cmd
 	 * @param commandLabel
 	 * @param args
 	 * @return
 	 */
 	@Override
 	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
 		if (cmd.getName().equalsIgnoreCase("vote")) {
 			sender.sendMessage(plugin.strings.get("voteinfo"));
 			for (String s : plugin.voteSites) {
 				sender.sendMessage(ChatColor.GREEN + s);
 				return true;
 			}
 		} else if (cmd.getName().equalsIgnoreCase("money")) {
 			if (args.length == 0) {
 				sender.sendMessage(ChatColor.GREEN + sender.getName()+"'s balance is " + plugin.econ.getBalance(sender.getName()) + ""); //Or something
 				return true;
 			} else {
 				sender.sendMessage(ChatColor.GREEN + args[0] + "'s balance is "+plugin.econ.getBalance(args[0])); //Or something
 				return true;
 			}
 		} else if (cmd.getName().equalsIgnoreCase("stats")) {
 			if (args.length == 0) {
 				//plugin.getStats(sender.getName());
 				return true;
 			} else {
 				//plugin.getStats(args[0]);
 				return true;
 			}
 		} else if (cmd.getName().equalsIgnoreCase("listkits")) {
 			sender.sendMessage(plugin.strings.get("kitinfo"));
 			String kits = "";
 			for (String s : plugin.kits.keySet()) {
 				kits += s + ", ";
 			}
 			sender.sendMessage(ChatColor.GREEN + kits);
 		} else if (cmd.getName().equalsIgnoreCase("kit") || args.length != 0) {
 			if (plugin.kits.containsKey(args[0])) {
				plugin.serverwide.livingPlayers.add(Archer.getByName(sender.getName()));
 				Archer.getByName(sender.getName()).selectKit(args[0]);
 				sender.sendMessage(plugin.strings.get("kitinfo").format(args[0]));
 				return true;
 			}
 		} else if (plugin.debug) {
 			if (args[0].equalsIgnoreCase("startGame")) {
 				ScheduledTasks.gameStatus = 2;
 				plugin.log.info("[ArcherGames/Debug]: Game force-started by " + sender.getName());
 				return true;
 			}
 		}
 		return false;
 	}
 
 	/**
 	 *
 	 * @param event
 	 */
 	@EventHandler
 	public void onCommandPreProccessEvent(final PlayerCommandPreprocessEvent event) {
 		if (!plugin.serverwide.getArcher(event.getPlayer()).canTalk) {
 			if (!event.getMessage().contains("kit")) {
 				event.setCancelled(true);
 				event.getPlayer().sendMessage(plugin.strings.get("nocommand"));
 			}
 		}
 	}
 }
