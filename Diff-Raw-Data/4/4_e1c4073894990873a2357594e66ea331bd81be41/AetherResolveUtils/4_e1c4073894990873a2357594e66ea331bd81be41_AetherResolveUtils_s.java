 package net.lmxm.ute.utils.aether;
 
 import net.lmxm.ute.event.StatusChangeHelper;
 import org.apache.maven.repository.internal.MavenRepositorySystemSession;
 import org.codehaus.plexus.DefaultPlexusContainer;
 import org.codehaus.plexus.util.FileUtils;
 import org.sonatype.aether.RepositorySystem;
 import org.sonatype.aether.RepositorySystemSession;
 import org.sonatype.aether.artifact.Artifact;
 import org.sonatype.aether.repository.LocalRepository;
 import org.sonatype.aether.repository.RemoteRepository;
 import org.sonatype.aether.resolution.ArtifactRequest;
 import org.sonatype.aether.resolution.ArtifactResult;
 import org.sonatype.aether.util.artifact.DefaultArtifact;
 
 import java.io.File;
 import java.io.IOException;
 
 /**
  * Aether library wrapper used to resolve Maven artifacts and copy to them a destination directory.
  */
 public final class AetherResolveUtils {
 
     /**
      * Maven repository identifier used by Aether.
      */
     private static final String REPOSITORY_ID = "ute";
 
     /**
      * Maven repostiroy type used by Aether.
      */
     private static final String REPOSITORY_TYPE = "default";
 
     /**
      * Remote Maven repository handle.
      */
     private final RemoteRepository remoteRepository;
 
     /**
      * Status change helper used to relay changes of status to the caller.
      */
     private final StatusChangeHelper statusChangeHelper;
 
     /**
      * Remove Maven repository system handle.
      */
     private final RepositorySystem system;
 
     /**
      * Create a new utils instance that will work with the Maven repository at the provided URL.
      *
      * @param repositoryUrl URL of the Maven repository to work with
      * @throws Exception Error occurred during initialization of Aether
      */
     public AetherResolveUtils(final String repositoryUrl, final StatusChangeHelper statusChangeHelper) throws Exception {
         super();
 
         remoteRepository = new RemoteRepository(REPOSITORY_ID, REPOSITORY_TYPE, repositoryUrl);
         system = new DefaultPlexusContainer().lookup(RepositorySystem.class);
         this.statusChangeHelper = statusChangeHelper;
     }
 
     /**
      * Creates a temporary directory to be used as a local repository base. This is the file system directory that will
      * be used to temporarily hold downloaded artifacts. The directory created is uniquely named and should not be
      * used by other executions.
      *
      * @return Unique, temporary directory to be used as a local repository base
      */
     private File createLocalRepositoryBaseDirectory() {
         return FileUtils.createTempFile(REPOSITORY_ID, "aether-resolve", null);
     }
 
     /**
      * Create and configures a Maven repository system session. This method initializes the Aether API objects used to
      * hold session information.
      *
      * @param system                       Maven repository system to work with
      * @param localRepositoryBaseDirectory Location of the local repository where files will be downloaded
      * @return Fully configured Maven repository system session instance
      */
     private RepositorySystemSession newRepositorySystemSession(final RepositorySystem system, final File localRepositoryBaseDirectory) {
         final MavenRepositorySystemSession session = new MavenRepositorySystemSession();
 
         final LocalRepository localRepository = new LocalRepository(localRepositoryBaseDirectory);
         session.setLocalRepositoryManager(system.newLocalRepositoryManager(localRepository));
         session.setTransferListener(new AetherTransferListener(statusChangeHelper));
         session.setRepositoryListener(new AetherRepositoryListener(statusChangeHelper));
 
         return session;
     }
 
     /**
      * Resolves, downloads and moves targeted artifact to the provided destination directory. This is the main entry
      * point into this utils class and performs all the necessary work to download an artifact from Maven and put the
      * artifact in the expected location.
      *
      * @param artifactCoordinates  Maven coordinates (GAV) of the artifact to download (groupId:artifactId:version)
      * @param destinationDirectory Directory where the artifact will be copied when done. This file must exist and must
      *                             be a directory.
      * @param targetName           Optional target name for the downlaoded artifact.
      */
     public void resolveArtifact(final String artifactCoordinates, final File destinationDirectory, final String targetName) {
         final File localRepositoryBaseDirectory = createLocalRepositoryBaseDirectory();
 
         try {
             final ArtifactRequest artifactRequest = new ArtifactRequest();
             artifactRequest.setArtifact(new DefaultArtifact(artifactCoordinates));
             artifactRequest.addRepository(remoteRepository);
 
             final RepositorySystemSession session = newRepositorySystemSession(system, localRepositoryBaseDirectory);
             final ArtifactResult artifactResult = system.resolveArtifact(session, artifactRequest);
             final Artifact artifact = artifactResult.getArtifact();
 
            if (targetName == null) {
                 FileUtils.copyFileToDirectory(artifact.getFile(), destinationDirectory);
             }
             else {
                 FileUtils.copyFile(artifact.getFile(), new File(destinationDirectory, targetName));
             }
         }
         catch (Exception e) {
             // TODO
             e.printStackTrace();
         }
         finally {
             try {
                 FileUtils.deleteDirectory(localRepositoryBaseDirectory);
             }
             catch (IOException e) {
                 System.err.println("Unable to delete " + localRepositoryBaseDirectory);
             }
         }
     }
 }
