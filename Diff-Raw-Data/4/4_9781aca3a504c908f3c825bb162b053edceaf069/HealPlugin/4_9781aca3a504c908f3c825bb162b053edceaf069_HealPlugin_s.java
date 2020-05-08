 package com.bukkthat.healplugin;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.plugin.java.JavaPlugin;
 
 /**
  * Main HealPlugin class that handles all of the
  * plugin functionality.  It extends JavaPlugin
  * because it's a Bukkit plugin, but it does not
  * implement Listener because it does not have
  * any EventHandlers within it.
  * 
  * @author gomeow
  */
 public class HealPlugin extends JavaPlugin {
 
     /* This is the method invoked when a command that we've registered in our plugin.yml
      * is typed by a Player, or the console.
      * <p>
      * The first parameter, the CommandSender, is the person who typed the command.
      * This person could be the console, or it could be a Player.  We should always
      * make sure that the sender is a Player before we cast to a Player.
      * <p>
      * The second parameter, the Command, is the command that they used.  The Command
      * can be used to determine the command that they typed by using Command#getName().
      * In our case, since our Command variable is called cmd, our method invocation would
      * look like cmd.getName().  You can compare the name using String's #equalIgnoreCase
      * to see if they typed a specific command.  We don't do that in this example because
      * we only have one command registered, so there's no need to check the name.
      * <p>
     * The third parameter, the String that we call the label, is the wrod that the user
     * literally typed to use our command.  If the server is using bukkit aliasing it is
      * possible for this word to not be our command, so it should only be used when giving
      * a response to the user.  In other words, instead of telling them to type "/heal", we
      * should tell them to type "/"+label as to respect whatever bukkit.yml aliases that the
      * server owner has set up.
      * <p>
      * The fourth parameter, the String array, is any words that they typed after the command,
      * separated by spaces.  That means if they typed "/heal a lot of people", the String array
      * would contain "a", "lot", "of", "people".  To combine the words into a single String we
      * can use org.apache.commons.lang.StringUtils.join with the appropriate arguments.
      * <p>
      * The return value determines whether or not the usage message that we put in our plugin.yml
      * is displayed to the user.  If we return true then the message isn't shown.  If we
      * return false then the usage message is sent to the CommandSender.
      */
     public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
         // Check to see if they only typed "/heal", with nothing after "heal".
         if(args.length == 0) {
             // The CommandSender could be the console instead of a Player.
             // We need to make sure they're a Player if we're going to heal them.
             if(sender instanceof Player) {
                 // We can now safely cast the CommandSender to a Player
                 Player player = (Player) sender;
                 // Permissions check, does the player have the heal.self node?
                 if(player.hasPermission("heal.self")) {
                     // Set their health back to the maximum
                     player.setHealth(player.getMaxHealth());
                     // Feed them to 20/20
                     player.setFoodLevel(20);
                     // Send them a chat message
                     player.sendMessage(ChatColor.GREEN + "You have been fed and healed!");
                 } else {
                     player.sendMessage(ChatColor.RED + "You do not have permission to do that!");
                 }
             } else {
                 // If they're not a Player(the console, in other words), we should tell them that they can't
                 // use the command.
                 sender.sendMessage(ChatColor.RED + "Only a player has health!");
             }
         // Check if they typed a word after "/heal".
         } else if(args.length == 1) {
             // Permissions check, does the player have the heal.others node?
             if(sender.hasPermission("heal.others")) {
                 // Get the player using the username supplied in the first argument
                 Player target = Bukkit.getPlayer(args[0]);
                 // Make sure the player is online.
                 // If they're offline, Bukkit.getPlayer will return null.
                 if(target == null) {
                     sender.sendMessage(ChatColor.RED + "That player is not online!");
                 } else {
                     // Set their health back to the maximum
                     target.setHealth(target.getMaxHealth());
                     // Feed them to 20/20
                     target.setFoodLevel(20);
                     sender.sendMessage(ChatColor.GREEN + target.getName() + " was healed and fed!");
                     target.sendMessage(ChatColor.GREEN + "You were healed and fed!");
                 }
             } else {
                 // If they don't have the required permission, tell them.
                 sender.sendMessage(ChatColor.RED + "You do not have permission to do that!");
             }
         } else {
             // Return false, this will output the usage message from the plugin.yml file
             return false;
         }
         // By default we should always return true.  If we return false the usage message from
         // our plugin.yml is sent to the CommandSender.
         return true;
     }
 
 }
