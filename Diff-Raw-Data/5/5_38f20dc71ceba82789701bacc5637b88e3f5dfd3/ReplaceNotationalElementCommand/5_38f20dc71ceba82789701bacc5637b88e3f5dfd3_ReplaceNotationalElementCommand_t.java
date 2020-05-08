 /**
  * Copyright (c) 2006 Eclipse.org
  * 
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    bblajer - initial API and implementation
  */
 package org.eclipse.gmf.runtime.lite.commands;
 
 import org.eclipse.emf.common.command.AbstractCommand;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.EStructuralFeature;
 import org.eclipse.gmf.runtime.notation.Edge;
 import org.eclipse.gmf.runtime.notation.NotationPackage;
 import org.eclipse.gmf.runtime.notation.View;
 
 public class ReplaceNotationalElementCommand extends AbstractCommand {
 	private final CreateNotationalElementCommand createCommand;
 	private final RemoveNotationalElementCommand removeCommand;
 	private final View obsoleteView;
 
 	public ReplaceNotationalElementCommand(View parentView, CreateNotationalElementCommand createCommand, View obsoleteView) {
 		this.createCommand = createCommand;
 		this.obsoleteView = obsoleteView;
 		this.removeCommand = obsoleteView instanceof Edge ? new RemoveNotationalEdgeCommand(parentView, obsoleteView) : new RemoveNotationalElementCommand(parentView, obsoleteView);
 	}
 
 	public boolean canExecute() {
 		return createCommand != null && createCommand.canExecute() && removeCommand != null && removeCommand.canExecute();
 	}
 
 	public boolean canUndo() {
 		return createCommand != null && createCommand.canUndo() && removeCommand != null && removeCommand.canUndo();
 	}
 
 	public void execute() {
 		createCommand.execute();
 		removeCommand.execute();
 		View createdView = createCommand.getCreatedView();
 		if (createdView != null && obsoleteView != null && createdView.eClass().equals(obsoleteView.eClass())) {
 			if (NotationPackage.eINSTANCE.getNode().equals(createdView.eClass())) {
 				copy(obsoleteView, createdView, NotationPackage.eINSTANCE.getNode_LayoutConstraint());
 			} else if (NotationPackage.eINSTANCE.getEdge().equals(createdView.eClass())) {
 				copy(obsoleteView, createdView, NotationPackage.eINSTANCE.getEdge_Bendpoints());
 			}
 		}
 	}
 
 	private void copy(EObject source, EObject target, EStructuralFeature feature) {
 		EObject oldValue = (EObject) source.eGet(feature);
 		if (oldValue == null) {
 			return;	//nothing to copy
 		}
 		EObject newValue = (EObject) target.eGet(feature);
 		if (newValue != null && !newValue.eClass().equals(oldValue.eClass())) {
 			return;	//incompatible instances.
 		}
 		target.eSet(feature, oldValue);
 	}
 
 	public void undo() {
 		View createdView = createCommand.getCreatedView();
 		if (createdView != null && obsoleteView != null && createdView.eClass().equals(obsoleteView.eClass())) {
 			if (NotationPackage.eINSTANCE.getNode().equals(createdView.eClass())) {
				copy(createdView, obsoleteView, NotationPackage.eINSTANCE.getNode_LayoutConstraint());
 			} else if (NotationPackage.eINSTANCE.getEdge().equals(createdView.eClass())) {
				copy(createdView, obsoleteView, NotationPackage.eINSTANCE.getEdge_Bendpoints());
 			}
 		}
 		removeCommand.undo();
 		createCommand.undo();
 	}
 
 	public void redo() {
 		execute();
 	}
 }
