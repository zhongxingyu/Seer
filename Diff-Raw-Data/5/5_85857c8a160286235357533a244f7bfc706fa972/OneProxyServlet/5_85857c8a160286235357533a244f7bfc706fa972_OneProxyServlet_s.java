 /*
  Created as part of the StratusLab project (http://stratuslab.eu),
  co-funded by the European Commission under the Grant Agreement
  INSFO-RI-261552.
 
  Copyright (c) 2010, Centre Nationale de la Recherche Scientifique
 
  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at
 
  http://www.apache.org/licenses/LICENSE-2.0
 
  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
  */
 
 package eu.stratuslab.authn;
 
 import java.io.UnsupportedEncodingException;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.net.URLEncoder;
 import java.security.MessageDigest;
 import java.security.NoSuchAlgorithmException;
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.List;
 import java.util.Vector;
 import java.util.logging.ConsoleHandler;
 import java.util.logging.Formatter;
 import java.util.logging.Handler;
 import java.util.logging.LogRecord;
 import java.util.logging.Logger;
 
 import javax.servlet.ServletConfig;
 import javax.servlet.ServletException;
 
 import org.apache.xmlrpc.XmlRpcException;
 import org.apache.xmlrpc.XmlRpcHandler;
 import org.apache.xmlrpc.XmlRpcRequest;
 import org.apache.xmlrpc.XmlRpcRequestConfig;
 import org.apache.xmlrpc.client.XmlRpcClient;
 import org.apache.xmlrpc.client.XmlRpcClientConfigImpl;
 import org.apache.xmlrpc.common.XmlRpcHttpRequestConfigImpl;
 import org.apache.xmlrpc.common.XmlRpcNotAuthorizedException;
 import org.apache.xmlrpc.server.XmlRpcHandlerMapping;
 import org.apache.xmlrpc.webserver.XmlRpcServlet;
 import org.apache.xmlrpc.webserver.XmlRpcServletServer;
 
 @SuppressWarnings("serial")
 public class OneProxyServlet extends XmlRpcServlet {
 
     final private static String PROXY_URL_PARAM_NAME = "oneProxyUrl";
     final private static String DEFAULT_PROXY_URL = "http://localhost:2633/RPC2";
 
     private URL proxyUrl = null;
 
     final private static Logger LOGGER;
     static {
         LOGGER = Logger.getLogger(OneProxyServlet.class.getCanonicalName());
         for (Handler h : LOGGER.getHandlers()) {
             LOGGER.removeHandler(h);
         }
 
         Handler handler = new ConsoleHandler();
         handler.setFormatter(new ShortMsgFormatter());
         LOGGER.addHandler(handler);
 
         LOGGER.setUseParentHandlers(false);
     }
 
     @Override
     public void init(ServletConfig pConfig) throws ServletException {
         super.init(pConfig);
 
         proxyUrl = extractProxyUrl(pConfig);
     }
 
     @Override
     public XmlRpcHandlerMapping newXmlRpcHandlerMapping()
             throws XmlRpcException {
 
         return new ProxyHandlerMapping();
     }
 
     @Override
     public XmlRpcServletServer newXmlRpcServer(ServletConfig pConfig) {
 
         return new OneProxyServletServer();
     }
 
     private class ProxyHandlerMapping implements XmlRpcHandlerMapping {
 
         public XmlRpcHandler getHandler(String handlerName) {
             return new ProxyHandler(proxyUrl);
         }
     }
 
     private URL extractProxyUrl(ServletConfig pConfig) throws ServletException {
 
         String url = pConfig.getInitParameter(PROXY_URL_PARAM_NAME);
         if (url == null) {
             url = DEFAULT_PROXY_URL;
         }
 
         try {
             return new URL(url);
         } catch (MalformedURLException e) {
             throw new ServletException("Proxy URL is malformed: " + url);
         }
     }
 
     private static class ProxyHandler implements XmlRpcHandler {
 
         final private URL proxyUrl;
 
         public ProxyHandler(URL proxyUrl) {
             this.proxyUrl = proxyUrl;
         }
 
         public Object execute(XmlRpcRequest request) throws XmlRpcException {
 
             List<Object> params = prepareRequestParameters(request);
 
             return executeProxyRequest(request.getMethodName(), params);
         }
 
         private Object executeProxyRequest(String methodName,
                 List<Object> params) throws XmlRpcException {
 
             XmlRpcClientConfigImpl config = new XmlRpcClientConfigImpl();
             config.setServerURL(proxyUrl);
 
             XmlRpcClient client = new XmlRpcClient();
             client.setConfig(config);
 
             return client.execute(methodName, params);
         }
 
         private List<Object> prepareRequestParameters(XmlRpcRequest request)
                 throws XmlRpcException {
 
             Vector<Object> params = new Vector<Object>();
 
             // Replace the authentication information.
             String authInfo = extractAuthnInfo(request);
             params.addElement(authInfo);
 
             // Log this request.
             LOGGER.info("forwarding request from " + authInfo);
 
             // Copy all remaining parameters.
             for (int i = 1; i < request.getParameterCount(); i++) {
                 params.addElement(request.getParameter(i));
             }
 
             return params;
         }
 
         private static String stripCNProxy(String username) {
             return username.replaceFirst("^CN\\s*=\\s*proxy\\s*,\\s*", "");
         }
 
         private String extractAuthnInfo(XmlRpcRequest request)
                 throws XmlRpcException {
 
             XmlRpcRequestConfig config = request.getConfig();
 
             String user = "";
             String basicPswdHash = "";
             String defaultPswdHash = "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa";
 
             if (config instanceof OneProxyRequestConfigImpl) {
                 OneProxyRequestConfigImpl opconfig = (OneProxyRequestConfigImpl) config;
                 user = opconfig.getUserDn();
             }
 
             if ("".equals(user)
                     && config instanceof XmlRpcHttpRequestConfigImpl) {
                 XmlRpcHttpRequestConfigImpl hconfig = (XmlRpcHttpRequestConfigImpl) config;
                 user = hconfig.getBasicUserName();
                 basicPswdHash = hashPassword(hconfig.getBasicPassword());
             }
 
             if (!"".equals(user)) {
 
                 // Get rid of the proxy part of the DN.
                 user = stripCNProxy(user);
 
                 try {
 
                     // Pass the hash of the password through if the username is
                     // 'oneadmin'.
                     String credentials = ("oneadmin".equals(user)) ? basicPswdHash
                             : defaultPswdHash;
 
                     // All of the usernames must be URL encoded to remove spaces
                     // and other special characters.
                     return URLEncoder.encode(user, "UTF-8") + ":" + credentials;
 
                 } catch (UnsupportedEncodingException e) {
                     LOGGER
                             .severe("can't create UTF-8 encoding for URL encoding");
                     throw new XmlRpcException("internal server error");
                 }
             } else {
                 throw new XmlRpcNotAuthorizedException(
                         "certificate DN or username not provided");
             }
         }
     }
 
     private static String hashPassword(String password) throws XmlRpcException {
 
         try {
             MessageDigest md = MessageDigest.getInstance("SHA-1");
             md.update((password != null) ? password.getBytes() : new byte[] {});
            return md.toString();
         } catch (NoSuchAlgorithmException e) {
             LOGGER.severe("can't create UTF-8 encoding for URL encoding");
             throw new XmlRpcException("internal server error");
         }
 
     }
 
     // TODO: Pull into separate class.
     private static class ShortMsgFormatter extends Formatter {
 
         public String format(LogRecord record) {
             StringBuilder sb = new StringBuilder();
 
             Date date = new Date(record.getMillis());
             DateFormat dateFormat = new SimpleDateFormat(
                     "yyyy-MM-dd HH:mm:ss.SSS");
             sb.append(dateFormat.format(date));
             sb.append(":");
 
             sb.append(record.getLevel().getName());
             sb.append("::");
 
             sb.append(record.getMessage());
             sb.append("\n");
 
             return sb.toString();
         }
     }
 
 }
