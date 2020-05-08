 package org.jboss.pressgang.ccms.contentspec.client.commands;
 
 import static com.google.common.base.Strings.isNullOrEmpty;
 
 import java.io.File;
 import java.io.IOException;
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
 import org.jboss.pressgang.ccms.contentspec.client.config.ZanataServerConfiguration;
 import org.jboss.pressgang.ccms.contentspec.client.constants.Constants;
 import org.jboss.pressgang.ccms.contentspec.client.processor.ClientContentSpecProcessor;
 import org.jboss.pressgang.ccms.contentspec.client.utils.ClientUtilities;
 import org.jboss.pressgang.ccms.contentspec.processor.ContentSpecParser;
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
 import org.jboss.pressgang.ccms.zanata.ETagCache;
 import org.jboss.pressgang.ccms.zanata.ETagInterceptor;
 import org.jboss.pressgang.ccms.zanata.NotModifiedException;
 import org.jboss.pressgang.ccms.zanata.ZanataDetails;
 import org.jboss.pressgang.ccms.zanata.ZanataInterface;
 import org.w3c.dom.Document;
 import org.w3c.dom.Entity;
 import org.zanata.common.ContentType;
 import org.zanata.common.LocaleId;
 import org.zanata.common.ResourceType;
 import org.zanata.rest.dto.resource.Resource;
 import org.zanata.rest.dto.resource.TextFlow;
 
 @Parameters(resourceBundle = "commands", commandDescriptionKey = "PUSH_TRANSLATION")
 public class PushTranslationCommand extends BaseCommandImpl {
     @Parameter(metaVar = "[ID]")
     private List<Integer> ids = new ArrayList<Integer>();
 
     @Parameter(names = Constants.ZANATA_SERVER_LONG_PARAM, descriptionKey = "ZANATA_SERVER", metaVar = "<URL>")
     private String zanataUrl = null;
 
     @Parameter(names = Constants.ZANATA_PROJECT_LONG_PARAM, descriptionKey = "ZANATA_PROJECT", metaVar = "<PROJECT>")
     private String zanataProject = null;
 
     @Parameter(names = Constants.ZANATA_PROJECT_VERSION_LONG_PARAM, descriptionKey = "ZANATA_PROJECT_VERSION", metaVar = "<VERSION>")
     private String zanataVersion = null;
 
     @Parameter(names = Constants.CONTENT_SPEC_ONLY_LONG_PARAM, descriptionKey = "PUSH_TRANSLATION_CONTENT_SPEC_ONLY")
     private Boolean contentSpecOnly = false;
 
     @Parameter(names = {Constants.YES_LONG_PARAM, Constants.YES_SHORT_PARAM}, descriptionKey = "ANSWER_YES")
     private Boolean answerYes = false;
 
     @Parameter(names = Constants.DISABLE_SSL_CERT_CHECK, descriptionKey = "DISABLE_SSL_CERT_CHECK")
     private Boolean disableSSLCert = false;
 
     private ClientContentSpecProcessor csp;
     private final ETagCache eTagCache = new ETagCache();
 
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
 
     public Boolean getDisableSSLCert() {
         return disableSSLCert;
     }
 
     public void setDisableSSLCert(Boolean disableSSLCert) {
         this.disableSSLCert = disableSSLCert;
     }
 
     @Override
     public boolean validateServerUrl() {
         setupZanataOptions();
         if (!super.validateServerUrl()) return false;
 
         final ZanataDetails zanataDetails = getCspConfig().getZanataDetails();
 
         // Print the zanata server url
         JCommander.getConsole().println(ClientUtilities.getMessage("ZANATA_WEBSERVICE_MSG", zanataDetails.getServer()));
 
         // Test that the server address is valid
         if (!ClientUtilities.validateServerExists(zanataDetails.getServer(), getDisableSSLCert())) {
             // Print a line to separate content
             JCommander.getConsole().println("");
 
             printErrorAndShutdown(Constants.EXIT_NO_SERVER, ClientUtilities.getMessage("ERROR_UNABLE_TO_FIND_SERVER_MSG"), false);
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
             ZanataServerConfiguration zanataConfig = null;
             // Find the zanata server if the url is a reference to the zanata server name
             for (final String serverName : getClientConfig().getZanataServers().keySet()) {
                 if (serverName.equals(getZanataUrl())) {
                     zanataConfig = getClientConfig().getZanataServers().get(serverName);
                     setZanataUrl(zanataConfig.getUrl());
                     break;
                 }
             }
 
             getCspConfig().getZanataDetails().setServer(ClientUtilities.fixHostURL(getZanataUrl()));
             if (zanataConfig != null) {
                 getCspConfig().getZanataDetails().setToken(zanataConfig.getToken());
                 getCspConfig().getZanataDetails().setUsername(zanataConfig.getUsername());
             }
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
             printErrorAndShutdown(Constants.EXIT_CONFIG_ERROR, ClientUtilities.getMessage("ERROR_PUSH_NO_ZANATA_DETAILS_MSG"), false);
         }
 
         // Good point to check for a shutdown
         allowShutdownToContinueIfRequested();
 
         final ContentSpecWrapper contentSpecEntity = ClientUtilities.getContentSpecEntity(contentSpecProvider, ids.get(0), null);
 
         if (contentSpecEntity == null) {
             printErrorAndShutdown(Constants.EXIT_FAILURE, ClientUtilities.getMessage("ERROR_NO_ID_FOUND_MSG"), false);
         }
 
         // Check that the content spec isn't a failed one
         if (contentSpecEntity.getFailed() != null) {
             printErrorAndShutdown(Constants.EXIT_FAILURE, ClientUtilities.getMessage("ERROR_INVALID_CONTENT_SPEC_MSG"), false);
         }
 
         // Transform the content spec
         final ContentSpec contentSpec = CSTransformer.transform(contentSpecEntity, getProviderFactory(), INCLUDE_CHECKSUMS);
 
         // Setup the processing options
         final ProcessingOptions processingOptions = new ProcessingOptions();
         processingOptions.setValidating(true);
         processingOptions.setIgnoreChecksum(true);
         processingOptions.setAllowNewTopics(false);
 
         // Validate and parse the Content Specification
         final ErrorLoggerManager loggerManager = new ErrorLoggerManager();
         csp = new ClientContentSpecProcessor(getProviderFactory(), loggerManager, processingOptions);
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
         // Note: All the data for this should have been downloaded in the validation step above
         initialiseSpecTopics(topicProvider, contentSpec);
 
         // Good point to check for a shutdown
         allowShutdownToContinueIfRequested();
 
         if (!pushToZanata(getProviderFactory(), contentSpec, contentSpecEntity)) {
             printErrorAndShutdown(Constants.EXIT_FAILURE, ClientUtilities.getMessage("ERROR_ZANATA_PUSH_FAILED_MSG"), false);
             shutdown(Constants.EXIT_FAILURE);
         } else {
             JCommander.getConsole().println(ClientUtilities.getMessage("SUCCESSFUL_ZANATA_PUSH_MSG"));
         }
 
         // Save the etag cache
         try {
             eTagCache.save(new File(Constants.ZANATA_CACHE_LOCATION));
         } catch (IOException e) {
             printErrorAndShutdown(Constants.EXIT_FAILURE,
                     ClientUtilities.getMessage("ERROR_FAILED_TO_SAVE_ZANATA_CACHE_MSG", Constants.ZANATA_CACHE_LOCATION), false);
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
      * Initialise the Zanata Interface and setup it's locales.
      *
      * @return The initialised Zanata Interface.
      */
     protected ZanataInterface initialiseZanataInterface() {
         final ZanataDetails zanataDetails = getCspConfig().getZanataDetails();
         final ZanataInterface zanataInterface = new ZanataInterface(0.2, zanataDetails, getDisableSSLCert());
 
         // Configure the cache
         ZanataServerConfiguration configuration = null;
         for (final Map.Entry<String, ZanataServerConfiguration> entry : getClientConfig().getZanataServers().entrySet()) {
             if (entry.getValue().getUrl().equals(zanataDetails.getServer())) {
                 configuration = entry.getValue();
                 break;
             }
         }
         if (configuration != null && configuration.useCache()) {
             try {
                 eTagCache.load(new File(Constants.ZANATA_CACHE_LOCATION));
             } catch (IOException e) {
                 // TODO
             }
             final ETagInterceptor interceptor = new ETagInterceptor(eTagCache);
             zanataInterface.getProxyFactory().registerPrefixInterceptor(interceptor);
         }
 
         return zanataInterface;
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
        final List<Entity> entities = XMLUtilities.parseEntitiesFromString(contentSpec.getEntities());
         final Map<TopicWrapper, SpecTopic> topicToSpecTopic = new HashMap<TopicWrapper, SpecTopic>();
         boolean error = false;
         final ZanataInterface zanataInterface = initialiseZanataInterface();
 
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
                     printError(ClientUtilities.getMessage("ERROR_INVALID_XML_MSG", topic.getId(), topic.getRevision()), false);
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
 
         final float total = getContentSpecOnly() ? 1 : (topics.size() + 1);
         float current = 0;
         final int showPercent = 5;
         int lastPercent = 0;
 
         final String answer;
         if (getAnswerYes()) {
             JCommander.getConsole().println(ClientUtilities.getMessage("PUSHING_TOPICS_TO_ZANATA_MSG", ((int) total)));
             answer = "yes";
         } else {
             JCommander.getConsole().println(ClientUtilities.getMessage("PUSH_TOPICS_TO_ZANATA_MSG", ((int) total)));
             answer = JCommander.getConsole().readLine();
         }
 
         final Map<MessageType, List<String>> messages = new HashMap<MessageType, List<String>>();
         messages.put(MessageType.WARNING, new ArrayList<String>());
         messages.put(MessageType.ERROR, new ArrayList<String>());
 
         if (answer.equalsIgnoreCase("yes") || answer.equalsIgnoreCase("y")) {
             JCommander.getConsole().println(ClientUtilities.getMessage("STARTING_TO_PUSH_TO_ZANATA_MSG"));
 
             // Upload the content specification to zanata first so we can reference the nodes when pushing topics
             final TranslatedContentSpecWrapper translatedContentSpec = pushContentSpecToZanata(providerFactory, contentSpecEntity,
                     zanataInterface, messages, entities);
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
                         messages.get(MessageType.ERROR).add(
                                 "Topic ID " + topic.getId() + ", Revision " + topic.getRevision() + " failed to be created in Zanata.");
                         error = true;
                     } else {
                         if (!pushTopicToZanata(providerFactory, contentSpec, specTopic, translatedCSNode, zanataInterface, messages,
                                 entities)) {
                             error = true;
                         }
                     }
                 }
             }
         }
 
         // Print the info/error messages
         if (messages.size() > 0) {
             JCommander.getConsole().println("Output:");
             // Print the warning messages first and then any errors
             if (messages.containsKey(MessageType.WARNING)) {
                 JCommander.getConsole().println("Warnings:");
                 for (final String message : messages.get(MessageType.WARNING)) {
                     JCommander.getConsole().println("\t" + message);
                 }
             }
             if (messages.containsKey(MessageType.ERROR)) {
                 JCommander.getConsole().println("Errors:");
                 for (final String message : messages.get(MessageType.ERROR)) {
                     JCommander.getConsole().println("\t" + message);
                 }
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
      * @param contentSpec
      * @param specTopic
      * @param zanataInterface
      * @param messages        @return True if the topic was pushed successful otherwise false.
      * @param entities
      */
     protected boolean pushTopicToZanata(final DataProviderFactory providerFactory, ContentSpec contentSpec, final SpecTopic specTopic,
             final TranslatedCSNodeWrapper translatedCSNode, final ZanataInterface zanataInterface,
             final Map<MessageType, List<String>> messages, final List<Entity> entities) {
         final TopicWrapper topic = (TopicWrapper) specTopic.getTopic();
         final Document doc = specTopic.getXMLDocument();
         boolean error = false;
 
         // Get the condition if the xml has any conditions
         boolean xmlHasConditions = !DocBookUtilities.getConditionNodes(doc).isEmpty();
         final String condition = xmlHasConditions ? specTopic.getConditionStatement(true) : null;
 
         // Process the conditions, if any exist, to remove any nodes that wouldn't be seen for the content spec.
         DocBookUtilities.processConditions(condition, doc, BuilderConstants.DEFAULT_CONDITION);
 
         // Remove any custom entities, since they cause massive translation issues.
         String customEntities = null;
         if (!entities.isEmpty()) {
             try {
                 if (TranslationUtilities.resolveCustomTopicEntities(entities, doc)) {
                     customEntities = contentSpec.getEntities();
                 }
             } catch (Exception e) {
 
             }
         }
 
         // Update the topics XML
         topic.setXml(XMLUtilities.convertNodeToString(doc.getDocumentElement(), true));
 
         // Create the zanata id based on whether a condition has been specified or not
         final boolean csNodeSpecificTopic = !isNullOrEmpty(condition) || !isNullOrEmpty(customEntities);
         final String zanataId = getTopicZanataId(specTopic, translatedCSNode, csNodeSpecificTopic);
 
         // Check if a translated topic already exists
         final boolean translatedTopicExists = EntityUtilities.getTranslatedTopicByTopicAndNodeId(providerFactory, topic.getId(),
                 topic.getRevision(), csNodeSpecificTopic ? translatedCSNode.getId() : null, topic.getLocale()) != null;
 
         // Check if the zanata document already exists, if it does than the topic can be ignored.
         Resource zanataFile;
         try {
             zanataFile = zanataInterface.getZanataResource(zanataId);
         } catch (NotModifiedException e) {
             // Assigned zanataFile anything since the file exists
             zanataFile = new Resource();
         }
         if (zanataFile == null) {
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
                 messages.get(MessageType.ERROR).add("Topic ID " + topic.getId() + ", Revision " + topic.getRevision() + " failed to be " +
                         "created in Zanata.");
                 error = true;
             } else if (!translatedTopicExists) {
                 createPressGangTranslatedTopic(providerFactory, topic, condition, customEntities, translatedCSNode, messages);
             }
         } else if (!translatedTopicExists) {
             createPressGangTranslatedTopic(providerFactory, topic, condition, customEntities, translatedCSNode, messages);
         } else {
             messages.get(MessageType.WARNING).add("Topic ID " + topic.getId() + ", Revision " + topic.getRevision() + " already exists - " +
                     "Skipping.");
         }
 
         return !error;
     }
 
     protected boolean createPressGangTranslatedTopic(final DataProviderFactory providerFactory, final TopicWrapper topic, String condition,
             String customEntities, final TranslatedCSNodeWrapper translatedCSNode, final Map<MessageType, List<String>> messages) {
         final TranslatedTopicProvider translatedTopicProvider = providerFactory.getProvider(TranslatedTopicProvider.class);
 
         // Create the Translated Topic based on if it has a condition/custom entity or not.
         final TranslatedTopicWrapper translatedTopic;
         if (condition == null && customEntities == null) {
             translatedTopic = TranslationUtilities.createTranslatedTopic(providerFactory, topic, null, null, null);
         } else {
             translatedTopic = TranslationUtilities.createTranslatedTopic(providerFactory, topic, translatedCSNode, condition,
                     customEntities);
         }
 
         // Save the Translated Topic
         try {
             if (translatedTopicProvider.createTranslatedTopic(translatedTopic) == null) {
                 messages.get(MessageType.ERROR).add("Topic ID " + topic.getId() + ", Revision " + topic.getRevision() + " failed to be " +
                         "created in PressGang.");
                 return false;
             }
         } catch (Exception e) {
             messages.get(MessageType.ERROR).add(
                     "Topic ID " + topic.getId() + ", Revision " + topic.getRevision() + " failed to be created " +
                             "in PressGang.");
             return false;
         }
 
         return true;
     }
 
     /**
      * Gets the Zanata ID for a topic based on whether or not the topic has any conditional text.
      *
      * @param specTopic        The topic to create the Zanata ID for.
      * @param translatedCSNode
      * @param csNodeSpecific   If the Topic the Zanata ID is being created for is specific to the translated CS Node. That is that it
      *                         either has conditions, or custom entities.
      * @return The unique Zanata ID that can be used to create a document in Zanata.
      */
     protected String getTopicZanataId(final SpecTopic specTopic, final TranslatedCSNodeWrapper translatedCSNode, boolean csNodeSpecific) {
         final TopicWrapper topic = (TopicWrapper) specTopic.getTopic();
 
         // Create the zanata id based on whether a condition has been specified or not
         final String zanataId;
         if (csNodeSpecific) {
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
      * @param entities
      * @return
      */
     protected TranslatedContentSpecWrapper pushContentSpecToZanata(final DataProviderFactory providerFactory,
             final ContentSpecWrapper contentSpecEntity, final ZanataInterface zanataInterface,
             final Map<MessageType, List<String>> messages, final List<Entity> entities) {
         final String zanataId = "CS" + contentSpecEntity.getId() + "-" + contentSpecEntity.getRevision();
         Resource zanataFile;
         try {
             zanataFile = zanataInterface.getZanataResource(zanataId);
         } catch (NotModifiedException e) {
             // Assigned zanataFile anything since the file exists
             zanataFile = new Resource();
         }
         TranslatedContentSpecWrapper translatedContentSpec = EntityUtilities.getTranslatedContentSpecById(providerFactory,
                 contentSpecEntity.getId(), contentSpecEntity.getRevision());
 
         // Resolve any custom entities that might exist
         TranslationUtilities.resolveCustomContentSpecEntities(entities, contentSpecEntity);
 
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
                 messages.get(MessageType.ERROR).add("Content Spec ID " + contentSpecEntity.getId() + ", " +
                         "Revision " + contentSpecEntity.getRevision() + " " +
                         "failed to be created in Zanata.");
                 return null;
             } else if (translatedContentSpec == null) {
                 return createPressGangTranslatedContentSpec(providerFactory, contentSpecEntity, messages);
             }
         } else if (translatedContentSpec == null) {
             return createPressGangTranslatedContentSpec(providerFactory, contentSpecEntity, messages);
         } else {
             messages.get(MessageType.WARNING).add("Content Spec ID " + contentSpecEntity.getId() + ", " +
                     "Revision " + contentSpecEntity.getRevision() + " already " +
                     "exists - Skipping.");
         }
 
         return translatedContentSpec;
     }
 
     protected TranslatedContentSpecWrapper createPressGangTranslatedContentSpec(final DataProviderFactory providerFactory,
             final ContentSpecWrapper contentSpecEntity, final Map<MessageType, List<String>> messages) {
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
                 messages.get(MessageType.ERROR).add("Content Spec ID" + contentSpecEntity.getId() + ", " +
                         "Revision " + contentSpecEntity.getRevision() + " failed to be created in PressGang.");
                 return null;
             } else {
                 return translatedContentSpec;
             }
         } catch (Exception e) {
             messages.get(MessageType.ERROR).add(
                     "Content Spec ID " + contentSpecEntity.getId() + ", Revision " + contentSpecEntity.getRevision() +
                             " failed to be created in PressGang.");
             return null;
         }
     }
 
     @Override
     public boolean requiresExternalConnection() {
         return true;
     }
 
     private static enum MessageType {
         WARNING, ERROR
     }
 }
