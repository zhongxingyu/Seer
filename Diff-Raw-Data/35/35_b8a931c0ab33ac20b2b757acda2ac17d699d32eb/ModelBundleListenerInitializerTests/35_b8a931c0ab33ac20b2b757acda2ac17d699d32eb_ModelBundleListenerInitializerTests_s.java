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
 
 import static org.easymock.EasyMock.createMock;
 import static org.junit.Assert.assertEquals;
 
 import java.io.IOException;
 
 import org.junit.Test;
 import org.osgi.framework.InvalidSyntaxException;
 import org.osgi.framework.Version;
 
 import org.eclipse.virgo.kernel.osgi.framework.PackageAdminUtil;
 
 import org.eclipse.virgo.kernel.model.StubArtifactRepository;
 import org.eclipse.virgo.kernel.model.internal.DependencyDeterminer;
 import org.eclipse.virgo.kernel.model.internal.bundle.ModelBundleListenerInitializer;
 import org.eclipse.virgo.kernel.serviceability.Assert.FatalAssertionException;
 import org.eclipse.virgo.teststubs.osgi.framework.StubBundle;
 import org.eclipse.virgo.teststubs.osgi.framework.StubBundleContext;
 import org.eclipse.virgo.teststubs.osgi.support.TrueFilter;
 
 public class ModelBundleListenerInitializerTests {
 
     private final StubArtifactRepository artifactRepository = new StubArtifactRepository();
 
     private final PackageAdminUtil packageAdminUtil = createMock(PackageAdminUtil.class);
 
     private final StubBundleContext bundleContext;
     
     private final StubBundleContext systemBundleContext;
     {
         StubBundle bundle = new StubBundle();
         this.bundleContext = (StubBundleContext) bundle.getBundleContext();
         StubBundle stubSystemBundle = new StubBundle(0L, "org.osgi.framework", new Version("0"), "loc");
         this.systemBundleContext = (StubBundleContext)stubSystemBundle.getBundleContext();
         this.bundleContext.addInstalledBundle(stubSystemBundle);
         this.systemBundleContext.addInstalledBundle(bundle);
         String filterString = String.format("(&(objectClass=%s)(artifactType=bundle))", DependencyDeterminer.class.getCanonicalName());
         this.bundleContext.addFilter(filterString, new TrueFilter(filterString));
        //this.bundleContext.addInstalledBundle(bundle);
     }
 
     private final ModelBundleListenerInitializer initializer = new ModelBundleListenerInitializer(artifactRepository, packageAdminUtil, bundleContext, bundleContext);
 
     @Test(expected = FatalAssertionException.class)
     public void nullArtifactRepository() {
         new ModelBundleListenerInitializer(null, packageAdminUtil, bundleContext, bundleContext);
     }
 
     @Test(expected = FatalAssertionException.class)
     public void nullPackageAdminUtil() {
         new ModelBundleListenerInitializer(artifactRepository, null, bundleContext, bundleContext);
     }
 
     @Test(expected = FatalAssertionException.class)
     public void nullKernelBundleContext() {
         new ModelBundleListenerInitializer(artifactRepository, packageAdminUtil, null, bundleContext);
     }
 
     @Test(expected = FatalAssertionException.class)
     public void nullUserRegionBundleContext() {
         new ModelBundleListenerInitializer(artifactRepository, packageAdminUtil, bundleContext, null);
     }
 
     @Test
     public void initialize() throws IOException, InvalidSyntaxException {
        assertEquals(0, this.systemBundleContext.getBundleListeners().size());
         this.initializer.initialize();
        assertEquals(1, this.systemBundleContext.getBundleListeners().size());
         assertEquals(1, this.artifactRepository.getArtifacts().size());
     }
 
     @Test
     public void destroy() throws IOException, InvalidSyntaxException {
         this.initializer.initialize();
        assertEquals(1, this.systemBundleContext.getBundleListeners().size());
         this.initializer.destroy();
        assertEquals(0, this.systemBundleContext.getBundleListeners().size());
     }
 }
