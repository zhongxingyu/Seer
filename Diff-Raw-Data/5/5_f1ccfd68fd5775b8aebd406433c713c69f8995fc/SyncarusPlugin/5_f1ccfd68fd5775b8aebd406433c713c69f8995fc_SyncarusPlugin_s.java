 package net.syncarus.rcp;
 
 import java.io.File;
 import java.io.PrintWriter;
 import java.io.StringWriter;
 
 import net.syncarus.core.Settings;
 import net.syncarus.core.Protocol;
 import net.syncarus.gui.SyncView;
 import net.syncarus.model.DiffNode;
 
 import org.eclipse.core.runtime.ILog;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.jface.dialogs.IDialogConstants;
 import org.eclipse.jface.dialogs.MessageDialog;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.ui.plugin.AbstractUIPlugin;
 
 public class SyncarusPlugin extends AbstractUIPlugin {
 	public static final String PLUGIN_ID = "Syncarus";
 	private static SyncarusPlugin instance;
 	
 	private ResourceRegistry resourceRegistry;
 	private Protocol protocol = new Protocol();
 	private DiffNode rootDiffNode;
 	private Settings settings;
 
 	/**
 	 * Checks whether the root directories have been set (via
 	 * <code>initialise</code>) and exist
 	 * 
 	 * @return true on success, else false
 	 */
 	public boolean isInitialized() {
 		return rootDiffNode != null;
 	}
 
 	/**
 	 * Initialises the Controller by converting paths locations A and B to a
 	 * unique form which only has '/' as folder-separators. Also checks validity
 	 * of locations
 	 * 
 	 * @param rootADir
 	 * @param rootBDir
 	 */
 	public void initialize(File rootADir, File rootBDir) {
 		rootDiffNode = new DiffNode(rootADir, rootBDir);
 		SyncarusPlugin.getInstance().getProtocol().add("rootA='" + rootDiffNode.getAbsolutePathA() +
 				"', rootB='" + rootDiffNode.getAbsolutePathB() + "'");
 	}
 
 	/**
 	 * forget about old differentiation-results and provide a clean
 	 * <code>rootDiffNode</code> for a new differentiation process.
 	 */
 	public void resetRootNode() {
 		rootDiffNode = new DiffNode(rootDiffNode.getAbsoluteFileA(), rootDiffNode.getAbsoluteFileB());
 	}
 
 	/**
 	 * @return the current rootDiffNode or null, when {@link DiffController} hasn't
 	 *         been initialised.
 	 */
 	public DiffNode getRootNode() {
 		return rootDiffNode;
 	}
 	
 	public Settings getSettings() {
 		return settings;
 	}
 	
 	public static SyncarusPlugin getInstance() {
 		return instance;
 	}
 
 	public SyncarusPlugin() {
 		instance = this;
 	}
 
 	public ResourceRegistry getResourceRegistry() {
 		if (resourceRegistry == null)
 			resourceRegistry = new ResourceRegistry();
 		return resourceRegistry;
 	}
 	
 	public Protocol getProtocol() {
 		return protocol;
 	}
 	
 	public SyncView getSyncView() {
 		return (SyncView)getWorkbench().getActiveWorkbenchWindow().getActivePage().findView(SyncView.ID);
 	}
 
 	public static boolean isOSWindows() {
 		return System.getProperty("os.name").toLowerCase().startsWith("windows");
 	}
 
 	public void logError(String message, Throwable e) {
 		ILog logger = instance.getLog();
 		logger.log(new Status(IStatus.ERROR, PLUGIN_ID, message, e));
		StringWriter stringWriter = new StringWriter();
 		String errorText = "";
 		if (e != null) {
			e.printStackTrace(new PrintWriter(stringWriter));
			errorText = stringWriter.getBuffer().toString();
 		}
 
 		// show error within the context of the SWT thread
 		Runnable runnable = new Runnable() {
 			private String errorText;
 			private String errorTitle;
 
 			@Override
 			public void run() {
 				new MessageDialog(null, errorTitle, null, errorText, MessageDialog.ERROR,
 						new String[] { IDialogConstants.OK_LABEL }, 0) {
 					@Override
 					protected boolean isResizable() {
 						return true;
 					}
 				}.open();
 			}
 
 			Runnable setErrorText(String errorTitle, String errorText) {
 				this.errorTitle = errorTitle;
 				this.errorText = errorText;
 				return this;
 			}
 		}.setErrorText(message, errorText);
 
 		Display.getDefault().syncExec(runnable);
 	}
 
 	public void initSettings() {
 		settings = new Settings(getPreferenceStore());
 	}
 }
