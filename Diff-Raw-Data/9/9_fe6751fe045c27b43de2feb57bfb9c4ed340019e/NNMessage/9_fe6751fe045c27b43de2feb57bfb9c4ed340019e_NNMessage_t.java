 package com.bukkit.N4th4.NuxNoobs;
 
 import java.util.TimerTask;
 import java.util.ArrayList;
 
 import org.bukkit.entity.Player;
 
 public class NNMessage extends TimerTask {
     private final NuxNoobs plugin;
 
     public NNMessage(NuxNoobs parent) {
         plugin = parent;
     }
 
     @Override
     public void run() {
         Player[] playersList = plugin.getServer().getOnlinePlayers();
         for (int i = 0; i < playersList.length; i++) {
            if (plugin.permissions.getPrimaryGroup(playersList[i].getWorld().getName(), playersList[i].getName()).equals(plugin.group)) {
                 ArrayList<String> al = plugin.noobMessage;
                 for (int j = 0; j < al.size(); j++) {
                     playersList[i].sendMessage(al.get(j));
                 }
             }
         }
     }
 }
