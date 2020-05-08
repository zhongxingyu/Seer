 package org.eclipse.dltk.internal.core.builder;
 
 import org.eclipse.osgi.util.NLS;
 
 public class Messages extends NLS {
 	private static final String BUNDLE_NAME = "org.eclipse.dltk.internal.core.builder.messages"; //$NON-NLS-1$
 	public static String ScriptBuilder_building;
 	public static String ScriptBuilder_buildingScriptsIn;
 	public static String ScriptBuilder_locatingResourcesFor;
	public static String ScriptBuilder_Location_source_modules;
	public static String ScriptBuilder_Looking_into_folder;
 	public static String ScriptBuilder_scanningExternalResourcesFor;
 	public static String ScriptBuilder_scanningResourcesIn;
 	static {
 		// initialize resource bundle
 		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
 	}
 
 	private Messages() {
 	}
 }
