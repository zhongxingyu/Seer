 package org.geoserver.geosearch;
 
 import java.io.IOException;
 import java.io.OutputStream;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.logging.Logger;
 
 import org.geoserver.ows.KvpParser;
 import org.geoserver.ows.util.CaseInsensitiveMap;
 import org.geoserver.platform.GeoServerExtensions;
 import org.geoserver.rest.RESTUtils;
 import org.geoserver.wms.kvp.GetMapKvpRequestReader;
 import org.geotools.util.logging.Logging;
 import org.restlet.data.MediaType;
 import org.restlet.data.Method;
 import org.restlet.data.Request;
 import org.restlet.data.Response;
 import org.restlet.data.Status;
 import org.restlet.resource.OutputRepresentation;
 import org.springframework.context.ApplicationContext;
 import org.springframework.context.ApplicationContextAware;
 import org.vfny.geoserver.config.DataConfig;
 import org.vfny.geoserver.global.Data;
 import org.vfny.geoserver.global.WMS;
 import org.vfny.geoserver.wms.requests.GetMapRequest;
 import org.vfny.geoserver.wms.responses.GetMapResponse;
 import org.vfny.geoserver.wms.responses.map.kml.GeoSearchMapProducerFactory;
 import org.vfny.geoserver.wms.servlets.GetMap;
 
 public class FeatureRestlet extends GeoServerProxyAwareRestlet implements ApplicationContextAware {
     private static Logger LOGGER = Logging.getLogger("org.geoserver.geosearch");
 
     private Data myData;
     private DataConfig myDataConfig;
     private WMS myWMS;
     private GetMap myGetMap;
     private ApplicationContext myContext;
 
     public void setData(Data d){
         myData = d;
     }
 
     public void setGetMap(GetMap gm){
         myGetMap = gm;
     }
 
     public void setWms(WMS wms){
         myWMS = wms;
     }
 
     public void setDataConfig(DataConfig dc){
         myDataConfig = dc;
     }
 
     public void setApplicationContext(ApplicationContext context){
         myContext = context;
     }
 
     public Data getData(){
         return myData;
     }
 
     public GetMap getGetMap(){
         return myGetMap;
     }
 
     public WMS getWms(){
         return myWMS;
     }
 
     public DataConfig getDataConfig(){
         return myDataConfig;
     }
 
     public ApplicationContext getApplicationContext(){
         return myContext;
     }
 
     public FeatureRestlet() {
     }
 
     public void handle(Request request, Response response){
         try{
             if (request.getMethod().equals(Method.GET)){
                 doGet(request, response);
             } else {
                 response.setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
             }
         } catch (Exception e){
             LOGGER.severe("Badness (" + e + ") while handling layer request!");
             e.printStackTrace();
             response.setStatus(Status.SERVER_ERROR_INTERNAL);
         }
     }
 
     public void doGet(Request request, Response response) throws Exception{
         String namespace = (String)request.getAttributes().get("namespace");
         String layername = (String)request.getAttributes().get("layer");
         String featureId = (String)request.getAttributes().get("feature");
         String format    = (String)request.getAttributes().get("format");
        String bu = getBaseURL(request);
        int lastslash = bu.lastIndexOf("/");
        bu = bu.substring(0, lastslash);
        GeoSearchMapProducerFactory.BASE_URL = bu;
         
         if (request.getMethod().equals(Method.GET)) {
             GetMapKvpRequestReader reader = new GetMapKvpRequestReader(getWms());
             reader.setHttpRequest( RESTUtils.getServletRequest( request ) );
             
             Map raw = new HashMap();
             raw.put("layers", namespace + ":" + layername); 
             raw.put("styles", "polygon");
            raw.put("format", "geosearch-kml");
             raw.put("srs", "epsg:4326");
             raw.put("bbox", "-180,-90,180,90");
             raw.put("height", "600");
             raw.put("width", "800");
             raw.put("featureid", layername + "." + featureId);
 
             final GetMapRequest gmreq = (GetMapRequest) reader.read((GetMapRequest) reader.createRequest(), parseKvp(raw), raw);
             
             final GetMapResponse gmresp = new GetMapResponse(getWms(), getApplicationContext());
             response.setEntity(new OutputRepresentation(new MediaType("application/xml+kml")){
                     public void write(OutputStream os){
                     try{
                     gmresp.execute(gmreq);
                     gmresp.writeTo(os);
                     } catch (IOException ioe){
                     // blah, this will never happen
                     }
                     }
 
                     });
 
         } else {
             response.setStatus(Status.CLIENT_ERROR_NOT_FOUND);
         }
     }
 
     /**
      * Parses a raw set of kvp's into a parsed set of kvps.
      *
      * @param kvp Map of String,String.
      */
     protected Map parseKvp(Map /*<String,String>*/ raw)
         throws Exception {
             //parse the raw values
             List parsers = GeoServerExtensions.extensions(KvpParser.class, getApplicationContext());
             Map kvp = new CaseInsensitiveMap(new HashMap());
 
             for (Iterator e = raw.entrySet().iterator(); e.hasNext();) {
                 Map.Entry entry = (Map.Entry) e.next();
                 String key = (String) entry.getKey();
                 String val = (String) entry.getValue();
                 Object parsed = null;
 
                 for (Iterator p = parsers.iterator(); p.hasNext();) {
                     KvpParser parser = (KvpParser) p.next();
 
                     if (key.equalsIgnoreCase(parser.getKey())) {
                         parsed = parser.parse(val);
 
                         if (parsed != null) {
                             break;
                         }
                     }
                 }
 
                 if (parsed == null) {
                     parsed = val;
                 }
 
                 kvp.put(key, parsed);
             }
 
             return kvp;
         }
 }
 
