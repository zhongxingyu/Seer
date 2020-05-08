 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.turkoid.imagetweaker;
 
 import java.awt.Dimension;
 import java.awt.Toolkit;
 import java.awt.image.BufferedImage;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import javax.swing.JFileChooser;
 import javax.swing.SwingUtilities;
 import javax.swing.filechooser.FileFilter;
 
 /**
  *
  * @author turkoid
  */
 public class GifTweaker extends javax.swing.JFrame {
     private final JFileChooser fc;
     private final GifFilter filter;
     private final String slash;
 
     private File src;
     private File dest;
 
     public GifTweaker() {
         initComponents();
 
         fc = new JFileChooser();
         filter = new GifFilter();
         slash = System.getProperty("file.separator");
         fc.setAcceptAllFileFilterUsed(false);
         /*
         txtSrc.getDocument().addDocumentListener(new DocumentListener() {
             public void changedUpdate(DocumentEvent evt) {
                 String ext = Utils.getExtension(txtSrc.getText());
                 btnTweakIt.setEnabled(ext != null && ext.equals("*.gif"));
             }
 
             public void removeUpdate(DocumentEvent evt) {
                 String ext = Utils.getExtension(txtSrc.getText());
                 btnTweakIt.setEnabled(ext != null && ext.equals("*.gif"));
             }
 
             public void insertUpdate(DocumentEvent evt) {
                 String ext = Utils.getExtension(txtSrc.getText());
                 btnTweakIt.setEnabled(ext != null && ext.equals("*.gif"));
             }
         });
         txtDest.getDocument().addDocumentListener(new DocumentListener() {
             public void changedUpdate(DocumentEvent evt) {
                 btnTweakIt.setEnabled(!txtDest.getText().trim().isEmpty());
             }
 
             public void removeUpdate(DocumentEvent evt) {
                 btnTweakIt.setEnabled(!txtDest.getText().trim().isEmpty());
             }
 
             public void insertUpdate(DocumentEvent evt) {
                 btnTweakIt.setEnabled(!txtDest.getText().trim().isEmpty());
             }
         });
         */
     }
 
     private static class GifFilter extends FileFilter {
         public boolean accept(File f) {
             if (f.isDirectory()) return true;
             String ext = Utils.getExtension(f);
             return ext != null && ext.equals("gif");
         }
 
         public String getDescription() {
             return "Gif Files (*.gif)";
         }
     }
 
     private static class Utils {
         public static String getExtension(File f) {
             return f == null ? null : getExtension(f.getName());
         }
 
         public static String getExtension(String s) {
             String ext = null;
             if (s != null) {
                 int i = s.lastIndexOf('.');
 
                 if (i > 0 &&  i < s.length() - 1) {
                     ext = s.substring(i+1).toLowerCase();
                 }
             }
             return ext;
         }
 
         public static String getFileName(File f) {
             return f == null ? null : getFileName(f.getName());
         }
 
         public static String getFileName(String s) {
             String filename = null;
             if (s != null) {
                 int i = s.lastIndexOf('.');
                 if (i > 0) filename = s.substring(0, i);
             }
             return filename;
         }
 
     }
 
     private void setStatus(String status) {
         lblStatus.setText(status);
     }
 
     private boolean TweakIt() {
         setStatus("Tweaking...");
         btnTweakIt.setEnabled(false);
         btnSrc.setEnabled(false);
         ctlRows.setEnabled(false);
         ctlColumns.setEnabled(false);
         SwingUtilities.invokeLater(new Runnable() {
             public void run() {
                 int rows = (Integer) ctlRows.getValue();
                 int cols = (Integer) ctlColumns.getValue();
                 int partialCount = rows * cols;
 
                 GifDecoder decoder = new GifDecoder();
                 AnimatedGifEncoder encoder = new AnimatedGifEncoder();
 
 
                 decoder.read(src.getPath());
                 int frameCount = decoder.getFrameCount();
                 BufferedImage[][] subFrames = new BufferedImage[frameCount][partialCount];
                 Dimension size = decoder.getFrameSize();
                 int partialW = size.width / cols;
                 int partialH = size.height / rows;
 
                 boolean fractionW = cols * partialW < size.width;
                 boolean fractionH = rows * partialH < size.height;
 
                 for (int i = 0; i < frameCount; i++) {
                     BufferedImage frame = decoder.getFrame(i);
                     BufferedImage[] partials = new BufferedImage[partialCount];
 
                     for (int r = 0; r < rows; r++) {
                         for (int c = 0; c < cols; c++) {
                             int x = c * partialW;
                             int y = r * partialH;
                             int w = partialW;
                             int h = partialH;
                             if (fractionW && c + 1 == cols) w++;
                             if (fractionH && r + 1 == rows) h++;
                             //System.out.println("[" + i + "]=(" + x + ", " + y + ", " + w + ", " + h + ")");
                             BufferedImage partial = frame.getSubimage(x, y, w, h);
                             subFrames[i][r * cols + c] = partial;
                         }
                     }
                 }
 
                 dest.mkdir();
                 for (File file : dest.listFiles()) {
                     file.delete();
                 }
 
                 String baseFileName = dest.getPath() + slash + Utils.getFileName(src);
                 File html = new File(baseFileName + ".html");
                 baseFileName += "_";
                 for (int i = 0; i < partialCount; i++) {
                     String filename = baseFileName + i + ".gif";
                     System.out.println("fn=" + filename);
                     encoder.start(filename);
                     encoder.setRepeat(0);
                     for (int j = 0; j < frameCount; j++) {
                        int delay = decoder.getDelay(j);
                        encoder.setDelay(delay == 0 ? 10 : delay);
                         encoder.addFrame(subFrames[j][i]);
                     }
                     encoder.finish();
                 }
                 try {
                     String partialFileName = Utils.getFileName(src) + "_";
                     if (!html.exists()) html.createNewFile();;
                     FileWriter fw = new FileWriter(html.getAbsoluteFile());
                     BufferedWriter bw = new BufferedWriter(fw);
                     bw.write("<html><body><table style=\" border-spacing: 0px;\">");
                     bw.newLine();
                     for (int r = 0; r < rows; r++) {
                         bw.write("<tr>");
                         for (int c = 0; c < cols; c++) {
                             bw.write("<td><img src=\"" + partialFileName + (r * cols + c) + ".gif\"></td>");
                         }
                         bw.write("</tr>");
                         bw.newLine();
                     }
                     bw.write("</table></body></html>");
                     bw.close();
                 } catch (IOException e) {
                     System.out.println("Error creating html file");
                 }
                 setStatus("Finished");
                 btnTweakIt.setEnabled(true);
                 btnSrc.setEnabled(true);
                 ctlRows.setEnabled(true);
                 ctlColumns.setEnabled(true);
             }
         });
 
         return true;
     }
 
     @SuppressWarnings("unchecked")
     // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
     private void initComponents() {
         java.awt.GridBagConstraints gridBagConstraints;
 
         panWrapper = new javax.swing.JPanel();
         btnTweakIt = new javax.swing.JButton();
         panCenter = new javax.swing.JPanel();
         lblSrcLabel = new javax.swing.JLabel();
         txtSrc = new javax.swing.JTextField();
         btnSrc = new javax.swing.JButton();
         lblDestLabel = new javax.swing.JLabel();
         txtDest = new javax.swing.JTextField();
         btnDest = new javax.swing.JButton();
         lblStatus = new javax.swing.JLabel();
         panDividePanel = new javax.swing.JPanel();
         lblRowsLabel = new javax.swing.JLabel();
         ctlRows = new javax.swing.JSpinner();
         lblColumnsLabel = new javax.swing.JLabel();
         ctlColumns = new javax.swing.JSpinner();
         jLabel1 = new javax.swing.JLabel();
 
         setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
         setTitle("Gif Tweaker 0.2b");
         setResizable(false);
 
         panWrapper.setBorder(javax.swing.BorderFactory.createEmptyBorder(5, 5, 5, 5));
         panWrapper.setPreferredSize(new java.awt.Dimension(700, 200));
         panWrapper.setLayout(new java.awt.BorderLayout());
 
         btnTweakIt.setText("Tweak It!");
         btnTweakIt.setMargin(new java.awt.Insets(0, 0, 0, 0));
         btnTweakIt.setPreferredSize(new java.awt.Dimension(51, 50));
         btnTweakIt.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 btnTweakItActionPerformed(evt);
             }
         });
         panWrapper.add(btnTweakIt, java.awt.BorderLayout.PAGE_END);
 
         panCenter.setLayout(new java.awt.GridBagLayout());
 
         lblSrcLabel.setText("Source");
         lblSrcLabel.setPreferredSize(new java.awt.Dimension(0, 0));
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.fill = java.awt.GridBagConstraints.VERTICAL;
         gridBagConstraints.ipadx = 70;
         gridBagConstraints.weighty = 1.0;
         gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
         panCenter.add(lblSrcLabel, gridBagConstraints);
 
         txtSrc.setEditable(false);
         txtSrc.setPreferredSize(new java.awt.Dimension(0, 0));
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
         gridBagConstraints.weightx = 1.0;
         gridBagConstraints.weighty = 1.0;
         gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
         panCenter.add(txtSrc, gridBagConstraints);
 
         btnSrc.setText("...");
         btnSrc.setPreferredSize(new java.awt.Dimension(0, 0));
         btnSrc.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 btnSrcActionPerformed(evt);
             }
         });
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.fill = java.awt.GridBagConstraints.VERTICAL;
         gridBagConstraints.ipadx = 50;
         gridBagConstraints.weighty = 1.0;
         gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
         panCenter.add(btnSrc, gridBagConstraints);
 
         lblDestLabel.setText("Destination");
         lblDestLabel.setPreferredSize(new java.awt.Dimension(0, 0));
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridy = 1;
         gridBagConstraints.fill = java.awt.GridBagConstraints.VERTICAL;
         gridBagConstraints.ipadx = 70;
         gridBagConstraints.weighty = 1.0;
         gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
         panCenter.add(lblDestLabel, gridBagConstraints);
 
         txtDest.setEditable(false);
         txtDest.setPreferredSize(new java.awt.Dimension(0, 0));
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridy = 1;
         gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
         gridBagConstraints.weightx = 1.0;
         gridBagConstraints.weighty = 1.0;
         gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
         panCenter.add(txtDest, gridBagConstraints);
 
         btnDest.setText("...");
         btnDest.setEnabled(false);
         btnDest.setPreferredSize(new java.awt.Dimension(0, 0));
         btnDest.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 btnDestActionPerformed(evt);
             }
         });
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridy = 1;
         gridBagConstraints.fill = java.awt.GridBagConstraints.VERTICAL;
         gridBagConstraints.ipadx = 50;
         gridBagConstraints.weighty = 1.0;
         gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
         panCenter.add(btnDest, gridBagConstraints);
 
         lblStatus.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
         lblStatus.setText("Status");
         lblStatus.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
         lblStatus.setPreferredSize(new java.awt.Dimension(0, 0));
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridy = 3;
         gridBagConstraints.gridwidth = 3;
         gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
         gridBagConstraints.weightx = 1.0;
         gridBagConstraints.weighty = 1.0;
         gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
         panCenter.add(lblStatus, gridBagConstraints);
 
         panDividePanel.setPreferredSize(new java.awt.Dimension(0, 0));
         panDividePanel.setLayout(new java.awt.GridBagLayout());
 
         lblRowsLabel.setText("Rows");
         lblRowsLabel.setPreferredSize(new java.awt.Dimension(0, 0));
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.fill = java.awt.GridBagConstraints.VERTICAL;
         gridBagConstraints.ipadx = 50;
         gridBagConstraints.weighty = 1.0;
         panDividePanel.add(lblRowsLabel, gridBagConstraints);
 
         ctlRows.setModel(new javax.swing.SpinnerNumberModel(2, 2, 10, 1));
         ctlRows.setPreferredSize(new java.awt.Dimension(0, 0));
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.fill = java.awt.GridBagConstraints.VERTICAL;
         gridBagConstraints.ipadx = 50;
         gridBagConstraints.weighty = 1.0;
         panDividePanel.add(ctlRows, gridBagConstraints);
 
         lblColumnsLabel.setText("Cols");
         lblColumnsLabel.setPreferredSize(new java.awt.Dimension(0, 0));
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.fill = java.awt.GridBagConstraints.VERTICAL;
         gridBagConstraints.ipadx = 50;
         gridBagConstraints.weighty = 1.0;
         gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 0);
         panDividePanel.add(lblColumnsLabel, gridBagConstraints);
 
         ctlColumns.setModel(new javax.swing.SpinnerNumberModel(2, 2, 10, 1));
         ctlColumns.setPreferredSize(new java.awt.Dimension(0, 0));
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.fill = java.awt.GridBagConstraints.VERTICAL;
         gridBagConstraints.ipadx = 50;
         gridBagConstraints.weighty = 1.0;
         panDividePanel.add(ctlColumns, gridBagConstraints);
 
         jLabel1.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
         jLabel1.setForeground(new java.awt.Color(255, 0, 0));
         jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
         jLabel1.setText("MORE OPTIONS COMING, SO DON'T BITE MY HEAD OFF.");
         jLabel1.setPreferredSize(new java.awt.Dimension(0, 0));
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridy = 0;
         gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
         gridBagConstraints.weightx = 1.0;
         gridBagConstraints.weighty = 1.0;
         panDividePanel.add(jLabel1, gridBagConstraints);
 
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridy = 2;
         gridBagConstraints.gridwidth = 3;
         gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
         gridBagConstraints.weightx = 1.0;
         gridBagConstraints.weighty = 1.0;
         gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
         panCenter.add(panDividePanel, gridBagConstraints);
 
         panWrapper.add(panCenter, java.awt.BorderLayout.CENTER);
 
         getContentPane().add(panWrapper, java.awt.BorderLayout.CENTER);
 
         pack();
     }// </editor-fold>//GEN-END:initComponents
 
     private void btnSrcActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSrcActionPerformed
         fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
         fc.setDialogTitle("Choose the gif to tweak.");
         fc.setFileFilter(filter);
 
         if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
             src = fc.getSelectedFile();
             txtSrc.setText(src.getPath());
             dest = new File(src.getParent() + "/" + Utils.getFileName(src));
             txtDest.setText(dest.getPath() + slash);
         }
     }//GEN-LAST:event_btnSrcActionPerformed
 
     private void btnDestActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDestActionPerformed
         fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
         fc.setDialogTitle("Tell me where you want to place the folder.");
         fc.setCurrentDirectory(src);
         fc.resetChoosableFileFilters();
 
         if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
             dest = new File(fc.getSelectedFile().getPath() + "/" + Utils.getFileName(src));
             txtDest.setText(dest.getPath() + slash);
         }
     }//GEN-LAST:event_btnDestActionPerformed
 
     private void btnTweakItActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnTweakItActionPerformed
         if (src == null) {
             setStatus("Pick a file dummy.");
         } else if (!src.exists()) {
            setStatus("Oh so you picked a file.  Pick one that still exists.");
         } else {
             TweakIt();
         }
 
 
     }//GEN-LAST:event_btnTweakItActionPerformed
 
     /**
      * @param args the command line arguments
      */
     public static void main(String args[]) {
         /* Set the Nimbus look and feel */
         //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
         /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
          * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html
          */
         try {
             for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                 if ("Nimbus".equals(info.getName())) {
                     javax.swing.UIManager.setLookAndFeel(info.getClassName());
                     break;
                 }
             }
         } catch (ClassNotFoundException ex) {
             java.util.logging.Logger.getLogger(GifTweaker.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
         } catch (InstantiationException ex) {
             java.util.logging.Logger.getLogger(GifTweaker.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
         } catch (IllegalAccessException ex) {
             java.util.logging.Logger.getLogger(GifTweaker.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
         } catch (javax.swing.UnsupportedLookAndFeelException ex) {
             java.util.logging.Logger.getLogger(GifTweaker.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
         }
         //</editor-fold>
 
         /* Create and display the form */
         java.awt.EventQueue.invokeLater(new Runnable() {
             public void run() {
                 GifTweaker gui = new GifTweaker();
                 Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
                 Dimension form = gui.getSize();
 
                 gui.setLocation((screen.width - form.width) / 2, (screen.height - form.height) / 2);
                 gui.setVisible(true);
             }
         });
     }
     // Variables declaration - do not modify//GEN-BEGIN:variables
     private javax.swing.JButton btnDest;
     private javax.swing.JButton btnSrc;
     private javax.swing.JButton btnTweakIt;
     private javax.swing.JSpinner ctlColumns;
     private javax.swing.JSpinner ctlRows;
     private javax.swing.JLabel jLabel1;
     private javax.swing.JLabel lblColumnsLabel;
     private javax.swing.JLabel lblDestLabel;
     private javax.swing.JLabel lblRowsLabel;
     private javax.swing.JLabel lblSrcLabel;
     private javax.swing.JLabel lblStatus;
     private javax.swing.JPanel panCenter;
     private javax.swing.JPanel panDividePanel;
     private javax.swing.JPanel panWrapper;
     private javax.swing.JTextField txtDest;
     private javax.swing.JTextField txtSrc;
     // End of variables declaration//GEN-END:variables
 }
