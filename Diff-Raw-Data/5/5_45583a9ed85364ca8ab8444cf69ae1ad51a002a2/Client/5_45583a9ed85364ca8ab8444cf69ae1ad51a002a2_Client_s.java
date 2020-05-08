 
 import java.awt.BorderLayout;
 //import javax.swing.JFrame;
 import javax.swing.*;
 
 import irc.*;
 
 import java.util.List;
 
 class Client extends JFrame {
 
 	private Connection irc;
 	
 	private ChatWindow status, channel;
 
 	private JTabbedPane tabs;
 
 	private List<ChatWindow> windows;
 
 	private client.SyncManager sync;
 
 	public static void main(String[] argv) {
 		new Client();
 	}
 
 	private final String CHAN = "#foo";
 
 	/**
 	 * **BASIC** prototype.
 	 */
 	private Client() {
 		irc = new Connection("irc.jaundies.com", 6667, "fubar");
 		
 		setSize(800,800);
 
 		setTitle("Irc client");
 
 		windows = new util.LinkedList<ChatWindow>();
 
 		setVisible(true);
 
 		//tabs is the main viewport.
 		tabs = new JTabbedPane();
 		add(tabs);
 
 		//tabs contain a status window.
 		tabs.setTabPlacement(JTabbedPane.BOTTOM);
 
 		add( status = new GenericChatWindow("Status", ChatWindow.Type.STATUS) );
 
 		sync = new client.SyncManager(irc);
 
 		//prototyping purposes, just receive ALL Pms
 		irc.addMessageHandler(messageHandler);
 
 		try {
 			//@TODO
 			irc.connect();
 		} catch (java.io.IOException e) {
 			e.printStackTrace();
 		}
 
 		irc.join( CHAN );
 
 		System.out.println("Thread is going to sleep...");
 
 		synchronized(irc) {
 			try {
 				irc.wait();
 			} catch (InterruptedException e) {
 				e.printStackTrace();
 			}
 		}
 
 		System.out.println("IRC changed it's state... goodbye.");
 
 	}
 
 	private void remove(ChatWindow c) {
 		tabs.remove(c.getContentPane());
 		windows.remove(c);
 	}
 
 	private void add(ChatWindow c) {
 		tabs.addTab(c.getName(), c.getContentPane());
 		windows.add(c);
 	}
 
 	/**
 	 * Handles a "slash command"
 	 */
 	private void handleCommand(ChatWindow src, String msg) {
 		Command cmd = new Command(msg);
 
 		System.out.println("CMD: '" +cmd.cmd+"'");
 
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
 		} else if ( cmd.equals("QUIT") ) {
 			irc.quit( cmd.getFinal(0) );
 			System.exit(0);
 		}
 	}
 
 	private class Command {
 
 		String cmd;
 		String msg;
 		String[] args;
 
 		private Command(String msg) {
 			int sp = msg.indexOf(' ');
 
 			//it's a command like '/part' with no arguments...
 			if (sp == -1) sp = msg.length();
 
 			this.cmd = msg.substring(1,sp).toUpperCase();
 
 			if ( msg.length() == sp )
 				args = null;
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
 
 		private String getFinal(int n) {
 			String ret = "";
 
 			for ( int i = n; args != null && i < args.length; ++i) 
 				ret += args[i];
 
 			return ret;
 		}
 
 		private boolean equals(String s) {
 			return cmd.equals(s);
 		}
 	}
 
 	/**
 	 * for prototyping, just send all privmsgs to a window...
 	 */
 	private MessageHandler messageHandler = new MessageHandler() {
 
 		//and put all PMS whether channel or private in one window...
 		public void handle(Message msg) {
 
 			if ( msg.getType() == MessageType.CHANNEL || (msg.getType() == MessageType.ACTION && msg.getTarget().scope(MessageTarget.Scope.CHANNEL) ) ) {
 
 				for (ChatWindow c : windows) {
 					
 					if ( c.getType() == ChatWindow.Type.CHANNEL && c.getName().equals(msg.getTarget().getChannel()) ) {
 						
 						if ( msg.getType() == MessageType.ACTION )
 							c.put("*" + msg.getSource().getNick() + " " +msg.getMessage() );
 						else 
 							c.put( "<" + msg.getSource().getNick() + "> " + msg.getMessage() );
 						break;
 					}
 
 				}
 
 			} else if ( msg.getType() == MessageType.PART ) {
 
 				ChatWindow window = null;
 
 				for (ChatWindow w : windows) {
 					if ( w.getType() == ChatWindow.Type.CHANNEL && w.getName().equals(msg.getTarget().getChannel())) {
 						window = w;
 						break;
 					}
 				}
 
 				if ( window != null ) {
 			
 					//this is ME leaving...
 					if ( msg.getSource().getNick().equals( irc.nick() ) ) 
 						remove(window);
 
 					else 
 						window.put(" <-- " + msg.getSource().getNick() + " left the channel");
 				}
 				
 	
 			} else if ( msg.getType() == MessageType.JOIN && msg.getSource().getNick().equals(irc.nick()) ) {
 				ChatWindow win = new ChannelWindow( msg.getTarget().getChannel(), sync );
				win.addActionListener(commandListener);				
				add(win);
 			}
 
 			status.put( msg.getRaw() );	
 
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
 
 			if ( cmd.length() < 1 ) return;
 
 			if ( cmd.charAt(0) == '/' ) {
 				handleCommand(src, cmd);
 
 			//if it's in a status window and it doesn't start with a /, do nothing
 			} else if ( src.getType() == ChatWindow.Type.STATUS ) {
 					
 			//otherwise, it is in some form of chat window, so send a message...
 			} else {
 				irc.msg( src.getName() , cmd );
 				src.put( "<" + irc.nick() + "> " + cmd );
 			}
 		}
 	};
 }
