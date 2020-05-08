 package edgruberman.bukkit.messageformatter.commands;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.plugin.java.JavaPlugin;
 
 import edgruberman.bukkit.messageformatter.Main;
 import edgruberman.bukkit.messageformatter.commands.util.Action;
 import edgruberman.bukkit.messageformatter.commands.util.Context;
 import edgruberman.bukkit.messageformatter.commands.util.Parser;
 import edgruberman.bukkit.messagemanager.MessageLevel;
 
 public final class Reply extends Action {
 
     static Map<CommandSender, CommandSender> lastTellFrom = new HashMap<CommandSender, CommandSender>();
 
     public Reply(final JavaPlugin plugin) {
         super(plugin, "reply");
     }
 
     @Override
     public boolean perform(final Context context) {
         if (context.arguments.size() < 1) return false;
 
        final CommandSender recipient = Reply.lastTellFrom.get(context.sender);
         if (recipient == null || (recipient instanceof Player && !((Player) recipient).isOnline())) {
            Main.messageManager.respond(context.sender, "Unable to send reply; Last sender not found", MessageLevel.WARNING, false);
             return true;
         }
 
         String message = Parser.join(context.arguments.subList(0, context.arguments.size())).trim();
         message = Main.formatColors(context.sender, message);
         Tell.send(recipient, context.sender, message);
         return true;
     }
 
 }
