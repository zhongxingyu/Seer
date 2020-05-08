 package com.imjake9.simplejail;
 
 import com.imjake9.simplejail.SimpleJail.JailMessage;
 import com.imjake9.simplejail.api.SimpleJailCommandListener;
 import com.imjake9.simplejail.api.SimpleJailCommandListener.Priority;
 import java.util.ArrayList;
 import java.util.EnumMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Scanner;
 import org.bukkit.Location;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 public class SimpleJailCommandHandler implements CommandExecutor {
     
     private final SimpleJail plugin;
     
     private Map<Priority, List<SimpleJailCommandListener>> commandListeners = new EnumMap<Priority, List<SimpleJailCommandListener>>(Priority.class);
     
     public SimpleJailCommandHandler(SimpleJail plugin) {
         this.plugin = plugin;
         
         this.registerCommands();
     }
     
     private void registerCommands() {
         plugin.getCommand("jail").setExecutor(this);
         plugin.getCommand("unjail").setExecutor(this);
         plugin.getCommand("setjail").setExecutor(this);
         plugin.getCommand("setunjail").setExecutor(this);
         plugin.getCommand("jailtime").setExecutor(this);
     }
     
     @Override
     public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
         
         if(commandLabel.equalsIgnoreCase("jail")) {
             
             if(!hasPermission(sender, "simplejail.jail")) {
                 JailMessage.LACKS_PERMISSIONS.send(sender, "simplejail.jail");
                 return true;
             }
             
             if (this.dispatchCommandToListeners(sender, commandLabel, args))
                 return true;
             
             if (args.length != 1 && args.length != 2) return false;
             
             // Catch JailExceptions
             try {
                 
                 // Jail/tempjail player:
                 if (args.length == 1) 
                     plugin.jailPlayer(args[0], sender.getName());
                 else {
                    int time = plugin.parseTimeString(args[1]);
                     plugin.jailPlayer(args[0], sender.getName(), time);
                 }
                 
             } catch (JailException ex) {
                 
                 // Send error message
                 sender.sendMessage(ex.getFormattedMessage());
                 return true;
                 
             }
             
             // Send success message:
             if (args.length == 1)
                 JailMessage.JAIL.send(sender, args[0]);
             else
                JailMessage.TEMPJAIL.send(sender, args[0], args[1]);
             
             return true;
             
         } else if(commandLabel.equalsIgnoreCase("unjail")) {
             
             if(!hasPermission(sender, "simplejail.unjail")) {
                 JailMessage.LACKS_PERMISSIONS.send(sender, "simplejail.unjail");
                 return true;
             }
             
             if (this.dispatchCommandToListeners(sender, commandLabel, args))
                 return true;
             
             if (args.length != 1) return false;
             
             // Catch JailExceptions
             try {
                 
                 // Unjail player
                 plugin.unjailPlayer(args[0]);
                 
             } catch (JailException ex) {
                 
                 // Send error message
                 sender.sendMessage(ex.getFormattedMessage());
                 return true;
                 
             }
             
             // Send success message
             JailMessage.UNJAIL.send(sender, args[0]);
             
             return true;
             
         } else if(commandLabel.equalsIgnoreCase("setjail")) {
             
             if(!hasPermission(sender, "simplejail.setjail")) {
                 JailMessage.LACKS_PERMISSIONS.send(sender, "simplejail.setjail");
                 return true;
             }
             
             if (args.length != 0 && args.length != 4) return false;
             
             // If not a player, only use explicit notation:
             if (!(sender instanceof Player) && args.length != 4) {
                 JailMessage.ONLY_PLAYERS.send(sender);
                 return true;
             }
             
             Location loc;
             
             // Use explicit vs current location:
             if (args.length == 0) {
                 
                 Player player = (Player)sender;
                 loc = player.getLocation();
                 
             } else {
                 
                 // Check if passed coordinates are correct:
                 if (!(new Scanner(args[0]).hasNextInt()) || !(new Scanner(args[1]).hasNextInt()) || !(new Scanner(args[2]).hasNextInt())) {
                     JailMessage.INVALID_COORDINATE.send(sender);
                     return true;
                 }
                 
                 // Set up Location value
                 loc = new Location(
                         plugin.getServer().getWorld(args[3]),
                         Integer.parseInt(args[0]),
                         Integer.parseInt(args[1]),
                         Integer.parseInt(args[2]));
                 
             }
             
             // Set the jail point
             plugin.setJail(loc);
             
             // Send success message
             JailMessage.JAIL_POINT_SET.send(sender);
             
             return true;
             
         } else if(commandLabel.equalsIgnoreCase("setunjail")) {
             
             if(!hasPermission(sender, "simplejail.setjail")) {
                 JailMessage.LACKS_PERMISSIONS.send(sender, "simplejail.setjail");
                 return true;
             }
             
             if (args.length != 0 && args.length != 4) return false;
             
             // If not a player, only use explicit notation:
             if (!(sender instanceof Player) && args.length != 4) {
                 JailMessage.ONLY_PLAYERS.send(sender);
                 return true;
             }
             
             Location loc;
             
             // Use explicit vs current location:
             if (args.length == 0) {
                 
                 Player player = (Player)sender;
                 loc = player.getLocation();
                 
             } else {
                 
                 // Check if passed coordinates are correct:
                 if (!(new Scanner(args[0]).hasNextInt()) || !(new Scanner(args[1]).hasNextInt()) || !(new Scanner(args[2]).hasNextInt())) {
                     JailMessage.INVALID_COORDINATE.send(sender);
                     return true;
                 }
                 
                 // Set up Location value
                 loc = new Location(
                         plugin.getServer().getWorld(args[3]),
                         Integer.parseInt(args[0]),
                         Integer.parseInt(args[1]),
                         Integer.parseInt(args[2]));
                 
             }
             
             // Set the jail point
             plugin.setUnjail(loc);
             
             // Send success message
             JailMessage.UNJAIL_POINT_SET.send(sender);
             
             return true;
             
         } else if(commandLabel.equalsIgnoreCase("jailtime")) {
             
             if(!hasPermission(sender, "simplejail.jailtime")) {
                 JailMessage.LACKS_PERMISSIONS.send(sender, "simplejail.jailtime");
                 return true;
             }
             
             if (args.length > 1) return false;
             
             // If not a player, a target must be explicit:
             if (!(sender instanceof Player) && args.length == 0) {
                 JailMessage.MUST_SPECIFY_TARGET.send(sender);
                 return true;
             }
             
             // Get the target
             Player player = (args.length == 0) ? (Player) sender : plugin.getServer().getPlayer(args[0]);
             
             // Validate target:
             if (player == null || !player.isOnline()) {
                 JailMessage.PLAYER_NOT_FOUND.send(sender, args[0]);
                 return true;
             }
             
             if (!plugin.playerIsTempJailed(player)) {
                 JailMessage.NOT_TEMPJAILED.send(sender, (args.length == 0) ? sender.getName() : args[0]);
                 return true;
             }
             
             // Send success message:
             int minutes = (int) ((plugin.getTempJailTime(player) - System.currentTimeMillis()) / 60000);
             JailMessage.JAIL_TIME.send(sender, plugin.prettifyMinutes(minutes));
  
             return true;
             
         } else {
             return false;
         }
         
     }
     
     /**
      * Checks any CommandSender for a permission node.
      * 
      * @param sender
      * @param permission
      * @return 
      */
     private boolean hasPermission(CommandSender sender, String permission) {
         if (sender instanceof Player)
             return sender.hasPermission(permission);
         else return true;
     }
     
     /**
      * Dispatches a particular command to all listeners, ordered by priority.
      * 
      * @param sender
      * @param command
      * @param args
      * @return 
      */
     private boolean dispatchCommandToListeners(CommandSender sender, String command, String args[]) {
         boolean handled = false;
         execute:
         for (Priority priority : commandListeners.keySet()) {
             if (priority.equals(Priority.MONITOR)) break;
             for (SimpleJailCommandListener listener : commandListeners.get(priority)) {
                 if (listener.handleJailCommand(sender, command, args)) {
                     handled = true;
                     break execute;
                 }
             }
         }
         if (commandListeners.containsKey(Priority.MONITOR))
             for (SimpleJailCommandListener listener : commandListeners.get(Priority.MONITOR)) {
                 listener.handleJailCommand(sender, command, args);
             }
         return handled;
     }
     
     /**
      * Registers a command listener.
      * 
      * @param listener
      * @param priority 
      */
     public void addListener(SimpleJailCommandListener listener, Priority priority) {
         if (!commandListeners.containsKey(priority))
             commandListeners.put(priority, new ArrayList<SimpleJailCommandListener>());
         commandListeners.get(priority).add(listener);
     }
     
     /**
      * Unregisters a command listener.
      * 
      * @param listener
      * @param priority 
      */
     public void removeListener(SimpleJailCommandListener listener, Priority priority) {
         if (!commandListeners.containsKey(priority)) return;
         if (!commandListeners.get(priority).contains(listener)) return;
         commandListeners.get(priority).remove(listener);
     }
     
 }
