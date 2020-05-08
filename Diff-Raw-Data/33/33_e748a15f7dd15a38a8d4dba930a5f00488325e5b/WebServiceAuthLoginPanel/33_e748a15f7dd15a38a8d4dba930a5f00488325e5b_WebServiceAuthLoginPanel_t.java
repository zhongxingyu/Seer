 /**
  * Project Wonderland
  *
  * Copyright (c) 2004-2009, Sun Microsystems, Inc., All Rights Reserved
  *
  * Redistributions in source code form must reproduce the above
  * copyright and this condition.
  *
  * The contents of this file are subject to the GNU General Public
  * License, Version 2 (the "License"); you may not use this file
  * except in compliance with the License. A copy of the License is
  * available at http://www.opensource.org/licenses/gpl-license.php.
  *
  * Sun designates this particular file as subject to the "Classpath" 
  * exception as provided by Sun in the License file that accompanied 
  * this code.
  */
 package org.jdesktop.wonderland.client.jme.login;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileWriter;
 import java.io.IOException;
import java.io.InputStreamReader;
 import java.io.PrintWriter;
 import java.util.Collections;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Properties;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javax.swing.JPanel;
 import org.jdesktop.wonderland.client.ClientContext;
 import org.jdesktop.wonderland.client.comms.LoginFailureException;
 import org.jdesktop.wonderland.client.jme.login.WonderlandLoginDialog.LoginPanel;
 import org.jdesktop.wonderland.client.jme.login.WonderlandLoginDialog.ValidityListener;
 import org.jdesktop.wonderland.client.login.ServerSessionManager.LoginControl;
 import org.jdesktop.wonderland.client.login.ServerSessionManager.UserPasswordLoginControl;
 
 /**
  *
  * @author jkaplan
  * @author Ronny Standtke <ronny.standtke@fhnw.ch>
  */
 public class WebServiceAuthLoginPanel extends JPanel implements LoginPanel {
 
     private static final Logger LOGGER =
             Logger.getLogger(WebServiceAuthLoginPanel.class.getName());
     private UserPasswordLoginControl control;
     private List<ValidityListener> listeners;
 
     /** Creates new form NoAuthLoginPanel */
     public WebServiceAuthLoginPanel(
             String serverURL, UserPasswordLoginControl control) {
         this.control = control;
 
         initComponents();
         /* DISABLE for cutoff popup fix
         try {
         UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
         } catch (Exception e) {
         }
          */
         // populate with defaults
         String userName = System.getProperty("user.name");
         userName = userName.replaceAll("\\s", "_");
         setUsername(userName);
 
         // override with any saved credentials
         loadCredentials();
 
         // set the server location
         setServer(serverURL);
 
         // notify validity listeners of validity of initial state
         notifyValidityListeners();
     }
 
     public JPanel getPanel() {
         return this;
     }
 
     public LoginControl getLoginControl() {
         return control;
     }
 
     public void setUsername(String username) {
         wsUsernameField.setText(username);
     }
 
     public void setPassword(String password) {
         wsPasswordField.setText(password);
     }
 
     public void setServer(String server) {
         naServerField.setText(server);
     }
 
     public void addValidityListener(ValidityListener listener) {
         if (listeners == null) {
             listeners = Collections.synchronizedList(new LinkedList());
         }
         listeners.add(listener);
     }
 
     public void removeValidityListener(ValidityListener listener) {
         if (listeners != null) {
             listeners.remove(listener);
         }
     }
 
     public void notifyValidityListeners() {
         if (listeners != null) {
             boolean isValid = wsUsernameField.getDocument().getLength() > 0;
 
             Iterator<ValidityListener> iter = listeners.iterator();
             while (iter.hasNext()) {
                 iter.next().setValidity(isValid);
             }
         }
     }
 
     public String doLogin() {
         String username = wsUsernameField.getText();
         String password = new String(wsPasswordField.getPassword());
         try {
             control.authenticate(username, password);
 
             // If we got here, it means login succeeded.  Save the successful
             // login information.
             storeCredentials(username);
             return null;
         } catch (LoginFailureException lfe) {
             LOGGER.log(Level.WARNING, "Login failed", lfe);
             return lfe.getMessage();
         }
     }
 
     public void cancel() {
         control.cancel();
     }
 
     protected void storeCredentials(String username) {
         Properties props = new Properties();
         props.put("username", username);
 
         File configDir = ClientContext.getUserDirectory("config");
 
         try {
            File outFile = new File(configDir, "login.properties");
            PrintWriter writer = new PrintWriter(outFile, "UTF8");
            props.list(writer);
            writer.close();
         } catch (IOException ioe) {
             LOGGER.log(Level.WARNING, "Error writing login data", ioe);
         }
     }
 
     protected void loadCredentials() {
         File configDir = ClientContext.getUserDirectory("config");
         File propsFile = new File(configDir, "login.properties");
         if (!propsFile.exists()) {
             return;
         }
 
         try {
            FileInputStream inStream = new FileInputStream(propsFile);
            InputStreamReader inReader = new InputStreamReader(inStream, "UTF8");
 
             Properties props = new Properties();
             props.load(inReader);
             setUsername(props.getProperty("username"));
         } catch (IOException ioe) {
             LOGGER.log(Level.WARNING, "Error reading login data", ioe);
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
 
         naServerLabel = new javax.swing.JLabel();
         naFullNameLabel = new javax.swing.JLabel();
         naUsernameLabel = new javax.swing.JLabel();
         naServerField = new javax.swing.JTextField();
         wsUsernameField = new javax.swing.JTextField();
         wsPasswordField = new javax.swing.JPasswordField();
 
         setOpaque(false);
 
         naServerLabel.setFont(naServerLabel.getFont().deriveFont(naServerLabel.getFont().getStyle() | java.awt.Font.BOLD));
         java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("org/jdesktop/wonderland/client/jme/login/Bundle"); // NOI18N
         naServerLabel.setText(bundle.getString("WebServiceAuthLoginPanel.naServerLabel.text")); // NOI18N
 
         naFullNameLabel.setFont(naFullNameLabel.getFont().deriveFont(naFullNameLabel.getFont().getStyle() | java.awt.Font.BOLD));
         naFullNameLabel.setText(bundle.getString("WebServiceAuthLoginPanel.naFullNameLabel.text")); // NOI18N
 
         naUsernameLabel.setFont(naUsernameLabel.getFont().deriveFont(naUsernameLabel.getFont().getStyle() | java.awt.Font.BOLD));
         naUsernameLabel.setText(bundle.getString("WebServiceAuthLoginPanel.naUsernameLabel.text")); // NOI18N
 
         naServerField.setEditable(false);
         naServerField.setMinimumSize(new java.awt.Dimension(98, 22));
 
         wsUsernameField.setFont(new java.awt.Font("Dialog", 0, 13)); // NOI18N
         wsUsernameField.setMinimumSize(new java.awt.Dimension(98, 22));
         wsUsernameField.addKeyListener(new java.awt.event.KeyAdapter() {
             public void keyReleased(java.awt.event.KeyEvent evt) {
                 wsUsernameFieldKeyReleased(evt);
             }
         });
 
         org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
         this.setLayout(layout);
         layout.setHorizontalGroup(
             layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(layout.createSequentialGroup()
                 .addContainerGap()
                 .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                     .add(naFullNameLabel)
                     .add(naUsernameLabel)
                     .add(naServerLabel))
                 .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                 .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                     .add(wsUsernameField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 266, Short.MAX_VALUE)
                     .add(wsPasswordField)
                     .add(naServerField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                 .addContainerGap(14, Short.MAX_VALUE))
         );
         layout.setVerticalGroup(
             layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(layout.createSequentialGroup()
                 .addContainerGap()
                 .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                     .add(naUsernameLabel)
                     .add(wsUsernameField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                 .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                 .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                     .add(naFullNameLabel)
                     .add(wsPasswordField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                 .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                 .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                     .add(naServerField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                     .add(naServerLabel))
                 .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
         );
     }// </editor-fold>//GEN-END:initComponents
 
     private void wsUsernameFieldKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_wsUsernameFieldKeyReleased
         notifyValidityListeners();
 }//GEN-LAST:event_wsUsernameFieldKeyReleased
     // Variables declaration - do not modify//GEN-BEGIN:variables
     private javax.swing.JLabel naFullNameLabel;
     private javax.swing.JTextField naServerField;
     private javax.swing.JLabel naServerLabel;
     private javax.swing.JLabel naUsernameLabel;
     private javax.swing.JPasswordField wsPasswordField;
     private javax.swing.JTextField wsUsernameField;
     // End of variables declaration//GEN-END:variables
 }
