 package com.compomics.relims.modes.gui;
 
 import com.compomics.relims.manager.progressmanager.Checkpoint;
 import com.compomics.relims.manager.progressmanager.ProgressManager;
 import com.compomics.util.gui.UtilitiesGUIDefaults;
 import java.io.*;
 import javax.swing.JOptionPane;
 
 /**
  * A wrapper class used to start the jar file with parameters. The parameters
  * are read from the JavaOptions file in the resources/conf folder.
  *
  * @author Harald Barsnes
  */
 public class RelimsWrapper {
 
     /**
      * If set to true debug output will be written to the screen.
      */
     private boolean debug = false;
     /**
      * The name of the jar file. Must be equal to the name given in the pom
      * file.
      */
     private String jarFileName = "compomics-relims-";
     private FileWriter f;
     private InputStreamReader isr;
 
     /**
      * Starts the launcher by calling the launch method. Use this as the main
      * class in the jar file.
      */
     public RelimsWrapper() {
 
         // get the version number set in the pom file
         jarFileName = jarFileName + getVersion() + ".jar";
 
         try {
             launch();
         } catch (Exception e) {
             e.printStackTrace();
            ProgressManager.setState(Checkpoint.FAILED, e);;
            Thread.currentThread().interrupt();
         }
     }
 
     /**
      * Launches the jar file with parameters to the jvm.
      *
      * @throws java.lang.Exception
      */
     private void launch() throws Exception {
 
         String path = this.getClass().getResource("RelimsWrapper.class").getPath();
         path = path.substring(5, path.indexOf(jarFileName));
         path = path.replace("%20", " ");
         path = path.replace("%5b", "[");
         path = path.replace("%5d", "]");
 
         File javaOptions = new File(path + "resources/conf/JavaOptions.txt");
 
         String options = "", currentOption;
 
         if (javaOptions.exists()) {
 
             try {
                 FileReader f = new FileReader(javaOptions);
                 BufferedReader b = new BufferedReader(f);
 
                 currentOption = b.readLine();
 
                 while (currentOption != null) {
                     if (!currentOption.startsWith("#")) {
                         options += currentOption + " ";
                     }
                     currentOption = b.readLine();
                 }
 
                 b.close();
                 f.close();
 
             } catch (FileNotFoundException ex) {
                 ex.printStackTrace();
                 ProgressManager.setState(Checkpoint.FAILED);
                 Thread.currentThread().interrupt();
             } catch (IOException ex) {
                 ex.printStackTrace();
                 ProgressManager.setState(Checkpoint.FAILED);
                 Thread.currentThread().interrupt();
             }
         } else {
             options = "-Xms128M -Xmx768M";
         }
 
         File tempFile = new File(path);
 
         String javaHome = System.getProperty("java.home") + File.separator + "bin" + File.separator;
 
         String quote = "";
 
         if (System.getProperty("os.name").lastIndexOf("Windows") != -1) {
             quote = "\"";
         }
 
         if (debug) {
             JOptionPane.showMessageDialog(null, "original java.home: " + javaHome);
         }
 
         // try to force the use of 64 bit Java if available
         if (javaHome.lastIndexOf(" (x86)") != -1) {
 
             // Java 32 bit home looks like this:    C:\Program Files (x86)\Java\jre6\bin\javaw.exe
             // Java 64 bit home looks like this:    C:\Program Files\Java\jre6\bin\javaw.exe
 
             String tempJavaHome = javaHome.replaceAll(" \\(x86\\)", "");
 
             if (debug) {
                 JOptionPane.showMessageDialog(null, "temp java.home: " + tempJavaHome);
             }
 
             if (new File(tempJavaHome).exists()) {
                 javaHome = tempJavaHome;
             }
         }
 
         if (debug) {
             JOptionPane.showMessageDialog(null, "new java.home: " + javaHome);
         }
 
         // get the splash 
         String splashPath = path + "resources/conf/relims-splash.png";
 
         // set the correct slashes for the splash path
         if (System.getProperty("os.name").lastIndexOf("Windows") != -1) {
             splashPath = splashPath.replace("/", "\\");
 
             // remove the initial '\' at the start of the line 
             if (splashPath.startsWith("\\") && !splashPath.startsWith("\\\\")) {
                 splashPath = splashPath.substring(1);
             }
         }
 
         String cmdLine = javaHome + "java -splash:" + quote + splashPath + quote + " " + options + " -cp "
                 + quote + new File(tempFile, jarFileName).getAbsolutePath() + quote
                 + " com.compomics.relims.gui.RelimsNBGUI";
 
         if (debug) {
             System.out.println(cmdLine);
         }
 
         String temp = "";
 
         try {
             Process p = Runtime.getRuntime().exec(cmdLine);
 
             InputStream stderr = p.getErrorStream();
             isr = new InputStreamReader(stderr);
             BufferedReader br = new BufferedReader(isr);
 
             temp = "<ERROR>\n\n";
 
             if (debug) {
                 System.out.println("<ERROR>");
             }
 
             String line = br.readLine();
 
             boolean error = false;
 
             while (line != null) {
 
                 if (debug) {
                     System.out.println(line);
                 }
 
                 temp += line + "\n";
                 line = br.readLine();
                 error = true;
             }
 
             if (debug) {
                 System.out.println("</ERROR>");
             }
 
             temp += "\nThe command line executed:\n";
             temp += cmdLine + "\n";
             temp += "\n</ERROR>\n";
             int exitVal = p.waitFor();
 
             if (debug) {
                 System.out.println("Process exitValue: " + exitVal);
             }
 
             if (error) {
                 File logFile = new File("resources/conf", "Relims.log");
                 f = new FileWriter(logFile, true);
                 f.write("\n\n" + temp + "\n\n");
                 f.close();
 
                 UtilitiesGUIDefaults.setLookAndFeel();
 
                 javax.swing.JOptionPane.showMessageDialog(null,
                         "Failed to start Relims.\n\n"
                         + "Inspect the log file for details: resources/conf/Relims.log.\n\n"
                         + "Then go to Troubleshooting at http://compomics-relims.googlecode.com.",
                         "Relims - Startup Failed", JOptionPane.ERROR_MESSAGE);
 
                 System.exit(0);
             }
         } catch (Throwable t) {
 
             if (temp.lastIndexOf("NoClassDefFound") != -1) {
                 JOptionPane.showMessageDialog(null,
                         "Seems like you are trying to start Relims from within a zip file!",
                         "Relims - Startup Failed", JOptionPane.ERROR_MESSAGE);
             }
 
             t.printStackTrace();
             System.exit(0);
         } finally {
             if (isr != null) {
                 isr = null;
             }
             if (f != null) {
                 f = null;
             }
         }
     }
 
     /**
      * Starts the launcher by calling the launch method. Use this as the main
      * class in the jar file.
      *
      * @param args
      */
     public static void main(String[] args) {
         new RelimsWrapper();
     }
 
     /**
      * Retrieves the version number set in the pom file.
      *
      * @return the version number of Relims.
      */
     public String getVersion() {
 
         java.util.Properties p = new java.util.Properties();
 
         try {
             InputStream is = this.getClass().getClassLoader().getResourceAsStream("compomics-relims.properties");
             p.load(is);
         } catch (IOException e) {
             e.printStackTrace();
             ProgressManager.setState(Checkpoint.FAILED, e);;
             Thread.currentThread().interrupt();
         }
 
         return p.getProperty("compomics-relims.version");
     }
 }
