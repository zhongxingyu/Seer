 package com.axiomalaska.sos;
 
 import java.io.ByteArrayInputStream;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.TimeZone;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 
 import org.apache.log4j.Logger;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 
 import com.axiomalaska.sos.data.Phenomenon;
 import com.axiomalaska.sos.data.Station;
 import com.axiomalaska.sos.data.ObservationCollection;
 import com.axiomalaska.sos.tools.HttpSender;
 import com.axiomalaska.sos.xmlbuilder.GetObservationLatestBuilder;
 import com.axiomalaska.sos.xmlbuilder.InsertObservationBuilder;
 
 /**
  * This class goes through a station's phenomenons/sensors and enters their 
  * observations into the SOS server. Create this class with the SOS URL passed in.
  * Then call the updateObservations method with the station. This method goes 
  * through each phenomenon and requests observations from the date time of 
  * the newest observation until the current date. Then each collection of observations 
  * are sent to the SOS server with a InsertObservation request. 
  * 
  * @author Lance Finfrock
  */
 public class StationObservationsUpdater {
 
 	// -------------------------------------------------------------------------
 	// Private Data
 	// -------------------------------------------------------------------------
 
 	private SimpleDateFormat parseDate = new SimpleDateFormat(
 			"yyyy-MM-dd'T'HH:mm:ss.sss'Z'");
 	private HttpSender httpSender = new HttpSender();
 	private String sosUrl;
 	private Logger logger;
 
 	// -------------------------------------------------------------------------
 	// Constructor
 	// -------------------------------------------------------------------------
 
 	/**
 	 * Create the class with the SOS URL
 	 * @param sosUrl
 	 */
 	public StationObservationsUpdater(String sosUrl) {
 		this(sosUrl, Logger.getRootLogger());
 	}
 	
 	public StationObservationsUpdater(String sosUrl, Logger logger) {
 		this.sosUrl = sosUrl;
 		this.logger = logger;
 	}
 
 	// -------------------------------------------------------------------------
 	// Public Members
 	// -------------------------------------------------------------------------
 
 	/**
 	 * Updates the station's phenomenon's observations in the SOS server.
 	 * 
 	 * @param station - the station to update
 	 */
 	public void updateObservations(Station station) throws Exception {
 		for (Phenomenon phenomenon : station.getPhenomena()) {
 			updateObservations(station, phenomenon);
 		}
 	}
 
 	/**
 	 * Update the observations of a station with a specific phenomenon in the SOS server.
 	 * 
 	 * @param station - the station to update
 	 * @param phenomenon - the phenomenon to update
 	 */
 	public void updateObservations(Station station, Phenomenon phenomenon)
 			throws Exception {
 		Calendar startDate = getLatestObservationDate(station, phenomenon);
 		Calendar endDate = Calendar.getInstance();
 		ObservationCollection observationCollection = station.getObservationCollection(
 				phenomenon, startDate, endDate);
 		if (isObservationCollectionValid(observationCollection, station, phenomenon)) {
 			try {
 				InsertObservationBuilder insertObservationBuilder = new InsertObservationBuilder(
 						station, observationCollection);
 
 				String insertXml = insertObservationBuilder.build();
 
 				String response = httpSender.sendPostMessage(sosUrl, insertXml);
 
 				if (response.contains("Exception")) {
 					logger.error("Inputed "
 							+ observationCollection.getObservationDates().size()
 							+ " observations from station: "
 							+ station.getProcedureId() + " and phenomenon: "
 							+ phenomenon.getName() + " from: " + "startDate: "
 							+ formatDate(startDate) + " endDate: "
 							+ formatDate(endDate) + " response: \n" + response);
 				} else {
 					logger.info("Inputed "
 							+ observationCollection.getObservationDates().size()
 							+ " observations from station: "
 							+ station.getProcedureId() + " and phenomenon: "
 							+ phenomenon.getName() + " from: " + "startDate: "
 							+ formatDate(startDate) + " endDate: "
 							+ formatDate(endDate));
 				}
 			} catch (Exception e) {
 				logger.error("Inputed "
 						+ observationCollection.getObservationDates().size()
 						+ " observations from station: "
 						+ station.getProcedureId() + " and phenomenon: "
 						+ phenomenon.getName() + " from: " + "startDate: "
 						+ formatDate(startDate) + " endDate: "
 						+ formatDate(endDate) + " message: \n" + e.getMessage());
 			}
 		}
 	}
 	
 	// -------------------------------------------------------------------------
 	// Private Members
 	// -------------------------------------------------------------------------
 
 	/**
 	 * A method to validate the ObservationCollection object. 
 	 * 
 	 * @return [True] if the ObservationCollection object is valid. [False] if the
 	 * ObservationCollection is not valid
 	 */
 	private boolean isObservationCollectionValid(ObservationCollection observationCollection, 
 			Station station, Phenomenon phenomenon){
 		if (observationCollection == null){
 			logger.info("No values from source "
 					+ " for station: "
 					+ station.getProcedureId() + " and phenomenon: "
 					+ phenomenon.getName());
 			return false;
 		}
 	    if (observationCollection.getObservationDates().size() != observationCollection.getObservationValues().size()){
 			logger.info("The observationCollection's size of the dates list is not equal to the values list "
 					+ " for station: "
 					+ station.getProcedureId() + " and phenomenon: "
 					+ phenomenon.getName());
 	    	return false;
 	    }
	    if(observationCollection.getObservationDates().size() == 0
				&& observationCollection.getObservationValues().size() == 0){
 			logger.info("No values from source "
 					+ " for station: "
 					+ station.getProcedureId() + " and phenomenon: "
 					+ phenomenon.getName());
 			return false;
 		}
 		
 		return true;
 	}
 	
 	/**
 	 * Convert the date into a readable string. 
 	 */
 	private String formatDate(Calendar date) {
 		Calendar localDate = (Calendar) date.clone();
 
 		String text = localDate.get(Calendar.YEAR) + "/"
 				+ (localDate.get(Calendar.MONTH) + 1) + "/"
 				+ localDate.get(Calendar.DAY_OF_MONTH) + " "
 				+ localDate.get(Calendar.HOUR_OF_DAY) + ":"
 				+ localDate.get(Calendar.MINUTE) + " "
 				+ localDate.getTimeZone().getID();
 		return text;
 	}
 	
 	/**
 	 * Get the newest observation date from the SOS server for the station and phenomenon. 
 	 * 
 	 * @param station - the station to look up the date from
 	 * @param phenomenon - the phenomenon to look up the date from
 	 */
 	private Calendar getLatestObservationDate(Station station,
 			Phenomenon phenomenon) throws Exception {
 		GetObservationLatestBuilder getObservationLatestBuilder = 
 				new GetObservationLatestBuilder(station, phenomenon);
 
 		String getObservationXml = getObservationLatestBuilder.build();
 
 		String response = httpSender.sendPostMessage(sosUrl, getObservationXml);
 
 		DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory
 				.newInstance();
 		DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
 		Document doc = docBuilder.parse(new ByteArrayInputStream(response
 				.getBytes()));
 
 		doc.normalize();
 
 		/*
 		 * <gml:TimePeriod xsi:type="gml:TimePeriodType">
 		 * <gml:beginPosition>2012-02-03T03:00:00.000Z</gml:beginPosition>
 		 * <gml:endPosition>2012-02-03T03:00:00.000Z</gml:endPosition>
 		 * </gml:TimePeriod>
 		 */
 		NodeList nodeList = doc.getElementsByTagName("gml:TimePeriod");
 
 		if (nodeList.getLength() == 1) {
 
 			Element timePeriod = (Element) nodeList.item(0);
 
 			Node beginPosition = timePeriod.getElementsByTagName(
 					"gml:beginPosition").item(0);
 
 			Calendar date = createDate(beginPosition.getTextContent());
 			
 			date.add(Calendar.MINUTE, 1);
 
 			return date;
 		} else {
 			logger.info("station: " + station.getProcedureId() + " phenomenon: " + phenomenon.getName() + " has no observations");
 			Calendar defaultDate = Calendar.getInstance();
 
 			defaultDate.add(Calendar.DAY_OF_MONTH, -3);
 
 			return defaultDate;
 		}
 	}
 
 	/**
 	 * Create a Calendar object from a string
 	 * @param dayRawText - the string to convert into a Calendar object
 	 */
 	@SuppressWarnings("deprecation")
 	private Calendar createDate(String dateString) throws ParseException {
 		Date date = parseDate.parse(dateString);
 		Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
 		calendar.set(Calendar.YEAR, date.getYear() + 1900);
 		calendar.set(Calendar.MONTH, date.getMonth());
 		calendar.set(Calendar.DAY_OF_MONTH, date.getDate());
 		calendar.set(Calendar.HOUR_OF_DAY, date.getHours());
 		calendar.set(Calendar.MINUTE, date.getMinutes());
 		calendar.set(Calendar.SECOND, date.getSeconds());
 
 		// The time is not able to be changed from the
 		// setTimezone if this is not set. Java Error
 		calendar.getTime();
 
 		return calendar;
 	}
 }
