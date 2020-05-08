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
 package org.eclipse.emf.eef.runtime.impl.components;
 
 import org.eclipse.emf.common.notify.Notification;
 import org.eclipse.emf.common.util.Diagnostic;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.eef.runtime.EEFRuntimePlugin;
 import org.eclipse.emf.eef.runtime.api.component.IPropertiesEditionComponent;
 import org.eclipse.emf.eef.runtime.api.notify.IPropertiesEditionComponentListener;
 import org.eclipse.emf.eef.runtime.api.notify.IPropertiesEditionEvent;
 import org.eclipse.emf.eef.runtime.api.notify.IPropertiesEditionListener;
 import org.eclipse.emf.eef.runtime.api.parts.IPropertiesEditionPart;
 import org.eclipse.emf.eef.runtime.api.providers.IPropertiesEditionPartProvider;
 import org.eclipse.emf.eef.runtime.context.ExtendedPropertiesEditingContext;
 import org.eclipse.emf.eef.runtime.context.PropertiesEditingContext;
 import org.eclipse.emf.eef.runtime.impl.notify.EEFLockNotification;
 import org.eclipse.emf.eef.runtime.impl.notify.PropertiesEditionEvent;
 import org.eclipse.emf.eef.runtime.impl.parts.CompositePropertiesEditionPart;
 import org.eclipse.emf.eef.runtime.impl.services.PropertiesEditionPartProviderService;
 
 /**
  * @author <a href="mailto:goulwen.lefur@obeo.fr">Goulwen Le Fur</a>
  * @author <a href="mailto:mikael.barbero@obeo.fr">Mikaël Barbero</a>
  */
 public abstract class SinglePartPropertiesEditingComponent extends StandardPropertiesEditionComponent {
 
 	/**
 	 * EObject to edit
 	 */
 	protected EObject semanticObject;
 
 	/**
 	 * Component's part
 	 */
 	protected IPropertiesEditionPart editingPart;
 
 	/**
 	 * Key to use to get the Part provider
 	 */
 	protected java.lang.Object repositoryKey;
 
 	/**
 	 * Key to use to get the Part
 	 */
 	protected java.lang.Object partKey;
 
 	/**
 	 * Default constructor
 	 */
 	public SinglePartPropertiesEditingComponent(PropertiesEditingContext editingContext,
 			EObject semanticObject, String editing_mode) {
 		this.semanticObject = semanticObject;
 		this.editingContext = editingContext;
 		if (IPropertiesEditionComponent.LIVE_MODE.equals(editing_mode)) {
 			semanticAdapter = initializeSemanticAdapter();
 		}
 		this.editing_mode = editing_mode;
 		activate();
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see IPropertiesEditionComponent#activate()
 	 */
 	public void activate() {
 		if (semanticAdapter != null) {
 			PropertiesEditingContext editingContext = getEditingContext();
 			if (editingContext instanceof ExtendedPropertiesEditingContext) {
 				((ExtendedPropertiesEditingContext)editingContext).getResourceSetAdapter().addEditingSemanticListener(semanticAdapter);
 			}
 		}
 		for (IPropertiesEditionComponentListener listener : EEFRuntimePlugin.getDefault().getPecListeners()) {
 			listener.activate(this);
 		}
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see IPropertiesEditionComponent#deactivate()
 	 */
 	public void deactivate() {
 		if (semanticAdapter != null) {
 			PropertiesEditingContext editingContext = getEditingContext();
 			if (editingContext instanceof ExtendedPropertiesEditingContext && ((ExtendedPropertiesEditingContext)editingContext).canReachResourceSetAdapter()) {
 				((ExtendedPropertiesEditingContext)editingContext).getResourceSetAdapter().removeEditingSemanticListener(semanticAdapter);
 			}
 		}
 		for (IPropertiesEditionComponentListener listener : EEFRuntimePlugin.getDefault().getPecListeners()) {
 			listener.deactivate(this);
 		}
 	}
 
 	/**
 	 * @return
 	 */
 	private String partID() {
 		return parts[0];
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.eef.runtime.impl.components.StandardPropertiesEditionComponent#translatePart(java.lang.String)
 	 */
 	public java.lang.Object translatePart(String key) {
 		if (partID().equals(key))
 			return partKey;
 		return super.translatePart(key);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.eef.runtime.impl.components.StandardPropertiesEditionComponent#
 	 *      setPropertiesEditionPart(java.lang.Object, int,
 	 *      org.eclipse.emf.eef.runtime.api.parts.IPropertiesEditionPart)
 	 */
 	public void setPropertiesEditionPart(java.lang.Object key, int kind,
 			IPropertiesEditionPart propertiesEditionPart) {
 		if (key == partKey) {
 			this.editingPart = propertiesEditionPart;
 			if (semanticAdapter != null)
 				semanticAdapter.setPart(editingPart);
 		}
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.eef.runtime.impl.components.StandardPropertiesEditionComponent#shouldProcess(org.eclipse.emf.eef.runtime.api.notify.IPropertiesEditionEvent)
 	 */
 	protected boolean shouldProcess(IPropertiesEditionEvent event) {
 		if (event instanceof PropertiesEditionEvent && associatedFeature(event.getAffectedEditor()) != null) {
 			Object currentValue = semanticObject.eGet(associatedFeature(event.getAffectedEditor()));
 			return (currentValue == null && event.getNewValue() != null)
 					|| (currentValue != null && !currentValue.equals(event.getNewValue()));
 		}
 		return super.shouldProcess(event);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.eef.runtime.api.component.IPropertiesEditionComponent#getEditingContext()
 	 */
 	public PropertiesEditingContext getEditingContext() {
 		return editingContext;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.eef.runtime.api.component.IPropertiesEditionComponent#validate()
 	 */
 	public Diagnostic validate() {
 		Diagnostic validate = Diagnostic.OK_INSTANCE;
 		validate = EEFRuntimePlugin.getEEFValidator().validate(semanticObject);
 		return validate;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.eef.runtime.api.component.IPropertiesEditionComponent#dispose()
 	 */
 	public void dispose() {
 		deactivate();
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.eef.runtime.api.component.IPropertiesEditionComponent#getPropertiesEditionPart
 	 *      (java.lang.String, java.lang.String)
 	 */
 	public IPropertiesEditionPart getPropertiesEditionPart(int kind, String key) {
 		if (semanticObject != null && partID().equals(key)) {
 			if (editingPart == null) {
 				IPropertiesEditionPartProvider provider = PropertiesEditionPartProviderService.getInstance()
 						.getProvider(repositoryKey);
 				if (provider != null) {
 					editingPart = provider.getPropertiesEditionPart(partKey, kind, this);
 					addListener((IPropertiesEditionListener)editingPart);
 					if (semanticAdapter != null)
 						semanticAdapter.setPart(editingPart);
 				}
 			}
 			return (IPropertiesEditionPart)editingPart;
 		}
 		return null;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.eef.runtime.api.component.IPropertiesEditionComponent#getTabText(java.lang.String)
 	 */
 	public String getTabText(String p_key) {
 		return editingPart.getTitle();
 	}
 
 	/**
 	 * @param key
 	 *            of the editor to ckeck
 	 * @return <code>true</code> is the editor is visible.
 	 */
 	public boolean isAccessible(Object key) {
 		if (editingPart != null && ((CompositePropertiesEditionPart)editingPart).getComposer() != null) {
 			return ((CompositePropertiesEditionPart)editingPart).getComposer().isVisible(key);
 		}
 		return false;
 	}
 
 	@Override
 	public void updatePart(Notification msg) {
 		if (msg instanceof EEFLockNotification && editingPart  instanceof CompositePropertiesEditionPart) {
 			((CompositePropertiesEditionPart) editingPart).refresh();
 		}
 	}

	/**
	 * @return IPropertiesEditionPart
	 */
	public IPropertiesEditionPart getEditingPart() {
		return editingPart;
	}
 	
 	
 }
