 /*
  * Copyright 2009 JBoss, a divison Red Hat, Inc
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *    http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package org.jboss.errai.common.client.types;
 
 import org.jboss.errai.common.client.types.handlers.collections.*;
 import org.jboss.errai.common.client.types.handlers.numbers.*;
 
 import java.util.*;
 
 public class TypeHandlerFactory {
     private static Map<Class, Map<Class, TypeHandler>> handlers =
             new HashMap<Class, Map<Class, TypeHandler>>();
 
     private static Map<Class, Class> inheritanceMap = new HashMap<Class, Class>();
 
     static {
         /**
          * Declare all the default coercion handlers.
          */
         Map<Class, TypeHandler> collectionHandlers = new HashMap<Class, TypeHandler>();
         collectionHandlers.put(Object[].class, new CollectionToObjArray());
         collectionHandlers.put(String[].class, new CollectionToStringArray());
         collectionHandlers.put(Integer[].class, new CollectionToIntArray());
         collectionHandlers.put(Long[].class, new CollectionToLongArray());
         collectionHandlers.put(Boolean[].class, new CollectionToBooleanArray());
         collectionHandlers.put(Double[].class, new CollectionToDoubleArray());
 
         collectionHandlers.put(int[].class, new CollectionToIntArray());
         collectionHandlers.put(long[].class, new CollectionToLongArray());
         collectionHandlers.put(boolean[].class, new CollectionToBooleanArray());
         collectionHandlers.put(double[].class, new CollectionToDoubleArray());
 
         collectionHandlers.put(Set.class, new CollectionToSet());
         collectionHandlers.put(List.class, new CollectionToList());
 
         handlers.put(Collection.class, collectionHandlers);
 
         Map<Class, TypeHandler> numberHandlers = new HashMap<Class, TypeHandler>();
         numberHandlers.put(Integer.class, new NumberToInt());
         numberHandlers.put(Long.class, new NumberToLong());
         numberHandlers.put(Short.class, new NumberToShort());
         numberHandlers.put(Float.class, new NumberToFloat());
         numberHandlers.put(Double.class, new NumberToFloat());
         numberHandlers.put(Byte.class, new NumberToByte());
         numberHandlers.put(java.util.Date.class, new NumberToDate());
         numberHandlers.put(java.sql.Date.class, new NumberToSQLDate());
 
         handlers.put(Number.class, numberHandlers);
 
         /**
          * Build an inheretence Map.
          */
         inheritanceMap.put(Integer.class, Number.class);
         inheritanceMap.put(Long.class, Number.class);
         inheritanceMap.put(Short.class, Number.class);
         inheritanceMap.put(Float.class, Number.class);
         inheritanceMap.put(Double.class, Number.class);
 
         inheritanceMap.put(ArrayList.class, List.class);
         inheritanceMap.put(LinkedList.class, List.class);
         inheritanceMap.put(AbstractList.class, List.class);
         inheritanceMap.put(Stack.class, List.class);
 
         inheritanceMap.put(HashSet.class, Set.class);
         inheritanceMap.put(AbstractSet.class, Set.class);
 
         inheritanceMap.put(Set.class, Collection.class);
         inheritanceMap.put(List.class, Collection.class);
     }
 
     public static Map<Class, TypeHandler> getHandler(Class from) {
         Map<Class, TypeHandler> toHandlers = handlers.get(from);
         if (toHandlers == null && inheritanceMap.containsKey(from)) {
             toHandlers = getHandler(inheritanceMap.get(from));
         }
         return toHandlers;
     }
 
     public static <T> T convert(final Class from, final Class<? extends T> to, final Object value) {
         if (value.getClass() == to) return (T) value;
         Map<Class, TypeHandler> toHandlers = getHandler(from);
         if (toHandlers == null) {
             if (value instanceof String) {
                 TypeHandler<String, T> th = new TypeHandler<String, T>() {
                     public T getConverted(String in) {
                         return (T) Enum.valueOf((Class<? extends Enum>) to, in);
                     }
                 };
                 try {
                    T val = th.getConverted(String.valueOf(value));
                    addHandler(from, to, th);
                    return val;
                 }
                 catch (Exception e) {
                    return (T) value;
                 }
             }
 
             return (T) value;
         }
         TypeHandler handler = toHandlers.get(to);
         if (handler == null) {
             return (T) value;
         }
         return (T) handler.getConverted(value);
     }
 
     private static final Map<Class, Map<Class, Boolean>> assignableCache = new HashMap<Class, Map<Class, Boolean>>();
 
     public static Boolean checkCache(Class from, Class to) {
         synchronized (assignableCache) {
             Map<Class, Boolean> assignableTable = assignableCache.get(from);
             if (assignableTable != null) {
                 return assignableTable.get(to);
             } else {
                 return null;
             }
         }
     }
 
     public static boolean recordCache(Class from, Class to, boolean result) {
         synchronized (assignableCache) {
             Map<Class, Boolean> assignableTable = assignableCache.get(from);
             if (assignableTable == null) {
                 assignableCache.put(from, assignableTable = new HashMap<Class, Boolean>());
             }
             assignableTable.put(to, result);
             return result;
         }
     }
 
     public static boolean gwtSafe_IsAssignableFrom(Class from, Class to) {
         Boolean cache = checkCache(from, to);
         if (cache != null) return cache;
 
         do {
             if (from == to) return recordCache(from, to, true);
         } while ((from = from.getSuperclass()) != null);
 
         return recordCache(from, to, false);
     }
 
     public static void addHandler(Class from, Class to, TypeHandler handler) {
         if (!handlers.containsKey(from)) {
             handlers.put(from, new HashMap<Class, TypeHandler>());
         }
         handlers.get(from).put(to, handler);
     }
 }
