 package org.intellij.plugins.junitgen.ui;
 
 import com.intellij.openapi.editor.Document;
 import com.intellij.openapi.editor.Editor;
 import com.intellij.openapi.editor.EditorFactory;
 import com.intellij.openapi.fileTypes.FileTypeManager;
 import com.intellij.openapi.project.Project;
 import org.intellij.plugins.junitgen.JUnitGeneratorSettings;
 
 import javax.swing.*;
 import java.awt.*;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.ItemEvent;
 import java.awt.event.ItemListener;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 /**
  * @author JOsborne
  * @since 1/4/12 2:44 PM
  */
 public class JUnitGeneratorConfiguration {
     private JPanel pane;
     private JTabbedPane tabbedPane1;
     private JCheckBox combineGetterAndSetterCheckBox;
     private JCheckBox generateForOverloadedMethodsCheckBox;
     private JComboBox methodGenerationComboBox;
     private JTextField outputTextBox;
     private JComboBox settingsTypeComboBox;
     private JLabel settingsTypeLabel;
     private JButton copyGobalSettingsToButton;
     private JComboBox selectedTemplateComboBox;
     private final List<Editor> velocityEditor = new ArrayList<Editor>();
     private final Project project;
 
     public JUnitGeneratorConfiguration(JUnitGeneratorSettings settings, Project project) {
         this.project = project;
         setSettings(settings);
         updateSettingsVisibility();
         this.settingsTypeComboBox.setVisible(this.project != null);
         this.settingsTypeLabel.setVisible(this.project != null);
         //if we use global settings, then hide the project settings
         this.settingsTypeComboBox.addItemListener(new ItemListener() {
             @Override
             public void itemStateChanged(ItemEvent e) {
                 updateSettingsVisibility();
             }
         });
         copyGobalSettingsToButton.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent e) {
                 if ("copy".equals(e.getActionCommand())) {
                     copyGlobalSettings();
                 }
             }
         });
     }
 
     public JPanel getPane() {
         return pane;
     }
 
     public void setSettings(JUnitGeneratorSettings settings) {
         setGenerateForOverloadedMethods(settings.isGenerateForOverloadedMethods());
         setCombineGetterAndSetter(settings.isCombineGetterAndSetter());
         setListOverloadedMethodsBy(settings.getListOverloadedMethodsBy());
         setOutput(settings.getOutputFilePattern());
         setVmTemplate(settings.getVmTemplates());
         setSelectedVmTemplateKey(settings.getVmTemplates(), settings.getSelectedTemplateKey());
         setUseProjectSettings(settings.isUseProjectSettings());
     }
 
     public boolean isUseProjectSettings() {
         return this.project != null && this.settingsTypeComboBox.getSelectedIndex() > 0;
     }
 
     public void setUseProjectSettings(boolean useProjectSettings) {
         this.settingsTypeComboBox.setSelectedIndex(useProjectSettings ? 1 : 0);
         this.tabbedPane1.setVisible(this.project == null || useProjectSettings);
     }
 
     public boolean isGenerateForOverloadedMethods() {
         return this.generateForOverloadedMethodsCheckBox.isSelected();
     }
 
     public void setGenerateForOverloadedMethods(boolean generateForOverloadedMethods) {
         this.generateForOverloadedMethodsCheckBox.setSelected(generateForOverloadedMethods);
     }
 
     public String getListOverloadedMethodsBy() {
        return this.methodGenerationComboBox.getSelectedItem().toString();
     }
 
     public void setListOverloadedMethodsBy(String listOverloadedMethodsBy) {
         for (int i = 0; i < this.methodGenerationComboBox.getItemCount(); i++) {
             if (this.methodGenerationComboBox.getItemAt(i).equals(listOverloadedMethodsBy)) {
                 this.methodGenerationComboBox.setSelectedIndex(i);
                 return;
             }
         }
         this.methodGenerationComboBox.setSelectedIndex(-1);
     }
 
     public boolean isCombineGetterAndSetter() {
         return this.combineGetterAndSetterCheckBox.isSelected();
     }
 
     public void setCombineGetterAndSetter(boolean combineGetterAndSetter) {
         this.combineGetterAndSetterCheckBox.setSelected(combineGetterAndSetter);
     }
 
     public String getOutput() {
         return this.outputTextBox.getText();
     }
 
     public void setOutput(String output) {
         this.outputTextBox.setText(output);
     }
 
     public String getSelectedTemplateName() {
         return (String) this.selectedTemplateComboBox.getSelectedItem();
     }
 
     public Map<String, String> getVmTemplates() {
         final Map<String, String> contents = new HashMap<String, String>();
         //find all of our editors and read them back
         for (int i = 0; i < tabbedPane1.getTabCount(); i++) {
             final Component component = this.tabbedPane1.getComponentAt(i);
             final Editor editor = getEditor(component);
             if (editor != null) {
                 contents.put(this.tabbedPane1.getTitleAt(i), editor.getDocument().getText());
             }
         }
         return contents;
     }
 
     private Editor getEditor(Component component) {
         for (Editor editor : this.velocityEditor) {
             if (editor.getComponent().equals(component)) {
                 return editor;
             }
         }
         return null;
     }
 
     private Component getComponent(String tabTitle) {
         for (int i = 1; i < this.tabbedPane1.getTabCount(); i++) {
             if (this.tabbedPane1.getTitleAt(i).equals(tabTitle)) {
                 return this.tabbedPane1.getTabComponentAt(i);
             }
         }
         return null;
     }
 
     public void setVmTemplate(Map<String, String> templates) {
         EditorFactory factory = EditorFactory.getInstance();
         //clean up if required
         if (this.velocityEditor.size() != templates.size()) {
             for (Editor editor : this.velocityEditor) {
                 this.tabbedPane1.remove(editor.getComponent());
             }
             this.velocityEditor.clear();
         }
         if (this.velocityEditor.size() == 0) {
             //create the editors
             for (Map.Entry<String, String> entry : templates.entrySet()) {
                 final Document velocityTemplate = factory.createDocument(entry.getValue() != null ? entry.getValue() : "");
                 final Editor editor =
                         factory.createEditor(velocityTemplate, null, FileTypeManager.getInstance().getFileTypeByExtension("vm"), false);
                 this.velocityEditor.add(editor);
                 this.tabbedPane1.addTab(entry.getKey(), editor.getComponent());
             }
         } else {
             //just update them
             for (Map.Entry<String, String> entry : templates.entrySet()) {
                 //grab the components and replace the content since the editor was already built
                 final Editor editor = getEditor(getComponent(entry.getKey()));
                 if (editor != null) {
                     editor.getDocument().setText(entry.getValue());
                 }
             }
         }
     }
 
     private void setSelectedVmTemplateKey(Map<String, String> templates, String selectedTemplateKey) {
         //also update the selected template combo box
         this.selectedTemplateComboBox.removeAllItems();
         for (String title : templates.keySet()) {
             this.selectedTemplateComboBox.addItem(title);
             if (title.equals(selectedTemplateKey))
                 this.selectedTemplateComboBox.setSelectedItem(title);
         }
     }
 
 
     public void updateSettingsVisibility() {
         this.tabbedPane1.setVisible(this.project == null || this.settingsTypeComboBox.getSelectedIndex() > 0);
         this.copyGobalSettingsToButton.setVisible(this.project != null);
     }
 
     /**
      * copy the global settings in but maintain the 'project settings' type
      */
     public void copyGlobalSettings() {
         boolean currentProjectSettingsType = isUseProjectSettings();
         setSettings(JUnitGeneratorSettings.getInstance());
         setUseProjectSettings(currentProjectSettingsType);
     }
 }
