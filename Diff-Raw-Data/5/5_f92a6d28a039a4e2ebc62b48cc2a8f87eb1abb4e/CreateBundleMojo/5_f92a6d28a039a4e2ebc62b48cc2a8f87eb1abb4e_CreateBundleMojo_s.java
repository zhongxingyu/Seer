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
 package org.sonatype.maven.plugin.app.bundle;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Properties;
 
 import org.apache.maven.execution.MavenSession;
 import org.apache.maven.plugin.AbstractMojo;
 import org.apache.maven.plugin.MojoExecutionException;
 import org.apache.maven.plugin.assembly.InvalidAssemblerConfigurationException;
 import org.apache.maven.plugin.assembly.archive.ArchiveCreationException;
 import org.apache.maven.plugin.assembly.archive.AssemblyArchiver;
 import org.apache.maven.plugin.assembly.format.AssemblyFormattingException;
 import org.apache.maven.plugin.assembly.io.AssemblyReadException;
 import org.apache.maven.plugin.assembly.io.AssemblyReader;
 import org.apache.maven.plugin.assembly.model.Assembly;
 import org.apache.maven.plugin.assembly.model.FileItem;
 import org.apache.maven.project.MavenProject;
 import org.apache.maven.project.MavenProjectHelper;
 import org.sonatype.maven.plugin.app.ClasspathUtils;
 
 /**
  * Create a plugin bundle artifact attach it to the plugin's project.
  * 
  * @goal create-bundle
  * @phase package
  */
 public class CreateBundleMojo
     extends AbstractMojo
 {
     /**
      * The current plugin project being built.
      * 
      * @parameter default-value="${project}"
      * @required
      * @readonly
      */
     private MavenProject project;
 
     /**
      * The current build session, for reference from the Assembly API.
      * 
      * @parameter default-value="${session}"
      * @required
      * @readonly
      */
     private MavenSession session;
 
     /**
      * Supplemental plugin assembly configuration.
      * 
      * @parameter
      */
     private BundleConfiguration bundle;
 
     /**
      * Alternative assembly descriptor. If not specified, default assembly descriptor will be used instead.
      * 
      * @parameter
      * @readonly
      */
     private File assemblyDescriptor;
 
     /**
      * Assembly manager component that is responsible for creating the plugin bundle assembly and attaching it to the
      * current project.
      * 
      * @component
      */
     private AssemblyArchiver archiver;
 
     /**
      * @component
      */
     private AssemblyReader assemblyReader;
 
     /**
      * Component used by the {@link AssemblyArchiver} to attach the bundle artifact to the current project.
      * 
      * @component
      */
     private MavenProjectHelper projectHelper;
 
     /**
      * The list of classpath dependencies to be excluded from bundling for some reason (for example because you are
      * shading it into plugin artifact).
      * 
      * @parameter
      * @since 1.3
      */
     private List<String> classpathDependencyExcludes;
 
     public void execute()
         throws MojoExecutionException
     {
 
         if ( bundle == null )
         {
             bundle = new BundleConfiguration( project, session );
         }
         else
         {
             bundle.initDefaults( project, session );
         }
 
         Assembly assembly;
 
         if ( assemblyDescriptor != null )
         {
             try
             {
                 assembly = assemblyReader.getAssemblyFromDescriptorFile( assemblyDescriptor, bundle );
             }
             catch ( AssemblyReadException e )
             {
                 throw new MojoExecutionException( "Could not read assembly descriptor "
                     + assemblyDescriptor.getAbsolutePath(), e );
             }
             catch ( InvalidAssemblerConfigurationException e )
             {
                 throw new MojoExecutionException(
                     "Invalid assembly descriptor " + assemblyDescriptor.getAbsolutePath(), e );
             }
         }
         else
         {
             assembly = new Assembly();
         }
 
         assembly.addFormat( "zip" );
         assembly.setId( "bundle" );
         assembly.setIncludeBaseDirectory( false );
 
         try
         {
             Properties cpArtifacts = ClasspathUtils.read( project );
             String outputDirectory = project.getArtifactId() + "-" + project.getVersion() + "/dependencies";
 
             for ( Iterator it = cpArtifacts.keySet().iterator(); it.hasNext(); )
             {
                 String destName = (String) it.next();
 
                 if ( !isExcluded( destName ) )
                 {
                     String sourcePath = cpArtifacts.getProperty( destName );
 
                     FileItem fi = new FileItem();
                     fi.setSource( sourcePath );
                     fi.setOutputDirectory( outputDirectory );
                     fi.setDestName( new File( sourcePath ).getName() );
 
                     assembly.addFile( fi );
                 }
                 else
                 {
                     getLog().info(
                         "Classpath dependency [" + destName + "] is excluded from plugin bundle by configuration." );
                 }
             }
         }
         catch ( IOException e )
         {
             throw new MojoExecutionException( "Failed to create plugin bundle: " + e.getMessage(), e );
         }
 
         FileItem fi = new FileItem();
         fi.setSource( project.getArtifact().getFile().getPath() );
         fi.setOutputDirectory( project.getArtifactId() + "-" + project.getVersion() );
 
         assembly.addFile( fi );
 
         try
         {
             File assemblyFile =
                 archiver.createArchive( assembly, bundle.getAssemblyFileName( assembly ), "zip", bundle );
             projectHelper.attachArtifact( project, "zip", assembly.getId(), assemblyFile );
         }
         catch ( ArchiveCreationException e )
         {
             throw new MojoExecutionException( "Failed to create plugin bundle: " + e.getMessage(), e );
         }
         catch ( AssemblyFormattingException e )
         {
             throw new MojoExecutionException( "Failed to create plugin bundle: " + e.getMessage(), e );
         }
         catch ( InvalidAssemblerConfigurationException e )
         {
             throw new MojoExecutionException( "Failed to create plugin bundle: " + e.getMessage(), e );
         }
     }
 
     protected boolean isExcluded( final String key )
     {
         for ( String exclude : classpathDependencyExcludes )
         {
             if ( key.startsWith( exclude ) )
             {
                 return true;
             }
         }
 
         return false;
     }
 }
