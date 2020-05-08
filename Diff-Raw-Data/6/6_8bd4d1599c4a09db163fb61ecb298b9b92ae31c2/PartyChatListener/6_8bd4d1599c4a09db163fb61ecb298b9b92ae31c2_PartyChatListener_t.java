 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package uk.co.drnaylor.mcmmopartyadmin;
 
 import com.gmail.nossr50.api.PartyAPI;
 import com.gmail.nossr50.events.chat.McMMOPartyChatEvent;
 import com.gmail.nossr50.mcMMO;
 import org.bukkit.ChatColor;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 
 /**
  *
  * @author dualspiral
  */
 public class PartyChatListener implements Listener {
     
     @EventHandler
     public void PartyChat(McMMOPartyChatEvent event)
     {
         for (Player online : PartyAdmin.plugin.getServer().getOnlinePlayers()) {
          if (online.hasPermission("mcmmopartyadmin.spy")) {
              if (!PartyAPI.inParty(online) || (PartyAPI.getPartyName(online) != event.getParty())) {
                 String p2 = ChatColor.GRAY + "[Spy: " + event.getParty() + "] " + ChatColor.GREEN + " (" + ChatColor.WHITE + event.getSender() + ChatColor.GREEN + ") ";
                 online.sendMessage(p2 + event.getMessage());
              }
           }
         }
         
         
     }
     
 }
