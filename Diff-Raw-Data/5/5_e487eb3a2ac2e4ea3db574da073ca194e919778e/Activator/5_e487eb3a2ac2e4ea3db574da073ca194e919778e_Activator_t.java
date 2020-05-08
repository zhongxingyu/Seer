 package org.meta_environment.eclipse.sdf;
 
 import org.eclipse.imp.runtime.PluginBase;
 import org.eclipse.ui.IStartup;
 
 public class Activator extends PluginBase implements IStartup {
 	public static final String kPluginID = "sdf_meta_eclipse";
 	public static final String kLanguageName = "SDF";
 	
 	public Activator(){
 		super();
 	}
 	
 	private static class InstanceKeeper{
 		public static Activator sPlugin = new Activator();
 	}
 
 	public static synchronized Activator getInstance(){
 		return InstanceKeeper.sPlugin;
 	}
 
 	public String getID() {
 		return kPluginID;
 	}
 
 	public void earlyStartup(){
 		getInstance();
 	}

	@Override
	public String getLanguageID() {
		return getID();
	}
 }
