 package com.minecarts.bouncer.listener;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.event.player.PlayerJoinEvent;
 import org.bukkit.event.player.PlayerLoginEvent.Result;
 import org.bukkit.event.player.PlayerQuitEvent;
 import org.bukkit.event.player.PlayerLoginEvent;
 import org.bukkit.event.player.PlayerPreLoginEvent;
 import org.bukkit.event.player.PlayerKickEvent;
 import org.bukkit.entity.Player;
 import com.minecarts.bouncer.Bouncer;
 import java.text.MessageFormat;
 
 import com.minecarts.barrenschat.cache.CacheIgnore;
 
 public class PlayerListener extends org.bukkit.event.player.PlayerListener{
     private Bouncer plugin;
     private java.util.HashMap<String, Integer> playerFlagged = new java.util.HashMap<String, Integer>();
     
     public PlayerListener(Bouncer plugin){
         this.plugin = plugin;
     }
 //Bans
     @Override
     public void onPlayerPreLogin(PlayerPreLoginEvent e){
         String reason = plugin.dbHelper.isIdentiferBanned(e.getAddress().toString());
         if(reason != null){
             e.setResult(PlayerPreLoginEvent.Result.KICK_BANNED);
             e.setKickMessage(reason);
         }
     }
     @Override
     public void onPlayerLogin(PlayerLoginEvent e){
         String reason = plugin.dbHelper.isIdentiferBanned(e.getPlayer().getName());
         if(reason != null){
             e.setResult(PlayerLoginEvent.Result.KICK_BANNED);
             e.setKickMessage(reason);
             return;
         }
         if(e.getResult() == Result.KICK_FULL){
             if(plugin.objectData.shared.get(e.getPlayer(), "subscriptionType") != null){
                 Player[] online = Bukkit.getServer().getOnlinePlayers();
                 online[online.length - 1].kickPlayer(plugin.fullMessage); //Kick the most recent connecting player
                 e.setResult(Result.ALLOWED); //And let the subscriber connect
                 return;
             }
             e.setKickMessage(plugin.fullMessage);
         }
     }
 //Login messages
     @Override
     public void onPlayerJoin(PlayerJoinEvent e){
         String playerName = e.getPlayer().getName();
         String playerDisplayName = e.getPlayer().getDisplayName();
         String format = plugin.dbHelper.getJoinMessage(playerName);
         String displayMessage = null;
 
         //Always clear the message, because we send it to all players ourselves for ignore list support
         e.setJoinMessage(null);
         //Determine the format of the message
        if(format != null && !format.equals("")){
             displayMessage = MessageFormat.format("{0}" + format,ChatColor.GRAY,playerDisplayName);
         } else if(plugin.dbHelper.getKey("Hints_FirstJoin", playerName) == null){
             displayMessage = ChatColor.WHITE + playerDisplayName + " has joined the server for the first time!";
         } else {
             displayMessage = ChatColor.GRAY + playerDisplayName + ChatColor.GRAY + " logged in.";
         }
 
         //Check to see if it's a rejoin 
         if(this.playerFlagged.containsKey(playerName)){
             Integer taskId = this.playerFlagged.remove(playerName);
             //e.setJoinMessage(null);  //They rejoined, no join message
             displayMessage = null;
             if(taskId != null){
                 Bukkit.getServer().getScheduler().cancelTask(taskId); //Cancel leave message from showing
             }
         }
 
         //If it's not blank, it's a valid message and lets send it!
         if(displayMessage != null && !displayMessage.equals("")){
             for(Player player : Bukkit.getServer().getOnlinePlayers()){
                 if(CacheIgnore.isIgnoring(player, e.getPlayer())) continue;
                 player.sendMessage(displayMessage); 
             }
         }
     }
     @Override
     public void onPlayerQuit(PlayerQuitEvent e){
         String playerName = e.getPlayer().getName();
         String playerDisplayName = e.getPlayer().getDisplayName();
         String format = plugin.dbHelper.getQuitMessage(playerName);
         String displayMessage = "";
         
       //Always clear the message, because we send it to all players ourselves for ignore list support
         e.setQuitMessage(null);
         
         //Determine the format of the message
        if(format != null && !format.equals("")){
             displayMessage = MessageFormat.format("{0}" + format,ChatColor.GRAY,playerDisplayName);
         } else {
             displayMessage = ChatColor.GRAY + playerDisplayName + ChatColor.GRAY + " logged out.";
         }
 
         if(displayMessage != null && !displayMessage.equals("")){
             this.delayedOptionalMessage(displayMessage, e.getPlayer());
         }
     }
 
     private void delayedOptionalMessage(String message, Player player){
         Runnable delayedSend = new DelayedSend(message, player, plugin);
         int taskId = plugin.getServer().getScheduler().scheduleAsyncDelayedTask(plugin,delayedSend,20 * 12); //12 seconds later
         this.playerFlagged.put(player.getName(), taskId);
     }
     
     private class DelayedSend implements Runnable{
         private String message;
         private Player playerLeft;
         private Bouncer plugin;
 
         public DelayedSend(String message, Player playerLeft, Bouncer plugin){
             this.message = message;
             this.playerLeft = playerLeft;
             this.plugin = plugin;
         }
         
         public void run(){
             Integer taskId = plugin.playerListener.playerFlagged.remove(playerLeft.getName());
             for(Player player : Bukkit.getServer().getOnlinePlayers()){
                 if(CacheIgnore.isIgnoring(player, playerLeft)) continue;
                 player.sendMessage(message); 
             }
         }
     }
     
 }
