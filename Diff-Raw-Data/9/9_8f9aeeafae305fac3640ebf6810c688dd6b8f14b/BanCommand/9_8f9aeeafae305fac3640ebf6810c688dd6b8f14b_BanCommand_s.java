 package com.minecarts.bouncer.command;
 
 import com.minecarts.bouncer.Bouncer;
 import com.minecarts.bouncer.CommandHandler;
 import org.bukkit.Bukkit;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 import java.util.List;
 
 public class BanCommand extends CommandHandler {
 
     private int taskId = 0;
 
     public BanCommand(Bouncer plugin){
         super(plugin);
     }
 
     @Override
     public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
         if(!sender.hasPermission("bouncer.ban")) return true;
 
         int argTracker = 0;
 
         String identifier = "";
         Integer duration = 60 * 24 * 365;
         String reason = "";
 
         //Try and match this player
             List<Player> players = Bukkit.matchPlayer(args[argTracker]);
             if(players.size() > 1){
                 sender.sendMessage("Error: Matched " + players.size() + " players when attempting to ban. Be more specific.");
                 return true;
             }
             if(players.size() == 1){
                 identifier = players.get(0).getName();
             } else {
                 identifier = args[argTracker];
             }
         
         //Get the duration and reason (if any)
             if(args.length > 1){
                 argTracker++;
                 try {
                     duration = Integer.parseInt(args[argTracker]);
                     argTracker++; //Increment the arg tracker because we matched args[1] now
                 } catch (NumberFormatException e){ // wasn't an integer
                 }
 
                 //Get the reason from the remaining args
                 for(int i = argTracker; i < args.length; i++){
                     reason = reason.concat(args[i]);
                     if(i != args.length){
                         reason = reason.concat(" ");
                     }
                 }
             }
 
         reason = (reason.equals("")) ? plugin.getConfig().getString("messages.BAN") : reason;
         plugin.banIdentifier(identifier,duration,reason,sender);
         if(players.size() == 1){
            players.get(0).kickPlayer(reason);
         }
         return true;
     }
 }
