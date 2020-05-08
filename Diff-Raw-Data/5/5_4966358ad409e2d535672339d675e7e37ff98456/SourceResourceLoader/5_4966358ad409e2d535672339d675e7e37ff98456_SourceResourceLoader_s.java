 /*
  * Licensed to the Apache Software Foundation (ASF) under one
  * or more contributor license agreements.  See the NOTICE file
  * distributed with this work for additional information
  * regarding copyright ownership.  The ASF licenses this file
  * to you under the Apache License, Version 2.0 (the
  * "License"); you may not use this file except in compliance
  * with the License.  You may obtain a copy of the License at
  *
  *   http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing,
  * software distributed under the License is distributed on an
  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  * KIND, either express or implied.  See the License for the
  * specific language governing permissions and limitations
  * under the License.
 */
 package org.apache.myfaces.extensions.scripting.jsf2.resources;
 
 import org.apache.myfaces.shared_impl.resource.ExternalContextResourceLoader;
 import org.apache.myfaces.shared_impl.resource.ResourceLoader;
 import org.apache.myfaces.shared_impl.resource.ResourceMeta;
 import org.apache.myfaces.extensions.scripting.core.util.WeavingContext;
 
 import javax.faces.context.FacesContext;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.Collections;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 /**
  * internal resource loader to be used with our custom resource handler
  * the resource loader is added to the list of available loaders
  * so that the resource gets loaded properly from our source path
  * instead of the web context if present, the source paths as usual
  * are picked up by our context params.
  */
 public class SourceResourceLoader extends ExternalContextResourceLoader {
 
     public SourceResourceLoader(String prefix) {
         super(prefix);
     }
 
     @Override
     protected Set<String> getResourcePaths(String path) {
         List<String> resourceRoots = WeavingContext.getConfiguration().getResourceDirs();
         if (resourceRoots == null || resourceRoots.isEmpty()) {
             return Collections.EMPTY_SET;
         }
         Set<String> retVals = new HashSet<String>(resourceRoots.size());
         //for (String resourceRoot : resourceRoots) {
         retVals.add(getPrefix() + "/" + path);
         //}
         return retVals;
     }
 
     @Override
     public URL getResourceURL(ResourceMeta resourceMeta) {
         try {
             List<String> resourceRoots = WeavingContext.getConfiguration().getResourceDirs();
             if (resourceRoots == null || resourceRoots.isEmpty()) {
                 return super.getResourceURL(resourceMeta);
             }
 
             for (String resourceRoot : resourceRoots) {
                File resourceFile = new File(resourceRoot + getPrefix() + "/" + resourceMeta.toString());
                 if (resourceFile.exists()) {
                     return resourceFile.toURI().toURL();
                 }
             }
 
             return super.getResourceURL(resourceMeta);
         }
         catch (MalformedURLException e) {
             return null;
         }
     }
 
     @Override
     public InputStream getResourceInputStream(ResourceMeta resourceMeta) {
         try {
             List<String> resourceRoots = WeavingContext.getConfiguration().getResourceDirs();
             if (resourceRoots == null || resourceRoots.isEmpty()) {
                 return super.getResourceInputStream(resourceMeta);
             }
 
             for (String resourceRoot : resourceRoots) {
                File resourceFile = new File(resourceRoot + getPrefix() + "/" + resourceMeta.toString());
                 if (resourceFile.exists()) {
                     return new FileInputStream(resourceFile);
                 }
             }
 
             return super.getResourceInputStream(resourceMeta);
         }
         catch (IOException e) {
             return null;
         }
     }
 }
