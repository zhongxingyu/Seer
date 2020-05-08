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
 
 package org.jboss.tools.jbpm.convert.bpmnto.wizard;
 
 import org.eclipse.core.resources.IContainer;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.jface.viewers.ISelection;
 import org.eclipse.jface.viewers.ISelectionChangedListener;
 import org.eclipse.jface.viewers.IStructuredSelection;
 import org.eclipse.jface.viewers.ITreeSelection;
 import org.eclipse.jface.viewers.SelectionChangedEvent;
 import org.eclipse.jface.viewers.TreePath;
 import org.eclipse.jface.viewers.TreeSelection;
 import org.eclipse.jface.viewers.TreeViewer;
 import org.eclipse.jface.viewers.Viewer;
 import org.eclipse.jface.viewers.ViewerFilter;
 import org.eclipse.jface.wizard.IWizard;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.events.SelectionListener;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.ui.model.WorkbenchContentProvider;
 import org.eclipse.ui.model.WorkbenchLabelProvider;
 import org.jboss.tools.jbpm.convert.b2j.messages.B2JMessages;
 
 /**
  * @author Grid Qian
  * 
  *         the wizardpage for the generated file location
  */
 public class GeneratedFileLocationPage extends AbstractConvertWizardPage {
 
 	private TreeViewer viewer;
 	private ISelection currentSelection;
 	private Button button;
 	private IWizard wizard;
 
 	protected GeneratedFileLocationPage(String pageName, String title,
 			String tableTitle, String description) {
 		super(pageName, title, tableTitle, description);
 	}
 
 	public void createTableViewer(Composite composite) {
 		viewer = new TreeViewer(composite, SWT.BORDER | SWT.MULTI
 				| SWT.H_SCROLL | SWT.V_SCROLL);
 		viewer.getTree().setLayoutData(new GridData(GridData.FILL_BOTH));
 		viewer.setLabelProvider(new WorkbenchLabelProvider());
 		WorkbenchContentProvider cp = new WorkbenchContentProvider();
 		viewer.setContentProvider(cp);
 		viewer.setFilters(new ViewerFilter[] { new ProFilter() });
 		viewer.addPostSelectionChangedListener(new ISelectionChangedListener() {
 			public void selectionChanged(SelectionChangedEvent event) {
 				updateControls();
 				currentSelection = viewer.getSelection();
 				((BpmnToWizard) wizard)
 						.setTargetLocationSelection((IStructuredSelection) currentSelection);
 
 			}
 		});
 	}
 
 	public void addOtherAreas(Composite composite) {
 		button = new Button(composite, SWT.CHECK | SWT.NONE);
		button
				.setText(B2JMessages.Bpmn_GeneratedFile_Location_WizardPage_CheckBox);
 		button.setFont(composite.getFont());
 		button.addSelectionListener(new SelectionListener() {
 			public void widgetDefaultSelected(SelectionEvent arg0) {
 			}
 
 			public void widgetSelected(SelectionEvent arg0) {
 				((BpmnToWizard) wizard).setOverWrite(button.getSelection());
 			}
 		});
 	}
 
 	public void initializePage() {
 		wizard = this.getWizard();
 		viewer.setInput(ResourcesPlugin.getWorkspace());
 		if (currentSelection != null
 				&& currentSelection instanceof ITreeSelection) {
 			// Select the parent project of this first bpmn file chosen
 			ITreeSelection node = (ITreeSelection) currentSelection;
 			TreePath[] paths = node.getPaths();
 			if (paths.length == 0) {
 				return;
 			}
 			TreePath projPath = new TreePath(new Object[] { paths[0]
 					.getFirstSegment() });
 			TreeSelection projSel = new TreeSelection(projPath);
 			viewer.setSelection(projSel, true);
 		}
 		button.setSelection(((BpmnToWizard) wizard).isOverWrite());
 	}
 
 	public boolean isPageComplete() {
		if (viewer == null || viewer.getSelection() == null) {
 			return false;
 		}
 		return true;
 	}
 
 	public ISelection getSelection() {
 		return currentSelection;
 	}
 
 	public void setSelection(ISelection currentSelection) {
 		this.currentSelection = currentSelection;
 	}
 
 }
 
 class ProFilter extends ViewerFilter {
 	@Override
 	public boolean select(Viewer viewer, Object parent, Object element) {
 		if (element instanceof IContainer) {
 			return ((IContainer) element).getProject().isAccessible();
 		} else {
 			return false;
 		}
 	}
 }
