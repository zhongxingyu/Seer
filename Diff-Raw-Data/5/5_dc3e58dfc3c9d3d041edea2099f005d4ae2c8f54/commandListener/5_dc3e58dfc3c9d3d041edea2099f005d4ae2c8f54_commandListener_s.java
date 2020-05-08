 package com.codisimus.plugins.pvpreward.listeners;
 
 import com.codisimus.plugins.pvpreward.PvPReward;
 import com.codisimus.plugins.pvpreward.Record;
 import com.codisimus.plugins.pvpreward.listeners.EntityEventListener.RewardType;
 import java.util.Collections;
 import java.util.Iterator;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 /**
  * Executes Player Commands
  * 
  * @author Codisimus
  */
 public class CommandListener implements CommandExecutor {
     private static enum Action { HELP, OUTLAWS, KARMA, KDR, RANK, TOP, RESET }
     
     /**
      * Listens for PvPReward commands to execute them
      * 
      * @param sender The CommandSender who may not be a Player
      * @param command The command that was executed
      * @param alias The alias that the sender used
      * @param args The arguments for the command
      * @return true always
      */
     @Override
     public boolean onCommand(CommandSender sender, Command command, String alias, String[] args) {
         //Cancel if the command is not from a Player
         if (!(sender instanceof Player))
             return true;
         
         Player player = (Player)sender;
 
         //Display the help page if the Player did not add any arguments
         if (args.length == 0) {
             sendHelp(player);
             return true;
         }
         
         Action action;
         
         try {
             action = Action.valueOf(args[0].toUpperCase());
         }
         catch (Exception notEnum) {
             sendHelp(player);
             return true;
         }
         
         //Execute the correct command
         switch (action) {
             case OUTLAWS:
                 if (args.length == 1)
                     outlaws(player);
                 else
                     sendHelp(player);
                 
                 return true;
                 
             case KARMA:
                 switch (args.length) {
                     case 1: karma(player, player.getName()); return true;
                     case 2: karma(player, args[1]); return true;
                     default: sendHelp(player); return true;
                 }
                 
             case KDR:
                 switch (args.length) {
                     case 1: kdr(player, player.getName()); return true;
                     case 2: kdr(player, args[1]); return true;
                     default: sendHelp(player); return true;
                 }
                 
             case RANK:
                 switch (args.length) {
                     case 1: rank(player, player.getName()); return true;
                     case 2: rank(player, args[1]); return true;
                     default: sendHelp(player); return true;
                 }
                 
             case TOP:
                 switch (args.length) {
                     case 1: top(player, 5); return true;
                         
                     case 2:
                         try {
                             top(player, Integer.parseInt(args[1]));
                             return true;
                         }
                         catch (Exception notInt) {
                             break;
                         }
                         
                     default: break;
                 }
                 
                 sendHelp(player);
                 return true;
                 
             case RESET:
                 switch (args.length) {
                     case 2:
                         if (args[1].equals("kdr"))
                             reset(player, true, player.getName());
                         else if (args[1].equals("karma"))
                             reset(player, false, player.getName());
                         else
                             break;
                         
                         return true;
                         
                     case 3:
                         if (args[1].equals("kdr"))
                             reset(player, true, args[2]);
                         else if (args[1].equals("karma"))
                             reset(player, false, args[2]);
                         else
                             break;
                         
                         return true;
                         
                     default: break;
                 }
                 
                 sendResetHelp(player);
                 return true;
                 
             default: sendHelp(player); return true;
         }
     }
     
     /**
      * Displays the current Outlaws
      *
      * @param player The Player executing the command
      */
     private static void outlaws(Player player) {
         String outlaws = "§eCurrent "+PvPReward.outlawName+"s:§2  ";
         
         //Append the name of each Outlaw
         for (Record record: PvPReward.records)
             if (record.karma > EntityEventListener.amount)
                outlaws.concat(record.name+", ");
         
        player.sendMessage(outlaws);
     }
     
     /**
      * Displays the current karma value of the specified Record
      *
      * @param player The Player executing the command
      * @param name The name of the Record
      */
     private static void karma(Player player, String name) {
         //Return if the Record does not exist
         Record record = PvPReward.findRecord(name);
         if (record == null) {
             player.sendMessage("No PvP Record found for "+name);
             return;
         }
         
         //Add '-' before the karma values if negative is set to true
         if (PvPReward.negative && record.karma != 0) {
             player.sendMessage("§2Current "+PvPReward.karmaName+" level:§b -"+record.karma);
             player.sendMessage("§2"+PvPReward.outlawName+" status at §b-"+ (int)EntityEventListener.amount);
         }
         else {
             player.sendMessage("§2Current "+PvPReward.karmaName+" level:§b "+record.karma);
             player.sendMessage("§2"+PvPReward.outlawName+" status at §b"+ (int)EntityEventListener.amount);
         }
     }
     
     /**
      * Displays the current kdr of the specified Record
      *
      * @param player The Player executing the command
      * @param name The name of the Record
      */
     private static void kdr(Player player, String name) {
         //Return if the Record does not exist
         Record record = PvPReward.findRecord(name);
         if (record == null) {
             player.sendMessage("No PvP Record found for "+name);
             return;
         }
         
         player.sendMessage("§2Current Kills:§b "+record.kills);
         player.sendMessage("§2Current Deaths:§b "+record.deaths);
         player.sendMessage("§2Current KDR:§b "+record.kdr);
     }
     
     /**
      * Displays the current kdr rank of the specified Record
      *
      * @param player The Player executing the command
      * @param name The name of the Record
      */
     private static void rank(Player player, String name) {
         //Return if the Record does not exist
         Record record = PvPReward.findRecord(name);
         if (record == null) {
             player.sendMessage("No PvP Record found for "+name);
             return;
         }
         
         int rank = 1;
         double kdr = record.kdr;
         
         //Increase rank by one for each Record that has a higher kdr
         for (Record tempRecord: PvPReward.records)
             if (tempRecord.kdr > kdr)
                 rank++;
         
         player.sendMessage("§2Current Rank:§b "+rank);
     }
     
     /**
      * Displays the top KDRs
      *
      * @param player The Player executing the command
      * @param amount The amount of KDRs to be displayed
      */
     private static void top(Player player, int amount) {
         player.sendMessage("§eKDR Leaderboard:");
         
         //Sort the Records
         Collections.sort(PvPReward.records);
         
         //Verify that amount is not too big
         int size = PvPReward.records.size();
         if (amount > size)
             amount = size;
         
         Iterator itr = PvPReward.records.iterator();
         Record record;
         
         //Display the name and KDR of the first x Records
         for (int i = 0; i < amount; i++) {
             record = (Record)itr.next();
             player.sendMessage("§2"+record.name+":§b "+record.kdr);
         }
     }
     
     /**
      * Resets kdr or karma values
      *
      * @param player The Player executing the command
      * @param kdr True if reseting kdr, false if reseting karma
      * @param name The name of the Record, 'all' to specify all Records,
      *          or null to specify the record of the given player
      */
     private static void reset(Player player, boolean kdr, String name) {
         //Cancel if the Player does not have the proper permissions
         if (!PvPReward.hasPermisson(player, "reset")) {
             player.sendMessage("You do not have permission to do that.");
             return;
         }
         
         if (kdr) //Reset kdr
             if (name.equals("all")) //Reset all Records
                 for (Record record: PvPReward.records) {
                     record.kills = 0;
                     record.deaths = 0;
                     record.kdr = 0;
                 }
             else { //Reset a specified Record
                 //Use the Record of the given Player if name is null
                 if (name == null)
                     name = player.getName();
                 
                 //Return if the Record does not exist
                 Record record = PvPReward.findRecord(name);
                 if (record == null) {
                     player.sendMessage("No PvP Record found for "+name);
                     return;
                 }
                 
                 record.kills = 0;
                 record.deaths = 0;
                 record.kdr = 0;
             }
         else //Reset karma
             if (name.equals("all")) //Reset all Records
                 for (Record record: PvPReward.records)
                     record.karma = 0;
             else { //Reset a specified Record
                 //Use the Record of the given Player if name is null
                 if (name == null)
                     name = player.getName();
                 
                 //Return if the Record does not exist
                 Record record = PvPReward.findRecord(name);
                 if (record == null) {
                     player.sendMessage("No PvP Record found for "+name);
                     return;
                 }
                 
                 record.karma = 0;
             }
         
         PvPReward.save();
     }
     
     /**
      * Displays the Reset Help Page to the given Player
      *
      * @param player The Player needing help
      */
     private static void sendResetHelp(Player player) {
         player.sendMessage("§e  PvPReward Reset Help Page:");
         player.sendMessage("§2/pvp reset kdr (Player)§b Set kills and deaths to 0");
         player.sendMessage("§2/pvp reset kdr all§b Set everyone's kills and deaths to 0");
         
         //Only display karma commands if the reward type is set to karma
         if (EntityEventListener.rewardType.equals(RewardType.KARMA)) {
             player.sendMessage("§2/pvp reset "+PvPReward.karmaName+" (Player)§b Set "+PvPReward.karmaName+" level to 0");
             player.sendMessage("§2/pvp reset "+PvPReward.karmaName+" all§b Set everyone's "+PvPReward.karmaName+" level to 0");
         }
     }
     
     /**
      * Displays the PvPReward Help Page to the given Player
      *
      * @param player The Player needing help
      */
     private static void sendHelp(Player player) {
         player.sendMessage("§e  PvPReward Help Page:");
         
         //Only display karma commands if the reward type is set to karma
         if (EntityEventListener.rewardType.equals(RewardType.KARMA)) {
             player.sendMessage("§2/pvp "+PvPReward.outlawName+"s§b List current "+PvPReward.outlawName+"s");
             player.sendMessage("§2/pvp "+PvPReward.karmaName+" (Player)§b List current "+PvPReward.karmaName+" level");
         }
         
         player.sendMessage("§2/pvp kdr (Player)§b List current KDR");
         player.sendMessage("§2/pvp rank (Player)§b List current rank");
         player.sendMessage("§2/pvp top (amount)§b List top x KDRs");
         player.sendMessage("§2/pvp reset§b List Admin reset commands");
     }
 }
