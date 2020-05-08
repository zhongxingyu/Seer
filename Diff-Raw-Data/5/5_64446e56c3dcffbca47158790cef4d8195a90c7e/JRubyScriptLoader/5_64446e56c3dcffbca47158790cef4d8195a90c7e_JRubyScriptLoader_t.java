 package com.ning.phatamorgana.models;
 
 import java.io.File;
 import java.util.Map;
 
 import org.jruby.embed.LocalVariableBehavior;
 import org.jruby.embed.PathType;
 import org.jruby.embed.ScriptingContainer;
 
 /**
  * Loader for scripts written in JRuby.
  * @see http://jruby.org/
  */
 public class JRubyScriptLoader {
 
     /** Hooks into the application. */
     private Map<String, Object> context;
     
     /**
     * Creates the JRuby loader.
      * @param context hooks into the application
      */
     public JRubyScriptLoader(Map<String, Object> context) {
         this.context = context;
     }
     
     /**
     * Loads the JRuby scripts in the given path and its subdirectories.
      * @param directory the path to the scripts
      */
     public void loadScripts(File directory) {
         try {
             ScriptingContainer container = new ScriptingContainer(LocalVariableBehavior.PERSISTENT);
             container.put("$context", context);
             container.runScriptlet("require 'java'");
             container.runScriptlet("java_import 'com.ning.phatamorgana.models.UnitTest'");
             container.runScriptlet("java_import 'com.ning.phatamorgana.models.Documentation'");
             container.runScriptlet("java_import 'com.ning.phatamorgana.models.ChangeSet'");
             for (File f : directory.listFiles()) {
                 if (f.isDirectory()) {
                     loadScripts(f);
                 } else if (f.isFile() && f.getName().endsWith(".rb")) {
                     container.runScriptlet(PathType.ABSOLUTE, f.getAbsolutePath());
                 }
             }
         } catch (Exception e) {
             throw new RuntimeException(e);
         }
     }
 
 }
