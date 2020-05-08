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
 
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

 import org.agilemore.agilegrid.AgileGrid;
 import org.agilemore.agilegrid.SWTX;
 import org.eclipse.emf.common.notify.AdapterFactory;
 import org.eclipse.emf.edit.domain.EditingDomain;
 import org.eclipse.jface.viewers.ISelection;
 import org.eclipse.jface.viewers.IStructuredSelection;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.graphics.Cursor;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Display;
 
/**
 * This control wraps an actual AgileGrid that displays the properties of the
 * current selection. It is instantiated twice, once for all and ones for those
 * properties relevant to users (via showAllProps).
 * 
 */
 public class ProrPropertyControl extends AgileGrid implements PropertyChangeListener {
 
 	private ProrPropertyContentProvider contentProvider;
 	
 	private Object object;
 
 	public ProrPropertyControl(Composite parent, EditingDomain editingDomain,
 			AdapterFactory adapterFactory, boolean showAllProps) {
 		super(parent, SWT.V_SCROLL | SWT.H_SCROLL | SWTX.FILL_WITH_LASTCOL
 				| SWT.MULTI | SWT.DOUBLE_BUFFERED);
 		setBackground(Display.getDefault().getSystemColor(SWT.COLOR_WHITE));
 		this.contentProvider = new ProrPropertyContentProvider(editingDomain, showAllProps);
 		setContentProvider(this.contentProvider);
 		setCellRendererProvider(new ProrPropertyCellRendererProvider(this,
				adapterFactory, contentProvider));
 		setLayoutAdvisor(new ProrPropertyLayoutAdvisor(this));
 		setCellEditorProvider(new ProrPropertyCellEditorProvider(this,
 				adapterFactory, editingDomain, this.contentProvider));
 		setRowResizeCursor(new Cursor(this.getDisplay(), SWT.CURSOR_ARROW));
 				
 		// listen to property changes in content
 		// Fix of 378041
 		contentProvider.addPropertyChangeListener(this);
 	}
 	
 	void setSelection(ISelection selection) {
 		if (selection instanceof IStructuredSelection) {
 			IStructuredSelection sel = (IStructuredSelection) selection;
 			if (sel.size() == 1) {
 				Object obj = sel.getFirstElement();
 				contentProvider.setContent(obj);
 				object = obj;
 
 				redraw();
 				return;
 			}
 		}
 		contentProvider.setContent(null);
 		redraw();
 	}
 
 	// Fix of 378041
 	// listen to property changes in content
 	// reload Content and redraw on change event
 	public void propertyChange(PropertyChangeEvent event) {
 		if (event.getPropertyName().equals("")) {
 			contentProvider.setContent(object);
 			super.redraw();
 		}
 	}
 
 }
