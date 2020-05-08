 package nl.giantit.minecraft.GiantShop.Executors;
 
 import nl.giantit.minecraft.GiantShop.GiantShop;
 import nl.giantit.minecraft.GiantShop.core.perm;
 import nl.giantit.minecraft.GiantShop.Misc.Misc;
 import nl.giantit.minecraft.GiantShop.core.Commands.*;
 
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 /**
  *
  * @author Giant
  */
 public class chat {
 	
 	private GiantShop plugin;
 	private perm perm;
 	
 	public chat(GiantShop plugin) {
 		this.plugin = plugin;
 	}
 
 	public boolean exec(CommandSender sender, Command cmd, String commandLabel, String[] args) {
 		Player player = (Player) sender;
 		perm = plugin.getPermMan();
 		if(args.length == 0 || args[0].equalsIgnoreCase("help") || args[0].equalsIgnoreCase("h") || args[0].equalsIgnoreCase("?")) {
 			//done
 			if(plugin.useLoc && plugin.cmds.contains("help")) {
 				if(plugin.loc.canUse(player))
 					help.showHelp(player, args);
 			}else
 				help.showHelp(player, args);
 		}else if(Misc.isEitherIgnoreCase(args[0], "sendhelp", "sh")) {
 			//done
 			if(plugin.useLoc && plugin.cmds.contains("sendhelp")) {
 				if(plugin.loc.canUse(player))
 					help.sendHelp(player, args);
 			}else
 				help.sendHelp(player, args);
 		}else if(Misc.isEitherIgnoreCase(args[0], "list", "l")) {
 			//done
 			if(plugin.useLoc && plugin.cmds.contains("list")) {
 				if(plugin.loc.canUse(player))
 					list.list(player, args);
 			}else
 				list.list(player, args);
 		}else if(Misc.isEitherIgnoreCase(args[0], "check", "c")) {
 			//in dev
 			if(plugin.useLoc && plugin.cmds.contains("check")) {
 				if(plugin.loc.canUse(player))
 					check.check(player, args);
 			}else
 				check.check(player, args);
 		}else if(Misc.isEitherIgnoreCase(args[0], "buy", "b")) {
 			//stalled
 			if(plugin.useLoc && plugin.cmds.contains("buy")) {
 				if(plugin.loc.canUse(player))
 					buy.buy(player, args);
 			}else
 				buy.buy(player, args);
 		}else if(Misc.isEitherIgnoreCase(args[0], "sell", "s")) {
 			//stalled
 			if(plugin.useLoc && plugin.cmds.contains("sell")) {
 				if(plugin.loc.canUse(player))
 					sell.sell(player, args);
 			}else
 				sell.sell(player, args);
 		}else if(Misc.isEitherIgnoreCase(args[0], "gift", "g")) {
 			//stalled
 			if(plugin.useLoc && plugin.cmds.contains("gift")) {
 				if(plugin.loc.canUse(player))
 					buy.gift(player, args);
 			}else
 				buy.gift(player, args);
 		}else if(Misc.isEitherIgnoreCase(args[0], "add", "a")) {
 			//in dev
 			//finished for now
 			if(plugin.useLoc && plugin.cmds.contains("add")) {
 				if(plugin.loc.canUse(player))
 					add.add(player, args);
 			}else
 				add.add(player, args);
 		}else if(Misc.isEitherIgnoreCase(args[0], "update", "u")) {
 			//stalled
 			if(plugin.useLoc && plugin.cmds.contains("update")) {
 				if(plugin.loc.canUse(player))
 					help.showHelp(player, args);
 			}else
 				help.showHelp(player, args);
 		}else if(Misc.isEitherIgnoreCase(args[0], "remove", "r")) {
 			//stalled
 			if(plugin.useLoc && plugin.cmds.contains("remove")) {
 				if(plugin.loc.canUse(player))
 					help.showHelp(player, args);
 			}else
 				help.showHelp(player, args);
 		}else if(Misc.isEitherIgnoreCase(args[0], "addstock", "as")) {
 			//stalled
 			if(plugin.useLoc && plugin.cmds.contains("addstock")) {
 				if(plugin.loc.canUse(player))
 					help.showHelp(player, args);
 			}else
 				help.showHelp(player, args);
 		}
 
 		return true;
 	}
 }
