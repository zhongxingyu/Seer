 package net.gtaun.util.event;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertFalse;
 import static org.junit.Assert.assertSame;
 import static org.junit.Assert.assertTrue;
 
 import java.util.LinkedList;
 import java.util.Queue;
 
 import net.gtaun.util.event.EventManager.HandlerEntry;
 import net.gtaun.util.event.EventManager.HandlerPriority;
 import net.gtaun.util.event.EventManager.ThrowableHandler;
 import net.gtaun.util.event.events.InterruptableEvent;
 import net.gtaun.util.event.events.UselessEvent;
 import net.gtaun.util.event.events.UselessEventHandler;
 
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 
 public class EventManagerTest
 {
 	private EventManager eventManager;
 	
 	
 	public EventManagerTest()
 	{
 
 	}
 	
 	@Before
 	public void setUp() throws Exception
 	{
 		eventManager = new EventManagerImpl();
 	}
 	
 	@After
 	public void tearDown() throws Exception
 	{
 		eventManager = null;
 	}
 	
 	@Test
 	public void testAddHandlerAndDispatchEvent()
 	{
 		UselessEventHandler handler = new UselessEventHandler()
 		{
 			@Override
			public void onUselessEvent(UselessEvent event)
 			{
				event.setProcessed(true);
 			}
 		};
 		
		eventManager.registerHandler(UselessEvent.class, handler, HandlerPriority.NORMAL);
 		
		UselessEvent event = new UselessEvent();
 		eventManager.dispatchEvent(event);
 		
		assertTrue(event.isProcessed());
 	}
 	
 	@Test
 	public void testAddHandlerAndDispatchEvent2()
 	{
 		final TapableCounter counter = new TapableCounter();
 		
 		UselessEventHandler handler = new UselessEventHandler()
 		{
 			@Override
 			public void onInterruptableEvent(InterruptableEvent event)
 			{
 				counter.tap();
 			}
 		};
 		
 		eventManager.registerHandler(InterruptableEvent.class, this, handler, HandlerPriority.NORMAL);
 		
 		InterruptableEvent event = new InterruptableEvent();
 		eventManager.dispatchEvent(event, this);
 		eventManager.dispatchEvent(event, new Object());
 		eventManager.dispatchEvent(event);
 		
 		assertEquals(1, counter.getTaps());
 	}
 	
 	@Test
 	public void testAddHandlerAndDispatchEvent3()
 	{
 		final TapableCounter counter = new TapableCounter();
 		
 		UselessEventHandler handler = new UselessEventHandler()
 		{
 			@Override
 			public void onInterruptableEvent(InterruptableEvent event)
 			{
 				counter.tap();
 			}
 		};
 		
 		eventManager.registerHandler(InterruptableEvent.class, EventManagerTest.class, handler, HandlerPriority.NORMAL);
 		
 		InterruptableEvent event = new InterruptableEvent();
 		eventManager.dispatchEvent(event, this);
 		eventManager.dispatchEvent(event, new Object());
 		eventManager.dispatchEvent(event);
 		
 		assertEquals(1, counter.getTaps());
 	}
 	
 	@Test
 	public void testAddHandlerAndDispatchEvent4()
 	{
 		final TapableCounter counter = new TapableCounter();
 		
 		UselessEventHandler handler = new UselessEventHandler()
 		{
 			@Override
 			public void onInterruptableEvent(InterruptableEvent event)
 			{
 				counter.tap();
 			}
 		};
 		
 		Cloneable cloneable = new Cloneable()
 		{
 		};
 		
 		eventManager.registerHandler(InterruptableEvent.class, Cloneable.class, handler, HandlerPriority.NORMAL);
 		
 		InterruptableEvent event = new InterruptableEvent();
 		eventManager.dispatchEvent(event, cloneable);
 		eventManager.dispatchEvent(event, new Object());
 		eventManager.dispatchEvent(event);
 		
 		assertEquals(1, counter.getTaps());
 	}
 	
 	@Test
 	public void testRemoveHandler()
 	{
 		UselessEventHandler handler = new UselessEventHandler()
 		{
 			@Override
 			public void onUselessEvent(UselessEvent event)
 			{
 				event.setProcessed(true);
 			}
 		};
 		
 		HandlerEntry entry = eventManager.registerHandler(UselessEvent.class, handler, HandlerPriority.NORMAL);
 		entry.cancel();
 		
 		UselessEvent event = new UselessEvent();
 		eventManager.dispatchEvent(event);
 		
 		assertFalse(event.isProcessed());
 	}
 	
 	@Test
 	public void testInterruptEvent()
 	{
 		final TapableCounter counter = new TapableCounter();
 		
 		UselessEventHandler handler = new UselessEventHandler()
 		{
 			@Override
 			public void onInterruptableEvent(InterruptableEvent event)
 			{
 				counter.tap();
 				event.interrupt();
 			}
 		};
 		
 		UselessEventHandler handler2 = new UselessEventHandler()
 		{
 			@Override
 			public void onInterruptableEvent(InterruptableEvent event)
 			{
 				counter.tap();
 			}
 		};
 
 		eventManager.registerHandler(InterruptableEvent.class, handler, HandlerPriority.NORMAL);
 		eventManager.registerHandler(InterruptableEvent.class, handler2, HandlerPriority.NORMAL);
 		
 		InterruptableEvent event = new InterruptableEvent();
 		eventManager.dispatchEvent(event);
 		
 		assertTrue(counter.getTaps() == 1);
 	}
 	
 	@Test
 	public void testCatchException()
 	{
 		final Exception exception = new Exception();
 		final Queue<Throwable> queue = new LinkedList<Throwable>();
 		
 		UselessEventHandler handler = new UselessEventHandler()
 		{
 			@Override
 			public void onUselessEvent(UselessEvent event) throws Exception
 			{
 				throw exception;
 			}
 		};
 		
 		ThrowableHandler throwableHandler = new ThrowableHandler()
 		{
 			@Override
 			public void handleThrowable(Throwable throwable)
 			{
 				queue.offer(throwable);
 			}
 		};
 		
 		eventManager.registerHandler(UselessEvent.class, handler, HandlerPriority.NORMAL);
 		eventManager.dispatchEvent(throwableHandler, new UselessEvent());
 		
 		assertSame(exception, queue.poll());
 	}
 	
 	@Test
 	public void testManagedEventManagerEntryProperties()
 	{
 		UselessEventHandler handler = new UselessEventHandler()
 		{
 		};
 		
 		ManagedEventManager managedEventManager = new ManagedEventManager(eventManager);
 		HandlerEntry entry = managedEventManager.registerHandler(UselessEvent.class, EntryTest.class, handler, HandlerPriority.LOWEST);
 
 		assertSame(managedEventManager, entry.getEventManager());
 		assertSame(UselessEvent.class, entry.getType());
 		assertSame(EntryTest.class, entry.getRelatedObject());
 		assertSame(EntryTest.class, entry.getRelatedClass());
 		assertSame(handler, entry.getHandler());
 		assertEquals(HandlerPriority.LOWEST.getValue(), entry.getPriority());
 	}
 }
