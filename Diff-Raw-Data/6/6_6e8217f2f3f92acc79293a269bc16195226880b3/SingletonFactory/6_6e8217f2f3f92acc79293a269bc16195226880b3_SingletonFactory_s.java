 package jDistsim.utils.pattern.singleton;
 
 import jDistsim.utils.logging.Logger;
 
 import java.util.HashMap;
 import java.util.Map;
 
 /**
  * Author: Jirka Pénzeš
  * Date: 28.9.12
  * Time: 15:13
  */
 public class SingletonFactory {
 
     private static Map<Class<?>, Object> instanceMap = new HashMap<Class<?>, Object>();
     private static volatile Object lock = new Object();
 
     private SingletonFactory() {
     }
 
     public static synchronized <TInstance> TInstance getInstance(Class<TInstance> singletonClass) {
         TInstance instance = singletonClass.cast(instanceMap.get(singletonClass));
 
         if (instance != null) {
             return instance;
         }
 
         synchronized (lock) {
             try {
                 instance = singletonClass.cast(singletonClass.newInstance());
             } catch (Exception exception) {
                 Logger.log(exception);
             }
             instanceMap.put(singletonClass, instance);
         }
         return instance;
     }
 }
