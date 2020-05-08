 package org.esgf.commonui;
 
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.Writer;
 import java.util.Enumeration;
 import java.util.List;
 import java.util.Random;
 
 import javax.servlet.http.Cookie;
 import javax.servlet.http.HttpServletRequest;
 
 import org.apache.log4j.Logger;
 import org.esgf.adminui.AccountsController;
 import org.esgf.adminui.User;
 import org.jdom.Document;
 import org.jdom.Element;
 
 import org.jdom.JDOMException;
 import org.jdom.input.SAXBuilder;
 import org.jdom.output.XMLOutputter;
 import org.springframework.ui.Model;
 
 public class Utils {
     
     private final static Logger LOG = Logger.getLogger(Utils.class);
     
    //switch used for development purposes 
    //a "false" represents a disconnect from postgres
    public final static boolean environmentSwitch = true;
     
     public static String getPassword(File file) {
         String passwd = null;
         
         StringBuffer contents = new StringBuffer();
         BufferedReader reader = null;
         try {
             reader = new BufferedReader(new FileReader(file));
             int counter = 0;
             String line = null;
             while ((line = reader.readLine()) != null) {
                 if(counter == 0) {
                     passwd = line;
                 }
                 counter++;
             }
         } catch (Exception e) {
             e.printStackTrace();
         }
         
         return passwd;
     }
     
     /*
      * Used to extract the openid from the cookie in the header of a request
      */
     public static String getIdFromHeaderCookie(HttpServletRequest request) {
         LOG.debug("------Utils getIdFromHeaderCookie------");
         
         Cookie [] cookies = request.getCookies();
         
         String userId = "";
         
         for(int i=0;i<cookies.length;i++) {
             LOG.debug("CookieArray: " + i + " " + cookies[i].getName());
             if(cookies[i].getName().equals("esgf.idp.cookie")) {
                 userId = cookies[i].getValue();
             }
         }
 
         LOG.debug("------End tils getIdFromHeaderCookie------");
         return userId;
     }
     
     public static void writeXMLContentToFile(Element rootNode,File file) {
         XMLOutputter outputter = new XMLOutputter();
         String xmlContent = outputter.outputString(rootNode);
         
         try {
             Writer output = null;
             output = new BufferedWriter(new FileWriter(file));
             output.write(xmlContent);
             
             output.close();
         } catch(Exception e) {
             System.out.println("Error in writeXMLContentTOFile");
             e.printStackTrace();
         }
         
     }
     
     public static String createGroupId(File file) {
         Random rand = new Random();
         
         int num = rand.nextInt();
         while(groupIdExists(num,file)) {
             num = rand.nextInt();
         }
         String str = "group" + num + "_id";
         return str;
     }
     
     public static boolean groupIdExists(int id,File file) {
         boolean idExists = false;
         SAXBuilder builder = new SAXBuilder();
         String xmlContent = "";
         //File file = GROUPS_FILE;
         
         try{
 
             Document document = (Document) builder.build(file);
             
             Element rootNode = document.getRootElement();
         
             List groups = (List)rootNode.getChildren();
             String intStr = Integer.toString(id);
             for(int i=0;i<groups.size();i++)
             {
                 Element groupEl = (Element)groups.get(i);
                 Element groupIdEl = groupEl.getChild("groupid");
                 String groupId = groupIdEl.getTextNormalize();
                 if(groupId.contains(intStr)) {
                     idExists = true;
                 }
             }
         
         
         }catch(Exception e) {
             System.out.println("Problem in idExists");
             
         }
         return idExists;
     }
     
     
     public static String createUserId(File file) {
         Random rand = new Random();
         
         int num = rand.nextInt(100);
         while(idExists(num,"id",file)) {
             num = rand.nextInt();
         }
         String str = "user" + num;
         return str;
     }
     
     
     public static boolean idExists(int id,String cat,File file) {
         boolean idExists = false;
         SAXBuilder builder = new SAXBuilder();
         String xmlContent = "";
         try{
             Document users_document = (Document) builder.build(file);
             Element rootNode = users_document.getRootElement();
             List users = (List)rootNode.getChildren();
             String intStr = Integer.toString(id);
             
             for(int i=0;i<users.size();i++)
             {
                 Element userEl = (Element)users.get(i);
                 Element userIdEl = userEl.getChild(cat);
                 String userId = userIdEl.getTextNormalize();
                 
                 if(userId.contains(intStr)) {
                     idExists = true;
                     System.out.println("Id exists");
                 }
             }
             
         }catch(Exception e) {
             e.printStackTrace();
             LOG.debug("Error in getUserIdFromOpenID");
         }
         
         return idExists;
     }
     
     public static String createOpenId(File file) {
         Random rand = new Random();
         
         int num = rand.nextInt(100);
         while(idExists(num,"openid",file)) {
             num = rand.nextInt();
         }
         String str = "openid" + num;
         return str;
     }
     
     public static String createUserName(File file) {
         Random rand = new Random();
         
         int num = rand.nextInt(100);
         while(idExists(num,"username",file)) {
             num = rand.nextInt();
         }
         String str = "username" + num;
         return str;
     }
     
     public static String createUserDN(File file) {
         Random rand = new Random();
         
         int num = rand.nextInt(100);
         while(idExists(num,"dn",file)) {
             num = rand.nextInt();
         }
         String str = "dn" + num;
         return str;
     }
     
     //Used by ManageUsersController to obtain the "type"
     @SuppressWarnings("unchecked")
     public static String getTypeFromQueryString(HttpServletRequest request) {
         LOG.debug("------Utils getTypeFromQueryString------");
         String type = "";
         Enumeration<String> paramEnum = request.getParameterNames();
         while(paramEnum.hasMoreElements()) { 
             String postContent = (String) paramEnum.nextElement();
             if(postContent.equalsIgnoreCase("type")) {
                 type = request.getParameter(postContent);
             }
         }
         LOG.debug("------End Utils getTypeFromQueryString------");
         return type;
     }
     
     
     
     
     
     /**
      * headerStringInfo(HttpServletRequest request)
      * Private method that prints out the header contents of the request.  Used mainly for debugging.
      * 
      * @param request
      */
     public static void headerStringInfo(HttpServletRequest request) {
         LOG.debug("--------Utils Header String Info--------");
         Enumeration headerNames = request.getHeaderNames(); 
         while(headerNames.hasMoreElements()) { 
             String headerName = (String)headerNames.nextElement(); 
             LOG.debug(headerName+"-->"); 
             LOG.debug(request.getHeader(headerName)); 
         }
         LOG.debug("--------End Utils Header String Info--------");
     }
     /**
      * queryStringInfo(HttpServletRequest request)
      * Private method that prints out the contents of the request.  Used mainly for debugging.
      * 
      * @param request
      */
     @SuppressWarnings("unchecked")
     public static void queryStringInfo(HttpServletRequest request) {
         LOG.debug("--------Utils Query String Info--------");
         Enumeration<String> paramEnum = request.getParameterNames();
         
         while(paramEnum.hasMoreElements()) { 
             String postContent = (String) paramEnum.nextElement();
             LOG.debug(postContent+"-->"); 
             LOG.debug(request.getParameter(postContent));
         }
         LOG.debug("--------End Utils Query String Info--------");
     }
     
     /*
      * Single level Element nesting debugger
      */
     public static void printElementContents(Element element) {
         LOG.debug("--------Utils printElementContents--------");
         List children = (List)element.getChildren();
         for(int i=0;i<children.size();i++)
         {
             LOG.debug("Element: " + i + " " + children.get(i));
         }
         LOG.debug("--------End Utils printElementContents--------");
     }
     
     
 }
