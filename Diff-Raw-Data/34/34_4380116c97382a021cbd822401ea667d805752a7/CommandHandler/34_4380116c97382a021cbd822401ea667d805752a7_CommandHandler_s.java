 package com.araeosia.ArcherGames;
 
 import com.araeosia.ArcherGames.utils.Archer;
 import com.araeosia.ArcherGames.utils.Kit;
 import java.util.HashMap;
 import org.bukkit.ChatColor;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.player.PlayerCommandPreprocessEvent;
 
 public class CommandHandler implements CommandExecutor, Listener {
 
 	public ArcherGames plugin;
 
 	public CommandHandler(ArcherGames plugin) {
 		this.plugin = plugin;
 	}
 	public HashMap<String, Integer> chunkReloads;
 
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
 				sender.sendMessage(ChatColor.GREEN + sender.getName() + "'s balance is " + plugin.econ.getBalance(sender.getName()) + ""); //Or something
 				return true;
 			} else {
 				sender.sendMessage(ChatColor.GREEN + args[0] + "'s balance is " + plugin.econ.getBalance(args[0])); //Or something
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
 		} else if (cmd.getName().equalsIgnoreCase("who")){
 			for(Archer a : plugin.serverwide.livingPlayers){
 				a.getName();
 			}
 			return true;
 		} else if (cmd.getName().equalsIgnoreCase("kit") || cmd.getName().equalsIgnoreCase("kits")) {
 			if (args.length != 0) {
 				Kit selectedKit = new Kit();
 				Boolean isOkay = false;
 				for (Kit kit : plugin.kits) {
 					if (args[0].equalsIgnoreCase(kit.getName())) {
 						isOkay = true;
 						selectedKit = kit;
 					}
 				}
 				if (isOkay) {
 					if(Archer.getByName(sender.getName()).isReady()){
 						sender.sendMessage(String.format(plugin.strings.get("alreadyselected"), Archer.getByName(sender.getName()).getKit().getName()));
 					}
 					if (sender.hasPermission(selectedKit.getPermission())) {
 						Archer.getByName(sender.getName()).selectKit(selectedKit);
						plugin.serverwide.livingPlayers.add(Archer.getByName(sender.getName()));
 						sender.sendMessage(String.format(plugin.strings.get("kitgiven"), selectedKit.getName()));
 					} else {
 						sender.sendMessage(ChatColor.RED + "You do not have permission to use this kit.");
 					}
 				} else {
 					sender.sendMessage(ChatColor.RED + "That is not a valid kit.");
 				}
 			} else {
 				sender.sendMessage(ChatColor.GREEN + plugin.strings.get("kitinfo"));
 				String kits = "";
 				String kitsNo = "";
 				for (Kit kit : plugin.kits) {
 					if (sender.hasPermission(kit.getPermission())) {
 						kits += kit.getName() + ", ";
 					} else {
 						kitsNo += kit.getName() + ", ";
 					}
 				}
 				sender.sendMessage(ChatColor.GREEN + kits);
 				sender.sendMessage(plugin.strings.get("kitnoaccessible"));
 				sender.sendMessage("§c" + kitsNo);
 			}
 			return true;
 		} else if (cmd.getName().equalsIgnoreCase("chunk")) {
 			if (!(ScheduledTasks.gameStatus == 1)) {
 				if (sender instanceof Player) {
 					Player player = (Player) sender;
 					player.getWorld().unloadChunk(player.getLocation().getChunk());
 					player.getWorld().loadChunk(player.getLocation().getChunk());
 					player.sendMessage(ChatColor.GREEN + "Chunk Reloaded.");
 					return true;
 				} else {
 					return false;
 				}
 			} else {
 				sender.sendMessage(ChatColor.RED + "You may not use this command yet.");
 				return true;
 			}
 
 
 		} else if (cmd.getName().equalsIgnoreCase("pay")) {
 			if (args.length != 0) {
 				if (args.length != 1) {
 					try {
 						if (Double.parseDouble(args[1]) > 0) {
 							plugin.econ.takePlayer(sender.getName(), Double.parseDouble(args[1]));
 							plugin.econ.givePlayer(args[0], Double.parseDouble(args[1]));
 							sender.sendMessage(ChatColor.GREEN + "$" + args[0] + " paid to " + args[0]);
 						}
 					} catch (Exception e) {
 						return false;
 					}
 				} else {
 					return false;
 				}
 			} else {
 				return false;
 			}
 		} else if (cmd.getName().equalsIgnoreCase("time")) {
 			sender.sendMessage(ChatColor.GREEN + ((String.format(plugin.strings.get("starttimeleft"), ((plugin.scheduler.preGameCountdown - plugin.scheduler.currentLoop) / 60 + " minute" + (((plugin.scheduler.preGameCountdown - plugin.scheduler.currentLoop) / 60) == 1 ? "" : "s") + ", " + ((plugin.scheduler.preGameCountdown - plugin.scheduler.currentLoop) % 60) + " second" + ((plugin.scheduler.preGameCountdown - plugin.scheduler.currentLoop != 1) ? "s" : ""))))));
 			return true;
 		} else if (cmd.getName().equalsIgnoreCase("timer")) {
 			if (args.length != 0) {
 				if (sender.hasPermission("ArcherGames.admin")) {
 					try {
 						plugin.scheduler.preGameCountdown = Integer.parseInt(args[0]);
 						plugin.scheduler.currentLoop = 0;
 						sender.sendMessage(ChatColor.GREEN + "Time left set to " + args[0] + " seconds left.");
 						return true;
 					} catch (Exception e) {
 						sender.sendMessage(ChatColor.RED + "Time could not be set.");
 						return true;
 					}
 				}
 			}
 		} else if (plugin.debug && cmd.getName().equalsIgnoreCase("ArcherGames")) {
 
 			if (args[0].equalsIgnoreCase("startGame")) {
 				ScheduledTasks.gameStatus = 2;
 				sender.sendMessage(ChatColor.GREEN + "Game started.");
 				plugin.log.info("[ArcherGames/Debug]: Game force-started by " + sender.getName());
 				return true;
 			}
 
 		} else if (cmd.getName().equalsIgnoreCase("lockdown")) {
 			if (sender.hasPermission("archergames.admin")) {
 				plugin.getConfig().set("ArcherGames.toggles.lockdownMode", !plugin.configToggles.get("ArcherGames.toggles.lockdownMode"));
 				plugin.saveConfig();
 				sender.sendMessage(ChatColor.GREEN + "LockDown mode toggled.");
 				return true;
 			} else {
 				return false;
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
 		if (!plugin.serverwide.getArcher(event.getPlayer()).canTalk && !event.getPlayer().hasPermission("archergames.overrides.command")) {
 			if (!event.getMessage().contains("kit") && false) { // Needs fixing.
 				event.setCancelled(true);
 				event.getPlayer().sendMessage(plugin.strings.get("nocommand"));
 			}
 		}
 	}
 }
