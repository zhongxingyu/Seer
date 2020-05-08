 package com.dobby.core;
 
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Queue;
 import java.util.Set;
 import java.util.Vector;
 
import com.sun.tools.internal.xjc.reader.xmlschema.bindinfo.BIConversion.User;

 /**
  * 
  * @author anand
  * 
  */
 public class Session implements Runnable {
 	private int sleepTime = 5;
 	private boolean running;
 
 	private Queue<Request> requestQueue;
 	private Map<String, List<Request>> requestLog;
 	private StateVector currentState;
 	private Set<Request> docMod;
 	private String docName;
 	private String userName;
 
 	private String docText;
 
 	/**
 	 * Creates a new editing session for a document
 	 * 
 	 * @param docName
 	 */
 	public Session(String user, String docName) {
 		this.requestQueue = new LinkedList<Request>();
 		this.requestLog = new HashMap<String, List<Request>>();
 		this.docMod = new HashSet<Request>();
 		this.docName = docName;
 		this.currentState = new StateVector();
 		this.userName = user;
 		this.docText = "";
 		running = false;
 	}
 
 	/**
 	 * Receives a request, and enters it into the Request queue
 	 * 
 	 * @param r
 	 */
 	public synchronized void receiveRequest(Request r) {
 		requestQueue.add(r);
 		putRequestInLog(r);
 	}
 
 	/**
 	 * Executes a request by dequeueing it, translating it, and then applying
 	 * the transformations
 	 * 
 	 * @param r
 	 */
 	public synchronized void executeRequest() {
 		if (!isRequestQueueEmpty()) {
 			Request target = requestQueue.remove();
 			Request translated = translateRequest(target);
 			applyRequestToText(translated);
 			// No matter how the translation goes, we should just be pushing
 			// The current state vector forward, and attaching things around it.
 			StateVector incedUser = currentState.incrementedUser(translated
 					.getUser());
 			currentState = incedUser;
 		}
 	}
 
 	private void applyRequestToText(Request translated) {
 		docText = translated.apply(docText);
 	}
 
 	/**
 	 * Checks if the requestQueue is empty
 	 * 
 	 * @return
 	 */
 	public synchronized boolean isRequestQueueEmpty() {
 		return requestQueue.isEmpty();
 	}
 
 	/**
 	 * Provides the number of pending operations on this session
 	 * 
 	 * @return
 	 */
 	public synchronized int getRequestQueueLength() {
 		return requestQueue.size();
 	}
 
 	/**
 	 * Translates a request into an appropriate space for execution.
 	 * 
 	 * @param target
 	 * @return translated request
 	 */
 	protected Request translateRequest(Request target) {
 		return translateRequest(target, currentState);
 	}
 
 	/**
 	 * Recursive call to translate a request into an appropriate space for
 	 * execution.
 	 * 
 	 * @param target
 	 *            , state
 	 * @return translated request
 	 */
 	protected Request translateRequest(Request target, StateVector state) {
 		if (target.getStateVector().equals(state)) {
 			addToModel(target);
 			return target;
 		} else {
 			// note that previous state is guaranteed by the Ressel paper
 			// to return a user
 			String userToDec = previousState(target, state);
 			StateVector decVec = state.decrementedUser(userToDec);
 			Request previousRequest = getRequest(userToDec,
 					decVec.getUser(userToDec));
 			if (previousRequest != null) {
 				Request translatedPrevReq = translateRequest(previousRequest,
 						decVec);
 				Request translatedThisReq = translateRequest(target, decVec);
 				Request transformedPrevReq = translatedPrevReq
 						.transform(translatedThisReq);
 				Request transformedThisReq = translatedThisReq
 						.transform(translatedPrevReq);
 				addToModel(transformedPrevReq);
 				addToModel(transformedThisReq);
 				return transformedThisReq;
 			} else {
 				// Prevents catastrophic failure in the case
 				// that we really dont have a good way to translate this.
 				System.err.println("Couldn't find a good way to translate "
 						+ target + " " + state);
 				return target;
 			}
 
 		}
 	}
 
 	private void addToModel(Request r) {
 		docMod.add(r);
 	}
 
 	/**
 	 * Helper function to get the first previous state such that the translated
 	 * request is reachable. The User which we should decrement to achieve this
 	 * state is returned.
 	 * 
 	 * @param target
 	 * @return
 	 */
 	private String previousState(Request target, StateVector curState) {
 		for (String user : curState.getUsers()) {
 			StateVector decUser = curState.decrementedUser(user);
 			if (reachable(decUser)
 					&& (target.getStateVector().getUser(user) <= (curState
 							.getUser(user) - 1))) {
 				return user;
 			}
 		}
 		return null;
 	}
 
 	public StateVector getCurrentState() {
 		return currentState;
 	}
 
 	public void setCurrentState(StateVector currentState) {
 		this.currentState = currentState;
 	}
 
 	/**
 	 * Gets the text body of the document
 	 * 
 	 * @return
 	 */
 	public String getCurrentText() {
 		return docText;
 	}
 
 	/**
 	 * Sets the document name
 	 * 
 	 * @param docName
 	 */
 	public void setDocName(String docName) {
 		this.docName = docName;
 	}
 
 	/**
 	 * Gets the document name
 	 * 
 	 * @return
 	 */
 	public String getDocName() {
 		return docName;
 	}
 
 	/**
 	 * Gets the number of requests recieved from a user
 	 * 
 	 * @param user
 	 * @return
 	 */
 	public int getUserState(String user) {
 		return currentState.getUser(user);
 	}
 
 	/**
 	 * Gets a request from the log, based on user and the request ordering
 	 * 
 	 * @param user
 	 * @param index
 	 * @return
 	 */
 	public Request getRequest(String user, int index) {
 		Request desired = null;
 		if (requestLog.containsKey(user)) {
 			List<Request> userLog = requestLog.get(user);
 			if (userLog.size() > index) {
 				desired = userLog.get(index);
 			}
 		}
 		return desired;
 	}
 
 	/**
 	 * Same purpose as the function above, however this checks to see if a given
 	 * StateVector is in the interaction model. This makes it easier to
 	 * implement translate request
 	 * 
 	 * Note: this is overloaded, but there might not be a reason to keep the
 	 * previous function around so possibly delete the Request version after
 	 * implementation of translate request.
 	 */
 	public boolean reachable(StateVector target) {
 		Set<String> users = target.getUsers();
 		for (String u : users) {
 			if (target.getUser(u) != 0
 					&& getRequest(u, target.getUser(u) - 1).getStateVector()
 							.incrementedUser(u).greaterThan(target))
 				return false;
 		}
 		return true;
 	}
 
 	/**
 	 * Inserts a request into the log of requests
 	 * 
 	 * @param r
 	 */
 	private void putRequestInLog(Request r) {
 		if (requestLog.containsKey(r.user)) {
 			List<Request> userLog = requestLog.get(r.user);
 			userLog.add(r);
 		} else {
 			List<Request> userLog = new Vector<Request>();
 			userLog.add(r);
 			requestLog.put(r.getUser(), userLog);
 		}
 	}
 
 	public String getUserName() {
 		return userName;
 	}
 
 	@Override
 	public void run() {
 		running = true;
 
 		while (running) {
 			executeRequest();
 			pause();
 		}
 
 		cleanup();
 	}
 
 	private void cleanup() {
 		// Any cleanup should be done here
 	}
 
 	private void pause() {
 		if (sleepTime > 0) {
 			try {
 				Thread.sleep(sleepTime);
 			} catch (InterruptedException e) {
 				stop();
 			}
 		}
 	}
 
 	public void stop() {
 		running = false;
 	}
 
 	public int getSleepTime() {
 		return sleepTime;
 	}
 
 	public void setSleepTime(int sleepTime) {
 		this.sleepTime = sleepTime;
 	}
 
 	public Set<Request> getDocMod() {
 		return docMod;
 	}
 
 }
