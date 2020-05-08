 package org.eclipse.dltk.internal.ui.rse;
 
 import org.eclipse.core.runtime.NullProgressMonitor;
 import org.eclipse.core.runtime.OperationCanceledException;
 import org.eclipse.dltk.core.internal.rse.DLTKRSEPlugin;
 import org.eclipse.dltk.core.internal.rse.RSEConnectionQueryManager;
 import org.eclipse.dltk.core.internal.rse.RSEConnectionQueryManager.IConnector;
 import org.eclipse.rse.core.model.IHost;
 import org.eclipse.rse.core.subsystems.ISubSystem;
 import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFileSubSystem;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.ui.PlatformUI;
 
 /**
  * @since 2.0
  */
 public class RSEConnector implements IConnector {
 	private static boolean running = true;
 	private Thread processingThread = null;
 
	private Thread createPoprocessingThread() {
 		return new Thread("RSE connection resolver") {
 			public void run() {
 				while (running) {
 					if (RSEConnectionQueryManager.getInstance().hasHosts()) {
 						Display display = PlatformUI.getWorkbench()
 								.getDisplay();
 						if (PlatformUI.getWorkbench().isClosing()) {
 							IHost host = RSEConnectionQueryManager
 									.getInstance().getNextHost(false);
 							if (host != null) {
 								RSEConnectionQueryManager.getInstance()
 										.markHostAsFinished(host);
 							}
 						} else {
 							display.syncExec(new Runnable() {
 								public void run() {
 									IHost host = RSEConnectionQueryManager
 											.getInstance().getNextHost(false);
 									if (host != null) {
 										connect(host);
 										RSEConnectionQueryManager.getInstance()
 												.markHostAsFinished(host);
 									}
 								}
 							});
 						}
 					}
 					try {
 						Thread.sleep(200);
 					} catch (InterruptedException e) {
 						DLTKRSEPlugin.log(e);
 					}
 				}
 			}
 		};
 	}
 
 	private void connect(IHost host) {
 		ISubSystem[] subSystems = host.getSubSystems();
 		for (ISubSystem subsystem : subSystems) {
 			if (subsystem instanceof IRemoteFileSubSystem) {
 				try {
 					subsystem.connect(new NullProgressMonitor(), false);
 				} catch (OperationCanceledException e) {
 					// don't log it
 				} catch (Exception e) {
 					DLTKRSEPlugin.log(e);
 				}
 			}
 		}
 	};
 
 	public RSEConnector() {
 	}
 
 	public boolean isDirectProcessingRequired() {
 		// Process direct connection request.
 		Display current = Display.getCurrent();
 		if (current != null) {
 			// We are in UI thread
 			return true;
 		}
 		return false;
 	}
 
 	public void register() {
 		if (processingThread == null) {
			processingThread = createPoprocessingThread();
 			processingThread.start();
 		}
 	}
 
 	public void runDisplayRunnables(long timeout) {
 		long end = System.currentTimeMillis() + timeout;
 		// We need to interrupt processingThread if it is no executing.
 		Display current = Display.getCurrent();
 		MAIN_LOOP: while (RSEConnectionQueryManager.getInstance().hasHosts()
 				&& !current.isDisposed()
 				&& !PlatformUI.getWorkbench().isClosing()) {
 			while (current.readAndDispatch()) {
 				if (current.isDisposed()) {
 					break MAIN_LOOP;
 				}
 			}
 			if (end < System.currentTimeMillis()) { // Timeout
 				break;
 			}
 			IHost host = RSEConnectionQueryManager.getInstance().getNextHost(
 					false);
 			if (host != null) {
 				connect(host);
 				RSEConnectionQueryManager.getInstance()
 						.markHostAsFinished(host);
 			}
 		}
 	}
 
 	public static void stop() {
 		running = false;
 	}
 }
