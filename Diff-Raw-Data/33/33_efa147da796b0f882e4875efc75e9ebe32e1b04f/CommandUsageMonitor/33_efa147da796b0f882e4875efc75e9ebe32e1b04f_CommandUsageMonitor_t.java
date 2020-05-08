 /*******************************************************************************
  * Copyright (c) 2012 Zend Technologies Ltd.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *    Zend Technologies Ltd. - initial API and implementation
  *******************************************************************************/
 package org.zend.usagedata.internal.monitors;
 
 import java.util.List;
 
 import org.eclipse.core.commands.ExecutionEvent;
 import org.eclipse.core.commands.ExecutionException;
 import org.eclipse.core.commands.IExecutionListener;
 import org.eclipse.core.commands.NotHandledException;
 import org.eclipse.ui.IWorkbench;
 import org.eclipse.ui.IWorkbenchPage;
 import org.eclipse.ui.IWorkbenchPart;
 import org.eclipse.ui.IWorkbenchWindow;
 import org.eclipse.ui.PlatformUI;
 import org.eclipse.ui.commands.ICommandService;
 import org.zend.usagedata.monitors.AbstractMonitor;
 import org.zend.usagedata.monitors.MonitorUtils;
 
 /**
  * Instances of the {@link CommandUsageMonitor} class monitor commands usage. It
  * records events only performed in a part which is specified in monitor
  * configuration file. It collects following information:
  * <ul>
  * <li>result of command call (executed, failed, no handler),</li>
  * <li>command id,</li>
  * <li>part id,</li>
  * <li>if result is failed or no handler, message of the thrown exception.</li>
  * </ul>
  * 
  * @author Wojciech Galanciak, 2012
  * 
  */
 public class CommandUsageMonitor extends AbstractMonitor {
 
 	public static final String MONITOR_ID = "org.zend.commandUsageMonitor"; //$NON-NLS-1$
 
	private static final String PARTS_FILE = "config/commandUsageMonitor.config"; //$NON-NLS-1$
 	private static final String EXECUTED = "executed"; //$NON-NLS-1$
 	private static final String FAILED = "failed"; //$NON-NLS-1$
 	private static final String NO_HANDLER = "no handler"; //$NON-NLS-1$
 
 	private List<String> parts;
 
 	private IExecutionListener executionListener = new IExecutionListener() {
 		public void notHandled(String commandId, NotHandledException exception) {
 			recordEvent(NO_HANDLER, commandId, exception.getMessage());
 		}
 
 		public void postExecuteFailure(String commandId,
 				ExecutionException exception) {
 			recordEvent(FAILED, commandId, exception.getMessage());
 		}
 
 		public void postExecuteSuccess(String commandId, Object returnValue) {
 			recordEvent(EXECUTED, commandId);
 		}
 
 		public void preExecute(String commandId, ExecutionEvent event) {
 
 		}
 	};
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.zend.usagedata.monitors.AbstractMonitor#getId()
 	 */
 	public String getId() {
 		return MONITOR_ID;
 	};
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.zend.usagedata.monitors.AbstractMonitor#doStartMonitoring()
 	 */
 	protected void doStartMonitoring() {
 		parts = MonitorUtils.getValues(PARTS_FILE);
 		getCommandService().addExecutionListener(executionListener);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.zend.usagedata.monitors.AbstractMonitor#doStopMonitoring()
 	 */
 	protected void doStopMonitoring() {
 		ICommandService commandService = getCommandService();
 		if (commandService != null)
 			commandService.removeExecutionListener(executionListener);
 	}
 
 	private ICommandService getCommandService() {
 		return (ICommandService) PlatformUI.getWorkbench().getAdapter(
 				ICommandService.class);
 	}
 
 	private void recordEvent(String result, String commandId) {
 		recordEvent(result, commandId, ""); //$NON-NLS-1$
 	}
 
 	private void recordEvent(String result, String commandId,
 			String exceptionMessage) {
 		IWorkbench workbench = PlatformUI.getWorkbench();
 		if (workbench != null) {
 			IWorkbenchWindow activeWindow = workbench
 					.getActiveWorkbenchWindow();
 			if (activeWindow != null) {
 				IWorkbenchPage activePage = activeWindow.getActivePage();
 				if (activePage != null) {
 					IWorkbenchPart activePart = activePage.getActivePart();
 					String id = activePart.getSite().getId();
 					if (parts.contains(id)) {
 						recordEvent(MONITOR_ID, result, commandId, id,
 								exceptionMessage);
 					}
 				}
 			}
 		}
 	}
 
 }
