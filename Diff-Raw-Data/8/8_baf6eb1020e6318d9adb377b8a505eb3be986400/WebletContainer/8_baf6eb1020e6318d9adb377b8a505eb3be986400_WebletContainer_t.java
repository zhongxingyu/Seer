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
 package net.java.dev.weblets;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.URL;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.Map;
 
 abstract public class WebletContainer {
     static protected void setInstance(WebletContainer container) throws WebletException {
         ClassLoader loader = getLoader();
         _INSTANCES.put(loader, container);
     }
 
     /* we store the valid classoader on a per thread base to bypass a performance leak */
     static final int CLASSLOADER_CONTEXT = 0;
     static final int CLASSLOADER_LOCAL = 1;
     static final int CLASSLOADER_UNDEFINED = -1;
     static int classloaderType = CLASSLOADER_UNDEFINED;    /* we do not keep the classloader only the type */
 
     /**
      * caching loader determination algorithm
      *
      * @return a valid classloader with access to our resources
      */
    public static ClassLoader getLoader() {
         /* once we are at a classloader we have to stay there! */
         if (classloaderType != CLASSLOADER_UNDEFINED) {
             switch (classloaderType) {
                 case CLASSLOADER_LOCAL:
                     return WebletContainer.class.getClassLoader();
                 default:
                     return Thread.currentThread().getContextClassLoader();
             }
         } else {
             ClassLoader loader = null;
             loader = Thread.currentThread().getContextClassLoader();
             URL testRes = loader.getResource("META-INF/services/" + WebletContainer.class.getName());
             classloaderType = CLASSLOADER_CONTEXT;
             if (testRes == null) { /* on some containers we do not have access to the resources over the context classloader at certain times */
                 loader = WebletContainer.class.getClassLoader();
                 classloaderType = CLASSLOADER_LOCAL;
             }
             return loader;
         }
     }
 
     static public WebletContainer getInstance() throws WebletException {
         ClassLoader loader = getLoader();
         return (WebletContainer) _INSTANCES.get(loader);
     }
 
     abstract public String getWebletContextPath();
 
     abstract public void setWebletContextPath(String contextPath);
 
     abstract public void service(WebletRequest request, WebletResponse response) throws IOException, WebletException;
 
     abstract public String getResourceUri(String webletName, String pathInfo) throws WebletException;
 
     /**
      * returns the mimetype of the underlying hosting container
      *
      * @param pattern
      * @return
      */
     public abstract String getContainerMimeType(String pattern);
 
     /**
      * Method which returns the actual resource as input stream from a given weblet request
      *
      * @param request the current request
      * @return an input stream on the resource
      * @throws WebletException
      */
     abstract public InputStream getResourceStream(WebletRequest request, String mimetype) throws WebletException;
 
     static private final Map _INSTANCES = Collections.synchronizedMap(new HashMap());
 }
