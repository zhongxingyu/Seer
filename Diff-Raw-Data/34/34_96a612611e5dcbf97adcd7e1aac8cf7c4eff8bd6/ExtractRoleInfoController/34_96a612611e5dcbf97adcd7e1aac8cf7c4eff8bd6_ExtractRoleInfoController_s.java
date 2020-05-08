 package org.esgf.adminui;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.xml.parsers.ParserConfigurationException;
 
 import org.apache.log4j.Logger;
 import org.esgf.commonui.GroupOperationsESGFDBImpl;
 import org.esgf.commonui.GroupOperationsInterface;
 import org.esgf.commonui.GroupOperationsXMLImpl;
 import org.esgf.commonui.RoleOperationsESGFDBImpl;
 import org.esgf.commonui.RoleOperationsInterface;
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
 @RequestMapping("/extractroledataproxy")
 public class ExtractRoleInfoController {
     
     private final static Logger LOG = Logger.getLogger(ExtractRoleInfoController.class);
     
     private final static boolean debugFlag = true;
 
     private GroupOperationsInterface goi;
     private UserOperationsInterface uoi;
     private RoleOperationsInterface roi;
     
     public ExtractRoleInfoController() throws FileNotFoundException, IOException {
         LOG.debug("IN ExtractRoleInfoController Constructor");
         //System.out.println("\n\n\n\n\n\n\n" + Utils.environmentSwitch + "\n\n\n\n\n\n\n");
         if(Utils.environmentSwitch) {
             
             goi = new GroupOperationsESGFDBImpl();
             uoi = new UserOperationsESGFDBImpl();
             roi = new RoleOperationsESGFDBImpl();
             
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
     public @ResponseBody String doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, JSONException, ParserConfigurationException, JDOMException {
         LOG.debug("ExtractRoleInfoController doGet");
 
         //Utils.queryStringInfo(request);
         
         String groupName = request.getParameter("groupName");
         String userName = request.getParameter("userName");
         
         LOG.debug("groupName->" + groupName);
         LOG.debug("userName->" + userName);
         
         String groupId = goi.getGroupIdFromGroupName(groupName);
         String userId = uoi.getUserIdFromUserName(userName);
 
         Role role = roi.getRoleForUserInGroup(userId, groupId);
         
         //String xmlOutput = "<roles>";
         String xmlOutput = "";
         xmlOutput += role.toXml();
         //xmlOutput += "</roles>";
         JSONObject jo = XML.toJSONObject(xmlOutput);
         String jsonContent = jo.toString();
 
         LOG.debug("JsonContent: " + jsonContent);
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
         LOG.debug("ExtractRoleInfoController doPost");
 
         //Utils.queryStringInfo(request);
         
         String groupName = request.getParameter("groupName");
         String userName = request.getParameter("userName");
         
         LOG.debug("groupName->" + groupName);
         LOG.debug("userName->" + userName);
         
         String groupId = goi.getGroupIdFromGroupName(groupName);
         String userId = uoi.getUserIdFromUserName(userName);
 
         Role role = roi.getRoleForUserInGroup(userId, groupId);
         
         //String xmlOutput = "<roles>";
         String xmlOutput = "";
         xmlOutput += role.toXml();
         //xmlOutput += "</roles>";
         JSONObject jo = XML.toJSONObject(xmlOutput);
         String jsonContent = jo.toString();
 
         LOG.debug("JsonContent: " + jsonContent);
         return jsonContent;
     }
     
     
     
     
 }
 
 
