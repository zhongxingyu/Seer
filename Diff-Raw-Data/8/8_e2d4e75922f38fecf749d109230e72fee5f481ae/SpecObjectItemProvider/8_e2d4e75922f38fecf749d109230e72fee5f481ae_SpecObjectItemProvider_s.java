 /*******************************************************************************
  * Copyright (c) 2011 Formal Mind GmbH and University of Dusseldorf.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *     Michael Jastram - initial API and implementation
  ******************************************************************************/
 
 package org.eclipse.rmf.reqif10.pror.provider;
 
 
 import java.util.Collection;
 import java.util.List;
 
 import org.eclipse.emf.common.command.Command;
 import org.eclipse.emf.common.notify.AdapterFactory;
 import org.eclipse.emf.common.notify.Notification;
 import org.eclipse.emf.common.util.TreeIterator;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.EStructuralFeature;
 import org.eclipse.emf.ecore.InternalEObject;
 import org.eclipse.emf.ecore.impl.ENotificationImpl;
 import org.eclipse.emf.ecore.util.EcoreUtil;
 import org.eclipse.emf.edit.command.DragAndDropFeedback;
 import org.eclipse.emf.edit.domain.EditingDomain;
 import org.eclipse.emf.edit.provider.ComposeableAdapterFactory;
 import org.eclipse.emf.edit.provider.IEditingDomainItemProvider;
 import org.eclipse.emf.edit.provider.IItemLabelProvider;
 import org.eclipse.emf.edit.provider.IItemPropertyDescriptor;
 import org.eclipse.emf.edit.provider.IItemPropertySource;
 import org.eclipse.emf.edit.provider.IStructuredItemContentProvider;
 import org.eclipse.emf.edit.provider.ITreeItemContentProvider;
 import org.eclipse.emf.edit.provider.ItemPropertyDescriptor;
 import org.eclipse.emf.edit.provider.ItemPropertyDescriptorDecorator;
 import org.eclipse.emf.edit.provider.ViewerNotification;
 import org.eclipse.rmf.reqif10.ReqIF;
 import org.eclipse.rmf.reqif10.ReqIF10Package;
 import org.eclipse.rmf.reqif10.ReqIFContent;
 import org.eclipse.rmf.reqif10.SpecHierarchy;
 import org.eclipse.rmf.reqif10.SpecObject;
 import org.eclipse.rmf.reqif10.common.util.ReqIF10Util;
 import org.eclipse.rmf.reqif10.pror.util.ProrUtil;
 import org.eclipse.rmf.reqif10.util.ReqIF10Switch;
 
 /**
  * This is the item provider adapter for a {@link org.eclipse.rmf.reqif10.SpecObject} object.
  * <!-- begin-user-doc -->
  * <!-- end-user-doc -->
  * @generated
  */
 public class SpecObjectItemProvider
 	extends SpecElementWithAttributesItemProvider
 	implements
 		IEditingDomainItemProvider,
 		IStructuredItemContentProvider,
 		ITreeItemContentProvider,
 		IItemLabelProvider,
 		IItemPropertySource {
 	/**
 	 * This constructs an instance from a factory and a notifier.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public SpecObjectItemProvider(AdapterFactory adapterFactory) {
 		super(adapterFactory);
 	}
 
 	/**
 	 * This returns the property descriptors for the adapted class.
 	 * <!-- begin-user-doc -->
 	 * Always call super.getPropertyDescriptors(object) to prevent caching of properties.
 	 * <!-- end-user-doc -->
 	 * @generated NOT
 	 */
 	@Override
 	public List<IItemPropertyDescriptor> getPropertyDescriptors(Object object) {
 		super.getPropertyDescriptors(object, "Spec Object");
 		addTypePropertyDescriptor(object);
 		return itemPropertyDescriptors;
 	}
 
 	/**
 	 * This adds a property descriptor for the Type feature.
 	 * <!-- begin-user-doc -->
 	 * We decorate the property to associate it with its object.
 	 * <!-- end-user-doc -->
 	 * @generated NOT
 	 */
 	protected void addTypePropertyDescriptor(Object object) {
 		ItemPropertyDescriptor descriptor = createItemPropertyDescriptor
 			(((ComposeableAdapterFactory)adapterFactory).getRootAdapterFactory(),
 			 getResourceLocator(),
 			 getString("_UI_SpecObject_type_feature"),
 			 getString("_UI_PropertyDescriptor_description", "_UI_SpecObject_type_feature", "_UI_SpecObject_type"),
 			 ReqIF10Package.Literals.SPEC_OBJECT__TYPE,
 			 true,
 			 false,
 			 true,
 			 null,
 			 getString("_UI_SpecObjectPropertyCategory"),
 			 null);
 		itemPropertyDescriptors.add
 			(new ItemPropertyDescriptorDecorator(object, descriptor));
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	@Override
 	protected boolean shouldComposeCreationImage() {
 		return true;
 	}
 
 	@Override
 	public Object getImage(Object object) {
 		return overlayImage(object, getResourceLocator().getImage("full/obj16/SpecObject.png"));
 	}
 
 	/**
 	 * This returns the label text for the adapted class.
 	 * <!-- begin-user-doc -->
 	 * Generalized label handling in {@link SpecElementWithAttributesItemProvider}
 	 * <!-- end-user-doc -->
 	 * @generated NOT
 	 */
 	@Override
 	public String getText(Object object) {
 		return super.getText(object);
 	}
 
 	/**
 	 * This handles model notifications by calling {@link #updateChildren} to update any cached
 	 * children and by creating a viewer notification, which it passes to {@link #fireNotifyChanged}.
 	 * <!-- begin-user-doc -->
 	 * React to changes in the type.  Also informs all SpecHierarchies that refer to this object.
 	 * <!-- end-user-doc -->
 	 * @generated NOT
 	 */
 	@Override
 	public void notifyChanged(Notification notification) {
 		updateChildren(notification);
 
 		switch (notification.getFeatureID(SpecObject.class)) {
 		case ReqIF10Package.SPEC_OBJECT__TYPE:
 			fireNotifyChanged(new ViewerNotification(notification,
 					notification.getNotifier(), true, true));
 			return;
 		case ReqIF10Package.SPEC_OBJECT__VALUES:
 			notifyReferencingSpecHierarchies((SpecObject) notification.getNotifier());
 		}
 		super.notifyChanged(notification);
 	}
 
 	/**
 	 * This method finds all SpecHierarchies that have a reference to the given
 	 * SpecObject. The ItemProviders of these SpecHierarchies are then notified
 	 * to update (as this may change the label).
 	 */
 	public void notifyReferencingSpecHierarchies(final SpecObject specObject) {
 		if (specObject == null)
 			return;
 
 		final ReqIF10Switch<SpecHierarchy> visitor = new ReqIF10Switch<SpecHierarchy>() {
 			@Override
 			public SpecHierarchy caseSpecHierarchy(SpecHierarchy specHierarchy) {
 				if (specObject.equals(specHierarchy.getObject())) {
 					specHierarchy.eNotify(new ENotificationImpl(
 							(InternalEObject) specHierarchy, Notification.SET,
 							ReqIF10Package.Literals.SPEC_HIERARCHY__OBJECT,
 							specObject, specObject));
 				}
 				return super.caseSpecHierarchy(specHierarchy);
 			}
 		};
 
 		ReqIF reqif = ReqIF10Util.getReqIF(specObject);
 		if (reqif != null) {
 			for (TreeIterator<Object> i = EcoreUtil.getAllContents(reqif
 					.getCoreContent().getSpecifications(), true); i.hasNext();) {
 				visitor.doSwitch((EObject) i.next());
 			}
 		}
 	}
 
 	/**
 	 * This adds {@link org.eclipse.emf.edit.command.CommandParameter}s describing the children
 	 * that can be created under this object.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	@Override
 	protected void collectNewChildDescriptors(Collection<Object> newChildDescriptors, Object object) {
 		super.collectNewChildDescriptors(newChildDescriptors, object);
 	}
 
 	@Override
 	protected EStructuralFeature getSpecTypeFeature() {
 		return ReqIF10Package.Literals.SPEC_OBJECT__TYPE;
 	}
 	
 	/**
 	 * Use the virtual intermediate provider as the parent, rather than
 	 * {@link ReqIFContentItemProvider}.
 	 */
 	@Override
 	public Object getParent(Object object) {
 		ReqIFContent content = ((ReqIFContent) super.getParent(object));
 		ReqIFContentItemProvider reqifContentProvider = (ReqIFContentItemProvider) ProrUtil
 				.getItemProvider(adapterFactory, content);
 		return reqifContentProvider != null ? reqifContentProvider
 				.getVirtualSpecObjects(content) : null;
 	}
 	
 	/**
 	 * Handles link-operations by creating SpecRelations.
 	 */
 	@Override
 	protected Command createDragAndDropCommand(EditingDomain domain,
 			Object owner, float location, int operations, int operation,
 			Collection<?> collection) {
 
 		// Create a SpecRelation on Linking
 		if (operation == DragAndDropFeedback.DROP_LINK) {
 			return ProrUtil.createCreateSpecRelationsCommand(domain, collection, owner);
 		}
 
 		// Otherwise default behavior
 		return super.createDragAndDropCommand(domain, owner, location, operations,
 				operation, collection);
 	}
 
 }
