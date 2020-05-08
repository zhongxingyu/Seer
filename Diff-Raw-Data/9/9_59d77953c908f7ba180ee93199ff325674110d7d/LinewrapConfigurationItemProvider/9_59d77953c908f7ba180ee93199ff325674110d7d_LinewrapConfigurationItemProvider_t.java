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
 
 package org.eclipse.rmf.reqif10.pror.presentation.linewrap.provider;
 
 
 import java.util.Collection;
 import java.util.List;
 
 import org.agilemore.agilegrid.AgileGrid;
 import org.agilemore.agilegrid.CellEditor;
 import org.eclipse.emf.common.command.Command;
 import org.eclipse.emf.common.notify.AdapterFactory;
 import org.eclipse.emf.common.notify.Notification;
 import org.eclipse.emf.common.util.ResourceLocator;
 import org.eclipse.emf.edit.domain.EditingDomain;
 import org.eclipse.emf.edit.provider.IEditingDomainItemProvider;
 import org.eclipse.emf.edit.provider.IItemLabelProvider;
 import org.eclipse.emf.edit.provider.IItemPropertyDescriptor;
 import org.eclipse.emf.edit.provider.IItemPropertySource;
 import org.eclipse.emf.edit.provider.IStructuredItemContentProvider;
 import org.eclipse.emf.edit.provider.ITreeItemContentProvider;
 import org.eclipse.rmf.reqif10.AttributeValue;
 import org.eclipse.rmf.reqif10.DatatypeDefinition;
 import org.eclipse.rmf.reqif10.DatatypeDefinitionSimple;
 import org.eclipse.rmf.reqif10.DatatypeDefinitionString;
 import org.eclipse.rmf.reqif10.SpecElementWithAttributes;
 import org.eclipse.rmf.reqif10.common.util.ReqIF10Util;
 import org.eclipse.rmf.reqif10.pror.configuration.ProrPresentationConfiguration;
 import org.eclipse.rmf.reqif10.pror.configuration.provider.ProrPresentationConfigurationItemProvider;
 import org.eclipse.rmf.reqif10.pror.editor.presentation.service.IProrCellRenderer;
 import org.eclipse.rmf.reqif10.pror.editor.presentation.service.PresentationEditorInterface;
 import org.eclipse.rmf.reqif10.pror.presentation.LinewrapEditPlugin;
 import org.eclipse.rmf.reqif10.pror.presentation.linewrap.LinewrapConfiguration;
 import org.eclipse.rmf.reqif10.pror.presentation.ui.LinewrapCellEditor;
 import org.eclipse.rmf.reqif10.pror.presentation.ui.LinewrapCellRenderer;
 
 /**
  * This is the item provider adapter for a {@link org.eclipse.rmf.reqif10.pror.presentation.linewrap.LinewrapConfiguration} object.
  * <!-- begin-user-doc -->
 * Modified to implement {@link PresentationEditorInterface}
  * <!-- end-user-doc -->
 * @generated NOT
  */
 public class LinewrapConfigurationItemProvider
 	extends ProrPresentationConfigurationItemProvider
 	implements
		IEditingDomainItemProvider, IStructuredItemContentProvider, 
		ITreeItemContentProvider, IItemLabelProvider, IItemPropertySource,
		PresentationEditorInterface {
 	/**
 	 * This constructs an instance from a factory and a notifier.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public LinewrapConfigurationItemProvider(AdapterFactory adapterFactory) {
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
 
 		}
 		return itemPropertyDescriptors;
 	}
 
 	/**
 	 * This returns LinewrapConfiguration.gif.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated NOT
 	 */
 	@Override
 	public Object getImage(Object object) {
 		return super.getImage(object);
 	}
 
 	/**
 	 * This returns the label text for the adapted class.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated NOT
 	 */
 	@Override
 	public String getText(Object object) {
 		LinewrapConfiguration config = (LinewrapConfiguration) object;
 		if (config.getDatatype() == null) {
 			return getString("_UI_LinewrapConfiguration_type_not_configured");
 		} else if (!(config.getDatatype() instanceof DatatypeDefinitionSimple)) {
 			return getString("_UI_LinewrapConfiguration_type_incorrect_type");
 		} else {
 			return getString("_UI_LinewrapConfiguration_type",
 					new Object[] { config.getDatatype().getLongName() });
 		}
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
 	}
 
 	/**
 	 * Return the resource locator for this item provider's resources.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	@Override
 	public ResourceLocator getResourceLocator() {
 		return LinewrapEditPlugin.INSTANCE;
 	}
 
 	public void registerPresentationConfiguration(ProrPresentationConfiguration config,
 			EditingDomain editingDomain) {
 		// No action required.
 
 	}
 
 	public void unregisterPresentationConfiguration(ProrPresentationConfiguration config) {
 		// No action required.
 	}
 
 	private LinewrapCellRenderer linewrapCellRenderer;
 
 
 	public IProrCellRenderer getCellRenderer(AttributeValue av) {
 		if (linewrapCellRenderer == null) {
 			linewrapCellRenderer = new LinewrapCellRenderer();
 		}
 		return linewrapCellRenderer;
 	}
 
 	public String getLabel(AttributeValue av) {
 		Object value = ReqIF10Util.getTheValue(av);
 		if (value == null) {
 			return "";
 		}
 		String text = value.toString();
 		if (text.indexOf("\n") != -1) {
 			text = text.substring(0, text.indexOf("\n"));
 		}
 		if (text.length() > 15) {
 			text = text.substring(0, 13) + "...";
 		}
 		return text;
 	}
 
 	public CellEditor getCellEditor(AgileGrid agileGrid,
 			EditingDomain editingDomain, AttributeValue av,
 			SpecElementWithAttributes parent, Object affectedObject) {
 		return new LinewrapCellEditor(agileGrid, editingDomain, parent,
 				affectedObject);
 	}
 
 	public boolean canEdit() {
 		return true;
 	}
 
 	public Command handleDragAndDrop(Collection<?> source, Object target,
 			EditingDomain editingDomain, int operation) {
 		return null;
 	}
 
 	/**
 	 * Use as default for Strings
 	 */
 	public Class<? extends DatatypeDefinition> suggestAsDefault() {
 		return DatatypeDefinitionString.class;
 	}
 }
