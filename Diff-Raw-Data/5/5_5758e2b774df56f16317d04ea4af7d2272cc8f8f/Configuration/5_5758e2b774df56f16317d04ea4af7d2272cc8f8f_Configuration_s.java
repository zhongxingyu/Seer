 /*
  * Copyright 2013 Qubell, Inc.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package com.qubell.jenkinsci.plugins.qubell;
 
 import hudson.Extension;
 import hudson.util.FormValidation;
 import jenkins.model.GlobalConfiguration;
 import net.sf.json.JSONObject;
 import org.kohsuke.stapler.QueryParameter;
 import org.kohsuke.stapler.StaplerRequest;
 
 import javax.servlet.ServletException;
 import java.io.IOException;
 
 /**
  * The plugin configuration holder, see {@link GlobalConfiguration} for details
  * @author Alex Krupnov
  */
 @Extension
 public class Configuration extends GlobalConfiguration {
     private String url;
     private String login;
     private String password;
     private int statusPollingInterval;
     private boolean skipCertificateChecks;
     private boolean enableMessageLogging = false;
 
     private static final int DEFAULT_POLLING_INTERVAL = 5;
 
 
     /**
      * @return the Jenkins managed singleton for the configuration object
      */
     public static Configuration get() {
         return GlobalConfiguration.all().get(Configuration.class);
     }
 
     public Configuration() {

     }
 
 
     public Configuration(String url, String login, String password, boolean skipCertificateChecks, boolean enableMessageLogging) {
         this.url = url;
         this.login = login;
         this.password = password;
         this.skipCertificateChecks = skipCertificateChecks;
         this.enableMessageLogging = enableMessageLogging;
     }
 
     /**
      * Loads configuration values from frm data
      * @param req current request
      * @param formData form data
      * @return true when successfull
      * @throws FormException
      */
     @Override
     public boolean configure(StaplerRequest req, JSONObject formData) throws FormException {
         // To persist global configuration information,
         // set that to properties and call save().
         login = formData.getString("login");
         password = formData.getString("password");
         url = formData.getString("url");
         statusPollingInterval = formData.getInt("statusPollingInterval");
         skipCertificateChecks = formData.getBoolean("skipCertificateChecks");
         // ^Can also use req.bindJSON(this, formData);
         //  (easier when there are many fields; need set* methods for this, like setUseFrench)
         save();
        return super.configure(req, formData);
     }
 
     /**
      * Qubell api login
      * @return string for email
      */
     public String getLogin() {
         return login;
     }
 
     /**
      * If set to true, authority and other parameters of SSL certificate won't be checked
      * @return true when check should be skipped
      */
     public boolean isSkipCertificateChecks() {
         return skipCertificateChecks;
     }
 
     /**
      * Qubell user password
      * @return string for password, not exposed to UI via native Jenkins password box
      */
     public String getPassword() {
         return password;
     }
 
     /**
      * Base URL for Qubell instance
      * @return url value
      */
     public String getUrl() {
         return url;
     }
 
     /**
      * Polling interval for checking the instance status
      * @return value in seconds
      */
     public int getStatusPollingInterval() {
         //Since int has default value, we can't use jelly's default for int fields, adding default on java code level
         return statusPollingInterval != 0 ? statusPollingInterval : DEFAULT_POLLING_INTERVAL;
     }
 
     /**
      * When true, in and out messages are appended to CXF specific logger
      * @return true when extra logging enabled, false otherwise
      */
     public boolean isEnableMessageLogging() {
         return enableMessageLogging;
     }
 
     /**
      * Validates polling interval value: integer, greater then zero
      *
      * @param value string value of polling interval passed from configuration form
      * @return jenkins validation container, see {@link hudson.util.FormValidation}
      * @throws java.io.IOException
      * @throws javax.servlet.ServletException
      */
     public FormValidation doCheckStatusPollingInterval(@QueryParameter String value)
             throws IOException, ServletException {
 
         try {
             int timeout = Integer.parseInt(value);
             if (timeout <= 0) {
                 return FormValidation.error("Polling interval must be a positive integer value");
             }
         } catch (NumberFormatException nfe) {
             return FormValidation.error("Polling interval must be a positive integer value");
         }
 
         return FormValidation.ok();
     }
 
 }
