 /*
  * Created on May 12, 2006
  */
 package ecologylab.oodss.distributed.client;
 
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
 
 import ecologylab.collections.Scope;
 import ecologylab.generic.Generic;
 import ecologylab.generic.StringBuilderPool;
 import ecologylab.generic.StringTools;
 import ecologylab.oodss.distributed.common.ClientConstants;
 import ecologylab.oodss.distributed.common.ServerConstants;
 import ecologylab.oodss.distributed.exception.MessageTooLargeException;
 import ecologylab.oodss.distributed.impl.MessageWithMetadata;
 import ecologylab.oodss.distributed.impl.MessageWithMetadataPool;
 import ecologylab.oodss.distributed.impl.NIONetworking;
 import ecologylab.oodss.distributed.impl.PreppedRequest;
 import ecologylab.oodss.distributed.impl.PreppedRequestPool;
 import ecologylab.oodss.exceptions.BadClientException;
 import ecologylab.oodss.messages.DisconnectRequest;
 import ecologylab.oodss.messages.InitConnectionRequest;
 import ecologylab.oodss.messages.InitConnectionResponse;
 import ecologylab.oodss.messages.RequestMessage;
 import ecologylab.oodss.messages.ResponseMessage;
 import ecologylab.oodss.messages.SendableRequest;
 import ecologylab.oodss.messages.ServiceMessage;
 import ecologylab.oodss.messages.UpdateMessage;
 import ecologylab.serialization.SIMPLTranslationException;
 import ecologylab.serialization.SimplTypesScope;
 import ecologylab.serialization.formatenums.StringFormat;
 
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
 public class NIOClient<S extends Scope> extends NIONetworking<S> implements Runnable,
 		ClientConstants
 {
 	protected String																									serverAddress;
 
 	protected final CharBuffer																				outgoingChars;
 
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
 
 	public NIOClient(String serverAddress, int portNumber, SimplTypesScope messageSpace,
 			S objectRegistry, int maxMessageLengthChars) throws IOException
 	{
 		super("NIOClient", portNumber, messageSpace, objectRegistry, maxMessageLengthChars);
 
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
 	}
 
 	public NIOClient(String serverAddress, int portNumber, SimplTypesScope messageSpace,
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
 		debug("initializing connection...");
 		if (this.connectImpl())
 		{
 			debug("starting listener thread...");
 
 			this.start();
 
 			// now send first handshake message
 			ResponseMessage initResponse = null;
 			try
 			{
 				initResponse = this.sendMessage(new InitConnectionRequest(this.sessionId));
 			}
 			catch (MessageTooLargeException e)
 			{
 				// this shouldn't be able to happen
 				e.printStackTrace();
 			}
 
 			if (initResponse instanceof InitConnectionResponse)
 			{
 				if (this.sessionId == null)
 				{
 					this.sessionId = ((InitConnectionResponse) initResponse).getSessionId();
 
 					debug("new session: " + this.sessionId);
 				}
 				else if (this.sessionId == ((InitConnectionResponse) initResponse).getSessionId())
 				{
 					debug("reconnected and restored previous connection: " + this.sessionId);
 				}
 				else
 				{
 					String newId = ((InitConnectionResponse) initResponse).getSessionId();
 					debug("unable to restore previous session, " + this.sessionId + "; new session: " + newId);
 					this.unableToRestorePreviousConnection(this.sessionId, newId);
 					this.sessionId = newId;
 				}
 
 				this.thisSocket.keyFor(this.selector).attach(this.sessionId);
 			}
 		}
 
 		this.notifyOfStatusChange(this.connected());
 
 		debug("connected? " + this.connected());
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
			SimplTypesScope.serialize(request, requestBuffer, StringFormat.XML);			
 
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
 
 		this.queueForWrite(this.thisSocket.keyFor(selector));
 
 		selector.wakeup();
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
 			stop();
 		}
 	}
 
 	/**
 	 * Hook method for subclasses to provide specific disconnect messages. For example, authenticating
 	 * clients will want to log out.
 	 */
 	protected void handleDisconnectingMessages()
 	{
 		debug("************** sending disconnect request");
 		try
 		{
 			this.sendMessage(DisconnectRequest.REUSABLE_INSTANCE, 10000);
 		}
 		catch (MessageTooLargeException e)
 		{
 			// this shouldn't be able to happen, unless the maximum request size
 			// gets set below the size of a DisconnectRequest
 			e.printStackTrace();
 		}
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
 		return (thisSocket != null) && !thisSocket.isConnectionPending() && thisSocket.isConnected();
 	}
 
 	/**
 	 * Side effect of calling start().
 	 */
 	protected boolean createConnection()
 	{
 		try
 		{
 			// create the channel and connect it to the server
 			debug("creating socket!");
 			thisSocket = SocketChannel.open(new InetSocketAddress(serverAddress, portNumber));
 
 			// disable blocking
 			thisSocket.configureBlocking(false);
 
 			if (connected())
 			{
 				// register the channel for read operations, now that it is
 				// connected
 				super.openSelector();
 				thisSocket.register(selector, SelectionKey.OP_READ);
 			}
 		}
 		catch (BindException e)
 		{
 			debug("Couldnt create socket connection to server - " + serverAddress + ":" + portNumber
 					+ " - " + e);
 
 			nullOut();
 		}
 		catch (PortUnreachableException e)
 		{
 			debug("Server is alive, but has no daemon on portNumber " + portNumber + ": " + e);
 
 			nullOut();
 		}
 		catch (SocketException e)
 		{
 			debug("Server '" + serverAddress + "' unreachable: " + e);
 
 			nullOut();
 		}
 		catch (IOException e)
 		{
 			debug("Bad response from server: " + e);
 
 			nullOut();
 		}
 
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
 			throws MessageTooLargeException
 	{
 		MessageWithMetadata<ServiceMessage, Object> responseMessage = null;
 
 		// notify the connection thread that we are waiting on a response
 		blockingRequestPending = true;
 
 		long currentMessageUid;
 
 		boolean blockingRequestFailed = false;
 		long startTime = System.currentTimeMillis();
 		int timeCounter = 0;
 
 		try
 		{
 			currentMessageUid = this.prepareAndEnqueueRequestForSending(request).getUid();
 		}
 		catch (SIMPLTranslationException e1)
 		{
 			error("error translating to XML; returning null");
 			e1.printStackTrace();
 
 			return null;
 		}
 		catch (MessageTooLargeException e)
 		{
 			blockingRequestPending = false;
 			error("message too large to send");
 			e.printStackTrace();
 			throw e;
 		}
 
 		// wait to be notified that the response has arrived
 		while (blockingRequestPending && !blockingRequestFailed)
 		{
 			if (timeOutMillis <= -1)
 			{
 				debug("waiting on blocking request");
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
 
 			debug("waking");
 
 			timeCounter += System.currentTimeMillis() - startTime;
 			startTime = System.currentTimeMillis();
 
 			while ((blockingRequestPending) && (!blockingResponsesQueue.isEmpty()))
 			{
 				responseMessage = blockingResponsesQueue.poll();
 
 				if (responseMessage.getUid() == currentMessageUid)
 				{
 					debug("got the right response: " + currentMessageUid);
 
 					blockingRequestPending = false;
 
 					blockingResponsesQueue.clear();
 
 					ResponseMessage respMsg = (ResponseMessage) responseMessage.getMessage();
 
 					// try
 					// {
 					// debug("response: " + respMsg.serialize().toString());
 					// }
 					// catch (SIMPLTranslationException e)
 					// {
 					// e.printStackTrace();
 					// }
 
 					responseMessage = responsePool.release(responseMessage);
 
 					return respMsg;
 				}
 
 				responseMessage = responsePool.release(responseMessage);
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
 
 	@Override
 	public void start()
 	{
 		if (connected())
 		{
 			super.start();
 		}
 	}
 
 	@Override
 	public void stop()
 	{
 		debug("shutting down client listening thread.");
 
 		super.stop();
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
 			this.stop();
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
 
 				synchronized (encoder)
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
 			}
 			if (this.sendCompressed)
 			{
 				thisSocket.write(compressedMessageBuffer);
 			}
 		}
 		catch (ClosedChannelException e)
 		{
 			debug("connection severed; disconnecting and storing requests...");
 			setPendingInvalidate(incomingKey, false);
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
 
 			encoder.reset();
 			encoder.encode(zippingChars, zippingInBytes, true);
 			encoder.flush(zippingInBytes);
 
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
 				processUpdate((UpdateMessage) response.getMessage());
 			}
 		}
 
 		return response;
 	}
 
 	public void disconnect()
 	{
 		disconnect(true);
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
 	@Override
 	protected void acceptKey(SelectionKey key)
 	{
 	}
 
 	/**
 	 * @see ecologylab.oodss.distributed.impl.NIONetworking#checkAndDropIdleKeys()
 	 */
 	@Override
 	protected void checkAndDropIdleKeys()
 	{
 
 	}
 
 	/**
 	 * @see ecologylab.oodss.distributed.impl.NIONetworking#invalidateKey(java.nio.channels.SelectionKey,
 	 *      boolean)
 	 */
 	@Override
 	protected void invalidateKey(SelectionKey key, boolean permanent)
 	{
 		debug("server disconnected...");
 
 		// clean up
 		this.invalidateKey((SocketChannel) key.channel());
 
 		if (!permanent)
 		{
 			Thread thread = new Thread(new Reconnecter());
 			thread.run();
 		}
 
 		this.notifyOfStatusChange(false);
 	}
 
 	/**
 	 * @see ecologylab.oodss.distributed.impl.NIONetworking#processReadData(java.lang.Object,
 	 *      java.nio.channels.SocketChannel, byte[], int)
 	 */
 	@Override
 	protected void processReadData(Object sessionToken, SelectionKey sk, ByteBuffer bytes,
 			int bytesRead) throws BadClientException
 	{
 		synchronized (incomingMessageBuffer)
 		{
 			try
 			{
 				incomingMessageBuffer.append(decoder.decode(bytes));
 
 				// look for HTTP header
 				while (incomingMessageBuffer.length() > 0)
 				{
 					if (endOfFirstHeader == -1)
 					{
 						endOfFirstHeader = this.parseHeader(startReadIndex, incomingMessageBuffer);
 					}
 
 					if (endOfFirstHeader == -1)
 					{ /*
 						 * no header yet; if it's too large, bad client; if it's not too large yet, just exit,
 						 * it'll get checked again when more data comes down the pipe
 						 */
 						if (incomingMessageBuffer.length() > ServerConstants.MAX_HTTP_HEADER_LENGTH)
 						{
 							// clear the buffer
 							BadClientException e = new BadClientException(((SocketChannel) sk.channel()).socket()
 									.getInetAddress().getHostAddress(), "Maximum HTTP header length exceeded. Read "
 									+ incomingMessageBuffer.length() + "/" + MAX_HTTP_HEADER_LENGTH);
 
 							incomingMessageBuffer.setLength(0);
 
 							throw e;
 						}
 
 						// next time around, start reading from where we left off this
 						// time
 						startReadIndex = incomingMessageBuffer.length();
 
 						break;
 					}
 					else
 					{ // we've read all of the header, and have it loaded into the
 						// map; now we can use it
 						if (contentLengthRemaining == -1)
 						{
 							try
 							{
 								// handle all header information here; delete it when
 								// done here
 								contentLengthRemaining = Integer
 										.parseInt(this.headerMap.get(CONTENT_LENGTH_STRING));
 								if (headerMap.containsKey(UNIQUE_IDENTIFIER_STRING))
 								{
 									uidOfCurrentMessage = Integer.parseInt(this.headerMap
 											.get(UNIQUE_IDENTIFIER_STRING));
 								}
 								contentEncoding = this.headerMap.get(HTTP_CONTENT_CODING);
 
 								// done with the header; delete it
 								incomingMessageBuffer.delete(0, endOfFirstHeader);
 								this.headerMap.clear();
 							}
 							catch (NumberFormatException e)
 							{
 								e.printStackTrace();
 								contentLengthRemaining = -1;
 							}
 							// next time we read the header (the next message), we need
 							// to start from the beginning
 							startReadIndex = 0;
 						}
 					}
 
 					/*
 					 * we have the end of the first header (otherwise we would have broken out earlier). If we
 					 * don't have the content length, something bad happened, because it should have been
 					 * read.
 					 */
 					if (contentLengthRemaining == -1)
 					{
 						/*
 						 * if we still don't have the remaining length, then there was a problem
 						 */
 						break;
 					}
 					else if (contentLengthRemaining > this.maxMessageLengthChars)
 					{
 						throw new BadClientException(((SocketChannel) sk.channel()).socket().getInetAddress()
 								.getHostAddress(), "Specified content length too large: " + contentLengthRemaining);
 					}
 
 					try
 					{
 						// see if the incoming buffer has enough characters to
 						// include the specified content length
 						if (incomingMessageBuffer.length() >= contentLengthRemaining)
 						{
 							firstMessageBuffer.append(incomingMessageBuffer.substring(0, contentLengthRemaining));
 
 							incomingMessageBuffer.delete(0, contentLengthRemaining);
 
 							// reset to do a new read on the next invocation
 							contentLengthRemaining = -1;
 							endOfFirstHeader = -1;
 						}
 						else
 						{
 							firstMessageBuffer.append(incomingMessageBuffer);
 
 							// indicate that we need to get more from the buffer in
 							// the next invocation
 							contentLengthRemaining -= incomingMessageBuffer.length();
 
 							incomingMessageBuffer.setLength(0);
 						}
 					}
 					catch (NullPointerException e)
 					{
 						e.printStackTrace();
 					}
 
 					if ((firstMessageBuffer.length() > 0) && (contentLengthRemaining == -1))
 					{ /*
 						 * if we've read a complete message, then contentLengthRemaining will be reset to -1
 						 */
 						// we got a response
 
 						if (contentEncoding != null)
 						{
 							if (contentEncoding.equals(HTTP_DEFLATE_ENCODING))
 							{
 								unCompress(firstMessageBuffer);
 							}
 							else
 							{
 								throw new BadClientException(((SocketChannel) sk.channel()).socket()
 										.getInetAddress().getHostAddress(), "Specified content encoding: "
 										+ contentEncoding + "not supported!");
 							}
 						}
 						if (!this.blockingRequestPending)
 						{
 							// we process the read data into a response message, let it
 							// perform its response, then dispose of
 							// the
 							// resulting MessageWithMetadata object
 							this.responsePool.release(processString(firstMessageBuffer.toString(),
 									uidOfCurrentMessage));
 						}
 						else
 						{
 							blockingResponsesQueue.add(processString(firstMessageBuffer.toString(),
 									uidOfCurrentMessage));
 							synchronized (this)
 							{
 								notify();
 							}
 						}
 
 						firstMessageBuffer.setLength(0);
 					}
 				}
 			}
 			catch (CharacterCodingException e1)
 			{
 				e1.printStackTrace();
 			}
 			catch (DataFormatException e)
 			{
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 
 		}
 	}
 
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
 
 			encoder.reset();
 			encoder.encode(zippingChars, zippingInBytes, true);
 			encoder.flush(zippingInBytes);
 
 			zippingInBytes.flip();
 
 			inflater.reset();
 			inflater.setInput(zippingInBytes.array(), zippingInBytes.position(), zippingInBytes.limit());
 
 			zippingOutBytes.clear();
 			inflater
 					.inflate(zippingOutBytes.array(), zippingOutBytes.position(), zippingOutBytes.limit());
 
 			zippingOutBytes.position(0);
 			zippingOutBytes.limit(inflater.getTotalOut());
 
 			zippingChars.clear();
 
 			decoder.reset();
 			decoder.decode(zippingOutBytes, zippingChars, true);
 			decoder.flush(zippingChars);
 
 			zippingChars.flip();
 
 			firstMessageBuffer.setLength(0);
 
 			return firstMessageBuffer.append(zippingChars.array(), 0, zippingChars.limit());
 		}
 	}
 
 	/**
 	 * @see ecologylab.oodss.distributed.impl.NIONetworking#removeBadConnections(java.nio.channels.SelectionKey)
 	 */
 	@Override
 	protected void removeBadConnections(SelectionKey key)
 	{
 		// TODO Auto-generated method stub
 
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
 	protected MessageWithMetadata<ServiceMessage, Object> translateXMLStringToServiceMessage(
 			String messageString, int incomingUid) throws SIMPLTranslationException
 	{
 		ServiceMessage resp = (ServiceMessage) this.translationScope
 				.deserialize(messageString, StringFormat.XML);
 
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
 		System.out.println("Waiting for a server on port " + portNumber);
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
 	@Override
 	protected void writeKey(SelectionKey key) throws IOException
 	{
 		// lock outgoing requests queue, send data from it, then switch out of
 		// write mode
 		synchronized (requestsQueue)
 		{
 			while (this.requestsRemaining() > 0)
 			{
 				this.createPacketFromMessageAndSend(this.dequeueRequest(), key);
 			}
 		}
 	}
 
 	/**
 	 * @see ecologylab.oodss.distributed.impl.NIOCore#acceptReady(java.nio.channels.SelectionKey)
 	 */
 	@Override
 	protected void acceptReady(SelectionKey key)
 	{
 	}
 
 	/**
 	 * @see ecologylab.oodss.distributed.impl.NIOCore#connectReady(java.nio.channels.SelectionKey)
 	 */
 	@Override
 	protected void connectReady(SelectionKey key)
 	{
 	}
 
 	/**
 	 * @see ecologylab.oodss.distributed.impl.NIOCore#readFinished(java.nio.channels.SelectionKey)
 	 */
 	@Override
 	protected void readFinished(SelectionKey key)
 	{
 	}
 
 	/**
 	 * @see ecologylab.oodss.distributed.impl.NIOCore#acceptFinished(java.nio.channels.SelectionKey)
 	 */
 	@Override
 	public void acceptFinished(SelectionKey key)
 	{
 	}
 
 	@Override
 	protected boolean handleInvalidate(SelectionKey key, boolean forcePermanent)
 	{
 		return false;
 	}
 
 	public int getMaxMessageLengthChars()
 	{
 		return this.maxMessageLengthChars;
 	}
 
 	public void terminate()
 	{
 		for (SelectionKey key : this.selector.keys())
 		{
 			this.setPendingInvalidate(key, false);
 		}
 
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
 }
