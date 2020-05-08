 /*
 This file is part of Salesmania.
 
     Salesmania is free software: you can redistribute it and/or modify
     it under the terms of the GNU General Public License as published by
     the Free Software Foundation, either version 3 of the License, or
     (at your option) any later version.
 
     Salesmania is distributed in the hope that it will be useful,
     but WITHOUT ANY WARRANTY; without even the implied warranty of
     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
     GNU General Public License for more details.
 
     You should have received a copy of the GNU General Public License
     along with Salesmania.  If not, see <http://www.gnu.org/licenses/>.
 */
 
 package net.invisioncraft.plugins.salesmania.channels.adapters;
 
 import com.dthielke.herochat.Channel;
 import com.dthielke.herochat.ChannelManager;
 import com.dthielke.herochat.Chatter;
 import com.dthielke.herochat.Herochat;
 import net.invisioncraft.plugins.salesmania.Salesmania;
 import net.invisioncraft.plugins.salesmania.configuration.AuctionIgnoreList;
 import net.invisioncraft.plugins.salesmania.worldgroups.WorldGroup;
 import org.bukkit.entity.Player;
 
 import java.util.ArrayList;
 
 public class HeroChatAdapter implements ChannelAdapter {
     private Salesmania plugin;
     private AuctionIgnoreList auctionIgnoreList;
     ChannelManager channelManager;
 
     public HeroChatAdapter(Salesmania plugin) {
         this.plugin = plugin;
         auctionIgnoreList = plugin.getAuctionIgnoreList();
         channelManager = Herochat.getChannelManager();
     }
 
     @Override
     public void broadcast(String channelName, String[] message) {
         if(channelManager.hasChannel(channelName)) {
             Channel channel = channelManager.getChannel(channelName);
            for(String msg : message) {
                channel.announce(msg);
             }
         }
     }
 
     @Override
     public void broadcast(WorldGroup worldGroup, String[] message) {
         for(String channelName : worldGroup.getChannels()) {
             broadcast(channelName, message);
         }
     }
 
     @Override
     public void broadcast(WorldGroup worldGroup, String message) {
         broadcast(worldGroup, new String[]{message});
     }
 
     @Override
     public void broadcast(WorldGroup worldGroup, String[] message, ArrayList<Player> players) {
         for(String channelName : worldGroup.getChannels()) {
             if(channelManager.hasChannel(channelName)) {
                 Channel channel = channelManager.getChannel(channelName);
                 for(Chatter chatter : channel.getMembers()) {
                    if(players.contains(chatter.getPlayer())) {
                         chatter.getPlayer().sendMessage(message);
                     }
                 }
             }
         }
     }
 
     @Override
     public void broadcast(WorldGroup worldGroup, String message, ArrayList<Player> players) {
         broadcast(worldGroup, new String[]{message}, players);
     }
 }
