 /*******************************************************************************
  * Copyright (c) 2004, 2006 Sybase, Inc. and others.
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
 
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 
 import org.eclipse.emf.common.notify.Adapter;
 import org.eclipse.emf.common.notify.AdapterFactory;
 import org.eclipse.emf.common.notify.Notification;
 import org.eclipse.emf.common.notify.impl.AdapterImpl;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.edit.domain.AdapterFactoryEditingDomain;
 import org.eclipse.emf.edit.domain.EditingDomain;
 import org.eclipse.emf.edit.domain.IEditingDomainProvider;
 import org.eclipse.emf.edit.ui.provider.AdapterFactoryContentProvider;
 import org.eclipse.emf.edit.ui.provider.AdapterFactoryLabelProvider;
 import org.eclipse.emf.edit.ui.view.ExtendedPropertySheetPage;
 import org.eclipse.jface.viewers.ISelection;
 import org.eclipse.jface.viewers.ISelectionChangedListener;
 import org.eclipse.jface.viewers.ISelectionProvider;
 import org.eclipse.jface.viewers.IStructuredSelection;
 import org.eclipse.jface.viewers.SelectionChangedEvent;
 import org.eclipse.jface.viewers.StructuredSelection;
 import org.eclipse.jface.viewers.TreeViewer;
 import org.eclipse.jface.viewers.Viewer;
 import org.eclipse.jface.viewers.ViewerFilter;
 import org.eclipse.jface.viewers.ViewerSorter;
 import org.eclipse.jst.jsf.facesconfig.emf.ApplicationType;
 import org.eclipse.jst.jsf.facesconfig.emf.FacesConfigPackage;
 import org.eclipse.jst.jsf.facesconfig.emf.FacesConfigType;
 import org.eclipse.jst.jsf.facesconfig.emf.FactoryType;
 import org.eclipse.jst.jsf.facesconfig.emf.LifecycleType;
 import org.eclipse.jst.jsf.facesconfig.ui.EditorMessages;
 import org.eclipse.jst.jsf.facesconfig.ui.EditorPlugin;
 import org.eclipse.jst.jsf.facesconfig.ui.FacesConfigEditor;
 import org.eclipse.jst.jsf.facesconfig.ui.section.AbstractFacesConfigSection;
 import org.eclipse.jst.jsf.facesconfig.ui.section.ApplicationSection;
 import org.eclipse.jst.jsf.facesconfig.ui.section.FactorySection;
 import org.eclipse.jst.jsf.facesconfig.ui.section.LifecycleSection;
 import org.eclipse.jst.jsf.facesconfig.ui.section.LocaleConfigSection;
 import org.eclipse.jst.jsf.facesconfig.ui.section.OthersPageBaseSection;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.custom.SashForm;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.ui.IActionBars;
 import org.eclipse.ui.PlatformUI;
 import org.eclipse.ui.forms.IManagedForm;
 import org.eclipse.ui.forms.editor.FormPage;
 import org.eclipse.ui.forms.widgets.FormToolkit;
 import org.eclipse.ui.forms.widgets.ScrolledForm;
 import org.eclipse.ui.views.contentoutline.ContentOutlinePage;
 import org.eclipse.ui.views.contentoutline.IContentOutlinePage;
 import org.eclipse.ui.views.properties.IPropertySheetPage;
 
 /**
  * @author Zhi-peng Zhang, sfshi
  * @version
  */
 
 /**
  * This class is the "Others" page.
  */
 public class OthersPage extends FormPage implements IFacesConfigPage,
 		ISelectionProvider, ISelectionChangedListener, IEditingDomainProvider {
 
     /**
      * The id of this page in the faces-config editor.
      */
 	public static final String PAGE_ID = "org.eclipse.jst.jsf.facesconfig.ui.page.OthersPage";
 
 	private final static Image BANNER_IMAGE = EditorPlugin.getDefault()
 			.getImage("form_banner.gif");
 
 	private List leftSections;
 
 	private List rightSections;
 
 	private Object input;
 
 	private List selectionChangedListeners = new ArrayList();
 
 	private IPropertySheetPage propertySheetPage;
 
 	private IContentOutlinePage contentOutlinePage;
 
 	private ISelection currentSelection;
 
 	private FacesConfigOthersAdapter _facesConfigOthersAdapter;
 
 	/**
 	 * Constructor of OthersPage;
 	 * 
 	 * @param editor
 	 *            the facesconfig editor;
 	 */
 	public OthersPage(FacesConfigEditor editor) {
 		super(editor, "OthersPage",
 				EditorMessages.FacesConfigEditor_Others_TabName);
 	}
 
 	protected void createFormContent(IManagedForm managedForm) {
 		super.createFormContent(managedForm);
 		ScrolledForm form = managedForm.getForm();
 		form.setText(EditorMessages.OthersPage_Title);
 		form.setBackgroundImage(BANNER_IMAGE);
 		fillBody(managedForm, managedForm.getToolkit());
 		managedForm.refresh();
 	}
 
 	private void initSection(OthersPageBaseSection section, String name,
 			String description, ArrayList list) {
 		section.getSection().setText(name);
 		section.getSection().setDescription(description);
 		section.initialize();
 		section.addSelectionChangedListener(this);
 		section.getSection().setExpanded(false);
 		section.getSection().setLayoutData(
 				new GridData(GridData.FILL_HORIZONTAL));
 		list.add(section);
 	}
 
 	public void dispose() {
 		disposeFormSections(rightSections);
 		disposeFormSections(leftSections);
 		removeAdapterFromInput(getInput());
 		getSite().setSelectionProvider(null);
 		super.dispose();
 	}
 
 	private void disposeFormSections(List sections) {
 		for (int i=0; i < sections.size();i++) {
 			OthersPageBaseSection section = (OthersPageBaseSection) sections.get(i);
 			section.removeSelectionChangedListener(this);
 			section.dispose();
 		}
 	}
 	
 	private void fillBody(IManagedForm managedForm, FormToolkit toolkit) {
 		Composite body = managedForm.getForm().getBody();
 
 		body.setLayout(new GridLayout());
 
 		SashForm sashForm = new SashForm(body, SWT.NONE);
 		managedForm.getToolkit().adapt(sashForm, false, false);
 		sashForm.setMenu(body.getMenu());
 		sashForm.setLayoutData(new GridData(GridData.FILL_BOTH));
 
 		Composite leftContainer = toolkit.createComposite(sashForm);
 		leftContainer.setLayout(new GridLayout());
 
 		ArrayList sectionList = new ArrayList();
 
 		OthersPageBaseSection section = new ApplicationSection(
 				FacesConfigPackage.eINSTANCE.getActionListenerType(),
 				leftContainer, managedForm, this, toolkit);
 		initSection(section, EditorMessages.ActionListenerSection_Name,
 				EditorMessages.ActionListenerSection_Description,
 				sectionList);
 		section.getSection().setLayoutData(new GridData(GridData.FILL_BOTH));
 		section.getSection().setExpanded(true);
 
 		section = new ApplicationSection(FacesConfigPackage.eINSTANCE
 				.getDefaultRenderKitIdType(), leftContainer, managedForm, this,
 				toolkit);
 		initSection(section,
 				EditorMessages.DefaultRenderKitIDSection_Name,
 				EditorMessages.DefaultRenderKitIDSection_Description,
 				sectionList);
 
 		section = new LocaleConfigSection(FacesConfigPackage.eINSTANCE
 				.getSupportedLocaleType(), leftContainer, managedForm, this,
 				toolkit);
 		initSection(section, EditorMessages.LocaleConfigSection_Name,
 				EditorMessages.LocaleConfigSection_Description,
 				sectionList);
 
 		section = new ApplicationSection(FacesConfigPackage.eINSTANCE
 				.getMessageBundleType(), leftContainer, managedForm, this,
 				toolkit);
 		initSection(section, EditorMessages.MessageBundleSection_Name,
 				EditorMessages.MessageBundleSection_Description,
 				sectionList);
 
 		section = new ApplicationSection(FacesConfigPackage.eINSTANCE
 				.getNavigationHandlerType(), leftContainer, managedForm, this,
 				toolkit);
 		initSection(section,
 				EditorMessages.NavigationHandlerSection_Name,
 				EditorMessages.NavigationHandlerSection_Description,
 				sectionList);
 
 		section = new ApplicationSection(FacesConfigPackage.eINSTANCE
 				.getPropertyResolverType(), leftContainer, managedForm, this,
 				toolkit);
 		initSection(section,
 				EditorMessages.PropertyResolverSection_Name,
 				EditorMessages.PropertyResolverSection_Description,
 				sectionList);
 
 		section = new ApplicationSection(FacesConfigPackage.eINSTANCE
 				.getStateManagerType(), leftContainer, managedForm, this,
 				toolkit);
 		initSection(section, EditorMessages.StateManagerSection_Name,
 				EditorMessages.StateManagerSection_Description,
 				sectionList);
 
 		section = new ApplicationSection(FacesConfigPackage.eINSTANCE
 				.getVariableResolverType(), leftContainer, managedForm, this,
 				toolkit);
 		initSection(section,
 				EditorMessages.VariableResolverSection_Name,
 				EditorMessages.VariableResolverSection_Description,
 				sectionList);
 
 		section = new ApplicationSection(FacesConfigPackage.eINSTANCE
 				.getViewHandlerType(), leftContainer, managedForm, this,
 				toolkit);
 		initSection(section, EditorMessages.ViewHandlerSection_Name,
 				EditorMessages.ViewHandlerSection_Description,
 				sectionList);
 
 		leftSections = sectionList;
 
 		sectionList = new ArrayList();
 
 		Composite rightContainer = toolkit.createComposite(sashForm);
 		rightContainer.setLayout(new GridLayout());
 		rightContainer.setLayoutData(new GridData(GridData.FILL_BOTH));
 
 		section = new FactorySection(FacesConfigPackage.eINSTANCE
 				.getApplicationFactoryType(), rightContainer, managedForm,
 				this, toolkit);
 		initSection(section,
 				EditorMessages.ApplicationFactorySection_Name,
 				EditorMessages.ApplicationFactorySection_Description,
 				sectionList);
 		section.getSection().setLayoutData(new GridData(GridData.FILL_BOTH));
 		section.getSection().setExpanded(true);
 
 		section = new FactorySection(FacesConfigPackage.eINSTANCE
 				.getFacesContextFactoryType(), rightContainer, managedForm,
 				this, toolkit);
 		initSection(section,
 				EditorMessages.FacesContextFactorySection_Name,
 				EditorMessages.FacesContextFactorySection_Description,
 				sectionList);
 
 		section = new FactorySection(FacesConfigPackage.eINSTANCE
 				.getLifecycleFactoryType(), rightContainer, managedForm, this,
 				toolkit);
 		initSection(section,
 				EditorMessages.LifecycleFactorySection_Name,
 				EditorMessages.LifecycleFactorySection_Description,
 				sectionList);
 
 		section = new FactorySection(FacesConfigPackage.eINSTANCE
 				.getRenderKitFactoryType(), rightContainer, managedForm, this,
 				toolkit);
 		initSection(section,
 				EditorMessages.RenderKitFactorySection_Name,
 				EditorMessages.RenderKitFactorySection_Description,
 				sectionList);
 
 		section = new LifecycleSection(FacesConfigPackage.eINSTANCE
 				.getPhaseListenerType(), rightContainer, managedForm, this,
 				toolkit);
 		initSection(section, EditorMessages.PhaseListenerSection_Name,
 				EditorMessages.PhaseListenerSection_Description,
 				sectionList);
 
 		rightSections = sectionList;
 
 		getSite().setSelectionProvider(this);
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
 			FacesConfigType facesConfig = (FacesConfigType) input;
 			facesConfig.eAdapters().add(getFacesConfigAdapter());
 			setInputForApplicationSections(facesConfig);
 			setInputForFactorySections(facesConfig);
 			setInputForLifecycleSections(facesConfig);
 		}
 	}
 
	private void removeAdapterFromInput(Object input) {
		if (_facesConfigOthersAdapter != null && input != null && input instanceof FacesConfigType) {
			((FacesConfigType)input).eAdapters().remove(_facesConfigOthersAdapter);
 		}
 	}
 	
 	private Adapter getFacesConfigAdapter() {
 		if (_facesConfigOthersAdapter == null) {
 			_facesConfigOthersAdapter = new FacesConfigOthersAdapter();
 		}
 		return _facesConfigOthersAdapter;
 	}
 
 	public boolean isEditor() {
 		return true;
 	}
 
 	/**
 	 * Reset the editor input for the application section
 	 */
 	public void resetApplicationInput() {
 		if (getInput() instanceof FacesConfigType) {
 			setInputForApplicationSections((FacesConfigType) getInput());
 		}
 	}
 
 	/**
 	 * Reset the editor input for the factory sections
 	 */
 	public void resetFactoryInput() {
 		if (getInput() instanceof FacesConfigType) {
 			setInputForFactorySections((FacesConfigType) getInput());
 		}
 	}
 
 	/**
 	 * Reset the editor input for the lifecycle section
 	 */
 	public void resetLifecycleInput() {
 		if (getInput() instanceof FacesConfigType) {
 			setInputForLifecycleSections((FacesConfigType) getInput());
 		}
 	}
 
 	/**
 	 * Sets the model for all Application sub-model elements to facesConfig
 	 * @param facesConfig
 	 */
 	protected void setInputForApplicationSections(FacesConfigType facesConfig) {
 		ApplicationType application;
 		if (facesConfig.getApplication().size() > 0) {
 			application = (ApplicationType) facesConfig.getApplication().get(0);
 		} else {
 			application = null;
 		}
 		for (Iterator it = leftSections.iterator(); it.hasNext();) {
 			ApplicationSection section = (ApplicationSection) it.next();
 			section.setInput(application);
 		}
 	}
 
 	/**
 	 * Sets the model for all Factory  sub-model elements to facesConfig
 	 * @param facesConfig
 	 */
 	protected void setInputForFactorySections(FacesConfigType facesConfig) {
 		FactoryType factory;
 		if (facesConfig.getFactory().size() > 0) {
 			factory = (FactoryType) facesConfig.getFactory().get(0);
 		} else {
 			factory = null;
 		}
 
 		for (Iterator it = rightSections.iterator(); it.hasNext();) {
 			Object section = it.next();
 			if (section instanceof FactorySection)
 				((FactorySection) section).setInput(factory);
 		}
 	}
 
 	/**
 	 * Sets the model for all Lifecycle sub-model elements to facesConfig
 	 * @param facesConfig
 	 */
 	protected void setInputForLifecycleSections(FacesConfigType facesConfig) {
 		LifecycleType lifecycle;
 		if (facesConfig.getLifecycle().size() > 0) {
 			lifecycle = (LifecycleType) facesConfig.getLifecycle().get(0);
 
 		} else {
 			lifecycle = null;
 		}
 		for (Iterator it = rightSections.iterator(); it.hasNext();) {
 			Object section = it.next();
 			if (section instanceof LifecycleSection)
 				((LifecycleSection) section).setInput(lifecycle);
 		}
 	}
 
 	public Object getInput() {
 		return input;
 	}
 
 	public void refresh() {
 
 		// only refresh the expanded section;
 		for (int i = 0, n = leftSections.size(); i < n; i++) {
 			OthersPageBaseSection section = (OthersPageBaseSection) leftSections
 					.get(i);
 			if (section.getSection().isExpanded()) {
 				section.refresh();
 			}
 		}
 
 		for (int i = 0, n = rightSections.size(); i < n; i++) {
 			OthersPageBaseSection section = (OthersPageBaseSection) leftSections
 					.get(i);
 			if (section.getSection().isExpanded()) {
 				section.refresh();
 			}
 		}
 
 	}
 
 	/**
 	 * set other sections' expand state to false, except this section;
 	 * 
 	 * @param section
 	 *            the section that be expanded.
 	 */
 	public void closeOtherSections(AbstractFacesConfigSection section) {
 		OthersPageBaseSection[] sections;
 		if (leftSections.contains(section)) {
 			sections = (OthersPageBaseSection[]) leftSections
 					.toArray(new OthersPageBaseSection[leftSections.size()]);
 		} else {
 			sections = (OthersPageBaseSection[]) rightSections
 					.toArray(new OthersPageBaseSection[rightSections.size()]);
 		}
 		for (int i = 0, n = sections.length; i < n; i++) {
 			AbstractFacesConfigSection aSection = sections[i];
 			if (aSection != section && aSection.getSection().isExpanded()) {
 				aSection.getSection().setExpanded(false);
 				GridData gd = new GridData(GridData.FILL_HORIZONTAL);
 				aSection.getSection().setLayoutData(gd);
 			}
 		}
 	}
 
 	/**
 	 * An adapter on faces config.
 	 * 
 	 * @author sfshi
 	 * 
 	 */
 	class FacesConfigOthersAdapter extends AdapterImpl {
 		public void notifyChanged(Notification msg) {
 			if (msg.getEventType() == Notification.REMOVE
 					|| msg.getEventType() == Notification.ADD) {
 				if (msg.getFeature() == FacesConfigPackage.eINSTANCE
 						.getFacesConfigType_Application()) {
 					if (Thread.currentThread() == PlatformUI.getWorkbench().getDisplay().getThread()) {
 						resetApplicationInput();
 					} else {
 						PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
 							public void run() {
 								resetApplicationInput();
 							}
 						});
 					}
 				} else if (msg.getFeature() == FacesConfigPackage.eINSTANCE
 						.getFacesConfigType_Factory()) {
 					if (Thread.currentThread() == PlatformUI.getWorkbench().getDisplay().getThread()) {
 						resetFactoryInput();
 					} else {
 						PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
 							public void run() {
 								resetFactoryInput();
 							}
 						});	
 					}
 				} else if (msg.getFeature() == FacesConfigPackage.eINSTANCE
 						.getFacesConfigType_Lifecycle()) {
 					if (Thread.currentThread() == PlatformUI.getWorkbench().getDisplay().getThread()) {
 						resetLifecycleInput();
 					} else {
 						PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
 							public void run() {
 								resetLifecycleInput();
 							}
 						});	
 					}
 				}
 			}
 		}
 	}
 
 	public void addSelectionChangedListener(ISelectionChangedListener listener) {
 		// TODO Auto-generated method stub
 
 		selectionChangedListeners.add(listener);
 	}
 
 	public ISelection getSelection() {
 		if (currentSelection != null)
 			return currentSelection;
 
 		return StructuredSelection.EMPTY;
 	}
 
 	public void removeSelectionChangedListener(
 			ISelectionChangedListener listener) {
 		// TODO Auto-generated method stub
 
 		selectionChangedListeners.remove(listener);
 	}
 
 	public void selectionChanged(SelectionChangedEvent event) {
 		currentSelection = event.getSelection();
 		if (event.getSource() instanceof OthersPageBaseSection) {
 			OthersPageBaseSection source = (OthersPageBaseSection) event
 					.getSource();
 
 			if (source.getSection().isExpanded()) {
 				for (int i = 0, n = selectionChangedListeners.size(); i < n; i++) {
 					((ISelectionChangedListener) selectionChangedListeners
 							.get(i)).selectionChanged(event);
 				}
 			}
 		}
 	}
 
 	/**
 	 * get the section that is expanded.
 	 * 
 	 * @return the page section
 	 */
 	protected OthersPageBaseSection getActiveSection() {
 		for (int i = 0, n = leftSections.size(); i < n; i++) {
 
 			OthersPageBaseSection section = (OthersPageBaseSection) leftSections
 					.get(i);
 			if (section.getSection().isExpanded()) {
 				return section;
 			}
 		}
 
 		for (int i = 0, n = rightSections.size(); i < n; i++) {
 
 			OthersPageBaseSection section = (OthersPageBaseSection) leftSections
 					.get(i);
 			if (section.getSection().isExpanded()) {
 				return section;
 			}
 		}
 		return null;
 
 	}
 
 	public Object getAdapter(Class adapter) {
 
 		if (adapter == IPropertySheetPage.class) {
 			return getPropertySheetPage();
 		}
 
 		if (adapter == IContentOutlinePage.class) {
 			return getOutlinePage();
 		}
 
 		return super.getAdapter(adapter);
 	}
 
 	private IPropertySheetPage getPropertySheetPage() {
 
 		AdapterFactoryEditingDomain editingDomain = (AdapterFactoryEditingDomain) getEditor()
 				.getAdapter(EditingDomain.class);
 		AdapterFactory adapterFactory = (AdapterFactory) getEditor()
 				.getAdapter(AdapterFactory.class);
 		if (propertySheetPage == null) {
 			propertySheetPage = new ExtendedPropertySheetPage(editingDomain) {
 				public void setSelectionToViewer(List selection) {
 					// FacesConfigEditor.this.setSelectionToViewer(selection);
 					// FacesConfigEditor.this.setFocus();
 				}
 
 				public void setActionBars(IActionBars actionBars) {
 					super.setActionBars(actionBars);
 					// getActionBarContributor().shareGlobalActions(this,
 					// actionBars);
 				}
 			};
 			((ExtendedPropertySheetPage) propertySheetPage)
 					.setPropertySourceProvider(new AdapterFactoryContentProvider(
 							adapterFactory));
 			// this.addSelectionChangedListener((ExtendedPropertySheetPage)
 			// propertySheetPage);
 		}
 
 		return propertySheetPage;
 
 	}
 
 	private IContentOutlinePage getOutlinePage() {
 
 		if (contentOutlinePage == null) {
 			// The content outline is just a tree.
 			//
 			class OthersContentOutlinePage extends ContentOutlinePage {
 				public void createControl(Composite parent) {
 					super.createControl(parent);
 					TreeViewer contentOutlineViewer = getTreeViewer();
 					contentOutlineViewer.addSelectionChangedListener(this);
 
 					AdapterFactory adapterFactory = (AdapterFactory) getEditor()
 							.getAdapter(AdapterFactory.class);
 					// Set up the tree viewer.
 					contentOutlineViewer
 							.setContentProvider(new AdapterFactoryContentProvider(
 									adapterFactory));
 					contentOutlineViewer
 							.setLabelProvider(new AdapterFactoryLabelProvider(
 									adapterFactory));
 					contentOutlineViewer.addFilter(new ViewerFilter() {
 
 						public boolean select(Viewer viewer,
 								Object parentElement, Object element) {
 							return FacesConfigPackage.eINSTANCE
 									.getApplicationType().isInstance(element)
 									|| FacesConfigPackage.eINSTANCE
 											.getFactoryType().isInstance(
 													element)
 									|| FacesConfigPackage.eINSTANCE
 											.getLifecycleType().isInstance(
 													element)
 									|| FacesConfigPackage.eINSTANCE
 											.getActionListenerType()
 											.isInstance(element)
 									|| FacesConfigPackage.eINSTANCE
 											.getDefaultRenderKitIdType()
 											.isInstance(element)
 									|| FacesConfigPackage.eINSTANCE
 											.getLocaleConfigType().isInstance(
 													element)
 									|| FacesConfigPackage.eINSTANCE
 											.getDefaultLocaleType().isInstance(
 													element)
 									|| FacesConfigPackage.eINSTANCE
 											.getSupportedLocaleType()
 											.isInstance(element)
 									|| FacesConfigPackage.eINSTANCE
 											.getMessageBundleType().isInstance(
 													element)
 									|| FacesConfigPackage.eINSTANCE
 											.getNavigationHandlerType()
 											.isInstance(element)
 									|| FacesConfigPackage.eINSTANCE
 											.getPropertyResolverType()
 											.isInstance(element)
 									|| FacesConfigPackage.eINSTANCE
 											.getStateManagerType().isInstance(
 													element)
 									|| FacesConfigPackage.eINSTANCE
 											.getVariableResolverType()
 											.isInstance(element)
 									|| FacesConfigPackage.eINSTANCE
 											.getViewHandlerType().isInstance(
 													element)
 									|| FacesConfigPackage.eINSTANCE
 											.getApplicationFactoryType()
 											.isInstance(element)
 									|| FacesConfigPackage.eINSTANCE
 											.getFacesContextFactoryType()
 											.isInstance(element)
 									|| FacesConfigPackage.eINSTANCE
 											.getLifecycleFactoryType()
 											.isInstance(element)
 									|| FacesConfigPackage.eINSTANCE
 											.getRenderKitFactoryType()
 											.isInstance(element)
 									|| FacesConfigPackage.eINSTANCE
 											.getPhaseListenerType().isInstance(
 													element)
 
 							;
 						}
 					});
 
 					contentOutlineViewer.setSorter(new ViewerSorter());
 					contentOutlineViewer.setInput(getInput());
 				}
 
 			}
 
 			contentOutlinePage = new OthersContentOutlinePage();
 
 			// Listen to selection so that we can handle it is a special
 			// way.
 			//
 			contentOutlinePage
 					.addSelectionChangedListener(new ISelectionChangedListener() {
 						// This ensures that we handle selections correctly.
 						public void selectionChanged(SelectionChangedEvent event) {
 							setSelection(event.getSelection());
 						}
 					});
 
 		}
 
 		return contentOutlinePage;
 
 	}
 
 	/**
 	 * handle the selection changed event from outline page.
 	 * 
 	 * @param selection
 	 */
 	public void setSelection(ISelection selection) {
 
 		if (selection instanceof IStructuredSelection) {
 			IStructuredSelection ss = (IStructuredSelection) selection;
 			if (ss.isEmpty())
 				return;
 
 			final EObject object = (EObject) ss.getFirstElement();
 			OthersPageBaseSection section = null;
 
 			if (FacesConfigPackage.eINSTANCE.getActionListenerType()
 					.isInstance(object)) {
 				section = (OthersPageBaseSection) leftSections.get(0);
 			} else if (FacesConfigPackage.eINSTANCE.getDefaultRenderKitIdType()
 					.isInstance(object)) {
 				section = (OthersPageBaseSection) leftSections.get(1);
 			} else if (FacesConfigPackage.eINSTANCE.getLocaleConfigType()
 					.isInstance(object)) {
 				section = (OthersPageBaseSection) leftSections.get(2);
 			} else if (FacesConfigPackage.eINSTANCE.getDefaultLocaleType()
 					.isInstance(object)) {
 				section = (OthersPageBaseSection) leftSections.get(2);
 			} else if (FacesConfigPackage.eINSTANCE.getSupportedLocaleType()
 					.isInstance(object)) {
 				section = (OthersPageBaseSection) leftSections.get(2);
 			} else if (FacesConfigPackage.eINSTANCE.getMessageBundleType()
 					.isInstance(object)) {
 				section = (OthersPageBaseSection) leftSections.get(3);
 			} else if (FacesConfigPackage.eINSTANCE.getNavigationHandlerType()
 					.isInstance(object)) {
 				section = (OthersPageBaseSection) leftSections.get(4);
 			} else if (FacesConfigPackage.eINSTANCE.getPropertyResolverType()
 					.isInstance(object)) {
 				section = (OthersPageBaseSection) leftSections.get(5);
 			} else if (FacesConfigPackage.eINSTANCE.getStateManagerType()
 					.isInstance(object)) {
 				section = (OthersPageBaseSection) leftSections.get(6);
 			} else if (FacesConfigPackage.eINSTANCE.getVariableResolverType()
 					.isInstance(object)) {
 				section = (OthersPageBaseSection) leftSections.get(7);
 			} else if (FacesConfigPackage.eINSTANCE.getViewHandlerType()
 					.isInstance(object)) {
 				section = (OthersPageBaseSection) leftSections.get(8);
 			} else if (FacesConfigPackage.eINSTANCE.getApplicationFactoryType()
 					.isInstance(object)) {
 				section = (OthersPageBaseSection) rightSections.get(0);
 			} else if (FacesConfigPackage.eINSTANCE
 					.getFacesContextFactoryType().isInstance(object)) {
 				section = (OthersPageBaseSection) rightSections.get(1);
 			} else if (FacesConfigPackage.eINSTANCE.getLifecycleFactoryType()
 					.isInstance(object)) {
 				section = (OthersPageBaseSection) rightSections.get(2);
 			} else if (FacesConfigPackage.eINSTANCE.getRenderKitFactoryType()
 					.isInstance(object)) {
 				section = (OthersPageBaseSection) rightSections.get(3);
 			} else if (FacesConfigPackage.eINSTANCE.getPhaseListenerType()
 					.isInstance(object)) {
 				section = (OthersPageBaseSection) rightSections.get(4);
 			}
 			if (section != null) {
 				if ((Thread.currentThread() == PlatformUI.getWorkbench().getDisplay().getThread())) {
 					 setSelectionInSection(section, object);
 				} 
 				else {
 					final OthersPageBaseSection baseSection = section;
 					PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable() {
 						public void run() {
 							setSelectionInSection(baseSection, object);							
 						}
 					});
 				 }
 			}
 		}
 	}
 
 	private void setSelectionInSection(final OthersPageBaseSection section, final Object object) {
 		if (!section.getSection().isExpanded()) {
 			this.closeOtherSections(section);
 			GridData gd = new GridData(GridData.FILL_BOTH);
 			section.getSection().setLayoutData(gd);
 			section.getSection().setExpanded(true);
 		}
 		IStructuredSelection newselection = new StructuredSelection(
 				object);
 		section.getTableViewer().setSelection(newselection);
 	}
 	
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.emf.edit.domain.IEditingDomainProvider#getEditingDomain()
 	 */
 	public EditingDomain getEditingDomain() {
 		return ((FacesConfigEditor) getEditor()).getEditingDomain();
 	}
 }
