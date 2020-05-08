 import java.util.ArrayList;
 import java.util.List;
 
 import org.jdom.Element;
 import org.joda.time.DateTime;
 import org.joda.time.DateTimeZone;
 import org.joda.time.format.DateTimeFormat;
 import org.joda.time.format.DateTimeFormatter;
 
 import com.sun.org.apache.xalan.internal.xsltc.compiler.Pattern;

 public class GoogleXMLParser extends AbstractXMLParser {
 
 	private static final String split_one = "\\s+| |,|-|<";
 	private static final String split_recur = "\\s+| |,|-|<|:";
 	private static final String TITLE = "title";
 	private static final String CONTENT = "content";
 
 	private Element parseGetEventsRoot() {
 		eventsRoot = doc.getRootElement();
 		return eventsRoot;
 	}
 
 	private String[] parseTime(Element element) {
 		String timeInfo = element.getChildText(CONTENT, null).toString();
 		return timeInfo.split("\n");
 	}
 
 	private DateTime parseStartTime(Element element) {
 		String[] timeInfoArray = parseTime(element);
 
 		if (timeInfoArray[0].startsWith("Recurring")) {
 			return parseRecurringEventStart(timeInfoArray[1].split(split_recur));
 		}
 		return parseOneTimeEventStart(timeInfoArray[0].split(split_one));
 	}
 
 	private DateTime parseEndTime(Element element) {
 		String[] timeInfoArray = parseTime(element);
 		
 		if (timeInfoArray[0].startsWith("Recurring")) {
 			return parseRecurringEventEnd(timeInfoArray[1].split(split_recur),
 			        timeInfoArray[3]);
 		}
 		return parseOneTimeEventEnd(timeInfoArray[0].split(split_one));
 	}
 	
 	private int[] parseOneTimeEvent(String[] timeInfoArray) {
 		int[] yearMonthDay = new int[3];
 		yearMonthDay[0] = parseOneTimeEventYear(timeInfoArray);
 		yearMonthDay[1] = parseOneTimeEventMonth(timeInfoArray);
 		yearMonthDay[2] = parseOneTimeEventDay(timeInfoArray);
 
 		return yearMonthDay;
 	}
 	//gets the year, month, day, hour, minute of a recurring event
 	private int[] parseRecurringEvent(String[] timeInfoArray) {
 
 		int[] DateTimeArray = new int[6];
 
 		for (int j = 0; j < 5; j++) {
 			DateTimeArray[j] = Integer.parseInt(timeInfoArray[j + 3]);
 		}
 		return DateTimeArray;
 	}
 	
 	private DateTime parseOneTimeEventStart(String[] timeInfoArray) {
 
 		int[] yearMonthDay = parseOneTimeEvent(timeInfoArray);
 
 		DateTimeZone dateTimeZone = DateTimeZone.forID("UTC"); // /fix this
 		String startTime = timeInfoArray[6];
 		String[] splitTime = startTime.split(":");
 
 		int[] hourMinute = parseHourMinute(splitTime, timeInfoArray);
 
 		return new DateTime(yearMonthDay[0], yearMonthDay[1], yearMonthDay[2],
 		        hourMinute[0], hourMinute[1], dateTimeZone);
 
 	}
 
 	private DateTime parseRecurringEventStart(String[] timeInfoArray) {
 		int[] DateTimeArray = parseRecurringEvent(timeInfoArray);
 
 		DateTimeZone dateTimeZone = DateTimeZone.forID("UTC");
 
 		return new DateTime(DateTimeArray[0], DateTimeArray[1],
 		        DateTimeArray[2], DateTimeArray[3], DateTimeArray[4],
 		        dateTimeZone);
 
 	}
 	
 	private DateTime parseOneTimeEventEnd(String[] timeInfoArray) {
 		DateTimeZone dateTimeZone = DateTimeZone.forID("UTC"); // /fix this
 
 		if (timeInfoArray.length >= 14) {
 			int year = Integer.parseInt(timeInfoArray[12]);
 			DateTimeFormatter format = DateTimeFormat.forPattern("MMM");
 			DateTime tempMonth = format.parseDateTime(timeInfoArray[9]);
 			int month = tempMonth.getMonthOfYear();
 			int day = Integer.parseInt(timeInfoArray[10]);
 
 			String endTime = timeInfoArray[13];
 			String[] splitTime = endTime.split(":");
 
 			int[] hourMinute = parseHourMinute(splitTime, timeInfoArray);
 
 			return new DateTime(year, month, day, hourMinute[0], hourMinute[1],
 			        dateTimeZone);
 		}
 		int[] yearMonthDay = parseOneTimeEvent(timeInfoArray);
 
 		String startTime = timeInfoArray[6];
 
 		String[] splitTime = startTime.split(":");
 		int[] hourMinute = parseHourMinute(splitTime, timeInfoArray);
 
 		return new DateTime(yearMonthDay[0], yearMonthDay[1], yearMonthDay[2],
 		        hourMinute[0], hourMinute[1], dateTimeZone);
 
 	}
 
 	private DateTime parseRecurringEventEnd(String[] timeInfoArray,
 	        String duration) {
 		String[] durationArray = duration.split(" ");
 		return parseRecurringEventStart(timeInfoArray).plusMinutes(
 		        Integer.parseInt(durationArray[1]));
 	}
 
 	private int[] parseHourMinute(String[] splitTime, String[] timeInfoArray) {
 		int[] hourMinute = new int[2];
 
 		if (timeInfoArray.length < 9) {
 			hourMinute[0] = 12;
 			hourMinute[1] = 00;
 			return hourMinute;
 		}
 
 		if (splitTime.length == 2) {
 			hourMinute[0] = Integer.parseInt(splitTime[0]);
 			hourMinute[1] = Integer.parseInt(splitTime[1].substring(0,
 			        splitTime[1].length() - 2));
 		}
 
 		else {
 			hourMinute[0] = Integer.parseInt(splitTime[0].substring(0,
 			        splitTime[0].length() - 2));
 			hourMinute[1] = 00;
 		}
 		return hourMinute;
 	}
 
 	private int parseOneTimeEventYear(String[] timeInfoArray) {
 		return Integer.parseInt(timeInfoArray[5]);
 	}
 
 	private int parseOneTimeEventDay(String[] timeInfoArray) {
 		return Integer.parseInt(timeInfoArray[3]);
 	}
 
 	private int parseOneTimeEventMonth(String[] timeInfoArray) {
 		DateTimeFormatter format = DateTimeFormat.forPattern("MMM");
 		DateTime tempMonth = format.parseDateTime(timeInfoArray[2]);
 		return tempMonth.getMonthOfYear();
 	}
 
 
 
 	private String parseTitle(Element event) {
 		return event.getChildText(TITLE, null).toString();
 	}
 
 	private String parseContent(Element event, String finder) {
 		String[] summary = event.getChildText(CONTENT, null).toString()
 		        .split("<br />");
 		String information = null;
 		for (String line : summary) {
 			if (line.contains(finder))
 				information = line.substring(line.indexOf(": ") + 1);
 		}
 		return information;
 
 	}
 
 	private String parseDescription(Element event) {
 		return parseContent(event, "Event Description:");
 	}
 
 	private String parseLocation(Element event) {
 		return parseContent(event, "Where:");
 	}
 
 	@Override
 	public List<Event> processEvents() {
 		parseGetEventsRoot();
 
 		@SuppressWarnings("unchecked")
 		List<Element> XMLChildren = (List<Element>) eventsRoot.getChildren(
 		        "entry", null);
 
 		List<Event> newXMLChildren = new ArrayList<Event>();
 
 		for (Element event : XMLChildren) {
 
 			String title = parseTitle(event);
 			String description = parseDescription(event);
 			String location = parseLocation(event);
 			DateTime startTime = parseStartTime(event);
 			DateTime endTime = parseEndTime(event);
 			Event newEvent = new Event(title, startTime, endTime, description,
 			        location);
 
 			System.out.println(title);
 
 			newXMLChildren.add(newEvent);
 		}
 
 		return newXMLChildren;
 	}
 
 	public static XMLParserFactory getFactory() {
 		return new XMLParserFactory(new GoogleXMLParser());
 	}
 
 	public static void main(String[] args) {
 		GoogleXMLParser parser = new GoogleXMLParser();
 		parser.loadFile("http://www.cs.duke.edu/courses/cps108/current/assign/02_tivoo/data/googlecal.xml");
 		// parser.loadFile("https://www.google.com/calendar/feeds/kathleen.oshima%40gmail.com/private-cf4e2a2cf06315dece847f9aaf867f3e/basic");
 		List<Event> listOfEvents = parser.processEvents();
 
 		for (Event event : listOfEvents) {
 			System.out.println(event.toString());
 		}
 	}
}
