 package org.jboss.pressgang.ccms.wrapper;
 
 import java.util.Collection;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import org.jboss.pressgang.ccms.contentspec.utils.EntityUtilities;
 import org.jboss.pressgang.ccms.model.RelationshipTag;
 import org.jboss.pressgang.ccms.model.Tag;
 import org.jboss.pressgang.ccms.model.Topic;
 import org.jboss.pressgang.ccms.model.TopicSourceUrl;
 import org.jboss.pressgang.ccms.model.TopicToPropertyTag;
 import org.jboss.pressgang.ccms.model.TranslatedTopic;
 import org.jboss.pressgang.ccms.model.TranslatedTopicData;
 import org.jboss.pressgang.ccms.model.TranslatedTopicString;
 import org.jboss.pressgang.ccms.model.contentspec.TranslatedCSNode;
 import org.jboss.pressgang.ccms.provider.DBProviderFactory;
 import org.jboss.pressgang.ccms.utils.constants.CommonConstants;
 import org.jboss.pressgang.ccms.wrapper.base.DBBaseEntityWrapper;
 import org.jboss.pressgang.ccms.wrapper.collection.CollectionWrapper;
 import org.jboss.pressgang.ccms.wrapper.collection.DBTagCollectionWrapper;
 import org.jboss.pressgang.ccms.wrapper.collection.DBTopicSourceURLCollectionWrapper;
 import org.jboss.pressgang.ccms.wrapper.collection.DBTopicToPropertyTagCollectionWrapper;
 import org.jboss.pressgang.ccms.wrapper.collection.DBTranslatedTopicDataCollectionWrapper;
 import org.jboss.pressgang.ccms.wrapper.collection.DBTranslatedTopicStringCollectionWrapper;
 import org.jboss.pressgang.ccms.wrapper.collection.UpdateableCollectionWrapper;
 import org.jboss.pressgang.ccms.wrapper.collection.handler.DBPropertyTagCollectionHandler;
 import org.jboss.pressgang.ccms.wrapper.collection.handler.DBRelatedFromTranslatedTopicCollectionHandler;
 import org.jboss.pressgang.ccms.wrapper.collection.handler.DBRelatedToTranslatedTopicCollectionHandler;
 import org.jboss.pressgang.ccms.wrapper.collection.handler.DBTagCollectionHandler;
 import org.jboss.pressgang.ccms.wrapper.collection.handler.DBTopicSourceUrlCollectionHandler;
 import org.jboss.pressgang.ccms.wrapper.collection.handler.DBTranslatedStringCollectionHandler;
 import org.jboss.pressgang.ccms.zanata.ZanataDetails;
 
 public class DBTranslatedTopicDataWrapper extends DBBaseEntityWrapper<TranslatedTopicWrapper,
         TranslatedTopicData> implements TranslatedTopicWrapper {
     private final static RelationshipTag dummyRelationshipTag = new RelationshipTag();
 
     static {
         dummyRelationshipTag.setRelationshipTagId(1);
     }
 
     private final DBTagCollectionHandler tagCollectionHandler;
     private final DBTopicSourceUrlCollectionHandler sourceUrlCollectionHandler;
     private final DBPropertyTagCollectionHandler<TopicToPropertyTag> propertyCollectionHandler;
     private final DBRelatedFromTranslatedTopicCollectionHandler relatedFromCollectionHandler;
     private final DBRelatedToTranslatedTopicCollectionHandler relatedToCollectionHandler;
     private final DBTranslatedStringCollectionHandler<TranslatedTopicString> translatedStringCollectionHandler;
 
     private final TranslatedTopicData translatedTopicData;
 
     public DBTranslatedTopicDataWrapper(final DBProviderFactory providerFactory, final TranslatedTopicData translatedTopicData,
             boolean isRevision) {
         super(providerFactory, isRevision, TranslatedTopicData.class);
         this.translatedTopicData = translatedTopicData;
         tagCollectionHandler = new DBTagCollectionHandler(getEnversTopic());
         sourceUrlCollectionHandler = new DBTopicSourceUrlCollectionHandler(getEnversTopic());
         propertyCollectionHandler = new DBPropertyTagCollectionHandler<TopicToPropertyTag>(getEnversTopic());
         relatedFromCollectionHandler = new DBRelatedFromTranslatedTopicCollectionHandler(getEnversTopic(),
                 providerFactory.getEntityManager());
         relatedToCollectionHandler = new DBRelatedToTranslatedTopicCollectionHandler(getEnversTopic(), providerFactory.getEntityManager());
         translatedStringCollectionHandler = new DBTranslatedStringCollectionHandler<TranslatedTopicString>(translatedTopicData);
     }
 
     protected TranslatedTopic getTranslatedTopic() {
         if (getEntity().getTranslatedTopic() == null) {
             getEntity().setTranslatedTopic(new TranslatedTopic());
         }
         return getEntity().getTranslatedTopic();
     }
 
     @Override
     protected TranslatedTopicData getEntity() {
         return translatedTopicData;
     }
 
     protected Topic getEnversTopic() {
         return getTranslatedTopic().getEnversTopic(getDatabaseProvider().getEntityManager());
     }
 
     @Override
     public Integer getId() {
         return getTranslatedTopic().getTopicId();
     }
 
     @Override
     public void setId(Integer id) {
         getEntity().setTranslatedTopicDataId(id);
     }
 
     @Override
     public Integer getTopicId() {
         return getTranslatedTopic().getTopicId();
     }
 
     @Override
     public Integer getTopicRevision() {
         return getTranslatedTopic().getTopicRevision();
     }
 
     @Override
     public String getTitle() {
         return getEnversTopic().getTopicTitle();
     }
 
     @Override
     public void setTitle(String title) {
         getEnversTopic().setTopicTitle(title);
     }
 
     @Override
     public String getXml() {
         return getEntity().getTranslatedXml();
     }
 
     @Override
     public void setXml(String xml) {
         getEntity().setTranslatedXml(xml);
     }
 
     @Override
     public Integer getXmlFormat() {
         return getTopic().getXmlFormat();
     }
 
     @Override
     public String getLocale() {
         return getEntity().getTranslationLocale();
     }
 
     @Override
     public void setLocale(String locale) {
         getEntity().setTranslationLocale(locale);
     }
 
     @Override
     public boolean hasTag(int tagId) {
         return getEnversTopic().isTaggedWith(tagId);
     }
 
     @Override
     public CollectionWrapper<TagWrapper> getTags() {
         return getWrapperFactory().createCollection(getEnversTopic().getTags(), Tag.class, isRevisionEntity(), tagCollectionHandler);
     }
 
     @Override
     public void setTags(CollectionWrapper<TagWrapper> tags) {
         if (tags == null) return;
         final DBTagCollectionWrapper dbTags = (DBTagCollectionWrapper) tags;
         dbTags.setHandler(tagCollectionHandler);
 
         // Since tags in a topic are generated from a set and not cached, there is no way to see if this collection is the
         // same as the collection passed. So just process all the tags anyway.
 
         // Add new tags and skip any existing tags
         final List<Tag> currentTags = getEnversTopic().getTags();
         final Collection<Tag> newTags = dbTags.unwrap();
         for (final Tag tag : newTags) {
             if (currentTags.contains(tag)) {
                 currentTags.remove(tag);
                 continue;
             } else {
                 getEnversTopic().addTag(tag);
             }
         }
 
         // Remove tags that should no longer exist in the collection
         for (final Tag removeTag : currentTags) {
             getEnversTopic().removeTag(removeTag);
         }
     }
 
     @Override
     public CollectionWrapper<TranslatedTopicWrapper> getOutgoingRelationships() {
         return getWrapperFactory().createCollection(getEntity().getOutgoingRelatedTranslatedTopicData(getEntityManager()),
                 TranslatedTopicData.class, isRevisionEntity(), relatedToCollectionHandler);
     }
 
     @Override
     public void setOutgoingRelationships(CollectionWrapper<TranslatedTopicWrapper> outgoingTopics) {
         if (outgoingTopics == null) return;
         final DBTranslatedTopicDataCollectionWrapper dbOutgoingTopics = (DBTranslatedTopicDataCollectionWrapper) outgoingTopics;
         dbOutgoingTopics.setHandler(relatedToCollectionHandler);
 
         // Since relationships in a topic are generated from a set and not cached, there is no way to see if this collection is the
         // same as the collection passed. So just process all the relationships anyway.
 
         // Add new relationships and skip any existing relationships
         final List<TranslatedTopicData> currentRelationships = getEntity().getOutgoingRelatedTranslatedTopicData(getEntityManager());
         final Collection<TranslatedTopicData> newRelationships = dbOutgoingTopics.unwrap();
         for (final TranslatedTopicData relationship : newRelationships) {
             if (currentRelationships.contains(relationship)) {
                 currentRelationships.remove(relationship);
                 continue;
             } else {
                 getEnversTopic().addRelationshipTo(getEntityManager(), relationship.getTranslatedTopic().getTopicId(), 1);
             }
         }
 
         // Remove relationships that should no longer exist in the collection
         for (final TranslatedTopicData removeRelationship : currentRelationships) {
             getEnversTopic().removeRelationshipTo(removeRelationship.getTranslatedTopic().getTopicId(), 1);
         }
     }
 
     @Override
     public CollectionWrapper<TranslatedTopicWrapper> getIncomingRelationships() {
         final CollectionWrapper<TranslatedTopicWrapper> collection = getWrapperFactory().createCollection(
                 getEntity().getIncomingRelatedTranslatedTopicData(getEntityManager()), TranslatedTopicData.class, isRevisionEntity(),
                 relatedFromCollectionHandler);
         return collection;
     }
 
     @Override
     public void setIncomingRelationships(CollectionWrapper<TranslatedTopicWrapper> incomingTopics) {
         if (incomingTopics == null) return;
         final DBTranslatedTopicDataCollectionWrapper dbIncomingTopics = (DBTranslatedTopicDataCollectionWrapper) incomingTopics;
         dbIncomingTopics.setHandler(relatedFromCollectionHandler);
 
         // Since relationships in a topic are generated from a set and not cached, there is no way to see if this collection is the
         // same as the collection passed. So just process all the relationships anyway.
 
         // Add new relationships and skip any existing relationships
         final List<TranslatedTopicData> currentRelationships = getEntity().getIncomingRelatedTranslatedTopicData(getEntityManager());
         final Collection<TranslatedTopicData> newRelationships = dbIncomingTopics.unwrap();
         for (final TranslatedTopicData relationship : newRelationships) {
             if (currentRelationships.contains(relationship)) {
                 currentRelationships.remove(relationship);
                 continue;
             } else {
                 getEnversTopic().addRelationshipFrom(getEntityManager(), relationship.getTranslatedTopic().getTopicId(), 1);
             }
         }
 
         // Remove relationships that should no longer exist in the collection
         for (final TranslatedTopicData removeRelationship : currentRelationships) {
             getEnversTopic().removeRelationshipFrom(removeRelationship.getTranslatedTopic().getTopicId(), 1);
         }
     }
 
     @Override
     public List<TagWrapper> getTagsInCategories(List<Integer> categoryIds) {
         return getWrapperFactory().createList(getEnversTopic().getTagsInCategoriesByID(categoryIds), isRevisionEntity());
     }
 
     @Override
     public UpdateableCollectionWrapper<PropertyTagInTopicWrapper> getProperties() {
         final CollectionWrapper<PropertyTagInTopicWrapper> collection = getWrapperFactory().createCollection(
                 getEnversTopic().getTopicToPropertyTags(), TopicToPropertyTag.class, isRevisionEntity(), propertyCollectionHandler);
         return (UpdateableCollectionWrapper<PropertyTagInTopicWrapper>) collection;
     }
 
     @Override
     public void setProperties(UpdateableCollectionWrapper<PropertyTagInTopicWrapper> properties) {
         if (properties == null) return;
         final DBTopicToPropertyTagCollectionWrapper dbProperties = (DBTopicToPropertyTagCollectionWrapper) properties;
         dbProperties.setHandler(propertyCollectionHandler);
 
         // Only bother readjusting the collection if its a different collection than the current
         if (dbProperties.unwrap() != getEnversTopic().getPropertyTags()) {
             // Add new property tags and skip any existing tags
             final Set<TopicToPropertyTag> currentProperties = new HashSet<TopicToPropertyTag>(getEnversTopic().getPropertyTags());
             final Collection<TopicToPropertyTag> newProperties = dbProperties.unwrap();
             for (final TopicToPropertyTag property : newProperties) {
                 if (currentProperties.contains(property)) {
                     currentProperties.remove(property);
                     continue;
                 } else {
                     property.setTopic(getEnversTopic());
                     getEnversTopic().addPropertyTag(property);
                 }
             }
 
             // Remove property tags that should no longer exist in the collection
             for (final TopicToPropertyTag removeProperty : currentProperties) {
                 getEnversTopic().removePropertyTag(removeProperty);
             }
         }
     }
 
     @Override
     public PropertyTagInTopicWrapper getProperty(int propertyId) {
         return getWrapperFactory().create(getEnversTopic().getProperty(propertyId), isRevisionEntity());
     }
 
     @Override
     public List<PropertyTagInTopicWrapper> getProperties(int propertyId) {
         return getWrapperFactory().createList(getEnversTopic().getProperties(propertyId), isRevisionEntity());
     }
 
     @Override
     public UpdateableCollectionWrapper<TopicSourceURLWrapper> getSourceURLs() {
         final CollectionWrapper<TopicSourceURLWrapper> collection = getWrapperFactory().createCollection(
                 getEnversTopic().getTopicSourceUrls(), TopicSourceUrl.class, isRevisionEntity(), sourceUrlCollectionHandler);
         return (UpdateableCollectionWrapper<TopicSourceURLWrapper>) collection;
     }
 
     @Override
     public void setSourceURLs(UpdateableCollectionWrapper<TopicSourceURLWrapper> sourceURLs) {
         if (sourceURLs == null) return;
         final DBTopicSourceURLCollectionWrapper dbSourceUrls = (DBTopicSourceURLCollectionWrapper) sourceURLs;
         dbSourceUrls.setHandler(sourceUrlCollectionHandler);
 
         // Since source urls in a topic are generated from a set and not cached, there is no way to see if this collection is the
         // same as the collection passed. So just process all the urls anyway.
 
         // Add new source urls and skip any existing urls
         final Set<TopicSourceUrl> currentSourceUrls = new HashSet<TopicSourceUrl>(getEnversTopic().getTopicSourceUrls());
         final Collection<TopicSourceUrl> newSourceUrls = dbSourceUrls.unwrap();
         for (final TopicSourceUrl sourceUrl : newSourceUrls) {
             if (currentSourceUrls.contains(sourceUrl)) {
                 currentSourceUrls.remove(sourceUrl);
                 continue;
             } else {
                 getEnversTopic().addTopicSourceUrl(sourceUrl);
             }
         }
 
         // Remove source urls that should no longer exist in the collection
         for (final TopicSourceUrl removeSourceUrl : currentSourceUrls) {
             getEnversTopic().removeTopicSourceUrl(removeSourceUrl);
         }
     }
 
     @Override
     public String getBugzillaBuildId() {
         return "Translation " + getZanataId() + " " + getLocale();
     }
 
     @Override
     public String getEditorURL(ZanataDetails zanataDetails) {
         final String zanataServerUrl = zanataDetails == null ? null : zanataDetails.getServer();
         final String zanataProject = zanataDetails == null ? null : zanataDetails.getProject();
         final String zanataVersion = zanataDetails == null ? null : zanataDetails.getVersion();
 
         if (zanataServerUrl != null && !zanataServerUrl.isEmpty() && zanataProject != null && !zanataProject.isEmpty() && zanataVersion
                 != null && !zanataVersion.isEmpty()) {
             final String zanataId = getZanataId();
 
             return zanataServerUrl + "webtrans/Application.html?project=" + zanataProject + "&amp;iteration=" + zanataVersion + "&amp;" +
                     "doc=" + zanataId + "&amp;localeId=" + getLocale() + "#view:doc;doc:" + zanataId;
         } else {
             return null;
         }
     }
 
     @Override
     public String getPressGangURL() {
         /*
          * If the topic isn't a dummy then link to the translated counterpart. If the topic is a dummy URL and the locale doesn't match
          * the historical topic's
          * locale then it means that the topic has been pushed to zanata so link to the original pushed translation. If neither of these
          * rules apply then return null.
          */
        final String serverUrl = System.getProperty(CommonConstants.PRESS_GANG_UI_SYSTEM_PROPERTY);
         if (EntityUtilities.isDummyTopic(this) ) {
             return (serverUrl.endsWith(
                     "/") ? serverUrl : (serverUrl + "/")) + "#TranslatedTopicResultsAndTranslatedTopicView;query;zanataIds=" +
                     getZanataId() + ";locale1=" + getLocale() + "1;";
         } else if (EntityUtilities.hasBeenPushedForTranslation(this)) {
             return (serverUrl.endsWith(
                     "/") ? serverUrl : (serverUrl + "/")) + "#TranslatedTopicResultsAndTranslatedTopicView;query;zanataIds=" +
                     getZanataId();
         } else {
            return (serverUrl.endsWith("/") ? serverUrl : (serverUrl + "/")) + "#SearchResultsAndTopicView;query;topicIds=" + getTopicId();
         }
     }
 
     @Override
     public String getErrorXRefId() {
         return CommonConstants.ERROR_XREF_ID_PREFIX + getZanataId();
     }
 
     @Override
     public String getXRefId() {
         return "TranslatedTopicID" + getEntity().getId();
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
     public void setTopicId(Integer id) {
         getTranslatedTopic().setTopicId(id);
     }
 
     @Override
     public void setTopicRevision(Integer revision) {
         getTranslatedTopic().setTopicRevision(revision);
     }
 
     @Override
     public Integer getTranslatedTopicId() {
         return getTranslatedTopic().getTranslatedTopicId();
     }
 
     @Override
     public void setTranslatedTopicId(Integer translatedTopicId) {
         getTranslatedTopic().setTranslatedTopicId(translatedTopicId);
     }
 
     @Override
     public String getZanataId() {
         return getTranslatedTopic().getZanataId();
     }
 
     @Override
     public boolean getContainsFuzzyTranslations() {
         return getEntity().containsFuzzyTranslation();
     }
 
     @Override
     public Integer getTranslationPercentage() {
         return getEntity().getTranslationPercentage();
     }
 
     @Override
     public void setTranslationPercentage(Integer percentage) {
         getEntity().setTranslationPercentage(percentage);
     }
 
     @Override
     public String getTranslatedXMLCondition() {
         return getTranslatedTopic().getTranslatedXMLCondition();
     }
 
     @Override
     public void setTranslatedXMLCondition(String translatedXMLCondition) {
         getTranslatedTopic().setTranslatedXMLCondition(translatedXMLCondition);
     }
 
     @Override
     public String getTranslatedAdditionalXML() {
         return getEntity().getTranslatedAdditionalXml();
     }
 
     @Override
     public void setTranslatedAdditionalXML(String translatedAdditionalXML) {
         getEntity().setTranslatedAdditionalXml(translatedAdditionalXML);
     }
 
     @Override
     public String getCustomEntities() {
         return getTranslatedTopic().getCustomEntities();
     }
 
     @Override
     public void setCustomEntities(String customEntities) {
         getTranslatedTopic().setCustomEntities(customEntities);
     }
 
     @Override
     public UpdateableCollectionWrapper<TranslatedTopicStringWrapper> getTranslatedTopicStrings() {
         final CollectionWrapper<TranslatedTopicStringWrapper> collection = getWrapperFactory().createCollection(
                 getEntity().getTranslatedTopicDataStringsArray(), TranslatedTopicString.class, isRevisionEntity(),
                 translatedStringCollectionHandler);
         return (UpdateableCollectionWrapper<TranslatedTopicStringWrapper>) collection;
     }
 
     @Override
     public void setTranslatedTopicStrings(UpdateableCollectionWrapper<TranslatedTopicStringWrapper> translatedStrings) {
         if (translatedStrings == null) return;
         final DBTranslatedTopicStringCollectionWrapper dbTranslatedStrings = (DBTranslatedTopicStringCollectionWrapper) translatedStrings;
         dbTranslatedStrings.setHandler(translatedStringCollectionHandler);
 
         // Only bother readjusting the collection if its a different collection than the current
         if (dbTranslatedStrings.unwrap() != getEntity().getTranslatedTopicStrings()) {
             // Add new translated strings and skip any existing strings
             final Set<TranslatedTopicString> currentStrings = new HashSet<TranslatedTopicString>(getEntity().getTranslatedTopicStrings());
             final Collection<TranslatedTopicString> newStrings = dbTranslatedStrings.unwrap();
             for (final TranslatedTopicString string : newStrings) {
                 if (currentStrings.contains(string)) {
                     currentStrings.remove(string);
                     continue;
                 } else {
                     getEntity().addTranslatedString(string);
                 }
             }
 
             // Remove strings that should no longer exist in the collection
             for (final TranslatedTopicString removeString : currentStrings) {
                 getEntity().removeTranslatedString(removeString);
             }
         }
     }
 
     @Override
     public TopicWrapper getTopic() {
         return getWrapperFactory().create(getEnversTopic(), true);
     }
 
     @Override
     public void setTopic(TopicWrapper topic) {
         getTranslatedTopic().setEnversTopic(topic == null ? null : (Topic) topic.unwrap());
     }
 
     @Override
     public TranslatedCSNodeWrapper getTranslatedCSNode() {
         return getWrapperFactory().create(getEntity().getTranslatedTopic(), isRevisionEntity(), TranslatedCSNodeWrapper.class);
     }
 
     @Override
     public void setTranslatedCSNode(TranslatedCSNodeWrapper translatedCSNode) {
         getTranslatedTopic().setTranslatedCSNode(translatedCSNode == null ? null : (TranslatedCSNode) translatedCSNode.unwrap());
     }
 }
