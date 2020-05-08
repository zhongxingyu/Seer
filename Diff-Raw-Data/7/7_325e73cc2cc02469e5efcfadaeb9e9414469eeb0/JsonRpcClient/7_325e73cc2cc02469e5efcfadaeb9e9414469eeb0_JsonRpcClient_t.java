 package better.jsonrpc.client;
 
 import java.io.IOException;
 import java.lang.reflect.Type;
 import java.util.Collection;
 import java.util.Hashtable;
 import java.util.Map;
 import java.util.Random;
 import java.util.concurrent.locks.Condition;
 import java.util.concurrent.locks.Lock;
 import java.util.concurrent.locks.ReentrantLock;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import better.jsonrpc.core.JsonRpcConnection;
 import better.jsonrpc.exceptions.DefaultExceptionResolver;
 import better.jsonrpc.exceptions.ExceptionResolver;
 
 import com.fasterxml.jackson.core.JsonParser;
 import com.fasterxml.jackson.databind.JavaType;
 import com.fasterxml.jackson.databind.JsonNode;
 import com.fasterxml.jackson.databind.ObjectMapper;
 import com.fasterxml.jackson.databind.node.ObjectNode;
 import com.fasterxml.jackson.databind.type.TypeFactory;
 
 /**
  * A JSON-RPC client.
  */
 public class JsonRpcClient {
 
 	private static final Logger LOGGER = Logger.getLogger(JsonRpcClient.class.getName());
 
 	private static final String JSON_RPC_VERSION = "2.0";
 
 	private ObjectMapper mapper;
 	private Random random;
 	private ExceptionResolver exceptionResolver = DefaultExceptionResolver.INSTANCE;
 
 	/**
 	 * Creates a client that uses the given {@link ObjectMapper} to
 	 * map to and from JSON and Java objects.
 	 * @param mapper the {@link ObjectMapper}
 	 */
 	public JsonRpcClient(ObjectMapper mapper) {
 		this.mapper = mapper;
 		this.random = new Random(System.currentTimeMillis());
 	}
 
 	/**
 	 * Creates a client that uses the default {@link ObjectMapper}
 	 * to map to and from JSON and Java objects.
 	 */
 	public JsonRpcClient() {
 		this(new ObjectMapper());
 	}
 	
 	private String getNextId() {
 		return random.nextLong()+"";
 	}
 	
 	public Object invokeMethod(String methodName, Object arguments, Type returnType, JsonRpcConnection connection)
 		throws Throwable {
 		String id = getNextId();
 		ObjectNode request = createRequest(methodName, arguments, id);
 		Request req = new Request(id, request, connection);
 		mOutstandingById.put(id, req);
 		connection.sendRequest(request);
 		return req.waitForResponse(returnType);
 	}
 	
 	public void invokeNotification(String methodName, Object arguments, JsonRpcConnection connection) {
 		ObjectNode request = createRequest(methodName, arguments, null);
 		connection.sendRequest(request);
 	}
 	
 	public void handleResponse(ObjectNode response, JsonRpcConnection connection) {
 		JsonNode idNode = response.get("id");
 		if(idNode != null && idNode.isTextual()) {
 			String id = idNode.asText();
 			Request req = mOutstandingById.get(id);
 			if(req != null && req.getConnection() == connection) {
 				req.handleResponse(response);
 			}
 		}
 	}
 	
 	private Hashtable<String, Request> mOutstandingById =
 			new Hashtable<String, Request>();
 	
 	private class Request {
 		Lock mLock;
 		Condition mCondition;
 		String mId;
 		boolean mFailed;
 		ObjectNode mRequest;
 		ObjectNode mResponse;
 		JsonRpcConnection mConnection;
 		public Request(String id, ObjectNode request, JsonRpcConnection connection) {
 			mLock = new ReentrantLock();
 			mCondition = mLock.newCondition();
 			mId = id;
 			mRequest = request;
 			mResponse = null;
 			mConnection = connection;
 		}
 		public JsonRpcConnection getConnection() {
 			return mConnection;
 		}
 		public void handleError() {
 			mLock.lock();
 			try {
 				mFailed = true;
 				mCondition.signalAll();
 			} finally {
 				mLock.unlock();
 			}
 		}
 		public void handleResponse(ObjectNode response) {
 			mLock.lock();
 			try {
 				if(mResponse == null && !mFailed) {
 					mResponse = response;
 					mCondition.signalAll();
 				}
 			} finally {
 				mLock.unlock();
 			}
 		}
 		public Object waitForResponse(Type returnType) throws Throwable {
 			mLock.lock();
 			try {
 				// wait for response
 				while(mResponse == null && !mFailed) {
 					try {
 						mCondition.await();
 					} catch (InterruptedException e) {
 					}
 				}
 				
 				// detect rpc failures
 				if (mFailed) {
 					throw new RuntimeException("RPC failed");
 				}
 				
 				// detect errors
 				if (mResponse.has("error")
 					&& mResponse.get("error")!=null
 					&& !mResponse.get("error").isNull()) {
 
 					// resolve and throw the exception
 					if (exceptionResolver==null) {
 						throw DefaultExceptionResolver.INSTANCE.resolveException(mResponse);
 					} else {
 						throw exceptionResolver.resolveException(mResponse);
 					}
 				}
 				
 				// convert it to a return object
 				if (mResponse.has("result")
 					&& !mResponse.get("result").isNull()
 					&& mResponse.get("result")!=null) {
 					if (returnType==null) {
 						LOGGER.warning(
 							"Server returned result but returnType is null");
 						return null;
 					}
 					
 					JsonParser returnJsonParser = mapper.treeAsTokens(mResponse.get("result"));
 					JavaType returnJavaType = TypeFactory.defaultInstance().constructType(returnType);
 					
 					return mapper.readValue(returnJsonParser, returnJavaType);
 				}
 				
 				return null;
 			} finally {
 				mLock.unlock();
 			}
 		}
 	}
 
 	/**
 	 * Creates a JSON request node.
 	 * @param methodName the method name
 	 * @param arguments the arguments
 	 * @param ops the stream
 	 * @param id the optional id
 	 * @throws IOException on error
 	 */
 	private ObjectNode createRequest(
 		String methodName, Object arguments, String id) {
 		
 		// create the request
 		ObjectNode request = mapper.createObjectNode();
 		
 		// add id
 		if (id!=null) { request.put("id", id); }
 		
 		// add protocol and method
 		request.put("jsonrpc", JSON_RPC_VERSION);
 		request.put("method", methodName);

        // default empty arguments, will be replaced further down
        request.put("params", mapper.valueToTree(new Object[0]));

        // object array args
 		if (arguments!=null && arguments.getClass().isArray()) {
 			Object[] args = Object[].class.cast(arguments);
 			if (args.length>0) {
 				request.put("params", mapper.valueToTree(Object[].class.cast(arguments)));
 			}
 		
 		// collection args
 		} else if (arguments!=null && Collection.class.isInstance(arguments)) {
 			if (!Collection.class.cast(arguments).isEmpty()) {
 				request.put("params", mapper.valueToTree(arguments));
 			}
 			
 		// map args
 		} else if (arguments!=null && Map.class.isInstance(arguments)) {
 			if (!Map.class.cast(arguments).isEmpty()) {
 				request.put("params", mapper.valueToTree(arguments));
 			}
 
 		// other args
 		} else if (arguments!=null) {
 			request.put("params", mapper.valueToTree(arguments));
 		}
 
 		// log
 		if (LOGGER.isLoggable(Level.FINE)) {
 			LOGGER.log(Level.FINE, "JSON-PRC Request: "+request.toString());
 		}
 
 		// return the request
 		return request;
 	}
 	
 	/**
 	 * Returns the {@link ObjectMapper} that the client
 	 * is using for JSON marshalling.
 	 * @return the {@link ObjectMapper}
 	 */
 	public ObjectMapper getObjectMapper() {
 		return mapper;
 	}
 
 	/**
 	 * @param exceptionResolver the exceptionResolver to set
 	 */
 	public void setExceptionResolver(ExceptionResolver exceptionResolver) {
 		this.exceptionResolver = exceptionResolver;
 	}
 
 }
