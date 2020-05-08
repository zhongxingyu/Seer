 package de.uni.stuttgart.informatik.ToureNPlaner.Net;
 
 import org.codehaus.jackson.JsonFactory;
 import org.codehaus.jackson.map.ObjectMapper;
 import org.codehaus.jackson.smile.SmileFactory;
 
 public class JacksonManager {
 	static ObjectMapper jsonMapper;
 	static ObjectMapper smileMapper;
 
 	public static ObjectMapper getJsonMapper() {
 		if (jsonMapper == null)
 			jsonMapper = new ObjectMapper(new JsonFactory());
 		return jsonMapper;
 	}
 
 	public static ObjectMapper getSmileMapper() {
 		if (smileMapper == null)
 			smileMapper = new ObjectMapper(new SmileFactory());
 		return smileMapper;
 	}
 
 	public static ObjectMapper getMapper(ContentType type) {
 		switch (type) {
 			case SMILE:
 				return getSmileMapper();
 			case JSON:
 			default:
 				return getJsonMapper();
 		}
 	}
 
 	public enum ContentType {
 		JSON("application/json"), SMILE("application/x-jackson-smile");
 
 		public final String identifier;
 
 		private ContentType(String s) {
 			identifier = s;
 		}
 
 		public static ContentType parse(String header) {
			if (header == null)
				throw new IllegalArgumentException("No Content-Type Header.");
 			String[] s = header.split(";");
 			if (s.length < 0)
				throw new IllegalArgumentException("Wrong Content-Type Header. Was: \"" + header + "\".");
 
 			for (ContentType t : ContentType.values()) {
 				if (s[0].toLowerCase().equals(t.identifier))
 					return t;
 			}
 
			throw new IllegalArgumentException("Wrong Content-Type Header. Was: \"" + header + "\".");
 		}
 	}
 }
