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
 
 
 import java.util.concurrent.ExecutionException;
 
 import javax.annotation.Nullable;
 
 import com.google.common.base.Preconditions;
 import com.google.inject.Key;
 import com.google.inject.Provider;
 import com.google.inject.ProvisionException;
 import com.google.inject.Scope;
 import com.google.inject.Singleton;
 import com.nesscomputing.scopes.threaddelegate.ThreadDelegatedContext.ScopeEvent;
 
 /**
  * Maintains the global state for the ThreadDelegatedScope.
  */
 @Singleton
 public class ThreadDelegatedScope implements Scope
 {
     private final ThreadLocal<ThreadDelegatedContext> threadLocal;
 
     /** The global scope object to bind. This is created at load time of this class. */
     public static final ThreadDelegatedScope SCOPE = new ThreadDelegatedScope();
 
     ThreadDelegatedScope()
     {
         this.threadLocal = new ThreadLocal<ThreadDelegatedContext>();
     }
 
     /**
      * Returns the context (the set of objects bound to the scope) for the current thread.
      * A context may be shared by multiple threads.
      */
     public ThreadDelegatedContext getContext()
     {
         ThreadDelegatedContext context = threadLocal.get();
         if (context == null) {
             context = new ThreadDelegatedContext();
             threadLocal.set(context);
         }
         return context;
     }
 
     /**
      * A thread enters the scope. Clear the current context. If a new context
      * was given, assign it to the scope, otherwise leave it empty.
      */
     public void changeScope(@Nullable final ThreadDelegatedContext context)
     {
         final ThreadDelegatedContext oldContext = threadLocal.get();
         if (oldContext != null) {
             if (oldContext == context) {
                 // If the context gets exchanged with itself, do nothing.
                 return;
             }
             else {
                 // This must not clear the context. It might still be
                 // referenced by another thread.
                 oldContext.event(ScopeEvent.LEAVE);
             }
         }
 
         if (context != null) {
             threadLocal.set(context);
             context.event(ScopeEvent.ENTER);
         }
         else {
             threadLocal.remove();
         }
     }
 
     @Override
     public <T> Provider<T> scope(final Key<T> key, final Provider<T> unscoped)
     {
         return new ThreadDelegatedScopeProvider<T>(key, unscoped);
     }
 
     public class ThreadDelegatedScopeProvider<T> implements Provider<T>
     {
         private final Key<T> key;
         private final Provider<T> unscoped;
 
         public ThreadDelegatedScopeProvider(final Key<T> key, final Provider<T> unscoped)
         {
             Preconditions.checkArgument(key != null, "key must not be null!");
             Preconditions.checkArgument(unscoped != null, "unscoped provider must not be null!");
 
             this.key = key;
             this.unscoped = unscoped;
         }
 
         @Override
        @SuppressWarnings("PMD.PreserveStackTrace")
         public T get()
         {
             try {
                 final ThreadDelegatedContext context = getContext();
                 T value = context.get(key);
                 if (value != null || context.containsKey(key)) {
                     return value;
                 }
                 else {
                     return context.putIfAbsent(key, unscoped);
                 }
             } catch (InterruptedException e) {
                 Thread.currentThread().interrupt();
                 throw new ProvisionException("Interrupted while waiting for computed ThreadDelegated value for key " + key, e);
             } catch (ExecutionException e) {
                 throw new ProvisionException("Exception while computing value for key " + key, e.getCause());
             }
         }
 
         private volatile String toString = null;
 
         @Override
         public synchronized String toString()
         {
             if (toString == null) {
                 toString = String.format("ThreadDelegatedScoped provider (Key: %s) of %s", key.toString(), unscoped.toString());
             }
             return toString;
         }
     }
 }
