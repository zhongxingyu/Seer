 package dit;
 
 import dit.panels.UserPanel;
 import dit.panels.WizardPanel;
 import java.awt.Toolkit;
 import java.awt.event.WindowAdapter;
 import java.awt.event.WindowEvent;
 import java.io.*;
 import java.util.Date;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javax.swing.JPanel;
 import java.util.zip.*;
 import java.util.Iterator;
 /**
  *
  * @author christianprescott
  */
 public class DEIDGUI extends javax.swing.JFrame {
     public static enum LOG_LEVEL {INFO, ERROR, WARNING};
     private static FileWriter logWriter;
     private static ErrorFrame errWindow;
     private static HelpManualFrame help;
     private static misHelpFrame mhf;
     public static String title = "User Information";
     /**
      * Creates new form DEIDGUI
      */
     public DEIDGUI() {
         initComponents();
         //  this.setTitle("Deidentification Tool- " + title);
         // Set DeID's platform-dependent output location
         jButtonMisHelp.setVisible(false);
         if(FileUtils.OS.isMac() || FileUtils.OS.isUnix()){
             DeidData.outputPath = "/tmp/deid_output/";
         } else if(FileUtils.OS.isWindows()){
             DeidData.outputPath = "E:\\Temp\\deid_output\\";
         } else if(FileUtils.OS.isUnix()){
             DeidData.outputPath = "/tmp/deid_output/";
         } else {
             DEIDGUI.log("Couldn't identify platform in \"" + FileUtils.OS.getOS()
                     + "\", local output directory will be used.", LOG_LEVEL.WARNING);
             DeidData.outputPath = "deid_output/";
         }
         
         //TODO: remove these
         //        DeidData.UserFullName = "Christian james precott";
         //        DeidData.UserInstitution = "Clemson U";
         //        DeidData.inputFiles = new Vector<File>(Arrays.asList(new File[]{new File("/Users/christianprescott/Desktop/dataset/152T1.nii")}));
         
         File logFile = new File(DeidData.outputPath + "deid.log");
         errWindow = new ErrorFrame(logFile);
         InitLogFile(logFile);
         
         this.addWindowListener(new WindowAdapter() {
             
             @Override
             public void windowClosing(WindowEvent e) {
                 DEIDGUI.log("Exiting");
                 // TODO: Delete files in the temporary output folder
                 dispose();
                 try {
                     DEIDGUI.logWriter.close();
                 } catch (IOException ex) {
                     Logger.getLogger(DEIDGUI.class.getName()).log(Level.SEVERE, null, ex);
                 }
                 System.exit(0);
             }
         });
         
         // Copy tools to the tmp directory
         unpackTools();
         
         pagePanel.setLayout(new java.awt.BorderLayout());
         pagePanel.add(new UserPanel());
         
         log("Deidentification tool GUI initialized");
         // TODO: File move does not work on linux
         // TODO: Mac, linux, windows(.bat) shell scripts
         // TODO: sharing mode in data file for each image?
         // TODO: Linux compatibility
         // TODO: Manual image-ID matching
         // TODO: Update progress bar for image montage and tar creation
         // TODO: VM option -Xdock:name=DeID sets the name displayed in the Apple menu bar.
         // TODO: use -Xmx256m VM option to ensure DeID is allowed the memory
         //    it needs to display images. Even more memory needed in some cases.
         // TODO: Link to help page with overview of functions and errors
         //    Warn in documentation about lots of free space needed on disk for
         //    converted files
         //    Attributions for dcm4che, fsl, dcm2nii, niftijlib, jtar, jsch, and ftp4j
         //    The fsl tools don't play nice with files on secondary drives, they
         //    Just throw a file read error (which DeID will display to the user)
         // TODO: default FTP server credentials for MUSC's server
         // TODO: Ability to cancel deface and tar and FTP procedures
         //    (See conversion tasks for example implementation)
         // TODO: file cleanup on start or exit/completion? The temp folder is cleared
         //    on shutdown anyway, but it could grow to be very large. Simply
         //    delete all files in /tmp/deid_output except log file
         // TODO: fslchfiletype and bet will have to have Windows/Unix specific
         //    builds because they need to know the absolute path of the tools
         // TODO: It would be nice to refactor the fields in DeidData into a nice
         //    DeidImage class, with data about each image's source and result of
         //    conversions or deface.
         // TODO: Error frame sometimes displays blank list panel, possible thread-related error?
     }
     
     /* public static void settheTitle(){
      * this.setTitle("Deidentification Tool- " + title);
      * 
      * }*/
     private static String newline = "\n";
     private void InitLogFile(File logFile) {
         newline = System.getProperty("line.separator");
         
         File outputDir = new File(DeidData.outputPath);
         if(!outputDir.exists()){
             outputDir.mkdirs();
         }
         
         try {
             logFile.createNewFile();
             logWriter = new FileWriter(logFile, false);
         } catch (IOException ex) {
             Logger.getLogger(DEIDGUI.class.getName()).log(Level.SEVERE, null, ex);
         }
         log("########################################");
         log("# Deidentification Tool log            #");
         String dateStr = new Date().toString();
         String dateLine = "# " + dateStr;
         for (int ndx = 0; ndx < 40 - (dateStr.length() + 3); ndx++) {
             dateLine += " ";
         }
         log(dateLine + "#");
         log("########################################");
     }
     
     public static void log(String line){
         log(line, LOG_LEVEL.INFO);
     }
     
     public static void log(String line, LOG_LEVEL level) {
         if(line.isEmpty()){
             return;
         }
         
         String writerLine = line.trim();
         if(level == LOG_LEVEL.ERROR){
             errWindow.addError(line, ErrorFrame.ERROR_TYPE.ERROR);
             writerLine = "ERROR: " + writerLine;
         } else if (level == LOG_LEVEL.WARNING){
             errWindow.addError(line, ErrorFrame.ERROR_TYPE.WARNING);
             writerLine = "WARNING: " + writerLine;
         }
         
         if (logWriter != null) {
             try {
                 logWriter.write(writerLine + newline);
                 logWriter.flush();
             } catch (IOException ex) {
                 Logger.getLogger(DEIDGUI.class.getName()).log(Level.SEVERE, null, ex);
             }
         }
         
     }
     
     private void unpackTools() {
         
          String[] toolNames = new String[]{
         
             "bet",
             "bet2",
             "dcm2nii",
             "mricron",
             "mricron_64",
             "fslchfiletype",
             "fslchfiletype_exe",
             "imtest",
             "remove_ext",
             "fslview",
             "libbint.so",
             "libcprob.so",
             "libfslio.so",
             "libggmix.so",
             "libhfunc.so",
             "libmeshclass.so",
             "libmiscmaths.so",
             "libmiscpic.so",
             "libmiscplot.so",
             "libmm.so",
             "libnewimage.so",
             "libprob.so",
             "libshapeModel.so",
             "libutils.so",
             "libnewmat.so.10.0.0",
             "libniftiio.so.2.0.0",
             "libstdc++.so.6.0.14",
             "libz.so.1.2.3.4",
             "libznz.so.2.0.0",
             "libnewmat.so.10.gz",
             "libniftiio.so.2.gz",
             "libstdc++.so.6.gz",
             "libz.so.1.gz",
             "libznz.so.2.gz",
             "default.ini"
         };
         
         /* String[] toolNames = new String[]{
          * "bet",
          * "bet2",
          * "dcm2nii",
          * "fslchfiletype",
          * "fslchfiletype_exe",
          * "imtest",
          * "remove_ext",};*/
         
         // Extract only the tools for this platform
         File outputDir;
         String osPrefix, outputPath;
         if(FileUtils.OS.isMac()){
             outputPath = "/tmp/";
             osPrefix = "osx";
         } else if(FileUtils.OS.isWindows()){
             outputPath = "E:\\Temp\\";
             osPrefix = "win";
             toolNames = new String[]{"robex.zip"};
             // Windows executables work fine without the .exe extension, no
             // need to concatenate it to unpacked files.
         } else if(FileUtils.OS.isUnix()){
             outputPath = "/tmp/";
             osPrefix = "nix";
         } else {
             DEIDGUI.log("Couldn't identify platform in \"" + FileUtils.OS.getOS()
                     + "\", unix tools will be used.", LOG_LEVEL.WARNING);
             outputPath = "/tmp/";
             osPrefix = "nix";
         }
         outputDir = new File(outputPath + "dit_tools");
         
        
         
         log("Unpacking " + toolNames.length + " " + osPrefix +
                 " tools to " + outputDir);
         if (!outputDir.exists()) {
             outputDir.mkdirs();
         }
         
         int unpackCount = 0, existCount = 0;
         for (String toolName : toolNames) {
             File oFile = new File(outputDir.getAbsolutePath() + File.separator + toolName);
             
             if (!oFile.exists()) {
                 // Get the resource from the jar
                 InputStream rStream = getClass().getResourceAsStream(
                         "tools/" + osPrefix + "_" + toolName);
                 FileOutputStream oStream = null;
                 if (rStream == null) {
                     DEIDGUI.log("Unable to get ResourceStream for " + "tools/" +
                             osPrefix + "_" + toolName + ", some deidentificati"
                             + "on tasks may fail", DEIDGUI.LOG_LEVEL.ERROR);
                 } else {
                     try {
                         oFile.createNewFile();
                         oStream = new FileOutputStream(oFile);
                         
                         // Copy the file from resources to a temp location
                         int size;
                         byte[] buf = new byte[1024];
                         while ((size = rStream.read(buf, 0, 1024)) != -1) {
                             oStream.write(buf, 0, size);
                         }
                         
                         oFile.setExecutable(true);
                         // Save locations of critical tools on successful unzip
                         DeidData.unpackedFileLocation.put(toolName, oFile);
                         unpackCount++;
                     } catch (FileNotFoundException ex) {
                         DEIDGUI.log("An unpacked file "
                                 + "was not found, some deidentification tasks "
                                 + "may fail: " + ex.getMessage(), DEIDGUI.LOG_LEVEL.ERROR);
                     } catch (IOException ex) {
                         DEIDGUI.log("Unpacking " + toolName + " failed, some "
                                 + "deidentification tasks may fail: " +
                                 ex.getMessage(), DEIDGUI.LOG_LEVEL.WARNING);
                     } finally {
                         if (oStream != null) {
                             try {
                                 oStream.close();
                             } catch (IOException ex) {
                             }
                         }
                         try {
                             rStream.close();
                         } catch (IOException ex) {
                         }
                     }
                 }
             } else {
                 // Save locations of critical tools
                 DeidData.unpackedFileLocation.put(toolName, oFile);
                 existCount++;
             }
         }
         
         if(unpackCount > 0){
             log(unpackCount + " tools were successfully unpacked");
         }
         if(existCount > 0){
             log(existCount + " tools were already in place");
         }
         String gzfiles[] = new String[]{"/tmp/dit_tools/libnewmat.so.10", "/tmp/dit_tools/libniftiio.so.2","/tmp/dit_tools/libstdc++.so.6", "/tmp/dit_tools/libz.so.1",
             "/tmp/dit_tools/libznz.so.2"};
        String zipfiles[]= new String[]{};
         if(FileUtils.OS.isWindows())
         {
             gzfiles=new String[]{};
           zipfiles= new String[]{DeidData.unpackedFileLocation.get("robex.zip").getParentFile().getAbsolutePath()+"\\robex"};
         }
         else
         {
             zipfiles=new String[]{};
         }
         for(String gzfile:gzfiles){
             try{
                 FileInputStream instream= new FileInputStream(gzfile+".gz");
                 FileOutputStream outstream;
                 try (GZIPInputStream ginstream = new GZIPInputStream(instream)) {
                     outstream = new FileOutputStream(gzfile);
                     byte[] buf = new byte[1024];
                     int len;
                     while ((len = ginstream.read(buf)) > 0)
                     {
                         outstream.write(buf, 0, len);
                     }
                 }
                 outstream.close();
             } catch(IOException e){
                 
                 System.out.println("failed:"+e);
             }
         }
         
         for(String gzfile:zipfiles){
             try {
                  String destinationname = DeidData.unpackedFileLocation.get("robex.zip").getParentFile().getAbsolutePath()+"\\";
                 byte[] buf = new byte[1024];
                 ZipInputStream zipinputstream = null;
                 ZipEntry zipentry;
                 zipinputstream = new ZipInputStream(
                         new FileInputStream(gzfile+".zip"));
                 
                 zipentry = zipinputstream.getNextEntry();
                 while (zipentry != null) {
                     //for each entry to be extracted
                     String entryName = destinationname + zipentry.getName();
                     entryName = entryName.replace('/', File.separatorChar);
                     entryName = entryName.replace('\\', File.separatorChar);
                     System.out.println("entryname " + entryName);
                     int n;
                     FileOutputStream fileoutputstream;
                     File newFile = new File(entryName);
                     if (zipentry.isDirectory()) {
                         if (!newFile.mkdirs()) {
                             break;
                         }
                         zipentry = zipinputstream.getNextEntry();
                         continue;
                     }
                     
                     fileoutputstream = new FileOutputStream(entryName);
                     
                     while ((n = zipinputstream.read(buf, 0, 1024)) > -1) {
                         fileoutputstream.write(buf, 0, n);
                     }
                     
                     fileoutputstream.close();
                     zipinputstream.closeEntry();
                     zipentry = zipinputstream.getNextEntry();
                     
                 }//while
                 
                 zipinputstream.close();
             } catch (Exception e) {
                 e.printStackTrace();
             }
         }
         
         
         
     }
     
     /**
      * This method is called from within the constructor to initialize the form.
      * WARNING: Do NOT modify this code. The content of this method is always
      * regenerated by the Form Editor.
      */
     @SuppressWarnings("unchecked")
     // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
     private void initComponents() {
 
         buttonPanel = new javax.swing.JPanel();
         jSeparator5 = new javax.swing.JSeparator();
         continueButton = new javax.swing.JButton();
         backButton = new javax.swing.JButton();
         cancelButton = new javax.swing.JButton();
         helpButton = new javax.swing.JButton();
         errlogButton = new javax.swing.JButton();
         jButtonMisHelp = new javax.swing.JButton();
         pagePanel = new javax.swing.JPanel();
 
         setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
         setTitle("DeIDentification Tool");
         setMinimumSize(new java.awt.Dimension(512, 512));
 
         continueButton.setText("Continue >");
         continueButton.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 continueButtonActionPerformed(evt);
             }
         });
 
         backButton.setText("< Back");
         backButton.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 backButtonActionPerformed(evt);
             }
         });
 
         cancelButton.setText("Cancel");
         cancelButton.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 cancelButtonActionPerformed(evt);
             }
         });
 
         helpButton.setText("Help");
         helpButton.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 helpButtonActionPerformed(evt);
             }
         });
 
         errlogButton.setText("See Error Log");
         errlogButton.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 errlogButtonActionPerformed(evt);
             }
         });
 
         jButtonMisHelp.setText("Mismatch Help");
         jButtonMisHelp.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 jButtonMisHelpActionPerformed(evt);
             }
         });
 
         org.jdesktop.layout.GroupLayout buttonPanelLayout = new org.jdesktop.layout.GroupLayout(buttonPanel);
         buttonPanel.setLayout(buttonPanelLayout);
         buttonPanelLayout.setHorizontalGroup(
             buttonPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(buttonPanelLayout.createSequentialGroup()
                 .addContainerGap()
                 .add(buttonPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                     .add(buttonPanelLayout.createSequentialGroup()
                         .add(cancelButton)
                         .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                         .add(helpButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 61, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                         .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                         .add(errlogButton)
                         .add(18, 18, 18)
                         .add(jButtonMisHelp)
                         .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                         .add(backButton)
                         .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                         .add(continueButton))
                     .add(jSeparator5))
                 .addContainerGap())
         );
         buttonPanelLayout.setVerticalGroup(
             buttonPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(org.jdesktop.layout.GroupLayout.TRAILING, buttonPanelLayout.createSequentialGroup()
                 .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                 .add(jSeparator5, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 10, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                 .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                 .add(buttonPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                     .add(continueButton)
                     .add(backButton)
                     .add(cancelButton)
                     .add(helpButton)
                     .add(errlogButton)
                     .add(jButtonMisHelp))
                 .addContainerGap())
         );
 
         org.jdesktop.layout.GroupLayout pagePanelLayout = new org.jdesktop.layout.GroupLayout(pagePanel);
         pagePanel.setLayout(pagePanelLayout);
         pagePanelLayout.setHorizontalGroup(
             pagePanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(0, 652, Short.MAX_VALUE)
         );
         pagePanelLayout.setVerticalGroup(
             pagePanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(0, 428, Short.MAX_VALUE)
         );
 
         org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(getContentPane());
         getContentPane().setLayout(layout);
         layout.setHorizontalGroup(
             layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(buttonPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
             .add(pagePanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
         );
         layout.setVerticalGroup(
             layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                 .add(pagePanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                 .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                 .add(buttonPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
         );
 
         pack();
     }// </editor-fold>//GEN-END:initComponents
     
     public static void advance() {
         continueButton.setEnabled(true);
         backButton.setEnabled(true);
         
         try{
             WizardPanel nextPanel = ((WizardPanel) pagePanel.getComponent(0)).getNextPanel();
             pagePanel.remove(0);
             pagePanel.add((JPanel) nextPanel);
             pagePanel.revalidate();
             log(">>> Advanced to " + nextPanel.getClass().getSimpleName());
         } catch (Exception e){
             e.printStackTrace();
             log("Failed to advance: " + e.getMessage(), LOG_LEVEL.ERROR);
         }
     }
     
     public static void previous() {
         continueButton.setEnabled(true);
         backButton.setEnabled(true);
         
         try{
             WizardPanel prevPanel = ((WizardPanel) pagePanel.getComponent(0)).getPreviousPanel();
             pagePanel.remove(0);
             pagePanel.add((JPanel) prevPanel);
             pagePanel.revalidate();
             log("<<< Back to " + prevPanel.getClass().getSimpleName());
         } catch (Exception e){
             log("Failed to go back: " + e.getMessage(), LOG_LEVEL.ERROR);
         }
         
     }
     
     private void continueButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_continueButtonActionPerformed
         advance();
     }//GEN-LAST:event_continueButtonActionPerformed
     
     private void backButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_backButtonActionPerformed
         previous();
     }//GEN-LAST:event_backButtonActionPerformed
     
     private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelButtonActionPerformed
         Toolkit.getDefaultToolkit().getSystemEventQueue().postEvent(
                 new WindowEvent(this, WindowEvent.WINDOW_CLOSING));
     }//GEN-LAST:event_cancelButtonActionPerformed
     
     private void helpButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_helpButtonActionPerformed
         // TODO add your handling code here:
         help = new HelpManualFrame(title);
         help.pack();
         help.setVisible(true);
     }//GEN-LAST:event_helpButtonActionPerformed
     
     private void errlogButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_errlogButtonActionPerformed
         // TODO add your handling code here:
         if (!DeidData.errorlog.isEmpty()){
             Iterator vItr = DeidData.errorlog.iterator();
             while(vItr.hasNext())
             {
                 log(vItr.next().toString(), LOG_LEVEL.WARNING);
             }
         }
     }//GEN-LAST:event_errlogButtonActionPerformed
     
     private void jButtonMisHelpActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonMisHelpActionPerformed
         // TODO add your handling code here:
         mhf = new misHelpFrame();
         mhf.pack();
         
         mhf.setVisible(true);
     }//GEN-LAST:event_jButtonMisHelpActionPerformed
     
     /**
      * @param args the command line arguments
      */
     public static void main(String args[]) {
         if(FileUtils.OS.isMac()){
             // Use the Mac OS X Menu bar instead of the JFrame one
             System.setProperty("apple.laf.useScreenMenuBar", "true");
             // This name setting does not work
             System.setProperty("com.apple.mrj.application.apple.menu.about.name", "DeID");
         }
         
         /*
          * Set the OS look and feel
          */
         //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
         // Use the System look and feel
         try {
             javax.swing.UIManager.setLookAndFeel(javax.swing.UIManager.getSystemLookAndFeelClassName());
         } catch (ClassNotFoundException ex) {
             java.util.logging.Logger.getLogger(DEIDGUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
         } catch (InstantiationException ex) {
             java.util.logging.Logger.getLogger(DEIDGUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
         } catch (IllegalAccessException ex) {
             java.util.logging.Logger.getLogger(DEIDGUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
         } catch (javax.swing.UnsupportedLookAndFeelException ex) {
             java.util.logging.Logger.getLogger(DEIDGUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
         }
         //</editor-fold>
         
         /*
          * Create and display the form
          */
         
         java.awt.EventQueue.invokeLater(new Runnable() {
             
             @Override
             public void run() {
                 new DEIDGUI().setVisible(true);
                 
                 //                DEIDGUI.log("This is a super long warning, it's just so warnful"
                 //                        + " file /Users/christian/Desktop/image.jpeg",
                 //                        LOG_LEVEL.WARNING);
                 //                DEIDGUI.log("This is a super long error, it is similar in its "
                 //                        + "longfulness to the previous warning file "
                 //                        + "/Users/christian/Desktop/image.jpeg",
                 //                        LOG_LEVEL.ERROR);
             }
         });
     }
     // Variables declaration - do not modify//GEN-BEGIN:variables
     public static javax.swing.JButton backButton;
     private javax.swing.JPanel buttonPanel;
     public static javax.swing.JButton cancelButton;
     public static javax.swing.JButton continueButton;
     public static javax.swing.JButton errlogButton;
     public static javax.swing.JButton helpButton;
     public static javax.swing.JButton jButtonMisHelp;
     private javax.swing.JSeparator jSeparator5;
     private static javax.swing.JPanel pagePanel;
     // End of variables declaration//GEN-END:variables
 }
