 /*******************************************************************************
  * Copyright (c) 2006-2010 eBay Inc. All Rights Reserved.
  * Licensed under the Apache License, Version 2.0 (the "License"); 
  * you may not use this file except in compliance with the License. 
  * You may obtain a copy of the License at 
  *
  *    http://www.apache.org/licenses/LICENSE-2.0
  *******************************************************************************/
 package org.ebayopensource.turmeric.monitoring.server;
 
 import java.io.IOException;
 import java.net.MalformedURLException;
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Date;
 import java.util.Enumeration;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.TimeZone;
 
 import javax.servlet.ServletConfig;
 import javax.servlet.ServletContext;
 import javax.servlet.ServletException;
 import javax.servlet.ServletRequest;
 import javax.servlet.ServletResponse;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.eclipse.jetty.client.ContentExchange;
 import org.eclipse.jetty.client.HttpClient;
 import org.eclipse.jetty.continuation.Continuation;
 import org.eclipse.jetty.continuation.ContinuationSupport;
 import org.eclipse.jetty.http.HttpHeaders;
 import org.eclipse.jetty.http.HttpSchemes;
 import org.eclipse.jetty.http.HttpURI;
 import org.eclipse.jetty.http.MimeTypes;
 import org.eclipse.jetty.io.Buffer;
 import org.eclipse.jetty.util.ajax.JSON;
 
 public class DownloadServlet extends HttpServlet {
     public static final String EMPTY_STRING = "\"\"";
     public static final String NS = "ns";
     /* MetricCriteria */
     public static final String METRIC_CRITERIA = "metricCriteria";
     public static final String METRIC_NAME = "metricName";
     public static final String FIRST_START_TIME = "firstStartTime";
     public static final String SECOND_START_TIME = "secondStartTime";
     public static final String DURATION = "duration";
     public static final String ROLE_TYPE = "roleType";
     
     /* MetricResourceCriteria */
     public static final String METRIC_RESOURCE_CRITERIA = "metricResourceCriteria";
     public static final String RESOURCE_ENTITY_RESPONSE_TYPE = "resourceEntityResponseType";
     
     public static final String GET_METRICS_DATA_RESPONSE = "getMetricsDataResponse";
     public static final String GET_ERROR_METRICS_DATA_RESPONSE = "getErrorMetricsDataResponse";
 
     
     public static final String RETURN_DATA = "returnData";
     
     /* MetricGroupData */
     public static final String CRITERIA_INFO = "criteriaInfo";
     public static final String DIFF = "diff";
     public static final String COUNT1 = "count1";
     public static final String COUNT2 = "count2";
     
     /* ErrorViewData */
     public static final String CRITERIA = "criteria";
     public static final String RATIO_DIFF = "ratioDiff";
     public static final String ERROR_DIFF = "errorDiff";
     public static final String ERROR_CALL_RATIO2 = "errorCallRatio2";
     public static final String ERROR_CALL_RATIO1 = "errorCallRatio1";
     public static final String ERROR_COUNT1 = "errorCount1";
     public static final String ERROR_COUNT2 = "errorCount2";
 
     private Map<String, String> metricCriteria = new HashMap<String,String>();
     private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd_HH_mm_ss_SSS");
     private SimpleDateFormat gmtFormat;
     private TimeZone gmt;
     private ServletConfig config;
     private ServletContext context;
     private String metricsQueryServiceURL;
     private String myPrefix;
     private HttpClient client;
    
     
     @Override
     public void init(ServletConfig config) throws ServletException {
         super.init(config);
         this.config=config;
         this.context=config.getServletContext();
         
         String[] ids = TimeZone.getAvailableIDs(0);
         gmt = TimeZone.getTimeZone(ids[0]); //GMT
         gmtFormat = new SimpleDateFormat("yyyy MM dd hh:00");
         gmtFormat.setTimeZone(gmt);
         
         if (config.getInitParameter("SOAMetricsQueryServiceURL")!=null)
             metricsQueryServiceURL = config.getInitParameter("SOAMetricsQueryServiceURL");
         
         if (config.getInitParameter("myPrefix")!=null)
             myPrefix=config.getInitParameter("myPrefix");
     
         System.err.println("myPrefix="+myPrefix);
         client=new HttpClient();
         client.setConnectorType(HttpClient.CONNECTOR_SELECT_CHANNEL);
         try {
             client.start();
         } catch (Exception e) {
             e.printStackTrace();
         }
 
     }
 
     public void service(ServletRequest req, ServletResponse res) throws ServletException, IOException {
         HttpServletRequest request = (HttpServletRequest)req;
         HttpServletResponse response = (HttpServletResponse)res;
         
         String json = (String)request.getAttribute("RESULT");
         Boolean error = (Boolean)request.getAttribute("ERROR");
         if (json == null && error == null) {
             
             final Continuation continuation = ContinuationSupport.getContinuation(request);
             continuation.setTimeout(10000);
             continuation.suspend();
 
             String tmp=request.getRequestURI();
             if (request.getQueryString()!=null)
                 tmp+="?"+request.getQueryString();
 
             HttpURI uri = null;
             if (tmp.startsWith(myPrefix)) {
                 try {
                     uri = new HttpURI(new URI(metricsQueryServiceURL + tmp.substring(myPrefix.length())).normalize().toString());
                 } catch (URISyntaxException e) {
                     throw new MalformedURLException(e.getMessage());
                 }
             } else {
                 response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid url for download: "+tmp);
                 return;
             }
 
             send (request, response, continuation, uri); 
             return;
         } 
 
         if (error != null) {
             response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
         } else {
             final String filename = computeFilename (request);
             String csv = convertToCSV(request, json);
             response.setContentType(MimeTypes.TEXT_HTML);
             response.setHeader("Content-disposition", "attachment; filename="+filename);
             response.getOutputStream().write(csv.getBytes());
         }
     }
     
     public void send (final HttpServletRequest request, final HttpServletResponse response, final Continuation continuation, final HttpURI uri) throws IOException {
         
         ContentExchange exchange = new ContentExchange() {
             protected void onRequestCommitted() throws IOException {
                 super.onRequestCommitted();
             }
 
             protected void onRequestComplete() throws IOException {
                 super.onRequestComplete();
             }
 
             protected void onResponseComplete() throws IOException {
                 String content = getResponseContent();
                 if (content == null) {
                     request.setAttribute("ERROR", Boolean.TRUE);
                 } else {
                     //convert json content to csv
                     request.setAttribute("RESULT", content);
                 }
 
                 continuation.resume();
             }
 
 
             protected void onResponseHeader(Buffer name, Buffer value) throws IOException {
                 String s = name.toString().toLowerCase();
                 if (s.startsWith("X-TURMERIC-ERROR") && !response.isCommitted()) {
                     request.setAttribute("ERROR", Boolean.TRUE);
                 }    
                 super.onResponseHeader(name, value);
             }
 
             protected void onConnectionFailed(Throwable ex) {
                 onException(ex);
             }
 
             protected void onException(Throwable ex) {
                 request.setAttribute("ERROR", Boolean.TRUE);
                 continuation.resume();
             }
 
             protected void onExpire() {
                 request.setAttribute("ERROR", Boolean.TRUE);
                 continuation.resume();
             }
 
         };
 
         exchange.setScheme(HttpSchemes.HTTPS.equals(request.getScheme())?HttpSchemes.HTTPS_BUFFER:HttpSchemes.HTTP_BUFFER);
         exchange.setMethod(request.getMethod());
         exchange.setURL(uri.toString());
         exchange.setVersion(request.getProtocol());
         
         // copy headers
         boolean xForwardedFor=false;
         boolean hasContent=false;
         long contentLength=-1;
         Enumeration<?> enm = request.getHeaderNames();
         while (enm.hasMoreElements()) {
             String hdr=(String)enm.nextElement();
             String lhdr=hdr.toLowerCase();
 
             if ("content-length".equals(lhdr)) {
                 contentLength=request.getContentLength();
                 exchange.setRequestHeader(HttpHeaders.CONTENT_LENGTH,Long.toString(contentLength));
                 if (contentLength>0)
                     hasContent=true;
             }
             else if ("x-forwarded-for".equals(lhdr))
                 xForwardedFor=true;
 
             Enumeration<?> vals = request.getHeaders(hdr);
             while (vals.hasMoreElements()) {
                 String val = (String)vals.nextElement();
                 if (val!=null) {
                     exchange.setRequestHeader(hdr,val);
                 }
             }
         }
         if (hasContent)
             exchange.setRequestContentSource(request.getInputStream());
         
         client.send(exchange);
     }
     
 
     /**
      * Generate the name of the file to return to the browser.
      * The name should be based on:
      * <ol>
      * <li>the metric requested
      * <li>the first and second start times
      * <li>duration
      * <li>aggregation interval
      * </ol>
      * @param request
      * @return
      */
     public String computeFilename (HttpServletRequest request)  throws ServletException {
         StringBuffer name = new StringBuffer();
         name.append(dateFormat.format(new Date()));
         name.append("_");
        String requestName = request.getHeader("X-TURMERIC-OPERATION-NAME");
         
         if ("getMetricsData".equals(requestName) || "getErrorMetricsData".equals(requestName)) {
             //2 part name eg metricCriteria.x
             ArrayList<String> elementNames = new ArrayList<String>(2);
             elementNames.add(METRIC_CRITERIA);
             elementNames.add(METRIC_NAME);
             metricCriteria.put(METRIC_NAME, request.getParameter(computeNameInNamespace(NS, elementNames)));
 
             elementNames.set(1, FIRST_START_TIME);
             metricCriteria.put(FIRST_START_TIME, request.getParameter(computeNameInNamespace(NS, elementNames)));
 
             elementNames.set(1, SECOND_START_TIME);
             metricCriteria.put(SECOND_START_TIME, request.getParameter(computeNameInNamespace(NS, elementNames)));
 
             elementNames.set(1, DURATION);
             metricCriteria.put(DURATION, request.getParameter(computeNameInNamespace(NS, elementNames)));
 
             elementNames.set(1, ROLE_TYPE);
             metricCriteria.put(ROLE_TYPE,request.getParameter(computeNameInNamespace(NS, elementNames)));
 
 
             //consider adding the other metricCriteria into the title
            
             for (Map.Entry<String, String>entry:metricCriteria.entrySet()) {
                 name.append("_");
                 if (entry.getValue() != null)
                     name.append(entry.getValue());
                 else
                     name.append("xxx");
             }
         }
         
         name.append(".csv");
         return name.toString();
     }
     
     
     public String convertToCSV (HttpServletRequest request ,String json) {
        String requestName = request.getHeader("X-TURMERIC-OPERATION-NAME");
         StringBuffer strbuff = new StringBuffer();
         
        if ("getMetricsDataRequest".equals(requestName)) {
             //Output the metric criteria from the request
             strbuff.append(METRIC_NAME).append(",").append(quote(metricCriteria.get(METRIC_NAME))).append("\n");
             strbuff.append(FIRST_START_TIME).append(",").append(metricCriteria.get(FIRST_START_TIME)).append("\n");
             strbuff.append(SECOND_START_TIME).append(",").append(metricCriteria.get(SECOND_START_TIME)).append("\n");
             strbuff.append(DURATION).append(",").append(metricCriteria.get(DURATION)).append("\n");
             strbuff.append(ROLE_TYPE).append(",").append(quote(metricCriteria.get(ROLE_TYPE))).append("\n");
             strbuff.append("\n");
 
             //Get the resource entity response type from the request to help parse the response
             String resourceEntityResponseType = 
                 request.getParameter(computeNameInNamespace(NS, Arrays.asList(new String[] {METRIC_RESOURCE_CRITERIA,RESOURCE_ENTITY_RESPONSE_TYPE})));
 
             //Output the response
             /* 
              * 
              * Consider how better to convert to csv format. It seems this cannot be done
              * in a generic way, but rather we need to have knowledge of the data.
              * Eg so we can convert the times to formatted date strings, the numbers
              * from strings to doubles etc.
              * 
              * Options:
              * It might be possible to use some of the ebay jaxb code to convert the 
              * json to objects? Although the jaxb code seems to expect names with
              * the "ns" namespace. Also, the json does not maintain typing - everything
              * is returned as a String.
              * 
              * We could use the turmeric client interface instead and deal with the
              * objects. Then we generate the csv from the objects. This would mean
              * we would have to demarshall the rest url from the browser into an
              * equivalent call for the turmeric client.
              */
             Map<String, Object> data = (Map<String, Object>)JSON.parse(json);
             Object o = data.get(GET_METRICS_DATA_RESPONSE);
             if (o != null) {
                 Map<String,Object> metricsDataResponse = (Map<String,Object>)o;
                 Object[] returnDatas = (Object[])metricsDataResponse.get(RETURN_DATA);
                 if (returnDatas != null) {
                     //output the heading
                     strbuff.append(quote(resourceEntityResponseType)).append(",").append(COUNT1).append(",").append(COUNT2).append(",").append(DIFF).append("\n");
                     for (Object map:returnDatas) {
                         Map<String,Object> returnData = (Map<String,Object>)map;
                         Map<String,Object> criteriaInfo = (Map<String,Object>)returnData.get(CRITERIA_INFO);
 
                         String type = null;
                         if (resourceEntityResponseType != null) {
                             if ("Service".equals(resourceEntityResponseType)) {
                                 type = (String)criteriaInfo.get("serviceName");
                             } else if ("Operation".equals(resourceEntityResponseType)) {
                                 type = (String)criteriaInfo.get("operationName");
                             } else if ("Consumer".equals(resourceEntityResponseType)) {
                                 type = (String)criteriaInfo.get("consumerName");
                             } else if ("Machine".equals(resourceEntityResponseType)) {
                                 type = (String)criteriaInfo.get("machineName");
                             } else if ("Pool".equals(resourceEntityResponseType)) {
                                 type = (String)criteriaInfo.get("poolName");
                             }
                         }
 
                         String count1 = (String)returnData.get(COUNT1);
                         String count2 = (String)returnData.get(COUNT2);
                         String diff = (String)returnData.get(DIFF);
                         strbuff.append(quote(type)).append(",");
                         strbuff.append(Double.parseDouble(count1)).append(",").append(Double.parseDouble(count2)).append(",").append(Double.parseDouble(diff)).append("\n");
                     }
                 }
             }
         } else if ("getErrorMetricsData".equals(requestName)) {
             //Output the metric criteria from the request
             strbuff.append(METRIC_NAME).append(",").append(quote(metricCriteria.get(METRIC_NAME))).append("\n");
             strbuff.append(FIRST_START_TIME).append(",").append(metricCriteria.get(FIRST_START_TIME)).append("\n");
             strbuff.append(SECOND_START_TIME).append(",").append(metricCriteria.get(SECOND_START_TIME)).append("\n");
             strbuff.append(DURATION).append(",").append(metricCriteria.get(DURATION)).append("\n");
             strbuff.append(ROLE_TYPE).append(",").append(quote(metricCriteria.get(ROLE_TYPE))).append("\n");
             strbuff.append("\n");
             //output the response
             Map<String, Object> data = (Map<String, Object>)JSON.parse(json);
             //output headings
             String firstStartTime  = "";
             String secondStartTime = "";
             String tmp = metricCriteria.get(FIRST_START_TIME);
             if (tmp != null) {
                 try {
                     firstStartTime= gmtFormat.format(new Date(Long.parseLong(tmp)));
                 }catch (NumberFormatException e) {
                     //leave first start time as blank
                 }
             }
             tmp = metricCriteria.get(SECOND_START_TIME);
             if (tmp != null) {
                 try {
                     secondStartTime = gmtFormat.format(new Date(Long.parseLong(tmp)));
                 } catch (NumberFormatException e) {
                     //leave as blank
                 }
             }
             strbuff.append(", , ");
             strbuff.append (firstStartTime);
             strbuff.append(",");
             strbuff.append(secondStartTime);
             strbuff.append(",");
             strbuff.append (firstStartTime); 
             strbuff.append(",");
             strbuff.append(secondStartTime);
             strbuff.append("\n");
             strbuff.append(quote("ERROR ID")).append(",").append("ERROR NAME").append(",").append(COUNT1).append(",").append(COUNT2).append(",").append("RATIO1").append(",").append("RATIO2").append("\n");
             Object o = data.get(GET_ERROR_METRICS_DATA_RESPONSE);
             if (o != null) {
                 Map<String,Object> metricsDataResponse = (Map<String,Object>)o;
                 Object[] returnDatas = (Object[])metricsDataResponse.get(RETURN_DATA);
                 if (returnDatas != null) {
                     for (Object map:returnDatas) {
                         Map<String,Object> returnData = (Map<String,Object>)map;
                         tmp = (String)returnData.get("criteria");
                         if (tmp != null)
                             strbuff.append(quote(tmp));
                         strbuff.append(",");
                         tmp = (String)returnData.get(ERROR_COUNT1);
                         if (tmp != null)
                             strbuff.append(Long.parseLong(tmp));
                         strbuff.append(",");
                         tmp = (String)returnData.get(ERROR_COUNT2);
                         if (tmp != null)
                             strbuff.append(Long.parseLong(tmp));
                         strbuff.append(",");
                         tmp = (String)returnData.get(ERROR_CALL_RATIO1);
                         if (tmp != null)
                             strbuff.append(Double.parseDouble(tmp));
                         strbuff.append(",");
                         tmp = (String)returnData.get(ERROR_CALL_RATIO2);
                         if (tmp != null)
                             strbuff.append(Double.parseDouble(tmp));
                         
                         strbuff.append("\n");
                         
                     }
                 }
             }
         }
          return strbuff.toString();
     }
     
     /**
      * Escape double quotes and quote strings containing
      * commas, newlines and spaces.
      * 
      * @param s
      * @return
      */
     public static String quote (String s) {     
         if (s == null)
             return EMPTY_STRING;
         if ("".equals(s))
             return EMPTY_STRING;
     
         StringBuffer result = new StringBuffer();
         boolean needsQuotes = false;
         for (int i=0;i<s.length();i++) {
             char c = s.charAt(i);
             
             if (c == ',') {
                 needsQuotes = true;
             }
             
             if (Character.isWhitespace(c)) {
                 needsQuotes = true;
             }
                 
             if (c == '"' && c > 0 && c < s.length()-1) {
                 needsQuotes = true;
                 result.append('"');
             }
                
             result.append(c);
         }
         if (needsQuotes)
             return "\""+result.toString()+"\"";
         else
             return result.toString();
     }
     
     public static String computeNameInNamespace(String ns, List<String> names) {
         if (names == null || names.isEmpty())
             return null;
         if (ns == null)
             return null;
         StringBuffer strbuff = new StringBuffer();
         for (int i=0;i<names.size();i++) {
             String name = names.get(i);
             if (i>0)
                 strbuff.append(".");
                 
            strbuff.append(ns).append(":").append(name);
         }
         return strbuff.toString();
     }
 }
