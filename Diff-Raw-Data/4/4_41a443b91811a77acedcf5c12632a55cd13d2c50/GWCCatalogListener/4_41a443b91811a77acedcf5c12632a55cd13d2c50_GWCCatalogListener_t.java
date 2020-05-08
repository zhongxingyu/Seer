 /* Copyright (c) 2001 - 2009 TOPP - www.openplans.org. All rights reserved.
  * This code is licensed under the GPL 2.0 license, available at the root
  * application directory.
  */
 package org.geoserver.gwc;
 
 import java.util.ArrayList;
 import java.util.Hashtable;
 import java.util.Iterator;
 import java.util.List;
 import java.util.logging.Logger;
 
 import org.geoserver.catalog.Catalog;
 import org.geoserver.catalog.CoverageInfo;
 import org.geoserver.catalog.FeatureTypeInfo;
 import org.geoserver.catalog.LayerInfo;
 import org.geoserver.catalog.ResourceInfo;
 import org.geoserver.catalog.event.CatalogAddEvent;
 import org.geoserver.catalog.event.CatalogListener;
 import org.geoserver.catalog.event.CatalogModifyEvent;
 import org.geoserver.catalog.event.CatalogRemoveEvent;
 import org.geotools.geometry.jts.ReferencedEnvelope;
 import org.geotools.util.logging.Logging;
 import org.geowebcache.GeoWebCacheException;
 import org.geowebcache.layer.Grid;
 import org.geowebcache.layer.GridCalculator;
 import org.geowebcache.layer.SRS;
 import org.geowebcache.layer.TileLayer;
 import org.geowebcache.layer.TileLayerDispatcher;
 import org.geowebcache.layer.wms.WMSLayer;
 import org.geowebcache.util.ApplicationContextProvider;
 import org.geowebcache.util.Configuration;
 import org.geowebcache.util.GeoServerConfiguration;
 import org.geowebcache.util.wms.BBOX;
 
 
 /**
  * This class acts as a source of TileLayer objects for GeoWebCache.
  * 
  * 
  * 
  * 
  * @author Arne Kepp / OpenGeo 2009
  */
 public class GWCCatalogListener implements CatalogListener, Configuration {
     private static Logger log = Logging.getLogger("org.geoserver.gwc");
     
     protected Catalog cat = null;
     
     protected TileLayerDispatcher layerDispatcher = null;
     
     private List<String> mimeFormats = null;
     
     private int[] metaFactors = {4,4};
     
     private String wmsUrl = null;
    
     
     /**
      * Constructor for Spring
      * 
      * @param cat
      * @param layerDispatcher
      * @param ctxProv
      */
     public GWCCatalogListener(Catalog cat, TileLayerDispatcher layerDispatcher, ApplicationContextProvider ctxProv) {
         this.cat = cat;
         this.layerDispatcher = layerDispatcher;
         
         mimeFormats = new ArrayList<String>(5);
         mimeFormats.add("image/png");
         mimeFormats.add("image/gif");
         mimeFormats.add("image/png8");
         mimeFormats.add("image/jpeg"); 
         mimeFormats.add("application/vnd.google-earth.kml+xml");
         
         wmsUrl = ctxProv.getSystemVar(GeoServerConfiguration.GEOSERVER_WMS_URL, "http://localhost:8080/geoserver/wms");
         
         cat.addListener(this);
         
         log.fine("GWCCatalogListener registered with catalog");
     }
     
     /**
      * Handles when a layer is added to the catalog
      */
     public void handleAddEvent(CatalogAddEvent event) {
         Object obj = event.getSource();
         
         WMSLayer wmsLayer = getLayer(obj);
        
        layerDispatcher.getLayers();
        
         layerDispatcher.add(wmsLayer);
         
         log.finer(wmsLayer.getName() + " added to TileLayerDispatcher");
     }
     
     public void handleModifyEvent(CatalogModifyEvent event) { 
         Object obj = event.getSource();
         
         WMSLayer wmsLayer = getLayer(obj);
                 
         layerDispatcher.update(wmsLayer);
         
         log.finer(wmsLayer.getName() + " updated on TileLayerDispatcher");
     }
     
     public void handleRemoveEvent(CatalogRemoveEvent event) { 
         Object obj = event.getSource();
         
         String name = getLayerName(obj);
         
         layerDispatcher.remove(name);
         
         log.finer(name + " removed from TileLayerDispatcher");
     }
 
     public void handlePostModifyEvent(org.geoserver.catalog.event.CatalogPostModifyEvent test) {
 
     }
 
     public void reloaded() {
         try {
             layerDispatcher.reInit();
         } catch (GeoWebCacheException gwce) {
             log.fine("Unable to reinit TileLayerDispatcher gwce.getMessage()");
         }
     }
 
     public String getIdentifier() throws GeoWebCacheException {
         return "GeoServer Catalog Listener";
     }
 
     public List<TileLayer> getTileLayers(boolean reload)
             throws GeoWebCacheException {
         
         Iterator<LayerInfo> iter = cat.getLayers().iterator();
         
         ArrayList<TileLayer> list = new ArrayList<TileLayer>(cat.getLayers().size());
         
         while(iter.hasNext()) {
             LayerInfo li = iter.next();
             TileLayer tl = getLayer(li);
             list.add(tl);
         }
         
         log.fine("Responding with " + list.size() + " to getTileLayers() request from TileLayerDispatcher");
         
         return list;
     }
     
     
     private String getLayerName(Object obj) {
         return getLayer(obj).getName();
     }
     
     private WMSLayer getLayer(Object obj) {
         if(obj instanceof LayerInfo) {
             obj = ((LayerInfo) obj).getResource();
         }
         
         if(obj instanceof ResourceInfo) {
             if(obj instanceof FeatureTypeInfo) {
                 FeatureTypeInfo fti = (FeatureTypeInfo) obj;
               
                 return new WMSLayer(
                         fti.getPrefixedName(),
                         getWMSUrl(), 
                         null, // Styles 
                         fti.getPrefixedName(), 
                         mimeFormats, 
                         getGrids(fti.getLatLonBoundingBox()), 
                         metaFactors,
                         null);
                 
             } else if(obj instanceof CoverageInfo) {
                 
                 CoverageInfo ci = (CoverageInfo) obj;
                 
                 return new WMSLayer(
                         ci.getPrefixedName(),
                         getWMSUrl(), 
                         null, // Styles 
                         ci.getPrefixedName(), 
                         mimeFormats, 
                         getGrids(ci.getLatLonBoundingBox()), 
                         metaFactors,
                         null);
                 
             } else {
                 log.fine("Unable to handle "  + obj.getClass());
             }
         } else {
             log.fine("Unable to handle "  + obj.getClass());
         }
         
         return null;
     }
     
     private String[] getWMSUrl() {
         String[] strs = { wmsUrl };
         return strs;
     }
     
     private Hashtable<SRS,Grid> getGrids(ReferencedEnvelope env) {
         double minX = env.getMinX();
         double minY = env.getMinY();
         double maxX = env.getMaxX();
         double maxY = env.getMaxY();
 
         BBOX bounds4326 = new BBOX(minX,minY,maxX,maxY);
  
         BBOX bounds900913 = new BBOX(
                 longToSphericalMercatorX(minX),
                 latToSphericalMercatorY(minY),
                 longToSphericalMercatorX(maxX),
                 latToSphericalMercatorY(maxY));
         
         Hashtable<SRS,Grid> grids = new Hashtable<SRS,Grid>(2);
         
         grids.put(SRS.getEPSG4326(), new Grid(SRS.getEPSG4326(), bounds4326, 
                 BBOX.WORLD4326, GridCalculator.get4326Resolutions()));
         grids.put(SRS.getEPSG900913(), new Grid(SRS.getEPSG900913(), bounds900913,
                 BBOX.WORLD900913, GridCalculator.get900913Resolutions()));
        
         return grids;
     }
     
     private double longToSphericalMercatorX(double x) {
         return (x/180.0)*20037508.34;
     }
     
     private double latToSphericalMercatorY(double y) {        
         if(y > 85.05112) {
             y = 85.05112;
         }
         
         if(y < -85.05112) {
             y = -85.05112;
         }
         
         y = (Math.PI/180.0)*y;
         double tmp = Math.PI/4.0 + y/2.0; 
         return 20037508.34 * Math.log(Math.tan(tmp)) / Math.PI;
     }
 }
 
 
