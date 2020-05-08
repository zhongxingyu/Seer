 package org.nohope.bean;
 
 import org.junit.Test;
 
 import javax.annotation.Nonnull;
 import java.util.HashMap;
 import java.util.Map;
 
 import static org.junit.Assert.assertFalse;
 import static org.junit.Assert.assertTrue;
 
 /**
  * @author <a href="mailto:ketoth.xupack@gmail.com">ketoth xupack</a>
  * @since 12/10/12 3:28 PM
  */
 public class StatefulDispatcherTest {
 
     @Test
     public void naiveTest() {
         final Dispatcher dispatcher = new Dispatcher();
         final SimpleBean a = new SimpleBean(dispatcher);
         final SimpleBean b = new SimpleBean(dispatcher);
         assertFalse(dispatcher.heated(a));
         assertFalse(dispatcher.heated(b));
         a.setA(1);
         assertFalse(dispatcher.heated(a));
         assertFalse(dispatcher.heated(b));
         b.setA(1);
         assertFalse(dispatcher.heated(a));
         assertFalse(dispatcher.heated(b));
         a.setA(1);
         assertTrue(dispatcher.heated(a));
         assertFalse(dispatcher.heated(b));
         b.setA(1);
         assertTrue(dispatcher.heated(a));
         assertTrue(dispatcher.heated(b));
     }
 
     private static class SimpleBean extends AbstractDispatchable<SimpleBean> {
         protected SimpleBean(@Nonnull final IDispatcher<SimpleBean> dispatcher) {
             super(dispatcher);
         }
 
         @Dispatch
         public void setA(final int param) {
         }
     }
 
    private static class Dispatcher extends StatefulDispatcher {
         private final Map<Object, Boolean> heated = new HashMap<>();
 
         @Override
        protected void handle(@Nonnull final IDispatchable obj
                 , @Nonnull final String propertyName
                 , final Object oldValue
                 , final Object newValue
                 , final boolean previousExists) {
             if (previousExists) {
                 heated.put(obj, true);
             }
         }
 
         public boolean heated(final Object bean) {
             return heated.containsKey(bean);
         }
     }
 }
