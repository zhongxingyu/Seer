 /*******************************************************************************
  * Copyright (c) 2010-2011 Red Hat Inc. and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     Red Hat Inc. - initial API and implementation
  *******************************************************************************/
 package org.fedoraproject.eclipse.packager.git.internal.ui;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 
 import org.eclipse.jface.dialogs.IMessageProvider;
 import org.eclipse.jface.layout.GridDataFactory;
 import org.eclipse.jface.resource.ImageDescriptor;
 import org.eclipse.jface.viewers.IStructuredSelection;
 import org.eclipse.jface.viewers.ITreeSelection;
 import org.eclipse.jface.viewers.TreePath;
 import org.eclipse.jface.wizard.WizardPage;
 import org.eclipse.osgi.util.NLS;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.ModifyEvent;
 import org.eclipse.swt.events.ModifyListener;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.events.SelectionListener;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.swt.widgets.Group;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.Text;
 import org.eclipse.ui.IWorkingSet;
 import org.eclipse.ui.dialogs.WorkingSetConfigurationBlock;
 import org.fedoraproject.eclipse.packager.FedoraSSL;
 import org.fedoraproject.eclipse.packager.git.Activator;
 import org.fedoraproject.eclipse.packager.git.FedoraPackagerGitText;
 
 /**
  * Page for selecting the module to clone.
  *
  */
 public class SelectModulePage extends WizardPage {
 
 	private Text projectText;
 	private Button anonymousCloneBtn;
 	private String fasUser;
 
 	private final WorkingSetGroup workingSetGroup;
 
 	private static final IWorkingSet[] EMPTY_WORKING_SET_ARRAY = new IWorkingSet[0];
 	private static final int GROUP_SPAN = 2;
 
 	/**
 	 * @param fasUser Either FedoraSSL.UNKNOWN_USER or the extracted FAS user name
 	 */
 	protected SelectModulePage(String fasUser) {
 		super(FedoraPackagerGitText.SelectModulePage_packageSelection);
 		setTitle(FedoraPackagerGitText.SelectModulePage_packageSelection);
 		setDescription(FedoraPackagerGitText.SelectModulePage_choosePackage); 
 		this.setImageDescriptor(ImageDescriptor.createFromFile(getClass(),
 				"/icons/wizban/newconnect_wizban.png")); //$NON-NLS-1$
 
 		workingSetGroup= new WorkingSetGroup();
 		setWorkingSets(EMPTY_WORKING_SET_ARRAY);
 		setPageComplete(false);
 		this.fasUser = fasUser;
 	}
 
 	/**
 	 * The wizard owning this page can call this method to initialise fields using the
 	 * current selection.
 	 * 
 	 * @param selection the current object selection
 	 */
 	public void init(IStructuredSelection selection) {
 		setWorkingSets(getSelectedWorkingSet(selection));
 	}
 
 	@Override
 	public void createControl(Composite parent) {
 		Composite composite = new Composite(parent, SWT.NONE);
 		GridLayout layout = new GridLayout();
 		composite.setLayout(layout);
 
 		// Package name controls
 		Label label = new Label(composite, SWT.NONE);
 		label.setText(FedoraPackagerGitText.SelectModulePage_packageName); 
 		projectText = new Text(composite, SWT.BORDER | SWT.SINGLE);
 		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
 		projectText.setLayoutData(gd);
 		projectText.setFocus();
 		projectText.addModifyListener(new ModifyListener(){
 
 			@Override
 			public void modifyText(ModifyEvent e) {
				if (projectText.getText() == null || projectText.getText().equals("")){ //$NON-NLS-1$
 					setPageComplete(false);
 					setErrorMessage(null);
 				} else if (projectText.getText().trim().equals("")){ //$NON-NLS-1$
 					setPageComplete(false);
 					setErrorMessage(FedoraPackagerGitText.SelectModulePage_badPackageName);
 				} else {
 					setPageComplete(true);
 					setErrorMessage(null);
 				}
 			}
 		});
 
 		final boolean isUnknownUser = fasUser.equals(FedoraSSL.UNKNOWN_USER);
 		
 		// Options group
 		Group optionsGroup = new Group(composite, SWT.SHADOW_ETCHED_IN);
 		optionsGroup.setText(FedoraPackagerGitText.SelectModulePage_optionsGroup);
 		optionsGroup.setLayout(new GridLayout());
 		// Clone anonymously button
 		anonymousCloneBtn = new Button(optionsGroup, SWT.CHECK);
 		anonymousCloneBtn.setText(FedoraPackagerGitText.SelectModulePage_anonymousCheckout);
 		anonymousCloneBtn.setSelection(isUnknownUser);
 		// disable checkbox if there is no choice of cloning non-anonymously
 		anonymousCloneBtn.setEnabled(!isUnknownUser);
 		anonymousCloneBtn.addSelectionListener(new SelectionListener() {
 
 			@Override
 			public void widgetSelected(SelectionEvent e) {
 				if (anonymousCloneBtn.getSelection() && !isUnknownUser) {
 					setMessage(
 							FedoraPackagerGitText.SelectModulePage_userSelectedAnonymousCloneInfoMsg,
 							IMessageProvider.INFORMATION);
 				} else if (isUnknownUser) {
 					setMessage(
 							FedoraPackagerGitText.SelectModulePage_anonymousCloneInfoMsg,
 							IMessageProvider.INFORMATION);
 				} else {
 					setMessage(
 							NLS.bind(
 									FedoraPackagerGitText.SelectModulePage_sshCloneInfoMsg,
 									fasUser), IMessageProvider.INFORMATION);
 				}
 			}
 
 			@Override
 			public void widgetDefaultSelected(SelectionEvent e) {
 				// do nothing
 			}
 		});
 		GridDataFactory.fillDefaults().grab(true, false).span(GROUP_SPAN, 1)
 		.applyTo(optionsGroup);
 		updateMargins(optionsGroup);
 
 		// Working set controls
 		Control workingSetControl = workingSetGroup.createControl(composite);
 		workingSetControl.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
 
 		
 		// Set info message indicating which kind of clone we are about
 		// to perform
 		if (isUnknownUser) {
 			setMessage(FedoraPackagerGitText.SelectModulePage_anonymousCloneInfoMsg, IMessageProvider.INFORMATION);
 		} else {
 			setMessage(NLS.bind(FedoraPackagerGitText.SelectModulePage_sshCloneInfoMsg, fasUser), IMessageProvider.INFORMATION);
 		}
 		
 		setControl(composite);
 	}
 	
 	/**
 	 * 
 	 * @return {code true} if the user chose an anonymous clone, {code false} otherwise.
 	 */
 	public boolean getCloneAnonymousButtonChecked() {
 		return anonymousCloneBtn.getSelection();
 		
 	}
 
 	/**
 	 * @return The name of the package to clone.
 	 */
 	public String getPackageName() {
 		return projectText.getText();
 	}
 
 	/**
 	 * Returns the working sets to which the new project should be added.
 	 *
 	 * @return the selected working sets to which the new project should be added
 	 */
 	public IWorkingSet[] getWorkingSets() {
 		return workingSetGroup.getSelectedWorkingSets();
 	}
 
 	/**
 	 * Sets the working sets to which the new project should be added.
 	 *
 	 * @param workingSets the initial selected working sets
 	 */
 	public void setWorkingSets(IWorkingSet[] workingSets) {
 		if (workingSets == null) {
 			throw new IllegalArgumentException();
 		}
 		workingSetGroup.setWorkingSets(workingSets);
 	}
 
 	/**
 	 * Try our best to set the working sets field to something sensible based on the
 	 * current selection.
 	 */
 	private IWorkingSet[] getSelectedWorkingSet(IStructuredSelection selection) {
 		if (!(selection instanceof ITreeSelection))
 			return EMPTY_WORKING_SET_ARRAY;
 
 		ITreeSelection treeSelection = (ITreeSelection) selection;
 		if (treeSelection.isEmpty())
 			return EMPTY_WORKING_SET_ARRAY;
 
 		@SuppressWarnings("unchecked")
 		List<Object> elements= treeSelection.toList();
 		if (elements.size() == 1) {
 			Object element = elements.get(0);
 			TreePath[] paths = treeSelection.getPathsFor(element);
 			if (paths.length != 1 || paths[0].getSegmentCount() == 0)
 				return EMPTY_WORKING_SET_ARRAY;
 
 			Object candidate = paths[0].getSegment(0);
 			if (!(candidate instanceof IWorkingSet))
 				return EMPTY_WORKING_SET_ARRAY;
 
 			IWorkingSet workingSetCandidate = (IWorkingSet) candidate;
 			if (!workingSetCandidate.isAggregateWorkingSet())
 				return new IWorkingSet[] { workingSetCandidate };
 
 			return EMPTY_WORKING_SET_ARRAY;
 		}
 
 		ArrayList<IWorkingSet> result = new ArrayList<IWorkingSet>();
 		for (Iterator<Object> iterator = elements.iterator(); iterator.hasNext();) {
 			Object element = iterator.next();
 			if (element instanceof IWorkingSet && !((IWorkingSet) element).isAggregateWorkingSet()) {
 				result.add((IWorkingSet)element);
 			}
 		}
 
 		if (result.size() > 0) {
 			return result.toArray(new IWorkingSet[result.size()]);
 		} else {
 			return EMPTY_WORKING_SET_ARRAY;
 		}
 	}
 
 	private void updateMargins(Group group) {
 		// make sure there is some room between the group border
 		// and the controls in the group
 		GridLayout layout = (GridLayout) group.getLayout();
 		layout.marginWidth = 5;
 		layout.marginHeight = 5;
 	}
 
 	/**
 	 * Little class to encapsulate the working set group of controls.
 	 */
 	private final class WorkingSetGroup {
 
 		private WorkingSetConfigurationBlock workingSetBlock;
 
 		public WorkingSetGroup() {
 			String[] workingSetIds = new String[] { "org.eclipse.ui.resourceWorkingSetPage" }; //$NON-NLS-1$
 			workingSetBlock = new WorkingSetConfigurationBlock(workingSetIds,
 					Activator.getDefault().getDialogSettings());
 		}
 
 		public Control createControl(Composite composite) {
 			Group workingSetGroup = new Group(composite, SWT.NONE);
 			workingSetGroup.setFont(composite.getFont());
 			workingSetGroup.setText(FedoraPackagerGitText.SelectModulePage_workingSets);
 			workingSetGroup.setLayout(new GridLayout(1, false));
 
 			workingSetBlock.createContent(workingSetGroup);
 
 			return workingSetGroup;
 		}
 
 		public void setWorkingSets(IWorkingSet[] workingSets) {
 			workingSetBlock.setWorkingSets(workingSets);
 		}
 
 		public IWorkingSet[] getSelectedWorkingSets() {
 			return workingSetBlock.getSelectedWorkingSets();
 		}
 	}
 }
