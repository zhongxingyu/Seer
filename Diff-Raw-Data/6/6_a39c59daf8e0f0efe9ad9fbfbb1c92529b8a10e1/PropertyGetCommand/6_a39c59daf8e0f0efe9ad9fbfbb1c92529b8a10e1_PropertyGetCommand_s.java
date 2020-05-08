 /**
  * 
  */
 package org.eclipse.dltk.rhino.dbgp;
 
 import java.util.HashMap;
 
 final class PropertyGetCommand extends DBGPDebugger.Command {
 	/**
 	 * 
 	 */
 	private final DBGPDebugger debugger;
 
 	/**
 	 * @param debugger
 	 */
 	PropertyGetCommand(DBGPDebugger debugger) {
 		this.debugger = debugger;
 	}
 
 	void parseAndExecute(String command, HashMap options) {
 		String longName = (String) options.get("-n");
 		int level = 0;
 		String depth = (String) options.get("-d");
 		if (depth != null) {
 			level = Integer.parseInt(depth);
 		}
 		Object value = null;
 		int shName = longName.indexOf('.');
		if (shName == -1)
			shName = longName.length();
		String shortName = longName.substring(0, shName);
 		StringBuffer properties = new StringBuffer();
 		DBGPDebugFrame stackFrame = this.debugger.stackmanager
 				.getStackFrame(level);
 		if (stackFrame != null) {
 			value = stackFrame.getValue(longName);
 		}
 		this.debugger.printProperty(shortName, longName, value, properties, 0,
 				true);
 		this.debugger.printResponse("<response command=\"property_get\"\r\n"
 				+ " transaction_id=\"" + options.get("-i") + "\">\r\n"
 				+ properties + "</response>\r\n" + "");
 	}
 }
