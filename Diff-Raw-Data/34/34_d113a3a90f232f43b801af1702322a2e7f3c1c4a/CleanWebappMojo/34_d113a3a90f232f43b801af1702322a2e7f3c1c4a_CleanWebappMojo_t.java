 package org.jasig.portal.plugin.mojo;
 
 import java.io.File;
 import java.io.IOException;
 
 import org.apache.commons.io.FileUtils;
 import org.apache.maven.plugin.MojoExecutionException;
 import org.apache.maven.plugin.MojoFailureException;
 
 /**
  * 
  * @author Jen Bourey
  * $Revision$
  * 
  * @goal clean-webapp
  */
 public class CleanWebappMojo extends AbstractTomcatMojo {
 
 	/**
 	 * @param
 	 * @required
 	 */
 	private String contextName;
 	
 	public void execute() throws MojoExecutionException, MojoFailureException {
         final File contextDir = new File(getWebAppsDir(), contextName);
         
         if (contextDir.exists()) {
             try {
 				FileUtils.deleteDirectory(contextDir);
 			} catch (IOException ex) {
 				getLog().error("Unable to delete directory for context " + contextName, ex);
	            throw new MojoFailureException("Failed to delete directory for context " + contextName);  				
 			}
         }
 	}
 
 }
