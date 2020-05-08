 /*
  * Copyright 2004-2009 the Seasar Foundation and the Others.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
  * either express or implied. See the License for the specific language
  * governing permissions and limitations under the License.
  */
 package org.seasar.teeda.extension.util;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.util.Calendar;
 import java.util.Collection;
 import java.util.LinkedHashMap;
 import java.util.LinkedHashSet;
 import java.util.Map;
 import java.util.Set;
 
 import javax.faces.context.FacesContext;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.seasar.framework.util.AssertionUtil;
 import org.seasar.framework.util.InputStreamUtil;
 import org.seasar.framework.util.OutputStreamUtil;
 import org.seasar.framework.util.ResourceUtil;
 import org.seasar.teeda.extension.render.html.THtmlHeadRenderer;
 
 /**
  * @author higa
  *
  */
 public abstract class VirtualResource {
 
     private static final String VIRTUAL_PATH = "/teedaExtension/";
 
     private static final String NAME = THtmlHeadRenderer.class.getName();
 
     private static final String JS_KEY = NAME + ".JS_KEY";
 
     private static final String INLINE_JS_KEY = NAME + ".INLINE_JS_KEY";
 
     private static final String CSS_KEY = NAME + ".CSS_KEY";
 
     private static final String INLINE_CSS_KEY = NAME + ".INLINE_CSS_KEY";
 
     protected VirtualResource() {
     }
 
     public static Set getJsResources(FacesContext context) {
         return getSetResources(context, JS_KEY);
     }
 
     public static Set getCssResources(FacesContext context) {
         return getSetResources(context, CSS_KEY);
     }
 
     public static Map getInlineJsResources(FacesContext context) {
         return getMapResources(context, INLINE_JS_KEY);
     }
 
     public static Map getInlineCssResources(FacesContext context) {
         return getMapResources(context, INLINE_CSS_KEY);
     }
 
     public static Collection getInlineJsResourceValues(FacesContext context) {
         return getMapResources(context, INLINE_JS_KEY).values();
     }
 
     public static Collection getInlineCssResourceValues(FacesContext context) {
         return getMapResources(context, INLINE_CSS_KEY).values();
     }
 
     protected static Set getSetResources(FacesContext context, String key) {
         AssertionUtil.assertNotNull("context", context);
         AssertionUtil.assertNotNull("key", key);
         Map requestMap = context.getExternalContext().getRequestMap();
         Set resources = (Set) requestMap.get(key);
         if (resources == null) {
             resources = new LinkedHashSet();
             requestMap.put(key, resources);
         }
         return resources;
     }
 
     protected static Map getMapResources(FacesContext context, String key) {
         AssertionUtil.assertNotNull("context", context);
         AssertionUtil.assertNotNull("key", key);
         Map requestMap = context.getExternalContext().getRequestMap();
         Map resources = (Map) requestMap.get(key);
         if (resources == null) {
             resources = new LinkedHashMap();
             requestMap.put(key, resources);
         }
         return resources;
     }
 
     public static void addJsResource(FacesContext context, String path) {
         AssertionUtil.assertNotNull("context", context);
         AssertionUtil.assertNotNull("path", path);
         Set resources = getJsResources(context);
         if (!resources.contains(path)) {
             resources.add(path);
         }
     }
 
     public static void addCssResource(FacesContext context, String path) {
         AssertionUtil.assertNotNull("context", context);
         AssertionUtil.assertNotNull("path", path);
         Set resources = getCssResources(context);
         if (!resources.contains(path)) {
             resources.add(path);
         }
     }
 
     public static void addInlineJsResource(FacesContext context, String key,
             String script) {
         AssertionUtil.assertNotNull("context", context);
         AssertionUtil.assertNotNull("path", key);
         AssertionUtil.assertNotNull("script", script);
         Map resources = getInlineJsResources(context);
         if (!resources.containsKey(key)) {
             resources.put(key, script);
         }
     }
 
     public static void addInlineJsResource(FacesContext context, String key,
             JavaScriptProvider provider) {
         AssertionUtil.assertNotNull("context", context);
         AssertionUtil.assertNotNull("path", key);
         AssertionUtil.assertNotNull("script", provider);
         Map resources = getInlineJsResources(context);
         if (!resources.containsKey(key)) {
             resources.put(key, provider);
         }
     }
 
     public static void addInlineCssResource(FacesContext context, String key,
             String style) {
         AssertionUtil.assertNotNull("context", context);
         AssertionUtil.assertNotNull("path", key);
         AssertionUtil.assertNotNull("script", style);
         Map resources = getInlineCssResources(context);
         if (!resources.containsKey(key)) {
             resources.put(key, style);
         }
     }
 
     public static String convertVirtualPath(FacesContext context, String path) {
         return context.getExternalContext().getRequestContextPath() +
                 VIRTUAL_PATH + path;
     }
 
     public static boolean isVirtualPath(HttpServletRequest request) {
         return request.getRequestURI().indexOf(VIRTUAL_PATH) != -1;
     }
 
     public static boolean startsWithVirtualPath(String path) {
         return path.startsWith(VIRTUAL_PATH);
     }
 
     public static void resolveVirtualPath(HttpServletRequest request,
             HttpServletResponse response) throws IOException {
         String path = getResourcePath(request);
         String lcPath = path.toLowerCase();
         if (lcPath.endsWith(".js")) {
             response.setContentType("text/javascript");
         } else if (lcPath.endsWith(".css")) {
             response.setContentType("text/css");
         } else if (lcPath.endsWith(".gif")) {
             response.setContentType("image/gif");
         } else if (lcPath.endsWith(".png")) {
             response.setContentType("image/png");
         } else if (lcPath.endsWith(".jpg") || lcPath.endsWith(".jpeg")) {
             response.setContentType("image/jpeg");
        } else if (lcPath.endsWith(".xml") || lcPath.endsWith(".xsl")) {
            response.setContentType("text/xml");
         }
         response.setDateHeader("Last-Modified", 0);
         Calendar expires = Calendar.getInstance();
         expires.add(Calendar.DATE, 1);
         response.setDateHeader("Expires", expires.getTimeInMillis());
         InputStream is = ResourceUtil.getResourceAsStream(path);
         OutputStream os = response.getOutputStream();
         try {
             InputStreamUtil.copy(is, os);
         } finally {
             InputStreamUtil.close(is);
             OutputStreamUtil.close(os);
         }
     }
 
     protected static String getResourcePath(HttpServletRequest request) {
         String uri = request.getRequestURI();
         int pos = uri.indexOf(VIRTUAL_PATH) + VIRTUAL_PATH.length();
         return uri.substring(pos);
     }
 }
