 /*
  * Sonatype Application Build Lifecycle
  * Copyright (C) 2009 Sonatype, Inc.                                                                                                                          
  * 
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  * 
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see http://www.gnu.org/licenses/.
  *
  */
 package org.sonatype.maven.plugin.app.buildhelper;
 
 import org.apache.maven.artifact.Artifact;
 import org.apache.maven.artifact.handler.ArtifactHandler;
 import org.apache.maven.artifact.handler.manager.ArtifactHandlerManager;
 import org.apache.maven.artifact.handler.manager.DefaultArtifactHandlerManager;
 import org.apache.maven.artifact.versioning.ArtifactVersion;
 import org.apache.maven.artifact.versioning.DefaultArtifactVersion;
 import org.apache.maven.execution.MavenSession;
 import org.apache.maven.execution.RuntimeInformation;
 import org.apache.maven.plugin.Mojo;
 import org.apache.maven.plugin.MojoExecutionException;
 import org.apache.maven.plugin.logging.Log;
 import org.apache.maven.project.MavenProject;
 import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
 
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Set;
 
 /**
  * Injects an {@link ArtifactHandler} instance, loaded from build extensions, into the current project's
  * {@link Artifact} instance. The new handler is loaded using the project's packaging. This mojo compensates for bugs in
  * the build-extension handling of Maven versions prior to 2.2.1.
  * 
  * @goal inject-artifact-handler
  * @phase initialize
  */
 public class InjectArtifactHandlerMojo
     implements Mojo
 {
     /**
      * The current project instance.
      * 
      * @parameter default-value="${project}"
      * @required
      * @readonly
      */
     private MavenProject project;
 
     /**
      * The {@link ArtifactHandlerManager} into which any extension {@link ArtifactHandler} instances should have been
      * injected when the extensions were loaded.
      * 
      * @component
      */
     private ArtifactHandlerManager artifactHandlerManager;
 
     /**
      * @parameter default-value="${session}"
      * @required
      * @readonly
      */
     private MavenSession session;
 
     /**
      * @component
      */
     private RuntimeInformation ri;
 
     private Log log;
 
     @SuppressWarnings( "unchecked" )
     public void execute()
         throws MojoExecutionException
     {
         ArtifactVersion currentVersion = ri.getApplicationVersion();
        ArtifactVersion requiredVersion = new DefaultArtifactVersion( "2.2.1" );
        if ( requiredVersion.compareTo( currentVersion ) <= 0 )
         {
             getLog().debug(
                             "This version of Maven does not require injection of custom ArtifactHandlers using this code. Skipping." );
             return;
         }
 
         Map<String, ?> handlerDescriptors = session.getContainer().getComponentDescriptorMap( ArtifactHandler.ROLE );
         if ( handlerDescriptors != null )
         {
             getLog().debug( "Registering all unregistered ArtifactHandlers..." );
 
             if ( artifactHandlerManager instanceof DefaultArtifactHandlerManager )
             {
                 Set<String> existingHints =
                     ( (DefaultArtifactHandlerManager) artifactHandlerManager ).getHandlerTypes();
                 if ( existingHints != null )
                 {
                     for ( String hint : existingHints )
                     {
                         handlerDescriptors.remove( hint );
                     }
                 }
             }
 
             if ( handlerDescriptors.isEmpty() )
             {
                 getLog().debug( "All ArtifactHandlers are registered. Continuing..." );
             }
             else
             {
                 Map<String, ArtifactHandler> unregisteredHandlers =
                     new HashMap<String, ArtifactHandler>( handlerDescriptors.size() );
 
                 for ( String hint : handlerDescriptors.keySet() )
                 {
                     try
                     {
                         unregisteredHandlers.put( hint, (ArtifactHandler) session.lookup( ArtifactHandler.ROLE, hint ) );
                         getLog().info( "Adding ArtifactHandler for: " + hint );
                     }
                     catch ( ComponentLookupException e )
                     {
                         getLog().warn(
                                        "Failed to lookup ArtifactHandler with hint: " + hint + ". Reason: "
                                            + e.getMessage(), e );
                     }
                 }
 
                 artifactHandlerManager.addHandlers( unregisteredHandlers );
             }
         }
 
         getLog().debug( "...done.\nSetting ArtifactHandler on project-artifact: " + project.getId() + "..." );
 
         Set<Artifact> artifacts = new HashSet<Artifact>();
         artifacts.add( project.getArtifact() );
 
         Set<Artifact> dependencyArtifacts = project.getDependencyArtifacts();
         if ( dependencyArtifacts != null && !dependencyArtifacts.isEmpty() )
         {
             artifacts.addAll( dependencyArtifacts );
         }
 
         for ( Artifact artifact : artifacts )
         {
             String type = artifact.getType();
             ArtifactHandler handler = artifactHandlerManager.getArtifactHandler( type );
 
             getLog().debug(
                            "Artifact: " + artifact.getId() + "\nType: " + type + "\nArtifactHandler extension: "
                                + handler.getExtension() );
 
             artifact.setArtifactHandler( handler );
         }
 
         getLog().debug( "...done." );
     }
 
     public Log getLog()
     {
         return log;
     }
 
     public void setLog( final Log log )
     {
         this.log = log;
     }
 
 }
