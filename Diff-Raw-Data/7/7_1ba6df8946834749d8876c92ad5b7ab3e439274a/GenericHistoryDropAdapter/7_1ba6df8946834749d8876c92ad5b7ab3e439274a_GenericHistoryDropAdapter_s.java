 /*******************************************************************************
  * Copyright (c) 2006 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *     IBM Corporation - initial API and implementation
  *******************************************************************************/
 
 package org.eclipse.team.internal.ui.history;
 
import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.swt.dnd.*;
 import org.eclipse.ui.part.PluginDropAdapter;
 import org.eclipse.ui.part.ResourceTransfer;
 
 public class GenericHistoryDropAdapter extends PluginDropAdapter {
 
 	private GenericHistoryView view;
 
 	public GenericHistoryDropAdapter(GenericHistoryView view) {
 		super(null);
 		this.view = view;
 	}
 
 	/*
 	 * Override dragOver to slam the detail to DROP_LINK, as we do not
 	 * want to really execute a DROP_MOVE, although we want to respond
 	 * to it.
 	 */
 	public void dragOver(DropTargetEvent event) {
 		if ((event.operations & DND.DROP_LINK) == DND.DROP_LINK) {
 			event.detail = DND.DROP_LINK;
 		}
 		super.dragOver(event);
 	}
 
 	/*
 	 * Override drop to slam the detail to DROP_LINK, as we do not
 	 * want to really execute a DROP_MOVE, although we want to respond
 	 * to it.
 	 */
 	public void drop(DropTargetEvent event) {
 		super.drop(event);
 		event.detail = DND.DROP_LINK;
 	}
 
 	public boolean performDrop(Object data) {
 		if (data == null)
 			return false;
 		if (data instanceof IResource[]) {
 			IResource[] sources = (IResource[]) data;
 			if (sources.length == 0)
 				return false;
 			IResource resource = sources[0];
			if (!(resource instanceof IFile))
				return false;

 			view.itemDropped(resource, true);
 
 			return true;
 		}
 		return false;
 	}
 
 	public boolean validateDrop(Object target, int operation, TransferData transferType) {
 		if (transferType != null && ResourceTransfer.getInstance().isSupportedType(transferType)) {
 			return true;
 		}
 		
 		return super.validateDrop(target, operation, transferType);
 	}
 
 	protected Object getCurrentTarget() {
 		return view;
 	}
 }
