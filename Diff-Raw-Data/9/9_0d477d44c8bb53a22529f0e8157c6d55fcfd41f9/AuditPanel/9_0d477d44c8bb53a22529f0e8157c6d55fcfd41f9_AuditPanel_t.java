 package dit.panels;
 
 import dit.AuditJTable;
 import dit.DEIDGUI;
 import dit.DeidData;
 import dit.DemographicTableModel;
 import dit.FileUtils;
 import dit.NiftiDisplayPanel;
 import dit.OpenImagewithMRIcron;
 import dit.TextviewFrame;
 import dit.ReSkullStrippingFrame;
 import java.awt.Desktop;
 import java.io.File;
 import java.io.IOException;
 import javax.imageio.ImageIO;
 import javax.swing.ImageIcon;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.WindowConstants;
 import javax.swing.event.ListSelectionEvent;
 import javax.swing.event.ListSelectionListener;
 import javax.swing.table.AbstractTableModel;
 import java.util.Hashtable;
 import java.util.Iterator;
 
 
 /**
  *
  * @author christianprescott
  */
 public class AuditPanel extends javax.swing.JPanel implements WizardPanel {
     
     /**
      * Creates new form AuditPanel
      */
     private ReSkullStrippingFrame redo;
     public AuditPanel() {
         initComponents();
         // if(DeidData.demographicData != DemographicTableModel.dummyModel)
         createFakenames();
         jButtonViewMontage.setVisible(false);
         DEIDGUI.title = "Auditing";
         DEIDGUI.helpButton.setEnabled(true);
         if(DeidData.includeFileInTar == null){
             DeidData.includeFileInTar = new Boolean[DeidData.deidentifiedFiles.size()];
             // Select all images by default
             for (int ndx = 0; ndx < DeidData.includeFileInTar.length; ndx++) {
                 DeidData.includeFileInTar[ndx] = true;
             }
         }
         
         // Define the AuditJTable model
         jTableImages.setModel(new AbstractTableModel() {
             // <editor-fold defaultstate="collapsed" desc="AuditTableModel">
             
             private String[] columnNames = new String[]{"Selected", "Image"};
             
             @Override
             public int getRowCount() {
                 return DeidData.deidentifiedFiles.size();
             }
             
             @Override
             public int getColumnCount() {
                 return columnNames.length;
             }
             
             @Override
             public String getColumnName(int i) {
                 return columnNames[i];
             }
             
             @Override
             public Class getColumnClass(int i) {
                 Class colClass;
                 switch (i) {
                     case 0:
                         colClass = Boolean.class;
                         break;
                     case 1:
                         colClass = File.class;
                         break;
                     default:
                         colClass = Object.class;
                 }
                 return colClass;
             }
             
             @Override
             public boolean isCellEditable(int row, int col) {
                 return (col == 0 ? true : false);
             }
             
             @Override
             public Object getValueAt(int row, int col) {
                 Object value;
                 switch (col) {
                     case 0:
                         value = DeidData.includeFileInTar[row];
                         break;
                     case 1:
                         value = DeidData.deidentifiedFiles.get(row);
                         break;
                     default:
                         value = "Error";
                         break;
                 }
                 return value;
             }
             
             @Override
             public void setValueAt(Object o, int row, int col) {
                 if (col == 0) {
                     DeidData.includeFileInTar[row] = (Boolean) o;
                 }
             }
             // </editor-fold>
         });
         // Add a selection listener for enabling/disabling the "View Header" button
         jTableImages.getSelectionModel().addListSelectionListener(
                 new ListSelectionListener() {
                     // <editor-fold defaultstate="collapsed" desc="AuditTableSelectionListener">
                     
                     @Override
                     public void valueChanged(ListSelectionEvent lse) {
                         if (lse.getValueIsAdjusting()) {
                             File selectedFile = DeidData.deidentifiedFiles.get(jTableImages.getSelectedRow());
                             jButtonViewHeader.setEnabled(
                                     DeidData.ConvertedDicomHeaderTable.containsKey(selectedFile)
                                     ? true : false);
                             ((NiftiDisplayPanel)jPanel1).setImage(selectedFile);
                         }
                     }
                     // </editor-fold>
                 });
         
         DEIDGUI.log("AuditPanel initialized");
     }
     
     /**
      * This method is called from within the constructor to initialize the form.
      * WARNING: Do NOT modify this code. The content of this method is always
      * regenerated by the Form Editor.
      */
     @SuppressWarnings("unchecked")
     // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
     private void initComponents() {
 
         jLabel1 = new javax.swing.JLabel();
         jButtonViewDemo = new javax.swing.JButton();
         jButtonViewImage = new javax.swing.JButton();
         jScrollPane1 = new javax.swing.JScrollPane();
         jTableImages = new AuditJTable();
         jButtonViewHeader = new javax.swing.JButton();
         jPanel1 = new NiftiDisplayPanel();
         jSliderSlice = new javax.swing.JSlider();
         jButtonViewMontage = new javax.swing.JButton();
         jButton1 = new javax.swing.JButton();
 
         jLabel1.setText("<html><p>Ensure the data has been de-identified. Deselect images that have not had identifying information properly removed or will not be transferred.</p><p>&nbsp;</p></html>");
 
         jButtonViewDemo.setText("View data file");
         jButtonViewDemo.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 jButtonViewDemoActionPerformed(evt);
             }
         });
 
         jButtonViewImage.setText("View image");
         jButtonViewImage.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 jButtonViewImageActionPerformed(evt);
             }
         });
 
         jTableImages.setModel(new javax.swing.table.DefaultTableModel(
             new Object [][] {
                 { new Boolean(true), "File1"},
                 {null, "File2"},
                 { new Boolean(true), "File3"},
                 {null, "File4"}
             },
             new String [] {
                 "Selected", "File"
             }
         ) {
             Class[] types = new Class [] {
                 java.lang.Boolean.class, java.lang.Object.class
             };
             boolean[] canEdit = new boolean [] {
                 true, false
             };
 
             public Class getColumnClass(int columnIndex) {
                 return types [columnIndex];
             }
 
             public boolean isCellEditable(int rowIndex, int columnIndex) {
                 return canEdit [columnIndex];
             }
         });
         jScrollPane1.setViewportView(jTableImages);
 
         jButtonViewHeader.setText("View DICOM header");
         jButtonViewHeader.setEnabled(false);
         jButtonViewHeader.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 jButtonViewHeaderActionPerformed(evt);
             }
         });
 
         jPanel1.setMinimumSize(new java.awt.Dimension(0, 32));
 
         org.jdesktop.layout.GroupLayout jPanel1Layout = new org.jdesktop.layout.GroupLayout(jPanel1);
         jPanel1.setLayout(jPanel1Layout);
         jPanel1Layout.setHorizontalGroup(
             jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(0, 0, Short.MAX_VALUE)
         );
         jPanel1Layout.setVerticalGroup(
             jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(0, 32, Short.MAX_VALUE)
         );
 
         jSliderSlice.addChangeListener(new javax.swing.event.ChangeListener() {
             public void stateChanged(javax.swing.event.ChangeEvent evt) {
                 jSliderSliceStateChanged(evt);
             }
         });
 
         jButtonViewMontage.setText("View image montage");
         jButtonViewMontage.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 jButtonViewMontageActionPerformed(evt);
             }
         });
 
         jButton1.setText("Redo skull-stripping");
         jButton1.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 jButton1ActionPerformed(evt);
             }
         });
 
         org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
         this.setLayout(layout);
         layout.setHorizontalGroup(
             layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(layout.createSequentialGroup()
                 .addContainerGap()
                 .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                     .add(jLabel1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                     .add(layout.createSequentialGroup()
                         .add(jButtonViewDemo)
                         .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                         .add(jButtonViewMontage)
                         .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                         .add(jButton1)
                         .add(0, 0, Short.MAX_VALUE))
                     .add(layout.createSequentialGroup()
                         .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 235, Short.MAX_VALUE)
                         .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                         .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                             .add(layout.createSequentialGroup()
                                 .add(jButtonViewImage)
                                 .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                 .add(jButtonViewHeader))
                             .add(jPanel1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                             .add(jSliderSlice, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                         .addContainerGap())))
         );
         layout.setVerticalGroup(
             layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(layout.createSequentialGroup()
                 .addContainerGap()
                 .add(jLabel1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                 .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                 .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                     .add(jButtonViewDemo)
                     .add(jButtonViewMontage)
                     .add(jButton1))
                 .add(18, 18, 18)
                 .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                     .add(jScrollPane1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                     .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                         .add(jPanel1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                         .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                         .add(jSliderSlice, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                         .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                         .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                             .add(jButtonViewImage)
                             .add(jButtonViewHeader))))
                 .addContainerGap())
         );
     }// </editor-fold>//GEN-END:initComponents
     
     private boolean OpenFile(File file) {
         boolean openSucceeded;
         if (Desktop.isDesktopSupported()) {
             try {
                 Desktop.getDesktop().open(file);
                 openSucceeded = true;
             } catch (IOException ex) {
                 DEIDGUI.log("The system couldn't open " + file.toString() + ": "
                         + ex.getMessage(), DEIDGUI.LOG_LEVEL.ERROR);
                 openSucceeded = false;
             } catch (IllegalArgumentException e) {
                 DEIDGUI.log("The system couldn't open " + file.toString() + ": "
                         + e.getMessage(), DEIDGUI.LOG_LEVEL.ERROR);
                 openSucceeded = false;
             }
         } else {
             DEIDGUI.log("Unable to open file, desktop operations not supported", DEIDGUI.LOG_LEVEL.ERROR);
             openSucceeded = false;
         }
         return openSucceeded;
     }
     
     private void createFakenames(){
         Iterator curFile = DeidData.deidentifiedFiles.iterator();
         while (curFile.hasNext()){
             String x = FileUtils.getName((File)curFile.next());
             String y="";
             System.out.println("Get:"+x);
            if(DeidData.isNoData)
                 y=DeidData.IdTable.get(x);
             else
                 y = DeidData.IdTable.get(DeidData.IdFilename.get(x));
             /*  if(!multiImages(DeidData.IdFilename,y)){
              * DeidData.multinameSolFile.put(x, y + ".nii");
              * }
              * else {   */
             System.out.println("y"+y);
             if (!DeidData.multinameSol.containsKey(y)){
                 DeidData.multinameSol.put(y,1);
                 DeidData.multinameSolFile.put(x, y + "_1"+".nii");
             }
             else
             {
                 int value =  DeidData.multinameSol.get(y);
                 value = value + 1;
                 DeidData.multinameSolFile.put(x,y +"_"+ value+".nii");
                 DeidData.multinameSol.remove(y);
                 DeidData.multinameSol.put(y, value);
             }
             
             //}
         }
         
         
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
     
     private void jButtonViewImageActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonViewImageActionPerformed
         /* if(jTableImages.getSelectedRow() >= 0){
          * File selectedFile = (File) DeidData.deidentifiedFiles.get(jTableImages.getSelectedRow());
          * OpenFile(selectedFile);
          * }*/
         if(jTableImages.getSelectedRow() >= 0){
             File selectedFile = (File) DeidData.deidentifiedFiles.get(jTableImages.getSelectedRow());
             OpenImagewithMRIcron openImage = new OpenImagewithMRIcron(selectedFile);
             openImage.run();
         }
     }//GEN-LAST:event_jButtonViewImageActionPerformed
     
     private void jButtonViewDemoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonViewDemoActionPerformed
         // OpenFile(DeidData.deidentifiedDemoFile);
         TextviewFrame viewtext = new TextviewFrame(DeidData.deidentifiedDemoFile);
         //try {
         viewtext.pack();
         viewtext.setVisible(true);
         //} catch(IOException ex)
         //{
         //      DEIDGUI.log("Unable to open demographic file.", DEIDGUI.LOG_LEVEL.ERROR);
         //}
         
     }//GEN-LAST:event_jButtonViewDemoActionPerformed
     
     private void jButtonViewHeaderActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonViewHeaderActionPerformed
         // Open matching header var file
         if(jTableImages.getSelectedRow() >= 0){
             File selectedFile = (File) DeidData.deidentifiedFiles.get(jTableImages.getSelectedRow());
             if (DeidData.ConvertedDicomHeaderTable.containsKey(selectedFile)) {
                 // OpenFile(DeidData.ConvertedDicomHeaderTable.get(selectedFile));
                 TextviewFrame viewtext = new TextviewFrame(DeidData.ConvertedDicomHeaderTable.get(selectedFile));
                 viewtext.pack();
                 viewtext.setVisible(true);
             }
         }
     }//GEN-LAST:event_jButtonViewHeaderActionPerformed
     
     private void jSliderSliceStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_jSliderSliceStateChanged
         if(jTableImages.getSelectedRow() >= 0){
             File selectedFile = (File) DeidData.deidentifiedFiles.get(jTableImages.getSelectedRow());
             ((NiftiDisplayPanel)jPanel1).setSlice((float)jSliderSlice.getValue()/100f);
         }
     }//GEN-LAST:event_jSliderSliceStateChanged
     
     private void jButtonViewMontageActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonViewMontageActionPerformed
         File montageFile = new File(DeidData.outputPath + "montage.jpg");
         if(montageFile.exists()){
             // Programatically create a JFrame to view the image
             // TODO: place image within a scrollviewer?
             JFrame imageFrame = new JFrame("DeID image montage");
             imageFrame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
             imageFrame.setResizable(false);
             JLabel imageLabel;
             try {
                 imageLabel = new JLabel(new ImageIcon(ImageIO.read(montageFile)));
                 imageFrame.add(imageLabel);
                 imageFrame.pack();
                 imageFrame.setVisible(true);
             } catch (IOException ex) {
                 DEIDGUI.log("Unable to read image montage", DEIDGUI.LOG_LEVEL.ERROR);
             }
         } else {
             DEIDGUI.log("No montage image was created", DEIDGUI.LOG_LEVEL.ERROR);
         }
     }//GEN-LAST:event_jButtonViewMontageActionPerformed
     
     private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
         // TODO add your handling code here:
         redo = new ReSkullStrippingFrame();
         redo.pack();
         redo.setVisible(true);
     }//GEN-LAST:event_jButton1ActionPerformed
     
     // Variables declaration - do not modify//GEN-BEGIN:variables
     private javax.swing.JButton jButton1;
     private javax.swing.JButton jButtonViewDemo;
     private javax.swing.JButton jButtonViewHeader;
     private javax.swing.JButton jButtonViewImage;
     private javax.swing.JButton jButtonViewMontage;
     private javax.swing.JLabel jLabel1;
     private javax.swing.JPanel jPanel1;
     private javax.swing.JScrollPane jScrollPane1;
     private javax.swing.JSlider jSliderSlice;
     private javax.swing.JTable jTableImages;
     // End of variables declaration//GEN-END:variables
     
     @Override
     public WizardPanel getNextPanel() {
         try{
             redo.setVisible(false);
         }catch (Exception e){
             
             return new TransferPanel();
             
         }
         return new TransferPanel();
     }
     
     @Override
     public WizardPanel getPreviousPanel() {
         DeidData.includeFileInTar = null;
         return new DeIdentifyPanel();
     }
 }
