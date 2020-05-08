 package org.esgf.adminui;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.util.Enumeration;
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
  * Implementation of metadata extraction controller responsible for extracting metadata that ARE NOT contained in the solr records.
  * The controller searches through the metdata file to find the proper record.  Currently parsing of TDS, OAI, CAS, and FGDC files are supported.
  *
  * @author john.harney
  *
  */
 @Controller
 @RequestMapping("/extractgroupdataproxy")
 public class ExtractGroupInfoController {
     
     private final static Logger LOG = Logger.getLogger(ExtractGroupInfoController.class);
     
     private final static boolean debugFlag = true;
 
     private GroupOperationsInterface goi;
     private UserOperationsInterface uoi;
     
     public ExtractGroupInfoController() throws FileNotFoundException, IOException {
         LOG.debug("IN CreateGroupsController Constructor");
         //System.out.println("\n\n\n\n\nGroup info " + Utils.environmentSwitch + "\n\n\n");
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
     @RequestMapping(method=RequestMethod.POST)
     public @ResponseBody String doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, JSONException, ParserConfigurationException, JDOMException {
         LOG.debug("ExtractGroupInfoController doPost");
 
         String type = request.getParameter("type");
         
         String groupName = request.getParameter("groupName");
         if(debugFlag)
             LOG.debug("Type: " + type);
         
         if(type.equalsIgnoreCase("edit")) {
             return processEditType(request, response);
         }
         else if(type.equalsIgnoreCase("getGroupInfo")) {
             return processGetGroupInfoType(groupName);
         }
         else if(type.equalsIgnoreCase("getAllUsersInGroup")) {
             return processGetAllUsersInGroupType(groupName);
         }
         else if(type.equalsIgnoreCase("getGroupForUser")) {
             return processgetGroupsForUserType(request, response);
             
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
     @RequestMapping(method=RequestMethod.GET)
     public @ResponseBody String doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, JSONException, ParserConfigurationException, JDOMException {
         LOG.debug("ExtractGroupInfoController doGet");
 
         String type = request.getParameter("type");
         
         String groupName = request.getParameter("groupName");
         
         
         LOG.debug("Type->" + type);
         LOG.debug("groupName->" + groupName);
         if(type.equalsIgnoreCase("edit")) {
             return processEditType(request, response);
         }
         else if(type.equalsIgnoreCase("getGroupInfo")) {
             return processGetGroupInfoType(groupName);
         }
         else if(type.equalsIgnoreCase("getAllUsersInGroup")) {
             return processGetAllUsersInGroupType(groupName);
         }
         else if(type.equalsIgnoreCase("getGroupForUser")) {
             return processgetGroupsForUserType(request, response);
             
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
     private String processGetAllUsersInGroupType(String groupName) throws IOException, JSONException, ParserConfigurationException, JDOMException {
         LOG.debug("ExtractGroupInfoController processGetAllUsersInGroupType|->" + groupName);
         String jsonContent = "jsonContent";
 
         String xmlOutput = "<users>";
         xmlOutput += "<ingroup>";
 
         List<User> users = uoi.getUsersFromGroup(groupName);//uoi.getAllUsers();
 
         if(users != null) {
             for(int i=0;i<users.size();i++) {
                 User user = users.get(i);
                 xmlOutput += user.toXml();
             }
         }
 
         xmlOutput += "</ingroup>";
         
         
         xmlOutput += "<allusers>";
         
         users = uoi.getAllUsers();//uoi.getAllUsers();
         if(users != null) {
             
             for(int i=0;i<users.size();i++) {
                 
                 User user = users.get(i);
                 if(user == null) {
                 } else {
                     xmlOutput += user.toXml();
                 }
                 
             }
         }
 
         xmlOutput += "</allusers>";
         
         xmlOutput += "</users>";
         
 
         JSONObject jo = XML.toJSONObject(xmlOutput);
         jsonContent = jo.toString();
         
         //LOG.debug("JsonContent: " + jsonContent);
         return jsonContent;
     }
     
     
     /**
      * @param  request  HttpServletRequest object containing the query string
      * @param  response  HttpServletResponse object containing the metadata in json format
      * @throws JDOMException 
      *
      */
     private String processGetGroupInfoType(String groupName) throws IOException, JSONException, ParserConfigurationException, JDOMException {
         LOG.debug("ExtractGroupInfoController processGetGroupInfoType");
         String jsonContent = "jsonContent";
 
         String xmlOutput = "<groupinfo>";
 
         Group group = goi.getGroupObjectFromGroupName(groupName);
         if(group == null) LOG.debug("null");
         else
             LOG.debug("group: " + group);
         xmlOutput += group.toXml();
         
         List<User> users = uoi.getUsersFromGroup(groupName);
         
         for(int i=0;i<users.size();i++) {
             User user = users.get(i);
             xmlOutput += user.toXml();
         }
         
         xmlOutput += "</groupinfo>";
         
         JSONObject jo = XML.toJSONObject(xmlOutput);
         jsonContent = jo.toString();
 
         //LOG.debug("JsonContent: " + jsonContent);
         
         return jsonContent;
     }
     
     
     /**
      * @param  request  HttpServletRequest object containing the query string
      * @param  response  HttpServletResponse object containing the metadata in json format
      * @throws JDOMException 
      *
      */
     private String processgetGroupsForUserType(HttpServletRequest request, HttpServletResponse response) throws IOException, JSONException, ParserConfigurationException, JDOMException {
         LOG.debug("ExtractGroupInfoController processgetGroupsForUserType");
         String jsonContent = "jsonContent";
 
         String userName = request.getParameter("id");
 
         String userId = uoi.getUserIdFromUserName(userName);
         
         LOG.debug("userId->" + userId);
         
         
         //List<Group> groups = goi.getGroupsFromUser(userId);
         //LOG.debug(groups);
         
         String xmlOutput = "<groups>";
         //for(int i=0;i<groups.size();i++) {
         //    Group group = (Group)groups.get(i);
         //    xmlOutput += group.toXml();
         //}
         xmlOutput += "</groups>";
         JSONObject jo = XML.toJSONObject(xmlOutput);
         jsonContent = jo.toString();
 
         //LOG.debug("JsonContent: " + jsonContent);
         return jsonContent;
     }
     
     
     
     /**
      * @param  request  HttpServletRequest object containing the query string
      * @param  response  HttpServletResponse object containing the metadata in json format
      * @throws JDOMException 
      *
      */
     private String processEditType(HttpServletRequest request, HttpServletResponse response) throws IOException, JSONException, ParserConfigurationException, JDOMException {
         LOG.debug("ExtractGroupInfoController processEditType");
         String jsonContent = "jsonContent";
 
         String groupName = request.getParameter("id");
         
         
         
         //queryStringInfo(request);
         
         
         try {
             //db version
             //LOG.debug("GroupId->" + groupId);
             Group group = goi.getGroupObjectFromGroupName(groupName);
             //User user = UserOps.getUserObjectFromUserName(userName);
             String xmlOutput = group.toXml();
             //LOG.debug(xmlOutput);
             //String xmlOutput = "<group></group>";
             
             JSONObject jo = XML.toJSONObject(xmlOutput);
 
             jsonContent = jo.toString();
             
         }catch(Exception e) {
             LOG.debug("Problem with conversion to json content in processEditType");
         }
         
         //LOG.debug("JsonContent: " + jsonContent);
         
         return jsonContent;
     }
     
     
     
     /**
      * queryStringInfo(HttpServletRequest request)
      * Private method that prints out the contents of the request.  Used mainly for debugging.
      * 
      * @param request
      */
     @SuppressWarnings("unchecked")
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
 
 
