 /**
  * Copyright (C) 2012 Ness Computing, Inc.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package com.nesscomputing.scopes.threaddelegate;
 
 import java.util.Set;
 import java.util.concurrent.Callable;
 import java.util.concurrent.ConcurrentMap;
 import java.util.concurrent.ExecutionException;
 import java.util.concurrent.Future;
 import java.util.concurrent.FutureTask;
 
 import javax.annotation.Nonnull;
 import com.google.common.annotations.VisibleForTesting;
 import com.google.common.base.Preconditions;
 import com.google.common.collect.Maps;
 import com.google.common.collect.Sets;
 import com.google.inject.Key;
 import com.google.inject.Provider;
 
 /**
  * This is the context object for the scope. All members of the context object can potentially
  * be shared between objects, so they should be thread safe.  Once a value is set in a Context, it may
  * never be replaced (unless you use testing-only-methods, which you should not...).
  */
 public class ThreadDelegatedContext
 {
     /**
      * Computed values.
      */
     private final ConcurrentMap<Key<?>, Future<?>> contents = Maps.newConcurrentMap();
 
     /**
      * Values which listen to scoping events.
      */
     private final Set<ScopeListener> listeners = Sets.newSetFromMap(Maps.<ScopeListener, Boolean>newConcurrentMap());
 
     ThreadDelegatedContext()
     {
     }
 
     boolean containsKey(@Nonnull final Key<?> key)
     {
         Preconditions.checkArgument(key != null, "Key must not be null!");
         return contents.containsKey(key);
     }
 
     @SuppressWarnings("unchecked")
     <T> T get(final Key<T> key) throws InterruptedException, ExecutionException
     {
         Preconditions.checkArgument(key != null, "Key must not be null!");
         final Future<?> result = contents.get(key);
         return result == null ? null : (T) result.get();
     }
 
     /**
      * Compute and enter a value into the context unless the value has already been computed.
      * @param key the key to associate this value with
      * @param valueComputer the Provider that will compute the value if necessary
      * @return the old value, if it already existed, or the freshly computed value
      */
    @SuppressWarnings("PMD.UnnecessaryLocalBeforeReturn")
     <T> T putIfAbsent(@Nonnull final Key<T> key, @Nonnull final Provider<T> valueComputer) throws InterruptedException, ExecutionException
     {
         Preconditions.checkArgument(key != null, "Key must not be null!");
 
         // Fast path, if the value exists, return it as fast as possible
         Future<?> oldValue = contents.get(key);
 
         if (oldValue != null) {
             @SuppressWarnings("unchecked")
             T value = (T) oldValue.get();
             return value;
         }
 
         FutureTask<T> task = new FutureTask<>(new Callable<T>() {
             @Override
             public T call() throws Exception
             {
                 T value = valueComputer.get();
 
                 // Register the value if it listens to scoping events
                 if (value instanceof ScopeListener) {
                     final ScopeListener listener = (ScopeListener) value;
                     listeners.add(listener);
                     // Send an "enter" event to notify the listener that it was put in scope.
                     listener.event(ScopeEvent.ENTER);
                 }
 
                 return value;
             }
         });
 
         Future<?> existingTask = contents.putIfAbsent(key, task);
         if (existingTask == null) {
             // Now our task is in the map, so run it
             task.run();
             return task.get();
         } else {
             // Someone else beat us to it, make sure to throw our task away and use theirs
             @SuppressWarnings("unchecked")
             T newValue = (T) existingTask.get();
             return newValue;
         }
     }
 
     /**
      * You must <b>NOT</b> potentially add any new keys while in the middle of a clear() call.
      * Only for testing!
      */
     @VisibleForTesting
     void clear()
     {
         event(ScopeEvent.LEAVE);
         listeners.clear();
         contents.clear();
     }
 
     @VisibleForTesting
     int size()
     {
         return contents.size();
     }
 
     void event(final ScopeEvent event)
     {
         for (ScopeListener listener: listeners) {
             listener.event(event);
         }
     }
 
     /**
      * Objects put in the ThreadDelegated scope can implement this interface to be notified when
      * they are moved from one thread to another.
      */
     public static interface ScopeListener
     {
         void event(ScopeEvent event);
     }
 
     public static enum ScopeEvent
     {
         ENTER,
         LEAVE;
     }
 }
