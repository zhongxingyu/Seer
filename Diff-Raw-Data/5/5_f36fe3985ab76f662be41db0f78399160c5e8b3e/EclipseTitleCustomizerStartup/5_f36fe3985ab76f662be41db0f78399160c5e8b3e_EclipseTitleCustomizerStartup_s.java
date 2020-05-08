 package eclipsetitlecustomizer;
 
 import java.io.File;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.Properties;
 
 import org.eclipse.core.runtime.Platform;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.ui.IPartListener;
 import org.eclipse.ui.IPerspectiveDescriptor;
 import org.eclipse.ui.IPerspectiveListener;
 import org.eclipse.ui.IStartup;
 import org.eclipse.ui.IWorkbenchPage;
 import org.eclipse.ui.IWorkbenchPart;
 import org.eclipse.ui.IWorkbenchWindow;
 import org.eclipse.ui.PlatformUI;
 
 public class EclipseTitleCustomizerStartup implements IStartup {
 
 	private static final String DEFAULT_FORMAT = "[$installName] - ($wsLocation)";
 
 	private String customizedName;
 
 	private static final String INSTALL_NAME_KEY = "$installName";
 	private static final String WS_LOCATION_KEY = "$wsLocation";
 	private static final String WS_NAME_KEY = "$wsName";
 	private static final String TITLE_FORMAT_KEY = "titleFormat";
 
 	@Override
 	public void earlyStartup() {
 		initializeName();
 
 		Display.getDefault().syncExec(new Runnable() {
 			@Override
 			public void run() {
 				IWorkbenchWindow activeWorkbenchWindow = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
 				activeWorkbenchWindow.getShell().setText(customizedName);
 			}
 		});
 
 		Display.getDefault().asyncExec(new Runnable() {
 			@Override
 			public void run() {
 				addListeners();
 			}
 		});
 	}
 
 	private void updateName() {
 		Display.getDefault().asyncExec(new Runnable() {
 			@Override
 			public void run() {
				PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell().setText(customizedName);
 			}
 		});
 	}
 
 	private void initializeName() {
 		File installFile = new File(Platform.getInstallLocation().getURL().getPath());
 		File workspaceFile = new File(Platform.getInstanceLocation().getURL().getPath());
 
 		String titleFormat = null;
 
 		File customizerFile = new File(installFile, "title.properties");
 		Properties props = new Properties();
 
 		if (!customizerFile.exists()) {
 			String comments = "Use the titleFormat property to customize the format of the title. Restart the eclipse to have the changes reflected.\n";
 			comments = comments + "Supported keys and  which can be used in titleFormat property are listed below \n"
 					+ "1.$installName-Name of the installation folder\n" + "2.$wsLocation - location of workspace\n"
 					+ "3.$wsName - name of workspace\n";
 			comments = comments + "Each of the key will be replaced by appropriate value when customizing the title\n";
 			comments = comments
 					+ "'Eclipse_Testing - $wsName' is  a sample format which will be translated to  'Eclipse_Testing - <workspace name>'. \n\n";
 			props.put(TITLE_FORMAT_KEY, DEFAULT_FORMAT);
 			try {
 				FileWriter fileWriter = new FileWriter(customizerFile);
 				try {
 					props.store(fileWriter, comments);
 				} finally {
 					fileWriter.close();
 				}
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 		} else {
 			try {
 				FileReader reader = new FileReader(customizerFile);
 				try {
 					props.load(reader);
 					if (props.containsKey(TITLE_FORMAT_KEY)) {
 						titleFormat = (String) props.get(TITLE_FORMAT_KEY);
 					}
 				} finally {
 					reader.close();
 				}
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 		}
 
 		if (titleFormat == null || titleFormat.trim().isEmpty()) {
 			titleFormat = DEFAULT_FORMAT;
 		}
 
 		customizedName = titleFormat.replace(INSTALL_NAME_KEY, installFile.getName());
 		customizedName = customizedName.replace(WS_LOCATION_KEY, workspaceFile.getPath());
 		customizedName = customizedName.replace(WS_NAME_KEY, workspaceFile.getName());
 	}
 
 	private void addListeners() {
 		IWorkbenchWindow activeWorkbenchWindow = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
 		activeWorkbenchWindow.addPerspectiveListener(new IPerspectiveListener() {
 
 			@Override
 			public void perspectiveChanged(IWorkbenchPage page, IPerspectiveDescriptor perspective, String changeId) {
 
 			}
 
 			@Override
 			public void perspectiveActivated(IWorkbenchPage page, IPerspectiveDescriptor perspective) {
 				updateName();
 			}
 		});
 
 		activeWorkbenchWindow.getActivePage().addPartListener(new IPartListener() {
 
 			@Override
 			public void partOpened(IWorkbenchPart part) {
 
 			}
 
 			@Override
 			public void partDeactivated(IWorkbenchPart part) {
 				updateName();
 			}
 
 			@Override
 			public void partClosed(IWorkbenchPart part) {
 
 			}
 
 			@Override
 			public void partBroughtToTop(final IWorkbenchPart part) {
 				updateName();
 			}
 
 			@Override
 			public void partActivated(final IWorkbenchPart part) {
 				updateName();
 			}
 		});
 	}
 
 }
