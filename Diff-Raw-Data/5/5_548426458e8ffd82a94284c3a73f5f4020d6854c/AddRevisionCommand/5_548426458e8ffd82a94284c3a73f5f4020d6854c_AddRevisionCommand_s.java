 package org.jboss.pressgang.ccms.contentspec.client.commands;
 
 import static com.google.common.base.Strings.isNullOrEmpty;
 
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.Date;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Locale;
 
 import com.beust.jcommander.JCommander;
 import com.beust.jcommander.Parameter;
 import com.beust.jcommander.Parameters;
 import com.google.common.collect.Lists;
 import org.jboss.pressgang.ccms.contentspec.builder.constants.BuilderConstants;
 import org.jboss.pressgang.ccms.contentspec.client.commands.base.BaseCommandImpl;
 import org.jboss.pressgang.ccms.contentspec.client.config.ClientConfiguration;
 import org.jboss.pressgang.ccms.contentspec.client.config.ContentSpecConfiguration;
 import org.jboss.pressgang.ccms.contentspec.client.constants.Constants;
 import org.jboss.pressgang.ccms.contentspec.client.utils.ClientUtilities;
 import org.jboss.pressgang.ccms.contentspec.sort.RevisionNodeSort;
 import org.jboss.pressgang.ccms.contentspec.structures.RevNumber;
 import org.jboss.pressgang.ccms.contentspec.structures.Version;
 import org.jboss.pressgang.ccms.contentspec.utils.EntityUtilities;
 import org.jboss.pressgang.ccms.provider.CSNodeProvider;
 import org.jboss.pressgang.ccms.provider.ContentSpecProvider;
 import org.jboss.pressgang.ccms.provider.StringConstantProvider;
 import org.jboss.pressgang.ccms.provider.TopicProvider;
 import org.jboss.pressgang.ccms.provider.TranslatedTopicProvider;
 import org.jboss.pressgang.ccms.rest.v1.query.RESTCSNodeQueryBuilderV1;
 import org.jboss.pressgang.ccms.utils.common.XMLUtilities;
 import org.jboss.pressgang.ccms.utils.constants.CommonConstants;
 import org.jboss.pressgang.ccms.wrapper.CSNodeWrapper;
 import org.jboss.pressgang.ccms.wrapper.ContentSpecWrapper;
 import org.jboss.pressgang.ccms.wrapper.StringConstantWrapper;
 import org.jboss.pressgang.ccms.wrapper.TopicWrapper;
 import org.jboss.pressgang.ccms.wrapper.TranslatedCSNodeWrapper;
 import org.jboss.pressgang.ccms.wrapper.TranslatedTopicWrapper;
 import org.jboss.pressgang.ccms.wrapper.collection.CollectionWrapper;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.NodeList;
 
 @Parameters(resourceBundle = "commands", commandDescriptionKey = "ADD_REVISION")
 public class AddRevisionCommand extends BaseCommandImpl {
     private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("EEE dd MMM yyyy", Locale.ENGLISH);
 
     @Parameter(metaVar = "[ID]")
     private List<Integer> ids = new ArrayList<Integer>();
 
    @Parameter(names = Constants.REV_MESSAGE_LONG_PARAM, descriptionKey = "REV_MESSAGE")
     private List<String> messages = Lists.newArrayList();
 
    @Parameter(names = Constants.LOCALE_LONG_PARAM, descriptionKey = "ADD_REVISION_LOCALE", metaVar = "<LOCALE>", required = true)
     private String locale = null;
 
     @Parameter(names = Constants.FIRST_NAME_LONG_PARAM, descriptionKey = "ADD_REVISION_FIRSTNAME", metaVar = "<FIRSTNAME>")
     private String firstname = null;
 
     @Parameter(names = Constants.SURNAME_LONG_PARAM, descriptionKey = "ADD_REVISION_SURNAME", metaVar = "<SURNAME>")
     private String surname = null;
 
     @Parameter(names = Constants.EMAIL_LONG_PARAM, descriptionKey = "ADD_REVISION_EMAIL", metaVar = "<EMAIL>")
     private String email = null;
 
     @Parameter(names = Constants.REVNUMBER_LONG_PARAM, descriptionKey = "ADD_REVISION_REVNUMBER", metaVar = "<REVNUMBER>")
     private String revnumber = null;
 
     @Parameter(names = Constants.DATE_LONG_PARAM, descriptionKey = "ADD_REVISION_DATE", metaVar = "<DATE>")
     private String date = null;
 
     public AddRevisionCommand(JCommander parser, ContentSpecConfiguration cspConfig, ClientConfiguration clientConfig) {
         super(parser, cspConfig, clientConfig);
     }
 
     public List<Integer> getIds() {
         return ids;
     }
 
     public void setIds(final List<Integer> ids) {
         this.ids = ids;
     }
 
     public String getLocale() {
         return locale;
     }
 
     public void setLocale(String locale) {
         this.locale = locale;
     }
 
     public List<String> getMessages() {
         return messages;
     }
 
     public void setMessages(List<String> messages) {
         this.messages = messages;
     }
 
     public String getFirstname() {
         return firstname;
     }
 
     public void setFirstname(String firstname) {
         this.firstname = firstname;
     }
 
     public String getSurname() {
         return surname;
     }
 
     public void setSurname(String surname) {
         this.surname = surname;
     }
 
     public String getEmail() {
         return email;
     }
 
     public void setEmail(String email) {
         this.email = email;
     }
 
     public String getRevnumber() {
         return revnumber;
     }
 
     public void setRevnumber(String revnumber) {
         this.revnumber = revnumber;
     }
 
     public String getDate() {
         return date;
     }
 
     public void setDate(String date) {
         this.date = date;
     }
 
     @Override
     public String getCommandName() {
         return Constants.ADD_REVISION_COMMAND_NAME;
     }
 
     @Override
     public void process() {
         final ContentSpecProvider contentSpecProvider = getProviderFactory().getProvider(ContentSpecProvider.class);
         final CSNodeProvider csNodeProvider = getProviderFactory().getProvider(CSNodeProvider.class);
         final TopicProvider topicProvider = getProviderFactory().getProvider(TopicProvider.class);
         final TranslatedTopicProvider translatedTopicProvider = getProviderFactory().getProvider(TranslatedTopicProvider.class);
 
         // Initialise the basic data and perform basic checks
         ClientUtilities.prepareAndValidateIds(this, getCspConfig(), getIds());
 
         // Validate that the minimum arguments are set
         validateArguments();
 
         // Good point to check for a shutdown
         allowShutdownToContinueIfRequested();
 
         // Check to make sure the lang is valid
         if (getLocale() != null && !ClientUtilities.validateLanguage(this, getProviderFactory(), getLocale())) {
             shutdown(Constants.EXIT_ARGUMENT_ERROR);
         }
 
         // Get the Content Specification from the server.
         final ContentSpecWrapper contentSpecEntity = ClientUtilities.getContentSpecEntity(contentSpecProvider, ids.get(0), null);
         if (contentSpecEntity == null) {
             printErrorAndShutdown(Constants.EXIT_FAILURE, getMessage("ERROR_NO_ID_FOUND_MSG"), false);
         }
 
         // Good point to check for a shutdown
         allowShutdownToContinueIfRequested();
 
         // Get the revision history topic if one exists
         final RESTCSNodeQueryBuilderV1 queryBuilder = new RESTCSNodeQueryBuilderV1();
         queryBuilder.setCSNodeTypes(Arrays.asList(CommonConstants.CS_NODE_META_DATA_TOPIC));
         queryBuilder.setCSNodeTitle(CommonConstants.CS_REV_HISTORY_TITLE);
         queryBuilder.setContentSpecIds(Arrays.asList(ids.get(0)));
 
         // Make sure the content spec has a revision history topic
         final CollectionWrapper<CSNodeWrapper> csNodes = csNodeProvider.getCSNodesWithQuery(queryBuilder.getQuery());
         if (csNodes.isEmpty()) {
             printErrorAndShutdown(Constants.EXIT_FAILURE, getMessage("ERROR_NO_REV_HISTORY_MSG"), false);
         }
 
         // Get the topic from the server
         final CSNodeWrapper revisionHistoryNode = csNodes.getItems().get(0);
         final TopicWrapper revisionHistory = ClientUtilities.getTopicEntity(topicProvider, revisionHistoryNode.getEntityId(),
                 revisionHistoryNode.getEntityRevision());
 
         // Good point to check for a shutdown
         allowShutdownToContinueIfRequested();
 
         if (getLocale() == null) {
             // Add the revision and save the topic
             addRevisionToTopic(revisionHistory, getMessages(), getFirstname(), getSurname(), getEmail(), getRevnumber(), getDate());
             topicProvider.updateTopic(revisionHistory);
         } else {
             // Get the matching translated csnode
             final CollectionWrapper<TranslatedCSNodeWrapper> translatedCSNodes = revisionHistoryNode.getTranslatedNodes();
             final TranslatedCSNodeWrapper matchingTranslatedCSNode = getMatchingTranslatedCSNode(revisionHistoryNode, translatedCSNodes);
 
             // Find the translated topic for the node
             TranslatedTopicWrapper translatedTopic = EntityUtilities.returnClosestTranslatedTopic(revisionHistory, matchingTranslatedCSNode,
                     getLocale());
             if (translatedTopic == null) {
                 translatedTopic = EntityUtilities.returnClosestTranslatedTopic(revisionHistory, getLocale());
             }
 
             // Good point to check for a shutdown
             allowShutdownToContinueIfRequested();
 
             // Add the revision and save the translated topic
             addRevisionToTranslatedTopic(translatedTopic, getMessages(), getFirstname(), getSurname(), getEmail(), getRevnumber(),
                     getDate());
             translatedTopicProvider.updateTranslatedTopic(translatedTopic);
         }
     }
 
     /**
      * Validates the command arguments to make sure the minimum values are set.
      */
     protected void validateArguments() {
         // Make sure the firstname has a default or is set via the command line
         if (isNullOrEmpty(getClientConfig().getDefaults().getFirstname()) && isNullOrEmpty(getFirstname())) {
             printErrorAndShutdown(Constants.EXIT_ARGUMENT_ERROR, getMessage("ERROR_NO_FIRSTNAME_MSG"), true);
         }
 
         // Make sure the surname has a default or is set via the command line
         if (isNullOrEmpty(getClientConfig().getDefaults().getSurname()) && isNullOrEmpty(getSurname())) {
             printErrorAndShutdown(Constants.EXIT_ARGUMENT_ERROR, getMessage("ERROR_NO_SURNAME_MSG"), true);
         }
 
         // Make sure the email has a default or is set via the command line
         if (isNullOrEmpty(getClientConfig().getDefaults().getEmail()) && isNullOrEmpty(getEmail())) {
             printErrorAndShutdown(Constants.EXIT_ARGUMENT_ERROR, getMessage("ERROR_NO_EMAIL_MSG"), true);
         }
 
         // Make sure at least one message has been defined
         if (getMessages().isEmpty()) {
             printErrorAndShutdown(Constants.EXIT_ARGUMENT_ERROR, getMessage("ERROR_NO_MESSAGES_MSG"), true);
         }
     }
 
     protected TranslatedCSNodeWrapper getMatchingTranslatedCSNode(final CSNodeWrapper node,
             final CollectionWrapper<TranslatedCSNodeWrapper> translatedNodes) {
         TranslatedCSNodeWrapper matchingNode = null;
         for (final TranslatedCSNodeWrapper translatedNode : translatedNodes.getItems()) {
             if (translatedNode.getNodeRevision().equals(node.getRevision())) {
                 return translatedNode;
             } else if ((matchingNode == null || matchingNode.getNodeRevision() > translatedNode.getNodeRevision()) && translatedNode
                     .getNodeRevision() <= node.getRevision()) {
                 matchingNode = translatedNode;
             }
         }
 
         return matchingNode;
     }
 
     /**
      * Adds a revision to a revision history topic.
      *
      * @param topic The revision history to add the revision element to.
      * @param messages The messages for the revision.
      * @param firstname The firstname of the author for the revision.
      * @param surname The surname of the author for the revision.
      * @param email The email of the author for the revision.
      * @param revnumber The revnumber to use for the revision, or null if one should be calculated.
      * @param date The date to use for the revision, or null if the current date should be used.
      */
     protected void addRevisionToTopic(final TopicWrapper topic, final List<String> messages, final String firstname, final String surname,
             final String email, final String revnumber, final String date) {
         Document doc = null;
         try {
             if (isNullOrEmpty(topic.getXml())) {
                 final String template = getRevisionHistoryTemplate();
                 doc = XMLUtilities.convertStringToDocument(template);
             } else {
                 doc = XMLUtilities.convertStringToDocument(topic.getXml());
             }
         } catch (Exception e) {
             printErrorAndShutdown(Constants.EXIT_FAILURE, getMessage("ERROR_INVALID_REV_HISTORY_MSG"), false);
         }
 
         final String fixedRevnumber;
         if (isNullOrEmpty(revnumber)) {
             // Get the revision nodes
             final NodeList docRevisions = doc.getElementsByTagName("revision");
 
             // Calculate the next revision number
             final GenericRevisionStrategy revisionStrategy = new GenericRevisionStrategy();
             fixedRevnumber = revisionStrategy.getNextRevisionNumber(docRevisions);
         } else {
             fixedRevnumber = revnumber;
         }
 
         addRevisionToDocument(doc, messages, firstname, surname, email, fixedRevnumber, date);
 
         topic.setXml(XMLUtilities.convertNodeToString(doc, true));
     }
 
     /**
      * Adds a translated revision to a revision history translated topics additional xml.
      *
      * @param topic The revision history to add the revision element to.
      * @param messages The messages for the revision.
      * @param firstname The firstname of the author for the revision.
      * @param surname The surname of the author for the revision.
      * @param email The email of the author for the revision.
      * @param revnumber The revnumber to use for the revision, or null if one should be calculated.
      * @param date The date to use for the revision, or null if the current date should be used.
      */
     protected void addRevisionToTranslatedTopic(final TranslatedTopicWrapper topic, final List<String> messages, final String firstname,
             final String surname, final String email, final String revnumber, final String date) {
         // Convert the base XML into a DOM document
         Document doc = null;
         try {
             if (isNullOrEmpty(topic.getXml())) {
                 final String template = getRevisionHistoryTemplate();
                 doc = XMLUtilities.convertStringToDocument(template);
             } else {
                 doc = XMLUtilities.convertStringToDocument(topic.getXml());
             }
         } catch (Exception e) {
             printErrorAndShutdown(Constants.EXIT_FAILURE, getMessage("ERROR_INVALID_REV_HISTORY_MSG"), false);
         }
 
         // Convert the translated additional XML into a DOM document
         Document translatedDoc = null;
         try {
             if (isNullOrEmpty(topic.getXml())) {
                 final String template = getRevisionHistoryTemplate();
                 translatedDoc = XMLUtilities.convertStringToDocument(template);
             } else {
                 translatedDoc = XMLUtilities.convertStringToDocument(topic.getTranslatedAdditionalXML());
             }
         } catch (Exception e) {
             printErrorAndShutdown(Constants.EXIT_FAILURE, getMessage("ERROR_INVALID_TRANS_REV_HISTORY_MSG"), false);
         }
 
         // Calculate the revnumber to be used
         final String fixedRevnumber;
         if (isNullOrEmpty(revnumber)) {
             // Get the revision nodes
             final NodeList docRevisions = doc.getElementsByTagName("revision");
             final NodeList additionalDocRevisions = translatedDoc.getElementsByTagName("revision");
             final List<Element> revisionNodes = new LinkedList<Element>();
             for (int i = 0; i < docRevisions.getLength(); i++) {
                 revisionNodes.add((Element) docRevisions.item(i));
             }
             for (int i = 0; i < additionalDocRevisions.getLength(); i++) {
                 revisionNodes.add((Element) additionalDocRevisions.item(i));
             }
 
             final TranslationRevisionStrategy revisionStrategy = new TranslationRevisionStrategy();
             fixedRevnumber = revisionStrategy.getNextRevisionNumber(revisionNodes);
         } else {
             fixedRevnumber = revnumber;
         }
 
         addRevisionToDocument(translatedDoc, messages, firstname, surname, email, fixedRevnumber, date);
 
         topic.setTranslatedAdditionalXML(XMLUtilities.convertNodeToString(translatedDoc, true));
     }
 
     /**
      * Creates a revision element and adds it to the &lt;revhistory&gt; element of a DOM document.
      *
      * @param doc The DOM document to add the revision to.
      * @param messages The messages for the revision.
      * @param firstname The firstname of the author for the revision.
      * @param surname The surname of the author for the revision.
      * @param email The email of the author for the revision.
      * @param revnumber The revnumber to use for the revision, or null if one should be calculated.
      * @param date The date to use for the revision, or null if the current date should be used.
      */
     protected void addRevisionToDocument(final Document doc, final List<String> messages, final String firstname, final String surname,
             final String email, final String revnumber, final String date) {
         final NodeList revhistories = doc.getElementsByTagName("revhistory");
         if (revhistories.getLength() > 0) {
             final Element revhistory = (Element) revhistories.item(0);
 
             // Get the revision nodes
             final NodeList docRevisions = doc.getElementsByTagName("revision");
             final List<Element> revisionNodes = new LinkedList<Element>();
             for (int i = 0; i < docRevisions.getLength(); i++) {
                 revisionNodes.add((Element) docRevisions.item(i));
             }
 
             // Create the revision
             final Element revision = doc.createElement("revision");
             if (revhistory.getFirstChild() != null) {
                 revhistory.insertBefore(revision, revhistory.getFirstChild());
             } else {
                 revhistory.appendChild(revision);
             }
 
             // Create the revnumber
             final Element revnumberEle = doc.createElement("revnumber");
             revnumberEle.setNodeValue(revnumber);
             revision.appendChild(revnumberEle);
 
             // Create the date
             final Element dateEle = doc.createElement("date");
             dateEle.setNodeValue(isNullOrEmpty(date) ? DATE_FORMAT.format(new Date()) : date);
             revision.appendChild(dateEle);
 
             // Create the author
             final Element author = doc.createElement("author");
             revision.appendChild(author);
 
             final Element firstnameEle = doc.createElement("firstname");
             firstnameEle.setNodeValue(firstname);
             author.appendChild(firstnameEle);
 
             final Element surnameEle = doc.createElement("surname");
             surnameEle.setNodeValue(surname);
             author.appendChild(surnameEle);
 
             final Element emailEle = doc.createElement("email");
             emailEle.setNodeValue(email);
             author.appendChild(emailEle);
 
             // Create the revdescription
             final Element revdescription = doc.createElement("revdescription");
             revision.appendChild(revdescription);
 
             final Element simpleList = doc.createElement("simplelist");
             revdescription.appendChild(simpleList);
             for (final String message : messages) {
                 final Element member = doc.createElement("member");
                 member.setNodeValue(message);
                 simpleList.appendChild(member);
             }
         } else {
             printErrorAndShutdown(Constants.EXIT_FAILURE, getMessage("ERROR_NO_REVHISTORY_ELE_MSG"), false);
         }
     }
 
     protected String getRevisionHistoryTemplate() {
         try {
             final StringConstantWrapper revisionHistoryConstant = getProviderFactory().getProvider(
                     StringConstantProvider.class).getStringConstant(BuilderConstants.REVISION_HISTORY_XML_ID);
             return revisionHistoryConstant.getValue();
         } catch (Exception e) {
             return null;
         }
     }
 
     @Override
     public boolean loadFromCSProcessorCfg() {
         return false;
     }
 
     @Override
     public boolean requiresExternalConnection() {
         return true;
     }
 
     protected static interface RevisionStrategy {
         /**
          * Gets the revision number that should be used next.
          *
          * @param revisions The current list of revisions.
          * @return The revision number that should be used next.
          */
         String getNextRevisionNumber(final List<Element> revisions);
 
         /**
          * Gets the highest revision based on its revnumber from a list of revision elements.
          *
          * @param revisions The current list of revisions.
          * @return The highest revision element.
          */
         Element getHighestRevision(final List<Element> revisions);
 
         /**
          * Sort the revisions from highest revision to the lowest revision.
          *
          * @param revisions A list of revisions to be sorted.
          */
         void sort(final List<Element> revisions);
     }
 
     protected static class GenericRevisionStrategy implements RevisionStrategy {
         public String getNextRevisionNumber(final NodeList revisions) {
             final List<Element> revisionNodes = new LinkedList<Element>();
             for (int i = 0; i < revisions.getLength(); i++) {
                 revisionNodes.add((Element) revisions.item(i));
             }
 
             return getNextRevisionNumber(revisionNodes);
         }
 
         @Override
         public String getNextRevisionNumber(final List<Element> revisions) {
             final Element highestRevision = getHighestRevision(revisions);
 
             if (highestRevision != null) {
                 final NodeList revnumbers = highestRevision.getElementsByTagName("revnumber");
                 if (revnumbers.getLength() > 0) {
                     final RevNumber revNumber = new RevNumber(revnumbers.item(0).getTextContent());
                     final Version release = revNumber.getRelease();
 
                     final Version newRelease;
                     if (release == null) {
                         newRelease = new Version("1");
                     } else if (release.getMinor() == null) {
                         newRelease = new Version(release.getMajor() + 1, null, null, release.getOther());
                     } else if (release.getRevision() == null) {
                         newRelease = new Version(release.getMajor(), release.getMinor() + 1, null, release.getOther());
                     } else {
                         newRelease = new Version(release.getMajor(), release.getMinor(), release.getRevision() + 1, release.getOther());
                     }
                     return new RevNumber(revNumber.getVersion(), newRelease).toString();
                 } else {
                     return null;
                 }
             } else {
                 return "1.0.0-1";
             }
         }
 
         @Override
         public Element getHighestRevision(final List<Element> revisions) {
             if (revisions == null || revisions.isEmpty()) {
                 return null;
             } else {
                 // Ensure that the revisions are sorted but make sure to leave the original list alone.
                 final List<Element> elements = new ArrayList<Element>(revisions);
                 sort(elements);
 
                 return elements.get(0);
             }
         }
 
         @Override
         public void sort(List<Element> revisions) {
             Collections.sort(revisions, new RevisionNodeSort());
         }
     }
 
     protected static class TranslationRevisionStrategy extends GenericRevisionStrategy {
 
         @Override
         public String getNextRevisionNumber(final List<Element> revisions) {
             final Element highestRevision = getHighestRevision(revisions);
 
             final NodeList revnumbers = highestRevision.getElementsByTagName("revnumber");
             if (revnumbers.getLength() > 0) {
                 final RevNumber revNumber = new RevNumber(revnumbers.item(0).getTextContent());
                 final Version release = revNumber.getRelease();
 
                 final Version newRelease;
                 if (release == null) {
                     newRelease = new Version("1");
                 } else if (release.getMinor() == null) {
                     newRelease = new Version(release.getMajor(), 1, null, release.getOther());
                 } else if (release.getRevision() == null) {
                     newRelease = new Version(release.getMajor(), release.getMinor(), 1, release.getOther());
                 } else {
                     newRelease = new Version(release.getMajor(), release.getMinor(), release.getRevision() + 1, release.getOther());
                 }
                 return new RevNumber(revNumber.getVersion(), newRelease).toString();
             } else {
                 return "1.0.0-1.1";
             }
         }
     }
 }
