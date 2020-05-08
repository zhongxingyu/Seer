 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.smartitengineering.demo.wicket.groovy;
 
 import groovy.lang.Closure;
 import java.lang.reflect.Method;
 import java.lang.reflect.Modifier;
 import java.util.ArrayList;
 import java.util.Arrays;
 import net.sf.cglib.proxy.MethodInterceptor;
 import net.sf.cglib.proxy.MethodProxy;
 
 /**
  *
  * @author imyousuf
  */
 public class AbstractMethodHandler
         implements MethodInterceptor{
 
   private Closure closure;
 
   public AbstractMethodHandler(Closure closure) {
     if (closure == null) {
       throw new IllegalArgumentException("Closure can't be Null!");
     }
     this.closure = closure;
   }
 
   public Object intercept(Object obj,
                           Method method,
                           Object[] args,
                           MethodProxy proxy)
           throws Throwable {
     if (Modifier.isAbstract(method.getModifiers())) {
       ArrayList<Object> list = new ArrayList();
       list.add(method.getName());
       list.add(obj);
       list.addAll(Arrays.asList(args));
       return closure.call(list.toArray(new Object[list.size()]));
     }
     else {
       return proxy.invokeSuper(obj, args);
     }
   }
 }
