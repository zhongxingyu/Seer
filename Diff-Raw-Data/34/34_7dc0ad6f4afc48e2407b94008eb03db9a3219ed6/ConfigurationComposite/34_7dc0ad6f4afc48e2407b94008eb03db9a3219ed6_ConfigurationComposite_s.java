 /*
  * Copyright (C) 2008 The Android Open Source Project
  *
  * Licensed under the Eclipse Public License, Version 1.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.eclipse.org/org/documents/epl-v10.php
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package com.android.ide.eclipse.adt.internal.editors.layout.configuration;
 
 import com.android.ide.eclipse.adt.internal.editors.IconFactory;
 import com.android.ide.eclipse.adt.internal.resources.ResourceType;
 import com.android.ide.eclipse.adt.internal.resources.configurations.CountryCodeQualifier;
 import com.android.ide.eclipse.adt.internal.resources.configurations.FolderConfiguration;
 import com.android.ide.eclipse.adt.internal.resources.configurations.LanguageQualifier;
 import com.android.ide.eclipse.adt.internal.resources.configurations.NetworkCodeQualifier;
 import com.android.ide.eclipse.adt.internal.resources.configurations.RegionQualifier;
 import com.android.ide.eclipse.adt.internal.resources.configurations.ResourceQualifier;
 import com.android.ide.eclipse.adt.internal.resources.configurations.ScreenDimensionQualifier;
 import com.android.ide.eclipse.adt.internal.resources.configurations.ScreenOrientationQualifier;
 import com.android.ide.eclipse.adt.internal.resources.configurations.VersionQualifier;
 import com.android.ide.eclipse.adt.internal.resources.configurations.ScreenOrientationQualifier.ScreenOrientation;
 import com.android.ide.eclipse.adt.internal.resources.manager.ProjectResources;
 import com.android.ide.eclipse.adt.internal.sdk.DeviceConfiguration;
 import com.android.ide.eclipse.adt.internal.ui.ConfigurationSelector.LanguageRegionVerifier;
 import com.android.ide.eclipse.adt.internal.ui.ConfigurationSelector.MobileCodeVerifier;
 import com.android.layoutlib.api.IResourceValue;
 import com.android.layoutlib.api.IStyleResourceValue;
 
 import org.eclipse.draw2d.geometry.Rectangle;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.ModifyEvent;
 import org.eclipse.swt.events.ModifyListener;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.events.SelectionListener;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Combo;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.Text;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Map;
 import java.util.Set;
 import java.util.SortedSet;
 
 public class ConfigurationComposite extends Composite {
 
     private final static String THEME_SEPARATOR = "----------"; //$NON-NLS-1$
 
     private Text mCountry;
     private Text mNetwork;
     private Combo mLocale;
     private Combo mDeviceList;
     private Combo mDeviceConfigs;
     private Combo mThemeCombo;
     private Button mCreateButton;
 
     private Label mCountryIcon;
     private Label mNetworkIcon;
 
     private Label mCurrentLayoutLabel;
 
     private Image mMatchImage;
     private Image mErrorImage;
 
     private int mPlatformThemeCount = 0;
     private boolean mDisableUpdates = false;
 
     /** The {@link FolderConfiguration} representing the state of the UI controls */
     private final FolderConfiguration mCurrentConfig = new FolderConfiguration();
 
     private DeviceConfiguration[] mDevices;
 
     private final ArrayList<ResourceQualifier[] > mLocaleList =
         new ArrayList<ResourceQualifier[]>();
 
     private final IConfigListener mListener;
 
     public interface IConfigListener {
         void onConfigurationChange();
         void onThemeChange();
         void onCreate();
 
         ProjectResources getProjectResources();
         ProjectResources getFrameworkResources();
         Map<String, Map<String, IResourceValue>> getConfiguredProjectResources();
         Map<String, Map<String, IResourceValue>> getConfiguredFrameworkResources();
     }
 
     public ConfigurationComposite(IConfigListener listener, Composite parent, int style) {
         super(parent, style);
         mListener = listener;
         mDevices = DeviceConfiguration.getDevices();
 
         IconFactory factory = IconFactory.getInstance();
         mMatchImage = factory.getIcon("match"); //$NON-NLS-1$
         mErrorImage = factory.getIcon("error"); //$NON-NLS-1$
 
         GridLayout gl;
         GridData gd;
         int cols = 10;
 
         setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
         setLayout(gl = new GridLayout(cols, false));
 
         new Label(this, SWT.NONE).setText("MCC");
         mCountryIcon = createControlComposite(this, true /* grab_horizontal */);
         mCountry = new Text(mCountryIcon.getParent(), SWT.BORDER);
         mCountry.setLayoutData(new GridData(
                 GridData.HORIZONTAL_ALIGN_FILL | GridData.GRAB_HORIZONTAL));
         mCountry.addVerifyListener(new MobileCodeVerifier());
         mCountry.addSelectionListener(new SelectionAdapter() {
             @Override
             public void widgetDefaultSelected(SelectionEvent e) {
                 onCountryCodeChange();
             }
         });
         mCountry.addModifyListener(new ModifyListener() {
             public void modifyText(ModifyEvent e) {
                 onCountryCodeChange();
             }
         });
 
         new Label(this, SWT.NONE).setText("MNC");
         mNetworkIcon = createControlComposite(this, true /* grab_horizontal */);
         mNetwork = new Text(mNetworkIcon.getParent(), SWT.BORDER);
         mNetwork.setLayoutData(new GridData(
                 GridData.HORIZONTAL_ALIGN_FILL | GridData.GRAB_HORIZONTAL));
         mNetwork.addVerifyListener(new MobileCodeVerifier());
         mNetwork.addSelectionListener(new SelectionAdapter() {
             @Override
             public void widgetDefaultSelected(SelectionEvent e) {
                 onNetworkCodeChange();
             }
         });
         mNetwork.addModifyListener(new ModifyListener() {
             public void modifyText(ModifyEvent e) {
                 onNetworkCodeChange();
             }
         });
 
         new Label(this, SWT.NONE).setText("Locale");
         mLocale = new Combo(this, SWT.DROP_DOWN | SWT.READ_ONLY);
         mLocale.setLayoutData(new GridData(
                 GridData.HORIZONTAL_ALIGN_FILL | GridData.GRAB_HORIZONTAL));
         mLocale.addVerifyListener(new LanguageRegionVerifier());
         mLocale.addSelectionListener(new SelectionListener() {
             public void widgetDefaultSelected(SelectionEvent e) {
                 onLocaleChange();
             }
             public void widgetSelected(SelectionEvent e) {
                 onLocaleChange();
             }
         });
 
         new Label(this, SWT.NONE).setText("Devices");
         mDeviceList = new Combo(this, SWT.DROP_DOWN | SWT.READ_ONLY);
         // fill with the devices
         for (DeviceConfiguration device : mDevices) {
             mDeviceList.add(device.getName());
         }
 
         mDeviceList.select(0);
         mDeviceList.setLayoutData(new GridData(
                 GridData.HORIZONTAL_ALIGN_FILL | GridData.GRAB_HORIZONTAL));
         mDeviceList.addSelectionListener(new SelectionAdapter() {
             @Override
             public void widgetSelected(SelectionEvent e) {
                 onDeviceChange();
             }
         });
 
         new Label(this, SWT.NONE).setText("Config");
         mDeviceConfigs = new Combo(this, SWT.DROP_DOWN | SWT.READ_ONLY);
         Map<String, FolderConfiguration> configs = mDevices[0].getConfigs();
         Set<String> configNames = configs.keySet();
         for (String name : configNames) {
             mDeviceConfigs.add(name);
         }
         mDeviceConfigs.select(0);
         if (configNames.size() == 1) {
             mDeviceConfigs.setEnabled(false);
         }
         mDeviceConfigs.setLayoutData(new GridData(
                 GridData.HORIZONTAL_ALIGN_FILL | GridData.GRAB_HORIZONTAL));
         mDeviceConfigs.addSelectionListener(new SelectionAdapter() {
             @Override
              public void widgetSelected(SelectionEvent e) {
                 onDeviceConfigChange();
             }
         });
 
         Composite labelParent = new Composite(this, SWT.NONE);
         labelParent.setLayout(gl = new GridLayout(8, false));
         gl.marginWidth = gl.marginHeight = 0;
         labelParent.setLayoutData(gd = new GridData(GridData.FILL_HORIZONTAL));
         gd.horizontalSpan = cols;
 
         new Label(labelParent, SWT.NONE).setText("Editing config:");
         mCurrentLayoutLabel = new Label(labelParent, SWT.NONE);
         mCurrentLayoutLabel.setLayoutData(gd = new GridData(GridData.FILL_HORIZONTAL));
         gd.widthHint = 50;
 
         // first separator
         Label separator = new Label(labelParent, SWT.SEPARATOR | SWT.VERTICAL);
         separator.setLayoutData(gd = new GridData(
                 GridData.VERTICAL_ALIGN_FILL | GridData.GRAB_VERTICAL));
         gd.heightHint = 0;
 
         mThemeCombo = new Combo(labelParent, SWT.READ_ONLY | SWT.DROP_DOWN);
         mThemeCombo.setEnabled(false);
         updateUIFromResources();
         mThemeCombo.addSelectionListener(new SelectionAdapter() {
             @Override
             public void widgetSelected(SelectionEvent e) {
                 onThemeChange();
             }
         });
 
         // second separator
         separator = new Label(labelParent, SWT.SEPARATOR | SWT.VERTICAL);
         separator.setLayoutData(gd = new GridData(
                 GridData.VERTICAL_ALIGN_FILL | GridData.GRAB_VERTICAL));
         gd.heightHint = 0;
 
         mCreateButton = new Button(labelParent, SWT.PUSH | SWT.FLAT);
         mCreateButton.setText("Create...");
         mCreateButton.setEnabled(false);
         mCreateButton.addSelectionListener(new SelectionAdapter() {
             @Override
             public void widgetSelected(SelectionEvent e) {
                 if (mListener != null) {
                     mListener.onCreate();
                 }
             }
         });
 
         onDeviceConfigChange();
     }
 
     public void setConfig(FolderConfiguration config) {
 //        mCurrentConfig.set(config);
         throw new UnsupportedOperationException("setConfig");
     }
 
     public FolderConfiguration getCurrentConfig() {
         return mCurrentConfig;
     }
 
     public void getCurrentConfig(FolderConfiguration config) {
         config.set(mCurrentConfig);
     }
 
     public Rectangle getScreenBounds() {
         // get the orientation from the current device config
         ScreenOrientationQualifier qual = mCurrentConfig.getScreenOrientationQualifier();
         ScreenOrientation orientation = qual.getValue();
 
         // get the device screen dimension
         ScreenDimensionQualifier qual2 = mCurrentConfig.getScreenDimensionQualifier();
         int s1 = qual2.getValue1();
         int s2 = qual2.getValue2();
 
         switch (orientation) {
             default:
             case PORTRAIT:
                 return new Rectangle(0, 0, s2, s1);
             case LANDSCAPE:
                 return new Rectangle(0, 0, s1, s2);
             case SQUARE:
                 return new Rectangle(0, 0, s1, s1);
         }
     }
 
     /**
      * Updates the UI from values in the resources, such as languages, regions, themes, etc...
      * This must be called from the UI thread.
      */
     public void updateUIFromResources() {
         if (mListener == null) {
             return; // can't do anything w/o it.
         }
 
         ProjectResources frameworkProject = mListener.getFrameworkResources();
 
         mDisableUpdates = true;
 
         // Reset stuff
         int selection = mThemeCombo.getSelectionIndex();
         mThemeCombo.removeAll();
         mPlatformThemeCount = 0;
 
         mLocale.removeAll();
         mLocaleList.clear();
 
         SortedSet<String> languages = null;
         ArrayList<String> themes = new ArrayList<String>();
 
         // get the themes, and languages from the Framework.
         if (frameworkProject != null) {
             // get the configured resources for the framework
             Map<String, Map<String, IResourceValue>> frameworResources =
                 mListener.getConfiguredFrameworkResources();
 
             if (frameworResources != null) {
                 // get the styles.
                 Map<String, IResourceValue> styles = frameworResources.get(
                         ResourceType.STYLE.getName());
 
 
                 // collect the themes out of all the styles.
                 for (IResourceValue value : styles.values()) {
                     String name = value.getName();
                     if (name.startsWith("Theme.") || name.equals("Theme")) {
                         themes.add(value.getName());
                         mPlatformThemeCount++;
                     }
                 }
 
                 // sort them and add them to the combo
                 Collections.sort(themes);
 
                 for (String theme : themes) {
                     mThemeCombo.add(theme);
                 }
 
                 mPlatformThemeCount = themes.size();
                 themes.clear();
             }
         }
 
         // now get the themes and languages from the project.
         ProjectResources project = mListener.getProjectResources();
         // in cases where the opened file is not linked to a project, this could be null.
         if (project != null) {
             // get the configured resources for the project
             Map<String, Map<String, IResourceValue>> configuredProjectRes =
                 mListener.getConfiguredProjectResources();
 
             if (configuredProjectRes != null) {
                 // get the styles.
                 Map<String, IResourceValue> styleMap = configuredProjectRes.get(
                         ResourceType.STYLE.getName());
 
                 if (styleMap != null) {
                     // collect the themes out of all the styles, ie styles that extend,
                     // directly or indirectly a platform theme.
                     for (IResourceValue value : styleMap.values()) {
                         if (isTheme(value, styleMap)) {
                             themes.add(value.getName());
                         }
                     }
 
                     // sort them and add them the to the combo.
                     if (mPlatformThemeCount > 0 && themes.size() > 0) {
                         mThemeCombo.add(THEME_SEPARATOR);
                     }
 
                     Collections.sort(themes);
 
                     for (String theme : themes) {
                         mThemeCombo.add(theme);
                     }
                 }
             }
 
             // now get the languages from the project.
             languages = project.getLanguages();
         }
 
         // add the languages to the Combo
         mLocale.add("Default");
         mLocaleList.add(new ResourceQualifier[] { null, null });
 
        if (languages != null && languages.size() > 0) {
             for (String language : languages) {
                 // first the language alone
                 mLocale.add(language);
                 LanguageQualifier qual = new LanguageQualifier(language);
                 mLocaleList.add(new ResourceQualifier[] { qual, null });
 
                 // now find the matching regions and add them
                 SortedSet<String> regions = project.getRegions(language);
                 for (String region : regions) {
                     mLocale.add(String.format("%1$s_%2$s", language, region)); //$NON-NLS-1$
                     RegionQualifier qual2 = new RegionQualifier(region);
                     mLocaleList.add(new ResourceQualifier[] { qual, qual2 });
                 }
 
             }
        } else {
         }
 
         mDisableUpdates = false;
 
         // handle default selection of themes
         if (mThemeCombo.getItemCount() > 0) {
             mThemeCombo.setEnabled(true);
             if (selection == -1) {
                 selection = 0;
             }
 
             if (mThemeCombo.getItemCount() <= selection) {
                 mThemeCombo.select(0);
             } else {
                 mThemeCombo.select(selection);
             }
         } else {
             mThemeCombo.setEnabled(false);
         }
 
         mThemeCombo.getParent().layout();
     }
 
     /**
      * Returns the current theme, or null if the combo has no selection.
      */
     public String getTheme() {
         int themeIndex = mThemeCombo.getSelectionIndex();
         if (themeIndex != -1) {
             return mThemeCombo.getItem(themeIndex);
         }
 
         return null;
     }
 
     /**
      * Returns whether the current theme selection is a project theme.
      * <p/>The returned value is meaningless if {@link #getTheme()} returns <code>null</code>.
      * @return true for project theme, false for framework theme
      */
     public boolean isProjectTheme() {
         return mThemeCombo.getSelectionIndex() >= mPlatformThemeCount;
     }
 
 
     public void setEnabledCreate(boolean enabled) {
         mCreateButton.setEnabled(enabled);
     }
 
     /**
      * Update the UI controls state with a given {@link FolderConfiguration}.
      * <p/>If <var>force</var> is set to <code>true</code> the UI will be changed to exactly reflect
      * <var>config</var>, otherwise, if a qualifier is not present in <var>config</var>,
      * the UI control is not modified. However if the value in the control is not the default value,
      * a warning icon is shown.
      * @param config The {@link FolderConfiguration} to set.
      * @param force Whether the UI should be changed to exactly match the received configuration.
      */
     public void setConfiguration(FolderConfiguration config, boolean force) {
         mDisableUpdates = true; // we do not want to trigger onXXXChange when setting new values in the widgets.
 
         // TODO: find a device that can display this particular config or create a custom one if needed.
 
 
         // update the string showing the folder name
         String current = config.toDisplayString();
         mCurrentLayoutLabel.setText(current != null ? current : "(Default)");
 
         mDisableUpdates = false;
     }
 
     private void onCountryCodeChange() {
         // because mCountry triggers onCountryCodeChange at each modification, calling setText()
         // will trigger notifications, and we don't want that.
         if (mDisableUpdates == true) {
             return;
         }
 
         // update the current config
         String value = mCountry.getText();
 
         // empty string, means no qualifier.
         if (value.length() == 0) {
             mCurrentConfig.setCountryCodeQualifier(null);
         } else {
             try {
                 CountryCodeQualifier qualifier = CountryCodeQualifier.getQualifier(
                         CountryCodeQualifier.getFolderSegment(Integer.parseInt(value)));
                 if (qualifier != null) {
                     mCurrentConfig.setCountryCodeQualifier(qualifier);
                 } else {
                     // Failure! Looks like the value is wrong (for instance a one letter string).
                     // We do nothing in this case.
                     mCountryIcon.setImage(mErrorImage);
                     return;
                 }
             } catch (NumberFormatException e) {
                 // Looks like the code is not a number. This should not happen since the text
                 // field has a VerifyListener that prevents it.
                 mCurrentConfig.setCountryCodeQualifier(null);
                 mCountryIcon.setImage(mErrorImage);
             }
         }
 
         if (mListener != null) {
             mListener.onConfigurationChange();
         }
     }
 
     private void onNetworkCodeChange() {
         // because mNetwork triggers onNetworkCodeChange at each modification, calling setText()
         // will trigger notifications, and we don't want that.
         if (mDisableUpdates == true) {
             return;
         }
 
         // update the current config
         String value = mNetwork.getText();
 
         // empty string, means no qualifier.
         if (value.length() == 0) {
             mCurrentConfig.setNetworkCodeQualifier(null);
         } else {
             try {
                 NetworkCodeQualifier qualifier = NetworkCodeQualifier.getQualifier(
                         NetworkCodeQualifier.getFolderSegment(Integer.parseInt(value)));
                 if (qualifier != null) {
                     mCurrentConfig.setNetworkCodeQualifier(qualifier);
                 } else {
                     // Failure! Looks like the value is wrong (for instance a one letter string).
                     // We do nothing in this case.
                     mNetworkIcon.setImage(mErrorImage);
                     return;
                 }
             } catch (NumberFormatException e) {
                 // Looks like the code is not a number. This should not happen since the text
                 // field has a VerifyListener that prevents it.
                 mCurrentConfig.setNetworkCodeQualifier(null);
                 mNetworkIcon.setImage(mErrorImage);
             }
         }
 
         if (mListener != null) {
             mListener.onConfigurationChange();
         }
     }
 
     /**
      * Call back for language combo selection
      */
     private void onLocaleChange() {
         // because mLanguage triggers onLanguageChange at each modification, the filling
         // of the combo with data will trigger notifications, and we don't want that.
         if (mDisableUpdates == true) {
             return;
         }
 
         int localeIndex = mLocale.getSelectionIndex();
         ResourceQualifier[] localeQualifiers = mLocaleList.get(localeIndex);
 
         mCurrentConfig.setLanguageQualifier((LanguageQualifier)localeQualifiers[0]); // language
         mCurrentConfig.setRegionQualifier((RegionQualifier)localeQualifiers[1]); // region
 
         if (mListener != null) {
             mListener.onConfigurationChange();
         }
     }
 
     private void onDeviceChange() {
 
         int deviceIndex = mDeviceList.getSelectionIndex();
         DeviceConfiguration device = mDevices[deviceIndex];
 
         mDeviceConfigs.removeAll();
 
         Set<String> configNames = device.getConfigs().keySet();
         for (String name : configNames) {
             mDeviceConfigs.add(name);
         }
 
         mDeviceConfigs.select(0);
         if (configNames.size() == 1) {
             mDeviceConfigs.setEnabled(false);
         }
 
         onDeviceConfigChange();
     }
 
     private void onDeviceConfigChange() {
         int deviceIndex = mDeviceList.getSelectionIndex();
         DeviceConfiguration device = mDevices[deviceIndex];
 
         int configIndex = mDeviceConfigs.getSelectionIndex();
         String name = mDeviceConfigs.getItem(configIndex);
         FolderConfiguration config = device.getConfigs().get(name);
 
         // get the current qualifiers from the current config
         CountryCodeQualifier mcc = mCurrentConfig.getCountryCodeQualifier();
         NetworkCodeQualifier mnc = mCurrentConfig.getNetworkCodeQualifier();
         LanguageQualifier lang = mCurrentConfig.getLanguageQualifier();
         RegionQualifier region = mCurrentConfig.getRegionQualifier();
         VersionQualifier version = mCurrentConfig.getVersionQualifier();
 
         // replace the config with the one from the device
         mCurrentConfig.set(config);
 
         // and put back the rest of the qualifiers
         mCurrentConfig.addQualifier(mcc);
         mCurrentConfig.addQualifier(mnc);
         mCurrentConfig.addQualifier(lang);
         mCurrentConfig.addQualifier(region);
         mCurrentConfig.addQualifier(version);
 
         if (mListener != null) {
             mListener.onConfigurationChange();
         }
     }
 
     private void onThemeChange() {
         int themeIndex = mThemeCombo.getSelectionIndex();
         if (themeIndex != -1) {
             String theme = mThemeCombo.getItem(themeIndex);
 
             if (theme.equals(THEME_SEPARATOR)) {
                 mThemeCombo.select(0);
             }
 
             if (mListener != null) {
                 mListener.onThemeChange();
             }
         }
     }
 
     /**
      * Creates a composite with no margin/spacing, and puts a {@link Label} in it with the matching
      * icon.
      * @param parent the parent to receive the composite
      * @return the created {@link Label} object.
      */
     private Label createControlComposite(Composite parent, boolean grab) {
         GridLayout gl;
 
         Composite composite = new Composite(parent, SWT.NONE);
         composite.setLayout(gl = new GridLayout(2, false));
         gl.marginHeight = gl.marginWidth = 0;
         gl.horizontalSpacing = 0;
         if (grab) {
             composite.setLayoutData(
                     new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.GRAB_HORIZONTAL));
         }
 
         // create the label
         Label icon = new Label(composite, SWT.NONE);
         icon.setImage(mMatchImage);
 
         return icon;
     }
 
     /**
      * Returns whether the given <var>style</var> is a theme.
      * This is done by making sure the parent is a theme.
      * @param value the style to check
      * @param styleMap the map of styles for the current project. Key is the style name.
      * @return True if the given <var>style</var> is a theme.
      */
     private boolean isTheme(IResourceValue value, Map<String, IResourceValue> styleMap) {
         if (value instanceof IStyleResourceValue) {
             IStyleResourceValue style = (IStyleResourceValue)value;
 
             boolean frameworkStyle = false;
             String parentStyle = style.getParentStyle();
             if (parentStyle == null) {
                 // if there is no specified parent style we look an implied one.
                 // For instance 'Theme.light' is implied child style of 'Theme',
                 // and 'Theme.light.fullscreen' is implied child style of 'Theme.light'
                 String name = style.getName();
                 int index = name.lastIndexOf('.');
                 if (index != -1) {
                     parentStyle = name.substring(0, index);
                 }
             } else {
                 // remove the useless @ if it's there
                 if (parentStyle.startsWith("@")) {
                     parentStyle = parentStyle.substring(1);
                 }
 
                 // check for framework identifier.
                 if (parentStyle.startsWith("android:")) {
                     frameworkStyle = true;
                     parentStyle = parentStyle.substring("android:".length());
                 }
 
                 // at this point we could have the format style/<name>. we want only the name
                 if (parentStyle.startsWith("style/")) {
                     parentStyle = parentStyle.substring("style/".length());
                 }
             }
 
            if (frameworkStyle) {
                // if the parent is a framework style, it has to be 'Theme' or 'Theme.*'
                return parentStyle.equals("Theme") || parentStyle.startsWith("Theme.");
            } else {
                // if it's a project style, we check this is a theme.
                value = styleMap.get(parentStyle);
                if (value != null) {
                    return isTheme(value, styleMap);
                 }
             }
         }
 
         return false;
     }
 
 }
