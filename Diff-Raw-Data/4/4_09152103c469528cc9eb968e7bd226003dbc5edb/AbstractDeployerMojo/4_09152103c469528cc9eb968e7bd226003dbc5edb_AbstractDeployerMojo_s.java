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
 
 import java.io.BufferedReader;
 import java.io.InputStreamReader;
 import java.net.HttpURLConnection;
 import java.net.URL;
 import java.util.List;
 
 import org.apache.commons.codec.binary.Base64;
 import org.apache.maven.artifact.manager.WagonManager;
 import org.apache.maven.plugin.AbstractMojo;
 import org.apache.maven.plugin.MojoExecutionException;
 import org.apache.maven.wagon.authentication.AuthenticationInfo;
 
 /**
  * Created by IntelliJ IDEA. User: jeffgenender Date: Oct 1, 2005 Time: 1:36:05 PM To change this template use File |
  * Settings | File Templates.
  */
 public abstract class AbstractDeployerMojo
     extends AbstractMojo
 {
 
     /**
      * The default username to use when authenticating with Tomcat manager.
      */
     private static final String DEFAULT_USERNAME = "admin";
 
     /**
      * The default password to use when authenticating with Tomcat manager.
      */
     private static final String DEFAULT_PASSWORD = "";
 
     /**
      * The port JBoss is running on.
      * 
      * @parameter expression="8080"
      * @required
      */
     protected int port;
 
     /**
      * The host JBoss is running on.
      * 
      * @parameter expression="localhost"
      * @required
      */
     protected String hostName;
 
     /**
      * The name of the file or directory to deploy or undeploy.
      * 
      * @parameter
      */
     protected List fileNames;
 
     /**
      * The Maven Wagon manager to use when obtaining server authentication details.
      * 
     * @parameter expression = "${component.org.apache.maven.artifact.manager.WagonManager}"
     * @required
     * @readonly
      */
     private WagonManager wagonManager;
 
     /**
      * The server id to use when authenticating with Tomcat manager, or <code>null</code> to use defaults.
      * 
      * @parameter
      */
     private String server;
 
     protected void doURL( String url )
         throws MojoExecutionException
     {
         try
         {
 
             url = url.replaceAll( "\\s", "%20" );
 
             getLog().debug( "url = " + url );
 
             HttpURLConnection connection = (HttpURLConnection) new URL( url ).openConnection();
             connection.setInstanceFollowRedirects( false );
             connection.setRequestProperty( "Authorization", toAuthorization() );
 
             BufferedReader reader = new BufferedReader( new InputStreamReader( connection.getInputStream() ) );
             reader.readLine();
             reader.close();
         }
         catch ( Exception e )
         {
             throw new MojoExecutionException( "Mojo error occurred: " + e.getMessage(), e );
         }
     }
 
     /**
      * Gets the HTTP Basic Authorization header value for the supplied username and password.
      * 
      * @return the HTTP Basic Authorization header value
      * @throws MojoExecutionException
      */
     private String toAuthorization()
         throws MojoExecutionException
     {
         String userName;
         String password;
 
         if ( server == null )
         {
             // no server set, use defaults
             getLog().info( "No server specified for authentication - using defaults" );
             userName = DEFAULT_USERNAME;
             password = DEFAULT_PASSWORD;
         }
         else
         {
             // obtain authenication details for specified server from wagon
             AuthenticationInfo info = wagonManager.getAuthenticationInfo( server );
             if ( info == null )
             {
                 throw new MojoExecutionException( "Server not defined in settings.xml: " + server );
             }
 
             // derive username
             userName = info.getUserName();
             if ( userName == null )
             {
                 getLog().info( "No server username specified - using default" );
                 userName = DEFAULT_USERNAME;
             }
 
             // derive password
             password = info.getPassword();
             if ( password == null )
             {
                 getLog().info( "No server password specified - using default" );
                 password = DEFAULT_PASSWORD;
             }
         }
 
         StringBuffer buffer = new StringBuffer();
         buffer.append( userName ).append( ':' );
         if ( password != null )
         {
             buffer.append( password );
         }
         return "Basic " + new String( Base64.encodeBase64( buffer.toString().getBytes() ) );
     }
 
 }
