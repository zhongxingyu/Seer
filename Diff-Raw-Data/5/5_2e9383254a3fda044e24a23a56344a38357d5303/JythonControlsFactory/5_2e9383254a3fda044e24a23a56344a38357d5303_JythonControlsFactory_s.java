 /*-
  * Copyright © 2009 Diamond Light Source Ltd.
  *
  * This file is part of GDA.
  *
  * GDA is free software: you can redistribute it and/or modify it under the
  * terms of the GNU General Public License version 3 as published by the Free
  * Software Foundation.
  *
  * GDA is distributed in the hope that it will be useful, but WITHOUT ANY
  * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
  * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
  * details.
  *
  * You should have received a copy of the GNU General Public License along
  * with GDA. If not, see <http://www.gnu.org/licenses/>.
  */
 
 package uk.ac.gda.menu;
 
 import gda.jython.InterfaceProvider;
 import gda.jython.Jython;
 import gda.jython.JythonServerStatus;
 import gda.observable.IObserver;
 
 import org.eclipse.core.expressions.Expression;
 import org.eclipse.jface.action.Action;
 import org.eclipse.jface.action.ActionContributionItem;
 import org.eclipse.jface.action.IAction;
 import org.eclipse.jface.action.Separator;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.widgets.Event;
 import org.eclipse.ui.handlers.IHandlerService;
 import org.eclipse.ui.menus.CommandContributionItem;
 import org.eclipse.ui.menus.CommandContributionItemParameter;
 import org.eclipse.ui.menus.ExtensionContributionFactory;
 import org.eclipse.ui.menus.IContributionRoot;
 import org.eclipse.ui.services.IServiceLocator;
 
 import com.swtdesigner.ResourceManager;
 
 /**
  * We have to implement these actions in code because they connect to the server and 
  * have complex logic as to when they are enabled.
  */
 public class JythonControlsFactory extends ExtensionContributionFactory {
 	
 	private static ActionContributionItem pauseScan;
 	private static ActionContributionItem haltScan;
 	private static ActionContributionItem haltScript;
 	private static ActionContributionItem pauseScript;
 	private static Boolean controlsEnabled = true;
 	
 	public static void enableUIControls(){
 		controlsEnabled = true;
 		enableControls();
 	}
 
 	private static void enableControls(){
 		enableControl(pauseScan);
 		enableControl(haltScan);
 		enableControl(haltScript);
 		enableControl(pauseScript);
 	}
 	
 	private static void enableControl(ActionContributionItem item) {
 		if (item != null) {
 			item.getAction().setEnabled(controlsEnabled);
 		}
 	}
 	
 	public static void disableUIControls(){
 		controlsEnabled = false;
 		enableControls();
 	}
 
 	@Override
 	public void createContributionItems(final IServiceLocator serviceLocator, IContributionRoot additions) {
 		
 		additions.addContributionItem(new Separator(), Expression.TRUE);
 
 		haltScan = createHaltAction(serviceLocator, "Interrupt Scan Gracefully", "uk.ac.gda.client.jython.HaltScan", "/control_stop_blue.png", true);
 		additions.addContributionItem(haltScan, Expression.TRUE);
 		
 		pauseScan = createPauseAction(serviceLocator, "Pause Scan", "uk.ac.gda.client.jython.PauseScan", "/control_pause_blue.png", true);
 		additions.addContributionItem(pauseScan, Expression.TRUE);
 
 		additions.addContributionItem(new Separator(), Expression.TRUE);
 		
 		haltScript = createHaltAction(serviceLocator, "Stop Script", "uk.ac.gda.client.jython.HaltScript", "/script_delete.png", false);
 		additions.addContributionItem(haltScript, Expression.TRUE);
 		
 		pauseScript = createPauseAction(serviceLocator, "Pause Script", "uk.ac.gda.client.jython.PauseScript", "/script_pause.png", false);
 		additions.addContributionItem(pauseScript, Expression.TRUE);
 
 		additions.addContributionItem(new Separator(), Expression.TRUE);
 		
 		CommandContributionItemParameter pStop = new CommandContributionItemParameter(serviceLocator, null, "uk.ac.gda.client.StopAll", null, ResourceManager.getImageDescriptor(JythonControlsFactory.class, "/stop.png"), null, null, "Stop All", null, null, SWT.PUSH, null, false);
 		final CommandContributionItem    stopAll = new CommandContributionItem(pStop);
 		  
 		additions.addContributionItem(stopAll, Expression.TRUE);
 	}
 	
 
 	private HaltContributionItem createHaltAction(final IServiceLocator serviceLocator, final String label, final String commandId, final String iconPath, final boolean isScan) {
 		final HaltContributionItem halt = new HaltContributionItem(new Action(label, SWT.NONE) {
 			@Override
 			public void run() {
 				try {
 					((IHandlerService)serviceLocator.getService(IHandlerService.class)).executeCommand(commandId, new Event());
 				} catch (Exception e) {
 					e.printStackTrace();
 				}
 			}
 		}, isScan);
 		halt.getAction().setImageDescriptor(ResourceManager.getImageDescriptor(JythonControlsFactory.class, iconPath));
 		return halt;
 	}
 
 	private PauseContributionItem createPauseAction(final IServiceLocator serviceLocator, final String label, final String commandId, final String iconPath, final boolean isScan) {
 		final PauseContributionItem pause = new PauseContributionItem(new Action(label, SWT.TOGGLE) {
 			@Override
 			public void run() {
 				try {
 					final Boolean isPaused = (Boolean)((IHandlerService)serviceLocator.getService(IHandlerService.class)).executeCommand(commandId, new Event());
 					setChecked(isPaused);
 				} catch (Exception e) {
 					e.printStackTrace();
 				}
 			}
 		}, isScan);
 		pause.getAction().setImageDescriptor(ResourceManager.getImageDescriptor(JythonControlsFactory.class, iconPath));
 		return pause;
 	}
 
 	private class HaltContributionItem extends JythonContributionItem {
 		public HaltContributionItem(IAction action, boolean isScan) {
 			super(action, isScan);
 		}
 	}
 
 	private class PauseContributionItem extends JythonContributionItem {
 		public PauseContributionItem(IAction action, boolean isScan) {
 			super(action, isScan);
 		}
 		
 		@Override
 		public void dispose() {
 			super.dispose();
 			InterfaceProvider.getJSFObserver().deleteIObserver(this);
 		}
 
 		@Override
 		public void update(Object source, Object arg) {
 			super.update(source, arg);
 			if (arg instanceof JythonServerStatus && controlsEnabled) {
 				JythonServerStatus status = (JythonServerStatus)arg;
 				getAction().setChecked(isPaused(status));
 			}
 		}
 	}
 	
 	private abstract class JythonContributionItem extends ActionContributionItem implements IObserver {
 		protected final boolean isScan;
 
 		public JythonContributionItem(IAction action, boolean isScan) {
 			super(action);
 			this.isScan = isScan;
 			InterfaceProvider.getJSFObserver().addIObserver(this);
			JythonServerStatus status = InterfaceProvider.getJythonServerStatusProvider().getJythonServerStatus();
			update(this, status);
 		}
 		
 		@Override
 		public void update(Object source, Object arg) {
 			if (arg instanceof JythonServerStatus && controlsEnabled) {
 				JythonServerStatus status = (JythonServerStatus)arg;
 				getAction().setEnabled(isRunning(status));
 			}
 		}
 		
 		@Override
 		public void dispose() {
 			super.dispose();
 			try {
 				InterfaceProvider.getJSFObserver().deleteIObserver(this);
 			} catch (Exception ignored) {
 				// We do not want any notification if this fails.
 			}
 		}
 		
 		public boolean isRunning(JythonServerStatus status) {
 			boolean isRunning = false;
 			if (isScan) {
 				if (status.scanStatus == Jython.PAUSED || status.scanStatus == Jython.RUNNING) {
 					isRunning = true;
 				}
 			} else {
 				if (status.scriptStatus == Jython.PAUSED || status.scriptStatus == Jython.RUNNING) {
 					isRunning = true;
 				}
 			}
 			return isRunning;
 		}
 		
 		public boolean isPaused(JythonServerStatus status) {
 			boolean isPaused = false;
 			if (isScan) {
 				if (status.scanStatus == Jython.PAUSED) {
 					isPaused = true;
 				}
 			} else {
 				if (status.scriptStatus == Jython.PAUSED) {
 					isPaused = true;
 				}
 			}
 			return isPaused;
 		}
 	}
 }
