 package ca.wacos;
 
 import java.util.HashMap;
 
 import org.bukkit.Bukkit;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 public class NametagCommand implements CommandExecutor {
 
 	HashMap<String, Boolean> updateTasks = new HashMap<String, Boolean>();
 	
 	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
 		Player senderPlayer = null;
 		if (sender instanceof Player) {
 			senderPlayer = (Player) sender;
 		}
 		
 		if (cmd.getName().equalsIgnoreCase("ne")) {
 			if (senderPlayer != null) {
 				if (!senderPlayer.hasPermission("NametagEdit.use")) {
 					sender.sendMessage("§cYou don't have permission to use this plugin.");
 					return true;
 				}
 			}
 			if (args.length >= 1 && args[0].equalsIgnoreCase("reload")) {
 				if (senderPlayer != null) {
 					if (!senderPlayer.hasPermission("NametagEdit.reload")) {
 						sender.sendMessage("§cYou don't have permission to reload this plugin.");
 						return true;
 					}
 				}
 				NametagEdit.plugin.load();
 				sender.sendMessage("§eReloaded group nodes and players.");
 				return true;
 			}
 			if (args.length >= 1 && args[0].equalsIgnoreCase("update")) {
 				if (senderPlayer != null) {
 					if (!senderPlayer.isOp()) {
 						sender.sendMessage("§cOnly operators can update this plugin.");
 						return true;
 					}
 				}
 				if (args.length == 1) {
 					sender.sendMessage("§eUsage: §a/ne update [stable/dev]");
 					sender.sendMessage("§astable§e: Updates to the latest stable build (reccomended).");
 					sender.sendMessage("§adev§e: Updates to the latest development build.");
 				}
 				else if (args.length > 1) {
 					boolean dev = false;
 					if (args[1].equalsIgnoreCase("dev")) {
 						dev = true;
 					}
 					else if (args[1].equalsIgnoreCase("stable")) {
 						dev = false;
 					}
 					else {
 						sender.sendMessage("§eInvalid build type: §a" + args[1]);
 						return true;
 					}
 					return update(sender, dev);
 				}
 				return true;
 			}
 			if (args.length >= 1 && args[0].equalsIgnoreCase("confirm")) {
 				if (senderPlayer != null) {
 					if (!senderPlayer.isOp()) {
 						sender.sendMessage("§cOnly operators can update this plugin.");
 						return true;
 					}
 				}
 				return download(sender);
 			}
 			if (args.length >= 2) {
 				String operation = args[0];
 				String text = NametagUtils.trim(NametagUtils.getValue(getText(args)));
 				String target = args[1];
 
 				if (senderPlayer != null) {
 					Player tp = Bukkit.getPlayer(target);
 					if (tp != null && senderPlayer != tp) {
 						if (!senderPlayer.hasPermission("NametagEdit.useall")) {
 							sender.sendMessage("§cYou can only edit your own nametag.");
 							return true;
 						}
 					}
 					else if (!target.equalsIgnoreCase(senderPlayer.getName())) {
 						if (!senderPlayer.hasPermission("NametagEdit.useall")) {
 							sender.sendMessage("§cYou can only edit your own nametag.");
 							return true;
 						}
 					}
 				}
 				
 				if (operation.equalsIgnoreCase("prefix") || operation.equalsIgnoreCase("suffix")) {
 					Player targetPlayer;
 					
 
 					targetPlayer = Bukkit.getPlayer(target);
 					
 					if (text.isEmpty()) {
 						sender.sendMessage("§eNo " + operation.toLowerCase() + " given!");
 						return true;
 					}
 					
 					if (targetPlayer != null) {
 						if (PlayerLoader.getPlayer(targetPlayer.getName()) == null) {
 							ScoreboardManager.clear(targetPlayer.getName());
 						}
 					}
 					
 					String prefix = "";
 					String suffix = "";
 					if (operation.equalsIgnoreCase("prefix"))
 						prefix = NametagUtils.formatColors(text);
 					else if (operation.equalsIgnoreCase("suffix"))
 						suffix = NametagUtils.formatColors(text);
 					
 					if (targetPlayer != null)
 						ScoreboardManager.update(targetPlayer.getName(), prefix, suffix);
 					if (targetPlayer != null)
 						PlayerLoader.update(targetPlayer.getName(), prefix, suffix);
 					else
 						PlayerLoader.update(target, prefix, suffix);
 					if (targetPlayer != null)
 						sender.sendMessage("§eSet " + targetPlayer.getName() + "\'s " + operation.toLowerCase() + " to \'" + text + "\'.");
 					else
 						sender.sendMessage("§eSet " + target + "\'s " + operation.toLowerCase() + " to \'" + text + "\'.");
 				}
 				else if (operation.equalsIgnoreCase("clear")) {
 					Player targetPlayer;
 					
 
 					targetPlayer = Bukkit.getPlayer(target);
 					if (targetPlayer != null)
 						sender.sendMessage("§eReset " + targetPlayer.getName() + "\'s prefix and suffix.");
 					else
 						sender.sendMessage("§eReset " + target + "\'s prefix and suffix.");
 					if (targetPlayer != null)
 						ScoreboardManager.clear(targetPlayer.getName());
 					if (targetPlayer != null)
 						PlayerLoader.removePlayer(targetPlayer.getName(), null);
 					else
 						PlayerLoader.removePlayer(target, null);
 					
 					if (targetPlayer != null)
 						for (String key : NametagEdit.groups.keySet().toArray(new String[NametagEdit.groups.keySet().size()])) {
 							if (targetPlayer.hasPermission(key)) {
 								String prefix = NametagEdit.groups.get(key).get("prefix");
 								String suffix = NametagEdit.groups.get(key).get("suffix");
 								if (prefix != null)
 									prefix = NametagUtils.formatColors(prefix);
 								if (suffix != null)
 									suffix = NametagUtils.formatColors(suffix);
 								ScoreboardManager.overlap(targetPlayer.getName(), prefix, suffix);
 								
 								break;
 							}
 						}
 				}
 				else {
 					sender.sendMessage("§eUnknown operation \'" + operation + "\', type §a/ne§e for help.");
 					return true;
 				}
 			}
 			else {
 				sender.sendMessage("§e§nNametagEdit v" + NametagEdit.plugin.getDescription().getVersion() + " command usage:");
 				sender.sendMessage("");
 				sender.sendMessage("§a/ne prefix [player] [text]§e - sets a player's prefix");
 				sender.sendMessage("§a/ne suffix [player] [text]§e - sets a player's suffix");
 				sender.sendMessage("§a/ne clear [player]§e - clears both a player's prefix and suffix.");
 				if (sender instanceof Player && ((Player) sender).hasPermission("NametagEdit.reload") || !(sender instanceof Player))
 					sender.sendMessage("§a/ne reload§e - reloads the configs");
 				if (sender instanceof Player && ((Player) sender).isOp() || !(sender instanceof Player))
 					sender.sendMessage("§a/ne update§e - check for updates");
 			}
 		}
 		return true;
 	}
 	private String getText(String[] args) {
 		String rv = "";
 		for (int t = 2; t < args.length; t++) {
 			if (t == args.length - 1)
 				rv += args[t];
 			else
 				rv += args[t] + " ";
 		}
 		return rv;
 	}
 	private boolean update(CommandSender sender, boolean dev) {
 		String name;
 		if (sender instanceof Player) {
 			name = ((Player) sender).getName();
 		}
 		else {
 			name = "^";
 		}
 		for (String key : updateTasks.keySet().toArray(new String[updateTasks.keySet().size()])) {
 			if (key.equals(name)) {
 				while (updateTasks.remove(key) != null) {}
 				break;
 			}
 		}
 		if (Updater.checkForUpdates(dev, sender)) {
 			updateTasks.put(name, dev);
 		}
 		return true;
 	}
 	private boolean download(CommandSender sender) {
 
 		String name;
 		if (sender instanceof Player) {
 			name = ((Player) sender).getName();
 		}
 		else {
 			name = "^";
 		}
 		for (String key : updateTasks.keySet().toArray(new String[updateTasks.keySet().size()])) {
 			if (key.equals(name)) {
 				for (Player p : Bukkit.getOnlinePlayers()) {
 					if (p.isOp()) {
 						Bukkit.broadcast("[§aNametagEdit§f] §ePlugin is updating...", key);
 					}
 				}
 				updateTasks.clear();
				boolean result = Updater.downloadUpdate(sender, updateTasks.get(key));
 				return true;
 			}
 		}
 		sender.sendMessage("§eNothing to confirm!");
 		return true;
 	}
 
 }
