 /*******************************************************************************
  * Copyright (c) 2004, 2005 Sybase, Inc. and others.
  *
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     Sybase, Inc. - initial API and implementation
  *******************************************************************************/
 package org.eclipse.jst.jsf.facesconfig.ui.page;
 
 import org.eclipse.jface.viewers.ISelection;
 import org.eclipse.jface.viewers.ISelectionChangedListener;
 import org.eclipse.jface.viewers.ISelectionProvider;
 import org.eclipse.jface.viewers.StructuredSelection;
 import org.eclipse.jst.jsf.facesconfig.emf.FacesConfigType;
 import org.eclipse.jst.jsf.facesconfig.ui.EditorPlugin;
 import org.eclipse.jst.jsf.facesconfig.ui.FacesConfigEditor;
 import org.eclipse.jst.jsf.facesconfig.ui.EditorMessages;
 import org.eclipse.jst.jsf.facesconfig.ui.section.OverviewComponentsSection;
 import org.eclipse.jst.jsf.facesconfig.ui.section.OverviewGeneralSection;
 import org.eclipse.jst.jsf.facesconfig.ui.section.OverviewManagedBeanSection;
 import org.eclipse.jst.jsf.facesconfig.ui.section.OverviewNavigationSection;
 import org.eclipse.jst.jsf.facesconfig.ui.section.OverviewOthersSection;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.ui.forms.IManagedForm;
 import org.eclipse.ui.forms.editor.FormEditor;
 import org.eclipse.ui.forms.editor.FormPage;
 import org.eclipse.ui.forms.widgets.FormToolkit;
 import org.eclipse.ui.forms.widgets.ScrolledForm;
 
 /**
  * @author jchoi, Xiao-guang Zhang
  * @version
  */
 public class OverviewPage extends FormPage implements ISelectionProvider,
 		IFacesConfigPage {
 
 	private static final int LAYOUT_MARGIN_HEIGHT = 2;
 
 	private static final int LAYOUT_MARGIN_WIDTH = 2;
 
 	private Object input;
 
 	private OverviewGeneralSection generalSection;
 
 	private OverviewManagedBeanSection beanSection;
 
 	private OverviewComponentsSection componentsSection;
 
 	//
 	private OverviewNavigationSection navigationSection;
 
 	//
 	private OverviewOthersSection otherSection;
 
 	/**
 	 * 
 	 * @param editor
 	 */
 	public OverviewPage(FormEditor editor) {
		super(editor, "OverviewPage", EditorMessages.OverviewPage_page_name);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.ui.forms.editor.FormPage#createFormContent(org.eclipse.ui.forms.IManagedForm)
 	 */
 	protected void createFormContent(IManagedForm managedForm) {
 
 		ScrolledForm form = managedForm.getForm();
 		form.setText(EditorMessages.OverviewPage_PageflowOverview_summary);
 		form.setBackgroundImage(EditorPlugin.getDefault().getImage(
 				"form_banner.gif")); //$NON-NLS-1$
 		Composite body = form.getBody();
 		GridLayout gridLayout = new GridLayout();
 		gridLayout.marginWidth = LAYOUT_MARGIN_WIDTH;
 		gridLayout.marginHeight = LAYOUT_MARGIN_HEIGHT;
 		body.setLayout(gridLayout);
 		gridLayout = new GridLayout(2, true);
 		form.getBody().setLayout(gridLayout);
 		FormEditor editor = getEditor();
 		FormToolkit toolkit = editor.getToolkit();
 
 		// add overview general information
 		generalSection = new OverviewGeneralSection(managedForm.getForm()
 				.getBody(), managedForm, this, toolkit);
 		generalSection.initialize();
 		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
 		gd.horizontalSpan = 2;
 		generalSection.getSection().setLayoutData(gd);
 
 		// Navigation section
 		navigationSection = new OverviewNavigationSection(managedForm.getForm()
 				.getBody(), managedForm, this, toolkit);
 		navigationSection.initialize();
 		gd = new GridData(GridData.FILL_BOTH);
 		navigationSection.getSection().setLayoutData(gd);
 		navigationSection.getSection().setExpanded(true);
 
 		// ManagedBeans section
 		beanSection = new OverviewManagedBeanSection(managedForm.getForm()
 				.getBody(), managedForm, this, toolkit);
 		beanSection.initialize();
 		gd = new GridData(GridData.FILL_BOTH);
 		beanSection.getSection().setLayoutData(gd);
 		beanSection.getSection().setExpanded(true);
 
 		/* components section */
 		componentsSection = new OverviewComponentsSection(managedForm.getForm()
 				.getBody(), managedForm, this, toolkit);
 		componentsSection.initialize();
 		gd = new GridData(GridData.FILL_BOTH);
 		componentsSection.getSection().setLayoutData(gd);
 		componentsSection.getSection().setExpanded(true);
 
 		/* other sections */
 		otherSection = new OverviewOthersSection(managedForm.getForm()
 				.getBody(), managedForm, this, toolkit);
 		otherSection.initialize();
 		gd = new GridData(GridData.FILL_BOTH);
 		otherSection.getSection().setLayoutData(gd);
 		otherSection.getSection().setExpanded(true);
 
 		this.getSite().setSelectionProvider(this);
 
 	}
 
 	/**
 	 * set input for this page when the it got activated at the first time; if
 	 * it's not the first time, then refresh the page.
 	 * 
 	 * @see org.eclipse.ui.forms.editor.FormPage#setActive(boolean)
 	 */
 	public void setActive(boolean active) {
 		super.setActive(active);
 		if (active) {
 			if (getInput() != ((FacesConfigEditor) getEditor())
 					.getFacesConfig()) {
 				setInput(((FacesConfigEditor) getEditor()).getFacesConfig());
 			} else {
 				this.refresh();
 			}
 		}
 	}
 
 	/**
 	 * 
 	 */
 	public void setInput(Object input) {
 		if (input instanceof FacesConfigType) {
 			this.input = input;
 			navigationSection.setInput(input);
 			beanSection.setInput(input);
 			componentsSection.setInput(input);
 			otherSection.setInput(input);
 		}
 	}
 
 	/**
 	 * Call refreshAll on all sections on this page
 	 */
 	public void refreshAll() {
 		generalSection.refreshAll();
 		navigationSection.refreshAll();
 		beanSection.refreshAll();
 		componentsSection.refreshAll();
 		otherSection.refreshAll();
 	}
 
 	public Object getInput() {
 		return input;
 	}
 
 	public void refresh() {
 		generalSection.refresh();
 		navigationSection.refresh();
 		beanSection.refresh();
 		componentsSection.refresh();
 		otherSection.refresh();
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.ui.forms.editor.IFormPage#isEditor()
 	 */
 	public boolean isEditor() {
 		return true;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.jface.viewers.ISelectionProvider#addSelectionChangedListener(org.eclipse.jface.viewers.ISelectionChangedListener)
 	 */
 	public void addSelectionChangedListener(ISelectionChangedListener listener) {
         // do not support change in selection
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.jface.viewers.ISelectionProvider#getSelection()
 	 */
 	public ISelection getSelection() {
 		return StructuredSelection.EMPTY;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.jface.viewers.ISelectionProvider#removeSelectionChangedListener(org.eclipse.jface.viewers.ISelectionChangedListener)
 	 */
 	public void removeSelectionChangedListener(
 			ISelectionChangedListener listener) {
         // do not support change in selection
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.jface.viewers.ISelectionProvider#setSelection(org.eclipse.jface.viewers.ISelection)
 	 */
 	public void setSelection(ISelection selection) {
 	    // do not support change in selection
 	}
 }
