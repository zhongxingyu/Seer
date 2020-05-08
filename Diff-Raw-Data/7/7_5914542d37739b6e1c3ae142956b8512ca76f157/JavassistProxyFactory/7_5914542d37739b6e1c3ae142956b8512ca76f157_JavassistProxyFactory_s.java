 /*
 * JBoss, Home of Professional Open Source
 * Copyright 2005, JBoss Inc., and individual contributors as indicated
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
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
 package org.jboss.ejb3.proxy.javassist;
 
 import java.lang.reflect.InvocationHandler;
 
 import org.jboss.ejb3.proxy.spi.factory.ProxyFactory;
 
 /**
  * Implementation of {@link ProxyFactory} which uses Javassist to create proxies
  *
  * @author Jaikiran Pai
  * @version $Revision: $
  */
 public class JavassistProxyFactory implements ProxyFactory
 {
 
    /**
     * @see org.jboss.ejb3.proxy.spi.factory.ProxyFactory#createProxy(java.lang.ClassLoader, java.lang.Class<?>[], java.lang.reflect.InvocationHandler)
     */
    public Object createProxy(ClassLoader cl, Class<?>[] types, InvocationHandler invocationHandler)
          throws IllegalArgumentException
    {
       // we can only handle one type
       if (types.length == 0)
       {
          throw new IllegalArgumentException("Class type not specified for creating proxy");
       }
       if (types.length > 1)
       {
          throw new IllegalArgumentException("More than one class type specified for creating proxy");
       }
       if (cl == null)
       {
          throw new IllegalArgumentException("Null classloader passed to proxy creation");
       }
       String className = types[0].getName();
       try
       {
          // we load the type using the passed classloader so
          // that javassist will then use this classloader for
          // creating the proxy on the passed type
          Class<?> type = cl.loadClass(className);
          // create a proxy for this type
          return this.createProxy(new Class<?>[] {type}, invocationHandler);
       }
       catch (ClassNotFoundException cnfe)
       {
          throw new RuntimeException("Could not load class: " + className + " during proxy creation", cnfe);
       }
       
    }
 
    /**
     * Creates a proxy using javassist
     * <p>
     *   This implementation expects the passed <code>types</code> to contain only one 
     *   {@link Class} type. If it contains more than one {@link Class} type then a 
     *   {@link IllegalArgumentException} is thrown
     * </p>
     * <p>
     *  The implementation uses the classloader of <code>types</code>[0] for creating the
     *  proxy
     * </p>
     * @throws IllegalArgumentException If more than one {@link Class} type is passed in the <code>types</code>
     *  
     * @see org.jboss.ejb3.proxy.spi.factory.ProxyFactory#createProxy(java.lang.Class<?>[], java.lang.reflect.InvocationHandler)
     */
    public Object createProxy(Class<?>[] types, InvocationHandler invocationHandler) throws IllegalArgumentException
    {
       // we can only handle one type
       if (types.length == 0)
       {
          throw new IllegalArgumentException("Class type not specified for creating proxy");
       }
       if (types.length > 1)
       {
          throw new IllegalArgumentException("More than one class type specified for creating proxy");
       }
 
       javassist.util.proxy.ProxyFactory javassistProxyFactory = new javassist.util.proxy.ProxyFactory();
       // set the bean class for which we need a proxy 
       // Javassist internally uses the classloader of this "superclass" for
       // proxy creation
       javassistProxyFactory.setSuperclass(types[0]);
      // Set our method handler which is responsible for handling the method invocations
      // on the proxy
      javassistProxyFactory.setHandler(new JavassistInvocationHandlerAdapter(invocationHandler));

       // create the proxy
       Object proxy;
       try
       {
         proxy = javassistProxyFactory.create(new Class[0], new Object[0]);
       }
       catch (Exception e)
       {
          throw new RuntimeException("Could not create a proxy of type " + types[0], e);
       }
       // cast to the bean type and return
       return types[0].cast(proxy);
    }
 
 }
