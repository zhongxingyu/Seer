 package name.richardson.james.hearthstone.general;
 
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 
 import name.richardson.james.bukkit.utilities.command.PluginCommand;
 import name.richardson.james.bukkit.utilities.plugin.SimplePlugin;
 
 public class HomeCommand implements CommandExecutor {
 
   private final PluginCommand teleport; 
   private final PluginCommand set;
   private final SimplePlugin plugin; 
   
   public HomeCommand(SimplePlugin plugin, PluginCommand teleport, PluginCommand set) {
     this.teleport = teleport;
     this.set = set;
     this.plugin = plugin;
   }
   
   public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
     
     if (args.length == 0) {
       this.teleport.onCommand(sender, command, label, args);
    } else if (args[0].equalsIgnoreCase("setcommand-name")) {
       String[] arguments;
       arguments = prepareArguments(args, this.plugin.getMessage("setcommand-name"));
       this.set.onCommand(sender, command, label, arguments);
    }
     
     return true;
     
   }
   
   private String[] prepareArguments(String[] args, String name) {
     if (args[0].equalsIgnoreCase(name)) {
       String[] arguments = new String[args.length - 1];
       System.arraycopy(args, 1, arguments, 0, args.length - 1);
       return arguments;
     }
     return args;
   }
   
 }
