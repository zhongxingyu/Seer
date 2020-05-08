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
 package com.netxforge.netxstudio.metrics.provider;
 
 import java.util.Collection;
 import java.util.List;
 
 import org.eclipse.emf.common.notify.AdapterFactory;
 import org.eclipse.emf.common.notify.Notification;
 import org.eclipse.emf.common.util.ResourceLocator;
 import org.eclipse.emf.ecore.EStructuralFeature;
 import org.eclipse.emf.edit.provider.ComposeableAdapterFactory;
 import org.eclipse.emf.edit.provider.IEditingDomainItemProvider;
 import org.eclipse.emf.edit.provider.IItemLabelProvider;
 import org.eclipse.emf.edit.provider.IItemPropertyDescriptor;
 import org.eclipse.emf.edit.provider.IItemPropertySource;
 import org.eclipse.emf.edit.provider.IStructuredItemContentProvider;
 import org.eclipse.emf.edit.provider.ITreeItemContentProvider;
 import org.eclipse.emf.edit.provider.ItemPropertyDescriptor;
 import org.eclipse.emf.edit.provider.ViewerNotification;
 
 import com.netxforge.netxstudio.generics.provider.BaseItemProvider;
 import com.netxforge.netxstudio.generics.provider.NetxstudioEditPlugin;
 import com.netxforge.netxstudio.metrics.MetricSource;
 import com.netxforge.netxstudio.metrics.MetricsFactory;
 import com.netxforge.netxstudio.metrics.MetricsPackage;
 
 /**
  * This is the item provider adapter for a
  * {@link com.netxforge.netxstudio.metrics.MetricSource} object. <!--
  * begin-user-doc --> <!-- end-user-doc -->
  * 
  * @generated
  */
 public class MetricSourceItemProvider extends BaseItemProvider implements
 		IEditingDomainItemProvider, IStructuredItemContentProvider,
 		ITreeItemContentProvider, IItemLabelProvider, IItemPropertySource {
 	
 
 	/**
 	 * This constructs an instance from a factory and a notifier. <!--
 	 * begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	public MetricSourceItemProvider(AdapterFactory adapterFactory) {
 		super(adapterFactory);
 	}
 
 	/**
 	 * This returns the property descriptors for the adapted class. <!--
 	 * begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	@Override
 	public List<IItemPropertyDescriptor> getPropertyDescriptors(Object object) {
 		if (itemPropertyDescriptors == null) {
 			super.getPropertyDescriptors(object);
 
 			addFilterPatternPropertyDescriptor(object);
 			addMetricLocationPropertyDescriptor(object);
 			addNamePropertyDescriptor(object);
 		}
 		return itemPropertyDescriptors;
 	}
 
 	/**
 	 * This adds a property descriptor for the Filter Pattern feature. <!--
 	 * begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	protected void addFilterPatternPropertyDescriptor(Object object) {
 		itemPropertyDescriptors.add
 			(createItemPropertyDescriptor
 				(((ComposeableAdapterFactory)adapterFactory).getRootAdapterFactory(),
 				 getResourceLocator(),
 				 getString("_UI_MetricSource_filterPattern_feature"),
 				 getString("_UI_PropertyDescriptor_description", "_UI_MetricSource_filterPattern_feature", "_UI_MetricSource_type"),
 				 MetricsPackage.Literals.METRIC_SOURCE__FILTER_PATTERN,
 				 true,
 				 false,
 				 false,
 				 ItemPropertyDescriptor.GENERIC_VALUE_IMAGE,
 				 null,
 				 null));
 	}
 
 	/**
 	 * This adds a property descriptor for the Metric Location feature. <!--
 	 * begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	protected void addMetricLocationPropertyDescriptor(Object object) {
 		itemPropertyDescriptors.add
 			(createItemPropertyDescriptor
 				(((ComposeableAdapterFactory)adapterFactory).getRootAdapterFactory(),
 				 getResourceLocator(),
 				 getString("_UI_MetricSource_metricLocation_feature"),
 				 getString("_UI_PropertyDescriptor_description", "_UI_MetricSource_metricLocation_feature", "_UI_MetricSource_type"),
 				 MetricsPackage.Literals.METRIC_SOURCE__METRIC_LOCATION,
 				 true,
 				 false,
 				 false,
 				 ItemPropertyDescriptor.GENERIC_VALUE_IMAGE,
 				 null,
 				 null));
 	}
 
 	/**
 	 * This adds a property descriptor for the Name feature.
 	 * <!-- begin-user-doc
 	 * --> <!-- end-user-doc -->
 	 * @generated
 	 */
 	protected void addNamePropertyDescriptor(Object object) {
 		itemPropertyDescriptors.add
 			(createItemPropertyDescriptor
 				(((ComposeableAdapterFactory)adapterFactory).getRootAdapterFactory(),
 				 getResourceLocator(),
 				 getString("_UI_MetricSource_name_feature"),
 				 getString("_UI_PropertyDescriptor_description", "_UI_MetricSource_name_feature", "_UI_MetricSource_type"),
 				 MetricsPackage.Literals.METRIC_SOURCE__NAME,
 				 true,
 				 false,
 				 false,
 				 ItemPropertyDescriptor.GENERIC_VALUE_IMAGE,
 				 null,
 				 null));
 	}
 
 	/**
 	 * This specifies how to implement {@link #getChildren} and is used to deduce an appropriate feature for an
 	 * {@link org.eclipse.emf.edit.command.AddCommand}, {@link org.eclipse.emf.edit.command.RemoveCommand} or
 	 * {@link org.eclipse.emf.edit.command.MoveCommand} in {@link #createCommand}.
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * @generated
 	 */
 	@Override
 	public Collection<? extends EStructuralFeature> getChildrenFeatures(
 			Object object) {
 		if (childrenFeatures == null) {
 			super.getChildrenFeatures(object);
 			childrenFeatures.add(MetricsPackage.Literals.METRIC_SOURCE__METRIC_MAPPING);
 			childrenFeatures.add(MetricsPackage.Literals.METRIC_SOURCE__STATISTICS);
 		}
 		return childrenFeatures;
 	}
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * @generated
 	 */
 	@Override
 	protected EStructuralFeature getChildFeature(Object object, Object child) {
 		// Check the type of the specified child object and return the proper feature to use for
 		// adding (see {@link AddCommand}) it as a child.
 
 		return super.getChildFeature(object, child);
 	}
 
 	/**
 	 * This returns MetricSource.gif. <!-- begin-user-doc --> <!-- end-user-doc
 	 * -->
 	 * 
 	 * @generated NOT
 	 */
 	@Override
 	public Object getImage(Object object) {
		return overlayImage(object, getResourceLocator().getImage("full/obj16/MetricSource_H.png"));
 	}
 
 	/**
 	 * This returns the label text for the adapted class.
 	 * CB Adapted 17-01-2012, to show the calculated label between quotes. 
 	 * <!-- begin-user-doc
 	 * --> <!-- end-user-doc -->
 	 * @generated
 	 */
 	@Override
 	public String getText(Object object) {
 		String label = ((MetricSource)object).getName();
 		return label == null || label.length() == 0 ?
 			getString("_UI_MetricSource_type") :
 			getString("_UI_MetricSource_type") + " \"" + label + "\"";
 	}
 
 	/**
 	 * This handles model notifications by calling {@link #updateChildren} to update any cached
 	 * children and by creating a viewer notification, which it passes to {@link #fireNotifyChanged}.
 	 * <!-- begin-user-doc --> <!--
 	 * end-user-doc -->
 	 * @generated
 	 */
 	@Override
 	public void notifyChanged(Notification notification) {
 		updateChildren(notification);
 
 		switch (notification.getFeatureID(MetricSource.class)) {
 			case MetricsPackage.METRIC_SOURCE__FILTER_PATTERN:
 			case MetricsPackage.METRIC_SOURCE__METRIC_LOCATION:
 			case MetricsPackage.METRIC_SOURCE__NAME:
 				fireNotifyChanged(new ViewerNotification(notification, notification.getNotifier(), false, true));
 				return;
 			case MetricsPackage.METRIC_SOURCE__METRIC_MAPPING:
 			case MetricsPackage.METRIC_SOURCE__STATISTICS:
 				fireNotifyChanged(new ViewerNotification(notification, notification.getNotifier(), true, false));
 				return;
 		}
 		super.notifyChanged(notification);
 	}
 
 	/**
 	 * This adds {@link org.eclipse.emf.edit.command.CommandParameter}s
 	 * describing the children that can be created under this object. <!--
 	 * begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	@Override
 	protected void collectNewChildDescriptors(
 			Collection<Object> newChildDescriptors, Object object) {
 		super.collectNewChildDescriptors(newChildDescriptors, object);
 
 		newChildDescriptors.add
 			(createChildParameter
 				(MetricsPackage.Literals.METRIC_SOURCE__METRIC_MAPPING,
 				 MetricsFactory.eINSTANCE.createMapping()));
 
 		newChildDescriptors.add
 			(createChildParameter
 				(MetricsPackage.Literals.METRIC_SOURCE__METRIC_MAPPING,
 				 MetricsFactory.eINSTANCE.createMappingCSV()));
 
 		newChildDescriptors.add
 			(createChildParameter
 				(MetricsPackage.Literals.METRIC_SOURCE__METRIC_MAPPING,
 				 MetricsFactory.eINSTANCE.createMappingRDBMS()));
 
 		newChildDescriptors.add
 			(createChildParameter
 				(MetricsPackage.Literals.METRIC_SOURCE__METRIC_MAPPING,
 				 MetricsFactory.eINSTANCE.createMappingXLS()));
 
 		newChildDescriptors.add
 			(createChildParameter
 				(MetricsPackage.Literals.METRIC_SOURCE__STATISTICS,
 				 MetricsFactory.eINSTANCE.createMappingStatistic()));
 	}
 
 	/**
 	 * Return the resource locator for this item provider's resources. <!--
 	 * begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	@Override
 	public ResourceLocator getResourceLocator() {
 		return NetxstudioEditPlugin.INSTANCE;
 	}
 }	
