 package org.eclipse.jdt.debug.ui.launchConfigurations;
 
 /*
  * (c) Copyright IBM Corp. 2000, 2001.
  * All Rights Reserved.
  */
  
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.io.File;
 
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.debug.core.ILaunchConfiguration;
 import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
 import org.eclipse.debug.ui.ILaunchConfigurationTab;
 import org.eclipse.jdt.core.IJavaElement;
 import org.eclipse.jdt.internal.debug.ui.JDIDebugUIPlugin;
 import org.eclipse.jdt.internal.debug.ui.launcher.AddVMDialog;
 import org.eclipse.jdt.internal.debug.ui.launcher.IAddVMDialogRequestor;
 import org.eclipse.jdt.internal.debug.ui.launcher.JavaLaunchConfigurationTab;
 import org.eclipse.jdt.internal.debug.ui.launcher.LauncherMessages;
 import org.eclipse.jdt.internal.debug.ui.launcher.VMStandin;
 import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
 import org.eclipse.jdt.launching.IVMInstall;
 import org.eclipse.jdt.launching.IVMInstallType;
 import org.eclipse.jdt.launching.JavaRuntime;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.ModifyEvent;
 import org.eclipse.swt.events.ModifyListener;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Combo;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.swt.widgets.Label;
 
 /**
  * A launch configuration tab that displays and edits the VM install 
  * launch configuration attributes.
  * <p>
  * This class may be instantiated. This class is not intended to be subclassed.
  * </p>
  * @since 2.0
  */
 
 public class JavaJRETab extends JavaLaunchConfigurationTab implements IAddVMDialogRequestor {
 
 	// UI widgets
 	protected Label fJRELabel;
 	protected Combo fJRECombo;
 	protected Button fJREAddButton;
 
 	// Collections used to populate the JRE Combo box
 	protected IVMInstallType[] fVMTypes;
 	protected List fVMStandins;	
 	
 	// Dynamic JRE UI widgets
 	protected ILaunchConfigurationTab fDynamicTab;
 	protected Composite fDynamicTabHolder;
 	protected boolean fUseDynamicArea = true;
 	
 	protected ILaunchConfigurationWorkingCopy fWorkingCopy;
 	protected ILaunchConfiguration fLaunchConfiguration;
 	
 	// Constants
 	protected static final String DEFAULT_JRE_NAME = LauncherMessages.getString("JavaJRETab.Default_1"); //$NON-NLS-1$
 	protected static final String EMPTY_STRING = ""; //$NON-NLS-1$
 		
 	/**
 	 * @see ILaunchConfigurationTab#createControl(Composite)
 	 */
 	public void createControl(Composite parent) {
 		Composite topComp = new Composite(parent, SWT.NONE);
 		setControl(topComp);
 		GridLayout topLayout = new GridLayout();
 		topLayout.numColumns = 2;
 		topLayout.marginHeight = 0;
 		topLayout.marginWidth = 0;
 		topComp.setLayout(topLayout);
 		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
 		topComp.setLayoutData(gd);
 		
 		createVerticalSpacer(topComp, 2);
 		
 		fJRELabel = new Label(topComp, SWT.NONE);
 		fJRELabel.setText(LauncherMessages.getString("JavaJRETab.Run_with_JRE__1")); //$NON-NLS-1$
 		gd = new GridData();
 		gd.horizontalSpan = 2;
 		fJRELabel.setLayoutData(gd);
 		
 		fJRECombo = new Combo(topComp, SWT.READ_ONLY);
 		gd = new GridData(GridData.FILL_HORIZONTAL);
 		fJRECombo.setLayoutData(gd);
 		initializeJREComboBox();
 		fJRECombo.addModifyListener(new ModifyListener() {
 			public void modifyText(ModifyEvent evt) {
 				handleJREComboBoxModified();
 			}
 		});
 		
 		fJREAddButton = createPushButton(topComp, LauncherMessages.getString("JavaJRETab.New_1"), null);  //$NON-NLS-1$
 		fJREAddButton.addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent evt) {
 				handleJREAddButtonSelected();
 			}
 		});	
 		
 		setDynamicTabHolder(new Composite(topComp, SWT.NONE));
 		GridLayout tabHolderLayout = new GridLayout();
 		tabHolderLayout.marginHeight= 0;
 		tabHolderLayout.marginWidth= 0;
 		tabHolderLayout.numColumns = 1;
 		getDynamicTabHolder().setLayout(tabHolderLayout);
 		gd = new GridData(GridData.FILL_BOTH);
 		getDynamicTabHolder().setLayoutData(gd);
 	}
 
 	protected void setDynamicTabHolder(Composite tabHolder) {
 		this.fDynamicTabHolder = tabHolder;
 	}
 
 	protected Composite getDynamicTabHolder() {
 		return fDynamicTabHolder;
 	}
 
 	protected void setDynamicTab(ILaunchConfigurationTab tab) {
 		fDynamicTab = tab;
 	}
 
 	protected ILaunchConfigurationTab getDynamicTab() {
 		return fDynamicTab;
 	}
 
 	/**
 	 * @see ILaunchConfigurationTab#setDefaults(ILaunchConfigurationWorkingCopy)
 	 */
 	public void setDefaults(ILaunchConfigurationWorkingCopy config) {
 		setLaunchConfigurationWorkingCopy(config);
 		ILaunchConfigurationTab dynamicTab = getDynamicTab();
 		if (dynamicTab != null) {
 			dynamicTab.setDefaults(config);
 		}
 	}
 	
 	/**
 	 * @see ILaunchConfigurationTab#initializeFrom(ILaunchConfiguration)
 	 */
 	public void initializeFrom(ILaunchConfiguration configuration) {
 		setLaunchConfiguration(configuration);
 		updateJREFromConfig(configuration);
 		ILaunchConfigurationTab dynamicTab = getDynamicTab();
 		if (dynamicTab != null) {
 			dynamicTab.initializeFrom(configuration);
 		}
 	}
 
 	/**
 	 * @see ILaunchConfigurationTab#performApply(ILaunchConfigurationWorkingCopy)
 	 */
 	public void performApply(ILaunchConfigurationWorkingCopy configuration) {
 		int selectedIndex = fJRECombo.getSelectionIndex();
 		if (selectedIndex > -1) {
 			String vmName = null;
 			String vmTypeID = null;
 			if (selectedIndex > 0) {
 				VMStandin vmStandin = (VMStandin)fVMStandins.get(selectedIndex - 1);
 				vmName = vmStandin.getName();
 				vmTypeID = vmStandin.getVMInstallType().getId();
 			} 
 			configuration.setAttribute(IJavaLaunchConfigurationConstants.ATTR_VM_INSTALL_NAME, vmName);
 			configuration.setAttribute(IJavaLaunchConfigurationConstants.ATTR_VM_INSTALL_TYPE, vmTypeID);
 		}	
 		ILaunchConfigurationTab dynamicTab = getDynamicTab();
		if (dynamicTab != null) {
 			dynamicTab.performApply(configuration);
 		}
 	}
 
 	/**
 	 * @see ILaunchConfigurationTab#isValid(ILaunchConfiguration)
 	 */
 	public boolean isValid(ILaunchConfiguration config) {
 		
 		setErrorMessage(null);
 		setMessage(null);
 		
 		// Don't do any validation if the default VM was chosen
 		int selectedIndex = fJRECombo.getSelectionIndex();
 		if (selectedIndex > 0) {
 			VMStandin vmStandin = (VMStandin)fVMStandins.get(selectedIndex - 1);
 			IVMInstall vm = vmStandin.convertToRealVM();
 			File location = vm.getInstallLocation();
 			if (location == null) {
 				setErrorMessage(LauncherMessages.getString("JavaJRETab.JRE_home_directory_not_specified_36")); //$NON-NLS-1$
 				return false;
 			}
 			if (!location.exists()) {
 				setErrorMessage(LauncherMessages.getString("JavaJRETab.JRE_home_directory_does_not_exist_37")); //$NON-NLS-1$
 				return false;
 			}			
 		} else if (selectedIndex < 0) {
 			setErrorMessage(LauncherMessages.getString("JavaJRETab.JRE_not_specified_38")); //$NON-NLS-1$
 			return false;
 		}		
 
 		ILaunchConfigurationTab dynamicTab = getDynamicTab();
 		if (dynamicTab != null) {
 			return dynamicTab.isValid(config);
 		}
 		return true;
 	}
 
 	/**
 	 * @see ILaunchConfigurationTab#getName()
 	 */
 	public String getName() {
 		return LauncherMessages.getString("JavaJRETab.&JRE_1"); //$NON-NLS-1$
 	}
 
 	/**
 	 * @see IAddVMDialogRequestor#isDuplicateName(IVMInstallType, String)
 	 */
 	public boolean isDuplicateName(IVMInstallType type, String name) {
 		if (name.equals(DEFAULT_JRE_NAME)) {
 			return true;
 		}
 		for (int i = 0; i < fVMStandins.size(); i++) {
 			IVMInstall vm= (IVMInstall)fVMStandins.get(i);
 			if (vm.getVMInstallType() == type) {
 				if (vm.getName().equals(name))
 					return true;
 			}
 		}
 		return false;
 	}
 
 	/**
 	 * @see IAddVMDialogRequestor#vmAdded(IVMInstall)
 	 */
 	public void vmAdded(IVMInstall vm) {
 		((VMStandin)vm).convertToRealVM();		
 		try {
 			JavaRuntime.saveVMConfiguration();
 		} catch(CoreException e) {
 			JDIDebugUIPlugin.log(e);
 		}
 		fVMStandins.add(vm);
 		populateJREComboBox();
 		selectJREComboBoxEntry(vm.getVMInstallType().getId(), vm.getName());
 	}
 
 	protected void updateJREFromConfig(ILaunchConfiguration config) {
 		String vmName = null;
 		String vmTypeID = null;
 		try {
 			vmTypeID = config.getAttribute(IJavaLaunchConfigurationConstants.ATTR_VM_INSTALL_TYPE, (String)null);
 			vmName = config.getAttribute(IJavaLaunchConfigurationConstants.ATTR_VM_INSTALL_NAME, (String)null);
 		} catch (CoreException ce) {
 			JDIDebugUIPlugin.log(ce);			
 		}
 		selectJREComboBoxEntry(vmTypeID, vmName);
 	}	
 	
 	/**
 	 * Load the JRE related collections, and use these to set the values on the combo box
 	 */
 	protected void initializeJREComboBox() {
 		fVMTypes= JavaRuntime.getVMInstallTypes();
 		fVMStandins= createFakeVMInstalls(fVMTypes);
 		populateJREComboBox();		
 	}
 	
 	/**
 	 * Notification that the user changed the selection in the JRE combo box.
 	 */
 	protected void handleJREComboBoxModified() {
 		loadDynamicJREArea();
 		
 		// always set the newly created area with defaults
 		ILaunchConfigurationWorkingCopy wc = getLaunchConfigurationWorkingCopy();
 		if (getDynamicTab() == null) {
 			// remove any VM specfic args from the config
 			if (wc == null) {
 				if (getLaunchConfiguration().isWorkingCopy()) {
 					wc = (ILaunchConfigurationWorkingCopy)getLaunchConfiguration();
 				}
 			}
 			if (wc != null) {
 				wc.setAttribute(IJavaLaunchConfigurationConstants.ATTR_VM_INSTALL_TYPE_SPECIFIC_ATTRS_MAP, (Map)null);
 			}
 		} else {
 			if (wc == null) {
 				try {
 					if (getLaunchConfiguration().isWorkingCopy()) {
 						// get a fresh copy to work on
 						wc = ((ILaunchConfigurationWorkingCopy)getLaunchConfiguration()).getOriginal().getWorkingCopy();
 					} else {
 							wc = getLaunchConfiguration().getWorkingCopy();
 					}
 				} catch (CoreException e) {
 					JDIDebugUIPlugin.errorDialog("Unable to initialize defaults for selected JRE", e);
 					return;
 				}
 			}
 			getDynamicTab().setDefaults(wc);
 			getDynamicTab().initializeFrom(wc);
 		}
 				
 		updateLaunchConfigurationDialog();		
 	}
 	
 	/**
 	 * Show a dialog that lets the user add a new JRE definition
 	 */
 	protected void handleJREAddButtonSelected() {
 		AddVMDialog dialog= new AddVMDialog(this, getShell(), fVMTypes, null);
 		dialog.setTitle(LauncherMessages.getString("vmPreferencePage.editJRE.title")); //$NON-NLS-1$
 		if (dialog.open() != dialog.OK) {
 			return;
 		}
 	}	
 	
 	/**
 	 * Set the available items on the JRE combo box
 	 */
 	protected void populateJREComboBox() {
 		String[] vmNames = new String[fVMStandins.size() + 1];
 
 		// Set the name of the 'default' VM
 		vmNames[0] = DEFAULT_JRE_NAME;
 		int index = 1;
 
 		// Add all installed VMs
 		Iterator iterator = fVMStandins.iterator();
 		while (iterator.hasNext()) {
 			VMStandin standin = (VMStandin)iterator.next();
 			String vmName = standin.getName();
 			vmNames[index++] = vmName;
 		}
 		fJRECombo.setItems(vmNames);
 	}	
 	
 	/**
 	 * Cause the specified VM to be selected in the JRE combo box.
 	 * This relies on the fact that the JRE names in the combo box are in the
 	 * same order as they are in the <code>fVMStandins</code> list.
 	 * 
 	 * @param typeID the VM install type identifier, or <code>null</code> to select "default"
 	 * @param vmName vm name, or <code>null</code> to select "default"
 	 */
 	protected void selectJREComboBoxEntry(String typeID, String vmName) {
 		int index = 0;
 		if (typeID != null && vmName != null) {
 			for (int i = 0; i < fVMStandins.size(); i++) {
 				VMStandin vmStandin = (VMStandin)fVMStandins.get(i);
 				if (vmStandin.getVMInstallType().getId().equals(typeID) && vmStandin.getName().equals(vmName)) {
 					index = i + 1;
 					break;
 				}
 			}
 		}
 
 		fJRECombo.select(index);
 	}	
 	
 	/**
 	 * Convenience method to remove any selection in the JRE combo box
 	 */
 	protected void clearJREComboBoxEntry() {
 		fJRECombo.deselectAll();
 	}	
 	
 	private java.util.List createFakeVMInstalls(IVMInstallType[] vmTypes) {
 		ArrayList vms= new ArrayList();
 		for (int i= 0; i < vmTypes.length; i++) {
 			IVMInstall[] vmInstalls= vmTypes[i].getVMInstalls();
 			for (int j= 0; j < vmInstalls.length; j++) 
 				vms.add(new VMStandin(vmInstalls[j]));
 		}
 		return vms;
 	}	
 	
 	/**
 	 * Return the class that implements <code>ILaunchConfigurationTab</code>
 	 * that is registered against the install type of the currently selected VM.
 	 */
 	protected ILaunchConfigurationTab getTabForCurrentJRE() {
 		int selectedIndex = fJRECombo.getSelectionIndex();
 		if (selectedIndex > 0) {
 			VMStandin vmStandin = (VMStandin) fVMStandins.get(selectedIndex - 1);
 			String vmInstallTypeID = vmStandin.getVMInstallType().getId();
 			return JDIDebugUIPlugin.getDefault().getVMInstallTypePage(vmInstallTypeID);		
 		}
 		return null;
 	}
 	
 	/**
 	 * Show the contributed piece of UI that was registered for the install type
 	 * of the currently selected VM.
 	 */
 	protected void loadDynamicJREArea() {
 		
 		// Dispose of any current child widgets in the tab holder area
 		Control[] children = getDynamicTabHolder().getChildren();
 		for (int i = 0; i < children.length; i++) {
 			children[i].dispose();
 		}
 		
 		if (isUseDynamicJREArea()) {
 			// Retrieve the dynamic UI for the current JRE 
 			setDynamicTab(getTabForCurrentJRE());
 			if (getDynamicTab() == null) {
 				return;
 			}
 			
 			// Ask the dynamic UI to create its Control
 			getDynamicTab().setLaunchConfigurationDialog(getLaunchConfigurationDialog());
 			getDynamicTab().createControl(getDynamicTabHolder());
 			getDynamicTabHolder().layout();	
 		}
 			
 	}
 
 	protected ILaunchConfigurationWorkingCopy getLaunchConfigurationWorkingCopy() {
 		return fWorkingCopy;
 	}
 	
 	/**
 	 * Overridden here so that any error message in the dynamic UI gets returned.
 	 * 
 	 * @see ILaunchConfigurationTab#getErrorMessage()
 	 */
 	public String getErrorMessage() {
 		ILaunchConfigurationTab tab = getDynamicTab();
 		if ((super.getErrorMessage() != null) || (tab == null)) {
 			return super.getErrorMessage();
 		} else {
 			return tab.getErrorMessage();
 		}
 	}
 
 	protected void setLaunchConfigurationWorkingCopy(ILaunchConfigurationWorkingCopy workingCopy) {
 		fWorkingCopy = workingCopy;
 	}
 
 	protected ILaunchConfiguration getLaunchConfiguration() {
 		return fLaunchConfiguration;
 	}
 
 	protected void setLaunchConfiguration(ILaunchConfiguration launchConfiguration) {
 		fLaunchConfiguration = launchConfiguration;
 	}
 	
 	/**
 	 * Sets whether this tab will display the VM specific arguments area
 	 * if a JRE supports VM specific arguments.
 	 * 
 	 * @param visible whether this tab will display the VM specific arguments area
 	 * 	if a JRE supports VM specific arguments
 	 */
 	public void setVMSpecificArgumentsVisible(boolean visible) {
 		fUseDynamicArea = visible;
 	}
 	
 	protected boolean isUseDynamicJREArea() {
 		return fUseDynamicArea;
 	}
 }
