 /*******************************************************************************
  * Copyright (c) 2004, 2005 Sybase, Inc. and others. All rights reserved. This
  * program and the accompanying materials are made available under the terms of
  * the Eclipse Public License v1.0 which accompanies this distribution, and is
  * available at http://www.eclipse.org/legal/epl-v10.html Contributors: Sybase,
  * Inc. - initial API and implementation
  ******************************************************************************/
 
 package org.eclipse.jst.jsf.facesconfig.ui.wizard;
 
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.jdt.core.IType;
 import org.eclipse.jdt.core.JavaConventions;
import org.eclipse.jdt.core.JavaCore;
 import org.eclipse.jface.dialogs.Dialog;
 import org.eclipse.jface.wizard.WizardPage;
 import org.eclipse.jst.jsf.common.ui.internal.dialogfield.ClassButtonDialogField;
 import org.eclipse.jst.jsf.common.ui.internal.dialogfield.DialogField;
 import org.eclipse.jst.jsf.common.ui.internal.dialogfield.IDialogFieldChangeListener;
 import org.eclipse.jst.jsf.common.ui.internal.dialogfield.LayoutUtil;
 import org.eclipse.jst.jsf.facesconfig.ui.EditorPlugin;
 import org.eclipse.jst.jsf.facesconfig.ui.util.JavaClassUtils;
 import org.eclipse.osgi.util.NLS;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Group;
 import org.eclipse.swt.widgets.Label;
 
 /**
  * This class used to select an existing java class or generate a new one.
  * However, the actural generated new java class is left to other pages.
  * 
  * @author Xiao-guang Zhang
  */
 public class ManagedBeanClassSelectionPage extends WizardPage {
 	private static final int HORIZONTAL_INDENT_DEFAULT = 25;
 
 	/** the class name */
 	private String className;
 
 	/**
 	 * Search Section, including radio button, label, text and browser button.
 	 */
 	private Button searchRadioButton;
 
 	private ClassButtonDialogField classSearchDialogField;
 
 	/** selected type */
 	private IType searchedType;
 
 	/** Generate radio button */
 	private Button createRadioButton;
 
 	private IProject currentProject;
 
 	/**
 	 * Constructor.
 	 * 
 	 * @param project
 	 *            The project.
 	 */
 	public ManagedBeanClassSelectionPage(IProject project) {
 		super("JavaSelectionWizardPage"); //$NON-NLS-1$
 
 		currentProject = project;
 		setTitle(WizardMessages.JavaSelectionWizardPage_Title);
 		setDescription(WizardMessages.JavaSelectionWizardPage_Description);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
 	 */
 	public void createControl(Composite parent) {
 		Composite container = new Composite(parent, SWT.NULL);
 		GridLayout layout = new GridLayout();
 		layout.marginWidth = 10;
 		layout.marginHeight = 5;
 		container.setLayout(layout);
 		container.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
 
 		final Group group = new Group(container, SWT.NONE);
 
 		group.setLayout(new GridLayout());
 
 		group.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
 
 		createSearchSection(group);
 		createGenerateSection(group);
 
 		setControl(container);
 		Dialog.applyDialogFont(container);
 		setPageComplete(classSearchDialogField.getText().length() > 0);
 		EditorPlugin.getDefault().getWorkbench().getHelpSystem().setHelp(
 				container,
 				WizardMessages.JavaSelectionWizardPage_HelpContextID);
 	}
 
 	/**
 	 * enabled or disabled the search section including the labels and texts
 	 * 
 	 * @param enabled -
 	 *            enabled or disabled the search section
 	 */
 	private void enableSearchSection(boolean enabled) {
 		classSearchDialogField.setEnabled(enabled);
 	}
 
 	/**
 	 * Create the search section including the labels, texts, and browser
 	 * buttons
 	 * 
 	 * @param parent -
 	 *            parent composite control
 	 */
 	private void createSearchSection(Composite parent) {
 		searchRadioButton = new Button(parent, SWT.RADIO);
 		// JavaSelectionWizardPage.Search = Using an existing Java class
 		searchRadioButton
 				.setText(WizardMessages.JavaSelectionWizardPage_Search);
 		searchRadioButton.setSelection(true);
 		searchRadioButton.addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent e) {
 				createRadioButton.setSelection(!searchRadioButton
 						.getSelection());
 				enableSearchSection(searchRadioButton.getSelection());
 				verifyComplete();
 			}
 		});
 
 		Composite searchSection = new Composite(parent, SWT.NULL);
 
 		classSearchDialogField = new ClassButtonDialogField(currentProject);
 		classSearchDialogField.setHyperLink(null);
 		int numberOfControls = classSearchDialogField.getNumberOfControls();
 
 		GridLayout layout = new GridLayout();
 		layout.numColumns = numberOfControls;
 		layout.verticalSpacing = 9;
 		searchSection.setLayout(layout);
 		searchSection.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
 
 		classSearchDialogField.doFillIntoGrid(null, searchSection,
 				numberOfControls);
 
 		GridData gd = (GridData) classSearchDialogField.getLabelControl(null,
 				searchSection).getLayoutData();
 		gd.horizontalIndent = HORIZONTAL_INDENT_DEFAULT;
 
 		// JavaSelectionWizardPage.Search.ClassName = Qulified class name:
 		classSearchDialogField
 				.setLabelText(WizardMessages.JavaSelectionWizardPage_Search_ClassName);
 
 		LayoutUtil.setHorizontalGrabbing(classSearchDialogField.getTextControl(
 				null, searchSection));
 
 		classSearchDialogField
 				.setDialogFieldChangeListener(new IDialogFieldChangeListener() {
 					public void dialogFieldChanged(DialogField field) {
 						className = classSearchDialogField.getText();
 						verifyComplete();
 					}
 
 				});
 
 		Label searchDesp = new Label(searchSection, SWT.NONE);
 		searchDesp
 				.setText(WizardMessages.JavaSelectionWizardPage_Search_Description);
 
 		gd = new GridData();
 		gd.horizontalSpan = numberOfControls;
 		gd.horizontalIndent = HORIZONTAL_INDENT_DEFAULT;
 		searchDesp.setLayoutData(gd);
 	}
 
 	/**
 	 * Create the generation section including the labels, texts, and browser
 	 * buttons
 	 * 
 	 * @param parent -
 	 *            parent composite control
 	 */
 	private void createGenerateSection(Composite parent) {
 		createRadioButton = new Button(parent, SWT.RADIO);
 		createRadioButton
 				.setText(WizardMessages.JavaSelectionWizardPage_Create);
 		createRadioButton.addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent e) {
 				searchRadioButton.setSelection(!createRadioButton
 						.getSelection());
 				enableSearchSection(!createRadioButton.getSelection());
 				verifyComplete();
 			}
 		});
 
 		Composite createSection = new Composite(parent, SWT.NULL);
 		GridLayout layout = new GridLayout();
 		layout.numColumns = 3;
 		layout.verticalSpacing = 9;
 		createSection.setLayout(layout);
 		createSection.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
 
 		Label createDesp = new Label(createSection, SWT.NONE);
 		createDesp
 				.setText(WizardMessages.JavaSelectionWizardPage_Create_Description);
 
 		GridData gd = new GridData();
 		gd.horizontalIndent = HORIZONTAL_INDENT_DEFAULT;
 		createDesp.setLayoutData(gd);
 	}
 
 	/**
 	 * create a new java class or not
 	 * 
 	 * @return true if the create new java class radio button is selected
 	 */
 	public boolean isCreateNewJavaClass() {
 		return createRadioButton.getSelection();
 	}
 
 	/**
 	 * get the selected java type
 	 * 
 	 * @return - type
 	 */
 	public IType getSelectedType() {
 		searchedType = JavaClassUtils.getType(currentProject,
 				classSearchDialogField.getText());
 
 		return searchedType;
 	}
 
 	/**
 	 * @return the class name
 	 */
 	public String getClassName() {
 		return className;
 	}
 
 	/**
 	 * validate the selected or input java type name
 	 * 
 	 * @return
 	 */
 	private IStatus validateJavaTypeName() {
 		IStatus status = null;
 		if (classSearchDialogField.getText().length() == 0) {
 			status = new Status(
 					IStatus.ERROR,
 					EditorPlugin.getPluginId(),
 					-1,
 					NLS
 							.bind(
 									WizardMessages.JavaSelectionWizardPage_Error_ClassIsEmpty,
 									classSearchDialogField.getText()), null);
 		} else {
 			// ensure the name follows the java conventsions.
 			status = JavaConventions
					    .validateJavaTypeName(classSearchDialogField.getText(), JavaCore.VERSION_1_3,JavaCore.VERSION_1_3);
 			if (status.getSeverity() != IStatus.ERROR) {
 				// ensure the input or selected type is defined in the current
 				// project
 				if (getSelectedType() == null) {
 					status = new Status(
 							IStatus.ERROR,
 							EditorPlugin.getPluginId(),
 							-1,
 							NLS
 									.bind(
 											WizardMessages.JavaSelectionWizardPage_Error_ClassIsNotDefined,
 											classSearchDialogField.getText()),
 							null);
 				}
 			}
 		}
 		return status;
 	}
 
 	/**
 	 * verify and update the complete status
 	 */
 	private void verifyComplete() {
 		IStatus status = null;
 
 		if (isCreateNewJavaClass()) {
 			setPageComplete(true);
 			setMessage(null);
 			setErrorMessage(null);
 		} else {
 			if (searchRadioButton.getSelection()) {
 				status = validateJavaTypeName();
 			}
 			String errorMessage = null;
 			setPageComplete(status.getSeverity() != IStatus.ERROR);
 
 			if (status.getSeverity() == IStatus.ERROR) {
 				errorMessage = status.getMessage();
 			}
 
 			if (errorMessage != null) {
 				setErrorMessage(errorMessage);
 			} else {
 				setErrorMessage(null);
 			}
 
 			if (status.getSeverity() != IStatus.OK) {
 				setMessage(status.getMessage());
 			} else {
 				setMessage(null);
 			}
 		}
 	}
 
 }
