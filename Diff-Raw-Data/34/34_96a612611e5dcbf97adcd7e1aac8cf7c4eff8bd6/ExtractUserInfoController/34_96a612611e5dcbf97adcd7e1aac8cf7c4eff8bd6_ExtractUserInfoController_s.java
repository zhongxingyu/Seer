 package org.esgf.adminui;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.util.List;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.xml.parsers.ParserConfigurationException;
 
 import org.apache.log4j.Logger;
 import org.esgf.commonui.GroupOperationsESGFDBImpl;
 import org.esgf.commonui.GroupOperationsInterface;
 import org.esgf.commonui.GroupOperationsXMLImpl;
 import org.esgf.commonui.UserOperationsESGFDBImpl;
 import org.esgf.commonui.UserOperationsInterface;
 import org.esgf.commonui.UserOperationsXMLImpl;
 import org.esgf.commonui.Utils;
 import org.esgf.metadata.JSONException;
 import org.esgf.metadata.JSONObject;
 import org.esgf.metadata.XML;
 import org.jdom.JDOMException;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.ResponseBody;
 /**
  * The ExtractUserInfoController is responsible for getting group information out of the 
  * database.  As of now, this controller is utilized by ajax calls in the following views:
  * - web/WEB-INF/views/adminview/usermanagement_main.jsp
  * 
  * The constructor is called when the page loads.  This is necessary to create Operations objects
  * that allow us to utilize the CRUD methods that are called on the data store.
  * 
  * The entry point is in the doGet method - doPost is not utilized as of yet.
  * 
  *
  * @author john.harney
  *
  */
 @Controller
 @RequestMapping("/extractuserdataproxy")
 public class ExtractUserInfoController {
     
     //Logger for the controller
     private final static Logger LOG = Logger.getLogger(ExtractUserInfoController.class);
     
     //Need to define two interfaces for group and user operations
     private GroupOperationsInterface goi;
     private UserOperationsInterface uoi;
     
     
     public ExtractUserInfoController() throws FileNotFoundException, IOException {
         LOG.debug("IN ExtractUserInfoController Constructor");
         if(Utils.environmentSwitch) {
             goi = new GroupOperationsESGFDBImpl();
             uoi = new UserOperationsESGFDBImpl();
         }
         else {
             goi = new GroupOperationsXMLImpl();
             uoi = new UserOperationsXMLImpl();
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
     @RequestMapping(method=RequestMethod.GET)
     public @ResponseBody String doGet(HttpServletRequest request, HttpServletResponse response) {
         LOG.debug("ExtractUserInfoController doGet");
 
         String jsonContent = "";
             
         Utils.queryStringInfo(request);
         
         String userName = request.getParameter("userName");
         String type = request.getParameter("type");
         
         if(type.equalsIgnoreCase("getUserInfo")) {
 
             jsonContent = processGetUserInfoType(userName);
             
         }
 
         //LOG.debug("JsonContent: " + jsonContent);
         return jsonContent;
         
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
         
         LOG.debug("Type: " + type);
         
             
         return null;
     }
     
     /**
      * @param  request  HttpServletRequest object containing the query string
      * @param  response  HttpServletResponse object containing the metadata in json format
      * @throws JDOMException 
      *
      */
     private String processGetUserInfoType(String userName) {
         LOG.debug("ExtractUserInfoController processGetUserInfoType|-->" + userName);
 
         
         String jsonContent = "";
         
         User user = uoi.getUserObjectFromUserName(userName);
         
         try {
             String xmlOutput = "<userinfo>";
             xmlOutput += user.toXml();
             
             String userId = (uoi.getUserObjectFromUserName(userName)).getUserId();
             List<Group> groups = uoi.getGroupsFromUser(userName);
             //goi.getGroupObjectFromGroupName(groupName)
             //List<Group> groups = goi.getGroupsFromUser(userId);
             
             if(groups != null) {
                 xmlOutput += "<groups>";
                 for(int i=0;i<groups.size();i++) {
                     xmlOutput += groups.get(i).toXml();
                 }
                 xmlOutput += "</groups>";
                 
             }
             
             xmlOutput += "</userinfo>";
             
             JSONObject jo = XML.toJSONObject(xmlOutput);
 
             jsonContent = jo.toString();
         } catch(Exception e) {
             LOG.debug("Problem in ExtractUserInfoController processGetUserInfoType");
             e.printStackTrace();
         }
         
         LOG.debug("JSONCONTENT: " + jsonContent);
 
         return jsonContent;
     }
     
     
     
     
 }
 
