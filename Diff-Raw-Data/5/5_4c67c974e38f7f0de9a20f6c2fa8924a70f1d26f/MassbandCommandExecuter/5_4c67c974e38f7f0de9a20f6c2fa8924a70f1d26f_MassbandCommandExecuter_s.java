 package de.MrX13415.Massband;
 
 import org.bukkit.ChatColor;
 import org.bukkit.entity.Player;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 
 /**
 * Handler for the 'massband' command.
 * @author MrX13415
 */
 public class MassbandCommandExecuter implements CommandExecutor{
 
 	@Override
 	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
 	    if (sender instanceof Player) {
 	        Player player = (Player) sender;
 	        PlayerVars tmpVars = null;
 	        
 	        //search for current player ...
 			for (int playerIndex = 0; playerIndex < Massband.getPlayerListSize(); playerIndex++) {
 	    		tmpVars = Massband.getPlayer(playerIndex);
 				
 	    		if (tmpVars.getPlayer().equals(player)) {	//player found
 //	    			player.sendMessage("MB: PLAYER-FOUND: " + player.getName());
 					break;
 				}
 	    	}	
 			
 			if (args.length <= 0) {
 				printHelpMsg(command, player);
 
 			}else if (tmpVars != null){
 				
 				if (args[0].equalsIgnoreCase("clear") || args[0].equalsIgnoreCase("clr")) {
 			    	onCommandClear(tmpVars, player);
 			    	
 				}else if (args[0].equalsIgnoreCase("length") || args[0].equalsIgnoreCase("l")) {
 		        	onCommandLength(tmpVars, player);
 		        	
 				}else if (args[0].equalsIgnoreCase("3d") || args[0].equalsIgnoreCase("3d")) {
 					onCommandSwitchMode(tmpVars, player, true);
 				
 				}else if (args[0].equalsIgnoreCase("2d") || args[0].equalsIgnoreCase("2d")) {
 					onCommandSwitchMode(tmpVars, player, false);
 					
 				}else if (args[0].equalsIgnoreCase("dimensions") || args[0].equalsIgnoreCase("d")) {
 					onCommandDimensions(tmpVars, player);
 						
 				}else if (args[0].equalsIgnoreCase("countblocks") || args[0].equalsIgnoreCase("cb")) {
 					onCommandCountBlocks(tmpVars, player);
 				
				}else if (args[0].equalsIgnoreCase("lengthmode") || args[0].equalsIgnoreCase("lt")) {
 					onCommandMode(tmpVars, player, PlayerVars.MODE_LENGTH);
 					
				}else if (args[0].equalsIgnoreCase("surfacemode") || args[0].equalsIgnoreCase("st")) {
 					onCommandMode(tmpVars, player, PlayerVars.MODE_SURFACE);
 					
 				}else{
 					printHelpMsg(command, player);
 				}
 			}
 			
 			return true;
 	    } else {
 	        return false;
 	    }
 	}
 	
 	public void onCommandMode(PlayerVars tmpVars, Player player, int mode){
 		tmpVars.setMode(mode);
 		tmpVars.removeAllWayPoints();
 		
 		if (mode == PlayerVars.MODE_LENGTH) {
 			player.sendMessage(ChatColor.GRAY + "Length-mode selected ...");
 		}else if(mode == PlayerVars.MODE_SURFACE){
 			player.sendMessage(ChatColor.GRAY + "Surface-mode selected ...");
 		}
 	}
 	
 	public static void onCommandClear(PlayerVars tmpVars, Player player){
 	 	tmpVars.removeAllWayPoints();
     	player.sendMessage(ChatColor.RED + "Points-list cleared.");
 	}
 	
 	public static void onCommandLength(PlayerVars tmpVars, Player player){
 		player.sendMessage(ChatColor.WHITE + "Length: " + ChatColor.GOLD + tmpVars.getLenght() + ChatColor.WHITE + " Blocks");
 	}
 	
 	public static void onCommandSwitchMode(PlayerVars tmpVars, Player player, boolean threeD){
     	tmpVars.setignoreHeight(! threeD);
     	
 		if(tmpVars.getignoreHeight()){
         	player.sendMessage(ChatColor.GRAY + "switch to 3D-Mode (does't ignores the height)");
     	}else{
         	player.sendMessage(ChatColor.GRAY + "switch to 2D-Mode (ignores the height)");
     	}
 	}
 	
 	public static void onCommandCountBlocks(PlayerVars tmpVars, Player player){
 		player.sendMessage(ChatColor.GRAY + "Counting Blocks ...  (could take some time)");
 		player.sendMessage(ChatColor.GRAY + "cuboid-volume: " + (int)(tmpVars.getDimensionHieght() * tmpVars.getDimensionWith() * tmpVars.getDimensionLength()) + " Blocks");
 		
 		if (tmpVars.getMode() == PlayerVars.MODE_SURFACE) {
 			int count = tmpVars.countBlocks(player.getWorld());
 			player.sendMessage(ChatColor.WHITE + "Content: " + ChatColor.GOLD + count + ChatColor.WHITE + " Blocks" + ChatColor.GRAY + " (exept air)");
 		}else{
 			player.sendMessage(ChatColor.RED + "This command is only in the 'surface-mode' available - see help (/massband)");
 		}
 	}
 	
 	public static void onCommandDimensions(PlayerVars tmpVars, Player player){
 		if (tmpVars.getMode() == PlayerVars.MODE_SURFACE) {
 			player.sendMessage(ChatColor.WHITE +  "With: " + ChatColor.GOLD + tmpVars.getDimensionWith() + ChatColor.WHITE + " Blocks");
 			player.sendMessage(ChatColor.WHITE +  "Length: " + ChatColor.GOLD + tmpVars.getDimensionLength() + ChatColor.WHITE + " Blocks");
 			player.sendMessage(ChatColor.WHITE +  "Height: " + ChatColor.GOLD + tmpVars.getDimensionHieght() + ChatColor.WHITE + " Blocks");
 		}else{
 			player.sendMessage(ChatColor.RED + "This command is only in the 'surface-mode' available - see help (/massband)");
 		}
 	}
 	
 	public static void printHelpMsg(Command command, Player player){
 		String[] usage = command.getUsage().split("" + (char) 10);
 		
 		for (String line : usage) {
 			if (line.contains("<%item>")) line = line.replaceAll("<%item>", Massband.configFile.itemName);	
 			player.sendRawMessage(ChatColor.GRAY + line);
 		}
 	}
 
 }
