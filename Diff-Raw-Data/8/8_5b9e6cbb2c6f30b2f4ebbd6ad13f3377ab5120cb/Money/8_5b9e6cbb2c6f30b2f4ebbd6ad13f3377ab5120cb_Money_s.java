 package com.hotmail.shinyclef.shinybridge.cmdadaptations;
 
 import com.hotmail.shinyclef.shinybridge.*;
 import net.milkbowl.vault.economy.Economy;
 import org.bukkit.ChatColor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.event.player.PlayerCommandPreprocessEvent;
 
 /**
  * User: Shinyclef
  * Date: 6/10/13
  * Time: 4:56 PM
  */
 
 public class Money extends AdaptedCommand
 {
     private static Economy economy;
     private static final String MONEY_TAG = ChatColor.DARK_GREEN + "[" + ChatColor.WHITE + "Money" +
             ChatColor.DARK_GREEN + "] ";
 
     public static void initialize(Economy economy)
     {
         Money.economy = economy;
         if (economy != null)
         {
             registerCommand("/money");
         }
     }
 
     public static void processMoney(PlayerCommandPreprocessEvent e, CommandSender sender, String[] args)
     {
         //command: /money
         if (args.length == 0)
         {
             showBalance(e, sender, sender.getName());
         }
 
         //command: money [something]
         if (args.length == 1)
         {
             if (!sender.hasPermission(CmdExecutor.MOD_PERM))
             {
                 return;
             }
 
             switch (args[0].toLowerCase())
             {
                 case "help": case "top": case "purge": case "empty":
                 return;
 
                 //we're looking at someone else's account
                 default:
                     showBalance(e, sender, args[0]);
                     return;
             }
         }
 
         if (args.length == 3)
         {
             if (args[0].equalsIgnoreCase("pay"))
             {
                 pay(e, sender, args);
             }
         }
     }
 
     public static void showBalance(PlayerCommandPreprocessEvent e, CommandSender sender, String targetPlayerName)
     {
         //we only need to deal with client side
         if (!(sender instanceof MCServer.ClientPlayer))
         {
             return;
         }
 
         //cancel original event
         e.setCancelled(true);
 
         String targetLabel = "";
         if (!sender.getName().equals(targetPlayerName))
         {
             if (!sender.hasPermission(CmdExecutor.MOD_PERM))
             {
                 sender.sendMessage(CmdExecutor.NO_PERM);
                 return;
             }
             targetLabel = targetPlayerName + "'s ";
         }
 
         //get target's balance
         String balance = economy.format(economy.getBalance(targetPlayerName));
         sender.sendMessage(MONEY_TAG + targetLabel + "Balance: " + ChatColor.WHITE + balance);
     }
 
     public static void pay(PlayerCommandPreprocessEvent e, CommandSender sender, String[] args)
     {
         String recipient = args[1];
         String amountString = args[2];
         Double amount;
 
         //we only need to inform recipients on r+
         if (!MCServer.isClientOnline(recipient))
         {
             return;
         }
 
         try
         {
             amount = Double.parseDouble(amountString);
         }
         catch (NumberFormatException ex)
         {
             return;
         }
 
         if (!economy.hasAccount(recipient))
         {
             return;
         }
 
         if (!economy.has(sender.getName(), amount))
         {
             return;
         }
 
         //we're all clear, make it happen
         NetProtocolHelper.sendToClientPlayerIfOnline(recipient, MONEY_TAG + ChatColor.WHITE + sender.getName() +
                 ChatColor.DARK_GREEN + " has sent you " + ChatColor.WHITE + economy.format(amount) +
                 ChatColor.DARK_GREEN + ".");
     }
 }
