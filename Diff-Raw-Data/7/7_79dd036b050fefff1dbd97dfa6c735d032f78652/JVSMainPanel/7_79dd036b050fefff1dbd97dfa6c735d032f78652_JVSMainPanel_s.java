 package org.javascool.gui;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.io.Console;
 import java.net.URL;
 import java.net.URLClassLoader;
 import java.util.HashMap;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javax.swing.JFileChooser;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.text.BadLocationException;
 import org.javascool.JVSFile;
 import org.javascool.JvsMain;
 import org.javascool.Utils;
 import org.javascool.tools.Macros;
 import org.javascool.tools.Proglet;
 import org.javascool.tools.ProgletManager;
 
 /** The main panel for Java's cool
  * This class wich is very static contain all that we need to run Java's cool like save and open file command.
  * This class can only be called by JvsMain on instance otherwise it can throw very big errors
  * @author Philippe Vienne
  */
 public final class JVSMainPanel extends JPanel {
 
     private static ProgletManager pgman = new ProgletManager();
     private static Proglet currentProglet;
     /** The java's cool top tool bar
      * @see JVSToolBar
      */
     private static JVSToolBar toolbar = new JVSToolBar();
     /** The java's cool split pane
      * @see JVSSplitPane
      */
     private static JVSSplitPane mainPane = new JVSSplitPane();
     /** This HashMap say if a file has to be saved */
     private static HashMap<String, Boolean> haveToSave = new HashMap<String, Boolean>();
     private static Boolean noFileEdited = true;
 
     /** This is the initializer command for the main panel
      * !! WARNING !! Call it only in JvsMain
      * @see JvsMain
      */
     public JVSMainPanel() {
         this.setVisible(true);
         this.setupViewLayout();
         this.setupToolBar();
         this.setupMainPanel();
     }
 
     /** Setup the Border Layout for the JPanel */
     private void setupViewLayout() {
         BorderLayout layout = new BorderLayout();
         this.setLayout(layout);
     }
 
     /** Setup the ToolBar */
     private void setupToolBar() {
         // Add Buttons here
         this.add(toolbar, BorderLayout.NORTH);
     }
 
     /** Setup Main Panel */
     private void setupMainPanel() {
         this.add(new JVSStartPanel());
     }
     
     public static void closeProglet(){
         if(JVSMainPanel.closeAllFiles()){
             JVSMainPanel.getThisInStatic().removeAll();
             JVSMainPanel.getThisInStatic().add(JVSMainPanel.toolbar);
             JVSMainPanel.getThisInStatic().add(new JVSStartPanel());
             JVSMainPanel.getThisInStatic().revalidate();
         } else {
             
         }
     }
 
     /** Get the toolbar
      * @return The toolbar
      */
     public static JVSToolBar getToolBar() {
         return toolbar;
     }
 
     /** Open a new file in the editor
      * @see JVSFileEditorTabs
      */
     public static void newFile() {
         String fileId = JVSMainPanel.getEditorTabs().openNewFile();
         JVSMainPanel.haveToSave.put(fileId, false);
     }
 
     /** Compile file in the editor
      * @see JVSFileEditorTabs
      */
     public static void compileFile() {
         org.javascool.tools.Console.stopProgram();
         JVSMainPanel.getEditorTabs().saveCurrentFile();
         JVSMainPanel.getEditorTabs().compileFile(JVSMainPanel.getEditorTabs().getCurrentFileId());
     }
 
     /** Open a file
      * Start a file chooser and open selected file
      * @see JFileChooser
      * @see JVSFileEditorTabs
      */
     public static void openFile() {
         final JFileChooser fc = new JFileChooser();
         int returnVal = fc.showOpenDialog(JvsMain.getJvsMainFrame());
         if (returnVal == JFileChooser.APPROVE_OPTION) {
             String path = fc.getSelectedFile().getAbsolutePath();
             if (noFileEdited) {
                 JVSMainPanel.closeFile();
                 noFileEdited = false;
             }
             String fileId = JVSMainPanel.getEditorTabs().open(path);
             JVSMainPanel.haveToSave.put(fileId, false);
         } else {
         }
     }
 
     /** Save the current file
      * @see JVSFileEditorTabs
      * @see JVSFile
      */
     public static void saveFile() {
         if (JVSMainPanel.getEditorTabs().saveCurrentFile()) {
             JVSMainPanel.haveToSave.put(JVSMainPanel.getEditorTabs().getCurrentFileId(), false);
         }
     }
 
     /** Close the current file
      * @see JVSFileEditorTabs
      */
     public static void closeFile() {
         if (JVSMainPanel.haveToSave.get(getEditorTabs().getCurrentFileId())) {
             if (saveFileIdBeforeClose(getEditorTabs().getCurrentFileId()) == 1) {
                 getEditorTabs().closeFile(getEditorTabs().getCurrentFileId());
             }
         } else {
             getEditorTabs().closeFile(getEditorTabs().getCurrentFileId());
         }
         if (JVSMainPanel.getEditorTabs().getOppenedFileCount() == 0) {
             JVSMainPanel.newFile();
         }
     }
 
     /** Update haveToSave for a file
      * Set it to true
      * @param fileId The file id
      */
     public static void mustSave(String fileId) {
         noFileEdited = false;
         JVSMainPanel.haveToSave.put(fileId, true);
     }
 
     /** Update haveToSave for a file
      * Set it to true
      * @param fileId The file id
      */
     public static void haveNotToSave(String fileId) {
         JVSMainPanel.haveToSave.put(fileId, false);
     }
 
     /** Show a compile error for an human
      * Open a dialog with compile error explains and hightlight the error line
      * @param line The line error
      * @param explication Human explain for that error
      * @see Jvs2Java
      * @see Console
      * @see DiagnosticCollector
      */
     public static void reportCompileError(int line, String explication) {
         org.javascool.tools.Console.clear();
         Macros.echo("-------------------\nErreur lors de la compilation à la ligne " + line + ".\n" + explication + "\n-------------------\n");
     }
 
     /** Handle the close application task
      * Check if all files are saved and if the user want to close the application
      * @return True meen that app can be close and false that app can NOT be closed
      */
     public static Boolean close() {
         String id = "";
         Boolean[] can_close = new Boolean[JVSMainPanel.haveToSave.keySet().toArray().length];
         int i = 0;
         int j = 0;
         for (Object fileId : JVSMainPanel.haveToSave.keySet().toArray()) {
             if (JVSMainPanel.haveToSave.get((String) fileId)) {
                 j++;
             }
         }
         // If user no have dialog to stop close, we create one
         if (j == 0) {
             final int n = JOptionPane.showConfirmDialog(
                     JvsMain.getJvsMainFrame(),
                     "Voulez vous vraiment quitter Java's cool ?",
                     "Confirmation",
                     JOptionPane.YES_NO_OPTION);
             if (n == JOptionPane.YES_OPTION) {
                 return true;
             } else {
                 return false;
             }
         }
         j = 0;
         // Check save for each file
         for (Object fileId : JVSMainPanel.haveToSave.keySet().toArray()) {
             id = (String) fileId;
             if (JVSMainPanel.haveToSave.get(id)) {
                 // File has to be saved
                 // For number see JVSMainPanel.saveFileIdBeforeClose() documentation about return
                 switch (JVSMainPanel.saveFileIdBeforeClose(id)) {
                     case 1:
                         can_close[i] = true;
                         break;
                     case 0:
                         can_close[i] = false;
                         break;
                     case -1:
                         return false;
                 }
                 j++;
             } else {
                 // If file has not to be saved it's good
                 can_close[i] = true;
             }
             if (can_close[i]) {
                 // If we can close this file, we close the tab
                 JVSMainPanel.getEditorTabs().closeFile(id);
             }
             i++;
         }
         // Check if a file is not save, if yes we can not close the application
         for (Boolean can_close_r : can_close) {
             if (can_close_r == false) {
 
                 return false;
             }
         }
 
         // We return true if all is good
         return true;
     }
 
     /** Handle the close file task
      * Check if all files are saved and if the user want to continue
      * @return True meen that app can be close and false that app can NOT be closed
      */
     public static Boolean closeAllFiles() {
         String id = "";
         Boolean[] can_close = new Boolean[JVSMainPanel.haveToSave.keySet().toArray().length];
         int i = 0;
         int j = 0;
         for (Object fileId : JVSMainPanel.haveToSave.keySet().toArray()) {
             if (JVSMainPanel.haveToSave.get((String) fileId)) {
                 j++;
             } else {
                 JVSMainPanel.getEditorTabs().closeFile((String) fileId);
             }
         }
         // If user no have dialog to stop close, we create one
         if (j == 0) {
             final int n = JOptionPane.showConfirmDialog(
                     JvsMain.getJvsMainFrame(),
                     "Voulez vous vraiment continuer ?",
                     "Confirmation",
                     JOptionPane.YES_NO_OPTION);
             if (n == JOptionPane.YES_OPTION) {
                 return true;
             } else {
                 return false;
             }
         }
         j = 0;
         // Check save for each file
         for (Object fileId : JVSMainPanel.haveToSave.keySet().toArray()) {
             id = (String) fileId;
             if (JVSMainPanel.haveToSave.get(id)) {
                 // File has to be saved
                 // For number see JVSMainPanel.saveFileIdBeforeClose() documentation about return
                 switch (JVSMainPanel.saveFileIdBeforeClose(id)) {
                     case 1:
                         can_close[i] = true;
                         break;
                     case 0:
                         can_close[i] = false;
                         break;
                     case -1:
                         return false;
                 }
                 j++;
             } else {
                 // If file has not to be saved it's good
                 can_close[i] = true;
             }
             if (can_close[i]) {
                 // If we can close this file, we close the tab
                 JVSMainPanel.getEditorTabs().closeFile(id);
             }
             i++;
         }
         // Check if a file is not save, if yes we can not close the application
         for (Boolean can_close_r : can_close) {
             if (can_close_r == false) {
 
                 return false;
             }
         }
 
         // We return true if all is good
         return true;
     }
 
     /** Ask to user to save a file before it close
      * @param fileId The file id
      * @return 1 meen that file is saved or that user not want to save the file. 0 meen that there was an error during the save of file. -1 meen that user want to stop all that happend (Cancel option).
      */
     private static int saveFileIdBeforeClose(String fileId) {
         JVSFile file = JVSMainPanel.getEditorTabs().getFile(fileId);
         int result = JOptionPane.showConfirmDialog(
                 JvsMain.getJvsMainFrame(),
                 "Voulez vous enregistrer " + file.getName() + " avant de continuer ?");
         if (result == JOptionPane.YES_OPTION) {
             if (JVSMainPanel.getEditorTabs().saveFile(fileId)) {
                 JVSMainPanel.haveToSave.put(fileId, false);
                 return 1;
             } else {
                 return 0;
             }
         } else if (result == JOptionPane.NO_OPTION) {
             return 1;
         } else {
             JVSMainPanel.haveToSave.put(fileId, true);
             return -1;
         }
     }
 
     public static Proglet getCurrentProglet() {
         return JVSMainPanel.currentProglet;
     }
 
     public static void loadProglet(String name) {
         try {
             JVSMainPanel.getThisInStatic().removeAll();
             JVSMainPanel.newFile();
             JVSMainPanel.getThisInStatic().add(JVSMainPanel.getToolBar(), BorderLayout.NORTH);
             JVSMainPanel.getThisInStatic().add(JVSMainPanel.getMainPane(), BorderLayout.CENTER);
             JVSMainPanel.getThisInStatic().revalidate();
             JVSMainPanel.getMainPane().revalidate();
             JVSMainPanel.getMainPane().setDividerLocation(JVSMainPanel.getThisInStatic().getWidth()/2);
             JVSMainPanel.getMainPane().revalidate();
             if (JVSMainPanel.pgman.getProglet(name) != null) {
                 ((JVSTabs) JVSMainPanel.getMainPane().getRightComponent()).removeAll();
                 ((JVSTabs) JVSMainPanel.getMainPane().getRightComponent()).add("Console", "", new org.javascool.tools.Console());
                 JVSMainPanel.currentProglet = JVSMainPanel.pgman.getProglet(name);
                 JVSMainPanel.getWidgetTabs().setProglet(currentProglet);
             } else {
                 Dialog.error("Impossible de continuer", "La proglet " + name + " ne peut pas être chargé car elle n'existe pas.");
             }
         } catch (Exception e) {
             System.err.println(e.getMessage());
         }
     }
     
    public static void reportRuntimeBug(String ex,Boolean stop){
         Dialog.error("Erreur du logiciel", ex);
        if(stop){
            org.javascool.tools.Console.stopProgram();
        }
     }
 
     public static ProgletManager getProgletManager() {
         return JVSMainPanel.pgman;
     }
 
     /** Get the current istance of Editor Tabs
      * @see JVSFileEditorTabs
      * @return The JVSFileEditorTabs instance
      */
     public static FileEditorTabs getEditorTabs() {
         return ((FileEditorTabs) JVSMainPanel.mainPane.getLeftComponent());
     }
 
     public static JVSWidgetPanel getWidgetTabs() {
         return (JVSWidgetPanel) JVSMainPanel.mainPane.getRightComponent();
     }
 
     public static JVSSplitPane getMainPane() {
         return JVSMainPanel.mainPane;
     }
 
     /** Get the JVSMainPanel
      * Used to access to non-static methods in static methods (Strange ? Yes)
      * @return The current instance of JvsMainPanel
      */
     public static JVSMainPanel getThisInStatic() {
         return JvsMain.getJvs();
     }
 
     public static class Dialog {
 
         /** Show a success dialog */
         public static void success(String title, String message) {
             JOptionPane.showMessageDialog(JvsMain.getJvsMainFrame(), message, title, JOptionPane.INFORMATION_MESSAGE);
         }
 
         /** Show an error dialog */
         public static void error(String title, String message) {
             JOptionPane.showMessageDialog(JvsMain.getJvsMainFrame(), message, title, JOptionPane.ERROR_MESSAGE);
         }
     }
 }
