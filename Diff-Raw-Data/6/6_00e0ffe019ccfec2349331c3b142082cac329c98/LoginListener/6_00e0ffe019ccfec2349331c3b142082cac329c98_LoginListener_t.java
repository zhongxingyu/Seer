 package net.robbytu.banjoserver.bungee.listeners;
 
 import net.md_5.bungee.api.event.LoginEvent;
 import net.md_5.bungee.api.plugin.Listener;
 
import net.md_5.bungee.event.EventHandler;
 import net.robbytu.banjoserver.bungee.bans.BanLoginListener;
 
 public class LoginListener implements Listener {
    @EventHandler
     public static void login(LoginEvent event) {
         if(!event.isCancelled()) BanLoginListener.handle(event);
     }
 }
