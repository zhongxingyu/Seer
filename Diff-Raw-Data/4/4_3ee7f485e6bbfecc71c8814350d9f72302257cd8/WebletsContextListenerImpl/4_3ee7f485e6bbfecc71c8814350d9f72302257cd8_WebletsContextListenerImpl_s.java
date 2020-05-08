 /*
  * WebletContextListener.java
  *
  * Created on November 29, 2006, 12:40 AM
  *
  * To change this template, choose Tools | Template Manager
  * and open the template in the editor.
  */
 package net.java.dev.weblets.impl;
 
 import net.java.dev.weblets.WebletContainer;
 import net.java.dev.weblets.WebletsServlet;
 import net.java.dev.weblets.impl.parse.DisconnectedEntityResolver;
 import net.java.dev.weblets.impl.util.ConfigurationUtils;
 import net.java.dev.weblets.impl.util.ReflectUtilsImpl;
 import org.apache.commons.digester.Digester;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.xml.sax.SAXException;
 
 import javax.servlet.ServletContext;
 import javax.servlet.ServletContextEvent;
 import javax.servlet.ServletContextListener;
 import javax.servlet.Servlet;
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.text.MessageFormat;
 import java.util.Enumeration;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.Set;
 import java.util.regex.Pattern;
 
 /**
  * @author john.fallows
  * @changes Werner Punz <p/> Changes from 0.4 to 1.0 <p/> Clear calling order of weblets servlets and jsf servlets <p/> Last references into the view handler
  * have been removed <p/> We have enforced a path trigger pattern Weblets servlet overrides any other entry it has higher priority than the faces
  * servlet pattern <p/> A simplified regexp handling of the pattern parsing parts Changes from 1.0 to 1.1 multiple weblet configs added as optional
  * context param
  * <p/>
  * <p/>
  * TODO add auto filters here to enable the request handling without parameters
  * so that we are able to plug in the current request as singleton holder
  * for our includes!
  */
 public class WebletsContextListenerImpl implements ServletContextListener {
 
 	public void contextInitialized(ServletContextEvent event) {
 		ServletContext context = event.getServletContext();
 		WebletContainer container = createContainer(context);
 	}
 
 	public void contextDestroyed(ServletContextEvent event) {
 		WebletContainerImpl container = (WebletContainerImpl) WebletContainerImpl.getInstance();
 		if (container != null)
 			container.destroy();
 	}
 
 	private WebletContainer createContainer(ServletContext context) {
 		try {
             URL webXml = context.getResource(Const.PAR_WEB_INF + Const.WEB_XML);
             String triggerPattern = Const.FACES + Const.ALL;
             String contextPath = Const.BLANK;
 			boolean multipleConfigs = false;
 			if (webXml != null) {
 				InputStream in = webXml.openStream();
 				try {
 					WebXmlParser parser = new WebXmlParser();
 					Digester digester = new Digester();
                    ClassLoader loader = context.getClass().getClassLoader();
                    digester.setClassLoader(loader);

 
 					digester.setValidating(false);
 					digester.setEntityResolver(DisconnectedEntityResolver.sharedInstance());
 					digester.push(parser);
                     digester.addCallMethod(Const.WEB_APP_SERVLET, Const.FUNC_ADD_SERVLET, 2);
                     digester.addCallParam(Const.WEB_APP_SERVLET + Const.PAR_SERVLET_NAME, 0);
                     digester.addCallParam(Const.WEB_APP_SERVLET + Const.PAR_SERVLET_CLASS, 1);
                     digester.addCallMethod(Const.WEB_APP_SERVLET_MAPPING, Const.FUNC_ADD_SERVLET_MAPPING, 2);
                     digester.addCallParam(Const.WEB_APP_SERVLET_MAPPING + Const.PAR_SERVLET_NAME, 0);
                     digester.addCallParam(Const.WEB_APP_SERVLET_MAPPING + Const.PAR_URL_PATTERN, 1);
                     digester.addCallMethod(Const.WEB_APP_CONTEXT_PARAM, Const.FUNC_ADD_CONTEXT_PARAM, 2);
                     digester.addCallParam(Const.WEB_APP_CONTEXT_PARAM + Const.PAR_PARAM_NAME, 0);
                     digester.addCallParam(Const.WEB_APP_CONTEXT_PARAM + Const.PAR_PARAM_VALUE, 1);
 					digester.parse(in);
                     if (parser.getWebletPattern() != null && !parser.getWebletPattern().trim().equals(Const.BLANK))
 						triggerPattern = parser.getWebletPattern();
                     else if (parser.getFacesPattern() != null && !parser.getFacesPattern().trim().equals(Const.BLANK))
 						triggerPattern = parser.getFacesPattern();
 					multipleConfigs = parser.isMultipleWebletConfigs();
                     contextPath = ReflectUtilsImpl.calculateContextPath(parser, context);
 					handlePathPatternWarnings(parser);
 				} catch (SAXException e) {
 					throw new RuntimeException(e);
 				} finally {
 					in.close();
 				}
 			}
 			// TODO: determine Faces Weblets ViewIds, assumes /weblets/*
             String webletsViewIds = Const.PAR_WEBLETS + Const.ALL;// we add a dedicated
 			// weblets/ to our url for
 			// the filter
 			// auto-prepend leading slash in case it is missing from web.xml
 			// entry
             if (!triggerPattern.startsWith(Const.SLASH)) {
                 triggerPattern = Const.SLASH + triggerPattern;
 			}
 			// to avoid any conflicts we reserve the weblets subnamespace
 			// anything under /weblets is a clear
 			// reference into a weblet resource url, anything before is the
 			// trigger and anything after
 			// is the version and path
 			String formatPattern = triggerPattern.replaceFirst("/\\*", webletsViewIds).replaceFirst("/\\*", "{0}");
 			// TODO remove some double weblets pattern to reduce urls
 			String webletsPattern = triggerPattern.replaceAll("\\.", "\\\\.").replaceAll("\\*", "weblets(/.*)");
 			MessageFormat format = new MessageFormat(formatPattern);
 			WebletContainerImpl container = new WebletContainerImpl(context, contextPath, format, Pattern.compile(webletsPattern), multipleConfigs);
 			// TODO we have to add the multiple configs here as well if needed
 			try {
 				// this is code duplication with the container we probably have to move
 				// all of this into our utils
 				if (multipleConfigs) {
 					Set configs = new HashSet();
 					// Enumeration e = getConfigEnumeration("weblets-config.xml"); /*lets find the root configs first*/
 					if (multipleConfigs) {
                         ConfigurationUtils.getValidConfigFiles(Const.PAR_WEB_INF, Const.WEB_XML, configs);
                         ConfigurationUtils.getValidConfigFiles(Const.PAR_META_INF, Const.WEBLETS_CONFIG_XML, configs);
                         ConfigurationUtils.getValidConfigFiles(Const.PAR_META_INF, Const.MANIFEST_MF, configs);
                         ConfigurationUtils.getValidConfigFiles(Const.PAR_META_INF, Const.CONTEXT_XML, configs);
 					}
 					Iterator configNameIterator = configs.iterator();
 					// Defensive: Glassfish.v2.b25 produces duplicates in Enumeration
 					// returned by loader.getResources()
 					Set urls = new HashSet(); // urls.add(element);
 					while (configNameIterator.hasNext()) {
 						Enumeration theUrlEnum = ConfigurationUtils.getConfigEnumeration("META-INF/", (String) configNameIterator.next());
 						while (theUrlEnum.hasMoreElements()) {
 							URL resource = (URL) theUrlEnum.nextElement();
 							urls.add(resource);
 						}
 					}
 					Iterator urlIterator = urls.iterator();
 					while (urlIterator.hasNext()) {
 						URL resource = (URL) urlIterator.next();
 						container.registerConfig(resource);
 					}
 				} else {
                     URL resource = context.getResource(Const.PAR_WEB_INF + Const.WEBLETS_CONFIG_XML);
 					if (resource != null)
 						container.registerConfig(resource);
                     resource = context.getResource(Const.PAR_META_INF + Const.WEBLETS_CONFIG_XML);
 					if (resource != null)
 						container.registerConfig(resource);
 				}
 			} catch (MalformedURLException e) {
                 context.log(Const.ERR_REGISTER, e);
 			}
 			return container;
 		} catch (IOException e) {
 			throw new RuntimeException(e);
 		}
 	}
 
 	private void handlePathPatternWarnings(WebXmlParser parser) {
 		if (!isPathPattern(parser.getFacesPattern()) && parser.isJSFEnabled()) {
 			Log logger = LogFactory.getLog(this.getClass());
             logger.warn(Const.ERR_PATHPATTERN_MISSING);
 		} else if (!isPathPattern(parser.getWebletPattern()) && parser.isServletEnabled()) {
 			Log logger = LogFactory.getLog(this.getClass());
             logger.warn(Const.ERR_PATHPATTERNMISSING_2);
 		}
 	}
 
 	static public class WebXmlParser {
         public static final String FACES_SERVLET = "javax.faces.webapp.FacesServlet";
 
 		public void addServlet(String servletName, String servletClass) {
             if (FACES_SERVLET.equals(servletClass))
 				_facesServletName = servletName;
 			if (WebletsServlet.class.getName().equals(servletClass))
 				_webletServletName = servletName;
 		}
 
 		public void addServletMapping(String servletName, String urlPattern) {
 			if (servletName.equals(_facesServletName))
                 if (_facesPattern == null || _facesPattern.trim().equals(Const.BLANK) || isPathPattern(urlPattern))
 					_facesPattern = urlPattern;
 			if (servletName.equals(_webletServletName))
                 if (_webletPattern == null || _webletPattern.trim().equals(Const.BLANK) || isPathPattern(urlPattern))
 					_webletPattern = urlPattern;
 		}
 
 		public void addContextParam(String contextName, String contextValue) {
 			if (contextName != null && contextName.matches(_contextPathPattern)) {
 				_webletsContextPath = contextValue.trim();
 			} else if (contextName != null && contextName.matches(_multipleConfigFiles)) {
 				_multipleWebletConfigs = true;
 			}
 		}
 
 		public boolean isServletEnabled() {
             return _webletServletName != null && !_webletServletName.trim().equals(Const.BLANK);
 		}
 
 		public boolean isJSFEnabled() {
             return _facesServletName != null && !_facesServletName.trim().equals(Const.BLANK);
 		}
 
 		public String getFacesPattern() {
 			return _facesPattern;
 		}
 
 		public String getWebletPattern() {
 			return _webletPattern;
 		}
 
 		public String getWebletsContextPath() {
 			return _webletsContextPath;
 		}
 
 		public boolean isMultipleWebletConfigs() {
 			return _multipleWebletConfigs;
 		}
 
 		public void setMultipleWebletConfigs(boolean multipleWebletConfigs) {
 			_multipleWebletConfigs = multipleWebletConfigs;
 		}
 
 		private String			_facesServletName;
 		private String			_facesPattern;
 		private String			_webletServletName;
 		private String			_webletPattern;
 		private String			_webletsContextPath;
 		private boolean			_multipleWebletConfigs	= false;
 		/* optional servlet context params to enforce the weblt initialisation */
 		/**
 		 * first one allows an override of a given context path
 		 */
 		private static String	_contextPathPattern		= "^\\s*net\\.java\\.dev\\.weblets\\.contextpath\\s*$";
 		/**
 		 * if this one is set to true a multiple config file path search is enabled either a weblets-config must be present or a Manifest.mf all other files are
 		 * *weblets-config*.xml
 		 */
 		private static String	_multipleConfigFiles	= "\\s*net\\.java\\.dev\\.weblets\\.multipleconfigs\\s*$";
 	}
 
 	private static boolean isPathPattern(String in) {
 		if (in == null)
 			return false;
 		return in.trim().matches("^(.)*\\/.*\\/(\\*){0,1}$");
 	}
 }
