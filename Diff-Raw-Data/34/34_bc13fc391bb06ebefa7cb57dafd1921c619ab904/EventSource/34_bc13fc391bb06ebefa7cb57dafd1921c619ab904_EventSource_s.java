 package fi.jawsy.jawwa.frp;
 
 import java.io.Serializable;
import java.util.Set;
 
import com.google.common.collect.Sets;
 
 import fi.jawsy.jawwa.lang.Effect;
 
 public class EventSource<E> extends EventStreamBase<E> implements EventSink<E> {
 
     private static final long serialVersionUID = -2046636095523847664L;
 
    private final Set<Effect<? super E>> listeners = Sets.newIdentityHashSet();
 
     @Override
     public CleanupHandle foreach(final Effect<? super E> e) {
        listeners.add(e);
         class ListenerCleanup implements CleanupHandle, Serializable {
             private static final long serialVersionUID = -8501830733777586751L;
 
             @Override
             public void cleanup() {
                listeners.remove(e);
             }
         }
         return new ListenerCleanup();
     }
 
     public static <E> EventSource<E> create() {
         return new EventSource<E>();
     }
 
     @Override
     public CleanupHandle consume(EventStream<? extends E> es) {
         class FireEvent implements Effect<E>, Serializable {
             private static final long serialVersionUID = -2885119782485225579L;
 
             @Override
             public void apply(E input) {
                 fire(input);
             }
         }
         return es.foreach(new FireEvent());
     }
 
     @Override
     public void fire(E event) {
         for (Effect<? super E> listener : listeners) {
             listener.apply(event);
         }
     }
 
     public boolean hasListeners() {
         return !listeners.isEmpty();
     }
 
 }
