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
 
 import org.agilemore.agilegrid.AgileGrid;
 import org.agilemore.agilegrid.SWTX;
 import org.eclipse.emf.common.notify.AdapterFactory;
 import org.eclipse.emf.edit.domain.EditingDomain;
 import org.eclipse.jface.viewers.ISelection;
 import org.eclipse.jface.viewers.Viewer;
 import org.eclipse.rmf.reqif10.Identifiable;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.graphics.Cursor;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.swt.widgets.Display;
 
 /**
  * 
  * The agile grid viewer for the properties view.
  * 
  * @author Lukas Ladenberger
  * 
  */
 public class ProrPropertyViewer extends Viewer {
 
 	private final AgileGrid agileGrid;
 	
 	private Identifiable currentSelectedSpecElement;
 	
 	private final ProrPropertyContentProvider contentProvider;
 
 	public ProrPropertyViewer(Composite parent, EditingDomain editingDomain, AdapterFactory adapterFactory) {
 		// Create Agile Grid
		agileGrid = new AgileGrid(parent, SWTX.AUTO_SCROLL
 				| SWTX.FILL_WITH_DUMMYCOL | SWT.FLAT);
 		agileGrid.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_WHITE));
 		this.contentProvider = new ProrPropertyContentProvider(editingDomain);
 		agileGrid.setContentProvider(this.contentProvider);
 		agileGrid.setCellRendererProvider(new ProrPropertyCellRendererProvider(
 				agileGrid, adapterFactory, editingDomain));
 		agileGrid.setLayoutAdvisor(new ProrPropertyLayoutAdvisor(agileGrid));
 		agileGrid.setCellEditorProvider(new ProrPropertyCellEditorProvider(
 				agileGrid, adapterFactory, editingDomain));
 		agileGrid.setRowResizeCursor(new Cursor(agileGrid.getDisplay(),
 				SWT.CURSOR_ARROW));
 	}
 	
 	@Override
 	public Control getControl() {
 		return agileGrid;
 	}
 
 	@Override
 	public Object getInput() {
 		return currentSelectedSpecElement;
 	}
 
 	@Override
 	public ISelection getSelection() {
 		return null;
 	}
 
 	@Override
 	public void refresh() {
 	}
 
 	public void update() {
 		this.contentProvider.setContent(this.currentSelectedSpecElement);
 		agileGrid.redraw();
 	}
 
 	/**
 	 * If a new selection was detected, set the corresponding input to the
 	 * {@link ProrPropertyContentProvider}.
 	 */
 	@Override
 	public void setInput(Object specElement) {
 		if (specElement instanceof Identifiable) {
 			this.currentSelectedSpecElement = (Identifiable) specElement;
 		} else {
 			this.currentSelectedSpecElement = null;
 		}
 		update();
 	}
 	
     @Override
     public void setSelection(ISelection selection, boolean reveal) {
         //Do nothing by default
     }
 
 }
