 package org.jboss.pressgang.ccms.restserver.webdav.resources.hierarchy.topics;
 
 import net.java.dev.webdav.jaxrs.xml.elements.MultiStatus;
 import net.java.dev.webdav.jaxrs.xml.elements.Response;
 import org.jboss.pressgang.ccms.restserver.webdav.utils.MathUtils;
 import org.jboss.pressgang.ccms.restserver.webdav.utils.WebDavUtils;
 import org.jboss.pressgang.ccms.restserver.webdav.resources.InternalResource;
 import org.jboss.pressgang.ccms.restserver.webdav.resources.MultiStatusReturnValue;
 import org.jboss.pressgang.ccms.restserver.webdav.managers.DeleteManager;
 
 import javax.persistence.EntityManager;
 import javax.ws.rs.core.UriInfo;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.logging.Logger;
 import java.util.regex.Matcher;
 
 /**
     Represents a folder that can hold topics.
  */
 public class InternalResourceTopicVirtualFolder extends InternalResource {
 
     public static final String RESOURCE_NAME = "TOPICS";
     private static final Logger LOGGER = Logger.getLogger(InternalResourceTopicVirtualFolder.class.getName());
 
     public InternalResourceTopicVirtualFolder(final UriInfo uriInfo, final String stringId) {
         super(uriInfo, stringId);
     }
 
     @Override
     public MultiStatusReturnValue propfind(final DeleteManager deleteManager, final int depth) {
 
         LOGGER.info("ENTER InternalResourceTopicVirtualFolder.propfind() " + depth + " " + getStringId());
 
         if (getUriInfo() == null) {
             throw new IllegalStateException("Can not perform propfind without uriInfo");
         }
 
         if (depth == 0) {
             /* A depth of zero means we are returning information about this item only */
             return new MultiStatusReturnValue(207, new MultiStatus(getFolderProperties(getUriInfo())));
         } else {
             /* Otherwise we are retuning info on the children in this collection */
 
             EntityManager entityManager = null;
             try {
 
                 final Matcher matcher = InternalResource.TOPIC_FOLDER_RE.matcher(getStringId());
 
                 if (!matcher.matches()) {
                     throw new IllegalStateException("This regex should always match");
                 }
 
                 /* var is an optional match */
                 final String var = matcher.group("var");
                LOGGER.info("var: " + var);
 
                 entityManager = WebDavUtils.getEntityManager(false);
 
                 final Integer minId = entityManager.createQuery("SELECT MIN(topic.topicId) FROM Topic topic", Integer.class).getSingleResult();
                 final Integer maxId = entityManager.createQuery("SELECT MAX(topic.topicId) FROM Topic topic", Integer.class).getSingleResult();
 
                 int maxScale = Math.abs(minId) > maxId ? Math.abs(minId) : maxId;
 
                 /* find out how large is the largest (or smallest) topic id, logarithmicaly speaking */
                 final int zeros = MathUtils.getScale(maxScale);
 
                 Integer lastPath = null;
 
                 if (!(var == null || var.isEmpty())) {
                     final String[] varElements = var.split("/");
                     StringBuilder path = new StringBuilder();
                     for (final String varElement : varElements) {
                         path.append(varElement);
                     }
                     lastPath = Integer.parseInt(path.toString());
                 }
 
                LOGGER.info("lastPath: " + lastPath);

                 final int thisPathZeros = lastPath == null ? 0 : MathUtils.getScale(lastPath);
 
                 /* we've gone too deep */
                 if (thisPathZeros > zeros) {
                     return new MultiStatusReturnValue(javax.ws.rs.core.Response.Status.NOT_FOUND.getStatusCode());
                 }
 
                 /* the response collection */
                 final List<Response> responses = new ArrayList<Response>();
 
                 /*
                     The only purpose of the directory /TOPICS/0 is to list TOPIC0.
                     Also don't list subdirectories when there is no way topics
                     could live in them.
                 */
                 if (lastPath == null || (lastPath != 0 && thisPathZeros < zeros)) {
                     for (int i = 0; i < 10; ++i) {
                         responses.add(getFolderProperties(getUriInfo(), i + ""));
                     }
                 }
 
                 /* The top level of the directory structure just lists digits, not an actual topic */
                 if (lastPath != null) {
                     responses.add(getFolderProperties(getUriInfo(), "TOPIC" + lastPath.toString()));
                 }
 
                 final MultiStatus st = new MultiStatus(responses.toArray(new net.java.dev.webdav.jaxrs.xml.elements.Response[responses.size()]));
 
                 return new MultiStatusReturnValue(207, st);
             } finally {
                 if (entityManager != null) {
                     entityManager.close();
                 }
             }
         }
     }
 }
