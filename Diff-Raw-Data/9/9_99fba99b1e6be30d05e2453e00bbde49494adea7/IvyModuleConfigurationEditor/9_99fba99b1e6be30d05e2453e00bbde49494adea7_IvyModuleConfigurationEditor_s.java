 /*
  * Copyright 2011 Stanley Shyiko
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package ivyplug.ui.module;
 
 import com.intellij.ide.util.BrowseFilesListener;
 import com.intellij.openapi.module.Module;
 import com.intellij.openapi.module.ModuleConfigurationEditor;
 import com.intellij.openapi.options.ConfigurationException;
 import com.intellij.openapi.roots.ui.configuration.ModuleConfigurationState;
 import com.intellij.openapi.ui.TextFieldWithBrowseButton;
 import ivyplug.bundles.IvyPlugBundle;
 import ivyplug.ui.PropertiesCompositePanel;
 import org.jetbrains.annotations.Nls;
 import org.jetbrains.annotations.Nullable;
 
 import javax.swing.*;
 import javax.swing.event.ChangeEvent;
 import javax.swing.event.ChangeListener;
 import java.awt.*;
 import java.io.File;
 import java.util.*;
 import java.util.List;
 
 /**
  * @author <a href="mailto:stanley.shyiko@gmail.com">shyiko</a>
  * @since 30.01.2011
  */
 public class IvyModuleConfigurationEditor implements ModuleConfigurationEditor {
 
     private final JPanel rootPanel = new JPanel(new GridBagLayout());
     private final JCheckBox autoDiscoveryCheckBox  =
             new JCheckBox(IvyPlugBundle.message("determine.ivy.descriptor.and.settings.file.automatically"), true);
     private final JLabel ivyXMLLabel = new JLabel(IvyPlugBundle.message("path.to.ivy.xml.file"));
     private final TextFieldWithBrowseButton ivyXML = new TextFieldWithBrowseButton();
     private final JLabel ivySettingsXMLLabel = new JLabel(IvyPlugBundle.message("path.to.ivysettings.xml.file"));
     private final TextFieldWithBrowseButton ivySettingsXML = new TextFieldWithBrowseButton();
     private final PropertiesCompositePanel propertiesCompositePanel = new PropertiesCompositePanel();
 
     private final IvyModuleConfigurationModuleComponent configurationModuleComponent;
 
     public IvyModuleConfigurationEditor(ModuleConfigurationState state) {
         Module module = state.getRootModel().getModule();
         configurationModuleComponent = module.getComponent(IvyModuleConfigurationModuleComponent.class);
     }
 
     public JComponent createComponent() {
         autoDiscoveryCheckBox.addChangeListener(new ChangeListener() {
             public void stateChanged(ChangeEvent e) {
                 onAutoDiscoveryModeChange();
             }
         });
         ivyXML.addActionListener(new BrowseFilesListener(ivyXML.getTextField(),
                 IvyPlugBundle.message("select.ivy.xml.file.location"),
                 IvyPlugBundle.message("selected.file.will.be.used.for.dependencies.resolving"),
                 BrowseFilesListener.SINGLE_FILE_DESCRIPTOR));
         ivySettingsXML.addActionListener(new BrowseFilesListener(ivySettingsXML.getTextField(),
                 IvyPlugBundle.message("select.ivysettings.xml.file.location"),
                 IvyPlugBundle.message("selected.file.will.be.used.for.ivy.configuration"),
                 BrowseFilesListener.SINGLE_FILE_DESCRIPTOR));
         final GridBagConstraints gc = new GridBagConstraints(0, GridBagConstraints.RELATIVE, 1, 1, 1.0, 0.0,
                 GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, new Insets(2, 2, 2, 2), 0, 0);
         rootPanel.add(autoDiscoveryCheckBox, gc);
         rootPanel.add(ivyXMLLabel, gc);
         rootPanel.add(ivyXML, gc);
         rootPanel.add(ivySettingsXMLLabel, gc);
         rootPanel.add(ivySettingsXML, gc);
         rootPanel.add(propertiesCompositePanel, gc);
         gc.weighty = 1.0;
         rootPanel.add(new JPanel(new GridBagLayout()), gc);
         rootPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
         return rootPanel;
     }
 
     public void saveData() {
     }
 
     public void moduleStateChanged() {
     }
 
     @Nls
     public String getDisplayName() {
         return IvyPlugBundle.message("module.ivyplug.configuration");
     }
 
     public Icon getIcon() {
        return null;
     }
 
     public String getHelpTopic() {
         return null;
     }
 
     public boolean isModified() {
         IvyModuleConfiguration configuration = configurationModuleComponent.getConfiguration();
         return !(configuration.isUseAutoDiscovery() == autoDiscoveryCheckBox.isSelected() &&
                  equals(getPath(configuration.getIvyXMlFile()), ivyXML.getText()) &&
                  equals(getPath(configuration.getIvySettingsXMlFile()), ivySettingsXML.getText())) || propertiesCompositePanel.isModified();
     }
 
     public void apply() throws ConfigurationException {
         IvyModuleConfiguration configuration = configurationModuleComponent.getConfiguration();
         configuration.setUseAutoDiscovery(autoDiscoveryCheckBox.isSelected());
         configuration.setIvyXMlFile(getFile(ivyXML.getText()));
         configuration.setIvySettingsXMlFile(getFile(ivySettingsXML.getText()));
         List<String> propertyFilesOriginList = propertiesCompositePanel.getPropertyFiles();
         List<File> propertyFiles = new ArrayList<File>(propertyFilesOriginList.size());
         for (String propertyFile : propertyFilesOriginList) {
             File file = new File(propertyFile);
             if (!file.exists())
                 throw new ConfigurationException(IvyPlugBundle.message("file.doesnt.exist", file.getAbsolutePath()));
             propertyFiles.add(file);
         }
         configuration.setPropertyFiles(propertyFiles);
         configuration.setCustomProperties(propertiesCompositePanel.getCustomProperties());
         propertiesCompositePanel.setUnModified();
     }
 
     public void reset() {
         IvyModuleConfiguration state = configurationModuleComponent.getConfiguration();
         autoDiscoveryCheckBox.setSelected(state.isUseAutoDiscovery());
         ivyXML.setText(getPath(state.getIvyXMlFile()));
         ivySettingsXML.setText(getPath(state.getIvySettingsXMlFile()));
         List<File> propertyFilesOriginList = state.getPropertyFiles();
         List<String> propertyFiles = new ArrayList<String>(propertyFilesOriginList.size());
         for (File file : propertyFilesOriginList) {
             propertyFiles.add(file.getAbsolutePath());
         }
         propertiesCompositePanel.setPropertyFiles(propertyFiles);
         propertiesCompositePanel.setCustomProperties(state.getCustomProperties());
         onAutoDiscoveryModeChange();
     }
 
     public void disposeUIResources() {
     }
 
     private void onAutoDiscoveryModeChange() {
         boolean useAutoDiscoveryMode = autoDiscoveryCheckBox.isSelected();
         ivyXML.setEnabled(!useAutoDiscoveryMode);
         ivySettingsXML.setEnabled(!useAutoDiscoveryMode);
     }
 
     private boolean equals(@Nullable String first, @Nullable String second) {
         if (first == null) {
             return second == null || second.trim().isEmpty();
         }
         if (second == null) {
             return first.trim().isEmpty();
         }
         return first.equals(second);
     }
 
     private File getFile(String filePath) throws ConfigurationException {
         File result = null;
         if (filePath != null && !filePath.trim().isEmpty()) {
             result = new File(filePath);
             if (!result.exists())
                 throw new ConfigurationException(IvyPlugBundle.message("file.doesnt.exist", filePath));
         }
         return result;
     }
 
     private String getPath(File file) {
         String result = null;
         if (file != null) {
             result = file.getAbsolutePath();
         }
         return result;
     }
 
 }
