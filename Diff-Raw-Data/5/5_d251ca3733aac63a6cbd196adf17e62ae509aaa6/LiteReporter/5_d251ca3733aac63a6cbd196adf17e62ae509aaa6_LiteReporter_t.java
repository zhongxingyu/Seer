 package org.lite.js;
 
 import org.mozilla.javascript.ErrorReporter;
 import org.mozilla.javascript.EvaluatorException;
 
 public class LiteReporter implements ErrorReporter{
	public static int errorCount = 0;
 	public void warning(String message, String sourceName, int line, String lineSource, int lineOffset) {
		errorCount ++;
 		if (line < 0) {
 			System.err.println("[WARNING]" + message);
 		} else {
 			System.err.println("[WARNING]" + line + ":" + lineOffset + "%_%" + message + "%_%" + lineSource);
 		}
 	}
 
 	public void error(String message, String sourceName,	int line, String lineSource, int lineOffset) {
		errorCount ++;
 		if (line < 0) {
 			System.err.println("[ERROR_PACKER]" + message );
 		} else {
 			System.err.println("[ERROR_PACKER]" + line + ":" + lineOffset + "%_%" + message+"%_%" + lineSource);
 		}
 	}
 
 	public EvaluatorException runtimeError(String message, String sourceName,int line, String lineSource, int lineOffset) {
 		error(message, sourceName, line, lineSource, lineOffset);
 		return new EvaluatorException(message);
 	}
 }
