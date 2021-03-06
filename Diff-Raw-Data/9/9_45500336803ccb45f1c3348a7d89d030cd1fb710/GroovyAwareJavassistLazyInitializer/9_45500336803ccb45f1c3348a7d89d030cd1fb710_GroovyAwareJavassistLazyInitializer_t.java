 /*
  * Copyright 2004-2005 Graeme Rocher
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package org.codehaus.groovy.grails.orm.hibernate.proxy;
 
 import grails.util.CollectionUtils;
 import groovy.lang.GroovyObject;
 
 import java.io.Serializable;
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 import java.util.Arrays;
 import java.util.HashSet;
 import java.util.Set;
 
 import javassist.util.proxy.MethodFilter;
 import javassist.util.proxy.MethodHandler;
 import javassist.util.proxy.ProxyFactory;
 import javassist.util.proxy.ProxyObject;
 
 import org.apache.commons.logging.LogFactory;
 import org.codehaus.groovy.grails.orm.hibernate.cfg.HibernateUtils;
 import org.grails.datastore.mapping.proxy.GroovyObjectMethodHandler;
 import org.hibernate.HibernateException;
 import org.hibernate.engine.spi.SessionImplementor;
 import org.hibernate.internal.util.ReflectHelper;
 import org.hibernate.proxy.HibernateProxy;
 import org.hibernate.proxy.pojo.BasicLazyInitializer;
 import org.hibernate.proxy.pojo.javassist.SerializableProxy;
 import org.hibernate.type.CompositeType;
 
 /**
  * @author Graeme Rocher
  * @since 1.0
  */
 public class GroovyAwareJavassistLazyInitializer extends BasicLazyInitializer implements MethodHandler {
 
     private static final String WRITE_CLASSES_DIRECTORY = System.getProperty("javassist.writeDirectory");
 
     private static final Set<String> GROOVY_METHODS = CollectionUtils.newSet("$getStaticMetaClass");
 
     private static final MethodFilter METHOD_FILTERS = new MethodFilter() {
         public boolean isHandled(Method m) {
             // skip finalize methods
             return m.getName().indexOf("super$") == -1 &&
                 !GROOVY_METHODS.contains(m.getName()) &&
                 !(m.getParameterTypes().length == 0 && (m.getName().equals("finalize")));
         }
     };
 
     private Class<?>[] interfaces;
     private boolean constructed = false;
     HibernateGroovyObjectMethodHandler groovyObjectMethodHandler;    
 
     protected GroovyAwareJavassistLazyInitializer(
             final String entityName,
             final Class<?> persistentClass,
             final Class<?>[] interfaces,
             final Serializable id,
             final Method getIdentifierMethod,
             final Method setIdentifierMethod,
             final CompositeType componentIdType,
             final SessionImplementor session,
             final boolean overridesEquals) {
         super(entityName, persistentClass, id, getIdentifierMethod, setIdentifierMethod, componentIdType, session, overridesEquals);
         this.interfaces = interfaces;
     }
 
     public static HibernateProxy getProxy(
             final String entityName,
             final Class<?> persistentClass,
             final Class<?>[] interfaces,
             final Method getIdentifierMethod,
             final Method setIdentifierMethod,
             CompositeType componentIdType,
             final Serializable id,
             final SessionImplementor session) throws HibernateException {
         // note: interface is assumed to already contain HibernateProxy.class
         final GroovyAwareJavassistLazyInitializer instance = new GroovyAwareJavassistLazyInitializer(
                 entityName, persistentClass, interfaces, id, getIdentifierMethod,
                 setIdentifierMethod, componentIdType, session, ReflectHelper.overridesEquals(persistentClass));
         ProxyFactory factory = createProxyFactory(persistentClass, interfaces);
        Class<?> proxyClass = getProxyFactory(persistentClass, interfaces);
         return createProxyInstance(proxyClass, instance);
     }
 
     protected static HibernateProxy createProxyInstance(Class<?> proxyClass,
             final GroovyAwareJavassistLazyInitializer instance) {
         final HibernateProxy proxy;
         try {
             proxy = (HibernateProxy)proxyClass.newInstance();
         } catch (Exception e) {
             throw new HibernateException("Javassist Enhancement failed: " + proxyClass.getName(), e);
         }
         ((ProxyObject) proxy).setHandler(instance);
        instance.groovyObjectMethodHandler = new HibernateGroovyObjectMethodHandler(proxyClass, proxy);
         HibernateUtils.enhanceProxy(proxy);
         instance.constructed = true;
         return proxy;
     }
 
     public static HibernateProxy getProxy(
             final Class<?> factory,
             final String entityName,
             final Class<?> persistentClass,
             final Class<?>[] interfaces,
             final Method getIdentifierMethod,
             final Method setIdentifierMethod,
             final CompositeType componentIdType,
             final Serializable id,
             final SessionImplementor session) throws HibernateException {
 
         final GroovyAwareJavassistLazyInitializer instance = new GroovyAwareJavassistLazyInitializer(
                 entityName, persistentClass, interfaces, id, getIdentifierMethod,
                 setIdentifierMethod, componentIdType, session, ReflectHelper.overridesEquals(persistentClass));
 
         return createProxyInstance(factory, instance);
     }
 
     public static Class<?> getProxyFactory(Class<?> persistentClass, Class<?>[] interfaces) throws HibernateException {
         // note: interfaces is assumed to already contain HibernateProxy.class
 
         try {
             Set<Class<?>> allInterfaces = new HashSet<Class<?>>();
             if(interfaces != null) {
                 allInterfaces.addAll(Arrays.asList(interfaces));
             }
             allInterfaces.add(GroovyObject.class);
             ProxyFactory factory = createProxyFactory(persistentClass, allInterfaces.toArray(new Class<?>[allInterfaces.size()]));
             Class<?> proxyClass = factory.createClass();
             HibernateUtils.enhanceProxyClass(proxyClass);
             return proxyClass;
         }
         catch (Throwable t) {
             LogFactory.getLog(BasicLazyInitializer.class).error(
                     "Javassist Enhancement failed: " + persistentClass.getName(), t);
             throw new HibernateException("Javassist Enhancement failed: " + persistentClass.getName(), t);
         }
     }
 
     private static ProxyFactory createProxyFactory(Class<?> persistentClass, Class<?>[] interfaces) {
         ProxyFactory factory = new ProxyFactory();
         factory.setSuperclass(persistentClass);
         factory.setInterfaces(interfaces);
         factory.setFilter(METHOD_FILTERS);
         factory.setUseCache(true);
         if (WRITE_CLASSES_DIRECTORY != null) {
             factory.writeDirectory = WRITE_CLASSES_DIRECTORY;
         }
         return factory;
     }
 
     public Object invoke(final Object proxy, final Method thisMethod, final Method proceed,
             final Object[] args) throws Throwable {
         Object result = groovyObjectMethodHandler.handleInvocation(proxy, thisMethod, args);
         if(groovyObjectMethodHandler.wasHandled(result)) {
            return result; 
         }
 
         if (constructed) {
             try {
                 result = invoke(thisMethod, args, proxy);
             }
             catch (Throwable t) {
                 throw new Exception(t.getCause());
             }
             if (result == INVOKE_IMPLEMENTATION) {
                 Object target = getImplementation();
                 final Object returnValue;
                 try {
                     if (ReflectHelper.isPublic(persistentClass, thisMethod)) {
                         if (!thisMethod.getDeclaringClass().isInstance(target)) {
                             throw new ClassCastException(target.getClass().getName());
                         }
                         returnValue = thisMethod.invoke(target, args);
                     }
                     else {
                         if (!thisMethod.isAccessible()) {
                             thisMethod.setAccessible(true);
                         }
                         returnValue = thisMethod.invoke(target, args);
                     }
                     return returnValue == target ? proxy : returnValue;
                 }
                 catch (InvocationTargetException ite) {
                     throw ite.getTargetException();
                 }
             }
             return result;
         }
 
         // while constructor is running
         if (thisMethod.getName().equals("getHibernateLazyInitializer")) {
             return this;
         }
 
         return proceed.invoke(proxy, args);
     }
 
     @Override
     protected Object serializableProxy() {
         return new SerializableProxy(
                 getEntityName(),
                 persistentClass,
                 interfaces,
                 getIdentifier(),
                 false,
                 getIdentifierMethod,
                 setIdentifierMethod,
                 componentIdType);
     }
     
     private static class HibernateGroovyObjectMethodHandler extends GroovyObjectMethodHandler {
         private Object target;
         private final Object originalSelf;
         
         public HibernateGroovyObjectMethodHandler(Class<?> proxyClass, Object originalSelf) {
             super(proxyClass);
             this.originalSelf = originalSelf;
         }
         
         @Override
         protected Object resolveDelegate(Object self) {
             if(self != originalSelf) {
                 throw new IllegalStateException("self instance has changed.");
             }
             if(target==null) {
                 target = ((HibernateProxy)self).getHibernateLazyInitializer().getImplementation(); 
             }
             return target;            
         }
     }
 }
