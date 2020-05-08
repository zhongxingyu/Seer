 /*******************************************************************************
 * Copyright (c) 2008-2012 EclipseSource Muenchen GmbH.
  * 
  * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  * Maximilian Koegel
  * Edgar Mueller
  ******************************************************************************/
 package org.eclipse.emf.emfstore.client.model.impl;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.LinkedHashSet;
 import java.util.List;
 import java.util.Set;
 
 import org.eclipse.emf.common.command.Command;
 import org.eclipse.emf.common.notify.Notification;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.resource.Resource;
 import org.eclipse.emf.ecore.xmi.XMIResource;
 import org.eclipse.emf.emfstore.client.model.Configuration;
 import org.eclipse.emf.emfstore.client.model.changeTracking.commands.CommandObserver;
 import org.eclipse.emf.emfstore.client.model.changeTracking.notification.NotificationInfo;
 import org.eclipse.emf.emfstore.client.model.changeTracking.notification.filter.EmptyRemovalsFilter;
 import org.eclipse.emf.emfstore.client.model.changeTracking.notification.filter.FilterStack;
 import org.eclipse.emf.emfstore.client.model.changeTracking.notification.filter.NotificationFilter;
 import org.eclipse.emf.emfstore.client.model.changeTracking.notification.filter.TouchFilter;
 import org.eclipse.emf.emfstore.client.model.changeTracking.notification.filter.TransientFilter;
 import org.eclipse.emf.emfstore.client.model.util.WorkspaceUtil;
 import org.eclipse.emf.emfstore.common.EMFStoreResource;
 import org.eclipse.emf.emfstore.common.model.IModelElementIdToEObjectMapping;
 import org.eclipse.emf.emfstore.common.model.IdEObjectCollection;
 import org.eclipse.emf.emfstore.common.model.util.IdEObjectCollectionChangeObserver;
 import org.eclipse.emf.emfstore.common.model.util.ModelUtil;
 
 /**
  * 
  * 
  * @author koegel
  * @author emueller
  */
 public class ResourcePersister implements CommandObserver, IdEObjectCollectionChangeObserver {
 
 	private FilterStack filterStack;
 
 	private boolean isDirty;
 
 	/**
 	 * Indicates whether a command is running.
 	 */
 	private boolean commandIsRunning;
 
 	private final Set<Resource> resources;
 
 	private final IModelElementIdToEObjectMapping mapping;
 
 	private List<IDEObjectCollectionDirtyStateListener> listeners;
 
 	/**
 	 * Constructor.
 	 * 
 	 * @param mapping
 	 *            a mapping from model element to their respective IDs
 	 */
 	public ResourcePersister(IModelElementIdToEObjectMapping mapping) {
 		this.mapping = mapping;
 		this.resources = new LinkedHashSet<Resource>();
 		this.listeners = new ArrayList<IDEObjectCollectionDirtyStateListener>();
 		this.filterStack = new FilterStack(new NotificationFilter[] { new TouchFilter(), new TransientFilter(),
 			new EmptyRemovalsFilter() });
 	}
 
 	/**
 	 * 
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.emfstore.client.model.changeTracking.commands.CommandObserver#commandStarted(org.eclipse.emf.common.command.Command)
 	 */
 	public void commandStarted(Command command) {
 		commandIsRunning = true;
 	}
 
 	/**
 	 * 
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.emfstore.client.model.changeTracking.commands.CommandObserver#commandCompleted(org.eclipse.emf.common.command.Command)
 	 */
 	public void commandCompleted(Command command) {
 		commandIsRunning = false;
 		saveDirtyResources(false);
 	}
 
 	/**
 	 * 
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.emfstore.client.model.changeTracking.commands.CommandObserver#commandFailed(org.eclipse.emf.common.command.Command,
 	 *      java.lang.Exception)
 	 */
 	public void commandFailed(Command command, Exception exception) {
 		commandIsRunning = false;
 	}
 
 	/**
 	 * Adds the given resource to the set of resources which should be saved by the persister.
 	 * 
 	 * @param resource
 	 *            the resource to be saved
 	 */
 	protected void addResource(Resource resource) {
 		if (resource != null) {
 			resources.add(resource);
 		}
 	}
 
 	/**
 	 * Save all dirty resources to disk now if auto-save is active.
 	 * If auto-save is disabled, clients have to programatically
 	 * save the dirty resource set by setting the <code>force</code> parameter
 	 * to true.
 	 * 
 	 * @param force
 	 *            whether to force the saving of resources
 	 */
 	public void saveDirtyResources(boolean force) {
 		if (!force && !Configuration.isAutoSaveEnabled()) {
 			return;
 		}
 
 		if (!isDirty) {
 			return;
 		}
 
 		for (Resource resource : resources) {
 
 			if (resource.getURI() == null || resource.getURI().toString().equals("")) {
 				continue;
 			}
 
 			if (resource instanceof EMFStoreResource) {
 				((EMFStoreResource) resource).setIdToEObjectMap(mapping.getIdToEObjectMapping(),
 					mapping.getEObjectToIdMapping());
 			} else {
 				Set<EObject> modelElements = ModelUtil.getAllContainedModelElements(resource, false, false);
 
 				for (EObject modelElement : modelElements) {
 					setModelElementIdOnResource((XMIResource) resource, modelElement);
 				}
 			}
 
 			try {
 				ModelUtil.saveResource(resource, WorkspaceUtil.getResourceLogger());
 			} catch (IOException e) {
 				// ignore exception
 			}
 		}
 
 		isDirty = false;
 		fireDirtyStateChangedNotification();
 	}
 
 	/**
 	 * Determine if there is resources that still need to be saved.
 	 * 
 	 * @return true if there is resource to be saved.
 	 */
 	public boolean isDirty() {
 		return isDirty;
 	}
 
 	/**
 	 * Add a dirty state change listener.
 	 * 
 	 * @param listener the listener
 	 */
 	public void addDirtyStateChangeLister(IDEObjectCollectionDirtyStateListener listener) {
 		listeners.add(listener);
 	}
 
 	/**
 	 * Remove a dirty state change listener.
 	 * 
 	 * @param listener the listener
 	 */
 	public void removeDirtyStateChangeLister(IDEObjectCollectionDirtyStateListener listener) {
 		listeners.remove(listener);
 	}
 
 	/**
 	 * 
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.emfstore.common.model.util.IdEObjectCollectionChangeObserver#notify(org.eclipse.emf.common.notify.Notification,
 	 *      org.eclipse.emf.emfstore.common.model.IdEObjectCollection, org.eclipse.emf.ecore.EObject)
 	 */
 	public void notify(Notification notification, IdEObjectCollection collection, EObject modelElement) {
 
 		// filter unwanted notifications that did not change anything in the
 		// state
 		if (filterStack.check(new NotificationInfo(notification), collection)) {
 			return;
 		}
 
 		isDirty = true;
 
 		if (!commandIsRunning) {
 			saveDirtyResources(false);
 		}
 	}
 
 	/**
 	 * 
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.emfstore.common.model.util.IdEObjectCollectionChangeObserver#modelElementAdded(org.eclipse.emf.emfstore.common.model.IdEObjectCollection,
 	 *      org.eclipse.emf.ecore.EObject)
 	 */
 	public void modelElementAdded(IdEObjectCollection collection, EObject modelElement) {
 		isDirty = true;
 	}
 
 	/**
 	 * 
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.emfstore.common.model.util.IdEObjectCollectionChangeObserver#modelElementRemoved(org.eclipse.emf.emfstore.common.model.IdEObjectCollection,
 	 *      org.eclipse.emf.ecore.EObject)
 	 */
 	public void modelElementRemoved(IdEObjectCollection collection, EObject modelElement) {
 
 		cleanResources(modelElement);
 
 		// save the collection's resource from where the element has been removed
 		isDirty = true;
 	}
 
 	/**
 	 * 
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.emfstore.common.model.util.IdEObjectCollectionChangeObserver#collectionDeleted(org.eclipse.emf.emfstore.common.model.IdEObjectCollection)
 	 */
 	public void collectionDeleted(IdEObjectCollection collection) {
 
 	}
 
 	private void cleanResources(EObject deletedElement) {
 
 		Set<EObject> allDeletedModelElements = ModelUtil.getAllContainedModelElements(deletedElement, false);
 		allDeletedModelElements.add(deletedElement);
 
 		// TODO: check whether resource is contained in resources??
 		for (EObject element : allDeletedModelElements) {
 			Resource childResource = element.eResource();
 			if (childResource != null) {
 				childResource.getContents().remove(element);
 			}
 		}
 	}
 
 	private void setModelElementIdOnResource(XMIResource resource, EObject modelElement) {
 
 		if (modelElement instanceof IdEObjectCollection) {
 			return;
 		}
 
 		String modelElementId = getIDForEObject(modelElement);
 		resource.setID(modelElement, modelElementId);
 	}
 
 	private String getIDForEObject(EObject modelElement) {
 		String modelElementId = mapping.getEObjectToIdMapping().get(modelElement);
 
 		if (modelElementId == null) {
 			WorkspaceUtil.handleException(new IllegalStateException("No ID for model element" + modelElement));
 		}
 
 		return modelElementId;
 	}
 
 	private void fireDirtyStateChangedNotification() {
 		for (IDEObjectCollectionDirtyStateListener listener : listeners) {
 			listener.notifyAboutDirtyStateChange();
 		}
 	}
 }
