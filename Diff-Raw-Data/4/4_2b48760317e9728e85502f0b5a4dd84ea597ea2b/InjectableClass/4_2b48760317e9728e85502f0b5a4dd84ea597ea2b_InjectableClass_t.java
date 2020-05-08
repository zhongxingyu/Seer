 /**
  * ********************************************************************************
  * Copyright (c) 2011, Monnet Project All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions are met: *
  * Redistributions of source code must retain the above copyright notice, this
  * list of conditions and the following disclaimer. * Redistributions in binary
  * form must reproduce the above copyright notice, this list of conditions and
  * the following disclaimer in the documentation and/or other materials provided
  * with the distribution. * Neither the name of the Monnet Project nor the names
  * of its contributors may be used to endorse or promote products derived from
  * this software without specific prior written permission.
  *
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
  * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
  * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
  * ARE DISCLAIMED. IN NO EVENT SHALL THE MONNET PROJECT BE LIABLE FOR ANY
  * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
  * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
  * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
  * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
  * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
  * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  * *******************************************************************************
  */
 package eu.monnetproject.framework.services.impl;
 
 import eu.monnetproject.framework.services.Inject;
 import eu.monnetproject.framework.services.NonEmpty;
 import eu.monnetproject.framework.services.ServiceCollection;
 import eu.monnetproject.framework.services.ServiceLoadException;
 import eu.monnetproject.framework.services.Singleton;
 import java.lang.annotation.Annotation;
 import java.lang.reflect.Constructor;
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.ParameterizedType;
 import java.lang.reflect.Type;
 import java.util.HashMap;
 
 /**
  *
  * @author John McCrae
  */
 public class InjectableClass<C> {
 
     private final Class<C> clazz;
     private final Constructor<C> constructor;
 
     @SuppressWarnings("unchecked")
     public InjectableClass(Class<C> clazz) {
         this.clazz = clazz;
         Constructor<C> injectableConstructor = null;
         for (Constructor<?> constructr : clazz.getConstructors()) {
             if (constructr.getAnnotation(Inject.class) != null) {
                 if (injectableConstructor == null) {
                     injectableConstructor = (Constructor<C>) constructr;
                 } else {
                     throw new ServiceLoadException(clazz, clazz.getName() + " has multiple injectable constructors");
                 }
             }
         }
         if (injectableConstructor == null) {
             if (clazz.getConstructors().length == 1) {
                 injectableConstructor = (Constructor<C>) clazz.getConstructors()[0];
             } else {
                 throw new ServiceLoadException(clazz, clazz.getName() + " does not have a marked or single constructor");
             }
         }
         this.constructor = injectableConstructor;
         for (Type type : constructor.getGenericParameterTypes()) {
             if (type instanceof ParameterizedType) {
                 final ParameterizedType pt = (ParameterizedType) type;
                 if (!(pt.getRawType() instanceof Class)) {
                     throw new ServiceLoadException(clazz, "Bad type on constructor argument " + pt);
                 }
                 if (ServiceCollection.class.isAssignableFrom((Class) pt.getRawType()) && !(pt.getActualTypeArguments()[0] instanceof Class)) {
                     throw new ServiceLoadException(clazz, "Bad type on constructor argument " + pt);
                 }
             } else if (!(type instanceof Class)) {
                 throw new ServiceLoadException(clazz, "Bad type on constructor argument " + type);
             } else if (type.equals(ServiceCollection.class) || type.equals(Iterable.class)) {
                 throw new ServiceLoadException(clazz, "Unparameterized ServiceCollection or Iterable used as constructor argument");
             }
         }
     }
 
     /**
      * Is the type returned from {@code dependencies} multiple
      *
      * @param t The type
      * @return true if it is a ServiceCollection or Iterable
      */
     public static boolean isMultiple(Type t) {
        return t instanceof ParameterizedType && 
                ((Class<?>) ((ParameterizedType) t).getRawType()).isAssignableFrom(ServiceCollection.class) && 
                (((ParameterizedType) t).getActualTypeArguments()[0] instanceof Class);
     }
 
     /**
      * Get the real type of the argument
      *
      * @param t The type
      * @return Either the class t represents or the arguments
      */
     public static Class<?> getRealType(Type t) {
         if (t instanceof Class) {
             return (Class) t;
         } else if (t instanceof ParameterizedType && isMultiple(t)) {
             return (Class) (((ParameterizedType) t).getActualTypeArguments()[0]);
         } else {
             return (Class) ((ParameterizedType) t).getRawType();
         }
     }
 
     public Type[] dependencies() {
         return constructor.getGenericParameterTypes();
     }
 
     public boolean[] isNonEmpty() {
         final Annotation[][] paramAnnos = constructor.getParameterAnnotations();
         boolean[] nonEmpty = new boolean[paramAnnos.length];
         for (int i = 0; i < nonEmpty.length; i++) {
             nonEmpty[i] = false;
             for (int j = 0; j < paramAnnos[i].length; j++) {
                 if (paramAnnos[i][j] instanceof NonEmpty) {
                     nonEmpty[i] = true;
                 }
             }
         }
         return nonEmpty;
     }
 
     protected boolean isSingleton() {
         return clazz.getAnnotation(Singleton.class) != null;
     }
     
     private static final HashMap<InjectableClass<?>,Object> singletonInstances = new HashMap<InjectableClass<?>, Object>();
 
     public void resetSingleton() {
         if(isSingleton()) {
             synchronized(singletonInstances) {
                 singletonInstances.remove(this);
             }
         }
     }
     
     @SuppressWarnings("unchecked")
     public C newInstance(Object[] args) {
 
         try {
             if (isSingleton()) {
                 C c = null;
                 do {
                     if (!singletonInstances.containsKey(this)) {
                         // We do this unsynchronized to avoid deadlocks
                         c = constructor.newInstance(args);
                     } else {
                         // Unsynchronized so singleInstance may have become null (!)
                         c = (C)singletonInstances.get(this);
                     }
                 } while (c == null); 
                 synchronized (singletonInstances) {
                     if (!singletonInstances.containsKey(this)) {
                         singletonInstances.put(this, c);
                         return c;
                     } else {
                         return (C)singletonInstances.get(this);
                     }
                 }
             } else {
                 return constructor.newInstance(args);
             }
         } catch (IllegalAccessException x) {
             throw new ServiceLoadException(clazz, x);
         } catch (InstantiationException x) {
             throw new ServiceLoadException(clazz, x);
         } catch (InvocationTargetException x) {
             throw new ServiceLoadException(clazz, x);
         }
     }
 
     public String getClassName() {
         return clazz.getName();
     }
 
     @Override
     public boolean equals(Object obj) {
         if (obj == null) {
             return false;
         }
         if (getClass() != obj.getClass()) {
             return false;
         }
         @SuppressWarnings("unchecked")
         final InjectableClass<C> other = (InjectableClass<C>) obj;
         if (this.clazz != other.clazz && (this.clazz == null || !this.clazz.equals(other.clazz))) {
             return false;
         }
         return true;
     }
 
     @Override
     public int hashCode() {
         int hash = 5;
         hash = 79 * hash + (this.clazz != null ? this.clazz.hashCode() : 0);
         return hash;
     }
 }
