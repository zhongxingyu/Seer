 /* 
  * Copyright (C) 2013 Dr Daniel R. Naylor
  * 
  * Permission is hereby granted, free of charge, to any person obtaining a copy of this software 
  * and associated documentation files (the "Software"), to deal in the Software without restriction, 
  * including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, 
  * and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, 
  * subject to the following conditions:
  * 
  * The above copyright notice and this permission notice shall be included in all copies or substantial 
  * portions of the Software.
  * 
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT 
  * LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. 
  * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, 
  * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE 
  * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
  * 
  **/
 package uk.co.drnaylor.mcmmopartyadmin;
 
 import com.gmail.nossr50.api.PartyAPI;
 import com.gmail.nossr50.events.chat.McMMOPartyChatEvent;
 import org.bukkit.ChatColor;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 
 public class PartyChatListener implements Listener {
     
     @EventHandler
     public void PartyChat(McMMOPartyChatEvent event)
     {
         for (Player online : PartyAdmin.plugin.getServer().getOnlinePlayers()) {
           if (PartySpy.isSpy(online)) {
              if (!PartyAPI.inParty(online) || !(PartyAPI.getPartyName(online).equals(event.getParty()))) {
                 String p2 = ChatColor.GRAY + "[Spy: " + event.getParty() + "] " + ChatColor.GREEN + " (" + ChatColor.WHITE + event.getSender() + ChatColor.GREEN + ") ";
                 online.sendMessage(p2 + event.getMessage());
               }
           }
         }
     }
 }
