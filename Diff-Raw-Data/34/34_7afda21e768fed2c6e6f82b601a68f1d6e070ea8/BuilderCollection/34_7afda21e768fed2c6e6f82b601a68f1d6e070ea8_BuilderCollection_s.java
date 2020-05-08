 package collections;
 
 import java.util.Collection;
 import java.util.Iterator;
 
 /**
  * This {@code BuilderCollection} can be used to quickly construct a collection from another collection or as a base
  * class to simplify building a collection that self populate from a custom type.
  * <p/>
  * It is constructed with a {@link Builder} and a backing {@link Collection}.
  * <p/>
  * The {@link Builder#build()} method must be implemented to provide the logic that will be used to build each item to
  * be contain within the collection. The {@link Builder#build()} method will be repeatedly called until it returns
  * {@code null}.
  * <p/>
  * The backing collection is the actual collection that will hold the built items.
  *
  * @author Karl Bennett
  *
  * @param <T> the generic type of the collections items.
  */
 public class BuilderCollection<T> implements Collection<T> {
 
    private final Collection collection;
 
 
     /**
      * Instantiate a new {@code BuilderCollection} that will use the supplied {@link Builder} to build it's items and
      * the supplied backing {@link Collection} to hold it's items.
      *
      * @param builder    the builder used to build the entries for the new map.
      * @param collection the collection that will be used to hold the built items.
      */
    public BuilderCollection(Builder<T> builder, Collection collection) {
 
         if (null == builder) {
 
             throw new IllegalArgumentException(getClass().getName() + "(Builder,Collection) builder must not be null.");
         }
 
         if (null == collection) {
 
             throw new IllegalArgumentException(getClass().getName() + "(Builder,Collection) collection must not be null.");
         }
 
         this.collection = collection;
 
         for (T item = builder.build(); null != item; item = builder.build()) {
 
             this.collection.add(item);
         }
     }
 
 
     /**
      * {@inheritDoc}
      */
     @Override
     public int size() {
 
         return collection.size();
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public boolean isEmpty() {
 
         return collection.isEmpty();
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public boolean contains(Object item) {
 
         return collection.contains(item);
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public Iterator<T> iterator() {
 
         return collection.iterator();
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public Object[] toArray() {
 
         return collection.toArray();
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
    public <T extends Object> T[] toArray(T[] array) {
 
        return (T[]) collection.toArray(array);
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public boolean add(T item) {
 
         return collection.add(item);
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public boolean remove(Object item) {
 
         return collection.remove(item);
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public boolean containsAll(Collection<?> items) {
 
         return collection.containsAll(items);
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public boolean addAll(Collection<? extends T> items) {
 
         return collection.addAll(items);
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public boolean removeAll(Collection<?> items) {
 
         return collection.removeAll(items);
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public boolean retainAll(Collection<?> items) {
 
         return collection.retainAll(items);
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public void clear() {
 
         collection.clear();
     }
 
     @Override
     public boolean equals(Object o) {
 
         if (this == o) return true;
 
         if (o == null || getClass() != o.getClass()) return false;
 
         BuilderCollection that = (BuilderCollection) o;
 
         return collection.equals(that.collection);
 
     }
 
     @Override
     public int hashCode() {
 
         return collection.hashCode();
     }
 
     @Override
     public String toString() {
 
         return collection.toString();
     }
 }
