 package com.undeadscythes.udsplugin.commands;
 
 import com.undeadscythes.udsplugin.Color;
 import com.undeadscythes.udsplugin.*;
 import org.bukkit.*;
 
 /**
  * Rent VIP rank and perform other tasks. Sends help on wrong arguments.
  * @author UndeadScythes
  */
 public class VIPCmd extends CommandWrapper {
     @Override
     public void playerExecute() {
         if(args.length == 0) {
            if(player.hasPermission(Perm.VIP)) {
                 player.sendNormal("You have " + player.getVIPTimeString()+ " left in VIP.");
             } else if(canAfford(UDSPlugin.getConfigInt(ConfigRef.VIP_COST)) && notJailed() && hasPerm(Perm.VIP_BUY)) {
                 player.setRank(PlayerRank.VIP);
                 player.setVIPTime(System.currentTimeMillis());
                 player.setVIPSpawns(UDSPlugin.getConfigInt(ConfigRef.VIP_SPAWNS));
                 player.sendNormal("Welcome to the elite, enjoy your VIP status.");
             }
         } else if(maxArgsHelp(2)) {
             if(subCmd.equals("spawns")) {
                 int page;
                 if(args.length == 2 && (page = parseInt(args[1])) != -1) {
                     sendPage(page, player);
                 } else {
                     sendPage(1, player);
                 }
             } else {
                 subCmdHelp();
             }
         }
     }
 
     private void sendPage(final int page, final SaveablePlayer player) {
         final int pages = (UDSPlugin.getVipWhitelist().size() + 8) / 9;
         if(pages == 0) {
             player.sendNormal("There are no currently whitelisted items.");
         } else if(page > pages) {
             player.sendMessage(Message.NO_PAGE);
         } else {
             player.sendNormal("--- VIP Item Whitelist " + (pages > 1 ? "Page " + page + "/" + pages + " " : "") + "---");
             int posted = 0;
             int skipped = 1;
             for(Material i : UDSPlugin.getVipWhitelist()) {
                 if(skipped > (page - 1) * 9 && posted < 9) {
                     String item = i.toString();
                     item = item.substring(0, 1).toUpperCase().concat(item.substring(1, item.length()).toLowerCase());
                     player.sendListItem(item + " (", i + "" + Color.ITEM  + ")");
                     posted++;
                 } else {
                     skipped++;
                 }
             }
         }
     }
 }
