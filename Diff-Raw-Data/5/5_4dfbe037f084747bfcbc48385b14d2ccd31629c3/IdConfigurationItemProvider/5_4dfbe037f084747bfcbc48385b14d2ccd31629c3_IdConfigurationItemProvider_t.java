 /*******************************************************************************
  * Copyright (c) 2011 Formal Mind GmbH and University of Dusseldorf.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *     Michael Jastram - initial API and implementation
  *     Kay Münch       - vertical alignment of the spec objects id
  ******************************************************************************/
 
 package org.eclipse.rmf.reqif10.pror.presentation.id.provider;
 
 
 import java.util.Collection;
 import java.util.List;
 
 import org.agilemore.agilegrid.AgileGrid;
 import org.agilemore.agilegrid.CellEditor;
 import org.eclipse.emf.common.command.Command;
 import org.eclipse.emf.common.command.CompoundCommand;
 import org.eclipse.emf.common.notify.AdapterFactory;
 import org.eclipse.emf.common.notify.Notification;
 import org.eclipse.emf.common.notify.Notifier;
 import org.eclipse.emf.common.notify.impl.AdapterImpl;
 import org.eclipse.emf.common.util.ResourceLocator;
 import org.eclipse.emf.ecore.util.EContentAdapter;
 import org.eclipse.emf.edit.command.AddCommand;
 import org.eclipse.emf.edit.command.SetCommand;
 import org.eclipse.emf.edit.domain.EditingDomain;
 import org.eclipse.emf.edit.provider.ComposeableAdapterFactory;
 import org.eclipse.emf.edit.provider.IEditingDomainItemProvider;
 import org.eclipse.emf.edit.provider.IItemLabelProvider;
 import org.eclipse.emf.edit.provider.IItemPropertyDescriptor;
 import org.eclipse.emf.edit.provider.IItemPropertySource;
 import org.eclipse.emf.edit.provider.IStructuredItemContentProvider;
 import org.eclipse.emf.edit.provider.ITreeItemContentProvider;
 import org.eclipse.emf.edit.provider.ItemPropertyDescriptor;
 import org.eclipse.emf.edit.provider.ViewerNotification;
 import org.eclipse.rmf.reqif10.AttributeDefinition;
 import org.eclipse.rmf.reqif10.AttributeDefinitionString;
 import org.eclipse.rmf.reqif10.AttributeValue;
 import org.eclipse.rmf.reqif10.AttributeValueString;
 import org.eclipse.rmf.reqif10.DatatypeDefinition;
 import org.eclipse.rmf.reqif10.ReqIF10Package;
 import org.eclipse.rmf.reqif10.SpecElementWithAttributes;
 import org.eclipse.rmf.reqif10.SpecType;
 import org.eclipse.rmf.reqif10.common.util.ReqIF10Util;
 import org.eclipse.rmf.reqif10.pror.configuration.ConfigurationPackage;
 import org.eclipse.rmf.reqif10.pror.configuration.ProrPresentationConfiguration;
 import org.eclipse.rmf.reqif10.pror.configuration.provider.ProrPresentationConfigurationItemProvider;
 import org.eclipse.rmf.reqif10.pror.editor.presentation.service.IProrCellRenderer;
 import org.eclipse.rmf.reqif10.pror.editor.presentation.service.PresentationEditorInterface;
 import org.eclipse.rmf.reqif10.pror.presentation.id.IdConfiguration;
 import org.eclipse.rmf.reqif10.pror.presentation.id.IdPackage;
 import org.eclipse.rmf.reqif10.pror.presentation.ui.IdLabelCellRenderer;
 
 /**
  * This is the item provider adapter for a {@link org.eclipse.rmf.reqif10.pror.presentation.id.IdConfiguration} object.
  * <!-- begin-user-doc -->
  * <!-- end-user-doc -->
  * @generated
  */
 public class IdConfigurationItemProvider
 	extends ProrPresentationConfigurationItemProvider
 	implements
 		IEditingDomainItemProvider,
 		IStructuredItemContentProvider,
 		ITreeItemContentProvider,
 		IItemLabelProvider,
 		IItemPropertySource,
 		PresentationEditorInterface {
 	/**
 	 * This constructs an instance from a factory and a notifier.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public IdConfigurationItemProvider(AdapterFactory adapterFactory) {
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
 
 			addPrefixPropertyDescriptor(object);
 			addCountPropertyDescriptor(object);
 			addVerticalAlignPropertyDescriptor(object);
 		}
 		return itemPropertyDescriptors;
 	}
 
 	/**
 	 * This adds a property descriptor for the Prefix feature.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	protected void addPrefixPropertyDescriptor(Object object) {
 		itemPropertyDescriptors.add
 			(createItemPropertyDescriptor
 				(((ComposeableAdapterFactory)adapterFactory).getRootAdapterFactory(),
 				 getResourceLocator(),
 				 getString("_UI_IdConfiguration_prefix_feature"),
 				 getString("_UI_PropertyDescriptor_description", "_UI_IdConfiguration_prefix_feature", "_UI_IdConfiguration_type"),
 				 IdPackage.Literals.ID_CONFIGURATION__PREFIX,
 				 true,
 				 false,
 				 false,
 				 ItemPropertyDescriptor.GENERIC_VALUE_IMAGE,
 				 null,
 				 null));
 	}
 
 	/**
 	 * This adds a property descriptor for the Count feature.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	protected void addCountPropertyDescriptor(Object object) {
 		itemPropertyDescriptors.add
 			(createItemPropertyDescriptor
 				(((ComposeableAdapterFactory)adapterFactory).getRootAdapterFactory(),
 				 getResourceLocator(),
 				 getString("_UI_IdConfiguration_count_feature"),
 				 getString("_UI_PropertyDescriptor_description", "_UI_IdConfiguration_count_feature", "_UI_IdConfiguration_type"),
 				 IdPackage.Literals.ID_CONFIGURATION__COUNT,
 				 true,
 				 false,
 				 false,
 				 ItemPropertyDescriptor.INTEGRAL_VALUE_IMAGE,
 				 null,
 				 null));
 	}
 
 	/**
 	 * This adds a property descriptor for the Vertical Align feature.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	protected void addVerticalAlignPropertyDescriptor(Object object) {
 		itemPropertyDescriptors.add
 			(createItemPropertyDescriptor
 				(((ComposeableAdapterFactory)adapterFactory).getRootAdapterFactory(),
 				 getResourceLocator(),
 				 getString("_UI_IdConfiguration_verticalAlign_feature"),
 				 getString("_UI_PropertyDescriptor_description", "_UI_IdConfiguration_verticalAlign_feature", "_UI_IdConfiguration_type"),
 				 IdPackage.Literals.ID_CONFIGURATION__VERTICAL_ALIGN,
 				 true,
 				 false,
 				 false,
 				 ItemPropertyDescriptor.GENERIC_VALUE_IMAGE,
 				 null,
 				 null));
 	}
 
 	/**
 	 * This returns IdConfiguration.gif.
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
 		IdConfiguration idConfiguration = (IdConfiguration)object;
 		if (idConfiguration.getDatatype() == null) 
 			return getString("_UI_IdConfiguration_type_not_set");
 		return getString("_UI_IdConfiguration_type", new Object[] {
 				idConfiguration.getDatatype().getLongName(),
 				idConfiguration.getCount() });
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
 
 		switch (notification.getFeatureID(IdConfiguration.class)) {
 			case IdPackage.ID_CONFIGURATION__PREFIX:
 			case IdPackage.ID_CONFIGURATION__COUNT:
 			case IdPackage.ID_CONFIGURATION__VERTICAL_ALIGN:
 				fireNotifyChanged(new ViewerNotification(notification, notification.getNotifier(), false, true));
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
 	}
 
 	/**
 	 * Return the resource locator for this item provider's resources.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	@Override
 	public ResourceLocator getResourceLocator() {
 		return IDEditPlugin.INSTANCE;
 	}
 
 	// Listener to be attached to the ReqIF
 	private EContentAdapter contentAdapter;
 
 	/**
 	 * We do two things:
 	 * <ul>
 	 * <li>Register an adapter with the model to set IDs as needed
 	 * <li>Register an adapter with the config element to react to config value
 	 * changes.
 	 * <ul>
 	 */
 	public void registerPresentationConfiguration(
 			final ProrPresentationConfiguration config,
 			final EditingDomain editingDomain) {
 		registerModelListener((IdConfiguration) config, editingDomain);
 
 		config.eAdapters().add(new AdapterImpl() {
 			public void notifyChanged(Notification notification) {
 				if (notification.getFeature() == ConfigurationPackage.Literals.PROR_PRESENTATION_CONFIGURATION__DATATYPE) {
 					unregisterModelListener(config);
 					registerModelListener((IdConfiguration) config,
 							editingDomain);
 				}
 			}
 		});
 	}
 
 	public void unregisterPresentationConfiguration(
 			ProrPresentationConfiguration config) {
 		unregisterModelListener(config);
 	}
 
 	private void registerModelListener(final IdConfiguration config,
 			final EditingDomain editingDomain) {
 		if (contentAdapter != null) {
 			throw new IllegalStateException(
 					"Cannot register IDConfigAdapter without unregistering first!");
 		}
 		contentAdapter = new EContentAdapter() {
 
 			/**
 			 * Action has to be taken when the SpecType changes
 			 */
 			@Override
 			public void notifyChanged(Notification notification) {
 				super.notifyChanged(notification);
 				if (notification.getNotifier() instanceof SpecElementWithAttributes) {
 					updateSpecElementIfNecessary(config,
 							(SpecElementWithAttributes) notification
 									.getNotifier(), editingDomain);
 				}
 			}
 
 			@Override
 			public void setTarget(final Notifier target) {
 				super.setTarget(target);
 				if (target instanceof SpecElementWithAttributes) {
 					updateSpecElementIfNecessary(config,
 							(SpecElementWithAttributes) target, editingDomain);
 				}
 			}
 		};
 		ReqIF10Util.getReqIF(config).getCoreContent().eAdapters()
 				.add(contentAdapter);
 	}
 
 	protected void updateSpecElementIfNecessary(IdConfiguration config,
 			SpecElementWithAttributes target, EditingDomain editingDomain) {
		
		if (config.getDatatype() == null) {
			return;
		}

 		SpecElementWithAttributes specElement = (SpecElementWithAttributes) target;
 
 		// 1. Find out whether there is a matching AttributeDefinition
 		SpecType type = ReqIF10Util.getSpecType(specElement);
 		if (type == null)
 			return;
 
 		AttributeDefinitionString attrDef = null;
 		for (AttributeDefinition ad : type.getSpecAttributes()) {
 			DatatypeDefinition dd = ReqIF10Util.getDatatypeDefinition(ad);
 			if (config.getDatatype().equals(dd)) {
 				attrDef = (AttributeDefinitionString) ad;
 				break;
 			}
 		}
 
 		// 2. No: Nothing to do, otherwise find Find out whether
 		// there is already a value
 		if (attrDef == null)
 			return;
 		AttributeValueString value = (AttributeValueString) ReqIF10Util
 				.getAttributeValue(specElement, attrDef);
 
 		// 3. Yes: Nothing to do, otherwise proceed
 		if (value != null)
 			return;
 		value = (AttributeValueString) ReqIF10Util
 				.createAttributeValue(attrDef);
 		int newCount = config.getCount() + 1;
 		value.setTheValue(config.getPrefix() + newCount);
 
 		String label = "Assigning ID " + config.getPrefix() + newCount;
 		CompoundCommand cmd = new CompoundCommand(label);
 		cmd.append(SetCommand.create(editingDomain, config,
 				IdPackage.Literals.ID_CONFIGURATION__COUNT, newCount));
 		cmd.append(AddCommand.create(editingDomain, specElement,
 				ReqIF10Package.Literals.SPEC_ELEMENT_WITH_ATTRIBUTES__VALUES,
 				value));
 		editingDomain.getCommandStack().execute(cmd);
 	}
 
 	private void unregisterModelListener(ProrPresentationConfiguration config) {
 		if (contentAdapter != null) {
 			ReqIF10Util.getReqIF(config).getCoreContent().eAdapters()
 					.remove(contentAdapter);
 			contentAdapter = null;
 		}
 	}
 
 	public Command handleDragAndDrop(Collection<?> source, Object target,
 			EditingDomain editingDomain, int operation) {
 		// No drag and drop support
 		return null;
 	}
 
 	public String getLabel(AttributeValue av) {
 		// No custom label
 		return null;
 	}
 
 	/**
 	 * Don't allow editing.
 	 */
 	public boolean canEdit() {
 		return false;
 	}
 	
 	IProrCellRenderer renderer = null;
 
 	/**
 	 * Use special renderer.
 	 */
 	public IProrCellRenderer getCellRenderer(AttributeValue av) {
 		if (renderer == null) {
 			renderer = new IdLabelCellRenderer();
 		}
 		return renderer;
 	}
 
 	/**
 	 * No special {@link CellEditor}.
 	 */
 	public CellEditor getCellEditor(AgileGrid agileGrid,
 			EditingDomain editingDomain, AttributeValue av,
 			SpecElementWithAttributes parent, Object affectedObject) {
 		return null;
 	}
 
 	/**
 	 * Not suggested as default.
 	 */
 	public Class<? extends DatatypeDefinition> suggestAsDefault() {
 		return null;
 	}
 
 }
