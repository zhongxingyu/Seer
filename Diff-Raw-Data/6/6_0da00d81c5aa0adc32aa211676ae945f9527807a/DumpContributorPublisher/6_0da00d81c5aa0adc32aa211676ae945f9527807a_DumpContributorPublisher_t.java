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
 
 package org.eclipse.virgo.medic.dump.impl;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.eclipse.virgo.medic.dump.DumpContributor;
 import org.eclipse.virgo.medic.dump.impl.heap.HeapDumpContributor;
 import org.eclipse.virgo.medic.dump.impl.logback.LogDumpContributor;
 import org.eclipse.virgo.medic.dump.impl.summary.SummaryDumpContributor;
 import org.eclipse.virgo.medic.dump.impl.thread.ThreadDumpContributor;
 import org.eclipse.virgo.medic.impl.config.ConfigurationProvider;
 import org.osgi.framework.BundleContext;
 import org.osgi.framework.ServiceRegistration;
 
 
 public final class DumpContributorPublisher {
 
     private final List<ServiceRegistration> contributorRegistrations = new ArrayList<ServiceRegistration>();
 
     private final BundleContext bundleContext;
     
     private final ConfigurationProvider configurationProvider;
     
     private final LogDumpContributor logDumpContributor;
 
     public DumpContributorPublisher(BundleContext bundleContext, ConfigurationProvider configurationProvider) {
         this.bundleContext = bundleContext;
         this.configurationProvider = configurationProvider;
         this.logDumpContributor = new LogDumpContributor(this.configurationProvider);
     }
 
     public void publishDumpContributors() {
         publishDumpContributor(new SummaryDumpContributor());
         publishDumpContributor(new HeapDumpContributor());
         publishDumpContributor(new ThreadDumpContributor());
 		publishDumpContributor(this.logDumpContributor);
     }
 
     private void publishDumpContributor(DumpContributor dumpContributor) {
         ServiceRegistration registration = this.bundleContext.registerService(DumpContributor.class.getName(), dumpContributor, null);
         this.contributorRegistrations.add(registration);
     }
 
     public void retractDumpContributors() {        
         for (ServiceRegistration registration : this.contributorRegistrations) {
             registration.unregister();
         }
         
         this.logDumpContributor.clear();
         
         this.contributorRegistrations.clear();
     }
 }
