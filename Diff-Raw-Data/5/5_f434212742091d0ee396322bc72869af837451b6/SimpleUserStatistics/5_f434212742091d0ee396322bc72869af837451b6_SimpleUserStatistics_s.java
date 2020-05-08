 package org.BB.interactive;
 
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.Set;
 import java.util.concurrent.ConcurrentHashMap;
 
 import org.cometd.bayeux.Message;
 import org.cometd.bayeux.server.BayeuxServer;
 import org.cometd.bayeux.server.ServerSession;
 import org.cometd.server.AbstractService;
 import org.eclipse.jetty.util.ConcurrentHashSet;
 
 public class SimpleUserStatistics extends AbstractService {
 
 	long time;
 	Map<String,Integer> messages;
 	Map<String, Set<ServerSession>> pages;
 	Map<ServerSession, String> users;
 	final long TIME_SPAN = 3*60000;
 	
     public SimpleUserStatistics(BayeuxServer bayeux)
 	{
         super(bayeux, "statistics-service");
         addService("/**", "all");
         time = System.currentTimeMillis();
         messages = new HashMap<String, Integer>();
         pages = new ConcurrentHashMap<String, Set<ServerSession>>();
         users = new ConcurrentHashMap<ServerSession, String>();
 	}
     
     public void all(ServerSession remote, Message message) {
         if (message != null) {
         	
         	if (messages.containsKey(message.getChannel())) {
         		messages.put(message.getChannel(), messages.get(message.getChannel())+1);
         	} else {
         		messages.put(message.getChannel(), 1);
         	}
 
    		    System.err.println(message.getJSON());
 
         	Object pageObj = message.get("page");
     		if (pageObj instanceof String) {
     			String page = (String)pageObj;
     			Set<ServerSession> sessions = null;
    			if (pages.containsKey(page)) {
    				sessions = pages.get(page);
    			} else {
     				sessions = new ConcurrentHashSet<ServerSession>();			
     			}
     			sessions.add(remote);
     			pages.put(page, sessions);
 
         		if (users.containsKey(remote) && 
         				users.get(remote).compareTo(page) != 0) {
         			
                 	Set<ServerSession> pages_sessions = pages.get(users.get(remote));
                 	pages_sessions.remove(remote);
             		if (pages_sessions.size() == 0) {
             			pages.remove(users.get(remote));
             		}
 
             		
      			}
 
     			users.put(remote, page);
         	}
         }
         
         update();
     }
     
     public void update() {
         if (System.currentTimeMillis() - time > TIME_SPAN) {
             int connected = 0;
             for(ServerSession s : getBayeux().getSessions()) {
             	if (!s.isLocalSession()) {
             		connected++;
             	}
             }
             
             // Print statistics header
             System.err.println();
             for(Entry<String, Integer> e : messages.entrySet()) {
             	System.err.print(e.getKey() + ":" + e.getValue() + " ");
             }
             // print timestamp
             System.err.println("in timespan:" + String.valueOf(System.currentTimeMillis() - time));
             
             // Print number of users.
         	System.err.println("Users:" + connected);
         	
         	// Print users locations.
         	for(Entry<String, Set<ServerSession>> e : pages.entrySet()) {
         		System.err.println(e.getKey() + ":" + e.getValue().size());
         	}
 
         	HashSet<ServerSession> liveSessions = 
         		new HashSet<ServerSession>(getBayeux().getSessions());
         	HashSet<ServerSession> toRemove = new HashSet<ServerSession>();
         	
             for(ServerSession s : users.keySet()) {
             	if (!liveSessions.contains(s)) {
             		toRemove.add(s);
             	}
             }
             for(ServerSession s : toRemove) {
             	Set<ServerSession> sessions = pages.get(users.get(s));
         		sessions.remove(s);
         		if (sessions.size() == 0) {
         			pages.remove(users.get(s));
         		}
         		users.remove(s);
             }
 
             messages = new HashMap<String, Integer>();
             time = System.currentTimeMillis();
         }
     }
 }
