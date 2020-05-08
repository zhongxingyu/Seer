 /**
  * 
  */
 package ltg.commons;
 
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.Map;
 
 import org.jivesoftware.smack.packet.Message;
 
 import com.fasterxml.jackson.core.JsonProcessingException;
 import com.fasterxml.jackson.databind.JsonNode;
 import com.fasterxml.jackson.databind.ObjectMapper;
 import com.fasterxml.jackson.databind.node.ObjectNode;
 
 /**
  * @author tebemis
  *
  */
 public class LTGEventHandler {
 
 	private SimpleXMPPClient sc = null;
 	private ObjectMapper jsonParser = new ObjectMapper();
 	private Map<String, LTGEventListener> listeners = new HashMap<String, LTGEventListener>();
 
 
 	public LTGEventHandler(String fullJid, String password) {
 		sc = new SimpleXMPPClient(fullJid, password);
 	}
 
 
 	public LTGEventHandler(String fullJid, String password, String chatRoom)  {
 		sc = new SimpleXMPPClient(fullJid, password, chatRoom);
 	}
 
 
 	public void registerHandler(String eventType, LTGEventListener listener) {
 		listeners.put(eventType, listener);
 	}
 
 
 	public void runSynchronously() {
 		printRegisteredListeners();
 		// We are now connected and in the group chat room. If we don't do something
 		// the main thread will terminate. Let's go ahead and 
 		// wait for a message to arrive...
 		while (!Thread.currentThread().isInterrupted()) {
 			// ... and process it ...
 			processEvent(sc.nextMessage());
 		}
 		// ... and finally disconnect
 		sc.disconnect();
 	}
 
 
 	public void runAsynchronously() {
 		printRegisteredListeners();
 		sc.registerEventListener(new MessageListener() {
 			public void processMessage(Message m) {
 				processEvent(m);
 			}
 		});
 	}
 
 
 	public void close() {
 		sc.disconnect();
 	}
 
 
 	public void generateEvent(LTGEvent e) {
 		sc.sendMessage(serializeEvent(e));
 	}
 	
 	
 	public void generateEvent(String event, String destination, JsonNode payload) {
 		generateEvent(new LTGEvent(event, sc.getUsername(), destination, payload));
 	}
 
 
 	public void generatePrivateEvent(String destination, LTGEvent e) {
 		sc.sendMessage(destination, serializeEvent(e));
 	}
 
 	
 	public void generatePrivateEvent(String event, String destination, JsonNode payload) {
 		generateEvent(new LTGEvent(event, sc.getUsername(), destination, payload));
 	}
 
 	
 	private void processEvent(Message m) {
 		// Parse JSON
 		LTGEvent event = null;
 		try {
 			event = deserializeEvent(m.getBody());
 		} catch (Exception e) {
 			// Not JSON or wrong format, ignore and return
 			return;
 		}
 		// Process event
 		LTGEventListener el;
 		if (event!=null) {
 			el = listeners.get(event.getType());
 			if (el!=null)
 				el.processEvent(event);
 		}
 	}
 
 
 	private String serializeEvent(LTGEvent e) {
 		ObjectNode json = new ObjectMapper().createObjectNode();
 		json.put("event", e.getType());
 		if (e.getOrigin()!=null)
 			json.put("origin", e.getOrigin());
 		if (e.getDestination()!=null)
 			json.put("destination", e.getDestination());
 		json.put("payload", e.getPayload());
		return json.toString();
 	}
 
 
 	// This method deserializes JSON into an object
 	private LTGEvent deserializeEvent(String json) throws IOException, NotAnLTGEventException {
 		// Parse JSON
 		JsonNode jn = null;
 		try {
 			jn = jsonParser.readTree(json);
 		} catch (JsonProcessingException e) {
 			// Not JSON
 			throw new IOException();
 		} catch (IOException e) {
 			// Not JSON
 			throw new IOException();
 		}
 		String event = jn.path("event").textValue();
 		String origin = jn.path("origin").textValue();
 		String destination = jn.path("destination").textValue();
 		JsonNode payload = jn.path("payload");
 		// Validate fields
 		if(event==null || event.isEmpty()) 
 			throw new NotAnLTGEventException();
 		if (payload==null)
 			throw new NotAnLTGEventException();
 		// Create and return event
 		return new LTGEvent(event, origin, destination, payload);
 	}
 	
 	
 	private void printRegisteredListeners() {
 		String registeredListeners = " ";
 		for (String s: listeners.keySet())
			registeredListeners = registeredListeners + s + ", ";
 		System.out.print("Listening for events of type [");
		System.out.print(registeredListeners.substring(0, registeredListeners.length()-2)+" ]\n");
 	}
 
 }
