 /*
  * Copyright 2012 INRIA
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package com.mymed.controller.core.manager.pubsub;
 
 import java.io.UnsupportedEncodingException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 
 import com.mymed.controller.core.exception.IOBackEndException;
 import com.mymed.controller.core.exception.InternalBackEndException;
 import com.mymed.controller.core.manager.AbstractManager;
 import com.mymed.controller.core.manager.storage.IStorageManager;
 import com.mymed.controller.core.manager.storage.StorageManager;
 import com.mymed.model.data.application.MDataBean;
 import com.mymed.model.data.user.MUserBean;
 import com.mymed.utils.MConverter;
 import com.mymed.utils.Mail;
 /**
  * Manage an user profile
  * 
  * @author lvanni
  * 
  */
 public class PubSubManager extends AbstractManager implements IPubSubManager {
 
   protected static final String APPLICATION_CONTROLLER_ID = "applicationControllerID";
   protected static final String PUBLISHER_PREFIX = "PUBLISH_";
   protected static final String SUBSCRIBER_PREFIX = "SUBSCRIBE_";
   protected static final String MEMBER_LIST_KEY = "memberList";
   protected static final String DATA_ARG = "data";
   protected static final String BEGIN_ARG = "begin";
   protected static final String END_ARG = "end";
 
   private static final String SC_APPLICATION_CONTROLLER = COLUMNS.get("column.sc.application.controller");
   private static final String SC_APPLICATION_MODEL = COLUMNS.get("column.sc.application.model");
   private static final String SC_USER_LIST = COLUMNS.get("column.sc.user.list");
   private static final String SC_DATA_LIST = COLUMNS.get("column.sc.data.list");
   private static final String CF_USER = COLUMNS.get("column.cf.user");
 
   public PubSubManager() {
     this(new StorageManager());
   }
 
   public PubSubManager(final IStorageManager storageManager) {
     super(storageManager);
   }
 
   /**
    * PUBLISH
    * 
    * @see IPubSubManager#create(String, String, MUserBean)
    */
   @Override
   public void create(final String application, final String predicate, final String subPredicate,
       final MUserBean publisher, final List<MDataBean> dataList) throws InternalBackEndException, IOBackEndException {
	  
     try {
       // STORE THE PUBLISHER
       final Map<String, byte[]> args = new HashMap<String, byte[]>();
       args.put("publisherList", (PUBLISHER_PREFIX + application + subPredicate).getBytes(ENCODING));
       args.put("predicate", subPredicate.getBytes(ENCODING));
       storageManager.insertSuperSlice(SC_APPLICATION_CONTROLLER, application + predicate, MEMBER_LIST_KEY, args);
 
       // STORE A NEW ENTRY IN THE UserList (PublisherList)
       args.clear();
       args.put("name", publisher.getName().getBytes(ENCODING));
       args.put("user", publisher.getId().getBytes(ENCODING));
       storageManager.insertSuperSlice(SC_USER_LIST, PUBLISHER_PREFIX + application + subPredicate, publisher.getId(),
           args);
 
       // STORE VALUES RELATED TO THE PREDICATE
       String data = "";
       String begin = Long.toString(System.currentTimeMillis());
       String end = "";
 
       for (final MDataBean item : dataList) {
         if (item.getKey().equals(DATA_ARG)) {
           data = item.getValue();
         } else if (item.getKey().equals(BEGIN_ARG)) {
           begin = item.getValue();
         } else if (item.getKey().equals(END_ARG)) {
           end = item.getValue();
         }
       }
 
       args.clear();
       args.put("predicate", subPredicate.getBytes(ENCODING));
       args.put("begin", begin.getBytes(ENCODING));
       args.put("end", end.getBytes(ENCODING));
       args.put("publisherID", publisher.getId().getBytes(ENCODING));
       args.put("publisherName", publisher.getName().getBytes(ENCODING));
       args.put("publisherProfilePicture",
           (publisher.getProfilePicture() == null ? "" : publisher.getProfilePicture()).getBytes(ENCODING));
       args.put("publisherReputation",
           (publisher.getReputation() == null ? "" : publisher.getReputation()).getBytes(ENCODING));
       args.put("data", data.getBytes(ENCODING));
       storageManager.insertSuperSlice(SC_APPLICATION_CONTROLLER, application + predicate,
           subPredicate + publisher.getId(), args);
 
       // STORE A NEW ENTRY IN THE ApplicationModel (use to retreive all the
       // predicate of a given application)
       args.clear();
       args.put(APPLICATION_CONTROLLER_ID, (application + predicate).getBytes(ENCODING));
       storageManager.insertSuperSlice(SC_APPLICATION_MODEL, application, predicate, args);
 
       // STORE THE DATAs
       args.clear();
       for (final MDataBean item : dataList) {
         args.put("key", item.getKey().getBytes(ENCODING));
         args.put("value", item.getValue().getBytes(ENCODING));
         args.put("ontologyID", item.getOntologyID().getBytes(ENCODING));
         storageManager.insertSuperSlice(SC_DATA_LIST, application + subPredicate + publisher.getId(), item.getKey(),
             args);
         args.clear();
       }
 
       // SEND A MAIL TO THE SUBSCRIBERS
       // TODO better find a better way to send the email: we are sending the
       // same email to all at once, exposing the users email
       final StringBuffer mailingList = new StringBuffer(250);
       final List<Map<byte[], byte[]>> subscribers = storageManager.selectList(SC_USER_LIST, SUBSCRIBER_PREFIX
          + application + predicate);
       for (final Map<byte[], byte[]> set : subscribers) {
         for (final Entry<byte[], byte[]> entry : set.entrySet()) {
           if (new String(entry.getKey(), ENCODING).equals("user")) {
             final String userID = new String(entry.getValue(), ENCODING);
             mailingList.append(new String(storageManager.selectColumn(CF_USER, userID, "email"), ENCODING));
             mailingList.append(',');
           }
         }
       }
 
       mailingList.trimToSize();
       final String receivers = mailingList.toString();
 
       // Format the mail
       // TODO Refactor and put this into another class (mail package should be
       // used)
       // TODO move this somewhere else and handle translation of this email!
       if (!"".equals(receivers)) {
         final StringBuffer mailContent = new StringBuffer(250);
         mailContent.append("Bonjour, \nDe nouvelles informations sont arrivées sur votre plateforme myMed.\n");
         mailContent.append("Application Concernée: ");
         mailContent.append(application);
         mailContent.append('\n');
         mailContent.append("Predicate: \n");
         for (final MDataBean item : dataList) {
           mailContent.append("\t-");
           mailContent.append(item.getKey());
           mailContent.append(": ");
           mailContent.append(item.getValue());
           mailContent.append('\n');
         }
         mailContent.append("\n------\nL'équipe myMed");
         mailContent.trimToSize();
         new Mail("mymed.subscribe@gmail.com", receivers, "myMed subscribe info: " + application, mailContent.toString());
       }
     } catch (final UnsupportedEncodingException e) {
       LOGGER.debug(ERROR_ENCODING, ENCODING, e);
       throw new InternalBackEndException(e.getMessage()); // NOPMD
     }
   }
   /**
    * SUBSCRIBE
    * 
    * @see IPubSubManager#create(String, String, MUserBean)
    */
   @Override
   public void create(final String application, final String predicate, final MUserBean subscriber)
       throws InternalBackEndException, IOBackEndException {
     try {
       // STORE A NEW ENTRY IN THE ApplicationController
       Map<String, byte[]> args = new HashMap<String, byte[]>();
       args.put("subscriberList", (SUBSCRIBER_PREFIX + application + predicate).getBytes(ENCODING));
       storageManager.insertSuperSlice(SC_APPLICATION_CONTROLLER, application + predicate, MEMBER_LIST_KEY, args);
 
       // STORE A NEW ENTRY IN THE UserList (SubscriberList)
       args = new HashMap<String, byte[]>();
       args.put("name", subscriber.getName().getBytes(ENCODING));
       args.put("user", subscriber.getId().getBytes(ENCODING));
       storageManager.insertSuperSlice(SC_USER_LIST, SUBSCRIBER_PREFIX + application + predicate, subscriber.getId(),
           args);
 
     } catch (final UnsupportedEncodingException e) {
       LOGGER.debug(ERROR_ENCODING, ENCODING, e);
       throw new InternalBackEndException(e.getMessage()); // NOPMD
     }
   }
 
   /**
    * FIND
    * 
    * @see IPubSubManager#read(String, String)
    */
   @Override
   public List<Map<String, String>> read(final String application, final String predicate)
       throws InternalBackEndException, IOBackEndException {
     final List<Map<String, String>> resList = new ArrayList<Map<String, String>>();
     final List<Map<byte[], byte[]>> subPredicateListMap = storageManager.selectList(SC_APPLICATION_CONTROLLER,
         application + predicate);
 
     for (final Map<byte[], byte[]> set : subPredicateListMap) {
       if (set.size() > 3) { // do not return the memberList
         final Map<String, String> resMap = new HashMap<String, String>();
         for (final Entry<byte[], byte[]> entry : set.entrySet()) {
           resMap.put(MConverter.byteArrayToString(entry.getKey()), MConverter.byteArrayToString(entry.getValue()));
         }
 
         resList.add(resMap);
       }
     }
 
     return resList;
   }
 
   /**
    * FIND - GET DETAILS
    * 
    * @see IPubSubManager#read(String, String)
    */
   @Override
   public List<Map<String, String>> read(final String application, final String predicate, final String userID)
       throws InternalBackEndException, IOBackEndException {
 
     final List<Map<byte[], byte[]>> list = storageManager.selectList(SC_DATA_LIST, application + predicate + userID);
     final List<Map<String, String>> resList = new ArrayList<Map<String, String>>();
     for (final Map<byte[], byte[]> set : list) {
       final Map<String, String> resMap = new HashMap<String, String>();
       for (final Entry<byte[], byte[]> entry : set.entrySet()) {
         try {
           resMap.put(new String(entry.getKey(), ENCODING), new String(entry.getValue(), ENCODING));
         } catch (final UnsupportedEncodingException e) {
           LOGGER.debug(ERROR_ENCODING, ENCODING, e);
           throw new InternalBackEndException(e.getMessage()); // NOPMD
         }
       }
 
       resList.add(resMap);
     }
 
     return resList;
   }
 
   /**
    * @see IPubSubManager#delete(String, String)
    */
   @Override
   public void delete(final String application, final String predicate, final MUserBean user)
       throws InternalBackEndException, IOBackEndException {
     storageManager.removeAll(SC_DATA_LIST, application + predicate + user.getId());
     storageManager.removeAll(SC_USER_LIST, PUBLISHER_PREFIX + application + predicate);
     storageManager.removeSuperColumn(SC_APPLICATION_CONTROLLER, application, predicate);
     storageManager.removeSuperColumn(SC_APPLICATION_MODEL, application, predicate + user.getId());
   }
 }
