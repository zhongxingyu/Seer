 /*
  * Copyright 2011 Red Hat, Inc.
  * 
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * 
  *   http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package org.apache.maven.mae.project.testutil;
 
 import org.apache.commons.io.FileUtils;
 import org.apache.log4j.ConsoleAppender;
 import org.apache.log4j.Level;
 import org.apache.log4j.LogManager;
 import org.apache.log4j.Logger;
 import org.apache.log4j.SimpleLayout;
 import org.apache.log4j.spi.Configurator;
 import org.apache.log4j.spi.LoggerRepository;
 import org.apache.maven.artifact.InvalidRepositoryException;
 import org.apache.maven.artifact.repository.ArtifactRepository;
 import org.apache.maven.mae.MAEException;
 import org.apache.maven.mae.app.AbstractMAEApplication;
 import org.apache.maven.mae.boot.embed.MAEEmbedder;
 import org.apache.maven.mae.boot.embed.MAEEmbedderBuilder;
 import org.apache.maven.mae.boot.embed.MAEEmbeddingException;
 import org.apache.maven.mae.conf.MavenPomVersionProvider;
 import org.apache.maven.mae.conf.VersionProvider;
 import org.apache.maven.mae.project.ProjectLoader;
 import org.apache.maven.mae.project.session.ProjectToolsSession;
 import org.apache.maven.mae.project.session.SimpleProjectToolsSession;
 import org.apache.maven.model.Repository;
 import org.apache.maven.model.building.ModelBuildingRequest;
 import org.apache.maven.project.DefaultProjectBuildingRequest;
 import org.apache.maven.project.MavenProject;
 import org.apache.maven.project.ProjectBuilder;
 import org.apache.maven.project.ProjectBuildingException;
 import org.apache.maven.project.ProjectBuildingResult;
 import org.codehaus.plexus.component.annotations.Component;
 import org.codehaus.plexus.component.annotations.Requirement;
 import org.junit.Assert;
 
 import java.io.File;
 import java.io.IOException;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Enumeration;
 import java.util.LinkedHashSet;
 import java.util.List;
 import java.util.Set;
 
 @Component( role = TestFixture.class )
 public final class TestFixture
     extends AbstractMAEApplication
 {
 
     @Requirement
     private MAEEmbedder embedder;
 
     @Requirement
     private ProjectBuilder projectBuilder;
 
     @Requirement
     private ProjectLoader projectManager;
 
     private File localRepoDir;
 
     private File tempProjectDir;
 
     private ArtifactRepository localRepository;
 
     private static Repository rawRemoteRepo;
 
     private ArtifactRepository remoteRepository;
 
     private final Set<File> tempFiles = new LinkedHashSet<File>();
 
     private boolean debug = false;
 
     private static TestFixture fixture;
 
     private static int instances = 0;
 
     private TestFixture()
         throws MAEException, IOException
     {
         setupDebugLogging();
 
         initFiles();
         // withVirtualComponent( ProjectToolsSession.class );
         // setVirtualInstance( ProjectToolsSession.class, )
     }
 
     public static void setupDebugLogging()
     {
         final Configurator log4jConfigurator = new Configurator()
         {
             @Override
             @SuppressWarnings( "unchecked" )
             public void doConfigure( final URL notUsed, final LoggerRepository repo )
             {
                 final ConsoleAppender appender = new ConsoleAppender( new SimpleLayout() );
                 appender.setImmediateFlush( true );
                 appender.setThreshold( Level.ALL );
 
                 repo.getRootLogger().addAppender( appender );
 
                 final Enumeration<Logger> loggers = repo.getCurrentLoggers();
                 while ( loggers.hasMoreElements() )
                 {
                     final Logger logger = loggers.nextElement();
                     logger.addAppender( appender );
                     logger.setLevel( Level.INFO );
                 }
             }
         };
 
         log4jConfigurator.doConfigure( null, LogManager.getLoggerRepository() );
     }
 
     public static TestFixture getInstance()
         throws MAEException, IOException
     {
         if ( fixture == null )
         {
             fixture = new TestFixture();
             fixture.load();
             fixture.initObjects();
         }
 
         instances++;
         return fixture;
     }
 
     public void setDebug( final boolean debug )
     {
         this.debug = debug;
     }
 
     public MavenProject getTestProject( final String path )
         throws ProjectBuildingException, IOException
     {
         final File pom = getTestFile( "projects", path );
 
         final DefaultProjectBuildingRequest req = new DefaultProjectBuildingRequest();
         req.setLocalRepository( localRepository );
 
         final List<ArtifactRepository> repos = new ArrayList<ArtifactRepository>();
         repos.add( remoteRepository );
 
         req.setRemoteRepositories( repos );
 
         req.setSystemProperties( System.getProperties() );
         req.setValidationLevel( ModelBuildingRequest.VALIDATION_LEVEL_MINIMAL );
         req.setProcessPlugins( false );
 
         final ProjectBuildingResult result = projectBuilder.build( pom, req );
         return result.getProject();
     }
 
     public File getTestFile( final String basedir, final String subpath )
         throws IOException
     {
         return getTestFile( basedir, subpath, false );
     }
 
     public File getTestFile( final String basedir, final String subpath, final boolean copy )
         throws IOException
     {
         String path;
         if ( basedir == null )
         {
             path = subpath;
         }
         else if ( subpath == null )
         {
             path = basedir;
         }
         else
         {
             path = new File( basedir, subpath ).getPath();
         }
 
         final URL resource = Thread.currentThread().getContextClassLoader().getResource( path );
         if ( resource == null )
         {
             Assert.fail( "Cannot find test file: " + path );
         }
 
         File pom = new File( resource.getPath() );
         if ( copy )
         {
             final File pomCopy = new File( tempProjectDir, path );
             pomCopy.getParentFile().mkdirs();
 
             FileUtils.copyFile( pom, pomCopy );
             pom = pomCopy;
         }
 
         return pom;
     }
 
     public MAEEmbedder embedder()
     {
         return embedder;
     }
 
     public void shutdown()
         throws IOException
     {
         instances--;
         if ( instances < 1 )
         {
             if ( !debug )
             {
                 for ( final File f : tempFiles )
                 {
                     if ( f != null && f.exists() )
                     {
                         FileUtils.forceDelete( f );
                     }
                 }
 
                 tempFiles.clear();
             }
         }
     }
 
     public Repository externalRepository()
     {
         return rawRemoteRepo;
     }
 
     public ProjectToolsSession newSession( final MavenProject... projects )
         throws IOException
     {
        final ProjectToolsSession session = new SimpleProjectToolsSession( localRepoDir, rawRemoteRepo );
 
         session.setRemoteArtifactRepositories( Collections.singletonList( remoteRepository ) );
 
         if ( projects != null && projects.length > 0 )
         {
             for ( final MavenProject project : projects )
             {
                 session.addReactorProject( project );
             }
         }
 
         return session;
     }
 
     @Override
     protected void afterLoading( final MAEEmbedder embedder )
         throws MAEException
     {
         super.afterLoading( embedder );
         try
         {
             initFiles();
         }
         catch ( final IOException e )
         {
             throw new MAEException( "Failed to initialize: %s", e, e.getMessage() );
         }
     }
 
     private void initFiles()
         throws MAEException, IOException
     {
         if ( localRepoDir == null || !localRepoDir.isDirectory() )
         {
             try
             {
                 localRepoDir = createTempDir( "local-repository.", ".dir" );
             }
             catch ( final IOException e )
             {
                 throw new MAEEmbeddingException( "Failed to create test local-repository directory.\nReason: %s", e,
                                                  e.getMessage() );
             }
         }
 
         if ( tempProjectDir == null || !tempProjectDir.isDirectory() )
         {
             try
             {
                 tempProjectDir = createTempDir( "test-projects.", ".dir" );
             }
             catch ( final IOException e )
             {
                 throw new MAEEmbeddingException( "Failed to create temporary projects directory.\nReason: %s", e,
                                                  e.getMessage() );
             }
         }
 
         rawRemoteRepo = new Repository();
         rawRemoteRepo.setId( "test" );
         rawRemoteRepo.setName( "Test Remote Repository" );
 
         try
         {
             rawRemoteRepo.setUrl( getTestFile( "test-repo", null, false ).toURI().toURL().toExternalForm() );
         }
         catch ( final MalformedURLException e )
         {
             throw new MAEEmbeddingException( "Failed to create test remote-repository instance.\nReason: %s", e,
                                              e.getMessage() );
         }
     }
 
     private void initObjects()
         throws MAEException
     {
         try
         {
             remoteRepository =
                 embedder.serviceManager().mavenRepositorySystem().buildArtifactRepository( rawRemoteRepo );
             localRepository = embedder.serviceManager().mavenRepositorySystem().createLocalRepository( localRepoDir );
         }
         catch ( final InvalidRepositoryException e )
         {
             throw new MAEEmbeddingException( "Failed to create  repository instances. Reason: %s", e, e.getMessage() );
         }
     }
 
     public File createTempDir( final String prefix, final String suffix )
         throws IOException
     {
         final File dir = createTempFile( prefix, suffix );
 
         dir.delete();
         dir.mkdirs();
 
         return dir;
     }
 
     public File createTempFile( final String prefix, final String suffix )
         throws IOException
     {
         final File f = File.createTempFile( prefix, suffix );
         tempFiles.add( f );
 
         return f;
     }
 
     @Override
     protected void configureBuilder( final MAEEmbedderBuilder builder )
         throws MAEException
     {
         super.configureBuilder( builder );
 
         // show the versions of things loaded
         // enable classpath scanning to avoid the need to generate plexus component descriptors before testing.
         builder.withVersion( true ).withClassScanningEnabled( true );
     }
 
     public ProjectLoader projectManager()
     {
         return projectManager;
     }
 
     @Override
     public String getId()
     {
         return "depsolv";
     }
 
     @Override
     public String getName()
     {
         return "Dependency-Resolver";
     }
 
     @Override
     protected VersionProvider getVersionProvider()
     {
         return new MavenPomVersionProvider( "org.commonjava.emb.components", "emb-dependency-resolver" );
     }
 
 }
