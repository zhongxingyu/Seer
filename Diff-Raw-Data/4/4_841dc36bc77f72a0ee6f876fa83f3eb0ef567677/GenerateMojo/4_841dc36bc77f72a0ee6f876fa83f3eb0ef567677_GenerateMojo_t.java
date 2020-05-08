 package org.sonatype.maven.plugins.provideddependencies;
 
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.lang.reflect.Field;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.LinkedHashSet;
 import java.util.List;
 import java.util.Set;
 
 import org.apache.maven.artifact.Artifact;
 import org.apache.maven.artifact.factory.ArtifactFactory;
 import org.apache.maven.artifact.resolver.filter.AndArtifactFilter;
 import org.apache.maven.artifact.resolver.filter.ArtifactFilter;
 import org.apache.maven.artifact.resolver.filter.ExcludesArtifactFilter;
 import org.apache.maven.artifact.resolver.filter.IncludesArtifactFilter;
 import org.apache.maven.model.Dependency;
 import org.apache.maven.model.DependencyManagement;
 import org.apache.maven.model.Exclusion;
 import org.apache.maven.model.Model;
 import org.apache.maven.model.io.xpp3.MavenXpp3Writer;
 import org.apache.maven.plugin.AbstractMojo;
 import org.apache.maven.plugin.MojoExecutionException;
 import org.apache.maven.plugin.MojoFailureException;
 import org.apache.maven.project.MavenProject;
 import org.codehaus.plexus.util.IOUtil;
 
 /**
  * @author velo
  * @goal generate
  * @requiresDependencyResolution test
  * @phase generate-resources
  */
 public class GenerateMojo
     extends AbstractMojo
 {
 
     /**
      * @parameter default-value="${project.groupId}"
      */
     private String groupId;
 
     /**
      * @parameter default-value="${project.artifactId}-dependencies"
      */
     private String artifactId;
 
     /**
      * @parameter default-value="${project.version}"
      */
     private String version;
 
     /**
      * @parameter default-value="${project.artifacts}"
      * @readonly
      */
     private Collection<Artifact> artifacts;
 
     /**
      * @parameter default-value="${project.build.directory}"
      * @readonly
      */
     private File target;
 
     /**
      * @parameter default-value="${project}"
      * @readonly
      */
     private MavenProject project;
 
     /**
      * @component
      */
     private ArtifactFactory artifactFactory;
 
     public void execute()
         throws MojoExecutionException, MojoFailureException
     {
 
         Model pom = new Model();
         pom.setModelVersion( "4.0.0" );
         pom.setGroupId( groupId );
         pom.setArtifactId( artifactId );
         pom.setVersion( version );
         pom.setPackaging( "pom" );
 
         DependencyManagement dependencyManagement = new DependencyManagement();
         for ( Artifact artifact : artifacts )
         {
             Dependency dep = new Dependency();
             dep.setGroupId( artifact.getGroupId() );
             dep.setArtifactId( artifact.getArtifactId() );
             dep.setVersion( artifact.getBaseVersion() );
             dep.setClassifier( artifact.getClassifier() );
             dep.setType( artifact.getType() );
             dep.setScope( "provided" );
 
            // using a set to prevent duplicated entries
             Set<String> exclusions = new LinkedHashSet<String>( getExclusions( artifact.getDependencyFilter() ) );
             for ( String exclusion : exclusions )
             {
                 String[] pattern = exclusion.split( ":" );
                 if ( pattern.length == 2 )
                 {
                     Exclusion ex = new Exclusion();
                     ex.setGroupId( pattern[0] );
                     ex.setArtifactId( pattern[1] );
                     dep.addExclusion( ex );
                 }
             }
 
             dependencyManagement.addDependency( dep );
            pom.addDependency( dep );
         }
 
         pom.setDependencyManagement( dependencyManagement );
 
         File file = new File( target, artifactId + "-dependencies.pom" );
 
         FileWriter writer = null;
         try
         {
             file.createNewFile();
             writer = new FileWriter( file );
             new MavenXpp3Writer().write( writer, pom );
         }
         catch ( IOException e )
         {
             throw new MojoExecutionException( "Failed to generate pom", e );
         }
         IOUtil.close( writer );
 
         Artifact artifact = artifactFactory.createArtifact( groupId, artifactId, version, null, "pom" );
         artifact.setFile( file );
 
         project.addAttachedArtifact( artifact );
     }
 
     private List<String> getExclusions( ArtifactFilter filter )
     {
         if ( filter == null )
         {
             return Collections.emptyList();
         }
 
         if ( filter instanceof ExcludesArtifactFilter )
         {
             ExcludesArtifactFilter efilter = (ExcludesArtifactFilter) filter;
             return getPatterns( efilter );
         }
         if ( filter instanceof AndArtifactFilter )
         {
             AndArtifactFilter af = (AndArtifactFilter) filter;
             List<ArtifactFilter> filters = getFilters( af );
             List<String> patterns = new ArrayList<String>();
             for ( ArtifactFilter f : filters )
             {
                 patterns.addAll( getExclusions( f ) );
             }
 
             return patterns;
         }
 
         getLog().warn( "Unsupported filter " + filter.getClass() );
 
         return Collections.emptyList();
     }
 
     @SuppressWarnings( "unchecked" )
     private List<ArtifactFilter> getFilters( AndArtifactFilter af )
     {
         try
         {
             Field f = AndArtifactFilter.class.getDeclaredField( "filters" );
             f.setAccessible( true );
             return (List<ArtifactFilter>) f.get( af );
         }
         catch ( Exception e )
         {
             getLog().error( e.getMessage() );
             return Collections.emptyList();
         }
     }
 
     @SuppressWarnings( "unchecked" )
     private List<String> getPatterns( ExcludesArtifactFilter efilter )
     {
         try
         {
             Field f = IncludesArtifactFilter.class.getDeclaredField( "patterns" );
             f.setAccessible( true );
             return (List<String>) f.get( efilter );
         }
         catch ( Exception e )
         {
             getLog().error( e.getMessage() );
             return Collections.emptyList();
         }
     }
 }
