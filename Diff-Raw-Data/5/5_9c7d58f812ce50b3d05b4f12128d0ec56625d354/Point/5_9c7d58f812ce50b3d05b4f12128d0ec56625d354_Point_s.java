 package com.github.aphelionpowered.mcnavigator.commands;
 
 import org.bukkit.Location;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import com.github.aphelionpowered.mcnavigator.utilities.Speaker;
 import com.github.aphelionpowered.mcnavigator.utilities.Matcher;
 import org.bukkit.entity.Player;
 
 public class Point {
   public static void Execute(CommandSender sender, String[] args){
 
     if (!(sender instanceof Player)){
       Speaker.denyConsole(sender);
       return;
     } 
 
     if (!sender.hasPermission("mcnavigator.point")){
       Speaker.noPermission(sender);
       return;
     }
 
     if (args.length > 1){
       Speaker.tooManyArguments(sender); 
       return;
     }
 
     if (args.length == 1){
       Player matchedPlayer = Matcher.matchWithPlayer(sender, args[0]);
       Player commandSender = (Player)sender;
       Location targetLocation = matchedPlayer.getLocation();
 
       Speaker.compassPointed(sender, matchedPlayer.getName()); 
       commandSender.setCompassTarget(targetLocation);
       Speaker.compassPointedAt(matchedPlayer, sender.getName());
       
       return;
     }
  return; 
   }
 }
