 /*******************************************************************************
  * Copyright (c) 2004, 2012 BREDEX GmbH.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     BREDEX GmbH - initial API and implementation and/or initial documentation
  *******************************************************************************/
 package org.eclipse.jubula.rc.common.adaptable;
 
 import java.io.File;
 import java.io.IOException;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Enumeration;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.jar.JarEntry;
 import java.util.jar.JarFile;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * Singleton to register adapter factories
  */
 public class AdapterFactoryRegistry {
     /**
      * the name of the package to search for adapters
      */
     private static final String ADAPTER_PACKAGE_NAME = "org.eclipse.jubula.rc.common.adapter"; //$NON-NLS-1$
 
     /** the logger */
     private static Logger log = LoggerFactory
             .getLogger(AdapterFactoryRegistry.class);
 
     /**
      * Singleton instance of this class
      */
     private static AdapterFactoryRegistry instance = 
         new AdapterFactoryRegistry();
 
     /**
      * Map that manages the registration. Key is always a class Value is a
      * collection of IAdapterFactory
      */
     private Map m_registrationMap = new HashMap();
 
     /**
      * Call Constructor only by using getInstance
      */
     private AdapterFactoryRegistry() {
     }
 
     /**
      * Return the singleton of this class
      * 
      * @return singleton
      */
     public static AdapterFactoryRegistry getInstance() {
         return instance;
     }
 
     /**
      * Register adapter factory with all its supported classes
      * 
      * @param factory
      *            adapter factory that should be registered
      */
     public void registerFactory(IAdapterFactory factory) {
         Class[] supportedClasses = factory.getSupportedClasses();
         for (int i = 0; i < supportedClasses.length; i++) {
             Collection registeredFactories = (Collection) m_registrationMap
                     .get(supportedClasses[i]);
             if (registeredFactories == null) {
                 registeredFactories = new ArrayList();
             }
             registeredFactories.add(factory);
             m_registrationMap.put(supportedClasses[i], registeredFactories);
         }
     }
 
     /**
      * Sign off adapter factory from all its supported classes
      * 
      * @param factory
      *            adapter factory that should be signed off
      */
     public void signOffFactory(IAdapterFactory factory) {
         Class[] supportedClasses = factory.getSupportedClasses();
         for (int i = 0; i < supportedClasses.length; i++) {
             Collection registeredFactories = (Collection) m_registrationMap
                    .get(supportedClasses[i]);
             if (registeredFactories == null) {
                 return;
             }
             registeredFactories.remove(factory);
            m_registrationMap.put(supportedClasses[i], registeredFactories);
         }
     }
 
     /**
      * 
      * @param targetAdapterClass
      *            Type of the adapter
      * @param objectToAdapt
      *            object that should be adapted to
      * @return Returns an adapter for the objectToAdapt of type
      *         targetAdapterClass. The collection of all supported adapter
      *         factories is iterated. The first value that is not null will be
      *         returned. Null will only be returned if no adapter can be found
      *         for the targetAdapterClass or none of the given factories can
      *         handle the objectToAdapt
      */
     public Object getAdapter(Class targetAdapterClass, Object objectToAdapt) {
         Collection registeredFactories = null;
         Class superClass = targetAdapterClass;
         while (registeredFactories == null && superClass != Object.class) {
             registeredFactories = (Collection) m_registrationMap
                     .get(superClass);
             superClass = superClass.getSuperclass();
         }
         if (registeredFactories == null) {
             return null;
         }
         for (Iterator iterator = registeredFactories.iterator(); iterator
                 .hasNext();) {
             IAdapterFactory adapterFactory = (IAdapterFactory) iterator.next();
             Object object = adapterFactory.getAdapter(targetAdapterClass,
                     objectToAdapt);
 
             if (object != null) {
                 return object;
             }
         }
         return null;
     }
 
     /**
      * Must be called to initialize the registration of adapters
      * 
      * @throws InstantiationException
      * @throws IllegalAccessException
      */
     public static void initRegistration() {
         Class[] adapterFactories = findClassesOfType(ADAPTER_PACKAGE_NAME,
                 IAdapterFactory.class);
 
         //Register all found factories
         for (int i = 0; i < adapterFactories.length; i++) {
             try {
                 IAdapterFactory factory = (IAdapterFactory) adapterFactories[i]
                         .newInstance();
                 getInstance().registerFactory(factory);
             } catch (IllegalAccessException e) {
                 log.error(e.getLocalizedMessage(), e);
             } catch (InstantiationException e) {
                 log.error(e.getLocalizedMessage(), e);
             }
         }
     }
 
     
     /**
      * Investigate a package of subclasses of a specific superclass
      * 
      * @param packageName
      *            name of the package
      * @param superclass
      *            parent class for found classes
      * @return found classes
      */
     private static Class[] findClassesOfType(String packageName,
             Class superclass) {
         try {
             Class[] allClasses = getClasses(packageName);
 
             List assignableClasses = new ArrayList();
             for (int i = 0; i < allClasses.length; i++) {
                 if (superclass.isAssignableFrom(allClasses[i]) 
                         && superclass != allClasses[i]) {
                     assignableClasses.add(allClasses[i]);
                 }
             }
             return castListToClassArray(assignableClasses);
         } catch (ClassNotFoundException e) {
             return new Class[0];
         } catch (IOException e) {
             return new Class[0];
         }
     }
 
     /**
      * Cast a list of classes to an array of classes
      * 
      * @param classes
      *            List of classes
      * @return array of classes
      */
     private static Class[] castListToClassArray(List classes) {
         Class[] arrayClasses = new Class[classes.size()];
         for (int i = 0; i < arrayClasses.length; i++) {
             arrayClasses[i] = (Class) classes.get(i);
         }
         return arrayClasses;
     }
     
     /**
      * Scans all classes accessible from the context class loader which belong
      * to the given package and subpackages.
      * 
      * @param packageName
      *            The base package
      * @return The classes
      * @throws ClassNotFoundException
      * @throws IOException
      */
     private static Class[] getClasses(String packageName)
         throws ClassNotFoundException, IOException {
         ClassLoader classLoader = AdapterFactoryRegistry.class.getClassLoader();
         String path = packageName.replace('.', '/');
         Enumeration resources = classLoader.getResources(path);
         List dirs = new ArrayList();
 
         while (resources.hasMoreElements()) {
             URL resource = (URL) resources.nextElement();
             dirs.add(resource);
         }
         List classes = new ArrayList();
         for (int i = 0; i < dirs.size(); i++) {
             if (dirs.get(i).toString().startsWith("jar:")) { //$NON-NLS-1$
                 classes.addAll(findClassesInJar((URL)dirs.get(i), packageName));
             } else {
                 classes.addAll(findClasses((URL) dirs.get(i), packageName));
             }
         }
         return castListToClassArray(classes);
     }
 
 
     /**
      * Recursive method used to find all classes in a given directory and
      * subdirectories.
      * 
      * @param directoryUrl
      *            The base directory
      * @param packageName
      *            The package name for classes found inside the base directory
      * @return The classes
      * @throws ClassNotFoundException
      */
     private static List findClasses(URL directoryUrl, String packageName)
         throws ClassNotFoundException {
         List classes = new ArrayList();
         File directory = new File(directoryUrl.getFile());
         if (!directory.exists()) {
             return classes;
         }
         File[] files = directory.listFiles();
         for (int i = 0; i < files.length; i++) {
             File file = files[i];
             String fileName = file.getName();
             if (file.isDirectory()) {
                 try {
                     classes.addAll(findClasses(file.toURI().toURL(),
                             packageName + '.' + fileName));
                 } catch (MalformedURLException e) {
                     log.error(e.getLocalizedMessage(), e);
                 }
             } else if (fileName.endsWith(".class")) { //$NON-NLS-1$
                 classes.add(Class.forName(packageName + '.'
                         + fileName.substring(0, fileName.length() - 6)));
             }
         }
         return classes;
     }
 
     /**
      * method to find all classes in a given jar
      * 
      * @param resource
      *            The url to the jar file
      * @param pkgname
      *            The package name for classes found inside the base directory
      * @return The classes
      */
     private static List findClassesInJar(URL resource, String pkgname) {
         String relPath = pkgname.replace('.', '/');
         String path = resource.getPath()
                 .replaceFirst("[.]jar[!].*", ".jar") //$NON-NLS-1$ //$NON-NLS-2$
                 .replaceFirst("file:", ""); //$NON-NLS-1$ //$NON-NLS-2$
         List classes = new ArrayList();
         try {            
             JarFile jarFile = new JarFile(path);        
             Enumeration entries = jarFile.entries();
             while (entries.hasMoreElements()) {
                 JarEntry entry = (JarEntry) entries.nextElement();
                 String entryName = entry.getName();
                 String className = null;
                 if (entryName.endsWith(".class")  //$NON-NLS-1$
                         && entryName.startsWith(relPath)) {
                     className = entryName.replace('/', '.').replace('\\', '.')
                             .replaceAll(".class", ""); //$NON-NLS-1$ //$NON-NLS-2$
                 
                     if (className != null) {
                         try {
                             classes.add(Class.forName(className));
                         } catch (ClassNotFoundException cnfe) {
                             log.error(cnfe.getLocalizedMessage(), cnfe);
                         }
                     }
                 }
             }
         } catch (IOException ioe) {            
             log.warn(ioe.getLocalizedMessage(), ioe);
         }
         return classes;
     }
     
 }
