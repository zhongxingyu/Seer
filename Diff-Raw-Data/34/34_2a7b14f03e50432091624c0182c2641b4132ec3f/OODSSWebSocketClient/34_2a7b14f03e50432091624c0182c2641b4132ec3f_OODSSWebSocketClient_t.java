 /*
  * Created on May 12, 2006
  */
 package ecologylab.oodss.distributed.client;
 
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.net.BindException;
 import java.net.InetSocketAddress;
 import java.net.PortUnreachableException;
 import java.net.SocketException;
 import java.nio.BufferOverflowException;
 import java.nio.ByteBuffer;
 import java.nio.CharBuffer;
 import java.nio.channels.ClosedChannelException;
 import java.nio.channels.SelectionKey;
 import java.nio.channels.SocketChannel;
 import java.nio.charset.CharacterCodingException;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Queue;
 import java.util.concurrent.LinkedBlockingQueue;
 import java.util.zip.DataFormatException;
 import java.util.zip.Deflater;
 import java.util.zip.Inflater;
 
 
 import org.jwebsocket.api.WebSocketClientEvent;
 import org.jwebsocket.api.WebSocketClientTokenListener;
 import org.jwebsocket.api.WebSocketPacket;
 import org.jwebsocket.client.token.BaseTokenClient;
 import org.jwebsocket.kit.WebSocketException;
 import org.jwebsocket.token.Token;
 
 
 import ecologylab.collections.Scope;
 import ecologylab.generic.Debug;
 import ecologylab.generic.Generic;
 import ecologylab.generic.StringBuilderPool;
 import ecologylab.generic.StringTools;
 import ecologylab.oodss.distributed.common.ClientConstants;
 import ecologylab.oodss.distributed.common.ServerConstants;
 import ecologylab.oodss.distributed.exception.MessageTooLargeException;
 //import ecologylab.oodss.distributed.impl.AIONetworking;
 import ecologylab.oodss.distributed.impl.MessageWithMetadata;
 import ecologylab.oodss.distributed.impl.MessageWithMetadataPool;
 import ecologylab.oodss.distributed.impl.NIONetworking;
 import ecologylab.oodss.distributed.impl.PreppedRequest;
 import ecologylab.oodss.distributed.impl.PreppedRequestPool;
 import ecologylab.oodss.exceptions.BadClientException;
 import ecologylab.oodss.messages.DisconnectRequest;
 import ecologylab.oodss.messages.InitConnectionRequest;
 import ecologylab.oodss.messages.InitConnectionResponse;
 import ecologylab.oodss.messages.PingRequest;
 import ecologylab.oodss.messages.RequestMessage;
 import ecologylab.oodss.messages.ResponseMessage;
 import ecologylab.oodss.messages.SendableRequest;
 import ecologylab.oodss.messages.ServiceMessage;
 import ecologylab.oodss.messages.UpdateMessage;
 import ecologylab.serialization.ClassDescriptor;
 import ecologylab.serialization.SIMPLTranslationException;
 import ecologylab.serialization.TranslationScope;
 import ecologylab.serialization.ElementState.FORMAT;
 
 /**
  * Services Client using NIO; a major difference with the NIO version is state tracking. Since the
  * sending methods do not wait for the server to return.
  * 
  * This object will listen for incoming messages from the server, and will send any messages that it
  * receives on its end.
  * 
  * Since the underlying implementation is TCP/IP, messages sent should be sent in order, and the
  * responses should match that order.
  * 
  * Another major difference between this and the non-NIO version of ServicesClient is that it is
  * StartAndStoppable.
  * 
  * @author Zachary O. Dugas Toups (zach@ecologylab.net)
  */
 public class OODSSWebSocketClient<S extends Scope> extends Debug implements Runnable,
 		ClientConstants, WebSocketClientTokenListener
 {
 	protected String																									serverAddress;
 	protected final CharBuffer																				outgoingChars;
 
 	protected Scope objectRegistry;
 	/**
 	 * A temporary buffer of characters into which requests are placed before they are moved to
 	 * outgoingChars.
 	 */
 	protected final StringBuilder																			requestBuffer;
 
 	/**
 	 * Stores incoming character data until it can be parsed into an XML message and turned into a
 	 * Java object.
 	 */
 	protected final StringBuilder																			incomingMessageBuffer;
 
 	/** Stores outgoing character data for ResponseMessages. */
 	protected final StringBuilder																			outgoingMessageBuffer;
 
 	/** Stores outgoing header character data. */
 	protected final StringBuilder																			outgoingMessageHeaderBuffer;
 
 	/**
 	 * stores the sequence of characters read from the header of an incoming message, may need to
 	 * persist across read calls, as the entire header may not be sent at once.
 	 */
 	private final StringBuilder																				currentHeaderSequence					= new StringBuilder();
 
 	/**
 	 * stores the sequence of characters read from the header of an incoming message and identified as
 	 * being a key for a header entry; may need to persist across read calls.
 	 */
 	private final StringBuilder																				currentKeyHeaderSequence			= new StringBuilder();
 
 	private MessageWithMetadata<ServiceMessage, Object>								response											= null;
 
 	private volatile boolean																					blockingRequestPending				= false;
 
 	private final Queue<MessageWithMetadata<ServiceMessage, Object>>	blockingResponsesQueue				= new LinkedBlockingQueue<MessageWithMetadata<ServiceMessage, Object>>();
 
 
 	private Queue<ResponseMessage>	newBlockingResponsesQueue				= new LinkedBlockingQueue<ResponseMessage>();
 
 	
 	protected final Queue<PreppedRequest>															requestsQueue									= new LinkedBlockingQueue<PreppedRequest>();
 
 	/**
 	 * A map that stores all the requests that have not yet gotten responses. Maps UID to
 	 * RequestMessage.
 	 */
 	protected final Map<Long, PreppedRequest>													unfulfilledRequests						= new HashMap<Long, PreppedRequest>();
 
 	/**
 	 * The number of times a call to reconnect() should attempt to contact the server before giving up
 	 * and calling stop().
 	 */
 	protected int																											reconnectAttempts							= RECONNECT_ATTEMPTS;
 
 	/** The number of milliseconds to wait between reconnect attempts. */
 	protected int																											waitBetweenReconnectAttempts	= WAIT_BEWTEEN_RECONNECT_ATTEMPTS;
 
 	private String																										sessionId											= null;
 
 	protected ReconnectBlocker																				blocker												= null;
 
 	/**
 	 * selectInterval is passed to select() when it is called in the run loop. It is set to 0
 	 * indicating that the loop should block until the selector picks up something interesting.
 	 * However, if this class is subclassed, it is possible to modify this value so that the select()
 	 * will only block for the number of ms supplied by this field. Thus, it is possible (by also
 	 * subclassing the sendData() method) to have this send data on an interval, and then select.
 	 */
 	protected long																										selectInterval								= 0;
 
 	protected boolean																									isSending											= false;
 
 	/**
 	 * Contains the unique identifier for the next message that the client will send.
 	 */
 	private long																											uidIndex											= 1;
 
 	private int																												endOfFirstHeader							= -1;
 
 	protected int																											startReadIndex								= 0;
 
 	private int																												uidOfCurrentMessage						= -1;
 
 	/**
 	 * Counts how many characters still need to be extracted from the incomingMessageBuffer before
 	 * they can be turned into a message (based upon the HTTP header). A value of -1 means that there
 	 * is not yet a complete header, so no length has been determined (yet).
 	 */
 	private int																												contentLengthRemaining				= -1;
 
 	/**
 	 * Stores the first XML message from the incomingMessageBuffer, or parts of it (if it is being
 	 * read over several invocations).
 	 */
 	private final StringBuilder																				firstMessageBuffer						= new StringBuilder();
 
 	/**
 	 * Whether or not to allow server to reply using compression
 	 */
 	private boolean																										allowCompression							= false;
 
 	/**
 	 * Whether or not to send compressed requests.
 	 */
 	private boolean																										sendCompressed								= false;
 
 	/**
 	 * Stores the key-value pairings from a parsed HTTP-like header on an incoming message.
 	 */
 	protected final HashMap<String, String>														headerMap											= new HashMap<String, String>();
 
 	protected SocketChannel																						thisSocket										= null;
 
 	protected final PreppedRequestPool																pRequestPool;
 
 	protected final MessageWithMetadataPool<ServiceMessage, Object>		responsePool									= new MessageWithMetadataPool<ServiceMessage, Object>(
 																																																			2,
 																																																			4);
 
 	private final StringBuilderPool																		builderPool;
 
 	private int																												maxMessageLengthChars;
 
 	private CharBuffer																								zippingChars;
 
 	private ByteBuffer																								zippingInBytes;
 
 	private Deflater																									deflater											= new Deflater();
 
 	private String																										contentEncoding;
 
 	private Inflater																									inflater											= new Inflater();
 
 	private ByteBuffer																								zippingOutBytes;
 
 	private ByteBuffer																								compressedMessageBuffer;
 
 	private List<ClientStatusListener>																clientStatusListeners					= null;
 	private TranslationScope translationScope;
 
 	public OODSSWebSocketClient(String serverAddress, int portNumber, TranslationScope messageSpace,
 			S objectRegistry, int maxMessageLengthChars) throws IOException
 	{
 		//super("AIOClient", portNumber, messageSpace, objectRegistry, maxMessageLengthChars);
 
 		//I know the above and much below is unnecessary glut.
 		this.objectRegistry = objectRegistry;
 		
 
 		this.translationScope = messageSpace;
 		this.maxMessageLengthChars = maxMessageLengthChars;
 
 		this.outgoingChars = CharBuffer.allocate(maxMessageLengthChars);
 
 		this.outgoingMessageBuffer = new StringBuilder(maxMessageLengthChars);
 		this.outgoingMessageHeaderBuffer = new StringBuilder(MAX_HTTP_HEADER_LENGTH);
 		this.requestBuffer = new StringBuilder(maxMessageLengthChars);
 		this.incomingMessageBuffer = new StringBuilder(maxMessageLengthChars);
 
 		builderPool = new StringBuilderPool(2, 4, maxMessageLengthChars);
 		pRequestPool = new PreppedRequestPool(2, 4, maxMessageLengthChars);
 
 		zippingChars = CharBuffer.allocate(maxMessageLengthChars);
 		zippingInBytes = ByteBuffer.allocate(maxMessageLengthChars);
 		zippingOutBytes = ByteBuffer.allocate(maxMessageLengthChars);
 		this.compressedMessageBuffer = ByteBuffer.allocate(maxMessageLengthChars);
 
 		this.serverAddress = serverAddress;
 		objectRegistry.put("session_id", this.sessionId);
 		objectRegistry.put("test", "its here buddy");
 
 	
 		initializeWebSocketClient();//this is called before start()
 	}
 
 	public OODSSWebSocketClient(String serverAddress, int portNumber, TranslationScope messageSpace,
 			S objectRegistry) throws IOException
 	{
 		this(serverAddress, portNumber, messageSpace, objectRegistry, DEFAULT_MAX_MESSAGE_LENGTH_CHARS);
 	}
 
 	/**
 	 * If this client is not already connected, connects to the specified serverAddress on the
 	 * specified portNumber, then calls start() to begin listening for server responses and processing
 	 * them, then sends handshake data and establishes the session id.
 	 * 
 	 * @see ecologylab.oodss.distributed.legacy.ServicesClientBase#connect()
 	 */
 	public boolean connect()
 	{
 		debug("initializing connection... NOT REALLY");
 	
 		return connected();
 	}
 
 	/**
 	 * Connect to the server (if not already connected). Return connection status.
 	 * 
 	 * @return True if connected, false if not.
 	 */
 	private boolean connectImpl()
 	{
 		return connected() ? true : createConnection();
 	}
 
 	/**
 	 * Sets the UID for request (if necessary), enqueues it then registers write interest for the
 	 * NIOClient's selection key and calls wakeup() on the selector.
 	 * 
 	 * @param request
 	 * @throws SIMPLTranslationException
 	 */
 	protected PreppedRequest prepareAndEnqueueRequestForSending(SendableRequest request)
 			throws SIMPLTranslationException, MessageTooLargeException
 	{
 		int reqLength = requestBuffer.length();
 		if (reqLength > this.maxMessageLengthChars)
 			throw new MessageTooLargeException(this.maxMessageLengthChars, reqLength);
 		
 		long uid = this.generateUid();
 
 		PreppedRequest pReq = null;
 
 		synchronized (requestBuffer)
 		{
 			// fill requestBuffer
 			request.serialize(requestBuffer);
 
 			// TODO not convinced this is the most efficient workflow. Why do we need requestBuffer at all??? -ZODT
 			// drain requestBuffer and fill a prepped request
 			pReq = this.pRequestPool.acquire();
 			pReq.setRequest(requestBuffer);
 			pReq.setUid(uid);
 			pReq.setDisposable(request.isDisposable());
 
 			// clear requestBuffer
 			requestBuffer.setLength(0);
 		}
 
 		if (pReq != null)
 			enqueueRequestForSending(pReq);
 
 		return pReq;
 	}
 
 	protected void enqueueRequestForSending(PreppedRequest request)
 	{
 		synchronized (requestsQueue)
 		{
 			requestsQueue.add(request);
 		}
 
 	//	this.queueForWrite(this.thisSocket.keyFor(selector));
 
 		//selector.wakeup();
 	}
 
 	public void disconnect(boolean waitForResponses)
 	{
 		int attemptsCounter = 0;
 		while (this.requestsRemaining() > 0 && this.connected() && waitForResponses
 				&& attemptsCounter++ < 10)
 		{
 			debug("******************* Request queue not empty, finishing " + requestsRemaining()
 					+ " messages before disconnecting (attempt " + attemptsCounter + ")...");
 			synchronized (this)
 			{
 				try
 				{
 					wait(100);
 				}
 				catch (InterruptedException e)
 				{
 					e.printStackTrace();
 				}
 			}
 		}
 
 		debug("* starting disconnect process...");
 
 		try
 		{
 			if (connected())
 			{
 				debug("** currently connected...");
 
 				while (waitForResponses && connected() && !this.shutdownOK() && attemptsCounter-- > 0)
 				{
 					debug("*** " + this.unfulfilledRequests.size()
 							+ " requests still pending response from server (attempt " + attemptsCounter + ").");
 					debug("*** connected: " + connected());
 
 					synchronized (this)
 					{
 						try
 						{
 							wait(100);
 						}
 						catch (InterruptedException e)
 						{
 							e.printStackTrace();
 						}
 					}
 				}
 
 				// disconnect properly...
 				if (this.sessionId != null)
 				{
 					debug("**** session still active; handling disconnecting messages...");
 					this.handleDisconnectingMessages();
 					this.sessionId = null;
 				}
 			}
 		}
 		finally
 		{
 			nullOut();
 			///stop();
 		}
 	}
 
 	/**
 	 * Hook method for subclasses to provide specific disconnect messages. For example, authenticating
 	 * clients will want to log out.
 	 */
 	protected void handleDisconnectingMessages()
 	{
 		debug("************** sending disconnect request");
 	//	try
 	//	{
 			this.sendMessage(DisconnectRequest.REUSABLE_INSTANCE, 10000);
 	//	}
 		/*catch (MessageTooLargeException e)
 		{
 			// this shouldn't be able to happen, unless the maximum request size
 			// gets set below the size of a DisconnectRequest
 			e.printStackTrace();
 		}*/
 	}
 
 	/**
 	 * @return
 	 */
 	protected boolean shutdownOK()
 	{
 		return !(this.unfulfilledRequests.size() > 0);
 	}
 
 	protected void nullOut()
 	{
 		if (thisSocket != null)
 		{
 			synchronized (thisSocket)
 			{
 				debug("null out");
 
 				thisSocket = null;
 			}
 		}
 	}
 
 	public boolean connected()
 	{
 		//this is wrong...
 		return true;//(thisSocket != null) && !thisSocket.isConnectionPending() && thisSocket.isConnected();
 	}
 
 	/**
 	 * Side effect of calling start().
 	 */
 	protected boolean createConnection()
 	{
 		
 
 		return connected();
 	}
 
 	/**
 	 * Hook method to allow subclasses to deal with a failed restore after disconnect. This should be
 	 * a rare occurance, but some sublcasses may need to deal with this case specifically.
 	 * 
 	 * @param oldId
 	 *          - the previous session id.
 	 * @param newId
 	 *          - the new session id given by the server after reconnect.
 	 */
 	protected void unableToRestorePreviousConnection(String oldId, String newId)
 	{
 	}
 
 	/**
 	 * Sends request, but does not wait for the response. The response gets processed later in a
 	 * non-stateful way by the run method.
 	 * 
 	 * @param request
 	 *          the request to send to the server.
 	 * 
 	 * @return the UID of request.
 	 */
 	public PreppedRequest nonBlockingSendMessage(RequestMessage request) throws IOException,
 			MessageTooLargeException
 	{
 		if (connected())
 		{
 			try
 			{
 				return this.prepareAndEnqueueRequestForSending(request);
 			}
 			catch (SIMPLTranslationException e)
 			{
 				error("error translating message; returning null");
 				e.printStackTrace();
 
 				return null;
 			}
 		}
 		else
 		{
 			throw new IOException("Not connected to server.");
 		}
 	}
 
 	/**
 	 * Blocking send. Sends the request and waits infinitely for the response, which it returns.
 	 * 
 	 * @throws MessageTooLargeException
 	 * 
 	 * @see ecologylab.oodss.distributed.legacy.ServicesClientBase#sendMessage(ecologylab.oodss.messages.RequestMessage)
 	 */
 	public synchronized ResponseMessage sendMessage(RequestMessage request)
 			throws MessageTooLargeException
 	{
 		return this.sendMessage(request, -1);
 	}
 
 	/**
 	 * Blocking send with timeout. Sends the request and waits timeOutMillis milliseconds for the
 	 * response, which it returns. sendMessage(RequestMessage, int) will return null if no message was
 	 * received in time.
 	 * 
 	 * @param request
 	 * @param timeOutMillis
 	 * @return
 	 * @throws MessageTooLargeException
 	 */
 	public synchronized ResponseMessage sendMessage(SendableRequest request, int timeOutMillis)
 			//throws MessageTooLargeException
 	{
 		
 		// notify the connection thread that we are waiting on a response
 		blockingRequestPending = true;
 		debug("I actually think Iwill wait here...");
 		
 		PingRequest p = new PingRequest();
 		RequestMessage requestMessage = (RequestMessage) request;
 		
 		ByteArrayOutputStream outStream = new ByteArrayOutputStream();
 		try {
 			
 			requestMessage.serialize(outStream, FORMAT.JSON);
 		} catch (SIMPLTranslationException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		
 		String pJSON = new String(outStream.toByteArray());
 		System.out.println("pJSON:"+pJSON);
 		try {
 			debug("sending the request message "+pJSON);
 			webSocketClient.sendText("5",pJSON);
 		} catch (WebSocketException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		
 		
 		long currentMessageUid;
 
 		boolean blockingRequestFailed = false;
 		long startTime = System.currentTimeMillis();
 		int timeCounter = 0;
 
 		
 		// wait to be notified that the response has arrived
 		while (blockingRequestPending)// && !blockingRequestFailed)
 		{
 			if (timeOutMillis <= -1)
 			{
 				debug("HEY....   waiting on blocking request");
 				//debug("psyce!");
 				//return null;
 			}
 
 			try
 			{
 				if (timeOutMillis > -1)
 				{
 					wait(timeOutMillis);
 				}
 				else
 				{
 					wait();
 				}
 			}
 			catch (InterruptedException e)
 			{
 				e.printStackTrace();
 				Thread.interrupted();
 			}
 
 			debug("waking UP TO GET THE MESSAGE");
 
 			timeCounter += System.currentTimeMillis() - startTime;
 			startTime = System.currentTimeMillis();
 
 			ResponseMessage responseMessage = null;
 			while ((blockingRequestPending) && (!newBlockingResponsesQueue.isEmpty()))
 			{
 				responseMessage = newBlockingResponsesQueue.poll();
 
 			//	if (responseMessage.getUid() == currentMessageUid)
 				//{
 					debug("I think got the right response: "); //+ currentMessageUid);
 
 					blockingRequestPending = false;
 
 					blockingResponsesQueue.clear();
 
 					//ResponseMessage respMsg = (ResponseMessage) responseMessage.getMessage();
 
 					// try
 					// {
 					// debug("response: " + respMsg.serialize().toString());
 					// }
 					// catch (SIMPLTranslationException e)
 					// {
 					// e.printStackTrace();
 					// }
 
 					//responseMessage = responsePool.release(responseMessage);
 
 					return responseMessage;//respMsg;
 				//}
 
 			//	responseMessage = responsePool.release(responseMessage);
 			}
 
 			if ((timeOutMillis > -1) && (timeCounter >= timeOutMillis) && (blockingRequestPending))
 			{
 				blockingRequestFailed = true;
 			}
 		}
 
 		if (blockingRequestFailed)
 		{
 			debug("Request failed due to timeout!");
 		}
 
 		return null;
 		
 	}
 
 
 	
 
 	/**
 	 * Returns the next request in the request queue and removes it from that queue. Sublcasses that
 	 * override the queue functionality will need to override this method.
 	 * 
 	 * @return the next request in the request queue.
 	 */
 	protected PreppedRequest dequeueRequest()
 	{
 		return this.requestsQueue.poll();
 	}
 
 	/**
 	 * Returns the number of requests remaining in the requests queue. Subclasses that override the
 	 * queue functionality will need to change this method accordingly.
 	 * 
 	 * @return the size of the request queue.
 	 */
 	protected int requestsRemaining()
 	{
 		return this.requestsQueue.size();
 	}
 
 	/**
 	 * Attempts to reconnect this client if it has been disconnected. After reconnecting, re-queues
 	 * all requests still in the unfulfilledRequests map.
 	 * 
 	 * If the attempt to reconnect fails, reconnect() will attempt a number of times equal to
 	 * reconnectAttempts, waiting waitBetweenReconnectAttempts milliseconds between attempts. If all
 	 * such attempts fail, calls stop() on this to shut down the client. The client will then need to
 	 * be re-started manually.
 	 * 
 	 */
 	protected void reconnect()
 	{
 		debug("attempting to reconnect...");
 		int reconnectsRemaining = this.reconnectAttempts;
 		if (reconnectsRemaining < 0)
 		{
 			reconnectsRemaining = 1;
 		}
 
 		while (!connected() && reconnectsRemaining > 0)
 		{
 			this.nullOut();
 
 			// attempt to connect, if failed, wait
 			if (!this.connect() && --reconnectsRemaining > 0)
 			{
 				try
 				{
 					this.wait(this.waitBetweenReconnectAttempts);
 				}
 				catch (InterruptedException e)
 				{
 					e.printStackTrace();
 				}
 			}
 		}
 
 		if (connected())
 		{
 			synchronized (unfulfilledRequests)
 			{
 				List<PreppedRequest> rerequests = new LinkedList<PreppedRequest>(
 						this.unfulfilledRequests.values());
 
 				Collections.sort(rerequests);
 
 				for (PreppedRequest req : rerequests)
 				{
 					this.enqueueRequestForSending(req);
 				}
 			}
 		}
 		else
 		{
 			//this.stop();
 		}
 	}
 
 	/**
 	 * Hook method to allow subclasses to deal with unfulfilled requests in their own way.
 	 * 
 	 * Adds req to the unfulfilled requests map.
 	 * 
 	 * @param req
 	 */
 	protected void addUnfulfilledRequest(PreppedRequest req)
 	{
 		synchronized (unfulfilledRequests)
 		{
 			this.unfulfilledRequests.put(req.getUid(), req);
 		}
 	}
 
 	/**
 	 * Stores the request in the unfulfilledRequests map according to its UID, converts it to XML,
 	 * prepends the HTTP-like header, then writes it out to the channel. Then re-registers key for
 	 * reading.
 	 * 
 	 * @param pReq
 	 */
 	private void createPacketFromMessageAndSend(PreppedRequest pReq, SelectionKey incomingKey)
 	{
 		StringBuilder outgoingReq = pReq.getRequest();
 
 		this.addUnfulfilledRequest(pReq);
 
 		StringBuilder message = null;
 
 		try
 		{
 			message = builderPool.acquire();
 
 			if (this.sendCompressed)
 			{
 				this.compressedMessageBuffer.clear();
 
 				try
 				{
 					this.compress(outgoingReq, compressedMessageBuffer);
 					compressedMessageBuffer.flip();
 				}
 				catch (DataFormatException e)
 				{
 					e.printStackTrace();
 					return;
 				}
 			}
 
 			message.append(CONTENT_LENGTH_STRING);
 			message.append(':');
 			if (!this.sendCompressed)
 			{
 				message.append(outgoingReq.length());
 			}
 			else
 			{
 				message.append(compressedMessageBuffer.limit() - compressedMessageBuffer.position());
 			}
 			message.append(HTTP_HEADER_LINE_DELIMITER);
 
 			message.append(UNIQUE_IDENTIFIER_STRING);
 			message.append(':');
 			message.append(pReq.getUid());
 
 			if (allowCompression)
 			{
 				message.append(HTTP_HEADER_LINE_DELIMITER);
 				message.append(HTTP_ACCEPTED_ENCODINGS);
 			}
 			if (this.sendCompressed)
 			{
 				message.append(HTTP_HEADER_LINE_DELIMITER);
 				message.append(HTTP_CONTENT_CODING);
 				message.append(":");
 				message.append(HTTP_DEFLATE_ENCODING);
 			}
 
 			message.append(HTTP_HEADER_TERMINATOR);
 
 			if (!this.sendCompressed)
 			{
 				message.append(outgoingReq);
 			}
 
 			outgoingChars.clear();
 
 			int capacity;
 
 			while (message.length() > 0)
 			{
 				outgoingChars.clear();
 				capacity = outgoingChars.capacity();
 
 				if (message.length() > capacity)
 				{
 					outgoingChars.put(message.toString(), 0, capacity);
 					message.delete(0, capacity);
 				}
 				else
 				{
 					outgoingChars.put(message.toString());
 					message.delete(0, message.length());
 				}
 
 				outgoingChars.flip();
 
 /*				synchronized (encoder)
 				{
 					// XXX
 					// debug(outgoingChars.toString());
 					ByteBuffer encodedBytes = encoder.encode(outgoingChars);
 					int sentBytes = 0;
 					int bytesToSend = encodedBytes.limit();
 
 					while (bytesToSend > sentBytes)
 					{
 						sentBytes += thisSocket.write(encodedBytes);
 						// debug("sentBytes: "+sentBytes);
 					}
 				}
 				*/
 			}
 			if (this.sendCompressed)
 			{
 				thisSocket.write(compressedMessageBuffer);
 			}
 		}
 		catch (ClosedChannelException e)
 		{
 			debug("connection severed; disconnecting and storing requests...");
 			//setPendingInvalidate(incomingKey, false);
 		}
 		catch (BufferOverflowException e)
 		{
 			debug("buffer overflow.");
 			e.printStackTrace();
 			System.out.println("capacity: " + outgoingChars.capacity());
 			System.out.println("outgoing request: " + outgoingReq);
 		}
 		catch (NullPointerException e)
 		{
 			e.printStackTrace();
 			System.out.println("recovering.");
 		}
 		catch (CharacterCodingException e)
 		{
 			e.printStackTrace();
 		}
 		catch (IOException e)
 		{
 			debug("connection severed; disconnecting...");
 			this.disconnect(false);
 		}
 		finally
 		{
 			message = builderPool.release(message);
 		}
 	}
 
 	private void compress(StringBuilder src, ByteBuffer dest) throws DataFormatException
 	{
 		synchronized (zippingChars)
 		{
 			zippingChars.clear();
 
 			src.getChars(0, src.length(), this.zippingChars.array(), 0);
 			zippingChars.position(0);
 			zippingChars.limit(src.length());
 
 			zippingInBytes.clear();
 			
 			/*
 
 			encoder.reset();
 			encoder.encode(zippingChars, zippingInBytes, true);
 			encoder.flush(zippingInBytes);
 			*/
 
 
 			zippingInBytes.flip();
 
 			deflater.reset();
 			deflater.setInput(zippingInBytes.array(), zippingInBytes.position(), zippingInBytes.limit());
 			deflater.finish();
 
 			dest.position(dest.position()
 					+ deflater.deflate(dest.array(), dest.position(), dest.remaining()));
 		}
 	}
 
 	private void processUpdate(UpdateMessage message)
 	{
 		message.processUpdate(objectRegistry);
 		System.out.println("Should be doing a processUpdate but I don't know what function to call!!!");
 	}
 
 	/**
 	 * Converts incomingMessage to a ResponseMessage, then processes the response and removes its UID
 	 * from the unfulfilledRequests map.
 	 * 
 	 * @param incomingMessage
 	 * @return
 	 */
 	private MessageWithMetadata<ServiceMessage, Object> processString(String incomingMessage,
 			int incomingUid)
 	{
 
 		if (show(5))
 			debug("incoming message: " + incomingMessage);
 
 		try
 		{
 			response = translateXMLStringToServiceMessage(incomingMessage, incomingUid);
 		}
 		catch (SIMPLTranslationException e)
 		{
 			e.printStackTrace();
 		}
 
 		if (response == null)
 		{
 			debug("ERROR: translation failed: ");
 		}
 		else
 		{
 			if (response.getMessage() instanceof ResponseMessage)
 			{
 				// perform the service being requested
 				processResponse((ResponseMessage) response.getMessage());
 
 				synchronized (unfulfilledRequests)
 				{
 					PreppedRequest finishedReq = unfulfilledRequests.remove(response.getUid());
 
 					if (finishedReq != null)
 					{ // subclasses might choose not to use unfulfilledRequests; this
 						// avoids problems with releasing resources;
 						// NOTE -- it may be necessary to release elsewhere in this case.
 						finishedReq = this.pRequestPool.release(finishedReq);
 					}
 				}
 			}
 			else if (response.getMessage() instanceof UpdateMessage)
 			{
 				debug("Guess what.  I noticed that is is an update message. YOU WIN!!!");
 				processUpdate((UpdateMessage) response.getMessage());
 			}
 		}
 
 		return response;
 	}
 
 
 	
 	
 	/**
 	 * Converts incomingMessage to a ResponseMessage, then processes the response and removes its UID
 	 * from the unfulfilledRequests map.
 	 * 
 	 * @param incomingMessage
 	 * @return
 	 */
 	//modified to use json
 	private ServiceMessage processString(String incomingMessage)
 	{
 
 		if (show(5))
 			debug("incoming message: " + incomingMessage);
 
 		ServiceMessage actualRespose = null;
 		try
 		{
 			actualRespose = translateJSONStringToServiceMessage(incomingMessage);
 		}
 		catch (SIMPLTranslationException e)
 		{
 			e.printStackTrace();
 		}
 
 		if (actualRespose == null)
 		{
 			debug("ERROR: translation failed: ");
 		}
 		else
 		{
 			if (actualRespose instanceof ResponseMessage)
 			{
 				// perform the service being requested
 				processResponse((ResponseMessage) actualRespose);
 				return actualRespose;
 
 			}
 			else if (actualRespose instanceof UpdateMessage)
 			{
 				//this will fail at the moment
 				//processUpdate((UpdateMessage) response.getMessage());
 				processUpdate((UpdateMessage) actualRespose);
 				
 			}
 		}
 
 		return null;
 		//return response;
 	}
 
 	
 	
 	
 	public void disconnect()
 	{
 		//disconnect(true);
 		try {
 			webSocketClient.disconnect();
 		} catch (WebSocketException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		//this.stop();
 	}
 
 	/**
 	 * @param reconnectAttempts
 	 *          the reconnectAttempts to set
 	 */
 	public void setReconnectAttempts(int reconnectAttempts)
 	{
 		this.reconnectAttempts = reconnectAttempts;
 	}
 
 	/**
 	 * @param waitBetweenReconnectAttempts
 	 *          the waitBetweenReconnectAttempts to set
 	 */
 	public void setWaitBetweenReconnectAttempts(int waitBetweenReconnectAttempts)
 	{
 		this.waitBetweenReconnectAttempts = waitBetweenReconnectAttempts;
 	}
 
 	protected void clearSessionId()
 	{
 		this.sessionId = null;
 	}
 
 	/**
 	 * This method does nothing, as NIOClients do not accept incoming connections.
 	 * 
 	 * @see ecologylab.oodss.distributed.impl.NIONetworking#acceptKey(java.nio.channels.SelectionKey)
 	 */
 //	@Override
 //	protected void acceptKey(SelectionKey key)
 //	{
 //	}
 
 		private CharSequence unCompress(StringBuilder firstMessageBuffer)
 			throws CharacterCodingException, DataFormatException
 	{
 		synchronized (zippingChars)
 		{
 			zippingChars.clear();
 
 			firstMessageBuffer.getChars(0, firstMessageBuffer.length(), this.zippingChars.array(), 0);
 			zippingChars.position(0);
 			zippingChars.limit(firstMessageBuffer.length());
 
 			zippingInBytes.clear();
 /*
 			encoder.reset();
 			encoder.encode(zippingChars, zippingInBytes, true);
 			encoder.flush(zippingInBytes);
 */
 			zippingInBytes.flip();
 
 			inflater.reset();
 			inflater.setInput(zippingInBytes.array(), zippingInBytes.position(), zippingInBytes.limit());
 
 			zippingOutBytes.clear();
 			inflater
 					.inflate(zippingOutBytes.array(), zippingOutBytes.position(), zippingOutBytes.limit());
 
 			zippingOutBytes.position(0);
 			zippingOutBytes.limit(inflater.getTotalOut());
 
 			zippingChars.clear();
 /*
 			decoder.reset();
 			decoder.decode(zippingOutBytes, zippingChars, true);
 			decoder.flush(zippingChars);
 */
 			zippingChars.flip();
 
 			firstMessageBuffer.setLength(0);
 
 			return firstMessageBuffer.append(zippingChars.array(), 0, zippingChars.limit());
 		}
 	}
 
 
 
 	/**
 	 * Increments the internal tracker of the next UID, and returns the current one.
 	 * 
 	 * @return the current uidIndex.
 	 */
 	public synchronized long generateUid()
 	{
 		// return the current value of uidIndex, then increment.
 		return uidIndex++;
 	}
 
 
 	/**
 	 * Use the ServicesClient and its NameSpace to do the translation. Can be overridden to provide
 	 * special functionalities
 	 * 
 	 * @param messageString
 	 * @return
 	 * @throws XMLTranslationException
 	 */
 	public ServiceMessage translateJSONStringToServiceMessage(
 			String messageString) throws SIMPLTranslationException
 	{
 		System.out.println("Derp");
 		System.out.println("Service message is:"+messageString);
 		
 		ServiceMessage resp = (ServiceMessage) this.translationScope
 				.deserializeCharSequence(messageString,FORMAT.JSON);
 
 		if (resp == null)
 		{
 			return null;
 		}
 
 		//MessageWithMetadata<ServiceMessage, Object> retVal = this.responsePool.acquire();
 
 		//retVal.setMessage(resp);
 	//	retVal.setUid(incomingUid);
 
 		return resp;
 	}
 	
 	/**
 	 * Use the ServicesClient and its NameSpace to do the translation. Can be overridden to provide
 	 * special functionalities
 	 * 
 	 * @param messageString
 	 * @return
 	 * @throws XMLTranslationException
 	 */
 	protected MessageWithMetadata<ServiceMessage, Object> translateXMLStringToServiceMessage(
 			String messageString, int incomingUid) throws SIMPLTranslationException
 	{
 		ServiceMessage resp = (ServiceMessage) this.translationScope
 				.deserializeCharSequence(messageString);
 
 		if (resp == null)
 		{
 			return null;
 		}
 
 		MessageWithMetadata<ServiceMessage, Object> retVal = this.responsePool.acquire();
 
 		retVal.setMessage(resp);
 		retVal.setUid(incomingUid);
 
 		return retVal;
 	}
 
 	/**
 	 * Process a ResponseMessage received from the server in response to a previously-sent
 	 * RequestMessage.
 	 * 
 	 * @param responseMessageToProcess
 	 */
 	protected void processResponse(ResponseMessage responseMessageToProcess)
 	{
 		//responseMessageToProcess.processResponse(objectRegistry);
 		System.out.println("Should be foing a processResponse but I'm not sure what function to call!!!");
 		
 		responseMessageToProcess.processResponse(objectRegistry);
 		
 	}
 
 	public String getServer()
 	{
 		return this.serverAddress;
 	}
 
 	public void setServer(String serverAddress)
 	{
 		this.serverAddress = serverAddress;
 	}
 
 	/**
 	 * Returns the most recently used UID.
 	 * 
 	 * @return the current uidIndex.
 	 */
 	public long getUidNoIncrement()
 	{
 		// return the current value of uidIndex
 		return uidIndex;
 	}
 
 	/**
 	 * Check to see if the server is running.
 	 * 
 	 * @return true if the server is running, false otherwise.
 	 */
 	public boolean isServerRunning()
 	{
 		debug("checking availability of server");
 		boolean serverIsRunning = createConnection();
 
 		// we're just checking, don't keep the connection
 		if (connected())
 		{
 			debug("server is running; disconnecting");
 			disconnect();
 		}
 
 		return serverIsRunning;
 	}
 
 	/**
 	 * Try and connect to the server. If we fail, wait CONNECTION_RETRY_SLEEP_INTERVAL and try again.
 	 * Repeat ad nauseum.
 	 */
 	public void waitForConnect()
 	{
 		System.out.println("Waiting for a server on port "); //+ portNumber);
 		while (!connect())
 		{
 			// try again soon
 			Generic.sleep(WAIT_BEWTEEN_RECONNECT_ATTEMPTS);
 		}
 	}
 
 	/**
 	 * Parses the header of an incoming set of characters (i.e. a message from a client to a server),
 	 * loading all of the HTTP-like headers into the given headerMap.
 	 * 
 	 * If headerMap is null, this method will throw a null pointer exception.
 	 * 
 	 * @param allIncomingChars
 	 *          - the characters read from an incoming stream.
 	 * @param headerMap
 	 *          - the map into which all of the parsed headers will be placed.
 	 * @return the length of the parsed header, or -1 if it was not yet found.
 	 */
 	protected int parseHeader(int startChar, StringBuilder allIncomingChars)
 	{
 		// indicates that we might be at the end of the header
 		boolean maybeEndSequence = false;
 		char currentChar;
 
 		synchronized (currentHeaderSequence)
 		{
 			StringTools.clear(currentHeaderSequence);
 			StringTools.clear(currentKeyHeaderSequence);
 
 			int length = allIncomingChars.length();
 
 			for (int i = 0; i < length; i++)
 			{
 				currentChar = allIncomingChars.charAt(i);
 
 				switch (currentChar)
 				{
 				case (':'):
 					/*
 					 * we have the end of a key; move the currentHeaderSequence into the
 					 * currentKeyHeaderSequence and clear it
 					 */
 					currentKeyHeaderSequence.append(currentHeaderSequence);
 
 					StringTools.clear(currentHeaderSequence);
 
 					break;
 				case ('\r'):
 					/*
 					 * we have the end of a line; if there's a CRLF, then we have the end of the value
 					 * sequence or the end of the header.
 					 */
 					if (allIncomingChars.charAt(i + 1) == '\n')
 					{
 						if (!maybeEndSequence)
 						{// load the key/value pair
 							headerMap.put(currentKeyHeaderSequence.toString().toLowerCase(),
 									currentHeaderSequence.toString());
 
 							StringTools.clear(currentKeyHeaderSequence);
 							StringTools.clear(currentHeaderSequence);
 
 							i++; // so we don't re-read that last character
 						}
 						else
 						{ // end of the header
 							return i + 2;
 						}
 
 						maybeEndSequence = true;
 					}
 					break;
 				default:
 					currentHeaderSequence.append(currentChar);
 					maybeEndSequence = false;
 					break;
 				}
 			}
 
 			// if we got here, we didn't finish the header
 			return -1;
 		}
 	}
 
 	/**
 	 * @see ecologylab.oodss.distributed.impl.NIONetworking#writeKey(java.nio.channels.SelectionKey)
 	 */
 	//@Override
 	//protected void writeKey(SelectionKey key) throws IOException
 	//{
 		// lock outgoing requests queue, send data from it, then switch out of
 		// write mode
 		//synchronized (requestsQueue)
 		//{
 		//	while (this.requestsRemaining() > 0)
 		//	{
 		//		this.createPacketFromMessageAndSend(this.dequeueRequest(), key);
 		//	}
 		//}
 	//}
 	///////////****...//////
 	//I'm trying to keep changes to the client down here... but they will undoubtably sprawl at some point
 	//BaseTokenClient client;
 	
 	BaseTokenClient webSocketClient;
 	
 	private void initializeWebSocketClient()
 	{
 		newBlockingResponsesQueue = new LinkedBlockingQueue<ResponseMessage>();//new 
 		webSocketClient = new BaseTokenClient();
 		webSocketClient.addListener(this);	
 		
 			try {
 				webSocketClient.open("ws://localhost:8787/;unid=admin_ui_1");
 				System.out.println("Client now connected...");
 			} catch (WebSocketException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 
 		/*	try {
 				Thread.sleep(2000);
 			} catch (InterruptedException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 			*/
 			/*
 			try {
 				
 				PingRequest p = new PingRequest();
 				
 				ByteArrayOutputStream outStream = new ByteArrayOutputStream();
 				try {
 					p.serialize(outStream, FORMAT.JSON);
 				} catch (SIMPLTranslationException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 				
 				String pJSON = new String(outStream.toByteArray());
 				System.out.println("pJSON:"+pJSON);
 				webSocketClient.sendText("5",pJSON);
 				//client.sendText("5", new String("{\"ns\":\"org.jWebSocket.plugins.system\",\"utid\":25,\"data\":\"LALAlaaaalaLAAL\",\"targetId\":\"*\",\"type\":\"send\",\"sourceId\":\"51262\"}"));
 			} catch (WebSocketException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 			*/
 	}
 	
 
 	public int getMaxMessageLengthChars()
 	{
 		return this.maxMessageLengthChars;
 	}
 
 	public void terminate()
 	{
 
 
 		nullOut();
 	}
 
 	public void setReconnectBlocker(ReconnectBlocker blocker)
 	{
 		this.blocker = blocker;
 	}
 
 	public class Reconnecter implements Runnable
 	{
 		public void run()
 		{
 			if (blocker != null)
 				blocker.reconnectBlock();
 			debug("Reconnecting in reconnector thread!");
 			reconnect();
 		}
 	}
 
 	public boolean allowsCompression()
 	{
 		return this.allowCompression;
 	}
 
 	public void allowCompression(boolean useCompression)
 	{
 		this.allowCompression = useCompression;
 	}
 
 	/**
 	 * Specifies whether or not to use request compression, most web servers don't allow receiving
 	 * compressed requests (i.e. Apache)
 	 * 
 	 * @param useRequestCompression
 	 *          whether or not to use compression when sending requests
 	 */
 	public void useRequestCompression(boolean useRequestCompression)
 	{
 		this.sendCompressed = useRequestCompression;
 	}
 
 	public boolean usesRequestCompression()
 	{
 		return this.sendCompressed;
 	}
 
 	public void addClientStatusListener(ClientStatusListener csl)
 	{
 		this.clientStatusListeners().add(csl);
 	}
 
 	public void removeClientStatusListener(ClientStatusListener csl)
 	{
 		this.clientStatusListeners().remove(csl);
 	}
 
 	private void notifyOfStatusChange(boolean newStatus)
 	{
 		if (this.clientStatusListeners != null)
 		{
 			for (ClientStatusListener csl : this.clientStatusListeners())
 			{
 				csl.clientConnectionStatusChanged(newStatus);
 			}
 		}
 	}
 
 	private List<ClientStatusListener> clientStatusListeners()
 	{
 		if (this.clientStatusListeners == null)
 		{
 			synchronized (this)
 			{
 				if (this.clientStatusListeners == null)
 				{
 					this.clientStatusListeners = new LinkedList<ClientStatusListener>();
 				}
 			}
 		}
 
 		return this.clientStatusListeners;
 	}
 	
 	
 	@Override
 	public void processOpened(WebSocketClientEvent aEvent) {
 		// TODO Auto-generated method stub
 		System.out.println("TestWebSocketClient:");
 		
 	}
 
 	
 	/*
 	 * returns true if argument is in the start of testAgainst
 	 */
 	private static boolean startsWith(String argument, String testAgainst)
 	{
		try{
	       String compareTo = testAgainst.substring(0, Math.max(0, argument.length()));
	       System.out.println("Compare the next two lines:");
	       System.out.println(argument);
	       System.out.println(compareTo);
	       return argument.equals(compareTo);
		}
		catch(Exception e)
		{
		   return false;
		}
 	}
 	
 	boolean processedFirstMessage = false;
 	@Override//can i add synchronized here?...
 	public void processPacket(WebSocketClientEvent aEvent,
 			WebSocketPacket aPacket) {
 		if(!processedFirstMessage)
 		{
 			System.out.println("Skipping the message:"+aPacket.getUTF8());
 			processedFirstMessage = true;
 			return;
 		}
 		String newConnectedStart = "{\"name\":\"connect\"";
 		String newDisconnected ="{\"name\":\"disconnect\"";
 		// TODO Auto-generated method stub
 		System.out.println("TestWebSocketClient:processPacket+"+aPacket.getUTF8());
 		String responseJSON = aPacket.getUTF8();
 		
 		//this is a hack until I can dig into the code of jwebsocket enough to turn messages off or use them in different events
 		if(startsWith(newConnectedStart, responseJSON) || startsWith(newDisconnected,responseJSON))
 			return;
 		//turn it from json to response object...  WIN
 		//put in newBlockingResponsesQueue
 		
 		if (!this.blockingRequestPending)
 		{
 			// we process the read data into a response message, let it
 			// perform its response, then dispose of
 			// the
 			// resulting MessageWithMetadata object
 			//this.responsePool.release(processString(firstMessageBuffer.toString(),
 				//	uidOfCurrentMessage));
 			processString(responseJSON);//not really using uid... hmmm...
 			blockingRequestPending = false;
 			blockingResponsesQueue.clear();
 			debug("I something should be released here... ");
 			//this.responsePool.release();
 					//tbd lookup responsePool
 		}
 		else
 		{
 			System.out.println("Making this response a thing..."+responseJSON);
 			
 			ArrayList<ClassDescriptor> b = translationScope.getAllClassDescriptors();
 			for(int i=0; i<b.size(); i++)
 			{
 				System.out.println(b.get(i).getName());
 			}
 			
 			ResponseMessage rma = (ResponseMessage)processString(responseJSON);
 			if(newBlockingResponsesQueue == null)
 			    System.out.println("Awe crud it's null");
 			//newBlockingResponsesQueue.add(rma);
 			blockingRequestPending = false;
 			blockingResponsesQueue.clear();
 			synchronized (this)
 			{
 				notify();
 			}
 		}
 		
 		//this.notify();
 		//TODO take care of update messages... also POOP.
 		
 		
 	}
 
 	@Override
 	public void processClosed(WebSocketClientEvent aEvent) {
 		// TODO Auto-generated method stub
 		System.out.println("TestWebSocketClient:processClosed");
 		
 	}
 
 	@Override
 	public void processToken(WebSocketClientEvent aEvent, Token aToken) {
 		// TODO Auto-generated method stub
 		System.out.println("TestWebSocketClient:processToken");
 		////aToken.
 	}
 
 	@Override
 	public void run() {
 		// TODO Auto-generated method stub
 		
 	}
 
 //	@Override
 //	protected void processReadData(Object sessionToken, SelectionKey sk,
 //			ByteBuffer bytes, int bytesRead) throws BadClientException {
 		// TODO Auto-generated method stub
 		
 //	}
 }
