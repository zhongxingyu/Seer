 package com.thesmartpuzzle.ircbot;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Scanner;
 
 import jerklib.ConnectionManager;
 import jerklib.ProfileImpl;
 import jerklib.Session;
 import jerklib.events.ChannelMsgEvent;
 import jerklib.events.IRCEvent;
 import jerklib.events.JoinCompleteEvent;
 import jerklib.events.NumericErrorEvent;
 import jerklib.events.QuitEvent;
 import jerklib.events.InviteEvent;
 import jerklib.events.IRCEvent.Type;
 import jerklib.events.listeners.IRCEventListener;
 
 /**
  * @author Tommaso Soru <mommi84 at gmail dot com>
  *
  */
 public class Bot implements IRCEventListener {
 	private ConnectionManager manager;
 	final private Session session;
 	private Map<Type, IrcRunnable> stratMap = new HashMap<Type, IrcRunnable>();
 	
 	private HashMap<String, ArrayList<Answer>> index = new HashMap<>();
 	
 	// length of "[00:00:00] "
 	private int prefixsize = 11;
 	
 	private String channel, bible, name;
 
 	/**
 	 * A simple example that demonstrates how to use JerkLib
 	 */
 	public Bot(Configuration config) {
 		
 		channel = config.getChannel();
 		bible = config.getBible();
 		name = config.getName();
 
 		indexBible();
 		initStratMap();
 
 		/*
 		 * ConnectionManager takes a Profile to use for new connections. The
 		 * profile will contain the users real name, nick, alt. nick 1 and
 		 * alt. nick 2
 		 */
 		manager = new ConnectionManager(new ProfileImpl(config.getIdent(),
 				config.getNick(), config.getNick1(), config.getNick2()));
 
 		/*
 		 * One instance of ConnectionManager can connect to many IRC networks.
 		 * ConnectionManager#requestConnection(String) will return a Session
 		 * object. The Session is the main way users will interact with this
 		 * library and IRC networks
 		 */
 		session = manager.requestConnection("irc.freenode.net");
 
 		/*
 		 * JerkLib fires IRCEvents to notify users of the lib of incoming events
 		 * from a connected IRC server.
 		 */
 		session.addIRCEventListener(this);
 
 		/*
 		 * Tells JerkLib to rejoin any channel kicked from
 		 */
 		session.setRejoinOnKick(true);
 
 		/*
 		 * Tells jerklib to rejoin any channels previously joined to in event of
 		 * a reconnect.
 		 */
 		session.setRejoinOnReconnect(true);
 
 		/*
 		 * Gives JerkLib a chance to gracefully exit in event of a kill signal.
 		 */
 		Runtime.getRuntime().addShutdownHook(new Thread() {
 			@Override
 			public void run() {
 				manager.quit();
 			}
 		});
 
 	}
 
 	private void indexBible() {
 		Scanner in = null;
 		try {
 			in = new Scanner(new File(bible));
 		} catch (FileNotFoundException e) {
 			System.err.println("Bible not found.");
 			return;
 		}
 		
 		int l = name.length();
 		while(in.hasNextLine()) {
 			String s = in.nextLine();
 			if(s.length() > prefixsize+1+l) {
 				if(s.substring(prefixsize, prefixsize+l).equals(name)) {
 					String phrase = s.substring(prefixsize+2+l);
 					String[] words = StringUtilities.normalize(phrase).split(" ");
 					for(String word : words) {
 						ArrayList<Answer> w = index.get(word);
 						if(w == null) {
 							ArrayList<Answer> ans = new ArrayList<>();
 							ans.add(new Answer(phrase));
 							index.put(word, ans);
 						} else {
 							Answer a = new Answer(phrase);
 							if(!w.contains(a))
 								w.add(a);
 						}
 					}
 				}
 			}
 		}
 		in.close();
 	}
 
 	/*
 	 * This method is for implementing IRCEventListener. This method will be
 	 * called anytime Jerklib parses and event from an IRC server
 	 */
 	public void recieveEvent(IRCEvent e) {
 		// using a strategy pattern to handle the events
 		// http://en.wikipedia.org/wiki/Strategy_pattern
 		IrcRunnable r = stratMap.get(e.getType());
 		if (r != null) {
 			r.run(e);
 		} else {
 			System.out.println(e.getRawEventData());
 		}
 	}
 
 	private void initStratMap() {
 
 		stratMap.put(Type.INVITE_EVENT, new IrcRunnable() {
 			@Override
 			public void run(IRCEvent e) {
 				/* invited to a channel */
 				InviteEvent event = (InviteEvent) e;
 				System.out.print(event.getNick() + "!" + event.getUserName()
 						+ "@");
 				System.out.print(event.getHostName() + " invited us to "
 						+ event.getChannelName() + "\n");
 			}
 		});
 
 		stratMap.put(Type.CHANNEL_MESSAGE, new IrcRunnable() {
 			@Override
 			public void run(IRCEvent e) {
 				/* someone speaks in a channel */
 				ChannelMsgEvent cme = (ChannelMsgEvent) e;
 				System.out.println("<" + cme.getNick() + ">" + cme.getMessage());
 			}
 		});
 
 		stratMap.put(Type.DEFAULT, new IrcRunnable() {
 			@Override
 			public void run(IRCEvent e) {
 				/* raw data is the raw text message received from an IRC server */
 				System.out.println(e.getRawEventData());
 			}
 		});
 
 		stratMap.put(Type.READY_TO_JOIN, new IrcRunnable() {
 			@Override
 			public void run(IRCEvent e) {
 				/* join a channel */
 				e.getSession().joinChannel(channel);
 			}
 		});
 
 		stratMap.put(Type.JOIN_COMPLETE, new IrcRunnable() {
 			@Override
 			public void run(IRCEvent e) {
 				JoinCompleteEvent jce = (JoinCompleteEvent) e;
 				if (jce.getChannel().getName().equals(channel)) {
					/* say hello and version number */
 					jce.getChannel().say("hi!");
 				}
 			}
 		});
 
 		stratMap.put(Type.ERROR, new IrcRunnable() {
 			@Override
 			public void run(IRCEvent e) {
 				/* some error occured */
 				NumericErrorEvent ne = (NumericErrorEvent) e;
 				System.out.println(ne.getErrorType() + " " + ne.getNumeric()
 						+ " " + ne.getErrorMsg());
 			}
 		});
 
 		stratMap.put(Type.QUIT, new IrcRunnable() {
 			@Override
 			public void run(IRCEvent e) {
 				/* someone quit */
 				QuitEvent qe = (QuitEvent) e;
 				System.out.println("User quit:" + qe.getWho() + ":"
 						+ qe.getQuitMessage());
 			}
 		});
 		
 
 		stratMap.put(Type.CHANNEL_MESSAGE, new IrcRunnable() {
 			@Override
 			public void run(IRCEvent e) {
 				ChannelMsgEvent cme = (ChannelMsgEvent) e;
 				if (cme.getChannel().getName().equals(channel)) {
 					String inputmsg = cme.getMessage();
 					if(!cme.getNick().equals(session.getNick())) {
 						ArrayList<Answer> ans = index.get(inputmsg);
 						if(ans != null)
 							cme.getChannel().say(ans.get((int) (ans.size()*Math.random())).getMessage());
 					}
 				}
 			}
 		});
 		
 
 	}
 
 	private interface IrcRunnable {
 		public void run(IRCEvent e);
 	}
 
 }
