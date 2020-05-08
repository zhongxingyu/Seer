 package org.geworkbench.engine.management;
 
 import java.net.URLClassLoader;
 import java.net.URL;
 import java.io.File;
 import java.io.IOException;
 import java.util.List;
 import java.util.ArrayList;
 
 /**
  * Represents a component (or set of components), their configuration, and required libraries.
  * <p/>
  * Component Resource directories must have the component classes in a subdirectory called
  * <tt>classes</tt> and a lib directory in a subdirectory called <tt>lib</tt>. Only
  * <tt>.zip</tt> and <tt>.jar</tt> files will be loaded from the lib directory.
  * <p/>
  * Visual components may have an optional <tt>.cwb.xml</tt> file with component configuration
  * in the <tt>classes</tt> directory at the same level as the class that extends
  * {@link org.geworkbench.engine.config.VisualPlugin}.
  *
  * @author John Watkinson
  */
 public class ComponentResource {
 
     private static final String LIB_DIR = "lib";
     private static final String CLASSES_DIR = "classes";
 
     /**
      * The directory in which the component resides.
      */
     private String dir;
 
     /**
      * The class loader for the resource.
      */
     private URLClassLoader classLoader;
 
     /**
      * Used to search for classes of a certain type within this resource.
      */
     private ClassSearcher classSearcher;
 
     /**
      * Creates a new component resource rooted in the given directory.
      *
      * @param dir the directory for the component resource.
      */
     public ComponentResource(String dir) throws IOException {
         this.dir = dir;
         classLoader = createClassLoader();
     }
 
     private URLClassLoader createClassLoader() throws IOException {
         // Do classes dir
         File classesDir = new File(dir + '/' + CLASSES_DIR);
         List<URL> urls = new ArrayList<URL>();
         if (classesDir.exists()) {
            URL baseURL = classesDir.toURL();            
             urls.add(baseURL);
             // Create ClassSearcher based on classes path
             classSearcher = new ClassSearcher(new URL[] {baseURL});
         }
         // Do libs
         File libdir = new File(dir + '/' + LIB_DIR);
         if (libdir.exists()) {
             File[] libFiles = libdir.listFiles();
             for (int i = 0; i < libFiles.length; i++) {
                 File file = libFiles[i];
                 if (!file.isDirectory()) {
                     String name = file.getName().toLowerCase();
                     if (name.endsWith(".jar") || name.endsWith(".zip")) {
                         urls.add(file.toURL());
                     }
                 }
             }
         }
         URL[] classpath = new URL[urls.size()];
         for (int i = 0; i < urls.size(); i++) {
             classpath[i] = (URL) urls.get(i);
         }
         return new URLClassLoader(classpath);
     }
 
     public ClassLoader getClassLoader() {
         return classLoader;
     }
 
 }
