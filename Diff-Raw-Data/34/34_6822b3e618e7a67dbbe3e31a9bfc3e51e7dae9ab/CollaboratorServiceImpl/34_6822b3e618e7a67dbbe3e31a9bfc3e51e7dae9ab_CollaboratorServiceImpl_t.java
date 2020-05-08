 package edu.caltech.cs141b.hw2.gwt.collab.server;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 
 import javax.jdo.PersistenceManager;
 import javax.jdo.Query;
 import javax.jdo.Transaction;
 
 import com.google.appengine.api.channel.ChannelMessage;
 import com.google.appengine.api.channel.ChannelService;
 import com.google.appengine.api.channel.ChannelServiceFactory;
 import com.google.appengine.api.datastore.Key;
 import com.google.appengine.api.datastore.KeyFactory;
 import com.google.gwt.user.server.rpc.RemoteServiceServlet;
 
 import edu.caltech.cs141b.hw2.gwt.collab.client.CollaboratorService;
 import edu.caltech.cs141b.hw2.gwt.collab.shared.DocumentMetadata;
 import edu.caltech.cs141b.hw2.gwt.collab.shared.LockExpired;
 import edu.caltech.cs141b.hw2.gwt.collab.shared.LockUnavailable;
 import edu.caltech.cs141b.hw2.gwt.collab.shared.LockedDocument;
 import edu.caltech.cs141b.hw2.gwt.collab.shared.UnlockedDocument;
 
 /**
  * The server side implementation of the RPC service.
  */
 @SuppressWarnings("serial")
 public class CollaboratorServiceImpl extends RemoteServiceServlet implements
 		CollaboratorService {
 
 	/*
 	 * private static final String QUEUE_MAP = "queue_map"; private static final
 	 * String TOKEN_MAP = "token_map"; private static final String TIMER_MAP =
 	 * "timer_map";
 	 */
 	private static final int LOCK_TIME = 30;
 	private static Map<String, List<String>> queueMap;
 	private static Map<String, String> tokenMap;
 
 	private static ArrayList<String> lockedDocuments = new ArrayList<String>();
 
 	private static CollaboratorServiceImpl server = new CollaboratorServiceImpl();
 
 	public CollaboratorServiceImpl() {
 		queueMap = Collections
 				.synchronizedMap(new HashMap<String, List<String>>());
 		tokenMap = Collections.synchronizedMap(new HashMap<String, String>());
 	}
 
 	public static void cleanLocks() {
 		// Clean up documents if there are document currently locked
 		if (!lockedDocuments.isEmpty()) {
 			ArrayList<String> toClear = new ArrayList<String>();
 
 			for (String docKey : lockedDocuments) {
 				System.out.println("Checking lock for " + docKey);
 				PersistenceManager pm = PMF.get().getPersistenceManager();
 				Transaction t = pm.currentTransaction();
 				try {
 					// Starting transaction...
 					t.begin();
 
 					// Create the key
 					Key key = KeyFactory.stringToKey(docKey);
 
 					// Get the document from the Datastore
 					Document doc = pm.getObjectById(Document.class, key);
 
 					// If the doc is locked and the lock expired
 					if (doc.isLocked()
 							&& doc.getLockedUntil().before(
 									new Date(System.currentTimeMillis()))) {
 						toClear.add(docKey);
 						if (queueMap.containsKey(docKey)) {
 							toClear.add(docKey);
 							System.out.println("In the queueMap " + docKey);
 
 						}
 						// Check if there are clients waiting for the document
 
 					}
 					// ...Ending transaction
 					t.commit();
 				} finally {
 					// Do some cleanup
 					if (t.isActive()) {
 						t.rollback();
 					}
 					pm.close();
 				}
 			}
 
 			for (String docKey : toClear) {
 				String previousClient = tokenMap.get(docKey);
 				server.receiveToken(previousClient, docKey);
 			}
 		}
 
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * edu.caltech.cs141b.hw2.gwt.collab.client.CollaboratorService#getDocumentList
 	 * ()
 	 */
 	@SuppressWarnings("unchecked")
 	@Override
 	public List<DocumentMetadata> getDocumentList() {
 		// Get the PM
 		PersistenceManager pm = PMF.get().getPersistenceManager();
 
 		// Make the Document query
 		Query query = pm.newQuery(Document.class);
 
 		ArrayList<DocumentMetadata> docList = new ArrayList<DocumentMetadata>();
 
 		try {
 			// Query the Datastore and iterate through all the Documents in the
 			// Datastore
 			for (Document doc : (List<Document>) query.execute()) {
 				// Get the document metadata from the Documents
 				DocumentMetadata metaDoc = new DocumentMetadata(doc.getKey(),
 						doc.getTitle());
 				docList.add(metaDoc);
 			}
 
 			// Return the list of docs
 			return docList;
 		} finally {
 			// Do cleanup
 			query.closeAll();
 			pm.close();
 		}
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * edu.caltech.cs141b.hw2.gwt.collab.client.CollaboratorService#getDocument
 	 * (java.lang.String)
 	 */
 	@Override
 	public UnlockedDocument getDocument(String documentKey) {
 		// Get the PM
 		PersistenceManager pm = PMF.get().getPersistenceManager();
 
 		try {
 			// Create the key for this document
 			Key key = KeyFactory.stringToKey(documentKey);
 			// Use the key to retrieve the document
 			Document doc = pm.getObjectById(Document.class, key);
 
 			// Return the unlocked document
 			return doc.getUnlockedDoc();
 		} finally {
 			// Do cleanup
 			pm.close();
 		}
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * edu.caltech.cs141b.hw2.gwt.collab.client.CollaboratorService#saveDocument
 	 * (edu.caltech.cs141b.hw2.gwt.collab.shared.LockedDocument)
 	 */
 	@Override
 	public UnlockedDocument saveDocument(String clientID, LockedDocument doc)
 			throws LockExpired {
 		// Get the PM
 		PersistenceManager pm = PMF.get().getPersistenceManager();
 
 		Document toSave;
 		// Get the document's key
 		String stringKey = doc.getKey();
 
 		Transaction t = pm.currentTransaction();
 		try {
 			// If the doc has no key, it's a new document, so create a new
 			// document

			// Starting transaction...
			t.begin();

 			if (stringKey == null) {
 				// First unlock it
 				toSave = new Document(doc.unlock());
 			} else {

 				// Create the key
 				Key key = KeyFactory.stringToKey(stringKey);
 
 				// Get the document corresponding to the key
 				toSave = pm.getObjectById(Document.class, key);
 
 				// Get the lock information - saveDocument can only be called if
 				// there is a lock or if it's a new document
 				String lockedBy = toSave.getLockedBy();
 				Date lockedUntil = toSave.getLockedUntil();
 
 				// Get the client's ID
 				String identity = clientID;// getThreadLocalRequest().getRemoteAddr();
 
 				// Check that the person trying to save has the lock and that
 				// the lock hasn't expired
 				if (lockedBy.equals(identity)
 						&& lockedUntil.after(new Date(System
 								.currentTimeMillis()))) {
 					// If both are fulfilled, update and unlock the doc
 					toSave.update(doc);
 					toSave.unlock();

 				} else {
 					// Otherwise, throw an exception
 					throw new LockExpired();
 				}
 			}
 
			// Now write the results to Datastore
			pm.makePersistent(toSave);

			// ...Ending transaction
			t.commit();

			// Now, take the token back
			receiveToken(clientID, stringKey);

 			// Return the unlocked document
 			return toSave.getUnlockedDoc();
 		} finally {
 			// Do some cleanup
 			if (t.isActive()) {
 				t.rollback();
 			}
 			pm.close();
 		}
 	}
 
 	/**
 	 * Called whenever a client returns a token for an item. This function will
 	 * update the token map and then send a token if possible.
 	 * 
 	 * @param clientID
 	 * @param docKey
 	 */
 	private void receiveToken(String clientID, String docKey) {
 		/*
 		 * Map<String, String> tokenMap = (Map<String, String>)
 		 * getThreadLocalRequest() .getAttribute(TOKEN_MAP); Map<String, Thread>
 		 * timerMap = (Map<String, Thread>) getThreadLocalRequest()
 		 * .getAttribute(TIMER_MAP);
 		 */
 
 		// Stop the lock timer.
 		// timerMap.get(docKey).cancel();
 
 		// Return the token
 		tokenMap.put(docKey, "server");
 
 		// If there is no document waiting for the document remove from list of
 		// locked docs CRITICAL CODE?
 		if (!queueMap.containsKey(docKey) && lockedDocuments.contains(docKey)) {
 			lockedDocuments.remove(docKey);
 		}
 
 		// getThreadLocalRequest().setAttribute(TOKEN_MAP, tokenMap);
 
 		// Now, try to send a token
 		clientID = pollNextClient(docKey);
 		if (clientID != null) {
 			sendToken(clientID, docKey);
 		}
 	}
 
 	private void sendToken(final String clientID, final String docKey) {
 		// Fetch and create the necessary maps
 		/*
 		 * Map<String, String> tokenMap = (Map<String, String>)
 		 * getThreadLocalRequest() .getAttribute(TOKEN_MAP);
 		 */
 
 		// Now, set the correct clientID. We are "giving" them the token here.
 		tokenMap.put(docKey, clientID);
 
 		// Add to list of locked documents if not already in there
 		if (!lockedDocuments.contains(docKey)) {
 			lockedDocuments.add(docKey);
 		}
 
 		// getThreadLocalRequest().setAttribute(TOKEN_MAP, tokenMap);
 
 		// Lock the document
 		// Get the PM
 		PersistenceManager pm = PMF.get().getPersistenceManager();
 
 		Document toSave;
 		Date endTime;
 
 		Transaction t = pm.currentTransaction();
 		try {
 			// Starting transaction...
 			t.begin();
 			// Create the key
 			Key key = KeyFactory.stringToKey(docKey);
 
 			// Get the document corresponding to the key
 			toSave = pm.getObjectById(Document.class, key);
 			endTime = new Date(System.currentTimeMillis() + LOCK_TIME * 1000);
 			System.out.println("Giving lock to " + clientID);
 			toSave.lock(endTime, clientID);
 
 			pm.makePersistent(toSave);
 
 			t.commit();
 		} finally {
 			// Do some cleanup
 			/*
 			 * if (t.isActive()) { t.rollback(); }
 			 */
 			pm.close();
 		}
 
 		// Finally, inform the client that the doc is locked and ready for them
 		getChannelService().sendMessage(new ChannelMessage(clientID, docKey));
 
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * edu.caltech.cs141b.hw2.gwt.collab.client.CollaboratorService#releaseLock
 	 * (edu.caltech.cs141b.hw2.gwt.collab.shared.LockedDocument)
 	 */
 	@Override
 	public void releaseLock(String clientID, LockedDocument doc)
 			throws LockExpired {
 		// Get the PM
 		PersistenceManager pm = PMF.get().getPersistenceManager();
 
 		Transaction t = pm.currentTransaction();
 		try {
 			// Starting transaction...
 			t.begin();
 
 			// Get the doc's key
 			Key key = KeyFactory.stringToKey(doc.getKey());
 
 			// Use the key to retrieve the doc
 			Document toSave = pm.getObjectById(Document.class, key);
 
 			// Get the lock information - saveDocument can only be called if
 			// there is a lock or if it's a new document
 			String lockedBy = toSave.getLockedBy();
 
 			// Get the client's identity
 			String identity = clientID;// getThreadLocalRequest().getRemoteAddr();
 
 			// Make sure that the person unlocking is the person who locked the
 			// doc.
 			if (lockedBy.equals(identity)) {
 				// Unlock it
 				toSave.unlock();
 
 				// And store it in the Datastore
 				pm.makePersistent(toSave);
 
 				// ...Ending transaction
 				t.commit();
 
 				// Indicate that the token has been returned
 				receiveToken(clientID, doc.getKey());
 			} else {
 				// Otherwise, throw an exception
 				throw new LockExpired("You no longer have the lock");
 			}
 
 		} finally {
 			// Do some cleanup
 			if (t.isActive()) {
 				t.rollback();
 			}
 			pm.close();
 		}
 	}
 
 	private void addToDocQueue(String clientID, String documentKey) {
 		/*
 		 * Map<String, List<String>> queueMap = (Map<String, List<String>>)
 		 * getThreadLocalRequest() .getAttribute(QUEUE_MAP);
 		 */
 
 		if (!queueMap.containsKey(documentKey)) {
 			queueMap.put(documentKey, Collections
 					.synchronizedList(new LinkedList<String>()));
 		}
 
 		List<String> queue = queueMap.get(documentKey);
 
 		queue.add(clientID);
 
 		queueMap.put(documentKey, queue);
 		// getThreadLocalRequest().setAttribute(QUEUE_MAP, queueMap);
 	}
 
 	/**
 	 * 
 	 * @param documentKey
 	 * @return a client ID of the next client waiting on this doc
 	 */
 	private String pollNextClient(String documentKey) {
 		/*
 		 * Map<String, List<String>> queueMap = (Map<String, List<String>>)
 		 * getThreadLocalRequest() .getAttribute(QUEUE_MAP);
 		 */
 
 		List<String> queue = queueMap.get(documentKey);
 		if (queue != null && !queue.isEmpty()) {
 			return queue.remove(0);
 		}
 
 		return null;
 	}
 
 	private boolean removeClient(String clientID, String documentKey) {
 		/*
 		 * Map<String, List<String>> queueMap = (Map<String, List<String>>)
 		 * getThreadLocalRequest() .getAttribute(QUEUE_MAP);
 		 */
 
 		List<String> queue = queueMap.get(documentKey);
 		if (queue != null) {
 			return queue.remove(clientID);
 		}
 
 		return false;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * edu.caltech.cs141b.hw2.gwt.collab.client.CollaboratorService#lockDocument
 	 * (java.lang.String)
 	 */
 	@Override
 	public void lockDocument(String clientID, String documentKey) {
 		/*
 		 * Map<String, String> tokenMap = (Map<String, String>)
 		 * getThreadLocalRequest() .getAttribute(TOKEN_MAP);
 		 */
 		// Handle the case where the token map doesn't have the docKey - no
 		// client has tried to access it yet
 
 		if (!tokenMap.containsKey(documentKey))
 			tokenMap.put(documentKey, "server");
 
 		// Check if we have the token. If we do, send the token out immediately
 		if (tokenMap.get(documentKey).equals("server"))
 			sendToken(clientID, documentKey);
 		else
 			addToDocQueue(clientID, documentKey);
 
 		/*
 		 * for (Entry<String, List<String>> e : queueMap.entrySet()) {
 		 * System.out.println(e.getKey()); for (String s : e.getValue()) {
 		 * System.out.println(s); } }
 		 * 
 		 * for (Entry<String, String> e : tokenMap.entrySet()) {
 		 * System.out.println(e.getKey() + ": " + e.getValue()); }
 		 */
 	}
 
 	@Override
 	public String login(String clientID) {
 		return getChannelService().createChannel(clientID);
 	}
 
 	private ChannelService getChannelService() {
 		return ChannelServiceFactory.getChannelService();
 	}
 
 	@Override
 	public LockedDocument getLockedDocument(String clientID, String documentKey)
 			throws LockUnavailable {
 
 		// Get the PM
 		PersistenceManager pm = PMF.get().getPersistenceManager();
 
 		Transaction t = pm.currentTransaction();
 		try {
 			// Starting transaction...
 			t.begin();
 
 			// Create the key
 			Key key = KeyFactory.stringToKey(documentKey);
 
 			// Get the document from the Datastore
 			Document toSave = pm.getObjectById(Document.class, key);
 
 			String identity = clientID;// getThreadLocalRequest().getRemoteAddr();
 
 			// If the doc is locked and you own it...
 			if (!toSave.isLocked()
 					|| !toSave.getLockedBy().equals(identity)
 					|| toSave.getLockedUntil().before(
 							new Date(System.currentTimeMillis()))) {
 				throw new LockUnavailable("This lock is not yours");
 			}
 
 			// ...Ending transaction
 			t.commit();
 
 			// Return the locked document
 			return toSave.getLockedDoc();
 		} finally {
 			// Do some cleanup
 			if (t.isActive()) {
 				t.rollback();
 			}
 			pm.close();
 		}
 
 	}
 
 	@Override
 	public void leaveLockQueue(String clientID, String documentKey) {
 		removeClient(clientID, documentKey);
 	}
 
 }
