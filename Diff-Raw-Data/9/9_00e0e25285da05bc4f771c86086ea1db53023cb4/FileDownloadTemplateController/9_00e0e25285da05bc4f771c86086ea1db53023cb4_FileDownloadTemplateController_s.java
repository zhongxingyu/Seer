 
 /*****************************************************************************
  * Copyright � 2011 , UT-Battelle, LLC All rights reserved
  *
  * OPEN SOURCE LICENSE
  *
  * Subject to the conditions of this License, UT-Battelle, LLC (the
  * �Licensor�) hereby grants to any person (the �Licensee�) obtaining a copy
  * of this software and associated documentation files (the "Software"), a
  * perpetual, worldwide, non-exclusive, irrevocable copyright license to use,
  * copy, modify, merge, publish, distribute, and/or sublicense copies of the
  * Software.
  *
  * 1. Redistributions of Software must retain the above open source license
  * grant, copyright and license notices, this list of conditions, and the
  * disclaimer listed below.  Changes or modifications to, or derivative works
  * of the Software must be noted with comments and the contributor and
  * organization�s name.  If the Software is protected by a proprietary
  * trademark owned by Licensor or the Department of Energy, then derivative
  * works of the Software may not be distributed using the trademark without
  * the prior written approval of the trademark owner.
  *
  * 2. Neither the names of Licensor nor the Department of Energy may be used
  * to endorse or promote products derived from this Software without their
  * specific prior written permission.
  *
  * 3. The Software, with or without modification, must include the following
  * acknowledgment:
  *
  *    "This product includes software produced by UT-Battelle, LLC under
  *    Contract No. DE-AC05-00OR22725 with the Department of Energy.�
  *
  * 4. Licensee is authorized to commercialize its derivative works of the
  * Software.  All derivative works of the Software must include paragraphs 1,
  * 2, and 3 above, and the DISCLAIMER below.
  *
  *
  * DISCLAIMER
  *
  * UT-Battelle, LLC AND THE GOVERNMENT MAKE NO REPRESENTATIONS AND DISCLAIM
  * ALL WARRANTIES, BOTH EXPRESSED AND IMPLIED.  THERE ARE NO EXPRESS OR
  * IMPLIED WARRANTIES OF MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE,
  * OR THAT THE USE OF THE SOFTWARE WILL NOT INFRINGE ANY PATENT, COPYRIGHT,
  * TRADEMARK, OR OTHER PROPRIETARY RIGHTS, OR THAT THE SOFTWARE WILL
  * ACCOMPLISH THE INTENDED RESULTS OR THAT THE SOFTWARE OR ITS USE WILL NOT
  * RESULT IN INJURY OR DAMAGE.  The user assumes responsibility for all
  * liabilities, penalties, fines, claims, causes of action, and costs and
  * expenses, caused by, resulting from or arising out of, in whole or in part
  * the use, storage or disposal of the SOFTWARE.
  *
  *
  ******************************************************************************/
 
 /**
  *
  * @author John Harney (harneyjf@ornl.gov), Feiyi Wang (fwang2@ornl.gov)
  *
  * Changelog:
  *
  * The query string is encoded through encoder instead of manual string
  * The converted template will be:
  *
  * For debug only:
  *
  * The converted template:
  *
  * <response>
  *    <doc>
  *       <dataset_id> whatever </dataset_id>
  *       <file>
  *          <file_id> ... </file_id>
  *          <size> ... </size>
  *          ...
  *       </file>
  *
  *       <file> .... </file>
  *   </doc>
  * </response>
  */
 
 
 
 package org.esgf.filedownload;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Enumeration;
 import java.util.List;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.xml.ws.http.HTTPException;
 
 import org.apache.commons.httpclient.DefaultHttpMethodRetryHandler;
 import org.apache.commons.httpclient.HttpClient;
 import org.apache.commons.httpclient.HttpStatus;
 import org.apache.commons.httpclient.URIException;
 import org.apache.commons.httpclient.methods.GetMethod;
 import org.apache.commons.httpclient.params.HttpMethodParams;
 import org.apache.commons.httpclient.util.URIUtil;
 import org.apache.log4j.Logger;
 import org.esgf.metadata.JSONArray;
 import org.esgf.metadata.JSONException;
 import org.esgf.metadata.JSONObject;
 import org.esgf.metadata.XML;
 import org.jdom.Document;
 import org.jdom.Element;
 import org.jdom.input.SAXBuilder;
 import org.jdom.output.XMLOutputter;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.ResponseBody;
 
 @Controller
 @RequestMapping("/solrfileproxy")
 public class FileDownloadTemplateController {
 
     private static String solrURL="http://localhost:8983/solr/select";
     
     private final static Logger LOG = Logger.getLogger(FileDownloadTemplateController.class);
 
     //toggle between full datasets and filtered datasets
     private final static boolean datasetFilterFlag = true;
    
 
     @RequestMapping(method=RequestMethod.GET)
     public @ResponseBody String doGet(HttpServletRequest request, HttpServletResponse response) throws JSONException {
         System.out.println("doGet");
         
         /*
         String responseStr = convertTemplateFormat(request, response);
         return responseStr;
         */
         
         return getDataCart(request);
     }
     
     
     private String getDataCart(HttpServletRequest request) {
         System.out.println("getDataCart");
         
         String queryString = preassembleQueryString(request);
         System.out.println("QueryString: " + queryString);
         
         //get the ids from the servlet querystring here
         //note: these represent the 'keys' in the localStorage['dataCart'] map
         String [] names = request.getParameterValues("id[]");
         
         String showAll = request.getParameter("showAll");
         
         System.out.println("ShowAll: " + showAll);
         
         
         Document document = null;
         
         String jsonContent = null;
 
         document = new Document(new Element("response"));
         
         if(names != null) {
             List<DocElement> docElements = new ArrayList<DocElement>();
             
             for(int i=0;i<names.length;i++) {
                 System.out.println("\tid: " + i + " " + names[i]);
                 String dataset_id = names[i];
                 
                 //get files for each data set in a jsonarray
                 JSONArray jsonArrayResponseDocs = getJSONArrayForDatasetid(queryString,dataset_id);
                 
                 try {
                     
                     //create doc element
                     DocElement docElement = new DocElement();
                     
                     //add the dataset id
                     docElement.setDataset_id(dataset_id);
                     
                     List<FileElement> fileElements = new ArrayList<FileElement>();
                     
                     //insert initial file here
                     FileElement initialFileElement = createInitialFileElement1();
                     
                     //add all other file elements
                     for(int j=0;j<jsonArrayResponseDocs.length();j++) {
                         JSONObject docJSON = new JSONObject(jsonArrayResponseDocs.get(j).toString());
                         FileElement fileElement = createFileElement1(docJSON);
                         fileElements.add(fileElement);
                     }
                     
                     docElement.setCount(jsonArrayResponseDocs.length());
                     
                     docElement.setFileElements(fileElements);
                     
                     docElements.add(docElement);
                     
                     
                     
                     
                     
                     //System.out.println("\nJSONCONTENT\n" + jsonContent);
                     
                     
                     
                 } catch(Exception e) {
                     System.out.println("JSON ERROR");
                     e.printStackTrace();
                 }
             }
             
             
             String xmlStr = "";
             for(int i=0;i<docElements.size();i++) {
                 xmlStr += docElements.get(i).toXML();
             }
             
             JSONObject returnJSON = null;
             try {
                 returnJSON = XML.toJSONObject(xmlStr);
             } catch (JSONException e) {
                 // TODO Auto-generated catch block
                 e.printStackTrace();
             }
             
             
             jsonContent = returnJSON.toString();
         
         }
         
         System.out.println("end getDataCart");
         
         
         return jsonContent;
     }
     
     
     private static FileElement createInitialFileElement1() {
         
         FileElement fileElement = new FileElement();
         
         //put "dummy" values in the file element
         fileElement.setFileId("fileId");
         fileElement.setHasGrid("hasGrid");
         fileElement.setHasHttp("hasHttp");
         fileElement.setSize("size");
         fileElement.setTitle("title");
         fileElement.setMimesElement(new MIMESElement());
         fileElement.setServicesElement(new ServicesElement());
         fileElement.setUrlsElement(new URLSElement());
         
         
         return fileElement;
     }
     
     public FileElement createFileElement1(JSONObject docJSON) throws JSONException {
         
         FileElement fileElement = new FileElement();
         
         String fileId = docJSON.get("id").toString();
         fileElement.setFileId(fileId);
         
         String title = docJSON.get("title").toString();
         fileElement.setTitle(title);
         
         String size = docJSON.get("size").toString();
         fileElement.setSize(size);
 
         String hasGrid = "false";
         fileElement.setHasGrid(hasGrid);
         
         String hasHttp = "false";
         fileElement.setHasGrid(hasHttp);
         
         
         URLSElement urlsElement = new URLSElement();
         MIMESElement mimesElement = new MIMESElement();
         ServicesElement servicesElement = new ServicesElement();
         
         
         JSONArray urlsJSON = (JSONArray)docJSON.getJSONArray("url");
         for(int i=0;i<urlsJSON.length();i++) {
             String urlStr = urlsJSON.get(i).toString();
             
             String [] urlStrTokens = urlStr.split("\\|");
             
             String url = urlStrTokens[0];
             urlsElement.addURL(url);
             
             String mime = urlStrTokens[1];
             mimesElement.addMIME(mime);
             
             String service = urlStrTokens[2];
             servicesElement.addService(service);
             
         }
         
         fileElement.setUrlsElement(urlsElement);
         fileElement.setMimesElement(mimesElement);
         fileElement.setServicesElement(servicesElement);
         
         return fileElement;
     }
     
     
     
     
     
     /**
      * creates json array of file entries for a given dataset
      */
     JSONArray getJSONArrayForDatasetid(String queryString,String dataset_id) {
         String marker = "\"response\":";
         
         //get the json response for all files associated with dataset_id 
         String responseRawString = getResponseBody(queryString,dataset_id);
         
         //just get the important part of the response (i.e. leave off the header and the facet info)
         int start = responseRawString.lastIndexOf(marker) + marker.length();
         int end = responseRawString.length();
         String extractedString = responseRawString.substring(start,end);
         
         
         JSONObject jsonResponse = null;
         JSONArray jsonArrayResponseDocs = null;
         
         try {
             jsonResponse = new JSONObject(extractedString);
             jsonArrayResponseDocs = jsonResponse.getJSONArray("docs");
         } catch (JSONException e) {
             // TODO Auto-generated catch block
             e.printStackTrace();
         }
         
         return jsonArrayResponseDocs;
     }
     
     /**
      * creates json array of file entries for a given dataset
      */
     JSONArray getJSONArrayForDatasetID(String queryString,String id) {
         String marker = "\"response\":";
         String responseRawString = getResponseBody(queryString,id);
         int start = responseRawString.lastIndexOf(marker) + marker.length();
         int end = responseRawString.length();
         String extractedString = responseRawString.substring(start,end);
         JSONObject responseBody = null;
         JSONArray docsJSON = null;
         try {
             responseBody = new JSONObject(extractedString);
             docsJSON = responseBody.getJSONArray("docs");
         } catch (JSONException e) {
             // TODO Auto-generated catch block
             e.printStackTrace();
         }
         
         return docsJSON;
     }
     
     
     
     /*
      * Conversion
      */
 
     //responsebody looks like this
     //  <responseHeader>
     //  </responseHeader>
     //  <response>
     //   <doc>
     //    <id>
     //    <parent_id>
     //    <service>
     //    <title>
     //    <size>
     //   </doc>
     //  </response>
 
     //----->
 
     //convert response body to the following format
     //<doc>
     //    <file*>
     //      <file_id></file_id>
     //      <file_size></file_size>
     //      <file_url></file_url>
     //      <services>
     //        <service></service>
     //        <service></service>
     //      </services>
     //    </file>
     //<doc>
     private String convertTemplateFormat(HttpServletRequest request, HttpServletResponse response) throws JSONException {
         
         String queryString = preassembleQueryString(request);
         
         //get the ids from the servlet querystring here
         //note: these represent the 'keys' in the localStorage['dataCart'] map
         String [] names = request.getParameterValues("id[]");
        
         String id = "";
         String xmlOutput = "";
         
         SAXBuilder builder = new SAXBuilder();
         Document document = null;
         
 
         //getResponseInJSON(queryString);
         String jsonContent = null;
 
         
         
         document = new Document(new Element("response"));
         if(names != null) {
             //traverse all the dataset ids given by the array
             for(int i=0;i<names.length;i++) {
                 id = names[i];
                 
                 //get files for each data set in a jsonarray
                 JSONArray docsJSON = getJSONArrayForDatasetID(queryString,id);
                 
                 try {
                     //create <doc> element
                     Element docEl = new Element("doc");
                     //create doc/dataset_id element
                     Element dataset_idEl = new Element("dataset_id");
                     dataset_idEl.addContent(id);
                     docEl.addContent(dataset_idEl);
                     
                     //insert initial file here
                     //this is to combat the array vs json object problem
                     JSONObject docJSON = new JSONObject(docsJSON.get(0).toString());
                     Element fileEl = createInitialFileElement(docJSON);
                     docEl.addContent(fileEl);
                     
                     
                     //insert all other file elements here
                     for(int j=0;j<docsJSON.length();j++) {
                         docJSON = new JSONObject(docsJSON.get(j).toString());
                         fileEl = createFileElement(docJSON);
                         docEl.addContent(fileEl);
                     }
                     
                     //add the content to the document root
                     document.getRootElement().addContent(docEl);
                     
                     //convert the document to json
                     jsonContent = convertXMLDocToJSON(document);
                     
                 } catch(Exception e) {
                     LOG.debug("\nJSON Error in converting template format \n");
                 }
             }
         }
         
 
         return jsonContent;
         
     }
     
     
     
 
     String convertXMLDocToJSON(Document document) {
         String jsonContent = "";
         XMLOutputter outputter = new XMLOutputter();
         String xmlOutput = outputter.outputString(document.getRootElement());
         JSONObject returnJSON;
         try {
             returnJSON = XML.toJSONObject(xmlOutput);
             jsonContent = returnJSON.toString();
         } catch (JSONException e) {
             // TODO Auto-generated catch block
             e.printStackTrace();
         }
         
         return jsonContent;
     }
     
     
     private static Element createInitialFileElement(JSONObject docJSON) {
         Element fileEl = new Element("file");
 
         try {
          // create <file> element
            
            // create file/file_id element
            Element file_idEl = new Element("file_id");
            file_idEl.addContent("nully");
            fileEl.addContent(file_idEl);
 
            // create file/title element
            Element titleEl = new Element("title");
            titleEl.addContent("nully");
            fileEl.addContent(titleEl);
 
            // create file/file_size element
            Element sizeEl = new Element("size");
            sizeEl.addContent(docJSON.get("size").toString());
            fileEl.addContent(sizeEl);
 
            // create file/url element
            Element urlEl = new Element("url");
            JSONArray urlsJSON = docJSON.getJSONArray("url");
 
            urlEl.addContent("nully");
            fileEl.addContent(urlEl);
 
 
            // create file/services element
            Element servicesEl = new Element("services");
 
            JSONArray docserviceJSON = docJSON.getJSONArray("service");
            for(int k=0;k<docserviceJSON.length();k++) {
                Element serviceEl = new Element("service");
                String serviceStr = docserviceJSON.get(k).toString();
                String [] serviceTokens = serviceStr.split("|");
                //serviceEl.addContent(docsJSON.get(i).toString());
                serviceEl.addContent(serviceTokens[2]);
                //LOG.debug("service: " + serviceTokens[2]);
                servicesEl.addContent(serviceEl);
            }
            fileEl.addContent(servicesEl);
        
         } catch(Exception e) {
             
         }
         
         return fileEl;
     }
     
     
     
    
 
     
     
     
     
     public Element createFileElement(JSONObject docJSON) throws JSONException {
         // create <file> element
         Element fileEl = new Element("file");
 
         // create file/file_id element
         Element file_idEl = new Element("file_id");
         file_idEl.addContent(docJSON.get("id").toString());
         fileEl.addContent(file_idEl);
 
         // create file/title element
         Element titleEl = new Element("title");
         titleEl.addContent(docJSON.get("title").toString());
         fileEl.addContent(titleEl);
 
         // create file/file_size element
         Element sizeEl = new Element("size");
         sizeEl.addContent(docJSON.get("size").toString());
         fileEl.addContent(sizeEl);
 
         // create file/url element
         Element urlEl = new Element("url");
         JSONArray urlsJSON = docJSON.getJSONArray("url");
 
         urlEl.addContent(urlsJSON.get(0).toString());
         fileEl.addContent(urlEl);
 
 
         // create file/services element
         Element servicesEl = new Element("services");
 
         JSONArray docsJSON = docJSON.getJSONArray("service");
         for(int i=0;i<docsJSON.length();i++) {
             Element serviceEl = new Element("service");
             String serviceStr = docsJSON.get(i).toString();
             String [] serviceTokens = serviceStr.split("|");
             //serviceEl.addContent(docsJSON.get(i).toString());
             serviceEl.addContent(serviceTokens[2]);
             //LOG.debug("service: " + serviceTokens[2]);
             servicesEl.addContent(serviceEl);
         }
         fileEl.addContent(servicesEl);
 
         return fileEl;
     }
     
     /*
      * For version v2, this method converts xml to json ready format for the front end to use
      */
 
     //responsebody looks like this
     //  <responseHeader>
     //  </responseHeader>
     //  <response>
     //   <doc>
     //    <id>
     //    <parent_id>
     //    <service>
     //    <title>
     //    <size>
     //   </doc>
     //  </response>
 
     //----->
 
     //convert response body to the following format
     //<doc>
     //    <file*>
     //      <file_id></file_id>
     //      <file_size></file_size>
     //      <file_url></file_url>
     //      <services>
     //        <service></service>
     //        <service></service>
     //      </services>
     //    </file>
     //<doc>
 
     private String convertTemplateFormatV2(HttpServletRequest request, HttpServletResponse response) throws JSONException {
         String queryString = "q=*:*&json.nl=map&start=0&rows=300&fq=type:File";
 
         //add the distrib attribute to the query
         //this will temporarily fix the error associated with viewing files with distributed search
         queryString = "qt=/distrib&" + queryString;
         
         String shards = request.getParameter("shards");
         
 
         LOG.debug("\n\n\n\n\n\n");
         
         LOG.debug("Shards: " + shards);
         
         LOG.debug("\n\n\n\n\n\n");
         
         String xmlOutput = "";
         SAXBuilder builder = new SAXBuilder();
         Document document = null;
 
         String jsonContent = null;
         
         String dataset_id = request.getParameter("id");
         
         String marker = "\"response\":";
         //String responseRawString = getResponseBody(dataset_id);
 
         document = new Document(new Element("response"));
         
         String responseBody = null;
 
         HttpClient client = new HttpClient();
 
         String combinedQueryStr = queryString + dataset_id + "&wt=json";
 
         
         GetMethod method = new GetMethod(solrURL);
         try {
             method.setQueryString(URIUtil.encodeQuery(combinedQueryStr));
         } catch (URIException e1) {
             // TODO Auto-generated catch block
             e1.printStackTrace();
         }
         
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
             //LOG.debug(responseBody);
         } catch (HTTPException e) {
             LOG.error("Fatal protocol violation");
             e.printStackTrace();
         } catch (IOException e) {
             LOG.error("Fatal transport error");
             e.printStackTrace();
         } finally {
             method.releaseConnection();
         }
         
         //assemble the json string here
         int start = responseBody.lastIndexOf(marker) + marker.length();
         int end = responseBody.length();
         String extractedString = responseBody.substring(start, end);
         JSONObject jsonResponse = new JSONObject(extractedString);
         JSONArray docsJSON = jsonResponse.getJSONArray("docs");
         
         try{
             
             JSONObject docJSON = new JSONObject(docsJSON.get(0).toString());
             
             //  create <doc> element
             Element docEl = new Element("doc");
 
             //  create doc/dataset_id element
             Element dataset_idEl = new Element("dataset_id");
             dataset_idEl.addContent(dataset_id);
             docEl.addContent(dataset_idEl);
             
             //insert initial file here
             //this is to combat the array vs. json object problem
             Element fileEl = createInitialFileElement(docJSON);
             docEl.addContent(fileEl);
             
 
             //  for each file found
             for(int j=0;j<docsJSON.length();j++) {
                 docJSON = new JSONObject(docsJSON.get(j).toString());
                 fileEl = createFileElement(docJSON);
                 docEl.addContent(fileEl);
             }
             
             document.getRootElement().addContent(docEl);
             
             XMLOutputter outputter = new XMLOutputter();
             xmlOutput = outputter.outputString(document.getRootElement());
 
             JSONObject returnJSON = XML.toJSONObject(xmlOutput);
 
             jsonContent = returnJSON.toString();
             
         }catch(Exception e) {
             System.out.println("\n\nerror in retrieve files\n\n");
         }
         
         return jsonContent;
     }
     
 
     /** getResponseBody(String id)
      * This method extracts all file records for a given dataset id and assembles them in json format
      * 
      * @param id        Dataset Id
      * @return          Solr response for all files given the dataset id
      */
     private static String getResponseBody(String queryString,String dataset_id)  {
 
         String responseBody = null;
 
         // create an http client
         HttpClient client = new HttpClient();
 
         //attact the dataset id to the query string
         String combinedQueryStr = queryString + "&fq=dataset_id:" + dataset_id + "&wt=json";
         
         GetMethod method = new GetMethod(solrURL);
 
         try {
             method.setQueryString(URIUtil.encodeQuery(combinedQueryStr));
         } catch (URIException e1) {
             // TODO Auto-generated catch block
             e1.printStackTrace();
         }
         
         
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
             //LOG.debug(responseBody);
         } catch (HTTPException e) {
             LOG.error("Fatal protocol violation");
             e.printStackTrace();
         } catch (IOException e) {
             LOG.error("Fatal transport error");
             e.printStackTrace();
         } finally {
             method.releaseConnection();
         }
 
         //System.out.println("Response Body: " + responseBody);
         
         
         return responseBody;
     }
     
     
     
     private String preassembleQueryString(HttpServletRequest request) {
         String queryString = "q=*:*&json.nl=map&start=0&rows=300&fq=type:File";
 
         queryString = "qt=/distrib&" + queryString;
         
         System.out.println("ShowAll: " + request.getParameter("showAll"));
         
         if(request.getParameter("showAll").equals("false")) {
           //get the 'fq' params from the servlet query string here
             String [] fqParams = request.getParameterValues("fq[]");
             
             //get the 'q' param from the servlet query string here
             String qParam = request.getParameter("q");
             
             if(fqParams != null) {
               //append the 'fq' params to the query string
                 for(int i=0;i<fqParams.length;i++) {
                     String fqParam = fqParams[i];
                     queryString += "&fq=" + fqParam;
                 }
             }
             
         }
         System.out.println("queryString: " + queryString);
         
         
         return queryString;
     }
     
 
     private void printShards(String [] shards) {
         
         if(shards != null) {
             System.out.println("shards length");
             for(int i=0;i<shards.length;i++) {
                 System.out.println("Shard: " + i + " " + shards[i]);
             }
         } else {
             System.out.println("null shards");
         }
         
         
     }
 
     private static void appendShardsToQueryString(String [] shards) {
         /*
         String shardsString = "shards=";
         for(int i=0;i<shards.length;i++) {
             shardsString += shards[i] + ":8983/solr";
             if(i < shards.length-1) {
                 shardsString += ',';
             }
             else {
                 shardsString += "&";
             }
         }
         queryString = shardsString + queryString;
         System.out.println("Query string: " + queryString);
         */
     }
 
 }
 
 
 
 /* from  convertTemplateFormat
 String [] shards = request.getParameterValues("shards[]");
 
 //attach active shards to the query string to be sent to solr
 appendShardsToQueryString(shards);
 
 String[] names = request.getParameterValues("id[]");
 
 String id = "";
 String xmlOutput = "";
 
 SAXBuilder builder = new SAXBuilder();
 Document document = null;
 
 String jsonContent = null;
 
 document = new Document(new Element("response"));
 if(names != null) {
 
     //traverse all the dataset ids given by the array
     for(int i=0;i<names.length;i++) {
 
         id = names[i];
         String marker = "\"response\":";
         String responseRawString = getResponseBody(id);
         int start = responseRawString.lastIndexOf(marker) + marker.length();
         int end = responseRawString.length();
         String extractedString = responseRawString.substring(start, end);
         JSONObject responseBody = new JSONObject(extractedString);
         JSONArray docsJSON = responseBody.getJSONArray("docs");
 
         try{
             
             JSONObject docJSON = new JSONObject(docsJSON.get(0).toString());
             
             //  create <doc> element
             Element docEl = new Element("doc");
 
             //  create doc/dataset_id element
             Element dataset_idEl = new Element("dataset_id");
             dataset_idEl.addContent(id);
             docEl.addContent(dataset_idEl);
             
             //insert initial file here
             //this is to combat the array vs. json object problem
             Element fileEl = createInitialFileElement(docJSON);
             docEl.addContent(fileEl);
             
             for(int j=0;j<docsJSON.length();j++) {
                 docJSON = new JSONObject(docsJSON.get(j).toString());
                 fileEl = createFileElement(docJSON);
                 docEl.addContent(fileEl);
             }
             
             document.getRootElement().addContent(docEl);
             
             XMLOutputter outputter = new XMLOutputter();
             xmlOutput = outputter.outputString(document.getRootElement());
 
             JSONObject returnJSON = XML.toJSONObject(xmlOutput);
 
             jsonContent = returnJSON.toString();
         }
         catch(Exception e) {
             LOG.debug("\nJSON Error in converting template format \n");
         }
 
     }
 }
 */
