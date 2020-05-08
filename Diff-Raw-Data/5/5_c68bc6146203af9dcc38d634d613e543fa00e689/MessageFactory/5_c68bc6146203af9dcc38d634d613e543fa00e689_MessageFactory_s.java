 package ar.edu.itba.pdc.duta.http;
 
 import java.io.UnsupportedEncodingException;
 import java.util.HashMap;
 import java.util.Map;
 
 import ar.edu.itba.pdc.duta.http.model.Message;
 import ar.edu.itba.pdc.duta.http.model.MessageHeader;
 import ar.edu.itba.pdc.duta.http.model.ResponseHeader;
import ar.edu.itba.pdc.duta.net.buffer.FixedDataBuffer;
 
 public final class MessageFactory {
 	
 	private MessageFactory() {
 	}
 	
 	public static Message build404() {
 		return build(404, "Not Found", "");
 	}
 	
 	public static Message build200(String body) {
 		return build(200, "OK", body);
 	}
 	
 	public static Message build(int code, String reason, String body) {
 		
 		byte[] bytes;
 		try {
 			bytes = body.getBytes("UTF8");
 		} catch (UnsupportedEncodingException e) {
 			// This shouldnt happen.
 			return build500();
 		}
 		
 		Map<String, String> fields = new HashMap<String, String>();
 		
 		fields.put("Via", "dUta");
 		fields.put("Content-Length", String.valueOf(bytes.length));
 		
 		MessageHeader header = new ResponseHeader(Grammar.HTTP11, code, reason, fields);
 		Message message = new Message(header);
 		
		message.setBody(new FixedDataBuffer(bytes));
 		//FIXME: Release the data buffer!
 		
 		return message;
 	}
 
 	private static Message build500() {
 		return build(500, "Internal server error", "");
 	}
 
 	public static Message build400() {
 		return build(400, "Bad Request", "");
 	}
 
 }
