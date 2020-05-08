 package edgruberman.bukkit.kitteh.commands;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 
 import org.bukkit.Bukkit;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 import edgruberman.bukkit.kitteh.Kit;
 import edgruberman.bukkit.kitteh.Main;
 import edgruberman.bukkit.kitteh.Manager;
 import edgruberman.bukkit.kitteh.messaging.Message;
 
 public final class Show extends Executor {
 
     private static final int PAGE_SIZE = 10;
 
     private final Manager manager;
 
     public Show(final Manager manager) {
         this.manager = manager;
     }
 
     // usage: /<command>[ <Page>[ <Player>]]
     @Override
     protected boolean execute(final CommandSender sender, final Command command, final String label, final List<String> args) {
         if (args.size() < 2 && !(sender instanceof Player)) {
             Main.courier.send(sender, "messages.requiresPlayer", label);
             return true;
         }
 
         if (args.size() >= 2 && !sender.hasPermission("kitteh.show.all")) {
             Main.courier.send(sender, "messages.showDenied", args.get(1));
             return true;
         }
 
         final String target = (args.size() >= 2 ? Bukkit.getOfflinePlayer(args.get(1)).getName() : sender.getName());
 
         final Map<Kit, Integer> balance = this.manager.balance(target);
         if (balance.size() == 0) {
             Main.courier.send(sender, "messages.showNone", target);
             return true;
         }
 
         final List<Message> header = Main.courier.draft("messages.show.header", target);
         final int footerSize = Main.courier.draft("messages.show.footer").size();
         final int lineCount = Show.PAGE_SIZE - header.size() - footerSize;
 
        final int pageTotal = (balance.size() / lineCount) + 1;
         final int pageCurrent = ( args.size() >= 1 ? Show.parseInt(args.get(0), 1) : 1 );
         if (pageCurrent <= 0 || pageCurrent > pageTotal) {
             Main.courier.send(sender, "messages.unknownPage", pageCurrent);
             return false;
         }
 
         final int first = (pageCurrent - 1) * lineCount;
         final int last = Math.min(first + lineCount, balance.size()) - 1;
 
         Main.courier.send(sender, "messages.show.header", target);
 
         final List<Map.Entry<Kit, Integer>> available = new ArrayList<Map.Entry<Kit, Integer>>(balance.entrySet());
         for (int i = first; i <= last; i++)
             Main.courier.send(sender, "messages.show.line", available.get(i).getKey().getName(), available.get(i).getValue(), available.get(i).getKey().describe().toString());
 
         Main.courier.send(sender, "messages.show.footer", pageCurrent, pageTotal, balance.entrySet().size());
         return true;
     }
 
     private static Integer parseInt(final String s, final Integer def) {
         try { return Integer.parseInt(s);
         } catch (final NumberFormatException e) { return def; }
     }
 
 }
