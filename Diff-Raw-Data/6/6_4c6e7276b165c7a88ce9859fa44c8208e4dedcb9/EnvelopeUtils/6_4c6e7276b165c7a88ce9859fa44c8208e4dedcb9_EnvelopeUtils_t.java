 package pegasus.esp;
 
 import java.io.UnsupportedEncodingException;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.UUID;
 
 import pegasus.eventbus.client.Envelope;
 
 import com.google.common.collect.Maps;
 import com.google.gson.Gson;
 import com.google.gson.GsonBuilder;
 
 public class EnvelopeUtils {
 
 	public static Map<String, UUID> idMappings = Maps.newHashMap();
 
 	/**
 	 * Translate between human readable IDs and internal UUIDs.  This method finds the UUID
 	 * that corresponds to the specified string and returns it.  If there isn't a UUID for
 	 * that string already, then a new UUID is allocated and returned, caching the correspondence
 	 * for future references of that string.
 	 *
 	 * @param idsymbol a string representing a unique ID, or null to create a new unique UUID
 	 * @return the UUID that permanently corresponds to the string
 	 */
 	public static UUID symIdToRealId(String idsymbol) {
 	    UUID id = null;
 
 	    if (idsymbol != null) {
 	        id = idMappings.get(idsymbol);
 	    }
 
 	    if (id == null) {
 	        id = UUID.randomUUID();
 	        idMappings.put(idsymbol, id);
 	    }
 	    return id;
 	}
 
	private static long testTime = 12104;
	private static long timeIncr = 2000;
 
 	public static Envelope makeEnvelope(String type, String idsymbol, String correlationIdsymbol,
 	        String topic, String replyTo) {
 	    Envelope e = new Envelope();
 	    e.setEventType(type);
 	    e.setId(symIdToRealId(idsymbol));
 	    if (correlationIdsymbol != null) {
 	        e.setCorrelationId(symIdToRealId(correlationIdsymbol));
 	    }
 	    e.setTopic(topic);
 	    e.setReplyTo(replyTo);
 	    Date timestamp = new Date(testTime);
 	    testTime += timeIncr;
         e.setTimestamp(timestamp);
        e.setBody((type + topic).getBytes());
 	    return e;
 	}
 
 	private final static Gson gson_pp = new GsonBuilder().setPrettyPrinting().create();
 	private final static Gson gson = new Gson();
 
 	public static String toFormattedJson(String line) {
         HashMap map = gson.fromJson(line, HashMap.class);
         String mapstr = gson.toJson(map);
         return mapstr;
     }
 
 	public static String toJson(Envelope env) {
 	    return gson.toJson(env, Envelope.class);
 	}
 
 	public static String toPrettyJson(Envelope env) {
 	    return gson_pp.toJson(env, Envelope.class);
 	}
 
 	public static String envelopeToReadableJson(Envelope env) {
 		byte[] body = env.getBody();
 		String bodyJson = "";
         try {
             bodyJson = new String(body, "UTF-8");
         } catch (UnsupportedEncodingException e) {
             e.printStackTrace();
         }
         String bodyformatted = toFormattedJson(bodyJson);
         JsonString json = new JsonString().start()
 				.add("EVENT_TYPE", env.getEventType())
 				.add("REPLYTO", env.getReplyTo())
 				.add("TOPIC", env.getTopic())
 				.add("ID", env.getId())
 				.add("CORRELATION_ID", env.getCorrelationId())
 				.add("TIMESTAMP", env.getTimestamp())
 				.add("HEADERS", env.getHeaders())
 				.add("BODY_SIZE", body.length)
 				.add("BODY_SRC", body)
 				.add("BODY_JSON", bodyJson)
 				.add("BODY", bodyformatted)
 				;
 		return json.end().toString();
 	}
 
     public static Envelope fromJson(String line) {
         Envelope envelope = gson.fromJson(line, Envelope.class);
         return envelope;
     }
 }
