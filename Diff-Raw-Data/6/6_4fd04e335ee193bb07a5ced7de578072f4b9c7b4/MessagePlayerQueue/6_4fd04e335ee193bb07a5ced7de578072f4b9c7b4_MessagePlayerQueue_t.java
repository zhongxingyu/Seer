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
 
 import org.bukkit.entity.Player;
 
 import com.comphenix.xp.Action;
 import com.comphenix.xp.Debugger;
 import com.comphenix.xp.listeners.PlayerCleanupListener;
 
 public class MessagePlayerQueue implements PlayerCleanupListener {
 	
	private Map<Player, MessageQueue> queues = new HashMap<Player, MessageQueue>();
 	
 	private long messageDelay;
 	
 	private Debugger debugger;
 	private ChannelProvider channelProvider;
 
 	public MessagePlayerQueue(long messageDelay, ChannelProvider channelProvider, Debugger debugger) {
 		this.messageDelay = messageDelay;
 		this.channelProvider = channelProvider;
 		this.debugger = debugger;
 	}
 
 	public Debugger getDebugger() {
 		return debugger;
 	}
 
 	public ChannelProvider getChannelProvider() {
 		return channelProvider;
 	}
 
 	public void setChannelProvider(ChannelProvider channelProvider) {
 		this.channelProvider = channelProvider;
 	}
 	
 	/**
 	 * Enqueues a message transmitted by a player (or environment, in which case player should be NULL), 
 	 * ensuring that there aren't too many messages sent at once.
 	 * @param player - a player (or NULL) that sent this message.
 	 * @param action - the action that contains the sent message.
 	 * @param formatter - a message formatter that contains all the parameters.
 	 */
 	public void enqueue(Player player, Action action, MessageFormatter formatter) {
 		
 		MessageQueue queue = queues.get(player);
 		
 		// Construct queue if it hasn't been already
 		if (queue == null) {
 			queue = new MessageQueue(messageDelay, player, channelProvider, debugger);
 			queues.put(player, queue);
 		}
 		
 		// Let the queue handle the rest
 		queue.enqueue(action, formatter);
 	}
 	
 	/**
 	 * Gets the current message delay in milliseconds.
 	 * @return Message delay in milliseconds.
 	 */
 	public long getMessageDelay() {
 		return messageDelay;
 	}
 
 	/**
 	 * Sets the message delay in milliseconds.
 	 * @param messageDelay The new message delay in milliseconds.
 	 */
 	public void setMessageDelay(long messageDelay) {
 		this.messageDelay = messageDelay;
 	}
 	
 	/**
 	 * Creates a shallow copy of this queue. Enqueued messages are not copied.
 	 * @return A copy of the current queue.
 	 */
 	public MessagePlayerQueue createView() {
 		return new MessagePlayerQueue(messageDelay, channelProvider, debugger);
 	}
 	
 	public void onTick() {
 		for (MessageQueue queue : queues.values()) 
 			queue.onTick();
 	}
 
 	@Override
 	public void removePlayerCache(Player player) {
 		queues.remove(player);
 	}
 }
