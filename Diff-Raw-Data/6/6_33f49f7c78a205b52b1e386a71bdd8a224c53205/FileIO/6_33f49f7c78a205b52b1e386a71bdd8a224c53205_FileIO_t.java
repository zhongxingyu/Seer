 
 package edu.msoe.se2800.h4;
 
 import edu.msoe.se2800.h4.jplot.JPlotController;
 
 import java.io.*;
 
 import javax.swing.*;
 import javax.swing.filechooser.FileNameExtensionFilter;
 
 /**
  * Temporary class for saving and writing path files
  *
  * @author koenigj
  */
 public abstract class FileIO {
 
     /**
      * A file filter to filter out all files except .scrumbot
      */
     private static FileNameExtensionFilter filter;
 
     private static File mPathFile = null;
 
     /**
      * Opens a JFileChooser and returns the file to be opened.
      *
      * @return file, null if cancel is pressed
      * @author koenigj
      */
     public static File showOpenDialog() {
 
         JFrame directory = new JFrame();
         JFileChooser chooser = new JFileChooser();
         filter = new FileNameExtensionFilter("Robot files", "scrumbot");
         chooser.setFileFilter(filter);
         chooser.showOpenDialog(directory);
         return chooser.getSelectedFile();
 
     }
 
     public static File getCurrentPathFile() {
         return mPathFile;
     }
 
     /**
      * Returns the file to be saved.
      *
      * @return file, null if cancel is pressed
      * @throws FileNotFoundException
      */
     public static File showSaveDialog() {
         JFrame directory = new JFrame();
         JFileChooser chooser = new JFileChooser();
         filter = new FileNameExtensionFilter("Robot files", "scrumbot");
         chooser.setFileFilter(filter);
         chooser.showSaveDialog(directory);
         File file = chooser.getSelectedFile();
         if (file != null) {
             String text = file.getAbsolutePath();
             if (text.endsWith(".scrumbot")) {
                 file = new File(text + ".scrumbot");
             }
         }
         return file;
     }
 
     public static void saveAs(){
         File toSave = FileIO.showSaveDialog();
 
         if (toSave != null) {
             // Save the fact that we nowhave a file for future use
             if (!toSave.getPath().endsWith(".scrumbot")) {
                 toSave = new File(toSave.getPath() + ".scrumbot");
             }
            mPathFile = toSave;
            FileIO.save();
         }
 

     }
 
     /**
      * @return indicates where the path was saved to. If null, the showSaveDialog was cancelled. Since the File class
      *         is immutable, the argument cannot simply be changed.
      */
     public static void save() {
         // If performing showSaveDialog as... show the file chooser
         if (mPathFile == null) {
             FileIO.saveAs();
         } else {
             try {
                 JPlotController.getInstance().getPath()
                         .dumpObject(new DataOutputStream(new FileOutputStream(mPathFile)));
 
                 JOptionPane.showMessageDialog(null, "Path saved succesfully!");
             } catch (FileNotFoundException e1) {
                 // Already handled in the FileIO
             } catch (IOException e1) {
                 JOptionPane.showMessageDialog(null,
                         "An unknown error occurred while saving the Path.", "Uh-oh!",
                         JOptionPane.ERROR_MESSAGE);
             }
         }
 
     }
 
     /**
      * @return The File that was chosen to load
      */
     public static void load() {
 
         //if currently editing a file or the path on the grid is not empty
         if (mPathFile != null || !JPlotController.getInstance().getPath().isEmpty()) {
             int result = JOptionPane
                     .showConfirmDialog(null, "Do you wish to save your current Path?", "Save...?",
                             JOptionPane.YES_NO_OPTION);
             if (result == JOptionPane.YES_OPTION) {
                 FileIO.save();
             }
         }
 
         File tempPathFile = FileIO.showOpenDialog();
         if (tempPathFile != null) {
             try {
                 JPlotController.getInstance().getPath()
                         .loadObject(new DataInputStream(new FileInputStream(tempPathFile)));
             } catch (IOException e1) {
                 JOptionPane.showMessageDialog(null, "Unable to access the file.",
                         "Uh-oh!", JOptionPane.ERROR_MESSAGE);
             }
             JPlotController.getInstance().getGrid().redraw();
             mPathFile = tempPathFile;
         }
 
     }
 }
