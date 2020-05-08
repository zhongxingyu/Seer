 /*
  * Copyright 2004-2006 the Seasar Foundation and the Others.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, 
  * either express or implied. See the License for the specific language
  * governing permissions and limitations under the License.
  */
 package org.seasar.kijimuna.ui.internal.preference;
 
 import java.lang.reflect.InvocationTargetException;
 
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.jface.operation.IRunnableWithProgress;
 import org.eclipse.jface.preference.IPreferenceStore;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.swt.widgets.Group;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.ui.IWorkbenchWindow;
 import org.eclipse.ui.PlatformUI;
 import org.eclipse.ui.dialogs.PropertyPage;
 
 import org.seasar.kijimuna.core.ConstCore;
 import org.seasar.kijimuna.core.KijimunaCore;
 import org.seasar.kijimuna.core.util.PreferencesUtil;
 import org.seasar.kijimuna.core.util.ProjectUtils;
 import org.seasar.kijimuna.ui.KijimunaUI;
 import org.seasar.kijimuna.ui.internal.preference.design.ErrorMarkerDesign;
 import org.seasar.kijimuna.ui.internal.preference.design.Messages;
 
 /**
  * @author Masataka Kurihara (Gluegent, Inc.)
  */
 public class ProjectPreferencePage extends PropertyPage implements ConstCore {
 
 	private Button natureCheck;
 	private Button enableProjectCustomSetting;
 	private ErrorMarkerDesign markerDesign;
 	private Composite base;
 	
 	protected Control createContents(Composite parent) {
 		IPreferenceStore store = PreferencesUtil.getPreferenceStoreOfProject(getProject());
 		setPreferenceStore(store);
 
 		GridLayout layout = new GridLayout(1, true);
 		layout.marginHeight = 0;
 		layout.marginWidth = 0;
 		parent.setLayout(layout);
 		
 		// nature checkbox
 		natureCheck = new Button(parent, SWT.CHECK | SWT.LEFT);
 		natureCheck.setText(Messages.getString("ProjectPreferencePage.1")); //$NON-NLS-1$
 		natureCheck.setFont(parent.getFont());
 		natureCheck.addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent e) {
 				handleNatureCheck();
 			}
 		});
 
 		Label separator = new Label(parent, SWT.SEPARATOR | SWT.HORIZONTAL);
 		GridData separatorGd = new GridData();
 		separatorGd.horizontalAlignment = GridData.FILL;
 		separator.setLayoutData(separatorGd);
 		
 		base = new Composite(parent, SWT.NONE){
 			public void setEnabled(boolean enabled) {
 				enableProjectCustomSetting.setEnabled(enabled);
 				if(enabled){
 					markerDesign.setEnabled(enableProjectCustomSetting.getSelection());
 				}else{
 					markerDesign.setEnabled(false);
 				}
 			}
 		};
 		base.setLayout(layout);
 		GridData pgd = new GridData();
 		pgd.horizontalAlignment = GridData.FILL;
 		pgd.verticalAlignment = GridData.FILL;
 		pgd.grabExcessVerticalSpace = true;
 		pgd.grabExcessHorizontalSpace = true;
 		base.setLayoutData(pgd);
 		
 		enableProjectCustomSetting = new Button(base, SWT.CHECK | SWT.LEFT);
		enableProjectCustomSetting.setText(Messages.getString("ProjectPreferencePage.2")); //$NON-NLS-1$
 		enableProjectCustomSetting.addSelectionListener(new SelectionAdapter(){
 			public void widgetSelected(SelectionEvent e) {
 				handleEnableProjectCustomSetting();
 			}
 		});
 		
 		Group group = new Group(base, SWT.SHADOW_NONE);
 		group.setLayout(new GridLayout(1, true));
		group.setText(Messages.getString("ErrorMarkerDesign.1")); //$NON-NLS-1$
 		GridData gd = new GridData();
 		gd.horizontalAlignment = GridData.FILL;
 		gd.grabExcessHorizontalSpace = true;
 		group.setLayoutData(gd);
 		
 		// error marker
 		markerDesign = new ErrorMarkerDesign(group, SWT.NULL, getPreferenceStore());
 		markerDesign.setLayoutData(gd);
 		
 		init();
 		
 		return parent;
 	}
 
 	private void init() {
 		try {
 			boolean hasNature = getProject().hasNature(ID_NATURE_DICON);
 			natureCheck.setSelection(hasNature);
 			handleNatureCheck();
 		} catch (CoreException e) {
 			KijimunaUI.reportException(e);
 		}
 		IPreferenceStore store = getPreferenceStore();
 		boolean projectCustom = store.getBoolean(MARKER_SEVERITY_ENABLE_PROJECT_CUSTOM);
 		enableProjectCustomSetting.setSelection(projectCustom);
 		handleEnableProjectCustomSetting();
 	}
 	
 	private void handleNatureCheck(){
 		base.setEnabled(natureCheck.getSelection());
 	}
 	private void handleEnableProjectCustomSetting() {
 		markerDesign.setEnabled(enableProjectCustomSetting.getSelection());
 	}
 	
 	private IProject getProject() {
 		return (IProject) getElement();
 	}
 	
 	public boolean performOk() {
 		applyModification();
 		cleanupDiconModel();
 		return true;
 	}
 	
 	protected void performApply() {
 		applyModification();
 	}
 	
 	private void applyModification() {
 		try {
 			IProject project = getProject();
 			if (natureCheck.getSelection()) {
 				if (!project.hasNature(ID_NATURE_DICON)) {
 					ProjectUtils.addNature(project, ID_NATURE_DICON);
 				}
 			} else {
 				ProjectUtils.removeNature(project, ID_NATURE_DICON);
 			}
 			IPreferenceStore store = getPreferenceStore();
 			
 			boolean enablePrjCustom = enableProjectCustomSetting.getSelection();
 			store.setValue(MARKER_SEVERITY_ENABLE_PROJECT_CUSTOM, enablePrjCustom);
 			
 			markerDesign.store();
 			
 		} catch (CoreException e) {
 			KijimunaUI.reportException(e);
 		}
 	}
 
 	protected void performDefaults() {
 		if (natureCheck.getSelection()) {
 			markerDesign.loadDefault();
 
 			boolean b = getPreferenceStore().getDefaultBoolean(MARKER_SEVERITY_ENABLE_PROJECT_CUSTOM);
 			enableProjectCustomSetting.setSelection(b);
 			handleEnableProjectCustomSetting();
 		}
 	}
 	
 	private void cleanupDiconModel() {
 		IRunnableWithProgress runnable = new IRunnableWithProgress() {
 			public void run(IProgressMonitor monitor) throws
 					InvocationTargetException, InterruptedException {
 				KijimunaCore.getProjectRecorder().cleanup(getProject(),
 						monitor);
 			}
 		};
 		IWorkbenchWindow w = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
 		try {
 			if (w != null) {
 				w.run(true, true, runnable);
 			}
 		} catch (InvocationTargetException e) {
 			KijimunaCore.reportException(e);
 		} catch (InterruptedException e) {
 			KijimunaCore.reportException(e);
 		}
 	}
 
 }
