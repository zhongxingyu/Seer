 /*
  * Copyright (c) 2011, Karl Trygve Kalleberg <karltk at strategoxt dot org>
  *
  * Licensed under the GNU Lesser General Public License, v2.1
  */
 package org.spoofax.interpreter.ui;
 
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.ui.plugin.AbstractUIPlugin;
 
 public class InterpreterPlugin extends AbstractUIPlugin {
 	
 	public static final boolean DEBUG_MODE = InterpreterPlugin.class.desiredAssertionStatus();
 
 	private static InterpreterPlugin instance;
 
 	public static InterpreterPlugin instance() {
 		if(instance == null) {
 			instance = new InterpreterPlugin();
 		}
 		return instance;
 	}
 	
 	public static void logError(String message, Throwable t) {
 		if (DEBUG_MODE) {
 			if (message != null) 
 				System.err.println(message);
 			t.printStackTrace();
 		}
 
 		if (message == null) 
 			message = t.getLocalizedMessage() == null ? t.getMessage() : t.getLocalizedMessage();
		
		if(instance() != null && instance().getBundle() != null)
			instance().getLog().log(new SpoofaxStatus(IStatus.ERROR, 0, message, t));
 	}
 	
 	private static class SpoofaxStatus extends Status {
 		SpoofaxStatus(int severity, int code, String message, Throwable exception) {
 			super(severity, "org.spoofax.interpreter.ui", code, message, exception);
 		}
 	}
 }
