 package com.googlecode.jsonrpc4j;
 
 import java.io.ByteArrayInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.lang.annotation.Annotation;
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 import java.lang.reflect.Type;
 import java.net.URLDecoder;
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Set;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.codehaus.jackson.JsonGenerationException;
 import org.codehaus.jackson.JsonNode;
 import org.codehaus.jackson.JsonParseException;
 import org.codehaus.jackson.map.JsonMappingException;
 import org.codehaus.jackson.map.ObjectMapper;
 import org.codehaus.jackson.map.type.TypeFactory;
 import org.codehaus.jackson.node.ArrayNode;
 import org.codehaus.jackson.node.ObjectNode;
 
 /**
  * A JSON-RPC request server reads JSON-RPC requests from an 
  * input stream and writes responses to an output stream.
  */
 public class JsonRpcServer {
 
 	private static final Logger LOGGER = Logger.getLogger(JsonRpcServer.class.getName());
 
     public static final String JSONRPC_RESPONSE_CONTENT_TYPE = "application/json-rpc";
 
     private boolean rethrowExceptions = false;
 	private ObjectMapper mapper;
 	private Object handler;
 	private Class<?> remoteInterface;
 
 	/**
 	 * Creates the server with the given {@link ObjectMapper} delegating
 	 * all calls to the given {@code handler} {@link Object} but only
 	 * methods available on the {@code remoteInterface}.
 	 * @param mapper the {@link ObjectMapper}
 	 * @param handler the {@code handler}
 	 * @param remoteInterface the interface
 	 */
 	public JsonRpcServer(ObjectMapper mapper, Object handler, Class<?> remoteInterface) {
 		this.mapper				= mapper;
 		this.handler 			= handler;
 		this.remoteInterface	= remoteInterface;
 	}
 
 	/**
 	 * Creates the server with the given {@link ObjectMapper} delegating
 	 * all calls to the given {@code handler}.
 	 * @param mapper the {@link ObjectMapper}
 	 * @param handler the {@code handler}
 	 */
 	public JsonRpcServer(ObjectMapper mapper, Object handler) {
 		this(mapper, handler, null);
 	}
 
 	/**
 	 * Creates the server with a default {@link ObjectMapper} delegating
 	 * all calls to the given {@code handler} {@link Object} but only
 	 * methods available on the {@code remoteInterface}.
 	 * @param handler the {@code handler}
 	 * @param remoteInterface the interface
 	 */
 	public JsonRpcServer(Object handler, Class<?> remoteInterface) {
 		this(new ObjectMapper(), handler, remoteInterface);
 	}
 
 	/**
 	 * Creates the server with a default {@link ObjectMapper} delegating
 	 * all calls to the given {@code handler}.
 	 * @param handler the {@code handler}
 	 */
 	public JsonRpcServer(Object handler) {
 		this(new ObjectMapper(), handler, null);
 	}
 
 	/**
 	 * Handles a single request from the given {@link InputStream},
 	 * that is to say that a single {@link JsonNode} is read from
 	 * the stream and treated as a JSON-RPC request.  All responses
 	 * are written to the given {@link OutputStream}.
 	 * @param ips the {@link InputStream}
 	 * @param ops the {@link OutputStream}
 	 * @throws JsonParseException
 	 * @throws JsonMappingException
 	 * @throws IOException
 	 */
 	public void handle(HttpServletRequest request, HttpServletResponse response)
 		throws JsonParseException,
 		JsonMappingException,
 		IOException {
 
     	// set response type
     	response.setContentType(JSONRPC_RESPONSE_CONTENT_TYPE);
  
     	// setup streams
     	InputStream input 	= null;
     	OutputStream output	= response.getOutputStream();
 
 		// POST
 		if (request.getMethod().equals("POST")) {
 			input = request.getInputStream();
 
 		// GET
 		} else if (request.getMethod().equals("GET")) {
 
 			// get parameters
 			String method	= request.getParameter("method");
 			String id		= request.getParameter("id");
 			String params 	= URLDecoder.decode(new String(Base64.decode(
 				request.getParameter("params"))), "UTF-8");
 
 			// create full RPC request
 			StringBuilder buff = new StringBuilder();
 			buff.append("{ ")
 				.append("\"id\": \"").append(id).append("\", ")
 				.append("\"method\": \"").append(method).append("\", ")
 				.append("\"params\": ").append(params).append(" ")
 				.append("}");
 
 			// setup stream to byte array
 			input = new ByteArrayInputStream(buff.toString().getBytes());
 
 		// invalid request
 		} else {
 			throw new IOException(
 				"Invalid request method, only POST and GET is supported");
 		}
 
 		// service the request
 		handleNode(mapper.readValue(input, JsonNode.class), output);
 	}
 
 	/**
 	 * Handles a single request from the given {@link InputStream},
 	 * that is to say that a single {@link JsonNode} is read from
 	 * the stream and treated as a JSON-RPC request.  All responses
 	 * are written to the given {@link OutputStream}.
 	 * @param ips the {@link InputStream}
 	 * @param ops the {@link OutputStream}
 	 * @throws JsonParseException
 	 * @throws JsonMappingException
 	 * @throws IOException
 	 */
 	public void handle(InputStream ips, OutputStream ops)
 		throws JsonParseException,
 		JsonMappingException,
 		IOException {
 		handleNode(mapper.readValue(ips, JsonNode.class), ops);
 	}
 
 	/**
 	 * Returns the handler's class or interface.
 	 * @return the class
 	 */
 	private Class<?> getHandlerClass() {
 		return (remoteInterface != null)
 			? remoteInterface : handler.getClass();
 	}
 
 	/**
 	 * Handles the given {@link JsonNode} and writes the
 	 * responses to the given {@link OutputStream}.
 	 * @param node the {@link JsonNode}
 	 * @param ops the {@link OutputStream}
 	 * @throws JsonGenerationException
 	 * @throws JsonMappingException
 	 * @throws IOException
 	 */
 	private void handleNode(JsonNode node, OutputStream ops)
 		throws JsonGenerationException,
 		JsonMappingException,
 		IOException {
 
 		// handle objects
 		if (node.isObject()) {
 			handleObject(ObjectNode.class.cast(node), ops);
 
 		// handle arrays
 		} else if (node.isArray()) {
 			handleArray(ArrayNode.class.cast(node), ops);
 
 		// bail on bad data
 		} else {
 			throw new IllegalArgumentException(
 				"Invalid JsonNode type: "+node.getClass().getName());
 		}
 	}
 
 	/**
 	 * Handles the given {@link ArrayNode} and writes the
 	 * responses to the given {@link OutputStream}.
 	 * @param node the {@link JsonNode}
 	 * @param ops the {@link OutputStream}
 	 * @throws JsonGenerationException
 	 * @throws JsonMappingException
 	 * @throws IOException
 	 */
 	private void handleArray(ArrayNode node, OutputStream ops)
 		throws JsonGenerationException,
 		JsonMappingException,
 		IOException {
 
 		// loop through each array element
 		for (int i=0; i<node.size(); i++) {
 			handleNode(node.get(i), ops);
 		}
 	}
 
 	/**
 	 * Handles the given {@link ObjectNode} and writes the
 	 * responses to the given {@link OutputStream}.
 	 * @param node the {@link JsonNode}
 	 * @param ops the {@link OutputStream}
 	 * @throws JsonGenerationException
 	 * @throws JsonMappingException
 	 * @throws IOException
 	 */
 	private void handleObject(ObjectNode node, OutputStream ops)
 		throws JsonGenerationException,
 		JsonMappingException,
 		IOException {
 
 		// validate request
 		if (!node.has("jsonrpc") || !node.has("method")) {
 			mapper.writeValue(ops, createErrorResponse(
 				"jsonrpc", "null", -32600, "Invalid Request", null));
 			return;
 		}
 
 		// parse request
 		String jsonRpc		= node.get("jsonrpc").getValueAsText();
 		String methodName	= node.get("method").getValueAsText();
 		String id			= node.get("id").getValueAsText();
 		JsonNode params		= node.get("params");
 		int paramCount		= (params!=null) ? params.size() : 0;
 
 		// find methods
 		Set<Method> methods = new HashSet<Method>();
 		methods.addAll(ReflectionUtil.findMethods(getHandlerClass(), methodName));
 
 		// iterate through the methods and remove
 		// the one's who's parameter count's don't
 		// match the request
 		Iterator<Method> itr = methods.iterator();
 		while (itr.hasNext()) {
 			Method method = itr.next();
 			if (method.getParameterTypes().length!=paramCount) {
 				itr.remove();
 			}
 		}
 
 		// method not found
 		if (methods.isEmpty()) {
 			mapper.writeValue(ops, createErrorResponse(
 				jsonRpc, id, -32601, "Method not found", null));
 			return;
 		}
 
 		// choose a method
 		Method method = null;
 		List<JsonNode> paramNodes = new ArrayList<JsonNode>();
 
 		// handle param arrays, no params
 		if (paramCount==0 || params.isArray()) {
 			method = methods.iterator().next();
 			for (int i=0; i<paramCount; i++) {
 				paramNodes.add(params.get(i));
 			}
 
 		// handle named params
 		} else if (params.isObject()) {
 
 			// loop through each method
 			for (Method m : methods) {
 
 				// get method annotations
 				Annotation[][] annotations = m.getParameterAnnotations();
 				boolean found = true;
 				List<JsonNode> namedParams = new ArrayList<JsonNode>();
 				for (int i=0; i<annotations.length; i++) {
 
 					// look for param name annotations
 					String paramName = null;
 					for (int j=0; j<annotations[i].length; j++) {
 						if (!JsonRpcParamName.class.isInstance(annotations[i][j])) {
 							continue;
 						} else {
 							paramName = JsonRpcParamName.class.cast(annotations[i][j]).value();
 							continue;
 						}
 					}
 
 					// bail if param name wasn't found
 					if (paramName==null) {
 						found = false;
 						break;
 
 					// found it by name
 					} else if (params.has(paramName)) {
 						namedParams.add(params.get(paramName));
 					}
 				}
 
 				// did we find it?
 				if (found) {
 					method = m;
 					paramNodes.addAll(namedParams);
 					break;
 				}
 			}
 		}
 
 		// invalid parameters
 		if (method==null) {
 			mapper.writeValue(ops, createErrorResponse(
 				jsonRpc, id, -32602, "Invalid method parameters.", null));
 			return;
 		}
 
 		// invoke the method
 		JsonNode result = null;
 		ObjectNode error = null;
 		Throwable thrown = null;
 		try {
 			result = invoke(method, paramNodes);
 		} catch (Throwable e) {
 			thrown = e;
 			if (InvocationTargetException.class.isInstance(e)) {
 				e = InvocationTargetException.class.cast(e).getTargetException();
 			}
 			error = mapper.createObjectNode();
 			error.put("code", 0);
 			error.put("message", e.getMessage());
 			error.put("data", e.getClass().getName());
 		}
 
 		// bail if notification request
 		if (id==null) {
 			return;
 		}
 
 		// create response
 		ObjectNode response = mapper.createObjectNode();
 		response.put("jsonrpc", jsonRpc);
 		response.put("id", id);
 		if (error==null) {
 			response.put("result", result);
 		} else if (error!=null) {
 			response.put("error", error);
 		}
 
 		// write it
 		mapper.writeValue(ops, response);
 
 		// log and potentially re-throw errors
 		if (thrown!=null) {
 			LOGGER.log(Level.SEVERE, "Error in JSON-RPC Service", thrown);
 			if (rethrowExceptions) {
 				throw new RuntimeException(thrown);
 			}
 		}
 	}
 
 	/**
 	 * Invokes the given method on the {@code handler} passing
 	 * the given params (after converting them to beans\objects)
 	 * to it.
 	 * @param m the method to invoke
 	 * @param params the params to pass to the method
 	 * @return the return value (or null if no return)
 	 * @throws JsonParseException
 	 * @throws JsonMappingException
 	 * @throws IOException
 	 * @throws IllegalArgumentException
 	 * @throws IllegalAccessException
 	 * @throws InvocationTargetException
 	 */
 	private JsonNode invoke(Method m, List<JsonNode> params)
 		throws JsonParseException,
 		JsonMappingException,
 		IOException, 
 		IllegalArgumentException, 
 		IllegalAccessException, 
 		InvocationTargetException {
 
 		// convert the parameters
 		Object[] convertedParams = new Object[params.size()];
 		Type[] parameterTypes = m.getGenericParameterTypes();
 		for (int i=0; i<parameterTypes.length; i++) {
			convertedParams[i] = mapper.readValue(
				params.get(i), TypeFactory.type(parameterTypes[i]));
 		}
 
 		// invoke the method
 		Object result = m.invoke(handler, convertedParams);
 		return (m.getGenericReturnType()!=null) ? mapper.valueToTree(result) : null;
 	}
 
 	/**
 	 * Convenience method for creating an error response
 	 * @param jsonRpc the jsonrpc string
 	 * @param id the id
 	 * @param code the error code
 	 * @param message the error message
 	 * @param data the error data (if any)
 	 * @return the error response
 	 */
 	private ObjectNode createErrorResponse(
 		String jsonRpc, String id, int code, String message, Object data) {
 		ObjectNode response = mapper.createObjectNode();
 		ObjectNode error = mapper.createObjectNode();
 		error.put("code", code);
 		error.put("message", message);
 		if (data!=null) {
 			error.put("data",  mapper.valueToTree(data));
 		}
 		response.put("jsonrpc", jsonRpc);
 		response.put("id", id);
 		response.put("error", error);
 		return response;
 	}
 
 	/**
 	 * Indicates whether or not the server is re-throwing exceptions.
 	 * @return true if re-throwing, false otherwise
 	 */
 	public boolean isRethrowExceptions() {
 		return rethrowExceptions;
 	}
 
 	/**
 	 * Sets whether or not the server should re-throw exceptions.
 	 * @param rethrowExceptions true or false
 	 */
 	public void setRethrowExceptions(boolean rethrowExceptions) {
 		this.rethrowExceptions = rethrowExceptions;
 	}
 
 }
