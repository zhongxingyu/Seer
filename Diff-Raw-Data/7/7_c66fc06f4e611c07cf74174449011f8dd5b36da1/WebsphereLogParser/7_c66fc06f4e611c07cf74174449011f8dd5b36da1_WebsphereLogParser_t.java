 package de.age.logtool.parsing;
 
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.regex.Pattern;
 
 
 public class WebsphereLogParser extends AbstractLogParser {
 
 	private static final SimpleDateFormat FORMAT = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss:SSS");
	private static final Pattern pattern = Pattern.compile("\\[\\d{2}\\.\\d{2}\\.\\d{4} \\d{2}:\\d{2}:\\d{2}:\\d{3} CET\\].*");
 	private StringBuilder buffer;
 	
 	public WebsphereLogParser() {
 		buffer = new StringBuilder();
 	}
 	
 	@Override
 	public void parse(String input) {
 		if (buffer.length() > 0 && isInputWithTimestamp(input)) {
 			flushBuffer();
 		} else {
 			buffer.append(input);
 		}
 	}
 
 	private boolean isInputWithTimestamp(String input) {
		return pattern.matcher(input).find();
 	}
 	
 	/**
 	 * Parses the current content of the buffer as one event and fires that event.
 	 */
 	private void flushBuffer() {
 		if (pattern.matcher(buffer.toString()).find()) {
 		try {
 			Date parse = FORMAT.parse(buffer.substring(1, 24));
 			fireLogEvent(parse.getTime(), buffer.toString());
 		} catch (ParseException e) {
 			// TODO
 			throw new RuntimeException();
 		}
 		}
 		buffer = new StringBuilder();
 	}
 
 }
