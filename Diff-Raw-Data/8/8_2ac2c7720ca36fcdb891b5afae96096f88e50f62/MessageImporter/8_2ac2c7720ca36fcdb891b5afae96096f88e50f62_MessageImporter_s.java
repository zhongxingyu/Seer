 package org.synyx.minos.i18n.importer;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 import org.springframework.core.io.Resource;
 
 import org.synyx.minos.i18n.dao.AvailableLanguageDao;
 import org.synyx.minos.i18n.dao.AvailableMessageDao;
 import org.synyx.minos.i18n.dao.MessageDao;
 import org.synyx.minos.i18n.dao.MessageTranslationDao;
 import org.synyx.minos.i18n.domain.AvailableLanguage;
 import org.synyx.minos.i18n.domain.AvailableMessage;
 import org.synyx.minos.i18n.domain.LocaleWrapper;
 import org.synyx.minos.i18n.domain.Message;
 import org.synyx.minos.i18n.domain.MessageStatus;
 import org.synyx.minos.i18n.domain.MessageTranslation;
 import org.synyx.minos.i18n.util.CollationUtils;
 
 import java.io.IOException;
 
 import java.util.Enumeration;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Properties;
 
 
 /**
 * This class provides the functionalities to import and update messages for basenames from property resources. Beside
  * offering the import of a specific property resource for a specific basename, a map assigning property resources to
  * basenames can be set (i.e. via spring beans config) to provide a method for importing/updating messages on
  * application boot up.
  *
  * @author Marc Kannegiesser - kannegiesser@synyx.de
  */
 public class MessageImporter {
 
     private static final Log LOG = LogFactory.getLog(MessageImporter.class);
 
     private Map<String, Resource> resources;
 
     private MessageDao messageDao;
 
     private AvailableMessageDao availableMessageDao;
 
     private AvailableLanguageDao availableLanguageDao;
 
     private MessageTranslationDao messageTranslationDao;
 
     /**
      * Empty constructor. Makes this class accessible for wrapping or aspect proxies.
      */
     protected MessageImporter() {
     }
 
 
     /**
      * Creates a new instance of {@link MessageImporter}.
      *
      * @param messageDao
      * @param availableMessageDao
      * @param availableLanguageDao
      * @param messageTranslationDao
      */
     public MessageImporter(MessageDao messageDao, AvailableMessageDao availableMessageDao,
         AvailableLanguageDao availableLanguageDao, MessageTranslationDao messageTranslationDao) {
 
         super();
         this.messageDao = messageDao;
         this.availableMessageDao = availableMessageDao;
         this.availableLanguageDao = availableLanguageDao;
         this.messageTranslationDao = messageTranslationDao;
     }
 
     /**
      * Import messages from the given property resource for the given basename. Messages already existing for a
      * combination of basename and key are updated, new combinations are created. Additionally new instances of
      * {@link MessageTranslation} are created for each required {@link AvailableLanguage} or already defined message
      * indicating that the translation for these languages need to be checked.
      *
      * @param basename the basename to import the messages for.
      * @param resource the property resource to import the messages from.
      */
     public void importMessages(String basename, Resource resource) {
 
         Map<String, String> messages = loadProperties(resource);
 
         List<AvailableLanguage> availableLanguages = getAvailableLanguages(basename);
 
         // update/create messages
        for (String key : messages.keySet()) {
            String message = messages.get(key);
            setMessage(basename, key, message, availableLanguages);
         }
 
         // find deleted message meta
         List<AvailableMessage> availableMessages = availableMessageDao.findByBasename(basename);
 
         for (AvailableMessage availableMessage : availableMessages) {
             String key = availableMessage.getKey();
 
             if (!messages.containsKey(key)) {
                 // remove translationinfo
                 messageTranslationDao.deleteBy(availableMessage);
 
                 // and info about the key
                 availableMessageDao.delete(availableMessage);
 
                 // and all messages
                 messageDao.deleteBy(basename, key);
 
                 LOG.debug("Deleted meta-information and all translation-requests for basename: " + basename + " key: "
                     + key);
             }
         }
     }
 
 
     /**
      * Import a single message for the given basename and key. If the combination of basename and key already exists the
      * message is updated. Otherwise a new {@link AvailableMessage} entry for the given basename and key and a new
      * {@link Message} entry for the base language is created. Additionally a new instances of
      * {@link MessageTranslation} are created for all required {@link AvailableLanguage}s and already existing messages
      * respectively, indicating that the translation for these {@link AvailableLanguage}s need to be checked.
      *
      * @param basename the basename.
      * @param key the key.
      * @param message the message text.
      * @param availableLanguages list of {@link AvailableLanguage}s for which to check for needed translation check.
      */
     protected void setMessage(String basename, String key, String message, List<AvailableLanguage> availableLanguages) {
 
         AvailableMessage availableMessage = CollationUtils.getRealMatch(availableMessageDao.findByBasenameAndKey(
                     basename, key), "key", key);
 
         MessageStatus status = null;
 
         if (null == availableMessage) { // new
             availableMessage = new AvailableMessage(basename, key, message);
             availableMessage = availableMessageDao.save(availableMessage);
 
             // create a (base) message so that the keys get resolved
             messageDao.save(new Message("", "", "", basename, key, message));
             LOG.debug("Added new message for basename: " + basename + " and key: " + key);
             status = MessageStatus.NEW;
         } else if (!availableMessage.getMessage().equals(message)) { // updated
             status = MessageStatus.UPDATED;
 
             availableMessage.setMessage(message);
             availableMessageDao.save(availableMessage);
             LOG.debug("Updated existing message for basename: " + basename + " and key: " + key);
         } else { // unchanged
             return;
         }
 
         // now update the information what has to be translated
         for (AvailableLanguage lang : availableLanguages) {
             MessageTranslation translation = messageTranslationDao.findByAvailableMessageAndAvailableLanguage(
                     availableMessage, lang);
 
             if (translation == null) {
                 // if the language is not required we need to check if the key is defined
                 if (!lang.isRequired()) {
                     boolean found = false;
 
                     // check if the key is definied in the current language
                     List<Message> messages = messageDao.findByBasenameAndLocaleAndKey(basename, lang.getLocale(), key);
 
                     for (Message msg : messages) {
                         if (msg.getKey().equals(key)) {
                             found = true;
 
                             break;
                         }
                     }
 
                     // if not skip translation for the language
                     if (!found) {
                         continue;
                     }
                 }
 
                 translation = new MessageTranslation(availableMessage, lang, status);
             }
 
             translation.setMessageStatus(status);
 
             messageTranslationDao.save(translation);
             LOG.debug("Added/Updated new translation (" + lang.getLocale().toString() + ")for basename: " + basename
                 + " key: " + key);
         }
     }
 
 
     /**
      * Get all {@link AvailableLanguage}s for the given basename. If there is no {@link AvailableLanguage} for the given
      * basename, a new {@link AvailableLanguage} for the basename and default language is created.
      *
      * @param basename the basename to retrieve all {@link AvailableLanguage}s for.
      * @return all {@link AvailableLanguage}s for the given basename.
      */
     private List<AvailableLanguage> getAvailableLanguages(String basename) {
 
         List<AvailableLanguage> availableLanguages = availableLanguageDao.findByBasename(basename);
 
         if (availableLanguages.isEmpty()) {
             // first time we "see" this basename
             // create a "default" language entry for it
 
             AvailableLanguage lang = new AvailableLanguage(LocaleWrapper.DEFAULT, basename, true);
             availableLanguageDao.save(lang);
             availableLanguages = availableLanguageDao.findByBasename(basename);
 
             LOG.info("Created new entry for default-language of new basename: " + basename);
         }
 
         return availableLanguages;
     }
 
 
     /**
      * Loads {@link Map} of Key=Value pairs from the given {@link Resource} while handling errors.
      *
      * @param resource the resource to load from.
      */
     private Map<String, String> loadProperties(Resource resource) {
 
         Properties properties = null;
 
         try {
             properties = new Properties();
             properties.load(resource.getInputStream());
         } catch (IOException e) {
             throw new RuntimeException("Could not load messages from " + resource.toString() + ": " + e.getMessage(),
                 e);
         }
 
         Map<String, String> messages = new HashMap<String, String>(properties.size());
 
         Enumeration<Object> keys = properties.keys();
 
         while (keys.hasMoreElements()) {
             String key = (String) keys.nextElement();
             String value = (String) properties.getProperty(key);
             messages.put(key, value);
         }
 
         return messages;
     }
 
 
     /**
      * Import all messages from the set map of basenames to {@link Resource}s.
      *
      * @see MessageImporter#setResources(Map)
      */
     public void importMessages() {
 
         for (String resourceName : resources.keySet()) {
             importMessages(resourceName, resources.get(resourceName));
         }
     }
 
 
     /**
      * Get the map of basenames to {@link Resource}s.
      *
      * @return the map of basenames to {@link Resource}s.
      */
     public Map<String, Resource> getResources() {
 
         return resources;
     }
 
 
     /**
      * Set the map of basenames to {@link Resource}s to import from.
      *
      * @param resources the map of basenames to {@link Resource} to set.
      * @see MessageImporter#importMessages()
      */
     public void setResources(Map<String, Resource> resources) {
 
         this.resources = resources;
     }
 }
