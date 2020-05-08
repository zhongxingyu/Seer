 package com.minecarts.bouncer.listener;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.event.player.PlayerJoinEvent;
 import org.bukkit.event.player.PlayerLoginEvent.Result;
 import org.bukkit.event.player.PlayerQuitEvent;
 import org.bukkit.event.player.PlayerLoginEvent;
 import org.bukkit.event.player.PlayerPreLoginEvent;
 import org.bukkit.entity.Player;
 import com.minecarts.bouncer.Bouncer;
 import java.text.MessageFormat;
 
 public class PlayerListener extends org.bukkit.event.player.PlayerListener{
     private Bouncer plugin;
     public PlayerListener(Bouncer plugin){
         this.plugin = plugin;
     }
     
     @Override
     public void onPlayerPreLogin(PlayerPreLoginEvent e){
         String reason = plugin.dbHelper.isIdentiferBanned(e.getAddress().toString());
         if(reason != null){
             e.setResult(PlayerPreLoginEvent.Result.KICK_BANNED);
             e.setKickMessage("You have been banned: " + reason);
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
     @Override
     public void onPlayerJoin(PlayerJoinEvent e){
         String playerName = e.getPlayer().getName();
         String playerDisplayName = e.getPlayer().getDisplayName();
         String message = plugin.dbHelper.getJoinMessage(playerName);
         if(message != null){
             e.setJoinMessage(MessageFormat.format("{0}" + message,ChatColor.GRAY,playerDisplayName));
         } else if(plugin.dbHelper.getKey("joinCount", playerName) == null){
             e.setJoinMessage(ChatColor.WHITE + playerDisplayName + " has joined the server for the first time!");
         } else {
            e.setJoinMessage(ChatColor.GRAY + playerDisplayName + ChatColor.GRAY + " has joined the server.");
         }
     }
     @Override
     public void onPlayerQuit(PlayerQuitEvent e){
         String playerName = e.getPlayer().getName();
         String playerDisplayName = e.getPlayer().getDisplayName();
         String message = plugin.dbHelper.getQuitMessage(playerName);
         if(message != null){
             e.setQuitMessage(MessageFormat.format("{0}" + message,ChatColor.GRAY,playerDisplayName));
         } else {
            e.setQuitMessage(ChatColor.GRAY + playerDisplayName + ChatColor.GRAY + " has left the server.");
         }
     }
 }
