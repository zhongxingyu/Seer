 package com.leetclan.snoopy.commands;
 
 import java.util.Set;
 
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 import com.dthielke.herochat.Channel;
 import com.dthielke.herochat.Herochat;
 import com.leetclan.snoopy.SnoopingPlayer;
 import com.leetclan.snoopy.Snoopy;
 import com.leetclan.snoopy.util.Chat;
 
 public class SnoopCommandExecutor extends CommandExecutorChain {
   public static class OffCommand extends BasicCommandExecutor {
     public OffCommand(Snoopy snoopy) {
       super(snoopy);
     }
 
     public boolean onCommand(CommandSender sender, Command command,
         String label, String[] args) {
       
      SnoopingPlayer snooper = getSnoopy().getSnooper(sender.getName());
      snooper.setSnoopAllChannels(false);
      snooper.setSnoopAllPlayers(false);
      
      getSnoopy().removeSnooper(snooper.getPlayer().getName());
      
       Chat.tell(getSnoopy().getServer().getPlayer(sender.getName()), "{red}Snooping disabled for all targets.");
       return true;
     }
 
     @Override
     public String[] getTriggeringCommands() {
       return new String[] {
         "off",
         "stop"
       };
     }
   }
   
   public static abstract class SnoopTargetCommand extends BasicCommandExecutor {
     public SnoopTargetCommand(Snoopy snoopy) {
       super(snoopy);
     }
     
     public abstract void snoopAll(SnoopingPlayer snooper);
     public abstract int addTarget(SnoopingPlayer snooper, String targetName);
     public abstract int removeTarget(SnoopingPlayer snooper, String targetName);
 
     public boolean onCommand(CommandSender sender, Command command,
         String label, String[] targetNames) {
       
       SnoopingPlayer snooper = getSnoopy().getSnooper(sender.getName());
       
       if (targetNames.length == 0) {
         snoopAll(snooper);
         return true;
       }
       
       int numNewTargets = 0;
       int numTargetsRemoved = 0;
       
       for (String targetName : targetNames) {
         boolean addTarget = true;
         
         if (targetName.startsWith("-")) {
           targetName = targetName.substring(1);
           addTarget = false;
         }
 
         if (addTarget) {
           numNewTargets += addTarget(snooper, targetName);
         } else {
           numTargetsRemoved += removeTarget(snooper, targetName);
         }
       }
       
       Chat.tell(snooper.getPlayer(), "Snooping targets updated: {green}+%d{/}, {red}-%d{/}", numNewTargets, numTargetsRemoved);
       
       return true;
     }
   }
   
   public static class SnoopPlayersCommand extends SnoopTargetCommand {
     public SnoopPlayersCommand(Snoopy snoopy) {
       super(snoopy);
     }
 
     @Override
     public void snoopAll(SnoopingPlayer snooper) {
       snooper.setSnoopAllPlayers(true);
     }
 
     @Override
     public int addTarget(SnoopingPlayer snooper, String targetName) {
       Set<Player> players = getSnoopy().getPlayerLookup().matchPlayers(targetName);
       
       switch (players.size()) {
         case 0:
           Chat.scold(snooper.getPlayer(), "'%s' did not match any players.", targetName);
           return 0;
           
         case 1:
           Player target = players.toArray(new Player[0])[0];
           
           if (target == snooper.getPlayer()) {
             return 0;
           }
             
           snooper.snoopOn(target);
           return 1;
         
         default:
           Chat.scold(snooper.getPlayer(), "Target name '%s' is ambiguous. Did you mean:", targetName);
           
           for (Player player : players) {
             Chat.scold(snooper.getPlayer(), "  - %s", player.getName());
           }
           
           return 0;
       }
     }
 
     @Override
     public int removeTarget(SnoopingPlayer snooper, String targetName) {
       Set<Player> players = getSnoopy().getPlayerLookup().matchPlayers(targetName);
       int numTargetsRemoved = 0;
       
       for (Player player : players) {
         if (snooper.isSnoopingOn(player)) {
           numTargetsRemoved++;
           snooper.stopSnoopingOn(player);
         }
       }
       
       return numTargetsRemoved;
     }
     
     @Override
     public String[] getTriggeringCommands() {
       return new String[] {
           "player",
           "players",
           "people",
           "ppl",
           "p"
       };
     }
   }
   
   public static class SnoopChannelsCommand extends SnoopTargetCommand {
     public SnoopChannelsCommand(Snoopy snoopy) {
       super(snoopy);
     }
 
     @Override
     public void snoopAll(SnoopingPlayer snooper) {
       snooper.setSnoopAllChannels(true);
     }
 
     @Override
     public int addTarget(SnoopingPlayer snooper, String targetName) {
       Channel channel = Herochat.getChannelManager().getChannel(targetName);
       
       if (channel != null) {
         if (channel.isMember(Herochat.getChatterManager().getChatter(snooper.getPlayer()))) {
           Chat.tell(snooper.getPlayer(), "{yellow}- you are already in channel '%s'", targetName);
           return 0;
         }
         
         if (!snooper.isSnoopingOn(channel)) {
           snooper.snoopOn(channel);
           return 1;
         }
       } else {
         Chat.scold(snooper.getPlayer(), "- channel '%s' does not exist.", targetName);
       }
       
       return 0;
     }
 
     @Override
     public int removeTarget(SnoopingPlayer snooper, String targetName) {
       Channel channel = Herochat.getChannelManager().getChannel(targetName);
       
       if (channel != null) {
         if (snooper.isSnoopingOn(channel)) {
           snooper.stopSnoopingOn(channel);
           return 1;
         }
       } else {
         Chat.scold(snooper.getPlayer(), "- channel '%s' does not exist.", targetName);
       }
 
       return 0;
     }
     
     @Override
     public String[] getTriggeringCommands() {
       return new String[] {
           "channel",
           "channels",
           "chans",
           "chan",
           "c"
       };
     }
   }
 
   public SnoopCommandExecutor(Snoopy snoopy, BasicCommandExecutor...subCommands) {
     super(snoopy, subCommands);
   }
 
   public String[] getTriggeringCommands() {
     return new String[] {
       "snoop"
     };
   }
 
   @Override
   protected boolean onCommand(CommandSender sender, String label, String[] args) {
     if (!getSnoopy().isSnooping(sender.getName())) {
       getSnoopy().addSnooper(sender.getName());
     }
 
     return true;
   }
 }
