 package com.test9.irc.engine;
 
 import java.util.Arrays;
 
 import com.test9.irc.parser.Message;
 import com.test9.irc.parser.Parser;
 
 /**
  * 
  * @author Jared Patton
  *
  */
 public class IRCEventAdapter implements IRCEventListener {
 
 	/**
 	 * Reference to the connection the adapter belongs to.
 	 */
 	private IRCConnection connection;
 
 	/**
 	 * Reference to the chat window to access the listener.
 	 */
 	private com.test9.irc.display.Listener cw;
 
 	public IRCEventAdapter(com.test9.irc.display.Listener cw, IRCConnection connection) {
 		this.connection = connection;
 		this.cw = cw;
 	}
 
 	@Override
 	public void onConnect(Message m) {
 
 
 	}
 
 	@Override
 	public void onDisconnect() {
 
 	}
 
 	@Override
 	public void onError(Message m) {
 		int numCode = Integer.valueOf(m.getCommand());
 		if(numCode == IRCConstants.ERR_NICKNAMEINUSE) {
 			System.out.println("ERR_NICKNAMEINUSE");
 			connection.setNick(connection.getNick()+"_");
 			connection.send("NICK "+ connection.getNick());
 		} else {
 			cw.onNewMessage(connection.getConnectionName(), connection.getConnectionName(),
 					"Error("+m.getCommand()+")"+m.getContent(), "ERROR");
 		}
 	}
 
 	@Override
 	public void onInvite() {
 
 	}
 
 	@Override
 	public void onJoin(String connectionName, String host, Message m) {

 		if(m.getNickname().equalsIgnoreCase(connection.getNick())) {
 			if(!(m.getContent().equals("")))
 			{
 				System.out.println("i am joining a channel myself.");
 				cw.onJoinChannel(connectionName, m.getContent());
 			}
 
 		} else if(m.getUser().equalsIgnoreCase(connection.getNick())) {
 			if(!(m.getContent().equals("")))
 			{
 				System.out.println("i am joining a channel myself.");
 				cw.onJoinChannel(connectionName, m.getContent());
 			}
 		}else {
 			if(!(m.getParams()[0].equals(""))) {
 				System.out.println("no params[0]");
 				cw.onUserJoin(connectionName, m.getParams()[0], m.getNickname(), false);
 			} else if(m.getContent().equals("")){
 				System.out.println("no content");
 				cw.onUserJoin(connectionName, m.getContent(), m.getNickname(), false);
 			}
 			if(!(connection.getUsers().contains(m.getNickname())))
 			{
 				connection.getUsers().add(new User(m.getNickname(), false));
 			}
 		}
 	}
 
 	@Override
 	public void onKick() {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void onMode(Message m) {
 		System.out.println("mode");
 	}
 
 	@Override
 	public void onMode(int two) {
 		System.out.println("mode");
 
 	}
 
 	@Override
 	public void onNick(Message m) {
 		// Set the user's nick in the conneciton user list
 		connection.getUser(m.getNickname()).setNick(m.getContent());
 
 		// Tell the chat window listener there was a nick change
 		cw.onNickChange(connection.getConnectionName(), m.getNickname(), m.getContent());
 
 		// If it is me
 		// Set my change my nick in the connection
 		if(m.getNickname().equals(connection.getNick()))
 			connection.setNick(m.getContent());
 
 	}
 
 	@Override	
 	public void onNotice(Message m) {
 		// Notify the listener of a new NoticeMessage
 		cw.onNotice(connection.getConnectionName(), m.getParams().toString(), m.getContent());
 	}
 
 	@Override
 	public void onPart(Message m) {
 		// If i parted a channel, tell the listener that i parted
 		try {
 			if(m.getNickname().equals(connection.getNick())) {
 				cw.onPartChannel(connection.getConnectionName(), m.getParams()[0]);
 			} else /* If someone else parted, notify of a user part*/ {
 				cw.onUserPart(connection.getConnectionName(), m.getParams()[0], m.getNickname());
 			}
 		} catch (Exception e) {
 			// TODO Auto-generated catch block
 		}
 	}
 
 	@Override
 	public void onPing(String line) {
 		System.out.println("got a ping");
 		connection.send("PONG " + line);
 		connection.send("WHO *");
 	}
 
 	@Override
 	public void onPrivmsg(String connectionName, String host, Message m) {
 
 		// If my name was mentioned in a message, notify the window listener of a highlight
 		if(m.getContent().toLowerCase().contains(connection.getNick().toLowerCase())) {
 
 			cw.onNewHighlight(connection.getUser(m.getNickname()),connectionName, m.getParams()[0], m.getNickname(), m.getContent());
 
 		} else { // Otherwise...
 
 			// If the message came from a user not in the channel (a ghost)
 			if(connection.getUser(m.getNickname())==null) {
 				// Add the user
 				connection.getUsers().add(new User(m.getNickname(), false));
 			}
 
 			// There is a new private message from some nickname for host at channel
 			// It is not a local ChatWindow event
 			if(!m.getParams()[0].equalsIgnoreCase(connection.getNick())) {
 				cw.onNewPrivMessage(
 						connection.getUser(m.getNickname()), connectionName, m.getParams()[0], 
 						m.getNickname(), m.getContent(), false);		
 
 			} 
 			// Else, this is a secret message to me and should make a new channel of the senders nick 
 			else if(m.getParams()[0].equalsIgnoreCase(connection.getNick())) {
 				cw.onNewPrivMessage(connection.getUser(m.getNickname()), connectionName, 
 						m.getNickname(), m.getNickname(), m.getContent(), false);
 			} // any others, well shit
 		}
 	}
 
 	@Override
 	public void onQuit(Message m) {
 		// Someone quit, notify the ChatWindow listener
 		cw.onUserQuit(connection.getConnectionName(), m.getNickname(), m.getContent());
 	}
 
 	@Override
 	public void onReply(Message m) {
 		// Get the numerical code of the server reply
 		int numCode = Integer.valueOf(m.getCommand());
 
 		// If it is a welcome message 001
 		if(numCode == IRCConstants.RPL_WELCOME) {
 			// Notify the chat window listener of a new irc connection
 			cw.onNewIRCConnection(connection);
 
 			// If the numCode is the name list of a channel
 		} else if(numCode == IRCConstants.RPL_NAMREPLY) {
 			//Split up the nicks list
 			String[] nicks = m.getContent().split(" ");
 
 			// Add nicks 1 at a time
 			for(String n : nicks)
 			{
 				// The nick has joined on a certain server at a certain channel
 				// It was a user reply
 				cw.onUserJoin(connection.getConnectionName(), m.getParams()[2], n, true);
 				// If the nick was not me
 				if(!n.equals(connection.getNick()))
 					//Add a new user to the Connections user list, false - not me
 					connection.getUsers().add(new User(n,false));
 				else
 					//Add myself ot the Connections user list, true - it was me
 					connection.getUsers().add(new User(n,true));
 			}
 			// If there is a topic message
 		} else if(numCode == IRCConstants.RPL_TOPIC) {
 			// Notify the chat window listener of a new topic
 			cw.onNewTopic(connection.getConnectionName(), m.getParams()[1], m.getContent());
 			// Notify the chat window listener that there is a new topic to show the user
 			cw.onNewMessage(connection.getConnectionName(), m.getParams()[1],
 					"<Topic> " + m.getContent(), "TOPIC");
 			// Someone went away
 		} else if(numCode == IRCConstants.RPL_NOWAWAY) {
 			System.out.println("some user went away");
 		} else if(numCode == IRCConstants.RPL_ISUPPORT) {
 			cw.onNewMessage(connection.getConnectionName(), connection.getConnectionName(), 
 					Arrays.toString(m.getParams())+" "+m.getContent(), "REPLY");
 			//List of channels available
 		} else if(numCode == IRCConstants.RPL_LIST) {
 			cw.onNewMessage(connection.getConnectionName(), 
 					connection.getConnectionName(), Arrays.toString(m.getParams()), "REPLY");
 		} else if(numCode == IRCConstants.RPL_MOTD) {
 			cw.onNewMessage(connection.getConnectionName(), connection.getConnectionName(), m.getContent(), "REPLY");
 		} else if(numCode == IRCConstants.RPL_WHOREPLY) {
 			char c = m.getParams()[6].charAt(0);
 			boolean isAway = Parser.isAway(c);
 			cw.onWhoReplyStatus(connection.getConnectionName(),m.getParams()[5], isAway);
 			
 			if(isAway)
 				cw.onNewMessage(connection.getConnectionName(), 
 						connection.getConnectionName(), m.getParams()[5]+" is now away.", "REPLY");
 			
 		} else if (numCode == IRCConstants.RPL_ENDOFNAMES) {
 			connection.send("WHO *");
 		} else {
 			cw.onNewMessage(connection.getConnectionName(), connection.getConnectionName(),
 					"Reply("+m.getCommand()+")"+m.getContent(), "REPLY");
 		}
 	}
 
 	@Override
 	public void onTopic(String connectionName, String host, Message m) {
 		// There is a topic message from the server
 		cw.onNewTopic(connectionName, m.getParams()[0], m.getContent());
 	}
 
 	@Override
 	public void onUnknown(String connectionName, String host, Message m) {
 		try{
 			cw.onNewMessage(connectionName, connectionName, m.getContent(), "REPLY");	
 		}catch(Exception e){}
 	}
 }
