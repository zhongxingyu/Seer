 package org.sakaiproject.nakamura.app;
 
 import org.apache.sling.launchpad.app.Main;
 import org.apache.sling.launchpad.base.shared.SharedConstants;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.PrintStream;
import java.net.MalformedURLException;
 import java.net.URL;
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Map;
 
 public class NakamuraMain {
 
   // The name of the environment variable to consult to find out
   // about sling.home
   private static final String ENV_SLING_HOME = "SLING_HOME";
 
   public static void main(String[] args) throws IOException {
     checkLaunchDate(args);
     System.setSecurityManager(null);
     Main.main(args);
   }
 
   private static void checkLaunchDate(String[] args) throws IOException {
     // Find the last modified of this jar
	  try {
     String resource = NakamuraMain.class.getName().replace('.','/')+".class";
     URL u = NakamuraMain.class.getClassLoader().getResource(resource);
     String jarFilePath = u.getFile();
     jarFilePath = jarFilePath.substring(0,jarFilePath.length()-resource.length()-2);
     u = new URL(jarFilePath);
     jarFilePath = u.getFile();
     File jarFile = new File(jarFilePath);
     info("Loading from "+jarFile,null);
     long lastModified = jarFile.lastModified();
 
     Map<String, String> parsedArgs = parseCommandLine(args);
     // Find the last modified when the jar was loaded.
     String slingHome = getSlingHome(parsedArgs);
     File slingHomeFile = new File(slingHome);
     File loaderTimestamp = new File(slingHome, ".lauchpadLastModified");
     long launchpadLastModified = 0L;
     if (loaderTimestamp.exists()) {
       BufferedReader fr = null;
       try {
         fr = new BufferedReader(new FileReader(loaderTimestamp));
         launchpadLastModified = Long.parseLong(fr.readLine());
       } catch (NumberFormatException e) {
         e.printStackTrace();
       } catch (IOException e) {
         e.printStackTrace();
       } finally {
         if (fr != null) {
           try {
             fr.close();
           } catch (IOException e) {
             e.printStackTrace();
           }
         }
       }
     } else {
       info("No loader timestamp ",null);
     }
 
     // if the jar is newer, then delete the bootstrap servialization file that will
     // cause the contents of the jar to replace the contents on disk.
     
     if (launchpadLastModified < lastModified) {
       File bundleSer = new File(slingHomeFile, "felix/bundle0/bootstrapinstaller.ser");
       if (bundleSer.exists()) {
         info("Launcer Jar is newer than runtime image, removing bundle state, jar will reload ",null);
         bundleSer.delete();
       } else {
         info("No runtime, will use contents of launcher jar",null);
       }
       slingHomeFile.mkdirs();
       FileWriter fw = new FileWriter(loaderTimestamp);
       fw.write(String.valueOf(lastModified));
       fw.close();
       fw = null;
     } else {
       info("Runtime image, newer than launcher, using runtime image ",null);
     }
	  } catch (MalformedURLException e) {
		  info("Not launching from a jar ",null);
	  }
 
   }
 
   /**
    * Define the sling.home parameter implementing the algorithme defined on the wiki page
    * to find the setting according to this algorithm:
    * <ol>
    * <li>Command line option <code>-c</code></li>
    * <li>System property <code>sling.home</code></li>
    * <li>Environment variable <code>SLING_HOME</code></li>
    * <li>Default value <code>sling</code></li>
    * </ol>
    * 
    * @param args
    *          The command line arguments
    * @return The value to use for sling.home
    */
   private static String getSlingHome(Map<String, String> commandLine) {
     String source = null;
 
     String slingHome = commandLine.get("c");
     if (slingHome != null) {
 
       source = "command line";
 
     } else {
 
       slingHome = System.getProperty(SharedConstants.SLING_HOME);
       if (slingHome != null) {
 
         source = "system property sling.home";
 
       } else {
 
         slingHome = System.getenv(ENV_SLING_HOME);
         if (slingHome != null) {
 
           source = "environment variable SLING_HOME";
 
         } else {
 
           source = "default";
           slingHome = SharedConstants.SLING_HOME_DEFAULT;
 
         }
       }
     }
 
     info("Setting sling.home=" + slingHome + " (" + source + ")", null);
     return slingHome;
   }
 
   /**
    * Parses the command line arguments into a map of strings indexed by strings. This
    * method suppports single character option names only at the moment. Each pair of an
    * option name and its value is stored into the map. If a single dash '-' character is
    * encountered the rest of the command line are interpreted as option names and are
    * stored in the map unmodified as entries with the same key and value.
    * <table>
    * <tr>
    * <th>Command Line</th>
    * <th>Mapping</th>
    * </tr>
    * <tr>
    * <td>x</td>
    * <td>x -> x</td>
    * </tr>
    * <tr>
    * <td>-y z</td>
    * <td>y -> z</td>
    * </tr>
    * <tr>
    * <td>-yz</td>
    * <td>y -> z</td>
    * </tr>
    * <tr>
    * <td>-y -z</td>
    * <td>y -> y, z -> z</td>
    * </tr>
    * <tr>
    * <td>-y x - -z a</td>
    * <td>y -> x, -z -> -z, a -> a</td>
    * </tr>
    * </table>
    * 
    * @param args
    *          The command line to parse
    * 
    * @return The map of command line options and their values
    */
   static Map<String, String> parseCommandLine(String[] args) {
     Map<String, String> commandLine = new HashMap<String, String>();
     boolean readUnparsed = false;
     for (int argc = 0; args != null && argc < args.length; argc++) {
       String arg = args[argc];
 
       if (readUnparsed) {
         commandLine.put(arg, arg);
       } else if (arg.startsWith("-")) {
         if (arg.length() == 1) {
           readUnparsed = true;
         } else {
           String key = String.valueOf(arg.charAt(1));
           if (arg.length() > 2) {
             commandLine.put(key, arg.substring(2));
           } else {
             argc++;
             if (argc < args.length
                 && (args[argc].equals("-") || !args[argc].startsWith("-"))) {
               commandLine.put(key, args[argc]);
             } else {
               commandLine.put(key, key);
               argc--;
             }
           }
         }
       } else {
         commandLine.put(arg, arg);
       }
     }
     return commandLine;
   }
   
   // ---------- logging
 
   // emit an informational message to standard out
   static void info(String message, Throwable t) {
       log(System.out, "*INFO*", message, t);
   }
 
   // emit an error message to standard err
   static void error(String message, Throwable t) {
       log(System.err, "*ERROR*", message, t);
   }
 
   private static final DateFormat fmt = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss.SSS ");
 
   // helper method to format the message on the correct output channel
   // the throwable if not-null is also prefixed line by line with the prefix
   private static void log(PrintStream out, String prefix, String message,
           Throwable t) {
 
       final StringBuilder linePrefixBuilder = new StringBuilder();
       synchronized (fmt) {
           linePrefixBuilder.append(fmt.format(new Date()));
       }
       linePrefixBuilder.append(prefix);
       linePrefixBuilder.append(" [");
       linePrefixBuilder.append(Thread.currentThread().getName());
       linePrefixBuilder.append("] ");
       final String linePrefix = linePrefixBuilder.toString();
 
       out.print(linePrefix);
       out.println(message);
       if (t != null) {
           t.printStackTrace(new PrintStream(out) {
               @Override
               public void println(String x) {
                   synchronized (this) {
                       print(linePrefix);
                       super.println(x);
                       flush();
                   }
               }
           });
       }
   }
 
 }
