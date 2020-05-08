 /*
  * UploadFrameView.java
  */
 package uploadframe;
 
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import org.jdesktop.application.Action;
 import org.jdesktop.application.ResourceMap;
 import org.jdesktop.application.SingleFrameApplication;
 import org.jdesktop.application.FrameView;
 import org.jdesktop.application.TaskMonitor;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.io.BufferedReader;
 import javax.swing.Timer;
 import javax.swing.Icon;
 import javax.swing.JDialog;
 import java.io.File;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.PrintWriter;
 import java.io.StringWriter;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 import javax.swing.ImageIcon;
 import javax.swing.JOptionPane;
 
 import org.apache.http.HttpVersion;
 import org.apache.http.client.HttpClient;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.params.CoreProtocolPNames;
 
 /**
  * The application's main frame.
  */
 public class UploadFrameView extends FrameView {
 
     public static javax.swing.JTabbedPane source;
     List<String> bx = new ArrayList<String>();
     Settings s2 = new Settings();
     String rx;
     SiteOps so = new SiteOps();
     public static boolean x64 = true;
     String frxFileName = "";
     ImdbSearch ix = new ImdbSearch();
     String tempdir = "";
     String assembled = "";
     String[] imdbRefIDs = {"", "", "", "", ""};
     String[] str = {"", "", "", "", ""};
 
     public enum Quality {
 
         QTY_LOW, QTY_HIGH
     }
 
     public enum Codec {
 
         CDC_XVID, CDC_X264
     }
 
     public enum Container {
 
         CTN_AVI, CTN_MKV, CTN_MP4
     }
 
     public enum Resolution {
 
         FOUREIGHTY, FIVESEVENTYSIX, SEVENTWENTY, TENEIGHTY
     }
 
     public static String promptForInput(String txtMessage) {
          String result = "";
          while(result.isEmpty()) {
             result = JOptionPane.showInputDialog(frxFrame, txtMessage);
          }
          return result;
     }
     
     public void updateStatus(String ttfText, int percent) {
         statusMessageLabel.setText(ttfText);
         progressBar.setValue(percent);
     }
 
     public static String getExceptionStackTraceAsString(Exception exception) {
         StringWriter sw = new StringWriter();
         exception.printStackTrace(new PrintWriter(sw));
         return sw.toString();
     }
 
     public UploadFrameView(SingleFrameApplication app) {
         // START HERE, 6/17
         super(app);
 
         initComponents();
         source = frxFrame;
         ConfigReader cfg = new ConfigReader();
         String arch = cfg.getConfigValue("arch");
         if (arch.equals("win32")) {
             x64 = false;
         } else if (arch.equals("win64")) {
             x64 = true;
         }
 
         new FileDrop(null, jfDragFile, /*dragBorder,*/ new FileDrop.Listener() {
 
             public void filesDropped(java.io.File[] files) {
                 assembled = "";
                 int i, x = 0;
                 HttpClient httpclient = new DefaultHttpClient();
                 httpclient.getParams().setParameter(CoreProtocolPNames.PROTOCOL_VERSION, HttpVersion.HTTP_1_1);
 
                 for (i = 0; i < files.length; i++) {
                     if (i > 1) {
                         break;
                     }
                     try {
                         jfDragFile.append("Processing media file:\n " + files[i].getCanonicalPath() + "\n\n");
                         jfDragFile.append("Attempting to communicate with API\n");
                         jfDragFile.append("Searching IMDB");
 
                         rx = ix.search(files[i].getCanonicalPath());
                         if (rx.equals("-17614")) {
                             JOptionPane.showMessageDialog(frxFrame, "Unable to find movie on IMDB. The filename must be simple to be auto-detected on IMDB. You may enter in the IMDB ID manually.");
                             frxFileName = ix.getLastMovieName();
                             frxFrame.setSelectedIndex(1);
                             jTextField1.requestFocusInWindow();
                             
                         } else {
                             jTextField1.setText(rx);
                             frxFileName = ix.getLastMovieName();
                             frxFrame.setSelectedIndex(1);
                             String[][] z = {{"", "", ""}, {"", "", ""}, {"", "", ""}, {"", "", ""}, {"", "", ""}};
                             System.arraycopy(ImdbSearch.lastArray, 0, z, 0, 5);
                             if (z.length > 0) {
                                 int qz = 0;
                                 for (String ref[] : z) {
                                     if (!ref[1].isEmpty() && !ref[2].isEmpty()) {
                                         str[qz] = ref[1] + " (" + ref[2] + ")";
                                         imdbRefIDs[qz] = ref[0];
                                         qz++;
                                     }
                                 }
                                 try {
                                     if (qz > 0) {
                                         jList1.setModel(new javax.swing.AbstractListModel() {
 
                                             String[] strings = str;
 
                                             public int getSize() {
                                                 return strings.length;
                                             }
 
                                             public Object getElementAt(int i) {
                                                 return strings[i];
                                             }
                                         });
                                         jList1.setEnabled(true);
                                     }
                                 } catch (ArrayIndexOutOfBoundsException e) {
                                     e.printStackTrace();
                                 }
                             }
                             if (rx.isEmpty()) {
                                 JOptionPane.showMessageDialog(frxFrame, "There was a problem obtaining the IMDB ID. Please enter in the ID (numbers only) manually.");
                                 jTextField1.requestFocusInWindow();
                             }
                         }
                     } catch (Exception e) {
                         e.printStackTrace();
                         jfDragFile.append(getExceptionStackTraceAsString(e));
                     }
                 }
                 ModifyClipboard clipboard = new ModifyClipboard();
                 clipboard.setClipboardContents(assembled);
                 jfDragFile.append(x + " files uploaded (links copied to clipboard)\n\n");
             }   // end filesDropped
         }); // end FileDrop.Listener
         // status bar initialization - message timeout, idle icon and busy animation, etc
         ResourceMap resourceMap = getResourceMap();
         int messageTimeout = resourceMap.getInteger("StatusBar.messageTimeout");
         messageTimer = new Timer(messageTimeout, new ActionListener() {
 
             public void actionPerformed(ActionEvent e) {
                 statusMessageLabel.setText("");
             }
         });
         messageTimer.setRepeats(false);
         int busyAnimationRate = resourceMap.getInteger("StatusBar.busyAnimationRate");
         for (int i = 0; i < busyIcons.length; i++) {
             busyIcons[i] = resourceMap.getIcon("StatusBar.busyIcons[" + i + "]");
         }
         busyIconTimer = new Timer(busyAnimationRate, new ActionListener() {
 
             public void actionPerformed(ActionEvent e) {
                 busyIconIndex = (busyIconIndex + 1) % busyIcons.length;
                 statusAnimationLabel.setIcon(busyIcons[busyIconIndex]);
             }
         });
         idleIcon = resourceMap.getIcon("StatusBar.idleIcon");
         statusAnimationLabel.setIcon(idleIcon);
         progressBar.setVisible(false);
 
         // connecting action tasks to status bar via TaskMonitor
         TaskMonitor taskMonitor = new TaskMonitor(getApplication().getContext());
         taskMonitor.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
 
             public void propertyChange(java.beans.PropertyChangeEvent evt) {
                 String propertyName = evt.getPropertyName();
                 if ("started".equals(propertyName)) {
                     if (!busyIconTimer.isRunning()) {
                         statusAnimationLabel.setIcon(busyIcons[0]);
                         busyIconIndex = 0;
                         busyIconTimer.start();
                     }
                     progressBar.setVisible(true);
                     progressBar.setIndeterminate(true);
                 } else if ("done".equals(propertyName)) {
                     busyIconTimer.stop();
                     statusAnimationLabel.setIcon(idleIcon);
                     progressBar.setVisible(false);
                     progressBar.setValue(0);
                 } else if ("message".equals(propertyName)) {
                     String text = (String) (evt.getNewValue());
                     statusMessageLabel.setText((text == null) ? "" : text);
                     messageTimer.restart();
                 } else if ("progress".equals(propertyName)) {
                     int value = (Integer) (evt.getNewValue());
                     progressBar.setVisible(true);
                     progressBar.setIndeterminate(false);
                     progressBar.setValue(value);
                 }
             }
         });
     }
 
     /** This method is called from within the constructor to
      * initialize the form.
      * WARNING: Do NOT modify this code. The content of this method is
      * always regenerated by the Form Editor.
      */
     @SuppressWarnings("unchecked")
     // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
     private void initComponents() {
 
         mainPanel = new javax.swing.JPanel();
         frxFrame = new javax.swing.JTabbedPane();
         jPanel2 = new javax.swing.JPanel();
         jLabel12 = new javax.swing.JLabel();
         jScrollPane3 = new javax.swing.JScrollPane();
         jfDragFile = new javax.swing.JTextArea();
         jPanel1 = new javax.swing.JPanel();
         jScrollPane1 = new javax.swing.JScrollPane();
         jList1 = new javax.swing.JList();
         jLabel1 = new javax.swing.JLabel();
         jTextField1 = new javax.swing.JTextField();
         jButton1 = new javax.swing.JButton();
         jLabel2 = new javax.swing.JLabel();
         panel2 = new javax.swing.JTabbedPane();
         jPanel3 = new javax.swing.JPanel();
         jScrollPane2 = new javax.swing.JScrollPane();
         jTextArea1 = new javax.swing.JTextArea();
         jPanel4 = new javax.swing.JPanel();
         jTabbedPane1 = new javax.swing.JTabbedPane();
         jLabel5 = new javax.swing.JLabel();
         jLabel6 = new javax.swing.JLabel();
         jLabel7 = new javax.swing.JLabel();
         jPanel6 = new javax.swing.JPanel();
         jLabel4 = new javax.swing.JLabel();
         jLabel8 = new javax.swing.JLabel();
         jLabel9 = new javax.swing.JLabel();
         jLabel10 = new javax.swing.JLabel();
         jLabel11 = new javax.swing.JLabel();
         jComboBox1 = new javax.swing.JComboBox();
         jComboBox2 = new javax.swing.JComboBox();
         jComboBox3 = new javax.swing.JComboBox();
         jComboBox5 = new javax.swing.JComboBox();
         jTextField2 = new javax.swing.JTextField();
         jLabel14 = new javax.swing.JLabel();
         jCheckBox1 = new javax.swing.JCheckBox();
         jTextField3 = new javax.swing.JTextField();
         jLabel15 = new javax.swing.JLabel();
         jPanel5 = new javax.swing.JPanel();
         jLabel13 = new javax.swing.JLabel();
         jButton2 = new javax.swing.JButton();
         jButton3 = new javax.swing.JButton();
         statusPanel = new javax.swing.JPanel();
         javax.swing.JSeparator statusPanelSeparator = new javax.swing.JSeparator();
         statusMessageLabel = new javax.swing.JLabel();
         statusAnimationLabel = new javax.swing.JLabel();
         progressBar = new javax.swing.JProgressBar();
         jLabel3 = new javax.swing.JLabel();
 
         mainPanel.setName("mainPanel"); // NOI18N
 
         frxFrame.setName("adminTab"); // NOI18N
         frxFrame.setRequestFocusEnabled(false);
 
         jPanel2.setName("jPanel2"); // NOI18N
 
         org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(uploadframe.UploadFrameApp.class).getContext().getResourceMap(UploadFrameView.class);
         jLabel12.setText(resourceMap.getString("jLabel12.text")); // NOI18N
         jLabel12.setName("jLabel12"); // NOI18N
 
         jScrollPane3.setName("jScrollPane3"); // NOI18N
 
         jfDragFile.setColumns(20);
         jfDragFile.setRows(5);
         jfDragFile.setName("jfDragFile"); // NOI18N
         jScrollPane3.setViewportView(jfDragFile);
 
         javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
         jPanel2.setLayout(jPanel2Layout);
         jPanel2Layout.setHorizontalGroup(
             jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(jPanel2Layout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 539, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(jLabel12))
                 .addContainerGap(131, Short.MAX_VALUE))
         );
         jPanel2Layout.setVerticalGroup(
             jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(jPanel2Layout.createSequentialGroup()
                 .addComponent(jLabel12)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 291, Short.MAX_VALUE)
                 .addContainerGap())
         );
 
         frxFrame.addTab(resourceMap.getString("jPanel2.TabConstraints.tabTitle"), jPanel2); // NOI18N
 
         jPanel1.setName("panelImdbRef"); // NOI18N
 
         jScrollPane1.setName("jScrollPane1"); // NOI18N
 
         jList1.setModel(new javax.swing.AbstractListModel() {
             String[] strings = { "--Not on List--", "Avatar (2009)", "Tomorrow Never Dies (1997)", "\"Avatar: The Last Airbender\" (2005)", "The Last Airbender (2010)" };
             public int getSize() { return strings.length; }
             public Object getElementAt(int i) { return strings[i]; }
         });
         jList1.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
         jList1.setEnabled(false);
         jList1.setName("jList1"); // NOI18N
         jList1.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
             public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                 jList1ValueChanged(evt);
             }
         });
         jScrollPane1.setViewportView(jList1);
 
         jLabel1.setText(resourceMap.getString("jLabel1.text")); // NOI18N
         jLabel1.setName("jLabel1"); // NOI18N
 
         jTextField1.setText(resourceMap.getString("imdbURL.text")); // NOI18N
         jTextField1.setName("imdbURL"); // NOI18N
         jTextField1.addFocusListener(new java.awt.event.FocusAdapter() {
             public void focusLost(java.awt.event.FocusEvent evt) {
                 jTextField1FocusLost(evt);
             }
         });
 
         javax.swing.ActionMap actionMap = org.jdesktop.application.Application.getInstance(uploadframe.UploadFrameApp.class).getContext().getActionMap(UploadFrameView.class, this);
         jButton1.setAction(actionMap.get("rfFrChangeRipSpecs")); // NOI18N
         jButton1.setText(resourceMap.getString("jButton1.text")); // NOI18N
         jButton1.setName("jButton1"); // NOI18N
 
         jLabel2.setText(resourceMap.getString("jLabel2.text")); // NOI18N
         jLabel2.setName("jLabel2"); // NOI18N
 
         javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
         jPanel1.setLayout(jPanel1Layout);
         jPanel1Layout.setHorizontalGroup(
             jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(jPanel1Layout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addGroup(jPanel1Layout.createSequentialGroup()
                         .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, 207, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addGap(18, 18, 18)
                         .addComponent(jButton1))
                     .addComponent(jLabel1)
                     .addGroup(jPanel1Layout.createSequentialGroup()
                         .addComponent(jLabel2)
                         .addGap(110, 110, 110)
                         .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 271, javax.swing.GroupLayout.PREFERRED_SIZE)))
                 .addContainerGap(204, Short.MAX_VALUE))
         );
         jPanel1Layout.setVerticalGroup(
             jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(jPanel1Layout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addGroup(jPanel1Layout.createSequentialGroup()
                         .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 14, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addGap(78, 78, 78)
                         .addComponent(jLabel2))
                     .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 86, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(jButton1))
                 .addContainerGap())
         );
 
         frxFrame.addTab(resourceMap.getString("panelImdbRef.TabConstraints.tabTitle"), jPanel1); // NOI18N
 
         panel2.setName("panel2"); // NOI18N
 
         jPanel3.setName("jPanel3"); // NOI18N
 
         jScrollPane2.setName("jScrollPane2"); // NOI18N
 
         jTextArea1.setColumns(20);
         jTextArea1.setRows(5);
         jTextArea1.setName("jTextArea1"); // NOI18N
         jScrollPane2.setViewportView(jTextArea1);
 
         javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
         jPanel3.setLayout(jPanel3Layout);
         jPanel3Layout.setHorizontalGroup(
             jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(jPanel3Layout.createSequentialGroup()
                 .addContainerGap()
                 .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 655, Short.MAX_VALUE)
                 .addContainerGap())
         );
         jPanel3Layout.setVerticalGroup(
             jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(jPanel3Layout.createSequentialGroup()
                 .addContainerGap()
                 .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 272, Short.MAX_VALUE)
                 .addContainerGap())
         );
 
         panel2.addTab(resourceMap.getString("jPanel3.TabConstraints.tabTitle"), jPanel3); // NOI18N
 
         jPanel4.setName("jPanel4"); // NOI18N
 
         jTabbedPane1.setName("jTabbedPane1"); // NOI18N
 
         jLabel5.setText(resourceMap.getString("jLabel5.text")); // NOI18N
         jLabel5.setName("jLabel5"); // NOI18N
         jTabbedPane1.addTab(resourceMap.getString("jLabel5.TabConstraints.tabTitle"), jLabel5); // NOI18N
 
         jLabel6.setText(resourceMap.getString("jLabel6.text")); // NOI18N
         jLabel6.setName("jLabel6"); // NOI18N
         jTabbedPane1.addTab(resourceMap.getString("jLabel6.TabConstraints.tabTitle"), jLabel6); // NOI18N
 
         jLabel7.setText(resourceMap.getString("jLabel7.text")); // NOI18N
         jLabel7.setName("jLabel7"); // NOI18N
         jTabbedPane1.addTab(resourceMap.getString("jLabel7.TabConstraints.tabTitle"), jLabel7); // NOI18N
 
         javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
         jPanel4.setLayout(jPanel4Layout);
         jPanel4Layout.setHorizontalGroup(
             jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(jPanel4Layout.createSequentialGroup()
                 .addContainerGap()
                 .addComponent(jTabbedPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 655, Short.MAX_VALUE)
                 .addContainerGap())
         );
         jPanel4Layout.setVerticalGroup(
             jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(jPanel4Layout.createSequentialGroup()
                 .addContainerGap()
                 .addComponent(jTabbedPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 272, Short.MAX_VALUE)
                 .addContainerGap())
         );
 
         panel2.addTab(resourceMap.getString("jPanel4.TabConstraints.tabTitle"), jPanel4); // NOI18N
 
         jPanel6.setName("jPanel6"); // NOI18N
 
         jLabel4.setText(resourceMap.getString("jLabel4.text")); // NOI18N
         jLabel4.setName("jLabel4"); // NOI18N
 
         jLabel8.setText(resourceMap.getString("jLabel8.text")); // NOI18N
         jLabel8.setName("jLabel8"); // NOI18N
 
         jLabel9.setText(resourceMap.getString("jLabel9.text")); // NOI18N
         jLabel9.setName("jLabel9"); // NOI18N
 
         jLabel10.setText(resourceMap.getString("jLabel10.text")); // NOI18N
         jLabel10.setName("jLabel10"); // NOI18N
 
         jLabel11.setText(resourceMap.getString("jLabel11.text")); // NOI18N
         jLabel11.setName("jLabel11"); // NOI18N
 
         jComboBox1.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Standard Definition", "High Definition", "Other" }));
         jComboBox1.setName("jComboBox1"); // NOI18N
         jComboBox1.addItemListener(new java.awt.event.ItemListener() {
             public void itemStateChanged(java.awt.event.ItemEvent evt) {
                 jComboBox1ItemStateChanged(evt);
             }
         });
 
         jComboBox2.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "x264", "xvid" }));
         jComboBox2.setName("jComboBox2"); // NOI18N
         jComboBox2.addItemListener(new java.awt.event.ItemListener() {
             public void itemStateChanged(java.awt.event.ItemEvent evt) {
                 jComboBox2ItemStateChanged(evt);
             }
         });
 
         jComboBox3.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "mkv", "avi", "mp4" }));
         jComboBox3.setName("jComboBox3"); // NOI18N
         jComboBox3.addItemListener(new java.awt.event.ItemListener() {
             public void itemStateChanged(java.awt.event.ItemEvent evt) {
                 jComboBox3ItemStateChanged(evt);
             }
         });
 
         jComboBox5.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Blu-ray", "DVD", "VHS", "HD-DVD", "TV", "HDTV" }));
         jComboBox5.setName("jComboBox5"); // NOI18N
         jComboBox5.addItemListener(new java.awt.event.ItemListener() {
             public void itemStateChanged(java.awt.event.ItemEvent evt) {
                 jComboBox5ItemStateChanged(evt);
             }
         });
 
         jTextField2.setText(resourceMap.getString("jTextField2.text")); // NOI18N
         jTextField2.setName("jTextField2"); // NOI18N
         jTextField2.addFocusListener(new java.awt.event.FocusAdapter() {
             public void focusLost(java.awt.event.FocusEvent evt) {
                 jTextField2FocusLost(evt);
             }
         });
         jTextField2.addKeyListener(new java.awt.event.KeyAdapter() {
             public void keyReleased(java.awt.event.KeyEvent evt) {
                 jTextField2KeyReleased(evt);
             }
         });
 
         jLabel14.setText(resourceMap.getString("jLabel14.text")); // NOI18N
         jLabel14.setName("jLabel14"); // NOI18N
 
         jCheckBox1.setLabel(resourceMap.getString("jCheckBox1.label")); // NOI18N
         jCheckBox1.setName("jCheckBox1"); // NOI18N
         jCheckBox1.addItemListener(new java.awt.event.ItemListener() {
             public void itemStateChanged(java.awt.event.ItemEvent evt) {
                 jCheckBox1ItemStateChanged(evt);
             }
         });
 
         jTextField3.setText(resourceMap.getString("jTextField3.text")); // NOI18N
         jTextField3.setName("jTextField3"); // NOI18N
         jTextField3.addFocusListener(new java.awt.event.FocusAdapter() {
             public void focusLost(java.awt.event.FocusEvent evt) {
                 jTextField3FocusLost(evt);
             }
         });
 
         jLabel15.setText(resourceMap.getString("jLabel15.text")); // NOI18N
         jLabel15.setName("jLabel15"); // NOI18N
 
         javax.swing.GroupLayout jPanel6Layout = new javax.swing.GroupLayout(jPanel6);
         jPanel6.setLayout(jPanel6Layout);
         jPanel6Layout.setHorizontalGroup(
             jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(jPanel6Layout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addGroup(jPanel6Layout.createSequentialGroup()
                         .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                             .addComponent(jLabel9)
                             .addComponent(jLabel4)
                             .addComponent(jLabel8))
                         .addGap(23, 23, 23)
                         .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                             .addComponent(jComboBox1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                             .addComponent(jComboBox2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                             .addComponent(jComboBox3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                     .addGroup(jPanel6Layout.createSequentialGroup()
                         .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                             .addComponent(jLabel15)
                             .addComponent(jLabel10)
                             .addComponent(jLabel11)
                             .addComponent(jLabel14))
                         .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                             .addGroup(jPanel6Layout.createSequentialGroup()
                                 .addGap(18, 18, 18)
                                 .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                     .addComponent(jComboBox5, 0, 123, Short.MAX_VALUE)
                                     .addComponent(jCheckBox1)
                                     .addComponent(jTextField3, javax.swing.GroupLayout.PREFERRED_SIZE, 76, javax.swing.GroupLayout.PREFERRED_SIZE)))
                             .addGroup(jPanel6Layout.createSequentialGroup()
                                 .addGap(18, 18, 18)
                                 .addComponent(jTextField2, javax.swing.GroupLayout.DEFAULT_SIZE, 123, Short.MAX_VALUE)))))
                 .addContainerGap(470, javax.swing.GroupLayout.PREFERRED_SIZE))
         );
         jPanel6Layout.setVerticalGroup(
             jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(jPanel6Layout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(jLabel4)
                     .addComponent(jComboBox1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(jLabel8)
                     .addComponent(jComboBox2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(jLabel9)
                     .addComponent(jComboBox3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(jLabel10)
                     .addComponent(jTextField2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(jLabel11)
                     .addComponent(jComboBox5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                     .addComponent(jCheckBox1)
                     .addComponent(jLabel14))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                 .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(jLabel15)
                     .addComponent(jTextField3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addContainerGap(105, Short.MAX_VALUE))
         );
 
         panel2.addTab(resourceMap.getString("jPanel6.TabConstraints.tabTitle"), jPanel6); // NOI18N
 
         frxFrame.addTab(resourceMap.getString("panel2.TabConstraints.tabTitle"), panel2); // NOI18N
         panel2.getAccessibleContext().setAccessibleName(resourceMap.getString("panel2.AccessibleContext.accessibleName")); // NOI18N
 
         jPanel5.setName("jPanel5"); // NOI18N
 
         jLabel13.setText(resourceMap.getString("jLabel13.text")); // NOI18N
         jLabel13.setName("jLabel13"); // NOI18N
 
         jButton2.setAction(actionMap.get("rfxCheckGroup")); // NOI18N
         jButton2.setText(resourceMap.getString("jButton2.text")); // NOI18N
         jButton2.setName("jButton2"); // NOI18N
 
         jButton3.setAction(actionMap.get("rfxUpload")); // NOI18N
         jButton3.setText(resourceMap.getString("jButton3.text")); // NOI18N
         jButton3.setName("jButton3"); // NOI18N
 
         javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
         jPanel5.setLayout(jPanel5Layout);
         jPanel5Layout.setHorizontalGroup(
             jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(jPanel5Layout.createSequentialGroup()
                 .addGap(24, 24, 24)
                 .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addComponent(jButton3)
                     .addGroup(jPanel5Layout.createSequentialGroup()
                         .addComponent(jButton2)
                         .addGap(18, 18, 18)
                         .addComponent(jLabel13, javax.swing.GroupLayout.PREFERRED_SIZE, 277, javax.swing.GroupLayout.PREFERRED_SIZE)))
                 .addContainerGap(250, Short.MAX_VALUE))
         );
         jPanel5Layout.setVerticalGroup(
             jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(jPanel5Layout.createSequentialGroup()
                 .addGap(25, 25, 25)
                 .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                     .addComponent(jLabel13, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(jButton2))
                 .addGap(38, 38, 38)
                 .addComponent(jButton3)
                 .addContainerGap(213, Short.MAX_VALUE))
         );
 
         frxFrame.addTab(resourceMap.getString("jPanel5.TabConstraints.tabTitle"), jPanel5); // NOI18N
 
         javax.swing.GroupLayout mainPanelLayout = new javax.swing.GroupLayout(mainPanel);
         mainPanel.setLayout(mainPanelLayout);
         mainPanelLayout.setHorizontalGroup(
             mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(mainPanelLayout.createSequentialGroup()
                 .addContainerGap()
                 .addComponent(frxFrame, javax.swing.GroupLayout.DEFAULT_SIZE, 685, Short.MAX_VALUE)
                 .addContainerGap())
         );
         mainPanelLayout.setVerticalGroup(
             mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(mainPanelLayout.createSequentialGroup()
                 .addComponent(frxFrame, javax.swing.GroupLayout.DEFAULT_SIZE, 350, Short.MAX_VALUE)
                 .addContainerGap())
         );
 
         statusPanel.setName("statusPanel"); // NOI18N
 
         statusPanelSeparator.setName("statusPanelSeparator"); // NOI18N
 
         statusMessageLabel.setName("statusMessageLabel"); // NOI18N
 
         statusAnimationLabel.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
         statusAnimationLabel.setName("statusAnimationLabel"); // NOI18N
 
         progressBar.setName("currentProgressBar"); // NOI18N
 
         jLabel3.setText(resourceMap.getString("currentProgressText.text")); // NOI18N
         jLabel3.setName("currentProgressText"); // NOI18N
 
         javax.swing.GroupLayout statusPanelLayout = new javax.swing.GroupLayout(statusPanel);
         statusPanel.setLayout(statusPanelLayout);
         statusPanelLayout.setHorizontalGroup(
             statusPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addComponent(statusPanelSeparator, javax.swing.GroupLayout.DEFAULT_SIZE, 705, Short.MAX_VALUE)
             .addGroup(statusPanelLayout.createSequentialGroup()
                 .addGroup(statusPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                     .addGroup(statusPanelLayout.createSequentialGroup()
                         .addContainerGap()
                         .addComponent(statusMessageLabel)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 535, Short.MAX_VALUE))
                     .addGroup(statusPanelLayout.createSequentialGroup()
                         .addContainerGap()
                         .addComponent(jLabel3)
                         .addGap(18, 18, 18)))
                 .addComponent(progressBar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(statusAnimationLabel)
                 .addContainerGap())
         );
         statusPanelLayout.setVerticalGroup(
             statusPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(statusPanelLayout.createSequentialGroup()
                 .addComponent(statusPanelSeparator, javax.swing.GroupLayout.PREFERRED_SIZE, 2, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                 .addGroup(statusPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                     .addGroup(statusPanelLayout.createSequentialGroup()
                         .addGroup(statusPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                             .addComponent(statusMessageLabel)
                             .addComponent(statusAnimationLabel)
                             .addComponent(progressBar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                         .addGap(3, 3, 3))
                     .addComponent(jLabel3)))
         );
 
         setComponent(mainPanel);
         setStatusBar(statusPanel);
     }// </editor-fold>//GEN-END:initComponents
 
     private void jComboBox1ItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jComboBox1ItemStateChanged
         s2.changeQuality(jComboBox1.getSelectedItem().toString());
     }//GEN-LAST:event_jComboBox1ItemStateChanged
 
     private void jComboBox2ItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jComboBox2ItemStateChanged
         s2.changeCodec(jComboBox2.getSelectedItem().toString());
     }//GEN-LAST:event_jComboBox2ItemStateChanged
 
     private void jComboBox3ItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jComboBox3ItemStateChanged
         s2.changeContainer(jComboBox3.getSelectedItem().toString());
     }//GEN-LAST:event_jComboBox3ItemStateChanged
 
     private void jTextField2KeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTextField2KeyReleased
     }//GEN-LAST:event_jTextField2KeyReleased
 
     private void jComboBox5ItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jComboBox5ItemStateChanged
         s2.changeSource(jComboBox5.getSelectedItem().toString());
     }//GEN-LAST:event_jComboBox5ItemStateChanged
 
     private void jTextField2FocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jTextField2FocusLost
         s2.changeResolution(jTextField2.getText());
     }//GEN-LAST:event_jTextField2FocusLost
 
     private void jCheckBox1ItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jCheckBox1ItemStateChanged
         s2.changeScene(jCheckBox1.isSelected());
     }//GEN-LAST:event_jCheckBox1ItemStateChanged
 
     private void jTextField3FocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jTextField3FocusLost
         s2.changeRemaster(jTextField3.getText());
     }//GEN-LAST:event_jTextField3FocusLost
 
     private void jTextField1FocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jTextField1FocusLost
         jTextField1.setText(jTextField1.getText().replaceAll("[^0-9]", ""));
         this.rx = jTextField1.getText();
     }//GEN-LAST:event_jTextField1FocusLost
 
     private void jList1ValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_jList1ValueChanged
         jTextField1.setText(this.imdbRefIDs[jList1.getSelectedIndex()]);
     }//GEN-LAST:event_jList1ValueChanged
 
     @Action
     public List<String> rfFrChangeRipSpecs() {
         final List<String> rtx = new ArrayList<String>();
         new Thread() {
 
             @Override
             public void run() {
                 try {
                     System.out.println(new File(".").toURI());
                     MediaInfo minfo = new MediaInfo();
                     assembled = "";
                     assembled += minfo.grabInfo(frxFileName);
                     String ffm_exe;
                     ConfigReader cfg = new ConfigReader();
                     if (cfg.getConfigValue("ffmpeg").equalsIgnoreCase("autodetect")) {
                         ffm_exe = x64 ? "win64/ffmpeg64.exe" : "win32/ffmpeg32.exe";
                     } else {
                         ffm_exe = cfg.getConfigValue("ffmpeg");
                     }
                     String line, output = "";
                     System.out.println("Runonce1");
                     List<String> cmdargs = new ArrayList<String>();
                     cmdargs.add(ffm_exe);
                     cmdargs.add("-i");
                     cmdargs.add(frxFileName);
                     ProcessBuilder process = new ProcessBuilder(cmdargs);
                     process.redirectErrorStream(true);
                     Process p = process.start();
                     InputStream is = p.getInputStream();
                     InputStreamReader isr = new InputStreamReader(is);
                     BufferedReader br = new BufferedReader(isr);
                     output = "";
                     while ((line = br.readLine()) != null) {
                         System.out.println(line);
                         output += line;
                     }
                     System.out.println("Runonce2");
                     int zh = p.waitFor();
                     Pattern dar1, dar2;
                     Matcher m1, m2;
                     int w = 0, h = 0, parx = 0, pary = 0;
                     dar1 = Pattern.compile("(\\d+)x(\\d+), PAR (\\d+):(\\d+) DAR");
                     dar2 = Pattern.compile("(\\d+)x(\\d+) \\[PAR (\\d+):(\\d+) DAR");
                     m1 = dar1.matcher(output);
                     m2 = dar2.matcher(output);
                     boolean x4 = false;
                     if (m1.find()) {
                         w = Integer.parseInt(m1.group(1));
                         h = Integer.parseInt(m1.group(2));
                         parx = Integer.parseInt(m1.group(3));
                         pary = Integer.parseInt(m1.group(4));
                         x4 = true;
                     } else if (m2.find()) {
                         w = Integer.parseInt(m2.group(1));
                         h = Integer.parseInt(m2.group(2));
                         parx = Integer.parseInt(m2.group(3));
                         pary = Integer.parseInt(m2.group(4));
                         x4 = true;
                     } else {
                         // Anamorphic problem?
                         // need to grab mediainfo stuff here?
                         JOptionPane.showMessageDialog(frxFrame, "There was a problem finding the anamorphic resolution of this file. We have falled back to the normal resolution from mediainfo.");
                     }
                     int width = 0;
                     int height = 0;
                     if (x4) {
                         // Modify resolution
                         width = (w * parx) / pary;
                         height = h;
                     } else {
                         /*
                          * Width                            : 560 pixels
                          * Height                           : 320 pixels
                          */
                         dar1 = Pattern.compile("Width.+(\\d+) pixels");
                         dar2 = Pattern.compile("Height.+(\\d+) pixels");
                         m1 = dar1.matcher(assembled);
                         m2 = dar2.matcher(assembled);
                         m1.find();
                         m2.find();
                         width = Integer.parseInt(m1.group(1));
                         height = Integer.parseInt(m2.group(1));
                     }
                    
                    if (width%2>0)
                        width+=1;
                    
                     System.out.println("Modified res: " + w + "x" + h + " to " + width + "x" + height);
 
                     if (cfg.getConfigValue("tempdir").equalsIgnoreCase("autodetect")) {
                         tempdir = System.getProperty("java.io.tmpdir");
 
                         if (!(tempdir.endsWith("/") || tempdir.endsWith("\\"))) {
                             tempdir = tempdir + System.getProperty("file.separator");
                         }
                     } else {
                         tempdir = cfg.getConfigValue("tempdir");
                     }
 
                     Matcher mx;
                     Pattern px = Pattern.compile("(?:(\\d+)h\\s?)?(?:(\\d+)mn\\s?)?(?:(\\d+)s\\s?)?", Pattern.MULTILINE);
                     String rx = assembled.substring(assembled.indexOf("Duration")).trim();
                     rx = rx.substring(0, rx.indexOf("\n"));
                     System.out.println(px.pattern());
                     int duration = 0;
                     mx = px.matcher(rx);
                     while (mx.find()) {
                         if (mx.group(1) != null) {
                             duration += Integer.parseInt(mx.group(1)) * 60 * 60;
                         }
                         if (mx.group(2) != null) {
                             duration += Integer.parseInt(mx.group(2)) * 60;
                         }
                         if (mx.group(3) != null) {
                             duration += Integer.parseInt(mx.group(3));
                         }
                     }
                     for (int y = 1; y <= 3; y++) {
                         if (!cmdargs.isEmpty()) {
                             cmdargs.clear();
                         }
                         if (!new File(ffm_exe).exists()) {
                             JOptionPane.showMessageDialog(frxFrame, "FFmpeg missing! Check your config file.");
                             throw new Exception("FFmpeg missing! Check your config file.");
                         }
                         cmdargs.add(ffm_exe);
                         cmdargs.add("-an");
                         cmdargs.add("-sn");
                         cmdargs.add("-ss");
                         int len = 0;
                         switch (y) {
                             case 1:
                                 len = (int) (duration * 0.10);
                                 break;
                             case 2:
                                 len = (int) (duration * 0.15);
                                 break;
                             case 3:
                                 len = (int) (duration * 0.2);
                                 break;
                         }
 
                         cmdargs.add(Integer.toString(len));
                         cmdargs.add("-i");
                         cmdargs.add(frxFileName);
                         cmdargs.add("-vcodec");
 //, "png", "-vframes", "1", outputPngPath
                         cmdargs.add("png");
                         cmdargs.add("-vframes");
                         cmdargs.add("1");
 
                         if (x4) {
                             cmdargs.add("-s");
                             cmdargs.add(width + "x" + height);
                         }
 
                         cmdargs.add("-y");
                         cmdargs.add(tempdir + "ss" + y + ".png");
                         process = new ProcessBuilder(cmdargs);
                         process.redirectErrorStream(true);
                         p = process.start();
                         is = p.getInputStream();
                         isr = new InputStreamReader(is);
                         br = new BufferedReader(isr);
                         output = "";
                         while ((line = br.readLine()) != null) {
                             System.out.println(line);
                             output += line;
                         }
                         System.out.println("Uploading: " + tempdir + "ss" + y + ".png\n");
                         assembled += FileUpload.UploadFile(tempdir + "ss" + y + ".png");
                     }
                     ImageIcon ic1 = new ImageIcon(tempdir + "ss1.png", "Screenshot #1");
                     ImageIcon ic2 = new ImageIcon(tempdir + "ss2.png", "Screenshot #2");
                     ImageIcon ic3 = new ImageIcon(tempdir + "ss3.png", "Screenshot #3");
                     jLabel5.setIcon(ic1);
                     jLabel6.setIcon(ic2);
                     jLabel7.setIcon(ic3);
 
                     //info.matches("x264")
                     jTextArea1.setText(assembled);
 
 
                     frxFrame.setSelectedIndex(2);
                     panel2.setSelectedIndex(2);
                     MediaParser mpx = new MediaParser(assembled);
                     List<String> list = mpx.ParseInfo();
                     String codec = list.get(0);
                     String container = list.get(1);
                     String resolution = list.get(2);
                     int size = (int) new File(frxFileName).length();
                     /*
                     public enum Quality { QTY_LOW, QTY_HIGH }
                     public enum Codec { CDC_XVID, CDC_X264 }
                     public enum Container { CTN_AVI, CTN_MKV, CTN_MP4 }
                     public enum Resolution { FOUREIGHTY, FIVESEVENTYSIX, SEVENTWENTY, TENEIGHTY }
                      */
                     if (codec.equalsIgnoreCase("x264")) {
                         jComboBox2.setSelectedIndex(0);
                     } else if (codec.equalsIgnoreCase("xvid")) {
                         jComboBox2.setSelectedIndex(1);
                     }
 
                     if (container.equalsIgnoreCase("mkv")) {
                         jComboBox3.setSelectedIndex(0);
                     } else if (container.equalsIgnoreCase("avi")) {
                         jComboBox3.setSelectedIndex(1);
                     } else if (container.equalsIgnoreCase("mp4")) {
                         jComboBox3.setSelectedIndex(2);
                     }
 
                     int source = 0, quality = 0;
 
 
                     if (resolution.equals("720p") || resolution.equals("1080p")) {
                         quality = 1;
                         source = 0;
                     } else {
                         quality = 0;
                         source = 1;
                     }
 
                     System.out.println("q:" + quality);
                     System.out.println("s:" + source);
                     System.out.println("c:" + codec);
                     System.out.println("c2:" + container);
                     System.out.println("r:" + resolution);
 
                     jComboBox1.setSelectedIndex(quality);
                     jComboBox5.setSelectedIndex(source);
                     jTextField2.setText(resolution);
 
                     rtx.add(Integer.toString(quality));
                     rtx.add(jComboBox5.getSelectedItem().toString());
                     rtx.add(codec);
                     rtx.add(container);
                     rtx.add(resolution);
                     bx = rtx;
                     s2 = new Settings(bx.get(3), bx.get(4), bx.get(2), bx.get(1), bx.get(0), jCheckBox1.isSelected(), jTextField3.getText());
                     //jComboBox1.setSelectedIndex(codec);
                     //jComboBox1.setSelectedIndex();
 	            	/*(new File(dir+"ss1.png")).delete();
                     (new File(dir+"ss2.png")).delete();
                     (new File(dir+"ss3.png")).delete();*/
                 } catch (Exception e) {
                     e.printStackTrace();
                 }
             }
         }.run();
         return rtx;
     }
 
     @Action
     public void rfxCheckGroup() {
         new Thread() {
 
             @Override
             public void run() {
                 try {
                     jLabel13.setText("Contacting site...");
                     so.login();
                     jLabel13.setText("Logged in!");
                     Thread.sleep(1000);
                     jLabel13.setText("Checking site for existing files");
                     jButton3.setEnabled(true);
                     if (!so.checkImdbAgainstSite(rx, s2)) {
                         jLabel13.setText("Movie exists in format already.");
                     } else {
                         jLabel13.setText("Ready for upload!");
                     }
 
                     jButton3.setEnabled(true);
 
                 } catch (Exception ex) {
                     Logger.getLogger(UploadFrameView.class.getName()).log(Level.SEVERE, null, ex);
                 }
             }
         }.run();
     }
 
     @Action
     public void rfxUpload() {
         new Thread() {
 
             @Override
             public void run() {
                 try {
                     //updateStatus("Hashing...", 50);
                     jButton3.setEnabled(false);
                     jLabel13.setText("Hashing...");
                     jLabel13.updateUI();
                     ConfigReader c = new ConfigReader();
                     /*List<String> cmdargs = new ArrayList<String>();
                     if (!cmdargs.isEmpty()) {
                     cmdargs.clear();
                     }
                     String mturi = new String(c.getConfigValue("mktorrent"));
                     if (mturi.equalsIgnoreCase("autodetect") || mturi.length() == 0) {
                     mturi = "mktorrent.exe";
                     }
                     cmdargs.add(mturi);
                     cmdargs.add("-a");
                     cmdargs.add(c.getConfigValue("passkey"));
                     cmdargs.add("-o");
                     // We don't use the temp directory for this.
                     // on Windows, our mktorrent.exe binary seems to dislike absolute paths, so we cannot feed it the absolute path of the temp directory
                     cmdargs.add("temp.torrent");
                     cmdargs.add("-p");
                     //cmdargs.add(frxFileName.replace("\\", "/"));
                     cmdargs.add(frxFileName);
                     Process p;
                     ProcessBuilder process = new ProcessBuilder(cmdargs);
                     process.redirectErrorStream(true);
                     p = process.start();
                     p.waitFor();
                     InputStream is = p.getInputStream();
                     InputStreamReader isr = new InputStreamReader(is);
                     BufferedReader br = new BufferedReader(isr);
                     String line, output = "";
                     while ((line = br.readLine()) != null) {
                     System.out.println(line);
                     output += line;
                     }*/
                     if (new File("temp.torrent").exists()) {
                         new File("temp.torrent").delete();
                     }
 
                     CreateTorrent.createTorrent(new File("temp.torrent"), new File(frxFileName), c.getConfigValue("passkey"));
                     if (c.getConfigValue("webui_enabled").equals("yes")) {
                         WebUI wux = new WebUI();
                         wux.addTorrent(new File("temp.torrent"));
                         jLabel13.setText("Done. Uploading to WebUI.");
                     }
                     so.upload(rx, s2, new File("temp.torrent"), assembled);
                 } catch (Exception ex) {
                     Logger.getLogger(UploadFrameView.class.getName()).log(Level.SEVERE, null, ex);
                 }
 
             }
         }.start();
     }
     // Variables declaration - do not modify//GEN-BEGIN:variables
     private static javax.swing.JTabbedPane frxFrame;
     private javax.swing.JButton jButton1;
     private javax.swing.JButton jButton2;
     private javax.swing.JButton jButton3;
     private javax.swing.JCheckBox jCheckBox1;
     private javax.swing.JComboBox jComboBox1;
     private javax.swing.JComboBox jComboBox2;
     private javax.swing.JComboBox jComboBox3;
     private javax.swing.JComboBox jComboBox5;
     private javax.swing.JLabel jLabel1;
     private javax.swing.JLabel jLabel10;
     private javax.swing.JLabel jLabel11;
     private javax.swing.JLabel jLabel12;
     private javax.swing.JLabel jLabel13;
     private javax.swing.JLabel jLabel14;
     private javax.swing.JLabel jLabel15;
     private javax.swing.JLabel jLabel2;
     private javax.swing.JLabel jLabel3;
     private javax.swing.JLabel jLabel4;
     private javax.swing.JLabel jLabel5;
     private javax.swing.JLabel jLabel6;
     private javax.swing.JLabel jLabel7;
     private javax.swing.JLabel jLabel8;
     private javax.swing.JLabel jLabel9;
     private javax.swing.JList jList1;
     private javax.swing.JPanel jPanel1;
     private javax.swing.JPanel jPanel2;
     private javax.swing.JPanel jPanel3;
     private javax.swing.JPanel jPanel4;
     private javax.swing.JPanel jPanel5;
     private javax.swing.JPanel jPanel6;
     private javax.swing.JScrollPane jScrollPane1;
     private javax.swing.JScrollPane jScrollPane2;
     private javax.swing.JScrollPane jScrollPane3;
     private javax.swing.JTabbedPane jTabbedPane1;
     private javax.swing.JTextArea jTextArea1;
     private javax.swing.JTextField jTextField1;
     private javax.swing.JTextField jTextField2;
     private javax.swing.JTextField jTextField3;
     private javax.swing.JTextArea jfDragFile;
     private javax.swing.JPanel mainPanel;
     private javax.swing.JTabbedPane panel2;
     private javax.swing.JProgressBar progressBar;
     private javax.swing.JLabel statusAnimationLabel;
     private javax.swing.JLabel statusMessageLabel;
     private javax.swing.JPanel statusPanel;
     // End of variables declaration//GEN-END:variables
     private final Timer messageTimer;
     private final Timer busyIconTimer;
     private final Icon idleIcon;
     private final Icon[] busyIcons = new Icon[15];
     private int busyIconIndex = 0;
     private JDialog aboutBox;
 }
