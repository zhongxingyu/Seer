 package gx.realtime;
 
 import org.junit.Test;
 
 import static org.junit.Assert.fail;
 import static org.mockito.Mockito.*;
 
 public class EventTargetTest
 {
 
     //Event handlers
     private EventHandler<TestEvent> handler1 = (testEvent) -> {
         //System.out.println("--EventHandler 1 called.");
         TestObject testObject = (TestObject) testEvent.getTarget();
         testObject.setId(testObject.getId() + 1);
     };
 
     private EventHandler<TestEvent> handler2 = (testEvent) -> {
         //System.out.println("--EventHandler2 called.");
         TestObject testObject2 = (TestObject) testEvent.getTarget();
         testObject2.setId(testObject2.getId() + 10);
     };
 
     @Test
     public void testAddEventListener()
     {
         EventTarget target = new EventTarget()
         {
         };
         EventHandler<ObjectChangedEvent> handler = mock(EventHandler.class);
         target.addEventListener(EventType.OBJECT_CHANGED, handler);
 
         target.fireEvent(new ObjectChangedEvent(target, null, null, true, null));
 
         verify(handler).handleEvent(isA(ObjectChangedEvent.class));
     }
 
     @Test
     public void testRemoveEventListener()
     {
         EventTarget target = new EventTarget()
         {
         };
         EventHandler<ObjectChangedEvent> handler = mock(EventHandler.class);
         target.addEventListener(EventType.OBJECT_CHANGED, handler);
 
         // Remove the handler we just added
         target.removeEventListener(EventType.OBJECT_CHANGED, handler);
 
         target.fireEvent(new ObjectChangedEvent(target, null, null, true, null));
 
         verify(handler, never()).handleEvent(isA(ObjectChangedEvent.class));
     }
 
     @Test
     public void testRemovingNonexistentEventListenerIsIgnored()
     {
         EventTarget target = new EventTarget()
         {
         };
 
         // Remove the handler (which we never added in the first place)
         EventHandler<ObjectChangedEvent> handler = mock(EventHandler.class);
         target.removeEventListener(EventType.OBJECT_CHANGED, handler);
 
         // Expected: no exception thrown
     }
 
     @Test
     public void testEventListeners()
     {
 
         TestObject simpleObject = new TestObject(100);
 
         //Events
         TestEvent insertedEvent = new TestEvent(EventType.TEXT_INSERTED, simpleObject, "SID", "GxTestSuite", true, false);
         TestEvent deletedEvent = new TestEvent(EventType.TEXT_DELETED, simpleObject, "SID", "GxTestSuite", true, false);
 
         //fire event with no handlers registered.
         simpleObject.fireEvent(insertedEvent);
         simpleObject.fireEvent(deletedEvent);
         assertEquals(100, simpleObject.getId());
 
         //fire events with handler1 registered for inserted event.
         simpleObject.addEventListener(insertedEvent.getType(), handler1);
         simpleObject.fireEvent(insertedEvent);
         assertEquals(101, simpleObject.getId());
         simpleObject.fireEvent(deletedEvent);
         assertEquals(101, simpleObject.getId());
 
         //fire events with handler1 registered for deleted event.
         simpleObject.addEventListener(deletedEvent.getType(), handler1);
         simpleObject.fireEvent(insertedEvent);
         assertEquals(102, simpleObject.getId());
         simpleObject.fireEvent(deletedEvent);
         assertEquals(103, simpleObject.getId());
 
         //fire events for both handlers registered for inserted event.
         simpleObject.addEventListener(insertedEvent.getType(), handler2);
         simpleObject.fireEvent(insertedEvent);
         assertEquals(114, simpleObject.getId());
         simpleObject.fireEvent(deletedEvent);
         assertEquals(115, simpleObject.getId());
 
         //fire events for duplicate handler1 registered for inserted event.
         simpleObject.addEventListener(insertedEvent.getType(), handler1);
         simpleObject.fireEvent(insertedEvent);
         assertEquals(126, simpleObject.getId());
         simpleObject.fireEvent(deletedEvent);
         assertEquals(127, simpleObject.getId());
 
         //fire events for both handlers registered for both event types.
         simpleObject.addEventListener(deletedEvent.getType(), handler2);
         simpleObject.fireEvent(insertedEvent);
         assertEquals(138, simpleObject.getId());
         simpleObject.fireEvent(deletedEvent);
         assertEquals(149, simpleObject.getId());
 
         //fire events for duplicate registration of handler2 for deleted type
         simpleObject.addEventListener(deletedEvent.getType(), handler2);
         simpleObject.fireEvent(insertedEvent);
         assertEquals(160, simpleObject.getId());
         simpleObject.fireEvent(deletedEvent);
         assertEquals(171, simpleObject.getId());
     }
 
     @Test
     public void testEventBubbling()
     {
         //"bubbling" without any parents
         TestObject object1 = new TestObject(10);
         TestEvent event = new TestEvent(EventType.OBJECT_CHANGED, object1, "SID", "UID", true, true);
         object1.addEventListener(EventType.OBJECT_CHANGED, handler1);
 
         object1.fireEvent(event);
         assertEquals(11, object1.getId());
 
         //one parent
         TestObject object2 = new TestObject(110);
         object1.addChild(object2);
 
         System.out.println("--Firing event on object 2 with object 1 as parent");
         object2.fireEvent(event);
         assertEquals(12, object1.getId());
         assertEquals(110, object2.getId());
 
         //3 parents of object2
         TestObject object3 = new TestObject(210);
         object3.addChild(object2);
         TestObject object4 = new TestObject(310);
         object4.addChild(object2);
         object2.fireEvent(event);
         assertEquals(13, object1.getId());
         assertEquals(210, object3.getId());
         assertEquals(310, object4.getId());
 
         //two layers of parents, object 5 is leaf node.
         TestObject object5 = new TestObject(410);
         object2.addChild(object5);
         TestObject object6 = new TestObject(510);
         object6.addChild(object5);
 
         object5.fireEvent(event);
         assertEquals(14, object1.getId());
         assertEquals(210, object3.getId());
         assertEquals(310, object4.getId());
         assertEquals(410, object5.getId());
         assertEquals(510, object6.getId());
 
         //test if not fired twice
         object1.addChild(object5);
         object5.fireEvent(event);
         assertEquals(15, object1.getId());
         assertEquals(210, object3.getId());
         assertEquals(310, object4.getId());
         assertEquals(410, object5.getId());
         assertEquals(510, object6.getId());
 
         fail("TODO: test with ordinary events instead of BaseModelEvent");
     }
 }
