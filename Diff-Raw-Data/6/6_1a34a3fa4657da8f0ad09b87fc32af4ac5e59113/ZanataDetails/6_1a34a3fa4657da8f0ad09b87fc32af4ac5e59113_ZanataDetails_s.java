 package org.jboss.pressgang.ccms.zanata;
 
 /**
  * A utility class to pull out the Zanata details from the system properties
  */
 public class ZanataDetails {
     private String server;
     private String project;
     private String version;
     private String username;
     private String token;
 
     public ZanataDetails() {
         this.server = System.getProperty(ZanataConstants.ZANATA_SERVER_PROPERTY);
         this.project = System.getProperty(ZanataConstants.ZANATA_PROJECT_PROPERTY);
         this.version = System.getProperty(ZanataConstants.ZANATA_PROJECT_VERSION_PROPERTY);
         this.username = System.getProperty(ZanataConstants.ZANATA_USERNAME_PROPERTY);
         this.token = System.getProperty(ZanataConstants.ZANATA_TOKEN_PROPERTY);
     }
 
     public ZanataDetails(final ZanataDetails zanataDetails) {
         this.server = zanataDetails.server;
         this.project = zanataDetails.project;
         this.version = zanataDetails.version;
         this.username = zanataDetails.username;
         this.token = zanataDetails.token;
     }
 
     public String getServer() {
         return server;
     }
 
     public void setServer(String server) {
         this.server = server;
     }
 
     public String getProject() {
         return project;
     }
 
     public void setProject(String project) {
         this.project = project;
     }
 
     public String getVersion() {
         return version;
     }
 
     public void setVersion(String version) {
         this.version = version;
     }
 
     public String getUsername() {
         return username;
     }
 
     public void setUsername(String username) {
         this.username = username;
     }
 
     public String getToken() {
         return token;
     }
 
     public void setToken(String token) {
         this.token = token;
     }
 
     public String returnUrl() {
        return server + (server.endsWith("/") ? "" : "/") + "seam/resource/restv1/projects/p/" + project + "/iterations/i/" + version + "/r";
     }
 }
