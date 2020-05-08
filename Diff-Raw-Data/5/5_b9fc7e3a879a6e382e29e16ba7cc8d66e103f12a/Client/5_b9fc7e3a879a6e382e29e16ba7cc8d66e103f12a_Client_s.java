 /**
  * Copyright 2010 The University of Nottingham
  * 
  * This file is part of GenericAndroidClient.
  *
  *  GenericAndroidClient is free software: you can redistribute it and/or modify
  *  it under the terms of the GNU Affero General Public License as published by
  *  the Free Software Foundation, either version 3 of the License, or
  *  (at your option) any later version.
  *
  *  GenericAndroidClient is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU Affero General Public License for more details.
  *
  *  You should have received a copy of the GNU Affero General Public License
  *  along with GenericAndroidClient.  If not, see <http://www.gnu.org/licenses/>.
  * 
  */
 package uk.ac.horizon.ug.exploding.client;
 
 import java.io.BufferedInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.OutputStream;
 import java.io.OutputStreamWriter;
 import java.net.HttpURLConnection;
 import java.net.MalformedURLException;
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.net.URL;
 import java.nio.charset.Charset;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import org.apache.http.HttpEntity;
 import org.apache.http.HttpResponse;
 import org.apache.http.HttpStatus;
 import org.apache.http.StatusLine;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.entity.ByteArrayEntity;
 import org.apache.http.entity.StringEntity;
 import org.json.JSONStringer;
 
 import uk.ac.horizon.ug.exploding.client.logging.LoggingUtils;
 
 import uk.ac.horizon.ug.exploding.client.model.Game;
 import uk.ac.horizon.ug.exploding.client.model.Member;
 import uk.ac.horizon.ug.exploding.client.model.ModelUtils;
 import uk.ac.horizon.ug.exploding.client.model.Player;
 import uk.ac.horizon.ug.exploding.client.model.Position;
 import uk.ac.horizon.ug.exploding.client.model.Zone;
 
 //import org.json.JSONArray;
 //import org.json.JSONException;
 //import org.json.JSONObject;
 
 //import uk.ac.horizon.ug.bluetoothex.testclient.Main;
 //import uk.ac.horizon.ug.exserver.clientapi.JsonUtils;
 //import uk.ac.horizon.ug.exserver.clientapi.protocol.Message;
 //import uk.ac.horizon.ug.exserver.clientapi.protocol.MessageStatusType;
 //import uk.ac.horizon.ug.exserver.clientapi.protocol.MessageType;
 
 import android.os.Handler;
 import android.util.Log;
 
 import com.thoughtworks.xstream.XStream;
 import com.thoughtworks.xstream.io.xml.CompactWriter;
 import com.thoughtworks.xstream.io.xml.DomDriver;
 
 /** Client stub
  * 
  * @author cmg
  *
  */
 public class Client {
 	private static final String TAG = "Client";
 	private static final String LOGTYPE_CLIENT = "Client";
 	//static Logger logger = Logger.getLogger(Client.class.getName());
 	protected URI conversationUrl;
 	protected String clientId;
 	protected HttpClient httpClient;
 	/** queued message state */
 	private enum QueuedMessageStatus {
 		QUEUED, CONNECTING, SENDING, AWAITING_STATUS, AWAITING_RESPONSE, DONE
 	}
 	/** log from thread */
 	private void log(String message) {
 		log(message, null, null);
 	}
 	private void log(String message, String extraKey, Object extraValue) {
 		try {
 			JSONStringer js = new JSONStringer();
 			js.object();
 			js.key("client");
 			js.value(this.hashCode());
 			js.key("message");
 			js.value(message);
 			if (extraKey!=null) {
 				js.key(extraKey);
 				js.value(extraValue);
 			}
 			js.endObject();
 			LoggingUtils.log(LOGTYPE_CLIENT, js.toString());
 		}
 		catch (Exception e) {
 			Log.e(TAG,"Logging "+message, e);
 		}
 	}
 	private void logCreated() {
 		try {
 			JSONStringer js = new JSONStringer();
 			js.object();
 			js.key("client");
 			js.value(this.hashCode());
 			js.key("message");
 			js.value("created");
 			js.key("clientId");
 			js.value(clientId);
 			js.key("conversationUrl");
 			js.value(conversationUrl);
 			js.endObject();
 			LoggingUtils.log(LOGTYPE_CLIENT, js.toString());
 		}
 		catch (Exception e) {
 			Log.e(TAG,"Logging created", e);
 		}
 	}
 	/** a queued message */
 	public class QueuedMessage {
 		// not public cons
 		QueuedMessage() {			
 		}
 		Message message;
 		Message response;
 		ClientMessageListener listener;
 		QueuedMessageStatus status;
 		MessageStatusType messageStatus;
 		String errorMessage;
 		volatile boolean cancelled = false;
 	}
 	/** queue of QueuedMessages */
 	private LinkedList<QueuedMessage> queuedMessages = new LinkedList<QueuedMessage>();
 	/**
 	 * @param conversationUrl
 	 * @param clientId
 	 */
 	public Client(HttpClient httpClient, URI conversationUrl, String clientId) {
 		super();
 		this.httpClient = httpClient;
 		this.conversationUrl = conversationUrl;
 		this.clientId = clientId;
 		logCreated();
 	}
 	/**
 	 * @param conversationUrl
 	 * @param clientId
 	 * @throws MalformedURLException 
 	 * @throws URISyntaxException 
 	 */
 	public Client(HttpClient httpClient, String conversationUrl, String clientId) throws MalformedURLException, URISyntaxException {
 		super();
 		this.httpClient = httpClient;
 		this.conversationUrl = new URI(conversationUrl);
 		this.clientId = clientId;
 		logCreated();
 	}
 	
 	public URI getConversationUrl() {
 		return conversationUrl;
 	}
 	public String getClientId() {
 		return clientId;
 	}
 
 	protected int seqNo = 1;
 // move to selective Ack
 //  protected int ackSeq = 0;
     protected List<Integer> ackSeqs = new LinkedList<Integer>();
     
 	/** connect */
 //	public boolean connect(/*List<String> classNames*/) throws /*JSONException,*/ IOException {
 //		
 //		List<Message> messages = new LinkedList<Message>();
 //		// we are a content display device!
 //		for (String className : classNames) {
 //			int ix = className.lastIndexOf('.');
 //			String namespace = ix>=0 ? className.substring(0, ix) : null;
 //			String typeName = className.substring(ix+1);
 //			JSONObject val = new JSONObject();
 //			val.put("typeName", typeName);
 //			if(namespace!=null)
 //				val.put("namespace", namespace);
 //			val.put("id", clientId);
 //			
 //			messages.add(addFactMessage(val.toString()));
 //		}
 //		sendMessages(messages);
 //		return true;
 //	}	
 	/** add fact message */
 	public Message addFactMessage(Object object) {
 		Message msg = new Message();
 		msg.setType(MessageType.ADD_FACT.name());
 		msg.setSeqNo(seqNo++);
 		msg.setNewVal(object);
 		return msg;
 	}
 	/** add fact message */
 	public Message updateFactMessage(Object oldVal, Object newVal) {
 		Message msg = new Message();
 		msg.setType(MessageType.UPD_FACT.name());
 		msg.setSeqNo(seqNo++);
 		msg.setOldVal(oldVal);
 		msg.setNewVal(newVal);
 		return msg;
 	}
 	/** queue message to send */
 	public QueuedMessage queueMessage(Message msg, ClientMessageListener listener) throws IOException {
 		synchronized (queuedMessages) {
 			QueuedMessage qm = new QueuedMessage();
 			qm.message = msg;
 			qm.listener = listener;
 			qm.status = QueuedMessageStatus.QUEUED;
 			queuedMessages.addLast(qm);
 			queuedMessages.notify();
 			log("queueMessage", "message", msg.toString());
 			return qm;
 		}
 	}
 	/** wait up to max delay on queued messages */
 	public void waitOnQueuedMessages(int maxMs) {
 		synchronized (queuedMessages) {
 			if (queuedMessages.isEmpty()) {
 		 		try {
 					queuedMessages.wait(maxMs);
 				}
 		 		catch (InterruptedException ie) {
 		 			Log.d(TAG,"waitOnQueuedMessages interrupted");
 		 		}
 			}
 		}
 	}
 	/** messages queued? */
 	public boolean isQueuedMessage() {
 		synchronized(queuedMessages) {
 			return !queuedMessages.isEmpty();
 		}
 	}
 	/** send message thread */
 	private Thread sendMessageThread;
 	private QueuedMessage currentMessage;
 	/** cancel single queued message.
 	 * @return true if message was known and cancelled */
 	public boolean cancelMessage(QueuedMessage qm, boolean callListeners) {
 		synchronized (queuedMessages) {
 			log("cancelMessage", "message", qm.message.toString());
 			if (queuedMessages.contains(qm)) {
 				queuedMessages.remove(qm);
 				Log.d(TAG,"cancelMessage for queued Message");
 			} else if (qm==currentMessage){
 				Log.d(TAG,"cancelMessage for current Message");
 			}
 			else {
 				Log.d(TAG,"cancelMessage for unknown message "+qm);
 				return false;
 			}
 			if (qm.cancelled) {
 				Log.d(TAG,"cancelMessage for already-cancelled message "+qm);
 				return false;
 			}
 			qm.cancelled = true;
 			Log.d(TAG,"cancelMessage for "+qm.status+" message "+qm);
 			MessageStatusType status = MessageStatusType.OK;
 			switch(qm.status) {
 			case CONNECTING:
 				status = MessageStatusType.CANCELLED_BEFORE_SEND;
 				sendMessageThread.interrupt();
 				break;
 			case SENDING:
 			case AWAITING_RESPONSE:
 			case AWAITING_STATUS:				
 				status = MessageStatusType.CANCELLED_AFTER_SEND;
 				sendMessageThread.interrupt();
 				break;
 			case QUEUED:
 				status = MessageStatusType.CANCELLED_BEFORE_SEND;
 				break;
 			case DONE:
 				return false;
 			}
 			if (callListeners && qm.listener!=null)
 				callMessageListener(qm.listener, status, status.toString(), null);
 			return true;
 		}		
 	}
 	/** cancel all queued message. */
 	public void cancelQueuedMessages(boolean callListeners) {
 		while(true) {
 			synchronized (queuedMessages) {
 				if (queuedMessages.isEmpty())
 					return;
 				cancelMessage(queuedMessages.getFirst(), callListeners);
 			}
 		}
 	}
 	/** send queued messages */
 	public void sendQueuedMessages() {
 		while(true) {
 			QueuedMessage qm = null;
 			synchronized (queuedMessages) {
 				if (queuedMessages.isEmpty())
 					return;
 				qm = queuedMessages.removeFirst();
 			}
 			sendQueuedMessage(qm);
 		}	
 	}
 	private void sendQueuedMessage(QueuedMessage qm) {
 		List<Message> messages = sendQueuedMessageInternal(qm);
 		if (qm.status!=QueuedMessageStatus.DONE) {
 			Log.d(TAG,"sendQueuedMessage found status "+qm.status);
 			return;
 		}
 		if (qm.listener!=null) {
 			Object value = null;
 			if (qm.response!=null)
 				value = qm.response.getNewVal();
 			callMessageListener(qm.listener, qm.messageStatus, qm.errorMessage, value);
 		}
 	}
 	private void callMessageListener(final ClientMessageListener listener,
 			final MessageStatusType status, final String errorMessage, final Object value) {
 		if (listener==null)
 			return;
 		Handler handler = BackgroundThread.getHandler();
 		if (handler==null)
 			Log.e(TAG,"Cannot callMessageListener: handler=null");
 		else
 			handler.post(new Runnable() {
 				public void run() {
 					try {
 						listener.onMessageResponse(status, errorMessage, value);
 					}
 					catch (Exception e) {
 						Log.e(TAG,"Calling message listener "+listener, e);
 					}
 				}
 			});
 	}
 	/** internal async send */
 	@SuppressWarnings("unchecked")
 	private synchronized List<Message> sendQueuedMessageInternal(QueuedMessage qm) {
 		log("sendQueuedMessage", "message", qm.message.toString());
 		currentMessage = qm;
 		sendMessageThread = Thread.currentThread();
 		HttpPost request  = new HttpPost(conversationUrl);
 		Log.i(TAG,"SendMessages to "+request.getURI()+", requestline="+request.getRequestLine());
 		request.setHeader("Content-Type", "application/xml;charset=UTF-8");
 		//HttpURLConnection conn = (HttpURLConnection) conversationUrl.openConnection();
 		XStream xs = new XStream(/*new DomDriver()*/);
 		xs.alias("list", LinkedList.class);    	
 		xs.alias("message", Message.class);
 
 		// game specific
 		ModelUtils.addAliases(xs);
 		
 		List<Message> messages = new LinkedList<Message>();
 		messages.add(qm.message);
 
 		ByteArrayOutputStream baos = new ByteArrayOutputStream();
 		xs.marshal(messages, new CompactWriter(new OutputStreamWriter(baos,Charset.forName("UTF-8"))));
 		byte [] bytes = baos.toByteArray();
 		Log.d(TAG, "Sent: "+bytes.length+" bytes");
 		log("sendQueuedMessage.bytes", "bytes", bytes.length);
 		request.setEntity(new ByteArrayEntity(bytes));
 		
 		synchronized(queuedMessages) {
 			if (qm.cancelled) {
 				currentMessage = null;
 				return null;
 			}
 			qm.status = QueuedMessageStatus.SENDING;
 		}
 		
 		HttpResponse reply = null;
 		try {
 			reply = httpClient.execute(request);
 		}
 		catch (Exception e) {
 			Log.e(TAG,"Doing http request", e);
 			log("sendQueuedMessage.error", "exception", e.toString());
 			synchronized(queuedMessages) {
 				currentMessage = null;
 				if (qm.cancelled) 
 					return null;
 				synchronized (qm) {
 					qm.status = QueuedMessageStatus.DONE;
 					qm.messageStatus = MessageStatusType.NETWORK_ERROR;
 					qm.errorMessage = "Error sending request: "+e.getMessage()+", URL="+request.getURI();
 				}
 				return null;
 			}		
 		}
 
 		synchronized(queuedMessages) {
 			if (qm.cancelled) {
 				currentMessage = null;
 				return null;
 			}
 			qm.status = QueuedMessageStatus.AWAITING_STATUS;
 		}
 		
 		StatusLine statusLine = reply.getStatusLine();
 		Log.d(TAG, "Http status: "+statusLine);
 		int status = statusLine.getStatusCode();
 		if (status!=200) {
 			log("sendQueuedMessage.error", "status", status);
 			if (reply.getEntity()!=null)
 				try {
 					reply.getEntity().consumeContent();
 				} catch (IOException e) {
 					Log.d(TAG,"Ignored exception on "+status+" response: "+e);
 				}
 
 			synchronized(queuedMessages) {
 				currentMessage = null;
 				if (qm.cancelled)
 					return null;
 				synchronized (qm) {
 					qm.status = QueuedMessageStatus.DONE;
 					if (status==HttpStatus.SC_NOT_FOUND)
 						qm.messageStatus = MessageStatusType.NOT_FOUND;
 					else if (status==HttpStatus.SC_INTERNAL_SERVER_ERROR)
 						qm.messageStatus = MessageStatusType.INTERNAL_ERROR;
 					else if (status==HttpStatus.SC_BAD_REQUEST)
 						qm.messageStatus = MessageStatusType.INVALID_REQUEST;
 					else if (status==HttpStatus.SC_FORBIDDEN)
 						qm.messageStatus = MessageStatusType.NOT_PERMITTED;
 					else
 						qm.messageStatus = MessageStatusType.INTERNAL_ERROR;
 					qm.errorMessage = "Error response ("+status+") from server: "+statusLine.getReasonPhrase()+", URL="+request.getURI();
 				}
 				return null;
 			}
 		}
 		try {
 			BufferedInputStream in = new BufferedInputStream(reply.getEntity().getContent());
 			Log.d(TAG,"Reading HTTP response -> XML");
 			messages = (List<Message>)xs.fromXML(new InputStreamReader(in, Charset.forName("UTF-8")));
 			reply.getEntity().consumeContent();
 		}
 		catch (Exception e) {
 			Log.e(TAG,"Reading response", e);
 			log("sendQueuedMessage.error", "error", e.toString());
 			synchronized(queuedMessages) {
 				currentMessage = null;
 				if (qm.cancelled)
 					return null;
 				synchronized (qm) {
 					qm.status = QueuedMessageStatus.DONE;
 					qm.messageStatus = MessageStatusType.NETWORK_ERROR;
 					qm.errorMessage = "Error reading response: "+e.getMessage();
 				}
 				return null;
 			}
 		}
 		log("sendQueueMessage.ok","messages",Arrays.toString(messages.toArray()));
 		Log.i(TAG, "Response "+messages.size()+" messages: "+messages);
 
 		synchronized(queuedMessages) {
 			currentMessage = null;
 			if (qm.cancelled)
 				return null;
 			synchronized (qm) {
 				qm.status = QueuedMessageStatus.DONE;
 				qm.errorMessage = null;
 				qm.messageStatus = MessageStatusType.OK;
 				// check status(es)
 				for (Message response : messages) {
 					if (response.getType().equals(MessageType.ERROR.name())) {
 						qm.messageStatus = MessageStatusType.valueOf(response.getStatus());
 						qm.errorMessage = response.getErrorMsg();
 						break;
 					}
 				}
 				if (!messages.isEmpty())
 					qm.response = messages.get(0);
 			}
 		}
 		if (Thread.interrupted()) 
 			Log.d(TAG,"Client message Thread was intterupted");
 		
 		return messages;
 	}
 	
 //
 //		return messages;
 //	}
 	/** class name -> id -> fact */
 	protected HashMap<String,HashMap<Object,Object>> facts = new HashMap<String,HashMap<Object,Object>> ();
 	/** lock facts before using is a good idea */
 	public HashMap<String,HashMap<Object,Object>> getFacts() {
 		return facts;
 	}
 	public Object getFirstFact(String typeName) {
 		synchronized (facts) {
 			HashMap<Object,Object> typeFacts = facts.get(typeName);
 			if (typeFacts!=null && typeFacts.size()>0) {
 				return typeFacts.values().iterator().next();
 			}
 			return null;
 		}
 		
 	}
 	public List<Object> getFacts(String typeName) {
 		
 		synchronized (facts) {
 			LinkedList<Object> fs = new LinkedList<Object>();
 			HashMap<Object,Object> typeFacts = facts.get(typeName);
 			if (typeFacts!=null) {
 				fs.addAll(typeFacts.values());
 			}
 			return fs;
 		}
 	}
 	/** poll 
 	 * @throws JSONException */
	public List<Message> poll() throws IOException {
 		log("poll");
 		Message msg = new Message();
 		msg.setSeqNo(seqNo++);
 		msg.setType(MessageType.POLL.name());
		//msg.setToFollow(0);
         synchronized (ackSeqs) {
             int ackSeqsInt [] = new int[ackSeqs.size()];
             for (int i=0; i<ackSeqs.size(); i++)
                     ackSeqsInt[i] = ackSeqs.get(i);
             ackSeqs.clear();
             msg.setAckSeqs(ackSeqsInt);
         }
 		
 		QueuedMessage qm = new QueuedMessage();
 		qm.message = msg;
 		List<Message> messages = sendQueuedMessageInternal(qm);
 		if (messages==null) {
 			if (qm.messageStatus!=MessageStatusType.OK)
 				throw new IOException(qm.errorMessage!=null ? qm.errorMessage : qm.messageStatus.toString());
 			return messages;
 		}
 		Set<String> changedTypes = new HashSet<String>();
 		synchronized (facts) {
 			for (Message message : messages) {
 				// selective ack
 				if (message.getSeqNo()>0) {
 					synchronized(ackSeqs) {
 						ackSeqs.add(message.getSeqNo());
 					}
 				}
 				MessageType messageType = MessageType.valueOf(message.getType());
 				if (messageType==MessageType.FACT_EX || messageType==MessageType.FACT_ADD) {
 					Object val = message.getNewVal();
 					String typeName = val.getClass().getName();
 					HashMap<Object,Object> typeFacts = facts.get(typeName);
 					if (typeFacts==null) {
 						typeFacts = new HashMap<Object,Object>();
 						facts.put(typeName,typeFacts);
 					}
 					typeFacts.put(getFactID(val), val);
 					Log.d(TAG,"Add fact "+val);
 					log("addFact", "newVal", val.toString());
 					if (!changedTypes.contains(typeName))
 						changedTypes.add(typeName);
 					checkSpecialCaseUpdates(val);
 				} else if (messageType==MessageType.FACT_UPD || messageType==MessageType.FACT_DEL) {
 					Object val = message.getOldVal();
 					String typeName = val.getClass().getName();
 					HashMap<Object,Object> typeFacts = facts.get(typeName);
 					if (typeFacts==null) {
 						typeFacts = new HashMap<Object,Object>();
 						facts.put(typeName,typeFacts);
 					}
 					Object key = getFactID(val);
 					boolean found = typeFacts.containsKey(key);
 					if (found) {
 						typeFacts.remove(key);
 						Log.i(TAG,"Removing/update old fact "+val);
 						if (messageType==MessageType.FACT_DEL)
 							log("deleteFact", "oldVal", val.toString());
 						if (!changedTypes.contains(typeName))
 							changedTypes.add(typeName);						
 					}
 					else {
 						log("deleteFact.notfound", "oldVal", val.toString());
 						Log.i(TAG, "Did not find old fact to remove: "+val);
 					}
 					if (messageType==MessageType.FACT_UPD) {
 						val = message.getNewVal();
 						log("updateFact", "newVal", val.toString());
 						if (!val.getClass().getName().equals(typeName))
 							Log.e(TAG, "Nominal Update from class "+typeName+" to "+val.getClass().getName());
 						typeFacts = facts.get(typeName);
 						if (typeFacts==null) {
 							typeFacts = new HashMap<Object,Object>();
 							facts.put(typeName,typeFacts);
 						}
 						Object newKey = getFactID(val);
 						if (!key.equals(newKey)) 
 							Log.e(TAG,"Nominal Update from ID "+key+" to "+newKey+" for "+val);
 						typeFacts.put(newKey, val);
 						Log.i(TAG,"Update new fact "+val);
 						if (!changedTypes.contains(typeName))
 							changedTypes.add(typeName);
 						checkSpecialCaseUpdates(val);
 					}
 				}
 			}
 		}
 		BackgroundThread.cachedStateChanged(this, changedTypes);
 		return messages;
 	}
 	/**
 	 * @param val
 	 */
 	private void checkSpecialCaseUpdates(Object val) {
 		if (val instanceof Game) {
 			Game game = (Game)val;
 			if (game.isSetState()) {
 				Log.d(TAG,"Update game state to "+game.getState());
 				BackgroundThread.setGameStatus(GameStatus.valueOf(game.getState()));
 			}
 		}
 	}
 	/** remove a Fact from the cache without signalling state change */
 	public void removeFactSilent(Object fact) {
 		if (fact==null)
 			return;
 		log("removeFactSilent", "fact", fact.toString());
 		synchronized(facts) {
 			String typeName = getFactType(fact);
 			HashMap<Object,Object> typeFacts = facts.get(typeName);
 			if (typeFacts!=null) {
 				Object id = getFactID(fact);
 				if (id!=null) {
 					if (typeFacts.remove(id)==null)
 						Log.w(TAG, "removeFactSilent could not find "+typeName+" "+id);
 				} 
 				else
 					Log.w(TAG,"removeFactSilent called with object without id: "+fact);
 			}
 			else
 				Log.w(TAG, "removeFactSilent could not find any "+typeName);
 		}
 	}
 	private static String getFactType(Object o) {
 		return o.getClass().getName();		
 	}
 	/** HACK application specific */
 	private static Object getFactID(Object o) {
 		if (o instanceof Player) {
 			Player p = (Player)o;
 			return p.getID();
 		}
 		if (o instanceof uk.ac.horizon.ug.exploding.client.model.Message) {
 			uk.ac.horizon.ug.exploding.client.model.Message p = (uk.ac.horizon.ug.exploding.client.model.Message)o;
 			return p.getID();
 		}
 		if (o instanceof Member) {
 			Member p = (Member)o;
 			return p.getID();
 		}
 		if (o instanceof Zone) {
 			Zone p = (Zone)o;
 			return p.getID();
 		}
 		if (o instanceof Game) {
 			Game p = (Game)o;
 			return p.getID();
 		}
 		Log.d(TAG,"getFactID for unknown class "+o.getClass().getName());
 		return o; //!?!
 	}
 }
