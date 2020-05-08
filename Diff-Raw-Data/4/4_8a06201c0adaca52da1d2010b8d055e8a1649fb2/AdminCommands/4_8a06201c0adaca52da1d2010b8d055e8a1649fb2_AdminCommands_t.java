 package darkknightcz.InviteEm.commands;
 
 import java.sql.SQLException;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.plugin.java.JavaPlugin;
 
 import darkknightcz.InviteEm.MySQL;
 import darkknightcz.InviteEm.Settings;
 
 public class AdminCommands implements CommandExecutor {
 	JavaPlugin plugin;
 	MySQL db;
 
 	public AdminCommands(JavaPlugin plugin, MySQL db) {
 		this.plugin = plugin;
 		this.db = db;
 	}
 
 	@Override
 	public boolean onCommand(CommandSender sender, Command cmd, String label,
 			String[] args) {
 		if (sender instanceof Player) {
 				if (cmd.getName().equalsIgnoreCase("inva")) {
 					if (args[0].equalsIgnoreCase("offset")) {
 						if (args.length == 3) {
 							int offset = Integer.parseInt(args[2]);
 							try {
 								db.setOffset(args[1], offset);
 								sender.sendMessage(ChatColor.GREEN
 										+ Settings.offsetChanged
 												.replaceAll("PLAYER", args[1])
 												.replaceAll("OFFSET",
 														"" + offset)
 												.replaceAll(
 														"MAX",
 														""
 																+ (Settings.MaxInvitations + offset)));
 								return true;
 							} catch (SQLException e) {
 								sender.sendMessage(ChatColor.RED
 										+ Settings.somethingWentWrongOffset
 												.replaceAll("PLAYER", args[1]));
 								return false;
 							}
 						} else if (args.length == 2) {
 							try {
 								int offset = db.getOffset(args[1]);
 								sender.sendMessage(ChatColor.YELLOW
 										+ Settings.playerOffset
 												.replaceAll("PLAYER", args[1])
 												.replaceAll("OFFSET",
 														"" + offset)
 												.replaceAll(
 														"MAX",
 														""
 																+ (Settings.MaxInvitations + offset)));
 								return true;
 							} catch (Exception e) {
 								sender.sendMessage(ChatColor.RED
 										+ Settings.playerDoesNotExist
 												.replaceAll("PLAYER", args[1]));
 								return false;
 							}
 						} else {
 							sender.sendMessage(ChatColor.BLUE
 									+ "Usage: /inva offset [player] (number)");
 							return true;
 						}
 					}else if(args[0].equalsIgnoreCase("warn")){
 						if(args.length>2){
 							String player = args[1].toLowerCase();
 							StringBuilder sb = new StringBuilder();
 							for(int i=2;i<args.length;i++){
 								sb.append(args[i]);
 							}
 							String msg = sb.toString();
 							
 							int id=db.warnAdmin(player, msg);
 							Player pl = Bukkit.getPlayer(player);
 							if(pl.isOnline()){
 								pl.sendMessage(ChatColor.RED+Settings.youHaveBeenWarned.replaceAll("REASON",msg));
 								System.out.println("Vracene ID = "+id);
 								db.setWarned(id);
 								return true;
 							}
 						}else{
 							sender.sendMessage(ChatColor.BLUE+"Usage: /inva warn [player] [reason]");
 							return true;							
 						}
 						
 					}
 				}
			
 
 		} else {
 			sender.sendMessage(ChatColor.RED + Settings.youHaveToBePlayer);
 			return false;
 		}
 
 		return false;
 	}
 }
