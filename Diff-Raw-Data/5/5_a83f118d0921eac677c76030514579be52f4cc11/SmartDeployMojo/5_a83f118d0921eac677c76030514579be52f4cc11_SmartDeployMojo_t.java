 package org.smart.plugin;
 
 /*
  * Copyright 2001-2005 The Apache Software Foundation.
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
 
 import org.apache.maven.plugin.AbstractMojo;
 import org.apache.maven.plugin.MojoExecutionException;
 
 import org.apache.maven.plugins.annotations.LifecyclePhase;
 import org.apache.maven.plugins.annotations.Mojo;
 import org.apache.maven.plugins.annotations.Execute;
 import org.apache.maven.plugins.annotations.Parameter;
 import org.apache.maven.plugins.annotations.ResolutionScope;
 
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 
 @Mojo( name = "deploy" )
 @Execute( phase = LifecyclePhase.PACKAGE )
 public class SmartDeployMojo
     extends AbstractMojo
 {
     /**
      * Location of the file.
      */
     @Parameter( defaultValue = "${project.build.directory}/${project.build.finalName}.jar", property = "maven.smart.jarFile", required = true )
     private File jarFile;
 
     @Parameter( property = "maven.smart.port", defaultValue = "9081", required = true )
     private int port;
 
     @Parameter( property = "maven.smart.server", defaultValue = "localhost", required = true )
     private String server;
 
     @Parameter( property = "maven.smart.user", defaultValue = "smartadmin", required = true )
     private String user;
 
     @Parameter( property = "maven.smart.password", defaultValue = "smartadmin", required = true )
     private String password;
 
     @Parameter( property = "maven.smart.flowsoa", required = true )
     private String flowsoa;
 
     public void execute()
         throws MojoExecutionException
     {
         File f = jarFile;
 
         SecureSmartClient clnt = null;
 
         try
         {
             clnt = new SecureSmartClient(port, server, "", "", flowsoa);
             clnt.authenticateAdmin(user, password);
            String str = jarFile.getAbsolutePath();
            str = str.replaceAll("\\\\", "\\/");
            System.out.println("File Name is: " + str);
            clnt.deployJar(str, flowsoa);
         }
         catch ( Exception e )
         {
             throw new MojoExecutionException( "Error deploying file " + jarFile, e );
         }
     }
 }
