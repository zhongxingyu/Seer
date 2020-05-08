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
 
 import java.io.UnsupportedEncodingException;
 import java.net.URLEncoder;
 import java.util.Iterator;
 
 import org.apache.maven.plugin.MojoExecutionException;
 
 /**
  * Deploys a directory or file to JBoss via JMX.
  * 
  * @author <a href="mailto:jgenender@apache.org">Jeff Genender</a>
  * @goal deploy
  */
 public class DeployMojo
     extends AbstractDeployerMojo
 {
     /**
      * The deployment URL.
      * 
      * @parameter expression="/jmx-console/HtmlAdaptor?action=invokeOpByName&name=jboss.system:service%3DMainDeployer&methodName=deploy&argType=java.net.URL&arg0="
      * @required
      */
     protected String deployUrlPath;
 
     /**
      * The character encoding for the fileName.
      * 
      * @parameter expression="UTF-8"
      */
     protected String fileNameEncoding;
 
     public void execute()
         throws MojoExecutionException
     {
 
        // Fix the ejb packaging to a jar

         if ( fileNames == null )
         {
             return;
         }
         Iterator iter = fileNames.iterator();
         while ( iter.hasNext() )
         {
 
             String fileName = (String) iter.next();
             String fixedFile = null;
             if ( fileName.toLowerCase().endsWith( "ejb" ) )
             {
                 fixedFile = fileName.substring( 0, fileName.length() - 3 ) + "jar";
             }
             else
             {
                 fixedFile = fileName;
             }
 
             try
             {
                 String encoding = fileNameEncoding;
                 if ( encoding == null )
                 {
                     encoding = "UTF-8";
                 }
                 fixedFile = URLEncoder.encode( fixedFile, encoding );
             }
             catch ( UnsupportedEncodingException ex )
             {
                 throw new MojoExecutionException( ex.getMessage() );
             }
 
             getLog().info( "Deploying " + fixedFile + " to JBoss." );
             String url = "http://" + hostName + ":" + port + deployUrlPath + fixedFile;
             doURL( url );
         }
     }
 }
