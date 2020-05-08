 /*
  * Copyright (c) 2008-2009 by Xuggle Inc. All rights reserved.
  *
  * It is REQUESTED BUT NOT REQUIRED if you use this library, that you let 
  * us know by sending e-mail to info@xuggle.com telling us briefly how you're
  * using the library and what you like or don't like about it.
  *
  * This library is free software; you can redistribute it and/or modify it under the
  * terms of the GNU Lesser General Public License as published by the Free Software
  * Foundation; either version 2.1 of the License, or (at your option) any later
  * version.
  *
  * This library is distributed in the hope that it will be useful, but WITHOUT ANY
  * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
  * PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public License along
  * with this library; if not, write to the Free Software Foundation, Inc.,
  * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
  */
 package com.xuggle.utils.event;
 
 import java.lang.ref.ReferenceQueue;
 import java.lang.ref.WeakReference;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.ListIterator;
 import java.util.Map;
 import java.util.Queue;
 import java.util.Set;
 import java.util.SortedMap;
 import java.util.TreeMap;
 import java.util.concurrent.ConcurrentLinkedQueue;
 import java.util.concurrent.atomic.AtomicLong;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * A synchronous implementation of {@link IEventDispatcher}.  This
  * implementation is not thread safe; i.e. if multiple threads try
  * dispatching at the same time, correctness is not guaranteed.
  * 
  * <p>
  * 
  * This method will guarantee that another event will not be dispatched
  * until all handlers for the current event being dispatched have been called
  * and returned.
  * 
  * </p>
  * <p>
  * 
  * This means that, even if a handler causes another event to be dispatched,
  * the new event will be queued until the current handler completely
  * unwinds.
  * 
  * </p> 
  */
 
 public class SynchronousEventDispatcher implements IEventDispatcher
 {
   private class HandlerReference extends WeakReference<IEventHandler<? extends IEvent>>
   {
     private final Class<? extends IEvent> mClass;
     private final int mPriority;
     private final boolean mKeepingWeakReference;
     public HandlerReference(IEventHandler<? extends IEvent> referent,
         ReferenceQueue<IEventHandler<? extends IEvent>> q,
             int priority, Class<? extends IEvent> clazz,
             boolean useWeakReference
     )
     {
       super(referent, q);
       mClass = clazz;
       mPriority = priority;
       mKeepingWeakReference = useWeakReference; 
     }
 
     public Class<? extends IEvent> getEventClass()
     {
       return mClass;
     }
 
     public int getPriority()
     {
       return mPriority;
     }
 
     public boolean isKeepingWeakReference()
     {
       return mKeepingWeakReference;
     }
   }
   final private Logger log = LoggerFactory.getLogger(this.getClass());
 
   /**
    * Here's the data structure type
    * 
    *   A map of class names to:
    *      A Map (because it can be sparse) of Priority to a list of Event Handlers
    *        A list of event Handlers
    */
 
   private final Map<IEventHandler<? extends IEvent>,
    Queue<IEventHandler<? extends IEvent>>> mStrongReferences;
   private final Map<String, SortedMap<Integer,
   List<HandlerReference>>> mHandlers;
   
   private final AtomicLong mNumNestedEventDispatches;
   private final Queue<IEvent> mPendingEventDispatches;
 
   private ReferenceQueue<IEventHandler<? extends IEvent>> mReferenceQueue;
 
   public SynchronousEventDispatcher()
   {
     mNumNestedEventDispatches = new AtomicLong(0);
     mPendingEventDispatches = new ConcurrentLinkedQueue<IEvent>();
     mStrongReferences = new HashMap<IEventHandler<? extends IEvent>,
     Queue<IEventHandler<? extends IEvent>>>(); 
     mHandlers = new HashMap<String, SortedMap<Integer,
       List<HandlerReference>>>();
     mReferenceQueue = new ReferenceQueue<IEventHandler<? extends IEvent>>();
     log.trace("<init>");
   }
   
   public void addEventHandler(int priority,
       Class<? extends IEvent> eventClass,
       IEventHandler<? extends IEvent> handler)
   {
     addEventHandler(priority, eventClass, handler, false);
   }
   public void addEventHandler(int priority,
       Class<? extends IEvent> eventClass,
       IEventHandler<? extends IEvent> handler,
       boolean useWeakReferences)
   {
     if (eventClass == null)
       throw new IllegalArgumentException("cannot pass null class");
     if (handler == null)
       throw new IllegalArgumentException("cannot pass null handler");
     
     String className = eventClass.getName();
     if (className == null || className.length() <= 0)
       throw new IllegalArgumentException("cannot get name of class");
    
     HandlerReference reference = new HandlerReference(handler, 
         mReferenceQueue,
         priority,
         eventClass,
         useWeakReferences);
     synchronized(mHandlers)
     {
       SortedMap<Integer, List<HandlerReference>> priorities
         = mHandlers.get(className);
       if (priorities == null)
       {
         priorities = new TreeMap<Integer, List<HandlerReference>>();
         mHandlers.put(className, priorities);
       }
 
       List<HandlerReference> handlers = priorities.get(priority);
       if (handlers == null)
       {
         handlers = new ArrayList<HandlerReference>();
         priorities.put(priority, handlers);
       }
       handlers.add(reference);
       if (!useWeakReferences) {
         Queue<IEventHandler<? extends IEvent>> refs =
           mStrongReferences.get(handler);
         if (refs == null) {
           refs = new LinkedList<IEventHandler<? extends IEvent>>();
           mStrongReferences.put(handler, refs);
         }
         refs.offer(handler);
       }
       // and we're done.
     }
     
     // now outside the lock, communicate what just happened
     dispatchEvent(new AddEventHandlerEvent(this, priority, eventClass, handler,
         useWeakReferences));
   }
 
   @SuppressWarnings("unchecked")
   public void dispatchEvent(IEvent event)
   {
     long dispatcherNum = mNumNestedEventDispatches.incrementAndGet();
     try
     {
       if (event == null)
         throw new IllegalArgumentException("cannot dispatch null event");
       
       //log.debug("dispatching event: {}", event);
       mPendingEventDispatches.add(event);
       // don't process a dispatch if nested within a dispatchEvent() call;
       // wait for the stack to unwind, and then process it.
       while(dispatcherNum == 1
           && (event = mPendingEventDispatches.poll()) != null)
       {
         boolean eventHandled = false;
         Queue<IEventHandler<? extends IEvent>> handlers =
           new LinkedList<IEventHandler<? extends IEvent>>();
         
         // First, determine all the valid handlers
         
         // find our registered handlers.
         String className = event.getClass().getName();
         if (className == null)
           throw new IllegalArgumentException("cannot get class name for event");
 
         // self handlers ARE ALWAYS called first
         if (event instanceof ISelfHandlingEvent)
         {
           ISelfHandlingEvent<? extends IEvent> selfHandlingEvent =
             (ISelfHandlingEvent<? extends IEvent>)event;
           
           handlers.add(selfHandlingEvent);
         }
         synchronized(mHandlers)
         {
           Map<Integer, List<HandlerReference>> priorities
             = mHandlers.get(className);
           if (priorities != null)
           {
             Set<Integer> priorityKeys = priorities.keySet();
             Iterator<Integer> orderedKeys = priorityKeys.iterator();
             while(orderedKeys.hasNext())
             {
               Integer priority = orderedKeys.next();
               List<HandlerReference> priHandlers
                 = priorities.get(priority);
               if (priHandlers != null)
               {
                 for(HandlerReference reference : priHandlers)
                 {
                   IEventHandler<? extends IEvent> handler = reference.get();
                   if (handler != null)
                     handlers.add(handler);
                 }
               }
             }
           }
         }
         //log.debug("Handling event: {} with {} handlers", event, handlers.size());
         Iterator<IEventHandler<? extends IEvent>> handlersIter = handlers.iterator();
         while(!eventHandled && handlersIter.hasNext())
         {
           // deliberately untyped!
           IEventHandler handler = handlersIter.next();
           //log.debug("Handling event: {} with handler: {}", event, handler);
           eventHandled = handler.handleEvent(this, event);
         }
         //log.debug("Handling event: {} done", event);
         
         // and finish by checking our reference queue and removing any event
         // handlers that are now dead.
         HandlerReference deadRef;
         while((deadRef = (HandlerReference)mReferenceQueue.poll()) != null)
           removeDeadHandler(deadRef);
       }
     }
     finally
     {
       mNumNestedEventDispatches.decrementAndGet();
     }
   }
 
   private void removeDeadHandler(HandlerReference deadRef)
   {
     Class<? extends IEvent> eventClass = deadRef.getEventClass();
     int priority = deadRef.getPriority();
     
     if (eventClass == null)
       throw new IllegalArgumentException("cannot pass null class");
 
     String className = eventClass.getName();
     if (className == null || className.length() <= 0)
       throw new IllegalArgumentException("cannot get name of class");
     synchronized(mHandlers)
     {
       Map<Integer, List<HandlerReference>> priorities
         = mHandlers.get(className);
       if (priorities == null)
       {
         // could not find entry in list
         return;
       }
 
       List<HandlerReference> handlers = priorities.get(priority);
       if (handlers == null)
       {
         // could not find entry in list
         return;
       }
 
       ListIterator<HandlerReference> iter = handlers.listIterator();
       while(iter.hasNext())
       {
         HandlerReference registeredHandlerReference = iter.next();
         if (registeredHandlerReference == deadRef)
         {
           iter.remove();
         }
       }
       if (handlers.size() ==0)
       {
         // All handlers were removed for this priority; clean up.
         priorities.remove(priority);
       }
       if (priorities.size() == 0)
       {
         // all priorities were removed for this class; clean up
         mHandlers.remove(className);
       }
     }
     // now outside the lock, communicate what just happened
     dispatchEvent(new RemoveEventHandlerEvent(this, priority, eventClass,
         null));
 
   }
 
   public void removeEventHandler(int priority,
       Class<? extends IEvent> eventClass,
       IEventHandler<? extends IEvent> handler) throws IndexOutOfBoundsException
   {
     if (eventClass == null)
       throw new IllegalArgumentException("cannot pass null class");
     if (handler == null)
       throw new IllegalArgumentException("cannot pass null handler");
     
     String className = eventClass.getName();
     if (className == null || className.length() <= 0)
       throw new IllegalArgumentException("cannot get name of class");
     synchronized(mHandlers)
     {
       Map<Integer, List<HandlerReference>> priorities
         = mHandlers.get(className);
       if (priorities == null)
       {
         // could not find entry in list
         throw new IndexOutOfBoundsException();
       }
 
       List<HandlerReference> handlers = priorities.get(priority);
       if (handlers == null)
       {
         // could not find entry in list
         throw new IndexOutOfBoundsException();
       }
 
       ListIterator<HandlerReference> iter = handlers.listIterator();
       // Walk through and remove all copies of this handler.
       // someone may have registered multiple copies of the same
       // handler, and we nuke them all
       int handlersNuked = 0;
       while(iter.hasNext())
       {
         HandlerReference registeredHandlerReference = iter.next();
         IEventHandler<? extends IEvent> registeredHandler = 
           registeredHandlerReference.get();
         if (registeredHandler == handler)
         {
           iter.remove();
           ++handlersNuked;
          if (!registeredHandlerReference.isKeepingWeakReference())
           {
             Queue<IEventHandler<? extends IEvent>> refs =
               mStrongReferences.get(handler);
             if (refs != null)
             {
               // get ride of one reference we kept
               refs.poll();
               // and kill the index entry if this is the last
               // reference
               if (refs.size() == 0)
                 mStrongReferences.remove(registeredHandler);
             }
           }
          // and we're done; break the loop
          break;
         }
       }
       if (handlersNuked == 0)
       {
         // could not find entry in list
         throw new IndexOutOfBoundsException();      
       }
       if (handlers.size() ==0)
       {
         // All handlers were removed for this priority; clean up.
         priorities.remove(priority);
       }
       if (priorities.size() == 0)
       {
         // all priorities were removed for this class; clean up
         mHandlers.remove(className);
       }
     }
     // now outside the lock, communicate what just happened
     dispatchEvent(new RemoveEventHandlerEvent(this, priority, eventClass, handler));
 
   }
 
 }
