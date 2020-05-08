 package nl.astraeus.persistence;
 
 import java.io.Serializable;
 import java.util.*;
 
 /**
  * PersistentList
  * <p/>
  * User: rnentjes
  * Date: 7/27/11
  * Time: 2:52 PM
  */
 public class PersistentList<K, M extends Persistent<K>> implements List<M>, Serializable {
     public static final long serialVersionUID = 1L;
 
     private Class<M> cls;
     private List<K> list = new LinkedList<K>();
    private transient Map<K, M> incoming;
 
     public PersistentList(Class<M> cls) {
         this.cls = cls;
     }
 
     public Map<K, M> getIncoming() {
         if (incoming == null) {
             incoming = new HashMap<K, M>();
         }
 
         return incoming;
     }
 
     public void clearIncoming() {
         getIncoming().clear();
     }
 
     public int size() {
         return list.size();
     }
 
     public boolean isEmpty() {
         return list.isEmpty();
     }
 
     public boolean contains(Object o) {
         if (o instanceof Persistent) {
             return list.contains(((Persistent)o).getId());
         } else {
             return list.contains(o);
         }
     }
 
     public List<K> getIdList() {
         return list;
     }
 
     public Class<M> getType() {
         return cls;
     }
 
     public Iterator<M> iterator() {
         return new Iterator<M>() {
             Iterator<K> it = list.iterator();
             M next = null;
 
             public boolean hasNext() {
                 while (next == null && it.hasNext()) {
                     K id = it.next();
 
                     next = (M) PersistentManager.get().getModelMap(cls).get(id);
                 }
 
                 return (next != null);
             }
 
             public M next() {
                 M result = next;
 
                 next = null;
 
                 while (it.hasNext() && next == null) {
                     K nextId = it.next();
 
                     next = getIncoming().get(nextId);
 
                     if (next == null) {
                         next = (M) PersistentManager.get().find(cls, nextId);
                     }
                 }
 
                 return result;
             }
 
             public void remove() {
                 throw new UnsupportedOperationException();
             }
         };
     }
 
     public Object[] toArray() {
         throw new IllegalStateException("Not implemented yet.");
     }
 
     public <T> T[] toArray(T[] a) {
         throw new IllegalStateException("Not implemented yet.");
     }
 
     public boolean add(M m) {
         //SimpleStore.get().assertIsStored(m);
 
         getIncoming().put(m.getId(), m);
 
         return list.add(m.getId());
     }
 
     // used by SimplePersistence to copy this list,
     // using this function will avoid the entity to be added to the incoming list
     public boolean addId(K id) {
         //SimpleStore.get().assertIsStored(m);
         return list.add(id);
     }
 
     public boolean remove(Object o) {
         getIncoming().remove(((M)o).getId());
         return list.remove(((M)o).getId());
     }
 
     public boolean containsAll(Collection<?> c) {
         throw new IllegalStateException("Not implemented yet.");
     }
 
     public boolean addAll(Collection<? extends M> c) {
         boolean result = true;
 
         for (M m : c) {
             result = result && add(m);
         }
 
         return result;
     }
 
     public boolean addAll(int index, Collection<? extends M> c) {
         for (M m : c) {
             add(index, m);
         }
 
         return true;
     }
 
     public boolean removeAll(Collection<?> c) {
         for (Object m : c) {
             remove(m);
         }
 
         return true;
     }
 
     private List<K> getIdList(Collection<? extends M> c) {
         List<K> result = new LinkedList<K>();
 
         for (M m : c) {
             result.add(m.getId());
         }
 
         return result;
     }
 
     public boolean retainAll(Collection<?> c) {
         return list.retainAll(getIdList((Collection<? extends M>) c));
     }
 
     public void clear() {
         getIncoming().clear();
         list.clear();
     }
 
     public M get(int index) {
         K id = list.get(index);
 
         M result = getIncoming().get(id);
 
         if (result == null) {
             result = (M) PersistentManager.get().find(cls, id);
         }
 
         return result;
     }
 
     public M set(int index, M element) {
         M result = get(index);
 
         getIncoming().put(element.getId(), element);
 
         list.set(index, element.getId());
 
         return result;
     }
 
     public void add(int index, M element) {
         getIncoming().put(element.getId(), element);
 
         list.add(index, element.getId());
     }
 
     public M remove(int index) {
         K id = list.remove(index);
 
         M result = getIncoming().get(id);
 
         if (result == null) {
             result = (M) PersistentManager.get().find(cls, id);
         }
 
         return result;
     }
 
     public int indexOf(Object o) {
         return list.indexOf(((M)o).getId());
     }
 
     public int lastIndexOf(Object o) {
         return list.lastIndexOf(((M) o).getId());
     }
 
     public ListIterator<M> listIterator() {
         throw new IllegalStateException("Not implemented yet.");
     }
 
     public ListIterator<M> listIterator(int index) {
         throw new IllegalStateException("Not implemented yet.");
     }
 
     public List<M> subList(int fromIndex, int toIndex) {
         throw new IllegalStateException("Not implemented yet.");
     }
     
     public String toString() {
         StringBuilder result = new StringBuilder();
         
         if (list.isEmpty()) {
             result.append("empty");
         } else {
             for (int i = 0; i < 10 && this.list.size() > i; i++) {
                 if (i > 0) {
                     result.append(", ");
                 }
                 result.append(this.list.get(i));
             }
             if (this.list.size() > 10) {
                 result.append(", <" + (this.list.size() - 10) + " more>");
             }
         }
         return result.toString();
     }
 }
