 package livestreamerJGUI;
 
 import javax.swing.JOptionPane;
 import javax.swing.UIManager;
 import javax.swing.UnsupportedLookAndFeelException;
 import javax.swing.filechooser.FileFilter;
 import javax.swing.filechooser.FileNameExtensionFilter;
 
 public class livestreamerJGUI extends javax.swing.JFrame {
 
     public livestreamerJGUI() {
         initComponents();
     }
 
     @SuppressWarnings("unchecked")
     // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
     private void initComponents() {
 
         fd = new javax.swing.JFileChooser();
         selectGroup = new javax.swing.ButtonGroup();
         bFile = new javax.swing.JButton();
         labelFile = new javax.swing.JLabel();
         rbRecord = new javax.swing.JRadioButton();
         rbWatch = new javax.swing.JRadioButton();
         bGo = new javax.swing.JButton();
         jScrollPane1 = new javax.swing.JScrollPane();
         taOutput = new javax.swing.JTextArea();
         labelOutput = new javax.swing.JLabel();
         jSeparator1 = new javax.swing.JSeparator();
         jSeparator2 = new javax.swing.JSeparator();
         labelStreamUrl = new javax.swing.JLabel();
         bStop = new javax.swing.JButton();
         tfStreamUrl = new javax.swing.JTextField();
         tfQuality = new javax.swing.JTextField();
         labelQuality = new javax.swing.JLabel();
         tfFile = new javax.swing.JTextField();
 
         setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
         setTitle("livestreamerJGUI");
         setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
         setMinimumSize(new java.awt.Dimension(626, 481));
 
         bFile.setText("File");
         bFile.setEnabled(false);
         bFile.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 bFileActionPerformed(evt);
             }
         });
 
         labelFile.setText("File name:");
         labelFile.setEnabled(false);
 
         selectGroup.add(rbRecord);
         rbRecord.setText("Record to file");
         rbRecord.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 rbRecordActionPerformed(evt);
             }
         });
 
         selectGroup.add(rbWatch);
         rbWatch.setSelected(true);
         rbWatch.setText("Watch stream in player (Default: VLC)");
         rbWatch.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 rbWatchActionPerformed(evt);
             }
         });
 
         bGo.setText("GO!");
         bGo.setMaximumSize(new java.awt.Dimension(55, 23));
         bGo.setMinimumSize(new java.awt.Dimension(55, 23));
         bGo.setPreferredSize(new java.awt.Dimension(55, 23));
         bGo.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 bGoActionPerformed(evt);
             }
         });
 
         taOutput.setEditable(false);
         taOutput.setColumns(20);
         taOutput.setLineWrap(true);
         taOutput.setRows(5);
         taOutput.setWrapStyleWord(true);
         jScrollPane1.setViewportView(taOutput);
 
         labelOutput.setText("Output:");
 
         labelStreamUrl.setText("Stream URL:");
 
         bStop.setText("Stop");
         bStop.setEnabled(false);
         bStop.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 bStopActionPerformed(evt);
             }
         });
 
         tfQuality.setToolTipText("Leave empty and ceck output if you are unsure about available bitrates.");
 
         labelQuality.setText("Quality:");
 
         tfFile.setEnabled(false);
 
         javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
         getContentPane().setLayout(layout);
         layout.setHorizontalGroup(
             layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addComponent(jSeparator2)
             .addComponent(jSeparator1, javax.swing.GroupLayout.Alignment.TRAILING)
             .addGroup(layout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addComponent(jScrollPane1)
                     .addGroup(layout.createSequentialGroup()
                         .addComponent(rbWatch)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 344, Short.MAX_VALUE)
                         .addComponent(bGo, javax.swing.GroupLayout.PREFERRED_SIZE, 55, javax.swing.GroupLayout.PREFERRED_SIZE))
                     .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                         .addGap(0, 0, Short.MAX_VALUE)
                         .addComponent(bStop))
                     .addGroup(layout.createSequentialGroup()
                         .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                             .addGroup(layout.createSequentialGroup()
                                 .addComponent(bFile)
                                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                 .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                     .addComponent(labelFile)
                                     .addComponent(tfFile, javax.swing.GroupLayout.PREFERRED_SIZE, 320, javax.swing.GroupLayout.PREFERRED_SIZE)))
                             .addComponent(labelOutput)
                             .addComponent(rbRecord)
                             .addGroup(layout.createSequentialGroup()
                                 .addComponent(labelStreamUrl)
                                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                 .addComponent(tfStreamUrl, javax.swing.GroupLayout.PREFERRED_SIZE, 311, javax.swing.GroupLayout.PREFERRED_SIZE)
                                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                 .addComponent(labelQuality)
                                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                 .addComponent(tfQuality, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)))
                         .addGap(0, 0, Short.MAX_VALUE)))
                 .addContainerGap())
         );
         layout.setVerticalGroup(
             layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(rbWatch)
                     .addComponent(bGo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(bStop)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 11, Short.MAX_VALUE)
                 .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(labelStreamUrl)
                     .addComponent(tfStreamUrl, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(tfQuality, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(labelQuality))
                 .addGap(18, 18, 18)
                 .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                 .addComponent(rbRecord)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(labelFile)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(bFile)
                     .addComponent(tfFile, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addGap(26, 26, 26)
                 .addComponent(jSeparator2, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                 .addComponent(labelOutput)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 211, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addContainerGap())
         );
 
         pack();
     }// </editor-fold>//GEN-END:initComponents
 
     private void toggleRec(){
         if (rbWatch.isEnabled()){
             rbWatch.setEnabled(false);
             rbRecord.setEnabled(false);
             tfStreamUrl.setEnabled(false);
             tfQuality.setEnabled(false);
             bFile.setEnabled(false);
             tfFile.setEnabled(false);
         }
         else{
             rbWatch.setEnabled(true);
             rbRecord.setEnabled(true);
             tfStreamUrl.setEnabled(true);
             tfQuality.setEnabled(true);
             bFile.setEnabled(true);
             tfFile.setEnabled(true);
         } 
     }
     private void toggleWatch(){
         if (rbRecord.isEnabled()){
             rbWatch.setEnabled(false);
             rbRecord.setEnabled(false);
             tfStreamUrl.setEnabled(false);
             tfQuality.setEnabled(false);
             bFile.setEnabled(false);
             tfFile.setEnabled(false);
         }
         else{
             rbWatch.setEnabled(true);
             rbRecord.setEnabled(true);
             tfStreamUrl.setEnabled(true);
             tfQuality.setEnabled(true);
         } 
     }
     
     private String addFileExtIfNecessary(String file,String ext) {
         if(file.lastIndexOf('.') == -1) {
             file = ext;
             return file;
         }
         return "";
     }
     private boolean filterCheck = false;
     private String fileName = "no";
     private void bFileActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bFileActionPerformed
         if (!filterCheck) {
         FileFilter ft = new FileNameExtensionFilter("Transport Streams (*.ts)", "ts");
         fd.setAcceptAllFileFilterUsed(false);
         fd.addChoosableFileFilter( ft );
         filterCheck = true;
         }
         int returnVal = fd.showOpenDialog( this );
         String fileExt = ".ts";
         
         if (returnVal == javax.swing.JFileChooser.APPROVE_OPTION) {
             String file_name = fd.getSelectedFile().getPath() + addFileExtIfNecessary(fd.getSelectedFile().getName(),fileExt);
            tfFile.setText("\"" +file_name +"\"");
            fileName = tfFile.getText();
         }
     }//GEN-LAST:event_bFileActionPerformed
 
     private void rbRecordActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_rbRecordActionPerformed
         bFile.setEnabled(true);
         tfFile.setEnabled(true);
         tfFile.setEnabled(true);
         labelFile.setEnabled(true);
     }//GEN-LAST:event_rbRecordActionPerformed
 
     private void rbWatchActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_rbWatchActionPerformed
         fileName = "no";
         tfFile.setText("");
         bFile.setEnabled(false);
         tfFile.setEnabled(false);
         labelFile.setEnabled(false);
     }//GEN-LAST:event_rbWatchActionPerformed
     
 livestreamerExe le = new livestreamerExe();
 
     private void bGoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bGoActionPerformed
         
         bGo.setEnabled(false);
         taOutput.setText("");
         if (rbWatch.isSelected()) {
             String[] cl = { "livestreamer", tfStreamUrl.getText(), tfQuality.getText()};
             le.runLivestreamer(cl, taOutput);
             bStop.setEnabled(true);
             toggleWatch();
             new Thread() {
                 public void run() {
                     try{
                         le.getProc().waitFor();
                     }
                     catch (Exception err){
                     }
                     bGo.setEnabled(true);
                     bStop.setEnabled(false);
                     toggleWatch();
                 }
             }.start();
             le.nullProc();
         }
         if (rbRecord.isSelected() && !"no".equals(fileName)) {
             String[] cl = { "livestreamer", tfStreamUrl.getText(), tfQuality.getText(), "-o", tfFile.getText()};
             le.runLivestreamer(cl, taOutput);
             bStop.setEnabled(true);
             toggleRec();
             new Thread() {
                 public void run() {
                     try{
                         le.getProc().waitFor();
                     }
                     catch (Exception err){
                     }
                     bGo.setEnabled(true);
                     bStop.setEnabled(false);
                     toggleRec();
                 }
             }.start();
             le.nullProc();
         }
         if (rbRecord.isSelected() && "no".equals(fileName)) {
             JOptionPane.showMessageDialog(this,"Please enter a file name");
             bGo.setEnabled(true);
             bStop.setEnabled(false);
         }
     }//GEN-LAST:event_bGoActionPerformed
 
     private void bStopActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bStopActionPerformed
         le.killLivestreamer();
         bGo.setEnabled(true);
         bStop.setEnabled(false);
     }//GEN-LAST:event_bStopActionPerformed
 
     public static void main(String args[]) {
         java.awt.EventQueue.invokeLater(new Runnable() {
             public void run() {
                 try {
                     UIManager.setLookAndFeel(
                             UIManager.getSystemLookAndFeelClassName());
                 } 
                 catch (UnsupportedLookAndFeelException | ClassNotFoundException | InstantiationException | IllegalAccessException e) {
                 }
                 new livestreamerJGUI().setVisible(true);
             }
         });
     }
     // Variables declaration - do not modify//GEN-BEGIN:variables
     private javax.swing.JButton bFile;
     private javax.swing.JButton bGo;
     private javax.swing.JButton bStop;
     private javax.swing.JFileChooser fd;
     private javax.swing.JScrollPane jScrollPane1;
     private javax.swing.JSeparator jSeparator1;
     private javax.swing.JSeparator jSeparator2;
     private javax.swing.JLabel labelFile;
     private javax.swing.JLabel labelOutput;
     private javax.swing.JLabel labelQuality;
     private javax.swing.JLabel labelStreamUrl;
     private javax.swing.JRadioButton rbRecord;
     private javax.swing.JRadioButton rbWatch;
     private javax.swing.ButtonGroup selectGroup;
     private javax.swing.JTextArea taOutput;
     private javax.swing.JTextField tfFile;
     private javax.swing.JTextField tfQuality;
     private javax.swing.JTextField tfStreamUrl;
     // End of variables declaration//GEN-END:variables
 }
