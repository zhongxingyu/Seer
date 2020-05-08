 package org.amanzi.scripting.jruby;
 
 import java.awt.Font;
 import java.awt.GraphicsEnvironment;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.List;
 
 import org.eclipse.core.runtime.FileLocator;
 import org.eclipse.core.runtime.Platform;
 
 /**
  * This utility class has some static methods for investigating the environment
  * and finding appropriate settings for JRuby. Originally this class started
  * as shared code between the pure-swing and the swt-swing versions of the IRBConsole
  * but it has expanded to include directory searching for ideal locations for JRuby.
  * @author craig
  */
 public class ScriptUtils {
 	private static ScriptUtils instance = new ScriptUtils();
 	private String jRubyHome = null;
 	private String jRubyVersion = null;
 
 	/**
 	 * Utility to setup fonts for the swing console. This was taken directly from the
 	 * relevant code in org.jruby.demo.IRBConsole.
 	 * @param otherwise
 	 * @param style
 	 * @param size
 	 * @param families
 	 * @return
 	 */
     public static Font findFont(String otherwise, int style, int size, String[] families) {
         String[] fonts = GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();
         Arrays.sort(fonts);
         Font font = null;
         for (int i = 0; i < families.length; i++) {
             if (Arrays.binarySearch(fonts, families[i]) >= 0) {
                 font = new Font(families[i], style, size);
                 break;
             }
         }
         if (font == null)
             font = new Font(otherwise, style, size);
         return font;
     }
 
     /**
      * Utility to find the best working location of JRuby. Set -Djruby.home
      * to add an explicit location to the beginning of the search path.
      * @return String path to JRuby installation (eg. "/usr/lib/jruby")
      */
     public static String getJRubyHome(){
     	return instance.ensureJRubyHome();
     }
 
     /**
      * Utility to find the version of ruby supported by the current JRuby location.
      * Set -Djruby.version to override this result.
      * @return String describing supported ruby version (eg. "1.8")
      */
     public static String getJRubyVersion(){
     	return instance.ensureJRubyVersion();
     }
 
 	/**
 	 * Utility method to add default ruby variable names for specified java class names.
 	 * This is used in some code to check dynamically the type of the class.
 	 * @param globals
 	 * @param classNames
 	 */
     public static void makeGlobalsFromClassNames(HashMap<String, Object> globals, String[] classNames) {
 		for(String className:classNames){
 			try {
 				String[] fds = className.split("\\.");
 				//TODO: Rather have a proper Camel->Underscore conversion here (see if JRuby code has one we can use)
 				String var = fds[fds.length-1].toLowerCase().replace("reader", "_reader").concat("_class");
 				globals.put(var, Class.forName(className));			
 			}
 			catch (ClassNotFoundException e) {
				System.out.println("Error setting global Ruby variable for class '"+className+"': "+e.getMessage());
				//e.printStackTrace(System.out);
 			}
 		}
 	}
 
     /**
      * Build a list of String paths to be added to the JRuby search paths
      * for finding ruby libraries. We use this when starting the interpreter
      * to ensure that any custom code we've written is found.
      * @param extras
      * @return
      */
     public static List<String> makeLoadPath(String[] extras) {
 		return instance.doMakeLoadPath(extras);
 	}
 
     /** Actually construct the list of load paths */ 
     private List<String> doMakeLoadPath(String[] extras) {
 		// The following code finds the location of jruby on the computer and
         // makes sure the right loadpath is provided to the interpreter
         // The paths can be overridden by -Djruby.home and -Djruby.version
 		ensureJRubyHome();
 		ensureJRubyVersion();
         List<String> loadPath = new ArrayList<String>();
         if(extras!=null) for(String path:extras) loadPath.add(path);
         loadPath.add(jRubyHome+"/lib/ruby/site_ruby/"+jRubyVersion);
         loadPath.add(jRubyHome+"/lib/ruby/site_ruby");
         loadPath.add(jRubyHome+"/lib/ruby/"+jRubyVersion);
         loadPath.add(jRubyHome+"/lib/ruby/"+jRubyVersion+"/java");
         loadPath.add(jRubyHome+"/lib");
         loadPath.add(jRubyHome + "/lib/ruby/gems/" + jRubyVersion + "/gems/neo4j-0.2.1/lib");
         loadPath.add(jRubyHome + "/lib/ruby/gems/" + jRubyVersion + "/gems/neo4j-0.2.1/lib/relations");
         loadPath.add(jRubyHome + "/lib/ruby/gems/" + jRubyVersion + "/gems/neo4j-0.2.1/lib/mixins");
         loadPath.add(jRubyHome + "/lib/ruby/gems/" + jRubyVersion + "/gems/neo4j-0.2.1/lib/jars");
         loadPath.add(jRubyHome + "/lib/ruby/gems/" + jRubyVersion + "/gems/neo4j-0.2.1/examples/imdb");
 
         loadPath.add("lib/ruby/"+jRubyVersion);
         loadPath.add(".");
 		return loadPath;
 	}
 
 	/** return JRubyHome, searching for it if necessary */
 	private String ensureJRubyHome() {
 		if(jRubyHome==null){
 			try {
 				jRubyHome = ScriptUtils.findJRubyHome(System.getProperty("jruby.home"));
 			}
 			catch (IOException e) {
 				//TODO: handle this exception
 				e.printStackTrace();				
 			}
 		}
 		return(jRubyHome);
 	}
 
 	/** return JRubyVersion, searching for it if necessary */
 	private String ensureJRubyVersion() {
 		if(jRubyVersion==null){
 			jRubyVersion = ScriptUtils.findJRubyVersion(ensureJRubyHome(),System.getProperty("jruby.version"));
 		}
 		return(jRubyVersion);
 	}
 
 	/** search for jruby home, starting with passed value, if any */
 	private static String findJRubyHome(String suggested) throws IOException {
 		String jRubyHome = null;
 		String userDir = System.getProperty("user.home");	
 		//Lagutko, 22.06.2009, since now we search ruby home only in org.jruby plugin		
 		for (String path : new String[] { FileLocator.resolve(Platform.getBundle("org.jruby").getEntry(".")).getFile() /*suggested,
 				Platform.getBundle("org.jruby").getLocation(),
 				".",
 				"C:/Program Files/JRuby",
 				"/usr/lib/jruby",
 				"/usr/local/lib/jruby",
 				userDir+"/.jruby",
                 userDir+"/dev/jruby-1.5.0",
                 userDir+"/dev/jruby-1.4.0",
                 userDir+"/dev/jruby-1.3.0",
                 userDir+"/dev/jruby-1.2.0",
                 userDir+"/dev/jruby-1.1.6",
                 userDir+"/dev/jruby-1.1.5",
 				userDir+"/dev/jruby-1.1.4",
 				userDir+"/dev/jruby-1.1.3",
 				userDir+"/dev/jruby-1.1.2",
 				userDir+"/dev/jruby-1.1.1",
 				userDir+"/dev/jruby-1.1",
 				userDir+"/dev/jruby-1.1RC1"*/
 		}) {
 			try {
 				//Lagutko, 22.06.2009, JRuby path contains source files but not a JAR
 				/*if ((new java.io.File(path+"/lib")).isDirectory() && (new java.io.File(path+"/lib/jruby.jar")).exists()) {
 					jRubyHome = path;
 					break;
 				}*/
 				jRubyHome = path;
 			} catch (Exception e) {
 				System.err
 						.println("Failed to process possible JRuby path '"
 								+ path + "': " + e.getMessage());
 			}
 		}
 		if (jRubyHome == null) {
 			jRubyHome = ".";
 		}
 		return jRubyHome;
 	}
 
 	/** try determine ruby version jruby.version property was not set. Default to "1.8" */
 	private static String findJRubyVersion(String jRubyHome, String jRubyVersion) {
 		if (jRubyVersion == null) {
 			for (String version : new String[] { "1.8", "1.9", "2.0", "2.1" }) {
 				String path = jRubyHome + "/lib/ruby/" + version;
 				try {
 					if ((new java.io.File(path)).isDirectory()) {
 						jRubyVersion = version;
 						break;
 					}
 				} catch (Exception e) {
 					System.err
 							.println("Failed to process possible JRuby path '"
 									+ path + "': " + e.getMessage());
 				}
 			}
 		}
 		if (jRubyVersion == null) {
 			jRubyVersion = "1.8";
 		}
 		return jRubyVersion;
 	}
 
 }
