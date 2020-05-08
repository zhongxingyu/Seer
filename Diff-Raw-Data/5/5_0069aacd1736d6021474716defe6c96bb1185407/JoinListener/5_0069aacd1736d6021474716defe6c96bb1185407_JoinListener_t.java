 /*
  * Copyright (C) 2013 Dabo Ross <www.daboross.net>
  */
 package net.daboross.bungeedev.uberchat;
 
 import net.md_5.bungee.api.ChatColor;
 import net.md_5.bungee.api.connection.ProxiedPlayer;
 import net.md_5.bungee.api.event.ServerConnectedEvent;
 import net.md_5.bungee.api.plugin.Listener;
 import net.md_5.bungee.event.EventHandler;
 
 /**
  *
  * @author daboross
  */
 public class JoinListener implements Listener {
 
     private final UberChatPlugin plugin;
 
     public JoinListener(UberChatPlugin plugin) {
         this.plugin = plugin;
     }
 
     @EventHandler
     public void onJoin(ServerConnectedEvent evt) {
         final ProxiedPlayer p = evt.getPlayer();
         String name = plugin.getDisplayNameDatabase().getDisplayName(p.getName());
         if (name == null) {
             name = ChatColor.BLUE + p.getName();
         }
        p.setDisplayName(name);
         plugin.getUtils().setDisplayName(evt.getServer(), name);
     }
 }
