 package org.eclipse.dltk.javascript.internal.console.ui;
 
 import org.eclipse.dltk.console.IScriptInterpreter;
 import org.eclipse.dltk.console.ScriptConsolePrompt;
 import org.eclipse.dltk.console.ui.IScriptConsoleFactory;
 import org.eclipse.dltk.console.ui.ScriptConsole;
 import org.eclipse.dltk.console.ui.ScriptConsoleFactoryBase;
 import org.eclipse.dltk.javascript.console.JavaScriptConsoleConstants;
 import org.eclipse.dltk.javascript.console.JavaScriptConsoleUtil;
 import org.eclipse.dltk.javascript.console.JavaScriptInterpreter;
 import org.eclipse.dltk.javascript.internal.debug.ui.JavaScriptDebugUIPlugin;
 import org.eclipse.jface.preference.IPreferenceStore;
 
 
 public class JavaScriptConsoleFactory extends ScriptConsoleFactoryBase implements
 		IScriptConsoleFactory {
 	protected IPreferenceStore getPreferenceStore() {
 		return JavaScriptDebugUIPlugin.getDefault().getPreferenceStore();
 	}
 
 	protected ScriptConsolePrompt makeInvitation() {
 		IPreferenceStore store = getPreferenceStore();
 		return new ScriptConsolePrompt(store
 				.getString(JavaScriptConsoleConstants.PREF_NEW_PROMPT), store
 				.getString(JavaScriptConsoleConstants.PREF_CONTINUE_PROMPT));
 	}
 
 	protected JavaScriptConsole makeConsole(JavaScriptInterpreter interpreter, String id) {
 		JavaScriptConsole console = new JavaScriptConsole(interpreter, id);
 		console.setPrompt(makeInvitation());
 		return console;
 	}
 
 	private JavaScriptConsole createConsoleInstance(IScriptInterpreter interpreter, String id) {
 		if (interpreter == null) {
 			try {
 				id = "default";
 				interpreter = new JavaScriptInterpreter();
 				JavaScriptConsoleUtil
 						.runDefaultTclInterpreter((JavaScriptInterpreter) interpreter);
 			} catch (Exception e) {
 				return null;
 			}
 		}
 
 		return makeConsole((JavaScriptInterpreter) interpreter, id);
 	}
 
 	protected ScriptConsole createConsoleInstance() {
 		return createConsoleInstance(null, null);
 	}
 
 	public JavaScriptConsoleFactory() {
 	}
 
	public void openConsole(IScriptInterpreter interpreter, String id) {
 		registerAndOpenConsole(createConsoleInstance(interpreter, id));
 	}
 }
