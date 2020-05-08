 /**
  * 
  */
 package org.eclipse.dltk.rhino.dbgp;
 
 import java.util.HashMap;
 
 final class EvalCommand extends DBGPDebugger.Command {
 	/**
 	 * 
 	 */
 	private final DBGPDebugger debugger;
 
 	/**
 	 * @param debugger
 	 */
 	EvalCommand(DBGPDebugger debugger) {
 		this.debugger = debugger;
 	}
 
 	void parseAndExecute(String command, HashMap options) {
 		String value = Base64Helper.decodeString((String) options.get("--"));
 		if (value.length() == 0)
 			value = "this";
 		StringBuffer valueBuffer = new StringBuffer();
 		if (this.debugger.stackmanager.getStackDepth() == 0 || value == null) {
 			this.debugger.printProperty(value, value, "", valueBuffer, 0, true);
 			this.debugger.printResponse("<response command=\"eval\"\r\n"
 					+ " transaction_id=\"" + options.get("-i")
 					+ "\" success=\"1\" " + ">\r\n" + valueBuffer
 					+ "</response>\r\n" + "");
 			return;
 		}
		Object evaluated = "<error evaluating>";
		try {
			DBGPDebugFrame fr = this.debugger.stackmanager.getStackFrame(0);
			evaluated = fr.eval(value);
		} catch (Exception e) {
			e.printStackTrace();
		}
 		String shName = value;
 		int k = shName.lastIndexOf('.');
 		if (k != -1) {
 			shName = shName.substring(k + 1);
 		}
 		this.debugger.printProperty(shName, value, evaluated, valueBuffer, 0,
 				true);
 		this.debugger.printResponse("<response command=\"eval\"\r\n"
 				+ " transaction_id=\"" + options.get("-i")
 				+ "\" success=\"1\" " + ">\r\n" + valueBuffer
 				+ "</response>\r\n" + "");
 	}
 }
