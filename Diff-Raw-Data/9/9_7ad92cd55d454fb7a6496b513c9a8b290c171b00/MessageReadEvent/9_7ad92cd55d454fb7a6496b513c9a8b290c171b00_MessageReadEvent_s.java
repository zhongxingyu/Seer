 package org.x3.mail.event;
 
 import org.bukkit.command.CommandSender;
 import org.bukkit.event.Event;
 import org.bukkit.event.HandlerList;
 import org.x3.mail.util.Message;
 
 public class MessageReadEvent extends Event {
 
	private static HandlerList handlers;
 	private final CommandSender sender;
 	private final Message[] messages;
 
 	public MessageReadEvent(CommandSender sender, Message[] messages) {
 		this.sender = sender;
 		this.messages = messages;
 	}
 
 	public CommandSender getSender() {
 		return sender;
 	}
 
 	public Message[] getMessages() {
 		return messages;
 	}
 
 	@Override
 	public HandlerList getHandlers() {
 		return handlers;
 	}
 
 	public static HandlerList getHandlerList() {
 		return handlers;
 	}
 
 }
