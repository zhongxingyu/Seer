 /*
  * OpenRemote, the Home of the Digital Home.
  * Copyright 2008-2012, OpenRemote Inc.
  *
  * See the contributors.txt file in the distribution for a
  * full listing of individual contributors.
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Affero General Public License as
  * published by the Free Software Foundation, either version 3 of the
  * License, or (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  * GNU Affero General Public License for more details.
  *
  * You should have received a copy of the GNU Affero General Public License
  * along with this program. If not, see <http://www.gnu.org/licenses/>.
  */
 package org.openremote.controller.servlet;
 
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Properties;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 
 import org.apache.commons.codec.binary.Base64;
 import org.apache.http.HttpResponse;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.params.BasicHttpParams;
 import org.apache.http.params.DefaultedHttpParams;
 import org.apache.http.params.HttpConnectionParams;
 import org.apache.http.params.HttpParams;
 import org.apache.log4j.Logger;
 import org.openremote.controller.Constants;
 import org.openremote.controller.ControllerConfiguration;
 import org.openremote.controller.service.ConfigurationService;
 import org.openremote.controller.service.ControllerXMLChangeService;
 import org.openremote.controller.service.FileService;
 import org.openremote.controller.spring.SpringContext;
 import org.openremote.controller.utils.AuthenticationUtil;
 import org.openremote.controller.utils.FreemarkerUtil;
 import org.openremote.controller.utils.AlgorithmUtil;
 import org.openremote.controller.utils.PathUtil;
 import org.springframework.security.providers.encoding.Md5PasswordEncoder;
 
 import freemarker.template.TemplateException;
 
 /**
  * This servlet is used to authenticate an administrator and set a session if successful
  * 
  * @author <a href="mailto:vincent.kriek@tass.nl">Vincent Kriek</a>
  * 
  */
 @SuppressWarnings("serial")
 public class LoginServlet extends HttpServlet
 {
 
    /**
    * Common log category for HTTP
    */
   private final static Logger logger = Logger.getLogger(Constants.LOGIN_SERVLET_LOG_CATEGORY);
   private final static String DATABASE_SALT_NAME = "salt";
   private final static String PEPPER_PROPERTY_VALUE = "java.class.path";
   private static String FIRST_TIME_SYNC = "first_time_sync";
   private static String SALT_STRING = "cbb9e9c64deaa65656f77aed39f8903c92cd2a4595cae16ddcfe9176567990d9b015376a0f832c1099b5b31630e4e76f151b6bf5021e3989d523ed93dc9510c5";
   private static Properties properties = System.getProperties();
   
   private ControllerXMLChangeService controllerXMLChangeService = (ControllerXMLChangeService) SpringContext
         .getInstance().getBean("controllerXMLChangeService");
   
   private ConfigurationService configurationService = (ConfigurationService) SpringContext
         .getInstance().getBean("configurationService");
   
   private ControllerConfiguration configuration = ControllerConfiguration.readXML();
   private FileService fileService =  (FileService) SpringContext
         .getInstance().getBean("fileService");
 
   private static final int timeout = 10000;
 
   /**
    * Check username and password against the online interface
    * @param username The OpenRemote Composer username
    * @param password The OpenRemote Composer password
    * @return 0 if valid, -1 if not yet synced, -2 if invalid, -3 if different user
    */
   public int checkOnline(String username, String password)
   {
      String databaseuser = configurationService.getItem("composer_username");
      String databasepassword = configurationService.getItem("composer_password");
 
 
      if(!databaseuser.equals("") && !username.equals(databaseuser)) {
         return -3;
      }
 
      HttpParams params = new BasicHttpParams();
 
      HttpConnectionParams.setConnectionTimeout(params, timeout);
      HttpConnectionParams.setSoTimeout(params, timeout);
      
      HttpClient httpClient = new DefaultHttpClient(params);
 
      
      HttpGet httpGet = new HttpGet(PathUtil.addSlashSuffix(configuration.getBeehiveRESTRootUrl()) + "user/" + username
            + "/openremote.zip");
 
      httpGet.setHeader(Constants.HTTP_AUTHORIZATION_HEADER, Constants.HTTP_BASIC_AUTHORIZATION
            + encode(username, password));
 
      boolean success = false;
      try {
         HttpResponse resp= httpClient.execute(httpGet);
         int statuscode = resp.getStatusLine().getStatusCode();
         if (200 == statuscode) {
            
            if(!configurationService.getBooleanItem(FIRST_TIME_SYNC))
            {
               if(databaseuser == null || databaseuser.equals("")) {
                  success = fileService.writeZipAndUnzip(resp.getEntity().getContent());
                  if(success)
                  {
                     controllerXMLChangeService.refreshController();
                  }
               }
            }
            
            if(!configurationService.getItem("composer_password").equals(this.getHashedPassword(password)))
            {
               this.generateSalt();
               saveCredentials(username, password);
            }
            
            return 0;
         } else if(401 == statuscode) {
            return -2;
         } else {
            logger.error("Login checking failed with HTTP Code " + statuscode);
         }
         
      } catch (IOException e) {
         logger.error(e.getMessage());
      }
           
      if(databaseuser.equals("") && databasepassword.equals("")) {
         saveCredentials(username, password);
         return 0;
      } else if(username.equals(username) && this.getHashedPassword(password).equals(databasepassword)) {
         return 0;
      } else {
         return -2;   
      }
      
   }
   
   private String getHashedPassword(String password)
   {
      return AlgorithmUtil.generateSHA512((password + this.getSalt() + this.getPepper()).getBytes());
   }
   
   /**
    * Save the username in the configurationtable
    * @param username The username you want to save
    */
   private void saveCredentials(String username, String password) {
      configurationService.updateItem("composer_username", username);
      
      configurationService.updateItem("composer_password", this.getHashedPassword(password));
   }
   
   /**
    * Generate a hash used in the session variable
    * @param username The admin username
    * @param password The admin password
    * @return The hash
    */
   private String getSessionHash(String username, String password) {
      String timestamp = Long.toString(System.currentTimeMillis());
      
      configurationService.updateItem("session_timestamp", timestamp);
      
      String sha512 = AlgorithmUtil.generateSHA512((username + timestamp + this.getHashedPassword(password)).getBytes());
      
      return sha512;
   }
   
   /**
    * Encode username and password for sending to the Beehive rest controller
    * @param username The OpenRemote Composer username
    * @param password The OpenRemote Composer password
    * @return String to send to the Beehive rest model
    */
   private String encode(String username, String password) {
      Md5PasswordEncoder encoder = new Md5PasswordEncoder();
      String encodedPwd = encoder.encodePassword(password, username);
      if (username == null || encodedPwd == null) {
         return null;
      }
      return new String(Base64.encodeBase64((username + ":" + encodedPwd).getBytes()));
   }
   
 
   /**
    * Get a pepper value for generating a hash
    * @return Salt String
    */
   private String getPepper()
   {
      return properties.getProperty(PEPPER_PROPERTY_VALUE, SALT_STRING);     
   }
   
   private String getSalt()
   {
      return configurationService.getItem(DATABASE_SALT_NAME);
   }
   
   private void generateSalt()
   {
      configurationService.updateItem(DATABASE_SALT_NAME, AlgorithmUtil.getSalt());
   }
   
   /**
    * Handle login request, redirect to administrator if successful, show login age if not
    * @param request the request from HTTP servlet
    * @param reponse Response that will be sent to the client
    */  
   @Override 
   protected void doPost(HttpServletRequest request, HttpServletResponse response)
                                                               throws ServletException, IOException
   {
      String username = request.getParameter("username");
      String password = request.getParameter("password");
      HttpSession session = request.getSession(true);
      PrintWriter out = response.getWriter();
      Object auth = session.getAttribute("authenticated");
         
     if(auth != null) {
        out.write(auth.toString());
     }
      
      int ret = checkOnline(username, password);
      if(ret == 0) {
         session.setAttribute(AuthenticationUtil.AUTH_SESSION, getSessionHash(username, password));
         
         response.sendRedirect("/controller/administrator");
      } else if(ret == -1) {
         returnLoginPage(response, "Controller did not yet sync with the composer");
      } else {
         returnLoginPage(response, "Wrong login credentials");
      }
   }
   
   /**
    * Return the login page
    * @param request the request from HTTP servlet
    * @param reponse Response that will be sent to the client
    */  
   @Override 
   protected void doGet(HttpServletRequest request, HttpServletResponse response)
                                                               throws ServletException, IOException
   {
      returnLoginPage(response, "");
   }
   
   /**
    * Load the login page template and insert a possible error message
    * @param response Response that will be sent to the client
    * @param error Error message to be shown in login panel
    * @throws IOException
    */
   private void returnLoginPage(HttpServletResponse response, String error) throws IOException
   {
      PrintWriter out = response.getWriter();
      Map<String, String> data = new HashMap<String, String>();
      data.put("errorMessage", error);
      
      try {
       out.write(FreemarkerUtil.freemarkerDo(data, "login.ftl"));
    } catch (TemplateException e) {
       logger.error(e.getMessage());
    }
   }
 }
