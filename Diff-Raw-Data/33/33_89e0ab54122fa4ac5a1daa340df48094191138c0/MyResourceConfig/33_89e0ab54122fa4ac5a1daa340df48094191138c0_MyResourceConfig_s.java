 package org.ow2.chameleon.rose.rest;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import javax.ws.rs.Path;
 
 import com.sun.jersey.api.core.DefaultResourceConfig;
 import com.sun.jersey.core.spi.component.ioc.IoCComponentProvider;
 
 public class MyResourceConfig extends DefaultResourceConfig {
 
     private final Map<Class<?>, IoCComponentProvider> providers = new HashMap<Class<?>, IoCComponentProvider>();
 
     private final Map<String, Class<?>> paths = new HashMap<String, Class<?>>();
 
     public void addComponentProvider(Class<?> pClass, IoCComponentProvider pProvider) throws IllegalArgumentException {
         // Check if the class is a valid ressource
         if (!isRootResourceClass(pClass)) {
             throw new IllegalArgumentException("The class: " + pClass.getCanonicalName() + " is not a root ressource class.");
         }
 
         // Check if the pass has not already been registered
         String path = pClass.getAnnotation(Path.class).value();
 
         if (paths.containsKey(path)) {
             throw new IllegalArgumentException("A ressource of path: " + path + " has already been registered.");
         }
 
         paths.put(path, pClass);
         providers.put(pClass, pProvider);
         getClasses().add(pClass);
 
     }
 
     public void removeComponentProvider(String path) {
         if (!paths.containsKey(path)) {
             throw new IllegalArgumentException("There is no ressource registered with the path: " + path);
         }
         Class<?> klass = paths.remove(path);
         providers.remove(klass);
         getClasses().remove(klass);
     }
 
     public boolean isManaged(Class<?> pClass) {
         return providers.containsKey(pClass);
     }
 
     public IoCComponentProvider getComponentProvider(Class<?> pClass) {
         return providers.get(pClass);
     }
 }
