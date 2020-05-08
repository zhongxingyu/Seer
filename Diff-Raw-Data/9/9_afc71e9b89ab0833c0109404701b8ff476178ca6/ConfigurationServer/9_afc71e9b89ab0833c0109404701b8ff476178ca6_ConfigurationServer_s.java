 /*
  * Copyright 2004-2013 ICEsoft Technologies Canada Corp.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the
  * License. You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing,
  * software distributed under the License is distributed on an "AS
  * IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
  * express or implied. See the License for the specific language
  * governing permissions and limitations under the License.
  *
  */
 package org.icepush;
 
 import org.icepush.http.Request;
 import org.icepush.http.ResponseHandler;
 import org.icepush.http.Server;
 import org.icepush.http.standard.Cookie;
 import org.icepush.http.standard.FixedXMLContentHandler;
 
 import javax.servlet.ServletContext;
 import java.io.IOException;
 import java.io.Writer;
 import java.util.logging.Logger;
 
 public class ConfigurationServer implements Server {
     private static final Logger log = Logger.getLogger(ConfigurationServer.class.getName());
     private static final String defaultServerErrorRetries = "1000 2000 4000";
     private static final int defaultEmptyResponseRetries = 3;
    private static final int defaultBlockingConnectionTimeout = 10000;
     private static final String BrowserIDCookieName = "ice.push.browser";
 
     private Server blockingConnectionServer;
     private boolean nonDefaultConfiguration;
 
     private FixedXMLContentHandler configureBridge;
     private FixedXMLContentHandler setBrowserID;
     private ResponseHandler setBrowserIDAndConfigureBridgeMacro;
     private boolean redirect;
 
     public ConfigurationServer(final PushContext context, final ServletContext servletContext, Configuration configuration, final Server server) {
         blockingConnectionServer = server;
         String contextPath = normalizeContextPath(configuration.getAttribute("contextPath", (String) servletContext.getAttribute("contextPath")));
         //PUSH-218: temporarily disabling modification of the context parameter
         long heartbeatTimeout = configuration.getAttributeAsLong("heartbeatTimeout", defaultBlockingConnectionTimeout);
        if(heartbeatTimeout != defaultBlockingConnectionTimeout){
            heartbeatTimeout = defaultBlockingConnectionTimeout;
        }
         String serverErrorRetries = configuration.getAttribute("serverErrorRetryTimeouts", defaultServerErrorRetries);
         int emptyResponseRetries = configuration.getAttributeAsInteger("emptyResponseRetries", defaultEmptyResponseRetries);
 
         String configurationMessage = "<configuration" +
                 (heartbeatTimeout != defaultBlockingConnectionTimeout ?
                         " heartbeatTimeout=\"" + heartbeatTimeout + "\"" : "") +
                 (emptyResponseRetries != defaultEmptyResponseRetries ?
                         " emptyResponseRetries=\"" + emptyResponseRetries + "\"" : "") +
                 (!serverErrorRetries.equals(defaultServerErrorRetries) ?
                         " serverErrorRetryTimeouts=\"" + serverErrorRetries + "\"" : "") +
                 (contextPath != null ?
                         " blockingConnectionURI=\"" + contextPath + "/listen.icepush\"" : "") +
                 (contextPath != null ?
                         " contextPath=\"" + contextPath + "\"" : "") +
                 "/>";
         //always redirect if the request comes to this context path
        redirect = context != null && !servletContext.getContextPath().equals(contextPath);
         nonDefaultConfiguration = configurationMessage.length() != "<configuration/>".length();
         configureBridge = new ConfigureBridge(configurationMessage);
         setBrowserID = new SetBrowserID(context);
         setBrowserIDAndConfigureBridgeMacro = new SetBrowserIDAndConfigureBridgeMacro();
     }
 
     private static String normalizeContextPath(String path) {
         if (path == null) {
             return null;
         } else {
             return path.startsWith("/") ? path : "/" + path;
         }
     }
 
     public void service(Request request) throws Exception {
         Cookie browserIDCookie = Cookie.readCookie(request, BrowserIDCookieName);
         if (redirect || request.containsParameter("ice.sendConfiguration")) {
             boolean browserIDNotSet = browserIDCookie == null;
 
             if (nonDefaultConfiguration && browserIDNotSet) {
                 request.respondWith(setBrowserIDAndConfigureBridgeMacro);
             } else if (nonDefaultConfiguration) {
                 request.respondWith(configureBridge);
             } else if (browserIDNotSet) {
                 request.respondWith(setBrowserID);
             } else {
                 blockingConnectionServer.service(request);
             }
         } else {
             blockingConnectionServer.service(request);
         }
     }
 
     public void shutdown() {
         blockingConnectionServer.shutdown();
     }
 
     private static class ConfigureBridge extends FixedXMLContentHandler {
         private String configurationMessage;
 
         private ConfigureBridge(String configurationMessage) {
             this.configurationMessage = configurationMessage;
         }
 
         public void writeTo(Writer writer) throws IOException {
             writer.write(configurationMessage);
             log.fine("Re-configured bridge.");
         }
     }
 
     private static class SetBrowserID extends FixedXMLContentHandler {
         private PushContext context;
 
         public SetBrowserID(PushContext context) {
             this.context = context;
         }
 
         public void writeTo(Writer writer) throws IOException {
             writer.write("<browser id=\"" + context.generateBrowserID() + "\"/>");
             log.fine("BrowserID set through blocking connection.");
         }
     }
 
     private class SetBrowserIDAndConfigureBridgeMacro extends FixedXMLContentHandler {
         public void writeTo(Writer writer) throws IOException {
             writer.write("<macro>");
             configureBridge.writeTo(writer);
             setBrowserID.writeTo(writer);
             writer.write("</macro>");
         }
     }
 }
