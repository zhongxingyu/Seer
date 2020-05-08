 package com.censoredsoftware.demigods.engine.conversation;
 
 import org.bukkit.conversations.Conversation;
 import org.bukkit.event.Listener;
 
 import com.censoredsoftware.core.bukkit.ListedConversation;
 
 public class Administration implements ListedConversation
 {
 	// Define variables
 	private static Conversation conversation;
 
 	@Override
 	public Listener getUniqueListener()
 	{
 		return new NoGriefListener();
 	}
 
 	// TODO Everything.
 }
 
 class AdministrationListener implements Listener
 {
 	// TODO Everything.
 }
