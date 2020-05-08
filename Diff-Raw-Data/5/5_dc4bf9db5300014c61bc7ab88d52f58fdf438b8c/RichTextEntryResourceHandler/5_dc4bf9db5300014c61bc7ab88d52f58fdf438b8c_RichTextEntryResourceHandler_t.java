 /*
  * Copyright 2004-2012 ICEsoft Technologies Canada Corp.
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
  */
 
 package org.icefaces.ace.component.richtextentry;
 
 import org.icefaces.impl.util.Base64;
 import org.icefaces.impl.util.Util;
 import org.icefaces.util.EnvUtils;
 
 import javax.faces.application.Resource;
 import javax.faces.application.ResourceHandler;
 import javax.faces.application.ResourceHandlerWrapper;
 import javax.faces.context.ExternalContext;
 import javax.faces.context.FacesContext;
 import javax.faces.event.AbortProcessingException;
 import javax.faces.event.PreRenderViewEvent;
 import javax.faces.event.SystemEvent;
 import javax.faces.event.SystemEventListener;
 import java.io.*;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.*;
 
 public class RichTextEntryResourceHandler extends ResourceHandlerWrapper {
     private static final String INPUTRICHTEXT_CKEDITOR_DIR = "icefaces.ace/richtextentry/ckeditor/";
     private static final String META_INF_RESOURCES = "/META-INF/resources/";
     private static final String CKEDITOR_MAPPING_JS = "ckeditor.mapping.js";
     private static final String CKEDITOR_JS = "ckeditor.js";
     private ResourceHandler handler;
     private String extensionMapping;
     private HashMap<String, ResourceEntry> cssResources = new HashMap();
     private ResourceEntry codeResource;
     private String prefixMapping;
 	private int countInvokations = 0; // workaround for issue with not correctly mapping the urls the first time
 
     public RichTextEntryResourceHandler(ResourceHandler handler) {
         this.handler = handler;
 
         final ArrayList imageResources = new ArrayList();
         final ArrayList allResources = new ArrayList();
         try {
             //collecting resource relative paths
             Class thisClass = this.getClass();
             InputStream in = thisClass.getResourceAsStream(META_INF_RESOURCES + "icefaces.ace/richtextentry/ckeditor.resources");
             String resourceList = new String(readIntoByteArray(in), "UTF-8");
             String[] paths = resourceList.split(" ");
             for (int i = 0; i < paths.length; i++) {
                 String localPath = paths[i];
                 byte[] content = readIntoByteArray(thisClass.getResourceAsStream(META_INF_RESOURCES + localPath));
                 if (localPath.endsWith(".css")) {
                     cssResources.put(localPath, new ResourceEntry(localPath, content));
                 } else if (localPath.endsWith(".jpg") || localPath.endsWith(".gif") || localPath.endsWith(".png")) {
                     imageResources.add(localPath);
                 } else {
                     allResources.add(localPath);
                 }
             }
 
             //calculate mappings when the first request comes in (to find out the context path)
             FacesContext.getCurrentInstance().getApplication().subscribeToEvent(PreRenderViewEvent.class, new SystemEventListener() {
                 public void processEvent(SystemEvent event) throws AbortProcessingException {
                     FacesContext context = FacesContext.getCurrentInstance();
                     try {
 						countInvokations++;
                         calculateExtensionMapping();
                         calculateMappings(context, allResources, cssResources, imageResources);
                     } catch (UnsupportedEncodingException e) {
                         throw new AbortProcessingException(e);
                     }
                 }
 
                 public boolean isListenerForSource(Object source) {
                     return EnvUtils.isICEfacesView(FacesContext.getCurrentInstance());
                 }
             });
         } catch (Exception e) {
             throw new RuntimeException(e);
         }
     }
 
     private void calculateExtensionMapping() {
         if (extensionMapping == null || prefixMapping == null) {
             Resource resource = super.createResource(INPUTRICHTEXT_CKEDITOR_DIR + CKEDITOR_JS);
             String path = resource.getRequestPath();
 
             int extensionPosition = path.indexOf(".js");
             int queryPosition = path.indexOf("?");
             if (queryPosition > 0 && queryPosition < extensionPosition) {
                 //there is no exception mapping used, most probably we're running as a portlet with URLs seriously mangled
                 extensionMapping = "";
             } else {
                 extensionMapping = extensionPosition < 0 ? "" : path.substring(extensionPosition + 3/*".js".length()*/);
             }
 
             int prefixPosition = path.indexOf(ResourceHandler.RESOURCE_IDENTIFIER + "/" + INPUTRICHTEXT_CKEDITOR_DIR);
             prefixMapping = prefixPosition < 0 ? "" : path.substring(0, prefixPosition);
         }
     }
 
     private void calculateMappings(FacesContext context, ArrayList allResources, HashMap cssResources, ArrayList imageResources) throws UnsupportedEncodingException {
         ExternalContext externalContext = context.getExternalContext();
         Map applicationMap = externalContext.getApplicationMap();
 
         String value = (String) applicationMap.get(RichTextEntryResourceHandler.class.getName());
        if (value == null || countInvokations == 1) {
             //rewrite relative request paths
             Iterator<ResourceEntry> i = cssResources.values().iterator();
             while (i.hasNext()) {
                 ResourceEntry css = i.next();
                 String content = css.getContentAsString("UTF-8");
                 String dir = toRelativeLocalDir(css.localPath);
 
                 Iterator<String> ri = imageResources.iterator();
                 while (ri.hasNext()) {
                     String entry = ri.next();
                     String path = toRelativeLocalPath(entry);
                     if (path.startsWith(dir)) {
                         String relativePath = path.substring(dir.length() + 1);
                         String requestPath = externalContext.encodeResourceURL(toRequestPath(context, entry));
                         content = content.replaceAll(relativePath, requestPath);
                     }
                 }
 
                 css.setContentAsString(content, "UTF-8");
             }
 
             //add modified css resources
             allResources.addAll(cssResources.keySet());
             //add images
             allResources.addAll(imageResources);
 
             StringBuffer code = new StringBuffer();
             code.append("window.CKEDITOR_GETURL = function(r) { var mappings = [");
             Iterator<String> entries = allResources.iterator();
             while (entries.hasNext()) {
                 String next = entries.next();
                 code.append("{i: '");
                 code.append(toRelativeLocalPath(next));
                 code.append("', o: '");
                 code.append(externalContext.encodeResourceURL(toRequestPath(context, next)));
                 code.append("'}");
                 if (entries.hasNext()) {
                     code.append(",");
                 }
             }
             code.append("]; if (r.indexOf('://') > -1) { var i = document.location.href.lastIndexOf('/'); r = r.substring(i + 1); }; for (var i = 0, l = mappings.length; i < l; i++) { var m = mappings[i]; if (m.i == r) { return m.o;} } return false; };");
 
             value = code.toString();
             applicationMap.put(RichTextEntryResourceHandler.class.getName(), value);
         }
 
        if (codeResource == null || countInvokations == 1) {
             codeResource = new ResourceEntry(INPUTRICHTEXT_CKEDITOR_DIR + CKEDITOR_MAPPING_JS, value.getBytes("UTF-8"));
         }
     }
 
     public ResourceHandler getWrapped() {
         return handler;
     }
 
     public Resource createResource(String resourceName) {
         return createResource(resourceName, null, null);
     }
 
     public Resource createResource(String resourceName, String libraryName) {
         return createResource(resourceName, libraryName, null);
     }
 
     public Resource createResource(String resourceName, String libraryName, String contentType) {
         if (codeResource != null && codeResource.localPath != null && codeResource.localPath.equals(resourceName)) {
             //serving up the mapping as a referenced JS resource
             return codeResource;
         } else if (cssResources != null && cssResources.containsKey(resourceName)) {
             //serve the modified CSS resources
             return cssResources.get(resourceName);
         } else {
             //let JSF serve the rest of resources
             return super.createResource(resourceName, libraryName, contentType);
         }
     }
 
     private static byte[] readIntoByteArray(InputStream in) throws IOException {
         byte[] buffer = new byte[4096];
         int bytesRead;
         ByteArrayOutputStream out = new ByteArrayOutputStream();
 
         while ((bytesRead = in.read(buffer)) != -1) {
             out.write(buffer, 0, bytesRead); // write
         }
         out.flush();
 
         return out.toByteArray();
     }
 
     private static String toRelativeLocalPath(String localPath) {
         return localPath.substring(INPUTRICHTEXT_CKEDITOR_DIR.length());
     }
 
     public String toRequestPath(FacesContext context, String localPath) {
         return prefixMapping + ResourceHandler.RESOURCE_IDENTIFIER + "/" + localPath + extensionMapping;
     }
 
     private String toRelativeLocalDir(String localPath) {
         int position = localPath.lastIndexOf("/");
         return INPUTRICHTEXT_CKEDITOR_DIR.length() > position ? "/" : localPath.substring(INPUTRICHTEXT_CKEDITOR_DIR.length(), position);
     }
 
     private class ResourceEntry extends Resource {
         private Date lastModified = new Date();
         private String localPath;
         private byte[] content;
         private String mimeType;
 
         private ResourceEntry(String localPath, byte[] content) {
             this.localPath = localPath;
             this.content = content;
             FacesContext facesContext = FacesContext.getCurrentInstance();
             ExternalContext externalContext = facesContext.getExternalContext();
             this.mimeType = externalContext.getMimeType(localPath);
         }
 
         public InputStream getInputStream() throws IOException {
             return new ByteArrayInputStream(content);
         }
 
         public Map<String, String> getResponseHeaders() {
 
             HashMap headers = new HashMap();
             headers.put("ETag", eTag());
             headers.put("Cache-Control", "public");
             headers.put("Content-Type", mimeType);
             headers.put("Date", Util.HTTP_DATE.format(new Date()));
             headers.put("Last-Modified", Util.HTTP_DATE.format(lastModified));
 
             return headers;
         }
 
         public String getContentType() {
             return mimeType;
         }
 
         public String getRequestPath() {
             return toRequestPath(FacesContext.getCurrentInstance(), localPath);
         }
 
         public URL getURL() {
             try {
                 return FacesContext.getCurrentInstance().getExternalContext().getResource(localPath);
             } catch (MalformedURLException e) {
                 throw new RuntimeException(e);
             }
         }
 
         public boolean userAgentNeedsUpdate(FacesContext context) {
             try {
                 Date modifiedSince = Util.HTTP_DATE.parse(context.getExternalContext().getRequestHeaderMap().get("If-Modified-Since"));
                 return lastModified.getTime() > modifiedSince.getTime() + 1000;
             } catch (Throwable e) {
                 return true;
             }
         }
 
         private String getContentAsString(String encoding) throws UnsupportedEncodingException {
             return new String(content, encoding);
         }
 
         private void setContentAsString(String newContent, String encoding) throws UnsupportedEncodingException {
             content = newContent.getBytes(encoding);
         }
 
         private String eTag() {
             return Base64.encode(String.valueOf(localPath.hashCode()));
         }
     }
 }
