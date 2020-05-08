 package dem.bounding;
 
 import dem.quanta.Event;
 import dem.quanta.Handler;
 import dem.quanta.Log;
 
 /**
  * Bounded handlers are required when we must have
  * some info about the bound class {@link E}. For example, it's used
  * a lot in the {@link dem.bundles Bundles} section.
  * <p/>
  * Bounded handlers are strong filters.
  *
  * @author Devgru &lt;java@devg.ru&gt;
  * @since 0.15
  */
 public abstract class BoundedHandler<E extends Event>
         implements Filter<E> {
 
     private final Class<E> bound;
 
     public BoundedHandler(Class<E> bound) {
         this.bound = bound;
     }
 
     public final Class<E> getBoundClass() {
         return bound;
     }
 
     @SuppressWarnings("unchecked")
     public boolean handleIfPossible(Event event) {
         /**
          * <code>assert event != null;</code> not required because {@link Class#isInstance(Object)}
          * returns <code>false</code> on null values
          */
         boolean canHandle = bound.isInstance(event);
         if (canHandle) {
             handle((E) event);
         }
         return canHandle;
     }
 
 
     /**
      * This function returns a simple bounded instance of your handler.
      * No, it's not a brainfuck, I claim! 01:31, 09.12.2008
      *
      * @param target handler we will bound
     * @param cls    our bound class
     * @param <E>    bound type
      * @return bounded handler
      */
     public static <E extends Event>
     BoundedHandler<E> bound(final Handler<E> target, Class<E> cls) {
         assert target != null;
         assert cls != null;
         return new BoundedHandler<E>(cls) {
             public void handle(E event) {
                 target.handle(event);
             }
 
             @Override
             public String toString() {
                return "Bounded handler\n" + Log.offset("bound is " + getBoundClass() + "\ntarget is " + target);
             }
         };
     }
 
     @Override
     public String toString() {
         return "Bounded handler\n" + Log.offset("bound is " + bound);
     }
 
 }
