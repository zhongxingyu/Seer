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
 import java.util.Collection;
 
 import org.eclipse.core.runtime.IConfigurationElement;
 import org.eclipse.core.runtime.IExtension;
 import org.eclipse.core.runtime.IExtensionPoint;
 import org.eclipse.core.runtime.Platform;
 import org.eclipse.tcf.debug.ui.ITCFPrettyExpressionProvider;
 import org.eclipse.tcf.internal.debug.ui.Activator;
 import org.osgi.framework.Bundle;
 
 /**
  * TCF clients can implement ITCFPrettyExpressionProvider to provide human
  * readable "pretty expression" strings that represent values of the TCF debug model objects
  * to be shown in the debugger views.
  *
  * TCF will use internal pretty expression if no suitable provider is found
  * through "pretty_expression_provider" extension point.
  */
 public class TCFPrettyExpressionProvider {
 
     private static ArrayList<ITCFPrettyExpressionProvider> providers;
 
     public static Collection<ITCFPrettyExpressionProvider> getProviders() {
         if (providers == null) {
             providers = new  ArrayList<ITCFPrettyExpressionProvider>();
             try {
                 IExtensionPoint point = Platform.getExtensionRegistry().getExtensionPoint(
                         Activator.PLUGIN_ID, "pretty_expression_provider"); //$NON-NLS-1$
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
                                 providers.add((ITCFPrettyExpressionProvider)c.newInstance());
                             }
                         }
                     }
                     catch (Throwable x) {
                         Activator.log("Cannot access pretty expression provider extension points", x);
                     }
                 }
             }
             catch (Exception x) {
                 Activator.log("Cannot access pretty expression provider extension points", x);
             }
         }
         return providers;
     }
 }
