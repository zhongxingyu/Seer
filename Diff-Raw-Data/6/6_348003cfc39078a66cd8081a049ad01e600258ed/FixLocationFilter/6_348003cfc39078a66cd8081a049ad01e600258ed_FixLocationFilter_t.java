 /* --------------------------------------------------------------------------
  * @author Hauke Walden
  * @created 28.06.2011 
  * Copyright 2011 by Hauke Walden 
  * All rights reserved.
  * --------------------------------------------------------------------------
  */
 
 package de.mbaaba.calendar;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.net.URL;
 import java.net.URLConnection;
 import java.net.URLEncoder;
 import java.util.HashMap;
 
 import com.google.gson.Gson;
 
 import de.mbaaba.directions.GoogleDirections;
 import de.mbaaba.directions.Legs;
 import de.mbaaba.directions.Routes;
 import de.mbaaba.util.Configurator;
 import de.mbaaba.util.Logger;
 
 /**
  * The FixLocationFilter is used to fix the location of an event by looking up the given location at googles directions API.
  * It normalizes the given location by using the end-address of the location.
  * If the location is far away from the default location, an error is assumed (like, a room is given in the location field)
  * In that case, a default location is used instead.  
  */
 public class FixLocationFilter implements ICalendarFilter {
 
 	/**
 	 * A logger for this class.
 	 */
 	private static final Logger LOG = new Logger(FixLocationFilter.class);
 
 	static final String DEFAULT_LOCATION = "default.location";
 
 	/**
 	 * A request cache.
 	 */
 	private static HashMap<String, String> cachedLocations = new HashMap<String, String>();
 
 	private static final int SANITY_LENGTH = 8;
 
 	private static final Number ONE_HUNDRED_KILOMETER = 100000;
 
 	private final Configurator configurator;
 
 	public FixLocationFilter(Configurator aConfigurator) {
 		configurator = aConfigurator;
 
 	}
 
 	@Override
 	public boolean passes(ICalendarEntry aParamCalendarEntry) {
 
 		final String defaultLocation = configurator.getProperty(DEFAULT_LOCATION, "");
 
 		String room = aParamCalendarEntry.getRoom();
 		if (room == null) {
 			room = "";
 		}
 		String location = aParamCalendarEntry.getLocation();
 		if (location == null) {
 			location = "";
 		}
 
 		if (room.isEmpty()) {
 			// if no room is set, it is probably set in the location instead.
 			if (!location.isEmpty()) {
 				aParamCalendarEntry.setRoom(location);
 
 			}
 		}
 
 		try {
 			String fixedLocation = fixLocation(location, defaultLocation);
 			aParamCalendarEntry.setLocation(fixedLocation);
 		} catch (IOException e) {
			LOG.error("Could not check location \"" + location + "\" for entry \"" + aParamCalendarEntry.getSubject() + "\": "
					+ e.getMessage(), e);
 		}
 		// Let this entry pass regardless of whether the location could be fixed.
 		return true;
 	}
 
 	private String fixLocation(final String aOriginalLocation, String aDefaultLocation) throws IOException {
 		String result;
 		if (cachedLocations.containsKey(aOriginalLocation)) {
 			result = cachedLocations.get(aOriginalLocation);
 		} else {
 			result = aDefaultLocation;
 			if (aOriginalLocation.length() >= SANITY_LENGTH) {
 				final URL url = new URL("http://maps.google.de/maps/api/directions/json?origin="
 						+ URLEncoder.encode(aDefaultLocation, "UTF-8") + "&destination="
						+ URLEncoder.encode(aOriginalLocation, "UTF-8") + "&sensor=true");
 
 				final Gson gson = new Gson(); // Or use new GsonBuilder().create();
 
 				final URLConnection conn = url.openConnection();
 				final BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
 				final GoogleDirections directions = gson.fromJson(rd, GoogleDirections.class);
 
 				if ((directions.getStatus().equals("OK"))) {
 					final Routes route0 = directions.getRoutes().get(0);
 					final Legs leg0 = route0.getLegs().get(0);
 					if (leg0.getDistance().getValue().longValue() > ONE_HUNDRED_KILOMETER.longValue()) {
 						// result is probably broken!
 						result = aDefaultLocation;
 					} else {
 						//TODO: check encoding problems
 						result = leg0.getEnd_address();
 					}
 				}
 			}
 			cachedLocations.put(aOriginalLocation, result);
 		}
 		return result;
 	}
 
 }
