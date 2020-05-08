 /*******************************************************************************
  * Copyright (c) 2012 Wind River Systems, Inc. and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     Wind River Systems - initial API and implementation
  *******************************************************************************/
 package org.eclipse.tcf.internal.debug.ui.model;
 
 import java.util.ArrayList;
 
 import org.eclipse.core.runtime.IConfigurationElement;
 import org.eclipse.core.runtime.IExtension;
 import org.eclipse.core.runtime.IExtensionPoint;
 import org.eclipse.core.runtime.Platform;
 import org.eclipse.tcf.debug.ui.ITCFAnnotationProvider;
 import org.eclipse.tcf.internal.debug.ui.Activator;
 import org.osgi.framework.Bundle;
 
 /**
  * TCF clients can implement ITCFAnnotationProvider to manage debugger annotations
  * in the Eclipse workspace.
  *
  * Debugger annotations include editor markers for current instruction pointer,
  * stack frame addresses, and breakpoint planting status.
  *
  * TCF will use internal annotation provider if no suitable provider is found
  * through "annotation_provider" extension point for current selection in the Debug view.
  */
 public class TCFAnnotationProvider {
 
     private static ArrayList<ITCFAnnotationProvider> providers;
 
     public static ITCFAnnotationProvider getAnnotationProvider(Object selection) {
         if (providers == null) {
             providers = new  ArrayList<ITCFAnnotationProvider>();
             try {
                 IExtensionPoint point = Platform.getExtensionRegistry().getExtensionPoint(
                         Activator.PLUGIN_ID, "annotation_provider"); //$NON-NLS-1$
                 IExtension[] extensions = point.getExtensions();
                 for (int i = 0; i < extensions.length; i++) {
                     try {
                        Bundle bundle = Platform.getBundle(extensions[i].getNamespaceIdentifier());
                         bundle.start(Bundle.START_TRANSIENT);
                         IConfigurationElement[] e = extensions[i].getConfigurationElements();
                         for (int j = 0; j < e.length; j++) {
                             String nm = e[j].getName();
                             if (nm.equals("class")) { //$NON-NLS-1$
                                 Class<?> c = bundle.loadClass(e[j].getAttribute("name")); //$NON-NLS-1$
                                 providers.add((ITCFAnnotationProvider)c.newInstance());
                             }
                         }
                     }
                     catch (Throwable x) {
                         Activator.log("Cannot access annotation provider extension points", x);
                     }
                 }
             }
             catch (Exception x) {
                 Activator.log("Cannot access annotation provider extension points", x);
             }
         }
         for (ITCFAnnotationProvider p : providers) {
             if (p.isSupportedSelection(selection)) return p;
         }
         return null;
     }
 }
