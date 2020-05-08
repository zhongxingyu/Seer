 /*
  *  Copyright 2009-2010 Mathieu ANCELIN
  * 
  *  Licensed under the Apache License, Version 2.0 (the "License");
  *  you may not use this file except in compliance with the License.
  *  You may obtain a copy of the License at
  * 
  *       http://www.apache.org/licenses/LICENSE-2.0
  * 
  *  Unless required by applicable law or agreed to in writing, software
  *  distributed under the License is distributed on an "AS IS" BASIS,
  *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *  See the License for the specific language governing permissions and
  *  limitations under the License.
  *  under the License.
  */
 package cx.ath.mancel01.dependencyshot.graph;
 
 import cx.ath.mancel01.dependencyshot.api.InjectionPoint;
 import cx.ath.mancel01.dependencyshot.api.Stage;
 import cx.ath.mancel01.dependencyshot.exceptions.DSIllegalStateException;
 import cx.ath.mancel01.dependencyshot.injection.InjectorImpl;
 import cx.ath.mancel01.dependencyshot.injection.util.EnhancedProvider;
 import cx.ath.mancel01.dependencyshot.injection.util.InstanceProvider;
 import cx.ath.mancel01.dependencyshot.spi.InstanceHandler;
 import cx.ath.mancel01.dependencyshot.spi.InstanceLifecycleHandler;
 import cx.ath.mancel01.dependencyshot.spi.PluginsLoader;
 import java.lang.annotation.Annotation;
 import java.lang.reflect.Type;
 import java.util.Map;
 import javax.inject.Named;
 import javax.inject.Provider;
 import javax.inject.Qualifier;
 import javax.inject.Singleton;
 
 /**
  * Object representation of a binding.
  * 
  * @author Mathieu ANCELIN
  */
 public class Binding<T> {
 
     /**
      *
      */
     private static final int HASH = 7;
     /**
      *
      */
     private static final int HASH_KEY = 79;
     /**
      * Binded class.
      */
     private Class<T> from;
     /**
      * Implementation of extends of @Code from
      */
     private Class<? extends T> to;
     /**
      * Custom qualifier annotation for a binding. 
      * Useful for specific injection.
      */
     private Class<? extends Annotation> qualifier;
     /**
      * Instance provider for an injection (factory like)
      */
     private Provider<T> provider;
     /**
      * Name of a named binding i.e with @Named("something")
      */
     private String name;
 
     private Stage stage;
 
 
     public Binding() {}
     /**
      * Constructor
      * 
      * @param qualifier qualifier of the binding
      * @param name name of the binding @Named
      * @param from basic binded class
      * @param to implementation or extends of the binded class
      * @param provider provider object
      */
     public Binding(
             Class<? extends Annotation> qualifier,
             String name,
             Class<T> from,
             Class<? extends T> to,
             Provider<T> provider,
             Stage stage) {
         if (qualifier != null && !qualifier.isAnnotationPresent(Qualifier.class)) {
             throw new IllegalArgumentException();
         }
         this.from = from;
         this.qualifier = qualifier;
         this.to = to;
         this.name = name;
         this.provider = provider;
         this.stage = stage;
     }
 
     public Binding(Map params) {
         Class qualif = null;
         if (params.containsKey("annotedWith")) {
             qualif = (Class<? extends Annotation>) params.get("annotedWith");
         }
         if (qualif != null && !qualif.isAnnotationPresent(Qualifier.class)) {
             throw new IllegalArgumentException();
         } else {
             this.qualifier = qualif;
         }
         if (params.containsKey("from")) {
             this.from = (Class<T>) params.get("from");
         }
         if (params.containsKey("to")) {
             this.to = (Class<? extends T>) params.get("to");
         }
         if (params.containsKey("named")) {
             this.name = (String) params.get("named");
         }
         if (params.containsKey("providedBy")) {
             this.provider = (Provider<T>) params.get("providedBy");
         }
         if (params.containsKey("onStage")) {
             this.stage = (Stage) params.get("onStage");
         }
         if (params.containsKey("toInstance")) {
             this.provider = new InstanceProvider(params.get("toInstance"));
         }
     }
 
     /**
      *
      * @return
      */
     public final Class<T> getFrom() {
         return from;
     }
 
     /**
      *
      * @return
      */
     public final Class<? extends Annotation> getQualifier() {
         return qualifier;
     }
 
     /**
      *
      * @return
      */
     public final Class<? extends T> getTo() {
         return to;
     }
 
     /**
      * 
      * @return
      */
     public final Stage getStage() {
         return stage;
     }
 
     /**
      * 
      * @param to
      */
     public final void setTo(Class<? extends T> to) {
         this.to = to;
     }
 
     /**
      * Get an instance of a binded object.
      *
      * @param injector the concerned injector
      * @return binded object
      */
     public final T getInstance(InjectorImpl injector, InjectionPoint point) {
         // TODO : extension point : inject dynamic
         T result = null;
         if (provider != null) {
             if (isImplementingEnhancedProvider(provider.getClass().getGenericInterfaces())) {
                 result = (T) ((EnhancedProvider) provider).enhancedGet(point);
             } else {
                 result = provider.get();
             }
         } else if (to.isAnnotationPresent(Singleton.class)) {
             result = (T) injector.getSingleton(to);
         } else {
             result = (T) injector.createInstance(to);
         }
         if (result == null) {
             throw new DSIllegalStateException("Could not get a " + to);
         }
         for (InstanceLifecycleHandler handler : PluginsLoader.getInstance().getLifecycleHandlers()) {
             handler.handlePostConstruct(result);
         }
         for (InstanceHandler handler : PluginsLoader.getInstance().getInstanceHandlers()) {
             result = (T) handler.handleInstance(result, from, injector);
         }
         return result;
     }
 
     private boolean isImplementingEnhancedProvider(Type[] interfaces) {
         boolean ret = false;
         for (Type t : interfaces) {
             if (t.equals(EnhancedProvider.class)) {
                 ret = true;
             }
         }
         return ret;
     }
 
     /**
      * @{@inheritDoc }
      */
     @Override
     public final boolean equals(Object obj) {
         if (this == obj) {
             return true;
         }
         if (obj == null) {
             return false;
         }
         if (getClass() != obj.getClass()) {
             return false;
         }
         Binding<?> other = (Binding<?>) obj;
         if (from == null) {
             if (other.from != null) {
                 return false;
             }
         } else if (!from.equals(other.from)) {
             return false;
         }
         if (name == null) {
             if (other.name != null) {
                 return false;
             }
         } else if (!name.equals(other.name)) {
             return false;
         }
         if (qualifier == null) {
             if (other.qualifier != null) {
                 return false;
             }
         } else if (!qualifier.equals(other.qualifier)) {
             return false;
         }
         return true;
     }
 
     /**
      * @{@inheritDoc }
      */
     @Override
     public final int hashCode() {
         int hash = HASH;
         hash = HASH_KEY * hash + (this.from != null ? this.from.hashCode() : 0);
         hash = HASH_KEY * hash + (this.qualifier != null ? this.qualifier.hashCode() : 0);
         hash = HASH_KEY * hash + (this.name != null ? this.name.hashCode() : 0);
         return hash;
     }
 
     /**
      * @{@inheritDoc }
      */
     @Override
     public final String toString() {
         StringBuilder builder = new StringBuilder();
         builder.append(getClass().getSimpleName() + " [");
         if (from != null) {
             builder.append("from=");
             builder.append(from.getName());
         }
         if (to != null) {
             builder.append(", ");
             builder.append("to=");
             builder.append(to.getName());
         }
         if (qualifier != null) {
             builder.append(", ");
             builder.append("qualifier=");
             builder.append(qualifier.getName());
         }
         if (provider != null) {
             builder.append(", ");
             builder.append("provider=");
             builder.append(provider);
         }
         if (name != null) {
             builder.append(", ");
             builder.append("name=");
             builder.append(name);
         }
         builder.append("]");
         return builder.toString();
     }
 
     /**
      * Create a fake binding to search it in a map.
      *
      * @param <T> type of the class
      * @param c class to bind
      * @param annotation qualifier
      * @return a fake binding
      */
     public static <T> Binding<T> lookup(Class<T> c, Annotation annotation) {
         if (annotation instanceof Named) {
             Named named = (Named) annotation;
             return new Binding<T>(null, named.value(), c, c, null, null);
         } else if (annotation != null) {
             return new Binding<T>(annotation.annotationType(), null, c, null, null, null);
         } else {
             return new Binding<T>(null, null, c, null, null, null);
         }
     }
 }
