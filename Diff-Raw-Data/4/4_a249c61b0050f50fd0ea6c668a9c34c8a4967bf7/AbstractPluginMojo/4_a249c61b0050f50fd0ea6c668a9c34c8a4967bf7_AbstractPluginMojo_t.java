 /*
  * Licensed to the Apache Software Foundation (ASF) under one
  * or more contributor license agreements.  See the NOTICE file
  * distributed with this work for additional information
  * regarding copyright ownership.  The ASF licenses this file
  * to you under the Apache License, Version 2.0 (the
  * "License"); you may not use this file except in compliance
  * with the License.  You may obtain a copy of the License at
  *
  *   http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing,
  * software distributed under the License is distributed on an
  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  * KIND, either express or implied.  See the License for the
  * specific language governing permissions and limitations
  * under the License.
  */
 package com.enonic.cms.maven.plugin;
 
 import org.apache.maven.artifact.Artifact;
 import org.apache.maven.artifact.resolver.filter.ScopeArtifactFilter;
 import org.apache.maven.plugin.AbstractMojo;
 import org.apache.maven.plugin.MojoExecutionException;
 import org.apache.maven.plugin.MojoFailureException;
 import org.apache.maven.project.MavenProject;
 import org.apache.maven.project.MavenProjectHelper;
 
 import java.io.File;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 public abstract class AbstractPluginMojo
     extends AbstractMojo
 {
     /**
      * The Maven project.
      *
      * @parameter expression="${project}"
      * @required
      * @readonly
      */
     private MavenProject project = null;
 
     /**
      * Classifier to add to the artifact generated. If given, the artifact will be an attachment instead.
      *
      * @parameter
      */
     private String classifier = null;
 
     /**
      * @component
      */
     private MavenProjectHelper projectHelper = null;
 
     /**
      * Name of the generated JAR.
      *
      * @parameter alias="jarName" expression="${jar.finalName}" default-value="${project.build.finalName}"
      * @required
      */
     private String finalName = null;
 
     /**
      * Directory containing the generated JAR.
      *
      * @parameter expression="${project.build.directory}"
      * @required
      */
     private File outputDirectory = null;
 
     /**
      * Directory containing the classes and resource files that should be packaged into the JAR.
      *
      * @parameter expression="${project.build.outputDirectory}"
      * @required
      */
     private File classesDirectory = null;
 
     /**
      * Plugin id.
      *
      * @parameter default-value="${project.artifactId}"
      */
     private String pluginId = null;
 
     /**
      * Plugin id.
      *
      * @parameter default-value="${project.name}"
      */
     private String pluginName = null;
 
     /**
      * The directory where the app is built.
      *
      * @parameter expression="${project.build.directory}/${project.build.finalName}"
      * @required
      */
     private File appDirectory = null;
 
     protected final MavenProject getProject()
     {
         return this.project;
     }
 
     protected final String getClassifier()
     {
         return this.classifier;
     }
 
     protected final MavenProjectHelper getProjectHelper()
     {
         return this.projectHelper;
     }
 
     protected final String getFinalName()
     {
         return this.finalName;
     }
 
     protected final File getOutputDirectory()
     {
         return this.outputDirectory;
     }
 
     protected final File getClassesDirectory()
     {
         return this.classesDirectory;
     }
 
     protected final String getPluginId()
     {
         return this.pluginId;
     }
 
     protected final String getPluginName()
     {
         return this.pluginName;
     }
 
     protected final File getAppDirectory()
     {
         return this.appDirectory;
     }
 
     public final void execute()
         throws MojoExecutionException, MojoFailureException
     {
         try {
             doExecute();
         } catch (final MojoExecutionException e) {
             throw e;
         } catch (final MojoFailureException e) {
             throw e;
         } catch (final Exception e) {
             throw new MojoExecutionException("Failed to execute mojo: " + e.getMessage(), e);
         }
     }
 
     protected abstract void doExecute()
         throws Exception;
 
     @SuppressWarnings({ "unchecked" })
     protected final Set<Artifact> getIncludedArtifacts()
     {
         final Set<Artifact> result = new HashSet<Artifact>();
         final Set<Artifact> artifacts = this.project.getArtifacts();
         final ScopeArtifactFilter filter = new ScopeArtifactFilter(Artifact.SCOPE_RUNTIME);
 
         for (final Artifact artifact : artifacts) {
            if (filter.include(artifact) && !artifact.isOptional()) {
                 result.add(artifact);
             }
         }
 
         return result;
     }
 
     protected final String getMessage(final String title, final List<String> ids)
     {
         final StringBuilder message = new StringBuilder();
         message.append(title);
         message.append("\n\n");
 
         for (final String id : ids) {
             message.append("\t").append(id).append("\n");
         }
 
         return message.toString();
     }
 }
