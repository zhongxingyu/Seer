 // $codepro.audit.disable com.instantiations.assist.eclipse.analysis.audit.rule.effectivejava.alwaysOverridetoString.alwaysOverrideToString, com.instantiations.assist.eclipse.analysis.deserializeabilitySecurity, com.instantiations.assist.eclipse.analysis.disallowReturnMutable, com.instantiations.assist.eclipse.analysis.enforceCloneableUsageSecurity
 /*******************************************************************************
  * Copyright (c) 2010 Ericsson Research Canada
  * 
  * All rights reserved. This program and the accompanying materials are
  * made available under the terms of the Eclipse Public License v1.0 which
  * accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Description:
  * 
  * This class implements the review navigator view toolbar command used 
  * to browse the view's treeviewer elements and open the ones that can be open
  * 
  * Contributors:
  *   Sebastien Dubois - Created for Mylyn Review R4E project
  *   
  ******************************************************************************/
 package org.eclipse.mylyn.reviews.r4e.ui.internal.commands;
 
 import org.eclipse.core.commands.AbstractHandler;
 import org.eclipse.core.commands.ExecutionEvent;
 import org.eclipse.core.commands.ExecutionException;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.jface.viewers.ISelection;
 import org.eclipse.jface.viewers.IStructuredSelection;
 import org.eclipse.jface.viewers.StructuredSelection;
 import org.eclipse.mylyn.reviews.r4e.ui.R4EUIPlugin;
 import org.eclipse.mylyn.reviews.r4e.ui.internal.editors.EditorProxy;
 import org.eclipse.mylyn.reviews.r4e.ui.internal.model.IR4EUIModelElement;
 import org.eclipse.mylyn.reviews.r4e.ui.internal.model.R4EUIAnomalyBasic;
 import org.eclipse.mylyn.reviews.r4e.ui.internal.model.R4EUIContent;
 import org.eclipse.mylyn.reviews.r4e.ui.internal.model.R4EUIFileContext;
 import org.eclipse.mylyn.reviews.r4e.ui.internal.model.R4EUIModelController;
 import org.eclipse.mylyn.reviews.r4e.ui.internal.navigator.ReviewNavigatorTreeViewer;
 import org.eclipse.mylyn.reviews.r4e.ui.internal.navigator.ReviewNavigatorView;
 import org.eclipse.swt.widgets.TreeItem;
 import org.eclipse.ui.handlers.HandlerUtil;
 import org.eclipse.ui.progress.UIJob;
 
 /**
  * @author lmcdubo
  * @version $Revision: 1.0 $
  */
 public class SelectPreviousHandler extends AbstractHandler {
 
 	// ------------------------------------------------------------------------
 	// Methods
 	// ------------------------------------------------------------------------
 
 	/**
 	 * Method execute.
 	 * 
 	 * @param event
 	 *            ExecutionEvent
 	 * @return Object
 	 * @throws ExecutionException
 	 * @see org.eclipse.core.commands.IHandler#execute(ExecutionEvent)
 	 */
 	public Object execute(final ExecutionEvent event) {
 
 		final UIJob job = new UIJob("Selecting Previous Element...") {
 			@Override
 			public IStatus runInUIThread(IProgressMonitor monitor) {
 				final ISelection selection = HandlerUtil.getCurrentSelection(event);
 				if (selection instanceof IStructuredSelection) {
					if (null != selection && !selection.isEmpty()) {
 						final ReviewNavigatorView view = R4EUIModelController.getNavigatorView();
 
 						//Get the previous element
 						final IR4EUIModelElement previousElement = getPreviousElement((ReviewNavigatorTreeViewer) view.getTreeViewer());
 
 						//If there is one, select it
 						if (null != previousElement) {
 							R4EUIPlugin.Ftracer.traceInfo("Select previous element " + previousElement.getName());
 							final ISelection previousSelection = new StructuredSelection(previousElement);
 							view.getTreeViewer().setSelection(previousSelection);
 
 							//Open the editor on FileContexts, selections amd anomalies
 							if (previousElement instanceof R4EUIFileContext || previousElement instanceof R4EUIContent
 									|| previousElement instanceof R4EUIAnomalyBasic) {
 								EditorProxy.openEditor(view.getSite().getPage(), previousSelection, false);
 							}
 						}
 					}
 				}
 				return Status.OK_STATUS;
 			}
 		};
 		job.setUser(true);
 		job.schedule();
 		return null;
 	}
 
 	/**
 	 * Method getPreviousElement.
 	 * 
 	 * @param aTreeViewer
 	 *            ReviewNavigatorTreeViewer
 	 * @return IR4EUIModelElement
 	 */
 	private IR4EUIModelElement getPreviousElement(ReviewNavigatorTreeViewer aTreeViewer) {
 		final TreeItem item = aTreeViewer.getTree().getSelection()[0];
 		final TreeItem previousItem = aTreeViewer.getPrevious(item);
 		return (IR4EUIModelElement) previousItem.getData();
 	}
 }
