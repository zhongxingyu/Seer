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
 
 package org.eclipse.virgo.kernel.model.internal.bundle;
 
 import javax.annotation.PostConstruct;
 import javax.annotation.PreDestroy;
 
 import org.eclipse.virgo.kernel.model.RuntimeArtifactRepository;
 import org.eclipse.virgo.kernel.osgi.framework.PackageAdminUtil;
 import org.eclipse.virgo.kernel.serviceability.NonNull;
 import org.osgi.framework.Bundle;
 import org.osgi.framework.BundleContext;
 import org.osgi.framework.BundleListener;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * An intializer responsible for registering a {@link ModelBundleListener} and enumerating any existing {@link Bundle}
  * objects from the OSGi Framework.
  * <p />
  * 
  * <strong>Concurrent Semantics</strong><br />
  * 
  * Threadsafe
  * 
  * @see ModelBundleListener
  */
 public final class ModelBundleListenerInitializer {
 
     private final Logger logger = LoggerFactory.getLogger(this.getClass());
 
     private final RuntimeArtifactRepository artifactRepository;
 
     private final PackageAdminUtil packageAdminUtil;
 
     private final BundleContext kernelBundleContext;
 
     private final BundleContext userRegionBundleContext;
 
     private final BundleListener bundleListener;
 
    private final BundleContext systemBundleContext;

     public ModelBundleListenerInitializer(@NonNull RuntimeArtifactRepository artifactRepository, @NonNull PackageAdminUtil packageAdminUtil,
         @NonNull BundleContext kernelBundleContext, @NonNull BundleContext userRegionBundleContext) {
         this.artifactRepository = artifactRepository;
         this.packageAdminUtil = packageAdminUtil;
         this.kernelBundleContext = kernelBundleContext;
         this.userRegionBundleContext = userRegionBundleContext;
        this.systemBundleContext = kernelBundleContext.getBundle(0L).getBundleContext();
         this.bundleListener = new ModelBundleListener(kernelBundleContext, artifactRepository, packageAdminUtil);
     }
 
     /**
      * Registers a {@link BundleListener} with the OSGi framework. Enumerates any existing {@link Bundle}s that exist
     * from the OSGi framework.
      */
     @PostConstruct
     public void initialize() {
        // Register the listener with the system bundle to see all bundles.
        this.systemBundleContext.addBundleListener(this.bundleListener);
        // Find bundles using the user region context as application bundles are added to the user region.
         for (Bundle bundle : this.userRegionBundleContext.getBundles()) {
             try {
                 this.artifactRepository.add(new BundleArtifact(this.kernelBundleContext, this.packageAdminUtil, bundle));
             } catch (Exception e) {
                 this.logger.error(String.format("Exception adding bundle '%s:%s' to the repository", bundle.getSymbolicName(),
                     bundle.getVersion().toString()), e);
             }
         }
     }
 
     /**
      * Unregisters the listener from the OSGi framework
      */
     @PreDestroy
     public void destroy() {
        this.systemBundleContext.removeBundleListener(this.bundleListener);
     }
 }
