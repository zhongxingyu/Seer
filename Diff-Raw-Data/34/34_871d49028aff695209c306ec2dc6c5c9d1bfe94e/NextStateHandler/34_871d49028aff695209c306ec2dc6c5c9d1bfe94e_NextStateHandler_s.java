 // $codepro.audit.disable com.instantiations.assist.eclipse.analysis.audit.rule.effectivejava.alwaysOverridetoString.alwaysOverrideToString, com.instantiations.assist.eclipse.analysis.deserializeabilitySecurity, com.instantiations.assist.eclipse.analysis.disallowReturnMutable, com.instantiations.assist.eclipse.analysis.enforceCloneableUsageSecurity
 /*******************************************************************************
  * Copyright (c) 2011 Ericsson Research Canada
  * 
  * All rights reserved. This program and the accompanying materials are
  * made available under the terms of the Eclipse Public License v1.0 which
  * accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Description:
  * 
  * This class implements the command to progress the element (Review/Anomaly)
  * to its next state
  * 
  * Contributors:
  *   Sebastien Dubois - Created for Mylyn Review R4E project
  *   
  ******************************************************************************/
 package org.eclipse.mylyn.reviews.r4e.ui.internal.commands;
 
 import java.util.Iterator;
 
 import org.eclipse.core.commands.AbstractHandler;
 import org.eclipse.core.commands.ExecutionEvent;
 import org.eclipse.core.commands.ExecutionException;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.jface.viewers.ISelection;
 import org.eclipse.jface.viewers.IStructuredSelection;
 import org.eclipse.jface.window.Window;
 import org.eclipse.mylyn.reviews.r4e.core.model.R4EAnomalyState;
 import org.eclipse.mylyn.reviews.r4e.core.model.R4EReviewPhase;
 import org.eclipse.mylyn.reviews.r4e.ui.R4EUIPlugin;
 import org.eclipse.mylyn.reviews.r4e.ui.internal.dialogs.ChangeStateDialog;
 import org.eclipse.mylyn.reviews.r4e.ui.internal.model.IR4EUIModelElement;
 import org.eclipse.mylyn.reviews.r4e.ui.internal.model.R4EUIAnomalyExtended;
 import org.eclipse.mylyn.reviews.r4e.ui.internal.model.R4EUIModelController;
 import org.eclipse.mylyn.reviews.r4e.ui.internal.model.R4EUIReviewBasic;
 import org.eclipse.mylyn.reviews.r4e.ui.internal.model.R4EUIReviewExtended;
 import org.eclipse.mylyn.reviews.r4e.ui.internal.utils.R4EUIConstants;
 import org.eclipse.mylyn.reviews.r4e.ui.internal.utils.UIUtils;
 import org.eclipse.ui.handlers.HandlerUtil;
 import org.eclipse.ui.progress.UIJob;
 import org.eclipse.ui.services.IEvaluationService;
 
 /**
  * @author lmcdubo
  * @version $Revision: 1.0 $
  */
 public class NextStateHandler extends AbstractHandler {
 
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
 
 		final UIJob job = new UIJob("Progressing element to next state...") {
 			@Override
 			public IStatus runInUIThread(IProgressMonitor monitor) {
 				final ISelection selection = HandlerUtil.getCurrentSelection(event);
 				if (selection instanceof IStructuredSelection) {
 					if (!selection.isEmpty()) {
 						IR4EUIModelElement element = null;
 						R4EUIModelController.setJobInProgress(true);
 						for (final Iterator<?> iterator = ((IStructuredSelection) selection).iterator(); iterator.hasNext();) {
 							element = (IR4EUIModelElement) iterator.next();
 							R4EUIPlugin.Ftracer.traceInfo("Progressing state for element " + element.getName());
 
 							if (element instanceof R4EUIReviewExtended) {
 
 								R4EUIReviewExtended reviewElement = (R4EUIReviewExtended) element;
 
 								String[] newPhasesStr = reviewElement.getNextAvailablePhases();
 								if (newPhasesStr.length > 1) {
 									//Get next phase from user
 									R4EUIModelController.setJobInProgress(true);
 									final ChangeStateDialog dialog = new ChangeStateDialog(
 											R4EUIModelController.getNavigatorView()
 													.getSite()
 													.getWorkbenchWindow()
 													.getShell(), reviewElement);
 									dialog.create();
 									dialog.setStates(newPhasesStr);
 
 									final int result = dialog.open();
 									if (result == Window.OK) {
 										R4EReviewPhase newPhase = reviewElement.getPhaseFromString(dialog.getState());
 										UIUtils.changeReviewPhase(element, newPhase);
 									} else if (result != Window.CANCEL) {
 										R4EUIModelController.setJobInProgress(false); //Enable commands in case of error
 									}
 								} else {
 									//Only 1 possible value to use
 									R4EReviewPhase newPhase = reviewElement.getPhaseFromString(newPhasesStr[0]);
 									UIUtils.changeReviewPhase(element, newPhase);
 								}
 
 							} else if (element instanceof R4EUIReviewBasic) {
 								R4EUIReviewBasic reviewElement = (R4EUIReviewBasic) element;
 								R4EReviewPhase newPhase = reviewElement.getPhaseFromString(R4EUIConstants.REVIEW_PHASE_COMPLETED);
 								UIUtils.changeReviewPhase(element, newPhase);
 
 							} else if (element instanceof R4EUIAnomalyExtended) {
 
 								//Get next state (from user if needed)
 								R4EUIAnomalyExtended anomalyElement = (R4EUIAnomalyExtended) element;
 								R4EUIModelController.setJobInProgress(true);
 
 								String[] newStatesStr = anomalyElement.getNextAvailableStates();
 								if (newStatesStr.length > 1) {
 
 									final ChangeStateDialog dialog = new ChangeStateDialog(
 											R4EUIModelController.getNavigatorView()
 													.getSite()
 													.getWorkbenchWindow()
 													.getShell(), anomalyElement);
 									dialog.create();
 									dialog.setStates(newStatesStr);
 
 									final int result = dialog.open();
 									if (result == Window.OK) {
 										R4EAnomalyState newState = R4EUIAnomalyExtended.getStateFromString(dialog.getState());
 										UIUtils.changeAnomalyState(element, newState);
 									} else if (result != Window.CANCEL) {
 										R4EUIModelController.setJobInProgress(false); //Enable commands in case of error
 									}
 								} else {
 									//Only 1 possible value to use
 									R4EAnomalyState newState = R4EUIAnomalyExtended.getStateFromString(newStatesStr[0]);
 									UIUtils.changeAnomalyState(element, newState);
 								}
 							}
 						}
 						try {
 							final IEvaluationService evService = (IEvaluationService) HandlerUtil.getActiveWorkbenchWindowChecked(
 									event)
 									.getService(IEvaluationService.class);
 							evService.requestEvaluation("org.eclipse.mylyn.reviews.r4e.ui.commands.nextState");
 							evService.requestEvaluation("org.eclipse.mylyn.reviews.r4e.ui.commands.previousState");
 						} catch (ExecutionException e) {
 							R4EUIPlugin.Ftracer.traceError("Exception: " + e.toString() + " (" + e.getMessage() + ")");
 						}
 						R4EUIModelController.setJobInProgress(false); //Enable commands in case of error
 						UIUtils.setNavigatorViewFocus(element, false);
 					}
 				}
 				return Status.OK_STATUS;
 			}
 		};
 		job.setUser(true);
 		job.schedule();
 		return null;
 	}
 }
