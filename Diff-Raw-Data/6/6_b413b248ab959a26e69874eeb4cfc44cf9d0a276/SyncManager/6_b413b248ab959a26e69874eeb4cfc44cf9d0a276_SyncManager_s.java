 
 package client;
 
 import irc.*;
 
 import java.util.List;
 import java.util.StringTokenizer;
 
 public class SyncManager implements MessageHandler {
 
 	Connection irc;
 
 	List<User> users;
 	List<Channel> channels;
 
 	public SyncManager( Connection irc ) {
 		this.irc = irc;
 
 		channels = new util.LinkedList<Channel>();
 		users = new util.LinkedList<User>();
 
 		irc.addMessageHandler(this)
 			.addType( MessageType.NICKCHANGE )
 			.addType( MessageType.JOIN )
 			.addType( MessageType.QUIT )
 			.addType( MessageType.PART )
 			.addType( MessageType.NAME )
 	//		.addType( MessageType.WHO )
 		;
 	}
 
 	public void handle( Message m ) {
 
 		System.out.println(m.getRaw());
 
 		switch ( m.getType() ) {
 
 			case JOIN:
 				handleJoin(m);
 				//print( m.getSource() + " JOINS " + m.getTarget() );
 				break;
 
 			case NAME:
 				handleNames(m);
 				break;
 
 			case NICKCHANGE:
 				print( m.getSource() +" CHANGES NICK TO "+ m.getTarget());
 				break;
 
 			case QUIT:
 				print(m.getSource() + " QUITS ("+m.getMessage()+")");
 				break;
 
 			case PART:
 				print(m.getSource() + " PARTS " + m.getTarget() + " (" + m.getMessage() + ")" );
 				break;
 
 			default:
 				print("????");
 
 		}
 
 	}
 
 	private void handleNames(Message m) {
 		Channel c = getChannel( m.getArg(3) );
 
 		StringTokenizer st = new StringTokenizer( m.getMessage(), " ");
 
 		List<User> names = new util.LinkedList<User>();
 
 		User u;
 		String nick;
 		while ( st.hasMoreTokens() ) {
 			nick = st.nextToken();
 
 			switch (nick.charAt(0)) {
 				case '~':
 				case '&':
 				case '@':
 				case '%':
 				case '+':
 					nick = nick.substring(1);
 			}
 
 			u = getUser( nick );
 			u.addChannel(c);
 			names.add(u);
 		}
 
 		c.addUsers(names);
 //:fubar m donmorrison picardo &RomanII Father_MacklePenny- Clipper Panda DocHoliday Dreamhunter Myrddin Someone Tiff SpikeSpiegel Tish Lil-Wayne TeQ|Bed Shaggs palladino_ RD Mr_Trapani crab Whiplash @Kittie Shaw FCUK Marston ghostdog Gaara Tanda[A] Jasmine Falcon Trixie AWillAY Bulldog JewishJake +tita Genesis DeadlySin Weedall Sparkles|away @Canucklehead &anubis rofldog Gue
 	}
 
 	private void handleJoin(Message m) {
 		print( m.getSource() + " JOINS " + m.getTarget() );
 
 		Channel c = getChannel(m.getTarget().getChannel());
 			//_getChannel( m.getTarget().getChannel() );
 
 		//no record of the channel.
 /*		if (c == null) {
 
 			//the join is not ME joining
 			if ( ! m.getSource().getNick().equals( irc.nick() ) ) {
 				System.out.println(m.getSource().getNick() + " != " + irc.nick());
 					//Which is pretty odd, so we'll ignore it..
 					return;
 			}
 				c = new Channel( m.getTarget().getChannel() );
 		}*/
 
 			getUserFromTarget( m.getSource() ).join(c);
 			
 	}
 
 	public User getUser(String nick) {
 		User user = null;
 
 		for (User u : users)
 			if ( u.getNick().equals(nick) ) {
 				user = u;
 				break;
 			}
  
 		if ( user == null ) {
 			user = new User(nick);
 		}
 
 		return user;
 	}
 
 	private User getUserFromTarget(MessageTarget tg) {
 		User user = null;
 
 		String nick = tg.getNick();
 		String host = "", ident = "";
 
 		if ( tg.scope( MessageTarget.Scope.USER ) ) {
 			host = tg.getHost();
 			ident = tg.getUser();
 		}
 			
 
 		for (User u : users)
 			if ( u.getNick().equals(nick) ) {
 				user = u;
 				break;
 			}
 			
 		if ( user == null ) {
 			user = new User(nick,ident,host);
 		} else {
 			user.setUser(ident);
 			user.setHost(host);
 		}
 	
 		return user;
 	}
 
 	public Channel getChannel(String name) {
 		Channel c;
 
 		if ( (c = _getChannel(name)) != null )
 			return c;
 
 		else {
 			c = new Channel(name);
 			channels.add(c);
 			return c;
 		}
 	}
 
 
 	private Channel _getChannel(String name) {
 		for (Channel c : channels) {
 			if ( c.getName().equals(name) )
 				return c;
 		}
 
 		return null;
 	}
 
 	private void addChannel(Channel c) {
 		if (channels == null) 
 			channels = new util.LinkedList<Channel>();
 
 		channels.add(c);
 	}
 
 	public void print(String ln) {
 		System.out.println(ln);
 	}
 }
