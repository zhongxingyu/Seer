 package de.fkoeberle.autocommit.message;
 
 import org.eclipse.core.runtime.Plugin;
import org.osgi.framework.BundleContext;
 
 public class CommitMessageBuilderPluginActivator extends Plugin {
 	public static final String PLUGIN_ID = "de.fkoeberle.autocommit.message"; //$NON-NLS-1$
 	private static CommitMessageBuilderPluginActivator plugin;
 	
 	public CommitMessageBuilderPluginActivator() {
 	}
 
 	@Override
 	public void start(BundleContext context) throws Exception {
 		super.start(context);
 		plugin = this;
 	}
 
 	@Override
 	public void stop(BundleContext context) throws Exception {
 		plugin = null;
 		super.stop(context);
 	}
 
 	/**
 	 * Returns the shared instance
 	 *
 	 * @return the shared instance
 	 */
 	public static CommitMessageBuilderPluginActivator getDefault() {
 		return plugin;
 	}
 	
 	public ICommitMessageBuilder createBuilder() {
 		return new CompleteContentCommitMessageBuilder();
 	}
 
 }
