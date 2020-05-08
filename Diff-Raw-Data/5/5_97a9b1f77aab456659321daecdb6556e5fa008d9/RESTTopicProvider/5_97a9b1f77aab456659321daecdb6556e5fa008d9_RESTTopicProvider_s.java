 package org.jboss.pressgang.ccms.provider;
 
 import javax.ws.rs.core.PathSegment;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import org.jboss.pressgang.ccms.rest.RESTManager;
 import org.jboss.pressgang.ccms.rest.v1.collections.RESTTagCollectionV1;
 import org.jboss.pressgang.ccms.rest.v1.collections.RESTTopicCollectionV1;
 import org.jboss.pressgang.ccms.rest.v1.collections.RESTTopicSourceUrlCollectionV1;
 import org.jboss.pressgang.ccms.rest.v1.collections.RESTTranslatedTopicCollectionV1;
 import org.jboss.pressgang.ccms.rest.v1.collections.join.RESTAssignedPropertyTagCollectionV1;
 import org.jboss.pressgang.ccms.rest.v1.constants.RESTv1Constants;
 import org.jboss.pressgang.ccms.rest.v1.entities.RESTTagV1;
 import org.jboss.pressgang.ccms.rest.v1.entities.RESTTopicSourceUrlV1;
 import org.jboss.pressgang.ccms.rest.v1.entities.RESTTopicV1;
 import org.jboss.pressgang.ccms.rest.v1.entities.RESTTranslatedTopicV1;
 import org.jboss.pressgang.ccms.rest.v1.entities.base.RESTBaseTopicV1;
 import org.jboss.pressgang.ccms.rest.v1.entities.join.RESTAssignedPropertyTagV1;
 import org.jboss.pressgang.ccms.rest.v1.query.RESTTopicQueryBuilderV1;
 import org.jboss.pressgang.ccms.utils.common.CollectionUtilities;
 import org.jboss.pressgang.ccms.wrapper.LogMessageWrapper;
 import org.jboss.pressgang.ccms.wrapper.PropertyTagInTopicWrapper;
 import org.jboss.pressgang.ccms.wrapper.RESTTopicV1Wrapper;
 import org.jboss.pressgang.ccms.wrapper.RESTWrapperFactory;
 import org.jboss.pressgang.ccms.wrapper.TagWrapper;
 import org.jboss.pressgang.ccms.wrapper.TopicSourceURLWrapper;
 import org.jboss.pressgang.ccms.wrapper.TopicWrapper;
 import org.jboss.pressgang.ccms.wrapper.TranslatedTopicWrapper;
 import org.jboss.pressgang.ccms.wrapper.collection.CollectionWrapper;
 import org.jboss.pressgang.ccms.wrapper.collection.RESTTopicCollectionV1Wrapper;
 import org.jboss.pressgang.ccms.wrapper.collection.UpdateableCollectionWrapper;
 import org.jboss.resteasy.specimpl.PathSegmentImpl;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 public class RESTTopicProvider extends RESTDataProvider implements TopicProvider {
     private static final Logger LOG = LoggerFactory.getLogger(RESTTopicProvider.class);
 
     protected RESTTopicProvider(final RESTManager restManager, final RESTWrapperFactory wrapperFactory) {
         super(restManager, wrapperFactory);
     }
 
     protected RESTTopicV1 loadTopic(int id, Integer revision, String expandString) {
         if (revision == null) {
             return getRESTClient().getJSONTopic(id, expandString);
         } else {
             return getRESTClient().getJSONTopicRevision(id, revision, expandString);
         }
     }
 
     public RESTTopicV1 getRESTTopic(int id) {
         return getRESTTopic(id, null);
     }
 
     @Override
     public TopicWrapper getTopic(int id) {
         return getTopic(id, null);
     }
 
     public RESTTopicV1 getRESTTopic(int id, Integer revision) {
         try {
             final RESTTopicV1 topic;
             if (getRESTEntityCache().containsKeyValue(RESTTopicV1.class, id, revision)) {
                 topic = getRESTEntityCache().get(RESTTopicV1.class, id, revision);
             } else {
                 final String expansionString = getExpansionString(Arrays.asList(RESTTopicV1.TAGS_NAME, RESTTopicV1.PROPERTIES_NAME));
                 topic = loadTopic(id, revision, expansionString);
                 getRESTEntityCache().add(topic, revision);
             }
             return topic;
         } catch (Exception e) {
             LOG.debug("Failed to retrieve Topic " + id + (revision == null ? "" : (", Revision " + revision)), e);
             throw handleException(e);
         }
     }
 
     @Override
     public TopicWrapper getTopic(int id, Integer revision) {
         return getWrapperFactory().create(getRESTTopic(id, revision), revision != null);
     }
 
     public RESTTagCollectionV1 getRESTTopicTags(int id, final Integer revision) {
         try {
             RESTTopicV1 topic = null;
             // Check the cache first
             if (getRESTEntityCache().containsKeyValue(RESTTopicV1.class, id, revision)) {
                 topic = getRESTEntityCache().get(RESTTopicV1.class, id, revision);
 
                 if (topic.getTags() != null) {
                     return topic.getTags();
                 }
             }
 
             // We need to expand the tags in the topic
             final String expandString = getExpansionString(RESTTopicV1.TAGS_NAME);
 
             // Load the topic from the REST Interface
             final RESTTopicV1 tempTopic = loadTopic(id, revision, expandString);
 
             if (topic == null) {
                 topic = tempTopic;
                 getRESTEntityCache().add(topic, revision);
             } else {
                 topic.setTags(tempTopic.getTags());
             }
             getRESTEntityCache().add(topic.getTags(), revision != null);
 
             return topic.getTags();
         } catch (Exception e) {
             LOG.debug("Failed to retrieve the Tags for Topic " + id + (revision == null ? "" : (", Revision " + revision)), e);
             throw handleException(e);
         }
     }
 
     @Override
     public CollectionWrapper<TagWrapper> getTopicTags(int id, final Integer revision) {
         return getWrapperFactory().createCollection(getRESTTopicTags(id, revision), RESTTagV1.class, revision != null);
     }
 
     public RESTTopicCollectionV1 getRESTTopics(final List<Integer> ids) {
         if (ids.isEmpty()) return null;
 
         try {
             final RESTTopicCollectionV1 topics = new RESTTopicCollectionV1();
             final Set<Integer> queryIds = new HashSet<Integer>();
 
             for (final Integer id : ids) {
                 if (!getRESTEntityCache().containsKeyValue(RESTTopicV1.class, id)) {
                     queryIds.add(id);
                 } else {
                     topics.addItem(getRESTEntityCache().get(RESTTopicV1.class, id));
                 }
             }
 
             // Get the missing topics from the REST interface
             if (!queryIds.isEmpty()) {
                 final RESTTopicQueryBuilderV1 queryBuilder = new RESTTopicQueryBuilderV1();
                 queryBuilder.setTopicIds(new ArrayList<Integer>(queryIds));
 
                 // We need to expand the topic collection
                 final String expandString = getExpansionString(RESTv1Constants.TOPICS_EXPANSION_NAME,
                         Arrays.asList(RESTTopicV1.TAGS_NAME, RESTTopicV1.PROPERTIES_NAME));
 
                 // Load the topics from the REST Interface
                 final RESTTopicCollectionV1 downloadedTopics = getRESTClient().getJSONTopicsWithQuery(queryBuilder.buildQueryPath(),
                         expandString);
                 getRESTEntityCache().add(downloadedTopics);
 
                 // Transfer the downloaded data to the current topic list
                 if (downloadedTopics != null && downloadedTopics.getItems() != null) {
                     final List<RESTTopicV1> items = downloadedTopics.returnItems();
                     for (final RESTTopicV1 item : items) {
                         topics.addItem(item);
                     }
                 }
             }
 
             return topics;
         } catch (Exception e) {
             LOG.debug("Failed to retrieve all Topics for the Ids: " + CollectionUtilities.toSeperatedString(ids), e);
             throw handleException(e);
         }
     }
 
     @Override
     public CollectionWrapper<TopicWrapper> getTopics(final List<Integer> ids) {
         if (ids.isEmpty()) return null;
 
         return getWrapperFactory().createCollection(getRESTTopics(ids), RESTTopicV1.class, false);
     }
 
     public RESTTopicCollectionV1 getRESTTopicsWithQuery(final String query) {
         if (query == null || query.isEmpty()) return null;
 
         try {
             // We need to expand the all the topics in the collection
             final String expandString = getExpansionString(RESTv1Constants.TOPICS_EXPANSION_NAME,
                     Arrays.asList(RESTTopicV1.TAGS_NAME, RESTTopicV1.PROPERTIES_NAME));
 
             final RESTTopicCollectionV1 topics = getRESTClient().getJSONTopicsWithQuery(new PathSegmentImpl(query, false), expandString);
             getRESTEntityCache().add(topics);
 
             return topics;
         } catch (Exception e) {
             LOG.debug("Failed to retrieve Topics with Query: " + query, e);
             throw handleException(e);
         }
     }
 
     @Override
     public CollectionWrapper<TopicWrapper> getTopicsWithQuery(final String query) {
         if (query == null || query.isEmpty()) return null;
 
         return getWrapperFactory().createCollection(getRESTTopicsWithQuery(query), RESTTopicV1.class, false);
     }
 
     public RESTTranslatedTopicCollectionV1 getRESTTopicTranslations(int id, final Integer revision) {
         try {
             RESTTopicV1 topic = null;
             // Check the cache first
             if (getRESTEntityCache().containsKeyValue(RESTTopicV1.class, id, revision)) {
                 topic = getRESTEntityCache().get(RESTTopicV1.class, id, revision);
 
                 if (topic.getTranslatedTopics_OTM() != null) {
                     return topic.getTranslatedTopics_OTM();
                 }
             }
 
             // We need to expand the the translated topics in the topic
             final String expandString = getExpansionString(RESTTopicV1.TRANSLATEDTOPICS_NAME, Arrays.asList(RESTTranslatedTopicV1.TOPIC_NAME,
                     RESTTranslatedTopicV1.TAGS_NAME, RESTTranslatedTopicV1.PROPERTIES_NAME, RESTTranslatedTopicV1.TRANSLATED_CSNODE_NAME));
 
             // Load the topic from the REST API
             final RESTTopicV1 tempTopic = loadTopic(id, revision, expandString);
 
             if (topic == null) {
                 topic = tempTopic;
                 getRESTEntityCache().add(topic, revision);
             } else {
                 topic.setTranslatedTopics_OTM(tempTopic.getTranslatedTopics_OTM());
             }
            if (topic.getTranslatedTopics_OTM() != null) {
                for (final RESTTranslatedTopicV1 translatedTopic : topic.getTranslatedTopics_OTM().returnItems()) {
                    translatedTopic.setTopic(topic);
                }
            }
 
             return topic.getTranslatedTopics_OTM();
         } catch (Exception e) {
             LOG.debug("Failed to retrieve the Translations for Topic " + id + (revision == null ? "" : (", Revision " + revision)), e);
             throw handleException(e);
         }
     }
 
     @Override
     public CollectionWrapper<TranslatedTopicWrapper> getTopicTranslations(int id, final Integer revision) {
         return getWrapperFactory().createCollection(getRESTTopicTranslations(id, revision), RESTTranslatedTopicV1.class, revision != null);
     }
 
     public RESTTopicCollectionV1 getRESTTopicRevisions(int id, final Integer revision) {
         try {
             RESTTopicV1 topic = null;
             // Check the cache first
             if (getRESTEntityCache().containsKeyValue(RESTTopicV1.class, id, revision)) {
                 topic = getRESTEntityCache().get(RESTTopicV1.class, id, revision);
 
                 if (topic.getRevisions() != null) {
                     return topic.getRevisions();
                 }
             }
 
             // We need to expand the revisions in the topic
             final String expandString = getExpansionString(RESTTopicV1.REVISIONS_NAME);
 
             // Load the topic from the REST API
             final RESTTopicV1 tempTopic = loadTopic(id, revision, expandString);
 
             if (topic == null) {
                 topic = tempTopic;
                 getRESTEntityCache().add(topic, revision);
             } else {
                 topic.setRevisions(tempTopic.getRevisions());
             }
 
             return topic.getRevisions();
         } catch (Exception e) {
             LOG.debug("Failed to retrieve the Revisions for Topic " + id + (revision == null ? "" : (", Revision " + revision)), e);
             throw handleException(e);
         }
     }
 
     @Override
     public CollectionWrapper<TopicWrapper> getTopicRevisions(int id, final Integer revision) {
         return getWrapperFactory().createCollection(getRESTTopicRevisions(id, revision), RESTTopicV1.class, true);
     }
 
     public RESTAssignedPropertyTagCollectionV1 getRESTTopicProperties(int id, final Integer revision) {
         try {
             RESTTopicV1 topic = null;
             // Check the cache first
             if (getRESTEntityCache().containsKeyValue(RESTTopicV1.class, id, revision)) {
                 topic = getRESTEntityCache().get(RESTTopicV1.class, id, revision);
 
                 if (topic.getProperties() != null) {
                     return topic.getProperties();
                 }
             }
 
             // We need to expand the all the properties in the topic
             final String expandString = getExpansionString(RESTTopicV1.PROPERTIES_NAME);
 
             // Load the topic from the REST Interface
             final RESTTopicV1 tempTopic = loadTopic(id, revision, expandString);
 
             if (topic == null) {
                 topic = tempTopic;
                 getRESTEntityCache().add(topic, revision);
             } else {
                 topic.setProperties(tempTopic.getProperties());
             }
 
             return topic.getProperties();
         } catch (Exception e) {
             LOG.debug("Failed to retrieve the Properties for Topic " + id + (revision == null ? "" : (", Revision " + revision)), e);
             throw handleException(e);
         }
     }
 
     @Override
     public UpdateableCollectionWrapper<PropertyTagInTopicWrapper> getTopicProperties(int id, final Integer revision) {
         throw new UnsupportedOperationException("A parent is needed to get Topic Properties using V1 of the REST Interface.");
     }
 
     public UpdateableCollectionWrapper<PropertyTagInTopicWrapper> getTopicProperties(int id, final Integer revision,
             final RESTBaseTopicV1<?, ?, ?> parent) {
         final CollectionWrapper<PropertyTagInTopicWrapper> collection = getWrapperFactory().createCollection(
                 getRESTTopicProperties(id, revision), RESTAssignedPropertyTagV1.class, revision != null, parent,
                 PropertyTagInTopicWrapper.class);
         return (UpdateableCollectionWrapper<PropertyTagInTopicWrapper>) collection;
     }
 
     public RESTTopicCollectionV1 getRESTTopicOutgoingRelationships(int id, final Integer revision) {
         try {
             RESTTopicV1 topic = null;
             // Check the cache first
             if (getRESTEntityCache().containsKeyValue(RESTTopicV1.class, id, revision)) {
                 topic = getRESTEntityCache().get(RESTTopicV1.class, id, revision);
 
                 if (topic.getOutgoingRelationships() != null) {
                     return topic.getOutgoingRelationships();
                 }
             }
 
             // We need to expand the outgoing topic relationships in the topic
             final String expandString = getExpansionString(RESTTopicV1.OUTGOING_NAME);
 
             // Load the topic from the REST Interface
             final RESTTopicV1 tempTopic = loadTopic(id, revision, expandString);
 
             if (topic == null) {
                 topic = tempTopic;
                 getRESTEntityCache().add(topic, revision);
             } else {
                 topic.setOutgoingRelationships(tempTopic.getOutgoingRelationships());
             }
 
             return topic.getOutgoingRelationships();
         } catch (Exception e) {
             LOG.debug("Failed to retrieve the Outgoing Relationships for Topic " + id + (revision == null ? "" : (", " +
                     "Revision " + revision)), e);
             throw handleException(e);
         }
     }
 
     @Override
     public CollectionWrapper<TopicWrapper> getTopicOutgoingRelationships(int id, final Integer revision) {
         return getWrapperFactory().createCollection(getRESTTopicOutgoingRelationships(id, revision), RESTTopicV1.class, revision != null);
     }
 
     public RESTTopicCollectionV1 getRESTTopicIncomingRelationships(int id, final Integer revision) {
         try {
             RESTTopicV1 topic = null;
             // Check the cache first
             if (getRESTEntityCache().containsKeyValue(RESTTopicV1.class, id, revision)) {
                 topic = getRESTEntityCache().get(RESTTopicV1.class, id, revision);
 
                 if (topic.getIncomingRelationships() != null) {
                     return topic.getIncomingRelationships();
                 }
             }
 
             // We need to expand the incoming topic relationships in the topic
             final String expandString = getExpansionString(RESTTopicV1.INCOMING_NAME);
 
             // Load the topic from the REST Interface
             final RESTTopicV1 tempTopic = loadTopic(id, revision, expandString);
 
             if (topic == null) {
                 topic = tempTopic;
                 getRESTEntityCache().add(topic, revision);
             } else {
                 topic.setIncomingRelationships(tempTopic.getIncomingRelationships());
             }
 
             return topic.getIncomingRelationships();
         } catch (Exception e) {
             LOG.debug("Failed to retrieve the Incoming Relationships for Topic " + id + (revision == null ? "" : (", " +
                     "Revision " + revision)), e);
             throw handleException(e);
         }
     }
 
     @Override
     public CollectionWrapper<TopicWrapper> getTopicIncomingRelationships(int id, final Integer revision) {
         return getWrapperFactory().createCollection(getRESTTopicIncomingRelationships(id, revision), RESTTopicV1.class, revision != null);
     }
 
     @Override
     public CollectionWrapper<TopicSourceURLWrapper> getTopicSourceUrls(int id, final Integer revision) {
         throw new UnsupportedOperationException("A parent is needed to get Topic Source URLs using V1 of the REST Interface.");
     }
 
     public RESTTopicSourceUrlCollectionV1 getRESTTopicSourceUrls(int id, final Integer revision) {
         try {
             RESTTopicV1 topic = null;
             // Check the cache first
             if (getRESTEntityCache().containsKeyValue(RESTTopicV1.class, id, revision)) {
                 topic = getRESTEntityCache().get(RESTTopicV1.class, id, revision);
 
                 if (topic.getSourceUrls_OTM() != null) {
                     return topic.getSourceUrls_OTM();
                 }
             }
 
             // We need to expand the source urls in the topic
             final String expandString = getExpansionString(RESTTopicV1.SOURCE_URLS_NAME);
 
             // Load the topic from the REST Interface
             final RESTTopicV1 tempTopic = loadTopic(id, revision, expandString);
 
             if (topic == null) {
                 topic = tempTopic;
                 getRESTEntityCache().add(topic, revision);
             } else {
                 topic.setSourceUrls_OTM(tempTopic.getSourceUrls_OTM());
             }
 
             return topic.getSourceUrls_OTM();
         } catch (Exception e) {
             LOG.debug("Failed to retrieve the Source URLs for Topic " + id + (revision == null ? "" : (", Revision " + revision)), e);
             throw handleException(e);
         }
     }
 
     public CollectionWrapper<TopicSourceURLWrapper> getTopicSourceUrls(int id, final Integer revision,
             final RESTBaseTopicV1<?, ?, ?> parent) {
         return getWrapperFactory().createCollection(getRESTTopicSourceUrls(id, revision), RESTTopicSourceUrlV1.class, revision != null,
                 parent);
     }
 
     @Override
     public TopicWrapper createTopic(final TopicWrapper topicEntity) {
         return createTopic(topicEntity, null);
     }
 
     @Override
     public TopicWrapper createTopic(TopicWrapper topicEntity, LogMessageWrapper logMessage) {
         try {
             final RESTTopicV1 topic = ((RESTTopicV1Wrapper) topicEntity).unwrap();
 
             // Clean the entity to remove anything that doesn't need to be sent to the server
             cleanEntityForSave(topic);
 
             final String expansionString = getExpansionString(Arrays.asList(RESTTopicV1.TAGS_NAME, RESTTopicV1.PROPERTIES_NAME));
 
             final RESTTopicV1 updatedTopic;
             if (logMessage != null) {
                 updatedTopic = getRESTClient().createJSONTopic(expansionString, topic, logMessage.getMessage(), logMessage.getFlags(),
                         logMessage.getUser());
             } else {
                 updatedTopic = getRESTClient().createJSONTopic(expansionString, topic);
             }
             if (updatedTopic != null) {
                 getRESTEntityCache().add(updatedTopic);
                 return getWrapperFactory().create(updatedTopic, false);
             } else {
                 return null;
             }
         } catch (Exception e) {
             LOG.debug("Failed to create Topic", e);
             throw handleException(e);
         }
     }
 
     @Override
     public TopicWrapper updateTopic(TopicWrapper topicEntity) {
         return updateTopic(topicEntity, null);
     }
 
     @Override
     public TopicWrapper updateTopic(TopicWrapper topicEntity, LogMessageWrapper logMessage) {
         try {
             final RESTTopicV1 topic = ((RESTTopicV1Wrapper) topicEntity).unwrap();
 
             // Clean the entity to remove anything that doesn't need to be sent to the server
             cleanEntityForSave(topic);
 
             final String expansionString = getExpansionString(Arrays.asList(RESTTopicV1.TAGS_NAME, RESTTopicV1.PROPERTIES_NAME));
 
             final RESTTopicV1 updatedTopic;
             if (logMessage != null) {
                 updatedTopic = getRESTClient().updateJSONTopic(expansionString, topic, logMessage.getMessage(), logMessage.getFlags(),
                         logMessage.getUser());
             } else {
                 updatedTopic = getRESTClient().updateJSONTopic(expansionString, topic);
             }
             if (updatedTopic != null) {
                 getRESTEntityCache().expire(RESTTopicV1.class, updatedTopic.getId());
                 getRESTEntityCache().add(updatedTopic);
                 return getWrapperFactory().create(updatedTopic, false);
             } else {
                 return null;
             }
         } catch (Exception e) {
             LOG.debug("Failed to update Topic " + topicEntity.getId(), e);
             throw handleException(e);
         }
     }
 
     @Override
     public boolean deleteTopic(Integer id) {
         return deleteTopic(id, null);
     }
 
     @Override
     public boolean deleteTopic(Integer id, LogMessageWrapper logMessage) {
         try {
             if (logMessage != null) {
                 return getRESTClient().deleteJSONTopic(id, logMessage.getMessage(), logMessage.getFlags(), logMessage.getUser(),
                         "") != null;
             } else {
                 return getRESTClient().deleteJSONTopic(id, "") != null;
             }
         } catch (Exception e) {
             LOG.debug("Failed to delete Topic " + id, e);
             throw handleException(e);
         }
     }
 
     @Override
     public CollectionWrapper<TopicWrapper> createTopics(CollectionWrapper<TopicWrapper> topics) {
         return updateTopics(topics, null);
     }
 
     @Override
     public CollectionWrapper<TopicWrapper> createTopics(CollectionWrapper<TopicWrapper> topics, LogMessageWrapper logMessage) {
         try {
             final RESTTopicCollectionV1 unwrappedTopics = ((RESTTopicCollectionV1Wrapper) topics).unwrap();
 
             // Clean the collection to remove anything that doesn't need to be sent to the server
             cleanCollectionForSave(unwrappedTopics, false);
 
             final String expandString = getExpansionString(RESTv1Constants.TOPICS_EXPANSION_NAME,
                     Arrays.asList(RESTTopicV1.TAGS_NAME, RESTTopicV1.PROPERTIES_NAME));
             final RESTTopicCollectionV1 createdTopics;
             if (logMessage != null) {
                 createdTopics = getRESTClient().createJSONTopics(expandString, unwrappedTopics, logMessage.getMessage(),
                         logMessage.getFlags(), logMessage.getUser());
             } else {
                 createdTopics = getRESTClient().createJSONTopics(expandString, unwrappedTopics);
             }
             if (createdTopics != null) {
                 getRESTEntityCache().add(createdTopics, false);
                 return getWrapperFactory().createCollection(createdTopics, RESTTopicV1.class, false);
             } else {
                 return null;
             }
         } catch (Exception e) {
             LOG.debug("Failed to create Topics", e);
             throw handleException(e);
         }
     }
 
     @Override
     public CollectionWrapper<TopicWrapper> updateTopics(CollectionWrapper<TopicWrapper> topics) {
         return updateTopics(topics, null);
     }
 
     @Override
     public CollectionWrapper<TopicWrapper> updateTopics(CollectionWrapper<TopicWrapper> topics, LogMessageWrapper logMessage) {
         try {
             final RESTTopicCollectionV1 unwrappedTopics = ((RESTTopicCollectionV1Wrapper) topics).unwrap();
 
             // Clean the collection to remove anything that doesn't need to be sent to the server
             cleanCollectionForSave(unwrappedTopics, false);
 
             final String expandString = getExpansionString(RESTv1Constants.TOPICS_EXPANSION_NAME,
                     Arrays.asList(RESTTopicV1.TAGS_NAME, RESTTopicV1.PROPERTIES_NAME));
             final RESTTopicCollectionV1 updatedTopics;
             if (logMessage != null) {
                 updatedTopics = getRESTClient().updateJSONTopics(expandString, unwrappedTopics, logMessage.getMessage(),
                         logMessage.getFlags(), logMessage.getUser());
             } else {
                 updatedTopics = getRESTClient().updateJSONTopics(expandString, unwrappedTopics);
             }
             if (updatedTopics != null) {
                 // Expire the old cached data
                 for (final RESTTopicV1 topic : updatedTopics.returnItems()) {
                     getRESTEntityCache().expire(RESTTopicV1.class, topic.getId());
                 }
                 // Add the new data to the cache
                 getRESTEntityCache().add(updatedTopics, false);
                 return getWrapperFactory().createCollection(updatedTopics, RESTTopicV1.class, false);
             } else {
                 return null;
             }
         } catch (Exception e) {
             LOG.debug("", e);
             throw handleException(e);
         }
     }
 
     @Override
     public boolean deleteTopics(final List<Integer> topicIds) {
         return deleteTopics(topicIds, null);
     }
 
     @Override
     public boolean deleteTopics(List<Integer> topicIds, LogMessageWrapper logMessage) {
         try {
             final String pathString = "ids;" + CollectionUtilities.toSeperatedString(topicIds, ";");
             final PathSegment path = new PathSegmentImpl(pathString, false);
             if (logMessage != null) {
                 return getRESTClient().deleteJSONTopics(path, logMessage.getMessage(), logMessage.getFlags(), logMessage.getUser(),
                         "") != null;
             } else {
                 return getRESTClient().deleteJSONTopics(path, "") != null;
             }
         } catch (Exception e) {
             LOG.debug("Failed to delete Topics " + CollectionUtilities.toSeperatedString(topicIds, ", "), e);
             throw handleException(e);
         }
     }
 
     @Override
     public TopicWrapper newTopic() {
         return getWrapperFactory().create(new RESTTopicV1(), false, true);
     }
 
     @Override
     public CollectionWrapper<TopicWrapper> newTopicCollection() {
         return getWrapperFactory().createCollection(new RESTTopicCollectionV1(), RESTTopicV1.class, false);
     }
 }
