 /*
  *  Copyright (C) 2005  Iulian-Corneliu Costan
  *
  *  This library is free software; you can redistribute it and/or
  *  modify it under the terms of the GNU Lesser General Public
  *  License as published by the Free Software Foundation; either
  *  version 2.1 of the License, or (at your option) any later version.
  *
  *  This library is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  *  Lesser General Public License for more details.
  *
  *  You should have received a copy of the GNU Lesser General Public
  *  License along with this library; if not, write to the Free Software
  *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
  */
 package wicket.contrib.tinymce;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import wicket.Application;
 import wicket.IInitializer;
 import wicket.markup.html.PackageResource;
 import wicket.util.string.Strings;
 
 import java.io.File;
 import java.io.IOException;
 import java.net.URL;
 import java.util.Enumeration;
 import java.util.zip.ZipEntry;
 import java.util.zip.ZipFile;
 
 
 /**
  * Wicket initializer for TinyMCE component.
  *
  * @author Iulian-Corneliu COSTAN
  */
 public class TinyMCEInitializer implements IInitializer
 {
 
     private static final Log log = LogFactory.getLog(TinyMCEInitializer.class);
 
     private static final String TINY_MCE = "tiny_mce";
 
     private Class scope = TinyMCEPanel.class;
 
 
     public void init(Application application)
     {
         String protocol = scope.getResource("").getProtocol();
         if ("file".equals(protocol))
         {
             initFromClasses(application);
 
         }
         else if ("jar".equals(protocol))
         {
             initFromJar(application);
 
         }
         else
         {
             if (log.isErrorEnabled())
             {
                 log.error("unknown protocol, only file and jar are implemented");
             }
             throw new UnsupportedOperationException("protocol " + protocol + "not implemented");
         }
     }
 
     private void initFromJar(Application application)
     {
         ZipFile zipFile = null;
         try
         {
             String basePath = scope.getResource(Strings.afterLast(scope.getName(), '.') + ".class").getPath();
             String jarFilePath = new URL(Strings.beforeLast(basePath, '!')).getPath();
             zipFile = new ZipFile(jarFilePath);
         }
         catch (IOException e)
         {
             if (log.isErrorEnabled())
             {
                 log.error("tinymce.jar file exception", e);
             }
             throw new RuntimeException(e);
         }
 
         Enumeration entries = zipFile.entries();
         while (entries.hasMoreElements())
         {
             ZipEntry zipEntry = (ZipEntry) entries.nextElement();
             if (zipEntry.getName().indexOf(TINY_MCE) > 0)
             {
                 String name = zipEntry.getName();
                 bindResource(application, name);
             }
         }
     }
 
     private void initFromClasses(Application application)
     {
        String basePath = scope.getResource("").getPath();
         initResources(application, basePath);
     }
 
     private void initResources(Application application, String path)
     {
         File tinyMceDir = new File(path + "/" + TINY_MCE);
         recursiveInitialization(application, tinyMceDir);
     }
 
     private void recursiveInitialization(Application application, File dir)
     {
         File[] files = dir.listFiles();
         for (int i = 0; i < files.length; i++)
         {
             File file = files[i];
             if (file.isDirectory())
             {
                 recursiveInitialization(application, file);
             }
             else
             {
                 String uri = file.toURI().toString();
                 bindResource(application, uri);
             }
         }
     }
 
     private void bindResource(Application application, String uri)
     {
         String resource = uri.substring(uri.indexOf(TINY_MCE));
         PackageResource.bind(application, scope, resource);
     }
 
     /* used for testing purpose */
     void setScope(Class scope)
     {
         this.scope = scope;
     }
 }
