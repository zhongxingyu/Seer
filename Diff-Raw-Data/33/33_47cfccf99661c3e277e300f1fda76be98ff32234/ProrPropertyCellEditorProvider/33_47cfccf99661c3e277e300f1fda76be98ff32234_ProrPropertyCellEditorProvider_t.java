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
 package org.eclipse.rmf.pror.reqif10.editor.propertiesview;
 
 import java.util.ArrayList;
 import java.util.Collection;
 
 import org.agilemore.agilegrid.AgileGrid;
 import org.agilemore.agilegrid.CellEditor;
 import org.agilemore.agilegrid.editors.ComboBoxCellEditor;
 import org.agilemore.agilegrid.editors.TextCellEditor;
 import org.eclipse.emf.common.command.Command;
 import org.eclipse.emf.common.notify.AdapterFactory;
 import org.eclipse.emf.edit.command.SetCommand;
 import org.eclipse.emf.edit.domain.EditingDomain;
 import org.eclipse.emf.edit.provider.IItemPropertyDescriptor;
 import org.eclipse.rmf.pror.reqif10.configuration.ProrPresentationConfiguration;
 import org.eclipse.rmf.pror.reqif10.editor.agilegrid.AbstractProrCellEditorProvider;
 import org.eclipse.rmf.pror.reqif10.presentation.service.PresentationPluginManager;
 import org.eclipse.rmf.pror.reqif10.presentation.service.PresentationService;
 import org.eclipse.rmf.pror.reqif10.util.ConfigurationUtil;
 import org.eclipse.rmf.reqif10.AttributeValue;
import org.eclipse.rmf.reqif10.Identifiable;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.custom.CCombo;
 
 public class ProrPropertyCellEditorProvider extends AbstractProrCellEditorProvider {
 
 	private final ProrPropertyContentProvider contentProvider;
 	
 	private EditingDomain editingDomain;
 
 	public ProrPropertyCellEditorProvider(AgileGrid agileGrid, AdapterFactory adapterFactory, EditingDomain editingDomain) {
 		super(agileGrid, adapterFactory, editingDomain);
 		this.editingDomain = editingDomain;
 		contentProvider = (ProrPropertyContentProvider) agileGrid.getContentProvider();
 	}
 
 	@Override
 	protected AttributeValue getAttributeValue(int row, int col) {
 		return contentProvider.getAttributeValue(row);
 	}
 	
 	@Override
 	public boolean canEdit(int row, int col) {
 
 		ProrPresentationConfiguration config = ConfigurationUtil
 				.getPresentationConfig(this.contentProvider
 				.getAttributeValue(row), editingDomain);
 
 		if (config != null) {
 			PresentationService service = PresentationPluginManager.getPresentationService(config);
 			if (service != null)
 				return service.canEdit();
 		} else {
 			IItemPropertyDescriptor descriptor = this.contentProvider
 					.getItemPropertyDescriptor(row);
 			if (descriptor != null)
 				return descriptor.canSetProperty(this.contentProvider
 						.getSpecElement());
 		}
 
 		return false;
 
 	}
 
 	@Override
 	public CellEditor getCellEditor(int row, int col, Object hint) {
 
		// Get the correct celleditor

 		AttributeValue attrValue = getAttributeValue(row, col);
 
 		CellEditor cellEditor = null;
 
		// If the attribute is a reqif attribute (an attribute value exists),
		// when try to get the presentation service
 		if (attrValue != null) {
 
 			ProrPresentationConfiguration config = ConfigurationUtil
 					.getPresentationConfig(attrValue, editingDomain);
 			if (config != null) {
 				PresentationService service = PresentationPluginManager.getPresentationService(config);
 				if (service != null)
 					cellEditor = service.getCellEditor(agileGrid,
 						editingDomain, attrValue);
 			}
 
 			if (cellEditor == null)
 				cellEditor = getDefaultCellEditor(attrValue);
 
		} else { // If the attribute is an EMF attribute (no attribute value
					// exists) return a default celleditor

 			final IItemPropertyDescriptor descriptor = this.contentProvider
 					.getItemPropertyDescriptor(row);
 			
			Identifiable selectedSpecElement = this.contentProvider.getSpecElement();
			
 			Collection<?> collection = descriptor
					.getChoiceOfValues(selectedSpecElement);
 
 			if (collection != null) { // Multivalue --> combobox celleditor
 				final ArrayList<String> strList = new ArrayList<String>();
 				final ArrayList<Object> objList = new ArrayList<Object>();
				// Add a null entry. This is particularly fatal when no entries
				// exist, i.e. the user opens a dropdown that has no entries.
				strList.add("");
				objList.add(null);
 				for (Object o : collection) {
 					if (o != null) {
 						String str = descriptor.getLabelProvider(
								selectedSpecElement)
 								.getText(o);
 						objList.add(o);
 						strList.add(str);
 					}
 				}
 				
 				cellEditor = new ComboBoxCellEditor(agileGrid,
 						strList.toArray(new String[strList.size()]), SWT.NONE) {
 
 					@Override
 					protected Object doGetValue() {
 						CCombo combo = (CCombo) getControl();
 						int selectionIndex = combo.getSelectionIndex();
 						sendCommand(objList.get(selectionIndex),
 								descriptor);
 						return strList.get(selectionIndex);
 					}
 
 				};
 	
 			} else { // Singlevalue --> text celleditor
 				
 				cellEditor = new TextCellEditor(agileGrid) {
 
 					@Override
 					protected Object doGetValue() {
 						Object value = super.doGetValue();
 						sendCommand(value, descriptor);
 						return value;
 					}
 
 				};
 
 			}
 
 		}
 
 		return cellEditor;
 
 	}
 
 	/**
 	 * 
 	 * Sends a command to the command stack in order to change the new attribute
 	 * value of the {@link IItemPropertyDescriptor}.
 	 * 
 	 * @param newValue
 	 *            the new attribute value
 	 * @param descriptor
 	 *            the corresponding {@link IItemPropertyDescriptor}
 	 */
 	public void sendCommand(Object newValue, IItemPropertyDescriptor descriptor) {
 		Command cmd = SetCommand.create(editingDomain,
 				((ProrPropertyContentProvider) agileGrid.getContentProvider())
 						.getSpecElement(), descriptor
 						.getFeature(contentProvider.getSpecElement()), newValue);
 		editingDomain.getCommandStack().execute(cmd);
 	}
 
 }
