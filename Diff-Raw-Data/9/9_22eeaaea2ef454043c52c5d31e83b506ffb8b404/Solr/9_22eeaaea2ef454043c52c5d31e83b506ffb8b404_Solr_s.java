 package org.esgf.solr.model;
 
 import java.io.IOException;
 import java.io.UnsupportedEncodingException;
 import java.net.URLEncoder;
 import java.util.HashMap;
 import java.util.Map;
 
 import javax.xml.ws.http.HTTPException;
 
 import org.apache.commons.httpclient.DefaultHttpMethodRetryHandler;
 import org.apache.commons.httpclient.HttpClient;
 import org.apache.commons.httpclient.HttpStatus;
 import org.apache.commons.httpclient.methods.GetMethod;
 import org.apache.commons.httpclient.methods.PostMethod;
 import org.apache.commons.httpclient.params.HttpMethodParams;
 import org.esgf.srm.SRMUtils;
 
 public class Solr {
     
    public static String searchAPI = "http://localhost/esg-search/search?";
     private static String queryPrefix = "format=application%2Fsolr%2Bjson&type=File";
     
     
     private Map<String,String> constraints;
     private String queryString;
     private SolrResponse solrResponse;
     
     public Solr() {
         this.setQueryString(null);
         this.setSolrResponse(null);
         this.constraints = null;
     }
 
     public static void main(String [] args) {
         Solr solr = new Solr();
         
         solr.addConstraint("query", "*");
         solr.addConstraint("distrib", "false");
         solr.addConstraint("limit", "12");
         
         solr.executeQuery();
         
     }
     
     public void removeConstraint(String field) {
         this.queryString = null;
         
         Object removeKey = null;
         
         for(Object key : this.constraints.keySet()) {
             String keyStr = (String) key;
             String value = this.constraints.get(key);
             if(keyStr.equals(field)) {
                 //this.constraints.remove(key);
                 removeKey = key;
             } else {
                 if(this.queryString == null) {
                     this.queryString = keyStr + "=" + value;
                 } else {
                     this.queryString += "&" + keyStr + "=" + value;
                 }
             }
             
         }
         
         this.constraints.remove(removeKey);
     }
     
     public void addConstraint(String field,String value) {
         
         try {
             value = URLEncoder.encode(value,"UTF-8").toString();
         } catch (UnsupportedEncodingException e) {
             // TODO Auto-generated catch block
             e.printStackTrace();
         }
         
         if(this.constraints == null) {
             this.constraints = new HashMap<String,String>();
         }
         this.constraints.put(field, value);
         if(this.queryString == null) {
             this.queryString = field + "=" + value;
         } else {
             this.queryString += "&" + field + "=" + value;
         }
     }
     
     public void executeQuery() {
         //System.out.println("Executing query");
         
         // create an http client
         HttpClient client = new HttpClient();
 
         //attact the dataset id to the query string
       //attact the dataset id to the query string
         GetMethod method = new GetMethod(searchAPI);
         
         
         method.setQueryString(this.queryString);
         
         method.getParams().setParameter(HttpMethodParams.RETRY_HANDLER,
                 new DefaultHttpMethodRetryHandler(3, false));
         
         String responseBody = null;
         try {
             // execute the method
             int statusCode = client.executeMethod(method);
 
             //System.out.println("statusCode: " + statusCode);
             
             if (statusCode != HttpStatus.SC_OK) {
                     System.out.println("Method failed: " + method.getStatusLine());
 
             }
 
             // read the response
             responseBody = method.getResponseBodyAsString();
         } catch (HTTPException e) {
             System.out.println("Fatal protocol violation");
             e.printStackTrace();
         } catch (IOException e) {
             System.out.println("Fatal transport error");
             e.printStackTrace();
         } finally {
             method.releaseConnection();
         }
         
         
         this.solrResponse = new SolrResponse(responseBody);
     }
     
     
     /**
      * @return the queryString
      */
     public String getQueryString() {
         return queryString;
     }
 
     /**
      * @param queryString the queryString to set
      */
     public void setQueryString(String queryString) {
         this.queryString = queryString;
     }
 
     /**
      * @return the solrResponse
      */
     public SolrResponse getSolrResponse() {
         return solrResponse;
     }
 
     /**
      * @param solrResponse the solrResponse to set
      */
     public void setSolrResponse(SolrResponse solrResponse) {
         this.solrResponse = solrResponse;
     }
     
     
     
     
 }
