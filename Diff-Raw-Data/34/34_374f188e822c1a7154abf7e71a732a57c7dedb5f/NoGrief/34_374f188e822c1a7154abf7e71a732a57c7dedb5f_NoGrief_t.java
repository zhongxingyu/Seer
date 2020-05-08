 package com.censoredsoftware.demigods.engine.conversation;
 
 import org.bukkit.conversations.Conversation;
import org.bukkit.entity.Player;
 import org.bukkit.event.Listener;
 
 import com.censoredsoftware.core.bukkit.ListedConversation;
 
 public class NoGrief implements ListedConversation
 {
 	// Define variables
 	private static Conversation conversation;
 
 	@Override
 	public Listener getUniqueListener()
 	{
 		return new NoGriefListener();
 	}
 
	@Override
	public Conversation startMenu(Player player)
	{
		return null;
	}

 	// TODO Everything.
 }
 
 class NoGriefListener implements Listener
 {
 	// TODO Everything.
 }
