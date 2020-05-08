 /**
  * 
  */
 package chord.analyses.snapshot;
 
 import gnu.trove.TIntHashSet;
 
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.Map.Entry;
 
 import chord.project.Chord;
 
 /**
  * @author omert
  *
  */
 @Chord(name="ss-thr-access")
 public class ThreadAccessAnalysis extends SnapshotAnalysis {
 
 	private static class ThreadAccessQuery extends Query {
 		public final int threadCount;
 		public final Object abs;
 		
 		public ThreadAccessQuery(Object abs, int threadCount) {
 			this.abs = abs;
 			this.threadCount = threadCount;
 		}
 
 		@Override
 		public int hashCode() {
 			final int prime = 31;
 			int result = 1;
 			result = prime * result + ((abs == null) ? 0 : abs.hashCode());
 			result = prime * result + threadCount;
 			return result;
 		}
 
 		@Override
 		public boolean equals(Object obj) {
 			if (this == obj)
 				return true;
 			if (obj == null)
 				return false;
 			if (getClass() != obj.getClass())
 				return false;
 			ThreadAccessQuery other = (ThreadAccessQuery) obj;
 			if (abs == null) {
 				if (other.abs != null)
 					return false;
 			} else if (!abs.equals(other.abs))
 				return false;
 			if (threadCount != other.threadCount)
 				return false;
 			return true;
 		}
 	}
 	
 	private static class Event {
 		public final int t;
 		public final int b;
 		public final boolean isRemove;
 
 		public Event(int t, int b, boolean isRemove) {
 			this.t = t;
 			this.b = b;
 			this.isRemove = isRemove;
 		}
 	}
 
 	private final List<Event> events = new LinkedList<Event>();
 	private final Map<Object, TIntHashSet> abs2threads = new HashMap<Object, TIntHashSet>();
 	
 	@Override
 	public String propertyName() {
 		return "thread-access";
 	}
 	
 	@Override
 	public void initPass() {
 		abs2threads.clear();
 	}
 	
 	@Override
 	public void donePass() {
 		super.donePass();
 		Set<Entry<Object,TIntHashSet>> entrySet = abs2threads.entrySet();
 		for (Entry<Object,TIntHashSet> e : entrySet) {
 			ThreadAccessQuery q = new ThreadAccessQuery(e.getKey(), e.getValue().size());
 			if (shouldAnswerQueryHit(q)) {
 				// The query is answered positively iff the object was accessed by no more than a single thread.
 				answerQuery(q, q.threadCount <= 1);
 			}
 		}
 	}
 
 	@Override
 	public SnapshotResult takeSnapshot() {
 		if (queryOnlyAtSnapshot) {
 			abstraction.ensureComputed();
 			for (Event evt : events) {
 				if (evt.isRemove) {
 					registerThreadAccessOnNew(evt.t, evt.b);
 				} else {
 					registerThreadAccess(evt.t, evt.b);
 				}
 			}
 			events.clear();
 		}
 		return null;
 	}
 
 	@Override
 	public void processNewOrNewArray(int h, int t, int o) {
 		super.processNewOrNewArray(h, t, o);
		if (h >= 0 && o != 0 && !isExcluded(h)) {
 			if (queryOnlyAtSnapshot) {
 				Event event = new Event(t, o, true);
 				events.add(event);
 			} else {
 				abstraction.ensureComputed();
 				registerThreadAccessOnNew(t, o);
 			}
 		}
 	}
 	
 	@Override
 	public void fieldAccessed(int e, int t, int b, int f, int o) {
 		super.fieldAccessed(e, t, b, f, o);
		if (e >= 0 && b != 0 && !isExcluded(e)) {
 			if (queryOnlyAtSnapshot) {
 				Event event = new Event(t, b, false);
 				events.add(event);
 			} else {
 				abstraction.ensureComputed();
 				registerThreadAccess(t, b);
 			}
 		}
 	}
 
 	private void registerThreadAccess(int t, int b) {
 		Object abs = abstraction.getValue(b);
 		TIntHashSet S = abs2threads.get(abs);
 		if (S == null) {
 			S = new TIntHashSet();
 			abs2threads.put(abs, S);
 		}
 		S.add(t);
 	}
 	
 	private void registerThreadAccessOnNew(int t, int o) {
 		Object abs = abstraction.getValue(o);
 		abs2threads.remove(abs);
 		TIntHashSet S = new TIntHashSet();
 		S.add(t);
 		abs2threads.put(abs, S);
 	}
 }
