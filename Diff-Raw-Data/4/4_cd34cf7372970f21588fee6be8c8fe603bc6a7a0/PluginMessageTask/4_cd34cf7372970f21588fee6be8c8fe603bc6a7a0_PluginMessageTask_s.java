 package com.minecraftdimensions.bungeesuiteteleports.tasks;
 
 import com.minecraftdimensions.bungeesuiteteleports.BungeeSuiteTeleports;
 import org.bukkit.Bukkit;
 import org.bukkit.entity.Player;
 import org.bukkit.scheduler.BukkitRunnable;
 
 import java.io.ByteArrayOutputStream;
 
 public class PluginMessageTask extends BukkitRunnable {
 
     private final ByteArrayOutputStream bytes;
 
     public PluginMessageTask( ByteArrayOutputStream bytes ) {
         this.bytes = bytes;
     }
 
     public PluginMessageTask( ByteArrayOutputStream b, boolean empty ) {
         this.bytes = b;
     }
 
     @SuppressWarnings("unchecked")
     public void run() {
         Player p = Bukkit.getOnlinePlayers()[0];
         if ( p == null ) {
             return;
         }
         p.sendPluginMessage( BungeeSuiteTeleports.instance, BungeeSuiteTeleports.OUTGOING_PLUGIN_CHANNEL, bytes.toByteArray() );
     }
 
 
 }
