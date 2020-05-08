 /**********************************************************************************
  * Copyright (c) 2011, Monnet Project
  * All rights reserved.
  * 
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions are met:
  *     * Redistributions of source code must retain the above copyright
  *       notice, this list of conditions and the following disclaimer.
  *     * Redistributions in binary form must reproduce the above copyright
  *       notice, this list of conditions and the following disclaimer in the
  *       documentation and/or other materials provided with the distribution.
  *     * Neither the name of the Monnet Project nor the
  *       names of its contributors may be used to endorse or promote products
  *       derived from this software without specific prior written permission.
  * 
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
  * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
  * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
  * DISCLAIMED. IN NO EVENT SHALL THE MONNET PROJECT BE LIABLE FOR ANY
  * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
  * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
  * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
  * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
  * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
  * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  *********************************************************************************/
 package eu.monnetproject.util;
 
 import eu.monnetproject.osgi.OSGiUtil;
 import java.io.File;
 import java.io.FileReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.Reader;
 import java.net.MalformedURLException;
 import java.net.URL;
import java.net.URLDecoder;
 import org.osgi.framework.Bundle;
 
 /**
  *
  * @author John McCrae
  */
 public class ResourceFinder {
 
     private static final Logger log = Logging.getLogger(ResourceFinder.class);
 
     public static Reader getResourceAsReader(String name) throws IOException {
         final URL resource = getResource(name);
         if (resource == null) {
             return null;
         } else {
             if (resource.getProtocol().equals("file")) {
                final File trgFile = new File(URLDecoder.decode(resource.getFile(),"UTF-8"));
                return new FileReader(trgFile);
             } else {
                 return new InputStreamReader(resource.openStream());
             }
         }
     }
 
     public static URL getResource(String name) {
         final Bundle fwBundle = OSGiUtil.getFrameWorkBundle();
         if (fwBundle != null) {
             for (Bundle bundle : fwBundle.getBundleContext().getBundles()) {
                 final URL resource = bundle.getResource(name);
                 if (resource != null) {
                     return resource;
                 }
             }
         } else {
             final URL resource = Thread.currentThread().getContextClassLoader().getResource(name);
             if (resource != null) {
                 return resource;
             } else {
                 final URL resource1 = ResourceFinder.class.getResource(name);
                 if (resource1 != null) {
                     return resource1;
                 }
             }
         }
         File f = new File(System.getProperty("user.dir")+ System.getProperty("file.separator") + "load" + System.getProperty("file.separator") + name);
         if (f.exists()) {
             try {
                 return f.toURI().toURL();
             } catch (MalformedURLException ex) {
             }
         } else {
             URL url = ResourceFinder.class.getResource("load/" + name);
             if (url != null) {
                 return url;
             } else {
                 url = ResourceFinder.class.getResource("/load/" + name);
                 if (url != null) {
                     return url;
                 } else {
                     if(name.startsWith("/") || name.matches("\\w:\\\\.*")) {
                         final File file = new File(name);
                         if(file.exists()) {
                             try {
                                 return file.toURI().toURL();
                             } catch(MalformedURLException x) {
                             }
                         }
                     }
                     log.warning("Resource not found: " + f.getPath());
                 }
             }
         }
         return null;
     }
 }
