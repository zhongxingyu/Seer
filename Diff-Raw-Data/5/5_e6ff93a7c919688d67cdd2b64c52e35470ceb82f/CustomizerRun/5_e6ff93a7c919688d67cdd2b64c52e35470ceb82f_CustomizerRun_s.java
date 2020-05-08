 /*
  * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
  *
  * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
  *
  * The contents of this file are subject to the terms of either the GNU
  * General Public License Version 2 only ("GPL") or the Common
  * Development and Distribution License("CDDL") (collectively, the
  * "License"). You may not use this file except in compliance with the
  * License. You can obtain a copy of the License at
  * http://www.netbeans.org/cddl-gplv2.html
  * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
  * specific language governing permissions and limitations under the
  * License.  When distributing the software, include this License Header
  * Notice in each file and include the License file at
  * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
  * particular file as subject to the "Classpath" exception as provided
  * by Sun in the GPL Version 2 section of the License file that
  * accompanied this code. If applicable, add the following below the
  * License Header, with the fields enclosed by brackets [] replaced by
  * your own identifying information:
  * "Portions Copyrighted [year] [name of copyright owner]"
  *
  * Contributor(s):
  *
  * The Original Software is NetBeans. The Initial Developer of the Original
  * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
  * Microsystems, Inc. All Rights Reserved.
  *
  * If you wish your version of this file to be governed by only the CDDL
  * or only the GPL Version 2, indicate your decision by adding
  * "[Contributor] elects to include this software in this distribution
  * under the [CDDL or GPL Version 2] license." If you do not indicate a
  * single choice of license, a recipient has the option to distribute
  * your version of this file under either the CDDL, the GPL Version 2 or
  * to extend the choice of license to its licensees as provided above.
  * However, if you add GPL Version 2 code and therefore, elected the GPL
  * Version 2 license, then the option applies only if the new code is
  * made subject to such option by the copyright holder.
  */
 
 package org.netbeans.modules.javafx.project.ui.customizer;
 
 import java.awt.Component;
 import java.awt.Dialog;
 import java.awt.Font;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.MouseEvent;
 import java.io.File;
 import java.io.IOException;
 import java.net.URISyntaxException;
 import java.text.Collator;
 import java.util.Comparator;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Properties;
 import java.util.SortedSet;
 import java.util.TreeSet;
 import javax.swing.DefaultComboBoxModel;
 import javax.swing.JButton;
 import javax.swing.JLabel;
 import javax.swing.JList;
 import javax.swing.JPanel;
 import javax.swing.JTextField;
 import javax.swing.ListCellRenderer;
 import javax.swing.SwingUtilities;
 import javax.swing.event.ChangeEvent;
 import javax.swing.event.ChangeListener;
 import javax.swing.event.DocumentEvent;
 import javax.swing.event.DocumentListener;
 import javax.swing.plaf.UIResource;
 import javax.swing.text.JTextComponent;
 import org.netbeans.api.java.platform.JavaPlatform;
 import org.netbeans.api.javafx.platform.JavaFXPlatform;
 import org.netbeans.modules.java.api.common.SourceRoots;
 import org.netbeans.modules.javafx.platform.PlatformUiSupport;
 import org.netbeans.modules.javafx.project.JavaFXProject;
 import org.openide.DialogDescriptor;
 import org.openide.DialogDisplayer;
 import org.openide.NotifyDescriptor;
 import org.openide.awt.MouseUtils;
 import org.openide.filesystems.FileObject;
 import org.openide.filesystems.FileUtil;
 import org.openide.util.HelpCtx;
 import org.openide.util.NbBundle;
 import org.openide.util.RequestProcessor;
 import org.openide.util.Utilities;
 
 public class CustomizerRun extends JPanel implements HelpCtx.Provider {
 
     private JavaFXProject project;
 
     private JTextComponent[] data;
     private JLabel[] dataLabels;
     private String[] keys;
     private Map<String, Map<String, String>> configs;
     JavaFXProjectProperties uiProperties;
 
     public CustomizerRun(JavaFXProjectProperties uiProperties) {
         this.uiProperties = uiProperties;
         initComponents();
 
         this.project = uiProperties.getProject();
 
         PlatformUiSupport.PlatformKey pk = (PlatformUiSupport.PlatformKey)uiProperties.PLATFORM_MODEL.getSelectedItem();
         if (pk != null) {
             JavaPlatform jp = PlatformUiSupport.getPlatform(pk);
             if (jp instanceof JavaFXPlatform) try {
                 jRadioButton4.setEnabled(new File(new File(((JavaFXPlatform)jp).getJavaFXFolder().toURI()), "emulator/bin/preverify" + (Utilities.isWindows() ? ".exe" : "")).isFile()); //NOI18N
             } catch (URISyntaxException e) {}
         }
         
         configs = uiProperties.RUN_CONFIGS;
 
         data = new JTextComponent[]{jTextFieldMainClass, jTextArguments, jvmText, (JTextComponent)deviceCombo.getEditor().getEditorComponent()};
         dataLabels = new JLabel[]{jLabelMainClass, jLabelArguments, jvmLabel, deviceLabel};
         keys = new String[]{JavaFXProjectProperties.MAIN_CLASS, JavaFXProjectProperties.APPLICATION_ARGS, JavaFXProjectProperties.RUN_JVM_ARGS, JavaFXProjectProperties.MOBILE_DEVICE};
         assert data.length == keys.length;
 
         configChanged(uiProperties.activeConfig);
         configUpdated();
 
         configCombo.setRenderer(new ConfigListCellRenderer());
 
         for (int i = 0; i < data.length; i++) {
             final JTextComponent field = data[i];
             final String prop = keys[i];
             final JLabel label = dataLabels[i];
             field.getDocument().addDocumentListener(new DocumentListener() {
                 Font basefont = label.getFont();
                 Font boldfont = basefont.deriveFont(Font.BOLD);
                 {
                     updateFont();
                 }
 
                 public void insertUpdate(DocumentEvent e) {
                     changed();
                 }
 
                 public void removeUpdate(DocumentEvent e) {
                     changed();
                 }
 
                 public void changedUpdate(DocumentEvent e) {
                 }
 
                 void changed() {
                     String config = (String) configCombo.getSelectedItem();
                     if (config.length() == 0) {
                         config = null;
                     }
                     String v = field.getText();
                     if (v != null && config != null && v.equals(configs.get(null).get(prop))) {
                         // default value, do not store as such
                         v = null;
                     }
                     if (v != null && v.equals(""))
                         v = " "; // NOI18N
                     configs.get(config).put(prop, v);
                     updateFont();
                 }
 
                 void updateFont() {
                     String v = field.getText();
                     String config = (String) configCombo.getSelectedItem();
                     if (config.length() == 0) {
                         config = null;
                     }
                     String def = configs.get(null).get(prop);
                     label.setFont(config != null && !Utilities.compareObjects(v != null ? v : "", def != null ? def : "") ? boldfont : basefont); // NOI18N
                 }
             });
         }
         installCheckBox1.addActionListener(new ActionListener() {
             Font basefont = installCheckBox1.getFont();
             Font boldfont = basefont.deriveFont(Font.BOLD);
             {
                 updateFont();
             }
             public void actionPerformed(ActionEvent e) {
                 String config = (String) configCombo.getSelectedItem();
                 if (config.length() == 0) {
                     configs.get(null).put(JavaFXProjectProperties.JAD_INSTALL, String.valueOf(installCheckBox1.isSelected()));
                 } else {
                     configs.get(config).put(JavaFXProjectProperties.JAD_INSTALL, Boolean.parseBoolean(configs.get(null).get(JavaFXProjectProperties.JAD_INSTALL)) != installCheckBox1.isSelected() ? String.valueOf(installCheckBox1.isSelected()) : null);
                 }
                 updateFont();
             }
             void updateFont() {
                 String config = (String) configCombo.getSelectedItem();
                 if (config.length() == 0) {
                     installCheckBox1.setFont(basefont);
                 } else {
                     installCheckBox1.setFont(Boolean.parseBoolean(configs.get(null).get(JavaFXProjectProperties.JAD_INSTALL)) == installCheckBox1.isSelected() ? basefont : boldfont); // NOI18N
                 }
             }
         });
         jButtonMainClass.addActionListener(new MainClassListener(project.getSourceRoots(), jTextFieldMainClass));
     }
 
     public HelpCtx getHelpCtx() {
         return new HelpCtx(CustomizerRun.class);
     }
 
     /** This method is called from within the constructor to
      * initialize the form.
      * WARNING: Do NOT modify this code. The content of this method is
      * always regenerated by the Form Editor.
      */
     // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
     private void initComponents() {
         java.awt.GridBagConstraints gridBagConstraints;
 
         buttonGroup1 = new javax.swing.ButtonGroup();
         configSep = new javax.swing.JSeparator();
         configPanel = new javax.swing.JPanel();
         configLabel = new javax.swing.JLabel();
         configCombo = new javax.swing.JComboBox();
         configNew = new javax.swing.JButton();
         configDel = new javax.swing.JButton();
         mainPanel = new javax.swing.JPanel();
         jLabelMainClass = new javax.swing.JLabel();
         jTextFieldMainClass = new javax.swing.JTextField();
         jButtonMainClass = new javax.swing.JButton();
         jLabelArguments = new javax.swing.JLabel();
         jTextArguments = new javax.swing.JTextField();
         extPanel = new javax.swing.JPanel();
         jLabel1 = new javax.swing.JLabel();
         jRadioButton1 = new javax.swing.JRadioButton();
         jRadioButton2 = new javax.swing.JRadioButton();
         jRadioButton3 = new javax.swing.JRadioButton();
         jRadioButton4 = new javax.swing.JRadioButton();
         deviceLabel = new javax.swing.JLabel();
         deviceCombo = new javax.swing.JComboBox();
         installCheckBox1 = new javax.swing.JCheckBox();
         jvmLabel = new javax.swing.JLabel();
         jvmText = new javax.swing.JTextField();
 
         setLayout(new java.awt.GridBagLayout());
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 1;
         gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
         gridBagConstraints.insets = new java.awt.Insets(6, 0, 6, 0);
         add(configSep, gridBagConstraints);
 
         configPanel.setLayout(new java.awt.GridBagLayout());
 
         configLabel.setLabelFor(configCombo);
         org.openide.awt.Mnemonics.setLocalizedText(configLabel, org.openide.util.NbBundle.getMessage(CustomizerRun.class, "CustomizerRun.configLabel")); // NOI18N
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
         gridBagConstraints.insets = new java.awt.Insets(2, 0, 2, 0);
         configPanel.add(configLabel, gridBagConstraints);
         configLabel.getAccessibleContext().setAccessibleDescription("Configuration profile");
 
         configCombo.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "<default>" }));
         configCombo.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 configComboActionPerformed(evt);
             }
         });
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
         gridBagConstraints.weightx = 1.0;
         gridBagConstraints.insets = new java.awt.Insets(2, 6, 2, 0);
         configPanel.add(configCombo, gridBagConstraints);
 
         org.openide.awt.Mnemonics.setLocalizedText(configNew, org.openide.util.NbBundle.getMessage(CustomizerRun.class, "CustomizerRun.configNew")); // NOI18N
         configNew.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 configNewActionPerformed(evt);
             }
         });
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
         gridBagConstraints.insets = new java.awt.Insets(2, 6, 2, 0);
         configPanel.add(configNew, gridBagConstraints);
         configNew.getAccessibleContext().setAccessibleDescription("Button for creating new profile");
 
         org.openide.awt.Mnemonics.setLocalizedText(configDel, org.openide.util.NbBundle.getMessage(CustomizerRun.class, "CustomizerRun.configDelete")); // NOI18N
         configDel.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 configDelActionPerformed(evt);
             }
         });
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
         gridBagConstraints.insets = new java.awt.Insets(2, 6, 2, 0);
         configPanel.add(configDel, gridBagConstraints);
         configDel.getAccessibleContext().setAccessibleDescription("Deletes current configuration profile");
 
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
         gridBagConstraints.insets = new java.awt.Insets(6, 0, 6, 0);
         add(configPanel, gridBagConstraints);
 
         mainPanel.setLayout(new java.awt.GridBagLayout());
 
         jLabelMainClass.setLabelFor(jTextFieldMainClass);
         org.openide.awt.Mnemonics.setLocalizedText(jLabelMainClass, org.openide.util.NbBundle.getMessage(CustomizerRun.class, "LBL_CustomizeRun_Run_MainClass_JLabel")); // NOI18N
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
         gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
         mainPanel.add(jLabelMainClass, gridBagConstraints);
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
         gridBagConstraints.weightx = 1.0;
         gridBagConstraints.insets = new java.awt.Insets(0, 12, 5, 0);
         mainPanel.add(jTextFieldMainClass, gridBagConstraints);
         jTextFieldMainClass.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getBundle(CustomizerRun.class).getString("AD_jTextFieldMainClass")); // NOI18N
 
         org.openide.awt.Mnemonics.setLocalizedText(jButtonMainClass, org.openide.util.NbBundle.getMessage(CustomizerRun.class, "LBL_CustomizeRun_Run_MainClass_JButton")); // NOI18N
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
         gridBagConstraints.insets = new java.awt.Insets(0, 6, 5, 0);
         mainPanel.add(jButtonMainClass, gridBagConstraints);
         jButtonMainClass.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getBundle(CustomizerRun.class).getString("AD_jButtonMainClass")); // NOI18N
 
         org.openide.awt.Mnemonics.setLocalizedText(jLabelArguments, org.openide.util.NbBundle.getMessage(CustomizerRun.class, "LBL_CustRun_Arguments")); // NOI18N
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
         gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
         mainPanel.add(jLabelArguments, gridBagConstraints);
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
         gridBagConstraints.weightx = 1.0;
         gridBagConstraints.insets = new java.awt.Insets(0, 12, 5, 0);
         mainPanel.add(jTextArguments, gridBagConstraints);
 
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 2;
         gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
         gridBagConstraints.weightx = 1.0;
         gridBagConstraints.insets = new java.awt.Insets(6, 0, 6, 0);
         add(mainPanel, gridBagConstraints);
 
         extPanel.setLayout(new java.awt.GridBagLayout());
 
         org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(CustomizerRun.class, "CustomizerRunComponent.jLabel1.text")); // NOI18N
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
         gridBagConstraints.insets = new java.awt.Insets(12, 0, 5, 0);
         extPanel.add(jLabel1, gridBagConstraints);
 
         buttonGroup1.add(jRadioButton1);
         org.openide.awt.Mnemonics.setLocalizedText(jRadioButton1, org.openide.util.NbBundle.getBundle(CustomizerRun.class).getString("CustomizerRunComponent.jRadioButton1.text")); // NOI18N
         jRadioButton1.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 jRadioButton1ActionPerformed(evt);
             }
         });
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 1;
         gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
         gridBagConstraints.weightx = 1.0;
         extPanel.add(jRadioButton1, gridBagConstraints);
 
         buttonGroup1.add(jRadioButton2);
         org.openide.awt.Mnemonics.setLocalizedText(jRadioButton2, org.openide.util.NbBundle.getBundle(CustomizerRun.class).getString("CustomizerRunComponent.jRadioButton2.text")); // NOI18N
         jRadioButton2.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 jRadioButton2ActionPerformed(evt);
             }
         });
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 2;
         gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
         extPanel.add(jRadioButton2, gridBagConstraints);
 
         buttonGroup1.add(jRadioButton3);
         org.openide.awt.Mnemonics.setLocalizedText(jRadioButton3, org.openide.util.NbBundle.getMessage(CustomizerRun.class, "CustomizerRunComponent.jRadioButton3.text")); // NOI18N
         jRadioButton3.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 jRadioButton3ActionPerformed(evt);
             }
         });
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 3;
         gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
         extPanel.add(jRadioButton3, gridBagConstraints);
 
         buttonGroup1.add(jRadioButton4);
         org.openide.awt.Mnemonics.setLocalizedText(jRadioButton4, org.openide.util.NbBundle.getMessage(CustomizerRun.class, "CustomizerRunComponent.jRadioButton4.text")); // NOI18N
         jRadioButton4.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 jRadioButton4ActionPerformed(evt);
             }
         });
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 4;
         gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
         extPanel.add(jRadioButton4, gridBagConstraints);
 
         org.openide.awt.Mnemonics.setLocalizedText(deviceLabel, org.openide.util.NbBundle.getMessage(CustomizerRun.class, "LBL_MobileDevice")); // NOI18N
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 5;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHEAST;
         gridBagConstraints.insets = new java.awt.Insets(5, 32, 0, 0);
         extPanel.add(deviceLabel, gridBagConstraints);
 
         deviceCombo.setEditable(true);
         deviceCombo.addPopupMenuListener(new javax.swing.event.PopupMenuListener() {
             public void popupMenuCanceled(javax.swing.event.PopupMenuEvent evt) {
             }
             public void popupMenuWillBecomeInvisible(javax.swing.event.PopupMenuEvent evt) {
             }
             public void popupMenuWillBecomeVisible(javax.swing.event.PopupMenuEvent evt) {
                 deviceComboPopupMenuWillBecomeVisible(evt);
             }
         });
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 1;
         gridBagConstraints.gridy = 5;
         gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
         gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
         gridBagConstraints.weightx = 1.0;
         gridBagConstraints.insets = new java.awt.Insets(5, 12, 0, 0);
         extPanel.add(deviceCombo, gridBagConstraints);
 
         org.openide.awt.Mnemonics.setLocalizedText(installCheckBox1, org.openide.util.NbBundle.getMessage(CustomizerRun.class, "LBL_CustomizerRun_InstallPermanently")); // NOI18N
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 1;
         gridBagConstraints.gridy = 6;
         gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
         gridBagConstraints.insets = new java.awt.Insets(5, 12, 0, 0);
         extPanel.add(installCheckBox1, gridBagConstraints);
 
         org.openide.awt.Mnemonics.setLocalizedText(jvmLabel, org.openide.util.NbBundle.getMessage(CustomizerRun.class, "LBL_CustomizerRun_JVMArgs")); // NOI18N
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 7;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
         gridBagConstraints.insets = new java.awt.Insets(12, 0, 0, 0);
         extPanel.add(jvmLabel, gridBagConstraints);
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 1;
         gridBagConstraints.gridy = 7;
         gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
         gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
         gridBagConstraints.weightx = 1.0;
         gridBagConstraints.weighty = 1.0;
         gridBagConstraints.insets = new java.awt.Insets(10, 12, 0, 0);
         extPanel.add(jvmText, gridBagConstraints);
 
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 3;
         gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
         gridBagConstraints.gridheight = java.awt.GridBagConstraints.REMAINDER;
         gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
         gridBagConstraints.weightx = 1.0;
         gridBagConstraints.weighty = 1.0;
         add(extPanel, gridBagConstraints);
     }// </editor-fold>//GEN-END:initComponents
 
     private void configDelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_configDelActionPerformed
         String config = (String) configCombo.getSelectedItem();
         assert config != null;
         configs.put(config, null);
         configChanged(null);
         uiProperties.activeConfig = null;
     }//GEN-LAST:event_configDelActionPerformed
 
     private void configNewActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_configNewActionPerformed
         NotifyDescriptor.InputLine d = new NotifyDescriptor.InputLine(NbBundle.getMessage(CustomizerRun.class, "CustomizerRun.input.prompt"), NbBundle.getMessage(CustomizerRun.class, "CustomizerRun.input.title")); // NOI18N
         if (DialogDisplayer.getDefault().notify(d) != NotifyDescriptor.OK_OPTION) {
             return;
         }
         String name = d.getInputText();
         String config = name.replaceAll("[^a-zA-Z0-9_.-]", "_"); // NOI18N
         if (configs.get(config) != null) {
             DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(NbBundle.getMessage(CustomizerRun.class, "CustomizerRun.input.duplicate", config), NotifyDescriptor.WARNING_MESSAGE)); // NOI18N
             return;
         }
         Map<String, String> m = new HashMap<String, String>();
         if (!name.equals(config)) {
             m.put("$label", name); // NOI18N
         }
         m.put("javafx.profile", "desktop"); //NOI18N
         m.put("execution.target", "standard"); //NOI18N
         configs.put(config, m);
         configChanged(config);
         uiProperties.activeConfig = config;
     }//GEN-LAST:event_configNewActionPerformed
 
     private void configComboActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_configComboActionPerformed
         String config = (String) configCombo.getSelectedItem();
         if (config.length() == 0) {
             config = null;
         }
         configChanged(config);
         uiProperties.activeConfig = config;
         configUpdated();
     }//GEN-LAST:event_configComboActionPerformed
 
     
     
 private void enableDeviceCombo() {
     boolean enabled = jRadioButton4.isEnabled() && jRadioButton4.isSelected();
     deviceLabel.setEnabled(enabled);
     deviceCombo.setEnabled(enabled);
     installCheckBox1.setEnabled(enabled);
 }
     
     
 private void jRadioButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRadioButton1ActionPerformed
     Map<String, String> m = configs.get(uiProperties.activeConfig);
     m.put("javafx.profile", "desktop"); //NOI18N
     m.put("execution.target", "standard"); //NOI18N
     enableDeviceCombo();
 }//GEN-LAST:event_jRadioButton1ActionPerformed
 
 private void jRadioButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRadioButton2ActionPerformed
     Map<String, String> m = configs.get(uiProperties.activeConfig);
     m.put("javafx.profile", "desktop"); //NOI18N
     m.put("execution.target", "jnlp"); //NOI18N
     enableDeviceCombo();
 }//GEN-LAST:event_jRadioButton2ActionPerformed
 
 private void jRadioButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRadioButton3ActionPerformed
     Map<String, String> m = configs.get(uiProperties.activeConfig);
     m.put("javafx.profile", "desktop"); //NOI18N
     m.put("execution.target", "applet"); //NOI18N
     enableDeviceCombo();
 }//GEN-LAST:event_jRadioButton3ActionPerformed
 
 private void jRadioButton4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRadioButton4ActionPerformed
     Map<String, String> m = configs.get(uiProperties.activeConfig);
     m.put("javafx.profile", "mobile"); //NOI18N
     m.put("execution.target", "midp"); //NOI18N
     enableDeviceCombo();
 }//GEN-LAST:event_jRadioButton4ActionPerformed
 
     boolean firstPopup = true;
 
 private void deviceComboPopupMenuWillBecomeVisible(javax.swing.event.PopupMenuEvent evt) {//GEN-FIRST:event_deviceComboPopupMenuWillBecomeVisible
     deviceCombo.setSelectedItem(deviceCombo.getEditor().getItem());
     if (firstPopup) {
         firstPopup = false;
         deviceCombo.addItem(NbBundle.getMessage(CustomizerRun.class, "NODE_PleaseWait"));//NOI18N
         RequestProcessor.getDefault().post(new Runnable() {
                 public void run() {
                     PlatformUiSupport.PlatformKey pk = (PlatformUiSupport.PlatformKey)uiProperties.PLATFORM_MODEL.getSelectedItem();
                     if (pk != null) {
                         JavaPlatform jp = PlatformUiSupport.getPlatform(pk);
                         if (jp instanceof JavaFXPlatform) {
                             FileObject fo = jp.findTool("javafxpackager"); //NOI18N
                             if (fo != null) {
                                 File em = new File(FileUtil.toFile(fo).getParentFile().getParentFile(), "emulator/bin/emulator" + (Utilities.isWindows() ? ".exe" : "")); //NOI18N
                                 if (em.isFile()) try {
                                     Properties p = new Properties();
                                     p.load(Runtime.getRuntime().exec(new String[] {em.getAbsolutePath(), "-Xquery"}).getInputStream()); //NOI18N
                                     final String list = p.getProperty("device.list"); //NOI18N
                                     if (list != null) SwingUtilities.invokeLater(new Runnable() {
                                         public void run() {
                                             Object dev = deviceCombo.getEditor().getItem();
                                             deviceCombo.removeAllItems();
                                             for (String name : list.split(",")) 
                                                 deviceCombo.addItem(name.trim());
                                             deviceCombo.setSelectedItem(dev);
                                             if (deviceCombo.isPopupVisible()) {
                                                 deviceCombo.hidePopup();
                                                 deviceCombo.showPopup();
                                             }
                                         }
                                     });
                                 } catch (IOException e) {}
                             }
                         }
                     }
                     
                 }
             }
         );
     }
 }//GEN-LAST:event_deviceComboPopupMenuWillBecomeVisible
     
     public void configUpdated() {
         Map<String, String> m = configs.get(uiProperties.activeConfig);
         String run = m.get("execution.target"); //NOI18N
         if (run == null || run.equals("standard")) jRadioButton1.setSelected(true); //NOI18N
         else if (run.equals("jnlp")) jRadioButton2.setSelected(true); //NOI18N
         else if (run.equals("applet")) jRadioButton3.setSelected(true); //NOI18N
         else if (run.equals("midp")) jRadioButton4.setSelected(true); //NOI18N
         enableDeviceCombo();
     }
 
     
     
     
     
     private void configChanged(String activeConfig) {
         DefaultComboBoxModel model = new DefaultComboBoxModel();
         model.addElement(""); // NOI18N
         SortedSet<String> alphaConfigs = new TreeSet<String>(new Comparator<String>() {
             Collator coll = Collator.getInstance();
 
             public int compare(String s1, String s2) {
                 return coll.compare(label(s1), label(s2));
             }
 
             private String label(String c) {
                 Map<String, String> m = configs.get(c);
                 String label = m.get("$label"); // NOI18N
                 return label != null ? label : c;
             }
         });
         for (Map.Entry<String, Map<String, String>> entry : configs.entrySet()) {
             String config = entry.getKey();
             if (config != null && entry.getValue() != null) {
                 alphaConfigs.add(config);
             }
         }
         for (String c : alphaConfigs) {
             model.addElement(c);
         }
         configCombo.setModel(model);
         configCombo.setSelectedItem(activeConfig != null ? activeConfig : ""); // NOI18N
         Map<String, String> m = configs.get(activeConfig);
         Map<String, String> def = configs.get(null);
         if (m != null) {
             for (int i = 0; i < data.length; i++) {
                 String v = m.get(keys[i]);
                 if (v == null) {
                     // display default value
                     v = def.get(keys[i]);
                 }
                 data[i].setText(v);
             }
             String v = m.get(JavaFXProjectProperties.JAD_INSTALL);
             if (v == null) {
                 // display default value
                 v = def.get(JavaFXProjectProperties.JAD_INSTALL);
             }
             installCheckBox1.setSelected(Boolean.parseBoolean(v));
             for (ActionListener l : installCheckBox1.getActionListeners()) l.actionPerformed(null);
         } // else ??
         configDel.setEnabled(activeConfig != null);
     }
 
 
     // Variables declaration - do not modify//GEN-BEGIN:variables
     private javax.swing.ButtonGroup buttonGroup1;
     private javax.swing.JComboBox configCombo;
     private javax.swing.JButton configDel;
     private javax.swing.JLabel configLabel;
     private javax.swing.JButton configNew;
     private javax.swing.JPanel configPanel;
     private javax.swing.JSeparator configSep;
     private javax.swing.JComboBox deviceCombo;
     private javax.swing.JLabel deviceLabel;
     private javax.swing.JPanel extPanel;
     private javax.swing.JCheckBox installCheckBox1;
     private javax.swing.JButton jButtonMainClass;
     private javax.swing.JLabel jLabel1;
     private javax.swing.JLabel jLabelArguments;
     private javax.swing.JLabel jLabelMainClass;
     private javax.swing.JRadioButton jRadioButton1;
     private javax.swing.JRadioButton jRadioButton2;
     private javax.swing.JRadioButton jRadioButton3;
     private javax.swing.JRadioButton jRadioButton4;
     private javax.swing.JTextField jTextArguments;
     private javax.swing.JTextField jTextFieldMainClass;
     private javax.swing.JLabel jvmLabel;
     private javax.swing.JTextField jvmText;
     private javax.swing.JPanel mainPanel;
     // End of variables declaration//GEN-END:variables
 
 // End of variables declaration
 // End of variables declaration
     // Innercasses -------------------------------------------------------------
     private class MainClassListener implements ActionListener {
 
         private final JButton okButton;
         private SourceRoots sourceRoots;
         private JTextField mainClassTextField;
 
         MainClassListener(SourceRoots sourceRoots, JTextField mainClassTextField) {
             this.sourceRoots = sourceRoots;
             this.mainClassTextField = mainClassTextField;
             this.okButton = new JButton(NbBundle.getMessage(CustomizerRun.class, "LBL_ChooseMainClass_OK")); // NOI18N
             this.okButton.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(CustomizerRun.class, "AD_ChooseMainClass_OK")); // NOI18N
         }
 
         // Implementation of ActionListener ------------------------------------
         /** Handles button events
          */
         public void actionPerformed(ActionEvent e) {
 
             // only chooseMainClassButton can be performed
             final MainClassChooser panel = new MainClassChooser(sourceRoots.getRoots() ,null , mainClassTextField.getText());
             Object[] options = new Object[]{okButton, DialogDescriptor.CANCEL_OPTION};
             panel.addChangeListener(new ChangeListener() {
 
                 public void stateChanged(ChangeEvent e) {
                     if (e.getSource() instanceof MouseEvent && MouseUtils.isDoubleClick((MouseEvent) e.getSource ())) {
                         // click button and finish the dialog with selected class
                         okButton.doClick();
                     } else {
                         okButton.setEnabled(panel.getSelectedMainClass() != null);
                     }
                 }
             });
             okButton.setEnabled(false);
             DialogDescriptor desc = new DialogDescriptor(panel, NbBundle.getMessage(CustomizerRun.class, "LBL_ChooseMainClass_Title"), true, options, options[0], DialogDescriptor.BOTTOM_ALIGN, null, null); // NOI18N
             //desc.setMessageType (DialogDescriptor.INFORMATION_MESSAGE);
             //desc.setMessageType (DialogDescriptor.INFORMATION_MESSAGE);
             //desc.setMessageType (DialogDescriptor.INFORMATION_MESSAGE);
             //desc.setMessageType (DialogDescriptor.INFORMATION_MESSAGE);
             Dialog dlg = DialogDisplayer.getDefault().createDialog(desc);
             dlg.setVisible(true);
             if (desc.getValue() == options[0]) {
                 mainClassTextField.setText(panel.getSelectedMainClass());
             }
             dlg.dispose();
         }
     }
 
     private final class ConfigListCellRenderer extends JLabel implements ListCellRenderer, UIResource {
 
         public ConfigListCellRenderer() {
             setOpaque(true);
         }
 
         public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
             // #93658: GTK needs name to render cell renderer "natively"
             setName("ComboBox.listRenderer"); // NOI18N
             // NOI18N
             // NOI18N
 // NOI18N
             String config = (String) value;
             String label;
             if (config == null) {
                 // uninitialized?
                 label = null;
             } else if (config.length() > 0) {
                 Map<String, String> m = configs.get(config);
                 label = m != null ? m.get("$label") : null; // NOI18N
                 if (label == null) {
                     label = config;
                 }
             } else {
                 label = NbBundle.getMessage(CustomizerRun.class, "CustomizerRun.default"); // NOI18N
             }
             setText(label);
 
             if (isSelected) {
                 setBackground(list.getSelectionBackground());
                 setForeground(list.getSelectionForeground());
             } else {
                 setBackground(list.getBackground());
                 setForeground(list.getForeground());
             }
 
             return this;
         }
 
         // #93658: GTK needs name to render cell renderer "natively"
         public String getName() {
             String name = super.getName();
             return name == null ? "ComboBox.renderer" : name; // NOI18N
         }
     }
 }
