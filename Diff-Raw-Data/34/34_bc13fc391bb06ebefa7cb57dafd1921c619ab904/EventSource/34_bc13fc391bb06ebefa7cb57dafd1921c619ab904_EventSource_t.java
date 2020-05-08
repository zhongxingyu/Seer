 package fi.jawsy.jawwa.frp;
 
 import java.io.Serializable;
 
import com.google.common.base.Objects;
import com.google.common.collect.ImmutableSet;
 
 import fi.jawsy.jawwa.lang.Effect;
 
 public class EventSource<E> extends EventStreamBase<E> implements EventSink<E> {
 
     private static final long serialVersionUID = -2046636095523847664L;
 
    private ImmutableSet<Effect<? super E>> listeners = ImmutableSet.of();
 
     @Override
     public CleanupHandle foreach(final Effect<? super E> e) {
        addListener(e);
         class ListenerCleanup implements CleanupHandle, Serializable {
             private static final long serialVersionUID = -8501830733777586751L;
 
             @Override
             public void cleanup() {
                removeListener(e);
             }
         }
         return new ListenerCleanup();
     }
 
    void removeListener(Effect<? super E> e) {
        ImmutableSet.Builder<Effect<? super E>> b = ImmutableSet.builder();

        for (Effect<? super E> listener : listeners) {
            if (!Objects.equal(e, listener))
                b.add(listener);
        }

        listeners = b.build();
    }

    void addListener(Effect<? super E> e) {
        ImmutableSet.Builder<Effect<? super E>> b = ImmutableSet.builder();

        b.addAll(listeners).add(e);

        listeners = b.build();
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
