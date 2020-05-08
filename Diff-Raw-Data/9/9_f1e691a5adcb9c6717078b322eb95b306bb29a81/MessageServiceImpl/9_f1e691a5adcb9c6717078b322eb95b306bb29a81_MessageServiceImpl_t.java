 /**
  * 
  */
 package org.synyx.minos.i18n.service;
 
 import static com.google.common.collect.Collections2.*;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.Enumeration;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Locale;
 import java.util.Map;
 import java.util.Properties;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.springframework.transaction.annotation.Transactional;
 import org.springframework.util.StringUtils;
 import org.synyx.messagesource.util.LocaleUtils;
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
 import org.synyx.minos.i18n.web.LocaleInformation;
 import org.synyx.minos.i18n.web.MessageView;
 
 import com.google.common.base.Predicate;
 import com.google.common.collect.ImmutableList;
 
 
 /**
  * Service-Implementation for managing I18n Messages
  * 
  * @author Marc Kannegiesser - kannegiesser@synyx.de
  */
 public class MessageServiceImpl implements MessageService {
 
     private static final Log LOG = LogFactory.getLog(MessageServiceImpl.class);
 
     private MessageDao messageDao;
 
     private AvailableLanguageDao availableLanguageDao;
 
     private AvailableMessageDao availableMessageDao;
 
     private MessageTranslationDao messageTranslationDao;
 
    private Locale defaultLocale;
 
 
     public MessageServiceImpl(MessageDao messageDao, AvailableLanguageDao availableLanguageDao,
             AvailableMessageDao availableMessageDao, MessageTranslationDao messageTranslationDao) {
 
         this.messageDao = messageDao;
         this.availableLanguageDao = availableLanguageDao;
         this.availableMessageDao = availableMessageDao;
         this.messageTranslationDao = messageTranslationDao;
     }
 
 
     /*
      * (non-Javadoc)
      * 
      * @see org.synyx.minos.i18n.service.MessageService#save(org.synyx.minos.i18n.domain.Message)
      */
     @Override
     @Transactional
     public void save(Message message, boolean finished) {
 
         if (finished) {
             removeTranslationInfo(message);
         }
 
         if (!message.getLocale().isDefault()) {
            
            List<Locale> parentPath = LocaleUtils.getPath(message.getLocale().getLocale(), defaultLocale);
            Locale parent = parentPath.get(1); // 0 is the current one, 1 is the parent and guaranteed to be set here
            
            
             Message parentMessage = getMessageEntity(message.getBasename(), message.getKey(), parent);
             if (parentMessage.getMessage().equals(message.getMessage())) {
                 // skip messages that dont have differences to their parent
                 if (!message.isNew()) {
                     LOG.info("Removing message since it equals its parent: " + message.toString());
                     messageDao.delete(messageDao.readByPrimaryKey(message.getId()));
                 }
                 return;
 
             }
         }
 
         if (StringUtils.hasLength(message.getMessage())) {
             messageDao.save(message);
 
         } else if (!message.isNew()) {
             LOG.info("Removing message since its empty: " + message.toString());
             messageDao.delete(messageDao.readByPrimaryKey(message.getId()));
         }
 
     }
 
 
     private Message getMessageEntity(String basename, String key, Locale loc) {
 
         List<Locale> path = LocaleUtils.getPath(loc, defaultLocale);
         for (Locale locale : path) {
 
             Message message =
                     CollationUtils.getRealMatch(messageDao.findByBasenameAndLocaleAndKey(basename, new LocaleWrapper(
                             locale), key), "key", key);
 
             if (message != null) {
                 return message;
             }
 
         }
 
         return null;
 
     }
 
 
     /*
      * (non-Javadoc)
      * 
      * @see org.synyx.minos.i18n.service.MessageService#getBasenames()
      */
     @Override
     public List<String> getBasenames() {
 
         return messageDao.findBasenames();
     }
 
 
     /*
      * (non-Javadoc)
      * 
      * @see org.synyx.minos.i18n.service.MessageService#getLocales(java.lang.String)
      */
     @Override
     @Transactional(readOnly = true)
     public List<LocaleWrapper> getLocales(String basename) {
 
         List<AvailableLanguage> languages = availableLanguageDao.findByBasename(basename);
         List<LocaleWrapper> locales = new ArrayList<LocaleWrapper>();
 
         for (AvailableLanguage lang : languages) {
             locales.add(lang.getLocale());
         }
         return locales;
 
     }
 
 
     @Override
     @Transactional(readOnly = true)
     public List<LocaleInformation> getLocaleInformations(String basename) {
 
         List<LocaleInformation> infos = new ArrayList<LocaleInformation>();
         for (LocaleWrapper locale : getLocales(basename)) {
             Long newCount = messageTranslationDao.countByStatus(basename, locale, MessageStatus.NEW);
             Long updatedCount = messageTranslationDao.countByStatus(basename, locale, MessageStatus.UPDATED);
             Long totalCount = messageDao.countByBasenameAndLocale(basename, locale);
             AvailableLanguage language = availableLanguageDao.findByBasenameAndLocale(basename, locale);
 
             infos.add(new LocaleInformation(language, newCount, updatedCount, totalCount));
         }
 
         return infos;
 
     }
 
 
     /*
      * (non-Javadoc)
      * 
      * @see org.synyx.minos.i18n.service.MessageService#getMessagesHierarchically(java.lang.String, java.util.Locale)
      */
     @Override
     public List<MessageView> getMessages(String basename, Locale locale) {
 
         List<MessageView> result = new ArrayList<MessageView>();
 
         List<String> knownKeys = messageDao.getKnownKeys(basename);
 
         List<Map<String, Message>> messageChain = getMessageChain(basename, locale);
 
         for (String key : knownKeys) {
 
             MessageView message = getMessage(basename, key, locale, messageChain);
             if (message != null) {
                 result.add(message);
             }
         }
 
         Collections.sort(result, new Comparator<MessageView>() {
 
             @Override
             public int compare(MessageView o1, MessageView o2) {
 
                 return o1.getMessage().getKey().compareTo(o2.getMessage().getKey());
             }
         });
 
         return result;
     }
 
 
     /*
      * (non-Javadoc)
      * 
      * @see org.synyx.minos.i18n.service.MessageService#getMessages(java.lang.String, java.util.Locale, boolean,
      * boolean)
      */
     @Override
     public List<MessageView> getMessages(String basename, Locale locale, boolean includeNew, boolean includeUpdated) {
 
         List<MessageView> allMessages = getMessages(basename, locale);
 
         if (!includeNew && !includeUpdated) {
             return allMessages;
         }
 
         return ImmutableList.copyOf(filter(allMessages, new MessageViewStatusPredicate(includeNew, includeUpdated)));
     }
 
 
     /*
      * (non-Javadoc)
      * 
      * @see org.synyx.minos.i18n.service.MessageService#getMessage(String, String, Locale)
      */
     @Override
     public MessageView getMessage(String basename, String key, Locale locale) {
 
         // TODO think about performance
         List<Map<String, Message>> messageChain = getMessageChain(basename, locale);
 
         return getMessage(basename, key, locale, messageChain);
     }
 
 
     /*
      * (non-Javadoc)
      * 
      * @see org.synyx.minos.i18n.service.MessageService#getMetaMessage(java.lang.String, java.lang.String)
      */
     @Override
     public AvailableMessage getAvailableMessage(String basename, String key) {
 
         AvailableMessage availableMessage =
                 CollationUtils.getRealMatch(availableMessageDao.findByBasenameAndKey(basename, key), "key", key);
 
         if (null == availableMessage) {
             throw new IllegalArgumentException("No Available Message for basename: " + basename + " and key: " + key
                     + " !");
         }
 
         return availableMessage;
     }
 
 
     private MessageView getMessage(String basename, String key, Locale locale, List<Map<String, Message>> messageChain) {
 
         // retrieve message (setting the possibly found reference message)
         MessageView messageView = null;
         for (Map<String, Message> messages : messageChain) {
             if (messages.containsKey(key)) {
 
                 Message message = null;
                 LocaleWrapper resolvingLocale = new LocaleWrapper(locale);
 
                 // if the messages locale does not equal the requested locale, create a new message entity prefilled
                 // by the resolved message text and requested locale
                 if (!resolvingLocale.equals(messages.get(key).getLocale())) {
                     Message resolvedMessage = messages.get(key);
                     resolvingLocale = resolvedMessage.getLocale();
 
                     message = new Message(locale, basename, key, resolvedMessage.getMessage());
                 } else {
                     message = messages.get(key);
                 }
 
                 MessageTranslation translation = getTranslationInformation(basename, key, locale);
                 messageView = new MessageView(resolvingLocale, message, translation);
                 break;
             }
         }
 
         return messageView;
     }
 
 
     private MessageTranslation getTranslationInformation(String basename, String key, Locale locale) {
 
         AvailableLanguage lang = availableLanguageDao.findByBasenameAndLocale(basename, new LocaleWrapper(locale));
         AvailableMessage message =
                 CollationUtils.getRealMatch(availableMessageDao.findByBasenameAndKey(basename, key), "key", key);
 
         if (lang == null || message == null) {
             throw new IllegalArgumentException("Given combination of basename (" + basename + "), locale (" + locale
                     + ") and key (" + key + ") is invalid.");
         }
 
         return messageTranslationDao.findByAvailableMessageAndAvailableLanguage(message, lang);
 
     }
 
 
     private List<Map<String, Message>> getMessageChain(String basename, Locale locale) {
 
         List<Locale> path = LocaleUtils.getPath(locale, defaultLocale);
 
         List<Map<String, Message>> chain = new ArrayList<Map<String, Message>>();
         for (Locale loc : path) {
             chain.add(toMap(messageDao.findByBasenameAndLocale(basename, new LocaleWrapper(loc))));
         }
 
         return chain;
 
     }
 
 
     private Map<String, Message> toMap(List<Message> messages) {
 
         Map<String, Message> map = new HashMap<String, Message>(messages.size());
         for (Message message : messages) {
             map.put(message.getKey(), message);
         }
 
         return map;
     }
 
 
     /*
      * (non-Javadoc)
      * 
      * @see org.synyx.minos.i18n.service.MessageService#addLanguage(java.lang.String,
      * org.synyx.minos.i18n.domain.LocaleWrapper)
      */
     @Override
     @Transactional
     public void addLanguage(AvailableLanguage language) {
 
         if (availableLanguageDao.findByBasenameAndLocale(language.getBasename(), language.getLocale()) == null) {
 
             LOG.info("Creating new language: " + language.toString());
             language = availableLanguageDao.save(language);
 
             if (language.isRequired()) {
 
                 List<AvailableMessage> messages = availableMessageDao.findByBasename(language.getBasename());
                 for (AvailableMessage message : messages) {
                     MessageTranslation t = new MessageTranslation(message, language, MessageStatus.NEW);
                     messageTranslationDao.save(t);
                 }
 
                 LOG.info("Language is required. Added " + messages.size() + " translation-informations: "
                         + language.toString());
             }
         }
 
     }
 
 
     /*
      * (non-Javadoc)
      * 
      * @see org.synyx.minos.i18n.service.MessageService#removeTranslationInfo(org.synyx.minos.i18n.domain.Message)
      */
     @Override
     @Transactional
     public void removeTranslationInfo(Message message) {
 
         LOG.info("Removing translation information for " + message.toString());
 
         AvailableMessage availableMessage =
 
                 CollationUtils.getRealMatch(availableMessageDao.findByBasenameAndKey(message.getBasename(), message
                         .getKey()), "key", message.getKey());
 
         AvailableLanguage availableLanguage =
                 availableLanguageDao.findByBasenameAndLocale(message.getBasename(), message.getLocale());
 
         if (availableMessage == null || availableLanguage == null) {
             throw new IllegalStateException(
                     "Missing available Message and/or available Language entry for given message!");
         }
 
         messageTranslationDao.deleteBy(availableMessage, availableLanguage);
     }
 
 
     @Transactional
     @Override
     public void removeLanguage(String basename, LocaleWrapper locale) {
 
         AvailableLanguage language = availableLanguageDao.findByBasenameAndLocale(basename, locale);
         LOG.info("Removing language as well as translations and messages for it: " + language.toString());
 
         messageTranslationDao.deleteByAvailableLanguage(language);
         messageDao.deleteBy(language.getBasename(), language.getLocale());
         availableLanguageDao.delete(language);
 
     }
 
 
     public void setDefaultLocale(Locale defaultLocale) {
 
         this.defaultLocale = defaultLocale;
     }
 
     /**
      * Preticate that is able to filter {@link MessageView}s by their status
      * 
      * @author Alexander Menz - menz@synyx.de
      */
     private class MessageViewStatusPredicate implements Predicate<MessageView> {
 
         private boolean includeNew = false;
         private boolean includeUpdated = false;
 
 
         public MessageViewStatusPredicate(boolean includeNew, boolean includeUpdated) {
 
             super();
 
             this.includeNew = includeNew;
             this.includeUpdated = includeUpdated;
         }
 
 
         @Override
         public boolean apply(MessageView messageView) {
 
             if (messageView.getTranslation() == null) {
                 return false;
             }
 
             if (includeNew && MessageStatus.NEW.equals(messageView.getTranslation().getMessageStatus())) {
                 return true;
             }
 
             if (includeUpdated && MessageStatus.UPDATED.equals(messageView.getTranslation().getMessageStatus())) {
                 return true;
             }
 
             return false;
         }
 
     }
 
 
     /*
      * (non-Javadoc)
      * 
      * @see org.synyx.minos.i18n.service.MessageService#saveAll(org.synyx.minos.i18n.domain.AvailableLanguage,
      * java.util.Properties)
      */
     @Override
     @Transactional
     public void saveAll(AvailableLanguage language, Properties properties) {
 
         LOG.info("Batch-Saving " + properties.size() + " translations of language " + language.toString());
         String basename = language.getBasename();
         LocaleWrapper locale = language.getLocale();
 
         Enumeration<Object> keys = properties.keys();
         while (keys.hasMoreElements()) {
             String key = (String) keys.nextElement();
             String value = (String) properties.getProperty(key);
 
             // check if the key is "known" for the given basename.
             if (CollationUtils.getRealMatch(availableMessageDao.findByBasenameAndKey(basename, key), "key", key) == null) {
                 // TODO think about "invalid" keys
                 continue;
             }
 
             // see if there is already a message for the given key and locale
 
             Message message =
                     CollationUtils.getRealMatch(messageDao.findByBasenameAndLocaleAndKey(basename, locale, key), "key",
                             key);
 
             if (message == null) {
                 // if not create
                 message = new Message(locale, basename, key, value);
             } else {
 
                 // otherwise update
                 message.setMessage(value);
             }
 
             save(message, true);
         }
 
     }
 
 }
