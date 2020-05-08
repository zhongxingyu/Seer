 package com.github.kaitoyuuki.LastCall;
 
 import org.bukkit.Bukkit;
 import org.bukkit.Effect;
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 
 public class LastCallPlay extends com.github.kaitoyuuki.LastCall.LastDiscs implements CommandExecutor {
 
	@SuppressWarnings("unused")

 	private LCMain plugin;
	PlayMetrics play;
 	public LastCallPlay(LCMain plugin) {
 		this.plugin = plugin;
		play = new PlayMetrics(plugin);
 	}
 	LastDiscs disc = new LastDiscs();
 
 	@Override
 	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
 		if (cmd.getName().equalsIgnoreCase("play")) {
 			if (args.length > 2) {
 				sender.sendMessage("Too many arguments!");
 				return false;
 			}
 			if (!(sender instanceof Player)) {
 				if (args.length < 1) {
 					sender.sendMessage("/play [player] <discID>");
 					return false;
 				}
 				if (args.length == 1) {
 					int discID = 0;
 					discID = disc.getDiscID(args[0]);
 					Material material = Material.getMaterial(discID);
 					String discName = disc.getDiscName(material);
 					if (discID != 0) { 
 						Effect effect = Effect.RECORD_PLAY;
 						for(Player target : Bukkit.getServer().getOnlinePlayers()) {
 							Location loc = target.getLocation();
 							target.playEffect(loc, effect, discID);	
 						}
 						sender.sendMessage("Now playing " + discName + " for all users");
 						play.incPlays(discID);
 						return true;
 					}
 					else {
 						sender.sendMessage("Not a valid disc!");
 						return false;
 					}
 				}
 				if (args.length == 2) {
 					Player target = (Bukkit.getServer().getPlayer(args[0]));
 					if (target == null) {
 						sender.sendMessage(args[0] + " is not online!");
 						return false;
 					}
 					else {
 						int discID = 0;
 						discID = disc.getDiscID(args[1]);
 						Material material = Material.getMaterial(discID);
 						String discName = disc.getDiscName(material);
 						if (discID == 0) {
 							sender.sendMessage("Not a valid disc!");
 							return false;
 						}
 						else {
 							Location loc = target.getLocation();
 							Effect effect = Effect.RECORD_PLAY;
 							target.playEffect(loc, effect, discID);
 							sender.sendMessage("Now playing " + discName + " for " + args[0]);
 							play.incPlays(discID);
 							return true;
 						}
 					}
 				}
 			}
 			if (sender instanceof Player) {
 				Player player = (Player) sender;
 				if (player.hasPermission("lastcall.play")) {
 					if (args.length == 0) {
 						int discID = player.getItemInHand().getType().getId();
 						if ((discID > 2255) && (discID < 2268)) {
 							Location loc = player.getLocation();
 							Effect effect = Effect.RECORD_PLAY;
 							Material material = player.getItemInHand().getType();
 							String discName = disc.getDiscName(material);
 							player.playEffect(loc, effect, discID);
 							sender.sendMessage("Now playing " + discName);
 							play.incPlays(discID);
 							return true;
 						}
 						else {
 							sender.sendMessage("That is not a music disc!");
 							return false;
 						}
 					}
 					if (args.length == 1) {
 						Player target = (Player) sender;
 						int discID = 0;
 						discID = disc.getDiscID(args[0]);
 						Material material = Material.getMaterial(discID);
 						String discName = disc.getDiscName(material);
 						if (discID == 0) {
 							sender.sendMessage("Not a valid disc!");
 							return false;
 						}
 						else {
 							Location loc = target.getLocation();
 							Effect effect = Effect.RECORD_PLAY;
 							target.playEffect(loc, effect, discID);
 							sender.sendMessage("Now playing " + discName);
 							play.incPlays(discID);
 							return true;
 						}
 					}
 					if (args.length == 2) {
 						Player target = (Bukkit.getServer().getPlayer(args[0]));
 						if (target == null) {
 							sender.sendMessage(args[0] + " is not online!");
 							return false;
 						}
 						else {
 							if (target != player && player.hasPermission("lastcall.play.others") || target == player) {
 								int discID = 0;
 								discID = disc.getDiscID(args[1]);
 								Material material = Material.getMaterial(discID);
 								String discName = disc.getDiscName(material);
 								if (discID == 0) {
 									sender.sendMessage("Not a valid disc!");
 									return false;
 								}
 								else {
 									Location loc = target.getLocation();
 									Effect effect = Effect.RECORD_PLAY;
 									target.playEffect(loc, effect, discID);
 									sender.sendMessage("Now playing " + discName + " for " + args[0]);
 									play.incPlays(discID);
 									return true;
 								}
 							}
 						}
 					}
 				}
 			}
 		}
 		return false;
 	}
 }
