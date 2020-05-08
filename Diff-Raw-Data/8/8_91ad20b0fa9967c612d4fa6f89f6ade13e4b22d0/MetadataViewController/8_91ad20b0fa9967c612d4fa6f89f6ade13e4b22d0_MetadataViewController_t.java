 package org.esgf.metadata;
 
 import java.io.IOException;
 import java.io.UnsupportedEncodingException;
 import java.net.URLEncoder;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.xml.ws.http.HTTPException;
 
 import org.apache.commons.httpclient.DefaultHttpMethodRetryHandler;
 import org.apache.commons.httpclient.HttpClient;
 import org.apache.commons.httpclient.HttpStatus;
 import org.apache.commons.httpclient.methods.GetMethod;
 import org.apache.commons.httpclient.params.HttpMethodParams;
 import org.esgf.datacart.MIMESElement;
 import org.esgf.datacart.ServicesElement;
 import org.esgf.datacart.URLSElement;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.servlet.ModelAndView;
 
 @Controller
 @RequestMapping(value="/metadataview")
 public class MetadataViewController {
 
     private final static String METADATAVIEW_DATASET_NAME = "MetadataView_Dataset_Name";
     private final static String METADATAVIEW_DATASET_TITLE = "MetadataView_Dataset_Title";
     private final static String METADATAVIEW_DATASET_NUMFILES = "MetadataView_Dataset_NumFiles";
     private final static String METADATAVIEW_DATASET_INDEXNODE = "MetadataView_Dataset_IndexNode";
     private final static String METADATAVIEW_DATASET_XLINK = "MetadataView_Dataset_Xlink";
     private final static String RESPONSE = "Response";
     private static final String METADATAVIEW_KEYARR = "MetadataView_KeyArr";
     private static final String METADATAVIEW_VALUEARR = "MetadataView_ValueArr";
 
    private static String searchAPIURL = "http://localhost/esg-search/search?";
     
     @SuppressWarnings("unchecked")
     @RequestMapping(method=RequestMethod.GET,value="/{dataset_id}.html")
     public ModelAndView doGet(final HttpServletRequest request,@PathVariable("dataset_id") String dataset_id) {
     
         Map<String,Object> model = new HashMap<String,Object>();
 
         String id = dataset_id;
 
         model.put(METADATAVIEW_DATASET_NAME, id);
         
         
         
         String solrQueryString = "";
 
         //add the dataset to the query string
         try {
             solrQueryString += "format=application%2Fsolr%2Bjson&id=" + URLEncoder.encode(id,"UTF-8").toString();
             //System.out.println("\nthis.solrQueryString->\t" + URLEncoder.encode(dataset_id,"UTF-8").toString());
             
         } catch (UnsupportedEncodingException e1) {
             // TODO Auto-generated catch block
             e1.printStackTrace();
         }
         
         JSONObject jsonResponse = this.getJSONResponse(solrQueryString);
         
         Iterator iter = jsonResponse.sortedKeys();
         
         while(iter.hasNext()) {
         
             String key = (String)iter.next();
             
             if(key.equals("docs")) {
                 try {
                     
                     JSONArray jsonarr = new JSONArray(jsonResponse.getString(key));
                     JSONObject jsonobj = (JSONObject)jsonarr.get(0);
                     Iterator iter2 = jsonobj.sortedKeys();
                     List<String> keys = new ArrayList<String>();
                     List<String> values = new ArrayList<String>();
                     
                     while(iter2.hasNext()) {
                         
                         
                         String key2 = (String)iter2.next();
                         
                         String value = "";
                         
 
                         if((jsonobj.get(key2)).getClass().getName().equals("org.esgf.metadata.JSONArray")) {
                             
                             
                             value = this.processJSONArray(jsonobj, key2);
                             
                             if(key2.equals("number_of_files")) {
                                 model.put(METADATAVIEW_DATASET_NUMFILES, value);
                             } else if(key2.equals("index_node")) {
                                 model.put(METADATAVIEW_DATASET_INDEXNODE, value);
                             } else if(key2.equals("xlink")) {
                                 model.put(METADATAVIEW_DATASET_XLINK, value);
                             }
                             
                         } else {
                                                       
                             value = (String)jsonobj.getString(key2);   
                             
                             if(key2.equals("title")) {
                                 model.put(METADATAVIEW_DATASET_TITLE, value);
                             } 
                         }
                         
                         keys.add(key2);
                         values.add(value);
                         
                     }
                     
 
                     String [] keyArr = new String[keys.size()];
                     String [] valueArr = new String[values.size()];
                     for(int i=0;i<keys.size();i++) {
                         keyArr[i] = keys.get(i);
                     }
                     for(int i=0;i<values.size();i++) {
                         valueArr[i] = values.get(i);
                     }
 
                     model.put(METADATAVIEW_KEYARR, keyArr);
                     model.put(METADATAVIEW_VALUEARR, valueArr);
                     
                 } catch (JSONException e) {
                     // TODO Auto-generated catch block
                     e.printStackTrace();
                 }
                
             }
             
 
         }
         
         
         
         return new ModelAndView("metadataview", model);
     
     }
     
     private String processJSONArray(JSONObject jsonobj,String key) {
         
 
         String valueString = "";
         
         try {
             JSONArray keyArr = jsonobj.getJSONArray(key);
             
             int length = keyArr.length();
             for(int i=0;i<length;i++) {
                 if(i == (length-1)) {
                     valueString += keyArr.getString(i);
                 } else {
                     valueString += keyArr.getString(i) + " ; ";
                 }
             }
         } catch(Exception e) {
             
         }
         
         
         
         return valueString;
     }
     
    
     
     private JSONObject getJSONResponse(String solrQueryString) {
         
         String responseBody = null;
         
         // create an http client
         HttpClient client = new HttpClient();
 
         //attact the dataset id to the query string
         GetMethod method = new GetMethod(searchAPIURL);
         

        System.out.println("this.solrQueryString prefix->\t" + searchAPIURL);
        System.out.println("this.solrQueryString->\t" + solrQueryString);
         
         method.setQueryString(solrQueryString);
         
         
         method.getParams().setParameter(HttpMethodParams.RETRY_HANDLER,
                 new DefaultHttpMethodRetryHandler(3, false));
         
         try {
             // execute the method
             int statusCode = client.executeMethod(method);
 
             if (statusCode != HttpStatus.SC_OK) {
                     //LOG.error("Method failed: " + method.getStatusLine());
 
             }
 
             // read the response
             responseBody = method.getResponseBodyAsString();
         } catch (HTTPException e) {
             //LOG.error("Fatal protocol violation");
             e.printStackTrace();
         } catch (IOException e) {
             //LOG.error("Fatal transport error");
             e.printStackTrace();
         } finally {
             method.releaseConnection();
         }
         
 
         String marker = "\"response\":";
         
         //just get the important part of the response (i.e. leave off the header and the facet info)
         int start = responseBody.lastIndexOf(marker) + marker.length();
         int end = responseBody.length();
         String responseString = responseBody.substring(start,end);
         
         
       //convert extracted string into json array
         JSONObject jsonResponse = null;
         JSONArray jsonArray = null;
         String numFound = null;
         try {
             
             jsonResponse = new JSONObject(responseString);
             
         } catch (JSONException e) {
             // TODO Auto-generated catch block
             System.out.println("Problem converting Solr response to json string - getDocElement");
             e.printStackTrace();
         }
         
         return jsonResponse;
     }
     
   
     
     
     
 }
 
 
 
 /*
 if(key2.equals("title")) {
     String title = (String)jsonobj.getString(key2);
 
     model.put(METADATAVIEW_DATASET_TITLE, title);
     
     value = title;
     
 }
 else if(key2.equals("number_of_files")) {
     JSONArray keyArr = jsonobj.getJSONArray(key2);
     
     int length = keyArr.length();
     String valueString = "";
     for(int i=0;i<length;i++) {
         if(i == (length-1)) {
             valueString += keyArr.getString(i);
         } else {
             valueString += keyArr.getString(i) + " ; ";
         }
     }
     
     //String numFiles = (String)jsonobj.getString(key2);
 
     model.put(METADATAVIEW_DATASET_NUMFILES, valueString);
     
     value = valueString;
     
 }
 else if(key2.equals("index_node")) {
     
    
     JSONArray keyArr = jsonobj.getJSONArray(key2);
     
     String indexNode = (String)jsonobj.getString(key2);
 
     int length = keyArr.length();
     String valueString = "";
     for(int i=0;i<length;i++) {
         if(i == (length-1)) {
             valueString += keyArr.getString(i);
         } else {
             valueString += keyArr.getString(i) + " ; ";
         }
     }
     
     model.put(METADATAVIEW_DATASET_INDEXNODE, valueString);
     
     value = valueString;
     
 }
 else if(key2.equals("xlink")) {
     JSONArray keyArr = jsonobj.getJSONArray(key2);
     
     String xlink = (String)jsonobj.getString(key2);
 
     int length = keyArr.length();
     String valueString = "";
     for(int i=0;i<length;i++) {
         if(i == (length-1)) {
             valueString += keyArr.getString(i);
         } else {
             valueString += keyArr.getString(i) + " ; ";
         }
     }
     
     model.put(METADATAVIEW_DATASET_INDEXNODE, valueString);
     
     value = valueString;
     
 }
 else 
 */
