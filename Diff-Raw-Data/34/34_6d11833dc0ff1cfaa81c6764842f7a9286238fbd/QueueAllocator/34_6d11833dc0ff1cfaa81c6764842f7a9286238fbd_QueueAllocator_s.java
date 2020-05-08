 package uk.ac.imperial.lpgdash.allocators;
 
 import java.util.Iterator;
 import java.util.LinkedHashSet;
 import java.util.LinkedList;
 import java.util.List;
 
 import org.drools.runtime.StatefulKnowledgeSession;
 
 import uk.ac.imperial.lpgdash.actions.Allocate;
 import uk.ac.imperial.lpgdash.facts.Cluster;
 import uk.ac.imperial.lpgdash.facts.Player;
 
 public class QueueAllocator {
 
 	private final StatefulKnowledgeSession session;
 	private final Cluster c;
 	LinkedHashSet<Player> queue = new LinkedHashSet<Player>();
 
 	public QueueAllocator(Cluster c, StatefulKnowledgeSession session) {
 		super();
 		this.c = c;
 		this.session = session;
 	}
 
 	public Cluster getC() {
 		return c;
 	}
 
 	public void allocate(List<Player> players, double poolSize, int t) {
 		// add new players to queue
 		queue.addAll(players);
 		// allocate from queue
 		LinkedList<Player> allocated = new LinkedList<Player>();
 		Iterator<Player> it = queue.iterator();
 		while (it.hasNext() && poolSize > 0) {
 			Player p = it.next();
			double allocation = Math.min(p.getD(), poolSize);
			session.insert(new Allocate(p, allocation, t));
			poolSize -= allocation;
 			it.remove();
			allocated.addLast(p);
 		}
 		queue.addAll(allocated);
 		for (Player p : players) {
 			if (allocated.contains(p))
 				continue;
 			else
 				session.insert(new Allocate(p, 0, t));
 		}
 	}
 
 	public void penalise(Player p) {
 		queue.remove(p);
 	}
 
 }
