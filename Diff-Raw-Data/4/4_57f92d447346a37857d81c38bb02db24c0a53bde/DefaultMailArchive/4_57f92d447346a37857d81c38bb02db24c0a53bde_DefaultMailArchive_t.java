 /*
  * See the NOTICE file distributed with this work for additional
  * information regarding copyright ownership.
  *
  * This is free software; you can redistribute it and/or modify it
  * under the terms of the GNU Lesser General Public License as
  * published by the Free Software Foundation; either version 2.1 of
  * the License, or (at your option) any later version.
  *
  * This software is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  * Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public
  * License along with this software; if not, write to the Free
  * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
  * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
  */
 package org.xwiki.contrib.mailarchive.internal;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 
 import javax.inject.Inject;
 import javax.inject.Named;
 import javax.inject.Singleton;
 import javax.mail.Message;
 import javax.mail.MessagingException;
 import javax.mail.Part;
 
 import org.apache.commons.lang.StringUtils;
 import org.slf4j.Logger;
 import org.xwiki.bridge.DocumentAccessBridge;
 import org.xwiki.component.annotation.Component;
 import org.xwiki.component.manager.ComponentLookupException;
 import org.xwiki.component.manager.ComponentManager;
 import org.xwiki.component.phase.Initializable;
 import org.xwiki.component.phase.InitializationException;
 import org.xwiki.context.Execution;
 import org.xwiki.context.ExecutionContext;
 import org.xwiki.contrib.mail.IMailComponent;
 import org.xwiki.contrib.mail.IMailReader;
 import org.xwiki.contrib.mail.IStoreManager;
 import org.xwiki.contrib.mail.MailItem;
 import org.xwiki.contrib.mail.SourceConnectionErrors;
 import org.xwiki.contrib.mail.internal.JavamailMessageParser;
 import org.xwiki.contrib.mail.source.IMailSource;
 import org.xwiki.contrib.mail.source.SourceType;
 import org.xwiki.contrib.mailarchive.IMASource;
 import org.xwiki.contrib.mailarchive.IMAUser;
 import org.xwiki.contrib.mailarchive.IMailArchive;
 import org.xwiki.contrib.mailarchive.IMailArchiveConfiguration;
 import org.xwiki.contrib.mailarchive.IMailingList;
 import org.xwiki.contrib.mailarchive.IType;
 import org.xwiki.contrib.mailarchive.LoadingSession;
 import org.xwiki.contrib.mailarchive.internal.data.IFactory;
 import org.xwiki.contrib.mailarchive.internal.data.MailDescriptor;
 import org.xwiki.contrib.mailarchive.internal.data.MailStore;
 import org.xwiki.contrib.mailarchive.internal.data.Server;
 import org.xwiki.contrib.mailarchive.internal.data.TopicDescriptor;
 import org.xwiki.contrib.mailarchive.internal.exceptions.MailArchiveException;
 import org.xwiki.contrib.mailarchive.internal.persistence.IPersistence;
 import org.xwiki.contrib.mailarchive.internal.persistence.XWikiPersistence;
 import org.xwiki.contrib.mailarchive.internal.threads.IMessagesThreader;
 import org.xwiki.contrib.mailarchive.internal.threads.ThreadableMessage;
 import org.xwiki.contrib.mailarchive.internal.timeline.ITimeLineGenerator;
 import org.xwiki.contrib.mailarchive.internal.utils.DecodedMailContent;
 import org.xwiki.contrib.mailarchive.internal.utils.IMailUtils;
 import org.xwiki.contrib.mailarchive.internal.utils.ITextUtils;
 import org.xwiki.contrib.mailarchive.internal.utils.TextUtils;
 import org.xwiki.environment.Environment;
 import org.xwiki.query.Query;
 import org.xwiki.query.QueryException;
 import org.xwiki.query.QueryManager;
 
 import com.xpn.xwiki.XWiki;
 import com.xpn.xwiki.XWikiContext;
 import com.xpn.xwiki.XWikiException;
 import com.xpn.xwiki.doc.XWikiDocument;
 import com.xpn.xwiki.objects.BaseObject;
 
 /**
  * Implementation of a <tt>IMailArchive</tt> component.
  */
 @Component
 @Singleton
 public class DefaultMailArchive implements IMailArchive, Initializable
 {
 
     /**
      * XWiki profile name of a non-existing user.
      */
     public static final String UNKNOWN_USER = "XWiki.UserDoesNotExist";
 
     public boolean isConfigured = false;
 
     // Components injected by the Component Manager
 
     /** Provides access to documents. */
     @Inject
     private DocumentAccessBridge dab;
 
     /** Provides access to the request context. */
     @Inject
     public Execution execution;
 
     /** Provides access to execution environment and from it to context and old core */
     @Inject
     private Environment environment;
 
     /**
      * Secure query manager that performs checks on rights depending on the query being executed.
      */
     @Inject
     private QueryManager queryManager;
 
     /** Provides access to log facility */
     @Inject
     Logger logger;
 
     /**
      * The component manager. We need it because we have to access some components dynamically based on the input
      * syntax.
      */
     @Inject
     private ComponentManager componentManager;
 
     /** Provides access to low-level mail api component */
     @Inject
     private IMailComponent mailManager;
 
     // Other global objects
 
     /** The XWiki context */
     private XWikiContext context;
 
     // TODO remove dependency to old core
     /** The XWiki old core */
     private XWiki xwiki;
 
     /** Provides access to Mail archive configuration items */
     @Inject
     private IItemsManager store;
 
     @Inject
     @Named("mbox")
     private IStoreManager builtinStore;
 
     /** Factory to convert raw conf to POJO */
     @Inject
     private IFactory factory;
 
     /** Provides access to the Mail archive configuration */
     @Inject
     private IMailArchiveConfiguration config;
 
     @Inject
     private IMessagesThreader threads;
 
     @Inject
     private ITimeLineGenerator timelineGenerator;
 
     /** Used to persist pages for mails and topics */
     @Inject
     private IPersistence persistence;
 
     /** Some utilities */
     @Inject
     public IMailUtils mailutils;
 
     @Inject
     public ITextUtils textUtils;
 
     /** Already archived topics, loaded from database */
     private HashMap<String, TopicDescriptor> existingTopics;
 
     /** Already archived messages, loaded from database */
     private HashMap<String, MailDescriptor> existingMessages;
 
     /** Is the component initialized ? */
     private boolean isInitialized = false;
 
     /** Are we currently in a loading session ? */
     private boolean locked = false;
 
     /**
      * {@inheritDoc}
      * 
      * @see org.xwiki.component.phase.Initializable#initialize()
      */
     @Override
     public void initialize() throws InitializationException
     {
         try {
             logger.debug("initialize updated()");
             ExecutionContext context = execution.getContext();
             this.context = (XWikiContext) context.getProperty("xwikicontext");
             this.xwiki = this.context.getWiki();
             // Initialize switchable logging for main components useful for the mail archive
 
             logger.info("Mail archive initialized !");
             logger.debug("PERMANENT DATA DIR : " + this.environment.getPermanentDirectory());
             // Create dump folder
             new File(this.environment.getPermanentDirectory(), "mailarchive/dump").mkdirs();
             // Register custom job
             // this.componentManager.registerComponent(this.componentManager.getComponentDescriptor(Job.class,
             // "mailarchivejob"));
 
         } catch (Throwable e) {
             logger.error("Could not initiliaze mailarchive ", e);
             e.printStackTrace();
         }
 
         this.isInitialized = true;
     }
 
     /**
      * {@inheritDoc}
      * 
      * @throws MailArchiveException
      * @throws InitializationException
      * @see org.xwiki.contrib.mailarchive.IMailArchive#getConfiguration()
      */
     @Override
     public IMailArchiveConfiguration getConfiguration() throws InitializationException, MailArchiveException
     {
         configure();
         return this.config;
     }
 
     /**
      * {@inheritDoc}
      * 
      * @see org.xwiki.contrib.mailarchive.IMailArchive#checkSource(String)
      */
     @Override
     public int checkSource(final String sourcePrefsDoc)
     {
         XWikiDocument serverDoc = null;
         try {
             serverDoc = xwiki.getDocument(sourcePrefsDoc, context);
         } catch (XWikiException e) {
             serverDoc = null;
         }
         if (serverDoc == null || !dab.exists(sourcePrefsDoc)) {
             logger.error("Page " + sourcePrefsDoc + " does not exist");
             return SourceConnectionErrors.INVALID_PREFERENCES.getCode();
         }
         if (serverDoc.getObject(XWikiPersistence.CLASS_MAIL_SERVERS) != null) {
             // Retrieve connection properties from prefs
             Server server = factory.createMailServer(sourcePrefsDoc);
             if (server == null) {
                 logger.warn("Could not retrieve server information from wiki page " + sourcePrefsDoc);
                 return SourceConnectionErrors.INVALID_PREFERENCES.getCode();
             }
 
             return checkSource(server);
         } else if (serverDoc.getObject(XWikiPersistence.CLASS_MAIL_STORES) != null) {
             // Retrieve connection properties from prefs
             MailStore store = factory.createMailStore(sourcePrefsDoc);
             if (store == null) {
                 logger.warn("Could not retrieve store information from wiki page " + sourcePrefsDoc);
                 return SourceConnectionErrors.INVALID_PREFERENCES.getCode();
             }
 
             return checkSource(store);
 
         } else {
             logger.error("Could not retrieve valid configuration object from page");
             return SourceConnectionErrors.INVALID_PREFERENCES.getCode();
         }
 
     }
 
     /**
      * {@inheritDoc}
      * 
      * @see org.xwiki.contrib.mailarchive.IMailArchive#checkSource(org.xwiki.contrib.mailarchive.LoadingSession)
      */
     @Override
     public Map<String, Integer> checkSource(final LoadingSession session)
     {
         Map<String, Integer> results = new HashMap<String, Integer>();
         List<IMASource> sources = getSourcesList(session);
         for (IMASource source : sources) {
             if ("SERVER".equals(source.getType())) {
                 results.put(source.getWikiDoc(), checkSource((Server) source));
             } else if ("STORE".equals(source.getType())) {
                 results.put(source.getWikiDoc(), checkSource((MailStore) source));
             } else {
                 logger.error("Unknown type of source " + source.getType() + " for " + source.getId());
                 results.put(source.getWikiDoc(), SourceConnectionErrors.UNKNOWN_SOURCE_TYPE.getCode());
             }
         }
         return results;
     }
 
     /**
      * @param server
      * @return
      */
     public int checkSource(final Server server)
     {
         logger.info("Checking server " + server);
 
         IMailReader mailReader = null;
         try {
             mailReader =
                 mailManager.getMailReader(server.getHostname(), server.getPort(), server.getProtocol(),
                     server.getUsername(), server.getPassword(), server.getAdditionalProperties());
         } catch (ComponentLookupException e) {
             logger.error("Could not find appropriate mail reader for server " + server.getId(), e);
             return -1;
         }
 
         int nbMessages = mailReader.check(server.getFolder(), true);
         logger.debug("check of server " + server.getId() + " returned " + nbMessages);
 
         // Persist connection state
 
         try {
             persistence.updateMailServerState(server.getWikiDoc(), nbMessages);
         } catch (Exception e) {
             logger.info("Failed to persist server connection state", e);
         }
 
         server.setState(nbMessages);
 
         return nbMessages;
     }
 
     /**
      * @param store
      * @return
      */
     public int checkSource(final MailStore store)
     {
         logger.info("Checking store " + store);
 
         IMailReader mailReader = null;
         try {
             mailReader = mailManager.getStoreManager(store.getFormat(), store.getLocation());
         } catch (ComponentLookupException e) {
             logger.error("Could not find appropriate mail reader for store " + store.getId(), e);
             return SourceConnectionErrors.INVALID_PREFERENCES.getCode();
         }
 
         int nbMessages = mailReader.check(store.getFolder(), true);
         logger.debug("check of server " + store.getId() + " returned " + nbMessages);
 
         // Persist connection state
 
         try {
             persistence.updateMailStoreState(store.getWikiDoc(), nbMessages);
         } catch (Exception e) {
             logger.info("Failed to persist server connection state", e);
         }
 
         store.setState(nbMessages);
 
         return nbMessages;
     }
 
     /**
      * {@inheritDoc}
      * 
      * @see org.xwiki.contrib.mailarchive.IMailArchive#computeThreads(java.lang.String)
      */
     public ThreadableMessage computeThreads(final String topicId)
     {
         try {
             if (topicId == null) {
                 return threads.thread();
             } else {
                 return threads.thread(topicId);
             }
         } catch (Exception e) {
             logger.error("Could not compute threads", e);
         }
         return null;
     }
 
     @Override
     public List<IMASource> getSourcesList(final LoadingSession session)
     {
         List<IMASource> servers = null;
         final Map<SourceType, String> sources = session.getSources();
         servers = new ArrayList<IMASource>();
         boolean hasServers = false;
         for (Entry<SourceType, String> source : sources.entrySet()) {
 
             if (SourceType.SERVER.equals(source.getKey())) {
                 final String prefsDoc = XWikiPersistence.SPACE_PREFS + ".Server_" + source.getValue();
                 Server server = factory.createMailServer(prefsDoc);
                 if (server != null) {
                     hasServers = true;
                     servers.add(server);
                 }
             } else if (SourceType.STORE.equals(source.getKey())) {
                 final String prefsDoc = XWikiPersistence.SPACE_PREFS + ".Store_" + source.getValue();
                 MailStore store = factory.createMailStore(prefsDoc);
                 if (store != null) {
                     servers.add(store);
                 }
             } else {
                 // This should never occur
                 logger.warn("Unknown type of source connection: " + source.getKey());
             }
         }
         if (!hasServers) {
             // Empty server config means all servers
             // Empty store config means no store
             servers.addAll(config.getServers());
         }
         return servers;
     }
 
     public String computeTimeline() throws XWikiException, InitializationException, MailArchiveException, IOException
     {
         if (!this.isConfigured) {
             configure();
         }
 
         String timelineFeed = timelineGenerator.compute();
         if (!StringUtils.isBlank(timelineFeed)) {
             File timelineFeedLocation = new File(environment.getPermanentDirectory(), "mailarchive/timeline");
             FileWriter fw = new FileWriter(timelineFeedLocation.getAbsolutePath() + "/TimeLineFeed.xml", false);
             fw.write(timelineFeed);
             fw.close();
         }
 
         return timelineFeed;
     }
 
     /**
      * @throws InitializationException
      * @throws MailArchiveException
      */
     protected void configure() throws InitializationException, MailArchiveException
     {
         // Init
         if (!this.isInitialized) {
             initialize();
         }
 
         config.reloadConfiguration();
 
         if (config.getItemsSpaceName() != null && !"".equals(config.getItemsSpaceName())) {
             XWikiPersistence.SPACE_ITEMS = config.getItemsSpaceName();
         }
         if (config.isUseStore()) {
             File maStoreLocation = new File(environment.getPermanentDirectory(), "mailarchive/storage");
             logger.info("Local Store Location: " + maStoreLocation.getAbsolutePath());
             logger.info("Local Store Provider: mstor");
             try {
                 this.builtinStore = mailManager.getStoreManager("mstor", maStoreLocation.getAbsolutePath());
             } catch (ComponentLookupException e) {
                 throw new InitializationException("Could not create or connect to built-in store");
             }
         }
 
         TextUtils.setLogger(this.logger);
         loadTopicsAndMails();
 
         this.isConfigured = true;
     }
 
     protected void loadTopicsAndMails() throws MailArchiveException
     {
         existingTopics = store.loadStoredTopics();
         existingMessages = store.loadStoredMessages();
     }
 
     @Override
     public Map<String, TopicDescriptor> getTopics() throws MailArchiveException
     {
         return store.loadStoredTopics();
     }
 
     @Override
     public Map<String, MailDescriptor> getMails() throws MailArchiveException
     {
         return store.loadStoredMessages();
     }
 
     public IType getType(String name)
     {
         return config.getMailTypes().get(name);
     }
 
     /**
      * @param m
      */
     public void setMailSpecificParts(final MailItem m)
     {
         logger.debug("Extracting types");
         try {
             // Types
             List<IType> foundTypes = mailutils.extractTypes(config.getMailTypes().values(), m);
             logger.debug("Extracted types " + foundTypes);
             // foundTypes.remove(getType(IType.BUILTIN_TYPE_MAIL));
             if (foundTypes.size() > 0) {
                 for (IType foundType : foundTypes) {
                     logger.debug("Adding extracted type " + foundType);
                     m.addType(foundType.getId());
                 }
             } /*
                * else { logger.debug("No specific type found for this mail");
                * m.addType(getType(IType.BUILTIN_TYPE_MAIL).getId()); }
                */
 
             // User
             logger.debug("Extracting user information");
             String userwiki = null;
             if (config.isMatchProfiles()) {
                 IMAUser maUser = mailutils.parseUser(m.getFrom(), config.isMatchLdap());
                 userwiki = maUser.getWikiProfile();
             }
             if (StringUtils.isBlank(userwiki)) {
                 if (config.isMatchProfiles()) {
                     userwiki = UNKNOWN_USER;
                 } else {
                     userwiki = config.getLoadingUser();
                 }
             }
             m.setWikiuser(userwiki);
 
             // If no topic id is provided by message, we default to message id
             if (StringUtils.isBlank(m.getTopicId())) {
                 m.setTopicId(m.getMessageId());
             }
 
             // Compatibility: crop ids
             if (config.isCropTopicIds() && m.getTopicId().length() >= 30) {
                 m.setTopicId(m.getTopicId().substring(0, 29));
             }
         } catch (Throwable t) {
             logger.warn("Exception ", t);
             t.printStackTrace();
         }
     }
 
     @Override
     public IMAUser parseUser(final String internetAddress)
     {
         logger.debug("parseUser {}", internetAddress);
         try {
             configure();
         } catch (Exception e) {
             logger.warn("parseUser: failed to configure the Mail Archive", e);
             return null;
         }
         IMAUser user = mailutils.parseUser(internetAddress, config.isMatchLdap());
         logger.debug("parseUser return {}", user);
         return user;
     }
 
     /**
      * @param mail
      * @param confirm
      * @param isAttachedMail
      * @param parentMail
      * @return
      * @throws XWikiException
      * @throws ParseException
      * @throws IOException
      * @throws MessagingException
      */
     @Override
     public MailLoadingResult loadMail(final Part mail, final boolean confirm, final boolean isAttachedMail,
         final String parentMail) throws XWikiException, ParseException, MessagingException, IOException
     {
         MailItem m = null;
 
         logger.warn("Parsing headers");
         m = mailManager.parseHeaders(mail);
         if (StringUtils.isBlank(m.getFrom())) {
             logger.warn("Invalid email : missing 'from' header, skipping it");
             return new MailLoadingResult(false, null, null);
         }
         logger.warn("Parsing specific parts");
         setMailSpecificParts(m);
         // Compatibility option with old version of the mail archive
         if (config.isCropTopicIds() && m.getTopicId().length() > 30) {
             m.setTopicId(m.getTopicId().substring(0, 29));
         }
         logger.warn("PARSED MAIL  " + m);
 
         return loadMail(m, confirm, isAttachedMail, parentMail);
     }
 
     /**
      * @param m
      * @param confirm
      * @param isAttachedMail
      * @param parentMail
      * @throws XWikiException
      * @throws ParseException
      */
     public MailLoadingResult loadMail(final MailItem m, final boolean confirm, final boolean isAttachedMail,
         final String parentMail) throws XWikiException, ParseException
     {
         String topicDocName = null;
         String messageDocName = null;
 
         logger.debug("Loading mail content into wiki objects");
 
         // set loading user for rights - loading user must have edit rights on IMailArchive and MailArchiveCode spaces
         context.setUser(config.getLoadingUser());
         logger.debug("Loading user " + config.getLoadingUser() + " set in context");
 
         SimpleDateFormat dateFormatter = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss ZZZZZ", m.getLocale());
 
         // Create a new topic if needed
         String existingTopicId = "";
         // we don't create new topics for attached emails
         if (!isAttachedMail) {
             existingTopicId =
                 existsTopic(m.getTopicId(), m.getTopic(), m.getReplyToId(), m.getMessageId(), m.getRefs());
             if (existingTopicId == null) {
                 logger.debug("  did not find existing topic, creating a new one");
                 if (existingTopics.containsKey(m.getTopicId())) {
                     // Topic hack ...
                     logger.debug("  new topic but topicId already loaded, using messageId as new topicId");
                     m.setTopicId(m.getMessageId());
                     // FIX: "cut" properly mail history when creating a new topic
                     m.setReplyToId("");
                     existingTopicId =
                         existsTopic(m.getTopicId(), m.getTopic(), m.getReplyToId(), m.getMessageId(), m.getRefs());
                 } else {
                     existingTopicId = m.getTopicId();
                 }
                 logger.debug("   creating new topic");
                 topicDocName = createTopicPage(m, confirm);
 
                 logger.debug("  loaded new topic " + topicDocName);
             } else if (textUtils.similarSubjects(m.getTopic(), existingTopics.get(existingTopicId).getSubject())) {
                 logger.debug("  topic already loaded " + m.getTopicId() + " : " + existingTopics.get(existingTopicId));
                 topicDocName = updateTopicPage(m, existingTopicId, dateFormatter, confirm);
             } else {
                 // We consider this was a topic hack : someone replied to an existing thread, but to start on another
                 // subject.
                 // In this case, we split, use messageId as a new topic Id, and set replyToId to empty string in order
                 // to treat this as a new topic to create.
                 // In order for this new thread to be correctly threaded, we search for existing topic with this new
                 // topicId,
                 // so now all new mails in this case will be attached to this new topic.
                 logger.debug("  found existing topic but subjects are too different, using new messageid as topicid ["
                     + m.getMessageId() + "]");
                 m.setTopicId(m.getMessageId());
                 m.setReplyToId("");
                 existingTopicId =
                     existsTopic(m.getTopicId(), m.getTopic(), m.getReplyToId(), m.getMessageId(), m.getRefs());
                 logger.debug("  creating new topic");
                 topicDocName = createTopicPage(m, confirm);
 
             }
         } // if not attached email
 
         // Create a new message if needed
         if (!existingMessages.containsKey(m.getMessageId())) {
             logger.debug("creating new message " + m.getMessageId() + " ...");
             /*
              * Note : use already existing topic id if any, instead of the one from the message, to keep an easy to
              * parse link between thread messages
              */
             if ("".equals(existingTopicId)) {
                 existingTopicId = m.getTopicId();
             }
             // Note : correction bug of messages linked to same topic but with different topicIds
             m.setTopicId(existingTopicId);
             try {
                 String parent = parentMail;
                 if (StringUtils.isBlank(parentMail)) {
                     parent = existingTopics.get(m.getTopicId()).getFullName();
                 }
                 messageDocName = createMailPage(m, existingTopicId, isAttachedMail, parent, confirm);
             } catch (Exception e) {
                 logger.error("Could not create mail page for " + m.getMessageId(), e);
                 return new MailLoadingResult(false, topicDocName, null);
             }
 
             return new MailLoadingResult(true, topicDocName, messageDocName);
         } else {
             // message already loaded
             logger.debug("Mail already loaded - checking for updates ...");
 
             MailDescriptor msg = existingMessages.get(m.getMessageId());
             logger.debug("TopicId of existing message " + msg.getTopicId() + " and of topic " + existingTopicId
                 + " are different ?" + (!msg.getTopicId().equals(existingTopicId)));
             if (!msg.getTopicId().equals(existingTopicId)) {
                 messageDocName = existingMessages.get(m.getMessageId()).getFullName();
                 XWikiDocument msgDoc = xwiki.getDocument(messageDocName, context);
                 BaseObject msgObj = msgDoc.getObject(XWikiPersistence.SPACE_CODE + ".MailClass");
                 msgObj.set("topicid", existingTopicId, context);
                 if (confirm) {
                     logger.debug("saving message " + m.getSubject());
                     persistence.saveAsUser(msgDoc, null, config.getLoadingUser(),
                         "Updated mail with existing topic id found");
                 }
             }
 
             return new MailLoadingResult(true, topicDocName, messageDocName);
         }
     }
 
     /**
      * createTopicPage Creates a wiki page for a Topic.
      * 
      * @throws XWikiException
      */
     protected String createTopicPage(final MailItem m, final boolean create) throws XWikiException
     {
         String pageName = "T" + m.getTopic().replaceAll(" ", "");
 
         // Materialize mailing-lists information and mail IType in Tags
         ArrayList<String> taglist = extractTags(m);
 
         String createdTopicName = persistence.createTopic(pageName, m, taglist, config.getLoadingUser(), create);
 
         // add the existing topic created to the map
         existingTopics.put(m.getTopicId(), new TopicDescriptor(createdTopicName, m.getTopic()));
 
         return createdTopicName;
     }
 
     protected String updateTopicPage(final MailItem m, final String existingTopicId,
         final SimpleDateFormat dateFormatter, final boolean create) throws XWikiException
     {
         logger.debug("updateTopicPage(" + m.toString() + ", existingTopicId=" + existingTopicId + ")");
 
         final String existingTopicPage = existingTopics.get(existingTopicId).getFullName();
         logger.debug("Topic page to update: " + existingTopicPage);
 
         String updatedTopicName =
             persistence.updateTopicPage(m, existingTopicPage, dateFormatter, config.getLoadingUser(), create);
 
         existingTopics.put(m.getTopicId(), new TopicDescriptor(updatedTopicName, m.getTopic()));
 
         return updatedTopicName;
     }
 
     protected String createMailPage(final MailItem m, final String existingTopicId, final boolean isAttachedMail,
         final String parentMail, final boolean create) throws XWikiException, MessagingException, IOException
     {
         // Materialize mailing-lists information and mail IType in Tags
         final String pageName = persistence.getMessageUniquePageName(m, isAttachedMail);
        // Parse mail content
        m.setMailContent(mailManager.parseContent(m.getOriginalMessage()));
         List<String> taglist = extractTags(m);
        // We load attachment emails first - so we can link them afterwards
         List<String> attachedMailPages = loadAttachedMails(m.getMailContent().getAttachedMails(), pageName, create);
         final String createdPageName =
             persistence.createMailPage(m, pageName, existingTopicId, isAttachedMail, taglist, attachedMailPages,
                 parentMail, config.getLoadingUser(), create);
         existingMessages.put(m.getMessageId(), new MailDescriptor(m.getSubject(), existingTopicId, createdPageName));
 
         return createdPageName;
     }
 
     private List<String> loadAttachedMails(final List<Message> attachedMails, final String parentFullName,
         final boolean create)
     {
         final List<String> attachedMailsPages = new ArrayList<String>();
         if (attachedMails.size() > 0) {
             logger.debug("Loading attached mails ...");
             for (Message message : attachedMails) {
                 try {
                     MailLoadingResult result = loadMail(message, create, true, parentFullName);
                     if (result.isSuccess()) {
                         attachedMailsPages.add(result.getCreatedMailDocumentName());
                     } else {
                         logger.warn("Could not create attached mail " + message.getSubject());
                     }
                 } catch (Exception e) {
                     logger.warn("Could not create attached mail because of " + e.getMessage());
                     if (logger.isDebugEnabled()) {
                         logger.debug("Could not create attached mail ", e);
                     }
                 }
             }
         }
         return attachedMailsPages;
     }
 
     protected ArrayList<String> extractTags(final MailItem m)
     {
         // Materialize mailing-lists information and mail IType in Tags
         ArrayList<String> taglist = extractMailingListsTags(m);
 
         for (String typeid : m.getTypes()) {
             IType type = config.getMailTypes().get(typeid);
             taglist.add(type.getName());
         }
         taglist.add(m.getBuiltinType());
 
         return taglist;
     }
 
     /**
      * @param m
      * @return
      */
     protected ArrayList<String> extractMailingListsTags(final MailItem m)
     {
         ArrayList<String> taglist = new ArrayList<String>();
 
         for (IMailingList list : config.getMailingLists().values()) {
             if (m.getFrom().contains(list.getDisplayName()) || m.getTo().contains(list.getPattern())
                 || m.getCc().contains(list.getPattern())) {
                 // Add tag of this mailing-list to the list of tags
                 taglist.add(list.getTag());
             }
         }
 
         return taglist;
     }
 
     /**
      * Returns the topicId of already existing topic for this topic id or subject. If no topic with this id or subject
      * is found, try to search for a message for wich msgid = replyid of new msg, then attach this new msg to the same
      * topic. If there is no existing topic, returns null. Search topic with same subject only if inreplyto is not
      * empty, meaning it's not supposed to be the first message of another topic.
      * 
      * @param topicId
      * @param topicSubject
      * @param inreplyto
      * @return
      */
     public String existsTopic(final String topicId, final String topicSubject, final String inreplyto,
         final String messageid, final String refs)
     {
         String foundTopicId = null;
         String replyId = inreplyto;
         String previous = "";
         String previousSubject = topicSubject;
         boolean quit = false;
 
         // Search in existing messages for existing msg id = new reply id, and grab topic id
         // search replies until root message
         while (StringUtils.isNotBlank(replyId) && existingMessages.containsKey(replyId)
             && existingMessages.get(replyId) != null && !quit) {
             XWikiDocument msgDoc = null;
             try {
                 msgDoc = context.getWiki().getDocument(existingMessages.get(replyId).getFullName(), context);
             } catch (XWikiException e) {
                 // TODO Auto-generated catch block
                 e.printStackTrace();
             }
             if (msgDoc != null) {
                 BaseObject msgObj = msgDoc.getObject(XWikiPersistence.SPACE_CODE + ".MailClass");
                 if (msgObj != null) {
                     logger
                         .debug("existsTopic : message " + replyId + " is a reply to " + existingMessages.get(replyId));
                     if (textUtils.similarSubjects(previousSubject, msgObj.getStringValue("topicsubject"))) {
                         previous = replyId;
                         replyId = msgObj.getStringValue("inreplyto");
                         previousSubject = msgObj.getStringValue("topicSubject");
                     } else {
                         logger.debug("existsTopic : existing message subject is too different, exiting loop");
                         quit = true;
                     }
                 } else {
                     replyId = null;
                 }
             } else {
                 replyId = null;
             }
         }
         if (replyId != inreplyto && replyId != null) {
             logger
                 .debug("existsTopic : found existing message that current message is a reply to, to attach to same topic id");
             foundTopicId = existingMessages.get(previous).getTopicId();
             logger.debug("existsTopic : Found topic id " + foundTopicId);
         }
         // Search in existing topics with id
         if (foundTopicId == null) {
             if (!StringUtils.isBlank(topicId) && existingTopics.containsKey(topicId)) {
                 logger.debug("existsTopic : found topic id in loaded topics");
                 if (textUtils.similarSubjects(topicSubject, existingTopics.get(topicId).getSubject())) {
                     foundTopicId = topicId;
                 } else {
                     logger.debug("... but subjects are too different");
                 }
             }
         }
         // Search with references
         if (foundTopicId == null) {
             String xwql =
                 "select distinct mail.topicid from Document doc, doc.object(" + XWikiPersistence.CLASS_MAILS
                     + ") as mail where mail.references like '%" + messageid + "%'";
             try {
                 List<String> topicIds = queryManager.createQuery(xwql, Query.XWQL).execute();
                 // We're not supposed to find several topics related to messages having this id in references ...
                 if (topicIds.size() == 1) {
                     foundTopicId = topicIds.get(0);
                 }
                 if (topicIds.size() > 1) {
                     logger.warn("We should have found only one topicId instead of this list : " + topicIds
                         + ", using the first found");
                 }
             } catch (QueryException e) {
                 logger.warn("Issue while searching for references", e);
             }
         }
         // Search in existing topics with exactly same subject
         if (foundTopicId == null) {
             for (String currentTopicId : existingTopics.keySet()) {
                 TopicDescriptor currentTopic = existingTopics.get(currentTopicId);
                 if (currentTopic.getSubject().trim().equalsIgnoreCase(topicSubject.trim())) {
                     logger.debug("existsTopic : found subject in loaded topics");
                     if (!StringUtils.isBlank(inreplyto)) {
                         logger.debug("existsTopic : not first message in topic, so we assume it's linked to it");
                         foundTopicId = currentTopicId;
                     } else {
                         logger.debug("existsTopic : found a topic but it's first message in topic");
                         // Note : desperate tentative to attach this message to an existing topic
                         // instead of creating a new one ... Sometimes replyId and refs can be
                         // empty even if this is a reply to something already loaded, in this
                         // case we just check if topicId was already loaded once, even if not
                         // the same topic ...
                         if (existingTopics.containsKey(topicId)) {
                             logger
                                 .debug("existsTopic : ... but we 'saw' this topicId before, so attach to found topicId "
                                     + currentTopicId + " with same subject");
                             foundTopicId = currentTopicId;
                         }
                         if (!StringUtils.isBlank(refs)) {
                             logger.debug("existsTopic : ... but references are not empty, so attach to found topicId "
                                 + currentTopicId + " with same subject");
                             foundTopicId = currentTopicId;
                         }
                     }
 
                 }
             }
         }
 
         return foundTopicId;
     }
 
     /**
      * {@inheritDoc}
      * 
      * @throws IOException
      * @throws XWikiException
      * @throws MailArchiveException
      * @throws InitializationException
      * @see org.xwiki.contrib.mailarchive.IMailArchive#getDecodedMailText(java.lang.String, boolean)
      */
     @SuppressWarnings("deprecation")
     @Override
     public DecodedMailContent getDecodedMailText(final String mailPage, final boolean cut) throws IOException,
         XWikiException, InitializationException, MailArchiveException
     {
         if (!this.isConfigured) {
             configure();
         }
         if (!StringUtils.isBlank(mailPage)) {
             XWikiDocument htmldoc = null;
             try {
                 htmldoc = xwiki.getDocument(mailPage, this.context);
             } catch (Throwable t) {
                 // FIXME Ugly workaround for "org.hibernate.SessionException: Session is closed!"
                 try {
                     htmldoc = xwiki.getDocument(mailPage, this.context);
                 } catch (Exception e) {
                     htmldoc = null;
                 }
             }
             if (htmldoc != null) {
                 BaseObject htmlobj = htmldoc.getObject("MailArchiveCode.MailClass");
                 if (htmlobj != null) {
                     String ziphtml = htmlobj.getLargeStringValue("bodyhtml");
                     String body = htmlobj.getLargeStringValue("body");
 
                     return (mailutils.decodeMailContent(ziphtml, body, cut));
                 }
             }
         }
 
         return new DecodedMailContent(false, "");
 
     }
 
     /**
      * @return the locked
      */
     @Override
     public boolean isLocked()
     {
         return locked;
     }
 
     /**
      * @param locked the locked to set
      */
     @Override
     public void setLocked(final boolean locked)
     {
         this.locked = locked;
     }
 
     @Override
     public void saveToInternalStore(final String serverId, final IMailSource source, final Message message)
     {
         // Save to internal store only if we did not already load this mail from internal store ...
         if (!StringUtils.isBlank(serverId) && !builtinStore.getMailSource().equals(source)) {
             try {
                 // Use server id as folder to avoid colliding folders from different servers
                 builtinStore.write(serverId, message);
             } catch (MessagingException e) {
                 logger.error("Can't copy mail to local store", e);
             }
         }
     }
 
     @Override
     public void dumpEmail(final Message message)
     {
         try {
             final String id =
                 JavamailMessageParser.extractSingleHeader(message, "Message-ID").replaceAll("[^a-zA-Z0-9-_\\.]", "_");
             final File emlFile = new File(environment.getPermanentDirectory(), "mailarchive/dump/" + id + ".eml");
 
             emlFile.createNewFile();
 
             final FileOutputStream fo = new FileOutputStream(emlFile);
             message.writeTo(fo);
             fo.flush();
             fo.close();
 
             logger.debug("Message dumped into " + id + ".eml");
         } catch (Throwable t) {
             // we catch Throwable because we don't want to cause problems in debug mode
             logger.debug("Could not dump message for debug", t);
         }
     }
 
 }
