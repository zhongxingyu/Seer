 package org.mule.galaxy.impl.event;
 
 import java.lang.annotation.Annotation;
 import java.lang.reflect.Method;
 import java.text.MessageFormat;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.LinkedHashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.mule.galaxy.event.EventManager;
 import org.mule.galaxy.event.GalaxyEvent;
 import org.mule.galaxy.event.annotation.BindToEvent;
 import org.mule.galaxy.event.annotation.BindToEvents;
 import org.mule.galaxy.event.annotation.OnEvent;
 import org.springframework.aop.support.AopUtils;
 import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
 import org.springmodules.jcr.SessionFactory;
 
 public class DefaultEventManager implements EventManager {
 
     protected final Log logger = LogFactory.getLog(getClass());
 
     protected final Object listenersLock = new Object();
 
    protected LinkedHashMap<Class, List<InternalGalaxyEventListener>> event2listeners = new LinkedHashMap<Class, List<InternalGalaxyEventListener>>();
 
     protected Map<Object, List<InternalGalaxyEventListener>> listeners = new HashMap<Object, List<InternalGalaxyEventListener>>();
      
     /**
      * Use Spring's wrapper around TPTE, exposes config properties as a JavaBean.
      */
     private ThreadPoolTaskExecutor executor;
 
     private SessionFactory sessionFactory;
     
     public DefaultEventManager(final List<?> newListeners, 
                                final SessionFactory sessionFactory,
                                final ThreadPoolTaskExecutor executor) {
         this.sessionFactory = sessionFactory;
         this.executor = executor;
         for (Object listener : newListeners) {
             addListener(listener);
         }
     }
 
     public void addListener(Object listenerCandidate) {
         if (listenerCandidate == null) {
             throw new IllegalArgumentException("Listener can't be null");
         }
 
         // get event binding annotation
         final Class<?> clazz;
 
         if (AopUtils.isAopProxy(listenerCandidate)) {
             clazz = AopUtils.getTargetClass(listenerCandidate);
         } else {
             clazz = listenerCandidate.getClass();
         }
         
         final String[] eventNames;
         InternalGalaxyEventListener adapter = null;
 
         // single-event listeners
         final Annotation annotation = findAnnotation(clazz, BindToEvent.class);
         if (annotation != null) {
             eventNames = new String[] {((BindToEvent) annotation).value()};
 
             adapter = getListener(listenerCandidate, clazz, adapter, annotation);
             
             // no OnEvent annotation found, fail
             if (adapter == null) {
                 throw new IllegalArgumentException(String.format("Listener %s is missing an @OnEvent entry point",
                                                                  listenerCandidate.getClass().getName()));
             }
         } else if (clazz.isAnnotationPresent(BindToEvents.class)) {
             // multi-event listeners
             eventNames = clazz.getAnnotation(BindToEvents.class).value();
             adapter = new DelegatingMultiEventListener(listenerCandidate, executor, sessionFactory);
         } else {
             throw new IllegalArgumentException(clazz.getName() + " doesn't have a BindToEvent(s) annotation");
         }
         
         for (String eventName : eventNames) {
             registerListener(listenerCandidate, adapter, eventName);
         }
 
         //synchronized (listenersLock) {
             // TODO reimplement
             // check for duplicate registration, this could be a programming error
             // due to a missing paired call of removeListener() earlier
             //for (Iterator<GalaxyEventListener> it = listeners.iterator(); it.hasNext();) {
             //    GalaxyEventListener reference = it.next();
             //    if (listener == reference) {
             //        System.out.println(">>> Listener " + listener + " already registered. Duplicate listenerCandidate registration could be a " +
             //                           "programming error due to a missing removeListener() call earlier. Ignoring this request.");
             //    }
             //}
             //listeners.add(listener);
         //}
     }
 
     private InternalGalaxyEventListener getListener(Object listenerCandidate, 
                                                     final Class<?> clazz,
                                                     InternalGalaxyEventListener adapter,
                                                     final Annotation annotation) {
         Method[] methods = clazz.getMethods();
         for (final Method method : methods) {
             if (method.isAnnotationPresent(OnEvent.class)) {
                 // detect duplicate entry-points
                 if (adapter != null) {
                     throw new IllegalArgumentException("Multiple @OnEvent entry-points detected for " + clazz.getName());
                 }
                 adapter = new DelegatingSingleEventListener(annotation, listenerCandidate, method, executor, sessionFactory);
             }
         }
         return adapter;
     }
 
     /**
      * Check for annotation presence <strong>with inheritance</strong>. I.e. if a subclass doesn't have an annotation,
      * but extends a class having one, this method would report the subclass as having the annotation.
      * @return annotation or null if none found
      */
     protected Annotation findAnnotation(Class<?> clazz, final Class<? extends Annotation> annotation) {
         boolean annotationPresent = clazz.isAnnotationPresent(annotation);
         // doesn't handle cases when a listener implements an interface which has an annotation
         while (!annotationPresent && clazz.getSuperclass() != null) {
             clazz = clazz.getSuperclass();
             annotationPresent = clazz.isAnnotationPresent(annotation);
         }
 
         return annotationPresent ? clazz.getAnnotation(annotation) : null;
     }
 
     protected void registerListener(Object listener, 
                                     final InternalGalaxyEventListener adapter, 
                                     final String eventName) {
 
         if (adapter == null) {
             throw new IllegalArgumentException(
                     String.format("Attempt detected to register a null listener for %s event", eventName));
         }
 
         // get event name and load its class
         ClassLoader current = Thread.currentThread().getContextClassLoader();
         List<InternalGalaxyEventListener> allAdapters = new ArrayList<InternalGalaxyEventListener>();
         synchronized (listenersLock) {
             Class<? extends GalaxyEvent> eventClass = null;
             
             try {
                 eventClass = Class.forName(eventName, true, current).asSubclass(GalaxyEvent.class);
             } catch (ClassNotFoundException e) {
                 // Try another method below
             }
             
 
             if (eventClass == null) {
                 String evtClassName = "org.mule.galaxy.event." + eventName + "Event";
                 try {
                     eventClass = Class.forName(evtClassName, true, current).asSubclass(GalaxyEvent.class);
                 } catch (ClassNotFoundException e) {
                     final String realListenerClass = adapter instanceof DelegatingGalaxyEventListener
                             ? ((DelegatingGalaxyEventListener) adapter).getDelegateListener().getClass().getName()
                             : adapter.getClass().getName();
                     throw new IllegalArgumentException(String.format("Event class %s not found for listener %s",
                                                                      evtClassName, realListenerClass));
                 }
             }
             
             List<InternalGalaxyEventListener> evtListeners = event2listeners.get(eventClass);
             if (evtListeners == null) {
                 evtListeners = new LinkedList<InternalGalaxyEventListener>();
             }
             evtListeners.add(adapter);
             event2listeners.put(eventClass, evtListeners);
             allAdapters.add(adapter);
             
             if (logger.isDebugEnabled()) {
                 Object listenerObj = adapter instanceof DelegatingGalaxyEventListener
                         ? ((DelegatingGalaxyEventListener) adapter).getDelegateListener()
                         : adapter;
 
                 final String message =
                         MessageFormat.format("Registered {0} as a listener for {1}", listenerObj, eventClass.getName());
                 logger.debug(message);
             }
         
             listeners.put(listener, allAdapters);
         }
     }
     
     public List<Object> getListeners() {
         return new ArrayList<Object>(listeners.keySet());
     }
 
     public void removeListener(Object listener) {
         synchronized (listenersLock) {
            List<InternalGalaxyEventListener> adapters = listeners.get(listener);
             if (adapters != null) {
                 for (InternalGalaxyEventListener i : adapters) {
                     removeAdapter(i);
                 }
             }
         }
     }
 
     protected void removeAdapter(InternalGalaxyEventListener i) {
         for (List<InternalGalaxyEventListener> list : event2listeners.values()) {
             list.remove(i);
         }
     }
 
     public void fireEvent(final GalaxyEvent event) {
         synchronized (listenersLock) {
             List<InternalGalaxyEventListener> eventListeners = event2listeners.get(event.getClass());
 
             if (eventListeners != null && !eventListeners.isEmpty()) {
                 for (InternalGalaxyEventListener listener : eventListeners) {
                     try {
                         if (logger.isDebugEnabled()) {
                             logger.debug("Firing event: " + event);
                         }
                         listener.onEvent(event);
                     } catch (Throwable t) {
                         logger.error(String.format("Listener %s failed to process event %s",
                                                    listener.getClass().getName(),
                                                    event.getClass().getName()), t);
                     }
                 }
             } else {
                 if (logger.isDebugEnabled()) {
                     logger.debug("No listeners registered for " + event.getClass().getName() + ", ignoring");
                 }
             }
         }
     }
 
     public void destroy() {
         executor.shutdown();
     }
     
     public ThreadPoolTaskExecutor getExecutor() {
         return executor;
     }
     
 }
