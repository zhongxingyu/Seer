 /*******************************************************************************
  * Copyright (c) 2000, 2007 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  
  *******************************************************************************/
 package org.eclipse.dltk.ruby.internal.ui;
 
 import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
 import org.eclipse.dltk.ui.editor.highlighting.SemanticHighlightingUtils;
 import org.eclipse.jface.preference.IPreferenceStore;
 import org.eclipse.ui.editors.text.EditorsUI;
 
 public class RubyUIPreferenceInitializer extends AbstractPreferenceInitializer {
 
 	public void initializeDefaultPreferences() {
 		IPreferenceStore store = RubyUI.getDefault().getPreferenceStore();
 
 		EditorsUI.useAnnotationsPreferencePage(store);
 		EditorsUI.useQuickDiffPreferencePage(store);
 		RubyPreferenceConstants.initializeDefaultValues(store);
 		SemanticHighlightingUtils.initializeDefaultValues(store, RubyUI.getDefault()
 				.getTextTools().getSemanticHighlightings());
 	}
 }
