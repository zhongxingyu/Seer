 package com.scurab.java.ftpleechergui.window;
 
 import com.scurab.java.ftpleecher.FTPConnection;
 import com.scurab.java.ftpleechergui.Application;
 import com.scurab.java.ftpleechergui.TextUtils;
 
 import javax.swing.*;
 import javax.swing.text.DefaultFormatterFactory;
 import javax.swing.text.NumberFormatter;
 import java.awt.*;
 import java.awt.event.*;
 import java.text.NumberFormat;
 import java.util.ArrayList;
 import java.util.ResourceBundle;
 
 public class OpenConnectionDialog extends JDialog {
     private JPanel contentPane;
     private JButton buttonOK;
     private JButton buttonCancel;
     private JTextField mServer;
     private JTextField mUsername;
     private JPasswordField mPassword;
     private JFormattedTextField mPort;
     private JCheckBox mPassive;
     private JButton mAdd;
     private JComboBox mConnections;
     private JButton mDelete;
     private JCheckBox mFTPs;
 
     public OpenConnectionDialog(final ActionListener okListener) {
         setContentPane(contentPane);
         setModal(true);
         getRootPane().setDefaultButton(buttonOK);
         setTitle(Application.getLabels().getString("OpenConnection"));
 
         setResizable(true);
         pack();
         setMinimumSize(new Dimension(getWidth(), getHeight()));
 
         setLocationRelativeTo(null);
        onLoadConnections();
         if (okListener != null) {
             buttonOK.addActionListener(new AbstractAction() {
                 @Override
                 public void actionPerformed(ActionEvent e) {
                     saveConnections();
                     e.setSource(OpenConnectionDialog.this);
                     okListener.actionPerformed(e);
                 }
             });
         }
 
         buttonCancel.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent e) {
                 onCancel();
             }
         });
 
         setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
         addWindowListener(new WindowAdapter() {
             public void windowClosing(WindowEvent e) {
                 onCancel();
             }
         });
 
         contentPane.registerKeyboardAction(new ActionListener() {
             public void actionPerformed(ActionEvent e) {
                 onCancel();
             }
         }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
 
         mAdd.addActionListener(new AbstractAction() {
             @Override
             public void actionPerformed(ActionEvent e) {
                 onAdd();
             }
         });
         mDelete.addActionListener(new AbstractAction() {
             @Override
             public void actionPerformed(ActionEvent e) {
                 onDelete();
             }
         });
         mConnections.addActionListener(new AbstractAction() {
             @Override
             public void actionPerformed(ActionEvent e) {
                 int index = mConnections.getSelectedIndex();
                 mDelete.setEnabled(index > 0);
                 initValues(index < 1 ? new FTPConnection() : (FTPConnection) mConnections.getItemAt(index));
             }
         });
 
         //port formatter
         NumberFormat nf = NumberFormat.getIntegerInstance();
         nf.setGroupingUsed(false);
         NumberFormatter nff = new NumberFormatter(nf);
         nff.setMinimum(1);
         nff.setMaximum(65535);
         DefaultFormatterFactory factory = new DefaultFormatterFactory(nff);
         mPort.setFormatterFactory(factory);
         mPort.setValue(21);
     }
 
 
     private void onDelete() {
         try {
             int index = mConnections.getSelectedIndex();
             mConnections.removeItemAt(index);
         } catch (Exception e) {
             JOptionPane.showMessageDialog(this, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
         }
     }
 
     private void onAdd() {
         try {
             FTPConnection fc = getValues();
             if (TextUtils.isNullOrEmpty(fc.server)) {
                 throw new Exception("Server is not set");
             }
             if (!(fc.port > 0 && fc.port < 65536)) {
                 throw new Exception("Port can e in range 1 - 65536");
             }
             mConnections.addItem(fc);
         } catch (Exception e) {
             JOptionPane.showMessageDialog(this, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
         }
     }
 
    private void onLoadConnections() {
         FTPConnection[] cons = Application.getInstance().getConnections();
         mConnections.addItem(null);
         if (cons != null) {
             for (FTPConnection fc : cons) {
                 mConnections.addItem(fc);
             }
         }
     }
 
     public FTPConnection getValues() {
         FTPConnection fc = new FTPConnection();
         fc.server = mServer.getText();
         try {
             fc.port = Integer.parseInt(mPort.getText());
         } catch (Exception e) {
             //ignore it
         }
         fc.username = mUsername.getText();
         fc.password = new String(mPassword.getPassword());
         fc.passive = mPassive.isSelected();
         fc.ftps = mFTPs.isSelected();
         return fc;
     }
 
     public void initValues(FTPConnection config) {
         mServer.setText(TextUtils.nz(config.server, ""));
         mPort.setText(config == null ? "21" : String.valueOf(config.port));
         mUsername.setText(TextUtils.nz(config.username, ""));
         mPassword.setText(TextUtils.nz(config.password, ""));
         mPassive.setSelected(config == null ? true : config.passive);
         mFTPs.setSelected(config == null ? false : config.ftps);
     }
 
     private void onCancel() {
         saveConnections();
         dispose();
     }
 
     private void saveConnections() {
         ArrayList<FTPConnection> saved = new ArrayList<FTPConnection>();
         for (int i = 0, n = mConnections.getItemCount(); i < n; i++) {
             Object p = mConnections.getItemAt(i);
             if (p != null && p instanceof FTPConnection) {
                 saved.add((FTPConnection) p);
             }
         }
         Application.getInstance().setConnections(saved.toArray(new FTPConnection[saved.size()]));
     }
 
     {
 // GUI initializer generated by IntelliJ IDEA GUI Designer
 // >>> IMPORTANT!! <<<
 // DO NOT EDIT OR ADD ANY CODE HERE!
         $$$setupUI$$$();
     }
 
     /**
      * Method generated by IntelliJ IDEA GUI Designer
      * >>> IMPORTANT!! <<<
      * DO NOT edit this method OR call it in your code!
      *
      * @noinspection ALL
      */
     private void $$$setupUI$$$() {
         contentPane = new JPanel();
         contentPane.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(2, 1, new Insets(5, 5, 5, 5), -1, -1));
         final JPanel panel1 = new JPanel();
         panel1.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(7, 2, new Insets(0, 0, 0, 0), -1, -1));
         contentPane.add(panel1, new com.intellij.uiDesigner.core.GridConstraints(0, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_BOTH, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
         final JLabel label1 = new JLabel();
         this.$$$loadLabelText$$$(label1, ResourceBundle.getBundle("Labels").getString("Server"));
         panel1.add(label1, new com.intellij.uiDesigner.core.GridConstraints(1, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
         mServer = new JTextField();
         panel1.add(mServer, new com.intellij.uiDesigner.core.GridConstraints(1, 1, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
         final JLabel label2 = new JLabel();
         this.$$$loadLabelText$$$(label2, ResourceBundle.getBundle("Labels").getString("Username"));
         panel1.add(label2, new com.intellij.uiDesigner.core.GridConstraints(3, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
         mUsername = new JTextField();
         panel1.add(mUsername, new com.intellij.uiDesigner.core.GridConstraints(3, 1, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
         final JLabel label3 = new JLabel();
         this.$$$loadLabelText$$$(label3, ResourceBundle.getBundle("Labels").getString("Password"));
         panel1.add(label3, new com.intellij.uiDesigner.core.GridConstraints(4, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
         mPassword = new JPasswordField();
         panel1.add(mPassword, new com.intellij.uiDesigner.core.GridConstraints(4, 1, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
         mPort = new JFormattedTextField();
         mPort.setText("21");
         panel1.add(mPort, new com.intellij.uiDesigner.core.GridConstraints(2, 1, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
         final JLabel label4 = new JLabel();
         this.$$$loadLabelText$$$(label4, ResourceBundle.getBundle("Labels").getString("Port"));
         panel1.add(label4, new com.intellij.uiDesigner.core.GridConstraints(2, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
         final JLabel label5 = new JLabel();
         this.$$$loadLabelText$$$(label5, ResourceBundle.getBundle("Labels").getString("Passive"));
         panel1.add(label5, new com.intellij.uiDesigner.core.GridConstraints(6, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
         mPassive = new JCheckBox();
         mPassive.setEnabled(true);
         mPassive.setSelected(true);
         mPassive.setText("                                                                      ");
         panel1.add(mPassive, new com.intellij.uiDesigner.core.GridConstraints(6, 1, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
         mConnections = new JComboBox();
         panel1.add(mConnections, new com.intellij.uiDesigner.core.GridConstraints(0, 1, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
         final JLabel label6 = new JLabel();
         this.$$$loadLabelText$$$(label6, ResourceBundle.getBundle("Labels").getString("FTPs"));
         panel1.add(label6, new com.intellij.uiDesigner.core.GridConstraints(5, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
         mFTPs = new JCheckBox();
         mFTPs.setSelected(false);
         mFTPs.setText("                                                                      ");
         panel1.add(mFTPs, new com.intellij.uiDesigner.core.GridConstraints(5, 1, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
         final JPanel panel2 = new JPanel();
         panel2.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(1, 4, new Insets(0, 0, 0, 0), -1, -1));
         contentPane.add(panel2, new com.intellij.uiDesigner.core.GridConstraints(1, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_BOTH, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, 1, null, null, null, 0, false));
         final com.intellij.uiDesigner.core.Spacer spacer1 = new com.intellij.uiDesigner.core.Spacer();
         panel2.add(spacer1, new com.intellij.uiDesigner.core.GridConstraints(0, 2, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
         final JPanel panel3 = new JPanel();
         panel3.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(1, 2, new Insets(0, 0, 0, 0), -1, -1));
         panel2.add(panel3, new com.intellij.uiDesigner.core.GridConstraints(0, 3, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_BOTH, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
         buttonCancel = new JButton();
         buttonCancel.setText("Cancel");
         panel3.add(buttonCancel, new com.intellij.uiDesigner.core.GridConstraints(0, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
         buttonOK = new JButton();
         this.$$$loadButtonText$$$(buttonOK, ResourceBundle.getBundle("Labels").getString("Connect"));
         panel3.add(buttonOK, new com.intellij.uiDesigner.core.GridConstraints(0, 1, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
         mAdd = new JButton();
         this.$$$loadButtonText$$$(mAdd, ResourceBundle.getBundle("Labels").getString("AddToList"));
         panel2.add(mAdd, new com.intellij.uiDesigner.core.GridConstraints(0, 1, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
         mDelete = new JButton();
         this.$$$loadButtonText$$$(mDelete, ResourceBundle.getBundle("Labels").getString("Delete"));
         panel2.add(mDelete, new com.intellij.uiDesigner.core.GridConstraints(0, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
     }
 
     /**
      * @noinspection ALL
      */
     private void $$$loadLabelText$$$(JLabel component, String text) {
         StringBuffer result = new StringBuffer();
         boolean haveMnemonic = false;
         char mnemonic = '\0';
         int mnemonicIndex = -1;
         for (int i = 0; i < text.length(); i++) {
             if (text.charAt(i) == '&') {
                 i++;
                 if (i == text.length()) {
                     break;
                 }
                 if (!haveMnemonic && text.charAt(i) != '&') {
                     haveMnemonic = true;
                     mnemonic = text.charAt(i);
                     mnemonicIndex = result.length();
                 }
             }
             result.append(text.charAt(i));
         }
         component.setText(result.toString());
         if (haveMnemonic) {
             component.setDisplayedMnemonic(mnemonic);
             component.setDisplayedMnemonicIndex(mnemonicIndex);
         }
     }
 
     /**
      * @noinspection ALL
      */
     private void $$$loadButtonText$$$(AbstractButton component, String text) {
         StringBuffer result = new StringBuffer();
         boolean haveMnemonic = false;
         char mnemonic = '\0';
         int mnemonicIndex = -1;
         for (int i = 0; i < text.length(); i++) {
             if (text.charAt(i) == '&') {
                 i++;
                 if (i == text.length()) {
                     break;
                 }
                 if (!haveMnemonic && text.charAt(i) != '&') {
                     haveMnemonic = true;
                     mnemonic = text.charAt(i);
                     mnemonicIndex = result.length();
                 }
             }
             result.append(text.charAt(i));
         }
         component.setText(result.toString());
         if (haveMnemonic) {
             component.setMnemonic(mnemonic);
             component.setDisplayedMnemonicIndex(mnemonicIndex);
         }
     }
 
     /**
      * @noinspection ALL
      */
     public JComponent $$$getRootComponent$$$() {
         return contentPane;
     }
 }
