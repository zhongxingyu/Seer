 package me.tehbeard.BeardStat.commands;
 
 import java.util.Date;
 import java.util.HashSet;
 import java.util.Iterator;
 
 import me.tehbeard.BeardStat.BeardStat;
 import me.tehbeard.BeardStat.containers.PlayerStat;
 import me.tehbeard.BeardStat.containers.PlayerStatManager;
 
 import org.bukkit.ChatColor;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 public class statCommand implements CommandExecutor {
 
 
 	public boolean onCommand(CommandSender sender, Command command, String cmdLabel,
 			String[] args) {
 		if(sender instanceof Player){
 			if(!BeardStat.hasPermission((Player)sender, "command.stat")){return true;}
 			if(args.length > 0){
 				if(args[0].equals("-c")){
 					if(args.length==2){
 
 						sender.sendMessage(ChatColor.LIGHT_PURPLE + "getting stats in category");
 						HashSet<String> stats = new HashSet<String>();
 						for( PlayerStat ps :PlayerStatManager.getPlayerBlob(((Player)sender).getName()).getStats()){
 							if(ps.getCat().equals(args[1])){
 								stats.add(ps.getName());
 							}
 						}
 						String msg = "";
 
 						Iterator<String> it = stats.iterator();
 						while(it.hasNext()){
 							for(int i=0;i<10;i++){
 								if(it.hasNext()){
 									if(i>0){msg+=", ";}
 									msg+=it.next();
 								}
 								else
 								{

 									sender.sendMessage(msg);
									msg="";
 									break;
 								}
 							}
							if(!msg.equals("")){
 							sender.sendMessage(msg);
							msg="";}
 						}
 						return true;
 						
 					}else{
 						sender.sendMessage(ChatColor.LIGHT_PURPLE + "getting categories");
 						HashSet<String> cats = new HashSet<String>();
 						for( PlayerStat ps :PlayerStatManager.getPlayerBlob(((Player)sender).getName()).getStats()){
 							if(!cats.contains(ps.getCat())){
 								cats.add(ps.getCat());
 							}
 						}
 						String msg = "";
 
 						Iterator<String> it = cats.iterator();
 						while(it.hasNext()){
 							for(int i=0;i<10;i++){
 								if(it.hasNext()){
 									if(i>0){msg+=", ";}
 									msg+=it.next();
 								}
 								else
 								{
 									sender.sendMessage(msg);
 									break;
 								}
 							}
 							sender.sendMessage(msg);
 							msg="";
 						}
 						return true;
 
 					}
 				}
 
 
 				for(String arg: args){
 					String[] part = arg.split("\\.");
 					for(String p :part){
 						BeardStat.printDebugCon(p);	
 					}
 
 					if(part.length==2){
 						BeardStat.printDebugCon("sending stat to player"); 
 
 						if(PlayerStatManager.getPlayerBlob(((Player)sender).getName()).hasStat(part[0],part[1])){
 							sender.sendMessage(arg +": " + PlayerStatManager.getPlayerBlob(((Player)sender).getName()).getStat(part[0],part[1]).getValue());
 						}
 						else
 						{
 							sender.sendMessage("not found");
 						}
 					}
 					else
 					{
 						sender.sendMessage(arg + " not found!");
 					}
 
 
 				}
 			}
 			else
 			{
 				sender.sendMessage(ChatColor.GOLD + "-= your Stats =-");
 
 				//send playtime
 				if(BeardStat.loginTimes.containsKey((((Player)sender).getName()))){
 					long seconds = PlayerStatManager.getPlayerBlob(((Player)sender).getName()).getStat("stats","playedfor").getValue() +
 							(((new Date()).getTime() - BeardStat.loginTimes.get(((Player)sender).getName()))/1000);
 					int weeks   = (int) seconds / 604800;
 					int days = (int)Math.ceil((seconds -604800*weeks) / 86400);
 					int hours = (int)Math.ceil((seconds - (86400 * days + 604800*weeks)) / 3600);
 					int minutes = (int)Math.ceil((seconds - (604800*weeks + 86400 * days + 3600 * hours)) / 60);
 
 
 					sender.sendMessage(ChatColor.LIGHT_PURPLE + "playtime: " + ChatColor.WHITE+ 
 							weeks + ChatColor.LIGHT_PURPLE +  " wks " + ChatColor.WHITE +
 							days + ChatColor.LIGHT_PURPLE + " days " + ChatColor.WHITE+
 							hours + ChatColor.LIGHT_PURPLE + " hours " + ChatColor.WHITE+
 							minutes + ChatColor.LIGHT_PURPLE + " mins ");
 				}
 
 				//display default stats
 				String[] stats = {"stats.totalblockcreate","stats.totalblockdestroy","kills.total","deaths.total"};
 				String[] statTitle = {"blocks placed","blocks broken","kills","deaths"};
 				int i =0;
 				for(String stati:stats){
 					String cat=null;
 					String stat=null;
 					if(stati.split("\\.").length==2){
 						cat = stati.split("\\.")[0];
 						stat = stati.split("\\.")[1];
 						BeardStat.printDebugCon(cat + " -> " + stat);
 					}
 
 					if(PlayerStatManager.getPlayerBlob(((Player)sender).getName()).hasStat(cat,stat)){
 						sender.sendMessage(ChatColor.LIGHT_PURPLE + statTitle[i]+ ": " + ChatColor.WHITE + PlayerStatManager.getPlayerBlob(((Player)sender).getName()).getStat(cat,stat).getValue());
 					}
 					i+=1;
 				}
 			}
 			return true;
 		}
 
 		return false;
 	}
 
 
 }
