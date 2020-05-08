 package madsdf.shimmer.gui;
 
 import com.google.common.eventbus.EventBus;
 import com.google.common.eventbus.Subscribe;
 import info.monitorenter.gui.chart.Chart2D;
 import madsdf.shimmer.glview.ShimmerAngleConverter;
 import madsdf.shimmer.glview.ShimmerCanvas;
 import java.awt.Cursor;
 import java.awt.Toolkit;
 import java.awt.event.KeyEvent;
 import java.io.BufferedWriter;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 import java.util.Observer;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import java.util.prefs.BackingStoreException;
 import java.util.prefs.Preferences;
 import javax.swing.DefaultComboBoxModel;
 import javax.swing.JFrame;
 import madsdf.shimmer.event.Globals;
 import org.apache.commons.lang3.StringUtils;
 import org.jfree.chart.ChartPanel;
 
 /**
  * This is the main class of the application. The design has been realised with
  * the Netbeans graphic tool.
  *
  * Java version : JDK 1.6.0_21 IDE : Netbeans 7.1.1
  *
  * @author Gregoire Aubert
  * @version 1.0
  */
 public class ShimmerMoveAnalyzerFrame extends JFrame {
     private final static String PREFS_DEVICES = "btDevicesIDs";
 
     private static final long serialVersionUID = 1L;
     private final static Logger Log = Logger.getLogger(ShimmerMoveAnalyzerFrame.class.getName());
     // The bluetooth connected device
     private BluetoothDeviceCom connectedDevice;
     // The class drawing the chart
     private ChartsDrawer chartsDrawer;
     // Controller for OpenGL display
     private ShimmerAngleConverter angleConverter;
     // The write buffer
     private BufferedWriter out;
     // Remember user input
     private Preferences prefs;
     
     // The max commands and samples
     private int maxCommand = 6;
     private int maxSample = 10;
     private int currentCommand = 1;
     private int currentSample = 1;
     
     private final String btid;
     
     private final Logger log = Logger.getLogger(ShimmerMoveAnalyzerFrame.class.getName());
     
     private final EventBus eventBus;
     private Object sampleListener;
 
     /**
      * Creates new form ShimmerMoveAnalyzerFrame
      *
      * @param args The numbers of commands and samples can be passed via the
      * console parameters.
      */
     public ShimmerMoveAnalyzerFrame(String title, String btid) {
         this.btid = btid;
         initComponents();
         setTitle(title);
         /*prefs = Preferences.userRoot().node(this.getClass().getName());
         restorePreferences();*/
         
         
         final ShimmerCanvas shimmerCanvas = (ShimmerCanvas)panGL;
         eventBus = Globals.getBusForShimmer(btid);
         angleConverter = new ShimmerAngleConverter(eventBus, txtLog);
         
         // Eventbus registration
         eventBus.register(shimmerCanvas);
         eventBus.register(angleConverter);
         
         txtLog.setEditable(false);
         
         labBtId.setText(btid);
         
         // Automatically start streaming
         // TODO: This is DEBUG only
         connect();
         setCalibratedChart(cbCalibrated.isSelected());
     }
     
     /*private void restorePreferences() {
         Log.info(prefs.get(PREFS_DEVICES, ""));
         String[] devices = StringUtils.split(prefs.get(PREFS_DEVICES, ""), ";");
         jcbDevices.setModel(new DefaultComboBoxModel(devices));
     }
     
     private void appendToPreferences(String key, String value) {
         List<String> lst = new ArrayList<String>(Arrays.asList(StringUtils.split(prefs.get(key, ""), ";")));
         lst.add(value);
         
         prefs.put(key, StringUtils.join(lst, ";"));
         try {
             prefs.flush();
         } catch (BackingStoreException ex) {
             Logger.getLogger(ShimmerMoveAnalyzerFrame.class.getName()).log(Level.SEVERE, null, ex);
         }
         Log.info("Preferences saved");
     }*/
     
     private void connect() {
         try {
             log.info("Connecting to shimmer...");
             final String btServiceID = "btspp://00066646" + btid + ":1;authenticate=false;encrypt=false;master=false";
             connectedDevice = new BluetoothDeviceCom(eventBus, btid);
             connectedDevice.connect(btServiceID);
             log.info("Connected to shimmer");
         } catch (IOException ex) {
             Logger.getLogger(ShimmerMoveAnalyzerFrame.class.getName()).log(Level.SEVERE, null, ex);
         }
     }
     
     private void setCalibratedChart(boolean calibrated) {
         // Replace sample listener
         if (sampleListener != null) {
             eventBus.unregister(sampleListener);
         }
         
         chartsDrawer = new ChartsDrawer((Chart2D) panAccel, (Chart2D) panGyro);
         
         if (calibrated) {
             sampleListener  = new Object() {
                 @Subscribe
                 public void onSample(AccelGyro.CalibratedSample sample) {
                     chartsDrawer.addSample(sample);
                 }
             };
         } else {
             sampleListener  = new Object() {
                 @Subscribe
                 public void onSample(AccelGyro.UncalibratedSample sample) {
                     chartsDrawer.addSample(sample);
                 }
             };
         }
         eventBus.register(sampleListener);
     }
 
     /**
      * This method is called from within the constructor to initialize the form.
      * WARNING: Do NOT modify this code. The content of this method is always
      * regenerated by the Form Editor.
      */
     @SuppressWarnings("unchecked")
     // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
     private void initComponents() {
 
         jPanel5 = new javax.swing.JPanel();
         btnSave = new javax.swing.JButton();
         jPanelConnect = new javax.swing.JPanel();
         jLabel9 = new javax.swing.JLabel();
         labBtId = new javax.swing.JLabel();
         cbCalibrated = new javax.swing.JCheckBox();
         jPanel2 = new javax.swing.JPanel();
         panAccel = new Chart2D();
         panGyro = new Chart2D();
         panGL = ShimmerCanvas.createCanvas(this);
         panLog = new javax.swing.JScrollPane();
         txtLog = new javax.swing.JTextArea();
 
         setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
         setTitle("Graph accelerometer / gyro");
         setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
         setIconImage(Toolkit.getDefaultToolkit().getImage(getClass().getResource("shimmer_icon.png")));
         setMinimumSize(new java.awt.Dimension(500, 400));
         setName("frmShimmerMoveAnalyzer"); // NOI18N
         setPreferredSize(new java.awt.Dimension(400, 400));
         addWindowListener(new java.awt.event.WindowAdapter() {
             public void windowClosing(java.awt.event.WindowEvent evt) {
                 formWindowClosing(evt);
             }
         });
 
         btnSave.setText("Save");
         btnSave.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 btnSaveActionPerformed(evt);
             }
         });
 
         javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
         jPanel5.setLayout(jPanel5Layout);
         jPanel5Layout.setHorizontalGroup(
             jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel5Layout.createSequentialGroup()
                 .addContainerGap()
                 .addComponent(btnSave)
                 .addContainerGap(436, Short.MAX_VALUE))
         );
         jPanel5Layout.setVerticalGroup(
             jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(jPanel5Layout.createSequentialGroup()
                 .addContainerGap()
                 .addComponent(btnSave)
                 .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
         );
 
         jLabel9.setText("BT ID :");
 
         labBtId.setText("jLabel1");
 
         cbCalibrated.setText("Calibrated");
         cbCalibrated.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 cbCalibratedActionPerformed(evt);
             }
         });
 
         javax.swing.GroupLayout jPanelConnectLayout = new javax.swing.GroupLayout(jPanelConnect);
         jPanelConnect.setLayout(jPanelConnectLayout);
         jPanelConnectLayout.setHorizontalGroup(
             jPanelConnectLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(jPanelConnectLayout.createSequentialGroup()
                 .addContainerGap()
                 .addComponent(jLabel9)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(labBtId)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                 .addComponent(cbCalibrated)
                 .addContainerGap())
         );
         jPanelConnectLayout.setVerticalGroup(
             jPanelConnectLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(jPanelConnectLayout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(jPanelConnectLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(jLabel9)
                     .addComponent(labBtId)
                     .addComponent(cbCalibrated))
                 .addContainerGap(17, Short.MAX_VALUE))
         );
 
         jPanel2.setLayout(new java.awt.GridLayout(2, 2, 5, 5));
 
         panAccel.setName("panAccel"); // NOI18N
 
         javax.swing.GroupLayout panAccelLayout = new javax.swing.GroupLayout(panAccel);
         panAccel.setLayout(panAccelLayout);
         panAccelLayout.setHorizontalGroup(
             panAccelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGap(0, 262, Short.MAX_VALUE)
         );
         panAccelLayout.setVerticalGroup(
             panAccelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGap(0, 200, Short.MAX_VALUE)
         );
 
         jPanel2.add(panAccel);
 
         panGyro.setName("panAccel"); // NOI18N
 
         javax.swing.GroupLayout panGyroLayout = new javax.swing.GroupLayout(panGyro);
         panGyro.setLayout(panGyroLayout);
         panGyroLayout.setHorizontalGroup(
             panGyroLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGap(0, 262, Short.MAX_VALUE)
         );
         panGyroLayout.setVerticalGroup(
             panGyroLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGap(0, 200, Short.MAX_VALUE)
         );
 
         jPanel2.add(panGyro);
 
         javax.swing.GroupLayout panGLLayout = new javax.swing.GroupLayout(panGL);
         panGL.setLayout(panGLLayout);
         panGLLayout.setHorizontalGroup(
             panGLLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGap(0, 262, Short.MAX_VALUE)
         );
         panGLLayout.setVerticalGroup(
             panGLLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGap(0, 200, Short.MAX_VALUE)
         );
 
         jPanel2.add(panGL);
 
         txtLog.setColumns(20);
         txtLog.setFont(new java.awt.Font("Monospaced", 0, 12)); // NOI18N
         txtLog.setRows(5);
         panLog.setViewportView(txtLog);
 
         jPanel2.add(panLog);
 
         javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
         getContentPane().setLayout(layout);
         layout.setHorizontalGroup(
             layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(layout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addGroup(layout.createSequentialGroup()
                         .addComponent(jPanel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addGap(0, 0, Short.MAX_VALUE))
                     .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                         .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                             .addComponent(jPanel2, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 530, Short.MAX_VALUE)
                             .addComponent(jPanelConnect, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                         .addContainerGap())))
         );
         layout.setVerticalGroup(
             layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                 .addContainerGap()
                 .addComponent(jPanelConnect, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, 406, Short.MAX_VALUE)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(jPanel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addContainerGap())
         );
 
         pack();
     }// </editor-fold>//GEN-END:initComponents
 
    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
        if (connectedDevice != null) {
            connectedDevice.stop();
        }
    }//GEN-LAST:event_formWindowClosing
 
     private void btnSaveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSaveActionPerformed
         java.awt.EventQueue.invokeLater(new Runnable() {
             public void run() {
                 FileWriter output = null;
                 float[][] accelData = chartsDrawer.getRecentAccelData();
                 new CaptureEditFrame("movements", btid, accelData).setVisible(true);
             }
         });
     }//GEN-LAST:event_btnSaveActionPerformed
 
     private void cbCalibratedActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cbCalibratedActionPerformed
         setCalibratedChart(cbCalibrated.isSelected());
     }//GEN-LAST:event_cbCalibratedActionPerformed
 
     /**
      * @param args the command line arguments
      */
     public static void main(final String args[]) {
         /*
          * Set the Nimbus look and feel
          */
         //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
         /*
          * If Nimbus (introduced in Java SE 6) is not available, stay with the
          * default look and feel. For details see
          * http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html
          */
         try {
             boolean nimbus = false;
             for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                 if ("Nimbus".equals(info.getName())) {
                     javax.swing.UIManager.setLookAndFeel(info.getClassName());
                     nimbus = true;
                     break;
                 }
             }
             if (!nimbus || System.getProperty("os.name").toLowerCase().indexOf("win") >= 0) {
                 javax.swing.UIManager.setLookAndFeel(javax.swing.UIManager.getSystemLookAndFeelClassName());
             }
         } catch (ClassNotFoundException ex) {
             java.util.logging.Logger.getLogger(ShimmerMoveAnalyzerFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
         } catch (InstantiationException ex) {
             java.util.logging.Logger.getLogger(ShimmerMoveAnalyzerFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
         } catch (IllegalAccessException ex) {
             java.util.logging.Logger.getLogger(ShimmerMoveAnalyzerFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
         } catch (javax.swing.UnsupportedLookAndFeelException ex) {
             java.util.logging.Logger.getLogger(ShimmerMoveAnalyzerFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
         }
         //</editor-fold>
 
         /*
          * Create and display the form
          */
         // TODO: Get that from properties file
        //final String btid = "BDCD";
        final String btid = "9EDB";
         java.awt.EventQueue.invokeLater(new Runnable() {
             public void run() {
                 new ShimmerMoveAnalyzerFrame("Shimmer", btid).setVisible(true);
             }
         });
     }
     // Variables declaration - do not modify//GEN-BEGIN:variables
     private javax.swing.JButton btnSave;
     private javax.swing.JCheckBox cbCalibrated;
     private javax.swing.JLabel jLabel9;
     private javax.swing.JPanel jPanel2;
     private javax.swing.JPanel jPanel5;
     private javax.swing.JPanel jPanelConnect;
     private javax.swing.JLabel labBtId;
     private javax.swing.JPanel panAccel;
     private javax.swing.JPanel panGL;
     private javax.swing.JPanel panGyro;
     private javax.swing.JScrollPane panLog;
     private javax.swing.JTextArea txtLog;
     // End of variables declaration//GEN-END:variables
 }
