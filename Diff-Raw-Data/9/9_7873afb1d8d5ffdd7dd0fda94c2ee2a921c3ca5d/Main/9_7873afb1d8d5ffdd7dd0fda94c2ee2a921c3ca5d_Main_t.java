// Version 1.0.0
 package com.desiringsolid.info;
 import org.bukkit.ChatColor;
 import org.bukkit.GameMode;
 import org.bukkit.Location;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.plugin.java.JavaPlugin;
  
 public class Main extends JavaPlugin{
  
     @Override
     public void onEnable(){
     	getServer().getPluginManager().registerEvents(new MyPlayerListener(), this);
     }
     public void onDisable(){
     // code disable
     }
     
     // Command List
 public boolean onCommand(CommandSender sender, Command command,
         String commandLabel, String[] args)
 {
     if (command.getName().equalsIgnoreCase("survival"))
         return SurvivalGameMode(sender, args);
     if (command.getName().equalsIgnoreCase("creative"))
         return CreativeGameMode(sender, args);
     if (command.getName().equalsIgnoreCase("tp"))
     	return Teleport(sender, args);
     if (command.getName().equalsIgnoreCase("tp2"))
     	return Teleport2(sender, args);
     return true;
 }
  
     // Command Executes
 private boolean SurvivalGameMode(CommandSender sender, String[] args)
 {
     Player player = (Player) sender;
     if(sender instanceof Player){
     player.setGameMode(GameMode.SURVIVAL);
     player.sendMessage(ChatColor.RED + "Survival Mode enabled!");
     }
     return true;
     }
 private boolean CreativeGameMode(CommandSender sender, String[] args)
 {
     Player player = (Player) sender;
     if(sender instanceof Player){
     player.setGameMode(GameMode.CREATIVE);
     player.sendMessage(ChatColor.GREEN + "Creative Mode enabled!");
     }
     return true;
 	}
 private boolean Teleport(CommandSender sender, String[] args)
 {
 	Player player = (Player) sender;
 	if(args.length == 0){
 		player.sendMessage(ChatColor.RED + "Usage: /tp <playername>");
 	}else if(args.length == 1){
 		Player targetPlayer = player.getServer().getPlayer(args[0]);
 		Location targetPlayerLocation = targetPlayer.getLocation();
 		player.teleport(targetPlayerLocation);
 	}
     return true;
 	}
 private boolean Teleport2(CommandSender sender, String[] args)
 {
 	Player player = (Player) sender;
 	if(args.length == 0){
 		player.sendMessage(ChatColor.RED + "Usage: /tp2 <playername>");
 	}else if(args.length == 1){
 			Player targetPlayer = player.getServer().getPlayer(args[0]);
 			Location location1 = player.getLocation();
 			targetPlayer.teleport(location1);
 	}
     return true;
 	}
 }
