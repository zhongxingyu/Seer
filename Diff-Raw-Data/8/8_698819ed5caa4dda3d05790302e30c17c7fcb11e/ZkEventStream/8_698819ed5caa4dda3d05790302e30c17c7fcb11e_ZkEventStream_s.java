 package fi.jawsy.jawwa.zk.frp;
 
 import java.io.Serializable;
 
 import lombok.RequiredArgsConstructor;
 
 import org.zkoss.zk.ui.Component;
 import org.zkoss.zk.ui.event.Deferrable;
 import org.zkoss.zk.ui.event.Event;
 import org.zkoss.zk.ui.event.EventListener;
 
 import fi.jawsy.jawwa.frp.CancellationToken;
 import fi.jawsy.jawwa.frp.EventStream;
 import fi.jawsy.jawwa.frp.EventStreamBase;
 import fi.jawsy.jawwa.lang.Effect;
 
 @RequiredArgsConstructor
 public class ZkEventStream<E extends Event> extends EventStreamBase<E> {
 
     private static final long serialVersionUID = 1093097272751986811L;
 
     private final Component c;
     private final String eventName;
     private final boolean deferrable;
 
     public ZkEventStream(Component c, String eventName) {
         this(c, eventName, false);
     }
 
     @RequiredArgsConstructor
     class Listener implements EventListener<E>, Serializable, Deferrable, Runnable {
         private static final long serialVersionUID = 5492016167303631840L;
 
         private final Effect<? super E> f;
 
         public void register(CancellationToken token) {
             c.addEventListener(eventName, this);
             if (token.canBeCancelled()) {
                 token.onCancel(this);
             }
             if (token.isCancelled()) {
                 unregister();
             }
         }
 
         void unregister() {
             c.removeEventListener(eventName, this);
         }
 
         @Override
         public void run() {
             unregister();
         }
 
         @Override
         public void onEvent(E event) throws Exception {
             f.apply(event);
         }
 
         @Override
         public boolean isDeferrable() {
             return deferrable;
         }
 
     }
 
     @Override
     public EventStream<E> foreach(final Effect<? super E> e, CancellationToken token) {
         Listener listener = new Listener(e);
         listener.register(token);
         return this;
     }
 
     public ZkEventStream<E> deferrable() {
         return new ZkEventStream<E>(c, eventName, true);
     }
 
 }
