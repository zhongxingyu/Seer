 package me.KeybordPiano459.kChat;
 
 import org.bukkit.ChatColor;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.player.AsyncPlayerChatEvent;
 
 public class ChatColors implements Listener {
     static kChat plugin;
     public ChatColors(kChat plugin) {
         ChatColors.plugin = plugin;
     }
     
     @EventHandler
     public void onChat(AsyncPlayerChatEvent event) {
         Player player = event.getPlayer();
         String msg = event.getMessage();
         if (plugin.colorallowed) {
             event.setMessage(ChatColor.translateAlternateColorCodes('&', msg));
        } // Add color symbols to chat messages
         if (player.isOp() && !plugin.opcolor.equals("none")) {
            event.setFormat(ChatColor.translateAlternateColorCodes('&', "<" + player.getName() + "> " + msg));
         }
     }
 }
