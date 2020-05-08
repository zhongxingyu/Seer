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
 import java.io.UnsupportedEncodingException;
 import java.net.MalformedURLException;
 import java.net.URL;
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
 import org.esgf.metadata.XML;
 import org.springframework.mock.web.MockHttpServletRequest;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.ResponseBody;
 
 
 
 
 @Controller
 @RequestMapping("/solrfileproxy")
 public class FileDownloadTemplateController {
 
     
     private static String solrURL="http://localhost:8983/solr/select";
     
    private static String searchAPIURL = "http://localhost:8081/esg-search/search?";
     
     private final static Logger LOG = Logger.getLogger(FileDownloadTemplateController.class);
 
     //for debugging
     private final static int startCharIndex = 600;
     private final static int endCharIndex = 2600;
 
     private final static boolean doGetDebug = false;
     private final static boolean clockDebug = false;
     private final static boolean preAssembleQueryStringDebug = false;
     private final static boolean getDocElementDebug = false;
     private static final boolean getFileElementsDebug = false;
     private static final boolean querySolrForFilesDebug = false;
     private static final boolean solrResponseDebug = false;
     private static final boolean solrResponseToJSONDebug = false;
     private static final boolean resposneToJSONDebug = false;
     
     private static final int numInitialFilesShown = 10;
     
     
     /**
      * Main method to test the controller using Mock Objects
      * 
      * @param args
      */
     public static void main(String [] args) {
         
         final MockHttpServletRequest mockRequest = new MockHttpServletRequest();
         //String [] id = {"obs4MIPs.NASA-JPL.AIRS.mon","obs4MIPs.NASA-JPL.AIRS.mon.v1:esg-datanode.jpl.nasa.gov"};
         //String peer = "esg-datanode.jpl.nasa.gov;undefined";
         String [] id = {"obs4MIPs.NASA-JPL.AIRS.mon"};
         String peer = "esg-datanode.jpl.nasa.gov";
         
         
         String technotes = "http://esg-datanode.jpl.nasa.gov/thredds/fileServer/esg_dataroot/obs4MIPs/technotes/taTechNote_AIRS_L3_RetStd-v5_200209-201105.pdf|AIRS Air Temperature Technical Note|Technical Note,http://esg-datanode.jpl.nasa.gov/thredds/fileServer/esg_dataroot/obs4MIPs/technotes/husTechNote_AIRS_L3_RetStd-v5_200209-201105.pdf|AIRS Specific Humidity Technical Note|Technical Note";
         
         String showAll = "false";
         
         //this is the string that I will get
         String fqStr = ",offset=0,replica=false,project=obs4MIPs";
         //need to convert to fq []
         
         String [] fq = fqStr.split(",");
         //;undefined
         
         mockRequest.addParameter("id[]", id);
         mockRequest.addParameter("peer", peer);
         mockRequest.addParameter("showAll", showAll);
         mockRequest.addParameter("fq[]", fq);
         mockRequest.addParameter("technotes", technotes);
         
         FileDownloadTemplateController fc = new FileDownloadTemplateController();
         
         try {
             String dataCart = fc.doGet(mockRequest);
             
             //System.out.println("\n\n\nDATACART: " + dataCart);
             
         } catch (JSONException e) {
             e.printStackTrace();
         }
         
         
     }
     
     
 
     
     /**
      * Function doGet
      * 
      * Entry point into the FileDownloadController
      * 
      * @param request
      * @return
      * @throws JSONException
      */
     @RequestMapping(method=RequestMethod.GET)
     public @ResponseBody String doGet(HttpServletRequest request) throws JSONException {
         
         String dataCart = "";
         
         if(doGetDebug)
             System.out.println("---In doGet--");
 
         String peerStr = request.getParameter("peer");
         String technoteStr = request.getParameter("technotes");
         String showAllStr = request.getParameter("showAll");
         
         String [] fq = request.getParameterValues("fq[]");
         //need to parse fqStr and create an array [] fq
         
         String initialQuery = request.getParameter("initialQuery");
         
         //System.out.println("\n\nINITIALQUERY\n" + initialQuery + "\n\n\n");
         
         
         if(initialQuery.equals("true")) {
             String queryString = preassembleQueryString(showAllStr,fq);
             
             //queryString += "&rows=" + numInitialFilesShown;
 
             String [] peers = peerStr.split(";");
             String [] technotes = technoteStr.split(";");
             String [] id = request.getParameterValues("id[]");
             
             
             if(id != null) {
                 
                 List<DocElement> docElements = new ArrayList<DocElement>();
 
                 long beginTime = System.nanoTime();
                 
                 //peers, technotes and dataset id lengths SHOULD BE THE SAME
                 //if not, fail gracefully
                 
                 if(doGetDebug) {
                     System.out.println("peer length: " + peers.length + " id length: " + id.length);
                 }
                 
                 for(int i=0;i<id.length;i++) {
                     DocElement docElement = getDocElement(queryString,id[i],peers[i],technotes[i]);
                     if(doGetDebug) {
                         System.out.println("\tAdding docElement: " + i);
                     }
                     docElements.add(docElement);
 
                 }
 
                 dataCart = responseToJSON(docElements);
 
                 
                 if(doGetDebug)
                     System.out.println("DATACART\n" + dataCart);
 
                 long endTime = System.nanoTime();
                 
                 if(clockDebug) {
                     System.out.println("--TIME MEASUREMENT FOR LOADING DATACART--");
                     System.out.println("\tTotal Time: " + ((int)(endTime - beginTime)) + "ns");
                     
                 }
                 
             }
             
             
             if(doGetDebug)
                 System.out.println("---End doGet--");
             
             return dataCart;
             
             
         } else {
             
             String queryString = preassembleQueryString(showAllStr,fq);
 
             //change the offset
             queryString += "&offset=" + numInitialFilesShown;
             
             String id = request.getParameter("id");
             
             
             DocElement docElement = getDocElement(queryString,id,peerStr,technoteStr);
             
             List<DocElement> docElements = new ArrayList<DocElement>();
             docElements.add(docElement);
             
             
             
             dataCart = responseToJSON(docElements);
             
             return dataCart;
             
         }
         
         
     }
     
     public static String responseToJSON(List<DocElement> docElements) {
         if(resposneToJSONDebug)
             System.out.println("---responseToJSON--");
         
         String jsonContent = "";
         
         
         //create xml and convert to JSON
         //***UPDATE ME WITH A PROPER MARSHALLER!!!!***
         String xmlStr = "";
         for(int i=0;i<docElements.size();i++) {
             xmlStr += docElements.get(i).toXML();
         }
         JSONObject returnJSON = null;
         try {
             returnJSON = XML.toJSONObject(xmlStr);
         } catch (JSONException e) {
             e.printStackTrace();
         }
         
         //convert the JSON object back to a string
         jsonContent = returnJSON.toString();
         
         
         if(resposneToJSONDebug) {
             try {
                 String json2xml = XML.toString(returnJSON);
 
                 System.out.println(json2xml);
             } catch (JSONException e) {
                 // TODO Auto-generated catch block
                 e.printStackTrace();
             }
             
             System.out.println("JSONCONTENT\n" + jsonContent);
         }
         
         
         if(resposneToJSONDebug) 
             System.out.println("---responseToJSON--");
 
         return jsonContent;
     }
     
     private static String preassembleQueryString(String showAll,String [] fq) {
 
         if(preAssembleQueryStringDebug) {
             System.out.println("---Preassemble query string---");
         }
         
         //form the base querystring
         //format=application/solr+json
         //type=File
         String queryString = "";
         queryString += "format=application%2Fsolr%2Bjson&type=File";//
         
         //put any search criteria if the user selected "filter"
         if(showAll.equals("false")) {
             if(preAssembleQueryStringDebug) {
                 System.out.println("\tFiltered selected");
             }
             String [] fqParams = fq;
             String fullText = "";
             if(fqParams != null) {
                 if(preAssembleQueryStringDebug) {
                     System.out.println("\t  Search criteria:");
                 }
                 for(int i=0;i<fqParams.length;i++) {
                     String fqParam = fqParams[i];
                     if(preAssembleQueryStringDebug) {
                         System.out.println("\t    Adding: " + fqParam);
                     }
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
                     if(!ignore) {
                     //if(!fqParam.equals("") && 
                     //   !fqParam.equals(" ")) {
                         if(!fqParam.contains("query")) {
                             queryString += "&" + fqParam;
                         } else {
                             String [] clause = fqParam.split("=");
                             fullText += clause[1] + "%20";
                         }
                     }
                 }//end for
                 if(!fullText.equals("") && !fullText.equals(" ")) {
                     queryString += "&query=" + fullText;
                 }
             }
         }
         
         if(preAssembleQueryStringDebug) {
             System.out.println("\tReturned query string: " + queryString);
         }
         
         if(preAssembleQueryStringDebug) {
             System.out.println("---End Preassemble query string---");
         }
         return queryString;
     }
 
     
     
     public static DocElement getDocElement(String queryString,String dataset_id,String peer,String technote) {
     
         //add the peer to the query string
         if(getDocElementDebug) {
             System.out.println("---Get Doc Element---");
         }
         
         //add the peer to the query string IF there was one given
         if(!peer.equals("undefined")) {
             queryString += "&shards=" + peer + ":8983/solr";
         }
         
         if(getDocElementDebug) {
             System.out.println("\tNew QueryString (with shard): " + queryString);
         }
         
         
         DocElement docElement = null;
         
         //start building the doc element here
         try {
             //create doc element
             docElement = new DocElement();
             
             //add the dataset id
             docElement.setDatasetId(dataset_id);
 
             //add the technote information
             /*
             TechnotesElement technotesElement = new TechnotesElement();
             String [] technotes = technote.split(",");
             for(int i=0;i<technotes.length;i++) {
                 String [] technoteTokens = technotes[i].split("\\|");
                 String location = technoteTokens[0];
                 String name = technoteTokens[1];
                 TechnoteElement te = new TechnoteElement();
                 te.setLocation(location);
                 te.setName(name);
                 technotesElement.addTechnoteElement(te);
             }
             docElement.setTechnotesElement(technotesElement);
             */
             
             List<FileElement> fileElements = getFileElements(queryString,dataset_id);
 
             //create a count element
             docElement.setCount(fileElements.size());
 
             //determine here if grid ftp should be displayed
             
             
             
             
             //attach the file elements to the document
             for(int i=0;i<fileElements.size();i++) {
                 
                 docElement.addFileElement(fileElements.get(i));
                 
                 if(getDocElementDebug) {
                     System.out.println("\tAdding file: " + i);
                 }
             }
             
             
         }catch(Exception e) {
             System.out.println("Problem building doc element");
         }
         
         if(getDocElementDebug) {
             System.out.println("---End Get Doc Element---");
         }
 
         return docElement;
     }
     
     
     
     private static List<FileElement> getFileElements(String queryString,String dataset_id) {
         
         if(getFileElementsDebug) {
             System.out.println("---Get File Elements---");
         }
         
         List<FileElement> fileElements = new ArrayList<FileElement>();
         
         //raw response from solr of files matching the query for dataset_id
         String solrResponse = querySolrForFiles(queryString,dataset_id);
         
         if(getFileElementsDebug) {
             System.out.println("\tsolr response");
         }
         
         //convert to JSON array
         JSONArray files = solrResponseToJSON(solrResponse);
 
         for(int j=0;j<files.length();j++) {
             
             try {
                 //grab the JSON object
                 JSONObject docJSON = new JSONObject(files.get(j).toString());
 
                 //create a new FileElement from the JSONObject
                 FileElement fileElement = new FileElement(docJSON,"solr");
                 //System.out.println("\ttracking id" + fileElement.getTrackingId());
                 fileElements.add(fileElement);
                 
             } catch(Exception e) {
                 System.out.println("Problem assembling files");
             }
         }
 
         
         if(getFileElementsDebug) {
             System.out.println("---End Get File Elements---");
         }
 
         return fileElements;
     }
     
     
     
     /**
      * 
      * @param rawString
      * @return
      */
     private static JSONArray solrResponseToJSON(String rawString) {
         if(solrResponseToJSONDebug) {
             System.out.println("---SolrResponseToJSON---");
         }
         
         //convert extracted string into json array
         JSONObject jsonResponse = null;
         JSONArray jsonArray = null;
         try {
             jsonResponse = new JSONObject(rawString);
             jsonArray = jsonResponse.getJSONArray("docs");
         } catch (JSONException e) {
             // TODO Auto-generated catch block
             System.out.println("Problem converting Solr response to json string");
             e.printStackTrace();
         }
         
         if(solrResponseToJSONDebug) {
             System.out.println("---End SolrResponseToJSON---");
         }
         return jsonArray;
     }
     
     
     /**
      * 
      * @param queryString
      * @param dataset_id
      * @return
      */
     private static String querySolrForFiles(String queryString,String dataset_id) {
         if(querySolrForFilesDebug) {
             System.out.println("---Query Solr for Files---");
         }
         
         String marker = "\"response\":";
         
         String responseBody = null;
         
         // create an http client
         HttpClient client = new HttpClient();
 
         //attact the dataset id to the query string
         GetMethod method = new GetMethod(searchAPIURL);
         
         //dataset_id = dataset_id.replace("|", "%7C");
         if(querySolrForFilesDebug) {
             System.out.println("\n\n\tQueryString issued to solr (before encoding) -> " + (queryString+"&dataset_id=" + dataset_id));
         }
         
         //add the dataset to the query string
         try {
             queryString += "&dataset_id=" + URLEncoder.encode(dataset_id,"UTF-8").toString();
         } catch (UnsupportedEncodingException e1) {
             // TODO Auto-generated catch block
             e1.printStackTrace();
         }
         
         if(querySolrForFilesDebug) {
             System.out.println("\tQueryString issued to solr -> " + queryString + "\n\n");
         }
         
         method.setQueryString(queryString);
         
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
         
         if(solrResponseDebug) {
             System.out.println("\tResponse:\n\t" + responseString);
         }
         
         if(querySolrForFilesDebug) {
             System.out.println("---End Query Solr for Files---");
         }
 
         return responseString;
     }
     
     
     
     
     
     
     /**
      * queryStringInfo(HttpServletRequest request)
      * Private method that prints out the contents of the request.  Used mainly for debugging.
      * 
      * @param request
      */
     @SuppressWarnings("unchecked")
     public static void queryStringInfo(HttpServletRequest request) {
         System.out.println("--------Utils Query String Info--------");
         Enumeration<String> paramEnum = request.getParameterNames();
         
         while(paramEnum.hasMoreElements()) { 
             String postContent = (String) paramEnum.nextElement();
             System.out.println(postContent+"-->"); 
             System.out.println(request.getParameter(postContent));
         }
         System.out.println("--------End Utils Query String Info--------");
     }
 
     
     
 }
 
 
