 package cc.warlock.rcp.stormfront;
 
 import org.eclipse.core.runtime.Platform;
 import org.eclipse.ui.plugin.AbstractUIPlugin;
 import org.osgi.framework.BundleContext;
 
 import cc.warlock.core.configuration.Profile;
 import cc.warlock.core.configuration.SavedProfiles;
 import cc.warlock.core.stormfront.client.internal.StormFrontClient;
import cc.warlock.core.stormfront.script.javascript.StormFrontJavascriptVars;
 import cc.warlock.rcp.application.WarlockApplication;
 import cc.warlock.rcp.plugin.Warlock2Plugin;
 import cc.warlock.rcp.stormfront.adapters.StormFrontClientAdapterFactory;
 import cc.warlock.rcp.stormfront.ui.actions.ProfileConnectAction;
 import cc.warlock.rcp.ui.client.WarlockClientAdaptable;
 
 /**
  * The activator class controls the plug-in life cycle
  */
 public class StormFrontRCPPlugin extends AbstractUIPlugin {
 
 	// The plug-in ID
 	public static final String PLUGIN_ID = "cc.warlock.rcp.stormfront";
 
 	// The shared instance
 	private static StormFrontRCPPlugin plugin;
 	
 	/**
 	 * The constructor
 	 */
 	public StormFrontRCPPlugin() {
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
 	 */
 	public void start(BundleContext context) throws Exception {
 		super.start(context);
 		plugin = this;
 		
		// i think this is the best place for this to go?
		new StormFrontJavascriptVars();
		
 		// force-load our initial client so we can do offline scripting
 		Warlock2Plugin.getDefault().addClient(new StormFrontClient());
 		
 		Platform.getAdapterManager().registerAdapters(new StormFrontClientAdapterFactory(), WarlockClientAdaptable.class);
 		
 		if (WarlockApplication.instance().getStartWithProfile() != null) {
 			Profile connectToProfile = null;
 			
 			for (Profile profile : SavedProfiles.getAllProfiles())
 			{
 				if (WarlockApplication.instance().getStartWithProfile().equals(profile.getCharacterName()))
 				{
 					connectToProfile = profile;
 				}
 			}
 			
 			if (connectToProfile == null) /* TODO show a warning */ return;
 			
 			ProfileConnectAction action = new ProfileConnectAction(connectToProfile);
 			action.run();
 		}
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
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
 	public static StormFrontRCPPlugin getDefault() {
 		return plugin;
 	}
 
 }
