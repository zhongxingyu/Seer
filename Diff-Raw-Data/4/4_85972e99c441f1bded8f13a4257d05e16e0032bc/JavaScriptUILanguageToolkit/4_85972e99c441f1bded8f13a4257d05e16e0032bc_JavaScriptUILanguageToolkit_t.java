 /*******************************************************************************
  * Copyright (c) 2005, 2007 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  
  *******************************************************************************/
 package org.eclipse.dltk.javascript.internal.ui;
 
 import org.eclipse.dltk.core.IDLTKLanguageToolkit;
 import org.eclipse.dltk.javascript.core.IJavaScriptConstants;
 import org.eclipse.dltk.javascript.core.JavaScriptLanguageToolkit;
 import org.eclipse.dltk.ui.IDLTKUILanguageToolkit;
 import org.eclipse.dltk.ui.ScriptElementLabels;
 import org.eclipse.jface.dialogs.IDialogSettings;
 import org.eclipse.jface.preference.IPreferenceStore;
 
 public class JavaScriptUILanguageToolkit implements IDLTKUILanguageToolkit {
 	private static ScriptElementLabels sInstance = new ScriptElementLabels() {};
 
 	public ScriptElementLabels getScriptElementLabels() {
 		return sInstance;
 	}
 
 	public IPreferenceStore getPreferenceStore() {
 		return JavaScriptUI.getDefault().getPreferenceStore();
 	}
 
 	public IDLTKLanguageToolkit getCoreToolkit() {
 		return JavaScriptLanguageToolkit.getDefault();
 	}
 
 	public IDialogSettings getDialogSettings() {
 		return JavaScriptUI.getDefault().getDialogSettings();
 	}
 	public String getEditorID(Object inputElement) {
 		return "org.eclipse.dltk.javascript.ui.editor.JavascriptEditor";
 	}
 	public String getPartitioningID() {
 		return IJavaScriptConstants.JS_PARTITIONING;
 	}

	public String getInterpreterContainerID() {
		return "org.eclipse.dltk.javascript.launching.INTERPRETER_CONTAINER";
	}
 }
