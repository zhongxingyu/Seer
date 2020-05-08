 /******************************************************************************* 
  * Copyright (c) 2008 Red Hat, Inc. 
  * Distributed under license by Red Hat, Inc. All rights reserved. 
  * This program is made available under the terms of the 
  * Eclipse Public License v1.0 which accompanies this distribution, 
  * and is available at http://www.eclipse.org/legal/epl-v10.html 
  * 
  * Contributors: 
  * Red Hat, Inc. - initial API and implementation 
  ******************************************************************************/
 
 package org.jboss.tools.ws.ui.preferences;
 
 import java.io.File;
 
 import org.eclipse.jface.preference.IPreferenceStore;
 import org.eclipse.jface.preference.PreferencePage;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.ModifyEvent;
 import org.eclipse.swt.events.ModifyListener;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.swt.widgets.DirectoryDialog;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.swt.widgets.TabFolder;
 import org.eclipse.swt.widgets.TabItem;
 import org.eclipse.swt.widgets.Text;
 import org.eclipse.ui.IWorkbench;
 import org.eclipse.ui.IWorkbenchPreferencePage;
 import org.jboss.tools.ws.core.JbossWSCorePlugin;
 import org.jboss.tools.ws.ui.JbossWSUIMessages;
 import org.jboss.tools.ws.ui.JbossWSUIPlugin;
 import org.jboss.tools.ws.ui.UIUtils;
 
 public class JbossWSRuntimePreferencePage extends PreferencePage implements
 		IWorkbenchPreferencePage {
 
 	private Text jbosswsPath;
 	private Label statusLabel;
 
 	protected Control createContents(Composite superparent) {
 
 		IPreferenceStore ps = JbossWSCorePlugin.getDefault()
 				.getPreferenceStore();
 		this.setPreferenceStore(ps);
 
 		UIUtils uiUtils = new UIUtils(JbossWSUIPlugin.PLUGIN_ID);
 		final Composite mainComp = uiUtils.createComposite(superparent, 1);
 
 		TabFolder jbosswsPreferenceTab = new TabFolder(mainComp, SWT.WRAP);
 		jbosswsPreferenceTab.setLayoutData(new GridData(
 				GridData.GRAB_HORIZONTAL | GridData.GRAB_VERTICAL
 						| GridData.FILL_BOTH));
 
 		TabItem runtimeInstalLocationItem = new TabItem(jbosswsPreferenceTab,
 				SWT.WRAP);
 		runtimeInstalLocationItem.setText(JbossWSUIMessages.JBOSSWS_RUNTIME);
 		runtimeInstalLocationItem
 				.setToolTipText(JbossWSUIMessages.JBOSSWS_RUNTIME_TOOLTIP);
 
 		Composite runtimeTab = uiUtils.createComposite(jbosswsPreferenceTab, 1);
 		runtimeTab.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL
 				| GridData.GRAB_VERTICAL | GridData.FILL_BOTH));
 		Composite runtimeGroup = uiUtils.createComposite(runtimeTab, 3);
 
 		runtimeInstalLocationItem.setControl(runtimeTab);
 		runtimeTab.setToolTipText(JbossWSUIMessages.JBOSSWS_RUNTIME_TOOLTIP);
 
 		jbosswsPath = uiUtils.createText(runtimeGroup,
 				JbossWSUIMessages.JBOSSWS_RUNTIME_LOCATION, null, null,
 				SWT.BORDER);
 
 		Button browseButton = uiUtils.createPushButton(runtimeGroup,
 				JbossWSUIMessages.LABEL_BROUSE, null, null);
 		browseButton.addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent e) {
 				handleBrowse(mainComp.getShell());
 			}
 		});
 
 		jbosswsPath.addModifyListener(new ModifyListener() {
 			public void modifyText(ModifyEvent e) {
 				statusUpdate(runtimeExist(jbosswsPath.getText()));
 
 			}
 		});
 		new Label(runtimeTab, SWT.HORIZONTAL);
 		statusLabel = new Label(runtimeTab, SWT.BACKGROUND | SWT.READ_ONLY | SWT.CENTER | SWT.WRAP | SWT.H_SCROLL);
 		statusLabel.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL
 				| GridData.GRAB_VERTICAL | GridData.FILL_BOTH));
 		
 		initializeValues();
 		jbosswsPreferenceTab.setEnabled(true);
 		jbosswsPreferenceTab.setVisible(true);
 		return mainComp;
 	}
 
 	public void init(IWorkbench workbench) {
 	}
 
 	/**
 	 * Pops up the file browse dialog box
 	 */
 	private void handleBrowse(Shell parent) {
 		DirectoryDialog fileDialog = new DirectoryDialog(parent);
 		String fileName = fileDialog.open();
 		if (fileName != null) {
 			jbosswsPath.setText(fileName);
 		}
 	}
 
 	private void statusUpdate(boolean status) {
 		if (statusLabel != null) {
 			if (!jbosswsPath.getText().equals("")) {
 				if (status) {
 					statusLabel
 							.setText(JbossWSUIMessages.LABEL_JBOSSWS_RUNTIME_LOAD);
 				} else {
 					statusLabel
 							.setText(JbossWSUIMessages.LABEL_JBOSSWS_RUNTIME_LOAD_ERROR);
 				}
 			} else {
 				statusLabel
 						.setText(JbossWSUIMessages.LABEL_JBOSSWS_RUNTIME_NOT_EXIT);
 			}
 		}
 	}
 
 	private boolean runtimeExist(String path) {
 
 		File jbosswsHomeDir = new File(path);
 		if (!jbosswsHomeDir.isDirectory())
 			return false;
 		String[] newNode = {JbossWSUIMessages.BIN, JbossWSUIMessages.COMMOND};
 		String jbosswsBinPath = UIUtils.addNodesToPath(jbosswsHomeDir.getAbsolutePath(), newNode);
 		if(new File(jbosswsBinPath).isFile()){
 			return true;
 		}
 		return false;
 	}
 
 	/**
 	 * store the location to the preference
 	 */
 	private void storeValues() {
 		IPreferenceStore store = this.getPreferenceStore();
 		System.out.println(jbosswsPath.getText());
		store.setValue("jbosswsruntimelocation", jbosswsPath.getText());
 	}
 
 	/**
 	 * Initializes location using default values in the preference store.
 	 */
 	private void initializeDefaults() {
 		IPreferenceStore preferenceStore = this.getPreferenceStore();
 		jbosswsPath.setText(preferenceStore
 				.getDefaultString("jbosswsruntimelocation"));
 	}
 
 	/**
 	 * Initializes the location using default values in the preference
 	 */
 	private void initializeValues() {
 		IPreferenceStore preferenceStore = this.getPreferenceStore();
 		jbosswsPath
 				.setText(preferenceStore.getString("jbosswsruntimelocation"));
 
 	}
 
 	/**
 	 * Default button has been pressed.
 	 */
 	protected void performDefaults() {
 		super.performDefaults();
 		initializeDefaults();
 	}
 
 	/**
 	 * Apply button has been pressed.
 	 */
 	protected void performApply() {
 		performOk();
 	}
 
 	/**
 	 * OK button has been pressed.
 	 */
 	public boolean performOk() {
 		storeValues();
 		return true;
 	}
 
 }
