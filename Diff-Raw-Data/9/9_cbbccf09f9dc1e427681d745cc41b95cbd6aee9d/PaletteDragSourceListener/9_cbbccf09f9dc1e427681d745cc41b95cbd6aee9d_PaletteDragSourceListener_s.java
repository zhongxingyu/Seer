 /*******************************************************************************
  * Copyright (c) 2007 Exadel, Inc. and Red Hat, Inc.
  * Distributed under license by Red Hat, Inc. All rights reserved.
  * This program is made available under the terms of the
  * Eclipse Public License v1.0 which accompanies this distribution,
  * and is available at http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     Exadel, Inc. and Red Hat, Inc. - initial API and implementation
  ******************************************************************************/ 
 package org.jboss.tools.jst.web.ui.palette;
 
 import java.util.*;
 
 import org.eclipse.gef.EditPartViewer;
 import org.eclipse.gef.internal.ui.palette.editparts.*;
 import org.eclipse.gef.ui.palette.editparts.PaletteEditPart;
 import org.eclipse.swt.dnd.*;
 import org.eclipse.swt.widgets.Display;
 
 import org.jboss.tools.common.meta.action.XActionInvoker;
 import org.jboss.tools.common.model.XModelObject;
 import org.jboss.tools.common.model.XModelTransferBuffer;
 import org.jboss.tools.common.model.plugin.ModelPlugin;
 import org.jboss.tools.common.model.ui.views.palette.PaletteInsertHelper;
 import org.jboss.tools.jst.jsp.jspeditor.dnd.JSPPaletteInsertHelper;
 import org.jboss.tools.jst.web.ui.WebUiPlugin;
 import org.jboss.tools.jst.web.ui.palette.model.PaletteItem;
 
 public class PaletteDragSourceListener extends DragSourceAdapter {
 	EditPartViewer viewer;
 	boolean isDragging = false;
 
 	public PaletteDragSourceListener(EditPartViewer viewer) {
 		this.viewer = viewer;
 	}
 
 	boolean isDragging() {
 		return isDragging;
 	}
 
 	public void dragStart(DragSourceEvent event) {
 			XModelTransferBuffer.getInstance().enable();
 			List list = ((PaletteViewer)viewer).getSelectedEditParts();
 			XModelObject object = (list.size() == 0) ? null : getObject(list.get(0));
 			if(object == null) {
 				event.doit = false;
 				return;
 			}
 			Properties p = new Properties();
 			p.setProperty("isDrag", "true"); //$NON-NLS-1$ //$NON-NLS-2$
 			XActionInvoker.invoke("CopyActions.Copy", object, p); //$NON-NLS-1$
 			isDragging = true;
 	}
 	public void dragSetData(DragSourceEvent event) {
 		if (TextTransfer.getInstance().isSupportedType(event.dataType)) {
 			List list = ((PaletteViewer)viewer).getSelectedEditParts();
 			XModelObject object = (list.size() == 0) ? null : getObject(list.get(0));
 			if(object != null) {
 				String[] d = new String[2];
 				d[0] = object.getAttributeValue("start text"); //$NON-NLS-1$
 				if(d[0] == null) d[0] = ""; //$NON-NLS-1$
 				d[1] = object.getAttributeValue("end text"); //$NON-NLS-1$
 				if(d[1] == null) d[1] = ""; //$NON-NLS-1$
 				
 				String defaultPrefix = object.getAttributeValue("default prefix"); //$NON-NLS-1$
 				String tag = object.getAttributeValue("name"); //$NON-NLS-1$
 				if(defaultPrefix == null) {
 					defaultPrefix = object.getParent().getAttributeValue("default prefix"); //$NON-NLS-1$
 				}
 				if(defaultPrefix != null && tag != null) {
 					JSPPaletteInsertHelper.applyPrefix(d, "", tag, "xxx", defaultPrefix); //$NON-NLS-1$ //$NON-NLS-2$
 				}
 				int i = d[0].indexOf('|');
 				if(i >= 0) d[0] = d[0].substring(0, i) + d[0].substring(i + 1);
 				event.data = d[0] + d[1];
 			} else {			
 				event.data = "data"; //$NON-NLS-1$
 			}
 		} else {
 ///			event.data = new String[] {"model object"};
 			event.data = "model object"; //$NON-NLS-1$
 		}
 	}
 
 	public void dragFinished(DragSourceEvent event) { 
 		event.data = null;
 		Display.getDefault().asyncExec(new Runnable() {
 			public void run() {
 				isDragging = false;
 			}
 		});
 	}
 	
 	XModelObject getObject(Object part) {
		if(part instanceof ToolEntryEditPart) {
 			PaletteEditPart entry = (PaletteEditPart)part;
			PaletteItem item = (PaletteItem)entry.getModel();
			return item.getXModelObject();
 		} else {
 			if(ModelPlugin.isDebugEnabled()){
 				// TODO Should be replaced with trace in future
 				WebUiPlugin.getPluginLog().logInfo(part.getClass().getName());
 			}
 		}
 		return null;
 	}
 
 	public void dragFinish(DragSourceEvent event) {
 		XModelTransferBuffer.getInstance().disable();
 	}
 }
