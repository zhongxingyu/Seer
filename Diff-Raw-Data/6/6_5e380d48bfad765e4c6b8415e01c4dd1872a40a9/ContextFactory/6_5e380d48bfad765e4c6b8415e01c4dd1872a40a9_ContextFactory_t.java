 package com.sun.xml.bind.v2;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.StringTokenizer;
 
 import javax.xml.bind.JAXBContext;
 import javax.xml.bind.JAXBException;
 
 import com.sun.xml.bind.api.JAXBRIContext;
 import com.sun.xml.bind.api.TypeReference;
 import com.sun.xml.bind.v2.runtime.JAXBContextImpl;
 import com.sun.xml.bind.v2.util.FinalArrayList;
 
 /**
  * This class is responsible for producing RI JAXBContext objects.  In
  * the RI, this is the class that the javax.xml.bind.context.factory
  * property will point to.
  *
  * <p>
  * Used to create JAXBContext objects for v1.0.1 and forward
  *
  * @since 2.0
  * @author Kohsuke Kawaguchi
  */
 public class ContextFactory {
     /**
      * The API will invoke this method via reflection
      */
     public static JAXBContext createContext( Class[] classes, Map<String,Object> properties ) throws JAXBException {
         // fool-proof check, and copy the map to make it easier to find unrecognized properties.
         if(properties==null)
             properties = Collections.emptyMap();
         else
             properties = new HashMap<String,Object>(properties);
 
         String defaultNsUri = getPropertyValue(properties,JAXBRIContext.DEFAULT_NAMESPACE_REMAP,String.class);
 
         Boolean c14nSupport = getPropertyValue(properties,JAXBRIContext.CANONICALIZATION_SUPPORT,Boolean.class);
         if(c14nSupport==null)
             c14nSupport = false;
 
 
         if(!properties.isEmpty()) {
             throw new JAXBException(Messages.UNSUPPORTED_PROPERTY.format(properties.keySet().iterator().next()));
         }
 
         return createContext(classes,Collections.<TypeReference>emptyList(),defaultNsUri,c14nSupport);
     }
 
     /**
      * If a key is present in the map, remove the value and return it.
      */
     private static <T> T getPropertyValue(Map<String, Object> properties, String keyName, Class<T> type ) throws JAXBException {
         Object o = properties.get(keyName);
         if(o==null)     return null;
 
         properties.remove(keyName);
         if(!type.isInstance(o))
             throw new JAXBException(Messages.INVALID_PROPERTY_VALUE.format(keyName,o));
         else
             return type.cast(o);
     }
 
     /**
      * Used from the JAXB RI runtime API, invoked via reflection.
      */
     public static JAXBContext createContext( Class[] classes, Collection<TypeReference> typeRefs, String defaultNsUri, boolean c14nSupport ) throws JAXBException {
         return new JAXBContextImpl(classes,typeRefs,defaultNsUri,c14nSupport);
     }
 
     /**
      * The API will invoke this method via reflection.
      */
     public static JAXBContext createContext( String contextPath,
                                              ClassLoader classLoader, Map<String,Object> properties ) throws JAXBException {
         FinalArrayList<Class> classes = new FinalArrayList<Class>();
         StringTokenizer tokens = new StringTokenizer(contextPath,":");
         List<Class> indexedClasses;
 
         // at least on of these must be true per package
         boolean foundObjectFactory;
         boolean foundJaxbIndex;
 
         while(tokens.hasMoreTokens()) {
             foundObjectFactory = foundJaxbIndex = false;
             String pkg = tokens.nextToken();
 
             // look for ObjectFactory and load it
             final Class<?> o;
             try {
                 o = classLoader.loadClass(pkg+".ObjectFactory");
                 classes.add(o);
                 foundObjectFactory = true;
             } catch (ClassNotFoundException e) {
                 // not necessarily an error
             }
 
             // look for jaxb.index and load the list of classes
             try {
                 indexedClasses = loadIndexedClasses(pkg, classLoader);
             } catch (IOException e) {
                 //TODO: think about this more
                 throw new JAXBException(e);
             }
             if(indexedClasses != null) {
                 classes.addAll(indexedClasses);
                 foundJaxbIndex = true;
             }
 
             if( !(foundObjectFactory || foundJaxbIndex) ) {
                 throw new JAXBException( Messages.BROKEN_CONTEXTPATH.format(pkg));
             }
         }
 
 
         return createContext(classes.toArray(new Class[classes.size()]),properties);
     }
 
     /**
      * Look for jaxb.index file in the specified package and load it's contents
      *
      * @param pkg package name to search in
      * @param classLoader ClassLoader to search in
      * @return a List of Class objects to load, null if there weren't any
      * @throws IOException if there is an error reading the index file
      * @throws JAXBException if there are any errors in the index file
      */
     private static List<Class> loadIndexedClasses(String pkg, ClassLoader classLoader) throws IOException, JAXBException {
         final String resource = pkg.replace('.', '/') + "/jaxb.index";
         final InputStream resourceAsStream = classLoader.getResourceAsStream(resource);
 
         if (resourceAsStream == null) {
             return null;
         }
 
         BufferedReader in =
                 new BufferedReader(new InputStreamReader(resourceAsStream, "UTF-8"));
         try {
             FinalArrayList<Class> classes = new FinalArrayList<Class>();
             String className = in.readLine();
             while (className != null) {
                 className = className.trim();
                 if (className.startsWith("#") || (className.length() == 0)) {
                     className = in.readLine();
                     continue;
                 }
                 int dot = className.indexOf('.');
                if (dot != -1) {
                    throw new JAXBException(Messages.ILLEGAL_ENTRY.format(className));
                 }
 
                 try {
                     classes.add(classLoader.loadClass(pkg + '.' + className));
                 } catch (ClassNotFoundException e) {
                     throw new JAXBException(Messages.ERROR_LOADING_CLASS.format(className, resource));
                 }
 
                 className = in.readLine();
             }
             return classes;
         } finally {
             in.close();
         }
     }
 }
