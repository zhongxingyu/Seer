 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 package com.preppa.web.components;
 
 import com.preppa.web.entities.User;
 import javax.servlet.http.HttpServletRequest;
 import nu.localhost.tapestry5.springsecurity.services.LogoutService;
 import org.apache.tapestry5.annotations.ApplicationState;
 import org.apache.tapestry5.annotations.IncludeJavaScriptLibrary;
 import org.apache.tapestry5.annotations.IncludeStylesheet;
 import org.apache.tapestry5.ioc.annotations.Inject;
 import org.apache.tapestry5.ioc.annotations.Value;
 import org.apache.tapestry5.services.Request;
 /**
  *
  * @author newtonik
  */
 @IncludeStylesheet(value = {"context:styles/site.css"})
 @IncludeJavaScriptLibrary(value = {"context:js/jquery-1.3.2.js", "context:js/jquery.tools.min.js", "context:js/navbar.js"})
 public class NavBar {
     @Inject
     @Value("${spring-security.check.url}")
     private String checkUrl;
 
 //    @Parameter(defaultPrefix="literal", required=true)
 //    private String pages;
 //
 //    @Inject
 //    private ComponentResources resources;
 //
 //    @Property
 //    private String pageName;
 //
 //    public String[] getPageNames()
 //    {
 //        return pages.split(",");
 //    }
 //
 //    public String getTabClass()
 //    {
 //        if (pageName.equalsIgnoreCase(resources.getPageName()))
 //            return "current";
 //
 //        return null;
 //    }
  @Inject
  @Value("${spring-security.logout}")
  private String logoutUrl;
     @Inject
     private Request request;
     @Inject
     private LogoutService logout;
   @ApplicationState
     private User user;
   private boolean userExists;
   @Inject
   private HttpServletRequest _request;
     /**
      * @return the logoutUrl
      */
     public String getLogoutUrl() {
         return request.getContextPath() + logoutUrl;
     }
 
     void onActionFromLogoutUrl() {
         logout.logout();
       
     }
 
     /**
      * @return the user
      */
     public User getUser() {
         return user;
     }
    public boolean getUserExists()
     {
 
         return userExists;
     }
 
     /**
      * @return the checkUrl
      */
     public String getCheckUrl() {
          return request.getContextPath() + checkUrl;
     }
 
     public String getpath() {
         String url = _request.getRequestURL().toString();
         return url.substring(url.length() - 8, url.length());
     }
 
     public boolean getHome() {
         String url = _request.getRequestURL().toString();
         String check = url.substring(url.length() - 8, url.length());
         return check.contains("/preppa/") || check.contains("/preppa");
     }
 
     public boolean getProfile() {
         String url = _request.getRequestURL().toString();
         return url.contains("/user/") || url.contains("loginpage");
     }
 
     public boolean getOpenQuestion() {
         String url = _request.getRequestURL().toString();
         return url.contains("openquestion");
     }
 
     public boolean getLearning() {
         String url = _request.getRequestURL().toString();
         return url.contains("learning");
     }
 }
