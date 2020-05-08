 /*******************************************************************************
  * Copyright (c) 2008, 2012 Obeo.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     Obeo - initial API and implementation
  *******************************************************************************/
 package org.eclipse.emf.eef.runtime.ui.wizards;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.eclipse.emf.common.command.Command;
 import org.eclipse.emf.common.notify.AdapterFactory;
 import org.eclipse.emf.ecore.EClass;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.EReference;
 import org.eclipse.emf.ecore.resource.ResourceSet;
 import org.eclipse.emf.ecore.util.EcoreUtil;
 import org.eclipse.emf.edit.domain.EditingDomain;
 import org.eclipse.emf.eef.runtime.EEFRuntimePlugin;
 import org.eclipse.emf.eef.runtime.api.component.IPropertiesEditionComponent;
 import org.eclipse.emf.eef.runtime.api.notify.IPropertiesEditionEvent;
 import org.eclipse.emf.eef.runtime.api.notify.IPropertiesEditionListener;
 import org.eclipse.emf.eef.runtime.context.PropertiesEditingContext;
 import org.eclipse.emf.eef.runtime.context.impl.DomainPropertiesEditionContext;
 import org.eclipse.emf.eef.runtime.context.impl.EReferencePropertiesEditionContext;
 import org.eclipse.emf.eef.runtime.impl.services.PropertiesContextService;
 import org.eclipse.emf.eef.runtime.impl.utils.EEFUtils;
 import org.eclipse.emf.eef.runtime.ui.utils.EEFRuntimeUIMessages;
 import org.eclipse.emf.eef.runtime.ui.viewers.PropertiesEditionContentProvider;
 import org.eclipse.emf.eef.runtime.ui.viewers.PropertiesEditionMessageManager;
 import org.eclipse.emf.eef.runtime.ui.viewers.PropertiesEditionViewer;
 import org.eclipse.emf.eef.runtime.ui.widgets.referencestable.ReferencesTableSettings;
 import org.eclipse.emf.eef.runtime.ui.widgets.settings.EEFEditorSettings;
 import org.eclipse.jface.wizard.Wizard;
 import org.eclipse.jface.wizard.WizardPage;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.custom.ScrolledComposite;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.layout.FillLayout;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Label;
 
 /**
  * @author <a href="mailto:goulwen.lefur@obeo.fr">Goulwen Le Fur</a>
  */
 public class PropertiesEditionWizard extends Wizard {
 
 	protected EditPropertyWizardPage mainPage;
 
 	protected ElementCreationWizardPage elementCreationPage;
 
 	protected PropertiesEditingContext editingContext;
 
 	protected EObject eObject;
 
 	protected EReference eReference;
 
 	protected Command command;
 
 	// protected ResourceSet allResources;
 
 	private PropertiesEditionMessageManager messageManager;
 
 	protected AdapterFactory adapterFactory;
 
 	/**
 	 * @param editionContext
 	 *            defines the editing context.
 	 * @param adapterFactory
 	 *            the adapterFactory to use to get the editing component
 	 * @param eObject
 	 *            the eObject to edit
 	 */
 	public PropertiesEditionWizard(PropertiesEditingContext editionContext, AdapterFactory adapterFactory,
 			EObject eObject) {
 		this.editingContext = editionContext;
 		this.eObject = eObject;
 		this.adapterFactory = adapterFactory;
 		this.setWindowTitle(eObject.eClass().getName());
 		initMessageManager();
 	}
 
 	/**
 	 * @param editingContext
 	 *            defines the editing context.
 	 * @param adapterFactory
 	 *            the adapterFactory to use to get the editing component
 	 * @param eReference
 	 *            the eReference to edit
 	 */
 	public PropertiesEditionWizard(PropertiesEditingContext editingContext, AdapterFactory adapterFactory,
 			EReference eReference) {
 		this.editingContext = editingContext;
 		this.eReference = eReference;
 		this.adapterFactory = adapterFactory;
 		this.setWindowTitle(eReference.getName());
 		initMessageManager();
 	}
 
 	private void initMessageManager() {
 		messageManager = new PropertiesEditionMessageManager() {
 
 			@Override
 			protected void updateStatus(String message) {
 				if (mainPage != null) {
 					if (message == null || "".equals(message)) {
 						mainPage.setErrorMessage(null);
 						mainPage.setPageComplete(true);
 					} else {
 						mainPage.setErrorMessage(message);
 						mainPage.setPageComplete(false);
 					}
 				}
 			}
 		};
 	}
 
 	/**
 	 * @return the editingDomain where to perform commands.
 	 */
 	public EditingDomain getEditingDomain() {
 		return editingContext instanceof DomainPropertiesEditionContext ? ((DomainPropertiesEditionContext)editingContext)
 				.getEditingDomain() : null;
 	}
 
 	/**
 	 * @return the updated EObject.
 	 */
 	public EObject getEObject() {
 		return eObject != null ? eObject : editingContext.getEObject();
 	}
 
 	/**
 	 * @return the command to update the EObject.
 	 */
 	public Command getCommand() {
 		return command;
 	}
 
 	/**
 	 * @param listener
 	 */
 	public void addListener(IPropertiesEditionListener listener) {
 		for (int i = 0; i < getPages().length; i++) {
 			if (getPages()[i] instanceof EditPropertyWizardPage) {
 				((EditPropertyWizardPage)getPages()[i]).viewer.addPropertiesListener(listener);
 			}
 		}
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.jface.wizard.Wizard#performCancel()
 	 */
 	@Override
 	public boolean performCancel() {
 		PropertiesContextService.getInstance().pop();
 		return super.performCancel();
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.jface.wizard.Wizard#performFinish()
 	 */
 	@Override
 	public boolean performFinish() {
 		return true;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.jface.wizard.Wizard#createPageControls(org.eclipse.swt.widgets.Composite)
 	 */
 	@Override
 	public void createPageControls(Composite pageContainer) {
 		super.createPageControls(pageContainer);
 		mainPage.setInput(eObject);
 	}
 
 	@Override
 	public void addPages() {
 		if (inReferenceMode()) {
 			elementCreationPage = new ElementCreationWizardPage();
 			addPage(elementCreationPage);
 		}
 		mainPage = new EditPropertyWizardPage();
 		addPage(mainPage);
 		super.addPages();
 	}
 
 	/**
 	 * @return
 	 */
 	protected boolean inReferenceMode() {
 		return eReference != null && eReference.isContainment() && eObject == null;
 	}
 
 	protected class ElementCreationWizardPage extends WizardPage {
 
 		private List<Button> buttons = new ArrayList<Button>();
 
 		protected ElementCreationWizardPage() {
 			super(EEFRuntimeUIMessages.PropertiesEditionWizard_creation_page_key);
 			this.setTitle(EEFRuntimeUIMessages.PropertiesEditionWizard_creation_page_title);
 			this.setDescription(EEFRuntimeUIMessages.PropertiesEditionWizard_creation_page_description);
 		}
 
 		public void createControl(Composite parent) {
 			Composite control = new Composite(parent, SWT.NONE);
 			GridData gd = new GridData(GridData.FILL_BOTH);
 			control.setLayoutData(gd);
 			GridLayout layout = new GridLayout();
 			control.setLayout(layout);
 			List<EClass> instanciableTypesInHierarchy;
 			if (editingContext instanceof DomainPropertiesEditionContext) {
 				instanciableTypesInHierarchy = EEFUtils.allTypeFor(eReference,
 						((DomainPropertiesEditionContext)editingContext).getEditingDomain());
 				editingContext = null;
 			} else {
 				instanciableTypesInHierarchy = EEFUtils.instanciableTypesInHierarchy(eReference.getEType(),
 						editingContext.getResourceSet());
 			}
 			for (final EClass eClass : instanciableTypesInHierarchy) {
 				Button button = new Button(control, SWT.RADIO);
 				button.setText(eClass.getName());
 				button.addSelectionListener(new SelectionAdapter() {
 
 					public void widgetSelected(SelectionEvent e) {
 						if (editingContext instanceof EReferencePropertiesEditionContext
 								&& ((EReferencePropertiesEditionContext)editingContext).getSettings() != null) {
 							EEFEditorSettings settings = ((EReferencePropertiesEditionContext)editingContext)
 									.getSettings();
 							if (settings instanceof ReferencesTableSettings) {
 								((ReferencesTableSettings)((EReferencePropertiesEditionContext)editingContext)
 										.getSettings()).removeFromReference(eObject);
 							}
 							eObject = EcoreUtil.create(eClass);
 							EEFUtils.putToReference(settings, eObject);
 						} else {
 							eObject = EcoreUtil.create(eClass);
 						}
 						mainPage.setInput(eObject);
 					}
 				});
 				buttons.add(button);
 			}
 			if (buttons.size() > 0) {
 				buttons.get(0).setSelection(true);
 				eObject = EcoreUtil.create(instanciableTypesInHierarchy.get(0));
 				if (editingContext instanceof EReferencePropertiesEditionContext
 						&& ((EReferencePropertiesEditionContext)editingContext).getSettings() != null) {
 					EEFUtils.putToReference(
 							((EReferencePropertiesEditionContext)editingContext).getSettings(), eObject);
 				}
 			} else {
 				Label errorLabel = new Label(control, SWT.NONE);
 				errorLabel.setText("Error non instanciable type found");
 			}
 			setControl(control);
 		}
 
 	}
 
 	protected class EditPropertyWizardPage extends WizardPage implements IPropertiesEditionListener {
 
 		protected PropertiesEditionViewer viewer;
 
 		protected EditPropertyWizardPage() {
 			super(EEFRuntimeUIMessages.PropertiesEditionWizard_main_page_key);
 		}
 
 		/**
 		 * {@inheritDoc}
 		 * 
 		 * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
 		 */
 		public void createControl(Composite parent) {
 			try {
 				Composite parentComposite = new Composite(parent, SWT.NONE);
 				FillLayout layout = new FillLayout();
 				layout.marginHeight = -5;
 				layout.marginWidth = -5;
 				parentComposite.setLayout(layout);
 				ScrolledComposite scrolledContainer = new ScrolledComposite(parentComposite, SWT.H_SCROLL
 						| SWT.V_SCROLL);
 				scrolledContainer.setExpandHorizontal(true);
 				scrolledContainer.setExpandVertical(true);
 				Composite container = new Composite(scrolledContainer, SWT.FLAT);
 				GridLayout containerLayout = new GridLayout();
 				container.setLayout(containerLayout);
 				ResourceSet resourceSet;
 				if (editingContext != null)
 					resourceSet = editingContext.getResourceSet();
 				else
 					resourceSet = eObject.eResource().getResourceSet();
 				viewer = new PropertiesEditionViewer(container, resourceSet, 0);
 				viewer.setDynamicTabHeader(true);
 				viewer.setContentProvider(new PropertiesEditionContentProvider(adapterFactory,
 						IPropertiesEditionComponent.BATCH_MODE));
 				scrolledContainer.setContent(container);
 				setControl(parentComposite);
 			} catch (InstantiationException e) {
 				EEFRuntimePlugin.getDefault().logError(
 						EEFRuntimeUIMessages.PropertiesEditionWizard_error_wizard_live_mode, e);
 			}
 		}
 
 		public void setInput(EObject eObject) {
 			this.setTitle(eObject.eClass().getName());
 			this.setDescription(EEFRuntimeUIMessages.PropertiesEditionWizard_main_page_description
 					+ eObject.eClass().getName());
			editingContext.seteObject(eObject);
			viewer.setInput(editingContext);
 			viewer.addPropertiesListener(this);
 		}
 
 		/**
 		 * {@inheritDoc}
 		 * 
 		 * @see org.eclipse.emf.eef.runtime.api.notify.IPropertiesEditionListener#firePropertiesChanged(org.eclipse.emf.eef.runtime.api.notify.IPropertiesEditionEvent)
 		 */
 		public void firePropertiesChanged(IPropertiesEditionEvent event) {
 			handleChange(event);
 		}
 
 		private void handleChange(IPropertiesEditionEvent event) {
 			// do not handle changes if you are in initialization.
 			if (viewer.isInitializing())
 				return;
 			messageManager.processMessage(event);
 		}
 
 		private void updateStatus(final String message) {
 			setMessage(null);
 			setErrorMessage(message);
 			setPageComplete(message == null);
 		}
 
 	}
 
 }
