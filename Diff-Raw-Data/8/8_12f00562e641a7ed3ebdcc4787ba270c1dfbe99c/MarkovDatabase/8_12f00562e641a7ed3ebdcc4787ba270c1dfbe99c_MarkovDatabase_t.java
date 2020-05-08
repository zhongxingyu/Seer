 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package Markov2;
 
 import com.db4o.defragment.Defragment;
 import com.db4o.Db4o;
 import com.db4o.ObjectContainer;
 import com.db4o.ObjectSet;
 import java.util.concurrent.ConcurrentHashMap;
 import java.util.concurrent.LinkedBlockingQueue;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 /**
  *
  * @author ajc39
  */
 public class MarkovDatabase implements Runnable {
 
     private ObjectContainer database;
     private boolean shuttingDown = false;
     private LinkedBlockingQueue<MarkovNode> saveQueue;
     private static final int MAX_SENTANCE_LENGTH = 30;
 
     private final ConcurrentHashMap<String, MarkovNode> cache = new ConcurrentHashMap<String, MarkovNode>();
 //    private final ConcurrentLinkedQueue<MarkovNode> queue = new ConcurrentLinkedQueue<MarkovNode>();
 
     public MarkovDatabase(LinkedBlockingQueue<MarkovNode> saveQueue) {
 //        try {
 //            Logger.getLogger(MarkovDatabase.class.getName()).log(Level.INFO, "Defragging Database");
 //            Defragment.defrag("Markov2.db4o");
 //            Logger.getLogger(MarkovDatabase.class.getName()).log(Level.INFO, "Defrag Complete");
 //        } catch (IOException ex) {
 //            Logger.getLogger(MarkovDatabase.class.getName()).log(Level.SEVERE, null, ex);
 //        }
 
         this.saveQueue = saveQueue;
         Db4o.configure().automaticShutDown(false);
         // set up indexing
         Db4o.configure().objectClass(MarkovNode.class).objectField("word").indexed(true);
         // set it up to update the lists properly
         Db4o.configure().objectClass(MarkovNode.class).updateDepth(2);
         // and activate the lists far enough
         Db4o.configure().objectClass(MarkovNode.class).minimumActivationDepth(2);
         database = Db4o.openFile("Markov2.db4o");
         populate();
     }
 
     public void populate()
     {
         //busy = true;
         Logger.getLogger(MarkovDatabase.class.getName()).log(Level.INFO, "Loading data");
         // get a list of all nodes
         ObjectSet<MarkovNode> set = database.get(MarkovNode.class);
         // if we dont have any, we have an empty database and need to start
         // learning
         if (set.size() == 0)
         {
                 database.set(new MarkovNode("["));
                 database.set(new MarkovNode("]"));
         }
         else
         {
             for (MarkovNode n : set)
             {
                 cache.put(n.getWord(), n);
             }
         }
         Logger.getLogger(MarkovDatabase.class.getName()).log(Level.INFO, "Loading done");
         //busy = false;
     }
 
     public String Generate() {
         Logger.getLogger(MarkovString.class.getName()).log(Level.INFO, "Generating");
         StringBuffer sb = new StringBuffer();
         // get the beginning node
         MarkovNode current = getNode("[");
         // loop through until we hit the end
         for (int i = 0; i < MAX_SENTANCE_LENGTH && !current.getWord().equals("]"); i++) {
             // get a random next node
             MarkovNode newNode = current.GetRandomNode();
             // if its null we need to add a new join to the end
             if (newNode == null) {
                 MarkovNode end = getNode("]");
                 current.AddChild(end);
                 try {
                     saveQueue.put(current);
                 } catch (InterruptedException ex) {
                     Logger.getLogger(MarkovDatabase.class.getName()).log(Level.SEVERE, null, ex);
                 }
                 newNode = end;
             }
             current = newNode;
             // append the word at the new nodes
             sb.append(current.getWord());
             // append a space
             sb.append(" ");
         }
         // return the whole string
         Logger.getLogger(MarkovString.class.getName()).log(Level.INFO, "Generating End");
         return sb.toString().replace("]", " ").trim();
     }
 
     public int[] getStats() {
         int ret[] = {0, 0};
         ret[0] = cache.size();
         //ObjectSet<MarkovNode> query = database.get(new MarkovNode(null, false));
         //ret[0] = query.size();
         //for (MarkovNode n : query) {
         for (MarkovNode n : cache.values())
         {
             ret[1] += n.getConnectionCount();
         }
         return ret;
     }
 
     public void shutdown() {
         shuttingDown = true;
     }
 
     public MarkovNode getNode(String word) {
         if (cache.containsKey(word))
         {
             return cache.get(word);
         } else {
             ObjectSet<MarkovNode> query = database.get(new MarkovNode(word, true));
             if (query.size() == 0) {
                 return null;
             } else {
                 MarkovNode n = query.get(0);
                 cache.put(word, n);
                 return n;
             }
         }
     }
 
     public void run() {
         Logger.getLogger(MarkovDatabase.class.getName()).log(Level.INFO, "Markov Database Thread Running");
         while (!shuttingDown) {
             do {
                 try {
                     MarkovNode n = saveQueue.take();
                     if (!n.getWord().equals(("")))
                         database.set(n);
                 } catch (InterruptedException ex) {
                     Logger.getLogger(MarkovDatabase.class.getName()).log(Level.SEVERE, null, ex);
                 }
             }
             while (saveQueue.peek() != null);
             Logger.getLogger(MarkovString.class.getName()).log(Level.INFO, "Committing");
             database.commit();
             Logger.getLogger(MarkovString.class.getName()).log(Level.INFO, "Committing Complete");
         }
        Logger.getLogger(MarkovString.class.getName()).log(Level.INFO, "Markov Database Thread Finished");
         database.close();
     }
 }
