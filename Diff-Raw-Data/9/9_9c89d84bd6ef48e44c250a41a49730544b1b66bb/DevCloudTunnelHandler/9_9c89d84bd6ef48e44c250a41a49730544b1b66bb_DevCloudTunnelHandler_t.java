 package org.zend.php.zendserver.deployment.debug.ui.handlers;
 
 import java.io.DataOutputStream;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 
 import org.eclipse.core.commands.AbstractHandler;
 import org.eclipse.core.commands.ExecutionEvent;
 import org.eclipse.core.commands.ExecutionException;
 import org.eclipse.jface.dialogs.MessageDialog;
 import org.eclipse.ui.IWorkbenchWindow;
 import org.eclipse.ui.handlers.HandlerUtil;
 import org.zend.php.zendserver.deployment.core.targets.TargetsManagerService;
 import org.zend.php.zendserver.deployment.debug.core.tunnel.ZendDevCloudTunnel;
 import org.zend.php.zendserver.deployment.debug.ui.Activator;
 import org.zend.php.zendserver.deployment.debug.ui.Messages;
 import org.zend.sdklib.internal.target.ZendDevCloud;
 import org.zend.sdklib.manager.TargetsManager;
 import org.zend.sdklib.target.IZendTarget;
 
 /**
  * Our sample handler extends AbstractHandler, an IHandler base class.
  * 
  * @see org.eclipse.core.commands.IHandler
  * @see org.eclipse.core.commands.AbstractHandler
  */
 public class DevCloudTunnelHandler extends AbstractHandler {
 
 	/**
 	 * The constructor.
 	 */
 	public DevCloudTunnelHandler() {
 	}
 
 	/**
 	 * the command has been executed, so extract extract the needed information
 	 * from the application context.
 	 */
 	public Object execute(ExecutionEvent event) throws ExecutionException {
 		String user = null;
 		String filename = null;
 		final TargetsManager m = TargetsManagerService.INSTANCE
 				.getTargetManager();
 		final IZendTarget[] targets = m.getTargets();
 		for (IZendTarget t : targets) {
 			final String host = t.getHost().getHost();
 			if (host.contains(ZendDevCloud.DEVPASS_HOST)) {
 				user = host.substring(0, host.indexOf('.'));
 				final String property = t.getProperty(ZendDevCloud.SSH_PRIVATE_KEY); 
 				if (property != null) {
 					try {
 						final File file = File.createTempFile("zend-cloud", "pem"); //$NON-NLS-1$ //$NON-NLS-2$
 						DataOutputStream dos = new DataOutputStream(new FileOutputStream(file));
 						dos.write(property.getBytes());
 						dos.close();
 						file.deleteOnExit();
 						filename = file.getAbsolutePath();
 					} catch (IOException e) {
 						throw new ExecutionException("", e); //$NON-NLS-1$
 					}
 				}
 			}
 		}
 		IWorkbenchWindow window = HandlerUtil
 				.getActiveWorkbenchWindowChecked(event);
 		
 		ZendDevCloudTunnel t = new ZendDevCloudTunnel(user, filename);
 		try {
 			t.connect();
 		} catch (IOException e) {
 			MessageDialog.openError(window.getShell(), Messages.DevCloudTunnelHandler_7,
					e.getMessage());
 			Activator.log(e);
 			return null;
 		}
 
 		MessageDialog.openInformation(window.getShell(), Messages.DevCloudTunnelHandler_7,
 				Messages.DevCloudTunnelHandler_10);
 		return null;
 	}
 }
