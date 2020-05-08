 /*
  * jMaki Version 2
  *
  * Copyright (c) 2009 jmaki.org
  * 
  * Licensed under the MIT License:
  * 
  *  http://www.opensource.org/licenses/mit-license.php
  *
  */
 package org.jmaki.model.impl;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import org.jmaki.model.IConfig;
 import org.jmaki.model.IContext;
 import org.jmaki.model.IWidget;
 import org.jmaki.util.JSONUtil;
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 public class WidgetFactory {
 
     public static final String WIDGET_CONFIGS = "WIDGET_CONFIGS";
 
     @SuppressWarnings("unchecked")
     public static WidgetConfig loadConfig(IContext ctx, String name) {
         Map<String, WidgetConfig> cfgs = (Map<String, WidgetConfig>)ctx.getGlobalAttribute(WIDGET_CONFIGS);
         
         WidgetConfig wcfg = null;
         boolean devMode = ctx.isDevMode();
         if (cfgs == null) {
             cfgs = new HashMap<String, WidgetConfig>();
         }
         if (cfgs != null  && !devMode && 
             cfgs.get(name) != null) {
             wcfg =  cfgs.get(name);
         } else {
            System.out.println("Creating a new Widget Config for " + name);
             wcfg = new WidgetConfig();
         }
         String baseDir = "/" + ctx.getGlobalConfig().getAttribute(IConfig.RESOURCE_ROOT) + "/" + name.replace(".", "/") + "/";
         wcfg.setBaseDir(baseDir);
         // find the config
         InputStream in = null;
         // check the web app
         String resourceName = baseDir + "widget.json";
         try {
             in = ctx.getResourceAsStream(resourceName);
         } catch (IOException e) {
             // do nothing
         }
         JSONObject json = null;
         if (in == null) {
            System.out.println("No widget.json at " + resourceName);
         } else {
             json = JSONUtil.loadFromInputStream(in);
         }
 
         String widgetDir = ctx.getWebRoot() + ctx.getContextRoot() + baseDir;
 
         if (json != null) {
             if (json.has("config")) {
                 try {
                     JSONObject config = json.getJSONObject("config");
                     JSONObject typeo = null;
                     if (config.has("type")) {
                         typeo = config.getJSONObject("type");
                         if (typeo.has("libs")) {
                             List<ResourceURI> scripts = getResourceURIs(ResourceURI.SCRIPT, typeo.getJSONArray("libs"), baseDir, name);
                             wcfg.setScripts(scripts);
                         }
                         if (typeo.has("styles")) {
                             List<ResourceURI> styles = getResourceURIs(ResourceURI.LINK, typeo.getJSONArray("styles"), baseDir, name);
                             wcfg.setStyles(styles);
                         }
                     }
 
                 } catch (JSONException e) {
                     getLogger().log(Level.SEVERE, "Error parsing coinfig file " + resourceName, e);
                 }
             }
         }
         boolean hasCSS = ctx.resourceExists(baseDir  + "component.css");
         boolean hasTemplate = ctx.resourceExists(baseDir + "component.htm");
 
         wcfg.setHasCss(hasCSS);
         wcfg.setWidgetDir(widgetDir);
         wcfg.setHasTemplate(hasTemplate);
         if (hasTemplate) {
             try {
                 StringBuffer template = ctx.getResource(baseDir, "component.htm");
                 wcfg.setTemplate(template);
             } catch (IOException e) {
                 getLogger().log(Level.SEVERE, "Error loading template " + baseDir + "component.htm", e);
             }
         }
         cfgs.put(name, wcfg);
         ctx.setGlobalAttribute(WIDGET_CONFIGS, cfgs);
         return wcfg;
     }
 
     public static StringBuffer getWidgetFragment(IWidget w, WidgetConfig wcfg, IContext ctx) throws IOException {
         StringBuffer templateBuffer = new StringBuffer(wcfg.getTemplate().toString());
         replace(templateBuffer, "${uuid}", w.getUuid());
         // default service URL to the name
         if (w.getService() != null) {
             replace(templateBuffer, "${service}", w.getService());
         }
         if (w.getArgs() != null) {
             replace(templateBuffer, "${args}", w.getArgs().toString());
         }
         if (w.getValue() != null && w.getValue() instanceof String) {
             if (((String) w.getValue()).startsWith("@{")) {
                 replace(templateBuffer, "${value}", "");
             } else {
                 replace(templateBuffer, "${value}", w.getValue().toString());
             }
         }
         replace(templateBuffer, "${webRoot}", ctx.getWebRoot()); 
         replace(templateBuffer, "${widgetDir}", wcfg.getWidgetDir());
         replace(templateBuffer, "${contextPath}", ctx.getContextRoot());
       return templateBuffer;
     }
 
     public static void replace(StringBuffer buff, String target, String replacement) {
         if (buff == null || target == null || replacement == null) {
             return;
         }
         int index = 0;
         while (index < buff.length()) {
             index = buff.indexOf(target);
             if (index == -1) {
                 break;
             }
             buff.replace(index, index +  target.length(), replacement);
             index += replacement.length() + 1;
         }
     }
     
     static List<ResourceURI> getResourceURIs(int type, JSONArray ja, String baseURI, String name) throws JSONException {
 
         List<ResourceURI> resources = null;
         resources = new ArrayList<ResourceURI>();
 
         for (int j = 0; j < ja.length(); j++) {
             
             Object o = ja.get(j);
             JSONObject so = null;
             String url = null;
             if (o instanceof JSONObject) {
                 so = (JSONObject)o;
                 url = so.getString("url");
             } else if (o instanceof String) {
                 url = (String)o;
             }
             if (url != null) {
                 if (url.startsWith("/") || url.startsWith("http")) {
                     baseURI = "";
                 }
                 ResourceURI ri = null;
                 try {
                     ri = new ResourceURI(resolvePath("/resources/" + name.replace(".", "/"), url), "", type);
                 } catch (WidgetConfigException e) {
                     getLogger().log(Level.SEVERE, "Error parsing widget " + name + " configuration.",e);
                 }
                 if (so != null && ri != null) {
                     if (so.has("id")) {
                         ri.setId(so.getString("id"));
                     }
                     if (so.has("uaTest")) {
                         ri.setUATest(so.getString("uaTest"));
                     }
                     if (so.has("test")) {
                         ri.setTest(so.getString("test"));
                     }
                 }
                 resources.add(ri);
             }
         }
         return resources;
     }
     
     public static String resolvePath(String rootPath, String relativePath) throws WidgetConfigException {
         if (rootPath == null || relativePath.startsWith("/") || isExternalUri(relativePath))
             return relativePath;
         int relativePathLen=relativePath.length();
         int relativePos = 0;
         for (;relativePos<relativePathLen && relativePath.indexOf("../",relativePos)==relativePos;relativePos+=3);
         int numberOfDirUp = relativePos>0 ? relativePos / 3 : 0;
         if (numberOfDirUp>0) {
             int rootPos = rootPath.length();
             int positionOfSlash;
             while ((positionOfSlash=rootPath.lastIndexOf('/',rootPos-1))>0 && numberOfDirUp>0) {
                 rootPos=positionOfSlash;
                 numberOfDirUp--;
             }
             if (numberOfDirUp>0) {
                 throw new WidgetConfigException("Error parsing widget path rootPath : " +
                                               rootPath  + ". relativePath : " + relativePath);
             }
             return rootPath.substring(0,rootPos)+'/'+relativePath.substring(relativePos);
         }
         return rootPath.endsWith("/") ? rootPath+relativePath : rootPath+'/'+relativePath;
     }
     
     public static boolean isExternalUri(String uri) {
         return uri.startsWith("http://") || uri.startsWith("https://");
     }
 
     private static Logger logger;
 
     public static Logger getLogger() {
         if (logger == null) {
             logger = Logger.getLogger("org.jmaki");
         }
         return logger;
     }
 }
