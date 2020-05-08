 package net.invisioncraft.plugins.salesmania.commands.auction;
 
 import net.invisioncraft.plugins.salesmania.Salesmania;
 import net.invisioncraft.plugins.salesmania.configuration.Locale;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 
 /**
  * Owner: Justin
  * Date: 5/17/12
  * Time: 9:49 AM
  */
 public class AuctionCommandExecutor implements CommandExecutor {
     protected Salesmania plugin;
 
     enum AuctionCommand {
         START, S,
         BID, B,
         END,
         CANCEL,
         INFO,
         IGNORE
     }
 
     AuctionStart auctionStart;
     AuctionBid auctionBid;
     AuctionEnd auctionEnd;
     AuctionCancel auctionCancel;
     AuctionInfo auctionInfo;
     AuctionIgnore auctionIgnore;
 
     public AuctionCommandExecutor(Salesmania plugin) {
         this.plugin = plugin;
 
         // Initialize command handlers
         auctionStart = new AuctionStart(plugin);
         auctionEnd = new AuctionEnd(plugin);
         auctionCancel = new AuctionCancel(plugin);
         auctionBid = new AuctionBid(plugin);
         auctionInfo = new AuctionInfo(plugin);
         auctionIgnore = new AuctionIgnore(plugin);
     }
 
     @Override
     public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
         Locale locale = plugin.getLocaleHandler().getLocale(sender);
         AuctionCommand auctionCommand = null;

        // Syntax
        if(args.length < 1) {
            sender.sendMessage(locale.getMessageList("Syntax.Auction.auction").toArray(new String[0]));
            return false;
        }
         try {
             auctionCommand = AuctionCommand.valueOf(args[0].toUpperCase());
         } catch (IllegalArgumentException ex) {
             sender.sendMessage(locale.getMessageList("Syntax.Auction.auction").toArray(new String[0]));
             return false;
         }
         switch(auctionCommand) {
 
             case START:
             case S:
                 auctionStart.execute(sender, command, label, args);
                 break;
 
             case BID:
             case B:
                 auctionBid.execute(sender, command, label, args);
                 break;
 
             case END:
                 auctionEnd.execute(sender, command, label, args);
                 break;
 
             case CANCEL:
                 auctionCancel.execute(sender, command, label, args);
                 break;
 
             case INFO:
                 auctionInfo.execute(sender, command, label, args);
                 break;
 
             case IGNORE:
                 auctionIgnore.execute(sender, command, label, args);
                 break;
 
             default:
                 return false;
         }
         return true;
     }
 }
