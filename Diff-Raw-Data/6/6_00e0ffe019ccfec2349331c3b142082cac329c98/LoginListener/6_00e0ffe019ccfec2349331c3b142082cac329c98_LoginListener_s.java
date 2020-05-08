 package net.robbytu.banjoserver.bungee.listeners;
 
import com.google.common.eventbus.Subscribe;
 import net.md_5.bungee.api.event.LoginEvent;
 import net.md_5.bungee.api.plugin.Listener;
 
 import net.robbytu.banjoserver.bungee.bans.BanLoginListener;
 
 public class LoginListener implements Listener {
    @Subscribe
     public static void login(LoginEvent event) {
         if(!event.isCancelled()) BanLoginListener.handle(event);
     }
 }
