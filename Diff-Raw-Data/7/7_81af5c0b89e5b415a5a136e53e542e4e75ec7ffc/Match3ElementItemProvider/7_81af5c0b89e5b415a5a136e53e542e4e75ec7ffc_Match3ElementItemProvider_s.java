 /*******************************************************************************
  * Copyright (c) 2006, 2007 Obeo.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *     Obeo - initial API and implementation
  *******************************************************************************/
 package org.eclipse.emf.compare.match.metamodel.provider;
 
 import java.util.Collection;
 import java.util.List;
 
 import org.eclipse.emf.common.notify.AdapterFactory;
 import org.eclipse.emf.common.notify.Notification;
 import org.eclipse.emf.common.util.ResourceLocator;
import org.eclipse.emf.compare.match.metamodel.Match2Elements;
 import org.eclipse.emf.compare.match.metamodel.Match3Element;
 import org.eclipse.emf.compare.match.metamodel.MatchPackage;
 import org.eclipse.emf.compare.util.ClassUtils;
 import org.eclipse.emf.edit.provider.ComposeableAdapterFactory;
 import org.eclipse.emf.edit.provider.IEditingDomainItemProvider;
 import org.eclipse.emf.edit.provider.IItemLabelProvider;
 import org.eclipse.emf.edit.provider.IItemPropertyDescriptor;
 import org.eclipse.emf.edit.provider.IItemPropertySource;
 import org.eclipse.emf.edit.provider.IStructuredItemContentProvider;
 import org.eclipse.emf.edit.provider.ITreeItemContentProvider;
 
 /**
  * This is the item provider adapter for a {@link org.eclipse.emf.compare.match.metamodel.Match3Element} object.
  * <!-- begin-user-doc --> <!-- end-user-doc -->
  * @generated
  */
 public class Match3ElementItemProvider extends Match2ElementsItemProvider implements IEditingDomainItemProvider, IStructuredItemContentProvider, ITreeItemContentProvider, IItemLabelProvider, IItemPropertySource {
 	/**
 	 * This constructs an instance from a factory and a notifier. <!-- begin-user-doc --> <!-- end-user-doc
 	 * -->
 	 * 
 	 * @generated
 	 */
 	@SuppressWarnings("hiding")
 	public Match3ElementItemProvider(AdapterFactory adapterFactory) {
 		super(adapterFactory);
 	}
 
 	/**
 	 * This returns Match3Element.gif.
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * @generated
 	 */
 	@Override
 	public Object getImage(Object object) {
 		return overlayImage(object, getResourceLocator().getImage("full/obj16/Match3Element")); //$NON-NLS-1$
 	}
 
 	/**
 	 * This returns the property descriptors for the adapted class. <!-- begin-user-doc --> <!-- end-user-doc
 	 * -->
 	 * 
 	 * @generated
 	 */
 	@Override
 	public List<IItemPropertyDescriptor> getPropertyDescriptors(Object object) {
 		if (itemPropertyDescriptors == null) {
 			super.getPropertyDescriptors(object);
 
 			addOriginElementPropertyDescriptor(object);
 		}
 		return itemPropertyDescriptors;
 	}
 
 	/**
 	 * Return the resource locator for this item provider's resources.
 	 * <!-- begin-user-doc --> <!--
 	 * end-user-doc -->
 	 * @generated
 	 */
 	@Override
 	public ResourceLocator getResourceLocator() {
 		return MatchEditPlugin.INSTANCE;
 	}
 
 	/**
 	 * This returns the label text for the adapted class.
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * @generated NOT
 	 */
 	@Override
 	public String getText(Object object) {
 		Match3Element match3Element = (Match3Element)object;
 		final String leftName = (String)ClassUtils.invokeMethod(match3Element.getLeftElement(), "getName"); //$NON-NLS-1$
 		final String rightName = (String)ClassUtils.invokeMethod(match3Element.getRightElement(), "getName"); //$NON-NLS-1$
 		final String ancestorName = (String)ClassUtils.invokeMethod(match3Element.getOriginElement(), "getName"); //$NON-NLS-1$
 		if (leftName != null || ancestorName != null || rightName != null)
 			return leftName + " <---> " + ancestorName + " <---> " + rightName; //$NON-NLS-1$ //$NON-NLS-2$
 		return getString("_UI_Match3Element_type") + " " + match3Element.getSimilarity(); //$NON-NLS-1$ //$NON-NLS-2$
 	}
 
 	/**
 	 * This handles model notifications by calling {@link #updateChildren} to update any cached
 	 * children and by creating a viewer notification, which it passes to {@link #fireNotifyChanged}.
 	 * <!-- begin-user-doc
 	 * --> <!-- end-user-doc -->
 	 * @generated
 	 */
 	@Override
 	public void notifyChanged(Notification notification) {
 		updateChildren(notification);
 		super.notifyChanged(notification);
 	}
 
 	/**
 	 * This adds a property descriptor for the Origin Element feature.
 	 * <!-- begin-user-doc --> <!--
 	 * end-user-doc -->
 	 * @generated
 	 */
 	@SuppressWarnings("unused")
 	protected void addOriginElementPropertyDescriptor(Object object) {
 		itemPropertyDescriptors
 				.add(createItemPropertyDescriptor(
 						((ComposeableAdapterFactory)adapterFactory).getRootAdapterFactory(),
 						getResourceLocator(),
 						getString("_UI_Match3Element_originElement_feature"), //$NON-NLS-1$
 						getString(
 								"_UI_PropertyDescriptor_description", "_UI_Match3Element_originElement_feature", "_UI_Match3Element_type"), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
 						MatchPackage.Literals.MATCH3_ELEMENT__ORIGIN_ELEMENT, true, false, true, null, null,
 						null));
 	}
 
 	/**
 	 * This adds {@link org.eclipse.emf.edit.command.CommandParameter}s describing the children
 	 * that can be created under this object.
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * @generated
 	 */
 	@Override
 	protected void collectNewChildDescriptors(Collection<Object> newChildDescriptors, Object object) {
 		super.collectNewChildDescriptors(newChildDescriptors, object);
 	}
 
 }
