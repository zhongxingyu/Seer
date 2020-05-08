 package nl.astraeus.persistence;
 
 import java.io.Serializable;
 import java.util.*;
 
 /**
  * SimpleList
  * <p/>
  * User: rnentjes
  * Date: 7/27/11
  * Time: 2:52 PM
  */
 public class SimpleList<M extends SimpleModel> implements java.util.List<M>, Serializable {
     public static final long serialVersionUID = 1L;
 
     private Class<? extends SimpleModel> cls;
     private java.util.List<Long> list = new LinkedList<Long>();
     private transient Map<Long, M> incoming;
 
     public SimpleList(Class<? extends SimpleModel> cls) {
         this.cls = cls;
     }
 
     public Map<Long, M> getIncoming() {
         if (incoming == null) {
             incoming = new HashMap<Long, M>();
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
         if (o instanceof SimpleModel) {
             return list.contains(((SimpleModel)o).getId());
         } else {
             return list.contains(o);
         }
     }
     
     public java.util.List<Long> getIdList() {
         return list;
     }
 
     public Class<? extends SimpleModel> getType() {
         return cls;
     }
 
     public Iterator<M> iterator() {
         return new Iterator<M>() {
             Iterator<Long> it = list.iterator();
             M next = null;
 
             public boolean hasNext() {
                 while (next == null && it.hasNext()) {
                     long id = it.next();
 
                    next = incoming.get(id);
 
                     if (next == null) {
                         next = (M) SimpleStore.get().getModelMap(cls).get(id);
                     }
                 }
 
                 return (next != null);
             }
 
             public M next() {
                 M result = next;
                 
                 next = null;
 
                 while (it.hasNext() && next == null) {
                     Long nextId = it.next();
 
                     next = getIncoming().get(nextId);
 
                     if (next == null) {
                         next = (M) SimpleStore.get().find(cls, nextId);
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
     public boolean add(long id) {
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
 
     private java.util.List<Long> getLongList(Collection<? extends M> c) {
         java.util.List<Long> result = new LinkedList<Long>();
 
         for (M m : c) {
             result.add(m.getId());
         }
 
         return result;
     }
 
     public boolean retainAll(Collection<?> c) {
         return list.retainAll(getLongList((Collection<? extends M>) c));
     }
 
     public void clear() {
         getIncoming().clear();
         list.clear();
     }
 
     public M get(int index) {
         Long id = list.get(index);
 
         M result = getIncoming().get(id);
 
         if (result == null) {
             result = (M) SimpleStore.get().find(cls, id);
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
         Long id = list.remove(index);
 
         M result = getIncoming().get(id);
 
         if (result == null) {
             result = (M) SimpleStore.get().find(cls, id);
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
 
     public java.util.List<M> subList(int fromIndex, int toIndex) {
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
