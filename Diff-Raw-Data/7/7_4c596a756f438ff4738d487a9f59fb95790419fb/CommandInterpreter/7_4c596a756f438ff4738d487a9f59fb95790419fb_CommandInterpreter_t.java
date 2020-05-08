 package net.lotrcraft.minepermit;
 
 import java.util.Map;
 
 import org.bukkit.ChatColor;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.command.ConsoleCommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.ItemStack;
 
 public class CommandInterpreter implements CommandExecutor {
 
 	@Override
 	public boolean onCommand(CommandSender sender, Command cmnd, String arg2,
 			String[] arg3) {
 
 		// Console cant send commands
 		if (sender instanceof ConsoleCommandSender) {
 			MinePermit.log.info("[MinePermit] Sorry, commands cant be sent from console yet.");
 			return true;
 		}
 
 		Player p = (Player) sender;
 
 		// Make sure there is a secondary parameter
 		if (arg3.length == 0) {
 			return false;
 
 		} else if (arg3[0].equalsIgnoreCase("view")) {
 
 			Miner m = MinerManager.getMiner(p);
 
 			int id;
 
 			try {
 				id = Integer.parseInt(arg3[1]);
 			} catch (Exception e) {
 
 				String s = "";
 				Map<Integer, Long> tmp = m.getPermits();
 				
 				if(tmp.isEmpty()){
 					p.sendMessage(ChatColor.YELLOW + "You don't own any permits!");
 					return true;
 				}
 					
 				for (int y = 0; y < tmp.size(); y++) {
 					int n = (Integer) tmp.keySet().toArray()[y];
 
 					p.sendMessage(ChatColor.DARK_GREEN + "" + n + ": " + m.getRemainingTime(n) + " minutes.");
 				}
 
 				return true;
 			}
 
 			if (!m.hasPermit(id)) {
 				p.sendMessage(ChatColor.DARK_RED
 						+ "You do not own a permit for " + id);
 			} else {
 				p.sendMessage(ChatColor.DARK_GREEN + "You have "
 						+ m.getRemainingTime(id) + " minutes left.");
 			}
 
 			return true;
 
 		} else if (arg3[0].equalsIgnoreCase("cost")) {
 
 			int id;
 
 			try {
 				id = Integer.parseInt(arg3[1]);
			} catch (ArrayIndexOutOfBoundsException e1){
				return false;
			} catch (NumberFormatException e2) {
 				return false;
 			}
 
 			if (!Config.isPermitRequired(id)) {
 				p.sendMessage(ChatColor.GRAY
 						+ "A permit is not required for this item.");
 				return true;
 			}
 
 			p.sendMessage(ChatColor.AQUA + "The cost for this item is "
 					+ Config.getCost(id) + " dolars");
 			return true;
 
 		} else if (arg3[0].equalsIgnoreCase("buy")) {
 
 			int id;
 
 			//Atempt to get an id number for the block
 			try {
 				id = Integer.parseInt(arg3[1]);
			} catch (ArrayIndexOutOfBoundsException e1){
				return false;
 			} catch (NumberFormatException e) {
 				return false;
 			}
 
 			//Check if a permit is required for this block
 			if (!Config.isPermitRequired(id)) {
 				p.sendMessage(ChatColor.GRAY
 						+ "A permit is not required for this item.");
 				return true;
 			}
 			
 			//Check if the player already has a permit for this
 			if(MinerManager.getMiner(p).hasPermit(id)){
 				p.sendMessage(ChatColor.AQUA + "You already have a permit for this!");
 				return true;
 			}
 			
 			//Check if the player has enough money
 			//TODO: Convert to Economy plugin?
 			if (!p.getInventory().contains(Config.currencyBlockID, Config.getCost(id))) {
 				p.sendMessage(ChatColor.DARK_RED
 						+ "You dont have enough money!");
 				return true;
 			}
 
 			int cost = Config.getCost(id);
 
 			while (cost > 0) {
 				ItemStack x = p.getInventory().getItem(
 						p.getInventory().first(Config.currencyBlockID));
 
 				if (x.getAmount() < cost) {
 					cost -= x.getAmount();
 					x.setAmount(0);
 				} else {
 					x.setAmount(x.getAmount() - cost);
 					cost = 0;
 				}
 			}
 
 			MinerManager.getMiner(p).addPermit(id, Config.permitDuration);
 
 			p.sendMessage(ChatColor.DARK_GREEN + "Permit purchased!");
 
 			return true;
 		}
 
 		return false;
 	}
 }
