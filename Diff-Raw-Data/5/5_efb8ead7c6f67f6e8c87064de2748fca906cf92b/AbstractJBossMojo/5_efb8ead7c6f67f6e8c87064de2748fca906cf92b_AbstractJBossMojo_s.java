 /*
  * Copyright 2005 Jeff Genender.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package org.codehaus.mojo.jboss;
 
 import org.apache.maven.plugin.AbstractMojo;
 import org.apache.maven.plugin.MojoExecutionException;
 
 import org.apache.commons.lang.SystemUtils;
 
 import java.net.HttpURLConnection;
 import java.net.URL;
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 
 /**
  * Created by IntelliJ IDEA. User: jeffgenender Date: Oct 1, 2005 Time: 12:12:56
  * PM To change this template use File | Settings | File Templates.
  */
 public abstract class AbstractJBossMojo extends AbstractMojo {
 
     /**
      * The location to JBoss Home. This is a required configuration parameter
      * (unless JBOSS_HOME is set).
      * 
      * @parameter expression="ENV"
      * @required
      */
     protected String jbossHome;
 
     /**
      * @parameter expression="${project.build.directory}/jboss
      */
     protected File outputDirectory;
 
     /**
      * The server name
      * 
      * @parameter expression="default"
      * @required
      */
     protected String serverName;
 
     protected void checkConfig() throws MojoExecutionException {
         if (jbossHome == null || jbossHome.equals("ENV")) {
             if (SystemUtils.getJavaVersion() < 1.5)
             {
                 throw new MojoExecutionException(
                         "Neither JBOSS_HOME nor the jbossHome configuration parameter is set! Also, to save you the trouble, JBOSS_HOME cannot be read running a VM < 1.5, so set the jbossHome configuration parameter or use -D.");
             }
             jbossHome = System.getenv("JBOSS_HOME");
         }
 
         if (jbossHome == null) {
             throw new MojoExecutionException(
                     "Neither JBOSS_HOME nor the jbossHome configuration parameter is set!");
         }
     }
 
     protected void launch(String fName, String params)
         throws MojoExecutionException {
 
         try {
             checkConfig();
             String osName = System.getProperty("os.name");
             Runtime runtime = Runtime.getRuntime();
 
             Process p = null;
             if (osName.startsWith("Windows")) {
                 String command[] = {
                     "cmd.exe",
                     "/C",
                    "cd " + outputDirectory.getAbsolutePath() + "\\bin & "
                         + fName + ".bat " + " " + params };
                     p = runtime.exec(command);
                     dump(p.getInputStream());
                     dump(p.getErrorStream());
             } else {
                 String command[] = {
                     "sh",
                     "-c",
                    "cd " + outputDirectory.getAbsolutePath() + "/bin; ./"
                         + fName + ".sh " + " " + params };
                     p = runtime.exec(command);
             }
 
         } catch (Exception e) {
             throw new MojoExecutionException("Mojo error occurred: "
                     + e.getMessage(), e);
         }
     }
 
     protected void dump(final InputStream a_input) {
         new Thread(new Runnable() {
             public void run() {
                 try {
                     byte[] b = new byte[1000];
                     while ((a_input.read(b)) != -1) {
                     }
                 } catch (IOException e) {
                     e.printStackTrace();
                 }
             }
         }).start();
     }
 
 }
