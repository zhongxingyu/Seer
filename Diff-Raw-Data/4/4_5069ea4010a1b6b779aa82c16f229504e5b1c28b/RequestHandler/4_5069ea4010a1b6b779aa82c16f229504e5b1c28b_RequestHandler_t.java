 /**
  * ************************************************************************
  * 
  *    server-objects - a contrib to the Qooxdoo project that makes server 
  *    and client objects operate seamlessly; like Qooxdoo, server objects 
  *    have properties, events, and methods all of which can be access from
  *    either server or client, regardless of where the original object was
  *    created.
  * 
  *    http://qooxdoo.org
  * 
  *    Copyright:
  *      2010 Zenesis Limited, http://www.zenesis.com
  * 
  *    License:
  *      LGPL: http://www.gnu.org/licenses/lgpl.html
  *      EPL: http://www.eclipse.org/org/documents/epl-v10.php
  *      
  *      This software is provided under the same licensing terms as Qooxdoo,
  *      please see the LICENSE file in the Qooxdoo project's top-level directory 
  *      for details.
  * 
  *    Authors:
  *      * John Spackman (john.spackman@zenesis.com)
  * 
  * ************************************************************************
  */
 package com.zenesis.qx.remote;
 
 import java.io.IOException;
 import java.io.OutputStream;
 import java.io.OutputStreamWriter;
 import java.io.Reader;
 import java.io.StringReader;
 import java.io.StringWriter;
 import java.io.Writer;
 import java.lang.reflect.Array;
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.Map;
 
 import javax.servlet.ServletException;
 import org.apache.log4j.Logger;
 
 import com.fasterxml.jackson.core.JsonParser;
 import com.fasterxml.jackson.core.JsonToken;
 import com.fasterxml.jackson.databind.ObjectMapper;
 import com.zenesis.qx.event.EventManager;
 import com.zenesis.qx.remote.CommandId.CommandType;
 
 /**
  * Handles the request and responses for a client.
  * 
  * This uses the Jackson JSON parser to pull data incrementally from the request; this makes the 
  * code harder to read/write and means that we expect the JSON data to occur in a particular 
  * order even though the JSON specification does not allow ordering to be enforced.  However,
  * by dealing with data incrementally in the way we are able to delay deciding what type of
  * data to instantiate until we have worked out where it is going - i.e. we look at the types
  * of a method's parameters and use that type information to change the way we parse.  In this
  * way, we can support any arbitrary mapping between JSON and Java thanks to Jackson.
  * 
  * @author "John Spackman <john.spackman@zenesis.com>"
  *
  */
 public class RequestHandler {
 	
 	private static final Logger log = Logger.getLogger(RequestHandler.class);
 	
 	// Command type strings received from the client
 	private static final String CMD_BOOTSTRAP = "bootstrap";	// Reset application session and get bootstrap
 	private static final String CMD_CALL = "call";				// Call server object method 
 	private static final String CMD_DISPOSE = "dispose";		// The client has disposed of a Proxied object 
 	private static final String CMD_EDIT_ARRAY = "edit-array";	// Changes to an array 
 	private static final String CMD_EXPIRE = "expire";			// Expires a flushed property value 
 	private static final String CMD_LISTEN = "listen";			// Add an event listener 
 	private static final String CMD_NEW = "new";				// Create a new object 
 	private static final String CMD_POLL = "poll";				// Poll for changes (ie do nothing) 
 	private static final String CMD_SET = "set";				// Set a property value 
 	private static final String CMD_UNLISTEN = "unlisten";		// Remove an event listener
 
 	// This class is sent as data by cmdNewObject to change a client ID into a server ID
 	public static final class MapClientId {
 		public final int serverId;
 		public final int clientId;
 
 		public MapClientId(int serverId, int clientId) {
 			super();
 			this.serverId = serverId;
 			this.clientId = clientId;
 		}
 	}
 	
 	// This class is thrown to provide Exception information to the client
 	public static class ExceptionDetails {
 		public final String exceptionClass;
 		public final String message;
 		
 		/**
 		 * @param exceptionClass
 		 * @param message
 		 */
 		public ExceptionDetails(String exceptionClass, String message) {
 			super();
 			this.exceptionClass = exceptionClass;
 			this.message = message;
 		}
 		
 	}
 	
 	// Sent when a function returns
 	public static final class FunctionReturn {
 		public final int asyncId;
 		public final Object result;
 		public FunctionReturn(int asyncId, Object result) {
 			super();
 			this.asyncId = asyncId;
 			this.result = result;
 		}
 		
 	}
 
 	// This class is sent as data when an exception is thrown while setting a property value
 	public static final class PropertyReset extends ExceptionDetails {
 		public final Object oldValue;
 		
 		/**
 		 * @param oldValue
 		 * @param exceptionClass
 		 * @param message
 		 */
 		public PropertyReset(Object oldValue, String exceptionClass, String message) {
 			super(exceptionClass, message);
 			this.oldValue = oldValue;
 		}		
 	}
 
 	// RequestHandler for the current thread
 	private static ThreadLocal<RequestHandler> s_currentHandler = new ThreadLocal<RequestHandler>();
 	
 	// Tracker for the session
 	private final ProxySessionTracker tracker;
 	
 	// Client Objects, indexed by client ID (negative) 
 	private HashMap<Integer, Proxied> clientObjects;
 	
 	// The property currently having it's property set
 	private Proxied setPropertyObject;
 	private String setPropertyName;
 	
 	/**
 	 * @param tracker
 	 */
 	public RequestHandler(ProxySessionTracker tracker) {
 		super();
 		this.tracker = tracker;
 	}
 
 	/**
 	 * Handles the callback from the client; expects either an object or an array of objects
 	 * @param request
 	 * @param response
 	 * @throws ServletException
 	 * @throws IOException
 	 */
 	public void processRequest(Reader request, OutputStream response) throws ServletException, IOException {
 		processRequest(request, new OutputStreamWriter(response));
 	}
 
 	/**
 	 * Handles the callback from the client; expects either an object or an array of objects
 	 * @param request
 	 * @param response
 	 * @throws ServletException
 	 * @throws IOException
 	 */
 	public void processRequest(Reader request, Writer response) throws ServletException, IOException {
 		s_currentHandler.set(this);
 		ObjectMapper objectMapper = tracker.getObjectMapper();
 		try {
 			if (log.isDebugEnabled()) {
 				StringWriter sw = new StringWriter();
 				char[] buffer = new char[32 * 1024];
 				int length;
 				while ((length = request.read(buffer)) > 0) {
 					sw.write(buffer, 0, length);
 				}
 				log.debug("Received: " + sw.toString());
 				request = new StringReader(sw.toString());
 			}
 			JsonParser jp = objectMapper.getJsonFactory().createJsonParser(request);
 			if (jp.nextToken() == JsonToken.START_ARRAY) {
 				while(jp.nextToken() != JsonToken.END_ARRAY)
 					processCommand(jp);
 			} else if (jp.getCurrentToken() == JsonToken.START_OBJECT)
 				processCommand(jp);
 	
 			if (tracker.hasDataToFlush()) {
 				Writer actualResponse = response;
 				if (log.isDebugEnabled()) {
 					final Writer tmp = response;
 					actualResponse = new Writer() {
 						@Override
 						public void close() throws IOException {
 							tmp.close();
 						}
 	
 						@Override
 						public void flush() throws IOException {
 							tmp.flush();
 						}
 	
 						@Override
 						public void write(char[] arg0, int arg1, int arg2) throws IOException {
 							System.out.print(new String(arg0, arg1, arg2));
 							tmp.write(arg0, arg1, arg2);
 						}
 					};
 				}
 				objectMapper.writeValue(actualResponse, tracker.getQueue());
 			}
 			
 		} catch(ProxyTypeSerialisationException e) {
 			log.fatal("Unable to serialise type information to client: " + e.getMessage(), e);
 			
 		} catch(ProxyException e) {
 			handleException(response, objectMapper, e);
 			
 		} catch(Exception e) {
 			log.error("Exception during callback: " + e.getMessage(), e);
 			tracker.getQueue().queueCommand(CommandType.EXCEPTION, null, null, new ExceptionDetails(e.getClass().getName(), e.getMessage()));
 			objectMapper.writeValue(response, tracker.getQueue());
 			
 		} finally {
 			s_currentHandler.set(null);
 		}
 	}
 	
 	/**
 	 * Called to handle exceptions during processRequest
 	 * @param response
 	 * @param objectMapper
 	 * @param e
 	 * @throws IOException
 	 */
 	protected void handleException(Writer response, ObjectMapper objectMapper, ProxyException e) throws IOException {
 		tracker.getQueue().queueCommand(CommandType.EXCEPTION, e.getServerObject(), null, new ExceptionDetails(e.getClass().getName(), e.getMessage()));
 		objectMapper.writeValue(response, tracker.getQueue());
 	}
 	
 	/**
 	 * Returns the request handler for the current thread
 	 * @return
 	 */
 	public static RequestHandler getCurrentHandler() {
 		return s_currentHandler.get();
 	}
 	
 	/**
 	 * Handles an object from the client; expects the object to have a property "cmd" which is
 	 * the type of command
 	 * @param jp
 	 * @throws ServletException
 	 * @throws IOException
 	 */
 	protected void processCommand(JsonParser jp) throws ServletException, IOException {
 		String cmd = getFieldValue(jp, "cmd", String.class);
 		
 		if (cmd.equals(CMD_BOOTSTRAP))
 			cmdBootstrap(jp);
 		
 		else if (cmd.equals(CMD_CALL))
 			cmdCallServerMethod(jp);
 		
 		else if (cmd.equals(CMD_DISPOSE))
 			cmdDispose(jp);
 		
 		else if (cmd.equals(CMD_EDIT_ARRAY))
 			cmdEditArray(jp);
 		
 		else if (cmd.equals(CMD_EXPIRE))
 			cmdExpire(jp);
 		
 		else if (cmd.equals(CMD_LISTEN))
 			cmdAddListener(jp);
 		
 		else if (cmd.equals(CMD_NEW))
 			cmdNewObject(jp);
 		
 		else if (cmd.equals(CMD_POLL))
 			cmdPoll(jp);
 		
 		else if (cmd.equals(CMD_SET))
 			cmdSetProperty(jp);
 		
 		else if (cmd.equals(CMD_UNLISTEN))
 			cmdRemoveListener(jp);
 		
 		else
 			throw new ServletException("Unrecognised command from client: " + cmd);
 	}
 	
 	/**
 	 * Resets the application session and returns the bootstrap object to the client
 	 * @param jp
 	 */
 	protected void cmdBootstrap(JsonParser jp) throws ServletException, IOException {
 		tracker.resetSession();
 		tracker.getQueue().queueCommand(CommandId.CommandType.BOOTSTRAP, null, null, tracker.getBootstrap());
 		jp.nextToken();
 	}
 	
 	/**
 	 * Handles a server method call from the client; expects a serverId, methodName, and an optional
 	 * array of parameters
 	 * @param jp
 	 * @throws ServletException
 	 * @throws IOException
 	 */
 	protected void cmdCallServerMethod(JsonParser jp) throws ServletException, IOException {
 		// Get the basics
 		int serverId = getFieldValue(jp, "serverId", Integer.class);
 		String methodName = getFieldValue(jp, "methodName", String.class);
 		int asyncId = getFieldValue(jp, "asyncId", Integer.class);
 		Proxied serverObject = getProxied(serverId);
 		
 		
 		// Onto what should be parameters
 		jp.nextToken();
 		
 		// Find the method by hand - we have already guaranteed that there will not be conflicting
 		//	method names (ie no overridden methods) but Java needs a list of parameter types
 		//	so we do it ourselves.
 		boolean found = false;
 		if (methodName.length() > 3 && (methodName.startsWith("get") || methodName.startsWith("set"))) {
 			String name = methodName.substring(3, 4).toLowerCase();
 			if (methodName.length() > 4)
 				name += methodName.substring(4);
 			for (ProxyType type = ProxyTypeManager.INSTANCE.getProxyType(serverObject.getClass()); type != null; type = type.getSuperType()) {
 				ProxyProperty property = type.getProperties().get(name);
 				if (property != null) {
 					Object result = null;
 					if (methodName.startsWith("get")) {
 						readParameters(jp, null);
 						result = property.getValue(serverObject);
 					} else {
 						Object[] values = readParameters(jp, new Class[] { property.getPropertyClass().getJavaType() });
 						property.setValue(serverObject, values[0]);
 					}
 					if (property.isOnDemand())
 						tracker.setClientHasValue(serverObject, property);
 					tracker.getQueue().queueCommand(CommandId.CommandType.FUNCTION_RETURN, serverObject, null, new FunctionReturn(asyncId, result));
 					found = true;
 					break;
 				}
 			}
 		}
 
 		if (!found)
 			for (ProxyType type = ProxyTypeManager.INSTANCE.getProxyType(serverObject.getClass()); type != null && !found; type = type.getSuperType()) {
 				ProxyMethod[] methods = type.getMethods();
 				for (int i = 0; i < methods.length; i++)
 					if (methods[i].getName().equals(methodName)) {
 						Method method = methods[i].getMethod();
 						
 						// Call the method
 						Object[] values = null;
 						try {
 							values = readParameters(jp, method.getParameterTypes());
 							Object result = method.invoke(serverObject, values);
 							tracker.getQueue().queueCommand(CommandId.CommandType.FUNCTION_RETURN, serverObject, null, new FunctionReturn(asyncId, result));
 						}catch(InvocationTargetException e) {
 							Throwable t = e.getCause();
 							log.error("Exception while invoking " + method + "(" + Helpers.toString(values) + ") on " + serverObject + ": " + t.getMessage(), t);
 							throw new ProxyException(serverObject, "Exception while invoking " + method + " on " + serverObject + ": " + t.getMessage(), t);
 						}catch(RuntimeException e) {
 							log.error("Exception while invoking " + method + "(" + Helpers.toString(values) + ") on " + serverObject + ": " + e.getMessage(), e);
 							throw new ProxyException(serverObject, "Exception while invoking " + method + " on " + serverObject + ": " + e.getMessage(), e);
 						}catch(IllegalAccessException e) {
 							throw new ServletException("Exception while running " + method + "(" + Helpers.toString(values) + "): " + e.getMessage(), e);
 						}
 						found = true;
 						break;
 					}
 			}
 
 		if (!found)
 			throw new ServletException("Cannot find method called " + methodName + " in " + serverObject);
 		
 		jp.nextToken();
 	}
 	
 	private Object[] readParameters(JsonParser jp, Class[] types) throws IOException {
 		if (types == null) {
 			// Check for parameters
 			if (jp.getCurrentToken() == JsonToken.FIELD_NAME &&
 					jp.getCurrentName().equals("parameters") &&
 					jp.nextToken() == JsonToken.START_ARRAY) {
 				while (jp.nextToken() != JsonToken.END_ARRAY)
 					;
 			}
 			return null;
 		}
 		Object[] values = new Object[types.length];
 		Object[] params = null;
 		
 		// Check for parameters
 		if (jp.getCurrentToken() == JsonToken.FIELD_NAME &&
 				jp.getCurrentName().equals("parameters") &&
 				jp.nextToken() == JsonToken.START_ARRAY) {
 			
 			params = readArray(jp, types);
 		}
 		
 		for (int i = 0; i < values.length; i++)
 			if (i < params.length)
 				values[i] = params[i];
 			else
 				values[i] = null;
 		
 		return values;
 	}
 	
 	/**
 	 * Called when the client has disposed of
 	 * @param jp
 	 * @throws ServletException
 	 * @throws IOException
 	 */
 	protected void cmdDispose(JsonParser jp) throws ServletException, IOException {
 		skipFieldName(jp, "serverIds");
 		while (jp.nextToken() != JsonToken.END_ARRAY) {
 			int serverId = jp.readValueAs(Integer.class);
 			tracker.forget(serverId);
 		}
 		
 		jp.nextToken();
 	}
 	
 	/**
 	 * Handles setting a server object property from the client; expects a serverId, propertyName, and a value
 	 * @param jp
 	 * @throws ServletException
 	 * @throws IOException
 	 */
 	protected void cmdSetProperty(JsonParser jp) throws ServletException, IOException {
 		// Get the basics
 		int serverId = getFieldValue(jp, "serverId", Integer.class);
 		String propertyName = getFieldValue(jp, "propertyName", String.class);
 		Object value = null;
 
 		Proxied serverObject = getProxied(serverId);
 		ProxyType type = ProxyTypeManager.INSTANCE.getProxyType(serverObject.getClass());
 		ProxyProperty prop = getProperty(type, propertyName);
 		
 		skipFieldName(jp, "value");
 		MetaClass propClass = prop.getPropertyClass();
 		if (propClass.isSubclassOf(Proxied.class)) {
 			
 			if (propClass.isArray() || propClass.isCollection()) {
 				value = readArray(jp, propClass.getJavaType());
 				
 			} else if (propClass.isMap()) {
 				value = readMap(jp, propClass.getKeyClass(), propClass.getJavaType());
 				
 			} else {
 				Integer id = jp.readValueAs(Integer.class);
 				if (id != null)
 					value = getProxied(id);
 			}
 		} else {
 			if (propClass.isArray() || propClass.isCollection()) {
 				value = readArray(jp, propClass.getJavaType());
 				
 			} else if (propClass.isMap()) {
 				value = readMap(jp, propClass.getKeyClass(), propClass.getJavaType());
 				
 			} else {
 				value = jp.readValueAs(Object.class);
 				if (value != null && Enum.class.isAssignableFrom(propClass.getJavaType())) {
 					String str = Helpers.camelCaseToEnum(value.toString());
 					value = Enum.valueOf(propClass.getJavaType(), str);
 				}
 			}
 		}
 		
 		setPropertyValue(type, serverObject, propertyName, value);
 		jp.nextToken();
 	}
 	
 	/**
 	 * Sent when the client expires a cached property value, allowing the server property 
 	 * to also its flush caches; expects a serverId and propertyName
 	 * @param jp
 	 * @throws ServletException
 	 * @throws IOException
 	 */
 	protected void cmdExpire(JsonParser jp) throws ServletException, IOException {
 		// Get the basics
 		int serverId = getFieldValue(jp, "serverId", Integer.class);
 		String propertyName = getFieldValue(jp, "propertyName", String.class);
 
 		Proxied serverObject = getProxied(serverId);
 		ProxyType type = ProxyTypeManager.INSTANCE.getProxyType(serverObject.getClass());
 		ProxyProperty prop = getProperty(type, propertyName);
 		prop.expire(serverObject);
 		
 		jp.nextToken();
 	}
 	
 	/**
 	 * Handles dynamic changes to a qa.data.Array instance without having a complete replacement; expects a 
 	 * serverId, propertyName, type (one of "add", "remove", "order"), start, end, and optional array of items 
 	 * @param jp
 	 * @throws ServletException
 	 * @throws IOException
 	 */
 	protected void cmdEditArray(JsonParser jp) throws ServletException, IOException {
 		// Get the basics
 		int serverId = getFieldValue(jp, "serverId", Integer.class);
 		String propertyName = getFieldValue(jp, "propertyName", String.class);
 		String action = getFieldValue(jp, "type", String.class);
 		Integer start = null;
 		Integer end = null;
 		
 		if (!action.equals("replaceAll")) {
 			start = getFieldValue(jp, "start", Integer.class);
 			end = getFieldValue(jp, "end", Integer.class);
 		}
 		
 		// Get our info
 		Proxied serverObject = getProxied(serverId);
 		ProxyType type = ProxyTypeManager.INSTANCE.getProxyType(serverObject.getClass());
 		ProxyProperty prop = getProperty(type, propertyName);
 		
 		if (prop.getPropertyClass().isMap()) {
 			Map items = null;
 			
 			// Get the optional array of items
 			if (jp.nextToken() == JsonToken.FIELD_NAME &&
 					jp.getCurrentName().equals("items") &&
 					jp.nextToken() == JsonToken.START_OBJECT) {
 				
 				items = readMap(jp, prop.getPropertyClass().getKeyClass(), prop.getPropertyClass().getJavaType());
 			}
 			
 			// Quick logging
 			if (log.isInfoEnabled()) {
 				String str = "";
 				if (items != null)
 					for (Object key : items.keySet()) {
 						if (str.length() > 0)
 							str += ", ";
 						str += String.valueOf(key) + "=" + String.valueOf(items.get(key));
 					}
 				log.info("edit-array: property=" + prop + ", type=" + action + ", start=" + start + ", end=" + end + str);
 			}
 			
 			if (action.equals("replaceAll")) {
 				Map map = (Map)prop.getValue(serverObject);
 				map.clear();
 				map.putAll(items);
 			} else
 				throw new IllegalArgumentException("Unsupported action in cmdEditArray: " + action);
 			
 			jp.nextToken();
 		} else {
 			// NOTE: items is an Array!!  But because it may be an array of primitive types, we have
 			//	to use java.lang.reflect.Array to access members because we cannot cast arrays of
 			//	primitives to Object[]
 			Object items = null;
 			
 			// Get the optional array of items
 			if (jp.nextToken() == JsonToken.FIELD_NAME &&
 					jp.getCurrentName().equals("items") &&
 					jp.nextToken() == JsonToken.START_ARRAY) {
 				
 				items = readArray(jp, prop.getPropertyClass().getJavaType());
 			}
 			int itemsLength = Array.getLength(items);
 			
 			// Quick logging
 			if (log.isInfoEnabled()) {
 				String str = "";
 				if (items != null)
 					for (int i = 0; i < itemsLength; i++) {
 						if (str.length() != 0)
 							str += ", ";
 						str += Array.get(items, i);
 					}
 				log.info("edit-array: property=" + prop + ", type=" + action + ", start=" + start + ", end=" + end + str);
 			}
 			
 			if (action.equals("replaceAll")) {
 				if (prop.getPropertyClass().isCollection()) {
 					Collection list = (Collection)prop.getValue(serverObject);
 					if (list == null) {
 						try {
 							list = (Collection)prop.getPropertyClass().getCollectionClass().newInstance();
 						}catch(Exception e) {
 							throw new IllegalArgumentException(e.getMessage(), e);
 						}
 						prop.setValue(serverObject, list);
 					}
 					list.clear();
 					if (items != null)
 						for (int i = 0; i < itemsLength; i++)
 							list.add(Array.get(items, i));
					
					// Because collection properties are objects and we change them without the serverObject's
					//	knowledge, we have to make sure we notify other trackers ourselves
					ProxyManager.propertyChanged(serverObject, propertyName, list, null);
 				} else {
 					prop.setValue(serverObject, items);
 				}
 			} else
 				throw new IllegalArgumentException("Unsupported action in cmdEditArray: " + action);
 			
 			jp.nextToken();
 		}
 	}
 	
 	/**
 	 * Handles creating a server object to match one created on the client; expects className,
 	 * clientId, properties
 	 * @param jp
 	 * @throws ServletException
 	 * @throws IOException
 	 */
 	protected void cmdNewObject(JsonParser jp) throws ServletException, IOException {
 		// Get the basics
 		String className = getFieldValue(jp, "className", String.class);
 		int clientId = getFieldValue(jp, "clientId", Integer.class);
 
 		// Get the class
 		Class<? extends Proxied> clazz;
 		try {
 			clazz = (Class<? extends Proxied>)Class.forName(className);
 		} catch(ClassNotFoundException e) {
 			throw new ServletException("Unknown class " + className);
 		}
 		
 		// Create the instance
 		Proxied proxied;
 		try {
 			proxied = clazz.newInstance();
 		} catch(InstantiationException e) {
 			throw new ServletException("Cannot create class " + className + ": " + e.getMessage(), e);
 		} catch(IllegalAccessException e) {
 			throw new ServletException("Cannot create class " + className + ": " + e.getMessage(), e);
 		}
 		
 		// Get the server ID
 		int serverId = tracker.addClientObject(proxied);
 		ProxyType type = ProxyTypeManager.INSTANCE.getProxyType(clazz);
 		
 		// Remember the client ID, in case there are subsequent commands which refer to it
 		if (clientObjects == null)
 			clientObjects = new HashMap<Integer, Proxied>();
 		clientObjects.put(clientId, proxied);
 		
 		// Tell the client about the new ID - do this before changing properties
 		tracker.getQueue().queueCommand(CommandId.CommandType.MAP_CLIENT_ID, proxied, null, new MapClientId(serverId, clientId));
 		
 		// Set property values
 		if (jp.nextToken() == JsonToken.FIELD_NAME) {
 			if (jp.nextToken() != JsonToken.START_OBJECT)
 				throw new ServletException("Unexpected properties definiton for 'new' command");
 			while (jp.nextToken() != JsonToken.END_OBJECT) {
 				String propertyName = jp.getCurrentName();
 				jp.nextToken();
 				
 				// Read a Proxied object?  
 				ProxyProperty prop = getProperty(type, propertyName);
 				MetaClass propClass = prop.getPropertyClass();
 				Object value = null;
 				if (propClass.isSubclassOf(Proxied.class)) {
 					
 					if (propClass.isArray() || propClass.isCollection()) {
 						value = readArray(jp, propClass.getJavaType());
 						
 					} else if (propClass.isMap()) {
 						value = readMap(jp, propClass.getKeyClass(), propClass.getJavaType());
 						
 					} else {
 						Integer id = jp.readValueAs(Integer.class);
 						if (id != null)
 							value = getProxied(id);
 					}
 				} else {
 					value = jp.readValueAs(Object.class);
 					if (value != null && Enum.class.isAssignableFrom(propClass.getJavaType())) {
 						String str = Helpers.camelCaseToEnum(value.toString());
 						value = Enum.valueOf(propClass.getJavaType(), str);
 					}
 				}
 				setPropertyValue(type, proxied, propertyName, value);
 			}
 		}
 		
 		// Done
 		jp.nextToken();
 	}
 	
 	/**
 	 * Handles creating a server object to match one created on the client; expects className,
 	 * clientId, properties
 	 * @param jp
 	 * @throws ServletException
 	 * @throws IOException
 	 */
 	protected void cmdPoll(JsonParser jp) throws ServletException, IOException {
 		jp.nextToken();
 	}
 	
 	/**
 	 * Handles adding an event listener; expects serverId, eventName
 	 * @param jp
 	 * @throws ServletException
 	 * @throws IOException
 	 */
 	protected void cmdAddListener(JsonParser jp) throws ServletException, IOException {
 		int serverId = getFieldValue(jp, "serverId", Integer.class);
 		String eventName = getFieldValue(jp, "eventName", String.class);
 		
 		Proxied serverObject = getProxied(serverId);
 		EventManager.addListener(serverObject, eventName, ProxyManager.getInstance());
 		jp.nextToken();
 	}
 	
 	/**
 	 * Handles removing an event listener; expects serverId, eventName
 	 * @param jp
 	 * @throws ServletException
 	 * @throws IOException
 	 */
 	protected void cmdRemoveListener(JsonParser jp) throws ServletException, IOException {
 		int serverId = getFieldValue(jp, "serverId", Integer.class);
 		String eventName = getFieldValue(jp, "eventName", String.class);
 		
 		Proxied serverObject = getProxied(serverId);
 		EventManager.removeListener(serverObject, eventName, ProxyManager.getInstance());
 		jp.nextToken();
 	}
 	
 	/**
 	 * Returns the proxied object, by serverID or client ID
 	 * @param id
 	 * @return
 	 */
 	protected Proxied getProxied(int id) {
 		Proxied proxied;
 		if (id < 0)
 			proxied = clientObjects.get(id);
 		else
 			proxied = tracker.getProxied(id);
 		return proxied;
 	}
 	
 	/**
 	 * Finds a property in a type, recursing up the class hierarchy
 	 * @param type
 	 * @param name
 	 * @return
 	 */
 	protected ProxyProperty getProperty(ProxyType type, String name) {
 		while (type != null) {
 			ProxyProperty prop = type.getProperties().get(name);
 			if (prop != null)
 				return prop;
 			type = type.getSuperType();
 		}
 		return null;
 	}
 	
 	/**
 	 * Sets a property value, tracking which property is being set so that isSettingProperty can
 	 * detect recursive sets
 	 * @param type
 	 * @param proxied
 	 * @param propertyName
 	 * @param value
 	 */
 	protected void setPropertyValue(ProxyType type, Proxied proxied, String propertyName, Object value) throws ProxyException {
 		if (setPropertyObject != null || setPropertyName != null)
 			throw new IllegalStateException("Recursive property setting!");
 		setPropertyObject = proxied;
 		setPropertyName = propertyName;
 		try {
 			ProxyProperty property = getProperty(type, propertyName);
 			
 			value = coerce(property.getPropertyClass().getJavaType(), value);
 			
 			if (!property.isSendExceptions())
 				property.setValue(proxied, value);
 			else {
 				Object oldValue = property.getValue(proxied);
 				try {
 					property.setValue(proxied, value);
 				} catch(Exception e) {
 					tracker.getQueue().queueCommand(CommandId.CommandType.RESTORE_VALUE, proxied, propertyName, new PropertyReset(oldValue, e.getClass().getName(), e.getMessage()));
 				}
 			}
 		}finally {
 			setPropertyObject = null;
 			setPropertyName = null;
 		}
 	}
 	
 	/**
 	 * Attempts to convert a native type - Jackson will interpret floating point numbers as
 	 * Double, which will cause an exception if the destination only accepts float.
 	 * @param clazz
 	 * @param value
 	 * @return
 	 */
 	protected Object coerce(Class clazz, Object value) {
 		if (value == null)
 			return null;
 		if (value.getClass() == clazz)
 			return value;
 		
 		if (value.getClass() == Double.class) {
 			double val = (Double)value;
 			if (clazz == float.class)
 				value = (float)val;
 			else if (clazz == int.class)
 				value = (int)val;
 			else if (clazz == long.class)
 				value = (long)val;
 			
 		} else if (value.getClass() == Long.class) {
 			long val = (Long)value;
 			if (clazz == float.class)
 				value = (float)val;
 			else if (clazz == int.class)
 				value = (int)val;
 			else if (clazz == long.class)
 				value = (long)val;
 		} 
 		
 		return value;
 	}
 	
 	/**
 	 * Detects if a property is currently being set as part of synchronising with the client
 	 * @param proxied
 	 * @param propertyName
 	 * @return
 	 */
 	public boolean isSettingProperty(Proxied proxied, String propertyName) {
 		if (setPropertyObject == null || setPropertyName == null)
 			return false;
 		return setPropertyObject == proxied && setPropertyName.equals(propertyName);
 	}
 	
 	/**
 	 * Reads an array from JSON, where each value is of the listed in types; EG the first element
 	 * is class type[0], the second element is class type[1] etc
 	 * @param jp
 	 * @param types
 	 * @return
 	 * @throws IOException
 	 */
 	private Object[] readArray(JsonParser jp, Class[] types) throws IOException {
 		if (jp.getCurrentToken() == JsonToken.VALUE_NULL)
 			return null;
 		
 		ArrayList result = new ArrayList();
 		for (int paramIndex = 0; jp.nextToken() != JsonToken.END_ARRAY; paramIndex++) {
 			Class type = null;
 			if (types != null && paramIndex < types.length)
 				type = types[paramIndex];
 			
 			if (type != null && type.isArray()) {
 				if (jp.getCurrentToken() == JsonToken.VALUE_NULL)
 					result.add(null);
 				else if (jp.getCurrentToken() == JsonToken.START_ARRAY) {
 					Object obj = readArray(jp, type.getComponentType());
 					result.add(obj);
 				} else
 					throw new IllegalStateException("Expected array but found " + jp.getCurrentToken());
 				
 			} else if (type != null && Proxied.class.isAssignableFrom(type)) {
 				Integer id = jp.readValueAs(Integer.class);
 				if (id != null) {
 					Proxied obj = getProxied(id);
 					result.add(obj);
 				} else
 					result.add(null);
 				
 			} else if (type != null && Enum.class.isAssignableFrom(type)) {
 				Object obj = jp.readValueAs(Object.class);
 				if (obj != null) {
 					String str = Helpers.camelCaseToEnum(obj.toString());
 					obj = Enum.valueOf(type, str);
 					result.add(obj);
 				}
 			} else {
 				Object obj = jp.readValueAs(type != null ? type : Object.class);
 				result.add(obj);
 			}
 		}
 		return result.toArray(new Object[result.size()]);
 	}
 	
 	/**
 	 * Reads an array from JSON, where each value is of the class clazz.  Note that while the result
 	 * is an array, you cannot assume that it is an array of Object, or use generics because generics
 	 * are always Objects - this is because arrays of primitive types are not arrays of Objects
 	 * @param jp
 	 * @param clazz
 	 * @return
 	 * @throws IOException
 	 */
 	private Object readArray(JsonParser jp, Class clazz) throws IOException {
 		if (jp.getCurrentToken() == JsonToken.VALUE_NULL)
 			return null;
 		
 		boolean isProxyClass = Proxied.class.isAssignableFrom(clazz);
 		ArrayList result = new ArrayList();
 		for (; jp.nextToken() != JsonToken.END_ARRAY;) {
 			if (isProxyClass) {
 				Integer id = jp.readValueAs(Integer.class);
 				if (id != null) {
 					Proxied obj = getProxied(id);
 					if (!clazz.isInstance(obj))
 						throw new ClassCastException("Cannot cast " + obj + " class " + obj.getClass() + " to " + clazz);
 					result.add(obj);
 				} else
 					result.add(null);
 			} else {
 				Object obj = readSimpleValue(jp, clazz);
 				result.add(obj);
 			}
 		}
 		
 		Object arr = Array.newInstance(clazz, result.size());
 		for (int i = 0; i < result.size(); i++)
 			Array.set(arr, i, result.get(i));
 		return arr;
 		//return result.toArray(Array.newInstance(clazz, result.size()));
 	}
 	
 	/**
 	 * Reads an array from JSON, where each value is of the class clazz.  Note that while the result
 	 * is an array, you cannot assume that it is an array of Object, or use generics because generics
 	 * are always Objects - this is because arrays of primitive types are not arrays of Objects
 	 * @param jp
 	 * @param clazz
 	 * @return
 	 * @throws IOException
 	 */
 	private Map readMap(JsonParser jp, Class keyClazz, Class clazz) throws IOException {
 		if (jp.getCurrentToken() == JsonToken.VALUE_NULL)
 			return null;
 		
 		boolean isProxyClass = Proxied.class.isAssignableFrom(clazz);
 		if (keyClazz == null)
 			keyClazz = String.class;
 		HashMap result = new HashMap();
 		for (; jp.nextToken() != JsonToken.END_OBJECT;) {
 			Object key = readSimpleValue(jp, keyClazz);
 			
 			jp.nextToken();
 			
 			if (isProxyClass) {
 				Integer id = jp.readValueAs(Integer.class);
 				if (id != null) {
 					Proxied obj = getProxied(id);
 					if (!clazz.isInstance(obj))
 						throw new ClassCastException("Cannot cast " + obj + " class " + obj.getClass() + " to " + clazz);
 					result.put(key, obj);
 				} else
 					result.put(key, null);
 			} else {
 				Object obj = readSimpleValue(jp, clazz);
 				result.put(key, obj);
 			}
 		}
 		
 		return result;
 	}
 	
 	/**
 	 * Reads the current token value, with special consideration for enums
 	 * @param jp
 	 * @param clazz
 	 * @return
 	 * @throws IOException
 	 */
 	private Object readSimpleValue(JsonParser jp, Class clazz) throws IOException {
 		if (jp.getCurrentToken() == JsonToken.VALUE_NULL)
 			return null;
 		
 		Object obj = null;
 		if (Enum.class.isAssignableFrom(clazz)) {
 			if (jp.getCurrentToken() == JsonToken.FIELD_NAME)
 				obj = jp.getCurrentName();
 			else
 				obj = jp.readValueAs(Object.class);
 			if (obj != null) {
 				String str = Helpers.camelCaseToEnum(obj.toString());
 				obj = Enum.valueOf(clazz, str);
 			}
 		} else {
 			if (jp.getCurrentToken() == JsonToken.FIELD_NAME)
 				obj = jp.getCurrentName();
 			else
 				obj = jp.readValueAs(clazz);
 		}
 		return obj;
 	}
 	
 	/**
 	 * Gets a field value from the parser, checking that it is the type expected
 	 * @param <T> The desired type of object returned
 	 * @param jp the parser
 	 * @param fieldName the name of the field to get
 	 * @param clazz the class of the type to get 
 	 * @return
 	 * @throws ServletException
 	 * @throws IOException
 	 */
 	private <T> T getFieldValue(JsonParser jp, String fieldName, Class<T> clazz) throws ServletException, IOException {
 		skipFieldName(jp, fieldName);
 
 		T obj = (T)jp.readValueAs(clazz);
 		return obj;
 	}
 
 	/**
 	 * Reads the next token and ensures that it is a field name called <code>fieldName</code>; leaves the current
 	 * token on the start of the field value
 	 * @param jp
 	 * @param fieldName
 	 * @throws ServletException
 	 * @throws IOException
 	 */
 	private void skipFieldName(JsonParser jp, String fieldName) throws ServletException, IOException {
 		if (jp.nextToken() != JsonToken.FIELD_NAME)
 			throw new ServletException("Cannot find field name - looking for " + fieldName + " found " + jp.getCurrentToken() + ":" + jp.getText());
 		String str = jp.getText();
 		if (!fieldName.equals(str))
 			throw new ServletException("Cannot find field called " + fieldName + " found " + str);
 		jp.nextToken();
 	}
 	
 }
