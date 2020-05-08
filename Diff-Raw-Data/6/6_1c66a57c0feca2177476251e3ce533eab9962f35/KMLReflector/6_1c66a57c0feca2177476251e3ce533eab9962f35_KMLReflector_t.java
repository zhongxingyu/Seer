 /* Copyright (c) 2001 - 2007 TOPP - www.openplans.org. All rights reserved.
  * This code is licensed under the GPL 2.0 license, availible at the root
  * application directory.
  */
 package org.vfny.geoserver.wms.responses.map.kml;
 
 import java.nio.charset.Charset;
 import java.util.Map;
 import java.util.HashMap;
 import java.util.logging.Logger;
 
 import javax.servlet.http.HttpServletResponse;
 
 import org.geoserver.wms.WebMapService;
import org.vfny.geoserver.wms.WmsException;
 import org.vfny.geoserver.wms.requests.GetMapRequest;
 import org.vfny.geoserver.wms.responses.GetMapResponse;
 
 
 /**
  * KML reflecting service.
  * <p>
  * This
  * </p>
  *
  * @author Justin Deoliveira, The Open Planning Project, jdeolive@openplans.org
  *
  */
 public class KMLReflector {
     private static Logger LOGGER = 
         org.geotools.util.logging.Logging.getLogger("org.vfny.geoserver.wms.responses.map.kml");
 
     /** default 'kmscore' value */
     public static final Integer KMSCORE = new Integer(50);
 
     /** default 'kmattr' value */
     public static final Boolean KMATTR = Boolean.TRUE;
 
     /** default 'kmplacemark' value */
     public static final Boolean KMPLACEMARK = Boolean.FALSE;
 
     /** default 'format' value */
     public static final String FORMAT = KMLMapProducer.MIME_TYPE;
 
     private static Map<String, Map<String, String> > MODES;
 
     static {
         Map temp = new HashMap();
         Map options;
         
         options = new HashMap();
         options.put("superoverlay", true);
         options.put("regionatemode", "overview");
         temp.put("overview", options);
 
         options = new HashMap();
         options.put("superoverlay", true);
         options.put("regionatemode", "background");
         temp.put("background", options);
 
         options = new HashMap();
         options.put("superoverlay", false);
         options.put("regionatemode", null);
         options.put("kmscore", null);
         temp.put("flat", options);
 
         options = new HashMap();
         options.put("superoverlay", true);
         options.put("regionatemode", null);
         temp.put("vector", options);
 
         options = new HashMap();
         options.put("superoverlay", true);
         options.put("regionatemode", "raster");
         temp.put("raster", options);
 
         options = new HashMap();
         options.put("superoverlay", false);
         temp.put("onstop", options);
 
         MODES = temp;
     }
 
     /**
      * web map service
      */
     WebMapService wms;
 
     public KMLReflector(WebMapService wms) {
         this.wms = wms;
     }
 
     public void wms(GetMapRequest request, HttpServletResponse response) throws Exception {
         doWms(request, response, wms);
     }
         
     public static void doWms(GetMapRequest request, HttpServletResponse response, WebMapService wms)
         throws Exception {
 
 
         String mode = "vector";
 
         for (Object key : request.getHttpServletRequest().getParameterMap().keySet())
             if (key instanceof String && "mode".equalsIgnoreCase((String)key)){
                 Object value = request.getHttpServletRequest().getParameter((String)key);
                 mode = (value instanceof String) ? ((String) value).toLowerCase() : mode;
             }

        if (!MODES.containsKey(mode)){
            throw new WmsException("Unknown KML mode: " + mode);
        }
                 
         //first set up some of the normal wms defaults
         if ( request.getWidth() < 1 ) {
             request.setWidth(mode.equals("onstop") ? 1024 : 256);
         } 
 
         if ( request.getHeight() < 1 ) {
             request.setHeight(mode.equals("onstop") ? 1024 : 256);
         }
 
         // Force srs to lat/lon for KML output.
         request.setSRS("EPSG:4326");
         
         //set rest of the wms defaults
         wms.reflect(request);
         
         //set some kml specific defaults
         Map fo = request.getFormatOptions();
         if ( fo.get( "kmattr") == null ) {
             fo.put( "kmattr", KMATTR );
         } if ( fo.get( "kmscore" ) == null ) {
             fo.put( "kmscore", KMSCORE );
         } if (fo.get("kmplacemark") == null) {
             fo.put("kmplacemark", KMPLACEMARK);
         }
 
         merge(fo, MODES.get(mode));
         
         //set the format
         //TODO: create a subclass of GetMapRequest to store these values
  
         Boolean superoverlay = (Boolean)fo.get("superoverlay");
         if (superoverlay == null) superoverlay = Boolean.FALSE;
         if (superoverlay) {
             request.setFormat(KMLMapProducer.MIME_TYPE);
             request.setBbox(KMLUtils.expandToTile(request.getBbox()));
         } else {
             request.setFormat( KMZMapProducer.MIME_TYPE );
         }
 
         response.setContentType( KMLMapProducer.MIME_TYPE );
 
         //set the content disposition
         StringBuffer filename = new StringBuffer();
         for ( int i = 0; i < request.getLayers().length; i++ ) {
             String name = request.getLayers()[i].getName();
 
             //strip off prefix
             int j = name.indexOf(':');
             if ( j > -1 ) {
                 name = name.substring( j + 1 );
             }
 
             filename.append(name + "_");
         }
         filename.setLength(filename.length()-1);
         response.setHeader("Content-Disposition", 
                 "attachment; filename=" + filename.toString() + ".kml");
 
         if ("flat".equals(mode)){
             GetMapResponse wmsResponse = wms.getMap(request);
             wmsResponse.execute(request);
             wmsResponse.writeTo(response.getOutputStream());
         } else {
             KMLNetworkLinkTransformer transformer = new KMLNetworkLinkTransformer();
             transformer.setIndentation(3);
             Charset encoding = request.getWMS().getCharSet();
             transformer.setEncoding(encoding);
             transformer.setEncodeAsRegion(superoverlay);
             transformer.transform( request, response.getOutputStream() );
         }
     }
 
     private static void merge(Map options, Map addition){
         for (Object o : addition.entrySet()){
             Map.Entry entry = (Map.Entry) o;
             if (entry.getValue() == null) 
                 options.remove(entry.getKey());
             else
                 options.put(entry.getKey(), entry.getValue());
         }
     }
 }
