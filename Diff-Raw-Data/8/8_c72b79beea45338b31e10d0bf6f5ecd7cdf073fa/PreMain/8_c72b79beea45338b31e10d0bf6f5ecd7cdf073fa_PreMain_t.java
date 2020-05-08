 package main;
 
 import java.io.File;
 import java.io.IOException;
import java.lang.ProcessBuilder.Redirect;
 import java.util.LinkedList;
 import java.util.List;
 
 import lge.util.FileUtils;
 import lge.util.LogUtils;
 import lge.util.PathFinder;
 import lge.xmlevents.XMLEventsHelper;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 public final class PreMain {
   static {
     final File logsDir = new File("logs");
     if (!logsDir.exists() && !logsDir.mkdirs()) {
       System.err.println("Could not create logs dir " + logsDir.getAbsolutePath());
     }
     LogUtils.createDefaultLogConfigFileIfMissing();
     System.setProperty("java.util.logging.config.file", "config/logging.properties");
     
     FileUtils.copyFile(PreMain.class.getResourceAsStream("/lge/xmlevents/schema/event.xsd"), PathFinder.EVENT_XSD);
   }
   
   private static final Logger LOGGER = LoggerFactory.getLogger(PreMain.class);
   
   /**
    * @param args
    */
   public static void main(final String[] args) {
     XMLEventsHelper.buildAll();
     
     final List<String> newArgs = new LinkedList<>();
     newArgs.add(determineJavaExecutable());
     newArgs.add("-cp");
     newArgs.add("mod/events/bin" + determineSeparator() + determineNameOfOwnJar());
     newArgs.add(Main.class.getName());
     for (final String arg : args) {
       newArgs.add(arg);
     }
     String cmdString = "";
     for (final String newArg : newArgs) {
       cmdString += newArg + " ";
     }
     LOGGER.info("Trying to start main application with command: " + cmdString);
     try {
      final ProcessBuilder pb = new ProcessBuilder(newArgs);
      pb.redirectError(Redirect.INHERIT);
      pb.redirectOutput(Redirect.INHERIT);
      pb.redirectInput(Redirect.INHERIT);
      pb.start();
     } catch (final IOException e) {
       // TODO Auto-generated catch block
       e.printStackTrace();
     }
   }
   
   private static String determineNameOfOwnJar() {
     return new File(PreMain.class.getProtectionDomain().getCodeSource().getLocation().getPath()).getName();
   }
   
   private static String determineJavaExecutable() {
     final String javaHome = System.getProperty("java.home");
     if (javaHome == null) {
       LOGGER.error("System property \"java.home\" not set; unable to launch game automatically.");
       printUsageAndExit();
     }
     File f = new File(javaHome);
     if (!f.exists()) {
       LOGGER.error("System property \"java.home\" points to non-existing path \"{}\".", f.getAbsolutePath());
       printUsageAndExit();
     }
     f = new File(f, "bin");
     if (!f.exists()) {
       LOGGER.error("\"bin\" directory \"{}\" in system property \"java.home\" does not exist.", f.getAbsolutePath());
       printUsageAndExit();
     }
     File executable = new File(f, "javaw.exe");
     if (executable.exists()) {
       return executable.getAbsolutePath();
     }
     executable = new File(f, "java.exe");
     if (executable.exists()) {
       return executable.getAbsolutePath();
     }
     executable = new File(f, "java");
     if (executable.exists()) {
       return executable.getAbsolutePath();
     }
     
     LOGGER.error("Java executable not found in \"{}\".", f.getAbsolutePath());
     printUsageAndExit();
     return null;
   }
   
   private static void printUsageAndExit() {
     
     LOGGER.info("Try: <path_to_jdk_java_executable> -cp mod/events/bin" + determineSeparator()
         + "LuckySurvivor-XY.jar main.Main");
     System.exit(-1);
   }
   
   private static String determineSeparator() {
     String separator;
     if (System.getProperty("os.name").toLowerCase().indexOf("win") > -1) {
       separator = ";";
     } else {
       separator = ":";
     }
     return separator;
   }
 }
