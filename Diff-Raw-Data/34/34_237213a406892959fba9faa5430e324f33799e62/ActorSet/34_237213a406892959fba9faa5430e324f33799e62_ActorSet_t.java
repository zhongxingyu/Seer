 package actor;
 
 import java.util.Collection;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 public class ActorSet implements Set<Actor> {
 
     private Map<ActorId, Actor> actors;
     private final int playerId;
 
     public ActorSet(int playerId) {
         this.playerId = playerId;
         actors = new java.util.concurrent.ConcurrentHashMap<ActorId, Actor>();
     }
 
     public ActorSet() {
         this(0);
     }
 
     @Override
     public synchronized boolean add(Actor a) {     
         if (a.id == null)
             a.id = new ActorId(playerId);
 
         if (actors.containsKey(a.id))
             return false;
         actors.put(a.id, a);
 
         return true;
     }
 
 
     @Override
     public  boolean addAll(Collection<? extends Actor> list) {
         for (Actor a: list)
             add(a);
 
         return true;
     }
 
 
     @Override
     public synchronized void clear() {
         actors.clear();
     }
 
     @Override
     public boolean contains(Object arg0) {
         return false;
     }
 
     public synchronized boolean contains(Actor a) {
         if (a.id == null)
             return false;
         return actors.containsKey(a.id);
     }
 
     @Override
     public boolean containsAll(Collection<?> list) {
         for (Object o: list)
             if (contains(o) == false)
                 return false;
 
 
         return true;
     }
 
     @Override
     public synchronized boolean isEmpty() {
         return actors.isEmpty();
     }
 
 
     @Override
     public synchronized Iterator<Actor> iterator() {
         return new CopyIterator();
     }
 
 
     @Override
     public boolean remove(Object arg0) {
         return false;
     }
 
     public synchronized boolean remove(Actor a) {
         if (a.id == null)
             return false;
 
         return actors.remove(a.id) != null;
     }
 
     @Override
     public boolean removeAll(Collection<?> arg0) {
         // TODO Auto-generated method stub
         return false;
     }
 
 
     @Override
     public boolean retainAll(Collection<?> arg0) {
         // TODO Auto-generated method stub
         return false;
     }
 
 
     @Override
     public synchronized int size() {
         return actors.size();
     }
 
 
     @Override
     public Object[] toArray() {
         // TODO Auto-generated method stub
         return null;
     }
 
 
     @Override
     public <T> T[] toArray(T[] arg0) {
         // TODO Auto-generated method stub
         return null;
     }
 
     public synchronized Actor findById(ActorId id) {
         return actors.get(id);
     }
 
     public void update(int frames) {
         for (Actor a: this) {
             a.update();
         }
     }
     
     public synchronized List<Actor> getCopyList() {
         return new CopyList();    
     }
 
     /**
      * Copy iterator
      * @author dustin
      */
     private class CopyIterator implements Iterator<Actor> {
         List<Actor> copy;
         Iterator<Actor> it;
 
         public CopyIterator() {
             copy = new java.util.ArrayList<Actor>(actors.values());
             it = copy.iterator();
         }
 
         @Override
         public boolean hasNext() {
             return it.hasNext();
         }
 
         @Override
         public Actor next() {
             return it.next();
         }
 
         @Override
         public void remove() {
             throw new UnsupportedOperationException("remove() not supported by " + this.getClass());
         }
     }
 
     private class CopyList extends java.util.ArrayList<Actor> {
         private static final long serialVersionUID = -7412443957198423304L;
 
         public CopyList() {
             super(actors.values());
         }
     }
 }
