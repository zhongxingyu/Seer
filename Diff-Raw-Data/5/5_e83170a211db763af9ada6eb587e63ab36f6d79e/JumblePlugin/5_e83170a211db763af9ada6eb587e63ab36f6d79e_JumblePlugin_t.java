 package jumble;
 
 import java.io.File;
 import java.io.IOException;
 import java.net.URL;
 
import org.eclipse.core.runtime.FileLocator;
 import org.eclipse.jface.resource.ImageDescriptor;
 import org.eclipse.ui.plugin.AbstractUIPlugin;
 import org.osgi.framework.BundleContext;
 
 /**
  * The main plugin class to be used in the desktop.
  */
 public class JumblePlugin extends AbstractUIPlugin {
 
 	//The shared instance.
 	private static JumblePlugin plugin;
 	
 	/**a
 	 * The constructor.
 	 */
 	public JumblePlugin() {
 		plugin = this;
 	}
 
 	/**
 	 * This method is called upon plug-in activation
 	 */
 	public void start(BundleContext context) throws Exception {
 		super.start(context);
 	}
 
 	/**
 	 * This method is called when the plug-in is stopped
 	 */
 	public void stop(BundleContext context) throws Exception {
 		super.stop(context);
 		plugin = null;
 	}
 
 	/**
 	 * Returns the shared instance.
 	 */
 	public static JumblePlugin getDefault() {
 		return plugin;
 	}
 
 	/**
 	 * Returns an image descriptor for the image file at the given
 	 * plug-in relative path.
 	 *
 	 * @param path the path
 	 * @return the image descriptor
 	 */
 	public static ImageDescriptor getImageDescriptor(String path) {
 		return AbstractUIPlugin.imageDescriptorFromPlugin("jumble", path);
 	}
   
   /**
    * Gets the installation location of the plugin.
   * @return The File Location of this plugin
   */
   public File getPluginFolder() throws IOException {
       URL url = getBundle().getEntry("/");
      url = FileLocator.toFileURL(url);
       return new File(url.getPath());
   }
 }
