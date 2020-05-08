 // $codepro.audit.disable com.instantiations.assist.eclipse.analysis.audit.rule.effectivejava.alwaysOverridetoString.alwaysOverrideToString, com.instantiations.assist.eclipse.analysis.deserializeabilitySecurity, com.instantiations.assist.eclipse.analysis.disallowReturnMutable, com.instantiations.assist.eclipse.analysis.enforceCloneableUsageSecurity
 /*******************************************************************************
  * Copyright (c) 2012 Ericsson AB and others.
  * 
  * All rights reserved. This program and the accompanying materials are
  * made available under the terms of the Eclipse Public License v1.0 which
  * accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Description:
  * 
  * This class implements the context-sensitive command to show the properties
  * for the element in the properties view
  * 
  * Contributors:
  *   Sebastien Dubois - Created for Mylyn Review R4E project
  *   
  ******************************************************************************/
 package org.eclipse.mylyn.reviews.r4e.ui.internal.commands.handlers;
 
 import java.util.List;
 
 import org.eclipse.core.commands.AbstractHandler;
 import org.eclipse.core.commands.ExecutionEvent;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.mylyn.reviews.r4e.ui.R4EUIPlugin;
 import org.eclipse.mylyn.reviews.r4e.ui.internal.model.IR4EUIModelElement;
 import org.eclipse.mylyn.reviews.r4e.ui.internal.model.R4EUIModelController;
 import org.eclipse.mylyn.reviews.r4e.ui.internal.navigator.ReviewNavigatorView;
 import org.eclipse.mylyn.reviews.r4e.ui.internal.utils.UIUtils;
 import org.eclipse.ui.IWorkbenchPart;
 import org.eclipse.ui.handlers.HandlerUtil;
 import org.eclipse.ui.progress.UIJob;
 
 /**
  * @author Sebastien Dubois
  * @version $Revision: 1.0 $
  */
 public class ShowPropertiesHandler extends AbstractHandler {
 
 	// ------------------------------------------------------------------------
 	// Constants
 	// ------------------------------------------------------------------------
 
 	/**
 	 * Field COMMAND_MESSAGE. (value is ""Showing Properties..."")
 	 */
 	private static final String COMMAND_MESSAGE = "Showing Properties...";
 
 	// ------------------------------------------------------------------------
 	// Methods
 	// ------------------------------------------------------------------------
 
 	/**
 	 * Method execute.
 	 * 
 	 * @param aEvent
 	 *            ExecutionEvent
 	 * @return Object
 	 * @see org.eclipse.core.commands.IHandler#execute(ExecutionEvent)
 	 */
 	public Object execute(final ExecutionEvent aEvent) {
 
 		final UIJob job = new UIJob(COMMAND_MESSAGE) {
 			@Override
 			public IStatus runInUIThread(IProgressMonitor monitor) {
 				monitor.beginTask(COMMAND_MESSAGE, 1);
 				R4EUIPlugin.Ftracer.traceInfo("Showing Properties View"); //$NON-NLS-1$
 
 				IWorkbenchPart part = HandlerUtil.getActivePart(aEvent);
				if (part instanceof ReviewNavigatorView) {
					R4EUIModelController.getNavigatorView().showProperties();
				} else {
 					//Here we assume that the commands comes from the annotation popup, so we will activate the Review Navigator view
 					//element to display the properties for the selected element.
 					final List<IR4EUIModelElement> selectedElements = UIUtils.getCommandUIElements();
 					ReviewNavigatorView mainView = R4EUIModelController.getNavigatorView();
 					if (null != mainView) {
 						mainView.updateView(selectedElements.get(0), 0, true);
 					}
 				}
 				monitor.worked(1);
 				monitor.done();
 				return Status.OK_STATUS;
 			}
 		};
 		job.setUser(true);
 		job.schedule();
 		return null;
 	}
 }
