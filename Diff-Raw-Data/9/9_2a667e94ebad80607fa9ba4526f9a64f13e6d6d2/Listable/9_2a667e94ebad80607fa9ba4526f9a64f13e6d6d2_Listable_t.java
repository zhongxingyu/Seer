 package collection.persistent;
 
 import javax.annotation.Nullable;
 
 public interface Listable<V> {
   /**
   * @return List over collection values,
    *         or <code>null</code> if collection is empty.
    */
   @Nullable
   List<V> list();
 
   interface List<V> {
    /** @return First element of the current sublist. */
     V head();
 
     /**
      * @return Sublist without the first element,
      *         may be <code>null</code> if no more elements.
      */
     @Nullable
     List<V> tail();
   }
 }
