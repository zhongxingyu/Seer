 /* Copyright (c) 2001 - 2007 TOPP - www.openplans.org. All rights reserved.
  * This code is licensed under the GPL 2.0 license, availible at the root
  * application directory.
  */
 package org.geoserver.wms;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 
 import javax.servlet.http.HttpServletResponse;
 
 import org.geoserver.platform.GeoServerExtensions;
 import org.geotools.geometry.jts.ReferencedEnvelope;
 import org.geotools.referencing.CRS;
 import org.geotools.referencing.crs.DefaultGeographicCRS;
 import org.geotools.referencing.operation.projection.ProjectionException;
 import org.opengis.referencing.FactoryException;
 import org.opengis.referencing.NoSuchAuthorityCodeException;
 import org.opengis.referencing.crs.CoordinateReferenceSystem;
 import org.opengis.referencing.operation.TransformException;
 import org.springframework.beans.BeansException;
 import org.springframework.context.ApplicationContext;
 import org.springframework.context.ApplicationContextAware;
 import org.vfny.geoserver.wms.WmsException;
 import org.vfny.geoserver.wms.requests.DescribeLayerRequest;
 import org.vfny.geoserver.wms.requests.GetFeatureInfoRequest;
 import org.vfny.geoserver.wms.requests.GetLegendGraphicRequest;
 import org.vfny.geoserver.wms.requests.GetMapRequest;
 import org.vfny.geoserver.wms.requests.WMSCapabilitiesRequest;
 import org.vfny.geoserver.wms.responses.DescribeLayerResponse;
 import org.vfny.geoserver.wms.responses.GetFeatureInfoResponse;
 import org.vfny.geoserver.wms.responses.GetLegendGraphicResponse;
 import org.vfny.geoserver.wms.responses.GetMapResponse;
 import org.vfny.geoserver.wms.responses.WMSCapabilitiesResponse;
 import org.vfny.geoserver.wms.responses.map.kml.KMLReflector;
 import org.vfny.geoserver.wms.servlets.Capabilities;
 import org.vfny.geoserver.wms.servlets.DescribeLayer;
 import org.vfny.geoserver.wms.servlets.GetFeatureInfo;
 import org.vfny.geoserver.wms.servlets.GetLegendGraphic;
 import org.vfny.geoserver.wms.servlets.GetMap;
 
 import com.vividsolutions.jts.geom.Envelope;
 
 public class DefaultWebMapService implements WebMapService,
         ApplicationContextAware {
     /**
      * default for 'format' parameter.
      */
     public static String FORMAT = "image/png";
 
     /**
      * default for 'styles' parameter.
      */
     public static List STYLES = Collections.EMPTY_LIST;
 
     /**
      * longest side for the preview
      */
     public static int MAX_SIDE = 512;
     
     /**
      * minimum height to have a decent looking OL preview
      */
     public static int MIN_OL_HEIGHT = 330;
 
     /**
      * default for 'srs' parameter.
      */
     public static String SRS = "EPSG:4326";
 
     /**
      * default for 'transparent' parameter.
      */
     public static Boolean TRANSPARENT = Boolean.TRUE;
     
     /**
      * default for 'bbox' paramter
      */
     public static ReferencedEnvelope BBOX = new ReferencedEnvelope(
             new Envelope(-180, 180, -90, 90), DefaultGeographicCRS.WGS84);
 
     /**
      * wms configuration 
      */
     WMS wms;
     
     /**
      * Application context
      */
     ApplicationContext context;
     
     /**
      * Temporary field that handles the usage of the line width optimization code
      */
     private static Boolean OPTIMIZE_LINE_WIDTH = null;
     
     /**
      * Temporary field that handles the choice of renderer to be used
      */
     private static Boolean USE_STREAMING_RENDERER = null;
     
     /**
      * Max number of rule filters to be used against the data source
      */
     public static Integer MAX_FILTER_RULES = null;
 
 
     public DefaultWebMapService( WMS wms ) {
         this.wms = wms;
     }
     
     /**
      * @see WebMapService#getServiceInfo()
      */
     public WMSInfo getServiceInfo() {
         return wms.getServiceInfo();
     }
     
     /**
      * @see ApplicationContextAware#setApplicationContext(ApplicationContext)
      */
     public void setApplicationContext(ApplicationContext context)
             throws BeansException {
         this.context = context;
         
         // first time initialization of line width optimization flag
         if (OPTIMIZE_LINE_WIDTH == null) {
             String enabled = GeoServerExtensions.getProperty("OPTIMIZE_LINE_WIDTH", context);
             // default to true, but allow switching off
             if(enabled == null)
                 OPTIMIZE_LINE_WIDTH = true;
             else
                 OPTIMIZE_LINE_WIDTH = Boolean.valueOf(enabled);
         }
         
         // initialization of the renderer choice flag
         if (USE_STREAMING_RENDERER == null) {
             String enabled = GeoServerExtensions.getProperty("USE_STREAMING_RENDERER", context);
             // default to true, but allow switching off
             if(enabled == null)
                 USE_STREAMING_RENDERER = false;
             else
                 USE_STREAMING_RENDERER = Boolean.valueOf(enabled);
         }
         
         // initialization of the renderer choice flag
         if (MAX_FILTER_RULES == null) {
            String rules = GeoServerExtensions.getProperty("MAX_FILTER_RULES", context);
             // default to true, but allow switching off
            if(rules == null)
                 MAX_FILTER_RULES = 20;
             else
                MAX_FILTER_RULES = Integer.valueOf(rules);
         }
     }
     
     /**
      * Checks wheter the line width optimization is enabled, or not (defaults to true
      * unless the user sets the OPTIMIZE_LINE_WIDTH property to false)
      * @return
      */
     public static boolean isLineWidthOptimizationEnabled() {
         return OPTIMIZE_LINE_WIDTH;
     }
     
     /**
      * Checks wheter the line streaming renderer is enabled, or not (defaults to false
      * unless the user sets the USE_STREAMING_RENDERER property to true)
      * @return
      */
     public static boolean useStreamingRenderer() {
         return USE_STREAMING_RENDERER;
     }
     
     /**
      * If true (default) use the sld rule filters to compose the query to the DB,
      * otherwise don't and get down only with the bbox and eventual definition filter)
      * @return
      */
     public static int getMaxFilterRules() {
         return MAX_FILTER_RULES;
     }
 
     /**
      * @see WebMapService#getCapabilities(WMSCapabilitiesRequest)
      */
     public WMSCapabilitiesResponse getCapabilities(
             WMSCapabilitiesRequest request) {
         Capabilities capabilities = (Capabilities) context
                 .getBean("wmsGetCapabilities");
 
         return (WMSCapabilitiesResponse) capabilities.getResponse();
     }
 
     /**
      * @see WebMapService#capabilities(WMSCapabilitiesRequest)
      */
     public WMSCapabilitiesResponse capabilities(WMSCapabilitiesRequest request) {
         return getCapabilities(request);
     }
 
     /**
      * @see WebMapService#describeLayer(DescribeLayerRequest)
      */
     public DescribeLayerResponse describeLayer(DescribeLayerRequest request) {
         DescribeLayer describeLayer = (DescribeLayer) context
                 .getBean("wmsDescribeLayer");
 
         return (DescribeLayerResponse) describeLayer.getResponse();
     }
 
     /**
      * @see WebMapService#getMap(GetMapRequest)
      */
     public GetMapResponse getMap(GetMapRequest request) {
         return new GetMapResponse(WMSExtensions.findMapProducers(context));
     }
 
     /**
      * @see WebMapService#map(GetMapRequest)
      */
     public GetMapResponse map(GetMapRequest request) {
         return getMap(request);
     }
 
     /**
      * @see WebMapService#getFeatureInfo(GetFeatureInfoRequest)
      */
     public GetFeatureInfoResponse getFeatureInfo(GetFeatureInfoRequest request) {
         GetFeatureInfo getFeatureInfo = 
             (GetFeatureInfo) context.getBean("wmsGetFeatureInfo");
 
         return (GetFeatureInfoResponse) getFeatureInfo.getResponse();
     }
 
     /**
      * @see WebMapService#getLegendGraphic(GetLegendGraphicRequest)
      */
     public GetLegendGraphicResponse getLegendGraphic(
             GetLegendGraphicRequest request) {
         GetLegendGraphic getLegendGraphic = 
             (GetLegendGraphic) context.getBean("wmsGetLegendGraphic");
 
         return (GetLegendGraphicResponse) getLegendGraphic.getResponse();
     }
 
     public void kml(GetMapRequest getMap, HttpServletResponse response){
         try{
             KMLReflector.doWms(getMap, response, this);
             // return response;
         } catch (Exception e){
             throw new RuntimeException(e);
         }
     }
 
     /**
      * @see WebMapService#reflect(GetMapRequest)
      */
     public GetMapResponse reflect(GetMapRequest request) {
         return getMapReflect(request);
     }
 
     /**
      * @see WebMapService#getMapReflect(GetMapRequest)
      */
     public GetMapResponse getMapReflect(GetMapRequest request) {
         GetMapRequest getMap = (GetMapRequest) request;
 
         // set the defaults
         if (getMap.getFormat() == null) {
             getMap.setFormat(FORMAT);
         }
 
         if ((getMap.getStyles() == null) || getMap.getStyles().isEmpty()) {
             // set styles to be the defaults for the specified layers
             // TODO: should this be part of core WMS logic? is so lets throw
             // this
             // into the GetMapKvpRequestReader
             if ((getMap.getLayers() != null) && (getMap.getLayers().length > 0)) {
                 ArrayList styles = new ArrayList(getMap.getLayers().length);
 
                 for (int i = 0; i < getMap.getLayers().length; i++) {
                     styles.add(getMap.getLayers()[i].getDefaultStyle());
                 }
 
                 getMap.setStyles(styles);
             } else {
                 getMap.setStyles(STYLES);
             }
         }
 
         // auto-magic missing info configuration
         autoSetBoundsAndSize(getMap);
 
         return getMap(getMap);
     }
 
     /**
      * This method tries to automatically determine SRS, bounding box and output
      * size based on the layers provided by the user and any other parameters.
      * 
      * If bounds are not specified by the user, they are automatically se to the
      * union of the bounds of all layers.
      * 
      * The size of the output image defaults to 512 pixels, the height is
      * automatically determined based on the width to height ratio of the
      * requested layers. This is also true if either height or width are
      * specified by the user. If both height and width are specified by the
      * user, the automatically determined bounding box will be adjusted to fit
      * inside these bounds.
      * 
      * General idea
      * 1) Figure out whether SRS has been specified, fall back to EPSG:4326
      * 2) Determine whether all requested layers use the same SRS, 
      *   - if so, try to do bounding box calculations in native coordinates
      * 3) Aggregate the bounding boxes (in EPSG:4326 or native)
      * 4a) If bounding box has been specified, adjust height of image to match 
      * 4b) If bounding box has not been specified, but height has, adjust bounding box
      */
     public static void autoSetBoundsAndSize(GetMapRequest getMap) {
         // Get the layers
         MapLayerInfo[] layers = getMap.getLayers();        
         
         /** 1) Check what SRS has been requested */
         String reqSRS = getMap.getSRS();
         
         // if none, try to determine which SRS to use
         // and keep track of whether we can use native all the way
         boolean useNativeBounds = true;
         if(reqSRS == null) {
             reqSRS = guessCommonSRS(layers);
             forceSRS(getMap, reqSRS);
         } 
         
         /** 2) Compare requested SRS */
         for (int i = 0; useNativeBounds && i < layers.length; i++) {
             if (layers[i] != null) {
                 String layerSRS = layers[i].getSRS();
                 useNativeBounds = reqSRS.equalsIgnoreCase(layerSRS);
             } else {
                 useNativeBounds = false;
             }
         }
         
         CoordinateReferenceSystem reqCRS;
         try {
             reqCRS = CRS.decode(reqSRS);
         } catch(Exception e) {
             throw new WmsException(e);
         }
         
         // Ready to determine the bounds based on the layers, if not specified
         Envelope aggregateBbox = getMap.getBbox();
         boolean specifiedBbox = true;
 
         // If bbox is not specified by request
         if (aggregateBbox == null) {
             specifiedBbox = false;
 
             // Get the bounding box from the layers
             for (int i = 0; i < layers.length; i++) {
                 MapLayerInfo layerInfo = layers[i];
                 ReferencedEnvelope curbbox;
                 try{
                     curbbox = layerInfo.getLatLongBoundingBox();
                     if(useNativeBounds){
                         ReferencedEnvelope nativeBbox = layerInfo.getBoundingBox();
                         if(nativeBbox == null){
                             try {
                                 CoordinateReferenceSystem nativeCrs = layerInfo.getCoordinateReferenceSystem();
                                 nativeBbox = curbbox.transform(nativeCrs, true);
                             } catch(Exception e) {
                                 throw new WmsException("Best effort native bbox computation failed", "", e);
                             }
                         }
                         curbbox = nativeBbox;
                     }
                 }catch(Exception e){
                     throw new RuntimeException(e);
                 }
                 if (aggregateBbox != null) {
                     aggregateBbox.expandToInclude(curbbox);
                 } else {
                     aggregateBbox = curbbox;
                 }
             }
    
             ReferencedEnvelope ref = null;
             // Reproject back to requested SRS if we have to
             if (!useNativeBounds && ! reqSRS.equalsIgnoreCase(SRS)) {
                 try {
                    ref = new ReferencedEnvelope(aggregateBbox,
                             CRS.decode("EPSG:4326"));
                     aggregateBbox = ref.transform(reqCRS, true);
                 } catch (ProjectionException pe) {
                     ref.expandBy( -1 * ref.getWidth() / 50, -1 * ref.getHeight() / 50);
                     try {
                         aggregateBbox = ref.transform(reqCRS, true);
                     } catch (FactoryException e) {
                         e.printStackTrace();
                     } catch (TransformException e) {
                         e.printStackTrace();
                     }
                     // And again...
                 } catch (NoSuchAuthorityCodeException e) {
                     e.printStackTrace();
                 } catch (TransformException e) {
                     e.printStackTrace();
                 } catch (FactoryException e) {
                     e.printStackTrace();
                 }
             }
         }
 
         // Just in case
         if (aggregateBbox == null) {
             forceSRS(getMap, DefaultWebMapService.SRS);
             aggregateBbox = DefaultWebMapService.BBOX;   
         }
 
         // Start the processing of adjust either the bounding box
         // or the pixel height / width
         
         double bbheight = aggregateBbox.getHeight();
         double bbwidth = aggregateBbox.getWidth();
         double bbratio = bbwidth / bbheight;
 
         double mheight = getMap.getHeight();
         double mwidth = getMap.getWidth();
         
         if (mheight > 0.5 && mwidth > 0.5 && specifiedBbox) {
             // This person really doesnt want our help,
             // we'll warp it any way they like it...
         } else {
             if (mheight > 0.5 && mwidth > 0.5) {
                 // Fully specified, need to adjust bbox
                 double mratio = mwidth / mheight;
                 // Adjust bounds to be less than ideal to meet spec
                 if (bbratio > mratio) {
                     // Too wide, need to increase height of bb
                     double diff = ((bbwidth / mratio) - bbheight) / 2;
                     aggregateBbox.expandBy(0, diff);
                 } else {
                     // Too tall, need to increase width of bb
                     double diff = ((bbheight * mratio) - bbwidth) / 2;
                     aggregateBbox.expandBy(diff, 0);
                 }
                 
                 adjustBounds(reqSRS, aggregateBbox);
                 
             } else if (mheight > 0.5) {
                 mwidth = bbratio * mheight;
             } else {
                 if (mwidth > 0.5) {
                     mheight = (mwidth / bbratio >= 1) ? mwidth / bbratio : 1;
                 } else {
                     if(bbratio > 1) {
                         mwidth = MAX_SIDE;
                         mheight = (mwidth / bbratio >= 1) ? mwidth / bbratio : 1;
                     } else {
                         mheight = MAX_SIDE;
                         mwidth = (mheight * bbratio >= 1) ? mheight * bbratio : 1;
                     }
                     
                     // make sure OL output height is sufficient to show the OL scale bar fully
                     if(mheight < MIN_OL_HEIGHT && (
                             "application/openlayers".equalsIgnoreCase(getMap.getFormat()) 
                             || "openlayers".equalsIgnoreCase(getMap.getFormat()))) {
                         mheight = MIN_OL_HEIGHT;
                         mwidth = (mheight * bbratio >= 1) ? mheight * bbratio : 1;
                     }
                         
                 }
                 
             }
 
             // Actually set the bounding box and size of image
             getMap.setBbox(aggregateBbox);
             getMap.setWidth((int) mwidth);
             getMap.setHeight((int) mheight);
         }
     }
     
     private static String guessCommonSRS(MapLayerInfo[] layers) {
         String SRS = null;
         for (MapLayerInfo layer : layers) {
             String layerSRS = layer.getSRS();
             if(SRS == null) {
                 SRS = layerSRS.toUpperCase();
             } else if(!SRS.equals(layerSRS)) {
                 // layers with mixed native SRS, let's just use the default
                 return DefaultWebMapService.SRS;
             }
         }
         if(SRS == null) {
             return DefaultWebMapService.SRS;
         }
         return SRS;
     }
 
     private static void forceSRS(GetMapRequest getMap, String srs) {
         getMap.setSRS(srs);
         
         try {
             getMap.setCrs( CRS.decode(srs) );
         } catch (NoSuchAuthorityCodeException e) {
             e.printStackTrace();
         } catch (FactoryException e) {
             e.printStackTrace();
         }
     }
     
     /**
      * This adjusts the bounds by zooming out 2%, but also ensuring that
      * the maximum bounds do not exceed the world bounding box
      * 
      * This only applies if the SRS is EPSG:4326 or EPSG:900913
      * 
      * @param reqSRS the SRS 
      * @param bbox the current bounding box
      * @return the adjusted bounding box
      */
     private static Envelope adjustBounds(String reqSRS, Envelope bbox) {        
         if(reqSRS.equalsIgnoreCase("EPSG:4326")) {
             bbox.expandBy(bbox.getWidth() / 100, bbox.getHeight() / 100);
             Envelope maxEnv = new Envelope(
                     -180.0,-90.0,
                     180.0,90.0 );
             return bbox.intersection(maxEnv);
             
         } else if(reqSRS.equalsIgnoreCase("EPSG:900913")) {
             bbox.expandBy(bbox.getWidth() / 100, bbox.getHeight() / 100);
             Envelope maxEnv = new Envelope(
                     -20037508.33, -20037508.33,
                     20037508.33, 20037508.33 );
             return bbox.intersection(maxEnv);
         }
         return bbox;
     }
 }
