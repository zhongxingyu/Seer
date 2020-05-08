 /* 
  * Muni 
  * Copyright (C) 2013 bobbshields <https://github.com/xiebozhi/Muni> and contributors
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program. If not, see <http://www.gnu.org/licenses/>.
  * 
  * Binary releases are available freely at <http://dev.bukkit.org/server-mods/muni/>.
 */
 package com.teamglokk.muni.commands;
 
 import com.teamglokk.muni.Citizen;
 import com.teamglokk.muni.Muni;
 import com.teamglokk.muni.Town;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 import org.bukkit.ChatColor;
 
 import java.util.Iterator;
 /**
  * Handler for the /town command.
  * @author BobbShields
  */
 public class TownCommand implements CommandExecutor {
     private Muni plugin;
     private Player player;
     
     public TownCommand (Muni instance){
         plugin = instance;
     }
     @Override
     public boolean onCommand(CommandSender sender, Command command, String label, String[] split) {
         String [] args = plugin.trimSplit(split);
         
         if (args.length == 0){
             displayHelp(sender);
             return true;
         } else if (args[0].equalsIgnoreCase("list")) { 
             if (args.length != 1) {
                 plugin.out(sender,"/town list (no parameters allowed)");
                 return false;
             }
             plugin.out(sender,"List of towns:");
             // iteration will be required here
             Iterator<Town> itr = plugin.towns.values().iterator();
             if (!itr.hasNext() ){
                 plugin.out(sender,"There are no towns to check");
                 return true;
             }
             while (itr.hasNext() ){
                 Town current = itr.next();
                 plugin.out(sender,current.getName() ) ;
             }
             return true;
         } else if (args[0].equalsIgnoreCase("rankings")) { 
             if (args.length != 1) {
                 plugin.out(sender,"/town rankings (no parameters allowed)");
                 return false;
             }
             plugin.displayTownRankings(sender);
             return true;
         } else if (args[0].equalsIgnoreCase("info")) { 
             if(args.length>2){
                 plugin.out(sender,"/town info <town_Name> "+ChatColor.RED+"OR" +ChatColor.WHITE+" /town info (this is for your own town",ChatColor.WHITE); 
                 return true;
             } else if (args.length==1){
                 if (sender instanceof Player && plugin.isCitizen(sender.getName() ) ) {
                     plugin.getTown(plugin.getTownName( sender.getName() ) ).info(sender); //NPE when /town info && no town
                 } else { plugin.out(sender, "You must specify a town"); }
             }else if (args.length == 2) { 
                 if ( plugin.towns.containsKey(args[1]) ) {
                     plugin.getTown( args[1] ).info(sender);
                 } else { sender.sendMessage(args[1]+" is not a valid town.  (/town list)"); }
             }
             return true;
         } else if (args[0].equalsIgnoreCase("help") ) { 
             displayHelp(sender);
             return true;
         }  
         //End of console commands
         
         if (!(sender instanceof Player)) {
             sender.sendMessage("You cannot send that command from the console");
             return true;
         } else { 
             player = (Player) sender; 
             if (!plugin.econwrapper.hasPerm(player, "muni.town") ){
                 player.sendMessage("You do not have permission to run /town subcommands");
                 return true; 
             }
         }
         
         if (args[0].equalsIgnoreCase("makeHome")) { 
             boolean rtn = false; 
             Town temp = plugin.getTown( plugin.getTownName( player.getName() ) );
             if (args.length == 1 ) {
                 plugin.wgwrapper.makeHome(player);
             } else {
                 player.sendMessage("/town makeHome - incorrect number of parameters");
             }
             return rtn;
         } else if (args[0].equalsIgnoreCase("makeShop")) { 
             boolean rtn = false; 
             Town temp = plugin.getTown( plugin.getTownName( player.getName() ) );
             if (args.length == 1 ) {
                 plugin.wgwrapper.makeShop(player);
             } else {
                 player.sendMessage(("/town makeShop - incorrect number of parameters"));
             }
             return rtn;
         } else if (args[0].equalsIgnoreCase("payTaxes")) { 
            boolean rtn = false; 
             Town temp = plugin.getTown(plugin.getTownName( player.getName() ) );
             if (args.length == 2 ) {
                 Double amount = plugin.parseD( args[1] );
                 rtn = temp.payTaxes(player, amount, 0 );
             } else if ( args.length == 1 ){
                 rtn = temp.payTaxes(player);
             } 
            return rtn;
             
         } else if (args[0].equalsIgnoreCase("apply")) { 
             if (args.length != 2) {
                 player.sendMessage("Incorrect number of parameters");
                 return false;
             }
             
             if (!plugin.isCitizen(player.getName()) ){
                 Town temp = plugin.getTown( args[1] );
                 if (temp == null) { 
                     player.sendMessage("Check the spelling"); 
                     return true; 
                 }
                 temp.apply ( player ); 
                 player.sendMessage("Application to "+temp.getName()+" was sent.");
                 temp.messageOfficers(player.getName() + " has applied to your town");
                 return true;
             } else { 
                 player.sendMessage("You are already engaged with "+ plugin.allCitizens.get(player.getName() ) );
                 player.sendMessage("To clear your status, do /town leave");
                 return true;
             }
         } else if (args[0].equalsIgnoreCase("accept")) { //working - 19 Feb
             if (args.length != 1) {
                 player.sendMessage("/town accept (no parameters, do /town viewInvite)");
                 return false;
             }
             Town temp = plugin.getTown( plugin.getTownName( player.getName() ) );
             temp.acceptInvite(player);
             
             return true;
         } else if (args[0].equalsIgnoreCase("viewInvite")) { 
             if (args.length != 1) {
                 player.sendMessage("/town viewInvite (no parameters)");
                 return false;
             }
             if (!plugin.isCitizen(player) ) {
                 plugin.out(player, "You are not engaged with any town.");
                 return true;
             }
             Town temp = plugin.getTown( plugin.getTownName( player.getName() ) );
             if (temp.isInvited(player) ){
                 plugin.out(player,"You are invited to "+temp.getName() ); 
             } else { 
                 plugin.out(player,"You are not an invitee of " + temp.getName() );
             }
             return true;
         } else if (args[0].equalsIgnoreCase("leave")) { 
             Town temp = plugin.getTown( plugin.getTownName( player.getName() ) );
             if ( temp.leave(player) ){
             }
             return true;
         }else if (args[0].equalsIgnoreCase("sethome")) {
             player.sendMessage("Sethome not yet added.");
             return true;
         }else if (args[0].equalsIgnoreCase("vote")) {
             player.sendMessage("Voting not yet added.");
             return true;
         } else if (args[0].equalsIgnoreCase("bank")) { 
             if (!plugin.isCitizen(player) ){ 
                 player.sendMessage("You are not a member of a town" ); 
                 return true;
             }
             Town temp = plugin.getTown( plugin.getTownName( player.getName() ) );
             player.sendMessage(temp.getName()+" has bank balance of "+temp.getBankBal()); //NPE if player doesn't have town
             return true;
         }  else if (args[0].equalsIgnoreCase("signCharter")) {
             player.sendMessage("Charters not yet enabled ");
             return true;
         } else {
             player.sendMessage("[Muni] Input not understood.");
             displayHelp(player);
             return true;
         }
     }
     private void displayHelp(CommandSender player){
         plugin.out( player,"Muni Help.  You can do these commands:",ChatColor.LIGHT_PURPLE);
         plugin.out( player, "/town list");
         plugin.out( player, "/town rankings");
         plugin.out( player, "/town info <optional:townName>");
         plugin.out( player, "/town apply <townName>");
         plugin.out( player, "/town viewInvite");
         plugin.out( player, "/town accept");
         plugin.out( player, "/town leave");
         //plugin.out( player,"/town sethome");
         //plugin.out( player,"/town signCharter");
         plugin.out( player, "/town payTaxes <optional: amount>");
         plugin.out( player, "/town makeHome");
         plugin.out( player, "/town makeShop");
         plugin.out( player, "/town bank (check the town bank balance)");
         plugin.out( player,"Future: /town vote");
     }
 }
