 /**
  * 
  */
 package ecologylab.oodss.distributed.server;
 
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.io.OutputStream;
 import java.net.BindException;
 import java.net.InetAddress;
 import java.nio.ByteBuffer;
 import java.nio.CharBuffer;
 import java.nio.channels.SelectionKey;
 import java.nio.charset.CharacterCodingException;
 import java.nio.charset.Charset;
 import java.nio.charset.CharsetDecoder;
 import java.util.HashMap;
 import java.util.Iterator;
 
 import org.jwebsocket.api.WebSocketConnector;
 import org.jwebsocket.api.WebSocketServer;
 import org.jwebsocket.console.JWebSocketServerLight;
 import org.jwebsocket.console.JWebSocketListenerBridge;
 import org.jwebsocket.kit.RawPacket;
 
 import ecologylab.collections.Scope;
 import ecologylab.generic.CharBufferPool;
 import ecologylab.generic.HashMapArrayList;
 import ecologylab.generic.StringBuilderPool;
 import ecologylab.io.ByteBufferPool;
 import ecologylab.net.NetTools;
 import ecologylab.oodss.distributed.common.ServerConstants;
 import ecologylab.oodss.distributed.common.SessionObjects;
 import ecologylab.oodss.distributed.impl.AbstractAIOServer;
 
 import ecologylab.oodss.exceptions.BadClientException;
 import ecologylab.oodss.messages.RequestMessage;
 import ecologylab.oodss.messages.ResponseMessage;
 import ecologylab.oodss.messages.UpdateMessage;
 import ecologylab.oodss.server.clientsessionmanager.WebSocketSessionManager;
 //import ecologylab.oodss.server.clientsessionmanager.NewBaseSessionManager;
 import ecologylab.oodss.distributed.server.clientsessionmanager.SessionHandle;
 //import ecologylab.oodss.server.clientsessionmanager.NewBaseSessionManager;
 import ecologylab.serialization.SIMPLTranslationException;
 import ecologylab.serialization.TranslationScope;
 import ecologylab.serialization.ElementState.FORMAT;
 
 /**
  * A server that uses NIO and two threads (one for handling IO, the other for handling interfacing
  * with messages).
  * 
  * Automatically processes and responds to any client RequestMessages.
  * 
  * Subclasses should generally override the generateContextManager hook method, so that they can use
  * their own, specific ContextManager in place of the default.
  * 
  * @author Zachary O. Toups (zach@ecologylab.net)
  */
 public class OODSSWebSocketServer<S extends Scope> extends AbstractAIOServer<S> implements
 		ServerConstants, ServerMessages
 {		
 	
 	public WebSocketServer webSocketServer;
 	
 
 //need to replace blah
     public void blah(WebSocketServer webSocketServer2)
     {
     	webSocketServer = webSocketServer2;
     }
     
     public void printNumberOfConnectedClients()
     {
     	System.out.println("In this map, we have "+webSocketServer.getAllConnectors().size()+ " Connected Clients..xxxxx");
     }
     
     public void sendUpdateMessage(String clientId, UpdateMessage updateMessage)
     {
     	debug("Sending update message to client:"+clientId);
     	//WebSocketPacket packet = new ;
     	//todo, make serialized update message
     	//updateMessage.ser
     	//RawPacket rp = new RawPacket();
     	
     	OutputStream s;
 		//WebSocketPacket webSocketPacket = new
     	
     	
 		ByteArrayOutputStream outStream = new ByteArrayOutputStream();
 		try {
 			updateMessage.serialize(outStream, FORMAT.JSON);
 		} catch (SIMPLTranslationException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		
 		String pJSON = new String(outStream.toByteArray());
 		System.out.println("pJSON:"+pJSON);
         RawPacket updateMessagePacket = new RawPacket(outStream.toByteArray());	
     	
 	    //WebSocketConnector theConnector = webSocketServer.getConnector(clientId);
 	    for(String connectorKey : webSocketServer.getAllConnectors().keySet())
 	    {
 	    	
 	    	WebSocketConnector theConnector = webSocketServer.getConnector(connectorKey);
 	    	String sessionKey = theConnector.getSession().getSessionId();
 	    	if(sessionKey == clientId)
 	    	{
 	    		theConnector.sendPacket(updateMessagePacket);
 	    	}
 	    	
 	    }
 	   // webSocketServer.getAllConnectors();
 	    
 	    //
 	    //.sendPacket(updateMessagePacket);
 	    System.out.println("SendUpdateMessage was called...");
     }
 	
 	protected static InetAddress[] addressToAddresses(InetAddress address)
 	{
 		InetAddress[] addresses =
 		{ address };
 		return addresses;
 	}
 
 
 	Thread																													t												= null;
 
 	boolean																													running									= false;
 
 	/**
 	 * Map in which keys are sessionTokens, and values are associated ClientSessionManagers.
 	 */
 	private HashMapArrayList<Object, WebSocketSessionManager>	clientSessionManagerMap	= new HashMapArrayList<Object, WebSocketSessionManager>();
 
 	public HashMap<String, WebSocketSessionManager> GetAllSessions()
 	{
 		return sessionForSessionIdMap;
 	}
 	/**
 	 * Map in which keys are sessionTokens, and values are associated SessionHandles
 	 */
 	private HashMapArrayList<Object, SessionHandle>									clientSessionHandleMap	= new HashMapArrayList<Object, SessionHandle>();
 
 	private static final Charset																		ENCODED_CHARSET					= Charset
 																																															.forName(CHARACTER_ENCODING);
 
 	private static CharsetDecoder																		DECODER									= ENCODED_CHARSET
 																																															.newDecoder();
 
 	protected int																										maxMessageSize;
 
 	/**
 	 * CharBuffers for use with translating from bytes to chars; may need to support having many
 	 * messages come through at once.
 	 */
 	protected CharBufferPool																				charBufferPool;
 	
 	protected ByteBufferPool																				byteBufferPool;
 	
 	protected StringBuilderPool																				stringBuilderPool;
 
 	
 	private static OODSSWebSocketServer serverInstance = null;
 	
 	public static OODSSWebSocketServer getInstance()
 	{
 		if(serverInstance == null)
 		{
 			System.out.println("THE INSTANCE YOU CALLED WAS NULL");
 			return null;//throw new Exception("This is not instantiated yet.");
 		}
 		else
 		{
 		    return serverInstance;
 		}
 	}
 	/**
 	 * 
 	 */
 	public OODSSWebSocketServer(TranslationScope requestTranslationScope, S applicationObjectScope,
 			int idleConnectionTimeout, int maxMessageSize) throws IOException, BindException
 	{
 		super(0, InetAddress.getLocalHost(), requestTranslationScope, applicationObjectScope,
 				idleConnectionTimeout, maxMessageSize);
 
 		this.maxMessageSize = maxMessageSize + MAX_HTTP_HEADER_LENGTH;
 		this.translationScope = requestTranslationScope;//gbgbgb
 
 		applicationObjectScope.put(SessionObjects.SESSIONS_MAP, clientSessionHandleMap);
 		applicationObjectScope.put(SessionObjects.OODSS_WEBSOCKET_SERVER, this);
 		
 
 		instantiateBufferPools(this.maxMessageSize);
 		
 		sessionForSessionIdMap = new HashMap<String, WebSocketSessionManager>();
 		applicationObjectScope.put(SessionObjects.SESSIONS_MAP_BY_SESSION_ID, sessionForSessionIdMap);
 
 		serverInstance = this;
 	}
 
 	/**
 	 * @param maxMessageSize
 	 */
 	protected void instantiateBufferPools(int maxMessageSize)
 	{
 		instantiateBufferPools(4, 4, maxMessageSize);
 	}
 	
 	protected void instantiateBufferPools(int poolSize, int minimumCapicity, int maxMessageSize)
 	{
 		this.charBufferPool 		= new CharBufferPool(poolSize, minimumCapicity, maxMessageSize);
 		
 		this.byteBufferPool 		= new ByteBufferPool(poolSize, minimumCapicity, maxMessageSize);
 		
 		this.stringBuilderPool 	= new StringBuilderPool(poolSize, minimumCapicity, maxMessageSize);
 	}
 
 	
 	
 	////////
 	HashMap<String, WebSocketSessionManager> sessionForSessionIdMap;
 	@Override
 	public String getAPushFromWebSocket(String s, String sessionId)
 	{
 		//should read more information about where this is coming from and add it to a queue or something...
 		//most of the thread stuff is handled by the jWebSocket architechure
 		
 		System.out.println("Just got getAPushFromWebSocket:"+s);
 		applicationObjectScope.put(SessionObjects.SESSION_ID, sessionId);
 		
 		
 		//String sessionId = "2212121212122112";
 		WebSocketSessionManager theClientSessionManages = null;
 		
 		if(sessionForSessionIdMap.containsKey(sessionId))
 		{
 			theClientSessionManages = sessionForSessionIdMap.get(sessionId);
 		}
 		else
 		{
 			theClientSessionManages = new WebSocketSessionManager(sessionId,
 					this.translationScope, this.applicationObjectScope );
 			sessionForSessionIdMap.put(sessionId, theClientSessionManages);
 		}
 		
 		System.out.println(0);
 		CharSequence cs = s;
 		RequestMessage request = null;
 		try {
 			request = theClientSessionManages.translateOODSSRequestJSON(s);
 			System.out.println(1);
 		} catch (SIMPLTranslationException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}//processStringJSON(incomingMessage, incomingUid)
 		System.out.println(2);
 		//Object cm = generateContextManager((String) sessionToken, sk, translationScope,
 		//		applicationObjectScope);
 		//System.out.println("Got RequestMessage type:"+request.getClassName().toString());
 		ResponseMessage response = theClientSessionManages.processRequest(request);
 		//System.out.println("Got ResponseMessage type:"+response.getClassName().toString());
 		
 		System.out.println(3);
 		
 		ByteArrayOutputStream outStream = new ByteArrayOutputStream();
 		try {
 			response.serialize(outStream, FORMAT.JSON);
 		} catch (SIMPLTranslationException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		System.out.println(4);
 		String pJSON = new String(outStream.toByteArray());
 		return pJSON;
 	
 		
 	}
 	
 	////////
 	
 	public void processRead(Object sessionToken, /*AIOServerIOThread base,*/ SelectionKey sk,
 			ByteBuffer bs, int bytesRead) throws BadClientException
 	{
 		if (bytesRead > 0)
 		{
 			synchronized (clientSessionManagerMap)
 			{
 				WebSocketSessionManager cm = (WebSocketSessionManager) clientSessionManagerMap.get(sessionToken);
 
 				if (cm == null)
 				{
 					debug("server creating context manager for " + sessionToken);
 
 					cm = generateContextManager((String) sessionToken, sk, translationScope,
 							applicationObjectScope);
 					clientSessionManagerMap.put(sessionToken, cm);
 			//		clientSessionHandleMap.put(sessionToken, cm.getHandle());
 				}
 /*
 				try
 				{
 					CharBuffer buf = this.charBufferPool.acquire();
 
 					DECODER.decode(bs, buf, true);
 					buf.flip();
 					//cm.processIncomingSequenceBufToQueue(buf);
 
 					buf = this.charBufferPool.release(buf);
 				}
 				catch (CharacterCodingException e)
 				{
 					e.printStackTrace();
 				}
 				*/
 			}
 
 			synchronized (this)
 			{
 				this.notify();
 			}
 		}
 	}
 
 	/**
 	 * Hook method to allow changing the ContextManager to enable specific extra functionality.
 	 * 
 	 * @param token
 	 * @param sc
 	 * @param translationScopeIn
 	 * @param registryIn
 	 * @return
 	 */
 	@Override
 	protected WebSocketSessionManager generateContextManager(String sessionId, SelectionKey sk,
 			TranslationScope translationScopeIn, Scope registryIn)
 	{
 		return new WebSocketSessionManager(sessionId,translationScopeIn,registryIn);
 				
 				//sessionId, maxMessageSize/*, this.getBackend(), this*/, sk,translationScopeIn, registryIn);
 	}
 
 	public void run()
 	{
 		Iterator<WebSocketSessionManager> contextIter;
 
 		while (running)
 		{
 			synchronized (clientSessionManagerMap)
 			{
 				contextIter = clientSessionManagerMap.values().iterator();
 
 				// process all of the messages in the queues
 				while (contextIter.hasNext())
 				{
 					WebSocketSessionManager cm = contextIter.next();
                     System.out.println("!!!!!!!!!!!!! should have done the ....  cm.processAllMessagesAndSendResponses();");
 					//try
 					//{
 						//cm.processAllMessagesAndSendResponses();
 					//}
 					//catch (BadClientException e)
 					//{
 						// Handle BadClientException! -- remove it
 						//error(e.getMessage());
 
 						// invalidate the manager's key
 						//this.getBackend().setPendingInvalidate(cm.getSocketKey(), true);
 
 						// remove the manager from the collection
 						//contextIter.remove();
 					//}
 				}
 			}
 
 			// sleep until notified of new messages
 			synchronized (this)
 			{
 				try
 				{
 					wait();
 				}
 				catch (InterruptedException e)
 				{
 					e.printStackTrace();
 					Thread.interrupted();
 				}
 			}
 		}
 	}
 
 	/**
 	 * @see ecologylab.generic.StartAndStoppable#start()
 	 */
 	@Override
 	public void start()
 	{
 		running = true;
 
 		if (t == null)
 		{
 			t = new Thread(this);
 		}
 
 		t.start();
 
 		super.start();
 		JWebSocketServerLight.startWebsocketServer(this);
 	}
 
 	/**
 	 * @see ecologylab.generic.StartAndStoppable#stop()
 	 */
 	@Override
 	public void stop()
 	{
 		debug("Server stopping.");
 		running = false;
 		synchronized (this)
 		{
 			this.notify();
 			synchronized (t)
 			{
 				t = null;
 			}
 		}
 		super.stop();
 	}
 
 	/**
 	 * @see ecologylab.oodss.distributed.impl.Shutdownable#shutdown()
 	 */
 	public void shutdown()
 	{
 		// TODO Auto-generated method stub
 
 	}
 
 	/**
 	 * @see ecologylab.oodss.distributed.server.AIOServerProcessor#invalidate(java.lang.Object,
 	 *      ecologylab.oodss.distributed.impl.AIOServerIOThread, java.nio.channels.SocketChannel)
 	 */
 	public boolean invalidate(String sessionId, boolean forcePermanent)
 	{
 		WebSocketSessionManager cm = clientSessionManagerMap.get(sessionId);
 
 		// figure out if the disconnect is permanent; will be permanent if forcing
 		// (usually bad client), if there is no context manager (client never sent
 		// data), or if the client manager says it is invalidating (client
 		// disconnected properly)
 		boolean permanent = (forcePermanent ? true : (cm == null ? true : cm.isInvalidating()));
 
 		// get the context manager...
 		if (permanent)
 		{
 			synchronized (clientSessionManagerMap)
 			{ // ...if this session will not be restored, remove the context
 				// manager
 				clientSessionManagerMap.remove(sessionId);
 				clientSessionHandleMap.remove(sessionId);
 			}
 		}
 
 		if (cm != null)
 		{
 			/*
 			 * if we've gotten here, then the client has disconnected already, no reason to deal w/ the
 			 * remaining messages // finish what the context manager was working on while
 			 * (cm.isMessageWaiting()) { try { cm.processAllMessagesAndSendResponses(); } catch
 			 * (BadClientException e) { e.printStackTrace(); } }
 			 */
 			cm.shutdown();
 		}
 
 		return permanent;
 	}
 
 
 
 	/**
 	 * 
 	 * @return status of server in boolean
 	 */
 	public boolean isRunning()
 	{
 		return running;
 	}
 
 	@Override
 	protected void shutdownImpl()
 	{
 		// TODO Auto-generated method stub
 
 	}
 
 	/**
 	 * Utility method for dynamically name TranslationScopes.
 	 * 
 	 * @param inetAddresses
 	 * @param portNumber
 	 * @return
 	 */
 	protected static String connectionTscopeName(InetAddress[] inetAddresses, int portNumber)
 	{
 		
 		return "double_threaded_logging " + inetAddresses[0].toString() + ":" + portNumber;
 	}
 
 
 
 	@Override
 	public void putServerObject(Object o) {
		// TODO Auto-generated method stub
 		webSocketServer = (WebSocketServer) o;
 	}
 }
