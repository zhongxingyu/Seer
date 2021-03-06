 /**
  * Licensed to the Apache Software Foundation (ASF) under one or more
  * contributor license agreements.  See the NOTICE file distributed with
  * this work for additional information regarding copyright ownership.
  * The ASF licenses this file to You under the Apache License, Version 2.0
  * (the "License"); you may not use this file except in compliance with
  * the License.  You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  *  Unless required by applicable law or agreed to in writing, software
  *  distributed under the License is distributed on an "AS IS" BASIS,
  *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *  See the License for the specific language governing permissions and
  *  limitations under the License.
  */
 package org.apache.openejb.tck.impl;
 
 import java.io.Closeable;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.Flushable;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.Properties;
 
 import javax.naming.Context;
 import javax.naming.InitialContext;
 
 import org.apache.openejb.assembler.Deployer;
 import org.apache.openejb.assembler.classic.AppInfo;
 import org.apache.openejb.client.RemoteInitialContextFactory;
 import org.apache.openejb.config.Deploy;
 import org.apache.openejb.config.RemoteServer;
 import org.apache.openejb.loader.Options;
 import org.jboss.testharness.api.DeploymentException;
 import org.jboss.testharness.spi.Containers;
 
 /**
  * @version $Rev$ $Date$
  */
 public class ContainersImplTomEE implements Containers {
     private static int count = 0;
     private final RemoteServer server;
     private Deployer deployer = null;
     private static final String tmpDir = System.getProperty("java.io.tmpdir");
     private Exception exception;
     private AppInfo appInfo;
 
     public ContainersImplTomEE() {
         System.out.println("Initialized ContainersImplTomEE " + (++count));
         server = new RemoteServer(20, true);
     }
     private Deployer lookup() {
         final Options options = new Options(System.getProperties());
         Properties props = new Properties();
         props.put(Context.INITIAL_CONTEXT_FACTORY, RemoteInitialContextFactory.class.getName());
        String port = System.getProperty("server.http.port");
        if (port != null) {
            props.put(Context.PROVIDER_URL, options.get(Context.PROVIDER_URL,"http://localhost:" + port + "/openejb/ejb"));
        } else {
            throw new RuntimeException("Please set the tomee port as a system property");
        }
         try {
             InitialContext context = new InitialContext(props);
             return (Deployer) context.lookup("openejb/DeployerBusinessRemote");
         } catch (Exception e) {
             throw new RuntimeException(e);
         }
     }
     @Override
     public boolean deploy(InputStream archive, String name) throws IOException {
         exception = null;
         appInfo = null;
 
         System.out.println("Deploying " + archive + " with name " + name);
 
         File fileName = getFile(name);
         System.out.println(fileName);
         writeToFile(fileName, archive);
         try {
             if (deployer == null) {
                 deployer = lookup();
             }
             appInfo = deployer.deploy(fileName.getAbsolutePath());
         } catch (Exception e) {
 
             if (name.contains(".broken.")) {
                 // Tests that contain the name '.broken.' are expected to fail deployment
                 // This is how the TCK verifies the container is doing the required error checking
                 exception = (DeploymentException) new DeploymentException("deploy failed").initCause(e);
             } else {
                 // This on the other hand is not good ....
                 System.out.println("FIX Deployment of " + name);
                 e.printStackTrace();
                 exception = e;
             }
 
             return false;
         }
         return true;
     }
 
     private void writeToFile(File file, InputStream archive) {
         try {
             FileOutputStream fos = new FileOutputStream(file);
             byte[] buffer = new byte[4096];
             int bytesRead = -1;
             while ((bytesRead = archive.read(buffer)) > -1) {
                 fos.write(buffer, 0, bytesRead);
             }
             Util.close(fos);
         } catch (Exception e) {
             throw new RuntimeException(e);
         }
     }
 
     private File getFile(String name) {
         final File dir = new File(tmpDir, Math.random()+"");
         dir.mkdir();
 
         return new File(dir, name);
     }
 
     @Override
     public DeploymentException getDeploymentException() {
         try {
             return (DeploymentException) exception;
         } catch (Exception e) {
             System.out.println("BADCAST");
             return new DeploymentException("", exception);
         }
     }
 
     @Override
     public void undeploy(String name) throws IOException {
         if (appInfo == null) {
             if (!(exception instanceof DeploymentException)) {
                 System.out.println("Nothing to undeploy" + name);
             }
             return;
         }
 
         System.out.println("Undeploying " + name);
         try {
             deployer.undeploy(appInfo.path);
         } catch (Exception e) {
             e.printStackTrace();
             throw new RuntimeException(e);
         }
     }
     @Override
     public void setup() throws IOException {
         System.out.println("Setup called");
         server.start();
     }
     @Override
     public void cleanup() throws IOException {
         System.out.println("Cleanup called");
         server.stop();
     }
     private static final class Util {
         static void close(Closeable closeable) throws IOException {
             if (closeable == null)
                 return;
             try {
                 if (closeable instanceof Flushable) {
                     ((Flushable) closeable).flush();
                 }
             } catch (IOException e) {
                 // no-op
             }
             try {
                 closeable.close();
             } catch (IOException e) {
                 // no-op
             }
         }
     }
 }
