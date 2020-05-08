 package cz.cvut.fel.reposapi.assembla.client;
 
 import java.io.IOException;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 
 import org.codehaus.jackson.JsonParseException;
 import org.codehaus.jackson.JsonParser;
 import org.codehaus.jackson.JsonProcessingException;
 import org.codehaus.jackson.map.DeserializationContext;
 import org.codehaus.jackson.map.JsonDeserializer;
 
 public class AssemblaDateDeserializer extends JsonDeserializer<Date> {
 
 	@Override
 	public Date deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException, JsonProcessingException {
 		// example: 2013-04-29T19:03:36Z
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
 		String text = jp.getText();

 		try {
 			return format.parse(text);
 		} catch (ParseException e) {
 			throw new JsonParseException("Error parsing datetime", jp.getCurrentLocation(), e);
 		}
 	}
 }
