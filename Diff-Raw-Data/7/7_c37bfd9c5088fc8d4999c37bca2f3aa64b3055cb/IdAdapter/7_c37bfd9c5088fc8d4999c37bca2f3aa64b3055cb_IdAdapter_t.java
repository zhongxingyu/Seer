 /**
  * Copyright (c) 2013 itemis AG and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *     Mark Broerkens - initial API and implementation
  * 
  */
 package org.eclipse.rmf.serialization;
 
 import java.util.Collection;
import java.util.List;
 import java.util.Map;
 
 import org.eclipse.emf.common.notify.Notification;
 import org.eclipse.emf.common.util.EList;
 import org.eclipse.emf.common.util.TreeIterator;
 import org.eclipse.emf.ecore.EAttribute;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.EPackage;
 import org.eclipse.emf.ecore.EReference;
 import org.eclipse.emf.ecore.resource.Resource;
 import org.eclipse.emf.ecore.resource.ResourceSet;
 import org.eclipse.emf.ecore.util.EContentAdapter;
 import org.eclipse.emf.ecore.util.EcoreUtil;
 
 public class IdAdapter extends EContentAdapter {
 	final Map<String, EObject> idToEObjectMap;
 	final Map<EObject, String> eObjectToIDMap;
 	final Collection<EPackage> createIdForPackages;
 
 	public IdAdapter(Map<String, EObject> idToEObjectMap, Map<EObject, String> eObjectToIDMap, Collection<EPackage> createIdForPackages) {
 		super();
 		this.idToEObjectMap = idToEObjectMap;
 		this.eObjectToIDMap = eObjectToIDMap;
 		this.createIdForPackages = createIdForPackages;
 	}
 
 	@Override
 	public boolean isAdapterForType(Object type) {
 		// TODO Auto-generated method stub
 		return super.isAdapterForType(type);
 	}
 
 	@Override
 	public void notifyChanged(Notification n) {
 		assert null != n.getNotifier();
 		super.notifyChanged(n); // the superclass handles adding/removing this Adapter to new Books
 
 		Object notifier_ = n.getNotifier();
 		if (!n.isTouch()) {
 			if (notifier_ instanceof EObject) {
 				Object feature = n.getFeature();
 				if (feature instanceof EAttribute) {
 					// handle changed id
 					EAttribute attribute = (EAttribute) feature;
 					if (attribute.isID()) {
 						String newId = n.getNewStringValue();
 						String oldId = n.getOldStringValue();
 						EObject objectWithId = (EObject) n.getNotifier();
 						switch (n.getEventType()) {
 						case Notification.SET:
 							if (null == newId) {
 								eObjectToIDMap.remove(objectWithId);
 							} else {
 								eObjectToIDMap.put(objectWithId, newId);
 								idToEObjectMap.put(newId, objectWithId);
 							}
 
 							if (null != oldId) {
 								idToEObjectMap.remove(oldId);
 							}
 							break;
 						case Notification.UNSET:
 							eObjectToIDMap.remove(objectWithId);
 							idToEObjectMap.remove(oldId);
 							break;
 						}
 					}
 				} else {
 					// handle removed or added objects
 					EReference reference = (EReference) feature;
 					if (reference.isContainment()) {
 
 						switch (n.getEventType()) {
 						case Notification.SET:
 						case Notification.ADD:
 							handleNewObjectAndSubObjects((EObject) n.getNewValue());
 							break;
 						case Notification.ADD_MANY:
 							EList<EObject> newObjects = (EList<EObject>) n.getNewValue();
 							int size = newObjects.size();
 							for (int i = 0; i < size; i++) {
 								handleNewObjectAndSubObjects(newObjects.get(i));
 							}
 							break;
 						case Notification.UNSET:
 						case Notification.REMOVE:
 							handleRemoveObjectAndSubObjects((EObject) n.getOldValue());
 							break;
 						case Notification.REMOVE_MANY:
							// for empty lists getOldValue might return java.util.Collections$EmptyList instead of an
							// EList
							// therefore it is not save to cast to EList here
							List<EObject> removeObjects = (List<EObject>) n.getOldValue();
 							size = removeObjects.size();
 							for (int i = 0; i < size; i++) {
 								handleRemoveObjectAndSubObjects(removeObjects.get(i));
 							}
 							break;
 						}
 
 					}
 				}
 
 			} else if (notifier_ instanceof Resource) {
 				// feature is null
 				int featureID = n.getFeatureID(Resource.class);
 				if (Resource.RESOURCE__CONTENTS == featureID) {
 					switch (n.getEventType()) {
 					case Notification.SET:
 					case Notification.ADD:
 						handleNewObjectAndSubObjects((EObject) n.getNewValue());
 						break;
 					case Notification.ADD_MANY:
 						EList<EObject> newObjects = (EList<EObject>) n.getNewValue();
 						int size = newObjects.size();
 						for (int i = 0; i < size; i++) {
 							handleNewObjectAndSubObjects(newObjects.get(i));
 						}
 						break;
 					case Notification.UNSET:
 					case Notification.REMOVE:
 						handleRemoveObjectAndSubObjects((EObject) n.getOldValue());
 						break;
 					case Notification.REMOVE_MANY:
 						EList<EObject> removeObjects = (EList<EObject>) n.getOldValue();
 						size = removeObjects.size();
 						for (int i = 0; i < size; i++) {
 							handleRemoveObjectAndSubObjects(removeObjects.get(i));
 						}
 						break;
 					}
 				}
 			} else if (notifier_ instanceof ResourceSet) {
 				// NOP
 			} else {
 				// NOP
 			}
 
 		} // end if isTouch
 
 	}// end notifyChanged
 
 	void handleNewObjectAndSubObjects(EObject objectWithId) {
 		if (null != objectWithId) {
 			handleNewObject(objectWithId);
 		}
 		TreeIterator<EObject> iterator = objectWithId.eAllContents();
 		while (iterator.hasNext()) {
 			handleNewObject(iterator.next());
 		}
 	}
 
 	void handleNewObject(EObject objectWithId) {
 		assert null != objectWithId;
 		EAttribute idAttribute = objectWithId.eClass().getEIDAttribute();
 		if (null != idAttribute) {
 			String id = (String) objectWithId.eGet(idAttribute);
 			if ((id == null || 0 == id.length()) && createIdForPackages.contains(objectWithId.eClass().getEPackage())) {
 				id = EcoreUtil.generateUUID();
 				objectWithId.eSet(idAttribute, id);
 				// id map gets updated by notification on setId
 			} else {
 				eObjectToIDMap.put(objectWithId, id);
 				idToEObjectMap.put(id, objectWithId);
 			}
 		}
 	}
 
 	void handleRemoveObjectAndSubObjects(EObject objectWithId) {
 		if (null != objectWithId) {
 			handleRemoveObject(objectWithId);
 		}
 		TreeIterator<EObject> iterator = objectWithId.eAllContents();
 		while (iterator.hasNext()) {
 			handleRemoveObject(iterator.next());
 		}
 	}
 
 	void handleRemoveObject(EObject objectWithId) {
 		assert null != objectWithId;
 		String id = eObjectToIDMap.remove(objectWithId);
 		if (null != id) {
 			idToEObjectMap.remove(id);
 		}
 	}
 
 }
