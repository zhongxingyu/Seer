 package com.precipicegames.zeryl.hidenseek;
 
 import java.util.HashSet;
 import java.util.Iterator;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.event.Event.Priority;
 import org.bukkit.event.Event;
 import org.bukkit.plugin.PluginDescriptionFile;
 import org.bukkit.plugin.java.JavaPlugin;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.ChatColor;
 import org.bukkit.Material;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.ItemStack;
 
 /**
  *
  * @author Zeryl
  */
 public class HideNSeek extends JavaPlugin {
     public boolean state = false;
     
     private final HideNSeekEntityListener entityListener = new HideNSeekEntityListener(this);
     private final HideNSeekPlayerListener playerListener = new HideNSeekPlayerListener(this);
    
     private HashSet<Player> players;
     private HashSet<Player> ready;
     
     public void onEnable() {
         PluginManager pm = getServer().getPluginManager();
         PluginDescriptionFile pdf = this.getDescription();
         pm.registerEvent(Event.Type.ENTITY_DAMAGE, entityListener, Priority.Monitor, this);
         pm.registerEvent(Event.Type.PLAYER_QUIT, playerListener, Priority.Monitor, this);
         System.out.println(pdf.getName() + " is now enabled.");
         players = new HashSet<Player>();
         ready = new HashSet<Player>();
     }
     
     public void onDisable() {
         PluginDescriptionFile pdf = this.getDescription();
         System.out.println(pdf.getName() + " is now disabled.");
     }
     
     @Override
     public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
         if(cmd.getName().equalsIgnoreCase("hns")) {
             if(args.length > 0) {
                 if(args[0].equalsIgnoreCase("toggle") && sender.hasPermission("precipice.hidenseek.toggle")) {
                     if(sender instanceof Player) {
                         if(this.players.contains((Player) sender)) {
                             this.players.remove((Player) sender);
                             sender.sendMessage("Hide n Seek is now " + ChatColor.YELLOW + "Off");
                             this.sendToPlayers(sender.getName() + " is no longer playing Hide n Seek!", ChatColor.RED);
                         }
                         else {
                             this.players.add((Player) sender);
                             sender.sendMessage("Hide n Seek is now " + ChatColor.YELLOW + "On");
                             this.sendToPlayers(sender.getName() + " is now playing Hide n Seek!", ChatColor.RED);
                         }
                     }
                 }
                 else if(args[0].equalsIgnoreCase("who")) {
                     sender.sendMessage(this.getPlayers());
                 }
                 else if(args[0].equalsIgnoreCase("help")) {
                     sender.sendMessage(ChatColor.YELLOW + "/hns" + ChatColor.WHITE + ": Shows your current Hide n Seek status.");
                     sender.sendMessage(ChatColor.YELLOW + "/hns toggle" + ChatColor.WHITE + ": Toggles whether or not you're playing Hide n Seek.");
                     sender.sendMessage(ChatColor.YELLOW + "/hns who" + ChatColor.WHITE + ": Shows who is playing Hide n Seek, and their status.");
                     sender.sendMessage(ChatColor.YELLOW + "/hns ready" + ChatColor.WHITE + ": Marks you as ready to play.");
                 }
                 else if(args[0].equalsIgnoreCase("ready")) {
                     if(this.ready.contains((Player) sender)) {
                         this.ready.remove((Player) sender);
                         sender.sendMessage(ChatColor.RED + "You are now marked as not ready to play!");
                         this.sendToPlayers(sender.getName() + " is no longer ready to play!", ChatColor.RED);
                     } else {
                         this.ready.add((Player) sender);
                         this.sendToPlayers(sender.getName() + " is ready to play!", ChatColor.RED);
                     }
                     
                     if(this.ready.size() == this.players.size()) {
                         this.sendToPlayers("All players are ready, enjoy your game!", ChatColor.GREEN);
                         this.ready.clear();
                     }
                 }
                 else if(args[0].equalsIgnoreCase("reload") && sender.isOp()) {
                     //nothing for now.
                 }
             }
             else {
                 if(this.players.contains((Player) sender))
                     sender.sendMessage("You are currently participating in Hide n Seek!");
                 else
                     sender.sendMessage("You are currently " + ChatColor.RED + "not" + ChatColor.WHITE + " participating in Hide n Seek!");
             }
         }
         return true;
     }
     
     public boolean isPlaying(Player player) {
         if(this.players.contains(player))
             return true;
         else
             return false;
     }
     
     public boolean isRunning() {
         if(this.players.size() > 0)
             return true;
         else
             return false;
     }
     
     public void sendToPlayers(String message, ChatColor color) {
         Iterator it = this.players.iterator();
         while(it.hasNext()) {
             Player player = (Player) it.next();
             player.sendMessage(color + message);
         }
     }
     
     public String getPlayers() {
         Iterator it = this.players.iterator();
         String string = "";
         String role = "";
 
         while(it.hasNext()) {
             String name = "";
             Player player = (Player) it.next();
             ItemStack helm = player.getInventory().getHelmet();
             if(helm.getType() == Material.DIAMOND_HELMET)
                 role = "(Seeker)";
             else if(helm.getType() == Material.GOLD_HELMET)
                 role = "(Observer)";
             else
                 role = "(Hider)";
             if(this.ready.contains(player))
                 name = ChatColor.GREEN + player.getDisplayName();
             else
                 name = player.getDisplayName();
             
             if(string.equalsIgnoreCase(""))
                 string = ChatColor.RED + role + ChatColor.WHITE + " " + name;
             else
                 string = string + ", " + ChatColor.RED + role + ChatColor.WHITE + " " + name;
         }
         
         if(this.players.isEmpty()) {
             string = ChatColor.RED + "Noone is playing Hide n Seek!";
         }
         return string;
     }
     
     public void removePlayer(Player player) {
         if(this.players.contains(player))
             this.players.remove(player);
     }
 }
