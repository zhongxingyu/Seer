 /*******************************************************************************
  * Copyright (c) 2012 Wind River Systems, Inc. and others. All rights reserved.
  * This program and the accompanying materials are made available under the terms
  * of the Eclipse Public License v1.0 which accompanies this distribution, and is
  * available at http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  * Wind River Systems - initial API and implementation
  *******************************************************************************/
 package org.eclipse.tcf.te.ui.views.handler;
 
 import org.eclipse.core.commands.AbstractHandler;
 import org.eclipse.core.commands.ExecutionEvent;
 import org.eclipse.core.commands.ExecutionException;
 import org.eclipse.jface.viewers.ISelection;
 import org.eclipse.jface.viewers.IStructuredSelection;
 import org.eclipse.jface.viewers.StructuredSelection;
 import org.eclipse.tcf.te.ui.views.ViewsUtil;
 import org.eclipse.tcf.te.ui.views.interfaces.IUIConstants;
 import org.eclipse.ui.IEditorInput;
 import org.eclipse.ui.IWorkbenchPart;
 import org.eclipse.ui.handlers.HandlerUtil;
 import org.eclipse.ui.part.EditorPart;
 
 /**
  * "Show In System Management" command handler implementation.
  */
 public class ShowInSystemManagementHandler extends AbstractHandler {
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.core.commands.IHandler#execute(org.eclipse.core.commands.ExecutionEvent)
 	 */
 	@Override
 	public Object execute(ExecutionEvent event) throws ExecutionException {
 		// Show the view
 		ViewsUtil.show(IUIConstants.ID_EXPLORER);
 
 		// Get the active part
 		IWorkbenchPart part = HandlerUtil.getActivePart(event);
 		// Get the current selection
 		ISelection selection = HandlerUtil.getCurrentSelection(event);
 
		// If the handler is invoked from an editor part, than we do not have a selection.
		// Determine the active editor input and construct a fake selection object from it.
		if ((selection == null || selection.isEmpty()) && part instanceof EditorPart) {
 			IEditorInput input = ((EditorPart)part).getEditorInput();
 			Object element = input != null ? input.getAdapter(Object.class) : null;
 			if (element != null) selection = new StructuredSelection(element);
 		}
 
 		// Pass on to the view
 		if (selection instanceof IStructuredSelection && !selection.isEmpty()) {
 			ViewsUtil.setSelection(IUIConstants.ID_EXPLORER, selection);
 		}
 
 		return null;
 	}
 }
