 /**
  * Elastic Grid
 * Copyright (C) 2008-2009 Elastic Grid, LLC.
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Affero General Public License as
  * published by the Free Software Foundation, either version 3 of the
  * License, or (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU Affero General Public License for more details.
  *
  * You should have received a copy of the GNU Affero General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package com.elasticgrid.maven;
 
 import org.apache.maven.artifact.Artifact;
 import org.apache.maven.artifact.metadata.ArtifactMetadataSource;
 import org.apache.maven.artifact.repository.ArtifactRepository;
 import org.apache.maven.artifact.resolver.ArtifactNotFoundException;
 import org.apache.maven.artifact.resolver.ArtifactResolutionException;
 import org.apache.maven.artifact.resolver.ArtifactResolutionResult;
 import org.apache.maven.artifact.resolver.ArtifactResolver;
 import org.apache.maven.artifact.resolver.filter.ArtifactFilter;
 import org.apache.maven.artifact.resolver.filter.ScopeArtifactFilter;
 import org.apache.maven.plugin.AbstractMojo;
 import org.apache.maven.plugin.MojoExecutionException;
 import org.apache.maven.plugin.MojoFailureException;
 import org.apache.maven.project.MavenProject;
 import java.io.File;
 import java.util.List;
 import java.util.Set;
 
 /**
  * Deploys an Elastic Grid OpString to the repository.
  *
  * @goal deploy
 * @execute goal="oar" phase="package"
  * @description Deploys an Elastic Grid OpString to the repository
  * @requiresProject true
  * @requiresDependencyResolution
  */
 public class DeployMojo extends AbstractMojo {
     /**
      * The OAR to generate.
      *
      * @parameter expression="${project.build.directory}/${project.build.finalName}.oar"
      * @required
      */
     private String oarFileName;
 
     /**
      * The maven project.
      *
      * @parameter expression="${project}"
      * @required
      * @readonly
      */
     private MavenProject project;
 
     /**
      * Dependency artifacts
      *
      * @parameter expression="${project.dependencyArtifacts}"
      */
     private Set<Artifact> dependencies;
 
     /** @component */
     private ArtifactResolver resolver;
 
     /** @component */
     private ArtifactMetadataSource artifactMetadataSource;
 
     /** @parameter default-value="${localRepository}" */
     private ArtifactRepository localRepository;
 
     /** @parameter default-value="${project.remoteArtifactRepositories}" */
     private List remoteRepositories;
 
     @SuppressWarnings("unchecked")
     public void execute() throws MojoExecutionException, MojoFailureException {
         File oar = new File(oarFileName);
         getLog().info("Deploying oar " + oar.getName() + "...");
         // TODO: copy the OAR to the dropBucket
         ArtifactFilter filter = new ScopeArtifactFilter(Artifact.SCOPE_RUNTIME);
         try {
             ArtifactResolutionResult result = resolver.resolveTransitively(
                     dependencies, project.getArtifact(),
                     localRepository, remoteRepositories,
                     artifactMetadataSource, filter);
             Set<Artifact> artifacts = result.getArtifacts();
 
             for (Artifact artifact : artifacts) {
                 getLog().info("Detected dependency: " + artifact + " available in " + artifact.getFile());
                 // TODO: this is where the actual copy to the remote maven repository should occur!
             }
         } catch (ArtifactResolutionException e) {
             throw new MojoFailureException(e, "Can't resolve artifact", "Can't resolve artifact");
         } catch (ArtifactNotFoundException e) {
             throw new MojoFailureException(e, "Can't find artifact", "Can't find artifact");
         }
 
         List<Artifact> attachments = project.getAttachedArtifacts();
         getLog().info("Found " + attachments.size() + " attachments");
         for (Artifact artifact : attachments) {
             getLog().info("Detected attachment: " + artifact + " available in " + artifact.getFile());
             // TODO: copy the artifacts to the maven repo too!
         }
     }
 }
