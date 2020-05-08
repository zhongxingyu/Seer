 package org.jasig.portal.plugin.mojo;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 
 import org.apache.commons.io.FileUtils;
 import org.apache.commons.io.IOUtils;
 import org.apache.maven.artifact.Artifact;
 import org.apache.maven.artifact.resolver.ArtifactNotFoundException;
 import org.apache.maven.artifact.resolver.ArtifactResolutionException;
 import org.apache.maven.plugin.MojoExecutionException;
 import org.apache.maven.plugin.MojoFailureException;
 import org.codehaus.plexus.archiver.ArchiverException;
 import org.codehaus.plexus.archiver.manager.NoSuchArchiverException;
 
 /**
  * 
  * @author Jen Bourey
  * @goal deploy-war
  */
 public class DeployWarMojo extends AbstractTomcatWarDeployerMojo {
 
 
     /**
      * @parameter
      * @required
      */
     private String artifactId;
     
     /**
      * @parameter
      * @required
      */
     private String artifactGroupId;
     
     /**
      * @parameter
      * @required
      */
     private String artifactVersion;
     
     /**
      * @parameter
      */
     private String artifactClassifier;
     
     /**
      * @parameter
      */
     private String contextName;
     
     private File ear;
     
     /**
      * @parameter default-value="false"
      */
     private boolean extractWars = false;
     
     /**
      * @parameter default-value="false"
      */
     private boolean removeExistingDirectories = false;
     
     /**
      * @parameter default-value="true"
      */
     private boolean useTempDir = true;
 
 
 	public void execute() throws MojoExecutionException, MojoFailureException {
 
         final File fullWebAppsDir = new File(getWebAppsDir());
         final org.apache.maven.plugin.logging.Log log = getLog();
         
         if (contextName.endsWith(".war")) {
             contextName = contextName.substring(contextName.length() - 4);
         }
         if (contextName.startsWith("/")) {
             contextName = contextName.substring(1);
         }
         
         Artifact artifact;
         if (artifactClassifier == null) {
             artifact = artifactFactory.createArtifact(artifactGroupId, artifactId, "default", artifactVersion, "war");
         } else {
             artifact = artifactFactory.createArtifactWithClassifier(artifactGroupId, artifactId, artifactVersion, "war", artifactClassifier);
         }
         try {
 			resolver.resolve( artifact, remoteRepositories, localRepository );
 	        File artifactFile = artifact.getFile();
 	        
 	        if (!this.extractWars) {
 		        // remove the currently-deployed webapp directory
 		        if (this.removeExistingDirectories) {
 		        	removeExisting(fullWebAppsDir, contextName);
 		        }
 		        
 	        	// copy the war file over to the webapps directory
 	        	File deployedFile = new File(fullWebAppsDir, contextName.concat(".war"));
 	            final InputStream jarEntryStream = new FileInputStream(artifactFile);
 	            try {
 	                final OutputStream jarOutStream = new FileOutputStream(deployedFile);
 	                try {
 	                    IOUtils.copy(jarEntryStream, jarOutStream);
 	                }
 	                finally {
 	                    IOUtils.closeQuietly(jarOutStream);
 	                }
 	            }
 	            finally {
 	                IOUtils.closeQuietly(jarEntryStream);
 	            }
 	        } else if (this.useTempDir) {
 	        	
 	        	// clear out and create temp directory
 		        File tmpDir = new File(System.getProperty("java.io.tmpdir"), "jasig");
 		        FileUtils.forceMkdir(tmpDir);
 		        File contextTmpDir = new File(tmpDir, contextName);
 		        FileUtils.deleteDirectory(contextTmpDir);
 		        FileUtils.forceMkdir(contextTmpDir);
 		        
 		        // unzip the war in the temporary location
 		        log.info("Extracting war for context " + contextName);
 		        extractWar(artifactFile, contextTmpDir, contextName);
 		        
 		        // remove the currently-deployed webapp directory
 		        if (this.removeExistingDirectories) {
 		        	removeExisting(fullWebAppsDir, contextName);
 		        }
 
 	            // copy the unzipped contents to the deploy location
 	            log.info("Copying extracted war");
 		        File contextDir = new File(fullWebAppsDir, contextName);
 	            FileUtils.moveDirectory(contextTmpDir, contextDir);
 	            
 	        } else {
 		        // remove the currently-deployed webapp directory
 		        if (this.removeExistingDirectories) {
 		        	removeExisting(fullWebAppsDir, contextName);
 		        }
 		        
 	        	// extract the war file to the webapps directory
 		        File contextDir = new File(fullWebAppsDir, contextName);
 		        extractWar(artifactFile, contextDir, contextName);
 	        }
 	        
 		} catch (ArtifactResolutionException ex) {
 			getLog().error("Failed to resolve artifact", ex);
			return;
 		} catch (ArtifactNotFoundException ex) {
			getLog().error("Failed to resolve artifact", ex);
			return;
 		} catch (IOException ex) {
 			getLog().error("Failed to deploy artifact", ex);
			return;
 		} catch (NoSuchArchiverException ex) {
 			getLog().error("Failed to unpack artifact", ex);
			return;
 		} catch (ArchiverException ex) {
 			getLog().error("Failed to unpack artifact", ex);
			return;
 		}
 
 	}
 	
 }
