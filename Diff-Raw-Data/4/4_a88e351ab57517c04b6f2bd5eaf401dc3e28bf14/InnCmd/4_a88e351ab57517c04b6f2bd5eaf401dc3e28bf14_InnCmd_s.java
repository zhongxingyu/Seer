 package me.greatman.plugins.inn.commands;
 
 import me.greatman.plugins.inn.IConfig;
 import me.greatman.plugins.inn.IPermissions;
 import me.greatman.plugins.inn.ITools;
 import me.greatman.plugins.inn.Inn;
 import me.greatman.plugins.inn.PlayerData;
 import me.greatman.plugins.inn.extras.CommandManager;
 
 import org.bukkit.ChatColor;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 public class InnCmd implements CommandExecutor {
 	private final Inn plugin;
 	private final IConfig IConfig;
     public InnCmd(Inn instance) {
         plugin = instance;
         IConfig = new IConfig(plugin);
         
     }
     public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
     	boolean handled = false;
     	if (is(label, "inn")) {
     		if (args == null || args.length == 0){
     			handled = true;
     		}
     		if (is(args[0], "select")){
     			handled = true;
     			if (!(sender instanceof Player)){
     				sendMessage(sender,colorizeText("Only players can use this command.",ChatColor.RED));
     				return handled;
     			}
     			if (IPermissions.permission(plugin.getPlayer(sender), "inn.create", plugin.getPlayer(sender).isOp())){
     				Player player = (Player) sender;
     				String playerName = player.getName();
     				if (!plugin.getPlayerData().containsKey(playerName)) {
     	                plugin.getPlayerData().put(playerName, new PlayerData(plugin, playerName));
     	            }
     	            plugin.getPlayerData().get(playerName).setSelecting(!plugin.getPlayerData().get(playerName).isSelecting());
 
     	            if (plugin.getPlayerData().get(playerName).isSelecting()) {
     	                sender.sendMessage(ChatColor.WHITE + "Inn selection enabled." + ChatColor.DARK_AQUA + " Use " + ChatColor.WHITE + "bare hands " + ChatColor.DARK_AQUA + "to select!");
     	                sender.sendMessage(ChatColor.DARK_AQUA + "Left click the room door");
     	            } else {
     	                sender.sendMessage(ChatColor.DARK_AQUA + "Selection disabled");
     	                plugin.getPlayerData().put(playerName, new PlayerData(plugin, playerName));
     	            }
     			}else
     				sendMessage(sender,colorizeText("Permission denied.",ChatColor.RED));
     		}else if(is(args[0], "create")){
     			handled = true;
     			if (!(sender instanceof Player)){
     				sendMessage(sender,colorizeText("Only players can use this command.",ChatColor.RED));
     				return handled;
     			}
     			if (IPermissions.permission(plugin.getPlayer(sender), "inn.create", plugin.getPlayer(sender).isOp())){
     				if (args.length == 1){
     					sendMessage(sender,colorizeText("Syntax: /inn create [Price]",ChatColor.RED));
     					return true;
     				}
     				if (ITools.isInt(args[1])){
     					Player player = (Player) sender;
         				String playerName = player.getName();
     					int[] xyz = plugin.getPlayerData().get(playerName).getPositionA();
     					int i = 0;
     					for (i=0;i < 9999999;i++){
     						if (!IConfig.readBoolean("door."+ i + ".active")){
     							break;
     						}
     					}
     					IConfig.write("door." + i + ".active", true);
     					IConfig.write("door." + i + ".x",xyz[0]);
    					IConfig.write("door." + i + ".x",xyz[1]);
    					IConfig.write("door." + i + ".x",xyz[2]);
     					IConfig.write("door." + i + ".price",args[1]);
     				}else
     					sendMessage(sender,colorizeText("Expected integer. Received string.",ChatColor.RED));
     			}
     		}	
     	}
     	return handled;
     }
  // Simplifies and shortens the if statements for commands.
     private boolean is(String entered, String label) {
         return entered.equalsIgnoreCase(label);
     }
 
     // Checks if the current user is actually a player.
     private boolean isPlayer(CommandSender sender) {
         return sender != null && sender instanceof Player;
     }
 
     // Checks if the current user is actually a player and sends a message to that player.
     private boolean sendMessage(CommandSender sender, String message) {
         boolean sent = false;
         if (isPlayer(sender)) {
             Player player = (Player) sender;
             player.sendMessage(message);
             sent = true;
         }
         return sent;
     }
     public String colorizeText(String text, ChatColor color) {
         return color + text + ChatColor.WHITE;
     }
 }
