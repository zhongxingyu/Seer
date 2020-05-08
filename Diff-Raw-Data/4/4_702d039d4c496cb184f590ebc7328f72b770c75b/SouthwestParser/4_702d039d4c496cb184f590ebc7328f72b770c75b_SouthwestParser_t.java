 package visiblehand.parser.air;
 
 import java.io.IOException;
 import java.text.DateFormat;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import javax.mail.Message;
 import javax.mail.MessagingException;
 
 import lombok.Data;
 import lombok.Getter;
 
 import org.jsoup.Jsoup;
 import org.jsoup.nodes.Document;
 import org.jsoup.nodes.Element;
 import org.jsoup.select.Elements;
 
 import visiblehand.entity.Airline;
 import visiblehand.entity.Airport;
 import visiblehand.entity.Flight;
 import visiblehand.entity.Route;
 
 import com.avaje.ebean.Ebean;
 
 // United Airlines email receipt parser for older receipts
 // subjects of the form "Your United flight confirmation..."
 
 public @Data
 class SouthwestParser extends AirParser {
 	private final String fromString = "SouthwestAirlines@luv.southwest.com";
 	private final String subjectString = "Southwest Airlines Confirmation";
 	private final String bodyString = "";
 
 	@Getter(lazy = true)
 	private final Airline airline = Ebean.find(Airline.class, 4547);
 	
 	private boolean active = true;
 
 	private final DateFormat dateFormat = new SimpleDateFormat("EEE MMM d");
 
 	public AirReceipt parse(Message message) throws ParseException,
 			MessagingException, IOException {
 
 		AirReceipt receipt = new AirReceipt();
 		String content = getContent(message);
 		receipt.setFlights(getFlights(content, message.getSentDate()));
 		receipt.setAirline(getAirline());
		receipt.setConfirmation(getConfirmation(message.getSubject()));
 		receipt.setDate(message.getSentDate());
 
 		return receipt;
 	}
 
 	protected static String getConfirmation(String subject)
 			throws ParseException {
		System.out.println(subject);
 		return subject.substring(subject.length() - 6);
 	}
 
 	protected List<Flight> getFlights(String content, Date sentDate)
 			throws ParseException {
 		List<Flight> flights = new ArrayList<Flight>();
 
 		content = content.replaceAll("<!-- Start Flight Info -->",
 				"<flightInfo></flightInfo>");
 		Document doc = Jsoup.parse(content);
 		Elements flightTables = doc.select("flightInfo + table");
 
 		// store last date, for connections
 		Date date = null;
 
 		for (Element flightTable : flightTables) {
 			if (flightTable.hasText()) {
 				Elements cells = flightTable.select("td");
 				if (cells.size() < 4) {
 					throw new ParseException("Not enough cells in flight table", 0);
 				}
 				String dateString = cells.get(1).text(), number = cells.get(2)
 						.text(), itinerary = cells.get(3).text();
 
 				Flight flight = new Flight();
 				flight.setAirline(getAirline());
 				flight.setNumber(Integer.parseInt(number));
 				if (dateString.matches("\\s*")) {
 					flight.setDate(date);
 				} else {
 					date = getDate(sentDate, dateString);
 					flight.setDate(date);
 				}
 
 				Matcher matcher = Pattern.compile(
 						"[^(]*\\((\\w{3})\\)[^(]*\\((\\w{3})\\).*").matcher(
 						itinerary);
 				if (matcher.find()) {
 					String depart = matcher.group(1);
 					String arrive = matcher.group(2);
 					Airport source = Ebean.find(Airport.class).where()
 							.eq("code", depart).findUnique();
 					Airport destination = Ebean.find(Airport.class).where()
 							.eq("code", arrive).findUnique();
 					Route route = Ebean.find(Route.class).where()
 							.eq("airline", getAirline()).eq("source", source)
 							.eq("destination", destination).findUnique();
 					flight.setRoute(route);
 					flights.add(flight);
 				}
 			}
 		}
 
 		return flights;
 	}
 
 	// get the StartFlightInfo comment nodes
 	private static Date getDate(Date sentDate, String dateString) {
 		// TODO implement this
 		return null;
 	}
 }
