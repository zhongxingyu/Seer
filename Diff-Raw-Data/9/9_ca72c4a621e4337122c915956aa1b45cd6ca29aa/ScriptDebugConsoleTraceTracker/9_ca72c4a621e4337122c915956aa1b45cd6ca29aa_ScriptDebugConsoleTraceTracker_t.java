 package org.eclipse.dltk.debug.ui;
 
 import org.eclipse.jface.text.BadLocationException;
 import org.eclipse.ui.console.PatternMatchEvent;
 
 public class ScriptDebugConsoleTraceTracker extends ScriptDebugConsoleTracker {
 
 	public void matchFound(PatternMatchEvent event) {
 		try {
 			int offset = event.getOffset();
 			int length = event.getLength();
 			ScriptDebuggerConsoleToFileHyperlink link = new ScriptDebuggerConsoleToFileHyperlink(
 					console);
 			console.addHyperlink(link, link.computeOffset(offset, length,
 					console), link.computeLength(offset, length, console));
 
 		} catch (BadLocationException e) {
 		}
 	}
 
 	public String getPattern() {
		return "\\t*#\\d+ +file:(.*) \\[(\\d+)\\]";
 	}
 
 }
