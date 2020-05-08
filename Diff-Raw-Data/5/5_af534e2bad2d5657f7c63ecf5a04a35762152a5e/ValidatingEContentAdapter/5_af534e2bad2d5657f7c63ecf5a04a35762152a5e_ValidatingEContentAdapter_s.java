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
 * $Id: ValidatingEContentAdapter.java,v 1.12 2009/10/23 16:14:21 bcabe Exp $
  */
 package org.eclipse.pde.emfforms.internal.validation;
 
 import java.util.*;
 import org.eclipse.core.databinding.DataBindingContext;
 import org.eclipse.emf.common.notify.Notification;
 import org.eclipse.emf.common.util.Diagnostic;
 import org.eclipse.emf.common.util.DiagnosticChain;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.resource.Resource;
 import org.eclipse.emf.ecore.util.Diagnostician;
 import org.eclipse.emf.ecore.util.EContentAdapter;
 import org.eclipse.emf.edit.provider.IItemLabelProvider;
 import org.eclipse.emf.transaction.NotificationFilter;
 import org.eclipse.pde.emfforms.editor.EmfFormEditor;
 import org.eclipse.pde.emfforms.editor.ValidatingService;
 import org.eclipse.pde.emfforms.internal.Activator;
 import org.eclipse.ui.forms.IMessageManager;
 import org.eclipse.ui.forms.editor.IFormPage;
 import org.osgi.framework.*;
 
 public class ValidatingEContentAdapter extends EContentAdapter {
 	private EmfFormEditor<?> _formEditor;
 	private DataBindingContext _dataBindingContext;
 	private Diagnostician _diagnostician;
 
 	private ValidatingService validatingService;
 
 	public ValidatingEContentAdapter(EmfFormEditor<?> formEditor) {
 		_formEditor = formEditor;
 		_dataBindingContext = formEditor.getDataBindingContext();
 
 		// if SCR is not present, we want to register the Eclipse 3.4 validator manually ...
 		forceValidatingService34Registration();
 
 		// Subclass the default Diagnostician to customize the String
 		// representation of each validated EObject
 		_diagnostician = new Diagnostician() {
 			@Override
 			public String getObjectLabel(EObject eObject) {
 				if (!eObject.eIsProxy()) {
 					IItemLabelProvider itemLabelProvider = (IItemLabelProvider) _formEditor.getAdapterFactory().adapt(eObject, IItemLabelProvider.class);
 					if (itemLabelProvider != null) {
 						return itemLabelProvider.getText(eObject);
 					}
 				}
 
 				return super.getObjectLabel(eObject);
 			}
 
 			protected boolean doValidateContents(EObject eObject, DiagnosticChain diagnostics, Map<Object, Object> context) {
 
 				Resource eContainerResource = eObject.eResource();
 				List<EObject> eContents = eObject.eContents();
 				if (!eContents.isEmpty()) {
 					boolean result = true;
 					for (Iterator<EObject> i = eContents.iterator(); i.hasNext() && (result || diagnostics != null);) {
 						EObject child = i.next();
 						// in case of cross resource containment, 
 						// avoid to validate a child which are not in the container resource
 						Resource eChildResource = child.eResource();
 						if (eContainerResource != null && eChildResource == eContainerResource) {
 							result &= validate(child, diagnostics, context);
 						}
 					}
 					return result;
 				}
 				return true;
 			}
 		};
 	}
 
 	private void forceValidatingService34Registration() {
 		if (getValidatorService() == null) {
 			BundleContext context = Activator.getDefault().getBundle().getBundleContext();
 			Properties serviceProperties = new Properties();
 			serviceProperties.put(Constants.SERVICE_RANKING, 10);
 			context.registerService(ValidatingService.class.getName(), new ValidatingService34(), serviceProperties);
 		}
 	}
 
 	@Override
 	public void notifyChanged(Notification notification) {
 		super.notifyChanged(notification);
 		if (!NotificationFilter.READ.matches(notification)) {
 			validate();
 		}
 	}
 
 	public void validate() {
 		IFormPage activePageInstance = _formEditor.getActivePageInstance();
		if (_formEditor.getActivePageInstance() == null) {
 			return;
 		}
 
 		IMessageManager messageManager = activePageInstance.getManagedForm().getMessageManager();
 		messageManager.removeAllMessages();
 		messageManager.setAutoUpdate(false);
 
 		Diagnostic diagnostics = validate(_formEditor.getCurrentEObject());
 
 		for (Diagnostic diagnostic : diagnostics.getChildren()) {
 			getValidatorService().analyzeDiagnostic(_dataBindingContext, diagnostic, messageManager);
 		}
 
 		messageManager.update();
 	}
 
 	public Diagnostic validate(EObject obj) {
 		return _diagnostician.validate(obj);
 	}
 
 	private ValidatingService getValidatorService() {
 		BundleContext context = Activator.getDefault().getBundle().getBundleContext();
 		if (validatingService == null) {
 			ServiceReference validatingServiceRef = context.getServiceReference(ValidatingService.class.getName());
 			if (validatingServiceRef != null)
 				validatingService = (ValidatingService) context.getService(validatingServiceRef);
 		}
 		return validatingService;
 	}
 
 }
