 package judge.client;
 
 import java.awt.Color;
 import java.io.*;
 import java.net.Socket;
 import java.net.URL;
 import javax.swing.DefaultComboBoxModel;
 import javax.swing.JButton;
 import javax.swing.JFileChooser;
 import javax.swing.JOptionPane;
 
 public class JudgeMain extends javax.swing.JApplet implements Codes {
 
     final static int MAX_LENGTH = 1000000;
     String[][] acceptableFileExtensions = new String[][]{{"java", "txt"}, {"py", "txt"}, {"cpp", "c", "txt"}};
     String[] origins;
     String[][] contests;
     String[][][] problems;
     JButton[] tests;
 
     @Override
     public void init() {
         try {
             for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                 if ("Nimbus".equals(info.getName())) {
                     javax.swing.UIManager.setLookAndFeel(info.getClassName());
                     break;
                 }
             }
         } catch (ClassNotFoundException ex) {
             java.util.logging.Logger.getLogger(JudgeMain.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
         } catch (InstantiationException ex) {
             java.util.logging.Logger.getLogger(JudgeMain.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
         } catch (IllegalAccessException ex) {
             java.util.logging.Logger.getLogger(JudgeMain.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
         } catch (javax.swing.UnsupportedLookAndFeelException ex) {
             java.util.logging.Logger.getLogger(JudgeMain.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
         }
         try {
             java.awt.EventQueue.invokeAndWait(new Runnable() {
 
                 public void run() {
                     initComponents();
                 }
             });
         } catch (Exception ex) {
             ex.printStackTrace();
         }
         //Create test result button index
         tests = new JButton[]{jButton1, jButton2, jButton3, jButton4, jButton5, jButton6, jButton7, jButton8, jButton9, jButton10, jButton11, jButton12, jButton13, jButton14, jButton15, jButton16};
         for (int i = 0; i < 16; i++) { //Set to default state
             tests[i].setBackground(Color.GRAY);
             tests[i].setText("-");
         }
         loadProblems();
     }
 
     private void loadProblems() { //Programming death penalty here
         try {
             //Read in problem database
             URL source = new URL("http://wacode.github.io/judge/problemdb.txt");
             InputStream read = source.openStream();
             ByteArrayOutputStream baos = new ByteArrayOutputStream();
             byte[] buffer = new byte[4096];
             int len;
             while ((len = read.read(buffer)) != -1) {
                 baos.write(buffer, 0, len);
             }
             String problemsList = baos.toString();
             String[] lines = problemsList.split("\n");
             //Count number of origins
             int originCount = 0;
             for (String line : lines) {
                 if (!line.startsWith("-")) {
                     originCount++;
                 }
             }
             origins = new String[originCount];
             //Read in origins
             int on = 0;
             for (String line : lines) {
                 if (!line.startsWith("-")) {
                     origins[on++] = line;
                 }
             }
             contests = new String[originCount][];
             //Count number of contests each
             int lineon = 1;
             for (int origin = 0; origin < originCount; origin++) {
                 int contestCount = 0;
                 while (lineon < lines.length && lines[lineon].startsWith("-")) {
                     if (!lines[lineon].startsWith("--")) {
                         contestCount++;
                     }
                     lineon++;
                 }
                 lineon++;
                 contests[origin] = new String[contestCount];
             }
             //Read in contests
             lineon = 1;
             for (int origin = 0; origin < originCount; origin++) {
                 int contestOn = 0;
                 while (lineon < lines.length && lines[lineon].startsWith("-")) {
                     if (!lines[lineon].startsWith("--")) {
                         contests[origin][contestOn++] = lines[lineon].substring(1);
                     }
                     lineon++;
                 }
                 lineon++;
             }
             problems = new String[originCount][][];
             for (int i = 0; i < originCount; i++) {
                 problems[i] = new String[contests[i].length][];
             }
             //Count number of problems each
             lineon = 2;
             for (int origin = 0; origin < originCount; origin++) {
                 for (int contest = 0; contest < contests[origin].length; contest++) {
                     int problemsCount = 0;
                     while (lineon < lines.length && lines[lineon].startsWith("--")) {
                         problemsCount++;
                         lineon++;
                     }
                     if (lineon < lines.length && lines[lineon].startsWith("-")) {
                         lineon++;
                     } else {
                         lineon += 2;
                     }
                     problems[origin][contest] = new String[problemsCount];
                 }
             }
             //Read in problems
             lineon = 2;
             for (int origin = 0; origin < originCount; origin++) {
                 for (int contest = 0; contest < contests[origin].length; contest++) {
                     int problemOn = 0;
                     while (lineon < lines.length && lines[lineon].startsWith("--")) {
                         problems[origin][contest][problemOn++] = lines[lineon++].substring(2);
                     }
                     if (lineon < lines.length && lines[lineon].startsWith("-")) {
                         lineon++;
                     } else {
                         lineon += 2;
                     }
                 }
             }
             //Apply defaults
             originSelect.setModel(new DefaultComboBoxModel(origins));
             contestSelect.setModel(new DefaultComboBoxModel(contests[0]));
             problemSelect.setModel(new DefaultComboBoxModel(problems[0][0]));
         } catch (Exception e) {
             e.printStackTrace();
             JOptionPane.showConfirmDialog(null, "The problem list could not be retrieved.", "Error", -1);
         }
     }
 
     @SuppressWarnings("unchecked")
     // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
     private void initComponents() {
 
         jLabel1 = new javax.swing.JLabel();
         fileName = new javax.swing.JTextField();
         browseButton = new javax.swing.JButton();
         jLabel2 = new javax.swing.JLabel();
         jLabel3 = new javax.swing.JLabel();
         jLabel4 = new javax.swing.JLabel();
         originSelect = new javax.swing.JComboBox();
         contestSelect = new javax.swing.JComboBox();
         problemSelect = new javax.swing.JComboBox();
         jScrollPane1 = new javax.swing.JScrollPane();
         outputArea = new javax.swing.JTextArea();
         submitButton = new javax.swing.JButton();
         jLabel5 = new javax.swing.JLabel();
         languageSelect = new javax.swing.JComboBox();
         jButton2 = new javax.swing.JButton();
         jButton1 = new javax.swing.JButton();
         jButton3 = new javax.swing.JButton();
         jButton4 = new javax.swing.JButton();
         jButton5 = new javax.swing.JButton();
         jButton6 = new javax.swing.JButton();
         jButton7 = new javax.swing.JButton();
         jButton8 = new javax.swing.JButton();
         jButton9 = new javax.swing.JButton();
         jButton10 = new javax.swing.JButton();
         jButton11 = new javax.swing.JButton();
         jButton12 = new javax.swing.JButton();
         jButton13 = new javax.swing.JButton();
         jButton14 = new javax.swing.JButton();
         jButton15 = new javax.swing.JButton();
         jButton16 = new javax.swing.JButton();
         jLabel6 = new javax.swing.JLabel();
 
         jLabel1.setText("File to Upload:");
 
         browseButton.setText("Browse");
         browseButton.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 browseButtonActionPerformed(evt);
             }
         });
 
         jLabel2.setText("Origin:");
 
         jLabel3.setText("Contest:");
 
         jLabel4.setText("Problem:");
 
         originSelect.addItemListener(new java.awt.event.ItemListener() {
             public void itemStateChanged(java.awt.event.ItemEvent evt) {
                 originSelectItemStateChanged(evt);
             }
         });
 
         contestSelect.addItemListener(new java.awt.event.ItemListener() {
             public void itemStateChanged(java.awt.event.ItemEvent evt) {
                 contestSelectItemStateChanged(evt);
             }
         });
 
         outputArea.setColumns(20);
         outputArea.setEditable(false);
         outputArea.setRows(5);
         jScrollPane1.setViewportView(outputArea);
 
         submitButton.setText("Submit");
         submitButton.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 submitButtonActionPerformed(evt);
             }
         });
 
         jLabel5.setText("Language:");
 
         languageSelect.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Java", "Python", "C++" }));
 
         jButton2.setText("-");
 
         jButton1.setText("-");
 
         jButton3.setText("-");
 
         jButton4.setText("-");
 
         jButton5.setText("-");
 
         jButton6.setText("-");
 
         jButton7.setText("-");
 
         jButton8.setText("-");
 
         jButton9.setText("-");
 
         jButton10.setText("-");
 
         jButton11.setText("-");
 
         jButton12.setText("-");
 
         jButton13.setText("-");
 
         jButton14.setText("-");
 
         jButton15.setText("-");
 
         jButton16.setText("-");
 
         jLabel6.setText("Judge by Michael Colavita");
 
         javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
         getContentPane().setLayout(layout);
         layout.setHorizontalGroup(
             layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(layout.createSequentialGroup()
                 .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 53, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                 .addComponent(jButton2, javax.swing.GroupLayout.PREFERRED_SIZE, 53, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(jButton3, javax.swing.GroupLayout.PREFERRED_SIZE, 53, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(jButton4, javax.swing.GroupLayout.PREFERRED_SIZE, 53, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(jButton5, javax.swing.GroupLayout.PREFERRED_SIZE, 53, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(jButton6, javax.swing.GroupLayout.PREFERRED_SIZE, 53, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(jButton7, javax.swing.GroupLayout.PREFERRED_SIZE, 53, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addGroup(layout.createSequentialGroup()
                         .addComponent(jButton8, javax.swing.GroupLayout.PREFERRED_SIZE, 53, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addComponent(jButton9, javax.swing.GroupLayout.PREFERRED_SIZE, 53, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addComponent(jButton10, javax.swing.GroupLayout.PREFERRED_SIZE, 53, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addComponent(jButton11, javax.swing.GroupLayout.PREFERRED_SIZE, 53, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addComponent(jButton12, javax.swing.GroupLayout.PREFERRED_SIZE, 53, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addComponent(jButton13, javax.swing.GroupLayout.PREFERRED_SIZE, 53, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addComponent(jButton14, javax.swing.GroupLayout.PREFERRED_SIZE, 53, javax.swing.GroupLayout.PREFERRED_SIZE))
                     .addComponent(jLabel6))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                     .addGroup(layout.createSequentialGroup()
                         .addComponent(jButton15, javax.swing.GroupLayout.PREFERRED_SIZE, 53, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addComponent(jButton16, javax.swing.GroupLayout.PREFERRED_SIZE, 53, javax.swing.GroupLayout.PREFERRED_SIZE))
                     .addComponent(submitButton)))
             .addGroup(layout.createSequentialGroup()
                 .addComponent(jLabel5)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(languageSelect, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addContainerGap())
             .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                 .addComponent(jLabel1)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(fileName)
                 .addGap(12, 12, 12)
                 .addComponent(browseButton))
             .addGroup(layout.createSequentialGroup()
                 .addComponent(jLabel3)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(contestSelect, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
             .addGroup(layout.createSequentialGroup()
                 .addComponent(jLabel2)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(originSelect, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
             .addGroup(layout.createSequentialGroup()
                 .addComponent(jLabel4)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(problemSelect, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
             .addComponent(jScrollPane1)
         );
         layout.setVerticalGroup(
             layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(layout.createSequentialGroup()
                 .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(originSelect, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(jLabel2))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(jLabel3)
                     .addComponent(contestSelect, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(jLabel4)
                     .addComponent(problemSelect, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(fileName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(jLabel1)
                     .addComponent(browseButton))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 291, Short.MAX_VALUE)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 44, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(jButton2, javax.swing.GroupLayout.PREFERRED_SIZE, 44, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(jButton15, javax.swing.GroupLayout.PREFERRED_SIZE, 44, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(jButton16, javax.swing.GroupLayout.PREFERRED_SIZE, 44, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(jButton3, javax.swing.GroupLayout.PREFERRED_SIZE, 44, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(jButton4, javax.swing.GroupLayout.PREFERRED_SIZE, 44, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(jButton5, javax.swing.GroupLayout.PREFERRED_SIZE, 44, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(jButton6, javax.swing.GroupLayout.PREFERRED_SIZE, 44, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(jButton7, javax.swing.GroupLayout.PREFERRED_SIZE, 44, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(jButton8, javax.swing.GroupLayout.PREFERRED_SIZE, 44, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(jButton9, javax.swing.GroupLayout.PREFERRED_SIZE, 44, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(jButton10, javax.swing.GroupLayout.PREFERRED_SIZE, 44, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(jButton11, javax.swing.GroupLayout.PREFERRED_SIZE, 44, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(jButton12, javax.swing.GroupLayout.PREFERRED_SIZE, 44, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(jButton13, javax.swing.GroupLayout.PREFERRED_SIZE, 44, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(jButton14, javax.swing.GroupLayout.PREFERRED_SIZE, 44, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                         .addComponent(jLabel5)
                         .addComponent(languageSelect, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addComponent(jLabel6))
                     .addComponent(submitButton)))
         );
 
         layout.linkSize(javax.swing.SwingConstants.VERTICAL, new java.awt.Component[] {jButton1, jButton10, jButton11, jButton12, jButton13, jButton14, jButton15, jButton16, jButton2, jButton3, jButton4, jButton5, jButton6, jButton7, jButton8, jButton9});
 
     }// </editor-fold>//GEN-END:initComponents
 
     private void clearConsole() {
         outputArea.setText("");
     }
 
     private void println(String st) {
         outputArea.setText(outputArea.getText() + st + "\n");
     }
 
     private void print(String st) {
         outputArea.setText(outputArea.getText() + st);
     }
 
     private void abortSubmission() {
         println("Submission aborted.");
     }
 
     private void browseButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_browseButtonActionPerformed
         //Open a dialog to choose a file
         JFileChooser jfc = new JFileChooser();
         int result = jfc.showOpenDialog(null);
         if (result != JFileChooser.APPROVE_OPTION) {
             abortSubmission();
             return;
         }
         File selected = jfc.getSelectedFile();
         fileName.setText(selected.getPath());
     }//GEN-LAST:event_browseButtonActionPerformed
 
     private void submitButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_submitButtonActionPerformed
         Thread t = new Thread(new Runnable() {
 
             public void run() {
                 submitButton.setEnabled(false);
                 submit();
                 submitButton.setEnabled(true);
             }
         });
         t.start();
     }//GEN-LAST:event_submitButtonActionPerformed
 
     private void originSelectItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_originSelectItemStateChanged
         //Update contest and problem list
         contestSelect.setModel(new DefaultComboBoxModel(contests[originSelect.getSelectedIndex()]));
         problemSelect.setModel(new DefaultComboBoxModel(problems[originSelect.getSelectedIndex()][contestSelect.getSelectedIndex()]));
     }//GEN-LAST:event_originSelectItemStateChanged
 
     private void contestSelectItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_contestSelectItemStateChanged
         //Update problem list
         problemSelect.setModel(new DefaultComboBoxModel(problems[originSelect.getSelectedIndex()][contestSelect.getSelectedIndex()]));
     }//GEN-LAST:event_contestSelectItemStateChanged
 
     private void submit() {
         //Reset test buttons
         for (int i = 0; i < 16; i++) {
             tests[i].setBackground(Color.GRAY);
             tests[i].setText("-");
         }
         //Clear console
         outputArea.setBackground(Color.WHITE);
         clearConsole();
         //Check that file exists
         File selected = new File(fileName.getText());
         if (!selected.exists()) {
             println("Chosen file is nonexistent.");
             abortSubmission();
             return;
         }
         //File length check
         if (selected.length() > MAX_LENGTH) { //File too long
            println("Fail: File size exceeds " + MAX_LENGTH + " bytes.");
             abortSubmission();
             return;
         }
         //Type check
         int language = languageSelect.getSelectedIndex();
         String selFileName = selected.getPath();
         String[] divide = selFileName.split("\\.");
         if (divide.length == 1) {
            println("Fail: File has no extension.");
             abortSubmission();
             return;
         }
         String extension = divide[divide.length - 1];
         boolean matchFound = false;
         //Search acceptable file extensions for match
         for (int i = 0; i < acceptableFileExtensions[language].length; i++) {
             if (extension.toLowerCase().contentEquals(acceptableFileExtensions[language][i].toLowerCase())) { //Match found
                 matchFound = true;
                 break;
             }
         }
         if (!matchFound) { //Invalid extension
            println("Fail: Extension \"" + extension + "\" is invalid for language \"" + languageSelect.getSelectedItem() + "\".");
             abortSubmission();
             return;
         }
         InputStream is = null;
         OutputStream os = null;
         try {
             //Read file to socket
             FileInputStream fis = new FileInputStream(selected);
             Socket socket = new Socket(SERVERIPADDR, 13786);
             for (int i = 0; i < 10; i++) {
                 try {
                     os = socket.getOutputStream();
                 } catch (Exception e) {
                     continue;
                 }
                 break;
             }
             if (os == null) {
                 abortSubmission();
                 return;
             }
             //Connection has been established
             println("Connection established.");
             //Send info header
             String header = selected.getName() + "===SPLIT HEADER===" + originSelect.getSelectedItem() + "===SPLIT HEADER===" + contestSelect.getSelectedItem() + "===SPLIT HEADER===" + problemSelect.getSelectedItem() + "===SPLIT HEADER===" + languageSelect.getSelectedItem() + "===END HEADER===\n";
             os.write(header.getBytes());
             //Send file
             int len;
             byte[] buffer = new byte[4096];
             while ((len = fis.read(buffer)) != -1) {
                 os.write(buffer, 0, len);
             }
             //File submission completed
             fis.close();
             socket.shutdownOutput();
             println("File sent! Awaiting results.");
             //Listen for results
             is = socket.getInputStream();
             int testOn = 0;
             out:
             while (true) {
                 int read = is.read();
                 if (read == -1) {
                     throw new IOException("Stream terminated unexpectedly");
                 }
                 switch (read) {
                     case JUDGING_INIT:
                         println("Judging initiated");
                         break;
                     case COMPILE_FAIL:
                         println("Compilation failed!");
                         break;
                     case COMPILE_PASS:
                         println("Compilation successful!");
                         break;
                     case TEST_PASS:
                         tests[testOn].setVisible(true);
                         tests[testOn].setText("OK");
                         //tests[testOn].setEnabled(false);
                         tests[testOn++].setBackground(Color.GREEN);
                         println("Test " + (testOn) + " passed!");
                         break;
                     case TEST_FAIL_TIMEOUT:
                         tests[testOn].setVisible(true);
                         tests[testOn].setText("T");
                         //tests[testOn].setEnabled(false);
                         tests[testOn++].setBackground(Color.RED);
                         println("Test " + (testOn) + " failed: Timeout!");
                         break;
                     case TEST_FAIL_WRONG:
                         tests[testOn].setVisible(true);
                         tests[testOn].setText("X ");
                         //tests[testOn].setEnabled(false);
                         tests[testOn++].setBackground(Color.RED);
                         println("Test " + (testOn) + " failed: Incorrect!");
                         break;
                     case INVALID_PROBLEM:
                         System.out.println("Faulty problem: " + contestSelect.getSelectedItem() + ": " + problemSelect.getSelectedItem());
                         println("Invalid problem selected.");
                         break;
                     case JUDGING_ERROR:
                         println("The judge encountered an error.");
                         break;
                     case TESTS_BAD:
                         println("Tests failed!");
                         outputArea.setBackground(Color.PINK);
                         break out;
                     case TESTS_GOOD:
                         println("Tests passed!");
                         outputArea.setBackground(Color.GREEN);
                         break out;
                     case JUDGING_ABORT:
                         println("Judging aborted!");
                         outputArea.setBackground(Color.PINK);
                         break out;
                 }
             }
         } catch (Exception e) {
             println("An IOException occurred while submitting: " + e.getMessage() + ".");
             e.printStackTrace();
             abortSubmission();
         } finally {
             try {
                 os.close();
                 is.close();
             } catch (Exception e) {
             }
         }
     }
     // Variables declaration - do not modify//GEN-BEGIN:variables
     private javax.swing.JButton browseButton;
     private javax.swing.JComboBox contestSelect;
     private javax.swing.JTextField fileName;
     private javax.swing.JButton jButton1;
     private javax.swing.JButton jButton10;
     private javax.swing.JButton jButton11;
     private javax.swing.JButton jButton12;
     private javax.swing.JButton jButton13;
     private javax.swing.JButton jButton14;
     private javax.swing.JButton jButton15;
     private javax.swing.JButton jButton16;
     private javax.swing.JButton jButton2;
     private javax.swing.JButton jButton3;
     private javax.swing.JButton jButton4;
     private javax.swing.JButton jButton5;
     private javax.swing.JButton jButton6;
     private javax.swing.JButton jButton7;
     private javax.swing.JButton jButton8;
     private javax.swing.JButton jButton9;
     private javax.swing.JLabel jLabel1;
     private javax.swing.JLabel jLabel2;
     private javax.swing.JLabel jLabel3;
     private javax.swing.JLabel jLabel4;
     private javax.swing.JLabel jLabel5;
     private javax.swing.JLabel jLabel6;
     private javax.swing.JScrollPane jScrollPane1;
     private javax.swing.JComboBox languageSelect;
     private javax.swing.JComboBox originSelect;
     private javax.swing.JTextArea outputArea;
     private javax.swing.JComboBox problemSelect;
     private javax.swing.JButton submitButton;
     // End of variables declaration//GEN-END:variables
 }
