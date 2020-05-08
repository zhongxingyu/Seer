 /*******************************************************************************
  * Copyright (c) 2011 VMware Inc.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *   VMware Inc. - initial contribution
  *******************************************************************************/
 
 package org.eclipse.virgo.kernel.osgi.region.hook;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertFalse;
 import static org.junit.Assert.assertTrue;
 
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 
 import org.eclipse.virgo.kernel.osgi.region.Region;
 import org.eclipse.virgo.kernel.osgi.region.RegionFilter;
 import org.eclipse.virgo.kernel.osgi.region.RegionPackageImportPolicy;
 import org.eclipse.virgo.kernel.osgi.region.StandardRegionFilter;
 import org.eclipse.virgo.kernel.osgi.region.internal.StandardRegionDigraph;
 import org.eclipse.virgo.teststubs.osgi.framework.StubBundle;
 import org.eclipse.virgo.teststubs.osgi.framework.StubBundleContext;
 import org.eclipse.virgo.util.osgi.VersionRange;
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 import org.osgi.framework.Bundle;
 import org.osgi.framework.BundleException;
 import org.osgi.framework.Version;
 import org.osgi.framework.hooks.resolver.ResolverHook;
 import org.osgi.framework.wiring.BundleRevision;
 import org.osgi.framework.wiring.Capability;
 
 public class RegionResolverHookTests {
 
     private static final String PACKAGE_A = "package.a";
 
     private static final String PACKAGE_B = "package.b";
 
     private static final String PACKAGE_C = "package.c";
 
     private static final String PACKAGE_D = "package.d";
 
     private static final String PACKAGE_X = "package.x";
 
     private static final String BUNDLE_X = "X";
 
     private static final Version BUNDLE_VERSION = new Version("0");
 
     private long bundleId;
 
     private static final String REGION_A = "RegionA";
 
     private static final String BUNDLE_A = "BundleA";
 
     private static final String REGION_B = "RegionB";
 
     private static final String BUNDLE_B = "BundleB";
 
     private static final String REGION_C = "RegionC";
 
     private static final String BUNDLE_C = "BundleC";
 
     private static final String REGION_D = "RegionD";
 
     private static final String BUNDLE_D = "BundleD";
 
     private StandardRegionDigraph digraph;
 
     private ResolverHook resolverHook;
 
     private Map<String, Region> regions;
 
     private Map<String, Bundle> bundles;
 
     private Collection<Capability> candidates;
 
     private ThreadLocal<Region> threadLocal;
 
     @Before
     public void setUp() throws Exception {
         this.bundleId = 1L;
         this.regions = new HashMap<String, Region>();
         this.bundles = new HashMap<String, Bundle>();
         this.threadLocal = new ThreadLocal<Region>();
         StubBundle stubSystemBundle = new StubBundle(0L, "osgi.framework", new Version("0"), "loc");
         StubBundleContext stubBundleContext = new StubBundleContext();
         stubBundleContext.addInstalledBundle(stubSystemBundle);
         this.digraph = new StandardRegionDigraph(stubBundleContext, this.threadLocal);
         this.resolverHook = new RegionResolverHook(this.digraph);
         this.candidates = new HashSet<Capability>();
 
         // Create regions A, B, C, D containing bundles A, B, C, D, respectively.
         createRegion(REGION_A, BUNDLE_A);
         createRegion(REGION_B, BUNDLE_B);
         createRegion(REGION_C, BUNDLE_C);
         createRegion(REGION_D, BUNDLE_D);
 
         createBundle(BUNDLE_X);
 
     }
 
     @After
     public void tearDown() throws Exception {
     }
 
     @Test
     public void testResolveInSameRegion() {
         this.candidates.add(packageCapability(BUNDLE_A, PACKAGE_A));
         this.resolverHook.filterMatches(bundleRevision(BUNDLE_A), this.candidates);
         assertTrue(this.candidates.contains(packageCapability(BUNDLE_A, PACKAGE_A)));
     }
 
     @Test
     public void testResolveInDisconnectedRegion() {
         this.candidates.add(packageCapability(BUNDLE_B, PACKAGE_B));
         this.resolverHook.filterMatches(bundleRevision(BUNDLE_A), this.candidates);
         assertFalse(this.candidates.contains(packageCapability(BUNDLE_B, PACKAGE_B)));
     }
 
     @Test
     public void testResolveConnectedRegionAllowed() throws BundleException {
         RegionFilter filter = createFilter(PACKAGE_B);
         region(REGION_A).connectRegion(region(REGION_B), filter);
 
         this.candidates.add(packageCapability(BUNDLE_B, PACKAGE_B));
         this.resolverHook.filterMatches(bundleRevision(BUNDLE_A), this.candidates);
         assertTrue(this.candidates.contains(packageCapability(BUNDLE_B, PACKAGE_B)));
     }
 
     @Test
     public void testResolveBundleCapabilityConnectedRegionAllowed() throws BundleException {
         RegionFilter filter = createBundleFilter(BUNDLE_B, BUNDLE_VERSION);
         region(REGION_A).connectRegion(region(REGION_B), filter);
 
         this.candidates.add(bundleCapability(BUNDLE_B));
         this.resolverHook.filterMatches(bundleRevision(BUNDLE_A), this.candidates);
         assertTrue(this.candidates.contains(bundleCapability(BUNDLE_B)));
     }
 
     @Test
     public void testResolveConnectedRegionFiltering() throws BundleException {
         region(REGION_A).connectRegion(region(REGION_B), createFilter(PACKAGE_B));
         Bundle x = createBundle(BUNDLE_X);
         region(REGION_B).addBundle(x);
 
         this.candidates.add(packageCapability(BUNDLE_B, PACKAGE_B));
         this.candidates.add(packageCapability(BUNDLE_X, PACKAGE_X));
         this.resolverHook.filterMatches(bundleRevision(BUNDLE_A), this.candidates);
         assertTrue(this.candidates.contains(packageCapability(BUNDLE_B, PACKAGE_B)));
         assertFalse(this.candidates.contains(packageCapability(BUNDLE_X, PACKAGE_X)));
     }
 
     @Test
     public void testResolveBundleConnectedRegionFiltering() throws BundleException {
         RegionFilter filter = createBundleFilter(BUNDLE_B, BUNDLE_VERSION);
         region(REGION_A).connectRegion(region(REGION_B), filter);
         Bundle x = createBundle(BUNDLE_X);
         region(REGION_B).addBundle(x);
 
         this.candidates.add(bundleCapability(BUNDLE_B));
         this.candidates.add(bundleCapability(BUNDLE_X));
         this.resolverHook.filterMatches(bundleRevision(BUNDLE_A), this.candidates);
         assertTrue(this.candidates.contains(bundleCapability(BUNDLE_B)));
         assertFalse(this.candidates.contains(bundleCapability(BUNDLE_X)));
     }
 
     @Test
     public void testResolveTransitive() throws BundleException {
         region(REGION_A).connectRegion(region(REGION_B), createFilter(PACKAGE_C));
         region(REGION_B).connectRegion(region(REGION_C), createFilter(PACKAGE_C));
         region(REGION_C).addBundle(bundle(BUNDLE_X));
 
         this.candidates.add(packageCapability(BUNDLE_B, PACKAGE_B));
         this.candidates.add(packageCapability(BUNDLE_C, PACKAGE_C));
         this.candidates.add(packageCapability(BUNDLE_X, PACKAGE_X));
         this.resolverHook.filterMatches(bundleRevision(BUNDLE_A), this.candidates);
         assertTrue(this.candidates.contains(packageCapability(BUNDLE_C, PACKAGE_C)));
         assertFalse(this.candidates.contains(packageCapability(BUNDLE_B, PACKAGE_B)));
         assertFalse(this.candidates.contains(packageCapability(BUNDLE_X, PACKAGE_X)));
 
     }
 
     @Test
     public void testResolveInCyclicGraph() throws BundleException {
         region(REGION_D).addBundle(bundle(BUNDLE_X));
 
         region(REGION_A).connectRegion(region(REGION_B), createFilter(PACKAGE_D, PACKAGE_X));
         region(REGION_B).connectRegion(region(REGION_A), createFilter());
 
         region(REGION_B).connectRegion(region(REGION_D), createFilter(PACKAGE_D));
         region(REGION_D).connectRegion(region(REGION_B), createFilter());
 
         region(REGION_B).connectRegion(region(REGION_C), createFilter(PACKAGE_X));
         region(REGION_C).connectRegion(region(REGION_B), createFilter());
 
         region(REGION_C).connectRegion(region(REGION_D), createFilter(PACKAGE_X));
         region(REGION_D).connectRegion(region(REGION_C), createFilter());
 
         region(REGION_A).connectRegion(region(REGION_C), createFilter());
         region(REGION_C).connectRegion(region(REGION_A), createFilter());
 
         region(REGION_D).connectRegion(region(REGION_A), createFilter());
         region(REGION_A).connectRegion(region(REGION_D), createFilter());
 
         // Find from region A.
         this.candidates.add(packageCapability(BUNDLE_B, PACKAGE_B));
         this.candidates.add(packageCapability(BUNDLE_C, PACKAGE_C));
         this.candidates.add(packageCapability(BUNDLE_D, PACKAGE_D));
         this.candidates.add(packageCapability(BUNDLE_X, PACKAGE_X));
 
         this.resolverHook.filterMatches(bundleRevision(BUNDLE_A), this.candidates);
         assertEquals(2, this.candidates.size());
         assertTrue(this.candidates.contains(packageCapability(BUNDLE_D, PACKAGE_D)));
         assertTrue(this.candidates.contains(packageCapability(BUNDLE_X, PACKAGE_X)));
 
         // Find from region B
         this.candidates.add(packageCapability(BUNDLE_B, PACKAGE_B));
         this.candidates.add(packageCapability(BUNDLE_C, PACKAGE_C));
         this.candidates.add(packageCapability(BUNDLE_D, PACKAGE_D));
         this.candidates.add(packageCapability(BUNDLE_X, PACKAGE_X));
 
         this.resolverHook.filterMatches(bundleRevision(BUNDLE_B), this.candidates);
         assertEquals(3, this.candidates.size());
         assertTrue(this.candidates.contains(packageCapability(BUNDLE_B, PACKAGE_B)));
         assertTrue(this.candidates.contains(packageCapability(BUNDLE_D, PACKAGE_D)));
         assertTrue(this.candidates.contains(packageCapability(BUNDLE_X, PACKAGE_X)));
     }
 
     @Test
     public void testResolveFromSystemBundle() {
         this.candidates.add(packageCapability(BUNDLE_A, PACKAGE_A));
 
         Bundle stubBundle = new StubBundle(0L, "sys", BUNDLE_VERSION, "");
         this.resolverHook.filterMatches(new StubBundleRevision(stubBundle), this.candidates);
         assertEquals(1, this.candidates.size());
         assertTrue(this.candidates.contains(packageCapability(BUNDLE_A, PACKAGE_A)));
     }
 
     @Test
     public void testResolveFromBundleInNoRegion() {
         this.candidates.add(packageCapability(BUNDLE_A, PACKAGE_A));
 
         Bundle stranger = createBundle("stranger");
         this.resolverHook.filterMatches(new StubBundleRevision(stranger), this.candidates);
         assertEquals(0, this.candidates.size());
     }
 
     @Test
     public void testUnimplementedMethods() {
         this.resolverHook.filterResolvable(null);
         this.resolverHook.end();
     }
 
     private Capability packageCapability(final String bundleSymbolicName, String packageName) {
         return new StubPackageCapability(bundleSymbolicName, packageName);
     }
 
     private Capability bundleCapability(String bundleSymbolicName) {
         return new StubBundleCapability(bundleSymbolicName);
     }
 
     private Region createRegion(String regionName, String... bundleSymbolicNames) throws BundleException {
         Region region = this.digraph.createRegion(regionName);
         for (String bundleSymbolicName : bundleSymbolicNames) {
             Bundle stubBundle = createBundle(bundleSymbolicName);
             region.addBundle(stubBundle);
         }
         this.regions.put(regionName, region);
         return region;
     }
 
     private Region region(String regionName) {
         return this.regions.get(regionName);
     }
 
     private RegionFilter createFilter(final String... packageNames) {
         RegionFilter filter = new StandardRegionFilter();
         filter.setPackageImportPolicy(new RegionPackageImportPolicy() {
 
             @Override
             public boolean isImported(String packageName, Map<String, Object> attributes, Map<String, String> directives) {
                 for (String pkg : packageNames) {
                     if (packageName.equals(pkg)) {
                         return true;
                     }
                 }
                 return false;
             }
         });
         return filter;
     }
 
     private RegionFilter createBundleFilter(String bundleSymbolicName, Version bundleVersion) {
         RegionFilter filter = new StandardRegionFilter();
         filter.allowBundle(bundleSymbolicName, new VersionRange(bundleVersion.toString()));
         return filter;
     }
 
     private Bundle createBundle(String bundleSymbolicName) {
         Bundle stubBundle = new StubBundle(this.bundleId++, bundleSymbolicName, BUNDLE_VERSION, "loc:" + bundleSymbolicName);
         this.bundles.put(bundleSymbolicName, stubBundle);
         return stubBundle;
     }
 
     private BundleRevision bundleRevision(String bundleSymbolicName) {
         return new StubBundleRevision(bundle(bundleSymbolicName));
     }
 
     private Bundle bundle(String bundleSymbolicName) {
         Bundle bundleA = this.bundles.get(bundleSymbolicName);
         return bundleA;
     }
 
     private final class StubPackageCapability implements Capability {
 
         private final String bundleSymbolicName;
 
         private final String packageName;
 
         private StubPackageCapability(String bundleSymbolicName, String packageName) {
             this.bundleSymbolicName = bundleSymbolicName;
             this.packageName = packageName;
         }
 
         @Override
         public String getNamespace() {
             return Capability.PACKAGE_CAPABILITY;
         }
 
         @Override
         public Map<String, String> getDirectives() {
             return new HashMap<String, String>();
         }
 
         @Override
         public Map<String, Object> getAttributes() {
             HashMap<String, Object> attributes = new HashMap<String, Object>();
             attributes.put(Capability.PACKAGE_CAPABILITY, this.packageName);
             return attributes;
         }
 
         @Override
         public BundleRevision getProviderRevision() {
             return new StubBundleRevision(bundle(this.bundleSymbolicName));
         }
 
         @Override
         public int hashCode() {
             final int prime = 31;
             int result = 1;
             result = prime * result + getOuterType().hashCode();
             result = prime * result + (this.bundleSymbolicName == null ? 0 : this.bundleSymbolicName.hashCode());
             result = prime * result + (this.packageName == null ? 0 : this.packageName.hashCode());
             return result;
         }
 
         @Override
         public boolean equals(Object obj) {
             if (this == obj) {
                 return true;
             }
             if (obj == null) {
                 return false;
             }
             if (!(obj instanceof StubPackageCapability)) {
                 return false;
             }
             StubPackageCapability other = (StubPackageCapability) obj;
             if (!getOuterType().equals(other.getOuterType())) {
                 return false;
             }
             if (this.bundleSymbolicName == null) {
                 if (other.bundleSymbolicName != null) {
                     return false;
                 }
             } else if (!this.bundleSymbolicName.equals(other.bundleSymbolicName)) {
                 return false;
             }
             if (this.packageName == null) {
                 if (other.packageName != null) {
                     return false;
                 }
             } else if (!this.packageName.equals(other.packageName)) {
                 return false;
             }
             return true;
         }
 
         private RegionResolverHookTests getOuterType() {
             return RegionResolverHookTests.this;
         }
 
     }
 
     private final class StubBundleCapability implements Capability {
 
         private final String bundleSymbolicName;
 
         private StubBundleCapability(String bundleSymbolicName) {
             this.bundleSymbolicName = bundleSymbolicName;
         }
 
         @Override
         public String getNamespace() {
             return Capability.BUNDLE_CAPABILITY;
         }
 
         @Override
         public Map<String, String> getDirectives() {
             return new HashMap<String, String>();
         }
 
         @Override
         public Map<String, Object> getAttributes() {
             HashMap<String, Object> attributes = new HashMap<String, Object>();
             return attributes;
         }
 
         @Override
         public BundleRevision getProviderRevision() {
             return new StubBundleRevision(bundle(this.bundleSymbolicName));
         }
 
         @Override
         public int hashCode() {
             final int prime = 31;
             int result = 1;
             result = prime * result + getOuterType().hashCode();
             result = prime * result + ((bundleSymbolicName == null) ? 0 : bundleSymbolicName.hashCode());
             return result;
         }
 
         @Override
         public boolean equals(Object obj) {
             if (this == obj)
                 return true;
             if (obj == null)
                 return false;
             if (!(obj instanceof StubBundleCapability))
                 return false;
             StubBundleCapability other = (StubBundleCapability) obj;
             if (!getOuterType().equals(other.getOuterType()))
                 return false;
             if (bundleSymbolicName == null) {
                 if (other.bundleSymbolicName != null)
                     return false;
             } else if (!bundleSymbolicName.equals(other.bundleSymbolicName))
                 return false;
             return true;
         }
 
         private RegionResolverHookTests getOuterType() {
             return RegionResolverHookTests.this;
         }
 
     }
 
     private final class StubBundleRevision implements BundleRevision {
 
         private final Bundle bundle;
 
         private StubBundleRevision(Bundle bundle) {
             this.bundle = bundle;
         }
 
         @Override
         public Bundle getBundle() {
             return this.bundle;
         }
 
         @Override
         public String getSymbolicName() {
             return this.bundle.getSymbolicName();
         }
 
         @Override
         public Version getVersion() {
             return this.bundle.getVersion();
         }
 
         @Override
         public List<Capability> getDeclaredCapabilities(String namespace) {
             throw new UnsupportedOperationException();
         }
 
         @Override
         public int getTypes() {
             throw new UnsupportedOperationException();
         }
 
     }
 
 }
