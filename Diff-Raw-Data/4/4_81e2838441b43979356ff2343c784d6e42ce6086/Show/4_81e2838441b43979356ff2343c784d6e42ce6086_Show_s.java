 package edgruberman.bukkit.doorman.commands;
 
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 
 import edgruberman.bukkit.doorman.Doorman;
 import edgruberman.bukkit.doorman.Main;
 import edgruberman.bukkit.doorman.RecordKeeper;
 
 public final class Show implements CommandExecutor {
 
     private final Doorman doorman;
     private final RecordKeeper records;
 
     public Show(final Doorman doorman, final RecordKeeper records) {
         this.doorman = doorman;
         this.records = records;
     }
 
     // usage: /<command>[ <Index>]
     @Override
     public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args) {
         final int index = ( args.length == 0 ? 0 : Show.parseInt(args[0], 0));
         if (this.records.getHistory().size() <= index) {
             Main.courier.send(sender, "no-message", index);
             return true;
         }
 
        this.records.declare(sender, this.records.getHistory().get(index));
         if (index == 0) this.doorman.updateLast(sender.getName());
         return true;
     }
 
     private static Integer parseInt(final String s, final Integer def) {
         try { return Integer.parseInt(s);
         } catch (final NumberFormatException e) { return def; }
     }
 
 }
