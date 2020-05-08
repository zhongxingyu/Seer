 /*
  * Copyright © 2010 Red Hat, Inc.
  *
  * This is free software; you can redistribute it and/or modify it
  * under the terms of the GNU Lesser General Public License as
  * published by the Free Software Foundation; either version 2.1 of
  * the License, or (at your option) any later version.
  *
  * This software is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  * Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public
  * License along with this software; if not, write to the Free
  * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
  * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
  */
 
 package com.redhat.rhevm.api.common.util;
 
 import java.lang.ref.Reference;
 import java.lang.ref.ReferenceQueue;
 import java.lang.ref.SoftReference;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.LinkedHashMap;
 import java.util.Map;
 
 
 /**
  * Models a strongly-referenced primary hash map, coupled with a reapable
  * secondary map based on soft-referenced values (rather than keys, as is
  * the case with the java.util.WeakHashMap).
  * REVISIT: does the world really need yet another hand-rolled cache type?
  * REVISIT: inherited entrySet() etc. don't take account of secondary
  *
  * @param <K> key type
  * @param <V> value type
  */
 public class ReapedMap<K, V> extends HashMap<K, V> {
 
     // FIXME: I just made up this UID
     static final long serialVersionUID = 12345678987654321L;
 
     private static Long DEFAULT_REAP_AFTER = 10 * 60 * 1000L; // 10 minutes
 
     private ValueToKeyMapper<K, V> keyMapper;
     private long reapAfter;
     private ReferenceQueue<V> queue;
 
     // Secondary Map, note:
     // - keys are strongly referenced, as GC of corresponding values
     //   will trigger their release
     // - reap requires a predictable iteration order (based on insertion order)
     //   hence the use of LinkedHasMap
     //
     LinkedHashMap<K, SoftlyReferencedValue<V>> reapableMap;
 
     /**
      * @param keyMapper maps from value to key type
      */
     public ReapedMap(ValueToKeyMapper<K, V> keyMapper) {
         this(keyMapper, DEFAULT_REAP_AFTER);
     }
 
     /**
      * @param keyMapper keyMapper maps from value to key type
      * @param reapAfter entries become eligible for reaping after this duration (ms)
      */
     public ReapedMap(ValueToKeyMapper<K, V> keyMapper, long reapAfter) {
         this(keyMapper, reapAfter, new ReferenceQueue<V>());
     }
 
     /**
      * Package-protected constructor intended for test use.
      *
      * @param keyMapper keyMapper maps from value to key type
      * @param reapAfter entries become eligible for reaping after this duration (ms)
      * @param queue reference queue to avoid leaked mappings in case where
      * aggressive GC eats referent before it is reaped
      */
     ReapedMap(ValueToKeyMapper<K, V> keyMapper, long reapAfter, ReferenceQueue<V> queue) {
         this.keyMapper = keyMapper;
         this.reapAfter = reapAfter;
         this.queue = queue;
         reapableMap = new LinkedHashMap<K, SoftlyReferencedValue<V>>();
     }
 
     @Override
     public synchronized V get(Object key) {
         V ret = super.get(key);
         if (ret == null) {
             SoftlyReferencedValue<V> softValue = reapableMap.get(key);
             if (softValue != null) {
                 SoftReference<V> softRef = softValue.value;
                 if (softRef.isEnqueued()) {
                     softRef.clear();
                     reapableMap.remove(key);
                 } else {
                     ret = softRef.get();
                     if (ret == null) {
                         reapableMap.remove(key);
                     }
                 }
             }
         }
         reap();
         return ret;
     }
 
     @Override
     public synchronized V put(K k, V v)  {
         reap();
         return super.put(k, v);
     }
 
     @Override
     public synchronized V remove(Object key) {
         V ret = super.remove(key);
         if (ret == null) {
             SoftlyReferencedValue<V> softValue = reapableMap.remove(key);
             if (softValue != null) {
                 SoftReference<V> softRef = softValue.value;
                 if (softRef.isEnqueued()) {
                     softRef.clear();
                 } else {
                     ret = softRef.get();
                 }
             }
         }
         reap();
         return ret;
     }
 
     @Override
     public synchronized void clear() {
         super.clear();
         reapableMap.clear();
         while (queue.poll() != null)
             ;
     }
 
     /**
      * Mark a key as being reapable, caching corresponding soft reference to
      * corresponding value in the secondary map.
      *
      * @param k
      */
     public synchronized void reapable(K k) {
         V v = super.remove(k);
         if (v != null) {
             reapableMap.put(k, new SoftlyReferencedValue<V>(new SoftReference<V>(v, queue)));
         }
         reap();
     }
 
     /**
      * @return the size of the secondary map
      */
     public synchronized long reapableSize() {
         return reapableMap.size();
     }
 
     /**
      * Reap <i>before</i> additive operations, <i>after</i> for neutral and
      * destructive ones.
      */
     private synchronized void reap() {
 
         // reap entries older than age permitted
         //
         long now = System.currentTimeMillis();
         Iterator<Map.Entry<K, SoftlyReferencedValue<V>>> entries = reapableMap.entrySet().iterator();
         while (entries.hasNext()) {
             Map.Entry<K, SoftlyReferencedValue<V>> entry = entries.next();
             SoftlyReferencedValue<V> v = entry.getValue();
             if (now - v.timestamp > reapAfter) {
                 entries.remove();
                 entry.getValue().value.clear();
                 entry.setValue(null);
             } else {
                 // guaranteed iteration on insertion order => no older entries
                 //
                 break;
             }
         }
 
         // poll reference queue for GC-pending references to trigger
         // reaping of referent
         //
         Reference<? extends V> ref = null;
         while ((ref = queue.poll()) != null) {
             V value = ref.get();
             if (value != null) {
                 reapableMap.remove(keyMapper.getKey(value));
             }
         }
     }
 
     /**
      * Required to map back from GC-pending value to the corresponding key
      */
     public interface ValueToKeyMapper<K, V>  {
         K getKey(V value);
     }
 
     /**
      * Encapsulate soft-reference and timestamp (the latter is used for
      * eager reaping)
      */
    private class SoftlyReferencedValue<S> {
        SoftReference<S> value;
         long timestamp;
 
        SoftlyReferencedValue(SoftReference<S> value) {
             this.value = value;
             timestamp = System.currentTimeMillis();
         }
 
         public boolean equals (Object other) {
             boolean ret = false;
             try {
                 ret = other == value
                       || (value.equals(((SoftlyReferencedValue<?>)other).value));
             } catch (ClassCastException cee) {
             }
             return ret;
         }
 
         public int hashCode() {
             return value.hashCode();
         }
     }
 }
