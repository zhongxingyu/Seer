 
 import java.awt.BorderLayout;
 //import javax.swing.JFrame;
 import javax.swing.*;
 
 import java.awt.Color;
 
 import irc.*;
 
 import java.util.List;
 
 class Client extends JFrame {
 
 	/**
 	 * IRC Connection object.
 	 */
 	private Connection irc;
 	
 	/**
 	 * Two status windows.
 	 */
 	private ChatWindow status, debug;
 
 	/**
 	 * Tabs to switch between status/pm/channel windows.
 	 */
 	private JTabbedPane tabs;
 
 	/**
 	 * List of the windows (this may seem redundant as
 	 * the windows are stored in the JTabbedPane,
 	 * but each chat window isn't necessarily a component,
 	 * it simply provides a component to display in the JTabbedPane,
 	 * so there are potentially cases in which it is not possible
 	 * to get the instance of ChatWindow from the JTabbedPane
 	 */
 	private List<ChatWindow> windows;
 
 	/**
 	 * Channel state syncing.
 	 */
 	private client.SyncManager sync;
 
 	/**
 	 * Default channel to join.
 	 * @TODO connection dialog.
 	 */
 	private final String CHAN = "#foo";
 
 	/**
 	 * Basic standalone starter
 	 */
 	public static void main(String[] argv) {
 		new Client();
 	}
 
 	/**
 	 * **BASIC** prototype.
 	 */
 	private Client() {
 		
 		setSize(1000,800);
 
 		setTitle("Irc client");
 
 		windows = new util.LinkedList<ChatWindow>();
 
 		setVisible(true);
 
 		//tabs is the main viewport.
 		tabs = new JTabbedPane();
 		add(tabs);
 
 		//tabs contain a status window.
 		tabs.setTabPlacement(JTabbedPane.BOTTOM);
 	
 		add( debug = new GenericChatWindow("Debug", ChatWindow.Type.STATUS) );
 		add( status = new GenericChatWindow("Status", ChatWindow.Type.STATUS) );
 
 		status.put("Creating a new IRC connection...");
 
 		//create a new IRC connection
 		irc = new Connection("irc.jaundies.com", 6667, "fubar");
 
 
 		//prototyping purposes, just receive ALL messages...
 		irc.addMessageHandler(messageHandler);
 
 		/**
 		 * IMPORTANT: Sync is registered after messageHandler.
 		 * This class implicitly relies on the order guarantees by
 		 * Connection.registerMessageHandler()
 		 *
 		 * @see Connection.registerMessageHandler() for more information
 		 */
 		sync = new client.SyncManager(irc);
 
 		try {
 			//@TODO
 			irc.connect();
 		} catch (java.io.IOException e) {
 			//@TODO...
 			e.printStackTrace();
 		}
 
 		irc.join( CHAN );
 
 		System.out.println("Thread sleeeeeping.");
 
 		synchronized(irc) {
 			try {
 				irc.wait();
 			} catch (InterruptedException e) {
 				e.printStackTrace();
 			}
 		}
 
 		System.out.println("IRC changed it's state... goodbye.");
 
 		System.exit(0);
 	}
 
 	/**
 	 * Get the chat window with thespecified name
 	 * 
 	 * @param name name of the ChatWindow to retrieve.
 	 * @return chat window with the specified name, or null if no such window.
 	 */
 	private ChatWindow getWindow(String name) {
 		//heh this is efficient :/
 		for (ChatWindow c : windows) 
 			if ( c.getName().equals(name) )
 				return c;
 
 		return null;
 	}
 	
 	/**
 	 * Remove teh specified chat window from the list of windows
 	 * and the tabbed pane.
 	 *
 	 * @param c ChatWindow to remove
 	 */
 	private void remove(ChatWindow c) {
 		
 		//remove the tab.
 		tabs.remove(c.getContentPane());
 
 		//forgot about the chatwindow
 		windows.remove(c);
 
 		//and ignore its commands
 		c.removeActionListener( commandListener );
 	}
 
 	/**
 	 * Add the specified chat window to the list of windows
 	 * and the tabbed pane.
 	 *
 	 * @param c ChatWindow to add
 	 */
 	private void add(ChatWindow c) {
 
 		//listen to commands from the window
 		c.addActionListener( commandListener );
 
 		//since the chat window is not necessarily a JComponent,
 		//we need to extract the contentPane
 		tabs.addTab(c.getName(), c.getContentPane());
 
 		//add it to the list of chatwindows
 		windows.add(c);
 
 		//and select the new tab.
 		tabs.setSelectedComponent( c.getContentPane() );
 	}
 
 	/**
 	 * Handles a "slash command"
 	 *
 	 * @param src the ChatWindow that initiated this command
 	 * @param msg the command string from the ChatWindow.
 	 */
 	private void handleCommand(ChatWindow src, String msg) {
 		Command cmd = new Command(msg);
 
 		if ( cmd.equals("JOIN") && cmd.numArgs() >= 1 ) {
 			irc.join( cmd.getArg(0) );
 
 
 		//note thaton part, we only send a pART to teh server
 		//if the part is succesful, the server will send a PART back
 		//and then the channel window will close...
 		} else if ( cmd.equals("PART") ) {
 	
 			//there's channel specified in the command.
 			if ( cmd.numArgs() > 0 && cmd.getArg(0).charAt(0) == '#' ) {
 
 				irc.part(cmd.getArg(0), cmd.getFinal(1));
 
 			//there was no channel specified, but it was 
 			//run in a channel window
 			} else if ( src.getType() == ChatWindow.Type.CHANNEL ) {
 				irc.part(src.getName() , cmd.getFinal(0) );
 			}
 			//otherwise do nothing.
 		} else if ( cmd.equals("CLOSE" ) ) {
 
 			if ( src.getType() == ChatWindow.Type.QUERY ) remove(src);
 
 		} else if ( cmd.equals("QUIT") ) {
 			irc.quit( cmd.getFinal(0) );
 
 		// /msg target msg
 		} else if ( cmd.equals("MSG") ) {
 
 			if ( cmd.numArgs() < 2 ) return;
 			
 			String target = cmd.getArg(0);
 			String message = cmd.getFinal(1);
 			
 			ChatWindow c = getWindow(target);
 
 			if ( c == null ) {
 
 				//we can't just pop a new window for messagine a
 				//channel that we haven't joined...
 				if (target.charAt(0) == '#') return;
 
 				//otherwise, it is a PM....
 				c = new GenericChatWindow( target, ChatWindow.Type.QUERY );
 			
 				add(c);
 			}
 
 			c.put( new QueryMessage( MessageType.QUERY, irc.nick(), message, QueryMessage.Dir.OUTGOING ) );
 			irc.msg(target, message);
 
 		} else if ( cmd.equals("NOTICE") ) {
 			
 			if ( cmd.numArgs() < 2 ) return;
 
 			String target = cmd.getArg(0);
 			String message = cmd.getFinal(1);
 	
 			irc.notice( target, message );
 
 			src.put( new QueryMessage( MessageType.NOTICE, target, message, QueryMessage.Dir.OUTGOING) );
 		
 			///fss why am I doin this.
 		} else if ( cmd.equals("ME" ) ) {
 
 			// /me arg1"
 
 			if ( cmd.numArgs() < 1) return;
 			
 			if ( src.getType() != ChatWindow.Type.QUERY && src.getType() != ChatWindow.Type.CHANNEL) return;	
 
 			irc.action( src.getName(), cmd.getFinal(0) );
 
 			src.put( 
 				new QueryMessage( MessageType.ACTION, irc.nick(), cmd.getFinal(0), QueryMessage.Dir.OUTGOING)
 			);
 		} else {
 			irc.send(cmd.cmd + " " + cmd.getFinal(0));
 		}
 	}
 
 	/**
 	 * Parses a /Commanmd from a ChatWindow
 	 * and provides access to the arguments.
 	 */
 	private static class Command {
 
 		String cmd;
 		String msg;
 		String[] args;
 
 		/**
 		 * Parse the command
 		 * 
 		 * @param msg The command (includeing /) to parse.
 		 */
 		private Command(String msg) {
 			int sp = msg.indexOf(' ');
 
 			//it's a command like '/part' with no arguments...
 			if (sp == -1) sp = msg.length();
 
 			//cut off the 1, up to the first space.
 			//and convert to uppercase for simple matching.
 			this.cmd = msg.substring(1,sp).toUpperCase();
 
 			//if there was no first space, there are no arguments
 			if ( msg.length() == sp )
 				args = null;
 
 			//otherwise, parse the argument list.
 			else {
 				msg = msg.substring( sp + 1 );
 				args = msg.split(" ");
 			}
 		}
 
 		private int numArgs() {
 			return (args == null) ? 0 : args.length;
 		}
 
 		private String getArg(int n) {
 			return args[n];
 		}
 
 		/**
 		 * Most IRC commands have the followin format:
 		 * /COMMAND[ ARG0[ ARG1 ... [ARGN][ FINAL argument that may include spaces]
 		 * This method returns the final argument, given an index of 
 		 * args at which to begin concatenating
 		 *
 		 * NOTE: this will silently handle out of bounds errors by returning an empty string.
 		 *
 		 * @param n index of args at which to begin concatenating.
 		 * @return Concatenation of all arguements beginning at index n and separted by spaces.
 		 */
 		private String getFinal(int n) {
 			String ret = "";
 
 			for ( int i = n; args != null && i < args.length; ++i) 
 				ret += ((i > n) ? " " : "") + args[i];
 
 			return ret;
 		}
 
 		private boolean equals(String s) {
 			return cmd.equals(s.toUpperCase());
 		}
 	}
 
 	/**
 	 * Handles Message(s) from the IRC Message Dispatcher.
 	 */
 	private MessageHandler messageHandler = new MessageHandler() {
 
 		/**
 		 * Handle a message...
 		 */
 		public void handle(Message msg) {
 
 			//because a lot of these need a window and a user
 			//and case blocks don't have scope
 			//I really should just handle these wtih refelection or something.
 			client.User user;
 			ChatWindow win;
 
 			switch(msg.getType()) {
 		
 				//all user->user/channel messages.
 				case CHANNEL:
 				case ACTION:
 				case QUERY: 
 				case NOTICE:
 					handlePM(msg);
 					break;
 
 				case PART:
 		
 					win = getWindow(msg.getTarget().getChannel());
 
 					if ( win != null ) {
 			
 						//this is ME leaving...
 						//note that when I type /part, it just SENDS the part command
 						//the window isn't removed until the server responds with a PART reply...
 						if ( msg.getSource().getNick().equals( irc.nick() ) ) 
 							remove(win);
 
 						else 
 							win.put(
 								(new PaintableMessage()).append("<-- ",Color.lightGray).append(msg.getSource().getNick(), Color.white)
 									.append(" [", Color.darkGray).append(msg.getSource().toString(), Color.cyan).append("]",Color.darkGray).append(" left ")
 									.append(msg.getTarget().toString(),Color.cyan).append(" (",Color.darkGray).append( msg.getMessage() )
 									.append(")", Color.darkGray).indent(4)
 							);
 					}
 
 					break;
 				
 				case JOIN:
 					win = null;
 
 					//if I joined a channel, pop a new window...
 					if ( msg.getSource().getNick().equals(irc.nick()) ) {
 						win = new ChannelWindow( msg.getTarget().getChannel(), sync );
 						add( win );
 				
 					//if I didn't, I'm already there...
 					}  else {
 						win = getWindow( msg.getTarget().getChannel() );
 					}
 
 					//in every case:
 					if (win != null)  {
 						win.put((new PaintableMessage())
 							.append("--> ",Color.lightGray).append(msg.getSource().getNick(), Color.white)
 							.append(" [", Color.darkGray).append(msg.getSource().toString(), Color.cyan).append("]",Color.darkGray).append(" has joined ")
 							.append(msg.getTarget().toString(),Color.cyan).indent(4)
 						);
 					}
 
 					break;
 				case MOTD:
 					status.put((new PaintableMessage()).append("[",Color.gray).append("MOTD",Color.blue).append("] ",Color.gray).append(msg.getMessage() ).indent(7));
 					break;
 
 				case TOPIC:
 					win = getWindow( msg.getArg(2) );
 					if ( win == null ) break;
 
 					win.put( (new PaintableMessage()).indent(4)
 						.append("--- ", Color.lightGray).append( msg.getArg(2)+": ", Color.white ).append( msg.getMessage(), Color.cyan )
 					);
 
 					break;
 
 				/**
 				 * @TODO...
 				 * NICKCHANGE and QUIT need to check QUERY windows as well
 				 * QUERY windows will need renaming on QUIT...
 				 */
 				case NICKCHANGE:
 					user = sync.getUser( msg.getSource().getNick() );
 					
 					for ( client.Channel channel : user ) if ( (win = getWindow( channel.getName() )) != null) {
 						win.put(
 							(new PaintableMessage()).append("--- ",Color.lightGray).append(msg.getSource().getNick(), Color.white)
 								.append(" is now known as ").append(msg.getTarget().getNick(), Color.cyan)
 							);
 					}
 
 					break;
 
 				case QUIT:
 					user = sync.getUser( msg.getSource().getNick() );
 
 					for ( client.Channel channel : user ) if ( (win = getWindow( channel.getName() )) != null) {
 						win.put(
 							(new PaintableMessage()).append("<-- ",Color.lightGray).append(msg.getSource().getNick(), Color.white)
 								.append(" [", Color.darkGray).append(msg.getSource().toString(), Color.cyan).append("]",Color.darkGray).append(" QUIT ")
 								.append("(",Color.darkGray).append( msg.getMessage() )
 								.append(")", Color.darkGray).indent(4)
 							);
 					}
 
 					break;
 
 				case ERROR:
 					status.put( (new PaintableMessage()) 
 						.append("ERROR: ",Color.red).append("[",Color.darkGray).append(msg.getCommand()).append("] ",Color.darkGray)
 						.append( msg.getMessage(), Color.orange).indent(7)
 					);
 
 					debug.put( (new PaintableMessage()).append(msg.getRaw(), Color.red));
 					break;
 
 				case LOGIN:
 				case INFO:
 					status.put( new QueryMessage(msg) );
 
 					//no break here for now...
 				default:
 					debug.put( msg.getRaw() );	
 					break;
 			}
 		}
 
 		/**
 		 * Handle PRIVMSG or NOTICE commands.
 		 */
 		private void handlePM(Message msg) {
 	
 
 			ChatWindow window = null;
 
 			//notices go to the currently selected window
 			//servers should only be sending notices
 			//but in case a server PMs, we handle that as a notice as well
 			if ( msg.getType() == MessageType.NOTICE || msg.getSource().scope(MessageTarget.Scope.SERVER) )  {
 				window = getWindow( tabs.getTitleAt(tabs.getSelectedIndex()) );
 
 				if (window == null)
 					return;
 			}
 
 			//not a notice, to a channel.
 			else if (  msg.getTarget().scope(MessageTarget.Scope.CHANNEL) ) {
 				window = getWindow( msg.getTarget().getChannel() );
 
 				//don't open up new windows automatically for channels
 				//that should have been done anyway...
 				if (window == null)
 					return;
 
 			//it is not a notice, and not a channel message...
 			//so it must be a PM... from a user!
 			} else {
 					
 				window = getWindow( msg.getSource().getNick() );
 
 				if ( window == null ) {
 					window = new GenericChatWindow( msg.getSource().getNick() , ChatWindow.Type.QUERY );
 					add(window);
 				}
 			}
 
 			//now we have a window to put the damn thing in...
 			window.put( new QueryMessage(msg) );
 		}
 	};
 
 	/*
 	 * TEMP to provide working chatting...
 	 */
 	private java.awt.event.ActionListener commandListener = new java.awt.event.ActionListener() {
 		public void actionPerformed(java.awt.event.ActionEvent e) {
 
 			//@TODO
 			if ( ! (e.getSource() instanceof ChatWindow) ) 
 				throw new RuntimeException("Why am I receiving commands from a non-chat window???");
 
 			ChatWindow src = (ChatWindow)e.getSource();
 			String cmd = e.getActionCommand();
 
 			debug.put("CMD: ["+src.getName()+"] "+cmd);
 
 			if ( cmd.length() < 1 ) return;
 
 			if ( cmd.charAt(0) == '/' ) {
 				handleCommand(src, cmd);
 
 			//if it's in a status window and it doesn't start with a /, do nothing
 			} else if ( src.getType() == ChatWindow.Type.STATUS ) {
 
 			//otherwise, it is in some form of chat window, so send a message...
 			} else {
				src.put(new QueryMessage( MessageType.QUERY, irc.nick(), cmd, QueryMessage.OUTGOING));
				irc.msg( src.getName() , cmd );	
 		}
 	};
 }
