 /*******************************************************************************
  * Copyright (c) 2008, 2011 Obeo.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     Obeo - initial API and implementation
  *******************************************************************************/
 package org.eclipse.emf.eef.runtime.ui.properties.sections;
 
 import java.lang.reflect.Field;
 import java.lang.reflect.Method;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 import org.eclipse.core.runtime.IAdaptable;
 import org.eclipse.emf.common.notify.AdapterFactory;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.edit.domain.EditingDomain;
 import org.eclipse.emf.edit.provider.ComposedAdapterFactory;
 import org.eclipse.emf.eef.runtime.EEFRuntimePlugin;
 import org.eclipse.emf.eef.runtime.api.adapters.SemanticAdapter;
 import org.eclipse.emf.eef.runtime.api.component.IPropertiesEditionComponent;
 import org.eclipse.emf.eef.runtime.api.notify.IPropertiesEditionEvent;
 import org.eclipse.emf.eef.runtime.api.notify.IPropertiesEditionListener;
 import org.eclipse.emf.eef.runtime.api.parts.IFormPropertiesEditionPart;
 import org.eclipse.emf.eef.runtime.api.parts.IPropertiesEditionPart;
 import org.eclipse.emf.eef.runtime.context.PropertiesEditingContext;
 import org.eclipse.emf.eef.runtime.context.impl.DomainPropertiesEditionContext;
 import org.eclipse.emf.eef.runtime.impl.services.PropertiesContextService;
 import org.eclipse.emf.eef.runtime.providers.PropertiesEditingProvider;
 import org.eclipse.emf.eef.runtime.ui.utils.EEFRuntimeUIMessages;
 import org.eclipse.emf.eef.runtime.ui.utils.EditingUtils;
 import org.eclipse.emf.eef.runtime.ui.viewers.PropertiesEditionContentProvider;
 import org.eclipse.emf.eef.runtime.ui.viewers.PropertiesEditionMessageManager;
 import org.eclipse.jface.dialogs.IMessageProvider;
 import org.eclipse.jface.viewers.IFilter;
 import org.eclipse.jface.viewers.ISelection;
 import org.eclipse.jface.viewers.IStructuredSelection;
 import org.eclipse.jface.viewers.ViewerFilter;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.ui.IWorkbenchPart;
 import org.eclipse.ui.forms.widgets.Form;
 import org.eclipse.ui.forms.widgets.Section;
 import org.eclipse.ui.views.properties.tabbed.AbstractPropertySection;
 import org.eclipse.ui.views.properties.tabbed.TabbedPropertySheetPage;
 
 /**
  * @author <a href="mailto:goulwen.lefur@obeo.fr">Goulwen Le Fur</a>
  * @deprecated use directly the generated forms as {@link Section}
  */
 public class PropertiesEditionSection extends AbstractPropertySection implements IFilter, IPropertiesEditionListener {
 
 	/**
 	 * the property sheet page for this section.
 	 */
 	private TabbedPropertySheetPage propertySheetPage;
 
 	/**
 	 * the section's parent
 	 */
 	protected Composite parent;
 
 	/**
 	 * The section's viewer
 	 */
 	private Composite container;
 
 	/**
 	 * The editingDomain where the viewer must perform editing commands.
 	 */
 	private EditingDomain editingDomain;
 
 	/**
 	 * The current selected object or the first object in the selection when multiple objects are selected.
 	 */
 	protected EObject eObject;
 
 	/**
 	 * The list of current selected objects.
 	 */
 	protected List eObjectList;
 
 	/**
 	 * The form containing the viewer
 	 */
 	private Form scrolledForm;
 
 	/**
 	 * Manager for error message
 	 */
 	private PropertiesEditionMessageManager messageManager;
 
 	/**
 	 * Filters list
 	 */
 	private ViewerFilter[] filters = new ViewerFilter[1];
 
 	protected IPropertiesEditionComponent propertiesEditionComponent;
 
 	private PropertiesEditionContentProvider contentProvider;
 
 	private AdapterFactory adapterFactory;
 
 	/**
 	 * @see org.eclipse.ui.views.properties.tabbed.ISection#createControls(org.eclipse.swt.widgets.Composite,
 	 *      org.eclipse.ui.views.properties.tabbed.TabbedPropertySheetPage)
 	 */
 	public void createControls(Composite parent, TabbedPropertySheetPage aTabbedPropertySheetPage) {
 		super.createControls(parent, aTabbedPropertySheetPage);
 		this.propertySheetPage = aTabbedPropertySheetPage;
 		this.parent = parent;
 		scrolledForm = getWidgetFactory().createForm(parent);
 		scrolledForm.getBody().setLayout(new GridLayout());
 		scrolledForm.getBody().setLayoutData(new GridData(GridData.FILL_BOTH));
 		container = getWidgetFactory().createComposite(scrolledForm.getBody());
 		GridLayout containerLayout = new GridLayout();
 		container.setLayout(containerLayout);
 		container.setLayoutData(new GridData(GridData.FILL_BOTH));
 		getWidgetFactory().decorateFormHeading(scrolledForm);
 		messageManager = new PropertiesEditionMessageManager() {
 
 			@Override
 			protected void updateStatus(String message) {
 				if (!scrolledForm.isDisposed()) {
 					if (message != null)
 						scrolledForm.setMessage(message, IMessageProvider.ERROR);
 					else
 						scrolledForm.setMessage(""); //$NON-NLS-1$
 				}
 			}
 		};
 	}
 
 	/**
 	 * @see org.eclipse.ui.views.properties.tabbed.ISection#setInput(org.eclipse.ui.IWorkbenchPart,
 	 *      org.eclipse.jface.viewers.ISelection)
 	 */
 	public void setInput(IWorkbenchPart part, ISelection selection) {
 		super.setInput(part, selection);
 		initializeEditingDomain(part);
 		if (!(selection instanceof IStructuredSelection)) {
 			return;
 		}
 		if (resolveSemanticObject(((IStructuredSelection)selection).getFirstElement()) != null) {
 			EObject newEObject = resolveSemanticObject(((IStructuredSelection)selection).getFirstElement());
 			if (newEObject != eObject) {
 				eObject = newEObject;
 				if (eObject != null) {
 					disposeComponent();
 					String descriptor = getDescriptor();
 					refreshComponent(descriptor);
 				}
 			}
 		}
 		eObjectList = ((IStructuredSelection)selection).toList();
 	}
 
 	private void refreshComponent(String descriptor) {
 			DomainPropertiesEditionContext propertiesEditingContext = new DomainPropertiesEditionContext(null, null, editingDomain, adapterFactory, eObject);
 			propertiesEditionComponent = propertiesEditingContext.createPropertiesEditingComponent(IPropertiesEditionComponent.LIVE_MODE);
 			if (propertiesEditionComponent != null) {
 				PropertiesContextService.getInstance().push(eObject, propertiesEditionComponent);
 				propertiesEditionComponent.setLiveEditingDomain(editingDomain);
 				propertiesEditionComponent.addListener(this);
 				IPropertiesEditionPart propertiesEditionPart = propertiesEditionComponent.getPropertiesEditionPart(1, descriptor);
 				if (propertiesEditionPart instanceof IFormPropertiesEditionPart) {
 					for (int i = 0; i < container.getChildren().length; i++) {
 						Composite child = (Composite)container.getChildren()[i];
 						child.dispose();
 					}
 					Composite editComposite = ((IFormPropertiesEditionPart)propertiesEditionPart).createFigure(container, getWidgetFactory());
 					if (editComposite != null) {
 						editComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
 						container.layout();
 						initSemanticContents(descriptor);
 					}
 				}
 			}
 	}
 
 	/**
 	 * @deprecated
 	 */
 	protected PropertiesEditingProvider getProvider(EObject eObject) {
 		if (this.adapterFactory == null) {
 			adapterFactory = new ComposedAdapterFactory(ComposedAdapterFactory.Descriptor.Registry.INSTANCE);
 		}
 		return (PropertiesEditingProvider)adapterFactory.adapt(eObject, PropertiesEditingProvider.class);
 	}
 
 	/**
 	 * @param descriptor
 	 */
 	protected void initSemanticContents(String descriptor) {
 		propertiesEditionComponent.initPart(propertiesEditionComponent.translatePart(descriptor), 1, eObject);
 	}
 
 	private void disposeComponent() {
 		if (propertiesEditionComponent != null) {
 			PropertiesEditingContext editingContext = propertiesEditionComponent.getEditingContext();
 			if (editingContext != null && editingContext.getParentContext() == null) {
 				editingContext.dispose();
 			}
 			propertiesEditionComponent.dispose();
 			PropertiesContextService.getInstance().pop();
 		}
 	}
 
 	private void initializeEditingDomain(IWorkbenchPart part) {
 		editingDomain = EditingUtils.getResourceSetFromEditor(part);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.ui.views.properties.tabbed.AbstractPropertySection#dispose()
 	 */
 	public void dispose() {
 		super.dispose();
 		disposeComponent();
 	}
 
 	/**
 	 * Magic method For eclipse 3.2 & 3.3 & 3.4 & 3.5
 	 * 
 	 * @return
 	 */
 	protected String getDescriptor() {
 		Map descriptor = getPageDescriptor(propertySheetPage);
 		for (Iterator iterator = descriptor.keySet().iterator(); iterator.hasNext();) {
 			Object key = iterator.next();
 			Object tab = descriptor.get(key);
 			Method getSectionAtIndex = getMethod(tab, "getSectionAtIndex", int.class); //$NON-NLS-1$
 			if (getSectionAtIndex != null) {
 				Object result = callMethod(tab, getSectionAtIndex, new Integer(0));
 				if (result == this) {
 					Method getId = getMethod(key, "getId"); //$NON-NLS-1$
 					if (getId != null) {
 						String id = (String)callMethod(key, getId);
 						return id;
 					}
 				}
 			}
 		}
 		return ""; //$NON-NLS-1$
 	}
 
 	private Map getPageDescriptor(TabbedPropertySheetPage propertySheetPage) {
 		Field descriptorToTabField = null;
 		boolean oldAccessible = false;
 		try {
 			Class<?> cls = propertySheetPage.getClass();
 			while (!cls.equals(TabbedPropertySheetPage.class)) {
 				cls = cls.getSuperclass();
 			}
 			descriptorToTabField = cls.getDeclaredField("descriptorToTab"); //$NON-NLS-1$
 			oldAccessible = descriptorToTabField.isAccessible();
 			descriptorToTabField.setAccessible(true);
 			return (Map)descriptorToTabField.get(propertySheetPage);
 
 		} catch (SecurityException e) {
 
 			EEFRuntimePlugin.getDefault().logError(
 					EEFRuntimeUIMessages.PropertiesEditionSection_descriptorToTab_not_found, e);
 		} catch (NoSuchFieldException e) {
 
 			EEFRuntimePlugin.getDefault().logError(
 					EEFRuntimeUIMessages.PropertiesEditionSection_descriptorToTab_not_found, e);
 		} catch (IllegalArgumentException e) {
 
 			EEFRuntimePlugin.getDefault().logError(
 					EEFRuntimeUIMessages.PropertiesEditionSection_descriptorToTab_not_found, e);
 		} catch (IllegalAccessException e) {
 
 			EEFRuntimePlugin.getDefault().logError(
 					EEFRuntimeUIMessages.PropertiesEditionSection_descriptorToTab_not_found, e);
 		} finally {
 			if (descriptorToTabField != null) {
 				descriptorToTabField.setAccessible(oldAccessible);
 			}
 		}
 		return null;
 	}
 
 	/**
 	 * @param object
 	 * @return
 	 */
 	protected EObject resolveSemanticObject(Object object) {
 		if (object instanceof EObject) {
 			return (EObject)object;
 		} else if (object instanceof IAdaptable) {
 			IAdaptable adaptable = (IAdaptable)object;
 			if (adaptable.getAdapter(SemanticAdapter.class) != null) {
 				SemanticAdapter semanticAdapter = (SemanticAdapter)adaptable
 						.getAdapter(SemanticAdapter.class);
 				return semanticAdapter.getEObject();
 			} else if (adaptable.getAdapter(EObject.class) != null) {
 				return (EObject)adaptable.getAdapter(EObject.class);
 			}
 		}
 		return null;
 	}
 
 	/**
 	 * @param source
 	 *            the source object
 	 * @param name
 	 *            the method to get
 	 * @param argsType
 	 *            the method arguments type
 	 * @return the given method
 	 */
 	private Method getMethod(Object source, String name, Class... argsType) {
 		try {
 			return source.getClass().getDeclaredMethod(name, argsType);
 		} catch (Exception e) {
 			EEFRuntimePlugin.getDefault().logError(
 					EEFRuntimeUIMessages.PropertiesEditionSection_method_not_found + name, e);
 		}
 		return null;
 	}
 
 	/**
 	 * @param source
 	 *            the source object
 	 * @param name
 	 *            the method to get
 	 * @param argsType
 	 *            the method arguments type
 	 * @return the result of the given method
 	 */
 	private Object callMethod(Object source, Method method, Object... args) {
 		try {
 			return method.invoke(source, args);
 		} catch (Exception e) {
 			EEFRuntimePlugin.getDefault().logError(
 					EEFRuntimeUIMessages.PropertiesEditionSection_error_occured_on + method.getName()
 							+ EEFRuntimeUIMessages.PropertiesEditionSection_call, e);
 		}
 		return null;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.jface.viewers.IFilter#select(java.lang.Object)
 	 */
 	public boolean select(Object toTest) {
 		EObject eObj = resolveSemanticObject(toTest);
 		if (eObj != null) {
 			return getProvider(eObj) != null;
 		}
 		return false;
 	}
 
 	public void firePropertiesChanged(IPropertiesEditionEvent event) {
 		handleChange(event);
 	}
 
 	private void handleChange(IPropertiesEditionEvent event) {
 		// do not handle changes if you are in initialization.
 		// if (viewer.isInitializing())
 		// return;
 		messageManager.processMessage(event);
 	}
 
 }
