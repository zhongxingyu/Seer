 /*******************************************************************************
  * Copyright (c) 2005, 2007 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  ******************************************************************************/
 
 package org.eclipse.dltk.python.activestatedebugger.preferences;
 
 import org.eclipse.core.resources.IProject;
 import org.eclipse.dltk.debug.ui.preferences.ExternalDebuggingEngineOptionsBlock;
 import org.eclipse.dltk.python.activestatedebugger.PythonActiveStateDebuggerConstants;
 import org.eclipse.dltk.python.activestatedebugger.PythonActiveStateDebuggerPlugin;
 import org.eclipse.dltk.ui.preferences.AbstractConfigurationBlockPropertyAndPreferencePage;
 import org.eclipse.dltk.ui.preferences.AbstractOptionsBlock;
 import org.eclipse.dltk.ui.preferences.PreferenceKey;
 import org.eclipse.dltk.ui.util.IStatusChangeListener;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.ui.preferences.IWorkbenchPreferenceContainer;
 
 /**
  * Python ActiveState debugging engine preference page
  */
 public class PythonActiveStateDebuggerPreferencePage extends
 		AbstractConfigurationBlockPropertyAndPreferencePage {
 
 	static PreferenceKey ENGINE_PATH = new PreferenceKey(
 			PythonActiveStateDebuggerPlugin.PLUGIN_ID,
 			PythonActiveStateDebuggerConstants.DEBUGGING_ENGINE_PATH_KEY);
 
	private static String PREFERENCE_PAGE_ID = "org.eclipse.dltk.python.preferences.debug.engines.activestatedebugger";
	private static String PROPERTY_PAGE_ID = "org.eclipse.dltk.python.propertyPage.debug.engines.activestatedebugger";
 
 	protected AbstractOptionsBlock createOptionsBlock(
 			IStatusChangeListener newStatusChangedListener, IProject project,
 			IWorkbenchPreferenceContainer container) {
 
 		return new ExternalDebuggingEngineOptionsBlock(
 				newStatusChangedListener, project,
 				new PreferenceKey[] { ENGINE_PATH }, container) {
 
 			protected void createEngineBlock(Composite parent) {
 				super.createEngineBlock(parent);
 				addDownloadLink(parent,
 						PreferenceMessages.DebuggingEngineDownloadPage,
 						PreferenceMessages.DebuggingEngineDownloadPageLink);
 			}
 
 			protected PreferenceKey getDebuggingEnginePathKey() {
 				return ENGINE_PATH;
 			}
 		};
 	}
 
 	/*
 	 * @see org.eclipse.dltk.ui.preferences.AbstractConfigurationBlockPropertyAndPreferencePage#getHelpId()
 	 */
 	protected String getHelpId() {
 		return null;
 	}
 
 	/*
 	 * @see org.eclipse.dltk.internal.ui.preferences.PropertyAndPreferencePage#getPreferencePageId()
 	 */
 	protected String getPreferencePageId() {
 		return PREFERENCE_PAGE_ID;
 	}
 
 	/*
 	 * @see org.eclipse.dltk.ui.preferences.AbstractConfigurationBlockPropertyAndPreferencePage#getProjectHelpId()
 	 */
 	protected String getProjectHelpId() {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	/*
 	 * @see org.eclipse.dltk.internal.ui.preferences.PropertyAndPreferencePage#getPropertyPageId()
 	 */
 	protected String getPropertyPageId() {
 		return PROPERTY_PAGE_ID;
 	}
 
 	/*
 	 * @see org.eclipse.dltk.ui.preferences.AbstractConfigurationBlockPropertyAndPreferencePage#setDescription()
 	 */
 	protected void setDescription() {
 		setDescription(PreferenceMessages.DebuggingEngineDescription);
 	}
 
 	/*
 	 * @see org.eclipse.dltk.ui.preferences.AbstractConfigurationBlockPropertyAndPreferencePage#setPreferenceStore()
 	 */
 	protected void setPreferenceStore() {
 		setPreferenceStore(PythonActiveStateDebuggerPlugin.getDefault()
 				.getPreferenceStore());
 	}
 }
