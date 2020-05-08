 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 /*
  * scrubberGUI.java
  *
  * Created on 29 Dec, 2009, 4:27:10 PM
  */
 package packages;
 
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javax.swing.*;
 import java.io.*;
 import java.util.Scanner;
 import java.util.Arrays;
 import java.awt.*;
 import java.beans.*;
 
 /**
  *
  * @author hari
  */
 public class scrubberGUI extends javax.swing.JFrame implements PropertyChangeListener {
 
     /** Creates new form scrubberGUI */
     public scrubberGUI() {
         initComponents();
     }
 
     class Task extends SwingWorker<Void, Void> {
         /*
          * Main task. Executed in background thread.
          */
 
         @Override
         public Void doInBackground() {
             setProgress(0);
 
             inputNumbers = new long[totalInputNumbers];
             dndNumbers = new long[totalDndNumbers];
             outputNumbers = new long[totalInputNumbers];
 
             // First get all the input into an array.
             statusLabel.setText("Loading data from files...");
             scrubProgressBar.setIndeterminate(false);
             Scanner inputFileScanner = null;
             Scanner dndFileScanner = null;
             try {
                 inputFileScanner = new Scanner(new BufferedReader(new FileReader(inputFile)));
                 dndFileScanner = new Scanner(new BufferedReader(new FileReader(dndFile)));
 
                 int oldProgressBarValue = 0;
                 int newProgressBarValue = 0;
                 while (inputFileScanner.hasNext()) {
                     inputNumbers[inputNumberCount] = Long.parseLong(inputFileScanner.next());
                     inputNumberCount++;
                     setProgress(Math.min(((inputNumberCount + dndNumberCount) * 100) / (totalInputNumbers + totalDndNumbers), 100));
                 }
                 hasCompletedInputImport = true;
                 while (dndFileScanner.hasNext()) {
                     dndNumbers[dndNumberCount] = Long.parseLong(dndFileScanner.next());
                     dndNumberCount++;
                     setProgress(Math.min(((inputNumberCount + dndNumberCount) * 100) / (totalInputNumbers + totalDndNumbers), 100));
                 }
                 hasCompletedDNDImport = true;
             } catch (FileNotFoundException ex) {
                 Logger.getLogger(scrubberGUI.class.getName()).log(Level.SEVERE, null, ex);
                 statusLabel.setText("Error encountered when reading from files!");
             } finally {
                 if (inputFileScanner != null) {
                     inputFileScanner.close();
                 }
                 if (dndFileScanner != null) {
                     dndFileScanner.close();
                 }
             }
             setProgress(0);
 
             // Then sort the data in the array.
             statusLabel.setText("Sorting data for binary search operation...");
             scrubProgressBar.setIndeterminate(true);
             Arrays.sort(inputNumbers);
             Arrays.sort(dndNumbers);
 
             // Then do the main processing.
             scrubProgressBar.setIndeterminate(false);
             setProgress(0);
             statusLabel.setText("Processing...");
             inputNumberCount = 0;
             while (inputNumberCount < totalInputNumbers) {
                 // Do, step by step.
                 if (0 > Arrays.binarySearch(dndNumbers, inputNumbers[inputNumberCount])) {
                     outputNumbers[outputNumberCount] = inputNumbers[inputNumberCount];
                     outputNumberCount++;
                 }
                 inputNumberCount++;
                 setProgress(Math.min(((inputNumberCount * 100) / totalInputNumbers), 100));
             }
             return null;
         }
 
         /*
          * Executed in event dispatching thread
          */
         @Override
         public void done() {
             done = true;
             if(!hasCompletedInputImport)
                 statusLabel.setText("Importing of Input Numbers crashed at " + inputNumberCount + "-th line.");
             else if(!hasCompletedDNDImport)
                 statusLabel.setText("Importing of DND Numbers crashed at " + dndNumberCount + "-th line.");
             else {
                 outputButton.setEnabled(true);
                 statusLabel.setText("Number of MSISDN-s in output: " + outputNumberCount);
             }
             setCursor(null); // Turn off the wait cursor
         }
     }
 
     /**
      * Invoked when task's progress property changes.
      */
     public void propertyChange(PropertyChangeEvent evt) {
         if ("progress" == evt.getPropertyName()) {
             int progress = (Integer) evt.getNewValue();
             scrubProgressBar.setValue(progress);
         }
     }
 
     /** This method is called from within the constructor to
      * initialize the form.
      * WARNING: Do NOT modify this code. The content of this method is
      * always regenerated by the Form Editor.
      */
     @SuppressWarnings("unchecked")
     // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
     private void initComponents() {
 
         inputLabel = new javax.swing.JLabel();
         inputButton = new javax.swing.JButton();
         dndLabel = new javax.swing.JLabel();
         dndButton = new javax.swing.JButton();
         outputLabel = new javax.swing.JLabel();
         outputButton = new javax.swing.JButton();
         scrubButton = new javax.swing.JButton();
         scrubLabel = new javax.swing.JLabel();
         scrubProgressBar = new javax.swing.JProgressBar();
         inputCheckBox = new javax.swing.JCheckBox();
         dndCheckBox = new javax.swing.JCheckBox();
         statusSeperator = new javax.swing.JSeparator();
         statusLabel = new javax.swing.JLabel();
 
         setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
         setTitle("Scrubber v2");
         setAlwaysOnTop(true);
 
         inputLabel.setText("Step 1. Choose an Input File:");
 
         inputButton.setText("Select Input File");
         inputButton.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 inputButtonActionPerformed(evt);
             }
         });
 
         dndLabel.setText("Step 2. Select a DND File:");
 
         dndButton.setText("Select DND File");
         dndButton.setEnabled(false);
         dndButton.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 dndButtonActionPerformed(evt);
             }
         });
 
         outputLabel.setText("Step 4. Save the Output File:");
 
         outputButton.setText("Save Output");
         outputButton.setEnabled(false);
         outputButton.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 outputButtonActionPerformed(evt);
             }
         });
 
         scrubButton.setText("Scrub!");
         scrubButton.setEnabled(false);
         scrubButton.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 scrubButtonActionPerformed(evt);
             }
         });
 
         scrubLabel.setText("Step 3. Intiate Scrubbing:");
 
         inputCheckBox.setEnabled(false);
 
         dndCheckBox.setEnabled(false);
 
         statusLabel.setText("Waiting for user to select files...");
 
         javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
         getContentPane().setLayout(layout);
         layout.setHorizontalGroup(
             layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(layout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addComponent(statusLabel)
                     .addComponent(statusSeperator, javax.swing.GroupLayout.DEFAULT_SIZE, 442, Short.MAX_VALUE)
                     .addComponent(scrubProgressBar, javax.swing.GroupLayout.DEFAULT_SIZE, 442, Short.MAX_VALUE)
                     .addGroup(layout.createSequentialGroup()
                         .addComponent(outputLabel)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addComponent(outputButton, javax.swing.GroupLayout.DEFAULT_SIZE, 246, Short.MAX_VALUE))
                     .addGroup(layout.createSequentialGroup()
                         .addComponent(inputLabel)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addComponent(inputButton, javax.swing.GroupLayout.DEFAULT_SIZE, 220, Short.MAX_VALUE)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addComponent(inputCheckBox))
                     .addGroup(layout.createSequentialGroup()
                         .addComponent(dndLabel)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addComponent(dndButton, javax.swing.GroupLayout.DEFAULT_SIZE, 220, Short.MAX_VALUE)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addComponent(dndCheckBox))
                     .addGroup(layout.createSequentialGroup()
                         .addComponent(scrubLabel)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addComponent(scrubButton, javax.swing.GroupLayout.DEFAULT_SIZE, 246, Short.MAX_VALUE)))
                 .addContainerGap())
         );
 
         layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {dndLabel, inputLabel, outputLabel, scrubLabel});
 
         layout.setVerticalGroup(
             layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(layout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                         .addComponent(inputButton)
                         .addComponent(inputLabel))
                     .addComponent(inputCheckBox))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                         .addComponent(dndLabel)
                         .addComponent(dndButton))
                     .addComponent(dndCheckBox))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(scrubLabel)
                     .addComponent(scrubButton))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                 .addComponent(scrubProgressBar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addGap(12, 12, 12)
                 .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(outputLabel)
                     .addComponent(outputButton))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(statusSeperator, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(statusLabel)
                 .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
         );
 
         pack();
     }// </editor-fold>//GEN-END:initComponents
 
     private void inputButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_inputButtonActionPerformed
         // Handle open button action.
         int returnVal = inputFileChooser.showOpenDialog(scrubberGUI.this);
 
         if (returnVal == JFileChooser.APPROVE_OPTION) {
             inputCheckBox.setSelected(true);
             dndButton.setEnabled(true);
             inputFile = inputFileChooser.getSelectedFile();
             try {
                 totalInputNumbers = countLines(inputFile);
             } catch (IOException ex) {
                 Logger.getLogger(scrubberGUI.class.getName()).log(Level.SEVERE, null, ex);
             }
            statusLabel.setText("Input file contains " + totalInputNumbers + " numbers. Waiting for DND file selection...");
         } else {
             statusLabel.setText("Input file selection cancelled... Waiting for input...");
         }
     }//GEN-LAST:event_inputButtonActionPerformed
 
     private void dndButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_dndButtonActionPerformed
         // Handle open button action.
         int returnVal = dndFileChooser.showOpenDialog(scrubberGUI.this);
 
         if (returnVal == JFileChooser.APPROVE_OPTION) {
             dndCheckBox.setSelected(true);
             scrubButton.setEnabled(true);
             dndFile = dndFileChooser.getSelectedFile();
             try {
                 totalDndNumbers = countLines(dndFile);
             } catch (IOException ex) {
                 Logger.getLogger(scrubberGUI.class.getName()).log(Level.SEVERE, null, ex);
             }
            statusLabel.setText("DND file contains " + totalDndNumbers + " numbers. Press 'Scrub!' to begin.");
         } else {
             statusLabel.setText("DND file selection cancelled... Waiting for input...");
         }
     }//GEN-LAST:event_dndButtonActionPerformed
 
     private void scrubButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_scrubButtonActionPerformed
 
         // Define basics for progressbar
         scrubProgressBar.setStringPainted(true);
         scrubProgressBar.setValue(0);
 
         // Reset the scrub button
         scrubButton.setEnabled(false);
         inputButton.setEnabled(false);
         dndButton.setEnabled(false);
         setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
 
         //Instances of javax.swing.SwingWorker are not reusuable, so
         //we create new instances as needed.
         task = new Task();
         task.addPropertyChangeListener(this);
         task.execute();
     }//GEN-LAST:event_scrubButtonActionPerformed
 
     private void outputButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_outputButtonActionPerformed
         int returnVal = outputFileChooser.showSaveDialog(scrubberGUI.this);
         BufferedWriter outputStream = null;
         if (returnVal == JFileChooser.APPROVE_OPTION) {
             statusLabel.setText("Saving to file...");
             File outputFile = outputFileChooser.getSelectedFile();
             try {
                 // Save output to selected file.
                 outputStream = new BufferedWriter(new FileWriter(outputFile));
                 for (int c = 0; c < outputNumberCount; c++) {
                     outputStream.write(outputNumbers[c] + "\r\n");
                 }
             } catch (IOException ex) {
                 Logger.getLogger(scrubberGUI.class.getName()).log(Level.SEVERE, null, ex);
             } finally {
                 try {
                     outputStream.close();
                 } catch (IOException ex) {
                     Logger.getLogger(scrubberGUI.class.getName()).log(Level.SEVERE, null, ex);
                 }
             }
             statusLabel.setText("Done! Restart this program to do more scrubbing! :-)");
         } else {
             statusLabel.setText("Save cancelled. Waiting for input...");
         }
     }//GEN-LAST:event_outputButtonActionPerformed
 
     public static int countLines(File someFile) throws IOException {
         int numberOfLines = 0;
         BufferedReader inputStream = null;
         try {
             inputStream = new BufferedReader(new FileReader(someFile));
             String l;
             while ((l = inputStream.readLine()) != null) {
                 numberOfLines++;
             }
         } finally {
             if (inputStream != null) {
                 inputStream.close();
             }
         }
         return (numberOfLines + 10);
     }
 
     /**
      * @param args the command line arguments
      */
     public static void main(String args[]) {
         java.awt.EventQueue.invokeLater(new Runnable() {
 
             public void run() {
                 new scrubberGUI().setVisible(true);
             }
         });
     }
     // Variables declaration - do not modify//GEN-BEGIN:variables
     private javax.swing.JButton dndButton;
     private javax.swing.JCheckBox dndCheckBox;
     private javax.swing.JLabel dndLabel;
     private javax.swing.JButton inputButton;
     private javax.swing.JCheckBox inputCheckBox;
     private javax.swing.JLabel inputLabel;
     private javax.swing.JButton outputButton;
     private javax.swing.JLabel outputLabel;
     private javax.swing.JButton scrubButton;
     private javax.swing.JLabel scrubLabel;
     private javax.swing.JProgressBar scrubProgressBar;
     private javax.swing.JLabel statusLabel;
     private javax.swing.JSeparator statusSeperator;
     // End of variables declaration//GEN-END:variables
     final JFileChooser inputFileChooser = new JFileChooser();
     final JFileChooser dndFileChooser = new JFileChooser();
     final JFileChooser outputFileChooser = new JFileChooser();
     int totalInputNumbers = 100000;
     int totalDndNumbers = 100000;
     int outputNumberCount = 0;
     File inputFile = null;
     File dndFile = null;
     long[] inputNumbers;
     long[] dndNumbers;
     long[] outputNumbers;
     private Task task;
     boolean done = false;
     boolean hasCompletedInputImport = false;
     boolean hasCompletedDNDImport = false;
     int inputNumberCount = 0;
     int dndNumberCount = 0;
 
 }
