 package org.jboss.pressgang.ccms.restserver.webdav.internal;
 
 import net.java.dev.webdav.jaxrs.xml.elements.*;
 import org.apache.commons.io.IOUtils;
 import org.jboss.pressgang.ccms.restserver.webdav.InternalResourceRoot;
 import org.jboss.pressgang.ccms.restserver.webdav.managers.DeleteManager;
 import org.jboss.pressgang.ccms.restserver.webdav.topics.InternalResourceTopicVirtualFolder;
 import org.jboss.pressgang.ccms.restserver.webdav.topics.topic.InternalResourceTopic;
 import org.jboss.pressgang.ccms.restserver.webdav.topics.topic.fields.InternalResourceTempTopicFile;
 import org.jboss.pressgang.ccms.restserver.webdav.topics.topic.fields.InternalResourceTopicContent;
 
 import javax.inject.Inject;
 import javax.ws.rs.core.MediaType;
 import javax.ws.rs.core.Response;
 import javax.ws.rs.core.UriBuilder;
 import javax.ws.rs.core.UriInfo;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.StringWriter;
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.util.logging.Logger;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import static javax.ws.rs.core.Response.Status.OK;
 import static net.java.dev.webdav.jaxrs.xml.properties.ResourceType.COLLECTION;
 
 /**
  * The WebDAV server exposes resources from multiple locations. Some resources are found in a database, and some
  * are saved as files.
  * <p/>
  * All resources can potentially be written to, read and deleted. Copying and moving are just combinations of this
  * basic functionality.
  * <p/>
  * Instances of the InternalResource class wrap the functionality needed to read, write and delete.
  * <p/>
  * The InternalResource class is also a factory, matching url paths to the InternalResource instances that manage
  * them. This provides a simple way for the JAX-RS interface to pass off the actual implementation of these underlying
  * methods.
  * <p/>
  * This means that the WebDavResource class can defer functionality to InternalResource.
  */
 public abstract class InternalResource {
     private static final Logger LOGGER = Logger.getLogger(InternalResource.class.getName());
 
     private static final Pattern TOPIC_RE = Pattern.compile("/TOPICS(?<var>(/\\d)*)/TOPIC(?<TopicID>\\d*)");
     private static final Pattern ROOT_FOLDER_RE = Pattern.compile("/");
     private static final Pattern TOPIC_FOLDER_RE = Pattern.compile("/TOPICS(/\\d)*");
     private static final Pattern TOPIC_CONTENTS_RE = Pattern.compile("/TOPICS(/\\d)*/TOPIC\\d+/(?<TopicID>\\d+).xml");
     private static final Pattern TOPIC_TEMP_FILE_RE = Pattern.compile("/TOPICS(/\\d)*/TOPIC\\d+/.*");
 
 
     protected final Integer intId;
     protected final String stringId;
     protected final UriBuilder uriInfo;
 
     protected InternalResource(final UriBuilder uriInfo, final Integer intId) {
         this.intId = intId;
         this.stringId = null;
         this.uriInfo = uriInfo;
     }
 
     protected InternalResource(final UriBuilder uriInfo, final String stringId) {
         this.intId = null;
         this.stringId = stringId;
         this.uriInfo = uriInfo;
     }
 
     public int write(final DeleteManager deleteManager, final String contents) {
         throw new UnsupportedOperationException();
     }
 
     public int delete(final DeleteManager deleteManager) {
         throw new UnsupportedOperationException();
     }
 
     public StringReturnValue get(final DeleteManager deleteManager) {
         throw new UnsupportedOperationException();
     }
 
     public MultiStatusReturnValue propfind(final DeleteManager deleteManager, final int depth) {
         throw new UnsupportedOperationException();
     }
 
     public static javax.ws.rs.core.Response propfind(final DeleteManager deleteManager, final UriInfo uriInfo, final int depth) {
         LOGGER.info("ENTER InternalResource.propfind() " + uriInfo.getPath() + " " + depth);
 
         final InternalResource sourceResource = InternalResource.getInternalResource(uriInfo.getRequestUriBuilder());
 
         if (sourceResource != null) {
             final MultiStatusReturnValue multiStatusReturnValue = sourceResource.propfind(deleteManager, depth);
 
             if (multiStatusReturnValue.getStatusCode() != 207) {
                 return javax.ws.rs.core.Response.status(multiStatusReturnValue.getStatusCode()).build();
             }
 
             return javax.ws.rs.core.Response.status(207).entity(multiStatusReturnValue.getValue()).type(MediaType.TEXT_XML).build();
         }
 
         return javax.ws.rs.core.Response.status(javax.ws.rs.core.Response.Status.NOT_FOUND).build();
     }
 
     public static javax.ws.rs.core.Response copy(final DeleteManager deleteManager, final UriInfo uriInfo, final String overwriteStr, final String destination) {
         LOGGER.info("ENTER InternalResource.copy() " + uriInfo.getPath() + " " + destination);
 
         try {
             final HRef destHRef = new HRef(destination);
             final URI destUriInfo = destHRef.getURI();
 
             final InternalResource destinationResource = InternalResource.getInternalResource(UriBuilder.fromUri(destUriInfo));
             final InternalResource sourceResource = InternalResource.getInternalResource(uriInfo.getRequestUriBuilder());
 
             if (destinationResource != null && sourceResource != null) {
                 final StringReturnValue stringReturnValue = sourceResource.get(deleteManager);
 
                 if (stringReturnValue.getStatusCode() != javax.ws.rs.core.Response.Status.OK.getStatusCode()) {
                     return javax.ws.rs.core.Response.status(stringReturnValue.getStatusCode()).build();
                 }
 
                 int statusCode;
                 if ((statusCode = destinationResource.write(deleteManager, stringReturnValue.getValue())) != javax.ws.rs.core.Response.Status.NO_CONTENT.getStatusCode()) {
                     return javax.ws.rs.core.Response.status(statusCode).build();
                 }
 
                 return javax.ws.rs.core.Response.ok().build();
 
             }
         } catch (final URISyntaxException e) {
 
         }
 
         return javax.ws.rs.core.Response.status(javax.ws.rs.core.Response.Status.NOT_FOUND).build();
     }
 
     public static javax.ws.rs.core.Response move(final DeleteManager deleteManager, final UriInfo uriInfo, final String overwriteStr, final String destination) {
 
         LOGGER.info("ENTER InternalResource.move() " + uriInfo.getPath() + " " + destination);
 
         /*
             We can't move outside of the filesystem
          */
         if (!destination.startsWith(uriInfo.getBaseUri().toString())) {
             return javax.ws.rs.core.Response.status(Response.Status.NOT_FOUND).build();
         }
 
         final InternalResource destinationResource = InternalResource.getInternalResource(UriBuilder.fromUri("/" + destination.replaceFirst(uriInfo.getBaseUri().toString(), "")));
         final InternalResource sourceResource = InternalResource.getInternalResource(uriInfo.getRequestUriBuilder());
 
         if (destinationResource != null && sourceResource != null) {
             final StringReturnValue stringReturnValue = sourceResource.get(deleteManager);
 
             if (stringReturnValue.getStatusCode() != javax.ws.rs.core.Response.Status.OK.getStatusCode()) {
                 return javax.ws.rs.core.Response.status(stringReturnValue.getStatusCode()).build();
             }
 
             int statusCode;
             if ((statusCode = destinationResource.write(deleteManager, stringReturnValue.getValue())) != javax.ws.rs.core.Response.Status.NO_CONTENT.getStatusCode()) {
                 return javax.ws.rs.core.Response.status(statusCode).build();
             }
 
             if ((statusCode = sourceResource.delete(deleteManager)) != javax.ws.rs.core.Response.Status.NO_CONTENT.getStatusCode()) {
                 return javax.ws.rs.core.Response.status(statusCode).build();
             }
 
             return javax.ws.rs.core.Response.ok().build();
 
         }
 
         return javax.ws.rs.core.Response.status(javax.ws.rs.core.Response.Status.NOT_FOUND).build();
     }
 
     public static javax.ws.rs.core.Response delete(final DeleteManager deleteManager, final UriInfo uriInfo) {
         final InternalResource sourceResource = InternalResource.getInternalResource(uriInfo.getRequestUriBuilder());
 
         if (sourceResource != null) {
             return javax.ws.rs.core.Response.status(sourceResource.delete(deleteManager)).build();
         }
 
         return javax.ws.rs.core.Response.status(javax.ws.rs.core.Response.Status.NOT_FOUND).build();
     }
 
     public static StringReturnValue get(final DeleteManager deleteManager, final UriInfo uriInfo) {
         final InternalResource sourceResource = InternalResource.getInternalResource(uriInfo.getRequestUriBuilder());
 
         if (sourceResource != null) {
             StringReturnValue statusCode;
             if ((statusCode = sourceResource.get(deleteManager)).getStatusCode() != javax.ws.rs.core.Response.Status.OK.getStatusCode()) {
                 return statusCode;
             }
 
             return statusCode;
         }
 
         return new StringReturnValue(javax.ws.rs.core.Response.Status.NOT_FOUND.getStatusCode(), null);
     }
 
     public static javax.ws.rs.core.Response put(final DeleteManager deleteManager, final UriInfo uriInfo, final InputStream entityStream) {
         try {
             final InternalResource sourceResource = InternalResource.getInternalResource(uriInfo.getRequestUriBuilder());
 
             if (sourceResource != null) {
                 final StringWriter writer = new StringWriter();
                 IOUtils.copy(entityStream, writer, "UTF-8");
                 final String newContents = writer.toString();
 
                 int statusCode = sourceResource.write(deleteManager, newContents);
                 return javax.ws.rs.core.Response.status(statusCode).build();
             }
 
             return javax.ws.rs.core.Response.status(javax.ws.rs.core.Response.Status.NOT_FOUND).build();
         } catch (final IOException e) {
 
         }
 
         return javax.ws.rs.core.Response.serverError().build();
     }
 
     /**
      * The factory method that returns the object to handle a URL request.
      * @param uri The request URI
      * @return  The object to handle the response, or null if the URL is invalid.
      */
     public static InternalResource getInternalResource(final UriBuilder uri) {
 
 
         final String requestPath = uri.build().getPath();
 
         LOGGER.info("ENTER InternalResource.getInternalResource() " + requestPath);
 
         final Matcher topicContents = TOPIC_CONTENTS_RE.matcher(requestPath);
         if (topicContents.matches()) {
             LOGGER.info("Matched InternalResourceTopicContent");
             return new InternalResourceTopicContent(uri, Integer.parseInt(topicContents.group("TopicID")));
         }
 
         final Matcher topicTemp = TOPIC_TEMP_FILE_RE.matcher(requestPath);
         if (topicTemp.matches()) {
             LOGGER.info("Matched InternalResourceTempTopicFile");
             return new InternalResourceTempTopicFile(uri, requestPath);
         }
 
         final Matcher topicFolder = TOPIC_FOLDER_RE.matcher(requestPath);
         if (topicFolder.matches()) {
             LOGGER.info("Matched InternalResourceTopicVirtualFolder");
             return new InternalResourceTopicVirtualFolder(uri, requestPath);
         }
 
         final Matcher rootFolder = ROOT_FOLDER_RE.matcher(requestPath);
         if (rootFolder.matches()) {
             LOGGER.info("Matched InternalResourceRoot");
             return new InternalResourceRoot(uri, requestPath);
         }
 
         final Matcher topic = TOPIC_RE.matcher(requestPath);
         if (topic.matches()) {
             LOGGER.info("Matched InternalResourceTopic");
             return new InternalResourceTopic(uri, Integer.parseInt(topic.group("TopicID")));
         }
 
         return null;
     }
 
     /**
      * Returning a child folder means returning a Respose that identifies a WebDAV collection.
      * This method populates the returned request with the information required to identify
      * a child folder.
      *
      * @param requestUriBuilder      The URI of the current request
      * @param resourceName The name of the child folder
      * @return The properties for a child folder
      */
     public static net.java.dev.webdav.jaxrs.xml.elements.Response getFolderProperties(final UriBuilder requestUriBuilder, final String resourceName) {
         /*final Date lastModified = new Date(0);
         final CreationDate creationDate = new CreationDate(lastModified);
         final GetLastModified getLastModified = new GetLastModified(lastModified);
         final Prop prop = new Prop(creationDate, getLastModified, COLLECTION);*/
 
         final Prop prop = new Prop(COLLECTION);
 
         final Status status = new Status((javax.ws.rs.core.Response.StatusType) OK);
         final PropStat propStat = new PropStat(prop, status);
 
         final URI uri = requestUriBuilder.path(resourceName).build();
         final HRef hRef = new HRef(uri);
         final net.java.dev.webdav.jaxrs.xml.elements.Response folder = new net.java.dev.webdav.jaxrs.xml.elements.Response(hRef, null, null, null, propStat);
 
         return folder;
     }
 
     /**
      * @param uriInfo The URI of the current request
      * @return The properties for the current folder
      */
     public static net.java.dev.webdav.jaxrs.xml.elements.Response getFolderProperties(final UriBuilder uriInfo) {
         /*final Date lastModified = new Date(0);
         final CreationDate creationDate = new CreationDate(lastModified);
         final GetLastModified getLastModified = new GetLastModified(lastModified);
         final Prop prop = new Prop(creationDate, getLastModified, COLLECTION);*/
 
         final Prop prop = new Prop(COLLECTION);
 
         final Status status = new Status((javax.ws.rs.core.Response.StatusType) OK);
         final PropStat propStat = new PropStat(prop, status);
 
         final URI uri = uriInfo.build();
         final HRef hRef = new HRef(uri);
         final net.java.dev.webdav.jaxrs.xml.elements.Response folder = new net.java.dev.webdav.jaxrs.xml.elements.Response(hRef, null, null, null, propStat);
 
         return folder;
     }
 }
