 package com.avast.syringe.aop.cglib;
 
 import com.avast.syringe.aop.Interceptor;
 import com.avast.syringe.aop.MethodPointcut;
 import com.avast.syringe.aop.ProxyFactory;
 import net.sf.cglib.proxy.Callback;
 import net.sf.cglib.proxy.CallbackFilter;
 import net.sf.cglib.proxy.Enhancer;
 import net.sf.cglib.proxy.NoOp;
 
 /**
  * User: vacata
  * Date: 1/29/13
  * Time: 4:27 PM
  */
 public class CglibProxyFactory implements ProxyFactory {
 
     @Override
     public boolean canCreate(Class<?>[] types) {
        return (evalProxyType(types) != ProxyType.UNSUPPORTED);
     }
 
     @Override
     public Object createProxy(Interceptor interceptor, Class<?>[] types, MethodPointcut methodPointcut) {
         ProxyType proxyType = evalProxyType(types);
         Enhancer eh = new Enhancer();
         DefaultMethodInterceptor dmi = new DefaultMethodInterceptor(interceptor);
        DefaultDispatcher dispatcher = new DefaultDispatcher(interceptor.getTarget());
         Callback[] callbacks = new Callback[] {
            dmi, dispatcher
         };
         eh.setCallbacks(callbacks);
         CallbackFilter cf = new CallbackFilterAdapter(methodPointcut);
         eh.setCallbackFilter(cf);
 
         switch (proxyType) {
             case CLASS:
                 Class<?> clazz = types[0];
                 eh.setSuperclass(clazz);
                 return eh.create();
             case INTERFACES:
                 eh.setInterfaces(types);
                 return eh.create();
         }
 
         throw new UnsupportedOperationException("Unsupported proxy types!");
     }
 
     private static enum ProxyType {
         CLASS, INTERFACES, UNSUPPORTED
     }
 
     public ProxyType evalProxyType(Class<?>[] types) {
         if (types.length == 0) {
             return ProxyType.UNSUPPORTED;
         }
         //Proxy a collection of interfaces
         if (types[0].isInterface()) {
             for (Class<?> clazz : types) {
                 if (!clazz.isInterface()) {
                     return ProxyType.INTERFACES;
                 }
             }
         }
         //Proxy a single class
         if (types.length == 1) {
             return ProxyType.CLASS;
         }
         return ProxyType.UNSUPPORTED;
     }
 }
