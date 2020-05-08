 package org.esgf.adminui;
 import java.io.File;
 import java.io.IOException;
 import java.net.URL;
 import java.util.Enumeration;
 import java.util.List;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.xml.parsers.ParserConfigurationException;
 
 import org.apache.log4j.Logger;
 import org.esgf.metadata.JSONArray;
 import org.esgf.metadata.JSONException;
 import org.esgf.metadata.JSONObject;
 import org.esgf.metadata.XML;
 import org.esgf.web.MetadataExtractorController;
 import org.jdom.Document;
 import org.jdom.Element;
 import org.jdom.JDOMException;
 import org.jdom.Namespace;
 import org.jdom.input.SAXBuilder;
 import org.jdom.output.XMLOutputter;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.ResponseBody;
 /**
  * Implementation of metadata extraction controller responsible for extracting metadata that ARE NOT contained in the solr records.
  * The controller searches through the metdata file to find the proper record.  Currently parsing of TDS, OAI, CAS, and FGDC files are supported.
  *
  * @author john.harney
  *
  */
 @Controller
 @RequestMapping("/extractuserdataproxy")
 public class ExtractUserInfoController {
     private final static Logger LOG = Logger.getLogger(ExtractUserInfoController.class);
     
     //change me later
     private final static String USERS_FILE = "C:\\Users\\8xo\\esgProjects\\esgf-6-29\\esgf-web-fe\\esgf-web-fe\\src\\java\\main\\users.file";
 
 
     private final static boolean debugFlag = true;
     /**
      * Note: GET and POST contain the same functionality.
      *
      * @param  request  HttpServletRequest object containing the query string
      * @param  response  HttpServletResponse object containing the metadata in json format
      * @throws JDOMException 
      *
      */
     @RequestMapping(method=RequestMethod.GET)
     public @ResponseBody String doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, JSONException, ParserConfigurationException, JDOMException {
         LOG.debug("ExtractUserInfoController doGet");
 
         String type = request.getParameter("type");
         if(debugFlag)
             LOG.debug("Type: " + type);
         
         if(type.equalsIgnoreCase("edit")) {
             return processEditType(request, response);
         }
         else {
             return null;
         }
     }
     
     /**
      * Note: GET and POST contain the same functionality.
      *
      * @param  request  HttpServletRequest object containing the query string
      * @param  response  HttpServletResponse object containing the metadata in json format
      * @throws JDOMException 
      *
      */
     @RequestMapping(method=RequestMethod.POST)
     public @ResponseBody String doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, JSONException, ParserConfigurationException, JDOMException {
         LOG.debug("ExtractUserInfoController doPost");
 
         String type = request.getParameter("type");
         if(debugFlag)
             LOG.debug("Type: " + type);
         
         if(type.equalsIgnoreCase("edit")) {
             return processEditType(request, response);
         }
         else {
             return null;
         }
     }
     
     /**
      * @param  request  HttpServletRequest object containing the query string
      * @param  response  HttpServletResponse object containing the metadata in json format
      * @throws JDOMException 
      *
      */
     private String processEditType(HttpServletRequest request, HttpServletResponse response) throws IOException, JSONException, ParserConfigurationException, JDOMException {
         String jsonContent = "jsonContent";
 
         String userName = request.getParameter("id");
         
         if(debugFlag)
             queryStringInfo(request);
         
         /* Search through the data store to see if the id (username or openid) is there 
          * If it is there, then make the appropriate updates,
          * otherwise, just ignore
          * 
          * As of now, the returned data is in JSON format, so there needs to be some conversion between xml/db store to key/value json pairs
          */
         try {
             //xml store version
            String xmlOutput = getXMLTupleOutputFromEdit(userName);
             
             //db version
             
             
             JSONObject jo = XML.toJSONObject(xmlOutput);
 
             jsonContent = jo.toString();
             
         }catch(Exception e) {
             LOG.debug("Problem with conversion to json content in processEditType");
         }
         
         if(debugFlag)
             LOG.debug("JsonContent: " + jsonContent);
         
         return jsonContent;
     }
     
     
     
     /* get the user info for the give userName from the xml store */
     private String getXMLTupleOutputFromEdit(String userName) throws JDOMException, IOException {
         
         String xmlOutput = "";
         
         final File file = new File(USERS_FILE);
         SAXBuilder builder = new SAXBuilder();
         
         Document document = (Document) builder.build(file);
         
         Element rootNode = document.getRootElement();
         
         if(debugFlag)   
             LOG.debug("root name: " + rootNode.getName());
         
         Element returnedEl = null;
         
         List users = (List)rootNode.getChildren();
         for(int i=0;i<users.size();i++)
         {
             Element userEl = (Element) users.get(i);
             Element userNameEl = userEl.getChild("userName");
             if(debugFlag)
                 LOG.debug("USERNAME: " + userNameEl.getTextNormalize());
             if(userNameEl.getTextNormalize().contains(userName)) {
                 if(debugFlag)
                     LOG.debug("found--->: " + userName);
                 
                 returnedEl = userEl;
             }
         }
        
         XMLOutputter outputter = new XMLOutputter();
         xmlOutput = outputter.outputString(returnedEl);
         
         return xmlOutput;
     }
     
     
     /**
      * @param  request  HttpServletRequest object containing the query string
      * @param  response  HttpServletResponse object containing the metadata in json format
      * @throws JDOMException 
      *
      */
     /*
     private String relay(HttpServletRequest request, HttpServletResponse response) throws IOException, JSONException, ParserConfigurationException, JDOMException {
         String queryString = request.getQueryString();
         String jsonContent = "jsonContent";
 
         String userName = request.getParameter("id");
         String type = request.getParameter("type");
         LOG.debug("Type: " + type);
         
         queryStringInfo(request);
         
         try {
             String responseBody = "";
             JSONObject responseBodyJSON = null;
             String xmlOutput = "";
             
 
             final File file = new File(USERS_FILE);
             SAXBuilder builder = new SAXBuilder();
             
             Document document = (Document) builder.build(file);
             
             Element rootNode = document.getRootElement();
             LOG.debug("root name: " + rootNode.getName());
             
             Element returnedEl = null;
             
             List users = (List)rootNode.getChildren();
             for(int i=0;i<users.size();i++)
             {
                 Element userEl = (Element) users.get(i);
                 Element userNameEl = userEl.getChild("userName");
                 LOG.debug("USERNAME: " + userNameEl.getTextNormalize());
                 if(userNameEl.getTextNormalize().contains(userName)) {
                     LOG.debug("found--->: " + userName);
                     
                     returnedEl = userEl;
                 }
             }
            
             XMLOutputter outputter = new XMLOutputter();
             xmlOutput = outputter.outputString(returnedEl);
 
             JSONObject jo = XML.toJSONObject(xmlOutput);
 
             jsonContent = jo.toString();
             
         }catch(Exception e) {
             LOG.debug("Software crash");
         }
         
         
         LOG.debug("JsonContent: " + jsonContent);
         LOG.debug("\nend doGet extractuserdataproxy\n");
         return jsonContent;
         
     }
     */
     
     /**
      * queryStringInfo(HttpServletRequest request)
      * Private method that prints out the contents of the request.  Used mainly for debugging.
      * 
      * @param request
      */
     private void queryStringInfo(HttpServletRequest request) {
         LOG.debug("--------Query String Info--------");
         Enumeration<String> paramEnum = request.getParameterNames();
         
         while(paramEnum.hasMoreElements()) { 
             String postContent = (String) paramEnum.nextElement();
             LOG.debug(postContent+"-->"); 
             LOG.debug(request.getParameter(postContent));
         }
         LOG.debug("--------End Query String Info--------");
     }
 }
 
 
 
 /*
 String[] names = request.getParameterValues("id[]");
 
 String id = "";
 String responseBody = "";
 JSONObject responseBodyJSON = null;
 String xmlOutput = "";
 
 SAXBuilder builder = new SAXBuilder();
 Document document = null;
 
 document = new Document(new Element("response"));
 if(names != null) {
     
   //traverse all the dataset ids given by the array
     for(int i=0;i<names.length;i++) {
         
         id = names[i];
         responseBody = getResponseBody(id);
         responseBodyJSON = new JSONObject(responseBody);
 
         //get the different json texts here
         JSONObject responseJSON = new JSONObject(responseBodyJSON.get("response").toString());
         JSONArray docsJSON = responseJSON.getJSONArray("docs");
         
         try{
         //  create <doc> element
             Element docEl = new Element("doc");
             
         //  create doc/dataset_id element
             Element dataset_idEl = new Element("dataset_id");
             dataset_idEl.addContent(id);
             docEl.addContent(dataset_idEl);
  
             
         //  for each file found
             for(int j=0;j<docsJSON.length();j++) {
                 JSONObject docJSON = new JSONObject(docsJSON.get(j).toString());
                 Element fileEl = createFileElement(docJSON);
                 docEl.addContent(fileEl);
             }
             
             
             document.getRootElement().addContent(docEl);
         }
         catch(Exception e) {
             LOG.debug("\nJSON errors - investigate line 167\n");
         }
         
     }
 }
 
 
 XMLOutputter outputter = new XMLOutputter();
 xmlOutput = outputter.outputString(document.getRootElement());
 
 if(debugFlag) {
     LOG.debug("xmlOutput:\n " + xmlOutput);
 }
 
 
 JSONObject returnJSON = XML.toJSONObject(xmlOutput);
 
 String jsonContent = returnJSON.toString();
 if(debugFlag) {
     LOG.debug("json: \n" + returnJSON.toString());
 }
 return jsonContent;
 */
