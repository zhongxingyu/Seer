 package com.dafttech.eventmanager;
 
 import java.lang.annotation.Annotation;
 import java.lang.reflect.Field;
 import java.lang.reflect.Method;
 import java.lang.reflect.Modifier;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.NoSuchElementException;
 
 public class EventManager {
     public static final EventType WHITELIST = new EventType();
 
     volatile protected Map<EventType, List<EventListenerContainer>> registeredListeners = new HashMap<EventType, List<EventListenerContainer>>();
 
    // TODO JAVADOC!!!
     public EventManager() {
     }
 
     /**
      * Used to register an EventListener created with annotations to call the
      * annotated methods.
      * 
      * @param eventListener
      *            Object - Instance of the listening class
     * @param filter
     *            Object... - Sets the filter that is customizable in EventType
     *            subclasses
      */
     public final void registerEventListener(Object eventListener, EventType... blacklist) {
         if (blacklist.length == 1 && blacklist[0] == WHITELIST) return;
         boolean eventListenerStatic = eventListener.getClass() == Class.class;
         Class<?> eventListenerClass = eventListenerStatic ? (Class<?>) eventListener : eventListener.getClass();
         EventListener annotation = null;
         boolean isStatic = false;
         EventType typeFound = null;
         for (Method method : getAnnotatedMethods(eventListenerClass, EventListener.class, true, void.class, Event.class)) {
             annotation = method.getAnnotation(EventListener.class);
             isStatic = Modifier.isStatic(method.getModifiers());
             if (!eventListenerStatic || isStatic) {
                 for (String requestedEvent : annotation.value()) {
                     typeFound = EventType.types.get(requestedEvent);
                     if (typeFound != null) {
                         if (typeFound.isValidEventManager(this)
                                 && (blacklist.length == 0 || blacklist[0] == WHITELIST
                                         && Arrays.asList(blacklist).contains(typeFound) || blacklist[0] != WHITELIST
                                         && !Arrays.asList(blacklist).contains(typeFound))) {
                             addEventListenerContainer(typeFound, new EventListenerContainer(isStatic,
                                     isStatic ? eventListenerClass : eventListener, method, annotation));
                         }
                     } else {
                         throw new NoSuchElementException(requestedEvent);
                     }
                 }
             }
         }
     }
 
     public final void tryRegisterEventListener(String staticEventListener) {
         try {
             registerEventListener(Class.forName(staticEventListener));
         } catch (ClassNotFoundException e) {
         }
     }
 
     /**
      * Used to unregister an EventListener in all events.
      * 
      * @param eventListener
     *            Object - Instance of the listening class
      */
     public final void unregisterEventListener(EventType type, Object eventListener) {
         if (registeredListeners.containsKey(type)) {
             List<EventListenerContainer> eventListenerContainerList = registeredListeners.get(type);
             List<EventListenerContainer> eventListenerContainerListRead = new ArrayList<EventListenerContainer>(
                     eventListenerContainerList);
             for (EventListenerContainer eventListenerContainer : eventListenerContainerListRead) {
                 if (eventListenerContainer.eventListener == eventListener)
                     eventListenerContainerList.remove(eventListenerContainer);
             }
             if (eventListenerContainerList.size() == 0) registeredListeners.remove(type);
         }
     }
 
     private void addEventListenerContainer(EventType type, EventListenerContainer eventListenerContainer) {
         if (!registeredListeners.containsKey(type) || registeredListeners.get(type) == null)
             registeredListeners.put(type, new ArrayList<EventListenerContainer>());
         List<EventListenerContainer> eventListenerContainerList = registeredListeners.get(type);
         EventListenerContainer currEventListenerContainer;
         for (int i = 0; i < eventListenerContainerList.size(); i++) {
             currEventListenerContainer = eventListenerContainerList.get(i);
             if (currEventListenerContainer.equals(eventListenerContainer)) return;
             if (currEventListenerContainer.priority < eventListenerContainer.priority) {
                 eventListenerContainerList.add(i, eventListenerContainer);
                 return;
             }
         }
         eventListenerContainerList.add(eventListenerContainer);
     }
 
     /**
      * Calls this event and asks all registered EventListeners and sends the
      * objects to them.
      * 
      * @param objects
      *            Object... - You can send any objects to the registered
      *            classes.
      * @return Event - to manage the called event such as getting the output and
      *         checking if the event was cancelled
      */
     public final Event callSync(EventType type, Object... objects) {
         Event event = new Event(this, type, objects);
         event.schedule(registeredListeners.get(type));
         return event;
     }
 
     /**
      * Calls this event in another thread that has to be started with
      * eventManagerInstance.asyncEventQueue.start(). It asks all registered
      * EventListeners and sends the objects to them.
      * 
      * @param objects
      *            Object... - You can send any objects to the registered
      *            classes.
      * @return Event - to manage the called event such as checking if the event
      *         is done, getting the output and checking if the event was
      *         cancelled
      */
     public final Event callAsync(EventType type, Object... objects) {
         Event event = new Event(this, type, objects);
         new AsyncEventThread(event, registeredListeners.get(type));
         return event;
     }
 
     // STATIC METHODS
 
     public static final List<Method> getAnnotatedMethods(Class<?> targetClass, Class<? extends Annotation> annotation,
             boolean throwException, Class<?> reqType, Class<?>... reqArgs) {
         List<Method> methods = new ArrayList<Method>();
         for (Method method : getAllDeclaredMethods(targetClass)) {
             if (method.isAnnotationPresent(annotation)) {
                 if ((reqType == null || method.getReturnType() == reqType)
                         && (reqArgs.length == 1 && reqArgs[0] == null || Arrays.equals(method.getParameterTypes(), reqArgs))) {
                     methods.add(method);
                 } else if (throwException) {
                     String errorMessage = "\nat " + targetClass.getName() + " at Annotation " + annotation.getName() + ":";
                     errorMessage += "\nexpected: " + reqType.getName() + " with " + (reqArgs.length == 0 ? "no args" : "args:");
                     for (Class<?> arg : reqArgs)
                         errorMessage += ", " + arg.getName();
                     errorMessage += "\nand got:  " + method.getReturnType() + " with "
                             + (method.getParameterTypes().length == 0 ? "no args" : "args:");
                     for (Class<?> arg : method.getParameterTypes())
                         errorMessage += ", " + arg.getName();
                     errorMessage += ".";
                     throw new IllegalArgumentException(errorMessage);
                 }
             }
         }
         return methods;
     }
 
     public static final List<Field> getAnnotatedFields(Class<?> targetClass, Class<? extends Annotation> annotation,
             boolean throwException, Class<?> reqType) {
         List<Field> fields = new ArrayList<Field>();
         if (reqType == void.class) return fields;
         for (Field field : getAllDeclaredFields(targetClass)) {
             if (field.isAnnotationPresent(annotation)) {
                 if (reqType == null || field.getType() == reqType) {
                     fields.add(field);
                 } else if (throwException) {
                     String errorMessage = "\nat " + targetClass.getName() + " at Annotation " + annotation.getName() + ":";
                     errorMessage += "\nexpected: " + reqType.getName();
                     errorMessage += "\nand got:  " + field.getType();
                     errorMessage += ".";
                     throw new IllegalArgumentException(errorMessage);
                 }
             }
         }
         return fields;
     }
 
     public static List<Method> getAllDeclaredMethods(Class<?> targetClass) {
         return getAllDeclaredMethods(targetClass, null);
     }
 
     public static List<Field> getAllDeclaredFields(Class<?> targetClass) {
         return getAllDeclaredFields(targetClass, null);
     }
 
     private static List<Method> getAllDeclaredMethods(Class<?> targetClass, List<Method> methods) {
         if (methods == null) methods = new ArrayList<Method>();
         for (Method method : targetClass.getDeclaredMethods())
             if (!methods.contains(method)) {
                 method.setAccessible(true);
                 methods.add(method);
             }
         if (targetClass.getSuperclass() != null) getAllDeclaredMethods(targetClass.getSuperclass(), methods);
         return methods;
     }
 
     private static List<Field> getAllDeclaredFields(Class<?> targetClass, List<Field> fields) {
         if (fields == null) fields = new ArrayList<Field>();
         for (Field field : targetClass.getDeclaredFields())
             if (!fields.contains(field)) {
                 field.setAccessible(true);
                 fields.add(field);
             }
         if (targetClass.getSuperclass() != null) getAllDeclaredFields(targetClass.getSuperclass(), fields);
         return fields;
     }
 }
