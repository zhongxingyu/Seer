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
 
 package org.apache.myfaces.extensions.scripting.core.loader;
 
 import org.apache.myfaces.extensions.scripting.core.api.WeavingContext;
 import org.apache.myfaces.extensions.scripting.core.common.util.ClassUtils;
 import org.apache.myfaces.extensions.scripting.core.monitor.ClassResource;
 
 import java.io.*;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import static java.util.logging.Level.SEVERE;
 
 /**
  * @author Werner Punz (latest modification by $Author$)
  * @version $Revision$ $Date$
  */
 
 public class ThrowAwayClassloader extends ClassLoader
 {
 
     static final Logger _logger = Logger.getLogger(ThrowAwayClassloader.class.getName());
     boolean _untaint = true;
     public ThrowAwayClassloader(ClassLoader classLoader, boolean untaint)
     {
         super(classLoader);
         _untaint = untaint;
     }
     
     public ThrowAwayClassloader(ClassLoader classLoader)
     {
         super(classLoader);
     }
 
     public ThrowAwayClassloader()
     {
     }
 
     @Override
     /**
      * load called either if the class is not loaded at all
      * or if the class has been recompiled (check upfront)
      */
     public Class<?> loadClass(String className) throws ClassNotFoundException
     {
         if(className.contains("TestResourceHandler")) {
             System.out.println("Debugppint found");
         }
         ClassResource res = (ClassResource) WeavingContext.getInstance().getResource(className);
         if (res == null) return super.loadClass(className);
         if (!res.isTainted() && res.getAClass() != null) return res.getAClass();
         File target = resolveClassFile(className);
         //a load must happen anyway because the target was recompiled
         int fileLength;
         byte[] fileContent;
         FileInputStream iStream = null;
         //we cannot load while a compile is in progress
         //we have to wait until it is one
         try
         {
             fileLength = (int) target.length();
             fileContent = new byte[fileLength];
             iStream = new FileInputStream(target);
             int result = iStream.read(fileContent);
             _logger.log(Level.FINER, "read {0} bytes", String.valueOf(result));
         }
         catch (FileNotFoundException e)
         {
             throw new ClassNotFoundException(e.toString());
         }
         catch (IOException e)
         {
             throw new ClassNotFoundException(e.toString());
         }
         finally
         {
             if (iStream != null)
             {
                 try
                 {
                     iStream.close();
                 }
                 catch (Exception e)
                 {
                     Logger log = Logger.getLogger(this.getClass().getName());
                     log.log(SEVERE, "", e);
                 }
             }
         }
         //here we use trick17 we can store as many classes of the same name
         //as long as we store with a new classloader every time it needs refresh
         //we need to do it because the classloader can call itself recursively
         //TODO we might run into issues here with inner classes
         Class retVal;
         if (res != null) {
             retVal = (new ThrowAwayClassloader(getParent(),_untaint)).defineClass(className, fileContent, 0, fileLength);
             if(_untaint) {
                 res.setAClass(retVal);
                 res.setTainted(false);
             }
         } else {
             retVal = super.defineClass(className, fileContent, 0, fileLength);
         }
         return retVal;
 
     }
 
     @Override
     public InputStream getResourceAsStream(String name)
    {
        File resource = resolveClassFile(name);
         if (resource.exists())
         {
             try
             {
                 return new FileInputStream(resource);
             }
             catch (FileNotFoundException e)
             {
                 return super.getResourceAsStream(name);
             }
         }
         return super.getResourceAsStream(name);
     }
 
     private File resolveClassFile(String name)
     {
         return ClassUtils.classNameToFile(WeavingContext.getInstance().getConfiguration().getCompileTarget()
                 .getAbsolutePath
                 (), name);
 
     }
 
 }
