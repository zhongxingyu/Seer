 /*******************************************************************************
  * Copyright (c) 2006 Sybase, Inc. and others.
  *
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     Sybase, Inc. - initial API and implementation
  *******************************************************************************/
 package org.eclipse.jst.pagedesigner.dnd.internal;
 
 import org.eclipse.gef.EditDomain;
 import org.eclipse.gef.palette.ToolEntry;
 import org.eclipse.jst.pagedesigner.commands.PaletteDropInsertCommand;
 import org.eclipse.jst.pagedesigner.commands.SourceViewerCommand;
 import org.eclipse.jst.pagedesigner.editors.pagedesigner.PageDesignerResources;
 import org.eclipse.jst.pagedesigner.editors.palette.impl.PaletteItemDescriptor;
 import org.eclipse.jst.pagedesigner.itemcreation.ItemToolEntry;
 import org.eclipse.swt.custom.StyledText;
 import org.eclipse.swt.events.MouseEvent;
 import org.eclipse.swt.events.MouseListener;
 import org.eclipse.swt.events.MouseMoveListener;
 import org.eclipse.swt.graphics.Point;
 import org.eclipse.wst.sse.ui.StructuredTextEditor;
 import org.eclipse.wst.sse.ui.internal.ExtendedEditorDropTargetAdapter;
 
 /**
  * @author mengbo
  */
 public class DesignerSourceMouseTrackAdapter extends
 		ExtendedEditorDropTargetAdapter implements MouseListener,
 		MouseMoveListener {
 	private int _location;
 
 	private StructuredTextEditor _textEditor;
 
 	private EditDomain _domain;
 
 	public DesignerSourceMouseTrackAdapter(StructuredTextEditor textEditor,
 			EditDomain domain) {
 		super(false);
 		_textEditor = textEditor;
 		_domain = domain;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.swt.events.MouseMoveListener#mouseMove(org.eclipse.swt.events.MouseEvent)
 	 */
 	public void mouseMove(MouseEvent event) {
 		Object object = getPaletteObject();
 		StyledText text = null;
 		if (_textEditor.getTextViewer() != null) {
 			text = _textEditor.getTextViewer().getTextWidget();
 		} else {
 			return;
 		}
 		if (object == null) {
            // set to default cusror
			text.setCursor(null);
 			return;
 		}
 		Point p = new Point(event.x, event.y);
 		p = _textEditor.getTextViewer().getTextWidget().toDisplay(p);
 		SourceViewerDragDropHelper.getInstance().updateCaret(_textEditor, p);
 		_location = text.getCaretOffset();
 		_location = SourceViewerDragDropHelper.getInstance().getValidLocation(
 				_textEditor, _location);
 		_location = SourceViewerDragDropHelper.getInstance().showCaret(
 				_textEditor, _location);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.swt.events.MouseListener#mouseUp(org.eclipse.swt.events.MouseEvent)
 	 */
 	public void mouseUp(MouseEvent event) {
 		if (event.button != 1) {
 			if (_domain.getPaletteViewer() != null) {
 				_domain.getPaletteViewer().setActiveTool(null);
 			}
 			return;
 		}
         SourceViewerCommand command = getCommand(event);
         if (command != null) {
         	command.execute();
         	resetPalette();
         }
         _location = 0;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.gef.palette.PaletteListener#activeToolChanged(org.eclipse.gef.ui.palette.PaletteViewer,
 	 *      org.eclipse.gef.palette.ToolEntry)
 	 */
 	public Object getPaletteObject() {
 		if (_domain.getPaletteViewer() != null) {
 			Object tool = _domain.getPaletteViewer().getActiveTool();
 			if (tool instanceof ItemToolEntry) {
 				Object object = ((ItemToolEntry) tool).getItemDesc();
 				return object;
 			}
 		}
 		return null;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.swt.events.MouseListener#mouseDoubleClick(org.eclipse.swt.events.MouseEvent)
 	 */
 	public void mouseDoubleClick(MouseEvent e) {
        // do nothing
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.swt.events.MouseListener#mouseDown(org.eclipse.swt.events.MouseEvent)
 	 */
 	public void mouseDown(MouseEvent e) {
         // do nothing
 	}
 
 	private PaletteDropInsertCommand getCommand(MouseEvent event) {
 		Object data = getPaletteObject();
 		PaletteDropInsertCommand command = null;
 		if (data instanceof PaletteItemDescriptor) {
 			PaletteItemDescriptor descriptor = (PaletteItemDescriptor) data;
 			// "Create new item"
 			command = new PaletteDropInsertCommand(
 					PageDesignerResources
 							.getInstance()
 							.getString(
 									"DesignerSourceDropTargetListener.InserCommandLabel"),
 					_textEditor, descriptor, _location);
 		}
 		return command;
 	}
 
 	private void resetPalette() {
 		if (_domain.getPaletteViewer() != null) {
 			ToolEntry tool = _domain.getPaletteViewer().getPaletteRoot()
 					.getDefaultEntry();
 			if (tool != null) {
 				_domain.getPaletteViewer().setActiveTool(tool);
 			}
 		}
 	}
 }
