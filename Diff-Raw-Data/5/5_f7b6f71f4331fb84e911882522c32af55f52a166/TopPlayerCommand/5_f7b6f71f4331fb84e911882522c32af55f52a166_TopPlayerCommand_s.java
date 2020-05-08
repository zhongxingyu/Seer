 package me.tehbeard.BeardStat.commands;
 
 import java.text.SimpleDateFormat;
 import java.util.List;
 
 
 import me.tehbeard.BeardStat.BeardStat;
 import me.tehbeard.BeardStat.containers.PlayerStatManager;
 import me.tehbeard.BeardStat.containers.TopPlayer;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 public class TopPlayerCommand implements CommandExecutor {
 
 	private PlayerStatManager topPlayedManager;
 	private List<String> cats;
 
 	
 	public TopPlayerCommand(PlayerStatManager topPlayedManager) {
 		this.topPlayedManager = topPlayedManager;
 	}
 	
 
     public boolean onCommand(CommandSender sender, Command command, String cmdLabel, String[] args) {
 
     	if(!BeardStat.hasPermission(sender, "command.topplayer")){
 			BeardStat.sendNoPermissionError(sender);
         	return true;
         }
     	
     	String selectedcat = "playedfor";
     	
     	if(args.length > 0){
             BeardStat.printDebugCon("Called topplayer with args: " + args[0]);
 
     	    if(args[0].equalsIgnoreCase("-c")){
     	        if(getCategories() != null){
    	            String categorylist = "ChatColor.LIGHT_PURPLE";
     	            sender.sendMessage(ChatColor.GOLD + "Possible categories for topplayer command: ");
         	        for(String cat:getCategories()){
         	            categorylist += cat + " ";
         	        }
        	        sender.sendMessage(categorylist);
     	        }
                 return true;
     	    }
     	    else if(args[0].equalsIgnoreCase("-h") || args[0].equals("?")){
     	        sender.sendMessage(ChatColor.GREEN + "Stats TopPlayer Help page");
     	        sender.sendMessage(ChatColor.GREEN + "/topplayer : Default display of top players on the server");
                 sender.sendMessage(ChatColor.GREEN + "/topplayer -c : List of possible top player categories");
                 sender.sendMessage(ChatColor.GREEN + "/topplayer -h : This message");
                 sender.sendMessage(ChatColor.GREEN + "/topplayer <categoryname> : Display of top players for a specific category");
                 return true;
     	    }
     	    else{
     	        selectedcat = args[0];
     	    }
     	}
     	
    		List<TopPlayer> topPlayers = topPlayedManager.getTopPlayers(selectedcat);
     	
     	if(topPlayers == null) { 
     	    sender.sendMessage(ChatColor.RED + "Unable to retrieve the top players, category is not found.");
     	    BeardStat.printDebugCon("TopPlayer is not supported in a flat file.  You need to upgrade to mySql");
     	    return true;
     	}
     	
   
     	SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd");
 
     	sender.sendMessage("/--------------------------------------------------------\\");
     	sender.sendMessage("| " + ChatColor.RED + "Top " + padLeft(Integer.toString(BeardStat.self().getTopPlayerCount()), 3) + " players of all time by " + padRight(selectedcat + ":", 24) + "|");
     	sender.sendMessage("| " + ChatColor.GOLD + "Rank " + ChatColor.WHITE + 
     			"| " + ChatColor.DARK_RED + "Player           " + ChatColor.WHITE + 
     			"| " + ChatColor.DARK_GRAY + "Stat            " + ChatColor.WHITE + 
     			"| " + ChatColor.GRAY + "FirstLogin " + ChatColor.WHITE + "|");
     	sender.sendMessage("|------|------------------|-----------------|------------|");
 		
     	for(TopPlayer item : topPlayers){
     		Player player = Bukkit.getPlayer(item.playername);
     		String coloredplayer = item.playername; //Bukkit.getOfflinePlayer(item.playername).getPlayer().getPlayerListName();
     		if(player != null){
     		  coloredplayer = player.getDisplayName();	
     		}
  			sender.sendMessage("| " + ChatColor.GOLD + padLeft(Integer.toString(item.rank), 4) + ChatColor.WHITE + 
 	    			" | " + padRight(coloredplayer, 16) + ChatColor.WHITE + 
 	    			" | " + ChatColor.DARK_GRAY + padRight(item.time, 16) + ChatColor.WHITE + 
 	    			" | " + ChatColor.GRAY + f.format(item.firstOn) + ChatColor.WHITE + " |");
 		}
     	sender.sendMessage("\\--------------------------------------------------------/");
 		
 		return true;
     }
     
     private static String padRight(String s, int n) { 
         return String.format("%1$-" + n + "s", s);   
     }
     
     private static String padLeft(String s, int n) { 
         return String.format("%1$#" + n + "s", s);   
     } 
     
     private List<String> getCategories(){
         if(this.cats == null){
             cats = topPlayedManager.getAllStatsInCategory("stats");
         }
         return cats;
     }
 
 
 }
