 /*******************************************************************************
  * Copyright (c) 2005 Oracle Corporation.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    Gerry Kessler - initial API and implementation
  *******************************************************************************/ 
 
 package org.eclipse.jst.jsf.core.internal.project.facet;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.util.Iterator;
 import java.util.List;
 
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.jst.javaee.core.JavaeeFactory;
 import org.eclipse.jst.javaee.core.ParamValue;
 import org.eclipse.jst.javaee.core.UrlPatternType;
 import org.eclipse.jst.javaee.web.Servlet;
 import org.eclipse.jst.javaee.web.ServletMapping;
 import org.eclipse.jst.javaee.web.WebApp;
 import org.eclipse.jst.javaee.web.WebAppVersionType;
 import org.eclipse.jst.javaee.web.WebFactory;
 import org.eclipse.jst.jsf.core.IJSFCoreConstants;
 import org.eclipse.jst.jsf.core.internal.JSFCorePlugin;
 import org.eclipse.jst.jsf.core.internal.Messages;
 import org.eclipse.wst.common.frameworks.datamodel.IDataModel;
 
 /**
  * Utility file for JSF v1.2 model
  * 
  * @author Gerry Kessler - Oracle
  */
 public class JSFUtils12 extends JSFUtils{
 
 	/**
 	 * @param webApp
 	 * @return Servlet - the JSF Servlet for the specified WebApp or null if not present
 	 */
 	public static Servlet findJSFServlet(WebApp webApp) {
 
 		Iterator it = webApp.getServlets().iterator();
 		
 		while (it.hasNext()) {
             Servlet servlet = (Servlet) it.next();
 			if (servlet.getServletClass().equals (JSF_SERVLET_CLASS)) {
 				return servlet;
 			}
 		}
         
         // if we get to here then we have finished the loop
         // without finding the servlet we're looking for
 		return null;
 	}
 
 	/**
 	 * Creates a stubbed JSF v1.2 configuration file for specified JSF version and path
 	 * @param jsfVersion
 	 * @param configPath
 	 */
 	public static void createConfigFile(String jsfVersion, IPath configPath) {
 		FileOutputStream os = null;
 		PrintWriter pw = null;
 		final String QUOTE = new String(new char[] { '"' });
 		try {
 			IPath dirPath = configPath.removeLastSegments(1);
 			dirPath.toFile().mkdirs();
 			File file = configPath.toFile();
 			file.createNewFile();
 			os = new FileOutputStream(file);
 			pw = new PrintWriter(os);
 			pw.write("<?xml version=" + QUOTE + "1.0" + QUOTE + " encoding=" //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
 					+ QUOTE + "UTF-8" + QUOTE + "?>\n\n"); //$NON-NLS-1$ //$NON-NLS-2$
 
 			if (jsfVersion.equals(IJSFCoreConstants.FACET_VERSION_1_2)) 
             {
 				pw.write("<faces-config\n"); //$NON-NLS-1$
 				pw.write("    " + "xmlns=" + QUOTE //$NON-NLS-1$ //$NON-NLS-2$
 						+ "http://java.sun.com/xml/ns/javaee" + QUOTE + "\n"); //$NON-NLS-1$ //$NON-NLS-2$
 				pw.write("    " + "xmlns:xsi=" + QUOTE //$NON-NLS-1$ //$NON-NLS-2$
 						+ "http://www.w3.org/2001/XMLSchema-instance" + QUOTE //$NON-NLS-1$
 						+ "\n"); //$NON-NLS-1$
 				pw
 						.write("    " //$NON-NLS-1$
 								+ "xsi:schemaLocation=" //$NON-NLS-1$
 								+ QUOTE
 								+ "http://java.sun.com/xml/ns/javaee http://java.sun.com/xml/ns/javaee/web-facesconfig_1_2.xsd" //$NON-NLS-1$
 								+ QUOTE + "\n"); //$NON-NLS-1$
 				pw.write("    " + "version=" + QUOTE + "1.2" + QUOTE + ">\n\n"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
 				pw.write("</faces-config>\n"); //$NON-NLS-1$
 			}
 
 			pw.close();
 			pw = null;
 		} catch (FileNotFoundException e) {
 			JSFCorePlugin.log(IStatus.ERROR, Messages.JSFUtils_ErrorCreatingConfigFile, e);
 		} catch (IOException e) {
 			JSFCorePlugin.log(IStatus.ERROR, Messages.JSFUtils_ErrorCreatingConfigFile, e);
 		} finally {
 			if (pw != null)
 				pw.close();
 			if (os != null) {
 				try {
 					os.close();
 				} catch (IOException e) {
 					JSFCorePlugin.log(IStatus.ERROR, Messages.JSFUtils_ErrorClosingConfigFile, e);
 				}
 			}
 		}
 	}
 
 
 	/**
 	 * Creates servlet reference in WebApp if not present or updates servlet name if found
 	 * using the passed configuration.
 	 * 
 	 * @param webApp
 	 * @param config
 	 * @param servlet
 	 * @return Servlet servlet - if passed servlet was null, will return created servlet
 	 */
 	public static Servlet createOrUpdateServletRef(WebApp webApp,
 			IDataModel config, org.eclipse.jst.javaee.web.Servlet servlet) {
 		
 		String displayName = getDisplayName(config);		
 		String className = getServletClassname(config);
 		
 		if (servlet == null){			
 			// Create the servlet instance and set up the parameters from data
 			// model
 			servlet = WebFactory.eINSTANCE.createServlet();
 			servlet.setId(displayName);
 			servlet.setServletName(displayName);
 			servlet.setServletClass(className);
 			servlet.setLoadOnStartup(new Integer(1));
 			// Add the servlet to the web application model
 			webApp.getServlets().add(servlet);
 
 		} else {
 			// update
 			servlet.setId(displayName);
 			servlet.setServletName(displayName);
 			servlet.setLoadOnStartup(new Integer(1));
 		}
 		return servlet;
 	}
 
 	/**
 	 * @param webAppObj as Object
 	 * @return true if webApp instanceof org.eclipse.jst.javaee.web.WebApp and WebAppVersionType._25
 	 */
 	public static boolean isWebApp25(Object webAppObj) {
 		if (webAppObj instanceof WebApp &&
 				((WebApp)webAppObj).getVersion() == WebAppVersionType._25_LITERAL) 
 			return true;
 		return false;
 	}
 	
 	/**
 	 * Creates servlet-mappings for the servlet for 2.5 WebModules or greated
 	 * 
 	 * @param webApp
 	 * @param urlMappingList - list of string values to  be used in url-pattern for servlet-mapping
 	 * @param servlet
 	 */
 	public static void setUpURLMappings(WebApp webApp, List urlMappingList,
 			Servlet servlet) {
 		
 		if (urlMappingList.size() > 0) {
 			ServletMapping mapping = WebFactory.eINSTANCE.createServletMapping();
 			mapping.setId(servlet.getServletName());
 			mapping.setServletName(servlet.getServletName());
 			webApp.getServletMappings().add(mapping);
 			// Add patterns
 			Iterator it = urlMappingList.iterator();
 			while (it.hasNext()) {
 				String pattern = (String) it.next();
 				UrlPatternType urlPattern = JavaeeFactory.eINSTANCE.createUrlPatternType();
 				urlPattern.setValue(pattern);				
 				mapping.getUrlPatterns().add(urlPattern);							
 			}
 		}
 	}
 	
 	/**
 	 * Removes servlet-mappings for servlet using servlet-name for >= 2.5 WebModules.
 	 * @param webApp
 	 * @param servlet
 	 */
 	public static void removeURLMappings(WebApp webApp, Servlet servlet) {
 		List mappings = webApp.getServletMappings();					
 		String servletName = servlet.getServletName();
 		if (servletName != null) {
 			for (int i=mappings.size()-1;i>=0;--i){
 				ServletMapping mapping = (ServletMapping)mappings.get(i);
 				if (mapping.getServletName()
 						.equals(servletName)) {
 					mappings.remove(mapping);
 				}
 			}
 		}
 	}
 	
 	/**
 	 * Creates or updates config file context-param in v2.5 WebApp if non default configuration file is specified.
 	 * @param webApp
 	 * @param config
 	 */
 	public static void setupConfigFileContextParamForV2_5(org.eclipse.jst.javaee.web.WebApp webApp,
 			IDataModel config) {
 		// if not default name and location, then add context param
 		ParamValue foundCP = null;
 		ParamValue cp = null;
 		boolean found = false;
 		if (!config.getStringProperty(IJSFFacetInstallDataModelProperties.CONFIG_PATH).equals(JSF_DEFAULT_CONFIG_PATH)) {
 			// check to see if present
 			Iterator it = webApp.getContextParams().iterator();
 			while (it.hasNext()) {
 				cp = (org.eclipse.jst.javaee.core.ParamValue) it.next();
 				if (cp.getParamName().equals(JSF_CONFIG_CONTEXT_PARAM)) {
 					foundCP = cp;
 					found = true;
 				}
 			}
 			if (!found) {
 				ParamValue pv = JavaeeFactory.eINSTANCE.createParamValue();
 				pv.setParamName(JSF_CONFIG_CONTEXT_PARAM);
 				pv.setParamValue(config.getStringProperty(IJSFFacetInstallDataModelProperties.CONFIG_PATH));
 				webApp.getContextParams().add(pv);
 			} else {
 				cp = foundCP;
 				if (cp.getParamValue().indexOf(config.getStringProperty(IJSFFacetInstallDataModelProperties.CONFIG_PATH)) < 0) {
 					String curVal = cp.getParamValue();
 					String val = config.getStringProperty(IJSFFacetInstallDataModelProperties.CONFIG_PATH);
 					if (curVal != null || !curVal.trim().equals("")) { //$NON-NLS-1$
 						val = curVal + ",\n" + val; //$NON-NLS-1$
 					}
 					cp.setParamValue(val);
 				}
 			}
 		}
 	}
 
 	/**
 	 * @param webApp
 	 * @return the default file extension from the context param.  Default is "jsp" if no context param
 	 */
 	public static String getDefaultSuffix(WebApp webApp) {
 		String defaultSuffix = "jsp"; //$NON-NLS-1$
 		for (Iterator it = webApp.getContextParams().iterator();it.hasNext();) {		
 			ParamValue cp = (ParamValue) it.next();		
 			if (cp.getParamName().equals(JSF_DEFAULT_SUFFIX_CONTEXT_PARAM)){				
 				String defSuffix = cp.getParamValue();
 				if (defSuffix.startsWith(".")) //$NON-NLS-1$
 					defSuffix = defSuffix.substring(1);
 								
 				return defSuffix;
 			}
 		}
 		return defaultSuffix;
 	}
 	
 	/**
 	 * @param map
 	 * @return prefix mapping 
 	 */
 	public static String getPrefixMapping(ServletMapping map) {
 		List urls = map.getUrlPatterns();
 		for (Iterator it=urls.iterator();it.hasNext();){
			IPath extPath = new Path(((UrlPatternType)it.next()).getValue());
 			if (extPath != null){
 				String ext = extPath.getFileExtension();
 				if (ext == null){
 					String lastSeg = extPath.lastSegment();
 					if (lastSeg.equals("*")) //$NON-NLS-1$
 					{
 						return extPath.removeLastSegments(1).toString();
 					}
 					
 					return extPath.toString();				
 				}
 			}
 		}
 		return null;
 	}
 	
 	/**
 	 * @param map
 	 * @return extension from map.  Will return null if file extension not found in url patterns.
 	 */
 	public static String getFileExtensionFromMap(ServletMapping map) {
 		List urls = map.getUrlPatterns();
 		for (Iterator it=urls.iterator();it.hasNext();){
			IPath extPath = new Path(((UrlPatternType)it.next()).getValue());
 			if (extPath != null){
 				String ext = extPath.getFileExtension();
 				if (ext != null && !ext.equals("")) //$NON-NLS-1$
 					return ext;
 			}
 		}
 		return null;
 	}
 }
