 package uk.co.cynicode.CyniCord.Listeners;
 
 import java.io.ByteArrayOutputStream;
 import java.io.DataOutputStream;
 import java.io.IOException;
 import java.util.Iterator;
 import java.util.Set;
 
 import net.md_5.bungee.api.config.ServerInfo;
 
 import uk.co.cynicode.CyniCord.CyniCord;
 
 import org.pircbotx.hooks.ListenerAdapter;
 import org.pircbotx.hooks.events.MessageEvent;
 import org.pircbotx.hooks.events.PrivateMessageEvent;
 
 /**
  * Deal with all the output that the bot can see
  *
  * @author Matthew Ball
  *
  */
 public class IRCChatListener extends ListenerAdapter {
 
 	/**
 	 * Listen for all the chatter that is going on on the IRC bot's end so that
 	 * any commands given there are going to be executed while commands given
 	 * from inside MC will be left alone.
	 * @param event : The message event we're going to examine
	 * @throws Exception
 	*/
	@Override
 	public void onMessage(MessageEvent event) throws Exception {
 		if (event.getMessage().startsWith(":?")) {
 			String[] argments = event.getMessage().split(" ");
 			org.pircbotx.Channel thisChan = event.getChannel();
 			
 			if (argments.length == 1) {
 				CyniCord.printDebug("Default used...");
 				//    ircResponses.helpOutput(event.getBot(), event.getUser());
 				return;
 			}
 			
 			if (argments[1].equalsIgnoreCase("help")) {
 				CyniCord.printDebug("Help selected...");
 				//    ircResponses.helpOutput(event.getBot(), event.getUser());
 				return;
 			}
 			
 			if (argments[1].equalsIgnoreCase("list")) {
 				CyniCord.printDebug("Listing chosen...");
 				if (argments.length > 2) {
 					if (argments[2].equalsIgnoreCase("all")) {
 						CyniCord.printDebug("You've either got 'all' as parameter...");
 						CyniCord.printDebug(event.getUser().getNick() + " : " + thisChan.getName());
 						//ircResponses.listOutput(event.getUser(), event.getBot(), thisChan.getName(), true);
 						return;
 					}
 				} else {
 					CyniCord.printDebug("Or you don't....");
 					CyniCord.printDebug(event.getUser().getNick() + " : " + thisChan.getName());
 					// ircResponses.listOutput(event.getUser(), event.getBot(), thisChan.getName(), false);
 					return;
 				}
 				return;
 			}
 			
 			if (argments[1].equalsIgnoreCase("kick")
 				&& thisChan.isOp(event.getUser())) {
 				CyniCord.printDebug("Kicking...");
 				if (argments[2] != null) {
 					// ircResponses.kickOutput(event.getUser(), event.getBot(), argments[2], thisChan.getName());
 					return;
 				}
 				event.respond("I'm sorry, you must include a person to be kicked");
 				return;
 			}
 			
 			if ((argments[1].equalsIgnoreCase("ban")
 				|| argments[1].equalsIgnoreCase("unban"))
 				&& thisChan.isOp(event.getUser())) {
 				CyniCord.printDebug("Banning...");
 				if (argments[2] != null) {
 					
 					if (argments[1].equalsIgnoreCase("ban")) {
 						// ircResponses.banOutput(event.getUser(), event.getBot(), argments[2], event.getChannel().getName(), false);
 					} else {
 						// ircResponses.banOutput(event.getUser(), event.getBot(), argments[2], event.getChannel().getName(), true);
 					}
 					return;
 				}
 				event.respond("I'm sorry, you must include a person to be un/banned");
 				return;
 			}
 			
 			if ((argments[1].equalsIgnoreCase("mute")
 				|| argments[1].equalsIgnoreCase("unmute"))
 				&& thisChan.isOp(event.getUser())) {
 				CyniCord.printDebug("Muting...");
 				if (argments[2] != null) {
 					
 					if (argments[1].equalsIgnoreCase("mute")) {
 						// ircResponses.muteOutput(event.getUser(), event.getBot(), argments[2], thisChan.getName(), false);
 					} else {
 						// ircResponses.muteOutput(event.getUser(), event.getBot(), argments[2], thisChan.getName(), true);
 					}
 					return;
 					
 				}
 				event.respond("I'm sorry, you must include a person to be un/muted");
 				return;
 			}
 			
 			if (argments[1].equalsIgnoreCase("restart")
 				&& thisChan.isOp(event.getUser())) {
 				CyniCord.printDebug("Restarting...");
 				CyniCord.PBot.restart();
 				return;
 			}
 			
 			if (argments[1].equalsIgnoreCase("kill")
 				&& thisChan.isOp(event.getUser())) {
 				CyniCord.printDebug("Murdering...");
 				CyniCord.PBot.stop();
 				return;
 			}
 			
 			CyniCord.printDebug("\"" + argments[1] + "\"");
 			
 			return;
 		}
 		
 		CyniCord.printDebug("Sender: " + event.getUser().getNick());
 		CyniCord.printDebug("Channel: " + event.getChannel().getName().toLowerCase());
 		CyniCord.printDebug("Message: " + event.getMessage());
 		
 		//Check if we are linked to this channel, and if so forward 
 		String ircChannelName = event.getChannel().getName().toLowerCase();
 		
 		Set<String> keySet = CyniCord.servers.keySet();
 		Iterator<String> iterKeys = keySet.iterator();
 		
 		while ( iterKeys.hasNext() ) {
 			
 			String thisKey = iterKeys.next();
 			ServerInfo thisServer = CyniCord.servers.get( thisKey );
 			
 			if ( thisServer.getPlayers().size() > 0 ) {
 				
 				try {
 					//Create message
 					ByteArrayOutputStream b = new ByteArrayOutputStream();
 					DataOutputStream out = new DataOutputStream(b);
 					out.writeUTF("");
 					out.writeUTF( event.getUser().getNick() );
 					out.writeUTF( ircChannelName );
 					out.writeUTF( event.getMessage() );
 					
 					ByteArrayOutputStream msgBytes = new ByteArrayOutputStream();
 					DataOutputStream msg = new DataOutputStream(msgBytes);
 					msg.writeUTF("Forward");
 					msg.writeUTF("ALL");
 					msg.writeUTF("CyniChat");
 					//Push message content
 					msg.writeShort(b.toByteArray().length);
 					msg.write(b.toByteArray());
 					
 					thisServer.sendData( "BungeeCord", msgBytes.toByteArray() );
 					
 				} catch (IOException ex) {
 					CyniCord.printSevere("Error sending message to BungeeChannelProxy");
 					ex.printStackTrace();
 				}
 			}
 		}
 		
 	}
 	
 	@Override
 	public void onPrivateMessage(PrivateMessageEvent event) {
 		CyniCord.printDebug("Private message called!");
 		if (event.getMessage().startsWith(":?")) {
 			String[] argments = event.getMessage().split(" ");
 			CyniCord.printDebug(":? called by " + event.getUser().getNick());
 			
 			if (argments[1].equalsIgnoreCase("talk")) {
 				CyniCord.printDebug("Talking with " + argments.length + " args... ");
 				if (argments.length > 3) {
 					
 					CyniCord.printDebug("Talking...");
 					/*if (ircResponses.talkOutput(event.getBot(), argments[2], stacker(argments, 3, argments.length)) == false) {
 						event.respond("Invalid statement. Please make sure that channel exits in the MC server.");
 					}*/
 				}
 				
 				return;
 			}
 			
 			// ircResponses.helpOutput(event.getBot(), event.getUser());
 		}
 	}
 	
 	public String stacker(String[] args, int start, int end) {
 		
 		String finalString = "";
 		String connector = "";
 		
 		for (int i = start; i < end; i++) {
 			finalString += connector + args[i];
 			connector = " ";
 		}
 		
 		return finalString;
 	}
 }
