 package org.rubypeople.rdt.internal.core;
 
 import java.util.MissingResourceException;
 import java.util.ResourceBundle;
 
 import org.eclipse.core.resources.IWorkspace;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.IPluginDescriptor;
 import org.eclipse.core.runtime.Plugin;
 
 public class RubyPlugin extends Plugin {
 	public final static String PLUGIN_ID = "org.rubypeople.rdt";
	public final static String RUBY_NATURE_ID = "rubynature";
 
 	protected static RubyPlugin plugin;
 	protected ResourceBundle resourceBundle;
 	
 	public RubyPlugin(IPluginDescriptor descriptor) {
 		super(descriptor);
 		plugin = this;
 		try {
			resourceBundle= ResourceBundle.getBundle("org.rubypeople.eclipse.rdt.RubyPluginResources");
 		} catch (MissingResourceException x) {
 			resourceBundle = null;
 		}
 	}
 
 	public static RubyPlugin getDefault() {
 		return plugin;
 	}
 
 	public static IWorkspace getWorkspace() {
 		return ResourcesPlugin.getWorkspace();
 	}
 
 	public static String getResourceString(String key) {
 		ResourceBundle bundle= RubyPlugin.getDefault().getResourceBundle();
 		try {
 			return bundle.getString(key);
		} catch (MissingResourceException e) {
 			return key;
 		}
 	}
 
 	public ResourceBundle getResourceBundle() {
 		return resourceBundle;
 	}
 }
