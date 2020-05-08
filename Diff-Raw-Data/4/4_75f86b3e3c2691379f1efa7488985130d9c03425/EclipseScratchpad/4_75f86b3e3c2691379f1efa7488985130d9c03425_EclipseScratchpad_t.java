 package testproduct.actions;
 
 import melnorme.miscutil.StringUtil;
 
 import org.eclipse.core.runtime.Platform;
 import org.eclipse.jface.dialogs.MessageDialog;
 import org.eclipse.ui.PlatformUI;
 
 public class EclipseScratchpad {
 
 	public static void printPlatformInfo() {
 		String message = StringUtil.collToString(Platform.getCommandLineArgs(), " ") + "\n" +
 			"Configuration: " + Platform.getConfigurationLocation().getURL() + "\n" +
 			"Install: " + Platform.getInstallLocation().getURL() + "\n" +
 			"Instance: " + Platform.getInstanceLocation().getURL() + "\n" +
 			"(Location): " + Platform.getLocation() + "\n" +
 			"User: " + Platform.getUserLocation().getURL() + "\n" +
//			"Workspace: " + ResourcesPlugin.getWorkspace().getRoot().getLocation() + "\n" +
 			"";
 		System.out.println(message);
 		MessageDialog.openInformation(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), "info", message);
 		
 	}
 	
 }
