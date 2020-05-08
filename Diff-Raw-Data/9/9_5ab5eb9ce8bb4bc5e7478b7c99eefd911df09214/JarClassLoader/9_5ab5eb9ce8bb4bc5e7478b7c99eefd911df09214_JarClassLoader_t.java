 // JarClassLoader
 //--------------------------------------------------------------------------
 // $Revision$
 // $Date$
 // $Author$
 //--------------------------------------------------------------------------
 package cytoscape.plugin;
 //--------------------------------------------------------------------------
 import java.net.*;
 import java.lang.reflect.*;
 import java.util.jar.*;
 import java.util.*;
 
 import cytoscape.plugin.AbstractPlugin;
 import cytoscape.CytoscapeObj;
 import cytoscape.plugin.*;
 
 /**
  * A class loader for loading jar files, both local and remote.
  * This class is derived from an online example:
  * http://java.sun.com/docs/books/tutorial/jar/api/jarclassloader.html
  */
 public class JarClassLoader extends URLClassLoader {
     private URL url;
     private CytoscapeObj cyObj;
     /**
      * Creates a new JarClassLoader for the specified url.
      *
      * @param urlString the url of the jar file
      * @param cyObj the shared cytoscape object
      */
     public JarClassLoader(String urlString, CytoscapeObj cyObj)
             throws MalformedURLException {
      super(new URL[] { new URL(urlString) },
            JarClassLoader.class.getClassLoader()); 
      this.cyObj = cyObj;
         this.url = new URL("jar", "", urlString + "!/");
     }
 
     /**
      * browses the jar file for any classes that extend AbstractPlugin;
      * upon finding such classes, add them to the plug in registry.
      */
     public void loadRelevantClasses() {
         JarURLConnection uc=null;
         try {
             uc = (JarURLConnection)url.openConnection();
             if(uc==null) throw(new Exception("null URL Connection"));
 
             JarFile jf = uc.getJarFile();
             if(jf==null) throw(new Exception("null JarFile from URL"));
 
             Enumeration entries = jf.entries();
             if(entries==null) throw(new Exception("null jar entries"));
 
             int totalEntries=0;
             int totalClasses=0;
             int totalPlugins=0;
 
             while(entries.hasMoreElements()) {
                 totalEntries++;
                 Object entry_o = entries.nextElement();
                 String entry_s = entry_o.toString();
                 if(!(entry_s.endsWith(".class"))) continue;
                 totalClasses++;
                 String className = PathAndFilenameToClassname(entry_s);
                 if(!(isClassPlugin(className))) continue;
                 totalPlugins++;
                 invokePlugin(className);
             }
             System.out.println(".jar summary: " +
                     " entries=" + totalEntries +
                     " classes=" + totalClasses +
                     " plugins=" + totalPlugins);
         }
         catch (Exception e) {
             System.err.println ("Error thrown: " + e.getMessage ());
             if(uc==null) System.out.println("uc is null 4e");
         }
     }
 
     /**
      * Invokes the application in this jar file given the name of the
      * main class and an array of arguments. The class must define a
      * static method "main" which takes an array of String arguemtns
      * and is of return type "void".
      * @deprecated This method is no longer maintained.
      * @param name the name of the main class
      * @param args the arguments for the application
      * @exception ClassNotFoundException if the specified class could not
      *            be found
      * @exception NoSuchMethodException if the specified class does not
      *            contain a "main" method
      * @exception InvocationTargetException if the application raised an
      *            exception
      */
     public void invokeClass(String name, String[] args)
             throws ClassNotFoundException,
             NoSuchMethodException,
             InvocationTargetException
     {
         Class c = loadClass(name);
         Method m = c.getMethod("main", new Class[] { args.getClass() });
         m.setAccessible(true);
         int mods = m.getModifiers();
         if (m.getReturnType() != void.class || !Modifier.isStatic(mods) ||
                 !Modifier.isPublic(mods)) {
             throw new NoSuchMethodException("main");
         }
         try {
             m.invoke(null, new Object[] { args });
         } catch (IllegalAccessException e) {
             // This should not happen, as we have disabled access checks
         }
     }
 
     /**
      * Invokes the application in this jar file given the name of the
      * main class and assuming it is a plugin.
      *
      * @param name the name of the plugin class
      */
     public void invokePlugin(String name)
     {
         try {
             Class pluginClass = loadClass(name);
             cyObj.getPluginRegistry().addPluginToRegistry(pluginClass);
             System.out.println("Loaded plugin: " + name);
         }
         catch (ClassNotFoundException e) {
             System.err.println("Error: plugin class " + name + " was not found.\n");
         }
 
         catch (NotAPluginException e) {
             System.err.println("Error: plugin class " + name + " is not a plugin.\n");
         }
         catch (PluginAlreadyRegisteredException e) {
             System.err.println("Error: plugin class " + name + " already Registered.\n");
         }
     }
 
 
     /**
      * Determines whether the class with a particular name
      * extends AbstractPlugin.
      *
      * @param name the name of the putative plugin class
      */
     public boolean isClassPlugin(String name)
             throws ClassNotFoundException
     {
         Class c = loadClass(name);
         Class p = AbstractPlugin.class;
         return p.isAssignableFrom(c);
     }
 
     /**
      * Converts filenames to corresponding classnames.<br>
      * Example:<br>
      * Input >> csplugins/nodecount/NodeCountPlugin.class<BR>
      * Output >> csplugins.nodecount.NodeCountPlugin
      * @param pathAndFilename
      * @return the classname with package specifiers
      */
     public String PathAndFilenameToClassname(String pathAndFilename) {
 
         String withoutExtension = pathAndFilename.replaceAll("\\.class$","");
         String[] tokens = withoutExtension.split( "/" );
         String classname = tokens[0];
         for ( int i = 1; i < tokens.length; ++i ) {
              classname = classname.concat( "."+tokens[i] );
          }
          return classname;
     }
}
