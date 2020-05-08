 /*
  * Copyright 2005 John R. Fallows
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
 package net.java.dev.weblets.impl;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.URL;
 import java.text.Format;
 import java.util.Enumeration;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.LinkedHashMap;
 import java.util.Map;
 import java.util.Set;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import javax.servlet.ServletContext;
 
 import net.java.dev.weblets.Weblet;
 import net.java.dev.weblets.WebletConfig;
 import net.java.dev.weblets.WebletContainer;
 import net.java.dev.weblets.WebletException;
 import net.java.dev.weblets.WebletRequest;
 import net.java.dev.weblets.WebletResponse;
 import net.java.dev.weblets.impl.misc.SandboxGuard;
 import net.java.dev.weblets.impl.parse.DisconnectedEntityResolver;
 import net.java.dev.weblets.impl.util.ConfigurationUtils;
 import net.java.dev.weblets.util.StringUtils;
 
 import org.apache.commons.digester.Digester;
 import org.apache.commons.digester.ObjectCreationFactory;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.xml.sax.Attributes;
 import org.xml.sax.SAXException;
 
 public class WebletContainerImpl extends WebletContainer {
 
     public WebletContainerImpl(
             ServletContext servletContext,
             String webletContextPath,
             Format webletURLFormat,
             Pattern webletURLPattern,
             boolean multipleConfigs) throws WebletException {
         _servletContext = servletContext;
         _webletURLFormat = webletURLFormat;
         _webletURLPattern = webletURLPattern;
         _webletContextPath = webletContextPath;
         checkWebletsContextPath();
         try {
             Set configs = new HashSet();
             //  Enumeration e = getConfigEnumeration("weblets-config.xml"); /*lets find the root configs first*/
             Set urls = new HashSet();     //      urls.add(element);
             if (multipleConfigs) {
                 ConfigurationUtils.getValidConfigFiles(Const.REL_META_INF, Const.WEBLETS_CONFIG_XML, configs);
                 try {
                     ConfigurationUtils.getValidConfigFiles(Const.REL_META_INF, Const.MANIFEST_MF, configs);
                 } catch (NullPointerException ex) {
                     Log log = LogFactory.getLog(this.getClass());
                     log.info("MANIFEST.MF search failed for configurations, if you use Websphere, then you can safely ignore this please use a weblets-config.xml as entry point for your weblets configuration, please !");
                 }
                 Iterator configNameIterator = configs.iterator();
                 // Defensive: Glassfish.v2.b25 produces duplicates in Enumeration
                 //            returned by loader.getResources()
                 while (configNameIterator.hasNext()) {
                     Enumeration theUrlEnum = ConfigurationUtils.getConfigEnumeration(Const.REL_META_INF, (String) configNameIterator.next());
                     while (theUrlEnum.hasMoreElements()) {
                         URL resource = (URL) theUrlEnum.nextElement();
                         urls.add(resource);
                     }
                 }
             } else {
                 Enumeration theUrlEnum = ConfigurationUtils.getConfigEnumeration(Const.REL_META_INF, Const.WEBLETS_CONFIG_XML);
                 while (theUrlEnum.hasMoreElements()) {
                     URL resource = (URL) theUrlEnum.nextElement();
                     urls.add(resource);
                 }
             }
             Iterator urlIterator = urls.iterator();
             while (urlIterator.hasNext()) {
                 URL resource = (URL) urlIterator.next();
                 registerConfig(resource);
             }
             WebletContainer.setInstance(this);
         }
         catch (IOException e) {
             throw new WebletException(e);
         }
     }
 
     private void checkWebletsContextPath() {
         if (_webletContextPath == null || _webletContextPath.trim().equals("")) {
             //the setup context path defaults to empty lets determine another one if possible
             Log log = LogFactory.getLog(WebletContainerImpl.class);
             log.warn("No net.java.dev.weblets.contextpath context-param has been set" +
                      " this might cause problems in non jsf environments! ");
         } else {
             if (_webletContextPath.endsWith("/"))
                 _webletContextPath = _webletContextPath.substring(Const.FIRST_PARAM, _webletContextPath.length() - Const.SECOND_PARAM);
         }
     }
 
     public ServletContext getServletContext() {
         return _servletContext;
     }
 
     public void destroy() {
         Iterator i = _weblets.values().iterator();
         while (i.hasNext()) {
             Weblet weblet = (Weblet) i.next();
             weblet.destroy();
         }
         _weblets = null;
         _webletConfigs = null;
         _webletMappings = null;
         _webletContextPath = null;
     }
 
     public Pattern getPattern() {
         return _webletURLPattern;
     }
 
     public String[] parseWebletRequest(
             String contextPath,
             String requestURI,
             long ifModifiedSince) {
         Iterator i = _webletMappings.entrySet().iterator();
         while (i.hasNext()) {
             Map.Entry entry = (Map.Entry) i.next();
             Pattern pattern = (Pattern) entry.getValue();
             Matcher matcher = pattern.matcher(requestURI);
             if (matcher.matches()) {
                 String webletPath = matcher.group(Const.SECOND_PARAM);
                 String pathInfo = matcher.group(Const.TWO_PARAMS);
                 String webletName = (String) entry.getKey();
                 return new String[]{webletName, webletPath, pathInfo};
             }
         }
         return null;
     }
 
     /*setter and getter to access the context path*/
     public String getWebletContextPath() {
         return _webletContextPath;
     }
 
     public void setWebletContextPath(String contextPath) {
         _webletContextPath = contextPath;
     }
 
     public void service(
             WebletRequest request,
             WebletResponse response) throws IOException, WebletException {
         Weblet weblet = getWeblet(request);
         if (weblet == null) {
             Log log = LogFactory.getLog(getClass());
             log.error("Error weblet for: " + request.getWebletName() + ":" + request.getPathInfo() + " could not be found" +
                       " please check either your configuration or your request cause! ");
             throw new WebletException("Error weblet for: " + request.getWebletName() + ":" + request.getPathInfo() + " could not be found" +
                                       " please check either your configuration or your request cause! ");
         }
         String pathInfo = request.getPathInfo();
         if (response.getDefaultContentType() == null) {
             //enhanced security check
             if (pathInfo != null && SandboxGuard.isJailBreak(pathInfo)) {
                 throw new WebletException("Security Exception, the " + pathInfo +
                                           " breaks out of the resource jail, no resource is served!");
             }
             WebletConfig webConfig = weblet.getWebletConfig();
             if (pathInfo != null) {
                 String mimeType = webConfig.getMimeType(pathInfo);
                 if (mimeType != null) {
                     response.setDefaultContentType(mimeType);
                     response.setContentType(mimeType);
                 }
             }
         }
         WebletConfig webConfig = weblet.getWebletConfig();
         Set allowedResources = webConfig.getAllowedResources();
         if (allowedResources != null) {
             String filetype = StringUtils.getExtension(pathInfo);
             if (!allowedResources.contains(filetype.toLowerCase())) {
                 throw new WebletException("Security Exception, the " + pathInfo +
                                           "  resource cannot be served!");
                 /* not allowed no content delivered */
             }
         }
         weblet.service(request, response);
     }
 
     public Weblet getWeblet(WebletRequest request) {
         String webletName = request.getWebletName();
         return getWeblet(webletName);
     }
 
     public Weblet getWeblet(String webletName) {
         Weblet weblet = (Weblet) _weblets.get(webletName);
         if (weblet == null) {
             try {
                 WebletConfigImpl config = (WebletConfigImpl) _webletConfigs.get(webletName);
                 ClassLoader loader = Thread.currentThread().getContextClassLoader();
                 Class webletClass = loader.loadClass(config.getWebletClass());
                 weblet = (Weblet) webletClass.newInstance();
                 weblet.init(config);
                 _weblets.put(webletName, weblet);
             }
             catch (ClassNotFoundException e) {
                 throw new WebletException(e);
             }
             catch (InstantiationException e) {
                 throw new WebletException(e);
             }
             catch (IllegalAccessException e) {
                 throw new WebletException(e);
             }
         }
         return weblet;
     }
 
     /**
      * new since 1.1 we now can get the weblet also
      * as local stream for furhter external processing
      * if we have our weblet request.
      * <p/>
      * We can add a dummy request
      *
      * @param request
      * @return an input stream on our current weblet resource
      * @throws WebletException
      * @throws IOException
      */
     public InputStream serviceStream(WebletRequest request, String mimetype) throws WebletException, IOException {
         Weblet weblet = getWeblet(request);
         return weblet.serviceStream(request.getPathInfo(), mimetype);
     }
 
     public String getResourceUri(
             String webletName,
             String pathInfo) throws WebletException {
         WebletConfig config = (WebletConfig) _webletConfigs.get(webletName);
         if (config.hasSubbundles()) {
             String newPathInfo = config.getSubbundleIdFromResource(pathInfo);
             if (newPathInfo != null) {
                 pathInfo = newPathInfo;
             }
         }
         if (config == null)
             throw new WebletException("Missing Weblet configuration for '" + webletName + "'");
         String webletPath = (String) _webletPaths.get(webletName);
         if (webletPath == null)
             throw new WebletException("Missing Weblet mapping for '" + webletName + "'");
         String webletVersion = config.getWebletVersion();
         // URL-syntax  /webletPath[$version]/pathInfo
         StringBuffer buffer = new StringBuffer();
         buffer.append(webletPath);
         if (webletVersion != null) {
             buffer.append('$');
             buffer.append(webletVersion);
         }
         if (pathInfo != null) {
             buffer.append(pathInfo);
         }
         String webletURL = buffer.toString();
         if (_webletURLFormat != null)
             webletURL = _webletURLFormat.format(new Object[]{webletURL});
         return webletURL;
     }
 
     public InputStream getResourceStream(WebletRequest request, String mimeType) throws WebletException {
         Weblet weblet = (Weblet) _weblets.get(request.getWebletName());
         if (weblet == null)
             return null;
         try {
             return weblet.serviceStream(request.getPathInfo(), mimeType);
         } catch (IOException ex) {
             return null;
         }
     }
 
     public void registerConfig(
             URL webletsConfig) {
         try {
             InputStream in = webletsConfig.openStream();
             try {
                 Digester digester = new Digester();
                 digester.setValidating(false);
                 digester.setEntityResolver(DisconnectedEntityResolver.sharedInstance());
                 digester.push(this);
                 digester.addFactoryCreate(Const.WEBLETS_CONFIG + Const.PAR_WEBLET, WEBLET_CONFIG_FACTORY);
                 digester.addSetNext(Const.WEBLETS_CONFIG + Const.PAR_WEBLET, "addWeblet", WebletConfigImpl.class.getName());
                 digester.addCallMethod(Const.WEBLETS_CONFIG + Const.PAR_WEBLET + Const.PAR_WEBLET_NAME,
                                        "setWebletName", Const.FIRST_PARAM);
                 digester.addCallMethod(Const.WEBLETS_CONFIG + Const.PAR_WEBLET + Const.PAR_WEBLET_CLASS,
                                        "setWebletClass", Const.FIRST_PARAM);
                 digester.addCallMethod(Const.WEBLETS_CONFIG + Const.PAR_WEBLET + Const.PAR_WEBLET_VERSION,
                                        "setWebletVersion", Const.FIRST_PARAM);
                 digester.addCallMethod(Const.WEBLETS_CONFIG + Const.PAR_WEBLET + Const.PAR_INIT_PARAM,
                                        "addInitParam", Const.TWO_PARAMS);
                 digester.addCallParam(Const.WEBLETS_CONFIG + Const.PAR_WEBLET + Const.PAR_INIT_PARAM + Const.PAR_PARAM_NAME, Const.FIRST_PARAM);
                 digester.addCallParam(Const.WEBLETS_CONFIG + Const.PAR_WEBLET + Const.PAR_INIT_PARAM + Const.PAR_PARAM_VALUE, Const.SECOND_PARAM);
                 digester.addCallMethod(Const.WEBLETS_CONFIG + Const.PAR_WEBLET + Const.PAR_MIME_MAPPING,
                                        "addMimeMapping", Const.TWO_PARAMS);
                 digester.addCallParam(Const.WEBLETS_CONFIG + Const.PAR_WEBLET + Const.PAR_MIME_MAPPING + Const.PAR_EXTENSION, Const.FIRST_PARAM);
                 digester.addCallParam(Const.WEBLETS_CONFIG + Const.PAR_WEBLET + Const.PAR_MIME_MAPPING + Const.PAR_MIME_TYPE, Const.SECOND_PARAM);
                 digester.addCallMethod(Const.WEBLETS_CONFIG + Const.PAR_WEBLET_MAPPING,
                                        "setWebletMapping", Const.TWO_PARAMS);
                 digester.addCallParam(Const.WEBLETS_CONFIG + Const.PAR_WEBLET_MAPPING + Const.PAR_WEBLET_NAME, Const.FIRST_PARAM);
                 digester.addCallParam(Const.WEBLETS_CONFIG + Const.PAR_WEBLET_MAPPING + Const.PAR_URL_PATTERN, Const.SECOND_PARAM);
                 digester.addCallMethod(Const.WEBLETS_CONFIG + Const.PAR_WEBLET + Const.PAR_SUBBUNDLE, Const.FUNC_ADD_SUBBUNDLE, Const.TWO_PARAMS);
                 digester.addCallParam(Const.WEBLETS_CONFIG + Const.PAR_WEBLET + Const.PAR_SUBBUNDLE + Const.PAR_ID, Const.FIRST_PARAM);
                 digester.addCallParam(Const.WEBLETS_CONFIG + Const.PAR_WEBLET + Const.PAR_SUBBUNDLE + Const.PAR_RESOURCES, Const.SECOND_PARAM);
                 digester.parse(in);
             }
             finally {
                 in.close();
             }
         }
         catch (IOException e) {
             throw new WebletException(e);
         }
         catch (SAXException e) {
             throw new WebletException(e);
         }
     }
 
     public void addWeblet(WebletConfigImpl webletConfig) {
         _webletConfigs.put(webletConfig.getWebletName(), webletConfig);
     }
 
     public void setWebletMapping(
             String webletName,
             String urlPattern) {
         WebletConfig webletConfig = (WebletConfig) _webletConfigs.get(webletName);
         if (webletConfig == null)
             throw new WebletException("Weblet configuration not found: " + webletName);
         Matcher matcher = Const.PATTERN_WEBLET_PATH.matcher(urlPattern);
         if (matcher.matches()) {
             String webletVersion = webletConfig.getWebletVersion();
             String webletPath = matcher.group(Const.SECOND_PARAM);
             StringBuffer buffer = new StringBuffer();
             //we have to prepend some optional mapping to cover
             //the servlet case and some frameworks
             //which add their own subcontext before triggering
             //in framework servlets
             buffer.append(".*(\\Q");
             buffer.append(webletPath);
             buffer.append("\\E)");
             if (webletVersion != null) {
                 buffer.append("\\Q$");
                 buffer.append(webletVersion);
                 buffer.append("\\E");
             }
             buffer.append("(/.*)?");
             ;
             _webletMappings.put(webletName, Pattern.compile(buffer.toString()));
             _webletPaths.put(webletName, webletPath);
         } else {
             throw new IllegalArgumentException("Invalid weblet mapping: " + urlPattern);
         }
     }
 
     public Map getRegisteredWeblets() {
         return _weblets;
     }
 
     private Format _webletURLFormat;
     private String _webletContextPath;
     private Pattern _webletURLPattern;
     private ServletContext _servletContext;
     private Map _weblets = new HashMap();
     private Map _webletPaths = new HashMap();
     private Map _webletConfigs = new HashMap();
     private Map _webletMappings = new LinkedHashMap();
 
     private ObjectCreationFactory WEBLET_CONFIG_FACTORY = new ObjectCreationFactory() {
 
         public Object createObject(Attributes attributes) throws Exception {
             return new WebletConfigImpl(WebletContainerImpl.this);
         }
 
         public Digester getDigester() {
             return digester;
         }
 
         public void setDigester(Digester digester) {
             this.digester = digester;
         }
 
         private Digester digester;
     };
 
     /**
      * Returns the mimetype of a file for the underlying hosting container
      */
     public String getContainerMimeType(String pattern) {
         return _servletContext.getMimeType(pattern);
     }
 }
