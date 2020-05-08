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
 
 import java.util.List;
 
 import org.agilemore.agilegrid.AgileGrid;
 import org.agilemore.agilegrid.editors.ComboBoxCellEditor;
 import org.eclipse.emf.common.command.Command;
 import org.eclipse.emf.common.ui.celleditor.ExtendedComboBoxCellEditor;
 import org.eclipse.emf.edit.command.SetCommand;
 import org.eclipse.emf.edit.domain.EditingDomain;
 import org.eclipse.emf.edit.provider.IItemPropertyDescriptor;
 import org.eclipse.rmf.reqif10.pror.editor.util.ProrEditorUtil;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.custom.CCombo;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.events.SelectionListener;
 import org.eclipse.swt.widgets.Control;
 
 public class ExtendedAgileComboBoxCellEditor extends ComboBoxCellEditor implements SelectionListener {
 
 	protected List<?> originalList;
 
 	protected List<?> list;
 
 	protected boolean sorted;
 	
 	protected CCombo combo;
 
 	private EditingDomain editingDomain;
 
 	private Object affectedObject;
 
 	private IItemPropertyDescriptor itemPropertyDescriptor;
 
 	public ExtendedAgileComboBoxCellEditor(AgileGrid agileGrid,
 			EditingDomain editingDomain, List<?> list,
 			IItemPropertyDescriptor itemPropertyDescriptor, Object object,
 			boolean sorted) {
 		super(agileGrid, ExtendedComboBoxCellEditor.createItems(list,
 				ProrPropertyCellEditorProvider.getLabelProvider(
 						itemPropertyDescriptor, object),
 				sorted), SWT.NONE);
 		this.itemPropertyDescriptor = itemPropertyDescriptor;
 		this.editingDomain = editingDomain;
 		this.affectedObject = object;
 		this.list = list;
 		this.sorted = sorted;
 	}
 
 	@Override
 	protected Object doGetValue() {
 
 		// Get the index into the list via this call to super.
 		//
 		int index = ((CCombo) getControl()).getSelectionIndex();
 
 		Object val = index < list.size() && index >= 0 ? list
 				.get(((CCombo) getControl()).getSelectionIndex()) : null;
 
 		Command setCmd = SetCommand.create(editingDomain, affectedObject,
 				itemPropertyDescriptor.getFeature(affectedObject), val);
 		Command affectedObjectCommand = ProrEditorUtil
 				.getAffectedObjectCommand(affectedObject, setCmd);
 		editingDomain.getCommandStack().execute(affectedObjectCommand);
 
 		return val;
 
 	}
 
 	@Override
 	protected void doSetValue(Object value) {
 		if (value == null)
 			return;
 		super.doSetValue(itemPropertyDescriptor.getLabelProvider(value)
 				.getText(value));
 	}
 	
 	public void widgetDefaultSelected(SelectionEvent e) {
		// No action required.
 	}
 
 	// when value is selected from drop down apply value directly
 	public void widgetSelected(SelectionEvent e) {
 		selection = combo.getSelectionIndex();
 	}
 	
 	protected Control createControl(AgileGrid agileGrid) {
 		combo = (CCombo) super.createControl(agileGrid);
 		combo.addSelectionListener(this);
 		return combo;
 	}	
 
 }
