 package org.eclipse.dltk.javascript.internal.debug.ui;
 
 import org.eclipse.dltk.debug.ui.AbstractDebugUILanguageToolkit;
 import org.eclipse.dltk.javascript.internal.debug.JavaScriptDebugConstants;
 import org.eclipse.jface.preference.IPreferenceStore;
 
 public class JavaScriptDebugUILanguageToolkit extends
 		AbstractDebugUILanguageToolkit {
 
 	/*
 	 * @see
 	 * org.eclipse.dltk.debug.ui.IDLTKDebugUILanguageToolkit#getDebugModelId()
 	 */
 	public String getDebugModelId() {
 		return JavaScriptDebugConstants.DEBUG_MODEL_ID;
 	}
 
 	/*
 	 * @see
 	 * org.eclipse.dltk.debug.ui.IDLTKDebugUILanguageToolkit#getPreferenceStore
 	 * ()
 	 */
 	public IPreferenceStore getPreferenceStore() {
 		return JavaScriptDebugUIPlugin.getDefault().getPreferenceStore();
 	}

	public String[] getVariablesViewPreferencePages() {
		return new String[] { "org.eclipse.dltk.javascript.preferences.debug.detailFormatters" };
	}

 }
