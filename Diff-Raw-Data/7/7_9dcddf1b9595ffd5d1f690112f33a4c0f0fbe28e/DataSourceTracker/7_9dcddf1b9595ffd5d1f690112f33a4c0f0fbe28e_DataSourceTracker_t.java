 /*******************************************************************************
  * Copyright (c) 2010 Oracle.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * and Apache License v2.0 which accompanies this distribution. 
  * The Eclipse Public License is available at
  *     http://www.eclipse.org/legal/epl-v10.html
  * and the Apache License v2.0 is available at 
  *     http://www.opensource.org/licenses/apache2.0.php.
  * You may elect to redistribute this code under either of these licenses.
  *
  * Contributors:
  *     mkeith - Gemini JPA work 
  ******************************************************************************/
 package org.eclipse.gemini.jpa;
 
 import org.osgi.framework.ServiceReference;
 import org.osgi.util.tracker.ServiceTrackerCustomizer;
 
 /**
  *  Service Tracker that tracks the DataSourceFactory service
  *  for a given EMF service.
  */
 public class DataSourceTracker implements ServiceTrackerCustomizer {
 
     // Look for data source factory for this persistence unit
     private PUnitInfo pUnitInfo;
 
     // Tell this guy when the service disappears
     GeminiServicesUtil servicesUtil;
         
     public DataSourceTracker(PUnitInfo pUnitInfo,
                              GeminiServicesUtil servicesUtil) {
         this.pUnitInfo = pUnitInfo;
         this.servicesUtil = servicesUtil;
     }
     
     public Object addingService(ServiceReference ref) {
        return pUnitInfo.getAssignedProvider()
                        .getBundleContext()
                        .getService(ref);
        // TODO We would like to be calling 
        //       servicesUtil.dataSourceFactoryAdded(pUnitInfo)
        // but that would involve doing all kinds of EMF service registration
     }
 
     public void modifiedService(ServiceReference ref, Object service) {}
 
     public void removedService(ServiceReference ref, Object service) {
         servicesUtil.dataSourceFactoryRemoved(pUnitInfo);
     }
 }
