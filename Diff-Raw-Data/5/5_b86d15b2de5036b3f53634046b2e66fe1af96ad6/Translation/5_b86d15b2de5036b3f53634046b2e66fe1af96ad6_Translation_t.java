 package de.aidger.utils;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.Enumeration;
 import java.util.List;
 import java.util.Locale;
 import java.util.PropertyResourceBundle;
 import java.util.Vector;
 import java.util.jar.JarEntry;
 import java.util.jar.JarFile;
 
 import de.aidger.model.Runtime;
 import de.unistuttgart.iste.se.adohive.util.tuple.Pair;
 
 /**
  * Sets the translation of the program. If Strings of the current language can't
  * be found, it will fall back to the default language.
  * 
  * @author aidGer Team
  */
 public class Translation {
 
     /**
      * The file name of the translation.
      */
     protected String filePath;
 
     /**
      * The bundle holding the translation.
      */
     protected static PropertyResourceBundle bundle = null;
 
     /**
      * Initializes this class. Resolves the file path of the translation files.
      * 
      * @param path
      *            The path of the aidGer settings.
      * @param language
      *            The language to which the program will be translated.
      */
     public Translation(String language) {
         /* Create path if necessary */
         filePath = Runtime.getInstance().getConfigPath() + "lang/";
 
         File languagePath = new File(filePath);
         if ((!languagePath.exists() || !languagePath.isDirectory())
                 && !languagePath.mkdirs()) {
            Logger.error("Couldn't create directory for translations.");
         }
 
         /* Load the language file */
         InputStream inputStream = null;
 
         /* Check first in .jar */
         String jarfile = Runtime.getInstance().getJarLocation();
         if (jarfile.endsWith(".jar")) {
             inputStream = getClass().getClassLoader().getResourceAsStream(
                 "de/aidger/lang/" + language + ".properties");
         }
 
         /* After that check in filesystem */
         if (inputStream == null) {
             try {
                 File inputFile = new File(filePath + language + ".properties");
                 inputStream = new FileInputStream(inputFile);
             } catch (Exception e) {
                 Logger
                    .info("Loading the translation from the filesystem failed. Only english will be available");
             }
         }
 
         /* Finally load the resource */
         if (inputStream != null) {
             try {
                 bundle = new PropertyResourceBundle(inputStream);
                 inputStream.close();
             } catch (IOException ex) {
             }
         }
     }
 
     /**
      * Returns the translation of the specified string from the bundle.
      * 
      * @param id
      *            The string to translate.
      * @return The translated string or return the id.
      */
     public static String _(String id) {
         try {
             return bundle.getString(id);
         } catch (Exception e) {
             /*
              * If the string is not translated in the bundle, or the bundle
              * cannot be found, just return the original string.
              */
             return id;
         }
     }
 
     /**
      * Get a list of all languages installed on the system. The format is 0 =>
      * short, 1 => long language name.
      * 
      * @return The list of all installed languages
      */
     public List<Pair<String, String>> getLanguages() {
         List<Pair<String, String>> list = new Vector<Pair<String, String>>();
 
         /* Add English as standard language */
         list.add(new Pair<String, String>("en", new Locale("en")
             .getDisplayLanguage()));
 
         /* Search all translations in the jar file */
         try {
             JarFile jf = new JarFile(Runtime.getInstance().getJarLocation());
             Enumeration ress = jf.entries();
 
             while (ress.hasMoreElements()) {
                 JarEntry je = (JarEntry) ress.nextElement();
 
                 if (je.getName().matches("de/aidger/lang/[a-z]+.properties")) {
                     int idx = je.getName().indexOf(".properties");
                     String lang = je.getName().substring(idx - 2, idx);
                     list.add(new Pair<String, String>(lang, new Locale(lang)
                         .getDisplayLanguage()));
                 }
             }
         } catch (IOException ex) {
         }
 
         /* Search all files in the lang directory and add them */
         File[] files = (new File(filePath)).listFiles();
         if (files != null) {
             for (File file : files) {
                 String filename = file.getName();
                 int idx = filename.indexOf(".properties");
                 if (idx > -1) {
                     String lang = filename.substring(0, idx);
                     list.add(new Pair<String, String>(lang, new Locale(lang)
                         .getDisplayLanguage()));
                 }
             }
         }
 
         return list;
     }
 }
