 package gphelper;
 
 import java.awt.Toolkit;
 import java.awt.datatransfer.DataFlavor;
 import java.awt.datatransfer.StringSelection;
 import java.awt.datatransfer.Transferable;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 import javax.swing.JLabel;
 import javax.swing.JOptionPane;
 import javax.swing.JPasswordField;
 import javax.swing.JTextField;
 
 
 public class Gphelper extends javax.swing.JFrame {
     
     public Gphelper() {
         initComponents();
         boolean bOk;
         String errText = "";
         SystemCommand cmd = new SystemCommand();
         gpgCommand = "gpg2";
         cmd.setCommand(gpgCommand + " --batch --version");
         bOk = cmd.run();
         if (bOk == false) {
             gpgCommand = "gpg";
             cmd.setCommand(gpgCommand + " --batch --version");
             bOk = cmd.run();
         }
         if (bOk) {
             // get public keys
             cmd.setCommand(gpgCommand + " --batch --list-keys");
             bOk = cmd.run();
             if (bOk) {
                 List<String> stdout = cmd.getStdout();
                 retrievePublicKeys(stdout);
             }
         }
         if (bOk) {
             // get secret keys
             cmd.setCommand(gpgCommand + " --batch --list-secret-keys");
             bOk = cmd.run();
             if (bOk) {
                 List<String> stdout = cmd.getStdout();
                 retrieveSecretKeys(stdout);
             }
         }
         if (bOk) {
             if ((publicKeyIds.size() != publicKeys.size()) ||
                  secretKeyIds.size() != secretKeys.size()) {
                 errText = "Internal error.\nInvalid programmer.";
             }
         }
         if (bOk == false) {
             if (errText.length() == 0) {
                 List<String> stderr = cmd.getStderr();
                 for (int i = 0; i < stderr.size(); i++) {
                     errText = errText + stderr.get(i) + "\n";
                 }
             }
             JOptionPane.showMessageDialog(null,errText,"GPG error",JOptionPane.ERROR_MESSAGE);
             jButtonEncrypt.setEnabled(false);
             jButtonDecrypt.setEnabled(false);
             jMenuEncrypt.setEnabled(false);
             jMenuDecrypt.setEnabled(false);
         }
     }
     
     private void retrievePublicKeys(List<String> list) {
         String line;
         boolean bNewKey = false;
         for (int i = 0; i < list.size(); i++) {
             line = list.get(i);
             if (line.startsWith("pub")) {
                 Pattern p = Pattern.compile("^.*/([^\\s]+).*");
                 Matcher m = p.matcher(line);
                 if (m.matches()) {
                     String str = m.group(1);
                     publicKeyIds.add(str);
                 }
                 bNewKey = true;
             }
             else if (line.startsWith("uid")) {
                 if (bNewKey) {
                     Pattern p = Pattern.compile("uid\\s+(.+)");
                     Matcher m = p.matcher(line);
                     if (m.matches()) {
                         String str = m.group (1);
                         publicKeys.add(str);
                     }
                 }
                 bNewKey = false;
             }
         }
     }
     
     private void retrieveSecretKeys(List<String> list) {
         String line;
         boolean bNewKey = false;
         for (int i = 0; i < list.size(); i++) {
             line = list.get(i);
             if (line.startsWith("sec")) {
                 Pattern p = Pattern.compile("^.*/([^\\s]+).*");
                 Matcher m = p.matcher(line);
                 if (m.matches()) {
                     String str = m.group(1);
                     secretKeyIds.add(str);
                 }
                 bNewKey = true;
             }
             else if (line.startsWith("uid")) {
                 if (bNewKey) {
                     Pattern p = Pattern.compile("uid\\s+(.+)");
                     Matcher m = p.matcher(line);
                     if (m.matches()) {
                         String str = m.group (1);
                         secretKeys.add(str);
                     }
                 }
                 bNewKey = false;
             }
         }
     }
     
     private void cut() {
         copy();
         delete();
     }
     
     private void copy() {
         String text     = jTextArea1.getText();
         int selStart    = jTextArea1.getSelectionStart();
         int selEnd      = jTextArea1.getSelectionEnd();
         
         if (selStart >= 0 && selEnd > selStart) {
             text = jTextArea1.getSelectedText();
         }
         try {
             StringSelection ss = new StringSelection(text);
             Toolkit.getDefaultToolkit().getSystemClipboard().setContents(ss,null);
         } catch( IllegalStateException e1) {
             java.util.logging.Logger.getLogger(Gphelper.class.getName()).log(java.util.logging.Level.SEVERE, null, e1);
         } 
     }
     
     private void paste(){
         // Paste from clipboard
         Transferable t = Toolkit.getDefaultToolkit().getSystemClipboard().getContents(null);
         try {
             if( t!=null && t.isDataFlavorSupported(DataFlavor.stringFlavor) ) {
                 String txt = (String)t.getTransferData(DataFlavor.stringFlavor);
                 jTextArea1.setText(txt);
             } 
         } 
         catch(Exception e1) {
             java.util.logging.Logger.getLogger(Gphelper.class.getName()).log(java.util.logging.Level.SEVERE, null, e1);
         } 
         
     }
 
     private void delete() {
         String text     = jTextArea1.getText();
         int selStart    = jTextArea1.getSelectionStart();
         int selEnd      = jTextArea1.getSelectionEnd();
         if (selStart >= 0 && selEnd > selStart) {
             String text1 = text.substring(0, selStart);
             String text2 = text.substring(selEnd);
             text = text1 + text2; 
             jTextArea1.setText(text);
         }
         else {
             jTextArea1.setText("");
         }
     }
     
     public List<String> getPublicKeys() {
         return publicKeys;
     }
 
     public List<String> getSecretKeys() {
         return secretKeys;
     }
 
     public List<String> getPublicKeyIds() {
         return publicKeyIds;
     }
 
     public List<String> getSecretKeyIds() {
         return secretKeyIds;
     }
 
     public String getGpgText() {
         return jTextArea1.getText();
     }
 
     public void setGpgText(String gpgText) {
         jTextArea1.setText(gpgText);
     }
     
     /* 
      * Encrypt text
      */
     private void encrypt() {
         String errText      = "";
         boolean bOk         = true;
         String Text         = jTextArea1.getText();
         String clearText    = Text;
         String beforeText   = "";
         String afterText    = "";
         int selStart        = jTextArea1.getSelectionStart();
         int selEnd          = jTextArea1.getSelectionEnd();
         
         if (selStart >= 0 && selEnd > selStart) {
             clearText = jTextArea1.getSelectedText();
             beforeText = Text.substring(0, selStart);
             afterText  = Text.substring(selEnd);
         }
         
         if (clearText.length() > 0) {
             JEncryptDialog dlg = new JEncryptDialog(this, true);
             int result = dlg.showDialog();
             if (result == 1) {
                 boolean bSign = dlg.isSigned();
                 int[]   publicKeysIdx = dlg.getSelectedPublicKeys();
                 int     secretKeysIdx = dlg.getSelectedSecretKey();
                 SystemCommand cmd = new SystemCommand();
 
                 if (publicKeysIdx.length > 0) {
                     String command = gpgCommand + " --batch --quiet --encrypt --armor --always-trust";
                     for (int i = 0; i < publicKeysIdx.length; i++) {
                         int idx = publicKeysIdx[i];
                         command = command + " --recipient " + publicKeyIds.get(idx);
                     }
                     if (bSign) {
                         String password = enterPassphrase();   
                         if (password != null) {
                             command = command + " --sign";
                             if (secretKeysIdx != -1) {
                                 command = command + " --default-key " + secretKeyIds.get(secretKeysIdx);
                             }
                             command = command + " --passphrase " + password;
                         }
                         else {
                             bOk = false;
                             errText = "Operation canceled.";
                         }
                     }
                     if (bOk) {
                         cmd.setCommand(command);
                         cmd.setStdin(clearText.trim());
                         bOk = cmd.run();
                     }
                     if (bOk) {
                         String txt = "";
                         List<String> stdout = cmd.getStdout();
                         for (int i = 0; i < stdout.size(); i++) {
                             txt = txt + stdout.get(i) + "\n";
                         }
                         jTextArea1.setText(beforeText + txt + afterText);
                         copy();
                     }
                 }
                 else {
                     bOk = false;
                     errText = "No recipient have been selected.";
                 }
                 if (bOk == false) {
                     if (errText.length() == 0) {
                         List<String> stderr = cmd.getStderr();
                         for (int i = 0; i < stderr.size(); i++) {
                             errText = errText + stderr.get(i) + "\n";
                         }
                     }
                     JOptionPane.showMessageDialog(this,errText,"GPG error",JOptionPane.ERROR_MESSAGE);
                 }
             }
         }
     }
     
     /**
      * Decrypt text
      */
     private void decrypt() {
         String startMarker  = "-----BEGIN PGP MESSAGE-----";
         String endMarker    = "-----END PGP MESSAGE-----";
         String Text         = jTextArea1.getText();
         String cipherText   = Text;
         String beforeText   = "";
         String afterText    = "";
         int selStart        = Text.indexOf(startMarker);
         int selEnd          = Text.indexOf(endMarker);
         
         if (selStart > -1 && selEnd > -1 && selEnd > selStart) {
            selEnd = selEnd + endMarker.length() + 1;
             cipherText = Text.substring(selStart, selEnd);
             beforeText = Text.substring(0, selStart);
             afterText  = Text.substring(selEnd);
         }
 
         if (cipherText.length() > 0) {
             String password = enterPassphrase();
             if (password != null) {
                 SystemCommand cmd = new SystemCommand();
                 cmd.setCommand(gpgCommand + " --decrypt --quiet --batch --always-trust --passphrase " + password);
                 cmd.setStdin(cipherText.trim());
                 boolean bOk = cmd.run();
                 if (bOk) {
                     String txt = "";
                     List<String> stdout = cmd.getStdout();
                     for (int i = 0; i < stdout.size(); i++) {
                         txt = txt + stdout.get(i) + "\n";
                     }
                     jTextArea1.setText(beforeText + txt + afterText);
                     List<String> stderr = cmd.getStderr();
                     for (int i = 0; i < stderr.size(); i++) {
                         if (i == 0) {
                             jTextArea1.append("\n\n");
                         }
                         jTextArea1.append(stderr.get(i) + "\n");
                     }
                 }
                 else {
                     String errText = "";
                     List<String> stderr = cmd.getStderr();
                     for (int i = 0; i < stderr.size(); i++) {
                         errText = errText + stderr.get(i) + "\n";
                     }
                     JOptionPane.showMessageDialog(this,errText,"GPG error",JOptionPane.ERROR_MESSAGE);
                 }
             }
         }
     }
 
     String enterPassphrase() {
         String      password = null;
         JLabel      jLabelPassword = new JLabel("Passphrase");
         JTextField  jTextFieldPassword = new JPasswordField();
         Object[]    ob = {jLabelPassword, jTextFieldPassword};
 
         int result = JOptionPane.showConfirmDialog(this, ob, "Please enter passphrase", JOptionPane.OK_CANCEL_OPTION);
         
         if (result == JOptionPane.OK_OPTION) {
             password = jTextFieldPassword.getText();
         }
         
         return(password);
     }
     
     /**
      * This method is called from within the constructor to initialize the form.
      * WARNING: Do NOT modify this code. The content of this method is always
      * regenerated by the Form Editor.
      */
     @SuppressWarnings("unchecked")
     // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
     private void initComponents() {
 
         jPopupMenu1 = new javax.swing.JPopupMenu();
         jPopupCut = new javax.swing.JMenuItem();
         jPopupCopy = new javax.swing.JMenuItem();
         jPopupPaste = new javax.swing.JMenuItem();
         jPopupDelete = new javax.swing.JMenuItem();
         jButtonEncrypt = new javax.swing.JButton();
         jButtonDecrypt = new javax.swing.JButton();
         jScrollPane1 = new javax.swing.JScrollPane();
         jTextArea1 = new javax.swing.JTextArea();
         jMenuBar1 = new javax.swing.JMenuBar();
         jMenu1 = new javax.swing.JMenu();
         jMenuEncrypt = new javax.swing.JMenuItem();
         jMenuDecrypt = new javax.swing.JMenuItem();
         jSeparator1 = new javax.swing.JPopupMenu.Separator();
         jMenuAbout = new javax.swing.JMenuItem();
         jSeparator2 = new javax.swing.JPopupMenu.Separator();
         jMenuExit = new javax.swing.JMenuItem();
         jMenu2 = new javax.swing.JMenu();
         jMenuCut = new javax.swing.JMenuItem();
         jMenuCopy = new javax.swing.JMenuItem();
         jMenuPaste = new javax.swing.JMenuItem();
         jMenuDelete = new javax.swing.JMenuItem();
 
         jPopupCut.setText("Cut");
         jPopupCut.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 jPopupCutActionPerformed(evt);
             }
         });
         jPopupMenu1.add(jPopupCut);
 
         jPopupCopy.setText("Copy");
         jPopupCopy.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 jPopupCopyActionPerformed(evt);
             }
         });
         jPopupMenu1.add(jPopupCopy);
 
         jPopupPaste.setText("Paste");
         jPopupPaste.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 jPopupPasteActionPerformed(evt);
             }
         });
         jPopupMenu1.add(jPopupPaste);
 
         jPopupDelete.setText("Delete");
         jPopupDelete.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 jPopupDeleteActionPerformed(evt);
             }
         });
         jPopupMenu1.add(jPopupDelete);
 
         setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
         setTitle("GnuPG Helper");
 
         jButtonEncrypt.setText("Encrypt");
         jButtonEncrypt.setToolTipText(" Encrypt text and copy to clipboard");
         jButtonEncrypt.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 jButtonEncryptActionPerformed(evt);
             }
         });
 
         jButtonDecrypt.setText("Decrypt");
         jButtonDecrypt.setToolTipText("Decrypt text");
         jButtonDecrypt.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 jButtonDecryptActionPerformed(evt);
             }
         });
 
         jTextArea1.setColumns(20);
         jTextArea1.setRows(5);
         jTextArea1.setTabSize(4);
         jTextArea1.addMouseListener(new java.awt.event.MouseAdapter() {
             public void mousePressed(java.awt.event.MouseEvent evt) {
                 jTextArea1MousePressed(evt);
             }
             public void mouseReleased(java.awt.event.MouseEvent evt) {
                 jTextArea1MouseReleased(evt);
             }
         });
         jScrollPane1.setViewportView(jTextArea1);
 
         jMenu1.setText("File");
 
         jMenuEncrypt.setMnemonic('E');
         jMenuEncrypt.setText("Encrypt");
         jMenuEncrypt.setToolTipText("Encrypt text");
         jMenuEncrypt.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 jMenuEncryptActionPerformed(evt);
             }
         });
         jMenu1.add(jMenuEncrypt);
 
         jMenuDecrypt.setMnemonic('D');
         jMenuDecrypt.setText("Decrypt");
         jMenuDecrypt.setToolTipText("Decrypt text");
         jMenuDecrypt.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 jMenuDecryptActionPerformed(evt);
             }
         });
         jMenu1.add(jMenuDecrypt);
         jMenu1.add(jSeparator1);
 
         jMenuAbout.setMnemonic('A');
         jMenuAbout.setText("About");
         jMenuAbout.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 jMenuAboutActionPerformed(evt);
             }
         });
         jMenu1.add(jMenuAbout);
         jMenu1.add(jSeparator2);
 
         jMenuExit.setMnemonic('x');
         jMenuExit.setText("Exit");
         jMenuExit.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 jMenuExitActionPerformed(evt);
             }
         });
         jMenu1.add(jMenuExit);
 
         jMenuBar1.add(jMenu1);
 
         jMenu2.setText("Edit");
 
         jMenuCut.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_X, java.awt.event.InputEvent.CTRL_MASK));
         jMenuCut.setText("Cut");
         jMenuCut.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 jMenuCutActionPerformed(evt);
             }
         });
         jMenu2.add(jMenuCut);
 
         jMenuCopy.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_C, java.awt.event.InputEvent.CTRL_MASK));
         jMenuCopy.setText("Copy");
         jMenuCopy.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 jMenuCopyActionPerformed(evt);
             }
         });
         jMenu2.add(jMenuCopy);
 
         jMenuPaste.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_V, java.awt.event.InputEvent.CTRL_MASK));
         jMenuPaste.setText("Paste");
         jMenuPaste.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 jMenuPasteActionPerformed(evt);
             }
         });
         jMenu2.add(jMenuPaste);
 
         jMenuDelete.setText("Delete");
         jMenuDelete.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 jMenuDeleteActionPerformed(evt);
             }
         });
         jMenu2.add(jMenuDelete);
 
         jMenuBar1.add(jMenu2);
 
         setJMenuBar(jMenuBar1);
 
         javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
         getContentPane().setLayout(layout);
         layout.setHorizontalGroup(
             layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(layout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addGroup(layout.createSequentialGroup()
                         .addGap(0, 0, Short.MAX_VALUE)
                         .addComponent(jButtonEncrypt)
                         .addGap(18, 18, 18)
                         .addComponent(jButtonDecrypt))
                     .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 576, Short.MAX_VALUE))
                 .addContainerGap())
         );
         layout.setVerticalGroup(
             layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                 .addContainerGap()
                 .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 509, Short.MAX_VALUE)
                 .addGap(18, 18, 18)
                 .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(jButtonDecrypt)
                     .addComponent(jButtonEncrypt))
                 .addContainerGap())
         );
 
         pack();
     }// </editor-fold>//GEN-END:initComponents
 
     private void jMenuEncryptActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuEncryptActionPerformed
         encrypt();
     }//GEN-LAST:event_jMenuEncryptActionPerformed
 
     private void jMenuDecryptActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuDecryptActionPerformed
         decrypt();
     }//GEN-LAST:event_jMenuDecryptActionPerformed
 
     private void jMenuExitActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuExitActionPerformed
         System.exit(0);
     }//GEN-LAST:event_jMenuExitActionPerformed
 
     private void jMenuDeleteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuDeleteActionPerformed
         delete();
     }//GEN-LAST:event_jMenuDeleteActionPerformed
 
     private void jMenuCutActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuCutActionPerformed
         cut();
     }//GEN-LAST:event_jMenuCutActionPerformed
 
     private void jMenuCopyActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuCopyActionPerformed
         copy();
     }//GEN-LAST:event_jMenuCopyActionPerformed
 
     private void jMenuPasteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuPasteActionPerformed
         paste();
     }//GEN-LAST:event_jMenuPasteActionPerformed
 
     private void jButtonEncryptActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonEncryptActionPerformed
         encrypt();
     }//GEN-LAST:event_jButtonEncryptActionPerformed
 
     private void jButtonDecryptActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonDecryptActionPerformed
         decrypt();
     }//GEN-LAST:event_jButtonDecryptActionPerformed
 
     private void jPopupCutActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jPopupCutActionPerformed
         cut();
     }//GEN-LAST:event_jPopupCutActionPerformed
 
     private void jPopupCopyActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jPopupCopyActionPerformed
         copy();
     }//GEN-LAST:event_jPopupCopyActionPerformed
 
     private void jPopupDeleteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jPopupDeleteActionPerformed
         delete();
     }//GEN-LAST:event_jPopupDeleteActionPerformed
 
     private void jPopupPasteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jPopupPasteActionPerformed
         paste();
     }//GEN-LAST:event_jPopupPasteActionPerformed
 
     private void jTextArea1MousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jTextArea1MousePressed
         checkForTriggerEvent(evt);
     }//GEN-LAST:event_jTextArea1MousePressed
 
     private void jTextArea1MouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jTextArea1MouseReleased
         checkForTriggerEvent(evt);
     }//GEN-LAST:event_jTextArea1MouseReleased
 
     private void jMenuAboutActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuAboutActionPerformed
         new JAboutDialog(this,true).setVisible(true);
     }//GEN-LAST:event_jMenuAboutActionPerformed
     
     private void checkForTriggerEvent(java.awt.event.MouseEvent e) {
         if ( e.isPopupTrigger() ) {
             jPopupMenu1.show( e.getComponent(), e.getX(), e.getY() );
          }
     }
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
                 String str = info.getName();
                 if ("Nimbus".equals(info.getName())) { //Nimbus GTK+
                     javax.swing.UIManager.setLookAndFeel(info.getClassName());
                     //break;
                 }
             }
         } catch (ClassNotFoundException ex) {
             java.util.logging.Logger.getLogger(Gphelper.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
         } catch (InstantiationException ex) {
             java.util.logging.Logger.getLogger(Gphelper.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
         } catch (IllegalAccessException ex) {
             java.util.logging.Logger.getLogger(Gphelper.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
         } catch (javax.swing.UnsupportedLookAndFeelException ex) {
             java.util.logging.Logger.getLogger(Gphelper.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
         }
         
         //</editor-fold>
 
         /* Create and display the form */
         java.awt.EventQueue.invokeLater(new Runnable() {
             @Override
             public void run() {
                 new Gphelper().setVisible(true);
             }
         });
     }
     // Variables declaration - do not modify//GEN-BEGIN:variables
     private javax.swing.JButton jButtonDecrypt;
     private javax.swing.JButton jButtonEncrypt;
     private javax.swing.JMenu jMenu1;
     private javax.swing.JMenu jMenu2;
     private javax.swing.JMenuItem jMenuAbout;
     private javax.swing.JMenuBar jMenuBar1;
     private javax.swing.JMenuItem jMenuCopy;
     private javax.swing.JMenuItem jMenuCut;
     private javax.swing.JMenuItem jMenuDecrypt;
     private javax.swing.JMenuItem jMenuDelete;
     private javax.swing.JMenuItem jMenuEncrypt;
     private javax.swing.JMenuItem jMenuExit;
     private javax.swing.JMenuItem jMenuPaste;
     private javax.swing.JMenuItem jPopupCopy;
     private javax.swing.JMenuItem jPopupCut;
     private javax.swing.JMenuItem jPopupDelete;
     private javax.swing.JPopupMenu jPopupMenu1;
     private javax.swing.JMenuItem jPopupPaste;
     private javax.swing.JScrollPane jScrollPane1;
     private javax.swing.JPopupMenu.Separator jSeparator1;
     private javax.swing.JPopupMenu.Separator jSeparator2;
     private javax.swing.JTextArea jTextArea1;
     // End of variables declaration//GEN-END:variables
     private String gpgCommand;
     private List<String> publicKeys    = new ArrayList<String>();  
     private List<String> secretKeys    = new ArrayList<String>();  
     private List<String> publicKeyIds  = new ArrayList<String>();  
     private List<String> secretKeyIds  = new ArrayList<String>(); 
 
 }
