 /*******************************************************************************
  * Copyright (c) 2013 Wind River Systems, Inc. and others. All rights reserved.
  * This program and the accompanying materials are made available under the terms
  * of the Eclipse Public License v1.0 which accompanies this distribution, and is
  * available at http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  * Wind River Systems - initial API and implementation
  *******************************************************************************/
 
 package org.eclipse.tcf.te.tcf.locator.steps;
 
 import java.util.concurrent.TimeoutException;
 
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Status;
import org.eclipse.osgi.util.NLS;
 import org.eclipse.tcf.protocol.IPeer;
 import org.eclipse.tcf.protocol.Protocol;
 import org.eclipse.tcf.te.runtime.callback.Callback;
 import org.eclipse.tcf.te.runtime.concurrent.util.ExecutorsUtil;
 import org.eclipse.tcf.te.runtime.interfaces.callback.ICallback;
 import org.eclipse.tcf.te.runtime.interfaces.properties.IPropertiesContainer;
 import org.eclipse.tcf.te.runtime.stepper.interfaces.IFullQualifiedId;
 import org.eclipse.tcf.te.runtime.stepper.interfaces.IStepContext;
 import org.eclipse.tcf.te.runtime.utils.ProgressHelper;
 import org.eclipse.tcf.te.runtime.utils.StatusHelper;
 import org.eclipse.tcf.te.tcf.core.Tcf;
 import org.eclipse.tcf.te.tcf.locator.activator.CoreBundleActivator;
 import org.eclipse.tcf.te.tcf.locator.interfaces.nodes.IPeerModelProperties;
 import org.eclipse.tcf.te.tcf.locator.interfaces.services.ILocatorModelRefreshService;
 import org.eclipse.tcf.te.tcf.locator.model.Model;
 import org.eclipse.tcf.te.tcf.locator.nls.Messages;
 
 /**
  * WaitForReadyStep
  */
 public class WaitForReadyStep extends AbstractPeerModelStep {
 
 	/**
 	 * Constructor.
 	 */
 	public WaitForReadyStep() {
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.tcf.te.runtime.stepper.interfaces.IStep#validateExecute(org.eclipse.tcf.te.runtime.stepper.interfaces.IStepContext, org.eclipse.tcf.te.runtime.interfaces.properties.IPropertiesContainer, org.eclipse.tcf.te.runtime.stepper.interfaces.IFullQualifiedId, org.eclipse.core.runtime.IProgressMonitor)
 	 */
 	@Override
 	public void validateExecute(IStepContext context, IPropertiesContainer data, IFullQualifiedId fullQualifiedId, IProgressMonitor monitor) throws CoreException {
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.tcf.te.runtime.stepper.interfaces.IStep#execute(org.eclipse.tcf.te.runtime.stepper.interfaces.IStepContext, org.eclipse.tcf.te.runtime.interfaces.properties.IPropertiesContainer, org.eclipse.tcf.te.runtime.stepper.interfaces.IFullQualifiedId, org.eclipse.core.runtime.IProgressMonitor, org.eclipse.tcf.te.runtime.interfaces.callback.ICallback)
 	 */
 	@Override
 	public void execute(final IStepContext context, final IPropertiesContainer data, final IFullQualifiedId fullQualifiedId, final IProgressMonitor monitor, final ICallback callback) {
 		// Trigger a refresh of the model to read in the newly created static peer
 		final ILocatorModelRefreshService service = Model.getModel().getService(ILocatorModelRefreshService.class);
 		if (service != null && !Boolean.getBoolean("WaitForReadyStep.skip")) { //$NON-NLS-1$
 			Protocol.invokeLater(new Runnable() {
 				final Runnable thisRunnable = this;
 				int refreshCount = 0;
 				@Override
 				public void run() {
 					if (ProgressHelper.isCancel(WaitForReadyStep.this, monitor, callback)) {
 						return;
 					}
 					else if (refreshCount >= getTotalWork(context, data)) {
						@SuppressWarnings("synthetic-access")
                        String message = NLS.bind(Messages.WaitForReadyStep_error_timeout, getActivePeerContext(context, data, fullQualifiedId).getName());
						callback(data, fullQualifiedId, callback, StatusHelper.getStatus(new TimeoutException(message)), null);
 					}
 					else if (getActivePeerModelContext(context, data, fullQualifiedId).isProperty(IPeerModelProperties.PROP_STATE, IPeerModelProperties.STATE_WAITING_FOR_READY)) {
 						// Refresh the model now (must be executed within the TCF dispatch thread)
 						service.refresh(new Callback() {
 							@Override
 							protected void internalDone(Object caller, org.eclipse.core.runtime.IStatus status) {
 								refreshCount++;
 								ProgressHelper.worked(monitor, 1);
 								Protocol.invokeLater(refreshCount < 20 ? 500 : 1000, thisRunnable);
 							}
 						});
 					}
 					else {
 						int state = getActivePeerModelContext(context, data, fullQualifiedId).getIntProperty(IPeerModelProperties.PROP_STATE);
 						if (state == IPeerModelProperties.STATE_CONNECTED || state == IPeerModelProperties.STATE_REACHABLE) {
 							Object wait = getParameters().get("wait"); //$NON-NLS-1$
 							if (wait != null) {
 								try {
 									int waitValue = Integer.parseInt(wait.toString());
 									ExecutorsUtil.waitAndExecute(waitValue, null);
 								}
 								catch (Exception e) {
 								}
 							}
 							callback(data, fullQualifiedId, callback, Status.OK_STATUS, null);
 						}
						else {
							@SuppressWarnings("synthetic-access")
	                        String message = NLS.bind(Messages.WaitForReadyStep_error_state, getActivePeerContext(context, data, fullQualifiedId).getName());

							String cause = null;
							if (state == IPeerModelProperties.STATE_ERROR) {
								cause = getActivePeerModelContext(context, data, fullQualifiedId).getStringProperty(IPeerModelProperties.PROP_LAST_SCANNER_ERROR);
							}

							if (cause != null && !"".equals(cause.trim())) { //$NON-NLS-1$
								message += NLS.bind(Messages.WaitForReadyStep_error_reason_cause, cause);
							} else {
								message += Messages.WaitForReadyStep_error_reason_unknown;
							}

							callback(data, fullQualifiedId, callback, StatusHelper.getStatus(new CoreException(new Status(IStatus.ERROR, CoreBundleActivator.getUniqueIdentifier(), message))), null);
						}
 					}
 				}
 			});
 		}
 		else {
 			callback(data, fullQualifiedId, callback, Status.OK_STATUS, null);
 		}
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.tcf.te.runtime.stepper.steps.AbstractStep#getTotalWork(org.eclipse.tcf.te.runtime.stepper.interfaces.IStepContext, org.eclipse.tcf.te.runtime.interfaces.properties.IPropertiesContainer)
 	 */
 	@Override
 	public int getTotalWork(IStepContext context, IPropertiesContainer data) {
 	    return 100;
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.tcf.te.runtime.stepper.steps.AbstractStep#rollback(org.eclipse.tcf.te.runtime.stepper.interfaces.IStepContext, org.eclipse.tcf.te.runtime.interfaces.properties.IPropertiesContainer, org.eclipse.core.runtime.IStatus, org.eclipse.tcf.te.runtime.stepper.interfaces.IFullQualifiedId, org.eclipse.core.runtime.IProgressMonitor, org.eclipse.tcf.te.runtime.interfaces.callback.ICallback)
 	 */
 	@Override
 	public void rollback(IStepContext context, IPropertiesContainer data, IStatus status, IFullQualifiedId fullQualifiedId, IProgressMonitor monitor, ICallback callback) {
 		final IPeer peer = getActivePeerContext(context, data, fullQualifiedId);
 		Protocol.invokeAndWait(new Runnable() {
 			@Override
 			public void run() {
 				Tcf.getChannelManager().shutdown(peer);
 			}
 		});
 		super.rollback(context, data, status, fullQualifiedId, monitor, callback);
 	}
 }
