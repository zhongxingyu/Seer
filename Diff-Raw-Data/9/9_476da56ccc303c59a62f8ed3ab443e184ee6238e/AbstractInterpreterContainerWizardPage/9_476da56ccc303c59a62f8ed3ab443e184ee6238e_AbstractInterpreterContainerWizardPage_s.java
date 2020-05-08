 /*******************************************************************************
  * Copyright (c) 2000, 2007 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  
  *******************************************************************************/
 package org.eclipse.dltk.internal.debug.ui.interpreters;
 
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.debug.core.DebugPlugin;
 import org.eclipse.debug.core.IStatusHandler;
 import org.eclipse.dltk.core.IBuildpathEntry;
 import org.eclipse.dltk.core.IScriptProject;
 import org.eclipse.dltk.core.environment.EnvironmentManager;
 import org.eclipse.dltk.debug.ui.ScriptDebugImages;
 import org.eclipse.dltk.internal.ui.wizards.IBuildpathContainerPage;
 import org.eclipse.dltk.ui.wizards.IBuildpathContainerPageExtension;
 import org.eclipse.jface.util.IPropertyChangeListener;
 import org.eclipse.jface.util.PropertyChangeEvent;
 import org.eclipse.jface.wizard.WizardPage;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Composite;
 
 /**
  * Extension to allow a user to associate a InterpreterEnvironment with a Script
  * project.
  */
 public abstract class AbstractInterpreterContainerWizardPage extends WizardPage
 		implements IBuildpathContainerPage, IBuildpathContainerPageExtension {
 
 	/**
 	 * The buildpath entry to be created.
 	 */
 	private IBuildpathEntry fSelection;
 
 	/**
 	 * InterpreterEnvironment control
 	 */
 	private AbstractInterpreterComboBlock fInterpreterEnvironmentBlock;
 
 	private IScriptProject scriptProject;
 
 	private IBuildpathEntry[] currentEntries;
 
 	/**
 	 * Constructs a new page.
 	 */
 	public AbstractInterpreterContainerWizardPage() {
 		super(
 				InterpretersMessages.InterpreterContainerWizardPage_Interpreter_System_Library_1);
 	}
 
 	public boolean finish() {
 		IStatus status = fInterpreterEnvironmentBlock.getStatus();
 		if (!status.isOK()) {
 			return false;
 		}
 		fSelection = fInterpreterEnvironmentBlock.getEntry();
 		return true;
 	}
 
 	public IBuildpathEntry getSelection() {
 		return fSelection;
 	}
 
 	public void setSelection(IBuildpathEntry containerEntry) {
 		fSelection = containerEntry;
 		initializeFromSelection();
 	}
 
 	/**
 	 * Initlaizes the InterpreterEnvironment selection
 	 */
 	protected void initializeFromSelection() {
 		if (getControl() != null) {
 			if (fSelection == null) {
 				fInterpreterEnvironmentBlock.setUseDefaultInterpreter();
 			} else {
 				fInterpreterEnvironmentBlock.setPath(fSelection.getPath());
 			}
 			IStatus status = fInterpreterEnvironmentBlock.getStatus();
 			if (!status.isOK()) {
 				setErrorMessage(status.getMessage());
 				try {
 					IStatusHandler handler = DebugPlugin.getDefault()
 							.getStatusHandler(status);
 					if (handler != null) {
 						Boolean b = (Boolean) handler
 								.handleStatus(status, this);
 						if (b.booleanValue()) {
 							fInterpreterEnvironmentBlock.refreshInterpreters();
 						}
 					}
 				} catch (CoreException e) {
 				}
 			}
 		}
 	}
 
 	protected abstract AbstractInterpreterComboBlock getInterpreterBlock();
 
 	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
 	 */
 	public void createControl(Composite parent) {
 		Composite composite = new Composite(parent, SWT.NONE);
 		GridLayout layout = new GridLayout();
 		composite.setLayout(layout);
 		GridData gd = new GridData(GridData.FILL_BOTH);
 		composite.setLayoutData(gd);
 		composite.setFont(parent.getFont());
 		fInterpreterEnvironmentBlock = getInterpreterBlock();
 		fInterpreterEnvironmentBlock.setEnvironment(EnvironmentManager
 				.getEnvironment(getScriptProject()));
 		String currentLanguageNature = fInterpreterEnvironmentBlock
 				.getCurrentLanguageNature();
 		fInterpreterEnvironmentBlock
 				.setDefaultInterpreterDescriptor(new BuildInterpreterDescriptor(
 						currentLanguageNature, EnvironmentManager
 								.getEnvironment(getScriptProject()).getId()));
 		fInterpreterEnvironmentBlock
 				.setTitle(InterpretersMessages.InterpreterContainerWizardPage_3);
 		fInterpreterEnvironmentBlock.createControl(composite);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		fInterpreterEnvironmentBlock.getControl().setLayoutData(gd);
 		setControl(composite);
 		fInterpreterEnvironmentBlock
 				.addPropertyChangeListener(new IPropertyChangeListener() {
 					public void propertyChange(PropertyChangeEvent event) {
 						IStatus status = fInterpreterEnvironmentBlock
 								.getStatus();
 						if (status.isOK()) {
 							setErrorMessage(null);
 						} else {
 							setErrorMessage(status.getMessage());
 						}
 					}
 				});
 
 		setTitle(InterpretersMessages.InterpreterContainerWizardPage_Interpreter_System_Library_1);
 		setMessage(InterpretersMessages.InterpreterContainerWizardPage_Select_the_Interpreter_used_to_build_this_project__4);
 
 		initializeFromSelection();
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.jface.dialogs.IDialogPage#getImage()
 	 */
 	public Image getImage() {
 		return ScriptDebugImages.get(ScriptDebugImages.IMG_WIZBAN_LIBRARY);
 	}
 
 	public void initialize(IScriptProject project,
 			IBuildpathEntry[] currentEntries) {
 		this.scriptProject = project;
 		this.currentEntries = currentEntries;
 	}
 
 	public IScriptProject getScriptProject() {
 		return this.scriptProject;
 	}
 
 	public IBuildpathEntry[] getCurrentEntries() {
 		return this.currentEntries;
 	}
 }
