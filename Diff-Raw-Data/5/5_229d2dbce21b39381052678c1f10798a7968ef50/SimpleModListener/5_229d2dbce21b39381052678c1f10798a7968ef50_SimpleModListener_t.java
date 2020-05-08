 package com.evosysdev.bukkit.taylorjb.simplemod;
 
 import org.bukkit.ChatColor;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
 import org.bukkit.event.player.PlayerLoginEvent;
 import org.bukkit.event.player.PlayerLoginEvent.Result;
 
 /**
  * Listens to all events relevant to SimpleMod
  * 
  * @author taylorjb
  *
  */
 public class SimpleModListener implements Listener
 {
     private SimpleMod plugin; // instance of our plugin
     
     /**
      * Initialize our listener
      * 
      * @param plugin
      *            instance of our plugin
      */
     public SimpleModListener(SimpleMod plugin)
     {
         this.plugin = plugin;
 
         // register events
         plugin.getServer().getPluginManager().registerEvents(this, plugin);
     }
     
     /**
      * Listen to player logins/login event
      * Check if user is banned
      * 
      * @param login
      *            login event
      */
     @EventHandler(priority = EventPriority.LOW)
     public void onPlayerLogin(PlayerLoginEvent login)
     {
         // if result is allowed, kick message is the IP we want
         if (login.getResult().equals(Result.ALLOWED))
         {
             if (this.plugin.getHandler().isBanned(login.getPlayer().getName(), login.getKickMessage()))
             {
                 login.disallow(Result.KICK_BANNED, "You are banned!");
                 plugin.getLogger().info("Player " + login.getPlayer().getName() + " denied entry: banned");
             }
         }
     }
     
     /**
      * Listen to chat/chat event
      * 
      * @param chat
      *            chat event
      */
     @EventHandler(priority = EventPriority.LOW)
    public void onPlayerChat(AsyncPlayerChatEvent chat)
     {
         if (plugin.getHandler().isMuted(chat.getPlayer().getName()))
         {
             chat.getPlayer().sendMessage(ChatColor.RED + "You are muted!");
             chat.setCancelled(true);
         }
     }
 }
