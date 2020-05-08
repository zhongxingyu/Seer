 package com.github.lankylord;
 
 import java.util.logging.Logger;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.SignChangeEvent;
 
 public class SignListener implements Listener{
     ColouredSigns plugin;
     static final Logger logger = Logger.getLogger("Minecraft");
     public SignListener(ColouredSigns instance) {
         this.plugin = instance;
     }
     
     @EventHandler
     public void onSignChange(SignChangeEvent event) {
         Player player = event.getPlayer();
         if (!player.hasPermission("colouredsigns.colour")){
             return;
         }
         
         for (int i = 0; i <= 3; i++) {
            String line = event.getLine(i);
            line = line.replace("&", "§");
            line = line.replace("&", "§");
            event.setLine(i, line);
         }
     }
 }
