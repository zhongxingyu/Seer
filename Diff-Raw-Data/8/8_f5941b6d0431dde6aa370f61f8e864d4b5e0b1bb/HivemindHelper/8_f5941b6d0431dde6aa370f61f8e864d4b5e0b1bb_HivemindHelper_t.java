 /*
  * HivemindHelper.java
  *
  * Created on December 21, 2006, 4:16 AM
  */
 
 package org.amplafi.hivemind.util;
 
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Locale;
 import java.util.Map;
 import java.util.concurrent.CopyOnWriteArrayList;
 
 import org.apache.commons.logging.Log;
 import org.apache.hivemind.ClassResolver;
 import org.apache.hivemind.ErrorHandler;
 import org.apache.hivemind.Location;
 import org.apache.hivemind.ModuleDescriptorProvider;
 import org.apache.hivemind.Registry;
 import org.apache.hivemind.Resource;
 import org.apache.hivemind.impl.DefaultClassResolver;
 import org.apache.hivemind.impl.DefaultErrorHandler;
 import org.apache.hivemind.impl.RegistryBuilder;
 import org.apache.hivemind.util.URLResource;
 
 /**
  * Static access to several hivemind related utilities.
  *
  * @author andyhot
  */
 public final class HivemindHelper {
     private final static HivemindHelper INSTANCE = new HivemindHelper();
 
     private Map<String, Registry> registries;
 
     /**
      * Allow only one instance.
      */
     private HivemindHelper() {
         registries = new HashMap<String, Registry>();
     }
 
     /**
      * @return the only HivemindHelper instance.
      */
     public static HivemindHelper instance() {
         return INSTANCE;
     }
 
     /**
      * Builds a minimal registry, containing only the specified files, plus the
      * master module descriptor (i.e., those visible on the classpath).
      * @param file
      * @return the registry
      * @throws Exception
      */
     public static Registry createFrameworkRegistry(String file)
             throws Exception {
         return INSTANCE.buildFrameworkRegistry(file);
     }
 
     /**
      * Builds a minimal registry, containing only the specified files, plus the
      * master module descriptor (i.e., those visible on the classpath).
      *
      * @param files
      *            The path to the hivemind xml configuration files to parse.
      * @return The constructed registry.
      *
      * @throws Exception
      *             When file can't be found or parsed.
      */
     public Registry buildFrameworkRegistry(String... files) throws Exception {
         return buildFrameworkRegistry(null, false, files);
     }

     public Registry buildFrameworkRegistry(String skipPattern, boolean skipFilesystem,
             String... files) throws Exception {
 
         ClassResolver resolver = getClassResolver();
 
         List<Resource> descriptorResources = new ArrayList<Resource>();
         for (String file : files) {
             Resource resource = getResource(file);
 
             descriptorResources.add(resource);
         }
 
         ModuleDescriptorProvider provider = new CustomModuleDescriptorProvider(
                 resolver, descriptorResources);
 
 
         return buildFrameworkRegistry(provider, skipPattern, skipFilesystem);
     }
 
     /**
      * Builds a registry from exactly the provided resource; this registry will
      * not include the <code>hivemind</code> module.
      * @param file
      * @return the registry
      * @throws Exception
      */
     public static Registry createMinimalRegistry(String file) throws Exception {
         Resource l = INSTANCE.getResource(file);
         return INSTANCE.buildMinimalRegistry(l);
     }
 
     /**
      * Get or create a hivemind registry.
      *
      * @param file
      *            The hivemind descriptor
      * @param shared
      *            If false, a new registry is always created. Otherwise a stored
      *            one is searched. If not found, a new one is created and also
      *            stored for later use.
      * @return the registry existing or a newly created one.
      * @throws Exception
      */
     public Registry getRegistry(String file, boolean shared) throws Exception {
             return getRegistry(file, shared, null, false);
     }
 
    public Registry getRegistry(String file, boolean shared,
             String skipPattern, boolean skipFiles ) throws Exception {
         Registry registry = null;
         // TODO: Always create a new registry for now.
         // OOME if we have too many registries created.
         shared = false;
         if (shared) {
             registry = registries.get(file);
         }
 
         if (registry == null) {
             registry = buildFrameworkRegistry(skipPattern, skipFiles, file);
 
             if (shared) {
                 registries.put(file, registry);
             }
         }
 
         return registry;
     }
 
     protected ClassResolver getClassResolver() {
         return new DefaultClassResolver();
     }
 
     /**
      * Returns the given file as a {@link Resource} from the classpath.
      * Typically, this is to find files in the same folder as the invoking
      * class.
      *
      * @param file
      *            Gets a resource object for the file representing the path
      *            specified.
      * @return A {@link Resource} object.
      */
     protected Resource getResource(String file) {
         URL url = getClass().getResource(file);
 
         if (url == null) {
             throw new NullPointerException("No resource named '" + file + "'.");
         }
 
         return new URLResource(url);
     }
 
     /**
      * Shutdown and clear all stored hivemind registries.
      */
     public void cleanUpRegistries() {
         for (Registry registry : registries.values()) {
             registry.shutdown();
         }
         registries.clear();
     }
 
     protected Registry buildFrameworkRegistry(ModuleDescriptorProvider customProvider,
             String skipPattern, boolean skipFilesystem) {
         ClassResolver resolver = getClassResolver();
 
        RegistryBuilder builder = new RegistryBuilder(/*new QuietErrorHandler()*/);
 
         CustomModuleDescriptorProvider provider = new CustomModuleDescriptorProvider(
                 resolver, skipPattern, skipFilesystem, false);
 
         builder.addModuleDescriptorProvider(provider);
         builder.addModuleDescriptorProvider(customProvider);
 
         return builder.constructRegistry(Locale.getDefault());
     }
 
     @SuppressWarnings("unused")
     protected Registry buildMinimalRegistry(Resource l) throws Exception {
         RegistryBuilder builder = new RegistryBuilder(/*new QuietErrorHandler()*/);
         return builder.constructRegistry(Locale.getDefault());
     }
 
     /**
      * keeps quiet about error messages.
      *
      * @author Patrick Moore
      */
     private class QuietErrorHandler extends DefaultErrorHandler implements
             ErrorHandler {
         private List<ErrorMsg> errorMsgs = new CopyOnWriteArrayList<ErrorMsg>();
 
         @Override
         public void error(Log log, String message, Location location,
                 Throwable cause) {
             if (message != null
                 && (message.startsWith("No module has contributed")
                     || message.contains("contains no contributions but expects at least")
                     || message.contains("has contributed to unknown configuration point"))) {
                 errorMsgs.add(new ErrorMsg(location, message, cause));
             } else {
                 super.error(log, message, location, cause);
             }
         }
 
     }
 
     /**
      * sucks up error messages.
      *
      * @author Patrick Moore
      */
     private class ErrorMsg {
 
         @SuppressWarnings("unused")
         private Location location;
 
         @SuppressWarnings("unused")
         private String message;
 
         @SuppressWarnings("unused")
         private Throwable cause;
 
         public ErrorMsg(Location location, String message, Throwable cause) {
             this.location = location;
             this.message = message;
             this.cause = cause;
         }
 
     }
 }
