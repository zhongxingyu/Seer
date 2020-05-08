 /**
  * Copyright (c) 2009 Anyware Technologies and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *     Anyware Technologies - initial API and implementation
  *
 * $Id: ServicesPage.java,v 1.5 2009/07/28 16:23:51 bcabe Exp $
  */
 package org.eclipse.pde.ds.ui.internal.editor;
 
 import org.eclipse.core.databinding.DataBindingContext;
 import org.eclipse.emf.common.command.Command;
 import org.eclipse.emf.edit.command.*;
 import org.eclipse.emf.edit.domain.AdapterFactoryEditingDomain;
 import org.eclipse.emf.edit.domain.EditingDomain;
 import org.eclipse.jface.databinding.viewers.ViewerProperties;
 import org.eclipse.jface.viewers.*;
 import org.eclipse.pde.ds.scr.*;
 import org.eclipse.pde.ds.ui.internal.editor.masterdetail.ServicesMasterDetail;
 import org.eclipse.pde.emfforms.editor.AbstractEmfFormPage;
 import org.eclipse.pde.emfforms.editor.EmfFormEditor;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.widgets.Composite;
 
 public class ServicesPage extends AbstractEmfFormPage {
 
 	private ServicesMasterDetail _servicesMasterDetail;
 
 	public final static String ID = "ds.services"; //$NON-NLS-1$
 
 	public ServicesPage(EmfFormEditor<?> editor) {
 		super(editor, 1, true);
 	}
 
 	public void bind(DataBindingContext bindingContext) {
 		final EditingDomain editingDomain = ((DSEditor) getEditor()).getEditingDomain();
 
 		bindingContext.bindValue(ViewerProperties.input().observe(_servicesMasterDetail.getTreeViewer()), getEditor().getInputObservable());
 
 		_servicesMasterDetail.getBtnAddProvided().addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent e) {
 				Object sel = ((IStructuredSelection) _servicesMasterDetail.getTreeViewer().getSelection()).getFirstElement();
 				int idx = CommandParameter.NO_INDEX;
 				if (sel != null) {
 					Object unwrappedElement = AdapterFactoryEditingDomain.unwrap(sel);
 					idx = ((Component) getEditor().getInputObservable().getValue()).getService().getProvide().indexOf(unwrappedElement);
 				}
 
 				Provide p = ScrFactory.eINSTANCE.createProvide();
 				Command command = AddCommand.create(editingDomain, ((Component) getEditor().getInputObservable().getValue()).getService(), ScrPackage.Literals.SERVICE__PROVIDE, p, idx);
 				editingDomain.getCommandStack().execute(command);
 
				getViewer().refresh(); // FIXME this should not be needed :/
 				getViewer().setSelection(new StructuredSelection(AdapterFactoryEditingDomain.getWrapper(p, editingDomain)), true);
 			}
 		});
 
 		_servicesMasterDetail.getBtnAddRequired().addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent e) {
 				Object sel = ((IStructuredSelection) _servicesMasterDetail.getTreeViewer().getSelection()).getFirstElement();
 				int idx = CommandParameter.NO_INDEX;
 				if (sel != null) {
 					Object unwrappedElement = AdapterFactoryEditingDomain.unwrap(sel);
 					idx = ((Component) getEditor().getInputObservable().getValue()).getReference().indexOf(unwrappedElement);
 				}
 
 				Reference r = ScrFactory.eINSTANCE.createReference();
 				Command command = AddCommand.create(editingDomain, getEditor().getInputObservable().getValue(), ScrPackage.Literals.COMPONENT__REFERENCE, r, idx);
 				editingDomain.getCommandStack().execute(command);
 
 				getViewer().setSelection(new StructuredSelection(AdapterFactoryEditingDomain.getWrapper(r, editingDomain)), true);
 			}
 		});
 
 		_servicesMasterDetail.getRemoveButton().addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent e) {
 				Object sel = ((IStructuredSelection) getViewer().getSelection()).getFirstElement();
 				if (sel != null) {
 					Command c = DeleteCommand.create(editingDomain, sel);
 					editingDomain.getCommandStack().execute(c);
 				}
 			}
 		});
 
 	}
 
 	public void createContents(Composite parent) {
 		createDataMasterDetailSection(parent);
 	}
 
 	private void createDataMasterDetailSection(Composite parent) {
 		_servicesMasterDetail = new ServicesMasterDetail(getEditor());
 		_servicesMasterDetail.createContent(this.getManagedForm());
 		// it is bad to manipulate editor here, but to manage Cut/Copy/Paste,
 		// the editor shall add a listener the viewer, and this is a way for him
 		// to know that viewer exists.
 		((DSEditor) getEditor()).addViewerToListenTo((StructuredViewer) getViewer());
 	}
 
 	public void setActive(boolean active) {
 		super.setActive(active);
 		if (active) {
 			// force the selection, to avoid a bug on the ContextMenu (on tab
 			// changed, display menu was unconsistent)
 			IStructuredSelection selection = (IStructuredSelection) getViewer().getSelection();
 			getViewer().setSelection(selection);
 			getViewer().refresh();
 		}
 	}
 
 	@Override
 	public String getId() {
 		return ID;
 	}
 
 	@Override
 	public String getPartName() {
 		return Messages.ServicesPage_Title;
 	}
 
 	@Override
 	public Viewer getViewer() {
 		return _servicesMasterDetail.getTreeViewer();
 	}
 
 }
