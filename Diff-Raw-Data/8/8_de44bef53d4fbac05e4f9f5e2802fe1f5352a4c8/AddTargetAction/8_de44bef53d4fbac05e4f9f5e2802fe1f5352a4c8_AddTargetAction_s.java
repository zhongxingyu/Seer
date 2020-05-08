 package org.zend.php.zendserver.deployment.ui.actions;
 
 import java.net.MalformedURLException;
 import java.net.URL;
 
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.jface.action.Action;
 import org.eclipse.jface.window.Window;
 import org.eclipse.jface.wizard.WizardDialog;
 import org.eclipse.ui.IWorkbenchWindow;
 import org.eclipse.ui.PlatformUI;
 import org.eclipse.ui.statushandlers.StatusManager;
 import org.zend.php.zendserver.deployment.core.targets.TargetsManagerService;
 import org.zend.php.zendserver.deployment.ui.Activator;
 import org.zend.php.zendserver.deployment.ui.Messages;
 import org.zend.php.zendserver.deployment.ui.targets.CreateTargetWizard;
 import org.zend.sdklib.internal.target.ZendTarget;
 import org.zend.sdklib.manager.TargetException;
 import org.zend.sdklib.manager.TargetsManager;
 import org.zend.sdklib.target.IZendTarget;
 
 /**
  * Adds new Deployment Target via TargetDetailsDialog.
  *
  */
 public class AddTargetAction extends Action {
 	
 	private static final int[] possiblePorts = new int[] { 10081, 10082, 10088 };
 	
 	private IZendTarget addedTarget;
 
 
 	public AddTargetAction() {
 		super(Messages.AddTargetAction_AddNewTarget, Activator.getImageDescriptor(Activator.IMAGE_ADD_TARGET));
 	}
 
 	
 	@Override
 	public void run() {
 		IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
 		
 		CreateTargetWizard wizard = new CreateTargetWizard();
 		WizardDialog dialog = wizard.createDialog(window.getShell());
 		
 		if (dialog.open() != Window.OK) {
 			return; // canceled by user
 		}
 		
 		IZendTarget[] newTarget = wizard.getTarget();
 		
 		TargetsManager tm = TargetsManagerService.INSTANCE.getTargetManager();
 		Exception lastException = null;
 		for (IZendTarget t : newTarget) {
 			if (t.getHost().getPort() == -1) {
 				for (int port : possiblePorts) {
 					try {
 						URL old = t.getHost();
 						URL host = new URL(old.getProtocol(), old.getHost(),
 								port, old.getFile());
						IZendTarget target = new ZendTarget(t.getId(), host,
								t.getDefaultServerURL(), t.getKey(),
								t.getSecretKey());
						if (tm.add(target) != null) {
							addedTarget = target;
 							break;
 						}
 					} catch (MalformedURLException e) {
 						// should not appear on this stage so just continue
 					} catch (TargetException e) {
 						lastException = e;
 						continue;
 					}
 				}
 			} else {
 				try {
 					if (tm.add(t) != null) {
 						addedTarget = t;
 						break;
 					}
 				} catch (TargetException e) {
 					lastException = e;
 				}
 			}
 		}
 		if (addedTarget == null && lastException != null) {
 			StatusManager.getManager().handle(new Status(IStatus.ERROR, Activator.PLUGIN_ID, lastException.getMessage(), lastException), StatusManager.SHOW);
 		}
 	}
 
 	/**
 	 * @return Created target or null if target was not created.  
 	 */
 	public IZendTarget getTarget() {
 		return addedTarget;
 	}
 }
