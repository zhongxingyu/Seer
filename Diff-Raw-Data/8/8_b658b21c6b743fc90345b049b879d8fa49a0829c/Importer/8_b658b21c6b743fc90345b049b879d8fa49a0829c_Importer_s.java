 package com.rcs.common.utils;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.Hashtable;
 import java.util.List;
 import java.util.Locale;
 import java.util.Map;
 import java.util.Properties;
 
 import javax.servlet.ServletContext;
 
 import org.apache.commons.lang.StringUtils;
 import org.apache.log4j.Logger;
 
 import com.liferay.portal.kernel.exception.SystemException;
 import com.liferay.portal.kernel.language.LanguageUtil;
 import com.liferay.portal.kernel.util.HttpUtil;
 import com.liferay.portal.kernel.util.StringPool;
 import com.liferay.portal.kernel.xml.Document;
 import com.liferay.portal.kernel.xml.DocumentException;
 import com.liferay.portal.kernel.xml.Element;
 import com.liferay.portal.kernel.xml.SAXReaderUtil;
 import com.liferay.portal.model.Portlet;
 import com.liferay.portal.service.PortletLocalServiceUtil;
 import com.liferay.portal.util.Portal;
 import com.rcs.service.model.MessageSource;
 import com.rcs.service.service.MessageSourceLocalServiceUtil;
 import com.rcs.service.service.persistence.MessageSourcePK;
 import com.rcs.service.service.persistence.MessageSourceUtil;
 
 public class Importer {
 	
 	public static Logger _logger = Logger.getLogger(Importer.class);
 	
 	private static ClassLoader classLoader = Thread.currentThread().getContextClassLoader();    
     
     public static final String DEFAULT_RESOURCE_NAME = "Language.properties";
 
     public static final String DEFAULT_RESOURCE_PREFIX = "Language_";
     public static final String DEFAULT_RESOURCE_SUFFIX = ".properties";
     
     public static final String LIFERAY_HOOK_PATH = "/WEB-INF/liferay-hook.xml";
     
     private static String baseResourceName = "content/Language";
     
 	public static void importLanguages() throws Exception {
 		importLiferayLanguages();
 		importPortletsLanguages();
 		importI18NEditorLanguages();
 	}
 	
 	public static void importPortletsLanguages() throws Exception {
 		List<Portlet> portlets = PortletLocalServiceUtil.getPortlets();               
         
         for (Portlet portlet : portlets) {
             if (StringUtils.isNotBlank(portlet.getContextPath())) {
             	
                 String portletId = portlet.getPortletId();                                
                 String bundleName = portlet.getPluginPackage().getName();
                 if(StringUtils.isBlank(bundleName)) {
                 	bundleName = portlet.getPortletName();
                 }                              
                 
                com.liferay.portal.kernel.portlet.PortletBag portletBag = com.liferay.portal.kernel.portlet.PortletBagPool.get(portletId);                               
                ServletContext servletContext = portletBag.getServletContext();
                importPortletLanguages(servletContext, bundleName);
             }
         }
 	}
 	
 	public static void importLiferayLanguages() throws Exception {
 		Locale[] availableLocales = LanguageUtil.getAvailableLocales();
 
         _logger.info("Number of available locales is " + availableLocales.length);
 
         for (int i = 1; i <= availableLocales.length; i++) {
 
             Locale locale = availableLocales[i - 1];
 
             _logger.info("Start importing locale " + locale.toString() + ", for bundle " + "\"" + RcsConstants.DEFAULT_BUNDLE_NAME + "\"" + ", #" + i);
 
             Map<String, String> languageMap = null;
                         	
         	languageMap = getDefaultLanguageMap(locale);        	
         	Hashtable hashBundle = new Hashtable(languageMap);
         	saveProperties(locale, hashBundle, RcsConstants.DEFAULT_BUNDLE_NAME);
         }
 	}
 	
 	public static void importPortletLanguages(ServletContext servletContext, String bundleName) throws Exception {		            
          
 		_logger.info("Import plugin: " + bundleName);
     	//determines whether <resource-bundle> specified in 'portlet.xml' or <language-properties> specified in 'liferay-hook.xml'
         boolean resourceBundleSpecified = false;                        
         
         String portletXML = StringUtils.EMPTY;
         try {
             portletXML = HttpUtil.URLtoString(servletContext.getResource("/WEB-INF/" + Portal.PORTLET_XML_FILE_NAME_STANDARD));
         } catch (IOException e) {
             _logger.warn("Can not read portlet.xml");
         }        
         
         if (StringUtils.isNotBlank(portletXML)) {
 
             //== try to read resource bundle from portlet.xml ===
             try {
                 resourceBundleSpecified = readPortletXML(portletXML, servletContext, bundleName);
             } catch (DocumentException e) {
                 if (_logger.isDebugEnabled()) {
                     _logger.debug("Unable to read process xml. ");
                 }                
             }
         }
 
         // if <resource-bundle> not specified in portlet.xml -  read from liferay-hook.xml
         if (!resourceBundleSpecified) {
 
             String liferayHookXml = StringUtils.EMPTY;
             try {
                 liferayHookXml = HttpUtil.URLtoString(servletContext.getResource(LIFERAY_HOOK_PATH));
             } catch (IOException e) {
                 _logger.warn("Can not read liferay-hook.xml");
             }
 
             if (StringUtils.isNotBlank(liferayHookXml)) {
 
                 //== try to read resource bundle from liferay-hook.xml ===
                 try {
                     resourceBundleSpecified = readLiferayHookXML(liferayHookXml, servletContext, bundleName);
                 } catch (DocumentException e) {
                     if (_logger.isDebugEnabled()) {
                         _logger.debug("Unable to read process xml. ");
                     }
                 }
             }
         }
 
         // if <resource-bundle> not specified - use default
         if (!resourceBundleSpecified) {
             processBundle(DEFAULT_RESOURCE_NAME, servletContext, bundleName);
         }    	    	
 	}
 	           
     private static Map<String, String> getDefaultLanguageMap(Locale locale) {
         Map<String, String> languageMap;
 
         try {
             Locale invariantLocale = new Locale(locale.getLanguage());
 
             //put locale invariant to special map
             PortalLanguageResourcesUtil.putLanguageMap(invariantLocale);
 
             //get language map of parent locale
             languageMap = PortalLanguageResourcesUtil.getLanguageMap(invariantLocale);
 
             //put casual locale
             PortalLanguageResourcesUtil.putLanguageMap(locale);
             //get language map for casual locale and merge it with invariant locale
             languageMap.putAll(PortalLanguageResourcesUtil.getLanguageMap(locale));
 
             return languageMap;
         } catch (Exception e) {
             //come to the next in case of error
             _logger.warn("Could not put new language map", e);
 
             return Collections.emptyMap();
         }
     }
     
     private static boolean readLiferayHookXML(String xml, ServletContext servletContext, String bundleName) throws DocumentException {
 
         boolean resourceBundleSpecified = false;
 
         Document document = SAXReaderUtil.read(xml, true);
 
         Element hookElement = document.getRootElement();
 
         if (hookElement != null) {
 
             String resourceBundleName = hookElement.elementText("language-properties");
 
             if (resourceBundleName != null) {
 
                 resourceBundleSpecified = true;
 
                 processBundle(resourceBundleName, servletContext, bundleName);
             }
 
         }
 
         return resourceBundleSpecified;
     }
 
     /*
     *  Reads 'resource-bundle' property from portlet.xml
     * */
     private static boolean readPortletXML(String xml, ServletContext servletContext, String bundleName) throws DocumentException {
 
         boolean resourceBundleSpecified = false;
 
         Document document = SAXReaderUtil.read(xml, true);
 
         Element rootElement = document.getRootElement();
 
         for (Element portletElement : rootElement.elements("portlet")) {
 
             String resourceBundleName = portletElement.elementText("resource-bundle").replace('.', '/');
 
             if (resourceBundleName != null) {
 
                 resourceBundleSpecified = true;                
                 processBundle(resourceBundleName, servletContext, bundleName);
             }
         }
         
         return resourceBundleSpecified;
     }
 
     /*
     *   Processes resource bundle for specified locale
     * */
     private static void processBundle(String resourceBundleName, ServletContext servletContext, String bundleName) {
     	
         String baseResourceName = resourceBundleName;
 
         if (resourceBundleName.endsWith(DEFAULT_RESOURCE_SUFFIX))
             baseResourceName = StringUtils.substringBefore(resourceBundleName, DEFAULT_RESOURCE_SUFFIX);
 
         URL defaultResource = null;
 		try {
 			defaultResource = servletContext.getResource(resourceBundleName);
 		} catch (MalformedURLException e2) {
 			// TODO Auto-generated catch block
 			e2.printStackTrace();
 		}
 
         if (defaultResource != null) {
 
             Properties bundle = new Properties();
             try {
                 InputStream inStream = defaultResource.openStream();
                 bundle.load(inStream);
                 inStream.close();
             } catch (IOException e) {
                 _logger.warn("Could not read file", e);
             }
 
             Hashtable hashBundle = new Hashtable(bundle);
             saveProperties(Locale.getDefault(), hashBundle, bundleName);
         }
         
         Locale[] availableLocales = LanguageUtil.getAvailableLocales();
         _logger.info("Number of available locales is " + availableLocales.length);
         
         for (int i = 1; i <= availableLocales.length; i++) {
 
             Locale locale = availableLocales[i - 1];
 
         	_logger.info("Start importing locale " + locale.toString() + ", for bundle " + "\"" + bundleName + "\"" + ", #" + i);
         	
             String resourceName = "/WEB-INF/classes/" + baseResourceName + StringPool.UNDERLINE + locale.getLanguage() + DEFAULT_RESOURCE_SUFFIX;
                        
             URL resource = null;
 			try {
 				resource = servletContext.getResource(resourceName);
 			} catch (MalformedURLException e1) {
 				// TODO Auto-generated catch block
 				e1.printStackTrace();
 			}
 
             if (resource == null) {
                 resourceName = baseResourceName + StringPool.UNDERLINE + locale.toString() + DEFAULT_RESOURCE_SUFFIX;
                 try {
 					resource = servletContext.getResource(resourceName);
 				} catch (MalformedURLException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
             }
 
             if (resource != null) {
 
                 Properties bundle = new Properties();
                 try {
                     InputStream inStream = resource.openStream();
                     bundle.load(inStream);
                     inStream.close();
                 } catch (IOException e) {
                     _logger.warn("Could not read file", e);
                     continue;
                 }
 
                 Hashtable hashBundle = new Hashtable(bundle);
                 saveProperties(locale, hashBundle, bundleName);
             }
             
             int leftNum = availableLocales.length - i;
             String leftStr = leftNum == 0 ? "nothing" : String.valueOf(leftNum);
 
             _logger.info("Stop importing locale " + locale.toString() + ", " + leftStr + " left");
         }
     }
 
     /*
      *   Processes resource bundle for specified locale
      * */
      private static void saveProperties(Locale locale, Hashtable<String, String> bundle, String bundleName) {
 
          for (Map.Entry property : bundle.entrySet()) {
 
              String key = (String) property.getKey();
              String value = (String) property.getValue();
              
              try {
              
              	MessageSourcePK messageSourcePK = new MessageSourcePK(key, locale.toString());
  	        	MessageSource messageSource = MessageSourceLocalServiceUtil.fetchMessageSource(messageSourcePK);
              	
  	            if (messageSource == null) {
  	 	            		            
  	            	messageSource = MessageSourceUtil.create(messageSourcePK); 	
  	                messageSource.setValue(value);                
  	                messageSource.setBundle(bundleName); 	                
  	                MessageSourceLocalServiceUtil.updateMessageSource(messageSource); 	                
  	            }  	            
  	            
              } catch (SystemException ex) {
              	ex.printStackTrace();
              } catch (Exception ex) {
              	ex.printStackTrace();
              }
          }
      }
 
      public static void importI18NEditorLanguages() throws Exception {
     	 
     	 Locale[] availableLocales = LanguageUtil.getAvailableLocales();
     	 String bundleName = RcsConstants.I18N_EDITOR_BUNDLE_NAME;
     	 
          _logger.info("Number of available locales is " + availableLocales.length);
 
          for (int i = 1; i <= availableLocales.length; i++) {
 
              Locale locale = availableLocales[i - 1];
 
              _logger.info("Start importing locale " + locale.toString() + ", for bundle " + "\"" + bundleName + "\"" + ", #" + i);
              
              
              Map<String, String> languageMap = getI18NEditorLanguageMap(locale);
 
              Hashtable hashBundle = new Hashtable(languageMap);
              saveProperties(locale, hashBundle, bundleName);          
              
              //small formatting
              int leftNum = availableLocales.length - i;
              String leftStr = leftNum == 0 ? "nothing" : String.valueOf(leftNum);
 
              _logger.info("Stop importing locale " + locale.toString() + ", " + leftStr + " left");
          }
 
     	 
      }
      
      public static Map<String, String> getI18NEditorLanguageMap(Locale locale) {
 
          String resourceName = baseResourceName + StringPool.UNDERLINE + locale.getLanguage() + DEFAULT_RESOURCE_SUFFIX;
          
          URL resource = classLoader.getResource(resourceName);
 
          if (resource == null) {
              resourceName = baseResourceName + StringPool.UNDERLINE + locale.toString() + DEFAULT_RESOURCE_SUFFIX;
              resource = classLoader.getResource(resourceName);
          }
 
          if (resource == null) {
              resourceName = baseResourceName + StringPool.UNDERLINE + DEFAULT_RESOURCE_SUFFIX;
              resource = classLoader.getResource(resourceName);
          }
 
          if (resource != null) {
 
              Properties bundle = new Properties();
 
              try {
 
                  InputStream inStream = resource.openStream();
                  bundle.load(inStream);
                  inStream.close();
 
                  return new HashMap<String, String>((Map) bundle);
 
              } catch (IOException e) {
                  _logger.warn("Could not read file", e);
              }
          }
 
          return Collections.emptyMap();
      }
 }
