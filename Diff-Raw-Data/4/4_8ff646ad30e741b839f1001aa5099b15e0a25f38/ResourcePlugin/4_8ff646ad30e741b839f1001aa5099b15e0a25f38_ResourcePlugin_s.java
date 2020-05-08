 package org.deepamehta.plugins.eduzen;
 
 import java.util.logging.Logger;
 import java.util.List;
 import java.util.Date;
 
 import javax.ws.rs.GET;
 import javax.ws.rs.HeaderParam;
 import javax.ws.rs.POST;
 import javax.ws.rs.Path;
 import javax.ws.rs.PathParam;
 import javax.ws.rs.Produces;
 import javax.ws.rs.Consumes;
 import javax.ws.rs.WebApplicationException;
 
 import de.deepamehta.core.Topic;
 import de.deepamehta.core.ResultSet;
 import de.deepamehta.core.model.TopicModel;
 import de.deepamehta.core.RelatedTopic;
 import de.deepamehta.core.model.CompositeValueModel;
 import de.deepamehta.core.service.ClientState;
 import de.deepamehta.core.service.Directives;
 import de.deepamehta.core.storage.spi.DeepaMehtaTransaction;
 import de.deepamehta.plugins.webactivator.WebActivatorPlugin;
 
 import org.deepamehta.plugins.eduzen.service.ResourceService;
 
 import com.sun.jersey.api.view.Viewable;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 import org.codehaus.jettison.json.JSONArray;
 
 
 @Path("/notes")
 @Consumes("application/json")
 @Produces("text/html")
 public class ResourcePlugin extends WebActivatorPlugin implements ResourceService {
 
     private Logger log = Logger.getLogger(getClass().getName());
 
     private final static String RESOURCE_URI = "org.deepamehta.resources.resource";
     private final static String RESOURCE_CONTENT_URI = "org.deepamehta.resources.content";
     private final static String RESOURCE_CREATED_AT_URI = "org.deepamehta.resources.created_at";
     private final static String RESOURCE_LAST_MODIFIED_URI = "org.deepamehta.resources.last_modified_at";
     private final static String RESOURCE_PUBLISHED_URI = "org.deepamehta.resources.is_published";
     private final static String RESOURCE_LOCKED_URI = "org.deepamehta.resources.blocked_for_edits";
     private final static String RESOURCE_LICENSE_URI = "org.deepamehta.resources.license";
     private final static String RESOURCE_LICENSE_UNSPECIFIED_URI = "org.deepamehta.licenses.unspecified";
     private final static String RESOURCE_LICENSE_UNKNOWN_URI = "org.deepamehta.licenses.unknown";
     private final static String RESOURCE_LICENSE_AREA_URI = "org.deepamehta.resources.license_jurisdiction";
 
     private final static String CREATOR_EDGE_URI = "org.deepamehta.resources.creator_edge";
     private final static String CONTRIBUTOR_EDGE_URI = "org.deepamehta.resources.contributor_edge";
     private final static String CHILD_TYPE_URI = "dm4.core.child";
     private final static String PARENT_TYPE_URI = "dm4.core.parent";
     private final static String DEFAULT_ROLE_TYPE_URI = "dm4.core.default";
     private final static String ACCOUNT_TYPE_URI = "dm4.accesscontrol.user_account";
 
     @Override
     public void init() {
         setupRenderContext();
     }
 
     /**
      * Creates a new <code>Resource</code> instance based on the domain specific
      * REST call with a alternate JSON topic representation.
      */
 
     @POST
     @Path("/resource/create")
     @Produces("application/json")
     @Override
     public Topic createResource(TopicModel topicModel, @HeaderParam("Cookie") ClientState clientState) {
         DeepaMehtaTransaction tx = dms.beginTx();
         Topic resource = null;
         try {
             String value = topicModel.getCompositeValueModel().getString(RESOURCE_CONTENT_URI);
             // skipping: check in htmlContent for <script>-tag
             // enrich new topic about content
             topicModel.getCompositeValueModel().put(RESOURCE_CONTENT_URI, value);
             // enrich new topic about timestamps
             long createdAt = new Date().getTime();
             topicModel.getCompositeValueModel().put(RESOURCE_CREATED_AT_URI, createdAt);
             topicModel.getCompositeValueModel().put(RESOURCE_LAST_MODIFIED_URI, createdAt);
             topicModel.getCompositeValueModel().put(RESOURCE_PUBLISHED_URI, true);
             topicModel.getCompositeValueModel().put(RESOURCE_LOCKED_URI, false);
             topicModel.getCompositeValueModel().putRef(RESOURCE_LICENSE_URI, RESOURCE_LICENSE_UNSPECIFIED_URI);
             topicModel.getCompositeValueModel().putRef(RESOURCE_LICENSE_AREA_URI, RESOURCE_LICENSE_UNKNOWN_URI);
             // topicModel.getCompositeValueModel().putRef(RESOURCE_AUTHOR_NAME_URI, RESOURCE_AUTHOR_ANONYMOUS_URI);
             // create new topic
             resource = dms.createTopic(topicModel, clientState); // clientstate is for workspace-assignment
             tx.success();
             return resource;
         } catch (Exception e) {
             throw new WebApplicationException(new RuntimeException("Something went wrong while creating resource", e));
         } finally {
             tx.finish();
         }
     }
 
     /**
      * Updates an existing <code>Resource</code> instance based on the domain specific
      * REST call with a alternate JSON topic representation.
      */
 
     @POST
     @Path("/resource/update")
     @Produces("application/json")
     @Override
     public Topic updateResource(TopicModel topic, @HeaderParam("Cookie") ClientState clientState) {
         DeepaMehtaTransaction tx = dms.beginTx();
         Topic resource = null;
         try {
             // check htmlContent for <script>-tag
             String value = topic.getCompositeValueModel().getString(RESOURCE_CONTENT_URI);
             // updated last_modified timestamp
             long modifiedAt = new Date().getTime();
             // is locked?
             boolean isLocked = topic.getCompositeValueModel().getBoolean(RESOURCE_LOCKED_URI);
             // update resource topic
             resource = dms.getTopic(topic.getId(), true, clientState);
             // Directives updateDirective = dms.updateTopic(topic, clientState);
             // dms.updateTopic() - most high-level model
             // resource.update(topic, clientState, updateDirective); // id, overriding model
             resource.setCompositeValue(new CompositeValueModel().put(RESOURCE_CONTENT_URI, value),
                     clientState, new Directives()); // why new Directives on an AttachedObject
             resource.setCompositeValue(new CompositeValueModel().put(RESOURCE_LAST_MODIFIED_URI, modifiedAt),
                     clientState, new Directives());
             resource.setCompositeValue(new CompositeValueModel().put(RESOURCE_LOCKED_URI, isLocked),
                     clientState, new Directives());
             tx.success();
             return resource;
         } catch (Exception e) {
             throw new WebApplicationException(new RuntimeException("Something went wrong while updating resource", e));
         } finally {
             tx.finish();
         }
     }
 
     /**
      * Fetches all resources with one given <code>Tag</code>.
      *
      * This method was never used, it's just a note that I should try to implement some sort of paging per user-session.
      */
 
     @GET
     @Path("/fetch/{count}/{offset}")
     @Produces("application/json")
     @Override
     public String getResources(@PathParam("count") long size, @PathParam("offset") long from,
             @HeaderParam("Cookie") ClientState clientState) {
         //
         JSONArray results = new JSONArray();
         try {
             ResultSet<RelatedTopic> all_results = dms.getTopics(RESOURCE_URI, true, 0, clientState);
             log.info("> fetching " +all_results.getSize()+ " resources.. for getting " + from + " to " + (from + size) );
             // build up sortable collection of all result-items (warning: in-memory copy of _all_ published soundposter)
             ArrayList<RelatedTopic> in_memory = getResultSetSortedByCreationTime(all_results, clientState);
             // throw error if page is unexpected high or NaN
             int count = 0;
             for (RelatedTopic item : in_memory) {
                 // start of preparing page results
                 if (count >= from) {
                     //
                     results.put(item.toJSON());
                     if (results.length() == size) break;
                 }
                 count++;
                 // finished preparing page results
             }
             return results.toString();
         } catch (Exception e) {
             throw new WebApplicationException(new RuntimeException("something went wrong", e));
         }
     }
 
     @GET
     @Path("/fetch/contributions/{userId}")
     @Produces("application/json")
     public ResultSet<RelatedTopic> getContributedResources(@PathParam("userId") long userId,
             @HeaderParam("Cookie") ClientState clientState) {
         //
         try {
             Topic user  = dms.getTopic(userId, true, clientState);
             ResultSet<RelatedTopic> all_results = fetchAllContributionsByUser(user);
             log.info("fetching " +all_results.getSize()+ " contributions by user " + user.getSimpleValue());
             return all_results;
         } catch (Exception e) {
             throw new WebApplicationException(new RuntimeException("something went wrong", e));
         }
     }
 
     private Topic fetchCreator(Topic resource) {
         //
         return resource.getRelatedTopic(CREATOR_EDGE_URI, PARENT_TYPE_URI,
                 CHILD_TYPE_URI, ACCOUNT_TYPE_URI, true, false, null);
     }
 
     private ResultSet<RelatedTopic> fetchAllContributionsByUser(Topic user) {
         //
         ResultSet<RelatedTopic> all_resources = null;
         all_resources = user.getRelatedTopics(CREATOR_EDGE_URI, CHILD_TYPE_URI,
                 PARENT_TYPE_URI, RESOURCE_URI, true, false, 0, null);
         all_resources.addAll(user.getRelatedTopics(CONTRIBUTOR_EDGE_URI, CHILD_TYPE_URI,
                 PARENT_TYPE_URI, RESOURCE_URI, true, false, 0, null));
         return all_resources;
     }
 
     @GET
     @Path("/")
     @Produces("text/html")
     public Viewable getFrontView() {
         return view("index");
     }
 
     @GET
     @Path("/info")
     @Produces("text/html")
     public Viewable getInfoView() {
         return view("info");
     }
 
     @GET
     @Path("/tagged/{tags}")
     @Produces("text/html")
     public Viewable getFilteredeTimelineView(@PathParam("tags") String tagFilter,
         @HeaderParam("Cookie") ClientState clientState) {
         return view("index");
     }
 
     @GET
     @Path("/user/{userId}")
     @Produces("text/html")
     public Viewable getPersonalTimelineView(@PathParam("userId") long userId,
         @HeaderParam("Cookie") ClientState clientState) {
         return view("index");
     }
 
     @GET
     @Path("/{id}")
     @Produces("text/html")
     public Viewable getDetailView(@PathParam("id") long resourceId, @HeaderParam("Cookie") ClientState clientState) {
         Topic resource = dms.getTopic(resourceId, true, clientState);
        String name = "" + resource.getId();
        context.setVariable("resourceName", name);
         // boolean isLocked = resource.getModel().getCompositeValueModel().getBoolean(RESOURCE_LOCKED_URI);
         // context.setVariable("isLocked", isLocked);
         context.setVariable("resourceId", resource.getId());
         return view("resource");
     }
 
     private ArrayList<RelatedTopic> getResultSetSortedByCreationTime (ResultSet<RelatedTopic> all, ClientState clientState) {
         // build up sortable collection of all result-items
         ArrayList<RelatedTopic> in_memory = new ArrayList<RelatedTopic>();
         for (RelatedTopic obj : all) {
             in_memory.add(obj);
         }
         // sort all result-items
         Collections.sort(in_memory, new Comparator<RelatedTopic>() {
             public int compare(RelatedTopic t1, RelatedTopic t2) {
                 long one = t1.getCompositeValue().getLong(RESOURCE_CREATED_AT_URI, 0);
                 long two = t2.getCompositeValue().getLong(RESOURCE_CREATED_AT_URI, 0);
                 if ( one < two ) return 1;
                 if ( one > two ) return -1;
                 return 0;
             }
         });
         return in_memory;
     }
 
 }
