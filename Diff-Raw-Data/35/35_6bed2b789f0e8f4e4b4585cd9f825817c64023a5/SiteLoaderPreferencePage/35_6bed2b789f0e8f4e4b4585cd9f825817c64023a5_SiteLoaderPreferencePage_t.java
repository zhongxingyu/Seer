 package org.amanzi.neo.preferences;
 
 import org.amanzi.neo.loader.internal.NeoLoaderPlugin;
 import org.amanzi.neo.loader.internal.NeoLoaderPluginMessages;
 import org.eclipse.jface.preference.FieldEditorPreferencePage;
 import org.eclipse.jface.preference.IPreferenceStore;
 import org.eclipse.jface.preference.StringFieldEditor;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Group;
 import org.eclipse.ui.IWorkbench;
 import org.eclipse.ui.IWorkbenchPreferencePage;
 
 public class SiteLoaderPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage{
 
 	@Override
     protected void createFieldEditors() {
         Composite attributePanel = getFieldEditorParent();
         attributePanel.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, true, true));
         attributePanel.setLayout(new GridLayout());
 
         Group attributeGroup = new Group(attributePanel, attributePanel.getStyle());
         attributeGroup.setText(NeoLoaderPluginMessages.PrefSite_title);
         attributeGroup.setLayout(new GridLayout());
         Composite marginPanel = new Composite(attributeGroup, attributeGroup.getStyle());
         GridLayout layout = new GridLayout();
         layout.marginHeight = 15;
         layout.marginWidth = 15;
         marginPanel.setLayout(layout);
 
         int width = 53;
        addField(new StringFieldEditor(DataLoadPreferences.NS_SITE, NeoLoaderPluginMessages.PrefSite_field_site, width,marginPanel));
        addField(new StringFieldEditor(DataLoadPreferences.NS_BEAMWIDTH, NeoLoaderPluginMessages.PrefSite_field_beamwidth, width,marginPanel));
         addField(new StringFieldEditor(DataLoadPreferences.NS_AZIMUTH, NeoLoaderPluginMessages.PrefSite_field_azimuth,width, marginPanel));
     }
 
     @Override
     public void init(IWorkbench workbench) {
     }
 
     @Override
     public IPreferenceStore getPreferenceStore() {
         return NeoLoaderPlugin.getDefault().getPreferenceStore();
     }
 
 }
