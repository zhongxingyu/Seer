 package org.dawnsci.plotting.tools;
 
import org.dawnsci.plotting.api.preferences.PlottingConstants;
 import org.eclipse.core.runtime.ILog;
 import org.eclipse.core.runtime.preferences.InstanceScope;
 import org.eclipse.jface.preference.IPreferenceStore;
 import org.eclipse.jface.resource.ImageDescriptor;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.ui.plugin.AbstractUIPlugin;
 import org.eclipse.ui.preferences.ScopedPreferenceStore;
 import org.osgi.framework.BundleContext;
 
 public class Activator extends AbstractUIPlugin {
 
     public static final String PLUGIN_ID = "org.dawnsci.plotting.tools";
     
 	private static Activator staticActivator;
 	public void start(BundleContext context) throws Exception {
 		super.start(context);
         staticActivator = this;
        
        // This line is required such that preference initializer extensions
        // are processed for this plugin.
        final int nfreds = this.getPreferenceStore().getInt("fred");
     }
 	
     public static ImageDescriptor getImageDescriptor(String path) {
         return imageDescriptorFromPlugin("org.dawnsci.plotting.tools", path);
     }
     		 
     public static Image getImage(String path) {
         return getImageDescriptor(path).createImage();
     }
 
     private static IPreferenceStore plottingPreferences;
 	public static IPreferenceStore getPlottingPreferenceStore() {
 		if (plottingPreferences!=null) return plottingPreferences;
 		plottingPreferences = new ScopedPreferenceStore(InstanceScope.INSTANCE, "org.dawnsci.plotting");
 		return plottingPreferences;
 	}
     		 
 	public static ILog getPluginLog() {
 		return staticActivator.getLog();
 	}
 }
