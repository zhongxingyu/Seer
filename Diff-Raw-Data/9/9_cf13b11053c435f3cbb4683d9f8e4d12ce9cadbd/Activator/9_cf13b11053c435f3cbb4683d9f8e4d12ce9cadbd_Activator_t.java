 package org.zend.php.zendserver.deployment.ui;
 
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.jface.resource.ImageDescriptor;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.graphics.GC;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IStartup;
 import org.eclipse.ui.forms.FormColors;
 import org.eclipse.ui.plugin.AbstractUIPlugin;
 import org.osgi.framework.BundleContext;
 import org.zend.php.zendserver.deployment.core.DeploymentCore;
 import org.zend.php.zendserver.deployment.ui.chrome.SocketCommandListener;
 
 /**
  * The activator class controls the plug-in life cycle
  */
public class Activator extends AbstractUIPlugin implements IStartup {
 
 	// The plug-in ID
 	public static final String PLUGIN_ID = "org.zend.php.zendserver.deployment.ui"; //$NON-NLS-1$
 
 	public static final String IMAGE_RUN_APPLICATION = "icons/obj16/run_exc.gif"; //$NON-NLS-1$
 	public static final String IMAGE_DEBUG_APPLICATION = "icons/obj16/debug_exc.gif"; //$NON-NLS-1$
 	public static final String IMAGE_RUN_TEST = "icons/obj16/test_application.gif"; //$NON-NLS-1$
 	public static final String IMAGE_EXPORT_APPLICATION = "icons/obj16/bundle_obj.gif"; //$NON-NLS-1$
 
 	public static final String IMAGE_DESCRIPTOR_OVERVIEW = "icons/obj16/overview_obj.gif"; //$NON-NLS-1$
 	public static final String IMAGE_DESCRIPTOR_PARAMETERS = "icons/sample.gif"; //$NON-NLS-1$
 	public static final String IMAGE_DESCRIPTOR_PREREQS = "icons/sample.gif"; //$NON-NLS-1$
 	public static final String IMAGE_DESCRIPTOR_REMOVAL = "icons/sample.gif"; //$NON-NLS-1$
 	public static final String IMAGE_DESCRIPTOR_VARIABLES = "icons/sample.gif"; //$NON-NLS-1$
 
 	public static final String IMAGE_PHP = "icons/obj16/dependency_obj.gif"; //$NON-NLS-1$
 	public static final String IMAGE_PHP_EXTENSION = "icons/obj16/dependency_obj.gif"; //$NON-NLS-1$
 	public static final String IMAGE_PHP_DIRECTIVE = "icons/obj16/dependency_obj.gif"; //$NON-NLS-1$
 	public static final String IMAGE_ZENDSERVER = "icons/obj16/dependency_obj.gif"; //$NON-NLS-1$
 	public static final String IMAGE_ZENDFRAMEWORK = "icons/obj16/dependency_obj.gif"; //$NON-NLS-1$
 	public static final String IMAGE_ZENDSERVERCOMPONENT = "icons/obj16/dependency_obj.gif"; //$NON-NLS-1$
 	public static final String IMAGE_WIZBAN_DEP = "icons/wizban/newdep_wiz.png"; //$NON-NLS-1$
 	
 	
 	public static final String IMAGE_PARAMTYPE_PASSWORD = "icons/obj16/category_obj.gif"; //$NON-NLS-1$
 	public static final String IMAGE_PARAMTYPE_STRING = "icons/obj16/category_obj.gif"; //$NON-NLS-1$
 	public static final String IMAGE_PARAMTYPE_NUMBER = "icons/obj16/category_obj.gif"; //$NON-NLS-1$
 	public static final String IMAGE_PARAMTYPE_CHOICE = "icons/obj16/category_obj.gif"; //$NON-NLS-1$
 	public static final String IMAGE_PARAMTYPE_CHECKBOX = "icons/obj16/category_obj.gif"; //$NON-NLS-1$
 	public static final String IMAGE_PARAMTYPE_HOSTNAME = "icons/obj16/category_obj.gif"; //$NON-NLS-1$
 	public static final String IMAGE_PARAMTYPE_EMAIL = "icons/obj16/category_obj.gif"; //$NON-NLS-1$
 	public static final String IMAGE_PARAMTYPE_UNKNOWN = "icons/obj16/category_obj.gif"; //$NON-NLS-1$
 
 	public static final String IMAGE_DEPLOY_WIZARD = "icons/wizban/deploy_wiz.png"; //$NON-NLS-1$
 	public static final String IMAGE_EXPORT_WIZARD = "icons/wizban/export_wiz.png"; //$NON-NLS-1$
 
 	public static final String IMAGE_VARIABLE = "icons/obj16/category_obj.gif";; //$NON-NLS-1$
 
 	public static final String IMAGE_SCRIPT = "icons/obj16/script_obj.gif"; //$NON-NLS-1$
 	public static final String IMAGE_SCRIPT_NOTEXISTS = "icons/obj16/script_dis_obj.gif"; //$NON-NLS-1$
 	public static final String IMAGE_SCRIPT_TYPE = "icons/obj16/req_obj.gif"; //$NON-NLS-1$
 
 	public static final String IMAGE_DESCRIPTOR_HELP = "icons/obj16/help.gif"; //$NON-NLS-1$
 
 	public static final String IMAGE_TARGET = "icons/obj16/server.gif"; //$NON-NLS-1$
 	public static final String IMAGE_DETECT_TARGET = "icons/obj16/detect_target.gif"; //$NON-NLS-1$
 	public static final String IMAGE_ADD_TARGET = "icons/obj16/new_target.gif"; //$NON-NLS-1$
 	public static final String IMAGE_EDIT_TARGET = "icons/obj16/edit_target.gif"; //$NON-NLS-1$
 	public static final String IMAGE_REMOVE_TARGET = "icons/elcl16/rem_co.gif"; //$NON-NLS-1$
 
 	public static final String IMAGE_REMOVE_TARGET_DISABLED = "icons/dlcl16/rem_co.gif"; //$NON-NLS-1$
 
 	public static final String IMAGE_REFRESH = "icons/elcl16/refresh.gif"; //$NON-NLS-1$
 
 	public static final String IMAGE_APPLICATION = "icons/obj16/application.gif"; //$NON-NLS-1$
 
 	public static final String IMAGE_ZEND = "icons/obj64/zend.gif"; //$NON-NLS-1$
 
 	public static final String IMAGE_CLOUD = "icons/obj64/cloud.gif"; //$NON-NLS-1$
 
 	public static final String IMAGE_DETECT = "icons/obj64/detect.gif"; //$NON-NLS-1$
 
 	// The shared instance
 	private static Activator plugin;
 
 	private FormColors formColors;
 
 	private SocketCommandListener socketCommand;
 
 	/**
 	 * The constructor
 	 */
 	public Activator() {
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext
 	 * )
 	 */
 	public void start(BundleContext context) throws Exception {
 		super.start(context);
 		plugin = this;
 		
 		DeploymentCore.getDefault().getSdk().isInstalled();
 		
 		socketCommand = new SocketCommandListener();
 		socketCommand.start();
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext
 	 * )
 	 */
 	public void stop(BundleContext context) throws Exception {
 		plugin = null;
 		super.stop(context);
 		
 		socketCommand.stop();
 	}
 
 	/**
 	 * Returns the shared instance
 	 * 
 	 * @return the shared instance
 	 */
 	public static Activator getDefault() {
 		return plugin;
 	}
 
 	/**
 	 * Returns an image descriptor for the image file at the given plug-in
 	 * relative path
 	 * 
 	 * @param path
 	 *            the path
 	 * @return the image descriptor
 	 */
 	public static ImageDescriptor getImageDescriptor(String path) {
 		return imageDescriptorFromPlugin(PLUGIN_ID, path);
 	}
 
 	public FormColors getFormColors(Display display) {
 		if (formColors == null) {
 			formColors = new FormColors(display);
 			formColors.markShared();
 		}
 		return formColors;
 	}
 
 	public Image getImage(String path) {
 		Image image = getImageRegistry().get(path);
 		if (image == null) {
 			getImageRegistry().put(path, getImageDescriptor(path));
 			image = getImageRegistry().get(path);
 		}
 
 		return image;
 	}
 
 	public Image createWorkspaceImage(String path, int height) {
 		Image image = null;
 		ImageDescriptor id = ImageDescriptor.createFromFile(null, path);
 		if (id != null) {
 			int origH = id.getImageData().height;
 			int origW = id.getImageData().width;
 			int width = origW * height / origH;
 			Image tmpImage = id.createImage();
 			image = resize(tmpImage, width, height);
 		}
 
 		return image;
 	}
 
 	private Image resize(Image image, int width, int height) {
 		Image scaled = new Image(Display.getDefault(), width, height);
 		GC gc = new GC(scaled);
 		gc.setAntialias(SWT.ON);
 		gc.setInterpolation(SWT.HIGH);
 		gc.drawImage(image, 0, 0, image.getBounds().width,
 				image.getBounds().height, 0, 0, width, height);
 		gc.dispose();
 		image.dispose(); // don't forget about me!
 		return scaled;
 	}
 
 	public static void log(Throwable e) {
 		getDefault().getLog().log(new Status(IStatus.ERROR, PLUGIN_ID, e.getMessage(), e));
 	}

	public void earlyStartup() {
		// empty. all the startup logic that we want to run is in Activator.start method.
	}
 }
