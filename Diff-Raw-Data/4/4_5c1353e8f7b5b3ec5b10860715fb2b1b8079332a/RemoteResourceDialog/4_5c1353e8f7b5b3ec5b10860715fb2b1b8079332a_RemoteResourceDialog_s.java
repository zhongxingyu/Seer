 package org.geworkbench.builtin.projects.remoteresources;
 
 import java.awt.*;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 import javax.swing.*;
 import javax.swing.border.Border;
 import javax.swing.border.EtchedBorder;
 
 
 /**
  * <p>Title: </p>
  *
  * <p>Description: </p>
  *
  * <p>Copyright: Copyright (c) 2005</p>
  *
  * <p>Company: </p>
  *
  * @author not attributable
  * @version 1.0
  */
 public class RemoteResourceDialog extends JDialog {
     private static RemoteResourceManager remoteResourceManager = new
             RemoteResourceManager();
     private static RemoteResourceDialog dialog;
     public static final int ADD = 0;
     public static final int DELETE = 1;
     public static final int EDIT = 2;
     public static int currentOption = 0;
     private static String currentResourceName;
     private String currentURL;
     private String currentUser;
     private String currentPassword;
     private static String previousResourceName;
     public RemoteResourceDialog() {
         try {
             jbInit();
         } catch (Exception ex) {
             ex.printStackTrace();
         }
     }
 
     public static String[] getResourceNames() {
         return remoteResourceManager.getItems();
     }
 
     private RemoteResourceDialog(Frame frame,
                                  String title,
                                  int option,
                                  String initialName) {
         super(frame, title, true);
         currentOption = option;
         currentResourceName = initialName;
         if (option == ADD) {
             clearFields();
         }
         try {
             jbInit();
         } catch (Exception ex) {
             ex.printStackTrace();
         }
         if (option == ADD) {
             clearFields();
             repaint();
         } else if (option == EDIT) {
             setFields(remoteResourceManager.getSelectedResouceByName(
                     initialName));
 
         }
 
     }
 
     private void jbInit() throws Exception {
         jLabel1.setText("Details:");
         jLabel2.setText("Port:");
         jLabel3.setText("User Name:");
         boxLayout21 = new BoxLayout(jPanel2, BoxLayout.Y_AXIS);
 
         jPanel2.setLayout(boxLayout21);
 
         jTextField1.setPreferredSize(new Dimension(60, 20));
         jTextField1.setToolTipText("");
         jTextField1.setText("Manju");
         jLabel4.setText("URL:");
         jPasswordField1.setPreferredSize(new Dimension(60, 22));
         jPasswordField1.setText("jPasswordField1");
         this.getContentPane().setLayout(xYLayout1);
         jPanel1.setLayout(borderLayout1);
 
         jLabel5.setText("Password: ");
         jTextField2.setPreferredSize(new Dimension(122, 20));
         jTextField2.setText("www.columbia.edu");
         jTextField3.setMinimumSize(new Dimension(45, 20));
         jTextField3.setPreferredSize(new Dimension(40, 20));
         jTextField3.setText("80");
         jButton1.setText("OK");
         jButton1.addActionListener(new
                                    RemoteResourceDialog_jButton1_actionAdapter(this));
         jButton6.setText("Cancel");
         jButton6.addActionListener(new
                                    RemoteResourceDialog_jButton6_actionAdapter(this));
         jPanel4.setBorder(null);
         jPanel2.setBorder(BorderFactory.createEtchedBorder());
         jPanel6.setBorder(BorderFactory.createLineBorder(Color.black));
         jLabel6.setText("Protocol:");
         jComboBox1.addActionListener(new
                                      RemoteResourceDialog_jComboBox1_actionAdapter(this));
         shortnameLabel.setText("ShortName:");
         shortnameTextField.setPreferredSize(new Dimension(90, 20));
         shortnameTextField.setText("NCI_CaArray");
         jPanel7.setBorder(BorderFactory.createLineBorder(Color.black));
         boxLayout22 = new BoxLayout(jPanel7, BoxLayout.Y_AXIS);
         jPanel7.setLayout(boxLayout22);
 
         jPanel3.add(jLabel3);
         jPanel3.add(jTextField1);
         jPanel2.add(jPanel3);
         jPanel2.add(jPanel4);
         jPanel4.add(jLabel5);
         jPanel4.add(jPasswordField1);
         jPanel1.add(jLabel1, java.awt.BorderLayout.NORTH);
         jPanel7.add(jPanel5);
         jPanel7.add(jPanel8);
         jPanel7.add(jPanel9);
         jPanel8.add(jLabel6);
         jPanel8.add(jComboBox1);
         jPanel5.add(shortnameLabel);
         jPanel5.add(shortnameTextField);
         jPanel7.add(jPanel2);
         jPanel9.add(jLabel4);
         jPanel9.add(jTextField2);
         jPanel9.add(jLabel2);
         jPanel9.add(jTextField3);
         jPanel6.add(jButton1);
         jPanel6.add(jButton6);
         this.getContentPane().add(jPanel1, BorderLayout.NORTH);
         this.getContentPane().add(jPanel7, BorderLayout.CENTER);
         this.getContentPane().add(jPanel6, BorderLayout.SOUTH);
 
         pack();
     }
 
     JLabel jLabel1 = new JLabel();
     JPanel jPanel1 = new JPanel();
     JPanel jPanel2 = new JPanel();
     BorderLayout borderLayout1 = new BorderLayout();
     JLabel jLabel2 = new JLabel();
     JLabel jLabel3 = new JLabel();
     JPanel jPanel3 = new JPanel();
     JPanel jPanel4 = new JPanel();
     BoxLayout boxLayout21;
     JTextField jTextField1 = new JTextField();
     JLabel jLabel4 = new JLabel();
     JPasswordField jPasswordField1 = new JPasswordField();
     BorderLayout xYLayout1 = new BorderLayout();
     JPanel jPanel6 = new JPanel();
 
     JLabel jLabel5 = new JLabel();
     JTextField jTextField2 = new JTextField();
     JTextField jTextField3 = new JTextField();
     JButton jButton1 = new JButton();
     JButton jButton6 = new JButton();
     Border border1 = BorderFactory.createEtchedBorder(EtchedBorder.RAISED,
             Color.white, new Color(165, 163, 151));
     JLabel jLabel6 = new JLabel();
     JComboBox jComboBox1 = new JComboBox(new String[] {"HTTP", "HTTPS", "RMI"});
     JPanel jPanel7 = new JPanel();
     JLabel shortnameLabel = new JLabel();
     JTextField shortnameTextField = new JTextField();
     JPanel jPanel5 = new JPanel();
     JPanel jPanel8 = new JPanel();
     JPanel jPanel9 = new JPanel();
     BoxLayout boxLayout22;
     /**
      * setOption
      *
      * @param option int
      */
     public void setOption(int option) {
         currentOption = option;
     }
 
     public void setCurrentResourceName(String name) {
         RemoteResource rr = remoteResourceManager.getSelectedResouceByName(name);
         if (rr != null) {
             setFields(rr);
         }
     }
 
     public void setCurrentURL(String currentURL) {
         this.currentURL = currentURL;
     }
 
     public void setCurrentUser(String currentUser) {
         this.currentUser = currentUser;
     }
 
     public void setCurrentPassword(String currentPassword) {
         this.currentPassword = currentPassword;
     }
 
     public void setUser(String user) {
         this.currentUser = user;
     }
 
     public void jComboBox1_actionPerformed(ActionEvent e) {
 
     }
 
     public void jButton4_actionPerformed(ActionEvent e) {
         remoteResourceManager.addRemoteResource(collectResourceInfo());
         dispose();
     }
 
     public RemoteResource collectResourceInfo() {
         RemoteResource rr = new RemoteResource();
 
         String shortname = shortnameTextField.getText().trim();
         String url = jTextField2.getText().trim();
 
         if (shortname.length() == 0) {
             JOptionPane.showMessageDialog(null, "Shortname can not be empty.",
                                           "Error",
                                           JOptionPane.INFORMATION_MESSAGE);
             return null;
         }
         if (url.length() == 0) {
             JOptionPane.showMessageDialog(null, "URL can not be empty.",
                                           "Error",
                                           JOptionPane.INFORMATION_MESSAGE);
             return null;
         }
 
         rr.setConnectProtocal(jComboBox1.getSelectedItem().toString().trim());
         rr.setPassword(new String(jPasswordField1.getPassword()).trim());
         rr.setUsername(jTextField1.getText().trim());
         rr.setUri(url);
         try {
             int portnum = new Integer(jTextField3.getText().trim()).intValue();
             rr.setPortnumber(portnum);
 
         } catch (NumberFormatException e) {
             rr.setPortnumber(80);
         }
 
         rr.setShortname(shortname);
         currentResourceName = shortname;
 
         return rr;
     }
 
     public void clearFields() {
         jPasswordField1.setText("");
         jTextField1.setText("");
         jTextField2.setText("");
         shortnameTextField.setText("");
         jTextField3.setText("80");
 
     }
 
     public void setFields(RemoteResource rr) {
         if (rr != null) {
             jPasswordField1.setText(rr.getPassword());
             jTextField1.setText(rr.getUsername());
             jTextField2.setText(rr.getUri().toString());
             jComboBox1.setSelectedItem(rr.getConnectProtocal());
             shortnameTextField.setText(rr.getShortname());
             jTextField3.setText(new Integer(rr.getPortnumber()).toString());
         }
     }
 
     public void jButton1_actionPerformed(ActionEvent e) {
         RemoteResource rr = collectResourceInfo();
         if (rr != null) {
            if(previousResourceName != null && !previousResourceName.equals(currentResourceName)){
                //remoteResourceManager.deleteRemoteResource(previousResourceName);
             }
             remoteResourceManager.addRemoteResource(rr);
             remoteResourceManager.saveToFile();
 
             dispose();
         }
 
     }
 
     /**
      * Set up and show the dialog.
      */
     public static String showDialog(Component frameComp,
 
                                     String title,
                                     int option,
                                     String initialValue
             ) {
         if(initialValue!=null){
             previousResourceName = initialValue;
         }
         Frame frame = JOptionPane.getFrameForComponent(frameComp);
         dialog = new RemoteResourceDialog(frame,
                                           title,
                                           option,
                                           initialValue);
         dialog.setVisible(true);
         return null;
     }
 
     public String getCurrentResourceName() {
         return currentResourceName;
     }
 
     public String getCurrentURL() {
         return currentURL;
     }
 
     public String getCurrentPassword() {
         return currentPassword;
     }
 
     public String getCurrentUser() {
         return currentUser;
     }
 
     /**Set up system.property
      * setupCurrentResource
      */
     public void setupSystemPropertyForCurrentResource(String resourceName) {
         RemoteResource rr = remoteResourceManager.getSelectedResouceByName(
                 resourceName);
         if (rr != null) {
             currentURL = "//" + rr.getUri() + ":" + rr.getPortnumber();
             currentUser = rr.getUsername();
             currentPassword = rr.getPassword();
 //            System.setProperty("RMIServerURL", url + "/SearchCriteriaHandler");
 //            System.setProperty("SecureSessionManagerURL",
 //                               url + "/SecureSessionManager");
 //            System.setProperty("caarray.mage.user", user);
 //            System.setProperty("caarray.mage.password", password);
 
 
         }
     }
 
 
     public void jButton6_actionPerformed(ActionEvent e) {
         dispose();
     }
 
     /**
      * removeResourceByName
      *
      * @param deleteResourceStr String
      */
     public void removeResourceByName(String deleteResourceStr) {
         remoteResourceManager.deleteRemoteResource(deleteResourceStr);
         remoteResourceManager.getFristItemName();
 
     }
 
 
 }
 
 
 class RemoteResourceDialog_jButton6_actionAdapter implements ActionListener {
     private RemoteResourceDialog adaptee;
     RemoteResourceDialog_jButton6_actionAdapter(RemoteResourceDialog adaptee) {
         this.adaptee = adaptee;
     }
 
     public void actionPerformed(ActionEvent e) {
         adaptee.jButton6_actionPerformed(e);
     }
 }
 
 
 class RemoteResourceDialog_jButton1_actionAdapter implements ActionListener {
     private RemoteResourceDialog adaptee;
     RemoteResourceDialog_jButton1_actionAdapter(RemoteResourceDialog adaptee) {
         this.adaptee = adaptee;
     }
 
     public void actionPerformed(ActionEvent e) {
         adaptee.jButton1_actionPerformed(e);
     }
 }
 
 
 class RemoteResourceDialog_jButton4_actionAdapter implements ActionListener {
     private RemoteResourceDialog adaptee;
     RemoteResourceDialog_jButton4_actionAdapter(RemoteResourceDialog adaptee) {
         this.adaptee = adaptee;
     }
 
     public void actionPerformed(ActionEvent e) {
 
         adaptee.jButton4_actionPerformed(e);
     }
 }
 
 
 class RemoteResourceDialog_jComboBox1_actionAdapter implements ActionListener {
     private RemoteResourceDialog adaptee;
     RemoteResourceDialog_jComboBox1_actionAdapter(RemoteResourceDialog adaptee) {
         this.adaptee = adaptee;
     }
 
     public void actionPerformed(ActionEvent e) {
         adaptee.jComboBox1_actionPerformed(e);
     }
 }
