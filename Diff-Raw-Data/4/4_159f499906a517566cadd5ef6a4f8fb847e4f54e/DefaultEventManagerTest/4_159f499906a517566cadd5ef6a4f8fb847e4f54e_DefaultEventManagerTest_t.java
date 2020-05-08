 package org.mule.galaxy.event;
 
 import static org.mule.galaxy.event.DefaultEvents.*;
 
 import java.util.Calendar;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.List;
 import java.util.Map;
 
 import junit.framework.TestCase;
 
 import org.mule.galaxy.DuplicateItemException;
 import org.mule.galaxy.Item;
 import org.mule.galaxy.NewItemResult;
 import org.mule.galaxy.NotFoundException;
 import org.mule.galaxy.PropertyException;
 import org.mule.galaxy.PropertyInfo;
 import org.mule.galaxy.RegistryException;
 import org.mule.galaxy.collab.CommentManager;
 import org.mule.galaxy.event.annotation.BindToEvent;
 import org.mule.galaxy.event.annotation.BindToEvents;
 import org.mule.galaxy.event.annotation.OnEvent;
 import org.mule.galaxy.impl.event.DefaultEventManager;
 import org.mule.galaxy.lifecycle.Lifecycle;
 import org.mule.galaxy.lifecycle.LifecycleManager;
 import org.mule.galaxy.policy.PolicyException;
 import org.mule.galaxy.security.AccessException;
 import org.mule.galaxy.security.User;
 import org.mule.galaxy.type.Type;
 import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
 
 public class DefaultEventManagerTest extends TestCase {
 
     private ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
 
     public void testRemoveListener() {
         EventManager em = new DefaultEventManager(Collections.emptyList(), null, executor);
         TestSingleEventListener listener = new TestSingleEventListener();
         TestEvent event = new TestEvent(this);
         em.addListener(listener);
 
         em.fireEvent(event);
 
         assertNotNull(listener.getEvent());
         assertSame(event, listener.getEvent());
 
         // now remove the listeners for the event and re-fire
         em.removeListener(listener);
         listener.reset();
         em.fireEvent(event);
 
         assertNull(listener.getEvent());
     }
 
     public void testNullInput() throws Exception {
         EventManager em = new DefaultEventManager(Collections.emptyList(), null, executor);
         try {
             em.addListener(null);
             fail("Should've thrown an IllegalArgumentException");
         } catch (IllegalArgumentException e) {
             // expected
         }
     }
 
     public void testMissingOnEventAnnotation() throws Exception {
         EventManager em = new DefaultEventManager(Collections.emptyList(), null, executor);
         try {
             em.addListener(new ClassAnnotationMissingOnEvent());
             fail("Should've thrown an IllegalArgumentException");
         } catch (IllegalArgumentException e) {
             // expected
         }
     }
 
     public void testMissingBindEventAnnotation() throws Exception {
         EventManager em = new DefaultEventManager(Collections.emptyList(), null, executor);
         try {
             em.addListener(new Object());
             fail("Should've thrown an IllegalArgumentException");
         } catch (IllegalArgumentException e) {
             // expected
         }
     }
 
     public void testAnnotationInheritance() throws Exception {
         EventManager em = new DefaultEventManager(Collections.emptyList(), null, executor);
         final InheritedBindingWithOnEvent listener = new InheritedBindingWithOnEvent();
         em.addListener(listener);
 
         // should not fail by now, as there's an @OnEvent in a subclass, but @BindToEvent in superclass
     }
 
     public void testOnEventAnnotation() throws Exception {
         EventManager em = new DefaultEventManager(Collections.emptyList(), null, executor);
         final InheritedBindingWithOnEvent listener = new InheritedBindingWithOnEvent();
         em.addListener(listener);
 
         final PropertyChangedEvent event = new PropertyChangedEvent(new User(), new DummyItem(), "testProperty", "newValue");
         em.fireEvent(event);
 
         assertTrue(listener.called);
         assertSame(event, listener.event);
     }
 
     /**
      * Single event, multiple @OnEvent entrypoints should fail
      */
     public void testMultipleOnEventAnnotationsSingleListener() throws Exception {
         EventManager em = new DefaultEventManager(Collections.emptyList(), null, executor);
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
         EventManager em = new DefaultEventManager(Collections.emptyList(), null, executor);
         MultiEventListener listener = new MultiEventListener();
         em.addListener(listener);
         
         final PropertyChangedEvent event1 = new PropertyChangedEvent(new User(), new DummyItem(), "testProperty", "newValue");
         em.fireEvent(event1);
 
         final ItemDeletedEvent event2 = new ItemDeletedEvent(new DummyItem());
         em.fireEvent(event2);
 
         assertSame(event1, listener.puEvent);
         assertSame(event2, listener.wdEvent);
     }
 
     public void testNonMatchingOnEventParam() {
         EventManager em = new DefaultEventManager(Collections.emptyList(), null, executor);
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
 
     public static class InheritedBindingWithOnEvent extends ClassAnnotationMissingOnEvent {
 
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
 
     @BindToEvents({ITEM_DELETED, PROPERTY_CHANGED})
     public static class MultiEventListener {
 
         public PropertyChangedEvent puEvent;
         public ItemDeletedEvent wdEvent;
 
         @OnEvent
         public void callbackProperty(PropertyChangedEvent e) {
             puEvent = e;
         }
 
         @OnEvent
         public void callbackWorkspace(ItemDeletedEvent e) {
             wdEvent = e;
         }
     }
 
     @BindToEvent("Test")
     private static class NonMatchingOnEventParam {
 
         @OnEvent
         public void onEvent(PropertyChangedEvent event) {}
 
     }
     
     public static class DummyItem implements Item {
 
         public DummyItem() {
         }
 
         public void delete() throws RegistryException, AccessException {
         }
 
         public CommentManager getCommentManager() {
             return null;
         }
 
         public Calendar getCreated() {
             return null;
         }
 
         public Lifecycle getDefaultLifecycle() {
             return null;
         }
 
         public String getId() {
             return null;
         }
 
         public Object getInternalProperty(String name) {
             return null;
         }
 
         public Item getItem(String name) throws RegistryException, NotFoundException, AccessException {
             return null;
         }
 
         public List<Item> getItems() throws RegistryException {
             return null;
         }
 
         public LifecycleManager getLifecycleManager() {
             return null;
         }
 
        public Item getPrevious() throws RegistryException {
            return null;
        }

         public String getName() {
             return null;
         }
 
         public Item getParent() {
             return null;
         }
 
         public String getPath() {
             return null;
         }
 
         public Collection<PropertyInfo> getProperties() {
             return null;
         }
 
         public Object getProperty(String name) {
             return null;
         }
 
         public PropertyInfo getPropertyInfo(String name) {
             return null;
         }
 
         public Type getType() {
             return null;
         }
 
         public Calendar getUpdated() {
             return null;
         }
 
         public boolean hasProperty(String name) {
             return false;
         }
 
         public boolean isLocal() {
             return false;
         }
 
         public NewItemResult newItem(String name, Type type, Map<String, Object> initialProperties)
                 throws DuplicateItemException, RegistryException, PolicyException, AccessException {
             return null;
         }
 
         public NewItemResult newItem(String name, Type type) throws DuplicateItemException,
                 RegistryException, PolicyException, AccessException {
             return null;
         }
 
         public void setDefaultLifecycle(Lifecycle l) {
         }
 
         public void setInternalProperty(String name, Object value) throws PropertyException, PolicyException,
                 AccessException {
         }
 
         public void setLocked(String name, boolean locked) {
         }
 
         public void setName(String name) {
         }
 
         public void setProperty(String name, Object value) throws PropertyException, PolicyException,
                 AccessException {
 
         }
 
         public void setType(Type type) {
         }
 
         public void setVisible(String property, boolean visible) {
         }
 
         public User getAuthor() {
             return null;
         }
 
         public Item getLatestItem() throws RegistryException {
             return null;
         }
 
     }
 
 }
