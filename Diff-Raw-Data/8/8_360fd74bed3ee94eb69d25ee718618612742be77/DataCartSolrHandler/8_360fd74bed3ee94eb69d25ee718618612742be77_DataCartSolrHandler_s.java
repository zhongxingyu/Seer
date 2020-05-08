 package org.esgf.datacart;
 
 import java.io.IOException;
 import java.io.UnsupportedEncodingException;
 import java.net.URLEncoder;
 import java.util.ArrayList;
 import java.util.Enumeration;
 import java.util.List;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.xml.ws.http.HTTPException;
 
 import org.apache.commons.httpclient.DefaultHttpMethodRetryHandler;
 import org.apache.commons.httpclient.HttpClient;
 import org.apache.commons.httpclient.HttpStatus;
 import org.apache.commons.httpclient.methods.GetMethod;
 import org.apache.commons.httpclient.params.HttpMethodParams;
 import org.apache.log4j.Logger;
 import org.esgf.metadata.JSONArray;
 import org.esgf.metadata.JSONException;
 import org.esgf.metadata.JSONObject;
 
 
 @SuppressWarnings("unused")
 public class DataCartSolrHandler {
 
     private static String searchAPIURL = "http://localhost:8080/esg-search/search?";//
     private static String queryPrefix = "format=application%2Fsolr%2Bjson&type=File";
     private final static Logger LOG = Logger.getLogger(DataCartSolrHandler.class);
     
     private static int initialLimit = 10;
     private static int totalLimit = 10000;
     
     private String showAllStr;
     private String [] fq;
     private String initialQuery;
     //private String technoteStr;
     
     
     private String solrQueryString;
     
     public static String getSearchAPIURL() {
         return searchAPIURL;
     }
 
     public static void setSearchAPIURL(String searchAPIURL) {
         DataCartSolrHandler.searchAPIURL = searchAPIURL;
     }
 
     public static String getQueryPrefix() {
         return queryPrefix;
     }
 
     public static void setQueryPrefix(String queryPrefix) {
         DataCartSolrHandler.queryPrefix = queryPrefix;
     }
 
 
     public String getShowAllStr() {
         return showAllStr;
     }
 
     public void setShowAllStr(String showAllStr) {
         this.showAllStr = showAllStr;
     }
 
 
     public String getSolrQueryString() {
         return solrQueryString;
     }
 
     public void setSolrQueryString(String solrQueryString) {
         this.solrQueryString = solrQueryString;
     }
 
     public String[] getFq() {
         return fq;
     }
 
     public void setFq(String[] fq) {
         this.fq = fq;
     }
 
     
     public DataCartSolrHandler() {
         this.solrQueryString = null;
         this.preassembleQueryString();
     }
     
     public DataCartSolrHandler(String showAllStr,String [] fq,String initialQuery) {
         this.solrQueryString = null;
         this.preassembleQueryString();
         
         this.addLimit(initialQuery,"10");
         
         this.addSearchConstraints(fq, showAllStr);
         
     }
     
     public DataCartSolrHandler(String showAllStr,String [] fq,String initialQuery,String fileCounter) {
         this.solrQueryString = null;
         this.preassembleQueryString();
         
         this.addLimit(initialQuery,fileCounter);
         
         this.addSearchConstraints(fq, showAllStr);
         
     }
     
     public void preassembleQueryString() {
         
         this.solrQueryString = queryPrefix;
         
     }
     
     public void addSearchConstraints(String [] fq,String showAllStr) {
         
         if(fq != null) {
             this.fq = new String[fq.length];
             for(int i=0;i<fq.length;i++) {
                 this.fq[i] = fq[i];
                 //System.out.println("\tAdding constraint: " + fq[i]);
             }
         }
         
         this.showAllStr = showAllStr;
         
         if(this.fq != null && this.showAllStr != null) {
             
             //System.out.println("Show all string: " + this.showAllStr);
             
           //put any search criteria if the user selected "filter"
             if(this.showAllStr.equals("false")) {
             
                 String fullText = "";
                 if(this.fq != null) {
                     for(int i=0;i<this.fq.length;i++) {
                         
                         /*
                         String fqParam = null;
                         try {
                             fqParam = URLEncoder.encode(this.fq[i],"UTF-8").toString();
                         } catch (UnsupportedEncodingException e) {
                             // TODO Auto-generated catch block
                             e.printStackTrace();
                         }
                         */
                         String fqParam = this.fq[i];
                         /*
                          * Should ignore the following:
                          * - blanks - ""
                          * - replica - "replica=..."
                          * - offset - "offset=..."
                          */
                         boolean ignore = fqParam.equals("") || 
                                          fqParam.equals(" ") ||
                                          fqParam.contains("offset=") ||
                                          fqParam.contains("replica=");
 
                         
                         
                         
                         //otherwise add to the query
                         if(!ignore) {
                         
                             String valueTerm = fqParam.split("=")[1];
                             String valueTermEnc = null;
                             try {
                                 valueTermEnc = URLEncoder.encode(valueTerm,"UTF-8").toString();
                             } catch (UnsupportedEncodingException e) {
                                 // TODO Auto-generated catch block
                                 e.printStackTrace();
                             }
                             
                             fqParam = fqParam.split("=")[0] + "=" + valueTermEnc;
 
                             if(!fqParam.contains("query")) {
                                 this.solrQueryString += "&" + fqParam;
                             } 
                             //full text queries have to be handled differently
                             else {
                                 String [] clause = fqParam.split("=");
                                 fullText += clause[1] + "%20";
                             }
                         }
 
                     }//end for
                     if(!fullText.equals("") && !fullText.equals(" ")) {
                         this.solrQueryString += "&query=" + fullText;
                     }
 
                 }
                 
             }
             
         }
 
         
     }
     
     
     public void addLimit(String initialQuery,String fileCounter) {
         this.initialQuery = initialQuery;
         
         initialLimit = Integer.parseInt(fileCounter);
         
       //if this is the initialQuery, only serve the number of files declared in "initialLimit"
         if(this.initialQuery.equals("true")) {
             this.solrQueryString += "&limit="+initialLimit;
         } else {
             this.solrQueryString += "&offset=" + initialLimit + "&limit="+totalLimit;
         }
     }
     
     public void addShard(String shard) {
         
         //System.out.println("Shard: " + shard);
         /*
         if(shard.equals("esg-datanode.jpl.nasa.gov:8983/solr")) {
             shard = "localhost:18983/solr";
         } 
         
         if (shard.equals("pcmdi9.llnl.gov:8983/solr")) {
             shard = "localhost:28983/solr";
         }
         */
         if(!shard.equals("undefined")) {
             this.solrQueryString += "&shards=" + shard;
         } 
         
         
         
     }
    
     
     public DocElement getDocElement(String datasetId) {
         
         
         
         DocElement docElement = new DocElement();
         
         //set the dataset id
         docElement.setDatasetId(datasetId);
         
         //get the file elements and set it
 
         //query the index here
         //JSONArray responseFiles = queryIndex(datasetId);
         String rawResponse = queryIndex(datasetId);
 
         //System.out.println("-----RAW RESPONSE-----");
         //System.out.println(rawResponse);
         //System.out.println("-----END RAW RESPONSE-----");
        
         //convert extracted string into json array
         JSONObject jsonResponse = null;
         JSONArray jsonArray = null;
         String numFound = null;
         try {
             
             jsonResponse = new JSONObject(rawResponse);
             
             jsonArray = jsonResponse.getJSONArray("docs");
             numFound = jsonResponse.getString("numFound");
         } catch (JSONException e) {
             // TODO Auto-generated catch block
             System.out.println("Problem converting Solr responsew to json string - getDocElement");
             //e.printStackTrace();
         }
         
         //create a count element
         if(numFound == null) {
             docElement.setCount(-1);
         } else {
             docElement.setCount(Integer.parseInt(numFound));
         }
        
         
         
         
         JSONArray responseFiles = this.solrResponseToJSON(rawResponse);
         
         List<FileElement> fileElements = getFileElements(datasetId,responseFiles);
       
         
         //set the file elements
         docElement.setFileElements(fileElements);
         
       //set the booleans
         docElement.setHasGridFTP("false");
         docElement.setHasHttp("false");
         docElement.setHasOpenDap("false");
         docElement.setHasSRM("false");
         for(int i=0;i<docElement.getFileElements().size();i++) {
             FileElement fe = docElement.getFileElements().get(i);
             if(fe.getHasGrid().equals("true")) {
                 docElement.setHasGridFTP("true");
             }
             if(fe.getHasHttp().equals("true")) {
                 docElement.setHasHttp("true");
             }
             if(fe.getHasOpenDap().equals("true")) {
                 docElement.setHasOpenDap("true");
             }
             if(fe.getHasSRM().equals("true")) {
                 docElement.setHasSRM("true");
             }
         }
 
         
         return docElement;
     }
     
     private List<FileElement> getFileElements(String dataset_id,JSONArray responseFiles) {
      
         List<FileElement> fileElements = new ArrayList<FileElement>();
         
         
         for(int i=0;i<responseFiles.length();i++) {
             
             JSONObject file = null;
             try {
                 file = responseFiles.getJSONObject(i);
             } catch (JSONException e) {
                 // TODO Auto-generated catch block
                 e.printStackTrace();
             }
 
             FileElement fileElement = new FileElement();
             fileElement.fromSolr(file);
             
             
             fileElements.add(fileElement);
             
         }    
         
         
         return fileElements;
     }
     
     
     public String queryIndex(String dataset_id) {
         
         String rawResponse = this.querySolrForFiles(dataset_id);
         
         
         
         return rawResponse;
         
     }
     
     private String querySolrForFiles(String dataset_id) {
      
         String marker = "\"response\":";
         
         String responseBody = null;
         
         // create an http client
         HttpClient client = new HttpClient();
 
         //attact the dataset id to the query string
         GetMethod method = new GetMethod(searchAPIURL);
         
         
         //add the dataset to the query string
         try {
             this.solrQueryString += "&dataset_id=" + URLEncoder.encode(dataset_id,"UTF-8").toString();
             //System.out.println("\nthis.solrQueryString->\t" + URLEncoder.encode(dataset_id,"UTF-8").toString());
            //System.out.println("\n\tthis.solrQueryString->\t" + this.solrQueryString);
             
         } catch (UnsupportedEncodingException e1) {
             // TODO Auto-generated catch block
             e1.printStackTrace();
         }
         
         
         method.setQueryString(this.solrQueryString);
         
         method.getParams().setParameter(HttpMethodParams.RETRY_HANDLER,
                 new DefaultHttpMethodRetryHandler(3, false));
         
         
         
         try {
             // execute the method
             int statusCode = client.executeMethod(method);
 
             if (statusCode != HttpStatus.SC_OK) {
                     LOG.error("Method failed: " + method.getStatusLine());
 
             }
 
             // read the response
             responseBody = method.getResponseBodyAsString();
         } catch (HTTPException e) {
             LOG.error("Fatal protocol violation");
             e.printStackTrace();
         } catch (IOException e) {
             LOG.error("Fatal transport error");
             e.printStackTrace();
         } finally {
             method.releaseConnection();
         }
 
 
         //just get the important part of the response (i.e. leave off the header and the facet info)
         int start = responseBody.lastIndexOf(marker) + marker.length();
         int end = responseBody.length();
         String responseString = responseBody.substring(start,end);
         
         //System.out.println("\nRESPONSE STR\n\n" + responseString + "\n\n\n");
 
         return responseString;
         
     }
     
     /**
      * 
      * @param rawString
      * @return
      */
     private JSONArray solrResponseToJSON(String rawString) {
         
         //convert extracted string into json array
         JSONObject jsonResponse = null;
         JSONArray jsonArray = null;
         try {
             jsonResponse = new JSONObject(rawString);
             jsonArray = jsonResponse.getJSONArray("docs");
         } catch (JSONException e) {
             // TODO Auto-generated catch block
             System.out.println("Problem converting Solr response to json string - solrResponseToJSON");
             e.printStackTrace();
         }
         
         return jsonArray;
     }
     
    
     
     public static void main(String [] args) {
 
         String showAllStr = "false";
         String peerStr = "esg-datanode.jpl.nasa.gov";
         
         //String [] id = {"obs4MIPs.NASA-JPL.AIRS.mon.v1|esg-datanode.jpl.nasa.gov"};
        
         
         String [] id = {
                         "cmip5.output1.NCC.NorESM1-M.historicalExt.6hr.atmos.6hrLev.r2i1p1.v20111102|norstore-trd-bio1.hpc.ntnu.no",
                         "obs4MIPs.NASA-JPL.AIRS.mon.v1|esg-datanode.jpl.nasa.gov"
                         };
         //this is the string that I will get
         String fqStr = ",offset=0,replica=false";
         //need to convert to fq []
         
         String [] fq = fqStr.split(",");
 
         String initialQuery = "true";
         
         DataCartSolrHandler handler = new DataCartSolrHandler(showAllStr,fq,initialQuery);
 
         
         DataCartDocs doc = new DataCartDocs();
         
         for(int i=0;i<id.length;i++) {
             handler.preassembleQueryString();
             DocElement d = handler.getDocElement(id[i]);
             doc.addDocElement(d);
             if(i == 1)
             System.out.println(new XmlFormatter().format(d.toXML()));
         }
         
         
     }
     
     
     
     
     
     
     
     
     
     
     
     
 }
 
 
