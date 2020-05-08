 /*******************************************************************************
  * Copyright (c) 2007 Exadel, Inc. and Red Hat, Inc.
  * Distributed under license by Red Hat, Inc. All rights reserved.
  * This program is made available under the terms of the
  * Eclipse Public License v1.0 which accompanies this distribution,
  * and is available at http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     Exadel, Inc. and Red Hat, Inc. - initial API and implementation
  ******************************************************************************/ 
 package org.jboss.tools.jst.web.model.helpers;
 
 import org.jboss.tools.common.meta.XModelEntity;
 import org.jboss.tools.common.meta.action.impl.handlers.DefaultCreateHandler;
 import org.jboss.tools.common.model.XModel;
 import org.jboss.tools.common.model.XModelException;
 import org.jboss.tools.common.model.XModelObject;
 import org.jboss.tools.common.model.util.XModelObjectUtil;
 
 /**
  * @author glory
  */
 public class WebAppHelper {
     public static String CONTEXT_PARAM_ENTITY = "WebAppContextParam"; //$NON-NLS-1$
     public static String FILTER_ENTITY = "WebAppFilter"; //$NON-NLS-1$
     public static String FILTER_30_ENTITY = "WebAppFilter30"; //$NON-NLS-1$
     public static String FILTER_MAPPING_ENTITY = "WebAppFilterMapping"; //$NON-NLS-1$
     public static String FILTER_MAPPING_24_ENTITY = "WebAppFilterMapping24"; //$NON-NLS-1$
     public static String LISTENER_ENTITY = "WebAppListener"; //$NON-NLS-1$
     public static String LISTENER_24_ENTITY = "WebAppListener24"; //$NON-NLS-1$
     public static String SERVLET_ENTITY = "WebAppServlet"; //$NON-NLS-1$
     public static String SERVLET_30_ENTITY = "WebAppServlet30"; //$NON-NLS-1$
     public static String SERVLET_MAPPING_ENTITY = "WebAppServletMapping"; //$NON-NLS-1$
     public static String TAGLIB_ENTITY = "WebAppTaglib"; //$NON-NLS-1$
     public static String ROLE_ENTITY = "WebAppSecurityRole"; //$NON-NLS-1$
     
     public static String FILTER_FOLDER = "Filters"; //$NON-NLS-1$
     public static String SERVLET_FOLDER = "Servlets"; //$NON-NLS-1$
     public static String JSP_CONFIG_FOLDER = "JSP Config"; //$NON-NLS-1$
     public static String ROLE_FOLDER = "Security Roles"; //$NON-NLS-1$
 	
 	/**
 	 * Returns model object representing web.xml in WEB-INF.
 	 * @param model
 	 * @return
 	 */	
 	public static XModelObject getWebApp(XModel model) {
 		return model.getByPath("/web.xml"); //$NON-NLS-1$
 	}
 	
 	public static String getServletVersion(XModelObject webxml) {
 		if(webxml == null) return ""; //$NON-NLS-1$
 		String entity = webxml.getModelEntity().getName();
 		if(entity.equals("FileWebApp")) return "2.3"; //$NON-NLS-1$ //$NON-NLS-2$
 		if(entity.equals("FileWebApp24")) return "2.4"; //$NON-NLS-1$ //$NON-NLS-2$
 		if(entity.equals("FileWebApp25")) return "2.5"; //$NON-NLS-1$ //$NON-NLS-2$
 		return "2.4"; //$NON-NLS-1$
 	}
 	
 	public static XModelObject getParentFile(XModelObject o) {
 		while(o != null && o.getFileType() != XModelObject.FILE) o = o.getParent();
 		return o;
 	}
 	
     public static XModelObject[] getServlets(XModelObject webxml) {
     	if(webxml == null) return null;
     	XModelObject folder = webxml.getChildByPath(SERVLET_FOLDER);
     	if(folder == null) folder = webxml;
     	return folder.getChildren(getServletEntity(folder.getModelEntity()));
     }
 
     public static XModelObject[] getServletMappings(XModelObject webxml) {
     	if(webxml == null) return null;
     	XModelObject folder = webxml.getChildByPath(SERVLET_FOLDER);
     	if(folder == null) folder = webxml;
     	return folder.getChildren(SERVLET_MAPPING_ENTITY);
     }
 
 	/**
 	 * Returns model object representing <servlet> with either 
 	 * <servlet-class> = className or <servlet-name> = servletName
 	 * @param webxml 
 	 * @param className <servlet-class> value or null
 	 * @param name - <servlet-name> value or null
 	 * @return
 	 */
 	public static XModelObject findServlet(XModelObject webxml, String className, String servletName) {
         if(webxml == null) return null;
         XModelObject[] s = getServlets(webxml);
         for (int i = 0; i < s.length; i++) {
         	if(className != null && className.equals(s[i].getAttributeValue("servlet-class"))) return s[i]; //$NON-NLS-1$
         	if(servletName != null && servletName.equals(s[i].getAttributeValue("servlet-name"))) return s[i]; //$NON-NLS-1$
         }
         return null;
 	}
 	
 	/**
 	 * Returns model object of entity WebAppServlet, either found
 	 * by findServlet method or new object. 
 	 * @param webxml
 	 * @param className
 	 * @param servletName
 	 * @param loadOnStartUp
 	 * @return
 	 */	
 	public static XModelObject findOrCreateServlet(XModelObject webxml, String className, String servletName, int loadOnStartUp) throws XModelException {
         if(webxml == null) return null;
         XModelObject s = findServlet(webxml, className, servletName);
         if(s == null) {
         	XModelObject folder = webxml.getChildByPath(SERVLET_FOLDER);
         	if(folder == null) folder = webxml;
         	s = webxml.getModel().createModelObject(getServletEntity(folder.getModelEntity()), null);
         	s.setAttributeValue("servlet-name", servletName); //$NON-NLS-1$
         	s.setAttributeValue("servlet-class", className); //$NON-NLS-1$
             DefaultCreateHandler.addCreatedObject(folder, s, -1);
         }
         if(loadOnStartUp >= 0 && s.getAttributeValue("load-on-startup").length() == 0) { //$NON-NLS-1$
         	s.getModel().changeObjectAttribute(s, "load-on-startup", "" + loadOnStartUp); //$NON-NLS-1$ //$NON-NLS-2$
         }
         return s;
 	}
 
 	public static String getServletEntity(XModelEntity folder) {
 		if(folder != null && folder.getChild(SERVLET_30_ENTITY) != null) return SERVLET_30_ENTITY;
 		return SERVLET_ENTITY;
 	}
 	
 	/**
 	 * Returns model object representing <servlet-mapping> with  
 	 * <servlet-name> = servletName
 	 * @param webxml 
 	 * @param name - <servlet-name> value or null
 	 * @return
 	 */
 	public static XModelObject findServletMapping(XModelObject webxml, String servletName) {
         if(webxml == null) return null;
         XModelObject[] s = getServletMappings(webxml);
         for (int i = 0; i < s.length; i++) {
         	if(servletName != null && servletName.equals(s[i].getAttributeValue("servlet-name"))) return s[i]; //$NON-NLS-1$
         }
         return null;
 	}
 	
 	/**
 	 * Returns model object of entity WebAppServlet, either found
 	 * by findServletMapping method or new object. 
 	 * @param webxml
 	 * @param servletName
 	 * @return
 	 */	
 	public static XModelObject findOrCreateServletMapping(XModelObject webxml, String servletName) throws XModelException {
         if(webxml == null) return null;
         XModelObject s = findServletMapping(webxml, servletName);
         if(s == null) {
         	XModelObject folder = webxml.getChildByPath(SERVLET_FOLDER);
         	if(folder == null) folder = webxml;
         	s = webxml.getModel().createModelObject(SERVLET_MAPPING_ENTITY, null);
         	s.setAttributeValue("servlet-name", servletName); //$NON-NLS-1$
         	s.setAttributeValue("url-pattern", "*.jsf"); //$NON-NLS-1$ //$NON-NLS-2$
             DefaultCreateHandler.addCreatedObject(folder, s, -1);
         }
         return s;
 	}
 	
 	/**
 	 * Returns model object representing <init-param> in <servlet> with 
 	 * param-name = name.
 	 * @param servlet
 	 * @param name
 	 * @return
 	 */
     public static XModelObject findWebAppInitParam(XModelObject servlet, String name) {
         if(servlet == null) return null;
         XModelObject[] init = servlet.getChildren("WebAppInitParam"); //$NON-NLS-1$
         for (int i = 0; i < init.length; i++) {
             String n = init[i].getAttributeValue("param-name"); //$NON-NLS-1$
             if(n != null && n.equals(name)) return init[i];
         }
         return null;
     }
     
     /**
      * Sets <param-value> to value in <init-param> with <param-name> = name.
      * If <init-param> does not exist, it is created. Returns model object
      * representing <init-param>.
      * @param servlet
      * @param name
      * @param value
      * @return
      */    
     public static XModelObject setWebAppInitParam(XModelObject servlet, String name, String value) throws XModelException {
         if(servlet == null) return null;
         XModelObject p = findWebAppInitParam(servlet, name);
         if(p == null) {
 			p = createWebAppInitParam(servlet, name, value);
         } else {
             servlet.getModel().changeObjectAttribute(p, "param-value", value); //$NON-NLS-1$
         }
         return p;        
     }
     
     /**
      * Appends valuePart to comma separated <param-value> in <init-param> 
      * with <param-name> = name. If <init-param> does not exist, 
      * it is created. Returns model object representing <init-param>.
      * @param servlet
      * @param name
      * @param value
      * @return
      */    
     public static XModelObject appendToWebAppInitParam(XModelObject servlet, String name, String valuePart) throws XModelException {
     	if(servlet == null) return null;
 		XModelObject p = findWebAppInitParam(servlet, name);
 		if(p == null) {
 			p = createWebAppInitParam(servlet, name, valuePart);
 		} else {
 			String oldValue = p.getAttributeValue("param-value"); //$NON-NLS-1$
 			if(oldValue.length() == 0 || ("," + oldValue + ",").indexOf("," + valuePart + ",") < 0) { //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
 				String newValue = (oldValue.length() > 0) ? oldValue + "," + valuePart : valuePart; //$NON-NLS-1$
 				servlet.getModel().changeObjectAttribute(p, "param-value", newValue); //$NON-NLS-1$
 			}
 		}
 		return p;
     }
     
     private static XModelObject createWebAppInitParam(XModelObject servlet, String name, String value) throws XModelException {
     	if(servlet == null) return null;
 		XModelObject p = servlet.getModel().createModelObject("WebAppInitParam", null); //$NON-NLS-1$
 		p.setAttributeValue("param-name", name); //$NON-NLS-1$
 		p.setAttributeValue("param-value", value); //$NON-NLS-1$
 		DefaultCreateHandler.addCreatedObject(servlet, p, -1);
 		return p;
     }
     
     /**
      * Returns list of values separated by comma in <param-value>
      * of <init-param> with <param-name> = name.
      * @param servlet
      * @param name
      * @return
      */    
     public static String[] getWebAppInitParamValueList(XModelObject servlet, String name) {
     	if(servlet == null) return new String[0];
     	XModelObject p = findWebAppInitParam(servlet, name);
     	if(p == null) return new String[0];
     	return XModelObjectUtil.asStringArray(p.getAttributeValue("param-value")); //$NON-NLS-1$
     }
 
 
 	/**
 	 * Returns model object representing <context-param> in <web-app> with 
 	 * param-name = name.
 	 * @param servlet
 	 * @param name
 	 * @return
 	 */
     public static XModelObject findWebAppContextParam(XModelObject webxml, String name) {
         if(webxml == null) return null;
         XModelObject folder = webxml.getChildByPath("Context Params"); //$NON-NLS-1$
         if(folder == null) folder = webxml;
         return folder.getChildByPath(name);
     }
 
     /**
      * Sets <param-value> to value in <context-param> with <param-name> = name.
      * If <context-param> does not exist, it is created. Returns model object
      * representing <context-param>.
      * @param servlet
      * @param name
      * @param value
      * @return
      */    
     public static XModelObject setWebAppContextParam(XModelObject webxml, String name, String value) throws XModelException {
         if(webxml == null) return null;
         XModelObject p = findWebAppContextParam(webxml, name);
         if(p == null) {
 			p = createWebAppContextParam(webxml, name, value);
         } else {
         	webxml.getModel().changeObjectAttribute(p, "param-value", value); //$NON-NLS-1$
         }
         return p;        
     }
     
     /**
      * Appends valuePart to comma separated <param-value> in <context-param> 
      * with <param-name> = name. If <context-param> does not exist, 
      * it is created. Returns model object representing <context-param>.
      * @param servlet
      * @param name
      * @param value
      * @return
      */    
     public static XModelObject appendToWebAppContextParam(XModelObject webxml, String name, String valuePart) throws XModelException {
     	if(webxml == null) return null;
 		XModelObject p = findWebAppContextParam(webxml, name);
 		if(p == null) {
 			p = createWebAppContextParam(webxml, name, valuePart);
 		} else {
 			String oldValue = p.getAttributeValue("param-value"); //$NON-NLS-1$
 			if(oldValue.length() == 0 || ("," + oldValue + ",").indexOf("," + valuePart + ",") < 0) { //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
 				String newValue = (oldValue.length() > 0) ? oldValue + "," + valuePart : valuePart; //$NON-NLS-1$
 				webxml.getModel().changeObjectAttribute(p, "param-value", newValue); //$NON-NLS-1$
 			}
 		}
 		return p;
     }
     
     private static XModelObject createWebAppContextParam(XModelObject webxml, String name, String value) throws XModelException {
     	if(webxml == null) return null;
         XModelObject folder = webxml.getChildByPath("Context Params"); //$NON-NLS-1$
         if(folder == null) folder = webxml;
 		XModelObject p = folder.getModel().createModelObject(CONTEXT_PARAM_ENTITY, null);
 		p.setAttributeValue("param-name", name); //$NON-NLS-1$
 		p.setAttributeValue("param-value", value); //$NON-NLS-1$
 		DefaultCreateHandler.addCreatedObject(folder, p, -1);
 		return p;
     }
 
     /**
      * Returns list of values separated by comma in <param-value>
      * of <init-param> with <param-name> = name.
      * @param servlet
      * @param name
      * @return
      */    
     public static String[] getWebAppContextParamValueList(XModelObject webxml, String name) {
     	if(webxml == null) return new String[0];
     	XModelObject p = findWebAppContextParam(webxml, name);
     	if(p == null) return new String[0];
     	return XModelObjectUtil.asStringArray(p.getAttributeValue("param-value")); //$NON-NLS-1$
     }
     
     public static XModelObject[] getFilters(XModelObject webxml) {
     	if(webxml == null) return null;
     	XModelObject folder = webxml.getChildByPath(FILTER_FOLDER);
     	if(folder == null) folder = webxml;
     	if(folder.getModelEntity().getChild(FILTER_30_ENTITY) != null) {
    		folder.getChildren(FILTER_30_ENTITY);
     	}
     	return folder.getChildren(FILTER_ENTITY);
     }
 
     public static XModelObject[] getFilterMappings(XModelObject webxml) {
     	if(webxml == null) return null;
     	XModelObject folder = webxml.getChildByPath(FILTER_FOLDER);
     	if(folder == null) folder = webxml;
    	String entity = (folder != webxml && folder.getModelEntity().getName().endsWith("24"))  //$NON-NLS-1$
     			? FILTER_MAPPING_24_ENTITY : FILTER_MAPPING_ENTITY;
     	return folder.getChildren(entity);
     }
 
     public static XModelObject findFilterByClass(XModelObject webxml, String cls) {
 		XModelObject[] os = getFilters(webxml);
 		for (int i = 0; i < os.length; i++) {
 			String c = os[i].getAttributeValue("filter-class"); //$NON-NLS-1$
 			if(cls.equals(c)) return os[i];
 		}
 		return null;
 	}
 
     public static XModelObject findFilterMapping(XModelObject webxml, String name) {
     	XModelObject folder = webxml.getChildByPath(FILTER_FOLDER);
     	if(folder == null) folder = webxml;
     	String entity = (folder != webxml && folder.getModelEntity().getName().endsWith("24"))  //$NON-NLS-1$
     			? FILTER_MAPPING_24_ENTITY : FILTER_MAPPING_ENTITY;
 		XModelObject[] os = folder.getChildren(entity);
 		for (int i = 0; i < os.length; i++) {
 			String c = os[i].getAttributeValue("filter-name"); //$NON-NLS-1$
 			if(name.equals(c)) return os[i];
 		}
 		return null;
 	}
 
     public static XModelObject getJSPConfig(XModelObject webxml) {
     	if(webxml == null) return null;
     	XModelObject folder = webxml.getChildByPath("JSP Config"); //$NON-NLS-1$
     	return folder != null ? folder : webxml;
     }
 
     public static XModelObject[] getTaglibs(XModelObject webxml) {
     	if(webxml == null) return null;
     	XModelObject folder = webxml.getChildByPath("JSP Config"); //$NON-NLS-1$
     	if(folder == null) folder = webxml;
     	return folder.getChildren(TAGLIB_ENTITY);
     }
 
     public static XModelObject[] getRoles(XModelObject webxml) {
     	if(webxml == null) return null;
     	XModelObject folder = webxml.getChildByPath(ROLE_FOLDER);
     	if(folder == null) folder = webxml;
     	return folder.getChildren(ROLE_ENTITY);
     }
 
 }
