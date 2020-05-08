 package com.dafttech.eventmanager;
 
 import java.lang.reflect.Field;
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 import java.lang.reflect.Modifier;
 
 public class EventListenerContainer {
     volatile protected Object eventListener;
     volatile protected boolean eventListenerStatic;
     volatile protected Method method;
     volatile protected int priority;
 
     volatile private Object filter;
 
     protected EventListenerContainer(Object eventListener, Method method, EventListener annotation) {
         this.eventListener = eventListener;
         this.eventListenerStatic = Modifier.isStatic(method.getModifiers());
         this.method = method;
         this.priority = annotation.priority();
         this.filter = getFilterContainer(eventListener, annotation.filter());
         if (eventListenerStatic && !(eventListener instanceof Class)) {
             this.eventListener = this.eventListener.getClass();
         }
     }
 
     protected Object[] getFilter() {
         try {
             if (filter != null) {
                 if (filter instanceof Field) return (Object[]) ((Field) filter).get(eventListener);
                 if (filter instanceof Method) return (Object[]) ((Method) filter).invoke(eventListener);
             }
         } catch (IllegalAccessException e) {
             e.printStackTrace();
         } catch (IllegalArgumentException e) {
             e.printStackTrace();
         } catch (InvocationTargetException e) {
             e.printStackTrace();
         }
         return new Object[0];
     }
 
     @Override
     public boolean equals(Object paramObject) {
         if (paramObject instanceof EventListenerContainer) {
             return paramObject == this;
         } else {
             return paramObject == eventListener;
         }
     }
 
     private static final Object getFilterContainer(Object eventListener, String filterName) {
         if (!filterName.equals("")) {
             for (Field field : EventManager.getAnnotatedFields(eventListener.getClass(), EventFilter.class, Object[].class)) {
                if (((EventFilter) field.getAnnotation(EventFilter.class)).name().equals(filterName)) return field;
             }
             for (Method method : EventManager.getAnnotatedMethods(eventListener.getClass(), EventFilter.class, Object[].class)) {
                if (((EventFilter) method.getAnnotation(EventFilter.class)).name().equals(filterName)) return method;
             }
         }
         return null;
     }
 }
