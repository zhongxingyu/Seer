 /*
  * Util.java
  *
  * Created on July 10, 2007, 12:27 PM
  *
  * To change this template, choose Tools | Template Manager
  * and open the template in the editor.
  */
 package org.netbeans.test.javafx.editor.lib;
 
 import java.awt.Container;
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileReader;
 import java.io.IOException;
 import java.io.PrintWriter;
 import org.netbeans.jellytools.Bundle;
 import org.netbeans.jellytools.MainWindowOperator;
import org.netbeans.jellytools.NewJavaProjectNameLocationStepOperator;
 import org.netbeans.jellytools.NewProjectWizardOperator;
 import org.netbeans.jellytools.OutputOperator;
 import org.netbeans.jellytools.ProjectsTabOperator;
 import org.netbeans.jellytools.TopComponentOperator;
 import org.netbeans.jellytools.actions.ActionNoBlock;
 import org.netbeans.jellytools.actions.OpenAction;
 import org.netbeans.jellytools.actions.SaveAllAction;
 import org.netbeans.jellytools.nodes.Node;
 import org.netbeans.jemmy.QueueTool;
 import org.netbeans.jemmy.TimeoutExpiredException;
 import org.netbeans.jemmy.operators.JEditorPaneOperator;
 import org.netbeans.jemmy.operators.JMenuItemOperator;
 import org.netbeans.jemmy.operators.JPopupMenuOperator;
 import org.netbeans.jemmy.operators.JProgressBarOperator;
 import org.netbeans.jemmy.util.Dumper;
 import org.netbeans.jemmy.util.PNGEncoder;
 import org.openide.filesystems.FileObject;
 import org.openide.filesystems.FileUtil;
 
 /**
  *
  * @author Lark Fitzgerald
  */
 public class Util {
 
     protected static final String _close = "Close";
     protected static final String _compile = "Compile";
     private static final int WAIT_TIME = 2000;
     public static final long MAX_WAIT_TIME = 300000;
     public static String PATH_SEPARATOR = "|";
     public static String FILE_SEPARATOR = "/";
     public static String _bundle = "org.netbeans.test.javafx.editor.lib.Bundle";
     public static String _category = Bundle.getStringTrimmed(_bundle, "javaFX");
     public static String _project = Bundle.getStringTrimmed(_bundle, "javaFXProject");
     public static String _source = Bundle.getStringTrimmed(_bundle, "sourcePackages");
     public static String _main = Bundle.getStringTrimmed(_bundle, "mainFX");
     public static String _placeCodeHere = Bundle.getStringTrimmed(_bundle, "placeCodeHere");
     public static String _logFileLocation = Bundle.getStringTrimmed(_bundle, "logFile");
 //    public static String LOGFILE = WORKDIR + FILE_SEPARATOR + _logFileLocation;
 
     public static FileObject getTestFile(File dataDir, String projectName, String testFile) throws IOException, InterruptedException {
         File projectFile = new File(dataDir, projectName);
         FileObject project = FileUtil.toFileObject(projectFile);
         FileObject test = project.getFileObject("src/" + testFile);
 
         if (test == null)
             throw new IllegalStateException("File not found: src/" + testFile + " in project " + projectName);
 
         return test;
     }
 
     /** Creates a JavaFX project */
     public static Boolean createProject(String name, String location) {
         NewProjectWizardOperator projectWizard = NewProjectWizardOperator.invoke();
         projectWizard.selectCategory(_category);
         projectWizard.selectProject(_project);
         projectWizard.next();
        NewJavaProjectNameLocationStepOperator locationWizard = new NewJavaProjectNameLocationStepOperator();
         locationWizard.txtProjectName().setText(name);
         locationWizard.txtProjectLocation().setText(location);
         projectWizard.finish();
         waitScanFinished();
         new QueueTool().waitEmpty();
 
         //Verifies that project exists
         try {
             ProjectsTabOperator pto = new ProjectsTabOperator();
             new Node(pto.invoke().tree(), name);
             new QueueTool().waitEmpty();
         } catch (Exception e) {
             return false;
         }
         return true;
     }
 
 
     /** Compile single file using treePath */
     public static Boolean compileProjectFile(String path) {
         ProjectsTabOperator pto = new ProjectsTabOperator();
         Node projectNode = new Node(pto.invoke().tree(), path);
         new QueueTool().waitEmpty();
         sleep();
         JPopupMenuOperator item = projectNode.callPopup();
         item.pushMenuNoBlock(_compile);
         new QueueTool().waitEmpty();
         sleep();
 
         //Verify compilation
         try {
             OutputOperator oo = new OutputOperator();
             new QueueTool().waitEmpty();
             String output = oo.getText();
             CharSequence sucess = new String("BUILD SUCCESS");
             CharSequence warning = new String("warnings");
             if ((!output.contains(sucess)) || (output.contains(warning))) {
                 return false;
             }
             return true;
         } catch (org.netbeans.jemmy.TimeoutExpiredException e) {
             //open it and try again
             new ActionNoBlock("Window|Output|Output", null).perform();
             new QueueTool().waitEmpty();
             try {
                 OutputOperator oo = new OutputOperator();
                 new QueueTool().waitEmpty();
                 String output = oo.getText();
                 CharSequence cs = new String("BUILD SUCCESS");
                 if (!output.contains(cs)) {
                     return false;
                 }
                 return true;
             } catch (org.netbeans.jemmy.TimeoutExpiredException e2) {
                 return false; //output window not found
             }
         }
     }
 
     public static void clearEditor() {
 
     }
 
     /** Inserts loaded code in the editor 'Place Code Here' comment. */
     public static JEditorPaneOperator placeCodeHere(String PROJECT_NAME, String SAMPLE, String DATADIR) {
         Node projectNode = new Node(ProjectsTabOperator.invoke().tree(), PROJECT_NAME);
         String mainPath = _source + Util.PATH_SEPARATOR +
                 PROJECT_NAME.toLowerCase() +
                 Util.PATH_SEPARATOR + _main;
         Node mainFileNode = new Node(projectNode, mainPath);
 
         new OpenAction().performPopup(mainFileNode);
         TopComponentOperator main = new TopComponentOperator(_main);
         JEditorPaneOperator textComponent = new JEditorPaneOperator(main);
 //        JTextComponentOperator textComponent = new JTextComponentOperator(main);
         textComponent.changeCaretPosition(textComponent.getPositionByText(_placeCodeHere));
 
         String text = getSourceCodeData(SAMPLE, DATADIR);
         if (text == null) { //fails to find source
 
             return null;
         }
         textComponent.selectText(_placeCodeHere);
         textComponent.replaceSelection(text);
         new QueueTool().waitEmpty();
 
 //        screenCapture(PROJECT_NAME + "screen.png");
 //        return textComponent.getText();
         return textComponent;
     }
 
     /** Inserts string typed char by char in the editor 'Place Code Here' comment and types tab to expand it. */
     public static JEditorPaneOperator typeTemplateAtPlaceCodeHere(String PROJECT_NAME, String SAMPLE, String text) {
         Node projectNode = new Node(ProjectsTabOperator.invoke().tree(), PROJECT_NAME);
         String mainPath = _source + Util.PATH_SEPARATOR +
                 PROJECT_NAME.toLowerCase() +
                 Util.PATH_SEPARATOR + _main;
         Node mainFileNode = new Node(projectNode, mainPath);
 
         new OpenAction().performPopup(mainFileNode);
         TopComponentOperator main = new TopComponentOperator(_main);
         JEditorPaneOperator textComponent = new JEditorPaneOperator(main);
         textComponent.changeCaretPosition(textComponent.getPositionByText(_placeCodeHere));
 
         if (text == null) { //fails to find source
             return null;
         }
         textComponent.selectText(_placeCodeHere);
 //        textComponent.replaceSelection(text);
         for (int i = 0; i < text.length(); i++) {
             textComponent.typeKey(text.charAt(i));
         }
         textComponent.typeKey('\t'); //Tab
 
         new QueueTool().waitEmpty();
         return textComponent;
     }
 
     /** Gets source from file and returns it as a String. */
     public static String getSourceCodeData(String example, String dataDir) {
         String examplePath = dataDir + FILE_SEPARATOR + example;
         System.out.println(">>> getSourceCodeData Path = " + examplePath);
         try {
             BufferedReader input = new BufferedReader(new FileReader(examplePath));
             String text = "";
             String line = null;
             while ((line = input.readLine()) != null) {
                 text += line + "\r\n"; // add carraige return and line feed
             }
             return text;
         } catch (Exception e) {
             System.out.println(">>> EXCEPTION: Unable to process " + examplePath + e.toString());
             System.out.println(e.toString());
             e.printStackTrace();
         }
         return null;
     }
 
 
     /** Popups are always children of TopNode */
     public static void clickPopup(String name) {
         JPopupMenuOperator popup = new JPopupMenuOperator(getTopNode());
         JMenuItemOperator menuItem = new JMenuItemOperator(popup, name);
         menuItem.pushNoBlock();
         new QueueTool().waitEmpty();
     }
 
 
     /** returns TopNode, aka Main IDE window */
     public static MainWindowOperator getTopNode() {
         return MainWindowOperator.getDefault();
     }
 
     public static String trimComments(String text) {
 
         String commentStart = "/*";
         String commentEnd = "*/";
         String temp = text;
         int start = temp.indexOf(commentStart);
         int end = temp.indexOf(commentEnd);
 
         System.out.println(">>> Trim: Start index = " + start);
         System.out.println(">>> Trim: End index = " + end);
 
         if (temp.startsWith(commentStart)) {
             temp = temp.substring(end + 2, temp.length());
 //            System.out.println(">>> Temp: " + temp);
             start =
                     temp.indexOf(commentStart);
             System.out.println(">>> Trim: Start index = " + start);
             end =
                     temp.indexOf(commentEnd);
             System.out.println(">>> Trim: End index = " + end);
         }
 
         String temp2 = "";
         while ((start >= 0) && (end > 0)) {
             temp2 = temp.substring(0, start);
             temp2 =
                     temp2 + temp.substring(end + 2);
             start =
                     temp2.indexOf(commentStart);
             System.out.println(">>> Trim: Start index = " + start);
             end =
                     temp2.indexOf(commentEnd);
             System.out.println(">>> Trim: End index = " + end);
         }
 
         System.out.println(temp2);
         return temp2;
     }
 
 
     public static boolean diff(String current, String golden) {
         int x = golden.compareTo(current);
         if (x == 0) {
             return true;
         } else {
             System.out.println(">>> diff at position " + x);
             return false;
         }
     /*
      *     public void ref(String message)
     public PrintStream getRef()
     public File getGoldenFile()
     public File getGoldenFile(String filename)
     public void compareReferenceFiles()
     public void compareReferenceFiles(String testFilename, String goldenFilename, String diffFilename)
      */
 
     }
 
     /** Performs Save and Close of project */
     public static Boolean closeProject(String name) {
         new SaveAllAction().performAPI();
         new QueueTool().waitEmpty();
         Node projectNode = new Node(ProjectsTabOperator.invoke().tree(), name);
         JPopupMenuOperator item = projectNode.callPopup();
         item.pushMenuNoBlock(_close);
         new QueueTool().waitEmpty();
         sleep();
 
         //Verify Project is not listed
         Boolean status = false;
         try {
             ProjectsTabOperator pto = new ProjectsTabOperator();
             new Node(pto.invoke().tree(), name);
         } catch (org.netbeans.jemmy.TimeoutExpiredException e) {
             status = true; //Should not find project
         }
         new QueueTool().waitEmpty();
         return status;
     }
 
     /**
      * Used to check the ide log file for exceptions.
      * This method was taken from the sanity test and modified to return a string.
      */
     /*
     public static String hasUnexpectedException() throws java.io.IOException {
         String[] knownException = {//"org.netbeans.modules.uihandler.exceptionreporter",
 //                        "NbBrowserException" //doesn't work
             //Machine specific
             "Exceptions: INFO [org.netbeans.modules.extbrowser]: Cannot detect Mozilla : org.netbeans.modules.extbrowser.NbBrowserException: RegOpenKeyEx() failed for SOFTWARE\\Microsoft\\Windows\\CurrentVersion\\App Paths\\MOZILLA.exe.",
             "INFO [org.netbeans.modules.extbrowser]: Cannot detect Netscape 7 : org.netbeans.modules.extbrowser.NbBrowserException: RegOpenKeyEx() failed for SOFTWARE\\Microsoft\\Windows\\CurrentVersion\\App Paths\\NETSCP.exe.",
             "INFO [org.netbeans.modules.extbrowser]: Cannot detect Netscape 6 : org.netbeans.modules.extbrowser.NbBrowserException: RegOpenKeyEx() failed for SOFTWARE\\Microsoft\\Windows\\CurrentVersion\\App Paths\\NETSCP6.exe.",
             "INFO [org.netbeans.modules.extbrowser]: Cannot detect Netscape 4 : org.netbeans.modules.extbrowser.NbBrowserException: RegOpenKeyEx() failed for SOFTWARE\\Microsoft\\Windows\\CurrentVersion\\App Paths\\NETSCAPE.exe."
         };
         String lineSep = System.getProperty("line.separator");
         StringBuffer sb = new StringBuffer();
         String nextLine = "";
         boolean isUnexpectedException;
 
         //Get lines that has word "Exceptions" from message.log
         System.out.println("Getting " + LOGFILE);
         String exceptions = parseLogs(LOGFILE, "Exception");
         if (exceptions.equals("")) {
             System.out.println(">>> No Exceptions found in messages.log");
             return ""; //No exceptions found
 
         } else {
             // Compile the pattern
             System.out.println(">>> Exception(s) has been found in messages.log");
             String patternStr = "^(.*)$";
             Pattern pattern = Pattern.compile(patternStr, Pattern.MULTILINE);
             Matcher matcher = pattern.matcher(exceptions);
 
             // Read exceptions line by line to determine if it is unexpected
             while (matcher.find()) {
                 isUnexpectedException = true;
                 nextLine = matcher.group(1);
                 for (int i = 0; i < knownException.length; i++) {
                     System.out.println(">>> Exception Received: " + nextLine);
 //                    System.out.println(" >>> Know Exception[" + i + "] = " + knownException[i]);
                     if ( (nextLine.indexOf(knownException[i]) != -1)) { // || (nextLine.startsWith("INFO")) ) {
                         isUnexpectedException = false;
                     }
                 }
                 if (isUnexpectedException) {
                     sb.append(nextLine);
                     sb.append(lineSep);
                 }
             }
             if (!sb.toString().equals("")) {
                 return "Unexpected exceptions: \n" + sb.toString();
             } else {
                 return "";
             }
         }
     }
 */
     /** Creates a screen capture of name in the workdir */
     public static void screenCapture(String name, String WORKDIR, String FILE_SEPARATOR) { //screen.png
         String loc = WORKDIR + FILE_SEPARATOR + name;
         PNGEncoder.captureScreen(loc);
     }
 
     /** Creates a screen dump of name in the workdir */
     public static void screenDump(String name, String WORKDIR) { //screen.xml
         String loc = WORKDIR + FILE_SEPARATOR + name;
         try {
             Dumper.dumpAll(loc);
         } catch (Exception e) {
         }
     }
 
     public static void waitScanFinished() {
         try {
             Thread.sleep(3000);
         } catch (Exception e) {
         }
 
         long waitTime = 50;
         long waitCount = MAX_WAIT_TIME / waitTime;
 
         for (long time = 0; time < waitCount; time++) {
             try {
                 Thread.sleep(waitTime);
             } catch (Exception e) {
             }
 
             Object scanning = JProgressBarOperator.findJProgressBar((Container) MainWindowOperator.getDefault().getSource());
             if (scanning == null) {
                 return;
             }
         }
         throw new TimeoutExpiredException("Scaning isn't finished in " + MAX_WAIT_TIME + " ms");
     }
 // =================== Utility Operations  ===================
     public static void sleep() {
         sleep(WAIT_TIME);
     }
 
     public static void sleep(int ms) {
         try {
             Thread.sleep(ms);
         } catch (InterruptedException ex) {
         }
     }
 }
