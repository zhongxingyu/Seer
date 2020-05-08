 package net.jsdpu.process.executors;
 
 import static java.io.File.separator;
 import static java.lang.System.*;
 import static java.util.regex.Pattern.compile;
 import static net.jsdpu.logger.Logger.getLogger;
 import static net.jsdpu.process.executors.Commands.*;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.regex.Matcher;
 
 import net.jsdpu.logger.Logger;
 
 /**
  * Class used to replace multiple commands calls with one.
  * 
  * <p>
  * Used by some ProcessExecutors to execute multiple commands with the privilege
  * elevation.
  * </p>
  */
 public class MultiCaller {
     private static final Logger logger = getLogger(MultiCaller.class);
 
     private static String java;
     private static String path;
     private static String classPath;
 
     /**
      * Generates command that will allow to run multiple commands through
      * MultiCaller front end.
      * 
      * <p>
      * Used by some ProcessExecutors to run multiple commands with one request
      * for the privilege elevation.
      * </p>
      * 
      * @param commands
      *            commands that should be run by MultiCaller
      * @return list of commands for ProcessBulder/ProcessExecutor
      */
     static String[] prepareCommand(List<String[]> commands) {
         logger.trace("Preparation of MultiCaller run: " + commands);
         List<String> command = new ArrayList<String>();
         command.add(getJava());
         command.add("-cp");
         command.add(getClassPath());
         command.add(MultiCaller.class.getName());
         for (String[] subCommand : commands)
             command.add(wrapArgument(joinArguments(subCommand)));
         logger.detailedTrace("MultiCaller command: " + command);
         return command.toArray(new String[0]);
     }
 
     /**
      * Runs each argument as a command, treating it as if it were a console
      * line.
      * 
      * <p>
      * Results are redirected to output - both standard output and error output.
      * </p>
      * 
      * @param args
      *            commands to run
      */
     public static void main(String[] args) {
         try {
             for (String[] command : convertMultipleConsoleCommands(args)) {
                 try {
                     Process process = new ProcessBuilder(command).start();
 
                     BufferedReader in = new BufferedReader(new InputStreamReader(
                             process.getInputStream()));
                     BufferedReader error = new BufferedReader(new InputStreamReader(
                             process.getErrorStream()));
 
                     String line;
 
                     while ((line = in.readLine()) != null)
                         out.println(line);
                     while ((line = error.readLine()) != null)
                         err.println(line);
                 } catch (IOException e) {
                     e.printStackTrace();
                 }
             }
         } catch (InvalidCommandException e) {
             e.printStackTrace();
         }
     }
 
     /**
      * Obtains path to JVN.
      * 
      * @return path to JVM
      */
     private static String getJava() {
         if (java == null) {
             java = System.getProperty("java.home");
             if (java.endsWith("/") || java.endsWith("\\"))
                 java = java.substring(0, java.length() - 2);
             java += separator + "bin" + separator + "java";
         }
         return java;
     }
 
     /**
      * Obtains path to MultiCaller class in ClassPath.
      * 
      * @return path to MultiCaller
      */
     private static String getPath() {
        return (path != null) ? path : (path = MultiCaller.class.getResource(
                MultiCaller.class.getSimpleName() + ".class").toString());
     }
 
     /**
      * Finds out whether MultiCaller should be run as JAR or from byte code.
      * 
      * @return true if class is run from JAR, false otherwise
      */
     private static boolean runAsJar() {
         return getPath().startsWith("jar:");
     }
 
     /**
      * Returns ClassPath that will be needed to run main(String[]) method.
      * 
      * <p>
      * Automatically resolves whether MultiCaller should be run from JAR or
      * directly from byte code.
      * </p>
      * 
      * @return ClassPath
      */
     private static String getClassPath() {
         if (classPath == null) {
             if (runAsJar()) {
                System.out.println("classpath: " + getPath());
                 Matcher matcher = compile("jar:(file:/)?([^!]+)!.+").matcher(getPath());
                 if (!matcher.find())
                    throw new RuntimeException("Invalid class path");
                 classPath = matcher.group(2);
                System.out.println("new classpath: " + classPath);
            } else
                 classPath = getProperty("java.class.path", null);
         }
         return classPath;
     }
 }
