 package com.mymed.controller.core.manager.registration;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 
 import com.google.gson.Gson;
 import com.google.gson.JsonSyntaxException;
 import com.mymed.controller.core.exception.AbstractMymedException;
 import com.mymed.controller.core.exception.InternalBackEndException;
 import com.mymed.controller.core.manager.AbstractManager;
 import com.mymed.controller.core.manager.authentication.AuthenticationManager;
 import com.mymed.controller.core.manager.authentication.IAuthenticationManager;
 import com.mymed.controller.core.manager.pubsub.IPubSubManager;
 import com.mymed.controller.core.manager.pubsub.PubSubManager;
 import com.mymed.controller.core.manager.storage.IStorageManager;
 import com.mymed.controller.core.manager.storage.StorageManager;
 import com.mymed.model.data.application.MDataBean;
 import com.mymed.model.data.session.MAuthenticationBean;
 import com.mymed.model.data.user.MUserBean;
 import com.mymed.utils.HashFunction;
 import com.mymed.utils.mail.Mail;
 import com.mymed.utils.mail.MailMessage;
 import com.mymed.utils.mail.SubscribeMailSession;
 
 /**
  * Handles the user registrations in myMed.
  * 
  * @author lvanni
  */
 public class RegistrationManager extends AbstractManager implements IRegistrationManager {
     /**
      * The general name of the application responsible for registering a user.
      */
     private static final String APP_NAME = GENERAL.get("general.social.network.app");
 
     /**
      * The 'user' field.
      */
     private static final String FIELD_USER = FIELDS.get("field.user");
 
     /**
      * The 'authentication' field.
      */
     private static final String FIELD_AUTHENTICATION = FIELDS.get("field.authentication");
 
     /**
      * The 'publisherID' field.
      */
     private static final String FIELD_PUBLISHER_ID = FIELDS.get("field.publisher.id");
 
     /**
      * The 'key' field.
      */
     private static final String FIELD_KEY = FIELDS.get("field.key");
 
     /**
      * The 'value' field.
      */
     private static final String FIELD_VALUE = FIELDS.get("field.value");
 
     /**
      * The default ontology id.
      */
     private static final String ONTOLOGY_ID = "0";
 
     private final IPubSubManager pubSubManager;
     private final IAuthenticationManager authenticationManager;
     private final Gson gson;
 
     /**
      * Default constructor.
      * 
      * @throws InternalBackEndException
      */
     public RegistrationManager() throws InternalBackEndException {
         this(new StorageManager());
     }
 
     /**
      * Default constructor.
      * 
      * @param storageManager
      * @throws InternalBackEndException
      */
     public RegistrationManager(final IStorageManager storageManager) throws InternalBackEndException {
         super(storageManager);
 
         pubSubManager = new PubSubManager();
         authenticationManager = new AuthenticationManager();
         gson = new Gson();
     }
 
     /*
      * (non-Javadoc)
      * @see
      * com.mymed.controller.core.manager.registration.IRegistrationManager#create(com.mymed.model.data.user.MUserBean,
      * com.mymed.model.data.session.MAuthenticationBean, java.lang.String)
      */
     @Override
     public void create(final MUserBean user, final MAuthenticationBean authentication, final String application)
                     throws AbstractMymedException {
         // PUBLISH A NEW REGISTATION PENDING TASK
         final List<MDataBean> dataList = new ArrayList<MDataBean>();
         try {
             final MDataBean dataUser = new MDataBean();
             dataUser.setKey(FIELD_USER);
             dataUser.setOntologyID(ONTOLOGY_ID);
             dataUser.setValue(gson.toJson(user));
 
             final MDataBean dataAuthentication = new MDataBean();
             dataAuthentication.setKey(FIELD_AUTHENTICATION);
             dataAuthentication.setOntologyID(ONTOLOGY_ID);
             dataAuthentication.setValue(gson.toJson(authentication));
 
             dataList.add(dataUser);
             dataList.add(dataAuthentication);
         } catch (final JsonSyntaxException e) {
             throw new InternalBackEndException("User/Authentication jSon format is not valid");
         }
 
         // We use the APP_NAME as the epsilon for the hash function
         final HashFunction hashFunc = new HashFunction(APP_NAME);
         final String accessToken = hashFunc.SHA1ToString(user.getLogin() + System.currentTimeMillis());
 
         pubSubManager.create(APP_NAME, accessToken, accessToken, user, dataList);
 
         final StringBuilder contentBuilder = new StringBuilder(250);
         // TODO add internationalization support
        contentBuilder.append("Bienvenu sur myMed.<br/><br/>Pour finaliser votre inscription allez sur <a href='");
         contentBuilder.append(getServerProtocol());
         contentBuilder.append(getServerURI());
 
         if (application != null) {
             contentBuilder.append("/application/" + application);
         }
 
         contentBuilder.append("?registration=ok&accessToken=");
         contentBuilder.append(accessToken);
        contentBuilder.append("'>"+application+"</a><br/><br/>------<br/>L'&eacute;quipe myMed");
 
         contentBuilder.trimToSize();
 
         final MailMessage message = new MailMessage();
         message.setSubject("Bienvenu sur myMed");
         message.setRecipient(user.getEmail());
         message.setText(contentBuilder.toString());
 
         final Mail mail = new Mail(message, SubscribeMailSession.getInstance());
         mail.send();
     }
 
     /*
      * (non-Javadoc)
      * @see com.mymed.controller.core.manager.registration.IRegistrationManager#read(java.lang.String)
      */
     @Override
     public void read(final String accessToken) throws AbstractMymedException {
         // Retrieve the user profile
         final String userID = pubSubManager.read(APP_NAME, accessToken).get(0).get(FIELD_PUBLISHER_ID);
         final List<Map<String, String>> dataList = pubSubManager.read(APP_NAME, accessToken, userID);
         MUserBean userBean = null;
         MAuthenticationBean authenticationBean = null;
 
         try {
             for (final Map<String, String> dataEntry : dataList) {
                 if (dataEntry.get(FIELD_KEY).equals(FIELD_USER)) {
                     userBean = gson.fromJson(dataEntry.get(FIELD_VALUE), MUserBean.class);
                 } else if (dataEntry.get(FIELD_KEY).equals(FIELD_AUTHENTICATION)) {
                     authenticationBean = gson.fromJson(dataEntry.get(FIELD_VALUE), MAuthenticationBean.class);
                 }
             }
         } catch (final JsonSyntaxException e) {
             LOGGER.debug("JSON string is not valid", e);
             throw new InternalBackEndException("User/Authentication jSon format is not valid"); // NOPMD
         }
 
         // register the new user
         if ((userBean != null) && (authenticationBean != null)) {
             authenticationManager.create(userBean, authenticationBean);
             // delete pending tasks
             pubSubManager.delete(APP_NAME, accessToken, accessToken, userBean);
         }
     }
 }
