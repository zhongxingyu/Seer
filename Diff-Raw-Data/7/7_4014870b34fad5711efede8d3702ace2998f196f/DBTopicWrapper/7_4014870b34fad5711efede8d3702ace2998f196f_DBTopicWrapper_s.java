 package org.jboss.pressgang.ccms.wrapper;
 
 import java.text.SimpleDateFormat;
 import java.util.Collection;
 import java.util.Date;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import org.jboss.pressgang.ccms.model.Tag;
 import org.jboss.pressgang.ccms.model.Topic;
 import org.jboss.pressgang.ccms.model.TopicSourceUrl;
 import org.jboss.pressgang.ccms.model.TopicToPropertyTag;
 import org.jboss.pressgang.ccms.model.TranslatedTopic;
 import org.jboss.pressgang.ccms.model.utils.EnversUtilities;
 import org.jboss.pressgang.ccms.provider.DBProviderFactory;
 import org.jboss.pressgang.ccms.utils.constants.CommonConstants;
 import org.jboss.pressgang.ccms.wrapper.collection.CollectionWrapper;
 import org.jboss.pressgang.ccms.wrapper.collection.DBTagCollectionWrapper;
 import org.jboss.pressgang.ccms.wrapper.collection.DBTopicCollectionWrapper;
 import org.jboss.pressgang.ccms.wrapper.collection.DBTopicSourceURLCollectionWrapper;
 import org.jboss.pressgang.ccms.wrapper.collection.DBTopicToPropertyTagCollectionWrapper;
 import org.jboss.pressgang.ccms.wrapper.collection.UpdateableCollectionWrapper;
 import org.jboss.pressgang.ccms.wrapper.collection.base.CollectionEventListener;
 import org.jboss.pressgang.ccms.wrapper.collection.base.UpdateableCollectionEventListener;
 import org.jboss.pressgang.ccms.zanata.ZanataDetails;
 
 public class DBTopicWrapper extends DBBaseWrapper<TopicWrapper, Topic> implements TopicWrapper {
     private final TagCollectionEventListener tagCollectionEventListener = new TagCollectionEventListener();
     private final SourceUrlCollectionEventListener sourceUrlCollectionEventListener = new SourceUrlCollectionEventListener();
     private final PropertyCollectionEventListener propertyCollectionEventListener = new PropertyCollectionEventListener();
     private final RelatedFromTopicCollectionEventListener relatedFromTopicCollectionEventListener = new
             RelatedFromTopicCollectionEventListener();
     private final RelatedToTopicCollectionEventListener relatedToTopicCollectionEventListener = new RelatedToTopicCollectionEventListener();
 
     private final Topic topic;
 
     public DBTopicWrapper(final DBProviderFactory providerFactory, final Topic topic, boolean isRevision) {
         super(providerFactory, isRevision, Topic.class);
         this.topic = topic;
     }
 
     @Override
     protected Topic getEntity() {
         return topic;
     }
 
     @Override
     public void setId(Integer id) {
         getEntity().setTopicId(id);
     }
 
     @Override
     public Topic unwrap() {
         return topic;
     }
 
     @Override
     public Integer getTopicId() {
         return getId();
     }
 
     @Override
     public Integer getTopicRevision() {
         return getRevision();
     }
 
     @Override
     public String getTitle() {
         return getEntity().getTopicTitle();
     }
 
     @Override
     public void setTitle(String title) {
         getEntity().setTopicTitle(title);
     }
 
     @Override
     public String getXml() {
         return getEntity().getTopicXML();
     }
 
     @Override
     public void setXml(String xml) {
         getEntity().setTopicXML(xml);
     }
 
     @Override
     public String getLocale() {
         return getEntity().getTopicLocale();
     }
 
     @Override
     public void setLocale(String locale) {
         getEntity().setTopicLocale(locale);
     }
 
     @Override
     public boolean hasTag(int tagId) {
         return getEntity().isTaggedWith(tagId);
     }
 
     @Override
     public CollectionWrapper<TagWrapper> getTags() {
         final CollectionWrapper<TagWrapper> collection = getWrapperFactory().createCollection(getEntity().getTags(), Tag.class,
                 isRevisionEntity());
         final DBTagCollectionWrapper dbCollection = (DBTagCollectionWrapper) collection;
         dbCollection.registerEventListener(tagCollectionEventListener);
         return dbCollection;
     }
 
     @Override
     public void setTags(CollectionWrapper<TagWrapper> tags) {
         if (tags == null) return;
         final DBTagCollectionWrapper dbTags = (DBTagCollectionWrapper) tags;
         dbTags.registerEventListener(tagCollectionEventListener);
 
         // Remove Tags
         final List<Tag> currentTags = getEntity().getTags();
         for (final Tag removeTag : currentTags) {
             getEntity().removeTag(removeTag);
         }
 
         // Add Tags
         final Collection<Tag> newTags = dbTags.unwrap();
         for (final Tag addTag : newTags) {
             getEntity().addTag(addTag);
         }
     }
 
     @Override
     public CollectionWrapper<TopicWrapper> getOutgoingRelationships() {
         final CollectionWrapper<TopicWrapper> collection = getWrapperFactory().createCollection(getEntity().getOutgoingRelatedTopicsArray(),
                 Topic.class, isRevisionEntity());
         final DBTopicCollectionWrapper dbCollection = (DBTopicCollectionWrapper) collection;
         dbCollection.registerEventListener(relatedToTopicCollectionEventListener);
         return dbCollection;
     }
 
     @Override
     public void setOutgoingRelationships(CollectionWrapper<TopicWrapper> outgoingTopics) {
         if (outgoingTopics == null) return;
         final DBTopicCollectionWrapper dbOutgoingTopics = (DBTopicCollectionWrapper) outgoingTopics;
         dbOutgoingTopics.registerEventListener(relatedToTopicCollectionEventListener);
 
         // Remove Topics
         final List<Topic> currentTopics = getEntity().getOutgoingRelatedTopicsArray();
         for (final Topic removeTopic : currentTopics) {
             getEntity().removeRelationshipTo(removeTopic.getTopicId(), 1);
         }
 
         // Add Topics
         final Collection<Topic> newTopics = dbOutgoingTopics.unwrap();
         for (final Topic addTopic : newTopics) {
             getEntity().addRelationshipTo(getEntityManager(), addTopic, 1);
         }
     }
 
     @Override
     public CollectionWrapper<TopicWrapper> getIncomingRelationships() {
         final CollectionWrapper<TopicWrapper> collection = getWrapperFactory().createCollection(getEntity().getIncomingRelatedTopicsArray(),
                 Topic.class, isRevisionEntity());
         final DBTopicCollectionWrapper dbCollection = (DBTopicCollectionWrapper) collection;
         dbCollection.registerEventListener(relatedFromTopicCollectionEventListener);
         return dbCollection;
     }
 
     @Override
     public void setIncomingRelationships(CollectionWrapper<TopicWrapper> incomingTopics) {
         if (incomingTopics == null) return;
         final DBTopicCollectionWrapper dbIncomingTopics = (DBTopicCollectionWrapper) incomingTopics;
         dbIncomingTopics.registerEventListener(relatedFromTopicCollectionEventListener);
 
         // Remove Topics
         final List<Topic> currentTopics = getEntity().getOutgoingRelatedTopicsArray();
         for (final Topic removeTopic : currentTopics) {
             getEntity().removeRelationshipFrom(removeTopic.getTopicId(), 1);
         }
 
         // Add Topics
         final Collection<Topic> newTopics = dbIncomingTopics.unwrap();
         for (final Topic addTopic : newTopics) {
             getEntity().addRelationshipFrom(getEntityManager(), addTopic, 1);
         }
     }
 
     @Override
     public List<TagWrapper> getTagsInCategories(List<Integer> categoryIds) {
         return getWrapperFactory().createList(getEntity().getTagsInCategoriesByID(categoryIds), isRevisionEntity());
     }
 
     @Override
     public UpdateableCollectionWrapper<PropertyTagInTopicWrapper> getProperties() {
         final CollectionWrapper<PropertyTagInTopicWrapper> collection = getWrapperFactory().createCollection(
                 getEntity().getTopicToPropertyTags(), TopicToPropertyTag.class, isRevisionEntity());
         final DBTopicToPropertyTagCollectionWrapper dbCollection = (DBTopicToPropertyTagCollectionWrapper) collection;
         dbCollection.registerEventListener(propertyCollectionEventListener);
         return dbCollection;
     }
 
     @Override
     public void setProperties(UpdateableCollectionWrapper<PropertyTagInTopicWrapper> properties) {
         if (properties == null) return;
         final DBTopicToPropertyTagCollectionWrapper dbProperties = (DBTopicToPropertyTagCollectionWrapper) properties;
         dbProperties.registerEventListener(propertyCollectionEventListener);
 
         // Remove the current properties
         final Set<TopicToPropertyTag> propertyTags = new HashSet<TopicToPropertyTag>(getEntity().getTopicToPropertyTags());
         for (final TopicToPropertyTag propertyTag : propertyTags) {
             getEntity().removePropertyTag(propertyTag);
         }
 
         // Set the new properties
         final Collection<TopicToPropertyTag> newPropertyTags = dbProperties.unwrap();
         for (final TopicToPropertyTag propertyTag : newPropertyTags) {
             getEntity().addPropertyTag(propertyTag);
         }
     }
 
     @Override
     public PropertyTagInTopicWrapper getProperty(int propertyId) {
         return getWrapperFactory().create(getEntity().getProperty(propertyId), isRevisionEntity());
     }
 
     @Override
     public CollectionWrapper<TopicSourceURLWrapper> getSourceURLs() {
         final CollectionWrapper<TopicSourceURLWrapper> collection = getWrapperFactory().createCollection(getEntity().getTopicSourceUrls(),
                 TopicSourceUrl.class, isRevisionEntity());
         final DBTopicSourceURLCollectionWrapper dbCollection = (DBTopicSourceURLCollectionWrapper) collection;
         dbCollection.registerEventListener(sourceUrlCollectionEventListener);
         return dbCollection;
     }
 
     @Override
     public void setSourceURLs(CollectionWrapper<TopicSourceURLWrapper> sourceURLs) {
         if (sourceURLs == null) return;
         final DBTopicSourceURLCollectionWrapper dbProperties = (DBTopicSourceURLCollectionWrapper) sourceURLs;
         dbProperties.registerEventListener(sourceUrlCollectionEventListener);
 
         // Remove the current properties
         final List<TopicSourceUrl> currentSourceUrls = getEntity().getTopicSourceUrls();
         for (final TopicSourceUrl sourceUrl : currentSourceUrls) {
             getEntity().removeTopicSourceUrl(sourceUrl.getId());
         }
 
         // Set the new properties
         final Collection<TopicSourceUrl> newSourceUrls = dbProperties.unwrap();
         for (final TopicSourceUrl sourceUrl : newSourceUrls) {
             getEntity().addTopicSourceUrl(sourceUrl);
         }
     }
 
     @Override
     public String getBugzillaBuildId() {
         final SimpleDateFormat formatter = new SimpleDateFormat(CommonConstants.FILTER_DISPLAY_DATE_FORMAT);
         return getId() + "-" + getRevision() + " " + formatter.format(
                 EnversUtilities.getFixedLastModifiedDate(getEntityManager(), getEntity())) + " " + getLocale();
     }
 
     @Override
     public String getEditorURL(final ZanataDetails zanataDetails) {
         return getEditorURL();
     }
 
     @Override
     public String getPressGangURL() {
         final String serverUrl = System.getProperty(CommonConstants.PRESS_GANG_UI_SYSTEM_PROPERTY);
         return (serverUrl.endsWith("/") ? serverUrl : (serverUrl + "/")) + "#SearchResultsAndTopicView;query;topicIds=" + getId();
     }
 
     @Override
     public String getEditorURL() {
         return getPressGangURL();
     }
 
     @Override
     public String getInternalURL() {
         return "Topic.seam?topicTopicId=" + getId() + "&selectedTab=Rendered+View";
     }
 
     @Override
     public String getErrorXRefId() {
         return CommonConstants.ERROR_XREF_ID_PREFIX + getId();
     }
 
     @Override
     public String getXRefId() {
         return "TopicID" + getId();
     }
 
     @Override
     public String getXRefPropertyOrId(int propertyId) {
         final PropertyTagInTopicWrapper propertyTag = getProperty(propertyId);
         if (propertyTag != null) {
             return propertyTag.getValue();
         } else {
             return getXRefId();
         }
     }
 
     @Override
     public String getDescription() {
         return getEntity().getTopicText();
     }
 
     @Override
     public void setDescription(String description) {
         getEntity().setTopicText(description);
     }
 
     @Override
     public Date getCreated() {
         return getEntity().getTopicTimeStamp();
     }
 
     @Override
     public void setCreated(Date created) {
         getEntity().setTopicTimeStamp(created);
     }
 
     @Override
     public Date getLastModified() {
         return EnversUtilities.getFixedLastModifiedDate(getEntityManager(), getEntity());
     }
 
     @Override
     public void setLastModified(Date lastModified) {
         getEntity().setLastModifiedDate(lastModified);
     }
 
     @Override
     public Integer getXmlDoctype() {
         return getEntity().getXmlDoctype();
     }
 
     @Override
     public void setXmlDoctype(Integer doctypeId) {
         getEntity().setXmlDoctype(doctypeId);
     }
 
     @Override
     public CollectionWrapper<TranslatedTopicWrapper> getTranslatedTopics() {
         return getWrapperFactory().createCollection(getEntity().getTranslatedTopics(getEntityManager(), getRevision()),
                 TranslatedTopic.class, isRevisionEntity());
     }
 
     /**
      *
      */
     private class RelatedToTopicCollectionEventListener implements CollectionEventListener<Topic> {
 
         @Override
         public void onAddItem(Topic entity) {
             getEntity().addRelationshipTo(getEntityManager(), entity, 1);
         }
 
         @Override
         public void onRemoveItem(Topic entity) {
             getEntity().removeRelationshipTo(entity.getId(), 1);
         }
 
         @Override
         public boolean equals(Object o) {
             return o instanceof RelatedToTopicCollectionEventListener;
         }
     }
 
     /**
      *
      */
     private class RelatedFromTopicCollectionEventListener implements CollectionEventListener<Topic> {
 
         @Override
         public void onAddItem(Topic entity) {
             getEntity().addRelationshipFrom(getEntityManager(), entity, 1);
         }
 
         @Override
         public void onRemoveItem(Topic entity) {
             getEntity().removeRelationshipFrom(entity.getId(), 1);
         }
 
         @Override
         public boolean equals(Object o) {
             return o instanceof RelatedFromTopicCollectionEventListener;
         }
     }
 
     /**
      *
      */
     private class TagCollectionEventListener implements CollectionEventListener<Tag> {
 
         @Override
         public void onAddItem(Tag entity) {
             getEntity().addTag(entity);
         }
 
         @Override
         public void onRemoveItem(Tag entity) {
             getEntity().removeTag(entity);
         }
 
         @Override
         public boolean equals(Object o) {
             return o instanceof TagCollectionEventListener;
         }
     }
 
     /**
      *
      */
     private class PropertyCollectionEventListener implements UpdateableCollectionEventListener<TopicToPropertyTag> {
         @Override
         public void onAddItem(final TopicToPropertyTag entity) {
             getEntity().addPropertyTag(entity);
         }
 
         @Override
         public void onRemoveItem(final TopicToPropertyTag entity) {
             getEntity().removePropertyTag(entity);
         }
 
         @Override
         public void onUpdateItem(final TopicToPropertyTag entity) {
         }
 
         @Override
         public boolean equals(Object o) {
             return o instanceof PropertyCollectionEventListener;
         }
     }
 
     /**
      *
      */
     private class SourceUrlCollectionEventListener implements UpdateableCollectionEventListener<TopicSourceUrl> {
         @Override
         public void onAddItem(final TopicSourceUrl entity) {
             getEntity().addTopicSourceUrl(entity);
         }
 
         @Override
         public void onRemoveItem(final TopicSourceUrl entity) {
             getEntity().removeTopicSourceUrl(entity.getId());
         }
 
         @Override
         public void onUpdateItem(final TopicSourceUrl entity) {
         }
 
         @Override
         public boolean equals(Object o) {
             return o instanceof SourceUrlCollectionEventListener;
         }
     }
 }
