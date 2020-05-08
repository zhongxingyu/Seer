 package com.financial.tools.recorderserver.util;
 
 import java.io.IOException;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.fasterxml.jackson.core.JsonParser;
 import com.fasterxml.jackson.core.JsonProcessingException;
 import com.fasterxml.jackson.databind.DeserializationContext;
 import com.fasterxml.jackson.databind.JsonDeserializer;
 
 public class CustomJsonDateDeserializer extends JsonDeserializer<Date> {
 
 	private static Logger logger = LoggerFactory.getLogger(CustomJsonDateDeserializer.class);
 
 	@Override
 	public Date deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException, JsonProcessingException {
		SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy");
 		String date = jp.getText();
 		try {
 			return format.parse(date);
 		} catch (ParseException e) {
			logger.error("Failed to parse date json string value with format: dd/MM/yyyy.");
 			throw new RuntimeException(e);
 		}
 	}
 
 }
