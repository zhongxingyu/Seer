 package com.poemspace.dm4;
 
 import static de.deepamehta.plugins.mail.MailPlugin.*;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.logging.Logger;
 
 import javax.ws.rs.GET;
 import javax.ws.rs.HeaderParam;
 import javax.ws.rs.POST;
 import javax.ws.rs.PUT;
 import javax.ws.rs.Path;
 import javax.ws.rs.PathParam;
 import javax.ws.rs.Produces;
 import javax.ws.rs.WebApplicationException;
 import javax.ws.rs.core.MediaType;
 
 import de.deepamehta.core.Association;
 import de.deepamehta.core.DeepaMehtaObject;
 import de.deepamehta.core.RelatedTopic;
 import de.deepamehta.core.ResultSet;
 import de.deepamehta.core.Topic;
 import de.deepamehta.core.TopicType;
 import de.deepamehta.core.ViewConfiguration;
 import de.deepamehta.core.model.AssociationDefinitionModel;
 import de.deepamehta.core.model.AssociationModel;
 import de.deepamehta.core.model.IndexMode;
 import de.deepamehta.core.model.TopicModel;
 import de.deepamehta.core.model.TopicRoleModel;
 import de.deepamehta.core.model.TopicTypeModel;
 import de.deepamehta.core.osgi.PluginActivator;
 import de.deepamehta.core.service.ClientState;
import de.deepamehta.core.service.Directives;
 import de.deepamehta.core.service.PluginService;
 import de.deepamehta.core.service.accesscontrol.ACLEntry;
 import de.deepamehta.core.service.accesscontrol.AccessControlList;
 import de.deepamehta.core.service.accesscontrol.Operation;
 import de.deepamehta.core.service.accesscontrol.UserRole;
 import de.deepamehta.core.service.annotation.ConsumesService;
 import de.deepamehta.core.storage.spi.DeepaMehtaTransaction;
 import de.deepamehta.plugins.accesscontrol.service.AccessControlService;
 import de.deepamehta.plugins.mail.Mail;
 import de.deepamehta.plugins.mail.RecipientType;
 import de.deepamehta.plugins.mail.StatusReport;
 import de.deepamehta.plugins.mail.service.MailService;
 
 @Path("/poemspace")
 @Produces(MediaType.APPLICATION_JSON)
 public class PoemSpacePlugin extends PluginActivator {
 
     private static final String CAMPAIGN = "dm4.poemspace.campaign";
 
     private static final String COUNT = "dm4.poemspace.campaign.count";
 
     private static final String EXCLUDE = "dm4.poemspace.campaign.excl";
 
     private static final String INCLUDE = "dm4.poemspace.campaign.adds";
 
     private static Logger log = Logger.getLogger(PoemSpacePlugin.class.getName());
 
     private AccessControlService acService;
 
     private CriteriaCache criteria = null;
 
     private MailService mailService;
 
     private boolean isInitialized;
 
     public static final Comparator<Topic> VALUE_COMPARATOR = new Comparator<Topic>() {
         @Override
         public int compare(Topic a, Topic b) {
             return a.getSimpleValue().toString().compareTo(b.getSimpleValue().toString());
         }
     };
 
     @GET
     @Path("/criteria-types")
     public List<Topic> getCriteriaTypes() {
         try {
             return criteria.getTypes();
         } catch (Exception e) {
             throw new WebApplicationException(e);
         }
     }
 
     @POST
     @Path("/criteria-reload")
     public List<Topic> reloadCriteriaCache() {
         criteria = new CriteriaCache(dms);
         return getCriteriaTypes();
     }
 
     @POST
     @Path("/criteria/{name}")
     public Topic createCriteria(@PathParam("name") String name,//
             @HeaderParam("Cookie") ClientState cookie) {
         log.info("create criteria " + name);
         // TODO sanitize name parameter
         String uri = "dm4.poemspace.criteria." + name.trim().toLowerCase();
 
         DeepaMehtaTransaction tx = dms.beginTx();
         try {
             TopicType type = dms.createTopicType(//
                     new TopicTypeModel(uri, name, "dm4.core.text"), cookie);
             type.setIndexModes(new HashSet<IndexMode>(Arrays.asList(IndexMode.FULLTEXT)));
 
             ViewConfiguration viewConfig = type.getViewConfig();
             viewConfig.addSetting("dm4.webclient.view_config",//
                     "dm4.webclient.multi_renderer_uri", "dm4.webclient.checkbox_renderer");
             viewConfig.addSetting("dm4.webclient.view_config",//
                     "dm4.webclient.add_to_create_menu", true);
             viewConfig.addSetting("dm4.webclient.view_config",//
                     "dm4.webclient.is_searchable_unit", true);
 
             // associate criteria type
             dms.createAssociation(new AssociationModel("dm4.core.association",//
                     new TopicRoleModel("dm4.poemspace.criteria.type", "dm4.core.default"),//
                     new TopicRoleModel(type.getId(), "dm4.core.default"), null), cookie);
 
             // create search type aggregates
             for (Topic topic : mailService.getSearchParentTypes()) {
                 TopicType searchType = dms.getTopicType(topic.getUri(), cookie);
                 searchType.addAssocDef(new AssociationDefinitionModel("dm4.core.aggregation_def",//
                         searchType.getUri(), type.getUri(), "dm4.core.one", "dm4.core.many"));
             }
 
             // renew cache
             criteria = new CriteriaCache(dms);
             tx.success();
 
             return type;
         } catch (Exception e) {
             throw new WebApplicationException(e);
         } finally {
             tx.finish();
         }
     }
 
     @POST
     @Path("/campaign/{id}/include/{recipient}")
     public Association include(//
             @PathParam("id") long campaignId,//
             @PathParam("recipient") long recipientId,//
             @HeaderParam("Cookie") ClientState cookie) {
         try {
             log.info("include recipient " + recipientId + " into campaign " + campaignId);
             return createOrUpdateRecipient(INCLUDE, campaignId, recipientId, cookie);
         } catch (Exception e) {
             throw new WebApplicationException(e);
         }
     }
 
     @POST
     @Path("/campaign/{id}/exclude/{recipient}")
     public Association exclude(//
             @PathParam("id") long campaignId,//
             @PathParam("recipient") long recipientId,//
             @HeaderParam("Cookie") ClientState cookie) {
         try {
             log.info("exclude recipient " + recipientId + " from campaign " + campaignId);
             return createOrUpdateRecipient(EXCLUDE, campaignId, recipientId, cookie);
         } catch (Exception e) {
             throw new WebApplicationException(e);
         }
     }
 
     @GET
     @Path("/campaign/{id}/recipients")
     public List<Topic> queryCampaignRecipients(//
             @PathParam("id") long campaignId,//
             @HeaderParam("Cookie") ClientState cookie) {
         log.info("get campaign " + campaignId + " recipients");
         DeepaMehtaTransaction tx = dms.beginTx();
         try {
             Topic campaign = dms.getTopic(campaignId, true, cookie);
 
             // get and sort recipients
             List<Topic> recipients = queryCampaignRecipients(campaign);
             Collections.sort(recipients, VALUE_COMPARATOR);
 
             // update campaign count and return result
            campaign.getCompositeValue().set(COUNT, recipients.size(), cookie, new Directives());
             tx.success();
             return recipients;
         } catch (Exception e) {
             throw new WebApplicationException(new RuntimeException(//
                     "recipients query of campaign " + campaignId + " failed", e));
         } finally {
             tx.finish();
         }
     }
 
     /**
      * Starts and returns a new campaign from a mail.
      * 
      * @param mailId
      * @param cookie
      * @return Campaign associated with the starting mail.
      */
     @PUT
     @Path("/mail/{id}/start")
     public Topic startCampaign(//
             @PathParam("id") long mailId,//
             @HeaderParam("Cookie") ClientState cookie) {
         log.info("start a campaign from mail " + mailId);
         DeepaMehtaTransaction tx = dms.beginTx();
         try {
             Topic campaign = dms.createTopic(new TopicModel(CAMPAIGN), cookie);
             dms.createAssociation(new AssociationModel("dm4.core.association",//
                     new TopicRoleModel(mailId, "dm4.core.default"),//
                     new TopicRoleModel(campaign.getId(), "dm4.core.default"), null), cookie);
             tx.success();
             return campaign;
         } catch (Exception e) {
             throw new WebApplicationException(new RuntimeException(//
                     "start a campaign from mail \"" + mailId + "\" failed", e));
         } finally {
             tx.finish();
         }
     }
 
     /**
      * Sends a campaign mail.
      * 
      * @param mailId
      * @param cookie
      * @return Sent mail topic.
      */
     @PUT
     @Path("/mail/{id}/send")
     public StatusReport sendCampaignMail(//
             @PathParam("id") long mailId,//
             @HeaderParam("Cookie") ClientState cookie) {
         log.info("send campaign mail " + mailId);
         DeepaMehtaTransaction tx = dms.beginTx();
         try {
             Topic mail = dms.getTopic(mailId, false, cookie);
             RelatedTopic campaign = mail.getRelatedTopic("dm4.core.association",//
                     "dm4.core.default", "dm4.core.default", CAMPAIGN, false, false, cookie);
 
             // associate recipients of query result
             for (Topic recipient : queryCampaignRecipients(campaign)) {
                 Topic topic = dms.getTopic(recipient.getId(), true, cookie);
                 if (topic.getCompositeValue().has(EMAIL_ADDRESS)) {
                     for (Topic address : topic.getCompositeValue().getTopics(EMAIL_ADDRESS)) {
                         mailService.associateRecipient(mailId, //
                                 address.getId(), RecipientType.BCC, cookie);
                     }
                 }
             }
 
             tx.success();
             return mailService.send(new Mail(mailId, dms, cookie));
         } catch (Exception e) {
             throw new WebApplicationException(new RuntimeException(//
                     "send campaign mail \"" + mailId + "\" failed", e));
         } finally {
             tx.finish();
         }
     }
 
     /**
      * Initialize.
      */
     @Override
     public void init() {
         isInitialized = true;
         configureIfReady();
     }
 
     @Override
     @ConsumesService({ "de.deepamehta.plugins.accesscontrol.service.AccessControlService",
             "de.deepamehta.plugins.mail.service.MailService" })
     public void serviceArrived(PluginService service) {
         if (service instanceof AccessControlService) {
             acService = (AccessControlService) service;
         } else if (service instanceof MailService) {
             mailService = (MailService) service;
         }
         configureIfReady();
     }
 
     private void configureIfReady() {
         if (isInitialized && acService != null && mailService != null) {
             // TODO add update listener to reload cache (create, update, delete)
             criteria = new CriteriaCache(dms);
             checkACLsOfMigration();
         }
     }
 
     @Override
     public void serviceGone(PluginService service) {
         if (service == acService) {
             acService = null;
         } else if (service == mailService) {
             mailService = null;
         }
     }
 
     private void checkACLsOfMigration() {
         for (String typeUri : new String[] { "dm4.poemspace.project", //
                 "dm4.poemspace.year", //
                 "dm4.poemspace.affiliation", //
                 "dm4.poemspace.press", //
                 "dm4.poemspace.education", //
                 "dm4.poemspace.public", //
                 "dm4.poemspace.art", //
                 "dm4.poemspace.gattung" }) {
             checkACLsOfTopics(typeUri);
         }
     }
 
     // private void checkACLsOfAssociations(String typeUri) {
     // for (RelatedAssociation topic : dms.getAssociations(typeUri)) {
     // checkACLsOfObject(topic);
     // }
     // }
 
     private void checkACLsOfTopics(String typeUri) {
         for (RelatedTopic topic : dms.getTopics(typeUri, false, 0, null)) {
             checkACLsOfObject(topic);
         }
     }
 
     private void checkACLsOfObject(DeepaMehtaObject instance) {
         if (acService.getCreator(instance.getId()) == null) {
             log.info("initial ACL update " + instance.getId() + ": " + instance.getSimpleValue());
             Topic admin = acService.getUsername("admin");
             String adminName = admin.getSimpleValue().toString();
             acService.setCreator(instance.getId(), adminName);
             acService.setOwner(instance.getId(), adminName);
             acService.createACL(instance.getId(), new AccessControlList( //
                     new ACLEntry(Operation.WRITE, UserRole.OWNER)));
         }
     }
 
     private List<Topic> queryCampaignRecipients(Topic campaign) {
         List<Topic> recipients = new ArrayList<Topic>();
         Set<String> searchTypeUris = getSearchTypeUris();
         Map<String, Set<RelatedTopic>> criterionMap = getCriterionMap(campaign);
 
         // get and add the first recipient list
         Iterator<String> criteriaIterator = criterionMap.keySet().iterator();
         if (criteriaIterator.hasNext()) {
             String uri = criteriaIterator.next();
             Set<RelatedTopic> topics = criterionMap.get(uri);
             Set<Topic> and = getCriterionRecipients(topics, searchTypeUris);
             recipients.addAll(and);
             if (recipients.isEmpty() == false) { // merge each other list
                 while (criteriaIterator.hasNext()) {
                     uri = criteriaIterator.next();
                     topics = criterionMap.get(uri);
                     and = getCriterionRecipients(topics, searchTypeUris);
                     // TODO use iterator instead of cloned list
                     // TODO use map by ID to simplify contain check
                     for (Topic topic : new ArrayList<Topic>(recipients)) {
                         if (and.contains(topic) == false) {
                             recipients.remove(topic);
                         }
                         if (recipients.size() == 0) {
                             break;
                         }
                     }
                 }
             }
         }
 
         // get and add includes
         Iterator<RelatedTopic> includes = campaign.getRelatedTopics(INCLUDE, 0, null).iterator();
         while (includes.hasNext()) {
             RelatedTopic include = includes.next();
             if (recipients.contains(include) == false) {
                 recipients.add(include);
             }
         }
 
         // get and remove excludes
         Iterator<RelatedTopic> excludes = campaign.getRelatedTopics(EXCLUDE, 0, null).iterator();
         while (excludes.hasNext()) {
             RelatedTopic exclude = excludes.next();
             if (recipients.contains(exclude)) {
                 recipients.remove(exclude);
             }
         }
         return recipients;
     }
 
     private Set<String> getSearchTypeUris() {
         Set<String> uris = new HashSet<String>();
         for (Topic topic : mailService.getSearchParentTypes()) {
             uris.add(topic.getUri());
         }
         return uris;
     }
 
     /**
      * Returns parent aggregates of each criterion.
      * 
      * @param criterionList
      *            criterion topics
      * @param searchTypeUris
      *            topic type URIs of possible recipients
      * @return
      */
     private Set<Topic> getCriterionRecipients(Set<RelatedTopic> criterionList,//
             Set<String> searchTypeUris) {
         Set<Topic> recipients = new HashSet<Topic>();
         for (Topic criterion : criterionList) {
             for (RelatedTopic topic : dms.getTopic(criterion.getId(), false, null)//
                     .getRelatedTopics("dm4.core.aggregation", "dm4.core.child", "dm4.core.parent", //
                             null, false, false, 0, null)) {
                 if (searchTypeUris.contains(topic.getTypeUri())) {
                     recipients.add(topic);
                 }
             }
         }
         return recipients;
     }
 
     /**
      * Returns all criteria aggregations of a topic.
      * 
      * @param topic
      * @return criterion map of all aggregated criteria sub type instances
      */
     private Map<String, Set<RelatedTopic>> getCriterionMap(Topic topic) {
         Map<String, Set<RelatedTopic>> criterionMap = new HashMap<String, Set<RelatedTopic>>();
         for (String typeUri : criteria.getTypeUris()) {
             ResultSet<RelatedTopic> relatedTopics = topic.getRelatedTopics("dm4.core.aggregation",//
                     "dm4.core.parent", "dm4.core.child", typeUri, false, false, 0, null);
             if (relatedTopics.getSize() > 0) {
                 criterionMap.put(typeUri, relatedTopics.getItems());
             }
         }
         return criterionMap;
     }
 
     private Association createOrUpdateRecipient(String typeUri, long campaignId, long recipientId,
             ClientState clientState) {
         log.fine("create recipient " + typeUri + " association");
         Set<Association> associations = dms.getAssociations(campaignId, recipientId);
         if (associations.size() > 1) {
             throw new IllegalStateException("only one association is supported");
         }
         DeepaMehtaTransaction tx = dms.beginTx();
         try {
             for (Association association : associations) {
                 log.fine("update recipient " + typeUri + " association");
                 association.setTypeUri(typeUri);
                 return association; // only one association can be used
             }
             Association association = dms.createAssociation(new AssociationModel(typeUri,//
                     new TopicRoleModel(campaignId, "dm4.core.default"),//
                     new TopicRoleModel(recipientId, "dm4.core.default"), null), clientState);
             tx.success();
             return association;
         } finally {
             tx.finish();
         }
     }
 
 }
