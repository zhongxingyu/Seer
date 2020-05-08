 package org.x3.mail.event;
 
 import org.bukkit.event.Event;
 import org.bukkit.event.HandlerList;
 import org.x3.mail.util.Message;
 
 public class MessageSentEvent extends Event {
 
	private static HandlerList handlers;
 	private final Message message;
 
 	public MessageSentEvent(Message message) {
 		this.message = message;
 	}
 
 	public Message getMessage() {
 		return message;
 	}
 
 	@Override
 	public HandlerList getHandlers() {
 		return handlers;
 	}
 
 	public static HandlerList getHandlerList() {
 		return handlers;
 	}
 
 }
