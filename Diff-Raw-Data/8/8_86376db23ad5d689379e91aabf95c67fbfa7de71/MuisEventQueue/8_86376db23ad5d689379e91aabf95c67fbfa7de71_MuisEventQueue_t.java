 package org.muis.core;
 
 import java.awt.Point;
 import java.awt.Rectangle;
 
 /** The event queue in MUIS which makes sure elements's states stay up-to-date */
 public class MuisEventQueue
 {
 	/** Represents an event that may be queued for handling by the MuisEventQueue */
 	public static interface Event
 	{
 		/** @return The time at which this event was created */
 		long getTime();
 
 		/**
 		 * Checks whether the event should be handled now or stay in the queue and be handled later
 		 *
 		 * @param time The time to use in determining whether this event should be handled now
 		 * @return Whether the event should be handled now or stay in the queue and be handled later
 		 */
 		boolean shouldHandle(long time);
 
 		/** Executes this event's action */
 		void handle();
 
 		/** Called when the event queue discards this event because it has been {@link #isSupersededBy(Event) superseded} */
 		void discard();
 
 		/** @return Whether this event's {@link #handle()} method is currently executing */
 		boolean isHandling();
 
 		/** @return Whether this event's {@link #handle()} method has finished executing or its {@link #discard()} method was called */
 		boolean isFinished();
 
 		/** @return Whether this event's {@link #discard()} method was called */
 		boolean isDiscarded();
 
 		/**
 		 * Compares this event's functionality with that of another event. If this method returns true, it signifies that this event's
 		 * action is completely contained by the functionality of the given event such that this event may be discarded and not handled.
 		 * This method must obey a contract similar to {@link Comparable#compareTo(Object)}, in that if A supersedes B and B supersedes C,
 		 * then A supersedes C.
 		 *
 		 * @param evt The event to compare with
 		 * @return Whether this event's functionality encompasses the functionality of the given event such that the given event need not be
 		 *         acted on
 		 */
 		boolean isSupersededBy(Event evt);
 
 		/** @return This event's priority. This value must not change for the event. */
 		int getPriority();
 	}
 
 	/** Implements the trivial parts of {@link Event} */
 	public static abstract class AbstractEvent implements Event
 	{
 		private final long theTime;
 
 		private final int thePriority;
 
 		private volatile boolean isHandling;
 
 		private volatile boolean isHandled;
 
 		private volatile boolean isDiscarded;
 
 		/** @param priority The priority of this event */
 		public AbstractEvent(int priority)
 		{
 			theTime = System.currentTimeMillis();
 			thePriority = priority;
 		}
 
 		@Override
 		public long getTime()
 		{
 			return theTime;
 		}
 
 		@Override
 		public boolean shouldHandle(long time)
 		{
 			return true;
 		}
 
 		@Override
 		public final void handle()
 		{
 			isHandling = true;
 			try
 			{
 				doHandleAction();
 			} finally
 			{
 				isHandled = true;
 				isHandling = false;
 			}
 		}
 
 		/** Executes this event's action */
 		protected abstract void doHandleAction();
 
 		@Override
 		public void discard()
 		{
 			isDiscarded = true;
 		}
 
 		@Override
 		public boolean isHandling()
 		{
 			return isHandling;
 		}
 
 		@Override
 		public boolean isFinished()
 		{
 			return isHandled || isDiscarded;
 		}
 
 		@Override
 		public boolean isDiscarded()
 		{
 			return isDiscarded;
 		}
 
 		@Override
 		public boolean isSupersededBy(Event evt)
 		{
 			return false;
 		}
 
 		@Override
 		public int getPriority()
 		{
 			return thePriority;
 		}
 	}
 
 	/** Represents an element's need to be redrawn */
 	public static class PaintEvent extends AbstractEvent
 	{
 		/** The priority of paint events */
 		public static final int PRIORITY = 0;
 
 		private final MuisElement theElement;
 
 		private final Rectangle theArea;
 
 		private final boolean isNow;
 
 		/**
 		 * @param element The element that needs to be repainted
 		 * @param area The area in the element that needs to be repainted, or null if the element needs to be repainted entirely
 		 * @param now Whether the repaint should happen quickly or be processed in normal time
 		 */
 		public PaintEvent(MuisElement element, Rectangle area, boolean now)
 		{
 			super(PRIORITY);
 			theElement = element;
 			theArea = area;
 			isNow = now;
 		}
 
 		/** @return The element that needs to be repainted */
 		public MuisElement getElement()
 		{
 			return theElement;
 		}
 
 		/** @return The area in the element that needs to be repainted, or null if the element needs to be repainted entirely */
 		public Rectangle getArea()
 		{
 			return theArea;
 		}
 
 		/** @return Whether this event is meant to happen immediately or in normal time */
 		public boolean isNow()
 		{
 			return isNow;
 		}
 
 		@Override
 		public boolean shouldHandle(long time)
 		{
 			return isNow || time >= theElement.getPaintDirtyTime() + MuisEventQueue.get().getPaintDirtyTolerance();
 		}
 
 		@Override
 		protected void doHandleAction()
 		{
 			Point trans = MuisUtils.relative(new Point(0, 0), null, theElement);
 			java.awt.Graphics2D graphics = theElement.getDocument().getGraphics();
 			graphics.translate(-trans.x, -trans.y);
 			theElement.paint(graphics, theArea);
 		}
 
 		@Override
 		public boolean isSupersededBy(Event evt)
 		{
 			if(!(evt instanceof PaintEvent))
 				return false;
 			PaintEvent paint = (PaintEvent) evt;
 
 			if(!MuisUtils.isAncestor(paint.theElement, theElement))
 				return false;
 			if(paint.theArea == null)
 				return true; // Element will be repainted with its ancestor
 
 			Rectangle area = MuisUtils.relative(paint.theArea, paint.theElement, theElement);
 			Rectangle area2 = theArea;
 			if(area2 != null)
 			{
 				if(area.x <= area2.x && area.y <= area2.y && area.x + area.width >= area2.x + area2.width
 					&& area.y + area.height >= area2.y + area2.height)
 					return true; // Element's area will be repainted with its ancestor
 			}
 			else if(area.x <= 0 && area.y <= 0 && area.x + area.width >= theElement.getWidth()
 				&& area.y + area.height >= theElement.getHeight())
 				return true; // Element will be repainted with its ancestor
 			return false;
 		}
 	}
 
 	/** Represents an element's need to lay out its children */
 	public static class LayoutEvent extends AbstractEvent
 	{
 		/** The priority of layout events */
 		public static final int PRIORITY = 10;
 
 		private final MuisElement theElement;
 
 		private final boolean isNow;
 
 		/**
 		 * @param element The element that needs to be layed out
 		 * @param now Whether the layout should happen quickly or be processed in normal time
 		 */
 		public LayoutEvent(MuisElement element, boolean now)
 		{
 			super(PRIORITY);
 			theElement = element;
 			isNow = now;
 		}
 
 		/** @return The element that needs to be layed out */
 		public MuisElement getElement()
 		{
 			return theElement;
 		}
 
 		/** @return Whether this event is meant to happen immediately or in normal time */
 		public boolean isNow()
 		{
 			return isNow;
 		}
 
 		@Override
 		public boolean shouldHandle(long time)
 		{
 			return isNow || time >= theElement.getLayoutDirtyTime() + MuisEventQueue.get().getLayoutDirtyTolerance();
 		}
 
 		@Override
 		protected void doHandleAction()
 		{
 			theElement.doLayout();
 		}
 
 		@Override
 		public boolean isSupersededBy(Event evt)
 		{
 			return evt instanceof LayoutEvent && ((LayoutEvent) evt).theElement == theElement;
 		}
 	}
 
 	/** Represents a request to set an element's bounds */
 	public static class ReboundEvent extends AbstractEvent
 	{
 		/** The priority of rebound events */
 		public static final int PRIORITY = 20;
 
 		private final MuisElement theElement;
 
 		private final Rectangle theBounds;
 
 		/**
 		 * @param element The element to set the bounds of
 		 * @param bounds The bounds to set on the element
 		 */
 		public ReboundEvent(MuisElement element, Rectangle bounds)
 		{
 			super(PRIORITY);
 			theElement = element;
 			theBounds = bounds;
 		}
 
 		/** @return The element whose bounds need to be set */
 		public MuisElement getElement()
 		{
 			return theElement;
 		}
 
 		/** @return The bounds that will be set on the element */
 		public Rectangle getBounds()
 		{
 			return theBounds;
 		}
 
 		@Override
 		protected void doHandleAction()
 		{
 			theElement.setBounds(theBounds.x, theBounds.y, theBounds.width, theBounds.height);
 		}
 	}
 
 	/** Represents a position event that was captured and needs to be propagated */
 	public static class PositionQueueEvent extends AbstractEvent
 	{
 		/** The priority of mouse events */
 		public static final int PRIORITY = 100;
 
 		private final MuisElement theRoot;
 
 		private final org.muis.core.event.PositionedUserEvent theEvent;
 
 		private final boolean isDownward;
 
 		/**
 		 * @param root The root from which this event fires downward or to which it fires upward
 		 * @param evt The position event to propagate
 		 * @param downward Whether this event fires downward from the root to the deepest level or the reverse
 		 */
 		public PositionQueueEvent(MuisElement root, org.muis.core.event.PositionedUserEvent evt, boolean downward)
 		{
 			super(PRIORITY);
 			theRoot = root;
 			theEvent = evt;
 			isDownward = downward;
 		}
 
 		/** @return The root from which this event fires downward or to which it fires upward */
 		public MuisElement getRoot()
 		{
 			return theRoot;
 		}
 
 		/** @return The event to be propagated */
 		public org.muis.core.event.PositionedUserEvent getEvent()
 		{
 			return theEvent;
 		}
 
 		/** @return Whether this event fires downward from the root to the deepest level or the reverse */
 		public boolean isDownward()
 		{
 			return isDownward;
 		}
 
 		@Override
 		protected void doHandleAction()
 		{
 			if(theEvent.getCapture() == null) // Non-positioned event
 			{
 				if(isDownward)
 					for(MuisElement pathEl : MuisUtils.path(theEvent.getElement()))
 						pathEl.fireEvent(theEvent, pathEl != theEvent.getElement(), false);
 				else
 				{
 					MuisElement el = theEvent.getElement();
 					while(el != null)
 					{
 						el.fireEvent(theEvent, el != theEvent.getElement(), false);
 						el = el.getParent();
 					}
 				}
 			}
 			else
 				for(MuisElementCapture el : theEvent.getCapture().iterate(!isDownward))
					el.element.fireEvent(theEvent, theEvent.isCanceled(), false);
 		}
 	}
 
 	/** Represents a non-positioned user event that needs to be fired */
 	public static class UserQueueEvent extends AbstractEvent
 	{
 		/** The priority of user events */
 		public static int PRIORITY = 100;
 
 		private final org.muis.core.event.UserEvent theEvent;
 
 		private final boolean isDownward;
 
 		/**
 		 * @param evt The event to fire
 		 * @param downward Whether the event should fire from root to deepest element or the reverse
 		 */
 		public UserQueueEvent(org.muis.core.event.UserEvent evt, boolean downward)
 		{
 			super(PRIORITY);
 			theEvent = evt;
 			isDownward = downward;
 		}
 
 		/** @return The event that needs to be fired */
 		public org.muis.core.event.UserEvent getEvent()
 		{
 			return theEvent;
 		}
 
 		/** @return Whether the event will be fired from root to deepest element or the reverse */
 		public boolean isDownward()
 		{
 			return isDownward;
 		}
 
 		@Override
 		protected void doHandleAction()
 		{
 			if(isDownward)
 				for(MuisElement pathEl : MuisUtils.path(theEvent.getElement()))
 					pathEl.fireEvent(theEvent, pathEl != theEvent.getElement(), false);
 			else
 			{
 				MuisElement el = theEvent.getElement();
 				while(el != null)
 				{
 					el.fireEvent(theEvent, el != theEvent.getElement(), false);
 					el = el.getParent();
 				}
 			}
 		}
 	}
 
 	private static MuisEventQueue theInstance = new MuisEventQueue();
 
 	/** @return The instance of the queue to use to schedule core events */
 	public static MuisEventQueue get()
 	{
 		return theInstance;
 	}
 
 	private Event [] theEvents;
 
 	private java.util.Comparator<Event> theComparator;
 
 	final Object theLock;
 
 	private volatile Thread theThread;
 
 	private volatile boolean isShuttingDown;
 
 	volatile boolean isInterrupted;
 
 	private long theFrequency;
 
 	private long thePaintDirtyTolerance;
 
 	private long theLayoutDirtyTolerance;
 
 	private boolean isPrioritized;
 
 	private MuisEventQueue()
 	{
 		theEvents = new Event[0];
 		theComparator = new java.util.Comparator<Event>() {
 			@Override
 			public int compare(Event o1, Event o2)
 			{
 				int diff = o1.getPriority() - o2.getPriority();
 				if(diff != 0)
 					return diff;
 				long timeDiff = o1.getTime() - o2.getTime();
 				return timeDiff < 0 ? -1 : (timeDiff > 0 ? 1 : 0);
 			}
 		};
 		theLock = new Object();
 		theFrequency = 50;
 		thePaintDirtyTolerance = 10;
 		theLayoutDirtyTolerance = 10;
 		isPrioritized = true;
 	}
 
 	/**
 	 * Schedules an event
 	 *
 	 * @param event The event that MUIS needs to take action on
 	 * @param now Whether to take action on the event immediately or allow it to execute when the queue gets to it
 	 */
 	public void scheduleEvent(Event event, boolean now)
 	{
 		Event [] events = theEvents;
 		for(Event evt : events)
 		{
 			if(evt.isHandling() || evt.isFinished())
 				continue;
 			if(event.isSupersededBy(evt))
 			{
 				event.discard();
 				return;
 			}
 			else if(evt.isSupersededBy(event))
 			{
 				evt.discard();
 				remove(evt);
 			}
 		}
 		addEvent(event, now);
 	}
 
 	private void addEvent(Event event, boolean now)
 	{
 		synchronized(theLock)
 		{
 			int spot;
 			if(isPrioritized)
 			{
 				spot = java.util.Arrays.binarySearch(theEvents, event, theComparator);
 				if(spot < 0)
 					spot = -(spot + 1);
 			}
 			else
 				spot = theEvents.length;
 			theEvents = prisms.util.ArrayUtils.add(theEvents, event, spot);
 		}
 		start();
 		if(now)
 		{
 			isInterrupted = true;
 			if(theThread != null)
 				theThread.interrupt();
 		}
 	}
 
 	void remove(Event event)
 	{
 		synchronized(theLock)
 		{
 			theEvents = prisms.util.ArrayUtils.remove(theEvents, event);
 		}
 	}
 
 	Event [] getEvents()
 	{
 		return theEvents;
 	}
 
 	private void start()
 	{
 		if(theThread != null)
 			return;
 		new EventQueueThread().start();
 	}
 
 	/**
 	 * Can't think why this should be called, but if it's needed we can change the modifier to public or provide some mechanism to access it
 	 */
 	@SuppressWarnings("unused")
 	private void shutdown()
 	{
 		isShuttingDown = true;
 	}
 
 	/** @return Whether this event queue is currently running */
 	public boolean isRunning()
 	{
 		return theThread != null;
 	}
 
 	/** @return Whether this event queue is shutting down. Not currently used. */
 	public boolean isShuttingDown()
 	{
 		return isShuttingDown;
 	}
 
 	/** @return The frequency with which this event queue handles its events */
 	public long getFrequency()
 	{
 		return theFrequency;
 	}
 
 	/**
 	 * @return The amount of time for which this queue will let paint events rest until {@link MuisElement#repaint(Rectangle, boolean)}
 	 *         stops being called repeatedly
 	 */
 	public long getPaintDirtyTolerance()
 	{
 		return thePaintDirtyTolerance;
 	}
 
 	/**
 	 * @return The amount of time for which this queue will let layout events rest until {@link MuisElement#relayout(boolean)} stops being
 	 *         called repeatedly
 	 */
 	public long getLayoutDirtyTolerance()
 	{
 		return theLayoutDirtyTolerance;
 	}
 
 	private class EventQueueThread extends Thread
 	{
 		EventQueueThread()
 		{
 			super("MUIS Event Queue");
 		}
 
 		/**
 		 * The queue threads action is to go through the events in the queue. When an event is come to, the dirty paint or layout state of
 		 * the element is checked. If not dirty, remove the event and do nothing. If the dirty time is <=10ms ago, skip the event so that it
 		 * is checked again the next 50ms. Heavy, multiple-op processes on elements from external threads will cause few layout/redraw
 		 * actions this way, but after ops finish, layout/redraw will happen within 60ms, average 35ms.
 		 */
 		@Override
 		public void run()
 		{
 			synchronized(theLock)
 			{
 				if(theThread != null)
 					return;
 				theThread = this;
 			}
 			while(!isShuttingDown())
 				try
 				{
 					isInterrupted = false;
 					Event [] events = getEvents();
 					boolean acted = false;
 					long now = System.currentTimeMillis();
 					for(Event evt : events)
 					{
 						if(isInterrupted)
 							break;
 						if(evt.isFinished())
 							continue;
 						if(evt.shouldHandle(now))
 						{
 							acted = true;
 							remove(evt);
 							evt.handle();
 							now = System.currentTimeMillis(); // Update the time, since the action may have taken some
 						}
 					}
 					if(!acted && !isInterrupted)
 						Thread.sleep(getFrequency());
 				} catch(InterruptedException e)
 				{
 				}
 			theThread = null;
 			isShuttingDown = false;
 		}
 	}
 }
