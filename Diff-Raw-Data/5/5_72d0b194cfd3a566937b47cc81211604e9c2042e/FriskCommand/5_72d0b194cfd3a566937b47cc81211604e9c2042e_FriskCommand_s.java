 package friskstick.cops.plugin;
 
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.inventory.PlayerInventory;
 
 /*
  * 
  * This is where the command for frisk will be implemented, making it easier to organize the program.
  * 
  */
 
 public class FriskCommand implements CommandExecutor{
 
 	private FriskStick plugin;
 	JailPlayer jailed = new JailPlayer();
 
 	public FriskCommand(FriskStick plugin) {
 
 		this.plugin = plugin;
 
 	}
 
 	int index = 0;
 
 	@Override
 	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
 
 		Player player = (Player)sender;
 
 		if(player == null) {
 
 			sender.sendMessage("You cannot run this command in the console!");
 
 		} else {
 
 			if(commandLabel.equalsIgnoreCase("frisk")) { // If the player typed /frisk then do the following...
 
 				if(args.length == 0) {
 
 					player.sendMessage("Usage: /frisk <playername>");
 
 				} else if(args.length == 1) {
 
 					Player frisked = plugin.getServer().getPlayer(args[0]);
 
 					if(player.hasPermission("friskstick.chat")) {
 
 						PlayerInventory inventory = frisked.getInventory();
 						boolean found = false;
 
 						for(String drug: plugin.getConfig().getStringList("drug-ids")) {
 
 							if(drug.contains(":")) {
 
 								String firsthalf = drug.split(":")[0];
 								String lasthalf = drug.split(":")[1];
 
 								for(int i = 1; i <= plugin.getConfig().getInt("amount-to-search-for"); i++) {
 
 									ItemStack[] contents = inventory.getContents();
 
 									if(inventory.contains(new ItemStack(Integer.parseInt(firsthalf), i, Short.parseShort(lasthalf)))) {
 
 										player.getInventory().addItem(new ItemStack(contents[inventory.first(new ItemStack(Integer.parseInt(firsthalf), i, Short.parseShort(lasthalf)))]));
 										inventory.removeItem(new ItemStack(Integer.parseInt(firsthalf), 2305, Short.parseShort(lasthalf)));
 
										player.sendMessage(plugin.getConfig().getString("cop-found-msg").replaceAll("&", "").replaceAll("%itemname%", plugin.getConfig().getStringList("drug-names").toArray()[index].toString()).replaceAll("%cop%", player.getName()).replaceAll("%player%", plugin.getServer().getPlayer(args[0]).getName()));
 										plugin.getServer().getPlayer(args[0]).sendMessage(plugin.getConfig().getString("player-found-msg").replaceAll("&", "").replaceAll("%player%", player.getName()).replaceAll("%itemname%", plugin.getConfig().getStringList("drug-names").toArray()[index].toString()));
 
 										if(player.hasPermission("friskstick.jail")) {
 
 											jailed.jail(plugin.getServer().getPlayer(args[0]).getName());
 
 										}
 
 										found = true;
 
 									}
 
 								}
 
 							} else {
 
 								if(inventory.contains(Integer.parseInt(drug))) {
 
 									int drugid = Integer.parseInt(drug);
 
 									ItemStack[] contents = inventory.getContents();
 									player.getInventory().addItem(new ItemStack(contents[inventory.first(drugid)]));
 									inventory.removeItem(new ItemStack(drugid, 2305));
 
									player.sendMessage(plugin.getConfig().getString("cop-found-msg").replaceAll("&", "").replaceAll("%itemname%", plugin.getConfig().getStringList("drug-names").toArray()[index].toString()).replaceAll("%cop%", player.getName()).replaceAll("%player%", plugin.getServer().getPlayer(args[0]).getName()));
 									plugin.getServer().getPlayer(args[0]).sendMessage(plugin.getConfig().getString("player-found-msg").replaceAll("&", "").replaceAll("%player%", player.getName()).replaceAll("%itemname%", plugin.getConfig().getStringList("drug-names").toArray()[index].toString()));
 
 									if(player.hasPermission("friskstick.jail")) {
 
 										jailed.jail(plugin.getServer().getPlayer(args[0]).getName());
 
 									}
 
 									found = true;
 
 								}
 
 							}
 
 							index++;
 
 						}
 
 						index = 0;
 
 						if(!found) {
 
 							player.sendMessage(plugin.getConfig().getString("cop-not-found-msg").replaceAll("&", "").replaceAll("%cop%", player.getName()).replaceAll("%player%", plugin.getServer().getPlayer(args[0]).getName()));
 							plugin.getServer().getPlayer(args[0]).sendMessage(plugin.getConfig().getString("player-not-found-msg").replaceAll("&", "").replaceAll("%player%", player.getName()));
 
 							if(player.getHealth() >= 2) {
 
 								player.setHealth(player.getHealth() - 2);
 
 							} else {
 
 								player.setHealth(0);
 
 							}
 
 						}
 
 					}
 
 				}
 
 				return true;
 
 			} 
 
 		}
 
 		return false; 
 
 	}
 
 }
