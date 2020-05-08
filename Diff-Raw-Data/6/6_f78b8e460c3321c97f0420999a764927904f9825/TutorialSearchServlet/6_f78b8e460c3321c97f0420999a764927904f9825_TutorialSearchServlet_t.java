 package com.t11e.web;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.OutputStream;
 import java.io.OutputStreamWriter;
 import java.net.HttpURLConnection;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.Map;
 
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.json.simple.JSONValue;
 
 public class TutorialSearchServlet
   extends HttpServlet
 {
   private static final long serialVersionUID = 8872041268396435423L;
 
  @SuppressWarnings("unchecked")
   @Override
   protected void doGet(
     final HttpServletRequest request,
     final HttpServletResponse response)
     throws IOException
   {
 
     final Map<String, Object> discoveryRequest =
      buildDiscoveryRequest(request.getParameterMap());
 
     final Map<String, Object> discoveryResponse =
       queryDiscoveryEngine(discoveryRequest);
 
     final Map<String, Object> searchResponse =
       buildSearchResponse(discoveryRequest, discoveryResponse);
 
     response.setContentType("application/json");
     JSONValue.writeJSONString(searchResponse, response.getWriter());
   }
 
   private Map<String, Object> buildSearchResponse(
     final Map<String, Object> discoveryRequest,
     final Map<String, Object> discoveryResponse)
   {
     final Map<String, Object> searchResponse = new LinkedHashMap<String, Object>();
     {
       final Map<String, Object> _discovery = new LinkedHashMap<String, Object>();
       searchResponse.put("_discovery", _discovery);
       _discovery.put("request", discoveryRequest);
       _discovery.put("response", discoveryResponse);
     }
     return searchResponse;
   }
 
   private Map<String, Object> buildDiscoveryRequest(
     Map<String, String[]> parameters)
   {
     final Map<String, Object> request = new LinkedHashMap<String, Object>();
     final List<Map<String, Object>> searchCriteria = new ArrayList<Map<String,Object>>();
     final int pageSize = 4;
     request.put("criteria", searchCriteria);
     request.put("pageSize", pageSize);
     if (parameters.containsKey("page"))
     {
       final int page = Integer.parseInt(parameters.get("page")[0], 10);
       final int startIndex = (page - 1) * pageSize;
       request.put("startIndex", startIndex);
     }
 
     if (parameters.containsKey("shape"))
     {
       final Map<String, Object> criterion = new LinkedHashMap<String, Object>();
       searchCriteria.add(criterion);
       criterion.put("dimension", "shape");
       criterion.put("id", Arrays.asList(parameters.get("shape")));
     }
     if (parameters.containsKey("tags"))
     {
       final List<String> tags =
         Arrays.asList(parameters.get("tags")[0].split("\\s+"));
       final Map<String, Object> criterion = new LinkedHashMap<String, Object>();
       searchCriteria.add(criterion);
       criterion.put("dimension", "tags");
       criterion.put("value", tags);
     }
     if (parameters.containsKey("price_min") || parameters.containsKey("price_max"))
     {
       final Map<String, Object> criterion = new LinkedHashMap<String, Object>();
       searchCriteria.add(criterion);
       criterion.put("dimension", "price");
       final StringBuilder buf = new StringBuilder();
       buf.append("[");
       if (parameters.containsKey("price_min"))
       {
         buf.append(parameters.get("price_min")[0]);
       }
       buf.append(",");
       if (parameters.containsKey("price_max"))
       {
         buf.append(parameters.get("price_max")[0]);
       }
       buf.append("]");
       criterion.put("value", buf.toString());
     }
     if (parameters.containsKey("weight"))
     {
       final Map<String, Object> criterion = new LinkedHashMap<String, Object>();
       searchCriteria.add(criterion);
       criterion.put("dimension", "weight");
       criterion.put("value", Arrays.asList(parameters.get("weight")));
     }
 
     final List<Map<String, Object>> drilldownCriteria = new ArrayList<Map<String,Object>>();
     request.put("drillDown", drilldownCriteria);
     {
       final Map<String, Object> criterion = new LinkedHashMap<String, Object>();
       drilldownCriteria.add(criterion);
       criterion.put("dimension", "shape");
       criterion.put("ids", Arrays.asList(new String[] {"square", "circle", "triangle"}));
 
     }
     return request;
 
   }
 
  @SuppressWarnings("unchecked")
   private Map<String, Object> queryDiscoveryEngine(
     final Map<String, Object> request)
     throws IOException
   {
     final String engineUrl = "http://tutorial.discoverysearchengine.com/json/query";
     final HttpURLConnection connection = (HttpURLConnection) new URL(engineUrl).openConnection();
     connection.setRequestMethod("POST");
     connection.setDoInput(true);
     connection.setDoOutput(true);
     connection.addRequestProperty("Content-Type", "application/json; charset=utf-8");
     final OutputStream out = connection.getOutputStream();
     final OutputStreamWriter writer = new OutputStreamWriter(out, "utf-8");
     JSONValue.writeJSONString(request, writer);
     writer.flush();
     final int responseCode = connection.getResponseCode();
     if (responseCode < 200 || responseCode > 299)
     {
       throw new RuntimeException("Engine HTTP error [" + responseCode + "] "
         + connection.getResponseMessage());
     }
     final InputStream inputStream = connection.getInputStream();
     final Map<String, Object> result =
       (Map<String, Object>) JSONValue.parse(new InputStreamReader(inputStream, "utf-8")); // safe assumption engine uses utf-8
     return result;
   }
 }
