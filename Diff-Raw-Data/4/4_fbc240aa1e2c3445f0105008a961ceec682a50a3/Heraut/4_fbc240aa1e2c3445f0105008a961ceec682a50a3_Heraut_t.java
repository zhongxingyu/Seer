 package nl.giantit.minecraft.GiantBanks.core.Misc;
 
 import nl.giantit.minecraft.GiantBanks.GiantBanks;
import org.bukkit.ChatColor;
 
 import org.bukkit.entity.Player;
 import org.bukkit.command.CommandSender;
 
 
 /**
  *
  * @author Giant
  */
 public class Heraut {
 		
 	public static String parse(String input) {
       return ChatColor.translateAlternateColorCodes('&', input);
     }
 	
 	public static String clean(String input) {
 		return input.replaceAll("(&([a-fA-F0-9]))", "");
 	}
 	
 	public static void say (Player player, String message) {
 		player.sendMessage(parse(message));
 	}
 	
 	public static void say (CommandSender sender, String message) {
 		sender.sendMessage(message);
 	}
 
 
 	public static void broadcast (String message) {
 		for(Player p : GiantBanks.getPlugin().getServer().getOnlinePlayers()) {
 			p.sendMessage(parse(message));
 		}
 	}
 	
 	public static void broadcast (String message, Boolean opOnly) {
 		if(!opOnly) {
 			broadcast(message);
 			return;
 		}
 		
 		for(Player p : GiantBanks.getPlugin().getServer().getOnlinePlayers()) {
 			if(p.isOp()) {
 				p.sendMessage(parse(message));
 			}
 		}
 	}
 }
