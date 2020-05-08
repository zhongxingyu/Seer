 package com.carlgo11.simpleautomessage;
 
 import com.carlgo11.simpleautomessage.language.Lang;
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.event.Listener;
 
 public class Broadcast implements Listener {
 
     Main plugin;
 
     public Broadcast(Main plug) {
         super();
         this.plugin = plug;
         broadcast();
     }
     int errormaxmsgs = 5;
     int errormsgs = 0;
 
         public void broadcast() {
 
         final long d = (long) (plugin.time);
         Bukkit.getServer().getScheduler().runTaskTimerAsynchronously(plugin, new Runnable() {
             public void run() {
                 int minP = plugin.getConfig().getInt("min-players");
                 int onlineP = Bukkit.getServer().getOnlinePlayers().length;
                int realonlineP = onlineP;
                 onlineP++;
                         
                 if (onlineP > minP) {
                     if (plugin.getConfig().getBoolean("debug") == true) {
                         plugin.debugmsg = "tick: " + plugin.tick;
                         plugin.onDebug();
                         plugin.debugmsg = "time: " + d;
                         plugin.onDebug();
                     }
                     String senderToSend = plugin.getConfig().getString("sender");
                     String senderToMC = ChatColor.translateAlternateColorCodes('&', senderToSend);
                     String prefixToSend = plugin.getConfig().getString("prefix");
                     String prefixToMC = ChatColor.translateAlternateColorCodes('&', prefixToSend);
                     String suffixToSend = plugin.getConfig().getString("suffix");
                     String suffixToMC = ChatColor.translateAlternateColorCodes('&', suffixToSend);
                     if (plugin.getConfig().contains("msg" + plugin.tick)) {
                         String messageToSend = plugin.getConfig().getString("msg" + plugin.tick);
                         String msgToMC = ChatColor.translateAlternateColorCodes('&', messageToSend);
                         plugin.getServer().broadcast(prefixToMC + senderToMC + suffixToMC + " " + ChatColor.RESET + msgToMC, "SimpleAutoMessage.seemsg");
                         plugin.tick++;
                     } else {
                         plugin.debugmsg = "no msg" + plugin.tick + " set in the config. calling msg1 instead.";
                         plugin.onDebug();
                         if (plugin.getConfig().contains("msg1")) {
                             String messageToSend = plugin.getConfig().getString("msg1");
                             String msgToMC = ChatColor.translateAlternateColorCodes('&', messageToSend);
                             plugin.getServer().broadcast(prefixToMC + senderToMC + suffixToMC + " " + ChatColor.RESET + msgToMC, "SimpleAutoMessage.seemsg");
                             plugin.tick = 2;
                         } else {
                             if (errormsgs != errormaxmsgs) {
                                 System.out.println(ChatColor.stripColor(prefixToMC) + ChatColor.stripColor(senderToMC) + ChatColor.stripColor(suffixToMC) + " " + Lang.NO_MSG1);
                                 errormsgs++;
                             }
 
                         }
                     }
                }else{
                    plugin.debugmsg = "Error: minP:"+minP+" realOnlineP: "+realonlineP;
                    plugin.onDebug();
                 }
             }
         }, d, d);
     }
     /* What does this do? 
      * 
      * This class sends the broadcast messages.
      * 
      */
 }
