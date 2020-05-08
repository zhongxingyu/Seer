 /*******************************************************************************
  * <copyright>
  *
 * Copyright (c) 2005, 2012 SAP AG.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    SAP AG - initial API, implementation and documentation
 *    Craig Petre - mwenz - Bug 378083 - Focus editor on DnD events from another control
  *
  * </copyright>
  *
  *******************************************************************************/
 package org.eclipse.graphiti.ui.internal.dnd;
 
 import org.eclipse.gef.EditPartViewer;
 import org.eclipse.gef.Request;
 import org.eclipse.gef.commands.Command;
 import org.eclipse.gef.dnd.AbstractTransferDropTargetListener;
 import org.eclipse.gef.requests.CreateRequest;
 import org.eclipse.gef.requests.CreationFactory;
 import org.eclipse.jface.util.LocalSelectionTransfer;
 import org.eclipse.jface.viewers.ISelection;
 import org.eclipse.swt.dnd.DND;
 
 /**
  * @noinstantiate This class is not intended to be instantiated by clients.
  * @noextend This class is not intended to be subclassed by clients.
  */
 public class ObjectsTransferDropTargetListener extends AbstractTransferDropTargetListener {
 
 	public ObjectsTransferDropTargetListener(EditPartViewer viewer) {
 		super(viewer, LocalSelectionTransfer.getTransfer());
 		setEnablementDeterminedByCommand(true);
 	}
 
 	@Override
 	protected void handleDrop() {
 		super.handleDrop();
 		if (getCurrentEvent().detail == DND.DROP_MOVE) {
 			getCurrentEvent().detail = DND.DROP_COPY;
 		}
		// Bug 378083 - set focus to diagram editor after drop
		getViewer().getControl().setFocus();
 	}
 
 	@Override
 	protected void updateTargetRequest() {
 		((CreateRequest) getTargetRequest()).setLocation(getDropLocation());
 	}
 
 	@Override
 	protected Request createTargetRequest() {
 		CreateRequest request = new CreateRequest();
 
 		request.setFactory(new MyCreationFactory());
 		request.setLocation(getDropLocation());
 		return request;
 	}
 
 	@Override
 	protected void handleDragOver() {
 
 		super.handleDragOver();
 
 		Command command = getCommand();
 		if (command != null && command.canExecute())
 			getCurrentEvent().detail = DND.DROP_COPY;
 	}
 
 	private class MyCreationFactory implements CreationFactory {
 
 		public MyCreationFactory() {
 		}
 
 		public Object getNewObject() {
 			return LocalSelectionTransfer.getTransfer().getSelection();
 		}
 
 		public Object getObjectType() {
 			return ISelection.class;
 		}
 	}
 }
