 package com.atex.confluence.plugin.nexus;
 
 import com.atlassian.extras.common.log.Logger;
 import com.atlassian.sal.api.auth.LoginUriProvider;
 import com.atlassian.sal.api.pluginsettings.PluginSettings;
 import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;
 import com.atlassian.sal.api.transaction.TransactionCallback;
 import com.atlassian.sal.api.transaction.TransactionTemplate;
 import com.atlassian.sal.api.user.UserManager;
 import com.atlassian.templaterenderer.TemplateRenderer;
 
 import electric.util.log.Log;
 
 import java.io.IOException;
 import java.net.MalformedURLException;
 import java.net.URI;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Map;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import static com.atex.confluence.plugin.nexus.Configuration.*;
 
 /**
  *
  * @author pau
  */
 public class ConfigurationServlet extends HttpServlet {
 
 
     private static final String CONTENT_TYPE = "text/html;charset=utf-8";
     private static final String VIEW = "configure.vm";
     
     private final UserManager userManager;
     private final LoginUriProvider loginUriProvider;
     private final TemplateRenderer renderer;
     private final PluginSettingsFactory pluginSettingsFactory;
     private final TransactionTemplate transactionTemplate;
 
     public ConfigurationServlet(TemplateRenderer renderer, PluginSettingsFactory pluginSettingsFactory, TransactionTemplate transactionTemplate, UserManager userManager, LoginUriProvider loginUriProvider) {
         this.renderer = renderer;
         this.pluginSettingsFactory = pluginSettingsFactory;
         this.transactionTemplate = transactionTemplate;
         this.userManager = userManager;
         this.loginUriProvider = loginUriProvider;
     }
 
     @Override
     protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
         String username = userManager.getRemoteUsername(req);
         if (username != null && !userManager.isSystemAdmin(username)) {
            redirectToMain(req, resp);
             return;
         } else if (username==null) {
             redirectToLogin(req, resp);
             return;            
         }
         Configuration configuration = transactionTemplate.execute(new ConfigurationReader(pluginSettingsFactory));
         Map<String, Object> models = new HashMap<String, Object>();
         models.put("configuration", configuration);
         resp.setContentType(CONTENT_TYPE);
         renderer.render(VIEW, models, resp.getWriter());
     }
 
     @Override
     protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
         final String urlString = (String) req.getParameter("url");
         final String username = (String) req.getParameter("username");
         final String password = (String) req.getParameter("password");
         final String groupId = (String) req.getParameter("groupId");
         Map<String, Object> models = new HashMap<String, Object>();
         if (!isValid(urlString)) {
             models.put("urlString", "URL String is required");
         }
         if (!isValid(username)) {
             models.put("username", "Username is required");
         }
         if (!isValid(password)) {
             models.put("password", "Password is required");
         }
         if (!isValid(groupId)) {
             models.put("groupId", "Default Group ID is required");
         }
 
         Configuration configuration = null;
         try {
             configuration = new Configuration(urlString, username, password, groupId);
         } catch (MalformedURLException e) {
             models.put("urlString", "Invalid URL format");
         }
         // some error occured
         if (!models.isEmpty()) {
             resp.setContentType(CONTENT_TYPE);
             renderer.render(VIEW, models, resp.getWriter());
         } else {
 
             // do save operation
             transactionTemplate.execute(new TransactionCallback<Void>() {
 
                 @Override
                 public Void doInTransaction() {
                     PluginSettings pluginSettings = pluginSettingsFactory.createGlobalSettings();
                     pluginSettings.put(URL, urlString);
                     pluginSettings.put(USERNAME, username);
                     pluginSettings.put(PASSWORD, password);
                     pluginSettings.put(GROUPID, groupId);
                     return null;
                 }
             });
             // update to use new configuration
             MetadataManager.setConfiguration(configuration);
             models.put("configuration", configuration);
             models.put("success", "success");
             resp.setContentType(CONTENT_TYPE);
             renderer.render(VIEW, models, resp.getWriter());
         }
     }
     
     private boolean isValid(String value) {
         if (value == null || value.trim().isEmpty()) {
             return false;
         } else {
             return true;
         }
     }
     
    private void redirectToMain(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.sendRedirect(loginUriProvider.getLoginUri(URI.create("")).toASCIIString());
    }
   
     private void redirectToLogin(HttpServletRequest req, HttpServletResponse resp) throws IOException {
         resp.sendRedirect(loginUriProvider.getLoginUri(getUri(req)).toASCIIString());
     }
 
     private URI getUri(HttpServletRequest req) {
         StringBuffer builder = req.getRequestURL();
         if (req.getQueryString() != null) {
             builder.append("?");
             builder.append(req.getQueryString());
         }
         return URI.create(builder.toString());
     }
     
 }
