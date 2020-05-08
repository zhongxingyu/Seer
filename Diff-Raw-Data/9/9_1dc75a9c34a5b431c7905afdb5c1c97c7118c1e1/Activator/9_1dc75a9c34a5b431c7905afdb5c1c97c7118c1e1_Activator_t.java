 /*******************************************************************************
  * Copyright (c) 2009, 2011 Wind River Systems, Inc. and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     Wind River Systems - initial API and implementation
  *******************************************************************************/
 package org.eclipse.tcf.internal;
 
 import org.osgi.framework.Bundle;
 import org.osgi.framework.BundleActivator;
 import org.osgi.framework.BundleContext;
 import org.osgi.service.packageadmin.PackageAdmin;
 import org.osgi.util.tracker.ServiceTracker;
 
 public class Activator implements  BundleActivator {
 
     private static final String TCF_INTEGRATION_BUNDLE_ID = "org.eclipse.tcf";
 
     public void start(BundleContext context) throws Exception {
         /*
          * Activate TCF Eclipse integration bundle "org.eclipse.tcf".
          * It must be activated explicitly, because default activation through
          * class loading may never happen - most client don't need classes from that bundle.
          */
         ServiceTracker tracker = new ServiceTracker(context, PackageAdmin.class.getName(), null);
         tracker.open();
         Bundle[] bundles = ((PackageAdmin)tracker.getService()).getBundles(TCF_INTEGRATION_BUNDLE_ID, null);
         int cnt = 0;
         if (bundles != null) {
             for (Bundle bundle : bundles) {
                 if ((bundle.getState() & (Bundle.INSTALLED | Bundle.UNINSTALLED)) == 0) {
                    // Calling the start(...) method leads to an state change bundle
                    // exception in case the bundle is not in RESOLVED state. Trigger
                    // the bundle activation via the loadClass method is apparently safer.
                    bundle.loadClass("org.eclipse.tcf.Activator");
                     cnt++;
                 }
             }
         }
         if (cnt != 1) throw new Exception("Invalid or missing bundle: " + TCF_INTEGRATION_BUNDLE_ID);
     }
 
     public void stop(BundleContext context) throws Exception {
     }
 }
