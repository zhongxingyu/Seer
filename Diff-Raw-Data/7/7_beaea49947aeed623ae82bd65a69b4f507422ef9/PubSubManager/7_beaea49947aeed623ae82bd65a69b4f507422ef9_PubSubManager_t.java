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
 import com.mymed.utils.Mail;
 
 /**
  * Manage an user profile
  * 
  * @author lvanni
  * 
  */
 public class PubSubManager extends AbstractManager implements IPubSubManager {
 
 	/* --------------------------------------------------------- */
 	/* Constructors */
 	/* --------------------------------------------------------- */
 	public PubSubManager() throws InternalBackEndException {
 		this(new StorageManager());
 	}
 
 	public PubSubManager(final IStorageManager storageManager)
 			throws InternalBackEndException {
 		super(storageManager);
 	}
 
 	/* --------------------------------------------------------- */
 	/* implements IPubSubManager */
 	/* --------------------------------------------------------- */
 	/**
 	 * PUBLISH
 	 * @see IPubSubManager#create(String, String, MUserBean)
 	 */
 	@Override
 	public void create(String application, String predicate, String subPredicate, MUserBean publisher, List<MDataBean> dataList)
 			throws InternalBackEndException, IOBackEndException {
 		
 		try {
 			// STORE A NEW ENTRY IN THE ApplicationController
 			Map<String, byte[]> args = new HashMap<String, byte[]>();
 			args.put("publisherList", ("PUBLISH_" + application + subPredicate).getBytes("UTF-8"));
 			args.put("predicate", subPredicate.getBytes("UTF-8"));
 			storageManager.insertSuperSlice(SC_APPLICATION_CONTROLLER, application + predicate, subPredicate, args);
			
			// store the new predicate (use to retreive all the predicate of a given application)
			args = new HashMap<String, byte[]>();
			args.put("predicate", predicate.getBytes("UTF-8"));
			storageManager.insertSuperSlice(SC_APPLICATION_CONTROLLER, application, predicate, args);
 
 			// STORE A NEW ENTRY IN THE UserList (PublisherList)
 			args = new HashMap<String, byte[]>();
 			args.put("name", publisher.getName().getBytes("UTF-8"));
 			args.put("user", publisher.getId().getBytes("UTF-8"));
 			storageManager.insertSuperSlice(SC_USER_LIST, ("PUBLISH_" + application + subPredicate), publisher.getId(), args);
 
 			// STORE A NEW ENTRY IN THE ApplicationModel
 			args = new HashMap<String, byte[]>();
 			args.put("dataList", (application + subPredicate + publisher.getId()).getBytes("UTF-8"));
 			storageManager.insertSuperSlice(SC_APPLICATION_MODEL, application, (subPredicate + publisher.getId()), args);
 
 			// STORE THE DATAs
 			for(MDataBean data : dataList){
 				args = new HashMap<String, byte[]>();
 				args.put("key", data.getKey().getBytes("UTF-8"));
 				args.put("value", data.getValue().getBytes("UTF-8"));
 				args.put("ontology", data.getOntologyID().getBytes("UTF-8"));
 				storageManager.insertSuperSlice(SC_DATA_LIST, (application + subPredicate + publisher.getId()), data.getKey(), args);
 			}
 
 			// SEND A MAIL TO THE SUBSCRIBER
 			String mailinglist = "";
 			List<Map<byte[], byte[]>> subscribers = storageManager.selectList(SC_USER_LIST, "SUBSCRIBE_" + application + subPredicate);
 			for(Map<byte[], byte[]> set : subscribers) {
 				for(Entry<byte[], byte[]> entry : set.entrySet()){
 					if(new String(entry.getKey(), "UTF-8").equals("user")){
 						String userID = new String(entry.getValue(), "UTF-8");
 						mailinglist += new String(storageManager.selectColumn(CF_USER, userID, "email"), "UTF-8") + ",";
 					}
 				}
 			}
 
 			// Format the mail -- TODO Refactor and put this into an other class (mail package should be used)
 			if(!mailinglist.equals("")){
 				String content = "Bonjour, \nDe nouvelles informations sont arrivées sur votre plateforme myMed.\n"
 						+ "Application Concernée: " + application + "\n"
 						+ "Predicate: \n";
 				for(MDataBean data : dataList){
 					content += "\t- " + data.getKey() + ": " + data.getValue() + "\n";
 				}
 				content += "\n------\nL'équipe myMed";
 				new Mail("mymed.subscribe@gmail.com", mailinglist, "myMed subscribe info: " + application, content);
 			}
 		} catch (UnsupportedEncodingException e) {
 			throw new InternalBackEndException(e.getMessage());
 		}
 	}
 
 	/**
 	 * SUBSCRIBE
 	 * @see IPubSubManager#create(String, String, MUserBean)
 	 */
 	@Override
 	public void create(String application, String predicate, MUserBean subscriber)
 			throws InternalBackEndException, IOBackEndException {
 		try {
 			// STORE A NEW ENTRY IN THE ApplicationController
 			Map<String, byte[]> args = new HashMap<String, byte[]>();
 			args.put("subscriberList", ("SUBSCRIBE_" + application + predicate).getBytes("UTF-8"));
 			args.put("predicate", predicate.getBytes("UTF-8"));
 			storageManager.insertSuperSlice(SC_APPLICATION_CONTROLLER, application, predicate, args);
 
 			// STORE A NEW ENTRY IN THE UserList (SubscriberList)
 			args = new HashMap<String, byte[]>();
 			args.put("name", subscriber.getName().getBytes("UTF-8"));
 			args.put("user", subscriber.getId().getBytes("UTF-8"));
 			storageManager.insertSuperSlice(SC_USER_LIST, ("SUBSCRIBE_" + application + predicate), subscriber.getId(), args);
 
 		} catch (UnsupportedEncodingException e) {
 			throw new InternalBackEndException(e.getMessage());
 		}
 	}
 
 	/**
 	 * FIND
 	 * @see IPubSubManager#read(String, String)
 	 */
 	@Override
 	public List<Map<String, String>> read(String application, String predicate)
 			throws InternalBackEndException, IOBackEndException {
 
 		List<Map<String, String>> resList = new ArrayList<Map<String,String>>();
 
 		// get the list of sub predicate
 		List<Map<byte[], byte[]>> subPredicateListMap = storageManager.selectList(SC_APPLICATION_CONTROLLER, application + predicate);
 		List<String> subPredicateList = new ArrayList<String>();
 		for(Map<byte[], byte[]> set : subPredicateListMap) {
 			try {
 				for(Entry<byte[], byte[]> entry : set.entrySet()){
 					String entryStr = new String(entry.getKey(), "UTF-8");
 					if(entryStr.equals("predicate")){
 						String subPredicate = new String(entry.getValue(), "UTF-8");
 						subPredicateList.add(subPredicate);
 						break;
 					}
 				}
 			} catch (UnsupportedEncodingException e) {
 				throw new InternalBackEndException(e.getMessage());
 			}
 		}
 
 		// concatenate all the publisher
 		for(String subPredicate : subPredicateList) {
 			List<Map<byte[], byte[]>> publisherList = storageManager.selectList(SC_USER_LIST, "PUBLISH_" + application + subPredicate);
 			for(Map<byte[], byte[]> set : publisherList) {
 				Map<String, String> resMap = new HashMap<String, String>();
 				for(Entry<byte[], byte[]> entry : set.entrySet()){
 					try {
 						resMap.put(new String(entry.getKey(), "UTF-8"), new String(entry.getValue(), "UTF-8"));
 					} catch (UnsupportedEncodingException e) {
 						throw new InternalBackEndException(e.getMessage());
 					}
 				}
 				resMap.put("predicate", subPredicate); // TO FIX USE THE APPLICATIONCONTROLLER SC
 				resList.add(resMap);
 			}
 		}
 
 		return resList;
 	}
 
 	/**
 	 * FIND - GET DETAILS
 	 * @see IPubSubManager#read(String, String)
 	 */
 	@Override
 	public List<Map<String, String>> read(String application, String predicate, String userID)
 			throws InternalBackEndException, IOBackEndException {
 		List<Map<byte[], byte[]>> list = storageManager.selectList(SC_DATA_LIST, application+predicate+userID);
 		List<Map<String, String>> resList = new ArrayList<Map<String,String>>();
 		for(Map<byte[], byte[]> set : list) {
 			Map<String, String> resMap = new HashMap<String, String>();
 			for(Entry<byte[], byte[]> entry : set.entrySet()){
 				try {
 					resMap.put(new String(entry.getKey(), "UTF-8"), new String(entry.getValue(), "UTF-8"));
 				} catch (UnsupportedEncodingException e) {
 					throw new InternalBackEndException(e.getMessage());
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
 	public void delete(String application, String predicate, MUserBean user)
 			throws InternalBackEndException, IOBackEndException {
 		storageManager.removeAll(SC_DATA_LIST, (application + predicate + user.getId()));
 		storageManager.removeAll(SC_USER_LIST, ("PUBLISH_" + application + predicate));
 		storageManager.removeSuperColumn(SC_APPLICATION_CONTROLLER, application, predicate);
 		storageManager.removeSuperColumn(SC_APPLICATION_MODEL, application, (predicate + user.getId()));
 	}
 
 
 }
