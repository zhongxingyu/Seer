 package org.jasig.portal.plugin.mojo;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.List;
 
 import org.apache.maven.artifact.Artifact;
 import org.apache.maven.artifact.factory.ArtifactFactory;
 import org.apache.maven.artifact.repository.ArtifactRepository;
 import org.apache.maven.artifact.resolver.ArtifactNotFoundException;
 import org.apache.maven.artifact.resolver.ArtifactResolutionException;
 import org.apache.maven.artifact.resolver.ArtifactResolver;
 import org.apache.maven.plugin.MojoExecutionException;
 import org.apache.maven.plugin.MojoFailureException;
 import org.apache.tools.ant.BuildException;
 import org.jasig.portal.plugin.deployer.config.TomcatDeployerConfig;
 import org.jasig.portal.plugin.deployer.config.TomcatEarDeployer;
 
 /**
  * @goal deploy-ear
  * @requiresDependencyResolution
  */
 public class DeployEarMojo extends AbstractTomcatMojo {
 
     /** @component */
     private ArtifactFactory artifactFactory;
 
     /** @component */
     private ArtifactResolver resolver;
 
     /**@parameter expression="${localRepository}" */
     private ArtifactRepository localRepository;
 
     /** @parameter expression="${project.remoteArtifactRepositories}" */
     private List remoteRepositories;
 
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
     
     
     private File ear;
     
     /**
      * @parameter default-value="false"
      */
     private boolean extractWars = false;
     
     /**
      * @parameter default-value="false"
      */
     private boolean removeExistingDirectories = false;
 
 
     public ArtifactFactory getArtifactFactory() {
 		return this.artifactFactory;
 	}
 
 	public void setArtifactFactory(ArtifactFactory artifactFactory) {
 		this.artifactFactory = artifactFactory;
 	}
 
 	public ArtifactResolver getResolver() {
 		return this.resolver;
 	}
 
 	public void setResolver(ArtifactResolver resolver) {
 		this.resolver = resolver;
 	}
 
 	public ArtifactRepository getLocalRepository() {
 		return this.localRepository;
 	}
 
 	public void setLocalRepository(ArtifactRepository localRepository) {
 		this.localRepository = localRepository;
 	}
 
 	public List getRemoteRepositories() {
 		return this.remoteRepositories;
 	}
 
 	public void setRemoteRepositories(List remoteRepositories) {
 		this.remoteRepositories = remoteRepositories;
 	}
 
 	public String getArtifactId() {
 		return this.artifactId;
 	}
 
 	public void setArtifactId(String artifactId) {
 		this.artifactId = artifactId;
 	}
 
 	public String getArtifactGroupId() {
 		return this.artifactGroupId;
 	}
 
 	public void setArtifactGroupId(String artifactGroupId) {
 		this.artifactGroupId = artifactGroupId;
 	}
 
 	public String getArtifactVersion() {
 		return this.artifactVersion;
 	}
 
 	public void setArtifactVersion(String artifactVersion) {
 		this.artifactVersion = artifactVersion;
 	}
 
 	public String getArtifactClassifier() {
 		return this.artifactClassifier;
 	}
 
 	public void setArtifactClassifier(String artifactClassifier) {
 		this.artifactClassifier = artifactClassifier;
 	}
 
 	public File getEar() {
         return this.ear;
     }
     
     public void setEar(File ear) {
         this.ear = ear;
     }
 
     public boolean isExtractWars() {
         return this.extractWars;
     }
     
     public void setExtractWars(boolean extractWars) {
         this.extractWars = extractWars;
     }
 
     public boolean isRemoveExistingDirectories() {
         return this.removeExistingDirectories;
     }
     
     public void setRemoveExistingDirectories(boolean removeExistingDirectories) {
         this.removeExistingDirectories = removeExistingDirectories;
     }
 
 
 	public void execute() throws MojoExecutionException, MojoFailureException {
         final TomcatDeployerConfig config = new TomcatDeployerConfig();
 
         final File fullJarDir = new File(getJarDir());
         config.setJarDir(fullJarDir);
         
         final File fullWebAppsDir = new File(getWebAppsDir());
         config.setWebAppsDir(fullWebAppsDir);
         
         Artifact artifact;
         getLog().info("Using classifier: " + artifactClassifier);
         artifact = artifactFactory.createArtifactWithClassifier(artifactGroupId, artifactId, artifactVersion, "ear", artifactClassifier);
         try {
 			resolver.resolve( artifact, remoteRepositories, localRepository );
 	        File artifactFile = artifact.getFile();
 	        config.setEarLocation(artifactFile);
 		} catch (ArtifactResolutionException ex) {
 			getLog().error("Failed to resolve artifact", ex);
			throw new MojoFailureException("Failed to resolve artifact "+artifact);			
 		} catch (ArtifactNotFoundException ex) {
			getLog().error("Failed to find artifact", ex);
			throw new MojoFailureException("Failed to find artifact "+artifact);  
 		}
 
         config.setExtractWars(this.isExtractWars());
         config.setRemoveExistingDirectories(this.isRemoveExistingDirectories());
 
         final TomcatEarDeployer deployer = new TomcatEarDeployer();
         try {
             deployer.deployEar(config);
         }
         catch (IOException e) {
             throw new BuildException(e);
         }
     }
 
 }
