 /*******************************************************************************
  * Copyright (c) 2012 Wind River Systems, Inc. and others. All rights reserved.
  * This program and the accompanying materials are made available under the terms
  * of the Eclipse Public License v1.0 which accompanies this distribution, and is
  * available at http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  * Wind River Systems - initial API and implementation
  *******************************************************************************/
 package org.eclipse.tcf.internal.cdt.ui;
 
 import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
 import org.eclipse.debug.internal.ui.DebugUIPlugin;
 import org.eclipse.debug.internal.ui.IInternalDebugUIConstants;
 import org.eclipse.jface.preference.IPreferenceStore;
 import org.eclipse.ui.IStartup;
 import org.eclipse.ui.PlatformUI;
 
 /**
  * Set the default TCF CDT plugin preferences
  */
 @SuppressWarnings("restriction")
 public class StartupPreferencesInitializer extends AbstractPreferenceInitializer implements IStartup {
 
     /* (non-Javadoc)
      * @see org.eclipse.ui.IStartup#earlyStartup()
      */
     public void earlyStartup() {
         PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable() {
 
             public void run() {
                 initializeDefaultPreferences();
             }
         });
     }
 
     /* (non-Javadoc)
      * @see org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer#initializeDefaultPreferences()
      */
     @Override
     public void initializeDefaultPreferences() {
 
         // "TCF Remote Application" launch is hidden by default.
         // No longer supported or maintained.
         IPreferenceStore store = DebugUIPlugin.getDefault().getPreferenceStore();
         if (store != null) {
            store.setValue(IInternalDebugUIConstants.PREF_FILTER_LAUNCH_TYPES, true);
 
             boolean added = false;
             String typeId = "org.eclipse.tcf.cdt.launch.remoteApplicationLaunchType"; //$NON-NLS-1$
 
            String typeList = store.getString(IInternalDebugUIConstants.PREF_FILTER_TYPE_LIST);
            if ("".equals(typeList)) typeList = store.getDefaultString(IInternalDebugUIConstants.PREF_FILTER_TYPE_LIST);
             if ("".equals(typeList)) {
                 typeList = typeId;
                 added = true;
             } else if (!typeList.contains(typeId)) {
                 typeList = typeList + "," + typeId; //$NON-NLS-1$
                 added = true;
             }
             if (added) {
                store.putValue(IInternalDebugUIConstants.PREF_FILTER_TYPE_LIST, typeList);
             }
         }
     }
 
 }
