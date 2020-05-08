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
 package org.sonatype.maven.plugin.app.descriptor;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import org.apache.maven.artifact.Artifact;
 import org.apache.maven.model.License;
 import org.apache.maven.plugin.AbstractMojo;
 import org.apache.maven.plugin.MojoExecutionException;
 import org.apache.maven.plugin.MojoFailureException;
 import org.apache.maven.project.MavenProject;
 import org.apache.maven.scm.ScmException;
 import org.apache.maven.scm.ScmFileSet;
 import org.apache.maven.scm.manager.ScmManager;
 import org.apache.maven.scm.provider.ScmProviderRepository;
 import org.apache.maven.scm.provider.git.AbstractGitScmProvider;
 import org.apache.maven.scm.provider.hg.HgScmProvider;
 import org.apache.maven.scm.provider.svn.AbstractSvnScmProvider;
 import org.apache.maven.scm.provider.svn.command.info.SvnInfoItem;
 import org.apache.maven.scm.provider.svn.command.info.SvnInfoScmResult;
 import org.apache.maven.scm.repository.ScmRepository;
 import org.codehaus.plexus.interpolation.InterpolationException;
 import org.codehaus.plexus.util.StringUtils;
 import org.sonatype.maven.plugin.app.ApplicationInformation;
 import org.sonatype.maven.plugin.app.ClasspathUtils;
 
 /**
  * Generates a plugin's <tt>plugin.xml</tt> descriptor file based on the project's pom and class annotations.
  * 
  * @goal generate-metadata
  * @phase process-classes
  * @requiresDependencyResolution test
  */
 public class PluginDescriptorMojo
     extends AbstractMojo
 {
 
     /**
      * A list of groupId:artifactId references to non-plugin dependencies that contain components which should be
      * gleaned for this plugin build.
      * 
      * @parameter
     * @deprecated use {@link #sharedDependencies} instead. This Mojo will handle all "componentDependencies" and
     *             "sharedDependencies" until removed.
      */
     @Deprecated
     private List<String> componentDependencies;
 
     /**
      * The output location for the generated plugin descriptor. <br/>
      * <b>NOTE:</b> Default value for this field is supplied by the {@link ApplicationInformation} component included
      * via build extension.
      * 
      * @parameter
      */
     private File generatedPluginMetadata;
 
     /**
      * @parameter expression="${project}"
      * @required
      * @readonly
      */
     private MavenProject mavenProject;
 
     /**
      * The ID of the target application. For example if this plugin was for the Nexus Repository Manager, the ID would
      * be, 'nexus'. <br/>
      * <b>NOTE:</b> Default value for this field is supplied by the {@link ApplicationInformation} component included
      * via build extension.
      * 
      * @parameter
      */
     private String applicationId;
 
     /**
      * The edition of the target application. Some applications come in multiple flavors, OSS, PRO, Free, light, etc. <br/>
      * <b>NOTE:</b> Default value for this field is supplied by the {@link ApplicationInformation} component included
      * via build extension.
      * 
      * @parameter expression="OSS"
      */
     private String applicationEdition;
 
     /**
      * The minimum product version of the target application. <br/>
      * <b>NOTE:</b> Default value for this field is supplied by the {@link ApplicationInformation} component included
      * via build extension.
      * 
      * @parameter
      */
     private String applicationMinVersion;
 
     /**
      * The maximum product version of the target application. <br/>
      * <b>NOTE:</b> Default value for this field is supplied by the {@link ApplicationInformation} component included
      * via build extension, if it specified at all.
      * 
      * @parameter
      */
     private String applicationMaxVersion;
 
     /**
      * Brought in via build extension, this supplies default values specific to the application being built. <br/>
      * <b>NOTE:</b> There should be <b>AT MOST ONE</b> {@link ApplicationInformation} component present in any given
      * build. If this component is missing, it should be created on-the-fly, and will be empty...which means the plugin
      * parameters given here will be required.
      * 
      * @component
      */
     private ApplicationInformation mapping;
 
     /**
      * @parameter expression="${project.scm.developerConnection}"
      * @readonly
      */
     private String urlScm;
 
     /**
      * The username that is used when connecting to the SCM system.
      * 
      * @parameter expression="${username}"
      * @since 1.0-beta-1
      */
     private String username;
 
     /**
      * The password that is used when connecting to the SCM system.
      * 
      * @parameter expression="${password}"
      * @since 1.0-beta-1
      */
     private String password;
 
     /**
      * @component
      */
     private ScmManager scmManager;
 
     /**
      * The list of classpath dependencies to be excluded from bundling for some reason (for example because you are
      * shading it into plugin artifact).
      * 
      * @parameter
      * @since 1.3
      */
     private List<String> classpathDependencyExcludes;
 
     /**
      * A list of groupId:artifactId references to non-plugin dependencies that should be shared along with main plugin
     * JAR to to dependants of this plugin.
      * 
      * @parameter
      * @since 1.5
      */
     private List<String> sharedDependencies;
 
     protected void checkConfig()
     {
         if ( componentDependencies != null && componentDependencies.size() > 0 )
         {
             getLog().warn( "Plugin is using a deprecated configuration element \"componentDependencies\"!" );
             getLog().warn(
                 "Please update your build by replacing \"componentDependencies\" with \"sharedDependencies\"." );
             getLog().warn( "This plugin will continue handling \"componentDependencies\" as \"sharedDependencies\"..." );
         }
     }
 
     public void execute()
         throws MojoExecutionException, MojoFailureException
     {
         if ( !this.mavenProject.getPackaging().equals( mapping.getPluginPackaging() ) )
         {
             this.getLog().info( "Project is not of packaging type '" + mapping.getPluginPackaging() + "'." );
             return;
         }
 
         checkConfig();
 
         initConfig();
 
         PluginMetadataGenerationRequest request = new PluginMetadataGenerationRequest();
         request.setGroupId( this.mavenProject.getGroupId() );
         request.setArtifactId( this.mavenProject.getArtifactId() );
         request.setVersion( this.mavenProject.getVersion() );
         request.setName( this.mavenProject.getName() );
         request.setDescription( this.mavenProject.getDescription() );
         request.setPluginSiteURL( this.mavenProject.getUrl() );
 
         request.setApplicationId( applicationId );
         request.setApplicationEdition( applicationEdition );
         request.setApplicationMinVersion( applicationMinVersion );
         request.setApplicationMaxVersion( applicationMaxVersion );
 
         // licenses
         if ( this.mavenProject.getLicenses() != null )
         {
             for ( License mavenLicenseModel : (List<License>) this.mavenProject.getLicenses() )
             {
                 request.addLicense( mavenLicenseModel.getName(), mavenLicenseModel.getUrl() );
             }
         }
 
         // scm information
         fillScmInfo( request );
 
         // dependencies
         List<Artifact> artifacts = mavenProject.getTestArtifacts();
         Set<Artifact> classpathArtifacts = new HashSet<Artifact>();
         if ( artifacts != null )
         {
 
             Set<String> excludedArtifactIds = new HashSet<String>();
 
             artifactLoop: for ( Artifact artifact : artifacts )
             {
                 if ( artifact.getType().equals( mapping.getPluginPackaging() ) )
                 {
                     if ( !Artifact.SCOPE_PROVIDED.equals( artifact.getScope() ) )
                     {
                         throw new MojoFailureException( "Plugin dependency \"" + artifact.getDependencyConflictId()
                             + "\" must have the \"provided\" scope!" );
                     }
 
                     excludedArtifactIds.add( artifact.getId() );
 
                     // plugin interdependencies will use baseVersion, and let PluginManager resolve them runtime
                     request.addPluginDependency( new GAVCoordinate( artifact.getGroupId(), artifact.getArtifactId(),
                         artifact.getBaseVersion(), artifact.getClassifier(), artifact.getType(), false ) );
                 }
                 else if ( Artifact.SCOPE_PROVIDED.equals( artifact.getScope() )
                     || Artifact.SCOPE_TEST.equals( artifact.getScope() ) )
                 {
                     excludedArtifactIds.add( artifact.getId() );
                 }
                 else if ( ( Artifact.SCOPE_COMPILE.equals( artifact.getScope() ) || Artifact.SCOPE_RUNTIME.equals( artifact.getScope() ) )
                     && ( !mapping.matchesCoreGroupIds( artifact.getGroupId() ) ) )
                 {
                     if ( artifact.getDependencyTrail() != null )
                     {
                         for ( String trailId : (List<String>) artifact.getDependencyTrail() )
                         {
                             if ( excludedArtifactIds.contains( trailId ) )
                             {
                                 getLog().debug(
                                     "Dependency artifact: "
                                         + artifact.getId()
                                         + " is part of the transitive dependency set for a dependency with 'provided' or 'test' scope: "
                                         + trailId + "\nThis artifact will be excluded from the plugin classpath." );
                                 continue artifactLoop;
                             }
                         }
                     }
 
                     final String artifactKey = ClasspathUtils.formatArtifactKey( artifact );
 
                     if ( !isExcluded( artifactKey ) )
                     {
                         // support for deprecated config, if any
                         final boolean hasComponents =
                             ( componentDependencies != null && componentDependencies.contains( artifact.getGroupId()
                                 + ":" + artifact.getArtifactId() ) );
 
                         final boolean isShared = hasComponents ||
                             ( sharedDependencies != null && sharedDependencies.contains( artifact.getGroupId() + ":"
                                 + artifact.getArtifactId() ) );
 
                         // classpath dependencies uses baseVersion, and let PluginManager resolve them runtime
                         // this enables easy development turnaround, by not having recompiling the plugin to drop-in
                         // newer snapshot
                         request.addClasspathDependency( new GAVCoordinate( artifact.getGroupId(),
                             artifact.getArtifactId(), artifact.getBaseVersion(), artifact.getClassifier(),
                             artifact.getType(), isShared ) );
                         classpathArtifacts.add( artifact );
                     }
                     else
                     {
                         getLog().info(
                             "Classpath dependency [" + artifactKey
                                 + "] is excluded from plugin bundle by user configuration." );
                     }
                 }
             }
         }
 
         request.setOutputFile( this.generatedPluginMetadata );
 
         // do the work
         try
         {
             new PluginDescriptorGenerator().generatePluginDescriptor( request );
         }
         catch ( IOException e )
         {
             throw new MojoFailureException( "Failed to generate plugin xml file: " + e.getMessage(), e );
         }
 
         try
         {
             ClasspathUtils.write( classpathArtifacts, mavenProject );
         }
         catch ( IOException e )
         {
             throw new MojoFailureException( "Failed to generate classpath properties file: " + e.getMessage(), e );
         }
     }
 
     private void initConfig()
         throws MojoFailureException
     {
         if ( this.generatedPluginMetadata == null )
         {
             try
             {
                 this.generatedPluginMetadata = mapping.getPluginMetadataFile( this.mavenProject );
             }
             catch ( InterpolationException e )
             {
                 throw new MojoFailureException( "Cannot calculate plugin metadata file location from expression: "
                     + mapping.getPluginMetadataPath(), e );
             }
         }
 
         this.applicationId = applicationId == null ? mapping.getApplicationId() : applicationId;
         this.applicationEdition = applicationEdition == null ? mapping.getApplicationEdition() : applicationEdition;
         this.applicationMinVersion =
             applicationMinVersion == null ? mapping.getApplicationMinVersion() : applicationMinVersion;
         this.applicationMaxVersion =
             applicationMaxVersion == null ? mapping.getApplicationMaxVersion() : applicationMaxVersion;
     }
 
     protected boolean isExcluded( final String key )
     {
         if ( classpathDependencyExcludes == null )
         {
             return false;
         }
 
         for ( String exclude : classpathDependencyExcludes )
         {
             if ( key.startsWith( exclude ) )
             {
                 return true;
             }
         }
 
         return false;
     }
 
     // SCM
 
     protected void fillScmInfo( final PluginMetadataGenerationRequest request )
     {
         // scm information
         try
         {
             final ScmRepository repository = getScmRepository();
 
             // we are here, so no ScmException was thrown, we are fine
             request.setScmUrl( urlScm );
 
             final String provider = repository.getProvider();
 
             if ( "svn".equals( provider ) )
             {
                 fillSvnScmInfo( request, repository );
             }
             else if ( "git".equals( provider ) )
             {
                 fillGitScmInfo( request, repository );
             }
             else if ( "hg".equals( provider ) )
             {
                 fillHgScmInfo( request, repository );
             }
         }
         catch ( ScmException e )
         {
             this.getLog().warn( "Failed to get scm information: " + e.getMessage() );
 
             this.getLog().debug( e );
         }
     }
 
     protected ScmRepository getScmRepository()
         throws ScmException
     {
         if ( StringUtils.isEmpty( urlScm ) )
         {
             throw new ScmException( "No SCM URL found." );
         }
 
         ScmRepository repository = scmManager.makeScmRepository( urlScm );
 
         ScmProviderRepository scmRepo = repository.getProviderRepository();
 
         if ( !StringUtils.isEmpty( username ) )
         {
             scmRepo.setUser( username );
         }
 
         if ( !StringUtils.isEmpty( password ) )
         {
             scmRepo.setPassword( password );
         }
 
         return repository;
     }
 
     protected void fillSvnScmInfo( final PluginMetadataGenerationRequest request, final ScmRepository repository )
         throws ScmException
     {
         AbstractSvnScmProvider abstractSvnScmProvider = (AbstractSvnScmProvider) scmManager.getProviderByType( "svn" );
 
         SvnInfoScmResult scmResult =
             abstractSvnScmProvider.info( repository.getProviderRepository(),
                 new ScmFileSet( mavenProject.getBasedir() ), null );
 
         if ( !scmResult.isSuccess() )
         {
             throw new ScmException( scmResult.getCommandOutput() );
         }
 
         SvnInfoItem info = (SvnInfoItem) scmResult.getInfoItems().get( 0 );
 
         request.setScmVersion( info.getLastChangedRevision() );
         request.setScmTimestamp( info.getLastChangedDate() );
     }
 
     protected void fillGitScmInfo( final PluginMetadataGenerationRequest request, final ScmRepository repository )
         throws ScmException
     {
         AbstractGitScmProvider abstractGitScmProvider = (AbstractGitScmProvider) scmManager.getProviderByType( "git" );
 
         GitRevParseCommand cmd = new GitRevParseCommand();
 
         cmd.setLogger( abstractGitScmProvider.getLogger() );
 
         GitRevParseScmResult scmResult =
             (GitRevParseScmResult) cmd.execute( repository.getProviderRepository(),
                 new ScmFileSet( mavenProject.getBasedir() ), null );
 
         if ( !scmResult.isSuccess() )
         {
             throw new ScmException( scmResult.getCommandOutput() );
         }
 
         request.setScmVersion( scmResult.getChangeSetHash() );
         request.setScmTimestamp( scmResult.getChangeSetDate() );
     }
 
     protected void fillHgScmInfo( final PluginMetadataGenerationRequest request, final ScmRepository repository )
         throws ScmException
     {
         HgScmProvider hgScmProvider = (HgScmProvider) scmManager.getProviderByType( "hg" );
 
         HgDebugIdCommand cmd = new HgDebugIdCommand();
 
         cmd.setLogger( hgScmProvider.getLogger() );
 
         HgDebugIdScmResult scmResult =
             (HgDebugIdScmResult) cmd.execute( repository.getProviderRepository(),
                 new ScmFileSet( mavenProject.getBasedir() ), null );
 
         if ( !scmResult.isSuccess() )
         {
             throw new ScmException( scmResult.getCommandOutput() );
         }
 
         request.setScmVersion( scmResult.getChangeSetHash() );
         request.setScmTimestamp( scmResult.getChangeSetDate() );
     }
 }
