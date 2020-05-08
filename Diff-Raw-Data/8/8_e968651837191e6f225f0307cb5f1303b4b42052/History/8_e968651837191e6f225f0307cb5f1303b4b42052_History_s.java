 package edgruberman.bukkit.doorman.commands;
 
 import java.util.List;
 
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 import edgruberman.bukkit.doorman.Main;
 import edgruberman.bukkit.doorman.RecordKeeper;
 import edgruberman.bukkit.doorman.RecordKeeper.Message;
 
 public class History implements CommandExecutor {
 
    private static final int PAGE_SIZE = 10;
 
     private final RecordKeeper records;
 
     public History(final RecordKeeper records) {
         this.records = records;
     }
 
     // usage: /<command>[ <Page>]
     @Override
     public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args) {
         final long lastPlayed = ( sender instanceof Player ? ((Player) sender).getLastPlayed() : System.currentTimeMillis() );
 
         final List<Message> history = this.records.getHistory();
 
        final int bodySize = History.PAGE_SIZE - Main.courier.compose("history|footer").size();
 
         final int pageTotal = (history.size() / bodySize) + ( history.size() % bodySize > 0 ? 1 : 0 );
         final int pageCurrent = ( args.length >= 1 ? History.parseInt(args[0], 1) : 1 );
         if (pageCurrent <= 0 || pageCurrent > pageTotal) {
             Main.courier.send(sender, "unknown-page", pageCurrent);
             return false;
         }
 
         final int first = (pageCurrent - 1) * bodySize;
         final int last = Math.min(first + bodySize, history.size());
         int index = first;
         for (final Message message : history.subList(first, last)) {
             Main.courier.send(sender, "history|body", ( message.set > lastPlayed ? 1 : 0 )
                     , message.set, message.from, message.text
                     , RecordKeeper.duration(System.currentTimeMillis() - message.set)
                     , index);
             index++;
         }
 
        Main.courier.send(sender, "history|footer", pageCurrent, pageTotal);
         return true;
     }
 
     private static Integer parseInt(final String s, final Integer def) {
         try { return Integer.parseInt(s);
         } catch (final NumberFormatException e) { return def; }
     }
 
 }
