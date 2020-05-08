 package com.axiomalaska.sos.example;
 
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.List;
 import java.util.TimeZone;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import com.axiomalaska.phenomena.Phenomena;
 import com.axiomalaska.phenomena.Phenomenon;
 import com.axiomalaska.sos.ObservationRetriever;
 import com.axiomalaska.sos.data.ObservationCollection;
 import com.axiomalaska.sos.data.SosSensor;
 import com.axiomalaska.sos.data.SosStation;
 import com.axiomalaska.sos.tools.HttpSender;
 
 public class CnfaicObservationRetriever implements ObservationRetriever {
 
 	// -------------------------------------------------------------------------
 	// Private Data
 	// -------------------------------------------------------------------------
 
 	private final static int DATE_INDEX = 2;
 	private final static int AIR_TEMPERATURE_INDEX = 3;
 	private final static int RELATIVE_HUMIDITY_INDEX = 4;
 	private final static int WIND_SPEED_INDEX = 5;
 	private final static int WIND_DIRECTION_INDEX = 6;
 	private final static int WIND_GUST_INDEX = 7;
 	
 	private HttpSender httpSender = new HttpSender();
 	private SimpleDateFormat parseDate = new SimpleDateFormat("yyyy-MM-dd HH");
 	
 	// -------------------------------------------------------------------------
 	// Public ObservationRetriever
 	// -------------------------------------------------------------------------
 
 	@Override
 	public ObservationCollection getObservationCollection(SosStation station,
 			SosSensor sensor, Phenomenon phenomenon, Calendar startDate) {
 		String hoursText = calculatedDifferenceFromNow(startDate);
 		
 		try {
 			String rawObservationData = httpSender.sendGetMessage(
 					"http://www.cnfaic.org/library/grabbers/nws_feed.php?hours=" + hoursText);
 			
 			Phenomenon airTemperaturePhenomenon = Phenomena.instance().AIR_TEMPERATURE;
 			Phenomenon relativeHumidityPhenomenon = Phenomena.instance().RELATIVE_HUMIDITY;
 			Phenomenon windSpeedPhenomenon = Phenomena.instance().WIND_SPEED;
 			Phenomenon windfromDirectionPhenomenon = Phenomena.instance().WIND_FROM_DIRECTION;
 			Phenomenon windSpeedofGustPhenomenon = Phenomena.instance().WIND_SPEED_OF_GUST;
 			
 			Pattern observationParser = 
					Pattern.compile(station.getId() + 
 							",((\\d{4}-\\d{2}-\\d{2} \\d{2}):\\d{2}:\\d{2},(\\d+.\\d+),(\\d+),(\\d+),(\\d+),(\\d+))");
 		
 			Matcher matcher = observationParser.matcher(rawObservationData);
 			List<Calendar> dateValues = new ArrayList<Calendar>();
 			List<Double> dataValues = new ArrayList<Double>();
 			while(matcher.find()){
 				String rawDate = matcher.group(DATE_INDEX);
 				Calendar date = createDate(rawDate);
 				if (date.after(startDate)) {
 					
 					dateValues.add(date);
 
 					if (phenomenon.getId().equals(
 							airTemperaturePhenomenon.getId())) {
 						String airTemperatureRaw = matcher
 								.group(AIR_TEMPERATURE_INDEX);
 						double airTemperature = Double
 								.parseDouble(airTemperatureRaw);
 
 						dataValues.add(airTemperature);
 					} else if (phenomenon.getId().equals(
 							relativeHumidityPhenomenon.getId())) {
 						String relativeHumidityRaw = matcher
 								.group(RELATIVE_HUMIDITY_INDEX);
 						double relativeHumidity = Double
 								.parseDouble(relativeHumidityRaw);
 
 						dataValues.add(relativeHumidity);
 					} else if (phenomenon.getId().equals(
 							windSpeedPhenomenon.getId())) {
 						String windSpeedRaw = matcher.group(WIND_SPEED_INDEX);
 						double windSpeed = Double.parseDouble(windSpeedRaw);
 
 						dataValues.add(windSpeed);
 					} else if (phenomenon.getId().equals(
 							windfromDirectionPhenomenon.getId())) {
 						String windDirectionRaw = matcher
 								.group(WIND_DIRECTION_INDEX);
 						double windDirection = Double
 								.parseDouble(windDirectionRaw);
 
 						dataValues.add(windDirection);
 					} else if (phenomenon.getId().equals(
 							windSpeedofGustPhenomenon.getId())) {
 						String windGustRaw = matcher.group(WIND_GUST_INDEX);
 						double windGust = Double.parseDouble(windGustRaw);
 
 						dataValues.add(windGust);
 					}
 				}
 			}
 			
 			ObservationCollection observationCollection = new ObservationCollection();
 			
 			observationCollection.setObservationDates(dateValues);
 			observationCollection.setObservationValues(dataValues);
 			observationCollection.setSensor(sensor);
 			observationCollection.setStation(station);
 			observationCollection.setPhenomenon(phenomenon);
 			
 			return observationCollection;
 		} catch (Exception e) {
 			System.out.println(e.getMessage());
 			return null;
 		}
 	}
 	
 	// -------------------------------------------------------------------------
 	// Private Members
 	// -------------------------------------------------------------------------
 
 	@SuppressWarnings("deprecation")
 	private Calendar createDate(String rawDate) throws ParseException {
 		Date date = parseDate.parse(rawDate);
 
 		Calendar calendar = Calendar.getInstance(TimeZone
 				.getTimeZone("US/Alaska"));
 		calendar.set(Calendar.YEAR, date.getYear() + 1900);
 		calendar.set(Calendar.MONTH, date.getMonth());
 		calendar.set(Calendar.DAY_OF_MONTH, date.getDate());
 		calendar.set(Calendar.HOUR_OF_DAY, date.getHours());
 		calendar.set(Calendar.MINUTE, 0);
 		calendar.set(Calendar.SECOND, 0);
 
 		// The time is not able to be changed from the
 		// setTimezone if this is not set. Java Error
 		calendar.getTime();
 
 		return calendar;
 	}
 
 	private String calculatedDifferenceFromNow(Calendar calendar) {
 		Calendar copyCalendar = (Calendar) calendar.clone();
 		copyCalendar.setTimeZone(TimeZone.getTimeZone("US/Alaska"));
 		Calendar now = Calendar.getInstance(TimeZone.getTimeZone("US/Alaska"));
 
 		long diff = now.getTime().getTime() - calendar.getTime().getTime();
 
 		int hourDiff = Math.round(diff / (1000 * 60 * 60));
 
 		if(hourDiff > 720){
 			return "720";
 		}
 		else{
 			return hourDiff + "";
 		}
 	}
 }
