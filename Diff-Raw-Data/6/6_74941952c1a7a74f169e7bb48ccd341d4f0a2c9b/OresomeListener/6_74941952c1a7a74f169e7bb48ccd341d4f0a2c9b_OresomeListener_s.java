 package com.oresomecraft.oresomenotes;
 
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.player.PlayerCommandPreprocessEvent;
 import org.bukkit.event.player.PlayerJoinEvent;
 
 import java.util.ArrayList;
 
 public class OresomeListener implements Listener {
 
     OresomeNotes plugin;
 
     public OresomeListener(OresomeNotes pl) {
         plugin = pl;
         pl.getServer().getPluginManager().registerEvents(this, pl);
     }
 
     @EventHandler
     public void onPlayerJoin(PlayerJoinEvent event) {
         final Player p = event.getPlayer();
         Bukkit.getScheduler().runTaskLater(OresomeNotes.getInstance(), new Runnable() {
             public void run() {
                 if (Utility.hasNotes(p)) {
                     Utility.sendStaffNotes(p);
                 }
             }
         }, 2 * 20);
     }
 
     @EventHandler
     public void automaticNoteListener(PlayerCommandPreprocessEvent event) {
         if (!Utility.hasAutomatic(event.getPlayer().getName().toLowerCase())) return;
         if (event.getMessage().startsWith("/mute") || event.getMessage().startsWith("/tempmute") ||
                 (event.getMessage().startsWith("/ban") && !event.getMessage().startsWith("/banlist")) ||
                 event.getMessage().startsWith("/tempban") || event.getMessage().startsWith("/kick")) {
            event.getPlayer().sendMessage(ChatColor.RED + "A note was automatically added to the player!");
            Utility.addAnonymousNote(argToTarget(event.getMessage()), argBuilderReason(event.getMessage()));
         }
     }
 
     private String argToTarget(String arg) {
         ArrayList<String> string = new ArrayList<String>();
         for (String s : arg.split(" ")) string.add(s);
         return string.get(1);
     }
 
     private String argBuilderReason(String args) {
         ArrayList<String> string = new ArrayList<String>();
         for (String s : args.split(" ")) string.add(s);
         String reason = "";
         reason = "[BANS] " + string.get(0).toUpperCase().replace("/", "") + " -> " + string.get(1) + ":";
         for (int i = 0; i < string.size(); i++) {
             if (i != 0 && i != 1) {
                 reason = reason + " " + string.get(i);
             }
         }
         return reason;
     }
 }
