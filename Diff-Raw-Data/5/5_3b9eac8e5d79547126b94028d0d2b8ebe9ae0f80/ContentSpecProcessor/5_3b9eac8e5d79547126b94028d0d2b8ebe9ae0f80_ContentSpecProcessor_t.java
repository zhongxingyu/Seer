 package com.redhat.contentspec.processor;
 
 import static org.jboss.pressgang.ccms.rest.v1.collections.base.RESTBaseCollectionItemV1.ADD_STATE;
 
 import java.util.*;
 import java.util.Map.Entry;
 import java.util.concurrent.atomic.AtomicBoolean;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.apache.log4j.Logger;
 import org.jboss.pressgang.ccms.contentspec.ContentSpec;
 import org.jboss.pressgang.ccms.contentspec.SpecTopic;
 import org.jboss.pressgang.ccms.contentspec.constants.CSConstants;
 import org.jboss.pressgang.ccms.contentspec.interfaces.ShutdownAbleApp;
 import org.jboss.pressgang.ccms.contentspec.rest.RESTManager;
 import org.jboss.pressgang.ccms.contentspec.rest.RESTReader;
 import org.jboss.pressgang.ccms.contentspec.rest.RESTWriter;
 import org.jboss.pressgang.ccms.contentspec.rest.utils.TopicPool;
 import org.jboss.pressgang.ccms.contentspec.utils.logging.ErrorLogger;
 import org.jboss.pressgang.ccms.contentspec.utils.logging.ErrorLoggerManager;
 import org.jboss.pressgang.ccms.rest.v1.collections.RESTTagCollectionV1;
 import org.jboss.pressgang.ccms.rest.v1.collections.RESTTopicSourceUrlCollectionV1;
 import org.jboss.pressgang.ccms.rest.v1.collections.items.RESTTopicCollectionItemV1;
 import org.jboss.pressgang.ccms.rest.v1.collections.items.RESTTopicSourceUrlCollectionItemV1;
 import org.jboss.pressgang.ccms.rest.v1.collections.join.RESTAssignedPropertyTagCollectionV1;
 import org.jboss.pressgang.ccms.rest.v1.components.ComponentTagV1;
 import org.jboss.pressgang.ccms.rest.v1.entities.RESTTagV1;
 import org.jboss.pressgang.ccms.rest.v1.entities.RESTTopicSourceUrlV1;
 import org.jboss.pressgang.ccms.rest.v1.entities.RESTTopicV1;
 import org.jboss.pressgang.ccms.rest.v1.entities.RESTUserV1;
 import org.jboss.pressgang.ccms.rest.v1.entities.join.RESTAssignedPropertyTagV1;
 import org.jboss.pressgang.ccms.rest.v1.entities.join.RESTCategoryInTagV1;
 import org.jboss.pressgang.ccms.utils.common.CollectionUtilities;
 import org.jboss.pressgang.ccms.utils.common.ExceptionUtilities;
 import org.jboss.pressgang.ccms.utils.constants.CommonConstants;
 import org.jboss.pressgang.ccms.utils.structures.Pair;
 
 import com.redhat.contentspec.processor.constants.ProcessorConstants;
 import com.redhat.contentspec.processor.exceptions.ProcessingException;
 import com.redhat.contentspec.processor.structures.ProcessingOptions;
 import com.redhat.contentspec.processor.utils.ProcessorUtilities;
 
 /**
  * A class to fully process a Content Specification. It first parses the data using a ContentSpecParser,
  * then validates the Content Specification using a ContentSpecValidator and lastly saves the data to the database.
  * It can also be configured to only validate the data and not save it.
  * 
  * @author lnewson
  */
 @SuppressWarnings("rawtypes")
 public class ContentSpecProcessor implements ShutdownAbleApp
 {
 	private final Logger LOG = Logger.getLogger("com.redhat.contentspec.processor.CustomContentSpecProcessor");
 	
 	private final ErrorLogger log;
 	private final ErrorLoggerManager elm;
 	
 	private final RESTManager dbManager;
 	private final RESTReader reader;
 	private final RESTWriter writer;
 	
 	private final ProcessingOptions processingOptions;
 	private final ContentSpecParser csp;
 	private ContentSpecValidator validator;
 	private final TopicPool topics;
 	private final AtomicBoolean isShuttingDown = new AtomicBoolean(false);
 	private final AtomicBoolean shutdown = new AtomicBoolean(false);
 	
 	/**
 	 * Constructor
 	 * 
 	 * @param restManager A DBManager object that manages the REST connection and the functions to read/write to the REST Interface.
 	 * @param elm An Error Logger Manager that will be used to store all the log messages in case they need to be accessed at a later stage.
 	 * @param processingOptions The set of options to use when processing.
 	 */
 	public ContentSpecProcessor(final RESTManager restManager, final ErrorLoggerManager elm, final ProcessingOptions processingOptions)
 	{
 		reader = restManager.getReader();
 		writer = restManager.getWriter();
 		log = elm.getLogger(ContentSpecProcessor.class);
 		this.elm = elm;
 		this.dbManager = restManager;
 		this.csp = new ContentSpecParser(elm, restManager);
 		this.topics = new TopicPool(restManager.getRESTClient());
 		this.processingOptions = processingOptions;
 	}
 	
 	/**
 	 * Gets the Content Specification Object for the content specification.
 	 * 
 	 * @return The ContentSpec object that's used to store the processed data.
 	 */
 	public ContentSpec getContentSpec()
 	{
 		return csp.getContentSpec();
 	}
 	
 	/**
 	 * Gets the Content Specification Topics inside of a content specification
 	 * 
 	 * @return The mapping of topics to their unique content specification ID's.
 	 */
 	public HashMap<String, SpecTopic> getSpecTopics()
 	{
 		return csp.getSpecTopics();
 	}
 	
 	/**
 	 * Process a content specification so that it is parsed, validated and saved.
 	 * 
 	 * @param contentSpec The Content Specification that is to be processed.
 	 * @param user The user who requested the process operation.
 	 * @param mode The mode to parse the content specification in.
 	 * @return True if everything was processed successfully otherwise false.
 	 * @throws Exception Any unexpected exception that occurred when processing.
 	 */
 	public boolean processContentSpec(final String contentSpec, final RESTUserV1 user, final ContentSpecParser.ParsingMode mode) throws Exception
 	{
 		return processContentSpec(contentSpec, user, mode, null);
 	}
 	
 	/**
 	 * Process a content specification so that it is parsed, validated and saved.
 	 * 
 	 * @param contentSpec The Content Specification that is to be processed.
 	 * @param user The user who requested the process operation.
 	 * @param mode The mode to parse the content specification in.
 	 * @param overrideLocale Override the default locale using this parameter.
 	 * @return True if everything was processed successfully otherwise false.
 	 * @throws Exception Any unexpected exception that occurred when processing.
 	 */
 	public boolean processContentSpec(final String contentSpec, final RESTUserV1 user, final ContentSpecParser.ParsingMode mode, final String overrideLocale) throws Exception
 	{
 		return processContentSpec(contentSpec, user, mode, overrideLocale, false);
 	}
 	
 	/**
 	 * Process a content specification so that it is parsed, validated and saved.
 	 * 
 	 * @param contentSpec The Content Specification that is to be processed.
 	 * @param user The user who requested the process operation.
 	 * @param mode The mode to parse the content specification in.
 	 * @param overrideLocale Override the default locale using this parameter.
 	 * @param addRevisions If revision numbers should be added to each SpecTopic in the output.
 	 * @return True if everything was processed successfully otherwise false.
 	 * @throws Exception Any unexpected exception that occurred when processing.
 	 */
 	@SuppressWarnings({ "unchecked" })
 	public boolean processContentSpec(final String contentSpec, final RESTUserV1 user, final ContentSpecParser.ParsingMode mode, final String overrideLocale, final boolean addRevisions) throws Exception
 	{
 		boolean editing = false;
 		
 		// Check if the app should be shutdown
 		if (isShuttingDown.get())
 		{
 			shutdown.set(true);
 			return false;
 		}
 		
 		LOG.info("Starting to parse...");
 		if (mode == ContentSpecParser.ParsingMode.EDITED) editing = true;
 		if (!csp.parse(contentSpec, user, mode, true))
 		{
 			log.error(ProcessorConstants.ERROR_INVALID_CS_MSG);
 			return false;
 		}
 		
 		// Change the locale if the overrideLocale isn't null
 		if (overrideLocale != null)
 		{
 			csp.getContentSpec().setLocale(overrideLocale);
 		}
 		
 		// Check if the app should be shutdown
 		if (isShuttingDown.get())
 		{
 			shutdown.set(true);
 			return false;
 		}
 		
 		// Validate the content specification before doing any rest calls
         LOG.info("Starting first validation pass...");
 		
 		// Validate the relationships
 		validator = new ContentSpecValidator<RESTTopicV1>(RESTTopicV1.class, elm, dbManager, processingOptions);
 		
 		if (!validator.preValidateRelationships(csp.getProcessedRelationships(), csp.getSpecTopics(), csp.getTargetLevels(), csp.getTargetTopics())
 		        || !validator.preValidateContentSpec(csp.getContentSpec(), csp.getSpecTopics()))
 		{
 			log.error(ProcessorConstants.ERROR_INVALID_CS_MSG);
 			return false;
 		}
 		
 		// Check if the app should be shutdown
         if (isShuttingDown.get())
         {
             shutdown.set(true);
             return false;
         }
 		
 		// Download all of the latest and/or revision topics
 		downloadAllTopics();
 		
 		// Check if the app should be shutdown
 		if (isShuttingDown.get())
 		{
 			shutdown.set(true);
 			return false;
 		}
 		
 		// Validate the content specification now that we have most of the data from the REST API
 		LOG.info("Starting second validation pass...");
 
 		if (!validator.postValidateContentSpec(csp.getContentSpec(), csp.getSpecTopics()))
 		{
 			log.error(ProcessorConstants.ERROR_INVALID_CS_MSG);
 			return false;
 		}
 		else
 		{
 			log.info(ProcessorConstants.INFO_VALID_CS_MSG);
 			
 			// If we aren't validating then save the content specification
 			if (!processingOptions.isValidating())
 			{
 				// Check if the app should be shutdown
 				if (isShuttingDown.get())
 				{
 					shutdown.set(true);
 					return false;
 				}
 				
 				LOG.info("Saving the Content Specification to the server...");
 				if (saveContentSpec(csp.getContentSpec(), csp.getSpecTopics(), editing))
 				{
 					log.info(ProcessorConstants.INFO_SUCCESSFUL_SAVE_MSG);
 				}
 				else
 				{
 					return false;
 				}
 			}
 		}
 		return true;
 	}
 	
 	/**
 	 * Download all the topics that are to be used during processing from the
 	 * parsed Content Specification.
 	 */
 	protected void downloadAllTopics()
 	{
 	    /* If we are updating the revisions and no processing revision is passed then
 	     * we can just get the latest version for all of the topics. Other wise we need to
 	     * get the topics one by one that specify a revision.
 	     */
 	    if (processingOptions.isUpdateRevisions() && processingOptions.getRevision() == null)
         {
 	        final List<Integer> topicIds = csp.getReferencedTopicIds();
 	        if (!topicIds.isEmpty()) {
     	        // Download the list of topics in one go to reduce I/O overhead
                 LOG.info("Attempting to download all the latest topics...");
                 reader.getTopicsByIds(topicIds, csp.getContentSpec().getLocale() != null && !csp.getContentSpec().getLocale().equals(CommonConstants.DEFAULT_LOCALE));
             }
         }
 	    else
 	    {
     	    final List<Integer> topicIds = csp.getReferencedLatestTopicIds();
             final List<Pair<Integer, Integer>> referencedRevisionTopicIds = csp.getReferencedRevisionTopicIds();
             
             // Check if a maximum revision was specified for processing
             if (processingOptions.getRevision() == null && !topicIds.isEmpty())
             {
                 // Download the list of topics in one go to reduce I/O overhead
                 LOG.info("Attempting to download all the latest topics...");
                 reader.getTopicsByIds(topicIds, csp.getContentSpec().getLocale() != null && !csp.getContentSpec().getLocale().equals(CommonConstants.DEFAULT_LOCALE));
             }
             else if (!topicIds.isEmpty())
             {
                 // Add to the list of referenced topic ids
                 for (final Integer topicId : topicIds)
                 {
                     referencedRevisionTopicIds.add(new Pair<Integer, Integer>(topicId, processingOptions.getRevision()));
                 }
             }
     
             if (!referencedRevisionTopicIds.isEmpty())
             {
                 downloadRevisionTopics(referencedRevisionTopicIds);
             }
 	    }
 	}
 	
 	/**
 	 * Download the Topics from the REST API that specify a revision.
 	 * 
 	 * @param referencedRevisionTopicIds The Set of topic ids and revision to download.
 	 */
 	protected void downloadRevisionTopics(final List<Pair<Integer, Integer>> referencedRevisionTopicIds)
 	{
 	    LOG.info("Attempting to download all the revision topics...");
 
         final int showPercent = 5;
         final float total = referencedRevisionTopicIds.size();
         float current = 0;
         int lastPercent = 0;
 
         final boolean expandTranslations = csp.getContentSpec().getLocale() != null && !csp.getContentSpec().getLocale().equals(CommonConstants.DEFAULT_LOCALE);
         
         for (final Pair<Integer, Integer> topicToRevision : referencedRevisionTopicIds)
         {
             // If we want to update the revisions then we should get the latest topic and not the revision
             if (processingOptions.isUpdateRevisions())
             {
                 reader.getTopicById(topicToRevision.getFirst(), processingOptions.getRevision(), expandTranslations);
             }
             else
             {
                 reader.getTopicById(topicToRevision.getFirst(), topicToRevision.getSecond(), expandTranslations);
             }
 
             ++current;
             final int percent = Math.round(current / total * 100);
             if (percent - lastPercent >= showPercent)
             {
                 lastPercent = percent;
                 LOG.info("\tDownloading revision topics " + percent + "% Done");
             }
         }
 	}
 	
 	/**
 	 * Creates an entity to be sent through the REST interface to create or update a DB entry.
 	 * 
 	 * @param specTopic The Content Specification Topic to create the topic entity from.
 	 * @return The new topic object if any changes where made otherwise null.
 	 * @throws Exception Any error that occurs when trying to build the new topic Entity.
 	 */
 	protected RESTTopicV1 createTopicEntity(final SpecTopic specTopic) throws Exception
 	{
 		if (isShuttingDown.get())
 		{
 			return null;
 		}
 		
 		boolean changed = false;
 		
 		try
 		{		
 			final RESTAssignedPropertyTagCollectionV1 properties = new RESTAssignedPropertyTagCollectionV1();
 			
 			RESTTopicV1 topic = null;
 			
 			// Create a Tag collection that will hold the tags for this topic entity
 			RESTTagCollectionV1 topicTags = new RESTTagCollectionV1();
 			
 			if (specTopic.isTopicANewTopic())
 			{					
 				// Create the topic entity.
 				topic = new RESTTopicV1();
 				
 				// Set the basics
 				topic.explicitSetTitle(specTopic.getTitle());
 				topic.explicitSetDescription(specTopic.getDescription(true));
 				topic.explicitSetXml("");
 				
 				// Write the type
 				final RESTTagV1 type = reader.getTypeByName(specTopic.getType());
 				if (type == null)
 				{
 					log.error(String.format(ProcessorConstants.ERROR_TYPE_NONEXIST_MSG, specTopic.getLineNumber(), specTopic.getText()));
 					return null;
 				}
 				topicTags.addNewItem(type);
 				
 				// Add the type to the topic
 				topicTags.addNewItem(type);
 				
 				// Create the unique ID for the property
 				final RESTAssignedPropertyTagV1 cspProperty = new RESTAssignedPropertyTagV1();
 	            cspProperty.explicitSetValue(Integer.toString(specTopic.getLineNumber()));
 	            cspProperty.setId(CSConstants.CSP_PROPERTY_ID);
 	            properties.addNewItem(cspProperty);
 				
 				// Since this is a new topic the data has already changed
 				changed = true;
 			}
 			else if (specTopic.isTopicAClonedTopic())
 			{
 				// Get the existing topic from the database
 				int clonedId = Integer.parseInt(specTopic.getId().substring(1));
 				final RESTTopicV1 originalTopic = reader.getTopicById(clonedId, null);
 				topic = originalTopic.clone(true);
 				
 				// Set the ID to null so a new ID will be created
 				topic.setId(null);
 				// Set other items to null that should be recreated
 				topic.setCreated(null);
 				topic.setLastModified(null);
 				// Set-up the configured parameters so that everything gets saved
 				topic.setConfiguredParameters(CollectionUtilities.toArrayList(RESTTopicV1.TAGS_NAME, RESTTopicV1.SOURCE_URLS_NAME, RESTTopicV1.INCOMING_NAME,
 						RESTTopicV1.OUTGOING_NAME, RESTTopicV1.PROPERTIES_NAME, RESTTopicV1.TITLE_NAME, RESTTopicV1.XML_NAME, RESTTopicV1.DESCRIPTION_NAME, RESTTopicV1.HTML_NAME));
 			
 				// Go through each collection and set the "addItem" attribute to true
 				for (final RESTTopicCollectionItemV1 incomingRelationship : topic.getIncomingRelationships().getItems())
 				{
 					incomingRelationship.setState(ADD_STATE);
 				}
 				
 				for (final RESTTopicCollectionItemV1 outgoingRelationshipItem : topic.getOutgoingRelationships().getItems())
 				{
 					outgoingRelationshipItem.setState(ADD_STATE);
 				}
 				
 				for (final RESTTopicSourceUrlCollectionItemV1 sourceUrlItem : topic.getSourceUrls_OTM().getItems())
 				{
 				    final RESTTopicSourceUrlV1 sourceUrl = sourceUrlItem.getItem();
 				    
 					sourceUrlItem.setState(ADD_STATE);
 					sourceUrl.setConfiguredParameters(CollectionUtilities.toArrayList(RESTTopicSourceUrlV1.TITLE_NAME, RESTTopicSourceUrlV1.URL_NAME, RESTTopicSourceUrlV1.DESCRIPTION_NAME));
 				}
 				
 				final List<RESTAssignedPropertyTagV1> propertieItems = topic.getProperties().returnItems();
 				boolean cspPropertyFound = false;
 				for (final RESTAssignedPropertyTagV1 property : propertieItems)
 				{
 					// Ignore the CSP Property ID as we will add a new one
 					if (!property.getId().equals(CSConstants.CSP_PROPERTY_ID))
 					{
 						properties.addNewItem(property);
 					}
 					else
 					{
 					    cspPropertyFound = true;
 					    
 					    property.explicitSetValue(Integer.toString(specTopic.getLineNumber()));
 					    properties.addUpdateItem(property);
 					}
 				}
 				
 				if (!cspPropertyFound)
 				{
 				    final RESTAssignedPropertyTagV1 cspProperty = new RESTAssignedPropertyTagV1();
 	                cspProperty.explicitSetValue(Integer.toString(specTopic.getLineNumber()));
 	                cspProperty.setId(CSConstants.CSP_PROPERTY_ID);
 	                properties.addNewItem(cspProperty);
 				}
 				
 				final List<RESTTagV1> tags = topic.getTags().returnItems();
 				for (final RESTTagV1 tag: tags)
 				{
 					topicTags.addNewItem(tag);
 				}
 				
 				// Since this is a new topic the data has already changed
 				changed = true;
 			}
 			else if (specTopic.isTopicAnExistingTopic())
 			{
 				final RESTTopicV1 originalTopic = reader.getTopicById(specTopic.getDBId(), null);
 				topic = originalTopic.clone(true);
 				
 				// Remove any existing property tags
 				final List<RESTAssignedPropertyTagV1> propertieItems = topic.getProperties().returnItems();
                 boolean cspPropertyFound = false;
                 for (final RESTAssignedPropertyTagV1 property : propertieItems)
                 {
 					// Remove the CSP Property ID as we will add a new one
 					if (property.getId().equals(CSConstants.CSP_PROPERTY_ID))
 					{
 					    cspPropertyFound = true;
                         
                         property.explicitSetValue(Integer.toString(specTopic.getLineNumber()));
 						properties.addUpdateItem(property);
 					}
 				}
 
                 if (!cspPropertyFound)
                 {
                     final RESTAssignedPropertyTagV1 cspProperty = new RESTAssignedPropertyTagV1();
                     cspProperty.explicitSetValue(Integer.toString(specTopic.getLineNumber()));
                     cspProperty.setId(CSConstants.CSP_PROPERTY_ID);
                     properties.addNewItem(cspProperty);
                 }
 			}
 			topic.explicitSetProperties(properties);
 			
 			// Check if the app should be shutdown
 			if (isShuttingDown.get())
 			{
 				shutdown.set(true);
 				return null;
 			}
 			
 			if (!specTopic.isTopicAnExistingTopic())
 			{
 				// Set the assigned writer (Tag Table)
 				final List<RESTTagV1> assignedWriterTags = reader.getTagsByName(specTopic.getAssignedWriter(true));
 				if (assignedWriterTags.size() != 1)
 				{
 					log.error(String.format(ProcessorConstants.ERROR_WRITER_NONEXIST_MSG, specTopic.getLineNumber(), specTopic.getText()));
 					return null;
 				}
 				final RESTTagV1 writerTag = assignedWriterTags.iterator().next();
 				// Save a new assigned writer
 				topicTags.addNewItem(writerTag);
 			}
 			
 			// Check if the app should be shutdown
 			if (isShuttingDown.get()) {
 				shutdown.set(true);
 				return null;
 			}
 			
 			// Get the tags for the topic
 			List<String> tagNames = specTopic.getTags(true);
 			final List<RESTTagV1> tags = new ArrayList<RESTTagV1>();
 			for (final String tagName: tagNames)
 			{
 				final List<RESTTagV1> tagList = reader.getTagsByName(tagName);
 				if (tagList.size() == 1)
 				{
 					tags.add(tagList.get(0));
 				}
 			}
 			final Map<RESTCategoryInTagV1, List<RESTTagV1>> mapping = ProcessorUtilities.getCategoryMappingFromTagList(tags);
 			
 			// Check if the app should be shutdown
 			if (isShuttingDown.get())
 			{
 				shutdown.set(true);
 				return null;
 			}
 			
 			// If the topic is a cloned topic then only save new tags/urls
 			if (specTopic.isTopicAClonedTopic())
 			{
 				// Save the new tags
 				// Find tags that aren't already in the database and adds them
 				final List<RESTTagV1> tttList = topic.getTags().returnItems();
 				for (final Entry<RESTCategoryInTagV1, List<RESTTagV1>> catEntry: mapping.entrySet())
 				{
 					for (final RESTTagV1 tag: catEntry.getValue()) 
 					{
 						boolean found = false;
 						for (final RESTTagV1 ttt: tttList) 
 						{
 							if (ttt.getId().equals(tag.getId()))
 							{
 								found = true;
 								break;
 							}
 						}
 						
 						if (!found)
 						{
 							topicTags.addNewItem(tag);
 						}
 					}
 				}
 				
 				// Check if the app should be shutdown
 				if (isShuttingDown.get()) {
 					shutdown.set(true);
 					return null;
 				}
 				
 				// Remove the database tags for - tags
 				tagNames = specTopic.getRemoveTags(true);
 				final List<RESTTagV1> removeTags = new ArrayList<RESTTagV1>();
 				for (final String tagName: tagNames)
 				{
 					final List<RESTTagV1> tagList = reader.getTagsByName(tagName);
 					if (tagList.size() == 1) {
 						tags.add(tagList.get(0));
 					}
 				}
 				
 				for (final RESTTagV1 ttt: tttList)
 				{
 					boolean found = false;
 					for (final RESTTagV1 tag: removeTags)
 					{
 						if (ttt.getId().equals(tag.getId())) {
 							found = true;
 						}
 					}
 					
 					if (found)
 					{
 						// Set the tag to be removed from the database
 						topicTags.addRemoveItem(ttt);
 					}
 					
 					// Remove the old writer tag as it will get replaced
 					if (ComponentTagV1.containedInCategory(ttt, CSConstants.WRITER_CATEGORY_ID))
 					{
 						topicTags.addRemoveItem(ttt);
 					}
 				}
 			}
 			else if (specTopic.isTopicAnExistingTopic() && specTopic.getRevision() == null)
 			{
 				// Finds tags that aren't already in the database and adds them
 				final List<RESTTagV1> tttList = topic.getTags().returnItems();
 				for (final Entry<RESTCategoryInTagV1, List<RESTTagV1>> cat: mapping.entrySet())
 				{					
 					for (final RESTTagV1 tag: cat.getValue())
 					{
 						boolean found = false;
 						for (final RESTTagV1 ttt: tttList)
 						{
 							if (ttt.getId().equals(tag.getId()))
 							{
 								found = true;
 								break;
 							}
 						}
 						if (!found)
 						{
 							topicTags.addNewItem(tag);
 						}
 					}
 				}
 			}
 			else
 			{
 				// Save the tags
 				for (final Entry<RESTCategoryInTagV1, List<RESTTagV1>> cat: mapping.entrySet())
 				{
 					for (final RESTTagV1 tag: cat.getValue())
 					{
 						topicTags.addNewItem(tag);
 					}
 				}
 			}
 			
 			// Check if the app should be shutdown
 			if (isShuttingDown.get())
 			{
 				shutdown.set(true);
 				return null;
 			}
 			
 			if (!specTopic.isTopicAnExistingTopic())
 			{
 				// Save the new Source Urls
 				final List<String> urls = specTopic.getSourceUrls();
 				
 				final RESTTopicSourceUrlCollectionV1 sourceUrls = topic.getSourceUrls_OTM() == null ? 
 						new RESTTopicSourceUrlCollectionV1() : topic.getSourceUrls_OTM();
 
 				for (final String url: urls)
 				{
 					final RESTTopicSourceUrlV1 sourceUrl = new RESTTopicSourceUrlV1();
					sourceUrl.explicitSetUrl(url);
 					sourceUrls.addNewItem(sourceUrl);
 				}
 				
 				if (sourceUrls.getItems() != null && !sourceUrls.getItems().isEmpty())
 				{
 					topic.explicitSetSourceUrls_OTM(sourceUrls);
 					changed = true;
 				}
 			}
 			
 			// Check if the app should be shutdown
 			if (isShuttingDown.get())
 			{
 				shutdown.set(true);
 				return null;
 			}
 			
 			if (topicTags.getItems() != null && !topicTags.getItems().isEmpty())
 			{
 				topic.explicitSetTags(topicTags);
 				changed = true;
 			}
 			
 			if (changed)
 			{
 				return topic;
 			}
 			else
 			{
 				return null;
 			}
 		}
 		catch (Exception e)
 		{
 			log.debug(e.getMessage());
 			log.debug(ExceptionUtilities.getStackTrace(e), 2);
 			throw e;
 		}
 	}
 	
 	/**
 	 * Syncs all duplicated topics with their real topic counterpart in the content specification.
 	 * 
 	 * @param specTopics A HashMap of the all the topics in the Content Specification. The key is the Topics ID.
 	 * @return True if the duplicated topics saved successfully otherwise false.
 	 */
 	protected void syncDuplicatedTopics(final HashMap<String, SpecTopic> specTopics)
 	{
 		for (final Entry<String, SpecTopic> entry : specTopics.entrySet())
 		{
 			final SpecTopic topic = entry.getValue();
 			// Sync the normal duplicates first
 			if (topic.isTopicADuplicateTopic())
 			{
 				String id = topic.getId();
 				String temp = "N" + id.substring(1);
 				SpecTopic cloneTopic = specTopics.get(temp);
 				topic.setDBId(cloneTopic.getDBId());
 			}
 			// Sync the duplicate cloned topics
 			else if (topic.isTopicAClonedDuplicateTopic())
 			{
 				final String id = topic.getId();
 				final String idType = id.substring(1);
 				SpecTopic cloneTopic = null;
 				for (final Entry<String, SpecTopic> cloneEntry : specTopics.entrySet())
 				{
 				    final String key = cloneEntry.getKey();
 					if (key.endsWith(idType) && !key.endsWith(id))
 					{
 						cloneTopic = cloneEntry.getValue();
 					}
 				}
 				topic.setDBId(cloneTopic.getDBId());
 				
 			}
 		}
 	}
 	
 	/**
 	 * Saves the Content Specification and all of the topics in the content specification
 	 * 
 	 * @param contentSpec The Content Specification to be saved.
 	 * @param specTopics A HashMap of the all the Content Specification Topics that exist in the Content Specification. The key is the Topics ID.
 	 * @param edit Whether the content specification is being edited or created.
 	 * @return True if the topic saved successfully otherwise false.
 	 */
 	public boolean saveContentSpec(final ContentSpec contentSpec, final HashMap<String, SpecTopic> specTopics, final boolean edit)
 	{
 		try
 		{
 			// Get the full text representation of the processed content spec
 			final StringBuilder fullText = new StringBuilder("");
 			for (final String line: contentSpec.getPreProcessedText())
 			{
 				fullText.append(line + "\n");
 			}
 			
 			// A new content specification
 			if (contentSpec.getId() == 0)
 			{
 				contentSpec.setId(writer.createContentSpec(contentSpec.getTitle(), fullText.toString(), contentSpec.getDtd(), contentSpec.getCreatedBy()));
 				if (contentSpec.getId() == 0)
 				{
 					log.error(ProcessorConstants.ERROR_DATABASE_ERROR_MSG);
 					throw new Exception("Failed to create the pre content specification.");
 				}
 			}
 			// An existing content specification
 			else
 			{
 				if (!writer.updateContentSpec(contentSpec.getId(), contentSpec.getTitle(), fullText.toString(), contentSpec.getDtd()))
 				{
 					log.error(ProcessorConstants.ERROR_DATABASE_ERROR_MSG);
 					throw new Exception("Failed to create the pre content specification.");
 				}
 			}
 			
 			// Create the new topic entities
 			for (final Entry<String, SpecTopic> entry : specTopics.entrySet())
 			{
 			    final String specTopicId = entry.getKey();
 			    
 				// Check if the app should be shutdown
 				if (isShuttingDown.get())
 				{
 					shutdown.set(true);
 					throw new Exception("Shutdown Requested");
 				}
 				
 				// Add topics to the TopicPool that need to be added or updated
 				final SpecTopic specTopic = entry.getValue();
 				if (specTopic.getId().matches("(" + CSConstants.NEW_TOPIC_ID_REGEX + "|" + CSConstants.CLONED_TOPIC_ID_REGEX + ")"))
 				{
 					try
 					{
 						final RESTTopicV1 topic = createTopicEntity(specTopic);
 						if (topic != null)
 						{
 							topics.addNewTopic(topic);
 						}
 					}
 					catch (Exception e)
 					{
 						throw new ProcessingException("Failed to create topic: " + specTopicId);	
 					}
 				}
 				else if (specTopic.isTopicAnExistingTopic() && !specTopic.getTags(true).isEmpty() && specTopic.getRevision() == null)
 				{
 					try
 					{
 						final RESTTopicV1 topic = createTopicEntity(specTopic);
 						if (topic != null)
 						{
 							topics.addUpdatedTopic(topic);
 						}
 					}
 					catch (Exception e)
 					{
 						throw new ProcessingException("Failed to create topic: " + specTopicId);	
 					}
 				}
 			}
 			
 			// Check if the app should be shutdown
 			if (isShuttingDown.get())
 			{
 				shutdown.set(true);
 				throw new Exception("Shutdown Requested");
 			}
 			
 			// From here on the main saving happens so this shouldn't be interrupted
 			
 			// Save the new topic entities
 			if (!topics.savePool())
 			{
 				log.error(ProcessorConstants.ERROR_DATABASE_ERROR_MSG);
 				throw new Exception("Failed to save the pool of topics.");
 			}
 			
 			// Initialise the new and cloned topics using the populated topic pool
 			for (final Entry<String, SpecTopic> key: specTopics.entrySet())
 			{
 				topics.initialiseFromPool(key.getValue());
 			}
 			
 			// Sync the Duplicated Topics (ID = X<Number>)
 			syncDuplicatedTopics(specTopics);
 			
 			// Create the post processed content spec
			final String postCS = ProcessorUtilities.generatePostContentSpec(contentSpec, specTopics);
 			if (postCS == null)
 			{
 				throw new ProcessingException("Failed to create the Post Content Specification.");
 			}
 			
 			// Validate that the content specification was processed correctly
 			if (!validatePostProcessedSpec(postCS))
 			{
 				throw new ProcessingException("Failed to create the Post Content Specification.");
 			}
 			
 			if (!writer.updatePostContentSpec(contentSpec.getId(), postCS))
 			{
 				log.error(ProcessorConstants.ERROR_DATABASE_ERROR_MSG);
 				throw new Exception("Failed to save the post content Specification");
 			}
 		}
 		catch (ProcessingException e)
 		{
 			// Clean up the data that was created
 			if (contentSpec.getId() != 0 && !edit) writer.deleteContentSpec(contentSpec.getId());
 			if (topics.isInitialised()) topics.rollbackPool();
 			log.error(String.format("%s\n%7s%s", ProcessorConstants.ERROR_PROCESSING_ERROR_MSG, "", e.getMessage()));
 		}
 		catch (Exception e)
 		{
 			// Clean up the data that was created
 			if (contentSpec.getId() != 0 && !edit) writer.deleteContentSpec(contentSpec.getId());
 			if (topics.isInitialised()) topics.rollbackPool();
 			log.debug(e.getMessage());
 			log.debug(ExceptionUtilities.getStackTrace(e), 2);
 			return false;
 		}
 		return true;
 	}
 	
 	/**
 	 * Checks a post processed content specification to ensure that no new, cloned or duplicated
 	 * topics exist in the content specification as they should have been resolved to 
 	 * existing topics.
 	 * 
 	 * @param postProcessedSpec The post processed content specification.
 	 * @return True if no invalid topics were found, otherwise false
 	 */
 	private boolean validatePostProcessedSpec(final String postProcessedSpec)
 	{
 		Pattern newTopicPattern = Pattern.compile("(#.*)?\\[[ ]*N[0-9]*[ ]*,.*?\\]");
 		Matcher matcher = newTopicPattern.matcher(postProcessedSpec);
 		
 		while (matcher.find())
 		{
 			final String match = matcher.group();
 			if (!match.contains("#")) return false;
 		}
 		
 		Pattern clonedTopicPattern = Pattern.compile("(#.*)?\\[[ ]*C[0-9]+.*?\\]");
 		matcher = clonedTopicPattern.matcher(postProcessedSpec);
 		
 		while (matcher.find())
 		{
 			final String match = matcher.group();
 			if (!match.contains("#")) return false;
 		}
 		
 		Pattern duplicateTopicPattern = Pattern.compile("(#.*)?\\[[ ]*X[0-9]+.*?\\]");
 		matcher = duplicateTopicPattern.matcher(postProcessedSpec);
 		
 		while (matcher.find())
 		{
 			final String match = matcher.group();
 			if (!match.contains("#")) return false;
 		}
 		
 		Pattern duplicateClonedTopicPattern = Pattern.compile("(#.*)?\\[[ ]*XC[0-9]+.*?\\]");
 		matcher = duplicateClonedTopicPattern.matcher(postProcessedSpec);
 		
 		while (matcher.find())
 		{
 			final String match = matcher.group();
 			if (!match.contains("#")) return false;
 		}
 		
 		return true;
 	}
 	
 	/**
 	 * Get the Content Specification Parser used in this
 	 * Processor.
 	 * 
 	 * @return The ContentSpecParser used within this processor.
 	 */
 	public ContentSpecParser getParser()
 	{
 		return csp;
 	}
 
 	@Override
 	public void shutdown()
 	{
 		isShuttingDown.set(true);
 		if (validator != null)
 		{
 			validator.shutdown();
 		}
 	}
 
 	@Override
 	public boolean isShutdown()
 	{
 		return shutdown.get();
 	}
 }
