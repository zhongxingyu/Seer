 package org.yestech.publish.publisher;
 
 import org.apache.commons.httpclient.Credentials;
 import org.apache.commons.httpclient.HttpClient;
 import org.apache.commons.httpclient.UsernamePasswordCredentials;
 import org.apache.commons.httpclient.auth.AuthScope;
 import org.apache.commons.httpclient.methods.InputStreamRequestEntity;
 import org.apache.commons.httpclient.methods.PutMethod;
 import org.apache.commons.httpclient.methods.RequestEntity;
 import org.apache.commons.lang.StringUtils;
 import static org.apache.commons.lang.StringUtils.isBlank;
 import static org.apache.commons.io.FileUtils.openOutputStream;
 import org.apache.commons.io.IOUtils;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Required;
 import static org.yestech.lib.util.Pair.create;
 import org.yestech.lib.util.Pair;
 import org.yestech.publish.objectmodel.*;
 import static org.yestech.publish.util.PublishUtils.generateUniqueIdentifier;
 import org.yestech.publish.util.PublishUtils;
 import org.yestech.publish.publisher.webdav.MkColMethod;
 
 import java.io.*;
 
 /**
  * Publishes to Bitgravity using their webdav api.
  * <br />
  * <ul>
  * <li>webDavPrefix - Prefix to the web dav</li>
  * <li>uriPrefix - Base URL for git hub</ul>
  * <li>username - The username used for pushing to Bitgravity</li>
  * <li>password - The password for Bitgravity</li>
  * </ul>
  *
  * @author A.J. Wright
  */
 @ProducerArtifactType(type = {ArtifactType.IMAGE, ArtifactType.VIDEO, ArtifactType.TEXT, ArtifactType.AUDIO})
 public class BitgravityWebdavPublisher extends BasePublisher implements IPublisher<IFileArtifact> {
 
     private static final Logger logger = LoggerFactory.getLogger(BitgravityWebdavPublisher.class);
     private static final String HTTP_SEPARATOR = "/";
 
     private PublisherProperties properties;
     private ArtifactType artifactType;
 
     public BitgravityWebdavPublisher() {
         properties = new PublisherProperties();
     }
 
     @Override
     public void publish(IFileArtifact artifact) {
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
 
         final String tempFileFqn = saveToDisk(artifactDirectoryName, artifactStream, uniqueFileName);
         try {
 
             HttpClient client = new HttpClient();
             Credentials creds = new UsernamePasswordCredentials(getUsername(), getPassword());
             client.getState().setCredentials(AuthScope.ANY, creds);
 
             MkColMethod mkColMethod = new MkColMethod(getDirectoryPublishUrl(artifactDirectoryName));
             client.executeMethod(mkColMethod);
 
             PutMethod putMethod = new PutMethod(getFilePublishUrl(artifactDirectoryName, uniqueFileName));
             RequestEntity requestEntity = new InputStreamRequestEntity(new BufferedInputStream(new FileInputStream(tempFileFqn)));
             putMethod.setRequestEntity(requestEntity);
             client.executeMethod(putMethod);
             putMethod.releaseConnection();
 
             logger.debug(putMethod.getStatusCode() + " " + putMethod.getStatusText());
 
         } catch (IOException e) {
             logger.error(e.getMessage(), e);
 
         } catch (RuntimeException e) {
             logger.error(e.getMessage(), e);
             throw e;
         } finally {
             if (logger.isInfoEnabled()) {
                 logger.info("removing file: " + tempFileFqn);
             }
             File uploadedFile = new File(tempFileFqn);
             if (uploadedFile.exists()) {
                 //noinspection ResultOfMethodCallIgnored
                 uploadedFile.delete();
             }
             PublishUtils.reset(artifact);
         }
     }
 
     protected void setLocation(IFileArtifactMetaData metaData, String artifactDirectoryName, String uniqueFileName) {
         String defaultLocation = metaData.getLocation();
         if (StringUtils.isBlank(defaultLocation)) {
             final StringBuilder builder = new StringBuilder();
             String location = builder.append(getUrlPrefix())
                     .append(HTTP_SEPARATOR)
                     .append(artifactDirectoryName)
                     .append(HTTP_SEPARATOR)
                     .append(uniqueFileName)
                     .toString();
             metaData.setLocation(location);
         }
     }
 
     protected String getFilePublishUrl(String artifactDirectoryName, String uniqueFileName) {
        return new StringBuilder(getWebDavPrefix())
                 .append(HTTP_SEPARATOR)
                 .append(artifactDirectoryName)
                 .append(HTTP_SEPARATOR)
                 .append(uniqueFileName)
                 .toString();
     }
 
     protected String getDirectoryPublishUrl(String artifactDirectoryName) {
         return new StringBuilder(getWebDavPrefix())
                 .append(HTTP_SEPARATOR)
                 .append(artifactDirectoryName)
                 .toString();
     }
 
     @Required
     public void setArtifactType(ArtifactType artifactType) {
         this.artifactType = artifactType;
     }
 
     @Required
     public void setProperties(PublisherProperties properties) {
         this.properties = properties;
     }
 
     public String getUrlPrefix() {
         return properties.getProperty(create(getArtifactType(), "urlPrefix"));
     }
 
     public String getUsername() {
         return properties.getProperty(create(getArtifactType(), "username"));
     }
 
     public String getPassword() {
         return properties.getProperty(create(getArtifactType(), "password"));
     }
 
     public String getWebDavPrefix() {
         return properties.getProperty(create(getArtifactType(), "webDavPrefix"));
     }
 
     public ArtifactType getArtifactType() {
         return artifactType;
     }
 
     public File getTempDirectory() {
         return properties.getProperty(Pair.create(getArtifactType(), "tempDirectory"));
     }
 
     private String saveToDisk(String artifactDirectoryName, InputStream artifact, String uniqueFileName) {
         File fullPath = new File(getTempDirectory() + File.separator + artifactDirectoryName);
         if (!fullPath.exists()) {
             //noinspection ResultOfMethodCallIgnored
             fullPath.mkdirs();
         }
         String location = fullPath.getAbsolutePath() + File.separator + uniqueFileName;
         FileOutputStream outputStream = null;
         try {
             if (logger.isDebugEnabled()) {
                 logger.debug("Saving file: " + location);
             }
             outputStream = openOutputStream(new File(location));
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
 }
