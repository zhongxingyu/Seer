 /*
  *  ExperienceMod - Bukkit server plugin for modifying the experience system in Minecraft.
  *  Copyright (C) 2012 Kristian S. Stangeland
  *
  *  This program is free software; you can redistribute it and/or modify it under the terms of the 
  *  GNU General Public License as published by the Free Software Foundation; either version 2 of 
  *  the License, or (at your option) any later version.
  *
  *  This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
  *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
  *  See the GNU General Public License for more details.
  *
  *  You should have received a copy of the GNU General Public License along with this program; 
  *  if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 
  *  02111-1307 USA
  */
 
 package com.comphenix.xp.messages;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import com.comphenix.xp.parser.Utility;
 
 import org.bukkit.Server;
 import org.bukkit.entity.Player;
 
 /**
  * Simple default chat system. Can either broadcast or send a message to the current player.
  * 
  * @author Kristian
  */
 public class StandardService implements ChannelService {
 
 	public static final String NAME = "STANDARD";
 	private static Map<String, Channels> channelList = new HashMap<String, Channels>();
 	
 	public enum Channels {
 		GLOBAL,
 		WORLD,
 		PRIVATE
 	}
 	
 	// Associate every value
 	static {
 		for (Channels value : Channels.values()) {
 			channelList.put(value.name(), value);
 		}
 	}
 	
 	private Server server;
 	
 	public StandardService(Server server) {
 		this.server = server;
 	}
 	
 	@Override
 	public String getServiceName() {
 		return NAME;
 	}
 
 	@Override
 	public boolean hasChannel(String channelID) {
 		return getChannel(channelID) != null;
 	}
 
 	@Override
 	public void announce(String channelID, String message) {
 		// Do it globally. Private messages will be lost.
 		if (hasChannel(channelID)) {
 			switch (getChannel(channelID)) {
 			case GLOBAL:
 			case WORLD:
 				server.broadcastMessage(message);
 				break;
			case PRIVATE:
				// We don't know to whom, so print it to the console
				server.getLogger().info(String.format("Lost message: " + message));
				break;
 			}
 		}
 	}
 
 	@Override
 	public void emote(String channelID, String message, Player sender) {
 		// Decide what to do
 		if (hasChannel(channelID)) {
 			switch (getChannel(channelID)) {
 			case GLOBAL:
 				server.broadcastMessage(message);
 				break;
 				
 			case WORLD:
 				// Broadcast to every player located in the same world
 				if (sender != null) {
 					for (Player player : sender.getWorld().getPlayers()) {
 						player.sendMessage(message);
 					}
 				}
 				
 				break;
 				
 			case PRIVATE:
 				// Private message
 				if (sender != null)
 					sender.sendMessage(message);
 				break;
 			}
 		}
 	}
 
 	private Channels getChannel(String channelID) {
 		// Look up channel 
 		return channelList.get(Utility.getEnumName(channelID));
 	}
 }
