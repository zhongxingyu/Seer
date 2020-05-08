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
 import com.eatnumber1.util.concurrent.lock.LockProvider;
 import com.eatnumber1.util.facade.Facade;
 import java.lang.reflect.Method;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.List;
 import java.util.concurrent.locks.Lock;
 import java.util.concurrent.locks.ReentrantLock;
 import net.sf.cglib.proxy.Callback;
 import net.sf.cglib.proxy.CallbackFilter;
 import net.sf.cglib.proxy.Enhancer;
 import net.sf.cglib.proxy.MethodInterceptor;
 import net.sf.cglib.proxy.MethodProxy;
 import org.apache.commons.collections15.collection.CompositeCollection;
 import org.jetbrains.annotations.NotNull;
 import org.jetbrains.annotations.Nullable;
 
 /**
  * @author Russell Harmon
  * @since Jul 13, 2007
  */
 public class EnhancerUtils {
     private EnhancerUtils() {
     }
 
     private static Enhancer getEnhancer( @NotNull Class<?> type, @Nullable Class<?>... interfaces ) {
         Enhancer enhancer = new Enhancer();
         List<Class<?>> interfaceList = interfaces == null ? new ArrayList<Class<?>>() : new ArrayList<Class<?>>(Arrays.asList(interfaces));
         if( type.isInterface() ) {
             interfaceList.add(type);
         } else {
             enhancer.setSuperclass(type);
         }
         enhancer.setInterfaces(interfaceList.toArray(new Class<?>[interfaceList.size()]));
         return enhancer;
     }
 
     @NotNull
     public static <T> T create( @NotNull final Class<T> type, @NotNull Callback... callbacks ) {
         return create(new NoArgConstructorDescriptor<T>(type), callbacks);
     }
 
     @NotNull
     public static <T> T create( @NotNull ConstructorDescriptor<T> descriptor, @NotNull Callback... callbacks ) {
         Enhancer enhancer = getEnhancer(descriptor.getType(), (Class<?>[]) null);
         enhancer.setCallbacks(callbacks);
         //noinspection unchecked
         return (T) enhancer.create(descriptor.getArgumentTypes(), descriptor.getArguments());
     }
 
     @NotNull
     public static <T> T synchronize( @NotNull T delegate ) {
         //noinspection unchecked
         return synchronize((Class<T>) delegate.getClass(), delegate);
     }
 
     @NotNull
     public static <T, D extends T> T synchronize( @NotNull Class<T> type, @NotNull D delegate ) {
         return synchronize(type, delegate, new ReentrantLock());
     }
 
     @NotNull
     public static <T, D extends T> T synchronize( @NotNull ConstructorDescriptor<T> descriptor, @NotNull D delegate ) {
         return synchronize(descriptor, delegate, new ReentrantLock());
     }
 
     @NotNull
     public static <T, D extends T> T synchronize( @NotNull Class<T> type, @NotNull D delegate, @NotNull Lock lock ) {
         return synchronize(new NoArgConstructorDescriptor<T>(type), delegate, lock);
     }
 
     @NotNull
     public static <T, D extends T> T synchronize( @NotNull ConstructorDescriptor<T> descriptor, @NotNull D delegate, @NotNull Lock lock ) {
         Enhancer enhancer = getEnhancer(descriptor.getType(), LockProvider.class, Facade.class);
         enhancer.setCallbackFilter(new CallbackFilter() {
             private Collection<Method> INTERCEPTED_METHODS = Collections.unmodifiableCollection(new CompositeCollection<Method>(Arrays.asList(LockProvider.class.getMethods()), Arrays.asList(Facade.class.getMethods())));
 
             @Override
             public int accept( Method method ) {
                 return INTERCEPTED_METHODS.contains(method) ? 1 : 0;
             }
         });
         final SynchronizedMethodInterceptor interceptor = new SynchronizedMethodInterceptor<D>(delegate, lock);
         enhancer.setCallbacks(new Callback[] { interceptor, new MethodInterceptor() {
             @Override
             public Object intercept( Object o, Method method, Object[] objects, MethodProxy methodProxy ) throws Throwable {
                 return method.invoke(interceptor, objects);
             }
         } });
         //noinspection unchecked
         return (T) enhancer.create(descriptor.getArgumentTypes(), descriptor.getArguments());
     }
 }
