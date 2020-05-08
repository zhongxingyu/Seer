 package com.pressassociation.maven.wmb.mojo;
 
 import com.ibm.broker.config.proxy.*;
 import com.pressassociation.maven.wmb.types.BrokerArtifact;
 import org.apache.maven.artifact.factory.ArtifactFactory;
 import org.apache.maven.artifact.repository.ArtifactRepository;
 import org.apache.maven.artifact.resolver.ArtifactNotFoundException;
 import org.apache.maven.artifact.resolver.ArtifactResolutionException;
 import org.apache.maven.artifact.resolver.ArtifactResolver;
 import org.apache.maven.plugin.AbstractMojo;
 import org.apache.maven.plugin.MojoExecutionException;
 import org.apache.maven.plugin.MojoFailureException;
 import org.apache.maven.project.MavenProject;
 import org.jfrog.maven.annomojo.annotations.MojoComponent;
 import org.jfrog.maven.annomojo.annotations.MojoGoal;
 import org.jfrog.maven.annomojo.annotations.MojoParameter;
 import org.jfrog.maven.annomojo.annotations.MojoRequiresDependencyResolution;
 
 import javax.inject.Provider;
 import java.io.IOException;
import java.util.Enumeration;
 import java.util.List;
 
 /**
  * @author Bob Browning <bob.browning@pressassociation.com>
  */
 @MojoGoal("deploy")
 @MojoRequiresDependencyResolution
 public class ConfiguredDeployMojo extends AbstractMojo {
 
     private static final int BROKER_TIMEOUT = 30000;
 
     /**
      * Set of deployable artifacts.
      */
     @MojoParameter(required = true, readonly = true)
     private BrokerArtifact[] artifacts;
 
     /**
      * Artifact factory component.
      */
     @MojoComponent
     private ArtifactFactory artifactFactory;
 
     /**
      * Artifact resolver component.
      */
     @MojoComponent
     private ArtifactResolver resolver;
 
     /**
      * List of remote repositories.
      */
     @MojoParameter(expression = "${project.remoteArtifactRepositories}", required = true, readonly = true)
     private List remoteRepositories;
 
     /**
      * Local repository.
      */
     @MojoParameter(expression = "${localRepository}", required = true, readonly = true)
     private ArtifactRepository localRepository;
 
     /**
      * Maven project.
      */
     @MojoParameter(expression = "${project}", required = true, readonly = true)
     private MavenProject project;
 
     /**
      * Hostname of server to which BAR files will be deployed.
      */
     @MojoParameter(expression = "${wmb.host}", defaultValue = "localhost")
     private String hostname;
 
     /**
      * Port on which to connect to server.
      */
     @MojoParameter(expression = "${wmb.port}", defaultValue = "1414")
     private int port;
 
     /**
      * Queue Manager to use when connecting to Message Broker.
      */
     @MojoParameter(expression = "${wmb.queueMgr}", required = true)
     private String queueMgr;
 
     /**
      * Connection parameters provider.
      */
     private Provider<MQBrokerConnectionParameters> connectionParameters = new Provider<MQBrokerConnectionParameters>() {
         MQBrokerConnectionParameters parameters;
         @Override
         public MQBrokerConnectionParameters get() {
             if(parameters == null) {
                 parameters = new MQBrokerConnectionParameters(hostname, port, queueMgr);
             }
             return parameters;
         }
     };
 
     @Override
     public void execute() throws MojoExecutionException, MojoFailureException {
         BrokerProxy proxy;
         try {
             proxy = BrokerProxy.getInstance(connectionParameters.get());
         } catch (ConfigManagerProxyLoggedException e) {
             throw propagateMojoExecutionException(e);
         }
 
         for(BrokerArtifact artifact : artifacts) {
             artifact.setArtifactFactory(artifactFactory);
 
             if(artifact.getExecutionGroup() == null) {
                 throw new MojoExecutionException("Missing required execution group value for artifact " + artifact);
             }
 
             getLog().info("Deploying " + artifact + " to " + hostname + ":" + port + "/" + queueMgr + "/"
                     + artifact.getExecutionGroup());
 
             try {
                 resolver.resolve(artifact, remoteRepositories, localRepository);
             } catch (ArtifactResolutionException e) {
                 throw propagateMojoExecutionException(e);
             } catch (ArtifactNotFoundException e) {
                 throw propagateMojoExecutionException(e);
             }
 
             try {
                 ExecutionGroupProxy executionGroupProxy = proxy.getExecutionGroupByName(artifact.getExecutionGroup());
                 DeployResult deployResult = executionGroupProxy.deploy(artifact.getFile().getPath(), true, BROKER_TIMEOUT);
                 if(deployResult.getCompletionCode() != CompletionCodeType.success) {
                    Enumeration<LogEntry> responses = deployResult.getDeployResponses();
                    while(responses.hasMoreElements()) {
                        getLog().error(responses.nextElement().getDetail());
                    }
                    throw new MojoExecutionException("Error deploying broker archive.");
                 }
             } catch (ConfigManagerProxyPropertyNotInitializedException e) {
                 throw propagateMojoExecutionException(e);
             } catch (ConfigManagerProxyLoggedException e) {
                 throw propagateMojoExecutionException(e);
             } catch (IOException e) {
                 throw propagateMojoExecutionException(e);
             }
         }
     }
 
     /**
      * Propagate exception messages as MojoExecutionException instances.
      *
      * @param t Throwable to be wrapped.
      * @return MojoExecutionException instance wrapping t.
      */
     private static MojoExecutionException propagateMojoExecutionException(Throwable t) {
         if(t instanceof MojoExecutionException) {
             return (MojoExecutionException) t;
         }
         return new MojoExecutionException(t.getMessage(), t);
     }
 }
