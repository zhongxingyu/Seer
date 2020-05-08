 package com.coldsteelstudios.irc;
 
 import java.net.Socket;
 import java.io.PrintWriter;
 import java.io.BufferedReader;
 import java.io.InputStreamReader;
 
 //import java.util.concurrent.PriorityBlockingQueue;
 //import java.util.concurrent.BlockingQueue;
import com.coldsteelstudios.util.PriorityBlockingQueue;
import com.coldsteelstudios.util.BlockingQueue;
 
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 
 import java.util.Set;
 import java.util.TreeSet;
 import java.util.regex.Pattern;
 
 /**
  * Connects to, registers with an IRC server and dispatches events.
  *
  *
  * @TODO need to make the exception handling better
  * This might mean a way to register exception handlers or something, 
  * as currently most of the exceptions will occur in different threads.
  */
 public class Connection {
 
 	/**
 	 * State of the connection
 	 */
 	public static enum State {
 		/**
 		 * Completely not connected
 		 */
 		DISCONNECTED(0),
 
 		/**
 		 * TCP Connection complete, registration phase incomplete.
 		 */
 		CONNECTED(1),
 
 		/**
 		 * Connection is usable.
 		 */
 		REGISTERED(2);
 
 		private int code;
 
 		State(int code) {
 			this.code = code;
 		}
 	}
 
 	/**
 	 * Minimm time in between pings. (milliseconds)
 	 * If the connection is dropped, a ping will be sent to the server once
 	 * every PING_TIMEOUT milliseconds.
 	 */
 	private static final int PING_TIMEOUT = 30000;
 
 	/**
 	 * Maximum idle time of the connection.
 	 *
 	 * If there is no activity fromt he server within this time, the
 	 * connection will be closed.  The server will be pinged if it there is no
 	 * activity for half this time. (see PING_TIMEOUT)
 	 */
 	private static final int MAX_IDLE = 1000*60*3;
 
 
 	/**
 	 * Server for the IRC connection
 	 */
 	private String host;
 
 	/**
 	 * Port of the IRCd on host.
 	 */
 	private int port;
 
 	/**
 	 * Current nickname of the registered connection.
 	 */
 	private String nick;
 	
 	/**
 	 * The user/ident.
 	 * @TODO get this from the server? The sever doesn't necessarily accept what we give it (identd, ~)
 	 */
 	private String user;
 	
 	/**
 	 * The realname the connection was registered with.
 	 */
 	
 	private String real;
 	
 	/**
 	 * Password to use when registering.
 	 */
 	private String pass = null;
 
 	/**
 	 * Priority Queue of outgoing messages.
 	 */
 	private BlockingQueue<OutgoingMessage> sendQ;
 	
 	/**
 	 * Priority Queue of Incoming messages.
 	 */
 	private BlockingQueue<Message> recvQ;
 
 	/**
 	 * The object that handles the actual socket i/o
 	 */
 	private IrcConnection conn;
 
 	/**
 	 * Subscribed message handlers
 	 */
 	private List<IrcMessageSubscription> handlers;
 	
 	/**
 	 * Tracks the state of the connection.
 	 * @see Connection.State
 	 */
 	private State state;
 
 	/**
 	 * The hostname the server gave as the origin in the 001 reply.
 	 */
 	private String hostname = "none";
 
 	/**
 	 * The time (unix epoch) in milliseconds of the last message sent to the server.
 	 */
 	private long last_tx = 0;
 
 	/**
 	 * The time (unix epoch) in milliseconds of the last message received from to the server.
 	 */
 
 	private long last_rx = 0;
 
 	/**
 	 * The time (unix epoch) in milliseconds of the last ping sent to the server.
 	 */
 	private long last_ping = 0;
 
 	/**
 	 * Create a Connection with a default ident and realname.
 	 *
 	 * @param host the hostname/Ip of the server.
 	 * @param port the tcp port
 	 * @param nick the nickname, realname, ident to use (copied to all 3)
 	 */
 	public Connection(String host, int port, String nick) {
 		this(host,port,nick,nick);
 	}
 
 	/**
 	 * Create a Connection with a nickname and username, but default realname
 	 *
 	 * @param host the hostname/Ip of the server.
 	 * @param port the tcp port
 	 * @param nick the nickname, realname, to use
 	 * @param user ident/user to use.
 	 */
 	public Connection(String host, int port, String nick, String user) {
 		this(host,port,nick,user,nick);
 	}
 
 	/**
 	 * Create a Connection with a nickname and username, but default realname
 	 *
 	 * @param host the hostname/Ip of the server.
 	 * @param port the tcp port
 	 * @param nick the nickname to use
 	 * @param user the ident/user to use.
 	 * @param real the realname to use.
 	 */
 	public Connection(String host, int port, String nick, String user, String real) {
 		this.user = user;
 		this.host = host;
 		this.port = port;
 		this.nick = nick;
 		this.user = user;
 		this.real = real;
 
 		state = State.DISCONNECTED;
 
 		//handlers = java.util.Collections.synchronizedList( new LinkedList<IrcMessageSubscription>());
 		handlers = new com.coldsteelstudios.util.LinkedList<IrcMessageSubscription>();
 
 	}
 	
 	/**
 	 * Set the password for the connection
 	 *
 	 * @param pass the password to use during registration
 	 */
 	public void setPass(String pass) {
 		this.pass = pass;
 	}
 
 	/**
 	 * Change the connection state: disconnected->connected->registered.
 	 */
 	private void setState(State ns) {
 		state = ns;
 
 		synchronized(this) {
 			notifyAll();
 		}
 	}
 
 	public State getState() {
 		return state;
 	}
 
 	public String getServerName() {
 		return hostname;
 	}
 
 	public Connection getConnection() {
 		return this;
 	}
 
 
 	/**
 	 * Attempt to connect to the given IRC server
 	 * with the given parameters
 	 *
 	 * @TODO wrap IOException
 	 */
 	public void connect() throws ConnectionException {
 
 		try {
 			//connect
 			//(blocks until connected)
 			conn = new IrcConnection();
 		} catch (java.io.IOException e) {
 			throw new ConnectionException(e);
 		}
 
 		if ( state != State.CONNECTED )
 			throw new ConnectionException("Connection is not connected!");
 
 
 		//handle messages in a separate thread
 		//so the socket I/O never pauses
 		//(This can be important in the case of a flood of messages which require
 		//some potentially long, synchonous operation to handle. In this case,
 		//a PING could be left unread which would cause a pint timeout.
 		//)
 		(new Thread( new Worker(), "Message Handler" )).start();
 	
 		addMessageHandler(this.internalHandler)
 			.addType( MessageType.PING )
 			.addType( MessageType.ERROR )
 			.addType( MessageType.NICKCHANGE )
 			.or()
 			.addCode( MessageCode.RPL_WELCOME )
 			.addCommand( "ERROR" )	
 		;
 
 		//initiatite registration
 		register();
 
 		try {
 			/**
 			 * Block for a state change, up to 30 seconds.
 			 */
 			synchronized(this) {
 				this.wait(30000); 
 			}
 
 		} catch (InterruptedException e) {
 		}
 
 		/**
 		 * 	Either state changed, or 30 seconds passed,
 		 * 	so if the connection isn't registered, something went wrong.
 		 */
 	
 		if ( state != state.REGISTERED )
 			throw new ConnectionException("REGISTER timeout after 30 seconds");
 
 		//the connection is registered, so the client is now in a usable state and can execute commands.
 	}
 
 	//initiate registration
 	private void register() {
 		//All the commands in this method must go through sendRaw.
 
 		//command order per RFC2812
 
 		if ( pass != null )
 			send("PASS",pass);
 		
 
 		send("NICK", nick);
 		send("USER" ,user, "0", "*", real);
 	}
 
 	private void ping() {
 
 		if ( System.currentTimeMillis() - last_ping <= PING_TIMEOUT )
 			return;
 
 		last_ping = System.currentTimeMillis();
 			
 		send(Priority.CRITICAL, "PING", hostname);
 	}
 
 	public void nick(String nick) {
 		nick(nick, Priority.MEDIUM);
 	}
 
 	/**
 	 * Issue the nick command...
 	 */
 	public void nick(String nick, Priority p) {
 		//send the nick command...	
 		send(p, "NICK", nick);
 
 		this.nick = nick;
 
 		//@TODO monitor for failed nick changes.
 		//only set the nick on a successful reply..
 	}
 
 
 	/**
 	 * The following are helper methods for IRC commands.
 	 * Please read the relevant RFC for documentation. 
 	 * DOcumenting IRC commands is beyind the scope of these comments.
 	 * Kthanxbai.
 	 */
 
 	public void part(String chan, String msg) {
 		if (msg != null)
 			send("PART", chan, msg);
 		else 
 			send("PART", chan);
 	}
 
 	public void part(String chan) {
 		part(chan,null);
 	}
 
 	public void join(String ... chans) {
 
 		if (chans.length == 0)
 			throw new IllegalArgumentException("Must pass a channel to join!");
 
 		StringBuilder buf = new StringBuilder(chans[0]);	
 
 		for ( int i = 1; i < chans.length; buf.append(',').append(chans[i]), ++i);
 
 		send("JOIN",buf.toString());
 	}
 
 	public void msg(String target, String msg) {
 		msg(target, msg, Priority.MEDIUM);
 	}
 
 
 	public void msg(String target, String msg, Priority p) {
 		send(p, "PRIVMSG",target, msg);
 	}
 
 	public void action(String target, String msg) {
 		action(target,msg,Priority.MEDIUM);
 	}
 
 	public void action(String target, String msg, Priority p) {
 		ctcp(target, "ACTION", msg, p);
 	}
 
 	public void ctcp(String target, String command,String msg, Priority p) {
 		msg(target,"\u0001" + command + " " + msg + "\u0001",p);
 	}
 
 	public void ctcp(String target, String command, String msg) {
 		ctcp(target,command,msg,Priority.MEDIUM);
 	}
 
 	public void notice(String target, String msg) {
 		notice(target, msg, Priority.MEDIUM);
 	}
 
 	public void notice(String target, String msg, Priority p) {
 		send(p,"NOTICE" ,target, msg);
 	}
 
 	public void kick(String chan, String user, String msg) {
 		send(Priority.MEDIUM,"KICK", chan, user, msg);
 	}
 
 	public void kick(String chan, String user, String msg, Priority p) {
 		send(p,"KICK", chan, user, msg);
 
 	}
 
 	public String nick() {
 		return this.nick;
 	}
 
 	public void quit() {
 		quit("Client exited.");
 	}
 
 	public void quit(String msg) {
 		send(Priority.LOW, "QUIT", msg);
 	}
 
 	/**
 	 * End IRC helper methods.
 	 */
 
 	//handle a raw received message
 	private void handleRaw(String raw) {
 		if (raw.length() == 0) return;
 	
 		try {
 			recvQ.offer( MessageParser.parse( this, raw ) );
 
 		//probably best if incoming messages can't kill the client.
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 
 	}
 
 	/**
 	 * Below... about 50 varieties of send....
 	 */
 
 	public void send(String[] args, Priority p) {
 
 		if (args.length == 0) return;
 
 		StringBuilder buf = new StringBuilder(args[0]);	
 
 		for ( int i = 1; i < args.length; buf.append( (i == args.length - 1) ? " :" : ' ').append(args[i]), ++i);
 		
 		send(buf.toString(), p);
 	}
 
 	public void send(Priority p, String... args) {
 		send(args,p);
 	}
 
 	public void send(String ... args) {
 		send(args,Priority.MEDIUM);
 	}
 
 
 	public void send(String msg) {
 		send(msg, Priority.MEDIUM);
 	}
 
 	public void send(String msg, Priority p) {
 		sendRaw(msg,p);
 	}
 
 	private void sendRaw(String cmd) {
 		sendRaw(cmd, Priority.MEDIUM);
 	}
 
 	private void sendRaw(String cmd, Priority p) {
 
 		if ( state == state.DISCONNECTED ) 
 			//@TODO
 			throw new RuntimeException("Trying to execute commands in a disconnected state...?");
 
 		//@TODO error checking
 		//offer returns bool.
 		if (!sendQ.offer( new OutgoingMessage(cmd,p) ))
 			throw new RuntimeException("Failed to queue message: " + cmd);
 	}
 
 
 
 	/**
 	 * Registers a message handler (by default, to receive all messages).
 	 * Messages can be filtering by chaining filter methods onto 
 	 * the returned subscription.
 	 *
 	 * NOTE: The message subscriptions are backed by an ordered date structure.
 	 *       The MessageHandler guarantess that handlers are called in the order
 	 *       in which they were registered.
 	 *
 	 * @see IrcMessageSubscription
 	 * @return Provides a fluent interface...
 	 */
 	public IrcMessageSubscription addMessageHandler(MessageHandler handler) {
 		return (new IrcMessageSubscription(handler)).register(); 
 	}
 
 	//a subscription to irc messages
 	public class IrcMessageSubscription {
 			
 		private Set<MessageType> types = null;
 		private Set<MessageCode> codes = null;
 
 		private List<String> cmds = null;
 		private List<Pattern> patterns = null;
 
 		private MessageHandler handler;
 			
 		private IrcMessageSubscription(MessageHandler handler) {
 			this.handler = handler;
 		}
 
 		/**
 		 * Add a type to the subscription
 		 * @return provides a fluent interface
 		 */
 		public IrcMessageSubscription addType(MessageType type) {
 			
 			if  ( types == null ) 
 				types = new TreeSet<MessageType>();
 
 			types.add(type);
 
 			return this;
 		}
 
 		/**
 		 * Add a code to the subscription
 		 * @return provides a fluent interface
 		 */
 		public IrcMessageSubscription addCode(MessageCode code) {
 
 			if  ( codes == null ) 
 				codes = new TreeSet<MessageCode>();
 
 			codes.add(code);
 
 			return this;
 		}
 
 		/**
 		 * Add a command. THe command and the code are an OR match. Everything else is and.
 		 * 
 		 * @return provides a fluent interface.
 		 */
 		public IrcMessageSubscription addCommand(String cmd) {
 			
 			if ( cmds == null ) 
 				cmds = new LinkedList<String>();
 
 			cmds.add(cmd);
 
 			return this;
 		}
 
 		/**
 		 * Add a regex to match on the 'message' part.
 		 */
 		public IrcMessageSubscription addPattern(Pattern p) {
 			
 			if ( patterns == null )
 				patterns = new LinkedList<Pattern>();
 
 			patterns.add(p);
 
 			return this;
 		}
 
 		/**
 		 * Register this subscription
 		 */
 		private IrcMessageSubscription register() {
 			synchronized(handlers) {
 				handlers.add(this);
 			}
 			return this;
 		}
 
 		/**
 		 * kill this subscription
 		 */
 		public void unregister() {
 			synchronized(handlers) {
 				handlers.remove(this);
 			}
 		}
 
 		/**
 		 * add an "or" condition. Really just creates a new subscription.
 		 */
 		public IrcMessageSubscription or() {
 			return (new IrcMessageSubscription(this.handler)).register();
 		}
 
 		//tests if this subscription matches, calls the handlers handle if it does.
 		private void handle(Message msg) {
 
 			//Type must ALWAYS match...
 			//msg.getType() should NEVER return null.
 			if ( this.types != null && !types.contains( msg.getType() ) )
 				return;
 
 			//code or pattern must match...
 			boolean codeMatches = true,
 					cmdMatches = true;
 
 			if ( this.codes != null && (msg.getCode() == null || !codes.contains( msg.getCode() )) )
 				codeMatches = false;
 
 		
 			//if the code DOESN'T match and there are commands to match.
 			if ( !codeMatches && this.cmds != null ) {
 				cmdMatches = false;
 
 				for (String cmd : cmds) {
 					if ( cmd.equals(msg.getCommand()) ) {
 						cmdMatches = true;
 						break;
 					}
 				}
 			}
 
 			//if we didn't find a command or code match...
 			if ( !(cmdMatches || codeMatches) ) 
 				return;
 
 			//Patterns must always match....
 			if ( this.patterns != null ) {
 				boolean found = false;
 
 				for (Pattern p : patterns) {
 					if ( p.matcher( msg.getMessage() ).matches() ) {
 						found = true;
 						break;
 					}
 				}
 
 				if (!found) return;
 			}
 
 			//if we haven't returne by this point, the message must be a match...
 			//@TODO consider creating one event each time a handler matches? 
 			//this is a bit redundant.
 			handler.handle(new MessageEvent(getConnection(), this, msg));
 		}
 	}
 	
 	//handler for some internal stuff.
 	private MessageHandler internalHandler = new MessageHandler() {
 
 
 		public void handle(MessageEvent e) {
 
 			Message msg = e.getMessage();
 
 			if ( msg.getCode() == MessageCode.RPL_WELCOME ) {
 				setState(State.REGISTERED);
 
 				hostname = msg.getSource().getHost();
 
 			} else if ( msg.getType() == MessageType.PING ) {
 				send( Priority.CRITICAL, "PONG", msg.getMessage() );
 
 			} else if ( msg.getCommand().equals("ERROR") ) {
 				conn.close();
 
 			//@TODO huge mess
 			//	-handle erroneous nickname by choosing a random one.
 			//	-nick collision won't happen during registration.
 			//	-handle collision the same as in use.
 			} else if ( state == State.CONNECTED &&
 					(
 					 /*msg.getCode() == MessageCode.ERR_ERRONEUSNICKNAME ||*/
 						msg.getCode() == MessageCode.ERR_NICKNAMEINUSE ||
 						msg.getCode() == MessageCode.ERR_NICKCOLLISION 
 					)
 			) {
 				//I'm ASSUMING that all of these replies have the same format.
 				//server 433 * nick :reason
 				String bad = msg.getArg(2);
 
 				//if I'm right about all of this, it should keep tacking on a _ until it
 				//doesn't get a bad response...
 				nick(bad+"_", Priority.HIGH);
 
 				//connected, but not registered...
 				//In this case (99% sure), there will only be a 433, and no nick reply confirming the change.
 				//(GOD irc is annoying)
 				nick = bad+ "_";
 
 			//nick reply indicating that I changed my nick...
 			} else if ( msg.getType() == MessageType.NICKCHANGE && msg.getSource().getNick().equals(nick) ) {
 				nick = msg.getTarget().getNick();
 			}
 		}
 	};
 
 	//message handler thread
 	private class Worker implements Runnable {
 
 		private Worker() {}
 
 		public void run() {
 
 			while( state != state.DISCONNECTED ) {
 
 				Message msg = null;
 
 				try {
 					msg = recvQ.poll(PING_TIMEOUT, java.util.concurrent.TimeUnit.SECONDS);
 				} catch (InterruptedException e) {
 					e.printStackTrace();
 				}
 
 				//nothing to do
 				if (msg == null) continue;
 
 				Iterator<IrcMessageSubscription> it = handlers.iterator();
 					
 				while (it.hasNext()) try {
 					it.next().handle( msg );
 				} catch (Exception e) {
 					//@TODO
 					e.printStackTrace();
 				}
 
 				//ping timeout
 				if ( System.currentTimeMillis() - last_rx > MAX_IDLE ) { 
 					System.err.println("PING TIMEOUT");
 					conn.close();
 				}
 				
 				//active keep-alives.
 				else if ( ((System.currentTimeMillis() - last_tx > MAX_IDLE/2) || (System.currentTimeMillis() - last_rx) > MAX_IDLE/2) ) 
 					ping();
 			}
 		}
 	}
 
 	private static class OutgoingMessage implements Comparable<OutgoingMessage> {
 		
 		Priority priority;
 		String msg;
 
 		private OutgoingMessage(String msg) {
 			this(msg,Priority.MEDIUM);
 		}
 
 		private OutgoingMessage(String msg, Priority priority) {
 			this.priority = priority;
 			this.msg = msg;
 		}
 
 		public String getMessage() {
 			return msg;
 		}
 
 		public int compareTo( OutgoingMessage msg ) {
 			return this.priority.getValue() - msg.priority.getValue();
 		}
 	}
 	//connection thread
 	private class IrcConnection {
 
 		//per RFC, max size of irc message...
 		private static final int MSG_SIZE = 512;
 
 		private Socket conn = null;
 
 		private PrintWriter out;
 
 		private BufferedReader in;
 
 		private IrcConnection() throws java.io.IOException {
 
 			conn = new java.net.Socket(host,port);
 
 			out = new PrintWriter( conn.getOutputStream(), false );
 			in = new BufferedReader( new InputStreamReader( conn.getInputStream() ) );
 
 			last_tx = last_rx = System.currentTimeMillis();
 
 			//will replace these with own implementation of queue/prioirty queue later.
 			sendQ = new PriorityBlockingQueue<OutgoingMessage>();
 			recvQ = new PriorityBlockingQueue<Message>();
 
 			//setting state = connected starts all the fun loops..
 			setState(state.CONNECTED);
 		
 			(new Thread( readerThread, "Socket Read" )).start();
 			(new Thread( writerThread, "Socket Write" )).start();
 		}
 
 		private Runnable readerThread = new Runnable() {
 
 			/**
 			 * @TODO exception handling
 			 */
 			public void run() {
 			
 				if ( state == State.DISCONNECTED ) 
 					throw new ConnectionStateException("Trying to run wihout being connected???");
 	
 				while ( state != State.DISCONNECTED ) try {	
 					
 					/**
 					 * NOTE: this *should* work.  
 					 * Not 100% sure in the case of CTCPS
 					 * I believe CR/LF characters must be quoted in in a CTCP (by the server, presumably),
 					 * but in any case, this should be changed to a character-by-character read for two reasons:
 					 *
 					 * (1) enforce the MSG_SIZE
 					 * (2) An unquoted embedded CR\LF inside a CTCP would cause the message to be split into two
 					 * 	thus allowing message injection. Probably not a good idea to rely on the server to do the quoting...
 					 */
 					recv( in.readLine() );
 		
 				} catch (java.io.IOException e) {
 					e.printStackTrace();
 					close();
 				}
 			}
 		};
 
 		private Runnable writerThread = new Runnable() {
 
 			/**
 			 * @TODO exception handling
 			 */
 			public void run() {
 			
 				if ( state == State.DISCONNECTED ) 
 					throw new ConnectionStateException("Trying to run wihout being connected???");
 
 	
 				while( state != State.DISCONNECTED ) try {	
 
 					sendMsg( sendQ.poll(10, java.util.concurrent.TimeUnit.SECONDS) );
 		
 				} catch (InterruptedException e) {
 				}
 			}
 		};
 
 		private void sendMsg(OutgoingMessage msg) {
 
 			if (msg == null) return;
 
 			//connection could have died while waiting for something
 			//to be put onto the outgoing queue.
 			if (state == State.DISCONNECTED) return;
 
 			last_tx = System.currentTimeMillis();
 
 			try { 
 				out.print( msg.getMessage() );
 				out.print( "\r\n" );
 				out.flush();
 			} catch (Exception e) {
 //				e.printStackTrace();
 				close();
 			}
 		}
 
 		/**
 		 * Receives/buffers any data on the channel
 		 */
 		private void recv(String msg) {
 
 			if (msg == null) return;
 
 			//in case the connection died/was killed while waiting to read 
 			//(which doesn't make sense...)
 			if (state == State.DISCONNECTED) return;
 
 			last_rx = System.currentTimeMillis();
 
 			handleRaw(msg);
 		}
 
 		public void close() {
 			//stop everything from looping
 			setState(State.DISCONNECTED);
 			
 			sendQ.clear();
 
 			try {
 				conn.close();
 			} catch (Exception e) {
 				//not really anyhing we CAN do...
 			}
 		}
 
 		protected void finalize() {
 			close();
 		}
 	}
 }
