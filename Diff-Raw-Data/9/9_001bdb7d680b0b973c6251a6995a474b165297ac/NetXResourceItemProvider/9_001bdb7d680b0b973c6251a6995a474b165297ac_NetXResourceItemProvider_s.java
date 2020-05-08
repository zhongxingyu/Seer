 /**
  * Copyright (c) ${date} NetXForge
  * 
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Lesser General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  * 
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * You should have received a copy of the GNU Lesser General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>
  * 
  * Contributors:
  * Christophe Bouhier - initial API and implementation and/or initial documentation
  */
 package com.netxforge.netxstudio.library.provider;
 
 
 import java.util.Collection;
 import java.util.List;
 
 import org.eclipse.emf.common.notify.AdapterFactory;
 import org.eclipse.emf.common.notify.Notification;
 import org.eclipse.emf.ecore.EStructuralFeature;
 import org.eclipse.emf.edit.provider.ComposeableAdapterFactory;
 import org.eclipse.emf.edit.provider.IEditingDomainItemProvider;
 import org.eclipse.emf.edit.provider.IItemLabelProvider;
 import org.eclipse.emf.edit.provider.IItemPropertyDescriptor;
 import org.eclipse.emf.edit.provider.IItemPropertySource;
 import org.eclipse.emf.edit.provider.IStructuredItemContentProvider;
 import org.eclipse.emf.edit.provider.ITreeItemContentProvider;
 import org.eclipse.emf.edit.provider.ViewerNotification;
 
 import com.netxforge.netxstudio.generics.GenericsFactory;
 import com.netxforge.netxstudio.library.LibraryPackage;
 import com.netxforge.netxstudio.library.NetXResource;
 import com.netxforge.netxstudio.metrics.MetricsFactory;
 
 /**
  * This is the item provider adapter for a {@link com.netxforge.netxstudio.library.NetXResource} object.
  * <!-- begin-user-doc -->
  * <!-- end-user-doc -->
  * @generated
  */
 public class NetXResourceItemProvider
 	extends BaseResourceItemProvider
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
 	public NetXResourceItemProvider(AdapterFactory adapterFactory) {
 		super(adapterFactory);
 	}
 
 	/**
 	 * This returns the property descriptors for the adapted class.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	@Override
 	public List<IItemPropertyDescriptor> getPropertyDescriptors(Object object) {
 		if (itemPropertyDescriptors == null) {
 			super.getPropertyDescriptors(object);
 
 			addComponentRefPropertyDescriptor(object);
 			addMetricRefPropertyDescriptor(object);
 		}
 		return itemPropertyDescriptors;
 	}
 
 	/**
 	 * This adds a property descriptor for the Component Ref feature.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	protected void addComponentRefPropertyDescriptor(Object object) {
 		itemPropertyDescriptors.add
 			(createItemPropertyDescriptor
 				(((ComposeableAdapterFactory)adapterFactory).getRootAdapterFactory(),
 				 getResourceLocator(),
 				 getString("_UI_NetXResource_componentRef_feature"),
 				 getString("_UI_PropertyDescriptor_description", "_UI_NetXResource_componentRef_feature", "_UI_NetXResource_type"),
 				 LibraryPackage.Literals.NET_XRESOURCE__COMPONENT_REF,
 				 true,
 				 false,
 				 true,
 				 null,
 				 null,
 				 null));
 	}
 
 	/**
 	 * This adds a property descriptor for the Metric Ref feature.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	protected void addMetricRefPropertyDescriptor(Object object) {
 		itemPropertyDescriptors.add
 			(createItemPropertyDescriptor
 				(((ComposeableAdapterFactory)adapterFactory).getRootAdapterFactory(),
 				 getResourceLocator(),
 				 getString("_UI_NetXResource_metricRef_feature"),
 				 getString("_UI_PropertyDescriptor_description", "_UI_NetXResource_metricRef_feature", "_UI_NetXResource_type"),
 				 LibraryPackage.Literals.NET_XRESOURCE__METRIC_REF,
 				 true,
 				 false,
 				 true,
 				 null,
 				 null,
 				 null));
 	}
 
 	/**
 	 * This specifies how to implement {@link #getChildren} and is used to deduce an appropriate feature for an
 	 * {@link org.eclipse.emf.edit.command.AddCommand}, {@link org.eclipse.emf.edit.command.RemoveCommand} or
 	 * {@link org.eclipse.emf.edit.command.MoveCommand} in {@link #createCommand}.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	@Override
 	public Collection<? extends EStructuralFeature> getChildrenFeatures(Object object) {
 		if (childrenFeatures == null) {
 			super.getChildrenFeatures(object);
 			childrenFeatures.add(LibraryPackage.Literals.NET_XRESOURCE__METRIC_VALUE_RANGES);
 			childrenFeatures.add(LibraryPackage.Literals.NET_XRESOURCE__CAPACITY_VALUES);
 			childrenFeatures.add(LibraryPackage.Literals.NET_XRESOURCE__UTILIZATION_VALUES);
 			childrenFeatures.add(LibraryPackage.Literals.NET_XRESOURCE__FORECAST_CAPACITY_VALUES);
 			childrenFeatures.add(LibraryPackage.Literals.NET_XRESOURCE__FORECAST_VALUES);
 			childrenFeatures.add(LibraryPackage.Literals.NET_XRESOURCE__TRENDED_VALUES);
 		}
 		return childrenFeatures;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	@Override
 	protected EStructuralFeature getChildFeature(Object object, Object child) {
 		// Check the type of the specified child object and return the proper feature to use for
 		// adding (see {@link AddCommand}) it as a child.
 
 		return super.getChildFeature(object, child);
 	}
 
 	/**
 	 * This returns NetXResource.gif.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated NOT
 	 */
 	@Override
 	public Object getImage(Object object) {
		return overlayImage(object, getResourceLocator().getImage("full/obj16/NetXResource_H.gif"));
 	}
 
 	/**
 	 * This returns the label text for the adapted class.
 	 * CB Adapted 17-01-2012, to show the calculated label between quotes. 
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	@Override
 	public String getText(Object object) {
 		String label = ((NetXResource)object).getExpressionName();
 		return label == null || label.length() == 0 ?
 			getString("_UI_NetXResource_type") :
 			getString("_UI_NetXResource_type") + " \"" + label + "\"";
 	}
 
 	/**
 	 * This handles model notifications by calling {@link #updateChildren} to update any cached
 	 * children and by creating a viewer notification, which it passes to {@link #fireNotifyChanged}.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	@Override
 	public void notifyChanged(Notification notification) {
 		updateChildren(notification);
 
 		switch (notification.getFeatureID(NetXResource.class)) {
 			case LibraryPackage.NET_XRESOURCE__METRIC_VALUE_RANGES:
 			case LibraryPackage.NET_XRESOURCE__CAPACITY_VALUES:
 			case LibraryPackage.NET_XRESOURCE__UTILIZATION_VALUES:
 			case LibraryPackage.NET_XRESOURCE__FORECAST_CAPACITY_VALUES:
 			case LibraryPackage.NET_XRESOURCE__FORECAST_VALUES:
 			case LibraryPackage.NET_XRESOURCE__TRENDED_VALUES:
 				fireNotifyChanged(new ViewerNotification(notification, notification.getNotifier(), true, false));
 				return;
 		}
 		super.notifyChanged(notification);
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
 
 		newChildDescriptors.add
 			(createChildParameter
 				(LibraryPackage.Literals.NET_XRESOURCE__METRIC_VALUE_RANGES,
 				 MetricsFactory.eINSTANCE.createMetricValueRange()));
 
 		newChildDescriptors.add
 			(createChildParameter
 				(LibraryPackage.Literals.NET_XRESOURCE__CAPACITY_VALUES,
 				 GenericsFactory.eINSTANCE.createValue()));
 
 		newChildDescriptors.add
 			(createChildParameter
 				(LibraryPackage.Literals.NET_XRESOURCE__UTILIZATION_VALUES,
 				 GenericsFactory.eINSTANCE.createValue()));
 
 		newChildDescriptors.add
 			(createChildParameter
 				(LibraryPackage.Literals.NET_XRESOURCE__FORECAST_CAPACITY_VALUES,
 				 GenericsFactory.eINSTANCE.createValue()));
 
 		newChildDescriptors.add
 			(createChildParameter
 				(LibraryPackage.Literals.NET_XRESOURCE__FORECAST_VALUES,
 				 GenericsFactory.eINSTANCE.createValue()));
 
 		newChildDescriptors.add
 			(createChildParameter
 				(LibraryPackage.Literals.NET_XRESOURCE__TRENDED_VALUES,
 				 GenericsFactory.eINSTANCE.createValue()));
 	}
 
 	/**
 	 * This returns the label text for {@link org.eclipse.emf.edit.command.CreateChildCommand}.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	@Override
 	public String getCreateChildText(Object owner, Object feature, Object child, Collection<?> selection) {
 		Object childFeature = feature;
 		Object childObject = child;
 
 		boolean qualify =
 			childFeature == LibraryPackage.Literals.NET_XRESOURCE__CAPACITY_VALUES ||
 			childFeature == LibraryPackage.Literals.NET_XRESOURCE__UTILIZATION_VALUES ||
 			childFeature == LibraryPackage.Literals.NET_XRESOURCE__FORECAST_CAPACITY_VALUES ||
 			childFeature == LibraryPackage.Literals.NET_XRESOURCE__FORECAST_VALUES ||
 			childFeature == LibraryPackage.Literals.NET_XRESOURCE__TRENDED_VALUES;
 
 		if (qualify) {
 			return getString
 				("_UI_CreateChild_text2",
 				 new Object[] { getTypeText(childObject), getFeatureText(childFeature), getTypeText(owner) });
 		}
 		return super.getCreateChildText(owner, feature, child, selection);
 	}
 
 }
