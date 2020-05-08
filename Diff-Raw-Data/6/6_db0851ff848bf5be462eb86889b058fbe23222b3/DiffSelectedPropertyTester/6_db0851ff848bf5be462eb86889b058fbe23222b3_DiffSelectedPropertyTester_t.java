 /*******************************************************************************
  * Copyright (c) 2013 Obeo.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *     Obeo - initial API and implementation
  *******************************************************************************/
 package org.eclipse.emf.compare.ide.ui.internal.structuremergeviewer.handler.propertytester;
 
 import org.eclipse.compare.CompareConfiguration;
 import org.eclipse.compare.CompareEditorInput;
 import org.eclipse.core.expressions.PropertyTester;
 import org.eclipse.emf.common.notify.Adapter;
 import org.eclipse.emf.common.notify.Notifier;
 import org.eclipse.emf.compare.Diff;
import org.eclipse.emf.compare.DifferenceState;
 import org.eclipse.emf.compare.rcp.ui.internal.EMFCompareConstants;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.edit.tree.TreeNode;
 import org.eclipse.jface.viewers.ISelection;
 import org.eclipse.jface.viewers.IStructuredSelection;
 import org.eclipse.ui.IEditorInput;
 import org.eclipse.ui.IEditorPart;
 
 /**
  * A property tester that checks if a diff is selected in the compare editor.
  * 
  * @author <a href="mailto:axel.richard@obeo.fr">Axel Richard</a>
  * @since 3.0
  */
 public class DiffSelectedPropertyTester extends PropertyTester {
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.core.expressions.PropertyTester#test(java.lang.Object, java.lang.String,
 	 *      java.lang.Object[], java.lang.Object)
 	 */
 	public boolean test(Object receiver, String property, Object[] args, Object expectedValue) {
 		if (receiver instanceof IEditorPart) {
 			IEditorInput i = ((IEditorPart)receiver).getEditorInput();
 			if (i instanceof CompareEditorInput) {
 				CompareConfiguration configuration = ((CompareEditorInput)i).getCompareConfiguration();
 				ISelection selection = (ISelection)configuration
 						.getProperty(EMFCompareConstants.SMV_SELECTION);
 				if (selection instanceof IStructuredSelection) {
 					Object element = ((IStructuredSelection)selection).getFirstElement();
 					if (element instanceof Adapter) {
 						Notifier target = ((Adapter)element).getTarget();
 						if (target instanceof TreeNode) {
 							EObject data = ((TreeNode)target).getData();
 							if (data instanceof Diff) {
								if (DifferenceState.MERGED != ((Diff)data).getState()) {
									return true;
								}
 							}
 						}
 					}
 				}
 			}
 		}
 		return false;
 	}
 
 }
