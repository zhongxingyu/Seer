 /**
  * <copyright>
  * </copyright>
  *
  * $Id$
  */
 package org.eclipse.gmf.codegen.gmfgen.provider;
 
 
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
 import org.eclipse.emf.edit.provider.ItemProviderAdapter;
 import org.eclipse.emf.edit.provider.ViewerNotification;
 import org.eclipse.gmf.codegen.gmfgen.GMFGenFactory;
 import org.eclipse.gmf.codegen.gmfgen.GMFGenPackage;
 import org.eclipse.gmf.codegen.gmfgen.GenDiagramPreferences;
 import org.eclipse.gmf.codegen.gmfgen.Routing;
 import org.eclipse.gmf.codegen.gmfgen.presentation.EditorPlugin;
 
 /**
  * This is the item provider adapter for a {@link org.eclipse.gmf.codegen.gmfgen.GenDiagramPreferences} object.
  * <!-- begin-user-doc -->
  * <!-- end-user-doc -->
  * @generated
  */
 public class GenDiagramPreferencesItemProvider
 	extends ItemProviderAdapter
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
 	public GenDiagramPreferencesItemProvider(AdapterFactory adapterFactory) {
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
 
 			addLineStylePropertyDescriptor(object);
 			addShowConnectionHandlesPropertyDescriptor(object);
 			addShowPopupBarsPropertyDescriptor(object);
 			addPromptOnDelFromModelPropertyDescriptor(object);
 			addPromptOnDelFromDiagramPropertyDescriptor(object);
 			addEnableAnimatedLayoutPropertyDescriptor(object);
 			addEnableAnimatedZoomPropertyDescriptor(object);
 			addEnableAntiAliasPropertyDescriptor(object);
 			addShowGridPropertyDescriptor(object);
 			addShowRulersPropertyDescriptor(object);
 			addSnapToGridPropertyDescriptor(object);
 			addRulerUnitsPropertyDescriptor(object);
 			addGridSpacingPropertyDescriptor(object);
 		}
 		return itemPropertyDescriptors;
 	}
 
 	/**
 	 * This adds a property descriptor for the Line Style feature.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	protected void addLineStylePropertyDescriptor(Object object) {
 		itemPropertyDescriptors.add
 			(createItemPropertyDescriptor
 				(((ComposeableAdapterFactory)adapterFactory).getRootAdapterFactory(),
 				 getResourceLocator(),
 				 getString("_UI_GenDiagramPreferences_lineStyle_feature"),
 				 getString("_UI_PropertyDescriptor_description", "_UI_GenDiagramPreferences_lineStyle_feature", "_UI_GenDiagramPreferences_type"),
 				 GMFGenPackage.eINSTANCE.getGenDiagramPreferences_LineStyle(),
 				 true,
 				 false,
 				 false,
 				 ItemPropertyDescriptor.GENERIC_VALUE_IMAGE,
 				 null,
 				 null));
 	}
 
 	/**
 	 * This adds a property descriptor for the Show Connection Handles feature.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	protected void addShowConnectionHandlesPropertyDescriptor(Object object) {
 		itemPropertyDescriptors.add
 			(createItemPropertyDescriptor
 				(((ComposeableAdapterFactory)adapterFactory).getRootAdapterFactory(),
 				 getResourceLocator(),
 				 getString("_UI_GenDiagramPreferences_showConnectionHandles_feature"),
 				 getString("_UI_PropertyDescriptor_description", "_UI_GenDiagramPreferences_showConnectionHandles_feature", "_UI_GenDiagramPreferences_type"),
 				 GMFGenPackage.eINSTANCE.getGenDiagramPreferences_ShowConnectionHandles(),
 				 true,
 				 false,
 				 false,
 				 ItemPropertyDescriptor.BOOLEAN_VALUE_IMAGE,
 				 null,
 				 null));
 	}
 
 	/**
 	 * This adds a property descriptor for the Show Popup Bars feature.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	protected void addShowPopupBarsPropertyDescriptor(Object object) {
 		itemPropertyDescriptors.add
 			(createItemPropertyDescriptor
 				(((ComposeableAdapterFactory)adapterFactory).getRootAdapterFactory(),
 				 getResourceLocator(),
 				 getString("_UI_GenDiagramPreferences_showPopupBars_feature"),
 				 getString("_UI_PropertyDescriptor_description", "_UI_GenDiagramPreferences_showPopupBars_feature", "_UI_GenDiagramPreferences_type"),
 				 GMFGenPackage.eINSTANCE.getGenDiagramPreferences_ShowPopupBars(),
 				 true,
 				 false,
 				 false,
 				 ItemPropertyDescriptor.BOOLEAN_VALUE_IMAGE,
 				 null,
 				 null));
 	}
 
 	/**
 	 * This adds a property descriptor for the Prompt On Del From Model feature.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	protected void addPromptOnDelFromModelPropertyDescriptor(Object object) {
 		itemPropertyDescriptors.add
 			(createItemPropertyDescriptor
 				(((ComposeableAdapterFactory)adapterFactory).getRootAdapterFactory(),
 				 getResourceLocator(),
 				 getString("_UI_GenDiagramPreferences_promptOnDelFromModel_feature"),
 				 getString("_UI_PropertyDescriptor_description", "_UI_GenDiagramPreferences_promptOnDelFromModel_feature", "_UI_GenDiagramPreferences_type"),
 				 GMFGenPackage.eINSTANCE.getGenDiagramPreferences_PromptOnDelFromModel(),
 				 true,
 				 false,
 				 false,
 				 ItemPropertyDescriptor.BOOLEAN_VALUE_IMAGE,
 				 null,
 				 null));
 	}
 
 	/**
 	 * This adds a property descriptor for the Prompt On Del From Diagram feature.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	protected void addPromptOnDelFromDiagramPropertyDescriptor(Object object) {
 		itemPropertyDescriptors.add
 			(createItemPropertyDescriptor
 				(((ComposeableAdapterFactory)adapterFactory).getRootAdapterFactory(),
 				 getResourceLocator(),
 				 getString("_UI_GenDiagramPreferences_promptOnDelFromDiagram_feature"),
 				 getString("_UI_PropertyDescriptor_description", "_UI_GenDiagramPreferences_promptOnDelFromDiagram_feature", "_UI_GenDiagramPreferences_type"),
 				 GMFGenPackage.eINSTANCE.getGenDiagramPreferences_PromptOnDelFromDiagram(),
 				 true,
 				 false,
 				 false,
 				 ItemPropertyDescriptor.BOOLEAN_VALUE_IMAGE,
 				 null,
 				 null));
 	}
 
 	/**
 	 * This adds a property descriptor for the Enable Animated Layout feature.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	protected void addEnableAnimatedLayoutPropertyDescriptor(Object object) {
 		itemPropertyDescriptors.add
 			(createItemPropertyDescriptor
 				(((ComposeableAdapterFactory)adapterFactory).getRootAdapterFactory(),
 				 getResourceLocator(),
 				 getString("_UI_GenDiagramPreferences_enableAnimatedLayout_feature"),
 				 getString("_UI_PropertyDescriptor_description", "_UI_GenDiagramPreferences_enableAnimatedLayout_feature", "_UI_GenDiagramPreferences_type"),
 				 GMFGenPackage.eINSTANCE.getGenDiagramPreferences_EnableAnimatedLayout(),
 				 true,
 				 false,
 				 false,
 				 ItemPropertyDescriptor.BOOLEAN_VALUE_IMAGE,
 				 null,
 				 null));
 	}
 
 	/**
 	 * This adds a property descriptor for the Enable Animated Zoom feature.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	protected void addEnableAnimatedZoomPropertyDescriptor(Object object) {
 		itemPropertyDescriptors.add
 			(createItemPropertyDescriptor
 				(((ComposeableAdapterFactory)adapterFactory).getRootAdapterFactory(),
 				 getResourceLocator(),
 				 getString("_UI_GenDiagramPreferences_enableAnimatedZoom_feature"),
 				 getString("_UI_PropertyDescriptor_description", "_UI_GenDiagramPreferences_enableAnimatedZoom_feature", "_UI_GenDiagramPreferences_type"),
 				 GMFGenPackage.eINSTANCE.getGenDiagramPreferences_EnableAnimatedZoom(),
 				 true,
 				 false,
 				 false,
 				 ItemPropertyDescriptor.BOOLEAN_VALUE_IMAGE,
 				 null,
 				 null));
 	}
 
 	/**
 	 * This adds a property descriptor for the Enable Anti Alias feature.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	protected void addEnableAntiAliasPropertyDescriptor(Object object) {
 		itemPropertyDescriptors.add
 			(createItemPropertyDescriptor
 				(((ComposeableAdapterFactory)adapterFactory).getRootAdapterFactory(),
 				 getResourceLocator(),
 				 getString("_UI_GenDiagramPreferences_enableAntiAlias_feature"),
 				 getString("_UI_PropertyDescriptor_description", "_UI_GenDiagramPreferences_enableAntiAlias_feature", "_UI_GenDiagramPreferences_type"),
 				 GMFGenPackage.eINSTANCE.getGenDiagramPreferences_EnableAntiAlias(),
 				 true,
 				 false,
 				 false,
 				 ItemPropertyDescriptor.BOOLEAN_VALUE_IMAGE,
 				 null,
 				 null));
 	}
 
 	/**
 	 * This adds a property descriptor for the Show Grid feature.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	protected void addShowGridPropertyDescriptor(Object object) {
 		itemPropertyDescriptors.add
 			(createItemPropertyDescriptor
 				(((ComposeableAdapterFactory)adapterFactory).getRootAdapterFactory(),
 				 getResourceLocator(),
 				 getString("_UI_GenDiagramPreferences_showGrid_feature"),
 				 getString("_UI_PropertyDescriptor_description", "_UI_GenDiagramPreferences_showGrid_feature", "_UI_GenDiagramPreferences_type"),
 				 GMFGenPackage.eINSTANCE.getGenDiagramPreferences_ShowGrid(),
 				 true,
 				 false,
 				 false,
 				 ItemPropertyDescriptor.BOOLEAN_VALUE_IMAGE,
 				 null,
 				 null));
 	}
 
 	/**
 	 * This adds a property descriptor for the Show Rulers feature.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	protected void addShowRulersPropertyDescriptor(Object object) {
 		itemPropertyDescriptors.add
 			(createItemPropertyDescriptor
 				(((ComposeableAdapterFactory)adapterFactory).getRootAdapterFactory(),
 				 getResourceLocator(),
 				 getString("_UI_GenDiagramPreferences_showRulers_feature"),
 				 getString("_UI_PropertyDescriptor_description", "_UI_GenDiagramPreferences_showRulers_feature", "_UI_GenDiagramPreferences_type"),
 				 GMFGenPackage.eINSTANCE.getGenDiagramPreferences_ShowRulers(),
 				 true,
 				 false,
 				 false,
 				 ItemPropertyDescriptor.BOOLEAN_VALUE_IMAGE,
 				 null,
 				 null));
 	}
 
 	/**
 	 * This adds a property descriptor for the Snap To Grid feature.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	protected void addSnapToGridPropertyDescriptor(Object object) {
 		itemPropertyDescriptors.add
 			(createItemPropertyDescriptor
 				(((ComposeableAdapterFactory)adapterFactory).getRootAdapterFactory(),
 				 getResourceLocator(),
 				 getString("_UI_GenDiagramPreferences_snapToGrid_feature"),
 				 getString("_UI_PropertyDescriptor_description", "_UI_GenDiagramPreferences_snapToGrid_feature", "_UI_GenDiagramPreferences_type"),
 				 GMFGenPackage.eINSTANCE.getGenDiagramPreferences_SnapToGrid(),
 				 true,
 				 false,
 				 false,
 				 ItemPropertyDescriptor.BOOLEAN_VALUE_IMAGE,
 				 null,
 				 null));
 	}
 
 	/**
 	 * This adds a property descriptor for the Ruler Units feature.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	protected void addRulerUnitsPropertyDescriptor(Object object) {
 		itemPropertyDescriptors.add
 			(createItemPropertyDescriptor
 				(((ComposeableAdapterFactory)adapterFactory).getRootAdapterFactory(),
 				 getResourceLocator(),
 				 getString("_UI_GenDiagramPreferences_rulerUnits_feature"),
 				 getString("_UI_PropertyDescriptor_description", "_UI_GenDiagramPreferences_rulerUnits_feature", "_UI_GenDiagramPreferences_type"),
 				 GMFGenPackage.eINSTANCE.getGenDiagramPreferences_RulerUnits(),
 				 true,
 				 false,
 				 false,
 				 ItemPropertyDescriptor.GENERIC_VALUE_IMAGE,
 				 null,
 				 null));
 	}
 
 	/**
 	 * This adds a property descriptor for the Grid Spacing feature.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	protected void addGridSpacingPropertyDescriptor(Object object) {
 		itemPropertyDescriptors.add
 			(createItemPropertyDescriptor
 				(((ComposeableAdapterFactory)adapterFactory).getRootAdapterFactory(),
 				 getResourceLocator(),
 				 getString("_UI_GenDiagramPreferences_gridSpacing_feature"),
 				 getString("_UI_PropertyDescriptor_description", "_UI_GenDiagramPreferences_gridSpacing_feature", "_UI_GenDiagramPreferences_type"),
 				 GMFGenPackage.eINSTANCE.getGenDiagramPreferences_GridSpacing(),
 				 true,
 				 false,
 				 false,
 				 ItemPropertyDescriptor.REAL_VALUE_IMAGE,
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
 			childrenFeatures.add(GMFGenPackage.eINSTANCE.getGenDiagramPreferences_DefaultFont());
 			childrenFeatures.add(GMFGenPackage.eINSTANCE.getGenDiagramPreferences_FontColor());
 			childrenFeatures.add(GMFGenPackage.eINSTANCE.getGenDiagramPreferences_FillColor());
 			childrenFeatures.add(GMFGenPackage.eINSTANCE.getGenDiagramPreferences_LineColor());
 			childrenFeatures.add(GMFGenPackage.eINSTANCE.getGenDiagramPreferences_NoteFillColor());
 			childrenFeatures.add(GMFGenPackage.eINSTANCE.getGenDiagramPreferences_NoteLineColor());
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
 	 * This returns GenDiagramPreferences.gif.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	@Override
 	public Object getImage(Object object) {
 		return overlayImage(object, getResourceLocator().getImage("full/obj16/GenDiagramPreferences"));
 	}
 
 	/**
 	 * This returns the label text for the adapted class.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
	 * @generated NOT
 	 */
 	@Override
 	public String getText(Object object) {
		return getString("_UI_GenDiagramPreferences_type");
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
 
 		switch (notification.getFeatureID(GenDiagramPreferences.class)) {
 			case GMFGenPackage.GEN_DIAGRAM_PREFERENCES__LINE_STYLE:
 			case GMFGenPackage.GEN_DIAGRAM_PREFERENCES__SHOW_CONNECTION_HANDLES:
 			case GMFGenPackage.GEN_DIAGRAM_PREFERENCES__SHOW_POPUP_BARS:
 			case GMFGenPackage.GEN_DIAGRAM_PREFERENCES__PROMPT_ON_DEL_FROM_MODEL:
 			case GMFGenPackage.GEN_DIAGRAM_PREFERENCES__PROMPT_ON_DEL_FROM_DIAGRAM:
 			case GMFGenPackage.GEN_DIAGRAM_PREFERENCES__ENABLE_ANIMATED_LAYOUT:
 			case GMFGenPackage.GEN_DIAGRAM_PREFERENCES__ENABLE_ANIMATED_ZOOM:
 			case GMFGenPackage.GEN_DIAGRAM_PREFERENCES__ENABLE_ANTI_ALIAS:
 			case GMFGenPackage.GEN_DIAGRAM_PREFERENCES__SHOW_GRID:
 			case GMFGenPackage.GEN_DIAGRAM_PREFERENCES__SHOW_RULERS:
 			case GMFGenPackage.GEN_DIAGRAM_PREFERENCES__SNAP_TO_GRID:
 			case GMFGenPackage.GEN_DIAGRAM_PREFERENCES__RULER_UNITS:
 			case GMFGenPackage.GEN_DIAGRAM_PREFERENCES__GRID_SPACING:
 				fireNotifyChanged(new ViewerNotification(notification, notification.getNotifier(), false, true));
 				return;
 			case GMFGenPackage.GEN_DIAGRAM_PREFERENCES__DEFAULT_FONT:
 			case GMFGenPackage.GEN_DIAGRAM_PREFERENCES__FONT_COLOR:
 			case GMFGenPackage.GEN_DIAGRAM_PREFERENCES__FILL_COLOR:
 			case GMFGenPackage.GEN_DIAGRAM_PREFERENCES__LINE_COLOR:
 			case GMFGenPackage.GEN_DIAGRAM_PREFERENCES__NOTE_FILL_COLOR:
 			case GMFGenPackage.GEN_DIAGRAM_PREFERENCES__NOTE_LINE_COLOR:
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
 				(GMFGenPackage.eINSTANCE.getGenDiagramPreferences_DefaultFont(),
 				 GMFGenFactory.eINSTANCE.createGenStandardFont()));
 
 		newChildDescriptors.add
 			(createChildParameter
 				(GMFGenPackage.eINSTANCE.getGenDiagramPreferences_DefaultFont(),
 				 GMFGenFactory.eINSTANCE.createGenCustomFont()));
 
 		newChildDescriptors.add
 			(createChildParameter
 				(GMFGenPackage.eINSTANCE.getGenDiagramPreferences_FontColor(),
 				 GMFGenFactory.eINSTANCE.createGenRGBColor()));
 
 		newChildDescriptors.add
 			(createChildParameter
 				(GMFGenPackage.eINSTANCE.getGenDiagramPreferences_FontColor(),
 				 GMFGenFactory.eINSTANCE.createGenConstantColor()));
 
 		newChildDescriptors.add
 			(createChildParameter
 				(GMFGenPackage.eINSTANCE.getGenDiagramPreferences_FillColor(),
 				 GMFGenFactory.eINSTANCE.createGenRGBColor()));
 
 		newChildDescriptors.add
 			(createChildParameter
 				(GMFGenPackage.eINSTANCE.getGenDiagramPreferences_FillColor(),
 				 GMFGenFactory.eINSTANCE.createGenConstantColor()));
 
 		newChildDescriptors.add
 			(createChildParameter
 				(GMFGenPackage.eINSTANCE.getGenDiagramPreferences_LineColor(),
 				 GMFGenFactory.eINSTANCE.createGenRGBColor()));
 
 		newChildDescriptors.add
 			(createChildParameter
 				(GMFGenPackage.eINSTANCE.getGenDiagramPreferences_LineColor(),
 				 GMFGenFactory.eINSTANCE.createGenConstantColor()));
 
 		newChildDescriptors.add
 			(createChildParameter
 				(GMFGenPackage.eINSTANCE.getGenDiagramPreferences_NoteFillColor(),
 				 GMFGenFactory.eINSTANCE.createGenRGBColor()));
 
 		newChildDescriptors.add
 			(createChildParameter
 				(GMFGenPackage.eINSTANCE.getGenDiagramPreferences_NoteFillColor(),
 				 GMFGenFactory.eINSTANCE.createGenConstantColor()));
 
 		newChildDescriptors.add
 			(createChildParameter
 				(GMFGenPackage.eINSTANCE.getGenDiagramPreferences_NoteLineColor(),
 				 GMFGenFactory.eINSTANCE.createGenRGBColor()));
 
 		newChildDescriptors.add
 			(createChildParameter
 				(GMFGenPackage.eINSTANCE.getGenDiagramPreferences_NoteLineColor(),
 				 GMFGenFactory.eINSTANCE.createGenConstantColor()));
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
 			childFeature == GMFGenPackage.eINSTANCE.getGenDiagramPreferences_FontColor() ||
 			childFeature == GMFGenPackage.eINSTANCE.getGenDiagramPreferences_FillColor() ||
 			childFeature == GMFGenPackage.eINSTANCE.getGenDiagramPreferences_LineColor() ||
 			childFeature == GMFGenPackage.eINSTANCE.getGenDiagramPreferences_NoteFillColor() ||
 			childFeature == GMFGenPackage.eINSTANCE.getGenDiagramPreferences_NoteLineColor();
 
 		if (qualify) {
 			return getString
 				("_UI_CreateChild_text2",
 				 new Object[] { getTypeText(childObject), getFeatureText(childFeature), getTypeText(owner) });
 		}
 		return super.getCreateChildText(owner, feature, child, selection);
 	}
 
 	/**
 	 * Return the resource locator for this item provider's resources.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	@Override
 	public ResourceLocator getResourceLocator() {
 		return EditorPlugin.INSTANCE;
 	}
 
 }
