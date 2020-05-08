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
 
 import java.util.Collection;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.Set;
 import java.util.Stack;
 
 import org.eclipse.virgo.kernel.osgi.region.Region;
 import org.eclipse.virgo.kernel.osgi.region.RegionDigraph;
 import org.eclipse.virgo.kernel.osgi.region.RegionDigraph.FilteredRegion;
 import org.eclipse.virgo.kernel.osgi.region.RegionFilter;
 import org.osgi.framework.Bundle;
 import org.osgi.framework.BundleContext;
 import org.osgi.framework.hooks.bundle.FindHook;
 
 /**
  * {@link RegionBundleFindHook} manages the visibility of bundles across regions according to the {@link RegionDigraph}.
  * <p />
  * 
  * <strong>Concurrent Semantics</strong><br />
  * Thread safe.
  */
 public final class RegionBundleFindHook implements FindHook {
 
     private final RegionDigraph regionDigraph;
 
     public RegionBundleFindHook(RegionDigraph regionDigraph) {
         this.regionDigraph = regionDigraph;
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public void find(BundleContext context, Collection<Bundle> bundles) {
         if (context.getBundle().getBundleId() == 0L) {
             return;
         }
 
         Region finderRegion = getRegion(context);
         if (finderRegion == null) {
             bundles.clear();
             return;
         }
 
         Visitor visitor = new Visitor(bundles);
         getAllowed(finderRegion, visitor, new HashSet<Region>());
         Set<Bundle> allowed = visitor.getAllowed();
 
         bundles.retainAll(allowed);
     }
 
     private class Visitor {
 
         private Object monitor = new Object();
 
         private final Collection<Bundle> bundles;
 
         private Set<Bundle> allowed = new HashSet<Bundle>();
 
         private final Stack<Set<Bundle>> allowedStack = new Stack<Set<Bundle>>();
 
         private Visitor(Collection<Bundle> bundles) {
             this.bundles = bundles;
         }
 
         private Set<Bundle> getAllowed() {
             synchronized (this.monitor) {
                 return this.allowed;
             }
         }
 
         private Collection<Bundle> getBundles() {
             return this.bundles;
         }
 
         private void pushAllowed() {
             synchronized (this.monitor) {
                 this.allowedStack.push(this.allowed);
                 this.allowed = new HashSet<Bundle>();
             }
         }
 
         private void popAllowed() {
             synchronized (this.monitor) {
                 this.allowed = this.allowedStack.pop();
             }
         }
        
        private void allowInRegion(Region r) {
            for (Bundle b : this.bundles) {
                if (r.contains(b)) {
                    getAllowed().add(b);
                }
            }
        }
     }
 
     private Set<Bundle> getAllowed(Region r, Visitor visitor, Set<Region> path) {
 
         if (!path.contains(r)) {
            visitor.allowInRegion(r);
             allowImportedBundles(r, visitor, path);
         }
 
         return visitor.getAllowed();
     }
 
     private void allowImportedBundles(Region r, Visitor visitor, Set<Region> path) {
         for (FilteredRegion fr : this.regionDigraph.getEdges(r)) {
            allowTransitive(r, visitor, path, fr);
         }
     }
 
    private void allowTransitive(Region r, Visitor visitor, Set<Region> path, FilteredRegion fr) {
        visitor.pushAllowed();
        getAllowed(fr.getRegion(), visitor, extendPath(r, path));
        Set<Bundle> a = visitor.getAllowed();
        visitor.popAllowed();
        filter(a, fr.getFilter());
        visitor.getAllowed().addAll(a);
     }
 
     private Set<Region> extendPath(Region r, Set<Region> path) {
         Set<Region> newPath = new HashSet<Region>(path);
         newPath.add(r);
         return newPath;
     }
 
     private void filter(Set<Bundle> bundles, RegionFilter filter) {
         Iterator<Bundle> i = bundles.iterator();
         while (i.hasNext()) {
             Bundle b = i.next();
             if (!filter.isBundleAllowed(b.getSymbolicName(), b.getVersion())) {
                 i.remove();
             }
         }
     }
 
     private Region getRegion(BundleContext context) {
         Bundle b = context.getBundle();
         for (Region r : this.regionDigraph) {
             if (r.contains(b)) {
                 return r;
             }
         }
         return null;
     }
 
 }
