 /*******************************************************************************
  * Copyright (c) 2007-2009 Exadel, Inc. and Red Hat, Inc.
  * Distributed under license by Red Hat, Inc. All rights reserved.
  * This program is made available under the terms of the
  * Eclipse Public License v1.0 which accompanies this distribution,
  * and is available at http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     Exadel, Inc. and Red Hat, Inc. - initial API and implementation
  ******************************************************************************/
 package org.jboss.tools.jst.jsp.preferences;
 
 import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
 import org.eclipse.core.runtime.preferences.DefaultScope;
 import org.eclipse.core.runtime.preferences.IEclipsePreferences;
 import org.eclipse.core.runtime.preferences.IScopeContext;
 import org.jboss.tools.jst.jsp.JspEditorPlugin;
 
 public class VpePreferencesInitializer extends AbstractPreferenceInitializer {
 
 	@Override
 	public void initializeDefaultPreferences() {
 		IEclipsePreferences defaultPreferences = ((IScopeContext) new DefaultScope()).getNode(JspEditorPlugin.PLUGIN_ID);
 		
 		defaultPreferences.putBoolean(IVpePreferencesPage.SHOW_BORDER_FOR_UNKNOWN_TAGS, true);
 		defaultPreferences.putBoolean(IVpePreferencesPage.SHOW_NON_VISUAL_TAGS, false);
 		defaultPreferences.putBoolean(IVpePreferencesPage.SHOW_SELECTION_TAG_BAR, true);
 		defaultPreferences.putBoolean(IVpePreferencesPage.SHOW_TEXT_FORMATTING, true);
 		defaultPreferences.putBoolean(IVpePreferencesPage.SHOW_RESOURCE_BUNDLES_USAGE_AS_EL, false);
 		defaultPreferences.putBoolean(IVpePreferencesPage.ASK_TAG_ATTRIBUTES_ON_TAG_INSERT, true);
 		defaultPreferences.putBoolean(IVpePreferencesPage.ASK_CONFIRMATION_ON_CLOSING_SELECTION_BAR, true);
		defaultPreferences.putBoolean(IVpePreferencesPage.IGNORE_VPE_WARNINGS, false);
 		defaultPreferences.put(IVpePreferencesPage.DEFAULT_VPE_TAB, IVpePreferencesPage.DEFAULT_VPE_TAB_VISUAL_SOURCE_VALUE);
 		defaultPreferences.put(IVpePreferencesPage.VISUAL_SOURCE_EDITORS_SPLITTING, IVpePreferencesPage.SPLITTING_VERT_TOP_SOURCE_VALUE);
 		defaultPreferences.putInt(IVpePreferencesPage.VISUAL_SOURCE_EDITORS_WEIGHTS, IVpePreferencesPage.DEFAULT_VISUAL_SOURCE_EDITORS_WEIGHTS);
 		
 	}
 
 }
