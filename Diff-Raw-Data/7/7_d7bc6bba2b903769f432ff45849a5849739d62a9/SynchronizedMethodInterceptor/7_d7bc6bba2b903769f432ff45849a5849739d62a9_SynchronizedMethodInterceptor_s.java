 /*
  * Copyright 2007 Russell Harmon
  *
  *    Licensed under the Apache License, Version 2.0 (the "License");
  *    you may not use this file except in compliance with the License.
  *    You may obtain a copy of the License at
  *
  *        http://www.apache.org/licenses/LICENSE-2.0
  *
  *    Unless required by applicable law or agreed to in writing, software
  *    distributed under the License is distributed on an "AS IS" BASIS,
  *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *    See the License for the specific language governing permissions and
  *    limitations under the License.
  */
 
 package com.eatnumber1.util.cglib;
 
import com.eatnumber1.util.compat.Override;
 import com.eatnumber1.util.concurrent.lock.SynchronizedLockProvider;
 import com.eatnumber1.util.facade.Facade;
 import java.lang.reflect.Method;
 import java.util.concurrent.locks.Lock;
 import net.sf.cglib.proxy.MethodInterceptor;
 import net.sf.cglib.proxy.MethodProxy;
 import org.jetbrains.annotations.NotNull;
 
 /**
  * @author Russell Harmon
  * @since Jul 13, 2007
  */
 public class SynchronizedMethodInterceptor<T> extends SynchronizedLockProvider implements MethodInterceptor, Facade<T> {
     @NotNull
     private T delegate;
 
     public SynchronizedMethodInterceptor( @NotNull T delegate, @NotNull Lock lock ) {
         super(lock);
         this.delegate = delegate;
     }
 
     public SynchronizedMethodInterceptor( @NotNull T delegate ) {
         this.delegate = delegate;
     }
 
     @Override
     public Object intercept( Object o, Method method, Object[] objects, MethodProxy methodProxy ) throws Throwable {
         Lock lock = getLock();
         lock.lock();
         try {
             return method.invoke(delegate, objects);
         } finally {
             lock.unlock();
         }
     }
 
     @NotNull
     @Override
     public T getDelegate() {
         return delegate;
     }
 
     @Override
     public void setDelegate( @NotNull T delegate ) {
         Lock lock = getLock();
         lock.lock();
         try {
             this.delegate = delegate;
         } finally {
             lock.unlock();
         }
     }
 }
