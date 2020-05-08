 // Copyright 2008 Waterken Inc. under the terms of the MIT X license
 // found at http://www.opensource.org/licenses/mit-license.html
 package org.web_send.split;
 
 import java.io.Serializable;
 import java.lang.reflect.InvocationHandler;
 import java.lang.reflect.Method;
 import java.lang.reflect.Proxy;
 
 import org.joe_e.Selfless;
 import org.joe_e.Struct;
 import org.joe_e.reflect.Proxies;
 import org.joe_e.reflect.Reflection;
 import org.web_send.graph.Framework;
 import org.web_send.graph.Spawn;
 
 /**
  * A proxy that runs each invocation in a newly {@linkplain Spawn#run spawned}
  * vat.
  */
 public final class
 Splitter {
     
     private
     Splitter() {}
 
     /**
      * Constructs an instance.
      * @param <T> referent type
      * @param spawn sub-vat factory
      * @param maker {@linkplain Spawn#run maker} of objects of type
      *              <code>T</code>
      */
    static public <T> T
     make(final Spawn spawn, final Class<?> maker) {
         class X extends Struct implements InvocationHandler, Serializable {
             static private final long serialVersionUID = 1L;
 
             public Object
             invoke(final Object proxy,
                    final Method method, final Object[] args) throws Exception {
                 if (Object.class == method.getDeclaringClass()) {
                     if ("equals".equals(method.getName())) {
                         return args[0] instanceof Proxy &&
                             proxy.getClass() == args[0].getClass() &&
                             equals(Proxies.getHandler((Proxy)args[0]));
                     } else {
                         return Reflection.invoke(method, this, args);
                     }
                 }
                 return Reflection.invoke(method, spawn.run(maker), args);
             }
         }
         final Method build;
         try {
             build = Reflection.method(maker, "build", Framework.class);
         } catch (final NoSuchMethodException e){throw new ClassCastException();}
        final Class<?> T = build.getReturnType();
        return (T)Proxies.proxy(new X(), T, Selfless.class);
     }
 }
