 package de.m0ep.socc.connectors.facebook;
 
 import java.util.Date;
 
 import org.ontoware.rdf2go.model.node.URI;
 import org.ontoware.rdf2go.util.Builder;
 import org.ontoware.rdf2go.util.RDFTool;
 import org.rdfs.sioc.Container;
 import org.rdfs.sioc.Forum;
 import org.rdfs.sioc.Post;
 import org.rdfs.sioc.UserAccount;
 
 import com.google.common.base.Preconditions;
 import com.restfb.json.JsonObject;
 import com.restfb.types.Comment;
 import com.restfb.types.Group;
 import com.restfb.types.User;
 
 import de.m0ep.socc.exceptions.ConnectorException;
 import de.m0ep.socc.utils.DateUtils;
 import de.m0ep.socc.utils.RDF2GoUtils;
 import de.m0ep.socc.utils.SIOCUtils;
 import de.m0ep.socc.utils.StringUtils;
 
 public class FacebookSIOCConverter {
     /**
      * Create a {@link UserAccount} from a RestFB Facebook {@link User}.
      * 
      * @param connector
      *            A {@link FacebookConnector}.
      * @param user
      *            A RestFB Facebook {@link User}.
      * @param uri
      *            {@link URI} of the new {@link UserAccount}.
      * @return A {@link UserAccount} from a Facebook {@link User}.
      * 
      * @throws ConnectorException
      *             Thrown if the connector reported a problem.
      * @throws NullPointerException
      *             Thrown if one or more parameters are null
      */
     public static UserAccount createUserAccount(
 	    final FacebookConnector connector, final User user, final URI uri)
 	    throws ConnectorException {
 	Preconditions.checkNotNull(connector, "connector can not be null");
 	Preconditions.checkNotNull(user, "user can not be null");
 	Preconditions.checkNotNull(uri, "uri can not be null");
 
 	UserAccount result = new UserAccount(
 		connector.getContext().getDataModel(),
 		uri,
 		true);
 
 	result.setId(user.getId());
 	result.setIsPartOf(connector.getSite());
	result.setAccountServiceHomepage(Builder.createURI(connector
		.getURL()));
 
 	if (null != user.getEmail() && !user.getEmail().isEmpty()) {
 	    result.addEmail(RDF2GoUtils.createMailtoURI(user.getEmail()));
 	    result.addEmailSha1(RDFTool.sha1sum(user.getEmail()));
 	}
 
 	if (null != user.getUsername() && !user.getUsername().isEmpty()) {
 	    result.setAccountName(user.getUsername());
 	}
 
 	if (null != user.getName() && !user.getName().isEmpty()) {
 	    result.setName(user.getName());
 	}
 
 	if (null != user.getUpdatedTime()) {
 	    result.setModified(DateUtils.formatISO8601(user.getUpdatedTime()));
 	}
 
 	return result;
     }
 
     /**
      * Creates a SIOC {@link Forum} from a RestFB Facebook {@link Group}.
      * 
      * @param connector
      *            A {@link FacebookConnector}.
      * @param group
      *            A RestFB Facebook {@link Group}.
      * @param uri
      *            {@link URI} of the new {@link Forum}.
      * @return A SIOC {@link Forum} from a Facebook {@link Group}.
      * 
      * @throws ConnectorException
      *             Thrown if the connector reported a problem.
      * @throws NullPointerException
      *             Thrown if one or more parameters are null
      */
     public static Forum createForum(final FacebookConnector connector,
 	    final Group group, final URI uri) throws ConnectorException {
 	Preconditions.checkNotNull(connector, "connector can not be null");
 	Preconditions.checkNotNull(group, "group can not be null");
 	Preconditions.checkNotNull(uri, "uri can not be null");
 
 	Forum result = new Forum(
 		connector.getContext().getDataModel(),
 		uri,
 		true);
 	result.setId(group.getId());
 	result.setHost(connector.getSite());
 	connector.getSite().addHostOf(result);
 
 	if (null != group.getName()) {
 	    result.setName(group.getName());
 	}
 
 	if (null != group.getDescription()) {
 	    result.setDescription(group.getDescription());
 	}
 
 	if (null != group.getUpdatedTime()) {
 	    result.setModified(DateUtils.formatISO8601(group.getUpdatedTime()));
 	}
 
 	return result;
     }
 
     /**
      * Creates a SIOC {@link Post} form a RestFB Facebook
      * {@link com.restfb.types.Post} fetched as a {@link JsonObject}.
      * 
      * @param connector
      *            A {@link FacebookConnector}.
      * @param container
      *            {@link Container} where this post is made.
      * @param obj
      *            {@link JsonObject} from a RestFB {@link com.restfb.types.Post}
      *            fetch.
      * @param uri
      *            {@link URI} of the new {@link Post}.
      * 
      * @return A SIOC {@link Post} from a Facebook {@link com.restfb.types.Post}
      *         {@link JsonObject}.
      * 
      * @throws ConnectorException
      *             Thrown if the connector reported a problem.
      * @throws NullPointerException
      *             Thrown if one or more parameters are null
      */
     public static Post createPost(final FacebookConnector connector,
 	    final Container container, final JsonObject obj, final URI uri)
 	    throws ConnectorException {
 	Preconditions.checkNotNull(connector, "connector can not be null");
 	Preconditions.checkNotNull(container, "container can not be null");
 	Preconditions.checkNotNull(obj, "obj can not be null");
 	Preconditions.checkNotNull(uri, "uri can not be null");
 
 	Post result = new Post(
 		connector.getContext().getDataModel(),
 		uri,
 		true);
 
 	if (obj.has(FacebookConstants.FIELD_ID)) {
 	    result.setId(obj.getString(FacebookConstants.FIELD_ID));
 	}
 
 	if (obj.has(FacebookConstants.FIELD_FROM)) {
 	    result.setCreator(
 		    connector.getUserAccount(
 			    obj.getJsonObject(
 				    FacebookConstants.FIELD_FROM).getString(
 				    FacebookConstants.FIELD_ID)));
 	}
 
 	String content = "";
 	if (obj.has(FacebookConstants.FIELD_STORY)) {
 	    content = obj.getString(FacebookConstants.FIELD_STORY);
 	} else if (obj.has(FacebookConstants.FIELD_MESSAGE)) {
 	    content = obj.getString(FacebookConstants.FIELD_MESSAGE);
 	}
 
 	result.setContent(StringUtils.stripHTML(content));
 	result.setContentEncoded(RDF2GoUtils.createCDATASection(content));
 
 	if (obj.has(FacebookConstants.FIELD_NAME)) {
 	    result.setName(obj.getString(FacebookConstants.FIELD_NAME));
 	}
 
 	if (obj.has(FacebookConstants.FIELD_CAPTION)) {
 	    result.setTitle(FacebookConstants.FIELD_CAPTION);
 	}
 
 	if (obj.has(FacebookConstants.FIELD_DESCRIPTION)) {
 	    result.setDescription(obj
 		    .getString(FacebookConstants.FIELD_DESCRIPTION));
 	}
 
 	if (obj.has(FacebookConstants.FIELD_LINK)) {
 	    result.setAttachment(Builder.createURI(obj
 		    .getString(FacebookConstants.FIELD_LINK)));
 	} else if (obj.has(FacebookConstants.FIELD_SOURCE)) {
 	    result.setAttachment(Builder.createURI(obj
 		    .getString(FacebookConstants.FIELD_SOURCE)));
 	}
 
 	if (obj.has(FacebookConstants.FIELD_CREATED_TIME)) {
 	    Date date = com.restfb.util.DateUtils.toDateFromLongFormat(obj
 		    .getString(FacebookConstants.FIELD_CREATED_TIME));
 	    result.setCreated(DateUtils.formatISO8601(date));
 	}
 
 	if (obj.has(FacebookConstants.FIELD_UPDATED_TIME)) {
 	    Date date = com.restfb.util.DateUtils.toDateFromLongFormat(obj
 		    .getString(FacebookConstants.FIELD_UPDATED_TIME));
 	    result.setModified(DateUtils.formatISO8601(date));
 	}
 
 	result.setContainer(container);
 	container.addContainerOf(result);
 	SIOCUtils.updateLastItemDate(container, result);
 
 	return result;
     }
 
     /**
      * Creates a SIOC {@link Post} form a RestFB Facebook {@link Comment}
      * fetched as a {@link JsonObject}.
      * 
      * @param connector
      *            A {@link FacebookConnector}.
      * @param parentPost
      *            The parent {@link Post} of the comment
      * @param obj
      *            {@link JsonObject} from a RestFB {@link Comment} fetch.
      * @param uri
      *            {@link URI} of the new {@link Post}.
      * @return A SIOC {@link Post} from a Facebook {@link Comment}
      *         {@link JsonObject}.
      * 
      * @throws ConnectorException
      *             Thrown if the connector reported a problem.
      * @throws NullPointerException
      *             Thrown if one or more parameters are null
      */
     public static Post createComment(final FacebookConnector connector,
 	    final Post parentPost, final JsonObject obj, final URI uri)
 	    throws ConnectorException {
 	Preconditions.checkNotNull(connector, "connector can not be null");
 	Preconditions.checkNotNull(parentPost, "parentPost can not be null");
 	Preconditions.checkNotNull(obj, "obj can not be null");
 	Preconditions.checkNotNull(uri, "uri can not be null");
 
 	Post result = new Post(
 		connector.getContext().getDataModel(),
 		uri,
 		true);
 
 	if (obj.has(FacebookConstants.FIELD_ID)) {
 	    result.setId(obj.getString(FacebookConstants.FIELD_ID));
 	}
 
 	if (obj.has(FacebookConstants.FIELD_FROM)) {
 	    result.setCreator(connector.getUserAccount(obj.getJsonObject(
 		    FacebookConstants.FIELD_FROM).getString(
 		    FacebookConstants.FIELD_ID)));
 	}
 
 	String content = "";
 	if (obj.has(FacebookConstants.FIELD_MESSAGE)) {
 	    content = obj.getString(FacebookConstants.FIELD_MESSAGE);
 	}
 
 	result.setContent(StringUtils.stripHTML(content));
 	result.setContentEncoded(RDF2GoUtils.createCDATASection(content));
 
 	if (obj.has(FacebookConstants.FIELD_CREATED_TIME)) {
 	    Date date = com.restfb.util.DateUtils.toDateFromLongFormat(obj
 		    .getString(FacebookConstants.FIELD_CREATED_TIME));
 	    result.setCreated(DateUtils.formatISO8601(date));
 	}
 
 	if (parentPost.hasContainers()) {
 	    Container container = parentPost.getContainer();
 	    result.setContainer(container);
 	    container.addContainerOf(result);
 	    SIOCUtils.updateLastItemDate(container, result);
 	}
 
 	result.setReplyOf(parentPost);
 	parentPost.addReply(result);
 	SIOCUtils.updateLastReplyDate(parentPost, result);
 
 	return result;
     }
 }
