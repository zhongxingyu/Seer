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
 
 import org.agilemore.agilegrid.AgileGrid;
 import org.agilemore.agilegrid.DefaultCellRendererProvider;
 import org.agilemore.agilegrid.ICellRenderer;
 import org.agilemore.agilegrid.SWTResourceManager;
 import org.agilemore.agilegrid.renderers.TextCellRenderer;
 import org.eclipse.emf.common.notify.AdapterFactory;
 import org.eclipse.emf.edit.provider.IItemLabelProvider;
 import org.eclipse.emf.edit.ui.provider.ExtendedImageRegistry;
 import org.eclipse.rmf.reqif10.AttributeValue;
 import org.eclipse.rmf.reqif10.pror.editor.propertiesview.ProrPropertyContentProvider.ItemCategory;
 import org.eclipse.rmf.reqif10.pror.editor.propertiesview.ProrPropertyContentProvider.SortedItemPropertyDescriptor;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.graphics.GC;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.swt.graphics.Rectangle;
 
 public class ProrPropertyCellRendererProvider extends DefaultCellRendererProvider {
 
 	// The default cell renderer for attribute/value rows
 	private final ProrPropertyCellRenderer attributeCellRenderer;
 	
 	// The cell renderer for category rows
 	private final TextCellRenderer categoryCellRenderer;
 
	private ProrPropertyContentProvider contentProvider;

 	public ProrPropertyCellRendererProvider(AgileGrid agileGrid,
			AdapterFactory adapterFactory,
			ProrPropertyContentProvider contentProvider) {
 		super(agileGrid);
		this.contentProvider = contentProvider;
 		this.attributeCellRenderer = new ProrPropertyCellRenderer(agileGrid,
 				adapterFactory);
 		this.categoryCellRenderer = new TextCellRenderer(agileGrid, SWT.BOLD);
 		this.categoryCellRenderer.setDefaultBackground(SWTResourceManager
 				.getColor(SWT.COLOR_WIDGET_BACKGROUND));
 	}
 
 	@Override
 	public ICellRenderer getCellRenderer(int row, int col) {
 
 		// The obj could be an ItemCategory or SortenItemPropertyDescriptor
 		// instance
 		Object obj = contentProvider.getRowContent(row);
 
 		// If we have an ItemCategory at this row, return the category cell
 		// renderer
 		if (col <= 1 && obj instanceof ItemCategory) {
 			return this.categoryCellRenderer;
 		} else if (col == 1 && obj instanceof SortedItemPropertyDescriptor) {
 
 			// If we have a SortedItemPropertyDescriptor at this row,
 			// return the corresponding default cell renderer for
 			// attribute rows
 			AttributeValue attributeValue = ((ProrPropertyContentProvider) agileGrid
 					.getContentProvider()).getReqIfAttributeValue(row);
 			if (attributeValue != null) {
 				return this.attributeCellRenderer;
 			} else {
 
 				// else if we have a specific EMF attribute return the renderer
 				// for it
 				IItemLabelProvider labelProvider = contentProvider
 						.getItemLabelProvider(row);
 				if (labelProvider != null) {
 
 					Object itemPropertyValue = contentProvider
 							.getItemPropertyValue(row);
 
 					final String text = labelProvider
 							.getText(itemPropertyValue);
 					final Image image = getImageFromObject(labelProvider
 							.getImage(itemPropertyValue));
 
 					return new TextCellRenderer(agileGrid) {
 
 						@Override
 						protected void doDrawCellContent(GC gc, Rectangle rect,
 								int row, int col) {
 
 							// TODO: Check agile grid problem with third column
 							// We render only the second column (emf property
 							// value)
 							if (col == 1) {
 
 								if ((style & ICellRenderer.DRAW_VERTICAL) != 0) {
 									drawVerticalTextImage(gc, rect, text, null,
 											foreground, background);
 								} else {
 									int alignment = getAlignment();
 									drawTextImage(gc, text, alignment, image,
 											alignment, rect.x + 3, rect.y + 2,
 											rect.width - 6, rect.height - 4);
 								}
 
 							}
 
 						}
 
 					};
 
 				}
 				
 			}
 
 		}
 
 		// Else return the agile grid default cell renderer
 		return super.getCellRenderer(row, col);
 
 	}
 	
 	private Image getImageFromObject(Object object) {
 		return ExtendedImageRegistry.getInstance().getImage(object);
 	}
 
 }
