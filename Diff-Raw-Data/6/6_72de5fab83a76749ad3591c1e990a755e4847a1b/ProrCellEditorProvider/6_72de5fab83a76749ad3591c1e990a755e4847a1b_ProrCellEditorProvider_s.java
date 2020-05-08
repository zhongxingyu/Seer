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
 package org.eclipse.rmf.reqif10.pror.editor.agilegrid;
 
 import org.agilemore.agilegrid.AgileGrid;
 import org.agilemore.agilegrid.CellEditor;
 import org.eclipse.emf.common.notify.AdapterFactory;
 import org.eclipse.emf.edit.domain.EditingDomain;
 import org.eclipse.emf.edit.provider.ItemProviderAdapter;
 import org.eclipse.rmf.reqif10.AttributeValue;
 import org.eclipse.rmf.reqif10.Identifiable;
 import org.eclipse.rmf.reqif10.SpecElementWithAttributes;
 import org.eclipse.rmf.reqif10.pror.configuration.ProrPresentationConfiguration;
 import org.eclipse.rmf.reqif10.pror.editor.agilegrid.ProrRow.ProrRowSpecHierarchy;
 import org.eclipse.rmf.reqif10.pror.editor.presentation.service.PresentationEditorInterface;
 import org.eclipse.rmf.reqif10.pror.editor.presentation.service.PresentationServiceManager;
 import org.eclipse.rmf.reqif10.pror.util.ConfigurationUtil;
 import org.eclipse.rmf.reqif10.pror.util.ProrUtil;
 
 public class ProrCellEditorProvider extends AbstractProrCellEditorProvider {
 
 	private final ProrAgileGridContentProvider contentProvider;
 	private final AgileCellEditorActionHandler agileCellEditorActionHandler;
 	
 	public ProrCellEditorProvider(AgileGrid agileGrid,
 			EditingDomain editingDomain, AdapterFactory adapterFactory, AgileCellEditorActionHandler agileCellEditorActionHandler) {
 		super(agileGrid, adapterFactory, editingDomain);
 		this.contentProvider = (ProrAgileGridContentProvider) agileGrid
 				.getContentProvider();
 		this.agileCellEditorActionHandler = agileCellEditorActionHandler;
 	}
 
 	@Override
 	protected AttributeValue getAttributeValue(int row, int col) {
 		SpecElementWithAttributes specElement = contentProvider.getProrRow(row)
 				.getSpecElement();
 		return contentProvider.getValueForColumn(specElement, row, col);
 	}
 
 	@Override
 	public Identifiable getAffectedElement(int row, int col) {
 		ProrAgileGridContentProvider provider = (ProrAgileGridContentProvider) getAgileGrid()
 				.getContentProvider();
 		ProrRow prorRow = provider.getProrRow(row);
 		if (prorRow instanceof ProrRowSpecHierarchy) {
 			return ((ProrRowSpecHierarchy)prorRow).getSpecHierarchy();
 		}
 		return (Identifiable) prorRow.getSpecElement();
 	}
 
 	@Override
 	public CellEditor getCellEditor(int row, int col, Object hint) {
 		SpecElementWithAttributes specElement = contentProvider.getProrRow(row)
 				.getSpecElement();
 		
 		CellEditor cellEditor = null;		
 		AttributeValue av = getAttributeValue(row, col);
 		
 		// Consult the presentation
 		ProrPresentationConfiguration config = ConfigurationUtil
 				.getPresentationConfiguration(av);
 		if (config != null) {
 			ItemProviderAdapter ip = ProrUtil.getItemProvider(adapterFactory,
 					config);
 			if (ip instanceof PresentationEditorInterface) {
 				cellEditor = ((PresentationEditorInterface) ip).getCellEditor(
 						agileGrid, editingDomain, av, specElement,
 						getAffectedElement(row, col));
 			}
 		}
 		
 		// See whether there is a default editor
 		if (cellEditor == null) {
 			cellEditor = PresentationServiceManager.getDefaultCellEditor(
 					agileGrid, editingDomain, adapterFactory, av, specElement,
 					getAffectedElement(row, col));
 		}
 
 		if (cellEditor == null) {
 			cellEditor = getDefaultCellEditor(av, specElement,
 					getAffectedElement(row, col));
 		}
 
 		if (cellEditor != null)
 			agileCellEditorActionHandler.setActiveCellEditor(cellEditor);
 		
 		return cellEditor;
 		
 	}
 
 	@Override
 	public boolean canEdit(int row, int col) {
 		AttributeValue attrValue = getAttributeValue(row, col);
 		if (attrValue == null) {
 			return false;
 		}
 
 		// Consult the presentation
 		ProrPresentationConfiguration config = ConfigurationUtil
 				.getPresentationConfiguration(attrValue);
 		if (config != null) {
 			ItemProviderAdapter ip = ProrUtil.getItemProvider(adapterFactory,
 					config);
			if (ip instanceof PresentationEditorInterface) {
				return ((PresentationEditorInterface) ip).canEdit();
 			}
 		}
 		return true;
 	}
 
 }
