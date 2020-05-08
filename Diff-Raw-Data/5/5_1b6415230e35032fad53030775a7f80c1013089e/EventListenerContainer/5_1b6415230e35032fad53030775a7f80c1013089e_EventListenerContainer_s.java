 package com.dafttech.eventmanager;
 
 import java.lang.reflect.Field;
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 import java.lang.reflect.Modifier;
 import java.util.ArrayList;
 import java.util.List;
 
 public class EventListenerContainer {
     volatile protected boolean isStatic;
     volatile protected Object eventListener;
     volatile protected Method method;
     volatile protected int priority;
 
     volatile private Object[] filters;
 
     protected EventListenerContainer(boolean isStatic, Object eventListener, Method method, EventListener annotation) {
         this.isStatic = isStatic;
         this.eventListener = eventListener;
         this.method = method;
         priority = annotation.priority();
         filters = getFilterContainer(isStatic, isStatic ? (Class<?>) this.eventListener : this.eventListener.getClass(), annotation.filter());
     }
 
     protected Object[][] getFilters() {
         Object[][] filterArray = new Object[filters.length][];
         Object filter, filterObj;
         for (int i = 0; i < filters.length; i++) {
             filter = filters[i];
             filterObj = null;
             if (filter != null) {
                 try {
                     if (filter instanceof Field) filterObj = ((Field) filter).get(isStatic ? null : eventListener);
                     if (filter instanceof Method) filterObj = ((Method) filter).invoke(isStatic ? null : eventListener);
                 } catch (IllegalAccessException e) {
                     e.printStackTrace();
                 } catch (IllegalArgumentException e) {
                     e.printStackTrace();
                 } catch (InvocationTargetException e) {
                     e.printStackTrace();
                 }
             }
             if (filterObj == null) {
                 filterArray[i] = new Object[0];
             } else if (filterObj instanceof Object[]) {
                 filterArray[i] = (Object[]) filterObj;
             } else {
                 filterArray[i] = new Object[] { filterObj };
             }
         }
         return filterArray;
     }
 
     @Override
     public boolean equals(Object paramObject) {
         if (paramObject instanceof EventListenerContainer) {
             return paramObject == this;
         } else {
             return paramObject == eventListener;
         }
     }
 
     private static final Object[] getFilterContainer(boolean isStatic, Class<?> filterClass, String[] filterNames) {
         List<Object> filterList = new ArrayList<Object>();
         String filterName;
         for (int i = 0; i < filterNames.length; i++) {
             filterName = filterNames[i];
             if (!filterName.equals("")) {
                 if (filterName.contains(".")) {
                     Class<?> remoteFilterClass = null;
                     try {
                         remoteFilterClass = Class.forName(filterName.substring(0, filterName.lastIndexOf('.')));
                     } catch (ClassNotFoundException e1) {
                         String packageName = filterClass.getPackage().getName();
                         while (filterName.contains("..") && packageName.contains(".")) {
                             filterName = filterName.replaceFirst("..", "");
                             packageName = packageName.substring(0, packageName.lastIndexOf('.'));
                         }
                         filterName = packageName + '.' + filterName;
                         try {
                             remoteFilterClass = Class.forName(filterName.substring(0, filterName.lastIndexOf('.')));
                         } catch (ClassNotFoundException e2) {
                             e1.printStackTrace();
                             e2.printStackTrace();
                         }
                     }
                     filterName = filterName.substring(filterName.lastIndexOf('.') + 1);
 
                     if (remoteFilterClass != null) {
                         filterClass = remoteFilterClass;
                         isStatic = true;
                     }
 
                 }
                 for (Field field : EventManager.getAnnotatedFields(filterClass, EventFilter.class, true, null)) {
                     if ((!isStatic || Modifier.isStatic(field.getModifiers())) && field.getAnnotation(EventFilter.class).value().equals(filterName))
                        filterList.set(filterList.size(), field);
                 }
                 for (Method method : EventManager.getAnnotatedMethods(filterClass, EventFilter.class, true, null)) {
                     if ((!isStatic || Modifier.isStatic(method.getModifiers())) && method.getAnnotation(EventFilter.class).value().equals(filterName))
                        filterList.set(filterList.size(), method);
                 }
             }
         }
         return filterList.toArray();
     }
 }
