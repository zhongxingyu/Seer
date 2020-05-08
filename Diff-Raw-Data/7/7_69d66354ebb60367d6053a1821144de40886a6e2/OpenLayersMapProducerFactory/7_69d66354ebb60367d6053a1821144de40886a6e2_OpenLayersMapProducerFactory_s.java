 /* Copyright (c) 2001 - 2007 TOPP - www.openplans.org. All rights reserved.
  * This code is licensed under the GPL 2.0 license, availible at the root
  * application directory.
  */
 package org.vfny.geoserver.wms.responses.map.openlayers;
 
 import org.vfny.geoserver.global.WMS;
 import org.vfny.geoserver.wms.GetMapProducer;
 import org.vfny.geoserver.wms.GetMapProducerFactorySpi;
 import java.util.Collections;
 import java.util.Map;
 import java.util.Set;
 
 
 public class OpenLayersMapProducerFactory implements GetMapProducerFactorySpi {
     public boolean canProduce(String mapFormat) {
        return "openlayers".equalsIgnoreCase(mapFormat);
     }
 
     public GetMapProducer createMapProducer(String mapFormat, WMS wms)
         throws IllegalArgumentException {
         return new OpenLayersMapProducer(wms);
     }
 
     public String getName() {
         return "OpenLayers Map";
     }
 
     public Set getSupportedFormats() {
        return Collections.singleton("openlayers");
     }
 
     public boolean isAvailable() {
         return true;
     }
 
     public Map getImplementationHints() {
         return null;
     }
 }
