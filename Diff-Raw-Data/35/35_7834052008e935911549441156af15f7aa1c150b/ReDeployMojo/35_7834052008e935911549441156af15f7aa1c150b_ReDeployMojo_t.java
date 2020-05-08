 package org.codehaus.mojo.jboss;
 
 import java.util.Iterator;
 
 import org.apache.maven.plugin.MojoExecutionException;
 
 /**
  * ReDeploys a directory or file to JBoss via JMX.
  * 
  * @author <a href="mailto:jgenender@apache.org">Jeff Genender</a>
  * @goal redeploy
  */
 public class ReDeployMojo
     extends AbstractDeployerMojo
 {
 
     /**
      * The redeployment URL.
      * 
      * @parameter expression="/jmx-console/HtmlAdaptor?action=invokeOpByName&name=jboss.system:service%3DMainDeployer&methodName=redeploy&argType=java.net.URL&arg0="
      * @required
      */
     protected String redeployUrlPath;
 
     public void execute()
         throws MojoExecutionException
     {
 
         if ( fileNames == null )
        {
            getLog().info( "No files configured to deploy." );
             return;
        }
 
         Iterator iter = fileNames.iterator();
         while ( iter.hasNext() )
         {
             String fileName = (String) iter.next();
             String fixedFile = null;
             if ( fileName.toLowerCase().endsWith( "ejb" ) )
             {
                // Fix the ejb packaging to a jar
                 fixedFile = fileName.substring( 0, fileName.length() - 3 ) + "jar";
             }
             else
             {
                 fixedFile = fileName;
             }
 
             getLog().info( "Reploying " + fixedFile + " to JBoss." );
             String url = "http://" + hostName + ":" + port + redeployUrlPath + fixedFile;
             doURL( url );
         }
     }
 }
