 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package Markov2;
 
 import java.util.List;
 import java.util.Random;
 import java.util.concurrent.ConcurrentHashMap;
 import java.util.concurrent.LinkedBlockingQueue;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import com.db4o.Db4o;
 import com.db4o.ObjectContainer;
 import com.db4o.ObjectSet;
 import com.db4o.query.Predicate;
 
 /**
  * 
  * @author ajc39
  */
 public class MarkovDatabase implements Runnable {
 
 	private ObjectContainer database;
 	private boolean shuttingDown = false;
 	private static final int MAX_SENTANCE_LENGTH = 30;
 
 	// private final ConcurrentHashMap<String, MarkovNode> cache = new
 	// ConcurrentHashMap<String, MarkovNode>();
 	// private final MarkovExplorer ex;
 
 	// private final ConcurrentLinkedQueue<MarkovNode> queue = new
 	// ConcurrentLinkedQueue<MarkovNode>();
 
 	public MarkovDatabase() {
 		// try {
 		// Logger.getLogger(MarkovDatabase.class.getName()).log(Level.INFO,
 		// "Defragging Database");
 		// Defragment.defrag("Markov2.db4o");
 		// Logger.getLogger(MarkovDatabase.class.getName()).log(Level.INFO,
 		// "Defrag Complete");
 		// } catch (IOException ex) {
 		// Logger.getLogger(MarkovDatabase.class.getName()).log(Level.SEVERE,
 		// null, ex);
 		// }
 
 		Db4o.configure().automaticShutDown(false);
 		// set up indexing
 		Db4o.configure().objectClass(MarkovNode.class).objectField("word")
 				.indexed(true);
 		Db4o.configure().objectClass(MarkovLink.class).objectField("from")
 				.indexed(true);
 		Db4o.configure().objectClass(MarkovLink.class).objectField("to")
 				.indexed(true);
 		// set it up to update the lists properly
 		// Db4o.configure().objectClass(MarkovNode.class).updateDepth(2);
 		// and activate the lists far enough
 		// Db4o.configure().objectClass(MarkovNode.class).minimumActivationDepth(2);
 		database = Db4o.openFile("Markov2.db4o");
 		populate();
 		// ex = new MarkovExplorer(cache);
 	}
 
 	public void populate() {
 		// busy = true;
 		Logger.getLogger(MarkovDatabase.class.getName()).log(Level.INFO,
 				"Loading data");
 		// get a list of all nodes
 		List<MarkovNode> set = database.query(new Predicate<MarkovNode>() {
 			@Override
 			public boolean match(MarkovNode node) {
 				return node.getWord() == "[";
 			}
 		});
 		if (set.size() == 0) {
 			MarkovNode start = new MarkovNode("[");
 			MarkovNode end = new MarkovNode("]");
 			database.set(start);
 			database.set(end);
			MarkovLink link = new MarkovLink(start, end);
			database.set(link);
 		} else {
 		}
 		Logger.getLogger(MarkovDatabase.class.getName()).log(Level.INFO,
 				"Loading done");
 		// busy = false;
 	}
 
 	public String Generate() {
 		Logger.getLogger(MarkovString.class.getName()).log(Level.INFO,
 				"Generating");
 		StringBuffer sb = new StringBuffer();
 		// get the beginning node
 		MarkovNode current = getNode("[");
 		// loop through until we hit the end
 		for (int i = 0; i < MAX_SENTANCE_LENGTH
 				&& !current.getWord().equals("]"); i++) {
 			// get a random next node
 			MarkovNode newNode = getRandomNode(current);
 			
 			if (newNode == null)
 				return "";
 			
 
 			current = newNode;
 			// append the word at the new nodes
 			sb.append(current.getWord());
 			// append a space
 			sb.append(" ");
 			Logger.getLogger(MarkovString.class.getName()).log(Level.INFO,
 					"sentance = " + sb);
 		}
 		// return the whole string
 		Logger.getLogger(MarkovString.class.getName()).log(Level.INFO,
 				"Generating End");
 		return sb.toString().replace("]", " ").trim();
 	}
 
 	public MarkovNode getRandomNode(MarkovNode current) {
 		ObjectSet<MarkovLink> links = database
 				.get(new MarkovLink(current, null));
 		if (links.size() == 0) {
 			return null;
 		} else {
 			return links.get(new Random().nextInt(links.size())).getTo();
 		}
 
 	}
 
 	public int[] getStats() {
 		int ret[] = { 0, 0 };
 		ObjectSet<MarkovNode> result = database.get(MarkovNode.class);
 
 		ret[0] = result.size();
 
 		return ret;
 	}
 
 	public void shutdown() {
 		shuttingDown = true;
 		unload();
 	}
 
 	public MarkovNode getNode(String word) {
 
 		ObjectSet<MarkovNode> query = database.get(new MarkovNode(word));
 		if (query.size() == 0) {
 			return null;
 		} else {
 			MarkovNode n = query.get(0);
 			return n;
 		}
 	}
 
 	public MarkovLink getLink(MarkovLink link) {
 
 		ObjectSet<MarkovLink> query = database.get(link);
 		if (query.size() == 0) {
 			return null;
 		} else {
 			MarkovLink n = query.get(0);
 			return n;
 		}
 	}
 
 	public void add(MarkovLink link) {
 		database.set(link);
 	}
 
 	public void add(MarkovNode node) {
 		database.set(node);
 	}
 
 	public void run() {
 
 	}
 
 	public void unload() {
 		database.close();
 	}
 }
