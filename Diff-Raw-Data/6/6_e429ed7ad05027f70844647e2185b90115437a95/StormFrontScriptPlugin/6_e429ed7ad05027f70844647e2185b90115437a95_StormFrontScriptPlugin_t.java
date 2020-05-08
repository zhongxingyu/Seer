 package cc.warlock.core.stormfront.script;
 
 import org.eclipse.core.runtime.Plugin;
 import org.osgi.framework.BundleContext;
 
 import cc.warlock.core.stormfront.script.javascript.StormFrontJavascriptVars;
 
 /**
  * The activator class controls the plug-in life cycle
  */
 public class StormFrontScriptPlugin extends Plugin {
 
 	// The plug-in ID
 	public static final String PLUGIN_ID = "cc.warlock.core.stormfront.script";
 
 	// The shared instance
 	private static StormFrontScriptPlugin plugin;
 	
 	/**
 	 * The constructor
 	 */
 	public StormFrontScriptPlugin() {
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * @see org.eclipse.core.runtime.Plugins#start(org.osgi.framework.BundleContext)
 	 */
 	public void start(BundleContext context) throws Exception {
 		super.start(context);
 		plugin = this;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * @see org.eclipse.core.runtime.Plugin#stop(org.osgi.framework.BundleContext)
 	 */
 	public void stop(BundleContext context) throws Exception {
 		plugin = null;
 		super.stop(context);
 	}
	
 	/**
 	 * Returns the shared instance
 	 *
 	 * @return the shared instance
 	 */
 	public static StormFrontScriptPlugin getDefault() {
 		return plugin;
 	}
 
 }
