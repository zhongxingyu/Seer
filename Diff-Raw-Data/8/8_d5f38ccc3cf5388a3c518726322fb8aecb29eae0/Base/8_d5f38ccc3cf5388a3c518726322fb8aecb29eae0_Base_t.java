 /**
  * Copyright 1&1 Internet AG, https://github.com/1and1/
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package net.oneandone.maven.plugins.prerelease;
 
 import net.oneandone.maven.plugins.prerelease.util.Maven;
 import net.oneandone.sushi.fs.World;
 import org.apache.maven.artifact.repository.ArtifactRepository;
 import org.apache.maven.execution.MavenSession;
 import org.apache.maven.plugin.AbstractMojo;
 import org.apache.maven.plugin.MojoExecutionException;
 import org.apache.maven.plugins.annotations.Component;
 import org.apache.maven.plugins.annotations.Parameter;
 import org.apache.maven.project.ProjectBuilder;
 import org.sonatype.aether.RepositorySystemSession;
 
 import java.util.List;
 
 public abstract class Base extends AbstractMojo {
     /**
      * Where to store prereleases.
      */
     @Parameter(property = "prerelease.archive", defaultValue = "${settings.localRepository}/../prereleases", required = true)
     protected String archiveRoot;
 
     /**
      * Where to store prereleases.
      */
     @Parameter(property = "prerelease.lockTimeout", defaultValue = "3600", required = true)
     protected int lockTimeout;
 
     @Component
     private ProjectBuilder projectBuilder;
 
     /**
      * The current repository/network configuration of Maven.
      */
     @Parameter(property = "repositorySystemSession", readonly = true)
     private RepositorySystemSession repositorySession;
 
     @Parameter(property = "project.remoteArtifactRepositories", readonly = true)
     private List<ArtifactRepository> remoteRepositoriesLegacy;
 
     @Component
     protected MavenSession session;
 
     protected final World world;
 
     protected boolean alwaysUpdate;
 
     public Base() {
         this.world = new World();
     }
 
     @Override
     public void execute() throws MojoExecutionException {
         getLog().debug("user-properties: " + session.getUserProperties());
         alwaysUpdate = "always".equals(repositorySession.getUpdatePolicy());
         try {
             doExecute();
         } catch (RuntimeException | MojoExecutionException e) {
             throw e;
         } catch (Exception e) {
             throw new MojoExecutionException("plugin failed: " + e.getMessage(), e);
         }
     }
 
     public abstract void doExecute() throws Exception;
 
     protected Maven maven() {
         return new Maven(world, repositorySession, projectBuilder, remoteRepositoriesLegacy);
     }
 }
