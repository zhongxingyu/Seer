 /* Copyright (c) 2001 - 2007 TOPP - www.openplans.org. All rights reserved.
  * This code is licensed under the GPL 2.0 license, availible at the root
  * application directory.
  */
 package org.vfny.geoserver.wms.responses.map.openlayers;
 
 import freemarker.template.Configuration;
 import freemarker.template.Template;
 import freemarker.template.TemplateException;
 import org.geotools.geometry.jts.ReferencedEnvelope;
 import org.vfny.geoserver.ServiceException;
 import org.vfny.geoserver.global.WMS;
 import org.vfny.geoserver.wms.GetMapProducer;
 import org.vfny.geoserver.wms.WMSMapContext;
 import org.vfny.geoserver.wms.WmsException;
 import java.io.IOException;
 import java.io.OutputStream;
 import java.io.OutputStreamWriter;
 import java.util.ArrayList;
 import java.util.Enumeration;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import javax.servlet.http.HttpServletRequest;
 
 
 public class OpenLayersMapProducer implements GetMapProducer {
     /**
      * Set of parameters that we can ignore, since they are not part of the OpenLayers WMS request
      */
     private static final Set ignoredParameters;
 
     static {
         ignoredParameters = new HashSet();
         ignoredParameters.add("REQUEST");
         ignoredParameters.add("TILED");
         ignoredParameters.add("BBOX");
         ignoredParameters.add("SERVICE");
         ignoredParameters.add("VERSION");
         ignoredParameters.add("FORMAT");
     }
 
     /**
      * static freemaker configuration
      */
     static Configuration cfg;
 
     static {
         cfg = new Configuration();
         cfg.setClassForTemplateLoading(OpenLayersMapProducer.class, "");
     }
 
     /**
      * wms configuration
      */
     WMS wms;
 
     /**
      * The current template
      */
     Template template;
 
     /**
      * The current map context
      */
     WMSMapContext mapContext;
 
     public OpenLayersMapProducer(WMS wms) {
         this.wms = wms;
     }
 
     public void abort() {
         mapContext = null;
         template = null;
     }
 
     public String getContentDisposition() {
         return null;
     }
 
     public String getContentType() throws IllegalStateException {
         return "text/html";
     }
 
     public void produceMap(WMSMapContext map) throws WmsException {
         mapContext = map;
     }
 
     public void writeTo(OutputStream out) throws ServiceException, IOException {
         try {
             //create the template
             Template template = cfg.getTemplate("OpenLayersMapTemplate.ftl");
             HashMap map = new HashMap();
             map.put("context", mapContext);
             map.put("request", mapContext.getRequest());
             map.put("maxResolution", new Double(getMaxResolution(mapContext.getAreaOfInterest())));
             map.put("baseUrl", canonicUrl(mapContext.getRequest().getBaseUrl()));
             map.put("parameters", getLayerParameter(mapContext.getRequest().getHttpServletRequest()));
 
             if (mapContext.getLayerCount() == 1) {
                 map.put("layerName", mapContext.getLayer(0).getTitle());
             } else {
                 map.put("layerName", "Geoserver layers");
             }
 
             template.process(map, new OutputStreamWriter(out));
         } catch (TemplateException e) {
             throw new WmsException(e);
         }
 
         mapContext = null;
         template = null;
     }
 
     /**
      * Returns a list of maps with the name and value of each parameter that we have to
      * forward to OpenLayers. Forwarded parameters are all the provided ones, besides a short
      * set contained in {@link #ignoredParameters}.
      * @param request
      * @return
      */
     private List getLayerParameter(HttpServletRequest request) {
         List result = new ArrayList();
         Enumeration en = request.getParameterNames();
 
         while (en.hasMoreElements()) {
             String paramName = (String) en.nextElement();
 
             if (ignoredParameters.contains(paramName.toUpperCase())) {
                 continue;
             }
 
             // this won't work for multi-valued parameters, but we have none so far (they
             // are common just in HTML forms...)
             Map map = new HashMap();
             map.put("name", paramName);
             map.put("value", request.getParameter(paramName));
             result.add(map);
         }
 
         return result;
     }
 
     /**
      * Makes sure the url does not end with "/", otherwise we would have URL lik
      * "http://localhost:8080/geoserver//wms?LAYERS=..." and Jetty 6.1 won't digest them...
      * @param baseUrl
      * @return
      */
     private String canonicUrl(String baseUrl) {
         if (baseUrl.endsWith("/")) {
             return baseUrl.substring(0, baseUrl.length() - 1);
         } else {
             return baseUrl;
         }
     }
 
     private double getMaxResolution(ReferencedEnvelope areaOfInterest) {
         double w = areaOfInterest.getWidth();
         double h = areaOfInterest.getHeight();
 
         return ((w > h) ? w : h) / 256;
     }
 }
