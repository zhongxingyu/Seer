 package ch.cern.atlas.apvs.ptu.server;
 
 import java.io.ByteArrayInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.text.ParseException;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import ch.cern.atlas.apvs.domain.Event;
 import ch.cern.atlas.apvs.domain.Measurement;
 import ch.cern.atlas.apvs.domain.Message;
 
 import com.cedarsoftware.util.io.JsonReader;
 
 public class PtuJsonReader extends JsonReader {
 
 	private Logger log = LoggerFactory.getLogger(getClass().getName());
 
 	public PtuJsonReader(InputStream in) {
 		super(in);
 	}
 
 	public PtuJsonReader(InputStream in, boolean noObjects) {
 		super(in, noObjects);
 	}
 
 	@Override
 	public Object readObject() throws IOException {
 		List<Message> result = new ArrayList<Message>();
 
 		@SuppressWarnings("rawtypes")
 		JsonObject jsonObj = (JsonObject) readIntoJsonMaps();
 
 		String sender = (String) jsonObj.get("Sender");
 		// String receiver = (String) jsonObj.get("Receiver");
 		// String frameID = (String) jsonObj.get("FrameID");
 		// String acknowledge = (String) jsonObj.get("Acknowledge");
 
 		@SuppressWarnings({ "rawtypes", "unchecked" })
 		List<JsonObject> msgs = (List<JsonObject>) jsonObj.get("Messages");
 		JsonMessage[] messages = new JsonMessage[msgs.size()];
 
 		for (int i = 0; i < messages.length; i++) {
 			@SuppressWarnings("rawtypes")
 			JsonObject msg = msgs.get(i);
 			String type = (String) msg.get("Type");
 			if (type.equals("Measurement")) {
 				String sensor = (String) msg.get("Sensor");
 				String unit = (String) msg.get("Unit");
 				Number value = convertToDouble(msg.get("Value"));
 				String time = (String)msg.get("Time");
 				String samplingRate = (String) msg.get("SamplingRate");
 				
 				// fix for #486 and #490
 				if ((sensor == null) || (value == null) || (unit == null) || (time == null)) {
 					log.warn("PTU "+sender+": Measurement contains <null> sensor, value, samplingrate, unit or time ("+sensor+", "+value+", "+unit+", "+samplingRate+", "+time+")");
 					continue;
 				}
 
 				Number low = convertToDouble(msg.get("DownThreshold"));
 				Number high = convertToDouble(msg.get("UpThreshold"));
 
 				// Scale down to microSievert
 				value = Scale.getValue(value, unit);
 				low = Scale.getLowLimit(low, unit);
 				high = Scale.getHighLimit(high, unit);
 				unit = Scale.getUnit(unit);
 
 				result.add(new Measurement(sender, sensor, value, low, high,
 						unit,
 						Integer.parseInt(samplingRate),
 						convertToDate(time)));
 			} else if (type.equals("Event")) {
 				result.add(new Event(sender, (String) msg.get("Sensor"),
 						(String) msg.get("EventType"), convertToDouble(msg
 								.get("Value")), convertToDouble(msg
 								.get("Threshold")), (String) msg.get("Unit"),
 						convertToDate(msg.get("Time"))));
 			} else {
 				log.warn("Message type not implemented: " + type);
 			}
 			// FIXME add other types of messages, #115 #112 #114
 		}
 
 		// returns a list of messages
 		return result;
 	}
 
 	private Double convertToDouble(Object number) {
 		if ((number == null) || !(number instanceof String)) {
 			return null;
 		}
 		try {
 			return Double.parseDouble((String) number);
 		} catch (NumberFormatException e) {
 			return null;
 		}
 	}
 
 	@Override
 	protected Date convertToDate(Object rhs) {
 		try {
 			return PtuServerConstants.dateFormat.parse((String) rhs);
 		} catch (ParseException e) {
 			return null;
 		}
 	}
 
 	public static List<Message> jsonToJava(String json) throws IOException {
 		ByteArrayInputStream ba = new ByteArrayInputStream(
 				json.getBytes("UTF-8"));
 		PtuJsonReader jr = new PtuJsonReader(ba, false);
 		@SuppressWarnings("unchecked")
 		List<Message> result = (List<Message>) jr.readObject();
 		jr.close();
 		return result;
 	}
 
 	public static List<Message> toJava(String json) {
 		try {
 			return jsonToJava(json);
 		} catch (Exception ignored) {
 			return null;
 		}
 	}
 }
