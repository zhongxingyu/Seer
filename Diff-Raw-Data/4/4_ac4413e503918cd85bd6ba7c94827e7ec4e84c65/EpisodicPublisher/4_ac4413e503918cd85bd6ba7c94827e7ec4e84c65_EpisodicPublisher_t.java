 package org.yestech.publish.publisher;
 
 import org.apache.commons.io.IOUtils;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Required;
 import org.yestech.episodic.DefaultEpisodicService;
 import org.yestech.episodic.EpisodicService;
 import org.yestech.lib.util.Pair;
 import org.yestech.publish.objectmodel.*;
 import org.yestech.publish.objectmodel.episodic.IEpisodicArtifact;
 import org.yestech.publish.objectmodel.episodic.IEpisodicArtifactPersister;
 import org.yestech.publish.util.PublishUtils;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 
 import static org.apache.commons.io.FileUtils.openOutputStream;
 import static org.apache.commons.lang.StringUtils.isBlank;
 import static org.yestech.lib.util.Pair.create;
 import static org.yestech.publish.util.PublishUtils.generateUniqueIdentifier;
 
 /**
  * Publishes videos to episodic by creating a unique episode with 1 asset under a given show.
  * <p/>
  * <br />
  * <ul>
  * <li>secret - The episodic secret key.</li>
  * <li>api_key - The episodic api key</ul>
  * <li>show_id - The episodic show to publish artifacts as episodes too.</li>
  * </ul>
  * <p/>
  * Optionally It is possible to save the episodeId and assetId back to the artifact if desired.
  * The following must be done in order for this functionality to work:
  * <ol>
  * <li>Make your artfact object implement {@link org.yestech.publish.objectmodel.episodic.IEpisodicArtifact}
  * <li>Provide an implementation of {@link org.yestech.publish.objectmodel.episodic.IEpisodicArtifactPersister} to the publisher
  * via the {@link #setPersister(org.yestech.publish.objectmodel.episodic.IEpisodicArtifactPersister)} method.
  * </ol>
  *
  * @author A.J. Wright
  */
 @ProducerArtifactType(type = {ArtifactType.VIDEO})
 public class EpisodicPublisher extends BasePublisher implements IPublisher<IFileArtifact> {
 
     private static final Logger logger = LoggerFactory.getLogger(EpisodicPublisher.class);
 
     private PublisherProperties properties;
     private ArtifactType artifactType;
     private IEpisodicArtifactPersister persister;
 
     public EpisodicPublisher() {
         properties = new PublisherProperties();
     }
 
     @Override
     public void publish(IFileArtifact artifact) {
         if (logger.isDebugEnabled()) {
             logger.debug("EpisodicPublisher called for " + artifact);
         }
 
         IFileArtifactMetaData metaData = artifact.getArtifactMetaData();
         InputStream artifactStream = artifact.getStream();
 
         String artifactDirectoryName = (String) metaData.getUniqueNames().getFirst();
         if (isBlank(artifactDirectoryName)) {
             artifactDirectoryName = generateUniqueIdentifier(metaData.getArtifactOwner());
         }
 
         String uniqueFileName = (String) metaData.getUniqueNames().getSecond();
         if (isBlank(uniqueFileName)) {
             uniqueFileName = generateUniqueIdentifier(metaData);
         }
 
         final File tempFile = saveToDisk(artifactDirectoryName, artifactStream, uniqueFileName);
         try {
 
             EpisodicService episodicService = buildEpisodicService();
             String assetId = episodicService.createAsset(getShowId(), uniqueFileName, tempFile);
 
             if (assetId != null || !"".equals(assetId)) {
                 String episodeId = episodicService.createEpisode(getShowId(), uniqueFileName,
                         new String[]{assetId}, true, null, getPingUrl());
 
                if (metaData instanceof IEpisodicArtifact) {
                    IEpisodicArtifact ea = (IEpisodicArtifact) metaData;
                     ea.setAssetId(assetId);
                     ea.setEpisodeId(episodeId);
 
                     if (persister != null) {
                         persister.save(ea);
                     } else {
                         logger.warn("Artifact is an IEpisodicArtfact, but no IEpisodicArtifactPersister was supplied.");
                     }
 
                 }
             }
 
         } catch (RuntimeException e) {
             logger.error(e.getMessage(), e);
             throw e;
         } finally {
             if (logger.isInfoEnabled()) {
                 logger.info("removing file: " + tempFile);
             }
             if (tempFile.exists()) {
                 //noinspection ResultOfMethodCallIgnored
                 tempFile.delete();
             }
             PublishUtils.reset(artifact);
         }
     }
 
 
     @Required
     public void setArtifactType(ArtifactType artifactType) {
         this.artifactType = artifactType;
     }
 
     @Required
     public void setProperties(PublisherProperties properties) {
         this.properties = properties;
     }
 
     public void setPersister(IEpisodicArtifactPersister persister) {
         this.persister = persister;
     }
 
     public String getSecret() {
         return properties.getProperty(create(getArtifactType(), "secret"));
     }
 
     public String getApiKey() {
         return properties.getProperty(create(getArtifactType(), "api_key"));
     }
 
     public String getShowId() {
         return properties.getProperty(create(getArtifactType(), "show_id"));
     }
 
     public String getPingUrl() {
         return properties.getProperty(create(getArtifactType(), "ping_url"));
     }
 
     public String proxyHost() {
         return properties.getProperty(create(getArtifactType(), "proxy_host"));
     }
 
     public String proxyPort() {
         return properties.getProperty(create(getArtifactType(), "proxy_port"));
     }
 
     public ArtifactType getArtifactType() {
         return artifactType;
     }
 
     public File getTempDirectory() {
         return properties.getProperty(Pair.create(getArtifactType(), "tempDirectory"));
     }
 
     private File saveToDisk(String artifactDirectoryName, InputStream artifact, String uniqueFileName) {
         File fullPath = new File(getTempDirectory() + File.separator + artifactDirectoryName);
         if (!fullPath.exists()) {
             //noinspection ResultOfMethodCallIgnored
             fullPath.mkdirs();
         }
         File location = new File(fullPath.getAbsolutePath(), uniqueFileName);
         FileOutputStream outputStream = null;
         try {
             if (logger.isDebugEnabled()) {
                 logger.debug("Saving file: " + location);
             }
             outputStream = openOutputStream(location);
             IOUtils.copyLarge(artifact, outputStream);
             outputStream.flush();
             if (logger.isDebugEnabled()) {
                 logger.debug("Saved file: " + location);
             }
         } catch (IOException e) {
             logger.error(e.getMessage(), e);
         }
         finally {
             IOUtils.closeQuietly(artifact);
             IOUtils.closeQuietly(outputStream);
         }
         return location;
     }
 
     protected EpisodicService buildEpisodicService() {
         String proxyHost = proxyHost();
         String proxyPort = proxyPort();
         if (proxyHost != null && proxyPort != null) {
             return new DefaultEpisodicService(getSecret(), getApiKey(), proxyHost, Integer.parseInt(proxyPort));
         } else {
             return new DefaultEpisodicService(getSecret(), getApiKey());
         }
     }
 }
