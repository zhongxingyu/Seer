 package com.Jessy1237.RTest;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.command.ConsoleCommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.event.player.PlayerChatEvent;
 import org.bukkit.event.player.PlayerListener;
 
 public class RTestPlayerListener extends PlayerListener{
 	static String message = "";
 	static int EorD = 0;
 	static int seconds = 0;
 	public static Main plugin;
 		
 	public RTestPlayerListener(Main instance){
 		plugin = instance;
 	}
 	
 	public static void enable(){
 		EorD = 1;
 	}
 	
 	public static void disable(){
 		EorD = 0;
 	}
 
 	public void onPlayerChat(PlayerChatEvent event){
 		Player player = event.getPlayer();
 		String name = player.getDisplayName();
 		if(player.hasPermission("RTest.Member")||player.hasPermission("RTest.Admin")){
 			Player players[] = plugin.getServer().getOnlinePlayers();
 			if((event.getMessage()).equals((loadRnum.dNum1))  && EorD == 1){
 				ConsoleCommandSender console = Bukkit.getConsoleSender();
 				plugin.getServer().dispatchCommand(console, "money give " + name + " " + Main.reward());
 				for(int i = 0; i <players.length; i++)
 					players[i].sendMessage((new StringBuilder()).append(ChatColor.RED).append("The Winner is: " + name + ".").append(message).toString());
 				Main.log.info("[RTest] Displaying Winner to Players...");
 				disable();
 				if((Main.b) == 1){
 					Main.t.start();
 					Main.b = 0;
 				}
 			}
 		}else{
			if((event.getMessage()).equals((loadRnum.dNum1)) && EorD == 1){
 				player.sendMessage(ChatColor.GOLD + "You don't have permission to win a Reaction Test");
 				player.sendMessage(ChatColor.GOLD + "Congratulations Though for Getting it right!");
 			}else{
 				if(EorD == 1){
 					player.sendMessage(ChatColor.GOLD + "You don't have permission to win a Reaction Test");
 				}
 			}
 		}
 	}
 }
