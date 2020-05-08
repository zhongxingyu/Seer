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
 package org.eclipse.rmf.pror.reqif10.editor.agilegrid;
 
 import org.agilemore.agilegrid.AgileGrid;
 import org.agilemore.agilegrid.CellEditor;
 import org.eclipse.emf.common.notify.AdapterFactory;
 import org.eclipse.emf.edit.domain.EditingDomain;
 import org.eclipse.rmf.pror.reqif10.presentation.service.PresentationManager;
 import org.eclipse.rmf.pror.reqif10.presentation.service.PresentationService;
 import org.eclipse.rmf.reqif10.AttributeValue;
 import org.eclipse.rmf.reqif10.Identifiable;
 
 public class ProrCellEditorProvider extends AbstractProrCellEditorProvider {
 
 	private final ProrAgileGridContentProvider contentProvider;
 	
 	public ProrCellEditorProvider(AgileGrid agileGrid,
 			EditingDomain editingDomain, AdapterFactory adapterFactory) {
 		super(agileGrid, adapterFactory, editingDomain);
 		contentProvider = (ProrAgileGridContentProvider) agileGrid
 				.getContentProvider();
 	}
 
 	@Override
 	protected AttributeValue getAttributeValue(int row, int col) {
 		return contentProvider.getValueForColumn(contentProvider.getProrRow(row).getSpecElement(), col);
 	}
 
 	@Override
 	public Identifiable getAffectedElement(int row, int col) {
 		ProrAgileGridContentProvider provider = (ProrAgileGridContentProvider) getAgileGrid()
 				.getContentProvider();
		return (Identifiable) provider.getProrRow(row).element;
 	}
 
 	@Override
 	public CellEditor getCellEditor(int row, int col, Object hint) {
 		AttributeValue attrValue = getAttributeValue(row, col);
 		PresentationService service = PresentationManager
 				.getPresentationService(attrValue, editingDomain);
 		if (service != null) {
 			CellEditor cellEditor = service.getCellEditor(agileGrid,
 					editingDomain, attrValue);
 			if (cellEditor != null) {
 				return cellEditor;
 			}
 		}
 		return getDefaultCellEditor(attrValue, getAffectedElement(row, col));
 	}
 
 	@Override
 	public boolean canEdit(int row, int col) {
 		AttributeValue attrValue = getAttributeValue(row, col);
 		if (attrValue == null) {
 			return false;
 		}
 		PresentationService service = PresentationManager
 				.getPresentationService(attrValue, editingDomain);
 		return service == null ? true : service.canEdit();
 	}
 
 }
