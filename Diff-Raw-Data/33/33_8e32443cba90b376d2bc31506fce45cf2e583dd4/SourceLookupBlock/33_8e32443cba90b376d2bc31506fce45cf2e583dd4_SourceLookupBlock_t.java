 /*******************************************************************************
  * Copyright (c) 2000, 2003 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials 
  * are made available under the terms of the Common Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/cpl-v10.html
  * 
  * Contributors:
  *     IBM Corporation - initial API and implementation
  *******************************************************************************/
 package org.eclipse.jdt.internal.debug.ui.launcher;
 
 
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.debug.core.ILaunchConfiguration;
 import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
 import org.eclipse.debug.ui.ILaunchConfigurationTab;
 import org.eclipse.jdt.debug.ui.JavaUISourceLocator;
 import org.eclipse.jdt.internal.debug.ui.JDIDebugUIPlugin;
 import org.eclipse.jdt.internal.debug.ui.actions.AddAdvancedAction;
 import org.eclipse.jdt.internal.debug.ui.actions.AddExternalFolderAction;
 import org.eclipse.jdt.internal.debug.ui.actions.AddExternalJarAction;
 import org.eclipse.jdt.internal.debug.ui.actions.AddFolderAction;
 import org.eclipse.jdt.internal.debug.ui.actions.AddJarAction;
 import org.eclipse.jdt.internal.debug.ui.actions.AddProjectAction;
 import org.eclipse.jdt.internal.debug.ui.actions.AddVariableAction;
 import org.eclipse.jdt.internal.debug.ui.actions.AttachSourceAction;
 import org.eclipse.jdt.internal.debug.ui.actions.MoveDownAction;
 import org.eclipse.jdt.internal.debug.ui.actions.MoveUpAction;
 import org.eclipse.jdt.internal.debug.ui.actions.RemoveAction;
 import org.eclipse.jdt.internal.debug.ui.actions.RuntimeClasspathAction;
 import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
 import org.eclipse.jdt.launching.IRuntimeClasspathEntry;
 import org.eclipse.jdt.launching.JavaRuntime;
 import org.eclipse.jface.action.IAction;
 import org.eclipse.jface.dialogs.Dialog;
 import org.eclipse.jface.dialogs.IDialogConstants;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.graphics.Font;
 import org.eclipse.swt.graphics.FontMetrics;
 import org.eclipse.swt.graphics.GC;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Label;
  
 /**
  * Control used to edit the source lookup path for a Java launch configuration.
  */
 public class SourceLookupBlock extends JavaLaunchConfigurationTab implements ILaunchConfigurationTab {
 	
 	protected ILaunchConfiguration fConfig;
 	
 	protected RuntimeClasspathViewer fPathViewer;
 	protected Button fDefaultButton;
 	protected Button fDuplicatesButton;
 	protected List fActions = new ArrayList(10);
 	
 	protected static final String DIALOG_SETTINGS_PREFIX = "SourceLookupBlock"; //$NON-NLS-1$
 	
 	/**
 	 * Creates and returns the source lookup control.
 	 * 
 	 * @param parent the parent widget of this control
 	 */
 	public void createControl(Composite parent) {
 		Font font = parent.getFont();
 		
 		Composite comp = new Composite(parent, SWT.NONE);
 		GridLayout topLayout = new GridLayout();
 		topLayout.numColumns = 2;
 		comp.setLayout(topLayout);		
 		GridData gd = new GridData(GridData.FILL_BOTH);
 		comp.setLayoutData(gd);
 		
 		Label viewerLabel= new Label(comp, SWT.LEFT);
 		viewerLabel.setText(LauncherMessages.getString("SourceLookupBlock.&Source_Lookup_Path__1")); //$NON-NLS-1$
 		gd= new GridData(GridData.HORIZONTAL_ALIGN_FILL);
 		gd.horizontalSpan= 2;
 		viewerLabel.setLayoutData(gd);
 		viewerLabel.setFont(font);
 		
 		fPathViewer = new RuntimeClasspathViewer(comp);
 		fPathViewer.addEntriesChangedListener(this);
 		gd = new GridData(GridData.FILL_BOTH);
 		fPathViewer.getControl().setLayoutData(gd);
 		fPathViewer.getControl().setFont(font);
 
 		Composite pathButtonComp = new Composite(comp, SWT.NONE);
 		GridLayout pathButtonLayout = new GridLayout();
 		pathButtonLayout.marginHeight = 0;
 		pathButtonLayout.marginWidth = 0;
 		pathButtonComp.setLayout(pathButtonLayout);
 		gd = new GridData(GridData.VERTICAL_ALIGN_BEGINNING | GridData.HORIZONTAL_ALIGN_FILL);
 		pathButtonComp.setLayoutData(gd);
 		pathButtonComp.setFont(font);
 		
 		createVerticalSpacer(comp, 2);
 						
 		fDefaultButton = new Button(comp, SWT.CHECK);
 		fDefaultButton.setText(LauncherMessages.getString("SourceLookupBlock.Use_defau&lt_source_lookup_path_1")); //$NON-NLS-1$
 		gd = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
 		gd.horizontalSpan = 2;
 		fDefaultButton.setLayoutData(gd);
 		fDefaultButton.setFont(font);
 		fDefaultButton.addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent evt) {
 				handleDefaultButtonSelected();
 			}
 		});
 		
 		fDuplicatesButton = new Button(comp, SWT.CHECK);
 		fDuplicatesButton.setText(LauncherMessages.getString("SourceLookupBlock.&Search_for_duplicate_source_files_on_path_1")); //$NON-NLS-1$
 		gd = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
 		gd.horizontalSpan = 2;
 		fDuplicatesButton.setLayoutData(gd);
 		fDuplicatesButton.setFont(font);
 		fDuplicatesButton.addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent evt) {
 				setDirty(true);
 				updateLaunchConfigurationDialog();
 			}
 		});		
 		
 		List advancedActions = new ArrayList(5);
 		
 		GC gc = new GC(parent);
 		gc.setFont(parent.getFont());
 		FontMetrics fontMetrics= gc.getFontMetrics();
 		gc.dispose();
 				
 		RuntimeClasspathAction action = new MoveUpAction(null);								
 		Button button  = createPushButton(pathButtonComp, action.getText(), fontMetrics);
 		action.setButton(button);
 		addAction(action);
 		
 		action = new MoveDownAction(null);								
 		button  = createPushButton(pathButtonComp, action.getText(), fontMetrics);
 		action.setButton(button);
 		addAction(action);		
 
 		action = new RemoveAction(null);								
 		button  = createPushButton(pathButtonComp, action.getText(), fontMetrics);
 		action.setButton(button);
 		addAction(action);		
 		
 		action = new AddProjectAction(null);								
 		button  = createPushButton(pathButtonComp, action.getText(), fontMetrics);
 		action.setButton(button);
 		addAction(action);		
 
 		action = new AddJarAction(null);								
 		button  = createPushButton(pathButtonComp, action.getText(), fontMetrics);
 		action.setButton(button);
 		addAction(action);		
 
 		action = new AddExternalJarAction(null, DIALOG_SETTINGS_PREFIX);								
 		button  = createPushButton(pathButtonComp, action.getText(), fontMetrics);
 		action.setButton(button);
 		addAction(action);		
 
 		action = new AddFolderAction(null);								
 		advancedActions.add(action);
 
 		action = new AddExternalFolderAction(null, DIALOG_SETTINGS_PREFIX);								
 		advancedActions.add(action);		
 
 		action = new AddVariableAction(null);								
 		advancedActions.add(action);		
 		
		action = new AttachSourceAction(null, SWT.RADIO);								
 		advancedActions.add(action);				
 									
 		IAction[] adv = (IAction[])advancedActions.toArray(new IAction[advancedActions.size()]);
 		action = new AddAdvancedAction(null, adv);
 		button = createPushButton(pathButtonComp, action.getText(), fontMetrics);
 		action.setButton(button);
 		addAction(action);
 																
 		retargetActions(fPathViewer);
 				
 		setControl(comp);
 	}
 
 	/**
 	 * The "default" button has been toggled
 	 */
 	protected void handleDefaultButtonSelected() {
 		setDirty(true);
 		boolean def = fDefaultButton.getSelection();
 		if (def) {
 			try {
 				ILaunchConfiguration config = getLaunchConfiguration();
 				ILaunchConfigurationWorkingCopy wc = null;
 				if (config.isWorkingCopy()) {
 					wc= (ILaunchConfigurationWorkingCopy)config;
 				} else {
 					wc = config.getWorkingCopy();
 				}
 				performApply(wc);
 				IRuntimeClasspathEntry[] defs = JavaRuntime.computeUnresolvedSourceLookupPath(wc);
 				fPathViewer.setEntries(defs);
 			} catch (CoreException e) {
 				JDIDebugUIPlugin.log(e);
 			}
 		}
 		fPathViewer.setEnabled(!def);
 		updateLaunchConfigurationDialog();
 	}
 	
 	/**
 	 * Creates and returns a button 
 	 * 
 	 * @param parent parent widget
 	 * @param label label
 	 * @return Button
 	 */
 	protected Button createPushButton(Composite parent, String label, FontMetrics fontMetrics) {
 		Button button = new Button(parent, SWT.PUSH);
 		button.setFont(parent.getFont());
 		button.setText(label);
 		GridData gd= getButtonGridData(button, fontMetrics);
 		button.setLayoutData(gd);
 		return button;	
 	}
 	
 	private GridData getButtonGridData(Button button, FontMetrics fontMetrics) {
 		GridData gd= new GridData(GridData.FILL_HORIZONTAL | GridData.VERTICAL_ALIGN_BEGINNING);
 
 		int widthHint= Dialog.convertHorizontalDLUsToPixels(fontMetrics, IDialogConstants.BUTTON_WIDTH);
 		gd.widthHint= Math.max(widthHint, button.computeSize(SWT.DEFAULT, SWT.DEFAULT, true).x);
 	
 		gd.heightHint= Dialog.convertVerticalDLUsToPixels(fontMetrics, IDialogConstants.BUTTON_HEIGHT);
 		return gd;
 	}
 	
 	/**
 	 * Create some empty space 
 	 */
 	protected void createVerticalSpacer(Composite comp, int colSpan) {
 		Label label = new Label(comp, SWT.NONE);
 		GridData gd = new GridData();
 		gd.horizontalSpan = colSpan;
 		label.setLayoutData(gd);
 	}	
 
 	/**
 	 * Adds the given action to the action collection in this tab
 	 */
 	protected void addAction(RuntimeClasspathAction action) {
 		fActions.add(action);
 	}
 	
 	/**
 	 * Re-targets actions to the given viewer
 	 */
 	protected void retargetActions(RuntimeClasspathViewer viewer) {
 		Iterator actions = fActions.iterator();
 		while (actions.hasNext()) {
 			RuntimeClasspathAction action = (RuntimeClasspathAction)actions.next();
 			action.setViewer(viewer);
 		}
 	}
 	
 	/**
 	 * Initializes this control based on the settings in the given
 	 * launch configuration.
 	 */
 	public void initializeFrom(ILaunchConfiguration config) {
 		boolean useDefault = true;
 		setErrorMessage(null);
 		try {
 			useDefault = config.getAttribute(IJavaLaunchConfigurationConstants.ATTR_DEFAULT_SOURCE_PATH, true);
 		} catch (CoreException e) {
 			JDIDebugUIPlugin.log(e);
 		}		
 		if (config == getLaunchConfiguration()) {
 			// same as previously viewed launch config
 			if (!useDefault && !fDefaultButton.getSelection()) {
 				// If an explicit classpath is being used, it must be the same as before.
 				// No need to refresh
 				setDirty(false);
 				return;
 			}
 		}
 		setLaunchConfiguration(config);
 		fDefaultButton.setSelection(useDefault);
 		try {
 			IRuntimeClasspathEntry[] entries = JavaRuntime.computeUnresolvedSourceLookupPath(config);
 			fPathViewer.setEntries(entries);
 		} catch (CoreException e) {
 			setErrorMessage(e.getMessage());
 		}
 		fPathViewer.setEnabled(!useDefault);
 		fPathViewer.setLaunchConfiguration(config);
 		try {
 			fDuplicatesButton.setSelection(config.getAttribute(JavaUISourceLocator.ATTR_FIND_ALL_SOURCE_ELEMENTS, false));
 		} catch (CoreException e) {
 			JDIDebugUIPlugin.log(e);
 		}
 		setDirty(false);
 	}
 	
 	/**
 	 * Saves settings in the given working copy
 	 */
 	public void performApply(ILaunchConfigurationWorkingCopy configuration) {
 		if (isDirty()) {
 			boolean def = fDefaultButton.getSelection();		
 			if (def) {
 				configuration.setAttribute(IJavaLaunchConfigurationConstants.ATTR_DEFAULT_SOURCE_PATH, (String)null);
 				configuration.setAttribute(IJavaLaunchConfigurationConstants.ATTR_SOURCE_PATH, (List)null);
 			} else {
 				configuration.setAttribute(IJavaLaunchConfigurationConstants.ATTR_DEFAULT_SOURCE_PATH, def);
 				try {
 					IRuntimeClasspathEntry[] entries = fPathViewer.getEntries();
 					List mementos = new ArrayList(entries.length);
 					for (int i = 0; i < entries.length; i++) {
 						mementos.add(entries[i].getMemento());
 					}
 					configuration.setAttribute(IJavaLaunchConfigurationConstants.ATTR_SOURCE_PATH, mementos);
 				} catch (CoreException e) {
 					JDIDebugUIPlugin.errorDialog(LauncherMessages.getString("SourceLookupBlock.Unable_to_save_source_lookup_path_1"), e); //$NON-NLS-1$
 				}	
 			}
 			boolean dup = fDuplicatesButton.getSelection();
 			if (dup) {
 				configuration.setAttribute(JavaUISourceLocator.ATTR_FIND_ALL_SOURCE_ELEMENTS, true);		
 			} else {
 				configuration.setAttribute(JavaUISourceLocator.ATTR_FIND_ALL_SOURCE_ELEMENTS, (String)null);
 			}
 		}
 	}	
 	
 	/**
 	 * Returns the entries visible in the viewer
 	 */
 	public IRuntimeClasspathEntry[] getEntries() {
 		return fPathViewer.getEntries();
 	}	
 	
 	/**
 	 * Sets the configuration associated with this source lookup
 	 * block.
 	 * 
 	 * @param configuration launch configuration
 	 */
 	private void setLaunchConfiguration(ILaunchConfiguration configuration) {
 		fConfig = configuration;
 	}
 	
 	/**
 	 * Sets the configuration associated with this source lookup
 	 * block.
 	 * 
 	 * @param configuration launch configuration
 	 */
 	protected ILaunchConfiguration getLaunchConfiguration() {
 		return fConfig;
 	}	
 
 	/**
 	 * @see ILaunchConfigurationTab#getName()
 	 */
 	public String getName() {
 		return LauncherMessages.getString("SourceLookupBlock.Source_1"); //$NON-NLS-1$
 	}
 
 	/**
 	 * @see ILaunchConfigurationTab#setDefaults(ILaunchConfigurationWorkingCopy)
 	 */
 	public void setDefaults(ILaunchConfigurationWorkingCopy configuration) {
 		configuration.setAttribute(IJavaLaunchConfigurationConstants.ATTR_DEFAULT_SOURCE_PATH, (String)null);
 		configuration.setAttribute(IJavaLaunchConfigurationConstants.ATTR_SOURCE_PATH, (List)null);
 		configuration.setAttribute(JavaUISourceLocator.ATTR_FIND_ALL_SOURCE_ELEMENTS, (String)null);
 	}
 
 	/**
 	 * @see AbstractLaunchConfigurationTab#updateLaunchConfigurationDialog()
 	 */
 	protected void updateLaunchConfigurationDialog() {
 		if (getLaunchConfigurationDialog() != null) {
 			super.updateLaunchConfigurationDialog();
 		}
 	}
 
 	/**
 	 * @see ILaunchConfigurationTab#dispose()
 	 */
 	public void dispose() {
 		fPathViewer.removeEntriesChangedListener(this);
 		super.dispose();
 	}
 
 }
