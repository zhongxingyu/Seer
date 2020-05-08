 /* Copyright (c) 2001 - 2007 TOPP - www.openplans.org. All rights reserved.
  * This code is licensed under the GPL 2.0 license, available at the root
  * application directory.
  */
 
 package org.geoserver.wps;
 
 import org.geoserver.config.GeoServer;
 import org.geoserver.config.ServiceInfo;
 import org.geoserver.config.util.XStreamServiceLoader;
 import org.geoserver.platform.GeoServerResourceLoader;
 
 /**
  * Service loader for the Web Processing Service
  *
  * @author Lucas Reed, Refractions Research Inc
  * @author Justin Deoliveira, The Open Planning Project
  */
 public class WPSLoader extends XStreamServiceLoader {
     public WPSLoader(GeoServerResourceLoader resourceLoader) {
        super(resourceLoader);
     }
 
     public String getServiceId() {
         return "wps";
     }
 
     protected ServiceInfo createServiceFromScratch(GeoServer gs) {
         WPSInfoImpl wps = new WPSInfoImpl();
         wps.setId(getServiceId());
 
         return wps;
     }
 }
