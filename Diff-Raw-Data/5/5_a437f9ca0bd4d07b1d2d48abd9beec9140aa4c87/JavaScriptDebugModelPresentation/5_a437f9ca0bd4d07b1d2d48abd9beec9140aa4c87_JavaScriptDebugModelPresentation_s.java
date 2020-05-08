 package org.eclipse.dltk.javascript.internal.debug.ui;
 
 import org.eclipse.dltk.debug.core.model.IScriptThread;
 import org.eclipse.dltk.debug.ui.ScriptDebugModelPresentation;
 import org.eclipse.ui.IEditorInput;
 
 public class JavaScriptDebugModelPresentation extends ScriptDebugModelPresentation {
	private static final String TCL_EDITOR_ID = "org.eclipse.dltk.tcl.ui.editor.TclEditor";
 	
 	private static final String MAIN_THREAD_NAME = "Main thread";
 	
 	protected String getThreadText(IScriptThread thread) {
 		return MAIN_THREAD_NAME;
 	}
 	
 	public String getEditorId(IEditorInput input, Object element) {		
		return TCL_EDITOR_ID;
 	}
 }
