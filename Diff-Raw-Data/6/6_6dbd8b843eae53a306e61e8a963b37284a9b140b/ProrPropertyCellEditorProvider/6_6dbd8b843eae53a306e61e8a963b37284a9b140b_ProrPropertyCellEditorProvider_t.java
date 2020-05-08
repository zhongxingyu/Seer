 /*******************************************************************************
  * Copyright (c) 2011 Formal Mind GmbH and University of Dusseldorf.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *     Michael Jastram - initial API and implementation
  *     Lukas Ladenberger - ProR GUI     
  ******************************************************************************/
 package org.eclipse.rmf.reqif10.pror.editor.propertiesview;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.List;
 
 import org.agilemore.agilegrid.AgileGrid;
 import org.agilemore.agilegrid.CellEditor;
 import org.agilemore.agilegrid.ICellEditorValidator;
 import org.eclipse.emf.common.notify.AdapterFactory;
 import org.eclipse.emf.common.util.Diagnostic;
 import org.eclipse.emf.ecore.EClassifier;
 import org.eclipse.emf.ecore.EDataType;
 import org.eclipse.emf.ecore.EReference;
 import org.eclipse.emf.ecore.EStructuralFeature;
 import org.eclipse.emf.ecore.util.Diagnostician;
 import org.eclipse.emf.ecore.util.EcoreUtil;
 import org.eclipse.emf.edit.domain.EditingDomain;
 import org.eclipse.emf.edit.provider.IItemLabelProvider;
 import org.eclipse.emf.edit.provider.IItemPropertyDescriptor;
 import org.eclipse.emf.edit.provider.ItemPropertyDescriptor.PropertyValueWrapper;
 import org.eclipse.emf.edit.provider.ItemProviderAdapter;
 import org.eclipse.emf.edit.ui.EMFEditUIPlugin;
 import org.eclipse.emf.edit.ui.celleditor.FeatureEditorDialog;
 import org.eclipse.emf.edit.ui.provider.ExtendedImageRegistry;
 import org.eclipse.jface.dialogs.IInputValidator;
 import org.eclipse.jface.dialogs.InputDialog;
 import org.eclipse.jface.viewers.ILabelProvider;
 import org.eclipse.jface.viewers.LabelProvider;
 import org.eclipse.jface.window.Window;
 import org.eclipse.rmf.reqif10.AttributeValue;
 import org.eclipse.rmf.reqif10.SpecElementWithAttributes;
 import org.eclipse.rmf.reqif10.SpecHierarchy;
 import org.eclipse.rmf.reqif10.pror.configuration.ProrPresentationConfiguration;
 import org.eclipse.rmf.reqif10.pror.editor.agilegrid.AbstractProrCellEditorProvider;
 import org.eclipse.rmf.reqif10.pror.editor.presentation.service.PresentationEditorInterface;
 import org.eclipse.rmf.reqif10.pror.editor.presentation.service.PresentationServiceManager;
 import org.eclipse.rmf.reqif10.pror.editor.propertiesview.ProrPropertyContentProvider.Descriptor;
 import org.eclipse.rmf.reqif10.pror.editor.propertiesview.ProrPropertyContentProvider.PropertyRow;
 import org.eclipse.rmf.reqif10.pror.util.ConfigurationUtil;
 import org.eclipse.rmf.reqif10.pror.util.ProrUtil;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.swt.widgets.Shell;
 
 /**
  * The cell editor provider for the properties view.
  * 
  * @author Lukas Ladenberger
  * @author Michael Jastram
  */
 public class ProrPropertyCellEditorProvider extends AbstractProrCellEditorProvider {
 
 	private final ProrPropertyContentProvider contentProvider;
 	
 	public ProrPropertyCellEditorProvider(AgileGrid agileGrid,
 			AdapterFactory adapterFactory, EditingDomain editingDomain,
 			ProrPropertyContentProvider contentProvider) {
 		super(agileGrid, adapterFactory, editingDomain);
 		this.contentProvider = contentProvider;
 	}
 
 	@Override
 	public Object getValue(int row, int col) {
 		PropertyRow propertyRow = contentProvider.getRowContent(row);
 		if (propertyRow instanceof Descriptor) {
 			Descriptor descriptor = ((Descriptor) propertyRow);
 			if (descriptor.isRMFSpecific()) {
 				return descriptor.getAttributeValue();
 			} else {
 				Object target = contentProvider.getElement();
				PropertyValueWrapper wrapper = (PropertyValueWrapper) descriptor
 						.getItemPropertyDescriptor().getPropertyValue(target);
 				Object content = descriptor.getContent(col);
				return wrapper == null ? null : wrapper
						.getEditableValue(content);
 			}
 		} else {
 			return propertyRow.getContent(col);
 		}
 	}
 
 	@Override
 	protected AttributeValue getAttributeValue(int row, int col) {
 		PropertyRow propertyRow = contentProvider.getRowContent(row);
 		if (propertyRow instanceof Descriptor) {
 			return ((Descriptor) propertyRow).getAttributeValue();
 		}
 		return null;
 	}
 	
 	@Override
 	public boolean canEdit(int row, int col) {
 
 		AttributeValue av = getAttributeValue(row, col);
 
 		// If we have an attribute value, use default method in extended class
 		if (av != null) {
 
 			// Consult the presentation
 			ProrPresentationConfiguration config = ConfigurationUtil
 					.getPresentationConfiguration(av);
 			if (config != null) {
 				ItemProviderAdapter ip = ProrUtil.getItemProvider(
 						adapterFactory, config);
 				if (ip instanceof PresentationEditorInterface) {
 					return ((PresentationEditorInterface) ip).canEdit();
 				}
 			}
 
 		} else {
 
 			// Else we have an EMF specific attribute use the corresponding
 			// method of the item property descriptor
 			PropertyRow propertyRow = contentProvider.getRowContent(row);
 			if (propertyRow instanceof Descriptor) {
 				return ((Descriptor) propertyRow).getItemPropertyDescriptor()
 						.canSetProperty(contentProvider.getElement());
 			}
 		}
 
 		return true;
 
 	}
 
 	@Override
 	public CellEditor getCellEditor(int row, int col, Object hint) {
 
 		// Get the correct celleditor
 		PropertyRow propertyRow = contentProvider.getRowContent(row);
 
 		SpecElementWithAttributes specElement;
 		if (contentProvider.getElement() instanceof SpecHierarchy) {
 			specElement = ((SpecHierarchy) contentProvider.getElement())
 					.getObject();
 		} else if (contentProvider.getElement() instanceof SpecElementWithAttributes) {
 			specElement = (SpecElementWithAttributes) contentProvider
 					.getElement();
 		} else {
 			specElement = null;
 		}
 
 		AttributeValue attrValue = getAttributeValue(row, col);
 
 		CellEditor cellEditor = null;
 
 		// If the attribute is a reqif attribute (an attribute value exists),
 		// when try to get the presentation service
 		if (attrValue != null) {
 
 			// Ask Presentation
 			ProrPresentationConfiguration config = ConfigurationUtil
 					.getPresentationConfiguration(attrValue);
 			if (config != null) {
 				ItemProviderAdapter ip = ProrUtil.getItemProvider(
 						adapterFactory, config);
 				if (ip instanceof PresentationEditorInterface) {
 					cellEditor = ((PresentationEditorInterface) ip)
 							.getCellEditor(agileGrid, editingDomain, attrValue,
 									specElement, getAffectedElement(row, col));
 				}
 			}
 
 			// See whether there is a default editor
 			if (cellEditor == null) {
 				cellEditor = PresentationServiceManager.getDefaultCellEditor(
 						agileGrid, editingDomain, adapterFactory, attrValue,
 						specElement, getAffectedElement(row, col));
 			}
 
 			if (cellEditor == null)
 				cellEditor = getDefaultCellEditor(attrValue,
 						contentProvider.getElement(),
 						getAffectedElement(row, col));
 
 		} else { // If the attribute is an EMF attribute (no attribute value
 					// exists) return a default celleditor
 
 			if (propertyRow instanceof Descriptor) {
 				Descriptor rowDescriptor = (Descriptor) propertyRow;
 
 				final IItemPropertyDescriptor descriptor = rowDescriptor
 						.getItemPropertyDescriptor();
 			
 				String categoryName = descriptor.getCategory(contentProvider
 						.getElement());
 				Object selectedElement = contentProvider.getElement();
 			
 				if (categoryName != null
 						&& categoryName
 								.equals(ProrPropertyContentProvider.SPEC_OBJECT_NAME)) {
 				if (this.contentProvider.getElement() instanceof SpecHierarchy)
 					selectedElement = ((SpecHierarchy) this.contentProvider.getElement())
 							.getObject();
 			}
 			
 			cellEditor = getNonAttributeCellEditor(selectedElement, descriptor);
 			}
 		}
 		return cellEditor;
 		
 	}
 
 	/**
 	 * 
 	 * This returns the cell editor that will be used to edit the value of this
 	 * property. This default implementation determines the type of cell editor
 	 * from the nature of the structural feature.
 	 * 
 	 */
 	CellEditor getNonAttributeCellEditor(final Object object,
 			final IItemPropertyDescriptor itemPropertyDescriptor) {
 
 		if (!itemPropertyDescriptor.canSetProperty(object)) {
 			return null;
 		}
 
 		CellEditor result = null;
 
 		Object genericFeature = itemPropertyDescriptor.getFeature(object);
 		if (genericFeature instanceof EReference[]) {
 
 			result = null;
 
 			result = new ExtendedAgileComboBoxCellEditor(agileGrid,
 					editingDomain, new ArrayList<Object>(
 							itemPropertyDescriptor.getChoiceOfValues(object)),
 					itemPropertyDescriptor, object,
 					itemPropertyDescriptor.isSortChoices(object));
 
 		} else if (genericFeature instanceof EStructuralFeature) {
 
 			final EStructuralFeature feature = (EStructuralFeature) genericFeature;
 			final EClassifier eType = feature.getEType();
 			final Collection<?> choiceOfValues = itemPropertyDescriptor
 					.getChoiceOfValues(object);
 
 			if (choiceOfValues != null) {
 
 				if (itemPropertyDescriptor.isMany(object)) {
 					boolean valid = true;
 					for (Object choice : choiceOfValues) {
 						if (!eType.isInstance(choice)) {
 							valid = false;
 							break;
 						}
 					}
 
 					if (valid) {
 
 						final ILabelProvider editLabelProvider = getLabelProvider(
 								itemPropertyDescriptor, object);
 
 						result = new ExtendedAgileDialogCellEditor(agileGrid,
 								editingDomain, itemPropertyDescriptor, object) {
 
 							@Override
 							protected Object openDialogBox(
 									Control cellEditorWindow) {
 
 								FeatureEditorDialog dialog = new FeatureEditorDialog(
 										cellEditorWindow.getShell(),
 										editLabelProvider,
 										object,
 										feature.getEType(),
 										(List<?>) doGetValue(),
 										getDisplayName(itemPropertyDescriptor,
 												object), new ArrayList<Object>(
 												choiceOfValues), false,
 										itemPropertyDescriptor
 												.isSortChoices(object), feature
 												.isUnique());
 								super.openDialogBox(cellEditorWindow);
 								dialog.open();
 								return dialog.getResult();
 
 							}
 
 						};
 
 					}
 
 				}
 
 				if (result == null) {
 					result = new ExtendedAgileComboBoxCellEditor(agileGrid,
 							editingDomain, new ArrayList<Object>(
 									itemPropertyDescriptor
 											.getChoiceOfValues(object)),
 							itemPropertyDescriptor, object,
 							itemPropertyDescriptor.isSortChoices(object));
 				}
 
 			}
 
 			else if (eType instanceof EDataType) {
 
 				final EDataType eDataType = (EDataType) eType;
 
 				if (eDataType.isSerializable()) {
 					if (itemPropertyDescriptor.isMany(object)) {
 						final ILabelProvider editLabelProvider = getLabelProvider(
 								itemPropertyDescriptor, object);
 
 						result = new ExtendedAgileDialogCellEditor(agileGrid,
 								editingDomain, itemPropertyDescriptor, object) {
 							@Override
 							protected Object openDialogBox(
 									Control cellEditorWindow) {
 
 								FeatureEditorDialog dialog = new FeatureEditorDialog(
 										cellEditorWindow.getShell(),
 										editLabelProvider, object,
 										feature.getEType(),
 										(List<?>) doGetValue(),
 										getDisplayName(itemPropertyDescriptor,
 												object), null,
 										itemPropertyDescriptor
 												.isMultiLine(object), false,
 										feature.isUnique());
 								super.openDialogBox(cellEditorWindow);
 								dialog.open();
 								return dialog.getResult();
 
 							}
 						};
 					} else if (eDataType.getInstanceClass() == Boolean.class
 							|| eDataType.getInstanceClass() == Boolean.TYPE) {
 						result = new ExtendedAgileComboBoxCellEditor(agileGrid,
 								editingDomain, Arrays.asList(new Object[] {
 										Boolean.FALSE, Boolean.TRUE }),
 								itemPropertyDescriptor, object,
 								itemPropertyDescriptor.isSortChoices(object));
 					} else {
 
 						if (itemPropertyDescriptor.isMultiLine(object)) {
 
 							result = new ExtendedAgileDialogCellEditor(
 									agileGrid, editingDomain,
 									itemPropertyDescriptor, object) {
 
 								// TODO: not working yet ...
 
 								protected EDataTypeValueHandler valueHandler = new EDataTypeValueHandler(
 										eDataType);
 
 								@Override
 								protected Object openDialogBox(
 										Control cellEditorWindow) {
 									InputDialog dialog = new MultiLineInputDialog(
 											cellEditorWindow.getShell(),
 											EMFEditUIPlugin.INSTANCE
 													.getString(
 															"_UI_FeatureEditorDialog_title",
 															new Object[] {
 																	getDisplayName(
 																			itemPropertyDescriptor,
 																			object),
 																	getLabelProvider(
 																			itemPropertyDescriptor,
 																			object)
 																			.getText(
 																					object) }),
 											EMFEditUIPlugin.INSTANCE
 													.getString("_UI_MultiLineInputDialog_message"),
 											valueHandler.toString(getValue()),
 											valueHandler);
 									return dialog.open() == Window.OK ? valueHandler
 											.toValue(dialog.getValue()) : null;
 								}
 							};
 
 						} else {
 
 							result = new EDataTypeAgileCellEditor(agileGrid,
 									editingDomain, itemPropertyDescriptor,
 									object, eDataType);
 
 						}
 
 					}
 				}
 			}
 
 		}
 
 		return result;
 
 	}
 	
 //	/**
 //	 * This method undos the last command, wrapps it to change the affected
 //	 * objects, and executes it again.
 //	 * <p>
 //	 * This is a workaround, as we modify properties via
 //	 * {@link IItemPropertyDescriptor#setPropertyValue(Object, Object)}. That
 //	 * method builds the appropriate command and executes it. However, the
 //	 * affected objects are incorrect, as this is typically the
 //	 * {@link SpecElementWithAttributes} (or {@link SpecHierarchy}), but the
 //	 * property belongs to {@link AttributeValue}, which is therefore reported
 //	 * as the affected element.
 //	 */
 //	private void fixAffectedObjectsOfLastcommand() {
 //		Command lastCmd = editingDomain.getCommandStack().getMostRecentCommand();
 //		if (lastCmd == null) return;
 //		editingDomain.getCommandStack().undo();
 //		CommandWrapper wrappedCmd = new CommandWrapper(lastCmd) {
 //			public java.util.Collection<?> getAffectedObjects() {
 //				List<Object> list = new ArrayList<Object>();
 //				list.add(contentProvider.getIdentifiable());
 //				return list;
 //			}
 //		};
 //		editingDomain.getCommandStack().execute(wrappedCmd);
 //	}
 	
 	@Override
 	public Object getAffectedElement(int row, int col) {
 		if (this.contentProvider != null)
 				return this.contentProvider.getElement();
 		return null;
 	}
 
 	public static ILabelProvider getLabelProvider(
 			IItemPropertyDescriptor itemPropertyDescriptor, Object object) {
 		final IItemLabelProvider itemLabelProvider = itemPropertyDescriptor
 				.getLabelProvider(object);
 		return new LabelProvider() {
 			@Override
 			public String getText(Object object) {
 				return itemLabelProvider.getText(object);
 			}
 
 			@Override
 			public Image getImage(Object object) {
 				return ExtendedImageRegistry.getInstance().getImage(
 						itemLabelProvider.getImage(object));
 			}
 		};
 	}
 
 	public static String getDisplayName(
 			IItemPropertyDescriptor itemPropertyDescriptor, Object object) {
 		return itemPropertyDescriptor.getDisplayName(object);
 	}
 
 	/**
 	 * A delegate for handling validation and conversion for data type values.
 	 */
 	protected static class EDataTypeValueHandler implements
 			ICellEditorValidator, IInputValidator {
 		protected EDataType eDataType;
 
 		public EDataTypeValueHandler(EDataType eDataType) {
 			this.eDataType = eDataType;
 		}
 
 		public String isValid(Object object) {
 			Object value;
 			try {
 				value = eDataType.getEPackage().getEFactoryInstance()
 						.createFromString(eDataType, (String) object);
 			} catch (Exception exception) {
 				String message = exception.getClass().getName();
 				int index = message.lastIndexOf('.');
 				if (index >= 0) {
 					message = message.substring(index + 1);
 				}
 				if (exception.getLocalizedMessage() != null) {
 					message = message + ": " + exception.getLocalizedMessage();
 				}
 				return message;
 			}
 			Diagnostic diagnostic = Diagnostician.INSTANCE.validate(eDataType,
 					value);
 			if (diagnostic.getSeverity() == Diagnostic.OK) {
 				return null;
 			} else {
 				return (diagnostic.getChildren().get(0)).getMessage()
 						.replaceAll("'", "''").replaceAll("\\{", "'{'"); // }}
 			}
 		}
 
 		public String isValid(String text) {
 			return isValid((Object) text);
 		}
 
 		public Object toValue(String string) {
 			return EcoreUtil.createFromString(eDataType, string);
 		}
 
 		public String toString(Object value) {
 			String result = EcoreUtil.convertToString(eDataType, value);
 			return result == null ? "" : result;
 		}
 
 	}
 
 	private static class MultiLineInputDialog extends InputDialog {
 
 		public MultiLineInputDialog(Shell parentShell, String title,
 				String message, String initialValue, IInputValidator validator) {
 			super(parentShell, title, message, initialValue, validator);
 			setShellStyle(getShellStyle() | SWT.RESIZE);
 		}
 
 	}
 
 }
