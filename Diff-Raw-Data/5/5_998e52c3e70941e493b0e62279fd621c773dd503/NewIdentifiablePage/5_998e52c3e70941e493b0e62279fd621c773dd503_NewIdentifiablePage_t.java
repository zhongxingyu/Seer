 // NewIdentifiablePage.java
 package org.eclipse.stem.ui.wizards;
 
 /*******************************************************************************
  * Copyright (c) 2008 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     IBM Corporation - initial API and implementation
  *******************************************************************************/
 
 import java.util.List;
 
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.jface.viewers.ISelection;
 import org.eclipse.jface.viewers.IStructuredSelection;
 import org.eclipse.jface.wizard.WizardPage;
 import org.eclipse.stem.core.common.Identifiable;
 import org.eclipse.stem.core.common.presentation.CoreEditorAdvisor;
 import org.eclipse.stem.ui.Activator;
 import org.eclipse.stem.ui.Utility;
 import org.eclipse.stem.ui.views.explorer.IdentifiableTreeNode;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.ModifyEvent;
 import org.eclipse.swt.events.ModifyListener;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.events.SelectionListener;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Combo;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.Text;
 
 /**
  * This class is the {@link WizardPage} that is the super class of all wizard
  * pages used to specify the details of an {@link Identifiable}.
  * 
  * @see DublinCorePage
  * @see NewIdentifiableWizard
  */
 abstract public class NewIdentifiablePage extends WizardPage {
 
 	/**
 	 * This {@link Combo} will be populated with the names of the projects in
 	 * the workspace that the {@link Identifiable} can be created in.
 	 */
 	protected Combo projectNamesCombo;
 
 	/**
 	 * The name of the file in which the {@link Identifiable} will be
 	 * serialized.
 	 * 
 	 * @see NewIdentifiableWizard#createIdentifiable(org.eclipse.core.runtime.IProgressMonitor,
 	 *      org.eclipse.emf.common.util.URI)
 	 */
 	protected Text serializationFileNameField;
 
 	/**
 	 * This {@link Composite} is created by the subclasses with the call to
 	 * {@link #createSpecificComposite(Composite)}. This {@link Composite}
 	 * contains all of the interface features for defining the details of the
 	 * {@link Identifiable} to be created.
 	 */
 	private Composite specificComposite;
 
 	/**
 	 * The collection of STEM projects in the current workspace. This will be
 	 * used to populate {@link #projectNamesCombo}.
 	 */
 	protected List<IProject> stemProjects = null;
 
 	/**
 	 * Remember the last project used if the current selection is not referencing 
 	 * an existing project
 	 */
 	
 	protected static IProject lastProject = null;
 	
 	/**
 	 * This {@link ModifyListener} is attached to those widgets whose contents
 	 * need to be verified for correctness. Whenever one of those is modified, a
 	 * call to {@link #validatePage()} is made which then returns
 	 * <code>true</code> or <code>false</code>. This value is passed to
 	 * {@link #setPageComplete(boolean)} which then enables or disables the
 	 * buttons at the bottom of the page. The attachment is done in the method
 	 * {@link #createProjectComposite(Composite)} and in
 	 * {@link #createSerializationComposite(Composite)}
 	 */
 	protected ModifyListener projectValidator = new ModifyListener() {
 		public void modifyText(@SuppressWarnings("unused") final ModifyEvent e) {
 			setPageComplete(validatePage());
 		}
 	};
 
 	/**
 	 * @param pageName
 	 *            the name of the page, this is passed up by a subclass.
 	 */
 	protected NewIdentifiablePage(final String pageName) {
 		super(pageName);
 	} // NewIdentifiablePage
 
 	/**
 	 * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
 	 */
 	public void createControl(final Composite parent) {
 		/*
 		 * Creates a {@link Composite} and calls {@link  #setControl(org.eclipse.swt.widgets.Control)}. Basically, it adds
 		 * three things to the Composite it creates, a "project composite" an
 		 * "identifiable composite" and a "specific composite". The first is
 		 * common to all pages and is used to specify which project the
 		 * Identifiable will be serialized in. The second, also common to all
 		 * pages, is used to specify the name of the serialization file for the
 		 * Identifiable. The third is for the specific details for defining the
 		 * Identifiable.
 		 */
 		// Page Composite
 		final Composite pageComposite = new Composite(parent, SWT.NONE);
 		final GridLayout pageLayout = new GridLayout();
 		pageLayout.numColumns = 1;
 		pageLayout.verticalSpacing = 12;
 		pageComposite.setLayout(pageLayout);
 
 		final GridData pageLayoutData = new GridData();
 		pageLayoutData.verticalAlignment = GridData.FILL;
 		pageLayoutData.grabExcessVerticalSpace = true;
 		pageLayoutData.horizontalAlignment = GridData.FILL;
 		pageComposite.setLayout(pageLayout);
 
 		createProjectComposite(pageComposite);
 
 		// Horizontal Separator
 		final Label seperator = new Label(pageComposite, SWT.HORIZONTAL
 				| SWT.SEPARATOR);
 		seperator.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
 
 		final Composite identifiableComposite = createSerializationComposite(pageComposite);
 		identifiableComposite.setLayoutData(new GridData(
 				GridData.FILL_HORIZONTAL));
 
 		// Add anything that is specific to the type of wizard
 		specificComposite = createSpecificComposite(pageComposite);
 		// Was there anything?
 		if (specificComposite != null) {
 			// Yes
 			final GridData gd = new GridData(GridData.FILL_HORIZONTAL);
 			gd.grabExcessHorizontalSpace = true;
 			specificComposite.setLayoutData(gd);
 		} // if
 
 		setPageComplete(validatePage());
 		parent.pack();
 		setControl(pageComposite);
 	} // createControl
 
 	/**
 	 * @param parent
 	 *            the parent {@link Composite}
 	 */
 	private void createProjectComposite(final Composite parent) {
 		final Composite projectComposite = new Composite(parent, SWT.None);
 		final GridLayout projectCompositeLayout = new GridLayout();
 		projectCompositeLayout.numColumns = 3;
 		projectCompositeLayout.verticalSpacing = 12;
 		projectCompositeLayout.marginHeight = 0;
 		projectCompositeLayout.marginWidth = 0;
 		projectComposite.setLayout(projectCompositeLayout);
 
 		final GridData projectCompositeLayoutData = new GridData();
 		projectCompositeLayoutData.horizontalAlignment = GridData.FILL;
 		projectComposite.setLayoutData(projectCompositeLayoutData);
 
 		// Project Label
 		final Label projectLabel = new Label(projectComposite, SWT.LEFT);
 		projectLabel.setText(Messages.getString("NIdWiz.project") + ":"); //$NON-NLS-1$ //$NON-NLS-2$
 		final GridData projectLabelLayoutData = new GridData();
 		projectLabelLayoutData.horizontalSpan = 1;
 		projectLabelLayoutData.horizontalAlignment = GridData.FILL;
 		projectLabel.setLayoutData(projectLabelLayoutData);
 
 		// Project Combo
 		projectNamesCombo = new Combo(projectComposite, SWT.BORDER
 				| SWT.READ_ONLY);
 		final GridData projectFieldLayoutData = new GridData();
 		projectFieldLayoutData.horizontalAlignment = GridData.FILL;
 		projectFieldLayoutData.grabExcessHorizontalSpace = true;
 		projectFieldLayoutData.horizontalSpan = 1;
 		projectNamesCombo.setLayoutData(projectFieldLayoutData);
 
 		// initialize the project field to point to the selected project
 		initializeProjectCombo(projectNamesCombo);
 
 		// The projectValidator will look at the contents of the field and
 		// determine if it refers to a valid project.
 		projectNamesCombo.addModifyListener(projectValidator);
 		
 		// Remember the selection done for future 
 		projectNamesCombo.addSelectionListener(new SelectionListener() {
 			
 			public void widgetSelected(SelectionEvent e) {
 				NewIdentifiablePage.lastProject = NewIdentifiablePage.this.getSelectedProject();
 			}
 			
 			public void widgetDefaultSelected(SelectionEvent e) {
 				
 			}
 		});
 		// Project Browse Button
 		final Button projectFieldBrowseButton = new Button(projectComposite,
 				SWT.PUSH);
 		projectFieldBrowseButton.setText(Messages.getString("NIdWiz.browse")); //$NON-NLS-1$
 
 		projectFieldBrowseButton.addSelectionListener(new SelectionAdapter() {
 
 			/**
 			 * The browse button will pop-up a file search dialog and allow the
 			 * user to specify the project
 			 * 
 			 * @see org.eclipse.swt.events.SelectionAdapter#widgetSelected(org.eclipse.swt.events.SelectionEvent)
 			 */
 			@Override
 			public void widgetSelected(
 					@SuppressWarnings("unused") final SelectionEvent e) {
 				final String filePath = CoreEditorAdvisor.openFilePathDialog(
 						getShell(), "*", SWT.OPEN); //$NON-NLS-1$
 				if (filePath != null) {
 					projectNamesCombo.setText(filePath);
 				}
 			}
 		});
 	} // createProjectComposite
 
 	/**
 	 * @param parent
 	 *            the parent {@link Composite}
 	 * @return a {@link Composite} in which the user can specify the name of the
 	 *         file in which the {@link Identifiable} should be serialized.
 	 */
 	private Composite createSerializationComposite(final Composite parent) {
 		final Composite retValue = new Composite(parent, SWT.NONE);
 		GridData gridData;
 
 		final GridLayout identifableCompositeLayout = new GridLayout();
 		identifableCompositeLayout.numColumns = 3;
 		identifableCompositeLayout.verticalSpacing = 12;
 		identifableCompositeLayout.marginHeight = 0;
 		identifableCompositeLayout.marginWidth = 0;
 		retValue.setLayout(identifableCompositeLayout);
 		gridData = new GridData();
 		// identifiableCompositeLayoutData.horizontalSpan=1;
 		gridData.horizontalAlignment = GridData.FILL;
 		// identifiableCompositeLayoutData.grabExcessHorizontalSpace = true;
 		retValue.setLayoutData(gridData);
 
 		// Serialization file name label
 		final Label serializationFileNameLabel = new Label(retValue, SWT.NONE);
 		serializationFileNameLabel
 				.setText(Messages.getString("NIdWiz.name") + ":"); //$NON-NLS-1$ //$NON-NLS-2$
 		gridData = new GridData();
 		gridData.horizontalSpan = 1;
 		gridData.horizontalAlignment = SWT.LEAD;
 		serializationFileNameLabel.setLayoutData(gridData);
 
 		serializationFileNameField = new Text(retValue, SWT.BORDER);
 		gridData = new GridData();
 		gridData.horizontalAlignment = GridData.FILL;
 		gridData.grabExcessHorizontalSpace = true;
 		gridData.horizontalSpan = 1;
 		serializationFileNameField.setLayoutData(gridData);
 
 		serializationFileNameField.addModifyListener(projectValidator);
 
 		return retValue;
 	} // createSerializationComposite
 
 	/**
 	 * @param parent
 	 *            the parent {@link Composite}, typically the one created in
 	 *            {@link #createControl(Composite)}.
 	 */
 	protected Composite createSpecificComposite(final Composite parent) {
 		// The NewModelWizard doesn't provide a "specific composite" so we don't
 		// make this method abstract.
 		return null;
 	} // createSpecificComposite
 
 	/**
 	 * @return the project selected by the user.
 	 */
 	public IProject getSelectedProject() {
		int ind = projectNamesCombo.getSelectionIndex();
		if(ind != -1)
			return stemProjects.get(projectNamesCombo.getSelectionIndex());
		return null;
 	} // getSelectedProject
 
 	/**
 	 * @param projectCombo
 	 *            the {@link Combo} to be initialized with the names of the
 	 *            projects.
 	 */
 	private void initializeProjectCombo(final Combo projectCombo) {
 
 		IProject selectedProject = getWorkSpaceSelectedProject();
 
 		if(selectedProject == null) selectedProject = lastProject;
 		
 		// Get the Projects that are STEM projects
 		int index = 0;
 		int i = 0;
 		for (final IProject project : getSTEMProjects()) {
 			// Is the STEM project selected?
 			if (project.equals(selectedProject)) {
 				// Yes
 				index = i;
 			}
 
 			final String projectName = project.getName();
 			projectCombo.add(projectName);
 			i++;
 		} // for each STEM project
 		projectCombo.setFocus();
 		projectCombo.select(index);
 		NewIdentifiablePage.lastProject = this.getSelectedProject();
 	} // initializeProjectField
 
 	private IProject getWorkSpaceSelectedProject() {
 		IProject retValue = null;
 		// Find the name of the currently selected project, if there is one
 		final ISelection selection = Activator.getDefault().getWorkbench()
 				.getActiveWorkbenchWindow().getSelectionService()
 				.getSelection();
 		// Structured Selection that might have a project?
 		if (selection instanceof IStructuredSelection) {
 			for (Object obj : ((IStructuredSelection) selection).toList()) {
 				// Project?
 				if (obj instanceof IProject) {
 					retValue = (IProject) obj;
 					break;
 				}
 				// File or Folder? (in a Project)
 				else if (obj instanceof IResource) {
 					// Yes
 					retValue = ((IResource) obj).getProject();
 					break;
 				} // else if File or Folder
 				// Identifiable?
 				else if (obj instanceof Identifiable) {
 					final IPath path = new Path(((Identifiable) obj).getURI()
 							.toPlatformString(true));
 					retValue = ResourcesPlugin.getWorkspace().getRoot()
 							.getFile(path).getProject();
 					break;
 				} // else if Identifiable
 				// IdentifiableTreeNode?
 				else if (obj instanceof IdentifiableTreeNode) {
 					retValue = ((IdentifiableTreeNode) obj).getProject();
 					break;
 				} // else if IdentifiableTreeNode
 
 			} // for each selected object
 		} // if structured selection
 
 		return retValue;
 	} // getWorkSpaceSelectedProject
 
 	/**
 	 * @return a List of the projects in the workspace that have STEM Project
 	 *         "nature"
 	 * 
 	 * @see org.eclipse.stem.core.Constants#ID_STEM_PROJECT_NATURE
 	 */
 	private List<IProject> getSTEMProjects() {
 		// Refresh
 		stemProjects = Utility.getSTEMProjectsFromWorkspace();
 		return stemProjects;
 	} // getSTEMProjects
 
 	/**
 	 * @return <code>true</code> if the page is complete and ready to go
 	 */
 	protected boolean validatePage() {
 		boolean retValue = true;
 		setErrorMessage(null);
 
 		// Are there any projects?
 		if (getSTEMProjects().size() == 0) {
 			// No
 			setErrorMessage(Messages.getString("NIdWiz.noProjErr"));
 			retValue = false;
 		} // if no projects
 
 		// Is the serialization file name missing?
 		if (retValue && serializationFileNameField.getText() == null
 				|| "".equals(serializationFileNameField.getText().trim())) {
 			// Yes
 			setErrorMessage(Messages.getString("NIdWiz.nameMissingErr")); //$NON-NLS-1$
 			retValue = false;
 		}
 
 		// Is the serialization file name valid?
 		if (retValue && !serializationFileNameIsValid()) {
 			// No
 			setErrorMessage(Messages.getString("NIdWiz.nameErr")); //$NON-NLS-1$
 			retValue = false;
 		}
 
 		return retValue;
 	} // validatePage
 
 	/**
 	 * @return <code>true</code> if the serialization file name specified for
 	 *         the {@link Identifiable} to be created is a valid name.
 	 */
 	private boolean serializationFileNameIsValid() {
 		final String name = serializationFileNameField.getText();
 
 		if (name == null) {
 			return false;
 		}
 
 		final String trimedName = name.trim();
 
 		return !(trimedName.equals("") || trimedName.contains(" ")); //$NON-NLS-1$ //$NON-NLS-2$
 	} // serializationFileNameIsValid
 
 	/**
 	 * @return the value for the {@link DublinCore#getTitle()} attribute on the
 	 *         {@link DublinCorePage}.
 	 */
 	protected String getDCTitle() {
 		return getDCDescription();
 	} // getDCTitle
 
 	/**
 	 * @return the value for a {@link DublinCore#getDescription()} attribute on
 	 *         the {@link DublinCorePage}.
 	 */
 	abstract protected String getDCDescription();
 
 	/**
 	 * @return a value suitable for {@link DublinCore#setValid(String)}
 	 */
 	protected String getDCValidDateRange() {
 		return "";
 	} // getDCValidDateRange
 } // NewIdentifiablePage
