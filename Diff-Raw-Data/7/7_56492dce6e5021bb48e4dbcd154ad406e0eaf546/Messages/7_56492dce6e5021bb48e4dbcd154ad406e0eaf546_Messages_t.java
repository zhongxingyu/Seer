 package org.eclipse.dltk.javascript.internal.debug.ui;
 
 import org.eclipse.osgi.util.NLS;
 
 public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.dltk.javascript.internal.debug.ui.messages"; //$NON-NLS-1$

 	public static String NoDebuggingEngine_title;
 	public static String NoDebuggingEngine_message;

 	static {
 		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
 	}
 }
