 /***************************************************
 *
 * cismet GmbH, Saarbruecken, Germany
 *
 *              ... and it just works.
 *
 ****************************************************/
 /*
  *  Copyright (C) 2010 mscholl
  *
  *  This program is free software: you can redistribute it and/or modify
  *  it under the terms of the GNU General Public License as published by
  *  the Free Software Foundation, either version 3 of the License, or
  *  (at your option) any later version.
  *
  *  This program is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  You should have received a copy of the GNU General Public License
  *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 /*
  * ConnectionTester.java
  *
  * Created on Jul 5, 2010, 4:38:47 PM
  */
 package Sirius.navigator;
 
 import Sirius.navigator.resource.PropertyManager;
 import java.net.MalformedURLException;
 
 import org.apache.log4j.Logger;
 import org.apache.log4j.PropertyConfigurator;
 
 import org.openide.util.NbBundle;
 
 import java.io.BufferedOutputStream;
 import java.io.FileOutputStream;
 
 import java.net.InetSocketAddress;
 import java.net.ProxySelector;
 import java.net.URI;
 
 import java.util.Iterator;
 import java.util.List;
 import java.util.Properties;
 
 import javax.swing.JFileChooser;
 
 import de.cismet.cids.server.ws.rest.RESTfulSerialInterfaceConnector;
 import de.cismet.security.Proxy;
 
 import de.cismet.tools.gui.TextAreaAppender;
 import java.net.URL;
 
 /**
  * DOCUMENT ME!
  *
  * @author   mscholl
  * @version  $Revision$, $Date$
  */
 public class ConnectionTester extends javax.swing.JFrame {
 
     //~ Static fields/initializers ---------------------------------------------
 
     private static final transient Logger LOG = Logger.getLogger(ConnectionTester.class);
 
 
     // Variables declaration - do not modify//GEN-BEGIN:variables
     private javax.swing.JButton btnClear;
     private javax.swing.JButton btnStore;
     private javax.swing.JButton btnTest;
     private javax.swing.JEditorPane jEditorPane1;
     private javax.swing.JLabel jLabel1;
     private javax.swing.JLabel jLabel2;
     private javax.swing.JLabel jLabel3;
     private javax.swing.JLabel jLabel4;
     private javax.swing.JLabel jLabel5;
     private javax.swing.JPanel jPanel1;
     private javax.swing.JScrollPane jScrollPane1;
     private javax.swing.JScrollPane jScrollPane2;
     private javax.swing.JScrollPane jScrollPane3;
     private javax.swing.JScrollPane jScrollPane4;
     private javax.swing.JTabbedPane jTabbedPane1;
     private javax.swing.JTextPane jTextPane1;
     private javax.swing.JPasswordField pwdPassword;
     private javax.swing.JTextArea txaLog;
     private javax.swing.JTextArea txaOut;
     private javax.swing.JTextField txtDomain;
     private javax.swing.JTextField txtProxy;
     private javax.swing.JTextField txtRegex;
     private javax.swing.JTextField txtUsername;
     // End of variables declaration//GEN-END:variables
 
     //~ Constructors -----------------------------------------------------------
 
     /**
      * Creates new form ConnectionTester.
      */
     public ConnectionTester() {
         initComponents();
         initLog();
         init();
     }
 
     //~ Methods ----------------------------------------------------------------
 
     /**
      * DOCUMENT ME!
      */
     private void initLog() {
         TextAreaAppender.setTextArea(txaLog, txtRegex);
 
         final Properties logProperties = new Properties();
         logProperties.put("log4j.rootLogger", "DEBUG, CONSOLE, TEXTAREA");
         logProperties.put("log4j.appender.CONSOLE", "org.apache.log4j.ConsoleAppender");      // A standard console
                                                                                               // appender
         logProperties.put("log4j.appender.CONSOLE.layout", "org.apache.log4j.PatternLayout"); // See:
                                                                                               // http://logging.apache.org/log4j/docs/api/org/apache/log4j/PatternLayout.html
         logProperties.put("log4j.appender.CONSOLE.layout.ConversionPattern", "%d{HH:mm:ss} [%12.12t] %5.5p %c: %m%n");
 
         logProperties.put("log4j.appender.TEXTAREA", "de.cismet.tools.gui.TextAreaAppender");  // Our custom appender
         logProperties.put("log4j.appender.TEXTAREA.layout", "org.apache.log4j.PatternLayout"); // See:
                                                                                                // http://logging.apache.org/log4j/docs/api/org/apache/log4j/PatternLayout.html
         logProperties.put("log4j.appender.TEXTAREA.layout.ConversionPattern", "%d{HH:mm:ss} [%12.12t] %5.5p %c: %m%n");
 
         PropertyConfigurator.configure(logProperties);
     }
 
     /**
      * DOCUMENT ME!
      */
     private void init() {
         final String proxyURL = PropertyManager.getManager().getProxyURL();
         if ((proxyURL == null) || proxyURL.isEmpty()) {
             try {
                 System.setProperty("java.net.useSystemProxies", "true");
                 final List l = ProxySelector.getDefault().select(new URI("http://www.cismet.de/"));
 
                 for (final Iterator iter = l.iterator(); iter.hasNext();) {
                     final java.net.Proxy proxy = (java.net.Proxy)iter.next();
                     final InetSocketAddress addr = (InetSocketAddress)proxy.address();
 
                     if (addr != null) {
                         txtProxy.setText("http://" + addr.getHostName() + ":" + addr.getPort());
                     }
                 }
             } catch (final Exception e) {
                 // skip
             }
         } else {
             txtProxy.setText(proxyURL);
         }
         final String username = PropertyManager.getManager().getProxyUsername();
         if ((username == null) || username.isEmpty()) {
             txtUsername.setText(System.getenv("USERNAME"));
         } else {
             txtUsername.setText(username);
         }
         final String domain = PropertyManager.getManager().getProxyDomain();
         if ((domain == null) || domain.isEmpty()) {
             txtDomain.setText(System.getenv("USERDOMAIN"));
         } else {
             txtDomain.setText(domain);
         }
     }
 
     /**
      * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The
      * content of this method is always regenerated by the Form Editor.
      */
     @SuppressWarnings("unchecked")
     // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
     private void initComponents() {
 
         jScrollPane2 = new javax.swing.JScrollPane();
         jEditorPane1 = new javax.swing.JEditorPane();
         jScrollPane3 = new javax.swing.JScrollPane();
         jTextPane1 = new javax.swing.JTextPane();
         jLabel1 = new javax.swing.JLabel();
         txtProxy = new javax.swing.JTextField();
         jLabel2 = new javax.swing.JLabel();
         txtUsername = new javax.swing.JTextField();
         jLabel3 = new javax.swing.JLabel();
         pwdPassword = new javax.swing.JPasswordField();
         jLabel4 = new javax.swing.JLabel();
         txtDomain = new javax.swing.JTextField();
         btnTest = new javax.swing.JButton();
         btnStore = new javax.swing.JButton();
         jTabbedPane1 = new javax.swing.JTabbedPane();
         jScrollPane1 = new javax.swing.JScrollPane();
         txaOut = new javax.swing.JTextArea();
         jPanel1 = new javax.swing.JPanel();
         jScrollPane4 = new javax.swing.JScrollPane();
         txaLog = new javax.swing.JTextArea();
         jLabel5 = new javax.swing.JLabel();
         txtRegex = new javax.swing.JTextField();
         btnClear = new javax.swing.JButton();
 
         jScrollPane2.setViewportView(jEditorPane1);
 
         jScrollPane3.setViewportView(jTextPane1);
 
         setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
 
         jLabel1.setText(NbBundle.getMessage(ConnectionTester.class, "ConnectionTester.jLabel1.text")); // NOI18N
 
         txtProxy.setText(NbBundle.getMessage(ConnectionTester.class, "ConnectionTester.txtProxy.text")); // NOI18N
 
         jLabel2.setText(NbBundle.getMessage(ConnectionTester.class, "ConnectionTester.jLabel2.text")); // NOI18N
 
         txtUsername.setText(NbBundle.getMessage(ConnectionTester.class, "ConnectionTester.txtUsername.text")); // NOI18N
 
         jLabel3.setText(NbBundle.getMessage(ConnectionTester.class, "ConnectionTester.jLabel3.text")); // NOI18N
 
         pwdPassword.setText(NbBundle.getMessage(ConnectionTester.class, "ConnectionTester.pwdPassword.text")); // NOI18N
 
         jLabel4.setText(NbBundle.getMessage(ConnectionTester.class, "ConnectionTester.jLabel4.text")); // NOI18N
 
         txtDomain.setText(NbBundle.getMessage(ConnectionTester.class, "ConnectionTester.txtDomain.text")); // NOI18N
 
         btnTest.setText(NbBundle.getMessage(ConnectionTester.class, "ConnectionTester.btnTest.text")); // NOI18N
         btnTest.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 btnTestActionPerformed(evt);
             }
         });
 
         btnStore.setText(NbBundle.getMessage(ConnectionTester.class, "ConnectionTester.btnStore.text")); // NOI18N
         btnStore.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 btnStoreActionPerformed(evt);
             }
         });
 
         txaOut.setColumns(20);
         txaOut.setRows(5);
         jScrollPane1.setViewportView(txaOut);
 
         jTabbedPane1.addTab(NbBundle.getMessage(ConnectionTester.class, "ConnectionTester.jScrollPane1.TabConstraints.tabTitle"), jScrollPane1); // NOI18N
 
         txaLog.setColumns(20);
         txaLog.setRows(5);
         txaLog.setAutoscrolls(false);
         jScrollPane4.setViewportView(txaLog);
 
         jLabel5.setText(NbBundle.getMessage(ConnectionTester.class, "ConnectionTester.jLabel5.text")); // NOI18N
 
         txtRegex.setText(NbBundle.getMessage(ConnectionTester.class, "ConnectionTester.txtRegex.text")); // NOI18N
 
         btnClear.setText(NbBundle.getMessage(ConnectionTester.class, "ConnectionTester.btnClear.text")); // NOI18N
         btnClear.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 btnClearActionPerformed(evt);
             }
         });
 
         org.jdesktop.layout.GroupLayout jPanel1Layout = new org.jdesktop.layout.GroupLayout(jPanel1);
         jPanel1.setLayout(jPanel1Layout);
         jPanel1Layout.setHorizontalGroup(
             jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(jPanel1Layout.createSequentialGroup()
                 .addContainerGap()
                 .add(jLabel5)
                 .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                 .add(txtRegex, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 221, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                 .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 212, Short.MAX_VALUE)
                 .add(btnClear))
             .add(jScrollPane4, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 663, Short.MAX_VALUE)
         );
         jPanel1Layout.setVerticalGroup(
             jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(jPanel1Layout.createSequentialGroup()
                 .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                     .add(txtRegex, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                     .add(jLabel5)
                     .add(btnClear))
                 .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                 .add(jScrollPane4, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 351, Short.MAX_VALUE))
         );
 
         jTabbedPane1.addTab(NbBundle.getMessage(ConnectionTester.class, "ConnectionTester.jPanel1.TabConstraints.tabTitle"), jPanel1); // NOI18N
 
         org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(getContentPane());
         getContentPane().setLayout(layout);
         layout.setHorizontalGroup(
             layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(layout.createSequentialGroup()
                 .addContainerGap()
                 .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                     .add(jTabbedPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 684, Short.MAX_VALUE)
                     .add(layout.createSequentialGroup()
                         .add(jLabel1)
                         .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                         .add(txtProxy, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 607, Short.MAX_VALUE))
                     .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                         .add(jLabel2)
                         .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                         .add(txtUsername, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 193, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                         .add(18, 18, 18)
                         .add(jLabel3)
                         .add(18, 18, 18)
                         .add(pwdPassword, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 129, Short.MAX_VALUE)
                         .add(18, 18, 18)
                         .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                             .add(layout.createSequentialGroup()
                                 .add(18, 18, 18)
                                 .add(btnTest)
                                 .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                                 .add(btnStore))
                             .add(layout.createSequentialGroup()
                                 .add(jLabel4)
                                 .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                 .add(txtDomain)))))
                 .addContainerGap())
         );
         layout.setVerticalGroup(
             layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(layout.createSequentialGroup()
                 .addContainerGap()
                 .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                     .add(jLabel1)
                     .add(txtProxy, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                 .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                 .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                     .add(jLabel2)
                     .add(txtUsername, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                     .add(pwdPassword, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                     .add(txtDomain, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                     .add(jLabel4)
                     .add(jLabel3))
                 .add(26, 26, 26)
                 .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                     .add(btnStore)
                     .add(btnTest))
                 .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                 .add(jTabbedPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 436, Short.MAX_VALUE)
                 .addContainerGap())
         );
 
         pack();
     }// </editor-fold>//GEN-END:initComponents
 
     /**
      * DOCUMENT ME!
      *
      * @param  evt  DOCUMENT ME!
      */
     private void btnTestActionPerformed(final java.awt.event.ActionEvent evt)//GEN-FIRST:event_btnTestActionPerformed
     {//GEN-HEADEREND:event_btnTestActionPerformed
         txaOut.setText("");
         final Proxy proxy;
         if ((txtProxy.getText() == null) || txtProxy.getText().trim().isEmpty()) {
             proxy = null;
         } else {
             proxy = new Proxy();
             try
             {
                 final URL url = new URL(txtProxy.getText());
                 proxy.setHost(url.getHost());
                 proxy.setPort(url.getPort());
             }catch(final MalformedURLException ex)
             {
                 throw new IllegalStateException("illegal proxy url", ex);
             }
             proxy.setUsername(txtUsername.getText());
             proxy.setPassword(String.valueOf(pwdPassword.getPassword()));
             proxy.setDomain(txtDomain.getText());
         }
 
         final RESTfulSerialInterfaceConnector connector = new RESTfulSerialInterfaceConnector(
                 "http://callserver-lung.cismet.de/callserver/binary", proxy);
         try {
             txaOut.setText(connector.getUser("WRRL-DB-MV", "Administratoren", "WRRL-DB-MV", "admin", "cismet")
                         .toString() + "\n\nSUCCESS");
         } catch (final Exception e) {
             txaOut.append(e.getMessage() + "\n");
             txaOut.append("STACKTRACE: \n");
             for (final StackTraceElement ste : e.getStackTrace()) {
                 txaOut.append(ste.toString() + "\n");
             }
             txaOut.append("\nFAILURE");
         }
     }//GEN-LAST:event_btnTestActionPerformed
 
     /**
      * DOCUMENT ME!
      *
      * @param  evt  DOCUMENT ME!
      */
     private void btnStoreActionPerformed(final java.awt.event.ActionEvent evt)//GEN-FIRST:event_btnStoreActionPerformed
     {//GEN-HEADEREND:event_btnStoreActionPerformed
         final PropertyManager manager = PropertyManager.getManager();
         manager.setProxyURL(txtProxy.getText());
         manager.setProxyUsername(txtUsername.getText());
         manager.setProxyPassword(String.valueOf(pwdPassword.getPassword()));
         manager.setProxyDomain(txtDomain.getText());
         BufferedOutputStream bos = null;
         try {
             final JFileChooser chooser = new JFileChooser();
             final int answer = chooser.showSaveDialog(this);
             if (answer == JFileChooser.APPROVE_OPTION) {
                 bos = new BufferedOutputStream(new FileOutputStream(chooser.getSelectedFile()));
                 manager.save(bos);
             }
         } catch (final Exception e) {
             LOG.error("cannot save config", e);
         }
     }//GEN-LAST:event_btnStoreActionPerformed
 
     /**
      * DOCUMENT ME!
      *
      * @param  evt  DOCUMENT ME!
      */
     private void btnClearActionPerformed(final java.awt.event.ActionEvent evt)//GEN-FIRST:event_btnClearActionPerformed
     {//GEN-HEADEREND:event_btnClearActionPerformed
         txaLog.setText("");
     }//GEN-LAST:event_btnClearActionPerformed
 
     /**
      * DOCUMENT ME!
      *
      * @param   args  the command line arguments
      *
      * @throws  Exception  DOCUMENT ME!
      */
     public static void main(final String[] args) throws Exception {
         if (args.length > 4) {
             PropertyManager.getManager()
                     .configure(args[1], args[2], args[3], args[4], ((args.length > 5) ? args[5] : null));
         }
         java.awt.EventQueue.invokeLater(new Runnable() {
 
                 @Override
                 public void run() {
                     new ConnectionTester().setVisible(true);
                 }
             });
     }
 }
