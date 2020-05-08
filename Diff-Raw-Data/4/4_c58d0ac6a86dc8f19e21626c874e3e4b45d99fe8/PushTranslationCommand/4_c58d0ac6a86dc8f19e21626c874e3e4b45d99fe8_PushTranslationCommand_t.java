 package org.jboss.pressgang.ccms.contentspec.client.commands;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 
 import com.beust.jcommander.JCommander;
 import com.beust.jcommander.Parameter;
 import com.beust.jcommander.Parameters;
 import org.jboss.pressgang.ccms.contentspec.ContentSpec;
 import org.jboss.pressgang.ccms.contentspec.SpecTopic;
 import org.jboss.pressgang.ccms.contentspec.builder.constants.BuilderConstants;
 import org.jboss.pressgang.ccms.contentspec.client.commands.base.BaseCommandImpl;
 import org.jboss.pressgang.ccms.contentspec.client.config.ClientConfiguration;
 import org.jboss.pressgang.ccms.contentspec.client.config.ContentSpecConfiguration;
 import org.jboss.pressgang.ccms.contentspec.client.constants.Constants;
 import org.jboss.pressgang.ccms.contentspec.client.utils.ClientUtilities;
 import org.jboss.pressgang.ccms.contentspec.processor.ContentSpecParser;
 import org.jboss.pressgang.ccms.contentspec.processor.ContentSpecProcessor;
 import org.jboss.pressgang.ccms.contentspec.processor.structures.ProcessingOptions;
 import org.jboss.pressgang.ccms.contentspec.structures.StringToCSNodeCollection;
 import org.jboss.pressgang.ccms.contentspec.utils.CSTransformer;
 import org.jboss.pressgang.ccms.contentspec.utils.EntityUtilities;
 import org.jboss.pressgang.ccms.contentspec.utils.TranslationUtilities;
 import org.jboss.pressgang.ccms.contentspec.utils.logging.ErrorLoggerManager;
 import org.jboss.pressgang.ccms.provider.ContentSpecProvider;
 import org.jboss.pressgang.ccms.provider.DataProviderFactory;
 import org.jboss.pressgang.ccms.provider.TopicProvider;
 import org.jboss.pressgang.ccms.provider.TranslatedContentSpecProvider;
 import org.jboss.pressgang.ccms.provider.TranslatedTopicProvider;
 import org.jboss.pressgang.ccms.utils.common.DocBookUtilities;
 import org.jboss.pressgang.ccms.utils.common.HashUtilities;
 import org.jboss.pressgang.ccms.utils.common.XMLUtilities;
 import org.jboss.pressgang.ccms.utils.structures.Pair;
 import org.jboss.pressgang.ccms.utils.structures.StringToNodeCollection;
 import org.jboss.pressgang.ccms.wrapper.ContentSpecWrapper;
 import org.jboss.pressgang.ccms.wrapper.TopicWrapper;
 import org.jboss.pressgang.ccms.wrapper.TranslatedCSNodeWrapper;
 import org.jboss.pressgang.ccms.wrapper.TranslatedContentSpecWrapper;
 import org.jboss.pressgang.ccms.wrapper.TranslatedTopicWrapper;
 import org.jboss.pressgang.ccms.zanata.ZanataConstants;
 import org.jboss.pressgang.ccms.zanata.ZanataDetails;
 import org.jboss.pressgang.ccms.zanata.ZanataInterface;
 import org.w3c.dom.Document;
 import org.w3c.dom.Node;
 import org.zanata.common.ContentType;
 import org.zanata.common.LocaleId;
 import org.zanata.common.ResourceType;
 import org.zanata.rest.dto.resource.Resource;
 import org.zanata.rest.dto.resource.TextFlow;
 
 @Parameters(commandDescription = "Push a Content Specification and it's topics to Zanata for translation.")
 public class PushTranslationCommand extends BaseCommandImpl {
     @Parameter(metaVar = "[ID]")
     private List<Integer> ids = new ArrayList<Integer>();
 
     @Parameter(names = Constants.ZANATA_SERVER_LONG_PARAM,
             description = "The Zanata server to be associated with the Content Specification.")
     private String zanataUrl = null;
 
     @Parameter(names = Constants.ZANATA_PROJECT_LONG_PARAM,
             description = "The Zanata project name to be associated with the Content Specification.")
     private String zanataProject = null;
 
     @Parameter(names = Constants.ZANATA_PROJECT_VERSION_LONG_PARAM,
             description = "The Zanata project version to be associated with the Content Specification.")
     private String zanataVersion = null;
 
     @Parameter(names = Constants.CONTENT_SPEC_ONLY_LONG_PARAM, description = "Only push the the content specification to Zanata.")
     private Boolean contentSpecOnly = false;
 
     @Parameter(names = {Constants.YES_LONG_PARAM, Constants.YES_SHORT_PARAM},
             description = "Automatically answer \"yes\" to any questions.")
     private Boolean answerYes = false;
 
     private ContentSpecProcessor csp;
 
     public PushTranslationCommand(final JCommander parser, final ContentSpecConfiguration cspConfig,
             final ClientConfiguration clientConfig) {
         super(parser, cspConfig, clientConfig);
     }
 
     @Override
     public String getCommandName() {
         return Constants.PUSH_TRANSLATION_COMMAND_NAME;
     }
 
     public List<Integer> getIds() {
         return ids;
     }
 
     public void setIds(final List<Integer> ids) {
         this.ids = ids;
     }
 
     public String getZanataUrl() {
         return zanataUrl;
     }
 
     public void setZanataUrl(final String zanataUrl) {
         this.zanataUrl = zanataUrl;
     }
 
     public String getZanataProject() {
         return zanataProject;
     }
 
     public void setZanataProject(final String zanataProject) {
         this.zanataProject = zanataProject;
     }
 
     public String getZanataVersion() {
         return zanataVersion;
     }
 
     public void setZanataVersion(final String zanataVersion) {
         this.zanataVersion = zanataVersion;
     }
 
     public Boolean getAnswerYes() {
         return answerYes;
     }
 
     public void setAnswerYes(Boolean answerYes) {
         this.answerYes = answerYes;
     }
 
     public Boolean getContentSpecOnly() {
         return contentSpecOnly;
     }
 
     public void setContentSpecOnly(Boolean contentSpecOnly) {
         this.contentSpecOnly = contentSpecOnly;
     }
 
     @Override
     public boolean validateServerUrl() {
         setupZanataOptions();
         if (!super.validateServerUrl()) return false;
 
         final ZanataDetails zanataDetails = getCspConfig().getZanataDetails();
 
         // Print the zanata server url
         JCommander.getConsole().println(String.format(Constants.ZANATA_WEBSERVICE_MSG, zanataDetails.getServer()));
 
         // Test that the server address is valid
         if (!ClientUtilities.validateServerExists(zanataDetails.getServer())) {
             // Print a line to separate content
             JCommander.getConsole().println("");
 
             printErrorAndShutdown(Constants.EXIT_NO_SERVER, Constants.UNABLE_TO_FIND_SERVER_MSG, false);
         }
 
         return true;
     }
 
     /**
      * Sets the zanata options applied by the command line
      * to the options that were set via configuration files.
      */
     protected void setupZanataOptions() {
         // Set the zanata url
         if (getZanataUrl() != null) {
             // Find the zanata server if the url is a reference to the zanata server name
             for (final String serverName : getClientConfig().getZanataServers().keySet()) {
                 if (serverName.equals(getZanataUrl())) {
                     setZanataUrl(getClientConfig().getZanataServers().get(serverName).getUrl());
                     break;
                 }
             }
 
             getCspConfig().getZanataDetails().setServer(ClientUtilities.fixHostURL(getZanataUrl()));
         }
 
         // Set the zanata project
         if (getZanataProject() != null) {
             getCspConfig().getZanataDetails().setProject(getZanataProject());
         }
 
         // Set the zanata version
         if (getZanataVersion() != null) {
             getCspConfig().getZanataDetails().setVersion(getZanataVersion());
         }
     }
 
     protected boolean isValid() {
         final ZanataDetails zanataDetails = getCspConfig().getZanataDetails();
 
         // Check that we even have some zanata details.
         if (zanataDetails == null) return false;
 
         // Check that none of the fields are invalid.
         if (zanataDetails.getServer() == null || zanataDetails.getServer().isEmpty() || zanataDetails.getProject() == null ||
                 zanataDetails.getProject().isEmpty() || zanataDetails.getVersion() == null || zanataDetails.getVersion().isEmpty() ||
                 zanataDetails.getToken() == null || zanataDetails.getToken().isEmpty() || zanataDetails.getUsername() == null ||
                 zanataDetails.getUsername().isEmpty()) {
             return false;
         }
 
         // At this point the zanata details are valid, so save the details.
         System.setProperty(ZanataConstants.ZANATA_SERVER_PROPERTY, zanataDetails.getServer());
         System.setProperty(ZanataConstants.ZANATA_PROJECT_PROPERTY, zanataDetails.getProject());
         System.setProperty(ZanataConstants.ZANATA_PROJECT_VERSION_PROPERTY, zanataDetails.getVersion());
         System.setProperty(ZanataConstants.ZANATA_USERNAME_PROPERTY, zanataDetails.getUsername());
         System.setProperty(ZanataConstants.ZANATA_TOKEN_PROPERTY, zanataDetails.getToken());
 
         return true;
     }
 
     @Override
     public void process() {
         final ContentSpecProvider contentSpecProvider = getProviderFactory().getProvider(ContentSpecProvider.class);
         final TopicProvider topicProvider = getProviderFactory().getProvider(TopicProvider.class);
 
         // Load the ids and validate that one and only one exists
         ClientUtilities.prepareAndValidateIds(this, getCspConfig(), getIds());
 
         // Check that the zanata details are valid
         if (!isValid()) {
             printErrorAndShutdown(Constants.EXIT_CONFIG_ERROR, Constants.ERROR_PUSH_NO_ZANATA_DETAILS_MSG, false);
         }
 
         // Good point to check for a shutdown
         allowShutdownToContinueIfRequested();
 
         final ContentSpecWrapper contentSpecEntity = ClientUtilities.getContentSpecEntity(contentSpecProvider, ids.get(0), null);
 
         if (contentSpecEntity == null) {
             printErrorAndShutdown(Constants.EXIT_FAILURE, Constants.ERROR_NO_ID_FOUND_MSG, false);
         }
 
         // Check that the content spec isn't a failed one
         if (contentSpecEntity.getFailed() != null) {
             printErrorAndShutdown(Constants.EXIT_FAILURE, Constants.ERROR_INVALID_CONTENT_SPEC, false);
         }
 
         // Transform the content spec
         final ContentSpec contentSpec = CSTransformer.transform(contentSpecEntity, getProviderFactory());
 
         // Setup the processing options
         final ProcessingOptions processingOptions = new ProcessingOptions();
         processingOptions.setValidating(true);
         processingOptions.setIgnoreChecksum(true);
         processingOptions.setAllowNewTopics(false);
 
         // Validate and parse the Content Specification
         final ErrorLoggerManager loggerManager = new ErrorLoggerManager();
         csp = new ContentSpecProcessor(getProviderFactory(), loggerManager, processingOptions);
         boolean success = csp.processContentSpec(contentSpec, getUsername(), ContentSpecParser.ParsingMode.EITHER);
 
         // Print the error/warning messages
         JCommander.getConsole().println(loggerManager.generateLogs());
 
         // Check that everything validated fine
         if (!success) {
             shutdown(Constants.EXIT_TOPIC_INVALID);
         }
 
         // Good point to check for a shutdown
         allowShutdownToContinueIfRequested();
 
         // Initialise the topic wrappers in the spec topics
         initialiseSpecTopics(topicProvider, contentSpec);
 
         // Good point to check for a shutdown
         allowShutdownToContinueIfRequested();
 
         if (!pushToZanata(getProviderFactory(), contentSpec, contentSpecEntity)) {
             printErrorAndShutdown(Constants.EXIT_FAILURE, Constants.ERROR_ZANATA_PUSH_FAILED_MSG, false);
             shutdown(Constants.EXIT_FAILURE);
         } else {
             JCommander.getConsole().println(Constants.SUCCESSFUL_ZANATA_PUSH_MSG);
         }
     }
 
     @Override
     public boolean loadFromCSProcessorCfg() {
         return ids.size() == 0;
     }
 
     protected void initialiseSpecTopics(final TopicProvider topicProvider, final ContentSpec contentSpec) {
         final List<SpecTopic> specTopics = contentSpec.getSpecTopics();
         for (final SpecTopic specTopic : specTopics) {
             if (specTopic.getDBId() != null && specTopic.getDBId() > 0 && specTopic.getRevision() == null) {
                 specTopic.setTopic(topicProvider.getTopic(specTopic.getDBId()));
             } else if (specTopic.getDBId() != null && specTopic.getDBId() > 0 && specTopic.getRevision() != null) {
                 specTopic.setTopic(topicProvider.getTopic(specTopic.getDBId(), specTopic.getRevision()));
             }
         }
     }
 
     /**
      * Pushes a content spec and its topics to zanata.
      *
      * @param providerFactory
      * @param contentSpec
      * @param contentSpecEntity
      * @return True if the push was successful otherwise false.
      */
     protected boolean pushToZanata(final DataProviderFactory providerFactory, final ContentSpec contentSpec,
             final ContentSpecWrapper contentSpecEntity) {
         final Map<TopicWrapper, SpecTopic> topicToSpecTopic = new HashMap<TopicWrapper, SpecTopic>();
         boolean error = false;
         final ZanataInterface zanataInterface = new ZanataInterface(0.2);
 
         // Convert all the topics to DOM Documents first so we know if any are invalid
         final Map<Pair<Integer, Integer>, TopicWrapper> topics = new HashMap<Pair<Integer, Integer>, TopicWrapper>();
         final List<SpecTopic> specTopics = contentSpec.getSpecTopics();
         for (final SpecTopic specTopic : specTopics) {
             final TopicWrapper topic = (TopicWrapper) specTopic.getTopic();
             final Pair<Integer, Integer> topicId = new Pair<Integer, Integer>(topic.getId(), topic.getRevision());
 
             // Only process the topic if it hasn't already been added, since the same topic can exist twice
             if (!topics.containsKey(topicId)) {
                 topics.put(topicId, topic);
 
                 // Convert the XML String into a DOM object.
                 Document doc = null;
                 try {
                     doc = XMLUtilities.convertStringToDocument(topic.getXml());
                 } catch (Exception e) {
                     // Do Nothing as we handle the error below.
                 }
 
                 if (doc == null) {
                     JCommander.getConsole().println(
                             "ERROR: Topic ID " + topic.getId() + ", Revision " + topic.getRevision() + " does not have valid XML");
                     error = true;
                 } else {
                     specTopic.setXMLDocument(doc);
                     topicToSpecTopic.put(topic, specTopic);
                 }
             }
 
             // Good point to check for a shutdown
             allowShutdownToContinueIfRequested();
         }
 
         // Return if creating the documents failed
         if (error) {
             return false;
         }
 
         // Good point to check for a shutdown
         allowShutdownToContinueIfRequested();
 
         final float total = topics.size() + 1;
         float current = 0;
         final int showPercent = 5;
         int lastPercent = 0;
 
         final String answer;
         if (getAnswerYes()) {
             JCommander.getConsole().println("Pushing " + ((int) total) + " topics to zanata.");
             answer = "yes";
         } else {
             JCommander.getConsole().println("You are about to push " + ((int) total) + " topics to zanata. Continue? (Yes/No)");
             answer = JCommander.getConsole().readLine();
         }
 
         final List<String> messages = new ArrayList<String>();
 
         if (answer.equalsIgnoreCase("yes") || answer.equalsIgnoreCase("y")) {
             JCommander.getConsole().println("Starting to push topics to zanata...");
 
             // Upload the content specification to zanata first so we can reference the nodes when pushing topics
             final TranslatedContentSpecWrapper translatedContentSpec = pushContentSpecToZanata(providerFactory, contentSpecEntity,
                     zanataInterface, messages);
             if (translatedContentSpec == null) {
                 error = true;
             } else if (!getContentSpecOnly()) {
                 // Loop through each topic and upload it to zanata
                 for (final Entry<TopicWrapper, SpecTopic> topicEntry : topicToSpecTopic.entrySet()) {
                     ++current;
                     final int percent = Math.round(current / total * 100);
                     if (percent - lastPercent >= showPercent) {
                         lastPercent = percent;
                         JCommander.getConsole().println("\tPushing topics to zanata " + percent + "% Done");
                     }
 
                     final SpecTopic specTopic = topicEntry.getValue();
 
                     // Find the matching translated CSNode and if one can't be found then produce an error.
                     final TranslatedCSNodeWrapper translatedCSNode = findTopicTranslatedCSNode(translatedContentSpec, specTopic);
                     if (translatedCSNode == null) {
                         final TopicWrapper topic = (TopicWrapper) specTopic.getTopic();
                         messages.add(
                                 "Topic ID " + topic.getId() + ", Revision " + topic.getRevision() + " failed to be created in Zanata.");
                         error = true;
                     } else {
                         if (!pushTopicToZanata(providerFactory, specTopic, translatedCSNode, zanataInterface, messages)) {
                             error = true;
                         }
                     }
                 }
             }
         }
 
         // Print the info/error messages
         if (messages.size() > 0) {
             JCommander.getConsole().println("Output:");
             for (final String message : messages) {
                 JCommander.getConsole().println("\t" + message);
             }
         }
 
         return !error;
     }
 
     protected TranslatedCSNodeWrapper findTopicTranslatedCSNode(final TranslatedContentSpecWrapper translatedContentSpec,
             final SpecTopic specTopic) {
         final List<TranslatedCSNodeWrapper> translatedCSNodes = translatedContentSpec.getTranslatedNodes().getItems();
         for (final TranslatedCSNodeWrapper translatedCSNode : translatedCSNodes) {
             if (specTopic.getUniqueId() != null && specTopic.getUniqueId().equals(translatedCSNode.getNodeId().toString())) {
                 return translatedCSNode;
             }
         }
 
         return null;
     }
 
     /**
      * @param providerFactory
      * @param specTopic
      * @param zanataInterface
      * @param messages
      * @return True if the topic was pushed successful otherwise false.
      */
     protected boolean pushTopicToZanata(final DataProviderFactory providerFactory, final SpecTopic specTopic,
             final TranslatedCSNodeWrapper translatedCSNode, final ZanataInterface zanataInterface, final List<String> messages) {
         final TopicWrapper topic = (TopicWrapper) specTopic.getTopic();
         final Document doc = specTopic.getXMLDocument();
         boolean error = false;
 
         // Create the zanata id based on whether a condition has been specified or not
         final String zanataId = getTopicZanataId(specTopic, translatedCSNode);
 
         // Get the condition and see if a translated topic already exists.
         final String condition = specTopic.getConditionStatement(true);
        final boolean translatedTopicExists = EntityUtilities.getTranslatedTopicByTopicAndNodeId(providerFactory, topic.getId(),
                topic.getRevision(), condition == null ? null : translatedCSNode.getId(), topic.getLocale()) != null;
 
         // Check if the zanata document already exists, if it does than the topic can be ignored.
         final Resource zanataFile = zanataInterface.getZanataResource(zanataId);
         if (zanataFile == null) {
             // Process the conditions, if any exist, to remove any nodes that wouldn't be seen for the content spec.
             DocBookUtilities.processConditions(condition, doc, BuilderConstants.DEFAULT_CONDITION);
 
             // Create the document to be created in Zanata
             final Resource resource = new Resource();
 
             resource.setContentType(ContentType.TextPlain);
             resource.setLang(LocaleId.fromJavaName(topic.getLocale()));
             resource.setName(zanataId);
             resource.setRevision(1);
             resource.setType(ResourceType.FILE);
 
             // Get the translatable nodes
             final List<StringToNodeCollection> translatableStrings = XMLUtilities.getTranslatableStringsV2(doc, false);
 
             // Add the translatable nodes to the zanata document
             for (final StringToNodeCollection translatableStringData : translatableStrings) {
                 final String translatableString = translatableStringData.getTranslationString();
                 if (!translatableString.trim().isEmpty()) {
                     final TextFlow textFlow = new TextFlow();
                     textFlow.setContents(translatableString);
                     textFlow.setLang(LocaleId.fromJavaName(topic.getLocale()));
                     textFlow.setId(HashUtilities.generateMD5(translatableString));
                     textFlow.setRevision(1);
 
                     resource.getTextFlows().add(textFlow);
                 }
             }
 
             // Create the document in zanata and then in PressGang if the document was successfully created in Zanata.
             if (!zanataInterface.createFile(resource)) {
                 messages.add("Topic ID " + topic.getId() + ", Revision " + topic.getRevision() + " failed to be created in Zanata.");
                 error = true;
             } else if (!translatedTopicExists) {
                 createPressGangTranslatedTopic(providerFactory, topic, condition, translatedCSNode, messages);
             }
         } else if (!translatedTopicExists) {
             createPressGangTranslatedTopic(providerFactory, topic, condition, translatedCSNode, messages);
         } else {
             messages.add("Topic ID " + topic.getId() + ", Revision " + topic.getRevision() + " already exists - Skipping.");
         }
 
         return !error;
     }
 
     protected boolean createPressGangTranslatedTopic(final DataProviderFactory providerFactory, final TopicWrapper topic, String condition,
             final TranslatedCSNodeWrapper translatedCSNode, final List<String> messages) {
         final TranslatedTopicProvider translatedTopicProvider = providerFactory.getProvider(TranslatedTopicProvider.class);
 
         // Create the Translated Topic based on if it has a condition or not.
         final TranslatedTopicWrapper translatedTopic;
         if (condition == null) {
             translatedTopic = TranslationUtilities.createTranslatedTopic(providerFactory, topic, null, null);
         } else {
             translatedTopic = TranslationUtilities.createTranslatedTopic(providerFactory, topic, translatedCSNode, condition);
         }
 
         // Save the Translated Topic
         try {
             if (translatedTopicProvider.createTranslatedTopic(translatedTopic) == null) {
                 messages.add("Topic ID " + topic.getId() + ", Revision " + topic.getRevision() + " failed to be created in " +
                         "PressGang.");
                 return false;
             }
         } catch (Exception e) {
             messages.add("Topic ID " + topic.getId() + ", Revision " + topic.getRevision() + " failed to be created in " +
                     "PressGang.");
             return false;
         }
 
         return true;
     }
 
     /**
      * Gets the Zanata ID for a topic based on whether or not the topic has any conditional text.
      *
      * @param specTopic        The topic to create the Zanata ID for.
      * @param translatedCSNode
      * @return The unique Zanata ID that can be used to create a document in Zanata.
      */
     protected String getTopicZanataId(final SpecTopic specTopic, final TranslatedCSNodeWrapper translatedCSNode) {
         final TopicWrapper topic = (TopicWrapper) specTopic.getTopic();
         Map<Node, List<String>> conditionNodes = DocBookUtilities.getConditionNodes(specTopic.getXMLDocument());
 
         // Create the zanata id based on whether a condition has been specified or not
         final String zanataId;
         if (specTopic.getConditionStatement(true) != null && !conditionNodes.isEmpty()) {
             zanataId = topic.getId() + "-" + topic.getRevision() + "-" + translatedCSNode.getId();
         } else {
             zanataId = topic.getId() + "-" + topic.getRevision();
         }
 
         return zanataId;
     }
 
     /**
      * @param providerFactory
      * @param contentSpecEntity
      * @param zanataInterface
      * @param messages
      * @return
      */
     protected TranslatedContentSpecWrapper pushContentSpecToZanata(final DataProviderFactory providerFactory,
             final ContentSpecWrapper contentSpecEntity, final ZanataInterface zanataInterface, final List<String> messages) {
         final String zanataId = "CS" + contentSpecEntity.getId() + "-" + contentSpecEntity.getRevision();
         final Resource zanataFile = zanataInterface.getZanataResource(zanataId);
         TranslatedContentSpecWrapper translatedContentSpec = EntityUtilities.getTranslatedContentSpecById(providerFactory,
                 contentSpecEntity.getId(), contentSpecEntity.getRevision());
 
         if (zanataFile == null) {
             final Resource resource = new Resource();
 
             resource.setContentType(ContentType.TextPlain);
             resource.setLang(LocaleId.fromJavaName(contentSpecEntity.getLocale()));
             resource.setName(zanataId);
             resource.setRevision(1);
             resource.setType(ResourceType.FILE);
 
             final List<StringToCSNodeCollection> translatableStrings = TranslationUtilities.getTranslatableStrings(contentSpecEntity,
                     false);
 
             for (final StringToCSNodeCollection translatableStringData : translatableStrings) {
                 final String translatableString = translatableStringData.getTranslationString();
                 if (!translatableString.trim().isEmpty()) {
                     final TextFlow textFlow = new TextFlow();
                     textFlow.setContents(translatableString);
                     textFlow.setLang(LocaleId.fromJavaName(contentSpecEntity.getLocale()));
                     textFlow.setId(HashUtilities.generateMD5(translatableString));
                     textFlow.setRevision(1);
 
                     resource.getTextFlows().add(textFlow);
                 }
             }
 
             // Create the document in Zanata
             if (!zanataInterface.createFile(resource)) {
                 messages.add("Content Spec ID " + contentSpecEntity.getId() + ", Revision " + contentSpecEntity.getRevision() + " " +
                         "failed to be created in Zanata.");
                 return null;
             } else if (translatedContentSpec == null) {
                 return createPressGangTranslatedContentSpec(providerFactory, contentSpecEntity, messages);
             }
         } else if (translatedContentSpec == null) {
             return createPressGangTranslatedContentSpec(providerFactory, contentSpecEntity, messages);
         } else {
             messages.add("Content Spec ID " + contentSpecEntity.getId() + ", Revision " + contentSpecEntity.getRevision() + " already " +
                     "exists - Skipping.");
         }
 
         return translatedContentSpec;
     }
 
     protected TranslatedContentSpecWrapper createPressGangTranslatedContentSpec(final DataProviderFactory providerFactory,
             final ContentSpecWrapper contentSpecEntity, final List<String> messages) {
         final TranslatedContentSpecProvider translatedContentSpecProvider = providerFactory.getProvider(
                 TranslatedContentSpecProvider.class);
 
         // Create the Translated Content Spec and it's nodes
         final TranslatedContentSpecWrapper newTranslatedContentSpec = TranslationUtilities.createTranslatedContentSpec(providerFactory,
                 contentSpecEntity);
         try {
             // Save the translated content spec
             final TranslatedContentSpecWrapper translatedContentSpec = translatedContentSpecProvider.createTranslatedContentSpec(
                     newTranslatedContentSpec);
             if (translatedContentSpec == null) {
                 messages.add("Content Spec ID " + contentSpecEntity.getId() + ", " +
                         "Revision " + contentSpecEntity.getRevision() + " failed to be created in PressGang.");
                 return null;
             } else {
                 return translatedContentSpec;
             }
         } catch (Exception e) {
             messages.add("Content Spec ID " + contentSpecEntity.getId() + ", Revision " + contentSpecEntity.getRevision() + " " +
                     "failed to be created in PressGang.");
             return null;
         }
     }
 
     @Override
     public boolean requiresExternalConnection() {
         return true;
     }
 }
