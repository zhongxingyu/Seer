 package org.jboss.pressgang.ccms.contentspec.processor;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.concurrent.atomic.AtomicBoolean;
 
 import org.apache.log4j.Logger;
 import org.jboss.pressgang.ccms.contentspec.Comment;
 import org.jboss.pressgang.ccms.contentspec.ContentSpec;
 import org.jboss.pressgang.ccms.contentspec.KeyValueNode;
 import org.jboss.pressgang.ccms.contentspec.Level;
 import org.jboss.pressgang.ccms.contentspec.Node;
 import org.jboss.pressgang.ccms.contentspec.SpecNode;
 import org.jboss.pressgang.ccms.contentspec.SpecTopic;
 import org.jboss.pressgang.ccms.contentspec.constants.CSConstants;
 import org.jboss.pressgang.ccms.contentspec.entities.ProcessRelationship;
 import org.jboss.pressgang.ccms.contentspec.entities.Relationship;
 import org.jboss.pressgang.ccms.contentspec.entities.TargetRelationship;
 import org.jboss.pressgang.ccms.contentspec.entities.TopicRelationship;
 import org.jboss.pressgang.ccms.contentspec.enums.BookType;
 import org.jboss.pressgang.ccms.contentspec.enums.RelationshipType;
 import org.jboss.pressgang.ccms.contentspec.enums.TopicType;
 import org.jboss.pressgang.ccms.contentspec.interfaces.ShutdownAbleApp;
 import org.jboss.pressgang.ccms.contentspec.processor.constants.ProcessorConstants;
 import org.jboss.pressgang.ccms.contentspec.processor.exceptions.ProcessingException;
 import org.jboss.pressgang.ccms.contentspec.processor.structures.ProcessingOptions;
 import org.jboss.pressgang.ccms.contentspec.processor.utils.ProcessorUtilities;
 import org.jboss.pressgang.ccms.contentspec.utils.TopicPool;
 import org.jboss.pressgang.ccms.contentspec.utils.logging.ErrorLogger;
 import org.jboss.pressgang.ccms.contentspec.utils.logging.ErrorLoggerManager;
 import org.jboss.pressgang.ccms.provider.CSNodeProvider;
 import org.jboss.pressgang.ccms.provider.ContentSpecProvider;
 import org.jboss.pressgang.ccms.provider.DataProviderFactory;
 import org.jboss.pressgang.ccms.provider.PropertyTagProvider;
 import org.jboss.pressgang.ccms.provider.TagProvider;
 import org.jboss.pressgang.ccms.provider.TopicProvider;
 import org.jboss.pressgang.ccms.provider.TopicSourceURLProvider;
 import org.jboss.pressgang.ccms.utils.constants.CommonConstants;
 import org.jboss.pressgang.ccms.wrapper.CSNodeWrapper;
 import org.jboss.pressgang.ccms.wrapper.CSRelatedNodeWrapper;
 import org.jboss.pressgang.ccms.wrapper.ContentSpecWrapper;
 import org.jboss.pressgang.ccms.wrapper.LogMessageWrapper;
 import org.jboss.pressgang.ccms.wrapper.PropertyTagInContentSpecWrapper;
 import org.jboss.pressgang.ccms.wrapper.PropertyTagInTopicWrapper;
 import org.jboss.pressgang.ccms.wrapper.PropertyTagWrapper;
 import org.jboss.pressgang.ccms.wrapper.TagWrapper;
 import org.jboss.pressgang.ccms.wrapper.TopicSourceURLWrapper;
 import org.jboss.pressgang.ccms.wrapper.TopicWrapper;
 import org.jboss.pressgang.ccms.wrapper.collection.CollectionWrapper;
 import org.jboss.pressgang.ccms.wrapper.collection.UpdateableCollectionWrapper;
 import org.jsoup.Jsoup;
 import org.jsoup.nodes.Document;
 
 /**
  * A class to fully process a Content Specification. It first parses the data using a ContentSpecParser,
  * then validates the Content Specification using a ContentSpecValidator and lastly saves the data to the database.
  * It can also be configured to only validate the data and not save it.
  *
  * @author lnewson
  */
 @SuppressWarnings("rawtypes")
 public class ContentSpecProcessor implements ShutdownAbleApp {
     private final Logger LOG = Logger.getLogger(ContentSpecProcessor.class.getPackage().getName() + ".CustomContentSpecProcessor");
 
     private static List<String> IGNORE_META_DATA = Arrays.asList(CSConstants.CHECKSUM_TITLE, CSConstants.ID_TITLE);
 
     private final ErrorLogger log;
     private final DataProviderFactory providerFactory;
 
     private final ProcessingOptions processingOptions;
     private ContentSpecValidator validator;
     private final TopicPool topics;
     private final AtomicBoolean isShuttingDown = new AtomicBoolean(false);
     private final AtomicBoolean shutdown = new AtomicBoolean(false);
 
     /**
      * Constructor
      *
      * @param factory           A DBManager object that manages the REST connection and the functions to read/write to the REST Interface.
      * @param loggerManager
      * @param processingOptions The set of options to use when processing.
      */
     public ContentSpecProcessor(final DataProviderFactory factory, final ErrorLoggerManager loggerManager,
             final ProcessingOptions processingOptions) {
 
         providerFactory = factory;
 
         log = loggerManager.getLogger(ContentSpecProcessor.class);
         topics = new TopicPool(factory);
         this.processingOptions = processingOptions;
         validator = new ContentSpecValidator(factory, loggerManager, processingOptions);
     }
 
     /**
      * Process a content specification so that it is parsed, validated and saved.
      *
      * @param contentSpec The Content Specification that is to be processed.
      * @param username    The user who requested the process operation.
      * @param mode        The mode to parse the content specification in.
      * @return True if everything was processed successfully otherwise false.
      */
     public boolean processContentSpec(final ContentSpec contentSpec, final String username, final ContentSpecParser.ParsingMode mode) {
         return processContentSpec(contentSpec, username, mode, (String) null);
     }
 
     /**
      * Process a content specification so that it is parsed, validated and saved.
      *
      * @param contentSpec The Content Specification that is to be processed.
      * @param username    The user who requested the process operation.
      * @param mode        The mode to parse the content specification in.
      * @param logMessage
      * @return True if everything was processed successfully otherwise false.
      */
     public boolean processContentSpec(final ContentSpec contentSpec, final String username, final ContentSpecParser.ParsingMode mode,
             final LogMessageWrapper logMessage) {
         return processContentSpec(contentSpec, username, mode, null, logMessage);
     }
 
     /**
      * Process a content specification so that it is parsed, validated and saved.
      *
      * @param contentSpec    The Content Specification that is to be processed.
      * @param username       The user who requested the process operation.
      * @param mode           The mode to parse the content specification in.
      * @param overrideLocale Override the default locale using this parameter.
      * @return True if everything was processed successfully otherwise false.
      */
     public boolean processContentSpec(final ContentSpec contentSpec, final String username, final ContentSpecParser.ParsingMode mode,
             final String overrideLocale) {
         return processContentSpec(contentSpec, username, mode, overrideLocale, null);
     }
 
     /**
      * Process a content specification so that it is parsed, validated and saved.
      *
      * @param contentSpec    The Content Specification that is to be processed.
      * @param username       The user who requested the process operation.
      * @param mode           The mode to parse the content specification in.
      * @param overrideLocale Override the default locale using this parameter.
      * @param logMessage
      * @return True if everything was processed successfully otherwise false.
      */
     public boolean processContentSpec(final ContentSpec contentSpec, final String username, final ContentSpecParser.ParsingMode mode,
             final String overrideLocale, final LogMessageWrapper logMessage) {
         boolean editing = false;
         if (mode == ContentSpecParser.ParsingMode.EDITED) {
             editing = true;
         }
 
         // Set the user as the assigned writer
         contentSpec.setAssignedWriter(username);
 
         // Change the locale if the overrideLocale isn't null
         if (overrideLocale != null) {
             contentSpec.setLocale(overrideLocale);
         }
 
         // Set the log details user if one isn't set
         if (logMessage != null && username != null && logMessage.getUser() == null) {
             logMessage.setUser(CSConstants.UNKNOWN_USER_ID.toString());
         }
 
         // Check if the app should be shutdown
         if (isShuttingDown.get()) {
             shutdown.set(true);
             return false;
         }
 
         // Validate the content specification before doing any rest calls
         LOG.info("Starting first validation pass...");
 
         // Validate the relationships
         if (!validator.preValidateRelationships(contentSpec) || !validator.preValidateContentSpec(contentSpec)) {
             log.error(ProcessorConstants.ERROR_INVALID_CS_MSG);
             return false;
         }
 
         // Check if the app should be shutdown
         if (isShuttingDown.get()) {
             shutdown.set(true);
             return false;
         }
 
         // Validate the content specification now that we have most of the data from the REST API
         LOG.info("Starting second validation pass...");
 
         if (!validator.postValidateContentSpec(contentSpec, username)) {
             log.error(ProcessorConstants.ERROR_INVALID_CS_MSG);
             return false;
         } else {
             log.info(ProcessorConstants.INFO_VALID_CS_MSG);
 
             // If we aren't validating then save the content specification
             if (!processingOptions.isValidating()) {
                 // Check if the app should be shutdown
                 if (isShuttingDown.get()) {
                     shutdown.set(true);
                     return false;
                 }
 
                 LOG.info("Saving the Content Specification to the server...");
                 if (saveContentSpec(providerFactory, contentSpec, editing, username, logMessage)) {
                     log.info(ProcessorConstants.INFO_SUCCESSFUL_SAVE_MSG);
                 } else {
                     log.error(ProcessorConstants.ERROR_PROCESSING_ERROR_MSG);
                     return false;
                 }
             }
         }
         return true;
     }
 
     /**
      * Creates an entity to be sent through the REST interface to create or update a DB entry.
      *
      * @param providerFactory
      * @param specTopic       The Content Specification Topic to create the topic entity from.
      * @return The new topic object if any changes where made otherwise null.
      * @throws ProcessingException
      */
     protected TopicWrapper createTopicEntity(final DataProviderFactory providerFactory,
             final SpecTopic specTopic) throws ProcessingException {
         // Duplicates reference another new or cloned topic and should not have a different new/updated underlying topic
         if (specTopic.isTopicAClonedDuplicateTopic() || specTopic.isTopicADuplicateTopic()) return null;
 
         final TagProvider tagProvider = providerFactory.getProvider(TagProvider.class);
         final TopicSourceURLProvider topicSourceURLProvider = providerFactory.getProvider(TopicSourceURLProvider.class);
 
         if (isShuttingDown.get()) {
             return null;
         }
 
         // If the spec topic is a clone or new topic then it will have changed no mater what else is done, since it's a new topic
         boolean changed = specTopic.isTopicAClonedTopic() || specTopic.isTopicANewTopic();
         final TopicWrapper topic = getTopicForSpecTopic(providerFactory, specTopic);
 
         // Check if the topic is null, if so throw an exception as it shouldn't be at this stage
         if (topic == null) {
             throw new ProcessingException("Creating a topic failed.");
         }
 
         // Check if the app should be shutdown
         if (isShuttingDown.get()) {
             return null;
         }
 
         // Process the topic and add or remove any tags
         if (processTopicTags(tagProvider, specTopic, topic)) {
             changed = true;
         }
 
         // Check if the app should be shutdown
         if (isShuttingDown.get()) {
             return null;
         }
 
         // Process and set the assigned writer for new and cloned topics
         if (!specTopic.isTopicAnExistingTopic()) {
             processAssignedWriter(tagProvider, specTopic, topic);
             changed = true;
         }
 
         // Check if the app should be shutdown
         if (isShuttingDown.get()) {
             return null;
         }
 
         // Process and set the source urls for new and cloned topics
         if (!specTopic.isTopicAnExistingTopic()) {
             if (processTopicSourceUrls(topicSourceURLProvider, specTopic, topic)) changed = true;
         }
 
         // Check if the app should be shutdown
         if (isShuttingDown.get()) {
             return null;
         }
 
         if (changed) {
             return topic;
         } else {
             return null;
         }
     }
 
     /**
      * Gets or creates the underlying Topic Entity for a spec topic.
      *
      * @param providerFactory
      * @param specTopic       The spec topic to get the topic entity for.
      * @return The topic entity if one could be found, otherwise null.
      */
     protected TopicWrapper getTopicForSpecTopic(final DataProviderFactory providerFactory, final SpecTopic specTopic) {
         TopicWrapper topic = null;
 
         if (specTopic.isTopicANewTopic()) {
             topic = getTopicForNewSpecTopic(providerFactory, specTopic);
         } else if (specTopic.isTopicAClonedTopic()) {
             topic = ProcessorUtilities.cloneTopic(providerFactory, specTopic);
         } else if (specTopic.isTopicAnExistingTopic()) {
             topic = getTopicForExistingSpecTopic(providerFactory, specTopic);
         }
 
         return topic;
     }
 
     /**
      * C
      *
      * @param providerFactory
      * @param specTopic
      * @return
      */
     private TopicWrapper getTopicForNewSpecTopic(final DataProviderFactory providerFactory, final SpecTopic specTopic) {
         final TopicProvider topicProvider = providerFactory.getProvider(TopicProvider.class);
         final TagProvider tagProvider = providerFactory.getProvider(TagProvider.class);
         final PropertyTagProvider propertyTagProvider = providerFactory.getProvider(PropertyTagProvider.class);
 
         // Create the topic entity.
         final TopicWrapper topic = topicProvider.newTopic();
 
         // Set the basics
         topic.setTitle(specTopic.getTitle());
         topic.setDescription(specTopic.getDescription(true));
         topic.setXml("");
         topic.setXmlDoctype(CommonConstants.DOCBOOK_45);
         topic.setLocale(CommonConstants.DEFAULT_LOCALE);
 
         // Write the type
         final TagWrapper tag = tagProvider.getTagByName(specTopic.getType());
         if (tag == null) {
             log.error(String.format(ProcessorConstants.ERROR_TYPE_NONEXIST_MSG, specTopic.getLineNumber(), specTopic.getText()));
             return null;
         }
 
         // Add the type to the topic
         topic.setTags(tagProvider.newTagCollection());
         topic.getTags().addNewItem(tag);
 
         // Create the unique ID for the property
         topic.setProperties(propertyTagProvider.newPropertyTagInTopicCollection(topic));
         final PropertyTagWrapper propertyTag = propertyTagProvider.getPropertyTag(CSConstants.CSP_PROPERTY_ID);
         final PropertyTagInTopicWrapper cspProperty = propertyTagProvider.newPropertyTagInTopic(propertyTag, topic);
         cspProperty.setValue(specTopic.getUniqueId());
         topic.getProperties().addNewItem(cspProperty);
 
         // Add the added by property tag
         final String assignedWriter = specTopic.getAssignedWriter(true);
         if (assignedWriter != null) {
             final PropertyTagWrapper addedByPropertyTag = propertyTagProvider.getPropertyTag(CSConstants.ADDED_BY_PROPERTY_TAG_ID);
             final PropertyTagInTopicWrapper addedByProperty = propertyTagProvider.newPropertyTagInTopic(addedByPropertyTag, topic);
             addedByProperty.setValue(assignedWriter);
             topic.getProperties().addNewItem(addedByProperty);
         }
 
         return topic;
     }
 
     /**
      * @param providerFactory
      * @param specTopic
      * @return
      */
     private TopicWrapper getTopicForExistingSpecTopic(final DataProviderFactory providerFactory, final SpecTopic specTopic) {
         final TopicProvider topicProvider = providerFactory.getProvider(TopicProvider.class);
         final PropertyTagProvider propertyTagProvider = providerFactory.getProvider(PropertyTagProvider.class);
 
         // Get the current existing topic
         final TopicWrapper topic = topicProvider.getTopic(specTopic.getDBId(), null);
 
         // Update the CSP Property tag
         final UpdateableCollectionWrapper<PropertyTagInTopicWrapper> properties;
         if (topic.getProperties() == null) {
             properties = propertyTagProvider.newPropertyTagInTopicCollection(topic);
         } else {
             properties = topic.getProperties();
         }
         final List<PropertyTagInTopicWrapper> propertyItems = topic.getProperties().getItems();
         boolean cspPropertyFound = false;
         for (final PropertyTagInTopicWrapper property : propertyItems) {
             // Update the CSP Property Tag if it exists, otherwise add a new one
             if (property.getId().equals(CSConstants.CSP_PROPERTY_ID)) {
                 cspPropertyFound = true;
 
                 // Remove the current property
                 properties.remove(property);
 
                 // Add the updated one
                 property.setValue(specTopic.getUniqueId());
                 properties.addUpdateItem(property);
             }
         }
 
         if (!cspPropertyFound) {
             final PropertyTagWrapper cspPropertyTag = propertyTagProvider.getPropertyTag(CSConstants.CSP_PROPERTY_ID);
             final PropertyTagInTopicWrapper cspProperty = propertyTagProvider.newPropertyTagInTopic(cspPropertyTag, topic);
             cspProperty.setValue(specTopic.getUniqueId());
             cspProperty.setId(CSConstants.CSP_PROPERTY_ID);
             properties.addNewItem(cspProperty);
         }
 
         topic.setProperties(properties);
 
         return topic;
     }
 
     /**
      * Process a Spec Topic and add or remove tags defined by the spec topic.
      *
      * @param tagProvider
      * @param specTopic   The spec topic that represents the changes to the topic.
      * @param topic       The topic entity to be updated.
      * @return True if anything in the topic entity was changed, otherwise false.
      */
     protected boolean processTopicTags(final TagProvider tagProvider, final SpecTopic specTopic, final TopicWrapper topic) {
         boolean changed = false;
 
         // Get the tags for the topic
         final List<String> addTagNames = specTopic.getTags(true);
         final List<TagWrapper> addTags = new ArrayList<TagWrapper>();
         for (final String addTagName : addTagNames) {
             final TagWrapper tag = tagProvider.getTagByName(addTagName);
             if (tag != null) {
                 addTags.add(tag);
             }
         }
 
         // Check if the app should be shutdown
         if (isShuttingDown.get()) {
             return changed;
         }
 
         // Process the tags depending on the topic type
         if (specTopic.isTopicAClonedTopic()) {
             if (processClonedTopicTags(tagProvider, specTopic, topic, addTags)) changed = true;
         } else if (specTopic.isTopicAnExistingTopic() && specTopic.getRevision() == null) {
             if (processExistingTopicTags(tagProvider, topic, addTags)) changed = true;
         } else if (specTopic.isTopicANewTopic()) {
             if (processNewTopicTags(tagProvider, topic, addTags)) changed = true;
         }
 
         return changed;
     }
 
     /**
      * @param tagProvider
      * @param specTopic
      * @param topic
      * @param addTags
      * @return
      */
     private boolean processClonedTopicTags(final TagProvider tagProvider, final SpecTopic specTopic, final TopicWrapper topic,
             final List<TagWrapper> addTags) {
         // See if a new tag collection needs to be created
         if (addTags.size() > 0 && topic.getTags() == null) {
             topic.setTags(tagProvider.newTagCollection());
         }
 
         // Finds tags that aren't already in the database and adds them
         final List<TagWrapper> topicTagList = topic.getTags() == null ? new ArrayList<TagWrapper>() : topic.getTags().getItems();
 
         boolean changed = false;
         // Find tags that aren't already in the database and adds them
         for (final TagWrapper addTag : addTags) {
             boolean found = false;
             for (final TagWrapper topicTag : topicTagList) {
                 if (topicTag.getId().equals(addTag.getId())) {
                     found = true;
                     break;
                 }
             }
 
             if (!found) {
                 topic.getTags().addNewItem(addTag);
                 changed = true;
             }
         }
 
         // Check if the app should be shutdown
         if (isShuttingDown.get()) {
             return changed;
         }
 
         // Remove the database tags for - tags
         final List<String> removeTagNames = specTopic.getRemoveTags(true);
         final List<TagWrapper> removeTags = new ArrayList<TagWrapper>();
         for (final String removeTagName : removeTagNames) {
             final TagWrapper removeTag = tagProvider.getTagByName(removeTagName);
             if (removeTag != null) {
                 removeTags.add(removeTag);
             }
         }
 
         // Check if the app should be shutdown
         if (isShuttingDown.get()) {
             return changed;
         }
 
         // Iterate over the current tags and set any that should be removed. If they shouldn't be removed then add them as a normal list
         for (final TagWrapper topicTag : topicTagList) {
             boolean found = false;
             for (final TagWrapper removeTag : removeTags) {
                 if (topicTag.getId().equals(removeTag.getId())) {
                     found = true;
                 }
             }
 
             if (found) {
                 // Remove any current settings for the tag
                 topic.getTags().remove(topicTag);
                 // Set the tag to be removed from the database
                 topic.getTags().addRemoveItem(topicTag);
                 changed = true;
             }
         }
 
         return changed;
     }
 
     /**
      * @param tagProvider
      * @param topic
      * @param addTags
      * @return
      */
     private boolean processExistingTopicTags(final TagProvider tagProvider, final TopicWrapper topic, final List<TagWrapper> addTags) {
         boolean changed = false;
         // Finds tags that aren't already in the database and adds them
         final List<TagWrapper> topicTagList = topic.getTags() == null ? new ArrayList<TagWrapper>() : topic.getTags().getItems();
 
         // Copy the current tags into the new tag list
         final CollectionWrapper<TagWrapper> updatedTagCollection = tagProvider.newTagCollection();
         for (final TagWrapper topicTag : topicTagList) {
             updatedTagCollection.addItem(topicTag);
         }
 
         // Add the new tags to the updated tag list if they don't already exist
         for (final TagWrapper addTag : addTags) {
             boolean found = false;
             for (final TagWrapper topicTag : topicTagList) {
                 if (topicTag.getId().equals(addTag.getId())) {
                     found = true;
                     break;
                 }
             }
             if (!found) {
                 updatedTagCollection.addNewItem(addTag);
                 changed = true;
             }
         }
 
         // If something has changed then set the new updated list for the topic
         if (changed) {
             topic.setTags(updatedTagCollection);
         }
 
         return changed;
     }
 
     /**
      * @param tagProvider
      * @param topic
      * @param addTags
      * @return
      */
     private boolean processNewTopicTags(final TagProvider tagProvider, final TopicWrapper topic, final List<TagWrapper> addTags) {
         boolean changed = false;
 
         // See if a new tag collection needs to be created
         if (addTags.size() > 0 && topic.getTags() == null) {
             topic.setTags(tagProvider.newTagCollection());
         }
 
         // Save the tags
         for (final TagWrapper addTag : addTags) {
             topic.getTags().addNewItem(addTag);
             changed = true;
         }
 
         return changed;
     }
 
     /**
      * Processes a Spec Topic and adds the assigned writer for the topic it represents.
      *
      * @param tagProvider
      * @param specTopic   The spec topic object that contains the assigned writer.
      * @param topic       The topic entity to be updated.
      * @return True if anything in the topic entity was changed, otherwise false.
      */
     protected void processAssignedWriter(final TagProvider tagProvider, final SpecTopic specTopic, final TopicWrapper topic) {
         // See if a new tag collection needs to be created
         if (topic.getTags() == null) {
             topic.setTags(tagProvider.newTagCollection());
         }
 
         // Set the assigned writer (Tag Table)
         final TagWrapper writerTag = tagProvider.getTagByName(specTopic.getAssignedWriter(true));
         // Save a new assigned writer
         topic.getTags().addNewItem(writerTag);
         // Some providers need the collection to be set to set flags for saving
         topic.setTags(topic.getTags());
     }
 
     /**
      * Processes a Spec Topic and adds any new Source Urls to the topic it represents.
      *
      * @param topicSourceURLProvider
      * @param specTopic              The spec topic object that contains the urls to add.
      * @param topic                  The topic entity to be updated.
      * @return True if anything in the topic entity was changed, otherwise false.
      */
     protected boolean processTopicSourceUrls(final TopicSourceURLProvider topicSourceURLProvider, final SpecTopic specTopic,
             final TopicWrapper topic) {
         boolean changed = false;
         // Save the new Source Urls
         final List<String> urls = specTopic.getSourceUrls(true);
 
         if (urls != null && !urls.isEmpty()) {
             final CollectionWrapper<TopicSourceURLWrapper> sourceUrls = topic.getSourceURLs() == null ? topicSourceURLProvider
                     .newTopicSourceURLCollection(
                     topic) : topic.getSourceURLs();
 
             // Iterate over the spec topic urls and add them
             for (final String url : urls) {
                 final TopicSourceURLWrapper sourceUrl = topicSourceURLProvider.newTopicSourceURL(topic);
                 sourceUrl.setUrl(url);
 
                 // Get the Source URL title from the URL
                 try {
                     final Document doc = Jsoup.connect(url).get();
                     sourceUrl.setTitle(doc.title());
                 } catch (Exception e) {
                     // Do nothing if the HTML couldn't be parsed successfully.
                 }
 
                 sourceUrls.addNewItem(sourceUrl);
             }
 
             topic.setSourceURLs(sourceUrls);
             changed = true;
         }
 
         return changed;
     }
 
     /**
      * Syncs all duplicated topics with their real topic counterpart in the content specification.
      *
      * @param specTopics A HashMap of the all the topics in the Content Specification. The key is the Topics ID.
      */
     protected void syncDuplicatedTopics(final List<SpecTopic> specTopics) {
         for (final SpecTopic topic : specTopics) {
             // Sync the normal duplicates first
             if (topic.isTopicADuplicateTopic()) {
                 final String id = topic.getId();
                 final String temp = "N" + id.substring(1);
                 SpecTopic cloneTopic = null;
                 for (final SpecTopic specTopic : specTopics) {
                     final String key = specTopic.getId();
                     if (key.equals(temp)) {
                         cloneTopic = specTopic;
                         break;
                     }
                 }
                 topic.setDBId(cloneTopic.getDBId());
             }
             // Sync the duplicate cloned topics
             else if (topic.isTopicAClonedDuplicateTopic()) {
                 final String id = topic.getId();
                 final String idType = id.substring(1);
                 SpecTopic cloneTopic = null;
                 for (final SpecTopic specTopic : specTopics) {
                     final String key = specTopic.getId();
                     if (key.endsWith(idType) && !key.endsWith(id)) {
                         cloneTopic = specTopic;
                         break;
                     }
                 }
                 topic.setDBId(cloneTopic.getDBId());
 
             }
         }
     }
 
     /**
      * Saves the Content Specification and all of the topics in the content specification
      *
      * @param providerFactory
      * @param contentSpec     The Content Specification to be saved.
      * @param edit            Whether the content specification is being edited or created.
      * @param username        The User who requested the Content Spec be saved.
      * @param logMessage
      * @return True if the topic saved successfully otherwise false.
      */
     protected boolean saveContentSpec(final DataProviderFactory providerFactory, final ContentSpec contentSpec, final boolean edit,
             final String username, final LogMessageWrapper logMessage) {
         final ContentSpecProvider contentSpecProvider = providerFactory.getProvider(ContentSpecProvider.class);
 
         try {
             // Create the new topic entities
             final List<SpecTopic> specTopics = contentSpec.getSpecTopics();
             for (final SpecTopic specTopic : specTopics) {
 
                 // Check if the app should be shutdown
                 if (isShuttingDown.get()) {
                     shutdown.set(true);
                     throw new ProcessingException("Shutdown Requested");
                 }
 
                 // Add topics to the TopicPool that need to be added or updated
                 if (specTopic.isTopicAClonedTopic() || specTopic.isTopicANewTopic()) {
                     try {
                         final TopicWrapper topic = createTopicEntity(providerFactory, specTopic);
                         if (topic != null) {
                             topics.addNewTopic(topic);
                         }
                     } catch (Exception e) {
                         throw new ProcessingException("Failed to create topic: " + specTopic.getId(), e);
                     }
                 } else if (specTopic.isTopicAnExistingTopic() && !specTopic.getTags(true).isEmpty() && specTopic.getRevision() == null) {
                     try {
                         final TopicWrapper topic = createTopicEntity(providerFactory, specTopic);
                         if (topic != null) {
                             topics.addUpdatedTopic(topic);
                         }
                     } catch (Exception e) {
                         throw new ProcessingException("Failed to create topic: " + specTopic.getId(), e);
                     }
                 }
             }
 
             // Check if the app should be shutdown
             if (isShuttingDown.get()) {
                 shutdown.set(true);
                 throw new ProcessingException("Shutdown Requested");
             }
 
             // From here on the main saving happens so this shouldn't be interrupted
 
             // Save the new topic entities
             if (!topics.savePool()) {
                 log.error(ProcessorConstants.ERROR_DATABASE_ERROR_MSG);
                 throw new ProcessingException("Failed to save the pool of topics.");
             }
 
             // Initialise the new and cloned topics using the populated topic pool
             for (final SpecTopic specTopic : specTopics) {
                 topics.initialiseFromPool(specTopic);
             }
 
             // Sync the Duplicated Topics (ID = X<Number>)
             syncDuplicatedTopics(specTopics);
 
             // Save the content spec
             mergeAndSaveContentSpec(contentSpec, providerFactory, !edit, username, logMessage);
         } catch (ProcessingException e) {
             if (providerFactory.isRollbackSupported()) {
                 providerFactory.rollback();
             } else {
                 // Clean up the data that was created
                 if (contentSpec.getId() != null && !edit) {
                     try {
                         contentSpecProvider.deleteContentSpec(contentSpec.getId());
                     } catch (Exception e1) {
                         log.error("Unable to clean up the Content Specification from the database.", e);
                     }
                 }
                 if (topics.isInitialised()) topics.rollbackPool();
             }
             log.error(ProcessorConstants.ERROR_PROCESSING_ERROR_MSG);
             return false;
         } catch (Exception e) {
             LOG.error("", e);
             if (providerFactory.isRollbackSupported()) {
                 providerFactory.rollback();
             } else {
                 // Clean up the data that was created
                 if (contentSpec.getId() != null && !edit) {
                     try {
                         contentSpecProvider.deleteContentSpec(contentSpec.getId());
                     } catch (Exception e1) {
                         log.error("Unable to clean up the Content Specification from the database.", e);
                     }
                 }
                 if (topics.isInitialised()) topics.rollbackPool();
             }
             log.debug("", e);
             return false;
         }
         return true;
     }
 
     /**
      * Merges a Content Spec object with it's Database counterpart if one exists, otherwise it will create a new Database Entity if
      * create is set to true.
      *
      * @param contentSpec     The ContentSpec object to merge with the database entity.
      * @param providerFactory
      * @param create          If a new Content Spec Entity should be created if one doesn't exist.
      * @param username        The user who requested the save.
      * @param logMessage
      * @throws Exception Thrown if a problem occurs saving to the database.
      */
     protected void mergeAndSaveContentSpec(final ContentSpec contentSpec, final DataProviderFactory providerFactory, boolean create,
             final String username, final LogMessageWrapper logMessage) throws Exception {
         // Get the providers
         final ContentSpecProvider contentSpecProvider = providerFactory.getProvider(ContentSpecProvider.class);
         final PropertyTagProvider propertyTagProvider = providerFactory.getProvider(PropertyTagProvider.class);
 
         // Create the temporary entity to store changes in and load the real entity if it exists.
         ContentSpecWrapper contentSpecEntity = null;
         if (contentSpec.getId() != null) {
             contentSpecEntity = contentSpecProvider.getContentSpec(contentSpec.getId());
         } else if (create) {
             contentSpecEntity = contentSpecProvider.newContentSpec();
 
             // setup the basic values
             contentSpecEntity.setLocale(CommonConstants.DEFAULT_LOCALE);
 
             if (username != null) {
                 // Add the added by property tag
                 final UpdateableCollectionWrapper<PropertyTagInContentSpecWrapper> propertyTagCollection = propertyTagProvider
                         .newPropertyTagInContentSpecCollection(
                         contentSpecEntity);
 
                 // Create the new property tag
                 final PropertyTagWrapper addedByProperty = propertyTagProvider.getPropertyTag(CSConstants.ADDED_BY_PROPERTY_TAG_ID);
                 final PropertyTagInContentSpecWrapper propertyTag = propertyTagProvider.newPropertyTagInContentSpec(addedByProperty,
                         contentSpecEntity);
                 propertyTag.setValue(username);
                 propertyTagCollection.addNewItem(propertyTag);
 
                 // Set the updated properties for the content spec
                 contentSpecEntity.setProperties(propertyTagCollection);
             }
         } else {
             throw new ProcessingException("Unable to find the existing Content Specification");
         }
 
         // Check that the type still matches
         final int typeId = BookType.getBookTypeId(contentSpec.getBookType());
         if (contentSpecEntity.getType() == null || !contentSpecEntity.getType().equals(typeId)) {
             contentSpecEntity.setType(typeId);
         }
 
         final List<CSNodeWrapper> contentSpecNodes = getAllNodes(contentSpecEntity);
 
         // Create the content spec entity so that we have a valid reference to add nodes to
         if (create) {
             contentSpecEntity = contentSpecProvider.createContentSpec(contentSpecEntity);
         }
 
         // Check that the content spec was updated/created successfully.
         if (contentSpecEntity == null) {
             throw new ProcessingException("Saving the updated Content Specification failed.");
         }
         contentSpec.setId(contentSpecEntity.getId());
 
         // Get the list of transformable child nodes for processing
         final List<Node> nodes = getTransformableNodes(contentSpec.getNodes());
         nodes.addAll(getTransformableNodes(contentSpec.getBaseLevel().getChildNodes()));
 
         // Merge the base level and comments
         final Map<SpecNode, CSNodeWrapper> nodeMapping = new HashMap<SpecNode, CSNodeWrapper>();
         mergeChildren(nodes, contentSpecNodes, providerFactory, null, contentSpecEntity, nodeMapping);
 
         // Set the nodes that are no longer used for removal
         if (!contentSpecNodes.isEmpty()) {
             for (final CSNodeWrapper childNode : contentSpecNodes) {
                 final UpdateableCollectionWrapper<CSNodeWrapper> children;
                 if (childNode.getParent() == null) {
                     children = childNode.getContentSpec().getChildren();
                 } else {
                     children = childNode.getParent().getChildren();
                 }
                 children.remove(childNode);
                 children.addRemoveItem(childNode);
 
                 // Set the children so it'll be updated
                 if (childNode.getParent() == null) {
                     contentSpecEntity.setChildren(children);
                 } else {
                     childNode.getParent().setChildren(children);
                 }
             }
         }
 
         contentSpecProvider.updateContentSpec(contentSpecEntity);
 
         // Merge the relationships now all spec topics have a mapping to a node
         mergeTopicRelationships(nodeMapping, providerFactory);
 
         contentSpecProvider.updateContentSpec(contentSpecEntity, logMessage);
     }
 
     /**
      * Recursively find all of a Content Specs child nodes.
      *
      * @param contentSpec The content spec to get all the children nodes from.
      * @return A list of children nodes that exist for the content spec.
      */
     protected List<CSNodeWrapper> getAllNodes(final ContentSpecWrapper contentSpec) {
         final List<CSNodeWrapper> nodes = new LinkedList<CSNodeWrapper>();
         if (contentSpec.getChildren() != null) {
             final List<CSNodeWrapper> childrenNodes = contentSpec.getChildren().getItems();
             for (final CSNodeWrapper childNode : childrenNodes) {
                 nodes.add(childNode);
                 nodes.addAll(getAllChildrenNodes(childNode));
             }
         }
 
         return nodes;
     }
 
     /**
      * Recursively find all of a Content Spec Nodes children.
      *
      * @param csNode The node to get all the children nodes from.
      * @return A list of children nodes that exist for the node.
      */
     protected List<CSNodeWrapper> getAllChildrenNodes(final CSNodeWrapper csNode) {
         final List<CSNodeWrapper> nodes = new LinkedList<CSNodeWrapper>();
         if (csNode.getChildren() != null) {
             final List<CSNodeWrapper> childrenNodes = csNode.getChildren().getItems();
             for (final CSNodeWrapper childNode : childrenNodes) {
                 nodes.add(childNode);
                 nodes.addAll(getAllChildrenNodes(childNode));
             }
         }
 
         return nodes;
     }
 
     /**
      * Merges the children nodes of a Content Spec level into the Content Spec Entity level. If any new nodes have to be created,
      * the base of the node will be created on the server and then the rest after that.
      *
      * @param childrenNodes    The child nodes to be merged.
      * @param contentSpecNodes The list of content spec nodes that can be matched to.
      * @param providerFactory
      * @param parentNode       The parent entity node that the nodes should be assigned to.
      * @param contentSpec      The content spec entity that the nodes belong to.
      * @param nodeMapping      TODO
      * @throws Exception Thrown if an error occurs during saving new nodes.
      */
     protected void mergeChildren(final List<Node> childrenNodes, final List<CSNodeWrapper> contentSpecNodes,
             final DataProviderFactory providerFactory, final CSNodeWrapper parentNode, final ContentSpecWrapper contentSpec,
             final Map<SpecNode, CSNodeWrapper> nodeMapping) throws Exception {
         final CSNodeProvider nodeProvider = providerFactory.getProvider(CSNodeProvider.class);
         final List<CSNodeWrapper> processedNodes = new ArrayList<CSNodeWrapper>();
 
         UpdateableCollectionWrapper<CSNodeWrapper> levelChildren;
         if (parentNode == null) {
             levelChildren = contentSpec.getChildren();
 
             // Check that the level container isn't null
             if (levelChildren == null) {
                 levelChildren = nodeProvider.newCSNodeCollection();
             }
         } else {
             levelChildren = parentNode.getChildren();
 
             // Check that the level container isn't null
             if (levelChildren == null) {
                 levelChildren = nodeProvider.newCSNodeCollection();
             }
         }
 
         // Update or create all of the children nodes that exist in the content spec
         CSNodeWrapper prevNode = null;
         for (final Node childNode : childrenNodes) {
             if (!isTransformableNode(childNode)) {
                 continue;
             }
 
            LOG.debug("Processing: " + childNode.getText());

             // Find the Entity Node that matches the Content Spec node, if one exists
             CSNodeWrapper foundNodeEntity = findExistingNode(childNode, contentSpecNodes);
 
             // If the node was not found create a new one
             boolean changed = false;
             boolean newNode = false;
             if (foundNodeEntity == null) {
                LOG.debug("Creating a new node");
                 final CSNodeWrapper newCSNodeEntity = nodeProvider.newCSNode();
                 if (childNode instanceof SpecTopic) {
                     final SpecTopic specTopic = (SpecTopic) childNode;
                     mergeTopic(specTopic, newCSNodeEntity);
                     if (specTopic.getTopicType().equals(TopicType.LEVEL)) {
                         newCSNodeEntity.setNodeType(CommonConstants.CS_NODE_INNER_TOPIC);
                     } else {
                         newCSNodeEntity.setNodeType(CommonConstants.CS_NODE_TOPIC);
                     }
                 } else if (childNode instanceof Level) {
                     mergeLevel((Level) childNode, newCSNodeEntity);
                     newCSNodeEntity.setNodeType(((Level) childNode).getLevelType().getId());
                 } else if (childNode instanceof Comment) {
                     mergeComment((Comment) childNode, newCSNodeEntity);
                     newCSNodeEntity.setNodeType(CommonConstants.CS_NODE_COMMENT);
                 } else {
                     mergeMetaData((KeyValueNode<?>) childNode, newCSNodeEntity);
                     newCSNodeEntity.setNodeType(CommonConstants.CS_NODE_META_DATA);
                 }
 
                 // set the content spec for the new entity
                 foundNodeEntity = newCSNodeEntity;
                 newNode = true;
             } else {
                LOG.debug("Found existing node " + foundNodeEntity.getId());

                 if (childNode instanceof SpecTopic) {
                     final SpecTopic specTopic = (SpecTopic) childNode;
                     if (mergeTopic((SpecTopic) childNode, foundNodeEntity)) {
                         changed = true;
                     }
                     if (specTopic.getTopicType().equals(
                             TopicType.LEVEL) && (foundNodeEntity.getNodeType() == null || !foundNodeEntity.getNodeType().equals(
                             CommonConstants.CS_NODE_INNER_TOPIC))) {
                         foundNodeEntity.setNodeType(CommonConstants.CS_NODE_INNER_TOPIC);
                     } else if (foundNodeEntity.getNodeType() == null || !foundNodeEntity.getNodeType().equals(
                             CommonConstants.CS_NODE_TOPIC)) {
                         foundNodeEntity.setNodeType(CommonConstants.CS_NODE_TOPIC);
                     }
                 } else if (childNode instanceof Level) {
                     if (mergeLevel((Level) childNode, foundNodeEntity)) {
                         changed = true;
                     }
                 } else if (childNode instanceof Comment) {
                     if (mergeComment((Comment) childNode, foundNodeEntity)) {
                         changed = true;
                     }
                 } else {
                     if (mergeMetaData((KeyValueNode<?>) childNode, foundNodeEntity)) {
                         changed = true;
                     }
                 }
 
                 // If the node was found remove it from the list of content spec nodes, so it can no longer be matched
                 contentSpecNodes.remove(foundNodeEntity);
             }
             processedNodes.add(foundNodeEntity);
 
             // If the node is a level than merge the children nodes as well
             if (childNode instanceof Level) {
                 final Level level = (Level) childNode;
 
                 // Add the levels inner topic to the lsi tof children if one exists
                 final LinkedList<Node> children = level.getChildNodes();
                 if (level.getInnerTopic() != null) {
                     children.add(level.getInnerTopic());
                 }
 
                 mergeChildren(getTransformableNodes(children), contentSpecNodes, providerFactory, foundNodeEntity, contentSpec,
                         nodeMapping);
             }
 
             // setup the parent for the entity
             if (parentNode == null && foundNodeEntity.getParent() != null) {
                 foundNodeEntity.setParent(null);
                 changed = true;
             } else if (parentNode != null && (foundNodeEntity.getParent() == null || !foundNodeEntity.getParent().getId().equals(
                     parentNode.getId()))) {
                 foundNodeEntity.setParent(parentNode);
                 changed = true;
             }
 
             // Set up the next node relationship for the previous node
             if (!foundNodeEntity.getNodeType().equals(CommonConstants.CS_NODE_INNER_TOPIC) && prevNode != null &&
                     (prevNode.getNextNode() == null || (prevNode.getNextNode() != null && foundNodeEntity.getId() == null) || (prevNode
                             .getNextNode() != null && !prevNode.getNextNode().getId().equals(
                             foundNodeEntity.getId())))) {
                 prevNode.setNextNode(foundNodeEntity);
                 changed = true;
 
                 // Add the previous node to the updated collection if it's not already there
                 levelChildren.remove(prevNode);
                 if (prevNode.getId() == null) {
                     levelChildren.addNewItem(prevNode);
                 } else {
                     levelChildren.addUpdateItem(prevNode);
                 }
             }
 
             // The node has been updated or created so update it's state
             if (changed || newNode) {
                 levelChildren.remove(foundNodeEntity);
                 if (newNode) {
                     levelChildren.addNewItem(foundNodeEntity);
                 } else {
                     levelChildren.addUpdateItem(foundNodeEntity);
                 }
             }
 
             // Add the node to the mapping of nodes to entity nodes
             if (childNode instanceof SpecNode) {
                 nodeMapping.put((SpecNode) childNode, foundNodeEntity);
             }
 
             // Set the previous node to the current node since processing is done
             if (!foundNodeEntity.getNodeType().equals(CommonConstants.CS_NODE_INNER_TOPIC)) {
                 prevNode = foundNodeEntity;
             }
         }
 
         // If there is a previous node then make sure it's next node id is null as it's the last in the linked list
         if (prevNode != null && prevNode.getNextNode() != null) {
             prevNode.setNextNode(null);
 
             // Add the previous node to the updated collection if it's not already there
             levelChildren.remove(prevNode);
             if (prevNode.getId() == null) {
                 levelChildren.addNewItem(prevNode);
             } else {
                 levelChildren.addUpdateItem(prevNode);
             }
         }
 
         // Set the children so it'll be updated
         if (parentNode == null) {
             contentSpec.setChildren(levelChildren);
         } else {
             parentNode.setChildren(levelChildren);
         }
     }
 
     /**
      * Finds the existing Entity Node that matches a ContentSpec node.
      *
      * @param childNode           The ContentSpec node to find a matching Entity node for.
      * @param entityChildrenNodes The entity child nodes to match from.
      * @return The matching entity if one exists, otherwise null.
      */
     protected CSNodeWrapper findExistingNode(final Node childNode, final List<CSNodeWrapper> entityChildrenNodes) {
         CSNodeWrapper foundNodeEntity = null;
         if (entityChildrenNodes != null && !entityChildrenNodes.isEmpty()) {
             for (final CSNodeWrapper nodeEntity : entityChildrenNodes) {
                 if (childNode instanceof SpecTopic && doesTopicMatch((SpecTopic) childNode, nodeEntity)) {
                     foundNodeEntity = nodeEntity;
                     break;
                 } else if (childNode instanceof Level && doesLevelMatch((Level) childNode, nodeEntity)) {
                     foundNodeEntity = nodeEntity;
                     break;
                 } else if (childNode instanceof Comment && doesCommentMatch((Comment) childNode, nodeEntity)) {
                     foundNodeEntity = nodeEntity;
                     break;
                 } else if (childNode instanceof KeyValueNode && doesMetaDataMatch((KeyValueNode<?>) childNode, nodeEntity)) {
                     foundNodeEntity = nodeEntity;
                     break;
                 }
             }
         }
 
         return foundNodeEntity;
     }
 
     /**
      * Merges a Content Specs meta data with a Content Spec Entities meta data
      *
      * @param metaData       The meta data object to be merged into a entity meta data object
      * @param metaDataEntity The meta data entity to merge with.
      * @return True if some value was changed, otherwise false.
      */
     protected boolean mergeMetaData(final KeyValueNode<?> metaData, final CSNodeWrapper metaDataEntity) {
         boolean changed = false;
 
         // Meta Data Value
         final Object value = metaData.getValue();
         if (metaDataEntity.getAdditionalText() == null || !metaDataEntity.getAdditionalText().equals(value.toString())) {
             metaDataEntity.setAdditionalText(value.toString());
             changed = true;
         }
 
         // Set the Topic details if the Meta Data is a Spec Topic
         if (value instanceof SpecTopic) {
             if (mergeTopic((SpecTopic) value, metaDataEntity)) {
                 changed = true;
             }
         }
 
         // Meta Data Title
         if (metaDataEntity.getTitle() == null || !metaDataEntity.getTitle().equals(metaData.getKey())) {
             metaDataEntity.setTitle(metaData.getKey());
             changed = true;
         }
 
         return changed;
     }
 
     /**
      * @param level
      * @param levelEntity
      * @return True if some value was changed, otherwise false.
      * @throws Exception
      */
     protected boolean mergeLevel(final Level level, final CSNodeWrapper levelEntity) throws Exception {
         boolean changed = false;
 
         // TITLE
         if (level.getTitle() != null && !level.getTitle().equals(levelEntity.getTitle())) {
             levelEntity.setTitle(level.getTitle());
             changed = true;
         }
 
         // TARGET ID
         if (level.getTargetId() != null && !level.getTargetId().equals(levelEntity.getTargetId())) {
             levelEntity.setTargetId(level.getTargetId());
             changed = true;
         } else if (level.getTargetId() == null && levelEntity.getTargetId() != null) {
             levelEntity.setTargetId(null);
             changed = true;
         }
 
         // CONDITION
         if (level.getConditionStatement() != null && !level.getConditionStatement().equals(levelEntity.getCondition())) {
             levelEntity.setCondition(level.getConditionStatement());
             changed = true;
         } else if (level.getConditionStatement() == null && levelEntity.getCondition() != null) {
             levelEntity.setCondition(null);
             changed = true;
         }
 
         return changed;
     }
 
     /**
      * @param specTopic
      * @param topicEntity
      * @return True if a some value was changed, otherwise false.
      */
     protected boolean mergeTopic(final SpecTopic specTopic, final CSNodeWrapper topicEntity) {
         boolean changed = false;
 
         // TITLE
         // No need to check if the topic title should be set to null, as it is a mandatory field
         if (specTopic.getTitle() != null && !specTopic.getTitle().equals(topicEntity.getTitle())) {
             topicEntity.setTitle(specTopic.getTitle());
             changed = true;
         }
 
         // TARGET ID
         if (!specTopic.isTargetIdAnInternalId()) {
             if (specTopic.getTargetId() != null && !specTopic.getTargetId().equals(topicEntity.getTargetId())) {
                 topicEntity.setTargetId(specTopic.getTargetId());
                 changed = true;
             } else if (specTopic.getTargetId() == null && topicEntity.getTargetId() != null) {
                 topicEntity.setTargetId(null);
                 changed = true;
             }
         }
 
         // CONDITION
         if (specTopic.getConditionStatement() != null && !specTopic.getConditionStatement().equals(topicEntity.getCondition())) {
             topicEntity.setCondition(specTopic.getConditionStatement());
             changed = true;
         } else if (specTopic.getConditionStatement() == null && topicEntity.getCondition() != null) {
             topicEntity.setCondition(null);
             changed = true;
         }
 
         // TOPIC ID
         // No need to check if the topic id should be set to null, as it is a mandatory field
         if (specTopic.getDBId() != null && !specTopic.getDBId().equals(topicEntity.getEntityId())) {
             topicEntity.setEntityId(specTopic.getDBId());
             changed = true;
         }
 
         // TOPIC REVISION
         if (specTopic.getRevision() != null && !specTopic.getRevision().equals(topicEntity.getEntityRevision())) {
             topicEntity.setEntityRevision(specTopic.getRevision());
             changed = true;
         } else if (specTopic.getRevision() == null && topicEntity.getEntityRevision() != null) {
             topicEntity.setEntityRevision(null);
             changed = true;
         }
 
         return changed;
     }
 
     /**
      * @param comment
      * @param commentEntity
      * @return
      */
     protected boolean mergeComment(final Comment comment, final CSNodeWrapper commentEntity) {
         if (commentEntity.getTitle() == null || !commentEntity.getTitle().equals(comment.getText())) {
             commentEntity.setTitle(comment.getText());
             return true;
         }
 
         return false;
     }
 
     /**
      * Merges the relationships for all Spec Topics into their counterpart Entity nodes.
      *
      * @param nodeMapping     The mapping of Spec Nodes to Entity nodes.
      * @param providerFactory
      */
     protected void mergeTopicRelationships(final Map<SpecNode, CSNodeWrapper> nodeMapping, final DataProviderFactory providerFactory) {
         final CSNodeProvider nodeProvider = providerFactory.getProvider(CSNodeProvider.class);
 
         for (final Map.Entry<SpecNode, CSNodeWrapper> nodes : nodeMapping.entrySet()) {
             // Only process spec topics
             if (!(nodes.getKey() instanceof SpecTopic)) continue;
 
             // Get the matching spec topic and entity
             final SpecTopic specTopic = (SpecTopic) nodes.getKey();
             final CSNodeWrapper topicEntity = nodes.getValue();
 
             // Check if the node or entity have any relationships, if not then the node doesn't need to be merged
             if (!specTopic.getRelationships().isEmpty() || topicEntity.getRelatedToNodes() != null && !topicEntity.getRelatedToNodes()
                     .isEmpty()) {
                 // merge the relationships from the spec topic to the entity
                 mergeTopicRelationship(nodeMapping, specTopic, topicEntity, nodeProvider);
             }
         }
     }
 
     /**
      * Merges the relationships from a Spec Topic into a Topic Node Entity.
      *
      * @param nodeMapping  The mapping of Spec Nodes to Entity nodes.
      * @param specTopic    The Spec Topic to get the relationships from.
      * @param topicEntity  The Entity to merge the relationships into.
      * @param nodeProvider The provider factory for getting new or existing nodes.
      */
     protected void mergeTopicRelationship(final Map<SpecNode, CSNodeWrapper> nodeMapping, final SpecTopic specTopic,
             final CSNodeWrapper topicEntity, final CSNodeProvider nodeProvider) {
         final UpdateableCollectionWrapper<CSRelatedNodeWrapper> updatedRelatedToNodes = nodeProvider.newCSRelatedNodeCollection();
         final List<CSRelatedNodeWrapper> processedRelationships = new ArrayList<CSRelatedNodeWrapper>();
 
         // Check to make sure that the spec topic has any relationships
         if (!specTopic.getRelationships().isEmpty()) {
             int relationshipSortCount = 1;
 
             for (final Relationship relationship : specTopic.getRelationships()) {
                 // All process relationships should not be stored
                 if (relationship instanceof ProcessRelationship) continue;
 
                 // See if the related node already exists
                 CSRelatedNodeWrapper foundRelatedNode = findExistingRelatedNode(relationship, topicEntity);
 
                 if (foundRelatedNode != null) {
                     // Found a node so update anything that might have changed
                     boolean updated = false;
                     if (foundRelatedNode.getRelationshipSort() != relationshipSortCount) {
                         foundRelatedNode.setRelationshipSort(relationshipSortCount);
                         updated = true;
                     }
 
                     // If the node was updated, set it's state in the collection to updated, otherwise just put it back in normally.
                     if (updated) {
                         updatedRelatedToNodes.addUpdateItem(foundRelatedNode);
                     } else {
                         updatedRelatedToNodes.addItem(foundRelatedNode);
                     }
 
                     // Add the related node to the list of processed nodes
                     processedRelationships.add(foundRelatedNode);
                 } else {
                     // No related node was found for the relationship so make a new one.
                     final CSNodeWrapper relatedNode;
                     if (relationship instanceof TargetRelationship) {
                         relatedNode = nodeMapping.get(((TargetRelationship) relationship).getSecondaryRelationship());
                     } else {
                         relatedNode = nodeMapping.get(((TopicRelationship) relationship).getSecondaryRelationship());
                     }
 
                     foundRelatedNode = nodeProvider.newCSRelatedNode(relatedNode);
                     foundRelatedNode.setRelationshipType(RelationshipType.getRelationshipTypeId(relationship.getType()));
                     foundRelatedNode.setRelationshipSort(relationshipSortCount);
 
                     updatedRelatedToNodes.addNewItem(foundRelatedNode);
                 }
 
                 // increment the sort counter
                 relationshipSortCount++;
             }
         }
 
         // Remove any existing relationships that are no longer valid
         final CollectionWrapper<CSRelatedNodeWrapper> topicRelatedNodes = topicEntity.getRelatedToNodes();
         if (topicRelatedNodes != null) {
             for (final CSRelatedNodeWrapper relatedNode : topicRelatedNodes.getItems()) {
                 // If the node was processed then ignore it, otherwise remove it
                 if (processedRelationships.contains(relatedNode)) {
                     continue;
                 } else {
                     updatedRelatedToNodes.addRemoveItem(relatedNode);
                 }
             }
         }
 
         // Check if anything was changed, if so then set the changes
         if (!updatedRelatedToNodes.getAddItems().isEmpty() || !updatedRelatedToNodes.getUpdateItems().isEmpty() || !updatedRelatedToNodes
                 .getRemoveItems().isEmpty()) {
             topicEntity.setRelatedToNodes(updatedRelatedToNodes);
 
             // Make sure the node is in the updated state
             if (topicEntity.getParent() == null) {
                 topicEntity.getContentSpec().getChildren().remove(topicEntity);
                 if (topicEntity.getId() == null) {
                     topicEntity.getContentSpec().getChildren().addNewItem(topicEntity);
                 } else {
                     topicEntity.getContentSpec().getChildren().addUpdateItem(topicEntity);
                 }
             } else {
                 final CSNodeWrapper parent = topicEntity.getParent();
                 parent.getChildren().remove(topicEntity);
                 if (topicEntity.getId() == null) {
                     parent.getChildren().addNewItem(topicEntity);
                 } else {
                     parent.getChildren().addUpdateItem(topicEntity);
                 }
             }
         }
     }
 
     /**
      * Finds an existing relationship for a topic.
      *
      * @param relationship The relationship to be found.
      * @param topicEntity  The topic to find the relationship from.
      * @return The related Entity, otherwise null if one can't be found.
      */
     protected CSRelatedNodeWrapper findExistingRelatedNode(final Relationship relationship, final CSNodeWrapper topicEntity) {
         final CollectionWrapper<CSRelatedNodeWrapper> topicRelatedNodes = topicEntity.getRelatedToNodes();
 
         CSRelatedNodeWrapper foundRelatedNode = null;
         if (topicRelatedNodes != null) {
             // Loop over the current nodes and see if any match
             for (final CSRelatedNodeWrapper relatedNode : topicRelatedNodes.getItems()) {
                 if (relationship instanceof TargetRelationship) {
                     if (doesRelationshipMatch((TargetRelationship) relationship, relatedNode)) {
                         foundRelatedNode = relatedNode;
                         break;
                     }
                 } else {
                     if (doesRelationshipMatch((TopicRelationship) relationship, relatedNode)) {
                         foundRelatedNode = relatedNode;
                         break;
                     }
                 }
             }
         }
 
         return foundRelatedNode;
     }
 
     /**
      * Gets a list of child nodes that can be transformed.
      *
      * @param childNodes The list of nodes to filter for translatable nodes.
      * @return A list of transformable nodes.
      */
     protected List<Node> getTransformableNodes(final List<Node> childNodes) {
         final List<Node> nodes = new LinkedList<Node>();
         for (final Node childNode : childNodes) {
             if (isTransformableNode(childNode)) {
                 nodes.add(childNode);
             }
         }
 
         return nodes;
     }
 
     /**
      * Checks to see if a node is a node that can be transformed and saved,
      *
      * @param childNode The node to be checked.
      * @return True if the node can be transformed, otherwise false.
      */
     protected boolean isTransformableNode(final Node childNode) {
         if (childNode instanceof KeyValueNode) {
             return !IGNORE_META_DATA.contains(((KeyValueNode) childNode).getKey());
         } else {
             return childNode instanceof SpecNode || childNode instanceof Comment || childNode instanceof Level;
         }
     }
 
     /**
      * Checks to see if a ContentSpec meta data matches a Content Spec Entity meta data.
      * TODO Deal with SpecTopic Meta Data
      *
      * @param metaData The ContentSpec meta data object.
      * @param node     The Content Spec Entity topic.
      * @return True if the meta data is determined to match otherwise false.
      */
     protected boolean doesMetaDataMatch(final KeyValueNode<?> metaData, final CSNodeWrapper node) {
         if (!node.getNodeType().equals(CommonConstants.CS_NODE_META_DATA)) return false;
 
         // Check if the key matches, if it does than the nodes match.
         return metaData.getKey().equals(node.getTitle());
     }
 
     /**
      * Checks to see if a ContentSpec level matches a Content Spec Entity level.
      *
      * @param level The ContentSpec level object.
      * @param node  The Content Spec Entity level.
      * @return True if the level is determined to match otherwise false.
      */
     protected boolean doesLevelMatch(final Level level, final CSNodeWrapper node) {
         if (node.getNodeType().equals(CommonConstants.CS_NODE_COMMENT) || node.getNodeType().equals(CommonConstants.CS_NODE_TOPIC) ||
                 node.getNodeType().equals(CommonConstants.CS_NODE_INNER_TOPIC)) return false;
 
         // If the unique id is not from the parser, than use the unique id to compare
         if (level.getUniqueId() != null && level.getUniqueId().matches("^\\d.*")) {
             return level.getUniqueId().equals(Integer.toString(node.getId()));
         } else {
             // Since a content spec doesn't contain the database ids for the nodes use what is available to see if the level matches
             if (node.getNodeType() != level.getLevelType().getId()) return false;
 
             // If the target ids match then the level should be the same
             if (level.getTargetId() != null && level.getTargetId() == node.getTargetId()) {
                 return true;
             }
 
             return level.getTitle().equals(node.getTitle());
         }
     }
 
     /**
      * Checks to see if a ContentSpec topic matches a Content Spec Entity topic.
      *
      * @param specTopic The ContentSpec topic object.
      * @param node      The Content Spec Entity topic.
      * @return True if the topic is determined to match otherwise false.
      */
     protected boolean doesTopicMatch(final SpecTopic specTopic, final CSNodeWrapper node) {
         if (!node.getNodeType().equals(CommonConstants.CS_NODE_TOPIC) && !node.getNodeType().equals(CommonConstants.CS_NODE_INNER_TOPIC))
             return false;
 
         // If the unique id is not from the parser, in which case it will start with a number than use the unique id to compare
         if (specTopic.getUniqueId() != null && specTopic.getUniqueId().matches("^\\d.*")) {
             return specTopic.getUniqueId().equals(Integer.toString(node.getId()));
         } else {
             // Check the parent has the same name
             if (specTopic.getParent() != null && node.getParent() != null && node.getParent() instanceof Level) {
                 if (!((Level) specTopic.getParent()).getTitle().equals(node.getParent().getTitle())) {
                     return false;
                 }
             }
 
             // Since a content spec doesn't contain the database ids for the nodes use what is available to see if the topics match
             return specTopic.getDBId().equals(node.getEntityId());
         }
     }
 
     /**
      * Checks to see if a ContentSpec topic relationship matches a Content Spec Entity topic.
      *
      * @param relationship The ContentSpec topic relationship object.
      * @param relatedNode  The related Content Spec Entity topic.
      * @return True if the topic is determined to match otherwise false.
      */
     protected boolean doesRelationshipMatch(final TopicRelationship relationship, final CSRelatedNodeWrapper relatedNode) {
         // If the relationship is a TopicRelationship, then the related node must be a topic or its not a match
         if (!relatedNode.getNodeType().equals(CommonConstants.CS_NODE_TOPIC) && !relatedNode.getNodeType().equals(
                 CommonConstants.CS_NODE_INNER_TOPIC)) return false;
 
         // Check if the type matches first
         if (!RelationshipType.getRelationshipTypeId(relationship.getType()).equals(relatedNode.getRelationshipType())) return false;
 
         // If the unique id is not from the parser, in which case it will start with a number than use the unique id to compare
         if (relationship.getSecondaryRelationship().getUniqueId() != null && relationship.getSecondaryRelationship().getUniqueId().matches(
                 "^\\d.*")) {
             return relationship.getSecondaryRelationship().getUniqueId().equals(Integer.toString(relatedNode.getId()));
         } else {
             return relationship.getSecondaryRelationship().getDBId().equals(relatedNode.getEntityId());
         }
     }
 
     /**
      * Checks to see if a ContentSpec topic relationship matches a Content Spec Entity topic.
      *
      * @param relationship The ContentSpec topic relationship object.
      * @param relatedNode  The related Content Spec Entity topic.
      * @return True if the topic is determined to match otherwise false.
      */
     protected boolean doesRelationshipMatch(final TargetRelationship relationship, final CSRelatedNodeWrapper relatedNode) {
         // If the relationship node is a node that can't be related to (ie a comment or meta data), than the relationship cannot match
         if (relatedNode.getNodeType().equals(CommonConstants.CS_NODE_COMMENT) || relatedNode.getNodeType().equals(
                 CommonConstants.CS_NODE_META_DATA)) {
             return false;
         }
 
         // Check if the type matches first
         if (!RelationshipType.getRelationshipTypeId(relationship.getType()).equals(relatedNode.getRelationshipType())) return false;
 
         // If the unique id is not from the parser, in which case it will start with a number than use the unique id to compare
         if (relationship.getSecondaryRelationship().getUniqueId() != null && relationship.getSecondaryRelationship().getUniqueId().matches(
                 "^\\d.*")) {
             return relationship.getSecondaryRelationship().getUniqueId().equals(Integer.toString(relatedNode.getId()));
         } else if (relationship.getSecondaryRelationship() instanceof Level) {
             return ((Level) relationship.getSecondaryRelationship()).getTargetId().equals(relatedNode.getTargetId());
         } else if (relationship.getSecondaryRelationship() instanceof SpecTopic) {
             return ((SpecTopic) relationship.getSecondaryRelationship()).getTargetId().equals(relatedNode.getTargetId());
         } else {
             return false;
         }
     }
 
     /**
      * Checks to see if a ContentSpec comment matches a Content Spec Entity comment.
      *
      * @param comment The ContentSpec comment object.
      * @param node    The Content Spec Entity comment.
      * @return True if the comment is determined to match otherwise false.
      */
     protected boolean doesCommentMatch(final Comment comment, final CSNodeWrapper node) {
         if (!node.getNodeType().equals(CommonConstants.CS_NODE_COMMENT)) return false;
 
         // Check the parent has the same name
         if (comment.getParent() != null) {
             if (comment.getParent() instanceof ContentSpec) {
                 return node.getParent() == null;
             } else if (comment.getParent() instanceof Level && node.getParent() != null) {
                 final Level parent = ((Level) comment.getParent());
                 return parent.getTitle().equals(node.getParent().getTitle());
             } else {
                 return false;
             }
         }
 
         return true;
     }
 
     @Override
     public void shutdown() {
         isShuttingDown.set(true);
         if (validator != null) {
             validator.shutdown();
         }
     }
 
     @Override
     public boolean isShutdown() {
         return shutdown.get();
     }
 }
