 package org.zend.php.zendserver.monitor.internal.core;
 
 import org.eclipse.osgi.util.NLS;
 
 public class Messages extends NLS {
 	private static final String BUNDLE_NAME = "org.zend.php.zendserver.monitor.internal.core.messages"; //$NON-NLS-1$
 	public static String AbstractMonitor_EnablingJobName;
	public static String AbstractMonitor_InitializationJobConnectionError;
 	public static String TargetMonitor_JobName;
 	public static String ZendServerMonitor_JobTitle;
 	public static String ZendServerMonitor_TaskTitle;
 	static {
 		// initialize resource bundle
 		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
 	}
 
 	private Messages() {
 	}
 }
