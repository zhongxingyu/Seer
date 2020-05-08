 package edu.northwestern.bioinformatics.studycalendar.utility.osgimosis;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import java.lang.reflect.Method;
 import java.lang.reflect.Modifier;
 import java.lang.reflect.Constructor;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.LinkedHashSet;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Set;
 import java.util.Map;
 
 /**
  * @author Rhett Sutphin
  */
 @SuppressWarnings({ "RawUseOfParameterizedType" })
 public class DefaultEncapsulatorCreator {
     private final Logger log = LoggerFactory.getLogger(getClass());
 
     private Membrane membrane;
     private Class farClass;
     private ClassLoader nearClassLoader;
     private ClassLoader farClassLoader;
     private Map<String, Object[]> proxyConstructorParams;
 
     public DefaultEncapsulatorCreator(Membrane membrane, Class farClass, ClassLoader nearClassLoader, ClassLoader farClassLoader, Map<String, Object[]> proxyConstructorParams) {
         this.membrane = membrane;
         this.farClass = farClass;
         this.nearClassLoader = nearClassLoader;
         this.farClassLoader = farClassLoader;
         this.proxyConstructorParams = proxyConstructorParams;
     }
 
     public Encapsulator create() {
         if (Collection.class.isAssignableFrom(farClass)) {
             return new CollectionEncapsulator(membrane);
         } else if (farClass.isArray()) {
             log.trace(" - Encapsulating array with components {}", farClass.getComponentType());
             Encapsulator componentEncapsulator = new DefaultEncapsulatorCreator(
                 membrane, farClass.getComponentType(), nearClassLoader, farClassLoader, proxyConstructorParams).create();
             if (componentEncapsulator == null) {
                 return null;
             } else if (componentEncapsulator instanceof ArrayCapableEncapsulator) {
                 return new ArrayEncapsulator((ArrayCapableEncapsulator) componentEncapsulator);
             } else {
                 throw new MembraneException("Cannot encapsulate array; component type is not array capable");
             }
         } else if (nearClassLoader == null) {
             log.trace(" - Not proxying object from bootstrap classloader");
             return null;
         } else {
             Class base = sharedBaseClass();
             List<Class> interfaces = sharedInterfaces();
             if (base != null || !interfaces.isEmpty()) {
                 return new ProxyEncapsulator(
                     membrane, nearClassLoader,
                     base, base == null ? null : proxyConstructorParams.get(base.getName()),
                     interfaces,
                     farClassLoader
                 );
             }
         }
 
         return null;
     }
 
     private Class targetClass(Class sourceClass) {
         try {
             return nearClassLoader.loadClass(sourceClass.getName());
         } catch (ClassNotFoundException ex) {
            log.debug("Was not able to find a class matching {} in the target class loader {}",
                 sourceClass.getName(), nearClassLoader);
            return null;
         }
     }
 
     private Class sharedBaseClass() {
         Class base = null;
         Class sourceClass = farClass;
         while (sourceClass != null && base == null) {
             if (isInSharedPackage(sourceClass) && proxyConstructable(sourceClass) && !hasFinalMethods(sourceClass)) {
                 base = targetClass(sourceClass);
             }
             sourceClass = sourceClass.getSuperclass();
         }
         log.trace("Base class will be {}", base);
         return base;
     }
 
     private boolean hasFinalMethods(Class sourceClass) {
         for (Method method : sourceClass.getDeclaredMethods()) {
             if (Modifier.isFinal(method.getModifiers())) return true;
         }
         return false;
     }
 
     private boolean proxyConstructable(Class<?> clazz) {
         if (proxyConstructorParams == null || proxyConstructorParams.get(clazz.getName()) == null) {
             try {
                 Constructor<?> defaultConstructor = clazz.getDeclaredConstructor();
                 return !Modifier.isPrivate(defaultConstructor.getModifiers());
             } catch (NoSuchMethodException e) {
                 return false;
             }
         } else {
             return true;
         }
     }
 
     private List<Class> sharedInterfaces() {
         Set<Class> result = new LinkedHashSet<Class>();
         Class[] allInterfaces = ReflectionTools.getAllInterfaces(farClass);
         log.trace("All interfaces are {}", Arrays.asList(allInterfaces));
         for (Class<?> interfac : allInterfaces) {
             if (isInSharedPackage(interfac) && isAvailableInTargetClassLoader(interfac)) {
                Class target = targetClass(interfac);
                if (target != null) result.add(target);
             }
         }
         log.trace("Selected interfaces are {}", result);
         return new LinkedList<Class>(result);
     }
 
     private boolean isInSharedPackage(Class<?> interfac) {
         return membrane.getSharedPackages().contains(ReflectionTools.getPackageName(interfac));
     }
 
     private boolean isAvailableInTargetClassLoader(Class<?> sourceClass) {
         try {
             nearClassLoader.loadClass(sourceClass.getName());
             return true;
         } catch (ClassNotFoundException e) {
             log.debug("{} is in a shared package, but is not available from {}.  Skipping.", sourceClass, nearClassLoader);
             return false;
         }
     }
 }
