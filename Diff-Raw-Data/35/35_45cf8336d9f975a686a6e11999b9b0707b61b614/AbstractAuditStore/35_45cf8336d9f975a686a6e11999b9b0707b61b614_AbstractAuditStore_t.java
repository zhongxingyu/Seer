 /*
  *  Copyright (C) 2010 John Casey.
  *  
  *  This program is free software: you can redistribute it and/or modify
  *  it under the terms of the GNU Affero General Public License as published by
  *  the Free Software Foundation, either version 3 of the License, or
  *  (at your option) any later version.
  *  
  *  This program is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU Affero General Public License for more details.
  *  
  *  You should have received a copy of the GNU Affero General Public License
  *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package com.redhat.tools.nexus.audit.serial.store;
 
 import org.apache.maven.artifact.Artifact;
 import org.apache.maven.artifact.ArtifactUtils;
 import org.apache.maven.artifact.factory.ArtifactFactory;
 import org.apache.maven.artifact.repository.layout.ArtifactRepositoryLayout;
 import org.apache.maven.artifact.repository.metadata.Metadata;
import org.apache.maven.artifact.repository.metadata.Snapshot;
 import org.apache.maven.artifact.repository.metadata.SnapshotArtifactRepositoryMetadata;
 import org.apache.maven.artifact.repository.metadata.Versioning;
 import org.apache.maven.artifact.repository.metadata.io.xpp3.MetadataXpp3Reader;
 import org.codehaus.plexus.util.IOUtil;
 import org.codehaus.plexus.util.ReaderFactory;
 import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.sonatype.nexus.artifact.Gav;
 import org.sonatype.nexus.artifact.GavCalculator;
 import org.sonatype.nexus.artifact.IllegalArtifactCoordinateException;
 import org.sonatype.nexus.proxy.NoSuchRepositoryException;
 import org.sonatype.nexus.proxy.ResourceStoreRequest;
 import org.sonatype.nexus.proxy.StorageException;
 import org.sonatype.nexus.proxy.maven.ArtifactStoreRequest;
 import org.sonatype.nexus.proxy.maven.MavenRepository;
 import org.sonatype.nexus.proxy.registry.RepositoryRegistry;
 import org.sonatype.nexus.proxy.repository.Repository;
 
 import com.redhat.tools.nexus.audit.model.AuditInfo;
 
 import javax.inject.Inject;
 import javax.inject.Named;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.Reader;
 import java.net.URL;
 import java.util.List;
 
 public abstract class AbstractAuditStore
     implements AuditStore
 {
 
     private final Logger logger = LoggerFactory.getLogger( getClass() );
 
     @Inject
     @Named( "protected" )
     private RepositoryRegistry repoRegistry;
 
     @Inject
     @Named( "maven2" )
     private GavCalculator gavCalculator;
 
     @Inject
     @Named( "default" )
     private ArtifactRepositoryLayout repoLayout;
 
     @Inject
     @Named( "default" )
     private ArtifactFactory artifactFactory;
 
     protected abstract String getAuditFileExtension();
 
     protected RepositoryRegistry getRepositoryRegistry()
     {
         return repoRegistry;
     }
 
     protected File getStoreFile( final AuditInfo info )
         throws AuditStoreException
     {
         final Gav gav = resolveSnapshotGav( info.getRepositoryId(), info.getReferencedPath() );
 
         File auditFile;
         if ( gav != null )
         {
             auditFile = getResolvedStoreFile( info.getRepositoryId(), gav );
         }
         else
         {
             auditFile = getResolvedStoreFile( info.getRepositoryId(), info.getReferencedPath() );
         }
 
         return auditFile;
     }
 
     protected File getStoreFile( final String repoId, final Gav gav )
         throws AuditStoreException
     {
         final Gav resolved = resolveSnapshotsInGav( repoId, gav );
         return getResolvedStoreFile( repoId, resolved == null ? gav : resolved );
     }
 
     protected File getStoreFile( final String repoId, final String path )
         throws AuditStoreException
     {
         final Gav resolved = resolveSnapshotGav( repoId, path );
         if ( resolved == null )
         {
             return getResolvedStoreFile( repoId, path );
         }
         else
         {
             return getResolvedStoreFile( repoId, resolved );
         }
     }
 
     protected MavenRepository getRepository( final String repoId )
         throws AuditStoreException
     {
         try
         {
             return repoRegistry.getRepositoryWithFacet( repoId, MavenRepository.class );
         }
         catch ( final NoSuchRepositoryException e )
         {
             throw new AuditStoreException( "Failed to retrieve maven repository: %s\nReason: %s", e, repoId,
                                            e.getMessage() );
         }
     }
 
     private File getResolvedStoreFile( final Repository repository, final ResourceStoreRequest request )
         throws AuditStoreException
     {
         URL url;
         try
         {
             url = repository.getLocalStorage().getAbsoluteUrlFromBase( repository, request );
         }
         catch ( final StorageException e )
         {
             throw new AuditStoreException(
                                            "Failed to retrieve local-storage URL.\nPath: %s\nRepository-Id: %s\nReason: %s",
                                            e, request.getRequestPath(), repository.getId(), e.getMessage() );
         }
 
         File file;
 
         try
         {
             file = new File( url.toURI() );
         }
         catch ( final Throwable t )
         {
             file = new File( url.getPath() );
         }
 
         String artifactPath;
         try
         {
             artifactPath = file.getCanonicalPath();
         }
         catch ( final IOException e )
         {
             artifactPath = file.getAbsolutePath();
         }
 
         return new File( artifactPath + ".audit." + getAuditFileExtension() );
     }
 
     private Gav resolveSnapshotGav( final String repoId, final String path )
         throws AuditStoreException
     {
         Gav gav = null;
         try
         {
             gav = gavCalculator.pathToGav( path );
         }
         catch ( final IllegalArtifactCoordinateException e )
         {
             logger.debug( String.format( "Cannot parse artifact coordinate from path: %s\nError: ", path,
                                          e.getMessage() ) );
         }
 
         if ( gav != null )
         {
             gav = resolveSnapshotsInGav( repoId, gav );
         }
 
         return gav;
     }
 
     @SuppressWarnings( "unchecked" )
     private Gav resolveSnapshotsInGav( final String repoId, final Gav src )
         throws AuditStoreException
     {
         Gav gav = src;
 
         if ( ArtifactUtils.isSnapshot( gav.getVersion() ) )
         {
             final Artifact a =
                 artifactFactory.createArtifactWithClassifier( gav.getGroupId(), gav.getArtifactId(), gav.getVersion(),
                                                               gav.getExtension(), gav.getClassifier() );
 
             logger.info( String.format( "Resolving snapshot version in: '%s'", a.getId() ) );
             final SnapshotArtifactRepositoryMetadata m = new SnapshotArtifactRepositoryMetadata( a );
 
             final String mPath = repoLayout.pathOfRemoteRepositoryMetadata( m );
             final File mappingFile = getMetadataFile( repoId, mPath );
 
             Reader reader = null;
             String version = null;
             try
             {
                 reader = ReaderFactory.newXmlReader( mappingFile );
 
                 final MetadataXpp3Reader mappingReader = new MetadataXpp3Reader();
 
                 final Metadata metadata = mappingReader.read( reader, false );
                 if ( metadata != null && metadata.getVersioning() != null )
                 {
                     final Versioning versioning = metadata.getVersioning();
                    final Snapshot snapshot = versioning.getSnapshot();
                    if ( snapshot != null )
                    {
                        logger.info( String.format( "found snapshot metadata. timestamp: '%s'; build-number: '%s'",
                                                    snapshot.getTimestamp(), snapshot.getBuildNumber() ) );

                        version = snapshot.getTimestamp() + "-" + snapshot.getBuildNumber();
                    }
 
                     if ( version == null )
                     {
                        version = versioning.getLatest();
 
                        logger.info( String.format( "latest version is: '%s'", version ) );
                        if ( version == null )
                         {
                            final List<String> versions = versioning.getVersions();
                            logger.info( String.format(
                                                        "latest version missing; checking versions list with %d entries.",
                                                        ( versions == null ? 0 : versions.size() ) ) );

                            if ( versions != null && !versions.isEmpty() )
                            {
                                logger.info( String.format( "using version from top of versions list: '%s'", version ) );
                                version = versions.get( 0 );
                            }
                         }
                     }
 
                     if ( version != null )
                     {
                         logger.info( String.format( "resolved snapshot to: '%s'", version ) );
                         gav =
                             new Gav( gav.getGroupId(), gav.getArtifactId(), version, gav.getClassifier(),
                                      gav.getExtension(), null, null, null, false, false, null, false, null );
                     }
                 }
             }
             catch ( final FileNotFoundException e )
             {
                 throw new AuditStoreException( "Cannot read metadata from '" + mappingFile + "'", e );
             }
             catch ( final IOException e )
             {
                 throw new AuditStoreException( "Cannot read metadata from '" + mappingFile + "': " + e.getMessage(), e );
             }
             catch ( final XmlPullParserException e )
             {
                 throw new AuditStoreException( "Cannot read metadata from '" + mappingFile + "': " + e.getMessage(), e );
             }
             catch ( final IllegalArtifactCoordinateException e )
             {
                 throw new AuditStoreException( "Invalid artifact coordinate. Error: %s\nGroup-Id: %s\nArtifact-Id: %s"
                     + "\nVersion: %s\nClassifier: %s\nExtension: %s", e, e.getMessage(), gav.getGroupId(),
                                                gav.getArtifactId(), version, gav.getClassifier(), gav.getExtension() );
             }
             finally
             {
                 IOUtil.close( reader );
             }
         }
         else
         {
             // let the process work...
             gav = null;
         }
 
         return gav;
     }
 
     private File getMetadataFile( final String repoId, final String path )
         throws AuditStoreException
     {
         final MavenRepository repository = getRepository( repoId );
         final ResourceStoreRequest request = new ResourceStoreRequest( path, false );
 
         URL url;
         try
         {
             url = repository.getLocalStorage().getAbsoluteUrlFromBase( repository, request );
         }
         catch ( final StorageException e )
         {
             throw new AuditStoreException(
                                            "Failed to retrieve local-storage URL.\nPath: %s\nRepository-Id: %s\nReason: %s",
                                            e, request.getRequestPath(), repository.getId(), e.getMessage() );
         }
 
         File file;
 
         try
         {
             file = new File( url.toURI() );
         }
         catch ( final Throwable t )
         {
             file = new File( url.getPath() );
         }
 
         try
         {
             file = file.getCanonicalFile();
         }
         catch ( final IOException e )
         {
             file = file.getAbsoluteFile();
         }
 
         return file;
     }
 
     private File getResolvedStoreFile( final String repoId, final String path )
         throws AuditStoreException
     {
         final MavenRepository repo = getRepository( repoId );
         return getResolvedStoreFile( repo, new ResourceStoreRequest( path, false ) );
     }
 
     private File getResolvedStoreFile( final String repoId, final Gav gav )
         throws AuditStoreException
     {
         final MavenRepository repo = getRepository( repoId );
         return getResolvedStoreFile( repo, new ArtifactStoreRequest( repo, gav, false ) );
     }
 
 }
