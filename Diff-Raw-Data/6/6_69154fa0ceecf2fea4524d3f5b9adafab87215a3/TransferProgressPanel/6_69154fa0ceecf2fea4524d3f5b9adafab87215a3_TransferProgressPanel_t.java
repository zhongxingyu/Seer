 package dit.panels;
 
 import com.jcraft.jsch.*;
 import dit.DEIDGUI;
 import dit.DeidData;
 import dit.FileUtils;
 import dit.TarMaker;
 import it.sauronsoftware.ftp4j.*;
 import java.awt.Component;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import javax.swing.JPasswordField;
 import java.io.InputStream;
 import java.io.FileOutputStream;
 import java.io.FileInputStream;
 import java.util.zip.GZIPOutputStream;
 import java.io.BufferedInputStream;
 import java.util.Hashtable;
 import java.util.Iterator;
 import java.util.logging.Level;
 /**
  *
  * @author christianprescott
  */
 public class TransferProgressPanel extends javax.swing.JPanel implements WizardPanel{
     private File tarFile, logFile, tarFileO;
     private String FTPServer, FTPUser, FTPShare;
     private JPasswordField FTPPassField;
     private String remotePath="";
     private int FTPPort;
 
     /**
      * Creates new form ConvertingImagesPanel
      */    
     public TransferProgressPanel(JPasswordField passField){
         initComponents();
         DEIDGUI.helpButton.setEnabled(false);
         FTPServer = TransferPanel.FTPServer;
         FTPPort = TransferPanel.FTPPort;
         FTPUser = TransferPanel.FTPUser;
         FTPPassField = passField;
         remotePath= TransferPanel.remotePath;
         
         switch(TransferPanel.ShareMode){
             case 0:
                 FTPShare = "none";
                 break;
             case 1:
                 FTPShare = "enclave";
                 break;
             case 2:
                 FTPShare = "all";
                 break;
             default:
                 DEIDGUI.log("Invalid sharing level selection, data will not be "
                         + "shared: " + TransferPanel.ShareMode, 
                         DEIDGUI.LOG_LEVEL.WARNING);
                 FTPShare = "none";
                 break;
         }
         
         // Use the user's initials to name the tar file
         String[] words = DeidData.UserFullName.split("\\s");
         String initials = "";
         for (String s : words){
             if(s.length() > 0){
                 initials += s.toLowerCase().charAt(0);
             }
         }
         String tarName = "deid-" + initials + "-" + 
                 new SimpleDateFormat("yyMMdd-HHmmss").format(new Date());
         tarFile = new File(DeidData.outputPath + tarName + ".tar");
         logFile = new File(DeidData.outputPath + "log.txt");
 
         DEIDGUI.continueButton.setEnabled(false);
         DEIDGUI.backButton.setEnabled(false);
     
        doTransfer();    
         
         DEIDGUI.log("TransferProgressPanel initialized");
     }
     
     private void doTransfer(){
         final Component that = this;
        
         new Thread(new Runnable() {
             @Override
             public void run() {
                 if(!createLog()){
                     DEIDGUI.log("Unable to create log file. tarball not created.", DEIDGUI.LOG_LEVEL.ERROR);
                     DEIDGUI.previous();
                     return;
                 }
                 
                 if(!createTar()){
                     /*// Prompt user to examine tar, then choose to continue or abort
                     String[] options = new String[] {"Cancel", "Upload"};
                     int choice = JOptionPane.showOptionDialog(that, 
                             "There were errors when "
                             + "creating the tarball. Please examine the data "
                             + "and choose to continue or cancel upload.",
                             "Error Creating Tarball",
                             JOptionPane.OK_CANCEL_OPTION, 
                             JOptionPane.WARNING_MESSAGE, 
                             null, // No icon
                             options, 
                             options[0]);*/
                     DEIDGUI.log("Errors creating tarball, further save action aborted", DEIDGUI.LOG_LEVEL.ERROR);
                     DEIDGUI.previous();
                     return;
                 }
                 
                 if(TransferPanel.doFTP){
                     boolean success = false;
                     switch(TransferPanel.FTPProtocol){
                         case 0:
                             success = uploadTarSFTP();
                             break;
                         case 1:
                             try {
                                 success = uploadTarFTPS();
                             } catch (IllegalStateException ex) {
                                 java.util.logging.Logger.getLogger(TransferProgressPanel.class.getName()).log(Level.SEVERE, null, ex);
                             } catch (IOException ex) {
                                 java.util.logging.Logger.getLogger(TransferProgressPanel.class.getName()).log(Level.SEVERE, null, ex);
                             } catch (FTPIllegalReplyException ex) {
                                 java.util.logging.Logger.getLogger(TransferProgressPanel.class.getName()).log(Level.SEVERE, null, ex);
                             } catch (FTPException ex) {
                                 java.util.logging.Logger.getLogger(TransferProgressPanel.class.getName()).log(Level.SEVERE, null, ex);
                             }
                             break;
                     }
                     if(!success){
                         DEIDGUI.log("Errors during upload. Transfer may not "
                                 + "have completed, see log for details.",
                                 DEIDGUI.LOG_LEVEL.ERROR);
                        DEIDGUI.previous();
                     }
                 }
                 
                 if(TransferPanel.doSave && TransferPanel.tarSaveDir != null){
                     moveTar(TransferPanel.tarSaveDir);
                     
                 }
                     if (!deleteFile(tarFile))
                     { 
                         DEIDGUI.log("Failed to clean tar file ", 
                     DEIDGUI.LOG_LEVEL.ERROR);
                     }
                     if (!deleteFile(tarFileO))
                     { 
                         DEIDGUI.log("Failed to clean tar file ", 
                     DEIDGUI.LOG_LEVEL.ERROR);
                     }
                     
                     File out_dir = new File(DeidData.outputPath+"betOut");
                     if (!deleteFile(out_dir))
                     { 
                         DEIDGUI.log("Failed to clean files in betOut", 
                     DEIDGUI.LOG_LEVEL.ERROR);
                     }
                     else  {
                     DEIDGUI.log("Clean up finished." );
                     }
                 DEIDGUI.advance();
                 
             }
         }).start();
     }
 
     private boolean createLog(){
         boolean success = true;
         String newline = System.getProperty("line.separator");
 
         FileWriter writer = null;
         try {
             writer = new FileWriter(logFile);
             writer.write("name\t"+DeidData.UserFullName + newline);
             writer.write("institution\t" + DeidData.UserInstitution + newline);
             writer.write("sharing\t" + FTPShare + newline);
             writer.write("date\t" + 
                     new SimpleDateFormat("yy-MM-dd-HH:mm:ss").format(new Date()) + 
                     newline);
         } catch (IOException ex) {
             DEIDGUI.log("Could not write log file.", DEIDGUI.LOG_LEVEL.ERROR);
             success = false;
         } finally {
             if(writer != null){
                 try {
                     writer.close();
                 } catch (IOException ex) {}
             }
         }
         
         return success;
     }
     
     private boolean createTar(){
         try {
             TarMaker tm = new TarMaker(tarFile);
             
             for (int ndx = 0; ndx < DeidData.deidentifiedFiles.size(); ndx++) {
                 File curImage = DeidData.deidentifiedFiles.elementAt(ndx);
                 if (DeidData.includeFileInTar[ndx]) {
                     // Add image to the tar
                     String imageName = FileUtils.getName(curImage);
                     if(DeidData.IdFilename.containsKey(imageName)){
                         //tm.addFile(curImage, DeidData.IdTable.get(imageName) + ".nii");
                        // if(!multiImages(DeidData.IdFilename,DeidData.IdFilename.get(imageName)))
                        // tm.addFile(curImage, DeidData.IdTable.get(DeidData.IdFilename.get(imageName)) + ".nii");
                         //else 
                        // {                          
         
                         tm.addFile(curImage, DeidData.multinameSolFile.get(imageName).toString());
                         
                         //}  
                     } else {
                         DEIDGUI.log("No randomized ID was created for " + 
                                 curImage.getPath() + ", it may not be "
                                 + "deidentified", DEIDGUI.LOG_LEVEL.WARNING);
                         tm.addFile(curImage);
                     }
                     // Include header variables file for valid DICOM source files
                     // Header files were created using randomized names, so no 
                     // need to rename here.
                     if (DeidData.NiftiConversionSourceTable.containsKey(curImage)) {
                         tm.addFile(DeidData.ConvertedDicomHeaderTable.get(curImage));
                     }
                 }
             }
             tm.addFile(DeidData.deidentifiedDemoFile);
             tm.addFile(logFile);
             
 
             DEIDGUI.log("Successfully created " + tarFile.getAbsolutePath());
             tm.close();
             String filetogz = tarFile.getAbsolutePath().toString();
             if (gzipfile(filetogz, filetogz+".gz")) 
             {
                 tarFileO = tarFile; 
                 tarFile = new File(filetogz+".gz");
             }
         } catch (IOException ex) {
             DEIDGUI.log("Error creating tarball, file may be damaged or "
                     + "incomplete", DEIDGUI.LOG_LEVEL.WARNING);
             return false;
         }
         
         return true;
     }
     private boolean multiImages(Hashtable<String, String> ht, String val ){
     boolean ismulti = false;
     int count = 0;
     for (Iterator it = ht.keySet().iterator(); it.hasNext(); ) {
     String key = (String) it.next();
     String value = ht.get(key);
     if (value.equals(val)) count++;   
     
     }
     if (count > 1) ismulti = true;
     return ismulti;
     }
  private boolean gzipfile(String gzfile, String desfile)
  {
      boolean success = true;
      try {
       //File oldfile = new File(gzfile);
       //System.out.println(" Given file name is  : " + oldfile);
       FileInputStream finStream = new FileInputStream(gzfile);
       BufferedInputStream bufinStream = new BufferedInputStream(finStream);
       FileOutputStream outStream = new FileOutputStream(desfile);
       GZIPOutputStream goutStream = new GZIPOutputStream(outStream);  
       byte[] buf = new byte[1024];
       int i;
       while ((i = bufinStream.read(buf)) >= 0) {
         goutStream.write(buf, 0, i);
         }
       //System.out.println("Created  GZIP file is " + oldfile + ".gz");
       //System.out.println("GZIP File successfully created");
       bufinStream.close();
       goutStream.close();
       }catch (IOException e) {
         System.out.println("Exception is" + e.getMessage());
           success = false;
         }
      
      return success;
  
  }   
     private boolean uploadTarFTPS() throws IllegalStateException, IOException, FTPIllegalReplyException, FTPException{
         jLabel2.setText("<html><p>Connecting to "+ FTPServer + "...</p><p>&nbsp;</p></html>");
 
         boolean success = true;
         // The manual for ftp4j is here: http://www.sauronsoftware.it/projects/ftp4j/manual.php
         FTPClient client = new FTPClient();
         
         client.setSecurity(FTPClient.SECURITY_FTP);
         if(remotePath.length()>0)
             client.changeDirectory(remotePath);
         try {
             DEIDGUI.log("Connecting to " + FTPServer + " via FTPS");
             if (FTPPort >= 0) {
                 client.connect(FTPServer, FTPPort);
             } else {
                 client.connect(FTPServer);
             }
             DEIDGUI.log("Connected");
 
             // Anonymous authentication, if admitted by the connected service, 
             // can be done sending the username "anonymous" and an arbitrary 
             // password (note that some servers require an e-mail address 
             // in place of the password):
             char[] pass = FTPPassField.getPassword();
             client.login(FTPUser, (pass.length > 0 ? String.copyValueOf(pass) : null));
             for(int ndx = 0; ndx < pass.length; ndx++){
                 pass[ndx] = '\0';
             }
             DEIDGUI.log("Logged in as " + FTPUser);
 
             // Upload the result tarball
             try {
                 final long fileSize = tarFile.length();
                 client.upload(tarFile, new FTPDataTransferListener() {
                     //<editor-fold defaultstate="collapsed" desc="FTPS Upload Listener">
                     class BitrateData{
                         private Date timestamp;
                         private long size;
                         
                         public BitrateData(long size){
                             this.size = size;
                             timestamp = new Date();
                         }
                         public long getTime(){ return timestamp.getTime();}
                         public long getSize(){ return size;}
                     }
                     
                     private BitrateData point;
                     private long transferredSize;
 
                     @Override
                     public void started() {
                         transferredSize = 0L;
                         point = new BitrateData(transferredSize);
                         jLabel2.setText("<html><p>Transferring data...</p><p>&nbsp;</p></html>");
                         DEIDGUI.log("Started upload");
                     }
 
                     String speedStr = "0";
                     @Override
                     public void transferred(int i) {
                         transferredSize += i;
 
                         Date now = new Date();
                         long msSinceLastPoint = now.getTime() - point.getTime();
                         if(msSinceLastPoint > 1000){
                             BitrateData newPoint = new BitrateData(transferredSize);
                             
                             // Make an accurate estimation of bitrate
                             float sizeScale = (float)msSinceLastPoint / 1000;
                             long sizeDiff = (long)((newPoint.size - point.size) * sizeScale);
                             speedStr = FileUtils.sizeToString(sizeDiff);
                             jLabelSpeed.setText(speedStr + "/s");
                             point = newPoint;
                         }
                         
                         long percent = transferredSize * 100 / fileSize;
                         jProgressTransfer.setValue((int)percent);
                     }
 
                     @Override
                     public void completed() {
                         jLabelSpeed.setVisible(false);
                         jLabel2.setText("<html><p>Transfer complete</p><p>&nbsp;</p></html>");
                         DEIDGUI.log("Upload completed successfully");
                     }
 
                     @Override
                     public void aborted() {
                         jLabelSpeed.setVisible(false);
                         DEIDGUI.log("FTP upload aborted, " + transferredSize + " bytes sent", DEIDGUI.LOG_LEVEL.WARNING);
                     }
 
                     @Override
                     public void failed() {
                         jLabelSpeed.setVisible(false);
                         DEIDGUI.log("FTP upload failed, " + transferredSize + " bytes sent", DEIDGUI.LOG_LEVEL.WARNING);
                     }
                     // </editor-fold>
                 });
             } catch (FileNotFoundException ex) {
                 DEIDGUI.log("tar file couldn't be uploaded: " + ex.getMessage(), DEIDGUI.LOG_LEVEL.ERROR);
             } catch (FTPDataTransferException ex) {
                 jLabelSpeed.setVisible(false);
                 DEIDGUI.log("Error during file transfer. Upload not completed "
                         + "but connection may persist. " + ex.getMessage(), DEIDGUI.LOG_LEVEL.WARNING);
             } catch (FTPAbortedException ex) {
                 jLabelSpeed.setVisible(false);
                 DEIDGUI.log("FTP upload aborted: " + ex.getMessage(), DEIDGUI.LOG_LEVEL.ERROR);
             }
         } catch (IllegalStateException ex) {
             DEIDGUI.log("FTP client is already connected. This should never, ever happen.", DEIDGUI.LOG_LEVEL.ERROR);
         } catch (IOException ex) { // Can't connect
             DEIDGUI.log("FTP client could not connect: " + ex.getMessage(), DEIDGUI.LOG_LEVEL.ERROR);
             success = false;
         } catch (FTPIllegalReplyException ex) {
             DEIDGUI.log("Illegal FTP response: " + ex.getMessage(), DEIDGUI.LOG_LEVEL.ERROR);
             success = false;
         } catch (FTPException ex) {
             DEIDGUI.log("Server refused FTP connection: " + ex.getMessage(), DEIDGUI.LOG_LEVEL.ERROR);
             success = false;
         } finally {
             try {
                 client.disconnect(true);
             } catch (IllegalStateException ex) { // Not connected
             } catch (IOException ex) { // IO error when sending quit
             } catch (FTPIllegalReplyException ex) { // Duh
             } catch (FTPException ex) { // Server refused quit command
             }
         }
         
         return success;
     }
     
     private boolean uploadTarSFTP(){
         jLabel2.setText("<html><p>Connecting to "+ FTPServer + "...</p><p>&nbsp;</p></html>");
 
         boolean success = true;
 
         JSch jsch = new JSch();
         DEIDGUI.log("Connecting to " + FTPServer + " via SFTP");
         Session s = null;
         try{
             if (FTPPort >= 0) {
                 s = jsch.getSession(FTPUser, FTPServer, FTPPort);
             } else {
                 s = jsch.getSession(FTPUser, FTPServer);
             }
             s.setConfig("StrictHostKeyChecking", "no");
             char[] pass = FTPPassField.getPassword();
             s.setPassword(String.copyValueOf(pass));
             for(int ndx = 0; ndx < pass.length; ndx++){
                 pass[ndx] = '\0';
             }
             s.connect();
 
             ChannelSftp channel = null;
             try {
                 channel = (ChannelSftp) s.openChannel("sftp");
                 channel.connect();
                 DEIDGUI.log("Channel connected as " + FTPUser);
                 try {
                     // Upload the result tarball
                     // If it is necessary to change directory, do so here
                     if(remotePath.length()>0)
                         channel.cd(remotePath);
                     final long fileSize = tarFile.length();
                     channel.put(tarFile.getAbsolutePath(), 
                             tarFile.getName(), 
                             new SftpProgressMonitor() { //<editor-fold defaultstate="collapsed" desc="SFTP Upload Listener">
                                 class BitrateData{
                                     private Date timestamp;
                                     private long size;
 
                                     public BitrateData(long size){
                                         this.size = size;
                                         timestamp = new Date();
                                     }
                                     public long getTime(){ return timestamp.getTime();}
                                     public long getSize(){ return size;}
                                 }
                                 
                                 private BitrateData point;
                                 private long transferredSize;
                                 
                                 @Override
                                 public void init(int i, String string, String string1, long l) {
                                     transferredSize = 0L;
                                     point = new BitrateData(transferredSize);
                                     jLabel2.setText("<html><p>Transferring data...</p><p>&nbsp;</p></html>");
                                     DEIDGUI.log("Started upload");
                                 }
 
                                 String speedStr = "0";
                                 @Override
                                 public boolean count(long l) {
                                     transferredSize += l;
 
                                     Date now = new Date();
                                     long msSinceLastPoint = now.getTime() - point.getTime();
                                     if (msSinceLastPoint > 1000) {
                                         BitrateData newPoint = new BitrateData(transferredSize);
 
                                         // Make an accurate estimation of bitrate
                                         float sizeScale = (float) msSinceLastPoint / 1000;
                                         long sizeDiff = (long) ((newPoint.size - point.size) * sizeScale);
                                         speedStr = FileUtils.sizeToString(sizeDiff);
                                         jLabelSpeed.setText(speedStr + "/s");
                                         point = newPoint;
                                     }
 
                                     long percent = transferredSize * 100 / fileSize;
                                     jProgressTransfer.setValue((int)percent);
 
                                     return true;
                                 }
 
                                 @Override
                                 public void end() {
                                     jLabelSpeed.setVisible(false);
                                     jLabel2.setText("<html><p>Transfer complete</p><p>&nbsp;</p></html>");
                                     DEIDGUI.log("Upload completed successfully");
                                 }
                         //</editor-fold> 
                             }, ChannelSftp.OVERWRITE);
                     DEIDGUI.log("Successfully put " + fileSize + " bytes");
                 } catch (SftpException ex) {
                     jLabelSpeed.setVisible(false);
                     DEIDGUI.log("During SFTP put: " + ex.getMessage(), DEIDGUI.LOG_LEVEL.ERROR);
                 }
             } catch (JSchException ex) {
                 DEIDGUI.log("While opening SFTP channel: " + ex.getMessage(), DEIDGUI.LOG_LEVEL.ERROR);
                 success = false;
             } finally {
                 if(channel != null){
                     channel.exit();
                 }
             }
         } catch (JSchException ex){
             DEIDGUI.log("While initializing SFTP Session: " + ex.getMessage(), DEIDGUI.LOG_LEVEL.ERROR);
             success = false;
         } finally {
             if(s != null){
                 s.disconnect();
             }
         }
         
         return success;
     }
     
     private void moveTar(File saveDir){
         //boolean success = tarFile.renameTo(new File(saveDir.getAbsolutePath() + 
               //  File.separator + tarFile.getName()));
         boolean success = copyFile(tarFile.getAbsolutePath().toString(), saveDir.getAbsolutePath()+ 
                 File.separator + tarFile.getName());
         if(!success){
             DEIDGUI.log("tarball could not be moved to " + saveDir.getAbsolutePath(), 
                     DEIDGUI.LOG_LEVEL.ERROR);
         }
         else {
             DeidData.tarfilesavedpath = saveDir.getAbsolutePath()+ File.separator + tarFile.getName();
         }
       /*  if (success) {
             if (!deleteFile(tarFile)){ DEIDGUI.log("Failed to clean files ", 
                     DEIDGUI.LOG_LEVEL.ERROR);}
         }*/
     }
     private boolean  copyFile(String    oldPath,    String     newPath)    
     { 
         boolean success = true;
         try    
         {    
             int     bytesum     =     0;    
             int     byteread     =     0;    
             File     oldfile     =     new     File(oldPath);    
             if     (oldfile.exists())    
             {        
                 InputStream     inStream     =     new     FileInputStream(oldPath);         
                 FileOutputStream     fs     =     new     FileOutputStream(newPath);    
                 byte[]     buffer     =     new     byte[1444];    
                 int     length;    
                 while     (     (byteread     =     inStream.read(buffer))     !=     -1)    
                 {    
                     bytesum +=  byteread;     //字节数     文件大小    
                     //System.out.println(bytesum);    
                     fs.write(buffer,     0,     byteread);    
                 }    
                 inStream.close(); 
                 
                 }    
                 }    
                 catch     (Exception     e)    
                 {    
                 //System.out.println( "复制单个文件操作出错 ");  
                     success =false;
                 e.printStackTrace();    
                 }  
         return success;
 
     }
     
     private boolean deleteFile(File file)
     {
         boolean success = true;
         try 
         {
             if (!file.exists()) return success;
                 if (file.isFile()) {
                         file.delete();
                 } else {
                         for (File f : file.listFiles()) {
                                 f.delete();
                         }
                         file.delete();
                 } 
         }catch (Exception e){
         
         success = false;
         DEIDGUI.log("Failed to clean files ", 
                     DEIDGUI.LOG_LEVEL.ERROR);
         }
         return success;
     }
     /**
      * This method is called from within the constructor to initialize the form.
      * WARNING: Do NOT modify this code. The content of this method is always
      * regenerated by the Form Editor.
      */
     @SuppressWarnings("unchecked")
     // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
     private void initComponents() {
 
         jLabel2 = new javax.swing.JLabel();
         jProgressTransfer = new javax.swing.JProgressBar();
         jLabelSpeed = new javax.swing.JLabel();
 
         jLabel2.setText("<html><p>Creating DeID dataset for transfer...</p><p>&nbsp;</p></html>");
 
         org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
         this.setLayout(layout);
         layout.setHorizontalGroup(
             layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(layout.createSequentialGroup()
                 .addContainerGap()
                 .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                     .add(jProgressTransfer, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 388, Short.MAX_VALUE)
                     .add(layout.createSequentialGroup()
                         .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                             .add(jLabel2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                             .add(jLabelSpeed))
                         .add(0, 0, Short.MAX_VALUE)))
                 .addContainerGap())
         );
         layout.setVerticalGroup(
             layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(layout.createSequentialGroup()
                 .addContainerGap()
                 .add(jLabel2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                 .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                 .add(jProgressTransfer, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                 .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                 .add(jLabelSpeed)
                 .addContainerGap(230, Short.MAX_VALUE))
         );
     }// </editor-fold>//GEN-END:initComponents
     // Variables declaration - do not modify//GEN-BEGIN:variables
     public javax.swing.JLabel jLabel2;
     private javax.swing.JLabel jLabelSpeed;
     private javax.swing.JProgressBar jProgressTransfer;
     // End of variables declaration//GEN-END:variables
 
     @Override
     public WizardPanel getNextPanel() {
         return new CompletePanel();
     }
 
     @Override
     public WizardPanel getPreviousPanel() {
         return new TransferPanel();
     }
 }
