 package de.skuzzle.polly.tools.collections;
 
 import java.util.AbstractSet;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.Set;
 import java.util.concurrent.Executors;
 import java.util.concurrent.ScheduledExecutorService;
 import java.util.concurrent.ScheduledFuture;
 import java.util.concurrent.TimeUnit;
 
 import de.skuzzle.polly.tools.concurrent.ThreadFactoryBuilder;
 
 
 public class TemporaryValueMap<K, V> implements Map<K, V> {
 
     private class DeletionTask implements Runnable {
         private final K key;
         
         public DeletionTask(K key) {
             this.key = key;
         }
         
 
         @Override
         public void run() {
             synchronized (backend) {
                 backend.remove(this.key);
             }
         }
     }
     
     
     
     private final class TaskValuePair {
         private final ScheduledFuture<?> future;
         private final V value;
 
         public TaskValuePair(ScheduledFuture<?> future, V value) {
             this.future = future;
             this.value = value;
         }
         
         
         
         @Override
         public int hashCode() {
             return this.value == null ? 0 : this.value.hashCode();
         }
         
         
         
         @Override
         public boolean equals(Object obj) {
             return this.value == null ? obj == null : this.value.equals(obj);
         }
     }
     
     
     
     private final Map<K, TaskValuePair> backend;
     private final ScheduledExecutorService deletionService;
     private final long defaultCacheTime;
     
     
     
     public TemporaryValueMap(long defaultCacheTime) {
         this.defaultCacheTime = defaultCacheTime;
         this.backend = new HashMap<>();
         this.deletionService = Executors.newSingleThreadScheduledExecutor(
                 new ThreadFactoryBuilder().setDaemon(true));
     }
     
     
     
     public V put(K key, V value, long cacheTimeMs) {
         synchronized (this.backend) {
             final ScheduledFuture<?> future = this.deletionService.schedule(
                     new DeletionTask(key), cacheTimeMs, TimeUnit.MILLISECONDS);
             final TaskValuePair tvp = new TaskValuePair(future, value);
             final TaskValuePair result = this.backend.put(key, tvp);
             if (result != null) {
                 result.future.cancel(true);
                 return result.value;
             }
             return null;
         }
     }
     
     
     
     @Override
     public V put(K key, V value) {
         return this.put(key, value, this.defaultCacheTime);
     }
 
 
 
     @Override
     public void clear() {
         synchronized (this.backend) {
             for (final TaskValuePair tvp : this.backend.values()) {
                 tvp.future.cancel(true);
             }
             this.backend.clear();
         }
     }
 
 
 
     @Override
     public boolean containsKey(Object key) {
         synchronized (this.backend) {
             return this.backend.containsKey(key);
         }
     }
 
 
 
     @Override
     public boolean containsValue(Object value) {
         synchronized (this.backend) {
             return this.backend.containsValue(value);
         }
     }
 
 
 
     @Override
     public Set<java.util.Map.Entry<K, V>> entrySet() {
         synchronized (this.backend) {
             final Set<Entry<K, TaskValuePair>> cpy = new HashSet<>(
                     this.backend.entrySet());
             final Iterator<Entry<K, TaskValuePair>> it = cpy.iterator();
             final Iterator<Entry<K, V>> newIt = new Iterator<Map.Entry<K,V>>() {
                 @Override
                 public boolean hasNext() {
                     return it.hasNext();
                 }
 
                 @Override
                 public java.util.Map.Entry<K, V> next() {
                     final Entry<K, TaskValuePair> e = it.next();
                     return new Entry<K, V>() {
                         @Override
                         public K getKey() {
                             return e.getKey();
                         }
                         @Override
                         public V getValue() {
                             return e.getValue().value;
                         }
 
                         @Override
                         public V setValue(V value) {
                             throw new UnsupportedOperationException();
                         }
                     };
                 }
 
                 @Override
                 public void remove() {
                     throw new UnsupportedOperationException();
                 }
             };
             return new AbstractSet<Map.Entry<K,V>>() {
                 @Override
                 public Iterator<java.util.Map.Entry<K, V>> iterator() {
                     return newIt;
                 }
     
                 @Override
                 public int size() {
                     return backend.size();
                 }
             };
         }
     }
     
 
 
     @Override
     public Collection<V> values() {
         synchronized (this.backend) {
            final Collection<TaskValuePair> cpy = new HashSet<>(this.backend.values());
             final Iterator<TaskValuePair> it = cpy.iterator();
             final Iterator<V> newIt = new Iterator<V>() {
                 @Override
                 public boolean hasNext() {
                     return it.hasNext();
                 }
                 @Override
                 public V next() {
                     return it.next().value;
                 }
                 @Override
                 public void remove() {
                     throw new UnsupportedOperationException();
                 }
             };
             return new AbstractSet<V>() {
                 @Override
                 public Iterator<V> iterator() {
                     return newIt;
                 }
                 @Override
                 public int size() {
                     return backend.size();
                 }
             };
         }
     }
 
 
 
     @Override
     public V get(Object key) {
         synchronized (this.backend) {
             final TaskValuePair tvp = this.backend.get(key);
             return tvp == null ? null : tvp.value;
         }
     }
 
 
 
     @Override
     public boolean isEmpty() {
         synchronized (this.backend) {
             return this.backend.isEmpty();
         }
     }
 
 
 
     @Override
     public Set<K> keySet() {
         synchronized (this.backend) {
             return new HashSet<>(this.backend.keySet());
         }
     }
 
 
 
     @Override
     public void putAll(Map<? extends K, ? extends V> m) {
         synchronized (this.backend) {
             for (final Entry<? extends K, ? extends V> e : m.entrySet()) {
                 this.put(e.getKey(), e.getValue());
             }
         }
     }
 
 
 
     @Override
     public V remove(Object key) {
         synchronized (this.backend) {
             final TaskValuePair tvp = this.backend.get(key);
             if (tvp != null) {
                 tvp.future.cancel(true);
                 return tvp.value;
             }
         }
         return null;
     }
 
 
 
     @Override
     public int size() {
         synchronized (this.backend) {
             return this.backend.size();
         }
     }
 }
