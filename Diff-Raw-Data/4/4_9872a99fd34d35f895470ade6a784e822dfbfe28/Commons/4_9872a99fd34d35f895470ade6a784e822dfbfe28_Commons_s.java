 package com.redshape.utils;
 
 import java.util.*;
 
 /**
  * Most common static functions.
  *
  * @author nikelin
  */
 public final class Commons {
 
     /**
      * Conditional select operator which select B or C dependent on E equality to null
      *
      * @param <T>
      * @param condition
      * @param first
      * @param second
      *
      * @return
      */
     public static <T> T select( Object condition, T first, T second ) {
         return condition != null ? first : second;
     }
 
     /**
      * Simple selector which is analogy for JS operator || in a case of binary value
      * selection ( a || b ).
      *
      * @param <T>
      * @param first
      * @param second
      * @return
      */
     public static <T> T select( T first, T second ) {
         return first == null ? second : first;
     }
     
     public static <T> Collection<T> sort( Collection<T> source, Comparator<T> cmp ) {
         return Arrays.asList( sort( (T[]) source.toArray(), cmp ) );
     }
 
     public static <T> List<T> sort( List<T> source, Comparator<T> cmp ) {
         return Arrays.asList( sort( (T[]) source.toArray(), cmp ) );
     }
     
     public static <T> T[] sort( T[] source, Comparator<T> cmp ) {
         Arrays.sort( source, cmp );
         return source;
     }
 
     public static void checkArgument( boolean argument ) {
         checkArgument(argument, "<false>");
     }
 
     public static void checkArgument( boolean argument, String message ) {
         if ( !argument ) {
             throw new IllegalArgumentException(message);
         }
     }
 
     public static void checkNotNull( Object... x ) {
        Commons.checkNotNull(x);
 
         for ( Object item : x ) {
             Commons.checkNotNull(item);
         }
     }
 
     public static void checkNotNull( Object x ) {
         checkNotNull(x, "<null>");
     }
 
     public static void checkNotNull( Object x, String assertMessage ) {
         if ( x == null ) {
             throw new IllegalArgumentException(assertMessage);
         }
     }
 
     @SuppressWarnings("unchecked")
     public static <K, V> Map<K, V> map( List<K> keys, List<V> values ) {
         return Commons.map( (K[]) keys.toArray(), (V[]) values.toArray() );
     }
 
     /**
      * Construct map from a given key and value collections.
      *
      * @param <K>
      * @param <V>
      * @param keys
      * @param values
      * @return
      */
     public static <K, V> Map<K, V> map( K[] keys, V[] values ) {
         Map<K, V> result = new HashMap<K, V>();
         for ( int i = 0; i < keys.length; i ++ ) {
             result.put( keys[i], values[i] );
         }
         return result;
     }
 
     public static <K, V> Map<K, V> pair( K key, V value ) {
         Map<K, V> map = new HashMap<K, V>();
         map.put( key, value );
         return map;
     }
 
     public static <K, V> Map<K, V> map( Map<K, V>... pairs ) {
         Map<K, V> result = new HashMap<K, V>();
         for ( Map<K, V> pair : pairs ) {
             result.putAll(pair);
         }
 
         return result;
     }
 
     public static <T> T[] array( Collection<T> items ) {
         return (T[]) items.toArray();
     }
 
     public static <T> T[] array( T... items ) {
         return items;
     }
 
     public static <T> List<T> emptyList() {
         return new ArrayList<T>();
     }
 
     public static <T> List<T> list( T... items ) {
         List<T> result = new ArrayList<T>();
         for ( T item : items ) {
             result.add(item);
         }
 
         return result;
     }
 
     public static <T> Set<T> emptySet() {
         return new HashSet<T>();
     }
 
     public static <T> Set<T> set( T... items ) {
         Set<T> result = new HashSet<T>();
         for ( T item : items ) {
             result.add(item);
         }
 
         return result;
     }
 
     public static <T> T firstOrNull( Collection<T> collection ) {
         Commons.checkNotNull(collection);
 
         if ( collection.isEmpty() ) {
             return null;
         }
 
         for ( T item : collection ) {
             if ( item != null ) {
                 return item;
             }
         }
 
         return null;
     }
 
     public static <T> T firstOrNull( List<T> list ) {
         if ( list == null ) {
             throw new IllegalArgumentException("<null>");
         }
 
         if ( list.isEmpty() ) {
             return null;
         }
 
         return list.get(0);
     }
 
 }
