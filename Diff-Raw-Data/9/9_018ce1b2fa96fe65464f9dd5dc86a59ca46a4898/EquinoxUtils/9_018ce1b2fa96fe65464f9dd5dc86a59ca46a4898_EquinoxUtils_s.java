 /*******************************************************************************
  * Copyright (c) 2008, 2010 VMware Inc.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *   VMware Inc. - initial contribution
  *******************************************************************************/
 
 package org.eclipse.virgo.kernel.userregion.internal.equinox;
 
 import java.lang.reflect.Method;
 import java.util.HashSet;
 import java.util.Set;
 
 import org.eclipse.osgi.framework.internal.core.BundleHost;
import org.eclipse.osgi.internal.baseadaptor.DefaultClassLoader;
 import org.eclipse.osgi.internal.loader.BundleLoader;
 import org.eclipse.osgi.service.resolver.BundleDescription;
 import org.eclipse.osgi.service.resolver.ExportPackageDescription;
 import org.eclipse.osgi.service.resolver.PlatformAdmin;
 import org.eclipse.osgi.service.resolver.State;
 import org.osgi.framework.Bundle;
 import org.osgi.framework.BundleContext;
 
import org.eclipse.virgo.kernel.osgi.framework.BundleClassLoaderUnavailableException;

 /**
  * Utility methods for working with Equinox internals.
  * <p/>
  * 
  * <strong>Concurrent Semantics</strong><br />
  * 
  * Threadsafe.
  * 
  */
 public final class EquinoxUtils {
 
     /**
      * Gets the {@link ClassLoader} for the supplied {@link Bundle}.
      * 
      * @param bundle the bundle.
      * @return the bundle <code>ClassLoader</code>.
      */
     public static ClassLoader getBundleClassLoader(Bundle bundle) {
         ClassLoader classLoader = null;
         if (BundleHost.class.isAssignableFrom(bundle.getClass())) {
             BundleHost bundleHost = (BundleHost) bundle;
 
             Class<?>[] parmTypes = {};
             Method checkLoaderMethod;
             try {
                 checkLoaderMethod = BundleHost.class.getDeclaredMethod("checkLoader", parmTypes);
                 Object[] args = {};
                 checkLoaderMethod.setAccessible(true);
                 BundleLoader bundleLoader = (BundleLoader) checkLoaderMethod.invoke(bundleHost, args);
 
                 if (bundleLoader == null) {
                     throw new IllegalStateException("Unable to access BundleLoader for bundle '" + bundle.getSymbolicName() + "'.");
                 }
 
                 Method createClassLoaderMethod = BundleLoader.class.getDeclaredMethod("createClassLoader", parmTypes);
                 createClassLoaderMethod.setAccessible(true);
 
                classLoader = (DefaultClassLoader) createClassLoaderMethod.invoke(bundleLoader, args);
             } catch (Exception e) {
                 throw new BundleClassLoaderUnavailableException("Failed to get class loader for bundle '" + bundle
                     + "' - possible resolution problem.", e);
             }
         }
         return classLoader;
     }
 
     /**
      * Gets all direct dependencies of the supplied {@link Bundle}.
      * 
      * @param bundle the <code>Bundle</code>.
      * @param bundleContext the {@link BundleContext} to use for service access - typically the system
      *        <code>BundleContext</code>.
      * @param serverAdmin the {@link PlatformAdmin} service.
      * @return the direct dependencies.
      */
     public static Bundle[] getDirectDependencies(Bundle bundle, BundleContext bundleContext, PlatformAdmin serverAdmin) {
         return getDirectDependencies(bundle, bundleContext, serverAdmin, false);
     }
 
     /**
      * Gets all direct dependencies of the supplied {@link Bundle}, optionally included fragments of the direct
      * dependencies in the returned array.
      * 
      * @param bundle the <code>Bundle</code>.
      * @param bundleContext the {@link BundleContext} to use for service access - typically the system
      *        <code>BundleContext</code>.
      * @param serverAdmin the {@link PlatformAdmin} service.
      * @param includeFragments whether to include fragments or no
      * @return an array of {@link Bundle}s which are direct dependencies
      */
     public static Bundle[] getDirectDependencies(Bundle bundle, BundleContext bundleContext, PlatformAdmin serverAdmin, boolean includeFragments) {
         State state = serverAdmin.getState(false);
 
         ExportPackageDescription[] exportPackageDescriptions = serverAdmin.getStateHelper().getVisiblePackages(state.getBundle(bundle.getBundleId()));
 
         Set<Bundle> dependencies = new HashSet<Bundle>();
 
         for (ExportPackageDescription exportPackageDescription : exportPackageDescriptions) {
             BundleDescription bundleDescription = exportPackageDescription.getExporter();
             if (bundleDescription.getBundleId() != bundle.getBundleId()) {
                 Bundle dependencyBundle = bundleContext.getBundle(bundleDescription.getBundleId());
                 // Handle an uninstalled dependent bundle gracefully.
                 if (dependencyBundle != null) {
                     dependencies.add(dependencyBundle);
                     if (includeFragments) {
                         BundleDescription[] fragmentDescriptions = bundleDescription.getFragments();
                         for (BundleDescription fragmentDescription : fragmentDescriptions) {
                             Bundle fragment = bundleContext.getBundle(fragmentDescription.getBundleId());
                             if (fragment != null) {
                                 dependencies.add(fragment);
                             }
                         }
                     }
                 }
             }
         }
 
         return dependencies.toArray(new Bundle[dependencies.size()]);
     }
 
     /**
      * Queries whether the supplied {@link Bundle} is the system bundle.
      * 
      * @param bundle the <code>Bundle</code>.
      * @return <code>true</code> if <code>bundle</code> is the system bundle, otherwise <code>false</code>.
      */
     public static boolean isSystemBundle(Bundle bundle) {
         return bundle != null && bundle.getBundleId() == 0;
     }
 }
