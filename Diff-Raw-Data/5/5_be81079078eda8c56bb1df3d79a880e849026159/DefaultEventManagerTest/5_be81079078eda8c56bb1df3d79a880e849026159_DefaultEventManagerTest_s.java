 package org.mule.galaxy.event;
 
 import static org.mule.galaxy.event.DefaultEvents.PROPERTY_CHANGED;
 import static org.mule.galaxy.event.DefaultEvents.WORKSPACE_DELETED;
 import org.mule.galaxy.event.annotation.BindToEvent;
 import org.mule.galaxy.event.annotation.BindToEvents;
 import org.mule.galaxy.event.annotation.OnEvent;
 import org.mule.galaxy.impl.event.DefaultEventManager;
 import org.mule.galaxy.security.User;
 
 import java.util.Collections;
 
 import junit.framework.TestCase;
 
 public class DefaultEventManagerTest extends TestCase {
 
     public void testRemoveListener() {
         EventManager em = new DefaultEventManager(Collections.emptyList());
         TestSingleEventListener listener = new TestSingleEventListener();
         TestEvent event = new TestEvent(this);
         em.addListener(listener);
 
         em.fireEvent(event);
 
         assertNotNull(listener.getEvent());
         assertSame(event, listener.getEvent());
 
         // now remove the listeners for the event and re-fire
         em.removeListener(TestEvent.class);
         listener.reset();
         em.fireEvent(event);
 
         assertNull(listener.getEvent());
     }
 
     public void testNullInput() throws Exception {
         EventManager em = new DefaultEventManager(Collections.emptyList());
         try {
             em.addListener(null);
             fail("Should've thrown an IllegalArgumentException");
         } catch (IllegalArgumentException e) {
             // expected
         }
     }
 
     public void testMissingOnEventAnnotation() throws Exception {
         EventManager em = new DefaultEventManager(Collections.emptyList());
         try {
             em.addListener(new ClassAnnotationMissingOnEvent());
             fail("Should've thrown an IllegalArgumentException");
         } catch (IllegalArgumentException e) {
             // expected
         }
     }
 
     public void testMissingBindEventAnnotation() throws Exception {
         EventManager em = new DefaultEventManager(Collections.emptyList());
         try {
             em.addListener(new Object());
             fail("Should've thrown an IllegalArgumentException");
         } catch (IllegalArgumentException e) {
             // expected
         }
     }
 
     public void testAnnotationInheritance() throws Exception {
         EventManager em = new DefaultEventManager(Collections.emptyList());
         final InheritedBindingWithOnEvent listener = new InheritedBindingWithOnEvent();
         em.addListener(listener);
 
         // should not fail by now, as there's an @OnEvent in a subclass, but @BindToEvent in superclass
     }
 
     public void testOnEventAnnotation() throws Exception {
         EventManager em = new DefaultEventManager(Collections.emptyList());
         final InheritedBindingWithOnEvent listener = new InheritedBindingWithOnEvent();
         em.addListener(listener);
 
         final PropertyChangedEvent event = new PropertyChangedEvent(new User(), "/some/artifact", "testProperty", "newValue");
         em.fireEvent(event);
 
         assertTrue(listener.called);
         assertSame(event, listener.event);
     }
 
     /**
      * Single event, multiple @OnEvent entrypoints should fail
      */
     public void testMultipleOnEventAnnotationsSingleListener() throws Exception {
         EventManager em = new DefaultEventManager(Collections.emptyList());
         SingleEventMultipleEntryPoints listener = new SingleEventMultipleEntryPoints();
         try {
             em.addListener(listener);
             fail("Should've failed");
         } catch (IllegalArgumentException e) {
             // expected
             assertTrue(e.getMessage().startsWith("Multiple @OnEvent"));
         }
     }
 
     public void testMultiEventListener() {
         EventManager em = new DefaultEventManager(Collections.emptyList());
         MultiEventListener listener = new MultiEventListener();
         em.addListener(listener);
         
         final PropertyChangedEvent event1 = new PropertyChangedEvent(new User(), "/some/artifact", "testProperty", "newValue");
         em.fireEvent(event1);
 
         final WorkspaceDeletedEvent event2 = new WorkspaceDeletedEvent("test workspace");
         em.fireEvent(event2);
 
         assertSame(event1, listener.puEvent);
         assertSame(event2, listener.wdEvent);
     }
 
     public void testNonMatchingOnEventParam() {
         EventManager em = new DefaultEventManager(Collections.emptyList());
         NonMatchingOnEventParam listener = new NonMatchingOnEventParam();
         try {
             em.addListener(listener);
             fail("Should've failed");
         } catch (IllegalArgumentException e) {
             assertTrue("Wrong exception?", e.getMessage().contains("doesn't match"));
         }
     }
 
     @BindToEvent(PROPERTY_CHANGED)
     private static class ClassAnnotationMissingOnEvent {
 
     }
 
    private static class InheritedBindingWithOnEvent extends ClassAnnotationMissingOnEvent {
 
         public volatile boolean called;
         public PropertyChangedEvent event;
 
         @OnEvent
         public void callback(PropertyChangedEvent e) {
             called = true;
             event = e;
         }
 
     }
 
     private static class SingleEventMultipleEntryPoints extends ClassAnnotationMissingOnEvent {
 
         @OnEvent
         public void callback1(PropertyChangedEvent e) {
 
         }
 
         @OnEvent
         public void callback2(PropertyChangedEvent e) {
 
         }
 
     }
 
     @BindToEvents({WORKSPACE_DELETED, PROPERTY_CHANGED})
    private static class MultiEventListener {
 
         public PropertyChangedEvent puEvent;
         public WorkspaceDeletedEvent wdEvent;
 
         @OnEvent
         public void callbackProperty(PropertyChangedEvent e) {
             puEvent = e;
         }
 
         @OnEvent
         public void callbackWorkspace(WorkspaceDeletedEvent e) {
             wdEvent = e;
         }
     }
 
     @BindToEvent("Test")
     private static class NonMatchingOnEventParam {
 
         @OnEvent
         public void onEvent(PropertyChangedEvent event) {}
 
     }
 
 }
